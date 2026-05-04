package j.mail;

import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.log.Logger;
import j.core.nvwa.Nvwa;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.util.ConcurrentMap;
import j.util.JUtilRandom;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.activation.MimetypesFileTypeMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
public class MailManager implements Consumer {
	private static Logger log=Logger.create(MailManager.class);//日志输出
	private static ConcurrentMap configs=new ConcurrentMap();
	private static ConcurrentMap senders=new ConcurrentMap();
	private static ConcurrentMap readers=new ConcurrentMap();

	static {
		final MimetypesFileTypeMap mimetypes = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
		mimetypes.addMimeTypes("text/calendar ics ICS");

		final MailcapCommandMap mc = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
		mc.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
	}

	@FieldDescription(description = "最新配置信息")
	private static String config;

	/**
	 * 
	 * @param senderId
	 * @return
	 */
	public static MailSenderConfig getConfig(String senderId){
		return (MailSenderConfig)configs.get(senderId);
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getSenders(){
		return senders.listValues();
	}
	
	/**
	 * 
	 * @param senderId
	 * @return
	 */
	public static MailReader getReader(String senderId){
		try{
			MailSenderConfig config=getConfig(senderId);
			if(config==null) return null;
			
			if(config.getReaderManager()==null||"".equals(config.getReaderManager())){
				return new MailReader(senderId);
			}else{
				MailReader reader=(MailReader)Class.forName(config.getReaderManager()).newInstance();
				reader.setSenderId(senderId);
				
				return reader;
			}
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return null;
		}
	}
	
	/**
	 * 
	 * @param senderId 发送器ID
	 * @param to 收件人
	 * @param cc 抄送
	 * @param subj 标题
	 * @param text 正文
	 * @param type 邮件正文内容类型，text/html或text/plain
	 * @param encoding 邮件正文内容字符编码
	 * @param filePaths 邮件附件的文件路径[]
	 * @throws Exception
	 */
	public static void send(String senderId,String to, String cc, String subj, String text, String type, String encoding, String[] filePaths) throws Exception{
		List works=(List)senders.get(senderId);
		
		if(works==null||works.size()==0){
			throw new Exception("no mail sender of id - "+senderId);
		}
		
		MailSender sender=(MailSender)works.get(JUtilRandom.nextInt(works.size()));
		sender.send(to,cc,subj,text,type,encoding,filePaths);
	}
	
	/**
	 * 
	 * @param senderId
	 * @param to
	 * @param cc
	 * @param subj
	 * @param text
	 * @param type
	 * @param encoding
	 * @param filePaths
	 * @param fromName
	 * @throws Exception
	 */
	public static void send(String senderId,String to, String cc, String subj, String text, String type, String encoding, String[] filePaths,String fromName) throws Exception{
		List works=(List)senders.get(senderId);
		
		if(works==null||works.size()==0){
			throw new Exception("no mail sender of id - "+senderId);
		}
		
		MailSender sender=(MailSender)works.get(JUtilRandom.nextInt(works.size()));
		sender.send(to,cc,subj,text,type,encoding,filePaths,fromName);
	}
	
	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource){
		try{
			Document document= ((ResourceXml)resource).getResource();
			Element root=document.getRootElement();

			//新版配置（nvwa.xml中的MAIL节点）
			if(root.element("MAIL")!=null){
				root=root.element("MAIL");
			}

			String asXml=root.asXML();
			if(asXml.equals(config)) return false;
			else config=asXml;

			//停止已有邮件接收线程
			List _readers=readers.listValues();
			for(int i=0;i<_readers.size();i++){
				MailReaderThread reader=(MailReaderThread)_readers.get(i);
				if(reader.isBlocked()){
					MailSenderConfig config=MailManager.getConfig(reader.getSenderId());

					log.log("shutdown mail reader "+config.getId()+","+config.getDesc()+" because blocking.",-1);
					reader.shutdown();

					reader=new MailReaderThread(config.getId());
					Thread thread=new Thread(reader);
					thread.start();

					readers.put(config.getId(),reader);

					log.log("new mail reader "+config.getId()+","+config.getDesc()+" started after shutdown the blocked.",-1);
				}
			}

			//停止已有邮件发送线程
			List values=senders.listValues();
			for(int i=0;i<values.size();i++){
				List ss=(List)values.get(i);
				for(int j=0;j<ss.size();j++){
					MailSender sender=(MailSender)ss.get(j);
					sender.shutdown();
				}
			}

			senders.clear();
			configs.clear();

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}
			
			List senderEles=root.elements("sender");
			for(int i=0;i<senderEles.size();i++){
				Element senderEle=(Element)senderEles.get(i);
				int threads=Integer.parseInt(senderEle.elementText("threads"));
				
				MailSenderConfig config=new MailSenderConfig();
				config.setId(senderEle.elementText("id"));
				config.setDesc(senderEle.attributeValue("desc"));
				config.setHost(senderEle.elementText("host"));
				config.setPort(senderEle.elementText("port"));
				config.setUser(senderEle.elementText("user"));
				config.setSender(senderEle.elementText("sender"));
				config.setReaderProtocol(senderEle.elementText("reader-protocol"));
				config.setReaderHost(senderEle.elementText("reader-host"));
				config.setReaderPort(senderEle.elementText("reader-port"));
				config.setReaderManager(senderEle.elementText("reader-manager"));
				config.setReaderThread(senderEle.elementText("reader-thread"));
				config.setReaderInterval(senderEle.elementText("reader-interval"));
				config.setReaderCount(senderEle.elementText("reader-count"));
				config.setReaderFlagRead(senderEle.elementText("reader-flag-read"));
				config.setReaderFolder(senderEle.elementText("reader-folder"));
				config.setReaderName(senderEle.elementText("reader-name"));
				config.setReaderVersion(senderEle.elementText("reader-version"));
				config.setPassword(senderEle.elementText("password"));
				config.setAuthCode(senderEle.elementText("auth-code"));
				config.setFrom(senderEle.elementText("from"));
				config.setFromName(senderEle.elementText("from-name"));
				config.setSecure("true".equalsIgnoreCase(senderEle.elementText("secure")));
				config.setTls("true".equalsIgnoreCase(senderEle.elementText("tls")));
				config.setProtocols(senderEle.elementText("protocols"));
				config.setMaxTries(Integer.parseInt(senderEle.elementText("max-tries")));
				
				List params=senderEle.elements("param");
				for(int p=0;p<params.size();p++){
					Element paramE=(Element)params.get(p);
					config.setParam(paramE.attributeValue("key"),paramE.attributeValue("value"));
				}
				
				configs.put(config.getId(),config);
				
				//启动邮件发送线程
				List works=new LinkedList();
				for(int t=0;t<threads;t++){
					MailSender sender=(MailSender) Class.forName(config.getSender()).newInstance();
					sender.setConfig(senderEle.elementText("id"),t,config);

					Thread thread=new Thread(sender);
					thread.start();
					log.log("mail sender(supplier:"+config.getSender()+") "+senderEle.elementText("id")+","+t+" started.",-1);
					
					works.add(sender);
				}
				
				senders.put(senderEle.elementText("id"),works);
				
				//启动邮件收取线程
				if(config.getReaderThread()>0){
					MailReaderThread reader=(MailReaderThread)readers.get(config.getId());
					if(reader!=null){
						reader.shutdown();
						log.log("shutdown mail reader "+config.getId()+","+config.getDesc()+".",-1);
					}
					
					reader=new MailReaderThread(config.getId());
					Thread thread=new Thread(reader);
					thread.start();
					
					readers.put(config.getId(),reader);
					
					log.log("mail reader "+config.getId()+","+config.getDesc()+" started.",-1);
				}
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

		//仅处理mail.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("mail.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource){
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理mail.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("mail.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	public static void main(String[] args) throws Exception{
		System.out.println("start...........");
		Nvwa.startup();
		try{
			Thread.sleep(5000);
		}catch(Exception e){}
		MailManager.send("verify", "songcmend@163.com", null, "欢迎光临", "Hello, world!", MailSender.CONTENT_HTML, "UTF-8", null);
	}
}