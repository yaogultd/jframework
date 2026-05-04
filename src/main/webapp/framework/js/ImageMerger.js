//图片拼接
/**
 * 图片拼接
 * @param args
 * @constructor
 */
function ImageMerger(args){
    this.width=(typeof args.width)=='number' ? args.width : W.vw();
    this.height=(typeof args.height)=='number' ? args.height : W.vh();
    this.units=(Array.isArray(args.units)) ? args.units : [];
    this.target=args.target;
    this.canvas=null;
    this.context=null;
    this.bgColor=(typeof args.bgColor)=='string' ? args.bgColor : '#FFF';
    this.callback=args.callback;
    this.callbackTarget=args.callbackTarget;
    this.loaded=0;
}

/**
 *
 * @param unit
 */
ImageMerger.prototype.appendUnit=function(unit){
    this.units.push(unit);
}

/**
 *
 */
ImageMerger.prototype.merge=function(){
    //初始化画布
    this.canvas = document.createElement("canvas");
    this.canvas.width = this.width;
    this.canvas.height = this.height;
    this.context = this.canvas.getContext("2d");
    this.unitOffsetYAdjust = 0;

    //根据屏幕像素比缩放
    let ratio=W.getPixelRatio(this.context)
    this.canvas.style.width = this.canvas.width + 'px';
    this.canvas.style.height = this.canvas.height + 'px';
    this.canvas.width = this.canvas.width * ratio;
    this.canvas.height = this.canvas.height * ratio;

    if(!Str.isBlank(this.bgColor)) {
        this.context.fillStyle = this.bgColor;
        this.context.fillRect(0, 0, this.width, this.height);
    }

    for(let i=0; i<this.units.length; i++) {
        Logger.log('try to load unit('+this.units[i].type+') -> '+this.units[i].source);
        this.units[i].container=this;
        this.units[i].load();
    }
}

/**
 *
 * @param _target
 * @returns {HTMLImageElement|*}
 */
ImageMerger.prototype.toImage=function(_target){
    if(_target){
        _target.src=this.canvas.toDataURL('image/png');
        return _target;
    }

    if(this.target){
        this.target.src=this.canvas.toDataURL('image/png');
        return this.target;
    }

    let image=new Image();
    image.crossOrigin='anonymous';
    image.src=this.canvas.toDataURL('image/png');
    return image;
}

/**
 * 当某个元素加载完毕
 */
ImageMerger.prototype.onUnitLoad=function(){
    this.loaded++;
    Logger.log('this.loaded = '+this.loaded + ' of '+this.units.length);
    if(this.loaded>=this.units.length){//元素全部加载完毕，回调
        if(this.callback) this.callback.call(this.callbackTarget ? this.callbackTarget : window, this);
    }
}

/**
 * 图片拼接单元
 * @param args
 * @constructor
 */
function ImageMergerUnit(args){
    this.uuid=Global.generateUUID();
    this.type=(typeof args.type)=='string' ? args.type : 'image';// image | text | line
    this.width=(typeof args.width)=='number' ? args.width : W.vw();
    this.height=(typeof args.height)=='number' ? args.height : W.vh();
    this.offsetX=(typeof args.offsetX)=='number' ? args.offsetX : 0;
    this.offsetY=(typeof args.offsetY)=='number' ? args.offsetY : 0;
    this.quality=(typeof args.quality)=='number' ? args.quality : 1;
    this.source=args.source;//url | file | text
    this.fontStyle=args.fontStyle;
    this.container=null;
    this.fontColor=(typeof args.fontColor)=='string' ? args.fontColor : '#333333';
    this.align=(typeof args.align)=='string' ? args.align : 'left';

    let _unit=this;
    if(this.type=='image'){
        this.img=new Image();//图像
        this.img.crossOrigin='anonymous';
        this.img.onload = function(e){//加载完成后
            Logger.log('image unit of ImageMerger loaded -> '+_unit.uuid);
            _unit.container.context.drawImage(_unit.img,
                _unit.offsetX,
                _unit.offsetY,
                _unit.width,
                _unit.height);
            _unit.container.onUnitLoad();
        };

        if((typeof this.source)!='string'){
            this.reader=new FileReader();//文件读取器
            this.reader.onload = function(e){//读取完成后给图像赋值
                Logger.log('image(file) unit of ImageMerger loaded -> '+_unit.uuid);
                _unit.img.src = e.target.result;//图像赋值后会触发img的onload
            };
        }
    }
}

/**
 * 加载
 */
ImageMergerUnit.prototype.load=function(){
    this.offsetY+=this.container.unitOffsetYAdjust;//根据上下元素实际输出高度调整位置

    if(this.type=='text') {//如果是文字
        if(this.fontColor) this.container.context.fillStyle = this.fontColor;
        if(this.fontStyle) this.container.context.font = this.fontStyle;

        let textWidth=this.container.context.measureText(this.source).width;
        if(this.align=='center'){//文本居中
            this.offsetX=Math.round((this.container.width - textWidth)/2);
            if(this.offsetX<0) this.offsetX=0;
        }

        //换行输出
        let line='';
        let output = 0;
        let lines=0;
        let textHeight=0;
        while(output < this.source.length){
            line+=this.source.substring(output, output+1);
            if(this.container.context.measureText(line).width >= this.width){
                this.container.context.fillText(line+'\n',this.offsetX,this.offsetY+lines*20, this.width);
                line='';
                lines++;
                textHeight+=20;
            }
            output++;
        }
        if(!Str.isBlank(line)){
            textHeight+=20;
            this.container.context.fillText(line,this.offsetX,this.offsetY+lines*20, this.width);
        }
        this.container.unitOffsetYAdjust+=(textHeight - this.height);
        //换行输出 end

        Logger.log('unit('+this.type+') '+this.source+' loaded.');
        this.container.onUnitLoad();
        return;
    }

    if(this.type=='line') {//如果是线条
        this.container.context.moveTo(this.offsetX,this.offsetY);
        this.container.context.lineTo(this.offsetX+this.width,this.offsetY);
        this.container.context.closePath();
        this.container.context.fillStyle=this.fontColor;
        this.container.context.lineWidth=this.height;
        this.container.context.fill();
        Logger.log('unit('+this.type+') '+this.source+' loaded.');
        this.container.onUnitLoad();
        return;
    }

    if(Str.isBlank(this.source)){
        this.container.onUnitLoad();
        return;
    }

    if((typeof this.source)=='string') this.img.src = this.source;
    else this.reader.readAsDataURL(this.source);
}