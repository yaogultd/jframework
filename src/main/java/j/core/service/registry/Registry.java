package j.core.service.registry;

import j.core.annotation.configuration.Properties;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.annotation.nvwa.Nvwa;
import j.core.nio.DataPackage;
import j.core.nio.SocketOptions;
import j.core.service.registry.channel.HttpChannel;
import j.core.service.registry.channel.NioChannel;
import j.core.service.server.config.Service;
import j.core.service.server.config.Services;
import j.log.Logger;
import j.util.*;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/08/31",
        description = "服务注册中心")
@Nvwa
@Properties(path = "service.registry.properties")
public class Registry{
    public static Logger log=Logger.create(Registry.class);//日志输出

    @FieldDescription(description = "服务约定标识：响应")
    public static final String FLAG_RESP="resp";

    @FieldDescription(description = "服务约定标识：命令")
    public static final String FLAG_COMMAND="command";

    @FieldDescription(description = "服务约定标识：结果")
    public static final String FLAG_RESULT="result";

    @FieldDescription(description = "服务约定标识：提示信息")
    public static final String FLAG_MESSAGE="message";

    @FieldDescription(description = "服务约定标识：服务路径")
    public static final String FLAG_PATH="path";

    @FieldDescription(description = "服务约定标识：多个服务配置信息")
    public static final String FLAG_SERVICES="services";

    @FieldDescription(description = "服务约定标识：单个服务配置信息")
    public static final String FLAG_SERVICE="service";

    @FieldDescription(description = "服务约定标识：服务节点")
    public static final String FLAG_SERVICE_REGISTRATION="registration";

    @FieldDescription(description = "服务约定标识：多个服务节点")
    public static final String FLAG_SERVICE_REGISTRATIONS="registrations";

    @FieldDescription(description = "服务约定标识：通信层类型")
    public static final String FLAG_NETWORKER="networker";

    @FieldDescription(description = "服务约定标识：主机/IP")
    public static final String FLAG_HOST="host";

    @FieldDescription(description = "服务约定标识：端口")
    public static final String FLAG_PORT="port";

    @FieldDescription(description = "服务约定标识：服务注册时间")
    public static final String FLAG_REG_AT="regAt";

    @FieldDescription(description = "服务约定标识：服务心跳时间")
    public static final String FLAG_HEARTBEAT_AT="heartbeatAt";

    @FieldDescription(description = "服务约定标识：服务是否暂停")
    public static final String FLAG_PAUSED="paused";

    @FieldDescription(description = "服务约定标识：处理中任务队列长度（任务数）")
    public static final String FLAG_PACKAGES ="packages";

    @FieldDescription(description = "服务约定标识：运行时UUID")
    public static final String FLAG_RUN_UUID ="UUID";

    @FieldDescription(description = "服务注册指令：注册")
    public static final String COMMAND_REG="REG";

    @FieldDescription(description = "服务注册指令：删除")
    public static final String COMMAND_DEL="DEL";

    @FieldDescription(description = "服务注册指令：心跳")
    public static final String COMMAND_HEARTBEAT="HEARTBEAT";

    @FieldDescription(description = "服务注册指令：暂停服务")
    public static final String COMMAND_PAUSE="PAUSE";

    @FieldDescription(description = "服务注册指令：恢复服务")
    public static final String COMMAND_RESUME="RESUME";

    @FieldDescription(description = "服务注册指令：获取服务信息")
    public static final String COMMAND_QUERY="QUERY";

    @FieldDescription(description = "响应结果：无效指令")
    public static final String RESP_INVALID_COMMAND="INVALID_COMMAND";

    @FieldDescription(description = "响应结果：无效accessKey")
    public static final String RESP_INVALID_ACCESS_KEY="INVALID_ACCESS_KEY";

    @FieldDescription(description = "响应结果：无效服务信息")
    public static final String RESP_INVALID_SERVICE="INVALID_SERVICE";

    @FieldDescription(description = "响应结果：无效签名")
    public static final String RESP_INVALID_SIGN="INVALID_SIGN";

    @FieldDescription(description = "响应结果：成功")
    public static final String RESP_OK="OK";
    
