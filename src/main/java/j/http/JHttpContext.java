package j.http;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.HttpRequestBase;

import j.util.JUtilString;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class JHttpContext {
	private String[] allowedErrorCodes=new String[]{"200","302","202"};
	private int status=0;
	private String responseText;
	private InputStream responseStream;
	private HttpRequestBase request;

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

	//pipe设置
	private String proxyByUrl;//通过哪个url进行代理
	private String proxyIgnoredHeaders;//通过代理请求需要忽略的http头（多个用英文逗号分隔）

	/**
	 * 
	 *
	 */
	public JHttpContext() {
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
	public JHttpContext clone() {
		JHttpContext c=new JHttpContext();
		c.addResponseHeaders(this.getResponseHeaders());
		c.addCookies(this.getCookies());
		c.setClearRequestHeadersOnFinish(this.clearRequestHeadersOnFinish);
		return c;
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
	 * @param contentType
	 */
	public void setContentType(String contentType){
		this.contentType=contentType;
		if(JUtilString.isBlank(contentType)){
			this.removeRequestHeader("Content-Type");
		}else{
			this.addRequestHeader("Content-Type", contentType);
		}
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
	 * @param name
	 */
	public void removeRequestHeader(String name) {
		this.requestHeaders.remove(name);
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
	public void clearRequestCookies() {
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
		if(responseStream!=null){
			try{
				responseStream.close();
			}catch(Exception e){}
			responseStream=null;
		}
		if(request!=null){
			try{
				request.releaseConnection();
				request.abort();
			}catch(Exception e){}
			request=null;
		}
		
		if(requestBody!=null){
			requestBody=null;
		}
		
		if(this.clearRequestHeadersOnFinish) {
			clearRequestHeader();
		}

		clearRequestCookies();
	}

	/**
	 * 
	 *
	 */
	public void reset(){
		if(this.responseStream!=null){
			try{
				this.responseStream.close();
			}catch(Exception e){}
			this.responseStream=null;
		}

		if(this.request!=null){
			try{
				this.request.releaseConnection();
				this.request.abort();
			}catch(Exception e){}
			this.request=null;
		}

		this.responseText=null;

		setRequestBody(null);

		clearRequestHeader();
		clearRequestCookies();

		clearResponseHeader();
		clearResponseCookies();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize(){
		if(responseStream!=null){
			try{
				responseStream.close();
			}catch(Exception e){}
			responseStream=null;
		}
		if(request!=null){
			try{
				request.releaseConnection();
				request.abort();
			}catch(Exception e){}
			request=null;
		}
		if(responseText!=null){
			responseText=null;
		}
		clearRequestHeader();
		clearResponseHeader();
		clearRequestCookies();
	}
}
