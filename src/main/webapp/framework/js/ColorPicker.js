//取色器
let ColorPicker={
    callback:null,
    R:0,
    G:0,
    B:0,
    show:function(callback,initValue){
        if(callback) this.callback=callback;
        else this.callback=null;

        if(initValue){
            if(initValue.indexOf('#')==0) initValue=initValue.substring(1);
            if(initValue.length==3){
                let r=initValue.substring(0,1)+initValue.substring(0,1);
                let g=initValue.substring(1,2)+initValue.substring(1,2);
                let b=initValue.substring(2,3)+initValue.substring(2,3);
                this.R=parseInt(r,16);
                this.G=parseInt(g,16);
                this.B=parseInt(b,16);
            }else if(initValue.length=6){
                let r=initValue.substring(0,2);
                let g=initValue.substring(2,4);
                let b=initValue.substring(4,6);
                this.R=parseInt(r,16);
                this.G=parseInt(g,16);
                this.B=parseInt(b,16);
            }
        }

        if(!_$('ColorPicker')){
            let str=new Array();
            str.push('<div id="ColorPickerBg"><iframe src="about:blank" width="100%" height="100%" frameborder="0" scrolling="no"></iframe></div>');
            str.push('<div id="ColorPicker">');
            str.push('	<div id="ColorPickerSliders">');
            str.push('		<div class="ColorPickerSlider">');
            str.push('			<div class="ColorPickerSliderLeft noselect" onclick="ColorPicker.reduce(\'R\',10);">&lt;&lt;</div>');
            str.push('			<div class="ColorPickerSliderLeft noselect marginL10" onclick="ColorPicker.reduce(\'R\',1);">&lt;</div>');
            str.push('			<div class="ColorPickerSliderColor" id="ColorPickerSliderR" onclick="ColorPicker.set(event,this,\'R\');"><div class="ColorPickerSliderLine" id="ColorPickerSliderLineR" style="margin-left:'+Math.floor(this.R/2)+'px;"></div></div>');
            str.push('			<div class="ColorPickerSliderRight noselect marginL10" onclick="ColorPicker.add(\'R\',1);">&gt;</div>');
            str.push('			<div class="ColorPickerSliderRight noselect marginL10" onclick="ColorPicker.add(\'R\',10);">&gt;&gt;</div>');
            str.push('		</div>');
            str.push('		<div class="ColorPickerSlider">');
            str.push('			<div class="ColorPickerSliderLeft noselect" onclick="ColorPicker.reduce(\'G\',10);">&lt;&lt;</div>');
            str.push('			<div class="ColorPickerSliderLeft noselect marginL10" onclick="ColorPicker.reduce(\'G\',1);">&lt;</div>');
            str.push('			<div class="ColorPickerSliderColor" id="ColorPickerSliderG" onclick="ColorPicker.set(event,this,\'G\');"><div class="ColorPickerSliderLine" id="ColorPickerSliderLineG" style="margin-left:'+Math.floor(this.G/2)+'px;"></div></div>');
            str.push('			<div class="ColorPickerSliderRight noselect marginL10" onclick="ColorPicker.add(\'G\',1);">&gt;</div>');
            str.push('			<div class="ColorPickerSliderRight noselect marginL10" onclick="ColorPicker.add(\'G\',10);">&gt;&gt;</div>');
            str.push('		</div>');
            str.push('		<div class="ColorPickerSlider">');
            str.push('			<div class="ColorPickerSliderLeft noselect" onclick="ColorPicker.reduce(\'B\',10);">&lt;&lt;</div>');
            str.push('			<div class="ColorPickerSliderLeft noselect marginL10" onclick="ColorPicker.reduce(\'B\',1);">&lt;</div>');
            str.push('			<div class="ColorPickerSliderColor" id="ColorPickerSliderB" onclick="ColorPicker.set(event,this,\'B\');"><div class="ColorPickerSliderLine" id="ColorPickerSliderLineB" style="margin-left:'+Math.floor(this.B/2)+'px;"></div></div>');
            str.push('			<div class="ColorPickerSliderRight noselect marginL10" onclick="ColorPicker.add(\'B\',1);">&gt;</div>');
            str.push('			<div class="ColorPickerSliderRight noselect marginL10" onclick="ColorPicker.add(\'B\',10);">&gt;&gt;</div>');
            str.push('		</div>');
            str.push('		<div id="ColorPickerSliderPicked">&nbsp;</div>');
            str.push('	</div>');
            str.push('	<div id="ColorPickerBtns">');
            str.push('		<div class="displayBlock btnH30 w100 btnBgGreen" onclick="ColorPicker.done();">I{确定}</div>');
            str.push('		<div class="displayBlock btnH30 w100 btnBgGray mL10" onclick="ColorPicker.hide();">I{取消}</div>');
            str.push('	</div>');
            str.push('</div>');
            document.body.insertAdjacentHTML('afterBegin', Lang.convert(str.join('')));
            str=null;
            delete str;
        }

        _$('ColorPickerBg').style.height=(W.vh()-0)+'px';
        _$('ColorPickerBg').style.width='100%';
        _$('ColorPickerBg').style.top='0px';
        _$('ColorPickerBg').style.left='0px';
        _$('ColorPickerBg').style.zIndex=W.getMaxZIndex();
        _$('ColorPickerBg').style.visibility='visible';

        _$('ColorPicker').style.top=Math.ceil((W.vh()-W.elementHeight(_$('ColorPicker')))/2)+'px';
        _$('ColorPicker').style.left=Math.ceil((W.vw()-W.elementWidth(_$('ColorPicker')))/2)+'px';
        _$('ColorPicker').style.zIndex=W.getMaxZIndex();
        _$('ColorPicker').style.visibility='visible';

        _$('ColorPickerSliderPicked').style.backgroundColor='#'+this.toHex();
    },

    reduce:function(c,amount){
        if(c=='R'){
            this.R-=amount;
            if(this.R<0) this.R=0;
            _$('ColorPickerSliderLineR').style.marginLeft=Math.floor(this.R/2)+'px';
        }else if(c=='G'){
            this.G-=amount;
            if(this.G<0) this.G=0;
            _$('ColorPickerSliderLineG').style.marginLeft=Math.floor(this.G/2)+'px';
        }else if(c=='B'){
            this.B-=amount;
            if(this.B<0) this.B=0;
            _$('ColorPickerSliderLineB').style.marginLeft=Math.floor(this.B/2)+'px';
        }
        _$('ColorPickerSliderPicked').style.backgroundColor='#'+this.toHex();
    },

    add:function(c,amount){
        if(c=='R'){
            this.R+=amount;
            if(this.R>255) this.R=255;
            _$('ColorPickerSliderLineR').style.marginLeft=Math.floor(this.R/2)+'px';
        }else if(c=='G'){
            this.G+=amount;
            if(this.G>255) this.G=255;
            _$('ColorPickerSliderLineG').style.marginLeft=Math.floor(this.G/2)+'px';
        }else if(c=='B'){
            this.B+=amount;
            if(this.B>255) this.B=255;
            _$('ColorPickerSliderLineB').style.marginLeft=Math.floor(this.B/2)+'px';
        }
        _$('ColorPickerSliderPicked').style.backgroundColor='#'+this.toHex();
    },

    set:function(event,obj,c){
        let x=0;
        if(event.clientX){
            x=event.clientX;
        }else if(event.pageX){
            x=event.pageX;
        }
        x-=W.elementLeft(obj);

        if(c=='R'){
            this.R=x*2;
            if(this.R>255) this.R=255;
            _$('ColorPickerSliderLineR').style.marginLeft=Math.floor(this.R/2)+'px';
        }else if(c=='G'){
            this.G=x*2;
            if(this.G>255) this.G=255;
            _$('ColorPickerSliderLineG').style.marginLeft=Math.floor(this.G/2)+'px';
        }else if(c=='B'){
            this.B=x*2;
            if(this.B>255) this.B=255;
            _$('ColorPickerSliderLineB').style.marginLeft=Math.floor(this.B/2)+'px';
        }
        _$('ColorPickerSliderPicked').style.backgroundColor='#'+this.toHex();
    },

    toHex:function(){
        let hex='';
        let temp=this.R.toString(16);
        if(temp.length==1) temp='0'+temp;
        hex+=temp;

        temp=this.G.toString(16);
        if(temp.length==1) temp='0'+temp;
        hex+=temp;

        temp=this.B.toString(16);
        if(temp.length==1) temp='0'+temp;
        hex+=temp;

        return hex;
    },

    rgbToHex:function(rgb){
        rgb=rgb.toUpperCase();
        if(!rgb.startsWith('RGB(')) return rgb;

        if(rgb.startsWith('RGB(')) rgb=rgb.substring(4);
        if(rgb.endsWith(')')) rgb=rgb.substring(0, rgb.length-1);
        rgb=Str.replaceAll(rgb, ' ', '').split(',');
        if(rgb.length!=3) return rgb;

        let hex='';
        let temp=(rgb[0]*1).toString(16);
        if(temp.length==1) temp='0'+temp;
        hex+=temp;

        temp=(rgb[1]*1).toString(16);
        if(temp.length==1) temp='0'+temp;
        hex+=temp;

        temp=(rgb[2]*1).toString(16);
        if(temp.length==1) temp='0'+temp;
        hex+=temp;

        return '#'+hex.toUpperCase();
    },

    hide:function(){
        if(!_$('ColorPickerBg')) return;
        _$('ColorPickerBg').parentNode.removeChild(_$('ColorPickerBg'));
        _$('ColorPicker').parentNode.removeChild(_$('ColorPicker'));
    },

    done:function(){
        this.hide();
        this.callback(this.toHex());
    }
}
window.ColorPicker = ColorPicker;