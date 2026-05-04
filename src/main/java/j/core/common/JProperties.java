package j.core.common;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.dao.DB;
import j.core.dao.Database;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceHelper;
import j.core.nvwa.resource.ResourceProperties;
import j.util.*;

import java.io.File;
import java.net.URL;
import java.util.*;

@ClassDescription(author = "肖炯",
		date = "2021/07/13",
		description = ".properties中配置的key-value", reviewers = {})
public class JProperties implements Consumer {
	@FieldDescription(description = "操作系统类型")
	private static String OS_TYPE="linux";

	@FieldDescription(description = "程序运行目录")
	private static String USER_DIR="";

	@FieldDescription(description = "框架运行根路径（动态获取）")
	private static String JFRAMEWORK_HOME="";

	@FieldDescription(description = "框架运行环境参数")
	private static ConcurrentMap<String, JUtilKeyValue> env = new ConcurrentMap();

	@FieldDescription(description = "分组保存不同properties文件中的配置（不包括env.properties），不同文件中key可重复")
	private static ConcurrentMap<String, JProperties> instances=new ConcurrentMap<>();

	@FieldDescription(description = "属性文件")
	private String fileName;

	@FieldDescription(description = "参数")
	private ConcurrentMap<String, JUtilKeyValue> properties= new ConcurrentMap();

	@FieldDescription(description = "分组保存参数")
	private ConcurrentMap groups= new ConcurrentMap();

	static{
		load();
	}

