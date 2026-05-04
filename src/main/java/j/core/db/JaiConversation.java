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
public class JaiConversation implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String convId;
	private java.lang.String beingId;
	private java.lang.String convName;
	private java.lang.String convDesc;
	private java.lang.String convConf;
	private java.lang.Long maxIdle;
	private java.lang.Long latestActive;
	private java.lang.String latestProviderId;
	private java.lang.String latestModelId;
	private java.lang.String latestPluginId;
	private java.lang.Integer lifeType;
	private java.lang.Long lifeStart;
	private java.lang.Long lifeEnd;
	private java.lang.String lifeStatus;
	private java.lang.String rowDeleted;

	public java.sql.Timestamp tGetMaxIdle(){
		return this.maxIdle == null ? null : new java.sql.Timestamp(this.maxIdle);
	}
	public void tSetMaxIdle(java.sql.Timestamp maxIdle){
		this.maxIdle=(maxIdle == null ? null : maxIdle.getTime());
	}

	public java.sql.Timestamp tGetLatestActive(){
		return this.latestActive == null ? null : new java.sql.Timestamp(this.latestActive);
	}
	public void tSetLatestActive(java.sql.Timestamp latestActive){
		this.latestActive=(latestActive == null ? null : latestActive.getTime());
	}

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
