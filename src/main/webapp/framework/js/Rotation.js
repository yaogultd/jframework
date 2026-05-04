//媒体轮播组件
function Rotation(id,width,height,photos,mediaTypes,mediaSizes,medias,links,direction,speed,showNumbers,openInImageViewer){
    this.id=id;//id
    this.width=width;//宽度
    this.height=height;//高度
    this.photos=photos;//图片（或视频的封面）
    this.mediaTypes=mediaTypes;//媒体类型（photo,video）
    this.mediaSizes=mediaSizes;//媒体宽和高[宽,高]
    this.medias=medias;//媒体（未指定取photos值）
    this.links=links;//点击打开的链接（或动作，如 javascript:alert(some message)）
    this.direction=direction;//图片滚动方向（L,T 表示左移、上移）
    this.speed=speed;//滚动间隔（单位：毫秒）
    this.showNumbers=(showNumbers?showNumbers:false);//是否显示编号
    this.numbersOffSet=-30;//编号显示位置便宜量

    this.sliderIndex=0;//当前显示媒体下标
    this.sliderTimeout=null;//计时器
    this.sliderInterval=null;//计时器
    this.sliderLength=0;//累计滚动长度

    this.paused=(photos.length<2);//是否暂停
    this.inPlays=[];//各媒体是否处于播放状态
    this.zoomType=4;//1,按宽  2，按高  3，按较长一边  4，正好满屏，较长（相对于容器长宽比例而言）一边超出容器部分隐藏
    this.currentMediaType=null;//当前显示媒体类型（视频/图片）
    this.openInImageViewer=(typeof openInImageViewer)=='undefined'?true:openInImageViewer;//点击时是否通过图片查看组件查看图片

    Rotations.instances[id]=this;
}

/**
 * 计算第i个媒体的显示尺寸
 * @param i
 * @returns {[*, *]}
 */
Rotation.prototype.mediaDisplaySize=function(i){
    let w=0;
    let h=0;
    let ratio=this.width/this.height;
    let ratioMedia=this.mediaSizes[i][0]/this.mediaSizes[i][1];
    if(ratio>=ratioMedia){
        h=this.height;
        w=Math.floor(h*ratioMedia);
    }else{
        w=this.width;
        h=Math.floor(w/ratioMedia);
    }

    return [w,h];
}

/**
 * 当前显示媒体类型的媒体个数
 * @returns {number|*}
 */
Rotation.prototype.countOfMediaType=function(type){
    if(!type) type=this.currentMediaType;
    if(!type) return this.photos.length;

    let count=0;
    for(let i=0;i<this.mediaTypes.length;i++){
        if(this.mediaTypes[i]==type) count++;
    }
    return count;
}

/**
 * 所有媒体中是否同时有视频和图片
 * @returns {boolean}
 */
Rotation.prototype.bothVideoAndPhoto=function(){
    let hasVideo=false;
    let hasPhoto=false;
    for(let i=0; i<this.mediaTypes.length; i++){
        if(this.mediaTypes[i]=='video') hasVideo=true;
        if(this.mediaTypes[i]=='photo') hasPhoto=true;
    }
    return hasVideo && hasPhoto;
}

/**
 * 在ImageViewer中打开
 */
Rotation.prototype.openImageViewer=function(){
    let list=[];
    for(let i=0; i<this.medias.length; i++){
        list.push(new ImageViewer.Media('', this.medias[i], this.photos[i]));
    }
    ImageViewer.open(window, list, this.photos[this.sliderIndex]);
}

/**
 * 初始化组件
 * @param containerId 父容器ID
 */
