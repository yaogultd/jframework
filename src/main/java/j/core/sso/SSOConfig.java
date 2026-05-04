package j.core.sso;

import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.JProperties;
import j.core.web.Constants;
import j.core.permission.Signature;
import j.http.JHttp;
import j.http.JHttpContext;
import j.log.Logger;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.util.*;
import org.apache.http.client.HttpClient;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class SSOConfig implements Consumer {
	private static Logger log=Logger.create(SSOConfig.class);
	private static boolean isServer=false;
	private static String ssoServer;//单点登录服务器地址
	private static Authenticator authenticator;//认证类
	private static boolean verifierCodeEnabled=true;//是否启用登录验证码
	private static int sessionTimeout=7200;//登录用户过期时间，即过多久没有与系统交互则视为会话超时，以秒为单位
	private static int onlineActiveTime=60;//多久没有活动表示用户离线，以秒为单位
	private static int notifiersPerClient=1;//对每个sso client，sso server启用多少个通知线程	
	private static String logoutOtherSessions="sameUserAgentType";//登录时是否注销同一用户的其它session,all 表示全部注销，sameUserAgentType 表示同一设备类型，none表示不注销
	private static ConcurrentList<Client> ssoClients=new ConcurrentList<>();//单点登录客户
	private static ConcurrentMap<String, Client> ssoClientsKeyedById=new ConcurrentMap<>();//单点登录客户

	private static JHttp http=null;
	private static HttpClient hclient=null;

	@FieldDescription(description = "最新配置信息")
	private static String config;

	/**
	 * 
	 *
	 */
	public SSOConfig() {
		super();
	}
	
	//getters
	public static boolean isServer(){
		return isServer;
	}
	
	public static Authenticator getAuthenticator(){
		return authenticator;
	}
	
	public static boolean getVerifierCodeEnabled(){
		return verifierCodeEnabled;
	}
	
	public static int getSessionTimeout(){
		return sessionTimeout;
	}
	
	public static int getOnlineActiveTime(){
		return onlineActiveTime;
	}
	
	public static int getNotifiersPerClient(){
		return notifiersPerClient;
	}
	
	public static String getLogoutOtherSessions(){
		return logoutOtherSessions;
	}
	
	public static ConcurrentList<Client> getSsoClients(){
		return ssoClients;
	}
	
	public static Client getSsoClientById(String id){
		if(id==null||"".equals(id)) return null;
		
		return ssoClientsKeyedById.get(id);
	}
	
	public static Client getSsoClientByIdOrUrl(String idOrUrl){
		if(idOrUrl==null||"".equals(idOrUrl)) return null;
		
		Client client = ssoClientsKeyedById.get(idOrUrl);
		if(client==null){
			for(int i=0;i<ssoClients.size();i++){
				Client thisOne = ssoClients.get(i);
				if(thisOne.isMine(idOrUrl)) return thisOne;
			}
		}
		
		if(client==null){
			for(int i=0;i<ssoClients.size();i++){
				Client thisOne = ssoClients.get(i);
				if(thisOne.isMineWildcard(idOrUrl)) return thisOne;
			}
		}
		
		return client;
	}

	public static Client getSsoClientByAccessKey(String accessKey){
		if(accessKey==null||"".equals(accessKey)) return null;

		for(int i=0;i<ssoClients.size();i++){
			Client thisOne = ssoClients.get(i);
			if(accessKey.equals(thisOne.getAccessKey())) return thisOne;
		}

		return null;
	}
	
	public static String getDefaultUrl(String idOrUrl){
		Client client=getSsoClientByIdOrUrl(idOrUrl);
		return client==null?null:client.getUrlDefault();
	}
	
	public static String getAbsoluteUrl(Client client,String clientUrlPrefix,String url){
		if(url.startsWith("http")) return url;
		
		String urlPrefix=client.getUrlPrefix(clientUrlPrefix);
		if(urlPrefix==null) return url;
		
		if(url.startsWith("/")) return urlPrefix+url.substring(1);
		else return urlPrefix+url; 
	}
	
	public static String getAbsoluteUrlSameDomainOfFromUrl(String fromUrl,String url){
		if(url.startsWith("http")) return url;
		else if(url.startsWith("/")) return SysUtil.getRequestURLBase(fromUrl)+url;
		else return SysUtil.getRequestURLBase(fromUrl)+"/"+url; 
	}
	
	public static String getMainDomain(String idOrUrl){
		if(idOrUrl==null||"".equals(idOrUrl)) return "";
		
		Client client = getSsoClientById(idOrUrl);
		if(client!=null){
			idOrUrl=client.getUrlDefault();
		}else{
			client=getSsoClientByIdOrUrl(idOrUrl);
		}
		
		if(client==null){
			return JUtilString.getMainDomain(idOrUrl);
		}
		
		List domains=client.getDomains();
		for(int i=0;i<domains.size();i++){
			String domain = (String)domains.get(i);
			if(idOrUrl.endsWith(domain+"/")) return domain;
		}
		
		return JUtilString.getMainDomain(idOrUrl);
	}

	public static String getLoginToken(Client client,HttpServletRequest request){
		String userAgentIp=JHttp.getRemoteIp(request);
		String _token=JUtilMD5.MD5EncodeToHex(userAgentIp+"."+client.getPassport());
		return _token;
	}
	//////////

	
	public static String getSsoServer(){
		return ssoServer;
	}
	
	public static String getSsoServer(Client c,String currentUrl){
		if(c.isSsoServer()){
			String defaultHost=SysUtil.getRequestURLBase(ssoServer);
			return ssoServer.replaceAll(defaultHost, SysUtil.getRequestURLBase(currentUrl));
		}else{
			return ssoServer;
		}
	}

	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource) {
		try{
			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的SSO节点）
			if(root.element("SSO")!=null){
				root=root.element("SSO");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			ssoClients.clear();
			ssoClientsKeyedById.clear();

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			if(http==null){
				http=JHttp.getInstance();
				hclient=http.createClient();
			}
			
			//isServer
			SSOConfig.isServer ="true".equalsIgnoreCase(root.elementText("is-server"));
			log.log("SSOConfig.isServer:"+SSOConfig.isServer, -1);
			
			//server
			String ssoServerUrl=root.elementText("server");
			if(!ssoServerUrl.endsWith("/")){
				ssoServerUrl+="/";
			}
			SSOConfig.ssoServer=ssoServerUrl;
			log.log("SSOConfig.ssoServer:"+SSOConfig.ssoServer, -1);

			//authenticator
			SSOConfig.authenticator =(Authenticator)Class.forName(root.elementText("authenticator")).newInstance();
			log.log("SSOConfig.authenticator:"+SSOConfig.authenticator.getClass(), -1);
		
			//
			SSOConfig.verifierCodeEnabled="true".equalsIgnoreCase(root.elementText("verifier-code-enabled"));
			log.log("SSOConfig.verifierCodeEnabled:"+SSOConfig.verifierCodeEnabled, -1);
			
			//session timeout
			SSOConfig.sessionTimeout=Integer.parseInt(root.elementText("session-time-out"));
			log.log("SSOConfig.sessionTimeout(int seconds):"+SSOConfig.sessionTimeout, -1);
			
			//online active time
			SSOConfig.onlineActiveTime=Integer.parseInt(root.elementText("online-active-time"));
			log.log("SSOConfig.onlineActiveTime(int seconds):"+SSOConfig.onlineActiveTime, -1);	
		
			//sso client notifiers
			SSOConfig.notifiersPerClient=Integer.parseInt(root.elementText("notifiers-per-client"));
			log.log("SSOConfig.notifiersPerClient:"+SSOConfig.notifiersPerClient, -1);

			
			//sso client notifiers
			SSOConfig.logoutOtherSessions=root.elementText("logout-other-sessions");
			log.log("SSOConfig.logoutOtherSessions:"+SSOConfig.logoutOtherSessions, -1);
			
			
			//sso client 与 cosite 配置信息加载器
			String clientsConfigLoader=root.elementText("clients-conf-loader");
			SSOConfigLoader loader=(SSOConfigLoader)Class.forName(clientsConfigLoader).newInstance();

			//加载sso client
			List<Client> clients=loader.loadClients();
			if(clients==null) clients=loader.loadClients(root);
			for(int i=0;clients!=null&&i<clients.size();i++){
				Client client=clients.get(i);
				SSOConfig.ssoClients.add(client);
				SSOConfig.ssoClientsKeyedById.put(client.getId(),client);
			}
			clients.clear();

			SSOServer.startup();

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			return false;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理sso.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("/sso.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理sso.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("/sso.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	/**
	 * SSO client调用该方法通知服务器自动登录（无需校验）
	 * @param request
	 * @param ssoUserId
	 * @param ssoSubUserId
	 * @return
	 * @throws Exception
	 */
	public static LoginStatus tellServerToLogin(HttpServletRequest request, String ssoUserId, String ssoSubUserId)throws Exception{
		Client client=SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());

		Map<String, String> params=new HashMap<>();
		params.put(Constants.SSO_CLIENT_ID, client.getId());
		params.put(Constants.SSO_USER_ID, ssoUserId);
		if(ssoSubUserId!=null){
			params.put(Constants.SSO_SUB_USER_ID, ssoSubUserId);
		}
		params.put(Constants.SSO_USER_IP, JHttp.getRemoteIp(request));
		params.put(Constants.SSO_IGNORE_CHECK, "true");//不需要验证

		try{
			String url=JUtilString.appendUrl(SSOConfig.getSsoServer(), "/framework/api/sso/server/login");

			JHttpContext context= Signature.request(http, hclient, null, url, params, null, client.getAccessKey(), client.getAccessSecret());

			String resp=context==null?null:context.getResponseText();
			if(resp==null){
				if(context != null){
					context.finalize();
					context=null;
				}
				return null;
			}

			if(context != null){
				context.finalize();
				context=null;
			}

			JSONObject resultJson= JUtilJSON.parse(resp);
			String responseCode=JUtilJSON.string(resultJson, "code");
			String responseMsg=JUtilJSON.string(resultJson, "message");

			resultJson=JUtilJSON.object(resultJson, "datas");
			resultJson=JUtilJSON.object(resultJson, "result");
			if(resultJson==null) return null;

			//登录结果
			LoginResult loginResult=(LoginResult)JUtilBean.json2Bean(LoginResult.class, resultJson);

			//结果代码
			int result=loginResult.getResult();

			//登录失败
			if(result != 1) return null;

			//登录成功
			String accessToken=loginResult.getAccessToken();
			String refreshToken=loginResult.getRefreshToken();
			String userId=loginResult.getUserId();
			String subUserId=loginResult.getSubUserId();
			String userIp=loginResult.getUserIp();
			if("null".equals(subUserId)) subUserId=null;

			//加载用户信息
			User user=User.loadUser(request, userId, subUserId, accessToken);//加载用户信息
			if(user==null) return null;

			LoginStatus loginStatus=SSOClient.findLoginStatusOfAccessToken(accessToken);

			//log.log("user loaded -> "+user.getClass().getName()+", userId -> "+user.getUserId(), -1);
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
			SSOClient.saveLoginStatus(loginStatus, user);

			return loginStatus;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}
}
