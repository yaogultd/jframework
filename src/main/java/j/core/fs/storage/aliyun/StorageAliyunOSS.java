package j.core.fs.storage.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.*;
import j.core.annotation.description.ClassDescription;
import j.core.fs.JDFSFile;
import j.core.fs.JFileMeta;
import j.core.fs.storage.Storage;
import j.core.nvwa.resource.ResourceHelper;
import j.log.Logger;
import j.util.JUtilInputStream;
import j.util.JUtilString;
import org.dom4j.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "肖炯",
        date = "2022-06-15",
        description = "基于阿里云OSS实现的文件存储功能")
public class StorageAliyunOSS extends Storage {
    //日志
    private static Logger log=Logger.create(StorageAliyunOSS.class);

    //OSS client
    private OSS client=null;

    /**
     * @param propertyElements
     */
    public StorageAliyunOSS(List<Element> propertyElements) {
        super(propertyElements);
    }

    @Override
    protected void init(){
        try{
            client = new OSSClientBuilder().build(this.getProperty("endpoint"),
                    this.getProperty("accessKeyId"),
                    this.getProperty("accessKeySecret"));
        }catch(Exception e){
            log.log("创建阿里云OSS Client出错：", Logger.LEVEL_ERROR);
            log.log(e, Logger.LEVEL_ERROR);
        }
    }

    /**
     *
     * @param file
     * @return
     */
    private static String getKey(JDFSFile file){
        String key="";

        if(file.getMapping()==null){
            key = ResourceHelper.getRelativePath(file.getPhysicalPath());
        }else{
            key = file.getMapping().relativePath(file.getPath());
        }
        if(key.startsWith("/")) key=key.substring(1);
        return JUtilString.isBlank(key) ? "/" : key;
    }

