package j.core.network.websocket;

import j.core.sys.SysUtil;
import lombok.Getter;
import lombok.Setter;

import java.net.Proxy;
import java.net.URI;

@Getter
@Setter
public class ClientContainer{
	//最大空闲时间（无限制）
	public static final long MAX_IDLE_NO_LIMIT = 0;

	//最大空闲时间（1小时）
	public static final long MAX_IDLE_1H = 3600L;

	//最大空闲时间（1天）
	public static final long MAX_IDLE_1D = 86400L;

	//客户端实现类
	private String clientClass;

	//客户端
	private Client client;

	//服务端地址
	private String serverUrl;

	//是否重连
	private boolean reconnect;

	//是否发送心跳
	private boolean heartbeat;

	//最大空闲时间（单位：秒）
	private long maxIdle=MAX_IDLE_1H;

	/**
	 *
	 * @param clientClass
	 * @param serverUrl
	 * @param reconnect
	 * @param heartbeat
	 * @throws Exception
	 */
	public ClientContainer(String clientClass, String serverUrl, boolean reconnect, boolean heartbeat) throws Exception{
		Client _client = (Client)Class.forName(clientClass).getConstructor(new Class[] {String.class}).newInstance(new Object[] {serverUrl});

		this.client = _client;

		this.clientClass=clientClass;
		this.serverUrl=serverUrl;
		this.reconnect=reconnect;
		this.heartbeat=heartbeat;

		//托管
		ClientManager.hosting(this);
	}

	/**
	 *
	 * @param client
	 * @param serverUrl
	 * @param reconnect
	 * @param heartbeat
	 * @throws Exception
	 */
	public ClientContainer(Client client, String serverUrl, boolean reconnect, boolean heartbeat) throws Exception{
		this.client = client;

		this.clientClass=this.client.getClass().getCanonicalName();
		this.serverUrl=serverUrl;
		this.reconnect=reconnect;
		this.heartbeat=heartbeat;

		//托管
		ClientManager.hosting(this);
	}

	/**
	 *
	 * @param proxy
	 */
	public void setProxy(Proxy proxy) throws Exception{
		this.client.setProxy(proxy);
	}

	/**
	 *
	 */
	public void removeProxy() throws Exception{
		this.client.setProxy(Proxy.NO_PROXY);
	}

	/**
	 *
	 * @throws Exception
	 */
	public void connect() throws Exception{
		client.connect();
	}

	/**
	 * 关闭连接
	 */
	public void disconnect(){
		try{
			this.client.setDisconnected(true);
			this.client.disconnect();
		}catch (Exception e){}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void reconnect() throws Exception{
		this.client.reconnect();
	}

	/**
	 * 是否空闲超过最大时间
	 * @return
	 */
	public boolean isIdle(){
		return this.getMaxIdle() > 0 && SysUtil.getNow() - this.client.getLastActive() >= this.getMaxIdle()*1000;
	}

	/**
	 * 保持连接
	 */
	protected void keepAlive(){
		try {
			if(this.client==null) return;

			//关闭空闲连接
			if(isIdle() && !this.client.isDisconnected()){
				this.disconnect();
			}

			//如果已经断开连接且设置为自动重连
			if(this.client.isDisconnected() && this.isReconnect()) {
				this.reconnect();
			}

			//如果设置为发送心跳
			if(this.isHeartbeat()){
				this.client.heartbeat();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}