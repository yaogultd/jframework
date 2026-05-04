package j.core.dao;

import j.core.Startup;
import j.core.common.Global;
import j.core.dao.config.Column;
import j.core.dao.config.Partition;
import j.core.dao.config.Table;
import j.log.Logger;
import j.util.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class Database implements Runnable{
	private static Logger log=Logger.create(Database.class);
	private boolean timeLoadAsLocal=false;
	private String name;
	private String desc;
	private String FOR;
	private long minUuid;
	private long maxUuid;
	private String dbKeyPrefix;
	private String testSql;
	private ConcurrentList<DBMirror> mirrors=new ConcurrentList<>();
	private ConcurrentList<DBMirror> availables=new ConcurrentList<>();
	private ConcurrentList<String> availableUuids=new ConcurrentList<>();
	private ConcurrentMap<String, String> metaOfTables=new ConcurrentMap<>();
	private ConcurrentMap<String, Table> tables=new ConcurrentMap<>();
	private ConcurrentList<Partition> partitions=new ConcurrentList<>();
	private volatile boolean shutdown=false;


	/**
	 *
	 * @param name
	 * @param desc
	 * @param FOR
	 * @param minUuid
	 * @param maxUuid
	 * @param dbKeyPrefix
	 * @param testSql
	 */
	public Database(String name,String desc,String FOR,String minUuid,String maxUuid,String dbKeyPrefix,String testSql){
		this.name=name;
		this.desc=desc;
		this.FOR=FOR;
		this.minUuid=JUtilMath.isLong(minUuid)?Long.parseLong(minUuid):0;
		this.maxUuid=JUtilMath.isLong(maxUuid)?Long.parseLong(maxUuid):Long.MAX_VALUE;
		this.dbKeyPrefix=dbKeyPrefix;
		this.testSql=testSql;
	}

	/**
	 *
	 * @return
	 */
	public boolean isCluster(){
		return this.mirrors.size()>1;
	}

	/**
	 *
	 * @param selector
	 * @param tableName
	 */
	public void addMetaOfTable(String selector, String tableName){
		this.metaOfTables.put(selector, tableName);
	}

	/**
	 *
	 * @param tableName
	 * @param table
	 */
	public void addTableConfig(String tableName, Table table){
		this.tables.put(tableName, table);
	}

	/**
	 *
	 * @param tableName
	 * @return
	 */
	public Table getTableConfig(String tableName){
		return this.tables.get(tableName);
	}

	/**
	 *
	 * @param partition
	 */
	public void addPartition(Partition partition){
		this.partitions.add(partition);
	}

	/**
	 *
	 * @param tableName
	 * @return
	 */
	public Partition getPartition(String tableName){
		for(int i=0; i<this.partitions.size(); i++){
			if(this.partitions.get(i).responsible(tableName)) return this.partitions.get(i);
		}

		return null;
	}

	/**
	 *
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	public boolean ignoreWhileUpdateViaBean(String tableName, String columnName){
		Table table=this.getTableConfig(tableName);
		if(table==null) return false;

		Column column=table.getColumn(columnName);
		if(column==null) return false;

		return column.isIgnoreWhileUpdateViaBean();
	}

	/**
	 *
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	public boolean timeLoadAsLocal(String tableName, String columnName){
		Table table=this.getTableConfig(tableName);
		if(table==null) return this.isTimeLoadAsLocal();

		Column column=table.getColumn(columnName);
		if(column==null) return table.isTimeLoadAsLocal();

		return column.isTimeLoadAsLocal();
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
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public DAO connect(Class clazz,long timeout) throws Exception{
		DBMirror mirror=null;
		synchronized(this){
			int tries=0;
			while(mirror == null && tries<30){//尝试30次（每次失败后等待1秒）
				tries++;
				mirror=select();
				if(mirror != null) break;

				Global.sleep1000Millis();
			}
		}
		if(mirror==null){
			throw new Exception("no mirror avail.");
		}
		return mirror.connect(clazz,timeout);
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
		DBMirror mirror=null;
		synchronized(this){
			mirror=select();
		}
		if(mirror==null){
			throw new Exception("no mirror avail.");
		}
		return mirror.connectForTables(clazz,tableNames, timeout);
	}
	
	/**
	 * 
	 * @return
	 */
	public DBMirror select(){
		if(availables.size()==0){
			return null;
		}
		int maxPriority=0;
		int maxPriorityIndex=0;
		boolean allEqual=true;
		for(int i=0;i<availables.size();i++){
			DBMirror mirror=(DBMirror)availables.get(i);
			if(mirror.getPriority()>maxPriority){
				maxPriorityIndex=i;
				maxPriority=mirror.getPriority();
				if(i>0) allEqual=false;
			}
		}
		
		if(allEqual){//如果全部镜像的优先级都相同，随机选取
			return (DBMirror)availables.get(JUtilRandom.nextInt(availables.size()));
		}else{//选用优先级最高的
			return (DBMirror)availables.get(maxPriorityIndex);
		}
	}
	
	/**
	 * 
	 * @param mirror
	 */
	public void addMirror(DBMirror mirror){
		mirrors.add(mirror);
	}
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public DBMirror mirror(String uuid){
		for(int i=0;i<mirrors.size();i++){
			DBMirror mirror=mirrors.get(i);
			if(mirror.getUuid().equalsIgnoreCase(uuid)) return mirror;
		}
		return null;
	}
	
	/**
	 * 
	 * @param mirrorUuid
	 * @return
	 */
	public boolean isAvail(String mirrorUuid){
		return availableUuids.contains(mirrorUuid);
	}
	
	/**
	 * 
	 * @param tableName
	 * @return
	 */
	public String getMetaTable(String tableName){
		List<String> selectors=this.metaOfTables.listKeys();
		for(int i=0; i<selectors.size(); i++){
			if(tableName.matches(selectors.get(i))) return this.metaOfTables.get(selectors.get(i));
		}

		return tableName;
	}
	
	
	/**
	 * 
	 *
	 */
	public void shutdown(){
		shutdown=true;
		
		for(int i=0;i<mirrors.size();i++){
			DBMirror mirror=mirrors.get(i);
			mirror.shutdown();
		}
	}
	
	/**
	 * 
	 *
	 */
	public void monitor(){
		try{
			List<DBMirror> temp=new LinkedList();
			List<String> tempUuids=new LinkedList();
			for(int i=0;i<mirrors.size();i++){
				DBMirror mirror=mirrors.get(i);

				if(mirror.available()){
					temp.add(mirror);
					tempUuids.add(mirror.getUuid());
				}
			}
			
			synchronized(this){
				availables.clear();
				availables.addAll(temp);
				
				availableUuids.clear();
				availableUuids.addAll(tempUuids);
			}
			
			temp.clear();
			tempUuids.clear();
		}catch(Exception e){
			log.log(e, Logger.LEVEL_INFO);
		}
	}

	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(!shutdown){
			if(Startup.isDestroyed()){
				this.shutdown();
				return;
			}

			monitor();
			
			try{
				Thread.sleep(5000);
			}catch(Exception e){}
		}
	}
}
