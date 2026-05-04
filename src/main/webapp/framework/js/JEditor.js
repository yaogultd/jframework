//图文编辑器
let JEditor={
    elements:[],
    container:null,
    operator:null,//seller,manage,usr
    current:0,
    titleOrContent:'',
    textTitleStyle:'',
    textContentStyle:'',
    pickColorFor:'',//Bg,Color
    stylePicker:[],
    currentTextContent:'',
    elementStyle:null,//即将添加的元素的样式
    paddingTopDefault: 0,
    paddingBottomDefault: 0,

    setBgColor:function(bgColor){
        console.log('set bgColor '+this.container.id+','+bgColor);
        this.container.style.backgroundColor=bgColor;
    },

    init:function(_container, operator, paddingTopDefault, paddingBottomDefault){
        if(_$('JEditorPanel')) return;

        if((typeof _container)=='string') this.container=_$(_container);
        else this.container=_container;
        this.container.onmouseover=function(){
            if(_$('JEditorPanel').style.visibility=='hidden'){
                JEditor.over();
            }
        };
        this.operator=operator;
        this.current=0;

        //文本样式设置对话框
        this.stylePicker=new Array();
        this.stylePicker.push('  <div id="JEditorFontSizeCssStyleBox" class="alignL">');
        this.stylePicker.push('    <div class="r mT10">');
        this.stylePicker.push('      	<div class="fl mL20">I{JEditor,字体}</div>');
        this.stylePicker.push('      	<div class="fl mL3">');
        this.stylePicker.push('      		<select id="JEditorFontFamily">');
        this.stylePicker.push('      			<option value="">I{JEditor,默认}</option>');
        this.stylePicker.push('      			<option value="宋体">I{JEditor,宋体}</option>');
        this.stylePicker.push('      			<option value="微软雅黑">I{JEditor,微软雅黑}</option>');
        this.stylePicker.push('      		</select>');
        this.stylePicker.push('      	</div>');
        this.stylePicker.push('    </div>');
        this.stylePicker.push('    <div class="r mT10">');
        this.stylePicker.push('      	<div class="fl mL20">I{JEditor,字体大小}</div>');
        this.stylePicker.push('      	<div class="fl mL3">');
        this.stylePicker.push('      		<select id="JEditorFontSize">');
        this.stylePicker.push('      			<option value="10">10PX</option>');
        this.stylePicker.push('      			<option value="11">11</option>');
        this.stylePicker.push('      			<option value="12" selected="selected">12PX</option>');
        this.stylePicker.push('      			<option value="13">13PX</option>');
        this.stylePicker.push('      			<option value="14">14PX</option>');
        this.stylePicker.push('      			<option value="15">15PX</option>');
        this.stylePicker.push('      			<option value="16">16PX</option>');
        this.stylePicker.push('      			<option value="17">17PX</option>');
        this.stylePicker.push('      			<option value="18">18PX</option>');
        this.stylePicker.push('      			<option value="20">20PX</option>');
        this.stylePicker.push('      			<option value="22">22PX</option>');
        this.stylePicker.push('      			<option value="24">24PX</option>');
        this.stylePicker.push('      			<option value="26">26PX</option>');
        this.stylePicker.push('      			<option value="28">28PX</option>');
        this.stylePicker.push('      			<option value="30">30PX</option>');
        this.stylePicker.push('      		</select>');
        this.stylePicker.push('      	</div>');
        this.stylePicker.push('    </div>');
        this.stylePicker.push('    <div class="r mT10">');
        this.stylePicker.push('      	<div class="fl mL20">I{JEditor,背景颜色}</div>');
        this.stylePicker.push('      	<div class="fl mL3 w60" id="JEditorBgShow">&nbsp;<input type="hidden" id="JEditorBg"/></div>');
        this.stylePicker.push('      	<div class="fl mL10"><a href="javascript:_void();" onclick="JEditor.colorPick(\'Bg\');">I{JEditor,取色}</a></div>');
        this.stylePicker.push('    </div>');
        this.stylePicker.push('    <div class="r mT10">');
        this.stylePicker.push('      	<div class="fl mL20">I{JEditor,字体颜色}</div>');
        this.stylePicker.push('      	<div class="fl mL3 w60" id="JEditorColorShow">&nbsp;<input type="hidden" id="JEditorColor"/></div>');
        this.stylePicker.push('      	<div class="fl mL10"><a href="javascript:_void();" onclick="JEditor.colorPick(\'Color\');">取色</a></div>');
        this.stylePicker.push('    </div>');
        this.stylePicker.push('    <div class="r mT10">');
        this.stylePicker.push('      	<div class="fl mL20">I{JEditor,是否加粗}</div>');
        this.stylePicker.push('      	<div class="fl mL3">');
        this.stylePicker.push('      		<select id="JEditorFontWeight">');
        this.stylePicker.push('      			<option value="">I{JEditor,默认}</option>');
        this.stylePicker.push('      			<option value="bold">I{JEditor,加粗}</option>');
        this.stylePicker.push('      		</select>');
        this.stylePicker.push('      	</div>');
        this.stylePicker.push('    </div>');
        this.stylePicker.push('    <div class="r alignC mT20" style="display:inline-block;">');
        this.stylePicker.push('      <div class="btnLong">');
        this.stylePicker.push('        <input type="button" value="I{JEditor,确定}" onclick="top.Dialog.win.JEditor.doneStylePicker(top._$(\'JEditorFontFamily\').value,top._$(\'JEditorFontSize\').value,top._$(\'JEditorBg\').value,top._$(\'JEditorColor\').value,top._$(\'JEditorFontWeight\').value);"/>');
        this.stylePicker.push('      </div>');
        this.stylePicker.push('      <div class="btnLongLight mL10">');
        this.stylePicker.push('        <input type="button" value="I{JEditor,关闭}" onclick="top.Dialog.close(); top.ColorPicker.hide();"/>');
        this.stylePicker.push('      </div>');
        this.stylePicker.push('    </div>');
        this.stylePicker.push('    <div class="r" style="height:10px;"></div>');
        this.stylePicker.push('  </div>');


        if((typeof paddingTopDefault)=='number') this.paddingTopDefault=paddingTopDefault;
        if((typeof paddingBottomDefault)=='number') this.paddingBottomDefault=paddingBottomDefault;

        //操作面板（插入、删除等按钮）
        let panel='<div id="JEditorPanel">';
        panel+='<div class="JEditorBtn JEditorAddImg" title="I{JEditor,插入图片}" onclick="JEditor.showAddImg();"></div>';
        panel+='<div class="JEditorBtn JEditorAddTxt" title="I{JEditor,插入文字}" onclick="JEditor.showAddTxt();"></div>';
        panel+='<div class="JEditorBtn JEditorForword" title="I{JEditor,上移}" onclick="JEditor.moveForward();"></div>';
        panel+='<div class="JEditorBtn JEditorBackward" title="I{JEditor,下移}" onclick="JEditor.moveBackward();"></div>';
        panel+='<div class="JEditorBtn JEditorDel" title="I{JEditor,删除}" onclick="JEditor.del();"></div>';
        panel+='<div class="JEditorBtn JEditorClear" title="I{JEditor,清空}" onclick="JEditor.clear();"></div>';
        panel+='</div>';
        document.body.insertAdjacentHTML('afterbegin', Lang.convert(panel));

        //文本录入框
        panel='<div id="JEditorText">';
        panel+='<div class="JEditorTextTitle"><div class="fl"><input type="text" id="JEditorTextTitle" placeholder="I{JEditor,标题（可不填）}"/></div> <div class="fr mR3"><a href="javascript:_void();" onclick="JEditor.showStylePicker(\'title\');">I{JEditor,设置标题样式}</a></div></div>'
        panel+='<div class="JEditorTextContent">'
        panel+='	<textarea id="JEditorTextContent" placeholder="I{JEditor,正文}"></textarea>'
        panel+='</div>'
        panel+='<div class="JEditorTextContentStyle"><a href="javascript:_void();" onclick="JEditor.showStylePicker(\'content\');">I{JEditor,设置正文样式}</a></div>';
        panel+='<div class="JEditorTextBtns">';
        panel+='<div class="fl btnH24Gray displayBlock" style="width:45%;" onclick="JEditor.inputCancel();">I{JEditor,取消}</div>';
        panel+='<div class="fr btnH24Gray btnBgGreen displayBlock" style="width:45%;" onclick="JEditor.inputText();">I{JEditor,添加}</div>';
        panel+='</div>';
        panel+='</div>';
        document.body.insertAdjacentHTML('afterBegin', panel);

        panel=null;
        delete panel;
    },
    showAddImg:function(){
        this.inputCancel();
        let layer=Layers.open(window, 'I{JEditor,选择图片}','/'+this.operator+'/album.htm?multi=true');
        layer.onAction=JEditor.inputImages;
        
        //图片上下边距设置
        let settings=[];
        settings.push('<div class="fl"><input type="number" id="JEditorElementPaddingTop" value="" placeholder="I{JEditor,上边距}"/></div>');
        settings.push('<div class="fl mL10"><input type="number" id="JEditorElementPaddingBottom" value="" placeholder="I{JEditor,下边距}"/></div>');
        layer.setBtns(settings.join(''));
    },
    showAddTxt:function(){
        this.inputCancel();
        _$('JEditorText').style.top=this.dialogTop(_$('JEditorText'))+'px';
        _$('JEditorText').style.left=Math.floor((W.vw()-W.elementWidth(_$('JEditorText')))/2)+'px';
        _$('JEditorText').style.visibility='visible';
        _$('JEditorTextContent').value=this.currentTextContent;
    },

    /**
     * @param dialog
     * @param offset
     * @returns {*}
     */
    dialogTop:function(dialog,offset){
        let theTop=0;
        if(location.href==top.location.href){
            theTop=W.tTotal()+Math.round((top.W.vh()-W.elementHeight(dialog))/2);
        }else{
            if(!offset||offset==0){
                if(top._$('header')) offset=top.W.elementHeight(top._$('header'));
                else offset=0;
            }

            if(top._$('layer')){
                theTop=Layer.scrollTop();
            }else{
                theTop=top.W.t()-offset;
            }

            if(theTop<=0) theTop=0;
            theTop+=50;
        }

        if(theTop<Loading.topMin) theTop=Loading.topMin;
        return theTop;
    },
    inputCancel:function(){
        _$('JEditorTextContent').style.fontSize='12px';
        _$('JEditorTextContent').style.backgroundColor='';
        _$('JEditorTextContent').style.color='';
        _$('JEditorTextContent').style.fontWeight='normal';
        _$('JEditorText').style.visibility='hidden';
        _$('JEditorImage').style.visibility='hidden';
    },
    inputText:function(){
        _$('JEditorTextContent').value=Str.trimAll(_$('JEditorTextContent').value);
        if(_$('JEditorTextContent').value==''){
            JEditor.inputCancel();
            return;
        }
        _$('JEditorTextContent').value=Str.replaceAll(_$('JEditorTextContent').value,'\n','<br/>');
        let element=new JEditorElement(0,'text',_$('JEditorTextTitle').value,JEditor.textTitleStyle,_$('JEditorTextContent').value,JEditor.textContentStyle,'','',0,0,paddingTop,paddingBottom);
        JEditor.insert(element);
        JEditor.inputCancel();
    },
    inputImages:function(ret, multi){
        let paddingTop=0;
        let paddingBottom=0;
        if(!Str.isBlank(top._$('JEditorElementPaddingTop').value) && (typeof top._$('JEditorElementPaddingTop').value*1)=='number') paddingTop=top._$('JEditorElementPaddingTop').value*1;
        if(!Str.isBlank(top._$('JEditorElementPaddingBottom').value) && (typeof top._$('JEditorElementPaddingBottom').value*1)=='number') paddingBottom=top._$('JEditorElementPaddingBottom').value*1;

        if(multi&&multi=='multi'){
            for(let i=0;i<ret.length;i++){
                let src=ret[i][4];
                let w=W.elementWidth(JEditor.container);
                let h=(!ret[i][15])?w:(w*(ret[i][16]/ret[i][15]));
                h=Math.floor(h);
                let element=new JEditorElement(0,'image','','','','',src,ret[i][6],w,h,paddingTop,paddingBottom);
                JEditor.insert(element);
            }
        }else{
            let src=ret[4];
            let w=W.elementWidth(JEditor.container);
            let h=(!ret[15])?w:(w*(ret[16]/ret[15]));
            h=Math.floor(h);
            let element=new JEditorElement(0,'image','','','','',src,ret[6],w,h,paddingTop,paddingBottom);
            JEditor.insert(element);
        }
        JEditor.inputCancel();
    },
    showStylePicker:function(titleOrContent){
        this.titleOrContent=titleOrContent;
        top.Dialog.noTitle=true;
        top.Dialog.open(-1,-1,300,-1,null,null,window,'dialog');
        top.Dialog.setTitle('I{JEditor,设置样式}');
        top.Dialog.setMsg(Lang.convert(this.stylePicker.join('')));
    },
    hideStylePicker:function(){
        top.Dialog.close();
    },
    doneStylePicker:function(family,size,bg,color,bold){
        top.Dialog.close();
        var st='';
        if(size!='12') st+='font-size:'+size+'px;';
        if(bg!='') st+='background-color:'+bg;
        if(color!='') st+='color:'+color;
        if(bold!='') st+='font-weight:bold;';

        if(this.titleOrContent=='title'){
            JEditor.textTitleStyle=st;
            if(size!='12') _$('JEditorTextTitle').style.fontSize=size+'px';
            if(bg!='') _$('JEditorTextTitle').style.backgroundColor=bg;
            if(color!='') _$('JEditorTextTitle').style.color=color;
            if(bold!='') _$('JEditorTextTitle').style.fontWeight='bold';
        }else{
            JEditor.textContentStyle=st;
            if(size!='12') _$('JEditorTextContent').style.fontSize=size+'px';
            if(bg!='') _$('JEditorTextContent').style.backgroundColor=bg;
            if(color!='') _$('JEditorTextContent').style.color=color;
            if(bold!='') _$('JEditorTextContent').style.fontWeight='bold';
        }
    },
    colorPick:function(_for){
        JEditor.pickColorFor=_for;
        top.ColorPicker.show(JEditor.colorPicked,'');
    },
    colorPicked:function(c){
        top._$('JEditor'+JEditor.pickColorFor).value='#'+c;
        top._$('JEditor'+JEditor.pickColorFor+'Show').style.backgroundColor='#'+c;
    },
    over:function(index){
        if(!_$('JEditorPanel')) return;//尚未初始化
        if(!JEditor.operator || (JEditor.operator!='manage' && JEditor.operator!='seller')) return;
        let relative=null;
        if((typeof index)!='undefined'){
            this.current=index;
            relative=_$('JEditor_element_'+index);
        }
        if(!relative) relative=this.container;
        _$('JEditorPanel').style.top=(W.elementTop(relative, true, null)+Math.floor(W.elementHeight(relative)/2)-20)+'px';
        _$('JEditorPanel').style.left=(W.elementLeft(relative, true, null)+Math.floor((W.elementWidth(relative)-W.elementWidth(_$('JEditorPanel')))/2))+'px';
        _$('JEditorPanel').style.zIndex=W.getMaxZIndex();
        _$('JEditorPanel').style.visibility='visible';
    },
    out:function(){
        if(!_$('JEditorPanel')) return;//尚未初始化
        _$('JEditorPanel').style.visibility='hidden';
    },
    before:function(index){
        for(let i=index-1;i>=0;i--){
            if(this.elements[i]) return this.elements[i];
        }
        return null;
    },
    after:function(index){
        for(let i=index+1;i<this.elements.length;i++){
            if(this.elements[i]) return this.elements[i];
        }
        return null;
    },
    find:function(index){
        return this.elements[index];
    },
    insert:function(element){
        let currentElement=this.find(this.current);
        if(currentElement){
            element.index=this.current+1;
        }else{
            element.index=this.elements.length;
        }

        let next=-1;

        //保存新节点到指定位置并调整插入位置之后节点的ID
        let temp=[];
        for(let i=0;i<this.elements.length&&i<element.index;i++){
            temp.push(this.elements[i]);
        }
        temp.push(element);
        for(let i=element.index;i<this.elements.length;i++){
            if(this.elements[i]){
                let oldIndex=this.elements[i].index;
                let newIndex=oldIndex+1+10000;

                if(next<0) next=newIndex;

                //+10000，避免当前节点与后续节点id重复
                this.elements[i].reindex(_$('JEditor_element_'+oldIndex),newIndex);
                this.elements[i].index=newIndex;
                temp.push(this.elements[i]);
            }else{
                temp[temp.length]=null;
            }
        }
        this.elements=temp;
        //保存新节点到指定位置并调整插入位置之后节点的ID  end

        if(next>-1){
            this.container.insertBefore(element.toHtml(),_$('JEditor_element_'+next));
        }else{
            this.container.appendChild(element.toHtml());
        }

        //将id+10000的节点恢复原本值
        for(let i=0;i<this.elements.length;i++){
            if(this.elements[i]&&this.elements[i].index>10000){
                let oldIndex=this.elements[i].index;
                let newIndex=oldIndex-10000;

                this.elements[i].reindex(_$('JEditor_element_'+oldIndex),newIndex);
                this.elements[i].index=newIndex;
            }
        }

        this.current=element.index;

        this.over(this.current);
    },
    moveForward:function(){
        let currentElement=this.find(this.current);
        if(!currentElement) return;
        if(!this.before(this.current)) return;

        let beforeElement=this.before(this.current);
        let beforeIndex=beforeElement.index;

        let e=_$('JEditor_element_'+currentElement.index);
        this.container.removeChild(e);

        let eBefore=_$('JEditor_element_'+beforeElement.index);
        e=currentElement.reindex(e,beforeIndex);
        eBefore=beforeElement.reindex(eBefore,currentElement.index);

        this.container.insertBefore(e,eBefore);

        beforeElement.index=currentElement.index;
        currentElement.index=beforeIndex;

        this.elements[currentElement.index]=currentElement;
        this.elements[beforeElement.index]=beforeElement;

        this.current=currentElement.index;
        this.over(this.current);
    },
    moveBackward:function(){
        let currentElement=this.find(this.current);
        if(!currentElement) return;
        if(!this.after(this.current)) return;

        let afterElement=this.after(this.current);
        let afterIndex=afterElement.index;

        let e=_$('JEditor_element_'+currentElement.index);
        this.container.removeChild(e);

        let eAfter=_$('JEditor_element_'+afterElement.index);
        e=currentElement.reindex(e,afterIndex);
        eAfter=afterElement.reindex(eAfter,currentElement.index);

        Utils.insertAfter(e,eAfter);

        afterElement.index=currentElement.index;
        currentElement.index=afterIndex;

        this.elements[currentElement.index]=currentElement;
        this.elements[afterElement.index]=afterElement;

        this.current=currentElement.index;
        this.over(this.current);
    },
    del:function(){
        let element=_$('JEditor_element_'+this.current);
        if(element){
            this.container.removeChild(element);
            this.elements[this.current]='';
            if(this.after(this.current)) this.current=this.after(this.current).index;
            else if(this.before(this.current)) this.current=this.before(this.current).index;
            else this.current=0;
        }
        this.over(this.current);
    },
    clear:function(){
        for(let i=0; i<1000; i++){
            let element=_$('JEditor_element_'+i);
            if(element) this.container.removeChild(element);
        }
        this.elements=[];
        this.current=0;
        this.over(this.current);
    },
    toJson:function(){
        let json=[];
        for(let i=0;i<this.elements.length;i++){
            if(this.elements[i]){
                json.push(this.elements[i].toJson());
                json.push(',');
            }
        }
        let _json=json.join('');
        if(_json.endsWith(',')) _json=_json.substring(0,_json.length-1);

        json=[];
        json.push('{"elements":[');
        json.push(_json);
        json.push(']}');
        _json=null;

        return json.join('');
    },
    fromJson:function(_json,withLoading){
        if(withLoading) _$('JEditorPanel').style.visibility='hidden';
        this.elements=[];
        this.current=0;

        if(_json==='') return;
        let json=(typeof _json)=='string' ? JSONUtil.parse(_json) : _json;
        let elements=json.elements;
        for(let i=0;i<elements.length;i++){
            if(elements[i]){
                let img=JSONUtil.de(elements[i].img);
                let cover=JSONUtil.de(elements[i].cover);
                if(img.indexOf('photoLoading.gif')>-1) continue;//兼容旧版本的商品信息，去掉其中的"加载中"图片

                let w=W.elementWidth(JEditor.container);
                let h=(!elements[i].width)?w:(w*(elements[i].height*1/elements[i].width*1));
                h=Math.floor(h);

                let element=new JEditorElement(i,
                    elements[i].type,
                    JSONUtil.de(elements[i].title),
                    JSONUtil.de(elements[i].titleStyle),
                    JSONUtil.de(elements[i].content),
                    JSONUtil.de(elements[i].contentStyle),
                    img,
                    cover,
                    w,
                    h);
                element.index=this.elements.length;
                if(withLoading) this.container.appendChild(element.toHtmlWithLoading());
                else this.container.appendChild(element.toHtml());

                this.elements.push(element);
            }
        }

        _json=null;
        delete _json;

        json=null;
        delete json;
    }
}

