package j.core.permission;

import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.ResourceXml;
import j.core.security.AES;
import j.core.security.StringEncrypt;
import j.core.sso.User;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.core.web.security.RobotInspector;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilRandom;
import j.util.JUtilString;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.LinkedList;
import java.util.List;

/**
 * 权限控制
 * @author 肖炯
 *
 */
public class Permission implements Consumer {
	private static Logger log=Logger.create(Permission.class);

	//机器人检测实现类
	private static String robotInspectorImpl="j.core.web.security.RobotInspector";
	private static RobotInspector robotInspector=null;

	//需要权限控制的资源
	private static List<Resource> resources=new LinkedList();

	//xml以外配置的权限资源（如注解）
	private static ConcurrentMap<String, Resource> extDefinedResources=new ConcurrentMap<>(false, new java.util.concurrent.ConcurrentHashMap());

	//通行证（String）列表
	private static List<String> passports=new LinkedList();

	@FieldDescription(description = "最新配置信息")
	private static String config;

	/**
	 *
	 * @param related
	 * @param resource
	 */
	public static void addExtDefinedResources(String related, Resource resource){
		extDefinedResources.put(related, resource);
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static Resource matchResource(HttpServletRequest request){
		for(int i=0;i<resources.size();i++){
			Resource r=resources.get(i);

			//完全匹配（完全匹配仅对网址类资源有效）
			if(r.matchesComplete(request)) return r;

			//匹配
			if(r.matches(request)) return r;
		}

		List<Resource> _resources=extDefinedResources.listValues();
		for(int i=0;i<_resources.size();i++){
			Resource r=_resources.get(i);

			//完全匹配（完全匹配仅对网址类资源有效）
			if(r.matchesComplete(request)) return r;

			//匹配
			if(r.matches(request)) return r;
		}

		return null;
	}

	/**
	 * 判断用户所访问的资源是否需要身份认证
	 * @param request
	 * @param user
	 * @return
	 */
	public static Resource permission(HttpServletRequest request, User user){
		for(int i=0;i<resources.size();i++){
			Resource r=resources.get(i);

			//完全匹配（完全匹配仅对网址类资源有效），且有权限
			if(r.matchesComplete(request) && r.isUserInRole(user)) return null;

			//匹配且无权限
			if(r.matches(request) && !r.isUserInRole(user)) return r;
		}

		List<Resource> _resources=extDefinedResources.listValues();
		for(int i=0;i<_resources.size();i++){
			Resource r=(Resource)_resources.get(i);

			//完全匹配（完全匹配仅对网址类资源有效），且有权限
			if(r.matchesComplete(request) && r.isUserInRole(user)) return null;

			//匹配且无权限
			if(r.matches(request) && !r.isUserInRole(user)) return r;
		}
		
		return null;
	}


	/**
	 * 判断用户所访问的资源是否需要身份认证
	 * @param requestURI
	 * @return
	 */
	public static Resource permission(String requestURI){
		for(int i=0;i<resources.size();i++){
			Resource r=resources.get(i);

			//完全匹配（完全匹配仅对网址类资源有效），且有权限
			if(r.matchesComplete(requestURI) && r.isUserInRole(null)) return null;

			//匹配且无权限
			if(r.matches(requestURI) && !r.isUserInRole(null)) return r;
		}

		List<Resource> _resources=extDefinedResources.listValues();
		for(int i=0;i<_resources.size();i++){
			Resource r=(Resource)_resources.get(i);

			//完全匹配（完全匹配仅对网址类资源有效），且有权限
			if(r.matchesComplete(requestURI) && r.isUserInRole(null)) return null;

			//匹配且无权限
			if(r.matches(requestURI) && !r.isUserInRole(null)) return r;
		}

		return null;
	}
	
	/**
	 * 是否持有有效通行证
	 * @param request
	 * @return
	 */
	public static boolean hasValidPassport(HttpServletRequest request){
		if(request==null){
			return false;
		}
		
		String passport=SysUtil.getHttpParameter(request, Constants.SSO_PASSPORT);
		if(passport==null){
			return false;
		}
		
		passport=AES.decrypt(passport, SysConfig.getAesKey(), SysConfig.getAesOffset());
		
		return passports.contains(passport);
	}
	
	
	/**
	 * 随机获取一个通行证
	 * @return
	 */
	public static String getSSOPassport(){
		int index=JUtilRandom.nextInt(passports.size());
		String p=(String)passports.get(index);
		p=AES.encrypt(p, SysConfig.getAesKey(), SysConfig.getAesOffset());
		p=JUtilString.encodeURI(p, SysConfig.sysEncoding);
		return p;
	}

	/**
	 *
	 * @return
	 */
	synchronized public static RobotInspector getRobotInspector(){
		if(robotInspector==null){
			try {
				robotInspector=(RobotInspector) Class.forName(robotInspectorImpl).getConstructor().newInstance();
			}catch (Exception e){
				log.log(e, Logger.LEVEL_ERROR);
				robotInspector=new RobotInspector();
			}
		}
		return robotInspector;
	}


	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(j.core.nvwa.resource.Resource resource){
		try{
			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的PERMISSION节点）
			if(root.element("PERMISSION")!=null){
				root=root.element("PERMISSION");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			resources.clear();
			passports.clear();

			//机器人检测实现类
			String _robotInspector=root.elementText("robot-inspector");
			if(!JUtilString.isBlank(_robotInspector)) Permission.robotInspectorImpl=_robotInspector;
			log.log("robot-inspector = "+Permission.robotInspectorImpl,-1);
	 
	        //从配置文件得到需要身份认证的资源列表
	        List<Element> urlsEles=root.elements("urls");
	        for(int j=0; urlsEles!=null && j<urlsEles.size(); j++){
				Element urlsEle=urlsEles.get(j);

				List<Element> urls=urlsEle.elements("url");
				for(int i=0;urls!=null&&i<urls.size();i++){
					Element rEle=urls.get(i);

					String policy=rEle.attributeValue("policy");
					String roles=rEle.attributeValue("roles");
					String noPermissionPage=rEle.attributeValue("no-permission-page");
					String loginPage=rEle.attributeValue("login-page");

					if(StringUtils.isBlank(policy)) policy=urlsEle.attributeValue("policy");
					if(StringUtils.isBlank(roles)) roles=urlsEle.attributeValue("roles");
					if(StringUtils.isBlank(noPermissionPage)) noPermissionPage=urlsEle.attributeValue("no-permission-page");
					if(StringUtils.isBlank(loginPage)) loginPage=urlsEle.attributeValue("login-page");

					ResourceUrl r=new ResourceUrl();
					r.setPolicy(policy);
					r.setRoles(roles);
					r.setNoPermissionPage(noPermissionPage);
					r.setLoginPage(loginPage);

					r.setMode(rEle.attributeValue("mode"));
					r.setUrlPattern(rEle.attributeValue("pattern"));
					r.setRobotInspectEnabled("true".equalsIgnoreCase(rEle.attributeValue("robot-inspect-enabled")));

					List<Element> excludes=rEle.elements("exclude");
					for(int k=0; excludes!=null && k<excludes.size(); k++){
						r.addExclude(excludes.get(k).getText());
					}
					resources.add(r);

					log.log(r.toString(),-1);
				}
			}

			List<Element> actionsEles=root.elements("actions");
	        for(int j=0; actionsEles!=null && j<actionsEles.size(); j++){
				Element actionsEle=actionsEles.get(j);

				List actions=actionsEle.elements("action");
				for(int i=0;actions!=null&&i<actions.size();i++){
					Element rEle=(Element)actions.get(i);

					String policy=rEle.attributeValue("policy");
					String roles=rEle.attributeValue("roles");
					String noPermissionPage=rEle.attributeValue("no-permission-page");
					String loginPage=rEle.attributeValue("login-page");

					if(StringUtils.isBlank(policy)) policy=actionsEle.attributeValue("policy");
					if(StringUtils.isBlank(roles)) roles=actionsEle.attributeValue("roles");
					if(StringUtils.isBlank(noPermissionPage)) noPermissionPage=actionsEle.attributeValue("no-permission-page");
					if(StringUtils.isBlank(loginPage)) loginPage=actionsEle.attributeValue("login-page");

					ResourceAction r=new ResourceAction();
					r.setPolicy(policy);
					r.setRoles(roles);
					r.setNoPermissionPage(noPermissionPage);
					r.setLoginPage(loginPage);

					r.setPath(rEle.attributeValue("path"));
					r.setActionId(rEle.attributeValue("id"));
					r.setRobotInspectEnabled("true".equalsIgnoreCase(rEle.attributeValue("robot-inspect-enabled")));

					List<Element> excludes=rEle.elements("exclude");
					for(int k=0; excludes!=null && k<excludes.size(); k++){
						r.addExclude(excludes.get(k).getText());
					}

					resources.add(r);

					log.log(r.toString(),-1);
				}
			}

			List<Element> servicesEles=root.elements("services");
			for(int j=0; servicesEles!=null && j<servicesEles.size(); j++){
				Element servicesEle=servicesEles.get(j);

				List actions=servicesEle.elements("service");
				for(int i=0;actions!=null&&i<actions.size();i++){
					Element rEle=(Element)actions.get(i);

					String policy=rEle.attributeValue("policy");
					String roles=rEle.attributeValue("roles");

					if(StringUtils.isBlank(policy)) policy=servicesEle.attributeValue("policy");
					if(StringUtils.isBlank(roles)) roles=servicesEle.attributeValue("roles");

					ResourceService r=new ResourceService();
					r.setPolicy(policy);
					r.setRoles(roles);

					r.setPath(rEle.attributeValue("path"));
					r.setClassName(rEle.attributeValue("className"));
					r.setMethod(rEle.attributeValue("id"));
					r.setRobotInspectEnabled("true".equalsIgnoreCase(rEle.attributeValue("robot-inspect-enabled")));

					List<Element> excludes=rEle.elements("exclude");
					for(int k=0; excludes!=null && k<excludes.size(); k++){
						r.addExclude(excludes.get(k).getText());
					}

					resources.add(r);

					log.log(r.toString(),-1);
				}
			}
	    	
	
	        
	        //生成通行证，用于服务器间通信，拥有此通行证的请求不经过任何权限认证
	        String passportTxt=root.elementText("passports");
	        String[] tokens=passportTxt.split("\\s{4}");
	        for(int i=0;i<tokens.length;i++){
	        	String[] cells=tokens[i].split("\\s{2}");
	        	passports.add(StringEncrypt.decrypt(cells[0],cells[1]));
	        }
	        //生成通行证，用于服务器间通信，拥有此通行证的请求不经过任何权限认证 ends

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return false;
		}
	}

	@Override
	public boolean onFound(j.core.nvwa.resource.Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理permission.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("permission.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(j.core.nvwa.resource.Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理permission.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("permission.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}
}
