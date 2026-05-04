//自定义下拉列表
let JSelectors={
    instances:[],

    /**
     * 保持显示
     * @param selectorId
     */
    keep:function (selectorId){
        let instance=JSelectors.instances[selectorId];
        if(!instance) return;

        if(instance.timer){
            clearTimeout(instance.timer);
            instance.timer=null;
        }
    },

    /**
     * 延时隐藏
     * @param selectorId
     */
    hideDelay:function(selectorId){
        let instance=JSelectors.instances[selectorId];
        if(!instance) return;

        if(instance.timer){
            clearTimeout(instance.timer);
            instance.timer=null;
        }

        instance.timer=setTimeout("JSelectors.hide('"+selectorId+"')",5000);
    },

    /**
     * 隐藏
     * @param selectorId
     */
    hide:function(selectorId){
        let instance=JSelectors.instances[selectorId];
        if(!instance) return;
        instance.hide();
    },

    /**
     * 通用选择项
     * @param item
     * @param txt
     * @param txtSelected
     * @param callback
     */
    selectCommonItem:function(item, txt, txtSelected, callback){
        if(!item) return;

        if(item.className=='JSelectorCommonItemSelected'){
            item.className='JSelectorCommonItem';
            item.innerHTML=txt;
        }else{
            item.className='JSelectorCommonItemSelected';
            item.innerHTML=txtSelected;
        }

        if(callback) callback(item);
    },

    /**
     * 搜索
     * @param event
     */
    search:function(event){
        let input=Utils.getEventTarget(event);
        if(!input) return;

        let selectorId=Utils.att(input, 'selectorId');
        let instance=JSelectors.instances[selectorId];
        if(!instance) return;

        if(instance.searchTimer){
            clearTimeout(instance.searchTimer);
            instance.searchTimer=null;
        }

        instance.searchTimer=setTimeout("JSelectors.searchDelay('"+selectorId+"')", 500);
    },

    /**
     * 输入后延时搜索
     * @param selectorId
     */
    searchDelay:function(selectorId){
        let instance=JSelectors.instances[selectorId];
        if(!instance) return;

        if(instance.onSearch) instance.onSearch.call(instance.onSearchTarget?instance.onSearchTarget:window, instance);
        else instance.search();
    },

    /**
     * 列表滚动
     * @param event
     */
    scroll:function(event){
        let list=Utils.getEventTarget(event);
        if(!list) return;

        let selectorId=Utils.att(list, 'selectorId');
        let instance=JSelectors.instances[selectorId];
        if(!instance) return;

        let scrollTop=list.scrollTop;

        //尚未滚动到底部
        if(!W.scrollToBottom(list)){
            //向上滚动时
            if(scrollTop<instance.listScrollTop)instance.listScrollTop=scrollTop;
            return;
        }

        if(scrollTop - instance.listScrollTop < 10) return;//相比上次滚动位置不超过10px，不予处理（避免重复调用）
        instance.listScrollTop=scrollTop;

        if(instance.onScroll) instance.onScroll.call(instance.onScrollTarget?instance.onScrollTarget:window, instance);
    },

    shown:function(){
        for(let i in this.instances){
            if(this.instances[i] && this.instances[i].shown()) return true;
        }
        return false;
    }
}

/**
 *
 * @param container 容器
 * @param selectorId ID
 * @param selectorWidth 宽度
 * @param listId 列表容器ID
 * @param listWidth 列表宽度
 * @param listHeight 列表最大高度
 * @param items 可选列表值[[id,name,callback,callbackTarget],[id,name,callback,callbackTarget]...]
 * @param itemCurrent 默认选择值[id,name]
 * @param selectorStyle 显示容器的css样式类名
 * @param selectorTextStyle 显示容器文本的css样式类名
 * @param selectorArrowStyle 显示容器下拉箭头的css样式类名
 * @param selectorArrowStyleOnShown 显示容器在列表显示状态下，下拉箭头的css样式类名
 * @param listStyle 类别容器的css样式类名
 * @param itemStyle 列表项的css样式类名
 * @param onChange 选中列表项时执行的操作
 * @param onChangeTarget 调用回调方法的对象（默认为window）
 * @param onSearch 用于替换搜索框的默认操作
 * @param onSearchTarget 调用搜功能的对象（默认window）
 * @param onScroll 列表滚动到底部时的操作（默认无操作）
 * @param onScrollTarget 调用列表滚动到底部时的操作的对象（默认window）
 * @param enableSearch 是否启用搜索
 * @constructor
 */
