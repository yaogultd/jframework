package j.core.web.handler;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import j.core.nvwa.Nvwa;
import j.core.permission.Permission;
import j.core.permission.Resource;
import j.core.permission.Signature;
import j.core.sso.Client;
import j.core.sso.SSOClient;
import j.core.sso.SSOConfig;
import j.core.sso.User;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.type.Result;
import j.core.web.Constants;
import j.core.web.online.Onlines;
import j.core.web.online.UrlAndFetchType;
import j.log.Logger;
import j.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author 肖炯
 *
 * 控制中心，根据用户请求调用相应的业务处理类，并根据执行结果导航
 */
public class Server{
	private static Logger log=Logger.create(Server.class);//日志输出
	private static ConcurrentMap<String, JHandler> handlers=new ConcurrentMap();
	private static ConcurrentMap<String, JHandler> handlersShadow=new ConcurrentMap();
	private static final Object lock=new Object();

	/**
	 *
	 * @param handler
	 * @return
	 */
	public static JHandler getHandler(Handler handler){
		JHandler jHandler=null;
		try {
			if (handler.getSingleton()) {
				synchronized (lock) {
					jHandler = handlers.get(handler.getPath());
					if (jHandler == null) {
						jHandler = (JHandler) Class.forName(handler.getClazz()).getConstructor().newInstance();
						handlers.put(handler.getPath(), jHandler);
					}
				}
			} else {
				jHandler = (JHandler) Class.forName(handler.getClazz()).getConstructor().newInstance();
			}
		}catch (Exception ex){
			log.log(ex, Logger.LEVEL_ERROR);
		}
		return jHandler;
	}

	/**
	 *
	 * @param handler
	 * @return
	 */
	public static JHandler getHandlerOfClazzShadow(Handler handler){
		JHandler jHandler=null;
		try {
			if (handler.getSingleton()) {
				synchronized (lock) {
					jHandler = handlersShadow.get(handler.getPath());
					if (jHandler == null) {
						jHandler = (JHandler) Class.forName(handler.getClazzShadow()).getConstructor().newInstance();
						handlersShadow.put(handler.getPath(), jHandler);
					}
				}
			} else {
				jHandler = (JHandler) Class.forName(handler.getClazzShadow()).getConstructor().newInstance();
			}
		}catch (Exception ex){
			log.log(ex, Logger.LEVEL_ERROR);
		}
		return jHandler;
	}


	/**
	 *
	 * @param request
	 * @param response
	 * @throws ServletException
	 */
	private static String[] ignoredLogActionsOfService=new String[]{"register","unregister","heartbeat","auth","service"};

