package j.core.nio.http;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.nio.SocketOptions;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilBean;
import j.util.JUtilUUID;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/10/01",
        description = "HttpClient支持并发请求，在一般并发量下，对同一服务端采用单例HttpClient来发送请求即可，HttpClientPool通过维护多个HttpClient实例来提供更高的并发能力。" +
                "同时，HttpClientPool监控HttpClient实例，处理超时、过期的传输、数据包，所以，建议采用HttpClientPool的方式使用HttpClient")
@Setter
@Getter
public class HttpClientPool implements Runnable{
    //日志
    private static Logger log= Logger.create(HttpClientPool.class);

    //key: host:port，value: HttpClientPool
    private static ConcurrentMap<String, HttpClientPool> pools=new ConcurrentMap<>();

    //Client实现类
    private String executor="j.core.nio.http.HttpClient";

    //uuid
    private String uuid;

    //host
    private String host;

    //port
    private int port;

    //pool size
    private int poolSize=1;

    //HttpClient
    private ConcurrentList<HttpClient> clients=new ConcurrentList<>();

    //socket options
    private Map<Integer, Object> socketOptions;

    //自定义业务参数
    private Object[] args;

    //轮训选择
    private int selector=0;

    /**
     *
     * @param host
     * @param port
     * @param poolSize
     * @param socketOptions
     * @param args
     * @return
     * @throws Exception
     */
    synchronized public static HttpClientPool getInstance(String host, int port, int poolSize, Map<Integer, Object> socketOptions, Object[] args) throws Exception{
        String key=host+":"+port;

        if(poolSize<=0) poolSize=1;
        if(socketOptions==null) socketOptions=SocketOptions.cloneDefaults();

        HttpClientPool pool = pools.get(key);
        if(pool != null){
            pool.poolSize=poolSize;
            pool.socketOptions=socketOptions;
            return pool;
        }

        pool=new HttpClientPool();
        pool.host=host;
        pool.port=port;
        pool.poolSize=poolSize;
        pool.socketOptions=socketOptions;
        pool.uuid=JUtilUUID.genUUIDShort();

        pools.put(key, pool);

        new Thread(pool).start();

        log.log("Http client pool for "+key+" is started -> "+ JUtilBean.map2Json(socketOptions), -1);

        return pool;
    }

    /**
     *
     * @param host
     * @param port
     * @return
     */
    synchronized public static HttpClientPool getInstance(String host, int port){
        return pools.get(host+":"+port);
    }

    /**
     *
     * @return
     * @throws Exception
     */
    synchronized public HttpClient getClient() throws Exception{
        this.closeDisconnected();

        while(this.clients.size() < this.poolSize){
            log.log(this.host+":"+this.port+",创建HttpClient ..................", -1);
            HttpClient client=(HttpClient)Class.forName(this.executor)
                    .getDeclaredConstructor(String.class, int.class, Map.class, Object[].class)
                    .newInstance(this.host, this.port, this.socketOptions, this.args);
            this.clients.add(client);
            log.log(this.host+":"+this.port+",创建HttpClient ..................OK.", -1);
        }

        int waits=0;
        List<HttpClient> avails=new ArrayList<>();
        while(avails.isEmpty() && waits<30){
            waits++;

            for(int i=0; i<this.clients.size(); i++){
                if(this.clients.get(i).isConnected()) avails.add(this.clients.get(i));
            }
            if(!avails.isEmpty()) break;

            try{
                Thread.sleep(100);
            }catch (Exception e){}
        }

        if(avails.isEmpty()) throw new Exception("no client connected.");

        if(!avails.isEmpty()){
            if(selector>=avails.size()) selector=0;
            HttpClient client=avails.get(selector);
            selector++;
            return client;
        }

        return null;
    }

    /**
     *
     */
    public void shutdown(){
        while(this.clients.size()>0){
            try{
                this.clients.get(0).end(true);
                this.clients.remove(0);
            }catch(Exception ex){}
        }
    }

    /**
     *
     */
    private void closeDisconnected(){
        for(int i=0; i<this.clients.size(); i++){
            if(!this.clients.get(i).isConnected()){
                this.clients.get(i).end(true);
                this.clients.remove(i);
                i--;
            }
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(1000);
            }catch (Exception e){}

            try {

                if(Startup.isDestroyed()) {
                    this.shutdown();
                    return;
                }

                this.closeDisconnected();

                for (int i = 0; i < this.clients.size(); i++) {
                    j.core.nio.http.HttpClient c = this.clients.get(i);
                    c.clearTimeoutDataSegments();
                    c.clearInvalidPackages();
                    c.heartbeat();
                }
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
            }
        }
    }
}