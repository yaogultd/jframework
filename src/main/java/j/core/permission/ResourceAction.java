package j.core.permission;

import java.util.ArrayList;
import java.util.List;

import j.core.annotation.description.ClassDescription;
import j.core.sso.User;
import j.core.web.handler.Handler;
import j.core.web.handler.Handlers;
import j.core.common.JArray;
import j.util.JUtilJSON;
import j.util.JUtilString;

import jakarta.servlet.http.HttpServletRequest;

@ClassDescription(author = "肖炯",
		date = "2021-08-08",
		description = "表示一个Controller或一个Action")
public class ResourceAction implements Resource{
	private String policy;
	private String path;	
	private String actionId;
	private String[] roles;//可访问该资源的角色，多个用|分隔
	private List<String> excludes;//如果匹配excludes中指定的方法，则不进行权限认证
	private String noPermissionPage;//当用户已经登录，但不具备访问该资源权限时转向的页面，如不设置则转向SSOServer.noRightPage
	private String loginPage;//当用户未登录时转向的页面，如不设置则转向SSOServer.LOGIN_PAGE
	private boolean robotInspectEnabled=false;//是否启用机器人检测


	/**
	 * 
	 *
	 */
	public ResourceAction(){
		excludes=new ArrayList();
	}
	
	//setters
	public void setPolicy(String policy){
		this.policy=policy;
	}

	public void setPath(String path){
		this.path=path;
	}
	
	public void setActionId(String actionId){
		this.actionId=actionId;
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
		String requestURI=request.getRequestURI();
		if("/".equals(requestURI)) return false;

		//去掉结尾的斜杠
		if(requestURI.endsWith("/")) requestURI=requestURI.substring(0, requestURI.length()-1);

		String _path="";
		Handler handler=null;

		if(requestURI.endsWith(".handler")
				||requestURI.endsWith(".service")
				||requestURI.endsWith(".controller")
				||requestURI.endsWith(".action")){//传统方式（后缀名为.handler、.service等）
			_path=requestURI.substring(0, requestURI.lastIndexOf("."));
		}else if(requestURI.lastIndexOf("/")>0){//RESTful方式
			_path=requestURI.substring(0,requestURI.lastIndexOf("/"));
		}
		handler=Handlers.getHandler(_path);
		
		if(handler==null) return false;

		//传统方式，通过request（或其它设定的参数名）指定action id
		String _actionId=request.getParameter(handler.getRequestBy());
		if(_actionId==null && requestURI.startsWith(handler.getRESTStylePath())){//RESTful
			_actionId=requestURI.substring(handler.getRESTStylePath().length()+1);
		}

		if(this.actionId==null || "".equals(this.actionId)){//如果权限设置未指定具体的action
			//如果匹配上了path
			if(path.equals(_path)){
				//检查是否在排除的action之内
				for(int i=0;i<this.excludes.size();i++){
					String exclude=(String)this.excludes.get(i);
					if(exclude.equals(_actionId)) return false;
				}
				return true;
			}
		}else{//设置了具体的action
			return path.equals(_path) && actionId.equals(_actionId);
		}
		
		return false;
	}

	@Override
	public boolean matchesComplete(HttpServletRequest request){
		return matches(request);
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
		sb.append("{\"path\":\"");
		sb.append(path);
		sb.append("\"");

		sb.append(",\"actionId\":\"");
		sb.append(actionId==null?"":actionId);
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
