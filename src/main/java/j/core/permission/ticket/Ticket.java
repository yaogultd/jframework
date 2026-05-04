package j.core.permission.ticket;

import j.core.common.JSerializable;
import j.util.JUtilString;
import j.util.JUtilUUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticket extends JSerializable {
    private Object related;
    private String ticket;
    private long created;
    private long survival;

    /**
     *
     * @param related 与ticket关联的对象，比如用户ID、accessToken等
     * @param ticket 不指定则自动生成UUID
     * @param survival 有效时间（ms）
     */
    public Ticket(Object related, String ticket, long survival){
        this.related = related;
        this.ticket = (JUtilString.isBlank(ticket) ? JUtilUUID.genUUID() : ticket);
        this.created = System.currentTimeMillis();
        this.survival = survival;
    }

    /**
     *
     * @return
     */
    public boolean isExpired(){
        return System.currentTimeMillis() - this.created >= this.survival;
    }
}
