package j.core.dao;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.Global;
import j.core.common.JProperties;
import j.core.dao.config.Partition;
import j.core.dao.config.Table;
import j.log.Logger;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.util.ConcurrentMap;
import j.util.JUtilMath;
import j.util.JUtilString;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "肖炯", date = "2021/07/21", description = "DAO数据源管理类", reviewers = {})
public class DB implements Consumer {
	@FieldDescription(description = "日志输出")
	private static Logger log=Logger.create(DB.class);

	@FieldDescription(description = "是否启用数据库功能")
	private static boolean enabled=true;

	@FieldDescription(description = "数据源")
	private static ConcurrentMap<String, Database> databases=new ConcurrentMap<>();

	@FieldDescription(description = "数据源，以用途分组")
	private static ConcurrentMap<String, List<Database>> databasesFor=new ConcurrentMap<>();

	@FieldDescription(description = "最新配置信息")
	private static String config;

	@FieldDescription(description = "配置是否加载完毕")
	private static volatile boolean configLoaded=false;

	@FieldDescription(description = "是否初始化完毕")
	private static volatile boolean initialized=false;
	
	static{
		if(JProperties.getProperty("org.sqlite.lib.path")!=null){
			System.setProperty("org.sqlite.lib.path", JProperties.getProperty("org.sqlite.lib.path"));
		}
	}

	/**
	 * 
	 *
	 */
	public DB(){
		super();
	}

	/**
	 *
	 * @param _enabled
	 */
	public static void setEnabled(boolean _enabled){
		enabled=_enabled;
	}

	/**
	 *
	 * @return
	 */
	public static boolean getEnabled(){
		return enabled;
	}

	/**
	 *
	 * @param dbId
	 * @param caller
	 * @return
	 * @throws Exception
	 */
	public static DAO connect(String dbId,Class caller) throws Exception{
		return connect(dbId,caller,0,false);
	}

	/**
	 *
	 * @param dbId
	 * @param caller
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static DAO connect(String dbId,Class caller,long timeout) throws Exception{
		return connect(dbId,caller,timeout,false);
	}

	/**
	 *
	 * @param dbId
	 * @param caller
	 * @param readonly
	 * @return
	 * @throws Exception
	 */
	public static DAO connect(String dbId,Class caller,boolean readonly) throws Exception{
		return connect(dbId,caller,0,readonly);
	}

	/**
	 *
	 * @param dbId
	 * @param caller
	 * @param timeout
	 * @param readonly
	 * @return
	 * @throws Exception
	 */
	public static DAO connect(String dbId,Class caller,long timeout,boolean readonly) throws Exception{
		if(!DB.getEnabled()){
			throw new Exception("database module is not enabled.");
		}

		Database db=DB.database(dbId);
		if(db==null) db=DB.getApplicationDB();
		if(db==null){
			throw new Exception("database "+dbId+" is not exists.");
		}
		DAO dao=db.connect(caller,timeout);
		dao.setReadOnly(readonly);
		return dao;
	}

	/**
	 *
	 * @param dbId
	 * @param tableNames
	 * @param caller
	 * @return
	 * @throws Exception
	 */
	public static DAO connectForTables(String dbId, String[] tableNames, Class caller) throws Exception{
		return connectForTables(dbId,tableNames,caller,0,false);
	}

	/**
	 *
	 * @param dbId
	 * @param tableNames
	 * @param caller
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static DAO connectForTables(String dbId, String[] tableNames, Class caller,long timeout) throws Exception{
		return connectForTables(dbId,tableNames,caller,timeout,false);
	}

	/**
	 *
	 * @param dbId
	 * @param tableNames
	 * @param caller
	 * @param readonly
	 * @return
	 * @throws Exception
	 */
	public static DAO connectForTables(String dbId, String[] tableNames, Class caller,boolean readonly) throws Exception{
		return connectForTables(dbId,tableNames,caller,0,readonly);
	}

