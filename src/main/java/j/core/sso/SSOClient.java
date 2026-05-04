package j.core.sso;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.annotation.description.MethodDescription;
import j.core.cache.CachedMap;
import j.core.cache.JCacheParams;
import j.core.common.JProperties;
import j.core.permission.Role;
import j.core.permission.Signature;
import j.core.security.Verifier;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.core.web.handler.JHandler;
import j.core.web.handler.JResponse;
import j.core.web.handler.JSession;
import j.core.web.online.Onlines;
import j.http.HttpUtil;
import j.http.JHttp;
import j.http.JHttpContext;
import j.log.Logger;
import j.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Handler(path = "/framework/api/sso/client")
public class SSOClient extends JHandler implements Runnable{
	private static Logger log=Logger.create(SSOClient.class);
	private static CachedMap logins=null;
	private static ConcurrentMap<String, User> users=new ConcurrentMap<>();
	private static JHttp http=null;
	private static HttpClient hclient=null;
	private static SSOClient instance;

	public static SSOClient getInstance(){
		synchronized (log){
			if(instance==null) instance=new SSOClient();
		}
		return instance;
	}

	 static {
	 	_init();
	 }

	/**
	 *
	 */
	private static void _init(){
		try{
			log.log("init cache for users......", -1);
			if(logins ==null) logins =new CachedMap(SysConfig.getSysId()+"."+Constants.SSO_SESSIONS_CACHE+".client");
			log.log("init cache for users done!", -1);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			try{
				Thread.sleep(5000);
			}catch(Exception ex){}
			_init();
		}
	}

	/**
	 * 是否和sso server运行于同一jvm
	 * @return
	 */
	public static boolean isSSOServer(){
		Client ofServer=SSOConfig.getSsoClientByIdOrUrl(SSOConfig.getSsoServer());
		return ofServer!=null && ofServer.getId().equals(SysConfig.getSysId());
	}

	/**
	 * @deprecated
	 * @param session
	 * @return
	 */
	public static User getCurrentUser(HttpSession session){
		return getCurrentUser(session, null);
	}

