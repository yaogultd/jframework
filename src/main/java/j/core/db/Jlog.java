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
public class Jlog implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String eventId;
	private java.lang.String asvrId;
	private java.lang.String asysId;
	private java.lang.String adomain;
	private java.lang.String aurl;
	private java.lang.String auIp;
	private java.lang.String auId;
	private java.lang.String staffId;
	private java.lang.String sellerId;
	private java.lang.String staffIdOfShop;
	private java.lang.String staffId2;
	private java.lang.String sellerId2;
	private java.lang.String staffIdOfShop2;
	private java.lang.String bizCode;
	private java.lang.String bizId;
	private java.lang.String bizName;
	private java.lang.String bizLink;
	private java.lang.String bizIcon;
	private java.lang.String bizData;
	private java.lang.Long eventTime;
	private java.lang.String eventCode;
	private java.lang.String eventData;
	private java.lang.String eventInfluence;
	private java.lang.String eventStat;
	private java.lang.String delBySys;
	private java.lang.String extra0;
	private java.lang.String extra1;
	private java.lang.String extra2;
	private java.lang.String extra3;
	private java.lang.String extra4;
	private java.lang.String extra5;
	private java.lang.String extra6;
	private java.lang.String extra7;
	private java.lang.String extra8;
	private java.lang.String extra9;
	private java.lang.String extra10;
	private java.lang.Long updEventTime;
	private java.lang.String updEventCode;
	private java.lang.String updEventData;
	private java.lang.String updEventInfluence;
	private java.lang.String updEventStat;
	private java.lang.String updStaffId;
	private java.lang.String updSellerId;
	private java.lang.String updStaffIdOfShop;

	public java.sql.Timestamp tGetEventTime(){
		return this.eventTime == null ? null : new java.sql.Timestamp(this.eventTime);
	}
	public void tSetEventTime(java.sql.Timestamp eventTime){
		this.eventTime=(eventTime == null ? null : eventTime.getTime());
	}

	public java.sql.Timestamp tGetUpdEventTime(){
		return this.updEventTime == null ? null : new java.sql.Timestamp(this.updEventTime);
	}
	public void tSetUpdEventTime(java.sql.Timestamp updEventTime){
		this.updEventTime=(updEventTime == null ? null : updEventTime.getTime());
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
