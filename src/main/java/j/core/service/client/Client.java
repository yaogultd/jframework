package j.core.service.client;

import j.core.annotation.configuration.Properties;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.Global;
import j.core.hp.asynchronous.Waitings;
import j.core.hp.thread.ThreadManager;
import j.core.hp.thread.ThreadPool;
import j.core.nio.DataPackage;
import j.core.nio.DataSourceFile;
import j.core.nio.DataSourceString;
import j.core.nio.SocketOptions;
import j.core.nio.http.HttpClient;
import j.core.nvwa.Nvwa;
import j.core.permission.Signature;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.core.service.exception.NoNodeAvailableException;
import j.core.service.registry.HostSorter;
import j.core.service.registry.Registration;
import j.core.service.registry.Registry;
import j.core.service.registry.RegistryChannel;
import j.core.service.server.Server;
import j.core.service.server.config.Service;
import j.core.service.server.config.Services;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.http.JHttpContext;
import j.log.Logger;
import j.util.*;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.NoOp;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ClassDescription(author = "肖炯",
        date = "2021/07/14",
        description = "从注册中心获取服务信息，并向应用提供服务调用入口",
        reviewers = {})
@j.core.annotation.nvwa.Nvwa
@Properties(path = "service.service.properties")
public class Client extends TimerTask {
    private static Logger log=Logger.create(Client.class);

    @FieldDescription(description = "与注册中心同步的服务注册信息，key为服务调用路径，value为该服务的节点列表")
    private static ConcurrentMap<String, ConcurrentList<Registration>> registrations=new ConcurrentMap<String, ConcurrentList<Registration>>();

    @FieldDescription(description = "对节点排序，选取最空闲的")
    private static HostSorter hostSorter=new HostSorter();

    //服务对象（客户端代理）
    private static ConcurrentHashMap<String, ServiceBase> services=new ConcurrentHashMap<>();

    //是否完成了初始化（第一次成功获取到服务信息）
    private static boolean initialized=false;

    //等待完成初始化的最长时间（秒）
    private static int waitingForInitialized=60;

    private static Map<Integer, Object> socketOptions = SocketOptions.cloneDefaults();

