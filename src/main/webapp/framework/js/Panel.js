/**
 * 通用面板（页面）
 * @type {{}}
 */
let Jpanels={
    /**
     * 面板分区
     * @param label 分区标签（默认无标签）
     * @param items 条目  [Jitem]
     * @param padding
     * @param radius 圆角半径
     * @constructor
     */
    Jblock:function(args){
        //label, items, padding, radius
        this.label=args.label;
        this.items=(Array.isArray(args.items))?args.items:[];
        this.padding=(typeof args.padding)=='number'?args.padding:10;
        this.radius=(typeof args.radius)=='number'?args.radius:10;
        this.blockIndex=0;
        this.itemIndex=0;
        this.forRoles=(Array.isArray(args.roles))?args.roles:null;
        this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
    },

    /**
     * 条目（限定高度）
     * @param name
     * @param info
     * @param icon
     * @param iconType 图标类型 img | iconfont
     * @param withArrow
     * @param url
     * @param urlTarget
     * @param content
     * @param onclick
     * @param extra
     * @constructor
     */
    Jitem:function(args){
        //name, info, icon, iconType, withArrow, url, urlTarget, content, onclick, extra
        this.name=args.name;
        this.info=args.info;
        this.icon=args.icon;
        this.iconType=Str.isBlank(args.iconType)?'iconfont':args.iconType;
        this.withArrow=(typeof args.withArrow)=='boolean'?args.withArrow:true;
        this.url=args.url;
        this.urlTarget=args.urlTarget;
        this.content=args.content;
        this.onclick=args.onclick;
        this.extra=args.extra;
        this.blockIndex=0;
        this.itemIndex=0;
        this.forRoles=(Array.isArray(args.roles))?args.roles:null;
        this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
    },

    /**
     * 一行（不限高度）
     * @param cells [Jcell]
     * @param cellsPerRow 每行多少个cell
     * @param cellspacing cell间距
     * @constructor
     */
    Jrow:function(args){
        //cells, cellsPerRow, cellspacing
        this.cells=(Array.isArray(args.cells))?args.cells:[];
        this.cellsPerRow=(typeof args.cellsPerRow)=='number'?args.cellsPerRow:2;
        this.cellspacing=(typeof args.cellspacing)=='number'?args.cellspacing:5;
        if(this.cellsPerRow<=0) this.cellsPerRow=1;
        this.blockIndex=0;
        this.itemIndex=0;
        this.forRoles=(Array.isArray(args.roles))?args.roles:null;
        this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
    },

    /**
     * 一行中的一个内容单元
     * @param name
     * @param url
     * @param urlTarget
     * @param content
     * @param onclick
     * @param extra
     * @constructor
     */
    Jcell:function(args){
        //name, url, urlTarget, content, onclick, extra
        this.name=args.name;
        this.url=args.url;
        this.urlTarget=args.urlTarget;
        this.content=args.content;
        this.onclick=args.onclick;
        this.extra=args.extra;
        this.blockIndex=0;
        this.itemIndex=0;
        this.forRoles=(Array.isArray(args.roles))?args.roles:null;
        this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
    },

    /**
     *
     * @param args
     * @constructor
     */
    Jbtn:function(args){
        //name, url, urlTarget, content, onclick, extra, bg, color
        this.name=args.name;
        this.url=args.url;
        this.urlTarget=args.urlTarget;
        this.content=args.content;
        this.onclick=args.onclick;
        this.extra=args.extra;
        this.blockIndex=0;
        this.itemIndex=0;
        this.forRoles=(Array.isArray(args.roles))?args.roles:null;
        this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
        this.bg=args.bg;
        this.color=args.color;
    },

    /**
     * 往blocks的最后一个或指定位置（index）的block追加一个item
     * @param blocks
     * @param index
     * @param item
     */
    appendItem:function(blocks, index, item){
        if(!blocks || blocks.length==0) return;
        if((typeof index)!='number' || index<0 || index>blocks.length-1) index=blocks.length-1;
        if((blocks[index] instanceof Jpanels.Jbtn)) return;
        blocks[index].items.push(item);
    },

    /**
     *
     * @param panelId
     * @param blockIndex
     * @param itemIndex
     */
    click:function(panelId, blockIndex, itemIndex){
        let instance=this.instances[panelId];
        if(!instance) return;
        instance.click(blockIndex, itemIndex);
    },

    /**
     * 关闭
     * @param panelId
     */
    close:function(panelId){
        if(panelId){
            let instance=this.instances[panelId];
            if(!instance) return;
            instance.close();
        }else{
            for(let i in this.instances) this.instances[i].close();
        }
    },

    //所有面板实例
    instances:[]
}

/**
 *
 * @param panel
 * @param blockIndex
 * @returns {string}
 */
