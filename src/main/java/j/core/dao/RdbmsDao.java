package j.core.dao;

import j.core.cache.JCacheParams;
import j.core.dao.intervener.RowIntervener;
import j.core.dao.type.Blob;
import j.core.dao.type.Clob;
import j.core.dao.util.Methods;
import j.core.dao.util.SQLUtil;
import j.core.hp.reflection.Accessor;
import j.core.hp.reflection.Accessors;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * @author 肖炯
 *
 */
public class RdbmsDao implements DAO {
	private static Logger log=Logger.create(RdbmsDao.class);

	private static final long timeoutDefault = 300000;

	protected TimeZone timeZone=null;
	protected DAOFactory factory;//工厂，包含数据库配置信息等
	protected DBMirror mirror;//使用哪个镜像
	protected Connection connection;//数据库连接
	protected volatile boolean autoCommit;//是否自动提交
	protected volatile boolean closed;//是否已经关闭

	protected long createTime;//创建时间
	protected volatile long lastUsingTime=-1;//最近一次使用时间
	protected volatile boolean using=false;//是否处于操作中
	protected long timeout=120000;//超时，默认2分钟
	protected String caller=null;//创建此对象的类的名字
	protected volatile boolean pluginEnabled=true;
	protected volatile boolean readonly=false;
	protected volatile long lastTest=SysUtil.getNow();
	protected RowIntervener intervener;
	protected Object[] runtimeDatas;

	/**
	 * 初始化
	 *
	 */
	public RdbmsDao(){
		autoCommit=true;
		closed=false;
		lastUsingTime=SysUtil.getNow();
		createTime=lastUsingTime;
		using=false;
	}

	@Override
	public void setConnection(Connection connection){
		this.connection=connection;
	}

	@Override
	public void setFactory(DAOFactory factory){
		this.factory=factory;
	}

	@Override
	public void setMirror(DBMirror mirror){
		this.mirror=mirror;
	}

	@Override
	public long getCreateTime(){
		return createTime;
	}

	@Override
	public long getLastTest(){
		return this.lastTest;
	}

	@Override
	public void setLastTest(long time){
		this.lastTest=time;
	}

	@Override
	public boolean getReadOnly(){
		return this.readonly;
	}

	@Override
	public void setReadOnly(boolean readonly){
		this.readonly=readonly;
	}

	@Override
	public DBMirror getMirror(){
		return this.mirror;
	}

	@Override
	public String getCaller(){
		return caller;
	}

	@Override
	public void setCaller(String caller){
		this.caller=caller;
	}

	@Override
	public long getTimeout(){
		return timeout;
	}

	@Override
	public void setTimeout(long timeout){
		this.timeout=(timeout<=0) ? timeoutDefault : timeout;
	}

	@Override
	public long getLastUsingTime(){
		return lastUsingTime;
	}

	@Override
	public boolean isUsing(){
		return using;
	}

	@Override
	public void begin(){
		lastUsingTime=SysUtil.getNow();
		using=true;
	}

	@Override
	public void finish(){
		lastUsingTime=SysUtil.getNow();
		using=false;
	}

	@Override
	public void beginTransaction() throws Exception {
		if(!autoCommit) return;
		autoCommit=false;
		connection.setAutoCommit(false);
		if(factory.getPlugin()!=null&&this.pluginEnabled){
			factory.getPlugin().onBeginTransaction();
		}
	}

	@Override
	public boolean isInTransaction(){
		//是否处于事务状态
		return !autoCommit;
	}

	@Override
	public void commit() throws Exception {
		if(autoCommit) return;
		connection.commit();
		connection.setAutoCommit(true);
		autoCommit=true;
		if(factory.getPlugin()!=null&&this.pluginEnabled){
			factory.getPlugin().onCommit();
		}
	}

