//CascadingMenu
let CascadingMenus={
    instances:[],

    /**
     *
     * @param eventSrcElement
     * @param menuId
     * @param nodeId
     * @param relatedUrl
     * @param status 展开状态，0：收起，1：展开，其它值：自动判断
     */
    folding:function(eventSrcElement, menuId, nodeId, relatedUrl, status, doNotCallback){
        let cMenu=this.instances[menuId];
        if(!cMenu) return;

        let node=cMenu.findNode(nodeId, relatedUrl);
        if(!node) return;

        if((typeof status)!='number' || (status!=0 && status!=1)) status=(node.status==0?1:0);

        if(status==0) node.fold(cMenu, doNotCallback);
        else node.unfold(cMenu, doNotCallback);
    },

    /**
     *
     * @param eventSrcElement
     * @param menuId
     * @param nodeId
     * @param relatedUrl
     * @param doNotCallback
     */
    click:function(eventSrcElement, menuId, nodeId, relatedUrl, doNotCallback){
        let cMenu=this.instances[menuId];
        if(!cMenu) return;

        let node=cMenu.findNode(nodeId, relatedUrl);
        if(!node) return;

        node.click(cMenu, doNotCallback);
    }
}

/**
 * 树节点
 * @param level
 * @param pid
 * @param id
 * @param canBeSelected 是否可被选中
 * @param relatedUrl 可用于标示该节点的关联url
 * @param content
 * @param btns
 * @param style
 * @param styleSelected
 * @param onUnfold
 * @param onFold
 * @param onClick
 * @constructor
 */
function CascadingMenuNode(level, pid, id, canBeSelected, relatedUrl, content, btns, style, styleSelected, onUnfold, onFold, onClick){
    this.level=level;//节点层级，顶级为0
    this.pid=pid;
    this.id=id;
    this.canBeSelected=(typeof canBeSelected)=='boolean'?canBeSelected:true;
    this.relatedUrl=relatedUrl;
    this.content=content;
    this.btns=btns;
    this.style=style;
    this.styleSelected=styleSelected;
    this.onUnfold=onUnfold;
    this.onFold=onFold;
    this.onClick=onClick;
    this.status=0;//默认收起
}

/**
 * 新增子节点
 * @param id
 * @param canBeSelected 是否可被选中
 * @param relatedUrl 可用于标示该节点的关联url
 * @param content
 * @param btns
 * @param style
 * @param styleSelected
 * @param onUnfold
 * @param onFold
 * @param onClick
 */
CascadingMenuNode.prototype.addChild=function(id, canBeSelected, relatedUrl, content, btns, style, styleSelected, onUnfold, onFold, onClick){
    return new CascadingMenuNode(this.level+1,
        this.id,
        id,
        canBeSelected,
        relatedUrl,
        content,
        btns,
        style ? style : this.style,
        styleSelected ? styleSelected : this.styleSelected,
        onUnfold ? onUnfold : this.onUnfold,
        onFold ? onFold : this.onFold,
        onClick ? onClick : this.onClick);
}

/**
 * 该节点的css classname
 * @param cMenu
 * @returns {*}
 */
CascadingMenuNode.prototype.getStyle=function(cMenu){
    return Str.isBlank(this.style)?cMenu.nodeStyle:this.style;
}

/**
 * 该节点被选中时的css classname
 * @param cMenu
 * @returns {*}
 */
CascadingMenuNode.prototype.getStyleSelected=function(cMenu){
    return Str.isBlank(this.styleSelected)?cMenu.nodeStyleSelected:this.styleSelected;
}

/**
 *
 * @param cMenu
 * @returns {*}
 */
CascadingMenuNode.prototype.getOnUnfold=function(cMenu){
    return this.onUnfold?this.onUnfold:cMenu.onUnfold;
}

/**
 *
 * @param cMenu
 * @returns {*}
 */
CascadingMenuNode.prototype.getOnFold=function(cMenu){
    return this.onFold?this.onFold:cMenu.onFold;
}

/**
 *
 * @param cMenu
 * @returns {*}
 */
CascadingMenuNode.prototype.getOnClick=function(cMenu){
    return this.onClick?this.onClick:cMenu.onClick;
}

/**
 *
 * @param cMenu
 */
