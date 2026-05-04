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
public class JfsTask implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String uuid;
	private java.lang.Long taskTime;
	private java.lang.String fromUuid;
	private java.lang.String toUuid;
	private java.lang.String filePath;
	private java.lang.String sourcePath;
	private java.lang.String taskOperation;
	private java.lang.Integer synTimes;
	private java.lang.Long synTime;

	public java.sql.Timestamp tGetTaskTime(){
		return this.taskTime == null ? null : new java.sql.Timestamp(this.taskTime);
	}
	public void tSetTaskTime(java.sql.Timestamp taskTime){
		this.taskTime=(taskTime == null ? null : taskTime.getTime());
	}

	public java.sql.Timestamp tGetSynTime(){
		return this.synTime == null ? null : new java.sql.Timestamp(this.synTime);
	}
	public void tSetSynTime(java.sql.Timestamp synTime){
		this.synTime=(synTime == null ? null : synTime.getTime());
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
