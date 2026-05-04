package j.core.sso;

import j.core.cache.JCacheFilter;
import j.core.common.JObject;
import j.util.JObjectFilter;
import j.util.JUtilString;

/**
 * 
 * @author 肖炯
 *
 */
public class LoginStatusFilter extends JObjectFilter {
	private String userId=null;
	private String subUserId=null;
	private boolean includeSubUsers=false;

	/**
	 * 
	 * @param userId
	 */
	public LoginStatusFilter(String userId) {
		this.userId=userId;
	}
	
	/**
	 * 
	 * @param userId
	 * @param subUserId
	 */
	public LoginStatusFilter(String userId, String subUserId) {
		this.userId=userId;
		this.subUserId=subUserId;
	}
	
	/**
	 * 
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 */
	public LoginStatusFilter(String userId, String subUserId, boolean includeSubUsers) {
		this.userId=userId;
		this.subUserId=subUserId;
		this.includeSubUsers=includeSubUsers;
	}

	@Override
	public boolean matches(Object object) {
		if(object==null) return false;
		
		LoginStatus obj=(LoginStatus)object;
		if(!JUtilString.isBlank(this.userId)){//如果指定了用户ID
			if(!JUtilString.isBlank(this.subUserId)){//并且指定了子账号用户ID
				return this.userId.equals(obj.getUserId()) && this.subUserId.equals(obj.getSubUserId());
			}else if(this.includeSubUsers){//包含属于subUserId的子账号（比如登出主账号时，同时登出子账号）
				return this.userId.equals(obj.getUserId());
			}else{//如果不包含子账号，则仅匹配主账号会话
				return this.userId.equals(obj.getUserId()) && JUtilString.isBlank(obj.getSubUserId());
			}
		}
		
		return true;
	}
}
