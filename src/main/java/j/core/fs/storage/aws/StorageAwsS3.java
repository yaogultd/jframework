package j.core.fs.storage.aws;

import j.core.fs.JDFSFile;
import j.core.fs.JFileMeta;
import j.core.fs.storage.Storage;
import org.dom4j.Element;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class StorageAwsS3 extends Storage {
    /**
     * @param propertyElements
     */
    public StorageAwsS3(List<Element> propertyElements) {
        super(propertyElements);
    }

    /**
     * 获得文件元数据信息
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return
     */
    public JFileMeta getMeta(JDFSFile file){
        return null;
    }

    /**
     * 列出目录中文件
     * @param file
     * @return
     */
    public String[] list(JDFSFile file){
        return null;
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
        return null;
    }

    /**
     *
     * @param file
     * @param encoding
     * @return
     */
    public String string(JDFSFile file, String encoding){
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
        return false;
    }

    /**
     * 保存文件
     * @param file
     * @param content 文件内容
     * @param closeStreamOnEnd
     * @return
     */
    public boolean save(JDFSFile file, InputStream content, boolean closeStreamOnEnd){
        return false;
    }

    /**
     *
     * @param file
     * @param localFile 本地文件
     * @return
     */
    public boolean save(JDFSFile file, File localFile){
        return false;
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    @Override
    public boolean delete(JDFSFile file) {
        return false;
    }

    /**
     * 获取文件
     *
     * @param file 文件名、文件路径或其它能为存储器所识别的能唯一标识某文件的值
     * @return 返回对象类型取决于具体的存储器
     */
    @Override
    public Object find(String file) {
        return null;
    }
}
