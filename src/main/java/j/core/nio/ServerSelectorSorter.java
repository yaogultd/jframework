package j.core.nio;

import j.util.JUtilSorter;

/**
 * @author 肖炯
 * @date 2023/7/31
 */
public class ServerSelectorSorter extends JUtilSorter {
    @Override
    public String compare(Object pre, Object after) {
        ServerSelector _pre=(ServerSelector)pre;
        ServerSelector _after=(ServerSelector)after;

        int preKeys=_pre.getKeys();
        int afterKeys=_after.getKeys();
        if(preKeys < afterKeys) return JUtilSorter.SMALLER;
        else if(preKeys > afterKeys) return JUtilSorter.BIGGER;
        else return JUtilSorter.EQUAL;
    }
}
