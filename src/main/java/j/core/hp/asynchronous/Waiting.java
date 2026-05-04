package j.core.hp.asynchronous;

import j.core.common.Global;
import j.core.common.JObject;
import j.core.sys.SysUtil;

import java.util.concurrent.*;

/**
 * 
 * @author 肖炯
 *
 * 2019年4月16日
 *
 * <b>功能描述</b> 等待一个异步操作的执行结果
 */
public class Waiting{
	private long created=0;//创建时间
	private String UUID;//uuid
	private long timeout=30000L;//等待超时时间
	private Object defaultResultWhenTimeout=null;//当等待超时时设置的默认结果

	private Object result=null;//执行结果
	private boolean finished=false;//是否已经完成（已设置结果）
	private long finishedTime=0L;//完成时间
	private boolean got=false;//调用者是否已取回结果（可以从列表中移除了）

	/**
	 * 
	 * @param UUID
	 * @param timeout
	 * @param defaultResultWhenTimeout
	 */
	public Waiting(String UUID,long timeout,Object defaultResultWhenTimeout) {
		this.created=System.currentTimeMillis();
		this.UUID=UUID;
		if(timeout>0) this.timeout=timeout;
		this.defaultResultWhenTimeout=defaultResultWhenTimeout;
	}

	/**
	 *
	 * @return
	 */
	public String getUUID() {
		return this.UUID;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isTimeout() {
		return System.currentTimeMillis()-this.created>this.timeout;
	}

	/**
	 * 结果是否被遗弃（设置了结果或超时一秒未取）
	 * @return
	 */
	public boolean isAbandoned() {
		return (this.finished && System.currentTimeMillis() - this.finishedTime > 1000) || this.isTimeout();
	}

	/**
	 *
	 * @return
	 */
	public Object getDefaultResultWhenTimeout(){
		return this.defaultResultWhenTimeout;
	}

	/**
	 *
	 */
	public boolean isFinished() {
		return this.finished;
	}
	
	/**
	 * 
	 * @return
	 */
	public void setGot() {
		this.got=true;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isGot() {
		return this.got;
	}

	/**
	 *
	 * @param result
	 */
	public void setResult(Object result) {
		this.result=result;
		this.finished=true;
		this.finishedTime=System.currentTimeMillis();
		try{
			notifyAll();
		}catch (Exception e){}
	}

	/**
	 *
	 * @return
	 */
	public Object getResult() {
		while(!this.isFinished() && !this.isTimeout()){
			try{
				wait();
			}catch (Exception e){}
		}
		this.setGot();//设置结果已取回
		return this.result;
	}
}
