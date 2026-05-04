package j.core.hp.thread;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilRandom;
import j.util.JUtilSorter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ClassDescription(author = "肖炯",
		date = "2022/11/20",
		description = "通用线程池")
@Setter
@Getter
public class ThreadPool extends TimerTask {
	private static Logger log=Logger.create(ThreadPool.class);

	//等待任务执行结果的最长时间（一小时）
	public static final long MAX_TIMEOUT=3600000L;

	//线程选择模式：轮询
	public static final int SELECT_TYPE_ROTATION=0;

	//线程选择模式：优先给最空闲的
	public static final int SELECT_TYPE_IDLEST=1;

	//线程选择模式：随机
	public static final int SELECT_TYPE_RANDOM=2;

	//线程池ID
	private String poolId;

	//线程池大小
	private int poolSize=1;

	//线程池内线程执行任务间隔，单位：ms
	private long interval=1000;

	private TimeUnit timeUnit=TimeUnit.MILLISECONDS;

	//线程池空间多久后销毁，单位：ms
	private long destroyAfterIdle=0;

	//线程池最新使用时间，单位：ms
	private long latestUsed=0;

	//任务执行器
	private ConcurrentList<ThreadRunner> threads=new ConcurrentList();

	//线程选择方式
	private int selectType=0;

	//线程选择游标
	private int selector=0;

	//线程池监视处理器
	private ThreadPoolMonitor monitor;

	//定时任务执行
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	//是否第一次完成任务
	private boolean firstExecuted=false;

	//重置后是否添加过任务
	private boolean executing=false;

	/**
	 * 创建线程池实例
	 * @param poolId
	 * @param poolSize
	 * @param interval
	 * @param destroyAfterIdle
	 */
	public ThreadPool(String poolId,int poolSize,long interval,long destroyAfterIdle){
		this(poolId, poolSize, interval, destroyAfterIdle, SELECT_TYPE_IDLEST);
	}

	/**
	 * 创建线程池实例
	 * @param poolId
	 * @param poolSize
	 * @param interval
	 * @param destroyAfterIdle
	 * @param selectType
	 */
	public ThreadPool(String poolId,int poolSize,long interval,long destroyAfterIdle, int selectType){
		this(poolId, poolSize, interval, TimeUnit.MILLISECONDS, destroyAfterIdle, selectType);
	}

	/**
	 * 创建线程池实例
	 * @param poolId
	 * @param poolSize
	 * @param interval
	 * @param timeUnit
	 * @param destroyAfterIdle
	 * @param selectType
	 */
	public ThreadPool(String poolId,int poolSize,long interval,TimeUnit timeUnit,long destroyAfterIdle, int selectType){
		if(poolSize<=0) poolSize=1;
		this.poolId=poolId;
		this.poolSize=poolSize;
		this.interval=interval;
		this.timeUnit=timeUnit;
		this.destroyAfterIdle=destroyAfterIdle;
		if(selectType != SELECT_TYPE_ROTATION && selectType != SELECT_TYPE_IDLEST && selectType != SELECT_TYPE_RANDOM) selectType=SELECT_TYPE_ROTATION;
		this.selectType=selectType;
		scheduledExecutorService.scheduleAtFixedRate(this,1000, 1000, TimeUnit.MILLISECONDS);
		this.createRunners();
		if(this.monitor!=null) this.monitor.onStart();
	}

	/**
	 * 获取线程池实例，并调整线程池大小（如有变化）
	 * @param poolSize
	 * @return
	 */
	synchronized public ThreadPool getInstance(int poolSize){
		this.poolSize=poolSize;
		this.createRunners();
		return this;
	}

	/**
	 *
	 */
	private void createRunners(){
		while(threads.size() < this.poolSize){
			ThreadRunner runner=new ThreadRunner(this);
			threads.add(runner);
		}
	}

	/**
	 * 判断任务是否已经存在线程池中，如果是则返回负责执行该任务的线程
	 * @param task
	 * @return
	 */
	public ThreadRunner exists(ThreadTask task){
		for(int i=0;i<this.threads.size();i++){
			ThreadRunner runner=this.threads.get(i);
			if(runner.exists(task)) return runner;
		}
		return null;
	}

	/**
	 * 判断任务是否已经存在线程池中，如果是则返回负责执行该任务的线程
	 * @param taskUuid
	 * @return
	 */
	public ThreadRunner exists(String taskUuid){
		for(int i=0;i<this.threads.size();i++){
			ThreadRunner runner=this.threads.get(i);
			if(runner.exists(taskUuid)) return runner;
		}
		return null;
	}

	/**
	 *
	 * @param runnerId
	 * @return
	 */
	public ThreadRunner getRunner(String runnerId){
		for(int i=0;i<this.threads.size();i++){
			ThreadRunner runner=this.threads.get(i);
			if(runner.getId().equals(runnerId)) return runner;
		}
		return null;
	}

	/**
	 * 线程池内所有待执行任务数
	 * @return
	 */
	public int getTasks(){
		int c=0;
		for(int i=0; i<threads.size(); i++){
			c+=threads.get(i).getTasksCount();
		}
		return c;
	}

