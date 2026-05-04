package j.core.nvwa;

import j.core.annotation.description.ClassDescription;
import lombok.Getter;
import lombok.Setter;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = ".properties中的配置项",
        reviewers = {})
@Getter
@Setter
public class NvwaProperty {
    private String path;
    private String field;
    private String name;
    private String defaultValue;

    /**
     *
     * @param field
     * @param path
     * @param name
     * @param defaultValue
     */
    public NvwaProperty(String field, String path, String name, String defaultValue){
        this.field=field;
        this.path=path;
        this.name=name;
        this.defaultValue=defaultValue;
    }
}
