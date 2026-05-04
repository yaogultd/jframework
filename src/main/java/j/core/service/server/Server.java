package j.core.service.server;

import j.core.Startup;
import j.core.annotation.configuration.Properties;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.nvwa.Nvwa;
import j.core.lang.Methods;
import j.core.nio.*;
import j.core.nio.http.HttpClient;
import j.core.permission.Signature;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.core.service.client.Client;
import j.core.service.registry.Registry;
import j.core.service.registry.RegistryChannel;
import j.core.service.server.channel.Channel;
import j.core.service.server.channel.HttpChannel;
import j.core.service.server.channel.NioChannel;
import j.core.service.server.config.Service;
import j.core.service.server.config.Services;
import j.core.web.Constants;
import j.http.JHttpContext;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilMath;
import j.util.JUtilString;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ClassDescription(author = "肖炯",
        date = "2021/08/30",
        description = "")
@Nvwa
@Properties(path = "service.service.properties")
public class Server implements Runnable {
    private static Logger log=Logger.create(Server.class);//日志输出

    @FieldDescription(description = "tcp发送缓冲区大小")
    private static int tcpSendBufferSize=256;

    @FieldDescription(description = "tcp接收缓冲区大小")
    private static int tcpReceiveBufferSize=256;

    @FieldDescription(description = "需要启动的服务")
    @Getter
    private static String[] includes=null;

    @FieldDescription(description = "不需要启动的服务")
    @Getter
    private static String[] excludes=null;

    @FieldDescription(description = "所有服务对象使用单例")
    private static ConcurrentHashMap<String, ServiceBase> services=new ConcurrentHashMap<>();

    @FieldDescription(description = "各端口上监听的服务")
    private static ConcurrentMap<Integer, Channel> channels=new ConcurrentMap<>();

    private Map<Integer, Object> socketOptions;

    /**
     * 启动
     * @throws Exception
     */
    public static void start() throws Exception {
        String _includes = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "includes");
        if(!JUtilString.isBlank(_includes)) includes=_includes.split(",");

