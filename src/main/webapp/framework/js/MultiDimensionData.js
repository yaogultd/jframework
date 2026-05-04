//多维数据
/**
 * 某个维度
 * @param index 第几维（0, 1, 2...)
 * @param identity 标识
 * @param name 名称
 * @param displayWidth 显示宽度（px或%，不设置则由table自动分配宽度）
 * @param verticalAlign 名称显示对其方式
 * @param choices 可选值，如['黑色', '白色', '灰色']
 * @constructor
 */
function Dimension(index, identity, name, displayWidth, verticalAlign, choices){
    this.index=index;
    this.identity=identity;
    this.name=name;
    this.displayWidth=displayWidth;
    this.verticalAlign=verticalAlign;
    this.choices=choices;
}

/**
 * 查找某维度的某个可选值
 * @param choice
 * @returns {null|DimensionChoice} 表示该可选值及其在可选值列表中位置的对象
 */
Dimension.prototype.findChoice=function(choice){
    for(let i=0; i<this.choices.length; i++){
        if(this.choices[i]==choice) return new DimensionChoice(this, i, choice);
    }
    return null;
}

/**
 * 修改选项名称
 * @param index
 * @param newName
 */
Dimension.prototype.updateChoice=function(index, newName){
    if(!this.choices[index]) return;
    this.choices[index]=newName;
}

/**
 *
 * @param dimension
 * @param index
 * @param choice
 * @constructor
 */
function DimensionChoice(dimension, index, choice){
    this.dimension=dimension;
    this.index=index;
    this.choice=choice;
}

/**
 * 数据列
 * @param identity 标识
 * @param name 列名
 * @param displayWidth 显示宽度（px或%，不设置则由table自动分配宽度）
 * @param type 数据类型 number:数字，int：整数，string：字符串，image：图片，其它：不限定
 * @param values 可选值 [DataColumnValue, DataColumnValue ...]（不指定则自由输入、指定则采用select进行选择）
 * @param validator 数据格式验证器
 * @param valueEmptyAllowed 是否允许空值
 * @param valueMinLength 数据最小（包含）长度
 * @param valueMaxLength 数据最大（包含）长度
 * @param valueMinNumber 数据（数值型）最小（包含）值
 * @param valueMaxNumber 数据（数值型）最大（包含）值
 * @param forDimensions 哪些维度具备该数据列 -1：全局（不区分维度），0：第1维度， 1，第二维度...，例如[-1,0,1]表示全局、第1维、第2维具备该数据列
 * @param unitGenerator 数据单元构造器（不指定则采用默认规则）
 * @param unitDataGetter 数据单元数据获取器（不指定则采用默认方法）
 * @param unitDataSetter 数据单元数据设置器（不指定则采用默认方法）
 * @param unitStyle 数据单元的css classname
 * @param onUnitClick 点击数据单元时执行的操作
 * @param batchEnabled 是否开启批量输入
 * @constructor
 */
function DataColumn(identity,
                    name,
                    displayWidth,
                    type,
                    values,
                    validator,
                    valueEmptyAllowed,
                    valueMinLength,
                    valueMaxLength,
                    valueMinNumber,
                    valueMaxNumber,
                    forDimensions,
                    unitGenerator,
                    unitDataGetter,
                    unitDataSetter,
                    unitStyle,
                    onUnitClick,
                    batchEnabled){
    this.identity=identity;
    this.name=name;
    this.displayWidth=displayWidth;
    this.type=type;
    this.values=values;
    this.validator=validator;
    this.valueEmptyAllowed=(typeof valueEmptyAllowed)=='boolean'?valueEmptyAllowed:true;
    this.valueMinLength=valueMinLength;
    this.valueMaxLength=valueMaxLength;
    this.valueMinNumber=valueMinNumber;
    this.valueMaxNumber=valueMaxNumber;
    this.forDimensions=forDimensions;
    this.unitGenerator=unitGenerator;
    this.unitDataGetter=unitDataGetter;
    this.unitDataSetter=unitDataSetter;
    if(Str.isBlank(unitStyle)){
        if(this.type=='number') this.unitStyle='MDDUnitNumber';
        else if(this.type=='int') this.unitStyle='MDDUnitInt';
        else if(this.type=='string') this.unitStyle='MDDUnitString';
        else if(this.type=='image') this.unitStyle='MDDUnitImage';
        else this.unitStyle='MDDUnit';
    }else{
        this.unitStyle=unitStyle;
    }
    this.onUnitClick=onUnitClick;
    this.batchEnabled=(typeof batchEnabled)=='boolean'?batchEnabled:true;
}

