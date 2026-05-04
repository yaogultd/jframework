package j.core.dao;


import com.esotericsoftware.reflectasm.MethodAccess;
import j.core.Startup;
import j.core.cache.CachedMap;
import j.core.common.JProperties;
import j.core.dao.connection.ConnectionProvider;
import j.core.dao.connection.ConnectionProviderFactory;
import j.core.nvwa.resource.ResourceHelper;
import j.core.dao.util.SQLUtil;
import j.log.Logger;
import j.core.sys.SysUtil;
import j.util.*;
import org.dom4j.Document;
import org.dom4j.Element;
import software.amazon.awssdk.services.pinpoint.endpoints.internal.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @author 肖炯
 *
 */
public class DAOFactory implements Runnable{	
	private static Logger log=Logger.create(DAOFactory.class);

	/**
	 * 是否初始化完毕
	 */
	private volatile boolean init;
	
	/**
	 * DAOFactory是一个重量级的类，且是线程安全的，故针对同一数据库只需创建一个实例：
	 * 应用调用getInstance得到DAOFactory实例时
	 */
	private static ConcurrentMap<String, DAOFactory> factories=new ConcurrentMap();

	/**
	 * Dialect所对应的数据库类型
	 */
	private static Map<String, String> dbTypeOfDialect=new HashMap();
	
	/**
	 * 包含如下信息
	 * DBConf.put(clsName+".name",tblName);//vo类所对应的表名
	 * DBConf.put(tblNameLowerCase+".name",tblName);//实际表名。key，小写的表名  value，区分大小写的表名（linux里需区分大小写）
	 * DBConf.put(tblNameLowerCase+".class",clsName);//表名所对应的vo类，key，小写的表名  value，vo类名
	 * Element pk=rootX.element("id");
	 * if(pk!=null){
	 * 	DBConf.put(tblNameLowerCase+".pk",pk.attributeValue("column"));//表的主键名。key，小写的表名  value，主键名
	 * 	DBConf.put(clsName+".pk",pk.attributeValue("column"));//vo类所对应的表的主键名。key，vo类名  value，主键名
	 * }
	 */
	private Map<String, String> DBConf=new HashMap();

	/**
	 * 所有表名
	 */
	private ConcurrentList<String> tblNames=new ConcurrentList();
	
	/**
	 * 各表的字段列表，key，小写表名  value，Column的List
	 */
	private Map<String, List<Column>> colsOfTables=new HashMap();
	
	/**
	 * key,小写表名.对应vo中的变量名   value,列的sql类型
	 * colsType.put(tblNameLowerCase+"."+fieldName,Integer.valueOf(col.colType));
	 */
	private Map<String, Integer> colsType=new HashMap();
	
	/**
	 * key,小写表名.对应vo中的变量名   value,表的列名
	 * colsName.put(tblNameLowerCase+"."+fieldName,col.colName);
	 */	
	private Map<String, String> colsName=new HashMap();
	
	/**
	 * key,小写表名.对应vo中的变量名   value,true/flase 表示是否gzip
	 * colsName.put(tblNameLowerCase+"."+fieldName,col.colName);
	 */
	private Map<String, String> colsIsGzip=new HashMap();
	
	/**
	 * key,小写表名.对应vo中的变量名   value,vo中对应的set方法
	 * setters.put(tblNameLowerCase+"."+fieldName,setter);
	 */
	private Map<String, Method> setters=new HashMap();
	private Map<String, String> setterNames=new HashMap();
	
	/**
	 * 调用DAO的几个返回List的find、findScale方法时，如果指定了vo类，
	 * 从unregisterSetters查找对应字段的set方法，如果存在则直接返回，如果不存在则得到set方法并保存在unregisterSetters，
	 * key，cls.getName()+"."+fieldName
	 * value，set方法
	 */
	private Map<String, Method> unregisterSetters=new HashMap();
	
	/**
	 * 使用哪个数据库操作实现类，大部分操作在RdbmsDao中有通用实现，
	 * 个别因数据库各不同的操作，比如分页，在具体的dialect中实现
	 */
	private String dialectCls;
	
	/**
	 * 数据库类型
	 */
	private String dbType;
	
	/**
	 * 数据库名字
	 */
	private String dbName;
	
	/**
	 * 数据库连接信息，连接池代码复制自hibernate，为简化操作properties的key采用于hibernate一样的名字，如：
	 * connectionPropedrties.put("connection.provider_class",cls)
	 */
	private Properties connectionPropedrties=new Properties();
	
	/**
	 * 连接池管理类，在cfg.xml中指定
	 */
	private ConnectionProvider connProvider=null;
	
	/**
	 * 由数据库连接信息生成的，代表该DAOFactory实例的key
	 */
	private String key="";
	
	/**
	 * 数据库操作擦肩，在执行数据库更新操作前后调用，可用于输出sql、缓存、数据库同步等......
	 * 在cfg.xml中指定
	 */
	private DAOPlugin plugin;

	/**
	 * 是否打印sql（打印sql插件启用时）
	 */
	private boolean showSql=false;
	
	/**
	 * 记录创建的DAO实例，在必要的时候进行处理，以保证数据库连接正当释放（在DAOFactory的监控线程中处理，run方法）
	 */
	private ConcurrentList<DAO> daos=new ConcurrentList();
	
	/**
	 * 更新操作（不包括插入）需要同步锁的表，通过registerSynTable(String tbl)方法设置
	 */
	private List<String> synchronizedTbls=new ArrayList();
	
