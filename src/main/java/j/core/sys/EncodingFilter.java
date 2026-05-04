package j.core.sys;

import j.core.nvwa.Nvwa;
import j.log.Logger;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



/**
 * 
 * @author 肖炯
 *
 */
@WebFilter(urlPatterns = {"*"}, asyncSupported = true)
public class EncodingFilter implements Filter{
	private static Logger log=Logger.create(EncodingFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		while(!Nvwa.isScanned()){
			log.log("等待Nvwa完成资源扫描......", -1);
			try{
				Thread.sleep(5000);
			}catch(Exception e){}
		}

		HttpServletRequest httpRequest=(HttpServletRequest)request;
		HttpServletResponse httpResponse=(HttpServletResponse)response;

		httpRequest.setCharacterEncoding(SysConfig.sysEncoding);
		if(SysConfig.needSettingResponseEncoding(httpRequest.getRequestURI())){
			httpResponse.setContentType("text/html; charset="+SysConfig.sysEncoding);
		}
		
		/**
		 * doFilter
		 */
		try{
			chain.doFilter(request,response);
		}catch(Exception e){}
	}
	

	/* (non-Javadoc)
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	public void init(FilterConfig conf) throws ServletException {		
	}
	
	/* (non-Javadoc)
	 * @see jakarta.servlet.Filter#destroy()
	 */
	public void destroy() {		
	}
}