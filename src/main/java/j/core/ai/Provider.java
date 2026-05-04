package j.core.ai;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import j.core.ai.plugin.Plugin;
import j.core.ai.plugin.Plugins;
import j.core.ai.provider.openai.OpenAIProvider;
import j.core.hp.asynchronous.Waitings;
import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.core.web.handler.JResponse;
import j.log.Logger;
import j.util.JUtilString;
import j.util.JUtilUUID;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Provider extends NvwaAncestor {
    private static Logger log=Logger.create(Provider.class);//日志输出

    //http请求
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    protected OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(1800, TimeUnit.SECONDS)
            .readTimeout(1800, TimeUnit.SECONDS)
            .writeTimeout(1800, TimeUnit.SECONDS)
            .callTimeout(1800, TimeUnit.SECONDS)
            .build();

    /**
     *
     * @return
     */
    public abstract String getProviderId();

    /**
     * 是否支持思考模式
     * @return
     */
    public abstract boolean thinkingEnabled();

    /**
     * 是否支持深度研究模式
     * @return
     */
    public abstract boolean deepResearchEnabled();

    /**
     * 是否支持联网搜索
     * @return
     */
    public abstract boolean onlineSearchEnabled();

    /**
     * 是否支持流式响应
     * @return
     */
    public abstract boolean streamingEnabled();

    /**
     *
     * @throws Exception
     */
    public void initHttp() throws Exception{
        if (this.client == null) {
            this.client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(1800, TimeUnit.SECONDS)
                    .writeTimeout(1800, TimeUnit.SECONDS)
                    .callTimeout(1800, TimeUnit.SECONDS)
                    .build();
        }
    }

    /**
     *
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public Request initPost(String url, String json) throws Exception{
        RequestBody requestBody = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return request;
    }

    /**
     *
     * @param url
     * @return
     * @throws Exception
     */
    public Request initGet(String url) throws Exception{
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return request;
    }

    /**
     * http get
     * @param url
     * @return
     * @throws Exception
     */
    protected String getRequest(String url) throws Exception{
        Request request = this.initGet(url);
        Response response = client.newCall(request).execute();
        return response.isSuccessful() ? response.body().string() : "";
    }

    /**
     * http post
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    protected String postRequest(String url, String json) throws Exception{
        Request request = this.initPost(url, json);
        Response response = client.newCall(request).execute();
        return response.isSuccessful() ? response.body().string() : "";
    }

    /**
     *
     * @param request
     * @param conf
     * @param exchange
     * @return
     * @throws Exception
     */
    protected abstract JResponse readStream(Request request, ConversationConf conf, McpSyncServerExchange exchange) throws Exception;

    /**
     * 可用模型
     * @return
     */
    public List<Model> listModelsUsedFor(Integer usedFor){
        List<Model> list = this.listModels();
        if(list==null || list.isEmpty()) return null;

        List<Model> matched = new ArrayList<>();
        for(Model model : list){
            if(model.canBeUsedFor(usedFor)) matched.add(model.cloneMe());
        }

        return matched;
    }
    /**
     * 可用模型列表
     * @return
     */
    public abstract List<Model> listModels();

    /**
     *
     * @param modelId
     * @return
     */
    public Model getModel(String modelId){
        if(JUtilString.isBlank(modelId)) return null;

        List<Model> models = this.listModels();
        for(Model model : models){
            if(model.getId().equalsIgnoreCase(modelId)) return model;
        }
        return null;
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param modelId
     * @param pluginId
     * @param message
     * @return
     * @throws Exception
     */
    public Message say(Conversation conversation, ConversationConf conf, String modelId, String pluginId, Message message) throws Exception{
        return say(conversation, conf, modelId, pluginId, message, false, false);
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param modelId
     * @param pluginId
     * @param message
     * @param thinking
     * @param onlineSearch
     * @return
     * @throws Exception
     */
    public Message say(Conversation conversation, ConversationConf conf, String modelId, String pluginId, Message message, boolean thinking, boolean onlineSearch) throws Exception{
        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        return say(conversation, conf, modelId, pluginId, messageList, thinking, onlineSearch);
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param modelId
     * @param pluginId
     * @param messageList
     * @return
     * @throws Exception
     */
    public Message say(Conversation conversation, ConversationConf conf, String modelId, String pluginId, List<Message> messageList) throws Exception{
        return say(conversation, conf, modelId, pluginId, messageList, false, false);
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
    public Message say(Conversation conversation, ConversationConf conf, String modelId, String pluginId, List<Message> messageList, boolean thinking, boolean onlineSearch) throws Exception{
        if(messageList==null || messageList.isEmpty()){
            Message response=new Message(null, conversation.getId(), conversation.getBeingId());
            response.setId(JUtilUUID.genUUID());
            response.setWho(Message.WHO_SYSTEM);
            response.setTime(SysUtil.getNow());
            response.setContent("no message tobe sent(3*)");
            response.setErrorCode("empty_message");
            return response;
        }

        conversation.checkStatus();
        conversation.setLatestActive();

        FOZU.updateConversationStatus(conversation);

        Model model = this.getModel(modelId);
        if(!JUtilString.isBlank(modelId)) {
            if (model == null) {
                Message response = new Message(null, conversation.getId(), conversation.getBeingId());
                response.setId(JUtilUUID.genUUID());
                response.setWho(Message.WHO_SYSTEM);
                response.setTime(SysUtil.getNow());
                response.setContent("model " + modelId + " is not exists.");
                response.setErrorCode("invalid_model_id");
                return response;
            }
        }

        Plugin plugin = Plugins.getPlugin(pluginId);
        if(!JUtilString.isBlank(pluginId)) {
            if (plugin == null) {
                Message response = new Message(null, conversation.getId(), conversation.getBeingId());
                response.setId(JUtilUUID.genUUID());
                response.setWho(Message.WHO_SYSTEM);
                response.setTime(SysUtil.getNow());
                response.setContent("plugin " + pluginId + " is not exists.");
                response.setErrorCode("invalid_plugin_id");
                return response;
            }
        }

        for(Message message : messageList) {
            message.setConversationId(conversation.getId());
            message.setBeingId(conversation.getBeingId());
            message.setProviderId(this.getProviderId());
            message.setModelId(modelId);
        }

        if(!this.thinkingEnabled()) thinking=false;
        if(!this.onlineSearchEnabled()) onlineSearch=false;

        return null;
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param providerId
     * @param modelId
     * @param pluginId
     * @param message
     * @return
     * @throws Exception
     */
    public static Message say(Conversation conversation, ConversationConf conf, String providerId, String modelId, String pluginId, Message message) throws Exception{
        return say(conversation, conf, providerId, modelId, pluginId, message, false, false);
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param providerId
     * @param modelId
     * @param pluginId
     * @param message
     * @param thinking
     * @param onlineSearch
     * @return
     * @throws Exception
     */
    public static Message say(Conversation conversation, ConversationConf conf, String providerId, String modelId, String pluginId, Message message, boolean thinking, boolean onlineSearch) throws Exception{
        List<Message> messageList = new ArrayList<>();
        messageList.add(message);
        return say(conversation, conf, providerId, modelId, pluginId, messageList, thinking, onlineSearch);
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param providerId
     * @param modelId
     * @param pluginId
     * @param messageList
     * @return
     * @throws Exception
     */
    public static Message say(Conversation conversation, ConversationConf conf, String providerId, String modelId, String pluginId, List<Message> messageList) throws Exception{
        return say(conversation, conf, providerId, modelId, pluginId, messageList, false, false);
    }

    /**
     *
     * @param conversation
     * @param conf
     * @param providerId
     * @param modelId
     * @param pluginId
     * @param messageList
     * @param thinking
     * @param onlineSearch
     * @return
     * @throws Exception
     */
    public static Message say(Conversation conversation, ConversationConf conf, String providerId, String modelId, String pluginId, List<Message> messageList, boolean thinking, boolean onlineSearch) throws Exception{
        Provider provider = FOZU.getProvider(providerId);
        if(provider==null){
            Message response = new Message(null, conversation.getId(), conversation.getBeingId());
            response.setId(JUtilUUID.genUUID());
            response.setWho(Message.WHO_SYSTEM);
            response.setTime(SysUtil.getNow());
            response.setContent("provider " + providerId + " is not exists.");
            response.setErrorCode("invalid_provider_id");
            return response;
        }

        //插件预处理
        Plugin plugin = Plugins.getPlugin(pluginId);
        if(plugin != null){
            messageList = plugin.inputHandle(conversation, conf, provider, messageList);
            if(messageList==null || messageList.isEmpty()){
                Message response=new Message(null, conversation.getId(), conversation.getBeingId());
                response.setId(JUtilUUID.genUUID());
                response.setWho(Message.WHO_SYSTEM);
                response.setTime(SysUtil.getNow());
                response.setContent("no message tobe sent(1*)");
                response.setErrorCode("empty_message_after_plugin_handled");
                return response;
            }
        }

        List<Message> inputs = new ArrayList<>();
        for(Message message : messageList){
            if(!JUtilString.isBlank(message.getPluginId())){
                plugin = Plugins.getPlugin(message.getPluginId());
                if(plugin != null){
                    List<Message> handled = plugin.inputHandle(conversation, conf, provider, message);
                    if(handled != null && !handled.isEmpty()) inputs.addAll(handled);
                }
            }else{
                inputs.add(message);
            }
        }

        if(inputs==null || inputs.isEmpty()){
            Message response=new Message(null, conversation.getId(), conversation.getBeingId());
            response.setId(JUtilUUID.genUUID());
            response.setWho(Message.WHO_SYSTEM);
            response.setTime(SysUtil.getNow());
            response.setContent("no message tobe sent(2*)");
            response.setErrorCode("empty_message_after_plugin_handled");
            return response;
        }

        log.log("provider => " + provider.getProviderId(), -1);
        Message response = provider.say(conversation, conf, modelId, pluginId, inputs, thinking, onlineSearch);
        if(response != null){
            plugin = Plugins.getPlugin(pluginId);
            if(plugin != null) response = plugin.outputHandle(conversation, conf, provider, inputs, response);
        }

        return response;
    }
}