	/**
	 * createDAO和close方法的调用记录，以检测不正确的使用DAO的情况，比如太频繁创建DAO等
	 * key，调用createDAO的类名（调用close方法时加上*close）
	 * value，该类调用createDAO或close方法的次数，Integer类型
	 */
	private ConcurrentMap<String, Long> callers=new ConcurrentMap();

	private CachedMap maxColumnValues;
	private ConcurrentMap maxColumnValuesLocal;
	
	static{
		dbTypeOfDialect.put("j.core.dao.dialect.MysqlDialect", DAO.DB_TYPE_MYSQL);
		dbTypeOfDialect.put("j.core.dao.dialect.SQLiteDialect", DAO.DB_TYPE_SQLITE);
		dbTypeOfDialect.put("j.core.dao.dialect.DB2Dialect", DAO.DB_TYPE_DB2);
		dbTypeOfDialect.put("j.core.dao.dialect.SqlServerDialect", DAO.DB_TYPE_SQLSERVER);
		dbTypeOfDialect.put("j.core.dao.dialect.OracleDialect", DAO.DB_TYPE_ORACLE);
		dbTypeOfDialect.put("j.core.dao.dialect.HSQLDialect", DAO.DB_TYPE_HSQL);
	}
	
	/**
	 * constructor
	 *
	 */
	private DAOFactory(){
		init=false;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	synchronized public CachedMap getMaxColumnValues() throws Exception{
		if(maxColumnValues==null){
			maxColumnValues=new CachedMap("MaxTableKeys");
		}
		return maxColumnValues;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	synchronized public ConcurrentMap getMaxColumnValuesLocal() throws Exception{
		if(maxColumnValuesLocal==null){
			maxColumnValuesLocal=new ConcurrentMap();
		}
		return maxColumnValuesLocal;
	}
	
	/**
	 * 指明对表tbl进行更新操作时，需要同步锁定
	 * @param tbl
	 */
	public void registerSynTable(String tbl){
		if(tbl==null) return;
		tbl=tbl.toUpperCase();
		if(synchronizedTbls.contains(tbl)) return;
		synchronizedTbls.add(tbl);
	}
	
	/**
	 * 表tbl进行更新操作时是否需要同步锁定
	 * @param tbl
	 * @return
	 */
	public boolean isSynchronized(String tbl){
		tbl=tbl.toUpperCase();
		return synchronizedTbls.contains(tbl);
	}
	
	/**
	 * 得到表的同步锁
	 * @param tblName
	 * @return
	 */
	public Object getTableLock(String tblName){
		return tblName.toLowerCase().intern();
	}
	
	/**
	 * 根据数据库连接信息生成的key查找是否已经创建的DAOFactory实例
	 * @param key
	 * @return
	 */
	private static DAOFactory findFactory(String key){
		return (DAOFactory)factories.get(key);
	}
	
	/**
	 * DAOFactory的一个简单实例，该实例创建的DAO不能执行利用java反射进行的数据库操作，如返回List和插入、更新vo
	 * @param _dbType
	 * @param _jndi
	 * @return
	 * @throws Exception
	 */
	public static DAOFactory getSimpleInstance(String _dbName,String _dbType,String _jndi)throws Exception{
		DAOFactory factory=findFactory("simple>"+_dbType+">"+_jndi);
		if(factory!=null){
			return factory;
		}
		factory=new DAOFactory();
		factory.dbType=_dbType;
		factory.dbName=_dbName;
		ResourceBundle dialectProp=ResourceBundle.getBundle("j.core.dao.dialect.dialect");
		factory.dialectCls=dialectProp.getString(_dbType).trim();
		factory.connectionPropedrties.put("connection.datasource",_jndi);
		factory.key="simple>"+_dbType+">"+_jndi;
		factories.put(factory.key,factory);
		return factory;
	}
	
	/**
	 * DAOFactory的一个简单实例，该实例创建的DAO不能执行利用java反射进行的数据库操作，如返回List和插入、更新vo
	 * @param _dbType
	 * @param _DBDriver
	 * @param _DBUrl
	 * @param _DBUser
	 * @param _DBPassword
	 * @return
	 * @throws Exception
	 */
	public static DAOFactory getSimpleInstance(String _dbName,String _dbType,String _DBDriver,String _DBUrl,String _DBUser,String _DBPassword)throws Exception{
		DAOFactory factory=findFactory("simple>"+_dbType+">"+_DBDriver+">"+_DBUrl+">"+_DBUser+">"+_DBPassword);
		if(factory!=null){
			return factory; 
		}
		factory=new DAOFactory();
		factory.dbType=_dbType;
		factory.dbName=_dbName;
		ResourceBundle dialectProp=ResourceBundle.getBundle("j.core.dao.dialect.dialect");
		factory.dialectCls=dialectProp.getString(_dbType).trim();
		factory.connectionPropedrties.put("connection.driver_class",_DBDriver);
		factory.connectionPropedrties.put("connection.url",_DBUrl);
		if(_DBUser!=null&&!"".equalsIgnoreCase(_DBUser)){
			factory.connectionPropedrties.put("connection.username",_DBUser);
			factory.connectionPropedrties.put("connection.password",_DBPassword);
		}
		factory.key="simple>"+_dbType+">"+_DBDriver+">"+_DBUrl+">"+_DBUser+">"+_DBPassword;
		factories.put(factory.key,factory);
		return factory;
	}		

	/**
	 * 创建DAOFactory实例
	 * @param confFilePath
	 * @return
	 * @throws Exception
	 */
	public static DAOFactory getInstance(String _dbName,String confFilePath)throws Exception{
		if(!confFilePath.endsWith(".xml")) confFilePath+=".xml";
		
		DAOFactory factory=findFactory(confFilePath);
		if(factory!=null) return factory;

		String config= ResourceHelper.getString(confFilePath);
		if(config==null){
			log.log("unable to locate config file "+confFilePath, Logger.LEVEL_ERROR);
			return null;
		}
		Document doc = JUtilDom4j.parseString(config, "UTF-8");
		Element root = doc.getRootElement().element("session-factory");

		factory=new DAOFactory();
		factory.dbName=_dbName;

		
		List connProperties=root.elements("property");
		for(int i=0;connProperties!=null&&i<connProperties.size();i++){
			Element prop=(Element)connProperties.get(i);
			String attr=prop.attributeValue("name");
			String value=prop.getText();
			value=ResourceHelper.replaceEnvVariables(value);

			if(attr.equals("dialect")){
				factory.dialectCls=prop.getText();
				factory.dbType=(String)dbTypeOfDialect.get(value);
			}else if(attr.equals("plugin")){
				factory.plugin=(DAOPlugin)Class.forName(value).newInstance();
				factory.plugin.setFactory(factory);
			}else if(attr.equals("show_sql")){
				factory.showSql="true".equalsIgnoreCase(value);
			}else{
				factory.connectionPropedrties.put(attr,value);
			}
		}
		
		List classes=root.elements("mapping");
		for(int i=0;classes!=null&&i<classes.size();i++){
			Element cls=(Element)classes.get(i);
			String clsXml=cls.attributeValue("resource");

			String configX=ResourceHelper.getString(clsXml);
			if(configX == null){
				log.log("unable to locate config file "+clsXml, Logger.LEVEL_ERROR);
				continue;
			}

			Document docX = JUtilDom4j.parseString(configX, "UTF-8");
			Element rootX = docX.getRootElement().element("class");
			
			String clsName=rootX.attributeValue("name");
			String tblName=rootX.attributeValue("table");
			String tblNameLowerCase=tblName.toLowerCase();

			factory.tblNames.add(tblName);
			factory.DBConf.put(clsName+".name",tblName);//这样可以根据类名找到表名
			factory.DBConf.put(tblNameLowerCase+".name",tblName);
			factory.DBConf.put(tblNameLowerCase+".class",clsName);
			log.log(tblNameLowerCase+".class => "+clsName, -1);
			
			Element pk=rootX.element("id");
			if(pk!=null){
				factory.DBConf.put(tblNameLowerCase+".pk",pk.attributeValue("column"));
				factory.DBConf.put(clsName+".pk",pk.attributeValue("column"));//这样可以根据类名找到主键
			}
			
			List<Column> colsList=new ArrayList();
			List properties=rootX.elements("property");
			for(int j=0;properties!=null&&j<properties.size();j++){
				Element property=(Element)properties.get(j);
				
				String fieldName=property.attributeValue("name");
				String colName=property.attributeValue("column");
				String fieldNameOld=JUtilBean.colNameToVariableNameOld(colName);

				int colType=SQLUtil.getJavaTypeValue(property.attributeValue("type"));
				
				Column col=new Column(colName,colType,"true".equals(property.attributeValue("gzip")));
				col.fieldName=fieldName;
				colsList.add(col);
				
				factory.colsType.put(tblNameLowerCase+"."+fieldName,Integer.valueOf(col.colType));
				factory.colsName.put(tblNameLowerCase+"."+fieldName,col.colName);	
				factory.colsIsGzip.put(tblNameLowerCase+"."+fieldName,property.attributeValue("gzip"));

				factory.colsType.put(tblNameLowerCase+"."+fieldNameOld,Integer.valueOf(col.colType));
				factory.colsName.put(tblNameLowerCase+"."+fieldNameOld,col.colName);
				factory.colsIsGzip.put(tblNameLowerCase+"."+fieldNameOld,property.attributeValue("gzip"));
			}	
			factory.colsOfTables.put(tblNameLowerCase,colsList);	
			
			Class clazz=Class.forName(clsName);
			Field[] fields=clazz.getDeclaredFields();
			for(int k=0;k<fields.length;k++){
				String fieldName=fields[k].getName();
				try{
					Method setter=JUtilBean.getSetter(clazz,fieldName,new Class[]{fields[k].getType()});
					factory.setters.put(tblNameLowerCase+"."+fieldName,setter);
					factory.setterNames.put(tblNameLowerCase+"."+fieldName,JUtilBean.getSetterName(fieldName));
				}catch(Exception e){}
			}
		}			

		factory.key=confFilePath;
		factories.put(factory.key,factory);
		return factory;
	}	
	
	/**
	 * 创建DAOFactory实例
	 * @param confFilePath
	 * @param _jndi
	 * @return
	 * @throws Exception
	 */
	public static DAOFactory getInstance(String _dbName,String confFilePath,String _jndi)throws Exception{
		if(!confFilePath.endsWith(".xml")) confFilePath+=".xml";

		DAOFactory factory=findFactory(confFilePath+">"+_jndi);
		if(factory!=null) return factory;

		String config= ResourceHelper.getString(confFilePath);
		if(config==null){
			log.log("unable to locate config file "+confFilePath, Logger.LEVEL_ERROR);
			return null;
		}
		Document doc = JUtilDom4j.parseString(config, "UTF-8");
		Element root = doc.getRootElement().element("session-factory");

		factory=new DAOFactory();
		factory.dbName=_dbName;
		
		List connProperties=root.elements("property");
		for(int i=0;connProperties!=null&&i<connProperties.size();i++){
			Element prop=(Element)connProperties.get(i);
			String attr=prop.attributeValue("name");
			String value=prop.getText();
			value=ResourceHelper.replaceEnvVariables(value);
			
			if(attr.equals("dialect")){
				factory.dialectCls=prop.getText();
				factory.dbType=(String)dbTypeOfDialect.get(value);
			}else if(attr.equals("plugin")){
				factory.plugin=(DAOPlugin)Class.forName(value).newInstance();
				factory.plugin.setFactory(factory);
			}else if(attr.equals("show_sql")){
				factory.showSql="true".equalsIgnoreCase(value);
			}else{
				factory.connectionPropedrties.put(""+attr,value);
			}
		}
		
		List classes=root.elements("mapping");
		for(int i=0;classes!=null&&i<classes.size();i++){
			Element cls=(Element)classes.get(i);
			String clsXml=cls.attributeValue("resource");

			String configX=ResourceHelper.getString(clsXml);
			if(configX == null){
				log.log("unable to locate config file "+clsXml, Logger.LEVEL_ERROR);
				continue;
			}

			Document docX = JUtilDom4j.parseString(configX, "UTF-8");
			Element rootX = docX.getRootElement().element("class");
			
			String clsName=rootX.attributeValue("name");
			String tblName=rootX.attributeValue("table");
			String tblNameLowerCase=tblName.toLowerCase();

			factory.tblNames.add(tblName);
			factory.DBConf.put(clsName+".name",tblName);//这样可以根据类名找到表名
			factory.DBConf.put(tblNameLowerCase+".name",tblName);
			factory.DBConf.put(tblNameLowerCase+".class",clsName);
			log.log(tblNameLowerCase+".class => "+clsName, -1);

			Element pk=rootX.element("id");
			if(pk!=null){
				factory.DBConf.put(tblNameLowerCase+".pk",pk.attributeValue("column"));
				factory.DBConf.put(clsName+".pk",pk.attributeValue("column"));//这样可以根据类名找到主键
			}
			
			List colsList=new ArrayList();
			List properties=rootX.elements("property");
			for(int j=0;properties!=null&&j<properties.size();j++){
				Element property=(Element)properties.get(j);
				
				String fieldName=property.attributeValue("name");
				String colName=property.attributeValue("column");
				String fieldNameOld=JUtilBean.colNameToVariableNameOld(colName);

				int colType=SQLUtil.getJavaTypeValue(property.attributeValue("type"));
				
				Column col=new Column(colName,colType,"true".equals(property.attributeValue("gzip")));
				col.fieldName=fieldName;
				colsList.add(col);
				
				factory.colsType.put(tblNameLowerCase+"."+fieldName,Integer.valueOf(col.colType));
				factory.colsName.put(tblNameLowerCase+"."+fieldName,col.colName);	
				factory.colsIsGzip.put(tblNameLowerCase+"."+fieldName,property.attributeValue("gzip"));

				factory.colsType.put(tblNameLowerCase+"."+fieldNameOld,Integer.valueOf(col.colType));
				factory.colsName.put(tblNameLowerCase+"."+fieldNameOld,col.colName);
				factory.colsIsGzip.put(tblNameLowerCase+"."+fieldNameOld,property.attributeValue("gzip"));
			}	
			
			factory.colsOfTables.put(tblNameLowerCase,colsList);	
			
			Class clazz=Class.forName(clsName);
			Field[] fields=clazz.getDeclaredFields();
			for(int k=0;k<fields.length;k++){
				String fieldName=fields[k].getName();
				try{
					Method setter=JUtilBean.getSetter(clazz,fieldName,new Class[]{fields[k].getType()});
					factory.setters.put(tblNameLowerCase+"."+fieldName,setter);
					factory.setterNames.put(tblNameLowerCase+"."+fieldName,JUtilBean.getSetterName(fieldName));
				}catch(Exception e){}
			}
		}			

		factory.connectionPropedrties.put("connection.datasource",_jndi);
		factory.key=confFilePath+">"+_jndi;
		factories.put(factory.key,factory);
		return factory;		
	}
	
	/**
	 * 创建DAOFactory实例
	 * @param confFilePath
	 * @param _DBDriver
	 * @param _DBUrl
	 * @param _DBUser
	 * @param _DBPassword
	 * @return
	 * @throws Exception
	 */
	public static DAOFactory getInstance(String _dbName,String confFilePath,String _DBDriver,String _DBUrl,String _DBUser,String _DBPassword)throws Exception{
		if(!confFilePath.endsWith(".xml")) confFilePath+=".xml";
		
		DAOFactory factory=findFactory(confFilePath+">"+_DBDriver+">"+_DBUrl+">"+_DBUser+">"+_DBPassword);
		if(factory!=null) return factory;

		String config= ResourceHelper.getString(confFilePath);
		if(config==null){
			log.log("unable to locate config file "+confFilePath, Logger.LEVEL_ERROR);
			return null;
		}
		Document doc = JUtilDom4j.parseString(config, "UTF-8");
		Element root = doc.getRootElement().element("session-factory");

		factory=new DAOFactory();
		factory.dbName=_dbName;
		
		List connProperties=root.elements("property");
		for(int i=0;connProperties!=null&&i<connProperties.size();i++){
			Element prop=(Element)connProperties.get(i);
			String attr=prop.attributeValue("name");
			String value=prop.getText();
			value=ResourceHelper.replaceEnvVariables(value);
			
			if(attr.equals("dialect")){
				factory.dialectCls=prop.getText();
				factory.dbType=dbTypeOfDialect.get(value);
			}else if(attr.equals("plugin")){
				factory.plugin=(DAOPlugin)Class.forName(value).newInstance();
				factory.plugin.setFactory(factory);
			}else if(attr.equals("show_sql")){
				factory.showSql="true".equalsIgnoreCase(value);
			}else{
				factory.connectionPropedrties.put(attr,value);
			}
		}
		
		List classes=root.elements("mapping");
		for(int i=0;classes!=null&&i<classes.size();i++){
			Element cls=(Element)classes.get(i);
			String clsXml=cls.attributeValue("resource");

			String configX=ResourceHelper.getString(clsXml);
			if(configX == null){
				log.log("unable to locate config file "+clsXml, Logger.LEVEL_ERROR);
				continue;
			}

			Document docX = JUtilDom4j.parseString(configX, "UTF-8");
			Element rootX = docX.getRootElement().element("class");
			
			String clsName=rootX.attributeValue("name");
			String tblName=rootX.attributeValue("table");
			String tblNameLowerCase=tblName.toLowerCase();
			
			factory.tblNames.add(tblName);
			factory.DBConf.put(clsName+".name",tblName);//这样可以根据类名找到表名
			factory.DBConf.put(tblNameLowerCase+".name",tblName);
			factory.DBConf.put(tblNameLowerCase+".class",clsName);
			log.log(tblNameLowerCase+".class => "+clsName, -1);

			Element pk=rootX.element("id");
			if(pk!=null){
				factory.DBConf.put(tblNameLowerCase+".pk",pk.attributeValue("column"));
				factory.DBConf.put(clsName+".pk",pk.attributeValue("column"));//这样可以根据类名找到主键
			}
			
			List colsList=new ArrayList();
			List properties=rootX.elements("property");
			for(int j=0;properties!=null&&j<properties.size();j++){
				Element property=(Element)properties.get(j);
				
				String fieldName=property.attributeValue("name");
				String colName=property.attributeValue("column");
				String fieldNameOld=JUtilBean.colNameToVariableNameOld(colName);

				int colType=SQLUtil.getJavaTypeValue(property.attributeValue("type"));
				
				Column col=new Column(colName,colType,"true".equals(property.attributeValue("gzip")));
				col.fieldName=fieldName;
				colsList.add(col);
				
				factory.colsType.put(tblNameLowerCase+"."+fieldName,Integer.valueOf(col.colType));
				factory.colsName.put(tblNameLowerCase+"."+fieldName,col.colName);
				factory.colsIsGzip.put(tblNameLowerCase+"."+fieldName,property.attributeValue("gzip"));

				factory.colsType.put(tblNameLowerCase+"."+fieldNameOld,Integer.valueOf(col.colType));
				factory.colsName.put(tblNameLowerCase+"."+fieldNameOld,col.colName);
				factory.colsIsGzip.put(tblNameLowerCase+"."+fieldNameOld,property.attributeValue("gzip"));
			}	
			
			factory.colsOfTables.put(tblNameLowerCase,colsList);	
			
			Class clazz=Class.forName(clsName);
			Field[] fields=clazz.getDeclaredFields();
			for(int k=0;k<fields.length;k++){
				String fieldName=fields[k].getName();
				try{
					Method setter=JUtilBean.getSetter(clazz,fieldName,new Class[]{fields[k].getType()});
					factory.setters.put(tblNameLowerCase+"."+fieldName,setter);
					factory.setterNames.put(tblNameLowerCase+"."+fieldName,JUtilBean.getSetterName(fieldName));
				}catch(Exception e){}
			}
		}			
		
		factory.connectionPropedrties.put("connection.driver_class",_DBDriver);
		factory.connectionPropedrties.put("connection.url",_DBUrl);
		if(_DBUser!=null&&!"".equalsIgnoreCase(_DBUser)){
			factory.connectionPropedrties.put("connection.username",_DBUser);
			factory.connectionPropedrties.put("connection.password",_DBPassword);	
		}	
		factory.key=confFilePath+">"+_DBDriver+">"+_DBUrl+">"+_DBUser+">"+_DBPassword;
		factories.put(factory.key,factory);
		return factory;
	}	
	
	/**
	 * 设置连接池管理类
	 * @param cls 
	 */
	public void setConnectionProviderClass(String cls){
		this.connectionPropedrties.put("connection.provider_class",cls);
	}
	
	/**
	 * 设置连接池最大连接数
	 * @param size
	 */
	public void setMaxPoolSize(int size){
		this.connectionPropedrties.put("connection.pool_size",Integer.toString(size));
		this.connectionPropedrties.put("connection.dbcp.maxActive",Integer.toString(size));
		this.connectionPropedrties.put("connection.c3p0.max_size",Integer.toString(size));
	}
	
	/**
	 * 初始化连接池类，启动监控线程
	 * @throws Exception
	 */
	public void init()throws Exception{
		synchronized(this){
			if(init) return;
			connProvider=ConnectionProviderFactory.newConnectionProvider(connectionPropedrties);
	
			Thread th=new Thread(this);
			th.start();
			log.log("dao 监控线程启动！",-1);
			
			init=true;
		}
	}
	
	

	
	
	/**
	 * 
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	public DAO createDAO(Class clz)throws Exception {
		return createDAO(clz,null);
	}
	
	
	/**
	 * 
	 * @param clz
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public DAO createDAO(Class clz,long timeout)throws Exception {
		return createDAO(clz,timeout,null);
	}
	
	
	/**
	 * 
	 * @param clz
	 * @param mirror
	 * @return
	 * @throws Exception
	 */
	public DAO createDAO(Class clz,DBMirror mirror)throws Exception {
		if(!init){
			this.init();
		}

		DAO _dao = null;
		try {
			if(dialectCls==null||dialectCls.equals("")){
				throw new Exception("没有指定dialect");
			}
			String clzName=clz.getName();
			
			if(callers.containsKey(clzName)){
				Long count=(Long)callers.get(clzName);
				count=Long.valueOf(count.longValue()+1);
				callers.put(clzName,count);
			}else{
				callers.put(clzName,Long.valueOf(1));
			}

			if("true".equals(JProperties.getProperty("DAO.PrintLog"))) {
				log.log("caller " + clzName + " has called {createDAO(Class clz)} for " + callers.get(clzName) + " times!", Logger.LEVEL_DEBUG);
			}

			_dao = (RdbmsDao)Class.forName(dialectCls).newInstance();
			_dao.setCaller(clz.getName());
	
			Connection connection=null;
			connection=connProvider.getConnection();

			connection.setAutoCommit(true);
			_dao.setConnection(connection);
			_dao.setFactory(this);
			_dao.setMirror(mirror);
			
			DAOProxy invocation=new DAOProxy();
			DAO dao= (DAO)invocation.bind(_dao);
			daos.add(dao);
			return dao;
			//return (DAO)_dao;
		}catch (Exception e) {
			log.log("create dao failed:"+connectionPropedrties.toString(),Logger.LEVEL_FATAL);
			e.printStackTrace();
			try{
				_dao.close();
			}catch(Exception ex){}
			try{
				_dao.close();
			}catch(Exception ex){}
			throw e;
		}
	}
	
	
	/**
	 * 
	 * @param clz
	 * @param timeout
	 * @param mirror
	 * @return
	 * @throws Exception
	 */
	public DAO createDAO(Class clz,long timeout,DBMirror mirror)throws Exception {
		if(!init){
			this.init();
		}
		DAO _dao = null;
		try {
			if(dialectCls==null||dialectCls.equals("")){
				throw new Exception("没有指定dialect");
			}
			
			String clzName=clz.getName();
			if(callers.containsKey(clzName)){
				Long count=(Long)callers.get(clzName);
				count=Long.valueOf(count.longValue()+1);
				callers.put(clzName,count);
			}else{
				callers.put(clzName,Long.valueOf(1));
			}

			if("true".equals(JProperties.getProperty("DAO.PrintLog"))) {
				log.log("caller " + clzName + " has called {createDAO(Class clz)} for " + callers.get(clzName) + " times!", Logger.LEVEL_DEBUG);
			}
			
			_dao = (RdbmsDao)Class.forName(dialectCls).newInstance();
			_dao.setCaller(clz.getName());

			Connection connection=null;

			connection=connProvider.getConnection();
			connection.setAutoCommit(true);
			_dao.setConnection(connection);
			_dao.setFactory(this);
			_dao.setMirror(mirror);
			_dao.setTimeout(timeout);
			
			DAOProxy invocation=new DAOProxy();
			DAO dao= (DAO)invocation.bind(_dao);
			daos.add(dao);
			return dao;
			//return (DAO)_dao;
		}catch (Exception e) {
			try{
				_dao.close();
			}catch(Exception ex){}
			try{
				_dao.close();
			}catch(Exception ex){}
			throw e;
		}
	}
	
	/**
	 * 关闭DAO
	 * @param conn
	 * @throws Exception
	 */
	public void close(DAO dao,Connection conn)throws Exception{
		if(connProvider!=null){
			//log.log("release connection to pool",Logger.LEVEL_DEBUG);
			connProvider.closeConnection(conn);
		}else{
			//log.log("close connection",Logger.LEVEL_DEBUG);
			conn.close();
		}
		String caller=dao.getCaller();
		if(caller!=null){
			String key=caller+"*close";
			if(callers.containsKey(key)){
				Long count=(Long)callers.get(key);
				count=Long.valueOf(count.longValue()+1);
				callers.put(key,count);
			}else{
				callers.put(key,Long.valueOf(1));
			}

			if("true".equals(JProperties.getProperty("DAO.PrintLog"))) {
				log.log("caller " + caller + " has called {close(DAO dao,Connection connection)} for " + callers.get(key) + " times!", Logger.LEVEL_DEBUG);
			}
		}
	}

	/**
	 *
	 * @param tblName
	 * @return
	 * @throws SQLException
	 */
	public String getTblClass(String tblName)throws SQLException{
		Database db=DB.database(this.dbName);
		tblName=db.getMetaTable(tblName);
		
		if(DBConf==null||DBConf.isEmpty()){
			throw new SQLException("DAO工厂没有被（正确）初始化");
		}
		tblName=tblName.toLowerCase();
		return this.DBConf.get(tblName+".class");
	}
	
	/**
	 * 
	 * @return
	 */
	public ConcurrentList getTables(){
		return this.tblNames;
	}

	/**
	 *
	 * @param tableName
	 * @return
	 */
	public boolean tableConfigured(String tableName){
		for(int i=0; i<this.tblNames.size(); i++){
			if(this.tblNames.get(i).equalsIgnoreCase(tableName)) return true;
		}
		return false;
	}

	/**
	 *
	 * @param tblName
	 * @return
	 * @throws SQLException
	 */
	public String getTrueTblName(String tblName)throws SQLException{
		if(DBConf==null||DBConf.isEmpty()){
			return tblName;
		}
		String tblNameTmp=tblName.toLowerCase();
		if(DBConf.containsKey(tblNameTmp+".name")){
			return DBConf.get(tblNameTmp+".name");
		}else{
			return tblName;
		}
	}	
	
	/**
	 * 得到与vo对应的，大小写与实际情况完全一致的表名
	 * @param vo
	 * @return String
	 * @throws SQLException
	 */
	public String getTrueTblName(Object vo)throws SQLException{
		if(DBConf==null||DBConf.isEmpty()){
			throw new SQLException("DAO工厂没有被（正确）初始化");
		}
		String clsName=vo.getClass().getName();
		return (String)DBConf.get(clsName+".name");
	}
	
	/**
	 * 得到与类对应的，大小写与实际情况完全一致的表名
	 * @param cls
	 * @return
	 * @throws SQLException
	 */
	public String getTrueTblNameOfCls(Class cls)throws SQLException{
		if(DBConf==null||DBConf.isEmpty()){
			throw new SQLException("DAO工厂没有被（正确）初始化");
		}
		String clsName=cls.getName();
		return (String)DBConf.get(clsName+".name");
	}

	/**
	 *
	 * @param tblName
	 * @return
	 * @throws SQLException
	 */
	public String getPkColumnName(String tblName)throws SQLException{
		if(DBConf==null||DBConf.isEmpty()){
			throw new SQLException("DAO工厂没有被（正确）初始化");
		}

		Database db=DB.database(this.dbName);
		tblName=db.getMetaTable(tblName);
		
		String tblNameTmp=tblName.toLowerCase();
		return (String)DBConf.get(tblNameTmp+".pk");
	}		
	
	/**
	 * 得到与vo对应的表的主键名
	 * @param vo
	 * @return String
	 * @throws SQLException
	 */
	public String getPkColumnName(Object vo)throws SQLException{
		if(DBConf==null||DBConf.isEmpty()){
			throw new SQLException("DAO工厂没有被（正确）初始化");
		}
		String clsName=vo.getClass().getName();
		return (String)DBConf.get(clsName+".pk");
	}
	
	/**
	 * 得到与类对应的表的主键名
	 * @param cls
	 * @return
	 * @throws SQLException
	 */
	public String getPkColumnNameOfClass(Class cls)throws SQLException{
		if(DBConf==null||DBConf.isEmpty()){
			throw new SQLException("DAO工厂没有被（正确）初始化");
		}
		
		String clsName=cls.getName();
		return (String)DBConf.get(clsName+".pk");	
	}
	
	/**
	 * 得到表tableName的列(j.core.dao.Column)的List
	 * @param tableName
	 * @return
	 */
	public List getColumns(String tableName){
		Database db=DB.database(this.dbName);
		tableName=db.getMetaTable(tableName);
		
		return (List)this.colsOfTables.get(tableName.toLowerCase());
	}
	
	/**
	 * 给定表的列名或对应vo类的变量名，得到与表tableName对应的vo类的该字段的set方法
	 * @param tableName
	 * @param colNameOrFieldName
	 * @return
	 */
	public Method getSetter(String tableName,String colNameOrFieldName){		
		Database db=DB.database(this.dbName);
		tableName=db.getMetaTable(tableName);

		Method m=this.setters.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableName(colNameOrFieldName));
		if(m==null) m=this.setters.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableNameOld(colNameOrFieldName));

		return m;
	}
	
