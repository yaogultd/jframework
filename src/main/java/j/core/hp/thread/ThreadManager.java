package j.core.hp.thread;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilBean;
import j.util.JUtilString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ClassDescription(author = "肖炯",
		date = "2022/11/20",
		description = "并发线程池管理")
public class ThreadManager implements Runnable{
	private static Logger log=Logger.create(ThreadManager.class);
	private static ConcurrentMap<String, ThreadPool> pools=new ConcurrentMap<>();
	
	static{
		ThreadManager m=new ThreadManager();
		Thread t=new Thread(m);
		t.start();
		
		log.log("并发线程池管理类已启动......", -1);
	}

	/**
	 *
	 * @param poolId
	 * @return
	 */
	public static ThreadPool getPool(String poolId) {
		if (JUtilString.isBlank(poolId)) return null;
		return pools.get(poolId);
	}

	/**
	 *
	 * @param poolId
	 * @param runnerId
	 * @return
	 */
	public static ThreadRunner getRunner(String poolId, String runnerId) {
		ThreadPool pool = getPool(poolId);
		if(pool == null) return null;
		return pool.getRunner(runnerId);
	}


	
	/**
	 * 
	 * @param poolId
	 * @param poolSize
	 * @param interval
	 * @param destroyAfterIdle
	 * @return
	 */
	public static ThreadPool getPool(String poolId,int poolSize,long interval,long destroyAfterIdle){
		if(JUtilString.isBlank(poolId)) return null;

		synchronized (poolId.intern()){
			if(pools.containsKey(poolId)){
				return pools.get(poolId).getInstance(poolSize);
			}else{
				Map<String, String> params=new LinkedHashMap<>();
				params.put("poolId", poolId);
				params.put("poolSize", ""+poolSize);
				params.put("interval", ""+interval);
				params.put("destroyAfterIdle", ""+destroyAfterIdle);
				log.log("创建线程池 -> "+ JUtilBean.map2Json(params), -1);
				ThreadPool pool=new ThreadPool(poolId,poolSize,interval,destroyAfterIdle);
				pools.put(poolId,pool);
				return pool;
			}
		}
	}


	/**
	 *
	 * @param poolId
	 * @param poolSize
	 * @param interval
	 * @param destroyAfterIdle
	 * @param selectType
	 * @return
	 */
	public static ThreadPool getPool(String poolId,int poolSize,long interval,long destroyAfterIdle,int selectType){
		if(JUtilString.isBlank(poolId)) return null;

		synchronized(poolId.intern()) {
			if (pools.containsKey(poolId)) {
				return pools.get(poolId).getInstance(poolSize);
			} else {
				Map<String, String> params = new LinkedHashMap<>();
				params.put("poolId", poolId);
				params.put("poolSize", "" + poolSize);
				params.put("interval", "" + interval);
				params.put("destroyAfterIdle", "" + destroyAfterIdle);
				params.put("selectType", "" + selectType);
				log.log("创建线程池 -> " + JUtilBean.map2Json(params), -1);
				ThreadPool pool = new ThreadPool(poolId, poolSize, interval, destroyAfterIdle, selectType);
				pools.put(poolId, pool);
				return pool;
			}
		}
	}


	/**
	 *
	 * @param poolId
	 * @param poolSize
	 * @param interval
	 * @param timeUnit
	 * @param destroyAfterIdle
	 * @param selectType
	 * @return
	 */
	public static ThreadPool getPool(String poolId, int poolSize, long interval, TimeUnit timeUnit,long destroyAfterIdle, int selectType){
		if(JUtilString.isBlank(poolId)) return null;

		synchronized(poolId.intern()) {
			if(pools.containsKey(poolId)) {
				return pools.get(poolId).getInstance(poolSize);
			}else{
				Map<String, String> params = new LinkedHashMap<>();
				params.put("poolId", poolId);
				params.put("poolSize", "" + poolSize);
				params.put("interval", "" + interval);
				params.put("timeUnit", "" + timeUnit);
				params.put("destroyAfterIdle", "" + destroyAfterIdle);
				params.put("selectType", "" + selectType);
				log.log("创建线程池 -> " + JUtilBean.map2Json(params), -1);
				ThreadPool pool = new ThreadPool(poolId, poolSize, interval, timeUnit, destroyAfterIdle, selectType);
				pools.put(poolId, pool);
				return pool;
			}
		}
	}

	@Override
	public void run(){
		while(!Startup.isDestroyed()){
			try{
				Thread.sleep(1000);
			}catch(Exception e){}
			if(Startup.isDestroyed()) return;
			
			try{
				List<String> poolIds=pools.listKeys();
				for(int i=0;i<poolIds.size();i++){
					String poolId = poolIds.get(i);
					ThreadPool pool=pools.get(poolId);
					if(pool==null) continue;
					boolean removed = pool.clearIfIdle();
					if(removed) pools.remove(poolId);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
