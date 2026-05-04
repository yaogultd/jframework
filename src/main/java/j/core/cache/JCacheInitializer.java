package j.core.cache;

import j.core.annotation.description.ClassDescription;

import java.io.Serializable;

@ClassDescription(author = "肖炯",
        date = "2022/06/28",
        description = "系统启动时完成业务需求的缓存初始化")
public interface JCacheInitializer extends Serializable {
    /**
     *
     * @param unit
     */
    public void initialize(JCacheUnit unit);
}