	/**
	 * 选择执行线程
	 * @return
	 */
	private ThreadRunner selectRunner(){
		if(this.selectType==SELECT_TYPE_ROTATION){//轮询模式
			if(this.selector > this.threads.size() - 1) this.selector=0;
			return this.threads.get(this.selector++);
		}

		if(this.selectType==SELECT_TYPE_RANDOM){//随机
			return this.threads.get(JUtilRandom.nextInt(this.threads.size()));
		}

		//分配给最空闲的
		//根据执行线程任务队列长度排序，队列最小的（最空闲的）排最前（run方法中定时排序）
		return this.threads.get(0);
	}

	/**
	 * 添加任务到线程池，并返回负责执行该任务的线程
	 * @param task
	 * @return
	 */
	synchronized public ThreadRunner addTask(ThreadTask task){
		this.executing=true;
		this.latestUsed=SysUtil.getNow();
		task.setPoolId(this.poolId);

		ThreadRunner runner=exists(task);

		//任务已经存在
		if(runner!=null) return runner;

		runner=this.selectRunner();
		if(runner != null) runner.addTask(task);

		return runner;
	}

	/**
	 * 批量添加任务到线程池，并返回负责执行每个任务的线程
	 * @param tasks
	 * @return 负责执行每个任务的线程 key:taskUuid, value:ThreadRunner
	 */
	public ConcurrentMap<String, ThreadRunner> addTasks(List<ThreadTask> tasks){
		this.executing=true;
		ConcurrentMap<String, ThreadRunner> runners=new ConcurrentMap();
		for(int i=0; i<tasks.size(); i++) {
			ThreadTask t=tasks.get(i);
			runners.put(t.getUuid(), this.addTask(t));
		}

		return runners;
	}

	/**
	 * 执行指定任务列表，并等获取到所有执行结果后（或超时）才返回
	 * @param tasks
	 * @return
	 */
	public ConcurrentMap<String, ThreadTaskResult> execute(List<ThreadTask> tasks){
		ConcurrentMap<String,ThreadTaskResult> results=new ConcurrentMap<>();

		List<ThreadRunner> runners=new ArrayList<>();
		for(int i=0; i<tasks.size(); i++) {
			runners.add(this.addTask(tasks.get(i)));
		}

		while(results.size() < runners.size()) {
			for(int i=0; i<runners.size(); i++) {
				String uuid=tasks.get(i).getUuid();
				ThreadTaskResult result=runners.get(i).getResult(uuid);
				if(result!=null) results.put(uuid, result);
			}
			try {
				Thread.sleep(10);
			}catch(Exception e) {}
		}

		return results;
	}

	/**
	 * 执行指定任务，并等获取到执行结果后（或超时）才返回
	 * @param task
	 * @return
	 */
	public ThreadTaskResult execute(ThreadTask task){
		ConcurrentMap<String,ThreadTaskResult> results=new ConcurrentMap<>();
		ThreadRunner runner=this.addTask(task);
		return runner.getResult(task.getUuid());
	}

	/**
	 * 如果系统已经停止，或超出最大空闲时间，销毁该线程池
	 */
	public boolean clearIfIdle(){
		if(Startup.isDestroyed()
				|| (this.destroyAfterIdle>0 && this.latestUsed>0 && SysUtil.getNow()-this.latestUsed>this.destroyAfterIdle)){
			for(int i=0;i<this.threads.size();i++){
				this.threads.get(i).destroy();
			}
			this.threads.clear();
			this.latestUsed=0;
			this.selector=0;
			this.scheduledExecutorService.shutdownNow();
			return true;
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public boolean isExecuted(){
		if(!this.executing) return false;
		boolean executed=true;
		for(int i=0;i<this.threads.size();i++){
			executed = this.threads.get(i).isExecuted();
			if(!executed) break;
		}
		return executed;
	}

	/**
	 *
	 * @return
	 */
	public boolean isEmpty() {
		boolean empty=true;
		for(int i=0;i<this.threads.size();i++){
			empty = this.threads.get(i).isEmpty();
			if(!empty) break;
		}
		return empty;
	}

	/**
	 * 重置：清空所有在执行的任务
	 */
	public void reset(){
		for(int i=0;i<this.threads.size();i++){
			this.threads.get(i).reset();
		}
		if(this.monitor!=null) this.monitor.onReset();
		this.firstExecuted=false;
		this.executing=false;
	}

	/**
	 * 根据执行线程任务队列长度排序，队列最小的（最空闲的）排最前
	 */
	synchronized private void sort(){
		if(this.selectType==SELECT_TYPE_IDLEST){//只有最闲模式下才需要排序
			this.threads=(ConcurrentList)ThreadRunnerSorter.instance().bubble(this.threads, JUtilSorter.ASC);
		}
	}

	@Override
	public void run() {
		//移除过期结果
		for(int i=0; i<this.threads.size(); i++){
			this.threads.get(i).removeResultsTimeout();
		}

		//排序
		sort();

		if(!this.executing) return;//重置后未添加过任务

		if(!this.firstExecuted  && this.isExecuted()){
			this.firstExecuted=true;
			if(this.monitor!=null) this.monitor.onExecuted();
		}
	}
}