Rotation.prototype.init=function(containerId){
    for(let i=0;i<this.photos.length;i++) this.inPlays[i]=this.paused;

    let htm=[];
    htm.push('<div id="'+this.id+'_container" style="width:'+this.width+'px; height:'+this.height+'px !important; overflow:hidden !important;">');

    if(this.direction=='L'){
        htm.push('<div id="'+this.id+'" style="width:'+this.width*(this.photos.length+1)+'px; height:'+this.height+'px; overflow:hidden !important;">');
    }else{
        htm.push('<div id="'+this.id+'" style="width:'+this.width+'px; height:'+this.height*(this.photos.length+1)+'px; overflow:hidden !important;">');
    }

    for(let i=0; i<this.photos.length; i++){
        htm.push('<div class="rotationMedia" id="'+this.id+'_box_'+i+'" style="width:'+this.width+'px; height:'+this.height+'px; text-align:center; overflow:hidden !important;'+(this.direction=='L'?' float:left;':'')+'">');

        if(this.mediaTypes[i]=='video'){
            htm.push('<video id="'+this.id+'_box_'+i+'_player" width="'+this.width+'" height="'+this.height+'" preload="auto" playsinline webkit-playsinline></video>');
            this.inPlays[i]=false;
            Players.reset(this.id+'_box_'+i+'_player');
        }else{
            let hasLink=false;//是否需要打开链接
            if(this.links[i].indexOf('javascript')==0){
                hasLink=true;
                htm.push('<a href="javascript:_void();" onclick="event.cancelBubble=true; '+this.links[i]+'">');
            }else if(this.links[i]!=''){
                hasLink=true;
                htm.push('<a href="javascript:_void();" onclick="event.cancelBubble=true; Rotations.openUrl(\''+this.links[i]+'\');">');
            }

            if(hasLink){
                htm.push('	<img id="'+this.id+'_img_'+i+'" _src="'+this.photos[i]+'" style="display:none;"/>');
            }else{
                if(this.openInImageViewer) htm.push('	<img id="'+this.id+'_img_'+i+'" _src="'+this.photos[i]+'" style="display:none;" onclick="event.cancelBubble=true; Rotations.instances[\''+this.id+'\'].openImageViewer();"/>');
                else htm.push('	<img id="'+this.id+'_img_'+i+'" _src="'+this.photos[i]+'" style="display:none;" onclick="event.cancelBubble=true;"/>');
            }

            if(hasLink) htm.push('</a>');
        }

        htm.push('</div>');
    }

    //最后添加一个空白框，是的最后一张可以继续滚动效果
    htm.push('<div class="rotationMedia" mediaType="photo" id="'+this.id+'_box_'+this.photos.length+'" style="width:'+this.width+'px; height:'+this.height+'px; text-align:center; overflow:hidden !important;'+(this.direction=='L'?' float:left;':'')+'">');
    htm.push('	<img id="'+this.id+'_img_'+this.photos.length+'" _src="/framework/img/blank.png" style="display:none;"/>');
    htm.push('</div>');
    //最后添加一个空白框，是的最后一张可以继续滚动效果 end

    htm.push('</div>');

    htm.push('</div>');

    //媒体编号
    if(this.showNumbers || (this.bothVideoAndPhoto() && UserAgent.isMobile())){
        htm.push('<div class="rotationNumbers" style="top:'+this.numbersOffSet+'px !important;" id="'+this.id+'_rotationNumbers">');

        if(this.bothVideoAndPhoto() && UserAgent.isMobile()){
            htm.push('<div class="rotationSwitches" id="'+this.id+'_rotationSwitch">');
            htm.push('	<div class="rotationSwitch" style="border-left:none !important;" id="'+this.id+'_switch_video" onclick="event.cancelBubble=true; Rotations.switchType(\''+this.id+'\',\'video\');">I{js,视频}</div>');
            htm.push('	<div class="rotationSwitch" id="'+this.id+'_switch_photo" onclick="event.cancelBubble=true; Rotations.switchType(\''+this.id+'\',\'photo\');">I{js,图片}</div>');
            htm.push('</div>');
        }

        if(this.showNumbers){
            if(UserAgent.isMobile()){
                htm.push('	<div id="'+this.id+'_rotationNumbersCount" class="rotationNumbersCount">1/'+this.photos.length+'</div>');
            }else{
                for(let i=0;i<this.photos.length;i++){
                    htm.push('<div class="'+(i==0?'rotationNumberCurrent':'rotationNumber')+'" id="'+this.id+'_num_'+i+'" onclick="event.cancelBubble=true; Rotations.show(\''+this.id+'\','+i+');"></div>');
                }
            }
        }

        htm.push('</div>');
    }
    //媒体编号 end

    _$(containerId).innerHTML=Lang.convert(htm.join(''));
    delete htm;

    if(this.photos.length==0) return;

    for(let i=0;i<this.photos.length;i++){
        if(this.mediaTypes[i]=='video') continue;

        try{
            IMG.reset(this.id+'_img_'+i);
            IMG.adjust(this.id+'_img_'+i,
                this.id+'_img_'+i+'_rotation',
                this.zoomType,
                this.width,
                this.height,
                true,
                false,
                null,
                null,
                null,
                true,
                true);
        }catch(e){}
    }

    if(UserAgent.isMobile()){
        let touch = new Touch(_$(this.id), 20, null, null, null, null, this.showNext, this.showPrevious, null, null);
        touch.preventDefaultOnClick = false;
        touch.callbackCaller=this;
    }

    this.show(0);
}

