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
public class JcryptoEntity implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String entityId;
	private java.lang.String parentEntityId;
	private java.lang.String walletId;
	private java.lang.String chainCode;
	private java.lang.String tokenId;
	private java.lang.String theOwner;
	private java.lang.String entityAddr;
	private java.lang.String entityPrikey;
	private java.lang.String entityPubkey;
	private java.lang.String entityMemo;
	private java.lang.String typeOnChain;
	private java.lang.String isExecutable;
	private java.lang.String isReadable;
	private java.lang.String isWritable;
	private java.lang.String assetName;
	private java.lang.String assetDesc;
	private java.lang.String assetAvatar;
	private java.lang.String metaData;
	private java.lang.String derivedFrom;
	private java.lang.String executableCode;
	private java.lang.Long createTime;
	private java.lang.Long destroyedTime;
	private java.lang.Long entityBalance;
	private java.lang.String entityStatus;
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

	public java.sql.Timestamp tGetEntityBalance(){
		return this.entityBalance == null ? null : new java.sql.Timestamp(this.entityBalance);
	}
	public void tSetEntityBalance(java.sql.Timestamp entityBalance){
		this.entityBalance=(entityBalance == null ? null : entityBalance.getTime());
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
