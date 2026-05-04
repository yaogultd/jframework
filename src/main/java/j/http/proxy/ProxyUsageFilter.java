package j.http.proxy;

import j.core.cache.JCacheFilter;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 */
@Getter
@Setter
public class ProxyUsageFilter{
    private String proxyUuid;
    private String proxyIp;
    private String proxyIpv6;
    private List<String> proxyUuids;

    /**
     *
     * @param object
     * @return
     */
    public boolean matches(Object object) {
        if(object==null || !(object instanceof ProxyUsage)) return false;
        ProxyUsage p=(ProxyUsage)object;

        if(!JUtilString.isBlank(proxyUuid) && !proxyUuid.equals(p.getProxyUuid())) return false;
        if(!JUtilString.isBlank(proxyIp) && !proxyIp.equals(p.getProxyIp())) return false;
        if(!JUtilString.isBlank(proxyIpv6) && !proxyIpv6.equals(p.getProxyIpv6())) return false;
        if(proxyUuids!=null && !proxyUuids.isEmpty() && !proxyUuids.contains(p.getProxyUuid())) return false;

        return true;
    }
}
