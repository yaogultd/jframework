package j.core.hp.thread;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 
 * @author ceo
 *
 */
@Getter
@Setter
public abstract class ThreadTask implements Serializable {
	private static final long serialVersionUID=1L;
	protected String poolId;//所属线程池ID
	protected String runnerId;//所属执行线程ID
	protected String uuid;
	protected long createTime;
	protected long resultTimeout=ThreadPool.MAX_TIMEOUT;
	protected Object[] in;
	protected Object[] out;
	protected int loops=1;//任务重复执行次数（默认1次，设置为小于等于0则无限循环）
	protected int executedTimes=0;//已成功执行次数
	protected int retries=0;
	protected boolean printErrors=true;

	/**
	 *
	 * @param in
	 * @param retries
	 */
	public ThreadTask(Object[] in,int retries){
		this.retries=retries;
		this.in=in;
	}

	/**
	 *
	 * @param in
	 * @param retries
	 */
	public ThreadTask(Object[] in,int retries,String uuid){
		this.retries=retries;
		this.in=in;
		this.uuid=uuid;
	}

	/**
	 *
	 * @param in
	 * @param retries
	 * @param uuid
	 * @param resultTimeout
	 */
	public ThreadTask(Object[] in,int retries,String uuid,long resultTimeout){
		this.retries=retries;
		this.in=in;
		this.uuid=uuid;

		if(resultTimeout<=0 || resultTimeout>ThreadPool.MAX_TIMEOUT) resultTimeout=ThreadPool.MAX_TIMEOUT;
		this.resultTimeout=resultTimeout;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public abstract Object[] execute() throws Exception;

	/**
	 *
	 * @param other
	 * @return
	 */
	public abstract boolean equalz(ThreadTask other);

	/**
	 *
	 * @return
	 */
	public void onExecuted(){
		this.executedTimes++;
	}

	/**
	 *
	 * @return
	 */
	public boolean exceedMaxLoops(){
		if(this.loops<=0) return false;
		return (this.executedTimes>=this.loops);
	}
}
