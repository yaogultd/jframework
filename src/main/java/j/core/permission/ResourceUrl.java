package j.core.permission;

import j.core.annotation.description.ClassDescription;
import j.core.sso.User;
import j.core.common.JArray;
import j.core.sys.SysUtil;
import j.util.JUtilJSON;
import j.util.JUtilString;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@ClassDescription(author = "肖炯",
		date = "2021-08-08",
		description = "表示一个网址")
public class ResourceUrl implements Resource{
	public static final String MODE_WILD="wild";//通配符匹配（默认模式）
	public static final String MODE_EQUAL="equal";//完全一致匹配

	private String policy;
	/*
	 * url模式，如：/roar/user/i*.jsp，其中*表示0个或多个任意字符，
	 * 如果用户请求的URL中包含符合该模式的子串，则表示匹配该模式，
	 * 只有具备roles中的一个或多个角色才可访问
	 */
	private String urlPattern;	
	private String mode;//匹配模式
	private String[] roles;//可访问该资源的角色，多个用|分隔
	private List<String> excludes;//如果匹配excludes中指定url模式的，则不进行权限认证
	private String noPermissionPage;//当用户已经登录，但不具备访问该资源权限时转向的页面，如不设置则转向SSOServer.noRightPage
	private String loginPage;//当用户未登录时转向的页面，如不设置则转向SSOServer.LOGIN_PAGE
	private boolean robotInspectEnabled=false;//是否启用机器人检测


	public ResourceUrl(){
		this.mode=MODE_WILD;
		this.excludes=new ArrayList();
	}
	
	//setters
	public void setPolicy(String policy){
		this.policy=policy;
	}

	public void setUrlPattern(String urlPattern){ this.urlPattern=urlPattern; }
	
	public void setMode(String mode) {
		this.mode=mode==null||"".equals(mode)?MODE_WILD:mode;
	}
	
	public void setRoles(String _roles){
		if(JUtilString.isBlank(_roles)) this.roles=new String[]{"none"};//任何人不可访问
		else if("-".equals(_roles)) this.roles=new String[]{""};//仅进行机器人检测
		else this.roles=_roles.split("\\|");
	}

	public void setRoles(String[] _roles){
		this.roles=_roles;
	}
	
	public void setNoPermissionPage(String noPermissionPage){
		this.noPermissionPage=noPermissionPage;
	}
	
	public void setLoginPage(String loginPage){
		this.loginPage=loginPage;
	}
	
	public void addExclude(String exclude){
		this.excludes.add(exclude);
	}

	public void setRobotInspectEnabled(boolean robotInspectEnabled){
		this.robotInspectEnabled=robotInspectEnabled;
	}
	//setters end

	@Override
	public String getPolicy(){ return this.policy; }

	@Override
	public String getNoPermissionPage(){
		return this.noPermissionPage;
	}

	@Override
	public String getLoginPage(){
		return this.loginPage;
	}

	@Override
	public boolean matches(HttpServletRequest request){
		String requestUrl=SysUtil.getRequestURL(request,"sso_");

		//去掉协议部分
		if(requestUrl.startsWith("https")) requestUrl=requestUrl.substring(8);
		else if(requestUrl.startsWith("http")) requestUrl=requestUrl.substring(7);

		//去掉域名部分
		if(requestUrl.indexOf("/")>0) requestUrl=requestUrl.substring(requestUrl.indexOf("/"));
		//如果是完全匹配
		if(MODE_EQUAL.equals(this.mode)) {
			return matchesComplete(request);
		}

		//如果未匹配上
		if(JUtilString.match(requestUrl,this.urlPattern,"*")<0) return false;

		//如已匹配，检查是否包含在排序项中
		for(int i=0; i<this.excludes.size(); i++){
			String exclude=(String)this.excludes.get(i);
			if(JUtilString.match(requestUrl,exclude,"*")>-1) return false;
		}
		
		return true;
	}

	@Override
	public boolean matchesComplete(HttpServletRequest request){
		return request.getRequestURI().equals(this.urlPattern);
	}

	@Override
	public boolean matches(String requestURI) {
		return false;
	}

	@Override
	public boolean matchesComplete(String requestURI) {
		return false;
	}

	@Override
	public boolean isUserInRole(User user){
		if(this.roles==null
				|| (this.roles.length==1 && JUtilString.isBlank(this.roles[0]))
				|| (this.roles.length==1 && "-".equals(this.roles[0]))) return true;
		if(user==null) return false;
		return user.isUserInRole(roles);
	}

	@Override
	public boolean verifySignature(Object data) {
		return false;
	}

	@Override
	public boolean isRobotInspectEnabled(){
		return this.robotInspectEnabled;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer sb=new StringBuffer();
		sb.append("{\"mode\":\"");
		sb.append(mode);
		sb.append("\"");

		sb.append(",\"urlPattern\":\"");
		sb.append(JUtilJSON.convertChars(urlPattern==null?"":urlPattern));
		sb.append("\"");

		sb.append(",\"roles\":\"");
		sb.append(JUtilJSON.convertChars(JArray.toString(roles, "|")));
		sb.append("\"");

		sb.append(",\"excludes\":\"");
		sb.append(JUtilJSON.convertChars(JArray.toString(excludes, ",")));
		sb.append("\"");

		sb.append(",\"noPermissionPage\":\"");
		sb.append(JUtilJSON.convertChars(noPermissionPage==null?"":noPermissionPage));
		sb.append("\"");

		sb.append(",\"loginPage\":\"");
		sb.append(JUtilJSON.convertChars(loginPage==null?"":loginPage));
		sb.append("\"");

		sb.append(",\"policy\":\"");
		sb.append(JUtilJSON.convertChars(policy==null?"role":policy));
		sb.append("\"");

		sb.append("}");

		return sb.toString();
	}
}
