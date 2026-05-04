package j.core.fs;

import j.core.annotation.nvwa.Nvwa;
import j.core.fs.storage.Storage;
import j.log.Logger;
import j.util.JUtilInputStream;
import j.util.JUtilString;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author 肖炯f
 *
 */
@Nvwa(code = "JDFS", singleton = Nvwa.SIGNLETON.FALSE)
@Getter
public class JDFSFile extends JFile{
	private static final long serialVersionUID = 1L;
	private static Logger log=Logger.create(JDFSFile.class);
	private JDFSMapping mapping=null;
	private JFileMeta meta=null;
	private String path=null;
	private String virtualPath=null;
	private String physicalPath=null;

	///////////////一些实用的方法////////////////////////
	/**
	 * 将文件读取成字符串
	 * @return
	 */
	public static String read(File file){
		return read(file,null);
	}

	/**
	 * 将文件读取成指定编码的字符串
	 * @param encoding
	 * @return
	 */
	public static String read(File file,String encoding){
		try{
	    	if(file.exists()){
	    		if(!JUtilString.isBlank(encoding)) return JUtilInputStream.string(new FileInputStream(file),encoding);
	        	else return JUtilInputStream.string(new FileInputStream(file));
	    	}else{
	    		return null;
	    	}
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 将输入流保持到文件
	 * @param is
	 * @param path 本地文件路径
	 */
	public static void save(InputStream is, String path){
		save(is, path, true);
	}

	/**
	 *
	 * @param is
	 * @param path
	 * @param closeStreamOnEnd
	 */
	public static void save(InputStream is, String path, boolean closeStreamOnEnd){
		try{
			File file=new File(path);
			if(file.exists()) file.delete();
			else file.getParentFile().mkdirs();

			OutputStream os=new FileOutputStream(file);

			byte[] buffer=new byte[1024];
			int readed=is.read(buffer);
			while(readed>-1){
				os.write(buffer,0, readed);
				readed=is.read(buffer);
			}
			os.flush();

			if(closeStreamOnEnd){
				try{
					is.close();
				}catch(Exception e){}
			}

			try{
				os.close();
			}catch(Exception e){}
		}catch(Exception e){
			if(closeStreamOnEnd) {
				try {
					is.close();
				} catch (Exception ex) {
				}
			}
			log.log(e,Logger.LEVEL_ERROR);
		}
	}
	
	/**
	 * 将字符串以指定编码保持到文件
	 * @param path 本地文件路径
	 * @param content
	 * @param append
	 * @param encoding
	 */
	public static void save(String path, String content, boolean append, String encoding){
		try{			
			File file=new File(path);
			file.getParentFile().mkdirs();
			
			Writer writer=null;
			if(!JUtilString.isBlank(encoding)) writer=new OutputStreamWriter(new FileOutputStream(file,append),encoding);
			else writer=new OutputStreamWriter(new FileOutputStream(file,append));
			writer.write(content);
			writer.flush();
			
			try{
				writer.close();
			}catch(Exception e){}
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 删除文件或目录，如果是非空目录将删除目录内所有内容
	 * @param file
	 */
	public static void delete(File file){
		if(!file.exists()) return;

		if(file.isFile()){
			file.delete();
			return;
		}

		File[] files=file.listFiles();
		if(files==null || files.length==0){
			file.delete();
			return;
		}

		for(int i=0; i<files.length; i++){
			delete(files[i]);
		}

		file.delete();
	}
	///////////////一些实用的方法  end////////////////////////

	/**
	 *
	 * @param path
	 * @throws Exception
	 */
	public JDFSFile(String path) throws Exception {
		super(JDFS.getPhysicalPath(path));
		this.path=path;
		this.virtualPath=path;

		//文件映射
		this.mapping=JDFS.mapping(path);

		//无匹配的文件映射，按本地文件处理
		if(this.mapping==null){
			this.physicalPath=this.path;
			this.meta=new JFileMeta(new File(this.physicalPath));
			return;
		}

		//物理路径
		this.physicalPath=this.mapping.virtual2Physical(path);

		//从主要（配置文件中排在最前的）存储器获得元数据
		List<Storage> storages=this.mapping.getStorageInstances();

		this.meta=storages.get(0).getMeta(this);
		//从主要（配置文件中排在最前的）存储器获得元数据 end
	}

	@Override
	public boolean canExecute(){
		return meta==null?false:meta.canExecute;
	}


	@Override
	public boolean canRead(){
		return meta==null?false:meta.canRead;
	}


	@Override
	public boolean canWrite(){
		return meta==null?false:meta.canWrite;
	}


	@Override
	public boolean delete(){
		//本地文件
		if(this.mapping==null) return super.delete();

		//从各个存储器中删除
		List<Storage> storages=this.mapping.getStorageInstances();

		for(int i=0; i<storages.size(); i++){
			storages.get(i).delete(this);
		}
		//从各个存储器中删除 end

		return true;
	}

	@Override
	public void deleteOnExit(){
		if(this.mapping==null) super.deleteOnExit();
		else this.delete();
	}

	@Override
	public boolean exists(){
		return this.meta==null?false:this.meta.exists;
	}

	@Override
	public File getAbsoluteFile(){
		return this;
	}

	@Override
	public String getAbsolutePath(){
		if(this.meta==null) return null;
		else if(this.mapping==null) return this.meta.absolutePath;
		else{
			if(this.isDirectory()) return this.mapping.physical2Virtual(meta.absolutePath+"/");
			else return this.mapping.physical2Virtual(this.meta.absolutePath);
		}
	}

	@Override
	public java.io.File getCanonicalFile() throws IOException{
		return JFile.create(this.getCanonicalPath());
	}

	@Override
	public String getCanonicalPath() throws IOException{
		if(this.meta==null) return null;
		else if(this.mapping==null) return this.meta.canonicalPath;
		else return this.mapping.physical2Virtual(this.meta.canonicalPath);
	}

	@Override
	public long getFreeSpace(){
		return this.meta==null?-1:this.meta.freeSpace;
	}

	@Override
	public String getName(){
		if(this.meta==null) return null;
		else if(this.mapping==null) return this.meta.name;
		else return this.mapping.physical2Virtual(this.meta.name);
	}

	@Override
	public String getParent(){
		if(this.meta==null) return null;
		else if(this.mapping==null) return this.meta.parent;
		else return this.mapping.physical2Virtual(meta.parent+"/");
	}

	@Override
	public java.io.File getParentFile(){
		return JFile.create(this.getParent());
	}

	@Override
	public String getPath(){
		if(this.meta==null) return this.physicalPath;
		else if(this.mapping==null) return this.meta.path;
		else return this.mapping.physical2Virtual(this.meta.path);
	}

	@Override
	public long getTotalSpace(){
		return this.meta==null?-1:this.meta.totalSpace;
	}

	@Override
	public long getUsableSpace(){
		return this.meta==null?-1:this.meta.usableSpace;
	}

	@Override
	public boolean isAbsolute(){
		return meta==null?false:meta.isAbsolute;
	}

	@Override
	public boolean isDirectory(){
		return meta==null?false:meta.isDirectory;
	}

	@Override
	public boolean isFile(){
		return meta==null?false:meta.isFile;
	}

	@Override
	public boolean isHidden(){
		return meta==null?false:meta.isHidden;
	}

	@Override
	public long lastModified(){
		return this.meta==null?-1:this.meta.lastModified;
	}

	@Override
	public long length(){
		return this.meta==null?-1:this.meta.length;
	}

	@Override
	public String[] list(){
		String[] ls=null;
		if(this.mapping==null){
			ls=super.list();
		}else{
			List<Storage> storages=this.mapping.getStorageInstances();

			Storage storage=storages==null||storages.isEmpty()?null:storages.get(0);

			ls=storage==null?null:storage.list(this);
		}

		for(int i=0; ls!=null && i<ls.length;i++){
			String dir=this.getAbsolutePath();
			if(!dir.endsWith("/")) dir+="/";
			ls[i]=JUtilString.appendPath(dir, ls[i]);
		}
		
		return ls;
	}

	@Override
	public String[] list(FilenameFilter filter){
		String names[] = this.list();
		if(names == null || filter == null) return names;

		ArrayList<String> v = new ArrayList();
		for (int i = 0 ; i < names.length ; i++) {
		    if(filter.accept(this, names[i])) v.add(names[i]);
		}
		return v.toArray(new String[v.size()]);
	}

	@Override
	public java.io.File[] listFiles(){
		String[] fileNames = this.list();
		if (fileNames == null) return null;

		JFile[] fs = new JFile[fileNames.length];
		for (int i = 0; i < fileNames.length; i++) {
		    fs[i] = JFile.create(fileNames[i]);
		}
		return fs;
	}

	@Override
	public java.io.File[] listFiles(FileFilter filter){
		String fileNames[] = list();
		if (fileNames == null) return null;

		ArrayList<JFile> v = new ArrayList();
		for (int i = 0 ; i < fileNames.length ; i++) {
			JFile f = JFile.create(fileNames[i]);
		    if (filter == null || filter.accept(f)) {
		    	v.add(f);
		    }
		}
		return v.toArray(new JFile[v.size()]);
	}

	@Override
	public java.io.File[] listFiles(FilenameFilter filter){
		String fileNames[] = list();
		if (fileNames == null) return null;

		ArrayList<JFile> v = new ArrayList();
		for (int i = 0 ; i < fileNames.length ; i++) {
		    if (filter == null || filter.accept(this, fileNames[i])) {
		    	v.add(JFile.create(fileNames[i]));
		    }
		}
		return v.toArray(new JFile[v.size()]);
	}

	@Override
	public boolean mkdir(){
		return this.mkdirs();
	}

	@Override
	public boolean mkdirs(){
		if(this.mapping==null) return super.mkdirs();

		//保存到各个存储器中
		List<Storage> storages=this.mapping.getStorageInstances();

		for(int i=0; i<storages.size(); i++){
			storages.get(i).mkdirs(this);
		}
		//保存到各个存储器中 end

		return true;
	}

	@Override
	public boolean renameTo(File dest){
		if(this.mapping==null) return super.renameTo(dest);

		//保存到各个存储器中
		List<Storage> storages=this.mapping.getStorageInstances();

		for(int i=0; i<storages.size(); i++){
			storages.get(i).renameTo(this, dest);
		}
		//保存到各个存储器中 end

		return true;
	}

	@Override
	public boolean setExecutable(boolean executable){
		return this.setExecutable(executable,true);
	}

	@Override
	public boolean setExecutable(boolean executable,boolean ownerOnly){
		if(this.mapping==null) return super.setExecutable(executable);

		//not implemented
		return false;
	}

	@Override
	public boolean setLastModified(long time){
		if(this.mapping==null) return super.setLastModified(time);

		//not implemented
		return false;
	}

	@Override
	public boolean setReadable(boolean readable){
		return setReadable(readable,true);
	}

	@Override
	public boolean setReadable(boolean readable,boolean ownerOnly){
		if(this.mapping==null) return super.setExecutable(readable,ownerOnly);

		//not implemented
		return false;
	}

	@Override
	public boolean setReadOnly(){
		if(this.mapping==null) return super.setReadOnly();

		//not implemented
		return false;
	}

	@Override
	public boolean setWritable(boolean writable){
		return setWritable(writable,true);
	}

	@Override
	public boolean setWritable(boolean writable,boolean ownerOnly){
		if(this.mapping==null) return super.setExecutable(writable,ownerOnly);

		//not implemented
		return false;
	}

	@Override
	public byte[] bytes() throws Exception{
		if(this.mapping==null) return JUtilInputStream.bytes(new FileInputStream(this));

		List<Storage> storages=this.mapping.getStorageInstances();

		Storage storage=storages==null||storages.isEmpty()?null:storages.get(0);

		return storage==null?null:storage.bytes(this);
	}

	@Override
	public String string() throws Exception{
		return string(null);
	}

	@Override
	public String string(String encoding) throws Exception{
		if(this.mapping==null){
			if(!JUtilString.isBlank(encoding)) return JUtilInputStream.string(new FileInputStream(this),encoding);
        	else return JUtilInputStream.string(new FileInputStream(this));
		}

		List<Storage> storages=this.mapping.getStorageInstances();

		Storage storage=storages==null||storages.isEmpty()?null:storages.get(0);

		return storage==null?null:storage.string(this, encoding);
	}

	/**
	 *
	 * @param storage
	 * @return
	 */
	public Object find(Storage storage){
		if(this.mapping==null || storage==null) return null;

		return storage.find(this.getPath());
	}

	@Override
	public void save(String content, boolean append) throws Exception {
		this.save(content,append,null);
	}

	@Override
	public void save(String content, boolean append, String encoding) throws Exception {
		if(this.mapping==null){
			JDFSFile.save(this.physicalPath, content, append, encoding);
			return;
		}

		//保存到各个存储器中
		List<Storage> storages=this.mapping.getStorageInstances();

		for(int i=0; i<storages.size(); i++){
			storages.get(i).save(this, content, append, encoding);
		}
		//保存到各个存储器中 end
	}

	@Override
	public void save(byte[] bytes) throws Exception {
		this.save(new ByteArrayInputStream(bytes));
	}

	@Override
	public void save(File file) throws Exception {
		if(file==null || !file.exists()) return;

		//保存到各个存储器中
		List<Storage> storages=this.mapping.getStorageInstances();
		for(int i=0; i<storages.size(); i++){
			try {
				storages.get(i).save(this, file);
			}catch (Exception e){
				throw e;
			}
		}
		//保存到各个存储器中 end
	}

	@Override
	public void save(InputStream is) throws Exception {
		if(is==null) return;

		if(this.mapping==null){
			JDFSFile.save(is, this.physicalPath);
			return;
		}

		if(!(is instanceof ByteArrayInputStream)){
			this.saveToStorages(new ByteArrayInputStream(JUtilInputStream.bytes(is)));
		}else{
			this.saveToStorages((ByteArrayInputStream)is);
		}
	}

	/**
	 *
	 * @param is
	 * @throws Exception
	 */
	private void saveToStorages(ByteArrayInputStream is) throws Exception {
		//保存到各个存储器中
		List<Storage> storages=this.mapping.getStorageInstances();

		for(int i=0; i<storages.size(); i++){
			is.mark(0);
			storages.get(i).save(this, is, false);
			is.reset();
		}

		try{
			is.close();
		}catch (Exception e){}
		//保存到各个存储器中 end
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public InputStream getLocalInputStream() throws Exception{
		File file=new File(this.physicalPath);
		if(!file.exists() || file.isDirectory()) return null;

		return new FileInputStream(file);
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public String getLocalString() throws Exception{
		File file=new File(this.physicalPath);
		if(!file.exists() || file.isDirectory()) return null;

		return JDFSFile.read(file);
	}
}
