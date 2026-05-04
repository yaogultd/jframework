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
public class Jip implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.Long ipId;
	private java.lang.Long ipStart;
	private java.lang.Long ipEnd;
	private java.lang.String ipAddr;

	public java.sql.Timestamp tGetIpId(){
		return this.ipId == null ? null : new java.sql.Timestamp(this.ipId);
	}
	public void tSetIpId(java.sql.Timestamp ipId){
		this.ipId=(ipId == null ? null : ipId.getTime());
	}

	public java.sql.Timestamp tGetIpStart(){
		return this.ipStart == null ? null : new java.sql.Timestamp(this.ipStart);
	}
	public void tSetIpStart(java.sql.Timestamp ipStart){
		this.ipStart=(ipStart == null ? null : ipStart.getTime());
	}

	public java.sql.Timestamp tGetIpEnd(){
		return this.ipEnd == null ? null : new java.sql.Timestamp(this.ipEnd);
	}
	public void tSetIpEnd(java.sql.Timestamp ipEnd){
		this.ipEnd=(ipEnd == null ? null : ipEnd.getTime());
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
