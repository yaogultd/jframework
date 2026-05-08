package j.core.ai;

import j.core.annotation.description.ClassDescription;
import j.core.sys.SysUtil;
import j.util.JUtilBean;
import j.util.JUtilJSON;
import j.util.JUtilString;
import j.util.JUtilUUID;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ClassDescription(author = "盖聂大叔（肖炯）",
        date = "2023-04-17",
        description = "AI会话消息")
@Getter
@Setter
public class Message implements Serializable, Cloneable {
    public static final String WHO_SYSTEM="system";
    public static final String WHO_USER="user";
    public static final String WHO_AI="assistant";
    public static final String WHO_MODEL="model";

    public static final int CONTENT_TYPE_TEXT_PLAIN=11;
    public static final int CONTENT_TYPE_TEXT_HTML=12;
    public static final int CONTENT_TYPE_IMAGE=21;
    public static final int CONTENT_TYPE_VIDEO=22;
    public static final int CONTENT_TYPE_AUDIO=23;
    public static final int CONTENT_TYPE_DOC_MD=31;
    public static final int CONTENT_TYPE_DOC_PDF=32;
    public static final int CONTENT_TYPE_DOC_WORD=33;
    public static final int CONTENT_TYPE_DOC_EXCEL=34;
    public static final int CONTENT_TYPE_DOC_CSV=35;

    public static final String STORE_TYPE_PLAIN="000";
    public static final String STORE_TYPE_BASE64="001";
    public static final String STORE_TYPE_HTTP_URL="100";
    public static final String STORE_TYPE_DISK="200";
    public static final String STORE_TYPE_OSS="201";

    protected boolean success=false;
    protected String errorCode;
    protected String who;
    protected String name;
    protected String id;
    protected String conversationId;
    protected String interactionId;
    protected String providerId;
    protected String modelId;
    protected String pluginId;
    protected String beingId;
    protected int convType = Conversation.TYPE_GENERATION_TEXT;
    protected int contentType = Message.CONTENT_TYPE_TEXT_PLAIN;
    protected String content;//实际内容或链接
    protected String storeType;//存储方式
    protected Map<String, MessageData> extraDatas =new HashMap<>();
    protected Map<String, String> extraTexts =new HashMap<>();
    protected long time;
    protected String ip;
    protected int tokens;
    protected boolean helloMessage=false;//是否初始会话信息
    protected boolean byeMessage=false;//是否结束会话信息
    protected boolean stored=false;//是否已入库

    /**
     *
     */
    public Message(){
        this.id = JUtilUUID.genUUID();
        this.time = SysUtil.getNow();
        this.contentType = Message.CONTENT_TYPE_TEXT_PLAIN;
    }

    /**
     *
     * @param id
     * @param conversation
     */
    public Message(String id, Conversation conversation){
        this.id = JUtilString.isBlank(id) ? JUtilUUID.genUUID() : id;
        this.conversationId=conversation.getId();
        this.beingId=conversation.getBeingId();
        this.time = SysUtil.getNow();
        this.contentType = Message.CONTENT_TYPE_TEXT_PLAIN;
    }

    /**
     *
     * @param id
     * @param conversationId
     * @param beingId
     */
    public Message(String id, String conversationId, String beingId){
        this.id = JUtilString.isBlank(id) ? JUtilUUID.genUUID() : id;
        this.conversationId=conversationId;
        this.beingId=beingId;
        this.time = SysUtil.getNow();
        this.contentType = Message.CONTENT_TYPE_TEXT_PLAIN;
    }

    /**
     *
     * @param id
     * @param conversationId
     * @param beingId
     * @param time
     * @param contentType
     */
    public Message(String id, String conversationId, String beingId, long time, int contentType){
        this.id = JUtilString.isBlank(id) ? JUtilUUID.genUUID() : id;
        this.conversationId=conversationId;
        this.beingId=beingId;
        this.time = time;
        this.contentType = contentType;
    }

    /**
     *
     * @param id
     * @param conversation
     * @param time
     * @param contentType
     */
    public Message(String id, Conversation conversation, long time, int contentType){
        this.id = JUtilString.isBlank(id) ? JUtilUUID.genUUID() : id;
        this.conversationId=conversation.getId();
        this.beingId=conversation.getBeingId();
        this.time = time;
        this.contentType = contentType;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        }catch(Exception e) {}
        return null;
    }

    public Message cloneMe() {
        try {
            return (Message)this.clone();
        }catch(Exception e) {}
        return null;
    }

    /**
     *
     * @param key
     * @param data
     */
    public void setExtraData(String key, MessageData data){
        this.extraDatas.put(key, data);
    }

    /**
     *
     * @param key
     * @return
     */
    public MessageData getExtraData(String key){
        return this.extraDatas.get(key);
    }

    /**
     *
     * @param key
     * @param text
     */
    public void setExtraText(String key, String text){
        this.extraTexts.put(key, text);
    }

    /**
     *
     * @param key
     * @return
     */
    public String getExtraText(String key){
        return this.extraTexts.get(key);
    }


    @Override
    public String toString(){
        return JUtilBean.bean2Json(this);
    }

    /**
     * 转换成openai api请求参数的格式
     * @return
     */
    public String toRequestBody4OpenAi(){
        StringBuffer s=new StringBuffer();
        s.append("{\"role\": \""+this.who+"\"");
        s.append(",\"content\": [");
        s.append("{\"type\": \"text\"");
        s.append(",\"text\": \""+JUtilJSON.convertChars(this.content)+"\"");
        s.append("}");
        s.append("]}");
        return s.toString();
    }

    /**
     * 转换成openai api请求参数的格式
     * @return
     */
    public String toRequestBody4Gemini(){
        StringBuffer s=new StringBuffer();
        s.append("{\"role\": \""+(Message.WHO_AI.equals(this.who) ? Message.WHO_MODEL : this.who)+"\"");
        s.append(",\"parts\": [");
        s.append("{\"text\": \""+JUtilJSON.convertChars(this.content)+"\"}");
        s.append("]");
        s.append("}");
        return s.toString();
    }

    /**
     *
     * @param messageList 消息列表（每条消息的role都是一样的）
     * @return
     */
    public String toRequestBody4Gemini(List<Message> messageList){
        StringBuffer s=new StringBuffer();
        s.append("{\"role\": \""+(Message.WHO_AI.equals(this.who) ? Message.WHO_MODEL : this.who)+"\"");
        s.append(",\"parts\": [");
        for(int i=0; i<messageList.size(); i++){
            if(i>0) s.append(",");
            s.append("{\"text\": \""+JUtilJSON.convertChars(this.content)+"\"}");
        }
        s.append("]");
        s.append("}");
        return s.toString();
    }
}
