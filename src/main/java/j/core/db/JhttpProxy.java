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
public class JhttpProxy implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String uuid;
	private java.lang.String poolId;
	private java.lang.String proxyIp;
	private java.lang.String proxyIpv6;
	private java.lang.Integer proxyPort;
	private java.lang.String proxyUsername;
	private java.lang.String proxyPassword;
	private java.lang.String httpsSupported;
	private java.lang.String continentId;
	private java.lang.String countryId;
	private java.lang.String provinceId;
	private java.lang.String proxyDesc;
	private java.lang.Long lifeStart;
	private java.lang.Long lifeEnd;
	private java.lang.String rowDeleted;

	public java.sql.Timestamp tGetLifeStart(){
		return this.lifeStart == null ? null : new java.sql.Timestamp(this.lifeStart);
	}
	public void tSetLifeStart(java.sql.Timestamp lifeStart){
		this.lifeStart=(lifeStart == null ? null : lifeStart.getTime());
	}

	public java.sql.Timestamp tGetLifeEnd(){
		return this.lifeEnd == null ? null : new java.sql.Timestamp(this.lifeEnd);
	}
	public void tSetLifeEnd(java.sql.Timestamp lifeEnd){
		this.lifeEnd=(lifeEnd == null ? null : lifeEnd.getTime());
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
