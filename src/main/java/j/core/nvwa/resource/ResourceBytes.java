package j.core.nvwa.resource;

import j.core.annotation.description.ClassDescription;
import j.core.fs.JDFS;
import j.core.fs.JDFSFile;
import j.util.JUtilInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "将文件保存为字节数组",
        reviewers = {})
public class ResourceBytes extends Resource{
    private byte[] bytes;

    public ResourceBytes(String path, long motified) {
        super(path, motified);
    }

    public ResourceBytes(String path, InputStream inputStream, long motified) {
        super(path, inputStream, motified);
    }

    @Override
    public byte[] getResource() throws Exception{
        if(bytes==null){
            if(this.getInputStream()!=null) {
                bytes = JUtilInputStream.bytes(this.getInputStream());
            }else{
                bytes = JUtilInputStream.bytes(new FileInputStream(new File(this.getPath())));
            }
        }
        return bytes;
    }

    @Override
    public byte[] getResource(ClassLoader classLoader) throws Exception {
        return getResource();
    }

    @Override
    public void reset(){
        this.bytes=null;
    }

    /**
     *
     * @return
     */
    public File saveTempFile() {
        try{
            File file=File.createTempFile("JFRAMEWORK", JDFS.getFileExt(this.getPath()));
            JDFSFile.save(new ByteArrayInputStream(getResource()), file.getAbsolutePath());
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
