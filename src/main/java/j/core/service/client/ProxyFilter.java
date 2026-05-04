package j.core.service.client;

import j.core.annotation.description.ClassDescription;
import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

@ClassDescription(author = "肖炯",
        date = "2021/11/25",
        description = "决定那些方法需要拦截")
public class ProxyFilter implements CallbackFilter {
    private static ProxyFilter filter=new ProxyFilter();

    /**
     *
     * @return
     */
    public static ProxyFilter getInstance(){
        return filter;
    }

    @Override
    public int accept(Method method) {
        if("getConfig".equals(method.getName())
                ||"setConfig".equals(method.getName())
                ||"run".equals(method.getName())
                ||"toString".equals(method.getName())
                ||"equals".equals(method.getName())
                ||"hashCode".equals(method.getName())){
            return 1;
        }else{
            return 0;
        }
    }
}
