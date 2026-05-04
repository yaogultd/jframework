//画板
let DrawingBoard={
    //可用字体
    fontFamilies:['宋体', '微软雅黑', 'Arial', 'Calibri', 'Cambria', 'Candara', 'Comic Sans MS', 'Consolas', 'Constantia', 'Corbel', 'Courier New', 'Georgia', 'Impact', 'Times New Roman', 'Verdana'],

    //图片库
    images:[
        '/shopping/diy/images/icon001.png',
        '/shopping/diy/images/icon002.png',
        '/shopping/diy/images/icon003.png',
        '/shopping/diy/images/icon004.png',
        '/shopping/diy/images/icon021.png',
        '/shopping/diy/images/icon022.png',
        '/shopping/diy/images/icon023.png',
        '/shopping/diy/images/icon024.png',
        '/shopping/diy/images/icon031.png',
        '/shopping/diy/images/icon032.png',
        '/shopping/diy/images/icon033.png',
        '/shopping/diy/images/icon034.png',
        '/shopping/diy/images/icon041.png',
        '/shopping/diy/images/icon042.png',
        '/shopping/diy/images/icon043.png',
        '/shopping/diy/images/icon044.png',
        '/shopping/diy/images/icon061.png',
        '/shopping/diy/images/icon062.png',
        '/shopping/diy/images/icon063.png',
        '/shopping/diy/images/icon064.png',
        '/shopping/diy/images/icon071.png',
        '/shopping/diy/images/icon072.png',
        '/shopping/diy/images/icon073.png',
        '/shopping/diy/images/icon074.png'
    ],

    layer: null,

    container: null,

    width: 0,

    height: 0,

    //打开画板
    open:function(args){
        if(!args || Str.isBlank(args.bg)) throw new Error("The bg is not set!");

        if(this.isOpen()) this.close();

        //加载颜色选取组件
        if(!this.readonly && (typeof ColorPicker)=='undefined'){
            loadJS({src:'/framework/js/ColorPicker.js'});
        }

        if(args.container){
            this.container = (typeof args.container)=='string' ? _$(args.container) : args.container;
        }

        this.uuid=Str.isBlank(args.uuid)?Global.generateUUID():args.uuid;

        //背景图
        this.bg = args.bg;
        this.position = isNaN(args.position*1)?1:args.position*1;
        this.bgImage = new Image();//图像
        this.bgImage.crossOrigin='anonymous';
        this.bgImage.src = this.bg;

        //左右上下边距
        this.paddingLeft = isNaN(args.paddingLeft*1)?0:args.paddingLeft*1;
        this.paddingRight = isNaN(args.paddingRight*1)?0:args.paddingRight*1;
        this.paddingTop = isNaN(args.paddingTop*1)?0:args.paddingTop*1;
        this.paddingBottom = isNaN(args.paddingBottom*1)?0:args.paddingBottom*1;

        //最多元素数
        this.maxItems = isNaN(args.maxItems*1)?10:args.maxItems*1;

        //纠正参数错误
        if(this.paddingLeft<0) this.paddingLeft=0;
        this.paddingLeft=Math.round(this.paddingLeft);

        if(this.paddingRight<0) this.paddingRight=0;
        this.paddingRight=Math.round(this.paddingRight);

        if(this.paddingTop<0) this.paddingTop=0;
        this.paddingTop=Math.round(this.paddingTop);

        if(this.paddingBottom<0) this.paddingBottom=0;
        this.paddingBottom=Math.round(this.paddingBottom);

        if(this.maxItems<=0) this.maxItems=10;
        this.maxItems=Math.round(this.maxItems);
        //纠正参数错误 end

        this.name=(typeof args.name)=='string' ? args.name : '';
        this.readonly=(typeof args.readonly)=='boolean' ? args.readonly : false;
        this.width=(typeof args.width)=='number' ? args.width : W.vw();
        this.height=(typeof args.height)=='number' ? args.height : W.vh();
        this.items=Array.isArray(args.items)?args.items:[];
        this.itemCurrent=null;//当前操作的对象的uuid
        this.target=args.target;
        this.canvas=null;
        this.context=null;
        this.bgColor=(typeof args.bgColor)=='string' ? args.bgColor : '#FFFFFF';
        this.callback=args.callback;
        this.callbackTarget=args.callbackTarget;
        this.loaded=0;
        this.initX=0;
        this.initY=0;
        this.distanceOfTwoPointX=0;
        this.distanceOfTwoPointY=0;

        //移动、放大方向
        this.moveMode=0;

        //是否正在编辑
        this.editing=false;

        //是否已经打开
        this.opened=false;

        //初始化界面
        this.init();

        //加载背景
        IMG.reset('DrawingBoardBg_image');
        IMG.adjust('DrawingBoardBg_image',
            null,
            3,
            this.width,
            this.height,
            0,
            0,
            null,
            null,
            this.onBgLoaded,
            true,
            false);
    },

    //设置放大、移动方式
    setMoveMode:function(){
        this.moveMode++;
        if(this.moveMode>2) this.moveMode=0;
        if(this.moveMode==0) _$('DrawingBoardMoveMode').className='DrawingBoardMove';
        if(this.moveMode==1) _$('DrawingBoardMoveMode').className='DrawingBoardMoveH';
        if(this.moveMode==2) _$('DrawingBoardMoveMode').className='DrawingBoardMoveV';
    },

    //背景加载完毕
    onBgLoaded:function(id,newW,newH){
        if((typeof this.fontFamilies)=='undefined'){
            DrawingBoard.onBgLoaded(id,newW,newH);
            return;
        }

        this.width=newW;
        this.height=newH;
        _$('DrawingBoardBg').style.height=newH+'px';

        //加载初始化对象
        for(let i=0; i<this.items.length; i++){
            this.appendItem(this.items[i], false);
        }
    },

    //初始化界面
    init:function (){
        let s = [];
        s.push('<div id="DrawingBoard">');

        //操作区
        s.push('    <div id="DrawingBoardOps">');

        s.push('        <div class="DrawingBoardOp" style="display:'+(this.readonly?'none':'')+';">');
        s.push('            <div class="fileInputWithSkin" style="width:30px;">');
        s.push('                <div class="skin">');
        s.push('                    <div class="aBtnWithIcon" style="width:30px;">');
        s.push('                        <div class="aIcon iconfont icon-tianjiatupian"></div>');
        s.push('                    </div>');
        s.push('                </div>');
        s.push('                <div class="file">');
        s.push('                    <input type="file" accept="image/*" onchange="DrawingBoard.imagePicked(this);" single/>');
        s.push('                </div>');
        s.push('            </div>');
        s.push('        </div>');

        s.push('        <div class="DrawingBoardOp" style="display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.showImageAddDialog();">');
        s.push('            <div class="fileInputWithSkin" style="width:30px;">');
        s.push('                <div class="skin">');
        s.push('                    <div class="aBtnWithIcon" style="width:30px;">');
        s.push('                        <div class="aIcon iconfont icon-tuku-copy"></div>');
        s.push('                    </div>');
        s.push('                </div>');
        s.push('            </div>');
        s.push('        </div>');

        s.push('        <div class="DrawingBoardOp" style="display:'+(this.readonly?'none':'')+';" onclick="AIGC.showAIGCDialog(null, DrawingBoard.onAIGC, window);">');
        s.push('            <div class="fileInputWithSkin" style="width:30px;">');
        s.push('                <div class="skin">');
        s.push('                    <div class="aBtnWithIcon" style="width:30px;">');
        s.push('                        <div class="aIcon iconfont icon-a-Component1"></div>');
        s.push('                    </div>');
        s.push('                </div>');
        s.push('            </div>');
        s.push('        </div>');

        s.push('        <div class="DrawingBoardOp" style="display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.showTextAddDialog();">');
        s.push('            <div class="fileInputWithSkin" style="width:30px;">');
        s.push('                <div class="skin">');
        s.push('                    <div class="aBtnWithIcon" style="width:30px;">');
        s.push('                        <div class="aIcon iconfont icon-xintianjiawenben"></div>');
        s.push('                    </div>');
        s.push('                </div>');
        s.push('            </div>');
        s.push('        </div>');


        if(this.callback && !this.readonly){
            s.push('        <div class="DrawingBoardOp mR5 mL0" style="float: right !important;" onclick="DrawingBoard.saveDraft();"><div class="font24px iconfont icon-baocun green"></div></div>');
        }

        s.push('        <div id="DrawingBoardRemoveItemBtn" class="DrawingBoardOp mR5 mL0" style="float: right !important; display: none;" onclick="DrawingBoard.removeItem();"><div class="font22px iconfont icon-ziyuanxhdpi red"></div></div>');
        s.push('        <div id="DrawingBoardShowTextAddDialogBtn" class="DrawingBoardOp mR5 mL0" style="float: right !important; display: none;" onclick="DrawingBoard.showTextAddDialog(true);"><div class="font22px iconfont icon-diaoseban"></div></div>');

        s.push('        <div class="DrawingBoardOp mR5 mL0" style="float: right !important; display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.rotate(-5);"><div class="font24px iconfont icon-nishizhenxuanzhuan"></div></div>');
        s.push('        <div class="DrawingBoardOp mR5 mL0" style="float: right !important; display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.rotate(5);"><div class="font24px iconfont icon-shunshizhenxuanzhuan1"></div></div>');
        s.push('        <div class="DrawingBoardOp mR5 mL0" style="float: right !important; display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.zoom(-10);"><div class="font24px iconfont icon-suoxiao"></div></div>');
        s.push('        <div class="DrawingBoardOp mR5 mL0" style="float: right !important; display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.zoom(10);"><div class="font24px iconfont icon-fangda"></div></div>');
        s.push('        <div class="DrawingBoardOp mR5 mL0" style="float: right !important; display:'+(this.readonly?'none':'')+';" onclick="DrawingBoard.setMoveMode();"><div id="DrawingBoardMoveMode" class="DrawingBoardMove"></div></div>');

        if(this.readonly){
            s.push('<div id="DrawingBoardName">I{DrawingBoard,'+(Str.isBlank(this.name)?'DIY':this.name)+'}</div>');
        }

        s.push('    </div>');
        //操作区 end

        //画板区
        s.push('    <div id="DrawingBoardPanel" class="noselect">');
        s.push('        <div id="DrawingBoardBg" class="noselect"><img class="noselect" id="DrawingBoardBg_image" _src="'+this.bg+'" style="display: none;"/></div>');
        s.push('    </div>');
        //画板区 end

        s.push('</div>');

        if(this.container==null){
            this.layer=Layers.open(window,'', '',null, '', 0, this.close);
            this.container = this.layer.getContentElement();
        }

        this.width=W.elementWidth(this.container);
        this.height=W.elementHeight(this.container);

        if(this.layer) this.layer.setContent(s.join(''));

        if(!this.readonly) {
            if(this.layer) s = [];
            s.push('<div class="fr btnH40 w80 btnBgGreen displayBlock mR5 mT5" style="width:30%;" onClick="DrawingBoard.done();">I{DrawingBoard,完成}</div>');
            s.push('<div class="fr btnH40 w80 btnBgOrange displayBlock mR5 mT5" style="width:30%;" onClick="DrawingBoard.preview();">I{DrawingBoard,预览}</div>');
            s.push('<div class="fr btnH40 w80 btnBgGray displayBlock mR5 mT5" style="width:30%;" onClick="DrawingBoard.close();">I{DrawingBoard,取消}</div>');
            if(this.layer) this.layer.setBtns(s.join(''));
        }

        if(this.layer==null){
            this.container.innerHTML = Lang.convert(s.join(''));
        }

        this.opened=true;
    },

    //选中本地图片
    imagePicked:function(input){
        if(!input.files || input.files.length==0) return;

        for(let i=0; i<input.files.length; i++){
            let item=new DrawingBoard.DrawingItem(null, 'image', '', input.files[i], 160, 160, 0, 0, null, {});
            DrawingBoard.appendItem(item, true);
        }
    },

    //选中图片库图片
    imageAdd:function(src){
        top.Dialog.close();
        let item=new DrawingBoard.DrawingItem(null, 'image', '', src, 160, 160, 0, 0, null, {});
        DrawingBoard.appendItem(item, true);
    },

    //图片库
    showImageAddDialog:function(){
        let s=[];
        s.push('<div style="width: 340px; height: 300px; overflow-y: scroll;">');
        for(let i=0; i<DrawingBoard.images.length; i++){
            s.push('<div class="DrawingBoardImageDefined" onclick="DrawingBoard.imageAdd(\''+DrawingBoard.images[i]+'\');"><img src="'+DrawingBoard.images[i]+'"/></div>');
        }
        s.push('</div>');

        top.Dialog.open(-1, -1, 360, -1);
        top.Dialog.setContent(Lang.convert(s.join('')));
        s=null;
        delete s;
    },

    //显示添加文本对话框
    showTextAddDialog:function(editing){
        this.editing = editing?editing:false;
        let s=[];
        s.push('<div class="r"><textarea style="width: 100%; height: 50px;" placeholder="I{DrawingBoard,输入您要添加的文字}" id="DrawingBoardTextInput" onkeyup="DrawingBoard.onTextStyleChanged();"></textarea></div>');

        s.push('<div class="r">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,字体大小}</div>');
        s.push('    <div class="fl"><select id="DrawingBoardFontSize" onchange="DrawingBoard.onTextStyleChanged();">');
        for(let i=12; i<=48; i++){
            s.push('<option value="'+i+'px">'+i+'px</option>');
        }
        s.push('   </select></div>');
        s.push('</div>');

        s.push('<div class="r">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,字体}</div>');
        s.push('    <div class="fl"><select id="DrawingBoardFontFamily" onchange="DrawingBoard.onTextStyleChanged();">');
        s.push('<option value="Arial">I{DrawingBoard,默认}</option>');
        for(let i=0; i<DrawingBoard.fontFamilies.length; i++){
            s.push('<option value="'+DrawingBoard.fontFamilies[i]+'">'+DrawingBoard.fontFamilies[i]+'</option>');
        }
        s.push('   </select></div>');
        s.push('</div>');

        s.push('<div class="r">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,是否加粗}</div>');
        s.push('    <div class="fl"><select id="DrawingBoardFontBold" onchange="DrawingBoard.onTextStyleChanged();">');
        s.push('        <option value="false">I{DrawingBoard,否}</option>');
        s.push('        <option value="true">I{DrawingBoard,是}</option>');
        s.push('   </select></div>');
        s.push('</div>');

        s.push('<div class="r">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,是否斜体}</div>');
        s.push('    <div class="fl"><select id="DrawingBoardFontItalic" onchange="DrawingBoard.onTextStyleChanged();">');
        s.push('        <option value="false">I{DrawingBoard,否}</option>');
        s.push('        <option value="true">I{DrawingBoard,是}</option>');
        s.push('   </select></div>');
        s.push('</div>');

        s.push('<div class="hidden">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,是否竖排}</div>');
        s.push('    <div class="fl"><select id="DrawingBoardFontVertical" onchange="DrawingBoard.onTextStyleChanged();">');
        s.push('        <option value="false">I{DrawingBoard,否}</option>');
        s.push('        <option value="true">I{DrawingBoard,是}</option>');
        s.push('   </select></div>');
        s.push('</div>');

        s.push('<div class="r">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,字体颜色}</div>');
        s.push('    <div class="fl w30" id="DrawingBoardFontColor" colorCode="#333333" style="background-color: #333333;">&nbsp;</div>');
        s.push('    <div class="fl mL5"><a href="javascript:_void();" class="aunderline ared" onclick="ColorPicker.show(DrawingBoard.onColorPicked, Utils.att(_$(\'DrawingBoardFontColor\'), \'colorCode\'));">I{DrawingBoard,设置}</a></div>');
        s.push('</div>');

        s.push('<div class="r">');
        s.push('    <div class="fl w100 alignR pdR5">I{DrawingBoard,背景颜色}</div>');
        s.push('    <div class="fl w30" id="DrawingBoardBgColor" colorCode="">&nbsp;</div>');
        s.push('    <div class="fl mL5"><a href="javascript:_void();" class="aunderline ared" onclick="ColorPicker.show(DrawingBoard.onBgColorPicked, Utils.att(_$(\'DrawingBoardBgColor\'), \'colorCode\'));">I{DrawingBoard,设置}</a></div>');
        s.push('    <div class="fl mL5"><a href="javascript:_void();" class="aunderline ared" onclick="DrawingBoard.onBgColorPicked();">I{DrawingBoard,清除背景}</a></div>');
        s.push('</div>');


        top.Dialog.open();
        top.Dialog.setContent(Lang.convert(s.join('')));
        s=null;
        delete s;

        if(this.editing){
            let found = this.findItem(this.itemCurrent);
            if(found<0) return;
            let item = this.items[found];
            _$('DrawingBoardTextInput').value=item.source;
            _$('DrawingBoardFontSize').value=item.styles.fontSize;
            _$('DrawingBoardFontFamily').value=item.styles.fontFamily;
            _$('DrawingBoardFontBold').value=item.styles.fontBold?'true':'false';
            _$('DrawingBoardFontItalic').value=item.styles.fontItalic?'true':'false';
            _$('DrawingBoardFontVertical').value=item.styles.vertical?'true':'false';
            Utils.setAtt(_$('DrawingBoardFontColor'), 'colorCode', item.styles.fontColor);
            Utils.setAtt(_$('DrawingBoardBgColor'), 'colorCode', item.styles.backgroundColor);

            _$('DrawingBoardFontColor').style.backgroundColor=item.styles.fontColor;
            _$('DrawingBoardBgColor').style.backgroundColor=item.styles.backgroundColor;

            top.Dialog.setBtns(['<div class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="DrawingBoard.appendText(false);">I{完成}</div>',
                '<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="top.Dialog.close();">I{取消}</div>']);
        }else{
            top.Dialog.setBtns(['<div class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="DrawingBoard.appendText(true);">I{添加}</div>',
                '<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="top.Dialog.close();">I{取消}</div>']);
        }
    },

    //添加文本
    appendText:function(isNew){
        if(!isNew){
            let found = this.findItem(this.itemCurrent);
            if(found<0) return;
            let item = this.items[found];
            item.source=_$('DrawingBoardTextInput').value;
            item.styles.fontSize=_$('DrawingBoardFontSize').value;
            item.styles.fontFamily=_$('DrawingBoardFontFamily').value;
            item.styles.fontBold=_$('DrawingBoardFontBold').value==='true';
            item.styles.fontItalic=_$('DrawingBoardFontItalic').value==='true';
            item.styles.vertical=_$('DrawingBoardFontVertical').value==='true';
            item.styles.fontColor=Utils.att(_$('DrawingBoardFontColor'), 'colorCode');
            item.styles.backgroundColor=Utils.att(_$('DrawingBoardBgColor'), 'colorCode');
            top.Dialog.close();
            item.render();
            return;
        }

        let item= new DrawingBoard.DrawingItem(null, 'text', '', _$('DrawingBoardTextInput').value, 160, 160, 0, 0, null, {});
        item.source=_$('DrawingBoardTextInput').value;
        item.styles.fontSize=_$('DrawingBoardFontSize').value;
        item.styles.fontFamily=_$('DrawingBoardFontFamily').value;
        item.styles.fontBold=_$('DrawingBoardFontBold').value==='true';
        item.styles.fontItalic=_$('DrawingBoardFontItalic').value==='true';
        item.styles.vertical=_$('DrawingBoardFontVertical').value==='true';
        item.styles.fontColor=Utils.att(_$('DrawingBoardFontColor'), 'colorCode');
        item.styles.backgroundColor=Utils.att(_$('DrawingBoardBgColor'), 'colorCode');
        top.Dialog.close();
        DrawingBoard.appendItem(item, true);
    },

    //选取颜色
    onColorPicked:function(colorCode){
        if((typeof this.fontFamilies)=='undefined'){
            DrawingBoard.onColorPicked(colorCode);
            return;
        }

        if(colorCode && !colorCode.startsWith('#')) colorCode='#'+colorCode;

        Utils.setAtt(_$('DrawingBoardFontColor'), 'colorCode', colorCode);
        _$('DrawingBoardFontColor').style.backgroundColor=colorCode;
        DrawingBoard.onTextStyleChanged();
    },

    //选取背景色
    onBgColorPicked:function(colorCode){
        if((typeof this.fontFamilies)=='undefined'){
            DrawingBoard.onBgColorPicked(colorCode);
            return;
        }

        if(!colorCode){
            Utils.setAtt(_$('DrawingBoardBgColor'), 'colorCode', '');
            _$('DrawingBoardBgColor').style.backgroundColor='';
            DrawingBoard.onTextStyleChanged();
            return;
        }

        if(!colorCode.startsWith('#')) colorCode='#'+colorCode;
        Utils.setAtt(_$('DrawingBoardBgColor'), 'colorCode', colorCode);
        _$('DrawingBoardBgColor').style.backgroundColor=colorCode;
        DrawingBoard.onTextStyleChanged();
    },

    //文本样式改变
    onTextStyleChanged:function(){
        if((typeof this.fontFamilies)=='undefined'){
            DrawingBoard.onTextStyleChanged();
            return;
        }
        if(!this.editing) return;

        let found = this.findItem(this.itemCurrent);
        if(found<0) return;
        let item = this.items[found];
        item.source=Str.trimAll(_$('DrawingBoardTextInput').value);
        item.styles.fontSize=_$('DrawingBoardFontSize').value;
        item.styles.fontFamily=_$('DrawingBoardFontFamily').value;
        item.styles.fontBold='true'==_$('DrawingBoardFontBold').value;
        item.styles.fontItalic='true'==_$('DrawingBoardFontItalic').value;
        item.styles.vertical='true'==_$('DrawingBoardFontVertical').value;
        item.styles.fontColor=Utils.att(_$('DrawingBoardFontColor'), 'colorCode');
        item.styles.backgroundColor=Utils.att(_$('DrawingBoardBgColor'), 'colorCode');
        item.render();
    },

    //当前对象
    getCurrentItem:function (){
        let found = this.findItem(this.itemCurrent);
        if(found<0) return null;
        return this.items[found];
    },

    //添加对象
    appendItem:function(item, isNew){
        if(this.items.length==this.maxItems){
            console.log('超出最多允许的元素个数 => '+this.maxItems+','+this.items.length);
            Page.alert('I{DrawingBoard,超出最多允许的元素个数}',null,null,Dialog.MSG_TYPE_WARN);
            return;
        }
        if(isNew) this.items.push(item);
        this.itemCurrent=item.uuid;
        item.container=this;
        item.load();//load完会调用onItemLoad，然后显示在画板上
    },

    //当对象加载完毕时
    onItemLoad:function(item){
        item.show();
    },

    //当对象显示时
    onItemShown:function(id, newW, newH){
        if(!id.startsWith('image_')) return;
        id=id.substring(6);
        let found = DrawingBoard.findItem(id);
        if(found<0) return;
        let item=DrawingBoard.items[found];
        if(item.ratio>0) return;
        item.ratio=newW/newH;
        _$('DrawingBoardItem_'+id).style.width=newW+'px';
        _$('DrawingBoardItem_'+id).style.height=newH+'px';
    },

    //查找对象（返回index）
    findItem:function(uuid){
        for(let i=0; i<this.items.length; i++){
            if(this.items[i].uuid==uuid) return i;
        }
        return -1;
    },

    //添加对象
    removeItem:function(){
        let found = this.findItem(this.itemCurrent);
        if(found<0) return;
        let item=this.items[found];
        item.destroy();
        this.items.splice(found, 1);
        this.itemCurrent=null;
        _$('DrawingBoardShowTextAddDialogBtn').style.display='none';
        _$('DrawingBoardRemoveItemBtn').style.display='none';
    },

    //放大、所选
    zoom:function(movement){
        let found = this.findItem(this.itemCurrent);
        if(found<0) return;
        let item=this.items[found];
        item.zoom(movement);
    },

    //顺时针旋转（默认10度）
    rotate:function(deg){
        if((typeof deg)!='number') deg=5;
        let found = this.findItem(this.itemCurrent);
        if(found<0) return;
        let item=this.items[found];
        item.rotate(deg);
    },

    isOpen:function (){
        return this.container != null;
    },

    //关闭
    close:function(){
        if((typeof this.fontFamilies)=='undefined'){
            DrawingBoard.close();
            return;
        }

        this.opened=false;

        this.container=null;

        //关闭关联组件
        ColorPicker.hide();
        top.Dialog.close();

        //销毁对象
        for(let i=0; i<this.items.length; i++) this.items[i].destroy();

        //清空对象列表
        this.items=[];

        //关闭layer
        if(this.layer){
            this.layer.close();
            this.layer=null;
        }
    },

    //预览
    preview:function(){
        if(this.target){
            this.toImage();
            return;
        }

        let layer=Layers.open(window, 'I{DrawingBoard,预览}',null,'<div></div>');
        layer.setContent('<img width="'+this.width+'" height="'+this.height+'" id="DrawingBoardPreview"/>');
        this.toImage(_$('DrawingBoardPreview'));
    },

    toData:function (){
        let datas=[];
        for(let i=0; i<this.items.length; i++){
            datas.push(this.items[i].toData(i));
        }
        return datas;
    },

    toJson:function(){
        let s=[];
        s.push('{"uuid": "'+this.uuid+'"');
        s.push(',"position": '+this.position);
        s.push(',"bg": "'+this.bg+'"');
        s.push(',"width": '+this.width);
        s.push(',"height": '+this.height);
        s.push(',"paddingLeft": '+this.paddingLeft);
        s.push(',"paddingRight": '+this.paddingRight);
        s.push(',"paddingTop": '+this.paddingTop);
        s.push(',"paddingBottom": '+this.paddingBottom);
        s.push(',"maxItems": '+this.maxItems);
        s.push(',"items": [');
        for(let i=0; i<this.items.length; i++){
            if(i>0) s.push(',');
            s.push(this.items[i].toJson());
        }
        s.push(']');
        s.push('}');
        return s.join('');
    },

    openFromJson:function(json, callback, callbackTarget){
        let args={
            uuid: json.uuid,
            position: json.position,
            bg: json.bg,
            name: json.name,
            readonly: (typeof json.readonly)=='boolean'?json.readonly:false,
            width: json.width,
            height: json.height,
            paddingLeft: json.paddingLeft,
            paddingRight: json.paddingRight,
            paddingTop: json.paddingTop,
            paddingBottom: json.paddingBottom,
            maxItems: json.maxItems,
            callback: callback,
            callbackTarget: callbackTarget
        };

        let items = [];
        if(json.items){
            for(let i=0; i<json.items.length; i++){
                let item = new DrawingBoard.DrawingItem(json.items[i].uuid,
                    json.items[i].type,
                    json.items[i].mimeType,
                    json.items[i].source,
                    json.items[i].width,
                    json.items[i].height,
                    json.items[i].posX,
                    json.items[i].posY,
                    json.items[i].zIndex,
                    json.items[i].styles);
                item.rotateDeg=json.items[i].rotateDeg;
                items.push(item);
                IMG.reset('image_'+item.uuid);
            }
        }
        args.items=items;
        this.open(args);
    },

    //完成
    done:function(){
        if(!this.callback){
            this.close();
            return;
        }

        this.callback.call(this.callbackTarget?this.callbackTarget:window, this.toJson(), this.toData());
        this.close();
    },

    //保存草稿
    saveDraft:function(){
        if(!this.callback){
            return;
        }
        this.callback.call(this.callbackTarget?this.callbackTarget:window, this.toJson(), this.toData(), 'saveDraft');
    },

    //根据item的zIndex排序
    sortItems:function(){
        this.items = Sorter.bubble(this.items, Sorter.SORT_ASC, DrawingBoard.DrawingItemSorter);
    },

    //绘制成一个图片
    toImage:function(_target){
        if(this.canvas){
            //清除画布
            if(this.width>this.height) this.context.clearRect(0, 0, this.width, this.width);
            else this.context.clearRect(0, 0, this.height, this.height);
        }else{
            //初始化画布
            this.canvas = document.createElement("canvas");
            this.canvas.width = this.width;
            this.canvas.height = this.height;
            this.context = this.canvas.getContext("2d");
        }

        //背景色
        if(!Str.isBlank(this.bgColor)) {
            this.context.fillStyle = this.bgColor;
            this.context.fillRect(0, 0, this.width, this.height);
        }

        //背景图
        this.context.drawImage(this.bgImage,
            0,
            0,
            this.width,
            this.height);

        //逐个对象绘制
        this.sortItems();
        for(let i=0; i<this.items.length; i++){
            this.items[i].draw();
        }

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
    },

    //item排序
    DrawingItemSorter:{
        compare:function(pre, after){
            if(pre.zIndex<after.zIndex) return Sorter.COMPARE_SMALLER;
            else if(pre.zIndex>after.zIndex) return Sorter.COMPARE_BIGGER;
            else return Sorter.COMPARE_EQUAL;
        }
    },

    //AIGC
    onAIGC:function(imageUrl){
        DrawingBoard.imageAdd(imageUrl);
    },

    /**
     *
     * @param uuid
     * @param type
     * @param mimeType
     * @param source
     * @param width
     * @param height
     * @param posX
     * @param posY
     * @param zIndex
     * @param styles
     * @constructor
     */
    DrawingItem:function(uuid, type, mimeType, source, width, height, posX, posY, zIndex, styles){
        this.uuid=Str.isBlank(uuid)?Global.generateUUID():uuid;
        this.type=type;
        this.mimeType=mimeType;
        this.source=source;
        this.width=width;
        this.height=height;
        this.ratio=0;
        this.posX=posX?posX:0;
        this.posY=posY?posY:0;
        this.zIndex=zIndex?zIndex:W.getMaxZIndex();
        this.inMovement=false;
        this.rotateDeg=0;//旋转角度（顺时针）
        this.blob=null;
        this.img=null;
        if((typeof source)=='string'){
            source=source.toLowerCase();
            if(source.endsWith('.png')){
                this.mimeType='image/png';
            }else{
                this.mimeType='image/jpeg';
            }
        }else{
            this.mimeType=(source.type||'image/png');
        }

        //默认样式
        this.styles={
            fontSize: '12px',
            fontFamily: 'Arial',
            fontColor: '#333333',
            backgroundColor: '',
            fontBold: false,
            fontItalic: false,
            vertical: false
        }

        if(styles){
            this.styles.fontSize=Str.isBlank(styles.fontSize)?'12px':styles.fontSize;
            this.styles.fontFamily=Str.isBlank(styles.fontFamily)?'':styles.fontFamily;
            this.styles.fontColor=Str.isBlank(styles.fontColor)?'#333333':styles.fontColor;
            if('text'==this.type) this.styles.backgroundColor=Str.isBlank(styles.backgroundColor)?'':styles.backgroundColor;
            this.styles.fontBold=(typeof styles.fontBold == 'boolean')?styles.fontBold:false;
            this.styles.fontItalic=(typeof styles.fontItalic == 'boolean')?styles.fontItalic:false;
        }

        if('image'==this.type){
            let _item=this;
            this.img=new Image();//图像
            this.img.crossOrigin='anonymous';
            this.img.onload = function(e){//加载完成后
                DrawingBoard.onItemLoad(_item);
                
                // 创建一个Canvas元素
                let canvas = document.createElement('canvas');
                canvas.width = this.width;
                canvas.height = this.height;

                // 绘制图片到Canvas
                let ctx = canvas.getContext('2d');
                ctx.drawImage(this, 0, 0, canvas.width, canvas.height);

                // 将Canvas转换为Blob
                canvas.toBlob(function(blob) {
                    console.log('blob <= '+blob);
                    _item.blob=blob;
                }, _item.mimeType);

            };

            if((typeof this.source)=='string'){
                source=source.toLowerCase();
                this.mimeType=source.endsWith('.png')?'image/png':'image/jpeg';
            }else{
                this.mimeType=(source.type||'image/png');
                this.reader=new FileReader();//文件读取器
                this.reader.onload = function(e){//读取完成后给图像赋值
                    _item.img.src = e.target.result;//图像赋值后会触发img的onload
                };
            }
        }
    }
}
window.DrawingBoard=DrawingBoard;

