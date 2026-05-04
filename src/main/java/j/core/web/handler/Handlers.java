package j.core.web.handler;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.JProperties;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceClass;
import j.core.nvwa.resource.ResourceXml;
import j.core.permission.Permission;
import j.core.permission.ResourceAction;
import j.core.web.JValidator;
import j.log.JLogger;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilMath;
import j.util.JUtilString;
import lombok.Getter;
import org.dom4j.Document;
import org.dom4j.Element;

import java.lang.reflect.Method;
import java.util.List;

@ClassDescription(author = "肖炯", date = "2021/07/20", description = "action配置信息", reviewers = {})
public class Handlers implements Consumer{
	private static Logger log=Logger.create(Handlers.class);//日志输出	

	private static ConcurrentMap<String,Handler> handlersByPath=new ConcurrentMap();//动作列表	
	private static ConcurrentMap<String,Handler> handlersByRESTPath=new ConcurrentMap();//动作列表
	private static String responserId=null;//本地作为响应节点的id
	private static String responserKey=null;//本地作为响应节点的key
	private static ConcurrentList<String> responsersClusterActions=new ConcurrentList();//需要同步的响应节点的请求地址
	
	private static ConcurrentMap globalNavigates=new ConcurrentMap();//全局导航配置（global-navigate）
	
	private static String[] actionPathPatterns=new String[]{".handler", ".service", ".action"};//action请求路径模式

	private static volatile boolean loggerOn=false;//是否默认开启日志（action中未配置时）
	private static volatile int loggerCount=1;//日志处理器个数
	private static volatile int loggerCountMax=1;//日志处理器个数（可动态增加到的最导致）

	private static volatile int loggerSelector=0;//当前使用哪个日志处理器

	@Getter
	private static ConcurrentList<ActionLogger> loggers=new ConcurrentList<>();//日志处理器
	
	private static volatile long actionTimeout=60000;//请求处理超时时间，如果超过此时间，日志系统将记录为“响应超时”

	//独占路径配置
	private static ConcurrentList<String> exclusivePaths = new ConcurrentList<>();

