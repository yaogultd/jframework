package j.core.dao;

import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public interface DAOPlugin {
	public void setFactory(DAOFactory fac);	

	public void onBeginTransaction() throws Exception;	
	public void onCommit() throws Exception;
	public void onRollback() throws Exception;

	public void beforeInsert(Object vo)throws Exception;
	public void afterInsert(Object vo)throws Exception;

	public void beforeUpdate(String tblName,Map colsBeUpdated,String condition)throws Exception;
	public void afterUpdate(String tblName,Map colsBeUpdated,String condition)throws Exception;

	public void beforeUpdateByKeys(Object vo,String[] conditionKeys)throws Exception;
	public void afterUpdateByKeys(Object vo,String[] conditionKeys)throws Exception;
	

	public void beforeUpdateByKeysIgnoreNulls(Object vo,String[] conditionKeys)throws Exception;
	public void afterUpdateByKeysIgnoreNulls(Object vo,String[] conditionKeys)throws Exception;

	public void beforeExecuteSQL(String sql)throws Exception;
	public void afterExecuteSQL(String sql)throws Exception;

	public void find(String tableName, String condition, int RPP, int PN)throws Exception;

	public void destroy();
}
