package j.core.nio.http;

import j.core.annotation.description.ClassDescription;
import j.core.common.JProperties;
import j.core.fs.JDFS;
import j.core.hp.thread.ThreadManager;
import j.core.hp.thread.ThreadPool;
import j.core.hp.thread.ThreadRunner;
import j.core.nio.*;
import j.core.nvwa.Nvwa;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.JUtilBytes;
import j.util.JUtilString;
import j.util.JUtilUUID;
import org.nustaq.serialization.FSTConfiguration;

import java.io.File;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

@ClassDescription(author = "肖炯",
        date = "2021/10/01",
        description = "基于NIO实现的类Http Server")
public class HttpServer extends ServerHandler {
    private static Logger log=Logger.create(HttpServer.class);

    public HttpServer(){}

    /**
     *
     * @param socketChannel
     * @param socket
     * @param socketOptions
     * @param args
     */
    public HttpServer(SocketChannel socketChannel, Socket socket, Map<Integer, Object> socketOptions, Object[] args) {
        super(socketChannel, socket, socketOptions, args);
    }

    /**
     *
     * @param statusCode
     * @param responseCode
     * @param dataPackage
     * @throws Exception
     */
    private void respond(int statusCode, String responseCode, DataPackage dataPackage) throws Exception{
        //doRespond(statusCode, responseCode, dataPackage);
        ThreadPool pool = ThreadManager.getPool("j.core.nio.http.HttpServer.doRespond",
                (Integer)this.socketOptions.get(SocketOptions.SERVER_RESPOND_POOL_SIZE),
                (Integer)this.socketOptions.get(SocketOptions.SERVER_RESPOND_POOL_EXECUTE_INTERVAL),
                TimeUnit.MICROSECONDS,
                3600000L,
                ThreadPool.SELECT_TYPE_ROTATION);
        ThreadRunner runner=pool.addTask(new HttpServerResponder(new Object[]{statusCode, responseCode, dataPackage, this}, 0));
        if(Nvwa.isDebug()){
            log.log("server("+this.uuid+") add task to pool("+dataPackage.getId()+") => "+dataPackage.getRequestLine()+" => runner => "+runner.getId()+" => "+runner.getTasksCount(), -1);
        }
    }

    /**
     * 响应
     * @param statusCode
     * @param responseCode
     * @param dataPackage
     * @throws Exception
     */
     public void doRespond(int statusCode, String responseCode, DataPackage dataPackage) throws Exception{
        //响应实体
        Map<String, DataSource> entities=new HashMap<>();

        //解析请求行
        String requestLine=dataPackage.getRequestLine();
        if(requestLine.startsWith("POST ")){
            String uri=requestLine.substring(5);
            File file=new File(JUtilString.appendPath(JProperties.getWebRoot(), uri));
            if(file.exists()){//请求一个存在的文件
                DataSourceFile entity=new DataSourceFile(this.getDataSourceBlockSize(), "UTF-8");
                entity.setSource(file);
                entities.put(uri, entity);
            }else{
                statusCode=404;
            }
        }

         //发送响应
         this.doRespond(dataPackage.getId(),
                 statusCode,
                 responseCode,
                 null,
                 null,
                 entities);

         dataPackage.clear();
    }

