package j.core.network.socket;

import j.core.Startup;
import j.util.ConcurrentList;

/**
 * 
 * @author 肖炯
 *
 * 2019年3月30日
 *
 * <b>功能描述</b> 监控与Server建立连接的客户端，并在其超时时关闭
 */
public class Monitor implements Runnable{
	private Server server;
	
	/**
	 * 
	 * @param server
	 */
	public Monitor(Server server) {
		this.server=server;
	}
	
	@Override
	public void run() {
		while(!Startup.isDestroyed()){
			try {
				Thread.sleep(100);
			}catch(Exception e) {}
			
			try {
				ConcurrentList clients=server.getClients();
				for(int i=0;i<clients.size();i++) {
					Client c=(Client)clients.get(i);
					c.end(false);
				}
			}catch(Exception e) {}
		}
	}
}
