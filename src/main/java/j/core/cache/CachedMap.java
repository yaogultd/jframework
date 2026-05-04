package j.core.cache;

import j.core.cache.storage.Storage;
import j.core.cache.storage.local.StorageLocal;
import j.core.common.Global;
import j.core.nvwa.Nvwa;
import j.core.type.TimedObject;
import j.core.type.index.IndexCreator;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 基于缓存实现的Map——实际开发中，请通过此类来使用缓存服务
 * @author 肖炯
 *
 */
public class CachedMap{
	private static Logger log=Logger.create(CachedMap.class);

	private static ConcurrentMap<String, CachedMap> instances = new ConcurrentMap<>();

	@Getter
	private String cacheId=null;
	private Storage storage=null;//缓存单元
	private boolean initialized=false;//是否已经初始化
	private int tries=1;//如出错，最多尝试次数

	private boolean local=true;//是否本地缓存

	@Setter
	private boolean localCacheEnabled=false;//是否启用本地缓存

	@Setter
	private long localCacheTimeout=3600000L;//本地缓存失效时间（默认1小时）

	private ConcurrentMap<Object, TimedObject> localCached = new ConcurrentMap<>();//本地缓存

	public void setTries(int tries){
		if(tries > 0) this.tries=tries;
	}

	/**
	 *
	 * @param cacheId
	 * @return
	 */
	synchronized public static CachedMap getInstance(String cacheId){
		if(instances.containsKey(cacheId)) return instances.get(cacheId);
		return new CachedMap(cacheId);
	}

	/**
	 *
	 * @param cacheId
	 * @param timeout
	 * @return
	 */
	synchronized public static CachedMap getInstance(String cacheId, long timeout){
		if(instances.containsKey(cacheId)) return instances.get(cacheId);
		return new CachedMap(cacheId, timeout);
	}

	/**
	 *
	 * @param cacheId
	 * @param timeout
	 * @param initializer
	 * @return
	 */
	synchronized public static CachedMap getInstance(String cacheId, long timeout, JCacheInitializer initializer){
		if(instances.containsKey(cacheId)) return instances.get(cacheId);
		return new CachedMap(cacheId, timeout, initializer);
	}
	
