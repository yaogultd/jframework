package j.core.ai;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.dao.DAO;
import j.core.dao.DAOs;
import j.core.dao.DB;
import j.core.dao.util.SQLUtil;
import j.core.db.JaiBeing;
import j.core.db.JaiConversation;
import j.core.db.JaiMessage;
import j.core.nvwa.Nvwa;
import j.log.Logger;
import j.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@ClassDescription(author = "盖聂大叔（肖炯）",
        date = "2023-04-17",
        description = "")
public class FOZU implements Runnable{
    private static Logger log=Logger.create(FOZU.class);//日志输出

    //以对接提供商ID
    public static final String PROVIDER_OPENAI ="openai";//openai
    public static final String PROVIDER_CLAUDE="claude";//Claude
    public static final String PROVIDER_GEMINI="gemini";//Gemini
    public static final String PROVIDER_DEEPSEEK="deepseek";//deepseek
    public static final String PROVIDER_DOUBAO="doubao";//doubao
    public static final String PROVIDER_QWEN="qwen";//千问

    //匿名创建者
    public static final String CREATOR_ANONYMOUS="anonymous";

    //提供商实现类
    private static ConcurrentMap<String, Provider> providers=new ConcurrentMap<>();

    //所有AI实例
    private static ConcurrentMap<String, Being> beings=new ConcurrentMap<>();

    static {
        //所有AI服务提供商的实现类（Provider的子类）
        Reflections reflections = new Reflections("j.core.ai.provider");
        Set<Class<? extends Provider>> clazz = reflections.getSubTypesOf(Provider.class);
        clazz.forEach(cls -> {
            try {
                Provider provider = cls.getConstructor().newInstance();

                //每个Being的实现类都需要重写getProviderId方法来提供自己的提供商身份标识
                log.log("ai provider => " + provider.getProviderId(), -1);
                providers.put(provider.getProviderId(), provider);
            } catch (Exception e) {
                log.log(e, Logger.LEVEL_ERROR);
            }
        });

        //启动监视线程
        (new Thread(new FOZU())).start();
        log.log("FOZU started.", -1);
    }

    /**
     * 调用此方式触发AI框架的启动
     */
    public static void init() {
        log.log("init FOZU...", -1);
    }

    /**
     *
     * @param providerId
     * @return
     * @throws Exception
     */
    public static Provider getProvider(String providerId) throws Exception{
        if(JUtilString.isBlank(providerId)) throw new Exception("blank provider id");
        if(!providers.containsKey(providerId)) throw new Exception("provider " + providerId +" is not exists");
        return providers.get(providerId);
    }

    /**
     *
     * @return
     */
    public static List<Provider> getProviders(){
        return providers.listValues();
    }

    /**
     * 检查creator是否为预留关键字
     * @param creator
     * @throws Exception
     */
    public static String checkCreator(String creator) throws Exception{
        //if(!JUtilString.isBlank(creator) && creator.equalsIgnoreCase(CREATOR_ANONYMOUS)) throw new Exception(creator + " is an illegal creator id.");
        if(JUtilString.isBlank(creator)) creator = CREATOR_ANONYMOUS;
        else creator = creator.toLowerCase();
        return creator;
    }

    /**
     * 创建一个AI生命
     * @param creator 创建者（比如网站的会员ID）
     * @param lifeType 生命周期类型，Being.LIFE_PERSISTENT / Being.LIFE_TEMPORARY，分别表示持久（存入数据库）、临时
     * @param id AI生命ID
     * @param name AI生命名称
     * @param desc AI生命描述
     * @return
     * @throws Exception
     */
    public static Being createBeing(String creator, int lifeType, String id, String name, String desc) throws Exception{
        creator = checkCreator(creator);

        if(JUtilString.isBlank(id)) id = JUtilUUID.genUUID();
        if(beings.containsKey(id)){
            Being being=beings.get(id);
            if(!JUtilString.isBlank(name)) being.setName(name);
            if(!JUtilString.isBlank(desc)) being.setDesc(desc);
            return being;
        }

        Being being = new Being(creator, lifeType, id, name, desc);

        beings.put(being.getId(), being);
        return being;
    }

