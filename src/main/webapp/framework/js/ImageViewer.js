let ImageViewer={
    layer:null,//在哪个Layer对象中操作（如指定了容器，则layer为null）
    container:null,//容器
    width:0,//工作区宽度
    height:0,//工作区高度
    WHRatio:1,//当前图片比例
    win:null,//关联窗口
    onClose:null,//关闭时回调
    current:0,//当前显示第几个
    allImages:[],//媒体列表
    sizes:[],//每个媒体的宽高比
    clickTime:0,//最近一次点击时间（两次点击很近时实现双击还原图片尺寸的功能）
    fromJMedia:false,//是否等待上传的图片（由JMedia加载、压缩）
    trim:false,//是否剪裁
    trimming:false,//是否正在剪裁（编辑）
    trimmingMask:'',//剪裁蒙版
    trimmingMedia:null,//剪裁媒体对象
    trimWHRatio:0,//剪裁区域宽高比
    trimWHRatioCustomized:true,//剪裁比例是否可调整
    initX:0,
    initY:0,
    initTop:0,
    initLeft:0,
    initWidth:0,
    initHeight:0,
    invokeCallback:false,//是否回调
    canClose:true,
    noTitle:false,
    zoomType:3,
    currentObjectId:null,//当前操作的对象（剪裁框或图）
    aigcEnabled:false,//是否启用AIGC

    /**
     * 媒体对象
     * @param name
     * @param url
     * @param cover
     * @param mediaId JMedia对象的ID
     * @param callback 回调
     * @param callbackTarget 调用回调方法的对象（不指定则为ImageViewer.win）
     * @param data 业务数据
     * @constructor
     */
    Media:function (name, url, cover, mediaId, callback, callbackTarget, data){
        this.name=Lang.convert(name);
        this.url=url;
        this.cover=cover;
        this.mediaId=mediaId;
        this.callback=callback;
        this.callbackTarget=callbackTarget;
        this.data=data;
    },

    /**
     * 打开指定相册
     * @param albumId
     * @param albumPwd
     * @param mediaType
     */
    openAlbum:function(albumId, albumPwd, mediaType){
        top.Dialog.open(-1,-1,-1,-1,null, null, window,'waiting');
        let ajax=new Ajax();
        ajax.mediaType=(mediaType?mediaType:'');
        ajax.send('GET',ImageViewer.doOpenAlbum,'/api/platform/cms/media/list?album_id='+albumId+'&album_pwd='+(albumPwd?albumPwd:'')+'&media_type='+(mediaType?mediaType:''));
    },

    /**
     * 打开用户相册
     * @param albumId
     * @param albumPwd
     * @param mediaType
     */
    openAlbumOfUser:function(uid, albumPwd, mediaType){
        top.Dialog.open(-1,-1,-1,-1,null, null, window,'waiting');
        let ajax=new Ajax();
        ajax.mediaType=(mediaType?mediaType:'');
        ajax.send('GET',ImageViewer.doOpenAlbum,'/api/platform/cms/media/list?uid='+uid+'&album_pwd='+(albumPwd?albumPwd:'')+'&media_type='+(mediaType?mediaType:''));
    },

    doOpenAlbum:function(ajax){
        if(ajax.getReadyState()==4&&ajax.getStatus()==200){
            top.Dialog.close();
            let resp=ajax.getResponseJson();
            if(resp.success){
                let list=resp.datas.photos;
                if(list.length==0){
                    Page.alert('I{ImageViewer,暂无相关照片或视频}', null, null, Dialog.MSG_TYPE_INFO);
                }else{
                    let _images=[];
                    for(let i=list.length-1; i>=0; i--) _images.push(new ImageViewer.Media('', list[i].display.mediaLink, list[i].display.imgLogo));
                    ImageViewer.open(window, _images);
                }
            }else if(resp.code=='non_login'){
                Auth.showNotLoginMessage();
            }else{
                Page.alert(resp.message, null, null, Dialog.MSG_TYPE_ERR);
            }
        }
    },

    /**
     * 是否已经打开
     * @returns {boolean}
     */
    isOpen:function(){
        return this.container!=null;
    },

    /**
     * 媒体是否已经存在与列表中
     * @param src
     */
    exists:function(src){
        for(let j=0;j<this.allImages.length;j++){
            if(this.allImages[j].url==src) return true;
        }
        return false;
    },

    /**
     * 打开
     * @param _win 关联窗口
     * @param _images 图片 [ImageViewer.Media]
     * @param _currentImgSrc 初始显示的媒体路径
     * @param _onClose 关闭时回调
     */
    open:function(_win,_images,_currentImgSrc,_onClose,_canClose,_noTitle,_zoomType,_container){
        if(top != window && (typeof top.ImageViewer) != 'undefined'){
            top.ImageViewer.trim=this.trim;
            top.ImageViewer.trimmingMask=this.trimmingMask;
            top.ImageViewer.trimWHRatioCustomized=this.trimWHRatioCustomized;
            top.ImageViewer.invokeCallback=this.invokeCallback;
            top.ImageViewer.aigcEnabled=this.aigcEnabled;

            this.close();

            top.ImageViewer.open(_win,_images,_currentImgSrc,_onClose,_canClose,_noTitle,_zoomType,_container);
            return;
        }

        if(this.isOpen()) this.close();

        this.win=_win?_win:window;
        this.onClose=_onClose?_onClose:null;
        this.canClose=(typeof _canClose)==='boolean'?_canClose:true;
        this.noTitle=(typeof _noTitle)==='boolean'?_noTitle:false;
        this.zoomType=(typeof _zoomType)==='number'?_zoomType:3;
        if(_container){
            this.container = (typeof _container)=='string' ? _$(_container) : _container;
        }

        if(_images && _images.length>0){//指定了要显示的媒体
            this.allImages=_images;

            //如果是剪裁模式，将url类型Media对象转换为JMedia类型
            if(this.trim && Str.isBlank(this.allImages[0].mediaId)){
                this.trimmingMedia=new JMedia(this.allImages[0].url, _$('ImageViewerCanvasWrapper'),6000,1, ImageViewer.trimPhotoLoaded);
                this.allImages=[];
            }else if(_currentImgSrc){
                for(let i=0;i<_images.length;i++){
                    //初始要显示第几个
                    if(_currentImgSrc.endsWith(_images[i].url) || _images[i].url.endsWith(_currentImgSrc)){
                        this.current=i;
                    }
                }
            }
        }else if(this.fromJMedia){//显示JMedia对象
            let jms=this.win.JMedias;
            for(let i in jms){
                if(jms[i] && jms[i].img){
                    this.allImages.push(new ImageViewer.Media('', null, null, jms[i].id));
                }
            }
        }else if(!this.trim && Str.isBlank(this.trimmingMask)){//自动获取页面内图片
            let _allImages=this.win.document.getElementsByTagName('img');
            for(let i=0; _allImages && i<_allImages.length; i++){
                let img=_allImages[i];
                if(!Utils.visible(img)) continue;

                let src=Utils.att(img,'_src');
                if(!src) src=img.src;
                if(!src || (src.indexOf('/i/')<0 && src.indexOf('/im/')<0))  continue;

                src=Str.replaceAll(src,'_logo','');
                src=Str.replaceAll(src,'_mini','');

                //图片的自定义属性alt（商品名）
                let alt=Utils.att(img,'alt');
                if(!alt) alt=Utils.att(img.parentNode.parentNode,'alt');

                //图片的自定义属性gid（商品ID）
                let gid=Utils.att(img,'gid');
                if(!gid) gid=Utils.att(img.parentNode.parentNode, 'gid');

                if(this.win.Page.hasPageFeature('onlyShowGoodsImages') && !gid){//仅显示商品图片
                    continue;
                }

                //已经存在
                if(this.exists(src)) continue;

                if(_currentImgSrc && (_currentImgSrc.endsWith(src) || src.endsWith(_currentImgSrc))){
                    this.current=this.allImages.length;
                }

                if(alt && gid) this.allImages.push(new ImageViewer.Media(alt, src, null, null, gid));
                else this.allImages.push(new ImageViewer.Media('', src, null, null));
            }
        }

        if(!this.trim && Str.isBlank(this.trimmingMask) && this.allImages.length==0){
            this.close(false);
            Page.alert('I{ImageViewer,没有可浏览的图片或视频}');
            return;
        }

        if(this.trim) this.currentObjectId='imageViewerTrimBox';

        //初始化组件
        this.init();

        //显示操作区
        this.showBtns();

        //加载
        this.load();

        //显示
        this.show();
    },

    /**
     * 初始化组件
     */
    init:function(){
        if(!this.container){
            this.layer=Layers.open(window,'','',null, '', 0, this.close, true, this.noTitle, this.canClose);
            this.container=this.layer.getContentElement();
        }
        if(this.layer) this.layer.setBtns('<div>&nbsp;</div>');

        this.width=W.elementWidth(this.container);
        this.height=W.elementHeight(this.container);
        this.WHRatio=this.width/this.height;

        let str=[];

        if(UserAgent.isMobile()){
            str.push('<div id="imageViewerTitle">I{ImageViewer,双指缩放，双击还原}</div>');
        }else{
            str.push('	<div id="imageViewerTitle">');
            str.push('		<div class="displayBlock iconfont icon-back" onclick="ImageViewer.right();"></div>');
            str.push('		<div class="displayBlock iconfont icon-more mL30" onclick="ImageViewer.left();"></div>');
            str.push('	</div>');
        }
        if(this.layer) this.layer.setTitle(str.join(''));

        if(this.layer) str=[];
        str.push('<div id="imageViewer">');
        str.push('	<div id="imageViewerContainer" style="width:'+this.width+'px; height:'+this.height+'px;"></div>');
        str.push('</div>');
        if(this.layer) this.layer.setContent(str.join(''));

        if(this.layer) str=[];
        str.push('<div id="imageViewerFooter">');
        str.push('	<div id="imageViewerMediaName"></div>');
        str.push('	<div id="imageViewerNumbers"></div>');
        str.push('</div>');
        if(this.layer) this.layer.setBtns(str.join(''));

        if(!this.layer) this.container.innerHTML=Lang.convert(str.join(''));

        str=[];
        if(!Str.isBlank(this.trimmingMask)){//指定了蒙版
            str.push('<div id="imageViewerTrimBox" class="imageViewerTrimBox" style="border:none !important; z-index:'+W.getMaxZIndex()+' !important;">');
            str.push('<img id="imageViewerTrimMask" _src="'+this.trimmingMask+'" style="display: none;"/>')
        }else{
            str.push('<div id="imageViewerTrimBox" class="imageViewerTrimBox" style="z-index:'+W.getMaxZIndex()+' !important;">');
        }
        str.push('</div>');
        document.body.insertAdjacentHTML('afterbegin', str.join(''));
        str=null;
        delete str;

        if(!_$('ImageViewerCanvasWrapper')){
            let cavasContainer = document.createElement('div');
            cavasContainer.id='ImageViewerCanvasWrapper';
            cavasContainer.className='cavasWrapper';
            document.body.appendChild(cavasContainer);
        }
    },

    /**
     * 加载
     */
    load:function(){
        let htm=[];
        for(let i=0;i<this.allImages.length;i++){
            let imgSrc=this.allImages[i].url;

            if(Str.endsWith(imgSrc,'.mp4',true)
                ||Str.endsWith(imgSrc,'.mov',true)
                ||Str.endsWith(imgSrc,'.3gp',true)){
                htm.push('<div id="imageViewerImage_'+i+'" class="imageViewerImage" style="width:'+this.width+'px; height:'+this.height+'px; display: none;">');
                htm.push('	<video id="imageViewerImage_'+i+'_player" preload="auto" playsinline webkit-playsinline></video>');
                htm.push('</div>');
            }else if(Str.endsWith(imgSrc,'.mp3',true)
                ||Str.endsWith(imgSrc,'.amr',true)
                ||Str.endsWith(imgSrc,'.ogg',true)
                ||Str.endsWith(imgSrc,'.wav',true)){
                htm.push('<div id="imageViewerImage_'+i+'" class="imageViewerImage" style="width:'+this.width+'px; height:'+this.height+'px; display: none;">');
                htm.push('	<audio id="imageViewerImage_'+i+'_player" autoplay="false" controls><source src="'+imgSrc+'" type="'+JMediaUtil.getAudioType(imgSrc)+'"></audio>');
                htm.push('</div>');
            }else{
                htm.push('<div id="imageViewerImage_'+i+'" class="imageViewerImage" style="width:'+this.width+'px; height:'+this.height+'px; display: none;">');
                htm.push('	<img id="imageViewerImage_'+i+'_img'+'" _src="'+imgSrc+'" style="display:none;"/>');
                htm.push('</div>');
            }
        }

        _$('imageViewerContainer').innerHTML=htm.join('');
        htm=null;
        delete htm;

        _$('imageViewerNumbers').innerHTML='1/'+this.allImages.length;
        for(let i=0;i<this.allImages.length;i++){
            if(Str.isBlank(this.allImages[i].mediaId)) continue;
            Utils.setAtt(_$('imageViewerImage_'+i+'_img'),'_src',this.getMedia(this.allImages[i].mediaId).getData());
        }

        //初始化视频播放器
        for(let i=0;i<this.allImages.length;i++){
            let imgSrc=this.allImages[i].url;
            let cover=this.allImages[i].cover;
            if(Str.endsWith(imgSrc,'.mp4',true)
                ||Str.endsWith(imgSrc,'.mov',true)
                ||Str.endsWith(imgSrc,'.3gp',true)){
                Players.addPlayer('imageViewerImage_'+i+'_player',
                    imgSrc,
                    false,
                    this.width,
                    this.height,
                    false,
                    this.allImages.length==1,
                    true,
                    (cover?cover:''),
                    false,
                    'H');

                Players.setMaxHeight('imageViewerImage_'+i+'_player', this.height);
            }
        }
        Players.initPlayers();

        //初始化图片
        for(let i=0;i<this.allImages.length;i++){
            let imgSrc=this.allImages[i].url;
            if(Str.endsWith(imgSrc,'.mp4',true)
                ||Str.endsWith(imgSrc,'.mov',true)
                ||Str.endsWith(imgSrc,'.3gp',true)
                ||Str.endsWith(imgSrc,'.mp3',true)
                ||Str.endsWith(imgSrc,'.amr',true)
                ||Str.endsWith(imgSrc,'.ogg',true)
                ||Str.endsWith(imgSrc,'.wav',true)){
                continue;
            }

            IMG.reset('imageViewerImage_'+i+'_img');
            IMG.adjust('imageViewerImage_'+i+'_img',
                null,
                this.zoomType,
                this.width,
                this.height,
                true,
                false,
                null,
                null,
                ImageViewer.trimShow,
                true,
                true,
                false);
        }

        if(_$('imageViewerTrimMask')){
            _$('imageViewerTrimMask').style.display='none';
            IMG.reset('imageViewerTrimMask');
            IMG.adjust('imageViewerTrimMask',
                null,
                3,
                this.width - 100,
                this.height - 100,
                false,
                false,
                null,
                null,
                ImageViewer.trimMaskLoaded,
                true,
                false,
                false);
        }

        new Touch(_$('imageViewerContainer'),
            10,
            this.start,
            this.moving,
            this.up,
            this.down,
            this.left,
            this.right,
            null,
            this.click,
            this.zoomIn,
            this.zoomOut,
            this.longPress);

        new Touch(_$('imageViewerTrimBox'),
            10,
            this.start,
            this.moving,
            this.up,
            this.down,
            this.left,
            this.right,
            null,
            this.click,
            this.zoomIn,
            this.zoomOut,
            this.longPress);
    },

    /**
     * 操作区
     */
    showBtns:function (){
        if(this.trim){//允许编辑
            let s=[];
            s.push('<div id="imageViewerTrimPickFile" class="fl mT10 mR10">');
            s.push('<div class="fileInputWithSkin" style="width:90px;">');
            s.push('    <div class="skin">');
            s.push('        <div class="aBtnWithIcon" style="width:90px;">');
            s.push('            <div class="aIcon iconfont icon-piclight"></div>');
            s.push('            <div class="aText" id="fileInputWithSkinText_image">I{ImageViewer,选择图片}</div>');
            s.push('        </div>');
            s.push('    </div>');
            s.push('    <div class="file">');
            s.push('        <input type="file" id="trimPhoto" name="trimPhoto" accept="image/*" onchange="ImageViewer.trimPhotoPicked();" single/>');
            s.push('    </div>');
            s.push('</div>');
            s.push('</div>');

            if(this.trim && this.aigcEnabled) {
                s.push('<div id="imageViewerAigc" class="fl mR10" onclick="AIGC.showAIGCDialog(null, ImageViewer.onAIGC, this.win);"><div class="font24px iconfont icon-a-Component1"></div></div>');
            }

            s.push('<div id="imageViewerTrimStart" class="btnH30 w60 fl mT10 mR10 btnBgGreen hidden" onclick="ImageViewer.trimShow();">I{ImageViewer,编辑}</div>');

            s.push('<div id="imageViewerTrimWHRatio" class="fl mR10" style="display:none;"><select id="imageViewerTrimWHRatioSelector" onchange="ImageViewer.setTrimWHRatio();">');
            s.push('    <option value="0">I{ImageViewer,宽高比例}</option>');
            s.push('    <option value="1">1:1</option>');
            s.push('    <option value="2/1">2:1</option>');
            s.push('    <option value="1/2">1:2</option>');
            s.push('    <option value="4/3">4:3</option>');
            s.push('    <option value="3/4">3:4</option>');
            s.push('    <option value="16/9">16:9</option>');
            s.push('    <option value="9/16">9:16</option>');
            s.push('</select></div>');

            s.push('<div id="imageViewerTrimZoomIn" class="fl mR10" style="display:none;" onclick="ImageViewer.zoomObject(0.01);"><div class="font24px iconfont icon-fangda"></div></div>');
            s.push('<div id="imageViewerTrimZoomOut" class="fl mR10" style="display:none;" onclick="ImageViewer.zoomObject(-0.01);"><div class="font24px iconfont icon-suoxiao"></div></div>');

            s.push('<div id="imageViewerTrimRotate" class="fl mR10" style="display:none;" onclick="ImageViewer.trimRotate();"><div class="font24px iconfont icon-shunshizhenxuanzhuan"></div></div>');
            s.push('<div id="imageViewerTrimDone" class="fl mR10" style="display:none;" onclick="ImageViewer.trimDone();"><div class="font24px iconfont icon-jianqie1"></div></div>');
            s.push('<div id="imageViewerTrimOriginal" class="fl mR10" style="display:none;" onclick="ImageViewer.trimShowOriginal();"><div class="font24px iconfont icon-huanyuan"></div></div>');
            s.push('<div id="imageViewerTrimFinish" class="fl mR10" style="display:none;" onclick="ImageViewer.trimCancel(); ImageViewer.close(true);"><div class="font24px iconfont icon-chenggong1 green"></div></div>');
            _$('imageViewerMediaName').innerHTML=Lang.convert(s.join(''));
            s=null;
            delete s;
        }else if(this.allImages.length>0){
            let imgBizName=this.allImages[this.current].name;
            _$('imageViewerMediaName').innerHTML=imgBizName;
        }
    },

    /**
     * 设置额外按钮
     * @param btns
     */
    setBtns:function(btns){
        if(!btns) return;
        if(Array.isArray(btns)) btns=btns.join('');
        _$('imageViewerFooter').insertAdjacentHTML('afterbegin', Lang.convert(btns));
    },


    /**
     * 显示
     */
    show:function(){
        //停止所有视频、音频播放
        try{
            for(let i=0;i<this.allImages.length;i++){
                let player=Players.getPlayer('imageViewerImage_'+i+'_player');
                if(player){//视频
                    Players.stop(player);
                }else if(_$('imageViewerImage_'+i+'_player')){//音频
                    _$('imageViewerImage_'+i+'_player').pause();
                }
            }
        }catch(e){}

        let current=this.current;
        if(this.allImages.length>0) {
            for (let i = 0; i < this.allImages.length; i++) {
                _$('imageViewerImage_' + i).style.display = 'none';
            }
            _$('imageViewerImage_' + current).style.display = '';
        }
        _$('imageViewerNumbers').innerHTML=(current+1)+'/'+this.allImages.length;

        //回调
        if(this.allImages.length>0 && this.allImages[current].callback){
            this.allImages[current].callback.call(this.allImages[current].callbackTarget ? this.allImages[current].callbackTarget : this.win,
                current,
                this.allImages[current].data);
        }
    },

    /**
     * 蒙版加载完毕（设定剪裁框固定比例）
     */
    trimMaskLoaded:function (id,newW,newH){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.trimMaskLoaded(id,newW,newH);
            return;
        }

        //指定剪裁区比例，禁止手动设定
        if(_$('imageViewerTrimWHRatio')) _$('imageViewerTrimWHRatio').style.display='none';

        if(newW && newH) this.trimWHRatio=newW/newH;
        this.setTrimWHRatio(this.trimWHRatio, newW, newH);
    },

    /**
     * 蒙版加载完毕（设定剪裁框固定比例）
     */
    getMedia:function (mediaId){
        if(this.win.JMedias[mediaId]) return this.win.JMedias[mediaId];
        else return JMedias[mediaId];
    },

    /**
     * 重置（编辑图片后）
     */
    reset:function(){
        //停止所有视频、音频播放
        try{
            for(let i=0;i<this.allImages.length;i++){
                let player=Players.getPlayer('imageViewerImage_'+i+'_player');
                if(player){//视频
                    Players.stop(player);
                }else if(_$('imageViewerImage_'+i+'_player')){//音频
                    _$('imageViewerImage_'+i+'_player').pause();
                }
            }
        }catch(e){}

        let cimg=_$('imageViewerImage_'+this.current+'_img');
        let cimgContainer=_$('imageViewerImage_'+this.current);
        if(!cimg) return;
        if(!this.sizes[cimg.id]){
            this.sizes[cimg.id]=[cimg.width,cimg.height];
        }

        let maxWidth=this.width;
        let maxHeight=this.height;

        let sizes=this.sizes[cimg.id];
        let ratio=sizes[0]/sizes[1];
        if(ratio>maxWidth/maxHeight){
            cimg.width=maxWidth;
            cimg.height=Math.floor(cimg.width/ratio);
        }else{
            cimg.height=maxHeight;
            cimg.width=Math.floor(cimg.height*ratio);
        }
        cimgContainer.style.paddingLeft='0px';
        cimgContainer.style.paddingRight='0px';
        cimgContainer.style.paddingTop='0px';
        cimgContainer.style.paddingBottom='0px';
        cimgContainer.scrollLeft=0;
        cimgContainer.scrollTop=0;
    },

    /**
     * 清除
     */
    clear:function(clearTrimmingMedia){
        if(clearTrimmingMedia && this.trimmingMedia){
            this.trimmingMedia.clear();
            this.trimmingMedia=null;
        }
        this.allImages=[];
        this.sizes=[];
        this.current=0;
        _$('imageViewerContainer').innerHTML='';
    },

    /**
     * 关闭
     * @param _invokeCallback
     */
    close:function(_invokeCallback){//关闭
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.close(_invokeCallback);
            return;
        }

        let trimBox=_$('imageViewerTrimBox');
        if(trimBox) trimBox.parentNode.removeChild(trimBox);

        if((_invokeCallback || this.invokeCallback) && this.onClose){
            try{
                if(this.trimmingMedia){
                    this.onClose.call(this.win?this.win:window, this.trimmingMedia, this.trimmingMedia?this.trimmingMedia.dataOriginal:null, this.trimmingMedia?this.trimmingMedia.data:null);
                }else{
                    let cimg = this.allImages[this.current];
                    if(cimg && !Str.isBlank(cimg.mediaId) && JMedias[cimg.mediaId]){
                        this.onClose.call(this.win?this.win:window, JMedias[cimg.mediaId], JMedias[cimg.mediaId].dataOriginal, JMedias[cimg.mediaId].data);
                    }else{
                        this.onClose.call(this.win?this.win:window);
                    }
                }
            }catch(e){}
        }

        this.container=null;
        this.width=0;
        this.height=0;
        this.WHRatio=1;
        this.currentObjectId=null;
        this.aigcEnabled=false;
        this.win=null;//关联窗口
        this.onClose=null;//关闭时回调
        this.current=0;//当前显示第几个
        this.allImages=[];//每个媒体的路径
        this.sizes=[];//每个媒体的宽高比
        this.clickTime=0;//最近一次点击时间（两次点击很近时实现双击还原图片尺寸的功能）
        this.fromJMedia=false;//是否等待上传的图片（由JMedia加载、压缩）
        this.trim=false;//是否剪裁
        this.trimming=false;//是否正在剪裁
        this.trimmingMask=null;
        this.trimWHRatio=0;
        this.trimWHRatioCustomized=true;
        this.initX=0;
        this.initY=0;
        this.initTop=0;
        this.initLeft=0;
        this.initWidth=0;
        this.initHeight=0;
        this.invokeCallback=false;//是否回调
        if(this.trimmingMedia){
            this.trimmingMedia.clear();
            this.trimmingMedia=null;
        }

        if(this.layer){
            this.layer.close();
            this.layer=null;
        }
    },

    /**
     * 编辑图片时宽高比（0表示不限制）
     * @returns {number}
     * @constructor
     */
    getTrimWHRatio:function(){
        if(!_$('imageViewerTrimWHRatioSelector')) return 0;
        let ratio=_$('imageViewerTrimWHRatioSelector').value;
        if(ratio=='0') return 0;
        if(ratio.indexOf('/')>0){
            let a=ratio.substring(0, ratio.indexOf('/'));
            let b=ratio.substring(ratio.indexOf('/')+1);
            return a/b;
        }else{
            return ratio*1;
        }
    },

    /**
     * 设置剪裁框宽高比
     * @param ratio
     * @param maxWidth
     * @param maxHeight
     * @param doNotMove
     */
    setTrimWHRatio:function(ratio, maxWidth, maxHeight, doNotMove){
        if(!ratio) ratio= this.getTrimWHRatio();
        this.trimWHRatio = ratio;
        if(ratio==0) return;

        if(!maxWidth) maxWidth=this.width;
        if(!maxHeight) maxHeight=this.height;


        let cimg=_$('imageViewerImage_'+this.current+'_img');

        let trimBoxInitWidth=0;
        let trimBoxInitHeight=0;
        let imageRatio=this.WHRatio;
        if(!cimg){
            trimBoxInitWidth=maxWidth;
            trimBoxInitHeight=maxHeight;
        }else{
            trimBoxInitWidth=Math.min(maxWidth, cimg.width);
            trimBoxInitHeight=Math.min(maxHeight, cimg.height);
            imageRatio=cimg.width/cimg.height;
        }

        if(this.trimWHRatio>0){
            if(this.trimWHRatio > imageRatio){//剪裁框宽度方向顶边
                trimBoxInitHeight = trimBoxInitWidth/this.trimWHRatio;
            }else{//剪裁框高度方向顶边
                trimBoxInitWidth = trimBoxInitHeight*this.trimWHRatio;
            }
        }

        _$('imageViewerTrimBox').style.width=trimBoxInitWidth+'px';
        _$('imageViewerTrimBox').style.height=trimBoxInitHeight+'px';

        if(_$('imageViewerTrimMask')){
            _$('imageViewerTrimMask').width=trimBoxInitWidth;
            _$('imageViewerTrimMask').height=trimBoxInitHeight;
        }

        let trimBoxLeft=Math.floor((this.width-trimBoxInitWidth)/2);
        let trimBoxTop=Math.floor((this.height-trimBoxInitHeight)/2)+54;
        if(!doNotMove){
            _$('imageViewerTrimBox').style.left=trimBoxLeft+'px';
            _$('imageViewerTrimBox').style.top=trimBoxTop+'px';
        }
        _$('imageViewerTrimBox').style.visibility='visible';
    },

    getTrimBoxLeftMin:function (){
        let cimg=_$('imageViewerImage_'+this.current+'_img');
        let cimgContainer=_$('imageViewerImage_'+this.current);

        if(!cimg || cimgContainer.scrollLeft>0) return 0;
        return W.elementLeft(cimg, false);
    },

    getTrimBoxLeftMax:function (){
        return this.width - W.elementWidth('imageViewerTrimBox');
    },

    getTrimBoxTopMin:function (){
        let cimg=_$('imageViewerImage_'+this.current+'_img');
        let cimgContainer=_$('imageViewerImage_'+this.current);

        if(!cimg || cimgContainer.scrollTop>0) return 54;
        return W.elementTop(cimg, false) + 54;
    },

    getTrimBoxTopMax:function (){
        return this.height - W.elementHeight('imageViewerTrimBox') + 54;
    },

    /**
     * 选择图片
     */
    trimPhotoPicked:function (){
        this.reset();
        this.clear(true);
        if(_$('trimPhoto').files && _$('trimPhoto').files.length>0){
            this.trimmingMedia=new JMedia(_$('trimPhoto').files[0], _$('ImageViewerCanvasWrapper'),6000,1, ImageViewer.trimPhotoLoaded);
        }
    },

    /**
     * 改变图片
     */
    trimPhotoChange:function (imageUrl){
        this.reset();
        this.clear(true);
        this.trimmingMedia=new JMedia(imageUrl, _$('ImageViewerCanvasWrapper'),6000,1, ImageViewer.trimPhotoLoaded);
    },

    /**
     * 图片加载完毕
     */
    trimPhotoLoaded:function (img, imgTrim, id){
        ImageViewer.reset();
        ImageViewer.clear(false);
        ImageViewer.allImages.push(new ImageViewer.Media('', null, null, id));
        ImageViewer.fromJMedia=true;
        ImageViewer.load();
        ImageViewer.show();
        ImageViewer.trimShow();
    },

    /**
     * 显示编辑相关内容
     */
    trimShow:function(){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.trimShow();
            return;
        }

        if(!this.trim) return;
        if(this.allImages.length==0){
            Page.alert('I{ImageViewer,请先选中一张图片}', null, null, Dialog.MSG_TYPE_INFO);
            return;
        }

        //设置剪裁框大小位置
        this.setTrimWHRatio(this.trimWHRatio);

        this.trimming=true;
        _$('imageViewerTrimBox').style.visibility='visible';
        _$('imageViewerTrimStart').style.display='none';
        _$('imageViewerTrimDone').style.display='';

        if(Str.isBlank(this.trimmingMask) && this.trimWHRatioCustomized){
            _$('imageViewerTrimWHRatio').style.display='';
        }

        _$('imageViewerTrimRotate').style.display='';
        _$('imageViewerTrimFinish').style.display='';
        _$('imageViewerTrimOriginal').style.display='';
        if(_$('imageViewerTrimZoomIn')) _$('imageViewerTrimZoomIn').style.display='';
        if(_$('imageViewerTrimZoomOut')) _$('imageViewerTrimZoomOut').style.display='';
        if(_$('imageViewerAigc')) _$('imageViewerAigc').style.display='';
    },

    /**
     * 取消编辑
     */
    trimCancel:function(){
        this.trimming=false;

        _$('imageViewerTrimBox').style.visibility='hidden';
        _$('imageViewerTrimDone').style.display='none';
        _$('imageViewerTrimWHRatio').style.display='none';
        _$('imageViewerTrimRotate').style.display='none';
        _$('imageViewerTrimFinish').style.display='none';
        _$('imageViewerTrimOriginal').style.display='none';
        if(_$('imageViewerTrimZoomIn')) _$('imageViewerTrimZoomIn').style.display='none';
        if(_$('imageViewerTrimZoomOut')) _$('imageViewerTrimZoomOut').style.display='none';
        _$('imageViewerTrimStart').style.display='';
    },

    /**
     * 剪裁框放大缩小
     */
    trimBoxZoom:function (movement){
        let cimg=_$('imageViewerImage_'+this.current+'_img');
        if(!cimg) return;

        if(this.trimming){
            this.initWidth=W.elementWidth(_$('imageViewerTrimBox'));
            this.initHeight=W.elementHeight(_$('imageViewerTrimBox'));
        }else{
            this.initWidth=cimg.width;
            this.initHeight=cimg.height;
        }

        //X方向缩放
        let width=Math.min(cimg.width-20, this.initWidth+movement);
        if(width<10) width=10;

        //Y方向缩放
        let height=Math.min(cimg.height-20, this.initHeight+movement);
        if(height<10) height=10;

        this.setTrimWHRatio(this.trimWHRatio, width, height);
    },

    /**
     * 完成编辑
     */
    trimDone:function(){
        if(!this.trimming){
            this.close();
            return;
        }

        let i=this.current;

        let cimg=_$('imageViewerImage_'+i+'_img');
        let cimgContainer=_$('imageViewerImage_'+i);

        let trimLeftLength=cimgContainer.scrollLeft + W.elementLeft(_$('imageViewerTrimBox'), false) - W.elementLeft(cimg);
        if(trimLeftLength<0) trimLeftLength=0;

        let trimRightLength=cimg.width - trimLeftLength - W.elementWidth(_$('imageViewerTrimBox'));
        if(trimRightLength<0) trimRightLength=0;

        let trimTopLength=cimgContainer.scrollTop + W.elementTop(_$('imageViewerTrimBox'), false) - W.elementTop(cimg); - 54;
        if(trimTopLength<0) trimTopLength=0;

        let trimBottomLength=cimg.height - trimTopLength - W.elementHeight(_$('imageViewerTrimBox'));
        if(trimBottomLength<0) trimBottomLength=0;

        let jmid=this.allImages[i].mediaId;
        let jm=this.getMedia(jmid);
        if(jm){
            jm.trimLeftRatio=trimLeftLength/cimg.width;//左边裁剪比率
            jm.trimRightRatio=trimRightLength/cimg.width;//右边裁剪比率
            jm.trimTopRatio=trimTopLength/cimg.height;//顶部裁剪比率
            jm.trimBottomRatio=trimBottomLength/cimg.height;//底部裁剪比率
            jm.trim(jmid, this.trimWHRatio);

            this.trimShowCanvas();
        }
    },

    /**
     * 顺时针旋转90度
     */
    trimRotate:function(){
        let i=this.current;
        let jmid=this.allImages[i].mediaId;
        let jm=this.getMedia(jmid);
        if(jm){
            jm.rotate(jmid,90);
            this.trimShowCanvas();
        }
    },

    /**
     * 从JMedia获取裁剪后的图片数据再显示
     */
    trimShowCanvas:function(){
        let i=this.current;
        IMG.reset('imageViewerImage_'+i+'_img');
        Utils.delAtt(_$('imageViewerImage_'+i+'_img'),'src');
        Utils.setAtt(_$('imageViewerImage_'+i+'_img'),'_src',this.getMedia(this.allImages[i].mediaId).getData());

        IMG.adjust('imageViewerImage_'+i+'_img',
            null,
            3,
            this.width,
            this.height,
            true,
            false,
            null,
            null,
            ImageViewer.trimShow,
            true,
            true,
            false);

        this.sizes['imageViewerImage_'+i+'_img']=null;//需要重新获取图片尺寸
    },

    /**
     * 显示原图
     */
    trimShowOriginal:function(){
        let i=this.current;

        let jmid=this.allImages[i].mediaId;
        let jm=this.getMedia(jmid);
        if(jm){
            jm.trimLeftRatio=0;
            jm.trimRightRatio=0;
            jm.trimTopRatio=0;
            jm.trimBottomRatio=0;
            jm.rotates=0;
            jm.zoom(jmid);
        }

        IMG.reset('imageViewerImage_'+i+'_img');
        Utils.delAtt(_$('imageViewerImage_'+i+'_img'),'src');
        Utils.setAtt(_$('imageViewerImage_'+i+'_img'),'_src',this.getMedia(this.allImages[i].mediaId).getData());

        IMG.adjust('imageViewerImage_'+i+'_img',
            null,
            this.zoomType,
            this.width,
            this.height,
            true,
            false,
            null,
            null,
            null,
            true,
            true,
            false);

        this.sizes['imageViewerImage_'+i+'_img']=null;//需要重新获取图片尺寸
    },

    /**
     *
     * @param event
     * @param _touch
     */
    start:function(event, _touch){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.start(event, _touch, true);
            return;
        }
        
        this.currentObjectId=_touch.obj.id;

        this.initX=_touch.initScreenX;
        this.initY=_touch.initScreenY;
        if(_touch.obj.id=='imageViewerTrimBox'){//操作剪裁框
            let _top=_$('imageViewerTrimBox').style.top;
            _top=Str.replaceAll(_top.toLowerCase(),'px','')*1;
            this.initTop=_top;

            let left=_$('imageViewerTrimBox').style.left;

            left=Str.replaceAll(left.toLowerCase(),'px','')*1;
            this.initLeft=left;

            this.initWidth=W.elementWidth(_$('imageViewerTrimBox'));
            this.initHeight=W.elementHeight(_$('imageViewerTrimBox'));
        }else{
            let cimg=_$('imageViewerImage_'+this.current+'_img');
            let cimgContainer=_$('imageViewerImage_'+this.current);
            if(!cimgContainer) return;

            this.initTop=cimgContainer.scrollTop;
            this.initLeft=cimgContainer.scrollLeft;
            if(cimg){
                this.initWidth=cimg.width;
                this.initHeight=cimg.height;
            }
        }
        this.distanceOfTwoPoint=0;
        this.distanceOfTwoPointX=0;
        this.distanceOfTwoPointY=0;
    },

    /**
     *
     * @param event
     * @param _touch
     */
    moving:function(event, _touch){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.moving(event, _touch);
            return;
        }

        let cimg=_$('imageViewerImage_'+this.current+'_img');
        if(_touch.obj.id=='imageViewerTrimBox'){//剪裁模式
            if(!this.trim) return;
            if(_touch.distanceOfTwoPoint==0){//一个手指
                //X方向移动距离
                let movement= Math.floor(_touch.screenX-this.initX);
                let left=this.initLeft+movement;

                let minLeft=this.getTrimBoxLeftMin();
                let maxLeft=this.getTrimBoxLeftMax();
                if(left<minLeft){
                    left=minLeft;
                }else if(left > maxLeft){
                    left=maxLeft;
                }
                _$('imageViewerTrimBox').style.left=left+'px';
                //X方向移动距离 end

                //Y方向移动距离
                movement=Math.floor(_touch.screenY-this.initY);
                let _top=this.initTop+movement;

                let minTop=this.getTrimBoxTopMin();
                let maxTop=this.getTrimBoxTopMax();
                if(_top<minTop){
                    _top=minTop;
                }else if(_top>maxTop){
                    _top=maxTop;
                }
                _$('imageViewerTrimBox').style.top=_top+'px';
                //Y方向移动距离 end

                this.initX=_touch.initScreenX;
                this.initY=_touch.initScreenY;
            }else{//两个手指
                //X方向缩放
                if(this.distanceOfTwoPointX==0) this.distanceOfTwoPointX=_touch.distanceOfTwoPointX;
                let movement=_touch.distanceOfTwoPointX-this.distanceOfTwoPointX;
                let width= Math.min(cimg ? cimg.width : this.width, this.initWidth + movement);
                if(width<10) width=10;

                //Y方向缩放
                if(this.distanceOfTwoPointY==0) this.distanceOfTwoPointY=_touch.distanceOfTwoPointY;
                movement=_touch.distanceOfTwoPointY-this.distanceOfTwoPointY;
                let height= Math.min(cimg ? cimg.height : this.height, this.initHeight+movement);
                if(height<10) height=10;

                this.setTrimWHRatio(this.trimWHRatio, width, height);
            }
            return;
        }

        if(!cimg) return;
        let cimgContainer=_$('imageViewerImage_'+this.current);

        let maxWidth=this.width;
        let maxHeight=this.height;

        if((cimg.width>maxWidth || cimg.height>maxHeight)
            && _touch.distanceOfTwoPoint==0){//图片已被放大，且只是一个手指
            let scrollLeft=this.initLeft;
            let scrollTop=this.initTop;

            let scrollLeftMax=cimg.width-maxWidth;//当前图片最大可偏移水平距离
            if(scrollLeftMax<0) scrollLeftMax=0;

            let scrollTopMax=cimg.height-maxHeight;//当前图片最大可偏移垂直距离
            if(scrollTopMax<0) scrollTopMax=0;

            //X方向移动
            let movement=Math.floor(_touch.screenX-this.initX);
            scrollLeft-=movement;
            if(scrollLeft<0) scrollLeft=0;
            if(scrollLeft>scrollLeftMax) scrollLeft=scrollLeftMax;
            cimgContainer.scrollLeft=Math.floor(scrollLeft);

            //Y方向移动
            movement=Math.floor(_touch.screenY-this.initY);
            scrollTop-=movement;
            if(scrollTop<0) scrollTop=0;
            if(scrollTop>scrollTopMax) scrollTop=scrollTopMax;
            cimgContainer.scrollTop=Math.floor(scrollTop);
            return;
        }

        if(_touch.distanceOfTwoPoint>0){//两个手指
            if(this.distanceOfTwoPoint==0) this.distanceOfTwoPoint=_touch.distanceOfTwoPoint;
            let movement=_touch.distanceOfTwoPoint-this.distanceOfTwoPoint;
            this.zoomImage(cimg, cimgContainer, movement);
        }
    },

    /**
     * 放大缩小图片
     * @param cimg
     * @param cimgContainer
     * @param movement
     */
    zoomImage:function (cimg, cimgContainer, movement){
        if(!this.sizes[cimg.id]){
            this.sizes[cimg.id]=[cimg.width, cimg.height];
        }

        let maxWidth=this.width;
        let maxHeight=this.height;

        let sizes=this.sizes[cimg.id];
        let ratio= sizes[0]/sizes[1];

        let widthNew=cimg.width;
        let heightNew=cimg.height;
        if(ratio>maxWidth/maxHeight){
            widthNew=(widthNew+movement);
            if(widthNew < maxWidth) widthNew=maxWidth;

            heightNew=Math.floor(widthNew/ratio);
        }else{
            heightNew=(heightNew+movement);
            if(heightNew < maxHeight) heightNew=maxHeight;

            widthNew=Math.floor(heightNew*ratio);
        }

        cimg.width=widthNew;
        cimg.height=heightNew;

        if(cimg.width<=maxWidth) cimgContainer.scrollLeft=0;
        if(cimg.height<=maxHeight) cimgContainer.scrollTop=0;
    },

    /**
     * 图片缩放
     * @param amount 绝对值小于表示百分比，大于等于1表示像素数
     */
    zoomObject:function(amount){
        if(amount==0) return;

        if(this.currentObjectId=='imageViewerTrimBox'){
            let w=W.elementWidth('imageViewerTrimBox');
            let h=W.elementHeight('imageViewerTrimBox');

            if(Math.abs(amount) < 1){
                amount = amount*W.elementWidth('imageViewerTrimBox');//转换为宽度的百分比
            }

            w+=amount;
            h=w/this.trimWHRatio;

            this.setTrimWHRatio(this.trimWHRatio, w, h, true);
            return;
        }

        let cimg=_$('imageViewerImage_'+this.current+'_img');
        if(!cimg) return;
        let cimgContainer=_$('imageViewerImage_'+this.current);

        if(Math.abs(amount) < 1){
            amount = amount*cimg.width;//转换为宽度的百分比
        }
        this.zoomImage(cimg, cimgContainer, amount);
    },

    /**
     * 左翻
     * @param event
     * @param _touch
     */
    left:function(event,_touch){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.left(event, _touch);
            return;
        }

        //处于剪裁模式，不对图片进行缩放、移动
        if(this.trimming) return;

        let cimg=_$('imageViewerImage_'+this.current+'_img');
        if(cimg){
            let maxWidth=this.width;
            let maxHeight=this.height;
            if(cimg.width>maxWidth+10 || cimg.height>maxHeight+10){//图片已被放大
                return;
            }
        }else{
            this.reset();
        }
        let i=this.current+1;
        if(i>this.allImages.length-1) i=0;
        this.current=i;

        this.show();
    },

    /**
     * 右翻
     * @param event
     * @param _touch
     */
    right:function(event,_touch){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.right(event, _touch);
            return;
        }

        //处于剪裁模式，不对图片进行缩放、移动
        if(this.trimming) return;

        let cimg=_$('imageViewerImage_'+this.current+'_img');
        if(cimg){
            let maxWidth=this.width;
            let maxHeight=this.height;
            if(cimg.width>maxWidth+10 || cimg.height>maxHeight+10){//图片已被放大
                return;
            }
        }else{
            this.reset();
        }

        let i=this.current-1;
        if(i<0) i=this.allImages.length-1;
        this.current=i;

        this.show();
    },

    /**
     * 点击
     * @param event
     * @param _touch
     */
    click:function(event,_touch){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.click(event, _touch);
            return;
        }

        //处于剪裁模式，不对图片进行缩放、移动
        if(this.trimming) return;

        let cimg=_$('imageViewerImage_'+this.current+'_img');
        if(!cimg) return;

        let maxWidth=this.width;
        let maxHeight=this.height;
        if(cimg.width<=maxWidth && cimg.height<=maxHeight){//未放大
            let media = this.allImages[this.current];
            if(media.data && media.data.link){
                let _link = media.data.link;
                let _linkOpenType = media.data.linkOpenType;
                if(!_linkOpenType) _linkOpenType='newLayer';

                if(_linkOpenType==='inLayer'){
                    ImageViewer.layer.load(window, '', _link);
                }else if(_linkOpenType==='newLayer'){
                    Layers.open(window, '', _link);
                }else if(_linkOpenType==='newWindow'){
                    window.open(_link);
                }else if(_linkOpenType==='topWindow'){
                    top.location.href=_link;
                }else{
                    location.href=_link;
                }
            }
        }

        let n=(new Date()).getTime();
        if(n-this.clickTime<500 && this.sizes[cimg.id]){//双击重置
            this.reset();
        }
        this.clickTime=n;
    },

    up:function(event,_touch){},

    down:function(event,_touch){},

    zoomIn:function(event,_touch){

    },

    zoomOut:function(event,_touch){},

    longPress:function(event,_touch){},

    onAIGC:function(imageUrl){
        if(!ImageViewer.container) return;//非打开状态
        if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
            ImageViewer.onAIGC(imageUrl);
            return;
        }

        top.ImageViewer.trimPhotoChange(imageUrl);
    }
}
window.ImageViewer=ImageViewer;