package j.http.proxy.provider;

import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.db.JhttpProxy;
import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.http.IPs;
import j.http.JHttp;
import j.http.JHttpContext;
import j.log.Logger;
import j.util.JUtilBean;
import j.util.JUtilJSON;
import j.util.JUtilString;
import j.util.JUtilTimestamp;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 芝麻HTTP（www.zmhttp.com）
 */
@Setter
@Getter
@Nvwa
@Properties(path = "httpProxyProviderIPIdea.properties")
public class ProxyProviderIPIdea extends ProxyProvider{
    //日志输出
    private static Logger log=Logger.create(ProxyProviderIPIdea.class);

    private JHttp http;
    private JHttpContext context;
    private HttpClient client;
    private long survival=JUtilTimestamp.millisOfDay;

    @Override
    public String getProviderId() {
        return "IPIdea";
    }

    /**
     *
     * @throws Exception
     */
    public ProxyProviderIPIdea() throws Exception{

    }

    /**
     *
     * @throws Exception
     */
    private void init() throws Exception{
        this.http=JHttp.getInstance();
        this.context=new JHttpContext();
        String addWhiteIPUrl=this.getParameter("addWhiteIPUrl");
        if(!JUtilString.isBlank(this.getParameter("proxyIp"))){
            this.client=this.http.createClient(15000,
                    1,
                    this.getParameter("proxyIp"),
                    Integer.parseInt(this.getParameter("proxyPort")),
                    "http",
                    null,
                    null);
        }else{
            this.client=this.http.createClient(15000);
        }

        Map<String, String> params=new HashMap<>();

        //添加代理IP
        if(!JUtilString.isBlank(this.getParameter("proxyIp"))){
            params.put("white_ips", this.getParameter("proxyIp"));
            try{
                String resp=this.postMultipartWithFixedWait(addWhiteIPUrl, params);
                log.log("add proxy ip to white list -> "+resp, -1);
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
                throw new Exception("add proxy ip to white list failed!");
            }
        }

        //添加本机IP
        try{
            params.put("white_ips", IPs.getMyIp());
            String resp=this.postMultipartWithFixedWait(addWhiteIPUrl, params);
            log.log("add ip to white list -> "+resp, -1);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            throw new Exception("add ip to white list failed!");
        }
    }

    /**
     * 不允许频繁访问，故每次访问休息5秒
     * @param url
     * @return
     * @throws Exception
     */
    synchronized private String getWithFixedWait(String url) throws Exception{
        try{
            Thread.sleep(5000);
        }catch (Exception e){}
        return this.http.getResponse(this.context, this.client, url, "UTF-8");
    }

    /**
     * 不允许频繁访问，故每次访问休息5秒
     * @param url
     * @return
     * @throws Exception
     */
    synchronized private String postMultipartWithFixedWait(String url, Map<String, String> strings) throws Exception{
        try{
            Thread.sleep(5000);
        }catch (Exception e){}
        strings.put("appkey", this.getParameter("appkey"));
        strings.put("uid", this.getParameter("uid"));
        this.http.postMultipartData(this.context, this.client, url, null, strings,"UTF-8");
        return this.context.getResponseText();
    }

    @Override
    public List<JhttpProxy> get(int amount,
                                int minSurvival,
                                int maxSurvival,
                                boolean httpsSupported,
                                boolean repeatable,
                                String continentId,
                                String countryId,
                                String provinceId) {
        try {
            if(this.http==null) this.init();

            String url = this.getParameter("url");
            url = JUtilString.replaceAll(url, "NUM", (amount + ""));
            url = JUtilString.replaceAll(url, "REGIONS", "");//暂时不支持指定地区

            String resp = this.getWithFixedWait(url);
            log.log("get ips from url -> " + url + " -> "+resp, -1);
            JSONArray data=JUtilJSON.array(JUtilJSON.parse(resp), "data");
            if(data==null) return null;

            /* {
                    "code":0,
                    "success":true,
                    "msg":"0",
                    "data":[
                                  {"ip":"47.244.192.12","port":16098},
                                  {"ip":"47.244.192.12","port":15698}
                              ]
                  }
             */

            List<JhttpProxy> list=new ArrayList<>();
            for(int i=0; i<data.length(); i++){
                JSONObject d=JUtilJSON.get(data, i);
                String ip=JUtilJSON.string(d, "ip");
                Integer port=JUtilJSON.getInteger(d, "port");

                JhttpProxy proxy=new JhttpProxy();
                proxy.setProxyIp(ip);
                proxy.setProxyPort(port);
                proxy.setLifeEnd(SysUtil.getNow() + this.survival);

                list.add(proxy);
            }
            return list;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return null;
    }

    @Override
    public int available() {
        return 900;//单次最高900
    }
}