function JSelector(container,
                   selectorId,
                   selectorWidth,
                   listId,
                   listWidth,
                   listHeight,
                   items,
                   itemCurrent,
                   selectorStyle,
                   selectorTextStyle,
                   selectorArrowStyle,
                   selectorArrowStyleOnShown,
                   listStyle,
                   itemsStyle,
                   itemStyle,
                   onChange,
                   onChangeTarget,
                   enableSearch,
                   onSearch,
                   onSearchTarget,
                   onScroll,
                   onScrollTarget){
    if(!listId) listId=selectorId+'_list';
    if(!listWidth) listWidth=selectorWidth;

    this.container=(typeof container)=='string' ? _$(container) : container;
    this.selectorId=selectorId;
    this.selectorWidth=selectorWidth;
    this.listId=listId;
    this.listWidth=listWidth;
    this.listHeight=(typeof listHeight)=='number'?listHeight:0;
    this.items=items;
    this.itemCurrent=itemCurrent;
    this.selectorStyle=Str.isBlank(selectorStyle)?'JSelector':selectorStyle;
    this.selectorTextStyle=Str.isBlank(selectorTextStyle)?'JSelectorText':selectorTextStyle;
    this.selectorArrowStyle=Str.isBlank(selectorArrowStyle)?'JSelectorArrow':selectorArrowStyle;
    this.selectorArrowStyleOnShown=Str.isBlank(selectorArrowStyleOnShown)?'JSelectorArrowOnShown':selectorArrowStyleOnShown;
    this.listStyle=Str.isBlank(listStyle)?'JSelectorList':listStyle;
    this.itemsStyle=Str.isBlank(itemsStyle)?'JSelectorItems':itemsStyle;
    this.itemStyle=Str.isBlank(itemStyle)?'JSelectorItem':itemStyle;
    this.onChange=onChange;
    this.onChangeTarget=onChangeTarget;
    this.onSearch=onSearch;
    this.onSearchTarget=onSearchTarget;
    this.onScroll=onScroll;
    this.onScrollTarget=onScrollTarget;
    this.timer=null;
    this.searchTimer=null;
    this.startIndexToShow=0;
    this.itemCanBeChoosen=true;
    this.enableSearch=(typeof enableSearch)=='boolean'?enableSearch:true;
    if(!this.itemCurrent && this.items && this.items.length>0) this.itemCurrent=this.items[0];
    this.inScrollableContaier=(_$('Jcontent') != null) && ('scroll'==Utils.getStyle(_$('Jcontent'), 'overflowY'));
    this.listScrollTop=0;//列表滚动条位置

    if(this.itemCurrent) this.itemCurrent[1]=Lang.convert(this.itemCurrent[1]);

    //定位类型
    this.listPosition='relative';

    //是否与容器对其
    this.alignToContainer=false;

    //是否只读
    this.readOnly=false;

    JSelectors.instances[selectorId]=this;
}

/**
 * 初始化组件
 * @param container 容器ID
 */
