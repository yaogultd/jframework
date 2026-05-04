package j.core.sso;


import j.core.cache.JCacheFilter;

/**
 * 
 * @author 肖炯
 *
 */
public class SSOSessionRemover implements JCacheFilter {
	private static final long serialVersionUID = 1L;
	private static SSOSessionRemover instance;
	private long sessionTimeout;

	/**
	 * 
	 *
	 */
	private SSOSessionRemover() {
		super();
	}
	
	/**
	 * 
	 * @return
	 */
	public static SSOSessionRemover getInstance(long sessionTimeout){
		if(instance==null) instance=new SSOSessionRemover();
		instance.sessionTimeout=sessionTimeout;
		return instance;
	}

	/*
	 *  (non-Javadoc)
	 * @see j.core.cache.JCacheFilter#matches(java.lang.Object)
	 */
	public boolean matches(Object object) {
		if(object==null) return true;
		
		SSOSession obj=(SSOSession)object;
		return obj.isAccessTokenExpired(this.sessionTimeout) && obj.isRefreshTokenExpired(this.sessionTimeout);
	}
}
