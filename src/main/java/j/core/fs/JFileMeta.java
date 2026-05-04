package j.core.fs;

import java.io.File;
import java.io.Serializable;

/**
 * 
 * @author 肖炯
 *
 */
public class JFileMeta implements Serializable{
	private static final long serialVersionUID = 1L;
	public boolean canExecute=false;
	public boolean canRead=true;
	public boolean canWrite=true;
	public boolean exists=false;
	public String absolutePath=null;
	public String canonicalPath=null;
	public long freeSpace=0;
	public String name=null;
	public String parent=null;
	public String path=null;
	public long totalSpace=0;
	public long usableSpace=0;
	public boolean isAbsolute=true;
	public boolean isDirectory=false;
	public boolean isFile=false;
	public boolean isHidden=false;
	public long lastModified=0;
	public long length=0;

	/**
	 *
	 */
	public JFileMeta() {
	}
	
	/**
	 * 
	 * @param file
	 */
	public JFileMeta(File file) {
		getMeta(file);
	}
	
	/**
	 * 
	 * @param file
	 */
	public void getMeta(File file) {
		if(file!=null){
			this.canExecute=file.canExecute();
			this.canRead=file.canRead();
			this.canWrite=file.canWrite();
			this.exists=file.exists();
			this.absolutePath=file.getAbsolutePath();
			try{
				this.canonicalPath=file.getCanonicalPath();
			}catch(Exception e){}
			this.freeSpace=file.getFreeSpace();
			this.name=file.getName();
			this.parent=file.getParent();
			this.path=file.getPath();
			this.totalSpace=file.getTotalSpace();
			this.usableSpace=file.getUsableSpace();
			this.isAbsolute=file.isAbsolute();
			this.isDirectory=file.isDirectory();
			this.isFile=file.isFile();
			this.isHidden=file.isHidden();
			this.lastModified=file.lastModified();
			this.length=file.length();
		}
	}
}
