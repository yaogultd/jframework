package j.log;

import j.core.common.JProperties;
import j.core.sys.SysConfig;
import j.util.JUtilTimestamp;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 肖炯
 *
 */
public class LoggerDefault extends Logger{
	protected static int logLevel=Logger.LEVEL_INFO;//系统设置的日志错误输出级别
	private org.slf4j.Logger log;

	static{
		try{
			String temp=JProperties.getLogLevel();
			if("LEVEL_DEBUG_ADV".equalsIgnoreCase(temp)) logLevel=Logger.LEVEL_DEBUG_ADV;
			else if("LEVEL_DEBUG".equalsIgnoreCase(temp)) logLevel=Logger.LEVEL_DEBUG;
			else if("LEVEL_INFO".equalsIgnoreCase(temp)) logLevel=Logger.LEVEL_INFO;
			else if("LEVEL_WARNING".equalsIgnoreCase(temp)) logLevel=Logger.LEVEL_WARNING;
			else if("LEVEL_ERROR".equalsIgnoreCase(temp)) logLevel=Logger.LEVEL_ERROR;
			else if("LEVEL_FATAL".equalsIgnoreCase(temp)) logLevel=Logger.LEVEL_FATAL;
		}catch(Exception e){}

		System.out.println(JUtilTimestamp.timestamp()+" log level is "+logLevel);
	}

	/**
	 *
	 * @param clazz
	 */
	public LoggerDefault(Class clazz){
		log=LoggerFactory.getLogger(clazz);
	}

	/**
	 *
	 * @param name
	 */
	public LoggerDefault(String name){
		log=LoggerFactory.getLogger(name);
	}

	/**
	 * 如果level小于0或<=系统设定级别，则输出日志
	 * @param exception
	 * @param level
	 */
	public void log(Exception exception,int level){
		if(level<logLevel&&level>=0) return;

		if(level==LEVEL_DEBUG||level==LEVEL_DEBUG_ADV){
			log.debug("["+SysConfig.getSysId()+"]"+exception.getMessage(), exception);
		}else if(level==LEVEL_INFO){
			log.info("["+SysConfig.getSysId()+"]"+exception.getMessage(), exception);
		}else if(level==LEVEL_WARNING){
			log.warn("["+SysConfig.getSysId()+"]"+exception.getMessage(), exception);
		}else if(level==LEVEL_ERROR){
			log.error("["+SysConfig.getSysId()+"]"+exception.getMessage(), exception);
		}else if(level==LEVEL_FATAL){
			log.error("["+SysConfig.getSysId()+"]"+exception.getMessage(), exception);
		}else{
			log.info("["+SysConfig.getSysId()+"]"+exception.getMessage(), exception);
		}
	}

	/**
	 * 如果level小于0或<=系统设定级别，则输出日志
	 * @param message
	 * @param level
	 */
	public void log(String message,int level){
		if(level<logLevel&&level>=0) return;

		if(level==LEVEL_DEBUG||level==LEVEL_DEBUG_ADV){
			log.debug("["+SysConfig.getSysId()+"]"+message);
		}else if(level==LEVEL_INFO){
			log.info("["+SysConfig.getSysId()+"]"+message);
		}else if(level==LEVEL_WARNING){
			log.warn("["+SysConfig.getSysId()+"]"+message);
		}else if(level==LEVEL_ERROR){
			log.error("["+SysConfig.getSysId()+"]"+message);
		}else if(level==LEVEL_FATAL){
			log.error("["+SysConfig.getSysId()+"]"+message);
		}else{
			log.info("["+SysConfig.getSysId()+"]"+message);
		}
	}
}