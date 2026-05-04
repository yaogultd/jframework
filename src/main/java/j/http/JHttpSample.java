package j.http;

import j.core.common.Global;
import j.core.nvwa.Nvwa;
import j.http.proxy.ProxyPool;
import j.http.proxy.ProxyUsage;
import j.http.proxy.provider.ProxyProviderZM;
import j.util.ConcurrentMap;
import j.util.JUtilMD5;
import j.util.JUtilSorter;
import j.util.JUtilString;

import java.util.List;

import org.apache.http.client.HttpClient;

/**
 * 
 * @author 肖炯
 *
 */
public class JHttpSample {
	/**
	 * 
	 * @param key
	 * @param parameters
	 * @return
	 */
	public static String createSign(String key,ConcurrentMap parameters){
		List keys=parameters.listKeys();//所有参数名
		
		JUtilString sorter=new JUtilString();
		keys=sorter.bubble(keys, JUtilSorter.ASC);//将参数名按字母排序，如  aac,aad,cac,f2v...
		
		StringBuffer sb = new StringBuffer();

        for(int i=0;i<keys.size();i++){//按参数名的字母顺序拼接字符串
            String k = (String)keys.get(i);
            String v = (String)parameters.get(k);

            if(!"v".equals(k)) {//不包括签名本身和无关参数
                sb.append(k+"="+v+"&");
            }
        }

        sb.append("k="+key);//通信秘钥
        
        System.out.println(sb.toString());

        String sign = JUtilMD5.MD5EncodeToHex(sb.toString());

        return sign;
    }
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)throws Exception{
		Nvwa.startup();

		while(!Nvwa.isScanned()){
			System.out.println("wait for startup...........");
			Global.sleep1000Millis();
		}

		JHttp http=JHttp.getInstance();
		HttpClient client=http.createClient(30000);
		JHttpContext context=new JHttpContext();

		ProxyPool pool = ProxyPool.getInstance("TEST");
		ProxyProviderZM zm=new ProxyProviderZM();
		zm.setSurvivalType(ProxyProviderZM.SURVIVAL_TYPE_5M25M);
		pool.appendProvider(zm);
		pool.setIncrement(2);

		ProxyUsage usage=pool.use("TEST",
				5,
				300000,
				-1,
				true,
				false,
				null,
				null,
				null);
		usage.setMaxIdle(60000);

		http.setProxyOfClient(client, usage);

		while ((true)){
			String resp=http.getResponse(context, client, "https://www.ifeng.com/", "UTF-8");
			if(resp.length()>15) resp=resp.substring(0, 15);
			System.out.println("resp -> " + resp);
			try{
				Thread.sleep(1000);
			}catch (Exception ex){}
		}
	}
}