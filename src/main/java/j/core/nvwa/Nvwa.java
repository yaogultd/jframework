package j.core.nvwa;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.JProperties;
import j.core.nvwa.resource.*;
import j.log.Logger;
import j.util.*;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;


@ClassDescription(author = "肖炯",
		date = "2021/07/19",
		description = "女娲 - 系统启动总入口 & IOC/AOP", reviewers = {})
public class Nvwa implements Runnable, Consumer {
	private static Logger log=Logger.create(Nvwa.class);//日志输出

	@FieldDescription(description = "表示null")
	public static final String IS_NULL="NVWA_IS_NULL";

	@FieldDescription(description = "是否调试模式")
	private static boolean debug=false;

	@FieldDescription(description = "资源组")
	private static ConcurrentList<String> jarEntries=new ConcurrentList<>();
	private static ConcurrentList<Resources> resources=new ConcurrentList<>();
	private static Resources resourcesInDeployedJar = new Resources();
	private static ConcurrentMap<String, File> tempFiles=new ConcurrentMap<>();

	@FieldDescription(description = "IOC配置（从nvwa*.xml或注解获得）")
	private static ConcurrentMap<String, NvwaObject> objects=new ConcurrentMap<>();

	@FieldDescription(description = "启动类")
	private static String starterName=null;

	@FieldDescription(description = "启动参数")
	private static String[] startupArgs=null;

	//是否已经启动
	private static boolean started=false;

	//资源是否已经扫描完毕
	private static boolean scanned=false;

	/**
	 *
	 * @return
	 */
	public static boolean isDebug(){
		return debug;
	}

	/**
	 *
	 * @return
	 */
	public static boolean isScanned(){
		return scanned;
	}

	@MethodDescription(author = "肖炯", date = "2021/07/19", description = "启动框架（只执行一次）")
	public static void startup(){
		startup(null);
	}

	@MethodDescription(author = "肖炯", date = "2021/07/19", description = "启动框架（只执行一次）")
	synchronized public static void startup(String[] args){
		if(started) return;
		started=true;

		startupArgs=args;

		//解析配置文件nvwa.xml
		if(Startup.deployAsJar()){//如果是jar包部署
			//扫描jar包本身
			try {
				String path = JProperties.getAppRoot();
				if (path.endsWith("!")) path = path.substring(0, path.length() - 1);

				//初始化仅扫描框架资源
				jarEntries.add("j/core");
				jarEntries.add("com/pt");
				jarEntries.add("config/");
				jarEntries.add("I18N/");
				jarEntries.add("service/");
				jarEntries.add("webserver/");
				jarEntries.add("env.properties");

				resourcesInDeployedJar.setPath(path);
				resourcesInDeployedJar.scan(new File(path));

				//清空jar entry配置，后续读取nvwa.xml中相关设置
				jarEntries.clear();
			}catch (Exception e){
				log.log(e, Logger.LEVEL_FATAL);
				log.log("Nvwa exited because of resource scanning failure.", Logger.LEVEL_FATAL);
				System.exit(-1);
			}
			//扫描jar包本身 end
		}

		load();

		//启动资源扫描线程
		Nvwa monitor=new Nvwa();
		Thread thread=new Thread(monitor);
		thread.start();
		System.out.println(JUtilTimestamp.timestamp()+" j.core.nvwa.Nvwa Nvwa monitor thread started.");
	}

	/**
	 * 当以jar方式部署时，获取jar包中的资源
	 * @param paths
	 * @param types
	 * @return
	 */
	public static ConcurrentMap<String, Resource> getResourcesInDeployedJar(List<String> paths, String[] types){
		return resourcesInDeployedJar.getResources(paths, types);
	}

	/**
	 * 当以jar方式部署时，获取jar包中的资源
	 * @param path
	 * @return
	 */
	public static Resource getResourceInDeployedJar(String path){
		return resourcesInDeployedJar.getResource(path);
	}

