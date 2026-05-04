package j.core.annotation.description;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.PACKAGE})
public @interface PackageDescription {
    public String author();
    public String date();
    public String description();
    public String lastModified() default "";
    public String lastModifiedBy() default "";
    public String[] reviewers() default {};
}