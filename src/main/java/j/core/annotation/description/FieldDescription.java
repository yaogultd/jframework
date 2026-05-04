package j.core.annotation.description;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
public @interface FieldDescription {
    public String author() default "";
    public String date() default "";
    public String description();
    public String lastModified() default "";
    public String lastModifiedBy() default "";
    public String[] reviewers() default {""};
}