	/**
	 *
	 * @param dbId
	 * @param tableNames
	 * @param caller
	 * @param timeout
	 * @param readonly
	 * @return
	 * @throws Exception
	 */
	public static DAO connectForTables(String dbId, String[] tableNames, Class caller,long timeout,boolean readonly) throws Exception{
		Database db=DB.database(dbId);
		if(db==null) db=DB.getApplicationDB();
		if(db==null){
			throw new Exception("database "+dbId+" is not exists.");
		}
		DAO dao=db.connectForTables(caller, tableNames, timeout);
		dao.setReadOnly(readonly);
		return dao;
	}
	
	/**
	 * 
	 * @param dbId
	 * @return
	 */
	public static Database database(String dbId){
		while(!DB.configLoaded) Global.sleep1000Millis();

		return databases.get(dbId);
	}

	/**
	 *
	 * @param FOR
	 * @return
	 */
	public static List<Database> getDatabasesFor(String FOR){
		if(JUtilString.isBlank(FOR)) return getDatabasesFor(Global.S_DEFAULT);

		while(!DB.configLoaded) Global.sleep1000Millis();

		List<Database> dbs=databasesFor.get(FOR);
		if(dbs != null && !dbs.isEmpty()) return dbs;

		dbs=new ArrayList<>();
		List<Database> _databases=databases.listValues();
		for(int i=0; i<_databases.size(); i++){
			Database db=_databases.get(i);
			if(FOR.equalsIgnoreCase(db.getFOR())) dbs.add(db);
		}
		databasesFor.put(FOR, dbs);

		return dbs;
	}

	/**
	 * 框架数据库
	 * @return
	 */
	public static Database getJFrameworkDB(){
		List<Database> dbs=getDatabasesFor(Global.S_JFRAMEWORK);
		return dbs==null || dbs.isEmpty() ? null : dbs.get(0);
	}

	/**
	 * 框架数据库(日志)
	 * @return
	 */
	public static Database getJFrameworkDB4Log(){
		List<Database> dbs=getDatabasesFor(Global.S_JFRAMEWORK_LOG);
		return dbs==null || dbs.isEmpty() ? null : dbs.get(0);
	}

