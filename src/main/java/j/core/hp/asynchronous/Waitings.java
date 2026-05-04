package j.core.hp.asynchronous;

import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import j.util.JUtilUUID;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author 肖炯
 *
 * 2019年4月16日
 *
 * <b>功能描述</b> 当一个进程触发一个异步操作后，如果需要等待获得该异步操作的结果才能返回，该类通过分配一个UUID将当前进程与异步操作关联起来，异步操作向UUID设置操作结果，当前进程从UUID查询结果，并可设定等待超时时间。
 */
public class Waitings extends TimerTask {
	private static Logger log=Logger.create(Waitings.class);

	//等待列表
	private static ConcurrentMap<String,Waiting> waitings=new ConcurrentMap<String,Waiting>(false, new ConcurrentHashMap<>());

	//定时任务执行
	private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	//启动监控线程，用于清除已获取结果的等待
	static {
		scheduledExecutorService.scheduleAtFixedRate(new Waitings(), 1000, 10, TimeUnit.MILLISECONDS);
		log.log("waitings monitor started",-1);
	}
	
	/**
	 * 
	 * @param timeout
	 * @param defaultResultWhenTimeout
	 * @return 返回为任务分配的uuid
	 */
	public static String waiting(long timeout, Object defaultResultWhenTimeout) {
		return waiting(JUtilUUID.genUUID(), timeout, defaultResultWhenTimeout);
	}
	
	/**
	 * @param UUID
	 * @param timeout
	 * @param defaultResultWhenTimeout
	 * @return 返回为任务分配的uuid
	 */
	public static String waiting(String UUID,long timeout,Object defaultResultWhenTimeout) {
		if(JUtilString.isBlank(UUID)) UUID=JUtilUUID.genUUID();
		Waiting waiting=new Waiting(UUID,timeout,defaultResultWhenTimeout);
		waitings.put(UUID, waiting);
		return UUID;
	}

	/**
	 * 设置结果
	 * @param UUID
	 * @param result
	 */
	public static void setResult(String UUID, Object result) {
		Waiting waiting=waitings.get(UUID);
		if(waiting==null) return;
		waiting.setResult(result);
	}
	
	/**
	 * 得到结果（直到结果返回或超时）
	 * @param UUID
	 * @return
	 */
	public static Object getResult(String UUID) {
		Waiting waiting = waitings.get(UUID);
		if (waiting == null) return null;

		try {
			return waiting.getResult();
		}catch (Exception e){
			log.log(e, Logger.LEVEL_DEBUG);
			return waiting.getDefaultResultWhenTimeout();
		}
	}

	/**
	 * 查询是否已有结果
	 * @param UUID
	 * @return
	 */
	public static Object hasResult(String UUID) {
		Waiting waiting=waitings.get(UUID);
		if(waiting==null) return null;

		if(waiting.isFinished()){
			waiting.setGot();
			return waiting.getResult();
		}

		if(waiting.isTimeout()){
			waiting.setGot();
			return waiting.getDefaultResultWhenTimeout();
		}

		return null;
	}
	
	@Override
	public void run() {
		List<Waiting> _waitings=waitings.listValues();
		for(int i=0; i<_waitings.size(); i++) {
			Waiting waiting=_waitings.get(i);
			if(waiting==null) continue;

			//结果已被取走 或 被遗弃
			if(waiting.isGot() || waiting.isAbandoned()) {
				try{
					waiting.notifyAll();
				}catch (Exception e){}

				waitings.remove(waiting.getUUID());
				waiting=null;
			}
		}
	}
}
