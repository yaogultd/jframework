package j.core.cache.storage;

import j.core.annotation.description.ClassDescription;
import j.core.cache.CachedList;
import j.core.cache.CachedMap;
import j.core.cache.JCacheInitializer;
import j.core.cache.JCacheParams;
import j.core.type.index.IndexCreator;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import j.util.JUtilUUID;
import org.dom4j.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@ClassDescription(author = "肖炯", date = "2022/04/19", description = "文件存储处理器")
public abstract class Storage implements Cloneable{
    private static Logger log=Logger.create(Storage.class);

    //自定义参数
    private ConcurrentMap<String, String> properties=new ConcurrentMap<>();

    //
    protected String storageUuid;

    /**
     *
     * @param propertyElements
     */
    public Storage(List<Element> propertyElements){
        this.storageUuid = JUtilUUID.genUUID();
        for(int i=0; propertyElements!=null && i<propertyElements.size(); i++){
            String name=propertyElements.get(i).attributeValue("name");
            String desc=propertyElements.get(i).attributeValue("desc");
            String value=propertyElements.get(i).getTextTrim();

            properties.put(name, value);
        }
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, String value){
        properties.put(name, value);
    }

    /**
     *
     * @param name
     * @return
     */
    public String getProperty(String name){
        return JUtilString.isBlank(name)?null:properties.get(name);
    }

    /**
     *
     * @param cacheId
     * @param indexCreator
     * @throws Exception
     */
    public abstract void setIndexCreator(String cacheId, IndexCreator indexCreator) throws Exception;

    /**
     * 创建缓存单元
     * @param cacheId  缓存单元ID，不管在哪个模块、哪个应用，同一个缓存单元ID总会指向同一个缓存单元
     * @param unitType 缓存单元类型（Map or List）
     * @param lifeCircleType 缓存单元生命周期类型
     * @param timeout 临时缓存超时时间
     * @throws Exception
     */
    public abstract void createUnit(String cacheId, int unitType, int lifeCircleType, long timeout) throws Exception;

    /**
     * 创建缓存单元
     * @param cacheId  缓存单元ID，不管在哪个模块、哪个应用，同一个缓存单元ID总会指向同一个缓存单元
     * @param unitType 缓存单元类型（Map or List）
     * @param lifeCircleType 缓存单元生命周期类型
     * @param timeout 临时缓存超时时间
     * @param initializer 自定义初始化类
     * @throws Exception
     */
    public abstract void createUnit(String cacheId, int unitType, int lifeCircleType, long timeout, JCacheInitializer initializer) throws Exception;

    /**
     * 添加一个key-value到缓存单元，仅当缓存单元类型为Map时有效
     * @param cacheId 缓存单元ID
     * @param key
     * @param value
     * @throws Exception
     */
    public abstract void addOne(String cacheId,Object key,Object value) throws Exception;

    /**
     * 将Map中的所有key-value全部添加到缓存单元，仅当缓存单元类型为Map时有效
     * @param cacheId 缓存单元ID
     * @param mappings 包含需要添加的key-value
     * @throws Exception
     */
    public abstract void addAll(String cacheId, Map mappings) throws Exception;


    /**
     * 添加一个对象到缓存单元，仅当缓存单元类型为List时有效
     * @param cacheId 缓存单元ID
     * @param value 需添加的对象
     * @throws Exception
     */
    public abstract void addOne(String cacheId,Object value) throws Exception;

    /**
     * 添加一个对象到缓存单元（仅当这个对象在缓存单元中不存在时才添加），仅当缓存单元类型为List时有效
     * @param cacheId 缓存单元ID
     * @param value 需添加的对象
     * @throws Exception
     */
    public abstract void addOneIfNotContains(String cacheId, Object value) throws Exception;


    /**
     * 添加集合中的全部对象到缓存单元，仅当缓存单元类型为List时有效
     * @param cacheId 缓存单元ID
     * @param values 需添加的对象集合
     * @throws Exception
     */
    public abstract void addAll(String cacheId, Collection values) throws Exception;

    /**
     * 是否包含符合缓存操作参数的对象
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @return
     * @throws Exception
     */
    public abstract boolean contains(String cacheId, JCacheParams jdcParams) throws Exception;

    /**
     * 缓存单元中对象的数量
     * @param cacheId 缓存单元ID
     * @return
     * @throws Exception
     */
    public abstract int size(String cacheId) throws Exception;

    /**
     * 缓存单元中符合操作参数的对象数量
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @return
     * @throws Exception
     */
    public abstract int size(String cacheId,JCacheParams jdcParams) throws Exception;

    /**
     * 缓存单元中符合各个操作参数的对象数量，返回结果数与指定的多个缓存操作参数对应
     * @param cacheId 缓存单元ID
     * @param jdcParams 多个缓存操作参数
     * @return
     * @throws Exception
     */
    public abstract int[] sizes(String cacheId,JCacheParams[] jdcParams) throws Exception;

    /**
     * 获得符合缓存操作参数的对象，如有多个符合则返回第一个
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @return
     * @throws Exception
     */
    public abstract Object get(String cacheId,JCacheParams jdcParams) throws Exception;

    /**
     * 将符合缓存操作参数的对象从缓存单元中移除
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @throws Exception
     */
    public abstract void remove(String cacheId,JCacheParams jdcParams) throws Exception;

    /**
     * 清空缓存单元
     * @param cacheId 缓存单元ID
     * @throws Exception
     */
    public abstract void clear(String cacheId) throws Exception;

    /**
     * 调用缓存操作参数中指定的更新器对缓存单元中特定对象进行更新，更新哪些对象以及怎样更新由指定的更新器来决定
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @throws Exception
     */
    public abstract void update(String cacheId,JCacheParams jdcParams) throws Exception;

    /**
     * 调用缓存操作参数中指定的集合更新器对缓存单元中特定对象集合进行更新，更新哪些对象以及怎样更新由指定的更新器来决定
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @throws Exception
     */
    public abstract void updateCollection(String cacheId,JCacheParams jdcParams) throws Exception;

    /**
     * 返回符合查询的子集
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @return Map类缓存单元应该返回Map，List类型缓存单元应该返回List
     * @throws Exception
     */
    public abstract Object sub(String cacheId,JCacheParams jdcParams) throws Exception;

    /**
     * 返回符合缓存操作参数的子集的key的List，仅当缓存单元类型为Map时有效
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @return
     * @throws Exception
     */
    public abstract ConcurrentList keys(String cacheId, JCacheParams jdcParams) throws Exception;

    /**
     * 返回符合缓存操作参数的子集的value的List，仅当缓存单元类型为Map时有效
     * @param cacheId 缓存单元ID
     * @param jdcParams 缓存操作参数
     * @return
     * @throws Exception
     */
    public abstract ConcurrentList values(String cacheId,JCacheParams jdcParams) throws Exception;
}
