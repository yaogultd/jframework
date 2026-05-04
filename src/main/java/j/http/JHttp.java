package j.http;

import j.core.Startup;
import j.core.common.Global;
import j.core.common.JObject;
import j.core.common.JProperties;
import j.core.fs.JDFSFile;
import j.core.nvwa.Nvwa;
import j.core.nvwa.resource.ResourceHelper;
import j.core.permission.Signature;
import j.core.sso.Client;
import j.core.sso.SSOConfig;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.http.handler.ProxyHandler;
import j.http.proxy.ProxyUsage;
import j.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 肖炯
 *
 */
public class JHttp{
	protected static final String serverJsk="jis:72,6l,72,6l,0,0,0,2,0,0,0,2,0,0,0,2,0,6,2r,39,37,38,33,31,0,0,1,2f,47,4x,3i,41,0,5,2g,1a,1h,1c,1l,0,0,3,2n,1c,3m,3,2j,1c,3m,2,1v,4g,3,2,1,2,2,4,2a,0,43,5l,1c,d,6,9,16,3q,20,3q,6v,d,1,1,b,5,0,1c,2m,1d,b,1c,9,6,3,2d,4,6,j,2,2r,32,1d,g,1c,e,6,3,2d,4,8,j,7,2q,2t,2x,2y,2x,32,2v,1d,e,1c,c,6,3,2d,4,7,j,5,2r,2w,2x,32,2p,1d,d,1c,b,6,3,2d,4,a,j,4,32,33,32,2t,1d,e,1c,c,6,3,2d,4,b,j,5,37,2p,32,2v,33,1d,e,1c,c,6,3,2d,4,3,j,5,37,2p,32,2v,33,1c,u,n,d,1d,1i,1d,1c,1c,1i,1c,1d,1h,1c,1f,1j,2i,n,d,1e,1i,1d,1c,1c,1g,1c,1d,1h,1c,1f,1j,2i,1c,2m,1d,b,1c,9,6,3,2d,4,6,j,2,2r,32,1d,g,1c,e,6,3,2d,4,8,j,7,2q,2t,2x,2y,2x,32,2v,1d,e,1c,c,6,3,2d,4,7,j,5,2r,2w,2x,32,2p,1d,d,1c,b,6,3,2d,4,a,j,4,32,33,32,2t,1d,e,1c,c,6,3,2d,4,b,j,5,37,2p,32,2v,33,1d,e,1c,c,6,3,2d,4,3,j,5,37,2p,32,2v,33,1c,3m,1,y,1c,d,6,9,16,3q,20,3q,6v,d,1,1,1,5,0,3,3m,1,f,0,1c,3m,1,a,2,3m,1,1,0,4g,2d,46,3n,6w,5v,6i,66,28,4s,q,4k,4d,3m,1o,6k,2t,2v,22,60,54,1y,5x,5o,7,4e,5z,5o,29,42,1j,z,3z,3m,45,6d,6b,4g,1m,24,4r,w,2v,5t,57,67,4g,5m,37,36,6g,2f,y,50,2p,4i,2k,4w,1f,5o,4x,34,61,60,3k,4l,36,73,4q,6r,6f,5g,30,6i,2k,3s,6j,5r,d,19,6c,6a,5s,4i,3k,69,65,13,25,3p,5v,4t,3d,o,6v,2c,30,1,5q,6p,1x,3m,5d,34,5s,3k,41,40,1x,4a,6m,2u,4x,v,2h,g,5o,2c,2t,l,3b,n,58,28,j,2l,1e,52,71,e,2u,3y,31,4x,1n,6y,2u,3u,49,5t,1m,3c,6c,5n,42,10,2j,5z,1g,c,2d,25,4e,n,7,2q,59,4t,1b,1k,1x,6g,3d,6h,6w,5y,2t,b,5i,5v,4y,18,5c,4t,25,5n,43,41,4r,5z,2r,41,l,3f,35,p,5o,1w,3e,4z,3h,2v,72,45,3y,5r,4o,20,4m,k,51,2,4e,13,7,1,2w,56,2t,5o,4p,1c,23,5n,20,2p,3c,f,42,38,3e,39,61,5b,5s,5i,h,6g,9,3x,24,6b,18,19,5k,6w,4k,5k,4v,1t,4h,73,3h,y,20,5k,41,b,3s,6m,43,5o,50,2x,6k,3x,2,3,1,0,1,4j,x,1c,v,1c,t,6,3,2d,t,e,4,m,4,k,5g,1x,5f,17,62,l,w,3l,13,4w,4u,21,72,y,4d,5m,18,h,0,6n,1c,d,6,9,16,3q,20,3q,6v,d,1,1,b,5,0,3,3m,1,1,0,46,4l,4u,4r,6y,1e,3u,3v,1p,5b,6g,22,4q,3a,2s,52,4y,4q,12,f,38,4d,18,40,20,1x,2h,5v,36,57,2l,6u,i,3t,7,m,65,3w,57,2f,3z,3h,1n,2y,9,2f,1,67,3h,4g,5v,5s,18,1n,9,61,6u,4m,4j,5q,22,29,6u,2z,4k,6r,54,u,1r,1h,1e,3f,2u,6e,19,2b,62,1l,4e,14,2f,30,3c,6c,6t,6k,5g,3l,28,2k,6f,1m,1y,1c,13,17,4z,6k,28,36,2g,2q,3i,3k,1y,1w,1b,57,5a,2,4y,6s,2r,6n,2o,5u,5t,4p,34,4d,70,2o,6q,5d,6e,41,3w,5c,5p,5u,2i,3,4q,5y,69,6h,5p,3z,5k,39,36,5h,2s,48,2y,6y,i,63,59,2z,1f,15,5y,2b,2s,5u,6s,y,63,6r,33,1w,s,56,5o,1b,51,q,36,5m,4d,63,52,13,60,70,5l,44,2d,64,31,5n,3i,6,53,6l,3u,4t,46,5b,63,2y,4r,5n,6x,4s,71,25,6b,31,3x,5h,q,5i,6g,5e,2l,43,c,4g,d,u,10,2r,w,2q,45,1l,4,11,17,70,4x,6u,65,4y,2i,6p,1s,5b,3t,4b,5j,42,l,5k,5r,39,4y,z,3g,1y,64,3r,5a,3t,17,w,3b,4s,5p,4s,5y,32,4e,10,0,0,0,1,0,6,37,2t,36,3a,2t,36,0,0,1,2f,47,4u,1g,3u,0,0,5,2,1c,3m,4,72,1c,e,6,a,17,6,1,4,1,16,2,h,1,1,5,0,4,3m,4,6i,5z,x,4r,2r,29,5u,58,41,6k,4d,2g,1r,w,g,1e,49,3s,3z,59,4f,15,61,6k,1k,10,6q,1z,6d,5i,2p,3q,3k,52,56,25,3f,4t,5j,24,3h,4d,32,1r,54,41,43,3c,1y,v,3u,4f,3h,1a,n,3g,5b,4x,9,6u,k,2g,1o,1p,4q,2f,3x,2v,34,18,3e,5u,3i,5v,6n,1i,1m,3f,b,2i,2x,33,5h,3x,6u,55,2u,r,61,5q,2o,5u,1y,6o,5b,28,t,2g,6l,13,56,4u,l,60,4o,46,4a,e,2t,40,5w,2s,25,6r,1i,56,63,t,6t,s,6m,6c,1f,42,37,f,6t,4x,23,2d,5t,27,1n,38,z,4p,5k,2b,7,6w,54,2h,66,5x,c,3z,i,t,3c,9,6z,3h,1d,64,13,1u,52,43,41,19,5p,1e,5h,4k,3l,6t,5g,6b,p,47,3q,3t,5n,3d,5a,4r,61,6n,15,59,6h,68,6j,6,12,4a,2n,43,64,6k,13,69,3c,n,6d,36,3w,4x,59,5z,4y,47,28,64,3r,49,3e,3f,5l,4w,3d,6o,y,6t,r,23,2j,2e,4j,1u,6f,64,3f,p,2v,31,2m,5s,32,1b,4b,1r,43,5k,13,2b,5a,28,6v,54,3n,1x,3x,35,4w,2d,33,5h,1s,5j,5g,39,5q,28,5l,1b,11,3b,4u,6b,6l,5i,3n,4q,6x,6s,2i,5p,1t,61,e,5z,1d,4w,37,3c,1a,c,33,3x,52,1r,s,2m,46,2v,0,4y,6n,3t,2a,4e,17,1a,30,36,5j,6s,14,5k,g,68,6l,9,3j,6h,51,5f,34,4,29,6s,3t,5y,54,1z,6b,x,6p,4r,9,1e,4q,5p,3r,37,6e,3s,4t,70,4u,5,2o,41,1c,4s,2k,15,3,28,3d,16,4p,26,d,6e,59,2g,5i,60,6g,3t,53,36,68,48,4b,2w,6m,5o,5s,53,34,6t,l,5r,3w,5e,5h,2f,2g,35,40,37,4o,65,5f,72,2o,f,6l,2e,71,32,3m,50,3p,3q,6x,24,29,6s,s,68,3m,3o,4i,44,3r,31,5l,3i,4d,n,37,3a,s,1v,66,6k,4e,72,28,2e,6h,9,27,6e,3o,10,3t,1d,h,4y,26,2r,2o,33,53,2f,3n,3u,1d,4w,15,3o,59,33,2s,1g,69,19,4z,6w,3j,2,9,6m,4h,c,2z,2d,4s,1b,d,c,47,5k,47,5u,67,2y,3z,20,5b,4a,47,73,39,11,2u,4,6l,1m,1v,56,5u,6h,5t,4a,3a,37,5q,5l,42,2n,51,61,46,4r,1r,5v,5i,o,s,1u,2s,20,6j,70,4g,54,1p,4f,6c,14,64,6b,6s,46,25,5n,2g,5x,20,v,3p,71,16,6d,44,62,2j,2d,q,1f,f,2r,4k,64,32,5o,3k,9,6l,3r,4r,v,4b,2g,67,3l,5r,5t,13,3m,x,6e,61,30,11,67,e,c,1k,2g,4k,5v,4x,59,m,3e,3o,32,3z,3e,6r,40,1p,5a,1u,3w,1r,e,2t,2x,1z,4d,52,4g,4n,1b,3v,1n,3z,2i,5d,6i,2s,4z,2m,4l,1,3k,72,1k,56,1g,2i,k,3u,5w,4p,4l,6t,10,3b,51,3m,5q,2v,m,2k,4w,l,6w,6o,4s,2r,6r,6h,3d,5y,5h,b,4l,d,b,0,3i,2z,3v,5v,72,j,3d,60,3s,5z,5g,2t,6,2j,6a,2p,62,1t,34,c,5x,2f,q,5,17,6p,5t,6,1o,c,3o,2o,1f,4,3m,5u,3o,67,10,4k,5h,3p,3n,6n,y,3p,1a,3l,3e,2e,1j,5c,51,12,2a,2o,10,4q,h,5j,3g,54,6r,69,2g,t,23,u,19,6b,6t,64,2x,4b,5h,4u,2k,2s,5w,5p,2v,62,55,4j,6j,6p,32,2t,3o,53,3k,1,3a,t,63,u,27,62,6p,3c,6i,1t,24,2o,d,6w,q,38,52,6z,51,25,1g,4l,69,66,73,5d,1a,58,3z,1,o,l,1y,6j,44,1i,j,50,4p,1f,4w,v,6k,50,1v,19,x,1h,50,n,1w,15,4c,5w,1a,3j,6p,6w,3g,c,3w,4s,30,o,1l,6e,4l,5,s,l,15,34,6q,6d,5b,5q,2l,1v,19,47,4w,1g,g,1i,2r,2n,37,7,67,5h,f,5d,5n,2i,5i,4e,32,61,3b,4w,61,56,6j,4o,6y,3s,4h,3j,5k,2i,6l,3,1r,71,5j,69,53,6n,3x,50,4a,4y,5y,6l,62,2y,l,4w,6s,5u,3e,4e,5w,34,39,2d,25,5s,6s,1w,6k,3v,6s,36,3h,5e,1t,28,n,t,6z,f,3h,5,71,6f,k,2g,1v,5,3s,6g,5k,19,6n,g,5v,36,22,6e,5l,1p,5r,71,6b,4s,3,4u,3p,4k,5r,3j,72,70,3c,59,50,61,63,3u,6e,5s,5m,g,5j,1s,2h,3b,3l,1z,2o,4i,34,53,1b,3k,4x,4e,1v,3k,1p,1t,5r,41,4g,3g,1r,s,22,1o,5o,6d,5l,1t,2j,1j,4c,15,1o,4m,5r,3g,x,w,3w,6f,60,5u,3n,0,2z,5q,5t,2z,5s,11,1a,3g,c,26,2n,k,6m,6v,5d,2x,8,3h,1b,4v,48,1k,3u,5g,2e,30,2m,2y,47,5s,2p,37,5y,1j,37,20,5a,50,1h,1e,35,2h,6l,5d,5s,1m,v,49,r,1d,52,2o,13,60,4a,65,3,55,1a,4h,5i,5o,1q,q,3u,2j,6w,4e,43,50,r,48,4a,5x,4p,69,4u,5o,3a,3i,1e,3m,2d,b,54,4e,70,73,1j,39,2c,64,1,2q,1n,1w,4o,2x,3i,2j,c,26,5j,1c,30,4h,2s,1m,2o,4,5s,5j,3a,1i,4h,5r,3a,38,46,3a,13,1q,4z,21,5q,19,6d,7,3v,47,3r,3j,6r,2j,u,9,6s,2u,6a,6m,c,21,h,2p,4b,2z,4z,6l,x,3p,5c,5a,j,4c,66,2x,e,44,1x,27,6p,6z,14,1t,4a,i,1x,f,72,f,6w,71,1s,4e,2,3s,6w,3f,2z,69,2e,59,46,3v,28,2c,24,w,5q,2x,11,5y,6l,5m,3g,3p,1q,6g,1,28,l,3p,6n,6q,6o,1n,54,2x,2q,59,3j,5k,38,2t,h,37,8,5,6l,1h,2n,1f,k,6o,1n,48,7,4g,4o,6p,1s,5q,1i,6q,1o,1l,3n,33,6v,5o,r,46,6l,6h,1o,28,3e,6m,6c,2l,45,6n,2c,6,5l,s,4a,l,3x,3r,2d,3g,53,w,5e,14,4b,6x,6c,5s,52,66,6v,5s,73,2o,5i,2n,44,1x,38,15,30,73,16,5o,2f,61,1s,2f,b,r,3b,2s,10,6m,3t,2i,0,0,0,1,0,5,2g,1a,1h,1c,1l,0,0,3,2v,1c,3m,3,2r,1c,3m,2,23,4g,3,2,1,2,2,4,29,4h,1g,3w,1c,d,6,9,16,3q,20,3q,6v,d,1,1,b,5,0,1c,2q,1d,b,1c,9,6,3,2d,4,6,j,2,2r,32,1d,g,1c,e,6,3,2d,4,8,j,7,2q,2t,2x,2y,2x,32,2v,1d,e,1c,c,6,3,2d,4,7,j,5,2r,2w,2x,32,2p,1d,d,1c,b,6,3,2d,4,a,j,4,32,33,32,2t,1d,e,1c,c,6,3,2d,4,b,j,5,37,2p,32,2v,33,1d,i,1c,g,6,3,2d,4,3,j,9,30,33,2r,2p,30,2w,33,37,38,1c,u,n,d,1d,1i,1d,1c,1c,1i,1c,1d,1g,1l,1d,1k,2i,n,d,1e,1i,1d,1c,1c,1g,1c,1d,1g,1l,1d,1k,2i,1c,2q,1d,b,1c,9,6,3,2d,4,6,j,2,2r,32,1d,g,1c,e,6,3,2d,4,8,j,7,2q,2t,2x,2y,2x,32,2v,1d,e,1c,c,6,3,2d,4,7,j,5,2r,2w,2x,32,2p,1d,d,1c,b,6,3,2d,4,a,j,4,32,33,32,2t,1d,e,1c,c,6,3,2d,4,b,j,5,37,2p,32,2v,33,1d,i,1c,g,6,3,2d,4,3,j,9,30,33,2r,2p,30,2w,33,37,38,1c,3m,1,y,1c,d,6,9,16,3q,20,3q,6v,d,1,1,1,5,0,3,3m,1,f,0,1c,3m,1,a,2,3m,1,1,0,3z,33,2z,2a,1h,m,y,y,16,4y,q,3o,1h,42,4i,13,5c,3e,q,1u,4l,2j,33,30,3l,o,42,3k,6q,3w,3w,1z,5z,d,2o,3j,2o,5h,34,6q,56,9,61,5f,t,1g,3b,64,4s,3,4l,1o,i,56,4x,5k,3r,6f,0,3l,b,4d,6q,5v,5u,5e,23,2m,23,2e,26,2e,45,6d,6u,40,14,3j,3b,3d,67,5r,6k,4n,d,1l,10,4i,5e,48,s,21,6h,g,6g,11,2d,6f,2a,3r,6z,x,62,1i,1a,2h,3j,1t,6w,b,21,4b,8,6m,47,5e,4v,1c,3n,4j,2o,17,3l,3r,5m,73,1o,2t,s,3k,1k,1o,4n,1a,b,64,2o,5i,22,e,58,5i,2d,1z,4u,k,1f,14,0,4v,3,1e,11,24,69,3a,5f,42,2a,6v,1w,46,s,1f,3k,1y,2w,1,41,4z,69,1u,4l,67,1y,66,8,4f,1a,5b,2p,3e,5x,j,5l,69,6m,22,67,33,1t,5u,66,56,x,6o,70,1y,1l,6q,2g,3l,b,5w,10,1w,6j,66,1w,71,4i,1g,3k,1v,4u,6e,2u,2e,6f,33,6n,5c,4n,69,6l,2,6f,l,5c,4o,1o,w,5u,53,3b,1w,5a,5g,6o,c,4y,18,3k,5,6n,1c,6d,6v,4r,1g,6s,20,1q,6l,23,17,2,3,1,0,1,4j,x,1c,v,1c,t,6,3,2d,t,e,4,m,4,k,1u,29,6f,3d,1y,6c,k,4u,47,73,1s,1x,2g,1j,6u,50,3r,2c,3y,1m,1c,d,6,9,16,3q,20,3q,6v,d,1,1,b,5,0,3,3m,1,1,0,3h,22,61,62,27,3w,e,1j,55,6i,70,2f,48,4,3q,4e,6w,54,4i,g,2,1h,3x,1f,22,p,4j,4l,6v,62,4z,45,37,4f,5n,38,24,5q,5,36,43,3s,4y,5a,2r,x,1r,1v,4y,59,3p,63,12,5i,3r,2v,z,33,2h,3y,6t,6p,3p,2s,71,6s,q,6s,4,5v,65,2g,73,j,5i,23,5u,6w,6a,1,42,51,6a,6j,2g,4r,2g,48,4p,4q,5j,25,24,1a,6d,5k,j,5g,5r,2m,24,5q,4b,49,38,4i,6w,57,42,3d,57,4w,20,4f,3b,b,5d,f,4n,6t,2,5e,3v,1q,5,4b,5n,5w,3,2n,1w,23,23,3c,60,f,6l,42,6x,38,2m,6l,67,2e,3h,q,35,6t,6h,48,1k,41,48,4,60,3v,44,25,3o,a,71,5o,5q,l,2p,6d,6v,2v,1,4c,2t,3,2t,5s,3k,15,5e,56,71,13,1x,4a,6o,2v,22,4g,2v,3u,56,1g,6k,26,6b,42,65,2x,5s,47,39,66,61,2p,3p,4f,5h,4a,4c,39,59,2b,1t,b,7,2n,4,58,33,2x,51,6z,5,3h,3p,33,4v,54,6h,1g,2l,5d,6x,46,5g,4o,6z,5p,3j,62,2d,2c,56,6l,5p,6l,1g,3,51,5d,s,2g,49,6y,5j,5j,1t,51,1a,1j,62,4t,14,1e,1w,j,4j,6j,45,41,1p,30,5w,50,6u,1a,13,1f";
	protected static final String serverJskPassword="20081016";
	protected static File serverJskFile=null;

