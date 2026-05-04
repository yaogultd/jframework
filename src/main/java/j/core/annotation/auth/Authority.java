package j.core.annotation.auth;

import j.core.annotation.action.LogParameters;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(Authorities.class)
public @interface Authority {
    public String belonged() default "";
    public String policy() default "role";
    public String[] roles() default {};
    public String noPermissionPage() default "/sso/permission_denied";
    public String loginPage() default "/sso/login";
}
