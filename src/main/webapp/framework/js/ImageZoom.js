/**
 * 该功能适用于PC版网页，在需要查看大图的对象（图片）上按如下方式触发
 * onmouseover="ImageZoom.view(srcImg,0);"
 * onmousemove="ImageZoom.move(event);"
 * onmouseout="ImageZoom.hide();"
 * @type {{init: ((function(*=): (boolean))|*), view: ImageZoom.view, hide: ImageZoom.hide, move: ImageZoom.move, size: number, done: ImageZoom.done, srcImg: null}}
 */
let ImageZoom={
    //放大框的大小
    size:348,

    //源对象（图片）
    srcImg:null,
    
    init:function(srcImg, boxSize){
        if(!srcImg || Str.isBlank(srcImg.src) || !srcImg.width || !srcImg.height) return false;

        this.srcImg=srcImg;
        this.size=boxSize?boxSize:Math.max(srcImg.width, srcImg.height);
        if(!_$('imageZoom')){
            let s=[];
            s.push('<div id="imageZoom" class="imageZoom" style="width: '+this.size+'px; height: '+this.size+'px;" title="I{js,点击关闭}" onclick="this.style.visibility=\'hidden\';">');
            s.push('	<img id="img_imageZoom_loading" src="/framework/img/loading_300x300.gif" width="'+this.size+'"/>');
            s.push('	<img id="img_imageZoom" style="display:none;"/>');
            s.push('</div>');
            document.body.insertAdjacentHTML('afterbegin', s.join(''));

            //置于顶层
            _$('imageZoom').style.zIndex=W.getMaxZIndex();
        }
        return true;
    },

    /**
     *
     * @param srcImg 源对象（图片）
     * @param topOffset 上边距调整（默认和源对象齐平）
     * @param boxSize 放大框大小，不指定则与原图大小一致
     */
    view:function(srcImg, topOffset, boxSize){
        //初始化（返回false表示初始化失败）
        if(!this.init(srcImg, boxSize)) return;

        //已经显示
        if(_$('imageZoom').style.visibility=='visible') return;

        //显示
        _$('imageZoom').style.visibility='visible';

        //确定放大框显示位置
        let l=0;
        let t=0;
        if(W.elementLeft(srcImg)>this.size){
            l=W.elementLeft(srcImg)-this.size-10;
            t=W.elementTop(srcImg);
        }else{
            l=W.elementLeft(srcImg)+W.elementWidth(srcImg)+10;
            t=W.elementTop(srcImg);
        }

        if(topOffset) t+=topOffset;
        //确定放大框显示位置 end

        _$('imageZoom').style.top=t+'px';
        _$('imageZoom').style.left=l+'px';
        _$('imageZoom').className='imageZoom';

        let src=srcImg.src;
        src=Str.replaceAll(src,'_logo','');
        src=Str.replaceAll(src,'_min','');

        IMG.reset('img_imageZoom');
        Utils.setAtt(_$('img_imageZoom'),'_src',src);
        IMG.adjust('img_imageZoom','img_imageZoom_loading',4,-1,-1,1,1,ImageZoom.done,null, null, false,true);
    },

    /**
     * 图片加载完毕
     */
    done:function(){
        if(_$('imageZoom')) _$('imageZoom').className='imageZoomLoaded';
    },

    /**
     *
     */
    hide:function(){
        if(_$('imageZoom')) _$('imageZoom').parentNode.removeChild(_$('imageZoom'));
    },

    /**
     *
     * @param event
     */
    move:function(event){
        if(!_$('imageZoom') || _$('imageZoom').style.visibility!='visible') return;

        //光标位置
        let initX=0;
        let initY=0;
        if(event.clientX){
            initX=event.clientX;
            initY=event.clientY;
        }else if(event.pageX){
            initX=event.pageX;
            initY=event.pageY;
        }
        initX+=W.l();
        initY+=W.t();
        //光标位置 end

        //源对象位置
        let _left=W.elementLeft(this.srcImg);
        let _top=W.elementTop(this.srcImg);

        //光标在源对象中的相对位置
        let _offsetLeft=initX-_left;
        let _offsetTop=initY-_top;

        //放大图尺寸
        let largeWidth=_$('img_imageZoom').width;

        //放大图与源图显示尺寸的比例
        let ratio=largeWidth/this.srcImg.width;

        //光标在放大图中的相对位置
        let _offsetLeftLarge=Math.floor(_offsetLeft*ratio);
        let _offsetTopLarge=Math.floor(_offsetTop*ratio);

        //滚动放大图，使得光标在放大框中的相对位置与光标在原图中一致
        let _offsetLeftBox=Math.floor(_offsetLeft*this.size/this.srcImg.width);
        let _offsetTopBox=Math.floor(_offsetTop*this.size/this.srcImg.height);

        //确定放大图在放大框中的滚动条位置
        let _scrollLeft=_offsetLeftLarge-_offsetLeftBox;
        let _scrollTop=_offsetTopLarge-_offsetTopBox;
        if(_scrollLeft<0) _scrollLeft=0;
        if(_scrollTop<0) _scrollTop=0;

        _$('imageZoom').scrollLeft=Math.round(_scrollLeft);
        _$('imageZoom').scrollTop=Math.round(_scrollTop);
    }
}