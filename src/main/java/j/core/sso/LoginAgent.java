package j.core.sso;

import j.core.web.Constants;
import j.core.permission.Signature;
import j.http.JHttp;
import j.http.JHttpContext;
import j.log.Logger;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.util.*;

import java.io.Serializable;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
public class LoginAgent implements Serializable{
	private static Logger log=Logger.create(LoginAgent.class);
	
	protected String clientId;
	protected boolean avail;
	protected String[] deny;
	protected String[] allow;
	protected boolean denyAll=false;
	protected boolean allowAll=false;
	protected Authenticator authenticator;//认证类
	protected String Interface;
	private JHttp http;
	private HttpClient hc;

	/**
	 *
	 * @param _clientId
	 * @param _avail
	 * @param _forOthers
	 * @param _authenticator
	 * @param _Interface
	 * @throws Exception
	 */
	public LoginAgent(String _clientId,
					  String _avail,
					  String _forOthers,
					  String _authenticator,
					  String _Interface) throws Exception{
		this.clientId=_clientId;
		this.avail="true".equalsIgnoreCase(_avail);
		this.http=JHttp.getInstance();
		this.hc=this.http.createClient(15000);
		
		String[] forOthers=_forOthers.split(";");
		
		for(int i=0;i<forOthers.length;i++){
			if(forOthers[i].equals("_DENY_ALL")) denyAll=true;
			else if(forOthers[i].equals("_ALLOW_ALL")) allowAll=true;
			else if(forOthers[i].startsWith("_DENY:")){
				deny=forOthers[i].substring(6).split(",");
			}else if(forOthers[i].startsWith("_ALLOW:")){
				allow=forOthers[i].substring(7).split(",");
			}
		}
		
		this.Interface=_Interface;

		if(this.avail&&SysConfig.getSysId().equals(this.clientId)){
			try{
				this.authenticator=(Authenticator)Class.forName(_authenticator).newInstance();
			}catch(Exception e){
				this.authenticator=null;
				log.log(e,Logger.LEVEL_FATAL);
			}
		}
	}
	
	/**
	 * 
	 * @param fromClientId
	 * @return
	 */
	boolean available(String fromClientId){
		if(!avail) return false;
		
		if(!clientId.equals(fromClientId)) {//用户不是来自本系统（本SSO Client）的网页上登录
			if(!forIt(fromClientId)) return false;//不为该SSO Client（fromClientId）提供用户验证
		}
		return true;
	}

	/**
	 *
	 * @return
	 */
	Authenticator getAuthenticator(){
		return authenticator;
	}

	/**
	 * 向SSO Server 为以fromClientId为ID的SSO Client提供用户验证
	 * @param fromClientId
	 * @param request
	 * @param _params
	 * @param ip
	 * @param uaId
	 * @return
	 */
	public LoginResult login(String fromClientId, HttpServletRequest request, Map<String, String> _params, String ip, String uaId){
		try{
			LoginResult loginResult=new LoginResult();

			Client parent=parent();//关联的SSO Client信息

			Map<String, String> params=request==null?_params:SysUtil.getHttpParameterMap(request);
			params.put(Constants.SSO_USER_IP, ip);
			params.put(Constants.USER_AGENT_IDENTIFY, uaId);

			//发起验证请求并获得结果
			JHttpContext context= Signature.request(this.http, this.hc, null, this.Interface, params, null, parent.getAccessKey(), parent.getAccessSecret());

			String result=context.getResponseText();
			context.finalize();
			context=null;

			//log.log("agent login result - "+result,-1);

			if(result==null){
				loginResult.setResult(LoginResult.RESULT_ERROR);
				return loginResult;
			}

			JSONObject resultJson= JUtilJSON.parse(result);
			String responseCode=JUtilJSON.string(resultJson, "code");
			String responseMsg=JUtilJSON.string(resultJson, "message");

			resultJson=JUtilJSON.object(resultJson, "datas");
			resultJson=JUtilJSON.object(resultJson, "result");
			if(resultJson==null){
				loginResult.setResult(JUtilMath.isInt(responseCode)?Integer.parseInt(responseCode):LoginResult.RESULT_ERROR);
				loginResult.setResultMsg(responseMsg);
				return loginResult;
			}

			loginResult= (LoginResult)JUtilBean.json2Bean(LoginResult.class, resultJson);

			return loginResult;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			return null;
		}
	}
	
	/**
	 * 是否为该SSO Client（fromClientId）提供用户验证
	 * @param fromClientId
	 * @return
	 */
	boolean forIt(String fromClientId){
		if(JUtilString.contain(deny,fromClientId)) return false;
		else if(JUtilString.contain(allow,fromClientId)) return true;
		else if(denyAll) return false;
		else if(allowAll) return true;
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public Client parent(){
		return SSOConfig.getSsoClientByIdOrUrl(this.clientId);
	}
}