package j.core.ai;

import j.core.sys.SysUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageData {
    private long createTime;
    private int contentType;
    private String storeType;
    private long length;
    private String name;
    private String content;

    private String bizCode;
    private String bizName;

    public MessageData(){
        this.createTime = SysUtil.getNow();
    }

    public MessageData(String name, int contentType, String storeType, long length, String content){
        this.createTime = SysUtil.getNow();
        this.contentType = contentType;
        this.storeType = storeType;
        this.length = length;
        this.name = name;
        this.content = content;
    }

    public MessageData(String name, int contentType, String storeType, long length, String content, String bizCode, String bizName){
        this.createTime = SysUtil.getNow();
        this.contentType = contentType;
        this.storeType = storeType;
        this.length = length;
        this.name = name;
        this.content = content;
        this.bizCode = bizCode;
        this.bizName = bizName;
    }
}
