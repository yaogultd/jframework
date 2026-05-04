package j.core.nvwa.resource;

import j.core.Startup;
import j.core.common.JProperties;
import j.core.nvwa.Nvwa;
import j.core.sys.SysConfig;
import j.log.Logger;
import j.util.JUtilInputStream;
import j.util.JUtilString;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author 肖炯
 *
 */
public class ResourceHelper {
	private static Logger log = Logger.create(ResourceHelper.class);

	/**
	 * 配置文件/证书文件保存目录
	 * @return
	 */
	public static String configDir(){
		String saveInDir=JProperties.getConfigPath();
		File file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(), "config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/classes/config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/classes/config");
		file=new File(saveInDir);
		if(!file.exists()) return saveInDir="config";

		return saveInDir;
	}


	/**
	 * 多语言配置目录
	 * @return
	 */
	public static String I18NDir(){
		String saveInDir=JProperties.getEnv("I18NPath");
		File file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(), "I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/classes/I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/classes/I18N");
		file=new File(saveInDir);
		if(!file.exists()) return saveInDir="I18N";

		return saveInDir;
	}

	/**
	 * 配置文件/证书文件保存目录
	 * @return
	 */
	public static String configDirExternal(){
		String saveInDir=JProperties.getConfigPath();
		File file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(), "config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/classes/config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/config");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/classes/config");

		return saveInDir;
	}


	/**
	 * 多语言配置目录
	 * @return
	 */
	public static String I18NDirExternal(){
		String saveInDir=JProperties.getEnv("I18NPath");
		File file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(), "I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getUserDir(),"WEB-INF/classes/I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/I18N");
		file=new File(saveInDir);
		if(file.exists()) return saveInDir;

		saveInDir=JUtilString.appendPath(JProperties.getAppRoot(),"WEB-INF/classes/I18N");

		return saveInDir;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private static URL find(String path) {
		try {
			return new URL(path);
		} catch (MalformedURLException e) {
			return findAsResource(path);
		}
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private static URL findAsResource(String path) {
		try{
			if(path.startsWith("/")) path=path.substring(1);

			URL url = Thread.currentThread().getContextClassLoader().getResource(path);
			if (url != null) return url;

			url = ResourceHelper.class.getClassLoader().getResource(path);
			if (url != null) return url;

			url = ClassLoader.getSystemClassLoader().getResource(path);

			return url;
		}catch (Exception e){
			return null;
		}
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static String getString(String path){
		InputStream is=null;
		try{
			//按绝对路径查找文件
			File file=new File(path);
			if(file.exists()){
				try{
					is = new FileInputStream(file);
				}catch (Exception e){
					log.log(e, Logger.LEVEL_ERROR);
				}
			}

			if(is == null){
				try{
					URL url = ResourceHelper.find(getRelativePath(path));
					if (url != null) is = url.openStream();
				}catch (Exception e){
					log.log(e, Logger.LEVEL_ERROR);
				}
			}

			if(is == null){
				Resource resource = Nvwa.getResourceInDeployedJar(path);
				if(resource == null) resource = Nvwa.getResourceInDeployedJar(getRelativePath(path));
				if(resource != null) return resource.getString();
			}

			return is==null?null:JUtilInputStream.string(is, SysConfig.sysEncoding);
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public static InputStream getInputStream(String path){
		InputStream is=null;
		try{
			//按绝对路径查找文件
			File file=new File(path);
			if(file.exists()){
				try{
					is = new FileInputStream(file);
				}catch (Exception e){
					log.log(e, Logger.LEVEL_ERROR);
				}
			}

			if(is == null){
				try{
					URL url = ResourceHelper.find(getRelativePath(path));
					if (url != null) is = url.openStream();
				}catch (Exception e){
					log.log(e, Logger.LEVEL_ERROR);
				}
			}

			if(is == null){
				Resource resource = Nvwa.getResourceInDeployedJar(path);
				if(resource == null) resource = Nvwa.getResourceInDeployedJar(getRelativePath(path));
				if(resource != null && resource.getString() != null) is=new ByteArrayInputStream(resource.getString().getBytes(StandardCharsets.UTF_8));
			}

			return is;
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}
	}

	/**
	 * 替换掉框架保留关键变量为实际运行值
	 * @param pathOrOther
	 * @return
	 */
	public static String replaceEnvVariables(String pathOrOther){
		if(JUtilString.isBlank(pathOrOther)) return pathOrOther;

		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_HOME", JProperties.getAppRoot());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "USER_DIR", JProperties.getUserDir());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_CONFIG_PATH", JProperties.getConfigPath());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_CLASSES_PATH", JProperties.getClassPath());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_WEBROOT", JProperties.getAppRoot());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_I18N_PATH", JProperties.getI18NPath());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_UI_PATH", JProperties.getAppRoot());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_JDFS_PATH", JProperties.getJDFSPath());
		pathOrOther=JUtilString.replaceAll(pathOrOther, "JFRAMEWORK_DATA_PATH", JProperties.getDataPath());

		return pathOrOther;
	}

	/**
	 * 如果以框架保留关键变量开头（绝对路径），调整为相对路径
	 * @param path
	 * @return
	 */
	public static String getRelativePath(String path){
		if(JUtilString.isBlank(path)) return path;

		if(!JUtilString.isBlank(JProperties.getAppRoot())
				&&path.startsWith(JProperties.getAppRoot())) path=path.substring(JProperties.getAppRoot().length());

		if(!JUtilString.isBlank(JProperties.getConfigPath())
				&&path.startsWith(JProperties.getConfigPath())) path=path.substring(JProperties.getConfigPath().length());

		if(!JUtilString.isBlank(JProperties.getClassPath())
				&&path.startsWith(JProperties.getClassPath())) path=path.substring(JProperties.getClassPath().length());

		if(!JUtilString.isBlank(JProperties.getI18NPath())
				&&path.startsWith(JProperties.getI18NPath())) path=path.substring(JProperties.getI18NPath().length());

		if(!JUtilString.isBlank(JProperties.getJDFSPath())
				&&path.startsWith(JProperties.getJDFSPath())) path=path.substring(JProperties.getJDFSPath().length());

		if(path.startsWith("/")) path=path.substring(1);

		return path;
	}
}
