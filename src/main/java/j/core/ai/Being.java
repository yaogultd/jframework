package j.core.ai;

import j.core.ai.plugin.Plugin;
import j.core.ai.plugin.Plugins;
import j.core.annotation.description.ClassDescription;
import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import j.util.JUtilUUID;
import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "盖聂大叔（肖炯）",
        date = "2023-04-17",
        description = "AI实例")
@Getter
@Setter
public class Being extends NvwaAncestor{
    //生命周期：持久的
    public static final int LIFE_PERSISTENT=1;

    //生命周期：临时的
    public static final int LIFE_TEMPORARY=2;

    //服务提供商ID
    protected String providerId;

    //创建者
    protected String creator;

    //生命周期
    protected int lifeType;

    //实例ID
    protected String id;

    //实例名称
    protected String name;

    //实例简介
    protected String desc;

    //实例简介
    protected String avatar;

    //擅长
    protected String expertIn;

    //性格
    protected String traits;

    //生存起始时间
    protected long lifeStart;

    //生存结束时间
    protected long lifeEnd;

    //最大空闲时间（超过将销毁）
    protected long maxIdle=0;

    //最近活跃时间
    protected long latestActive=0;

    //是否已创建
    protected boolean created=false;

    //是否已销毁
    protected boolean destroyed=false;

    //所有会话
    protected ConcurrentMap<String, Conversation> conversations=new ConcurrentMap<>();

    /**
     * 仅供框架初始化使用
     */
    public Being(){}

    /**
     *
     * @param creator
     * @param lifeType
     * @param id
     * @param name
     * @param desc
     */
    public Being(String creator, int lifeType, String id, String name, String desc){
        this.creator=creator;
        this.lifeType=lifeType;
        this.id=id;
        this.name=name;
        this.desc=desc;
        this.lifeStart=SysUtil.getNow();
    }

    /**
     *
     * @param creator
     * @param lifeType
     * @param id
     * @param name
     * @param desc
     * @param lifeStart
     * @param lifeEnd
     * @param maxIdle
     */
    public Being(String creator, int lifeType, String id, String name, String desc, long lifeStart, long lifeEnd, long maxIdle){
        this.creator=creator;
        this.lifeType=lifeType;
        this.id=id;
        this.name=name;
        this.desc=desc;
        this.lifeStart=lifeStart>0?lifeStart:SysUtil.getNow();
        this.lifeEnd=lifeEnd;
        this.maxIdle=maxIdle;
    }

    /**
     *
     * @throws Exception
     */
    private void checkStatus() throws Exception{
        if(this.isEnded()) throw new Exception("The being is ended.");
    }

    /**
     * 创建
     * @return
     * @throws Exception
     */
    public boolean create() throws Exception{
        this.checkStatus();
        if(this.created) throw new Exception("The being has been created.");
        this.created=true;
        return true;
    }

    /**
     * 销毁
     * @return
     */
    public boolean destroy() throws Exception{
        this.destroyed=true;
        List<Conversation> convs=this.conversations.listValues();
        for(int i=0; i<convs.size(); i++){
            convs.get(i).end();
        }
        return true;
    }

    /**
     * 是否已出生
     * @return
     */
    public boolean isBorn(){
        if(this.lifeStart<=0) return true;
        else return (SysUtil.getNow() > this.lifeStart);
    }

    /**
     * 是否已终止（到设定时间或超出最大空闲时间），不包含被动销毁的情况
     * @return
     */
    public boolean isEnded(){
        //如果明确指定了结束时间，不管什么情况，到时间就终止
        if(this.lifeEnd > 0 && this.lifeEnd <= SysUtil.getNow()) return true;

        //如果是持久生命，不予终止
        if(this.lifeType==Being.LIFE_PERSISTENT) return false;

        //如果指定了最大空闲时间
        if(this.maxIdle>0 && (SysUtil.getNow() - this.latestActive) >= this.maxIdle) return true;

        return false;
    }

    /**
     *
     * @param lifeType
     * @param id
     * @param name
     * @param desc
     * @param conf
     * @param pluginId
     * @param restoring
     * @return
     * @throws Exception
     */
    public Conversation startConversation(int lifeType, String id, String name, String desc, String conf, String pluginId, boolean restoring) throws Exception{
        return startConversation(lifeType, id, name, desc, conf, pluginId, restoring, 0, 0, 0);
    }

    /**
     *
     * @param lifeType
     * @param id
     * @param name
     * @param desc
     * @param conf
     * @param pluginId
     * @param restoring
     * @param lifeStart
     * @param lifeEnd
     * @param maxIdle
     * @return
     * @throws Exception
     */
    public Conversation startConversation(int lifeType, String id, String name, String desc, String conf, String pluginId, boolean restoring, long lifeStart, long lifeEnd, long maxIdle) throws Exception{
        Plugin plugin = Plugins.getPlugin(pluginId);
        if(!JUtilString.isBlank(pluginId)) {
            if (plugin == null) throw new Exception("plugin " + pluginId + " is not exists.");
        }

        if(JUtilString.isBlank(conf)) conf = "{}";

        if(JUtilString.isBlank(id)) id= JUtilUUID.genUUID();
        if(this.conversations.containsKey(id)){
            Conversation c=this.conversations.get(id);
            c.setName(name);
            c.setDesc(desc);
            c.setLifeEnd(lifeEnd);
            c.setMaxIdle(maxIdle);
            return c;
        }

        Conversation c = new Conversation(this, lifeType, id, name, desc, conf, lifeStart, lifeEnd, maxIdle);

        if(!restoring && plugin != null) c = plugin.onConversationStarted(c);
        this.conversations.put(id, c);
        return c;
    }

    /**
     *
     * @param conversationId
     * @return
     */
    public Conversation getConversation(String conversationId){
        if(JUtilString.isBlank(conversationId)) return null;
        return this.conversations.get(conversationId);
    }

    /**
     *
     * @param key
     * @param text
     * @return
     */
    public List<Conversation> getConversationsByConfig(String key, String text){
        if(JUtilString.isBlank(key) || JUtilString.isBlank(text)) return null;

        List<Conversation> matched = new ArrayList<>();

        List<Conversation> _conversations=this.conversations.listValues();
        for(int i=0; i<_conversations.size(); i++){
            Conversation c = _conversations.get(i);
            if(text.equals(c.getConf().get(key))) matched.add(c);
        }
        return matched;
    }

    /**
     *
     * @param key
     * @param text
     * @return
     */
    public Conversation getConversationByConfig(String key, String text){
        List<Conversation> matched = getConversationsByConfig(key, text);
        return matched==null || matched.isEmpty() ? null : matched.get(0);
    }

    /**
     *
     */
    public void removeEndedConverstions(){
        List<Conversation> _conversations=this.conversations.listValues();
        for(int i=0; i<_conversations.size(); i++){
            Conversation c = _conversations.get(i);
            if(c.isEnded()) this.conversations.remove(c.getId());
        }
    }
}