package j.core.cache;

import j.core.cache.storage.Storage;
import j.core.cache.storage.StoragePolicy;
import j.util.JUtilString;
import lombok.Getter;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
public class JCacheMapping {
	private String selector;
	private String storagePolicy;

	/**
	 *
	 * @param selector
	 * @param storagePolicy
	 */
	protected JCacheMapping(String selector, String storagePolicy){
		this.selector=selector;
		this.storagePolicy=storagePolicy;
	}

	/**
	 *
	 * @param cacheId
	 * @return
	 */
	public boolean matchesExactly(String cacheId){
		return cacheId.equals(selector);
	}

	/**
	 *
	 * @param cacheId
	 * @return
	 */
	public boolean matchesWildcard(String cacheId){
		return JUtilString.match(cacheId, selector, "*") > -1 ? true : false;
	}



	/**
	 * 
	 * @param cacheId
	 * @return
	 */
	public boolean matches(String cacheId){
		return cacheId.matches(selector);
	}

	/**
	 *
	 * @return
	 */
	synchronized public Storage getStorage(){
		//存储策略
		StoragePolicy policy = JCache.getStoragePolicy(this.getStoragePolicy());
		return policy==null ? null : policy.getStorage();
	}

	@Override
	public String toString(){
		StringBuffer s=new StringBuffer();
		s.append("{");
		s.append("\"selector\":\""+this.selector+"\"");
		s.append(",\"storagePolicy\":\""+this.storagePolicy+"\"");
		s.append("}");
		return s.toString();
	}
}
