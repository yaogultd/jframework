package j.sms.provider;

import j.log.Logger;
import j.sms.SMSChannel;

import java.util.Map;

public class NotSupported extends SMSChannel{
	private static Logger log=Logger.create(NotSupported.class);//日志输出

	@Override
	public boolean reachable(String dest){
		return false;
	}

	@Override
	public boolean send(String to, String signature, String text) throws Exception{
		return sendTemplateSMS(to, signature, null, text, null);
	}

	@Override
	public boolean send(String to, String signature, String text, String[] filePaths) throws Exception{
		throw new Exception("不支持彩信");
	}

	@Override
	public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts) throws Exception{
		return sendTemplateSMS(to, signature, templateId, template, texts, (Map)null);
	}

	@Override
	public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs) throws Exception{
		log.log("暂不支持向您手机号所在国家/地区发送短信", -1);
		return false;
	}

	@Override
	public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, String[] filePaths) throws Exception{
		throw new Exception("不支持彩信");
	}

	@Override
	public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths) throws Exception {
		throw new Exception("不支持彩信");
	}
}