    @FieldDescription(description = "key为服务调用路径，value为该服务的节点列表")
    public static ConcurrentMap<String, ConcurrentList<Registration>> registrations=new ConcurrentMap<String, ConcurrentList<Registration>>();

    @FieldDescription(description = "注册中心节点")
    public static ConcurrentList<RegistryChannel> channels=new ConcurrentList<>();

    @FieldDescription(description = "tcp发送缓冲区大小")
    public static int tcpSendBufferSize=256;

    @FieldDescription(description = "tcp接收缓冲区大小")
    public static int tcpReceiveBufferSize=256;

    /**
     *
     */
    public Registry(){
        super();
    }

    /**
     * 启动注册中心
     * @throws Exception
     */
    public static void start() throws Exception{
        //tcp缓冲区大小
        String tcpBufferSize = j.core.nvwa.Nvwa.getParameter(Registry.class, "tcpBufferSize");
        if(!JUtilMath.isInt(tcpBufferSize)){
            log.log("tcpBufferSize of service registry is not set", -1);
            return;
        }

        tcpSendBufferSize=Integer.parseInt(tcpBufferSize);
        tcpReceiveBufferSize=Integer.parseInt(tcpBufferSize);

        //通信层
        String networker = j.core.nvwa.Nvwa.getParameter(Registry.class, "networker");

        //是否注册中心
        String isServer = j.core.nvwa.Nvwa.getParameter(Registry.class, "isServer");
        if("true".equals(isServer)){//如果是注册中心
            //监听端口
            String listOn = j.core.nvwa.Nvwa.getParameter(Registry.class, "listOn");
            if(!JUtilMath.isInt(listOn)) listOn="1990";

            //响应线程池大小
            String respondPoolSize = j.core.nvwa.Nvwa.getParameter(Registry.class,"respondPoolSize");
            if(!JUtilMath.isInt(respondPoolSize)) respondPoolSize="1";

            //响应线程池执行频率
            String respondPoolExecutingPeriod = j.core.nvwa.Nvwa.getParameter(Registry.class,"respondPoolExecutingPeriod");
            if(!JUtilMath.isInt(respondPoolExecutingPeriod)) respondPoolExecutingPeriod="10000";

            //服务端Selector分组数
            String serverSelectors = j.core.nvwa.Nvwa.getParameter(Registry.class,"serverSelectors");
            if(!JUtilMath.isInt(serverSelectors)) serverSelectors="1";

            String serverSelectorExecutingPeriod = j.core.nvwa.Nvwa.getParameter(Registry.class,"serverSelectorExecutingPeriod");
            if(!JUtilMath.isLong(serverSelectorExecutingPeriod)) serverSelectorExecutingPeriod="10000";

            //注册中心服务端处理线程池大小
            String serverHandlerPoolSize = j.core.nvwa.Nvwa.getParameter(Registry.class, "serverHandlerPoolSize");
            if(!JUtilMath.isInt(serverHandlerPoolSize)) serverHandlerPoolSize="10";

            //注册中心允许的最大Client数量
            String maxClients = j.core.nvwa.Nvwa.getParameter(Registry.class, "maxClients");
            if(!JUtilMath.isInt(maxClients)) maxClients="100";

            //注册中心允许的每个IP上最大Client数量
            String maxClientsPerIp = j.core.nvwa.Nvwa.getParameter(Registry.class, "maxClientsPerIp");
            if(!JUtilMath.isInt(maxClientsPerIp)) maxClientsPerIp="1";

            //是否调试
            String debug = j.core.nvwa.Nvwa.getParameter(Registry.class, "debug");

            log.log("starting service registry server on port "+listOn+"(tcpBufferSize:"+tcpSendBufferSize+"|"+tcpReceiveBufferSize+", maxClientsPerIp:"+maxClientsPerIp+")", -1);

            Map<Integer, Object> options=SocketOptions.cloneDefaults();
            options.put(java.net.SocketOptions.SO_SNDBUF, tcpSendBufferSize);
            options.put(java.net.SocketOptions.SO_RCVBUF, tcpReceiveBufferSize);
            options.put(SocketOptions.SERVER_RESPOND_POOL_SIZE, Integer.parseInt(respondPoolSize));
            options.put(SocketOptions.SERVER_SELECTOR_EXECUTE_INTERVAL, Integer.parseInt(respondPoolExecutingPeriod));
            options.put(SocketOptions.SERVER_SELECTORS, Integer.parseInt(serverSelectors));
            options.put(SocketOptions.SERVER_SELECTOR_EXECUTE_INTERVAL, Integer.parseInt(serverSelectorExecutingPeriod));
            options.put(SocketOptions.SERVER_HANDLE_POOL_SIZE, Integer.parseInt(serverHandlerPoolSize));

            if("nio".equalsIgnoreCase(networker)){
                new NioChannel(Integer.parseInt(listOn),
                        Integer.parseInt(maxClients),
                        Integer.parseInt(maxClientsPerIp),
                        options,
                        null,
                        "true".equalsIgnoreCase(debug));
            }else{
                new HttpChannel(Integer.parseInt(listOn),
                        Integer.parseInt(maxClients),
                        Integer.parseInt(maxClientsPerIp),
                        options,
                        null,
                        "true".equalsIgnoreCase(debug));
            }
        }

        //可用注册中心节点
        String[] nodes = j.core.nvwa.Nvwa.getParameter(Registry.class, "nodes").split(",");
        for(int i=0; i<nodes.length; i++){
            String[] node=nodes[i].split(":");
            if(node.length!=2 || JUtilString.isBlank(node[0]) || !JUtilMath.isInt(node[1])) continue;

            channels.add(new RegistryChannel(node[0], Integer.parseInt(node[1])));

            log.log("service registry channel -> "+channels.get(channels.size()-1).toString(), -1);
        }

        String isClient = j.core.nvwa.Nvwa.getParameter(Registry.class, "isClient");
        if("true".equals(isClient) && "nio".equalsIgnoreCase(networker)) {//如果是客户端
            for(int i=0; i<channels.size(); i++){
                //触发连接
                log.log("service registry channel 触发连接...... ", -1);
                channels.get(i).getEndpoint();
            }
        }

        try{
            Thread.sleep(2000);
        }catch(Exception e){}
    }

