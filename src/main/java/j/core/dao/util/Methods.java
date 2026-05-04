package j.core.dao.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import j.log.Logger;
import j.util.JUtilCompressor;

/**
 * PreparedStatement和ResultSet的一些get/set方法，保存在map中，key为数据库字段类型、value为get/set方法。以简化反射操作时代码编写。
 * @author 肖炯
 *
 */
public class Methods {
	private static Logger log=Logger.create(Methods.class);

	//java.sql.PreparedStatement 的setXXX(int index,Object value)方法
	public static final Map<Integer, Method> settersOfPreparedStatement=new HashMap();

	//java.sql.ResultSet 的getXXX(int columnIndex)方法
	public static final Map<Integer, Method> gettersOfResultSetByIndex=new HashMap();

	//java.sql.ResultSet 的getXXX(String columnName)方法
	public static final Map<Integer, Method> gettersOfResultSetByName=new HashMap();
	
	/**
	 * 
	 */
	static{
		try{
			Class pstmtCls=java.sql.PreparedStatement.class;
			
			//java.sql.Types.BOOLEAN
			settersOfPreparedStatement.put(Integer.valueOf(Types.BOOLEAN),pstmtCls.getMethod("setBoolean", new Class[]{Integer.TYPE,Boolean.TYPE}));
			
			//java.sql.Types.ARRAY
//			settersOfPreparedStatement.put(Integer.valueOf(Types.ARRAY),pstmtCls.getMethod("setArray", new Class[]{Integer.TYPE,java.sql.Array.class}));
			
			//java.sql.Types.BIT
			settersOfPreparedStatement.put(Integer.valueOf(Types.BIT),pstmtCls.getMethod("setBoolean", new Class[]{Integer.TYPE,Boolean.TYPE}));
			
			//java.sql.Types.BLOB 
			settersOfPreparedStatement.put(Integer.valueOf(Types.BLOB),pstmtCls.getMethod("setBlob", new Class[]{Integer.TYPE,java.sql.Blob.class}));
			
			//java.sql.Types.CHAR
			settersOfPreparedStatement.put(Integer.valueOf(Types.CHAR),pstmtCls.getMethod("setString", new Class[]{Integer.TYPE,String.class}));
			
			//java.sql.Types.CLOB
			settersOfPreparedStatement.put(Integer.valueOf(Types.CLOB),pstmtCls.getMethod("setClob", new Class[]{Integer.TYPE,java.sql.Clob.class}));
						
			//java.sql.Types.DATE
			settersOfPreparedStatement.put(Integer.valueOf(Types.DATE),pstmtCls.getMethod("setDate", new Class[]{Integer.TYPE,java.sql.Date.class}));
			
			//java.sql.Types.DECIMAL
			settersOfPreparedStatement.put(Integer.valueOf(Types.DECIMAL),pstmtCls.getMethod("setBigDecimal", new Class[]{Integer.TYPE,BigDecimal.class}));
			
			//java.sql.Types.DOUBLE
			settersOfPreparedStatement.put(Integer.valueOf(Types.DOUBLE),pstmtCls.getMethod("setDouble", new Class[]{Integer.TYPE,Double.TYPE}));
			
			//java.sql.Types.REAL
			settersOfPreparedStatement.put(Integer.valueOf(Types.REAL),pstmtCls.getMethod("setDouble", new Class[]{Integer.TYPE,Double.TYPE}));
			
			//java.sql.Types.FLOAT
			settersOfPreparedStatement.put(Integer.valueOf(Types.FLOAT),pstmtCls.getMethod("setFloat", new Class[]{Integer.TYPE,Float.TYPE}));

			//java.sql.Types.INTEGER
			settersOfPreparedStatement.put(Integer.valueOf(Types.INTEGER),pstmtCls.getMethod("setInt", new Class[]{Integer.TYPE,Integer.TYPE}));
			
			//java.sql.Types.TINYINT 
			settersOfPreparedStatement.put(Integer.valueOf(Types.TINYINT),pstmtCls.getMethod("setShort", new Class[]{Integer.TYPE,Short.TYPE}));
			
			//java.sql.Types.SMALLINT
			settersOfPreparedStatement.put(Integer.valueOf(Types.SMALLINT),pstmtCls.getMethod("setShort", new Class[]{Integer.TYPE,Short.TYPE}));
			
			//java.sql.Types.BIGINT
			settersOfPreparedStatement.put(Integer.valueOf(Types.BIGINT),pstmtCls.getMethod("setLong", new Class[]{Integer.TYPE,Long.TYPE}));
			
			//java.sql.Types.JAVA_OBJECT
//			settersOfPreparedStatement.put(Integer.valueOf(Types.JAVA_OBJECT),pstmtCls.getMethod("setObject", new Class[]{Integer.TYPE,Object.class}));
			
			//java.sql.Types.LONGVARBINARY
			settersOfPreparedStatement.put(Integer.valueOf(Types.LONGVARBINARY),pstmtCls.getMethod("setBinaryStream", new Class[]{Integer.TYPE,java.io.InputStream.class,Integer.TYPE}));
			
			//java.sql.Types.BINARY
			settersOfPreparedStatement.put(Integer.valueOf(Types.BINARY),pstmtCls.getMethod("setBinaryStream", new Class[]{Integer.TYPE,java.io.InputStream.class,Integer.TYPE}));
			
			//java.sql.Types.VARBINARY
			settersOfPreparedStatement.put(Integer.valueOf(Types.VARBINARY),pstmtCls.getMethod("setBinaryStream", new Class[]{Integer.TYPE,java.io.InputStream.class,Integer.TYPE}));
			
			//java.sql.Types.LONGVARCHAR
			settersOfPreparedStatement.put(Integer.valueOf(Types.LONGVARCHAR),pstmtCls.getMethod("setString", new Class[]{Integer.TYPE,String.class}));

			//java.sql.Types.NUMERIC 
			settersOfPreparedStatement.put(Integer.valueOf(Types.NUMERIC),pstmtCls.getMethod("setBigDecimal", new Class[]{Integer.TYPE,BigDecimal.class}));
			
			//java.sql.Types.REF
//			settersOfPreparedStatement.put(Integer.valueOf(Types.REF),pstmtCls.getMethod("setRef", new Class[]{Integer.TYPE,java.sql.Ref.class}));			
			
			//java.sql.Types.TIME 
			settersOfPreparedStatement.put(Integer.valueOf(Types.TIME),pstmtCls.getMethod("setTime", new Class[]{Integer.TYPE,java.sql.Time.class}));
			
			//java.sql.Types.TIMESTAMP
			settersOfPreparedStatement.put(Integer.valueOf(Types.TIMESTAMP),pstmtCls.getMethod("setTimestamp", new Class[]{Integer.TYPE,java.sql.Timestamp.class}));

			//java.sql.Types.VARCHAR
			settersOfPreparedStatement.put(Integer.valueOf(Types.VARCHAR),pstmtCls.getMethod("setString", new Class[]{Integer.TYPE,String.class}));
			
			//java.sql.Types.OTHER
			settersOfPreparedStatement.put(Integer.valueOf(Types.OTHER),pstmtCls.getMethod("setObject", new Class[]{Integer.TYPE,Object.class}));
			
			//java.sql.Types.DATALINK
			//java.sql.Types.DISTINCT
			//java.sql.Types.NULL
			//java.sql.Types.STRUCT
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
		}
		
		try{
			Class[] paras=new Class[]{Integer.TYPE}; 
			
			Class rsCls= ResultSet.class;
			
			//java.sql.Types.BOOLEAN			
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.BOOLEAN),rsCls.getMethod("getBoolean",paras));
			
