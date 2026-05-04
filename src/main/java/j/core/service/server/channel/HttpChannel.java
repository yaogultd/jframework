package j.core.service.server.channel;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.lang.Methods;
import j.core.nio.*;
import j.core.serialize.JSerialization;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.core.service.server.config.Service;
import j.core.service.server.config.ServiceMethod;
import j.core.service.server.config.Services;
import j.core.web.handler.JHandler;
import j.core.web.handler.JSession;
import j.log.Logger;
import j.util.JUtilBean;
import j.util.JUtilString;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Getter
@Setter
@Handler(path = "/framework/service/channel/http")
public class HttpChannel extends JHandler implements Channel {
    private static Logger log = Logger.create(HttpChannel.class);
    private static Map<Integer, Object> socketOptions = SocketOptions.cloneDefaults();

    private int listenOn;
    private int maxClients;
    private int maxClientsPerIp;
    private Object[] args;
    private boolean debug;

    public HttpChannel(){

    }

    public HttpChannel(int listenOn,
                       int maxClients,
                       int maxClientsPerIp,
                       Map<Integer, Object> options,
                       Object[] args,
                       boolean debug){
        this.listenOn = listenOn;
        this.maxClients = maxClients;
        this.maxClientsPerIp = maxClientsPerIp;
        this.args = args;
        this.debug = debug;

        socketOptions = options == null || options.isEmpty() ? SocketOptions.cloneDefaults() : options;
    }

    @Override
    public boolean startup() throws Exception {
        return false;
    }

    @Override
    public int getTasksInProccess() {
        return 0;
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

    @Action(path = "request", getRequestBody = Action.GET_REQUEST_BODY.FALSE)
    public void request(JSession jsession, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //收到的请求数据包
        DataPackage dataPackage = DataPackage.receiveFromHttpStream(request);

        //响应数据包
        DataPackage responsePackage = new DataPackage(dataPackage.getId(), 0, getSegmentSize());

        //服务调用结果
        ServiceResponse serviceResponse;

        //服务调用结果封装成DataSource对象
        DataSourceObject responseDataSource=new DataSourceObject(getDataSourceBlockSize(), "UTF-8");

        if(!JUtilString.isBlank(dataPackage.getErrorCode())){
            //调用结果
            serviceResponse = new ServiceResponse(false, dataPackage.getErrorCode(), dataPackage.getErrorMessage());
            serviceResponse.setStatusCode(500);

            responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
            responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

            //发送
            responsePackage.sendToHttpResponse(response);
            return;
        }

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

            //调用结果
            serviceResponse = new ServiceResponse(false, j.core.service.Protocol.RESP_SERVICE_NOT_FOUND, "");
            serviceResponse.setStatusCode(404);

            responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
            responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

            //发送
            responsePackage.sendToHttpResponse(response);
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

            //调用结果
            serviceResponse = new ServiceResponse(false, j.core.service.Protocol.RESP_SERVICE_NOT_FOUND, "");
            serviceResponse.setStatusCode(404);

            responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
            responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

            //发送
            responsePackage.sendToHttpResponse(response);
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

                //调用结果
                serviceResponse = new ServiceResponse(false, j.core.service.Protocol.RESP_DESERIALIZE_ERROR, "");
                serviceResponse.setStatusCode(500);

                responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
                responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

                //发送
                responsePackage.sendToHttpResponse(response);
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
            if(j.core.nvwa.Nvwa.isDebug()){
                log.log("server failed to process request("+dataPackage.getId()+") => method not found => "+serviceMethod.getMethod(), -1);
            }

            //调用结果
            serviceResponse = new ServiceResponse(false, j.core.service.Protocol.RESP_SERVICE_NOT_FOUND, "");
            serviceResponse.setStatusCode(500);

            responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
            responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

            //发送
            responsePackage.sendToHttpResponse(response);
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
            if(j.core.nvwa.Nvwa.isDebug()){
                log.log("server failed to process request("+dataPackage.getId()+") => invalid response => not instance of ServiceResponse", -1);
            }

            //调用结果
            serviceResponse = new ServiceResponse(false, j.core.service.Protocol.RESP_BAD_RESPONSE, "");
            serviceResponse.setStatusCode(500);

            responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
            responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

            //发送
            responsePackage.sendToHttpResponse(response);
            return;
        }

        if(j.core.nvwa.Nvwa.isDebug()){
            log.log("server end process request("+dataPackage.getId()+") => "+requestLine, -1);
        }

        //调用结果
        serviceResponse = (ServiceResponse)responseObject;
        serviceResponse.setStatusCode(200);

        responseDataSource.setSource(response.getClass().getCanonicalName(), serviceResponse);
        responsePackage.addEntity(Protocol.J_RESPONSE, responseDataSource);

        //发送
        responsePackage.sendToHttpResponse(response);
    }
}
