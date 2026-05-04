package j.core.web.mcp;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.web.handler.JHandler;
import j.core.web.handler.JResponse;
import j.core.web.handler.JSession;

@Handler(path = "/helloworld")
public class McpServerTest extends JHandler {
    @Action
    public void hi(JSession jsession) throws Exception {
        System.out.println("I got => " + jsession.getRequestBody());
        jsession.jresponse = new JResponse(true, "abc", "okok");
    }
}
