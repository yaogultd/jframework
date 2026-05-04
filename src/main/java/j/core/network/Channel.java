package j.core.network;

import j.core.annotation.description.ClassDescription;
import lombok.Getter;
import lombok.Setter;

@ClassDescription(author = "肖炯",
        date = "2021/12/14",
        description = "主机和端口")
@Getter
@Setter
public class Channel {
    protected String host;
    protected int port;
    protected Boolean available;

    /**
     *
     * @param host
     * @param port
     */
    public Channel(String host, int port){
        this.host=host;
        this.port=port;
        this.available=Boolean.valueOf(false);
    }

    @Override
    public String toString(){
        StringBuffer s=new StringBuffer();
        s.append("{\"host\":\""+this.host+"\"");
        s.append(",\"port\":\""+this.port+"\"");
        s.append(",\"available\":\""+this.available+"\"");
        s.append("}");
        return s.toString();
    }
}
