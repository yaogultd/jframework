package j.core.dao.partition;

import j.core.annotation.description.ClassDescription;

@ClassDescription(author = "肖炯",
        date = "2022/06/30",
        description = "框架不实现具体的分表逻辑，业务代码可继承该类来实现分表")
public abstract class TablePartitioner {
    /**
     *
     * @return
     */
    public abstract String getMainTable();
}
