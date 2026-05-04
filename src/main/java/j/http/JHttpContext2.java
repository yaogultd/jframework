package j.http;

import j.util.JUtilString;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class JHttpContext2 {
	private String[] allowedErrorCodes=new String[]{"200","302"};
	private int status=0;
	private String responseText;
	private SimpleBody responseBody;
	private SimpleHttpRequest request;

	private String contentType;
	private String requestEncoding;
	private Map requestHeaders;
	private Map responseHeaders;
	private Map<String, JHttpCookie> responseCookies;
	private Map<String, JHttpCookie> requestCookies;
	private int retries;
	private long retryInterval;
	private String requestBody=null;
	private boolean clearRequestHeadersOnFinish=true;

	/**
	 *
	 *
	 */
	public JHttpContext2() {
		requestHeaders = new HashMap();
		responseHeaders = new HashMap();
		responseCookies = new HashMap();
		requestCookies = new HashMap();
		retries=0;
		retryInterval=0;
		
		requestHeaders.put("Accept-Encoding", "gzip, deflate");
	}
	
	/**
	 * 
	 */
	public JHttpContext2 clone() {
		JHttpContext2 c=new JHttpContext2();
		c.addResponseHeaders(this.getResponseHeaders());
		c.addCookies(this.getCookies());
		c.setClearRequestHeadersOnFinish(this.clearRequestHeadersOnFinish);
		return c;
	}
	
	/**
	 * 
	 * @param retries
	 */
	public void setRetries(int retries){
		this.retries=retries;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRetries(){
		return this.retries;
	}
	
	/**
	 * 
	 * @param retryInterval
	 */
	public void setRetryInterval(int retryInterval){
		this.retryInterval=retryInterval;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getRetryInterval(){
		return this.retryInterval;
	}
	
	/**
	 * 
	 * @param allowedErrorCodes
	 */
	public void setAllowedErrorCodes(String[] allowedErrorCodes){
		this.allowedErrorCodes=allowedErrorCodes;
	}
	public String[] getAllowedErrorCodes(){
		return allowedErrorCodes;
	}
	public boolean isErrorCodeAllowed(int status){
		if(allowedErrorCodes!=null
				&&allowedErrorCodes.length>0
				&&allowedErrorCodes[0].equalsIgnoreCase("ALL")){
			return true;
		}
		
		if(allowedErrorCodes==null){
			return status==200||status==302;
		}else{
			return JUtilString.contain(allowedErrorCodes, status+"");
		}
	}
	
	
	/**
	 * 
	 * @param status
	 */
	public void setStatus(int status){
		this.status=status;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getStatus(){
		return this.status;
	}
	
	/**
	 * 
	 * @param responseText
	 */
	public void setResponseText(String responseText){
		this.responseText=responseText;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getResponseText(){
		return this.responseText;
	}

	/**
	 *
	 * @param responseBody
	 */
	public void setSimpleBody(SimpleBody responseBody){
		this.responseBody=responseBody;
	}

	/**
	 *
	 * @return
	 */
	public SimpleBody getSimpleBody(){
		return this.responseBody;
	}
	
	/**
	 * 
	 * @param request
	 */
	public void setRequest(SimpleHttpRequest request){
		this.request=request;
	}

	/**
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType){
		this.contentType=contentType;
		this.addRequestHeader("Content-Type", contentType);
	}
	

	/**
	 * 
	 * @return
	 */
	public String getContentType(){
		return  this.contentType;
	}

	/**
	 * 
	 * @param requestEncoding
	 */
	public void setRequestEncoding(String requestEncoding){
		this.requestEncoding=requestEncoding;
	}
	

	/**
	 * 
	 * @return
	 */
	public String getRequestEncoding(){
		return  this.requestEncoding;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addRequestHeader(String name, String value) {
		this.requestHeaders.put(name, value);
	}

	/**
	 * 
	 * @param hs
	 */
	public void addRequestHeaders(Map hs) {
		this.requestHeaders.putAll(hs);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public String getRequestHeader(String name) {
		return (String)this.requestHeaders.get(name);
	}

	/**
	 * 
	 * @return
	 */
	public Map getRequestHeaders() {
		return this.requestHeaders;
	}

	/**
	 * 
	 *
	 */
	public void clearRequestHeader() {
		this.requestHeaders.clear();
		this.requestHeaders.put("Accept-Encoding", "gzip, deflate");
	}


	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addResponseHeader(String name, String value) {
		this.responseHeaders.put(name.toLowerCase(), value);
	}

	/**
	 * 
	 * @param hs
	 */
	public void addResponseHeaders(Map hs) {
		this.responseHeaders.putAll(hs);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public String getResponseHeader(String name) {
		return (String)this.responseHeaders.get(name.toLowerCase());
	}

	/**
	 *
	 * @param name
	 * @param value
	 * @param version
	 * @param domain
	 * @param path
	 */
	public void saveResponseCookie(String name, String value, int version, String domain, String path) {
		this.responseCookies.put(name, new JHttpCookie(name, value, version, domain, path));
	}

	/**
	 *
	 * @param name
	 * @param value
	 * @param version
	 * @param domain
	 * @param path
	 * @param expired
	 */
	public void saveResponseCookie(String name, String value, int version, String domain, String path, Date expired) {
		this.responseCookies.put(name, new JHttpCookie(name, value, version, domain, path, expired));
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public JHttpCookie getResponseCookie(String name) {
		return this.responseCookies.get(name);
	}

	/**
	 *
	 */
	public void clearResponseCookies(){
		this.responseCookies.clear();
	}

	/**
	 *
	 * @return
	 */
	public Map getResponseCookies() {
		return this.responseCookies;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @param version
	 * @param domain
	 * @param path
	 */
	public void addCookie(String name, String value, int version, String domain, String path) {
		this.requestCookies.put(name.toLowerCase(), new JHttpCookie(name.toLowerCase(), value, version, domain, path));
	}

	/**
	 *
	 * @param name
	 * @param value
	 * @param version
	 * @param domain
	 * @param path
	 * @param expired
	 */
	public void addCookie(String name, String value, int version, String domain, String path, Date expired) {
		this.requestCookies.put(name.toLowerCase(), new JHttpCookie(name.toLowerCase(), value, version, domain, path, expired));
	}

	/**
	 * 
	 * @param cs
	 */
	public void addCookies(Map cs) {
		this.requestCookies.putAll(cs);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public JHttpCookie getCookie(String name) {
		return this.requestCookies.get(name.toLowerCase());
	}

	/**
	 * 
	 *
	 */
	public void clearCookies() {
		this.requestCookies.clear();
	}
	
	/**
	 * 
	 * @return
	 */
	public Map getCookies() {
		return this.requestCookies;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSessionId() {
		JHttpCookie c=getCookie("JSESSIONID");
		return c==null?null:c.getValue();
	}

	/**
	 * 
	 * @return
	 */
	public Map getResponseHeaders() {
		return this.responseHeaders;
	}

	/**
	 * 
	 *
	 */
	public void clearResponseHeader() {
		this.responseHeaders.clear();
	}
	
	/**
	 * 
	 * @param requestBody
	 */
	public void setRequestBody(String requestBody){
		this.requestBody=requestBody;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getRequestBody(){
		return this.requestBody;
	}
	
	/**
	 * 
	 * @param clearRequestHeadersOnFinish
	 */
	public void setClearRequestHeadersOnFinish(boolean clearRequestHeadersOnFinish) {
		this.clearRequestHeadersOnFinish=clearRequestHeadersOnFinish;
	}
	
	/**
	 * 
	 */
	public void finish(){
		if(request!=null){
			try{
				request.clear();
			}catch(Exception e){}
			request=null;
		}
		
		if(requestBody!=null){
			requestBody=null;
		}
		
		if(this.clearRequestHeadersOnFinish) {
			clearRequestHeader();
		}

		clearCookies();
	}

	/**
	 * 
	 *
	 */
	public void reset(){
		if(request!=null){
			try{
				request.clear();
			}catch(Exception e){}
			request=null;
		}
		if(responseText!=null){
			responseText=null;
		}
		clearRequestHeader();
		clearResponseHeader();
		clearResponseCookies();
		clearCookies();

	}
}
