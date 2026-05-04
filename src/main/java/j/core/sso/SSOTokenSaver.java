package j.core.sso;

import j.core.annotation.description.ClassDescription;
import j.core.dao.*;
import j.core.db.JuserLogin;
import j.core.hp.thread.ThreadTask;
import j.util.JUtilString;

@ClassDescription(author = "肖炯",
        date = "2021/10/27",
        description = "用于线程池异步保存Token")
public class SSOTokenSaver extends ThreadTask {
    private DAO dao=null;

    public SSOTokenSaver(Object[] in, int retries) {
        super(in, retries);
    }

    public SSOTokenSaver(Object[] in, int retries, String uuid) {
        super(in, retries, uuid);
    }

    public SSOTokenSaver(Object[] in, int retries, String uuid, long resultTimeout) {
        super(in, retries, uuid, resultTimeout);
    }

    private void insureDAO(){
        try{
            if(this.dao == null || this.dao.isClosed()){
                Database db = DB.getJFrameworkDB();
                this.dao = DAOs.create(db.getName(), this.getClass(), true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object[] execute() throws Exception {
        JuserLogin login=(JuserLogin)this.getIn()[0];
        
        this.insureDAO();

        JuserLogin exists=null;
        if(JUtilString.isBlank(login.getSubUserId())){
            exists=(JuserLogin)this.dao.findSingle("j_user_login", "user_id='"+login.getUserId()+"' and sub_user_id is null and user_agent_sn='"+login.getUserAgentSn()+"'");
        }else{
            exists=(JuserLogin)this.dao.findSingle("j_user_login", "user_id='"+login.getUserId()+"' and sub_user_id='"+login.getSubUserId()+"' and user_agent_sn='"+login.getUserAgentSn()+"'");
        }

        if(exists!=null){
            login.setUuid(exists.getUuid());
            this.dao.updateByKeys(login);
        }else{
            this.dao.insert(login);
        }

        return new Object[]{login};
    }

    @Override
    public boolean equalz(ThreadTask other) {
        return false;
    }
}
