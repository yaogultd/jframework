package j.core.sys;


import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.common.Global;
import j.core.common.JProperties;
import j.core.nvwa.Nvwa;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilMD5;
import j.util.JUtilString;
import lombok.Getter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@ClassDescription(author = "肖炯",
		date = "2021/01/07",
		description = "应用参数配置，参数可配置于xml文件、或保存到数据")
public abstract class AppConfig{
	private static Logger log=Logger.create(AppConfig.class);
	private static ConcurrentMap<String, AppConfig> instances=new ConcurrentMap<>();//针对某个实体的实例
	protected static AppConfig instanceGlobal=null;//全局实例

	protected String instanceId="";//关联实体的ID（全局实例该字段为空）
	protected ConcurrentMap<String, AppParaGroup> groups=new ConcurrentMap<>(true, new LinkedHashMap<>());//参数分组
	protected ConcurrentMap<String, AppPara> params=new ConcurrentMap<>(true, new LinkedHashMap<>());//全部参数

	@Getter
	protected boolean loaded=false;//是否已加载
	protected long latestUsed=0;//最近使用时间
	protected final Object lock=new Object();
	
	static{
		//启动监控线程
		(new Thread(new AppConfigMonitor())).start();
		log.log("AppConfigMonitor started.", -1);
	}

	/**
	 *
	 */
	protected AppConfig(){
	}

	/**
	 *
	 */
	protected AppConfig(String instanceId){
		this.instanceId=(instanceId==null?"":instanceId);
	}


	/**
	 *
	 * @param instanceId
	 * @return
	 */
	protected static AppConfig getInstance(String instanceId){
		synchronized (instances){
			if(JUtilString.isBlank(instanceId)){
				if(instanceGlobal != null) return instanceGlobal;

				log.log("AppConfigMethod = "+JProperties.getEnv("AppConfigMethod"), -1);
				if("XML".equalsIgnoreCase(JProperties.getEnv("AppConfigMethod"))){
					instanceGlobal=new AppConfigXML();
				}else{
					instanceGlobal=new AppConfigDB();
				}

				try{
					instanceGlobal.load();
				}catch (Exception e){
					log.log(e, Logger.LEVEL_ERROR);
				}
				instanceGlobal.loaded=true;

				return used(instanceGlobal);
			}

			AppConfig instance=instances.get(instanceId);
			if(instance==null){
				if("XML".equalsIgnoreCase(JProperties.getEnv("AppConfigMethod"))){
					instance = new AppConfigXML(instanceId);
				}else{
					instance = new AppConfigDB(instanceId);
				}
			}
			instances.put(instanceId, instance);

			if(!instance.loaded){
				try{
					instance.load();
				}catch (Exception e){
					log.log(e, Logger.LEVEL_ERROR);
				}
				instance.loaded=true;
			}
			return used(instance);
		}
	}

	/**
	 * 加载
	 * @throws Exception
	 */
	protected abstract void load() throws Exception;

	/**
	 * 保存
	 * @throws Exception
	 */
	public abstract void doSave() throws Exception;

	/**
	 * 保存到数据库(用于XML模式向DB模式迁移)
	 * @throws Exception
	 */
	public abstract void doSaveToDB() throws Exception;

	/**
	 * 保存到XML(用于DB模式向XML模式迁移)
	 * @throws Exception
	 */
	public abstract void doSaveToXML() throws Exception;

	//////////////////全局实例////////////////////////////////////////////
	/**
	 *
	 * @throws Exception
	 */
	public static void save() throws Exception{
		save(null);
	}

	/**
	 *
	 * @throws Exception
	 */
	public static void saveToDB() throws Exception{
		saveToDB(null);
	}

	/**
	 *
	 * @throws Exception
	 */
	public static void saveToXML() throws Exception{
		saveToXML(null);
	}

	/**
	 * 
	 * @param group
	 * @return
	 */
	public static AppParaGroup getGroup(String group){
		return getGroup(null, group);
	}
	
	/**
	 * 返回组名为group的全部参数
	 * @param group
	 * @return
	 */
	public static List<AppPara> getParas(String group){
		return getParas(null, group);
	}
	
	/**
	 * 得到某个参数的值
	 * 
	 * @param group
	 * @param paraName
	 * @return
	 */
	public static String getPara(String group, String paraName){
		return getPara(null, group, paraName);
	}

	/**
	 * 设置参数值
	 * @param group
	 * @param paraName
	 * @param value
	 * @param desc
	 */
	public static void setPara(String group,String paraName,String value,String desc){
		setPara(null, group, paraName, value, desc);
	}

	/**
	 *
	 * @param group
	 * @param paraName
	 */
	public static void removePara(String group, String paraName){
		removePara(null, group, paraName);
	}

	
	//////////////////某个实例////////////////////////////////////////////
	/**
	 *
	 * @param instanceId
	 * @throws Exception
	 */
	public static void save(String instanceId) throws Exception{
		AppConfig instance = getInstance(instanceId);
		if(instance==null) return;
		while(!instance.isLoaded()){//等待加载完毕
			Global.sleep1000Millis();
		}
		instance.doSave();
	}

	/**
	 *
	 * @param instanceId
	 * @throws Exception
	 */
	public static void saveToDB(String instanceId) throws Exception{
		AppConfig instance = getInstance(instanceId);
		if(instance==null) return;
		instance.doSaveToDB();
	}

