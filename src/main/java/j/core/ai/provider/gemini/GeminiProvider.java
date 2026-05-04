package j.core.ai.provider.gemini;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import j.core.ai.*;
import j.core.annotation.configuration.Properties;
import j.core.annotation.nvwa.Nvwa;
import j.core.hp.asynchronous.Waitings;
import j.core.sys.SysUtil;
import j.core.web.handler.JResponse;
import j.log.Logger;
import j.util.JUtilBean;
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
import java.util.Map;

@Nvwa
@Properties(path = "gemini.properties")
public class GeminiProvider extends Provider {
    private static Logger log=Logger.create(GeminiProvider.class);//日志输出

    @Override
    public String getProviderId() {
        return FOZU.PROVIDER_GEMINI;
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

    public GeminiProvider(){
    }

    @Override
    public Request initPost(String url, String json) throws Exception{
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", this.getParameter("key"))
                .header("Accept-Encoding", "identity")
                .post(requestBody);

        return builder.build();
    }

    @Override
    public Request initGet(String url) throws Exception{
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", this.getParameter("key"))
                .header("Accept-Encoding", "identity")
                .get();

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
                /**
                 * {
                 *   "interaction": {
                 *     "id": "v1_Chc2Mzd4YWJTOEY3aVZfdU1QMk5pTC1RYxIXNjM3eGFiUzhGN2lWX3VNUDJOaUwtUWM",
                 *     "status": "in_progress",
                 *     "object": "interaction",
                 *     "agent": "deep-research-preview-04-2026"
                 *   },
                 *   "event_type": "interaction.start"
                 * }
                 */
                log.log("Event type: " + type, -1);
                log.log("Data: " + data, -1);
                segments.add(data);

                if("interaction.start".equals(type)){
                    JSONObject jsonObject = JUtilJSON.parse(data);
                    JSONObject interaction = JUtilJSON.object(jsonObject, "interaction");
                    String interactionId = JUtilJSON.string(interaction, "id");
                    log.log("interactionId: " + interactionId, -1);
                    conf.set("interactionId", interactionId);
                }

                // 使用 loggingNotification 模拟实时发送数据片段
                if(exchange != null){
                    exchange.loggingNotification(new io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification(
                            io.modelcontextprotocol.spec.McpSchema.LoggingLevel.INFO,
                            "stream",
                            data
                    ));
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.log("SSE connection closed", -1);
                JResponse response = new JResponse(false, "1", "");
                response.putData("segments", segments);
                Waitings.setResult(uuid, response);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                log.log("请求失败: "+t.getMessage(), Logger.LEVEL_ERROR);
                Waitings.setResult(uuid, new JResponse(false, "err", "请求失败"));
            }
        });

