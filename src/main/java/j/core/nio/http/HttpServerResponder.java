package j.core.nio.http;

import j.core.annotation.description.ClassDescription;
import j.core.hp.thread.ThreadManager;
import j.core.hp.thread.ThreadRunner;
import j.core.hp.thread.ThreadTask;
import j.core.nio.DataPackage;
import j.log.Logger;

@ClassDescription(author = "肖炯",
        date = "2021/10/14",
        description = "响应任务执行")
public class HttpServerResponder extends ThreadTask {
    private static Logger log=Logger.create(HttpServerResponder.class);

    public HttpServerResponder(Object[] in, int retries) {
        super(in, retries);
    }

    public HttpServerResponder(Object[] in, int retries, String uuid) {
        super(in, retries, uuid);
    }

    public HttpServerResponder(Object[] in, int retries, String uuid, long resultTimeout) {
        super(in, retries, uuid, resultTimeout);
    }

    @Override
    public Object[] execute() throws Exception {
        int statusCode=(Integer)this.getIn()[0];
        String responseCode=(String)this.getIn()[1];
        DataPackage dataPackage=(DataPackage)this.getIn()[2];
        HttpServer server=(HttpServer)this.getIn()[3];

        try{
            if(j.core.nvwa.Nvwa.isDebug()){
                if(dataPackage!=null) log.log("server("+server.getUuid()+") responder pool begion executing("+dataPackage.getId()+") => "+dataPackage.getRequestLine(), -1);
            }
            server.doRespond(statusCode, responseCode, dataPackage);
            if(j.core.nvwa.Nvwa.isDebug()){
                if(dataPackage!=null) log.log("server("+server.getUuid()+") responder pool end executing("+dataPackage.getId()+") => "+dataPackage.getRequestLine(), -1);
            }
            if(dataPackage != null) dataPackage.clear();
        }catch (Exception e){
            if(dataPackage != null) dataPackage.clear();
            throw e;
        }

        return new Object[]{"OK"};
    }

    @Override
    public boolean equalz(ThreadTask other) {
        return false;
    }
}
