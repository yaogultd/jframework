package j.core.web.mcp;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.web.handler.JHandler;
import j.core.web.handler.JResponse;
import j.core.web.handler.JSession;

@Handler(path = "/framework/api/web/mcp")
public class McpServerPing extends JHandler {
    @Action
    public void ping(JSession jsession) throws Exception {
        jsession.jresponse = new JResponse(true, "ok", "");
    }
}
