package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.util.ConcurrentMap;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/10/05",
        description = "一些约定的传输、数据格式规则")
public final class Protocol {
    //默认头部信息
    private static ConcurrentMap<String, String> headers=new ConcurrentMap<>();

    //换行符
    public final static String CRLF="\r\n";

    //Http protocol version
    public final static String HTTP_PROTOCOL_VERSION="HTTP/1.1";

    //Content-Types
    public final static String CONTENT_TYPE_TEXT="text/plain";
    public final static String CONTENT_TYPE_HTML="text/html";
    public final static String CONTENT_TYPE_JSON="application/json";
    public final static String CONTENT_TYPE_XML="application/xml";
    public final static String CONTENT_TYPE_MULTIPART="multipart/form-data";
    public final static String CONTENT_TYPE_ULRENCODED="application/x-www-form-urlencoded";
    public final static String CONTENT_TYPE_OBJECT="application/java-object";
    public final static String CONTENT_TYPE_FILE="application/octet-stream";

    //数据包开始标志
    public final static byte[] J_DATA_SEGMENT_START="\f\f".getBytes(StandardCharsets.UTF_8);

    //标识一次传输的结束
    public final static byte[] J_TRANSFER_END="$J_TRANS_END".getBytes(StandardCharsets.UTF_8);

    //标识Header信息块的开始
    public final static byte[] J_HEADERS_START ="$J_HEADERS_START".getBytes(StandardCharsets.UTF_8);

    //标识参数信息块的开始
    public final static byte[] J_PARAMS_START ="$J_PARAMS_START".getBytes(StandardCharsets.UTF_8);

    //标识实体开始
    public final static byte[] J_ENTITY_START ="$J_ENTITY_START".getBytes(StandardCharsets.UTF_8);

    //心跳信号
    public final static byte[] J_HEARTBEAT="$J_HEARTBEAT".getBytes(StandardCharsets.UTF_8);

    //请求的业务自定义对象
    public final static String J_OBJECTS ="J_OBJECTS";

    //请求的内容
    public final static String J_PAYLOAD="J_PAYLOAD";

    //封装响应的实体
    public final static String J_RESPONSE="J_RESPONSE";

    //数据包ID的标识
    public final static String J_DATAPACKGE_ID="J_DATAPACKGE_ID";

    static{
        headers.put("Content-Type", CONTENT_TYPE_ULRENCODED);
    }

    /**
     * 拷贝默认头信息
     * @return
     */
    public static Map<String, String> cloneDefaultHeaders(){
        Map<String, String> _headers=new HashMap<>();
        _headers.putAll(headers);
        return _headers;
    }
}
