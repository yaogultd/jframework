package j.http.proxy;

import j.core.dao.DAO;
import j.core.dao.DAOs;
import j.core.dao.util.SQLUtil;
import j.core.db.JhttpProxy;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.JUtilString;
import j.util.JUtilUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * 代理入库
 */
public class ProxySaver extends TimerTask {
    //日志输出
    private static Logger log=Logger.create(ProxySaver.class);

    //关联代理池ID
    private String poolId;

    //待入库队列
    private ConcurrentList<JhttpProxy> queue = new ConcurrentList<>();

    /**
     *
     * @param poolId
     */
    public ProxySaver(String poolId){
        this.poolId=poolId;
    }

    /**
     *
     * @param proxy
     */
    public void save(JhttpProxy proxy){
        proxy.setPoolId(this.poolId);
        if(JUtilString.isBlank(proxy.getUuid())) proxy.setUuid(JUtilUUID.genUUID());
        proxy.setRowDeleted("N");
        queue.add(proxy);
    }

    /**
     *
     * @param proxies
     */
    public void save(List<JhttpProxy> proxies){
        if(proxies==null || proxies.isEmpty()) return;
        for(int i=0; i<proxies.size(); i++) this.save(proxies.get(i));
    }

    @Override
    public void run() {
        DAO dao=null;
        try{
            dao=DAOs.create(this.getClass(), true);
            while(!queue.isEmpty()){
                JhttpProxy proxy = queue.remove(0);

                int exists = 0;
                if(!JUtilString.isBlank(proxy.getProxyUsername())) {
                    exists = dao.getRecordCnt("j_http_proxy", "proxy_ip='"+ proxy.getProxyIp() +"' and proxy_port="+proxy.getProxyPort()+" and proxy_username='"+SQLUtil.deleteCriminalChars(proxy.getProxyUsername())+"' and proxy_password='"+SQLUtil.deleteCriminalChars(proxy.getProxyPassword())+"' and pool_id='"+ SQLUtil.deleteCriminalChars(this.poolId)+"'");
                }else {
                    exists = dao.getRecordCnt("j_http_proxy", "proxy_ip='"+ proxy.getProxyIp() +"' and proxy_port="+proxy.getProxyPort()+" and pool_id='"+ SQLUtil.deleteCriminalChars(this.poolId) +"'");
                }
                if(exists > 0) dao.updateByKeys(proxy);
                else dao.insert(proxy);
            }
            DAOs.commit(dao);
        }catch (Exception e){
            DAOs.onException(dao);
            log.log(e, Logger.LEVEL_ERROR);
        }
    }
}