Jpanels.Jbtn.prototype.build=function(panel, blockIndex){
    let s=[];
    let myStyle='';
    if(blockIndex==0) myStyle='margin-top:0px;';
    else myStyle='margin-top:10px;';
    if(!Str.isBlank(this.bg)) myStyle+='background-color:'+this.bg+';';
    if(!Str.isBlank(this.color)) myStyle+='color:'+this.color+';';
    if(!this.showAnyWay && this.forRoles && !Auth.isRole(this.forRoles)) myStyle+='display:none !important;';

    s.push('<div id="'+this.id+'_'+blockIndex+'_0_name" class="Jbtn" style="'+myStyle+'" onclick="Jpanels.click(\''+panel.id+'\','+blockIndex+');">');
    s.push(this.name);
    s.push('</div>');
    return Lang.convert(s.join(''));
}

/**
 *
 * @param panel
 * @param blockIndex
 * @param width 一行的可用宽度（由上级节点决定）
 * @returns {string}
 */
Jpanels.Jrow.prototype.build=function(panel, blockIndex, width){
    let myStyle='';
    if(!this.showAnyWay && this.forRoles && !Auth.isRole(this.forRoles)) myStyle+='display:none !important;';

    let visibleCells=0;//可见单元数
    for(let j=0; j<this.cells.length; j++){
        if(!this.cells[j].showAnyWay && this.cells[j].forRoles && !Auth.isRole(this.cells[j].forRoles)){
            continue;
        }
        visibleCells++;
    }

    let cellWidth=Math.floor((width-this.cellspacing*(this.cellsPerRow+1))/this.cellsPerRow);
    let s=[];
    s.push('<div class="Jrow" style="'+myStyle+'">');

    let index=0;
    for(let j=0; j<this.cells.length; j++){
        this.cells[j].blockIndex=blockIndex;
        this.cells[j].rowIndex=j;
        let isBottom=(index>=visibleCells-this.cellsPerRow);
        let cellStyle='width:'+cellWidth+'px; margin-left:'+this.cellspacing+'px; margin-top:'+this.cellspacing+'px';
        if(isBottom) cellStyle+=' margin-bottom:'+this.cellspacing+'px';

        if(!this.cells[j].showAnyWay && this.cells[j].forRoles && !Auth.isRole(this.cells[j].forRoles)){
            cellStyle+='display:none !important;';
        }else{
            index++;
        }

        s.push('<div id="'+panel.id+'_'+blockIndex+'_'+j+'_name" class="Jcell" style="'+cellStyle+' onclick="Jpanels.click(\''+panel.id+'\','+blockIndex+','+j+');">');
        s.push(this.cells[j].name);
        s.push('</div>');
    }
    s.push('</div>');
    return Lang.convert(s.join(''));
}

/**
 *
 * @param id
 * @param name
 * @param container
 * @param blocks [Jblock | Jbtn | Jrow]
 * @param padding
 * @constructor
 */
