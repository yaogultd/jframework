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
@Properties(path = "httpProxyProvider51.properties")
public class ProxyProvider51 extends ProxyProvider{
    //日志输出
    private static Logger log=Logger.create(ProxyProvider51.class);

    private JHttp http;
    private JHttpContext context;
    private HttpClient client;

    @Override
    public String getProviderId() {
        return "51";
    }

    /**
     *
     * @throws Exception
     */
    public ProxyProvider51() throws Exception{

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
            log.log("add ip to white list use http proxy -> "+this.getParameter("proxyIp"), -1);
            this.client=this.http.createClient(this.getParameter("proxyIp"),
                    Integer.parseInt(this.getParameter("proxyPort")),
                    "http",
                    null,
                    null);
        }else{
            this.client=this.http.createClient(15000);
        }

        //添加代理IP
        if(!JUtilString.isBlank(this.getParameter("proxyIp"))){
            try{
                String resp=this.getWithFixedWait(JUtilString.replaceAll(addWhiteIPUrl, "THE_IP", this.getParameter("proxyIp")));
                log.log("add proxy ip to white list -> "+resp, -1);
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
                throw new Exception("add ip to white list failed!");
            }
        }

        //添加本机IP
        try{
            String resp=this.getWithFixedWait(JUtilString.replaceAll(addWhiteIPUrl, "THE_IP", IPs.getMyIp()));
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
                "msg":"",
                "data":[{
                "IP":"0.0.0.0",
                "Port":8080,
                "ExpireTime":"2018-01-01 08:08:08",
                "IpAddress":"湖南省益阳市 电信","ISP":"电信"
                },
                {
                "IP":"0.0.0.0",
                "Port":8080,
                "ExpireTime":"2018-01-01 08:08:08",
                "IpAddress":"湖南省益阳市 电信",
                "ISP":"电信"
                }]}
             */

            List<JhttpProxy> list=new ArrayList<>();
            for(int i=0; i<data.length(); i++){
                JSONObject d=JUtilJSON.get(data, i);
                String ip=JUtilJSON.string(d, "IP");
                Integer port=JUtilJSON.getInteger(d, "Port");
                if(ip.indexOf(":") > 0){
                    port=Integer.valueOf(ip.substring(ip.indexOf(":") + 1));
                    ip=ip.substring(0, ip.indexOf(":"));
                }
                String expire_time=JUtilJSON.string(d, "ExpireTime");

                JhttpProxy proxy=new JhttpProxy();
                proxy.setProxyIp(ip);
                proxy.setProxyPort(port);
                proxy.setLifeEnd(Timestamp.valueOf(expire_time).getTime());

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
        return 200;//单次最高200
    }
}