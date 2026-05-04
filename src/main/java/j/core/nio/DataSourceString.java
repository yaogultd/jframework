package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import j.util.JUtilJSON;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ClassDescription(author = "肖炯",
        date = "2021/08/25",
        description = "字符串类型数据源")
@Getter
@Setter
public class DataSourceString extends DataSource{
    //需要被传输的字符串（byte形式存储）
    private byte[] source;

    //即将被传输的数据块所处的位置
    private int blocks;

    //即将被传输的数据块所处的位置
    private int cursor;

    /**
     *
     */
    public DataSourceString(){
        super();
    }

    /**
     *
     * @param blockSize
     * @param charset
     */
    public DataSourceString(int blockSize, String charset){
        super(blockSize, charset);
    }

    /**
     *
     * @param source
     * @return
     */
    public DataSource setSource(String source){
        if(JUtilString.isBlank(source)){
            this.source=null;
            this.contentLength=0;
            return this;
        }
        try{
            this.source=source.getBytes(this.charset);
        }catch (Exception ignored){}
        this.contentLength=this.source.length;
        this.blocks=(int)(this.contentLength/this.blockSize + (this.contentLength%this.blockSize==0?0:1));
        return this;
    }

    /**
     *
     * @return
     */
    public String getSourceString(){
        try{
            return this.source==null?null:new String(this.source, StandardCharsets.UTF_8);
        }catch (Exception ignored){
            return new String(this.source);
        }
    }

    /**
     *
     * @param source
     * @return
     */
    public DataSource setSource(byte[] source){
        if(source==null||source.length==0){
            this.source=null;
            this.contentLength=0;
            return this;
        }
        this.source=source;
        this.contentLength=this.source.length;
        this.blocks=(int)(this.contentLength/this.blockSize + (this.contentLength%this.blockSize==0?0:1));
        return this;
    }

    @Override
    public String getContentType(){
        return "text/plain";
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
        sb.append("{\"type\":\"DataSourceString\"");
        sb.append(",\"contentLength\":"+contentLength);
        sb.append(",\"blockSize\":"+blockSize);
        sb.append(",\"blocks\":"+blocks);
        sb.append(",\"cursor\":"+cursor+"}");
        return sb.toString();
    }

    @Override
    public String detail(){
        StringBuffer sb=new StringBuffer();
        sb.append("{\"type\":\"DataSourceString\"");
        sb.append(",\"contentLength\":"+contentLength);
        sb.append(",\"blockSize\":"+blockSize);
        sb.append(",\"blocks\":"+blocks);
        sb.append(",\"cursor\":"+cursor);
        sb.append(",\"content\":"+ JUtilJSON.convertChars(this.getSourceString()) +"}");
        return sb.toString();
    }
}