/**
 * 找到当前指定类别下显示的第i个媒体，在初始的全部媒体列表中是第几个
 * @param i
 */
Rotation.prototype.find=function(i){
    let count=0;
    for(let index=0; index<this.photos.length; index++){
        if(this.currentMediaType && this.currentMediaType!=this.mediaTypes[index]) continue;
        if(count==i) return index;
        count++;
    }
}

/**
 * 当前图片（视频返回封面）
 */
Rotation.prototype.getCurrentPhoto=function(){
    let count=0;
    let index=0;
    for(index=0; index<this.photos.length; index++){
        if(this.currentMediaType && this.currentMediaType!=this.mediaTypes[index]) continue;
        if(count==this.sliderIndex) break;
        count++;
    }
    let size=this.mediaSizes[index];
    return [this.photos[index], size[0], size[1]];
}

/**
 * 当前媒体
 */
Rotation.prototype.getCurrentMedia=function(){
    let count=0;
    for(let index=0; index<this.photos.length; index++){
        if(this.currentMediaType && this.currentMediaType!=this.mediaTypes[index]) continue;
        if(count==this.sliderIndex) return Str.isBlank(this.medias[index]) ? this.photos[index] : this.medias[index];
        count++;
    }
    return Str.isBlank(this.medias[0]) ? this.photos[0] : this.medias[0];
}

/**
 * 显示第i个媒体
 * @param i
 */
Rotation.prototype.show=function(i){
    if(this.sliderInterval) clearInterval(this.sliderInterval);
    if(this.sliderTimeout) clearTimeout(this.sliderTimeout);

    this.sliderLength=0;
    this.sliderIndex=i;
    if(this.direction=='L'){
        _$(this.id+'_container').scrollLeft=(this.sliderIndex*this.width+this.sliderLength);
    }else{
        _$(this.id+'_container').scrollTop=(this.sliderIndex*this.height+this.sliderLength);
    }

    //原始列表中的位置
    i=this.find(i);

    //如果是视频
    if(this.mediaTypes[i]=='video'){
        let playerInstance=Players.getPlayer(this.id+'_box_'+i+'_player');
        if(!playerInstance){
            let size=this.mediaDisplaySize(i);
            Players.addPlayer(this.id+'_box_'+i+'_player',
                this.medias[i],
                false,
                size[0],
                size[1],
                false,
                false,
                true,
                this.photos[i],
                false,
                'W');
            Players.setMaxWidth(this.id+'_box_'+i+'_player', this.width);
            Players.setMaxHeight(this.id+'_box_'+i+'_player', this.height);
            Players.initPlayers();
        }else{
            Players.play(this.id+'_box_'+i+'_player');
        }
    }

    //停止播放非当前显示的视频
    for(let j=0;j<this.photos.length;j++){
        if(this.mediaTypes[j]=='video' && j!=i){
            Players.stop(this.id+'_box_'+j+'_player');
        }
    }

    //切换显示的媒体编号
    for(let n=0;n<this.photos.length;n++){
        if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
    }
    if(_$(this.id+'_num_'+i)) _$(this.id+'_num_'+i).className='rotationNumberCurrent';
    if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

    //非暂停状态下，定时显示下一个媒体
    if(!this.paused){
        this.sliderTimeout=setTimeout("Rotations.doSlider('"+this.id+"')",this.speed);
    }
}

