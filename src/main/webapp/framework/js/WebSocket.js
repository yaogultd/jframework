//WebSocket
let websockets=[];//全部websocket
let JWebSocketSetting={
    textNotSupported:'',
    textConnEstablished:'',
    textConnClosed:'',
    textCommError:'',

    onI18NChanged:function() {
        this.textNotSupported=Lang.convert('I{websocket,当前环境不支持该通信方式}');
        this.textConnEstablished=Lang.convert('I{websocket,连接成功}');
        this.textConnClosed=Lang.convert('I{websocket,连接关闭}');
        this.textCommError=Lang.convert('I{websocket,通信错误}');
    }
}
window.JWebSocketSetting=JWebSocketSetting;

function JWebSocket(id,server,onopen,onmessage,onclose,onerror){
    if(!server.startsWith('/')) server='/'+server;

    this.id=id;
    this.server=server;
    this.onopen=onopen;
    this.onerror=onerror;
    this.onmessage=onmessage;
    this.onclose=onclose;
    this.websocket=null;

    this.connect();
    websockets[id]=this;
}
JWebSocket.prototype.connect=function(){
    if('WebSocket' in window){//判断当前浏览器是否支持WebSocket
        this.websocket = new WebSocket((UserAgent.httpScheme=='https'?'wss':'ws')+'://'+UserAgent.currentDomain+this.server);

        if(this.onopen){
            this.websocket.onopen=this.onopen;
        }else{
            this.websocket.onopen=function(){
                Logger.log(JWebSocketSetting.textConnEstablished);
            }
        }

        if(this.onerror){
            this.websocket.onerror=this.onerror;
        }else{
            this.websocket.onerror=function(){
                Logger.log(JWebSocketSetting.textCommError);
            }
        }

        if(this.onmessage){
            this.websocket.onmessage=this.onmessage;
        }else{
            this.websocket.onmessage=function(event){
                let data=JSONUtil.parse(event.data);
                Logger.log(JSONUtil.de(data.data));
            }
        }

        if(this.onclose){
            this.websocket.onclose=this.onclose;
        }else{
            this.websocket.onclose=function(){
                Logger.log(JWebSocketSetting.textConnClosed);
            }
        }
    }else {
        Logger.log(JWebSocketSetting.textNotSupported+' WebSocket');
    }
}
JWebSocket.prototype.reconnect=function(){
    console.log('websocket try reconnect......');
    try{
        this.destroy();
    }catch (e){}
    this.connect();
}
JWebSocket.prototype.destroy=function(){
    if(this.websocket){
        this.websocket.close();
        this.websocket=null;
    }
}
JWebSocket.prototype.send=function(data) {
    //data必须是包含于{}或[]中的符合json格式字符串
    let s = [];
    s.push('{"businessField":"' + Fields.currentField + '"');
    s.push(',"businessRole":"' + Fields.currentRole + '"');
    s.push(',"data":');
    s.push(data);
    s.push('}');
    this.websocket.send(s.join(''));
    s = null;
    delete s;
}
//WebSocket end