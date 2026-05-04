package j.core.fs;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.fs.storage.StoragePolicy;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.dom4j.Document;
import org.dom4j.Element;

import java.sql.Timestamp;
import java.util.List;

@ClassDescription(author = "肖炯",
		date = "2021-08-04",
		description = "文件处理中心，包括可配置的通用文件上传/处理策略、（分布式）存储方案")
public class JDFS implements Consumer{
	//日志
	private static Logger log=Logger.create(JDFS.class);

	//文件存储策略定义
	private static ConcurrentMap<String, StoragePolicy> storagePolicies=new ConcurrentMap<>();

	//虚拟路径-物理路径映射
	private static ConcurrentList<JDFSMapping> mappings=new ConcurrentList<>();

	/**
	 * 
	 *
	 */
	public JDFS() {
		super();
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	public static StoragePolicy getStoragePolicy(String id){
		return JUtilString.isBlank(id)?null:storagePolicies.get(id);
	}

	/**
	 *
	 * @param bizDir
	 * @param time
	 * @return
	 */
	public static String getDir(String bizDir, Timestamp time){
		if(!bizDir.startsWith("/")) bizDir="/"+bizDir;
		if(!bizDir.endsWith("/")) bizDir+="/";
		if(time==null) return bizDir;

		String yymmddhhss=time.toString().substring(0,16);
		yymmddhhss=JUtilString.replaceAll(yymmddhhss,"-","");
		yymmddhhss=JUtilString.replaceAll(yymmddhhss," ","");
		yymmddhhss=JUtilString.replaceAll(yymmddhhss,":","");
		return JUtilString.appendPath(bizDir, yymmddhhss.substring(0,4)+"/"+yymmddhhss.substring(4,8)+"/"+yymmddhhss.substring(8,10)+"/"+yymmddhhss.substring(10,12)+"/");
	}

	/**
	 * 文件后缀名
	 * @param fileName
	 * @return 包含.的文件后缀名
	 */
	public static String getFileExt(String fileName){
		if(JUtilString.isBlank(fileName)
				|| fileName.lastIndexOf(".")<0
				|| fileName.lastIndexOf(".")==fileName.length()-1) return ".unknown";

		return fileName.substring(fileName.lastIndexOf("."));
	}
	
	/**
	 * 根据虚拟路径获得对应的分布式服务及虚拟路径与物理路径映射关系等信息
	 * @param virtualPath
	 * @return
	 */
	public static JDFSMapping mapping(String virtualPath){
		for(int i=0;i<mappings.size();i++){
			JDFSMapping mapping=mappings.get(i);
			if(mapping.matches(virtualPath)) return mapping;
		}
		return null;
	}
	
	/**
	 * 根据虚拟路径获得对应物理路径
	 * @param virtualPath
	 * @return
	 */
	public static String getPhysicalPath(String virtualPath){
		for(int i=0;i<mappings.size();i++){
			JDFSMapping mapping=mappings.get(i);
			if(mapping.matches(virtualPath)) return mapping.virtual2Physical(virtualPath);
		}
		return virtualPath;
	}

	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource){
		try{
			Document document= ((ResourceXml)resource).getResource();
			Element root=document.getRootElement();

			//新版配置（nvwa.xml中的JFS节点）
			if(root.element("JFS")!=null) root=root.element("JFS");

			storagePolicies.clear();
			mappings.clear();

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			//文件存储策略配置
			Element storagePoliciesElement=root.element("storage-policies");
			List<Element> storagePolicyElements=storagePoliciesElement==null?null:storagePoliciesElement.elements("policy");
			for(int i=0; storagePolicyElements!=null && i<storagePolicyElements.size(); i++){
				StoragePolicy policy=new StoragePolicy(storagePolicyElements.get(i));
				storagePolicies.put(policy.getId(), policy);
			}

			//文件映射配置
			Element mappingsElement=root.element("mappings");
			List mappingElements=mappingsElement==null?null:mappingsElement.elements("mapping");
			for(int i=0; mappingElements!=null && i<mappingElements.size(); i++){
				Element mappingElement=(Element)mappingElements.get(i);

				JDFSMapping mapping=new JDFSMapping(mappingElement.attributeValue("selector"),
						mappingElement.attributeValue("virtual-root"),
						mappingElement.attributeValue("physical-root"),
						mappingElement.attributeValue("storage-policy"));

				log.log("load JDFS mapping -> "+mapping, -1);

				mappings.add(mapping);
			}

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_FATAL);
			return false;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)){
			return false;
		}

		//仅处理JFS.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("JFS.xml")
				&&!resource.getPath().endsWith("nvwa.xml")){
			return false;
		}

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource){
		return true;
	}
}
