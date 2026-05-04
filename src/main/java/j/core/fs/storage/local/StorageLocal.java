package j.core.fs.storage.local;

import j.core.fs.JDFS;
import j.core.fs.JDFSFile;
import j.core.fs.JFileMeta;
import j.core.fs.storage.Storage;
import j.core.service.ServiceResponse;
import j.core.service.client.Client;
import j.log.Logger;
import j.util.JUtilInputStream;
import j.util.JUtilString;
import org.dom4j.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class StorageLocal extends Storage {
    //日志
    private static Logger log=Logger.create(StorageLocal.class);

    /**
     * @param propertyElements
     */
    public StorageLocal(List<Element> propertyElements) {
        super(propertyElements);
    }

    /**
     * 获得文件元数据信息
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return
     */
    public JFileMeta getMeta(JDFSFile file){
        return new JFileMeta(new File(file.getPhysicalPath()));
    }

    /**
     * 列出目录中文件
     * @param file
     * @return
     */
    public String[] list(JDFSFile file){
        File local=new File(file.getPhysicalPath());
        if(local.exists()) return local.list();

        try{
            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return null;

            ServiceResponse<String[]> resp=service.list(file.getPath());

            return resp==null?null:resp.getResponse();
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public boolean mkdirs(JDFSFile file){
        File local=new File(file.getPhysicalPath());
        local.mkdirs();

        try{
            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return true;

            List<ServiceResponse> resps=j.core.service.ServiceAdapter.callAll(true, true, service, "mkdirs", null, null, null, file.getPath(), null);
            return true;
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     *
     * @param file
     * @param dest
     * @return
     */
    public boolean renameTo(JDFSFile file, File dest){
        File local=new File(file.getPhysicalPath());
        if(local.exists()) local.renameTo(new File(JDFS.getPhysicalPath(dest.getPath())));

        try{
            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return true;

            List<ServiceResponse> resps=j.core.service.ServiceAdapter.callAll(true, true, service, "renameTo", null, null, null, null, new Object[]{file.getPath(), dest.getPath()});
            return true;
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return false;
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public byte[] bytes(JDFSFile file){
        try{
            File local=new File(file.getPhysicalPath());
            if(local.exists()) return JUtilInputStream.bytes(new FileInputStream(local));

            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return null;

            ServiceResponse<byte[]> resp=service.bytes(file.getPath());

            return resp==null?null:resp.getResponse();
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
    }

    /**
     *
     * @param file
     * @param encoding
     * @return
     */
    public String string(JDFSFile file, String encoding){
        try{
            File local=new File(file.getPhysicalPath());
            if(local.exists()){
                if(!JUtilString.isBlank(encoding)) return JUtilInputStream.string(new FileInputStream(local),encoding);
                else return JUtilInputStream.string(new FileInputStream(local));
            }

            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return null;

            ServiceResponse<String> resp=service.string(new Object[]{file.getPath(), encoding});

            return resp==null?null:resp.getResponse();
        }catch(Exception e){
            log.log(e, Logger.LEVEL_ERROR);
            return null;
        }
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
        JDFSFile.save(file.getPhysicalPath(), content, append, encoding);

        try{
            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return true;

            List<ServiceResponse> resps=j.core.service.ServiceAdapter.callAll(true, true, service, "save", null, null, null, null, new Object[]{file.getPath(), content, Boolean.valueOf(append), encoding});
            return true;
        }catch(Exception e){
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
            JDFSFile.save(content, file.getPhysicalPath());

            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return true;

            List<ServiceResponse> resps=j.core.service.ServiceAdapter.callAll(true, true, service, "save", null, null, null, null, new Object[]{file.getPath(), JUtilInputStream.string(new FileInputStream(file.getPhysicalPath())), Boolean.valueOf(false), null});
            return true;
        }catch(Exception e){
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
            JDFSFile.save(new FileInputStream(localFile), file.getPhysicalPath());

            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null) return true;

            j.core.service.ServiceAdapter.callAll(true, true, service, "save", null, null, null, null, new Object[]{file.getPath(), JUtilInputStream.string(new FileInputStream(file.getPhysicalPath())), Boolean.valueOf(false), null});
            return true;
        }catch(Exception e){
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
            StorageService service= (StorageService) j.core.service.ServiceAdapter.getService(this.getProperty("service"));
            if(service == null){
                File local=new File(file.getPhysicalPath());
                JDFSFile.delete(local);
                return true;
            }

            j.core.service.ServiceAdapter.callAll(true, false, service, "delete", null, null, null, file.getPath(), null);
            return true;
        }catch(Exception e){
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
        return file;
    }
}
