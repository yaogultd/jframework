package j.core.web.handler;

import j.core.annotation.description.ClassDescription;
import j.core.dao.partition.TablePartitioner;

@ClassDescription(author = "肖炯",
        date = "2022/06/30",
        description = "框架常规访问日志的分表逻辑实现")
public class TablePartitioner4ActionLog extends TablePartitioner {
    @Override
    public String getMainTable() {
        return "j_action_log";
    }
}