    /**
     *
     * 创建一个AI生命
     * @param creator 创建者（比如网站的会员ID）
     * @param lifeType 生命周期类型，Being.LIFE_PERSISTENT / Being.LIFE_TEMPORARY，分别表示持久（存入数据库）、临时
     * @param id AI生命ID
     * @param name AI生命名称
     * @param desc AI生命描述
     * @param lifeStart 生命开始时间
     * @param lifeEnd 生命结束时间，达到结束时间即销毁
     * @param maxIdle 最大空闲时间，仅对临时生命有效，超出最大空闲时间即销毁
     * @return
     * @throws Exception
     */
    public static Being createBeing(String creator, int lifeType, String id, String name, String desc, long lifeStart, long lifeEnd, long maxIdle) throws Exception{
        creator = checkCreator(creator);

        if(JUtilString.isBlank(id)) id = JUtilUUID.genUUID();
        if(beings.containsKey(id)){
            Being being=beings.get(id);
            if(!JUtilString.isBlank(name)) being.setName(name);
            if(!JUtilString.isBlank(desc)) being.setDesc(desc);
            return being;
        }

        Being being = new Being(creator, lifeType, id, name, desc, lifeStart, lifeEnd, maxIdle);

        beings.put(being.getId(), being);
        return being;
    }

    /**
     * 根据ID获取AI生命体
     * @param id AI生命体的ID
     * @return
     */
    public static Being getBeing(String id){
        if(JUtilString.isBlank(id)) return null;
        return beings.get(id);
    }

    /**
     * 获取某个创建者的全部AI生命体
     * @param creator 创建者（比如网站的会员ID）
     * @return
     */
    public static List<Being> getBeingsOfCreator(String creator) throws Exception{
        creator = checkCreator(creator);
        if(JUtilString.isBlank(creator)) return null;

        List<Being> of=new ArrayList<>();
        List<Being> values=beings.listValues();
        for(int i=0; i<values.size(); i++){
            Being being=values.get(i);
            if(creator.equals(being.getCreator())) of.add(being);
        }
        return of;
    }

