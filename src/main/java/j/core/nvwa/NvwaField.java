package j.core.nvwa;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;

@ClassDescription(author = "肖炯",
		date = "2021/07/19",
		description = "需要注入的对象（基本类型、或其它对象）",
		reviewers = {})
public class NvwaField {
	@FieldDescription(description = "注入对象类型：java.lang.String")
	public final static String TYPE_STRING="String";

	@FieldDescription(description = "注入对象类型：java.lang.Integer")
	public final static String TYPE_INTEGER="Integer";
	public final static String TYPE_INTEGER_PLAIN="int";

	@FieldDescription(description = "注入对象类型：java.lang.Long")
	public final static String TYPE_LONG="Long";
	public final static String TYPE_LONG_PLAIN="long";

	@FieldDescription(description = "注入对象类型：java.lang.Short")
	public final static String TYPE_SHORT="Short";
	public final static String TYPE_SHORT_PLAIN="short";

	@FieldDescription(description = "注入对象类型：java.lang.Double")
	public final static String TYPE_DOUBLE="Double";
	public final static String TYPE_DOUBLE_PLAIN="double";

	@FieldDescription(description = "注入对象类型：java.sql.Timestamp")
	public final static String TYPE_TIMESTAMP="Timestamp";

	@FieldDescription(description = "注入对象类型：java.lang.Boolean")
	public final static String TYPE_BOOLEAN="Boolean";
	public final static String TYPE_BOOLEAN_PLAIN="boolean";

	@FieldDescription(description = "注入对象类型：另外一个对象")
	public final static String TYPE_REF="Ref";

	@FieldDescription(description = "注入对象类型：指定类的对象")
	public final static String TYPE_CLASS="Class";

	private String name;//注入对象的变量名称
	private String type;//注入对象的类型
	private String initValue;//初始值（或引用对象的code）
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @param initValue
	 */
	public NvwaField(String name,String type,String initValue){
		this.name=name;
		this.type=type;
		this.initValue=initValue;
	}

	//getters
	public String getName(){
		return this.name;
	}

	public String getType(){
		return this.type;
	}

	public String getInitValue(){
		return this.initValue;
	}
	//getters end
}
