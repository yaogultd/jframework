package j.core.dao.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Column {
    private String name;
    private boolean ignoreWhileUpdateViaBean;
    private boolean timeLoadAsLocal=false;

    public Column(String name){
        this.name=name;
    }
}