/**
 *
 * @param dimIndex 维度序号
 */
DataColumn.prototype.forDimension=function(dimIndex){
    for(let i=0; i<this.forDimensions.length; i++){
        if(this.forDimensions[i]==dimIndex) return true;
    }
    return false;
}

/**
 * @param mdd 多维数据表格MultiDimensionData实例
 * @param choices 各维度（不含全局）的选择项 ['黑色', 'XL']
 * @param batch 是否批量输入组件
 * @returns {*}
 */
DataColumn.prototype.getUnitId=function(mdd, choices, batch){
    //批量输入
    if(batch) return mdd.id+'_'+this.identity+'_batch';

    //全局
    if(!choices || choices.length==0)  return mdd.id+'_'+this.identity;

    let id=mdd.id;
    for(let i=0; i<choices.length; i++){
        let dChoice=mdd.dimensions[i].findChoice(choices[i]);
        if(!dChoice) return null;//无效数据

        id+='_';
        id+=dChoice.dimension.identity+'_'+dChoice.index;
    }
    id+='_'+this.identity;

    return id;
}

/**
 * 生成数据单元格
 * @param mdd 多维数据表格MultiDimensionData实例
 * @param choices 各维度（不含全局）的选择项 ['黑色', 'XL']
 * @param batch 是否批量输入组件
 */
DataColumn.prototype.genUnit=function(mdd, choices, batch){
    let unit=null;
    if(this.unitGenerator){
        unit=this.unitGenerator(mdd, choices, batch);
    }else if(this.values && this.values.length>0){
        unit=document.createElement('select');
        unit.options.add(new Option(Lang.convert('I{js,请选择}'),''));
        for(let i=0; i<this.values.length; i++){
            unit.options.add(new Option(this.values[i].text,this.values[i].value));
        }
    }else if(this.type=='number' || this.type=='int' || this.type=='string'){
        unit=document.createElement('input');
    }else{
        unit=document.createElement('div');
    }

    unit.id=this.getUnitId(mdd, choices, batch);
    unit.className=this.unitStyle;

    if(choices){
        for(let i=0; i<choices.length; i++){
            let dChoice=mdd.dimensions[i].findChoice(choices[i]);
            if(!dChoice) return null;//无效数据

            Utils.setAtt(unit, mdd.dimensions[i].identity, choices[i]);
        }
    }

    Utils.setAtt(unit, 'mdd', mdd.id);
    Utils.setAtt(unit, 'dc', this.identity);

    return unit;
}

/**
 * 获取数据单元格数据
 * @param mdd 多维数据表格MultiDimensionData实例
 * @param choices 各维度（不含全局）的选择项 ['黑色', 'XL']
 */
DataColumn.prototype.getUnitData=function(mdd, choices){
    if(this.unitDataGetter) return this.unitDataGetter(mdd, choices);

    let unit=_$(this.getUnitId(mdd, choices));
    if(!unit) return null;

    if(unit.tagName.toUpperCase()=='DIV'){
        return unit.innerHTML;
    }else{
        return unit.value;
    }
}

/**
 * 设置数据单元格数据
 * @param mdd 多维数据表格MultiDimensionData实例
 * @param choices 各维度（不含全局）的选择项 ['黑色', 'XL']
 * @param data 数据
 */
DataColumn.prototype.setUnitData=function(mdd, choices, data){
    if(this.unitDataSetter) return this.unitDataSetter(mdd, choices, data);

    let unit=_$(this.getUnitId(mdd, choices));
    if(!unit) return;

    if(unit.tagName.toUpperCase()=='DIV') unit.innerHTML=data;
    else unit.value=data;
}

/**
 * 数据有效性校验
 * @param data
 * @returns {boolean|*} 返回null表示数据无效
 */
