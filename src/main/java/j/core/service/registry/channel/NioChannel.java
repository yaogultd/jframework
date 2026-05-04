package j.core.service.registry.channel;

import j.core.nio.*;
import j.core.nio.http.HttpServer;
import j.core.permission.Signature;
import j.core.service.registry.Registry;
import j.core.web.Constants;
import j.log.Logger;
import j.util.JUtilJSON;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

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

    private Server server;

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
        server = Server.start(listenOn,
                NioChannel.class,
                maxClients,
                maxClientsPerIp,
                options,
                null);
        server.setDebug(debug);

        return true;
    }

    @Override
    public void doRespond(int statusCode, String responseCode, DataPackage dataPackage) throws Exception{
        statusCode=200;

        //响应
        StringBuffer sResp=new StringBuffer();
        sResp.append("{");

        //验签
        //accessKey
        String accessKey=dataPackage.getHeader(Constants.ACCESS_KEY);

        //配置的accessKey
        String accessKeySet=j.core.nvwa.Nvwa.getParameter(Registry.class, Constants.ACCESS_KEY);

        //accessKey无效
        if(JUtilString.isBlank(accessKey)
                || !accessKey.equals(accessKeySet)){
            sResp.append("\""+Registry.FLAG_RESULT+"\":\"");
            sResp.append(Registry.RESP_INVALID_ACCESS_KEY);
            sResp.append("\"");
            sResp.append("}");

            //发送响应
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    responseCode,
                    sResp.toString());

            return;
        }

        String command="";//请求命令

        String requestLine=dataPackage.getRequestLine();
        if(requestLine.startsWith("POST ")) command=requestLine.substring(5);

        //服务配置信息
        DataSourceString entityService=(DataSourceString)dataPackage.getEntity(Registry.FLAG_SERVICES);

        //json格式的服务配置信息
        String jsonServiceString=entityService==null?null:entityService.getSourceString();

        //配置的accessSecret
        String accessSecretSet=j.core.nvwa.Nvwa.getParameter(Registry.class, Constants.ACCESS_SECRET);

        //签名
        String signature=dataPackage.getHeader(Constants.SIGNATURE);

        //验证签名
        //期待的正确签名
        //log.log("jsonServiceString = "+jsonServiceString, -1);
        String signatureExpected = Signature.sign(JUtilString.appendStringsIgnoreNulls(command, jsonServiceString), accessSecretSet);

        //签名错误
        if(!JUtilString.equals(signature, signatureExpected)){
            sResp.append("\""+Registry.FLAG_RESULT+"\":\"");
            sResp.append(Registry.RESP_INVALID_SIGN);
            sResp.append("\"");
            sResp.append("}");

            //发送响应
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    responseCode,
                    sResp.toString());

            return;
        }
        //验签 end

        //解析服务信息
        //服务信息格式应该如下：
        //{"services":$Services.toString()}
        JSONObject jsonServices= JUtilJSON.parse(jsonServiceString);

        //服务配置信息为null
        if(!Registry.COMMAND_QUERY.equals(command)
                && (jsonServices==null || jsonServices.keySet().isEmpty())){
            sResp.append("\""+Registry.FLAG_RESULT+"\":\"");
            sResp.append(Registry.RESP_INVALID_SERVICE);
            sResp.append("\"");
            sResp.append("}");

            //发送响应
            this.doRespond(dataPackage.getId(),
                    statusCode,
                    responseCode,
                    sResp.toString());

            return;
        }
        //服务配置信息为null end

        //解析请求并做响应处理
        String message="";
        String result="";

        sResp.append("\""+Registry.FLAG_COMMAND+"\":\""+command+"\"");

        if(Registry.COMMAND_REG.equalsIgnoreCase(command)){//注册服务
            result=Registry.doReg(dataPackage, jsonServices);
        }else if(Registry.COMMAND_DEL.equalsIgnoreCase(command)){//删除服务
            result=Registry.doDel(dataPackage, jsonServices);
        }else if(Registry.COMMAND_HEARTBEAT.equalsIgnoreCase(command)){//心跳
            result=Registry.doHeartbeat(dataPackage, jsonServices);
        }else if(Registry.COMMAND_PAUSE.equalsIgnoreCase(command)){//暂停服务
            result=Registry.doPause(dataPackage, jsonServices);
        }else if(Registry.COMMAND_RESUME.equalsIgnoreCase(command)){//恢复服务
            result=Registry.doResume(dataPackage, jsonServices);
        }else if(Registry.COMMAND_QUERY.equalsIgnoreCase(command)){//查询服务
            result=Registry.doQuery(dataPackage.getParam(Registry.FLAG_PATH));
        }else{//无效命令
            result=Registry.RESP_INVALID_COMMAND;
        }

        if (JUtilJSON.isJson(result) != null){
            sResp.append(",\""+Registry.FLAG_RESULT+"\":");
            sResp.append(result);
        }else{
            sResp.append(",\""+Registry.FLAG_RESULT+"\":\"");
            sResp.append(JUtilJSON.convertChars(result));
            sResp.append("\"");
        }

        sResp.append(",\""+Registry.FLAG_MESSAGE+"\":\""+ JUtilJSON.convertChars(message)+"\"");
        //解析请求并做响应处理 end

        sResp.append("}");

        //发送响应
        //log.log("try send response to "+command+" of id "+dataPackage.getId()+" -> "+sResp, -1);

        this.doRespond(dataPackage.getId(),
                statusCode,
                responseCode,
                sResp.toString());
    }

    /**
     *
     * @param thisRequestId
     * @param statusCode
     * @param responseCode
     * @param sResp
     * @throws Exception
     */
    protected void doRespond(long thisRequestId,
                             int statusCode,
                             String responseCode,
                             String sResp) throws Exception{
        //响应实体
        Map<String, DataSource> entities=new HashMap<>();

        DataSourceString resp=new DataSourceString(this.getDataSourceBlockSize(), "UTF-8");
        resp.setSource(sResp);
        entities.put(Registry.FLAG_RESP, resp);

        //设置签名
        Map<String, String> headers=new HashMap<>();

        //配置的accessSecret
        String accessSecretSet=j.core.nvwa.Nvwa.getParameter(Registry.class, Constants.ACCESS_SECRET);

        //验证签名
        //期待的正确签名
        String signature= Signature.sign(JUtilString.appendStringsIgnoreNulls(sResp), accessSecretSet);

        headers.put(Constants.SIGNATURE, signature);
        //设置签名 end

        //发送响应
        this.doRespond(thisRequestId,
                statusCode,
                responseCode,
                headers,
                null,
                entities);
    }
}