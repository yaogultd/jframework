package j.http.proxy;

import j.core.cache.JCacheFilter;
import j.core.db.JhttpProxy;
import j.core.sys.SysUtil;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 */
@Getter
@Setter
public class ProxyFilter {
    private int minSurvival;
    private int maxSurvival;
    private int httpsSupported;//-1 不限， 0 不支持， 1 支持
    private String continentId;
    private String countryId;
    private String provinceId;
    private List<String> excludedUuids;
    private boolean exclusive=false;//是否独占

    /**
     *
     * @param pool
     * @param object
     * @return
     */
    public boolean matches(ProxyPool pool, Object object) {
        if(object==null || !(object instanceof JhttpProxy)) return false;
        JhttpProxy p=(JhttpProxy)object;

        long survival=0;
        if(p.getLifeEnd() != null && p.getLifeEnd() > 0){
            survival = p.getLifeEnd() - SysUtil.getNow();
            if(this.minSurvival > 0 && survival < this.minSurvival) return false;
            if(this.maxSurvival > 0 && survival > this.maxSurvival) return false;
        }

        if(httpsSupported==0 && "T".equals(p.getHttpsSupported())) return false;
        if(httpsSupported==1 && !"T".equals(p.getHttpsSupported())) return false;

        //排除的
        if(excludedUuids!=null && excludedUuids.contains(p.getUuid())) return false;

        if(!JUtilString.isBlank(continentId) && !continentId.equals(p.getContinentId())) return false;
        if(!JUtilString.isBlank(countryId) && !countryId.equals(p.getCountryId())) return false;
        if(!JUtilString.isBlank(provinceId) && !provinceId.equals(p.getProvinceId())) return false;

        //独占
        if(this.exclusive && pool.inUse(p.getUuid()) > 0) return false;

        //被他人声明为独占，不能使用
        if(!pool.canBeUsedByMultiCallers(p.getUuid())) return false;

        return true;
    }
}
