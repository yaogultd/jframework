package j.core.nio;

import j.core.annotation.description.ClassDescription;

import java.util.HashMap;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/09/28",
        description = "SOCKET参数")
public class SocketOptions {
    //最大空闲时间，超过此时间未收到客户端消息将强制关闭连接，单位毫秒
    public static final Integer SO_MAX_IDLE=9901;

    //建立连接后多久内必须发生交互，否则关闭连接，单位毫秒
    public static final Integer SO_MUST_SEND_AFTER_CONN_WITH_IN=9902;

    //服务器响应线程池大小
    public static final Integer SERVER_RESPOND_POOL_SIZE=9903;

    //服务器响应线程池线程执行间隔（微秒）
    public static final Integer SERVER_RESPOND_POOL_EXECUTE_INTERVAL=9904;

    //服务器Selector分组数
    public static final Integer SERVER_SELECTORS=9905;

    //服务器Selector轮询间隔（微秒）
    public static final Integer SERVER_SELECTOR_EXECUTE_INTERVAL=9906;

    //服务器处理线程池大小
    public static final Integer SERVER_HANDLE_POOL_SIZE=9907;

    //客户端Selector轮询间隔（微秒）
    public static final Integer CLIENT_SELECTOR_EXECUTE_INTERVAL=9908;

    //最大数据包大小
    public static final Integer MAX_DATA_PACKAGE_SIZE=9909;

    //默认设置
    private static Map<Integer, Object> defaults=new HashMap<>();

    static{
        defaults.put(java.net.SocketOptions.TCP_NODELAY, true);
        defaults.put(java.net.SocketOptions.SO_REUSEADDR, true);
        defaults.put(java.net.SocketOptions.SO_LINGER, 0);//秒
        defaults.put(java.net.SocketOptions.SO_TIMEOUT, 30*1000);//毫秒
        defaults.put(java.net.SocketOptions.SO_SNDBUF, 256);//在默认情况下，输出流的发送缓冲区是256个字节
        defaults.put(java.net.SocketOptions.SO_RCVBUF, 256);//在默认情况下，输入流的接收缓冲区是256个字节
        defaults.put(java.net.SocketOptions.SO_KEEPALIVE, false);
        defaults.put(java.net.SocketOptions.SO_OOBINLINE, false);
        defaults.put(SO_MAX_IDLE, 60000L);
        defaults.put(SO_MUST_SEND_AFTER_CONN_WITH_IN, 60000L);
        defaults.put(SERVER_RESPOND_POOL_SIZE, 1);
        defaults.put(SERVER_RESPOND_POOL_EXECUTE_INTERVAL, 1000);
        defaults.put(SERVER_SELECTORS, 1);
        defaults.put(SERVER_SELECTOR_EXECUTE_INTERVAL, 1000);
        defaults.put(SERVER_HANDLE_POOL_SIZE, 1);
        defaults.put(CLIENT_SELECTOR_EXECUTE_INTERVAL, 1000);
        defaults.put(MAX_DATA_PACKAGE_SIZE, 1024*1024*10);
    }

    /**
     *
     * @return
     */
    public static Map<Integer, Object> cloneDefaults(){
        Map<Integer, Object> options=new HashMap<>();
        options.putAll(defaults);
        return options;
    }
}
