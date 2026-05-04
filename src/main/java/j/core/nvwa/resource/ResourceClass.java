package j.core.nvwa.resource;

import j.core.common.JProperties;
import j.core.annotation.description.ClassDescription;
import j.core.Startup;
import j.util.JUtilString;
import lombok.Getter;

import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "与一个class文件对应的资源",
        reviewers = {})
@Getter
public class ResourceClass extends Resource{
    private String clazz=null;

    public ResourceClass(String path, long motified) {
        super(path, motified);

        //处理路径格式
        path= JUtilString.replaceAll(this.getPath(), "\\", "/");
        if(!JUtilString.isBlank(JProperties.getClassPath())
                &&path.startsWith(JProperties.getClassPath())){
            path=path.substring(JProperties.getClassPath().length());
        }
        if(path.startsWith("/")) path=path.substring(1);

        path=JUtilString.replaceAll(path, "/", ".");

        if(path.endsWith(".class")){
            this.clazz=path.substring(0, path.length()-6);
        }else{
            this.clazz=path;
        }

        setPath(path);
    }

    public ResourceClass(String path, InputStream inputStream, long motified) {
        super(path, inputStream, motified);

        //处理路径格式
        path= JUtilString.replaceAll(this.getPath(), "\\", "/");
        if(path.startsWith(JProperties.getClassPath())){
            path=path.substring(JProperties.getClassPath().length());
        }
        if(path.startsWith("/")) path=path.substring(1);

        path=JUtilString.replaceAll(path, "/", ".");

        if(path.endsWith(".class")){
            this.clazz=path.substring(0, path.length()-6);
        }else{
            this.clazz=path;
        }

        setPath(path);
    }

    @Override
    public Class getResource() throws Exception{
        return Class.forName(this.clazz,false, Startup.getDefaultClassLoader());
    }

    @Override
    public Class getResource(ClassLoader classLoader) throws Exception {
        if(classLoader==null) return getResource();
        else return Class.forName(this.clazz, false, classLoader);
    }

    @Override
    public void reset(){

    }
}
