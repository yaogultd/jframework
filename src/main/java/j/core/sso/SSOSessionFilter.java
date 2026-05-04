package j.core.sso;

import j.core.annotation.description.ClassDescription;
import j.core.cache.JCacheFilter;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

@ClassDescription(author = "肖炯",
		date = "2021-07-31",
		description = "用于在缓存中查找SSOSession的过滤器")
@Getter
@Setter
public class SSOSessionFilter implements JCacheFilter {
	private static final long serialVersionUID = 1L;
	private String accessToken;
	private String userId;
	private String subUserId;
	private Boolean includeSubUsers=false;

	public SSOSessionFilter(String accessToken){
		this.accessToken=accessToken;
	}

	/**
	 *
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 */
	public SSOSessionFilter(String userId, String subUserId, boolean includeSubUsers) {
		this.userId=userId;
		this.subUserId=subUserId;
		this.includeSubUsers=includeSubUsers;
	}

	/**
	 *
	 * @param accessToken
	 * @param userId
	 * @param subUserId
	 * @param includeSubUsers
	 */
	public SSOSessionFilter(String accessToken, String userId, String subUserId, boolean includeSubUsers) {
		this.accessToken=accessToken;
		this.userId=userId;
		this.subUserId=subUserId;
		this.includeSubUsers=includeSubUsers;
	}

	/*
	 *  (non-Javadoc)
	 * @see j.core.cache.JCacheFilter#matches(java.lang.Object)
	 */
	public boolean matches(Object object) {
		if(object==null || !(object instanceof SSOSession)) return false;
		
		SSOSession session=(SSOSession)object;

		//如果指定了accessToken
		if(!JUtilString.isBlank(this.accessToken) && !this.accessToken.equals(session.getAccessToken())){
			return false;
		}

		if(!JUtilString.isBlank(this.userId)){//如果指定了用户ID
			if(!this.userId.equals(session.getUserId())) return false;

			if(!JUtilString.isBlank(this.subUserId)){//并且指定了子账号用户ID
				return this.subUserId.equals(session.getSubUserId());
			}

			//包含属于subUserId的子账号（比如登出主账号时，同时登出子账号）
			if(this.includeSubUsers){
				return this.userId.equals(session.getUserId());
			}

			//如果不包含子账号，则仅匹配主账号会话
			return this.userId.equals(session.getUserId())
					&& JUtilString.isBlank(session.getSubUserId());
		}
		
		return true;
	}
}
