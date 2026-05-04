package j.http.handler;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.annotation.auth.Authority;
import j.core.common.JProperties;
import j.core.fs.JDFSFile;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.core.web.handler.JHandler;
import j.core.web.handler.JResponse;
import j.core.web.handler.JSession;
import j.http.*;
import j.log.Logger;
import j.util.JUtilString;
import j.util.JUtilUUID;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.client.HttpClient;

import java.io.File;
import java.util.*;

@Handler(path = "/framework/api/http/proxy")
public class ProxyHandler extends JHandler {
    private static Logger log=Logger.create(ProxyHandler.class);
    public static final String PROXY_TO_HOST="PROXY_TO_HOST";
    public static final String PROXY_TO_PORT="PROXY_TO_PORT";
    public static final String PROXY_TO_URI="PROXY_TO_URI";
    public static final String PROXY_TO_URL="PROXY_TO_URL";
    public static final String PROXY_TO_METHOD="PROXY_TO_METHOD";
    public static final String PROXY_TO_USE_ORIGINIONAL_CLIENT_IP="PROXY_TO_UOC";
    public static final String PROXY_TO_MACHINE_IP="PROXY_TO_MIP";
    public static final String PROXY_TO_IGNORED_HEADERS="PROXY_TO_IHS";
    public static final String PROXY_TO_REQUEST_ENCODEING="PROXY_TO_REQENC";
    public static final String PROXY_TO_RESPONSE_ENCODEING="PROXY_TO_RESENC";
    public static final String PROXY_GET_STREAM="PROXY_GET_STREAM";
    public static final String[] allowedFileTypes = new String[]{"jpg","jpeg","png","mp3","mp4","mov","acc"};

