package j.core.cache;

import j.core.cache.storage.Storage;
import j.util.ConcurrentList;

import java.util.Collection;

/**
 * 基于缓存实现的List——实际开发中，请通过此类来使用缓存服务
 * @author 肖炯
 *
 */
public class SerializedList {
	private String cacheId=null;//缓存单元ID
	private Storage storage =null;//缓存单元

	/**
	 * 创建JCache.UNIT_LIST类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @throws Exception
	 */
	public SerializedList(String cacheId) throws Exception{
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();;
		}else{
			this.cacheId=cacheId;
		}
		storage = JCache.getStorage(this.cacheId);
		storage.createUnit(cacheId, JCache.UNIT_LIST, JCache.LIFECIRCLE_DURABLE, 0);
	}

	/**
	 *
	 * 创建JCache.UNIT_LIST类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 * @throws Exception
	 */
	public SerializedList(String cacheId, long timeout) throws Exception{
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}
		storage =JCache.getStorage(this.cacheId);
		storage.createUnit(cacheId, JCache.UNIT_LIST, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout);
	}

	/**
	 *
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 * @param initializer 缓存单元初始化类
	 * @throws Exception
	 */
	public SerializedList(String cacheId, long timeout, JCacheInitializer initializer) throws Exception{
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}
		storage = JCache.getStorage(this.cacheId);
		storage.createUnit(cacheId, JCache.UNIT_LIST, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout, initializer);
	}

	/**
	 * 添加一个对象到缓存单元
	 * @param value
	 * @throws Exception
	 */
	public void addOne(Object value) throws Exception {
		storage.addOne(this.cacheId,value);
	}

	/**
	 * 添加一个对象集合到缓存单元
	 * @param values
	 * @throws Exception
	 */
	public void addAll(Collection<Object> values) throws Exception {
		storage.addAll(this.cacheId,values);
	}

	/**
	 * 缓存单元是否存在符合缓存操作参数的对象
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public boolean contains(JCacheParams params) throws Exception {
		return storage.contains(this.cacheId,params);
	}
	
	/**
	 * 缓存单元中全部对象的数量
	 * @return
	 * @throws Exception
	 */
	public int size() throws Exception {
		return storage.size(this.cacheId);
	}
	
	/**
	 * 缓存单元中符合缓存操作参数的对象的数量
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public int size(JCacheParams params) throws Exception {
		return storage.size(this.cacheId,params);
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int[] sizes(JCacheParams[] params) throws Exception {
		return storage.sizes(this.cacheId,params);
	}

	/**
	 * 获得符合缓存操作参数的对象，如有多个符合，返回索引位置最靠前的那个
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public Object get(JCacheParams params) throws Exception {
		return storage.get(this.cacheId,params);
	}

	/**
	 * 移除符合缓存操作参数的对象
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void remove(JCacheParams params) throws Exception {
		storage.remove(this.cacheId,params);
	}

	/**
	 * 清空缓存单元
	 * @throws Exception
	 */
	public void clear() throws Exception {
		storage.clear(this.cacheId);
	}

	/**
	 * 调用缓存操作参数中指定的更新器对缓存单元中特定对象进行更新，更新规则由更新器定义
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void update(JCacheParams params) throws Exception {
		storage.update(this.cacheId,params);
	}

	/**
	 * 调用缓存操作参数中指定的更新器对缓存单元中特定对象集合进行更新，更新规则由更新器定义
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
	public ConcurrentList<Object> sub(JCacheParams params) throws Exception {
		return (ConcurrentList) storage.sub(this.cacheId,params);
	}
}
