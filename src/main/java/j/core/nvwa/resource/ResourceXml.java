package j.core.nvwa.resource;

import j.core.annotation.description.ClassDescription;
import j.core.fs.JDFSFile;
import j.core.sys.SysConfig;
import j.util.JUtilDom4j;
import j.util.JUtilInputStream;
import j.util.JUtilString;
import org.dom4j.Document;

import java.io.File;
import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "与一个xml文件对应的资源",
        reviewers = {})
public class ResourceXml extends Resource{
    private Document document;
    private String string;

    public ResourceXml(String path, long motified) {
        super(path, motified);
    }

    public ResourceXml(String path, InputStream inputStream, long motified) {
        super(path, inputStream, motified);
    }

    @Override
    public Document getResource() throws Exception{
        if(string == null){
            try{
                if(this.getInputStream()!=null){
                    string=JUtilInputStream.string(this.getInputStream(), SysConfig.sysEncoding);
                }else{
                    string= JDFSFile.read(new File(this.getPath()), SysConfig.sysEncoding);
                }

                if(!JUtilString.isBlank(string)){
                    document = JUtilDom4j.parseString(string, SysConfig.sysEncoding);
                }
            }catch(Exception e){}
        }

        return document;
    }

    @Override
    public Document getResource(ClassLoader classLoader) throws Exception {
        return getResource();
    }

    @Override
    public void reset(){
        this.string=null;
        this.document=null;
    }

    @Override
    public String getString(){
        return this.string;
    }
}