function Jpanel(id, name, container, blocks, padding){
    if(container && (typeof container)=='string') container=_$(container);
    this.id=id;
    this.name=Lang.convert(name);
    this.container=container;
    this.blocks=blocks?blocks:[];
    this.padding=(typeof padding)=='number'?padding:10;

    if(Jpanels.instances[this.id]) Jpanels.instances[this.id].close();
    Jpanels.instances[this.id]=this;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 */
Jpanel.prototype.getItem=function(blockIndex, itemIndex){
    if(blockIndex>this.blocks.length-1) return null;
    let b=this.blocks[blockIndex];
    if(itemIndex>b.items.length-1) return null;
    return b.items[itemIndex];
}

Jpanel.prototype.isOpen=function(){
    if(_$(this.id)) return true;
    return false;
}

/**
 *
 */
Jpanel.prototype.build=function(width){
    if((typeof width) != 'number') width=W.vw();
    width-=this.padding*2;

    let s=[];

    let panelStyle='';
    if(this.padding>0) panelStyle=' style="padding:'+this.padding+'px;"';

    s.push('<div class="Jpanel" id="'+this.id+'"'+panelStyle+'>');
    for(let i=0; i<this.blocks.length; i++){
        let b=this.blocks[i];
        b.blockIndex=i;

        if(b instanceof Jpanels.Jbtn){
            s.push(b.build(this, i));
            continue;
        }

        if(b instanceof Jpanels.Jrow){
            s.push(b.build(this, i, width));
            continue;
        }

        let blockStyle='';
        if(b.label || i==0) blockStyle='margin-top:0px;';
        else blockStyle='margin-top:10px;';

        if(b.label){
            let labelStyle='';
            if(i==0) labelStyle='margin-top:0px;';
            else labelStyle='margin-top:10px;';
            s.push('<div class="Jlabel" style="'+labelStyle+'">'+b.label+'</div>');
        }

        if(b.padding>0) blockStyle+=' padding:'+b.padding+'px;';
        if(b.radius>0) blockStyle+=' border-radius:'+b.radius+'px;';
        if(!b.showAnyWay && b.forRoles && !Auth.isRole(b.forRoles)) blockStyle+='display:none !important;';

        s.push('<div class="Jblock" style="'+blockStyle+'">');
        for(let j=0; j<b.items.length; j++){
            let item=b.items[j];
            item.blockIndex=i;
            item.itemIndex=j;

            let itemStyle='';
            if(j==b.items.length-1) itemStyle=' style=""';
            if(!item.showAnyWay && item.forRoles && !Auth.isRole(item.forRoles)) itemStyle+='display:none !important;';

            s.push('<div id="'+this.id+'_'+i+'_'+j+'" class="Jitem"'+itemStyle+' onclick="Jpanels.click(\''+this.id+'\','+i+','+j+');">');
            s.push('<div id="'+this.id+'_'+i+'_'+j+'_name" class="JitemName">'+item.name+'</div>');
            if(item.withArrow) s.push('<div class="JitemArrow"></div>');
            if(!Str.isBlank(item.icon)){
                if(item.iconType=='img') s.push('<div id="'+this.id+'_'+i+'_'+j+'_icon"  class="JitemIcon"><img src="'+item.icon+'"/></div>');
                else s.push('<div id="'+this.id+'_'+i+'_'+j+'_icon"  class="JitemIcon"><div class="iconfont '+item.icon+'"></div></div>');
            }
            if(!Str.isBlank(item.info)) s.push('<div id="'+this.id+'_'+i+'_'+j+'_info"  class="JitemInfo">'+item.info+'</div>');
            s.push('</div>');
        }
        s.push('</div>');
    }
    s.push('</div>');
    s=Lang.convert(s.join(''));

    if(this.container){
        if(this.container instanceof Layer){
            this.container.load(window, this.name, null, s, null, 0, null);
        }else{
            if(this.container.id=='Jcontent' && _$('JcontentBottom')){
                _$('JcontentBottom').insertAdjacentHTML('beforebegin', s);
            }else{
                this.container.innerHTML=s;
            }
        }
    }else{
        this.container = Layers.open(window, this.name, null, s, null, 0, null);
    }

    s=null;
    delete s;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemName=function(blockIndex, itemIndex, content){
    let item=this.getItem(blockIndex,itemIndex);
    if(!item) return;
    item.name=content;
    let itemName=_$(this.id+'_'+blockIndex+'_'+itemIndex+'_name');
    if(!itemName) return;
    itemName.innerHTML=content;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemInfo=function(blockIndex, itemIndex, content){
    let item=this.getItem(blockIndex,itemIndex);
    if(!item) return;
    item.info=content;
    let itemInfo=_$(this.id+'_'+blockIndex+'_'+itemIndex+'_info');
    if(!itemInfo) return;
    itemInfo.innerHTML=content;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemIcon=function(blockIndex, itemIndex, content){
    let item=this.getItem(blockIndex,itemIndex);
    if(!item) return;
    item.icon=content;
    let itemIcon=_$(this.id+'_'+blockIndex+'_'+itemIndex+'_icon');
    if(!itemIcon) return;
    if(itemIcon.innerHTML.indexOf('<img')>-1) itemIcon.innerHTML='<img src="'+content+'"/>';
    else itemIcon.innerHTML='<div class="iconfont '+content+'"></div>';
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemUrl=function(blockIndex, itemIndex, content){
    let item=this.getItem(blockIndex,itemIndex);
    if(!item) return;
    item.url=content;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param onclick
 */
Jpanel.prototype.setOnClick=function(blockIndex, itemIndex, onclick){
    let item=this.getItem(blockIndex,itemIndex);
    if(!item) return;
    item.onclick=onclick;
}

/**
 * 点击条目
 * @param blockIndex
 * @param itemIndex
 */
Jpanel.prototype.click=function(blockIndex, itemIndex){
    if(blockIndex<0 || itemIndex<0 || blockIndex>this.blocks.length-1) return;
    let b=this.blocks[blockIndex];
    let obj=null;
    if(b instanceof Jpanels.Jbtn) obj=b;
    else if(b instanceof Jpanels.Jrow) obj=b.cells[itemIndex];
    else obj=b.items[itemIndex];

    if(obj.onclick){
        obj.onclick.call(window, obj);
    }else if(obj.url){
        if(obj.urlTarget) obj.urlTarget.location.href=obj.url;
        else if(this.container instanceof Layer) this.container.load(window, obj.name, obj.url, null, null, 0, null);
        else Layers.open(window, obj.name, obj.url, null, null, 0, null);
    }else if(obj.content){
        if(this.container instanceof Layer) this.container.load(window, obj.name, null, obj.content, null, 0, null);
        else Layers.open(window, obj.name, null, obj.content, null, 0, null);
    }
}

/**
 * 关闭
 */
Jpanel.prototype.close=function(){
    if(this.container instanceof Layer) this.container.close();
}