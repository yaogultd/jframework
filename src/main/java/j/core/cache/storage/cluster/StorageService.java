package j.core.cache.storage.cluster;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.service.Service;
import j.core.cache.*;
import j.core.nvwa.Nvwa;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.core.type.index.IndexCreator;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import j.util.JUtilUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ClassDescription(author = "肖炯",
        date = "2022/06/18",
        description = "",
        reviewers = {"肖炯"})
@Service(path = "/JCACHE-SERVICE")
public class StorageService extends ServiceBase implements Runnable{
    @FieldDescription(description = "日志输出")
    private static Logger log = Logger.create(StorageService.class);
    private String uuid;
    private ConcurrentMap<String, JCacheUnit> units=new ConcurrentMap<String,JCacheUnit>(false, new ConcurrentHashMap<>());//key-缓存单元ID，value-缓存单元
    private Thread monitor;

    public StorageService(){
        super();
        this.uuid=JUtilUUID.genUUID();
    }

    /**
     *
     * @param cacheId
     * @return
     * @throws Exception
     */
    synchronized private JCacheUnit checkStatus(String cacheId) throws Exception{
        if(JUtilString.isBlank(cacheId)){
            throw new Exception("the cache id is null.");
        }

        if(this.monitor==null){
            this.monitor=new Thread(this);
            this.monitor.start();
            log.log("j.core.cache.storage.cluster.StorageService monitor thread started.",-1);
        }

        JCacheUnit unit=units.get(cacheId);
        if(unit==null){
            throw new Exception("the cache unit("+cacheId+") of storage("+this.uuid+") is not exists!");
        }
        unit.using();
        return unit;
    }

    @Service(path = "createUnit")
    public ServiceResponse createUnit(String cacheId, int unitType, int lifeCircle, long timeout, JCacheInitializer initializer) throws Exception {
        try {
            if (units.containsKey(cacheId)) {
                JCacheUnit unit = units.get(cacheId);
                if ((unit instanceof JCacheUnitMap) && unitType != JCache.UNIT_MAP) {
                    return new ServiceResponse(false, "type_mismatch", "cache unit of id " + cacheId + " exists(UNIT_MAP), but the type mismatch.");
                }
                if ((unit instanceof JCacheUnitList) && unitType != JCache.UNIT_LIST) {
                    return new ServiceResponse(false, "type_mismatch", "cache unit of id " + cacheId + " exists(UNIT_LIST), but the type mismatch.");
                }
                return new ServiceResponse(true, "OK", "");
            }

            if (unitType == JCache.UNIT_MAP) {
                units.put(cacheId, new JCacheUnitMap(lifeCircle, timeout, initializer));
            } else if (unitType == JCache.UNIT_LIST) {
                units.put(cacheId, new JCacheUnitList(lifeCircle, timeout, initializer));
            }
            log.log("cache unit("+cacheId+") of storage service("+this.uuid+") created!", -1);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "setIndexCreator")
    public ServiceResponse setIndexCreator(String cacheId, IndexCreator indexCreator) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.setIndexCreator(indexCreator);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "addOne")
    public ServiceResponse addOne(String cacheId, Object key, Object value) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.addOne(key, value);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "addOneForList")
    public ServiceResponse addOneForList(String cacheId, Object value) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.addOne(value);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "addAll")
    public ServiceResponse addAll(String cacheId, Map mappings) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.addAll(mappings);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "addAllForList")
    public ServiceResponse addAllForList(String cacheId, Collection values) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.addAll(values);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "addOneIfNotContains")
    public ServiceResponse addOneIfNotContains(String cacheId, Object value) throws Exception{
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.addOneIfNotContains(value);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "contains")
    public ServiceResponse<Boolean> contains(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            return new ServiceResponse(true, "OK", "", Boolean.valueOf(unit.contains(jdcParams)));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "size")
    public ServiceResponse<Integer> size(String cacheId) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            return new ServiceResponse(true, "OK", "", Integer.valueOf(unit.size()));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "size")
    public ServiceResponse<Integer> size(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            return new ServiceResponse(true, "OK", "", Integer.valueOf(unit.size(jdcParams)));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "sizes")
    public ServiceResponse<Integer[]> sizes(String cacheId, JCacheParams[] jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            Integer[] sizes = new Integer[jdcParams.length];
            for (int i = 0; i < sizes.length; i++) sizes[i] = unit.size(jdcParams[i]);
            return new ServiceResponse(true, "OK", "", sizes);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "get")
    public ServiceResponse<Object> get(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            return new ServiceResponse(true, "OK", "", unit.get(jdcParams));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "remove")
    public ServiceResponse remove(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.remove(jdcParams);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "clear")
    public ServiceResponse clear(String cacheId) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.clear();
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "update")
    public ServiceResponse update(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.update(jdcParams);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "updateCollection")
    public ServiceResponse updateCollection(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            unit.updateCollection(jdcParams);
            return new ServiceResponse(true, "OK", "");
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "sub")
    public ServiceResponse<Object> sub(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            return new ServiceResponse(true, "OK", "", unit.sub(jdcParams));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "keys")
    public ServiceResponse<ConcurrentList> keys(String cacheId, JCacheParams jdcParams) throws Exception {
        try {
            JCacheUnit unit = checkStatus(cacheId);
            return new ServiceResponse(true, "OK", "", unit.keys(jdcParams));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
    }

    @Service(path = "values")
    public ServiceResponse<ConcurrentList> values(String cacheId, JCacheParams jdcParams) throws Exception {
        try{
            JCacheUnit unit=checkStatus(cacheId);
            if(Nvwa.isDebug() && jdcParams!=null){
                jdcParams.cacheId = cacheId;
            }
            return new ServiceResponse(true, "OK", "", unit.values(jdcParams));
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse(false, "ERR", "", null);
        }
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
