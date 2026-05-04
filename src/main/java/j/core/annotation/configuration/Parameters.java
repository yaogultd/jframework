package j.core.annotation.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动读取指定para*.xml文件中的配置(AppConfig维护的配置)；
 * 对象通过Nvwa创建后，AppConfig中值的更新不会更新到与其关联的Field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Parameters {
    public String group();
}