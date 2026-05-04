package j.core.annotation.action;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(Navigates.class)
public @interface Navigate {
    public String belonged() default "";
    public String condition();
    public String type() default "forward";
    public String url();
}
