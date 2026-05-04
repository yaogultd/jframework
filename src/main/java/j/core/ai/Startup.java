package j.core.ai;

import j.core.sys.Initializer;

public class Startup implements Initializer {
    @Override
    public void initialization() throws Exception {
        //启动模块
        FOZU.init();
    }
}
