package j.core.ai;

import j.log.Logger;
import j.util.JUtilString;

import java.util.ArrayList;
import java.util.List;

public final class Tokenizer {
    private static Logger log=Logger.create(Tokenizer.class);//日志输出

    /**
     * 计算一条消息的token数
     * @param provider
     * @param model
     * @param message
     * @return
     */
    public static int countTokens(Provider provider, Model model, Message message){
        List<Message> messages=new ArrayList<>();
        messages.add(message);
        return countTokens(provider, model, messages);
    }

    /**
     * 计算消息列表的token数之和
     * @param provider
     * @param model
     * @param messages
     * @return
     */
    public static int countTokens(Provider provider, Model model, List<Message> messages){
        int tokens_per_message = 3;
        int tokens_per_name = 1;

        int num_tokens=0;
        for(int i=0; i<messages.size(); i++){
            num_tokens += tokens_per_message;

            Message m=messages.get(i);
            String role=m.getWho();
            String name=m.getName();
            String content=m.getContent();

            num_tokens += countTokens(role);
            num_tokens += countTokens(content);
            if(!JUtilString.isBlank(name)){
                num_tokens += countTokens(name);
                num_tokens += tokens_per_name;
            }
        }
        num_tokens += 3;//every reply is primed with <|start|>assistant<|message|>
        return num_tokens;
    }

    /**
     * 计算一条文本消息的token数
     * @param str
     * @return
     */
    public static int countTokens(String str){
        double count = 0d;
        for(int i=0; i<str.length(); i++){
            String ch = str.substring(i, i+1);
            count += ch.matches(JUtilString.RegExpCharCn) ? 0.6 : 0.3;
        }

        return (int)Math.ceil(count);
    }

    /**
     * 当前会话上下文中，下一条消息可用的最大token数
     * @param model
     * @param sentTokens
     * @return
     */
    public static int calcMaxTokens(Model model, int maxTokens, int sentTokens){
        return Math.min(maxTokens, model.getInputTokenLimit() - sentTokens);
    }
}