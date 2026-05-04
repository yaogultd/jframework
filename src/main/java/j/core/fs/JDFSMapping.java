package j.core.fs;

import j.core.fs.storage.Storage;
import j.core.fs.storage.StoragePolicy;
import j.core.nvwa.resource.ResourceHelper;
import j.util.ConcurrentList;
import j.util.JUtilString;
import lombok.Getter;

import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
public class JDFSMapping {
	private String selector;
	private String virtualRoot;
	private String physicalRoot;
	private String storagePolicy;
	private ConcurrentList<Storage> storageInstances =null;

	/**
	 *
	 * @param selector
	 * @param virtualRoot
	 * @param physicalRoot
	 * @param storagePolicy
	 */
	protected JDFSMapping(String selector, String virtualRoot, String physicalRoot, String storagePolicy){
		this.selector=selector;
		this.virtualRoot=virtualRoot;
		this.physicalRoot= ResourceHelper.replaceEnvVariables(physicalRoot);
		this.storagePolicy=storagePolicy;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public boolean matches(String path){
		return path.matches(selector);
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public String virtual2Physical(String path){
		String temp=JFile.adjustFileSeperator(path,"linux");
		return JFile.adjustFileSeperator(JUtilString.replaceAll(temp,virtualRoot,physicalRoot),"linux");
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public String physical2Virtual(String path){
		String temp=JFile.adjustFileSeperator(path,"linux");
		return JFile.adjustFileSeperator(JUtilString.replaceAll(temp,physicalRoot,virtualRoot),"linux");
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public String relativePath(String path){
		if(path.startsWith(this.virtualRoot)) path = path.substring(this.virtualRoot.length());
		else if(path.startsWith(this.physicalRoot)) path = path.substring(this.physicalRoot.length());
		else path = ResourceHelper.getRelativePath(path);
		if(path.startsWith("/")) path = path.substring(1);
		return path;
	}

	/**
	 *
	 * @return
	 */
	synchronized public List<Storage> getStorageInstances(){
		if(storageInstances==null) storageInstances=new ConcurrentList<>();
		if(!storageInstances.isEmpty()) return storageInstances;

		//存储策略
		StoragePolicy policy=JDFS.getStoragePolicy(this.getStoragePolicy());
		if(policy==null) return storageInstances;

		List<Storage> storages=policy.getStorages();
		if(storages==null) return storageInstances;

		try{
			for(int i=0; i<storages.size(); i++){
				storageInstances.add(storages.get(i).getInstance(this));
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return storageInstances;
	}

	@Override
	public String toString(){
		StringBuffer s=new StringBuffer();
		s.append("{");
		s.append("\"selector\":\""+this.selector+"\"");
		s.append(",\"virtualRoot\":\""+this.virtualRoot+"\"");
		s.append(",\"physicalRoot\":\""+this.physicalRoot+"\"");
		s.append(",\"storagePolicy\":\""+this.storagePolicy+"\"");
		s.append("}");
		return s.toString();
	}
}
