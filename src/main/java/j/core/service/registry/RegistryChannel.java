package j.core.service.registry;

import j.core.network.Channel;
import j.core.nio.SocketOptions;
import j.core.nio.http.HttpClient;
import j.core.nio.http.HttpClientPool;
import j.log.Logger;

import java.util.Map;

public class RegistryChannel extends Channel {
    //日志
    private static Logger log= Logger.create(RegistryChannel.class);

    /**
     * @param host
     * @param port
     */
    public RegistryChannel(String host, int port) {
        super(host, port);
    }

    /**
     *
     * @return
     */
    public HttpClient getEndpoint(){
        Map<Integer, Object> options = SocketOptions.cloneDefaults();
        options.put(java.net.SocketOptions.SO_SNDBUF, Registry.getTcpSendBufferSize());
        options.put(java.net.SocketOptions.SO_RCVBUF, Registry.getTcpReceiveBufferSize());
        options.put(SocketOptions.SO_MAX_IDLE, 60000L);
        options.put(SocketOptions.SO_MUST_SEND_AFTER_CONN_WITH_IN, 5000L);

        try{
            HttpClientPool pool = HttpClientPool.getInstance(this.host, this.port, 1, options, null);
            HttpClient client = pool.getClient();

            this.available=client!=null;

            return client;
        }catch (Exception e){
            this.available=false;
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }
}