	/**
	 * 查找资源
	 * @param paths
	 * @param types
	 * @return
	 */
	public static ConcurrentMap<String, Resource> getResources(List<String> paths, String[] types){
		ConcurrentMap<String, Resource> found=new ConcurrentMap<>();

		ConcurrentMap<String, Resource> inDeployedJar=getResourcesInDeployedJar(paths, types);
		if(inDeployedJar!=null && !inDeployedJar.isEmpty()) found.putAll(inDeployedJar);

		for(int i=0; i<resources.size(); i++){
			Resources rs=resources.get(i);

			ConcurrentMap<String, Resource> matches=rs.getResources(paths, types);
			if(matches!=null && !matches.isEmpty()) found.putAll(matches);
		}

		return found;
	}

	/**
	 * 当以jar方式部署时，将文件暂存到临时目录
	 * @param path
	 * @return
	 */
	public static File getTempFileOfResourceInJar(String path){
		String relativePath=ResourceHelper.getRelativePath(path);
		if(tempFiles.containsKey(path)) return tempFiles.get(path);
		if(tempFiles.containsKey(relativePath)) return tempFiles.get(relativePath);

		Resource resource = getResourceInDeployedJar(path);
		if(resource==null && !relativePath.equals(path)) resource = getResourceInDeployedJar(relativePath);
		if(resource==null) return null;

		if(resource instanceof ResourceString){
			ResourceString _resource=(ResourceString)resource;
			File file=_resource.saveTempFile();
			tempFiles.put(path, file);
			tempFiles.put(relativePath, file);
			log.log("save resource "+path+" to temp file "+file.getAbsolutePath(), -1);
		}else if(resource instanceof ResourceBytes){
			ResourceBytes _resource=(ResourceBytes)resource;
			File file=_resource.saveTempFile();
			tempFiles.put(path, file);
			tempFiles.put(relativePath, file);
			log.log("save resource "+path+" to temp file "+file.getAbsolutePath(), -1);
		}

		return tempFiles.get(path);
	}

	/**
	 *
	 * @param code
	 * @return
	 */
	private static NvwaObject get(String code) {
		return (NvwaObject) objects.get(code);
	}

	/**
	 *
	 * @param nvwaObjectCode
	 * @param paraGroup
	 * @param paraName
	 * @return
	 */
	public static String getParameter(String nvwaObjectCode, String paraGroup, String paraName){
		NvwaObject nvwaObject=get(nvwaObjectCode);
		if(nvwaObject == null) return null;

		return nvwaObject.getParameter(paraGroup, paraName);
	}

	/**
	 *
	 * @param nvwaClass
	 * @param paraGroup
	 * @param paraName
	 * @return
	 */
	public static String getParameter(Class nvwaClass, String paraGroup, String paraName){
		return getParameter(JUtilBean.lowerFirstChar(nvwaClass.getSimpleName()), paraGroup, paraName);
	}

	/**
	 *
	 * @param nvwaObject
	 * @param paraGroup
	 * @param paraName
	 * @return
	 */
	public static String getParameter(Object nvwaObject, String paraGroup, String paraName){
		return getParameter(nvwaObject.getClass(), paraGroup, paraName);
	}

	/**
	 *
	 * @param nvwaObjectCode
	 * @param key
	 * @return
	 */
	@MethodDescription(author = "肖炯", date = "2021/07/19", description = "获取自定义参数")
	public static String getParameter(String nvwaObjectCode, String key){
		NvwaObject nvwaObject=get(nvwaObjectCode);
		if(nvwaObject == null){
			return null;
		}

		return nvwaObject.getParameter(key);
	}

	/**
	 *
	 * @param nvwaClass
	 * @param key
	 * @return
	 */
	public static String getParameter(Class nvwaClass, String key){
		return getParameter(JUtilBean.lowerFirstChar(nvwaClass.getSimpleName()), key);
	}

	/**
	 *
	 * @param nvwaObject
	 * @param key
	 * @return
	 */
	public static String getParameter(Object nvwaObject, String key){
		return getParameter(nvwaObject.getClass(), key);
	}