    /**
     * 持久化（将AI生命体相关信息保存到数据库）
     * @return
     * @throws Exception
     */
    private static boolean persist(Being b) throws Exception{
        if(b.getLifeType() == Being.LIFE_TEMPORARY) return true;

        DAO dao=null;
        try{
            dao = DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, false);

            //保存主记录
            JaiBeing exists=(JaiBeing)dao.findSingle("j_ai_being", "being_id='"+ SQLUtil.deleteCriminalChars(b.getId())+"'");

            //已被删除、终止（见数据库表字段含义）
            if(exists!=null && ("D".equals(exists.getRowDeleted())
                    || "R".equals(exists.getRowDeleted())
                    || "100".equals(exists.getLifeStatus())
                    || "101".equals(exists.getLifeStatus()))){
                DAOs.commit(dao);
                return false;
            }

            JaiBeing being=new JaiBeing();
            being.setBeingId(b.getId());
            being.setBeingCreator(b.getCreator());
            being.setBeingName(b.getName());
            being.setBeingDesc(b.getDesc());
            being.setMaxIdle(b.getMaxIdle());
            being.setBeingAvatar(b.getAvatar());
            being.setBeingTraits(b.getTraits());
            being.setBeingExpertIn(b.getExpertIn());
            being.setLatestActive(b.getLatestActive());
            being.setLifeType(b.getLifeType());
            being.setLifeStart(b.getLifeStart());
            being.setLifeEnd(b.getLifeEnd());

            /*000 未出生
            001 默认/生存中
            100 到时终止
            101 被动销毁*/
            if(b.isDestroyed()) being.setLifeStatus("101");
            else if(b.isEnded()) being.setLifeStatus("100");
            else if(b.isBorn()) being.setLifeStatus("001");
            else being.setLifeStatus("000");

            being.setRowDeleted("N");

            if(exists==null) dao.insert(being);
            else dao.updateByKeysIgnoreNulls(being);

            dao.commit();
            //保存主记录 end

            //保存会话记录
            List<Conversation> conversations=b.getConversations().listValues();
            for(int i=0; i<conversations.size(); i++){
                Conversation c=conversations.get(i);

                JaiConversation cExists=(JaiConversation)dao.findSingle("j_ai_conversation", "conv_id='"+c.getId()+"'");

                //已被删除或终止
                if(cExists!=null && ("D".equals(cExists.getRowDeleted())
                        || "R".equals(cExists.getRowDeleted())
                        || "100".equals(cExists.getLifeStatus())
                        || "101".equals(cExists.getLifeStatus()))){
                    continue;
                }

                dao.beginTransaction();

                JaiConversation conv=new JaiConversation();
                conv.setConvId(c.getId());
                conv.setBeingId(c.getBeingId());
                conv.setConvName(c.getName());
                conv.setConvDesc(c.getDesc());
                conv.setConvConf(c.getConf()==null?"{}":c.getConf().toString());
                conv.setMaxIdle(c.getMaxIdle());
                conv.setLatestActive(c.getLatestActive());
                conv.setLifeType(c.getLifeType());
                conv.setLifeStart(c.getLifeStart());
                conv.setLifeEnd(c.getLifeEnd());

                /*000 未出生
                001 默认/生存中
                100 到时终止
                101 被动销毁*/
                if(c.isEnded()) conv.setLifeStatus("101");
                else if(c.isEnded()) conv.setLifeStatus("100");
                else if(c.isStarted()) conv.setLifeStatus("001");
                else conv.setLifeStatus("000");

                conv.setRowDeleted("N");

                if(cExists==null) dao.insert(conv);
                else dao.updateByKeysIgnoreNulls(conv);

                //保存消息记录
                List<Message> messages=c.getMessages();

                //仅交谈类消息保存
                for (int j = 0; j < messages.size(); j++) {
                    Message m = messages.get(j);
                    if(m.isStored()) continue;//已入库
                    if(!m.isSuccess()) continue;//未成功
                    if(m.isHelloMessage() || m.isByeMessage()) continue;//say hi 和 say bye的消息每次重载会话后根据设定重新生成

                    JaiMessage msg = new JaiMessage();
                    msg.setMessageId(m.getId());
                    msg.setBeingId(conv.getBeingId());
                    msg.setConvId(conv.getConvId());
                    msg.setConvType(m.getConvType()<=0 ? Conversation.TYPE_GENERATION_TEXT : m.getConvType());

                    msg.setInteractionId(m.getInteractionId());
                    msg.setProviderId(m.getProviderId());
                    msg.setModelId(m.getModelId());
                    msg.setPluginId(m.getPluginId());

                    msg.setInteractionId(m.getInteractionId());
                    msg.setMessageWho(m.getWho());
                    msg.setMessageTime(m.getTime());
                    msg.setMessageType(m.getContentType());
                    msg.setMessageContent(JUtilString.replaceAll(m.getContent(), "\n", "\\n"));
                    msg.setStoreType(m.getStoreType());
                    msg.setExtraDatas(JUtilBean.map2Json(m.getExtraDatas()));
                    msg.setExtraTexts(JUtilBean.map2Json(m.getExtraTexts()));
                    msg.setIsChatStart(m.isHelloMessage() ? "T" : "F");
                    msg.setIsChatEnd(m.isByeMessage() ? "T" : "F");
                    msg.setMessageIp(m.getIp());
                    msg.setMessageTokens(m.getTokens());
                    msg.setRowDeleted("N");

                    dao.insertIfNotExists(msg);
                }
                //保存消息记录 end

                dao.commit();

                //标记为已入库
                for(int j=0; j<messages.size(); j++) {
                    Message m = messages.get(j);
                    if(m.isStored()) continue;//已入库
                    if(!m.isSuccess()) continue;//未成功
                    m.setStored(true);
                }
            }
            //保存会话记录 end

            DAOs.commit(dao);
            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
        }
        return false;
    }

    /**
     * 从数据库重载生命体
     * @param exists
     * @return
     * @throws Exception
     */
    private static boolean restore(JaiBeing exists) throws Exception{
        if(exists==null) return false;

        //已被删除或终止
        if("D".equals(exists.getRowDeleted())
                || "R".equals(exists.getRowDeleted())
                || "100".equals(exists.getLifeStatus())
                || "101".equals(exists.getLifeStatus())){
            return false;
        }

        Being being=FOZU.createBeing(exists.getBeingCreator(),
                exists.getLifeType(),
                exists.getBeingId(),
                exists.getBeingName(),
                exists.getBeingDesc(),
                exists.getLifeStart(),
                exists.getLifeEnd(),
                exists.getMaxIdle());
        if(being==null) return false;

        //还原状态
        being.setLatestActive(exists.getLatestActive());
        being.setAvatar(exists.getBeingAvatar());
        being.setTraits(exists.getBeingTraits());
        being.setExpertIn(exists.getBeingExpertIn());

        //重载会话和消息
        DAO dao=null;
        try{
            dao = DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, true);

            int rpp=100;
            int pn=1;
            List<JaiConversation> _conversations=dao.find("j_ai_conversation", "being_id='"+SQLUtil.deleteCriminalChars(exists.getBeingId())+"' and row_deleted='N' order by life_start asc", rpp, pn);
            while(_conversations != null && !_conversations.isEmpty()){
                for(int i=0; i<_conversations.size(); i++){
                    JaiConversation conv=_conversations.get(i);

                    //已被删除或终止
                    if("100".equals(conv.getLifeStatus())
                            || "101".equals(conv.getLifeStatus())){
                        continue;
                    }

                    if(Nvwa.isDebug()) log.log("restoring conversation "+conv.getConvId(), -1);
                    Conversation c=being.startConversation(conv.getLifeType(),
                            conv.getConvId(),
                            conv.getConvName(),
                            conv.getConvDesc(),
                            conv.getConvConf(),
                            conv.getLatestPluginId(),
                            true,
                            conv.getLifeStart(),
                            conv.getLifeEnd(),
                            conv.getMaxIdle());

                    //还原状态
                    c.setConf(new ConversationConf(JUtilJSON.parse(conv.getConvConf())));
                    c.setLatestActive(conv.getLatestActive());

                    //载入最近N条信息
                    List<JaiMessage> messages = dao.find("j_ai_message", "conv_id='" + conv.getConvId() + "' and row_deleted='N' order by message_time desc", Conversation.MAX_MESSAGES_IN_CACHE, 1);
                    for (int j = 0; j < messages.size(); j++) {
                        JaiMessage msg = messages.get(j);

                        if (Nvwa.isDebug()) log.log("restoring message " + msg.getMessageId() + " -> " + msg.getMessageContent(), -1);

                        Message m = new Message(msg.getMessageId(), msg.getConvId(), msg.getBeingId(), msg.getMessageTime(), msg.getMessageType());
                        m.setInteractionId(msg.getInteractionId());
                        m.setStoreType(msg.getStoreType());
                        m.setWho(msg.getMessageWho());
                        m.setTokens(msg.getMessageTokens());
                        m.setContent(msg.getMessageContent());
                        m.setIp(msg.getMessageIp());
                        m.setHelloMessage("T".equalsIgnoreCase(msg.getIsChatStart()));
                        m.setByeMessage("T".equalsIgnoreCase(msg.getIsChatEnd()));

                        m.setProviderId(msg.getProviderId());
                        m.setModelId(msg.getModelId());
                        m.setPluginId(msg.getPluginId());
                        m.setConvType(msg.getConvType()==null || msg.getConvType()<=0 ? Conversation.TYPE_GENERATION_TEXT : msg.getConvType());

                        JSONObject extraDatasJson = JUtilJSON.parse(msg.getExtraDatas());
                        if(extraDatasJson != null && !extraDatasJson.isEmpty()){
                            Iterator<String> keys = extraDatasJson.keys();
                            while(keys.hasNext()){
                                String key = keys.next();
                                MessageData messageData = (MessageData) JUtilBean.json2Bean(MessageData.class, JUtilJSON.object(extraDatasJson, key));
                                if(messageData != null) m.setExtraData(key, messageData);
                            }
                        }

                        m.setExtraTexts(JUtilBean.jsonPlain2Map(JUtilJSON.parse(msg.getExtraDatas())));

                        m.setStored(true);//已经入库的消息
                        m.setSuccess(true);

                        c.saveMessage(m, 0);
                    }
                    //载入最近N条信息 end
                }

                pn++;
                _conversations=dao.find("j_ai_conversation", "being_id='"+SQLUtil.deleteCriminalChars(exists.getBeingId())+"' and row_deleted='N' order by life_start asc", rpp, pn);
            }

            DAOs.commit(dao);
            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
        }
        //重载会话和消息 end

        return false;
    }

    /**
     * 销毁生命体
     * @param being
     * @return
     */
    public static boolean destroy(Being being){
        if(being==null) return false;
        DAO dao=null;
        try{
            dao=DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, false);

            dao.executeSQL("update j_ai_being set row_deleted='D', life_status='101' where being_id='"+being.getId()+"'");
            dao.executeSQL("update j_ai_conversation set row_deleted='D', life_status='101' where being_id='"+being.getId()+"'");
            dao.executeSQL("update j_ai_message set row_deleted='D' where being_id='"+being.getId()+"'");

            DAOs.commit(dao);

            being.destroy();
            FOZU.persist(being);
            beings.remove(being.getId());

            return true;
        }catch (Exception e){
            DAOs.onException(dao);
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     * 更新会话状态
     * @param c
     * @return
     */
    public static boolean updateConversationStatus(Conversation c){
        if(c==null) return false;

        DAO dao=null;
        try{
            dao=DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, false);

            JaiConversation tobeUpdated = new JaiConversation();
            tobeUpdated.setConvId(c.getId());
            tobeUpdated.setLatestActive(c.getLatestActive());
            tobeUpdated.setConvConf(c.getConf()==null ? "{}" : c.getConf().toString());

            dao.updateByKeysIgnoreNulls(tobeUpdated);
            DAOs.commit(dao);
            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
            return false;
        }
    }

    /**
     * 销毁会话
     * @param conversation
     * @return
     */
    public static boolean destroy(Conversation conversation){
        if(conversation==null) return false;

        DAO dao=null;
        try{
            dao=DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, false);

            dao.executeSQL("update j_ai_conversation set row_deleted='D', life_status='101' where conv_id='"+conversation.getId()+"'");
            dao.executeSQL("update j_ai_message set row_deleted='D' where conv_id='"+conversation.getId()+"'");

            conversation.end();
            Being being=getBeing(conversation.getBeingId());
            if(being!=null) being.removeEndedConverstions();

            DAOs.commit(dao);
            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
            return false;
        }
    }

    /**
     * 销毁消息
     * @param message
     * @return
     */
    public static boolean destroy(Message message){
        if(message==null) return false;
        DAO dao=null;
        try{
            dao=DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, false);

            dao.executeSQL("update j_ai_message set row_deleted='D' where message_id='"+message.getId()+"'");

            Being being=FOZU.getBeing(message.getBeingId());
            Conversation conversation=being==null?null:being.getConversation(message.getConversationId());
            if(conversation!=null) conversation.removeMessage(message);

            DAOs.commit(dao);
            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
            return false;
        }
    }

    /**
     * 清空某会话的消息
     * @param conversation
     * @return
     */
    public static boolean clearMessages(Conversation conversation){
        if(conversation==null) return false;
        DAO dao=null;
        try{
            dao=DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, false);

            dao.executeSQL("update j_ai_message set row_deleted='D' where conv_id='"+conversation.getId()+"'");
            conversation.clearMessages();

            DAOs.commit(dao);
            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
            return false;
        }
    }

    /**
     *
     * @param dao
     * @param conversationId
     * @param includeDeleted
     * @param request
     * @return
     */
    public static List<Message> getMessages(DAO dao, String conversationId, boolean includeDeleted, HttpServletRequest request){
        JUtilPagingRppAndPn rppAndPn = JUtilPaging.getRppAndPn(request, 1000);
        return getMessages(dao, conversationId, includeDeleted, rppAndPn.getRpp(), rppAndPn.getPn());
    }

    /**
     *
     * @param dao
     * @param includeDeleted
     * @param rpp
     * @param pn
     * @return
     */
    public static List<Message> getMessages(DAO dao, String conversationId, boolean includeDeleted, int rpp, int pn){
        List<Message> list = new ArrayList<>();
        boolean newDAO = false;
        try{
            if(dao==null || dao.isClosed()){
                newDAO = true;
                dao = DAOs.create(DB.getJFrameworkDB().getName(), FOZU.class, true);
            }

            List<JaiMessage> messages = dao.find("j_ai_message", "conv_id='" + conversationId + "'"+(!includeDeleted ? " and row_deleted='N'" : "")+" order by message_time desc", rpp, pn);
            for (int j = 0; j < messages.size(); j++) {
                JaiMessage msg = messages.get(j);

                Message m = new Message(msg.getMessageId(), msg.getConvId(), msg.getBeingId(), msg.getMessageTime(), msg.getMessageType());
                m.setWho(msg.getMessageWho());
                m.setTokens(msg.getMessageTokens());
                m.setContent(msg.getMessageContent());
                m.setIp(msg.getMessageIp());
                m.setHelloMessage("T".equalsIgnoreCase(msg.getIsChatStart()));
                m.setByeMessage("T".equalsIgnoreCase(msg.getIsChatEnd()));

                JSONObject extraDatasJson = JUtilJSON.parse(msg.getExtraDatas());
                if(extraDatasJson != null && !extraDatasJson.isEmpty()){
                    Iterator<String> keys = extraDatasJson.keys();
                    while(keys.hasNext()){
                        String key = keys.next();
                        MessageData messageData = (MessageData) JUtilBean.json2Bean(MessageData.class, JUtilJSON.object(extraDatasJson, key));
                        if(messageData != null) m.setExtraData(key, messageData);
                    }
                }

                m.setExtraTexts(JUtilBean.jsonPlain2Map(JUtilJSON.parse(msg.getExtraDatas())));

                m.setStored(true);//已经入库的消息
                m.setSuccess(true);
                list.add(m);
            }

            if(newDAO) DAOs.close(dao);
        }catch (Exception e){
            if(newDAO) DAOs.onException(dao);
            log.log(e, Logger.LEVEL_ERROR);
        }
        return list;
    }

    @Override
    public void run() {
        //初始化（从数据库重载ai实例）
        DAO dao=null;
        try{
            dao=DAOs.create(DB.getJFrameworkDB().getName(), this.getClass(), true);

            int rpp=100;
            int pn=1;
            List<JaiBeing> beings=dao.find("j_ai_being", "row_deleted='N' and (life_status='000' or life_status='001') order by life_start asc", rpp, pn);
            while (beings!=null && !beings.isEmpty()){
                for (int i=0; i<beings.size(); i++){
                    if(Nvwa.isDebug()) log.log("restoring being "+beings.get(i).getBeingId(), -1);
                    FOZU.restore(beings.get(i));
                }

                pn++;
                beings=dao.find("j_ai_being", "row_deleted='N' and (life_status='000' or life_status='001') order by life_start asc", rpp, pn);
            }
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            DAOs.onException(dao);
        }
        //初始化（从数据库重载ai实例） end

        while(!Startup.isDestroyed()){
            try{
                Thread.sleep(5000);
            }catch (Exception ex){}

            try{
                List<Being> _beings=beings.listValues();
                for(int i=0; i<_beings.size(); i++){
                    Being being=_beings.get(i);
                    being.removeEndedConverstions();//移除过期或已结束会话

                    FOZU.persist(being);//保存到数据库

                    if(!being.isEnded() && !being.isDestroyed()) continue;

                    beings.remove(being.getId());
                    being.destroy();
                    being=null;
                }
            }catch (Exception e){
                log.log(e, Logger.LEVEL_ERROR);
            }
        }
    }
}