	@Override
	public void rollback() throws Exception {
		if(autoCommit) return;
		connection.rollback();
		connection.setAutoCommit(true);
		autoCommit=true;
		if(factory.getPlugin()!=null&&this.pluginEnabled){
			factory.getPlugin().onRollback();
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close()throws Exception{
		if(isInTransaction()){//如果在事务中，尝试回滚并结束事务
			try{
				rollback();
			}catch(Exception e){}
		}

		try{
			factory.close(this,connection);
		}catch(Exception e){}

		closed=true;
		using=false;
	}

	@Override
	public void beforeAnyInvocation() throws Exception {

	}

	@Override
	public void afterAnyInvocation() throws Exception {

	}

	@Override
	public void onException(){
		try{
			rollback();
		}catch(Exception e){}
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public DAOFactory getFactory(){
		return factory;
	}

	/**
	 *
	 * @param sql
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	protected String getSQLWithRowSetLimit(String sql, int start, int end) throws Exception{
		throw new Exception("由Dialect实现");
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	protected boolean supportsLimitOffset() throws Exception{
		throw new Exception("由Dialect实现");
	}

	@Override
	public StmtAndRs find(String sql) throws Exception {
		return find(sql,0,0);
	}

	@Override
	public StmtAndRs find(String sql,int RPP, int PN) throws Exception {
		return findScale(sql,RPP*(PN-1),RPP*PN);
	}

	@Override
	public StmtAndRs findScale(String sql,int start, int end) throws Exception {
		if(SQLUtil.sqlInjection(sql)!=null) return null;

		Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = null;
		try {
			if(start<0||end<=0||start>=end){
				//throw new Exception("指定的范围的参数小于零");
				//throw new Exception("指定的范围的start 大于等于 end");
				rs = stmt.executeQuery(sql);
			}else{
				stmt.setMaxRows(end-start);
				sql=getSQLWithRowSetLimit(sql,start,end);

				rs = stmt.executeQuery(sql);
				if(!supportsLimitOffset()){//不支持分页
					try{
						rs.absolute(start);
					}catch(Exception ex){
						throw ex;
					}
				}
			}

			return new StmtAndRs(stmt,rs,sql);
		} catch (Exception e) {
			try{
				rs.close();
			}catch(Exception ex){}
			try{
				stmt.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public List find(String sql,Class cls,String excludedColumns)throws Exception{
		return find(sql,cls,excludedColumns,0,0);
	}

	@Override
	public List find(String sql,Class cls,List<String> excludedColumns)throws Exception{
		return find(sql,cls,excludedColumns,0,0);
	}

	@Override
	public List find(String sql,Class cls,String excludedColumns,int RPP,int PN)throws Exception{
		return findScale(sql,cls,excludedColumns,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List find(String sql,Class cls,List<String> excludedColumns,int RPP,int PN)throws Exception{
		return findScale(sql,cls,excludedColumns,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List findScale(String sql,Class cls,String excludedColumns,int start,int end)throws Exception{
		if(excludedColumns==null || "".equals(excludedColumns)) {
			return findScale(sql,cls,(List)null,start,end);
		}else {
			//excludedColumns 某些不读取的列，格式：{列名1}{列名2}
			List<String> _excludedColumns=new ArrayList();
			String[] temp=JUtilString.getTokens(excludedColumns, "}{");
			for(int i=0; i<temp.length; i++) {
				if(temp[i].startsWith("{")) temp[i]=temp[i].substring(1);
				if(temp[i].endsWith("}")) temp[i]=temp[i].substring(temp[i].length()-1);
				_excludedColumns.add(temp[i]);
			}
			return findScale(sql,cls,_excludedColumns,start,end);
		}
	}

	@Override
	public List findScale(String sql,Class cls,List<String> excludedColumns,int start,int end)throws Exception{
		if(SQLUtil.sqlInjection(sql)!=null) return null;

		StmtAndRs sr=null;
		try {
			sr=findScale(sql,start,end);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs = sr.resultSet();
			List results=new ArrayList();
			Field[] fields=cls.getDeclaredFields();
			String tableName=SQLUtil.retrieveTableNameFromSQL(sql);
			tableName=factory.getTrueTblName(tableName);

			Map<String, String> setterNameCache=new HashMap();
			Map<String, Integer> colTypeCache=new HashMap();
			Map<String, String> colNameCache=new HashMap();
			Map<String, Boolean> isGzipCache=new HashMap();
			for(int i=0;i<fields.length;i++){
				String fieldName=fields[i].getName();
				String colName=factory.getColName(tableName,fieldName);
				if(excludedColumns!=null
						&& (excludedColumns.contains(fieldName) || excludedColumns.contains(colName))){
					continue;
				}

				setterNameCache.put(fieldName, factory.getUnregisterSetterName(fieldName));
				colTypeCache.put(fieldName, factory.getColType(tableName, fieldName));
				colNameCache.put(fieldName, colName);
				isGzipCache.put(fieldName, factory.getColIsGzip(tableName,fieldName));
			}

			Accessor accessor = Accessors.getAccessor(cls);
			while(rs!=null&&rs.next()){
				Object object=accessor.newObject();

				for(int i=0;i<fields.length;i++){
					String fieldName=fields[i].getName();
					factory.getColName(tableName, fieldName);

					//Method setter=setterCache.get(fieldName);
					//if(setter==null) continue;

					String setterName = setterNameCache.get(fieldName);
					if(setterName==null) continue;

					Object obj=null;
					try{
						obj = getObject(rs,
								colTypeCache.get(fieldName),
								colNameCache.get(fieldName),
								isGzipCache.get(fieldName));
					}catch(Exception e){
						//log.log("fieldName:"+fieldName,Logger.LEVEL_DEBUG);
						//log.log(e,Logger.LEVEL_DEBUG);
					}
					if(obj!=null) accessor.invokeMethod(object, setterName, obj);
					//setter.invoke(object,obj);
				}
				results.add(object);
			}

			sr.close();

			return results;
		} catch (Exception e) {
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public Object findSingle(String sql,Class cls,String excludedColumns)throws Exception{
		List lst=findScale(sql,cls,excludedColumns,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public Object findSingle(String sql,Class cls,List<String> excludedColumns)throws Exception{
		List lst=findScale(sql,cls,excludedColumns,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public List find(String tableName, String condition) throws Exception {
		return find(tableName,condition,0,0);
	}

	@Override
	public List find(String tableName, String condition, List<String> excludedColumns) throws Exception {
		return find(tableName,condition,excludedColumns,0,0);
	}

	@Override
	public List find(String tableName, String condition, int RPP, int PN) throws Exception {
		if(factory.getPlugin()!=null&&pluginEnabled){
			factory.getPlugin().find(tableName, condition, RPP, PN);
		}
		return findScale(tableName,condition,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List find(String tableName, String condition,List<String> excludedColumns, int RPP, int PN) throws Exception {
		return findScale(tableName,condition,excludedColumns,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List findScale(String tableName, String condition, int start, int end) throws Exception {
		return findScale(tableName,condition,(List)null,start,end);
	}

	@Override
	public List findScale(String tableName, String condition,List<String> excludedColumns, int start, int end) throws Exception {
		List results=new ArrayList();
		StmtAndRs sr=null;
		try{
			if (tableName == null || tableName.trim().equals("")) {
				throw new Exception("没有指定表名或指定的表名为空");
			}
			tableName=factory.getTrueTblName(tableName);

			String tblClass=factory.getTblClass(tableName);
			if(tblClass==null){
				throw new Exception("class of table "+factory.getDbName()+"."+tableName+" not found");
			}
			Class cls=Class.forName(tblClass);

			List cols=factory.getColumns(tableName);

			Map<String, String> setterNameCache=new HashMap();
			Map<String, Integer> colTypeCache=new HashMap();
			Map<String, Boolean> isGzipCache=new HashMap();
			for(int i=0;i<cols.size();i++){
				String colName=((Column)cols.get(i)).colName;
				String fieldName=((Column)cols.get(i)).fieldName;

				if(excludedColumns!=null
						&&(excludedColumns.contains(colName)
						|| excludedColumns.contains(fieldName))) continue;

				setterNameCache.put(colName, factory.getSetterName(tableName, colName));
				colTypeCache.put(colName, factory.getColType(tableName, colName));
				isGzipCache.put(colName, factory.getColIsGzip(tableName,colName));
			}

			StringBuilder sb = new StringBuilder();
			sb.append("select * from ").append(tableName);
			if(condition != null && condition.trim().length() >= 3){
				String tmpcondition = condition;
				tmpcondition = tmpcondition.toLowerCase();
				tmpcondition = JUtilString.replaceAll(tmpcondition, " ", "");
				if(tmpcondition.indexOf("groupby")!=-1){
					throw new Exception("该方法不支持 GROUP BY 子句，请直接使用find(String sql) 或 find(String sql,int rpp,int pn)");
				}

				if(tmpcondition.startsWith("orderby")){
					sb.append(" ").append(condition);
				} else{
					sb.append(" where ").append(condition);
				}
			}

			sr=findScale(sb.toString(), start, end);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sb);

			ResultSet rs = sr.resultSet();
			Accessor accessor=Accessors.getAccessor(cls);
			while(rs.next()){
				Object object=accessor.newObject();
				for(int i=0;i<cols.size();i++){
					String colName=((Column)cols.get(i)).colName;

					//Method setter=setterCache.get(colName);
					//if(setter==null) continue;

					String setterName = setterNameCache.get(colName);
					if(setterName==null) continue;

					Object obj=null;
					try{
						obj = getObject(rs,
								colTypeCache.get(colName),
								colName,
								isGzipCache.get(colName));
					}catch(Exception e){
						//log.log("fieldName:"+fieldName,Logger.LEVEL_DEBUG);
						//log.log(e,Logger.LEVEL_DEBUG);
					}
					if(obj!=null) accessor.invokeMethod(object, setterName, obj);
					//setter.invoke(object, obj);
				}
				results.add(object);
			}
			sr.close();

			return results;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public Object findSingle(String tableName, String condition) throws Exception {
		List lst=findScale(tableName,condition,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public Object findSingle(String tableName, String condition,List<String> excludedColumns) throws Exception {
		List lst=findScale(tableName,condition,excludedColumns,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public List find(String tableName, String condition,Class cls) throws Exception {
		return find(tableName,condition,cls,0,0);
	}

	@Override
	public List find(String tableName, String condition,Class cls,List<String> excludedColumns) throws Exception {
		return find(tableName,condition,cls,excludedColumns,0,0);
	}

	@Override
	public List find(String tableName, String condition,Class cls, int RPP, int PN) throws Exception {
		return findScale(tableName,condition,cls,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List find(String tableName, String condition,Class cls,List<String> excludedColumns, int RPP, int PN) throws Exception {
		return findScale(tableName,condition,cls,excludedColumns,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List findScale(String tableName, String condition, Class cls, int start, int end) throws Exception {
		return findScale(tableName, condition, cls,(List)null, start, end);
	}

	@Override
	public List findScale(String tableName, String condition, Class cls,List<String> excludedColumns, int start, int end) throws Exception {
		List results=new ArrayList();
		StmtAndRs sr=null;
		try{
			if (tableName == null || tableName.trim().equals("")) {
				throw new Exception("没有指定表名或指定的表名为空");
			}
			tableName=factory.getTrueTblName(tableName);

			//Map<String, Method> setterCache=new HashMap();
			Map<String, String> setterNameCache=new HashMap();
			Map<String, Integer> colTypeCache=new HashMap();
			Map<String, Boolean> isGzipCache=new HashMap();

			List cols=factory.getColumns(tableName);
			for(int i=0;i<cols.size();i++){
				String colName=((Column)cols.get(i)).colName;
				String fieldName=((Column)cols.get(i)).fieldName;

				if(excludedColumns!=null
						&& (excludedColumns.contains(fieldName) || excludedColumns.contains(colName))){
					continue;
				}

				//Method setter=factory.getSetter(tableName,colName);
				//if(setter==null){
				//	continue;
				//}

				//setter.setAccessible(true);
				//setterCache.put(colName, setter);

				setterNameCache.put(colName, factory.getSetterName(tableName, colName));
				colTypeCache.put(colName, factory.getColType(tableName, colName));
				isGzipCache.put(colName, factory.getColIsGzip(tableName,colName));
			}

			StringBuilder sb = new StringBuilder();
			sb.append("select * from ").append(tableName);
			if(condition != null && condition.trim().length() >= 3){
				String tmpcondition = condition;
				tmpcondition = tmpcondition.toLowerCase();
				tmpcondition = JUtilString.replaceAll(tmpcondition, " ", "");
				if(tmpcondition.indexOf("groupby")!=-1){
					throw new Exception("该方法不支持 GROUP BY 子句，请直接使用find(String sql) 或 find(String sql,int rpp,int pn)");
				}

				if(tmpcondition.startsWith("orderby")){
					sb.append(" ").append(condition);
				} else{
					sb.append(" where ").append(condition);
				}
			}

			sr=findScale(sb.toString(), start, end);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sb);

			ResultSet rs=sr.resultSet();
			Accessor accessor = Accessors.getAccessor(cls);
			while(rs!=null&&rs.next()){
				Object object=accessor.newObject();

				for(int i=0;i<cols.size();i++){
					String colName=((Column)cols.get(i)).colName;

					//Method setter=setterCache.get(fieldName);
					//if(setter==null) continue;

					String setterName=setterNameCache.get(colName);
					if(setterName==null) continue;

					Object obj=null;
					try{
						obj = getObject(rs,
								colTypeCache.get(colName),
								colName,
								isGzipCache.get(colName));
					}catch(Exception e){
						//log.log("fieldName:"+fieldName,Logger.LEVEL_DEBUG);
						//log.log(e,Logger.LEVEL_DEBUG);
					}

					if(obj!=null) accessor.invokeMethod(object, setterName, obj);
					//if(obj!=null) setter.invoke(object, obj);
				}
				results.add(object);
			}
			sr.close();
			return results;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public Object findSingle(String tableName, String condition, Class cls) throws Exception {
		List lst=findScale(tableName,condition,cls,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public Object findSingle(String tableName, String condition, Class cls,List<String> excludedColumns) throws Exception {
		List lst=findScale(tableName,condition,cls,excludedColumns,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public List find(String[] tableNames, String condition)throws Exception{
		return find(tableNames,condition,0,0);
	}

	@Override
	public List find(String[] tableNames, String condition, int RPP, int PN) throws Exception {
		return findScale(tableNames,condition,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List findScale(String[] tableNames, String condition, int start, int end) throws Exception {
		List results=new ArrayList();
		StmtAndRs sr=null;
		try{
			if (tableNames == null || tableNames.length==0) {
				throw new Exception("多表查询，没有指定操作的表");
			}

			if(tableNames.length==1){
				return findScale(tableNames[0],condition,start,end);
			}

			if(condition==null||condition.trim().equals("")){
				throw new Exception("多表查询必须指定查询条件");
			}

			for(int i=0;i<tableNames.length;i++){
				if(tableNames[i]==null||tableNames[i].trim().equals("")){
					throw new Exception("指定了一个或多个为空的表名");
				}
			}

			//处理表名
			for(int i=0;i<tableNames.length;i++){
				String trueTblName=factory.getTrueTblName(tableNames[i]);
				condition=JUtilString.replaceAll(condition,tableNames[i]+".",trueTblName+".");
				tableNames[i]=trueTblName;
			}

			Map<String, Integer> allColsIndex=new HashMap();
			Map<String, Integer> allColsType=new HashMap();
			Map<String, Boolean> allColsIsGzip=new HashMap();

			//记住各表各列在结果集中的位置，同时生成sql
			int index=1;
			StringBuilder sql=new StringBuilder("select ");
			StringBuilder sql1=new StringBuilder(" from ");
			for(int i=0;i<tableNames.length;i++){
				sql1.append(tableNames[i]).append(",");
				List cols=factory.getColumns(tableNames[i]);
				for(int j=0;j<cols.size();j++){
					Column col=(Column)cols.get(j);
					sql.append(tableNames[i]).append(".").append(col.colName).append(" AS C").append(index).append(",");
					allColsIndex.put(tableNames[i]+"."+col.colName,Integer.valueOf(index));
					allColsType.put(tableNames[i]+"."+col.colName,Integer.valueOf(col.colType));
					allColsIsGzip.put(tableNames[i]+"."+col.colName,Boolean.valueOf(col.gzip));
					index++;
				}
			}
			sql.deleteCharAt(sql.length() - 1);
			sql1.deleteCharAt(sql1.length() - 1);
			sql.append(sql1);
			sql1=null;

			String order="";
			String tmpcondition=condition;

			condition=condition.toUpperCase();
			if(condition!=null&&condition.trim().length()>=3){
				int groupbyIndex=condition.indexOf("GROUP BY");
				if(groupbyIndex!=-1){
					throw new Exception("该方法不支持 GROUP BY 子句，请直接使用find(String sql) 或 find(String sql,int rpp,int pn)");
				}

				int orderbyIndex=condition.indexOf("ORDER BY");
				if(orderbyIndex!=-1){
					order=condition.substring(orderbyIndex);

					tmpcondition=tmpcondition.substring(0,orderbyIndex);

					for(Iterator keys=allColsIndex.keySet().iterator();keys.hasNext();){
						String tableAndCol=(String)keys.next();
						String as="C"+allColsIndex.get(tableAndCol);
						order=JUtilString.replaceAll(order,tableAndCol.toUpperCase(),as);
					}
				}
				condition=condition.trim();
				if(condition.startsWith("ORDER BY")){
					sql.append(" ").append(tmpcondition).append(order);
				}else{
					sql.append(" where ").append(tmpcondition).append(order);
				}
			}//记住各表各列在结果集中的位置，同时生成sql end

			results=new ArrayList();

			//得到与数据库表名对应的类名
			//log.log("sql:"+sql,Logger.LEVEL_DEBUG);
			sr=findScale(sql.toString(), start, end);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);
			sql = null;

			ResultSet rs=sr.resultSet();
			Class[] classes=new Class[tableNames.length];
			for(int i=0;i<tableNames.length;i++){
				classes[i]=Class.forName(factory.getTblClass(tableNames[i]));
				if(classes[i]==null){
					throw new Exception("class of table "+classes[i]+" not found");
				}
			}//得到与数据库表名对应的类名 end

			List[] colsOfClasses=new ArrayList[classes.length];
			for(int i=0;i<classes.length;i++){
				colsOfClasses[i]=factory.getColumns(factory.getTrueTblNameOfCls(classes[i]));
			}

			Map<String, String> setterNameCache=new HashMap();
			for(int i=0;i<classes.length;i++){
				for(int j=0;j<colsOfClasses[i].size();j++){
					String colName=((Column)colsOfClasses[i].get(j)).colName;
					setterNameCache.put(tableNames[i]+"."+colName, factory.getSetterName(tableNames[i],colName));
				}
			}

			while(rs.next()){
				Object[] objects=new Object[classes.length];
				for(int i=0;i<classes.length;i++){
					Accessor accessor = Accessors.getAccessor(classes[i]);
					objects[i]=accessor.newObject();

					for(int j=0;j<colsOfClasses[i].size();j++){
						String colName=((Column)colsOfClasses[i].get(j)).colName;
						String colNameOfTbl=tableNames[i]+"."+colName;
						//Method setter=setterCache.get(colNameOfTbl);
						//if(setter==null) continue;

						String setterName=setterNameCache.get(colNameOfTbl);
						if(setterName==null) continue;

						Object obj=null;
						try{
							obj=getObject(rs,
									allColsType.get(colNameOfTbl),
									allColsIndex.get(colNameOfTbl),
									allColsIsGzip.get(colNameOfTbl));
						}catch(Exception e){}
						if(obj!=null) accessor.invokeMethod(objects[i], setterName, obj);
						//setter.invoke(objects[i],new Object[]{obj});
					}
				}
				results.add(objects);
			}
			sr.close();
			return results;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public Object findSingle(String[] tableNames, String condition)throws Exception{
		List lst=findScale(tableNames,condition,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public List find(String[] tableNames,Class[] CLSs, String condition)throws Exception{
		return find(tableNames,CLSs,condition,0,0);
	}

	@Override
	public List find(String[] tableNames,Class[] CLSs, String condition, int RPP, int PN) throws Exception {
		return findScale(tableNames,CLSs,condition,RPP*(PN-1),RPP*PN);
	}

	@Override
	public List findScale(String[] tableNames, Class[] CLSs, String condition, int start, int end) throws Exception {
		List results=new ArrayList();
		StmtAndRs sr=null;
		try{
			if (tableNames == null || tableNames.length==0) {
				throw new Exception("多表查询，没有指定操作的表");
			}

			if(tableNames.length==1){
				return findScale(tableNames[0],CLSs[0],condition,start,end);
			}

			if(condition==null||condition.trim().equals("")){
				throw new Exception("多表查询必须指定查询条件");
			}

			for(int i=0;i<tableNames.length;i++){
				if(tableNames[i]==null||tableNames[i].trim().equals("")){
					throw new Exception("指定了一个或多个为空的表名");
				}
			}

			//处理表名
			for(int i=0;i<tableNames.length;i++){
				String trueTblName=factory.getTrueTblName(tableNames[i]);
				condition=JUtilString.replaceAll(condition,tableNames[i]+".",trueTblName+".");
				tableNames[i]=trueTblName;
			}

			Map<String, Integer> allColsIndex=new HashMap();
			Map<String, Integer> allColsType=new HashMap();
			Map<String, Boolean> allColsIsGzip=new HashMap();

			//记住各表各列在结果集中的位置，同时生成sql
			int index=1;
			StringBuilder sql=new StringBuilder("select ");
			StringBuilder sql1=new StringBuilder(" from ");
			for(int i=0;i<tableNames.length;i++){
				sql1.append(tableNames[i]).append(",");
				List cols=factory.getColumns(tableNames[i]);
				for(int j=0;j<cols.size();j++){
					Column col=(Column)cols.get(j);
					sql.append(tableNames[i]).append(".").append(col.colName).append(" AS C").append(index).append(",");
					allColsIndex.put(tableNames[i]+"."+col.colName,Integer.valueOf(index));
					allColsType.put(tableNames[i]+"."+col.colName,Integer.valueOf(col.colType));
					allColsIsGzip.put(tableNames[i]+"."+col.colName,Boolean.valueOf(col.gzip));
					index++;
				}
			}
			sql.deleteCharAt(sql.length() - 1);
			sql1.deleteCharAt(sql1.length() - 1);
			sql.append(sql1);
			sql1=null;

			String order="";
			String tmpcondition=condition;

			condition=condition.toUpperCase();
			if(condition!=null&&condition.trim().length()>=3){
				//int groupbyIndex=condition.indexOf("GROUP BY");
				//if(groupbyIndex!=-1){
				//	throw new Exception("该方法不支持 GROUP BY 子句，请直接使用find(String sql) 或 find(String sql,int rpp,int pn)");
				//}

				int orderbyIndex=condition.indexOf("ORDER BY");
				if(orderbyIndex!=-1){
					order=condition.substring(orderbyIndex);

					tmpcondition=tmpcondition.substring(0,orderbyIndex);

					for(Iterator keys=allColsIndex.keySet().iterator();keys.hasNext();){
						String tableAndCol=(String)keys.next();
						String as="C"+allColsIndex.get(tableAndCol);
						order=JUtilString.replaceAll(order,tableAndCol.toUpperCase(),as);
					}
				}
				condition=condition.trim();
				if(condition.startsWith("ORDER BY")){
					sql.append(" ").append(tmpcondition).append(order);
				}else{
					sql.append(" where ").append(tmpcondition).append(order);
				}
			}//记住各表各列在结果集中的位置，同时生成sql end

			results=new ArrayList();

			//得到与数据库表名对应的类名
			//log.log("sql:"+sql,Logger.LEVEL_DEBUG);
			sr=find(sql.toString(),start,end);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);
			sql=null;

			ResultSet rs=sr.resultSet();

			Class[] classes=CLSs;

			Map[] colsOfClasses=new HashMap[classes.length];
			for(int i=0;i<classes.length;i++){
				colsOfClasses[i]=new HashMap();
				List cols=factory.getColumns(factory.getTrueTblName(tableNames[i]));
				for(int j=0;j<cols.size();j++){
					Column c=(Column)cols.get(j);
					colsOfClasses[i].put(JUtilBean.colNameToVariableName(c.colName),c.colName);
				}
			}

			List fieldsOfClasses=new ArrayList();
			for(int i=0;i<classes.length;i++){
				fieldsOfClasses.add(classes[i].getDeclaredFields());
			}

			Map<String, String> setterNameCache=new HashMap();
			for(int i=0;i<classes.length;i++){
				Field[] fields=(Field[])fieldsOfClasses.get(i);
				for(int j=0;j<fields.length;j++){
					String fieldName=(fields[j]).getName();
					String colName=(String)colsOfClasses[i].get(fieldName);
					if(colName==null) continue;

					setterNameCache.put(tableNames[i]+"."+colName, factory.getSetterName(tableNames[i],colName));
				}
			}

			while(rs.next()){
				Object[] objects=new Object[classes.length];;
				for(int i=0;i<classes.length;i++){
					Accessor accessor = Accessors.getAccessor(classes[i]);
					objects[i]=accessor.newObject();

					Field[] fields=(Field[])fieldsOfClasses.get(i);
					for(int j=0;j<fields.length;j++){
						String fieldName=(fields[j]).getName();
						String colName=(String)colsOfClasses[i].get(fieldName);
						if(colName==null) continue;

						String colNameOfTbl=tableNames[i]+"."+colName;

						//Method setter=setterCache.get(colNameOfTbl);
						//if(setter==null) continue;

						String setterName=setterNameCache.get(colNameOfTbl);
						if(setterName==null) continue;

						Object obj=null;
						try{
							obj=getObject(rs,
									allColsType.get(colNameOfTbl),
									allColsIndex.get(colNameOfTbl),
									allColsIsGzip.get(colNameOfTbl));
						}catch(Exception e){}
						if(obj!=null) accessor.invokeMethod(objects[i], setterName, obj);
						//setter.invoke(objects[i],new Object[]{obj});
					}
				}
				results.add(objects);
			}
			sr.close();
			return results;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public Object findSingle(String[] tableNames, Class[] CLSs, String condition) throws Exception {
		List lst=findScale(tableNames,CLSs,condition,0,1);
		return lst==null||lst.isEmpty()?null:lst.get(0);
	}

	@Override
	public void insert(Object vo) throws Exception{
		String tableName=factory.getTrueTblName(vo);
		insert(tableName,vo);
	}

	@Override
	public void insertIfNotExists(Object vo) throws Exception{
		insertIfNotExists(vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void insertIfNotExists(Object vo,String[] conditionKeys) throws Exception{
		String tableName=factory.getTrueTblName(vo);
		insertIfNotExists(tableName,vo,conditionKeys);
	}

	@Override
	public void insert(String tableName,Object vo) throws Exception{
		PreparedStatement pstmt=null;
		try{
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeInsert(vo);
			}
			if(vo==null){
				throw new Exception("待插入对象为空");
			}
			Class cls=vo.getClass();
			tableName=factory.getTrueTblName(tableName);
			List cols=factory.getColumns(tableName);

			StringBuilder sql=new StringBuilder("insert into "+tableName+"(");
			StringBuilder sqlValues=new StringBuilder(")VALUES(");
			List values=new ArrayList();
			for(int i=0;i<cols.size();i++){
				Column col=(Column)cols.get(i);
				String colName=col.colName;
				Method method=JUtilBean.getGetter(cls,col.fieldName,null);
				Object value=method.invoke(vo,(Object[])null);
				if(value==null){
					sql.append(colName).append(",");
					sqlValues.append("null,");
				}else{
					sql.append(colName).append(",");
					sqlValues.append("?,");
				}
				values.add(i,value);
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(sqlValues);
			sql.deleteCharAt(sql.length() - 1);
			sql.append(")");
			//log.log("JDAO SQL: "+sql,Logger.LEVEL_DEBUG);
			pstmt=connection.prepareStatement(sql.toString());
			sql=null;

			int i=1;
			for(int index=1;index<=cols.size();index++){
				Column col=(Column)cols.get(index-1);
				int    colType=col.colType;
				Object[] paras=null;
				//从vo得到对应字段的值
				Object value=values.get(index-1);
				if(value==null){
					continue;
				}
				if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
					paras=new Object[3];
					InputStream is=(InputStream)value;
					paras[0]=Integer.valueOf(i);
					paras[1]=is;
					paras[2]=Integer.valueOf(is.available());
				}else if(colType==Types.BLOB){
					Blob blob=(Blob)value;
					paras=new Object[2];
					paras[0]=Integer.valueOf(i);
					paras[1]=blob;
				}else if(colType==Types.CLOB){
					Clob clob=(Clob)value;

					paras=new Object[2];
					paras[0]=Integer.valueOf(i);
					paras[1]=clob;
				}else{
					paras=new Object[2];
					paras[0]=Integer.valueOf(i);
					paras[1]=value;
				}
				Methods.set(colType,col.gzip,pstmt,paras);
				i++;
			}

			pstmt.execute();
			pstmt.close();
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().afterInsert(vo);
			}
		}catch(Exception e){
			try{
				pstmt.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public void insertIfNotExists(String tableName,Object vo) throws Exception{
		insertIfNotExists(vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void insertIfNotExists(String tableName,Object vo,String[] conditionKeys) throws Exception{
		PreparedStatement pstmt=null;
		try{
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeInsert(vo);
			}
			if(vo==null){
				throw new Exception("待插入对象为空");
			}
			if(conditionKeys==null||conditionKeys.length==0){
				throw new Exception("没有指定主键");
			}

			Class cls=vo.getClass();
			tableName=factory.getTrueTblName(tableName);

			String condition="";
			for(int i=0;i<conditionKeys.length;i++){
				String colName=factory.getColName(tableName,conditionKeys[i]);

				if(JUtilString.isBlank(conditionKeys[i])){
					throw new Exception("colName not in config -> "+tableName+":"+conditionKeys[i]);
				}

				Method method=JUtilBean.getGetter(cls,JUtilBean.colNameToVariableName(conditionKeys[i]),null);
				Object keyValue=method.invoke(vo,(Object[])null);
				if(keyValue==null){
					condition+=colName+" is null and ";
				}else{
					if(keyValue instanceof Integer||keyValue instanceof Float||keyValue instanceof Double){
						condition+=colName+"="+keyValue+" and ";
					}else{
						condition+=colName+"='"+keyValue+"' and ";
					}
				}
			}
			condition=condition.substring(0,condition.length()-5);
			int exists=getRecordCnt(tableName,condition);
			if(exists>0) return;

			List cols=factory.getColumns(tableName);

			StringBuilder sql=new StringBuilder("insert into "+tableName+"(");
			StringBuilder sqlValues=new StringBuilder(")VALUES(");
			List values=new ArrayList();
			for(int i=0;i<cols.size();i++){
				Column col=(Column)cols.get(i);
				String colName=col.colName;
				Method method=JUtilBean.getGetter(cls,col.fieldName,null);
				Object value=method.invoke(vo,(Object[])null);
				if(value==null){
					sql.append(colName).append(",");
					sqlValues.append("null,");
				}else{
					sql.append(colName).append(",");
					sqlValues.append("?,");
				}
				values.add(i,value);
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(sqlValues);
			sql.deleteCharAt(sql.length() - 1);
			sql.append(")");
			//log.log("JDAO SQL: "+sql,Logger.LEVEL_DEBUG);
			pstmt=connection.prepareStatement(sql.toString());
			sql=null;

			int i=1;
			for(int index=1;index<=cols.size();index++){
				Column col=(Column)cols.get(index-1);
				int    colType=col.colType;
				Object[] paras=null;
				//从vo得到对应字段的值
				Object value=values.get(index-1);
				if(value==null){
					continue;
				}
				if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
					paras=new Object[3];
					InputStream is=(InputStream)value;
					paras[0]=Integer.valueOf(i);
					paras[1]=is;
					paras[2]=Integer.valueOf(is.available());
				}else if(colType==Types.BLOB){
					Blob blob=(Blob)value;
					paras=new Object[2];
					paras[0]=Integer.valueOf(i);
					paras[1]=blob;
				}else if(colType==Types.CLOB){
					Clob clob=(Clob)value;

					paras=new Object[2];
					paras[0]=Integer.valueOf(i);
					paras[1]=clob;
				}else{
					paras=new Object[2];
					paras[0]=Integer.valueOf(i);
					paras[1]=value;
				}
				Methods.set(colType,col.gzip,pstmt,paras);
				i++;
			}

			pstmt.execute();
			pstmt.close();
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().afterInsert(vo);
			}
		}catch(Exception e){
			try{
				pstmt.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public void update(String tableName,Map colsBeUpdated,String condition) throws Exception {
		PreparedStatement pstmt=null;
		try{
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeUpdate(tableName, colsBeUpdated, condition);
			}
			if(colsBeUpdated==null||colsBeUpdated.isEmpty()){
				return;
			}
			tableName=factory.getTrueTblName(tableName);

			//生成sql
			StringBuilder sql=new StringBuilder("update "+tableName+" set ");
			List cols=ConcurrentMap.keys(colsBeUpdated);
			for(int i=0;i<cols.size();i++){
				String colName=(String)cols.get(i);
				Object value=colsBeUpdated.get(colName);
				if(value==null){
					sql.append(factory.getColName(tableName,colName)).append("=null,");
					cols.remove(i);
					i--;
				}else{
					sql.append(factory.getColName(tableName,colName)).append("=?,");
				}
			}
			sql.deleteCharAt(sql.length() - 1);
			if(condition!=null&&condition.trim().length()>=3){
				sql.append(" where ").append(condition);
			}//生成sql end
			//log.log("JDAO SQL: "+sql,Logger.LEVEL_DEBUG);


			if(factory.isSynchronized(tableName)){
				Object lock=factory.getTableLock(tableName);
				synchronized(lock){
					pstmt=connection.prepareStatement(sql.toString());

					int index=1;
					for(int i=0;i<cols.size();i++){
						String colName=(String)cols.get(i);
						Object value=colsBeUpdated.get(colName);

						int colType=factory.getColType(tableName,colName);
						Object[] paras=null;

						if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
							InputStream is=(InputStream)value;
							paras=new Object[3];
							paras[0]=Integer.valueOf(index);
							paras[1]=is;
							paras[2]=Integer.valueOf(is.available());
						}else if(colType==Types.BLOB){
							Blob blob=(Blob)value;
							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=blob;
						}else if(colType==Types.CLOB){
							Clob clob=(Clob)value;

							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=clob;
						}else{
							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=value;
						}
						Methods.set(colType,factory.getColIsGzip(tableName,colName),pstmt,paras);
						index++;
					}

					pstmt.execute();
					pstmt.close();
				}
			}else{
				pstmt=connection.prepareStatement(sql.toString());

				int index=1;
				for(int i=0;i<cols.size();i++){
					String colName=(String)cols.get(i);
					Object value=colsBeUpdated.get(colName);

					int colType=factory.getColType(tableName,colName);
					Object[] paras=null;

					if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
						InputStream is=(InputStream)value;
						paras=new Object[3];
						paras[0]=Integer.valueOf(index);
						paras[1]=is;
						paras[2]=Integer.valueOf(is.available());
					}else if(colType==Types.BLOB){
						Blob blob=(Blob)value;
						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=blob;
					}else if(colType==Types.CLOB){
						Clob clob=(Clob)value;

						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=clob;
					}else{
						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=value;
					}
					Methods.set(colType,factory.getColIsGzip(tableName,colName),pstmt,paras);
					index++;
				}

				pstmt.execute();
				pstmt.close();
			}
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().afterUpdate(tableName, colsBeUpdated, condition);
			}
		}catch(Exception e){
			try{
				pstmt.close();
			}catch(Exception ex){
			}
			throw e;
		}
	}

	@Override
	public void updateByKeys(Object vo) throws Exception{
		updateByKeys(vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void updateByKeys(Object vo,String[] conditionKeys)throws Exception{
		String tableName=factory.getTrueTblName(vo);
		updateByKeys(tableName,vo,conditionKeys);
	}

	@Override
	public void updateByKeys(String tableName,Object vo) throws Exception{
		updateByKeys(tableName,vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void deleteByKeys(Object vo) throws Exception{
		deleteByKeys(vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void deleteByKeys(Object vo,String[] conditionKeys)throws Exception{
		String tableName=factory.getTrueTblName(vo);
		deleteByKeys(tableName,vo,conditionKeys);
	}

	@Override
	public void deleteByKeys(String tableName,Object vo) throws Exception{
		deleteByKeys(tableName,vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void deleteByKeys(String tableName,Object vo,String[] conditionKeys)throws Exception{
		try{
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeUpdateByKeys(vo, conditionKeys);
			}
			if(vo==null){
				throw new Exception("指定的对象为空");
			}
			if(conditionKeys==null||conditionKeys.length==0){
				throw new Exception("没有指定主键");
			}

			tableName=factory.getTrueTblName(tableName);
			Class cls=vo.getClass();

			//生成sql
			StringBuilder sql=new StringBuilder("delete from "+tableName);

			StringBuilder condition=new StringBuilder();
			for(int i=0;i<conditionKeys.length;i++){
				conditionKeys[i]=factory.getColName(tableName,conditionKeys[i]);
				Method method=JUtilBean.getGetter(cls,JUtilBean.colNameToVariableName(conditionKeys[i]),null);
				Object keyValue=method.invoke(vo,(Object[])null);
				if(keyValue==null){
					condition.append(conditionKeys[i]).append(" is null and ");
				}else{
					if((keyValue instanceof Integer)
							||(keyValue instanceof Float)
							||(keyValue instanceof Double)){
						condition.append(conditionKeys[i]).append("=").append(keyValue).append(" and ");
					}else{
						condition.append(conditionKeys[i]).append("='").append(SQLUtil.deleteCriminalChars(keyValue.toString())).append("' and ");
					}
				}
			}
			if(condition!=null && condition.length()>5){
				condition.delete(condition.length()-5, condition.length());
				sql.append(" where ").append(condition);
			}
			//生成sql end

			//log.log("JDAO SQL: "+sql,Logger.LEVEL_DEBUG);
			this.executeSQL(sql.toString());
			sql=null;
		}catch(Exception e){
			throw e;
		}
	}

	@Override
	public void updateByKeys(String tableName,Object vo,String[] conditionKeys)throws Exception{
		PreparedStatement pstmt=null;
		try{
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeUpdateByKeys(vo, conditionKeys);
			}
			if(vo==null){
				throw new Exception("指定的对象为空");
			}
			if(conditionKeys==null||conditionKeys.length==0){
				throw new Exception("没有指定主键");
			}

			tableName=factory.getTrueTblName(tableName);
			Class cls=vo.getClass();

			//生成sql
			StringBuilder sql=new StringBuilder("update "+tableName+" set ");

			List cols=factory.getColumns(tableName);
			for(int i=0;i<cols.size();i++){
				String colName=((Column)cols.get(i)).colName;
				String fieldName=((Column)cols.get(i)).fieldName;

				if(JUtilString.containIgnoreCase(conditionKeys,colName)
						||mirror.getDb().ignoreWhileUpdateViaBean(tableName, colName)){
					continue;
				}
				Method method=JUtilBean.getGetter(cls,fieldName,null);
				Object value=method.invoke(vo,(Object[])null);
				if(value==null){
					sql.append(colName).append("=null,");
				}else{
					sql.append(colName).append("=?,");
				}
			}
			sql.deleteCharAt(sql.length() - 1);
			StringBuilder condition=new StringBuilder();
			for(int i=0;i<conditionKeys.length;i++){
				conditionKeys[i]=factory.getColName(tableName,conditionKeys[i]);
				Method method=JUtilBean.getGetter(cls,JUtilBean.colNameToVariableName(conditionKeys[i]),null);
				Object keyValue=method.invoke(vo,(Object[])null);
				if(keyValue==null){
					condition.append(conditionKeys[i]).append(" is null and ");
				}else{
					if(keyValue instanceof Integer||keyValue instanceof Float||keyValue instanceof Double){
						condition.append(conditionKeys[i]).append("=").append(keyValue).append(" and ");
					}else{
						condition.append(conditionKeys[i]).append("='").append(keyValue).append("' and ");
					}
				}
			}

			if(condition!=null && condition.length()>5) {
				condition.delete(condition.length() - 5, condition.length());
				sql.append(" where ").append(condition);
			}

			//生成sql end
			//log.log("JDAO SQL: "+sql,Logger.LEVEL_DEBUG);
			if(factory.isSynchronized(tableName)){
				Object lock=factory.getTableLock(tableName);
				synchronized(lock){
					pstmt=connection.prepareStatement(sql.toString());

					int index=1;
					for(int i=0;i<cols.size();i++){
						String colName=((Column)cols.get(i)).colName;
						String fieldName=((Column)cols.get(i)).fieldName;

						if(JUtilString.containIgnoreCase(conditionKeys,colName)||mirror.getDb().ignoreWhileUpdateViaBean(tableName, colName)){
							continue;
						}

						int colType=factory.getColType(tableName,colName);
						Object[] paras=null;
						Method method=JUtilBean.getGetter(cls,fieldName,null);
						Object value=method.invoke(vo,(Object[])null);
						if(value==null){
							continue;
						}

						if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
							InputStream is=(InputStream)value;
							paras=new Object[3];
							paras[0]=Integer.valueOf(index);
							paras[1]=is;
							paras[2]=Integer.valueOf(is.available());
						}else if(colType==Types.BLOB){
							Blob blob=(Blob)value;
							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=blob;
						}else if(colType==Types.CLOB){
							Clob clob=(Clob)value;

							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=clob;
						}else{
							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=value;
						}
						Methods.set(colType,factory.getColIsGzip(tableName,colName),pstmt,paras);
						index++;
					}

					pstmt.execute();
					pstmt.close();
				}
			}else{
				pstmt=connection.prepareStatement(sql.toString());

				int index=1;
				for(int i=0;i<cols.size();i++){
					String colName=((Column)cols.get(i)).colName;
					String fieldName=((Column)cols.get(i)).fieldName;

					if(JUtilString.containIgnoreCase(conditionKeys,colName)
							||mirror.getDb().ignoreWhileUpdateViaBean(tableName, colName)){
						continue;
					}

					int colType=factory.getColType(tableName,colName);
					Object[] paras=null;
					Method method=JUtilBean.getGetter(cls,fieldName,null);
					Object value=method.invoke(vo,(Object[])null);
					if(value==null){
						continue;
					}

					if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
						InputStream is=(InputStream)value;
						paras=new Object[3];
						paras[0]=Integer.valueOf(index);
						paras[1]=is;
						paras[2]=Integer.valueOf(is.available());
					}else if(colType==Types.BLOB){
						Blob blob=(Blob)value;
						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=blob;
					}else if(colType==Types.CLOB){
						Clob clob=(Clob)value;

						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=clob;
					}else{
						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=value;
					}
					Methods.set(colType,factory.getColIsGzip(tableName,colName),pstmt,paras);
					index++;
				}

				pstmt.execute();
				pstmt.close();
			}
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().afterUpdateByKeys(vo, conditionKeys);
			}
		}catch(Exception e){
			try{
				pstmt.close();
			}catch(Exception ex){
			}
			throw e;
		}
	}

	@Override
	public void updateByKeysIgnoreNulls(Object vo) throws Exception{
		updateByKeysIgnoreNulls(vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void updateByKeysIgnoreNulls(Object vo,String[] conditionKeys)throws Exception{
		String tableName=factory.getTrueTblName(vo);
		updateByKeysIgnoreNulls(tableName,vo,conditionKeys);
	}

	@Override
	public void updateByKeysIgnoreNulls(String tableName,Object vo) throws Exception{
		updateByKeysIgnoreNulls(tableName,vo,new String[]{factory.getPkColumnName(vo)});
	}

	@Override
	public void updateByKeysIgnoreNulls(String tableName,Object vo,String[] conditionKeys)throws Exception{
		updateByKeysIgnoreNulls(tableName,vo,conditionKeys,null);
	}

	@Override
	public void updateByKeysIgnoreNulls(Object vo,List<String> updateNullCols) throws Exception{
		updateByKeysIgnoreNulls(vo,new String[]{factory.getPkColumnName(vo)},updateNullCols);
	}

	@Override
	public void updateByKeysIgnoreNulls(Object vo,String[] conditionKeys,List<String> updateNullCols)throws Exception{
		String tableName=factory.getTrueTblName(vo);
		updateByKeysIgnoreNulls(tableName,vo,conditionKeys,updateNullCols);
	}

	@Override
	public void updateByKeysIgnoreNulls(String tableName,Object vo,List<String> updateNullCols) throws Exception{
		updateByKeysIgnoreNulls(tableName,vo,new String[]{factory.getPkColumnName(vo)},updateNullCols);
	}

	@Override
	public void updateByKeysIgnoreNulls(String tableName,Object vo,String[] conditionKeys,List<String> updateNullCols)throws Exception{
		PreparedStatement pstmt=null;
		try{
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeUpdateByKeysIgnoreNulls(vo, conditionKeys);
			}
			if(vo==null){
				throw new Exception("指定的对象为空");
			}
			if(conditionKeys==null||conditionKeys.length==0){
				throw new Exception("没有指定主键");
			}

			tableName=factory.getTrueTblName(tableName);
			Class cls=vo.getClass();

			//生成sql
			StringBuilder sql=new StringBuilder("update "+tableName+" set ");
			List cols=factory.getColumns(tableName);
			for(int i=0;i<cols.size();i++){
				String colName=((Column)cols.get(i)).colName;
				String fieldName=((Column)cols.get(i)).fieldName;

				//作为条件的字段或JDAO.xml中配置的不可通过对象操作更新的字段
				if(JUtilString.containIgnoreCase(conditionKeys,colName)
						||mirror.getDb().ignoreWhileUpdateViaBean(tableName, colName)){
					continue;
				}

				Method method=JUtilBean.getGetter(cls,fieldName,null);
				Object value=method.invoke(vo,(Object[])null);
				if(value==null){//如果值为null
					if(updateNullCols==null) {
						//未指定即使为null也更新的字段列表
						continue;
					}else if(!updateNullCols.contains(fieldName)
							&&!updateNullCols.contains(colName.toUpperCase())
							&&!updateNullCols.contains(colName.toLowerCase())) {
						//未包含在指定的即使为null也更新的字段列表中
						continue;
					}
					sql.append(colName).append("=null,");
				}else{
					sql.append(colName).append("=?,");
				}
			}
			if(sql.indexOf("?")==-1 && sql.indexOf("=null,")<0){
				//throw new Exception("没有值需要更新！");
				if(factory.getPlugin()!=null&&pluginEnabled){
					factory.getPlugin().afterUpdateByKeysIgnoreNulls(vo, conditionKeys);
				}
				return;
			}


			sql.deleteCharAt(sql.length() - 1);
			StringBuilder condition=new StringBuilder();
			for(int i=0;i<conditionKeys.length;i++){
				conditionKeys[i]=factory.getColName(tableName,conditionKeys[i]);
				Method method=JUtilBean.getGetter(cls,JUtilBean.colNameToVariableName(conditionKeys[i]),null);
				Object keyValue=method.invoke(vo,(Object[])null);
				if(keyValue==null){
					condition.append(conditionKeys[i]).append(" is null and ");
				}else{
					if(keyValue instanceof Integer||keyValue instanceof Float||keyValue instanceof Double){
						condition.append(conditionKeys[i]).append("=").append(keyValue).append(" and ");
					}else{
						condition.append(conditionKeys[i]).append("='").append(keyValue).append("' and ");
					}
				}
			}
			condition.delete(condition.length() - 5, condition.length());
			sql.append(" where ").append(condition);
			//生成sql end
			//log.log("JDAO SQL: "+sql,-1);
			if(factory.isSynchronized(tableName)){
				Object lock=factory.getTableLock(tableName);
				synchronized(lock){
					pstmt=connection.prepareStatement(sql.toString());

					int index=1;
					for(int i=0;i<cols.size();i++){
						String colName=((Column)cols.get(i)).colName;
						String fieldName=((Column)cols.get(i)).fieldName;

						//作为条件的字段或JDAO.xml中配置的不可通过对象操作更新的字段
						if(JUtilString.containIgnoreCase(conditionKeys,colName)||mirror.getDb().ignoreWhileUpdateViaBean(tableName, colName)){
							continue;
						}

						int colType=factory.getColType(tableName,colName);
						Object[] paras=null;
						Method method=JUtilBean.getGetter(cls,fieldName,null);
						Object value=method.invoke(vo,(Object[])null);
						if(value==null){//如果值为null
							continue;
						}

						if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
							InputStream is=(InputStream)value;
							paras=new Object[3];
							paras[0]=Integer.valueOf(index);
							paras[1]=is;
							paras[2]=Integer.valueOf(is.available());
						}else if(colType==Types.BLOB){
							Blob blob=(Blob)value;
							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=blob;
						}else if(colType==Types.CLOB){
							Clob clob=(Clob)value;

							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=clob;
						}else{
							paras=new Object[2];
							paras[0]=Integer.valueOf(index);
							paras[1]=value;
						}
						Methods.set(colType,factory.getColIsGzip(tableName,colName),pstmt,paras);
						index++;
					}

					pstmt.execute();
					pstmt.close();
				}
			}else{
				pstmt=connection.prepareStatement(sql.toString());

				int index=1;
				for(int i=0;i<cols.size();i++){
					String colName=((Column)cols.get(i)).colName;
					String fieldName=((Column)cols.get(i)).fieldName;

					if(JUtilString.containIgnoreCase(conditionKeys,colName)
							||mirror.getDb().ignoreWhileUpdateViaBean(tableName, colName)){
						continue;
					}

					int colType=factory.getColType(tableName,colName);
					Object[] paras=null;
					Method method=JUtilBean.getGetter(cls,fieldName,null);
					Object value=method.invoke(vo,(Object[])null);
					if(value==null){
						continue;
					}

					if(colType==Types.BINARY||colType==Types.LONGVARBINARY||colType==Types.VARBINARY){
						InputStream is=(InputStream)value;
						paras=new Object[3];
						paras[0]=Integer.valueOf(index);
						paras[1]=is;
						paras[2]=Integer.valueOf(is.available());
					}else if(colType==Types.BLOB){
						Blob blob=(Blob)value;
						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=blob;
					}else if(colType==Types.CLOB){
						Clob clob=(Clob)value;

						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=clob;
					}else{
						paras=new Object[2];
						paras[0]=Integer.valueOf(index);
						paras[1]=value;
					}
					try{
						Methods.set(colType,factory.getColIsGzip(tableName,colName),pstmt,paras);
					}catch (Exception e){
						log.log("Methods.set error => " + tableName + " => " + colName, Logger.LEVEL_ERROR);
						log.log(e, Logger.LEVEL_ERROR);
					}
					index++;
				}

				pstmt.execute();
				pstmt.close();
			}
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().afterUpdateByKeysIgnoreNulls(vo, conditionKeys);
			}
		}catch(Exception e){
			try{
				pstmt.close();
			}catch(Exception ex){
			}
			throw e;
		}
	}

	@Override
	public void executeSQLList(List sqls) throws Exception {
		for(int i=0;i<sqls.size();i++){
			executeSQL((String)sqls.get(i));
		}
	}

	@Override
	public void executeSQL(String sql) throws Exception {
		Statement stmt = null;
		try {
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().beforeExecuteSQL(sql);
			}
			String tableName=SQLUtil.retrieveTableNameFromSQL(sql);
			String trueTblName=factory.getTrueTblName(tableName);
			sql=JUtilString.replaceAll(sql,tableName, trueTblName);
			if(SQLUtil.sqlInjection(sql)!=null) return;

			if(factory.isSynchronized(tableName)){
				Object lock=factory.getTableLock(tableName);
				synchronized(lock){
					stmt = connection.createStatement();
					stmt.execute(sql);
					stmt.close();
				}
			}else{
				stmt = connection.createStatement();
				stmt.execute(sql);
				stmt.close();
			}
			if(factory.getPlugin()!=null&&pluginEnabled){
				factory.getPlugin().afterExecuteSQL(sql);
			}
		} catch (Exception e) {
			try{
				stmt.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public void executeBatchSQL(List sqls)throws Exception{
		if(sqls==null||sqls.size()==0){
			return;
		}
		if(factory.getPlugin()!=null&&pluginEnabled){
			for(int i=0;i<sqls.size();i++){
				factory.getPlugin().beforeExecuteSQL((String)sqls.get(i));
			}
		}
		Statement stmt = null;
		try {
			beginTransaction();
			stmt = connection.createStatement();
			for(int i=0;i<sqls.size();i++){
				String sql=(String)sqls.get(i);
				String tableName=SQLUtil.retrieveTableNameFromSQL(sql);
				String trueTblName=factory.getTrueTblName(tableName);
				sql=JUtilString.replaceAll(sql,tableName, trueTblName);
				//log.log("sql:"+sql,Logger.LEVEL_DEBUG);

				if(SQLUtil.sqlInjection(sql)!=null) continue;

				stmt.addBatch(sql);
			}
			stmt.executeBatch();
			commit();
			stmt.close();

			if(factory.getPlugin()!=null&&pluginEnabled){
				for(int i=0;i<sqls.size();i++){
					factory.getPlugin().afterExecuteSQL((String)sqls.get(i));
				}
			}
		} catch (Exception e) {
			try{
				rollback();
			}catch(Exception ex){}
			try{
				stmt.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public List getCatalogs() throws Exception {
		ResultSet rs=null;
		try {
			List catalogs = new ArrayList();
			DatabaseMetaData dbmd = connection.getMetaData();
			rs = dbmd.getCatalogs();
			while (rs.next()) {
				catalogs.add(rs.getString(1));
			}
			rs.close();
			return catalogs;
		} catch (Exception e) {
			try{
				rs.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public List getSchemas() throws Exception {
		ResultSet rs=null;
		try {
			List schemas = new ArrayList();
			DatabaseMetaData dbmd = connection.getMetaData();
			rs = dbmd.getSchemas();
			while (rs.next()) {
				schemas.add(rs.getString(1));
			}
			rs.close();
			return schemas;
		} catch (Exception e) {
			try{
				rs.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public List getTables(String catalog, String schemaPattern,String tblPattern, String[] tblTypes) throws Exception {
		List tables = new ArrayList();
		ResultSet rs =null;
		try{
			DatabaseMetaData dmd = connection.getMetaData();
			rs = dmd.getTables(catalog, schemaPattern, tblPattern,tblTypes);
			while (rs.next()) {
				String table = rs.getString("TABLE_NAME");
				if(table.indexOf("$")>-1
						||table.indexOf("=")>-1
						||table.indexOf("/")>-1
						||table.indexOf("+")>-1){
					continue;
				}
				tables.add(table);
			}
			rs.close();
			return tables;
		}catch(Exception e){
			try{
				rs.close();
			}catch(Exception ex){}
			throw e;
		}
	}

	@Override
	public List getColumns(String tableName) throws Exception {
		Statement stmt=null;
		ResultSet rs=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			List columns = new ArrayList();
			stmt=connection.createStatement();
			stmt.setMaxRows(1);
			rs=stmt.executeQuery("select * from "+tableName);
			ResultSetMetaData rsmd=rs.getMetaData();
			int colCount=rsmd.getColumnCount();
			for(int i=1;i<=colCount;i++){
				try{
					String colName=rsmd.getColumnName(i);
					int colType=rsmd.getColumnType(i);
					String colTypeName=rsmd.getColumnTypeName(i);
					boolean notnull=(rsmd.isNullable(i)==ResultSetMetaData.columnNoNulls)?true:false;
					Column column=new Column(colName,
							SQLUtil.adjustDataType(factory.getDbType(),colType,colTypeName),
							notnull,
							false,
							rsmd.getColumnDisplaySize(i));
					columns.add(column);
				}catch(Exception ex){}
			}
			rs.close();
			stmt.close();
			return columns;
		}catch(Exception e){
			try{
				rs.close();
			}catch(Exception colseException){}
			try{
				stmt.close();
			}catch(Exception colseException){}
			throw e;
		}
	}

	@Override
	public Column[] getPrimaryKeyColumns(String tableName) throws Exception {
		Statement stmt=null;
		ResultSet rs=null;
		ResultSet pkrs=null;
		try{
			tableName=factory.getTrueTblName(tableName);
			stmt=connection.createStatement();
			stmt.setMaxRows(1);
			rs=stmt.executeQuery("select * from "+tableName);
			ResultSetMetaData rsmd=rs.getMetaData();

			Map typeOfColumnMap=new HashMap();
			Map notnullOfColumnMap=new HashMap();
			Map lengthOfColumnMap=new HashMap();
			int colCount=rsmd.getColumnCount();
			for(int i=1;i<=colCount;i++){
				try{
					String colName=rsmd.getColumnName(i);
					int colType=rsmd.getColumnType(i);
					String colTypeName=rsmd.getColumnTypeName(i);
					boolean notnull=rsmd.isNullable(i)==ResultSetMetaData.columnNoNulls?true:false;
					int length=rsmd.getColumnDisplaySize(i);

					typeOfColumnMap.put(colName,Integer.valueOf(SQLUtil.adjustDataType(factory.getDbType(),colType,colTypeName)));
					lengthOfColumnMap.put(colName,Integer.valueOf(length));
					notnullOfColumnMap.put(colName,Boolean.valueOf(notnull));
				}catch(Exception ex){
					log.log(ex,Logger.LEVEL_DEBUG);
				}
			}

			log.log("getPrimaryKeys > "+rsmd.getCatalogName(1)+" > "+rsmd.getSchemaName(1)+" > "+tableName, -1);
			pkrs=connection.getMetaData().getPrimaryKeys(rsmd.getCatalogName(1),rsmd.getSchemaName(1),tableName);

			List pks=new ArrayList();
			while(pkrs.next()){
				try{
					String colName=pkrs.getString(4);
					Column col=new Column(colName,
							((Integer)typeOfColumnMap.get(colName)).intValue(),
							((Boolean)notnullOfColumnMap.get(colName)).booleanValue(),
							false,
							((Integer)lengthOfColumnMap.get(colName)).intValue());
					pks.add(col);
				}catch(Exception e){
					log.log(e,Logger.LEVEL_DEBUG);
				}
			}

			rs.close();
			pkrs.close();
			stmt.close();

			Column[] cols=new Column[pks.size()];
			pks.toArray(cols);
			return cols;
		}catch(Exception e){
			try{
				rs.close();
			}catch(Exception colseException){}
			try{
				pkrs.close();
			}catch(Exception colseException){}
			try{
				stmt.close();
			}catch(Exception colseException){}
			throw e;
		}
	}

	@Override
	public int getRecordCnt(String sql) throws Exception{
		StmtAndRs sr=null;
		try {
			sql="select count(*) from ("+sql+") row_";
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			int cnt=0;
			while(rs.next()){
				String cntString=rs.getString(1);
				cnt= Integer.parseInt(cntString);
				break;
			}
			sr.close();
			return cnt;
		} catch (Exception e) {
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public int getRecordCnt(String tableName, String condition)throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select count(*) from "+tableName;
			if(condition!=null&&condition.length()>0){
				String tmpcondition=condition.toLowerCase();
				int orderByIndex=JUtilString.match(tmpcondition," order*by","*");
				if(orderByIndex>0){
					condition=condition.substring(0,orderByIndex);
				}

				condition=condition.trim();
				if(!"".equals(condition)){
					sql+=" where "+condition;
				}
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();

			int cnt=0;
			while(rs.next()){
				String cntString=rs.getString(1);
				cnt= Integer.parseInt(cntString);
				break;
			}
			sr.close();
			return cnt;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public int getRecordCnt(String[] tableNames, String condition)throws Exception {
		StmtAndRs sr=null;
		try{
			if(tableNames.length==1){
				return getRecordCnt(tableNames[0],condition);
			}
			if(condition==null||condition.trim().equals("")){
				throw new Exception("多表查询必须指定查询条件");
			}
			//处理表名
			for(int i=0;i<tableNames.length;i++){
				String trueTblName=factory.getTrueTblName(tableNames[i]);
				condition=JUtilString.replaceAll(condition,tableNames[i]+".",trueTblName+".");
				tableNames[i]=trueTblName;
			}

			String sql="select count(*) from ";
			for(int i=0;i<tableNames.length;i++){
				sql+=tableNames[i]+",";
			}
			sql=sql.substring(0,sql.length()-1);

			if(condition!=null&&condition.length()>0){
				String tmpcondition=condition.toLowerCase();
				int orderByIndex=JUtilString.match(tmpcondition," order*by","*");
				if(orderByIndex>0){
					condition=condition.substring(0,orderByIndex);
				}

				condition=condition.trim();
				if(!"".equals(condition)){
					sql+=" where "+condition;
				}
			}
			//System.out.println(sql);

			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			int cnt=0;
			while(rs.next()){
				String cntString=rs.getString(1);
				cnt= Integer.parseInt(cntString);
				break;
			}
			sr.close();
			return cnt;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String getMaxValue(String tableName, String colName, String condition) throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select max("+colName+") from "+tableName;

			if(condition!=null&&condition.trim().length()>0){
				sql+=" where "+condition;
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			String ret="";
			while(rs.next()){
				ret=rs.getString(1);
			}
			sr.close();
			return ret;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String getMaxNumber(String tableName, String colName, String condition) throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select max("+colName+"*1) from "+tableName;

			if(condition!=null&&condition.trim().length()>0){
				sql+=" where "+condition;
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			String ret="";
			while(rs.next()){
				ret=rs.getString(1);
			}
			sr.close();

			if(ret==null) ret="";
			if(ret.indexOf(".")>0){
				String tmp=ret.substring(ret.indexOf(".")+1);
				if("".equals(tmp.replaceAll("0",""))) ret=ret.substring(0,ret.indexOf("."));
			}

			return JUtilMath.convertScientificNotation(ret);
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String getMinValue(String tableName, String colName, String condition) throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select min("+colName+") from "+tableName;
			if(condition!=null&&condition.trim().length()>0){
				sql+=" where "+condition;
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			String ret="";
			while(rs.next()){
				ret=rs.getString(1);
			}
			sr.close();
			return ret;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String getMinNumber(String tableName, String colName, String condition) throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select min("+colName+"*1) from "+tableName;
			if(condition!=null&&condition.trim().length()>0){
				sql+=" where "+condition;
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			String ret="";
			while(rs.next()){
				ret=rs.getString(1);
			}
			sr.close();

			if(ret==null) ret="";
			if(ret.indexOf(".")>0){
				String tmp=ret.substring(ret.indexOf(".")+1);
				if("".equals(tmp.replaceAll("0",""))) ret=ret.substring(0,ret.indexOf("."));
			}

			return JUtilMath.convertScientificNotation(ret);
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String getSum(String tableName, String colName, String condition) throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select sum("+colName+") from "+tableName;
			if(condition!=null&&condition.trim().length()>0){
				sql+=" where "+condition;
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			String ret="";
			while(rs.next()){
				Object obj=null;
				try{
					obj=rs.getObject(1);
				}catch(Exception e){
					obj=null;
				}
				if(obj==null){
					ret= "";
				}else{
					ret = obj.toString();
				}

				break;
			}
			sr.close();
			return ret;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String[] getSum(String tableName, String[] colNames, String condition) throws Exception {
		StmtAndRs sr=null;
		try{
			tableName=factory.getTrueTblName(tableName);

			String sql="select";
			for(int i=0;i<colNames.length;i++){
				sql+=" sum("+colNames[i]+"),";
			}

			sql=sql.substring(0,sql.length()-1);

			sql+=" from "+tableName;

			if(condition!=null&&condition.trim().length()>0){
				sql+=" where "+condition;
			}
			sr=find(sql);
			if(sr==null) throw new Exception("SQL Exception(Injection?) "+sql);

			ResultSet rs=sr.resultSet();
			String[] ret=new String[colNames.length];
			while(rs.next()){
				for(int i=0;i<colNames.length;i++){
					Object obj=null;
					try{
						obj=rs.getObject(i+1);
					}catch(Exception e){
						obj=null;
					}
					if(obj==null){
						ret[i]= "";
					}else{
						ret[i] = obj.toString();
					}
				}

				break;
			}
			sr.close();
			return ret;
		}catch(Exception e){
			if(sr!=null) sr.close();
			throw e;
		}
	}

	@Override
	public String autoIncreaseKey(String table,String column) throws Exception{
		return autoIncreaseKey(table,column,0);
	}

	@Override
	public String autoIncreaseKey(String table,String column,long addition) throws Exception{
		return autoIncreaseKeyLargerThan(table,column,0,0);
	}

	@Override
	public String autoIncreaseKeyLargerThan(String table,String column,long min) throws Exception{
		return autoIncreaseKeyLargerThan(table,column,min,0);
	}

	@Override
	public String autoIncreaseKeyLargerThan(String table,String column,long min,long addition) throws Exception {
		return autoIncreaseKeyLargerThan(table,column,min,addition, null);
	}

	@Override
	public String autoIncreaseKeyLargerThan(String table,String column,long min,long addition,String condition) throws Exception{
		String key=factory.getDbName().toLowerCase()+"."+table.toLowerCase()+"."+column.toLowerCase();
		if(!JUtilString.isBlank(condition)) {
			String temp=JUtilString.replaceAll(condition, "  ", " ").toLowerCase();
			key+="."+temp;
		}
		key=key.intern();

		synchronized(key){
			Long max=null;
			if(this.mirror.getDb().isCluster()){
				max=(Long)factory.getMaxColumnValues().get(new JCacheParams(key));
			}else{
				max=(Long)factory.getMaxColumnValuesLocal().get(key);
			}

			if(max==null){
				String[] tables = table.split(",");//如果包含逗号，说明是取多个表中的最大值
				for(int i=0; i<tables.length; i++) {
					table = tables[i].trim();

					String strMax=this.getMaxNumber(table,column,condition==null?"":condition);

					if(!JUtilMath.isLong(strMax)) strMax = "0";
					if(max == null || Long.parseLong(strMax) > max){
						max=Long.valueOf(strMax);
					}
				}
			}

			long ret=0;

			ret=max.longValue()+1+addition;
			if(min>0 && ret<min) ret=min;

			if(this.mirror.getDb().isCluster()){
				factory.getMaxColumnValues().addOne(key,Long.valueOf(ret));
			}else{
				factory.getMaxColumnValuesLocal().put(key,Long.valueOf(ret));
			}

			return ""+ret;
		}
	}

	/**
	 *
	 * @param sql
	 * @param RPP
	 * @param PN
	 * @return
	 * @throws Exception
	 */
	protected String getFindSQL(String sql,int RPP,int PN)throws Exception{
		if(RPP<0||PN<0){
			throw new Exception("指定的分页相关的参数小于零");
		}
		if(RPP>0&&PN>0){
			sql=getSQLWithRowSetLimit(sql,RPP*(PN-1),RPP*PN);
		}
		return sql;
	}

	/**
	 *
	 * @param sql
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	protected String getFindSQLScale(String sql,int start,int end)throws Exception{
		if(start<0||end<=0){
			throw new Exception("指定的范围的参数小于零");
		}

		if(start>=end){
			throw new Exception("指定的范围的start 大于等于 end");
		}
		sql=getSQLWithRowSetLimit(sql,start,end);
		return sql;
	}


	/**
	 *
	 * @param rs
	 * @param colType
	 * @param colName
	 * @param isGzip
	 * @return
	 * @throws Exception
	 */
	protected Object getObject(ResultSet rs,int colType,String colName,boolean isGzip)throws Exception{
		if(colType==Types.BLOB){
			java.sql.Blob blob=rs.getBlob(colName);
			if(blob==null){
				return null;
			}
			return new Blob(blob.getBinaryStream());
		}else if(colType==Types.CLOB){
			java.sql.Clob clob=rs.getClob(colName);
			if(clob==null){
				return null;
			}
			return new Clob(clob.getCharacterStream());
		}else if(colType==Types.BINARY||colType==Types.VARBINARY||colType==Types.LONGVARBINARY){
			InputStream in=rs.getBinaryStream(colName);
			if(in==null){
				return null;
			}
			return new ByteArrayInputStream(JUtilInputStream.bytes(in));
		}else if(colType==Types.TIMESTAMP){
			return Methods.get(colType,rs,colName);
		}else{
			Object obj=Methods.get(colType,rs,colName);
			if(isGzip&&obj!=null&&(obj instanceof String)){
				try{
					obj=JUtilCompressor.gunzipString((String)obj,"UTF-8");
				}catch(Exception e){}
			}
			return obj;
		}
	}


	/**
	 *
	 * @param rs
	 * @param colType
	 * @param index
	 * @param isGzip
	 * @return
	 * @throws Exception
	 */
	protected Object getObject(ResultSet rs,int colType,int index,boolean isGzip)throws Exception{
		if(colType==Types.BLOB){
			java.sql.Blob blob=rs.getBlob(index);
			if(blob==null){
				return null;
			}
			return new Blob(blob.getBinaryStream());
		}else if(colType==Types.CLOB){
			java.sql.Clob clob=rs.getClob(index);
			if(clob==null){
				return null;
			}
			return new Clob(clob.getCharacterStream());
		}else if(colType==Types.BINARY||colType==Types.VARBINARY||colType==Types.LONGVARBINARY){
			InputStream in=rs.getBinaryStream(index);
			if(in==null){
				return null;
			}
			return new ByteArrayInputStream(JUtilInputStream.bytes(in));
		}else if(colType==Types.TIMESTAMP){
			return Methods.get(colType,rs,index);
		}else{
			Object obj=Methods.get(colType,rs,index);
			if(isGzip&&obj!=null&&(obj instanceof String)){
				try{
					obj=JUtilCompressor.gunzipString((String)obj,"UTF-8");
				}catch(Exception e){}
			}

			return obj;
		}
	}

	@Override
	public void disablePlugin() {
		pluginEnabled=false;
	}

	@Override
	public void enablePlugin() {
		pluginEnabled=true;
	}

	@Override
	public boolean isPluginEnabled(){
		return pluginEnabled;
	}

	@Override
	public void setTimeZone(TimeZone timeZone){
		this.timeZone=timeZone;
	}

	@Override
	public TimeZone getTimeZone(){
		return this.timeZone;
	}

	@Override
	public void setRowIntervener(RowIntervener intervener) {
		this.intervener=intervener;
	}

	@Override
	public void removeRowIntervener() {
		this.intervener=null;
	}

	@Override
	public RowIntervener getRowIntervener() {
		return this.intervener;
	}


	@Override
	public void setRuntimeDatas(Object[] datas) {
		this.runtimeDatas=datas;
	}

	@Override
	public void removeRuntimeDatas() {
		this.runtimeDatas=null;
	}

	@Override
	public Object[] getRuntimeDatas() {
		return this.runtimeDatas;
	}
}