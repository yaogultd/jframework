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
public class Jparam implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String uuid;
	private java.lang.String instanceId;
	private java.lang.String paramGroup;
	private java.lang.String paramName;
	private java.lang.String paramValue;
	private java.lang.String paramEditable;
	private java.lang.String paramDesc;
	private java.lang.Integer paramSequence;

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
