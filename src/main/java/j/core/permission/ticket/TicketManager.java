package j.core.permission.ticket;

import j.core.cache.CachedMap;
import j.core.cache.JCacheParams;
import j.core.common.Global;
import j.core.common.JObject;
import j.log.Logger;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketManager extends TimerTask {
    //日志输出
    private static Logger log=Logger.create(TicketManager.class);

    //所有Ticket
    private static CachedMap tickets = null;

    //定时执行服务（清除过期Ticket）
    private static ScheduledExecutorService scheduledExecutorService;

    //过期Ticket移除判断类
    private static JCacheParams remover = new JCacheParams(new TicketRemover());

    static {
        init();
    }

    /**
     *
     */
    private static void init(){
        try{
            tickets = new CachedMap("j.core.permission.ticket.TicketManager.tickets");
            log.log("cache unit for tickets created!", -1);

            //定时任务
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(new TicketManager(),1000,1000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            Global.sleep1000Millis();
            init();
        }
    }

    /**
     *
     * @param related
     * @param survival
     * @return
     */
    public static Ticket get(Object related, long survival){
        return get(related, null, survival);
    }

    /**
     *
     * @param related
     * @param theTicket
     * @param survival
     * @return
     */
    public static Ticket get(Object related, String theTicket, long survival){
        Ticket ticket = new Ticket(related, theTicket, survival);
        try{
            tickets.put(ticket.getTicket(), ticket);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return ticket;
    }

    /**
     *
     * @param theTicket
     * @return
     */
    public static Ticket exists(String theTicket){
        Ticket ticket = null;
        try{
            ticket = (Ticket) tickets.get(theTicket);
            if(ticket != null) tickets.remove(theTicket);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return ticket;
    }

    /**
     *
     * @param related
     * @param theTicket
     * @return
     */
    public static Ticket valid(Object related, String theTicket){
        Ticket ticket = null;
        try{
            ticket = (Ticket) tickets.get(theTicket);
            if(ticket != null) tickets.remove(theTicket);
            if(ticket==null || ticket.isExpired()) return null;

            if(!JObject.equals(related, ticket.getRelated())) return null;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return ticket;
    }

    @Override
    public void run() {
        try{
            if(tickets==null) return;
            tickets.remove(remover);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
    }
}
