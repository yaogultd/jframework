package j.core.web;


/**
 *
 */
public class Constants {
	public static final String SSO_SYS_ID="sso_sys_id";
	public static final String SSO_MACHINE_ID="sso_machine_id";
	public static final String SSO_LOGIN_FROM_SYS_ID="sso_login_from";
	public static final String SSO_USER_TYPE="sso_user_type";
	public static final String SSO_USER_ID="sso_user_id";
	public static final String SSO_SUB_USER_ID="sso_sub_user_id";
	public static final String SSO_INCLUDE_SUB_USERS="sso_include_sub_users";
	public static final String SSO_USER_NAME="sso_user_name";
	public static final String SSO_USER_ROLE="sso_user_role";
	public static final String SSO_USER_ROLES="sso_user_roles";
	public static final String SSO_WEBSITE="sso_website";
	public static final String SSO_USER_IP="sso_user_ip";
	public static final String SSO_USER_IS_TIMEOUT="sso_user_is_timeout";
	public static final String SSO_USER_PWD="sso_user_pwd";
	public static final String SSO_USER_DOMAIN="sso_user_domain";
	public static final String SSO_VERIFIER_UUID="sso_verifier_uuid";
	public static final String SSO_VERIFIER_CODE="sso_verifier_code";
	public static final String SSO_VERIFIER_SN="sso_verifier_sn";
	public static final String SSO_BACK_URL="sso_back_url";
	public static final String SSO_LOGIN_PAGE="sso_login_page";
	public static final String SSO_CLIENT_ID="sso_client_id";
	public static final String SSO_CLIENT_MIRROR_ID="sso_client_mirror_id";
	public static final String SSO_CLIENT_SESSION_ID="sso_client_sid";
	public static final String SSO_USER_AGENT_TYPE="sso_ua_type";
	public static final String SSO_USER_AGENT_ID="sso_ua_id";
	public static final String SSO_LOGIN_AGENT="sso_login_agent";
	public static final String SSO_GLOBAL_SESSION_ID_ON_SERVER="sso_global_session_id_on_server";
	public static final String SSO_GLOBAL_SESSION_ID="sso_global_session_id";
	public static final String SSO_LOGIN_RESULT_CODE="sso_login_result_code";
	public static final String SSO_LOGIN_TYPE="login_type";
	public static final String SSO_LOGIN_RESULT_MSG="sso_login_result_msg";
	public static final String SSO_LOGIN_CHANCES="sso_login_chances";
	public static final String SSO_LOGIN_INFO="sso_login_info";
	public static final String SSO_TOKEN="sso_token";
	public static final String SSO_SESSIONS_CACHE ="sso_sessions_cache";
	public static final String SSO_ONLINES_CACHE="sso_onlines";
	public static final String SSO_IGNORE_CHECK="sso_ignore_check";

	public static final String SSO_MSG="sso_msg";
	public static final String SSO_SERVICE_UNAVAILABLE="sso_service_unavailable";//服务不可用（比如不是sso server）
	public static final String SSO_BAD_CLIENT="sso_bad_client";//非法的SSO Client
	public static final String SSO_BAD_AGENT="sso_bad_agent";
	public static final String SSO_BAD_TOKEN="sso_bad_token";

	public static final String SSO_USER="sso_user";
	public static final String SSO_PASSPORT="sso_passport";
	public static final String SSO_STAT_CLIENT="sso_stat_client";
	public static final String SSO_TIME="sso_time";
	public static final String SSO_UPDATES="sso_updates";
	public static final String SSO_MD5_STRING="sso_md5_string";
	public static final String RESPONSE_OK="ok";
	public static final String RESPONSE_ERR="err";
	public static final String RESPONSE_MD5_ERR="md5err";

	public static final String I18N_LANGUAGE="lang";
	public static final String J_CURRENCY="currency";

	public static final String J_REQUEST_UUID="j_request_uuid";
	public static final String J_REQUEST_UUID_SN="j_request_uuid_sn";
	public static final String J_DUPLICATED_RQUEST="j_duplicated_request";
	public static final String J_RESPONSE_STREAMING="j_response_streaming";

	public static final String J_BACK_URL="j_back_url";
	public static final String J_BACK_TYPE="j_back_type";
	public static final String J_ACTION_RESULT="j_action_result";

	public static final String J_NO_ACTION="j_no_action";


	public static final String ACCESS_TOKEN="accessToken";
	public static final String REFRESH_TOKEN="refreshToken";
	public static final String ACCESS_KEY="accessKey";
	public static final String ACCESS_SECRET="accessSecret";
	public static final String SIGNATURE="signature";
	public static final String SIGNATURE_TYPE="signatureType";
	public static final String SIGNATURE_TYPE_PARAMS="params";
	public static final String SIGNATURE_TYPE_BODY="body";
	public static final String EXPRIRED="expired";

	public static final String LOGIN_RESULT_CODE="login_result_code";
	public static final String LOGIN_RESULT_MESSAGE="login_result_message";
	public static final String AES_KEY="aes_key";
	public static final String AES_OFFSET="aes_offset";

	public static final String REQUEST_BY_AJAX="REQUEST_BY_AJAX";
	public static final int RESPONSE_TYPE_REDIRECT=1;
	public static final int RESPONSE_TYPE_STRING=2;

	//当客户端初次与服务端交互时，分配给客户端的唯一标识，通过Cookie设置，客户端的后续请求Cookie中必须带上这个标识
	public static final String USER_AGENT_IDENTIFY="Ua_id";
}
