package j.core.web.mcp;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import j.core.web.handler.JResponse;
import j.core.web.handler.Server;
import j.log.Logger;
import j.util.JUtilString;

import java.util.List;
import java.util.Map;


public class McpRouter {
    private static Logger log=Logger.create(McpRouter.class);//日志输出

    public McpRouter() {

    }

    /**
     * 唯一的 MCP Tool 名称
     */
    public static final String GATEWAY_TOOL_NAME = "mcp_gateway";

    /**
     * 构建 MCP Gateway Tool Schema
     */
    public static McpSchema.Tool buildGatewayTool() {
        return McpSchema.Tool.builder()
                .name(GATEWAY_TOOL_NAME)
                .description("A single entry point for all remote method invocations.")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of(
                                "path", Map.of("type", "string", "description", "The controller path"),
                                "method", Map.of("type", "string", "description", "The operation name"),
                                "accessKey", Map.of("type", "string", "description", "The access key for auth"),
                                "signature", Map.of("type", "string", "description", "The signature for auth"),
                                "params", Map.of("type", "string", "description", "Business parameters in json format")
                        ),
                        List.of("path", "method"),
                        false,
                        Map.of(),
                        Map.of()
                )).build();
    }

    /**
     * 分发请求
     *
     * @param requestArgs 请求参数
     * @param exchange    MCP 服务端交换对象，可用于发送进度、日志等通知
     * @return 业务方法返回值序列化后的 JSON 字符串
     */
    public static String dispatch(Map<String, Object> requestArgs, McpSyncServerExchange exchange) {
        try {
            String path = (String) requestArgs.get("path");
            String methodName = (String) requestArgs.get("method");
            String accessKey = (String) requestArgs.get("accessKey");
            String signature = (String) requestArgs.get("signature");
            String params = (String) requestArgs.get("params");

            String requestURI = JUtilString.appendUrl(path, methodName);
            JResponse response = Server.service(exchange, requestURI, params, accessKey, signature);
            return response.toString();
        } catch (Exception e) {
            log.log(e, Logger.LEVEL_ERROR);
            return (new JResponse(false, "ERR", "调用错误")).toString();
        }
    }
}