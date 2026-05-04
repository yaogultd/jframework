package j.core.nio;

import j.core.common.JArray;
import j.core.sys.SysUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataSegmentBuffer {
    private Long packageId;
    private Long timeout;
    private Long total;
    private Integer length;
    private Long receiveBegin=0L;
    private Long receiveEnd=0L;
    private byte[] data;

    public DataSegmentBuffer(long packageId, long total, int length, long timeout){
        this.packageId=packageId;
        this.total=total;
        this.length=length;
        this.timeout=timeout<=0 ? 30000 : timeout;
        this.receiveBegin=SysUtil.getNow();
    }

    public boolean isTimeout(){
        return this.timeout > 0
                && this.receiveBegin > 0
                && this.receiveEnd==0
                && SysUtil.getNow() - this.receiveBegin > this.timeout;
    }

    public void append(byte[] append){
        this.data = JArray.append(this.data, append);
    }

    public void append(byte append){
        this.data = JArray.append(this.data, new byte[]{append});
    }

    public boolean isCompleted(){
        return (this.data!=null && this.data.length >= this.length + 22);
    }

    public boolean toCompleted(int tobeAppended){
        return (this.data!=null && this.data.length + tobeAppended >= this.length + 22);
    }

    public void destroy(){
        this.data=null;
    }
}