package j.http.proxy;

import j.core.common.Global;
import j.core.nvwa.Nvwa;
import j.http.proxy.provider.ProxyProvider51;
import j.http.proxy.provider.ProxyProviderIPIdea;
import j.http.proxy.provider.ProxyProviderZM;

/**
 * 测试http代理池
 */
public class ProxySample {
    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        Nvwa.startup();

        while(!Nvwa.isScanned()){
            System.out.println("1111...........");
            Global.sleep1000Millis();
        }

        System.out.println("2222...........");
        ProxyPool pool = ProxyPool.getInstance("TEST");
        ProxyProvider51 zm=new ProxyProvider51();
        pool.appendProvider(zm);
        pool.setIncrement(10);
        System.out.println("3333...........");

        ProxyUsage usage=pool.use("TEST",
                5,
                300000,
                -1,
                true,
                false,
                null,
                null,
                null);

        System.out.println("获得代理IP -> "+usage.getProxyIp());
    }
}