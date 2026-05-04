package j.core.webserver.undertow.servlet;

import j.core.annotation.description.ClassDescription;
import j.core.web.handler.Initializers;

import jakarta.servlet.*;
import java.util.Set;

@ClassDescription(author = "肖炯",
        date = "2021/11/15",
        description = "web容器初始化")
public class WebContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        //初始化servlet
        ServletRegistration.Dynamic dynamic = servletContext.addServlet(Initializers.class.getSimpleName(), Initializers.class);
        dynamic.setLoadOnStartup(1);
        dynamic.setAsyncSupported(true);
    }
}