	/**
	 * 
	 * @param tableName
	 * @param colNameOrFieldName
	 * @return
	 */
	public String getSetterName(String tableName,String colNameOrFieldName){		
		Database db=DB.database(this.dbName);
		tableName=db.getMetaTable(tableName);
		
		String n=this.setterNames.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableName(colNameOrFieldName));
		if(n==null) n=this.setterNames.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableNameOld(colNameOrFieldName));
		return n;
	}
	
	/**
	 * 得到未注册vo类的get方法
	 * @param cls
	 * @param fieldName
	 * @param paras
	 * @return
	 * @throws Exception
	 */
	public Method getUnregisterSetter(Class cls,String fieldName,Class[] paras)throws Exception{
		String key=cls.getName()+"."+fieldName;
		if(this.unregisterSetters.containsKey(key)){
			return (Method)this.unregisterSetters.get(key);
		}else{
			Method setter=null;
			try{
				setter=JUtilBean.getSetter(cls, fieldName, paras);
				this.unregisterSetters.put(key,setter);
			}catch(Exception e){}
			return setter;
		}
	}
	
	/**
	 * 
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public String getUnregisterSetterName(String fieldName)throws Exception{
		return JUtilBean.getSetterName(fieldName);
	}
	
	/**
	 * 得到表tableName的列名
	 * @param tableName
	 * @param colNameOrFieldName
	 * @return
	 */
	public String getColName(String tableName,String colNameOrFieldName){
		Database db=DB.database(this.dbName);
		tableName=db.getMetaTable(tableName);
		
		String n=this.colsName.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableName(colNameOrFieldName)).toString();
		if(n==null) n=this.colsName.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableNameOld(colNameOrFieldName)).toString();

		return n;
	}
	
	/**
	 * 得到表tableName的列类型
	 * @param tableName
	 * @param colNameOrFieldName
	 * @return
	 */
	public int getColType(String tableName,String colNameOrFieldName){
		Database db=DB.database(this.dbName);
		tableName=db.getMetaTable(tableName);

		Integer type=(Integer)this.colsType.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableName(colNameOrFieldName));
		if(type==null) type=(Integer)this.colsType.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableNameOld(colNameOrFieldName));
		return type==null?0:type.intValue();
	}
	
	/**
	 * 得到表tableName的列类型
	 * @param tableName
	 * @param colNameOrFieldName
	 * @return
	 */
	public boolean getColIsGzip(String tableName,String colNameOrFieldName){
		Database db=DB.database(this.dbName);
		tableName=db.getMetaTable(tableName);

		String isGzip=(String)this.colsIsGzip.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableName(colNameOrFieldName));
		if(isGzip==null) isGzip=(String)this.colsIsGzip.get(tableName.toLowerCase()+"."+JUtilBean.colNameToVariableNameOld(colNameOrFieldName));
		
		return "true".equals(isGzip);
	}
		
	
	/**
	 * 数据库类型
	 * @return
	 */
	public String getDbType(){
		return dbType;
	}
		
	
	/**
	 * 数据库名称
	 * @return
	 */
	public String getDbName(){
		return dbName;
	}
	
	/**
	 * 插件
	 * @return
	 */
	public DAOPlugin getPlugin(){
		return plugin;
	}

	/**
	 *
	 * @return
	 */
	public boolean isShowSql(){
		return this.showSql;
	}

	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize(){
		try{
			if(plugin!=null) plugin.destroy();
		}catch(Exception e){
			log.log(e,Logger.LEVEL_DEBUG);
		}		
	}

	/**
	 *
	 * @return
	 */
	public Map<String, Integer> ofCallers(){
		Map<String, Integer> map = new HashMap<>();
		for(int i=0;i<daos.size();i++) {
			DAO dao = daos.get(i);

			if (dao == null) {
				daos.remove(i);
				i--;
				continue;
			}

			String caller = dao.getCaller();
			if(JUtilString.isBlank(caller)) caller="undefined";

			Integer count = map.get(caller);
			if(count==null) count=1;
			else count++;
			map.put(caller, count);
		}

		return map;
	}
	
	@Override
	public void run() {
		while(true){
			try{
				//long now=SysUtil.getNow();
				
				for(int i=0;i<daos.size();i++){
					DAO dao=daos.get(i);
					
					if(dao==null){
						daos.remove(i);
						i--;
						continue;
					}
					
					try{	
						if(dao.isClosed()){//已经关闭，从列表中移除
							daos.remove(i);
							dao=null;
							
							i--;
							continue;
						}				
					}catch(Exception ex){
						log.log(ex,Logger.LEVEL_ERROR);
					}

					if(SysUtil.getNow()-dao.getCreateTime() > 300000L
							&& !"j.core.dao.QueryExecutor".equals(dao.getCaller())
							&& "true".equals(JProperties.getProperty("DAO.PrintLog"))){
						log.log(dao.getCaller()+",dao("+this.dbName+") has been used for more than an 5 minutes, please pay attention!",-1);
					}

					try{
						if(dao.getTimeout()>0
								&&!dao.isUsing()
								&&SysUtil.getNow()-dao.getLastUsingTime() > dao.getTimeout()){//没有处在执行操作中，且已经超时，故关闭并从列表中移除
							log.log(dao.getCaller()+",dao("+this.dbName+") is not closed after operation,so close it and remove from the queue!",-1);

							if(dao.isInTransaction()){//如果处于事务中，则回滚
								dao.rollback();
								log.log(dao.getCaller()+",dao("+this.dbName+") is not closed after operation,and it is not committed, rollback!",-1);
							}
							daos.remove(i);
							dao.close();
							dao=null;

							i--;
							continue;
						}
					}catch(Exception ex){
						log.log(ex,Logger.LEVEL_ERROR);
					}

					try{
						//保持连接
						if(!dao.isInTransaction() && SysUtil.getNow()-dao.getLastTest() >= 300000L){
							dao.setLastTest(SysUtil.getNow());
							DBMirror mirror=dao.getMirror();
							if(mirror==null) continue;

							Database db=mirror.getDb();
							if(db.getTestSql()!=null&&!"".equals(db.getTestSql())){
								try{
									dao.executeSQL(db.getTestSql());
									//if("true".equals(JProperties.getProperty("DAO.PrintLog"))){
									//	log.log("execute test sql("+db.getName()+") successfully:"+db.getTestSql(), -1);
									//}
								}catch(Exception ex){
									log.log("execute test sql("+db.getName()+") error:"+db.getTestSql(), -1);
									log.log(ex,Logger.LEVEL_ERROR);

									daos.remove(i);
									if(dao!=null){
										try{
											dao.close();
											dao=null;
										}catch(Exception e){}
									}
									i--;
								}
							}
						}
					}catch(Exception ex){
						log.log(ex,Logger.LEVEL_ERROR);
					}
				}
			}catch(Exception e){
				log.log(e, Logger.LEVEL_FATAL);
			}
			
			try{
				Thread.sleep(30000L);
			}catch(Exception e){}
		}
	}
	
	/**
	 * test
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println(JUtilBean.colNameToVariableName("uid"));
		System.out.println(JUtilBean.colNameToVariableName("userId"));
	}
}
		
		
