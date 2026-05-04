package j.core.nvwa;

import j.core.annotation.description.ClassDescription;
import lombok.Getter;
import lombok.Setter;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "para*.xml中的配置项",
        reviewers = {})
@Getter
@Setter
public class NvwaParameter {
    private String field;
    private String group;
    private String name;
    private String defaultValue;

    /**
     *
     * @param field
     * @param group
     * @param name
     * @param defaultValue
     */
    public NvwaParameter(String field, String group, String name, String defaultValue){
        this.field=field;
        this.group=group;
        this.name=name;
        this.defaultValue=defaultValue;
    }
}
