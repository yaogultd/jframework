//多级类目选择组件
/**
 * 类目
 * @param level 0,1,2....
 * @param id
 * @param code
 * @param nameCn
 * @param nameEn
 * @param callback 可为单个节点指定对应的回调（点击时）
 * @param callbackTarget 调用回调方法的对象（默认window）
 * @constructor
 */
function Catalog(level, id, code, nameCn, nameEn, callback, callbackTarget){
    this.level=level;
    this.id=id;
    this.pid=null;
    this.code=code;
    this.nameCn=nameCn;
    this.nameEn=nameEn;
    this.callback=callback;
    this.callbackTarget=callbackTarget;
    this.children=[];
}

/**
 * 获取父级类目
 */
Catalog.prototype.getParent=function(){
    return Str.isBlank(this.pid)?null:Catalogs.catalogs[this.pid];
}

/**
 * 添加下级类目
 * @param id
 * @param code
 * @param nameCn
 * @param nameEn
 * @param callback
 * @param callbackTarget
 */
Catalog.prototype.addChild=function(id, code, nameCn, nameEn, callback, callbackTarget){
    let c=new Catalog(this.level+1, id, code, nameCn, nameEn, callback, callbackTarget);
    c.pid=this.id;
    this.children.push(c);
    Catalogs.catalogs[c.id]=c;
    return c;
}

/**
 * 获取下级区域
 * @param idOrCodeOrName
 * @returns {null|*}
 */
Catalog.prototype.getChild=function(idOrCodeOrName){
    for(let i=0; i<this.children.length; i++){
        let c=this.children[i];
        if(c.id==idOrCodeOrName
            || c.code==idOrCodeOrName
            || c.nameCn==idOrCodeOrName
            || c.nameEn==idOrCodeOrName) return c;
    }
    return null;
}

/**
 * 获取名称与指定名称相似的下级区域
 * @param name
 * @returns {null|*}
 */
Catalog.prototype.getChildrenAlike=function(name){
    let alikes=[];
    for(let i=0; i<this.children.length; i++){
        let c=this.children[i];
        if(c.nameCn.indexOf(name)>-1
            ||c.nameEn.indexOf(name)>-1
            ||name.indexOf(c.nameCn)>-1
            ||name.indexOf(c.nameEn)>-1){
            alikes.push(c);
        }
    }
    return alikes;
}

/**
 * 根据当前使用语言返回对应的名称
 * @returns {*}
 */
Catalog.prototype.getName=function(lang){
    if(!lang) lang=Lang.getCurrentLang().id;
    if(lang=='cn') return this.nameCn;
    else return this.nameEn;
}

/**
 * 获取包括上级名称的完整名称
 * @param lang
 * @param splitter
 * @param topLevel 最高显示到哪一级，默认为0（顶级）
 * @returns {string}
 */
Catalog.prototype.getCanonicalName=function(lang, splitter, topLevel){
    if(!lang) lang=Lang.getCurrentLang().id;
    if(!splitter) splitter='';
    if((typeof topLevel)!='number' || topLevel<0) topLevel=0;

    let n=[];
    n.push(this.getName(lang));

    let p=this.getParent();
    while(p && p.level>=topLevel){
        n.push(p.getName(lang));
        p=p.getParent();
    }

    n.reverse();
    return n.join(splitter);
}

/**
 * 类目链
 */
Catalog.prototype.getChain=function(){
    let chain=[];
    chain.push(this);
    let p=this.getParent();
    while(p){
        chain.push(p);
        p=p.getParent();
    }
    chain.reverse();
    return chain;
}

/**
 *
 *
 */
Catalog.prototype.toString=function(){
    let s=[];
    s.push('{"level":'+this.level);
    s.push(',"id":"'+this.id+'"');
    if(this.code) s.push(',"code":"'+this.code+'"');
    s.push(',"nameCn":"'+JSONUtil.convert(this.nameCn)+'"');
    s.push(',"nameEn":"'+JSONUtil.convert(this.nameEn)+'"');
    s.push('}');
    return s.join('');
}

let Catalogs={
    //顶级类目
    tops:[],

    //所有类目（Catalog.id=Catalog）
    catalogs:[],

    //重置
    reset:function (){
        this.tops=[];
        this.catalogs=[];
        this.pickers=[];
    },

    //根据ID查找类目
    getCatalog:function (catalogId){
        return Str.isBlank(catalogId)?null:this.catalogs[catalogId];
    },

    //添加顶级类目
    C0:function (id, code, nameCn, nameEn, callback, callbackTarget) {
        let r = new Catalog(0, id, code, nameCn, nameEn, callback, callbackTarget);
        this.tops.push(r);
        this.catalogs[r.id] = r;
        return r;
    },

    //类目选择组件实例
    pickers:[]
}

