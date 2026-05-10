package j.core.nvwa.resource;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.JArray;
import j.core.common.JProperties;
import j.core.nvwa.Nvwa;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "一个需要扫描的资源组",
        reviewers = {})
public class Resources {
    private String[] types;
    private List<String> paths=new ArrayList<>();
    private List<Consumer> consumers=new ArrayList<>();
    private List<String> sConsumers=new ArrayList<>();
    private ConcurrentMap<String, Resource> resources=new ConcurrentMap<>();

    /**
     *
     */
    public Resources(){
    }

    /**
     *
     * @param element
     * @throws Exception
     */
    public Resources(Element element) throws Exception{
        if(element!=null) this.parse(element);
    }

    /**
     * 添加路径
     * @param path
     */
    public void setPath(String path){
        path=Resource.adjustPath(path);
        if(!this.paths.contains(path)) this.paths.add(path);
    }

    /**
     *
     * @param path
     * @return
     */
    public ConcurrentMap<String, Resource> getResources(String path){
        path=Resource.adjustPath(path);
        ConcurrentMap<String, Resource> matches=new ConcurrentMap<>();
        List<String> keys=resources.listKeys();
        for(int i=0; i<keys.size(); i++){
            String key=keys.get(i);
            Resource r=resources.get(key);
            if(matches(r, path, key)) matches.put(key, r);
        }
        return matches;
    }

    /**
     *
     * @param paths
     * @return
     */
    public ConcurrentMap<String, Resource> getResources(List<String> paths, String[] types){
        ConcurrentMap<String, Resource> matches=new ConcurrentMap<>();
        List<String> keys=resources.listKeys();
        for(int i=0; i<keys.size(); i++){
            String key=keys.get(i);
            Resource r=resources.get(key);

            if(!typeMatches(r, key, types)) continue;

            if(!matches(r, paths, key)) continue;

            matches.put(key, r);
        }
        return matches;
    }

    /**
     *
     * @param path
     * @return
     */
    public Resource getResource(String path){
        List<String> keys=resources.listKeys();
        for(int i=0; i<keys.size(); i++){
            String key=keys.get(i);
            Resource r=resources.get(key);
            if(matches(r, path, key)) return r;
        }
        return null;
    }

    /**
     *
     * @param r
     * @param path
     * @param key
     * @return
     */
    private boolean matches(Resource r, String path, String key){
        if(JUtilString.isBlank(path)) return true;

        path=Resource.adjustPath(r, path);

        if(path.indexOf("*")>-1) return JUtilString.match(key, path, "*")>-1;

        if(r instanceof ResourceClass){
            if(path.equals(key)) return true;

            //某个目录中的文件
            return key.startsWith(path);
        }else{
            //有后缀名（某个文件，此处假设除后缀名外，路径的其它位置不包含.）
            if(path.indexOf(".") > 0) return path.equals(key);

            //某个目录中的文件
            if(!path.endsWith("/")) path+="/";
            return key.startsWith(path) || (path.startsWith("/") && key.startsWith(path.substring(1)));
        }
    }

    /**
     *
     * @param r
     * @param paths
     * @param key
     * @return
     */
    private boolean matches(Resource r, List<String> paths, String key){
        for(int i=0; paths!=null && i<paths.size(); i++){
            if(this.matches(r, paths.get(i), key)) return true;
        }
        return false;
    }

    /**
     *
     * @param r
     * @param key
     * @param types
     * @return
     */
    private boolean typeMatches(Resource r, String key, String[] types){
        for(int i=0; i<types.length; i++){
            if(".class".equals(types[i]) && (r instanceof ResourceClass)) return true;
            if(key.endsWith(types[i])) return true;
        }
        return false;
    }



    @MethodDescription(author = "", date = "2021/07/19", description = "从xml配置片段中解析信息")
    private void parse(Element element) throws Exception{
        this.types=element.elementTextTrim("type").split(",");

        List<Element> _paths=element.element("paths").elements("path");
        for(int i=0; i<_paths.size(); i++){
            String path=ResourceHelper.replaceEnvVariables(_paths.get(i).getTextTrim());

            if(Startup.deployAsJar() && path.startsWith(JProperties.getAppRoot())){
                if(path.startsWith(JProperties.getAppRoot())) path=path.substring(JProperties.getAppRoot().length());
                if(path.startsWith("/")) path=path.substring(1);
            }

            path=JUtilString.replaceAll(path, "//", "/");

            this.paths.add(path);
        }

        Element consumorsElement=element.element("consumors");
        if(consumorsElement == null) return;

        List<Element> _consumers=consumorsElement.elements("consumor");
        for(int i=0; i<_consumers.size(); i++){
            this.sConsumers.add(_consumers.get(i).getTextTrim());
            this.consumers.add((Consumer)Class.forName(_consumers.get(i).getTextTrim()).getDeclaredConstructor().newInstance());
        }
    }

