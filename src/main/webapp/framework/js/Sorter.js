//冒泡排序
let Sorter={
    SORT_DESC:'DESC',
    SORT_ASC:'ASC',
    COMPARE_SMALLER:-1,
    COMPARE_BIGGER:1,
    COMPARE_EQUAL:0,

    bubble:function(original,sortType,comparer,additionalParams){//冒泡排序
        let cnt = original.length;
        for (let j = 0; j < cnt - 1; ++j) {
            for (let i = 1; i < cnt - j; ++i) {
                let pre = original[i - 1];
                let after = original[i];

                let comp=0;
                if(comparer){
                    comp=comparer.compare(pre, after, additionalParams);
                }else{
                    if(pre<after) comp=this.COMPARE_SMALLER;
                    else if(pre>after) comp=this.COMPARE_BIGGER;
                    else comp=this.COMPARE_EQUAL;
                }

                if(sortType==this.SORT_DESC){
                    if(comp==this.COMPARE_SMALLER) {
                        original[i - 1]=after;
                        original[i]=pre;
                    }
                }else{
                    if(comp==this.COMPARE_BIGGER) {
                        original[i - 1]=after;
                        original[i]=pre;
                    }
                }
            }
        }
        return original;
    }
}
window.Sorter=Sorter;