package j.core.sys;


import j.core.common.JArray;
import j.core.common.JProperties;
import j.core.dao.DAO;
import j.core.dao.DB;
import j.core.db.Jparam;
import j.core.nvwa.Nvwa;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceHelper;
import j.core.nvwa.resource.ResourceXml;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilDom4j;
import j.util.JUtilString;
import j.util.JUtilUUID;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppConfigXML extends AppConfig {
	private static Logger log=Logger.create(AppConfigXML.class);

	/**
	 *
	 */
	protected AppConfigXML(){
		super();
	}

	/**
	 *
	 * @param instanceId
	 */
	protected AppConfigXML(String instanceId){
		super(instanceId);
	}

	@Override
	protected void load() throws Exception{
		synchronized(lock){
			while(!Nvwa.isScanned()){
				log.log("waiting for resources......", -1);
				try{
					Thread.sleep(1000);
				}catch (Exception e){}
			}
			List<String> paths=new ArrayList<>();

			String saveInDir=ResourceHelper.configDir();
			log.log("config dir -> "+saveInDir, -1);

			if((new File(saveInDir)).exists()) paths.add(saveInDir);
			paths.add(ResourceHelper.getRelativePath(JProperties.getConfigPath()));
			log.log("config dirs -> "+ JArray.toString(paths, ","), -1);

			ConcurrentMap<String, Resource> resources = Nvwa.getResources(paths, new String[]{".xml"});

			String expectedPath="/"+getFileName(this.instanceId);
			log.log("app config file's path expected -> "+ expectedPath, -1);

			List<Resource> _resources=resources.listValues();
			for(int i=0; i<_resources.size(); i++){
				Resource xml=_resources.get(i);
				//log.log("resource -> "+xml.getClass().getCanonicalName()+", "+xml.getName()+", "+xml.getPath(), -1);

				if(!(xml instanceof ResourceXml)) continue;
				if(!xml.getPath().endsWith(expectedPath)) continue;

				//如果是jar包中的资源，查找外部目录中是否有对应资源（修改过的），如有则不予处理
				if(xml.isInJar() && externalResourceExists(_resources, xml.getName())){
					log.log("resource "+xml.getPath() +" exists in external directory, so ignored.", -1);
					continue;
				}

				log.log("loading parameters of application from -> "+xml.getPath(),-1);
				loadXML(((ResourceXml)xml).getResource());
			}
		}
	}

	/**
	 * 解析xml文件中的参数
	 * @param doc
	 * @throws Exception
	 */
	private void loadXML(Document doc) throws Exception{
		Element root = doc.getRootElement();
		List appElements=root.elements("group");
		for(int i=0;appElements!=null&&i<appElements.size();i++){
			Element app=(Element)appElements.get(i);

			String groupName=app.attributeValue("name");
			String groupDesc=app.attributeValue("desc");
			this.groups.put(groupName,new AppParaGroup(groupName,groupDesc));

			List paraElements=app.elements("para");
			for(int j=0;paraElements!=null&&j<paraElements.size();j++){
				Element para=(Element)paraElements.get(j);
				String paraName=para.attributeValue("name");
				String paraValue=para.getTextTrim();
				String paraDesc=para.attributeValue("desc");
				boolean canBeUpdated=!"false".equals(para.attributeValue("can-be-updated"));

				String key=groupName+"*"+paraName;
				this.params.put(key,new AppPara(key,
						groupName,
						paraName,
						paraValue,
						paraDesc==null?"":paraDesc,
						canBeUpdated,
						j));
				log.log("app para "+key+" = "+paraValue, -1);
			}
		}
	}

	/**
	 *
	 * @param resources
	 * @param name
	 * @return
	 */
	private static boolean externalResourceExists(List<Resource> resources, String name){
		for(int i=0; i<resources.size(); i++) {
			Resource resource = resources.get(i);
			if(!resource.isInJar() && resource.getName().equals(name)) return true;
		}
		return false;
	}

	@Override
	public void doSave() throws Exception{
		synchronized(lock){
			Document doc=DocumentHelper.createDocument();
			Element root=doc.addElement("root");

			//参数分组
			List<AppParaGroup> _groups=this.groups.listValues(null);

			for(int i=0;i<_groups.size();i++){
				AppParaGroup group=_groups.get(i);

				Element app=root.addElement("group");
				app.addAttribute("name",group.getName());
				app.addAttribute("desc",group.getDesc());

				//分组内参数
				List<AppPara> paras=AppConfig.getParas(this.instanceId, group.getName());
				for(int j=0;j<paras.size();j++){
					AppPara para=paras.get(j);
					para.setUpdated(false);

					Element p=app.addElement("para");
					p.addAttribute("name", para.getName());
					p.addAttribute("desc", para.getDesc());
					p.addAttribute("can-be-updated", para.getCanBeUpdated()?"true":"false");
					p.setText(para.getValue());
				}
			}

			String saveInDir=ResourceHelper.configDirExternal();
			JUtilDom4j.save(doc, JUtilString.appendPath(saveInDir, getFileName(this.instanceId)), "utf-8");
		}
	}


	@Override
	public void doSaveToDB() throws Exception{
		synchronized(lock){
			if(!DB.getEnabled()) return;//未启用数据库
			DAO dao=null;
			try{
				dao=DB.connectForTables(DB.getJFrameworkDB().getName(),
						new String[]{"j_param"},
						AppConfigXML.class);

				List<AppPara> paras=this.params.listValues(null);
				for(int j=0;j<paras.size();j++){
					AppPara para=paras.get(j);
					para.setUpdated(false);

					Jparam jparam=new Jparam();
					jparam.setInstanceId(this.instanceId);
					jparam.setParamGroup(para.getGroup());
					jparam.setParamName(para.getName());
					jparam.setParamValue(para.getValue());
					jparam.setParamEditable(para.getCanBeUpdated()?"T":"F");
					jparam.setParamDesc(para.getDesc());
					jparam.setParamSequence(para.getSequence());
					jparam.setUuid(JUtilUUID.genUUID());
					dao.insert(jparam);

					para.setIsNew(false);
				}

				dao.close();
				dao=null;
			}catch(Exception e){
				if(dao!=null){
					try{
						dao.close();
						dao=null;
					}catch(Exception ex){}
				}
				log.log(e, Logger.LEVEL_ERROR);
			}
		}
	}


	@Override
	public void doSaveToXML() throws Exception{
		this.doSave();
	}
}
