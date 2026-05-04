package j.core.webserver;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;

@ClassDescription(author = "肖炯",
        date = "2021/11/10",
        description = "")
public interface WebServer {
    @MethodDescription(author = "肖炯",
            date = "2021/11/10",
            description = "启动")
    public void start() throws Exception;

    @MethodDescription(author = "肖炯",
            date = "2021/11/10",
            description = "停止")
    public void stop();
}
