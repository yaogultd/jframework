package j.core.ai;

import j.core.ai.plugin.Plugin;
import j.core.ai.plugin.Plugins;
import j.core.annotation.description.ClassDescription;
import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilJSON;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "盖聂大叔（肖炯）",
        date = "2023-04-17",
        description = "AI会话")
@Getter
@Setter
public class Conversation extends NvwaAncestor {
    private static Logger log=Logger.create(Conversation.class);//日志输出

    //生命周期：持久的
    public static final int LIFE_PERSISTENT=1;

    //生命周期：临时的
    public static final int LIFE_TEMPORARY=2;

    //会话类型：普通交谈
    public static final int TYPE_CHAT=1;

    //会话类型：翻译
    public static final int TYPE_TRANSLATE=3;

    //会话类型：音频转文字
    public static final int TYPE_AUDIO_TO_TEXT=4;

    //会话类型：文字转音频
    public static final int TYPE_TEXT_TO_AUDIO=5;

    //最多缓存消息数
    public static final int MAX_MESSAGES_IN_CACHE=100;

    //会话类型：生成文本/图片/视频
    public static final int TYPE_GENERATION_TEXT=51;
    public static final int TYPE_GENERATION_IMAGE=52;
    public static final int TYPE_GENERATION_VIDEO=53;
    public static final int TYPE_EDIT_IMAGE=54;

    //会话类型：深度研究
    public static final int TYPE_DEEP_RESEARCH=101;

    //会话语言
    public static final String LANGUAGE_EN="English";
    public static final String LANGUAGE_CH="Chinese";
    public static final String LANGUAGE_FR="French";
    public static final String LANGUAGE_IT="Italian";
    public static final String LANGUAGE_RU="Russian";
    public static final String LANGUAGE_GE="German";
    public static final String LANGUAGE_SP="Spanish";
    public static final String LANGUAGE_JP="Japanese";
    public static final String LANGUAGE_KR="Korean";

    //会话设定固固定语
    private static ConcurrentMap<String, String> words = new ConcurrentMap<>();

