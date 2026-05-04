package j.core.ai.plugin;

import j.core.ai.Conversation;
import j.core.ai.ConversationConf;
import j.core.ai.Message;
import j.core.ai.Provider;

import java.util.List;

public interface Plugin {
    public String getPluginId();
    public String getPluginName();
    public String getPluginDesc();

    public Conversation onConversationStarted(Conversation conversation);
    public Conversation onConversationEnded(Conversation conversation);
    public List<Message> inputHandle(Conversation conversation, ConversationConf conf, Provider provider, List<Message> inputs);
    public List<Message> inputHandle(Conversation conversation, ConversationConf conf, Provider provider, Message input);
    public Message outputHandle(Conversation conversation, ConversationConf conf, Provider provider, List<Message> inputs, Message response);
}
