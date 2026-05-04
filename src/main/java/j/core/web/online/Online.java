package j.core.web.online;

import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class Online implements Serializable{
	private static final long serialVersionUID = 1L;
	public static final int FOUND_BY_SESSION_ID=1;
	public static final int FOUND_BY_GLOBAL_SESSION_ID=2;
	public static final int FOUND_BY_USER_AGNET_IDENTIFY=3;

	private String uaId;//客户端唯一标识

	private long updateTime;
	private long createTime;
	
	private String currentIp;
	private String currentSysId;
	private String currentMachineId;
	private String currentSessionId;
	private String currentReferer;
	private String currentUserAgent;
	private String currentUserAgentType;
	private String currentUrl;
	private String currentLang;//当前使用语言
	private String currentCurrency;//当前使用币种

	private String globalSessionId;//全局会话ID
	private String uid;
	private String subUserId;//子账号ID
	private String unum;
	private String mail;
	private String phone;
	private String uname;
	private String unick;
	private String comName;

	private int foundBy=-1;
	
	public volatile long firstRequestTime=0;
	public volatile long latestRequestTime=0;
	public volatile long requests=0;


	///////////////////（用于在线客服）///////////////
	private ConcurrentList messagesIn=new ConcurrentList();//收到的信息（来自管理员）
	private ConcurrentList messagesOut=new ConcurrentList();//发送的信息（来自管理员）
	private ConcurrentMap messagesWithSellerIn=new ConcurrentMap();//收到的信息	（来自卖家）
	private ConcurrentMap messagesWithSellerOut=new ConcurrentMap();//发送的信息（来自卖家）
	private String serviceStaffId=null;//客服人员ID（来自管理员）
	private ConcurrentMap<String, String> serviceWithSellerStaffId=new ConcurrentMap<>();//客服人员ID（来自卖家）
	private int chatting=0;//聊天状态
	///////////////////（用于在线客服） end///////////////
	
	public Online(){
		this.currentSysId=SysConfig.getSysId();
		this.createTime=SysUtil.getNow();
		this.updateTime=this.createTime;
		this.currentSysId=SysConfig.getSysId();
		this.currentMachineId=SysConfig.getMachineID();
	}

	public void update(){
		this.updateTime=SysUtil.getNow();
	}


	//chatting
	public void addMessageIn(Serializable msg){
		this.messagesIn.add(msg);
	}
	public ConcurrentList getMessagesIn(){
		return this.messagesIn;
	}
	public void clearMessageIn(){
		this.messagesIn.clear();
	}
	public void removeMessageInFirst(){
		if(!this.messagesIn.isEmpty()){
			this.messagesIn.remove(0);
		}
	}

	public void addMessageOut(Serializable msg){
		this.messagesOut.add(msg);
	}
	public ConcurrentList getMessagesOut(){
		return this.messagesOut;
	}
	public void clearMessageOut(){
		this.messagesOut.clear();
	}
	public void removeMessageOutFirst(){
		if(!this.messagesOut.isEmpty()){
			this.messagesOut.remove(0);
		}
	}

	public void setServiceStaffId(String serviceStaffId){
		this.serviceStaffId=serviceStaffId;
	}
	public String getServiceStaffId(){
		return this.serviceStaffId;
	}

	public void addMessageIn(Serializable msg,String sellerId){
		ConcurrentList temp=(ConcurrentList)messagesWithSellerIn.get(sellerId);
		if(temp==null) temp=new ConcurrentList();
		temp.add(msg);
		messagesWithSellerIn.put(sellerId,temp);
	}
	public ConcurrentList getMessagesIn(String sellerId){
		ConcurrentList temp=(ConcurrentList)messagesWithSellerIn.get(sellerId);
		if(temp==null) temp=new ConcurrentList();
		return temp;
	}
	public void clearMessageIn(String sellerId){
		messagesWithSellerIn.remove(sellerId);
	}
	public void removeMessageInFirst(String sellerId){
		ConcurrentList temp=(ConcurrentList)messagesWithSellerIn.get(sellerId);
		if(temp==null) temp=new ConcurrentList();

		if(!temp.isEmpty()){
			temp.remove(0);
		}
		messagesWithSellerIn.put(sellerId,temp);
	}

	public void addMessageOut(Serializable msg,String sellerId){
		ConcurrentList temp=(ConcurrentList)messagesWithSellerOut.get(sellerId);
		if(temp==null) temp=new ConcurrentList();
		temp.add(msg);
		messagesWithSellerOut.put(sellerId,temp);
	}
	public ConcurrentList getMessagesOut(String sellerId){
		ConcurrentList temp=(ConcurrentList)messagesWithSellerOut.get(sellerId);
		if(temp==null) temp=new ConcurrentList();
		return temp;
	}
	public void clearMessageOut(String sellerId){
		messagesWithSellerOut.remove(sellerId);
	}
	public void removeMessageOutFirst(String sellerId){
		ConcurrentList temp=(ConcurrentList)messagesWithSellerOut.get(sellerId);
		if(temp==null) temp=new ConcurrentList();

		if(!temp.isEmpty()){
			temp.remove(0);
		}
		messagesWithSellerOut.put(sellerId,temp);
	}

	public void setServiceStaffId(String serviceStaffId,String sellerId){
		serviceWithSellerStaffId.put(sellerId,serviceStaffId);
	}
	public String getServiceStaffId(String sellerId){
		return (String)serviceWithSellerStaffId.get(sellerId);
	}

	public void setChatting(int chatting){
		if(chatting!=Onlines.CHATTING_PENDING
				&&chatting!=Onlines.CHATTING_WAITING
				&&chatting!=Onlines.CHATTING_INPROCESS
				&&chatting!=Onlines.CHATTING_ENDED
				&&chatting!=Onlines.CHATTING_REFUSED
				&&chatting!=Onlines.CHATTING_REFUSED_SESSION){
			return;
		}

		this.chatting=chatting;
	}
	public int getChatting(){
		return this.chatting;
	}
	//chatting end
}