    /**
     * 获得文件元数据信息
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return
     */
    public JFileMeta getMeta(JDFSFile file){
        JFileMeta meta=new JFileMeta();
        try{
            String key=getKey(file);
            log.log("try get meta of oss file "+key, Logger.LEVEL_INFO);

            meta.absolutePath=file.getVirtualPath();
            meta.canonicalPath=file.getVirtualPath();
            meta.path=meta.absolutePath;
            meta.name=key;
            if(meta.name.endsWith("/")) meta.name=meta.name.substring(0, meta.name.length()-1);
            if(meta.name.lastIndexOf("/")>-1){
                meta.name=meta.name.substring(meta.name.lastIndexOf("/")+1);
            }

            String parent=meta.absolutePath;
            if(parent.endsWith("/")){
                parent=parent.substring(0, parent.length()-1);
                if(parent.lastIndexOf("/")>0) parent=parent.substring(0, parent.lastIndexOf("/")+1);
                else parent=null;
            }else if(parent.lastIndexOf("/")>0){
                parent=parent.substring(0, parent.lastIndexOf("/")+1);
            }else{
                parent=null;
            }

            meta.isDirectory=meta.path.endsWith("/");
            meta.isFile=!meta.isDirectory;

            if(!client.doesObjectExist(this.getProperty("bucket"), key)){
                log.log("get meta of oss file "+key+" failed, It's not exists.", Logger.LEVEL_ERROR);
                meta.exists=false;
                return meta;
            }

            ObjectMetadata result=client.getObjectMetadata(this.getProperty("bucket"), key);
            meta.exists=true;
            meta.lastModified=result.getLastModified().getTime();
            meta.length=result.getContentLength();

            return meta;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);

            return null;
        }
    }

    /**
     * 列出目录中文件
     * @param file
     * @return
     */
    public String[] list(JDFSFile file){
        List<String> children=new ArrayList<>();
        try{
            String key=getKey(file);
            if("/".equals(key)) key = "";

            int depth = JUtilString.isBlank(key)?0:key.split("/").length;

            ObjectListing result=client.listObjects(this.getProperty("bucket"), key);
            if(result==null) return null;

            List<OSSObjectSummary> summaries=result.getObjectSummaries();

            for(int i=0; i<summaries.size(); i++){
                OSSObjectSummary os=summaries.get(i);
                int depthOfChild = os.getKey().split("/").length;

                //只读取直接下级目录d
                if(depthOfChild - depth != 1) continue;

                children.add(os.getKey());
            }
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return children.toArray(new String[children.size()]);
    }

    /**
     *
     * @param file
     * @return
     */
    public boolean mkdirs(JDFSFile file){
        return true;
    }

    /**
     *
     * @param file
     * @param dest
     * @return
     */
    public boolean renameTo(JDFSFile file, File dest){
        return true;
    }

    /**
     *
     * @param file
     * @return
     */
    public byte[] bytes(JDFSFile file){
        try{
            String key=getKey(file);

            if(!client.doesObjectExist(this.getProperty("bucket"), key)){
                log.log("get bytes of oss file "+key+" failed, It's not exists.", Logger.LEVEL_ERROR);
                return null;
            }

            OSSObject obj=client.getObject(this.getProperty("bucket"), key);

            return JUtilInputStream.bytes(obj.getObjectContent());
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return null;
    }

    /**
     *
     * @param file
     * @param encoding
     * @return
     */
    public String string(JDFSFile file, String encoding){
        try{
            String key=getKey(file);

            if(!client.doesObjectExist(this.getProperty("bucket"), key)){
                log.log("get string of oss file "+key+" failed, It's not exists.", Logger.LEVEL_ERROR);
                return null;
            }

            OSSObject obj=client.getObject(this.getProperty("bucket"), key);

            return JUtilInputStream.string(obj.getObjectContent(), encoding);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
        }
        return null;
    }

    /**
     * 保存文件
     * @param file
     * @param content 文件内容
     * @param append
     * @param encoding
     * @return
     */
    public boolean save(JDFSFile file, String content, boolean append, String encoding){
        try{
            String key=getKey(file);
            if(append){
                log.log("try append oss file "+key, Logger.LEVEL_INFO);
                AppendObjectResult result = client.appendObject(new AppendObjectRequest(this.getProperty("bucket"), key, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))));

                ResponseMessage response=result.getResponse();
                if(response!=null && !response.isSuccessful()){
                    log.log("append oss file "+key+" failed: "+response.getErrorResponseAsString()+".", Logger.LEVEL_ERROR);
                    return false;
                }
                log.log("append oss file "+key+" successfully.", Logger.LEVEL_INFO);
            }else{
                log.log("try put oss file "+key, Logger.LEVEL_INFO);
                PutObjectResult result = client.putObject(this.getProperty("bucket"), key, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

                ResponseMessage response=result.getResponse();
                if(response!=null && !response.isSuccessful()){
                    log.log("put oss file "+key+" failed: "+response.getErrorResponseAsString()+".", Logger.LEVEL_ERROR);
                    return false;
                }
                log.log("put oss file "+key+" successfully.", Logger.LEVEL_INFO);
            }

            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     * 保存文件
     * @param file
     * @param content 文件内容
     * @param closeStreamOnEnd
     * @return
     */
    public boolean save(JDFSFile file, InputStream content, boolean closeStreamOnEnd){
        try{
            String key=getKey(file);
            log.log("try put oss file "+key, Logger.LEVEL_INFO);
            PutObjectResult result = client.putObject(this.getProperty("bucket"), key, content);

            ResponseMessage response=result.getResponse();
            if(response!=null && !response.isSuccessful()){
                log.log("put oss file "+key+" failed: "+response.getErrorResponseAsString()+".", Logger.LEVEL_ERROR);
                return false;
            }
            log.log("put oss file "+key+" successfully.", Logger.LEVEL_INFO);

            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     *
     * @param file
     * @param localFile 本地文件
     * @return
     */
    public boolean save(JDFSFile file, File localFile){
        try{
            return this.save(file, new FileInputStream(localFile), true);
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    @Override
    public boolean delete(JDFSFile file) {
        try{
            String key=getKey(file);
            log.log("try delete oss file "+key, Logger.LEVEL_INFO);
            client.deleteObject(this.getProperty("bucket"), key);

            log.log("delete oss file "+key+" successfully.", Logger.LEVEL_INFO);

            return true;
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     * 获取文件
     *
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return 返回对象类型取决于具体的存储器
     */
    @Override
    public Object find(String file) {
        OSSObject result=null;
        try{
            String key=ResourceHelper.getRelativePath(file);
            log.log("try find oss file "+key, Logger.LEVEL_INFO);

            if(!client.doesObjectExist(this.getProperty("bucket"), key)){
                log.log("find oss file "+key+" failed, It's not exists.", Logger.LEVEL_ERROR);
                return null;
            }

            result=client.getObject(this.getProperty("bucket"), key);

            ResponseMessage response=result.getResponse();
            if(response!=null && !response.isSuccessful()){
                log.log("find oss file "+key+" failed: "+response.getErrorResponseAsString()+".", Logger.LEVEL_ERROR);
                return null;
            }

            log.log("find oss file "+key+" successfully.", Logger.LEVEL_INFO);

            return result.getObjectContent();
        }catch (Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            if(result!=null) {
                try {
                    result.close();
                }catch(Exception ex){}
            }

            return null;
        }
    }
}