package j.core.permission;

import j.util.ConcurrentMap;
import j.util.JUtilBean;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 角色，代表一个权限的集合，拥有某个角色的用户可以执行某些操作/访问某些资源
 * @author 肖炯
 *
 */
@Getter
@Setter
public class Role implements Serializable{
	public static final String STATUS_AVAILABLE="1";
	public static final String STATUS_UNAVAILABLE="0";

	private static ConcurrentMap<String, Role> roles=new ConcurrentMap<>();

	private String roleId;
	private String roleName;
	private String roleDesc;
	private String roleStat;
	
	/**
	 * constructor
	 * 
	 */
	private Role(){
		this.roleStat=STATUS_AVAILABLE;
	}
	
	/**
	 * 
	 * @param roleId
	 * @return
	 */
	public static Role getInstance(String roleId){
		if(roles.containsKey(roleId)){
			return (Role)roles.get(roleId);
		}
		Role role=new Role();
		role.setRoleId(roleId);
		roles.put(roleId,role);
		return role;
	}

	@Override
	public String toString(){
		return JUtilBean.bean2Json(this);
	}
}
