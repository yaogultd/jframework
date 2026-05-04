package j.core.service.server.config;

import j.core.annotation.description.ClassDescription;
import j.util.JUtilJSON;
import org.json.JSONObject;

@ClassDescription(author = "肖炯",
        date = "2021-08-08",
        description = "本地服务配置信息")
public class ServiceMethod {
    private String path;
    private String method;
    private long timeout;

    /**
     *
     * @param path
     * @param method
     * @param timeout
     */
    public ServiceMethod(String path, String method, long timeout){
        this.setPath(path);
        this.setMethod(method);
        this.timeout=timeout;
        if(this.timeout<=0) this.timeout=60000;
    }

    /**
     *
     * @param path
     */
    public void setPath(String path){
        if(path.startsWith("/")) path=path.substring(1);
        if(path.endsWith("/")) path=path.substring(0, path.length()-1);
        this.path=path;
    }

    /**
     *
     * @return
     */
    public String getPath(){
        return this.path;
    }

    /**
     *
     * @param method
     */
    public void setMethod(String method){
        this.method=method;
    }

    /**
     *
     * @return
     */
    public String getMethod(){
        return this.method;
    }

    /**
     *
     * @return
     */
    public long getTimeout(){
        return this.timeout;
    }

    @Override
    public String toString(){
        StringBuffer json=new StringBuffer();
        json.append("{\"path\":\""+this.path+"\"");
        json.append(",\"method\":\""+this.method+"\"");
        json.append(",\"timeout\":"+this.timeout+"}");
        return json.toString();
    }

    /**
     *
     * @param json
     * @return
     */
    public static ServiceMethod fromJson(JSONObject json){
        return new ServiceMethod(JUtilJSON.string(json, "path"),
                JUtilJSON.string(json, "method"),
                JUtilJSON.getLong(json, "timeout"));
    }
}
