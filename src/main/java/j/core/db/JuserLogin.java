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
public class JuserLogin implements Serializable, j.core.type.SelfValidatedObject {

	private java.lang.String uuid;
	private java.lang.String userId;
	private java.lang.String subUserId;
	private java.lang.String userAgentType;
	private java.lang.String userAgentSn;
	private java.lang.String clientId;
	private java.lang.String accessToken;
	private java.lang.String refreshToken;
	private java.lang.String userIp;
	private java.lang.String thirdpartyCode;
	private java.lang.String thirdpartyUserId;
	private java.lang.Long loginTimeTry;
	private java.lang.Long loginTimeOk;
	private java.lang.Long loginTimeAuto;
	private java.lang.String loginStatus;
	private java.lang.String loginMethod;
	private java.lang.Short loginFailedTimes;
	private java.lang.String appidLoginFrom;
	private java.lang.String sessionIdLoginFrom;
	private java.lang.String sessionIdGlobal;

	public java.sql.Timestamp tGetLoginTimeTry(){
		return this.loginTimeTry == null ? null : new java.sql.Timestamp(this.loginTimeTry);
	}
	public void tSetLoginTimeTry(java.sql.Timestamp loginTimeTry){
		this.loginTimeTry=(loginTimeTry == null ? null : loginTimeTry.getTime());
	}

	public java.sql.Timestamp tGetLoginTimeOk(){
		return this.loginTimeOk == null ? null : new java.sql.Timestamp(this.loginTimeOk);
	}
	public void tSetLoginTimeOk(java.sql.Timestamp loginTimeOk){
		this.loginTimeOk=(loginTimeOk == null ? null : loginTimeOk.getTime());
	}

	public java.sql.Timestamp tGetLoginTimeAuto(){
		return this.loginTimeAuto == null ? null : new java.sql.Timestamp(this.loginTimeAuto);
	}
	public void tSetLoginTimeAuto(java.sql.Timestamp loginTimeAuto){
		this.loginTimeAuto=(loginTimeAuto == null ? null : loginTimeAuto.getTime());
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
