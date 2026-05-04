package j.core.sys;

import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.web.handler.Handler;
import j.core.web.handler.Handlers;
import j.log.Logger;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.util.JUtilString;
import org.dom4j.Document;
import org.dom4j.Element;

import jakarta.servlet.http.HttpServletRequest;


/**
 * @author 肖炯
 *
 */
public class SysConfig implements Consumer {	
	private static Logger log=Logger.create(SysConfig.class);
	
	private static String sysId;//系统ID，也是作为sso client的client ID
	
	private static String machineId;//物理服务器ID
	
	public static String sysEncoding="UTF-8";//字符编码
	
	public static String[] responseEncodingPages=new String[0];//哪些页面需调用response.setContentType("text/html;charset="+SysConfig.sysEncoding)

	public static String errorPage;//发生错误时转向页面

	public static boolean useUtcTime=false;
	
	//AES加密配置
	private static String AES_KEY;
	private static String AES_OFFSET;

	@FieldDescription(description = "最新配置信息")
	private static String config;
	
	/**
	 * 
	 * @return
	 */
	public static String getSysId(){
		return sysId;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getMachineID(){
		return machineId;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getAesKey() {
		return AES_KEY;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getAesOffset() {
		return AES_OFFSET;
	}

	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource){
		try{
			log.log("load sys config -> "+resource.getPath(), -1);

			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的SYS节点）
			if(root.element("SYS")!=null){
				root=root.element("SYS");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			//系统ID
			SysConfig.sysId=root.elementText("sys-id");
			log.log("SysConfig.sysId:"+SysConfig.sysId,-1);

			//物理服务器ID
			SysConfig.machineId=root.elementText("machine-id");
			if(SysConfig.machineId==null) SysConfig.machineId=SysConfig.sysId;
			log.log("SysConfig.machineId:"+SysConfig.machineId,-1);

			//错误信息页面
			SysConfig.errorPage=root.elementText("error-page");
			log.log("SysConfig.errorPage:"+SysConfig.errorPage,-1);

			//使用utc时间
			SysConfig.useUtcTime="true".equalsIgnoreCase(root.elementText("use-utc-time"));
			log.log("SysConfig.useUtcTime:"+SysConfig.useUtcTime,-1);

			//系统编码
			SysConfig.sysEncoding=root.elementText("sys-encoding");
			log.log("SysConfig.sysEncoding:"+SysConfig.sysEncoding,-1);

			//哪些页面需调用response.setContentType("text/html;charset="+SysConfig.sysEncoding)
			String responseEncodingPagesStr=root.elementText("responseEncodingPages");
			if(responseEncodingPagesStr!=null){
				responseEncodingPages=responseEncodingPagesStr.split(";");
			}
			log.log("SysConfig.responseEncodingPages:"+root.elementText("responseEncodingPages"),-1);

			//security
			Element security=root.element("security");
			if(security==null) {
				AES_KEY=null;
				AES_OFFSET=null;
			}else {
				AES_KEY=security.elementText("AES-KEY");
				AES_OFFSET=security.elementText("AES_OFFSET");
			}
			log.log("security.AES_KEY:"+AES_KEY,-1);
			log.log("security.AES_OFFSET:"+AES_OFFSET,-1);

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			return false;
		}
	}

	/**
	 *
	 * @param requestURI
	 * @return
	 */
	public static boolean needSettingResponseEncoding(String requestURI){
		for(int i=0;i<responseEncodingPages.length;i++){
			if("".equals(responseEncodingPages[i])) continue;
			if(JUtilString.match(requestURI,responseEncodingPages[i],"*")>-1){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getUserAgentType(HttpServletRequest request){
		String userAgent=request.getHeader("User-Agent");
		if(userAgent==null) userAgent="";
		
		if ((userAgent.indexOf("MSIE") >= 0) 
				&& (userAgent.indexOf("Opera") < 0) 
				&& (userAgent.indexOf("MSIE 9.0") < 0)
				&& (userAgent.indexOf("MSIE 10") < 0)
				&& (userAgent.indexOf("rv:11.0) like Gecko") < 0)){
				return "IE";
			}else if (userAgent.indexOf("MSIE 9.0") >= 0){
				return "IE9";
			}else if (userAgent.indexOf("MSIE 10") >= 0){
				return "IE10";
			}else if (userAgent.indexOf("rv:11.0) like Gecko") >= 0){
				return "IE11";
			}else if (userAgent.indexOf("Firefox") >= 0){
				return "FIREFOX";
			}else if (userAgent.indexOf("Opera") >= 0){
				return "OPERA";
			}else if (userAgent.indexOf("Chrome") >= 0){
				return "CHROME";
			}else{
				return "OTHER";	
			}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理sys.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("sys.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		return true;
	}
} 