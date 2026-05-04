package j.log;


import j.core.annotation.action.Handler;

/**
 * 
 * @author 肖炯
 *
 */
public abstract class Logger{
	public static final int LEVEL_DEBUG_ADV=0;
	public static final int LEVEL_DEBUG=1;
	public static final int LEVEL_INFO=2;
	public static final int LEVEL_WARNING=3;
	public static final int LEVEL_ERROR=4;
	public static final int LEVEL_FATAL=5;
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static Logger create(Class clazz){
		return new LoggerDefault(clazz);
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public static Logger create(String name){
		return new LoggerDefault(name);
	}
	
	/**
	 * 输出Exception信息
	 * @param exception
	 * @param level
	 */
	public abstract void log(Exception exception, int level);
	
	/**
	 * 输出文本消息
	 * @param message
	 * @param level
	 */
	public abstract void log(String message, int level);
}
