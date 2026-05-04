package j.core.nvwa;

import j.core.annotation.description.ClassDescription;

@ClassDescription(author = "肖炯",
        date = "2021/11/16",
        description = "系统完成加载时（即资源扫描时），调用指定的starter（j.core.nvwa.NvwaStarter的子类）")
public interface NvwaStarter {
    /**
     *
     * @param args
     * @throws Exception
     */
    public void startup(String[] args) throws Exception;
}
