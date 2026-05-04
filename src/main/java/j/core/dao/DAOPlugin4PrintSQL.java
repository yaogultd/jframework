package j.core.dao;

import j.core.dao.util.SQLUtil;
import j.log.Logger;

import java.util.Map;

/**
 * DAOPlugin实现范例，用于输出sql
 * @author 肖炯
 *
 */
public class DAOPlugin4PrintSQL implements DAOPlugin{
	private static Logger log=Logger.create(DAOPlugin4PrintSQL.class);
	private DAOFactory fac;

	@Override
	public void setFactory(DAOFactory fac) {
		this.fac=fac;
	}

	@Override
	public void onBeginTransaction() throws Exception {
		
	}

	@Override
	public void onRollback() throws Exception{
		
	}

	@Override
	public void onCommit() throws Exception {
		
	}

	@Override
	public void beforeInsert(Object vo) throws Exception {
		String sql=SQLUtil.retrieveInsertSQL(vo,fac);
		if(fac.isShowSql()) log.log("before insert(Object vo) - "+sql, -1);
	}

	@Override
	public void afterInsert(Object vo) throws Exception {
		String sql=SQLUtil.retrieveInsertSQL(vo,fac);
		if(fac.isShowSql()) log.log("after insert(Object vo) - "+sql, -1);
	}

	@Override
	public void beforeUpdate(String tblName, Map colsBeUpdated, String condition) throws Exception {
		String sql=SQLUtil.retrieveUpdateSQL(tblName,colsBeUpdated,condition);
		if(fac.isShowSql()) log.log("before update(String tblName,Map colsBeUpdated,String condition) - "+sql, -1);
	}

	@Override
	public void afterUpdate(String tblName, Map colsBeUpdated, String condition) throws Exception {
		String sql=SQLUtil.retrieveUpdateSQL(tblName,colsBeUpdated,condition);
		if(fac.isShowSql()) log.log("after update(String tblName,Map colsBeUpdated,String condition) - "+sql,-1);
	}

	@Override
	public void beforeUpdateByKeys(Object vo, String[] conditionKeys) throws Exception {
		String sql=SQLUtil.retrieveUpdateSQL(vo,conditionKeys,fac);
		if(fac.isShowSql()) log.log("before updateByKeys(Object vo,String[] conditionKeys) - "+sql,-1);
	}

	@Override
	public void afterUpdateByKeys(Object vo, String[] conditionKeys) throws Exception {
		String sql=SQLUtil.retrieveUpdateSQL(vo,conditionKeys,fac);
		if(fac.isShowSql()) log.log("after updateByKeys(Object vo,String[] conditionKeys) - "+sql,-1);
	}

	@Override
	public void beforeUpdateByKeysIgnoreNulls(Object vo, String[] conditionKeys) throws Exception {
		String sql=SQLUtil.retrieveUpdateSQLIgnoreNulls(vo,conditionKeys,fac);
		if(fac.isShowSql()) log.log("before updateByKeysIgnoreNulls(Object vo,String[] conditionKeys) - "+sql,-1);		
	}

	@Override
	public void afterUpdateByKeysIgnoreNulls(Object vo, String[] conditionKeys) throws Exception {
		String sql=SQLUtil.retrieveUpdateSQLIgnoreNulls(vo,conditionKeys,fac);
		if(fac.isShowSql()) log.log("after updateByKeysIgnoreNulls(Object vo,String[] conditionKeys) - "+sql,-1);
	}


	@Override
	public void beforeExecuteSQL(String sql) throws Exception {
		if(fac.isShowSql()) log.log("before executeSQL(String sql) - "+sql,-1);	
	}

	@Override
	public void afterExecuteSQL(String sql) throws Exception {
		if(fac.isShowSql()) log.log("after executeSQL(String sql) - "+sql,-1);			
	}

	@Override
	public void find(String tableName, String condition, int RPP, int PN) throws Exception{
		if(fac.isShowSql()) log.log("find(String tableName, String condition, int RPP, int PN) - "+tableName+","+condition+","+RPP+","+PN,-1);
	}


	@Override
	public void destroy() {		
	}
}
