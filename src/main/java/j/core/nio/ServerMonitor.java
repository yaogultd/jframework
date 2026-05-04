package j.core.nio;

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
public class ServerMonitor implements Runnable{
	private Server server;
	
	/**
	 * 
	 * @param server
	 */
	public ServerMonitor(Server server) {
		this.server=server;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(1000);
			}catch(Exception e) {}

			ConcurrentList<ServerHandler> handlers=server.getHandlers();
			for(int i=0;i<handlers.size();i++) {
				try {
					ServerHandler c=handlers.get(i);
					c.clearTimeoutDataSegments();
					c.clearInvalidPackages();//清除无效/过期数据包
					c.end(false);
				}catch(Exception e) {}
			}
		}
	}
}