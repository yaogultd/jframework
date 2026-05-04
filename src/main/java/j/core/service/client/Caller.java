package j.core.service.client;

import j.core.annotation.description.ClassDescription;
import j.core.nio.*;
import j.core.nio.http.HttpClient;
import j.core.nio.http.HttpClientPool;
import j.core.nvwa.Nvwa;
import j.core.serialize.JSerialization;
import j.core.service.ServiceResponse;
import j.core.service.registry.Registration;
import j.core.service.registry.Registry;
import j.core.service.server.Server;
import j.http.JHttpContext;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilMath;
import j.util.JUtilString;
import j.util.JUtilTimestamp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/11/25",
        description = "执行实际的远程调用")
public class Caller{
    private static Logger log=Logger.create(Caller.class);
    private static ConcurrentMap<String, Caller> callers=new ConcurrentMap<>();
    private HttpClientPool clientPool;

    /**
     *
     * @param host
     * @param port
     * @return
     */
    public static Caller getInstance(String host, int port){
        String key=host+":"+port;
        if(callers.containsKey(key)) return callers.get(key);
        try{
            Caller caller=new Caller();

            if("nio".equalsIgnoreCase(j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "networker"))){
                Map<Integer, Object> options = SocketOptions.cloneDefaults();
                options.put(java.net.SocketOptions.SO_SNDBUF, j.core.service.server.Server.getTcpSendBufferSize());
                options.put(java.net.SocketOptions.SO_RCVBUF, j.core.service.server.Server.getTcpReceiveBufferSize());
                options.put(SocketOptions.SO_MAX_IDLE, JUtilTimestamp.millisOfHour);

                String clientSelectorExecutingPeriod = Nvwa.getParameter(j.core.service.server.Server.class, "SERVICE", "clientSelectorExecutingPeriod");
                if(!JUtilMath.isInt(clientSelectorExecutingPeriod)) clientSelectorExecutingPeriod="1000";
                options.put(SocketOptions.CLIENT_SELECTOR_EXECUTE_INTERVAL, Integer.valueOf(clientSelectorExecutingPeriod));

                String poolSize = Nvwa.getParameter(j.core.service.server.Server.class, "SERVICE", "clientPoolSize");
                if(!JUtilMath.isInt(poolSize)) poolSize="10";

                caller.clientPool = HttpClientPool.getInstance(host, port, Integer.parseInt(poolSize), options, null);
            }

            callers.put(key, caller);

            return caller;
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     */
    private Caller(){

    }

    /**
     *
     * @param timeout
     * @param registration
     * @param method
     * @param headers
     * @param params
     * @param files
     * @param objects
     * @return
     * @throws Exception
     */
    public ServiceResponse call(long timeout,
                                Registration registration,
                                String method,
                                Map<String, String> headers,
                                Map<String, String> params,
                                Map<String, DataSourceFile> files,
                                String payload,
                                Object[] objects) throws Exception{
        HttpClient executor = clientPool.getClient();

        Map<String, DataSource> entities = new HashMap<>();

        if(files!=null && !files.isEmpty()){
            Iterator<String> keys=files.keySet().iterator();
            while(keys.hasNext()){
                String name=keys.next();
                DataSourceFile file=files.get(name);
                file.setBlockSize(Client.getDataSourceBlockSize());

                entities.put(name, file);
            }
        }

        if(!JUtilString.isBlank(payload)){
            DataSourceString payloadEntity=new DataSourceString(Client.getDataSourceBlockSize(), "UTF-8");
            payloadEntity.setSource(payload);
            entities.put(Protocol.J_PAYLOAD, payloadEntity);
        }

        if(objects != null && objects.length>0){
            DataSourceObject objectsEntity=new DataSourceObject(Client.getDataSourceBlockSize(), "UTF-8");
            objectsEntity.setSource(Object[].class.getCanonicalName(), objects);
            entities.put(Protocol.J_OBJECTS, objectsEntity);
        }

        DataPackage resp = executor.request(JUtilString.appendUrl(registration.getService().getPath(), method),
                headers,
                params,
                entities,
                timeout);


        if(resp==null){
            return new ServiceResponse(false, "no_response_from_service", "service mirror -> "+executor.getHost()+":"+executor.getPort());
        }

        DataSourceObject respEntity = (DataSourceObject) resp.getEntity(Protocol.J_RESPONSE);
        if(respEntity==null){
            return new ServiceResponse(false, "no_response_entity_from_service", "service mirror -> "+executor.getHost()+":"+executor.getPort());
        }

        //FST序列化
        return (ServiceResponse)JSerialization.deSerialize(null, respEntity.getSource(), true);
    }

    /**
     *
     * @param timeout
     * @param registration
     * @param method
     * @param headers
     * @param params
     * @param files
     * @param objects
     * @return
     * @throws Exception
     */
    public ServiceResponse callViaHttp(long timeout,
                                Registration registration,
                                String method,
                                Map<String, String> headers,
                                Map<String, String> params,
                                Map<String, DataSourceFile> files,
                                String payload,
                                Object[] objects) throws Exception{
        Map<String, DataSource> entities = new HashMap<>();

        if(files!=null && !files.isEmpty()){
            Iterator<String> keys=files.keySet().iterator();
            while(keys.hasNext()){
                String name=keys.next();
                DataSourceFile file=files.get(name);
                file.setBlockSize(Client.getDataSourceBlockSize());

                entities.put(name, file);
            }
        }

        if(!JUtilString.isBlank(payload)){
            DataSourceString payloadEntity=new DataSourceString(Client.getDataSourceBlockSize(), "UTF-8");
            payloadEntity.setSource(payload);
            entities.put(Protocol.J_PAYLOAD, payloadEntity);
        }

        if(objects != null && objects.length>0){
            DataSourceObject objectsEntity=new DataSourceObject(Client.getDataSourceBlockSize(), "UTF-8");
            objectsEntity.setSource(Object[].class.getCanonicalName(), objects);
            entities.put(Protocol.J_OBJECTS, objectsEntity);
        }

        //http / https
        String networker = registration.getNetworker();

        String serverPoint = networker+"://"+registration.getHost()+":"+registration.getPort()+"/framework/service/channel/http/request";

        //请求ID
        long thisRequestId=HttpClient.getRequestId();

        //请求数据包
        DataPackage request = new DataPackage(thisRequestId, 0, Client.getSegmentSize());

        request.setRequestLine("POST "+JUtilString.appendUrl(registration.getService().getPath(), method));
        request.addHeaders(headers);
        request.addParams(params);
        request.addEntities(entities);

        //通过http发送请求
        JHttpContext context = request.sendViaHttp(null, null, null, serverPoint);

        //读取响应
        DataPackage resp = DataPackage.receiveStream(context.getResponseStream());
        if(resp==null){
            return new ServiceResponse(false, "no_response_from_service", "service mirror -> "+serverPoint);
        }

        DataSourceObject respEntity = (DataSourceObject) resp.getEntity(Protocol.J_RESPONSE);
        if(respEntity==null){
            return new ServiceResponse(false, "no_response_entity_from_service", "service mirror -> "+serverPoint);
        }

        //FST序列化
        return (ServiceResponse)JSerialization.deSerialize(null, respEntity.getSource(), true);
    }
}