	/**
	 *
	 * @param session
	 * @return
	 */
	public static User getCurrentUser(HttpSession session, HttpServletRequest request){
		String accessToken=Onlines.getAccessToken(request);
		if(JUtilString.isBlank(accessToken)) return null;
		User user=users.get(accessToken);
		return user;
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static User getCurrentUser(HttpServletRequest request){
		String accessToken=Onlines.getAccessToken(request);
		if(JUtilString.isBlank(accessToken)) return null;
		User user=users.get(accessToken);
		if(user==null){
			String uaId = Onlines.getUaId(request);
			if(JUtilString.isBlank(uaId)) return null;


		}
		return user;
	}

	/**
	 *
	 * @param request
	 * @param user
	 */
	public static void setCurrentUser(HttpServletRequest request, User user){
		String accessToken=Onlines.getAccessToken(request);
		if(JUtilString.isBlank(accessToken)) return;

		LoginStatus loginStatus=findLoginStatusOfAccessToken(accessToken);
		if(loginStatus==null) return;
		users.put(accessToken, user);
	}

	/**
	 *
	 * @param loginStatus
	 * @param user
	 */
	public static void saveLoginStatus(LoginStatus loginStatus, User user) {
		try{
			logins.put(loginStatus.getAccessToken(), loginStatus);
			users.put(loginStatus.getAccessToken(), user);
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
		}
	}

	/**
	 *
	 * @param loginStatus
	 */
	public static void saveLoginStatus(LoginStatus loginStatus) {
		try{
			logins.put(loginStatus.getAccessToken(), loginStatus);
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 
	 * @param accessToken
	 */
	public static void removeLoginStatus(String accessToken) {
		try{
			users.remove(accessToken);
			logins.remove(accessToken);
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
		}
	}
	
	/**
	 * 
	 *
	 */
	public SSOClient(){
		super();
		if(http==null){
			http=JHttp.getInstance();
		}
		if(hclient==null){
			hclient=http.createClient();
		}
	}

	/**
	 *
	 * @param accessToken
	 * @return
	 */
	public static User getUser(String accessToken){
		return users.get(accessToken);
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static LoginStatus findLoginStatus(HttpServletRequest request){
		return findLoginStatusOfAccessToken(Onlines.getAccessToken(request));
	}

	/**
	 * 
	 * @param accessToken
	 * @return
	 */
	public static LoginStatus findLoginStatusOfAccessToken(String accessToken){
		if(JUtilString.isBlank(accessToken)) return null;

		LoginStatus stat=null;
		try{
			stat=(LoginStatus)logins.get(accessToken);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
		return stat;
	}
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public static LoginStatus[] findLoginStatusOfUserId(String userId){
		if(JUtilString.isBlank(userId)) return null;

		try{
			List<LoginStatus> temp = logins.values(new JCacheParams(new LoginStatusFilter(userId)));
			if(temp==null || temp.isEmpty()) return null;
			LoginStatus[] arr=new LoginStatus[temp.size()];
			temp.toArray(arr);
			return arr;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
		return null;
	}

	/**
	 *
	 * @param userId
	 * @param subUserId
	 * @return
	 */
	public static LoginStatus[] findLoginStatusOfUserId(String userId, String subUserId){
		if(JUtilString.isBlank(userId)) return null;

		try{
			List temp= logins.values(new JCacheParams(new LoginStatusFilter(userId, subUserId)));
			LoginStatus[] arr=new LoginStatus[temp.size()];
			temp.toArray(arr);
			
			return arr;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
		return null;
	}
	
	/**
	 * 
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 * @return
	 */
	public static LoginStatus[] findLoginStatusOfUserId(String userId, String subUserId, boolean includeSubUsers){
		if(JUtilString.isBlank(userId)) return null;
		
		try{
			List temp= logins.values(new JCacheParams(new LoginStatusFilter(userId, subUserId, includeSubUsers)));
			LoginStatus[] arr=new LoginStatus[temp.size()];
			temp.toArray(arr);
			
			return arr;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
		return null;
	}


	/**
	 *
	 * @param accessToken
	 * @param userId
	 * @param subUserId
	 * @throws Exception
	 */
	public static void tellServerToLogoutUser(String accessToken, String userId, String subUserId)throws Exception{
		tellServerToLogoutUser(accessToken, userId, subUserId, true);
	}

	/**
	 *
	 * @param accessToken
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 * @throws Exception
	 */
	public static void tellServerToLogoutUser(String accessToken, String userId, String subUserId, boolean includeSubUsers)throws Exception{
		if(JUtilString.isBlank(accessToken) && JUtilString.isBlank(userId)) return;

		Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());
		String time=SysUtil.getNow()+"";

		Map<String, String> params=new HashMap<>();
		params.put(Constants.SSO_TIME, ""+time);
		params.put(Constants.SSO_CLIENT_ID, client.getId());
		if(!JUtilString.isBlank(accessToken)) params.put(Constants.ACCESS_TOKEN, accessToken);
		if(!JUtilString.isBlank(userId)) params.put(Constants.SSO_USER_ID, userId);
		if(!JUtilString.isBlank(subUserId)) params.put(Constants.SSO_SUB_USER_ID, subUserId);

		String url=SSOConfig.getSsoServer()+"/framework/api/sso/server/logout";

		JHttpContext context = Signature.request(url, params, client.getAccessKey(), client.getAccessSecret());
		log.log("tellServerToLogoutUser - "+context.getResponseText(),-1);
		context.finalize();
		context=null;
	}

	/**
	 *
	 * @param accessToken
	 * @throws Exception
	 */
	public static void logout(String accessToken)throws Exception{
		if(accessToken==null||"".equals(accessToken)) return;

		LoginStatus loginStatus=findLoginStatusOfAccessToken(accessToken);
		if(loginStatus==null) return;

		tellServerToLogoutUser(loginStatus.getAccessToken(),loginStatus.getUserId(),loginStatus.getSubUserId());
	}
	
	/**
	 * 
	 * @param userId
	 * @throws Exception
	 */
	public static void logoutUserId(String userId)throws Exception{
		if(JUtilString.isBlank(userId)) return;
		tellServerToLogoutUser(null,userId,null, true);
	}
	
	/**
	 * 
	 * @param userId
	 * @param subUserId
	 * @throws Exception
	 */
	public static void logoutUserId(String userId, String subUserId)throws Exception{
		logoutUserId(userId, subUserId, false);
	}
	
	/**
	 * 
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 * @throws Exception
	 */
	public static void logoutUserId(String userId, String subUserId, boolean includeSubUsers)throws Exception{
		if(JUtilString.isBlank(userId)) return;

		log.log("logoutUserId userId:"+userId+", subUserId:"+subUserId+", includeSubUsers:"+includeSubUsers, -1);
		tellServerToLogoutUser(null, userId, subUserId, includeSubUsers);
	}
	
	/**
	 * 接收sso server端发出的某个会员登录的命令
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Action(path = "ssologin")
	public void ssologin(JSession jsession, HttpServletRequest request, HttpServletResponse response)throws Exception{
		Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

		String accessToken=jsession.getParameter(Constants.ACCESS_TOKEN);
		String userId=jsession.getParameter(Constants.SSO_USER_ID);
		String subUserId=jsession.getParameter(Constants.SSO_SUB_USER_ID);
		String userIp=jsession.getParameter(Constants.SSO_USER_IP);
		if("null".equals(subUserId)) subUserId=null;

		try{
			boolean verify= Signature.verify(request, client.getAccessSecret());
			if(!verify){
				jsession.jresponse=new JResponse(false, "verify_failed", "");
				return;
			}

			log.log("get ssologin notify, accessToken is -> "+accessToken, -1);
			LoginStatus loginStatus=findLoginStatusOfAccessToken(accessToken);
			if(loginStatus!=null){//已经处理过
				jsession.jresponse=new JResponse(true, "ok","");
				return;
			}

			loginStatus=new LoginStatus(client.getId(),
					accessToken,
					userId,
					subUserId,
					userIp,
					SysConfig.getSysId(),
					SysConfig.getMachineID(),
					"",
					"");

			log.log("get login notify from server -> "+accessToken+" -> "+userId+" -> "+subUserId, -1);

			//保存到缓存
			saveLoginStatus(loginStatus);

			jsession.jresponse=new JResponse(true, "ok","");
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	/**
	 *
	 * @param client
	 * @param params
	 */
	public static void doLogin(Client client, Map<String, String> params){
		String accessToken=params.get(Constants.ACCESS_TOKEN);
		String userId=params.get(Constants.SSO_USER_ID);
		String subUserId=params.get(Constants.SSO_SUB_USER_ID);
		String userIp=params.get(Constants.SSO_USER_IP);
		if("null".equals(subUserId)) subUserId=null;

		LoginStatus loginStatus=findLoginStatusOfAccessToken(accessToken);
		if(loginStatus!=null){//已经处理过
			return;
		}

		loginStatus=new LoginStatus(client.getId(),
				accessToken,
				userId,
				subUserId,
				userIp,
				SysConfig.getSysId(),
				SysConfig.getMachineID(),
				"",
				"");

		//log.log("get login notify from local server -> "+accessToken+" -> "+userId+" -> "+subUserId, -1);

		//保存到缓存
		saveLoginStatus(loginStatus);
	}
	
	/**
	 * 接收sso server端发出的注销某个会员或全部会员的命令
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Action(path = "ssologout")
	public void ssologout(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

		try{
			boolean verify= Signature.verify(request, client.getAccessSecret());
			if(!verify){
				jsession.jresponse=new JResponse(false, "verify_failed", "");
				return;
			}

			//从缓存中移除
			removeLoginStatus(jsession.getParameter(Constants.ACCESS_TOKEN));

			jsession.jresponse=new JResponse(true, "ok","");
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	/**
	 *
	 * @param client
	 * @param params
	 */
	public static void doLogout(Client client, Map<String, String> params){
		try{
			//从缓存中移除
			removeLoginStatus(params.get(Constants.ACCESS_TOKEN));
		}catch(Exception e){}
	}

	@MethodDescription(author = "肖炯",
			date = "2021-08-01",
			description = "分配与图片验证码相关联的UUID")
	@Action(path = "sso_verifier_uuid", getRequestBody = Action.GET_REQUEST_BODY.FALSE, logEnabled = Action.LOG_ENABLED.FALSE)
	public void sso_verifier_uuid(JSession jsession, HttpServletRequest request, HttpServletResponse response) throws Exception{
		try {
			String scriptType=SysUtil.getHttpParameter(request,"type");
			String form=SysUtil.getHttpParameter(request,"form");

			//验证码编号（当在同一界面需显示多个验证码图片时，通过该编号区分， 编号必须大于等于0、小于10）
			String sn = jsession.getParameter(Constants.SSO_VERIFIER_SN);

			//客户端用户会话ID
			String sid = jsession.getParameter(Constants.SSO_CLIENT_SESSION_ID);

			//客户端IP地址
			String ip = HttpUtil.getRemoteIp(request);

			//UUID生成规则：如指定了会话ID，则使用会话ID，否则使用客户端IP；如IP也未指定，则自动获请求来源IP地址
			String uuid = sid == null || "".equals(sid) ? ip : sid;
			if (uuid == null || "".equals(uuid)) uuid = HttpUtil.getRemoteIp(request);

			//再加上验证码编号
			if (JUtilMath.isInt(sn) && Integer.parseInt(sn) >= 0 && Integer.parseInt(sn) < 10) {
				uuid += ":" + sn;
			}

			//然后进行MD5
			uuid = JUtilMD5.MD5EncodeToHex(uuid);

			//分配验证码
			uuid = Verifier.allotUuid(uuid,ip);

			String script="";
			if("variable".equalsIgnoreCase(scriptType)){//输出js变量
				script+="var "+Constants.SSO_VERIFIER_UUID+"='"+uuid+"';";
			}else if("input".equalsIgnoreCase(scriptType)){//输出input对象
				script+=form+"."+Constants.SSO_VERIFIER_UUID+".value='"+uuid+"';";
			}else{//两者兼有
				script+="var "+Constants.SSO_VERIFIER_UUID+"='"+uuid+"';\r\n";
				script+=form+"."+Constants.SSO_VERIFIER_UUID+".value='"+uuid+"';";
			}
			jsession.resultString=script;
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			jsession.resultString="";
		}
	}

	@MethodDescription(author = "肖炯",
			date = "2021-08-01",
			description = "输出与UUID关联的验证码")
	@Action(path = "sso_verifier_code", getRequestBody = Action.GET_REQUEST_BODY.FALSE, logEnabled = Action.LOG_ENABLED.FALSE)
	public void sso_verifier_code(JSession jsession,HttpServletRequest request,HttpServletResponse response) throws Exception{
		try{
			String uuid=jsession.getParameter(Constants.SSO_VERIFIER_UUID);
			if(uuid==null||!Verifier.allotted(uuid)){//必须先通过sso_verifier_uuid请求与验证码关联的UUID
				//log.log("sso_verifier_code -> uuid_not_exists -> "+uuid, -1);
				jsession.resultString="uuid_not_exists";
			}else{
				//log.log("sso_verifier_code -> writeImage -> "+uuid, -1);
				Verifier.writeImage(uuid, response);
			}
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			jsession.resultString="";
		}
	}

	/**
	 * 接收用户请求进行登录
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Action(path = "login", getRequestBody = Action.GET_REQUEST_BODY.TRUE)
	public void login(JSession jsession, HttpServletRequest request, HttpServletResponse response)throws Exception{
		//与验证码管理的UUID
		String verifierUuid=jsession.getParameter(Constants.SSO_VERIFIER_UUID);

		//验证码
		String verifierCode=jsession.getParameter(Constants.SSO_VERIFIER_CODE);

		//如果启用了登录验证码或客户端设置了验证码
		if(SSOConfig.getVerifierCodeEnabled()
				|| (verifierCode!=null && !"".equals(verifierCode))){
			if(!Verifier.isCorrect(verifierUuid, HttpUtil.getRemoteIp(request), verifierCode)) {
				jsession.jresponse=new JResponse(false, "invalid_verifier_code","");
				return;
			}
		}

		Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

		Map<String, String> params=jsession.getParameters();
		params.put(Constants.SSO_CLIENT_ID, client.getId());
		params.put(Constants.SSO_USER_IP, HttpUtil.getRemoteIp(request));
		params.put(Constants.USER_AGENT_IDENTIFY, Onlines.getUaId(request));

		String uaType=jsession.getParameter(Constants.SSO_USER_AGENT_TYPE);
		if(StringUtils.isBlank(uaType)) params.put(Constants.SSO_USER_AGENT_TYPE, request.getHeader("User-Agent"));

		try{
			LoginResult loginResult=null;
			if(isSSOServer()){
				loginResult=loginLocal(params);
				if(loginResult==null){
					jsession.jresponse = new JResponse(false, "login_failed", "登录失败");
					return;
				}

				//登录失败
				if(loginResult.getResult() != 1){
					jsession.jresponse=new JResponse(false, loginResult.getResult()+"", loginResult.getResultMsg());
					jsession.jresponse.putData("result", loginResult);
					return;
				}
			}else {
				String url = JUtilString.appendUrl(SSOConfig.getSsoServer(), "/framework/api/sso/server/login");
				JHttpContext context = Signature.request(url, params, client.getAccessKey(), client.getAccessSecret());
				String resp = context == null ? null : context.getResponseText();
				if (resp == null) {
					if (context != null) {
						context.finalize();
						context = null;
					}
					jsession.jresponse = new JResponse(false, "connect_to_sso_server_failed", "");
					return;
				}
				if (context != null) {
					context.finalize();
					context = null;
				}

				JSONObject resultJson = JUtilJSON.parse(resp);
				String responseCode = JUtilJSON.string(resultJson, "code");
				String responseMsg = JUtilJSON.string(resultJson, "message");

				resultJson = JUtilJSON.object(resultJson, "datas");
				resultJson = JUtilJSON.object(resultJson, "result");
				if (resultJson == null) {
					jsession.jresponse = new JResponse(false, responseCode, responseMsg);
					return;
				}

				//登录结果
				loginResult = (LoginResult) JUtilBean.json2Bean(LoginResult.class, resultJson);

				//登录失败
				if(loginResult.getResult() != 1){
					jsession.jresponse=new JResponse(false, responseCode, responseMsg);
					jsession.jresponse.putData("result", loginResult);
					return;
				}
			}

			//登录成功
			String accessToken=loginResult.getAccessToken();
			String refreshToken=loginResult.getRefreshToken();
			String userId=loginResult.getUserId();
			String subUserId=loginResult.getSubUserId();
			String userIp=loginResult.getUserIp();
			if("null".equals(subUserId)) subUserId=null;

			//加载用户信息
			User user=User.loadUser(request, userId, subUserId, accessToken);//加载用户信息
			if(user==null){
				log.log("已经登录，但加载用户信息失败 - "+accessToken+","+userId+","+subUserId,-1);
				jsession.jresponse=new JResponse(false, "load_user_failed", "加载用户信息失败");
				jsession.jresponse.putData("result", loginResult);
				return;
			}
			log.log("user loaded userId -> "+user.getUserId()+", subUserId -> "+subUserId+", accessToken -> "+accessToken, -1);

			LoginStatus loginStatus=findLoginStatusOfAccessToken(accessToken);
			if(loginStatus==null){
				loginStatus=new LoginStatus(client.getId(),
						accessToken,
						userId,
						subUserId,
						userIp,
						SysConfig.getSysId(),
						SysConfig.getMachineID(),
						"",
						"");
			}

			//保存到session（兼容旧版本）
			if("true".equals(JProperties.getEnv("SessionRequired")) && request!=null){
				try{
					HttpSession session = request.getSession();
					session.setAttribute(Constants.ACCESS_TOKEN, accessToken);
				}catch (Exception ignored){}
			}

			loginStatus.login();//确认登录
			loginStatus.setUpdateTime(SysUtil.getNow());
			loginStatus.setLoginFromDomain(SysUtil.getHttpDomain(request));
			loginStatus.setUserAgent(request.getHeader("User-Agent"));

			//存入缓存
			saveLoginStatus(loginStatus, user);

			Map<String, String> data=new HashMap<>();
			data.put(Constants.EXPRIRED, String.valueOf(SSOConfig.getSessionTimeout()*1000));
			data.put(Constants.ACCESS_TOKEN, accessToken);
			data.put(Constants.REFRESH_TOKEN, refreshToken);

			String sRoles="";
			List<Role> roles=user.getRoles();
			for(int i=0; roles!=null && i<roles.size(); i++){
				Role role=roles.get(i);
				if(!"".equals(sRoles)) sRoles+=",";
				sRoles+=role.getRoleId();
			}
			data.put(Constants.SSO_USER_ROLES, sRoles);

			data.put(Constants.SSO_USER_ID, userId);
			if(subUserId!=null) data.put(Constants.SSO_SUB_USER_ID, subUserId);

			data.put(Constants.SSO_USER_NAME, JUtilString.isBlank(user.getUserName())?"":user.getUserName());
			data.put(Constants.SSO_USER_TYPE, JUtilString.isBlank(user.getUserType())?"":user.getUserType());

			jsession.jresponse=new JResponse(true, "ok","登录成功");
			jsession.jresponse.putData("result", data);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	/**
	 * 接收用户请求进行登出
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Action(path = "logout")
	public void logout(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		String accessToken=SysUtil.getHttpHeader(request, Constants.ACCESS_TOKEN);

		if(accessToken==null || "".equals(accessToken)){
			jsession.jresponse=new JResponse(false, "invalid_access_token","");
			return;
		}

		Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

		Map<String, String> params=new HashMap<>();
		params.put(Constants.SSO_CLIENT_ID, client.getId());
		params.put(Constants.SSO_INCLUDE_SUB_USERS, "false");
		params.put(Constants.ACCESS_TOKEN, accessToken);

		try{
			String url=JUtilString.appendUrl(SSOConfig.getSsoServer(), "/framework/api/sso/server/logout");
			JHttpContext context = Signature.request(url, params, client.getAccessKey(), client.getAccessSecret());
			String resp=context==null?null:context.getResponseText();
			if(resp==null){
				if(context != null){
					context.finalize();
					context=null;
				}
				jsession.jresponse=new JResponse(false, "connect_to_sso_server_failed","");
				return;
			}

			if(context != null){
				context.finalize();
				context=null;
			}


			JSONObject _resp=JUtilJSON.parse(resp);
			String success=JUtilJSON.string(_resp, "success");

			if("true".equals(success)){//登出成功//保存到session（兼容旧版本）
				if("true".equals(JProperties.getEnv("SessionRequired"))){
					try{
						HttpSession session = request.getSession();
						session.removeAttribute(Constants.USER_AGENT_IDENTIFY);
						session.removeAttribute(Constants.ACCESS_TOKEN);
					}catch (Exception ignored){}
				}

				//移除缓存
				removeLoginStatus(accessToken);
				jsession.jresponse=new JResponse(true, "ok","");
			}else{
				jsession.jresponse=new JResponse(false, "logout_failed", "");
			}
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}
	
	/**
	 * 
	 * @param jsession
	 * @param request
	 * @param response
	 */
	@Action(path = "ssologinagent")
	public void ssologinagent(JSession jsession,HttpServletRequest request,HttpServletResponse response){
		try{
			Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

			boolean verify= Signature.verify(request, client.getAccessSecret());
			if(!verify){
				jsession.jresponse=new JResponse(false, "verify_failed", "");
				return;
			}
			
			String ip=jsession.getParameter(Constants.SSO_USER_IP);
			String uaId=jsession.getParameter(Constants.USER_AGENT_IDENTIFY);
			
			LoginResult loginResult=client.getLoginAgent().getAuthenticator().login(request, ip, uaId);
			if(loginResult==null){
				jsession.jresponse=new JResponse(false, "login_failed", "");
				return;
			}

			jsession.jresponse=new JResponse((loginResult.getResult()==LoginResult.RESULT_PASSED), ""+loginResult.getResult(), loginResult.getResultMsg());
			jsession.jresponse.putData("result", loginResult);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	/**
	 * 本地登录
	 *
	 * @param params
	 * @return
	 */
	public static LoginResult loginLocal(Map<String, String> params){
		LoginResult loginResult=null;
		try{
			String ip=params.get(Constants.SSO_USER_IP);
			String uaId=params.get(Constants.USER_AGENT_IDENTIFY);

			//client
			Client client = SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

			if("true".equals(params.get(Constants.SSO_IGNORE_CHECK))){
				loginResult=new LoginResult();
				loginResult.setResult(LoginResult.RESULT_PASSED);
				loginResult.setUserId(params.get(Constants.SSO_USER_ID));
				loginResult.setSubUserId(params.get(Constants.SSO_SUB_USER_ID));
			}else{
				loginResult=client.getLoginAgent().getAuthenticator().login(params, ip, uaId);
			}

			if(loginResult!=null && loginResult.getResult()==LoginResult.RESULT_PASSED){
				SSOServer.doLogin(client, loginResult, params);
			}
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
		return loginResult;
	}

	/**
	 * 获取当前登录用户信息
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Action(path = "user", getRequestBody = Action.GET_REQUEST_BODY.FALSE)
	public void user(JSession jsession, HttpServletRequest request, HttpServletResponse response)throws Exception{
		try{
			if(response.getHeader(Constants.SSO_USER_IP) != null){
				response.setHeader(Constants.SSO_USER_IP, HttpUtil.getRemoteIp(request));
			}else{
				response.addHeader(Constants.SSO_USER_IP, HttpUtil.getRemoteIp(request));
			}

			User user=SSOClient.getCurrentUser(request);
			LoginStatus[] loginStatuses=user==null?null:SSOClient.findLoginStatusOfUserId(user.getUserId(), user.getSubUserId());
			if(user==null || loginStatuses==null || loginStatuses.length==0){
				jsession.jresponse=new JResponse(true, "non_login", "");
				return;
			}

			String sRoles="";
			user.isUserInRole("");//触发角色加载
			List<Role> roles=user.getRoles();
			for(int i=0; roles!=null && i<roles.size(); i++){
				Role role=roles.get(i);
				if(!"".equals(sRoles)) sRoles+=",";
				sRoles+=role.getRoleId();
			}

			if(response.getHeader(Constants.ACCESS_TOKEN) != null){
				response.setHeader(Constants.ACCESS_TOKEN, loginStatuses[0].getAccessToken());
			}else{
				response.addHeader(Constants.ACCESS_TOKEN, loginStatuses[0].getAccessToken());
			}

			if(response.getHeader(Constants.SSO_USER_ID) != null){
				response.setHeader(Constants.SSO_USER_ID, user.getUserId());
			}else{
				response.addHeader(Constants.SSO_USER_ID, user.getUserId());
			}

			if(!JUtilString.isBlank(user.getSubUserId())){
				if(response.getHeader(Constants.SSO_SUB_USER_ID) != null){
					response.setHeader(Constants.SSO_SUB_USER_ID, user.getSubUserId());
				}else{
					response.addHeader(Constants.SSO_SUB_USER_ID, user.getSubUserId());
				}
			}

			if(!JUtilString.isBlank(user.getUserType())){
				if(response.getHeader(Constants.SSO_USER_TYPE) != null){
					response.setHeader(Constants.SSO_USER_TYPE, user.getUserType());
				}else{
					response.addHeader(Constants.SSO_USER_TYPE, user.getUserType());
				}
			}

			if(response.getHeader(Constants.SSO_USER_ROLES) != null){
				response.setHeader(Constants.SSO_USER_ROLES, sRoles);
			}else{
				response.addHeader(Constants.SSO_USER_ROLES, sRoles);
			}
			//设置http头 end

			jsession.jresponse=new JResponse(true, "1","");
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	@Override
	public void run() {
		//等待系统初始化完成
		try{
			Thread.sleep(5000);
		}catch(Exception e){}
	}
}