DataColumn.prototype.valid=function(data){
    if(this.validator) return this.validator(data);

    data=Str.trimAll(data);
    if(Str.isBlank(data)) return this.valueEmptyAllowed?'':null;

    if(this.valueMinLength!=null && (typeof this.valueMinLength)=='number' && data.length<this.valueMinLength) return null;
    if(this.valueMaxLength!=null && (typeof this.valueMaxLength)=='number' && data.length>this.valueMaxLength) return null;

    if(this.type=='number' || this.type=='int'){
        if(this.type=='number' && !data.match(/^\d+$/) && !data.match(/^\d+\.\d+$/)) return null;
        if(this.type=='int' && !data.match(/^\d+$/)) return null;

        data=data*1;
        if(this.valueMinNumber!=null && (typeof this.valueMinNumber)=='number' && data<this.valueMinNumber) return null;
        if(this.valueMaxNumber!=null && (typeof this.valueMaxNumber)=='number' && data<this.valueMaxNumber) return null;
    }

    return data;
}

/**
 * 数据列可选值
 * @param value
 * @param text
 * @constructor
 */
function DataColumnValue(value, text){
    this.value=value;
    this.text=Str.isBlank(text)?value:text;
}

/**
 * 多维数据表
 * @param id
 * @param container 数据表容器
 * @param dimensions 名称
 * @param dimensions 维度
 * @param dataColumns 数据列
 * @param tableStyle 数据表的css classname
 * @constructor
 */
function MultiDimensionData(id, container, name, dimensions, dataColumns, tableStyle){
    this.id=id;
    this.container=(typeof container)=='string'?_$(container):container;
    this.name=name;
    this.dimensions=dimensions;
    this.dataColumns=dataColumns;
    this.tableStyle=Str.isBlank(tableStyle)?'MDDTable':tableStyle;
    MultiDimensionDatas.instances[this.id]=this;
}

/**
 *
 * @param dcIndentity
 */
MultiDimensionData.prototype.getDataColumn=function(dcIndentity){
    for(let i=0; i<this.dataColumns.length; i++){
        if(this.dataColumns[i].identity == dcIndentity) return this.dataColumns[i];
    }
    return null;
}

/**
 *
 * @param dimIndex
 * @param choiceIndex
 * @param choiceName
 */
MultiDimensionData.prototype.updateDimensionChoice=function(dimIndex, choiceIndex, choiceName){
    let dim=this.dimensions[dimIndex];
    if(!dim) return;
    dim.updateChoice(choiceIndex, choiceName);

    let choiceDisplay=_$(this.id+'_'+dim.identity+'_'+choiceIndex);
    if(choiceDisplay) choiceDisplay.innerHTML=choiceName;
}

/**
 * 创建表格
 */
MultiDimensionData.prototype.build=function(){
    let s=[];
    s.push('<table id="'+this.id+'" class="'+this.tableStyle+'">');

    //表头
    s.push('<tr>');
    s.push('<th colspan="'+(this.dimensions.length + this.dataColumns.length)+'" class="alignC">'+this.name+'</th>');
    s.push('</tr>');
    s.push('<tr>');
    for(let i=0; i<this.dimensions.length; i++){
        let dim=this.dimensions[i];
        s.push('<th style="'+(Str.isBlank(dim.displayWidth)?'':('width:'+dim.displayWidth))+'">'+dim.name+'</th>');
    }
    for(let i=0; i<this.dataColumns.length; i++){
        let dc=this.dataColumns[i];
        s.push('<th style="'+(Str.isBlank(dc.displayWidth)?'':('width:'+dc.displayWidth))+'">');
        s.push('<div>'+dc.name+'</div>');
        s.push('<div id="'+dc.getUnitId(this, null, true)+'_container"></div>');//批量输入组件的父节点
        s.push('</th>');
    }
    s.push('</tr>');

    //数据行
    s.push(this.genDataRows(this.dimensions[0], []));

    s.push('</table>');

    this.container.innerHTML=s.join('\n');
    s=null;
    delete s;

    //添加批量输入组件
    for(let i=0; i<this.dataColumns.length; i++){
        let dc=this.dataColumns[i];
        if(dc.batchEnabled){
            let unit=dc.genUnit(this,null, true);
            if(unit){
                _$(dc.getUnitId(this, null, true)+'_container').appendChild(unit);

                let tagName=unit.tagName.toUpperCase();
                if(tagName=='SELECT'){
                    unit.addEventListener('change',MultiDimensionDatas.setUnitDataBatch, false);
                }else if(tagName=='INPUT'){
                    unit.addEventListener('keyup',MultiDimensionDatas.setUnitDataBatch, false);
                }
            }
        }
    }

    //添加数据单元
    this.genDataUnits(this.dimensions[0], []);
    //console.log(this.container.innerHTML);
}

