package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.JArray;
import lombok.Getter;
import lombok.Setter;

import java.io.*;

@ClassDescription(author = "肖炯",
        date = "2021/08/25",
        description = "输入流类型数据源")
@Getter
@Setter
public class DataSourceFile extends  DataSource{
    //文件对象
    private File source;

    //输入流（用于从文件读取）
    private InputStream is;

    //输出流（用于保存到文件）
    private OutputStream os;

    //原始文件名
    private String nameOriginal;

    /**
     *
     */
    public DataSourceFile(){
        super();
    }

    /**
     *
     * @param blockSize
     * @param charset
     */
    public DataSourceFile(int blockSize, String charset){
        super(blockSize, charset);
    }

    /**
     *
     * @param source
     * @return
     */
    public DataSource setSource(File source){
        this.source=source;
        this.nameOriginal=source.getName();
        this.contentLength=this.source.exists()?this.source.length():0;
        return this;
    }

    @Override
    public String getName(){
        return this.source.getName();
    }

    @Override
    public byte[] fetchBlock() throws IOException {
        if(!this.source.exists()) return null;

        if(is==null) is=new FileInputStream(source);

        byte[] buffer=new byte[this.blockSize];

        int read=this.is.read(buffer);
        if(read < 0){
            try{
                is.close();
            }catch (Exception e){}
            return null;
        }

        return JArray.sub(buffer, 0, read);
    }

    /**
     * 存储到文件
     * @param data
     */
    public void save(byte[] data) throws Exception{
        if(data==null || data.length==0) return;

        if(!this.source.exists()) this.source.getParentFile().mkdirs();

        if(this.os == null) this.os=new FileOutputStream(this.source);

        this.os.write(data,0, data.length);
    }

    /**
     * 存储到文件完成
     */
    public void save(){
        if(this.os == null) return;

        try{
            os.flush();
        }catch(Exception e){}
        try{
            os.close();
        }catch(Exception e){}
    }

    @Override
    public void clear(){
        if(this.source != null && this.source.exists()){
            try{
                this.source.delete();
            }catch (Exception e){}
        }

        if(this.is != null){
            try{
                this.is.close();
            }catch (Exception e){}
        }

        if(this.os != null){
            try{
                this.os.close();
            }catch (Exception e){}
        }
    }

    @Override
    public String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append("{\"type\":\"DataSourceFile\"");
        sb.append(",\"contentLength\":"+contentLength);
        sb.append(",\"blockSize\":"+blockSize+"}");
        return sb.toString();
    }
}
