package j.http.proxy.provider;

import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.db.JhttpProxy;
import j.core.nvwa.NvwaAncestor;
import j.http.IPs;
import j.http.JHttp;
import j.http.JHttpContext;
import j.log.Logger;
import j.util.JUtilJSON;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 芝麻HTTP（www.zmhttp.com）
 */
@Setter
@Getter
@Nvwa
@Properties(path = "httpProxyProviderZM.properties")
public class ProxyProviderZM extends ProxyProvider{
    //日志输出
    private static Logger log=Logger.create(ProxyProviderZM.class);

    //有效时长类别
    public static final int SURVIVAL_TYPE_5M25M=1;
    public static final int SURVIVAL_TYPE_25M3H=2;
    public static final int SURVIVAL_TYPE_3H6H=3;

    private JHttp http;
    private JHttpContext context;
    private HttpClient client;
    private int survivalType=1;

    @Override
    public String getProviderId() {
        return "zm";
    }

    /**
     *
     * @throws Exception
     */
    public ProxyProviderZM() throws Exception{

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
                String resp=this.getWithFixedWait(JUtilString.replaceAll(addWhiteIPUrl, "IP", this.getParameter("proxyIp")));
                log.log("add proxy ip to white list -> "+resp, -1);
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
                throw new Exception("add ip to white list failed!");
            }
        }

        //添加本机IP
        try{
            String resp=this.getWithFixedWait(JUtilString.replaceAll(addWhiteIPUrl, "IP", IPs.getMyIp()));
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

            String url = "";
            if (this.survivalType == SURVIVAL_TYPE_3H6H) {
                if (httpsSupported) url = this.getParameter("survival3h6hUrlHttps");
                else url = this.getParameter("survival3h6hUrl");
                url = JUtilString.replaceAll(url, "PACK", this.getParameter("servival3h6hPackId"));
            } else {
                if (httpsSupported) url = this.getParameter("urlHttps");
                else url = this.getParameter("url");
                url = JUtilString.replaceAll(url, "PACK", this.getParameter("packId"));
            }
            url = JUtilString.replaceAll(url, "TIME", (this.survivalType + ""));
            url = JUtilString.replaceAll(url, "NUM", (amount + ""));
            if (provinceId == null) provinceId = "";
            url = JUtilString.replaceAll(url, "REGIONS", provinceId);

            String resp = this.getWithFixedWait(url);
            log.log("get ips from url -> " + url + " -> "+resp, -1);
            JSONArray data=JUtilJSON.array(JUtilJSON.parse(resp), "data");
            if(data==null) return null;

            /**
             * 		"ip": "115.208.85.87",
             * 		"port": 4226,
             * 		"expire_time": "2023-05-14 20:43:13",
             * 		"city": "浙江省湖州市"
             */

            List<JhttpProxy> list=new ArrayList<>();
            for(int i=0; i<data.length(); i++){
                JSONObject d=JUtilJSON.get(data, i);
                String ip=JUtilJSON.string(d, "ip");
                Integer port=JUtilJSON.getInteger(d, "port");
                String expire_time=JUtilJSON.string(d, "expire_time");

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
        try {
            String url=null;
            String resp=null;
            if(this.survivalType == SURVIVAL_TYPE_3H6H) {//资源包
                //{"code":0,"success":true,"msg":"ok","data":{"package_balance":80}}
                url = this.getParameter("survival3h6hBalanceUrl");
                url = JUtilString.replaceAll(url, "PACK", this.getParameter("servival3h6hPackId"));
            }else{
                //{"code":0,"success":true,"msg":"success","data":{"balance":50}}
                url = this.getParameter("getBalanceUrl");
            }
            resp = this.getWithFixedWait(url);
            log.log("balance -> "+resp, -1);

            JSONObject _resp= JUtilJSON.parse(resp);
            JSONObject data=JUtilJSON.object(_resp, "data");
            if(data==null) return 0;

            Integer package_balance=JUtilJSON.getInteger(data, "package_balance");//资源包
            if(package_balance!=null) return package_balance==null ? 0 : package_balance.intValue();

            Integer balance=JUtilJSON.getInteger(data, "balance");//账户余额

            double price=1;//每个IP的价格
            if(this.survivalType==SURVIVAL_TYPE_5M25M) price=Double.parseDouble(this.getParameter("price05m25m"));
            else if(this.survivalType==SURVIVAL_TYPE_25M3H) price=Double.parseDouble(this.getParameter("price25m3h"));
            else price=Double.parseDouble(this.getParameter("price3h6h"));

            //余额除以价格=可用IP数
            return (int)Math.round((balance==null ? 0 : balance.intValue()) / price);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return -1;
    }
}