	public static final String default_user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";
	public static final int default_redirects=1;
	public static final long default_retry_interval=500;

	private ConcurrentMap<String, RequestConfig> configOfClients=new ConcurrentMap<>();
	private ConcurrentMap<String, ProxyUsage> proxyOfClients=new ConcurrentMap<>();
	private ConcurrentMap<String, CredentialsProvider> credentialsOfClients=new ConcurrentMap<>();
	private SSLConnectionSocketFactory factory;
	private PoolingHttpClientConnectionManager poolingmgr;
	private CookieStore cookieStore = new BasicCookieStore();
	private String cookieSpec=CookieSpecs.STANDARD;
	private boolean redirectsEnabled=true;

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	protected static File getServerJsk() throws Exception{
		synchronized (serverJsk) {
			if(serverJskFile != null && serverJskFile.exists()) return serverJskFile;

			serverJskFile = new File(JUtilString.appendPath(ResourceHelper.configDirExternal(), "server.jks"));
			if(!serverJskFile.exists()) {
				serverJskFile = File.createTempFile("JHTTP_", ".jsk");
				JDFSFile.save(serverJskFile.getAbsolutePath(), JObject.intSequence2String(serverJsk), false, "ISO-8859-1");
			}

			return serverJskFile;
		}
	}

	/**
	 *
	 *
	 */
	private JHttp() {
	}

