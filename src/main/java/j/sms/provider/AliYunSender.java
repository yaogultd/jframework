package j.sms.provider;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import j.log.Logger;
import j.sms.SMSChannel;
import j.sms.SMSSenderConfig;
import j.tool.region.Countries;
import j.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class AliYunSender extends SMSChannel{
	private static Logger log=Logger.create(AliYunSender.class);//日志输出
	private static ConcurrentMap<String, Client> clients = new ConcurrentMap<>();

	/**
	 * 初始化
	 * @param config
	 */
	synchronized private static Client getClient(SMSSenderConfig config) throws Exception{
		if(clients.containsKey(config.getId())) return clients.get(config.getId());

		DefaultProfile profile = DefaultProfile.getProfile("default",
				config.getProperty("AccessKeyID"),
				config.getProperty("AccessKeySecret"));

		// 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考。
		// 建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html。
		com.aliyun.teaopenapi.models.Config apiConfig = new com.aliyun.teaopenapi.models.Config()
				// 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
				.setAccessKeyId(config.getProperty("AccessKeyID"))
				// 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
				.setAccessKeySecret(config.getProperty("AccessKeySecret"));

		// Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
		apiConfig.endpoint = config.getProperty("domain");

		Client client=new com.aliyun.dysmsapi20170525.Client(apiConfig);
		clients.put(config.getId(), client);
		return client;
	}

	@Override
	public boolean reachable(String dest){
		if(dest==null||"".equals(dest)) return false;
		return Countries.isPhoneNumberValid(dest);
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
		try {
			if(!JUtilString.isBlank(template)){
				for(int i=0; texts!=null && i<texts.length; i++){
					template=JUtilString.replaceAll(template, "{"+(i+1)+"}",texts[i]);
				}
			}

			if(!this.reachable(to)){
				log.log("短信发送失败，目标不可达!", -1);
				return true;
			}

			String[] dest=Countries.getPhoneNumberDetail(to);

			SendSmsRequest request = new SendSmsRequest();
			if("86".equals(dest[0])) request.setPhoneNumbers(dest[1]);
			else request.setPhoneNumbers("+"+dest[0]+dest[1]);
			request.setSignName(signature);
			request.setTemplateCode(templateId);

			if(keyValuePairs==null) keyValuePairs=new HashMap<>();

			String content="{";
			if(!keyValuePairs.isEmpty()){
				List keys = JUtilMap.listKeys(keyValuePairs);
				for(int i=0; i<keys.size(); i++){
					String key = (String)keys.get(i);
					String val = keyValuePairs.get(key);
					if(i>0) content+=",";

					if(val.length()>20) {
						log.log("阿里云短信参数["+key+"]长度超过20："+val, -1);
						val=val.substring(0,17)+"...";
					}
					content+="\""+key+"\":\""+JUtilJSON.convertChars(val)+"\"";
				}
			}else{
				for(int i=0; texts!=null && i<texts.length;i++){
					if(i>0) content+=",";

					if(texts[i].length()>20) {
						log.log("阿里云短信参数["+i+"]长度超过20："+texts[i], -1);
						texts[i]=texts[i].substring(0,17)+"...";
					}
					content+="\"p"+(i+1)+"\":\""+JUtilJSON.convertChars(texts[i])+"\"";

					keyValuePairs.put("p"+(i+1), texts[i]);
				}
			}
			content+="}";

			try {
				request.setTemplateParam(JUtilBean.map2Json(keyValuePairs));
				SendSmsResponse response = getClient(config).sendSmsWithOptions(request, new com.aliyun.teautil.models.RuntimeOptions());

				//{"Message":"账户余额不足","RequestId":"6FAFB85E-ED4D-46F8-BDCD-EC1EE9B6F35D","Code":"isv.AMOUNT_NOT_ENOUGH"}
				//{"Message":"OK","RequestId":"8ED2480E-A68E-4F8D-8466-E7427C1768AF","BizId":"856611864655428417^0","Code":"OK"}
				log.log("阿里云短信发送结果:"+JUtilBean.bean2Json(response.getBody()), -1);

				String respCode=response.getBody().getCode();
				return ("OK".equalsIgnoreCase(respCode));
			} catch (Exception e) {
				log.log(e, Logger.LEVEL_ERROR);
			}
			return false;
		}catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
			return false;
		}
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
