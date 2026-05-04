package j.core.service.server;

import j.I18N.I18N;
import j.core.annotation.description.ClassDescription;
import j.core.service.ServiceResponse;
import j.core.service.server.config.Service;
import j.core.service.server.config.Services;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.log.Logger;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@ClassDescription(author = "",
        date = "",
        description = "")
public class Router implements Filter {
    //日志输出
    private static Logger log=Logger.create(Router.class);

    /**
     *
     *
     */
    public Router() {
        super();
    }

    /*
     *  (non-Javadoc)
     * @see jakarta.servlet.Filter#destroy()
     */
    public void destroy() {

    }

    /*
     *  (non-Javadoc)
     * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {

    }

    /*
     *  (non-Javadoc)
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest=(HttpServletRequest)request;
        HttpServletResponse httpResponse=(HttpServletResponse)response;
        String requestURI=httpRequest.getRequestURI();

        //根据访问路径查找对应的服务
        Service service = Services.getService(requestURI);

        //如果查找到了服务
        if(service!=null){
            try {
                //请求
                ServiceResponse responseObject = j.core.service.ServiceAdapter.call(requestURI, httpRequest);
                if(responseObject == null) {
                    httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }

                //响应
                if(I18N.enabled) SysUtil.outHttpResponse(httpResponse, I18N.convert(responseObject.toString(), I18N.getCurrentLanguage(httpRequest)));
                else SysUtil.outHttpResponse(httpResponse, responseObject.toString());

                return;
            }catch(Exception e){
                log.log(e, Logger.LEVEL_ERROR);
            }
        }

        try{
            chain.doFilter(request,response);
        }catch(Exception e){
            log.log("errors occur on service:"+SysUtil.getRequestURL(httpRequest),Logger.LEVEL_ERROR);
            log.log(e,Logger.LEVEL_ERROR);
            SysUtil.redirect(httpRequest,httpResponse, SysConfig.errorPage);
        }
    }
}