/**
 * 显示下一个
 */
Rotation.prototype.showNext=function(event, by){
    let i=this.sliderIndex+1;
    if(i==this.countOfMediaType()){//开始新的循环
        i=0;
    }
    this.show(i);

    if(by) this.pause();
}

/**
 * 显示前一个
 */
Rotation.prototype.showPrevious=function(event, by){
    let i=this.sliderIndex-1;
    if(i<0){//开始新的循环
        i=this.countOfMediaType()-1;
    }
    this.show(i);

    if(by) this.pause();
}

/**
 * 当前显示的（或指定）媒体是否为视频
 * @param index
 * @returns {boolean}
 */
Rotation.prototype.videoShown=function(index){
    if(this.currentMediaType==null){
        if((typeof index) != 'undefined') return this.mediaTypes[index]=='video';
        else return this.mediaTypes[this.sliderIndex]=='video';
    }else if(this.currentMediaType=='video'){
        return true;
    }else{
        return false;
    }
}

/**
 * 重置轮播组件为初始状态
 */
Rotation.prototype.reset=function(){
    if(this.sliderInterval) clearInterval(this.sliderInterval);
    if(this.sliderTimeout) clearTimeout(this.sliderTimeout);
    this.sliderLength=0;
    this.sliderIndex=0;

    _$(this.id+'_container').scrollLeft=0;
    _$(this.id+'_container').scrollTop=0;

    for(let n=0;n<this.photos.length;n++){
        if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
    }
    if(_$(this.id+'_num_'+this.sliderIndex)){
        _$(this.id+'_num_'+this.sliderIndex).className='rotationNumberCurrent';
    }
    if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

    for(let i=0;i<this.photos.length;i++){
        if(this.mediaTypes[i]=='video'){
            Players.stop(this.id+'_box_'+i+'_player');
        }
    }

    this.show(0)
}

/**
 * 暂停
 */
Rotation.prototype.pause=function(){
    this.paused=true;

    if(this.sliderInterval) clearInterval(this.sliderInterval);
    if(this.sliderTimeout) clearTimeout(this.sliderTimeout);
}

/**
 * 滚动
 */
Rotation.prototype.slider=function(){
    this.sliderLength+=20;//每次滚动20px

    if(this.direction=='L'){
        if(this.sliderLength>this.width) this.sliderLength=this.width;

        _$(this.id+'_container').scrollLeft=(this.sliderIndex*this.width+this.sliderLength);

        if(this.sliderLength>=this.width){
            if(this.sliderInterval) clearInterval(this.sliderInterval);
            if(this.sliderTimeout) clearTimeout(this.sliderTimeout);

            this.sliderLength=0;
            this.sliderIndex++;

            if(this.sliderIndex==this.countOfMediaType()){//开始新的循环
                this.reset();
                return;
            }

            for(let n=0;n<this.photos.length;n++){
                if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
            }
            if(_$(this.id+'_num_'+this.sliderIndex)){
                _$(this.id+'_num_'+this.sliderIndex).className='rotationNumberCurrent';
            }
            if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

            //如果前一个媒体是视频，停止其播放
            if(this.sliderIndex>0 && this.videoShown(this.sliderIndex-1)){
                Players.stop(this.id+'_box_'+(this.sliderIndex-1)+'.player');
            }

            //定时显示下一个
            this.sliderTimeout=setTimeout("Rotations.doSlider('"+this.id+"')",this.speed);
        }
    }else{
        if(this.sliderLength>this.height) this.sliderLength=this.height;

        _$(this.id+'_container').scrollTop=(this.sliderIndex*this.height+this.sliderLength);

        if(this.sliderLength>=this.height){
            if(this.sliderInterval) clearInterval(this.sliderInterval);
            if(this.sliderTimeout) clearTimeout(this.sliderTimeout);

            this.sliderLength=0;
            this.sliderIndex++;

            if(this.sliderIndex==this.countOfMediaType()){//开始新的循环
                this.reset();
                return;
            }

            for(let n=0;n<this.photos.length;n++){
                if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
            }
            if(_$(this.id+'_num_'+this.sliderIndex)){
                _$(this.id+'_num_'+this.sliderIndex).className='rotationNumberCurrent';
            }
            if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

            //如果前一个媒体是视频，停止其播放
            if(this.sliderIndex>0 && this.videoShown(this.sliderIndex-1)){
                Players.stop(this.id+'_box_'+(this.sliderIndex-1)+'.player');
            }

            //定时显示下一个
            this.sliderTimeout=setTimeout("Rotations.doSlider('"+this.id+"')",this.speed);
        }
    }
}

