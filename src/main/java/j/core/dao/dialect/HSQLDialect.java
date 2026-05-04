package j.core.dao.dialect;

import j.core.dao.RdbmsDao;


/**
 * @author 肖炯
 *
 */
public class HSQLDialect extends RdbmsDao{
	/**
	 * 
	 *
	 */
	public HSQLDialect() {
		super();
	}

	@Override
	protected String getSQLWithRowSetLimit(String sql,int start, int end) {
		String tmpSql=sql.toLowerCase();
		int point=tmpSql.indexOf("select")+6;
		String limit=" limit ";
		if(start<0){
			start=0;
		}
		if(end<0){
			end=0;
		}
		limit+=start+" "+(end-start)+" ";
		return sql.substring(0,point)+limit+sql.substring(point);
	}

	@Override
	protected boolean supportsLimitOffset() {
		return true;
	}
}
