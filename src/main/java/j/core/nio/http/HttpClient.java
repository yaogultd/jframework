package j.core.nio.http;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import j.core.common.JProperties;
import j.core.fs.JDFS;
import j.core.hp.asynchronous.Waitings;
import j.core.nio.*;
import j.core.nvwa.Nvwa;
import j.core.serialize.JSerialization;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.JUtilBytes;
import j.util.JUtilMath;
import j.util.JUtilString;
import j.util.JUtilUUID;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

@ClassDescription(author = "肖炯",
        date = "2021/10/01",
        description = "基于NIO实现的类Http Client")
public class HttpClient extends ClientHandler {
    private static Logger log=Logger.create(HttpClient.class);

    //客户端ID
    private String uuid;

    //请求ID
    private static long requestId=0;

    //锁
    //private final Object lock=new Object();

    /**
     * 得到递增请求ID
     * @return
     */
    synchronized public static long getRequestId(){
        requestId++;
        if(requestId == Long.MAX_VALUE) requestId=1;
        return requestId;
    }

    /**
     *
     */
    protected HttpClient(){

    }

    /**
     * @param host
     * @param port
     * @param socketOptions
     * @param args
     */
    public HttpClient(String host, int port, Map<Integer, Object> socketOptions, Object[] args) {
        super(host, port, socketOptions, args);
        this.uuid=JUtilUUID.genUUID();
    }

    /**
     * 处理响应结果
     * @param dataPackage
     */
    private void onRespond(DataPackage dataPackage){
        if(Nvwa.isDebug()){
            log.log("end receiving response of -> "+this.uuid+"-"+dataPackage.getId(), -1);
        }
        Waitings.setResult(this.uuid+"-"+dataPackage.getId(), dataPackage);
    }

