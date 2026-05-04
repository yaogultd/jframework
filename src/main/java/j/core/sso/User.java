package j.core.sso;

import java.io.Serializable;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import j.core.permission.Role;
import j.core.sys.SysConfig;
import j.tool.region.Countries;
import j.util.ConcurrentList;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author 肖炯
 *
 */
public abstract class User implements Serializable{
	//角色信息是否已加载用户信息
	private boolean roloesLoaded=false;

	@Setter
	@Getter
	protected String accessToken=null;

	protected ConcurrentList<Role> roles;// 用户角色列表
	
	public abstract String getUserId();	
	
	public abstract void setUserId(String userId);	
	
	public abstract String getUserName();	
	
	public abstract void setUserName(String userName);

	public abstract String getUserType();

	public abstract void setUserType(String userType);

	public String getSubUserId(){
		return null;
	}
	
	public void setSubUserId(String subUserId) {}

	public abstract String getUserAvatar();

	public abstract void setUserAvatar(String userAvatar);

	public void setRoloesLoaded(boolean roloesLoaded){
		this.roloesLoaded=roloesLoaded;
	}

	public boolean getRoloesLoaded(){
		return this.roloesLoaded;
	}
	
	//加载用户信息
	public abstract boolean load(HttpServletRequest _request,String _accessToken) throws Exception;

	/**
	 * constructor
	 *
	 */
	public User() {
		roles = new ConcurrentList();
	}
	
	/**
	 * 
	 * @param _userId
	 * @throws Exception
	 */
	public User(String _userId) throws Exception {
		if (_userId == null) {
			throw new Exception("用户Id为空");
		} else {
			setUserId(_userId);
			roles = new ConcurrentList();
		}
	}

	/**
	 * 拥有的角色
	 * @return
	 * @throws Exception
	 */
	public ConcurrentList<Role> getRoles() throws Exception {
		return roles;
	}

	/**
	 * 用户是否拥有roleIds中的某个角色
	 * @param roleIds
	 * @return
	 */
	public boolean isUserInRole(String roleIds[]) {
		if(!getRoloesLoaded()){
			loadRoles();
			setRoloesLoaded(true);
		}

		if (roleIds == null || roleIds.length == 0){
			return true;
		}else{
			for (int i = 0; i < roles.size(); i++) {
				Role role = (Role) roles.get(i);
				if(Role.STATUS_AVAILABLE.equals(role.getRoleStat())&&JUtilString.contain(roleIds,role.getRoleId())) return true;
			}
			return false;
		}
	}

	public boolean loadRoles(){
		return true;
	}

	/**
	 * 用户是否拥有roleId所代表的角色
	 * @param roleId
	 * @return
	 */
	public boolean isUserInRole(String roleId){
		if(!getRoloesLoaded()){
			loadRoles();
			setRoloesLoaded(true);
		}

		if (roleId == null||roleId.equals("")){
			return true;
		}else{
			for (int i = 0; i < roles.size(); i++) {
				Role role = (Role) roles.get(i);
				if (Role.STATUS_AVAILABLE.equals(role.getRoleStat())&&roleId.equals(role.getRoleId())){
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 加载用户详细信息
	 * @param _request
	 * @param _userId
	 * @param _subUserId
	 * @param _accessToken
	 * @return
	 */
	public static User loadUser(HttpServletRequest _request,String _userId, String _subUserId, String _accessToken){
		User user=null;
		try{
			Client client=SSOConfig.getSsoClientById(SysConfig.getSysId());
			user=(User)Class.forName(client.getUserClass()).newInstance();
			user.setUserId(_userId);
			user.setSubUserId(_subUserId);
			user.setAccessToken(_accessToken);
			boolean loaded=user.load(_request,_accessToken);
			if(!loaded&&user!=null) user=null;
		}catch(Exception e){
			e.printStackTrace();
			if(user!=null) user=null;
		}
		return user;
	}

	/**
	 * 清除用户信息
	 * 
	 */
	public void destroy() {
		roles.clear();
	}
	
	/**
	 * 
	 * @param uid
	 * @return
	 */
	public static boolean isValidUid(String uid){
		if(uid==null||uid.equals("")) return false;

		uid=uid.toLowerCase();

		if(Countries.isPhoneNumberValid(uid)
				||JUtilString.isEmail(uid, 64)){
			return true;
		}
		
		return uid.matches("[0-9a-z]{1}[0-9a-z\\-._]{1,32}[0-9a-z]{1}$");
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println(isValidUid("crazyroar@126.com"));
		System.out.println(isValidUid("15099782078"));
		System.out.println(isValidUid("xiao-jiong_x.c"));
		System.out.println(isValidUid("xiao-jiong_x."));
	}
}