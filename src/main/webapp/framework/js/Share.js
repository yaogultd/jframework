let Share={
    sharedImage: '',
    sharedTitle: '',
    sharedDesc: '',
    sharedLink: '',
    spreaderHeader: '',//分享者头像
    spreaderNickname: '',//分享者昵称
    posterWidth: 600,
    posterHeight: 800,

    /**
     * 格式化分享链接
     * @param link
     * @returns {*}
     */
    formatLink:function(link){
        let paras=Str.getParams(link);
        if(link.indexOf('?')>0) link=link.substring(0, link.indexOf('?'));
        for(let i in paras){
            if(i.indexOf('thirdparty_')>-1) continue;//去掉第三方登录信息
            if(i=='referer') continue;//去掉原有referer
            if(i=='MERCHANT_NUM') continue;//去掉原有MERCHANT_NUM
            if(Str.isBlank(paras[i])) continue;//去掉参数值为空的

            if(link.indexOf('?')<0) link+='?'+i+'='+encodeURIComponent(paras[i]);
            else link+='&'+i+'='+encodeURIComponent(paras[i]);
        }

        if(Auth.profile && !Str.isBlank(Auth.profile.spreaderCode)){
            if(link.indexOf('?')>0) link+='&referer='+Auth.profile.spreaderCode
            else link+='?referer='+Auth.profile.spreaderCode;
        }

        if(!Str.isBlank(Fields.MERCHANT_NUM)){
            if(link.indexOf('?')>0) link+='&MERCHANT_NUM='+Fields.MERCHANT_NUM;
            else link+='?MERCHANT_NUM='+Fields.MERCHANT_NUM;
        }

        if(Auth.profile && !Str.isBlank(Auth.profile.weixinMiniprogramAppID)){//直接跳转小程序的链接
            link=UserAgent.httpScheme+'://'+UserAgent.currentDomain+'/to_mini_program_'+Auth.profile.weixinMiniprogramAppID+'/'+link;
        }

        return link;
    },

    /**
     * 初始化
     * @param _sharedImage
     * @param _sharedTitle
     * @param _sharedDesc
     * @param _sharedLink
     * @param _spreaderHeader
     * @param _spreaderNickname
     */
    init:function(_sharedImage,_sharedTitle,_sharedDesc,_sharedLink, _spreaderHeader, _spreaderNickname){
        if((UserAgent.getUserAgentType()==UserAgent.UA_WECHAT || UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI)
            && window!=top){//微信需初始化调用顶层页面
            top.Share.init(_sharedImage,_sharedTitle,_sharedDesc,_sharedLink);
            return;
        }

        if(!Str.isBlank(_sharedImage) && _sharedImage.indexOf('http') != 0){
            _sharedImage=UserAgent.httpScheme+'://'+UserAgent.currentDomain+_sharedImage;
        }

        this.posterWidth=UserAgent.isPC()?600:top.W.vw();
        this.posterHeight=UserAgent.isPC()?800:(top.W.vh()-41);
        this.imgWidth=this.posterWidth;
        this.imgHeight=this.posterWidth;

        this.sharedImage=_sharedImage;
        this.sharedTitle=_sharedTitle;
        this.sharedDesc=_sharedDesc;
        this.sharedLink=this.formatLink(_sharedLink);
        this.spreaderHeader=_spreaderHeader;
        this.spreaderNickname=_spreaderNickname;

        if(Str.isBlank(this.spreaderHeader)){
            let _spreaderHeader='';
            if(top.Auth.profile && top.Auth.profile.userAvatar){
                _spreaderHeader=top.Auth.profile.userAvatar;
            }
            this.spreaderHeader=_spreaderHeader;
        }

        if(Str.isBlank(this.spreaderNickname)){
            let _spreaderNickname='';
            if(top.Auth.profile && top.Auth.profile.userDisplayName){
                _spreaderNickname=top.Auth.profile.userDisplayName;
            }
            this.spreaderNickname=_spreaderNickname;
        }

        Auth.initThirdparty();
    },

    /**
     * 分享链接
     */
    shareLink:function(){
        if((UserAgent.getUserAgentType()==UserAgent.UA_WECHAT || UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI)
            && window!=top){//微信需初始化调用顶层页面
            top.Share.shareLink();
            return;
        }

        let htm=[];
        htm.push('<div class="alignC">');
        htm.push('	<div class="r mT10 font14px alignL" style="white-space:normal; word-break:break-all;" id="shared_link_show">');
        if(!Str.isBlank(this.spreaderNickname)){
            htm.push(this.spreaderNickname);
            htm.push(' I{share,向您推荐}');
        }else{
            htm.push('I{share,我向您推荐}');
        }
        htm.push(Lang.getCurrentLang().id=='cn' ? ' 【' : ' ＂');
        htm.push(this.sharedTitle);
        htm.push(Lang.getCurrentLang().id=='cn' ? '】 ' : '＂ ');
        htm.push(this.sharedDesc);
        htm.push('<br>');
        htm.push(this.sharedLink);
        htm.push('  </div>');
        htm.push('	<div class="r mT20">');
        htm.push('		<div id="shared_link_copy" class="btnH40 displayBlock w200">I{share,复制链接}</div>');
        htm.push('	</div>');
        htm.push('</div>');

        top.Dialog.open(-1,-1,-1,-1,null,null,window,'dialog','I{share,分享}');
        top.Dialog.setContent(htm.join(''));
        htm=null;
        delete htm;

        top._$('shared_link_copy').addEventListener('click', function(e){
            let url = Str.replaceAll(_$('shared_link_show').innerHTML, '<br>', '\r\n');
            url = Str.replaceAll(url, '&amp;', '&');
            Copy.copy(url);
        });
    },

    /**
     * 分享海报
     */
    sharePoster:function(imgWidth, imgHeight){
        if((UserAgent.getUserAgentType()==UserAgent.UA_WECHAT || UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI)
            && window!=top){//微信需初始化调用顶层页面
            top.Share.sharePoster(imgWidth, imgHeight);
            return;
        }

        if(imgWidth) this.imgWidth=imgWidth;
        if(imgHeight) this.imgHeight=imgHeight;
        let units=[];//海报元素

        //大图
        if(!Str.isBlank(this.sharedImage)){
            units.push(new ImageMergerUnit({
                type: 'image',
                width: this.imgWidth,
                height: this.imgHeight,
                source: this.sharedImage
            }));
        }

        //标题
        if(!Str.isBlank(this.sharedTitle)){
            units.push(new ImageMergerUnit({
                type: 'text',
                width: this.posterWidth - 20,
                height: 20,
                source: this.sharedTitle,
                fontColor: '#000',
                fontStyle: 'bold 16px',
                offsetX: 10,
                offsetY: this.imgHeight + 20
            }));
        }

        //描述
        if(!Str.isBlank(this.sharedDesc)){
            units.push(new ImageMergerUnit({
                type: 'text',
                width: this.posterWidth - 20,
                height: 40,
                source: this.sharedDesc,
                fontColor: '#999',
                fontStyle: 'plain 14px',
                offsetX: 10,
                offsetY: this.imgHeight + 55
            }));
        }

        //分割线
        units.push(new ImageMergerUnit({
            type: 'line',
            width: this.posterWidth - 20,
            height: 2,
            source: '',
            fontColor: '#999',
            offsetX: 10,
            offsetY: this.imgHeight + 80
        }));

        //头像
        if(Str.isBlank(this.spreaderHeader)){
            this.spreaderHeader='/img/user_header.png';
        }
        units.push(new ImageMergerUnit({
            type: 'image',
            width: 90,
            height: 90,
            source: this.spreaderHeader,
            offsetX: Math.round(this.posterWidth/2 - 45) - 50,
            offsetY: this.imgHeight + 90
        }));

        //二维码
        let qrcoder=QRCoder.create(null, this.sharedLink, 90, 90);
        Logger.log('qrcoder content -> '+this.sharedLink);
        Logger.log('qrcoder image id -> '+qrcoder.getImage().id);
        Logger.log('qrcoder image src -> '+qrcoder.getCanvas().toDataURL('image/png'));
        units.push(new ImageMergerUnit({
            type: 'image',
            width: 90,
            height: 90,
            source: qrcoder.getCanvas().toDataURL('image/png'),
            offsetX: Math.round(this.posterWidth/2 - 45) + 50,
            offsetY: this.imgHeight + 90
        }));
        qrcoder.clear();
        qrcoder=null;
        delete qrcoder;

        //昵称
        if(!Str.isBlank(this.spreaderNickname)){
            units.push(new ImageMergerUnit({
                type: 'text',
                width: this.posterWidth - 80,
                height: 20,
                source: this.spreaderNickname,
                offsetX: 0,
                offsetY: this.imgHeight + 205,
                align: 'center'
            }));
        }

        //分享提示
        units.push(new ImageMergerUnit({
            type: 'text',
            width: this.posterWidth - 20,
            height: 30,
            source: Lang.convert('I{share,我向您推荐}'),
            offsetX: 0,
            offsetY: this.imgHeight + 235,
            align: 'center'
        }));

        let layer=Layers.open(window, 'I{share,分享}',null,'<div></div>');
        layer.setContent('<img width="100%" id="poster_'+layer.uuid+'"/>');

        let merger=new ImageMerger({
            width: this.posterWidth,
            height: this.posterHeight,
            units: units,
            target: top._$('poster_'+layer.uuid),
            callback: Share.showPoster,
            callbackTarget: top
        });
        merger.merge();
    },

    /**
     * 显示合成的图片
     * @param merger
     */
    showPoster(merger){
        merger.toImage();
        merger=null;
        delete merger;
    }
}
window.Share=Share;