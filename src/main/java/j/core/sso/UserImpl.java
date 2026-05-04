package j.core.sso;

import j.core.permission.Role;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author 肖炯
 *
 */
public class UserImpl extends User{
	private static final long serialVersionUID = 1L;
	protected String userId;
	protected String subUserId;
	protected String userName;
	protected String userAvatar;
	protected String userType="000";

	/**
	 *
	 */
	public UserImpl() {
		super();
	}

	@Override
	public String getUserId() {
		return this.userId;
	}

	@Override
	public void setUserId(String userId) {	
		this.userId=userId;
	}

	@Override
	public String getSubUserId() {
		return this.subUserId;
	}

	@Override
	public void setSubUserId(String subUserId) {	
		this.subUserId=subUserId;
	}

	@Override
	public String getUserName() {
		return this.userName;
	}

	@Override
	public void setUserName(String userName) {	
		this.userName=userName;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public void setUserType(String userType) {
		this.userType=userType;
	}

	@Override
	public String getUserAvatar(){
		return this.userAvatar;
	}

	@Override
	public void setUserAvatar(String userAvatar){
		this.userAvatar=userAvatar;
	}

	@Override
	public boolean load(HttpServletRequest _request, String _accessToken) throws Exception {
		UserInXml user=AuthenticatorImpl.getUser(userId);
		if(user==null) return false;
		
		setUserName(user.name);
		
		for(int i=0;i<user.roles.length;i++){
			this.roles.add(Role.getInstance(user.roles[i]));
		}
		return true;
	}
}
