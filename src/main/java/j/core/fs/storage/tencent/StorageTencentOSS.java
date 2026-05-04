package j.core.fs.storage.tencent;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ClassDescription(author = "肖炯",
        date = "2022-06-15",
        description = "基于阿里云OSS实现的文件存储功能")
public class StorageTencentOSS extends Storage {
    //日志
    private static Logger log=Logger.create(StorageTencentOSS.class);

    //OSS client
    private COSClient client=null;

    //高级上传处理类
    TransferManager transferManager=null;

    /**
     * @param propertyElements
     */
    public StorageTencentOSS(List<Element> propertyElements) {
        super(propertyElements);
    }

    @Override
    protected void init(){
        try{
            // 1 初始化用户身份信息（secretId, secretKey）。
            // SECRETID和SECRETKEY请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
            String secretId = this.getProperty("SecretId");
            String secretKey = this.getProperty("SecretKey");
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

            // 2 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
            Region region = new Region("ap-shanghai");
            ClientConfig clientConfig = new ClientConfig(region);

            // 这里建议设置使用 https 协议
            // 从 5.6.54 版本开始，默认使用了 https
            clientConfig.setHttpProtocol(HttpProtocol.https);

            //生成 cos 客户端。
            client = new COSClient(cred, clientConfig);

            transferManager = createTransferManager();
        }catch(Exception e){
            log.log("创建阿里云OSS Client出错：", Logger.LEVEL_ERROR);
            log.log(e, Logger.LEVEL_ERROR);
        }
    }

    /**
     * 创建 TransferManager 实例，这个实例用来后续调用高级接口
     * @return
     */
    private TransferManager createTransferManager() {
        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(16);

        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(this.client, threadPool);

        // 设置高级接口的配置项
        // 分块上传阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(5*1024*1024);
        transferManagerConfiguration.setMinimumUploadPartSize(1*1024*1024);
        transferManager.setConfiguration(transferManagerConfiguration);

        return transferManager;
    }

    /**
     *
     * @param file
     * @return
     */
    private static String getKey(JDFSFile file){
        String key="";
        if(file.getMapping()==null) key = ResourceHelper.getRelativePath(file.getPhysicalPath());
        else key = file.getMapping().relativePath(file.getPath());
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
            // 指定文件上传到 COS 上的路径，即对象键。例如对象键为folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
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

            List<COSObjectSummary> summaries=result.getObjectSummaries();

            for(int i=0; i<summaries.size(); i++){
                COSObjectSummary os=summaries.get(i);
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

            COSObject obj=client.getObject(this.getProperty("bucket"), key);

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

            COSObject obj=client.getObject(this.getProperty("bucket"), key);

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
        String key=getKey(file);
        try{
            if(append){
                log.log("try append oss file "+key, Logger.LEVEL_INFO);
                AppendObjectResult result = client.appendObject(new AppendObjectRequest(this.getProperty("bucket"), key, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))));

                if(result == null){
                    log.log("append oss file "+key+" failed!", Logger.LEVEL_ERROR);
                    return false;
                }
                log.log("append oss file "+key+" successfully.", Logger.LEVEL_INFO);
            }else{
                log.log("try put oss file "+key, Logger.LEVEL_INFO);
                PutObjectResult result = client.putObject(this.getProperty("bucket"), key, content);

                if(result == null){
                    log.log("put oss file "+key+" failed!", Logger.LEVEL_ERROR);
                    return false;
                }
                log.log("put oss file "+key+" successfully.", Logger.LEVEL_INFO);
            }

            return true;
        }catch (Exception e){
            if(append){
                log.log("append oss file "+key+" failed!", Logger.LEVEL_ERROR);
            }else{
                log.log("put oss file "+key+" failed!", Logger.LEVEL_ERROR);
            }
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
        String key=getKey(file);
        try{
            log.log("try put oss file "+key+"(size:"+file.length()+")", Logger.LEVEL_INFO);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            if(file.length()>0) objectMetadata.setContentLength(file.length());
            PutObjectRequest putObjectRequest = new PutObjectRequest(this.getProperty("bucket"), key, content, objectMetadata);

            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);
            UploadResult result = upload.waitForUploadResult();

            if(result==null){
                log.log("put oss file "+key+" failed!", Logger.LEVEL_ERROR);
                return false;
            }
            log.log("put oss file "+key+" successfully.", Logger.LEVEL_INFO);

            return true;
        }catch (Exception e){
            log.log("put oss file "+key+" failed!", Logger.LEVEL_ERROR);
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
        String key=getKey(file);
        FileInputStream is=null;
        try{
            is=new FileInputStream(localFile);

            log.log("try put oss file "+key+"(size:"+file.length()+")", Logger.LEVEL_INFO);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(localFile.length());
            PutObjectRequest putObjectRequest = new PutObjectRequest(this.getProperty("bucket"), key, is, objectMetadata);

            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);
            UploadResult result = upload.waitForUploadResult();

            try {
                is.close();
            } catch (Exception e) {}

            if(result==null){
                log.log("put oss file "+key+" failed!", Logger.LEVEL_ERROR);
                return false;
            }
            log.log("put oss file "+key+" successfully.", Logger.LEVEL_INFO);

            return true;
        }catch (Exception e){
            if(is!=null){
                try {
                    is.close();
                } catch (Exception ex) {}
            }
            log.log("put oss file "+key+" failed!", Logger.LEVEL_ERROR);
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
        COSObject result=null;
        String key=ResourceHelper.getRelativePath(file);
        try{
            log.log("try find oss file "+key, Logger.LEVEL_INFO);

            if(!client.doesObjectExist(this.getProperty("bucket"), key)){
                log.log("find oss file "+key+" failed, It's not exists.", Logger.LEVEL_ERROR);
                return null;
            }

            result=client.getObject(this.getProperty("bucket"), key);

            if(result==null){
                log.log("find oss file "+key+" failed!", Logger.LEVEL_ERROR);
                return null;
            }

            log.log("find oss file "+key+" successfully.", Logger.LEVEL_INFO);

            return result.getObjectContent();
        }catch (Exception e){
            log.log("find oss file "+key+" failed!", Logger.LEVEL_ERROR);
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