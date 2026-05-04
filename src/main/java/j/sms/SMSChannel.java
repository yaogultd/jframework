package j.sms;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 
 * @author 肖炯
 * 必须是线程安全的
 *
 */
public abstract class SMSChannel {
	@Setter
	@Getter
	protected SMSSenderConfig config;

	/**
	 * 是否可送到（该短信通道是否可给某号码发送短信）
	 * @param dest 目标号码
	 * @return
	 */
	public abstract boolean reachable(String dest);

	/**
	 * 发送短信
	 * @param to
	 * @param signature
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public abstract boolean send(String to, String signature, String text) throws Exception;

	/**
	 * 发送彩信（包含文件）
	 * @param to
	 * @param signature
	 * @param text
	 * @param filePaths
	 * @return
	 * @throws Exception
	 */
	public abstract boolean send(String to, String signature, String text, String[] filePaths) throws Exception;

	/**
	 * 发送短信(适用于模板短信)
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts 短信模板中的参数值，按参数出现位置顺序排列
	 * @return
	 * @throws Exception
	 */
	public abstract boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts) throws Exception;

	/**
	 * 发送短信(适用于模板短信)
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts 短信模板中的参数值，按参数出现位置顺序排列
	 * @param keyValuePairs 短信模板中的参数名-参数值对
	 * @return
	 * @throws Exception
	 */
	public abstract boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs) throws Exception;


	/**
	 * 发送彩信(适用于模板短信)
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts 短信模板中的参数值，按参数出现位置顺序排列
	 * @param filePaths
	 * @return
	 * @throws Exception
	 */
	public abstract boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, String[] filePaths) throws Exception;


	/**
	 * 发送彩信(适用于模板短信)
	 * @param to
	 * @param signature
	 * @param templateId
	 * @param template
	 * @param texts 短信模板中的参数值，按参数出现位置顺序排列
	 * @param keyValuePairs 短信模板中的参数名-参数值对
	 * @param filePaths
	 * @return
	 * @throws Exception
	 */
	public abstract boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths) throws Exception;
}
