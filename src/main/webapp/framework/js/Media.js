//媒体处理器/////////
let JMedias=[];
let JMediaUtil={
    /**
     * 清空媒体对象
     * @param name 指定对象
     */
    clear:function(name){
        for(let i in JMedias){
            if(name && name!=i) continue;
            if(JMedias[i]){
                JMedias[i].clear();
                JMedias[i]=null;
            }
        }
        if(!name) JMedias=[];
    },

    /**
     * 播放声音
     * @param id
     * @param mediaUrl
     */
    playSound:function(id, mediaUrl){
        if(!_$(id)) {
            document.body.insertAdjacentHTML('afterbegin', '<audio id="'+id+'"><source src="' + mediaUrl + '" type="audio/mpeg"/></audio>');
            if(_$(id).load) _$(id).load();
        }
        _$(id).play();
    },

    /**
     * 停止播放
     * @param id
     */
    stopSound:function(id){
        if(_$(id)) _$(id).stop();
    },

    getAudioType:function(fileName){
        if(fileName.indexOf('?')>0) fileName=fileName.substring(0, fileName.indexOf('?'));
        if(Str.endsWith(fileName, ".ogg")) return "audio/ogg";
        else if(Str.endsWith(fileName, ".wav")) return "audio/wav";
        else return "audio/mpeg";
    }
}
window.JMediaUtil=JMediaUtil;
window.JMedias=JMedias;

/**
 * 媒体对象
 * @param fileOrUrl 媒体文件路径/url
 * @param container 容器
 * @param maxLength 最大长度
 * @param quality 品质（0~1）
 * @param callback 回调方法
 * @returns {*}
 * @constructor
 */
function JMedia(fileOrUrl, container, maxLength, quality, callback, callbackTarget){
    let _id='';
    if((typeof fileOrUrl)=='string'){
        _id=('JM'+Math.random());
    }else{
        _id=fileOrUrl.name;
    }
    this.id=_id;
    JMedias[this.id]=this;//保存实例
    if(top != window && (typeof top.JMedias)!='undefined'){
        top.JMedias[this.id]=this;
    }
    this.dataOriginal=null;//原始数据
    this.data=null;//数据
    this.dataSize=0;//大小
    this.dataSizeOriginal=0;//原始大小
    this.widthOriginal=0;//原始宽度
    this.heightOriginal=0;//原始高度
    this.maxLength=maxLength;//最大长度
    this.quality=quality;//品质
    this.callback=callback;//回调
    this.callbackTarget=callbackTarget;//调用回调函数的对象
    this.fileOrUrl=fileOrUrl;//媒体文件路径/url
    this.container=((typeof container)=='string'?_$(container):container);//容器
    this.reader=new FileReader();//文件读取器
    this.img=new Image();//图像
    this.img.crossOrigin='anonymous';
    this.imgTrim=new Image();//剪裁过的图像
    this.imgTrim.crossOrigin='anonymous';
    this.img.onload = function(e){//加载完成后，如果图片长或宽超出maxLength，则自动进行缩放
        JMedias[_id].zoom(_id);
    };
    this.reader.onload = function(e){//读取完成后给图像赋值
        JMedias[_id].img.src = e.target.result;
    };
    if((typeof fileOrUrl)=='string'){
        fileOrUrl=fileOrUrl.toLowerCase();
        if(fileOrUrl.endsWith('.png')){
            this.mimeType='image/png';
        }else{
            this.mimeType='image/jpeg';
        }
    }else{
        this.mimeType=(fileOrUrl.type||'image/png');
    }

    this.trimLeftRatio=0;//左边裁剪比率
    this.trimRightRatio=0;//右边裁剪比率
    this.trimTopRatio=0;//顶部裁剪比率
    this.trimBottomRatio=0;//底部裁剪比率

    //缩放图片需要的canvas
    this.canvas = document.createElement('canvas');
    this.context = this.canvas.getContext('2d');
    if(this.container) this.container.appendChild(this.canvas);

    //开始处理
    this.start();

    return this.id;
}
JMedia.prototype.clear=function(){
    this.img=null;
    this.imgTrim=null;
    this.reader=null;
    if(this.container && this.canvas){
        try{
            this.container.removeChild(this.canvas);
        }catch (e){}
    }
    this.context=null
    this.canvas=null;
    this.data=null;
    this.dataOriginal=null;
}
JMedia.prototype.start=function(){
    if((typeof this.fileOrUrl)=='string'){
        this.img.src = this.fileOrUrl;
    }else{
        this.reader.readAsDataURL(this.fileOrUrl);
    }
}

