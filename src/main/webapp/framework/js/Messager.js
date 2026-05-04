//打开客服窗口
let Messager={
    allowedFileTypes:['txt','jpg','jpeg','png','gif','zip','rar','doc','docx','pdf','ppt','pptx','xls','xlsx','amr','3gp','mp4','mp3','mov'],
    sellerId:null,//聊天对象（为空表示与平台客服）
    goodsId:null,//当前咨询的商品ID
    orderId:null,//当前咨询的订单ID
    depositId:null,//当前咨询的充值记录ID
    drawId:null,//当前咨询的提款记录ID
    sent:0,
    received:[],
    notifySn:1,
    websocket:null,
    heartbeatInterval:null,
    onGetMessages:null,
    isStaff:false,
    park:null,
    talkToSelector:null,
    shown:false,

    //选中聊天窗口所在的父窗口（优先选择顶部窗口）
    getPark: function (){
        try{
            if(top && (typeof top.Messager)!='undefined') this.park=top;
        }catch (e){}
        if(!this.park) this.park=window;
        return this.park;
    },

    //创建聊天窗口，但不显示
    create: function(silent,url,title,sellerId,goodsId,orderId,depositId,drawId){
        if(!url) url='/im/index.htm';
        if(!title) title='I{im,客服中心}';

        let park= this.getPark();
        if(depositId || drawId) sellerId=null;//充值、提款只能咨询平台
        park.Messager.init(silent,url,title,sellerId,goodsId,orderId,depositId,drawId);
    },

    //初始化聊天窗口
    init:function(silent,url,title,sellerId,goodsId,orderId,depositId,drawId){
        this.sent=0;
        this.sellerId=null;//sellerId;//暂时设为全部跟平台交谈
        this.goodsId=goodsId;
        this.orderId=orderId;
        this.depositId=depositId;
        this.drawId=drawId;

        if(!_$('chatting')){
            let str='<div class="layer" id="chatting" style="visibility: hidden;">';
            str+='	<div class="layerHeader" id="chattingTitle">';
            str+='		<div class="layerBack iconfont icon-back_light" onclick="Messager.hide();"></div>';
            str+='		<div class="layerTitle" id="chattingTitleText">'+title+'</div>';
            str+='		<div class="layerClose iconfont icon-close" onclick="Messager.hide();"></div>';
            str+='	</div>';
            str+='	<div class="layerContent" id="chattingContent">';
            str+='		<iframe id="chattingFrame" name="chattingFrame" src="'+url+'" width="100%" height="100%" frameborder="0" scrolling="no"></iframe>';
            str+='	</div>';
            str+='	<div class="layerFooter" id="chattingBtns"></div>';
            str+='</div>';
            if(_$('JcontentBottom')) _$('JcontentBottom').insertAdjacentHTML('beforebegin', Lang.convert(str));
            else document.body.insertAdjacentHTML('beforeend', Lang.convert(str));

            new Movable('chattingTitle', 'chatting', null, null, null, null);
            //setInterval(Messager.getNotifications, 5000);
        }
        try{
            chattingFrame.talkTo(this.sellerId);
        }catch (e){}
        if(!silent) this.show();
    },

    //显示聊天窗口
    show:function(){
        this.shown=true;
        try{
            chattingFrame.setUI(true);
        }catch (e){}
        let H = W.vh() - 41 - 51;
        IFrame.adjustSize('chattingFrame', 0, H);
        _$('chattingContent').style.height=H+'px';
        _$('chatting').style.zIndex=W.getMaxZIndex();
        _$('chatting').style.display='';
        _$('chatting').style.visibility='visible';
        try{
            chattingFrame._scroll();
        }catch(e){}
    },

    //隐藏聊天窗口
    hide:function(){
        this.shown=false;
        if(_$('chatting')){
            _$('chatting').style.display='none';
            _$('chatting').style.visibility='hidden';
        }
    },

    //开启会话（websocket通信）
    open:function(onGetMessages, isStaff){
        this.getPark();
        if(onGetMessages) this.onGetMessages=onGetMessages;
        this.isStaff=(typeof isStaff)=='boolean' ? isStaff : false;
        this.websocket = new JWebSocket('Message','/websocket/chatting',Messager.onopen,Messager.onmessage,Messager.onclose,Messager.onerror);
    },

    //websocket连接建立时
    onopen:function(){
        console.log('connected to /websocket/chatting');

        //获得信息列表
        Messager.hello();

        //每15秒发一次心跳，以免断开
        if(Messager.heartbeatInterval) clearInterval(Messager.heartbeatInterval);
        Messager.heartbeatInterval=setInterval(Messager.heartbeat, 15000);
    },

    //websocket通信发生错误时
    onerror:function() {
        console.log('error while connect to /websocket/chatting');
    },

    //websocket连接关闭时
    onclose:function(a,b){
        console.log('disconnect from /websocket/chatting, reconnect after 3 seconds.');
        setTimeout(Messager.reconnect, 3000);
        //Toast.show(null,'<a class="awhite" href="javascript:_void();" onclick="location.reload();">I{shopping,通讯中断，请刷[点此]重新连接}</a>',3600000);
    },

    //websocket重连
    reconnect:function(){
        Messager.websocket.reconnect();
        //Toast.show(null,'<a class="awhite" href="javascript:_void();" onclick="location.reload();">I{shopping,通讯中断，请刷[点此]重新连接}</a>',3600000);
    },

    //websocket收到消息时
    onmessage:function(event) {
        //console.log('get message from /websocket/chatting -> '+event.data);
        //心跳回复，无需处理
        if(event.data=='OK') return;
        if(Messager.onGetMessages) Messager.onGetMessages(event.data);
    },

    //任意发送一条信息给websocket的服务端以获得信息列表
    hello:function(rpp, pn){
        if(!rpp) rpp=100;
        if(!pn) pn='';
        let s='{"lang": "'+Lang.getCurrentLang().id+'","ua_id": "'+Cookie.get('UA_ID')+'","talk_with": "'+(this.park.Messager.sellerId?this.park.Messager.sellerId:'')+'","rpp": "'+rpp+'","pn": "'+pn+'","seller": "'+(Messager.isStaff?'true':'')+'"}';
        console.log('send to /websocket/chatting/ -> '+s);
        this.websocket.send(s);
    },

    //websocket心跳
    heartbeat:function(){
        //console.log('messager heartbeat to /websocket/chatting');
        Messager.websocket.send('{"heartbeat":"true","lang": "' + Lang.getCurrentLang().id + '","ua_id": "' + Cookie.get('UA_ID') + '","seller": "' + (Messager.isStaff ? 'true' : '') + '"}');
    },

    //发送文件时，判断文件类型是否合法
    isFileValid:function(fileName){
        return Str.endsWithOneOf(fileName,this.allowedFileTypes,true);
    },

    //构造准备发送的消息数据
    assemble:function(data){
        if(this.getPark() != window){
            return this.getPark().Messager.assemble(data);
        }
        if(this.depositId) data+='&deposit_id='+this.depositId;
        if(this.drawId) data+='&draw_id='+this.drawId;
        if(this.orderId) data+='&order_id='+this.orderId;
        if(this.goodsId) data+='&goods_id='+this.goodsId;
        if(this.sellerId) data+='&talk_with='+this.sellerId;

        this.depositId=null;
        this.drawId=null;
        this.orderId=null;
        this.goodsId=null;
        this.sent++;

        if(data.startsWith('&')) data=data.substring(1);
        return data;
    },

    //构造准备发送的消息数据（忽略业务数据）
    assembleIgnoreBiz:function(data){
        if(this.getPark() != window){
            return this.getPark().Messager.assembleIgnoreBiz(data);
        }
        if(this.sellerId) data+='&talk_with='+this.sellerId;
        if(data.startsWith('&')) data=data.substring(1);
        return data;
    },

    //查询未读消息
    getNotifications:function(){
        if(Messager.shown) return;
        (new Ajax()).send('GET',Messager.doGetNotifications,'/api/platform/im/message/notifications');
    },
    doGetNotifications:function(ajax){
        if(ajax.getReadyState()==4&&ajax.getStatus()==200){
            let txt=ajax.getResponseText();

            let doc=XML.parse(txt);
            let root=XML.getRoot(doc);
            let ms=XML.getChildNodes(root,'m');

            let hasNew=false;
            for(let i=0;i<ms.length;i++){
                let msgId=XML.getAttr(ms[i],'msgId');
                let msgFrom=XML.getAttr(ms[i],'msgFrom');
                let msgFromType=XML.getAttr(ms[i],'msgFromType');
                let msgFromName=XML.getAttr(ms[i],'msgFromName');
                let msgTo=XML.getAttr(ms[i],'msgTo');
                let msgToName=XML.getAttr(ms[i],'msgToName');
                let msgToType=XML.getAttr(ms[i],'msgToType');
                let businessCode=XML.getAttr(ms[i],'businessCode');
                let businessPk=XML.getAttr(ms[i],'businessPk');

                if(Messager.received[msgId]) continue;
                Messager.received[msgId]='1';
                hasNew=true;
            }

            if(hasNew){
                Page.alert('I{im,您收到新的客服消息}', null, null, Dialog.MSG_TYPE_INFO, Messager.onNewMessage);
            }
        }
    },
    onNewMessage:function (){
        Messager.getPark().Messager.show();
    }
}
window.Messager=Messager;

function _message(url,title,sellerId,goodsId,orderId,depositId,drawId){
    let park=Messager.getPark();
    park.Messager.init(false,url,title,sellerId,goodsId,orderId,depositId,drawId)
}