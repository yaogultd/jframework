package j.core.dao;

/**
 *
 */
public class DAOs {
    /**
     *
     * @param cls
     * @return
     * @throws Exception
     */
    public static DAO create(Class cls) throws Exception{
        return create(null, cls, false);
    }

    /**
     *
     * @param dbId
     * @param cls
     * @return
     * @throws Exception
     */
    public static DAO create(String dbId, Class cls) throws Exception{
        return create(dbId, cls, false);
    }

    /**
     *
     * @param cls
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public static DAO create(Class cls, boolean autoCommit) throws Exception{
        return create(null, cls, autoCommit);
    }

    /**
     *
     * @param dbId
     * @param cls
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public static DAO create(String dbId, Class cls, boolean autoCommit) throws Exception{
        return create(dbId, cls, autoCommit, 300000L);
    }

    /**
     *
     * @param dbId
     * @param cls
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public static DAO create(String dbId, Class cls, boolean autoCommit, long timeout) throws Exception{
        DAO dao=DB.connect(dbId, cls, timeout);
        if(!autoCommit) dao.beginTransaction();
        return dao;
    }

    /**
     *
     * @param dao
     * @throws Exception
     */
    public static void commit(DAO dao) throws Exception{
        if(dao.isInTransaction()) dao.commit();
        dao.close();
        dao=null;
    }

    /**
     *
     * @param dao
     */
    public static void close(DAO dao){
        if(dao!=null){
            if(dao.isInTransaction()){
                try{
                    dao.commit();
                }catch (Exception ex){}
            }

            try{
                dao.close();
                dao=null;
            }catch (Exception ex){}
        }
    }

    /**
     *
     * @param dao
     */
    public static void rollback(DAO dao){
        if(dao!=null){
            if(dao.isInTransaction()){
                try{
                    dao.rollback();
                }catch (Exception ex){}
            }

            try{
                dao.close();
                dao=null;
            }catch (Exception ex){}
        }
    }

    /**
     *
     * @param dao
     */
    public static void onException(DAO dao){
        if(dao!=null){
            if(dao.isInTransaction()){
                try{
                    dao.rollback();
                }catch (Exception ex){}
            }

            try{
                dao.close();
                dao=null;
            }catch (Exception ex){}
        }
    }
}
