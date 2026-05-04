package j.core.nvwa;


import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.JProperties;
import j.core.sys.AppConfig;
import j.util.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.List;

@ClassDescription(author = "肖炯",
		date = "2021/07/19",
		description = "nvwa.xml中配置的一个对象（或通过注解）",
		reviewers = {})
public class NvwaObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@FieldDescription(description = "对象编码（全局唯一）")
	private String code;

	@FieldDescription(description = "对象名称")
	private String name;

	@FieldDescription(description = "类名称")
	private String cls;

	@FieldDescription(description = "动态代理（AOP编程）")
	private String proxy;

	@FieldDescription(description = "是否单例")
	private boolean singleton;

	@FieldDescription(description = "使用para*.xml配置中的那个配置分组")
	private String parametersGroup;

	@FieldDescription(description = "使用哪个properties文件的配置")
	private String propertiesPath;

	@FieldDescription(description = "字段")
	private ConcurrentMap<String, NvwaField> fields;

	@FieldDescription(description = "来自para*.xml的配置")
	private ConcurrentMap<String, NvwaParameter> fieldConfigViaPara;

	@FieldDescription(description = "来自.properties的配置")
	private ConcurrentMap<String, NvwaProperty> fieldConfigViaProp;

	@FieldDescription(description = "兼容老版本中的参数配置")
	@Deprecated
	private ConcurrentMap parameters;

	@FieldDescription(description = "单例")
	private Object instance;

	/**
	 *
	 */
	public NvwaObject(){
		parameters=new ConcurrentMap();
		fieldConfigViaPara=new ConcurrentMap<>();
		fieldConfigViaProp=new ConcurrentMap<>();
		fields=new ConcurrentMap<>();
	}

	//setters and getters
	public void setCode(String code){
		this.code=code;
	}
	public String getCode(){
		return this.code;
	}

	public void setName(String name){
		this.name=name;
	}
	public String getName(){
		return this.name;
	}

	public void setCls(String cls){
		this.cls=cls;
	}
	public String getCls(){
		return this.cls;
	}

	public void setProxy(String proxy){
		this.proxy=proxy;
	}
	public String getProxy(){
		return this.proxy;
	}

	public void setSingleton(boolean singleton){
		this.singleton=singleton;
	}
	public boolean getSingleton(){
		return this.singleton;
	}

	public void setParametersGroup(String parametersGroup){
		this.parametersGroup=parametersGroup;
	}
	public String getParametersGroup(){
		return this.parametersGroup;
	}
	public void setParameter(String key,String value){
		parameters.put(key,value);
	}

	public void setPropertiesPath(String propertiesPath){
		this.propertiesPath=propertiesPath;
	}
	public String getPropertiesPath(){
		return this.propertiesPath;
	}


	public String getParameter(String key){
		String value=null;
		if(this.propertiesPath != null){
			value = JProperties.getInstance(this.propertiesPath)._getProperty(key);
		}

		if(value==null && !JUtilString.isBlank((this.parametersGroup))){
			value = AppConfig.getPara(this.parametersGroup, key);
		}

		if(value==null) value=(String)parameters.get(key);

		return value;
	}

	public String getParameter(String group, String name){
		String value=null;
		if(this.propertiesPath != null){
			value = JProperties.getInstance(this.propertiesPath)._getProperty(group, name);
		}

		if(value==null && !JUtilString.isBlank((this.parametersGroup))){
			value = AppConfig.getPara(group, name);
		}

		return value;
	}


	public void setFiled(String name,String type,String initValue){
		fields.put(name,new NvwaField(name,type,initValue));
	}
	public NvwaField getFiled(String fieldName){
		return fields.get(fieldName);
	}

	public void configFieldViaParameter(String field, String group, String name, String defaultVaule){
		if(JUtilString.isBlank(group)) group=this.parametersGroup;
		fieldConfigViaPara.put(field, new NvwaParameter(field, group, name, defaultVaule));
	}

	public void configFieldViaProperty(String field, String path, String name, String defaultVaule){
		if(JUtilString.isBlank(path)) path=this.propertiesPath;
		fieldConfigViaProp.put(field, new NvwaProperty(field, path, name, defaultVaule));
	}
	
	public Object getInstance(){
		return this.instance;
	}
	//setters and getters end
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/20",
			description = "通过无参数构造函数创建对象（如果配置了动态代理，则返回代理对象）")
	public Object create() throws Exception {
		try{
			if(this.singleton){//如果是单例模式
				synchronized(this){
					//需要创建的对象
					Object _new=null;

					//如果单例尚未创建
					if(instance==null){
						//创建对象
						Class clazz=Class.forName(this.cls);
						_new=clazz.getDeclaredConstructor().newInstance();

						//初始化处理
						init(clazz, _new);
					}

					//如果是新创建了对象
					if(_new!=null){
						//保存单例
						instance=_new;

						//如果设置了AOP模式
						if(this.getProxy()!=null && !"".equals(this.getProxy())){
							NvwaProxy proxyInstance=(NvwaProxy)Class.forName(this.getProxy()).getDeclaredConstructor().newInstance();
							_new=proxyInstance.bind(_new);
							instance=_new;
						}
					}
					
					return instance;
				}
			}else{//如果不是单例模式
				//创建对象
				Class clazz=Class.forName(this.cls);
				Object _new=clazz.getDeclaredConstructor().newInstance();

				//初始化处理
				init(clazz,_new);

				//如果设置了AOP模式
				if(this.getProxy()!=null && !"".equals(this.getProxy())){
					NvwaProxy proxyInstance=(NvwaProxy)Class.forName(this.getProxy()).getDeclaredConstructor().newInstance();
					_new=proxyInstance.bind(_new);
				}
				
				return _new;
			}
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/20",
			description = "通过有参数构造函数创建对象（如果配置了动态代理，则返回代理对象）")
	public Object create(Class[] parameterTypes, Object[] parameters) throws Exception {
		try{
			if(this.singleton){//如果是单例模式
				synchronized(this){
					//需要创建的对象
					Object _new=null;

					//如果单例尚未创建
					if(instance==null){
						//创建对象
						Class clazz=Class.forName(this.cls);
						_new=clazz.getDeclaredConstructor(parameterTypes).newInstance(parameters);

						//初始化处理
						init(clazz, _new);
					}

					//如果是新创建了对象
					if(_new!=null){
						//保存单例
						instance=_new;

						//如果设置了AOP模式
						if(this.getProxy()!=null&&!"".equals(this.getProxy())){
							NvwaProxy proxyInstance=(NvwaProxy)Class.forName(this.getProxy()).getDeclaredConstructor().newInstance();
							_new=proxyInstance.bind(_new);
							instance=_new;
						}
					}

					return instance;
				}
			}else{//如果不是单例模式
				//创建对象
				Class clazz=Class.forName(this.cls);
				Object _new=clazz.getDeclaredConstructor(parameterTypes).newInstance(parameters);

				//初始化处理
				init(clazz,_new);

				//如果设置了AOP模式
				if(this.getProxy()!=null&&!"".equals(this.getProxy())){
					NvwaProxy proxyInstance=(NvwaProxy)Class.forName(this.getProxy()).getDeclaredConstructor().newInstance();
					_new=proxyInstance.bind(_new);
				}

				return _new;
			}
		}catch(Exception e){
			throw e;
		}
	}

	/**
	 *
	 * @param clazz
	 * @param _new
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/20",
			description = "注入")
	private void init(Class clazz, Object _new) throws Exception{
		//注入基础类型或对象
		List<NvwaField> _fields=fields.listValues();
		for(int i=0;i<_fields.size();i++){
			NvwaField field=_fields.get(i);

			String initValue=field.getInitValue();

			if(JUtilString.isBlank(initValue)) continue;

			//setter
			String setter=JUtilBean.getSetterName(field.getName());
			
			Method[] methods=clazz.getMethods();//所有方法
			for(int m=0; m<methods.length; m++){
				if(!methods[m].getName().equals(setter)) continue;

				if (NvwaField.TYPE_STRING.equalsIgnoreCase(field.getType())) {
					methods[m].invoke(_new, initValue);
				} else if (NvwaField.TYPE_INTEGER.equalsIgnoreCase(field.getType())
						|| NvwaField.TYPE_INTEGER_PLAIN.equalsIgnoreCase(field.getType())) {
					if (JUtilMath.isInt(initValue)) methods[m].invoke(_new, Integer.valueOf(initValue));
				} else if (NvwaField.TYPE_LONG.equalsIgnoreCase(field.getType())) {
					if (JUtilMath.isLong(initValue)) methods[m].invoke(_new, Long.valueOf(initValue));
				} else if (NvwaField.TYPE_DOUBLE.equalsIgnoreCase(field.getType())) {
					if (JUtilMath.isNumber(initValue)) methods[m].invoke(_new, Double.valueOf(initValue));
				} else if (NvwaField.TYPE_TIMESTAMP.equalsIgnoreCase(field.getType())) {
					if (JUtilTimestamp.isTimestamp(initValue)) methods[m].invoke(_new, Timestamp.valueOf(initValue));
				} else if (NvwaField.TYPE_BOOLEAN.equalsIgnoreCase(field.getType())) {
					methods[m].invoke(_new, Boolean.valueOf("true".equalsIgnoreCase(initValue) || "1".equalsIgnoreCase(initValue) || "T".equalsIgnoreCase(initValue)));
				} else if (NvwaField.TYPE_REF.equals(field.getType())) {//引用其它对象
					methods[m].invoke(_new, Nvwa.create(initValue));
				} else if (NvwaField.TYPE_CLASS.equals(field.getType())) {//指定类名
					methods[m].invoke(_new, Class.forName(initValue).getDeclaredConstructor().newInstance());
				}

				break;//只关注第一个匹配的set方法（所以此种情景下，不应该设计多个同名的set方法）
			}
		}
		//注入基础类型或对象 end

		//注入para*.xml配置信息
		Field[] declaredFields = clazz.getDeclaredFields();
		for(int i=0; declaredFields!=null && i<declaredFields.length; i++){
			Field field=declaredFields[i];

			NvwaParameter np= fieldConfigViaPara.get(field.getName());
			if(np==null) continue;

			String initValue=AppConfig.getPara(np.getGroup(), np.getName());
			if(JUtilString.isBlank(initValue)) initValue=np.getDefaultValue();
			if(JUtilString.isBlank(initValue)) continue;

			//setter
			String setter=JUtilBean.getSetterName(field.getName());

			//字段类型
			String fieldType=field.getType().getCanonicalName();

			Method[] methods=clazz.getMethods();//所有方法
			for(int m=0; m<methods.length; m++){
				if(!methods[m].getName().equals(setter)) continue;

				if ("java.lang.String".equalsIgnoreCase(fieldType)) {
					methods[m].invoke(_new, initValue);
				} else if ("java.lang.Integer".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_INTEGER_PLAIN.equals(fieldType)) {
					if (JUtilMath.isInt(initValue)) methods[m].invoke(_new, Integer.valueOf(initValue));
				} else if ("java.lang.Long".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_LONG_PLAIN.equals(fieldType)) {
					if (JUtilMath.isLong(initValue)) methods[m].invoke(_new, Long.valueOf(initValue));
				} else if ("java.lang.Double".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_DOUBLE_PLAIN.equals(fieldType)) {
					if (JUtilMath.isNumber(initValue)) methods[m].invoke(_new, Double.valueOf(initValue));
				} else if ("java.sql.Timestamp".equalsIgnoreCase(fieldType)) {
					if (JUtilTimestamp.isTimestamp(initValue)) methods[m].invoke(_new, Timestamp.valueOf(initValue));
				} else if ("java.lang.Boolean".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_BOOLEAN_PLAIN.equals(fieldType)) {
					methods[m].invoke(_new, Boolean.valueOf("true".equalsIgnoreCase(initValue) || "1".equalsIgnoreCase(initValue) || "T".equalsIgnoreCase(initValue)));
				}

				break;//只关注第一个匹配的set方法（所以此种情景下，不应该设计多个同名的set方法）
			}
		}
		//注入para*.xml配置信息 end

		//注入properties配置信息
		for(int i=0; declaredFields!=null && i<declaredFields.length; i++){
			Field field=declaredFields[i];

			NvwaProperty np= fieldConfigViaProp.get(field.getName());

			if(np==null) continue;

			String initValue=JProperties.getInstance(np.getPath())._getProperty(np.getName());
			if(JUtilString.isBlank(initValue)) initValue=np.getDefaultValue();
			if(JUtilString.isBlank(initValue)) continue;

			//setter
			String setter=JUtilBean.getSetterName(field.getName());

			//字段类型
			String fieldType=field.getType().getCanonicalName();

			Method[] methods=clazz.getMethods();//所有方法
			for(int m=0; m<methods.length; m++){
				if(!methods[m].getName().equals(setter)) continue;

				if ("java.lang.String".equalsIgnoreCase(fieldType)) {
					methods[m].invoke(_new, initValue);
				} else if ("java.lang.Integer".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_INTEGER_PLAIN.equals(fieldType)) {
					if (JUtilMath.isInt(initValue)) methods[m].invoke(_new, Integer.valueOf(initValue));
				} else if ("java.lang.Long".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_LONG_PLAIN.equals(fieldType)) {
					if (JUtilMath.isLong(initValue)) methods[m].invoke(_new, Long.valueOf(initValue));
				} else if ("java.lang.Double".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_DOUBLE_PLAIN.equals(fieldType)) {
					if (JUtilMath.isNumber(initValue)) methods[m].invoke(_new, Double.valueOf(initValue));
				} else if ("java.sql.Timestamp".equalsIgnoreCase(fieldType)) {
					if (JUtilTimestamp.isTimestamp(initValue)) methods[m].invoke(_new, Timestamp.valueOf(initValue));
				} else if ("java.lang.Boolean".equalsIgnoreCase(fieldType)
						||NvwaField.TYPE_BOOLEAN_PLAIN.equals(fieldType)) {
					methods[m].invoke(_new, Boolean.valueOf("true".equalsIgnoreCase(initValue) || "1".equalsIgnoreCase(initValue) || "T".equalsIgnoreCase(initValue)));
				}

				break;//只关注第一个匹配的set方法（所以此种情景下，不应该设计多个同名的set方法）
			}
		}
		//注入para*.xml配置信息 end
	}
	
	@Override
	public String toString(){
		return "{\"code\":\""+this.code+"\",\"name\":\""+this.name+"\",\"cls\":\""+this.cls+"\",\"proxy\":\""+this.proxy+"\",\"singleton\":\""+this.singleton+"\",\"properties\":\""+this.propertiesPath+"\"}";
	}
}
