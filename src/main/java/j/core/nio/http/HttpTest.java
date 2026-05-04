package j.core.nio.http;

import j.core.common.Global;
import j.core.nio.DataPackage;
import j.core.nio.DataSource;
import j.core.nio.DataSourceString;
import j.core.nio.SocketOptions;
import j.core.nvwa.Nvwa;
import j.core.service.server.Server;

import java.util.Iterator;
import java.util.Map;

public class HttpTest {
    public static void main(String[] args) throws Exception{
//        Nvwa.startup();
//        while(!Nvwa.isScanned()){
//            System.out.println("waiting for startup--------------");
//            try{
//                Thread.sleep(1000);
//            }catch(Exception e){}
//        }
//        try{
//            Thread.sleep(5000);
//        }catch(Exception e){}

        //启动服务端
        Map<Integer, Object> options= SocketOptions.cloneDefaults();
        options.put(java.net.SocketOptions.SO_SNDBUF, 512);
        options.put(java.net.SocketOptions.SO_RCVBUF, 512);
        options.put(SocketOptions.SO_MAX_IDLE, 30000L);
        options.put(SocketOptions.SO_MUST_SEND_AFTER_CONN_WITH_IN, 6000L);
        options.put(SocketOptions.SERVER_RESPOND_POOL_SIZE, 10);

//        j.core.nio.Server server = j.core.nio.Server.start(1990,
//                HttpServer.class,
//                100,
//                10,
//                10,
//                options,
//                null);

        HttpClientPool clientPool = HttpClientPool.getInstance("39.106.140.175", 1990, 2, options, null);

        for(int i=0; i<1000; i++) {
            HttpClient executor = clientPool.getClient();
            long t2 = System.currentTimeMillis();
            DataPackage resp = executor.request("/something.txt", null, null, null, 5000);
            if(resp==null){
                System.out.println("resp is null");
                Global.sleep1000Millis();
                continue;
            }
            DataSourceString d = (DataSourceString) resp.getEntity("/something.txt");
            if (d != null) {
                System.out.println("resp -> " + d.getSourceString());
            }
            long t1 = System.currentTimeMillis();
            System.out.println("cost " + (t2 - t1));

            Global.sleep100Millis();
        }
    }
}
