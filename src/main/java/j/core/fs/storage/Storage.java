package j.core.fs.storage;

import j.core.annotation.description.ClassDescription;
import j.core.fs.JDFSFile;
import j.core.fs.JDFSMapping;
import j.core.fs.JFileMeta;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import org.dom4j.Element;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@ClassDescription(author = "肖炯", date = "2022/04/19", description = "文件存储处理器")
public abstract class Storage implements Cloneable{
    //自定义参数
    private ConcurrentMap<String, String> properties=new ConcurrentMap<>();

    //关联的文件映射配置
    protected JDFSMapping mapping;

    //为每个JDFSMapping创建的实例
    protected ConcurrentMap<String, Storage> instances=new ConcurrentMap<>();

    /**
     *
     * @param propertyElements
     */
    public Storage(List<Element> propertyElements){
        for(int i=0; propertyElements!=null && i<propertyElements.size(); i++){
            String name=propertyElements.get(i).attributeValue("name");
            String desc=propertyElements.get(i).attributeValue("desc");
            String value=propertyElements.get(i).getTextTrim();

            properties.put(name, value);
        }
    }

    /**
     *
     */
    protected void init(){

    }

    /**
     *
     * @param mapping
     * @return
     * @throws Exception
     */
    public Storage getInstance(JDFSMapping mapping) throws Exception{
        if(mapping==null) return this;

        if(this.instances.containsKey(mapping.getSelector())) return this.instances.get(mapping.getSelector());

        Storage instance=(Storage)this.clone();
        instance.mapping=mapping;
        instance.init();

        this.instances.put(mapping.getSelector(), instance);

        return instance;
    }

    /**
     *
     * @param key
     * @return
     * @throws Exception
     */
    public Storage getInstance(String key) throws Exception{
        if(key==null) return this;

        if(this.instances.containsKey(key)) return this.instances.get(key);

        Storage instance=(Storage)this.clone();
        instance.init();

        this.instances.put(key, instance);

        return instance;
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, String value){
        properties.put(name, value);
    }

    /**
     *
     * @param name
     * @return
     */
    public String getProperty(String name){
        return JUtilString.isBlank(name)?null:properties.get(name);
    }

    /**
     * 获得文件元数据信息
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return
     */
    public abstract JFileMeta getMeta(JDFSFile file);

    /**
     * 列出目录中文件
     * @param file
     * @return
     */
    public abstract String[] list(JDFSFile file);

    /**
     *
     * @param file
     * @return
     */
    public abstract boolean mkdirs(JDFSFile file);

    /**
     *
     * @param file
     * @param dest
     * @return
     */
    public abstract boolean renameTo(JDFSFile file, File dest);

    /**
     *
     * @param file
     * @return
     */
    public abstract byte[] bytes(JDFSFile file);

    /**
     *
     * @param file
     * @param encoding
     * @return
     */
    public abstract String string(JDFSFile file, String encoding);

    /**
     * 保存文件
     * @param file
     * @param content 文件内容
     * @param append
     * @param encoding
     * @return
     */
    public abstract boolean save(JDFSFile file, String content, boolean append, String encoding);

    /**
     *
     * @param file
     * @param content 文件内容
     * @param closeStreamOnEnd
     * @return
     */
    public abstract boolean save(JDFSFile file, InputStream content, boolean closeStreamOnEnd);

    /**
     *
     * @param file
     * @param localFile 本地文件
     * @return
     */
    public abstract boolean save(JDFSFile file, File localFile);

    /**
     * 删除文件
     * @param file
     * @return
     */
    public abstract boolean delete(JDFSFile file);

    /**
     * 获取文件
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return 返回对象类型取决于具体的存储器
     */
    public abstract Object find(String file);
}