	/**
	 * 框架数据库
	 * @return
	 */
	public static Database getApplicationDB(){
		List<Database> dbs=getDatabasesFor(Global.S_DEFAULT);
		return dbs==null || dbs.isEmpty() ? getJFrameworkDB() : dbs.get(0);
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getDatabases(){
		while(!DB.configLoaded) Global.sleep1000Millis();

		return databases.listValues();
	}
	
	
	public static final String sqliteSynchronousFull="FULL";
	public static final String sqliteSynchronousNormal="NORMAL";
	public static final String sqliteSynchronousOff="OFF";
	public static void sqliteSetSynchronous(DAO dao,String synchronous) throws Exception{
		if(sqliteSynchronousFull.equalsIgnoreCase(synchronous)){
			dao.executeSQL("PRAGMA synchronous = FULL");
		}else if(sqliteSynchronousNormal.equalsIgnoreCase(synchronous)){
			dao.executeSQL("PRAGMA synchronous = NORMAL");
		}else if(sqliteSynchronousOff.equalsIgnoreCase(synchronous)){
			dao.executeSQL("PRAGMA synchronous = OFF");
		}
	}

	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource){
		try{
			initialized=false;
			configLoaded=false;

			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的JDAO节点）
			if(root.element("JDAO")!=null) root=root.element("JDAO");

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			List dbs=databases.listValues();
			for(int i=0;i<dbs.size();i++){
				Database db=(Database)dbs.get(i);
				db.shutdown();
			}
			databases.clear();

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				initialized=true;
				configLoaded=true;
				DB.setEnabled(false);
				return true;
			}
			DB.setEnabled(true);

			List dbEles=root.elements("database");
			for(int i=0;i<dbEles.size();i++){
				Element dbEle=(Element)dbEles.get(i);

				//基本信息
				Database db=new Database(dbEle.attributeValue("name"),
						dbEle.attributeValue("desc"),
						dbEle.attributeValue("for"),
						dbEle.attributeValue("min-uuid"),
						dbEle.attributeValue("max-uuid"),
						dbEle.attributeValue("db-key-prefix"),
						dbEle.attributeValue("TEST-SQL"));

				//镜像
				List<Element> mirrorEles=dbEle.elements("mirror");
				for(int j=0;j<mirrorEles.size();j++){
					Element mirrorEle=mirrorEles.get(j);
					DBMirror m=new DBMirror(db,mirrorEle.attributeValue("config"));

					m.setUuid(mirrorEle.attributeValue("uuid"));
					m.setAvail(!"false".equalsIgnoreCase(mirrorEle.attributeValue("avail")));
					m.setReadable(!"false".equalsIgnoreCase(mirrorEle.attributeValue("read")));
					m.setInsertable(!"false".equalsIgnoreCase(mirrorEle.attributeValue("insert")));
					m.setUpdatable(!"false".equalsIgnoreCase(mirrorEle.attributeValue("update")));

					if(JUtilMath.isInt(mirrorEle.attributeValue("priority"))){
						m.setPriority(Integer.parseInt(mirrorEle.attributeValue("priority")));
					}

					db.addMirror(m);
				}

				//分库配置
				List<Element> partitions=dbEle.elements("partition");
				for(int j=0; partitions!=null && j<partitions.size(); j++){
					Element partitionEle=partitions.get(j);

					Partition p=new Partition(partitionEle.attributeValue("name"));

					List<Element> tables=partitionEle.elements("table");
					for(int k=0; tables!=null && k<tables.size(); k++){
						p.addTable(tables.get(k).getTextTrim());
					}

					db.addPartition(p);
				}

				//全局配置
				Element global=dbEle.element("global");
				if(global!=null){
					Element timeLoadAsLocal=global.element("time-load-as-local");
					db.setTimeLoadAsLocal(timeLoadAsLocal!=null && "true".equalsIgnoreCase(timeLoadAsLocal.getTextTrim()));

					List<Element> metas=global.elements("meta");
					for(int j=0; metas!=null && j<metas.size(); j++){
						Element meta=metas.get(j);
						db.addMetaOfTable(meta.attributeValue("selector"), meta.getTextTrim());
					}
				}

				//表配置
				List<Element> tables=dbEle.elements("table");
				for(int j=0; tables!=null && j<tables.size(); j++){
					Element tableEle=tables.get(j);

					//基本信息
					Table table=new Table(tableEle.attributeValue("name"),
							tableEle.attributeValue("min-uuid"),
							tableEle.attributeValue("max-uuid"),
							tableEle.attributeValue("db-key-prefix"));

					Element timeLoadAsLocal=tableEle.element("time-load-as-local");
					table.setTimeLoadAsLocal(timeLoadAsLocal!=null && "true".equalsIgnoreCase(timeLoadAsLocal.getTextTrim()));

					List<Element> columns=tableEle.elements("column");
					for(int k=0; columns!=null && k<columns.size(); k++){
						Element columnEle=columns.get(k);
						j.core.dao.config.Column column=new j.core.dao.config.Column(columnEle.attributeValue("name"));
						column.setIgnoreWhileUpdateViaBean("true".equalsIgnoreCase(columnEle.attributeValue("ignoreWhileUpdateViaBean")));

						timeLoadAsLocal=columnEle.element("time-load-as-local");
						column.setTimeLoadAsLocal(timeLoadAsLocal!=null && "true".equalsIgnoreCase(timeLoadAsLocal.getTextTrim()));

						table.setColumn(column);
					}
				}

				databases.put(db.getName(),db);
				db.monitor();

				Thread thread=new Thread(db);
				thread.start();
				log.log("db "+db.getName()+","+db.getDesc()+" started!",-1);
			}

			//等待数据库初始化
			try{
				Thread.sleep(5000);
			}catch(Exception e){}

			configLoaded=true;
			initialized=true;

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);

			configLoaded=true;
			initialized=true;
			
			return false;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理JDAO.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("JDAO.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		//不进行自动更新
		return true;
	}
}