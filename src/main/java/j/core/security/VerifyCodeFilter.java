package j.core.security;

import j.core.cache.JCacheFilter;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author 肖炯
 *
 */
@Setter
@Getter
public class VerifyCodeFilter implements JCacheFilter{
	private static final long serialVersionUID = 1L;
	private String uuid;
	private String related;
	private String ip;

	/**
	 *
	 */
	public VerifyCodeFilter(String uuid){
		this.uuid=uuid;
	}

	/*
	 * (non-Javadoc)
	 * @see j.core.cache.JCacheFilter#matches(java.lang.Object)
	 */
	public boolean matches(Object object) {
		if(object==null||!(object instanceof VerifyCodeBean)) return false;
		
		VerifyCodeBean o=(VerifyCodeBean)object;
		if(!JUtilString.isBlank(uuid) && !uuid.equals(o.getUuid())) return false;
		if(!JUtilString.isBlank(related) && !related.equals(o.getRelated())) return false;
		if(!JUtilString.isBlank(ip) && !ip.equals(o.getIp())) return false;
		return true;
	}
}
