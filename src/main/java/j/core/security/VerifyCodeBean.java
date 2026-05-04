package j.core.security;

import j.core.sys.SysUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class VerifyCodeBean implements Serializable{
	private static final long serialVersionUID = 1L;

	public static final int ERR_TOO_FREQUENT=-1;
	public static final int ERR_TOO_MANY_SURVIVALS=-2;
	public static final int ERR_BAD_REQUEST=-99;

	private String uuid;
	private String related;//关联的东西（手机号、邮箱、用户ID等）
	private String ip;
	private String code;
	private long timeout;
	private long interval;
	private long time;
	private int maxTries;//最多尝试次数
	private int tries;//已尝试次数
	private int error;//错误编码

	/**
	 *
	 * @param uuid
	 * @param code
	 * @param timeout
	 * @param interval
	 * @param maxTries
	 */
	public VerifyCodeBean(String uuid,String code,long timeout,long interval,int maxTries){
		this.uuid=uuid;
		this.code=code;
		this.timeout=timeout;
		this.interval=interval;
		this.maxTries=maxTries;
		this.tries=0;
		this.time=SysUtil.getNow();
	}

	/**
	 *
	 * @param uuid
	 * @param related
	 * @param ip
	 * @param code
	 * @param timeout
	 * @param interval
	 * @param maxTries
	 */
	public VerifyCodeBean(String uuid,String related,String ip, String code,long timeout,long interval,int maxTries){
		this.uuid=uuid;
		this.related=related;
		this.ip=ip;
		this.code=code;
		this.timeout=timeout;
		this.interval=interval;
		this.maxTries=maxTries;
		this.tries=0;
		this.time=SysUtil.getNow();
	}

	/**
	 * 是否已超时
	 * @return
	 */
	public boolean isTimeout(){
		return SysUtil.getNow() - this.time >= this.timeout;
	}

	/**
	 * 是否过于频繁
	 * @return
	 */
	public boolean isTooFrequent(){
		return SysUtil.getNow() - this.time < this.interval;
	}

	/**
	 * 是否该移除
	 * @return
	 */
	public boolean removable(){
		return SysUtil.getNow() - this.time >= this.interval;
	}
}
