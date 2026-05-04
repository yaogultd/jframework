package j.core.annotation.nvwa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *注入bean或初始化基础类型的值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Field {
    //FieldType:与字段类型一致（基本java类型）
    //Ref:引用另一个Nvwa对象的code
    //Class:指定对象类名（initValue指定为该类名），提供无参数的构造函数
    //
    public enum TYPE {FieldType, Ref, Class, FromString};
    public String name() default "";//如不指定，则使用字段名
    public TYPE type() default TYPE.FieldType;
    public String initValue() default "NVWA_IS_NULL";//如果是Ref类型，则该值为所引用对象的编码（@Nvwa的code）
}