/**
 * 递归生成数据行
 * @param dim
 * @param choice
 * @param parentChoices
 */
MultiDimensionData.prototype.genDataRows=function(dim, parentChoices){
    let s=[];

    //全局数据行
    if(!parentChoices || parentChoices.length==0){
        s.push('<tr>');
        s.push('<td colspan="'+this.dimensions.length+'"></td>');
        for(let k=0; k<this.dataColumns.length; k++){
            s.push('<td id="'+this.dataColumns[k].getUnitId(this)+'_container"></td>');
        }
        s.push('</tr>');
    }

    let childRows=this.getRowsCount(dim.index);
    for(let j=0; j<dim.choices.length; j++){
        let choices=Utils.arrayCopy(parentChoices);
        choices.push(dim.choices[j]);

        s.push('<tr>');
        s.push('<td id="'+this.id+'_'+dim.identity+'_'+j+'"'+(dim.index<this.dimensions.length-1?(' rowspan="'+childRows+'"'):'')+' style="vertical-align: '+(Str.isBlank(dim.verticalAlign)?'top':dim.verticalAlign)+';">'+dim.choices[j]+'</td>')
        if(dim.index<this.dimensions.length-1){
            let colspan=(this.dimensions.length-dim.index-1);
            if(colspan>1) s.push('<td colspan="'+colspan+'"></td>');
            else s.push('<td></td>');
        }
        for(let k=0; k<this.dataColumns.length; k++){
            s.push('<td id="'+this.dataColumns[k].getUnitId(this, choices)+'_container"></td>');
        }
        s.push('</tr>');

        //递归生成下级维度数据行
        if(dim.index<this.dimensions.length - 1) s.push(this.genDataRows(this.dimensions[dim.index+1], choices));
    }
    return s.join('\n');
}

/**
 * 递归生成数据单元
 * @param dim
 * @param choice
 * @param parentChoices
 */
MultiDimensionData.prototype.genDataUnits=function(dim, parentChoices){
    //全局数据单元
    if(!parentChoices || parentChoices.length==0){
        for(let k=0; k<this.dataColumns.length; k++){
            let dc=this.dataColumns[k];
            if(!dc.forDimension(-1)) continue;//该维度不具备该数据行

            let unit=dc.genUnit(this);
            if(unit) _$(dc.getUnitId(this)+'_container').appendChild(unit);
        }
    }

    for(let j=0; j<dim.choices.length; j++){
        let choices=Utils.arrayCopy(parentChoices);
        choices.push(dim.choices[j]);

        for(let k=0; k<this.dataColumns.length; k++){
            let dc=this.dataColumns[k];
            if(!dc.forDimension(dim.index)) continue;//该维度不具备该数据行

            let unit=dc.genUnit(this, choices);
            if(unit) _$(dc.getUnitId(this, choices)+'_container').appendChild(unit);
        }

        //递归生成下级维度数据行
        if(dim.index<this.dimensions.length - 1) this.genDataUnits(this.dimensions[dim.index+1], choices);
    }
}

/**
 * 第index个维度的下级维度共计有多少数据行
 * @param index 当前维度的index
 * @param childIndex 某下级维度的index
 * @returns {number}
 */
MultiDimensionData.prototype.getRowsCount=function(index){
    let rows=1;
    for(let i=this.dimensions.length-1; i>index; i--) {
        rows *= this.dimensions[i].choices.length;
        rows += 1;
    }
    return rows;
}

/**
 * 初始化数据表格
 * @param data json，格式如下（数据链的规则为：维度的identity -> C+维度可选值的index [ -> 维度的identity -> C+维度可选值的index] -> 数据列identity）：
 * {
	"price":2.35,
	"stock":100,
	"icon":"/img/icon.png",

	"attr1":{//颜色
		"C0":{//黑色（第一个可选值）
			"price":2.35,
			"stock":100,
			"icon":"/img/icon.png",
			"attr2":{//尺码
				"C0":{//L
					"price":2.35,
					"stock":100,
					"icon":"/img/icon.png"
				},
				"C1":{//XL
					//...
				},
				"C2":{//XXL
					//...
				},
				"C3":{//XXXL
					//...
				}
			}
		},
		"C1":{//白色（第二个可选值）
			"price":2.35,
			"stock":100,
			"icon":"/img/icon.png",
			"attr2":{//尺码
				"C0":{//L
					"price":2.35,
					"stock":100,
					"icon":"/img/icon.png"
				},
				"C1":{//XL
					//...
				},
				"C2":{//XXL
					//...
				},
				"C3":{//XXXL
					//...
				}
			}
		}
	}
 }
 */
