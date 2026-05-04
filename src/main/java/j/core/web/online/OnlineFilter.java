package j.core.web.online;

import j.core.cache.JCacheFilter;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class OnlineFilter implements JCacheFilter{
	private String uaId;
	private String ip;
	private String uid;
	private String subUserId;
	private String uname;
	private String unick;
	private String[] uids;
	private int login=-100;//-100,全部; 0，未登录的用户; 1，登录的用户
	private String globalSessionId;
	private String sessionId;
	private String sysId;
	private String machineId;

	/**
	 *
	 * @param sysId
	 */
	public OnlineFilter(String sysId){
		this.sysId=sysId;
	}

	@Override
	public boolean matches(Object obj) {
		if(obj==null) return false;
		
		Online v=(Online)obj;

		if(uaId!=null&&!"".equals(uaId)&&!uaId.equals(v.getUaId())) return false;
		
		if(ip!=null&&!"".equals(ip)&&!ip.equals(v.getCurrentIp())) return false;
		
		if(uid!=null&&!"".equals(uid)&&!uid.equals(v.getUid())) return false;

		if(subUserId!=null&&!"".equals(subUserId)&&!subUserId.equals(v.getSubUserId())) return false;

		if(uname!=null&&!"".equals(uname)&&!uname.equals(v.getUname())) return false;

		if(unick!=null&&!"".equals(unick)&&!unick.equals(v.getUnick())) return false;

		if(uids!=null&&!JUtilString.contain(uids,v.getUid())) return false;

		if(login==0&&!JUtilString.isBlank(v.getUid())) return false;

		if(login==1&&JUtilString.isBlank(v.getUid())) return false;

		if(globalSessionId!=null&&!"".equals(globalSessionId)&&!globalSessionId.equals(v.getGlobalSessionId())) return false;

		if(sessionId!=null&&!"".equals(sessionId)&&!sessionId.equals(v.getCurrentSessionId())) return false;

		if(sysId!=null&&!"".equals(sysId)&&!sysId.equals(v.getCurrentSysId())) return false;
		
		if(machineId!=null&&!"".equals(machineId)&&!machineId.equals(v.getCurrentMachineId())) return false;
		
		return true;
	}
}
