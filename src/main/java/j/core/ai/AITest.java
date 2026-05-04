package j.core.ai;

import j.core.common.Global;
import j.core.nvwa.Nvwa;

import java.util.ArrayList;
import java.util.List;

public class AITest {
    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        Nvwa.startup();
        while(!Nvwa.isScanned()){
            Global.sleep1000Millis();
            System.out.println("等待系统完成启动......");
        }

        Message m1=new Message();
        m1.setWho(Message.WHO_USER);
        m1.setContent("我们是共产主义接班人");
        m1.setExtraText("n", "1");

        Message m2=new Message();
        m2.setWho(Message.WHO_AI);
        m2.setContent("hello, world!");

        List<Message> ms=new ArrayList<>();
        ms.add(m1);
        ms.add(m2);

        FOZU.init();

        //等待会话重载
        try{
            Thread.sleep(5000);
        }catch (Exception e){}

        Being being=FOZU.createBeing("JIAO XIAO",
                Being.LIFE_PERSISTENT,
                "null",
                "GeNie",
                "Hello AI");

        System.out.println(being.getProviderId());
        System.out.println(being.getClass().getCanonicalName());

        being.setTraits("humorous");
        being.setExpertIn("playing basketball");

        Conversation c=being.startConversation(Conversation.LIFE_PERSISTENT,
                null,
                "AI TEST",
                "It's indeed a test!",
                "{}",
                null,
                false);
        System.out.println(c.getClass().getCanonicalName());

        c.getConf().set(ConversationConf.CONF_CHAT_LANGUAGE, Conversation.LANGUAGE_CH);
        c.getConf().set(ConversationConf.CONF_TRANSLATE_TO, Conversation.LANGUAGE_EN);
        c.getConf().set(ConversationConf.CONF_TRANSLATE_TO_AUDIO, "false");

        Message m=new Message();
        m.setWho(Message.WHO_USER);
        m.setContent("请收集马斯克SpaceX星舰的不锈钢材料用的哪个或哪些型号的不锈钢，这些不锈钢材料是哪家公司或哪几家公司生产的，这些不锈钢材料各方面的性能指标是怎么样的，比如屈服强度等，这些不锈钢材料的制造工艺是怎样的？需要列出引用的参考文献，参考文献的地址必须为其原始来源地址（而不是通过搜索引擎跳转的地址），最后请用中文、按如下格式范例以json格式输出研究报告和参考文献：{\"summary\":\"研究报告正文\",\"sources\":[{\"title\":\"参考文献的标题\",\"url\":\"参考文献来源的原始网址\"},{\"title\":\"参考文献的标题\",\"url\":\"参考文献来源的原始网址\"}]}。");
        //m.setContent("请收集马斯克SpaceX星舰的不锈钢材料用的哪个或哪些型号的不锈钢，这些不锈钢材料是哪家公司或哪几家公司生产的，这些不锈钢材料各方面的性能指标是怎么样的，比如屈服强度等，这些不锈钢材料的制造工艺是怎样的？需要收集详细、可靠的技术资料，并标明所有资料和数据的出处。请用中文。");
        m.setConvType(Conversation.TYPE_DEEP_RESEARCH);
        System.out.println(m.getContent());

        ConversationConf conf = new ConversationConf();
        conf.setStreaming(true);
        //conf.getStructuredOutputSettings().put("responseMimeType", "application/json");
        //conf.getStructuredOutputSettings().put("responseJsonSchema", "{\"type\":\"object\",\"properties\":{\"summary\":{\"type\":\"string\",\"description\":\"The research summaries\"},\"sources\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"title\":{\"type\":\"string\",\"description\":\"The title of webpage, document, or other source.\"},\"url\":{\"type\":\"string\",\"description\":\"The original url of source.\"}},\"required\":[\"title\",\"url\"]}}},\"required\":[\"summary\",\"sources\"]}");

        Message response=Provider.say(c, conf, FOZU.PROVIDER_GEMINI, "deep-research-preview-04-2026", null, m);

        System.out.println("结果---->\r\n"+response.toString());
    }
}