	/**
	 *
	 * @return
	 */
	public static JHttp getInstance(){
		try{
			return createSelfSigned(getServerJsk().getAbsolutePath(),
					serverJskPassword,
					new String[] {"TLSv1.2","TLSv1.3"});
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param protocols
	 * @return
	 */
	public static JHttp getInstance(String[] protocols){
		try{
			return createSelfSigned(getServerJsk().getAbsolutePath(),serverJskPassword,protocols);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param certFilePath
	 * @param certFilePassword
	 * @return
	 */
	public static JHttp getInstance(String certFilePath, String certFilePassword){
		try{
			return createSelfSigned(certFilePath,certFilePassword,new String[] {"TLSv1.2","TLSv1.3"});
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param certFilePath
	 * @param certFilePassword
	 * @param protocols
	 * @return
	 */
	public static JHttp getInstance(String certFilePath, String certFilePassword, String[] protocols){
		try{
			return createSelfSigned(certFilePath,certFilePassword,protocols);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param certFilePath
	 * @param password
	 * @param protocols
	 * @return
	 * @throws Exception
	 */
	public static JHttp createSelfSigned(String certFilePath,String password,String[] protocols) throws Exception{
		JHttp jhttp=new JHttp();

		File certFile = new File(certFilePath);
		if(!certFile.exists() && Startup.deployAsJar()) certFile = Nvwa.getTempFileOfResourceInJar(certFilePath);

		SSLContext ctx = SSLContexts.custom().loadTrustMaterial(certFile,password.toCharArray(), new TrustSelfSignedStrategy()).build();
		ctx.init(null, new TrustManager[] { new MyTrustManager() }, null);

		jhttp.factory = new SSLConnectionSocketFactory(
				ctx,
				protocols,
				null,
				org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE);

		jhttp.poolingmgr = new PoolingHttpClientConnectionManager(
				RegistryBuilder.<ConnectionSocketFactory>create()
						.register("http", PlainConnectionSocketFactory.getSocketFactory())
						.register("https", jhttp.factory)
						.build(),
				null,
				null,
				null,
				5000,
				TimeUnit.MILLISECONDS);

		jhttp.poolingmgr.setDefaultMaxPerRoute(100);
		jhttp.poolingmgr.setMaxTotal(1000);

		return jhttp;
	}

	/**
	 *
	 * @param certFilePath
	 * @param password
	 * @param protocols
	 * @return
	 * @throws Exception
	 */
	public static JHttp createSelfSignedX(URL certFilePath,String password,String[] protocols) throws Exception{
		JHttp jhttp=new JHttp();

		SSLContext ctx = SSLContexts.custom().loadTrustMaterial(certFilePath,password.toCharArray(), new TrustSelfSignedStrategy()).build();
		ctx.init(null, new TrustManager[] { new MyTrustManager() }, null);

		jhttp.factory = new SSLConnectionSocketFactory(
				ctx,
				protocols,
				null,
				org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE);


		jhttp.poolingmgr = new PoolingHttpClientConnectionManager(
				RegistryBuilder.<ConnectionSocketFactory>create()
						.register("http", PlainConnectionSocketFactory.getSocketFactory())
						.register("https", jhttp.factory)
						.build(),
				null,
				null,
				null,
				5000,
				TimeUnit.MILLISECONDS);

		jhttp.poolingmgr.setDefaultMaxPerRoute(100);
		jhttp.poolingmgr.setMaxTotal(1000);

		return jhttp;
	}

	/**
	 *
	 * @param certFilePath
	 * @param password
	 * @param keyStoreType
	 * @param protocals
	 * @return
	 * @throws Exception
	 */
	public static JHttp create(String certFilePath,String password,String keyStoreType,String[] protocals) throws Exception{
		JHttp jhttp=new JHttp();

		File certFile = new File(certFilePath);
		if(!certFile.exists() && Startup.deployAsJar()) certFile = Nvwa.getTempFileOfResourceInJar(certFilePath);

		KeyStore trustStore = KeyStore.getInstance(keyStoreType);
		FileInputStream fis=new FileInputStream(certFile);
		try{
			trustStore.load(fis, password.toCharArray());
		}finally {
			fis.close();
		}

		SSLContext ctx = SSLContexts.custom().loadKeyMaterial(trustStore, password.toCharArray()).build();

		KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyFactory.init(trustStore, password.toCharArray());
		ctx.init(keyFactory.getKeyManagers(), new TrustManager[]{new MyTrustManager()}, null);

		//"SSLv3","TLSv1","TLSv1.1","TLSv1.2","TLSv1.3"
		jhttp.factory = new SSLConnectionSocketFactory(
				ctx,
				protocals==null || protocals.length==0? new String[] {"TLSv1.2","TLSv1.3"} : protocals,
				null,
				org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE);

		jhttp.poolingmgr = new PoolingHttpClientConnectionManager(
				RegistryBuilder.<ConnectionSocketFactory>create()
						.register("http", PlainConnectionSocketFactory.getSocketFactory())
						.register("https", jhttp.factory)
						.build(),
				null,
				null,
				null,
				5000,
				TimeUnit.MILLISECONDS);

		jhttp.poolingmgr.setDefaultMaxPerRoute(10);
		jhttp.poolingmgr.setMaxTotal(100);

		return jhttp;
	}

	/**
	 *
	 * @param defaultMaxPerRoute
	 * @param maxTotal
	 */
	public void setPoolingManager(int defaultMaxPerRoute, int maxTotal) {
		poolingmgr.setDefaultMaxPerRoute(defaultMaxPerRoute);
		poolingmgr.setMaxTotal(maxTotal);
	}

	/**
	 *
	 * @param cookieSpec
	 */
	public void setCookieSpec(String cookieSpec) {
		this.cookieSpec=cookieSpec;
	}

	/**
	 *
	 * @param enabled
	 */
	public void setRedirectsEnabled(boolean enabled) {
		this.redirectsEnabled=enabled;
	}

	/**
	 *
	 * @return
	 */
	public boolean getRedirectsEnabled() {
		return this.redirectsEnabled;
	}

	/**
	 *
	 *
	 */
	public void destroy(){
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize(){
		destroy();
	}

	/**
	 *
	 * @param client
	 * @return
	 */
	public RequestConfig getConfigOfClient(HttpClient client) {
		return configOfClients.get(client.toString());
	}

	/**
	 *
	 * @param client
	 * @return
	 */
	public CredentialsProvider getCredentialsOfClient(HttpClient client) {
		return credentialsOfClients.get(client.toString());
	}

	/**
	 *
	 * @param client
	 * @param config
	 */
	public void setConfigOfClient(HttpClient client, RequestConfig config, CredentialsProvider credsProvider) {
		configOfClients.put(client.toString(), config);
		if(credsProvider!=null) credentialsOfClients.put(client.toString(), credsProvider);
	}

	/**
	 *
	 * @param client
	 * @return
	 */
	public ProxyUsage getProxyOfClient(HttpClient client) {
		return proxyOfClients.get(client.toString());
	}


	/**
	 *
	 * @param client
	 * @param proxyUsage
	 * @return
	 */
	public boolean isProxyOfClientChanged(HttpClient client, ProxyUsage proxyUsage) {
		ProxyUsage old=this.getProxyOfClient(client);
		if(old==null) return false;

		return !JUtilString.equals(old.getProxyIp(), proxyUsage.getProxyIp())
				||!JUtilMath.equals(old.getProxyPort(), proxyUsage.getProxyPort())
				||!JUtilString.equals(old.getProxyUsername(), proxyUsage.getProxyUsername())
				||!JUtilString.equals(old.getProxyPassword(), proxyUsage.getProxyPassword());
	}

	/**
	 *
	 * @param client
	 */
	public void removeClient(HttpClient client) {
		if(client==null) return;
		this.proxyOfClients.remove(client.toString());
		this.configOfClients.remove(client.toString());
		this.credentialsOfClients.remove(client.toString());
	}

	/**
	 *
	 * @param client
	 * @param cookieSpec
	 */
	public void setCookieSpec(HttpClient client, String cookieSpec) {
		RequestConfig config=getConfigOfClient(client);
		if(config==null){
			config=RequestConfig.custom()
					.setCookieSpec(cookieSpec)
					.setMaxRedirects(default_redirects)
					.setRedirectsEnabled(getRedirectsEnabled())
					.build();
		}else{
			config=RequestConfig.custom()
					.setCookieSpec(cookieSpec)
					.setMaxRedirects(config.getMaxRedirects())
					.setRedirectsEnabled(getRedirectsEnabled())
					.setSocketTimeout(config.getSocketTimeout())
					.setConnectTimeout(config.getConnectTimeout())
					.build();
		}
		setConfigOfClient(client, config, null);
	}

	/**
	 *
	 * @param client
	 * @param proxyUsage
	 */
	public void setProxyOfClient(HttpClient client, ProxyUsage proxyUsage) {
		proxyOfClients.put(client.toString(), proxyUsage);
		setProxy(client, proxyUsage.getProxyIp(), proxyUsage.getProxyPort(), proxyUsage.getProxyUsername(), proxyUsage.getProxyPassword());
	}

	/**
	 *
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 */
	public void setProxy(HttpClient client, String host,int port,String username, String password) {
		HttpHost proxy=new HttpHost(host,port,"http");

		if(JUtilString.isBlank(username)) {
			CredentialsProvider credsProvider=getCredentialsOfClient(client);
			if(credsProvider!=null) credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		}

		RequestConfig config=getConfigOfClient(client);
		if(config==null){
			System.out.println("[JHttp] set proxy for client "+client.toString()+" -> "+host+":"+port);
			config=RequestConfig.custom()
					.setCookieSpec(this.cookieSpec)
					.setMaxRedirects(default_redirects)
					.setRedirectsEnabled(getRedirectsEnabled())
					.setProxy(proxy)
					.build();
		}else{
			if(config.getProxy()!=null && !host.equals(config.getProxy().getHostName())) {
				System.out.println("[JHttp] change proxy for client "+client.toString()+" -> "+host+":"+port);
			}
			config=RequestConfig.custom()
					.setCookieSpec(config.getCookieSpec())
					.setMaxRedirects(config.getMaxRedirects())
					.setRedirectsEnabled(getRedirectsEnabled())
					.setSocketTimeout(config.getSocketTimeout())
					.setConnectTimeout(config.getConnectTimeout())
					.setProxy(proxy)
					.build();
		}
		setConfigOfClient(client, config, null);
	}

	/**
	 * @param client
	 */
	public void clearProxy(HttpClient client) {
		RequestConfig config=getConfigOfClient(client);
		if(config==null){
			config=RequestConfig.custom()
					.setCookieSpec(this.cookieSpec)
					.setMaxRedirects(default_redirects)
					.setRedirectsEnabled(getRedirectsEnabled())
					.setProxy(null)
					.build();
		}else{
			config=RequestConfig.custom()
					.setCookieSpec(config.getCookieSpec())
					.setMaxRedirects(config.getMaxRedirects())
					.setRedirectsEnabled(getRedirectsEnabled())
					.setSocketTimeout(config.getSocketTimeout())
					.setConnectTimeout(config.getConnectTimeout())
					.setProxy(null)
					.build();
		}
		setConfigOfClient(client, config, null);
	}

	/**
	 *
	 * @param timeout
	 * @param redirects
	 * @return
	 */
	public RequestConfig createClientConfig(int timeout, int redirects) {
		return RequestConfig.custom()
				.setCookieSpec(this.cookieSpec)
				.setMaxRedirects(redirects)
				.setRedirectsEnabled(getRedirectsEnabled())
				.setRelativeRedirectsAllowed(getRedirectsEnabled())
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.setProxy(null)
				.build();
	}


	/**
	 * @deprecated
	 * @param timeout
	 * @param host
	 * @param port
	 * @param scheme
	 * @param username
	 * @param password
	 * @return
	 */
	public RequestConfig createClientConfig(int timeout, String host,int port,String scheme,String username, String password) {
		return createClientConfig(timeout, default_redirects, host, port, scheme, username, password);
	}

	/**
	 *
	 * @param timeout
	 * @param redirects
	 * @param host
	 * @param port
	 * @param scheme
	 * @param username
	 * @param password
	 * @return
	 */
	public RequestConfig createClientConfig(int timeout, int redirects, String host,int port,String scheme,String username, String password) {
		HttpHost proxy=new HttpHost(host,port,scheme==null?"http":scheme);

		return RequestConfig.custom()
				.setCookieSpec(this.cookieSpec)
				.setMaxRedirects(redirects)
				.setRedirectsEnabled(getRedirectsEnabled())
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.setProxy(proxy)
				.build();
	}


	/**
	 *
	 * @return
	 */
	public HttpClient createClient() {
		return createClient(15000);
	}

	/**
	 *
	 * @param timeout
	 * @return
	 */
	public HttpClient createClient(int timeout) {
		return createClient(timeout, default_redirects);
	}

	/**
	 *
	 * @param timeout
	 * @param redirects
	 * @return
	 */
	public HttpClient createClient(int timeout, int redirects) {
		RequestConfig requestConfig = createClientConfig(timeout, redirects);

		PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
		Registry<CookieSpecProvider> r = RegistryBuilder.<CookieSpecProvider>create()
				.register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider(publicSuffixMatcher))
				.register(CookieSpecs.STANDARD, new RFC6265CookieSpecProvider(publicSuffixMatcher))
				.register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecProvider())
				.build();

		CloseableHttpClient client = HttpClients.custom()
				.setSSLSocketFactory(factory)
				.setConnectionManager(poolingmgr)
				.setDefaultCookieSpecRegistry(r)
				.setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(requestConfig)
				.setRedirectStrategy(new NoRedirectStrategy())
				.build();

		setConfigOfClient(client, requestConfig, null);

		return client;
	}

	/**
	 *
	 * @param host
	 * @param port
	 * @param scheme
	 * @param username
	 * @param password
	 * @return
	 */
	public HttpClient createClient(String host,int port,String scheme,String username, String password) {
		return createClient(15000,JHttp.default_redirects,host,port,scheme,username,password);
	}

	/**
	 *
	 * @param timeout
	 * @param redirects
	 * @param host
	 * @param port
	 * @param scheme
	 * @param username
	 * @param password
	 * @return
	 */
	public HttpClient createClient(int timeout,int redirects,String host,int port,String scheme,String username, String password) {
		CredentialsProvider credsProvider=null;
		if(JUtilString.isBlank(username)) {
			credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		}

		RequestConfig requestConfig = createClientConfig(timeout,
				redirects,
				host,
				port,
				scheme,
				username,
				password);

		PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
		Registry<CookieSpecProvider> r = RegistryBuilder.<CookieSpecProvider>create()
				.register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider(publicSuffixMatcher))
				.register(CookieSpecs.STANDARD, new RFC6265CookieSpecProvider(publicSuffixMatcher))
				.register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecProvider())
				.build();

		CloseableHttpClient client = null;
		if(credsProvider!=null) {
			client=HttpClients.custom()
					.setSSLSocketFactory(factory)
					.setConnectionManager(poolingmgr)
					.setDefaultCookieSpecRegistry(r)
					.setDefaultCookieStore(cookieStore)
					.setDefaultRequestConfig(requestConfig)
					.setDefaultCredentialsProvider(credsProvider)
					.build();
		}else {
			client=HttpClients.custom()
					.setSSLSocketFactory(factory)
					.setConnectionManager(poolingmgr)
					.setDefaultCookieSpecRegistry(r)
					.setDefaultCookieStore(cookieStore)
					.setDefaultRequestConfig(requestConfig)
					.build();
		}

		setConfigOfClient(client, requestConfig, credsProvider);

		return client;
	}

	/**
	 *
	 * @param context
	 * @param response
	 * @param request
	 */
	private void getStatusAndHeaders(JHttpContext context, HttpResponse response,HttpRequestBase request){
		if(context==null||response==null) return;

		StatusLine status=response.getStatusLine();
		if(status!=null) {
			context.setStatus(status.getStatusCode());
			if(HttpStatus.SC_MOVED_TEMPORARILY==status.getStatusCode()) {
				Header header=response.getFirstHeader("Location");
				if(header!=null) {
					context.addResponseHeader(header.getName(),header.getValue());
				}
			}
		}

		Header[] headers = response.getAllHeaders();
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

		List<Cookie> cookies=cookieStore.getCookies();
		if(cookies!=null) {
			for(int i=0; i<cookies.size(); i++) {
				Cookie c=cookies.get(i);

				context.saveResponseCookie(c.getName(),
						c.getValue(),
						c.getVersion(),
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
	private void initRequest(JHttpContext context,HttpClient client,HttpRequestBase request){
		if(context==null||request==null) return;
		context.setRequest(request);

		if (context.getRequestHeader("User-Agent") == null) {
			request.addHeader("User-Agent",default_user_agent);
		}


		String sCookies="";
		Map cookies=context.getCookies();
		if(cookies!=null&&!cookies.isEmpty()){
			for(Iterator it=cookies.keySet().iterator();it.hasNext();){
				String name=(String)it.next();
				JHttpCookie c=(JHttpCookie)cookies.get(name);

				if(!"".equals(sCookies)) sCookies+="; ";
				sCookies+=name+"="+c.getValue();

				BasicClientCookie cookie = new BasicClientCookie(name, c.getValue());
				//cookie.setVersion(c.getVersion());
				cookie.setDomain(c.getDomain());
				cookie.setPath(c.getPath());
				if(c.getExpired() != null) cookie.setExpiryDate(c.getExpired());
				cookieStore.addCookie(cookie);
			}
		}

//		if(!"".equals(sCookies)) {
//			context.addRequestHeader("Cookie", sCookies);
//		}

		Map headers=context.getRequestHeaders();
		if(headers!=null&&!headers.isEmpty()){
			for(Iterator it=headers.keySet().iterator();it.hasNext();){
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
	 * @param context
	 * @param request
	 * @param params
	 * @throws Exception
	 */
	private static void addParams(JHttpContext context,HttpPost request,Map params) throws Exception{
		if (params != null && !params.isEmpty()) {
			List formparams = new ArrayList();
			Iterator keys = params.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Object val = params.get(key);
				if(val!=null && val instanceof String[]) {
					String[] vals=(String[])val;
					for(int i=0; i<vals.length;i++) {
						formparams.add(new BasicNameValuePair((String)key, vals[i]));
					}
				}else if(val!=null && val instanceof List) {
					List vals=(List)val;
					for(int i=0; i<vals.size();i++) {
						formparams.add(new BasicNameValuePair((String)key, (String)vals.get(i)));
					}
				}else {
					formparams.add(new BasicNameValuePair((String)key, val==null?"":val.toString()));
				}
			}
			if(context.getRequestEncoding()==null) request.setEntity(new UrlEncodedFormEntity(formparams));
			else request.setEntity(new UrlEncodedFormEntity(formparams,context.getRequestEncoding()));
		}
	}

	/**
	 *
	 * @param context
	 * @param request
	 * @param strings
	 * @throws Exception
	 */
	private static void addParams(JHttpContext context,HttpPost request,Map parts,Map strings) throws Exception{
		HttpEntity reqEntity=null;
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if(parts!=null&&!parts.isEmpty()){
			Iterator keys = parts.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Object val = parts.get(key);
				if(val instanceof File){
					builder=builder.addPart((String)key, new FileBody((File)val));
				}else if(val instanceof byte[]){
					builder=builder.addPart((String)key, new ByteArrayBody((byte[])val,(String)key));
				}else if(val instanceof InputStream){
					builder=builder.addPart((String)key, new InputStreamBody((InputStream)val,(String)key));
				}else if(val instanceof String[]){
					String[] array=(String[])val;
					for(int i=0; i<array.length; i++) {
						if(context.getRequestEncoding()!=null&&!"".equals(context.getRequestEncoding())){
							builder=builder.addPart((String)key, new StringBody(array[i],ContentType.create(context.getContentType()==null?"text/plain":context.getContentType(),Charset.forName(context.getRequestEncoding()))));
						}else{
							builder=builder.addPart((String)key, new StringBody(array[i],ContentType.TEXT_PLAIN));
						}
					}
				}else if(val!=null && val instanceof List) {
					List vals=(List)val;
					for(int i=0; i<vals.size();i++) {
						if(context.getRequestEncoding()!=null&&!"".equals(context.getRequestEncoding())){
							builder=builder.addPart((String)key, new StringBody((String)vals.get(i),ContentType.create(context.getContentType()==null?"text/plain":context.getContentType(),Charset.forName(context.getRequestEncoding()))));
						}else{
							builder=builder.addPart((String)key, new StringBody((String)vals.get(i),ContentType.TEXT_PLAIN));
						}
					}
				}else if(val instanceof String){
					if(context.getRequestEncoding()!=null&&!"".equals(context.getRequestEncoding())){
						builder=builder.addPart((String)key, new StringBody((String)val,ContentType.create(context.getContentType()==null?"text/plain":context.getContentType(),Charset.forName(context.getRequestEncoding()))));
					}else{
						builder=builder.addPart((String)key, new StringBody((String)val,ContentType.TEXT_PLAIN));
					}
				}
			}
		}


		if(strings!=null&&!strings.isEmpty()){
			Iterator keys = strings.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Object val = strings.get(key);
				if(context.getRequestEncoding()!=null&&!"".equals(context.getRequestEncoding())){
					builder=builder.addPart((String)key, new StringBody((String)val,ContentType.create(context.getContentType()==null?"text/plain":context.getContentType(),Charset.forName(context.getRequestEncoding()))));
				}else{
					builder=builder.addPart((String)key, new StringBody((String)val,ContentType.TEXT_PLAIN));
				}
			}
		}

		reqEntity=builder.build();

		request.setEntity(reqEntity);
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
	private void execute(JHttpContext context,HttpClient client,HttpRequestBase request,String encoding,int responseType) throws Exception{
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
			throw new Exception("get "+context.getStatus()+" error while request "+request.getURI());
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
	private void doExecute(JHttpContext context,HttpClient client,HttpRequestBase request,String encoding,int responseType,boolean abort) throws Exception{
		ProxyUsage proxyUsage=getProxyOfClient(client);
		try {
			if(proxyUsage!=null){//自动从ProxyPool获取最新代理IP
				proxyUsage.use();
				if(isProxyOfClientChanged(client, proxyUsage)) setProxyOfClient(client, proxyUsage);
			}

			RequestConfig config=getConfigOfClient(client);
			if(config!=null) request.setConfig(config);

			HttpResponse response = client.execute(request);
			getStatusAndHeaders(context,response,request);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				if(responseType==0){
					boolean isGzip=isGzip(entity);
					String responseText=null;
					if(isGzip){
						if(encoding!=null) responseText = JUtilCompressor.readGZipStream2String(entity.getContent(),encoding);
						else responseText = JUtilCompressor.readGZipStream2String(entity.getContent());
					}else{
						if(encoding!=null) responseText = JUtilInputStream.string(entity.getContent(),encoding);
						else responseText = JUtilInputStream.string(entity.getContent());
					}
					context.setResponseText(responseText);
					EntityUtils.consume(entity);
					if(abort){
						try{
							request.releaseConnection();
							request.abort();
						}catch(Exception e){}
					}
				}else if(responseType==1){
					context.setResponseStream(entity.getContent());
				}
			}

			if(proxyUsage!=null)  proxyUsage.onOk();
		} catch (Exception e) {
			try{
				if(proxyUsage!=null) proxyUsage.onError();
				if(request!=null && abort) request.abort();
			}catch(Exception ex){}
			throw e;
		}
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	private static boolean isGzip(HttpEntity entity){
		if(entity==null) return false;
		Header header = entity.getContentEncoding();
		return (header != null&&"gzip".equalsIgnoreCase(header.getValue()));
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public JHttpContext get(JHttpContext context,HttpClient client, String url) throws Exception {
		return get(context,client,url,(String)null);
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
	public JHttpContext get(JHttpContext context,HttpClient client, String url, String encoding)throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");
		HttpGet request = new HttpGet(url);

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
	public JHttpContext post(JHttpContext context,HttpClient client, String url, Map params)throws Exception {
		return post(context,client,url,params,null);
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
	public JHttpContext post(JHttpContext context,HttpClient client, String url,Map params,String encoding) throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");
		HttpPost request = new HttpPost(url);

		initRequest(context,client,request);

		if(context.getRequestBody()!=null){
			StringEntity se=null;
			if(context.getRequestEncoding()==null) {
				se=new StringEntity(context.getRequestBody());
			}else{
				se=new StringEntity(context.getRequestBody(),context.getRequestEncoding());
			}
			request.setEntity(se);
		}else{
			addParams(context,request,params);
		}

		execute(context,client,request,encoding,0);

		return context;
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param parts
	 * @param strings
	 * @return
	 * @throws Exception
	 */
	public JHttpContext postMultipartData(JHttpContext context,HttpClient client, String url,Map parts,Map strings)throws Exception {
		return postMultipartData(context,client,url,parts,strings,null);
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param parts
	 * @param strings
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public JHttpContext postMultipartData(JHttpContext context,HttpClient client, String url, Map parts, Map strings, String encoding) throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");

		//如果指定了通过代理访问
		if(context!=null && !JUtilString.isBlank(context.getProxyByUrl())){
			String proxyByUrl = context.getProxyByUrl();
			context.setProxyByUrl(null);

			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_URL+"="+JUtilString.encodeURI(url, "UTF-8");
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_METHOD+"=POST";
			if(!JUtilString.isBlank(context.getRequestEncoding())){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_REQUEST_ENCODEING+"="+JUtilString.encodeURI(context.getRequestEncoding(), "UTF-8");
			}

			Client ssoClient= SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());
			if(ssoClient!=null){
				return Signature.requestMultipart(this, client, context, proxyByUrl, parts, strings, encoding, ssoClient.getAccessKey(), ssoClient.getAccessSecret());
			}else{
				return postMultipartData(context, client, proxyByUrl, parts, strings, encoding);
			}
		}

		HttpPost request = new HttpPost(url);

		initRequest(context,client,request);
		addParams(context,request,parts,strings);
		execute(context,client,request,encoding,0);

		return context;
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @param bytes
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public JHttpContext postBytes(JHttpContext context,HttpClient client, String url, byte[] bytes, String encoding) throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");

		HttpPost request = new HttpPost(url);

		initRequest(context,client,request);

		//封装字节数组为请求体，并设置Content-Type
		ByteArrayEntity entity = new ByteArrayEntity(
				bytes,
				ContentType.APPLICATION_OCTET_STREAM // 二进制流类型
		);
		request.setEntity(entity);

		execute(context,client,request,encoding,1);

		return context;
	}

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @throws Exception
	 */
	public JHttpContext getStream(JHttpContext context,HttpClient client, String url) throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");

		//如果指定了通过代理访问
		if(context!=null && !JUtilString.isBlank(context.getProxyByUrl())){
			String proxyByUrl = context.getProxyByUrl();
			context.setProxyByUrl(null);

			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_URL+"="+JUtilString.encodeURI(url, "UTF-8");
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_METHOD+"=GET";
			if(!JUtilString.isBlank(context.getRequestEncoding())){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_REQUEST_ENCODEING+"="+JUtilString.encodeURI(context.getRequestEncoding(), "UTF-8");
			}
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_GET_STREAM+"=true";

			Client ssoClient= SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());
			if(ssoClient!=null){
				if(!JUtilString.isBlank(context.getRequestBody())){
					//System.out.println("postResponse by proxy url\r\n=> "+proxyByUrl+"\r\n=> "+context.getRequestBody());
					context = Signature.requestStream(this, client, context, proxyByUrl, context.getRequestBody(), ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				}else{
					//System.out.println("postResponse by proxy url\r\n=> "+proxyByUrl+"\r\n=> "+JUtilBean.map2Json(params));
					context = Signature.requestStream(this, client, context, proxyByUrl, null, context.getRequestBody(), ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				}
				return context;
			}else{
				return getStream(context, client, proxyByUrl);
			}
		}

		HttpGet request = new HttpGet(url);

		initRequest(context,client,request);

		execute(context,client,request,null,1);

		return context;
	}


	/**
	 *
	 * @param client
	 * @param url
	 * @param params
	 * @return
	 */
	public JHttpContext postStream(JHttpContext context,HttpClient client, String url, Map params)throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");

		//如果指定了通过代理访问
		if(context!=null && !JUtilString.isBlank(context.getProxyByUrl())){
			String proxyByUrl = context.getProxyByUrl();
			context.setProxyByUrl(null);

			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_URL+"="+JUtilString.encodeURI(url, "UTF-8");
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_METHOD+"=POST";
			if(!JUtilString.isBlank(context.getRequestEncoding())){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_REQUEST_ENCODEING+"="+JUtilString.encodeURI(context.getRequestEncoding(), "UTF-8");
			}
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_GET_STREAM+"=true";

			Client ssoClient= SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());
			if(ssoClient!=null){
				if(!JUtilString.isBlank(context.getRequestBody())){
					//System.out.println("postResponse by proxy url\r\n=> "+proxyByUrl+"\r\n=> "+context.getRequestBody());
					context = Signature.requestStream(this, client, context, proxyByUrl, context.getRequestBody(), ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				}else{
					//System.out.println("postResponse by proxy url\r\n=> "+proxyByUrl+"\r\n=> "+JUtilBean.map2Json(params));
					context = Signature.requestStream(this, client, context, proxyByUrl, params, context.getRequestBody(), ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				}
				return context;
			}else{
				return postStream(context, client, proxyByUrl, params);
			}
		}

		HttpPost request = new HttpPost(url);
		initRequest(context,client,request);
		if(context.getRequestBody()!=null){
			StringEntity se=null;
			if(context.getRequestEncoding()==null) {
				se=new StringEntity(context.getRequestBody());
			}else{
				se=new StringEntity(context.getRequestBody(),context.getRequestEncoding());
			}
			request.setEntity(se);
		}else{
			addParams(context,request,params);
		}
		execute(context,client,request,null,1);
		return context;
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
	public JHttpContext delete(JHttpContext context, HttpClient client, String url, String encoding)throws Exception {
		if(context == null) context = new JHttpContext();
		if(client == null) client = createClient();

		url=JUtilString.replaceAll(url," ","%20");
		url=JUtilString.replaceAll(url, "$CURRENT_TIME_MILLIS", SysUtil.getNow()+"");
		HttpDelete request = new HttpDelete(url);

		initRequest(context,client,request);
		execute(context,client,request,encoding,1);

		return context;
	}

	public static String pipe(HttpServletRequest request,
					   String httMethod,
					   String host,
					   int port,
					   String requestUri) throws Exception{
		return pipe(request, httMethod, host, port, requestUri, null, false, null);
	}

	/**
	 *
	 * @param request 原始请求
	 * @param httMethod POST 或者 GET
	 * @param host
	 * @param port
	 * @param requestUri
	 * @param ignoredHeaders
	 * @param useOrigionalClientIp 使用原始客户端IP
	 * @param machineIp pipe所在节点的IP
	 * @throws Exception
	 */
	public static String pipe(HttpServletRequest request,
					   String httMethod,
					   String host,
					   int port,
					   String requestUri,
					   List<String> ignoredHeaders,
					   boolean useOrigionalClientIp,
					   String machineIp) throws Exception{
		Socket socket = new Socket(host,port);
		try {
			OutputStream out=socket.getOutputStream();
			out.write((httMethod+" "+requestUri+" HTTP/1.1\r\n").getBytes());
			Enumeration hns=request.getHeaderNames();
			while(hns.hasMoreElements()){
				String n=hns.nextElement().toString();
				String nLowerCase = n.toLowerCase();
				if(ignoredHeaders!=null && ignoredHeaders.contains(nLowerCase)) continue;//忽略的头部

				//框架自用参数
				if(Constants.ACCESS_KEY.equalsIgnoreCase(n) || Constants.SIGNATURE.equalsIgnoreCase(n)) continue;

				if(!useOrigionalClientIp){//不使用原始客户端IP
					if("x-forwarded-for".equals(nLowerCase)
							||"x-real-ip".equals(nLowerCase)
							||"proxy_forwarded_for".equals(nLowerCase)
							||"true-client-ip".equals(nLowerCase)
							||"remote-host".equals(nLowerCase)
							||"remote-addr".equals(nLowerCase)){
						continue;
					}
				}
				out.write((n+":  "+request.getHeader(n)+"\r\n").getBytes());
			}
			if(!useOrigionalClientIp){
				if(!JUtilString.isBlank(machineIp)) out.write(("x-real-ip:  "+ machineIp +"\r\n").getBytes());
				else if(!JUtilString.isBlank(JProperties.getEnv("MachineIp"))) out.write(("x-real-ip:  "+ JProperties.getEnv("MachineIp") +"\r\n").getBytes());
			}
			out.write("\r\n".getBytes());

			InputStream in=request.getInputStream();
			byte[] buffer=new byte[1024];
			int readed=in.read(buffer);
			while(readed>-1){
				out.write(buffer,0,readed);
				readed=in.read(buffer);
			}
			out.flush();

			BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String  line=reader.readLine();
			StringBuilder responseContent=new StringBuilder();
			boolean responseContentStarted=false;
			while(line!=null){
				if(responseContentStarted){
					responseContent.append(line);
					responseContent.append(Global.lineSeparator);
				}
				if(line.equals("")) responseContentStarted=true;

				line=reader.readLine();
			}

			if(responseContent.length()>=Global.lineSeparator.length()){
				responseContent.delete(responseContent.length() - Global.lineSeparator.length(), responseContent.length());
			}

			return responseContent.toString();
		} finally {
			socket.close();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String getResponse(JHttpContext context,HttpClient client, String url) throws Exception {
		return getResponse(context,client,url,(String)null);
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
	public String getResponse(JHttpContext context,HttpClient client, String url, String encoding)throws Exception {
		//如果指定了通过代理访问
		if(context!=null && !JUtilString.isBlank(context.getProxyByUrl())){
			String proxyByUrl = context.getProxyByUrl();
			context.setProxyByUrl(null);

			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_URL+"="+JUtilString.encodeURI(url, "UTF-8");
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_METHOD+"=GET";
			if(!JUtilString.isBlank(context.getRequestEncoding())){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_REQUEST_ENCODEING+"="+JUtilString.encodeURI(context.getRequestEncoding(), "UTF-8");
			}
			if(!JUtilString.isBlank(encoding)){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_RESPONSE_ENCODEING+"="+JUtilString.encodeURI(encoding, "UTF-8");
			}

			Client ssoClient= SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());
			if(ssoClient!=null){
				context = Signature.request(this, client, context, proxyByUrl, null, null, ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				return context.getResponseText();
			}else{
				return getResponse(context, client, proxyByUrl, encoding);
			}
		}

		context=get(context,client,url,encoding);

		if(context==null||!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("获取网页出错（get） - "+url+" - context - "+context+" status - "+(context==null?"unknown":context.getStatus()));
		}
		String response=context.getResponseText();
		context.finish();

		return response;
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
	public String postResponse(JHttpContext context,HttpClient client, String url, Map params)throws Exception {
		return postResponse(context,client,url,params,null);
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
	public String postResponse(JHttpContext context,HttpClient client, String url,Map params,String encoding) throws Exception {
		//如果指定了通过代理访问
		if(context!=null && !JUtilString.isBlank(context.getProxyByUrl())){
			String proxyByUrl = context.getProxyByUrl();
			context.setProxyByUrl(null);

			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_URL+"="+JUtilString.encodeURI(url, "UTF-8");
			proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_METHOD+"=POST";
			if(!JUtilString.isBlank(context.getRequestEncoding())){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_REQUEST_ENCODEING+"="+JUtilString.encodeURI(context.getRequestEncoding(), "UTF-8");
			}
			if(!JUtilString.isBlank(encoding)){
				proxyByUrl += (proxyByUrl.indexOf("?") > 0 ? "&" : "?") + ProxyHandler.PROXY_TO_RESPONSE_ENCODEING+"="+JUtilString.encodeURI(encoding, "UTF-8");
			}

			Client ssoClient= SSOConfig.getSsoClientByIdOrUrl(SysConfig.getSysId());
			if(ssoClient!=null){
				if(!JUtilString.isBlank(context.getRequestBody())){
					//System.out.println("postResponse by proxy url\r\n=> "+proxyByUrl+"\r\n=> "+context.getRequestBody());
					context = Signature.request(this, client, context, proxyByUrl, context.getRequestBody(), ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				}else{
					//System.out.println("postResponse by proxy url\r\n=> "+proxyByUrl+"\r\n=> "+JUtilBean.map2Json(params));
					context = Signature.request(this, client, context, proxyByUrl, params, context.getRequestBody(), ssoClient.getAccessKey(), ssoClient.getAccessSecret());
				}
				return context.getResponseText();
			}else{
				return postResponse(context, client, proxyByUrl, params, encoding);
			}
		}

		context=post(context,client,url,params,encoding);
		if(context==null||!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("获取网页出错（post） - "+url+" - context - "+context+" status - "+(context==null?"unknown":context.getStatus()));
		}
		String response=context.getResponseText();
		context.finish();

		return response;
	}


	/**
	 *
	 * @param context
	 * @param client
	 * @param url
	 * @throws Exception
	 */
	public InputStream getStreamResponse(JHttpContext context,HttpClient client, String url) throws Exception {
		context=getStream(context,client,url);

		if(context==null||!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("获取网页输入流出错（get） - "+url+" - context - "+context+" status - "+(context==null?"unknown":context.getStatus()));
		}

		InputStream is=context.getResponseStream();

		return is;
	}


	/**
	 *
	 * @param client
	 * @param url
	 * @param params
	 * @return
	 */
	public InputStream postStreamResponse(JHttpContext context,HttpClient client, String url, Map params)throws Exception {
		context=postStream(context,client,url,params);

		if(context==null||!context.isErrorCodeAllowed(context.getStatus())){
			throw new Exception("获取网页输入流出错（post） - "+url+" - context - "+context+" status - "+(context==null?"unknown":context.getStatus()));
		}

		InputStream is=context.getResponseStream();

		return is;
	}


	///////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @deprecated 请使用j.http.HttpUtil中对应的方法
	 * 得到访问者IP
	 * @param request
	 * @return
	 */
	public static String getRemoteIp(HttpServletRequest request){
		String[] ips=getRemoteIps(request);
		return ips.length==0?"":ips[0];
	}

	/**
	 * @deprecated 请使用j.http.HttpUtil中对应的方法
	 * @param request
	 * @return
	 */
	public static String getUserAgent(HttpServletRequest request){
		return request.getHeader("user-agent");
	}

	/**
	 * @deprecated 请使用j.http.HttpUtil中对应的方法
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
	 * 测试
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args){
//		try{
//			String charset = "utf-8";
//			String msg_type = "CNTECH_LV_LOGISTICS_DETAIL_GET";
//			String url = "https://link.cainiao.com/gateway/link.do";
//			String logistic_provider_id = "8a2fd6990b4e95e09234a512bcb74eab";
//			String appCode = "cq-ygdz";
//			String APPSECERT = "g21LWe070R1b08G5V7X0w60Lg20nYQ72";
//
//			StringBuffer logistics_interface=new StringBuffer();
//			logistics_interface.append("{\"arg0\":{\"cpCode\":\"ZTO\"");
//			logistics_interface.append(",\"mailNo\":\"73188530914144\"");
//			logistics_interface.append(",\"appCode\":\""+appCode+"\"}");
//			logistics_interface.append("}");
//
//			Map<String, String> params=new HashMap<>();
//			params.put("logistics_interface", logistics_interface.toString());
//			params.put("logistic_provider_id", logistic_provider_id);
//			params.put("msg_type", msg_type);
//			params.put("data_digest", doSign(logistics_interface.toString(), charset, APPSECERT));
//
//			System.out.println(JUtilBean.map2Json(params));
//
//			JHttp http=JHttp.getInstance();
//
//			String resp=http.postResponse(null, null, url, params, "UTF-8");
//			System.out.println("resp:"+resp);
//		}catch(Exception e){
//			e.printStackTrace();
//		}

//		String url="https://bspgw.sf-express.com/std/service";
//		String clientCode="ZQYGDZSW";
//		String checkword="f6TYeqonn6LkrurRYoAdwGh3wKLxUy6M";
//
//		StringBuffer msgData = new StringBuffer("");
//		msgData.append("{\"language\":0");
//		msgData.append(",\"trackingType\":1");
//		msgData.append(",\"trackingNumber\":[\"194778626454\"]}");
//		System.out.println("msgData:"+msgData);
//
//		try{
//			String timestamp=SysUtil.getNow()+"";
//			String respText= ExpressImplSFUtil.call(url, "EXP_RECE_SEARCH_ROUTES", clientCode, checkword, timestamp, msgData.toString());
//
//			System.out.println("respText:"+respText);
//		}catch(Exception e){
//			e.printStackTrace();
//		}

//		try{
//			String url="https://japi.zto.com/zto.open.getRouteInfo";
//
//			StringBuffer content=new StringBuffer();
//			content.append("{\"billCode\":\"73188530914144\"}");
//
//			JHttp http=JHttp.getInstance();
//			JHttpContext context=new JHttpContext();
//			context.setContentType("application/json; charset=utf-8");
//			context.addRequestHeader("x-appKey","bc9e336c153e190b4d75e");
//			context.addRequestHeader("x-datadigest",getDatadigest(null, content.toString()));
//			context.setRequestEncoding("UTF-8");
//			context.setRequestBody(content.toString());
//
//			String resp=http.postResponse(context,null,url,null,"UTF-8");
//			System.out.println("resp:"+resp);
//		}catch(Exception e){
//			e.printStackTrace();
//		}

//		try{
//			String resp = request(null, "express.channel.getlist", "{}");
//			System.out.println(resp);
//		}catch(Exception e){
//			e.printStackTrace();
//		}

		try{
			long now = SysUtil.getNow();
			int year = JUtilTimestamp.getValue(now, Calendar.YEAR);
			int month = JUtilTimestamp.getValue(now, Calendar.MONTH);
			int date = JUtilTimestamp.getValue(now, Calendar.DATE);

			System.out.println(year);
			System.out.println(month);
			System.out.println(date);

			System.out.println(JUtilString.encodeURI("cdr:1,cd_min:11/18/2024,cd_max:11/18/2024", "UTF-8"));
			String url="https://www.google.com/search?q=" + JUtilString.encodeURI("暴雨", "UTF-8");
			url += "&source=lnt";
			url += "&tbs="+JUtilString.encodeURI("cdr:1,cd_min:11/18/2024,cd_max:11/18/2024", "UTF-8");
			url += "&tbm=";

			JHttpContext context = new JHttpContext();

			JHttp http = JHttp.getInstance();
			HttpClient client = http.createClient("127.0.0.1", 21882, "http", "", "");
			String resp = http.getResponse(context, client, "https://www.google.com/", "UTF-8");

			try{
				Thread.sleep(5000);
			}catch (Exception e){}

			context.addRequestHeader("pragma", "no-cache");
			context.addRequestHeader("x-browser-year", "2024");
			resp = http.getResponse(context, client, url, "UTF-8");

			int start=resp.indexOf("<div id=\"result-stats\">");
			if(start>0){
				start += "<div id=\"result-stats\">".length();
				int end = resp.indexOf("<nobr>", start);
				if(end>start){
					String total = resp.substring(start, end);
					total = JUtilString.replaceAll(total, "About", "");
					total = JUtilString.replaceAll(total, "results", "");
					total = JUtilString.replaceAll(total, ",", "");
					total = JUtilString.replaceAll(total, " ", "");
					System.out.println("total: "+total);
				}
			}

			//System.out.println(resp);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 生成签名
	 * @param userId
	 * @param apiKey
	 * @param format
	 * @param method
	 * @param timestamp
	 * @param version
	 * @param data
	 * @return
	 */
	private static String sign(String userId, String apiKey, String format, String method, String timestamp, String version, String data){
		StringBuilder s = new StringBuilder();
		s.append(apiKey);
		s.append(userId);
		s.append(data);
		s.append(format);
		s.append(method);
		s.append(timestamp);
		s.append(version);
		s.append(apiKey);
		return JUtilMD5.MD5EncodeToHex(s.toString());
	}

	/**
	 *
	 * @param sellerId
	 * @param method
	 * @param data
	 * @return
	 */
	private static String request(String sellerId, String method, String data){
		try{
			String url="https://ejf-fat.yw56.com.cn/api/order";
			String userId="100000";
			String apiKey="D6140AA383FD8515B09028C586493DDB";
			String format="json";
			String timestamp=SysUtil.getNow()+"";
			String version="V1.0";
			String sign=sign(userId, apiKey, format, method, timestamp, version, data);

			url+="?user_id="+userId;
			url+="&method="+method;
			url+="&format="+format;
			url+="&timestamp="+timestamp;
			url+="&sign="+sign;
			url+="&version="+version;

			JHttp http = JHttp.getInstance();
			JHttpContext context = new JHttpContext();
			context.setAllowedErrorCodes(new String[]{"200","302","415"});
			context.setContentType("application/json");
			context.setRequestBody(data);

			return http.postResponse(context, null, url, null, "UTF-8");
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private static  String getDatadigest(String sellerId, String data) {
		data+="31ada638f569948c3799b7ea5266f922";

		byte[] bytes=null;
		try {
			bytes=data.getBytes("UTF-8");
		}catch(Exception e) {}

		return Base64.encodeBase64String(DigestUtils.md5(bytes));
	}

	/**
	 *
	 * @param content
	 * @param charset
	 * @param keys
	 * @return
	 */
	private static String doSign(String content, String charset, String keys) {
		String sign = "";
		content = content + keys;
		try {

			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(content.getBytes(charset));
			sign = new String(Base64.encodeBase64(md.digest()), charset);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return sign;
	}
}