	/////////////运行环境参数/////////////////////////////////////
	@MethodDescription(author = "", date = "2021/07/20", description = "加载框架运行环境参数")
	private static void load(){
		env.clear();

		try{
			String os=System.getProperty("os.name");
			if(os!=null&&os.toLowerCase().indexOf("windows")>-1) OS_TYPE="windows";
			else OS_TYPE="linux";
			System.out.println("os.name -> "+os);

			//从环境变量获取
			JFRAMEWORK_HOME=System.getenv("JFRAMEWORK_HOME");
			System.out.println(" JFRAMEWORK_HOME from environmental variable -> "+JFRAMEWORK_HOME);

			//从env.properties中加载框架运行环境参数（可指定config、11N8等目录）
			ResourceBundle keyValuePairs = null;
			try{
				keyValuePairs=ResourceBundle.getBundle("env");
			}catch (Exception e){
				System.out.println("the root environment variables file is not exists!!!");
			}

			//尝试从env.properties中读取UserDir
			if(keyValuePairs!=null){
				for(Iterator it = keyValuePairs.keySet().iterator(); it.hasNext(); ) {
					String key = (String) it.next();
					String value = (String) keyValuePairs.getObject(key);
					value = new String(value.getBytes("iso-8859-1"), "UTF-8");
					value = value.replaceAll("USER_DIR", USER_DIR);

					//WebRoot为兼容旧版配置，现已改用AppRoot标识
					if ("UserDir".equals(key)) {
						USER_DIR = value;
						System.out.println(" USER_DIR from env.properties -> "+USER_DIR);
						break;
					}
				}
			}

			//如果未指定，则使用当前程序运行目录
			if(JUtilString.isBlank(USER_DIR)){
				String userDir=System.getProperty("user.dir");
				userDir=JUtilString.replaceAll(userDir, "\\", "/");
				USER_DIR=userDir;
				System.out.println("USER_DIR -> "+USER_DIR);
			}

			//如果未设置环境变量JFRAMEWORK_HOME
			if(JUtilString.isBlank(JFRAMEWORK_HOME) && keyValuePairs!=null) {
				for (Iterator it = keyValuePairs.keySet().iterator(); it.hasNext(); ) {
					String key = (String) it.next();
					String value = (String) keyValuePairs.getObject(key);
					value = new String(value.getBytes("iso-8859-1"), "UTF-8");
					value = value.replaceAll("USER_DIR", USER_DIR);

					//WebRoot为兼容旧版配置，现已改用AppRoot标识
					if ("AppRoot".equals(key)) {
						JFRAMEWORK_HOME = value;
						System.out.println(" JFRAMEWORK_HOME from env.properties -> "+JFRAMEWORK_HOME);
						break;
					}
				}
			}

			//如未设置环境变量，通过Class.getResource方法获取
			if(JUtilString.isBlank(JFRAMEWORK_HOME)) {
				URL temp= JProperties.class.getResource("");
				if(temp==null) temp=JProperties.class.getResource("/");
				if(temp!=null) {
					JFRAMEWORK_HOME = temp.toString();
					if (JFRAMEWORK_HOME.indexOf("j/core/common/") > 0) JFRAMEWORK_HOME = JFRAMEWORK_HOME.substring(0, JFRAMEWORK_HOME.indexOf("j/core/common/"));
					if (JFRAMEWORK_HOME.indexOf("WEB-INF/") > 0) JFRAMEWORK_HOME = JFRAMEWORK_HOME.substring(0, JFRAMEWORK_HOME.indexOf("/WEB-INF/"));
					System.out.println(" JFRAMEWORK_HOME by Class.getResource -> "+JFRAMEWORK_HOME+"(original: "+temp.toString()+")");
				}

				//如未通过Class.getResource获取到目录，则使用运行根目录
				if(JUtilString.isBlank(JFRAMEWORK_HOME)){
					JFRAMEWORK_HOME=USER_DIR;
					System.out.println(" JFRAMEWORK_HOME by system property <user.dir> -> "+JFRAMEWORK_HOME+"(original: "+temp.toString()+")");
				}

				if(JFRAMEWORK_HOME.startsWith("file:/")) JFRAMEWORK_HOME=JFRAMEWORK_HOME.substring(6);
				if(JFRAMEWORK_HOME.startsWith("/")) JFRAMEWORK_HOME=JFRAMEWORK_HOME.substring(1);

				//linux下改为绝对路径
				if("linux".equals(OS_TYPE)&&!JFRAMEWORK_HOME.startsWith("/")) JFRAMEWORK_HOME="/"+JFRAMEWORK_HOME;
			}

			if(!JUtilString.isBlank(JFRAMEWORK_HOME)){
				if(JFRAMEWORK_HOME.startsWith("/jar:file:/")) JFRAMEWORK_HOME=JFRAMEWORK_HOME.substring(10);
				else if(JFRAMEWORK_HOME.startsWith("jar:file:/")) JFRAMEWORK_HOME=JFRAMEWORK_HOME.substring(10);

				if(JFRAMEWORK_HOME.lastIndexOf("jar!")>0) JFRAMEWORK_HOME=JFRAMEWORK_HOME.substring(0, JFRAMEWORK_HOME.lastIndexOf("jar!")+4);

				JFRAMEWORK_HOME=JUtilString.replaceAll(JFRAMEWORK_HOME,"%20"," ");
				while(JFRAMEWORK_HOME.endsWith("/")) JFRAMEWORK_HOME=JFRAMEWORK_HOME.substring(0, JFRAMEWORK_HOME.length()-1);
			}else{
				throw new Exception("JFRAMEWORK_HOME not set");
			}

			System.out.println("JFRAMEWORK_HOME -> "+JFRAMEWORK_HOME+", in jar -> "+JFRAMEWORK_HOME.toLowerCase().endsWith("jar!"));

			if(keyValuePairs!=null){
				for(Iterator it = keyValuePairs.keySet().iterator(); it.hasNext();){
					String key=(String)it.next();
					String value=(String)keyValuePairs.getObject(key);
					value=new String(value.getBytes("iso-8859-1"),"UTF-8");
					value = value.replaceAll("USER_DIR", USER_DIR);

					if ("AppRoot".equals(key)) continue;

					if(JFRAMEWORK_HOME!=null
							&&!"".equals(JFRAMEWORK_HOME)
							&&value.indexOf("JFRAMEWORK_HOME")>-1){
						value=JUtilString.replaceAll(value,"JFRAMEWORK_HOME", JFRAMEWORK_HOME);
					}
					value=JUtilString.replaceAll(value,"USER_DIR", USER_DIR);

					env.put(key,new JUtilKeyValue(key,value,0));
					System.out.println("environment variable -> "+key+" = "+value);
				}
			}

			System.out.println("environment variables: \r\n");
			System.out.println("UserDir(known as USER_DIR) -> "+getUserDir());
			System.out.println("AppRoot(known as JFRAMEWORK_HOME) -> "+getAppRoot());
			System.out.println("WebRoot(known as JFRAMEWORK_HOME or JFRAMEWORK_WEBROOT) -> "+getWebRoot());
			System.out.println("ConfigPath(known as JFRAMEWORK_CONFIG_PATH) -> "+getConfigPath());
			System.out.println("ClassPath(known as JFRAMEWORK_CLASSES_PATH) -> "+getClassPath());
			System.out.println("I18NPath(known as JFRAMEWORK_I18N_PATH) -> "+getI18NPath());
			System.out.println("JDFSPath(known as UI_PATH) -> "+getJDFSPath());
			System.out.println("DataPath(known as JFRAMEWORK_DATA_PATH) -> "+getDataPath());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 获取框架运行环境参数
	 * @param propertyName
	 * @return
	 */
	public static String getEnv(String propertyName){
		JUtilKeyValue p=env.get(propertyName);
		return p==null?null:p.getValue().toString();
	}

	/**
	 *
	 * @param propertyName
	 * @param p
	 */
	public static void setEnv(String propertyName, JUtilKeyValue p){
		env.put(propertyName, p);
	}

	/**
	 *
	 * @return
	 */
	public static String getOsType(){
		return OS_TYPE;
	}

	/**
	 *
	 * @return
	 */
	public static String getUserDir(){
		return USER_DIR;
	}

	/**
	 *
	 * @return
	 */
	public static String getAppRoot(){
		return JFRAMEWORK_HOME;
	}

	/**
	 * 配置文件根目录
	 * @return
	 */
	public static String getConfigPath(){
		String thePath=getEnv("ConfigPath");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)){
			if(Startup.deployAsJar()) thePath="config";//jar包运行
			else thePath=JUtilString.appendPath(getAppRoot(), "WEB-INF/config");//webapp形式
		}

		//去掉结尾的/
		if(thePath.endsWith("/")) thePath=thePath.substring(0, thePath.length()-1);

		//保存
		setEnv("ConfigPath", new JUtilKeyValue("ConfigPath", thePath));

		return thePath;
	}

