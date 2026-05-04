package j.core.security;

import j.core.Startup;
import j.core.annotation.service.Service;
import j.core.cache.CachedMap;
import j.core.cache.JCacheParams;
import j.core.nvwa.Nvwa;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilString;

import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author 肖炯
 *
 */
@Service(path = "/framework/service/security/VerifyCode")
public class VerifyCodeService extends ServiceBase implements Runnable{
    private static Logger log=Logger.create(VerifyCodeService.class);
    private static CachedMap records=null;//发送记录

    /**
     * 初始化缓存单元
     */
    synchronized private static void init(){
        if(records==null){
            try{
                records=new CachedMap(VerifyCode.getCacheId());

                //启用监控线程
                (new Thread(new VerifyCodeService())).start();
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
                try{
                    Thread.sleep(5000);
                }catch (Exception ex){}

                init();
            }
        }
    }

    /**
     *
     * @param uuid 验证码关联的UUID
     * @param related 关联的东西（手机号、邮箱、用户ID等）
     * @param ip
     * @param type 字符组成类型
     * @param length 长度
     * @param timeout 超时时间
     * @param interval 获取间隔
     * @param maxTries 最多尝试次数
     * @return
     * @throws Exception
     */
    @Service(path = "get")
    public ServiceResponse<VerifyCodeBean> get(String uuid,
                      String related,
                      String ip,
                      int type,
                      int length,
                      long timeout,
                      long interval,
                      int maxTries) throws Exception {
        if(type!=VerifyCode.TYPE_CHAR
                &&type!=VerifyCode.TYPE_NUMBER
                &&type!=VerifyCode.TYPE_MIXED){
            return null;
        }

        if(JUtilString.isBlank(uuid)) return null;
        if(length<1||length>64) return null;

        init();//初始化缓存单元

        VerifyCodeBean vcb=(VerifyCodeBean)records.get(uuid);
        if(vcb==null && !JUtilString.isBlank(related)){
            VerifyCodeFilter filter=new VerifyCodeFilter(null);
            filter.setRelated(related);
            vcb=(VerifyCodeBean)records.get(new JCacheParams(filter));
        }

        //获取太频繁
        if(vcb!=null && vcb.isTooFrequent()){
            vcb.setError(VerifyCodeBean.ERR_TOO_FREQUENT);
            return new ServiceResponse<>(false,"","",vcb);
        }

        //检查同一IP上未超时验证码数
        if(!JUtilString.isBlank(ip)){
            VerifyCodeFilter filter=new VerifyCodeFilter(null);
            filter.setIp(ip);
            int survivals = records.size(new JCacheParams(filter));
            if(survivals >= VerifyCode.getMaxSurvivalsPerIp()){
                vcb.setError(VerifyCodeBean.ERR_TOO_MANY_SURVIVALS);
                return new ServiceResponse<>(false,"","",vcb);
            }
        }

        //未超时，返回原有记录
        if(vcb!=null && !vcb.isTimeout()){
            vcb.setTime(SysUtil.getNow());
            records.addOne(uuid, vcb);
            return new ServiceResponse<>(true,"","",vcb);
        }

        String code="";
        if(type==VerifyCode.TYPE_CHAR){
            code=JUtilString.randomChars(length);
        }else if(type==VerifyCode.TYPE_NUMBER){
            code=JUtilString.randomNum(length);
        }else if(type==VerifyCode.TYPE_MIXED){
            code=JUtilString.randomStr(length);
        }else{
            return null;
        }
        code=code.toUpperCase();

        if(Nvwa.isDebug()) log.log("create verify code of uuid="+uuid+", related="+related+", code="+code,-1);
        vcb=new VerifyCodeBean(uuid,related,ip,code,timeout,interval,maxTries);
        records.addOne(uuid, vcb);

        return new ServiceResponse<>(true,"","",vcb);
    }

    /**
     *
     * @param uuid
     * @param related
     * @param code
     * @return
     * @throws Exception
     */
    @Service(path = "check")
    public ServiceResponse<Boolean> check(String uuid, String related, String code) throws Exception {
        try{
            if(JUtilString.isBlank(uuid)) return new ServiceResponse<>(false, "", "", Boolean.FALSE);
            if(JUtilString.isBlank(code)) return new ServiceResponse<>(false, "", "", Boolean.FALSE);
            init();//初始化缓存单元

            VerifyCodeBean vcb=(VerifyCodeBean)records.get(uuid);

            //不存在或超时
            if(vcb==null || vcb.isTimeout()) return new ServiceResponse<>(false, "", "", Boolean.FALSE);

            //与获取时关联的（手机、邮箱等）不一致
            if(!JUtilString.isBlank(related) && !related.equals(vcb.getRelated())) new ServiceResponse<>(false, "", "", Boolean.FALSE);

            //超过最大试错次数
            if(vcb.getTries() >= vcb.getMaxTries()) return new ServiceResponse<>(false, "too_many_tries", "", Boolean.FALSE);

            if(!code.equalsIgnoreCase(vcb.getCode())){
                vcb.setTries(vcb.getTries() + 1);
                records.addOne(uuid, vcb);
                return new ServiceResponse<>(false, "", "", Boolean.FALSE);
            }

            log.log("check verify code of uuid="+uuid+", related="+related+", code="+code+", I returned true!!!",-1);
            return new ServiceResponse<>(true, "", "", Boolean.TRUE);
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return new ServiceResponse<>(false, "", "", Boolean.FALSE);
        }
    }

    /**
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    @Service(path = "exists")
    public ServiceResponse<VerifyCodeBean> exists(String uuid) throws Exception {
        try{
            if(JUtilString.isBlank(uuid)) return null;
            init();//初始化缓存单元

            VerifyCodeBean vcb = (VerifyCodeBean)records.get(uuid);
            if(vcb==null) return null;

            return new ServiceResponse<>(true, "", "", vcb);
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     * @param uuid
     * @throws Exception
     */
    @Service(path = "remove")
    public ServiceResponse remove(String uuid) throws Exception {
        try{
            records.remove(uuid);
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return new ServiceResponse<>(true, "", "");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        JCacheParams params=new JCacheParams(new VerifyCodeRemover());
        while(!Startup.isDestroyed()){
            try{
                Thread.sleep(5000);
            }catch(Exception e){}

            try{
                records.remove(params);
            }catch(Exception e){
                log.log(e, Logger.LEVEL_ERROR);
            }
        }
    }
}
