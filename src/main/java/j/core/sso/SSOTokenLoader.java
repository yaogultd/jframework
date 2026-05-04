package j.core.sso;

import j.core.annotation.description.ClassDescription;
import j.core.dao.DB;
import j.core.dao.QueryPool;
import j.core.db.JuserLogin;
import j.core.hp.thread.ThreadTask;

import java.util.List;

@ClassDescription(author = "肖炯",
        date = "2021/10/27",
        description = "用于线程池异步保存Token")
public class SSOTokenLoader extends ThreadTask {
    public SSOTokenLoader(Object[] in, int retries) {
        super(in, retries);
    }

    public SSOTokenLoader(Object[] in, int retries, String uuid) {
        super(in, retries, uuid);
    }

    public SSOTokenLoader(Object[] in, int retries, String uuid, long resultTimeout) {
        super(in, retries, uuid, resultTimeout);
    }

    @Override
    public Object[] execute() throws Exception {
        Integer rpp=(Integer)this.getIn()[0];
        Integer pn=(Integer)this.getIn()[1];
        String query=(String)this.getIn()[2];
        QueryPool pool=QueryPool.getPool(DB.getJFrameworkDB().getName(), QueryPool.COMMON_POOL, QueryPool.COMMON_POOL_SIZE);
        List logins=pool.query(null, "j_user_login", query, rpp, pn);
        for(int i=0; i<logins.size(); i++){
            JuserLogin login=(JuserLogin)logins.get(i);
            SSOServer.loadSession(login);
        }
        return new Object[]{"ok"};
    }

    @Override
    public boolean equalz(ThreadTask other) {
        return false;
    }
}
