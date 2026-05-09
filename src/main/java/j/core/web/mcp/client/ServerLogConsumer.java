package j.core.web.mcp.client;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ServerLogConsumer implements Consumer<McpSchema.LoggingMessageNotification> {
    //日志
    private static Logger log = LoggerFactory.getLogger(ServerLogConsumer.class);

    @Override
    public void accept(McpSchema.LoggingMessageNotification notification) {
        // 当收到服务端的流式数据（日志通知）时触发
        if ("stream".equals(notification.logger())) {
            log.info("接收到流式数据: " + notification.data());
        } else {
            log.debug("[Server Log] " + notification.data());
        }
    }
}
