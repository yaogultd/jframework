package j.core.annotation.nvwa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Nvwa {
    public enum SIGNLETON {TRUE, FALSE};

    //如不指定，则将类名首字母改为小写后作为code（要特别注意类名相同的情况），当注入到某个字段时（使用@Field），该@Field的name也使用该默认值（采用字段名），如果字段名也采用类名首字母小写，则两者都使用默认值的情况下正好对应上）
    public  String code() default "";
    public String name() default "";
    public String proxy() default "";//j.core.nvwa.NvwaProxy的子类
    public SIGNLETON singleton() default SIGNLETON.TRUE;
}
