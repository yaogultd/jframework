package j.core.permission;

import java.util.ArrayList;
import java.util.List;

import j.core.annotation.description.ClassDescription;
import j.core.sso.User;
import j.core.common.JArray;
import j.core.service.server.config.Service;
import j.core.service.server.config.Services;
import j.util.JUtilJSON;
import j.util.JUtilString;

import jakarta.servlet.http.HttpServletRequest;

@ClassDescription(author = "肖炯",
		date = "2021-08-08",
		description = "表示一组/一个微服务")
public class ResourceService implements Resource{
	private String policy;
	private String path;
	private String className;
	private String method;
	private String[] roles;//可访问该资源的角色，多个用|分隔
	private List<String> excludes;//如果匹配excludes中指定的微服务，则不进行权限认证
	private boolean robotInspectEnabled=false;//是否启用机器人检测

	public ResourceService(){
		excludes=new ArrayList();
	}

	//setters
	public void setPolicy(String policy){
		this.policy=policy;
	}

	public void setPath(String path){ this.path=path; }

	public void setClassName(String className){
		this.className = className;
	}

	public void setMethod(String method){
		this.method = method;
	}

	public void setRoles(String _roles){
		if(JUtilString.isBlank(_roles)) this.roles=new String[]{"none"};//任何人不可访问
		else if("-".equals(_roles)) this.roles=new String[]{""};//仅进行机器人检测
		else this.roles=_roles.split("\\|");
	}

	public void setRoles(String[] _roles){
		this.roles=_roles;
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
	public String getNoPermissionPage(){ return "/"; }

	@Override
	public String getLoginPage(){ return "/"; }

	@Override
	public boolean matches(HttpServletRequest request){
		return false;
	}
	
	@Override
	public boolean matchesComplete(HttpServletRequest request){
		return matches(request);
	}

	@Override
	public boolean matches(String requestURI) {
		if("/".equals(requestURI)) return false;

		//去掉结尾的斜杠
		if(requestURI.endsWith("/")) requestURI=requestURI.substring(0, requestURI.length()-1);

		//至少包含一个/
		if(requestURI.lastIndexOf("/") < 1) return false;

		String _path=requestURI.substring(0, requestURI.lastIndexOf("/"));
		String _method=requestURI.substring(requestURI.lastIndexOf("/")+1);

		Service service= Services.getService(_path);
		if(service==null) return false;

		if(this.method==null || "".equals(this.method)){//如果权限设置未指定具体的action
			//如果匹配上了path
			if(path.equals(_path)){
				//检查是否在排除的method之内
				for(int i=0;i<this.excludes.size();i++){
					String exclude=(String)this.excludes.get(i);
					if(exclude.equals(_method)) return false;
				}
				return true;
			}
		}else{//设置了具体的method
			return path.equals(_path) && method.equals(_method);
		}

		return false;
	}

	@Override
	public boolean matchesComplete(String requestURI) {
		return matches(requestURI);
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
		sb.append("{\"path\":\"");
		sb.append(path);
		sb.append("\"");

		sb.append(",\"className\":\"");
		sb.append(className == null ? "": className);
		sb.append("\"");

		sb.append(",\"roles\":\"");
		sb.append(JUtilJSON.convertChars(JArray.toString(roles, "|")));
		sb.append("\"");

		sb.append(",\"excludes\":\"");
		sb.append(JUtilJSON.convertChars(JArray.toString(excludes, ",")));
		sb.append("\"");

		sb.append(",\"policy\":\"");
		sb.append(JUtilJSON.convertChars(policy==null?"role":policy));
		sb.append("\"");

		sb.append("}");

		return sb.toString();
	}
}