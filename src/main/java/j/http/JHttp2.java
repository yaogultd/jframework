package j.http;


import j.core.Startup;
import j.core.nvwa.Nvwa;
import j.core.sys.SysUtil;
import j.util.ConcurrentMap;
import j.util.JUtilCompressor;
import j.util.JUtilString;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author 肖炯
 *
 */
public class JHttp2 {
	public static final String default_user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";
	public static final long default_retry_interval=500;

	private TlsStrategy tlsStrategy;
	private IOReactorConfig ioReactorConfig;
	private ConnectionConfig connectionConfig;
	private RequestConfig requestConfig;
	private ConcurrentMap<String, HttpClientContext> clientContexts=new ConcurrentMap<>();

	/**
	 *
	 *
	 */
	private JHttp2() {
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public static JHttp2 getInstance() throws Exception{
		return getInstance("",
				"", 30000,
				30000,
				300000,
				org.apache.hc.core5.http.ssl.TLS.V_1_3);
	}

	/**
	 *
	 * @param certFilePath
	 * @param certFilePassword
	 * @param connectTimeout
	 * @param readTimeout
	 * @param keepLiveTimeout
	 * @param tls
	 * @return
	 * @throws Exception
	 */
	public static JHttp2 getInstance(String certFilePath,
									 String certFilePassword,
									 long connectTimeout,
									 long readTimeout,
									 long keepLiveTimeout,
									 org.apache.hc.core5.http.ssl.TLS... tls) throws Exception{
		JHttp2 jHttp=new JHttp2();

		if(JUtilString.isBlank(certFilePath)){
			certFilePath=JHttp.getServerJsk().getAbsolutePath();
			certFilePassword=JHttp.serverJskPassword;
		}

		File certFile= Startup.deployAsJar()? Nvwa.getTempFileOfResourceInJar(certFilePath):new File(certFilePath);
		SSLContext ctx = SSLContexts.custom().loadTrustMaterial(certFile,certFilePassword.toCharArray(), new TrustSelfSignedStrategy()).build();
		ctx.init(null, new TrustManager[] { new MyTrustManager() }, null);

		jHttp.tlsStrategy= ClientTlsStrategyBuilder.create()
				//.setSslContext(org.apache.hc.core5.ssl.SSLContexts.createSystemDefault())
				.setSslContext(ctx)
				.setTlsVersions(tls)
				.build();

		jHttp.ioReactorConfig=IOReactorConfig.custom()
				.setSoTimeout(Timeout.ofMilliseconds(readTimeout))
				.build();

		jHttp.connectionConfig=ConnectionConfig.custom()
				.setSocketTimeout(Timeout.ofMilliseconds(connectTimeout))
				.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
				.setTimeToLive(TimeValue.ofMilliseconds(keepLiveTimeout))
				.build();

		jHttp.requestConfig=RequestConfig.custom()
				.setCookieSpec(StandardCookieSpec.STRICT)
				.build();

		return jHttp;
	}

	/**
	 *
	 * @return
	 */
	public CloseableHttpAsyncClient createClient(){
		CloseableHttpAsyncClient client = HttpAsyncClients.customHttp2()
				.setTlsStrategy(this.tlsStrategy)
				.setIOReactorConfig(this.ioReactorConfig)
				.setDefaultConnectionConfig(this.connectionConfig)
				.setDefaultRequestConfig(this.requestConfig)
				.build();
		client.start();

		CookieStore cookieStore = new BasicCookieStore();

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		HttpClientContext clientContext = HttpClientContext.create();
		clientContext.setCookieStore(cookieStore);
		clientContext.setCredentialsProvider(credentialsProvider);
		clientContext.setRequestConfig(RequestConfig.custom().setCookieSpec(StandardCookieSpec.STRICT).build());

		clientContexts.put(client.toString(), clientContext);

		return client;
	}

	/**
	 *
	 * @param proxyIp
	 * @param proxyPort
	 * @param proxyUsername
	 * @param proxyPassword
	 * @return
	 */
	public CloseableHttpAsyncClient createClient(String proxyIp,
												 int proxyPort,
												 String proxyUsername,
												 String proxyPassword){
		HttpHost proxy=new HttpHost(proxyIp, proxyPort);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

		BasicCredentialsProvider credsProvider = null;
		if(!JUtilString.isBlank(proxyUsername)){
			credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(proxyIp, proxyPort),
					new UsernamePasswordCredentials(proxyUsername, proxyPassword.toCharArray()));
		}

		CloseableHttpAsyncClient client = HttpAsyncClients.customHttp2()
				.setTlsStrategy(this.tlsStrategy)
				.setIOReactorConfig(this.ioReactorConfig)
				.setDefaultConnectionConfig(this.connectionConfig)
				.setDefaultRequestConfig(this.requestConfig)
				.setRoutePlanner(routePlanner)
				.setDefaultCredentialsProvider(credsProvider)
				.build();
		client.start();

		CookieStore cookieStore = new BasicCookieStore();

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		HttpClientContext clientContext = HttpClientContext.create();
		clientContext.setCookieStore(cookieStore);
		clientContext.setCredentialsProvider(credentialsProvider);
		clientContext.setRequestConfig(RequestConfig.custom().setCookieSpec(StandardCookieSpec.STRICT).build());

		clientContexts.put(client.toString(), clientContext);

		return client;
	}

