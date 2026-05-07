package j.core.ai.provider.openai;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import j.core.ai.*;
import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.common.JArray;
import j.core.hp.asynchronous.Waitings;
import j.core.sys.SysUtil;
import j.core.web.handler.JResponse;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilJSON;
import j.util.JUtilString;
import j.util.JUtilUUID;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Nvwa
@Properties(path = "openai.properties")
public class OpenAIProvider extends Provider {
    private static Logger log=Logger.create(OpenAIProvider.class);//日志输出

    //语音转文字的语言代码
    private static ConcurrentMap<String, String> langCodesForAudio2Text = new ConcurrentMap<>();

    static {
        langCodesForAudio2Text.put(Conversation.LANGUAGE_EN, "en");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_CH, "zh");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_FR, "fr");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_IT, "it");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_RU, "ru");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_GE, "de");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_SP, "es");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_JP, "ja");
        langCodesForAudio2Text.put(Conversation.LANGUAGE_KR, "ko");
    }

    /**
     *
     * @param language
     * @return
     */
    public static String getLangCodeForAudio2Text(String language){
        String langCode = langCodesForAudio2Text.get(language);
        return JUtilString.isBlank(langCode) ? "" : langCode;
    }

    public OpenAIProvider(){
    }

    @Override
    public String getProviderId() {
        return FOZU.PROVIDER_OPENAI;
    }

    @Override
    public boolean thinkingEnabled() {
        return false;
    }

    @Override
    public boolean deepResearchEnabled() {
        return true;
    }

    @Override
    public boolean onlineSearchEnabled() {
        return true;
    }

    @Override
    public boolean streamingEnabled() {
        return true;
    }

    @Override
    public Request initPost(String url, String json) throws Exception{
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer "+this.getParameter("key"))
                .post(requestBody);

        if(!JUtilString.isBlank(this.getParameter("org"))){
            builder.addHeader("OpenAI-Organization", this.getParameter("org"));
        }

        if(!JUtilString.isBlank(this.getParameter("prj"))){
            builder.addHeader("OpenAI-Project", this.getParameter("prj"));
        }

        return builder.build();
    }

    @Override
    public Request initGet(String url) throws Exception{
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer "+this.getParameter("key"))
                .get();

        if(!JUtilString.isBlank(this.getParameter("org"))){
            builder.addHeader("OpenAI-Organization", this.getParameter("org"));
        }

        if(!JUtilString.isBlank(this.getParameter("prj"))){
            builder.addHeader("OpenAI-Project", this.getParameter("prj"));
        }

        return builder.build();
    }

    @Override
    protected JResponse readStream(Request request, ConversationConf conf, McpSyncServerExchange exchange){
        String uuid = JUtilUUID.genUUID();
        List<String> segments = new ArrayList<>();
        EventSource.Factory factory = EventSources.createFactory(client);
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                log.log("SSE connection opened", -1);
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                log.log("Event type: " + type, -1);
                log.log("Data: " + data, -1);
                segments.add(data);
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.log("SSE connection closed", -1);
                JResponse response = new JResponse(false, "1", "");
                response.putData("response", JArray.toString(segments));
                Waitings.setResult(uuid, response);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                Waitings.setResult(uuid, new JResponse(false, "err", "请求失败"));
            }
        });

        Waitings.waiting(uuid, 1800000, null);
        return (JResponse)Waitings.getResult(uuid);
    }

    public static final String MODEL_GPT5_4 = "gpt-5.4";
    public static final String MODEL_GPT5_4_PRO = "gpt-5.4-pro";
    public static final String MODEL_GPT5_4_MINI = "gpt-5.4-mini";
    public static final String MODEL_GPT5_4_NANO = "gpt-5.4-nano";
    public static final String MODEL_O3_RESEARCH = "o3-deep-research";
    public static final String MODEL_O4_MINI_DEEP_RESEARCH = "o4-mini-deep-research";

    @Override
    public List<Model> listModels(){
        List<Model> models = new ArrayList<>();

        //text-gen
        models.add(new Model(MODEL_GPT5_4,
                MODEL_GPT5_4,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                2.5d,
                0.25d,
                15d,
                5,
                3,
                1050000,
                128000
        ));



        models.add(new Model(MODEL_GPT5_4_PRO,
                MODEL_GPT5_4_PRO,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                30d,
                0d,
                180d,
                5,
                1,
                1050000,
                128000
        ));

        models.add(new Model(MODEL_GPT5_4_MINI,
                MODEL_GPT5_4_MINI,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                0.75d,
                0.08d,
                4.5d,
                4,
                4,
                400000,
                128000
        ));

        models.add(new Model(MODEL_GPT5_4_NANO,
                MODEL_GPT5_4_NANO,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                0.2d,
                0.02d,
                1.25d,
                3,
                4,
                400000,
                128000
        ));

        //deep-research
        models.add(new Model(MODEL_O3_RESEARCH,
                MODEL_O3_RESEARCH,
                List.of(Conversation.TYPE_DEEP_RESEARCH),
                10d,
                2.5d,
                40d,
                1,
                5,
                200000,
                100000
        ));

        models.add(new Model(MODEL_O4_MINI_DEEP_RESEARCH,
                MODEL_O4_MINI_DEEP_RESEARCH,
                List.of(Conversation.TYPE_DEEP_RESEARCH),
                2d,
                0.5d,
                8d,
                3,
                4,
                200000,
                100000
        ));

        return models;
    }


    @Override
    public Message say(Conversation conversation, ConversationConf conf, String modelId, String pluginId, List<Message> messageList, boolean thinking, boolean onlineSearch) throws Exception{
        Message response = super.say(conversation, conf, modelId, pluginId, messageList, thinking, onlineSearch);
        if(response != null && !response.isSuccess()){
            return response;
        }

        return this.send(conversation, conf, modelId, pluginId, messageList, thinking, onlineSearch);
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param modelId
     * @param pluginId
     * @param messageList
     * @param thinking
     * @param onlineSearch
     * @return
     * @throws Exception
     */
    private Message send(Conversation conversation, ConversationConf conf, String modelId, String pluginId, List<Message> messageList, boolean thinking, boolean onlineSearch) throws Exception{
        try {
            //交互的模型
            Model model = this.getModel(modelId);
            
            //第一条消息
            Message firstMessage = messageList.get(0);

            //会话类型
            int conversationType = firstMessage.getConvType();

            //根据会话类型来确定请求API的url和使用哪个模型
            String url = "";
            if(conversationType==Conversation.TYPE_TRANSLATE){
                url=JUtilString.appendUrl(this.getParameter("provider"), "v1/chat/completions");
                if(model==null || !model.canBeUsedFor(conversationType)) modelId=this.getParameter("model-translate");
            }else if(conversationType==Conversation.TYPE_CHAT){
                url=JUtilString.appendUrl(this.getParameter("provider"), "v1/chat/completions");
                if(model==null || !model.canBeUsedFor(conversationType)) modelId=this.getParameter("model-chat");
            }else{
                url=JUtilString.appendUrl(this.getParameter("provider"), "v1/chat/completions");
                if(model==null || !model.canBeUsedFor(conversationType)) modelId=this.getParameter("model-gen-text");
            }

            model = this.getModel(modelId);
            if(model == null){
                Message response=new Message(null, conversation.getId(), conversation.getBeingId());
                response.setId(JUtilUUID.genUUID());
                response.setWho(Message.WHO_SYSTEM);
                response.setTime(SysUtil.getNow());
                response.setContent("invalid model or unsupported message type.");
                response.setErrorCode("invalid_model_or_unsupported_message_type");
                return response;
            }
            //根据会话类型来确定请求API的url和使用哪个模型 end

            //重新设置新消息的时间，使其按列表顺序排列
            for(int i=0; i<messageList.size(); i++){
                messageList.get(i).setTime(SysUtil.getNow() + 1);
            }

            //保存到消息队列
            for(int i=0; i<messageList.size(); i++){
                Message message = messageList.get(i);
                conversation.saveMessage(message);
            }

            //打印调试日志
            log.log("request "+url+", model -> "+modelId, -1);

            if(conversationType==Conversation.TYPE_TRANSLATE){
                return translate(conversation, conf, model, messageList.get(0), url);
            }

            //根据会话类型调用不同API
            return generateText(conversation, conf, model, messageList, url);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     * 生成文本
     * @param conversation
     * @param model
     * @param messageList
     * @param url
     * @return
     * @throws Exception
     */
    private Message generateText(Conversation conversation, ConversationConf conf, Model model, List<Message> messageList, String url) throws Exception{
        StringBuffer params=new StringBuffer();
        params.append("{\"model\":\""+model.getId()+"\"");
        params.append(",\"messages\":[");
        for(int i=0; i<messageList.size(); i++){
            if(i>0) params.append(",");
            Message message = messageList.get(i);
            params.append(message.toRequestBody4OpenAi());
        }
        params.append("]");

        params.append(",\"n\":1");
        params.append("}");

        log.log("params -> \r\n"+params, -1);
        String responseText = postRequest(url, params.toString());

        log.log("response -> \r\n"+responseText, -1);
        JSONObject responseJson = JUtilJSON.parse(responseText);

        JSONArray choices=JUtilJSON.array(responseJson, "choices");
        if(choices==null || choices.length()==0) return null;

        JSONObject choice=JUtilJSON.get(choices, 0);
        JSONObject respMessage=JUtilJSON.object(choice, "message");
        if(respMessage==null) return null;

        Message response=new Message(null, conversation);
        response.setId(JUtilUUID.genUUID());
        response.setWho(Message.WHO_AI);
        response.setContentType(Message.CONTENT_TYPE_TEXT_PLAIN);
        response.setContent(JUtilJSON.string(respMessage, "content"));
        response.setTime(SysUtil.getNow());

        response.setInteractionId(JUtilJSON.string(responseJson, "id"));
        response.setConvType(messageList.get(0).getConvType());
        response.setProviderId(this.getProviderId());
        response.setModelId(model.getId());

        JSONObject usage=JUtilJSON.object(responseJson, "usage");
        if(usage!=null){
            Integer completion_tokens=JUtilJSON.getInteger(usage, "completion_tokens");
            response.setTokens(completion_tokens==null ? 0 : completion_tokens);
        }

        //将对话回合的两条信息设置为成功（才会入库）
        for(Message message : messageList) {
            message.setSuccess(true);
        }

        response.setSuccess(true);
        conversation.saveMessage(response);
        return response;
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param model
     * @param message
     * @param url
     * @return
     * @throws Exception
     */
    private Message translate(Conversation conversation, ConversationConf conf, Model model, Message message, String url) throws Exception{
        message.setContent(JUtilString.replaceAll(Conversation.getWords(conversation.getConf().chatLanguage(), "translateTo"),
                "{translateTo}",
                conversation.getConf().translateTo()) + message.getContent());

        StringBuffer params=new StringBuffer();
        params.append("{\"model\":\""+model.getId()+"\"");
        params.append(",\"messages\":[");
        params.append("{\"role\": \"system\", \"content\": \"将翻译结果以如下格式的JSON输出：{\"translation\":\"The translation result.\"}");
        params.append(",{\"role\": \"" + message.getWho() + "\", \"content\": \"" + JUtilJSON.convertChars(message.getContent()) + "\"}");
        params.append("]");

        params.append("}");

        log.log("params -> \r\n"+params, -1);
        String responseText = postRequest(url, params.toString());

        log.log("translate response -> \r\n"+responseText, -1);
        JSONObject responseJson = JUtilJSON.parse(responseText);

        JSONArray choices=JUtilJSON.array(responseJson, "choices");
        if(choices==null || choices.length()==0) return null;

        JSONObject choice=JUtilJSON.get(choices, 0);
        JSONObject respMessage=JUtilJSON.object(choice, "message");
        if(respMessage==null) return null;

        //content
        /**
         * {
         *   "candidates": [
         *     "SpaceX's R&D iteration speed is extremely fast."
         *   ]
         * }
         */
        String content = JUtilJSON.string(respMessage, "content");
        JSONObject contentJson = JUtilJSON.parse(content);
        String translation = JUtilJSON.string(contentJson, "translation");
        if(!JUtilString.isBlank(translation)) content = translation;

        Message response=new Message(null, conversation);
        response.setId(JUtilUUID.genUUID());
        response.setWho(Message.WHO_AI);
        response.setContent(content);
        response.setTime(SysUtil.getNow());

        response.setInteractionId(JUtilJSON.string(responseJson, "id"));
        response.setConvType(message.getConvType());
        response.setProviderId(this.getProviderId());
        response.setModelId(model.getId());

        JSONObject usage=JUtilJSON.object(responseJson, "usage");
        if(usage!=null){
            Integer completion_tokens=JUtilJSON.getInteger(usage, "completion_tokens");
            response.setTokens(completion_tokens==null ? 0 : completion_tokens);
        }

        //将对话回合的两条信息设置为成功（才会入库）
        message.setSuccess(true);
        response.setSuccess(true);
        conversation.saveMessage(response);

        return response;
    }
}