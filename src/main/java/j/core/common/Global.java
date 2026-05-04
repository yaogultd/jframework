package j.core.common;

import j.util.JUtilString;
import j.util.JUtilUUID;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author 肖炯
 *
 */
public class Global {
	public static final Charset charsetUTF8 = StandardCharsets.UTF_8;

	public static final String filePathSeparator=File.separator;//文件路径分隔符
	public static final String lineSeparator=System.getProperty("line.separator");//系统换行符
	public static final int lineSeparatorLength=System.getProperty("line.separator").length();//系统换行符长度

	public static final String S_JFRAMEWORK="JFRAMEWORK";
	public static final String S_JFRAMEWORK_LOG="JFRAMEWORK_LOG";
	public static final String S_DEFAULT="DEFAULT";

	private static String RUN_UUID=null;

	static {
		RUN_UUID= JUtilUUID.genUUID();
	}

	/**
	 *
	 * @return
	 */
	public static String getRunUuid(){
		return JUtilString.isBlank(JProperties.getEnv("RunTimeUUID")) ? RUN_UUID : JProperties.getEnv("RunTimeUUID");
	}

	/**
	 *
	 */
	public static final void sleep1000Millis(){
		try{
			Thread.sleep(1000);
		}catch(Exception e){}
	}

	/**
	 *
	 */
	public static final void sleep100Millis(){
		try{
			Thread.sleep(100);
		}catch(Exception e){}
	}

	/**
	 *
	 */
	public static final void sleep10Millis(){
		try{
			Thread.sleep(10);
		}catch(Exception e){}
	}

	/**
	 *
	 */
	public static final void sleep1Millis(){
		try{
			Thread.sleep(1);
		}catch(Exception e){}
	}

	/**
	 *
	 */
	public static final void sleep0Millis(){
		try{
			Thread.sleep(0);
		}catch(Exception e){}
	}
}