    @Override
    public void onReceive(DataPackage dataPackage, DataSegment segment, boolean isFirstSegment) throws Exception{
        //处理请求行、信息段开始/结束标记
        //第一个数据片段（新收到的响应）
        if(isFirstSegment){
            String statusLine=new String(segment.getData(), "UTF-8");
            if(Nvwa.isDebug()){
                log.log("begin receiving response("+dataPackage.getId()+") => "+statusLine, -1);
            }

            if(JUtilString.isBlank(statusLine)){
                //中断
                dataPackage.interrupt();

                //处理响应结果
                onRespond(dataPackage);
                return;
            }

            dataPackage.setStatusLine(statusLine);
            String[] statusLineCells=statusLine.split(" ");
            if(!JUtilMath.isInt(statusLineCells[0])){
                //中断
                dataPackage.interrupt();

                //处理响应结果
                onRespond(dataPackage);
                return;
            }

            dataPackage.setStatusCode(Integer.parseInt(statusLineCells[0]));
            if(statusLineCells.length>1) dataPackage.setResponseCode(statusLineCells[1]);

            //如传输已经完成，响应
            if(dataPackage.isCompleted()){
                packages.remove(dataPackage.getId());
                onRespond(dataPackage);
            }
            return;
        }

        //头部信息开始
        boolean isHeadersStartTag=JUtilBytes.equals(segment.getData(), Protocol.J_HEADERS_START);
        if(isHeadersStartTag) dataPackage.setHeadersStart(true);

        //参数信息开始
        boolean isParamsStartTag=JUtilBytes.equals(segment.getData(), Protocol.J_PARAMS_START);
        if(isParamsStartTag) dataPackage.setParamsStart(true);

        //某个实体信息开始
        boolean isEntityStartTag=JUtilBytes.equals(segment.getData(), Protocol.J_ENTITY_START);

        //传输结束
        boolean transferEnd=dataPackage.isCompleted() || JUtilBytes.equals(segment.getData(), Protocol.J_TRANSFER_END);

        //头开始
        if(isHeadersStartTag) dataPackage.clearSegments();

        //头结束（或参数开始、实体开始、传输结束）
        if(dataPackage.isHeadersStart() && (isParamsStartTag || isEntityStartTag || transferEnd)){
            dataPackage.setHeadersStart(false);
            dataPackage.setHeadersEnd(true);

            //解析头部
            String[] sHeaders=new String(dataPackage.getData(), "UTF-8").split(Protocol.CRLF);
            for(int i=0; i<sHeaders.length; i++){
                String h=sHeaders[i];
                if(h.indexOf(": ")<0) continue;

                String key=h.substring(0, h.indexOf(": "));
                String val=h.substring(h.indexOf(": ")+2);
                dataPackage.addHeader(key, val);
            }
            //解析头部 end

            //清空数据
            dataPackage.clearSegments();
        }

        //参数开始
        if(isParamsStartTag) dataPackage.clearSegments();

        //参数结束（或实体开始、传输结束）
        if(dataPackage.isParamsStart() && (isEntityStartTag || transferEnd)){
            dataPackage.setParamsStart(false);
            dataPackage.setParamsEnd(true);

            //解析参数
            String[] sParams = new String(dataPackage.getData(), "UTF-8").split("&");
            for (int i = 0; i < sParams.length; i++) {
                String p = sParams[i];
                if (p.indexOf("=") < 0) continue;

                String key = p.substring(0, p.indexOf("="));
                String val = p.substring(p.indexOf("=") + 1);
                dataPackage.addParam(key, val);
            }
            //解析参数 end

            //清空数据
            dataPackage.clearSegments();
        }

        //实体开始，或传输结束
        if(isEntityStartTag || transferEnd){
            if(dataPackage.isEntityStart()) {//上一个实体尚未处理完毕（第二个实体开始即第一个实体结束）
                //log.log("ENTITY START2....", -1);
                DataSource entity = dataPackage.getLastEntity();
                if(entity != null) {
                    //保存数据（首个片段为实体信息，不保存）
                    if (entity instanceof DataSourceFile) {//文件类型实体
                        ((DataSourceFile) entity).save();//文件类型，关闭输入输出流即可（前面已经逐个segment写入）
                    } else if (entity instanceof DataSourceString) {//字符串类型实体
                        //首个数据片段为实体信息
                        ((DataSourceString) entity).setSource(dataPackage.getData(1));
                    } else if (entity instanceof DataSourceObject) {//对象类型实体
                        ((DataSourceObject) entity).setSource(dataPackage.getData(1));
                    } else if (entity instanceof DataSourceBytes) {//自己数组类型实体
                        ((DataSourceBytes) entity).setSource(dataPackage.getData(1));
                    }
                }
            }

            //开始一个新实体
            if(isEntityStartTag){
                dataPackage.setEntityStart(true);
                dataPackage.setEntityEnd(false);
                dataPackage.clearSegments();
            }
        }

        //各种标记不保存到数据片段
        if(isHeadersStartTag || isParamsStartTag || isEntityStartTag || transferEnd){
            if(transferEnd) {
                dataPackage.clearSegments();
                packages.remove(dataPackage.getId());
                onRespond(dataPackage);
            }
            return;
        }

        if(dataPackage.isEntityStart()){//如果是一个实体开始了
            if(dataPackage.getSegments().isEmpty()){//第一个片段为实体信息
                //保存数据片段到列表
                dataPackage.saveSegment(segment);

                String[] sEntityInfo=new String(segment.getData(), "UTF-8").split(";");

                Map<String, String> entityInfo=new HashMap<>();
                for(int i=0; i<sEntityInfo.length; i++){
                    String p=sEntityInfo[i];
                    if(p.indexOf("=") < 0) continue;

                    String key=p.substring(0, p.indexOf("="));
                    String val=p.substring(p.indexOf("=")+1);

                    if(val.startsWith("\"") && val.endsWith("\"")) val=val.substring(1, val.length()-1);

                    //log.log(key+" = "+val, -1);
                    entityInfo.put(key, val);
                }

                String name=entityInfo.get("name");
                if(JUtilString.isBlank(name)){//信息不合规（必须设置实体名字，且各个实体名字不能相同，否则后面的实体会覆盖前面的）
                    //中断
                    dataPackage.interrupt();
                    onRespond(dataPackage);
                    return;
                }

                String filename=entityInfo.get("filename");
                String contentType=entityInfo.get("Content-Type");
                if(!JUtilString.isBlank(filename) && Protocol.CONTENT_TYPE_FILE.equalsIgnoreCase(contentType)){//文件
                    //临时文件路径
                    String tempFileDir=JUtilString.appendPath(JProperties.getWebRoot(), JDFS.getDir("/WEB-INF/JDFS/temporary/", new Timestamp(SysUtil.getNow())));

                    DataSourceFile entity=new DataSourceFile(this.getDataSourceBlockSize(), "UTF-8");
                    entity.setNameOriginal(filename);
                    entity.setSource(new File(JUtilString.appendPath(tempFileDir, JUtilUUID.genUUID()+JDFS.getFileExt(filename))));
                    dataPackage.addEntity(name, entity);
                }else if(Protocol.CONTENT_TYPE_OBJECT.equalsIgnoreCase(contentType)){//对象
                    DataSourceObject entity=new DataSourceObject(this.getDataSourceBlockSize(), "UTF-8");
                    entity.setClassName(filename);
                    dataPackage.addEntity(name, entity);
                }else{//字符串
                    DataSourceString entity=new DataSourceString(this.getDataSourceBlockSize(), "UTF-8");
                    dataPackage.addEntity(name, entity);
                }
            }else{
                DataSource entity=dataPackage.getLastEntity();//最近一个处理的实体
                if(entity instanceof DataSourceFile){//如果是文件
                    //保存到文件
                    ((DataSourceFile)entity).save(segment.getData());
                }else{
                    //保存数据片段
                    dataPackage.saveSegment(segment);
                }
            }
        }else{
            //log.log("save SEG.......", -1);
            dataPackage.saveSegment(segment);
        }

        //如果数据已经接收完毕
        if(transferEnd){
            dataPackage.clearSegments();
            packages.remove(dataPackage.getId());
            onRespond(dataPackage);
        }
    }