	/**
	 * 
	 * @param requestURI
	 * @return
	 */
	public static String isActionPath(String requestURI){
		//传统方式（后缀名为.handler、.service等）
		for(int i=0;i<actionPathPatterns.length;i++){
			if(requestURI.endsWith(actionPathPatterns[i])) return actionPathPatterns[i];
		}

		//RESTful方式（无后缀名）
		Handler handler=Handlers.getHandlerByRESTPath(requestURI);
		return handler==null?null:handler.getPathPattern();
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getActionPathPattern(){
		return actionPathPatterns[0];
	}
	
	/**
	 * 得到返回地址，如未找到对应信息则返回系统定义的错误页面地址
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public static String getGlobalNavigateUrl(String condition)throws Exception{
		if(condition==null||condition.equals("")) return null;

		Navigate navigate=(Navigate)globalNavigates.get(condition);
		return navigate==null?null:navigate.getUrl();
	}	
	
	/**
	 * 得到返回类型，如未找到对应信息则返回Navigate.TYPE_REDIRECT
	 * @param condition
	 * @return String
	 * @throws Exception
	 */
	public static String getGlobalNavigateType(String condition)throws Exception{
		if(condition==null||condition.equals("")) return null;
		
		Navigate navigate=(Navigate)globalNavigates.get(condition);
		return navigate==null?null:navigate.getType();
	}

	/**
	 *
	 * @param requestUri
	 * @return
	 */
	public static String isExclusivePath(String requestUri){
		for(String path : exclusivePaths){
			if(requestUri.startsWith(path)) return path;
		}
		return null;
	}
	
	/**
	 * 
	 * @param pathOrRESTPath
	 * @return
	 */
	public static Handler getHandler(String pathOrRESTPath){
		if(pathOrRESTPath==null || "".equals(pathOrRESTPath) || "/".equals(pathOrRESTPath)) return null;

		return handlersByPath.get(pathOrRESTPath);
	}
	
	/**
	 * 
	 * @param RESTPath
	 * @return
	 */
	public static Handler getHandlerByRESTPath(String RESTPath){
		if(RESTPath.indexOf(".")>0) return null;//REST路径中不能包含.

		if(RESTPath.endsWith("/")) RESTPath=RESTPath.substring(0, RESTPath.length()-1);
		if(handlersByRESTPath.containsKey(RESTPath)) return handlersByRESTPath.get(RESTPath);

		while(RESTPath.lastIndexOf("/")>1){
			RESTPath=RESTPath.substring(0, RESTPath.lastIndexOf("/"));
			Handler handler=handlersByRESTPath.get(RESTPath);

			if(handler!=null) return handler;
		}
		return handlersByRESTPath.get(RESTPath);
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getHandlers(){
		return handlersByPath.listValues();
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getResponserId() {
		return responserId;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getResponserKey() {
		return responserKey;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isResponserClusterAction(String url) {
		for(int i=0; i<responsersClusterActions.size(); i++) {
			if(JUtilString.match(url, responsersClusterActions.get(i), "*")>-1) return true;
		}
		return false;
	}

	/**
	 *
	 */
	private static void startLoggers(){
		int count = loggerCount;

		if(loggers.size() > 0){
			int queueLengthAvg = getLoggerQueueLength() / loggers.size();
			if(queueLengthAvg >= 10){
				count += queueLengthAvg/10;
			}
		}
		if(count > loggerCountMax) count=loggerCountMax;

		while(loggers.size() < count){
			ActionLogger logger=new ActionLogger("ACTION_LOGGER_"+loggers.size());
			Thread thread=new Thread(logger,logger.getSn());
			thread.start();

			loggers.add(logger);

			if(loggers.size() <= loggerCount) {
				log.log("Thread "+"ACTION_LOGGER_"+loggers.size()+" started.",-1);
			}else {
				log.log("Thread "+"ACTION_LOGGER_"+loggers.size()+" started(动态增加).",-1);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public static ActionLogger selectLogger(){
		synchronized(loggers){
			startLoggers();

			if(loggerSelector>=loggers.size()) loggerSelector=0;
			ActionLogger logger=loggers.get(loggerSelector);
			loggerSelector++;
			return logger;
		}
	}

	/**
	 *
	 * @return
	 */
	public static int getLoggerQueueLength() {
		int total = 0;
		for(ActionLogger logger : loggers) {
			total += logger.getQueueLength();
		}
		return total;
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean isLoggerOn(){
		return loggerOn;
	}
	

	/**
	 * 
	 * @return
	 */
	public static long getActionTimeout(){
		return actionTimeout;
	}

	@MethodDescription(author = "", date = "2021/07/20", description = "加载主配置信息")
	private static void load(Document doc){
		try{
			Element root = doc.getRootElement();

			//loggerCount
			loggerOn=!"false".equalsIgnoreCase(root.elementText("logger-on"));

			String loggerCountSetting=root.elementText("loggers");
			if(JUtilMath.isInt(loggerCountSetting)){
				loggerCount=Integer.parseInt(loggerCountSetting);
			}

			String loggerCountMaxSetting=root.elementText("loggersMax");
			if(JUtilMath.isInt(loggerCountMaxSetting)){
				loggerCountMax=Integer.parseInt(loggerCountMaxSetting);
			}
			if(loggerCountMax < loggerCount) loggerCountMax = loggerCount;

			//actionTimeout
			String actionTimeoutSetting=root.elementText("action-timeout");
			if(JUtilMath.isLong(actionTimeoutSetting)){
				actionTimeout=Long.parseLong(actionTimeoutSetting);
			}

			//响应节点
			Element responsersEle=root.element("responsers");
			if(responsersEle!=null) {
				responserId=responsersEle.attributeValue("id");
				responserKey=responsersEle.attributeValue("key");

				Element responsersClusterEle=responsersEle.element("cluster");
				if(responsersClusterEle!=null) {
					List urls=responsersClusterEle.elements("url");
					if(urls!=null&&urls.size()>0) {
						for(int j=0; j<urls.size(); j++) {
							Element urlEle=(Element)urls.get(j);
							responsersClusterActions.add(urlEle.getTextTrim());
						}
					}
				}
			}
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	
	/**
	 * 加载配置模块
	 * @throws Exception
	 */
	private static void loadModule(Document doc)throws Exception{
		try{
			Element root = doc.getRootElement();

			List handlerEles=root.elements("handler");
			for(int i=0;handlerEles!=null&&i<handlerEles.size();i++){
				Element handlerEle=(Element)handlerEles.get(i);

				Handler handler= new Handler();
				handler.setPath(handlerEle.attributeValue("path"));

				String RESTStylePath=handlerEle.attributeValue("REST-style-path");
				if(JUtilString.isBlank(RESTStylePath)) RESTStylePath=handler.getPath();
				handler.setRESTStylePath(RESTStylePath);

				handler.setPathPattern(handlerEle.attributeValue("path-pattern"));
				handler.setClazz(handlerEle.attributeValue("class"));
				handler.setRequestBy(handlerEle.attributeValue("request-by"));
				handler.setSingleton("false".equalsIgnoreCase(handlerEle.attributeValue("singleton"))?false:true);

				//检查是否在class中已配置
				Handler exists=handlersByPath.get(handler.getPath());
				if(exists!=null){
					log.log("handler 已经在class中配置，以下两个类将共享路径"+handler.getPath()+": "+handler.getClazz()+","+exists.getClazz(), -1);
					exists.clone(handler);
					handler.setClazzShadow(exists.getClazz());
				}

				List actions=handlerEle.elements("action");
				for(int j=0;j<actions.size();j++){
					Element actionEle=(Element)actions.get(j);

					Action action=new Action();
					action.setId(actionEle.attributeValue("id"));
					action.setName(actionEle.attributeValue("name"));
					action.setMethod(actionEle.attributeValue("method"));
					action.setGetRequestBody("true".equalsIgnoreCase(actionEle.attributeValue("get-request-body")));
					action.setOnError(actionEle.attributeValue("on-error"));

					//数据格式验证
					if(!JUtilString.isBlank(actionEle.attributeValue("validator"))){
						JValidator validator = (JValidator)Class.forName(actionEle.attributeValue("validator")).getConstructor().newInstance();
						if(validator!=null) action.setValidator(validator);
					}

					List navigates=actionEle.elements("navigate");
					for(int k=0;k<navigates.size();k++){
						Element navigateEle=(Element)navigates.get(k);
						Navigate navigate=new Navigate();
						navigate.setCondition(navigateEle.attributeValue("condition"));
						navigate.setType(navigateEle.attributeValue("type"));
						navigate.setUrl(navigateEle.getTextTrim());
						action.addNavigate(navigate);
					}

					Element logEle=actionEle.element("log");
					if(logEle!=null){
						if("true".equalsIgnoreCase(logEle.attributeValue("avail"))) {//action显示申明为“true”时，不管主配置是否开启日志，该action日志都将开启
							action.setLogEnabled(1);
						}else if("false".equalsIgnoreCase(logEle.attributeValue("avail"))) {//action显示申明为“false”时，不管主配置是否开启日志，该action日志都将关闭
							action.setLogEnabled(0);
						}else {//否则，以主配置为准
							action.setLogEnabled(-1);
						}

						//需要保存的参数
						List logParams=logEle.elements("p");
						if(logParams!=null && logParams.size()>0){//指定了需要保存的参数
							action.setLogAllParameters(false);
							for(int k=0;k<logParams.size();k++){
								Element logParamEle=(Element)logParams.get(k);
								action.addLogParam(logParamEle.getTextTrim());
							}
						}else{
							//是否保存全部参数
							action.setLogAllParameters(!"false".equalsIgnoreCase(logEle.attributeValue("save-all-parameters")));
						}

						//是否保存Request Body
						action.setSaveRequestBody("true".equalsIgnoreCase(logEle.attributeValue("save-request-body")));
					}else{
						action.setLogEnabled(-1);
						action.setLogAllParameters(true);
					}
					handler.addAction(action);
				}

				handlersByPath.put(handler.getPath(),handler);
				if(handler.getRESTStylePath()!=null&&!"".equals(handler.getRESTStylePath())){
					handlersByRESTPath.put(handler.getRESTStylePath(),handler);
					//log.log("handlersByRESTPath -> "+handler.getRESTStylePath(), -1);
				}
			}

			//global navigates
			List globalNavigateElements=root.elements("global-navigate");
			for(int i=0;globalNavigateElements!=null&&i<globalNavigateElements.size();i++){
				Element navigateElement=(Element)globalNavigateElements.get(i);

				Navigate navigate=new Navigate();
				navigate.setCondition(navigateElement.attributeValue("condition"));
				navigate.setType(navigateElement.attributeValue("type"));
				navigate.setUrl(navigateElement.getTextTrim());
				globalNavigates.put(navigate.getCondition(),navigate);
			}
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_FATAL);
		}
	}
	
	@MethodDescription(author = "", date = "", description = "")
	private static boolean load(Resource resource){
		if(resource==null) return false;//null

		try{
			if(resource instanceof ResourceXml){//xml配置文件
				ResourceXml _resource=(ResourceXml)resource;
				if(resource.getName().equals("actions.xml")) load(_resource.getResource());
				else{
					log.log("loading "+_resource.getPath(), -1);
					loadModule(_resource.getResource());
				}
			}else if(resource instanceof ResourceClass){//类文件
				ResourceClass _resource=(ResourceClass)resource;

				j.core.annotation.action.Handler handlerAnno=(j.core.annotation.action.Handler)_resource.getResource().getAnnotation(j.core.annotation.action.Handler.class);

				//不是Handler或未正取配置
				if(handlerAnno==null || handlerAnno.path()==null || "".equals(handlerAnno.path())) return false;

				//可配置多个多个path供不同角色调用
				String[] paths=handlerAnno.path().split(",");
				for(int p=0; p<paths.length; p++) {
					String path=paths[p];

					//保存Handler（与xml配置兼容）
					Handler handler = new Handler();
					handler.setRESTStylePath(path);
					handler.setPath(path);
					handler.setPathPattern(".handler");
					handler.setClazz(_resource.getResource().getCanonicalName());
					handler.setRequestBy("request");
					handler.setSingleton(true);

					//检查是否在xml中已配置
					Handler exists=handlersByPath.get(path);
					if(exists!=null){
						log.log("handler 已经在xml中配置，以下两个类将共享路径"+handler.getPath()+": "+handler.getClazz()+","+exists.getClazz(), -1);
						exists.clone(handler);
						handler.setClazzShadow(exists.getClazz());
					}

					//扫描权限注解
					//针对handler区分path的权限设置
					j.core.annotation.auth.Authorities handlerAuths = (j.core.annotation.auth.Authorities) _resource.getResource().getAnnotation(j.core.annotation.auth.Authorities.class);
					if(handlerAuths!=null){
						j.core.annotation.auth.Authority[] multiAuths=handlerAuths.value();
						for(int x=0; x<multiAuths.length; x++){
							j.core.annotation.auth.Authority handlerAuth=multiAuths[x];
							if(!JUtilString.isBlank(handlerAuth.belonged()) && !handlerAuth.belonged().equals(path)) continue;

							ResourceAction r = new ResourceAction();
							r.setPolicy(handlerAuth.policy());
							r.setRoles(handlerAuth.roles());
							r.setNoPermissionPage(handlerAuth.noPermissionPage());
							r.setLoginPage(handlerAuth.loginPage());

							r.setPath(handler.getRESTStylePath());
							r.setActionId("");

							Permission.addExtDefinedResources(handler.getRESTStylePath(), r);

							log.log("通过handler注解设置权限 -> " + r.toString(), -1);
						}
					}

					//针对handler不区分path的权限设置
					j.core.annotation.auth.Authority handlerAuth = (j.core.annotation.auth.Authority) _resource.getResource().getAnnotation(j.core.annotation.auth.Authority.class);
					if (handlerAuth != null
							&& (JUtilString.isBlank(handlerAuth.belonged()) || handlerAuth.belonged().equals(path))) {
						ResourceAction r = new ResourceAction();
						r.setPolicy(handlerAuth.policy());
						r.setRoles(handlerAuth.roles());
						r.setNoPermissionPage(handlerAuth.noPermissionPage());
						r.setLoginPage(handlerAuth.loginPage());

						r.setPath(handler.getRESTStylePath());
						r.setActionId("");

						Permission.addExtDefinedResources(handler.getRESTStylePath(), r);

						log.log("通过handler注解设置权限 -> " + r.toString(), -1);
					}
					System.out.println("扫描到Handler注解 -> " + handler.getRESTStylePath() + " -> " + _resource.getPath());

					//获取action配置
					Method[] methods = _resource.getResource().getDeclaredMethods();
					for (int i = 0; methods != null && i < methods.length; i++) {
						j.core.annotation.action.Action actionAnno = methods[i].getAnnotation(j.core.annotation.action.Action.class);

						//不是action
						if (actionAnno == null) continue;

						//指定了只能通过某个路径访问
						if(!JUtilString.isBlank(actionAnno.belonged()) && !actionAnno.belonged().equals(path)) continue;

						Action action = new Action();
						action.setId(JUtilString.isBlank(actionAnno.path()) ? methods[i].getName() : actionAnno.path());
						action.setName(actionAnno.name());
						action.setMethod(methods[i].getName());
						action.setGetRequestBody(actionAnno.getRequestBody().equals(j.core.annotation.action.Action.GET_REQUEST_BODY.TRUE));
						action.setOnError(null);

						//数据格式验证
						if(!JUtilString.isBlank(actionAnno.validator())){
							JValidator validator = (JValidator)Class.forName(actionAnno.validator()).getConstructor().newInstance();
							if(validator!=null) action.setValidator(validator);
						}

						if(actionAnno.logEnabled().equals(j.core.annotation.action.Action.LOG_ENABLED.TRUE)) {//action显示申明为“true”时，不管主配置是否开启日志，该action日志都将开启
							action.setLogEnabled(1);
						} else if (actionAnno.logEnabled().equals(j.core.annotation.action.Action.LOG_ENABLED.FALSE)) {//action显示申明为“false”时，不管主配置是否开启日志，该action日志都将关闭
							action.setLogEnabled(0);
						} else {//否则，以主配置为准
							action.setLogEnabled(-1);
						}

						//需要保存到日志的参数
						j.core.annotation.action.LogParameter logParameterAnnos[] = methods[i].getAnnotationsByType(j.core.annotation.action.LogParameter.class);
						if (logParameterAnnos == null || logParameterAnnos.length == 0) {
							action.setLogAllParameters(true);
						} else {
							action.setLogAllParameters(false);
							for (int j = 0; j < logParameterAnnos.length; j++) {
								action.addLogParam(logParameterAnnos[j].name());
							}
						}

						//路径独占
						if(actionAnno.pathExclusive()){
							String pathExclusive = JUtilString.appendUrl(handler.getPath(), action.getId());
							if(!exclusivePaths.contains(pathExclusive)) exclusivePaths.add(pathExclusive);
						}

						//针对action区分path的权限设置
						j.core.annotation.auth.Authorities actionAuths = methods[i].getAnnotation(j.core.annotation.auth.Authorities.class);
						if(actionAuths!=null){
							j.core.annotation.auth.Authority[] multiAuths=actionAuths.value();
							for(int x=0; x<multiAuths.length; x++){
								j.core.annotation.auth.Authority actionAuth=multiAuths[x];
								if(!JUtilString.isBlank(actionAuth.belonged()) && !actionAuth.belonged().equals(path)) continue;

								ResourceAction r = new ResourceAction();
								r.setPolicy(actionAuth.policy());
								r.setRoles(actionAuth.roles());
								r.setNoPermissionPage(actionAuth.noPermissionPage());
								r.setLoginPage(actionAuth.loginPage());

								r.setPath(handler.getRESTStylePath());
								r.setActionId(action.getId());

								Permission.addExtDefinedResources(handler.getRESTStylePath() + "->" + action.getId(), r);

								log.log("通过action注解设置权限 -> " + r.toString(), -1);
							}
						}

						//针对aciton的不分区path的权限设置
						j.core.annotation.auth.Authority actionAuth = methods[i].getAnnotation(j.core.annotation.auth.Authority.class);
						if (actionAuth != null
								&& (JUtilString.isBlank(actionAuth.belonged()) || actionAuth.belonged().equals(path))) {
							ResourceAction r = new ResourceAction();
							r.setPolicy(actionAuth.policy());
							r.setRoles(actionAuth.roles());
							r.setNoPermissionPage(actionAuth.noPermissionPage());
							r.setLoginPage(actionAuth.loginPage());

							r.setPath(handler.getRESTStylePath());
							r.setActionId(action.getId());

							Permission.addExtDefinedResources(handler.getRESTStylePath() + "->" + action.getId(), r);

							log.log("通过action注解设置权限 -> " + r.toString(), -1);
						}
						System.out.println("扫描到Action注解 -> " + handler.getRESTStylePath() + " -> " + action.getId() + " -> " + methods[i].getName() + " -> getRequestBody:" + action.isGetRequestBody());

						//导航配置（兼容jsp）
						j.core.annotation.action.Navigates navigates = methods[i].getAnnotation(j.core.annotation.action.Navigates.class);
						if(navigates!=null) {
							j.core.annotation.action.Navigate[] _navigates = navigates.value();
							for(int x=0; x<_navigates.length; x++){
								if(!JUtilString.isBlank(_navigates[x].belonged()) && !_navigates[x].belonged().equals(path)) continue;

								Navigate navigate=new Navigate();
								navigate.setCondition(_navigates[x].condition());
								navigate.setType(_navigates[x].type());
								navigate.setUrl(_navigates[x].url());

								action.addNavigate(navigate);
								log.log("扫描到action navigate注解 -> " + navigate.toString(), -1);
							}
						}

						handler.addAction(action);
					}

					handlersByPath.put(handler.getPath(), handler);
					if(!JUtilString.isBlank(handler.getRESTStylePath())) {
						handlersByRESTPath.put(handler.getRESTStylePath(), handler);
					}
				}
			}
			return true;
		}catch (Exception e){
			log.log(e, Logger.LEVEL_FATAL);
			return false;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)
				&&!(resource instanceof ResourceClass)) return false;

		//仅处理actions.xml（旧版配置）或nvwa.xml（新版配置）
		if(resource instanceof ResourceXml){
			if(!resource.getName().startsWith("action")
					&&!resource.getName().equals("nvwa.xml")) return false;
		}

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)
				&&!(resource instanceof ResourceClass)) return false;

		//仅处理actions.xml（旧版配置）或nvwa.xml（新版配置）
		if(resource instanceof ResourceXml){
			if(!resource.getName().startsWith("action")
					&&!resource.getName().equals("nvwa.xml")) return false;
		}

		return load(resource);
	}
}