    /**
     *
     * @throws Exception
     */
    public static void start() throws Exception{
        String isClient = j.core.nvwa.Nvwa.getParameter(Registry.class, "isClient");
        if(!"true".equals(isClient)) {//如果不是客户端
            log.log("this node is not a service client", -1);
            return;
        }

        //tcp缓冲区大小
        String tcpBufferSize = j.core.nvwa.Nvwa.getParameter(Registry.class, "tcpBufferSize");
        if(!JUtilMath.isInt(tcpBufferSize)){
            log.log("tcpBufferSize of service registry is not set", -1);
            return;
        }

        Map<Integer, Object> options= SocketOptions.cloneDefaults();
        options.put(java.net.SocketOptions.SO_SNDBUF, Integer.parseInt(tcpBufferSize));
        options.put(java.net.SocketOptions.SO_RCVBUF, Integer.parseInt(tcpBufferSize));
        socketOptions = options;

        //定时获取服务信息
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Client(), 1000, 2000, TimeUnit.MILLISECONDS);
    }



    /**
     * 数据片段（tcp缓冲区大小）
     * @return
     */
    public static int getSegmentSize(){
        return (Integer)socketOptions.get(java.net.SocketOptions.SO_SNDBUF);
    }

    /**
     * 配套的DataSource的block大小（数据片段大小 - 22个字节的数据包描述信息）
     * @return
     */
    public static int getDataSourceBlockSize(){
        return getSegmentSize() - 22;
    }

    /**
     *
     * @param _waitingForInitialized
     */
    public static void setWaitingForInitialized(int _waitingForInitialized){
        waitingForInitialized=_waitingForInitialized;
    }

    /**
     * 等待获取服务信息
     */
    private static void waiting(){
        int secs=0;
        while(!initialized && secs<=waitingForInitialized){
            secs++;
            try{
                Global.sleep1000Millis();
            }catch (Exception e){}
        }
    }

    /**
     *
     * @return
     */
    public static boolean isInitialized(){
        return initialized;
    }


    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据服务类名获得远程对象，完全当做本地对象调用继续（应用无需关心底层细节）")
    public static ConcurrentList<Registration> findRegistration(String pathOrClassName){
        waiting();

        if(registrations.containsKey(pathOrClassName)) return registrations.get(pathOrClassName);

        ConcurrentList<ConcurrentList<Registration>> regs=registrations.listValues();
        for(int i=0; i<regs.size(); i++){
            ConcurrentList<Registration> _regs=regs.get(i);
            if(_regs.isEmpty()) continue;

            Registration reg=_regs.get(0);
            if(reg.getService().matches(pathOrClassName)) return _regs;
        }

        return null;
    }

    /**
     * 查找本地服务节点
     * @param validRegs
     * @return
     */
    private static Registration findLocal(List<Registration> validRegs){
        for(int i=0; i<validRegs.size(); i++){
            if(validRegs.get(i).isLocal()) return validRegs.get(i);
        }
        return null;
    }

    /**
     * 分配远程节点（实现负载均衡）
     * @param validRegs
     * @return
     */
    private static Registration allocRegistration(List<Registration> validRegs){
        if(Services.localServiceFirst()){
            Registration r=findLocal(validRegs);
            if(r!=null){
                if(Nvwa.isDebug()){
                    log.log("[client]find local service => "+r, -1);
                }
                return r;
            }
        }

        if(validRegs.size()>1) validRegs=hostSorter.bubble(validRegs, JUtilSorter.ASC);

        if(Nvwa.isDebug()){
            log.log("[client]alloc service => "+validRegs.get(0), -1);
        }

        return validRegs.get(0);
    }

    /**
     *
     * @param service
     * @return
     * @throws Exception
     */
    private static ServiceBase getObject(Service service) throws Exception{
        if(Services.localServiceFirst()){//本地优先（直接调用）
            ServiceBase local=getObjectLocal(service.getClazz());
            if(local!=null) return local;
        }

        //服务全部是单例（不管是服务端的Service对象，还是客户端的Proxy对象）
        if(services.contains(service.getPath())) return services.get(service.getPath());

        ServiceBase obj = (ServiceBase)Class.forName(service.getClazz()).getDeclaredConstructor().newInstance();

        Proxy<ServiceBase> p=new Proxy();
        ServiceBase proxy=p.bind(obj, new Callback[] {p, NoOp.INSTANCE}, ProxyFilter.getInstance());
        proxy.setConfig(service);

        services.put(service.getPath(), proxy);

        return proxy;
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "本地（同jvm中）服务对象")
    private static ServiceBase getObjectLocal(String cls) throws Exception{
        //log.log("获取本地对象 -> "+cls, -1);
        Service config=Services.getService(cls);
        if(config==null || !Services.on(config)) return null;

        return Server.getObject(config);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据服务类名获得远程对象，完全当做本地对象调用继续（应用无需关心底层细节）")
    public static ServiceBase getService(Class c) throws Exception{
        return getService(c.getCanonicalName());
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据服务调用路径获得远程对象，完全当做本地对象调用继续（应用无需关心底层细节）")
    public static ServiceBase getService(String pathOrClassName) throws Exception{
        ConcurrentList<Registration> regs=findRegistration(pathOrClassName);

        if(regs==null || regs.isEmpty()){
            if(Nvwa.isDebug()){
                log.log("未找到匹配的服务注册信息 => "+pathOrClassName, -1);
            }
            return null;
        }

        //服务
        return getObject(regs.get(0).getService());
    }

    /**
     * 根据服务类名获得远程对象，如未获取到则每间隔1秒重试，直至超时
     * @param c
     * @param timeoutSecs 超时时间（秒）
     * @return
     * @throws Exception
     */
    public static ServiceBase waitService(Class c, int timeoutSecs) throws Exception{
        return waitService(c.getCanonicalName(), timeoutSecs);
    }

    /**
     * 根据服务调用路径获得远程对象，如未获取到则每间隔1秒重试，直至超时
     * @param pathOrClassName
     * @param timeoutSecs 超时时间（秒）
     * @return
     * @throws Exception
     */
    public static ServiceBase waitService(String pathOrClassName, int timeoutSecs) throws Exception{
        ServiceBase serv = getService(pathOrClassName);
        while(serv==null && timeoutSecs>0){
            timeoutSecs--;
            Global.sleep1000Millis();
            serv = getService(pathOrClassName);
        }
        return serv;
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据http请求调用服务")
    public static ServiceResponse call(String path, HttpServletRequest request) throws Exception{
        ServiceBase service=getService(path);

        if(service==null) throw new Exception("service("+path+") is not exists.");

        //解析调用的方法
        String method=path;
        if(method.endsWith("/")) method=method.substring(0, method.length()-1);

        //参数
        Map<String, String> params = SysUtil.getHttpParameterMap(request);

        //请求信息
        String payload = JUtilInputStream.string(request.getInputStream(), SysConfig.sysEncoding);

        return call(service, method, null, params, null, payload, null);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据路径调用服务")
    public static ServiceResponse call(String path, String payload) throws Exception{
        ServiceBase service=getService(path);

        if(service==null) throw new Exception("service("+path+") is not exists.");

        //解析调用的方法
        String method=path;
        if(method.endsWith("/")) method=method.substring(0, method.length()-1);

        return call(service, method, null, null, null, payload, null);
    }

    /**
     *
     * @param path
     * @param objects objects数组的长度与调用的服务方法的参数个数必须一致
     * @return
     * @throws Exception
     */
    public static ServiceResponse call(String path, Object[] objects) throws Exception{
        ServiceBase service=getService(path);

        if(service==null) throw new Exception("service("+path+") is not exists.");

        //解析调用的方法
        String method=path;
        if(method.endsWith("/")) method=method.substring(0, method.length()-1);

        return call(service, method, null, null, null, null, objects);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据路径调用服务")
    public static ServiceResponse call(String path,
                                       Map<String, String> headers,
                                       Map<String, String> params,
                                       Map<String, DataSourceFile> files,
                                       String payload,
                                       Object[] objects) throws Exception{
        ServiceBase service=getService(path);

        if(service==null) throw new Exception("service("+path+") is not exists.");

        //解析调用的方法
        String method=path;
        if(method.endsWith("/")) method=method.substring(0, method.length()-1);

        return call(service, method, headers, params, files, payload, objects);
    }

    /**
     *
     * @param service
     * @param method
     * @param headers
     * @param params
     * @param files
     * @param payload
     * @param objects
     * @return
     */
    public static ServiceResponse call(ServiceBase service,
                                          String method,
                                          Map<String, String> headers,
                                          Map<String, String> params,
                                          Map<String, DataSourceFile> files,
                                          String payload,
                                          Object[] objects){
        ConcurrentList<Registration> regs=findRegistration(service.getConfig().getPath());

        if(regs==null || regs.isEmpty()) {
            //没有对应的服务注册记录
            return new ServiceResponse(false, "service_not_available","");
        }

        return call(allocRegistration(regs), method, headers, params, files, payload, objects);
    }

    /**
     * 实际执行远程调用指定的远程节点
     * @param reg
     * @param method
     * @param headers
     * @param params
     * @param files
     * @param payload
     * @param objects
     * @return
     */
    public static ServiceResponse call(Registration reg,
                                          String method,
                                          Map<String, String> headers,
                                          Map<String, String> params,
                                          Map<String, DataSourceFile> files,
                                          String payload,
                                          Object[] objects){
        if(method.length()>reg.getService().getPath().length()){
            method=method.substring(reg.getService().getPath().length() + 1);
        }

        //通过Caller调用
        Caller caller = Caller.getInstance(reg.getHost(), reg.getPort());
        try {
            //http / https
            String networker = reg.getNetworker();

            if("nio".equalsIgnoreCase(networker)) {
                return caller.call(reg.getService().getMethod(method).getTimeout(),
                        reg,
                        method,
                        headers,
                        params,
                        files,
                        payload,
                        objects);
            }else{
                return caller.callViaHttp(reg.getService().getMethod(method).getTimeout(),
                        reg,
                        method,
                        headers,
                        params,
                        files,
                        payload,
                        objects);
            }
        } catch (Exception e) {
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "network_communication_error", "");
        }
    }

    /**
     * 调用全部可用节点
     * @param asyn 是否异步
     * @param excludeLocalNode 是否排除同一系统中的服务节点
     * @param service
     * @param method
     * @param headers
     * @param params
     * @param files
     * @param payload
     * @param objects
     * @return
     */
    public static List<ServiceResponse> callAll(Boolean asyn,
                                                Boolean excludeLocalNode,
                                                ServiceBase service,
                                                String method,
                                                Map<String, String> headers,
                                                Map<String, String> params,
                                                Map<String, DataSourceFile> files,
                                                String payload,
                                                Object[] objects) throws Exception{
        List<ServiceResponse> responses=new ArrayList<>();
        ConcurrentList<Registration> regs=findRegistration(service.getConfig().getPath());
        if(regs==null || regs.isEmpty()) throw new NoNodeAvailableException();

        List<String> requests=new ArrayList<>();
        for(int i=0; i<regs.size(); i++){
            if(excludeLocalNode && regs.get(i).isLocal()) continue;

            if(asyn){
                String uuid=JUtilUUID.genUUID();
                Waitings.waiting(uuid, service.getConfig().getMethod(method).getTimeout(), new ServiceResponse<>(false, "timeout", "call service timeout."));
                
                ThreadPool pool= ThreadManager.getPool("SERVICE-"+service.getConfig().getPath(), regs.size(), 100, TimeUnit.MICROSECONDS,3600000L, ThreadPool.SELECT_TYPE_IDLEST);
                pool.addTask(new CallingTask(new Object[]{regs.get(i), method, headers, params, files, payload, objects}, 0, uuid, service.getConfig().getMethod(method).getTimeout()));
                requests.add(uuid);
            }else{
                try {
                    ServiceResponse response = call(regs.get(i), method, headers, params, files, payload, objects);
                    if(response != null) responses.add(response);
                }catch(Exception e){
                    log.log(e, Logger.LEVEL_ERROR);
                }
            }
        }

        if(!requests.isEmpty()){
            while(responses.size() < requests.size()){
                for(int i=0; i<requests.size(); i++){
                    String uuid=requests.get(i);
                    ServiceResponse response=(ServiceResponse)Waitings.hasResult(uuid);
                    if(response != null) responses.add(response);
                }
                Thread.sleep(1);
            }
        }

        return responses;
    }

    @Override
    public void run() {
        try{
            ConcurrentList<RegistryChannel> channels = Registry.getChannels();
            if(channels.isEmpty()){
                log.log("no registry channel available.", -1);
                return;
            }

            //注册中心通信层
            String networker = j.core.nvwa.Nvwa.getParameter(Registry.class, "networker");

            DataPackage resp = null;

            //签名
            String signature = Signature.sign(JUtilString.appendStringsIgnoreNulls(Registry.COMMAND_QUERY),
                    Nvwa.getParameter(Registry.class, Constants.ACCESS_SECRET));

            //请求头
            Map<String, String> headers = new HashMap();
            headers.put(Constants.ACCESS_KEY, Nvwa.getParameter(Registry.class, Constants.ACCESS_KEY));
            headers.put(Constants.SIGNATURE, signature);

            if("nio".equals(networker)) {
                HttpClient endpoint=null;
                for (int i = 0; i < channels.size(); i++) {
                    if (endpoint == null) {
                        //找到第一个可用的通道（访问注册中心的并发量有限，不用考虑负载均衡）
                        endpoint = channels.get(i).getEndpoint();
                    } else {
                        //触发连接
                        channels.get(i).getEndpoint();
                    }
                }

                //没有可用的通道
                if (endpoint == null) {
                    log.log("no registry channel available.", -1);
                    return;
                }

                //发送请求
                resp = endpoint.request(Registry.COMMAND_QUERY, headers, null, null, 5000);
            }else{
                RegistryChannel channel = channels.get(0);

                String endpoint = networker + "://" + channel.getHost() + ":" + channel.getPort() + "/framework/service/registry/channel/http/request";

                //请求ID
                long thisRequestId = HttpClient.getRequestId();

                //请求数据包
                DataPackage dataPackage = new DataPackage(thisRequestId, 0, Client.getSegmentSize());

                dataPackage.setRequestLine("POST " + Registry.COMMAND_QUERY);
                dataPackage.addHeaders(headers);

                //通过http发送请求
                JHttpContext context = dataPackage.sendViaHttp(null, null, null, endpoint);

                //读取响应
                resp = DataPackage.receiveStream(context.getResponseStream());
            }

            if(resp == null) {
                log.log("no response from registry.", -1);
                return;
            }

            DataSourceString respEntity = (DataSourceString) resp.getEntity(Registry.FLAG_RESP);
            if(respEntity==null){
                log.log("no response entity from registry.", -1);
                return;
            }

            //解析服务信息
            //服务信息格式应该如下：
            //{"services":$Services.toString()}
            String jsonServiceString=respEntity.getSourceString();
            //log.log("jsonServiceString from registry -> "+jsonServiceString, -1);

            //验证响应签名
            signature=resp.getHeader(Constants.SIGNATURE);

            //配置的accessSecret
            String accessSecretSet=Nvwa.getParameter(Registry.class, Constants.ACCESS_SECRET);

            //期待的正确签名
            String signatureExpected = Signature.sign(JUtilString.appendStringsIgnoreNulls(jsonServiceString), accessSecretSet);

            if(!JUtilString.equals(signature, signatureExpected)){
                log.log("signature of response from registry error(accessSecretSet = "+accessSecretSet+", jsonServiceString = "+jsonServiceString+", signatureExpected = "+signatureExpected+", signature = "+signature+").", -1);
                return;
            }

            doReg(JUtilJSON.parse(jsonServiceString));

            if(!registrations.isEmpty()) initialized=true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
    }

    /**
     * 服务是否已经注册
     * @param host
     * @param service
     * @return
     */
    private static Registration exists(String host, Service service){
        ConcurrentList<Registration> ofService=registrations.get(service.getPath());
        if(ofService==null || ofService.isEmpty()) return null;

        for(int i=0; i<ofService.size(); i++){
            Registration registration=ofService.get(i);
            if(registration.getHost().equals(host)) return registration;
        }

        return null;
    }

    @MethodDescription(author = "肖炯", date = "2021/12/12", description = "注册")
    private static void doReg(JSONObject jsonServices){
        JSONObject result = JUtilJSON.object(jsonServices, "result");
        if(result == null) return;

        JSONArray services = JUtilJSON.array(result, Registry.FLAG_SERVICES);
        for(int i=0; services!=null && i<services.length(); i++){
            JSONObject service=JUtilJSON.get(services, i);
            //String path=JUtilJSON.string(service, Registry.FLAG_PATH);

            JSONArray rs=JUtilJSON.array(service, Registry.FLAG_SERVICE_REGISTRATIONS);
            for(int j=0; j<rs.length(); j++){
                Registration r=Registration.fromJson(JUtilJSON.get(rs, j));
                reg(r);
            }
        }
    }

    /**
     *
     * @param r
     */
    private static void reg(Registration r){
        ConcurrentList<Registration> ofService=registrations.get(r.getService().getPath());
        if(ofService == null) ofService=new ConcurrentList<>();

        Registration registration=exists(r.getHost(), r.getService());
        if(registration==null) {
            ofService.add(r);
            log.log("get new service registration -> "+r.toString(), -1);
        }else{
            registration.setHeartbeatAt(r.getHeartbeatAt());
            registration.setPaused(r.isPaused());
            registration.setPackages(r.getPackages());
            //log.log("refresh service registration -> "+r.toString(), -1);
        }

        registrations.put(r.getService().getPath(), ofService);
    }
}
