package j.core.nvwa.resource;

import j.core.annotation.description.ClassDescription;
import j.core.sys.SysConfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "与一个properties文件对应的资源",
        reviewers = {})
public class ResourceProperties extends Resource{
    private Properties properties;

    public ResourceProperties(String path, long motified) {
        super(path, motified);
    }

    public ResourceProperties(String path, InputStream inputStream, long motified) {
        super(path, inputStream, motified);
    }

    @Override
    public java.util.Properties getResource() throws Exception{
        if(properties == null){
            properties=new Properties();

            if(this.getInputStream()!=null){
                properties.load(new InputStreamReader(this.getInputStream(), SysConfig.sysEncoding));
            }else{
                properties.load(new InputStreamReader(new FileInputStream(this.getPath()), SysConfig.sysEncoding));
            }
        }
        return properties;
    }

    @Override
    public java.util.Properties getResource(ClassLoader classLoader) throws Exception {
        return getResource();
    }

    @Override
    public void reset(){
        this.properties=null;
    }
}
