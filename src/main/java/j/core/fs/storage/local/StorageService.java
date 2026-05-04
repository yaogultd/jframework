package j.core.fs.storage.local;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.service.Service;
import j.core.fs.JDFS;
import j.core.fs.JDFSFile;
import j.core.fs.JFileMeta;
import j.core.service.ServiceBase;
import j.core.service.ServiceResponse;
import j.log.Logger;
import j.util.JUtilInputStream;
import j.util.JUtilString;

import java.io.File;
import java.io.FileInputStream;


@ClassDescription(author = "肖炯",
        date = "2022/04/20",
        description = "",
        reviewers = {"肖炯"})
@Service(path = "/JDFS-SERVICE")
public class StorageService extends ServiceBase {
    @FieldDescription(description = "日志输出")
    private static Logger log = Logger.create(StorageService.class);

    @Service(path = "getMeta")
    public ServiceResponse<JFileMeta> getMeta(String path){
        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        File local=new File(JDFS.getPhysicalPath(path));

        return new ServiceResponse(false,"1","", new JFileMeta(local));
    }

    @Service(path = "list")
    public ServiceResponse<String[]> list(String path) throws Exception {
        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        File local=new File(JDFS.getPhysicalPath(path));
        if(!local.exists()){
            return new ServiceResponse(false, "not_exists", "the file is not exits.");
        }

        if(!local.isDirectory()){
            return new ServiceResponse(false, "-3", "不是目录");
        }

        return new ServiceResponse(false,"1","", local.list());
    }

    @Service(path = "mkdirs")
    public ServiceResponse<Boolean> mkdirs(String path) throws Exception {
        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        File local=new File(JDFS.getPhysicalPath(path));

        return new ServiceResponse(false,"1","", local.mkdirs());
    }

    @Service(path = "renameTo")
    public ServiceResponse<Boolean> renameTo(Object[] objects) throws Exception {
        if(objects==null || objects.length != 2){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        String path=(String)objects[0];
        String to=(String)objects[1];

        if(JUtilString.isBlank(path) || JUtilString.isBlank(to)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        File local=new File(JDFS.getPhysicalPath(path));
        if(!local.exists()){
            return new ServiceResponse(false, "not_exists", "the file is not exits.");
        }

        return new ServiceResponse(false,"1","", local.renameTo(new File(JDFS.getPhysicalPath(to))));
    }

    @Service(path = "bytes")
    public ServiceResponse<byte[]> bytes(String path) throws Exception {
        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        File local=new File(JDFS.getPhysicalPath(path));
        if(!local.exists()){
            return new ServiceResponse(false, "not_exists", "the file is not exits.");
        }

        if(local.isDirectory()){
            return new ServiceResponse(false, "is_directory", "the file is directory.");
        }

        return new ServiceResponse(false,"1","", JUtilInputStream.bytes(new FileInputStream(local)));
    }

    @Service(path = "string")
    public ServiceResponse<String> string(Object[] objects) throws Exception {
        if(objects==null || objects.length != 2){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        String path=(String)objects[0];
        String encoding=(String)objects[1];

        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        File local=new File(JDFS.getPhysicalPath(path));
        if(!local.exists()){
            return new ServiceResponse(false, "not_exists", "the file is not exits.");
        }

        if(local.isDirectory()){
            return new ServiceResponse(false, "is_directory", "the file is directory.");
        }

        if(!JUtilString.isBlank(encoding)){
            return new ServiceResponse(false,"1","", JUtilInputStream.string(new FileInputStream(local),encoding));
        }else {
            return new ServiceResponse(false,"1","", JUtilInputStream.string(new FileInputStream(local)));
        }
    }

    @Service(path = "save")
    public ServiceResponse<Boolean> save(Object[] objects) throws Exception {
        if(objects==null || objects.length != 4){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        String path=(String)objects[0];
        String content=(String)objects[1];
        Boolean append=(Boolean)objects[2];
        String encoding=(String)objects[3];

        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        JDFSFile.save(JDFS.getPhysicalPath(path), content, append, encoding);

        return new ServiceResponse(false,"1","", true);
    }

    @Service(path = "delete")
    public ServiceResponse<Boolean> delete(String path) throws Exception {
        if(JUtilString.isBlank(path)){
            return new ServiceResponse(false, "invalid_request", "data format error.");
        }

        //本地文件
        JDFSFile.delete(new File(JDFS.getPhysicalPath(path)));

        return new ServiceResponse(false,"1","", true);
    }
}