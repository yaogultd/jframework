package j.core.web.mcp.client;

import j.util.JUtilBean;

import java.util.Map;

public class McpGatewayClientExample {
    public static void main(String[] args) {
        System.out.println("Starting McpGatewayClientExample...");
        // 创建客户端连接
        try {
            McpGatewayClient client = new McpGatewayClient("http://localhost:8080/mcp");
            System.out.println("Client created.");

            // 调用 "/sample" 路由下的 "echo" 方法
            System.out.println("\n[1] Calling /sample -> echo ...");
            String echoResult = client.invoke("/sample", "echo", null, null, JUtilBean.map2Json(Map.of("message", "Hello Remote MCP!")));
            System.out.println("Result: " + echoResult);

            // 调用 "/sample" 路由下的 "add" 方法
            System.out.println("\n[2] Calling /sample -> add ...");
            String addResult = client.invoke("/sample", "add",null, null, JUtilBean.map2Json(Map.of("a", 50, "b", 70)));
            System.out.println("Result: " + addResult);

            // 测试一个不存在的路由，预期抛出异常或返回错误信息
            System.out.println("\n[3] Calling /sample -> not_exist ...");
            try {
                String errorResult = client.invoke("/sample", "not_exist",null, null, JUtilBean.map2Json(Map.of()));
                System.out.println("Result: " + errorResult);
            } catch (Exception e) {
                System.out.println("Expected error: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
