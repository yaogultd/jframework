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
public class JcryptoTransaction implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String tranId;
	private java.lang.String walletId;
	private java.lang.String entityId;
	private java.lang.String theOwner;
	private java.lang.String tranWith;
	private java.lang.String tranData;
	private java.lang.Long tranAmount;
	private java.lang.Long balanceBefore;
	private java.lang.Long balanceAfter;
	private java.lang.Long createTime;
	private java.lang.Long destroyedTime;
	private java.lang.String tranStatus;
	private java.lang.String rowDeleted;

	public java.sql.Timestamp tGetTranAmount(){
		return this.tranAmount == null ? null : new java.sql.Timestamp(this.tranAmount);
	}
	public void tSetTranAmount(java.sql.Timestamp tranAmount){
		this.tranAmount=(tranAmount == null ? null : tranAmount.getTime());
	}

	public java.sql.Timestamp tGetBalanceBefore(){
		return this.balanceBefore == null ? null : new java.sql.Timestamp(this.balanceBefore);
	}
	public void tSetBalanceBefore(java.sql.Timestamp balanceBefore){
		this.balanceBefore=(balanceBefore == null ? null : balanceBefore.getTime());
	}

	public java.sql.Timestamp tGetBalanceAfter(){
		return this.balanceAfter == null ? null : new java.sql.Timestamp(this.balanceAfter);
	}
	public void tSetBalanceAfter(java.sql.Timestamp balanceAfter){
		this.balanceAfter=(balanceAfter == null ? null : balanceAfter.getTime());
	}

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