JSelector.prototype.build=function(){
    //未指定必要参数
    if(!this.items || this.items.length==0 || !this.itemCurrent) return;

    let _selectorStyle='';
    if(this.selectorWidth) _selectorStyle+='width: '+this.selectorWidth+'px;';
    if(this.readOnly) _selectorStyle+='background-color: #eee;';

    let htm=[];
    htm.push('<div id="'+this.selectorId+'" class="'+this.selectorStyle+'" style="'+_selectorStyle+'" onclick="JSelectors.instances[\''+this.selectorId+'\'].show();">');
    htm.push('	<div id="'+this.selectorId+'_text" class="'+this.selectorTextStyle+'">'+this.itemCurrent[1]+'</div><div id="'+this.selectorId+'_arrow" class="'+this.selectorArrowStyle+'"></div>');
    htm.push('</div>');
    this.container.innerHTML=htm.join('');

    let arrowWidth=W.elementWidth(_$(this.selectorId+'_arrow'));
    if(arrowWidth<=0) arrowWidth=16;
    _$(this.selectorId+'_text').style.width=(this.selectorWidth - arrowWidth - 10)+'px';

    let _listStyle='width:'+(this.alignToContainer?W.elementWidth(this.container):this.listWidth)+'px; z-index:'+W.getMaxZIndex()+';';
    _listStyle+=' visibility:hidden; display:none; position:'+this.listPosition+';';

    let _itemsStyle='';
    if(this.listHeight>0) _itemsStyle+=' max-height:'+this.listHeight+'px; overflow-y:auto;';
    else _itemsStyle+=' display:inline-table; overflow-y:hidden;';

    htm=[];
    htm.push('<div id="'+this.listId+'" class="'+this.listStyle+'" style="'+_listStyle+'">');
    if(this.enableSearch){
        htm.push('	<div class="'+this.itemStyle+'" style="border-top:none !important;"><input type="text" id="'+this.selectorId+'_searcher" selectorId="'+this.selectorId+'" onfocus="JSelectors.keep(\''+this.selectorId+'\');" onblur="JSelectors.hideDelay(\''+this.selectorId+'\');" placeholder="I{搜索}" style="width:100%;" value="'+this.getSearchKeywords()+'"/></div>');
    }
    htm.push('<div id="'+this.listId+'_items" selectorId="'+this.selectorId+'" class="'+this.itemsStyle+'" style="'+_itemsStyle+'">');
    htm.push('</div>');
    htm.push('</div>');
    htm=Lang.convert(htm.join(''));

    if(this.inScrollableContaier) _$('JcontentBottom').insertAdjacentHTML('beforebegin', htm);
    else document.body.insertAdjacentHTML('beforeend', htm);
    htm=null;
    delete htm;

    if(_$(this.selectorId+'_searcher')){
        new InputEvent(_$(this.selectorId+'_searcher'), JSelectors.search);
    }

    //添加滚动事件监听
    _$(this.listId+'_items').addEventListener('scroll', JSelectors.scroll);
    this.buildList();
}

/**
 * 列表是否显示中
 */
JSelector.prototype.shown=function(){
    return (_$(this.listId) && _$(this.listId).style.visibility=='visible');
}

/**
 * 显示列表
 * @param force 是否强制显示
 */
JSelector.prototype.show=function(force){
    if(this.readOnly) return;
    if(!force && this.shown()){
        this.hide();
        return;
    }

    this.hide();

    let list=_$(this.listId);

    let t=0;
    let l=0;
    if(this.listPosition=='absolute'){
        t=W.elementTop(this.container);
        t+=W.elementHeight(this.container);
    }else{
        if(this.inScrollableContaier) t = W.elementTop(_$('JcontentBottom')) - (W.elementTop(_$(this.selectorId)) - W.elementTop(_$('Jcontent')));
        else t = W.elementHeight(document.body) - W.elementTop(_$(this.selectorId));
        t += W.elementHeight(list);
        t -= W.elementHeight(_$(this.selectorId));
    }
    l=this.alignToContainer?W.elementLeft(this.container):W.elementLeft(_$(this.selectorId));

    list.style.zIndex=W.getMaxZIndex()
    list.style.visibility='visible';
    list.style.display='';
    list.style.left=(l + 0)+'px';
    list.style.top=(0 - t)+'px';
    list.scrollTop=this.listScrollTop;

    _$(this.selectorId+'_text').innerHTML=this.itemCurrent[1];
    _$(this.selectorId+'_arrow').className=this.selectorArrowStyleOnShown;

    //JSelectors.hideDelay(this.selectorId);
}

/**
 * 隐藏列表
 */
JSelector.prototype.hide=function(){
    if(this.timer){
        clearTimeout(this.timer);
        this.timer=null;
    }

    _$(this.listId).style.visibility='hidden';
    _$(this.listId).style.display='none';
    _$(this.selectorId+'_arrow').className=this.selectorArrowStyle;
}

/**
 * 查找列表项
 * @param id
 * @returns {null|*}
 */
JSelector.prototype.findItem=function(id){
    for(let i=0;i<this.items.length;i++){
        if(this.items[i][0]==id) return this.items[i];
    }
    return null;
}

