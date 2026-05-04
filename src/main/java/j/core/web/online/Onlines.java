package j.core.web.online;

import j.I18N.I18N;
import j.core.Startup;
import j.core.annotation.description.MethodDescription;
import j.core.cache.CachedMap;
import j.core.cache.JCacheParams;
import j.core.common.Global;
import j.core.common.JObject;
import j.core.common.JProperties;
import j.core.nvwa.Nvwa;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.ResourceXml;
import j.core.permission.Permission;
import j.core.permission.Resource;
import j.core.permission.ResourceUrl;
import j.core.permission.Signature;
import j.core.sso.*;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.core.web.handler.Handlers;
import j.core.web.handler.JResponse;
import j.core.web.handler.Router;
import j.core.web.handler.UserAgents;
import j.http.HttpUtil;
import j.log.Logger;
import j.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
@WebFilter(urlPatterns = {"*", "/"}, asyncSupported = true)
public class Onlines implements Filter, Runnable, Consumer {
	private static Logger log=Logger.create(Onlines.class);
	public static final int CHATTING_PENDING=0;//未发起聊天
	public static final int CHATTING_WAITING=1;//已发起聊天，等待客服相应
	public static final int CHATTING_INPROCESS=2;//正在聊天
	public static final int CHATTING_ENDED=3;//聊天结束
	public static final int CHATTING_REFUSED=-1;//被客服拒绝（1次）
	public static final int CHATTING_REFUSED_SESSION=-2;//被客服拒绝（本次会话）

	private static ConcurrentMap<String, RequestCount> counts=new ConcurrentMap();
	private static CachedMap onlines;

	private static int maxRequestsPerMinutes=60;
	private static int maxSessionsPerIp=50;
	private static int maxPostSize=64;
	private static int maxUploadSize=1024;


	private static String[] ignoredIps=new String[]{};
	private static ConcurrentList<String> credibleIps=new ConcurrentList();
	private static ConcurrentList<BlackIp> blackIps=new ConcurrentList();
	private static ConcurrentList<BlackRegion> blackRegions=new ConcurrentList();
	private static ConcurrentList<DomainLimit> domainLimits=new ConcurrentList();
	private static String[] fileUploadAllowedUrls;
	private static String[] ignoredUrls;
	private static String[] forbiddenSpiders;
	private static OnlineHandler handler;

	private static Onlines instance=null;

	//最新配置信息
	private static String config;