	/**
	 * 多语言资源目录
	 * @return
	 */
	public static String getI18NPath(){
		String thePath=getEnv("I18NPath");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)){
			if(Startup.deployAsJar()) thePath="I18N";//jar包运行
			else thePath=JUtilString.appendPath(getAppRoot(), "WEB-INF/I18N");//webapp形式
		}

		//去掉结尾的/
		if (thePath.endsWith("/")) thePath = thePath.substring(0, thePath.length() - 1);

		//保存
		setEnv("I18NPath", new JUtilKeyValue("I18NPath", thePath));

		return thePath;
	}

	/**
	 * WEB应用根目录
	 * @return
	 */
	public static String getWebRoot(){
		String thePath=getEnv("WebRoot");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)){
			if(Startup.deployAsJar()) thePath="WebRoot";//jar包运行
			else thePath=JUtilString.appendPath(getAppRoot(), "WebRoot");//webapp形式
		}

		//去掉结尾的/
		if (thePath.endsWith("/")) thePath = thePath.substring(0, thePath.length() - 1);

		//保存
		setEnv("WebRoot", new JUtilKeyValue("WebRoot", thePath));

		return thePath;
	}

	/**
	 * JDFS文件存储根目录
	 * @return
	 */
	public static String getJDFSPath(){
		String thePath=getEnv("JDFSPath");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)){
			if(Startup.deployAsJar()) thePath="JDFSPath";//jar包运行
			else thePath=JUtilString.appendPath(getAppRoot(), "JDFSPath");//webapp形式
		}

		//去掉结尾的/
		if (thePath.endsWith("/")) thePath = thePath.substring(0, thePath.length() - 1);

		//保存
		setEnv("JDFSPath", new JUtilKeyValue("JDFSPath", thePath));

		return thePath;
	}


	/**
	 * 类文件存放根目录
	 * @return
	 */
	public static String getClassPath(){
		if(Startup.deployAsJar()) return "";//jar包运行（无需另外扫描类文件）

		String thePath=getEnv("ClassPath");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)) thePath=JUtilString.appendPath(getAppRoot(), "WEB-INF/classes");//webapp形式

		//去掉结尾的/
		if(thePath.endsWith("/")) thePath=thePath.substring(0, thePath.length()-1);

		//保存
		setEnv("ClassPath", new JUtilKeyValue("ClassPath", thePath));

		return thePath;
	}


	/**
	 * 配置文件根目录
	 * @return
	 */
	public static String getDataPath(){
		String thePath=getEnv("DataPath");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)){
			if(Startup.deployAsJar()) thePath="data";//jar包运行
			else thePath=JUtilString.appendPath(getAppRoot(), "WEB-INF/data");//webapp形式
		}

		//去掉结尾的/
		if(thePath.endsWith("/")) thePath=thePath.substring(0, thePath.length()-1);

		//保存
		setEnv("DataPath", new JUtilKeyValue("DataPath", thePath));

		return thePath;
	}

	/**
	 *
	 * @return
	 */
	public static String getJspPath(){
		String thePath=getEnv("JspPath");

		//如未设定，使用默认值
		if(JUtilString.isBlank(thePath)) thePath=getAppRoot();

		//去掉结尾的/
		if(thePath.endsWith("/")) thePath=thePath.substring(0, thePath.length()-1);

		//保存
		setEnv("JspPath", new JUtilKeyValue("JspPath", thePath));

		return thePath;
	}

	/////////////运行环境参数  END/////////////////////////////////////

	/**
	 *
	 */
	public JProperties(){
	}

	/**
	 *
	 * @param fileName
	 */
	private JProperties(String fileName){
		this.fileName=fileName;
	}

	/**
	 *
	 * @param fileName
	 * @return
	 */
	public static JProperties getInstance(String fileName){
		if(fileName==null || "".equals(fileName)){
			fileName="jframework.properties";
		}

		if(!JUtilString.isBlank(JProperties.getConfigPath())
				&&fileName.startsWith(JProperties.getConfigPath())){
			fileName=fileName.substring(JProperties.getConfigPath().length()+1);
		}

		if(!Startup.deployAsJar()
				&& !JUtilString.isBlank(JProperties.getClassPath())
				&& fileName.startsWith(JProperties.getClassPath())){
			fileName=fileName.substring(JProperties.getClassPath().length()+1);
		}

		fileName=JUtilString.replaceAll(fileName, "/", ".");

		if(instances.containsKey(fileName)) return instances.get(fileName);

		JProperties instance=new JProperties(fileName);
		instances.put(fileName, instance);

		return instance;
	}

	/////////////业务参数///////////////////////////////////////////////////////////
	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "")
	private void _load(Properties ps){
		System.out.println(this.fileName + " load property......");

		for(Iterator it=ps.keySet().iterator();it.hasNext();){
			String key=(String)it.next();
			String value=(String)ps.getProperty(key);
			if(JFRAMEWORK_HOME!=null
					&&!"".equals(JFRAMEWORK_HOME)){
				value= JUtilString.replaceAll(value,"JFRAMEWORK_HOME", JFRAMEWORK_HOME);
			}

			value = ResourceHelper.replaceEnvVariables(value);

			//value=JUtilString.replaceAll(value, "//", "/");

			int no=0;
			if(key.startsWith("<")){
				int noEnd=key.indexOf(">");
				if(noEnd>1){
					String noString=key.substring(1,noEnd);
					if(JUtilMath.isInt(noString)) no=Integer.parseInt(noString);
					key=key.substring(noEnd+1);
				}
			}

			properties.put(key,new JUtilKeyValue(key,value,no));

			System.out.println(this.fileName + " load property -> "+key+" = "+value);
		}
	}

	/**
	 * 获取其它参数
	 * @param propertyName
	 * @return
	 */
	public String _getProperty(String propertyName){
		JUtilKeyValue p=properties.get(propertyName);
		return p==null?null:p.getValue().toString();
	}

	/**
	 * 获取其它参数
	 * @param groupName
	 * @param propertyName
	 * @return
	 */
	public String _getProperty(String groupName,String propertyName){
		JUtilKeyValue p=properties.get("["+groupName+"]"+propertyName);
		return p==null?null:p.getValue().toString();
	}

	/**
	 *
	 * @param group
	 * @return
	 */
	public java.util.Properties _getProperties(String group){
		if(!groups.containsKey(group)){
			java.util.Properties props=new java.util.Properties();
			Iterator it=properties.keySet().iterator();
			while(it.hasNext()){
				String key=(String)it.next();
				if(key.startsWith("["+group+"]")){
					props.put(key.substring(group.length()+2),getProperty(key));
				}
			}
			groups.put(group, props);
		}

		return (java.util.Properties)groups.get(group);
	}

	/**
	 *
	 * @param group
	 * @return
	 */
	public JUtilKeyValue[] _getPropertiesAsArray(String group){
		if(!groups.containsKey(group)){
			List props=new LinkedList();
			Iterator it=properties.keySet().iterator();
			while(it.hasNext()){
				String key=(String)it.next();
				if(key.startsWith("["+group+"]")){
					JUtilKeyValue kv=properties.get(key);
					key=key.substring(group.length()+2);
					props.add(new JUtilKeyValue(key,kv.getValue(),kv.getNo()));
				}
			}

			PropertySorter sorter=new PropertySorter();
			props=sorter.bubble(props, JUtilSorter.ASC);

			groups.put(group, props.toArray(new JUtilKeyValue[props.size()]));
		}

		return (JUtilKeyValue[])groups.get(group);
	}

	/**
	 *
	 * @param prefix
	 * @return
	 */
	public java.util.Properties _getPropertiesStartsWith(String prefix){
		java.util.Properties props=new java.util.Properties();
		Iterator it=properties.keySet().iterator();
		while(it.hasNext()){
			String key=(String)it.next();
			if(key.startsWith(prefix)){
				props.put(key,getProperty(key));
			}
		}
		return props;
	}

	/**
	 *
	 * @param prefix
	 * @return
	 */
	public JUtilKeyValue[] _getPropertiesStartsWithAsArray(String prefix){
		List props=new LinkedList();
		Iterator it=properties.keySet().iterator();
		while(it.hasNext()){
			String key=(String)it.next();
			if(key.startsWith(prefix)){
				props.add(properties.get(key));
			}
		}

		PropertySorter sorter=new PropertySorter();
		props=sorter.bubble(props,JUtilSorter.ASC);

		JUtilKeyValue[] temp=new JUtilKeyValue[props.size()];
		props.toArray(temp);
		return temp;
	}


	//////////////////获取默认配置文件（jframework.properties）中的配置////////////////
	/**
	 * 获取其它参数
	 * @param propertyName
	 * @return
	 */
	public static String getProperty(String propertyName){
		return getInstance(null)._getProperty(propertyName);
	}

	/**
	 * 获取其它参数
	 * @param groupName
	 * @param propertyName
	 * @return
	 */
	public static String getProperty(String groupName,String propertyName){
		return getInstance(null)._getProperty(groupName, propertyName);
	}

	/**
	 *
	 * @param group
	 * @return
	 */
	public static java.util.Properties getProperties(String group){
		return getInstance(null)._getProperties(group);
	}

	/**
	 *
	 * @param group
	 * @return
	 */
	public static JUtilKeyValue[] getPropertiesAsArray(String group){
		return getInstance(null)._getPropertiesAsArray(group);
	}

	/**
	 *
	 * @param prefix
	 * @return
	 */
	public static java.util.Properties getPropertiesStartsWith(String prefix){
		return getInstance(null)._getPropertiesStartsWith(prefix);
	}

	/**
	 *
	 * @param prefix
	 * @return
	 */
	public static JUtilKeyValue[] getPropertiesStartsWithAsArray(String prefix){
		return getInstance(null)._getPropertiesStartsWithAsArray(prefix);
	}
	//////////////////获取默认配置文件（jframework.properties）中的配置 END////////////////
	/////////////业务参数 END///////////////////////////////////////////////////////////


	/////////////系统保留配置参数//////////////////
	/**
	 * 日志级别
	 * @return
	 */
	public static String getLogLevel(){
		return getInstance(null).getProperty("LogLevel");
	}

	/**
	 * 日志存储数据库
	 * @return
	 */
	public static String getLogDatabase(){
		Database logDB=DB.getJFrameworkDB4Log();
		return logDB==null?Global.S_JFRAMEWORK.toLowerCase():logDB.getName();
	}

	/**
	 * 日志处理线程个数
	 * @return
	 */
	public static int getLoggers(){
		String v=getInstance(null).getProperty("Loggers");
		if(JUtilMath.isInt(v)) return Integer.parseInt(v);
		return 1;
	}

	/**
	 * 日志处理线程个数
	 * @return
	 */
	public static int getLoggersMax(){
		String v=getInstance(null).getProperty("LoggersMax");
		if(!JUtilMath.isInt(v)) return getLoggers();
		return Integer.parseInt(v);
	}

	/**
	 * JHttp实例个数
	 * @return
	 */
	public static int getJHttpInstances(){
		if(getInstance(null).getProperty("JHttpInstances")==null) return 5;
		return Integer.parseInt(getInstance(null).getProperty("JHttpInstances"));
	}

	/**
	 * 每个JHttp实例默认HttpClient数
	 * @return
	 */
	public static int getClientsOfJHttpInstance(){
		if(getInstance(null).getProperty("ClientsOfJHttpInstance")==null) return 1;
		return Integer.parseInt(getInstance(null).getProperty("ClientsOfJHttpInstance"));
	}
	////////////系统保留配置参数 END///////////////////

	@Override
	public boolean onFound(Resource resource) {
		try{
			if(resource==null) return false;

			//不是properties资源不予加载
			if(!(resource instanceof ResourceProperties)) return false;

			java.util.Properties ps=((ResourceProperties)resource).getResource();

			getInstance(resource.getPath())._load(ps);

			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean onUpdate(Resource resource) {
		return true;
	}
}
