package j.core.common;

import j.util.JUtilBean;
import j.util.JUtilZip;

import java.io.*;
import java.lang.reflect.Method;

/**
 * 
 * @author 肖炯
 *
 */
public class JObject implements Serializable{
	/**
	 *
	 *
	 */
	public JObject() {
		super();
	}

	/**
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static byte[] serialize(Object obj) throws Exception{
		if(obj==null) return null;

		ByteArrayOutputStream byteOS=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(byteOS);
		oos.writeObject(obj);
		oos.flush();
		byte[] bytes = byteOS.toByteArray();

		try{
			oos.close();
		}catch(Exception e){}

		try{
			byteOS.close();
		}catch(Exception e){}

		return bytes;
	}

	/**
	 *
	 * @param bytes
	 * @return
	 */
	public static Object deSerialize(byte[] bytes) throws Exception{
		if(bytes==null || bytes.length==0) return null;

		ObjectInputStream ois=null;
		try{
			ois=new ObjectInputStream(new ByteArrayInputStream(bytes));
			Object obj=ois.readObject();
			try{
				ois.close();
			}catch(Exception ex){}
			return obj;
		}catch (Exception e){
			if(ois!=null){
				try{
					ois.close();
				}catch(Exception ex){}
			}
			throw e;
		}
	}

	/**
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String serializable2String(Serializable obj)throws Exception{
		if(obj==null){
			return "jserializable:null";
		}

		ByteArrayOutputStream byteOS=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(byteOS);
		oos.writeObject(obj);
		oos.flush();
		String ret= byteOS.toString("ISO-8859-1");

		try{
			oos.close();
		}catch(Exception e){}

		try{
			byteOS.close();
		}catch(Exception e){}

		ret="jserializable:"+JObject.string2IntSequence(ret);
		ret=JUtilZip.gzipString(ret,"ISO-8859-1");

		return ret;
	}

	/**
	 *
	 * @param obj
	 * @param gzip
	 * @return
	 * @throws Exception
	 */
	public static String serializable2String(Serializable obj,boolean gzip)throws Exception{
		if(obj==null){
			return "jserializable:null";
		}
		ByteArrayOutputStream byteOS=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(byteOS);
		oos.writeObject(obj);
		oos.flush();
		String ret= byteOS.toString("ISO-8859-1");

		try{
			oos.close();
		}catch(Exception e){}

		try{
			byteOS.close();
		}catch(Exception e){}

		ret="jserializable:"+JObject.string2IntSequence(ret);
		if(gzip) ret=JUtilZip.gzipString(ret,"ISO-8859-1");

		return ret;
	}

	/**
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static Object string2Serializable(String str) throws Exception{
		if(str==null||"jserializable:null".equals(str)){
			return null;
		}

		if(!str.startsWith("jserializable:")){
			try {
				str=JUtilZip.readGzipString(str,"ISO-8859-1");
			}catch(Exception e) {}
		}
		str=JObject.intSequence2String(str.substring(14));

		ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
		Object obj=ois.readObject();

		try{
			ois.close();
		}catch(Exception e){}

		return obj;
	}

	/**
	 *
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static Object inputStream2Serializable(InputStream in) throws Exception{
		if(in==null){
			return null;
		}

		ObjectInputStream ois=new ObjectInputStream(in);
		Object obj=ois.readObject();

		try{
			ois.close();
		}catch(Exception e){}

		return obj;
	}



	/**
	 *
	 * @param string
	 * @return
	 */
	public static String string2IntSequence(String string){
		return string2IntSequence(string, ",");
	}

	/**
	 *
	 * @param sequence
	 * @return
	 */
	public static String intSequence2String(String sequence){
		return intSequence2String(sequence,",");
	}


	/**
	 *
	 * @param string
	 * @return
	 */
	public static String string2IntSequence(String string, String splitter){
		if(string==null) return null;
		if(splitter==null || "".equals(splitter)) splitter=",";

		StringBuffer writer=new StringBuffer("jis:");
		for(int i=0;i<string.length();i++){
			String cha=Integer.toString((int)string.charAt(i),Character.MAX_RADIX);

			if(i==0) writer.append(cha);
			else writer.append(splitter+cha);
		}
		string = writer.toString();
		writer=null;

		return string;
	}

	/**
	 *
	 * @param sequence
	 * @return
	 */
	public static String intSequence2String(String sequence, String splitter){
		if(sequence==null||sequence.equals("")) return sequence;

		while(sequence.startsWith("jis:")) sequence=sequence.substring(4);
		if("".equals(sequence)) return sequence;

		if(splitter==null || "".equals(splitter)) splitter=",";

		StringBuffer writer=new StringBuffer();
		String[] arr=sequence.split(",");
		for(int i=0;i<arr.length;i++){
			writer.append((char)Integer.parseInt(arr[i],Character.MAX_RADIX));
		}
		arr=null;
		sequence=writer.toString();
		writer=null;
		return sequence;
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	public static boolean isBlank(Object object){
		return object==null;
	}

	/**
	 *
	 * @param objects
	 * @return
	 */
	public static boolean isBlank(Object[] objects){
		return objects==null || objects.length==0;
	}

	/**
	 *
	 * @param one
	 * @param other
	 * @return
	 */
	public static boolean equals(Object one, Object other){
		if(one==null && other==null) return true;
		if(one!=null && other==null) return false;
		if(one==null && other!=null) return false;
		return one.equals(other);
	}

	/**
	 *
	 * @param one
	 * @param other
	 * @return
	 */
	public static boolean equals(Object[] one, Object[] other){
		if(JObject.isBlank(one) && JObject.isBlank(other)) return true;

		if(JObject.isBlank(one) && !JObject.isBlank(other)) return false;

		if(!JObject.isBlank(one) && JObject.isBlank(other)) return false;

		if(one.length != other.length) return false;

		for(int i=0; i<one.length; i++){
			if(!equals(one[i], other[i])) return false;
		}

		return true;
	}

	/**
	 *
	 * @param cls
	 * @param methodName
	 * @param paramaters
	 * @return
	 */
	public static Method getMethod(Class cls, String methodName, Object[] paramaters){
		if(paramaters==null || paramaters.length==0) return getMethod(cls, methodName, (Class[])null);

		Class[] types=new Class[paramaters.length];
		for(int i=0; i<types.length; i++){
			types[i]=paramaters[i]==null?null:paramaters[i].getClass();
		}
		return getMethod(cls, methodName, types);
	}

	/**
	 *
	 * @param cls
	 * @param methodName
	 * @param paramaterTypes
	 * @return
	 */
	public static Method getMethod(Class cls, String methodName, Class[] paramaterTypes){
		Method[] methods = cls.getDeclaredMethods();
		for(int i=0; i<methods.length; i++){
			if(!methods[i].getName().equals(methodName)) continue;

			Class[] types=methods[i].getParameterTypes();
			if(!equals(types, paramaterTypes)) continue;

			return methods[i];
		}

		return getMethod(cls, methodName, paramaterTypes==null?0:paramaterTypes.length);
	}

	/**
	 *
	 * @param cls
	 * @param methodName
	 * @param parameters
	 * @return
	 */
	public static Method getMethod(Class cls, String methodName, int parameters){
		Method[] methods = cls.getDeclaredMethods();
		for(int i=0; i<methods.length; i++){
			if(!methods[i].getName().equals(methodName)) continue;

			Class[] types=methods[i].getParameterTypes();
			int _types=types==null?0:types.length;

			if(_types!=parameters) continue;

			return methods[i];
		}

		return null;
	}

	@Override
	public String toString(){
		return JUtilBean.bean2Json(this);
	}
}