	/**
	 *
	 * @param code 对象编码
	 * @return
	 */
	@MethodDescription(author = "肖炯", date = "2021/07/19", description = "通过不带参数的构造函数创建对象")
	public static Object create(String code){
		NvwaObject obj=objects.get(code);
		if(obj==null) return null;

		try{
			Object object = obj.create();
			if(object != null && (object instanceof NvwaAncestor)){
				((NvwaAncestor)object).setNvwaObjectCode(obj.getCode());
			}
			return object;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param code 对象编码
	 * @param parameterTypes 构造函数参数类类型
	 * @param parameters 构造函数参数
	 * @return
	 */
	@MethodDescription(author = "肖炯", date = "2021/07/19", description = "通过带参数的构造函数创建对象")
	public static Object create(String code,Class[] parameterTypes,Object[] parameters){
		NvwaObject obj=(NvwaObject)objects.get(code);
		if(obj==null) return null;
		try{
			Object object = obj.create(parameterTypes,parameters);
			if(object != null && (object instanceof NvwaAncestor)){
				((NvwaAncestor)object).setNvwaObjectCode(obj.getCode());
			}
			return object;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "解析配置文件nvwa.xml")
	private static void load(){
		try{
			Document document=null;
			String config=ResourceHelper.getString(JUtilString.appendPath(ResourceHelper.configDir(), "nvwa.xml"));
			if(JUtilString.isBlank(config) && Startup.deployAsJar()) config=ResourceHelper.getString(JUtilString.appendPath(ResourceHelper.configDirExternal(), "nvwa.xml"));

			if(!JUtilString.isBlank(config)) document=JUtilDom4j.parseString(config,"UTF-8");
			if(document==null){
				scanned=true;
				log.log("找不到配置文件："+JUtilString.appendPath(ResourceHelper.configDir(), "nvwa.xml"), Logger.LEVEL_FATAL);
				return;
			}

			//文件是否存在
			Element root=document.getRootElement();

			//debug
			Nvwa.debug="true".equalsIgnoreCase(root.elementText("debug"));
			System.out.println("调试模式 -> "+Nvwa.debug);

			//nvwa.xml中指定全局配置
			//资源扫描配置
			Element scanner=root.element("scanner");
			if(scanner!=null){
				Element jarEntriesE=scanner.element("jar-entries");
				if(jarEntriesE != null){
					List<Element> _entries=jarEntriesE.elements("entry");
					for (int i=0; _entries!=null && i<_entries.size(); i++){
						jarEntries.add(_entries.get(i).getTextTrim());
						log.log("jar entry -> "+_entries.get(i).getTextTrim(), -1);
					}
				}

				List<Element> _resources=scanner.elements("resources");
				for (int i=0; _resources!=null && i<_resources.size(); i++){
					Resources rs=new Resources(_resources.get(i));
					resources.add(rs);
				}
			}

			starterName=root.elementText("starter");
			if(JUtilString.isBlank(starterName)) starterName="j.core.nvwa.NvwaStarterDefault";
			System.out.println("系统启动类 -> "+starterName);

			//兼容老版本IOC
			Element _objects=root.element("objects");
			if(_objects != null){
				List objs=_objects.elements("object");
				for(int i=0;i<objs.size();i++){
					Element objEle=(Element)objs.get(i);
					String code=objEle.elementText("code");

					NvwaObject obj=new NvwaObject();
					obj.setCode(code);
					obj.setName(objEle.elementText("name"));

					String cls=objEle.elementText("cls");
					if(cls==null) cls=objEle.elementText("implementation");
					obj.setCls(cls);

					obj.setProxy(objEle.elementText("proxy"));
					obj.setSingleton("true".equalsIgnoreCase(objEle.elementText("singleton")));

					List params=objEle.elements("parameter");
					for(int j=0;j<params.size();j++){
						Element paramEle=(Element)params.get(j);
						obj.setParameter(paramEle.attributeValue("key"),paramEle.attributeValue("value"));
					}

					List fields=objEle.elements("field");
					for(int j=0;j<fields.size();j++){
						Element fieldEle=(Element)fields.get(j);
						obj.setFiled(fieldEle.attributeValue("name"),
								fieldEle.attributeValue("type"),
								fieldEle.attributeValue("init-value"));
					}

					objects.put(code,obj);

					System.out.println(JUtilTimestamp.timestamp()+" "+obj);
				}
			}
			//兼容老版本IOC end

			root=null;
			document=null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param entry
	 * @return
	 */
	public static boolean ignoreJarEntry(String entry){
		if(jarEntries.isEmpty()) return false;

		for(int i=0; i<jarEntries.size(); i++){
			if(entry.startsWith(jarEntries.get(i))) return false;
		}

		return true;
	}

	@Override
	@MethodDescription(author = "肖炯", date = "2021/07/19", description = "定时扫描各资源组")
	public void run(){
		while(!Startup.isDestroyed()){
			if(!scanned) System.out.println("scanning "+resources.size()+" resources.");
			for(int i=0; i<resources.size(); i++){
				if(!scanned) System.out.println("scanning resources NO."+(i+1)+" "+resources.get(i).toString());
				try{
					resources.get(i).scan();
				}catch(Exception e){
					e.printStackTrace();
				}
				if(!scanned) System.out.println("scanning resources NO."+(i+1)+" finished.");
			}

			scanned=true;

			//启动系统
			//System.out.println("Nvwa starter is "+starterName);
			if(starterName!=null){
				try{
					NvwaStarter starter=(NvwaStarter)Class.forName(starterName).getDeclaredConstructor().newInstance();
					starter.startup(startupArgs);
				}catch (Exception e){
					e.printStackTrace();
				}
				starterName=null;
			}

			try{
				Thread.sleep(60000L);
			}catch(Exception e){}
		}
	}

	/**
	 *
	 * @param resource
	 * @return
	 */
	private static boolean load(Resource resource){
		if(resource==null) return false;//null
		ResourceClass _resource=(ResourceClass)resource;
		try{
			j.core.annotation.nvwa.Nvwa nvwaAnno=(j.core.annotation.nvwa.Nvwa)_resource.getResource().getAnnotation(j.core.annotation.nvwa.Nvwa.class);

			//不是Nvwa或未正取配置
			if(nvwaAnno==null) return false;

			//来自para*.xml(AppConfig)的配置
			j.core.annotation.configuration.Parameters parametersAnno=(j.core.annotation.configuration.Parameters)_resource.getResource().getAnnotation(j.core.annotation.configuration.Parameters.class);

			//来自.properties文件的配置
			j.core.annotation.configuration.Properties propertiesAnno=(j.core.annotation.configuration.Properties)_resource.getResource().getAnnotation(j.core.annotation.configuration.Properties.class);

			NvwaObject obj=new NvwaObject();
			obj.setCode(JUtilString.isBlank(nvwaAnno.code())?JUtilBean.lowerFirstChar(_resource.getResource().getSimpleName()):nvwaAnno.code());
			obj.setName(nvwaAnno.name());
			obj.setProxy(nvwaAnno.proxy());
			obj.setSingleton(nvwaAnno.singleton() == j.core.annotation.nvwa.Nvwa.SIGNLETON.TRUE);
			obj.setCls(_resource.getResource().getCanonicalName());

			if(parametersAnno!=null){
				obj.setParametersGroup(parametersAnno.group());

				Class parent=_resource.getResource().getSuperclass();
				while(parent != null){
					parent=_resource.getResource().getSuperclass();
				}
			}

			if(propertiesAnno!=null){
				obj.setPropertiesPath(propertiesAnno.path());
			}

			System.out.println("扫描到Nvwa注解 -> "+obj.getCode()+" -> "+obj.getName()+", class -> "+obj.getCls()+", proxy -> "+obj.getProxy()+", signleton -> "+obj.getSingleton()+", parametersGroup -> "+obj.getParametersGroup()+", propertiesPath -> "+obj.getPropertiesPath());

			Field[] fields=_resource.getResource().getDeclaredFields();
			for(int i=0; fields!=null && i<fields.length; i++){
				//注入bean或初始化基础类型的值
				j.core.annotation.nvwa.Field fieldAnno=fields[i].getAnnotation(j.core.annotation.nvwa.Field.class);

				//来自para*.xml(AppConfig)的配置
				j.core.annotation.configuration.Parameter parameterAnno=fields[i].getAnnotation(j.core.annotation.configuration.Parameter.class);

				//来自.properties文件的配置
				j.core.annotation.configuration.Property propertyAnno=fields[i].getAnnotation(j.core.annotation.configuration.Property.class);

				//字段类型
				String fieldType=fields[i].getType().getCanonicalName();

				if(fieldAnno!=null){
					if(fieldAnno.type() == j.core.annotation.nvwa.Field.TYPE.FieldType){
						if ("java.lang.String".equalsIgnoreCase(fieldType)) {
							fieldType=NvwaField.TYPE_STRING;
						} else if ("java.lang.Integer".equalsIgnoreCase(fieldType)
								||NvwaField.TYPE_INTEGER_PLAIN.equals(fieldType)) {
							fieldType=NvwaField.TYPE_INTEGER;
						} else if ("java.lang.Long".equalsIgnoreCase(fieldType)
								||NvwaField.TYPE_LONG_PLAIN.equals(fieldType)) {
							fieldType=NvwaField.TYPE_LONG;
						} else if ("java.lang.Double".equalsIgnoreCase(fieldType)
								||NvwaField.TYPE_DOUBLE_PLAIN.equals(fieldType)) {
							fieldType=NvwaField.TYPE_DOUBLE;
						} else if ("java.sql.Timestamp".equalsIgnoreCase(fieldType)) {
							fieldType=NvwaField.TYPE_TIMESTAMP;
						} else if ("java.lang.Boolean".equalsIgnoreCase(fieldType)
								||NvwaField.TYPE_BOOLEAN_PLAIN.equals(fieldType)) {
							fieldType=NvwaField.TYPE_BOOLEAN;
						}else{
							fieldType=NvwaField.TYPE_REF;
						}
					}else if(fieldAnno.type() == j.core.annotation.nvwa.Field.TYPE.Class){
						fieldType=NvwaField.TYPE_CLASS;
					}else{
						fieldType=NvwaField.TYPE_REF;
					}

					if(JUtilString.isBlank(fieldAnno.initValue()) && NvwaField.TYPE_REF.equalsIgnoreCase(fieldType)){
						obj.setFiled(JUtilString.isBlank(fieldAnno.name())?fields[i].getName():fieldAnno.name(),
								fieldType,
								fields[i].getName());
						System.out.println("扫描到Field注解 -> "+(JUtilString.isBlank(fieldAnno.name())?fields[i].getName():fieldAnno.name())+" -> "+fieldType+" -> "+fields[i].getName());
					}else{
						obj.setFiled(JUtilString.isBlank(fieldAnno.name())?fields[i].getName():fieldAnno.name(),
								fieldType,
								fieldAnno.initValue());
						System.out.println("扫描到Field注解 -> "+(JUtilString.isBlank(fieldAnno.name())?fields[i].getName():fieldAnno.name())+" -> "+fieldType+" -> "+fieldAnno.initValue());
					}
				}

				if(parameterAnno != null){
					String defaultValue=null;
					if(!Nvwa.IS_NULL.equalsIgnoreCase(parameterAnno.defaultValue())) defaultValue=parameterAnno.defaultValue();

					String paramaterName=fields[i].getName();
					if(!JUtilString.isBlank(parameterAnno.name())) paramaterName=parameterAnno.name();

					obj.configFieldViaParameter(fields[i].getName(), parameterAnno.group(), paramaterName, defaultValue);
				}

				if(propertyAnno != null){
					String defaultValue=null;
					if(!Nvwa.IS_NULL.equalsIgnoreCase(propertyAnno.defaultValue())) defaultValue=propertyAnno.defaultValue();

					String propertyName=fields[i].getName();
					if(!JUtilString.isBlank(propertyAnno.name())) propertyName=propertyAnno.name();

					obj.configFieldViaProperty(fields[i].getName(), propertyAnno.path(), propertyName, defaultValue);
				}
			}

			objects.put(obj.getCode(), obj);
			return true;
		}catch (Exception e){
			System.out.println("Nvwa load class "+_resource.getClazz()+" failed!");
			log.log(e, Logger.LEVEL_FATAL);
			return true;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是class资源不予加载
		if(!(resource instanceof ResourceClass)) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		return true;
	}
}
