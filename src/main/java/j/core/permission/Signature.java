package j.core.permission;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.security.Hmac;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.http.JHttp;
import j.http.JHttpContext;
import j.util.JUtilBase64;
import j.util.JUtilBean;
import j.util.JUtilJSON;
import j.util.JUtilString;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021-08-02",
        description = "实现安全机制的帮助类（框架暂未实现统一控制策略，暂时使用这个来生成/验证签名、加减密）")
public final class Signature {
    public final static String SIG_REQUEST_BODY="SIG_REQUEST_BODY";

    @MethodDescription(author = "肖炯",
            date = "2021-09-15",
            description = "对参数按照规则生成签名")
    public static String sign(Map<String, String> params, String accessSecret) throws Exception{
        return sign(params, null, accessSecret);
    }

    @MethodDescription(author = "肖炯",
            date = "2021-09-15",
            description = "对参数按照规则生成签名")
    public static String sign(Map<String, String> params, String requestBody, String accessSecret) throws Exception{
        if(params==null) params=new HashMap<>();

        //去掉系统保留参数
        Map<String, String> _params=paraFilter(params);
        if(_params.isEmpty() && !JUtilString.isBlank(requestBody)){//没有参数，只有requestBody
            //System.out.println("没有参数，只有requestBody => "+requestBody);
            return sign(requestBody, accessSecret);
        }

        if(!JUtilString.isBlank(requestBody)){
            //System.out.println("将requestBody作为一个参数 => "+requestBody);
            _params.put(SIG_REQUEST_BODY, requestBody);//将requestBody作为一个参数
        }

        //拼接字符串（规则同阿里云 AccessKeyId + AcessKeySecret 策略）
        String queries = Hmac.composeStringToSign(_params);

        return sign(queries, accessSecret);
    }

