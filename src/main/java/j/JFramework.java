package j;

import j.core.common.Global;
import j.core.common.JProperties;
import j.core.fs.JDFSFile;
import j.core.nvwa.Nvwa;
import j.core.web.mcp.client.McpGatewayClient;
import j.core.webserver.undertow.UndertowConf;
import j.core.webserver.undertow.UndertowWebServer;
import j.util.JUtilBean;
import j.util.JUtilString;

import java.util.Map;

public class JFramework {
    public static void main(String[] args) throws Exception{
        UndertowConf undertowConf=new UndertowConf();

        if(undertowConf.isEnabled()) {//应用在web服务器内启动
            if (args != null && args.length > 0) {//启动参数拼接后保存到文件，以便Nvwa.startup获取到（类加载器不一致导致无法直接传递）
                String _args = "";
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) _args += "^";
                    _args += args[i];
                }

                String userDir = JProperties.getUserDir();
                userDir = JUtilString.replaceAll(userDir, "\\", "/");

                JDFSFile.save(JUtilString.appendPath(userDir, "Startup.Args"),
                        _args,
                        false,
                        "UTF-8");
            }

            //尝试启动嵌入式undertow
            try {
                UndertowWebServer undertowWebServer = new UndertowWebServer();
                undertowWebServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        while(!Nvwa.isScanned()){
            Global.sleep1000Millis();
            System.out.println("等待系统完成启动......");
        }


        McpGatewayClient client = new McpGatewayClient("http://localhost:8080/mcp");
        System.out.println("Client created.");

        // 调用 "/sample" 路由下的 "echo" 方法
        System.out.println("\n[1] Calling /helloworld -> hi ...");
        String echoResult = client.invoke("/helloworld", "hi", null, null, JUtilBean.map2Json(Map.of("name", "哈哈")));
        System.out.println("Result1: " + echoResult);
    }
}