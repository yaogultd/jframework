package j.core.nvwa.resource;

import j.core.annotation.description.*;
import j.core.common.JProperties;
import j.util.JUtilString;

import java.io.InputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "表示一个资源，可以是class、xml、properties、txt、网址...等。",
        reviewers = {})
public abstract class Resource<T> {
    @FieldDescription(description = "资源路径")
    private String path=null;

    @FieldDescription(description = "资源文件名")
    private String name=null;

    @FieldDescription(description = "资源输入流")
    private InputStream inputStream=null;

    @FieldDescription(description = "是否jar/war包内的资源")
    private boolean inJar=false;

    @FieldDescription(description = "资源最近修改时间")
    private long motified=0;

    /**
     *
     * @param path
     * @return
     */
    public static String adjustPath(String path){
        path = JUtilString.replaceAll(path,"\\", "/");

        if(path.endsWith(".class")){
            if(!JUtilString.isBlank(JProperties.getClassPath())
                    &&path.startsWith(JProperties.getClassPath())){
                path=path.substring(JProperties.getClassPath().length());
            }
            if(path.startsWith("/")) path=path.substring(1);

            path=JUtilString.replaceAll(path, "/", ".");
            //path=path.substring(0, path.length()-6);
        }

        return path;
    }

    /**
     *
     * @param r
     * @param path
     * @return
     */
    public static String adjustPath(Resource r, String path){
        path = JUtilString.replaceAll(path,"\\", "/");

        if((r instanceof ResourceClass) || path.endsWith(".class")){
            if(!JUtilString.isBlank(JProperties.getClassPath())
                    &&path.startsWith(JProperties.getClassPath())){
                path=path.substring(JProperties.getClassPath().length());
            }
            if(path.startsWith("/")) path=path.substring(1);

            path=JUtilString.replaceAll(path, "/", ".");
            //path=path.substring(0, path.length()-6);
        }

        return path;
    }

    public Resource(String path, long motified){
        path= adjustPath(path);
        this.path=path;
        if(path.lastIndexOf("/")>-1){
            this.name=path.substring(path.lastIndexOf("/")+1);
        }else{
            this.name=path;
        }
        this.motified=motified;
    }

    public Resource(String path, InputStream inputStream, long motified){
        path= adjustPath(path);
        this.path=path;
        if(path.lastIndexOf("/")>-1){
            this.name=path.substring(path.lastIndexOf("/")+1);
        }else{
            this.name=path;
        }
        this.motified=motified;
        this.inputStream=inputStream;
    }

    //getters and setters
    public String getPath(){
        return this.path;
    }
    public void setPath(String path){
        this.path=path;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name=name;
    }

    public InputStream getInputStream(){
        return this.inputStream;
    }
    public void setInputStream(InputStream inputStream){
        this.inputStream=inputStream;
    }

    public boolean isInJar(){
        return this.inJar;
    }
    public void setInJar(boolean inJar){
        this.inJar=inJar;
    }

    public long getMotified(){
        return this.motified;
    }
    public void setMotified(long motified){
        this.motified=motified;
    }
    //getters and setters end

    @MethodDescription(author = "肖炯", date = "021/07/19", description = "判断资源是否已更新")
    public boolean changed(long motified){
        return motified>this.motified;
    }

    @MethodDescription(author = "肖炯", date = "021/07/19", description = "获得资源，如class文件获得的是Class，xml获得的是org.dom4j.Document对象")
    public abstract T getResource() throws Exception;

    @MethodDescription(author = "肖炯", date = "021/07/19", description = "获得资源，如class文件获得的是Class，xml获得的是org.dom4j.Document对象")
    public abstract T getResource(ClassLoader classLoader) throws Exception;

    @MethodDescription(author = "肖炯", date = "022/01/05", description = "重置资源内容")
    public abstract void reset() throws Exception;

    /**
     *
     * @param types
     * @return
     */
    public boolean typeMatches(String[] types){
        if(types==null || types.length==0) return true;

        String _path=path.toLowerCase();
        for(int i=0; i<types.length; i++){
            if(_path.endsWith(types[i])) return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public String getString(){
        return null;
    }
}