/**
 * 新增一个列表项
 * @param item
 */
JSelector.prototype.addItem=function(item){
    let exists=this.findItem(item[0]);
    if(exists) return;

    this.items.push(item);
}

/**
 * 新增多个列表项
 * @param _items
 */
JSelector.prototype.addItems=function(_items){
    for(let i=0;i<_items.length;i++){
        this.addItem(_items[i]);
    }
    this.buildList();
    if(this.shown()) this.show(true);
}

/**
 * 设置列表项
 * @param _items
 */
JSelector.prototype.setItems=function(_items){
    this.listScrollTop=0;
    this.items=_items;
    this.buildList();
    if(this.shown()) this.show(true);
}

/**
 * 设清除列表项
 */
JSelector.prototype.clearItems=function(){
    this.items=[];
    this.buildList();
    if(this.shown()) this.show(true);
}

/**
 * 重新构建列表
 */
JSelector.prototype.buildList=function(){
    let htm=[];
    let index=this.enableSearch?1:0;
    for(let i=this.startIndexToShow; i<this.items.length; i++){
        htm.push('	<div id="'+this.selectorId+'_item'+i+'" class="'+this.itemStyle+'" style="'+(index==0?'border-top:none !important;':'')+'" onmouseover="JSelectors.keep(\''+this.selectorId+'\');" onmouseout="JSelectors.hideDelay(\''+this.selectorId+'\');" onclick="JSelectors.instances[\''+this.selectorId+'\'].choose(\''+this.items[i][0]+'\',false);">'+this.items[i][1]+'</div>');
        index++;
    }
    _$(this.listId+'_items').innerHTML=Lang.convert(htm.join(''));
    htm=null;
    delete htm;
}

/**
 * 设置当前选中项
 * @param _current
 * @param _doNotCallback 不要回调
 * @param _doNotHideList 不要隐藏列表
 */
JSelector.prototype.setCurrent=function(_current, _doNotCallback, _doNotHideList){
    this.itemCurrent=_current;
    if(this.itemCurrent) this.itemCurrent[1]=Lang.convert(this.itemCurrent[1]);
    this.choose(_current[0],true, _doNotCallback, _doNotHideList);
}
JSelector.prototype.getCurrent=function(){
    return this.itemCurrent;
}

/**
 * 选中列表项
 * @param id
 * @param force 是否强制选中
 * @param _doNotCallback 不要回调
 * @param _doNotHideList 不要隐藏列表
 */
JSelector.prototype.choose=function(id, force, _doNotCallback, _doNotHideList){
    //不可选择且非强制选中
    if(!force && !this.itemCanBeChoosen){
        this.hide();
        return;
    }

    //隐藏列表
    if(!_doNotHideList) this.hide();

    let item=this.findItem(id);
    let text=Lang.convert(item[1]);
    _$(this.selectorId+'_text').innerHTML=text;
    this.itemCurrent=[id, text];
    if(_doNotCallback) return;

    if((typeof item[2])=='function') item[2].call(item[3]?item[3]:(this.onChangeTarget?this.onChangeTarget:window), this.selectorId, id, text);
    else if(this.onChange) this.onChange.call(this.onChangeTarget?this.onChangeTarget:window, this.selectorId, id, text);
}

/**
 *
 */
JSelector.prototype.search=function(){
    if(!_$(this.selectorId+'_searcher')) return;
    let keywords=Str.trimAll(_$(this.selectorId+'_searcher').value);

    for(let i=this.startIndexToShow; i<this.items.length; i++){
        if(!_$(this.selectorId+'_item'+i)) continue;
        let matches=Str.isBlank(keywords) || this.items[i][1].toLowerCase().indexOf(keywords.toLowerCase())>-1;
        _$(this.selectorId+'_item'+i).style.display=(matches?'':'none');
    }
}

/**
 *
 */
JSelector.prototype.getSearchKeywords=function(){
    if(!_$(this.selectorId+'_searcher')) return '';
    return Str.trimAll(_$(this.selectorId+'_searcher').value);
}

/**
 *
 * @param keywords
 */
JSelector.prototype.setSearchKeywords=function(keywords){
    if(!_$(this.selectorId+'_searcher')) return;
    _$(this.selectorId+'_searcher').value=keywords;
}