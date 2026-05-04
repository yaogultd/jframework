package j.core.web.handler;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
public class JMethod {
    private Method method;
    private boolean deprecated;
    private boolean with4Paramaters;

    public JMethod(Method method, boolean deprecated, boolean with4Paramaters){
        this.method=method;
        this.deprecated=deprecated;
        this.with4Paramaters=with4Paramaters;
    }
}