CascadingMenuNode.prototype.fold=function(cMenu, doNotCallback){
    this.status=0;
    if(cMenu.getNodeChildrenDiv(this.id)){
        cMenu.getNodeChildrenDiv(this.id).style.display='none';
        cMenu.getNodeIconDiv(this.id).className=cMenu.iconStyleFold;
    }

    if(doNotCallback) return;
    if(this.getOnFold(cMenu)) this.getOnFold(cMenu)(cMenu, this);
}

/**
 *
 * @param cMenu
 */
CascadingMenuNode.prototype.unfold=function(cMenu, doNotCallback){
    this.status=1;
    if(cMenu.getNodeChildrenDiv(this.id)){
        cMenu.getNodeChildrenDiv(this.id).style.display='';
        cMenu.getNodeIconDiv(this.id).className=cMenu.iconStyleUnfold;
    }

    if(doNotCallback) return;
    if(this.getOnUnfold(cMenu)) this.getOnUnfold(cMenu)(cMenu, this);
}

/**
 *
 * @param cMenu
 */
CascadingMenuNode.prototype.click=function(cMenu, doNotCallback){
    if(!this.canBeSelected) return;

    if(cMenu.nodeSelected){
        cMenu.getNodeDiv(cMenu.nodeSelected.id).className=cMenu.nodeSelected.getStyle(cMenu);
    }
    cMenu.nodeSelected=this;

    cMenu.getNodeDiv(this.id).className=this.getStyle(cMenu)+' '+this.getStyleSelected(cMenu);

    if(doNotCallback) return;
    if(this.getOnClick(cMenu)) this.getOnClick(cMenu)(cMenu, this);
}

/**
 *
 * @param cMenu
 */
CascadingMenuNode.prototype.getChildren=function(cMenu){
    let children=[];
    for(let i=0; i<cMenu.nodes.length; i++){
        if(this.id==cMenu.nodes[i].pid) children.push(cMenu.nodes[i]);
    }
    return children;
}

/**
 *
 * @param cMenu 所属菜单对象
 */
CascadingMenuNode.prototype.build=function(cMenu){
    this.status=cMenu.unfoldLevelOnInit>this.level?1:0;

    //左边缩进量
    let indention=cMenu.indention*this.level;

    //直属子节点
    let children=this.getChildren(cMenu);

    let s=[];
    s.push('<div id="'+(cMenu.id+'_'+this.id)+'" class="'+this.getStyle(cMenu)+'" style="padding-left:'+indention+'px !important;">');
    if(children.length>0){
        s.push('    <div id="'+(cMenu.id+'_'+this.id)+'_icon" class="'+(cMenu.unfoldLevelOnInit>this.level?cMenu.iconStyleUnfold:cMenu.iconStyleFold)+'" onclick="CascadingMenus.folding(this, \''+cMenu.id+'\', \''+this.id+'\');"></div>');
    }else{
        s.push('    <div id="'+(cMenu.id+'_'+this.id)+'_icon" class="'+(cMenu.iconStyleNone)+'"></div>');
    }
    s.push('    <div id="'+(cMenu.id+'_'+this.id)+'_content" class="'+cMenu.contentStyle+'" onclick="CascadingMenus.click(this, \''+cMenu.id+'\', \''+this.id+'\');">'+this.content+'</div>');
    s.push('    <div id="'+(cMenu.id+'_'+this.id)+'_btns" class="'+cMenu.btnsStyle+'">'+this.btns.join(' ')+'</div>');
    s.push('</div>');

    if(children.length>0) {
        //子节点
        s.push('<div id="' + (cMenu.id + '_' + this.id) + '_children" class="' + cMenu.childrenStyle + '" style="display:' + (cMenu.unfoldLevelOnInit > this.level ? '' : 'none') + ';">');
        for (let i = 0; i < children.length; i++) {
            s.push(children[i].build(cMenu));
        }
        s.push('</div>');
        //子节点 end
    }

    return s.join('');
}

