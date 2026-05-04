package j.core.service.registry;

import j.core.annotation.description.ClassDescription;
import j.core.service.server.config.Service;
import j.util.JUtilSorter;

@ClassDescription(author = "肖炯",
        date = "2021/12/15",
        description = "根据服务端主机上待处理任务进行排序")
public class HostSorter extends JUtilSorter {
    @Override
    public String compare(Object pre, Object after) {
        if(pre == null || after == null) return JUtilSorter.EQUAL;

        if(!(pre instanceof Registration) || !(after instanceof Registration)) return JUtilSorter.EQUAL;

        Registration _pre=(Registration)pre;
        Registration _after=(Registration)after;

        if(_pre.getService().getPackages() < _after.getService().getPackages()) return JUtilSorter.SMALLER;
        else if(_pre.getService().getPackages() > _after.getService().getPackages()) return JUtilSorter.BIGGER;
        else return JUtilSorter.EQUAL;
    }
}
