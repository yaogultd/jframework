package j.http.proxy;

import j.core.dao.DAO;
import j.core.dao.DAOs;
import j.core.dao.DB;
import j.core.dao.util.SQLUtil;
import j.core.db.JhttpProxy;
import j.core.sys.SysUtil;
import j.http.proxy.provider.ProxyProvider;
import j.log.Logger;
import j.util.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 代理IP池维护
 */
@Setter
@Getter
public class ProxyPool extends TimerTask {
    //日志输出
    private static Logger log=Logger.create(ProxyPool.class);

    //所有实例
    private static ConcurrentMap<String, ProxyPool> instances = new ConcurrentMap<>();

    //默认实例ID
    public static final String POOL_ID_DEFAULT="j.http.proxy.ProxyPool";

    //代理IP池ID（必须指定）
    private String poolId;

    //所有代理
    private ConcurrentMap<String, JhttpProxy> proxies = new ConcurrentMap<>();

    //代理使用者记录
    private ConcurrentMap<String, List<String>> proxyCallers = new ConcurrentMap<>();

    //所有使用者
    private ConcurrentMap<String, ProxyUsage> usages = new ConcurrentMap<>();

    //是否已经完成初始化
    private boolean loaded=false;

    //每次获取IP增量
    private int increment=1;

    //代理提供商列表（至少需指定一个）
    private ConcurrentList<ProxyProvider> providers=new ConcurrentList<>();

    //定时任务执行
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService scheduledExecutorService4Saver = Executors.newSingleThreadScheduledExecutor();

    //入库进程
    //private ProxySaver saver;

    /**
     *
     * @param poolId
     * @return
     */
    public static ProxyPool getInstance(String poolId) throws Exception{
        if(JUtilString.isBlank(poolId)) poolId=POOL_ID_DEFAULT;
        if(JUtilString.bytes(poolId, "UTF-8") > 128) throw new Exception("The pool id can not be longer than 128 Bytes");

        ProxyPool instance = instances.get(poolId);
        if(instance != null) return instance;

        instance = new ProxyPool(poolId);
        instances.put(poolId, instance);
        instance.load();

        return instance;
    }

