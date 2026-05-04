//单选组件
/**
 * 所有单选组件实例
 * @type {{instances: any[]}}
 */
let Pickers={
    instances:[],

    get:function (id){
        return this.instances[id];
    }
}

/**
 * 选择项
 * @param value 值
 * @param name 名称
 * @param style 样式（css className）
 * @param styleSelected 选中后的样式（css className）
 * @param canBeChoosen 是否可选
 * @param callback 回调
 * @constructor
 */
function PickerItem(value, name, style, styleSelected, canBeChoosen, callback){
    this.value=value;
    this.name=Lang.convert(name);
    this.style=style;
    this.styleSelected=styleSelected;
    this.canBeChoosen=(typeof canBeChoosen)=='boolean'?canBeChoosen:true;
    this.callback=callback;
    this.index=0;
}

/**
 *
 * @param container 容器
 * @param id 组件ID
 * @param style（css className）
 * @param input 关联input
 * @param items 可选项
 * @param initValue 初始值
 * @param itemStyle（css className）
 * @param itemStyleSelected（css className）
 * @param editable 是否可修改选中项（不能则仅用于展现）
 * @param callback 回调
 * @constructor
 */
function Picker(container,
                id,
                style,
                input,
                items,
                initValue,
                itemStyle,
                itemStyleSelected,
                editable,
                callback){
    this.container=(typeof container)=='string'?_$(container):container;
    this.id=id;
    this.style=(!style?'Picker':style);
    this.input=(typeof input)=='string'?_$(input):input;
    this.items=items;
    this.initValue=initValue;
    this.itemStyle=(!itemStyle?'PickerItem':itemStyle);
    this.itemStyleSelected=(!itemStyleSelected?'PickerItemSelected':itemStyleSelected);
    this.editable=(typeof editable)=='boolean'?editable:true;
    this.callback=callback;
    Pickers.instances[id]=this;

    let htm=[];
    htm.push('<div id="'+id+'" class="'+this.style+'">');
    for(let i=0;i<items.length;i++){
        items[i].index=i;
        let item=items[i];
        let _style=Str.isBlank(item.style)?this.itemStyle:item.style;

        let _disabledCss='cursor:not-allowed !important;';
        htm.push('<div id="'+id+'_'+i+'" style="'+(this.editable?'':_disabledCss)+'" class="'+_style + (i==0?' noBorderL':'')+'" onclick="Pickers.instances[\''+id+'\'].pick('+i+', false);">'+item.name+'</div>');
    }
    htm.push('</div>');

    this.container.innerHTML=htm.join('');
    htm=null;
    delete htm;

    for(let i=0;i<items.length;i++){
        if(initValue==items[i].value){
            this.pick(i, true);
            break;
        }
    }
}

/**
 * 查找指定下标或值对应的项
 * @param value
 */
Picker.prototype.findItem=function (valueOrIndex){
    if((typeof valueOrIndex)=='number') return this.items[value];

    for(let i=0; i<this.items.length; i++){
        if(valueOrIndex==this.items[i].value) return this.items[i];
    }
    return null;
}

Picker.prototype.setValue=function (value){
    let item=this.findItem(value);
    if(!item) return;
    this.pick(item.index, true);
}

/**
 * 选中指定项
 * @param index
 * @param force 是否强制选中（即时在不可修改选中项情况下）
 * @returns {null|*}
 */
Picker.prototype.pick=function(index, force){
    let choice=this.items[index];
    if(!choice) return;

    if(!force && (!this.editable || !choice.canBeChoosen)) return;//不可选择

    for(let i=0; i<this.items.length; i++){
        let item=this.items[i];
        let _style=Str.isBlank(item.style)?this.itemStyle:item.style;
        _$(this.id+'_'+i).className=_style+(i==0?' noBorderL':'');
    }

    let _styleSelected=Str.isBlank(choice.styleSelected)?this.itemStyleSelected:choice.styleSelected;
    _$(this.id+'_'+index).className=_styleSelected+(index==0?' noBorderL':'');

    if(this.input) this.input.value=choice.value;

    let _callback=choice.callback?choice.callback:this.callback;
    if(_callback) _callback(this.id, this.input, choice.value, choice.name);
}