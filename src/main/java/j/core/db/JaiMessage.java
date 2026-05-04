/*
 * Created on 2026-04-26
 *
 */
package j.core.db;


import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class JaiMessage implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String messageId;
	private java.lang.String beingId;
	private java.lang.String convId;
	private java.lang.Integer convType;
	private java.lang.String providerId;
	private java.lang.String modelId;
	private java.lang.String pluginId;
	private java.lang.String interactionId;
	private java.lang.String messageWho;
	private java.lang.Long messageTime;
	private java.lang.Integer messageType;
	private java.lang.String messageContent;
	private java.lang.String storeType;
	private java.lang.String extraDatas;
	private java.lang.String extraTexts;
	private java.lang.String isChatStart;
	private java.lang.String isChatEnd;
	private java.lang.String messageIp;
	private java.lang.Integer messageTokens;
	private java.lang.String rowDeleted;

	public java.sql.Timestamp tGetMessageTime(){
		return this.messageTime == null ? null : new java.sql.Timestamp(this.messageTime);
	}
	public void tSetMessageTime(java.sql.Timestamp messageTime){
		this.messageTime=(messageTime == null ? null : messageTime.getTime());
	}

	public j.core.type.Result isValid(){
		return new j.core.type.Result(true, "1", "");
	}

	public void fromJson(org.json.JSONObject json) throws Exception{
		j.util.JUtilBean.json2Bean(this, json);
	}

	public String toString(){
		return j.util.JUtilBean.bean2Json(this);
	}

	public String toString(java.util.List<String> excludeColumns){
		return j.util.JUtilBean.bean2Json(this, false, null, excludeColumns);
	}

}