    static {
        words.put(LANGUAGE_EN, "You are a helpful asistant with name {name}");
        words.put(LANGUAGE_EN+".traits", ", you are {traits}");
        words.put(LANGUAGE_EN+".expertIn", ", and expert in {expertIn}");
        words.put(LANGUAGE_EN+".speak", ", please speak English");
        words.put(LANGUAGE_EN+".subject", ", let's talk about the subject: {subject}");
        words.put(LANGUAGE_EN+".translateTo", "Translate all the following messages into {translateTo}");

        words.put(LANGUAGE_CH, "你是一个名字叫“{name}”的AI助理");
        words.put(LANGUAGE_CH+".traits", "，你的性格特点是“{traits}”");
        words.put(LANGUAGE_CH+".expertIn", "，你的专长是“{expertIn}”");
        words.put(LANGUAGE_CH+".speak", "，请用中文和我交谈。");
        words.put(LANGUAGE_CH+".subject", "，让我们讨论一下如下话题：{subject}");
        words.put(LANGUAGE_CH+".translateTo", "将以下所有消息翻译成{translateTo}");

        words.put(LANGUAGE_FR, "Vous êtes un assistant ai avec le nom {name}");
        words.put(LANGUAGE_FR+".traits", ", vous êtes {traits}");
        words.put(LANGUAGE_FR+".expertIn", ", vous êtes un expert du {expertIn}");
        words.put(LANGUAGE_FR+".speak", ", Veuillez parler français");
        words.put(LANGUAGE_FR+".subject", ", parlons du sujet: {subject}");
        words.put(LANGUAGE_FR+".translateTo", "Traduisez tous les messages suivants en {translateTo}");

        words.put(LANGUAGE_IT, "Sei un assistente utile con nome {nome}");
        words.put(LANGUAGE_IT+".traits", ", sei {traits}");
        words.put(LANGUAGE_IT+".expertIn", ", ed esperto di {expertIn}");
        words.put(LANGUAGE_IT+".speak", ", Si prega di parlare italiano");
        words.put(LANGUAGE_IT+".subject", ", Parliamo dell'argomento: {subject}");
        words.put(LANGUAGE_IT+".translateTo", "Traduci tutti i seguenti messaggi in {translateTo}");

        words.put(LANGUAGE_RU, "Ты правая рука по имени {name}");
        words.put(LANGUAGE_RU+".traits", ", Ты {traits}");
        words.put(LANGUAGE_RU+".expertIn", ", Ты хорошо {expertIn}");
        words.put(LANGUAGE_RU+".speak", ", Говорите по - русски.");
        words.put(LANGUAGE_RU+".subject", ", Давайте поговорим об этом: {subject}");
        words.put(LANGUAGE_RU+".translateTo", "Переведите все следующие сообщения на {translateTo}");

        words.put(LANGUAGE_GE, "Du bist ein hilfreicher Assistent mit Namen {name}");
        words.put(LANGUAGE_GE+".traits", ", du bist {traits}");
        words.put(LANGUAGE_GE+".expertIn", ", und Experten im {expertIn}");
        words.put(LANGUAGE_GE+".speak", ", bitte sprechen Sie Deutsch");
        words.put(LANGUAGE_GE+".subject", ", lass uns über das Thema sprechen: {subject}");
        words.put(LANGUAGE_GE+".translateTo", "Übersetzen Sie alle folgenden Nachrichten ins {translateTo}");

        words.put(LANGUAGE_SP, "Eres una mano derecha llamada name}");
        words.put(LANGUAGE_SP+".traits", ", eres {traits}");
        words.put(LANGUAGE_SP+".expertIn", ", experto en {expertIn}");
        words.put(LANGUAGE_SP+".speak", ", Habla español, por favor.");
        words.put(LANGUAGE_SP+".subject", ", Hablemos de este tema: {subject}");
        words.put(LANGUAGE_SP+".translateTo", "Traduce todos los siguientes mensajes al {translateTo}");

        words.put(LANGUAGE_JP, "あなたは{name}という名前の役に立つアシスタントです");
        words.put(LANGUAGE_JP+".traits", ", あなたは{traits}だ");
        words.put(LANGUAGE_JP+".expertIn", ", {expertIn}の専門家");
        words.put(LANGUAGE_JP+".speak", ", 日本語で話してください");
        words.put(LANGUAGE_JP+".subject", ", 主題について話しましょう: {subject}");
        words.put(LANGUAGE_JP+".translateTo", "以下のすべてのメッセージを{translateTo}に翻訳してください");

        words.put(LANGUAGE_KR, "당신은 {name}이라는 이름의 도움이 되는 조수입니다.");
        words.put(LANGUAGE_KR+".traits", ", 너는 {traits}");
        words.put(LANGUAGE_KR+".expertIn", ", 그리고 {expertIn}의 전문가");
        words.put(LANGUAGE_KR+".speak", ", 한국어로 말해주세요");
        words.put(LANGUAGE_KR+".subject", ", {subject}라는 주제에 대해 이야기해 봅시다.");
        words.put(LANGUAGE_KR+".translateTo", "아래 모든 메시지를 {translateTo}로 번역하세요");
    }

    /**
     *
     * @param language
     * @param usedFor
     * @return
     */
    public static String getWords(String language, String usedFor){
        String text = JUtilString.isBlank(usedFor) ? words.get(language) : words.get(language+"."+usedFor);
        if(JUtilString.isBlank(text)) text = JUtilString.isBlank(usedFor) ? words.get(Conversation.LANGUAGE_EN) : words.get(Conversation.LANGUAGE_EN+"."+usedFor);
        return text;
    }

    //所属Being
    protected String beingId;

    //生命周期
    protected int lifeType;

    //实例ID
    protected String id;

    //实例名称
    protected String name;

    //实例简介
    protected String desc;

    //实例配置
    protected ConversationConf conf;

    //生存起始时间
    protected long lifeStart;

    //生存结束时间
    protected long lifeEnd;

    //最大空闲时间（超过将销毁）
    protected long maxIdle=0;

    //最近活跃时间
    protected long latestActive=0;

    //最近交互供应商
    //protected String latestProviderId;

