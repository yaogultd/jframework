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
public class Jblacklist implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String blackId;
	private java.lang.String uip;
	private java.lang.String uaddr;
	private java.lang.String blackType;
	private java.lang.Long startTime;
	private java.lang.Long endTime;
	private java.lang.String blackRemark;

	public java.sql.Timestamp tGetStartTime(){
		return this.startTime == null ? null : new java.sql.Timestamp(this.startTime);
	}
	public void tSetStartTime(java.sql.Timestamp startTime){
		this.startTime=(startTime == null ? null : startTime.getTime());
	}

	public java.sql.Timestamp tGetEndTime(){
		return this.endTime == null ? null : new java.sql.Timestamp(this.endTime);
	}
	public void tSetEndTime(java.sql.Timestamp endTime){
		this.endTime=(endTime == null ? null : endTime.getTime());
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