			//java.sql.Types.ARRAY
//			gettersOfResultSetByIndex.put(Integer.valueOf(Types.ARRAY),rsCls.getMethod("getArray",paras));
			
			//java.sql.Types.BIT
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.BIT),rsCls.getMethod("getBoolean",paras));
			
			//java.sql.Types.BLOB 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.BLOB),rsCls.getMethod("getBlob",paras));
			
			//java.sql.Types.CHAR
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.CHAR),rsCls.getMethod("getString",paras));
			
			//java.sql.Types.CLOB
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.CLOB),rsCls.getMethod("getClob",paras));
						
			//java.sql.Types.DATE
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.DATE),rsCls.getMethod("getDate",paras));
			
			//java.sql.Types.DECIMAL
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.DECIMAL),rsCls.getMethod("getBigDecimal",paras));
			
			//java.sql.Types.DOUBLE
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.DOUBLE),rsCls.getMethod("getDouble",paras));
			
			//java.sql.Types.REAL
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.REAL),rsCls.getMethod("getDouble",paras));
			
			//java.sql.Types.FLOAT
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.FLOAT),rsCls.getMethod("getFloat",paras));

			//java.sql.Types.INTEGER
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.INTEGER),rsCls.getMethod("getInt",paras));
			
			//java.sql.Types.TINYINT 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.TINYINT),rsCls.getMethod("getShort",paras));
			
			//java.sql.Types.SMALLINT
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.SMALLINT),rsCls.getMethod("getShort",paras));
			
			//java.sql.Types.BIGINT
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.BIGINT),rsCls.getMethod("getLong",paras));
			
			//java.sql.Types.JAVA_OBJECT
