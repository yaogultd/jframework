package j.core.service.server.config;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.common.Global;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceClass;
import j.core.permission.Permission;
import j.core.permission.ResourceAction;
import j.core.service.server.Server;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilJSON;
import j.util.JUtilString;
import org.json.JSONArray;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "肖炯",
        date = "2021-08-08",
        description = "本地服务配置信息")
public class  Services implements Consumer {
    private static Logger log=Logger.create(Services.class);//日志输出

    @FieldDescription(description = "服务，key为服务路径或实现类名")
    private static ConcurrentMap<String, Service> services = new ConcurrentMap<>();

    /**
     * 是否优先调用本地服务节点（同jvm中的），默认为true
     * @return
     */
    public static boolean localServiceFirst(){
        return !"false".equalsIgnoreCase(j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "localServiceFirst"));
    }

    /**
     *
     * @param pathOrClassName
     * @param service
     */
    public static void addService(String pathOrClassName, Service service){
        services.put(pathOrClassName, service);
    }

    /**
     * 根据路径获或实现类名取服务对象
     * @param pathOrClassName
     * @return
     */
    public static Service getService(String pathOrClassName){
        if(pathOrClassName.endsWith("/")) pathOrClassName=pathOrClassName.substring(0, pathOrClassName.length()-1);

        Service service=services.get(pathOrClassName);
        if(service!=null) return service;

        while(pathOrClassName.lastIndexOf("/")>1){
            pathOrClassName=pathOrClassName.substring(0, pathOrClassName.lastIndexOf("/"));
            service=services.get(pathOrClassName);

            if(service!=null) return service;
        }

        return services.get(pathOrClassName);
    }

    /**
     *
     * @param resource
     * @return
     */
    private static boolean load(Resource resource){
        if(resource==null) return false;//null

        try{
            if(resource instanceof ResourceClass){//类文件
                ResourceClass _resource=(ResourceClass)resource;

                j.core.annotation.service.Service handlerAno=(j.core.annotation.service.Service)_resource.getResource().getAnnotation(j.core.annotation.service.Service.class);

                //不是Handler或未正取配置
                if(handlerAno==null || handlerAno.path()==null || "".equals(handlerAno.path())) return false;

                //保存Handler（与xml配置兼容）
                Service handler= new Service(handlerAno.path(), _resource.getResource().getCanonicalName());
                handler.setVersion(handlerAno.version());

                //扫描权限注解
                j.core.annotation.auth.Authority handlerAuth=(j.core.annotation.auth.Authority)_resource.getResource().getAnnotation(j.core.annotation.auth.Authority.class);
                if(handlerAuth!=null){
                    ResourceAction r=new ResourceAction();
                    r.setPolicy(handlerAuth.policy());
                    r.setRoles(handlerAuth.roles());
                    r.setNoPermissionPage(handlerAuth.noPermissionPage());
                    r.setLoginPage(handlerAuth.loginPage());

                    r.setPath(handler.getPath());
                    r.setActionId("");

                    Permission.addExtDefinedResources(handler.getPath(), r);

                    log.log("通过Service类注解设置权限 -> "+r.toString(),-1);
                }

                System.out.println("扫描到Service类注解 -> "+handler.getPath()+" -> "+_resource.getPath()+", authority -> "+handlerAuth);

                //获取action配置
                Method[] methods=_resource.getResource().getDeclaredMethods();
                for(int i=0; methods!=null && i<methods.length; i++){
                    j.core.annotation.service.Service actionAno=methods[i].getAnnotation(j.core.annotation.service.Service.class);

                    //不是action
                    if(actionAno==null || actionAno.path()==null || "".equals(actionAno.path())) continue;

                    ServiceMethod action=new ServiceMethod(actionAno.path(),
                            methods[i].getName(),
                            actionAno.timeout());

                    //扫描权限注解
                    j.core.annotation.auth.Authority actionAuth=methods[i].getAnnotation(j.core.annotation.auth.Authority.class);
                    if(actionAuth!=null){
                        ResourceAction r=new ResourceAction();
                        r.setPolicy(actionAuth.policy());
                        r.setRoles(actionAuth.roles());
                        r.setNoPermissionPage(actionAuth.noPermissionPage());
                        r.setLoginPage(actionAuth.loginPage());

                        r.setPath(handler.getPath());
                        r.setActionId(action.getPath());

                        Permission.addExtDefinedResources(handler.getPath()+"->"+action.getPath(), r);

                        log.log("通过Service方法注解设置权限 -> "+r.toString(),-1);
                    }
                    System.out.println("扫描到Service方法注解 -> "+handler.getPath()+" -> "+action.getPath()+" -> "+methods[i].getName()+", authority -> "+actionAuth);

                    handler.addMethod(action);
                }

                addService(handler.getPath(), handler);
                addService(handler.getClazz(), handler);
            }

            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_FATAL);
            return false;
        }
    }

    /**
     *
     * @param port
     */
    public static void addPort(Integer port){
        ConcurrentList<Service> _services=services.listValues();
        for(int i=0; i<_services.size(); i++){
            Service service=_services.get(i);
            service.addPort(port);
        }
    }


    @Override
    public boolean onFound(Resource resource) {
        //不是xml资源不予加载
        if(!(resource instanceof ResourceClass)) return false;

        return load(resource);
    }

    @Override
    public boolean onUpdate(Resource resource) {
        return false;
    }

    /**
     *
     * @param all 是否包含全部服务（true表示忽略“包含的服务”、“排除的服务”配置）
     * @return 本地服务配置信息（即向注册中心注册服务时推送的信息）
     */
    public static String toJson(boolean all){
        StringBuffer json=new StringBuffer();
        json.append("[");

        int index=0;
        ConcurrentList<Service> _services=services.listValues();
        for(int i=0; i<_services.size(); i++){
            Service service=_services.get(i);
            service.setRunUuid(Global.getRunUuid());

            //不是“包含全部服务”，且不是设置为启用的服务
            if(!all && !on(service)) continue;

            if(index>0) json.append(",");
            json.append(service);
            index++;
        }

        json.append("]");

        return json.toString();
    }

    /**
     * 是否有需要启动的服务
     * @return
     */
    public static boolean hasServicesTobeStarted(){
        String[] _includes=null;
        String includes = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "includes");
        if(!JUtilString.isBlank(includes)) _includes=includes.split(",");

        String[] _excludes=null;
        String excludes = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "excludes");
        if(!JUtilString.isBlank(excludes)) _excludes=excludes.split(",");

        ConcurrentList<Service> _services=services.listValues();
        for(int i=0; i<_services.size(); i++){
            Service service=_services.get(i);
            //不是“包含全部服务”，且不是设置为启用的服务
            if(on(service)) return true;
        }
        return false;
    }

    /**
     *
     * @param service
     * @return
     */
    public static boolean on(Service service){
        String[] _includes=Server.getIncludes();
        String[] _excludes=Server.getExcludes();

        boolean in=false;//是否包括
        if(_includes==null || _includes.length==0){
            in=true;
        }else{
            for(int i=0; i<_includes.length; i++){
                if(JUtilString.isBlank(_includes[i])) continue;

                if(_includes[i].indexOf("*")>-1){
                    if(JUtilString.match(service.getClazz(), _includes[i], "*")>-1
                        ||JUtilString.match(service.getPath(), _includes[i], "*")>-1){
                        in=true;
                        break;
                    }
                }else{
                    if(_includes[i].equals(service.getClazz())
                            ||_includes[i].equals(service.getPath())){
                        in=true;
                        break;
                    }
                }
            }
        }

        //如果未设置排除的服务
        if(_excludes==null || _excludes.length==0) return in;

        boolean ex=false;//是否排除
        for(int i=0; i<_excludes.length; i++){
            if(JUtilString.isBlank(_excludes[i])) continue;

            if(_excludes[i].indexOf("*")>-1){
                if(JUtilString.match(service.getClazz(), _excludes[i], "*")>-1
                        || JUtilString.match(service.getPath(), _excludes[i], "*")>-1){
                    ex=true;
                    break;
                }
            }else{
                if(_excludes[i].equals(service.getClazz())
                        ||_excludes[i].equals(service.getPath())){
                    ex=true;
                    break;
                }
            }
        }

        return in && (!ex);
    }

    /**
     *
     * @param array
     * @return
     */
    public static List<Service> fromJson(JSONArray array){
        if(array==null || array.length()==0) return null;
        List<Service> _services=new ArrayList<>();
        for(int i=0; i<array.length(); i++){
            _services.add(Service.fromJson(JUtilJSON.get(array, i)));
        }
        return _services;
    }
}