package j.core.service;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.nio.DataSourceFile;
import j.core.service.registry.Registration;
import j.core.service.server.Server;
import j.log.Logger;
import j.util.ConcurrentList;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/07/14",
        description = "",
        reviewers = {})
public class ServiceAdapter{
    private static Logger log=Logger.create(ServiceAdapter.class);

    @MethodDescription(description = "通信层")
    private static String getNetworker(){
        return j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "networker");
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据服务类名获得远程对象，完全当做本地对象调用继续（应用无需关心底层细节）")
    public static ConcurrentList<Registration> findRegistration(String pathOrClassName){
        return j.core.service.client.Client.findRegistration(pathOrClassName);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据服务类名获得远程对象，完全当做本地对象调用继续（应用无需关心底层细节）")
    public static ServiceBase getService(Class c) throws Exception{
        return j.core.service.client.Client.getService(c);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据服务调用路径获得远程对象，完全当做本地对象调用继续（应用无需关心底层细节）")
    public static ServiceBase getService(String pathOrClassName) throws Exception{
        return j.core.service.client.Client.getService(pathOrClassName);
    }

    /**
     * 根据服务类名获得远程对象，如未获取到则每间隔1秒重试，直至超时
     * @param c
     * @param timeoutSecs 超时时间（秒）
     * @return
     * @throws Exception
     */
    public static ServiceBase waitService(Class c, int timeoutSecs) throws Exception{
        return j.core.service.client.Client.waitService(c.getCanonicalName(), timeoutSecs);
    }

    /**
     * 根据服务调用路径获得远程对象，如未获取到则每间隔1秒重试，直至超时
     * @param pathOrClassName
     * @param timeoutSecs 超时时间（秒）
     * @return
     * @throws Exception
     */
    public static ServiceBase waitService(String pathOrClassName, int timeoutSecs) throws Exception{
        return j.core.service.client.Client.waitService(pathOrClassName, timeoutSecs);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据http请求调用服务")
    public static ServiceResponse call(String path, HttpServletRequest request) throws Exception{
        return j.core.service.client.Client.call(path, request);
    }

    @MethodDescription(author = "肖炯",
            date = "2021/07/14",
            description = "根据路径调用服务")
    public static ServiceResponse call(String path, String payload) throws Exception{
        return j.core.service.client.Client.call(path, payload);
    }

    /**
     *
     * @param path
     * @param objects objects数组的长度与调用的服务方法的参数个数必须一致
     * @return
     * @throws Exception
     */
    public static ServiceResponse call(String path, Object[] objects) throws Exception{
        return j.core.service.client.Client.call(path, objects);
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
        return j.core.service.client.Client.call(path,
                headers,
                params,
                files,
                payload,
                objects);
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
        return j.core.service.client.Client.call(service,
                method,
                headers,
                params,
                files,
                payload,
                objects);
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
        return j.core.service.client.Client.call(reg,
                method,
                headers,
                params,
                files,
                payload,
                objects);
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
        return j.core.service.client.Client.callAll(asyn,
                excludeLocalNode,
                service,
                method,
                headers,
                params,
                files,
                payload,
                objects);
    }
}
