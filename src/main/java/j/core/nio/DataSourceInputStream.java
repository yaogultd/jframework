package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/08/25",
        description = "输入流类型数据源")
@Getter
@Setter
public class DataSourceInputStream extends  DataSource{
    //需要被传输的字符串（byte形式存储）
    private InputStream source;

    /**
     *
     */
    public DataSourceInputStream(){
        super();
    }

    /**
     *
     * @param blockSize
     * @param charset
     */
    public DataSourceInputStream(int blockSize, String charset){
        super(blockSize, charset);
    }

    /**
     *
     * @param source
     * @return
     */
    public DataSource setSource(InputStream source){
        this.source=source;
        return this;
    }

    @Override
    public byte[] fetchBlock() throws IOException {
        byte[] buffer=new byte[this.blockSize];

        int read=this.source.read(buffer);
        if(read < 0) {
            try{
                this.source.close();
            }catch (Exception e){}
            return null;
        }

        this.contentLength+=read;

        return JArray.sub(buffer, 0, read);
    }

    @Override
    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append("{\"type\":\"DataSourceInputStream\"");
        sb.append(",\"contentLength\":"+contentLength);
        sb.append(",\"blockSize\":"+blockSize+"}");
        return sb.toString();
    }
}
