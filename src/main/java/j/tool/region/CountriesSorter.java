package j.tool.region;

import j.util.JUtilSorter;

public class CountriesSorter extends JUtilSorter {
    @Override
    public String compare(Object pre, Object after) {
        CountryData _pre = (CountryData) pre;
        CountryData _after = (CountryData) after;

        int compared = _pre.getCnName().compareTo(_after.getCnName());
        if(compared < 0){
            return JUtilSorter.SMALLER;
        }else if(compared > 0){
            return JUtilSorter.BIGGER;
        }
        return JUtilSorter.EQUAL;
    }
}