	/**
	 * 从请求头或cookie获取客户端标识
	 * @param request
	 * @return
	 */
	public static String getUaId(HttpServletRequest request){
		if(request==null) return null;

		String uaId=request.getHeader(Constants.USER_AGENT_IDENTIFY);
		if(JUtilString.isBlank(uaId)) uaId=SysUtil.getCookie(request, Constants.USER_AGENT_IDENTIFY);
		if(JUtilString.isBlank(uaId) && "true".equals(JProperties.getEnv("SessionRequired"))){
			try{
				HttpSession session = request.getSession();
				uaId = (String)session.getAttribute(Constants.USER_AGENT_IDENTIFY);
			}catch (Exception ignored){}
		}
		if(JUtilString.isBlank(uaId) || !uaId.startsWith("UA_")) uaId=null;
		return uaId;
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static String getAccessToken(HttpServletRequest request){
		if(request==null) return null;

		String accessToken=request.getParameter(Constants.ACCESS_TOKEN);
		if(JUtilString.isBlank(accessToken)) accessToken=SysUtil.getHttpHeader(request, Constants.ACCESS_TOKEN);
		if(JUtilString.isBlank(accessToken)) accessToken=SysUtil.getCookie(request, Constants.ACCESS_TOKEN);
		if(JUtilString.isBlank(accessToken) && "true".equals(JProperties.getEnv("SessionRequired"))){
			try{
				HttpSession session = request.getSession();
				accessToken = (String)session.getAttribute(Constants.ACCESS_TOKEN);
			}catch (Exception ignored){}
		}
		return accessToken;
	}

	/**
	 * 根求请求的客户端信息生产客户端标识
	 * @param request
	 * @return
	 */
	public static String genUaId(HttpServletRequest request){
		return "UA_"+JUtilUUID.genUUID();
	}

	/**
	 *
	 * @return
	 */
	public static OnlineHandler getHandler(){
		return handler;
	}

	/**
	 *
	 * @return
	 */
	public static int getMaxPostSizeKB(){
		return maxPostSize;
	}

	/**
	 *
	 * @return
	 */
	public static int getMaxUploadSizeKB(){
		return maxUploadSize;
	}

	/**
	 *
	 * @param domain
	 * @param url
	 * @return
	 */
	public static boolean pass(String domain,String url){
		return true;
	}


	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(j.core.nvwa.resource.Resource resource){
		try{
			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的ONLINES节点）
			if(root.element("ONLINES")!=null){
				root=root.element("ONLINES");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			if(instance==null){
				instance=new Onlines();
				Thread thread=new Thread(instance);
				thread.start();
				log.log("Onlines started",-1);
			}

			String _maxRequestsPerMinutes=root.elementTextTrim("max-requests-per-minute");
			if(JUtilMath.isInt(_maxRequestsPerMinutes)){
				maxRequestsPerMinutes=Integer.parseInt(_maxRequestsPerMinutes);
				log.log("maxRequestsPerMinutes:"+maxRequestsPerMinutes, -1);
			}

			String _maxSessionsPerIp=root.elementTextTrim("max-sessions-per-ip");
			if(JUtilMath.isInt(_maxSessionsPerIp)){
				maxSessionsPerIp=Integer.parseInt(_maxSessionsPerIp);
				log.log("maxSessionsPerIp:"+maxSessionsPerIp, -1);
			}

			String _maxPostSize=root.elementTextTrim("max-post-size");
			if(JUtilMath.isInt(_maxPostSize)){
				maxPostSize=Integer.parseInt(_maxPostSize);
				log.log("maxPostSize:"+maxPostSize, -1);
			}

			String _maxUploadSize=root.elementTextTrim("max-upload-size");
			if(JUtilMath.isInt(_maxUploadSize)){
				maxUploadSize=Integer.parseInt(_maxUploadSize);
				log.log("maxUploadSize:"+maxUploadSize, -1);
			}


			List eles=root.elements("ignored-ip");
			ignoredIps=new String[eles.size()];
			for(int i=0;i<eles.size();i++){
				Element temp=(Element)eles.get(i);
				ignoredIps[i]=temp.getTextTrim();
			}

			credibleIps.clear();
			eles=root.elements("credible-ip");
			for(int i=0;i<eles.size();i++){
				Element temp=(Element)eles.get(i);
				credibleIps.add(temp.getTextTrim());
			}

			blackIps.clear();
			eles=root.elements("black-ip");
			for(int i=0;i<eles.size();i++){
				Element temp=(Element)eles.get(i);
				blackIps.add(new BlackIp(temp.getTextTrim()));
			}

			blackRegions.clear();
			eles=root.elements("black-region");
			for(int i=0;i<eles.size();i++){
				Element temp=(Element)eles.get(i);
				blackRegions.add(new BlackRegion(temp.getTextTrim()));
			}

			List urls=root.elements("file-upload-allowed-url");
			fileUploadAllowedUrls=new String[urls.size()];
			for(int i=0;i<urls.size();i++){
				Element temp=(Element)urls.get(i);
				fileUploadAllowedUrls[i]=temp.getTextTrim();
			}

			urls=root.elements("ignored-url");
			ignoredUrls=new String[urls.size()];
			for(int i=0;i<urls.size();i++){
				Element temp=(Element)urls.get(i);
				ignoredUrls[i]=temp.getTextTrim();
			}

			List spiders=root.elements("forbidden-spider");
			forbiddenSpiders=new String[spiders.size()];
			for(int i=0;i<spiders.size();i++){
				Element temp=(Element)spiders.get(i);
				forbiddenSpiders[i]=temp.getTextTrim();
			}
			domainLimits.clear();
			List domainLimitElements=root.elements("domain-limit");
			for(int i=0;i<domainLimitElements.size();i++){
				Element temp=(Element)domainLimitElements.get(i);

				DomainLimit limit=new DomainLimit();

				List matches=temp.elements("match");
				for(int j=0;j<matches.size();j++){
					Element temp2=(Element)matches.get(j);
					limit.addMatch(temp2.attributeValue("type"),temp2.getTextTrim());
				}

				List allowedDomains=temp.elements("allowed-domain");
				for(int j=0;j<allowedDomains.size();j++){
					Element temp2=(Element)allowedDomains.get(j);
					limit.addAllowedDomain(temp2.getTextTrim());
				}

				domainLimits.add(limit);
			}

			try{
				String handlerCls=root.elementText("initializer");
				if(!JUtilString.isBlank(handlerCls)){
					handler=(OnlineHandler)Class.forName(root.elementText("initializer")).newInstance();
					log.log("onlines handler => "+handlerCls, -1);
				}
			}catch(Exception e){
				log.log(e,Logger.LEVEL_ERROR);
			}

			root=null;
			doc=null;

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			return false;
		}
	}

	/**
	 * 根据UaId查找
	 * @param request
	 * @return
	 */
	public static Online find(HttpServletRequest request){
		if(request==null) return null;

		init();

		Online online=null;
		try{
			//通过客户端唯一标识查找
			String uaId=getUaId(request);
			if(!JUtilString.isBlank(uaId)){
				OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
				filter.setUaId(uaId);
				online=(Online) onlines.get(new JCacheParams(filter));
				if(online!=null){
					online.setFoundBy(Online.FOUND_BY_USER_AGNET_IDENTIFY);
					return online;
				}
			}

			return null;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static Online findByAccessToken(HttpServletRequest request){
		if(request==null) return null;
		init();

		Online online=null;
		try{
			//通过accessToken查找
			String accessToken=SysUtil.getHttpHeader(request, Constants.ACCESS_TOKEN);
			if(!JUtilString.isBlank(accessToken)){
				OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
				filter.setGlobalSessionId(accessToken);
				online=(Online) onlines.get(new JCacheParams(filter));
				if(online!=null){
					online.setFoundBy(Online.FOUND_BY_GLOBAL_SESSION_ID);
					return online;
				}
			}

			return null;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param uaId
	 * @return
	 */
	public static Online findByUaId(String uaId){
		init();

		Online online=null;
		try{
			//通过客户端唯一标识查找
			if(!JUtilString.isBlank(uaId)){
				OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
				filter.setUaId(uaId);
				online=(Online) onlines.get(new JCacheParams(filter));
				if(online!=null){
					online.setFoundBy(Online.FOUND_BY_USER_AGNET_IDENTIFY);
					return online;
				}
			}

			return null;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param userId
	 * @return
	 */
	public static Online findByUserId(String userId){
		if(JUtilString.isBlank(userId)) return null;
		init();

		OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
		filter.setUid(userId);
		try{
			return (Online)onlines.get(new JCacheParams(filter));
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param userId
	 * @param subUserId
	 * @return
	 */
	public static Online findByUserId(String userId, String subUserId){
		if(JUtilString.isBlank(subUserId)) return findByUserId(userId);
		init();

		OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
		filter.setUid(userId);
		filter.setSubUserId(subUserId);
		try{
			return (Online)onlines.get(new JCacheParams(filter));
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param userId
	 * @return
	 */
	public static Online[] findAllByUserId(String userId){
		if(JUtilString.isBlank(userId)) return null;

		init();

		OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
		filter.setUid(userId);
		try{
			ConcurrentList<Online> of=onlines.values(new JCacheParams(filter));
			if(of==null || of.isEmpty()) return null;

			Online[] found=new Online[of.size()];
			of.toArray(found);
			return found;
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param sessionId
	 * @return
	 */
	public static Online findBySessionId(String sessionId){
		if(JUtilString.isBlank(sessionId)) return null;

		init();

		OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
		filter.setSessionId(sessionId);
		try{
			return (Online)onlines.get(new JCacheParams(filter));
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 * 未登录的会话数
	 * @return
	 */
	public static int sessionsPerIp(String ip){
		if(JUtilString.isBlank(ip)) return 0;

		init();

		OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
		filter.setIp(ip);
		try{
			return onlines.size(new JCacheParams(filter));
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return 0;
		}
	}

	/**
	 *
	 * @return
	 */
	public static List<Online> getActiveUsers(){
		init();

		try{
			return onlines.values(null);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return new ConcurrentList();
		}
	}

	/**
	 *
	 * @param rpp
	 * @param pn
	 * @return
	 */
	public static List<Online> getActiveUsers(int rpp, int pn){
		init();
		JCacheParams params=new JCacheParams();
		params.recordsPerPage=rpp;
		params.pageNum=pn;
		try{
			return  onlines.values(params);
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return new ConcurrentList();
		}
	}

	/**
	 *
	 * @return
	 */
	public static int getActiveUsersTotal(){
		init();
		try{
			return onlines.size();
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return 0;
		}
	}

	/**
	 *
	 * @param online
	 */
	public static void update(Online online){
		init();
		try{
			onlines.put(SysConfig.getSysId()+">"+online.getUaId(), online);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
	}

	/**
	 *
	 * @param ip
	 * @return
	 */
	public static int requestsPerMinute(String ip){
		RequestCount count=counts.get(ip);
		if(count==null){
			return 0;
		}else{
			float minutes=(float)Math.ceil((count.latestRequestTime-count.firstRequestTime)/60000F);
			if(minutes<1) minutes=1;
			float requestsPerMinute=Float.parseFloat(JUtilMath.formatPrint(count.requests/minutes,3));
			return Math.round(requestsPerMinute);
		}
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static boolean credible(HttpServletRequest request){
		String ip=HttpUtil.getRemoteIp(request);

		boolean credible=false;
		if(Permission.hasValidPassport(request)){
			credible=true;
		}else{
			for(int i=0;i<credibleIps.size();i++){
				if(JUtilString.match(ip, credibleIps.get(i), "*")>-1){
					credible=true;
					break;
				}
			}
		}

		return credible;
	}

	/**
	 *
	 * @param ip
	 */
	public static void addCredibleIp(String ip) {
		if(!credibleIps.contains(ip)) credibleIps.add(ip);
	}

	/**
	 *
	 * @param ip
	 * @return
	 */
	public static boolean black(String ip){
		boolean black=false;
		for(int i=0;i<blackIps.size();i++){
			BlackIp bi=blackIps.get(i);
			if(JUtilString.match(ip, bi.ip, "*")>-1){
				black=true;
				break;
			}
		}

		return black;
	}

	/**
	 *
	 * @param ip
	 * @return
	 */
	public static boolean blackRegion(String ip){
		try{
			String region=j.tool.ip.IP.getLocation(ip);
			if(region==null||"".equals(region)) return false;

			boolean black=false;
			for(int i=0;i<blackRegions.size();i++){
				BlackRegion br=(BlackRegion)blackRegions.get(i);
				if(region.indexOf(br.region)>-1){
					black=true;
					break;
				}
			}

			return black;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return false;
		}
	}

	/**
	 *
	 * @param ip
	 * @param timeout
	 */
	public static void addBlackTemporary(String ip,long timeout){
		blackIps.add(new BlackIp(ip,timeout));
	}

	/**
	 *
	 * @param ip
	 */
	public static void addBlackPermanent(String ip){
		blackIps.add(new BlackIp(ip));
	}

	/**
	 *
	 */
	synchronized private static void init(){
		while(!Nvwa.isScanned()){
			log.log("等待Nvwa完成资源扫描......", -1);
			try{
				Thread.sleep(5000);
			}catch(Exception e){}
		}

		if("true".equals(JProperties.getEnv("disableOnlines"))){//禁用Onlines
			return;
		}

		if(onlines==null) {
			try {
				onlines = new CachedMap("j.core.web.online.Onlines.onlines");
			} catch (Exception e) {
				log.log(e, Logger.LEVEL_ERROR);
				Global.sleep1000Millis();
				init();
			}
		}
	}

	@Override
	public void doFilter(ServletRequest _request,
						 ServletResponse _response,
						 FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request=(HttpServletRequest)_request;
		SysUtil.setRequestUriOriginal(request);

		HttpSession httpSession = request.getSession(true);
		HttpServletResponse response=(HttpServletResponse)_response;
		response.setHeader("Cache-Control", "no-cache");

		if("true".equals(JProperties.getEnv("disableOnlines"))){//禁用Onlines
			chain.doFilter(_request,_response);
			return;
		}

		init();

		String ip=HttpUtil.getRemoteIp(request);
		String requestURL=request.getRequestURL().toString();
		requestURL=requestURL.replaceFirst(":"+request.getRemotePort(), "");
		String requestURI=request.getRequestURI();
		if(requestURI.startsWith("/websocket")){//websocket不做处理
			chain.doFilter(_request,_response);
			return;
		}

		//log.log("requestURI => " + requestURI, -1);

		/*
		if(JUtilString.getTokens(JUtilString.getHost(requestURL),".").length<2){
			log.log("ip "+ip+" 非法URL - "+requestURL,Logger.LEVEL_ERROR);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		try{
			if(!requestURL.matches(JUtilString.RegExpHttpUrl)){
				log.log("ip "+ip+" 非法URL - "+requestURL,Logger.LEVEL_ERROR);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			log.log("ip "+ip+" 非法URL - "+requestURL,Logger.LEVEL_ERROR);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		if(!requestURL.matches(JUtilString.RegExpHttpUrl)){
			log.log("ip "+ip+" 非法URL - "+requestURL,Logger.LEVEL_ERROR);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}*/

		//响应方式（跳转网址、或输出字符串）
		int responseType=Constants.RESPONSE_TYPE_STRING;

		try{
			//重启后尝试恢复旧版本session中的accessToken
			String accessToken = Onlines.getAccessToken(request);
			if(!JUtilString.isBlank(accessToken)
					&&"true".equals(JProperties.getEnv("SessionRequired"))){
				try{
					if(httpSession.getAttribute(Constants.ACCESS_TOKEN)==null){
						httpSession.setAttribute(Constants.ACCESS_TOKEN, accessToken);
					}
				}catch (Exception ignored){}
			}

			if(handler!=null  && !handler.doFilterBefore(_request, _response, chain)) {
				return;
			}

			User user=SSOClient.getCurrentUser(request);

			long now=SysUtil.getNow();

			String urlWithoutDomain=requestURL.substring(requestURL.indexOf("/",10));
			String domain=JUtilString.getHost(requestURL);

			String userAgent=request.getHeader("User-Agent");
			if(userAgent==null) userAgent="";

			String referer=request.getHeader("Referer");

			String method=request.getMethod();
			if(method==null) method="POST";

			String contentType=request.getContentType();
			if(contentType==null) contentType="";

			int contentLength=request.getContentLength();

			/////////////////////////////////////////安全控制////////////////////////////////////
			if(!Onlines.pass(domain,urlWithoutDomain)){//域名限制
				log.log("域名限制："+domain+","+urlWithoutDomain,Logger.LEVEL_ERROR);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			if(black(ip)){//黑IP
				log.log("黑名单IP："+ip,Logger.LEVEL_ERROR);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			/*
			if(blackRegion(ip)){//黑地区
				log.log("黑名单地区："+ip,Logger.LEVEL_ERROR);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}*/

			//是否允许通过
			if(handler!=null&&!handler.canPass(request)){
				log.log("被业务规则禁止访问："+handler.getClass().getCanonicalName(),Logger.LEVEL_ERROR);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			boolean credible=credible(request);
			if(!credible){
				//被禁止的搜索引擎
				if(userAgent!=null && forbiddenSpiders!=null && forbiddenSpiders.length>0){
					userAgent=userAgent.toLowerCase();
					for(int i=0;i<forbiddenSpiders.length;i++){
						if(forbiddenSpiders[i].startsWith("EQUALS")){
							if(forbiddenSpiders[i].substring(6).equalsIgnoreCase(userAgent)){
								log.log("被禁止的User-Agent："+userAgent,Logger.LEVEL_ERROR);
								response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
								return;
							}
						}else if(userAgent.indexOf(forbiddenSpiders[i])>-1){
							log.log("被禁止的User-Agent："+userAgent,Logger.LEVEL_ERROR);
							response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
							return;
						}
					}
				}
				//被禁止的搜索引擎  end

				//每个ip会话数
				int sessionsOfIp=sessionsPerIp(ip);
				if(sessionsOfIp>maxSessionsPerIp){
					if(handler!=null){
						handler.onManySessionsOnIp(ip);
					}
					log.log("ip "+ip+" 上共有  "+sessionsOfIp+" 个会话，超出限制 "+maxSessionsPerIp,Logger.LEVEL_ERROR);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}
			}

			boolean ignore=false;
			if(ignoredIps!=null
					&&JUtilString.contain(ignoredIps, ip)){
				ignore=true;
			}

			if(!ignore
					&&Permission.hasValidPassport(request)){
				ignore=true;
			}

			boolean ignoreUrl=false;
			if(!ignore
					&&ignoredUrls!=null){
				for(int i=0;i<ignoredUrls.length;i++){
					if(JUtilString.match(requestURI, ignoredUrls[i],"*")>-1){
						ignore=true;
						ignoreUrl=true;
						break;
					}
				}
			}

			if(!ignoreUrl){
				//文件上传是否允许
				if(contentType.indexOf("boundary")>-1){
					if(user==null){//未登录
						boolean allowed=false;
						for(int i=0;i<fileUploadAllowedUrls.length;i++){
							if(JUtilString.match(requestURI, fileUploadAllowedUrls[i], "*")>-1){
								allowed=true;
								break;
							}
						}

						if(!allowed){
							log.log("ip "+ip+" 未登录, 试图上传文件("+requestURI+") ，大小 "+contentLength+" ，已被禁止.", Logger.LEVEL_ERROR);

							SysUtil.outHttpResponse(response, "-login");
							return;
						}
					}

					if(contentLength<0||contentLength>maxUploadSize*1024){
						log.log("ip "+ip+" 试图上传文件 ("+requestURI+")，大小 "+contentLength+" 超过 "+ (maxUploadSize*1024) +"("+maxUploadSize+"K)，已被禁止.", Logger.LEVEL_ERROR);

						SysUtil.outHttpResponse(response, "-max-upload-size-"+maxUploadSize);
						return;
					}
				}else if(user==null
						&&"POST".equalsIgnoreCase(method)
						&&contentLength>maxPostSize*1024
						&&!Permission.hasValidPassport(request)){
					log.log("ip "+ip+" 发起POST请求("+requestURI+") ，大小 "+contentLength+" 超过 "+ (maxPostSize*1024) +"("+maxPostSize+"K)，已被禁止.", Logger.LEVEL_ERROR);

					SysUtil.outHttpResponse(response, "-max-post-size-"+maxPostSize);
					return;
				}
			}

			if(!credible&&!ignoreUrl){
				//频繁访问
				RequestCount count=counts.get(ip);
				if(count==null){
					count=new RequestCount();
					count.firstRequestTime=now;
					count.latestRequestTime=now;
					count.requests=1;
					counts.put(ip, count);
				}else{
					if(now-count.latestRequestTime>=SSOConfig.getOnlineActiveTime()*1000){//状态为离线了，重新计时
						count.firstRequestTime=now;
						count.latestRequestTime=now;
						count.requests=1;
					}else{
						count.latestRequestTime=now;
						count.requests++;
					}
					counts.put(ip, count);
				}

				double minutes=Math.ceil((double)(count.latestRequestTime-count.firstRequestTime)/(double)60000);
				if(minutes<1) minutes=1;
				double requestsPerMinute=Double.parseDouble(JUtilMath.formatPrint(count.requests/minutes,3));
				if(requestsPerMinute>maxRequestsPerMinutes){
					log.log("ip "+ip+" 共在线 "+minutes+" 分钟，请求  "+count.requests+" 次，平均每分钟 "+requestsPerMinute+" 次，超出限制 "+maxRequestsPerMinutes,Logger.LEVEL_ERROR);

					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}
				//频繁访问 END
			}
			/////////////////////////////////////////安全控制 end////////////////////////////////////

			String sign=request.getHeader(Constants.SIGNATURE);
			if(JUtilString.isBlank(sign) && !response.isCommitted()) {//只有头信息中不包含签名才处理（否则是系统间的api调用）
				//登录处理
				//从参数获取accessToken（通过跳转自动登录）、或从http头获取（集成环境下自动登录）
				accessToken=getAccessToken(request);

				//从缓存寻找登录信息（SSO Server异步发送登录通知时创建的）
				LoginStatus loginStatus=SSOClient.findLoginStatusOfAccessToken(accessToken);

				//如果当前未登录用户，且指定了accessToken
				if(user==null && !JUtilString.isBlank(accessToken)){
					if(accessToken.startsWith("jis:")){
						try{
							accessToken=JObject.intSequence2String(accessToken);
						}catch(Exception e){}
					}

					//如果找到登录信息，但用户未登录，则尝试加载用户信息
					if(loginStatus!=null){
						user=User.loadUser(request, loginStatus.getUserId(), loginStatus.getSubUserId(), accessToken);//加载用户信息
						if(user!=null){//加载用户信息成功
							loginStatus.login();//确认登录
							loginStatus.setUpdateTime(SysUtil.getNow());
							loginStatus.setLoginFromDomain(SysUtil.getHttpDomain(request));
							loginStatus.setUserAgent(request.getHeader("User-Agent"));
							SSOClient.saveLoginStatus(loginStatus, user);
						}else{
							log.log("已经登录，但加载用户信息失败 - "+accessToken+","+loginStatus.getUserId()+","+loginStatus.getSubUserId(),-1);
						}
					}
				}
				//处理登录 end

				/////////////////////////////////////////在线用户处理////////////////////////////////////
				String requestLine = "";
				if ("POST".equalsIgnoreCase(method)) {
					requestLine = method + " " + contentType + " " + contentLength + " " + SysUtil.getRequestURL(request);
				} else {
					requestLine = method + " " + contentType + " " + SysUtil.getRequestURL(request);
				}

				synchronized(httpSession) {
					String uaId = Onlines.getUaId(request);
					Online online = find(request);
					if (online != null) {
						if (user == null && !JUtilString.isBlank(online.getUid())) {//已经登出
							log.log("在线用户登出，uaId => " + online.getUaId() + "，uid => " + online.getUid() + "，url => " + requestURL+", accessToken => "+Onlines.getAccessToken(request), -1);
							online.setUid(null);
							online.setUname(null);
							online.setGlobalSessionId(null);
							online.setCurrentSessionId(null);

							if (handler != null) {
								handler.onLogout(online, user, request);
							}
						} else if (user != null && JUtilString.isBlank(online.getUid())) {//刚登入
							log.log("在线用户登入，uaId => " + online.getUaId() + "，uid => " + user.getUserId() + "，accessToken => " + (loginStatus==null?"(loginStatus is null)":loginStatus.getAccessToken()), -1);
							online.setUid(user.getUserId());
							online.setSubUserId(user.getSubUserId());
							online.setUname(user.getUserName());
							if(loginStatus != null) {
								online.setGlobalSessionId(loginStatus.getAccessToken());
								online.setCurrentSessionId(loginStatus.getAccessToken());
							}

							if (handler != null) {
								handler.onLogin(online, user, request);
							}
						}
					} else {
						if (JUtilString.isBlank(uaId) || !uaId.startsWith("UA_")) uaId = genUaId(request);

						online = new Online();

						//分配客户端唯一标识
						online.setUaId(uaId);

						//保存到session（兼容旧版本）
						if ("true".equals(JProperties.getEnv("SessionRequired")) && request != null) {
							try {
								httpSession.setAttribute(Constants.USER_AGENT_IDENTIFY, uaId);
							} catch (Exception ignored) {}
						}

						if (handler != null) {
							handler.onInit(online, user, request);
						}

						if (user != null) {
							if(loginStatus != null) {
								online.setGlobalSessionId(loginStatus.getAccessToken());
								online.setCurrentSessionId(loginStatus.getAccessToken());
							}
							online.setUid(user.getUserId());
							online.setSubUserId(user.getSubUserId());
							online.setUname(user.getUserName());

							if (handler != null) {
								handler.onLogin(online, user, request);
							}
						}

						if (!JUtilString.isBlank(online.getUid())) {
							log.log("创建新的在线用户，uaId => " + uaId + "，uid => " + online.getUid() + "，accessToken => " + (loginStatus==null?"(loginStatus is null)":loginStatus.getAccessToken()), -1);
						}
					}

					if (referer == null) referer = "";
					if (userAgent == null) userAgent = "";

					online.setCurrentIp(ip);
					online.setCurrentSysId(SysConfig.getSysId());
					online.setCurrentMachineId(SysConfig.getMachineID());
					online.setCurrentUrl(requestLine);
					online.setCurrentReferer(referer);
					online.setCurrentUserAgent(userAgent);
					online.setCurrentUserAgentType(UserAgents.getUserAgentType(request));

					String currency = SysUtil.getHttpHeader(request, Constants.J_CURRENCY);
					if (!JUtilString.isBlank(currency)) online.setCurrentCurrency(currency);

					String lang = SysUtil.getHttpHeader(request, Constants.I18N_LANGUAGE);
					if (!JUtilString.isBlank(lang)) online.setCurrentLang(lang);

					//客户端唯一标识通过cookie发送给客户端，后续请求需带上这个cookie（或通过http头进行设置）
					response.setHeader(Constants.USER_AGENT_IDENTIFY, online.getUaId());

					online.update();
					update(online);
				}
				/////////////////////////////////////////在线用户处理 end////////////////////////////////////
			}

			/////////////////////////////////////////权限处理////////////////////////////////////
			//持有通行证，不进行权限认证和SSO相关操作（过时的机制，暂时保留，兼容旧版本）
			if(Permission.hasValidPassport(request)){
				chain.doFilter(_request,_response);
				return;
			}

			//机器人检测
			Resource res=Permission.matchResource(request);
			if(res!=null && res.isRobotInspectEnabled() && !Permission.getRobotInspector().pass(request)){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			//是否是需要认证的资源
			res=Permission.permission(request, user);

			//是否action
			String isAction=Handlers.isActionPath(requestURI);

			//如果需要认证（仅处理网址类型资源， 其它分别在各自网关/调度中心中处理）
			if(res!=null && (res instanceof ResourceUrl) && isAction==null){
				boolean passed=true;

				if("signature".equals(res.getPolicy())){//使用签名验证机制
					String accessKey = SysUtil.getHttpHeader(request, Constants.ACCESS_KEY);
					if (StringUtils.isBlank(accessKey)) {
						passed=false;
					}

					if(passed){
						//client
						Client client = SSOConfig.getSsoClientByAccessKey(accessKey);

						//client不存在
						if (client == null) passed=false;

						if(passed){
							//验签未通过
							if (!Signature.verify(request, client.getAccessSecret())) {
								passed=false;
							}
						}
					}
				}else{//基于角色控制权限
					passed=res.isUserInRole(user);
				}

				if(!passed){
					if("signature".equals(res.getPolicy())){
						if(Nvwa.isDebug()) log.log("试图访问没有权限的资源:"+res+",验签失败,"+requestURL+" "+isAction+" -> "+JUtilBean.map2Json(SysUtil.getHttpParameterMap(request)), Logger.LEVEL_FATAL);
						SysUtil.outHttpResponse(response, (new JResponse(false, "signature_error", "")).toString());
					}else{
						if(Nvwa.isDebug()) log.log("试图访问没有权限的资源:"+res+","+(user==null?"未登录":user.getUserId())+","+requestURL, Logger.LEVEL_FATAL);

						if(user==null){//未登录
							if(!JUtilString.isBlank(res.getLoginPage())) SysUtil.redirect(request, response, res.getLoginPage());
							else SysUtil.outHttpResponse(response, (new JResponse(false, "non_login", "")).toString());
						}else{//无权限
							if(!JUtilString.isBlank(res.getNoPermissionPage())) SysUtil.redirect(request, response, res.getNoPermissionPage());
							else SysUtil.outHttpResponse(response, (new JResponse(false, "access_denied", "")).toString());
						}
					}

					return;
				}
			}
			/////////////////////////////////////////权限处理  end////////////////////////////////////


			//业务自定义处理
			if(handler!=null && !handler.doFilterAfter(_request, _response, chain)){
				return;
			}

			//如果是action，跳转在j.app.webserver.Server中处理
			//通过request.setAttribute("forwarded")，避免死循环forward
			if(Handlers.isActionPath(requestURI)==null && request.getAttribute("forwarded")==null){
				String fetchUrl=requestURI;

				UrlAndFetchType fetchUrlAndType=handler==null?null:handler.adjustUrl(request, requestURI);
				if(fetchUrlAndType!=null) fetchUrl=fetchUrlAndType.getUrl();
				if(!Router.jspExists(JUtilString.getUri(fetchUrl))){
					response.sendError(404);
					return;
				}

				if(!fetchUrl.equals(requestURI)) {//处理后的url有变化
					request.setAttribute("forwarded", "true");
					if(fetchUrlAndType!=null && fetchUrlAndType.getFetchType()==UrlAndFetchType.TYPE_REDIRECT) {
						//log.log("redirect 2 fetchUrl => "+fetchUrl+" ... ", -1);
						SysUtil.redirect(request, response, fetchUrl);
						return;
					}else {
						//log.log("forward 2 fetchUrl => "+fetchUrl+" ... ", -1);
						if(!I18N.enabled||!I18N.need(request)) SysUtil.forward(request, response, fetchUrl);
						else SysUtil.forwardI18N(request, response, fetchUrl);
						return;
					}
				}
			}

			chain.doFilter(_request,_response);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);

			log.log("errors on url \r\n"+requestURL+"\r\n"+requestURL,Logger.LEVEL_ERROR);

			if(responseType == Constants.RESPONSE_TYPE_REDIRECT){//跳转
				SysUtil.redirect(request,response,SysConfig.errorPage);
			}else{
				SysUtil.outHttpResponse(response, (new JResponse(false, "errors", "")).toString());
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {

	}


	/*
	 * (non-Javadoc)
	 * @see jakarta.servlet.Filter#destroy()
	 */
	public void destroy() {
		//nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		JCacheParams params=new JCacheParams();
		params.valueFilter=new OnlineRemover();

		while(!Startup.isDestroyed()){
			try{
				Thread.sleep(5000);
			}catch(Exception e){}

			if("true".equals(JProperties.getEnv("disableOnlines"))){//禁用Onlines
				return;
			}

			init();

			try {
				//移除过期对象
				long now = SysUtil.getNow();
				OnlineFilter filter=new OnlineFilter(SysConfig.getSysId());
				List keys = onlines.keys(new JCacheParams(filter));
				filter=null;
				if(keys == null) continue;

				for (int i = 0; i < keys.size(); i++) {
					String key = (String) keys.get(i);
					Online o = (Online)onlines.get(key);
					if (o == null || now - o.getUpdateTime() > SSOConfig.getOnlineActiveTime() * 1000) {
						onlines.remove(key);
					}
				}
				keys.clear();
				keys = null;

				//移除过期访问记录
				keys = counts.listKeys();
				for (int i = 0; i < keys.size(); i++) {
					String ip = (String) keys.get(i);
					RequestCount c = counts.get(ip);
					if (c == null || now - c.latestRequestTime > SSOConfig.getOnlineActiveTime() * 1000) {//5分钟
						counts.remove(ip);
					}
				}
				keys.clear();
				keys = null;
			}catch (Exception e){
				log.log(e, Logger.LEVEL_ERROR);
			}
		}
	}

	@Override
	public boolean onFound(j.core.nvwa.resource.Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理onlines.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("onlines.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(j.core.nvwa.resource.Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理onlines.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("onlines.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}
}

/**
 * 黑名单IP
 */
class BlackIp{
	public String ip;
	public long timeout=0;
	public long createTime=0;

	public BlackIp(String ip){
		this.ip=ip;
		this.timeout=-1;
		this.createTime=SysUtil.getNow();
	}

	public BlackIp(String ip,long timeout){
		this.ip=ip;
		this.timeout=timeout;
		this.createTime=SysUtil.getNow();
	}

	public boolean isTimeout(){
		if(this.timeout<=0) return false;
		return (SysUtil.getNow()-this.createTime>=this.timeout);
	}
}

/**
 * 黑名单地域
 */
class BlackRegion{
	public String region;
	public long timeout=0;
	public long createTime=0;

	public BlackRegion(String region){
		this.region=region;
		this.timeout=-1;
		this.createTime=SysUtil.getNow();
	}

	public BlackRegion(String region,long timeout){
		this.region=region;
		this.timeout=timeout;
		this.createTime=SysUtil.getNow();
	}

	public boolean isTimeout(){
		if(this.timeout<=0) return false;
		return (SysUtil.getNow()-this.createTime>=this.timeout);
	}
}

/**
 * 域名限定
 */
class DomainLimit{
	public List matches=new LinkedList();
	public List allowedDomains=new LinkedList();

	public DomainLimit(){

	}

	public void addMatch(String type,String pattern){
		matches.add(type+"^"+pattern);
	}

	public void addAllowedDomain(String pattern){
		allowedDomains.add(pattern);
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public boolean matches(String url){
		if(matches.isEmpty()) return false;

		for(int i=0;i<matches.size();i++){
			String[] match=((String)matches.get(i)).split("\\^");
			if(match[1].indexOf("*")>-1||match[0].equals("matches")){
				if(JUtilString.match(url, match[1], "*")>-1) return true;
			}else if(match[0].equals("startsWith")){
				if(url.startsWith(match[1])) return true;
			}else if(match[0].equals("equals")){
				if(url.equals(match[1])) return true;
			}else if(match[0].equals("contains")){
				if(url.indexOf(match[1])>-1) return true;
			}
		}

		return false;
	}

	/**
	 *
	 * @param domain
	 * @return
	 */
	public boolean allowed(String domain){
		if(allowedDomains.isEmpty()) return false;

		for(int i=0;i<allowedDomains.size();i++){
			String allowedDomain=(String)allowedDomains.get(i);
			if("*".equals(allowedDomain)) return true;

			if(allowedDomain.indexOf("*")>-1){
				if(JUtilString.match(domain, allowedDomain, "*")>-1) return true;
			}else{
				if(domain.equalsIgnoreCase(allowedDomain)) return true;
			}
		}

		return false;
	}
}