	public static void service(Handler handler,HttpServletRequest request,HttpServletResponse response)throws ServletException{
		String requestUuid=JUtilUUID.genUUID();

		JSession jsession=null;
		JHandler jHandler=null;//业务处理类
		Action action=null;
		String actionId=SysUtil.getHttpParameter(request,handler.getRequestBy());//得到用户请求的操作名
		String requestURI=request.getRequestURI();

		//检查是否包含独占路径，如果是，独占路径后的部分为业务数据（在具体的业务逻辑中处理，这里直接忽略）
		String exclusivePath = Handlers.isExclusivePath(requestURI);
		if(!JUtilString.isBlank(exclusivePath)) requestURI=exclusivePath;
		if(actionId==null){//RESTful
			actionId=requestURI;
			if(actionId.endsWith("/")) actionId=actionId.substring(0, actionId.length()-1);
			if(actionId.length()>handler.getRESTStylePath().length()){
				actionId=actionId.substring(handler.getRESTStylePath().length()+1);
			}
		}

		String navigateType = SysUtil.getHttpParameter(request,Constants.J_BACK_TYPE);//调转到返回地址所使用的机制
		String navigateUrl=SysUtil.getHttpParameter(request,Constants.J_BACK_URL);//返回给用户的地址
		if(navigateType==null) navigateType=(String)request.getAttribute(Constants.J_BACK_TYPE);
		if(navigateUrl==null) navigateUrl=(String)request.getAttribute(Constants.J_BACK_URL);
		boolean setNavigateUrl=navigateUrl!=null;

		ActionLogger logger=Handlers.selectLogger();
		boolean toLog=true;
		try{
			if(actionId==null){
				try{
					SysUtil.outHttpResponse(response,Constants.J_NO_ACTION);
				}catch(Exception e){}
				return;
			}

			action=handler.getAction(actionId);
			if(action==null) throw new Exception(handler.getPath()+" - 找不到请求的方法 - "+actionId);

			if(".service".equals(handler.getPathPattern())
					&&JUtilString.contain(ignoredLogActionsOfService, actionId)){
				toLog=false;
			}

			String processResult="";//调用业务处理类后的处理结果

			//根据操作名找到对应的业务处理类，并调用其process方法
			jsession=new JSession(action.getMethod());

			jHandler=getHandler(handler);
			if(jHandler==null) throw new Exception("no handler matches "+requestURI);

			JMethod method=jHandler.getMethod(jsession.method);

			//如果方法不存在，且存在共享同一path的另一个处理类
			if((method==null || method.getMethod()==null) && !JUtilString.isBlank(handler.getClazzShadow())){
				jHandler=getHandlerOfClazzShadow(handler);
				if(jHandler==null) throw new Exception("no handler matches "+requestURI);

				method=jHandler.getMethod(jsession.method);
			}

			//Content-Type: multipart/form-data; boundary=----WebKitFormBoundarynkX4zdHz8fJWE7ND
			String contentType=request.getContentType();

			//设置为自动获取request body，并且是post请求，并且不是上传文件
			if(action.isGetRequestBody()
					&& "POST".equalsIgnoreCase(request.getMethod())
					&& (contentType == null || request.getContentType().toLowerCase().indexOf("boundary=")<0)){
				jsession.setRequestBody(JUtilInputStream.string(request.getInputStream(), SysConfig.sysEncoding));
			}
			jHandler.init(jsession, request, response);

			//数据格式校验
			if(action.getValidator()!=null){
				Result valid = action.getValidator().validate(jsession, request);
				if(valid!=null && (valid.getOk()==null || !valid.getOk())){
					SysUtil.outHttpResponse(response, (new JResponse(false, valid.getCode(), valid.getMessage())).toString());
					return;
				}
			}

			//////权限控制/////////
			//用户信息
			User user=SSOClient.getCurrentUser(request);

			//是否是需要认证的资源
			Resource res = Permission.permission(request, user);

			//如果需要认证（仅处理action类型资源， 其它分别在各自网关/调度中心中处理）
			if(!Permission.hasValidPassport(request) && res!=null){
				//响应方式（跳转网址、或输出字符串）
				int responseType=Constants.RESPONSE_TYPE_STRING;

				//是否运行同行
				boolean passed=true;

				if("signature".equals(res.getPolicy())){//使用签名验证机制
					String accessKey = SysUtil.getHttpHeader(request, Constants.ACCESS_KEY);

					//log.log(requestURI+", accessKey -> "+accessKey+", signature -> "+request.getHeader(Constants.SIGNATURE)+" -> "+JUtilBean.map2Json(SysUtil.getHttpParameterMap(request)), -1);

					if(StringUtils.isBlank(accessKey)) passed=false;

					if(passed){
						//client
						Client client = SSOConfig.getSsoClientByAccessKey(accessKey);

						//client不存在
						if (client == null){
							passed=false;
						}

						if(passed){
							//验签未通过
							if (!Signature.verify(request, jsession.getRequestBody(), client.getAccessSecret())) {
								System.out.println("verify signature on url\r\n=> "+requestURI+"\r\n=>"+JUtilBean.map2Json(SysUtil.getHttpParameterMap(request))+"\r\n=>"+jsession.getRequestBody());
								passed=false;
							}
						}
					}
				}else{//基于角色控制权限
					passed=res.isUserInRole(user);
				}

				if(!passed){
					String requestURL=request.getRequestURL().toString();
					if("signature".equals(res.getPolicy())){
						if(Nvwa.isDebug()) log.log("试图访问没有权限的资源:"+res+",验签失败,"+requestURL+" ->> "+ JUtilBean.map2Json(SysUtil.getHttpParameterMap(request)), Logger.LEVEL_FATAL);
						SysUtil.outHttpResponse(response, (new JResponse(false, "signature_error", "")).toString());
					}else{
						if(Nvwa.isDebug()) log.log("试图访问没有权限的资源:"+res+","+(user==null?"未登录":user.getUserId())+","+requestURL, Logger.LEVEL_FATAL);
						if(user==null){//未登录
							SysUtil.outHttpResponse(response, (new JResponse(false, "non_login", "")).toString());
						}else{//无权限
							SysUtil.outHttpResponse(response, (new JResponse(false, "access_denied", "")).toString());
						}
					}

					return;
				}
			}
			//////权限控制///////// END

			jHandler.process(method, jsession, request.getSession(), request, response);
			processResult=jsession.result;//处理结果

			//log.log("process result(1) of "+SysUtil.getRequestURL(request)+" ->\r\n"+jsession.resultString, -1);
			//log.log("process result(2) of "+SysUtil.getRequestURL(request)+" ->\r\n"+jsession.jresponse, -1);
			//log.log("process result(3) of "+SysUtil.getRequestURL(request)+" ->\r\n"+jsession.result, -1);

			if(toLog) logger.before(jsession,request,action,requestUuid);

			if(response.isCommitted()){
				if(!handler.getSingleton()&&jHandler!=null) jHandler=null;
				if(toLog) logger.after(request,action,requestUuid);
				return;//如果已经返回给客户端
			}

			if(navigateUrl==null||(!navigateUrl.startsWith("http")&&!navigateUrl.startsWith("/"))){
				navigateUrl=jsession.getDynamicBackUrl();//动态返回url
			}

			if(jsession.resultString==null
					&&jsession.jresponse==null
					&&jsession.result==null
					&&navigateUrl==null){
				SysUtil.outHttpResponse(response,"");
				if(toLog) logger.after(request,action,requestUuid);
				return;//无处理结果
			}

			if(!JUtilString.isBlank(jsession.resultString)){//如果是直接输出
				if(!handler.getSingleton()&&jHandler!=null) jHandler=null;

				SysUtil.outHttpResponse(response,jsession.resultString);//print返回内容给用户
				if(toLog) logger.after(request,action,requestUuid,jsession.resultString);
				return;
			}else if(jsession.jresponse!=null){//如果是直接输出
				if(!handler.getSingleton()&&jHandler!=null) jHandler=null;

				String resultString=jsession.jresponse.toString(request);
				SysUtil.outHttpResponse(response,resultString);//print返回内容给用户
				if(toLog) logger.after(request,action,requestUuid,resultString);
				return;
			}

			if(jsession.getIsBackToGlobalNavigation()){//执行全局导航定义
				navigateType=Handlers.getGlobalNavigateType(processResult);//返回类型
				navigateUrl=Handlers.getGlobalNavigateUrl(processResult);//返回地址
			}else{
				if(navigateUrl==null||(!navigateUrl.startsWith("http")&&!navigateUrl.startsWith("/"))){
					navigateUrl=jsession.getDynamicBackUrl();//动态返回url
					if(navigateUrl==null){//如果未设置了动态返回url
						navigateUrl=action.getNavigateUrl(processResult);//返回地址
					}
				}

				if(!"forward".equals(navigateType)&&!"redirect".equals(navigateType)){
					navigateType=action.getNavigateType(processResult);//返回类型
				}
			}
			if(!handler.getSingleton()&&jHandler!=null) jHandler=null;

			if(toLog) logger.after(request,action,requestUuid,navigateType,navigateUrl);
			if(navigateType==null){
				SysUtil.outHttpResponse(response,jsession.resultString);//print返回内容给用户
				if(toLog) logger.after(request,action,requestUuid,jsession.resultString);
				return;
			}

			if(!setNavigateUrl && Onlines.getHandler()!=null){
				UrlAndFetchType urlAdjust=Onlines.getHandler().adjustUrl(request,navigateUrl);
				if(urlAdjust!=null && !urlAdjust.getUrl().equals(navigateUrl)){
					//通过request.setAttribute("forwarded")，避免死循环forward
					request.setAttribute("forwarded", "true");
					navigateUrl=urlAdjust.getUrl();
				}
			}

			if(navigateType.equalsIgnoreCase("forward")){//如果返回类型为forward
				SysUtil.forwardI18N(request, response, navigateUrl);
			}else{//如果返回类型为sendRedirect
				SysUtil.redirect(request,response,navigateUrl);
			}
		}catch(Exception ex){
			if(toLog) logger.after(request,action,requestUuid,ex);
			log.log("errors on "+SysUtil.getRequestURL(request)+"\r\n the handler is - "+(jHandler==null?"null":jHandler.getClass().getCanonicalName()), Logger.LEVEL_ERROR);
			log.log(ex, Logger.LEVEL_ERROR);
			if(!handler.getSingleton()&&jHandler!=null) jHandler=null;
			if(!response.isCommitted()){//如果未返回
				if(action!=null&&action.getOnError()!=null){//如果设置了on-error属性
					Navigate nav=action.getNavigate(action.getOnError());
					if(nav!=null){//on-error设置的属性值所代表的<navigate>存在
						navigateType=nav.getType();
						navigateUrl=nav.getUrl();

						if(Onlines.getHandler()!=null){
							UrlAndFetchType urlAdjust=Onlines.getHandler().adjustUrl(request,navigateUrl);
							if(urlAdjust!=null && !urlAdjust.getUrl().equals(navigateUrl)){
								//通过request.setAttribute("forwarded")，避免死循环forward
								request.setAttribute("forwarded", "true");
								navigateUrl=urlAdjust.getUrl();
							}
						}

						if(navigateType.equalsIgnoreCase("forward")){//如果返回类型为forward
							try{
								SysUtil.forwardI18N(request, response, navigateUrl);
								return;
							}catch(IOException ioEx){}
						}else{//如果返回类型为sendRedirect
							try{
								SysUtil.redirect(request,response,navigateUrl);
								return;
							}catch(IOException ioEx){}
						}
					}
				}

				try{
					navigateUrl=SysConfig.errorPage;
					SysUtil.redirect(request,response,navigateUrl);
				}catch(IOException ioEx){}
			}
		}finally{
			if(jsession!=null) jsession=null;
		}
	}

