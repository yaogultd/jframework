package j.http.proxy.provider;

import j.core.db.JhttpProxy;
import j.core.nvwa.NvwaAncestor;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.reflections.Reflections;

import java.util.List;
import java.util.Set;

public abstract class ProxyProvider extends NvwaAncestor {
    private static ConcurrentMap<String, String> providers = new ConcurrentMap<>();

    static {
        Reflections reflections = new Reflections("j.http.proxy.provider");
        Set<Class<? extends ProxyProvider>> clazz = reflections.getSubTypesOf(ProxyProvider.class);
        clazz.forEach(cls -> {
            try {
                ProxyProvider provider = cls.newInstance();
                providers.put(provider.getProviderId().toLowerCase(), cls.getCanonicalName());
                System.out.println("ProxyProvider "+provider.getProviderId().toLowerCase()+" => "+cls.getCanonicalName());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     *
     * @param providerId
     * @return
     * @throws Exception
     */
    public static ProxyProvider getInstance(String providerId) throws Exception{
        if(JUtilString.isBlank(providerId)) return null;

        String cls = providers.get(providerId.toLowerCase());
        if(JUtilString.isBlank(cls)) return null;
        return (ProxyProvider)Class.forName(cls).getConstructor().newInstance();
    }

    /**
     * 获取总可用代理数
     * @return
     */
    public abstract String getProviderId();
    
    /**
     * 获取代理
     * @param amount 获取数量（小于等于0表示不限制）
     * @param minSurvival 最小有效期（小于等于0表示不限制）
     * @param maxSurvival 最大有效期（小于等于0表示不限制）
     * @param httpsSupported 是否支持https
     * @param repeatable 是否可重复
     * @param continentId 所在大洲
     * @param countryId 所在国家
     * @param provinceId 所在省份
     * @return
     */
    public abstract List<JhttpProxy> get(int amount,
                                int minSurvival,
                                int maxSurvival,
                                boolean httpsSupported,
                                boolean repeatable,
                                String continentId,
                                String countryId,
                                String provinceId);

    /**
     * 获取总可用代理数
     * @return
     */
    public abstract int available();
}
