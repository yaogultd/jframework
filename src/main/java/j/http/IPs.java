package j.http;

import j.util.JUtilJSON;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IPs{
    /**
     * 本机公网IP
     * @return
     */
    public static String getMyIp(){
        String ip=null;
        try{
            ip=getIpViaIpCn();
        }catch(Exception e){
            ip=null;
        }

        if(ip==null){
            try{
                ip=getIpViaIp138Com();
            }catch(Exception e){
                ip=null;
            }
        }

        return ip;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public static String getIpViaIpCn() throws Exception{
        String url="https://www.ip.cn/api/index?ip=&type=0";
        JHttp http=JHttp.getInstance();
        String resp=http.getResponse(null, null, url, "UTF-8");
        JSONObject _resp= JUtilJSON.parse(resp);
        String ip=JUtilJSON.string(_resp, "ip");
        return ip;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public static String getIpViaIp138Com() throws Exception{
        String url="https://2023.ip138.com/";
        JHttp http=JHttp.getInstance();
        String resp=http.getResponse(null, null, url, "UTF-8");
        int start=resp.indexOf("您的IP地址是：");
        if(start<0) throw new Exception("no_result_from_ip138");

        start+="您的IP地址是：".length();
        int end=resp.indexOf("<", start);
        String ip=resp.substring(start, end);
        return ip;
    }

    public static void main(String[] args) throws Exception{
        List<String> list=new ArrayList<>();
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-m3tfgvfj_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-5l3nqf28_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-8hlf6as7_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-vmu99s0u_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-csqtsgac_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-qi9vw9gf_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-kt7dfaqj_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-1q2yrx2u_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-whnf7er1_lifetime-1h_streaming-1");
        list.add("geo.iproyal.com:12321:qingying:qingying2021_country-cn_city-foshan_session-1h0fny3t_lifetime-1h_streaming-1");

        JHttp http=JHttp.getInstance();
        for(int i=0; i<list.size(); i++){
            String[] cells=list.get(i).split(":");
            HttpClient client=http.createClient(15000,1, cells[0], Integer.parseInt(cells[1]), "http", cells[2], cells[3]);

            String url="https://www.ip.cn/api/index?ip=&type=0";
            try {
                String resp = http.getResponse(null, client, url, "UTF-8");
                JSONObject _resp = JUtilJSON.parse(resp);
                System.out.println(_resp);
            }catch (Exception e){}
        }
    }
}
