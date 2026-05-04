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
public class Jcontinent implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String continentId;
	private java.lang.String continentCode;
	private java.lang.String continentName;
	private java.lang.String continentNameTw;
	private java.lang.String continentNameEn;
	private java.lang.String isAvail;

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