    /**
     *
     * @return
     */
    public static ConcurrentList<RegistryChannel> getChannels(){
        return channels;
    }

    /**
     *
     * @return
     */
    public static int getTcpSendBufferSize(){
        return tcpSendBufferSize;
    }

    /**
     *
     * @return
     */
    public static int getTcpReceiveBufferSize(){
        return tcpReceiveBufferSize;
    }

    /**
     * 服务是否已经注册
     * @param host
     * @param service
     * @return
     */
    public static Registration exists(String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService==null || ofService.isEmpty()) return null;

        for(int i=0; i<ofService.size(); i++){
            Registration registration=ofService.get(i);
            if(registration.getHost().equals(host)) return registration;
        }

        return null;
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "注册")
    public static String doReg(DataPackage dataPackage, JSONObject jsonServices){
        String networker=JUtilJSON.string(jsonServices, "networker");
        String ip=JUtilJSON.string(jsonServices, "ip");

        List<Service> services = Services.fromJson(JUtilJSON.array(jsonServices, FLAG_SERVICES));
        for(int i=0; services!=null && i<services.size(); i++){
            //注册
            reg(networker, JUtilString.isBlank(ip) ? dataPackage.getFrom() : ip, services.get(i));
        }
        return Registry.RESP_OK;
    }

    /**
     * 服务注册
     * @param networker
     * @param host
     * @param service
     */
    public static void reg(String networker, String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService == null) ofService=new ConcurrentList<>();

        Registration registration=exists(host, service);
        if(registration==null) {
            registration = new Registration(networker, host, service);
            ofService.add(registration);
            log.log("new service registration -> "+registration.toString(), -1);
        }
        registration.setRunUuid(service.getRunUuid());
        registration.heartbeat();

        registrations.put(service.getPath(), ofService);
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "删除")
    public static String doDel(DataPackage dataPackage, JSONObject jsonServices){
        List<Service> services = Services.fromJson(JUtilJSON.array(jsonServices, FLAG_SERVICES));
        for(int i=0; services!=null && i<services.size(); i++){
            //删除
            del(dataPackage.getFrom(), services.get(i));
        }
        return null;
    }

    /**
     * 服务注册
     * @param host
     * @param service
     */
    public static void del(String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService==null || ofService.isEmpty()) return;

        for(int i=0; i<ofService.size(); i++){
            Registration registration=ofService.get(i);
            if(registration.getHost().equals(host)) {
                ofService.remove(i);
                if(ofService.isEmpty()) registrations.remove(service.getPath());
                break;
            }
        }
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "心跳")
    public static String doHeartbeat(DataPackage dataPackage, JSONObject jsonServices){
        String networker=JUtilJSON.string(jsonServices, "networker");

        List<Service> services = Services.fromJson(JUtilJSON.array(jsonServices, FLAG_SERVICES));
        for(int i=0; services!=null && i<services.size(); i++){
            //心跳
            heartbeat(networker, dataPackage.getFrom(), services.get(i));
        }
        return Registry.RESP_OK;
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "心跳")
    public static void heartbeat(String networker, String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService == null) ofService=new ConcurrentList<>();

        Registration registration=exists(host, service);
        if(registration==null) {
            registration = new Registration(networker, host, service);
            ofService.add(registration);
        }
        registration.heartbeat();

        registrations.put(service.getPath(), ofService);
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "暂停")
    public static String doPause(DataPackage dataPackage, JSONObject jsonServices){
        List<Service> services = Services.fromJson(JUtilJSON.array(jsonServices, FLAG_SERVICES));
        for(int i=0; services!=null && i<services.size(); i++){
            //暂停
            pause(dataPackage.getFrom(), services.get(i));
        }
        return Registry.RESP_OK;
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "暂停")
    public static void pause(String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService == null) ofService=new ConcurrentList<>();

        Registration registration=exists(host, service);
        if(registration != null) registration.pause();
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "恢复")
    public static String doResume(DataPackage dataPackage, JSONObject jsonServices){
        List<Service> services = Services.fromJson(JUtilJSON.array(jsonServices, FLAG_SERVICES));
        for(int i=0; services!=null && i<services.size(); i++){
            //暂停
            pause(dataPackage.getFrom(), services.get(i));
        }
        return Registry.RESP_OK;
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "恢复")
    public static void resume(String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService == null) ofService=new ConcurrentList<>();

        Registration registration=exists(host, service);
        if(registration != null) registration.resume();
    }

    /**
     * 筛选有效的
     * @param regs
     * @return
     */
    public static ConcurrentList<Registration> valid(ConcurrentList<Registration> regs){
        if(regs==null||regs.isEmpty()) return regs;
        ConcurrentList<Registration> validRegs=new ConcurrentList<>();
        for(int i=0; i<regs.size(); i++){
            if(regs.get(i).available()) validRegs.add(regs.get(i));
        }
        return validRegs;
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "查询")
    public static String doQuery(String path){
        return toString(path);
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "所有服务信息，json格式")
    public static String toString(String path){
        StringBuffer s=new StringBuffer();
        s.append("{\""+Registry.FLAG_SERVICES+"\":[");

        int index=0;
        ConcurrentList<String> paths=registrations.listKeys();
        for(int i=0; i<paths.size(); i++){
            //指定了只查询某个服务
            if(!JUtilString.isBlank(path) && !path.equals(paths.get(i))) continue;

            ConcurrentList<Registration> ofService=registrations.get(paths.get(i));
            ofService=valid(ofService);
            if(ofService==null || ofService.isEmpty()) continue;

            if(index>0) s.append(",");
            s.append("{\""+Registry.FLAG_PATH+"\":\""+paths.get(i)+"\"");
            s.append(",\""+Registry.FLAG_SERVICE_REGISTRATIONS+"\":[");
            for(int j=0; j<ofService.size(); j++){
                if(j>0) s.append(",");
                s.append(ofService.get(j).toString());
            }
            s.append("]}");
            index++;
        }
        s.append("]}");
        return s.toString();
    }
}