/**
 * 加载
 */
DrawingBoard.DrawingItem.prototype.load=function(){
    if(this.type==='text') {//如果是文字
        DrawingBoard.onItemLoad(this);
        return;
    }

    if(this.type==='line') {//如果是线条
        DrawingBoard.onItemLoad(this);
        return;
    }

    if((typeof this.source)=='string') this.img.src = this.source;
    else this.reader.readAsDataURL(this.source);
}

/**
 * 显示到面板上
 */
DrawingBoard.DrawingItem.prototype.show=function(){
    this.zIndex=W.getMaxZIndex();

    if(!this.display){
        let div = document.createElement('div');
        div.className='DrawingBoardItem';
        div.id='DrawingBoardItem_'+this.uuid;
        div.style.zIndex=this.zIndex;
        Utils.setAtt(div, 'uuid', this.uuid);
        _$('DrawingBoardPanel').appendChild(div);

        let s=[];
        if('image'==this.type){
            s.push('    <img id="image_'+this.uuid+'" _src="'+this.img.src+'" style="display: none;"/>');
        }else{
            s.push(this.source);
        }
        div.innerHTML=s.join('');
        s=null;
        delete s;
        this.display=_$('DrawingBoardItem_'+this.uuid);

        //移动、缩放
        new Touch(this.display,
            10,
            this.movingStart,
            this.moving,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    }else{
        if('image'==this.type){
            Utils.setAtt(_$('image_'+this.uuid), '_src', this.img.src);
            _$('image_'+this.uuid).style.display='none';
            IMG.reset('image_'+this.uuid);
        }else{
            _$('DrawingBoardItem_'+this.uuid).innerHTML=this.source;
        }
    }

    this.render();
    if(this.uuid==DrawingBoard.itemCurrent) this.setAsCurrent();
}

//设为当前编辑对象
DrawingBoard.DrawingItem.prototype.setAsCurrent=function(){
    //当前对象设置边框
    DrawingBoard.itemCurrent=this.uuid;
    let items = _$cls('DrawingBoardItem');
    for(let i=0; i<items.length; i++){
        items[i].style.borderColor='#000';
    }
    this.display.style.borderColor='var(--red)';
    this.showMenu();
}

//开始移动、缩放
DrawingBoard.DrawingItem.prototype.movingStart=function(event, _touch){
    let element=Utils.getEventTarget(event);
    let uuid=Utils.att(element, 'uuid');
    if(Str.isBlank(uuid)) return;

    let found=DrawingBoard.findItem(uuid);
    if(found<0) return;

    let me = DrawingBoard.items[found];
    me.initX=_touch.initScreenX;
    me.initY=_touch.initScreenY;
    me.distanceOfTwoPointX=0;
    me.distanceOfTwoPointY=0;
    me.setAsCurrent();
}

//移动、缩放
DrawingBoard.DrawingItem.prototype.moving=function(event, _touch){
    let element=Utils.getEventTarget(event);
    let uuid=Utils.att(element, 'uuid');
    if(Str.isBlank(uuid)) return;

    let found=DrawingBoard.findItem(uuid);
    if(found<0) return;

    let me = DrawingBoard.items[found];
    me.doMoving(event, _touch);
    me.initX=_touch.screenX;
    me.initY=_touch.screenY;
}

/**
 * 改变大小/样式
 */
DrawingBoard.DrawingItem.prototype.render=function(){
    this.display.style.width=this.width+'px';
    this.display.style.height=this.height+'px';
    this.display.style.left=this.posX+'px';
    this.display.style.top=this.posY+'px';
    if(DrawingBoard.readonly) this.display.style.border='none';
    if(this.rotateDeg>0) this.display.style.transform='rotate('+this.rotateDeg+'deg)';
    if(this.type==='image'){
        IMG.adjust('image_'+this.uuid,
            null,
            3,
            this.width,
            this.height,
            0,
            0,
            null,
            null,
            DrawingBoard.onItemShown,
            true,
            true);
    }else{
        _$('DrawingBoardItem_'+this.uuid).innerHTML=this.source;
        _$('DrawingBoardItem_'+this.uuid).style.fontSize=this.styles.fontSize;
        _$('DrawingBoardItem_'+this.uuid).style.fontWeight=this.styles.fontBold?'bold':'normal';
        _$('DrawingBoardItem_'+this.uuid).style.fontStyle=this.styles.fontItalic?'italic':'normal';
        _$('DrawingBoardItem_'+this.uuid).style.color=this.styles.fontColor;
        _$('DrawingBoardItem_'+this.uuid).style.backgroundColor=this.styles.backgroundColor;
        _$('DrawingBoardItem_'+this.uuid).style.fontFamily=this.styles.fontFamily;
        if(this.styles.vertical) _$('DrawingBoardItem_'+this.uuid).style.writingMode='vertical-rl';
        else _$('DrawingBoardItem_'+this.uuid).style.writingMode='horizontal-tb';
    }
}

/**
 * 旋转
 */
DrawingBoard.DrawingItem.prototype.rotate=function(deg){
    this.rotateDeg+=deg;
    if(this.rotateDeg>=360) this.rotateDeg-=360;
    if(this.rotateDeg<0) this.rotateDeg=360+this.rotateDeg;
    if(this.display) this.display.style.transform='rotate('+this.rotateDeg+'deg)';
}

/**
 * 放大、缩小
 */
DrawingBoard.DrawingItem.prototype.zoom=function(movement){
    this.inMovement=true;

    let maxLeft = W.elementWidth(_$('DrawingBoardBg'));
    let maxTop = W.elementHeight(_$('DrawingBoardBg'));

    //X方向缩放
    if(Math.abs(movement)>0 && (DrawingBoard.moveMode!=1 || this.type=='image')) this.width=Math.max(20, this.width+movement);

    //Y方向缩放
    if(Math.abs(movement)>0 && (DrawingBoard.moveMode!=2 || this.type=='image')) this.height=Math.max(20, this.height+movement);

    if(this.width>DrawingBoard.width) this.width=DrawingBoard.width;
    if(this.height>DrawingBoard.height) this.height=DrawingBoard.height;

    //图片保持比率
    if('image'==this.type){
        if(this.width/this.height > this.ratio){
            this.height=Math.round(this.width/this.ratio);
        }else if(this.width/this.height < this.ratio){
            this.width=Math.round(this.height*this.ratio);
        }
    }

    if(this.posX+this.width>maxLeft){
        this.posX=maxLeft-this.width;
    }

    if(this.posY+this.height>maxTop){
        this.posY=maxTop-this.height;
    }

    this.display.style.left=this.posX+'px';
    this.display.style.top=this.posY+'px';

    //图片调整大小
    this.render();
    if('image'==this.type) {
        //IMG.reset('image_' + this.uuid);
        this.render();
    }else{
        this.display.style.width=this.width+'px';
        this.display.style.height=this.height+'px';
    }
    this.inMovement=false;
}

/**
 * 双指缩放，单指移动
 */
DrawingBoard.DrawingItem.prototype.doMoving=function(event, _touch){
    if(!this.display || this.inMovement) return;//尚未构建对应的html对象
    this.inMovement=true;

    let maxLeft = W.elementWidth(_$('DrawingBoardBg'));
    let maxTop = W.elementHeight(_$('DrawingBoardBg'));
    if(_touch.distanceOfTwoPoint==0 && (!event.targetTouches || event.targetTouches.length==1)){//一个手指
        //X方向移动距离
        let movement= Math.floor(_touch.screenX-this.initX);
        if(DrawingBoard.moveMode==1) movement=0;//垂直移动，水平方向忽略

        let left=this.posX+movement;

        let minLeft= 0;
        if(left<minLeft){
            left=minLeft;
        }else if(left+this.width > maxLeft){
            left=maxLeft-this.width;
        }
        this.posX=left;

        //Y方向移动距离
        movement=Math.floor(_touch.screenY-this.initY);
        if(DrawingBoard.moveMode==2) movement=0;//水平移动，垂直方向忽略

        let _top=this.posY+movement;

        let minTop = 0;
        if(_top<minTop){
            _top=minTop;
        }else if(_top+this.height > maxTop){
            _top=maxTop-this.height;
        }
        this.posY=_top;

        this.display.style.left=left+'px';
        this.display.style.top=_top+'px';

        this.inMovement=false;
        return;
    }

    //两个手指
    //X方向缩放
    if(this.distanceOfTwoPointX==0) this.distanceOfTwoPointX=_touch.distanceOfTwoPointX;
    let movement=_touch.distanceOfTwoPointX-this.distanceOfTwoPointX;
    this.distanceOfTwoPointX=_touch.distanceOfTwoPointX;
    if(Math.abs(movement)>0) this.width=Math.max(20, this.width+movement);

    //Y方向缩放
    if(this.distanceOfTwoPointY==0) this.distanceOfTwoPointY=_touch.distanceOfTwoPointY;
    movement=_touch.distanceOfTwoPointY-this.distanceOfTwoPointY;
    this.distanceOfTwoPointY=_touch.distanceOfTwoPointY;
    if(Math.abs(movement)>0) this.height=Math.max(20, this.height+movement);

    if(this.width>DrawingBoard.width) this.width=DrawingBoard.width;
    if(this.height>DrawingBoard.height) this.height=DrawingBoard.height;

    //图片保持比率
    if('image'==this.type){
        if(this.width/this.height > this.ratio){
            this.height=Math.round(this.width/this.ratio);
        }else if(this.width/this.height < this.ratio){
            this.width=Math.round(this.height*this.ratio);
        }
    }

    if(this.posX+this.width>maxLeft){
        this.posX=maxLeft-this.width;
    }

    if(this.posY+this.height>maxTop){
        this.posY=maxTop-this.height;
    }

    this.display.style.left=this.posX+'px';
    this.display.style.top=this.posY+'px';

    //图片调整大小
    if('image'==this.type) {
        //IMG.reset('image_' + this.uuid);
        this.render();
    }else{
        this.display.style.width=this.width+'px';
        this.display.style.height=this.height+'px';
    }
    this.inMovement=false;
}

//操作菜单
DrawingBoard.DrawingItem.prototype.showMenu=function(){
    if(DrawingBoard.readonly) return;
    _$('DrawingBoardShowTextAddDialogBtn').style.display=this.type==='image'?'none':'';
    _$('DrawingBoardRemoveItemBtn').style.display='';
}

//绘制
DrawingBoard.DrawingItem.prototype.draw=function(){
    //画布旋转
    let offsetX=0;
    let offsetY=0;
    if(this.rotateDeg!=0){
        DrawingBoard.context.save();//保存状态

        //旋转画布
        DrawingBoard.context.translate(this.posX+this.width/2, this.posY+this.height/2);
        DrawingBoard.context.rotate(this.rotateDeg * (Math.PI / 180));
        offsetX=0-(this.posX+this.width/2);
        offsetY=0-(this.posY+this.height/2);
    }

    if(this.type==='text') {//如果是文字
        //背景色
        if(!Str.isBlank(this.styles.backgroundColor)) {
            DrawingBoard.context.fillStyle = this.styles.backgroundColor;
            DrawingBoard.context.fillRect(this.posX + offsetX, this.posY + offsetY, this.width, this.height);
        }

        if(Str.isBlank(this.source)){
            DrawingBoard.context.restore();//恢复状态
            return;
        }

        if(this.styles.fontColor) DrawingBoard.context.fillStyle = this.styles.fontColor;

        let fontStyle='';
        if(this.styles.fontBold) fontStyle+=' bold';
        if(this.styles.fontItalic) fontStyle+=' italic';
        fontStyle+=' '+this.styles.fontSize;
        if(this.styles.fontFamily) fontStyle+=' '+this.styles.fontFamily;
        fontStyle=Str.trimAll(fontStyle);
        DrawingBoard.context.font = fontStyle;
        console.log('text fontStyle => '+fontStyle+' : '+DrawingBoard.context.font);

        let metrics = DrawingBoard.context.measureText(this.source);

        //所有字在这个字体下的高度
        let fontHeight = metrics.fontBoundingBoxAscent + metrics.fontBoundingBoxDescent;
        console.log('fontHeight => '+fontHeight);

        // 当前文本字符串在这个字体下用的实际高度
        //let actualHeight = metrics.actualBoundingBoxAscent + metrics.actualBoundingBoxDescent;
        //console.log('actualHeight => '+actualHeight);

        DrawingBoard.context.textBaseline = 'top'; // 设置基线为顶部
        DrawingBoard.context.textAlign = 'left';
        if(this.styles.vertical){//竖排
            offsetX-=2;
            offsetY+=4;
            let chars = this.source.split(''); // 将文字分割为单个字符的数组
            let outputWidth= 0;
            let lines= 0;
            let maxCharWidth=0;
            for(let c=0; c<chars.length; c++){
                let charWidth = DrawingBoard.context.measureText(chars[c]).width;
                maxCharWidth=Math.max(maxCharWidth, charWidth);
            }
            for(let c=0; c<chars.length; c++){
                let charWidth = DrawingBoard.context.measureText(chars[c]).width;
                let actualHeight = metrics.actualBoundingBoxAscent + metrics.actualBoundingBoxDescent;
                outputWidth += actualHeight;
                if(outputWidth > this.height){//需要换行了
                    lines++;
                    outputWidth=actualHeight;
                }

                let y = this.posY + outputWidth - actualHeight;
                let x = this.posX + this.width - (lines + 1)*maxCharWidth + (maxCharWidth - charWidth)/2;
                DrawingBoard.context.fillText(chars[c], x + offsetX, y + offsetY);
            }
        }else{
            offsetX+=2;
            offsetY+=4;
            let chars = this.source.split(''); // 将文字分割为单个字符的数组
            let outputWidth= 0;
            let lines= 0;
            for(let c=0; c<chars.length; c++){
                let charWidth = DrawingBoard.context.measureText(chars[c]).width;
                outputWidth += charWidth;
                if(outputWidth > this.width){//需要换行了
                    lines++;
                    outputWidth=charWidth;
                }

                let x = this.posX + outputWidth - charWidth;
                let y = this.posY + lines*fontHeight;
                DrawingBoard.context.fillText(chars[c], x + offsetX, y + offsetY);
            }
        }

        DrawingBoard.context.restore();//恢复状态
        return;
    }

    if(this.type==='line') {//如果是线条
        DrawingBoard.context.moveTo(this.posX,this.posY);
        DrawingBoard.context.lineTo(this.posX+this.width+offsetX,this.posY+offsetY);
        DrawingBoard.context.closePath();
        DrawingBoard.context.fillStyle=this.fontColor;
        DrawingBoard.context.lineWidth=this.height;
        DrawingBoard.context.fill();

        DrawingBoard.context.restore();//恢复状态
        return;
    }

    if(this.type==='image'){
        DrawingBoard.context.drawImage(this.img,
            this.posX + offsetX,
            this.posY + offsetY,
            this.width,
            this.height);
    }

    DrawingBoard.context.restore();//恢复状态
}

DrawingBoard.DrawingItem.prototype.toJson=function(){
    let s=[];
    s.push('{"uuid": "'+this.uuid+'"');
    s.push(',"type": "'+this.type+'"');
    s.push(',"mimeType": "'+this.mimeType+'"');
    if((typeof this.source)=='string') s.push(',"source": "'+JSONUtil.convert(this.source)+'"');
    s.push(',"width": '+this.width);
    s.push(',"height": '+this.height);
    s.push(',"posX": '+this.posX);
    s.push(',"posY": '+this.posY);
    s.push(',"zIndex": '+this.zIndex);
    s.push(',"rotateDeg": '+this.rotateDeg);
    s.push(',"styles": {"fontSize": "'+this.styles.fontSize+'"');
    s.push(',"fontFamily": "'+this.styles.fontFamily+'"');
    s.push(',"fontColor": "'+this.styles.fontColor+'"');
    s.push(',"backgroundColor": "'+this.styles.backgroundColor+'"');
    s.push(',"fontBold": '+this.styles.fontBold);
    s.push(',"fontItalic": '+this.styles.fontItalic);
    s.push(',"vertical": '+this.styles.fontItalic);
    s.push('}');
    s.push('}');
    return s.join('');
}

DrawingBoard.DrawingItem.prototype.toData=function(index){
    return {
        index: index,
        uuid: this.uuid,
        type: this.type,
        mimeType: this.mimeType,
        source: this.source,
        blob: this.blob
    }
}

DrawingBoard.DrawingItem.prototype.destroy=function(){
    if(this.display){
        _$('DrawingBoardPanel').removeChild(this.display);
        this.display=null;
        delete this.display;
    }

    if(this.img){
        this.img=null;
        delete this.img;
    }

    if(this.source){
        this.source=null;
        delete this.source;
    }
}