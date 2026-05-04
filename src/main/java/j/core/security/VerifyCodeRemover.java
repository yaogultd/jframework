package j.core.security;

import j.core.cache.JCacheFilter;

/**
 * 
 * @author 肖炯
 *
 */
public class VerifyCodeRemover implements JCacheFilter{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public VerifyCodeRemover(){
	}

	@Override
	public boolean matches(Object object) {
		if(object==null||!(object instanceof VerifyCodeBean)) return true;
		
		VerifyCodeBean o=(VerifyCodeBean)object;
		if(o.removable() && o.isTimeout()) return true;
		
		return false;
	}
}
