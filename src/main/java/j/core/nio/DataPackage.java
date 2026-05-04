package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import j.core.common.JObject;
import j.core.common.JSerializable;
import j.core.fs.JDFSFile;
import j.core.sys.SysUtil;
import j.http.*;
import j.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpClient;

import java.io.File;
import java.io.InputStream;
import java.util.*;

@ClassDescription(author = "肖炯",
        date = "2021/09/02",
        description = "一个数据包，表示一次完整的传输，可以是请求、也可以是响应；" +
                "一个数据包由N个DataSegment组成，每个片段前8个字节为数据包ID（一对请求-响应使用同一ID），第二个8字节表示数据包总长度，接下来的4个字节表示该片段实际内容长度；" +
                "每个数据片段为固定长度，内容不足长度的以0补足")
@Getter
@Setter
public class DataPackage extends JSerializable {
    //数据包ID（数据包ID为0表示心跳或无效信息，业务数据的数据包ID必须设置为大于0）
    private long id;

    //数据包总长度
    private long total;

    //数据片段容量（包括头部信息、实际内容、补0）
    private int segmentSize;

    //数据源（将被发送的）
    private DataSource dataSource;

    //已经发送的/已接收到的数据
    private List<DataSegment> segments=new ArrayList<>();

    //最近一次发送/接收数据时间
    private long latestActive=0;

    //已经发送的/已接收到的数据长度
    private long finished=0;

    //是否已经传输完毕
    private boolean completed=false;

    //是否被中断
    private boolean interrupted=false;


    //请求行（如果是请求数据包）
    private String requestLine;

    //状态行（如果是应答数据包）
    private String statusLine;
    private int statusCode=0;
    private String responseCode;

    //解析出的请求头
    private Map<String, String> headers=new HashMap<>();

    //解析出的参数
    private Map<String, String> params=new HashMap<>();

    //解析出实体
    private Map<String, DataSource> entities=new HashMap<>();

    //最新接收到的实体
    private DataSource lastEntity;

    //头部信息块开始
    private boolean headersStart=false;

    //头部信息块结束
    private boolean headersEnd=false;

    //参数信息块开始
    private boolean paramsStart=false;

    //参数信息块结束
    private boolean paramsEnd=false;

    //某个实体开始
    private boolean entityStart=false;

    //某个实体结束
    private boolean entityEnd=false;

    //创建时间
    private long createdAt=0;

    //完成时间
    private long finishedAt=0;

    //来自IP
    private String from;

    //错误代码
    private String errorCode;

    //错误提示
    private String errorMessage;

    /**
     *
     * @param key
     * @param val
     */
    public void addHeader(String key, String val){
        this.headers.put(key, val);
    }

    /**
     *
     * @param _headers
     */
    public void addHeaders(Map<String, String> _headers){
        if(_headers==null || _headers.isEmpty()) return;
        this.headers.putAll(_headers);
    }

    /**
     *
     * @param key
     * @return
     */
    public String getHeader(String key){
        return this.headers==null?null:this.headers.get(key);
    }

    /**
     *
     * @param key
     * @param val
     */
    public void addParam(String key, String val){
        this.params.put(key, val);
    }

    /**
     *
     * @param _params
     */
    public void addParams(Map<String, String> _params){
        if(_params==null || _params.isEmpty()) return;
        this.params.putAll(_params);
    }

    /**
     *
     * @param key
     * @return
     */
    public String getParam(String key){
        return this.params==null?null:this.params.get(key);
    }

    /**
     *
     * @param key
     * @param val
     */
    public void addEntity(String key, DataSource val){
        if(this.entities==null) this.entities=new HashMap<>();
        this.entities.put(key, val);
        this.setLastEntity(val);
    }

    /**
     *
     * @param _entities
     */
    public void addEntities(Map<String, DataSource> _entities){
        if(_entities==null || _entities.isEmpty()) return;
        if(this.entities==null) this.entities=new HashMap<>();
        this.entities.putAll(_entities);
    }

    /**
     *
     * @param key
     * @return
     */
    public DataSource getEntity(String key){
        return this.entities==null?null:this.entities.get(key);
    }

    /**
     *
     * @param type
     * @return
     */
    public Map<String, DataSource> getEntities(String type){
        Map<String, DataSource> ofType=new LinkedHashMap<>();
        if(this.entities==null) return ofType;

        Iterator<String> keys=this.entities.keySet().iterator();
        while (keys.hasNext()){
            String key=keys.next();
            DataSource entity=this.entities.get(key);

            if(entity.getClass().getName().equals(type)) ofType.put(key, entity);
        }
        return ofType;
    }

