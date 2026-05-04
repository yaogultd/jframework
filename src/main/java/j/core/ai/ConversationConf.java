package j.core.ai;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import j.util.JUtilBean;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ConversationConf {
    /**
     * 一些通用配置项
     */

    //交谈语言
    public final static String CONF_CHAT_LANGUAGE="chatLanguage";

    //音频是否翻译成英文
    public final static String CONF_AUDIO_TO_ENGLISH="audioToEnglish";

    //翻译目标语言
    public final static String CONF_TRANSLATE_TO_AUDIO="getTranslateTo";

    //是否将翻译转换成音频
    public final static String CONF_TRANSLATE_TO="translateTo";

    //文字转音频使用声音（标签或ID）
    public final static String CONF_TEXT_TO_SPEECH_VOICE="speechVoice";

    //是否流式
    private boolean streaming;

    //MCP
    private McpSyncServerExchange exchange;

    //输入最多包含消息条数
    private int inputMessagesLimit=0;

    //输入上下文token限制
    private int inputTokenLimit=0;

    //输出token限制
    private int outTokenLimit=0;

    //上下文一直包含的第一条消息
    private Message firstMessageAlwaysIncluded;

    //输出格式化设置
    private Map<String, Object> structuredOutputSettings = new HashMap<>();

    //其它配置项
    private Map<String, String> conf = new HashMap<>();

    public ConversationConf(){

    }

    public ConversationConf(JSONObject conf){
        this.conf.putAll(JUtilBean.jsonPlain2Map(conf));
    }

    public ConversationConf(Model model){
        this.inputTokenLimit = model.getInputTokenLimit();
        this.outTokenLimit = model.getOutTokenLimit();
    }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("{\"inputTokenLimit\": " + inputTokenLimit);
        s.append(",\"inputMessagesLimit\": " + inputMessagesLimit);
        s.append(",\"outTokenLimit\": " + outTokenLimit);
        s.append(",\"firstMessageAlwaysIncluded\": ").append(JUtilBean.bean2Json(firstMessageAlwaysIncluded));
        s.append(",\"conf\": ").append(JUtilBean.map2Json(conf));
        s.append("}");
        return s.toString();
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public ConversationConf set(String key, String value){
        this.conf.put(key, value);
        return this;
    }

    /**
     *
     * @param key
     * @return
     */
    public String get(String key){
        return this.conf.get(key);
    }


    ////////////////一些通用配置项//////////////////////

    /**
     * 交谈语言
     * @return
     */
    public String chatLanguage(){
        String value = this.get(CONF_CHAT_LANGUAGE);
        return JUtilString.isBlank(value) ? Conversation.LANGUAGE_EN : value;
    }

    /**
     * 翻译目标语言
     * @return
     */
    public String translateTo(){
        String value = this.get(CONF_CHAT_LANGUAGE);
        return JUtilString.isBlank(value) ? Conversation.LANGUAGE_EN : value;
    }

    /**
     * 音频是否翻译成英文
     * @return
     */
    public boolean audioToEnglish(){
        String value = this.get(CONF_AUDIO_TO_ENGLISH);
        return "T".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * 是否将翻译转换成音频
     * @return
     */
    public boolean translateToAudio(){
        String value = this.get(CONF_TRANSLATE_TO_AUDIO);
        return "T".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    /**
     * 文字转音频使用声音（标签或ID）
     * @return
     */
    public String speechVoice(){
        String value = this.get(CONF_TEXT_TO_SPEECH_VOICE);
        return value==null ? "" : value;
    }

    ////////////////一些通用配置项 end//////////////////////
}
