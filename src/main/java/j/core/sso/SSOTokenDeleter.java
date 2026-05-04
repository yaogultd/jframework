package j.core.sso;

import j.core.annotation.description.ClassDescription;
import j.core.dao.DB;
import j.core.dao.QueryPool;
import j.core.hp.thread.ThreadTask;

@ClassDescription(author = "肖炯",
        date = "2021/10/27",
        description = "用于线程池异步保存Token")
public class SSOTokenDeleter extends ThreadTask {
    public SSOTokenDeleter(Object[] in, int retries) {
        super(in, retries);
    }

    public SSOTokenDeleter(Object[] in, int retries, String uuid) {
        super(in, retries, uuid);
    }

    public SSOTokenDeleter(Object[] in, int retries, String uuid, long resultTimeout) {
        super(in, retries, uuid, resultTimeout);
    }

    @Override
    public Object[] execute() throws Exception {
        SSOSession session=(SSOSession)this.getIn()[0];
        QueryPool pool=QueryPool.getPool(DB.getJFrameworkDB().getName(), QueryPool.COMMON_POOL, QueryPool.COMMON_POOL_SIZE);

        pool.execute(null, "delete from j_user_login where access_token='"+session.getAccessToken()+"'");

        return new Object[]{session};
    }

    @Override
    public boolean equalz(ThreadTask other) {
        return false;
    }
}
