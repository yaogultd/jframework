package j.core.nvwa;

import j.core.annotation.description.ClassDescription;
import j.core.service.client.Client;
import j.core.service.registry.Registry;
import j.core.service.server.Server;
import j.util.JUtilMath;

@ClassDescription(author = "肖炯",
        date = "2021/11/16",
        description = "Nvwa.xml中如未配置starter，则使用该默认starter")
public class NvwaStarterDefault implements NvwaStarter{
    @Override
    public void startup(String[] args) throws Exception{
        //启动注册中心
        //通信层
        System.out.println("[j.core.nvwa.NvwaStarterDefault] 尝试启动注册中心......");
        Registry.start();

        //启动服务
        //tcp缓冲区大小
        String tcpBufferSize = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "tcpBufferSize");
        String isServer = j.core.nvwa.Nvwa.getParameter(Server.class,"SERVICE", "isServer");
        System.out.println("[j.core.nvwa.NvwaStarterDefault] tcpBufferSize of service is "+tcpBufferSize);

        //通信层
        //启动服务Client
        System.out.println("[j.core.nvwa.NvwaStarterDefault] 尝试启动服务Client......");
        Client.start();

        if(JUtilMath.isInt(tcpBufferSize)){
            System.out.println("[j.core.nvwa.NvwaStarterDefault] 尝试启动服务......");
            Server.start();
            if("true".equalsIgnoreCase(isServer)){
                new Thread(new Server()).start();
            }
        }
    }
}