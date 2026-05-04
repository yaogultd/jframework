package j.core.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动读取指定properties文件中对应的配置项
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Property {
    public String path() default "";
    public String name() default "";
    public String defaultValue() default "NVWA_IS_NULL";
    public String description() default "";
}