	/**
	 * MCP请求处理
	 * @param exchange
	 * @param requestURI
	 * @param paramsJson
	 * @param accessKey
	 * @param signature
	 * @return
	 * @throws ServletException
	 */
	public static JResponse service(McpSyncServerExchange exchange, String requestURI, String paramsJson, String accessKey, String signature)throws ServletException{
		String requestUuid=JUtilUUID.genUUID();

		JSession jsession=null;
		JHandler jHandler=null;//业务处理类
		Action action=null;
		String actionId=null;//得到用户请求的操作名

		Handler handler=null;
		String pattern=Handlers.isActionPath(requestURI);

		if(pattern!=null){
			if(requestURI.endsWith(pattern)){//常规方式
				handler=Handlers.getHandler(requestURI.substring(0,requestURI.lastIndexOf(pattern)));
			}else{//RESTful方式
				handler=Handlers.getHandlerByRESTPath(requestURI);
			}
		}

		if(handler==null){
			return new JResponse(false, "no_handler_for_requested_resource", "请求的资源不存在");
		}


		//检查是否包含独占路径，如果是，独占路径后的部分为业务数据（在具体的业务逻辑中处理，这里直接忽略）
		String exclusivePath = Handlers.isExclusivePath(requestURI);
		if(!JUtilString.isBlank(exclusivePath)) requestURI=exclusivePath;
		if(actionId==null){//RESTful
			actionId=requestURI;
			if(actionId.endsWith("/")) actionId=actionId.substring(0, actionId.length()-1);
			if(actionId.length()>handler.getRESTStylePath().length()){
				actionId=actionId.substring(handler.getRESTStylePath().length()+1);
			}
		}

		if(actionId==null){
			return new JResponse(false, "no_method_matches_the_requested_resource", "请求的资源不存在");
		}

		ActionLogger logger=Handlers.selectLogger();
		boolean toLog=true;
		try{
			action=handler.getAction(actionId);
			if(action==null) throw new Exception(handler.getPath()+" - 找不到请求的方法 - "+actionId);

			if(".service".equals(handler.getPathPattern())
					&&JUtilString.contain(ignoredLogActionsOfService, actionId)){
				toLog=false;
			}

			String processResult="";//调用业务处理类后的处理结果

			//根据操作名找到对应的业务处理类，并调用其process方法
			jsession=new JSession(action.getMethod());

			jHandler=getHandler(handler);
			if(jHandler==null) throw new Exception("no handler matches "+requestURI);

			JMethod method=jHandler.getMethod(jsession.method);

			//如果方法不存在，且存在共享同一path的另一个处理类
			if((method==null || method.getMethod()==null) && !JUtilString.isBlank(handler.getClazzShadow())){
				jHandler=getHandlerOfClazzShadow(handler);
				if(jHandler==null) throw new Exception("no handler matches "+requestURI);

				method=jHandler.getMethod(jsession.method);
			}

			//////权限控制/////////
			//是否是需要认证的资源
			Resource res = Permission.permission(requestURI);

			//如果需要认证
			if(res!=null){
				//是否运行同行
				boolean passed=true;

				//log.log(requestURI+", accessKey -> "+accessKey+", signature -> "+request.getHeader(Constants.SIGNATURE)+" -> "+JUtilBean.map2Json(SysUtil.getHttpParameterMap(request)), -1);

				if(StringUtils.isBlank(accessKey)) passed=false;

				if(passed){
					//client
					Client client = SSOConfig.getSsoClientByAccessKey(accessKey);

					//client不存在
					if (client == null){
						passed=false;
					}

					if(passed){
						//验签未通过
						if (!Signature.verifyString(paramsJson, signature, client.getAccessSecret())) {
							passed=false;
						}
					}
				}

				if(!passed){
					return new JResponse(false, "signature_error", "");
				}
			}
			//////权限控制///////// END


			//设置request body
			jsession.setRequestBody(paramsJson);
			jsession.setExchange(exchange);
			jHandler.init(jsession, null, null);

			jHandler.process(method, jsession);

			return jsession.jresponse;
		}catch(Exception ex){
			log.log(ex, Logger.LEVEL_ERROR);
			return new JResponse(false, "ERR", "系统错误");
		}
	}
}