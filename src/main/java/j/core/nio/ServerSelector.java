package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.log.Logger;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ClassDescription(author = "肖炯", date = "2023-07-31", description = "负责一组连接的读写事件监听")
public class ServerSelector extends TimerTask {
    //日志
    private static final Logger log=Logger.create(ServerSelector.class);

    private int sn;
    private Server server;
    private Selector selector;
    private ScheduledExecutorService scheduledExecutorService;

    protected ServerSelector(int sn, Server server) throws Exception{
        this.sn=sn;
        this.server=server;
        this.selector = Selector.open();

        //定时任务
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutorService.scheduleAtFixedRate(this,
                100,
                (Integer)server.getSocketOptions().get(SocketOptions.SERVER_SELECTOR_EXECUTE_INTERVAL),
                TimeUnit.MICROSECONDS);
    }

    @MethodDescription(description = "SocketChannel的读写事件注册到Selector")
    protected SelectionKey bind(SocketChannel socketChannel, String uuid) throws Exception{
        //return socketChannel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, uuid);
        return socketChannel.register(this.selector, SelectionKey.OP_READ, uuid);
    }

    @MethodDescription(description = "key数量")
    protected int getKeys(){
        return this.selector.keys().size();
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
    @MethodDescription(description = "轮询事件")
    public void run() {
        try {
            int readyNum = selector.selectNow();
            if (readyNum == 0) return;

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey _key = keys.next();
                keys.remove();
                if(!_key.isValid()) continue;//key已经失效

                //处理某个与客户端的交互
                ServerHandler serverHandler = this.server.getHandlerOfUuid((String) _key.attachment());
                if(serverHandler == null) continue;

                //可写
                if(_key.isWritable()) serverHandler.setWritable(true);

                //可读
                if(_key.isReadable()){
                    serverHandler.setReadable(true);
                    this.server.getReaders().execute(serverHandler.getReader());
                }
            }
        } catch (Exception e) {
            log.log(e, Logger.LEVEL_ERROR);
        }
    }
}