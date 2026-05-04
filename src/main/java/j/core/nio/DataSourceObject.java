package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import j.core.serialize.JSerialization;
import j.core.service.ServiceResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@ClassDescription(author = "肖炯",
        date = "2021/08/25",
        description = "对象（可序列化）类型数据源")
@Getter
@Setter
public class DataSourceObject extends DataSource{
    //类名
    private String className;

    //对象序列化内容
    private byte[] source;

    //即将被传输的数据块所处的位置
    private int blocks;

    //即将被传输的数据块所处的位置
    private int cursor;

    /**
     *
     */
    public DataSourceObject(){
        super();
    }

    /**
     *
     * @param blockSize
     * @param charset
     */
    public DataSourceObject(int blockSize, String charset){
        super(blockSize, charset);
    }

    /**
     *
     * @param cls
     * @param source
     * @return
     */
    public DataSource setSource(Class cls, Object source) throws Exception{
        this.className=cls.getCanonicalName();

        //FST序列化
        this.source = JSerialization.serialize(null, source, true);
        this.contentLength=this.source==null?0:this.source.length;
        this.blocks=(int)(this.contentLength/this.blockSize + (this.contentLength%this.blockSize==0?0:1));
        return this;
    }

    /**
     *
     * @param className
     * @param source
     * @return
     */
    public DataSource setSource(String className, Object source) throws Exception{
        this.className=className;

        //序列化
        if(source!=null){
            try{
                this.source=JSerialization.serialize(null, source, true);
            }catch(Exception e){
                if(source instanceof ServiceResponse){
                    ServiceResponse response = (ServiceResponse)source;
                    if(response.getResponse() != null){
                        System.out.println(source.getClass().getCanonicalName()+"<"+response.getResponse().getClass().getCanonicalName()+"> 无法被序列化！");
                        throw e;
                    }
                }
                System.out.println(source.getClass().getCanonicalName()+" 无法被序列化！");
                throw e;
            }
        }

        this.contentLength=this.source==null?0:this.source.length;
        this.blocks=(int)(this.contentLength/this.blockSize + (this.contentLength%this.blockSize==0?0:1));
        return this;
    }

    /**
     *
     * @param source
     * @return
     */
    public DataSource setSource(byte[] source){
        this.source=source;
        this.contentLength=this.source==null?0:this.source.length;
        this.blocks=(int)(this.contentLength/this.blockSize + (this.contentLength%this.blockSize==0?0:1));
        return this;
    }

    @Override
    public String getName(){
        return this.className;
    }

    @Override
    public String getContentType(){
        return "application/java-object";
    }

    @Override
    public byte[] fetchBlock() throws IOException {
        //数据已经读取完毕
        if(this.cursor>=this.blocks) return null;

        byte[] block=null;
        if(this.cursor < this.blocks -1){//不是最后一块
            block = JArray.sub(this.source, this.cursor*this.blockSize, (this.cursor+1)*this.blockSize);
        }else{
            block = JArray.sub(this.source, this.cursor*this.blockSize, this.cursor*this.blockSize+(this.source.length%this.blockSize));
        }

        this.cursor++;

        return block;
    }

    @Override
    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append("{\"type\":\"DataSourceObject\"");
        sb.append(",\"contentLength\":"+contentLength);
        sb.append(",\"blockSize\":"+blockSize);
        sb.append(",\"blocks\":"+blocks);
        sb.append(",\"cursor\":"+cursor+"}");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception{
        System.out.println("encode -> "+ "a=b&b=c".split("&").length);
    }
}