	/**
	 *
	 * @param client
	 * @return
	 */
	public HttpClientContext getClientContext(CloseableHttpAsyncClient client){
		return clientContexts.get(client.toString());
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	private boolean isGzip(JHttpContext2 context){
		return "gzip".equalsIgnoreCase(context.getResponseHeader("content-encoding"));
	}

	/**
	 *
	 * @param client
	 * @param context
	 * @param response
	 */
	private void getStatusAndHeaders(CloseableHttpAsyncClient client, JHttpContext2 context, SimpleHttpResponse response){
		if(context==null||response==null) return;

		context.setStatus(response.getCode());

        Header[] headers = response.getHeaders();
        if(headers!=null){
            for(int i=0;i<headers.length;i++){
            	if(headers[i].getName().equalsIgnoreCase("set-cookie")){
            		String value=headers[i].getValue();
            		if(value.indexOf(";")>0) value=value.substring(0, value.indexOf(";"));

					if(context.getResponseHeader(headers[i].getName()) != null){
						value=context.getResponseHeader(headers[i].getName())+"; "+value;
					}
					context.addResponseHeader(headers[i].getName(), value);
				}else{
					context.addResponseHeader(headers[i].getName(),headers[i].getValue());
				}
            }
        }

        List<Cookie> cookies=getClientContext(client).getCookieStore().getCookies();
		if(cookies!=null) {
        	for(int i=0; i<cookies.size(); i++) {
        		Cookie c=cookies.get(i);

        		context.saveResponseCookie(c.getName(),
        				c.getValue(),
        				0,
        				c.getDomain(),
        				c.getPath(),
						c.getExpiryDate());
        	}
        }
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param request
	 */
	private void initRequest(JHttpContext2 context, CloseableHttpAsyncClient client, SimpleHttpRequest request){
		if(context==null||request==null) return;
		context.setRequest(request);

		if (context.getRequestHeader("User-Agent") == null) {
			request.addHeader("User-Agent",default_user_agent);
		}

		HttpClientContext clientContext=getClientContext(client);

		String sCookies="";
		Map cookies=context.getCookies();
		if(cookies!=null&&!cookies.isEmpty()){
			for(Iterator it=cookies.keySet().iterator();it.hasNext();){
				String name=(String)it.next();
				JHttpCookie c=(JHttpCookie)cookies.get(name);

				if(!"".equals(sCookies)) sCookies+="; ";
				sCookies+=name+"="+c.getValue();

				BasicClientCookie cookie = new BasicClientCookie(name, c.getValue());
				cookie.setDomain(c.getDomain());
				cookie.setPath(c.getPath());
				if(c.getExpired() != null) cookie.setExpiryDate(c.getExpired());
				clientContext.getCookieStore().addCookie(cookie);
			}
		}

		Map headers=context.getRequestHeaders();
		if(headers!=null&&!headers.isEmpty()){
			for(Iterator it = headers.keySet().iterator(); it.hasNext();){
				String name=(String)it.next();
				String value=(String)headers.get(name);

				if(request.containsHeader(name)){
					request.removeHeaders(name);
				}

				request.addHeader(name,value);
			}
		}
	}

	/**
	 *
	 * @param builder
	 * @param strings
	 * @throws Exception
	 */
	private static void addParams(SimpleRequestBuilder builder, Map<String, String> strings) throws Exception{
		if(strings!=null&&!strings.isEmpty()){
			Iterator<String> keys = strings.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				String val = strings.get(key);
				builder.addParameter(key, val);
			}
		}
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param request
	 * @param encoding
	 * @param responseType 0 - String, 1 - InputStream
	 * @throws Exception
	 */
	private void execute(JHttpContext2 context,CloseableHttpAsyncClient client,SimpleHttpRequest request,String encoding,int responseType) throws Exception{
		int retries=1;

		if(context!=null&&context.getRetries()>0){
			retries=context.getRetries();
		}

		long interval=default_retry_interval;
		if(context!=null&&context.getRetryInterval()>0){
			interval=context.getRetryInterval();
		}

		if(retries<=0) retries=1;
		while(retries>0){
			retries--;
			try{
				doExecute(context,client,request,encoding,responseType,retries==0?true:false);
				if(context!=null
						&&(context.getStatus()==200 || context.isErrorCodeAllowed(context.getStatus()))) return;

				Thread.sleep(interval);
			}catch(Exception e){
				if(retries==0) throw e;
			}
		}

		if(context!=null
				&&!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("get "+context.getStatus()+" error while request "+request.getRequestUri());
		}
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param request
	 * @param encoding
	 * @param responseType 0 - String, 1 - InputStream
	 * @param abort
	 * @throws Exception
	 */
	private void doExecute(JHttpContext2 context,CloseableHttpAsyncClient client,SimpleHttpRequest request, String encoding,int responseType,boolean abort) throws Exception{
		try {
			Future<SimpleHttpResponse> future = client.execute(request,
					getClientContext(client),
					new FutureCallback<>() {
						@Override
						public void completed(SimpleHttpResponse simpleHttpResponse) {
						}

						@Override
						public void failed(Exception ex) {
						}

						@Override
						public void cancelled() {
						}

					});

			SimpleHttpResponse response=future.get();
			getStatusAndHeaders(client, context, response);
			context.setSimpleBody(response.getBody());
		} catch (Exception e) {
			try{
				if(request!=null&&abort) request.clear();
			}catch(Exception ex){}
			throw e;
		}
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public JHttpContext2 get(JHttpContext2 context,CloseableHttpAsyncClient client, String url) throws Exception {
		return get(context,client,url,"UTF-8");
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public JHttpContext2 get(JHttpContext2 context,CloseableHttpAsyncClient client, String url, String encoding)throws Exception {
		if(context == null) context = new JHttpContext2();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");

		SimpleRequestBuilder builder=SimpleRequestBuilder.create(Method.GET).setUri(URI.create(url));
		SimpleHttpRequest request=builder.build();
		initRequest(context,client,request);

		execute(context,client,request,encoding,0);

		return context;
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public JHttpContext2 post(JHttpContext2 context,CloseableHttpAsyncClient client, String url, Map<String, String> params)throws Exception {
		return post(context,client,url,params,"UTF-8");
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param params
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public JHttpContext2 post(JHttpContext2 context,CloseableHttpAsyncClient client, String url,Map params,String encoding) throws Exception {
		if(context == null) context = new JHttpContext2();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");

		SimpleRequestBuilder builder=SimpleRequestBuilder.create(Method.POST).setUri(URI.create(url));
		addParams(builder,params);
		SimpleHttpRequest request=builder.build();
		initRequest(context,client,request);

		execute(context,client,request,encoding,0);

		return context;
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String getResponse(JHttpContext2 context,CloseableHttpAsyncClient client, String url) throws Exception {
		return getResponse(context,client,url,"UTF-8");
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public String getResponse(JHttpContext2 context,CloseableHttpAsyncClient client, String url, String encoding)throws Exception {
		context=get(context,client,url,encoding);

		if(context==null||!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("获取网页出错（get） - "+url+" - context - "+context+" status - "+(context==null?"unknown":context.getStatus()));
		}
		String responseText="";
		if(context.getSimpleBody()!=null) {
			if (isGzip(context)) {
				if (!JUtilString.isBlank(encoding))
					responseText = JUtilCompressor.unGzipString(context.getSimpleBody().getBodyBytes(), encoding);
				else responseText = JUtilCompressor.unGzipString(context.getSimpleBody().getBodyBytes());
			} else {
				if (!JUtilString.isBlank(encoding))
					responseText = new String(context.getSimpleBody().getBodyBytes(), encoding);
				else responseText = new String(context.getSimpleBody().getBodyBytes());
			}
		}
		return responseText;
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public String postResponse(JHttpContext2 context,CloseableHttpAsyncClient client, String url, Map params)throws Exception {
		return postResponse(context,client,url,params,"UTF-8");
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param params
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public String postResponse(JHttpContext2 context,CloseableHttpAsyncClient client, String url,Map params,String encoding) throws Exception {
		context=post(context,client,url,params,encoding);

		if(context==null||!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("获取网页出错（post） - "+url+" - context - "+context+" status - "+(context==null?"unknown":context.getStatus()));
		}
		String responseText="";
		if(context.getSimpleBody()!=null) {
			if (isGzip(context)) {
				if (!JUtilString.isBlank(encoding))
					responseText = JUtilCompressor.unGzipString(context.getSimpleBody().getBodyBytes(), encoding);
				else responseText = JUtilCompressor.unGzipString(context.getSimpleBody().getBodyBytes());
			} else {
				if (!JUtilString.isBlank(encoding))
					responseText = new String(context.getSimpleBody().getBodyBytes(), encoding);
				else responseText = new String(context.getSimpleBody().getBodyBytes());
			}
		}
		return responseText;
	}
}