    @MethodDescription(author = "", date = "2021/07/19", description = "扫描资源组下所指定的目录")
    public void scan() throws Exception{
        //如果是jar包方式运行，从jar包内资源中获取相匹配的
        if(Startup.deployAsJar()){
            ConcurrentMap<String, Resource> cachedResources = Nvwa.getResourcesInDeployedJar(this.paths, this.types);
            //System.out.println("resources loading from "+ JArray.toString(this.paths, ",")+"|"+JArray.toString(this.types, ",")+" -> "+cachedResources.size());

            //交给资源“消费者”处理
            List<String> keys=cachedResources.listKeys();
            for(int r=0; r<keys.size(); r++){
                String key=keys.get(r);
                Resource resource=cachedResources.get(key);

                Resource found=resources.get(key);
                if(found == null){//jar包内资源不考虑更新
                    resources.put(key, resource);
                    for(int i=0; i<consumers.size(); i++){
                        try{
                            consumers.get(i).onFound(resource);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        //查找指定路径
        //即使jar包方式下也可从jar包外目录获取资源，
        //比如I18N、para*.xml修改后可保存与外部目录，重启时优先从外部目录查找资源（修改后的）
        //故相关模块查找资源时，应优先获取绝对路径下的（jar包内资源为相对路径）
        for(int i=0; i<this.paths.size(); i++){
            File file=new File(this.paths.get(i));
            //System.out.println("scanning resources in "+file.getAbsolutePath()+" for "+JArray.toString(this.types, ","));
            scan(file);
        }
    }

    @MethodDescription(author = "", date = "2021/07/19", description = "扫描指定目录/文件")
    public void scan(File file) throws Exception{
        //文件不存在
        if(!file.exists()){
            //如果是匹配某些文件（仅支持文件或所配置路径的最后一级目录使用通配符）
            if(file.getName().indexOf("*")>0){
               File parent = file.getParentFile();
               if(parent.exists()){
                   File[] files=parent.listFiles();
                   for(int i=0; i<files.length; i++){
                       //扫描匹配的文件
                       if(JUtilString.match(files[i].getAbsolutePath(), file.getName(), "*")>-1){
                           scan(files[i]);
                       }
                   }
               }
            }

            return;
        }

        //忽略Nvwa.class
        if(file.getName().equals("Nvwa.class")) return;

        //如果是目录，递归处理
        if(file.isDirectory()){
            File[] files=file.listFiles();
            for(int i=0; files!=null && i<files.length; i++){
                scan(files[i]);
            }
        }else if(file.getName().toLowerCase().endsWith(".jar")){//如果是jar包
            JarFile jar=new JarFile(file);
            Enumeration es=jar.entries();
            while(es.hasMoreElements()){
                JarEntry en=(JarEntry)es.nextElement();
                this.scan(jar, en);
            }

            //关闭输入流
            jar.close();
        }else if(this.typeMatches(file.getName())){//如果文件类型匹配
            Resource resource=resources.get(Resource.adjustPath(file.getAbsolutePath()));

            if(resource==null){//第一次加载资源
                String fileName=file.getName().toLowerCase();

                if(fileName.endsWith(".class")){
                    if(fileName.indexOf("$")>0) return;
                    resource=new ResourceClass(file.getAbsolutePath(), file.lastModified());
                }
                else if(fileName.endsWith(".xml")) resource=new ResourceXml(file.getAbsolutePath(), file.lastModified());
                else if(fileName.endsWith(".properties")) resource=new ResourceProperties(file.getAbsolutePath(), file.lastModified());
                else if(fileName.endsWith(".sh") || fileName.endsWith(".bat")) resource=new ResourceScript(file.getAbsolutePath(), file.lastModified());
                else if(fileName.endsWith(".text") || fileName.endsWith(".template")) resource=new ResourceString(file.getAbsolutePath(), file.lastModified());
                else if(fileName.endsWith(".jks")) resource=new ResourceBytes(file.getAbsolutePath(), file.lastModified());
                else return;

                if(!(resource instanceof ResourceClass)){//如果不是class类型资源，读取资源，并打印出日志
                    resource.getResource();
                    System.out.println("缓存资源 "+resource.getPath());
                }else{//class类型资源
                    System.out.println("缓存资源 "+resource.getPath());
                }
                resources.put(resource.getPath(), resource);

                //交给资源“消费者”处理
                for(int i=0; i<consumers.size(); i++){
                    try{
                        consumers.get(i).onFound(resource);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else if(resource.changed(file.lastModified())){//资源有更新
                //保存更新时间
                resource.setMotified(file.lastModified());
                if(!(resource instanceof ResourceClass)){//如果不是class类型资源，重新读取资源
                    resource.reset();
                    resource.getResource();
                }

                //交给资源“消费者”处理
                for(int i=0; i<consumers.size(); i++){
                    try{
                        consumers.get(i).onUpdate(resource);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @MethodDescription(author = "", date = "2022/01/05", description = "扫描jar")
    private void scan(JarFile jar, JarEntry en) throws Exception{
        //目录不予处理
        if(en.isDirectory()) return;

        //类型不匹配
        if(!this.typeMatches(en.getName())) return;

        //不予处理的jar资源
        if(Nvwa.ignoreJarEntry(en.getName())) return;

        //修改时间
        long modified= en.getLastModifiedTime()==null?en.getTime(): en.getLastModifiedTime().to(TimeUnit.MILLISECONDS);
        if(modified < 0) modified=0;

        String fileName=en.getName();

        Resource resource=resources.get(Resource.adjustPath(fileName));

        //资源记录已经存在且未更新
        //if(resource!=null && !resource.changed(modified)) return;

        //jar包内资源（暂时）不考虑更新
        if(resource != null) return;

        //是否第一次扫描到
        boolean _new = resource==null;

        fileName=fileName.toLowerCase();

        if(fileName.endsWith(".sh") || fileName.endsWith(".bat")){
            resource=new ResourceScript(en.getName(), jar.getInputStream(en), modified);
        }else if(fileName.endsWith(".class")){
            if(en.getName().indexOf("$")>0) return;
            resource=new ResourceClass(en.getName(), modified);
        }else if(fileName.endsWith(".xml")){
            resource=new ResourceXml(en.getName(), jar.getInputStream(en), modified);
        }else if(fileName.endsWith(".properties")){
            resource=new ResourceProperties(en.getName(), jar.getInputStream(en), modified);
        }else if(fileName.endsWith(".jar")){//递归读取处理jar
            JarInputStream innerJar=new JarInputStream(jar.getInputStream(en));
            JarEntry innerEn=innerJar.getNextJarEntry();
            while(innerEn != null){
                this.scan(jar, innerEn);
                innerEn=innerJar.getNextJarEntry();
            }
        }else if(fileName.endsWith(".text")
                ||fileName.endsWith(".template")){
            resource=new ResourceString(en.getName(), jar.getInputStream(en), modified);
        }else if(fileName.endsWith(".jks")){
            resource=new ResourceBytes(en.getName(), jar.getInputStream(en), modified);
        }else{
            return;
        }

        //jar/war资源
        resource.setInJar(true);

        //保存更新时间
        resource.setMotified(modified);

        if(!(resource instanceof ResourceClass)){//如果不是class类型资源，读取资源，并打印出日志
            resource.getResource();
            System.out.println("缓存资源(in jar) "+resource.getPath());
        }else{//class类型资源
            System.out.println("缓存资源(in jar) "+resource.getPath());
        }
        resources.put(resource.getPath(), resource);

        //交给资源“消费者”处理
        for(int i=0; i<consumers.size(); i++){
            if(_new){
                try{
                    consumers.get(i).onFound(resource);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //jar包内资源（暂时）不考虑更新
            //else consumers.get(i).onUpdate(resource);
        }
    }

    @MethodDescription(author = "", date = "2021/07/19", description = "是否指定资源类型")
    private boolean typeMatches(String path){
        if(this.types==null || this.types.length==0) return true;

        path=path.toLowerCase();
        for(int i=0; i<this.types.length; i++){
            if(path.endsWith(this.types[i])) return true;
        }
        return false;
    }

    public String toString(){
        StringBuffer s=new StringBuffer();
        s.append("{\"type\":\""+JArray.toString(this.types, ",")+"\"");
        s.append(",\"paths\":\""+JArray.toString(this.paths, ",")+"\"");
        s.append(",\"consumors\":\""+JArray.toString(this.sConsumers, ",")+"\"}");
        return s.toString();
    }
}
