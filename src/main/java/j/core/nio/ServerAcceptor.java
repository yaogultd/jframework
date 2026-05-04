package j.core.nio;

import j.log.Logger;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 接受连接
 */
public class ServerAcceptor extends TimerTask {
    //日志
    private static final Logger log=Logger.create(ServerAcceptor.class);

    //关联的Server实例
    private Server server;

    //selector（接受连接）
    private Selector selector;

    //定时执行服务（接受连接）
    private ScheduledExecutorService scheduledExecutorService;

    /**
     *
     * @param server
     * @throws Exception
     */
    protected ServerAcceptor(Server server) throws Exception{
        this.server=server;

        this.selector = Selector.open();
        this.server.setSelectorForConnection(this.selector);

        //定时任务（接受连接）
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutorService.scheduleAtFixedRate(this,100,100, TimeUnit.MILLISECONDS);
    }

    /**
     *
     */
    protected void shutdown(){
        try{
            this.scheduledExecutorService.shutdownNow();
        }catch(Exception ignored){}

        try{
            selector.close();
        }catch(Exception ignored){}
    }

    @Override
    public void run() {
        try {
            int readyNum = selector.selectNow();
            if (readyNum == 0) return;

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while(keys.hasNext()) {
                SelectionKey _key = keys.next();
                keys.remove();

                //key已经失效
                if(!_key.isValid()) continue;

                this.server.accept();
            }
        } catch (Exception e) {
            log.log(e, Logger.LEVEL_ERROR);
        }
    }
}