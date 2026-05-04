package j.core.nvwa.resource;

import j.core.annotation.description.ClassDescription;
import j.core.fs.JDFS;
import j.core.fs.JDFSFile;
import j.core.sys.SysConfig;
import j.util.JUtilInputStream;

import java.io.File;
import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "与一个保存字符串的文件（如txt）对应的资源",
        reviewers = {})
public class ResourceString extends Resource{
    private String string;

    public ResourceString(String path, long motified) {
        super(path, motified);
    }

    public ResourceString(String path, InputStream inputStream, long motified) {
        super(path, inputStream, motified);
    }

    @Override
    public String getResource() throws Exception{
        if(string==null){
            if(this.getInputStream()!=null) {
                string = JUtilInputStream.string(this.getInputStream(), SysConfig.sysEncoding);
            }else{
                string = JDFSFile.read(new File(this.getPath()), SysConfig.sysEncoding);
            }
        }
        return string;
    }

    @Override
    public String getResource(ClassLoader classLoader) throws Exception {
        return getResource();
    }

    @Override
    public void reset(){
        this.string=null;
    }

    /**
     *
     * @return
     */
    public File saveTempFile() {
        try{
            File file=File.createTempFile("JFRAMEWORK", JDFS.getFileExt(this.getPath()));
            JDFSFile.save(file.getAbsolutePath(), getResource(), false, "UTF-8");
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getString(){
        return this.string;
    }
}
