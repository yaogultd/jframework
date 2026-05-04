package j.core.nvwa;

import j.core.annotation.description.ClassDescription;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

@ClassDescription(author = "肖炯",
        date = "2021/11/27",
        description = "Ancestor为始祖之意，业务代码的顶层父类建议都继承该类，以实现自动配置参数等功能")
@Setter
@Getter
public class NvwaAncestor {
    private String nvwaObjectCode;

    /**
     *
     * @param paraGroup
     * @param paraName
     * @return
     */
    public String getParameter(String paraGroup, String paraName){
        return JUtilString.isBlank(this.nvwaObjectCode) ? Nvwa.getParameter(this, paraGroup, paraName) : Nvwa.getParameter(this.nvwaObjectCode, paraGroup, paraName);
    }

    /**
     *
     * @param key
     * @return
     */
    public String getParameter(String key){
        return JUtilString.isBlank(this.nvwaObjectCode) ? Nvwa.getParameter(this, key) : Nvwa.getParameter(this.nvwaObjectCode, key);
    }
}
