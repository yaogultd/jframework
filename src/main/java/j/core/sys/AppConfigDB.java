package j.core.sys;


import j.core.dao.DAO;
import j.core.dao.DB;
import j.core.dao.util.SQLUtil;
import j.core.db.Jparam;
import j.core.nvwa.resource.ResourceHelper;
import j.log.Logger;
import j.util.JUtilDom4j;
import j.util.JUtilString;
import j.util.JUtilUUID;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;

public class AppConfigDB extends AppConfig {
	private static Logger log=Logger.create(AppConfigDB.class);

	/**
	 *
	 */
	protected AppConfigDB(){
		super();
	}

	/**
	 *
	 * @param instanceId
	 */
	protected AppConfigDB(String instanceId){
		super(instanceId);
	}

	@Override
	protected void load() throws Exception{
		synchronized(lock){
			log.log("try load app params from DB......", -1);
			DAO dao=null;
			try{
				dao=DB.connectForTables(DB.getJFrameworkDB().getName(),
						new String[]{"j_param"},
						AppConfigDB.class);

				//将实例id为null的更新为空字符串，以简化操作
				dao.executeSQL("update j_param set instance_id='' where instance_id is null");

				List<Jparam> _params=null;
				if(JUtilString.isBlank(this.instanceId)){
					_params=dao.find("j_param", "instance_id='' order by param_sequence asc");
				}else{
					_params=dao.find("j_param", "instance_id='"+ SQLUtil.deleteCriminalChars(this.instanceId) +"' order by param_sequence asc");
				}
				dao.close();
				dao=null;

				log.log("load "+_params.size()+" app params from DB.", -1);
				for(int i=0; i<_params.size(); i++){
					Jparam param=_params.get(i);

					String groupName=param.getParamGroup();
					String groupDesc="";
					this.groups.put(groupName,new AppParaGroup(groupName,groupDesc));

					String paraName=param.getParamName();
					String paraValue=param.getParamValue();
					String paraDesc=param.getParamDesc();
					boolean canBeUpdated="T".equalsIgnoreCase(param.getParamEditable()) || "1".equalsIgnoreCase(param.getParamEditable());
					String key=groupName+"*"+paraName;
					this.params.put(key, new AppPara(key,
							groupName,
							paraName,
							paraValue,
							paraDesc==null?"":paraDesc,
							canBeUpdated,
							param.getParamSequence()));
				}
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
	public void doSave(){
		synchronized(lock){
			DAO dao=null;
			try{
				dao=DB.connectForTables(DB.getJFrameworkDB().getName(),
						new String[]{"j_param"},
						AppConfigDB.class);

				List<AppPara> paras=this.params.listValues(null);
				for(int j=0;j<paras.size();j++){
					AppPara para=paras.get(j);
					if(!para.getUpdated()) continue;
					para.setUpdated(false);

					Jparam jparam=new Jparam();
					jparam.setInstanceId(this.instanceId);
					jparam.setParamGroup(para.getGroup());
					jparam.setParamName(para.getName());
					jparam.setParamValue(para.getValue());
					jparam.setParamEditable(para.getCanBeUpdated()?"T":"F");
					jparam.setParamDesc(para.getDesc());
					jparam.setParamSequence(para.getSequence());
					if(para.getIsNew()){
						jparam.setUuid(JUtilUUID.genUUID());
						dao.insert(jparam);
					}else{
						dao.updateByKeysIgnoreNulls(jparam, new String[]{"param_group", "param_name"});
					}

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
	public void doSaveToDB() throws Exception{
		this.doSave();
	}

	@Override
	public void doSaveToXML() throws Exception{
		synchronized(lock){
			Document doc= DocumentHelper.createDocument();
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

			String saveInDir = ResourceHelper.configDir();
			log.log("save app params to xml -> "+JUtilString.appendPath(saveInDir, getFileName(this.instanceId)), -1);
			JUtilDom4j.save(doc, JUtilString.appendPath(saveInDir, getFileName(this.instanceId)), "utf-8");
		}
	}
}
