package j.core.web.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;
import j.core.web.Constants;
import j.core.web.mcp.McpRouter;
import j.core.web.mcp.RequestParam;
import j.util.JUtilString;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McpGatewayClient implements AutoCloseable {
    //日志
    private static Logger log = LoggerFactory.getLogger(McpGatewayClient.class);
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_BACKOFF_MILLIS = 1000L;

    private McpSyncClient mcpClient;

    private HttpClientSseClientTransport transport;
    private final Object reconnectLock = new Object();

    @Getter
    private String serverBaseUrl;

    private ServerLogConsumer serverLogConsumer;

    /**
     *
     * @param serverBaseUrl
     */
    public McpGatewayClient(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
        this.init();
    }

    /**
     *
     * @param serverBaseUrl
     * @param serverLogConsumer
     */
    public McpGatewayClient(String serverBaseUrl, ServerLogConsumer serverLogConsumer) {
        this.serverBaseUrl = serverBaseUrl;
        this.serverLogConsumer = serverLogConsumer;
        this.init();
    }

    private void init(){
        // 例如 serverBaseUrl 为 "http://localhost:8080/mcp"
        this.transport = HttpClientSseClientTransport.builder(serverBaseUrl)
                .jsonMapper(McpJsonDefaults.getMapper())
                .connectTimeout(Duration.ofMinutes(1))
                .build();

        // 创建同步客户端
        McpClient.SyncSpec syncSpec = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("PT3A Client", "1.0.0"))
                .requestTimeout(Duration.ofHours(1));

        if(this.serverLogConsumer != null){
            syncSpec.loggingConsumer(serverLogConsumer);
        }

        this.mcpClient = syncSpec.build();

        // 建立连接并初始化
        this.mcpClient.initialize();
        log.info("[Client] Connected to MCP server at " + serverBaseUrl);
    }

    /**
     * 客户端暴露的唯一方法，将请求路由信息和参数通过唯一工具发送给服务端。
     * @param path    目标控制器路径，如 "/sample"
     * @param method  目标方法名，如 "add"
     * @param params 业务参数
     * @param accessKey 标识客户端身份
     * @param signature 签名
     * @return
     */
    public String invoke(String path, String method, List<RequestParam> params, String accessKey, String signature) throws Exception{
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        arguments.put("method", method);
        if(!JUtilString.isBlank(accessKey)) arguments.put(Constants.ACCESS_KEY, accessKey);
        if(!JUtilString.isBlank(signature)) arguments.put(Constants.SIGNATURE, signature);
        arguments.put("params", RequestParam.toJson(params));

        System.out.println("RequestParam.toJson(params) = " + RequestParam.toJson(params));

        McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
                .name(McpRouter.GATEWAY_TOOL_NAME)
                .arguments(arguments)
                .build();

        McpSchema.CallToolResult result = null;
        int reconnectAttempt = 0;
        while (result == null) {
            try {
                result = mcpClient.callTool(request);
            } catch (Exception ex) {
                reconnectAttempt++;
                if (reconnectAttempt > MAX_RECONNECT_ATTEMPTS) {
                    log.error("[Client] MCP call failed after {} reconnect attempts", MAX_RECONNECT_ATTEMPTS, ex);
                    throw ex;
                }
                log.warn("[Client] MCP connection may be broken, trying reconnect {}/{}", reconnectAttempt, MAX_RECONNECT_ATTEMPTS, ex);
                reconnect(reconnectAttempt);
            }
        }

        if (result.isError() != null && result.isError()) {
            throw new RuntimeException("Server error: " + extractText(result));
        }
        return extractText(result);
    }

    private void reconnect(int reconnectAttempt) throws Exception {
        synchronized (reconnectLock) {
            closeQuietly();
            if (reconnectAttempt > 1) {
                Thread.sleep(RECONNECT_BACKOFF_MILLIS * reconnectAttempt);
            }
            init();
            log.info("[Client] Reconnected to MCP server at {}", serverBaseUrl);
        }
    }

    private void closeQuietly() {
        if (mcpClient == null) {
            return;
        }
        try {
            mcpClient.closeGracefully();
        } catch (Exception closeEx) {
            log.warn("[Client] Ignore error while closing broken MCP client", closeEx);
        }
    }

    /**
     *
     * @param result
     * @return
     */
    private String extractText(McpSchema.CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return "";
        }
        Object content = result.content().get(0);
        if (content instanceof McpSchema.TextContent textContent) {
            return textContent.text();
        }
        return content.toString();
    }

    @Override
    public void close() throws Exception {
        closeQuietly();
    }
}
