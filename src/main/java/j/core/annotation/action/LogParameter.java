package j.core.annotation.action;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(LogParameters.class)
public @interface LogParameter {
    public String name();
}
