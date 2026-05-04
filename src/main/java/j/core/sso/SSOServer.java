package j.core.sso;

import j.core.Startup;
import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.cache.CachedMap;
import j.core.cache.JCacheParams;
import j.core.common.Global;
import j.core.db.JuserLogin;
import j.core.permission.Signature;
import j.core.sys.SysConfig;
import j.core.web.Constants;
import j.core.web.handler.JHandler;
import j.core.web.handler.JResponse;
import j.core.web.handler.JSession;
import j.http.HttpUtil;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.JUtilBean;
import j.util.JUtilString;
import j.util.JUtilUUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@ClassDescription(author = "肖炯",
		date = "2021-07-31",
		description = "提供统一登录服务")
@Handler(path = "/framework/api/sso/server")
public class SSOServer extends JHandler implements Runnable{
	//日志
	private static Logger log=Logger.create(SSOServer.class);

	//会话记录，key为accessToken，value为SSOSession
	private static CachedMap sessions =null;

	//保存和加载token
	private static SSOTokens tokens=new SSOTokens();

	private static SSOServer instance=null;

	/**
	 *
	 */
	synchronized public static void startup(){
		if(instance==null && SSOConfig.isServer()){
			log.log("SSOServer startup......",-1);

			SSOServer instance=new SSOServer();
			Thread thread=new Thread(instance);
			thread.start();
			log.log("SSOServer started",-1);

			Thread tokensThread=new Thread(tokens);
			tokensThread.start();
			log.log("SSOTokens started",-1);
		}
	}

	/**
	 * 初始化缓存
	 */
	private static void _init(){
		try{
			log.log("init cache for sessions......", -1);
			if(sessions==null) sessions = new CachedMap(SysConfig.getSysId()+"."+Constants.SSO_SESSIONS_CACHE+".server");
			log.log("init cache for sessions done!", -1);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			try{
				Thread.sleep(1000);
			}catch(Exception ex){}
			_init();
		}
	}

	/**
	 * 保存SSOSession到缓存和数据库
	 * @param session
	 */
	public static void saveSession(SSOSession session) {
		saveSession(session, true);
	}

	/**
	 * 保存SSOSession到缓存
	 * @param session
	 * @param saveToDB 是否保存到数据库
	 */
	public static void saveSession(SSOSession session, boolean saveToDB) {
		try{
			while(sessions==null) Global.sleep100Millis();
			sessions.addOne(session.getAccessToken(), session);
			if(!saveToDB) return;

			//保存到数据库
			JuserLogin login=new JuserLogin();
			login.setUuid(JUtilUUID.genUUID());
			login.setUserId(session.getUserId());
			login.setSubUserId(JUtilString.isBlank(session.getSubUserId())?null:session.getSubUserId());
			if(session.getUserAgentIds()!=null && session.getUserAgentIds().size()>0){
				login.setUserAgentType(session.getUserAgentTypes().get(0));
				login.setUserAgentSn(session.getUserAgentIds().get(0));
			}
			login.setUserIp(session.getUserIp());
			login.setLoginTimeTry(session.getCreatedAt());
			login.setLoginTimeOk(session.getCreatedAt());
			login.setLoginTimeAuto(session.getCreatedAt());
			login.setLoginStatus("001");
			login.setLoginMethod("000");
			login.setLoginFailedTimes((short)0);
			login.setAppidLoginFrom(login.getUserAgentSn());
			login.setSessionIdLoginFrom("");
			login.setSessionIdGlobal("");
			login.setClientId(session.getClientId());
			login.setAccessToken(session.getAccessToken());
			login.setRefreshToken(session.getRefreshToken());

			tokens.save(login);
			//保存到数据库 end
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
		}
	}

