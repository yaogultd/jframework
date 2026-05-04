package j.core.cache;

import j.core.cache.storage.Storage;
import j.core.type.index.IndexCreator;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;

import java.util.Map;

/**
 * 基于缓存实现的Map——实际开发中，请通过此类来使用缓存服务
 * @author 肖炯
 *
 */
public class SerializedMap<T> {
	private String cacheId=null;
	private Storage storage=null;//缓存单元

	/**
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @throws Exception
	 */
	public SerializedMap(String cacheId) throws Exception{
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}
		storage = JCache.getStorage(this.cacheId);
		storage.createUnit(cacheId, JCache.UNIT_MAP, JCache.LIFECIRCLE_DURABLE, 0);
	}

	/**
	 *
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 * @throws Exception
	 */
	public SerializedMap(String cacheId, long timeout) throws Exception{
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}
		storage = JCache.getStorage(this.cacheId);
		storage.createUnit(cacheId, JCache.UNIT_MAP, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout);
	}

	/**
	 *
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 * @param initializer 缓存单元初始化类
	 * @throws Exception
	 */
	public SerializedMap(String cacheId, long timeout, JCacheInitializer initializer) throws Exception{
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}
		storage = JCache.getStorage(this.cacheId);
		storage.createUnit(cacheId, JCache.UNIT_MAP, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout, initializer);
	}

	/**
	 *
	 * @param indexCreator
	 * @throws Exception
	 */
	public void setIndexCreator(IndexCreator indexCreator) throws Exception{
		storage.setIndexCreator(this.cacheId, indexCreator);
	}

	/**
	 * 兼容Map类的put方法
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void put(Object key, T value) throws Exception {
		this.addOne(key,value);
	}

	/**
	 * 添加一个key-value到缓存单元
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void addOne(Object key, T value) throws Exception {
		storage.addOne(this.cacheId,key,value);
	}

	/**
	 * 添加一组key-value到缓存单元
	 * @param mappings key-value集合
	 * @throws Exception
	 */
	public void addAll(Map<Object,T> mappings) throws Exception {
		storage.addAll(this.cacheId,mappings);
	}

	/**
	 * 缓存单元中是否包含符合缓存操作参数的key-value
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public boolean contains(JCacheParams params) throws Exception {
		return storage.contains(this.cacheId,params);
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int size() throws Exception {
		return storage.size(this.cacheId);
	}
	
	/**
	 * 
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public int size(JCacheParams params) throws Exception {
		return storage.size(this.cacheId,params);
	}
	
	/**
	 * 
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public int[] sizes(JCacheParams[] params) throws Exception {
		return storage.sizes(this.cacheId,params);
	}

	/**
	 * 兼容常见的通过Map中通过string类型的key获取value的方法
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public T get(String key) throws Exception {
		return (T)storage.get(this.cacheId, new JCacheParams(key));
	}

	/**
	 * 
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public Object get(JCacheParams params) throws Exception {
		return storage.get(this.cacheId,params);
	}

	/**
	 * 兼容常见的通过Map中移除string类型的key的方法
	 * @param key
	 * @throws Exception
	 */
	public void remove(String key) throws Exception {
		this.remove(new JCacheParams(key));
	}

	/**
	 * 
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void remove(JCacheParams params) throws Exception {
		storage.remove(this.cacheId,params);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void clear() throws Exception {
		storage.clear(this.cacheId);
	}

	/**
	 * 
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void update(JCacheParams params) throws Exception {
		storage.update(this.cacheId,params);
	}

	/**
	 * 
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void updateCollection(JCacheParams params) throws Exception {
		storage.updateCollection(this.cacheId,params);
	}

	/**
	 * 返回符合缓存操作参数的对象集合
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public ConcurrentMap sub(JCacheParams params) throws Exception {
		return (ConcurrentMap) storage.sub(this.cacheId,params);
	}

	/**
	 * 返回符合缓存操作参数的子集的key的list
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public ConcurrentList keys(JCacheParams params) throws Exception {
		return storage.keys(this.cacheId,params);
	}

	/**
	 * 返回符合缓存操作参数的子集的value的list
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public ConcurrentList values(JCacheParams params) throws Exception {
		return storage.values(this.cacheId,params);
	}
}