    /**
    /**
     * 发送请求
     * @param uri
     * @param headers
     * @param params
     * @param entities
     * @param timeout 等待响应超时时间（单位：ms）
     * @return
     * @throws Exception
     */
    public DataPackage request(String uri, Map<String, String> headers, Map<String, String> params, Map<String, DataSource> entities, long timeout) throws Exception{
        timeout=(timeout>0?timeout:this.getSoTimeout());

        //请求ID
        long thisRequestId=getRequestId();

        //处理uri中可能包含的参数
        if(uri.indexOf("?")>0){
            if(params == null) params=new HashMap<>();

            String[] queries=uri.substring(uri.indexOf("?")+1).split("&");
            for(int i=0; i<queries.length; i++){
                String q=queries[i].trim();
                if(q.indexOf("=")<0) continue;

                String key=q.substring(0, q.indexOf("="));
                String val=q.substring(q.indexOf("=")+1);

                params.put(key, val);
            }

            uri=uri.substring(0, uri.indexOf("?"));
        }
        //处理uri中可能包含的参数 end

        //请求行
        String requestLine="POST "+uri;
        //if(JUtilString.bytes(requestLine)>this.getDataSourceBlockSize()){
        //    throw new Exception("the request line is too long(more than "+this.getDataSourceBlockSize()+" bytes)");
        //}

        long t1=0L;
        long t2=0L;
        DataPackage result = null;
        synchronized(this) {
            t1 = System.currentTimeMillis();
            if(Nvwa.isDebug()){
                log.log("begin sending request("+thisRequestId+") => "+requestLine, -1);
            }

            List<DataSegment> segments = new ArrayList<>();
            segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, requestLine.getBytes(StandardCharsets.UTF_8)));

