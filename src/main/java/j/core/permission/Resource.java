package j.core.permission;

import j.core.sso.User;

import jakarta.servlet.http.HttpServletRequest;

public interface Resource {
	/**
	 * 权限控制策略
	 * @return
	 */
	public String getPolicy();

	/**
	 * 已经登录但无权限时跳转的地址
	 * @return
	 */
	public String getNoPermissionPage();
	
	/**
	 * 需要登录时未登录时跳转的地址
	 * @return
	 */
	public String getLoginPage();
	
	/**
	 * 模糊匹配
	 * @param request
	 * @return
	 */
	public boolean matches(HttpServletRequest request);
	
	/**
	 * 完全匹配
	 * @param request
	 * @return
	 */
	public boolean matchesComplete(HttpServletRequest request);

	/**
	 * 模糊匹配
	 * @param requestURI
	 * @return
	 */
	public boolean matches(String requestURI);

	/**
	 * 完全匹配
	 * @param requestURI
	 * @return
	 */
	public boolean matchesComplete(String requestURI);
	
	/**
	 * 用户是否拥有访问该资源的权限
	 * @param user
	 * @return
	 */
	public boolean isUserInRole(User user);

	/**
	 * 验证签名
	 * @param data
	 * @return
	 */
	public boolean verifySignature(Object data);

	/**
	 *
	 * @return
	 */
	public boolean isRobotInspectEnabled();
}