	/**
	 *
	 * @param instanceId
	 * @throws Exception
	 */
	public static void saveToXML(String instanceId) throws Exception{
		AppConfig instance = getInstance(instanceId);
		if(instance==null) return;
		instance.doSaveToXML();
	}

	/**
	 *
	 * @param instanceId
	 * @param group
	 * @return
	 */
	public static AppParaGroup getGroup(String instanceId, String group){
		try{
			return getInstance(instanceId).groups.get(group);
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 * 返回组名为group的全部参数
	 * @param instanceId
	 * @param group
	 * @return
	 */
	public static List<AppPara> getParas(String instanceId, String group){
		AppConfig instance = getInstance(instanceId);
		if(instance==null||instance.params==null) return null;

		List<AppPara> ofGroud=new LinkedList();
		try{
			List<AppPara> values=instance.params.listValues(null);
			for(int i=0;i<values.size();i++){
				if(JUtilString.equals(group, values.get(i).getGroup())) ofGroud.add(values.get(i));
			}
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
		return ofGroud;
	}

	/**
	 * 得到某个参数的值
	 * @param instanceId
	 * @param group
	 * @param paraName
	 * @return
	 */
	public static String getPara(String instanceId, String group, String paraName){
		return getPara(instanceId, group, paraName, true);
	}

	/**
	 * 得到某个参数的值
	 * @param instanceId
	 * @param group
	 * @param paraName
	 * @param inherit 不存在时是否继承默认实例的配置
	 * @return
	 */
	public static String getPara(String instanceId, String group, String paraName, boolean inherit){
		if(JUtilString.isBlank(group) || JUtilString.isBlank(paraName)){
			return null;
		}

		AppConfig instance= getInstance(instanceId);
		String key=group+"*"+paraName;
		try{
			AppPara para=instance.params.get(key);
			if(JUtilString.isBlank(instanceId) || !inherit) return para==null?null:para.getValue();

			//如果指定实例不存在该配置项，从默认实例获取
			return para==null?getPara(null, group, paraName):para.getValue();
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 * 设置参数值
	 * @param instanceId
	 * @param group
	 * @param paraName
	 * @param value
	 * @param desc
	 */
	public static void setPara(String instanceId, String group,String paraName,String value,String desc){
		if(JUtilString.isBlank(group)
				||JUtilString.isBlank(paraName)
				||value==null){
			return;
		}

		try {
			AppConfig instance = getInstance(instanceId);
			if (!instance.groups.containsKey(group)) {
				instance.groups.put(group, new AppParaGroup(group, ""));
			}

			String key = group + "*" + paraName;
			if (instance.params.containsKey(key)) {
				AppPara para = instance.params.get(key);
				para.setValue(value);
				para.setUpdated(true);
				para.setIsNew(false);
				instance.params.put(key, para);
			} else {
				List<AppPara> ofGroup = getParas(group);
				AppPara para = new AppPara(key,
						group,
						paraName,
						value,
						desc,
						true,
						ofGroup.size());
				para.setUpdated(true);
				para.setIsNew(true);
				instance.params.put(key, para);
			}
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 移除参数值
	 * @param instanceId
	 * @param group
	 * @param paraName
	 */
	public static void removePara(String instanceId, String group, String paraName){
		if(JUtilString.isBlank(group) || JUtilString.isBlank(paraName)) return;

		AppConfig instance = getInstance(instanceId);
		String key=group+"*"+paraName;
		try {
			instance.params.remove(key);
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 参数配置文件存储路径
	 * @param instanceId
	 * @return
	 */
	protected static String getFileName(String instanceId){
		if(JUtilString.isBlank(instanceId)) return "para.xml";
		else{
			String md5 = JUtilMD5.MD5EncodeToHex(instanceId);
			return md5.substring(0, 2)
					+ "/" + md5.substring(2, 4)
					+ "/" + md5.substring(4, 6)
					+ "/" + md5.substring(6, 8)
					+ "/" + "para.xml";
		}
	}

	/**
	 * 更新最近使用时间
	 * @param instance
	 * @return
	 */
	private static AppConfig used(AppConfig instance){
		instance.latestUsed=SysUtil.getNow();
		return instance;
	}

	/**
	 *
	 * @return
	 */
	private boolean idle(){
		return SysUtil.getNow()-this.latestUsed>300000L;
	}

	/**
	 *
	 */
	private void clear(){
		try{
			this.groups.clear();
			this.groups=null;
			this.params.clear();
			this.params=null;
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 清除过期未使用实例
	 */
	protected static void clearIdles(){
		List<String> instanceIds=instances.listKeys();
		for(int i=0;i<instanceIds.size();i++){
			String instanceId=instanceIds.get(i);

			AppConfig instance=instances.get(instanceId);
			if(instance==null){
				instances.remove(instanceId);
			}else if(instance.idle()){
				instances.remove(instanceId);
				instance.clear();
			}
		}
	}

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		Nvwa.startup();
		try{
			Thread.sleep(15000);
		}catch (Exception e){}
		AppConfig.saveToXML();
		//System.out.println("/"+"");
	}
}

/**
 *
 */
class AppConfigMonitor implements Runnable{
	@Override
	public void run() {
		//创建默认实例
		AppConfig.instanceGlobal=AppConfig.getInstance(null);

		while(!Startup.isDestroyed()){
			try{
				Thread.sleep(1000);
			}catch (Exception e){}
			AppConfig.clearIdles();
		}
	}
}