//压缩
JMedia.prototype.zoom=function(id){
    this.widthOriginal=this.img.width;
    this.heightOriginal=this.img.height;
    let ratio=1;//缩放比率
    if(this.widthOriginal<=this.maxLength
        && this.heightOriginal<=this.maxLength){
        //不需要压缩
        this.quality=1;
    }else{
        if(this.widthOriginal > this.heightOriginal) ratio=this.maxLength/this.widthOriginal;
        else ratio=this.maxLength/this.heightOriginal;
    }

    let width=Math.floor(this.widthOriginal*ratio);
    let height=Math.floor(this.heightOriginal*ratio);

    //canvas对图片进行缩放
    let dx=0;
    let dy=0;
    this.canvas.width = width;
    this.canvas.height = height;

    //清除画布
    if(width>height) this.context.clearRect(0, 0, width, width);
    else this.context.clearRect(0, 0, height, height);

    //图片压缩
    this.context.restore();//恢复状态
    this.context.drawImage(this.img, dx, dy, width, height);
    this.context.save();

    //输出临时图像到img
    this.imgTrim.src=this.getData();

    //canvas转为blob
    this.canvas.toBlob(function (blob) {
        JMedias[id].data=blob;
        JMedias[id].dataSize=blob.size;
        JMedias[id].dataSizeOriginal=blob.size;
        if(!JMedias[id].dataOriginal) JMedias[id].dataOriginal=blob;
        if(JMedias[id].callback){
            if(JMedias[id].callbackTarget) JMedias[id].callback.call(JMedias[id].callbackTarget, JMedias[id].img, JMedias[id].imgTrim, id);
            JMedias[id].callback.call(window, JMedias[id].img, JMedias[id].imgTrim, id);
        }
    }, JMedias[id].mimeType);
}

//剪裁
JMedia.prototype.trim=function(id, WHRatio){
    this.quality=1;//不再压缩

    let oldWidth = this.canvas.width;
    let oldHeight = this.canvas.height;

    //左上角坐标
    let startX=Math.floor(oldWidth*this.trimLeftRatio);
    let startY=Math.floor(oldHeight*this.trimTopRatio);

    //截取后宽、高
    let width=Math.floor(oldWidth*(1-this.trimLeftRatio-this.trimRightRatio));
    let height=WHRatio ? (width/WHRatio): Math.floor(oldHeight*(1-this.trimTopRatio-this.trimBottomRatio));

    let dx=0;
    let dy=0;

    this.canvas.width = width;
    this.canvas.height = height;

    //清除画布
    if(width>height) this.context.clearRect(0, 0, width, width);
    else this.context.clearRect(0, 0, height, height);

    //图片压缩
    this.context.drawImage(this.imgTrim, startX,startY, width,height,dx, dy, width, height);
    this.context.restore();

    //输出临时图像到img
    this.imgTrim.src=this.getData();

    //canvas转为blob
    this.canvas.toBlob(function (blob) {
        JMedias[id].data=blob;
        JMedias[id].dataSize=blob.size;
        if(!JMedias[id].dataOriginal) JMedias[id].dataOriginal=blob;
    }, JMedias[id].mimeType);
}

//旋转
JMedia.prototype.rotate=function(id, degrees) {
    let width=this.imgTrim.width;
    let height=this.imgTrim.height;

    // 清除画布
    if(width>height) this.context.clearRect(0, 0, width, width);
    else this.context.clearRect(0, 0, height, height);

    this.context.save();//保存状态

    this.canvas.width=height;
    this.canvas.height=width;
    Utils.setAtt(this.canvas,'width',height);
    Utils.setAtt(this.canvas,'height',width);

    this.context.translate(height,0);
    this.context.rotate(degrees*Math.PI/180);

    this.context.drawImage(this.imgTrim, 0,0, width, height);

    this.context.restore();//恢复状态

    //输出临时图像到img
    this.imgTrim.src=this.getData();

    // canvas转为blob
    this.canvas.toBlob(function (blob) {
        JMedias[id].data=blob;
        JMedias[id].dataSize=blob.size;
        JMedias[id].dataSizeOriginal=blob.size;
        if(!JMedias[id].dataOriginal) JMedias[id].dataOriginal=blob;
        if(JMedias[id].callback){
            if(JMedias[id].callbackTarget) JMedias[id].callback.call(JMedias[id].callbackTarget, JMedias[id].img, JMedias[id].imgTrim, id);
            JMedias[id].callback.call(window, JMedias[id].img, JMedias[id].imgTrim, id);
        }
    }, JMedias[id].mimeType);
}
JMedia.prototype.getData=function(){
    //type，设置输出的类型，比如 image/png image/jpeg等
    //encoderOptions： 0-1之间的数字，用于标识输出图片的质量，1表示无损压缩，类型为： image/jpeg 或者image/webp才起作用。
    return this.canvas.toDataURL(this.mimeType, this.quality);
}

let MediaManager={
    callback: null,
    callbackTarget: null,
    multi: true,
    inLayer: null,

    open:function (url, _multi, _callback, _callbackTarget){
        this.multi = (typeof _multi) == 'boolean' ? _multi : true;
        this.callback = (_callback ? _callback : null);
        this.callbackTarget = (_callbackTarget ? _callbackTarget : window);

        url+=(url.indexOf('?') > 0 ? '&' : '?')+'multi='+this.multi;
        url+='&selecting=true';

        this.inLayer = Layers.open(window, 'I{.媒体库}', url, '', null, 0, null);
    },

    done:function (selectedMedias){
        if(!this.multi && selectedMedias && selectedMedias.length>1){
            Page.alert('I{.只能选择一个媒体}');
            return;
        }

        if(this.callback){
            this.callback.call(this.callbackTarget, selectedMedias);
        }

        this.inLayer.close();
    }
}
//媒体处理器 end/////