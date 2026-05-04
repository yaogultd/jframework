package j.core.sso;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.security.AES;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.util.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.io.Serializable;

@ClassDescription(author = "肖炯",
        date = "2021-07-30",
        description = "表示一个会话，登录成功后分配一个accessToken，向服务端发送请求时必须带上accessToken；同一会话可能关联多个UserAgent（比如APP和H5混合开发")
@Getter
@Setter
public class SSOSession implements Serializable {
    @FieldDescription(description = "SSO客户应用ID")
    private String clientId;

    @FieldDescription(description = "用户ID")
    private String userId;

    @FieldDescription(description = "子账号用户ID")
    private String subUserId;

    @FieldDescription(description = "用户IP")
    private String userIp;

    @FieldDescription(description = "用户登录成功后，生成accessToken，后续交互带上accessToken")
    private String accessToken;

    @FieldDescription(description = "用户刷新accessToken")
    private String refreshToken;

    @FieldDescription(description = "会话初始创建时间")
    private long createdAt;

    @FieldDescription(description = "客户端最近请求时间")
    private long requestedAt;

    @FieldDescription(description = "与该会话相关的所有UserAgent的类型，按初次访问顺序加入")
    private ConcurrentList<String> userAgentTypes=new ConcurrentList<>();

    @FieldDescription(description = "与该会话相关的所有UserAgent的身份标识，按初次访问顺序加入")
    private ConcurrentList<String> userAgentIds=new ConcurrentList<>();

    @FieldDescription(description = "与该会话相关的UserAgent，key为UserAgent的身份标识")
    private ConcurrentMap<String, UserAgent> userAgents=new ConcurrentMap<>();

    private SSOSession(){
    }

    /**
     * 将发送请求的UserAgent的身份标识添加到列表
     * @param userAgentType
     * @param userAgentId
     */
    private void setUserAgentId(String userAgentType, String userAgentId){
        if(!this.userAgentIds.contains(userAgentId)){
            this.userAgentTypes.add(userAgentType);
            this.userAgentIds.add(userAgentId);
        }
    }

    /**
     * 记录用户访问使用的UserAgent，是否调用取决于客户端应用的业务需求
     * @param userAgent
     * @return true表示新接入UserAgent
     */
    public boolean update(UserAgent userAgent){
        UserAgent exists=userAgents.get(userAgent.getUaIdentify());

        //已经存在
        if(exists!=null){
            JUtilBean.copyObject(userAgent, exists);
            exists.setRequestedAt(SysUtil.getNow());
            return false;
        }

        userAgent.setConnectedAt(SysUtil.getNow());
        userAgent.setRequestedAt(userAgent.getConnectedAt());
        this.userAgents.put(userAgent.getUaIdentify(), userAgent);

        return true;
    }

    /**
     * 是否来自某类型的UserAgent
     * @param userAgentType
     * @return
     */
    public boolean fromUserAgentType(String userAgentType){
        if(userAgentType==null || "".equals(userAgentType)) return false;
        return this.userAgentTypes.contains(userAgentType);
    }

    /**
     * accessToken是否已超时
     * @return
     */
    public boolean isAccessTokenExpired(long sessionTimeout){
        if(sessionTimeout<=0) sessionTimeout=SSOConfig.getSessionTimeout()*1000L;
        return SysUtil.getNow() - this.getCreatedAt() > sessionTimeout;
    }

    /**
     * refreshToken是否已超时
     * @return
     */
    public boolean isRefreshTokenExpired(long sessionTimeout){
        if(sessionTimeout<=0) sessionTimeout=SSOConfig.getSessionTimeout()*1000L;
        return SysUtil.getNow() - this.getCreatedAt() > sessionTimeout;
    }

