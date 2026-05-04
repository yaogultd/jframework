package j.core.sso;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;



/**
 * @author 肖炯
 *
 * 处理用户登录，基于登录框架的应用必须实现此接口
 */
public interface Authenticator extends Serializable{
	/**
	 *
	 * @deprecated
	 * @param params
	 * @param clientIp
	 * @return
	 * @throws Exception
	 */
	public LoginResult login(Map<String, String> params, String clientIp) throws Exception;

	/**
	 * @deprecated
	 * @param request
	 * @param clientIp
	 * @return
	 * @throws Exception
	 */
	public LoginResult login(HttpServletRequest request,String clientIp) throws Exception;

	/**
	 * @deprecated
	 * @param params
	 * @param clientIp
	 * @return
	 * @throws Exception
	 */
	public LoginResult login(JSONObject params, String clientIp) throws Exception;

	/**
	 *
	 * @param params
	 * @param clientIp
	 * @param uaId
	 * @return
	 * @throws Exception
	 */
	public LoginResult login(Map<String, String> params, String clientIp,String uaId) throws Exception;

	/**
	 *
	 * @param request
	 * @param clientIp
	 * @param uaId
	 * @return
	 * @throws Exception
	 */
	public LoginResult login(HttpServletRequest request,String clientIp,String uaId) throws Exception;

	/**
	 *
	 * @param params
	 * @param clientIp
	 * @param uaId
	 * @return
	 * @throws Exception
	 */
	public LoginResult login(JSONObject params, String clientIp,String uaId) throws Exception;

	/**
	 * 注销
	 * @throws Exception
	 */
	public void logout()throws Exception;
}
