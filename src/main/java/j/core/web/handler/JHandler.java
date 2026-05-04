package j.core.web.handler;

import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.util.JUtilBean;
import j.util.JUtilJSON;
import j.util.JUtilString;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;
import java.util.Enumeration;


/**
 * @author 肖炯
 *
 * 实现该接口的业务处理类处理一个或多个用户请求，对应在actions.*.xml的一个<handler>
 */
public abstract class JHandler extends NvwaAncestor {
	/**
	 *
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public JMethod getMethod(String method) throws Exception {
		boolean deprecated = false;//是否旧版
		boolean with4Paramaters = false;//是否4个参数的方法（最老的版本）
		Method _method = null;
		try {
			_method = this.getClass().getDeclaredMethod(method, new Class<?>[]{JSession.class});
		} catch (Exception e) {
			deprecated = true;
			try {
				_method = this.getClass().getDeclaredMethod(method, new Class<?>[]{JSession.class, HttpServletRequest.class, HttpServletResponse.class});
			} catch (Exception exx) {
				with4Paramaters=true;
				try {
					_method = this.getClass().getDeclaredMethod(method, new Class<?>[]{JSession.class, HttpSession.class, HttpServletRequest.class, HttpServletResponse.class});
				} catch (Exception exxxx) {
				}
			}
		}
		return new JMethod(_method, deprecated, with4Paramaters);
	}

	/**
	 *
	 * @param method
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void process(JMethod method, JSession jsession,HttpSession session, HttpServletRequest request,HttpServletResponse response) throws Exception {
		if(method.getMethod()==null){
			throw new Exception(this.getClass().getName()+" - 指定的方法不存在："+jsession.method);
		}else if(method.isWith4Paramaters()){
			method.getMethod().invoke(this,new Object[]{jsession,session, request,response});
		}else if(method.isDeprecated()){
			method.getMethod().invoke(this,new Object[]{jsession,request,response});
		}else{
			method.getMethod().invoke(this,new Object[]{jsession});
		}
	}

	/**
	 *
	 * @param method
	 * @param jsession
	 * @throws Exception
	 */
	public void process(JMethod method, JSession jsession) throws Exception {
		if(method.getMethod()==null){
			throw new Exception(this.getClass().getName()+" - 指定的方法不存在："+jsession.method);
		}else if(method.isWith4Paramaters()){
			method.getMethod().invoke(this,new Object[]{jsession,null, null,null});
		}else if(method.isDeprecated()){
			method.getMethod().invoke(this,new Object[]{jsession,null,null});
		}else{
			method.getMethod().invoke(this,new Object[]{jsession});
		}
	}
	
	/**
	 * 
	 * @param jsession
	 * @param request
	 * @param response
	 */
	public void init(JSession jsession,HttpServletRequest request,HttpServletResponse response){
		//暂存参数及处理参数名兼容性
		if(request != null) {
			Enumeration<String> _parameters = request.getParameterNames();
			while (_parameters.hasMoreElements()) {
				String parameter = _parameters.nextElement();
				if (JUtilString.isBlank(parameter)) continue;
				jsession.setParameter(parameter, SysUtil.getHttpParameter(request, parameter));
			}
		}

		try{
			jsession.addParameters(JUtilBean.jsonPlain2Map(JUtilJSON.parse(jsession.getRequestBody())));
		}catch(Exception ignored){}

		//兼容性处理
		jsession.storeParametersCompatible();

		//暂存http头及处理header名兼容性
		if(request != null) {
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				jsession.addRequestHeader(headerName, SysUtil.getHttpHeader(request, headerName));
			}
		}

		//兼容性处理
		jsession.storeHeadersCompatible();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize(){
	}
}
