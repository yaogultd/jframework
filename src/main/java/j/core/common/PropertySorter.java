package j.core.common;

import j.util.JUtilKeyValue;
import j.util.JUtilSorter;

/**
 * @author 肖炯
 */
public class PropertySorter extends JUtilSorter {
    private static final long serialVersionUID = 1L;

    /*
     *  (non-Javadoc)
     * @see j.util.JUtilSorter#compare(java.lang.Object, java.lang.Object)
     */
    public String compare(Object pre, Object after) {
        JUtilKeyValue beanPre = (JUtilKeyValue) pre;
        JUtilKeyValue beanAfter = (JUtilKeyValue) after;
        String beanPreId = (String) beanPre.getKey();
        String beanAfterId = (String) beanAfter.getKey();

        if (beanPre.getNo() < beanAfter.getNo()) {
            return JUtilSorter.SMALLER;
        } else if (beanPre.getNo() > beanAfter.getNo()) {
            return JUtilSorter.BIGGER;
        } else {
            if (beanPreId.compareTo(beanAfterId) > 0) {
                return JUtilSorter.BIGGER;
            } else {
                return JUtilSorter.SMALLER;
            }
        }
    }
}
