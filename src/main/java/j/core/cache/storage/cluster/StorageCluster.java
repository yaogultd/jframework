package j.core.cache.storage.cluster;

import j.core.cache.JCacheInitializer;
import j.core.cache.JCacheParams;
import j.core.cache.storage.Storage;
import j.core.service.ServiceResponse;
import j.core.service.exception.AllNodesFailedException;
import j.core.service.exception.NoResponseException;
import j.core.service.exception.SomeNodesFailedException;
import j.core.type.index.IndexCreator;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.JUtilBean;
import org.dom4j.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StorageCluster extends Storage{
    private static Logger log=Logger.create(StorageCluster.class);

    /**
     *
     * @param propertyElements
     */
    public StorageCluster(List<Element> propertyElements) {
        super(propertyElements);
    }

    /**
     *
     * @param responses
     */
    private void checkResponses(List<ServiceResponse> responses) throws Exception {
        if(responses==null || responses.isEmpty()) throw new NoResponseException();

        int ok=0;
        for(int i=0; i<responses.size(); i++) if(responses.get(i).getSuccess()) ok++;
        if(ok==0){
            throw new AllNodesFailedException(JUtilBean.beans2Json(responses));
        }
        if(ok<responses.size()) throw new SomeNodesFailedException(JUtilBean.beans2Json(responses));
    }


    @Override
    public void createUnit(String cacheId, int unitType, int lifeCircle, long timeout) throws Exception {
        this.createUnit(cacheId, unitType, lifeCircle, timeout, null);
    }

    @Override
    public void createUnit(String cacheId, int unitType, int lifeCircle, long timeout, JCacheInitializer initializer) throws Exception {
        StorageService service = (StorageService)j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "createUnit",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, unitType, lifeCircle, timeout, initializer});

       this.checkResponses(responses);
    }

    @Override
    public void setIndexCreator(String cacheId, IndexCreator indexCreator) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "setIndexCreator",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, indexCreator});

        this.checkResponses(responses);
    }

    @Override
    public void addOne(String cacheId, Object key, Object value) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null){
            log.log("service for cache storage cluster is null!", -1);
            return;
        }

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "addOne",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, key, value});

        this.checkResponses(responses);
    }

    @Override
    public void addAll(String cacheId, Map mappings) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "addAll",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, mappings});

        this.checkResponses(responses);
    }

    @Override
    public void addOne(String cacheId, Object value) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "addOneForList",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, value});

        this.checkResponses(responses);
    }

    @Override
    public void addAll(String cacheId, Collection values) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "addAllForList",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, values});

        this.checkResponses(responses);
    }

    @Override
    public void addOneIfNotContains(String cacheId, Object value) throws Exception{
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "addOneIfNotContains",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, value});

        this.checkResponses(responses);
    }

    @Override
    public boolean contains(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return false;

        ServiceResponse<Boolean> response=service.contains(cacheId, jdcParams);
        return response==null || response.getResponse()==null ? false : response.getResponse();
    }

    @Override
    public int size(String cacheId) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return 0;

        ServiceResponse<Integer> response=service.size(cacheId);
        return response==null || response.getResponse()==null ? 0 : response.getResponse();
    }

    @Override
    public int size(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return 0;

        ServiceResponse<Integer> response=service.size(cacheId, jdcParams);
        return response==null || response.getResponse()==null ? 0 : response.getResponse();
    }

    @Override
    public int[] sizes(String cacheId, JCacheParams[] jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return null;

        ServiceResponse<Integer[]> response=service.sizes(cacheId, jdcParams);
        if(response==null || response.getResponse()==null) return null;

        Integer[] iSizes=response.getResponse();
        int[] _sizes=new int[]{iSizes.length};
        for(int i=0; i<iSizes.length; i++) _sizes[i]=iSizes[i];
        return _sizes;
    }

    @Override
    public Object get(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return null;

        ServiceResponse<Object> response=service.get(cacheId, jdcParams);
        return response==null ? null : response.getResponse();
    }

    @Override
    public void remove(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "remove",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, jdcParams});

        this.checkResponses(responses);
    }

    @Override
    public void clear(String cacheId) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "clear",
                null,
                null,
                null,
                null,
                new Object[]{cacheId});

        this.checkResponses(responses);
    }

    @Override
    public void update(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "update",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, jdcParams});

        this.checkResponses(responses);
    }

    @Override
    public void updateCollection(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return;

        List<ServiceResponse> responses=j.core.service.ServiceAdapter.callAll(false,
                false,
                service,
                "updateCollection",
                null,
                null,
                null,
                null,
                new Object[]{cacheId, jdcParams});

        this.checkResponses(responses);
    }

    @Override
    public Object sub(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return null;

        ServiceResponse<Object> response=service.sub(cacheId, jdcParams);
        return response==null ? null : response.getResponse();
    }

    @Override
    public ConcurrentList keys(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return null;

        ServiceResponse<ConcurrentList> response=service.keys(cacheId, jdcParams);
        return response==null ? null : response.getResponse();
    }

    @Override
    public ConcurrentList values(String cacheId, JCacheParams jdcParams) throws Exception {
        StorageService service = (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
        if(service == null) return null;

        ServiceResponse<ConcurrentList> response=service.values(cacheId, jdcParams);
        return response==null ? null : response.getResponse();
    }
}