    /**
     *
     * @param poolId
     */
    private ProxyPool(String poolId){
        this.poolId = poolId;
        //this.saver=new ProxySaver(this.poolId);
        //scheduledExecutorService4Saver.scheduleAtFixedRate(this.saver,1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param provider
     */
    public void appendProvider(ProxyProvider provider){
        this.providers.add(provider);
    }

    /**
     * 初始化
     * @return
     */
    private boolean load(){
        if(loaded) return true;

        DAO dao=null;
        int rpp=1000;
        int pn=1;
        try{
            dao= DAOs.create(DB.getJFrameworkDB().getName(), ProxyPool.class, false);
            List<JhttpProxy> list=dao.find("j_http_proxy", "pool_id='"+ SQLUtil.deleteCriminalChars(this.poolId) +"' and row_deleted='N'", rpp, pn);
            while(list!=null && !list.isEmpty()){
                log.log("load "+list.size()+" proxies from db.", -1);
                for(int i=0; i<list.size(); i++){
                    JhttpProxy proxy=list.get(i);
                    if(isProxyDead(proxy)) continue;
                    proxies.put(proxy.getUuid(), proxy);
                }

                pn++;
                list=dao.find("j_http_proxy", "pool_id='"+ SQLUtil.deleteCriminalChars(this.poolId) +"' and row_deleted='N'", rpp, pn);
            }
            dao.commit();

            //启动管理进程
            scheduledExecutorService.scheduleAtFixedRate(this,1000, 1000, TimeUnit.MILLISECONDS);

            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
            return false;
        }
    }

    /**
     *
     * @param proxyUuid
     * @return
     */
    public JhttpProxy getProxy(String proxyUuid){
        try{
            return proxies.get(proxyUuid);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     * @param filter
     * @return
     */
    public ConcurrentList<JhttpProxy> getProxies(ProxyFilter filter){
        ConcurrentList<JhttpProxy> list=new ConcurrentList<>();
        try{
            ConcurrentList<JhttpProxy> all = proxies.listValues();
            for(int i=0; i<all.size(); i++){
                JhttpProxy p=all.get(i);
                if(filter==null || filter.matches(this, p)) list.add(p);
            }
            all.clear();
            all=null;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return list;
    }

    /**
     * 请求代理IP
     * @param filter
     * @param usage
     * @return
     */
    public ConcurrentList<JhttpProxy> requestProxies(ProxyFilter filter, ProxyUsage usage){
        ConcurrentList<JhttpProxy> list=getProxies(filter);
        if(list==null || list.isEmpty()){//IP不够用
            for(int i=0; i<this.providers.size(); i++){
                ProxyProvider provider=this.providers.get(i);
                if(provider.available() == 0) continue;//该提供商最多可供获取的代理数量为0, <0表示未知（可获取ip）

                List<JhttpProxy> newProxies=provider.get(this.increment,
                        usage.getMinSurvival(),
                        usage.getMaxSurvival(),
                        usage.isHttpsSupported(),
                        usage.isRepeatable(),
                        usage.getContinentId(),
                        usage.getCountryId(),
                        usage.getProvinceId());

                if(newProxies!=null && !newProxies.isEmpty()){
                    //加入池
                    this.appendProxies(newProxies);
                    list.addAll(newProxies);

                    for(int j=0; j<newProxies.size(); j++){
                        JhttpProxy p=newProxies.get(j);
                        p.setHttpsSupported(usage.isHttpsSupported() ? "T" : "F");
                        p.setContinentId(usage.getContinentId());
                        p.setCountryId(usage.getCountryId());
                        p.setProvinceId(usage.getProvinceId());
                    }

                    //入库
                    //this.saver.save(newProxies);
                }


                //已经获取到多于或等于每次增量的代理数
                if(list.size() >= this.increment) break;
            }
        }
        return list;
    }

    /**
     * 将代理添加到队列
     * @param newProxies
     */
    private void appendProxies(List<JhttpProxy> newProxies){
        for(int j=0; j<newProxies.size(); j++){
            JhttpProxy proxy=newProxies.get(j);
            if(JUtilString.isBlank(proxy.getUuid())) proxy.setUuid(JUtilUUID.genUUID());
            this.proxies.put(proxy.getUuid(), proxy);
        }
    }

    /**
     *
     * @param caller
     * @return
     */
    public ProxyUsage getUsage(String caller){
        try{
            return usages.get(caller);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     * @param usage
     */
    public void removeUsage(ProxyUsage usage){
        try{
            removeCaller(usage.getProxyUuid(), usage.getId());
            usages.remove(usage.getId());
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
    }

    /**
     *
     * @param filter
     * @return
     */
    public ConcurrentList<ProxyUsage> getUsages(ProxyUsageFilter filter){
        ConcurrentList<ProxyUsage> list=new ConcurrentList<>();
        try{
            ConcurrentList<ProxyUsage> all = usages.listValues();
            for(int i=0; i<all.size(); i++){
                ProxyUsage usage=all.get(i);
                if(usage==null || filter.matches(usage)) list.add(usage);
            }
            all.clear();
            all=null;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return list;
    }

    /**
     *
     * @param proxyUuid
     * @return
     */
    public ConcurrentList<ProxyUsage> getUsages(String proxyUuid){
        ProxyUsageFilter filter = new ProxyUsageFilter();
        filter.setProxyUuid(proxyUuid);
        return getUsages(filter);
    }

    /**
     *
     * @param proxyUuids
     * @return
     */
    public ConcurrentList<ProxyUsage> getUsages(List<String> proxyUuids){
        if(proxyUuids==null || proxyUuids.isEmpty()) return null;
        ProxyUsageFilter filter = new ProxyUsageFilter();
        filter.setProxyUuids(proxyUuids);
        return getUsages(filter);
    }

    /**
     * 代理是否已过期
     * @param proxy
     * @return
     */
    public boolean isProxyDead(JhttpProxy proxy){
        //提前5秒结束
        return proxy==null || (proxy.getLifeEnd() > 0 && proxy.getLifeEnd() < (SysUtil.getNow() - 5000));
    }

    /**
     *
     * @param proxyUuid
     * @param caller
     */
    private void saveCaller(String proxyUuid, String caller){
        List<String> callers=proxyCallers.get(proxyUuid);
        if(callers==null) callers=new ArrayList<>();
        if(!callers.contains(caller)) callers.add(caller);
        proxyCallers.put(proxyUuid, callers);
    }

    /**
     *
     * @param proxyUuid
     * @param caller
     */
    private void removeCaller(String proxyUuid, String caller){
        List<String> callers=proxyCallers.get(proxyUuid);
        if(callers!=null && callers.contains(caller)) callers.remove(caller);
    }

    /**
     * 根据使用需求获取代理IP
     * @param caller 使用者，必须指定，由业务逻辑保证唯一性（如有需要）等
     * @param maxErrors 同一ip允许连续出错次数
     * @param minSurvival
     * @param maxSurvival
     * @param httpsSupported
     * @param repeatable
     * @param continentId
     * @param countryId
     * @param provinceId
     * @return
     * @throws Exception
     */
    public ProxyUsage use(String caller,
                                 int maxErrors,
                                 int minSurvival,
                                 int maxSurvival,
                                 boolean httpsSupported,
                                 boolean repeatable,
                                 String continentId,
                                 String countryId,
                                 String provinceId) throws Exception{
        if(JUtilString.isBlank(caller)) throw new Exception("the caller must not be blank.");
        ProxyUsage usage = usages.get(caller);

        //已经存在，并且未被释放
        if(usage != null && !usage.isReleased()) return usage;

        usage=new ProxyUsage(caller);
        usage.setMaxErrors(maxErrors);
        usage.setMinSurvival(minSurvival);
        usage.setMaxSurvival(maxSurvival);
        usage.setHttpsSupported(httpsSupported);
        usage.setRepeatable(repeatable);
        usage.setContinentId(continentId);
        usage.setCountryId(countryId);
        usage.setProvinceId(provinceId);

        JhttpProxy usedProxy=this.renew(usage);
        if(usedProxy==null) return null;

        //保存使用记录
        this.saveCaller(usedProxy.getUuid(), caller);
        usages.put(caller, usage);
        return usage;
    }

    /**
     * 获取代理IP（默认需求）
     * @param caller 使用者，必须指定，由业务逻辑保证唯一性（如有需要）等
     * @return
     * @throws Exception
     */
    public ProxyUsage use(String caller) throws Exception{
        if(JUtilString.isBlank(caller)) throw new Exception("the caller must not be blank.");
        ProxyUsage usage = usages.get(caller);

        //已经存在，并且未被释放
        if(usage != null && !usage.isReleased()) return usage;

        usage=new ProxyUsage(caller);
        JhttpProxy usedProxy=this.renew(usage);

        //保存使用记录
        if(usedProxy!=null) this.saveCaller(usedProxy.getUuid(), caller);

        usages.put(caller, usage);
        return usage;
    }

    /**
     * 根据设定的使用需求重新获取IP
     * @param usage
     * @return
     */
    synchronized public JhttpProxy renew(ProxyUsage usage){
        ProxyFilter filter=new ProxyFilter();
        filter.setMinSurvival(usage.getMinSurvival());
        filter.setMaxSurvival(usage.getMaxSurvival());
        filter.setHttpsSupported(usage.isHttpsSupported()?1:-1);
        if(!usage.isRepeatable()) filter.setExcludedUuids(usage.getUsedProxyUuids());//去重
        filter.setContinentId(usage.getContinentId());
        filter.setCountryId(usage.getCountryId());
        filter.setProvinceId(usage.getProvinceId());
        filter.setExclusive(usage.isExclusive());

        List<JhttpProxy> list=this.requestProxies(filter, usage);
        if(list==null || list.isEmpty()) return null;

        JhttpProxy proxy=list.get(list.size()==1 ? 0 : JUtilRandom.nextInt(list.size()));
        usage.use(proxy);

        //log.log("alloc proxy to "+usage.getId()+" -> "+JUtilBean.bean2Json(proxy), -1);
        return proxy;
    }

    /**
     * 是否已失效（连续错误次数太多）
     * @return
     */
    public boolean isUsageValid(ProxyUsage usage){
        if(usage.getMaxErrors() > 0 && usage.getErrorsContinuous() >= usage.getMaxErrors()) return false;
        JhttpProxy proxy=this.getProxy(usage.getProxyUuid());
        if(this.isProxyDead(proxy)) return false;
        return true;
    }

    /**
     *
     * @param proxyUuid
     * @return
     */
    public int inUse(String proxyUuid){
        List<String> callers=proxyCallers.get(proxyUuid);
        return callers==null ? 0 : callers.size();
    }

    /**
     * 是否可被多个用户使用
     * @param proxyUuid
     * @return
     */
    public boolean canBeUsedByMultiCallers(String proxyUuid){
        List<String> callers=proxyCallers.get(proxyUuid);
        if(callers==null || callers.isEmpty()) return true;

        for(int i=0; i<callers.size(); i++){
            ProxyUsage usage=getUsage(callers.get(i));
            if(usage != null && usage.isExclusive()) return false;//使用者声明要独占，不能给其它使用
        }

        return true;
    }

    /**
     * 当指定的ip失效时，自动获取新的有效ip
     * @param removedUuids
     */
    private void renewUsages(List<String> removedUuids){
        if(removedUuids==null || removedUuids.isEmpty()) return;
        ConcurrentList<ProxyUsage> usages=getUsages(removedUuids);
        for(int i=0; i<usages.size(); i++){
            ProxyUsage usage = usages.get(i);
            try{
                if(usage.isAutoRenew()) this.renew(usage);
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
            }
        }
    }

    @Override
    public void run() {
        DAO dao=null;
        try{
            //log.log("更新数据库记录状态，标记过期IP -> "+("update j_http_proxy set row_deleted='D' where life_end>0 and life_end<"+ SysUtil.getNow()), -1);
            dao=DAOs.create(DB.getJFrameworkDB().getName(), this.getClass(), false);
            dao.executeSQL("update j_http_proxy set row_deleted='D' where life_end>0 and life_end<"+ SysUtil.getNow());
            DAOs.commit(dao);

            //从缓存中移除过期的
            ConcurrentList<JhttpProxy> list=proxies.listValues();
            List<String> removedUuids=new ArrayList<>();
            for(int i=0; i<list.size(); i++){
                JhttpProxy p=list.get(i);
                if(isProxyDead(p)){
                    log.log("The proxy is dead(has expired), remove it from pool and auto renew related callers -> "+JUtilBean.bean2Json(p), -1);
                    proxies.remove(p.getUuid());
                    proxyCallers.remove(p.getUuid());
                    removedUuids.add(p.getUuid());
                }
            }
            renewUsages(removedUuids);
            list.clear();
            list=null;
            removedUuids.clear();
            removedUuids=null;

            //按需更新使用的代理
            ConcurrentList<ProxyUsage> all = usages.listValues();
            for(int i=0; i<all.size(); i++){
                ProxyUsage usage=all.get(i);
                try{
                    //闲置超过最大空闲时间
                    if(usage.isIdle()){
                        log.log("The usage is idle, so remove it -> "+JUtilBean.bean2Json(usage), -1);
                        removeUsage(usage);
                        return;
                    }

                    if(usage.isReleased()){
                        log.log("The usage is released, so remove it -> "+JUtilBean.bean2Json(usage), -1);
                        removeUsage(usage);
                        return;
                    }

                    if(usage.isAutoRenew() && !this.isUsageValid(usage)){
                        if(usage.getErrorsContinuous() >= usage.getMaxErrors()) {
                            log.log("The proxy usage is invalid(too many continuous errors), remove it from pool and auto renew related callers -> "+JUtilBean.bean2Json(usage), -1);
                        }else {
                            log.log("The proxy usage is invalid(the proxy has expired), remove it from pool and auto renew related callers -> "+JUtilBean.bean2Json(usage), -1);
                        }
                        this.renew(usage);
                    }
                }catch (Exception e){
                    log.log(e, Logger.LEVEL_ERROR);
                }
            }
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
        }
    }
}