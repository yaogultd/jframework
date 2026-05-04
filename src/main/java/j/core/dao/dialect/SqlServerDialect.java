package j.core.dao.dialect;

import j.core.dao.RdbmsDao;

/**
 * @author 肖炯
 *
 */
public class SqlServerDialect  extends RdbmsDao {
	/**
	 * 
	 *
	 */
	public SqlServerDialect() {
		super();
	}

	@Override
	protected String getSQLWithRowSetLimit(String sql, int start, int end) {
		StringBuffer pageSelectSQL=new StringBuffer("");
		pageSelectSQL.append(sql);
		pageSelectSQL.insert( getAfterSelectInsertPoint(sql), " top " + end);
		/*
		pageSelectSQL.append(" as row_");
		pageSelectSQL.insert(0,"select top "+(end-start)+" row_.* from (");

		SELECT TOP 页大小 * 
		FROM TestTable 
		WHERE (ID NOT IN 
		(SELECT TOP 页大小*页数 id 
		FROM 表 
		ORDER BY id)) 
		ORDER BY ID 
   	    */
		return pageSelectSQL.toString();
	}

	@Override
	protected boolean supportsLimitOffset() {
		return false;
	}
	
	/**
	 * 
	 * @param sql
	 * @return int
	 */
	private int getAfterSelectInsertPoint(String sql) {
		return sql.startsWith("select distinct") ? 15 : 6;
	}
}
