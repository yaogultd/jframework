package j.core.sso;


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
public class LoginStatus implements Serializable{
	public static final int STAT_CREATE=0;//登录状态-刚接收到server端某个用户登录的命令，但该用户还未曾到访
	public static final int STAT_VISITED=1;//登录状态-接收到server端某个用户登录的命令，并且该用户已经到访并成功加载用户信息
	
	private String           clientId;//SSO Client ID
	private String           accessToken;//全局会话ID
	private volatile long    refreshTime;//最近更新缓存时间
	private volatile long    updateTime;//最近访问系统的时间
	private String           userId;//用户ID
	private String           subUserId;//子账号ID
	private String           userIp;//用户Host
	private String           sysId;
	private String           machineId;
	private String           loginFrom;
	private String           loginFromDomain;
	private String           userAgent;
	private int              stat;//登录状态

	/**
	 *
	 * @param _clientId
	 * @param _accessToken
	 * @param _userId
	 * @param _userIp
	 * @param _sysId
	 * @param _machineId
	 * @param _loginFrom
	 * @param _loginFromDomain
	 */
	public LoginStatus(String _clientId,
			String _accessToken,
			String _userId,
			String _subUserId,
			String _userIp,
			String _sysId,
			String _machineId,
			String _loginFrom,
			String _loginFromDomain){
		clientId=_clientId;
		accessToken=_accessToken;
		userId=_userId;
		userIp=_userIp;
		subUserId=_subUserId;
		sysId=_sysId;
		machineId=_machineId;
		loginFrom=_loginFrom;
		loginFromDomain=_loginFromDomain;
		refreshTime=SysUtil.getNow();
		updateTime=SysUtil.getNow();
		stat=STAT_VISITED;
	}

	public boolean isTimeout(){
		return SysUtil.getNow()-updateTime>SSOConfig.getSessionTimeout()*1000L;
	}
	public void login(){
		this.stat=STAT_VISITED;
	}
}