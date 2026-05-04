package j.core.web.security;

import j.core.annotation.configuration.Properties;
import j.core.nvwa.NvwaAncestor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 机器人检测，一般通过第三方组件实现，默认不做检测
 */
public class RobotInspector extends NvwaAncestor {
    /**
     * 是否通过了机器人检测
     * @param request
     * @return
     */
    public boolean pass(HttpServletRequest request){
        return true;
    }
}
