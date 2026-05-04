package j.core.serialize;

import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.nvwa.NvwaAncestor;
import j.log.Logger;
import j.util.JUtilBean;
import j.util.JUtilZip;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Nvwa
@Properties(path = "serialize.properties")
public class JSerialization extends NvwaAncestor implements Serializable{
    private static Logger log=Logger.create(JSerialization.class);

    //FSTConfiguration实例个数
    private static int configInstances=2;

    //FSTConfiguration实例
    private static List<FSTConfiguration> FST_CONFs = new ArrayList<>();

    private static FSTConfiguration configuration;

    /**
     *
     */
    synchronized private static void createFSTConfigurations(){
        if(configuration==null){
            configuration=FSTConfiguration.getDefaultConfiguration();
        }

        /*
        String _configInstances = j.core.nvwa.Nvwa.getParameter(JSerialization.class, "configInstances");
        if(JUtilMath.isInt(_configInstances)){
            int setting=Integer.parseInt(_configInstances);
            if(setting>=2 && setting != configInstances){
                configInstances = Integer.parseInt(_configInstances);
                log.log("FSTConfiguration instances => "+configInstances, -1);
            }
        }

        boolean newInstances=false;
        while(FST_CONFs.size() < configInstances){
            FSTConfiguration FST_CONF = FSTConfiguration.createDefaultConfiguration();
            FST_CONFs.add(FST_CONF);
            newInstances=true;
        }
        if(newInstances) log.log("FSTConfiguration instances("+configInstances+") created!", -1);*/
    }

    /**
     *
     * @return
     */
    private static FSTConfiguration getFSTConfiguration(){
        createFSTConfigurations();
        return configuration;
        //return FST_CONFs.get(JUtilRandom.nextInt(FST_CONFs.size()));
    }

    public static Object deSerialize(byte[] bytes) throws Exception{
        return deSerialize(null, bytes);
    }

    public static byte[] serialize(Object object) throws Exception{
        return serialize(null, object);
    }

    public static Object deSerialize(FSTConfiguration fstConf, byte[] bytes) throws Exception{
        return deSerialize(fstConf, bytes, false);
    }

    public static byte[] serialize(FSTConfiguration fstConf, Object object) throws Exception{
        return serialize(fstConf, object, false);
    }

    public static Object deSerialize(FSTConfiguration fstConf, byte[] bytes, boolean gzip) throws Exception{
        if(gzip) bytes = JUtilZip.unGzip(bytes);
        return fstConf==null ? getFSTConfiguration().asObject(bytes) : fstConf.asObject(bytes);
    }

    public static byte[] serialize(FSTConfiguration fstConf, Object object, boolean gzip) throws Exception{
        byte[] bs = fstConf==null ? getFSTConfiguration().asByteArray(object) : fstConf.asByteArray(object);
        return gzip ? JUtilZip.gzip(bs) : bs;
    }

    @Override
    public String toString(){
        return JUtilBean.bean2Json(this);
    }
}