//			gettersOfResultSetByIndex.put(Integer.valueOf(Types.JAVA_OBJECT),rsCls.getMethod("getObject",paras));
			
			//java.sql.Types.LONGVARBINARY
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.LONGVARBINARY),rsCls.getMethod("getBinaryStream",paras));
			
			//java.sql.Types.BINARY
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.BINARY),rsCls.getMethod("getBinaryStream",paras));
			
			//java.sql.Types.VARBINARY 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.VARBINARY),rsCls.getMethod("getBinaryStream",paras));
			
			//java.sql.Types.LONGVARCHAR 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.LONGVARCHAR),rsCls.getMethod("getString",paras));

			//java.sql.Types.NUMERIC 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.NUMERIC),rsCls.getMethod("getBigDecimal",paras));
			
			//java.sql.Types.REF
//			gettersOfResultSetByIndex.put(Integer.valueOf(Types.REF),rsCls.getMethod("getRef",paras));			
			
			//java.sql.Types.TIME 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.TIME),rsCls.getMethod("getTime",paras));
			
			//java.sql.Types.TIMESTAMP; 
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.TIMESTAMP),rsCls.getMethod("getTimestamp",paras));

			//java.sql.Types.VARCHAR
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.VARCHAR),rsCls.getMethod("getString",paras));
			
			//java.sql.Types.OTHER
			gettersOfResultSetByIndex.put(Integer.valueOf(Types.OTHER),rsCls.getMethod("getObject",paras));
			
			//java.sql.Types.DATALINK
			//java.sql.Types.DISTINCT
			//java.sql.Types.NULL
			//java.sql.Types.STRUCT
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
		}		
		
		try{
			Class[] paras=new Class[]{String.class}; 		
			
			Class rsCls= ResultSet.class;
			
			//java.sql.Types.BOOLEAN			
			gettersOfResultSetByName.put(Integer.valueOf(Types.BOOLEAN),rsCls.getMethod("getBoolean",paras));
			
			//java.sql.Types.ARRAY
//			gettersOfResultSetByName.put(Integer.valueOf(Types.ARRAY),rsCls.getMethod("getArray",paras));
			
			//java.sql.Types.BIT
			gettersOfResultSetByName.put(Integer.valueOf(Types.BIT),rsCls.getMethod("getBoolean",paras));
			
			//java.sql.Types.BLOB 
			gettersOfResultSetByName.put(Integer.valueOf(Types.BLOB),rsCls.getMethod("getBlob",paras));
			
			//java.sql.Types.CHAR
			gettersOfResultSetByName.put(Integer.valueOf(Types.CHAR),rsCls.getMethod("getString",paras));
			
			//java.sql.Types.CLOB
			gettersOfResultSetByName.put(Integer.valueOf(Types.CLOB),rsCls.getMethod("getClob",paras));
						
			//java.sql.Types.DATE
			gettersOfResultSetByName.put(Integer.valueOf(Types.DATE),rsCls.getMethod("getDate",paras));
			
			//java.sql.Types.DECIMAL
			gettersOfResultSetByName.put(Integer.valueOf(Types.DECIMAL),rsCls.getMethod("getBigDecimal",paras));
			
			//java.sql.Types.DOUBLE
			gettersOfResultSetByName.put(Integer.valueOf(Types.DOUBLE),rsCls.getMethod("getDouble",paras));
			
			//java.sql.Types.REAL
			gettersOfResultSetByName.put(Integer.valueOf(Types.REAL),rsCls.getMethod("getDouble",paras));
			
			//java.sql.Types.FLOAT
			gettersOfResultSetByName.put(Integer.valueOf(Types.FLOAT),rsCls.getMethod("getFloat",paras));

			//java.sql.Types.INTEGER
			gettersOfResultSetByName.put(Integer.valueOf(Types.INTEGER),rsCls.getMethod("getInt",paras));
			
			//java.sql.Types.TINYINT 
			gettersOfResultSetByName.put(Integer.valueOf(Types.TINYINT),rsCls.getMethod("getShort",paras));
			
			//java.sql.Types.SMALLINT
			gettersOfResultSetByName.put(Integer.valueOf(Types.SMALLINT),rsCls.getMethod("getShort",paras));
			
			//java.sql.Types.BIGINT
			gettersOfResultSetByName.put(Integer.valueOf(Types.BIGINT),rsCls.getMethod("getLong",paras));
			
			//java.sql.Types.JAVA_OBJECT
//			gettersOfResultSetByName.put(Integer.valueOf(Types.JAVA_OBJECT),rsCls.getMethod("getObject",paras));
			
			//java.sql.Types.LONGVARBINARY
			gettersOfResultSetByName.put(Integer.valueOf(Types.LONGVARBINARY),rsCls.getMethod("getBinaryStream",paras));
			
			//java.sql.Types.BINARY
			gettersOfResultSetByName.put(Integer.valueOf(Types.BINARY),rsCls.getMethod("getBinaryStream",paras));
			
			//java.sql.Types.VARBINARY 
			gettersOfResultSetByName.put(Integer.valueOf(Types.VARBINARY),rsCls.getMethod("getBinaryStream",paras));
			
			//java.sql.Types.LONGVARCHAR 
			gettersOfResultSetByName.put(Integer.valueOf(Types.LONGVARCHAR),rsCls.getMethod("getString",paras));

			//java.sql.Types.NUMERIC 
			gettersOfResultSetByName.put(Integer.valueOf(Types.NUMERIC),rsCls.getMethod("getBigDecimal",paras));
			
			//java.sql.Types.REF
//			gettersOfResultSetByName.put(Integer.valueOf(Types.REF),rsCls.getMethod("getRef",paras));			
			
			//java.sql.Types.TIME 
			gettersOfResultSetByName.put(Integer.valueOf(Types.TIME),rsCls.getMethod("getTime",paras));
			
			//java.sql.Types.TIMESTAMP; 
			gettersOfResultSetByName.put(Integer.valueOf(Types.TIMESTAMP),rsCls.getMethod("getTimestamp",paras));

			//java.sql.Types.VARCHAR
			gettersOfResultSetByName.put(Integer.valueOf(Types.VARCHAR),rsCls.getMethod("getString",paras));
			
			//java.sql.Types.OTHER
			gettersOfResultSetByName.put(Integer.valueOf(Types.OTHER),rsCls.getMethod("getObject",paras));
			
			//java.sql.Types.DATALINK
			//java.sql.Types.DISTINCT
			//java.sql.Types.NULL
			//java.sql.Types.STRUCT
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
		}				
	}
	
	/**
	 * 调用PreparedStatement的setXXX(int index,Object value)方法
	 * @param colType
	 * @param obj
	 * @param paras
	 * @return
	 */
	public static Object set(int colType,boolean gzip,Object obj,Object[] paras)throws Exception{
		if(gzip&&paras!=null&&paras.length>1){
			Object value=paras[1];
			if(value!=null&&(value instanceof String)){
				try{
					//尝试解压，以避免重复执行gzip压缩
					value=JUtilCompressor.gunzipString((String)value,"UTF-8");
				}catch(Exception e){}
				
				try{
					//压缩
					value=JUtilCompressor.gzipString((String)value,"UTF-8");
					paras[1]=value;
				}catch(Exception e){}
			}
		}
		Method method=settersOfPreparedStatement.get(Integer.valueOf(colType));
		try{
			return method.invoke(obj,paras);
		}catch(Exception e){
			//log.log(colType+","+method.toString()+","+paras[0]+","+paras[1].getClass(),-1);
			throw e;
		}
	}
	
	/**
	 * 调用ResultSet的getXXX(int columnIndex)方法
	 * @param colType
	 * @param obj
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public static Object get(int colType,ResultSet obj,int index)throws Exception{
		Object ret=null;
		if(colType==Types.CHAR 
				|| colType==Types.VARCHAR 
				|| colType==Types.LONGNVARCHAR
				|| colType==Types.LONGVARCHAR) ret = obj.getString(index);
		else if(colType==Types.INTEGER) ret = obj.getInt(index);
		else if(colType==Types.TINYINT) ret = obj.getShort(index);
		else if(colType==Types.SMALLINT) ret = obj.getShort(index);
		else if(colType==Types.DOUBLE || colType==Types.REAL) ret = obj.getDouble(index);
		else if(colType==Types.FLOAT) ret = obj.getFloat(index);
		else if(colType==Types.BIGINT) ret = obj.getLong(index);
		else if(colType==Types.BOOLEAN || colType==Types.BIT) ret = obj.getBoolean(index);
		else if(colType==Types.DECIMAL) ret = obj.getBigDecimal(index);
		else if(colType==Types.NUMERIC) ret = obj.getBigDecimal(index);
		else if(colType==Types.BLOB) ret = obj.getBlob(index);
		else if(colType==Types.CLOB) ret = obj.getClob(index);
		else if(colType==Types.DATE) ret = obj.getDate(index);
		else if(colType==Types.JAVA_OBJECT) ret = obj.getObject(index);
		else if(colType==Types.LONGVARBINARY) ret = obj.getBinaryStream(index);
		else if(colType==Types.BINARY) ret = obj.getBinaryStream(index);
		else if(colType==Types.VARBINARY) ret = obj.getBinaryStream(index);
		else if(colType==Types.REF) ret = obj.getRef(index);
		else if(colType==Types.TIME) ret = obj.getTime(index);
		else if(colType==Types.TIMESTAMP) ret = obj.getTimestamp(index);
		else if(colType==Types.DATE) ret = obj.getDate(index);
		else ret = obj.getObject(index);

		if(ret==null || obj.wasNull()) return null;

		//LocalDateTime转Timestamp
		if(ret instanceof java.time.LocalDateTime) {
			ret = new Timestamp(((java.time.LocalDateTime)ret).toEpochSecond(ZoneOffset.ofTotalSeconds(TimeZone.getDefault().getRawOffset()/1000))*1000);
		}
		return ret;
		
		/*Method method=gettersOfResultSetByIndex.get(Integer.valueOf(colType));
		Object ret= method.invoke(obj,new Object[]{Integer.valueOf(index)});
		if(obj.wasNull()){
			return null;
		}else{
			return ret;
		}*/
	}
	
	/**
	 * 调用ResultSet的getXXX(int columnName)方法
	 * @param colType
	 * @param obj
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	public static Object get(int colType,ResultSet obj,String colName)throws Exception{
		Object ret=null;
		if(colType==Types.CHAR 
				|| colType==Types.VARCHAR 
				|| colType==Types.LONGNVARCHAR
				|| colType==Types.LONGVARCHAR) ret = obj.getString(colName);
		else if(colType==Types.INTEGER) ret = obj.getInt(colName);
		else if(colType==Types.TINYINT) ret = obj.getShort(colName);
		else if(colType==Types.SMALLINT) ret = obj.getShort(colName);
		else if(colType==Types.DOUBLE || colType==Types.REAL) ret = obj.getDouble(colName);
		else if(colType==Types.FLOAT) ret = obj.getFloat(colName);
		else if(colType==Types.BIGINT) ret = obj.getLong(colName);
		else if(colType==Types.BOOLEAN || colType==Types.BIT) ret = obj.getBoolean(colName);
		else if(colType==Types.DECIMAL) ret = obj.getBigDecimal(colName);
		else if(colType==Types.NUMERIC) ret = obj.getBigDecimal(colName);
		else if(colType==Types.BLOB) ret = obj.getBlob(colName);
		else if(colType==Types.CLOB) ret = obj.getClob(colName);
		else if(colType==Types.DATE) ret = obj.getDate(colName);
		else if(colType==Types.JAVA_OBJECT) ret = obj.getObject(colName);
		else if(colType==Types.LONGVARBINARY) ret = obj.getBinaryStream(colName);
		else if(colType==Types.BINARY) ret = obj.getBinaryStream(colName);
		else if(colType==Types.VARBINARY) ret = obj.getBinaryStream(colName);
		else if(colType==Types.REF) ret = obj.getRef(colName);
		else if(colType==Types.TIME) ret = obj.getTime(colName);
		else if(colType==Types.TIMESTAMP) ret = obj.getTimestamp(colName);
		else if(colType==Types.DATE) ret = obj.getDate(colName);
		else ret = obj.getObject(colName);

		if(ret==null || obj.wasNull()) return null;

		//LocalDateTime转Timestamp
		if(ret instanceof java.time.LocalDateTime) {
			/**
			 * LocalDateTime localDateTime = LocalDateTime.now();
			 * localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
			 */
			ret = new Timestamp(((java.time.LocalDateTime)ret).toEpochSecond(ZoneOffset.ofTotalSeconds(TimeZone.getDefault().getRawOffset()/1000))*1000);
		}

		return ret;
		
		/*Method method=gettersOfResultSetByName.get(Integer.valueOf(colType));
		Object ret= method.invoke(obj,new Object[]{colName});
		if(obj.wasNull()){
			return null;
		}else{
			return ret;
		}*/
	}
}
