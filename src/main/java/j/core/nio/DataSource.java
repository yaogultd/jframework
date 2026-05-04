package j.core.nio;

import j.core.annotation.description.ClassDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;

@ClassDescription(author = "肖炯",
        date = "2021/08/25",
        description = "在业务意义上一次完整传输中包含的数据称为一个DataSource，它必须指定数据长度、使用的字符集编码、数据块长度，" +
                "并实现一个逐个获取数据块的方法")
@Getter
@Setter
public abstract class DataSource implements Serializable {
    //数据源总长度（单位：Byte）
    protected long contentLength;

    //数据块长度（单位：Byte，默认1024）
    protected int blockSize=1024;

    //使用的字符集（默认UTF-8）
    protected String charset= "UTF-8";

    /**
     *
     */
    public DataSource(){}

    /**
     *
     * @return
     */
    public String getContentType(){
        return "application/octet-stream";
    }

    /**
     *
     * @return
     */
    public String getName(){
        return "";
    }

    /**
     *
     * @param blockSize
     * @param charset
     */
    public DataSource(int blockSize, String charset){
        this.blockSize=blockSize<=0?1024:blockSize;
        this.charset=charset;
    }

    /**
     * 逐个获取数据块
     * @return 当没有更多数据块需要获取时，返回null
     * @throws IOException
     */
    public abstract byte[] fetchBlock() throws IOException;

    /**
     *
     */
    public void clear(){

    }

    /**
     *
     * @return
     */
    public String detail(){
        return this.toString();
    }
}
