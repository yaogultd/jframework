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
public class Jtest implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.Long aa;
	private java.lang.String bb;
	private java.sql.Date cc;
	private java.math.BigDecimal dd;
	private java.lang.Double ee;
	private java.lang.Double ff;
	private java.lang.Double gg;
	private java.lang.Integer hh;
	private java.lang.Integer ii;
	private java.io.InputStream jj;
	private java.lang.String kk;
	private java.math.BigDecimal ll;
	private java.lang.Double mm;
	private java.io.InputStream nn;
	private java.lang.Short oo;
	private java.lang.String pp;
	private java.sql.Time qq;
	private java.sql.Timestamp rr;
	private java.lang.Short ss;
	private java.lang.String tt;

	public java.sql.Timestamp tGetAa(){
		return this.aa == null ? null : new java.sql.Timestamp(this.aa);
	}
	public void tSetAa(java.sql.Timestamp aa){
		this.aa=(aa == null ? null : aa.getTime());
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
