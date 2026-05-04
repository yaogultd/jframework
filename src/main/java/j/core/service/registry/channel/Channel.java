package j.core.service.registry.channel;

import j.core.annotation.description.MethodDescription;

public interface Channel {
    @MethodDescription(description = "启动通道")
    public abstract boolean startup()  throws Exception;
}
