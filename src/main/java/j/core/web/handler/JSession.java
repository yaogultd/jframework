package j.core.web.handler;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import j.core.annotation.description.MethodDescription;
import j.core.annotation.description.ParameterDescription;
import j.util.JUtilBean;
import j.util.JUtilString;
import j.util.JUtilTimestamp;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.*;

public class JSession{
	public String method =null;//用户请求的哪个方法
	public String result=null;//处理结果

	@Getter
	private Map<String, String> parameters=new HashMap<>();

	@Getter
	private Map<String, String> headers=new HashMap<>();

	/*
	 * 如果在<request>中定义了属性print-directly="true"，通过PrintWriter.out直接将字符串内容返回给用户
	 */
	public String resultString=null;
	
	//取代resultString，提供json格式的返回内容
	public JResponse jresponse=null;
	
	/*
	 * 有时需要根据处理结果返回不同的地址，而且这个地址是动态的，这时可设置dynamicBackUrl，
	 * 而忽略<request>中定义的navigate-url，如dynamicBackUrl为null则根据<request>中定义的navigate-url进行跳转
	 */
	private String dynamicBackUrl=null;
	
	private boolean isBackToGlobalNavigation=false;//是否根据全局导航配置进行跳转（而不是按对应<request>中定义的）

	private String requestBody=null;

	//MCP
	@Getter
	@Setter
	private McpSyncServerExchange exchange;
	
	public JSession(String method){
		this.method = method;
	}

	/**
	 * 
	 * @param requestBody
	 */
	public void setRequestBody(String requestBody) {
		this.requestBody=requestBody;
	}

	/**
	 * 
	 * @return
	 */
	public String getRequestBody() {
		return this.requestBody;
	}
	
	/**
	 * 动态设置返回URL
	 * @param url
	 */
	public void setDynamicBackUrl(String url){
		dynamicBackUrl=url;
	}
	
	public String getDynamicBackUrl(){
		return this.dynamicBackUrl;
	}
	
	/**
	 * 是否按全局导航配置返回
	 * @return
	 */
	public boolean getIsBackToGlobalNavigation(){
		return this.isBackToGlobalNavigation;
	}	
	public void setIsBackToGlobalNavigation(boolean _isBackToGlobalNavigation){
		this.isBackToGlobalNavigation=_isBackToGlobalNavigation;
	}

	public void setParameter(String name, String value){
		parameters.put(name, value);
	}

	public void setParameters(Map<String, String> params){
		if(parameters==null || parameters.isEmpty()) return;;
		parameters.putAll(params);
	}

	public String getParameter(String name){
		return parameters.get(name);
	}

	public String getParameter(String name, String defaultValue){
		String value = this.getParameter(name);
		return value==null?defaultValue:value;
	}

	public String getParameterLowerCase(String name){
		String value = this.getParameter(name);
		return value==null ? null : value.toLowerCase();
	}

	public String getParameterLowerCase(String name, String defaultValue){
		String value = this.getParameter(name, defaultValue);
		return value==null ? null : value.toLowerCase();
	}

	public String getParameterUpperCase(String name){
		String value = this.getParameter(name);
		return value==null ? null : value.toUpperCase();
	}

	public String getParameterUpperCase(String name, String defaultValue){
		String value = this.getParameter(name, defaultValue);
		return value==null ? null : value.toUpperCase();
	}

	public void addParameters(Map<String, String> _parameters){
		if(_parameters==null || _parameters.isEmpty()) return;
		parameters.putAll(_parameters);
	}

	@MethodDescription(description = "将带下划线的参数名，按(数据库列名 -> 关联类字段名)的规则，将参数名转为(列名)另存一份，前提是不存在该列名的参数")
	public void storeParametersCompatible(){
		List<String> originalKeys = new ArrayList<>();
		for(Iterator<String> keys = parameters.keySet().iterator(); keys.hasNext();){
			String key = keys.next();
			if(key.indexOf("_") <= 0) continue;
			originalKeys.add(key);
		}

		for(String key : originalKeys){
			String colName=JUtilBean.colNameToVariableName(key);
			if(!parameters.containsKey(colName)) parameters.put(colName, parameters.get(key));
		}
	}

	public void  addRequestHeader(String headerName, String headerValue){
		this.headers.put(headerName.toLowerCase(), headerValue);
	}

	public String  getRequestHeader(String headerName){
		return this.headers.get(headerName.toLowerCase());
	}

	@MethodDescription(description = "将带下划线的header名，按(数据库列名 -> 关联类字段名)的规则，将header名转为(列名)另存一份，前提是不存在该列名的header")
	public void storeHeadersCompatible(){
		List<String> originalKeys = new ArrayList<>();
		for(Iterator<String> keys = headers.keySet().iterator(); keys.hasNext();){
			String key = keys.next();
			if(key.indexOf("_") <= 0) continue;
			originalKeys.add(key);
		}

		for(String key : originalKeys){
			String colName=JUtilBean.colNameToVariableName(key);
			if(!headers.containsKey(colName)) headers.put(colName, headers.get(key));
		}
	}

	@MethodDescription(description = "获取客户端时区")
	public String getTimeZone(){
		String timeZone=this.getRequestHeader("timeZone");
		return JUtilString.isBlank(timeZone) ? "UTC" : timeZone;
	}

	@MethodDescription(description = "将本地时间转成UTC", parameters = {@ParameterDescription(name="dateOrTime",
			type = "java.lang.String",
			description = "YYYY-MM-DD 或者 YYYY-MM-DD HH:MM:SS，如格式为YYYY-MM-DD，会自动加上00:00:00再转换"),
			@ParameterDescription(name="timeZone", type = "java.lang.String", description = "时区，用GMT+8、GMT-4等表示")})
	public long timeLocal2UTC(String dateOrTime, String timeZone){
		if(JUtilString.isDate(dateOrTime)) dateOrTime+=" 00:00:00";
		return JUtilTimestamp.localToUTC(dateOrTime, TimeZone.getTimeZone(timeZone));
	}

	@MethodDescription(description = "将表示一个时间区间起点（>=）的本地时间转成UTC", parameters = {@ParameterDescription(name="dateOrTime",
			type = "java.lang.String",
			description = "YYYY-MM-DD 或者 YYYY-MM-DD HH:MM:SS，如格式为YYYY-MM-DD，会自动加上00:00:00再转换"),
			@ParameterDescription(name="timeZone", type = "java.lang.String", description = "时区，用GMT+8、GMT-4等表示")})
	public long timeScopeStartLocal2UTC(String dateOrTime, String timeZone){
		return timeLocal2UTC(dateOrTime, timeZone);
	}

	@MethodDescription(description = "将表示一个时间区间终点（<）的本地时间转成UTC", parameters = {@ParameterDescription(name="dateOrTime",
			type = "java.lang.String",
			description = "YYYY-MM-DD 或者 YYYY-MM-DD HH:MM:SS，如格式为YYYY-MM-DD，会自动加上00:00:00，再加1天（也就是第二天0点）再转换"),
			@ParameterDescription(name="timeZone", type = "java.lang.String", description = "时区，用GMT+8、GMT-4等表示")})
	public long timeScopeEndLocal2UTC(String dateOrTime, String timeZone){
		if(JUtilString.isDate(dateOrTime)){
			dateOrTime+=" 00:00:00";
			dateOrTime = (new Timestamp(Timestamp.valueOf(dateOrTime).getTime() + JUtilTimestamp.millisOfDay)).toString().substring(0, 19);
		}
		return JUtilTimestamp.localToUTC(dateOrTime, TimeZone.getTimeZone(timeZone));
	}
}