    /**
     *
     * @param id
     * @param total
     * @param segmentSize
     */
    public DataPackage(long id, long total, int segmentSize){
        this.createdAt=SysUtil.getNow();
        this.latestActive= SysUtil.getNow();
        this.id=id;
        this.total=total;
        this.segmentSize=segmentSize;
    }

    /**
     *
     * @param id
     * @param dataSource
     * @param segmentSize
     */
    public DataPackage(long id, DataSource dataSource, int segmentSize){
        this.createdAt=SysUtil.getNow();
        this.id=id;

        this.dataSource=dataSource;
        this.total=dataSource==null?0:dataSource.getContentLength();
        this.segmentSize=segmentSize;
    }

    /**
     * 收到数据片段时
     * @param segment
     * @return
     */
    public boolean onSegment(DataSegment segment){
        this.latestActive= SysUtil.getNow();
        this.finished+=segment.getData().length;
        if(JUtilBytes.equals(segment.getData(), Protocol.J_TRANSFER_END)
                || (this.total>0 && this.finished>=this.total)){//传输结束标志，或指定了总长度且已接收数据达到总长度
            this.completed=true;
            this.finishedAt=SysUtil.getNow();
        }
        return this.completed;
    }

    /**
     *
     * @param segment
     * @return
     */
    public void saveSegment(DataSegment segment){
        segments.add(segment);
    }

    /**
     *
     * @return
     */
    public void interrupt(){
        this.interrupted=true;
    }

    /**
     *
     */
    public void clearSegments(){
        if(segments!=null) segments.clear();
    }

    /**
     *
     */
    public void clear(){
        this.clearSegments();
        if(this.entities==null) return;
        Iterator<String> keys=this.entities.keySet().iterator();
        while(keys.hasNext()){
            String key=keys.next();
            DataSource entity=this.entities.get(key);
            entity.clear();
            keys.remove();
        }
    }

    /**
     * 是否超时
     * @return
     */
    public boolean isTimeout(long timeout){
        return SysUtil.getNow() - this.latestActive >= timeout;
    }

    /**
     * 组装全部数据
     * @return
     */
    public byte[] getData(){
        return this.getData(0);
    }

    /**
     * 组装数据，从offset指定的片段位置开始
     * @param offset（包括）
     * @return
     */
    public byte[] getData(int offset){
        if(segments.isEmpty() || offset>=segments.size()) return new byte[0];

        byte[] all=segments.get(offset).getData();
        for(int i=offset+1; i<segments.size(); i++){
            all=JArray.append(all, segments.get(i).getData());
        }

        return all;
    }

    /**
     *
     * @return 如果数据已经全部发送完毕，返回null
     * @throws Exception
     */
    public byte[] send() throws Exception{
        if(this.dataSource==null || this.finished>=this.total) return null;

        byte[] block=this.dataSource.fetchBlock();
        if(block == null) return null;

        DataSegment segment=new DataSegment(this.segmentSize, this.id, this.total, block);
        this.finished+=block.length;
        return segment.assemble();
    }

    /**
     * 通过http发送（不能包含无法序列化的数据源）
     * @param http
     * @param client
     * @param context
     * @param url
     * @return
     * @throws Exception
     */
    public JHttpContext sendViaHttp(JHttp http, HttpClient client, JHttpContext context, String url) throws Exception{
        if(http == null) http = JHttp.getInstance();
        if(context == null) context = new JHttpContext();

        return http.postBytes(context, client, url, JObject.serialize(this), "UTF-8");
    }

    /**
     * 通过http响应发送（不能包含无法序列化的数据源）
     * @param response
     * @throws Exception
     */
    public void sendToHttpResponse(HttpServletResponse response) throws Exception{
        response.getOutputStream().write(JObject.serialize(this));
    }

    /**
     * 从http接收(stream)
     * @param request
     * @return
     * @throws Exception
     */
    public static DataPackage receiveFromHttpStream(HttpServletRequest request) throws Exception{
        return (DataPackage)JObject.deSerialize(JUtilInputStream.bytes(request.getInputStream()));
    }

    /**
     * 从stream接收
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static DataPackage receiveStream(InputStream inputStream) throws Exception{
        return (DataPackage)JObject.deSerialize(JUtilInputStream.bytes(inputStream));
    }
}
