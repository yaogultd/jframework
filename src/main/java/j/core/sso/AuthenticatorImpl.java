package j.core.sso;

import j.core.nvwa.NvwaAncestor;
import j.core.web.Constants;
import j.log.Logger;
import j.core.sys.SysUtil;
import j.util.ConcurrentMap;
import j.util.JUtilBean;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class AuthenticatorImpl extends NvwaAncestor implements Authenticator{
	private static final long serialVersionUID = 1L;
	private static Logger log=Logger.create(AuthenticatorImpl.class);
	private static ConcurrentMap users=new ConcurrentMap();
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public static UserInXml getUser(String userId){
		return (UserInXml)users.get(userId);
	}

	@Override
	public LoginResult login(Map<String, String> params, String clientIp) throws Exception {
		return this.login(params, clientIp, null);
	}

	@Override
	public LoginResult login(HttpServletRequest request,String clientIp) throws Exception {
		return this.login(SysUtil.getHttpParameterMap(request), clientIp, null);
	}

	@Override
	public LoginResult login(JSONObject params, String clientIp) throws Exception {
		return this.login(JUtilBean.jsonPlain2Map(params), clientIp, null);
	}

	@Override
	public LoginResult login(Map<String, String> params, String clientIp, String uaId) throws Exception {
		/*
		 * 登录流程
		 * 1，判断验证码是否正确
		 * 2，登录信息是否完整
		 * 3，用户是否存在（实际应用中可能需要判断用户状态是否有效等）
		 * 4，密码是否正确
		 */
		String userId=params.get(Constants.SSO_USER_ID);
		String userPwd=params.get(Constants.SSO_USER_PWD);

		LoginResult result=new LoginResult();
		result.setUserId(userId);

		//2，登录信息是否完整
		if(userId==null||userPwd==null){
			result.setResult(LoginResult.RESULT_BAD_REQUEST);
			return result;
		}

		//用户不存在
		if(!users.containsKey(userId)){
			result.setResult(LoginResult.RESULT_USER_NOT_EXISTS);
			return result;
		}

		UserInXml user=(UserInXml)users.get(userId);
		if(!userPwd.equals(user.pw)){
			result.setResult(LoginResult.RESULT_PASSWORD_INCORRECT);
			return result;
		}

		result.setResult(LoginResult.RESULT_PASSED);
		return result;
	}

	@Override
	public LoginResult login(HttpServletRequest request,String clientIp,String uaId) throws Exception {
		return this.login(SysUtil.getHttpParameterMap(request), clientIp, uaId);
	}

	@Override
	public LoginResult login(JSONObject params, String clientIp,String uaId) throws Exception {
		return this.login(JUtilBean.jsonPlain2Map(params), clientIp, uaId);
	}

	@Override
	public void logout() throws Exception {
	}
}

/**
 * 
 * @author 肖炯
 *
 */
class UserInXml{
	String id;
	String pw;
	String name;
	String[] roles;
	
	/**
	 * 
	 * @param id
	 * @param pw
	 * @param name
	 * @param roles
	 */
	UserInXml(String id,String pw,String name,String roles){
		this.id=id;
		this.pw=pw;
		this.name=name;
		this.roles=roles.split(",");
	}
}
