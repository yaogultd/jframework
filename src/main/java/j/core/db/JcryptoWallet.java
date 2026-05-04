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
public class JcryptoWallet implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String walletId;
	private java.lang.String theOwner;
	private java.lang.String walletName;
	private java.lang.String walletDesc;
	private java.lang.String walletAvatar;
	private java.lang.String metaData;
	private java.lang.Long createTime;
	private java.lang.Long destroyedTime;
	private java.lang.String walletStatus;
	private java.lang.String rowDeleted;

	public java.sql.Timestamp tGetCreateTime(){
		return this.createTime == null ? null : new java.sql.Timestamp(this.createTime);
	}
	public void tSetCreateTime(java.sql.Timestamp createTime){
		this.createTime=(createTime == null ? null : createTime.getTime());
	}

	public java.sql.Timestamp tGetDestroyedTime(){
		return this.destroyedTime == null ? null : new java.sql.Timestamp(this.destroyedTime);
	}
	public void tSetDestroyedTime(java.sql.Timestamp destroyedTime){
		this.destroyedTime=(destroyedTime == null ? null : destroyedTime.getTime());
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