Rotation.prototype.doSlider=function(){
    //暂停或当前显示的视频，则不自动滚动
    if(this.paused || this.videoShown()) return;

    //每10毫秒滚动
    this.sliderInterval=setInterval("Rotations.slider('"+this.id+"')",10);
}

/**
 * 管理所有轮播组件
 * @type {{openUrl: Rotations.openUrl, slider: Rotations.slider, current: null, doSlider: Rotations.doSlider, instances: any[], show: Rotations.show, switchType: Rotations.switchType, currentIndex: number}}
 */
let Rotations={
    instances:new Array(),

    //当前正在操作的实例
    current:null,
    currentIndex:0,

    /**
     * 打开url
     * @param url
     */
    openUrl:function(url){
        if(url.indexOf('/shopping/shop.htm')<0
            && url.indexOf('/shopping/zone.htm')<0
            && url.indexOf('/user/index.htm')<0){
            Layers.load(window,'',url,null, '', 0, null);
        }else{
            location.href=url;
        }
    },

    /**
     * 显示指定实例的第i个媒体
     * @param id
     * @param i
     */
    show:function(id,i){
        let rotation=this.instances[id];
        rotation.paused=rotation.inPlays[i];
        rotation.show(i);
    },

    /**
     * 滚动指定实例
     * @param id
     */
    slider:function(id){
        let rotation=this.instances[id];
        rotation.slider();
    },

    /**
     * 滚动指定实例到下一个
     * @param id
     */
    doSlider:function(id){
        let rotation=this.instances[id];
        rotation.doSlider();
    },

    /**
     * 切换指定实例的媒体显示类型
     * @param id
     * @param type
     * @private
     */
    switchType:function(id, type){
        let rotation=this.instances[id];
        rotation.paused=(type=='video');

        for(let i=0;i<rotation.photos.length;i++){
            if(rotation.mediaTypes[i]!=type){
                _$(id+'_box_'+i).style.display='none';
            }else{
                _$(id+'_box_'+i).style.display='';
            }
        }

        if(rotation.direction=='L'){
            _$(rotation.id).style.width=rotation.width*(rotation.countOfMediaType(type)+1)+'px';
        }else{
            _$(rotation.id).style.height=rotation.height*(rotation.countOfMediaType(type)+1)+'px';
        }

        let temp=_$cls('rotationSwitchCurrent');
        for(let i=0; temp && i<temp.length; i++) temp[i].className='rotationSwitch';
        _$(rotation.id+'_switch_'+type).className='rotationSwitchCurrent';

        rotation.currentMediaType=type;

        if(_$(id+'_rotationNumbersCount')) _$(id+'_rotationNumbersCount').innerHTML='1/'+rotation.countOfMediaType();

        rotation.reset();
    }
}
//图片轮播 end