    /**
     * 根据会话信息生成accessToken
     * @param session
     * @param client
     * @return
     */
    private static String genAccessToken(SSOSession session, Client client){
        try{
            StringBuffer info=new StringBuffer("{");
            info.append("\"userId\":\""+session.getUserId()+"\"");
            if(!JUtilString.isBlank(session.getSubUserId())){
                info.append(",\"subUserId\":\""+session.getSubUserId()+"\"");
            }
            info.append(",\"time\":\""+session.getCreatedAt()+"\"");
            info.append("}");

            String s=info.toString();
            info=null;

            s=AES.encrypt(s, client.getAesKey(), client.getAesOffset());
            s=JUtilBase64.encode(s.getBytes(SysConfig.sysEncoding));
            s=JUtilString.replaceAll(s,"\r","r^r");
            s=JUtilString.replaceAll(s,"\n","n^n");
            s=JUtilString.replaceAll(s,"\b","b^b");
            s=JUtilString.replaceAll(s,"\t","t^t");

            return s;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 根据会话信息生成refreshToken
     * @param session
     * @return
     */
    private static String genRefreshToken(SSOSession session, Client client){
        try{
            StringBuffer info=new StringBuffer("{");
            info.append("\"userId\":\""+session.getUserId()+"\"");
            if(!JUtilString.isBlank(session.getSubUserId())){
                info.append(",\"subUserId\":\""+session.getSubUserId()+"\"");
            }
            info.append(",\"time\":\""+session.getCreatedAt()+"\"");
            info.append("}");

            String s=info.toString();
            info=null;

            s=AES.encrypt(s, client.getAesKey(), client.getAesOffset());
            s=JUtilBase64.encode(s.getBytes(SysConfig.sysEncoding));
            s=JUtilString.replaceAll(s,"\r","r^r");
            s=JUtilString.replaceAll(s,"\n","n^n");
            s=JUtilString.replaceAll(s,"\b","b^b");
            s=JUtilString.replaceAll(s,"\t","t^t");

            return JUtilMD5.MD5EncodeToHex(s);
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 登录成功后调用本方法创建一个会话
     * @param clientId
     * @param userId
     * @param subUserId
     * @param userIp
     * @param userAgentType
     * @param userAgentId
     * @return
     */
    public static SSOSession create(String clientId,  String userId, String subUserId, String userIp, String userAgentType, String userAgentId){
        Client client=SSOConfig.getSsoClientById(clientId);

        SSOSession session=new SSOSession();

        session.setClientId(clientId);
        session.setUserId(userId);
        session.setSubUserId(subUserId);
        session.setUserAgentId(userAgentType, userAgentId);
        session.setCreatedAt(SysUtil.getNow());
        session.setAccessToken(genAccessToken(session, client));
        session.setRefreshToken(genRefreshToken(session, client));

        return session;
    }

    /**
     * 解析accessToken
     * @param accessToken
     * @return 根据accessToken解析出关键信息（无accessToken）
     * @throws Exception
     */
    public static SSOSession parseAccessToken(String accessToken, Client client) throws Exception{
        accessToken=JUtilString.replaceAll(accessToken,"r^r","\r");
        accessToken=JUtilString.replaceAll(accessToken,"n^n","\n");
        accessToken=JUtilString.replaceAll(accessToken,"b^b","\b");
        accessToken=JUtilString.replaceAll(accessToken,"t^t","\t");
        accessToken = new String(JUtilBase64.decode(accessToken), SysConfig.sysEncoding);
        accessToken = AES.decrypt(accessToken, client.getAesKey(), client.getAesOffset());

        JSONObject info = JUtilJSON.parse(accessToken);

        SSOSession session = new SSOSession();
        session.setClientId(JUtilJSON.string(info, "clientId"));
        session.setUserId(JUtilJSON.string(info, "userId"));
        session.setSubUserId(JUtilJSON.string(info, "subUserId"));
        session.setUserAgentId(JUtilJSON.string(info, "userAgentType"), JUtilJSON.string(info, "userAgentId"));

        info=null;

        return session;
    }
}