    /**
     * http请求转发
     * @param jsession
     * @param request
     * @param response
     * @throws Exception
     */
    @Action()
    @Authority(policy="signature")
    public void request(JSession jsession, HttpServletRequest request, HttpServletResponse response) throws Exception{
        try {
            String proxyToUrl = jsession.getParameter(PROXY_TO_URL);
            String proxyToMethod = jsession.getParameter(PROXY_TO_METHOD, "GET").toUpperCase();
            String ignoredHeaders = jsession.getParameter(PROXY_TO_IGNORED_HEADERS, "").toLowerCase();
            String proxyToRequestEncoding = jsession.getParameter(PROXY_TO_REQUEST_ENCODEING);
            String proxyToResponseEncoding = jsession.getParameter(PROXY_TO_RESPONSE_ENCODEING);
            String proxyGetStream = jsession.getParameter(PROXY_GET_STREAM);

            if(JUtilString.isBlank(proxyToUrl)
                    || (!"GET".equals(proxyToMethod) && !"POST".equals(proxyToMethod))) {
                throw new Exception("invalid request");
            }

            List<String> _ignoredHeaders = new ArrayList<>();
            _ignoredHeaders.add("content-length");
            _ignoredHeaders.add("x-forwarded-for");
            _ignoredHeaders.add("x-real-ip");
            _ignoredHeaders.add("x-real-port");
            _ignoredHeaders.add("proxy_forwarded_for");
            _ignoredHeaders.add("true-client-ip");
            _ignoredHeaders.add("remote-host");
            _ignoredHeaders.add("remote-addr");
            _ignoredHeaders.add("cookie");
            _ignoredHeaders.add("host");
            _ignoredHeaders.add("connection");
            _ignoredHeaders.add("aws-waf-token");

            _ignoredHeaders.add(Constants.ACCESS_KEY.toLowerCase());
            _ignoredHeaders.add(Constants.SIGNATURE.toLowerCase());

            String[] _arr=ignoredHeaders.split(",");
            for(String h : _arr){
                if(!JUtilString.isBlank(h)) _ignoredHeaders.add(h);
            }

            JHttp http = JHttp.getInstance();
            HttpClient client = http.createClient(300000);
            JHttpContext context = new JHttpContext();
            context.setAllowedErrorCodes(new String[]{"200", "302", "400", "202"});

            Enumeration hns=request.getHeaderNames();
            while(hns.hasMoreElements()){
                String n=hns.nextElement().toString();

                if("aws-waf-token".equals(n.toLowerCase())){
                    context.addCookie(n, request.getHeader(n), 0, JUtilString.getHost(proxyToUrl), "/");
                    log.log("add request cookie(from http header aws-waf-token) => "+n+" = "+request.getHeader(n), -1);
                }

                //忽略的头部
                if(_ignoredHeaders.contains(n.toLowerCase())) continue;

                context.addRequestHeader(n, request.getHeader(n));
                log.log("add request header => "+n+" = "+request.getHeader(n), -1);
            }

            /*try{
                Cookie[] cookies = request.getCookies();
                for(Cookie c : cookies){
                    context.addCookie(c.getName(), c.getValue(), 0, c.getDomain(), c.getPath());
                    log.log("add request cookie => "+c.getName()+" = "+c.getValue(), -1);
                }
            }catch (Exception e){}*/

            List<String> paramsInQuery=new ArrayList<>();
            String query = request.getQueryString();
            if(!JUtilString.isBlank(query)){
                String paras[] = query.split("&");
                for (int i = 0; i < paras.length; i++) {
                    if (paras[i].indexOf("=") < 0) continue;

                    String name = paras[i].substring(0, paras[i].indexOf("="));
                    paramsInQuery.add(name);
                }
            }

            Map<String, String> params = SysUtil.getHttpParameterMap(request);
            for(String n : paramsInQuery) params.remove(n);//移除跟在url后的参数
            params.remove(PROXY_TO_HOST);
            params.remove(PROXY_TO_PORT);
            params.remove(PROXY_TO_URI);
            params.remove(PROXY_TO_URL);
            params.remove(PROXY_TO_METHOD);
            params.remove(PROXY_TO_USE_ORIGINIONAL_CLIENT_IP);
            params.remove(PROXY_TO_MACHINE_IP);
            params.remove(PROXY_TO_IGNORED_HEADERS);
            params.remove(PROXY_TO_REQUEST_ENCODEING);
            params.remove(PROXY_TO_RESPONSE_ENCODEING);
            params.remove(PROXY_GET_STREAM);

            log.log("proxy to ==> "+proxyToUrl, -1);

            String resp="";
            if(!JUtilString.isBlank(proxyToRequestEncoding)) context.setRequestEncoding(proxyToRequestEncoding);
            if(!JUtilString.isBlank(jsession.getRequestBody())){
                log.log("proxy to body => "+jsession.getRequestBody(), -1);
                context.setRequestBody(jsession.getRequestBody());
            }

            //multipart
            String contentType=request.getContentType();
            if(!JUtilString.isBlank(contentType) && contentType.indexOf("boundary")>-1){
                //临时文件保存轮径
                String saveTo = JUtilString.appendPath(JProperties.getJDFSPath(), "http/proxy/"+ JUtilUUID.genUUID());

                //保存上传文件的临时目录
                Upload uploader=new Upload(request,
                        saveTo,
                        "UTF-8",
                        1024*200,//不超过200M
                        false);

                UploadMsg upMsg=uploader.save();//保存附件
                if(!upMsg.isSuccessful){
                    jsession.jresponse=new JResponse(false,"upload_failed",upMsg.result == UploadMsg.RESULT_TOO_LARGE ? "附件太大" : "附件上传失败");
                    return;
                }

                //全部附件
                List<UploadedFile> files=uploader.getUploadedFiles();

                //检查文件有效性
                boolean fileValid=true;
                for(int i=0; i<files.size(); i++){
                    UploadedFile upFile=files.get(i);
                    String ext=upFile.getFileExt_Uploading();

                    if(!JUtilString.containIgnoreCase(allowedFileTypes,ext)){
                        log.log("无效文件 => "+upFile.getFileName_Uploading(), -1);
                        fileValid=false;
                        break;
                    }else{
                        File file=new File(upFile.getAbsoluteFileName_Saved());//保存的上传文件对象
                        if(file.length()==0){// 空文件
                            fileValid=false;
                            break;
                        }
                    }
                }

                //文件无效，删除临时文件
                if(!fileValid){
                    for(int i=0; i<files.size(); i++){
                        UploadedFile upFile=files.get(i);
                        File file=new File(upFile.getAbsoluteFileName_Saved());//文件对象
                        if(file.exists()) file.delete();
                    }

                    jsession.jresponse=new JResponse(false,"invalid_file","文件无效");
                    return;
                }

                //构建转发请求数据
                Map<String, File> multiparts = new HashMap<>();
                for(int i=0; i<files.size(); i++){
                    UploadedFile upFile=files.get(i);
                    File file=new File(upFile.getAbsoluteFileName_Saved());//文件对象
                    multiparts.put(upFile.getParameterName(), file);
                }

                context.setContentType(null);
                if(!JUtilString.isBlank(proxyToResponseEncoding)) http.postMultipartData(context, client, proxyToUrl, multiparts, uploader.getOtherParameters(), proxyToResponseEncoding);
                else http.postMultipartData(context, client, proxyToUrl, multiparts, uploader.getOtherParameters());

                response.setStatus(context.getStatus());
                jsession.resultString=context.getResponseText();
                return;
            }

            //返回响应流
            if("true".equalsIgnoreCase(proxyGetStream)){
                if("GET".equalsIgnoreCase(proxyToMethod)){
                    context=http.getStream(context, client, proxyToUrl);
                }else{
                    context=http.postStream(context, client, proxyToUrl, params);
                }

                String saveTo = JUtilString.appendPath(JProperties.getJDFSPath(), "http/proxy/"+ JUtilUUID.genUUID()+".temp");
                JDFSFile.save(context.getResponseStream(), saveTo);
                log.log("stream save to => "+saveTo, -1);

                //下载
                response.setStatus(context.getStatus());
                Download download = new Download(response, "application/octet-stream");
                download.downloadFile(saveTo, "noname");

                //删除临时文件
                File saveToFile = new File(saveTo);
                saveToFile.delete();
                return;
            }

            //返回响应文本
            if("GET".equalsIgnoreCase(proxyToMethod)){
                if(!JUtilString.isBlank(proxyToResponseEncoding)) resp=http.getResponse(context, client, proxyToUrl, proxyToResponseEncoding);
                else resp=http.getResponse(context, client, proxyToUrl);
            }else{
                if(!JUtilString.isBlank(proxyToResponseEncoding)) resp=http.postResponse(context, client, proxyToUrl, params, proxyToResponseEncoding);
                else resp=http.postResponse(context, client, proxyToUrl, params);
            }

            response.setStatus(context.getStatus());
            jsession.resultString=resp;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            response.sendError(500);
        }
    }
}