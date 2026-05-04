package j.core.ai;

import j.util.JUtilString;

import java.sql.Timestamp;

public class MessageStorage {
    /**
     *
     * @param bizDir 业务相关的根目录
     * @param time 根据时间按年、月、日、时分、秒，生成目录
     * @return
     */
    public static String getDir(String bizDir, Timestamp time){
        if(!bizDir.startsWith("/")) bizDir="/"+bizDir;
        if(!bizDir.endsWith("/")) bizDir+="/";
        if(time==null) return bizDir;

        String yymmddhhss=time.toString().substring(0,16);
        yymmddhhss= JUtilString.replaceAll(yymmddhhss,"-","");
        yymmddhhss=JUtilString.replaceAll(yymmddhhss," ","");
        yymmddhhss=JUtilString.replaceAll(yymmddhhss,":","");
        return bizDir+yymmddhhss.substring(0,4)+"/"+yymmddhhss.substring(4,8)+"/"+yymmddhhss.substring(8,10)+"/"+yymmddhhss.substring(10,12)+"/";
    }
}
