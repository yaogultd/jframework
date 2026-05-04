package j.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import j.core.common.JObject;
import j.core.sys.SysConfig;
import jakarta.servlet.http.HttpServletRequest;

import j.core.sys.SysUtil;
import j.util.JUtilInputStream;
import j.util.JUtilString;

/**
 * 
 * @author 肖炯
 *
 */
public class HttpUtil{	
	private static SSLContext sslContext=null;
	
	static{
		try{
			sslContext=SSLContext.getInstance("TLS");
			sslContext.init(null,new TrustManager[]{new MyTrustManager()},null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param encoding
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders getHttp(String url,String encoding,int timeout)throws Exception{
		return getHttp(url,encoding,timeout,null);
	}
	
	/**
	 * 
	 * @param url
	 * @param encoding
	 * @param timeout
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders getHttp(String url,String encoding,int timeout,Map<String,String> headers)throws Exception{
		URL u=new URL(url);
		HttpURLConnection connection=(HttpURLConnection)u.openConnection();
		connection.setConnectTimeout(timeout);
		connection.setRequestMethod("GET");
		
		if(headers!=null) {
			for(Iterator i=headers.keySet().iterator();i.hasNext();) {
				String key=(String)i.next();
				String val=(String)headers.get(key);
				connection.setRequestProperty(key, val);
			}
		}
		
		InputStream in=connection.getInputStream();
		String ret=encoding==null||"".equals(encoding)?JUtilInputStream.string(in):JUtilInputStream.string(in,encoding);

		connection.disconnect();
		
		return new HttpResponseAndHeaders(ret,connection.getHeaderFields());
	}
	
	/**
	 * 
	 * @param url
	 * @param paras
	 * @param encodePara
	 * @param encoding
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders postHttp(String url,Map paras,boolean encodePara,String encoding,int timeout)throws Exception{
		return postHttp(url,paras,encodePara,encoding,timeout,null);
	}	
	
	/**
	 * 
	 * @param url
	 * @param paras
	 * @param encodePara
	 * @param encoding
	 * @param timeout
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders postHttp(String url,Map paras,boolean encodePara,String encoding,int timeout,Map<String,String> headers)throws Exception{
		URL u=new URL(url);
		HttpURLConnection connection=(HttpURLConnection)u.openConnection();
		connection.setConnectTimeout(timeout);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		if(headers!=null) {
			for(Iterator i=headers.keySet().iterator();i.hasNext();) {
				String key=(String)i.next();
				String val=(String)headers.get(key);
				connection.setRequestProperty(key, val);
			}
		}
		
		OutputStream out = connection.getOutputStream();
		java.io.Writer writer = new OutputStreamWriter(out);
		writer.write(generateQueryString(paras,encoding,encodePara));
		writer.flush();
		writer.close();
		writer=null;
		
		InputStream in=connection.getInputStream();
		String ret=encoding==null||"".equals(encoding)?JUtilInputStream.string(in):JUtilInputStream.string(in,encoding);
		
		connection.disconnect();
		
		return new HttpResponseAndHeaders(ret,connection.getHeaderFields());
	}	
	
	/**
	 * 
	 * @param url
	 * @param data
	 * @param encoding
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders postData(String url,String data,String encoding,int timeout)throws Exception{
		return postData(url,data,encoding,timeout,null);
	}
	
	/**
	 * 
	 * @param url
	 * @param data
	 * @param encoding
	 * @param timeout
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders postData(String url,String data,String encoding,int timeout,Map<String,String> headers)throws Exception{
		URL u=new URL(url);
		HttpURLConnection connection=(HttpURLConnection)u.openConnection();
		connection.setConnectTimeout(timeout);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		if(headers!=null) {
			for(Iterator i=headers.keySet().iterator();i.hasNext();) {
				String key=(String)i.next();
				String val=(String)headers.get(key);
				connection.setRequestProperty(key, val);
			}
		}
		
		connection.setRequestProperty("Content-Type","multipart/form-data");
		OutputStream out = connection.getOutputStream();
		java.io.Writer writer = new OutputStreamWriter(out);
		writer.write(data);
		writer.flush();
		writer.close();
		writer=null;
		
		InputStream in=connection.getInputStream();
		String ret=encoding==null||"".equals(encoding)?JUtilInputStream.string(in):JUtilInputStream.string(in,encoding);

		connection.disconnect();
		
		return new HttpResponseAndHeaders(ret,connection.getHeaderFields());
	}	
	
	/**
	 * 
	 * @param fileUploadServer
	 * @param filetoBeUpload
	 * @param encoding
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders uploadFile(String fileUploadServer,File filetoBeUpload,String encoding,int timeout) throws Exception{
		return uploadFile(fileUploadServer,filetoBeUpload,encoding,timeout,null);
	}
	
	/**
	 * 
	 * @param fileUploadServer
	 * @param filetoBeUpload
	 * @param encoding
	 * @param timeout
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpResponseAndHeaders uploadFile(String fileUploadServer,File filetoBeUpload,String encoding,int timeout,Map<String,String> headers) throws Exception{
		String end="\r\n";
		String twoHyphens="--";
		String boundary="******";
		
		URL url=new URL(fileUploadServer);
		HttpURLConnection connection=(HttpURLConnection)url.openConnection();
		connection.setConnectTimeout(timeout);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection","Keep-Alive");
		connection.setRequestProperty("Charset","UTF-8");
		connection.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
		
		if(headers!=null) {
			for(Iterator i=headers.keySet().iterator();i.hasNext();) {
				String key=(String)i.next();
				String val=(String)headers.get(key);
				connection.setRequestProperty(key, val);
			}
		}

		DataOutputStream dos=new DataOutputStream(connection.getOutputStream());
		dos.writeBytes(twoHyphens+boundary+end);
		dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+filetoBeUpload.getName()+"\""+end);
		dos.writeBytes(end);

		FileInputStream fis=new FileInputStream(filetoBeUpload);
		byte[] buffer=new byte[8192]; // 8k
		int count=0;
		while((count=fis.read(buffer))!=-1){
			dos.write(buffer,0,count);
		}
		fis.close();

		dos.writeBytes(end);
		dos.writeBytes(twoHyphens+boundary+twoHyphens+end);
		dos.flush();

		InputStream in=connection.getInputStream();
		String ret=encoding==null||"".equals(encoding)?JUtilInputStream.string(in):JUtilInputStream.string(in,encoding);
		
		connection.disconnect();
		
		return new HttpResponseAndHeaders(ret,connection.getHeaderFields());
	}
	
	/**
	 * 
	 * @param paras
	 * @param encoding
	 * @param encodePara
	 * @return
	 * @throws Exception
	 */
	public static String generateQueryString(Map paras,String encoding,boolean encodePara)throws Exception{
		if(encoding==null){
			encoding="utf-8";
		}
		int i=0;
		String body="";
		for(Iterator keys=paras.keySet().iterator();keys.hasNext();i++){
			String key=(String)keys.next();
			String val=(String)paras.get(key);
			if(encodePara){
				if(i==0){
					body+=key+"="+JUtilString.encodeURI(val,encoding);
				}else{
					body+="&"+key+"="+JUtilString.encodeURI(val,encoding);
				}		
			}else{
				if(i==0){
					body+=key+"="+val;
				}else{
					body+="&"+key+"="+val;
				}
			}
		}
		return body;
	}

	/**
	 * 得到访问者IP
	 * @param request
	 * @return
	 */
	public static String getRemoteIp(HttpServletRequest request){
		String[] ips=getRemoteIps(request);
		return ips.length==0?"":ips[0];
	}

	/**
	 * 访问者的多个IP信息
	 * @param request
	 * @return
	 */
	public static String[] getRemoteIps(HttpServletRequest request){
		if(request==null) return new String[] {"127.0.0.1"};

		String ip=request.getHeader("x-forwarded-for");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("x-real-ip");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("proxy_forwarded_for");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("true-client-ip");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("remote-host");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("remote-addr");
		if(JUtilString.isBlank(ip)) ip=request.getRemoteHost();

		if(ip.indexOf(",")>0){
			ip=ip.replaceAll(" ","");
			return ip.split(",");
		}else{
			return new String[]{ip.trim()};
		}
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static String getUserAgent(HttpServletRequest request){
		return request.getHeader("user-agent");
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public static Map<String, String> parseQueryString(String url){
		Map<String, String> params = new HashMap<>();
		if(url.indexOf("?") > 0) {
			url = url.substring(url.indexOf("?") + 1);
			String paras[] = url.split("&");
			for (int i = 0; i < paras.length; i++) {
				if(paras[i].indexOf("=") < 0) continue;
				String name = paras[i].substring(0, paras[i].indexOf("="));
				String value = paras[i].substring(paras[i].indexOf("=") + 1);
				value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
				params.put(name, value);
			}
		}
		return params;
	}
}