package j.core.cache;

import j.log.Logger;

/**
 *
 */
public class JCacheInitializerDemo implements JCacheInitializer{
    private static Logger log=Logger.create(JCacheInitializerDemo.class);


    @Override
    public void initialize(JCacheUnit unit){
        try{
            log.log("initializing local cache demo......", -1);
            unit.addOne("key-001", "value-001");
            log.log("local cache demo initialized, the value of key-001 = "+unit.get(new JCacheParams("key-001")), -1);
        }catch(Exception e){
            log.log(e, Logger.LEVEL_FATAL);
        }
    }
}
