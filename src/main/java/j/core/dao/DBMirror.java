package j.core.dao;

import j.core.dao.config.Partition;
import j.core.nvwa.resource.ResourceHelper;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;


/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class DBMirror{
	private static Logger log=Logger.create(DBMirror.class);
	public static final int STATUS_AVAILABLE=1;
	public static final int STATUS_UNAVAILABLE=0;
	private DAOFactory factory;
	private Database db;


	private ConcurrentMap<String, DAOFactory> factories=new ConcurrentMap<>();
	private ConcurrentMap<String, Database> dbs=new ConcurrentMap<>();

	private String uuid;
	private String dbname;
	private String config;

	private boolean avail=true;
	private boolean readable=true;
	private boolean insertable=true;
	private boolean updatable=true;
	private int priority=1;
	
	public volatile boolean shutdown=false;
	public volatile int status=1;

	/**
	 *
	 * @param db
	 * @param config
	 */
	public DBMirror(Database db,String config){
		this.db=db;
		this.dbname=db.getName();
		this.config= ResourceHelper.replaceEnvVariables(config);
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public DAO connect(Class clazz) throws Exception{
		return this.connect(clazz, 0);
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public DAO connect(Class clazz,long timeout) throws Exception{
		synchronized(this){
			if(factory==null){
				factory=DAOFactory.getInstance(dbname,config);
			}
		}
		
		return factory.createDAO(clazz, timeout,this);
	}

	/**
	 *
	 * @param clazz
	 * @param tableNames
	 * @return
	 * @throws Exception
	 */
	public DAO connectForTables(Class clazz, String[] tableNames) throws Exception{
		return this.connectForTables(clazz, tableNames,0);
	}

	/**
	 *
	 * @param clazz
	 * @param tableNames
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public DAO connectForTables(Class clazz, String[] tableNames, long timeout) throws Exception{
		synchronized(this){
			String partition=selectPartition(tableNames);

			Database partitionDB=partition==null?null:DB.database(partition);
			if(partitionDB!=null) return partitionDB.connect(clazz, timeout);

			return this.connect(clazz, timeout);
		}
	}

	/**
	 * 选择分库
	 * @param tableNames
	 * @return null表示使用主库
	 */
	private String selectPartition(String[] tableNames){
		if(tableNames==null || tableNames.length==0) return null;
		Partition partition=null;
		for(int i=0; i<tableNames.length; i++){
			if(partition==null){
				//分库
				partition=this.db.getPartition(tableNames[i]);
			}else{
				//前一个表对应分库已经找到，如果目前表也有对应分库，但两个分库不相同，则使用主数据库
				Partition partitionOther=this.db.getPartition(tableNames[i]);
				if(partitionOther!=null && !partitionOther.getDbName().equals(partition.getDbName())){
					partition=null;
					break;
				}
			}
		}

		return partition==null?null:partition.getDbName();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean available(){
		if(shutdown){
			status=DBMirror.STATUS_UNAVAILABLE;
			return false;
		}

		if(!avail){
			status=DBMirror.STATUS_UNAVAILABLE;
			return false;
		}

		DAO dao=null;
		try{
			dao=this.connect(this.getClass());
			dao.close();

			status=DBMirror.STATUS_AVAILABLE;
			return true;
		}catch(Exception e){
			status=DBMirror.STATUS_UNAVAILABLE;
			log.log(e,Logger.LEVEL_INFO);
			if(dao!=null){
				try{
					dao.close();
				}catch(Exception ex){}
			}
			return false;
		}
	}
	
	/**
	 * 
	 *
	 */
	public void shutdown(){
		shutdown=true;
		if(factory!=null){
			try{
				factory.finalize();
				factory=null;
			}catch(Exception e){}
		}
	}
}