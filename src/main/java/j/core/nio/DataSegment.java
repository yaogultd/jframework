package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import j.util.JUtilMath;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ClassDescription(author = "肖炯",
        date = "2021/09/02",
        description = "数据包的一个数据片段")
@Getter
@Setter
public class DataSegment implements Serializable {
    //片段总容量
    private int capacity;

    //所属数据包ID
    private long packageId;

    //数据包总长度
    private long total;

    //实际数据
    private byte[] data;

    /**
     *
     * @param capacity
     * @param packageId
     * @param total
     * @param data
     */
    public DataSegment(int capacity, long packageId, long total, byte[] data){
        this.capacity=capacity;
        this.packageId=packageId;
        this.total=total;
        this.data=data;
    }

    /**
     * 组装成发送到channel的字节数组
     * @return
     * @throws Exception
     */
    public byte[] assemble() throws Exception{
        byte[] _packageId= JUtilMath.longToBytes(packageId, 8, false);
        byte[] _total= JUtilMath.longToBytes(total, 8, false);
        byte[] _length= JUtilMath.intToBytes(data.length, 4, false);

        int all= Protocol.J_DATA_SEGMENT_START.length;
        all+=_packageId.length;
        all+=_total.length;
        all+=_length.length;
        all+=data.length;

        //if(all > capacity) throw new Exception("the total length of meta and data is overflow capacity -> "+all+" -> "+capacity);

        //构建数据片段
        byte[] segment=new byte[all];

        int index=0;
        for(int i=0; i<Protocol.J_DATA_SEGMENT_START.length; i++) segment[index++]=Protocol.J_DATA_SEGMENT_START[i];

        for(int i=0; i<_packageId.length; i++) segment[index++]=_packageId[i];

        for(int i=0; i<_total.length; i++) segment[index++]=_total[i];

        for(int i=0; i<_length.length; i++) segment[index++]=_length[i];

        for(int i=0; i<data.length; i++) segment[index++]=data[i];

        return segment;
    }

    /**
     *
     * @param segment
     * @return
     */
    public static DataSegment parse(byte[] segment){
        if(segment.length <= 22) return null;//无实际内容

        //数据包ID
        long packageId=JUtilMath.eightBytesToLong(JArray.sub(segment, 2, 10),  false);

        //数据包大小
        long total=JUtilMath.eightBytesToLong(JArray.sub(segment, 10, 18),  false);

        //当前数据片段大小
        int length=JUtilMath.fourBytesToInt(JArray.sub(segment, 18, 22),  false);

        //实际数据
        byte data[]=JArray.sub(segment, 22, 22+length);

        return new DataSegment(segment.length, packageId, total, data);
    }

    /**
     *
     * @param buffer
     * @return
     */
    public static DataSegment parse(DataSegmentBuffer buffer){
        //实际数据
        byte data[]=JArray.sub(buffer.getData(), 22, buffer.getData().length);

        return new DataSegment(buffer.getData().length, buffer.getPackageId(), buffer.getTotal(), data);
    }
}