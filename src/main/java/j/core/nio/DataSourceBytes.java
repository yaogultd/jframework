package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@ClassDescription(author = "肖炯",
        date = "2021/08/25",
        description = "字节数组类型数据源")
@Getter
@Setter
public class DataSourceBytes extends  DataSource{
    //字节数组
    private byte[] source;

    //即将被传输的数据块所处的位置
    private int blocks;

    //即将被传输的数据块所处的位置
    private int cursor;

    /**
     *
     */
    public DataSourceBytes(){
        super();
    }

    /**
     *
     * @param blockSize
     * @param charset
     */
    public DataSourceBytes(int blockSize, String charset){
        super(blockSize, charset);
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

    /**
     *
     * @param append
     * @return
     */
    public DataSource appendSource(byte[] append){
        this.source=JArray.append(this.source, append);
        this.contentLength=this.source==null?0:this.source.length;
        this.blocks=(int)(this.contentLength/this.blockSize + (this.contentLength%this.blockSize==0?0:1));
        return this;
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
        sb.append("{\"type\":\"DataSourceBytes\"");
        sb.append(",\"contentLength\":"+contentLength);
        sb.append(",\"blockSize\":"+blockSize);
        sb.append(",\"blocks\":"+blocks);
        sb.append(",\"cursor\":"+cursor+"}");
        return sb.toString();
    }
}
