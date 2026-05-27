package j.core.web.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;
import j.core.web.Constants;
import j.core.web.mcp.McpRouter;
import j.util.JUtilString;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class McpGatewayClient implements AutoCloseable {
    //日志
    private static Logger log = LoggerFactory.getLogger(McpGatewayClient.class);

    private static final long RECONNECT_BACKOFF_MILLIS = 1000L;
    private static final long HEARTBEAT_INTERVAL_MILLIS = 5000L;//心跳间隔
    private static final long HEARTBEAT_TIMEOUT_MILLIS = 15000L;//心跳超时
    private static final long PING_TIMEOUT_MILLIS = 5000L;//心跳请求超时
    private static final String PING_PATH = "/framework/api/web/mcp";
    private static final String PING_METHOD = "ping";

    private volatile McpSyncClient mcpClient;

    private HttpClientSseClientTransport transport;
    private final Object reconnectLock = new Object();
    private final ExecutorService pingExecutor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile long lastPingSuccessTime = 0L;
    private volatile boolean closed = false;

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

    /**
     * 初始化
     */
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
        this.lastPingSuccessTime = System.currentTimeMillis();
        log.info("[Client] Connected to MCP server at " + serverBaseUrl);

        //心跳
        this.startHeartbeat();
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
        ensureConnectionAlive();
        McpSchema.CallToolRequest request = buildRequest(path, method, RequestParam.toJson(params), accessKey, signature);
        McpSchema.CallToolResult result = mcpClient.callTool(request);
        if (result.isError() != null && result.isError()) {
            throw new RuntimeException("Server error: " + extractText(result));
        }
        return extractText(result);
    }

    /**
     * 创建请求
     * @param path
     * @param method
     * @param paramsJson
     * @param accessKey
     * @param signature
     * @return
     */
    private McpSchema.CallToolRequest buildRequest(String path, String method, String paramsJson, String accessKey, String signature) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        arguments.put("method", method);
        if(!JUtilString.isBlank(accessKey)) arguments.put(Constants.ACCESS_KEY, accessKey);
        if(!JUtilString.isBlank(signature)) arguments.put(Constants.SIGNATURE, signature);
        arguments.put("params", paramsJson);

        return McpSchema.CallToolRequest.builder()
                .name(McpRouter.GATEWAY_TOOL_NAME)
                .arguments(arguments)
                .build();
    }

    /**
     * 开启心跳监测
     */
    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(this::runHeartbeat, HEARTBEAT_INTERVAL_MILLIS, HEARTBEAT_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * 心跳
     */
    private void runHeartbeat() {
        if (closed) return;

        try {
            if (pingWithTimeout()) {
                lastPingSuccessTime = System.currentTimeMillis();
                return;
            }
            log.warn("[Client] Heartbeat ping returned unexpected response");
        } catch (Exception ex) {
            log.warn("[Client] Heartbeat ping failed", ex);
        }

        if (System.currentTimeMillis() - lastPingSuccessTime > HEARTBEAT_TIMEOUT_MILLIS) {
            try {
                log.warn("[Client] Heartbeat timeout detected, trying reconnect");
                reconnect(1);
            } catch (Exception reconnectEx) {
                log.error("[Client] Reconnect after heartbeat timeout failed", reconnectEx);
            }
        }
    }

    /**
     *
     * @return
     * @throws Exception
     */
    private boolean pingWithTimeout() throws Exception {
        Future<Boolean> future = pingExecutor.submit(this::doPing);
        try {
            return future.get(PING_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeoutEx) {
            log.warn("[Client] Heartbeat ping timeout after {} ms", PING_TIMEOUT_MILLIS, timeoutEx);
            throw timeoutEx;
        } finally {
            future.cancel(true);
        }
    }

    /**
     * 发送ping请求
     * @return
     */
    private boolean doPing() {
        McpSchema.CallToolRequest pingRequest = buildRequest(
                PING_PATH,
                PING_METHOD,
                RequestParam.toJson(Collections.emptyList()),
                null,
                null
        );
        McpSchema.CallToolResult result = mcpClient.callTool(pingRequest);
        if (result.isError() != null && result.isError()) {
            return false;
        }
        return true;
    }

    /**
     *
     * @throws Exception
     */
    private void ensureConnectionAlive() throws Exception {
        if (System.currentTimeMillis() - lastPingSuccessTime <= HEARTBEAT_TIMEOUT_MILLIS) {
            return;
        }
        log.warn("[Client] Connection marked as timeout by heartbeat, reconnect before invoke");
        reconnect(1);
    }

    /**
     * 重连
     * @param reconnectAttempt
     * @throws Exception
     */
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

    /**
     * 关闭连接
     */
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
     * 提取响应结果
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
        closed = true;
        heartbeatScheduler.shutdownNow();
        closeQuietly();
        pingExecutor.shutdownNow();
    }
}
