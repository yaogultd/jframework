package j.sms;

import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.log.Logger;
import j.util.*;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class SMSManager implements Consumer {
	private static Logger log=Logger.create(SMSManager.class);//日志输出
	private static List<SMSSenderConfig> configs=new ArrayList();
	private static ConcurrentMap<String, List<SMSSender>> senders=new ConcurrentMap<>();
	private static SMSSenderSorter sorter=new SMSSenderSorter();

	@FieldDescription(description = "最新配置信息")
	private static String config;

	
	/**
	 * 
	 * @return
	 */
	public static List getSenderConfigs(){
		List _configs=new LinkedList();
		_configs.addAll(configs);
		return _configs;
	}
	
	/**
	 * 
	 * @param business
	 * @return
	 */
	public static List getSenderConfigsOfBusiness(String business){
		if(business==null||"".equals(business)) return getSenderConfigs();
		
		List _configs=new LinkedList();
		for(int i=0;i<configs.size();i++){
			SMSSenderConfig config=configs.get(i);
			if(_configs.equals(config.getBusiness())){
				_configs.add(config);
			}
		}
		return _configs;
	}
	
	/**
	 * 
	 * @param senderId
	 * @return
	 */
	public static SMSSenderConfig getSenderConfig(String senderId){
		for(int i=0;i<configs.size();i++){
			SMSSenderConfig config=configs.get(i);
			if(config.getId().equals(senderId)) return config;
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public static ConcurrentMap getBusinesses(){
		ConcurrentMap businesses=new ConcurrentMap();
		for(int i=0;i<configs.size();i++){
			SMSSenderConfig config=configs.get(i);
			if(!businesses.containsKey(config.getBusiness())){
				businesses.put(config.getBusiness(),new JUtilKeyValue(config.getBusiness(),config.getBusinessName()));
			}
		}
		
		return businesses;
	}
	
	/**
	 * 
	 * @param business
	 * @param senderId
	 * @param to
	 * @return
	 */
	public static boolean reachable(String business,String senderId,String to){
		if(!JUtilString.isBlank(senderId)
				&&senders.containsKey(senderId)){//指定ID
			String region=MobileVerifier.valid(to);
			if(region==null){//匹配不到
				return false;
			}
			
			SMSSenderConfig config=SMSManager.getSenderConfig(senderId);
			if(!JUtilString.contain(config.getRegions(), region)) return false;
			
			List works=senders.get(senderId);
			if(works==null||works.size()==0){
				return false;
			}
			
			return true;
		}else{//自动匹配
			String region=MobileVerifier.valid(to);
			if(region==null){//匹配不到
				return false;
			}
			
			for(int i=0;i<configs.size();i++){
				SMSSenderConfig config=configs.get(i);
				if(!config.business.equals(business)) continue;//不是指定业务的短信通道
				
				if(JUtilString.contain(config.getRegions(), region)){//匹配上地区
					senderId=config.id;
					
					List works=senders.get(senderId);
					
					if(works==null||works.size()==0){
						return false;
					}
					
					return ((SMSSender)works.get(0)).getChannel().reachable(to);
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param business
	 * @param senderId
	 * @param to
	 * @return
	 * @throws Exception
	 */
	private static SMSSender selectSender(String business,String senderId,String to)throws Exception{
		if(!JUtilString.isBlank(senderId)
				&&senders.containsKey(senderId)){//指定ID
			List works=senders.get(senderId);
			
			if(works==null||works.size()==0){
				throw new Exception("no works for sender "+senderId);
			}
			
			return (SMSSender)works.get(JUtilRandom.nextInt(works.size()));
		}else{//自动匹配
			String region=MobileVerifier.valid(to);
			if(region==null){//匹配不到
				throw new Exception("can not find region of mobile "+to);
			}
			
			for(int i=0;i<configs.size();i++){
				SMSSenderConfig config=configs.get(i);
				if(!config.business.equals(business)) continue;//不是指定业务的短信通道
				
				if(JUtilString.contain(config.getRegions(), region)){//匹配上地区
					senderId=config.id;
					
					List works=senders.get(senderId);
					
					if(works==null||works.size()==0){
						throw new Exception("no works for sender("+senderId+",auto match),business - "+business+", mobile - "+to);
					}
					
					//log.log(senderId+" matches business - "+business+",mobile - "+to,-1);
					
					return (SMSSender)works.get(JUtilRandom.nextInt(works.size()));
				}
			}
			
			//未匹配到相应地区，尝试查找region设定为OTHER的发送通道
			region="OTHER";
			for(int i=0;i<configs.size();i++){
				SMSSenderConfig config=configs.get(i);
				if(!config.business.equals(business)) continue;//不是指定业务的短信通道
				
				if(JUtilString.contain(config.getRegions(), region)){//匹配上地区
					senderId=config.id;
					
					List works=senders.get(senderId);
					
					if(works==null||works.size()==0){
						throw new Exception("no works for sender("+senderId+",auto match),business - "+business+", mobile - "+to);
					}
					
					log.log(senderId+" matches business - "+business+",mobile - "+to,-1);
					
					return (SMSSender)works.get(JUtilRandom.nextInt(works.size()));
				}
			}
		}
		
		return null;
	}

	/**
	 *
	 * @param business
	 * @param senderId
	 * @param to
	 * @param signature
	 * @param text
	 * @throws Exception
	 */
	public static boolean send(String business,String senderId,String to, String signature, String text) throws Exception{
		SMSSender sender=selectSender(business,senderId,to);
		if(sender==null){
			throw new Exception("no sender matches,business - "+business+",senderId - "+senderId+",to - "+to);
		}
		if(!sender.getChannel().reachable(to)) return false;

		sender.send(to, signature, text);
		return true;
	}

	/**
	 *
	 * @param business
	 * @param senderId
	 * @param to
	 * @param signature
	 * @param text
	 * @param filePaths
	 * @throws Exception
	 */
	public static boolean send(String business,String senderId,String to, String signature, String text, String[] filePaths) throws Exception{
		SMSSender sender=selectSender(business,senderId,to);
		if(sender==null){
			throw new Exception("no sender matches,business - "+business+",senderId - "+senderId+",to - "+to);
		}
		if(!sender.getChannel().reachable(to)) return false;

		sender.send(to, signature, text, filePaths);
		return true;
	}

	/**
	 *
	 * @param business
	 * @param senderId
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @throws Exception
	 */
	public static boolean sendTemplateSMS(String business,String senderId,String to, String signature, String templateId, String template, String[] texts) throws Exception{
		SMSSender sender=selectSender(business,senderId,to);
		if(sender==null){
			throw new Exception("no sender matches,business - "+business+",senderId - "+senderId+",to - "+to);
		}
		if(!sender.getChannel().reachable(to)) return false;

		sender.sendTemplateSMS(to,signature,templateId,template,texts);
		return true;
	}

	/**
	 *
	 * @param business
	 * @param senderId
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param keyValuePairs
	 * @throws Exception
	 */
	public static boolean sendTemplateSMS(String business,String senderId,String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs) throws Exception{
		SMSSender sender=selectSender(business,senderId,to);
		if(sender==null){
			throw new Exception("no sender matches,business - "+business+",senderId - "+senderId+",to - "+to);
		}
		if(!sender.getChannel().reachable(to)) return false;

		sender.sendTemplateSMS(to,signature,templateId,template,texts,keyValuePairs);
		return true;
	}

	/**
	 *
	 * @param business
	 * @param senderId
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param filePaths
	 * @throws Exception
	 */
	public static boolean sendTemplateSMS(String business,String senderId,String to, String signature, String templateId, String template, String[] texts, String[] filePaths) throws Exception{
		SMSSender sender=selectSender(business,senderId,to);
		if(sender==null){
			throw new Exception("no sender matches,business - "+business+",senderId - "+senderId+",to - "+to);
		}
		if(!sender.getChannel().reachable(to)) return false;

		sender.sendTemplateSMS(to,signature,templateId,template,texts,filePaths);
		return true;
	}

	/**
	 *
	 * @param business
	 * @param senderId
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param keyValuePairs
	 * @param filePaths
	 * @throws Exception
	 */
	public static boolean sendTemplateSMS(String business,String senderId,String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths) throws Exception{
		SMSSender sender=selectSender(business,senderId,to);
		if(sender==null){
			throw new Exception("no sender matches,business - "+business+",senderId - "+senderId+",to - "+to);
		}
		if(!sender.getChannel().reachable(to)) return false;

		sender.sendTemplateSMS(to,signature,templateId,template,texts,keyValuePairs,filePaths);
		return true;
	}
	
	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置信息")
	public static boolean load(Resource resource){
		try{
			Document document= ((ResourceXml)resource).getResource();
			Element root=document.getRootElement();

			//新版配置（nvwa.xml中的SMS节点）
			if(root.element("SMS")!=null){
				root=root.element("SMS");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			List values=senders.listValues();
			for(int i=0;i<values.size();i++){
				List works=(List)values.get(i);
				for(int j=0;j<works.size();j++){
					SMSSender sender=(SMSSender)works.get(j);
					sender.shutdown();
				}
			}
			senders.clear();
			configs.clear();

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}
			
			Element verifierEle=root.element("mobile-verifier");
			if(verifierEle!=null){
				List verifierRules=verifierEle.elements("rule");
				
				MobileVerifier.rules=new MobileVerifierRule[verifierRules.size()];
				for(int i=0;i<verifierRules.size();i++){
					Element ruleEle=(Element)verifierRules.get(i);
					MobileVerifierRule rule=new MobileVerifierRule(ruleEle.attributeValue("region"),ruleEle.getTextTrim());
					MobileVerifier.rules[i]=rule;
				}
			}
			
			List senderEles=root.elements("sender");
			for(int i=0;i<senderEles.size();i++){
				Element senderEle=(Element)senderEles.get(i);
				
				SMSSenderConfig config=new SMSSenderConfig();
				config.id=senderEle.elementText("id");
				config.priority=Integer.parseInt(senderEle.elementText("priority"));
				config.setRegionString(senderEle.elementText("region"));
				config.business=senderEle.elementText("business");
				config.businessName=senderEle.elementText("business-name");
				config.channelImpl=senderEle.elementText("channel-impl");
				config.from=senderEle.elementText("from");
				config.fromName=senderEle.elementText("from-name");
				config.threads=Integer.parseInt(senderEle.elementText("threads"));
				config.maxTries=Integer.parseInt(senderEle.elementText("max-tries"));

				List<Element> ps=senderEle.elements("property");
				for(int j=0; ps!=null && j<ps.size(); j++){
					Element p=ps.get(j);
					config.setProperty(p.attributeValue("name"), p.getTextTrim());
				}
				
				configs.add(config);
			}
			configs=sorter.bubble(configs, JUtilSorter.DESC);
			
			for(int i=0;i<configs.size();i++){
				SMSSenderConfig config=configs.get(i);
				int threads=config.threads;
				
				List works=new LinkedList();
				for(int t=0;t<threads;t++){
					SMSSender sender=new SMSSender(config.id,t,config);
					Thread thread=new Thread(sender);
					thread.start();
					log.log("sms sender "+config.id+","+t+" started.",-1);
					
					works.add(sender);
				}
				
				senders.put(config.id,works);
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
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理sms.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("sms.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource){
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理sms.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("sms.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}
}