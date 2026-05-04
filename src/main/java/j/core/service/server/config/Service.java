package j.core.service.server.config;

import j.core.annotation.description.ClassDescription;
import j.core.service.registry.Registry;
import j.core.service.server.Server;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilJSON;
import org.json.JSONArray;
import org.json.JSONObject;

@ClassDescription(author = "肖炯",
        date = "2021-08-08",
        description = "本地服务配置信息")
public class Service {
    //调用路径
    private String path;

    //版本（旧版为本地服务）
    private String version;

    //实现类名
    private String clazz;

    //服务接口（方法），key为方法调用路径
    private ConcurrentMap<String, ServiceMethod> methods = new ConcurrentMap<>();

    //在哪些断开提供服务
    private ConcurrentList<Integer> ports = new ConcurrentList<>();

    //各端口上正在处理的任务数
    private ConcurrentMap<Integer, Integer> packages = new ConcurrentMap<>();

    //运行时UUID
    private String runUuid="";

    /**
     *
     * @param path
     * @param clazz
     */
    public Service(String path, String clazz){
        this.setPath(path);
        this.setClazz(clazz);
    }

    /**
     *
     * @param path
     */
    public void setPath(String path){
        if(!path.startsWith("/")) path="/"+path;
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
     * @param version
     */
    public void setVersion(String version){
        this.version=version;
    }

    /**
     *
     * @return
     */
    public String getVersion(){
        return this.version;
    }

    /**
     *
     * @param clazz
     */
    public void setClazz(String clazz){
        this.clazz=clazz;
    }

    /**
     *
     * @return
     */
    public String getClazz(){
        return this.clazz;
    }

    /**
     *
     * @param method
     */
    public void addMethod(ServiceMethod method){
        methods.put(method.getPath(), method);
    }

    /**
     *
     * @param path 方法子路径（不包括Service的path）
     * @return
     */
    public ServiceMethod getMethod(String path){
        return methods.get(path);
    }

    /**
     *
     * @param port
     */
    public void addPort(Integer port){
        if(!ports.contains(port)) ports.add(port);
    }

    /**
     *
     * @return
     */
    public ConcurrentList<Integer> getPorts(){
        return this.ports;
    }

    /**
     *
     * @param port
     * @param packages
     */
    public void setPackages(Integer port, Integer packages){
        this.packages.put(port, packages);
    }

    /**
     *
     * @param port
     * @return
     */
    public Integer getPackages(Integer port){
        return this.packages.get(port);
    }

    /**
     *
     * @return
     */
    public Integer getPackages(){
        int packages=0;
        for(int i=0; i<ports.size(); i++){
            packages+=getPackages(ports.get(i));
        }
        return packages;
    }

    /**
     *
     * @param runUuid
     */
    public void setRunUuid(String runUuid){
        this.runUuid=runUuid;
    }

    /**
     *
     * @return
     */
    public String getRunUuid(){
        return this.runUuid;
    }

    /**
     *
     * @param requestURI 服务的完整访问路径
     * @return
     */
    public ServiceMethod findMethod(String requestURI){
        if(requestURI.endsWith("/")) requestURI=requestURI.substring(0, requestURI.length()-1);
        if(requestURI.length()>this.path.length()){
            requestURI=requestURI.substring(this.path.length()+1);
        }
        return getMethod(requestURI);
    }

    @Override
    public String toString(){
        StringBuffer json=new StringBuffer();
        json.append("{\"path\":\""+this.path+"\"");
        json.append(",\"version\":\""+this.version+"\"");
        json.append(",\"clazz\":\""+this.clazz+"\"");
        json.append(",\"methods\":[");

        ConcurrentList<ServiceMethod> _methods=methods.listValues();
        for(int i=0; i<_methods.size(); i++){
            ServiceMethod method=_methods.get(i);
            if(i>0) json.append(",");
            json.append(method.toString());
        }

        json.append("]");
        json.append(",\"ports\":[");
        for(int i=0; i<ports.size(); i++){
            if(i>0) json.append(",");
            json.append(ports.get(i).toString());
        }
        json.append("]");

        json.append(",\"packages\":[");
        for(int i=0; i<ports.size(); i++){
            if(i>0) json.append(",");
            json.append(Server.getPackages(ports.get(i)).toString());
        }
        json.append("]");
        json.append(",\""+ Registry.FLAG_RUN_UUID+"\":\""+ this.getRunUuid() +"\"");
        json.append("}");
        return json.toString();
    }

    /**
     * d
     * @param json
     * @return
     */
    public static Service fromJson(JSONObject json){
        Service service=new Service(JUtilJSON.string(json, "path"), JUtilJSON.string(json, "clazz"));
        service.setVersion(JUtilJSON.string(json, "version"));

        JSONArray methodsJson=JUtilJSON.array(json,"methods");
        for(int i=0; methodsJson!=null && i<methodsJson.length(); i++){
            JSONObject methodJson=JUtilJSON.get(methodsJson, i);
            ServiceMethod method=ServiceMethod.fromJson(methodJson);
            service.addMethod(method);
        }

        JSONArray portsJson=JUtilJSON.array(json,"ports");
        for(int i=0; portsJson!=null && i<portsJson.length(); i++){
            service.addPort(portsJson.getInt(i));
        }

        if(portsJson!=null){
            JSONArray queuesJson=JUtilJSON.array(json,"packages");
            for(int i=0; queuesJson!=null && i<queuesJson.length() && i<portsJson.length(); i++){
                service.setPackages(portsJson.getInt(i), queuesJson.getInt(i));
            }
        }

        service.setRunUuid(JUtilJSON.string(json, Registry.FLAG_RUN_UUID));

        return service;
    }



    /**
     * 类名或路径是否与该服务匹配
     * @param pathOrClassName
     * @return
     */
    public boolean matches(String pathOrClassName){
        if(pathOrClassName.equals(this.getClazz())) return true;

        if(pathOrClassName.endsWith("/")) pathOrClassName=pathOrClassName.substring(0, pathOrClassName.length()-1);

        if(pathOrClassName.equals(this.getPath())) return true;

        while(pathOrClassName.lastIndexOf("/")>1){
            pathOrClassName=pathOrClassName.substring(0, pathOrClassName.lastIndexOf("/"));

            if(pathOrClassName.equals(this.getPath())) return true;
        }

        return pathOrClassName.equals(this.getPath());
    }
}
