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
public class JcryptoToken implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String tokenId;
	private java.lang.String chainCode;
	private java.lang.String tokenAddr;
	private java.lang.String tokenName;
	private java.lang.String tokenDesc;
	private java.lang.String isOfficial;
	private java.lang.String metaData;
	private java.lang.Short valueFractions;
	private java.lang.Long currentPrice;
	private java.lang.String isEnabled;
	private java.lang.Integer showOrder;
	private java.lang.String rowDeleted;

	public java.sql.Timestamp tGetCurrentPrice(){
		return this.currentPrice == null ? null : new java.sql.Timestamp(this.currentPrice);
	}
	public void tSetCurrentPrice(java.sql.Timestamp currentPrice){
		this.currentPrice=(currentPrice == null ? null : currentPrice.getTime());
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
