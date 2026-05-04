package j.core.web.handler;

import j.I18N.I18N;
import j.core.common.JProperties;
import j.core.nvwa.Nvwa;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;


@WebFilter(urlPatterns = {"*"}, asyncSupported = true)
public class Router implements Filter{
	private static Logger log=Logger.create(Router.class);
	private static ConcurrentMap<String, Boolean> jspExists = new ConcurrentMap<>();

	/**
	 *
	 * @param uri
	 * @return
	 */
	public static boolean jspExists(String uri){
		if(!uri.endsWith(".jsp")) return true;
		if(jspExists.containsKey(uri)) return jspExists.get(uri);
		jspExists.put(uri, (new File(JUtilString.appendPath(JProperties.getJspPath(), uri))).exists());
		return jspExists.get(uri);
	}

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
		while(!Nvwa.isScanned()){
			log.log("等待Nvwa完成资源扫描......", -1);
			try{
				Thread.sleep(5000);
			}catch(Exception e){}
		}

		HttpServletRequest httpRequest=(HttpServletRequest)request;
		HttpServletResponse httpResponse=(HttpServletResponse)response;
		String requestURI=httpRequest.getRequestURI();

		//JSP支持
		if(requestURI.endsWith(".jhtml")){
			requestURI=requestURI.replaceAll(".jhtml",".jsp");
			if(!jspExists(requestURI)){
				httpResponse.sendError(404);
				return;
			}
			//log.log("forward 2 jsp => "+requestURI+" ... ", -1);
			String forwardUrl=JUtilString.appendUrl("/WEB-INF/pages", requestURI);
			if(!I18N.enabled||!I18N.need(httpRequest)) SysUtil.forward(httpRequest, httpResponse, forwardUrl);
			else SysUtil.forwardI18N(httpRequest, httpResponse, forwardUrl);
			return;
		}
		
		Handler handler=null;
		String pattern=Handlers.isActionPath(requestURI);

		if(pattern!=null){
			if(requestURI.endsWith(pattern)){//常规方式
				handler=Handlers.getHandler(requestURI.substring(0,requestURI.lastIndexOf(pattern)));
			}else{//RESTful方式
				handler=Handlers.getHandlerByRESTPath(requestURI);
			}
		}

		if(handler!=null){
			Server.service(handler,httpRequest,httpResponse);
			return;
		}

		try{
			chain.doFilter(request,response);
		}catch(Exception e){
			log.log("errors occur on url:"+SysUtil.getRequestURL(httpRequest),Logger.LEVEL_ERROR);
			log.log(e,Logger.LEVEL_ERROR);
			SysUtil.redirect(httpRequest,httpResponse,SysConfig.errorPage);
		}
	}
}
