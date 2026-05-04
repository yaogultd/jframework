package j.core.sso;

import j.core.annotation.description.ClassDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@ClassDescription(author = "肖炯",
		date = "2021-08-07",
		description = "登录结果")
@Getter
@Setter
public class LoginResult implements Serializable{
	private static final long serialVersionUID = 1L;
	/*
	 * 认证结果
	 */
	public final static int RESULT_PASSED=1;//登录成功
	public final static int RESULT_FAILED=0;//登录失败（未知错误）
	
	public final static int RESULT_SERVICE_UNAVAILABLE=-1;//服务不可用（比如不是sso server）
	public final static int RESULT_BAD_CLIENT=-2;//非法的SSO Client
	public final static int RESULT_BAD_AGENT=-3;//登录代理不存在或不受理
	public final static int RESULT_BAD_REQUEST=-4;//登录信息无效（不符合规定）
	public final static int RESULT_ROBOT_CHECK_FAILED=-5;//防机器人检查未通过（如：图形验证码错误）
	
	public final static int RESULT_VERIFIER_CODE_INCORRECT=-11;//验证码无效
	public final static int RESULT_USER_NOT_EXISTS=-12;//账号不存在
	public final static int RESULT_PASSWORD_INCORRECT=-13;//密码不正确
	public final static int RESULT_USER_INVALID=-14;//用户无效（冻结等）
	public final static int RESULT_LOAD_USER_ERROR=-15;//加载用户信息失败
	
	public final static int ADDITION_AUTH_FAILED=-21;//二次验证失败
	
	public final static int RESULT_ERROR=-100;//其它错误（登录未成功），可用resultMsg进一步指明错误原因

	private String sysId=null;
	private String machineId=null;
	private String userId=null;//用户ID，登录成功时必须正确设置
	private String subUserId=null;//子账号ID，登录成功时必须正确设置（如果是子账号）
	private String userIp=null;
	private String accessToken=null;
	private String refreshToken=null;
	private int    result=0;//登录结果
	private String resultMsg="";//登录结果提示信息
	private int    chances=-1;//登陆失败的情况下，还可尝试登陆次数
	private Map messages=new HashMap();//自定义键值对

	/**
	 * 添加自定义消息
	 * @param key
	 * @param value
	 */
	public void setMessage(Object key,Object value){
		messages.put(key,value);
	}
}
