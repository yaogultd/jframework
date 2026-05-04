package j.core.web.security;

import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.sys.SysUtil;
import j.core.web.handler.UserAgents;
import j.core.web.online.Onlines;
import j.http.HttpUtil;
import j.http.JHttp;
import j.log.Logger;
import j.util.JUtilBean;
import j.util.JUtilJSON;
import j.util.JUtilString;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于cloudflare的Turnstile实现的机器人检测
 */
@Nvwa
@Properties(path = "cloudflare.properties")
public class RobotInspectorCloudflare extends RobotInspector {
    private static Logger log=Logger.create(RobotInspectorCloudflare.class);//日志输出

    @Override
    public boolean pass(HttpServletRequest request){
        try {
            String uaId = Onlines.getUaId(request);
            if(JUtilString.isBlank(uaId)){
                return false;
            }

            String userAgentType = UserAgents.getUserAgentType(request);

            //微信、支付宝生态内不启用
            if(UserAgents.UA_ALIPAY.equals(userAgentType)
                    ||UserAgents.UA_ALIPAY_MINI.equals(userAgentType)
                    ||UserAgents.UA_WECHAT.equals(userAgentType)
                    ||UserAgents.UA_WECHAT_MINI.equals(userAgentType)){
                return true;
            }

            String response = SysUtil.getHttpParameter(request, "cf-turnstile-response");
            if (JUtilString.isBlank(response)){
                log.log("cf-turnstile-response is blank", -1);
                return false;
            }

            String ip = HttpUtil.getRemoteIp(request);

            String secret = this.getParameter("KEY");
            if (JUtilString.isBlank(secret)){
                log.log("secret is blank", -1);
                return false;
            }

            Map<String, String> params = new HashMap();
            params.put("secret", secret);
            params.put("response", response);
            params.put("remoteip", ip);

            JHttp http = JHttp.getInstance();

            String resp = http.postResponse(null, null, this.getParameter("provider"), params, "UTF-8");
            log.log("RobotInspectorCloudflare -> request -> "+ JUtilBean.map2Json(params)+" -> response -> "+resp, -1);
            JSONObject _resp = JUtilJSON.parse(resp);
            Boolean success=JUtilJSON.getBoolean(_resp, "success");
            if(success==null || !success) return false;

            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }
}
