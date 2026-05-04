package j.core.web.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;
import j.core.web.Constants;
import j.log.Logger;
import j.util.JUtilString;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class McpGatewayClient implements AutoCloseable {
    private static Logger log=Logger.create(McpGatewayClient.class);//日志输出

    private final McpSyncClient mcpClient;
    private final HttpClientSseClientTransport transport;

    public McpGatewayClient(String serverBaseUrl) {
        // 创建基于 SSE 的 HTTP 传输层。
        // 例如 serverBaseUrl 为 "http://localhost:8080/mcp"
        this.transport = HttpClientSseClientTransport.builder(serverBaseUrl)
                .jsonMapper(McpJsonDefaults.getMapper())
                .connectTimeout(Duration.ofMinutes(1))
                .build();

        // 创建同步客户端
        this.mcpClient = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("mcp-gateway-remote-client", "1.0.0"))
                .requestTimeout(Duration.ofHours(1))
                .build();

        // 建立连接并初始化
        this.mcpClient.initialize();
        System.out.println("[Client] Connected to MCP server at " + serverBaseUrl);
    }

    /**
     * 客户端暴露的唯一方法，将请求路由信息和参数通过唯一工具发送给服务端。
     * @param path    目标控制器路径，如 "/sample"
     * @param method  目标方法名，如 "add"
     * @param accessKey
     * @param signature
     * @param paramsJson 业务参数
     * @return
     */
    public String invoke(String path, String method, String accessKey, String signature, String paramsJson) throws Exception{
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        arguments.put("method", method);
        if(!JUtilString.isBlank(accessKey)) arguments.put(Constants.ACCESS_KEY, accessKey);
        if(!JUtilString.isBlank(signature)) arguments.put(Constants.SIGNATURE, signature);
        arguments.put("params", paramsJson);

        McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
                .name(McpRouter.GATEWAY_TOOL_NAME)
                .arguments(arguments)
                .build();

        McpSchema.CallToolResult result = mcpClient.callTool(request);

        if (result.isError() != null && result.isError()) {
            throw new RuntimeException("Server error: " + extractText(result));
        }
        return extractText(result);
    }

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
        mcpClient.closeGracefully();
    }
}