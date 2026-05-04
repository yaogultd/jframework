package j.core.service.registry;

import j.core.annotation.description.ClassDescription;
import j.core.service.server.config.Service;
import j.util.JUtilSorter;

@ClassDescription(author = "肖炯",
        date = "2021/12/15",
        description = "根据服务端主机某端口上待处理任务进行排序")
public class PortSorter extends JUtilSorter {
    @Override
    public String compare(Object pre, Object after) {
        return this.compare(pre, after, null);
    }

    @Override
    public String compare(Object pre, Object after, Object extra) {
        if(pre == null || after == null || extra == null) return JUtilSorter.EQUAL;

        if(!(pre instanceof Integer) || !(after instanceof Integer) || !(extra instanceof Service)) return JUtilSorter.EQUAL;

        Integer _pre=(Integer)pre;
        Integer _after=(Integer)after;
        Service service=(Service)extra;

        if(service.getPackages(_pre) < service.getPackages(_after)) return JUtilSorter.SMALLER;
        else if(service.getPackages(_pre) > service.getPackages(_after)) return JUtilSorter.BIGGER;
        else return JUtilSorter.EQUAL;
    }
}