	/**
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 */
	public CachedMap(String cacheId){
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
					storage.createUnit(cacheId, JCache.UNIT_MAP, JCache.LIFECIRCLE_DURABLE, 0);
					setInitialized();
					break;
				}catch (Exception e){
					log.log("初始化缓存单元出错（1秒后重试）："+ e.getMessage(), Logger.LEVEL_ERROR);
					if(Nvwa.isDebug()) log.log(e, Logger.LEVEL_ERROR);
					Global.sleep1000Millis();
				}
			}
		})).start();

		instances.put(cacheId, this);
	}

	/**
	 *
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 */
	public CachedMap(String cacheId, long timeout){
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
					storage.createUnit(cacheId, JCache.UNIT_MAP, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout);
					setInitialized();
					break;
				}catch (Exception e){
					log.log("初始化缓存单元出错（1秒后重试）："+ e.getMessage(), Logger.LEVEL_ERROR);
					if(Nvwa.isDebug()) log.log(e, Logger.LEVEL_ERROR);
					Global.sleep1000Millis();
				}
			}
		})).start();

		instances.put(cacheId, this);
	}

	/**
	 *
	 * 创建JCache.UNIT_MAP类型的缓存单元
	 * @param cacheId 缓存单元ID
	 * @param timeout 临时缓存超时时间
	 * @param initializer 缓存单元初始化类
	 */
	public CachedMap(String cacheId, long timeout, JCacheInitializer initializer){
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
					storage.createUnit(cacheId, JCache.UNIT_MAP, timeout>0?JCache.LIFECIRCLE_TEMPORARY:JCache.LIFECIRCLE_DURABLE, timeout, initializer);
					setInitialized();
					break;
				}catch (Exception e){
					log.log("初始化缓存单元出错（1秒后重试）："+ e.getMessage(), Logger.LEVEL_ERROR);
					if(Nvwa.isDebug()) log.log(e, Logger.LEVEL_ERROR);
					Global.sleep1000Millis();
				}
			}
		})).start();

		instances.put(cacheId, this);
	}

	synchronized private void setInitialized(){
		this.initialized=true;
		this.local=(this.storage instanceof StorageLocal);
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
	 *
	 * @param indexCreator
	 * @throws Exception
	 */
	public void setIndexCreator(IndexCreator indexCreator) throws Exception{
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.setIndexCreator(this.cacheId, indexCreator);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 兼容Map类的put方法
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void put(Object key, Object value) throws Exception {
		this.addOne(key,value);
	}

	/**
	 * 添加一个key-value到缓存单元
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void addOne(Object key, Object value) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.addOne(this.cacheId,key,value);

				//缓存到本地
				if(value != null
						&& this.localCacheEnabled
						&& !this.local){
					long now=System.currentTimeMillis();
					this.localCached.put(key, new TimedObject(value, now, now, this.localCacheTimeout, this.localCacheTimeout));
				}
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 添加一组key-value到缓存单元
	 * @param mappings key-value集合
	 * @throws Exception
	 */
	public void addAll(Map<Object,Object> mappings) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				storage.addAll(this.cacheId,mappings);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		if(ex != null) throw ex;
	}

	/**
	 * 缓存单元中是否包含符合缓存操作参数的key-value
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
	 * 
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
	 * 
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
	 * @param params 缓存操作参数
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
	 * 兼容常见的通过Map中通过string类型的key获取value的方法
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Object get(String key) throws Exception {
		return this.get(key, true);
	}

	/**
	 * 兼容常见的通过Map中通过string类型的key获取value的方法
	 * @param key
	 * @param fromLocalCache 是否可从本地缓存获取
	 * @return
	 * @throws Exception
	 */
	public Object get(String key, boolean fromLocalCache) throws Exception {
		return this.get(new JCacheParams(key), fromLocalCache);
	}

	/**
	 *
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Object get(JCacheParams params) throws Exception {
		return this.get(params, true);
	}

	/**
	 *
	 * @param params
	 * @param fromLocalCache 是否可从本地缓存获取
	 * @return
	 * @throws Exception
	 */
	public Object get(JCacheParams params, boolean fromLocalCache) throws Exception {
		waitForInitialized();

		//从本地缓存取得
		if(fromLocalCache
				&& this.localCacheEnabled
				&& !this.local
				&& params != null
				&& params.isExactKey()
				&& this.localCached.containsKey(params.key)){
			TimedObject timedObject = this.localCached.get(params.key);
			if(!timedObject.isTimeout()) return timedObject.getObject();
			else this.localCached.remove(params.key);
		}

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				Object value = storage.get(this.cacheId,params);

				//缓存到本地
				if(value != null
						&& this.localCacheEnabled
						&& !this.local
						&& params != null
						&& params.isExactKey()){
					long now=System.currentTimeMillis();
					this.localCached.put(params.key, new TimedObject(value, now, now, this.localCacheTimeout, this.localCacheTimeout));
				}
				return value;
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
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
		waitForInitialized();

		if(params != null
				&& params.isExactKey()
				&& this.localCached.containsKey(params.key)){
			this.localCached.remove(params.key);
		}

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
	 * 
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
	 * 
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
	 * 
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
	public ConcurrentMap sub(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return (ConcurrentMap) storage.sub(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}

	/**
	 * 返回符合缓存操作参数的子集的key的list
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public ConcurrentList keys(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.keys(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}

	/**
	 * 返回符合缓存操作参数的子集的value的list
	 * @param params 缓存操作参数
	 * @return
	 * @throws Exception
	 */
	public ConcurrentList values(JCacheParams params) throws Exception {
		waitForInitialized();

		int times = 0;
		Exception ex = null;
		while(times < this.tries){
			times++;
			try{
				return storage.values(this.cacheId,params);
			}catch(Exception e){
				ex = e;
				if(times == this.tries) throw e;
				Global.sleep100Millis();
			}
		}
		throw ex;
	}
}