/**
 * 类目选择组件
 * @param id
 * @param id
 * @param minLevel
 * @param maxLevel
 * @param multiChoices 一次选择多个类目
 * @param callback 回调（点击时）
 * @param callbackTarget 调用回调方法的对象（默认window）
 * @constructor
 */
function CatalogPicker(id, title, minLevel, maxLevel, multiChoices, callback, callbackTarget){
    document.body.style.setProperty('--CatalogPickerNodeWidth', Math.floor(W.vw()/3 - 10) + 'px');
    this.id=id;
    this.title=Str.isBlank(title)?'I{js,选择类目}':title;
    this.minLevel=minLevel;
    this.maxlevel=maxLevel;
    this.multiChoices=(typeof multiChoices)=='boolean'?multiChoices:false;
    this.callback=callback;
    this.callbackTarget=callbackTarget;
    this.inLayer=null;
    this.selectedChain=[];//当前选中类目链

    Catalogs.pickers[this.id]=this;
    this.build();
}

/**
 *
 * @param title
 */
CatalogPicker.prototype.setTitle=function(title){
    this.title=Str.isBlank(title)?'I{js,选择类目}':title;
    if(this.inLayer) {
        if(this.selectedChain.length > 0) this.inLayer.setTitle(this.selectedChain[this.selectedChain.length-1].getCanonicalName(null, ', '));
        else this.inLayer.setTitle(this.title);
    }
}

/**
 * 创建组件
 */
CatalogPicker.prototype.build=function(){
    let str=[];
    str.push('<div class="CatalogPicker" id="'+this.id+'">');

    str.push('	<div class="CatalogPickerHead" id="'+this.id+'_head"><input type="text" picker="'+this.id+'" id="'+this.id+'_keywords" placeholder="I{js,搜索}"/></div>');

    str.push('	<div class="CatalogPickerContent" id="'+this.id+'_content">');
    str.push('		<div class="CatalogPickerNodes" id="'+this.id+'_nodes"></div>');
    str.push('	</div>');
    str.push('</div>');

    let btns=[];
    btns.push('	<div class="CatalogPickerContentBtns">');
    btns.push('		<div class="btn mT10"><input type="button" value="'+(this.multiChoices?'I{js,添加}':'I{js,确定}')+'" onclick="Catalogs.pickers[\''+this.id+'\'].done();"/></div>');
    btns.push('		<div class="btnLight mT10 mL10" id="'+this.id+'_to_parent" style="display:none;"><input type="button" value="I{js,返回}" onclick="Catalogs.pickers[\''+this.id+'\'].toParent();"/></div>');
    btns.push('		<div class="btnLight mT10 mL10"><input type="button" value="I{js,关闭}" onclick="Catalogs.pickers[\''+this.id+'\'].close();"/></div>');
    btns.push('	</div>');

    this.inLayer=Layers.open(window, this.title, null,str.join(''), btns.join(''), 0, null);
    _$(this.id+'_content').style.height=(this.inLayer.getHeight() - W.elementHeight(_$(this.id+'_head')))+'px';

    str=null;
    delete str;

    btns=null;
    delete btns;

    //绑定搜索事件
    new InputEvent(_$(this.id+'_keywords'), this.search);

    this.init();
}

/**
 *
 * @param pid
 */
CatalogPicker.prototype.init=function(pid){
    _$(this.id+'_nodes').innerHTML='';

    let nodes=null;
    if(pid){
        let pNode=Catalogs.getCatalog(pid);
        if(!pNode) return;
        nodes=pNode.children;
    }else{
        nodes=Catalogs.tops;
    }

    //初始化顶级类目
    for(let i=0; i<nodes.length; i++){
        let c=nodes[i];
        let node=document.createElement('div');
        node.id=this.id+'_'+c.level+'_'+c.id;
        node.className='CatalogPickerNode';
        node.innerHTML=c.getName();
        _$(this.id+'_nodes').appendChild(node);

        Utils.setAtt(node, 'picker', this.id);
        Utils.setAtt(node, 'level', c.level);
        Utils.setAtt(node, 'nodeId', c.id);

        node.addEventListener('click', function(event){
            let node=Utils.getEventTarget(event);
            let pickerId=Utils.att(node, 'picker');
            if(!pickerId) return;

            let picker=Catalogs.pickers[pickerId];
            if(!picker) return;

            picker.select(node);
        });
    }

    this.setTitle();
}

