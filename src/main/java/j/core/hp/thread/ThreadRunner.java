package j.core.hp.thread;

import j.core.annotation.description.ClassDescription;
import j.core.nvwa.Nvwa;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import j.util.JUtilUUID;
import lombok.Getter;
import org.nustaq.serialization.FSTConfiguration;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ClassDescription(author = "肖炯",
		date = "2022/11/20",
		description = "任务执行线程")
@Getter
public class ThreadRunner extends TimerTask {
	private static Logger log=Logger.create(ThreadRunner.class);

	private FSTConfiguration fstConf = FSTConfiguration.createDefaultConfiguration();

	//id
	private String id;

	//所属线程池
	private ThreadPool inPool;

	//待执行任务列表
	private ConcurrentList<ThreadTask> tasks=new ConcurrentList<>();

	//任务执行结果
	private ConcurrentMap<String, ThreadTaskResult> results=new ConcurrentMap<>();

	//等待任务结果超时时间(以最新添加的一个任务设置的超时时间为准)
	private long timeout=ThreadPool.MAX_TIMEOUT;

	//执行任务次数计数器
	private long timer=0;

	//是否已经结束
	private boolean end=false;

	//定时任务执行
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	/**
	 *
	 * @param pool
	 */
	public ThreadRunner(ThreadPool pool){
		this.id=JUtilUUID.genUUID();
		this.inPool=pool;
		scheduledExecutorService.scheduleAtFixedRate(this,100, inPool.getInterval(), inPool.getTimeUnit());
	}

	/**
	 *
	 * @return
	 */
	public int getTasksCount(){
		return tasks.size();
	}

	/**
	 *
	 * @return
	 */
	public boolean isEmpty(){
		return tasks.isEmpty();
	}

	/**
	 *
	 * @param task
	 * @return
	 */
	public boolean exists(ThreadTask task){
		for(int i=0;i<tasks.size();i++){
			ThreadTask t=tasks.get(i);
			if(t==null) continue;
			if(t.equalz(task)) return true;
		}
		return false;
	}

	/**
	 *
	 * @param taskUuid
	 * @return
	 */
	public boolean exists(String taskUuid){
		for(int i=0;i<tasks.size();i++){
			ThreadTask t=tasks.get(i);
			if(t==null) continue;
			if(taskUuid.equals(t.getUuid())) return true;
		}
		return false;
	}

	/**
	 *
	 * @param task
	 */
	public boolean addTask(ThreadTask task){
		if(end) return false;

		if(!this.exists(task)){
			task.setRunnerId(this.id);
			task.setCreateTime(SysUtil.getNow());
			timeout=task.getResultTimeout();
			tasks.add(task);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param task
	 */
	public void removeTask(ThreadTask task){
		for(int i=0;i<tasks.size();i++){
			ThreadTask t=tasks.get(i);
			if(t==null) continue;
			if(t.equalz(task)){
				tasks.remove(i);
				return;
			}
		}
	}

	/**
	 *
	 * @param taskUuid
	 */
	public void removeTask(String taskUuid){
		for(int i=0;i<tasks.size();i++){
			ThreadTask t=tasks.get(i);
			if(t==null) continue;
			if(taskUuid.equals(t.getUuid())){
				tasks.remove(i);
				return;
			}
		}
	}

	/**
	 *
	 * @param uuid
	 * @return
	 */
	public ThreadTaskResult getResult(String uuid) {
		if(uuid==null||"".equals(uuid)) return null;

		long wait=0;
		while(!results.containsKey(uuid)) {
			try {
				Thread.sleep(10);
			}catch(Exception e) {}
			wait+=10;
			if(wait>=timeout) break;
		}
		ThreadTaskResult result=results.remove(uuid);
		if(result==null) return null;

		Object[] resultObjs=result.getResult();
		if(resultObjs!=null && resultObjs.length>0 && Nvwa.IS_NULL.equals(resultObjs[0])) {
			return null;
		}

		return result;
	}

	/**
	 *
	 * @param uuid
	 * @return
	 */
	public ThreadTaskResult getResultAsyn(String uuid) {
		if(uuid==null||"".equals(uuid)) return null;

		if(results.containsKey(uuid)) {
			ThreadTaskResult result=results.remove(uuid);
			return result;
		}

		return null;
	}

	/**
	 * 是否所有任务均已执行
	 */
	public boolean isExecuted(){
		boolean executed=true;
		for(int i=0;i<this.tasks.size();i++){
			executed = (this.tasks.get(i).getExecutedTimes() > 0);
			if(!executed) break;
		}
		return executed;
	}

	/**
	 * 重置：清空所有在执行的任务
	 */
	public void reset(){
		results.clear();
		tasks.clear();
	}

	/**
	 *
	 */
	public void destroy(){
		end=true;
		scheduledExecutorService.shutdownNow();
		results.clear();
		tasks.clear();
	}

	/**
	 * 移除超时结果
	 */
	public void removeResultsTimeout(){
		try {
			List<String> keys = results.listKeys();
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);

				ThreadTaskResult result = results.get(key);
				if (result != null && result.isTimeout()) {
					results.remove(key);
					result = null;
				}
			}
			keys.clear();
			keys = null;
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	@Override
	public void run(){
		if(this.end || this.tasks.isEmpty()) return;

		ThreadTask task=tasks.remove(0);
		try{
			int retries=0;
			while(retries <= task.getRetries()){
				try{
					Object[] result=task.execute();

					//如果任务指定了ID，即使没有结果，也写入表示为null的固定字符串（以免调用上下文获取不到结果无限等待）
					if(!JUtilString.isBlank(task.getUuid())){
						results.put(task.getUuid(), new ThreadTaskResult(task.getUuid(), result==null ? (new Object[] {Nvwa.IS_NULL}) : result, task.getResultTimeout()));
					}
					break;
				}catch(Exception e){
					retries++;
					log.log(e,Logger.LEVEL_ERROR);
				}
			}

			//标记已执行一次
			task.onExecuted();

			//未达最大执行次数，移动到队列末尾（循环执行队列内任务）
			if(!task.exceedMaxLoops()) tasks.add(task);
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);

			//未达最大执行次数，移动到队列末尾（循环执行队列内任务）
			if(!task.exceedMaxLoops()) tasks.add(task);
		}
	}
}