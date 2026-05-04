package j.core.web;

import j.core.type.Result;
import j.core.web.handler.JSession;
import jakarta.servlet.http.HttpServletRequest;

public abstract class JValidator {
    /**
     * 验证http请求数据有效性
     * @param session
     * @param request
     * @return
     */
    public abstract Result validate(JSession session, HttpServletRequest request);
}
