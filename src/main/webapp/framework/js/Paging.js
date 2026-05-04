//通用分页
/**
 *
 * @param id 组件ID
 * @param containers [组件容器]，以数组方式指定，指定多个表示在页码多个位置显示分页组件以方便交互
 * @param recordsPerPage 每页多少条（records per page）
 * @param pagesPerSection 每个页码片段显示多少个页码（pages per section）
 * @param pn 当前第几页（page number)
 * @param total 总记录条数
 * @param goto 加载某页的方法，如不指定则约定调用name为frm的表单的submit、并约定frm包含name为pn的表示页码的字段
 * @param showSummary 是否显示形如“共xx条记录”的统计信息，默认显示
 * @param showPageNumbers 是否显示页码，默认显示，设为false则显示为"当前页码/总页数"的形式（多用于手机端）
 * @param hideIfNoRecord 没记录时是否隐藏分页组件，默认不隐藏
 * @param style 组件的样式（PagingStyle对象）
 * @constructor
 */
function Paging(id, containers, recordsPerPage, pagesPerSection, pn, total, goto, showSummary, showPageNumbers, hideIfNoRecord, style){
    this.id=id;
    this.containers=containers;
    for(let i=0; i<this.containers.length; i++){
        if((typeof this.containers[i])=='string') this.containers[i]=_$(this.containers[i]);
    }
    this.recordsPerPage=recordsPerPage;
    this.pagesPerSection=pagesPerSection;
    this.pn=pn;
    this.total=total;
    this.totalPages=1;
    this.goto=goto;
    this.showSummary=(typeof showSummary)=='boolean'?showSummary:true;
    this.showPageNumbers=(typeof showPageNumbers)=='boolean'?showPageNumbers:true;
    this.hideIfNoRecord=(typeof hideIfNoRecord)=='boolean'?hideIfNoRecord:false;
    this.style=style?style:new PagingStyle();

    //计算总页数
    if(this.total>this.recordsPerPage){
        if(this.total%this.recordsPerPage==0) this.totalPages=this.total/this.recordsPerPage;
        else this.totalPages=Math.floor(this.total/this.recordsPerPage) + 1;
    }else{
        this.totalPages=1;
    }

    Pagings.instances[this.id]=this;
}

/**
 * 分页组件样式
 * @param style 整体组件样式
 * @param styleSummary 统计信息样式
 * @param stylePage 页码样式
 * @param styleCurrent 当前页样式
 * @param stylePages 当前页/总页数样式
 * @param stylePrePage 前一页样式
 * @param styleFirstPage 第一页样式
 * @param styleNextPage 下一页样式
 * @param styleLastPage 最后一页样式
 * @constructor
 */
function PagingStyle(style, styleSummary, stylePage, styleCurrent, stylePages, stylePrePage, styleFirstPage, styleNextPage, styleLastPage){
    this.style=Str.isBlank(style)?'Paging':style;
    this.styleSummary=Str.isBlank(styleSummary)?'summary':styleSummary;
    this.stylePage=Str.isBlank(stylePage)?'page':stylePage;
    this.styleCurrent=Str.isBlank(styleCurrent)?'current':styleCurrent;
    this.stylePages=Str.isBlank(stylePages)?'pages':stylePages;
    this.stylePrePage=Str.isBlank(stylePrePage)?'previous':stylePrePage;
    this.styleFirstPage=Str.isBlank(styleFirstPage)?'first':styleFirstPage;
    this.styleNextPage=Str.isBlank(styleNextPage)?'next':styleNextPage;
    this.styleLastPage=Str.isBlank(styleLastPage)?'last':styleLastPage;
}

/**
 * 创建分页组件
 */
Paging.prototype.build=function(){
    if(this.total==0 && this.hideIfNoRecord){
        for(let i=0; i<this.containers.length; i++) this.containers[i].innerHTML='';
        return;
    }

    //确定显示哪些页码（尽可能地使得当前页码处于中间位置）
    let sectionHalf=Math.floor(this.pagesPerSection/2);

    let start=this.pn-(this.pagesPerSection-1);//显示的起始页码（最多比当前页码小pagesPerSection-1）
    if(start<1) start=1;

    //往后移动直至当前页左右两边页码一样多，或最后一个页码已经是最后一页（页码数为偶数时当前页位于左侧一半）
    if(this.pagesPerSection%2==0){
        while(start + (sectionHalf - 1) < this.pn
            && start + (this.pagesPerSection - 1) < this.totalPages){
            start++;
        }
    }else{
        while(start + sectionHalf < this.pn
            && start + (this.pagesPerSection - 1) < this.totalPages){
            start++;
        }
    }

    let end=start + this.pagesPerSection - 1;//显示的最后一个页码
    if(end > this.totalPages) end=this.totalPages;
    //确定显示哪些页码（尽可能地使得当前页码处于中间位置） end

    let s=[];

    s.push('<div id="'+this.id+'" class="'+this.style.style+'">');

    //显示统计
    if(this.showSummary){
        s.push('<div class="'+this.style.styleSummary+'">I{总记录数} '+this.total+'</div>');
    }

    //第一页 & 前一页
    if(this.pn > 1){
        s.push('<div onclick="Pagings.gotoPage(\''+this.id+'\', 1);" class="'+this.style.styleFirstPage+'"></div>');
        s.push('<div onclick="Pagings.prePage(\''+this.id+'\', '+this.pn+');" class="'+this.style.stylePrePage+'"></div>');
    }

    //页码
    if(this.showPageNumbers){
        for(let i=start;i<=end;i++){
            if(i==this.pn){
                s.push('<div class="'+this.style.styleCurrent+'">'+i+'</div>');
            }else{
                s.push('<div onclick="Pagings.gotoPage(\''+this.id+'\', '+i+');" class="'+this.style.stylePage+'">'+i+'</div>');
            }
        }
    }else{
        s.push('<div class="'+this.style.stylePages+'">'+this.pn+'/'+this.totalPages+'</div>');
    }

    //下一页 & 最后一页
    if(this.pn < this.totalPages){
        s.push('<div onclick="Pagings.nextPage(\''+this.id+'\', '+this.pn+');" class="'+this.style.styleNextPage+'"></div>');
        s.push('<div onclick="Pagings.gotoPage(\''+this.id+'\', '+this.totalPages+');" class="'+this.style.styleLastPage+'"></div>');
    }

    s.push('</div>');

    s=Lang.convert(s.join(''));
    for(let i=0; i<this.containers.length; i++) this.containers[i].innerHTML=s;
    s=null;
    delete s;
}

/**
 * 管理分页组件实例
 * @type {{gotoPage: Pagings.gotoPage, instances: *[]}}
 */
let Pagings={
    instances:[],

    /**
     *
     * @param id
     * @param pn
     */
    gotoPage:function(id, pn){
        let paging=this.instances[id];
        if(!paging) return;

        paging.pn=pn;
        if(paging.goto){
            paging.goto(pn);
        }else if((typeof frm) != 'undefined' && (typeof frm.pn) != 'undefined'){
            frm.pn.value=pn;
            frm.submit();
        }
    },

    /**
     * 下一页
     * @param id
     * @param pn
     */
    nextPage:function(id, pn){
        let paging=this.instances[id];
        if(!paging) return;

        pn++;
        if(pn>paging.totalPages) pn=paging.totalPages;
        this.gotoPage(id, pn);
    },

    /**
     * 前一页
     * @param id
     * @param pn
     */
    prePage:function(id, pn){
        let paging=this.instances[id];
        if(!paging) return;

        pn--;
        if(pn<1) pn=1;
        this.gotoPage(id, pn);
    }
}