        Waitings.waiting(uuid, 1800000, null);
        return (JResponse)Waitings.getResult(uuid);
    }

    public static final String MODEL_V3_1_PRO_PREVIEW = "gemini-3.1-pro-preview";
    public static final String MODEL_V3_FLASH_PREVIEW = "gemini-3-flash-preview";
    public static final String MODEL_V3_FLASH_LITE_PREVIEW = "gemini-3.1-flash-lite-preview";
    public static final String MODEL_DEEP_RESEARCH_PREVIEW_04_2026 = "deep-research-preview-04-2026";
    public static final String MODEL_DEEP_RESEARCH_MAX_PREVIEW_04_2026 = "deep-research-max-preview-04-2026";

    @Override
    public List<Model> listModels(){
        List<Model> models = new ArrayList<>();

        //text-gen
        models.add(new Model(MODEL_V3_1_PRO_PREVIEW,
                MODEL_V3_1_PRO_PREVIEW,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                2d,
                0d,
                15d,
                0,
                0,
                1048576,
                65536
        ));

        models.add(new Model(MODEL_V3_FLASH_PREVIEW,
                MODEL_V3_FLASH_PREVIEW,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                0.5d,
                0d,
                3d,
                0,
                0,
                1048576,
                65536
        ));

        models.add(new Model(MODEL_V3_FLASH_LITE_PREVIEW,
                MODEL_V3_FLASH_LITE_PREVIEW,
                List.of(Conversation.TYPE_GENERATION_TEXT, Conversation.TYPE_CHAT, Conversation.TYPE_TRANSLATE),
                0.25d,
                0d,
                1.5d,
                0,
                0,
                1048576,
                65536
        ));

        //deep-research
        models.add(new Model(MODEL_DEEP_RESEARCH_PREVIEW_04_2026,
                MODEL_DEEP_RESEARCH_PREVIEW_04_2026,
                List.of(Conversation.TYPE_DEEP_RESEARCH),
                0d,
                0d,
                3d,
                0,
                0,
                1048576,
                65536
        ));

        models.add(new Model(MODEL_DEEP_RESEARCH_MAX_PREVIEW_04_2026,
                MODEL_DEEP_RESEARCH_MAX_PREVIEW_04_2026,
                List.of(Conversation.TYPE_DEEP_RESEARCH),
                0d,
                0d,
                7d,
                0,
                0,
                1048576,
                65536
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
            if(conversationType==Conversation.TYPE_DEEP_RESEARCH){
                url=JUtilString.appendUrl(this.getParameter("provider"), "v1beta/interactions");
                if(model==null || !model.canBeUsedFor(conversationType)) modelId = this.getParameter("model-deepresearch");
            }else{
                if(model==null || !model.canBeUsedFor(conversationType)) modelId = this.getParameter("model-gen-text");
                url=JUtilString.appendUrl(this.getParameter("provider"), "v1beta/models/"+modelId+":generateContent");
            }

            model = this.getModel(modelId);
            if(model == null){
                Message response=new Message(null, conversation.getId(), conversation.getBeingId());
                response.setId(JUtilUUID.genUUID());
                response.setWho(Message.WHO_SYSTEM);
                response.setTime(SysUtil.getNow());
                response.setContent("invalid model or unsupported message type.");
                response.setErrorCode("invalid_model_or_unsupported_message_type");
                response.setConvType(firstMessage.getConvType());
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

            //根据会话类型调用不同API
            if(conversationType==Conversation.TYPE_DEEP_RESEARCH){
                return deepResearch(conversation, conf, model, messageList, url);
            }

            return generateText(conversation, conf, model, messageList, url);
            //根据会话类型调用不同API end
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param model
     * @param messageList
     * @param url
     * @return
     * @throws Exception
     */
    private Message deepResearch(Conversation conversation, ConversationConf conf, Model model, List<Message> messageList, String url) throws Exception{
        Message firstMessage = messageList.get(0);

        StringBuffer params=new StringBuffer();
        params.append("{\"input\": \"" + JUtilJSON.convertChars(firstMessage.getContent()) + "\"");
        params.append(",\"agent\": \"" + model.getId() + "\"");
        params.append(",\"background\": true");
        params.append(",\"stream\": " + (conf!=null && conf.isStreaming()));

        params.append(", \"agent_config\": {");
        params.append("\"type\": \"deep-research\"");
        params.append(", \"thinking_summaries\": \"auto\"");
        params.append("}");

        params.append("}");

        log.log("params -> \r\n"+params, -1);
        String responseText = null;
        String interactionId = null;
        if(conf!=null && conf.isStreaming()){
            Request request = this.initPost(url, params.toString());
            this.readStream(request, conf, conf.getExchange());

            interactionId = conf.get("interactionId");
            if(JUtilString.isBlank(interactionId)){
                Message response=new Message(null, conversation.getId(), conversation.getBeingId());
                response.setId(JUtilUUID.genUUID());
                response.setWho(Message.WHO_SYSTEM);
                response.setTime(SysUtil.getNow());
                response.setContent("流式调用失败.");
                response.setErrorCode("call_api_failed_in_streaming_mode");
                response.setConvType(firstMessage.getConvType());
                return response;
            }
        }else{
            responseText = postRequest(url, params.toString());

            log.log("response -> \r\n"+responseText, -1);
            JSONObject responseJson = JUtilJSON.parse(responseText);
            interactionId = JUtilJSON.string(responseJson, "id");
        }

        /**
         * {
         *     "id": "v1_ChdzZnpvYWR1WkVlckdqTWNQMzVTV29RZxIXc2Z6b2FkdVpFZXJHak1jUDM1U1dvUWc",
         *     "status": "in_progress",
         *     "role": "agent",
         *     "created": "2026-04-22T16:52:01Z",
         *     "updated": "2026-04-22T16:52:01Z",
         *     "object": "interaction",
         *     "agent": "deep-research-pro-preview-12-2025"
         * }
         */

        if(JUtilString.isBlank(interactionId)) return null;

        String interactionResultUrl=JUtilString.appendUrl(this.getParameter("provider"), "v1beta/interactions/"+interactionId);
        responseText = getRequest(interactionResultUrl);
        JSONObject responseJson = JUtilJSON.parse(responseText);
        String interactionStatus = JUtilJSON.string(responseJson, "status");

        while("in_progress".equals(interactionStatus)){
            try {
                Thread.sleep(3000);
            }catch (Exception e){}

            responseText = getRequest(interactionResultUrl);

            log.log("response result -> \r\n"+responseText, -1);
            responseJson = JUtilJSON.parse(responseText);
            interactionStatus = JUtilJSON.string(responseJson, "status");
        }

        if(!"completed".equals(interactionStatus)){
            Message response=new Message(null, conversation.getId(), conversation.getBeingId());
            response.setId(JUtilUUID.genUUID());
            response.setWho(Message.WHO_SYSTEM);
            response.setTime(SysUtil.getNow());
            response.setContent("未获得完成状态的结果");
            response.setErrorCode("ai_no_completed_result");
            response.setConvType(firstMessage.getConvType());
            return response;
        }

        JSONArray outputs = JUtilJSON.array(responseJson, "outputs");

        String text = "";
        List<String> thought = new ArrayList<>();
        for(int i=0; i<outputs.length(); i++){
            JSONObject output = JUtilJSON.get(outputs, i);
            String type = JUtilJSON.string(output, "type");

            if("text".equals(type)){
                if(JUtilString.isBlank(text)) text = JUtilJSON.string(output, "text");
            }else if("thought".equals(type)){
                JSONArray summaries = JUtilJSON.array(output, "summary");
                for(int j=0; j<summaries.length(); j++){
                    JSONObject summary = JUtilJSON.get(summaries, j);
                    String summaryText = JUtilJSON.string(summary, "text");
                    if(!JUtilString.isBlank(summaryText)) thought.add(summaryText);
                }
            }
        }

        Message response=new Message(null, conversation);
        response.setId(JUtilUUID.genUUID());
        response.setWho(Message.WHO_AI);
        response.setContentType(Message.CONTENT_TYPE_TEXT_PLAIN);
        response.setContent(text);
        response.setTime(SysUtil.getNow());

        response.setExtraText("thought", JUtilBean.beans2Json(thought));

        response.setInteractionId(interactionId);
        response.setConvType(messageList.get(0).getConvType());
        response.setProviderId(this.getProviderId());
        response.setModelId(model.getId());

        JSONObject usage=JUtilJSON.object(responseJson, "usage");
        if(usage!=null){
            Integer total_tool_use_tokens=JUtilJSON.getInteger(usage, "total_tool_use_tokens");
            response.setTokens(total_tool_use_tokens==null ? 0 : total_tool_use_tokens);
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
     * @param messageList
     * @param url
     * @return
     * @throws Exception
     */
    private Message generateText(Conversation conversation, ConversationConf conf, Model model, List<Message> messageList, String url) throws Exception{
        StringBuffer params=new StringBuffer();
        params.append("{\"contents\": [");
        for(int i=0; i<messageList.size(); i++){
            if(i>0) params.append(",");
            Message message = messageList.get(i);
            params.append(message.toRequestBody4Gemini());
        }
        params.append("]");

        params.append(", \"generationConfig\": {");
        params.append("\"thinkingConfig\": {\"thinkingLevel\": \"low\"}");

        Map<String, Object> structuredOutputSettings = conf.getStructuredOutputSettings();
        if(structuredOutputSettings.containsKey("responseMimeType") && structuredOutputSettings.containsKey("responseJsonSchema")){
            params.append(", \"responseMimeType\": \""+structuredOutputSettings.get("responseMimeType")+"\"");
            params.append(", \"responseJsonSchema\": "+structuredOutputSettings.get("responseJsonSchema"));
        }

        params.append("}");

        params.append("}");

        log.log("params -> \r\n"+params, -1);
        String responseText = postRequest(url, params.toString());

        log.log("response -> \r\n"+responseText, -1);
        JSONObject responseJson = JUtilJSON.parse(responseText);

        JSONArray choices=JUtilJSON.array(responseJson, "candidates");
        if(choices==null || choices.length()==0) return null;

        JSONObject choice=JUtilJSON.get(choices, 0);
        JSONObject content=JUtilJSON.object(choice, "content");
        if(content==null) return null;

        JSONArray parts=JUtilJSON.array(content, "parts");
        if(parts==null || parts.length()==0) return null;

        String text = JUtilJSON.string(parts.getJSONObject(0), "text");
        if(JUtilString.isBlank(text)) return null;

        Message response=new Message(null, conversation);
        response.setId(JUtilUUID.genUUID());
        response.setWho(Message.WHO_AI);
        response.setContentType(Message.CONTENT_TYPE_TEXT_PLAIN);
        response.setContent(text);
        response.setTime(SysUtil.getNow());

        response.setInteractionId(JUtilJSON.string(responseJson, "id"));
        response.setConvType(messageList.get(0).getConvType());
        response.setProviderId(this.getProviderId());
        response.setModelId(model.getId());

        JSONObject usage=JUtilJSON.object(responseJson, "usageMetadata");
        if(usage!=null){
            Integer totalTokenCount=JUtilJSON.getInteger(usage, "totalTokenCount");
            response.setTokens(totalTokenCount==null ? 0 : totalTokenCount);
        }

        //将对话回合的两条信息设置为成功（才会入库）
        for(Message message : messageList) {
            message.setSuccess(true);
        }

        response.setSuccess(true);
        conversation.saveMessage(response);
        return response;
    }
}