	/**
	 *
	 * @param session
	 * @param notifyClients
	 */
	public static void removeSession(SSOSession session, boolean notifyClients) {
		try{
			sessions.remove(new JCacheParams(session.getAccessToken()));

			if(notifyClients){
				//通知各client登出
				ConcurrentList<Client> ssoClients=SSOConfig.getSsoClients();
				for(int j=0; j<ssoClients.size(); j++){
					Client c=ssoClients.get(j);
					SSONotifier.addTask(c, session, SSONotifier.type_logout);
				}
				//通知各client登出 end
			}

			//从数据库删除
			tokens.delete(session);
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 从数据库重新加载token
	 * @param login
	 */
	public static void loadSession(JuserLogin login){
		//创建会话
		SSOSession ssoSession=SSOSession.create(login.getClientId(),
				login.getUserId(),
				login.getSubUserId(),
				login.getUserIp(),
				login.getUserAgentType(),
				login.getUserAgentSn());
		ssoSession.setCreatedAt(login.getLoginTimeOk());
		ssoSession.setRequestedAt(login.getLoginTimeOk());
		ssoSession.setAccessToken(login.getAccessToken());
		ssoSession.setRefreshToken(login.getRefreshToken());

		//将会话信息保存到缓存
		SSOServer.saveSession(ssoSession, false);

		//通知各client登录
		ConcurrentList<Client> ssoClients=SSOConfig.getSsoClients();
		for(int j=0; j<ssoClients.size(); j++){
			Client c=ssoClients.get(j);

			//其它client异步通知
			//log.log("重新加载token "+login.getAccessToken()+", 并通知client -> "+c.getId(), -1);
			SSONotifier.addTask(c, ssoSession, SSONotifier.type_login);
		}
		//通知各client登录 end
	}

	/**
	 * 根据accessToken查找会话
	 * @param accessToken
	 * @return
	 */
	public static SSOSession findSession(String accessToken){
		SSOSessionFilter filter=new SSOSessionFilter(accessToken);

		try{
			return (SSOSession)sessions.get(new JCacheParams(filter));
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 * 根据用户信息查找会话
	 * @param accessToken
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 * @return
	 */
	public static List<SSOSession> findSession(String accessToken, String userId, String subUserId, boolean includeSubUsers){
		SSOSessionFilter filter=new SSOSessionFilter(userId, subUserId, includeSubUsers);
		filter.setAccessToken(accessToken);

		try{
			return sessions.values(new JCacheParams(filter));
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}

	@MethodDescription(author = "肖炯",
			date = "2021-08-01",
			description = "sso client 接收用户提交的信息后调用本接口进行登录验证")
	@Action(path = "login", getRequestBody = Action.GET_REQUEST_BODY.TRUE, logEnabled = Action.LOG_ENABLED.TRUE)
	public void login(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		LoginResult result=null;//登录结果，如登录成功LoginResult必须设置userId
		try{
			//不是sso server端
			if(!SSOConfig.isServer()){
				jsession.jresponse=new JResponse(false, "illegal_sso_server","");
				return;
			}

			//SSO Client Id
			String clientId=jsession.getParameter(Constants.SSO_CLIENT_ID);

			//SSO Client 镜像节点ID
			String clientMirrorId=jsession.getParameter(Constants.SSO_CLIENT_MIRROR_ID);

			//用户IP
			String userIp=jsession.getParameter(Constants.SSO_USER_IP);
			if(userIp==null) userIp=HttpUtil.getRemoteIp(request);

			String uaId=jsession.getParameter(Constants.USER_AGENT_IDENTIFY);

			//SSO Client
			Client client=SSOConfig.getSsoClientByIdOrUrl(clientId);

			//不是合法SSO Client
			if(client==null){
				jsession.jresponse=new JResponse(false, "illegal_sso_client","");
				return;
			}

			//该SSO Client不被允许登录
			if(!client.canLogin()){
				jsession.jresponse=new JResponse(false, "rejected_by_server","");
				return;
			}

			//验证签名
			if(!Signature.verify(request, client.getAccessSecret())){
				jsession.jresponse=new JResponse(false, "sign_error","");
				return;
			}

			//是否自动登录（无需验证）
			String ignoreCheck=jsession.getParameter(Constants.SSO_IGNORE_CHECK);
			if("true".equals(ignoreCheck)) {//自动登录
				result=new LoginResult();
				result.setResult(LoginResult.RESULT_PASSED);
				result.setUserId(jsession.getParameter(Constants.SSO_USER_ID));
				result.setSubUserId(jsession.getParameter(Constants.SSO_SUB_USER_ID));
			}else{
				//登录验证代理
				String loginAgent = jsession.getParameter(Constants.SSO_LOGIN_AGENT);
				Client agent = SSOConfig.getSsoClientByIdOrUrl(loginAgent);

				//登录代理不存在
				if (agent == null) {
					jsession.jresponse = new JResponse(false, "illegal_sso_agent", "");
					return;
				}

				//登录代理不能为该client提供服务
				if (!agent.available(clientId)) {
					jsession.jresponse = new JResponse(false, "rejected_by_agent", "");
					return;
				}

				//通过登录代理登录
				result = agent.login(client.getId(), request, userIp, uaId);

				//验证出错（无验证结果）
				if (result == null) {
					jsession.jresponse = new JResponse(false, "login_error", "");
					return;
				}
			}

			if(result.getResult()!=LoginResult.RESULT_PASSED){//登录失败
				jsession.jresponse=new JResponse(false, ""+result.getResult(),result.getResultMsg());
				jsession.jresponse.putData("result", result);
				return;
			}

			//登录成功

			//SSO UserAgent Type
			String userAgentType=jsession.getParameter(Constants.SSO_USER_AGENT_TYPE);

			//SSO UserAgent Id
			String userAgentId=jsession.getParameter(Constants.SSO_USER_AGENT_ID);

			//如果该用户如在别处登录了，先注销
			if(!"none".equalsIgnoreCase(SSOConfig.getLogoutOtherSessions())){//如果未设置为“全部不注销”
				//获取相关会话
				List<SSOSession> otherSessions=findSession(null, result.getUserId(), result.getSubUserId(), true);

				if(otherSessions != null){
					for(int i=0; i<otherSessions.size(); i++){
						//是否需要登出
						boolean toLogout="all".equalsIgnoreCase(SSOConfig.getLogoutOtherSessions());

						//如果设置为“登出同类型客户端”，
						if(!toLogout
								&& "sameUserAgentType".equalsIgnoreCase(SSOConfig.getLogoutOtherSessions())
								&& otherSessions.get(i).fromUserAgentType(userAgentType)){
							toLogout=true;
						}

						//如果需要登出
						if(toLogout){
							SSOServer.removeSession(otherSessions.get(i), true);
						}
					}
				}
			}

			//创建会话
			SSOSession ssoSession=SSOSession.create(clientId,
					result.getUserId(),
					result.getSubUserId(),
					userIp,
					userAgentType,
					userAgentId);

			//将会话信息保存到缓存
			SSOServer.saveSession(ssoSession);

			log.log("登录成功，创建新会话 -> "+ JUtilBean.bean2Json(ssoSession), -1);

			result.setAccessToken(ssoSession.getAccessToken());
			result.setRefreshToken(ssoSession.getRefreshToken());
			result.setUserIp(ssoSession.getUserIp());

			jsession.jresponse=new JResponse((result.getResult()==LoginResult.RESULT_PASSED), ""+result.getResult(),result.getResultMsg());
			jsession.jresponse.putData("result", result);

			//通知各client登录
			ConcurrentList<Client> ssoClients=SSOConfig.getSsoClients();
			for(int j=0; j<ssoClients.size(); j++){
				Client c=ssoClients.get(j);

				//当前发起请求的client无需通知
				if(c.getId().equals(client.getId())){
					continue;
				}

				//其它client异步通知
				SSONotifier.addTask(c, ssoSession, SSONotifier.type_login);
			}
			//通知各client登录 end
			//登录成功 end
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	/**
	 *
	 * @param client
	 * @param result
	 * @param params
	 * @throws Exception
	 */
	public static void doLogin(Client client, LoginResult result, Map<String, String> params) throws Exception{
		//登录成功
		//SSO UserAgent Type
		String userAgentType=params.get(Constants.SSO_USER_AGENT_TYPE);

		//SSO UserAgent Id
		String userAgentId=params.get(Constants.SSO_USER_AGENT_ID);

		//如果该用户如在别处登录了，先注销
		if(!"none".equalsIgnoreCase(SSOConfig.getLogoutOtherSessions())){//如果未设置为“全部不注销”
			//获取相关会话
			List<SSOSession> otherSessions=findSession(null, result.getUserId(), result.getSubUserId(), true);

			if(otherSessions != null){
				for(int i=0; i<otherSessions.size(); i++){
					//是否需要登出
					boolean toLogout="all".equalsIgnoreCase(SSOConfig.getLogoutOtherSessions());

					//如果设置为“登出同类型客户端”，
					if(!toLogout
							&& "sameUserAgentType".equalsIgnoreCase(SSOConfig.getLogoutOtherSessions())
							&& otherSessions.get(i).fromUserAgentType(userAgentType)){
						toLogout=true;
					}

					//如果需要登出
					if(toLogout){
						//从缓存中清除
						sessions.remove(new JCacheParams(otherSessions.get(i).getAccessToken()));

						//发送注销命令
						Client c=SSOConfig.getSsoClientById(otherSessions.get(i).getClientId());
						if(c==null) continue;

						SSONotifier.addTask(c, otherSessions.get(i), SSONotifier.type_logout);
					}
				}
			}
		}

		//创建会话
		SSOSession ssoSession=SSOSession.create(client.getId(),
				result.getUserId(),
				result.getSubUserId(),
				result.getUserIp(),
				userAgentType,
				userAgentId);

		//将会话信息保存到缓存
		SSOServer.saveSession(ssoSession);

		log.log("登录成功(本地登录)，创建新会话 -> "+ JUtilBean.bean2Json(ssoSession), -1);

		result.setAccessToken(ssoSession.getAccessToken());
		result.setRefreshToken(ssoSession.getRefreshToken());
		result.setUserIp(ssoSession.getUserIp());

		//通知各client登录
		ConcurrentList<Client> ssoClients=SSOConfig.getSsoClients();
		for(int j=0; j<ssoClients.size(); j++){
			Client c=ssoClients.get(j);

			if(c.getId().equals(client.getId())){
				//当前发起请求的client同步通知
				SSONotifier.getNotifier(c).login(c, ssoSession);
			}else{
				//其它client异步通知
				SSONotifier.addTask(c, ssoSession, SSONotifier.type_login);
			}
		}
		//通知各client登录 end
		//登录成功 end
	}


	@MethodDescription(author = "肖炯",
			date = "2021-08-01",
			description = "sso client 调用本接口注销某个会话/用户")
	@Action(path = "logout", getRequestBody = Action.GET_REQUEST_BODY.TRUE, logEnabled = Action.LOG_ENABLED.TRUE)
	public void logout(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		try{
			//不是sso server端
			if(!SSOConfig.isServer()){
				jsession.jresponse=new JResponse(false, "illegal_sso_server","");
				return;
			}

			//SSO Client Id
			String clientId=jsession.getParameter(Constants.SSO_CLIENT_ID);

			//SSO Client
			Client client=SSOConfig.getSsoClientByIdOrUrl(clientId);

			//不是合法SSO Client
			if(client==null){
				jsession.jresponse=new JResponse(false, "illegal_sso_client","");
				return;
			}

			//该SSO Client不被允许登录
			if(!client.canLogin()){
				jsession.jresponse=new JResponse(false, "rejected_by_server","");
				return;
			}

			//验证签名
			if(!Signature.verify(request, client.getAccessSecret())){
				jsession.jresponse=new JResponse(false, "sign_error","");
				return;
			}

			//会话token
			String accessToken=jsession.getParameter(Constants.ACCESS_TOKEN);

			//用户ID
			String userId=jsession.getParameter(Constants.SSO_USER_ID);

			//用户子账号ID
			String subUserId=jsession.getParameter(Constants.SSO_SUB_USER_ID);

			//登出是否包含子账号
			String includeSubUsers=jsession.getParameter(Constants.SSO_INCLUDE_SUB_USERS);

			//查找会话信息
			List<SSOSession> logoutSessions=findSession(accessToken, userId, subUserId, "true".equalsIgnoreCase(includeSubUsers));

			//通知各client注销相关会话
			ConcurrentList<Client> ssoClients=SSOConfig.getSsoClients();
			for(int i=0; logoutSessions!=null && i<logoutSessions.size(); i++){
				SSOSession ssoSession=logoutSessions.get(i);
				removeSession(ssoSession, false);

				for(int j=0; j<ssoClients.size(); j++){
					Client c=ssoClients.get(j);
					if(c.getId().equals(client.getId())){//当前发起请求的client，同步通知
						SSONotifier.getNotifier(c).logout(c, ssoSession);
					}else{//其它client异步通知
						SSONotifier.addTask(c, ssoSession, SSONotifier.type_logout);
					}
				}
			}
			//通知各client注销相关会话 end

			logoutSessions.clear();
			logoutSessions=null;

			jsession.jresponse=new JResponse(true, "ok","");
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false, "ERR", "");
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if(!SSOConfig.isServer()) return;

		try{
			Thread.sleep(1000);
		}catch(Exception ignored){}

		try{
			_init();
		}catch(Exception ignored){}

		while(!Startup.isDestroyed()){
			try{
				Thread.sleep(15000);
			}catch(Exception ignored){}

			if(Startup.isDestroyed()){
				return;
			}

			if(!SSOConfig.isServer()) continue;

			//注销过期用户
			try{
				JCacheParams params=new JCacheParams();
				params.valueFilter=SSOSessionRemover.getInstance(SSOConfig.getSessionTimeout()*1000L);

				List values = sessions.values(params);
				if(values==null) continue;
				for(int i=0;i<values.size();i++){
					SSOSession session=(SSOSession)values.get(i);
					SSOServer.removeSession(session, true);
				}
				values.clear();
				values=null;
			}catch(Exception ex){
				log.log(ex,Logger.LEVEL_ERROR);
			}
		}
	}
}