    //最近交互模型ID
    //protected String latestModelId;

    //最近使用插件ID
    //protected String latestPluginId;


    //历史对话消息
    protected ConcurrentList<Message> messages=new ConcurrentList<>();

    //会话是否已经结束
    protected boolean ended = false;

    /**
     *
     * @param being
     * @param lifeType
     * @param id
     * @param name
     * @param desc
     * @param conf
     * @param lifeStart
     * @param lifeEnd
     * @param maxIdle
     */
    public Conversation(Being being, int lifeType, String id, String name, String desc, String conf, long lifeStart, long lifeEnd, long maxIdle){
        this.beingId=being.getId();
        this.lifeType=lifeType;
        this.id=id;
        this.name=name;
        this.desc=desc;
        this.conf=new ConversationConf(JUtilJSON.parse(conf));
        this.lifeStart=lifeStart>0?lifeStart:SysUtil.getNow();
        this.lifeEnd=lifeEnd;
        this.maxIdle=maxIdle;
        this.setLatestActive();
    }

    /**
     *
     * @return
     */
    public Being ofBeing(){
        return FOZU.getBeing(this.beingId);
    }

    /**
     * 更新最近活动时间
     */
    public void setLatestActive(){
        this.setLatestActive(SysUtil.getNow());
        FOZU.getBeing(this.beingId).setLatestActive(this.getLatestActive());
    }

    /**
     *
     * @throws Exception
     */
    public void checkStatus() throws Exception{
        if(this.isEnded()) throw new Exception("The conversation is ended.");
    }

    /**
     * 结束
     * @return
     * @throws Exception
     */
    public boolean end() throws Exception{
        this.ended = true;
        return true;
    }

    /**
     * 是否已开始
     * @return
     */
    public boolean isStarted(){
        if(this.lifeStart<=0) return true;
        else return (SysUtil.getNow() > this.lifeStart);
    }

    /**
     *
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
     * @param messageId
     */
    public Message getMessage(String messageId){
        if(JUtilString.isBlank(messageId)) return null;
        for(int i=0; i<this.messages.size(); i++){
            if(this.messages.get(i).getId().equals(messageId)) return this.messages.get(i);
        }
        return null;
    }

    /**
     *
     * @param message
     */
    public void saveMessage(Message message){
        if(message==null) return;
        this.messages.add(message);
        while(this.messages.size() > MAX_MESSAGES_IN_CACHE) this.messages.remove(0);
    }

    /**
     *
     * @param message
     * @param index
     */
    public void saveMessage(Message message, int index){
        if(message==null) return;
        this.messages.add(index, message);
        while(this.messages.size() > MAX_MESSAGES_IN_CACHE) this.messages.remove(index+1);
    }

    /**
     *
     */
    public void clearMessages(){
        this.messages.clear();
    }

    /**
     *
     * @param message
     */
    public void removeMessage(Message message){
        if(message==null) return;
        for(int i=0; i<this.messages.size(); i++){
            if(this.messages.get(i).getId().equals(message.getId())){
                this.messages.remove(i);
                break;
            }
        }
    }

    /**
     *
     * @param key
     * @param text
     * @return
     */
    public List<Message> getMessagesByExtraText(String key, String text){
        if(JUtilString.isBlank(key) || JUtilString.isBlank(text)) return null;

        List<Message> matched = new ArrayList<>();

        for(int i=0; i<messages.size(); i++){
            Message c = messages.get(i);
            if(text.equals(c.getExtraText(key))) matched.add(c);
        }
        return matched;
    }

    /**
     *
     * @param key
     * @param text
     * @return
     */
    public Message getMessageByExtraText(String key, String text){
        List<Message> matched = getMessagesByExtraText(key, text);
        return matched==null || matched.isEmpty() ? null : matched.get(0);
    }

    @Override
    public String toString(){
        StringBuffer s=new StringBuffer();
        s.append("{\"messages\":[");
        for(int i=0; i<this.messages.size(); i++){
            if(i>0) s.append(",");
            s.append(this.messages.get(i).toString());
        }
        s.append("]}");
        return s.toString();
    }
}
