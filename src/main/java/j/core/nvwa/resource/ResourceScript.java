package j.core.nvwa.resource;

import j.core.annotation.description.ClassDescription;
import j.core.fs.JDFSFile;
import j.core.sys.SysConfig;
import j.util.JUtilInputStream;

import java.io.File;
import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "与一个可执行脚本文件（如bat、sh）对应的资源",
        reviewers = {})
public class ResourceScript extends Resource{
    private String string;

    public ResourceScript(String path, long motified) {
        super(path, motified);
    }

    public ResourceScript(String path, InputStream inputStream, long motified) {
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
        return this.getPath();
    }

    @Override
    public String getResource(ClassLoader classLoader) throws Exception {
        return getResource();
    }

    @Override
    public void reset(){
        this.string=null;
    }

    @Override
    public String getString(){
        return this.string;
    }
}
