package j.core.nio;

import j.log.Logger;

import java.util.TimerTask;

/**
 * 执行服务端读写操作
 */
public class ServerExecutor extends TimerTask {
    private static Logger log=Logger.create(ServerExecutor.class);

    private ServerHandler handler;
    private String operation;//read / write
    private boolean end;

    /**
     *
     * @param handler
     * @param operation
     */
    public ServerExecutor(ServerHandler handler, String operation){
        this.handler=handler;
        this.operation=operation;
        this.end=false;
    }

    /**
     *
     */
    public void end(){
        this.end=true;
    }

    @Override
    public void run() {
        if(this.end) return;
        try{
            if("read".equals(this.operation)){
                this.handler.setReadable(false);
                this.handler.receive();//读
            }else if("write".equals(this.operation)){
                this.handler.setWritable(false);
                //this.handler.write();
            }
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            handler.end(true);
        }
    }
}