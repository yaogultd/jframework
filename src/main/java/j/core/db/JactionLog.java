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
public class JactionLog implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String eventId;
	private java.lang.String asvrId;
	private java.lang.String asysId;
	private java.lang.String adomain;
	private java.lang.String aurl;
	private java.lang.String auIp;
	private java.lang.String auId;
	private java.lang.String actionHandler;
	private java.lang.String actionId;
	private java.lang.String actionParameters;
	private java.lang.String actionResult;
	private java.lang.String eventStat;
	private java.lang.Long eventTime;
	private java.lang.String delBySys;

	public java.sql.Timestamp tGetEventTime(){
		return this.eventTime == null ? null : new java.sql.Timestamp(this.eventTime);
	}
	public void tSetEventTime(java.sql.Timestamp eventTime){
		this.eventTime=(eventTime == null ? null : eventTime.getTime());
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
