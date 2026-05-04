package j.core.web.online;

import j.core.sso.SSOConfig;
import j.core.cache.JCacheFilter;
import j.core.sys.SysUtil;

/**
 * 
 * @author 肖炯
 *
 */ 
public class OnlineRemover implements JCacheFilter{
	private static final long serialVersionUID = 1L;

	/*
	 *  (non-Javadoc)
	 * @see j.core.cache.JCacheFilter#matches(java.lang.Object)
	 */
	public boolean matches(Object obj) {
		Online v=(Online)obj;
		
		long now=SysUtil.getNow();
		
		if(now-v.getUpdateTime()>SSOConfig.getOnlineActiveTime()*1000){
			return true;
		}
		return false;
	}
}
