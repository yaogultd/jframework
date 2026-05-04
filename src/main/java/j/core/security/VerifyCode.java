package j.core.security;

import j.core.Startup;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.cache.CachedMap;
import j.core.service.ServiceResponse;
import j.core.sys.SysConfig;
import j.log.Logger;
import j.core.nvwa.Nvwa;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.core.service.client.Client;
import j.core.sys.SysUtil;
import j.util.*;
import org.apache.commons.io.LineIterator;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
public class VerifyCode implements Consumer{
	private static Logger log=Logger.create(VerifyCode.class);
	public static final int TYPE_NUMBER=1;//验证码字符组成：纯数字
	public static final int TYPE_CHAR=2;//验证码字符组成：纯字母
	public static final int TYPE_MIXED=3;//验证码字符组成：数字、字母任意组合

	private static volatile long TIMEOUT_SHORT=300000L;//超时：5分钟
	private static volatile long TIMEOUT_LONG=1800000L;//超时：半小时

	private static volatile long INTERVAL_SHORT=60000;//发送间隔限制：60秒
	private static volatile long INTERVAL_LONG=300000;//发送间隔限制：5分钟

	private static volatile int MAX_SURVIVALS_PER_IP=5;//每个IP上最多可存在多少个未超时的验证码

	private static String cacheId="j.core.security.VerifyCode";
	private static String config;//最新配置信息

