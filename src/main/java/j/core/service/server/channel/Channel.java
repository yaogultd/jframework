package j.core.service.server.channel;

import j.core.annotation.description.MethodDescription;

public interface Channel {
    @MethodDescription(description = "处理中任务数")
    public int getTasksInProccess();

    @MethodDescription(description = "启动通道")
    public abstract boolean startup()  throws Exception;
}