        String _excludes = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "excludes");
        if(!JUtilString.isBlank(_excludes)) excludes=_excludes.split(",");

        //tcp缓冲区大小
        String tcpBufferSize = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "tcpBufferSize");
        if(!JUtilMath.isInt(tcpBufferSize)){
            log.log("tcpBufferSize of service is not set", -1);
            return;
        }

        tcpSendBufferSize = Integer.parseInt(tcpBufferSize);
        tcpReceiveBufferSize = Integer.parseInt(tcpBufferSize);

        //是否服务端
        String isServer = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "isServer");
        if(!"true".equals(isServer)) return;

        //监听端口
        String listOn = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "listOn");
        if(JUtilString.isBlank(listOn)) listOn="2000";

        //服务端处理线程池大小
        String serverHandlerPoolSize = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "serverHandlerPoolSize");
        if(!JUtilMath.isInt(serverHandlerPoolSize)) serverHandlerPoolSize="10";

        //响应线程池大小
        String respondPoolSize = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "respondPoolSize");
        if(!JUtilMath.isInt(respondPoolSize)) respondPoolSize="1";

        //响应线程池执行频率
        String respondPoolExecutingPeriod = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "respondPoolExecutingPeriod");
        if(!JUtilMath.isInt(respondPoolExecutingPeriod)) respondPoolExecutingPeriod="10000";

        //服务端Selector分组数
        String serverSelectors = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "serverSelectors");
        if(!JUtilMath.isInt(serverSelectors)) serverSelectors="1";

        String serverSelectorExecutingPeriod = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "serverSelectorExecutingPeriod");
        if(!JUtilMath.isLong(serverSelectorExecutingPeriod)) serverSelectorExecutingPeriod="10000";

        //注册中心允许的最大Client数量
        String maxClients = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "maxClients");
        if(!JUtilMath.isInt(maxClients)) maxClients="100";

        //注册中心允许的每个IP上最大Client数量
        String maxClientsPerIp = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "maxClientsPerIp");
        if(!JUtilMath.isInt(maxClientsPerIp)) maxClientsPerIp="1";

        //是否调试
        String debug = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "debug");

        //通信层
        String networker = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "networker");

        //启动监听（多端口）
        log.log("starting service server on port "+listOn+"(tcpBufferSize:"+tcpSendBufferSize+"|"+tcpReceiveBufferSize+", respondPoolSize:"+respondPoolSize+")", -1);

        String[] listOns=listOn.split(",");
        for(int i=0; i<listOns.length; i++){
            if(!JUtilMath.isInt(listOns[i])) continue;

            Map<Integer, Object> options=SocketOptions.cloneDefaults();
            options.put(java.net.SocketOptions.SO_SNDBUF, tcpSendBufferSize);
            options.put(java.net.SocketOptions.SO_RCVBUF, tcpReceiveBufferSize);
            options.put(SocketOptions.SERVER_RESPOND_POOL_SIZE, Integer.parseInt(respondPoolSize));
            options.put(SocketOptions.SERVER_RESPOND_POOL_EXECUTE_INTERVAL, Integer.parseInt(respondPoolExecutingPeriod));
            options.put(SocketOptions.SERVER_SELECTORS, Integer.parseInt(serverSelectors));
            options.put(SocketOptions.SERVER_SELECTOR_EXECUTE_INTERVAL, Integer.parseInt(serverSelectorExecutingPeriod));
            options.put(SocketOptions.SERVER_HANDLE_POOL_SIZE, Integer.parseInt(serverHandlerPoolSize));

            Channel channel = null;
            if("nio".equalsIgnoreCase(networker)){
                channel = new NioChannel(Integer.parseInt(listOns[i]),
                        Integer.parseInt(maxClients),
                        Integer.parseInt(maxClientsPerIp),
                        options,
                        null,
                        "true".equalsIgnoreCase(debug));
            }else{
                channel = new HttpChannel(Integer.parseInt(listOns[i]),
                        Integer.parseInt(maxClients),
                        Integer.parseInt(maxClientsPerIp),
                        options,
                        null,
                        "true".equalsIgnoreCase(debug));
            }

            Services.addPort(Integer.parseInt(listOns[i]));

            channels.put(Integer.valueOf(listOns[i]), channel);
        }

        List<Channel> _channels = channels.listValues();
        for(Channel channel : _channels) channel.startup();
    }

    /**
     * 某端口上正在处理的任务数
     * @param port
     * @return
     */
    public static Integer getPackages(Integer port){
        Channel channel = channels.get(port);
        return channel==null?0:channel.getTasksInProccess();
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

    public Server(){
        Map<Integer, Object> options=SocketOptions.cloneDefaults();
        options.put(java.net.SocketOptions.SO_SNDBUF, tcpSendBufferSize);
        options.put(java.net.SocketOptions.SO_RCVBUF, tcpReceiveBufferSize);
        options.put(SocketOptions.SERVER_RESPOND_POOL_SIZE, 1);
        this.socketOptions=options;
    }

    /**
     * 数据片段（tcp缓冲区大小）
     * @return
     */
    public int getSegmentSize(){
        return (Integer)socketOptions.get(java.net.SocketOptions.SO_SNDBUF);
    }

    /**
     * 配套的DataSource的block大小（数据片段大小 - 22个字节的数据包描述信息）
     * @return
     */
    public int getDataSourceBlockSize(){
        return this.getSegmentSize() - 22;
    }

    /**
     *
     * @param service
     * @return
     * @throws Exception
     */
    synchronized public static ServiceBase getObject(Service service) throws Exception{
        if(services.containsKey(service.getPath()))  return services.get(service.getPath());

        ServiceBase obj = (ServiceBase)Class.forName(service.getClazz()).getDeclaredConstructor().newInstance();
        obj.setConfig(service);
        services.put(service.getPath(), obj);

        return obj;
    }

    /**
     *
     * @param serv
     * @param methodName
     * @param headers
     * @param params
     * @param files
     * @param payload
     * @param objects
     * @return
     * @throws Exception
     */
    public static ServiceResponse callLocal(ServiceBase serv,
                                            String methodName,
                                            Map<String, String> headers,
                                            Map<String, String> params,
                                            Map<String, DataSourceFile> files,
                                            String payload,
                                            Object[] objects) throws Exception{
        if(serv==null) return null;

        //调用哪个方法
        int callWhichMethod=0;
        Method method=null;
        if(!(headers!=null && headers.isEmpty())
                || (params!=null && !params.isEmpty())
                || (files!=null && !files.isEmpty())
                || !JUtilString.isBlank(payload)){//匹配包含全功能参数列表的方法
            try{
                method = serv.getClass().getDeclaredMethod(methodName, Map.class, Map.class, Map.class, String.class, Object[].class);
                callWhichMethod=1;
            }catch(Exception e){
                //e.printStackTrace();
            }
        }

        if(method==null){
            try{
                method = Methods.matches(serv.getClass(), methodName, objects);
                callWhichMethod=2;
            }catch(Exception e){
                //e.printStackTrace();
            }
        }

        if(method==null) return null;

        //调用服务
        Object responseObject=null;
        if(callWhichMethod==1) responseObject = method.invoke(serv, headers, params, files, payload, objects);
        if(callWhichMethod==2) responseObject = method.invoke(serv, objects);
        if(responseObject != null && !(responseObject instanceof ServiceResponse)){
            return null;
        }

        return (ServiceResponse)responseObject;
    }


    @Override
    public void run() {
        int count=0;
        while(true){
            try{
                Thread.sleep(5000);
            }catch (Exception e){}

            if(Startup.isDestroyed()){
                log.log("system is down!", -1);
                return;
            }

            if(!Services.hasServicesTobeStarted()){
                log.log("no services tobe started!", -1);
                return;
            }

            ConcurrentList<RegistryChannel> channels = Registry.getChannels();
            if(channels.isEmpty()){
                log.log("no registry channel available.", -1);
                return;
            }

            try{
                //注册中心通信层
                String networker = j.core.nvwa.Nvwa.getParameter(Registry.class, "networker");

                //服务通信层
                String networker4Service = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "networker");

                //请求内容
                Map<String, DataSource> entities = new HashMap();

                String ip = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "ip");
                if(ip==null) ip="";

                StringBuffer servicesString=new StringBuffer();
                servicesString.append("{\"networker\":\""+networker4Service+"\",\"ip\":\""+ip+"\",\"services\":");
                servicesString.append(Services.toJson(false));
                servicesString.append("}");

                DataSourceString dataSourceString=new DataSourceString(this.getDataSourceBlockSize(), "UTF-8");
                dataSourceString.setSource(servicesString.toString());
                entities.put(Registry.FLAG_SERVICES, dataSourceString);

                if(count==0) log.log("send services to registry -> "+servicesString.toString(), -1);

                //签名
                String signature = Signature.sign(JUtilString.appendStringsIgnoreNulls(Registry.COMMAND_REG, servicesString.toString()),
                        j.core.nvwa.Nvwa.getParameter(Registry.class, Constants.ACCESS_SECRET));

                //请求头
                Map<String, String> headers = new HashMap();
                headers.put(Constants.ACCESS_KEY, j.core.nvwa.Nvwa.getParameter(Registry.class, Constants.ACCESS_KEY));
                headers.put(Constants.SIGNATURE, signature);

                //向注册中心发送服务信息
                if("nio".equals(networker)){
                    for(int i=0; i<channels.size(); i++){
                        HttpClient endpoint=null;
                        if(endpoint == null){
                            //找到第一个可用的通道（访问注册中心的并发量有限，不考虑负载均衡）
                            endpoint=channels.get(i).getEndpoint();
                        }else{
                            //触发连接
                            channels.get(i).getEndpoint();
                        }

                        //没有可用的通道
                        if(endpoint==null){
                            log.log("no registry channel available.", -1);
                            continue;
                        }

                        //发送请求
                        DataPackage resp = endpoint.request(Registry.COMMAND_REG,
                                headers,
                                null,
                                entities,
                                5000);

                        if (resp == null) {
                            log.log("no response from registry.", -1);
                            continue;
                        }

                        DataSourceString respEntity = (DataSourceString) resp.getEntity(Registry.FLAG_RESP);
                        if(respEntity==null){
                            log.log("no response entity from registry.", -1);
                            continue;
                        }

                        //注册中心响应
                        if(count==0) log.log("response from registry -> "+respEntity.getSourceString(), -1);
                    }
                }else{
                    for(int i=0; i<channels.size(); i++) {
                        RegistryChannel channel = channels.get(i);

                        String endpoint = networker + "://" + channel.getHost() + ":" + channel.getPort() + "/framework/service/registry/channel/http/request";

                        //请求ID
                        long thisRequestId = HttpClient.getRequestId();

                        //请求数据包
                        DataPackage dataPackage = new DataPackage(thisRequestId, 0, Client.getSegmentSize());

                        dataPackage.setRequestLine("POST " + Registry.COMMAND_REG);
                        dataPackage.addHeaders(headers);
                        dataPackage.addEntities(entities);

                        //通过http发送请求
                        JHttpContext context = dataPackage.sendViaHttp(null, null, null, endpoint);

                        //读取响应
                        DataPackage resp = DataPackage.receiveStream(context.getResponseStream());

                        if(resp == null) {
                            log.log("no response from registry.", -1);
                            continue;
                        }

                        DataSourceString respEntity = (DataSourceString) resp.getEntity(Registry.FLAG_RESP);
                        if(respEntity==null){
                            log.log("no response entity from registry.", -1);
                            continue;
                        }

                        //注册中心响应
                        if(count==0) log.log("response from registry -> "+respEntity.getSourceString(), -1);
                    }
                }

                count=1;
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
            }
        }
    }
}