/**
 *
 * @param id 菜单ID
 * @param container 容器ID
 * @param childrenStyle 包含全部子节点的div的样式
 * @param nodeStyle 节点样式
 * @param nodeStyleSelected 节点被选中样式
 * @param iconStyleFold 菜单收起时图标样式
 * @param iconStyleUnfold 菜单展开时图标样式
 * @param iconStyleNone 无下级节点时图标样式
 * @param contentStyle 菜单内容（通常是文本）样式
 * @param btnsStyle 菜单自定义按钮样式
 * @param indention 每一层菜单缩进量
 * @param onUnfold 展开菜单时回调
 * @param onFold 收起菜单时回调
 * @param onClick 点击菜单时回调
 * @param unfoldLevelOnInit 初始展开的菜单（可见的）最大级别
 * @constructor
 */
function CascadingMenu(id,
                       container,
                       childrenStyle,
                       nodeStyle,
                       nodeStyleSelected,
                       iconStyleFold,
                       iconStyleUnfold,
                       iconStyleNone,
                       contentStyle,
                       btnsStyle,
                       indention,
                       onUnfold,
                       onFold,
                       onClick,
                       unfoldLevelOnInit){
    this.id=id;
    this.container=((typeof container)=='string'?_$(container):container);

    this.childrenStyle=childrenStyle ? childrenStyle : 'cMenuNodes';
    this.nodeStyle=nodeStyle ? nodeStyle : 'cMenuNode';
    this.nodeStyleSelected=nodeStyleSelected ? nodeStyleSelected : 'cMenuNodeSelected';
    this.iconStyleFold=iconStyleFold ? iconStyleFold : 'cMenuIconFold';
    this.iconStyleUnfold=iconStyleUnfold ? iconStyleUnfold : 'cMenuIconUnfold';
    this.iconStyleNone=iconStyleNone ? iconStyleNone : 'iconStyleNone';
    this.contentStyle=contentStyle ? contentStyle : 'cMenuContent';
    this.btnsStyle=btnsStyle ? btnsStyle : 'cMenuBtns';
    this.indention=indention;

    this.onUnfold=onUnfold;
    this.onFold=onFold;
    this.onClick=onClick;

    this.unfoldLevelOnInit=(typeof unfoldLevelOnInit)=='number'?unfoldLevelOnInit:0;

    this.nodes=[];//所有菜单节点
    this.nodeSelected=null;//当前被选中节点

    this.built=false;//是否已经创建

    CascadingMenus.instances[this.id]=this;
}
CascadingMenu.prototype.addNode=function(node){
    this.nodes.push(node);

    if(this.built){
        //重建
        this.build();

        //自动选择新添加节点
        this.selectNode(node.id);
    }
}
CascadingMenu.prototype.findNode=function(id, relatedUrl){
    if(!id) return null;
    for(let i=0; i<this.nodes.length; i++){
        if(!Str.isBlank(id) && id==this.nodes[i].id) return this.nodes[i];
        if(!Str.isBlank(relatedUrl) && relatedUrl==this.nodes[i].relatedUrl) return this.nodes[i];
    }
    return null;
}
CascadingMenu.prototype.getNodeDiv=function(nodeId){
    return _$(this.id+'_'+nodeId);
}
CascadingMenu.prototype.getNodeIconDiv=function(nodeId){
    return _$(this.id+'_'+nodeId+'_icon');
}
CascadingMenu.prototype.getNodeContentDiv=function(nodeId){
    return _$(this.id+'_'+nodeId+'_content');
}
CascadingMenu.prototype.getNodeBtnsDiv=function(nodeId){
    return _$(this.id+'_'+nodeId+'_btns');
}
CascadingMenu.prototype.getNodeChildrenDiv=function(nodeId){
    return _$(this.id+'_'+nodeId+'_children');
}
CascadingMenu.prototype.build=function(){
    this.built=true;
    let s=[];
    for(let i=0; i<this.nodes.length; i++){
        if(this.nodes[i].level>0) continue;
        s.push(this.nodes[i].build(this));
    }
    this.container.innerHTML=s.join('\n\n');
    s=null;
    delete s;
}
/**
 * 选中某个节点（强制展开所有上级）
 * @param nodeId
 */
CascadingMenu.prototype.selectNode=function(nodeId){
    let node=this.findNode(nodeId);
    if(!node) return;

    //选中（不回调）
    node.click(this, true);

    let pNode=this.findNode(node.pid);
    while(pNode){
        pNode.unfold(this, true);
        pNode=this.findNode(pNode.pid);
    }
}