package j.core.cache.storage.local;

import j.core.Startup;
import j.core.cache.*;
import j.core.cache.storage.Storage;
import j.core.type.index.IndexCreator;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.dom4j.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StorageLocal extends Storage implements Runnable {
    private static Logger log=Logger.create(StorageLocal.class);
    protected ConcurrentMap<String, JCacheUnit> units=new ConcurrentMap<String,JCacheUnit>(false, new ConcurrentHashMap<>());//key-缓存单元ID，value-缓存单元


    /**
     *
     * @param propertyElements
     */
    public StorageLocal(List<Element> propertyElements) {
        super(propertyElements);

        Thread thread=new Thread(this);
        thread.start();
        log.log("j.core.cache.storage.local.StorageLocal monitor thread started.",-1);
    }

    /**
     *
     * @param cacheId
     * @return
     * @throws Exception
     */
    private JCacheUnit checkStatus(String cacheId) throws Exception{
        if(JUtilString.isBlank(cacheId)){
            throw new Exception("the cache id is null.");
        }

        JCacheUnit unit=units.get(cacheId);
        if(unit==null){
            throw new Exception("the cache unit of id "+cacheId+" is not exists.");
        }
        unit.using();
        return unit;
    }

    @Override
    public void createUnit(String cacheId, int unitType, int lifeCircle, long timeout) throws Exception {
        this.createUnit(cacheId, unitType, lifeCircle, timeout, null);
    }

    @Override
    public void createUnit(String cacheId, int unitType, int lifeCircle, long timeout, JCacheInitializer initializer) throws Exception {
        if(units.containsKey(cacheId)){
            JCacheUnit unit=units.get(cacheId);
            if((unit instanceof JCacheUnitMap) && unitType!=JCache.UNIT_MAP){
                throw new Exception("cache unit of id "+cacheId+" exists(UNIT_MAP), but the type mismatch.");
            }
            if((unit instanceof JCacheUnitList) && unitType!=JCache.UNIT_LIST){
                throw new Exception("cache unit of id "+cacheId+" exists(UNIT_LIST), but the type mismatch.");
            }
            return;
        }

        if(unitType==JCache.UNIT_MAP){
            units.put(cacheId,new JCacheUnitMap(lifeCircle, timeout, initializer));
        }else if(unitType== JCache.UNIT_LIST){
            units.put(cacheId,new JCacheUnitList(lifeCircle, timeout, initializer));
        }
    }

    @Override
    public void setIndexCreator(String cacheId, IndexCreator indexCreator) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.setIndexCreator(indexCreator);
    }

    @Override
    public void addOne(String cacheId, Object key, Object value) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.addOne(key,value);
    }

    @Override
    public void addAll(String cacheId, Map mappings) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.addAll(mappings);
    }

    @Override
    public void addOne(String cacheId, Object value) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.addOne(value);
    }

    @Override
    public void addAll(String cacheId, Collection values) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.addAll(values);
    }

    @Override
    public void addOneIfNotContains(String cacheId, Object value) throws Exception{
        JCacheUnit unit=checkStatus(cacheId);
        unit.addOneIfNotContains(value);
    }

    @Override
    public boolean contains(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.contains(jdcParams);
    }

    @Override
    public int size(String cacheId) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.size();
    }

    @Override
    public int size(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.size(jdcParams);
    }

    @Override
    public int[] sizes(String cacheId, JCacheParams[] jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        int[] sizes=new int[jdcParams.length];
        for(int i=0;i<sizes.length;i++) sizes[i]=unit.size(jdcParams[i]);
        return sizes;
    }

    @Override
    public Object get(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.get(jdcParams);
    }

    @Override
    public void remove(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.remove(jdcParams);
    }

    @Override
    public void clear(String cacheId) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.clear();
    }

    @Override
    public void update(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.update(jdcParams);
    }

    @Override
    public void updateCollection(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        unit.updateCollection(jdcParams);
    }

    @Override
    public Object sub(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.sub(jdcParams);
    }

    @Override
    public ConcurrentList keys(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.keys(jdcParams);
    }

    @Override
    public ConcurrentList values(String cacheId, JCacheParams jdcParams) throws Exception {
        JCacheUnit unit=checkStatus(cacheId);
        return unit.values(jdcParams);
    }

    @Override
    public void run(){
        while(!Startup.isDestroyed()){
            try{
                Thread.sleep(1000);
            }catch(Exception e){}

            //清除过期未使用的临时缓存单元
            try{
                List keys=units.listKeys();
                for(int i=0;i<keys.size();i++){
                    Object key=keys.get(i);
                    JCacheUnit unit=units.get(key);
                    if(unit.isTimeout()){
                        unit.clear();
                        units.remove(key);
                    }
                }
            }catch(Exception e){
                log.log(e,Logger.LEVEL_ERROR);
            }
        }
    }
}