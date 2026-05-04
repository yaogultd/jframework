package j.core.permission.ticket;

import j.core.cache.JCacheFilter;

public class TicketRemover implements JCacheFilter {
    @Override
    public boolean matches(Object object) {
        if(object==null || !(object instanceof Ticket)) return false;
        return ((Ticket)object).isExpired();
    }
}