//type: image or text
function JEditorElement(index,type,title,titleStyle,content,contentStyle,img,cover,width,height,paddingTop,paddingBottom){
    this.index=index;
    this.type=type;
    this.title=title;
    this.titleStyle=titleStyle;
    this.content=content;
    this.contentStyle=contentStyle;
    this.img=decodeURIComponent(img);
    this.cover=decodeURIComponent(cover);
    this.width=width;
    this.paddingTop=(typeof(paddingTop)=='number' && paddingTop>0) ? paddingTop : JEditor.paddingTopDefault;
    this.paddingBottom=(typeof(paddingBottom)=='number' && paddingBottom>0) ? paddingBottom : JEditor.paddingBottomDefault;
}
JEditorElement.prototype.toJson=function(){
    let json=[];
    json.push('{');
    json.push('"index":"'+this.index+'",');
    json.push('"type":"'+this.type+'",');
    json.push('"title":"'+Str.string2IntSequence(this.title)+'",');
    json.push('"titleStyle":"'+Str.string2IntSequence(this.titleStyle)+'",');
    json.push('"content":"'+Str.string2IntSequence(this.content)+'",');
    json.push('"contentStyle":"'+Str.string2IntSequence(this.contentStyle)+'",');
    json.push('"img":"'+Str.string2IntSequence(this.img)+'",');
    json.push('"cover":"'+(this.cover?Str.string2IntSequence(this.cover):'')+'",');
    json.push('"width":'+(this.width?this.width:0)+',');
    json.push('"height":'+(this.height?this.height:0));
    json.push('"paddingTop":'+(this.paddingTop?this.paddingTop:0)+',');
    json.push('"paddingBottom":'+(this.paddingBottom?this.paddingBottom:0));
    json.push('}');
    return json.join('');
}
JEditorElement.prototype.toHtml=function(){//用于编辑时
    let html=document.createElement('div');
    html.id='JEditor_element_'+this.index;
    html.style.cssText = this.contentStyle;
    html.style.paddingTop=(this.index==0?0:this.paddingTop)+'px';
    html.style.paddingBottom=(this.index==0?0:this.paddingBottom)+'px';
    Utils.setAtt(html, 'index', this.index);

    console.log("paddingTop => "+(this.index==0?0:this.paddingTop));

    html.addEventListener('mouseover',function(event){
        let target=Utils.getEventTarget(event);
        if(!target) return;

        let index=Utils.att(target, 'index');
        if(!index) return;
        JEditor.over(index*1);
    },false);
    html.addEventListener('click',function(event){
        let target=Utils.getEventTarget(event);
        if(!target) return;

        let index=Utils.att(target, 'index');
        if(!index) return;
        JEditor.over(index*1);
    },false);

    if(this.type=='text'){
        html.className = 'JEditor_text';
        if(!Str.isBlank(this.title)){
            let title=document.createElement('div');
            title.className='JEditor_text_title';
            title.innerHTML='<div style="'+this.titleStyle+'" onmouseover="JEditor.over('+this.index+');" onclick="JEditor.over('+this.index+');"><div class="theTitle">'+this.title+'</div></div>';
            html.appendChild(title);
        }

        let content=document.createElement('div');
        content.className='JEditor_text_content';
        content.innerHTML='<div style="'+this.contentStyle+'" onmouseover="JEditor.over('+this.index+');" onclick="JEditor.over('+this.index+');"><div class="theText">'+this.content+'</div></div>';
        html.appendChild(content);
    }else {
        html.className = 'JEditor_image';
        if (this.img.toLowerCase().endsWith('.mp4')
            || this.img.toLowerCase().endsWith('.mov')
            || this.img.toLowerCase().endsWith('.3gp')) {
            html.innerHTML='<video id="'+html.id+'_player" preload="auto" playsinline webkit-playsinline></video>';
            Players.addPlayer(html.id+'_player',
                this.img,
                false,
                this.width,
                this.height,
                false,
                false,
                true,
                this.cover,
                false,
                'H');
            Players.setMaxWidth(html.id+'_player', 0);
            Players.setMaxHeight(html.id+'_player', 0);
            Players.initPlayers();
        } else {
            html.innerHTML = '<img src="' + this.img + '" onmouseover="JEditor.over(' + this.index + ');" onclick="JEditor.over(' + this.index + ');"/>';
        }
    }
    return html;
}
JEditorElement.prototype.toHtmlWithLoading=function(){//用于前端展示
    let html=document.createElement('div');
    html.id='JEditor_element_'+this.index;
    html.style.cssText = this.contentStyle;
    html.style.paddingTop=this.paddingTop+'px';
    html.style.paddingBottom=this.paddingBottom+'px';
    Utils.setAtt(html, 'index', this.index);

    console.log("paddingTop => "+(this.index==0?0:this.paddingTop));

    html.addEventListener('mouseover',function(event){
        let target=Utils.getEventTarget(event);
        if(!target) return;

        let index=Utils.att(target, 'index');
        if(!index) return;
        JEditor.over(index*1);
    },false);
    html.addEventListener('click',function(event){
        let target=Utils.getEventTarget(event);
        if(!target) return;

        let index=Utils.att(target, 'index');
        if(!index) return;
        JEditor.over(index*1);
    },false);

    if(this.type=='text'){
        html.className = 'JEditor_text';
        if(!Str.isBlank(this.title)){
            let title=document.createElement('div');
            title.className='JEditor_text_title';
            title.innerHTML='<div style="'+this.titleStyle+'" onmouseover="JEditor.over('+this.index+');" onclick="JEditor.over('+this.index+');"><div class="theTitle">'+this.title+'</div></div>';
            html.appendChild(title);
        }

        let content=document.createElement('div');
        content.className='JEditor_text_content';
        content.innerHTML='<div style="'+this.contentStyle+'" onmouseover="JEditor.over('+this.index+');" onclick="JEditor.over('+this.index+');"><div class="theText">'+this.content+'</div></div>';
        html.appendChild(content);
    }else{
        html.className='JEditor_image';
        if(this.img.toLowerCase().endsWith('.mp4')
            ||this.img.toLowerCase().endsWith('.mov')
            ||this.img.toLowerCase().endsWith('.3gp')){
            html.innerHTML='<video id="'+html.id+'_player" preload="auto" playsinline webkit-playsinline></video>';
            Players.addPlayer(html.id+'_player',
                this.img,
                false,
                this.width,
                this.height,
                false,
                false,
                true,
                this.cover,
                false,
                'H');
            Players.setMaxWidth(html.id+'_player', 0);
            Players.setMaxHeight(html.id+'_player', 0);
            Players.initPlayers();
        }else{
            html.innerHTML='<img id="JEditor_image_'+this.index+'_loading" src="/img/photoLoading.gif"/><img id="JEditor_image_'+this.index+'" _src="'+this.img+'" style="display:none;" onclick="top.ImageViewer.open(window,null,this.src);"/>';
        }
    }
    return html;
}
JEditorElement.prototype.reindex=function(html,newIndex){//用于编辑时
    html.id='JEditor_element_'+newIndex;
    html.style.cssText = this.contentStyle;
    html.style.paddingTop=this.paddingTop+'px';
    html.style.paddingBottom=this.paddingBottom+'px';
    html.innerHTML='';
    Utils.setAtt(html, 'index', newIndex);
    html.addEventListener('mouseover',function(event){
        let target=Utils.getEventTarget(event);
        if(!target) return;

        let index=Utils.att(target, 'index');
        if(!index) return;
        JEditor.over(index*1);
    },false);
    html.addEventListener('click',function(event){
        let target=Utils.getEventTarget(event);
        if(!target) return;

        let index=Utils.att(target, 'index');
        if(!index) return;
        JEditor.over(index*1);
    },false);

    if(this.type=='text'){
        html.className = 'JEditor_text';
        if(!Str.isBlank(this.title)){
            let title=document.createElement('div');
            title.className='JEditor_text_title';
            title.innerHTML='<div style="'+this.titleStyle+'" onmouseover="JEditor.over('+newIndex+');" onclick="JEditor.over('+newIndex+');"><div class="theTitle">'+this.title+'</div></div>';
            html.appendChild(title);
        }

        let content=document.createElement('div');
        content.className='JEditor_text_content';
        content.innerHTML='<div style="'+this.contentStyle+'" onmouseover="JEditor.over('+newIndex+');" onclick="JEditor.over('+newIndex+');"><div class="theText">'+this.content+'</div></div>';
        html.appendChild(content);
    }else{
        html.className='JEditor_image';
        if(this.img.toLowerCase().endsWith('.mp4')
            ||this.img.toLowerCase().endsWith('.mov')
            ||this.img.toLowerCase().endsWith('.3gp')){
            html.innerHTML='<video id="'+html.id+'_player" preload="auto" playsinline webkit-playsinline></video>';
            Players.addPlayer(html.id+'_player',
                this.img,
                false,
                this.width,
                this.height,
                false,
                false,
                true,
                this.cover,
                false,
                'H');
            Players.setMaxWidth(html.id+'_player', 0);
            Players.setMaxHeight(html.id+'_player', 0);
            Players.initPlayers();
        }else{
            html.innerHTML='<img src="'+this.img+'" onmouseover="JEditor.over('+newIndex+');" onclick="JEditor.over('+newIndex+');"/>';
        }
    }

    return html;
}
//商品详情编辑器 END////////////////////////////