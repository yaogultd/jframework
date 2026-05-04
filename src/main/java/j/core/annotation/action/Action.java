package j.core.annotation.action;

import com.beust.jcommander.IDefaultProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Action {
    public enum GET_REQUEST_BODY {TRUE, FALSE};
    public enum LOG_ENABLED {TRUE, FALSE, INHERITED};
    public String belonged() default "";
    public String path() default "";
    public boolean pathExclusive() default false;
    public String name() default "";
    public String validator() default "";//请求数据格式校验实现类（j.core.web.JValidator的子类）
    public GET_REQUEST_BODY getRequestBody() default GET_REQUEST_BODY.TRUE;
    public LOG_ENABLED logEnabled() default LOG_ENABLED.INHERITED;
}