package j.core.nvwa;

import j.core.annotation.configuration.Properties;
import j.core.common.Global;
import j.util.JUtilBean;

@Properties(path = "config.sample.properties")
@j.core.annotation.nvwa.Nvwa
public class Sample extends NvwaAncestor {
    public static void main(String[] args) throws Exception{
        Nvwa.startup();

        while(!Nvwa.isScanned()) Global.sleep1000Millis();

        Sample sample = new Sample();
        System.out.println("key2 = "+sample.getParameter("key2"));
        System.out.println("key1 = "+Nvwa.getParameter(JUtilBean.lowerFirstChar("Sample"), "key1"));


    }
}