    /**
     *
     * @param thisRequestId
     * @param statusCode
     * @param responseCode
     * @param headers
     * @param params
     * @param entities
     * @throws Exception
     */
    protected void doRespond(long thisRequestId,
                           int statusCode,
                           String responseCode,
                           Map<String, String> headers,
                           Map<String, String> params,
                           Map<String, DataSource> entities) throws Exception{
        if(Nvwa.isDebug()){
            log.log("server("+this.uuid+") begin response("+thisRequestId+")", -1);
        }

        //发送状态行
        String statusLine=statusCode+" "+responseCode;
        if(JUtilString.bytes(statusLine)>this.getDataSourceBlockSize()){
            throw new Exception("the status line is too long(more than "+this.getDataSourceBlockSize()+" bytes)");
        }
        //this.sendAsSegment(statusLine, thisRequestId);

        List<DataSegment> segments = new ArrayList<>();
        segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, statusLine.getBytes(StandardCharsets.UTF_8)));

        //头部信息
        if(headers!=null && !headers.isEmpty()) {
            StringBuffer sHeaders=new StringBuffer();

            //发送头部开始标志
            //this.sendAsSegment(Protocol.J_HEADERS_START, thisRequestId);
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
            //this.send(sHeaders.toString(), thisRequestId);
            segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, sHeaders.toString().getBytes(StandardCharsets.UTF_8)));
            sHeaders=null;

            //发送头部结束标志
            //this.sendAsSegment(Protocol.J_HEADERS_END, thisRequestId);
        }
        //头部信息 end

        //参数拼接
        if(params != null && !params.isEmpty()){
            StringBuffer sParams=new StringBuffer();

            //发送参数开始标志
            //this.sendAsSegment(Protocol.J_PARAMS_START, thisRequestId);
            segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, Protocol.J_PARAMS_START));


            for(Iterator<String> keys=params.keySet().iterator(); keys.hasNext();){
                String key=keys.next();
                String val=params.get(key);

                if(sParams.length() >0 ) sParams.append("&");
                sParams.append(key);
                sParams.append("=");
                sParams.append(JUtilString.encodeURI(val, "UTF-8"));
            }

            //发送参数
            //this.send(sParams.toString(), thisRequestId);
            segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, sParams.toString().getBytes(StandardCharsets.UTF_8)));
            sParams=null;

            //发送参数结束标志
            //this.sendAsSegment(Protocol.J_PARAMS_END, thisRequestId);
        }

        sendSegments(segments);
        segments.clear();

        //发送实体
        if(entities != null && !entities.isEmpty()){
            for(Iterator<String> keys=entities.keySet().iterator(); keys.hasNext();){
                String key=keys.next();
                DataSource dataSource=entities.get(key);

                //发送实体开始标记
                //this.sendAsSegment(Protocol.J_ENTITY_START, thisRequestId);
                segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, Protocol.J_ENTITY_START));

                //实体信息
                String entityInfo="name=\""+key+"\";";
                entityInfo+="filename=\""+JUtilString.encodeURI(dataSource.getName(), "UTF-8")+"\";";
                entityInfo+="Content-Type=\""+dataSource.getContentType()+"\";";

                if(JUtilString.bytes(entityInfo)>this.getDataSourceBlockSize()){
                    throw new Exception("this datasource name(may be the file name, include Content-Type and name) is too long(more than "+this.getDataSourceBlockSize()+" bytes)");
                }

                //发送实体信息
                //this.sendAsSegment(entityInfo, thisRequestId);
                segments.add(new DataSegment(this.getSegmentSize(), thisRequestId, 0, entityInfo.getBytes(StandardCharsets.UTF_8)));
                //实体信息 end

                sendSegments(segments);
                segments.clear();

                //实体内容
                DataPackage dataPackage=new DataPackage(thisRequestId,
                        dataSource,
                        this.getSegmentSize());

                //发送数据
                this.sendDataPackage(dataPackage);
                //实体内容 end

                //发送实体结束标记
                //this.sendAsSegment(Protocol.J_ENTITY_END, thisRequestId);
            }
        }

        //发送结束标记
        this.sendAsSegment(Protocol.J_TRANSFER_END, thisRequestId);
        if(Nvwa.isDebug()){
            log.log("server("+this.uuid+") end response("+thisRequestId+")", -1);
        }
    }

    /**
     * 接收到的DataPackage会在服务端业务逻辑处理完毕后清除，其中接收到的文件会删除，如业务需要持久化数据或文件，需在业务逻辑结束前进行处理。
     * @param dataPackage
     * @param segment
     * @param isFirstSegment
     * @throws Exception
     */
    @Override
    public void onReceive(DataPackage dataPackage, DataSegment segment, boolean isFirstSegment) throws Exception{
        String requestLine=null;
        //处理请求行、信息段开始/结束标记
        //第一个数据片段（新收到的请求）
        if(isFirstSegment){
            requestLine=new String(segment.getData(), "UTF-8");
            if(!requestLine.startsWith("POST ")){//目前只支持POST
                //中断
                dataPackage.interrupt();

                //响应
                respond(400, "", dataPackage);
                return;
            }
            if(Nvwa.isDebug()){
                log.log("server("+this.uuid+") begin receive request("+dataPackage.getId()+") => "+requestLine, -1);
            }
            dataPackage.setRequestLine(requestLine);

            //如传输已经完成，响应
            if(dataPackage.isCompleted()){
                packages.remove(dataPackage.getId());

                if(Nvwa.isDebug()){
                    log.log("server("+this.uuid+") end receive request("+dataPackage.getId()+")(no body)，接收数据耗时 => "+(System.currentTimeMillis() - dataPackage.getCreatedAt())+"ms", -1);
                }
                respond(200, "", dataPackage);
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

                if(Nvwa.isDebug()){
                    log.log("server("+this.uuid+") end receive request("+dataPackage.getId()+")，接收数据耗时 => "+(System.currentTimeMillis() - dataPackage.getCreatedAt())+"ms", -1);
                }
                respond(200, "", dataPackage);
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

                    //响应
                    respond(400, "", dataPackage);
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
