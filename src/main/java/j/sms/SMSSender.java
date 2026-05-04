package j.sms;

import j.core.Startup;
import j.log.Logger;
import j.core.nvwa.Nvwa;
import j.util.ConcurrentList;
import j.util.JUtilString;
import lombok.Getter;

import java.util.Map;


/**
 * 
 * @author 肖炯
 *
 */
public class SMSSender implements Runnable{	
	private static Logger log=Logger.create(SMSSender.class);//日志输出
	public  final static String CONTENT_HTML="text/html";
	public  final static String CONTENT_TEXT="text/plain";
	public  final static String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	private String id;
	private int num;
	private SMSSenderConfig config;
	private ConcurrentList tasks=new ConcurrentList();

	private SMSChannel channel=null;
	private volatile boolean shutdown=false;
	
	/**
	 * 
	 * @param id
	 * @param num
	 * @param config
	 */
	protected SMSSender(String id,int num,SMSSenderConfig config){
		this.id=id;
		this.num=num;
		this.config=config;
	}
	
	/**
	 * 
	 * @return
	 */
	protected SMSSenderConfig getConfig(){
		return this.config;
	}

	/**
	 *
	 * @return
	 */
	synchronized public SMSChannel getChannel(){
		try{
			if(channel==null){
				channel=(SMSChannel)Class.forName(config.channelImpl).newInstance();
				channel.setConfig(config);
			}
		}catch (Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
		return channel;
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param text
	 * @return
	 */
	public boolean doSend(String to, String signature, String text){
		try {
			this.getChannel();
			return channel.send(to, signature, text);
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
			return false;
		}
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param text
	 * @param filePaths
	 * @return
	 */
	public boolean doSend(String to, String signature, String text, String[] filePaths){
		try {
			this.getChannel();
			return channel.send(to, signature, text, filePaths);
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
			return false;
		}
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param keyValuePairs
	 * @return
	 */
	public boolean doSend(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs) {
		try {
			this.getChannel();
			return channel.sendTemplateSMS(to, signature, templateId, template, texts, keyValuePairs);
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
			return false;
		}
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param keyValuePairs
	 * @param filePaths
	 * @return
	 */
	public boolean doSend(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths){
		try {
			this.getChannel();
			return channel.sendTemplateSMS(to, signature, templateId, template, texts, keyValuePairs, filePaths);
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
			return false;
		}
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param text
	 */
	public void send(String to, String signature, String text){
		tasks.add(new SendSMSTask(to,signature, text, null));
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param text
	 * @param filePaths
	 */
	public void send(String to, String signature, String text, String[] filePaths){
		tasks.add(new SendSMSTask(to,signature, text, filePaths));
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 */
	public void sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts){
		tasks.add(new SendSMSTask(to, signature, templateId, template, texts, null));
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 */
	public void sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs){
		tasks.add(new SendSMSTask(to, signature, templateId, template, texts, keyValuePairs, null));
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param filePaths
	 */
	public void sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, String[] filePaths){
		tasks.add(new SendSMSTask(to, signature, templateId, template, texts, filePaths));
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param keyValuePairs
	 * @param filePaths
	 */
	public void sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths){
		tasks.add(new SendSMSTask(to, signature, templateId, template, texts, keyValuePairs, filePaths));
	}
	
	/**
	 * 
	 *
	 */
	public void shutdown(){
		this.shutdown=true;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(!Startup.isDestroyed()){
			try{
				try{
					Thread.sleep(100);
				}catch(Exception ex){}

				if(Startup.isDestroyed()){
					return;
				}
				
				SendSMSTask task=null;
				if(tasks.size()>0) task=(SendSMSTask)tasks.get(0);
				else if(shutdown) break;
				
				if(task!=null){					
					boolean ok=false;
					if(JUtilString.isBlank(task.text)){//模板消息
						if(task.filePaths==null||task.filePaths.length<1){//无附件
							ok=doSend(task.to, task.signature, task.templateId, task.template, task.texts, task.keyValuePairs);
						}else{
							ok=doSend(task.to, task.signature, task.templateId, task.template, task.texts, task.keyValuePairs, task.filePaths);
						}
					}else{
						if(task.filePaths==null||task.filePaths.length<1){//无附件
							ok=doSend(task.to, task.signature, task.text);
						}else{
							ok=doSend(task.to, task.signature, task.text, task.filePaths);
						}
					}
					if(ok){
						log.log("sms:"+task+" has been sent by thread "+this.id+","+this.num+"!",-1);
						tasks.remove(0);
						task=null;
					}else{
						task.failCount++;
						if(task.failCount>config.maxTries){
							log.log("sms:"+task+" failed to send! "+this.id+","+this.num+"!",-1);
							tasks.remove(0);
							task=null;
						}else{
							log.log("sms:"+task+" has not been sent, to try again! "+this.id+","+this.num+"!",-1);
						}
					}
				}
			}catch(Exception e){
				log.log(e, Logger.LEVEL_ERROR);
				try{
					Thread.sleep(5000);
				}catch(Exception ex){}
			}
		}
	}
}

/**
 * sms发送任务
 */
class SendSMSTask{
	String to;
	String signature;
	String text;
	String templateId;
	String template;
	String[] texts;
	Map<String, String> keyValuePairs;
	String[] filePaths;
	int failCount=0;

	/**
	 *
	 * @param to
	 * @param signature
	 * @param text
	 * @param filePaths
	 */
	public SendSMSTask(String to, String signature, String text, String[] filePaths){
		this.to=to;
		this.signature=signature;
		this.text=text;
		this.filePaths=filePaths;
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param filePaths
	 */
	public SendSMSTask(String to, String signature, String templateId, String template, String[] texts, String[] filePaths){
		this.to=to;
		this.signature=signature;
		this.templateId=templateId;
		this.template=template;
		this.texts=texts;
		this.filePaths=filePaths;
	}

	/**
	 *
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts
	 * @param keyValuePairs
	 * @param filePaths
	 */
	public SendSMSTask(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths){
		this.to=to;
		this.signature=signature;
		this.templateId=templateId;
		this.template=template;
		this.texts=texts;
		this.keyValuePairs=keyValuePairs;
		this.filePaths=filePaths;
	}
	
	@Override
	public String toString(){
		String s="";
		if(texts!=null){
			for(int i=0;i<texts.length;i++){
				s+="{"+texts[i]+"} ";
			}
			s="to "+to+"\r\n"+s;
		}else{
			s="to "+to+"\r\n"+text+"\r\n";
		}
		if(filePaths!=null&&filePaths.length>0){
			for(int i=0;i<filePaths.length;i++){
				s+="["+filePaths[i]+"] ";
			}
		}
		return s;
	}
}
