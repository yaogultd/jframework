//显示/隐藏密码组件

//所有实例
let PasswordViewers=[];

/**
 *
 * @param id 组件ID
 * @param pwd 源密码input
 * @constructor
 */
function PasswordViewer(id, pwd){
    if(_$(id+'_show')) return;
    this.id=id;
    this.status='hidden';//密码默认为不显示
    this.pwd=pwd;

    //创建用于显式密码输入的input
    this.show=document.createElement('input');
    this.show.id=this.id+'_show';
    Utils.setAtt(this.show,'type','text');

    if(Utils.att(this.pwd,'placeholder')){
        Utils.setAtt(this.show,'placeholder',Utils.att(this.pwd,'placeholder'));
    }

    //复制源密码input的样式
    if(!Str.isBlank(this.pwd.className)) this.show.className=this.pwd.className;

    this.show.className='PasswordViewer';
    this.show.style.width=(W.elementWidth(this.pwd)-0)+'px';
    this.show.style.height=(W.elementHeight(this.pwd)-0)+'px';
    this.show.style.borderStyle=this.pwd.style.borderStyle;
    this.show.style.borderWidth=this.pwd.style.borderWidth;
    this.show.style.borderColor=this.pwd.style.borderColor;
    this.show.style.border=this.pwd.style.border;
    this.show.style.backgroundColor=this.pwd.style.backgroundColor;
    this.show.style.lineHeight=this.pwd.style.lineHeight;
    this.show.style.color=this.pwd.style.color;
    this.show.style.fontSize=this.pwd.style.fontSize;
    this.show.style.fontWeight=this.pwd.style.fontWeight;
    this.show.style.textAlign=this.pwd.style.textAlign;
    this.show.style.padding=this.pwd.style.padding;
    this.show.style.boxSizing=this.pwd.style.boxSizing;
    this.show.style.outline=this.pwd.style.outline;
    //复制源密码input的样式 end
    //创建用于显式密码输入的input end

    //绑定keyup事件，将显式输入框的值赋予源密码输入框
    this.show.onkeyup=function(){
        let _id=this.id.substring(0,this.id.length-5);
        let val=Str.passwordClear(PasswordViewers[_id].show.value);
        PasswordViewers[_id].show.value=val;
        PasswordViewers[_id].pwd.value=val;
    };

    this.show.style.display='none';
    this.pwd.parentNode.appendChild(this.show);


    //创建眼睛图标(css position属性必须为relative)
    this.eye=document.createElement('div');
    this.eye.id=this.id+'_eye';
    this.eye.className='PasswordViewerEye';
    this.eye.innerHTML='<div class="iconfont icon-attention_light" onclick="PasswordViewers[\''+this.id+'\'].view();"></div>';
    this.pwd.parentNode.appendChild(this.eye);

    //let align=Utils.getStyle(this.pwd.parentNode, 'textAlign');
    //console.log("align = "+align);

    //相对位置
    this.eye.style.left=(W.elementLeft(this.pwd, false)+W.elementWidth(this.pwd)-W.elementWidth(this.eye)-3)+'px';
    this.eye.style.top=(0-W.elementHeight(this.eye)-Math.floor((W.elementHeight(this.pwd) - W.elementHeight(this.eye))/2))+'px';
    //创建眼睛图标 end

    PasswordViewers[this.id]=this;
}

PasswordViewer.prototype.resize=function (){
    this.eye.style.left=(W.elementLeft(this.pwd, false)+W.elementWidth(this.pwd)-W.elementWidth(this.eye)-3)+'px';
    this.eye.style.top=(0-W.elementHeight(this.eye)-Math.floor((W.elementHeight(this.pwd) - W.elementHeight(this.eye))/2))+'px';
}

PasswordViewer.prototype.view=function(){
    if(this.status=='hidden'){
        this.pwd.style.display='none';
        this.show.style.display='';
        this.show.value=this.pwd.value;
        this.show.focus();

        _$(this.id+'_eye').className='PasswordViewerEye red';

        this.status='visible'
    }else{
        this.show.style.display='none';
        this.pwd.style.display='';
        this.pwd.value=this.show.value;
        this.pwd.focus();

        _$(this.id+'_eye').className='PasswordViewerEye color999';

        this.status='hidden'
    }
}