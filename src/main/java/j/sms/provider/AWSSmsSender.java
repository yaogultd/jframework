package j.sms.provider;


import j.core.annotation.description.ClassDescription;
import j.log.Logger;
import j.sms.SMSChannel;
import j.tool.region.Countries;
import j.util.JUtilBean;
import j.util.JUtilString;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.pinpoint.PinpointClient;
import software.amazon.awssdk.services.pinpoint.model.*;

import java.util.HashMap;
import java.util.Map;

@ClassDescription(author = "宋长明",
        date = "2021-08-10",
        description = "亚马逊短信通道")
public class AWSSmsSender extends SMSChannel{
    private static Logger log=Logger.create(AWSSmsSender.class);//日志输出
    private PinpointClient pinpoint;

    @Override
    public boolean reachable(String dest){
        if(dest==null||"".equals(dest)) return false;
        return Countries.isPhoneNumberValid(dest);
    }

    @Override
    public boolean send(String to, String signature, String text) throws Exception{
        return sendTemplateSMS(to, signature, null, text, null);
    }

    @Override
    public boolean send(String to, String signature, String text, String[] filePaths) throws Exception{
        throw new Exception("不支持彩信");
    }

    @Override
    public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts) throws Exception{
        return sendTemplateSMS(to, signature, templateId, template, texts, (Map)null);
    }

    @Override
    public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs) throws Exception {
        try {
            for(int i=0; texts!=null && i<texts.length;i++){
                template=JUtilString.replaceAll(template, "{"+(i+1)+"}",texts[i]);
            }
            if(!this.reachable(to)){
                log.log("短信发送失败，目标不可达!", -1);
                return true;
            }

            String[] dest=Countries.getPhoneNumberDetail(to);
            try {
                sendSMSMessage("+"+dest[0]+dest[1], template);
                log.log("AWS 发送短信成功！", -1);
                return true;
            }catch (Exception e) {
                log.log(e, Logger.LEVEL_ERROR);
                return false;
            }
        }catch (Exception e) {
            log.log(e, Logger.LEVEL_ERROR);

            return false;
        }
    }

    /**
     *
     * @param destinationNumber
     * @param message
     * @throws Exception
     */
    private void sendSMSMessage(String destinationNumber, String message) throws Exception{
        synchronized(this){
            if(pinpoint==null) {
                pinpoint = PinpointClient.builder()
                        .credentialsProvider(AwsCredentialsProviderChain.builder().addCredentialsProvider(new AwsCredentialsProviderImpl(config.getProperty("accessKey"), config.getProperty("accessSecret"))).build())
                        .region(Region.of(config.getProperty("region")))
                        .build();
            }
        }

        Map<String, AddressConfiguration> addressMap = new HashMap<>();
        AddressConfiguration addConfig = AddressConfiguration.builder()
                .channelType(ChannelType.SMS)
                .build();

        addressMap.put(destinationNumber, addConfig);
        SMSMessage smsMessage = SMSMessage.builder()
                .body(message)
                .messageType(MessageType.TRANSACTIONAL)
                .senderId(this.config.getProperty("senderId"))
                .build();

        // Create a DirectMessageConfiguration object.
        DirectMessageConfiguration direct = DirectMessageConfiguration.builder()
                .smsMessage(smsMessage)
                .build();

        MessageRequest msgReq = MessageRequest.builder()
                .addresses(addressMap)
                .messageConfiguration(direct)
                .build();

        // create a  SendMessagesRequest object
        SendMessagesRequest request = SendMessagesRequest.builder()
                .applicationId(this.config.getProperty("appId"))
                .messageRequest(msgReq)
                .build();

        log.log("AWS短信发送 => "+ destinationNumber + " => " + message, -1);
        SendMessagesResponse response= pinpoint.sendMessages(request);
        MessageResponse mResponse = response.messageResponse();
        log.log("AWS短信发送结果 => "+ JUtilBean.map2Json(mResponse.result()), -1);
    }

    @Override
    public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, String[] filePaths) throws Exception{
        throw new Exception("不支持彩信");
    }

    @Override
    public boolean sendTemplateSMS(String to, String signature, String templateId, String template, String[] texts, Map<String, String> keyValuePairs, String[] filePaths) throws Exception {
        throw new Exception("不支持彩信");
    }
}

class AwsCredentialsProviderImpl implements AwsCredentialsProvider{
    private String accessKey;
    private String accessSecret;
    protected AwsCredentialsProviderImpl(String accessKey, String accessSecret){
        this.accessKey=accessKey;
        this.accessSecret=accessSecret;
    }

    @Override
    public AwsCredentials resolveCredentials() {
        return AwsBasicCredentials.create(this.accessKey, this.accessSecret);
    }
}