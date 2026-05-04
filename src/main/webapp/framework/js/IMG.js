let IMG={
    images:[],

    //添加一个需要预加载的图片
    add:function(id,src,onload,onerr,fullScreen,parentSizeFixed,setStyle){
        let img=this.images[id];
        if(!img){
            img=new ImageObject(id,src,onload,onerr,fullScreen,parentSizeFixed,setStyle);
            this.images[id]=img;
        }
    },

    //根据id得到image对象
    get:function(id){
        return this.images[id];
    },

    //把预加载的图片的src赋给指定的对象
    set:function(obj,id){
        obj.src=this.images[id].src;
    },

    //重置图片
    reset:function(id){
        if(this.images[id]) this.images[id]=null;
        Utils.delAtt(_$(id),'src');
        Utils.delAtt(_$(id),'width');
        Utils.delAtt(_$(id),'height');
    },

    //重置全部记录
    resetAll(){
        this.images = new Array();
    },

    /**
     *
     * @param id 图片ID
     * @param idLoading 表示该图片正在加载中的对象（比如转圈的gif）的ID
     * @param zoomType 0，不调整大小，1，按宽  2，按高  3，按较长一边  4，正好满屏，较长（相对于容器长宽比例而言）一边超出容器部分隐藏
     * @param imgMaxWidth 最大宽度
     * @param imgMaxHeight 最大高度
     * @param middle 是否上下居中
     * @param center 是否左右居中
     * @param onload 图片加载完毕时回调
     * @param onerr 出错时回调
     * @param ondone 调整完毕时回调
     * @param fullScreen 如果实际尺寸小于设定尺寸是否放大到设定尺寸
     * @param parentSizeFixed 图片容器大小是否固定（不自动调整）
     * @param setStyle 是否通过style设置大小（默认：否）
     */
    adjust:function(id,idLoading,zoomType,imgMaxWidth,imgMaxHeight,middle,center,onload,onerr,ondone,fullScreen,parentSizeFixed,setStyle){
        if(!_$(id)) return;
        if((typeof setStyle)=='undefined') setStyle=true;

        let _parentNode=Utils.getParentNodeExcludeTag(_$(id),'a');

        //百分比转换
        if(imgMaxWidth<=1&&imgMaxWidth>0) imgMaxWidth=Math.floor(W.elementWidth(_parentNode)*imgMaxWidth);
        if(imgMaxHeight<=1&&imgMaxHeight>0) imgMaxHeight=Math.floor(W.elementWidth(_parentNode)*imgMaxHeight);

        let imgObejct=this.images[id];
        let imgSrc=Utils.att(_$(id),'_src')?Utils.att(_$(id),'_src'):_$(id).src;
        if(!imgObejct){
            this.add(id,
                imgSrc,
                onload,
                onerr,
                ondone,
                fullScreen,
                parentSizeFixed,
                setStyle);
            _$(id).style.marginTop='0px';
            _$(id).style.marginLeft='0px';
            imgObejct=this.images[id];
        }else{
            if(imgSrc!=imgObejct.src){
                imgObejct.src=imgSrc;
                imgObejct.img.src=imgSrc;
            }
            if(imgObejct.adjustTimer){
                clearTimeout(imgObejct.adjustTimer);
                imgObejct.adjustTimer=null;
            }
        }

        if(zoomType==0){//无需调整，直接显示
            if(!Str.isBlank(Utils.att(_$(id), '_src'))) _$(id).src=imgObejct.src;
            _$(id).style.display='';
            return;
        }

        let img=imgObejct.img;
        if(img.width<=0){
            //等待加载完毕
            imgObejct.adjustTimer=setTimeout("IMG.adjust('"+id+"','"+idLoading+"',"+zoomType+","+imgMaxWidth+","+imgMaxHeight+","+middle+","+center+",null,null,null,"+imgObejct.fullScreen+","+imgObejct.parentSizeFixed+","+imgObejct.setStyle+")",100);
            return;
        }

        if(imgMaxWidth<=0 && imgMaxHeight<=0){
            imgMaxWidth=img.width;
            imgMaxHeight=img.height;
        }
        let w=img.width;//图片实际宽度
        let h=img.height;//图片实际高度
        let newW=w;//新设宽度
        let newH=h;//新设高度

        if(zoomType==1){//1,按宽
            if(imgObejct.fullScreen || w>imgMaxWidth){
                newW=imgMaxWidth;
                newH=Math.round(h*(newW/w));
                _$(id).width=newW;
                _$(id).height=newH;
                if(setStyle){
                    _$(id).style.width=newW+'px';
                    _$(id).style.height='auto';
                }
            }
        }else if(zoomType==2){//2，按高
            if(imgObejct.fullScreen || h>imgMaxHeight){
                newH=imgMaxHeight;
                newW=Math.round(w*(newH/h));
                _$(id).height=newH;
                _$(id).width=newW;
                if(setStyle) {
                    _$(id).style.height=newH+'px';
                    _$(id).style.width='auto';
                }
            }
        }else if(zoomType==3){//3，按较长一边
            let containerRatio=imgMaxWidth/imgMaxHeight;//容器宽高比例
            let imgRatio=w/h;//图片宽高比例

            if(!imgObejct.parentSizeFixed){
                if(_parentNode){
                    _parentNode.style.width=imgMaxWidth+'px';
                    _parentNode.style.height=imgMaxHeight+'px';
                }
            }
            if(_parentNode) _parentNode.style.overflow='hidden';

            if(containerRatio>imgRatio){//容器比图片更狭长，图片高度方向顶边
                if(imgObejct.fullScreen || h>imgMaxHeight){
                    newH=imgMaxHeight;
                    newW=Math.round(w*(newH/h));
                    _$(id).height=newH;
                    _$(id).width=newW;
                    if(setStyle) {
                        _$(id).style.height = newH + 'px';
                        _$(id).style.width = 'auto';
                    }
                }
            }else{//图片宽度方向顶边
                if(imgObejct.fullScreen || w>imgMaxWidth){
                    newW=imgMaxWidth;
                    newH=Math.round(h*(newW/w));
                    _$(id).width=newW;
                    _$(id).height=newH;
                    if(setStyle) {
                        _$(id).style.width = newW + 'px';
                        _$(id).style.height = 'auto';
                    }
                }
            }
        }else if(zoomType==4){
            if(!imgObejct.parentSizeFixed){
                if(_parentNode){
                    _parentNode.style.width=imgMaxWidth+'px';
                    _parentNode.style.height=imgMaxHeight+'px';
                }
            }
            if(_parentNode) _parentNode.style.overflow='hidden';

            let containerRatio=imgMaxWidth/imgMaxHeight;//容器宽高比例
            let imgRatio=w/h;//图片实际宽高比例

            if(containerRatio>imgRatio) {//容器比图片更狭长，满屏后高度方向超出容器
                newW=imgMaxWidth;
                newH=Math.round(newW/imgRatio);

                _$(id).height=newH;
                _$(id).width=newW;
                if(setStyle){
                    _$(id).style.height=newH+'px';
                    _$(id).style.width=newW+'px';
                }

                if(_parentNode){
                    _parentNode.scrollTop=Math.round((newH-imgMaxHeight)/2);
                }
            }else if(imgRatio>containerRatio){//满屏后宽度方向超出容器
                newH=imgMaxHeight;
                newW=Math.round(imgRatio*newH);

                _$(id).height=newH;
                _$(id).width=newW;
                if(setStyle){
                    _$(id).style.height=newH+'px';
                    _$(id).style.width=newW+'px';
                }

                if(_parentNode){
                    _parentNode.scrollLeft=Math.round((newW-imgMaxWidth)/2);
                }
            }else {
                newW=imgMaxWidth;
                newH=imgMaxHeight;

                _$(id).height=newH;
                _$(id).width=newW;
                if(setStyle){
                    _$(id).style.height=newH+'px';
                    _$(id).style.width=newW+'px';
                }
            }
        }

        //上下居中
        if(middle && zoomType!=4){
            if(newH<imgMaxHeight) _$(id).style.marginTop=Math.floor((imgMaxHeight-newH)/2)+'px';
            else _$(id).style.marginTop='0px';
        }

        //左右居中
        if(center && zoomType!=4){
            if(newW<imgMaxWidth) _$(id).style.marginLeft=Math.floor((imgMaxWidth-newW)/2)+'px';
            else _$(id).style.marginLeft='0px';
        }

        if(idLoading&&_$(idLoading)) _$(idLoading).style.display='none';
        if(!Str.isBlank(Utils.att(_$(id), '_src'))) _$(id).src=imgObejct.src;
        _$(id).style.display='';

        if((typeof imgObejct.ondone)=='function') imgObejct.ondone.call(window,id,newW,newH);
    }
}
window.IMG=IMG;