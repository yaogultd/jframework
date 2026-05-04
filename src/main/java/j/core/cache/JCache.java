package j.core.cache;

import j.core.annotation.description.MethodDescription;
import j.core.cache.storage.Storage;
import j.core.cache.storage.StoragePolicy;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

/**
 * @author 肖炯
 *
 */
public class JCache implements Consumer{
    private static Logger log=Logger.create(JCache.class);

    public static final int UNIT_MAP=1;//缓存单元类型: KEY-VALUE
    public static final int UNIT_LIST=2;//缓存单元类型: LIST
    public static final int LIFECIRCLE_TEMPORARY=1;//临时缓存，超时未用的自动清除
    public static final int LIFECIRCLE_DURABLE=2;//常驻缓存，重启应用才会清除

    //缓存存储策略定义
    private static ConcurrentMap<String, StoragePolicy> storagePolicies=new ConcurrentMap<>();

    //缓存ID-存储策略对应关系
    private static ConcurrentList<JCacheMapping> mappings=new ConcurrentList<>();

    //是否已加载完配置
    private static boolean loaded=false;

    /**
     *
     *
     */
    public JCache() {
        super();
    }

    /**
     *
     * @param cacheId
     * @return
     * @throws Exception
     */
    public static Storage getStorage(String cacheId) throws Exception{
        if(JUtilString.isBlank(cacheId)) return null;

        while(!loaded){
            log.log("waiting for config loaded（cache id = "+cacheId+"）......", -1);
            try{
                Thread.sleep(1000);
            }catch (Exception e){}
        }

        JCacheMapping mapping=JCache.mapping(cacheId);
        if(mapping==null){
            throw new Exception("no cache mapping matches the cache id "+cacheId+".");
        }

        Storage storage=mapping.getStorage();
        if(storage==null){
            throw new Exception("no storage instance for cache id "+cacheId+".");
        }

        return storage;
    }

    /**
     *
     * @param id
     * @return
     */
    public static StoragePolicy getStoragePolicy(String id){
        return JUtilString.isBlank(id)?null:storagePolicies.get(id);
    }

    /**
     * @param cacheId
     * @return
     */
    public static JCacheMapping mapping(String cacheId){
        //精确匹配
        for(int i=0;i<mappings.size();i++){
            JCacheMapping mapping=mappings.get(i);
            if(mapping.matchesExactly(cacheId)) return mapping;
        }

        //模糊
        for(int i=0;i<mappings.size();i++){
            JCacheMapping mapping=mappings.get(i);
            if(mapping.matchesWildcard(cacheId)) return mapping;
        }

        //正则匹配
        for(int i=0;i<mappings.size();i++){
            JCacheMapping mapping=mappings.get(i);
            if(mapping.matches(cacheId)) return mapping;
        }
        return null;
    }

    @MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
    synchronized private static boolean load(Resource resource){
        try{
            Document document= ((ResourceXml)resource).getResource();
            Element root=document.getRootElement();

            //新版配置（nvwa.xml中的JCache节点）
            if(root.element("JCache")!=null) root=root.element("JCache");

            log.log("loading cache config => "+resource, -1);

            storagePolicies.clear();
            mappings.clear();

            //未启用
            if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
                loaded=true;
                return true;
            }

            //存储策略配置
            Element storagePoliciesElement=root.element("storage-policies");
            List<Element> storagePolicyElements=storagePoliciesElement==null?null:storagePoliciesElement.elements("policy");
            for(int i=0; storagePolicyElements!=null && i<storagePolicyElements.size(); i++){
                StoragePolicy policy=new StoragePolicy(storagePolicyElements.get(i));
                storagePolicies.put(policy.getId(), policy);
            }

            //映射配置
            Element mappingsElement=root.element("mappings");
            List mappingElements=mappingsElement==null?null:mappingsElement.elements("mapping");
            for(int i=0; mappingElements!=null && i<mappingElements.size(); i++){
                Element mappingElement=(Element)mappingElements.get(i);

                JCacheMapping mapping=new JCacheMapping(mappingElement.attributeValue("selector"),
                        mappingElement.attributeValue("storage-policy"));

                log.log("load JDCache mapping -> "+mapping, -1);

                mappings.add(mapping);
            }

            loaded=true;
            return true;
        }catch(Exception e){
            log.log(e,Logger.LEVEL_FATAL);
            loaded=true;
            return false;
        }
    }

    @Override
    public boolean onFound(Resource resource) {
        //不是xml资源不予加载
        if(!(resource instanceof ResourceXml)) return false;

        //仅处理JCache.xml（旧版配置）或nvwa.xml（新版配置）
        if(!resource.getPath().endsWith("JCache.xml")
                &&!resource.getPath().endsWith("nvwa.xml")) return false;

        return load(resource);
    }

    @Override
    public boolean onUpdate(Resource resource){
        return true;
    }
}