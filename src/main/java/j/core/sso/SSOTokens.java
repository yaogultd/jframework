package j.core.sso;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.dao.DAO;
import j.core.dao.DB;
import j.core.dao.Database;
import j.core.db.JuserLogin;
import j.core.hp.thread.ThreadManager;
import j.core.hp.thread.ThreadPool;
import j.log.Logger;
import j.core.sys.SysUtil;
import j.util.JUtilBean;

@ClassDescription(author = "肖炯",
        date = "2021/10/27",
        description = "登录Token保存到数据库并在启动是尝试从数据库加载")
public class SSOTokens implements Runnable{
    //日志
    private static Logger log=Logger.create(SSOTokens.class);

    private ThreadPool savePool=null;
    private ThreadPool deletePool=null;
    private ThreadPool loadPool=null;

    public SSOTokens(){
    }

    /**
     *
     */
    private void getPools(){
        savePool = ThreadManager.getPool("SSOTokenSaver",10,10,3600000*24L);
        deletePool = ThreadManager.getPool("SSOTokenDeleter",10,10,3600000*24L);
        loadPool = ThreadManager.getPool("SSOTokenLoader",10,10,3600000*24L);
    }

    /**
     * 保存token
     * @param login
     */
    public void save(JuserLogin login){
        log.log("保存token到数据库 -> "+ JUtilBean.bean2Json(login), -1);
        getPools();
        savePool.addTask(new SSOTokenSaver(new Object[]{login}, 0));
    }

    /**
     * 删除token
     * @param session
     */
    public void delete(SSOSession session){
        log.log("从数据库删除token -> "+ JUtilBean.bean2Json(session), -1);
        getPools();
        deletePool.addTask(new SSOTokenDeleter(new Object[]{session}, 0));
    }

    /**
     * 从数据库加载
     */
    public void load(){
        if(!SSOConfig.isServer()) return;

        getPools();

        DAO dao=null;
        try{
            Thread.sleep(60000L);

            String query="login_time_ok > "+(SysUtil.getNow() - SSOConfig.getSessionTimeout()*1000L);

            Database db=DB.getJFrameworkDB();

            dao= DB.connect(db.getName(), this.getClass());
            int cnt=dao.getRecordCnt("j_user_login", "login_time_ok > "+(SysUtil.getNow() - SSOConfig.getSessionTimeout()*1000L));
            dao.close();
            dao=null;

            if(cnt==0) return;

            for(int i=0; i<cnt; i+=1000){
                loadPool.addTask(new SSOTokenLoader(new Object[]{Integer.valueOf(1000), Integer.valueOf(i+1), query}, 0));
            }
        }catch (Exception e){
            log.log("加载sso tokens失败，框架数据库 -> "+DB.getJFrameworkDB().getName(), Logger.LEVEL_ERROR);
            log.log(e, Logger.LEVEL_ERROR);
            if(dao != null){
                try{
                    dao.close();
                    dao=null;
                }catch(Exception ex){}
            }
        }
    }

    @Override
    public void run() {
        if(!SSOConfig.isServer()) return;//不是SSO服务器
        try{
            load();
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }

        while(!Startup.isDestroyed()){
            try{
                Thread.sleep(15000L);
            }catch(Exception e){}

            if(Startup.isDestroyed()){
                return;
            }

            DAO dao=null;
            try{
                Database db=DB.getJFrameworkDB();

                dao= DB.connect(db.getName(), this.getClass());
                dao.executeSQL("delete from j_user_login where login_time_ok < "+(SysUtil.getNow() - SSOConfig.getSessionTimeout()*1000L));
                dao.close();
                dao=null;
            }catch(Exception e){
                log.log(e, Logger.LEVEL_ERROR);
                if(dao != null){
                    try{
                        dao.close();
                        dao=null;
                    }catch(Exception ex){}
                }
            }

            try{
                Thread.sleep(300000L);
            }catch(Exception e){}
        }
    }
}