    @MethodDescription(author = "肖炯",
            date = "2021-09-15",
            description = "给定内容直接生成签名")
    public static String sign(String content, String accessSecret) throws Exception{
        //生成签名
        String sign= JUtilBase64.encode(Hmac.encryptHmac(Hmac.ALGORITHM_HMACSHA256,
                content.getBytes(Hmac.CHARSET_UTF8),
                accessSecret.getBytes(Hmac.CHARSET_UTF8)));

        return sign;
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign）")
    public static JHttpContext request(String url, Map<String, String> params, String accessKey, String accessSecret) throws Exception{
        return request(null, null, null, url, params, null, accessKey, accessSecret);
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign）")
    public static JHttpContext request(String url, String requestBody, String accessKey, String accessSecret) throws Exception{
        return request(null, null, null, url, new HashMap<>(), requestBody, accessKey, accessSecret);
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign）")
    public static JHttpContext request(JHttp http, HttpClient hc, JHttpContext context, String url, Map<String, String> params, String requestBody, String accessKey, String accessSecret) throws Exception{
        if(context==null) context=new JHttpContext();
        try{
            //去掉系统保留参数
            Map<String, String> _params=paraFilter(params);

            //url上的参数
            if(url.indexOf("?")>0){
                String queryString = url.substring(url.indexOf("?")+1);
                if(!JUtilString.isBlank(queryString)){
                    String paras[] = queryString.split("&");
                    for (int i = 0; i < paras.length; i++) {
                        if (paras[i].indexOf("=") < 0) continue;

                        String name = paras[i].substring(0, paras[i].indexOf("="));
                        String value = paras[i].substring(paras[i].indexOf("=") + 1);
                        value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
                        //System.out.println("param on url "+name+" = "+value);
                        _params.put(name, value);
                    }
                }
            }

            //生成签名
            String sign=sign(_params, requestBody, accessSecret);

            if(http==null) http=JHttp.getInstance();

            context.addRequestHeader(Constants.ACCESS_KEY, accessKey);
            context.addRequestHeader(Constants.SIGNATURE, sign);

            if(!JUtilString.isBlank(requestBody)) context.setRequestBody(requestBody);
            if(_params!=null) _params.remove(SIG_REQUEST_BODY);
            http.postResponse(context, hc, url, _params, SysConfig.sysEncoding);
        }catch(Exception e){
            context.setStatus(500);
            throw e;
        }

        return context;
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign），使用简单签名方式")
    public static JHttpContext request(JHttp http, HttpClient hc, JHttpContext context, String url, String requestBody, String accessKey, String accessSecret) throws Exception{
        if(context==null) context=new JHttpContext();
        try{
            Map<String, String> _params=new HashMap<>();

            //url上的参数
            if(url.indexOf("?")>0){
                String queryString = url.substring(url.indexOf("?")+1);
                if(!JUtilString.isBlank(queryString)){
                    String paras[] = queryString.split("&");
                    for (int i = 0; i < paras.length; i++) {
                        if (paras[i].indexOf("=") < 0) continue;

                        String name = paras[i].substring(0, paras[i].indexOf("="));
                        String value = paras[i].substring(paras[i].indexOf("=") + 1);
                        value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
                        _params.put(name, value);
                    }
                }
            }

            //生成签名
            String sign=sign(_params, requestBody, accessSecret);

            if(http==null) http=JHttp.getInstance();

            context.addRequestHeader(Constants.ACCESS_KEY, accessKey);
            context.addRequestHeader(Constants.SIGNATURE, sign);

            context.setRequestBody(requestBody);
            http.postResponse(context, hc, url, null, SysConfig.sysEncoding);
        }catch(Exception e){
            context.setStatus(500);
            throw e;
        }

        return context;
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign）")
    public static JHttpContext requestStream(String url, Map<String, String> params, String accessKey, String accessSecret) throws Exception{
        return requestStream(null, null, null, url, params, null, accessKey, accessSecret);
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign）")
    public static JHttpContext requestStream(String url, String requestBody, String accessKey, String accessSecret) throws Exception{
        return requestStream(null, null, null, url, new HashMap<>(), requestBody, accessKey, accessSecret);
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign）")
    public static JHttpContext requestStream(JHttp http, HttpClient hc, JHttpContext context, String url, Map<String, String> params, String requestBody, String accessKey, String accessSecret) throws Exception{
        if(context==null) context=new JHttpContext();
        try{
            //去掉系统保留参数
            Map<String, String> _params=paraFilter(params);

            //url上的参数
            if(url.indexOf("?")>0){
                String queryString = url.substring(url.indexOf("?")+1);
                if(!JUtilString.isBlank(queryString)){
                    String paras[] = queryString.split("&");
                    for (int i = 0; i < paras.length; i++) {
                        if (paras[i].indexOf("=") < 0) continue;

                        String name = paras[i].substring(0, paras[i].indexOf("="));
                        String value = paras[i].substring(paras[i].indexOf("=") + 1);
                        value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
                        //System.out.println("param on url "+name+" = "+value);
                        _params.put(name, value);
                    }
                }
            }

            //生成签名
            String sign=sign(_params, requestBody, accessSecret);

            if(http==null) http=JHttp.getInstance();

            context.addRequestHeader(Constants.ACCESS_KEY, accessKey);
            context.addRequestHeader(Constants.SIGNATURE, sign);

            if(!JUtilString.isBlank(requestBody)) context.setRequestBody(requestBody);
            if(_params!=null) _params.remove(SIG_REQUEST_BODY);
            http.postStream(context, hc, url, _params);
        }catch(Exception e){
            context.setStatus(500);
            throw e;
        }

        return context;
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign），使用简单签名方式")
    public static JHttpContext requestStream(JHttp http, HttpClient hc, JHttpContext context, String url, String requestBody, String accessKey, String accessSecret) throws Exception{
        if(context==null) context=new JHttpContext();
        try{
            Map<String, String> _params=new HashMap<>();

            //url上的参数
            if(url.indexOf("?")>0){
                String queryString = url.substring(url.indexOf("?")+1);
                if(!JUtilString.isBlank(queryString)){
                    String paras[] = queryString.split("&");
                    for (int i = 0; i < paras.length; i++) {
                        if (paras[i].indexOf("=") < 0) continue;

                        String name = paras[i].substring(0, paras[i].indexOf("="));
                        String value = paras[i].substring(paras[i].indexOf("=") + 1);
                        value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
                        _params.put(name, value);
                    }
                }
            }

            //生成签名
            String sign=sign(_params, requestBody, accessSecret);

            if(http==null) http=JHttp.getInstance();

            context.addRequestHeader(Constants.ACCESS_KEY, accessKey);
            context.addRequestHeader(Constants.SIGNATURE, sign);

            context.setRequestBody(requestBody);
            http.postStream(context, hc, url, null);
        }catch(Exception e){
            context.setStatus(500);
            throw e;
        }

        return context;
    }

    @MethodDescription(author = "肖炯",
            date = "2021-08-02",
            description = "对参数按照规则签名，在请求头中添加accessKey和签名（sign），使用简单签名方式")
    public static JHttpContext requestMultipart(JHttp http, HttpClient hc, JHttpContext context, String url, Map parts, Map strings, String encoding, String accessKey, String accessSecret) throws Exception{
        if(context==null) context=new JHttpContext();
        try{
            Map<String, String> _params=new HashMap<>();

            //url上的参数
            if(url.indexOf("?")>0){
                String queryString = url.substring(url.indexOf("?")+1);
                if(!JUtilString.isBlank(queryString)){
                    String paras[] = queryString.split("&");
                    for (int i = 0; i < paras.length; i++) {
                        if (paras[i].indexOf("=") < 0) continue;

                        String name = paras[i].substring(0, paras[i].indexOf("="));
                        String value = paras[i].substring(paras[i].indexOf("=") + 1);
                        value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
                        _params.put(name, value);
                    }
                }
            }

            //生成签名
            String sign=sign(_params, accessSecret);

            if(http==null) http=JHttp.getInstance();

            context.addRequestHeader(Constants.ACCESS_KEY, accessKey);
            context.addRequestHeader(Constants.SIGNATURE, sign);

            http.postMultipartData(context, hc, url, parts, strings, encoding);
        }catch(Exception e){
            context.setStatus(500);
            throw e;
        }

        return context;
    }

    /**
     * 判断签名是否正确
     * @param request
     * @return
     * @throws Exception
     */
    public static boolean verify(HttpServletRequest request, String accessSecret) throws Exception{
        Map<String, String> _params=SysUtil.getHttpParameterMap(request);

        //生成签名
        String sign=sign(_params, accessSecret);

        String signOriginal=request.getHeader(Constants.SIGNATURE);

        return sign.equalsIgnoreCase(signOriginal);
    }

    /**
     * 判断签名是否正确
     * @param request
     * @param requestBody
     * @param accessSecret
     * @return
     * @throws Exception
     */
    public static boolean verify(HttpServletRequest request, String requestBody, String accessSecret) throws Exception{
        Map<String, String> _params=SysUtil.getHttpParameterMap(request);

        //签名类型
        String signType=request.getHeader(Constants.SIGNATURE_TYPE);

        //生成签名
        String sign=null;
        if(Constants.SIGNATURE_TYPE_BODY.equals(signType)){
            sign=sign(requestBody, accessSecret);
        }else{
            if(!JUtilString.isBlank(requestBody)) _params.put(SIG_REQUEST_BODY, requestBody);
            sign=sign(_params, accessSecret);
        }

        String signOriginal=request.getHeader(Constants.SIGNATURE);

        return sign.equalsIgnoreCase(signOriginal);
    }

    /**
     * 判断签名是否正确
     * @param json
     * @param accessSecret
     * @return
     * @throws Exception
     */
    public static boolean verify(JSONObject json, String accessSecret) throws Exception{
        Map<String, String> _params=JUtilBean.jsonPlain2Map(json);

        //生成签名
        String sign=sign(_params, accessSecret);

        String signOriginal=JUtilJSON.string(json, Constants.SIGNATURE);

        return sign.equalsIgnoreCase(signOriginal);
    }

    /**
     * 判断签名是否正确
     * @param requestBody
     * @param signOriginal
     * @param accessSecret
     * @return
     * @throws Exception
     */
    public static boolean verifyString(String requestBody, String signOriginal, String accessSecret ) throws Exception{
        //生成签名
        String sign=sign(requestBody, accessSecret);
        return sign.equalsIgnoreCase(signOriginal);
    }

    /**
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    private static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap<String, String>();

        if(sArray == null || sArray.isEmpty()) return result;

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (key.equalsIgnoreCase(Constants.SIGNATURE)
                    || key.equalsIgnoreCase(Constants.ACCESS_KEY)
                    || key.equalsIgnoreCase(Constants.ACCESS_SECRET)
                    || key.equalsIgnoreCase(Constants.AES_KEY)
                    || key.equalsIgnoreCase(Constants.AES_OFFSET)
                    || key.equalsIgnoreCase(Constants.J_REQUEST_UUID)) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    public static void main(String[] args) throws Exception{
        Map<String, String> params=new HashMap<>();
        params.put("u_id", "872963832");

        String url = "https://dd.cc.com/framework/api/http/proxy/request?PROXY_TO_URL=https%3A%2F%2Fapi.openai.com%2Fv1%2Fchat%2Fcompletions&PROXY_TO_METHOD=POST&PROXY_TO_REQENC=UTF-8&PROXY_TO_RESENC=UTF-8";

        if(url.indexOf("?")>0){
            String queryString = url.substring(url.indexOf("?")+1);
            if(!JUtilString.isBlank(queryString)){
                String paras[] = queryString.split("&");
                for (int i = 0; i < paras.length; i++) {
                    if (paras[i].indexOf("=") < 0) continue;

                    String name = paras[i].substring(0, paras[i].indexOf("="));
                    String value = paras[i].substring(paras[i].indexOf("=") + 1);
                    value = JUtilString.decodeURI(value, SysConfig.sysEncoding);
                    params.put(name, value);
                }
            }
        }

        //去掉系统保留参数
        String requestBody="{}";

        //生成签名
        String sign=Signature.sign(params, requestBody, "nLgs3jwA5vJLFCbhARPjbdrvT5vTcdv7");


        System.out.println("sign -> "+sign);
    }
}
