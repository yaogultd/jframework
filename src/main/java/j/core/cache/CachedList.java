package j.core.cache;

import j.core.cache.storage.Storage;
import j.core.common.Global;
import j.core.nvwa.Nvwa;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import lombok.Getter;

import java.util.Collection;

/**
 * 基于缓存实现的List——实际开发中，请通过此类来使用缓存服务
 * @author 肖炯
 *
 */
public class CachedList{
	private static Logger log=Logger.create(CachedList.class);

	private static ConcurrentMap<String, CachedList> instances = new ConcurrentMap<>();

	@Getter
	private String cacheId=null;//缓存单元ID
	private Storage storage=null;//缓存单元
	private boolean initialized=false;//是否已经初始化
	private int tries=1;//如出错，最多尝试次数

	/**
	 *
	 * @param cacheId
	 * @return
	 */
	synchronized public static CachedList getInstance(String cacheId){
		if(instances.containsKey(cacheId)) return instances.get(cacheId);
		return new CachedList(cacheId);
	}

	/**
	 *
	 * @param cacheId
	 * @param timeout
	 * @return
	 */
	synchronized public static CachedList getInstance(String cacheId, long timeout){
		if(instances.containsKey(cacheId)) return instances.get(cacheId);
		return new CachedList(cacheId, timeout);
	}

	/**
	 *
	 * @param cacheId
	 * @param timeout
	 * @param initializer
	 * @return
	 */
	synchronized public static CachedList getInstance(String cacheId, long timeout, JCacheInitializer initializer){
		if(instances.containsKey(cacheId)) return instances.get(cacheId);
		return new CachedList(cacheId, timeout, initializer);
	}

	public void setTries(int tries){
		if(tries > 0) this.tries=tries;
	}
	
	/**
	 * 创建JCache.UNIT_LIST类型的缓存单元
	 * @param cacheId 缓存单元ID
	 */
	public CachedList(String cacheId){
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();;
		}else{
			this.cacheId=cacheId;
		}

		//异步初始化
		(new Thread(() -> {
			while(true){
				try{
					storage = JCache.getStorage(cacheId);
					storage.createUnit(cacheId, JCache.UNIT_LIST, JCache.LIFECIRCLE_DURABLE, 0);

					setInitialized();
					break;
				}catch (Exception e){
					log.log("初始化缓存单元出错（1秒后重试）："+ e.getMessage(), Logger.LEVEL_ERROR);
					if(Nvwa.isDebug()) log.log(e, Logger.LEVEL_ERROR);
					Global.sleep1000Millis();
				}
			}
		})).start();
	}

	/**
	 *
	 * 创建JCache.UNIT_LIST类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 */
	public CachedList(String cacheId, long timeout){
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}

		//异步初始化
		(new Thread(() -> {
			while(true){
				try{
					storage =JCache.getStorage(cacheId);
					storage.createUnit(cacheId, JCache.UNIT_LIST, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout);
					setInitialized();
					break;
				}catch (Exception e){
					log.log("初始化缓存单元出错（1秒后重试）："+ e.getMessage(), Logger.LEVEL_ERROR);
					if(Nvwa.isDebug()) log.log(e, Logger.LEVEL_ERROR);
					Global.sleep1000Millis();
				}
			}
		})).start();
	}

	/**
	 *
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 * @param initializer 缓存单元初始化类
	 */
	public CachedList(String cacheId, long timeout, JCacheInitializer initializer){
		if(cacheId==null){
			this.cacheId=""+(new Object()).hashCode();
		}else{
			this.cacheId=cacheId;
		}

		//异步初始化
		(new Thread(() -> {
			while(true){
				try{
					storage = JCache.getStorage(cacheId);
					storage.createUnit(cacheId, JCache.UNIT_LIST, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout, initializer);
					setInitialized();
					break;
				}catch (Exception e){
					log.log("初始化缓存单元出错（1秒后重试）："+ e.getMessage(), Logger.LEVEL_ERROR);
					if(Nvwa.isDebug()) log.log(e, Logger.LEVEL_ERROR);
					Global.sleep1000Millis();
				}
			}
		})).start();
	}

	synchronized private void setInitialized(){
		this.initialized=true;
	}

	synchronized private boolean isInitialized(){
		return this.initialized;
	}

	/**
	 * 等待初始化
	 */
	private void waitForInitialized(){
		while(!this.isInitialized()) Global.sleep1000Millis();
	}

	/**
	 * 添加一个对象到缓存单元
	 * @param value
	 * @throws Exception
	 */
	public void addOne(Object value) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.addOne(this.cacheId,value);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 添加一个对象集合到缓存单元
	 * @param values
	 * @throws Exception
	 */
	public void addAll(Collection<Object> values) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.addAll(this.cacheId,values);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 缓存单元是否存在符合缓存操作参数的对象
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public boolean contains(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.contains(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}
	
	/**
	 * 缓存单元中全部对象的数量
	 * @return
	 * @throws Exception
	 */
	public int size() throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.size(this.cacheId);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}
	
	/**
	 * 缓存单元中符合缓存操作参数的对象的数量
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public int size(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.size(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int[] sizes(JCacheParams[] params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.sizes(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}

	/**
	 * 获得符合缓存操作参数的对象，如有多个符合，返回索引位置最靠前的那个
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public Object get(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.get(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}

	/**
	 * 移除符合缓存操作参数的对象
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void remove(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.remove(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 清空缓存单元
	 * @throws Exception
	 */
	public void clear() throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.clear(this.cacheId);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 调用缓存操作参数中指定的更新器对缓存单元中特定对象进行更新，更新规则由更新器定义
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void update(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.update(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 调用缓存操作参数中指定的更新器对缓存单元中特定对象集合进行更新，更新规则由更新器定义
	 * @param params 缓存操作参数
	 * @throws Exception
	 */
	public void updateCollection(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.updateCollection(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 返回符合缓存操作参数的对象集合
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public ConcurrentList<Object> sub(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return (ConcurrentList) storage.sub(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}
}
