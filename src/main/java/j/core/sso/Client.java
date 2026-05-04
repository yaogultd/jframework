package j.core.sso;

import j.core.web.Constants;
import j.log.Logger;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class Client implements Serializable, Runnable{
	private static final long serialVersionUID = 1L;
	private static Logger log=Logger.create(Client.class);
	
	private boolean isSsoServer=false;
	private boolean canLogin=true;
	private String id;
	private String name;
	private String accessKey;
	private String accessSecret;
	private String aesKey;
	private String aesOffset;
	private String urlDefault;
	private ConcurrentList<String> urls=new ConcurrentList<>();
	private ConcurrentList<String> domains=new ConcurrentList<>();
	private String loginPage;
	private String homePage;
	private String passport;
	private String loginInterface;
	private String logoutInterface;
	private LoginAgent loginAgent;
	private String userClass;
	private ConcurrentMap properties;//自定义参数
	
	/**
	 * 
	 *
	 */
	protected Client(){
		properties=new ConcurrentMap();
	}

	//该client是否和sso server在同一jvm中
	public boolean isLocal(){
		return this.getId().equals(SysConfig.getSysId());
	}

	//isSsoServer
	public boolean isSsoServer(){
		return this.isSsoServer;
	}
	public void setIsSsoServer(boolean isSsoServer){
		this.isSsoServer=isSsoServer;
	}
	
	//canLogin
	public boolean canLogin(){
		return this.canLogin;
	}
	public void setCanLogin(boolean canLogin){
		this.canLogin=canLogin;
	}

	//id
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id=id;
	}
	
	//name
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name=name;
	}

	//accessKey
	public String getAccessKey(){
		return this.accessKey;
	}
	public void setAccessKey(String accessKey){
		this.accessKey=accessKey;
	}

	//accessSecret
	public String getAccessSecret(){
		return this.accessSecret;
	}
	public void setAccessSecret(String accessSecret){
		this.accessSecret=accessSecret;
	}

	//aesKey
	public String getAesKey(){
		return this.aesKey;
	}
	public void setAesKey(String aesKey){
		this.aesKey=aesKey;
	}

	//aesOffset
	public String getAesOffset(){
		return this.aesOffset;
	}
	public void setAesOffset(String aesOffset){
		this.aesOffset=aesOffset;
	}
	
	//about default url
	public String getUrlDefault(){
		return this.urlDefault;
	}
	public void setUrlDefault(String urlDefault){
		this.urlDefault=urlDefault;
	}
	public boolean isUrlDefault(String url){
		return this.urlDefault.equalsIgnoreCase(url);
	}
	public String getUrlDefault(HttpServletRequest request){
		if(request.getScheme().toLowerCase().indexOf("https")>-1){
			if(this.urlDefault.startsWith("https:")) return this.urlDefault;
			else return "https"+this.urlDefault.substring(4);
		}else{
			if(this.urlDefault.startsWith("http:")) return this.urlDefault;
			else return "http"+this.urlDefault.substring(5);
		}
	}

	//about url
	public List getUrls(){
		return this.urls;
	}	
	public boolean isMine(String requestURL){
		if(requestURL==null) return false;

		for(int i=0; i<urls.size(); i++){
			String url=urls.get(i);

			if(requestURL.startsWith(url)
					||requestURL.startsWith(url.replaceAll("https","http"))
					||requestURL.startsWith(url.replaceAll("http","https"))) return true;
		}
		return false;
	}

	public boolean isMineWildcard(String requestURL){
		if(requestURL==null) return false;
		
		for(int i=0; i<urls.size(); i++){
			String url=urls.get(i);

			if(JUtilString.matchIgnoreCase(requestURL, url, "*")==0
					||JUtilString.matchIgnoreCase(requestURL, url.replaceAll("https","http"), "*")==0
					||JUtilString.matchIgnoreCase(requestURL, url.replaceAll("http","https"), "*")==0) return true;
		}
		return false;
	}
	public String getUrlPrefix(String requestURL){
		if(requestURL==null) return null;
		
		for(int i=0;i<urls.size();i++){
			String url=(String)urls.get(i);
			if(requestURL.startsWith(url)) return url;
		}
		
		for(int i=0;i<urls.size();i++){
			String url=(String)urls.get(i);
			if(JUtilString.matchIgnoreCase(requestURL, url, "*")==0){
				requestURL=requestURL.substring(0,requestURL.indexOf("/",8)+1);
			}
		}
		
		return null;
	}
	public boolean contains(String url){
		return this.urls.contains(url);
	}
	public void addUrl(String url){
		if(!this.urls.contains(url)) this.urls.add(url);
	}
	public void delUrl(String url){
		this.urls.remove(url);
	}	
	public void clearUrls(){
		urls.clear();
	}
	
	//all main domain
	public List getDomains(){
		return this.domains;
	}	
	public void addDomain(String domain){
		if(!this.domains.contains(domain)) this.domains.add(domain);
	}
	
	//loginPage
	public String getLoginPage(){
		return this.loginPage;
	}
	public void setLoginPage(String loginPage){
		this.loginPage=loginPage;
	}
	
	//homePage
	public String getHomePage(){
		return this.homePage;
	}
	public void setHomePage(String homePage){
		this.homePage=homePage;
	}
	
	//passport
	public String getPassport(){
		return this.passport;
	}
	public void setPassport(String passport){
		this.passport=passport;
	}
	
	//loginInterface
	public String getLoginInterface(){
		return this.loginInterface;
	}
	public void setLoginInterface(String loginInterface){
		this.loginInterface=loginInterface;
	}
	
	//logoutInterface
	public String getLogoutInterface(){
		return this.logoutInterface;
	}
	public void setLogoutInterface(String logoutInterface){
		this.logoutInterface=logoutInterface;
	}

	//LoginAgent
	public LoginAgent getLoginAgent(){
		return this.loginAgent;
	}
	public void setLoginAgent(LoginAgent loginAgent){
		this.loginAgent=loginAgent;
	}
	
	//userClass
	public String getUserClass(){
		return this.userClass;
	}
	public void setUserClass(String userClass){
		this.userClass=userClass;
	}
	
	//properties
	public ConcurrentMap getProperties(){
		return this.properties;
	}	
	public String getProperty(String key){
		return (String)this.properties.get(key);
	}
	public void setProperty(String key,String value){
		this.properties.put(key,value);
	}
	
	//agent login
	public boolean available(String fromClientId){
		return this.loginAgent.available(fromClientId);
	}
	
	public LoginResult login(String fromClientId, HttpServletRequest request, Map<String, String> params, String userIp, String uaId){
		return this.loginAgent.login(fromClientId,request, params,userIp,uaId);
	}

	public LoginResult login(String fromClientId,HttpServletRequest request, String userIp, String uaId){
		if(isLocal()){
			Map<String, String> params=SysUtil.getHttpParameterMap(request);
			params.put(Constants.SSO_USER_IP, userIp);
			params.put(Constants.USER_AGENT_IDENTIFY, uaId);
			params.put("action", "ssologinagent");
			return SSOClient.loginLocal(params);
		}

		return this.loginAgent.login(fromClientId,request, null, userIp, uaId);
	}

	@Override
	public void run() {
		//等待系统初始化完成
		try{
			Thread.sleep(5000);
		}catch(Exception e){}
	}
}