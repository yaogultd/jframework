package j.core.web.online;

import j.core.sso.User;

import java.io.IOException;

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
public class OnlineHandlerImpl implements OnlineHandler{
	@Override
	public UrlAndFetchType adjustUrl(HttpServletRequest request,String uri){
		return new UrlAndFetchType(uri, UrlAndFetchType.TYPE_FORWARD);
	}

	@Override
	public boolean canPass(HttpServletRequest request) {
		return true;
	}

	@Override
	public void onManySessionsOnIp(String ip) {
	}

	@Override
	public void onInit(Online online, 
			User user,
			HttpServletRequest request) {
	}

	@Override
	public void onLogin(Online online, User user, HttpServletRequest request) {
		//nothing to do by default
	}

	@Override
	public void onLogout(Online online, 
			User user,
			HttpServletRequest request) {
		//nothing to do by default
	}

	@Override
	public boolean doFilterBefore(ServletRequest _request, 
			ServletResponse _response,
			FilterChain chain) throws IOException, ServletException {
		return true;
	}

	@Override
	public boolean doFilterAfter(ServletRequest _request, 
			ServletResponse _response,
			FilterChain chain) throws IOException, ServletException {
		return true;
	}

	@Override
	public void doFilter(ServletRequest _request, 
			ServletResponse _response,
			FilterChain chain) throws IOException, ServletException {
		try{
			chain.doFilter(_request,_response);
		}catch(Exception e){}
	}
}