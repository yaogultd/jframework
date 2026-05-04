package j.http.proxy.provider;

import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.db.JhttpProxy;
import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.JUtilString;
import j.util.JUtilTimestamp;

import java.util.ArrayList;
import java.util.List;


@Properties(path = "httpProxyProviderIproyal.properties")
@Nvwa
public class ProxyProviderIproyal extends ProxyProvider{
    //日志输出
    private static Logger log=Logger.create(ProxyProviderIproyal.class);

    @Override
    public String getProviderId() {
        return "Iproyal";
    }

    /**
     *
     */
    public ProxyProviderIproyal() {
    }

    @Override
    public List<JhttpProxy> get(int amount,
                                int minSurvival,
                                int maxSurvival,
                                boolean httpsSupported,
                                boolean repeatable,
                                String continentId,
                                String countryId,
                                String provinceId) {
        List<JhttpProxy> list=new ArrayList<>();
        for(int sn=0; sn<100; sn++){
            String randomProxy=this.getParameter("randomProxy"+sn);
            if(JUtilString.isBlank(randomProxy)) continue;

            String[] ps=JUtilString.getTokens(randomProxy, ";");
            for(int i=0; i<ps.length; i++){
                if(JUtilString.isBlank(ps[i]) || ps[i].indexOf(":")<0) continue;
                String[] cells = JUtilString.getTokens(ps[i], ":");
                if(cells.length!=4) continue;

                JhttpProxy proxy=new JhttpProxy();
                proxy.setProxyIp(cells[0]);
                proxy.setProxyPort(Integer.parseInt(cells[1]));
                proxy.setProxyUsername(cells[2]);
                proxy.setProxyPassword(cells[3]);
                proxy.setLifeEnd(SysUtil.getNow() + JUtilTimestamp.millisOfDay*365);
                list.add(proxy);
            }
        }

        return list;
    }

    @Override
    public int available() {
        return 100;
    }
}