            //头部信息
            if (headers != null && !headers.isEmpty()) {
                StringBuffer sHeaders = new StringBuffer();

                //发送头部开始标志
                segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, Protocol.J_HEADERS_START));

                for (Iterator<String> keys = headers.keySet().iterator(); keys.hasNext(); ) {
                    String key = keys.next();
                    String val = headers.get(key);

                    sHeaders.append(key);
                    sHeaders.append(": ");
                    sHeaders.append(val);
                    sHeaders.append(Protocol.CRLF);
                }

                //发送头部信息
                segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, sHeaders.toString().getBytes(StandardCharsets.UTF_8)));
                sHeaders = null;
            }
            //头部信息 end

            //参数拼接
            if(params != null && !params.isEmpty()) {
                StringBuffer sParams = new StringBuffer();

                //发送参数开始标志
                this.sendAsSegment(Protocol.J_PARAMS_START, thisRequestId);

                for (Iterator<String> keys = params.keySet().iterator(); keys.hasNext(); ) {
                    String key = keys.next();
                    String val = params.get(key);

                    if (sParams.length() > 0) sParams.append("&");
                    sParams.append(key);
                    sParams.append("=");
                    sParams.append(JUtilString.encodeURI(val, "UTF-8"));
                }

                //发送参数
                //this.send(sParams.toString(), thisRequestId);
                segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, sParams.toString().getBytes(StandardCharsets.UTF_8)));
                sParams = null;
            }

            sendSegments(segments);
            segments.clear();

            //发送实体
            if(entities != null && !entities.isEmpty()) {
                for (Iterator<String> keys = entities.keySet().iterator(); keys.hasNext(); ) {
                    String key = keys.next();
                    DataSource dataSource = entities.get(key);

                    //发送实体开始标记
                    //this.sendAsSegment(Protocol.J_ENTITY_START, thisRequestId);
                    segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, Protocol.J_ENTITY_START));

                    //实体信息
                    String entityInfo = "name=\"" + key + "\";";
                    if (dataSource instanceof DataSourceFile) entityInfo += "filename=\"" + JUtilString.encodeURI(((DataSourceFile) dataSource).getNameOriginal(), "UTF-8") + "\";";
                    else entityInfo += "filename=\"" + JUtilString.encodeURI(dataSource.getName(), "UTF-8") + "\";";
                    entityInfo += "Content-Type=\"" + dataSource.getContentType() + "\";";
                    entityInfo += "Content-Length=\"" + dataSource.getContentLength() + "\";";

                    //发送实体信息
                    if(Nvwa.isDebug()) {
                        log.log(thisRequestId + " send entity info = " + entityInfo, -1);
                    }
                    segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, entityInfo.getBytes(StandardCharsets.UTF_8)));

                    sendSegments(segments);
                    segments.clear();

                    //实体内容
                    if(Nvwa.isDebug()) {
                        if(dataSource instanceof DataSourceObject){
                            DataSourceObject _dataSource = (DataSourceObject)dataSource;
                            Object _object = JSerialization.deSerialize(null, _dataSource.getSource(), true);
                            if(_object == null){
                                log.log(thisRequestId + " send entity objects = null", -1);
                            }else if(_object.getClass().isArray()){
                                Object[] _objects = (Object[])_object;
                                List<String> clsNames = new ArrayList<>();
                                for(Object o : _objects){
                                    if(o==null) clsNames.add("null");
                                    else clsNames.add(o.getClass().getCanonicalName());
                                }

                                String s = new String(_dataSource.getSource(), StandardCharsets.UTF_8);
                                if(s.indexOf("\f\f") > -1){
                                    log.log(thisRequestId + " send entity objects = "+ JArray.toString(clsNames, ",")+" => 包含两个连续分页符 => "+s.indexOf("\f\f"), -1);
                                }
                            }else{
                                log.log(thisRequestId + " send entity objects = "+_object.getClass().getCanonicalName(), -1);
                            }
                        }
                    }
                    DataPackage dataPackage = new DataPackage(thisRequestId,
                            dataSource,
                            this.getSegmentSize());

                    //发送数据
                    //log.log(thisRequestId+" send dataPackage = "+dataPackage.getDataSource().detail(), -1);
                    this.sendDataPackage(dataPackage);
                }
            }

            //必须在发送结束标记前，触发等待结果操作，否则如果响应过快，有可能在这之前就已经返回结果（从而导致无法获取到结果）
            Waitings.waiting(this.uuid + "-" + thisRequestId, timeout, null);

            //发送结束标记
            this.sendAsSegment(Protocol.J_TRANSFER_END, thisRequestId);

            t2 = System.currentTimeMillis();
            if(Nvwa.isDebug()){
                log.log("end sending request("+thisRequestId+")，耗时 => "+(t2 - t1)+"ms", -1);
            }
        }

        result = (DataPackage) Waitings.getResult(this.uuid + "-" + thisRequestId);
        long t3= System.currentTimeMillis();
        if(Nvwa.isDebug()){
            log.log("get response("+thisRequestId+")，等待结果耗时 => "+(t3-t2)+"ms => "+(result==null?"timeout!":"got!"), -1);
        }

        return result;
    }

    /**
     *
     * @param content
     * @param thisRequestId
     * @throws Exception
     */
    protected void send(String content, long thisRequestId) throws Exception{
        DataPackage dataPackage=new DataPackage(thisRequestId,
                new DataSourceString(this.getDataSourceBlockSize(), "UTF-8").setSource(content),
                this.getSegmentSize());

        this.sendDataPackage(dataPackage);
    }

    /**
     *
     * @param content
     * @param thisRequestId
     * @throws Exception
     */
    protected void sendAsSegment(byte[] content, long thisRequestId) throws Exception{
        DataSegment segment=new DataSegment(this.getSegmentSize(), thisRequestId, 0, content);
        this.sendSegment(segment);
    }

    /**
     *
     * @param content
     * @param thisRequestId
     * @throws Exception
     */
    protected void sendAsSegment(String content, long thisRequestId) throws Exception{
        DataSegment segment=new DataSegment(this.getSegmentSize(), thisRequestId, 0, content.getBytes("UTF-8"));
        this.sendSegment(segment);
    }
}