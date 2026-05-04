package j.core.service.server.channel;

import j.core.lang.Methods;
import j.core.nio.*;
import j.core.nio.http.HttpServer;
import j.core.serialize.JSerialization;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.core.service.server.config.Service;
import j.core.service.server.config.ServiceMethod;
import j.core.service.server.config.Services;
import j.log.Logger;
import j.util.JUtilBean;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class NioChannel extends HttpServer implements Channel {
    private static Logger log=Logger.create(NioChannel.class);

    private int listenOn;
    private int maxClients;
    private int maxClientsPerIp;
    private Map<Integer, Object> options;
    private Object[] args;
    private boolean debug;

    private j.core.nio.Server server;

    public NioChannel(int listenOn,
                      int maxClients,
                      int maxClientsPerIp,
                      Map<Integer, Object> options,
                      Object[] args,
                      boolean debug){
        this.listenOn = listenOn;
        this.maxClients = maxClients;
        this.maxClientsPerIp = maxClientsPerIp;
        this.options = options==null || options.isEmpty() ? SocketOptions.cloneDefaults() : options;
        this.args = args;
        this.debug = debug;
    }

    @Override
    public boolean startup()  throws Exception{
        server = j.core.nio.Server.start(listenOn,
                NioChannel.class,
                maxClients,
                maxClientsPerIp,
                options,
                null);
        server.setDebug(debug);

        return true;
    }

    @Override
    public int getTasksInProccess(){
        return this.server.getPackages();
    }

    @Override
    public void doRespond(int statusCode, String responseCode, DataPackage dataPackage) throws Exception{
        statusCode=200;

        String uri="";//请求路径
        String requestLine=dataPackage.getRequestLine();
        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server begin process request("+dataPackage.getId()+") => "+requestLine, -1);
        }

        if(requestLine.startsWith("POST ")) uri=requestLine.substring(5);

        //请求的服务
        Service service= Services.getService(uri);
        if(service==null){
            if(j.core.nvwa.Nvwa.isDebug()){
                log.log("server failed to process request("+dataPackage.getId()+") => service not found => "+uri, -1);
            }
            statusCode=404;
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    j.core.service.Protocol.RESP_SERVICE_NOT_FOUND,
                    null);
            return;
        }

        //请求的方法
        String methodPath=uri;
        if(methodPath.endsWith("/")) methodPath=methodPath.substring(0, methodPath.length()-1);
        if(methodPath.length() > service.getPath().length()){
            methodPath=methodPath.substring(service.getPath().length()+1);
        }

        ServiceMethod serviceMethod=service.getMethod(methodPath);
        if(serviceMethod==null){
            if(j.core.nvwa.Nvwa.isDebug()){
                log.log("server failed to process request("+dataPackage.getId()+") => method config not found => "+methodPath, -1);
            }
            statusCode=404;
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    j.core.service.Protocol.RESP_METHOD_NOT_FOUND,
                    null);
            return;
        }

        //请求信息
        Map<String, String> headers=dataPackage.getHeaders();
        Map<String, String> params=dataPackage.getParams();
        Map<String, DataSource> files=dataPackage.getEntities(DataSourceFile.class.getName());

        DataSourceString payloadEntity=(DataSourceString)dataPackage.getEntity(Protocol.J_PAYLOAD);
        String payload=payloadEntity==null?null:payloadEntity.getSourceString();

        DataSourceObject objectsEntity=(DataSourceObject)dataPackage.getEntity(Protocol.J_OBJECTS);
        Object[] objects=null;
        if(objectsEntity!=null){
            try{
                Object _objects = JSerialization.deSerialize(null, objectsEntity.getSource(), true);
                if(_objects!=null && (_objects instanceof Object[])){
                    objects=(Object[])_objects;
                }
            }catch (Exception e){
                log.log("反序列化出错("+dataPackage.getId()+")（requestLine => "+requestLine+"），classname -> "+ JUtilString.decodeURI(objectsEntity.getClassName(), "UTF-8")+"，length => "+objectsEntity.getSource().length+" => "+new String(objectsEntity.getSource(), StandardCharsets.UTF_8), -1);
                log.log(e, Logger.LEVEL_FATAL);
                this.doRespond(dataPackage.getId(),
                        statusCode,
                        j.core.service.Protocol.RESP_DESERIALIZE_ERROR,
                        null);
                return;
            }
        }

        //服务对象
        ServiceBase serv=j.core.service.server.Server.getObject(service);

        //调用哪个方法
        int callWhichMethod=0;

        Method method=null;

        if(!(headers!=null && headers.isEmpty())
                || (params!=null && !params.isEmpty())
                || (files!=null && !files.isEmpty())
                || !JUtilString.isBlank(payload)){//匹配包含全功能参数列表的方法
            try{
                method = serv.getClass().getDeclaredMethod(serviceMethod.getMethod(), Map.class, Map.class, Map.class, String.class, Object[].class);
                callWhichMethod=1;
            }catch(Exception e){
                //e.printStackTrace();
            }
        }

        if(method==null){
            try{
                method = Methods.matches(serv.getClass(), serviceMethod.getMethod(), objects);
                callWhichMethod=2;
            }catch(Exception e){
                //e.printStackTrace();
            }
        }

        if(method==null){
            statusCode=500;
            if(j.core.nvwa.Nvwa.isDebug()){
                log.log("server failed to process request("+dataPackage.getId()+") => method not found => "+serviceMethod.getMethod(), -1);
            }
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    j.core.service.Protocol.RESP_METHOD_NOT_FOUND,
                    null);
            return;
        }

        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server process request("+dataPackage.getId()+") => invoking method "+method+"("+callWhichMethod+")", -1);
        }

        //调用服务
        Object responseObject=null;
        if(callWhichMethod==1) responseObject = method.invoke(serv, headers, params, files, payload, objects);
        if(callWhichMethod==2) responseObject = method.invoke(serv, objects);

        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server process request("+dataPackage.getId()+") => invoked => "+ JUtilBean.bean2Json(responseObject), -1);
        }

        if(responseObject != null && !(responseObject instanceof ServiceResponse)){
            statusCode=500;
            if(j.core.nvwa.Nvwa.isDebug()){
                log.log("server failed to process request("+dataPackage.getId()+") => invalid response => not instance of ServiceResponse", -1);
            }
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    j.core.service.Protocol.RESP_BAD_RESPONSE,
                    null);

            return;
        }

        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server end process request("+dataPackage.getId()+") => "+requestLine, -1);
        }

        //发送响应
        this.doRespond(dataPackage.getId(),
                statusCode,
                responseCode,
                (ServiceResponse)responseObject);
    }

    /**
     *
     * @param thisRequestId
     * @param statusCode
     * @param responseCode
     * @param response
     * @throws Exception
     */
    public void doRespond(long thisRequestId,
                          int statusCode,
                          String responseCode,
                          ServiceResponse response) throws Exception{
        //响应实体
        Map<String, DataSource> entities=new HashMap<>();

        if(response!=null){
            DataSourceObject _response=new DataSourceObject(this.getDataSourceBlockSize(), "UTF-8");
            _response.setSource(response.getClass().getCanonicalName(), response);
            entities.put(Protocol.J_RESPONSE, _response);
        }

        //发送响应
        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server begin send respone("+thisRequestId+")", -1);
        }

        this.doRespond(thisRequestId,
                statusCode,
                responseCode,
                null,
                null,
                entities);

        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server end send respone("+thisRequestId+")", -1);
        }
    }
}