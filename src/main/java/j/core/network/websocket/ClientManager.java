package j.core.network.websocket;

import j.log.Logger;
import j.util.ConcurrentList;
import lombok.Getter;
import lombok.Setter;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ClientManager extends TimerTask {
	private static Logger log=Logger.create(ClientManager.class);

	//客户端
	private static ConcurrentList<ClientContainer> clients = new ConcurrentList<>();

	//定时任务执行
	private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	//启动监控线程，用于清除已获取结果的等待
	static {
		scheduledExecutorService.scheduleAtFixedRate(new ClientManager(), 1000, 1000, TimeUnit.MILLISECONDS);
		log.log("ClientManager for websocket client started",-1);
	}

	/**
	 * 托管
	 * @param clientContainer
	 */
	public static void hosting(ClientContainer clientContainer){
		clients.add(clientContainer);
	}

	@Override
	public void finalize(){
		if(scheduledExecutorService != null) scheduledExecutorService.shutdownNow();
	}

	@Override
	public void run() {
		for(ClientContainer clientContainer : clients){
			if(clientContainer==null) continue;
			clientContainer.keepAlive();
		}
	}
}