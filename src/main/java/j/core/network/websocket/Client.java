package j.core.network.websocket;

import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.JUtilBean;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import okio.ByteString;

import java.net.Proxy;

public class Client extends WebSocketListener{
    private static Logger log=Logger.create(Client.class);

    protected OkHttpClient httpClient;
    protected WebSocket webSocket;

    @Getter
    @Setter
    protected String serverUrl;

    //是否已经连接
    @Getter
    @Setter
    protected boolean connected=false;

    //是否已经断开连接
    @Getter
    @Setter
    protected boolean disconnected=false;

    //最近活跃时间
    @Getter
    @Setter
    protected long lastActive=0;

    //最近活跃时间
    @Getter
    protected Proxy proxy;

    public Client(){
        this.lastActive = SysUtil.getNow();
    }

    public Client(String serverUrl){
        this.lastActive = SysUtil.getNow();
        this.serverUrl = serverUrl;
    }

    /**
     *
     * @param proxy
     * @throws Exception
     */
    public void setProxy(Proxy proxy) throws Exception{
        if(this.httpClient != null) throw new Exception("please set proxy before connecting");
        this.proxy = proxy;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        this.setLastActive(SysUtil.getNow());
        log.log("connection opened => response = "+ JUtilBean.bean2Json(response), -1);
        connected=true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        this.setLastActive(SysUtil.getNow());
        log.log("text message received => "+text, -1);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        this.setLastActive(SysUtil.getNow());
        log.log("bytes message received => "+bytes.hex(), -1);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        log.log("connection closing => code = "+reason+", reason = "+reason, -1);
        this.disconnect();
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.log("connection closed => code = "+reason+", reason = "+reason, -1);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        try{
            log.log("communication error => response = "+ JUtilBean.bean2Json(response), -1);
            t.printStackTrace();
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
    }

    /**
     *
     * @param serverUrl
     * @throws Exception
     */
    public void connect(String serverUrl) throws Exception{
        this.serverUrl = serverUrl;
        this.doConnect();
    }

    /**
     *
     * @throws Exception
     */
    public void connect() throws Exception{
        if(JUtilString.isBlank(this.serverUrl)) throw new Exception("serverUrl is not set");
        this.doConnect();
    }

    /**
     *
     */
    private void doConnect(){
        if(this.webSocket != null) return;
        this.createClient();
        this.webSocket = this.httpClient.newWebSocket(new Request.Builder().url(this.serverUrl).build(), this);
    }

    /**
     *
     * @throws Exception
     */
    public void reconnect() throws Exception{
        this.disconnect();
        this.doConnect();
    }

    /**
     *
     */
    private void createClient(){
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if(this.proxy != null) clientBuilder.proxy(proxy);
        this.httpClient = clientBuilder.build();
    }

    /**
     *
     */
    public void disconnect(){
        if(this.webSocket != null){
            try{
                this.webSocket.close(1000, null);
            }catch (Exception e){}
            this.webSocket = null;
        }

        if(httpClient != null) {
            try {
                httpClient.dispatcher().executorService().shutdown();
            } catch (Exception e) {}
            httpClient = null;
        }

        this.connected = false;
        this.disconnected = false;
    }

    /**
     *
     * @return
     */
    public void heartbeat() {
        //nothing to do
    }

    /**
     *
     * @param text
     */
    public void send(String text){
        this.webSocket.send(text);
    }
}
