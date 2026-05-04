package j.core.annotation.description;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface MethodDescription {
    String author() default "";
    String date() default "";
    String description();
    String lastModified() default "";
    String lastModifiedBy() default "";
    String[] reviewers() default {""};
    ParameterDescription[] parameters() default {};
}