MultiDimensionData.prototype.init=function(data, dim, parentChoices) {
    if(!data) return;
    if((typeof data)=='string') data=JSONUtil.parse(data);

    if(!dim){
        dim=this.dimensions[0];
        parentChoices=[];
    }

    //全局数据单元
    if(!parentChoices || parentChoices.length==0){
        for (let k = 0; k < this.dataColumns.length; k++) {
            let dc = this.dataColumns[k];

            let d=JSONUtil.get(data, dc.identity);
            if(d) dc.setUnitData(this, null, d)
        }

        //第1维数据
        data=data[dim.identity];
    }

    if(!data) return;

    for (let j = 0; j < dim.choices.length; j++) {
        let choices = Utils.arrayCopy(parentChoices);
        choices.push(dim.choices[j]);

        let dataOfChoice=data['C'+j];
        for (let k = 0; k < this.dataColumns.length; k++) {
            let dc = this.dataColumns[k];

            let d=JSONUtil.get(dataOfChoice, dc.identity);
            if(d) dc.setUnitData(this, choices, d);
        }

        //递归处理
        if (dim.index < this.dimensions.length - 1){
            let nextData=JSONUtil.get(dataOfChoice, this.dimensions[dim.index+1].identity);
            this.init(nextData, this.dimensions[dim.index+1], choices);
        }
    }
}

/**
 * 获取表格数据（json，格式与init方法中data参数格式一致）
 * @param dim
 * @param parentChoices
 * @returns {string}
 */
MultiDimensionData.prototype.getData=function(dim, parentChoices){
    let s=[];

    if(!dim){
        dim=this.dimensions[0];
        parentChoices=[];
    }

    if(!parentChoices || parentChoices.length==0) s.push('{');

    //全局数据
    let index=0;
    if(!parentChoices || parentChoices.length==0){
        for(let i=0; i<this.dataColumns.length; i++){
            let dc=this.dataColumns[i];
            let unit=_$(dc.getUnitId(this, null));
            if(!unit) continue;//数据单元不存在

            let d=dc.valid(dc.getUnitData(this, null));
            if(d==null) continue;//数据无效

            if((dc.type=='number' || dc.type=='int') && Str.isBlank(d)) d=null;
            let quotes=(dc.type!='number' && dc.type!='int' && dc.type!='boolean' && !JSONUtil.isJson(d))?'"':'';

            if(index>0) s.push(',');
            s.push('"'+dc.identity+'": ');
            s.push(quotes + d + quotes);
            index++;
        }
    }

    //dimension
    if(index>0) s.push(',');
    s.push('"'+dim.identity+'": {');

    for (let j = 0; j < dim.choices.length; j++) {
        let choices = Utils.arrayCopy(parentChoices);
        choices.push(dim.choices[j]);

        //choice
        let choiceKey='C'+j;
        if(j>0) s.push(',');
        s.push('"'+choiceKey+'": {"name": "'+JSONUtil.convert(dim.choices[j])+'"');

        for (let k = 0; k < this.dataColumns.length; k++) {
            let dc = this.dataColumns[k];
            let unit=_$(dc.getUnitId(this, choices));
            if(!unit) continue;//数据单元不存在

            let d=dc.valid(dc.getUnitData(this, choices));
            if(d==null) continue;//数据无效

            if((dc.type=='number' || dc.type=='int') && Str.isBlank(d)) d=null;
            let quotes=(dc.type!='number' && dc.type!='int' && dc.type!='boolean' && !JSONUtil.isJson(d))?'"':'';

            s.push(',');
            s.push('"'+dc.identity+'": ');
            s.push(quotes + d + quotes);
        }

        //递归处理
        if (dim.index < this.dimensions.length - 1){
            s.push(',');
            s.push(this.getData(this.dimensions[dim.index+1], choices));
        }
        s.push('}');
        //choice end
    }
    s.push('}');
    //dimension end

    if(!parentChoices || parentChoices.length==0) s.push('}');
    return s.join('');
}