/**
 * 搜索
 * @param event
 * @param keywords
 */
CatalogPicker.prototype.search=function(event, keywords){
    if(event){
        let input=Utils.getEventTarget(event);
        keywords=Str.trimAll(input.value);

        let pickerId=Utils.att(input, 'picker');
        let picker=Catalogs.pickers[pickerId];
        picker.search(null, keywords);
        return;
    }
    keywords=Str.trimAll(keywords);

    //关键词为空，显示所有
    if(Str.isBlank(keywords)){
        let nodes=_$cls('CatalogPickerNodeSelected');
        for(let i=0; nodes && i<nodes.length; i++){
            nodes[i].style.display='';
        }

        nodes=_$cls('CatalogPickerNode');
        for(let i=0; nodes && i<nodes.length; i++){
            nodes[i].style.display='';
        }

        return;
    }

    //对当前显示级别的类目进行查找
    let container=_$(this.id+'_nodes');
    let cNodes=container.childNodes;
    for(let i=0; i<cNodes.length; i++){
        if(cNodes[i].innerHTML.indexOf(keywords) < 0) cNodes[i].style.display='none';
        else cNodes[i].style.display='';
    }
}

/**
 * 选中某个类目
 * @param node
 */
CatalogPicker.prototype.select=function(node){
    if(!node) return;

    let level=Utils.att(node, 'level')*1;
    if(level > this.maxlevel) return;//超过允许选择的最大层级

    let id=Utils.att(node, 'nodeId');
    let catalog=Catalogs.getCatalog(id);
    if(!catalog) return;

    this.selectedChain=catalog.getChain();

    //显示已选区域链
    this.setTitle();

    //设置节点选中/未选中样式
    let nodes=_$cls('CatalogPickerNodeSelected');
    for(let i=0; nodes && i<nodes.length; i++) nodes[i].className='CatalogPickerNode';

    //设置本节点的样式为“选中”
    node.className='CatalogPickerNodeSelected';

    let children=catalog.children;

    //显示/隐藏返回上级按钮
    if(level==0 && (!children || children.length==0)) _$(this.id+'_to_parent').style.display='noe';
    else _$(this.id+'_to_parent').style.display='';

    //显示下级
    if(this.maxlevel>level && children && children.length>0){
        _$(this.id+'_nodes').innerHTML='';

        //清空搜索关键词
        _$(this.id+'_keywords').value='';
        this.search(null, '');

        let childLevel=level+1;
        for(let i=0; i<children.length; i++){
            let r=children[i];
            let node=document.createElement('div');
            node.id=this.id+'_'+childLevel+'_'+r.id;
            node.className='CatalogPickerNode';
            node.innerHTML=r.getName();
            _$(this.id+'_nodes').appendChild(node);

            Utils.setAtt(node, 'picker', this.id);
            Utils.setAtt(node, 'level', ''+childLevel);
            Utils.setAtt(node, 'nodeId', r.id);

            node.addEventListener('click', function(event){
                let node=Utils.getEventTarget(event);
                let pickerId=Utils.att(node, 'picker');
                if(!pickerId) return;

                let picker=Catalogs.pickers[pickerId];
                if(!picker) return;

                picker.select(node);
            });
        }
        //展现直接下级 end
    }
}

/**
 * 返回上一级
 */
CatalogPicker.prototype.toParent=function(){
    //无需返回上级
    if(this.selectedChain.length==0){
        _$(this.id+'_to_parent').style.display='none';
        this.init();
        return;
    }

    //清空搜索关键词
    _$(this.id+'_keywords').value='';
    this.search(null, '');

    //移除最后一个选择的节点
    this.selectedChain.pop();

    //回到顶级
    if(this.selectedChain.length==0){
        _$(this.id+'_to_parent').style.display='none';
        this.init();
        return;
    }

    //上一级
    let catalog=this.selectedChain[this.selectedChain.length-1];
    this.init(catalog.pid);
    this.select(_$(this.id+'_'+catalog.level+'_'+catalog.id));
}

/**
 * 完成选择
 */
CatalogPicker.prototype.done=function(){
    let catalog=this.selectedChain.length==0?null:this.selectedChain[this.selectedChain.length-1];
    if(catalog && catalog.callback){
        catalog.callback.call(catalog.callbackTarget?catalog.callbackTarget:window, this.selectedChain);
    }else if(this.callback){
        this.callback.call(this.callbackTarget?this.callbackTarget:window, this.selectedChain);
    }
    if(!this.multiChoices) this.close();
}

/**
 * 关闭
 */
CatalogPicker.prototype.close=function(){
    this.inLayer.close();
}