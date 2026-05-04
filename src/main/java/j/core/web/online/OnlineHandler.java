package j.core.web.online;

import java.io.IOException;

import j.core.sso.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author 肖炯
 *
 */
public interface OnlineHandler {	
	/**
	 *
	 * @param request
	 * @param uri
	 * @return
	 */
	public UrlAndFetchType adjustUrl(HttpServletRequest request,String uri);
	
	/**
	 * @param request
	 * @return
	 */
	public boolean canPass(HttpServletRequest request);
	
	/**
	 * 
	 * @param ip
	 */
	public void onManySessionsOnIp(String ip);
	
	/**
	 * 
	 * @param online
	 * @param user
	 * @param request
	 */
	public void onInit(Online online,User user,HttpServletRequest request);
	
	/**
	 * 
	 * @param online
	 * @param user
	 * @param request
	 */
	public void onLogin(Online online,User user,HttpServletRequest request);
	
	/**
	 * 
	 * @param online
	 * @param user
	 * @param request
	 */
	public void onLogout(Online online,User user,HttpServletRequest request);

	/**
	 * 
	 * @param _request
	 * @param _response
	 * @param chain
	 */
	public boolean doFilterBefore(ServletRequest _request, 
			ServletResponse _response,
			FilterChain chain) throws IOException, ServletException;
	

	/**
	 * 
	 * @param _request
	 * @param _response
	 * @param chain
	 */
	public boolean doFilterAfter(ServletRequest _request, 
			ServletResponse _response,
			FilterChain chain) throws IOException, ServletException;
	
	/**
	 * 
	 * @param _request
	 * @param _response
	 * @param chain
	 */
	public void doFilter(ServletRequest _request, 
			ServletResponse _response,
			FilterChain chain) throws IOException, ServletException;
}
