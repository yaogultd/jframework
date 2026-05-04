package j.core.service.client;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.nio.DataSourceFile;
import j.core.nvwa.NvwaProxy;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.log.Logger;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/11/25",
        description = "执行远程调用")
public class Proxy<T> extends NvwaProxy<T> {
    private static Logger log=Logger.create(Proxy.class);

    @Override
    @MethodDescription(author = "肖炯",
            date = "2021/11/25",
            description = "执行远程调用")
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        try{
            //调用前
            this.beforeInvoke(method, args);

            Map<String, String> headers=null;
            Map<String, String> params=null;
            Map<String, DataSourceFile> files=null;
            String payload=null;
            Object[] objects=null;

            boolean definedFullParamTypes=false;
            if(args != null && args.length==5){
                definedFullParamTypes=true;
                if(args[0] != null && !(args[0] instanceof Map)) definedFullParamTypes=false;
                if(args[1] != null && !(args[1] instanceof Map)) definedFullParamTypes=false;
                if(args[2] != null && !(args[2] instanceof Map)) definedFullParamTypes=false;
                if(args[3] != null && !(args[3] instanceof String)) definedFullParamTypes=false;
                if(args[4] != null && !(args[4] instanceof Object[])) definedFullParamTypes=false;
            }

            if(definedFullParamTypes){
                if(args[0] != null && (args[0] instanceof Map)) headers=(Map)args[0];
                if(args[1] != null && (args[1] instanceof Map)) params=(Map)args[1];
                if(args[2] != null && (args[2] instanceof Map)) files=(Map)args[2];
                if(args[3] != null && (args[3] instanceof String)) payload=(String)args[3];
                if(args[4] != null && (args[4] instanceof Object[])) objects=(Object[])args[4];
            }else{
                objects=args;
            }

            //签名
            //TODO
            /*if(headers==null) headers = new HashMap();
            if(!headers.containsKey(Constants.ACCESS_KEY)){
                String signature = Signature.sign("TODO",
                        Nvwa.getParameter(Registry.class, Constants.ACCESS_SECRET));

                //请求头
                headers.put(Constants.ACCESS_KEY, Nvwa.getParameter(Registry.class, Constants.ACCESS_KEY));
                headers.put(Constants.SIGNATURE, signature);
            }*/

            //调用
            ServiceBase service=(ServiceBase)o;
            ServiceResponse response=Client.call(service,
                    method.getName(),
                    headers,
                    params,
                    files,
                    payload,
                    objects);

            //调用后
            this.afterInvoke(method, args, response);

            return response;
        }catch(Exception e){
            this.onException(method, args, e);
            throw e;
        }
    }

    @Override
    protected Object beforeInvoke(Method method, Object[] args) throws Exception{
        //System.out.println("call beforeInvoke......");
        return null;
    }

    @Override
    protected Object afterInvoke(Method method, Object[] args, Object returnValue) throws Exception{
        //System.out.println("call afterInvoke......");
        return null;
    }

    @Override
    protected Object onException(Method method, Object[] args, Throwable e) throws Exception{
        //System.out.println("call afterInvoke......");
        return null;
    }
}
