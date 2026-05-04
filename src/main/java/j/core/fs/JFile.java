package j.core.fs;

import j.core.common.Global;
import j.core.nvwa.Nvwa;
import j.util.JUtilString;

import java.io.File;
import java.io.InputStream;


/**
 * 该类继承了java.io.File类，并定义了几个实用方法，如把文件读内容取成字符串、保存流至文件等；JDFSFile继承JFile，并实现java.io.File和JFile定义的相关方法。
 * @author 肖炯
 *
 */
public abstract class JFile extends java.io.File {
	public static final String NOT_SUPPORTED="NOT_SUPPORTED";
	
	/**
	 * 
	 * @param path
	 */
	public JFile(String path) {
		super(path);
	}
	
	/**
	 * 
	 * @param path
	 * @param os
	 * @return
	 */
	public static String adjustFileSeperator(String path,String os){
		if(path==null||"".equals(path)) return path;
		
		if("linux".equalsIgnoreCase(os)){
        	path=JUtilString.replaceAll(path,"\\","/");
			if(path.endsWith("//")) path=path.substring(0, path.length()-1);
    	}else{
        	path=JUtilString.replaceAll(path,"/","\\");
			if(path.endsWith("\\\\")) path=path.substring(0, path.length()-1);
    	}
		
		return path;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static JFile create(String path){
		return (JFile)Nvwa.create("JDFS",new Class[]{String.class},new Object[]{path});
	}

	/**
	 * 将文件读取成字节数组
	 * @return
	 * @throws Exception
	 */
	public abstract byte[] bytes()throws Exception;

	/**
	 * 将文件读取成字符串
	 * @return
	 * @throws Exception
	 */
	public abstract String string()throws Exception;
	
	
	/**
	 * 将文件读取成指定编码的字符串
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public abstract String string(String encoding)throws Exception;
	
	
	/**
	 * 将字符串保存至本对象所代表的文件
	 * @param content
	 * @param append  true:追加至文件已存在内容 false:覆盖文件已存在的内容
	 * @throws Exception
	 */
	public abstract void save(String content,boolean append)throws Exception;
	
	
	/**
	 * 将字符串按指定编码保存至本对象所代表的文件
	 * @param content
	 * @param append  true:追加至文件已存在内容 false:覆盖文件已存在的内容
	 * @param encoding
	 * @throws Exception
	 */
	public abstract void save(String content,boolean append,String encoding)throws Exception;
	
	/**
	 * 将字节写入文件
	 * @param bytes
	 * @throws Exception
	 */
	public abstract void save(byte[] bytes)throws Exception;

	/**
	 * 将输入流写入文件
	 * @param is
	 * @throws Exception
	 */
	public abstract void save(InputStream is)throws Exception;

	/**
	 * 把本地文件写入分布式系统
	 * @param file
	 * @throws Exception
	 */
	public abstract void save(File file)throws Exception;
}