/**
 * 检查表格数据有效性
 * @param dim
 * @param parentChoices
 * @returns {boolean}
 */
MultiDimensionData.prototype.validData=function(dim, parentChoices){
    if(!dim){
        dim=this.dimensions[0];
        parentChoices=[];
    }

    //全局数据
    if(!parentChoices || parentChoices.length==0){
        for(let i=0; i<this.dataColumns.length; i++){
            let dc=this.dataColumns[i];
            let unit=_$(dc.getUnitId(this, null));
            if(!unit) continue;

            let d=dc.getUnitData(this, null);
            d=dc.valid(d);
            if(d==null) return false;
        }
    }

    //dimension
    let valid=true;
    for (let j = 0; j < dim.choices.length; j++) {
        let choices = Utils.arrayCopy(parentChoices);
        choices.push(dim.choices[j]);

        //choice
        for (let k = 0; k < this.dataColumns.length; k++) {
            let dc = this.dataColumns[k];
            let unit=_$(dc.getUnitId(this, choices));
            if(!unit) continue;

            let d=dc.getUnitData(this, choices);
            d=dc.valid(d);
            if(d==null) return false;
        }

        //递归处理
        if (dim.index < this.dimensions.length - 1){
            valid=valid && this.validData(this.dimensions[dim.index+1], choices);
        }
        //choice end
    }
    //dimension end

    return valid;
}

/**
 * 获取单元格数据
 * @param choices
 */
MultiDimensionData.prototype.getUnitData=function(choices, dataColumn){
    if(!dataColumn) return null;
    return dataColumn.getUnitData(this, choices);
}

/**
 * 设置单元格数据
 * @param choices
 * @param data
 */
MultiDimensionData.prototype.setUnitData=function(choices, dataColumn, data){
    if(!dataColumn) return null;
    return dataColumn.setUnitData(this, choices, data);
}

/**
 * 批量设置单元格数据
 * @param dataColumn
 * @param data
 * @param dim
 * @param parentChoices
 * @returns {boolean}
 */
MultiDimensionData.prototype.setUnitDataBatch=function(dataColumn, data, dim, parentChoices){
    if(!dim){
        dim=this.dimensions[0];
        parentChoices=[];
    }

    //全局数据
    if(!parentChoices || parentChoices.length==0){
        let unit=_$(dataColumn.getUnitId(this, null));
        if(unit){
            let tagName=unit.tagName.toUpperCase();
            if(tagName=='SELECT' || tagName=='INPUT') unit.value=data;
            else unit.innerHTML=data;
        }
    }

    //dimension
    for (let j = 0; j < dim.choices.length; j++) {
        let choices = Utils.arrayCopy(parentChoices);
        choices.push(dim.choices[j]);

        //choice
        let unit=_$(dataColumn.getUnitId(this, choices));
        if(unit){
            let tagName=unit.tagName.toUpperCase();
            if(tagName=='SELECT' || tagName=='INPUT') unit.value=data;
            else unit.innerHTML=data;
        }

        //递归处理
        if (dim.index < this.dimensions.length - 1){
            this.setUnitDataBatch(dataColumn, data, this.dimensions[dim.index+1], choices);
        }
        //choice end
    }
    //dimension end
}

MultiDimensionDatas={
    instances:[],

    /**
     * 批量设置数据
     * @param event
     */
    setUnitDataBatch:function(event, srcObject){
        let target=srcObject?srcObject:Utils.getEventTarget(event);
        if(!target) return;

        let mddId=Utils.att(target, 'mdd');
        if(!mddId) return;

        let dcIdentity=Utils.att(target, 'dc');
        if(!dcIdentity) return;

        let mdd=MultiDimensionDatas.instances[mddId];
        if(!mdd) return;

        let dc=mdd.getDataColumn(dcIdentity);
        if(!dc) return;

        let tagName=target.tagName.toUpperCase();
        if(tagName=='SELECT' || tagName=='INPUT'){
            mdd.setUnitDataBatch(dc, target.value);
        }else{
            mdd.setUnitDataBatch(dc, target.innerHTML);
        }
    }
}