	/**
	 *
	 * @return
	 */
	public static String getCacheId(){
		return SysConfig.getSysId() + "." + cacheId;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public static long getTimeoutA(){
		return TIMEOUT_SHORT;
	}

	/**
	 *
	 * @return
	 */
	public static long getTimeoutShort(){
		return TIMEOUT_SHORT;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public static long getTimeoutB(){
		return TIMEOUT_LONG;
	}

	/**
	 *
	 * @return
	 */
	public static long getTimeoutLong(){
		return TIMEOUT_LONG;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public static long getIntervalA(){
		return INTERVAL_SHORT;
	}

	/**
	 *
	 * @return
	 */
	public static long getIntervalShort(){
		return INTERVAL_SHORT;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public static long getIntervalB(){
		return INTERVAL_LONG;
	}

	/**
	 *
	 * @return
	 */
	public static long getIntervalLong(){
		return INTERVAL_LONG;
	}

	/**
	 *
	 * @return
	 */
	public static int getMaxSurvivalsPerIp(){
		return MAX_SURVIVALS_PER_IP;
	}
	
	/**
	 * 
	 * @param uuid
	 * @param interval
	 * @return
	 */
	public static long can(String uuid, long interval){
		try{
			VerifyCodeService service=(VerifyCodeService)j.core.service.ServiceAdapter.getService(VerifyCodeService.class);

			ServiceResponse<VerifyCodeBean> response=service.exists(uuid);
			if(response==null || response.getResponse()==null) return -1;

			VerifyCodeBean vcb=response.getResponse();
			return (SysUtil.getNow()-vcb.getTime()>=interval) ? 0 : (interval- (SysUtil.getNow() - vcb.getTime()));
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return interval;
		}
	}

	/**
	 *
	 * @param uuid
	 * @param related
	 * @param ip
	 * @param type
	 * @param length
	 * @param timeout 验证码超时时间
	 * @param interval 两次发送验证码最小间隔
	 * @param maxTries 最多试错次数
	 * @return
	 * @throws Exception
	 */
	public static VerifyCodeBean get(String uuid, String related, String ip, int type, int length, long timeout, long interval, int maxTries) throws Exception{
		if(JUtilString.isBlank(uuid)) return null;
		if(JUtilString.isBlank(related)) return null;
		try{
			VerifyCodeService service=(VerifyCodeService)j.core.service.ServiceAdapter.getService(VerifyCodeService.class);
			ServiceResponse<VerifyCodeBean> response = service.get(uuid,
					related,
					ip,
					type,
					length,
					timeout,
					interval,
					maxTries);
			return response==null?null:response.getResponse();
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param uuid
	 * @param code
	 * @param remove
	 * @return
	 */
	public static boolean check(String uuid,String related,String code,boolean remove){
		if(JUtilString.isBlank(uuid)) return false;
		if(JUtilString.isBlank(code)) return false;

		//如果和沙箱通用验证码匹配
		if(code.equals(Nvwa.getParameter("SandBoxSettings", "allowedVerifyCode"))){
			return true;
		}
		
		try{
			VerifyCodeService service=(VerifyCodeService) j.core.service.ServiceAdapter.getService(VerifyCodeService.class);
			ServiceResponse<Boolean> response=service.check(uuid, related, code);

			boolean ok=response==null || response.getResponse()==null ? false : response.getResponse();


			if(remove && ok){
				//验证成功，则移除
				service.remove(uuid);
			}else if(remove && response!=null && "too_many_tries".equals(response.getCode())){
				//验证失败，且超过最大尝试次数，则移除
				service.remove(uuid);
			}

			return ok;
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
			return false;
		}
	}
	
	/**
	 *
	 * @param uuid
	 * @return
	 */
	public static VerifyCodeBean exists(String uuid){
		if(JUtilString.isBlank(uuid)) return null;
		try{
			VerifyCodeService service=(VerifyCodeService) j.core.service.ServiceAdapter.getService(VerifyCodeService.class);
			ServiceResponse<VerifyCodeBean> response = service.exists(uuid);
			return response==null?null:response.getResponse();
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
			return null;
		}
	}
	
	/**
	 *
	 * @param uuid
	 */
	public static void remove(String uuid){
		if(JUtilString.isBlank(uuid)) return;
		try{
			VerifyCodeService service=(VerifyCodeService) j.core.service.ServiceAdapter.getService(VerifyCodeService.class);
			service.remove(uuid);
		}catch(Exception ex){
			log.log(ex,Logger.LEVEL_ERROR);
		}
	}


	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource){
		try{
			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的VerifyCode节点）
			if(root.element("VerifyCode")!=null){
				root=root.element("VerifyCode");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			long timeoutShort=0;
			if(JUtilMath.isLong(root.elementText("TIMEOUT_A"))) timeoutShort=Long.parseLong(root.elementText("TIMEOUT_A"));
			else if(JUtilMath.isLong(root.elementText("TIMEOUT_SHORT"))) timeoutShort=Long.parseLong(root.elementText("TIMEOUT_SHORT"));
			if(timeoutShort>0) VerifyCode.TIMEOUT_SHORT=timeoutShort;

			long timeoutLong=0;
			if(JUtilMath.isLong(root.elementText("TIMEOUT_B"))) timeoutLong=Long.parseLong(root.elementText("TIMEOUT_B"));
			else if(JUtilMath.isLong(root.elementText("TIMEOUT_LONG"))) timeoutLong=Long.parseLong(root.elementText("TIMEOUT_LONG"));
			if(timeoutLong>0) VerifyCode.TIMEOUT_LONG=timeoutLong;

			long intervalShort=0;
			if(JUtilMath.isLong(root.elementText("INTERVAL_A"))) intervalShort=Long.parseLong(root.elementText("INTERVAL_A"));
			else if(JUtilMath.isLong(root.elementText("INTERVAL_SHORT"))) intervalShort=Long.parseLong(root.elementText("INTERVAL_SHORT"));
			if(intervalShort>0) VerifyCode.INTERVAL_SHORT=intervalShort;

			long intervalLong=0;
			if(JUtilMath.isLong(root.elementText("INTERVAL_B"))) intervalLong=Long.parseLong(root.elementText("INTERVAL_B"));
			else if(JUtilMath.isLong(root.elementText("INTERVAL_LONG"))) intervalLong=Long.parseLong(root.elementText("INTERVAL_LONG"));
			if(intervalLong>0) VerifyCode.INTERVAL_LONG=intervalLong;

			int maxSurvivalsPerIp=0;
			if(JUtilMath.isInt(root.elementText("MAX_SURVIVALS_PER_IP"))) maxSurvivalsPerIp=Integer.parseInt(root.elementText("MAX_SURVIVALS_PER_IP"));
			if(maxSurvivalsPerIp>0) VerifyCode.MAX_SURVIVALS_PER_IP=maxSurvivalsPerIp;

			if(!JUtilString.isBlank(root.elementText("cache-id"))) VerifyCode.cacheId=root.elementText("cache-id");

			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理VerifyCode.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("VerifyCode.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理VerifyCode.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("VerifyCode.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}
}

