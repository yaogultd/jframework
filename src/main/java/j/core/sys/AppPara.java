package j.core.sys;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AppPara implements Serializable {
	private String key;
	private String group;
	private String name;
	private String value;
	private String desc;
	private Boolean canBeUpdated;
	private int sequence;//显示顺序
	private Boolean updated=false;//是否修改过未保存（保存到xml或数据库）
	private Boolean isNew=false;//是否新增的参数
	
	public AppPara(String key,String group,String name,String value,String desc,boolean canBeUpdated,int sequence){
		this.key=key;
		this.group=group;
		this.name=name;
		this.value=value;
		this.desc=desc;
		this.canBeUpdated=canBeUpdated;
		this.sequence=sequence;
	}
}
