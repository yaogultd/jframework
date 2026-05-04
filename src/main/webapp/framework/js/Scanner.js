let Scanner={
    contentMapping:[],
    currentContentText:null,
    currentContentJson:null,
    callback:null,
    callbackTarget:null,
    devices:[],
    deviceId:null,
    deviceName:null,
    reader:null,
    source:null,
    initialized:false,

    //初始化
    init:function(){
        if(UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI){//使用微信小程序插件
            this.initialized=true;
            this.doScan();
        }else if(UserAgent.getUserAgentType()==UserAgent.UA_ALIPAY_MINI){//使用支付宝小程序插件
            this.initialized=true;
            this.doScan();
        }else {//浏览器内使用zxing实现扫码
            loadJS({src:'/framework/js/zxing/index.min.js', callback:Scanner.doInit});//加载js
        }
    },
    doInit:function(){
        Scanner.initialized=true;
        Scanner.reader = new ZXing.BrowserMultiFormatReader();
        Scanner.reader.listVideoInputDevices().then((videoInputDevices) => {
            videoInputDevices.forEach((element) => {
                Scanner.devices.push(element);
                if(!Scanner.deviceName){
                    Scanner.deviceId=element.deviceId;
                    Scanner.deviceName=element.label;
                }
                console.log('camera device -> '+element.deviceId+','+element.label);
            });//[{element.deviceId, element.label}]

            Logger.log('ZXing code reader initialized, current device is '+Scanner.deviceId+','+Scanner.deviceName);
            Scanner.doScan();
        });
    },

    close:function (){
        try{
            if(Scanner.reader) Scanner.reader.reset();
        }catch (e){
            console.log(e);
        }
    },

    /**
     * 扫描
     * @param callback
     * @param callbackTarget
     * @param source 扫描来源，可以是img对象、图片url，不指定则使用摄像头
     */
    scan:function(callback, callbackTarget, source){
        Scanner.currentContentText=null;
        Scanner.currentContentJson=null;
        Scanner.callback=callback?callback:null;
        Scanner.callbackTarget=callbackTarget?callbackTarget:window;
        Scanner.source=source?source:null;

        if(!Scanner.initialized){
            Scanner.init();
            return;
        }

        Scanner.doScan();
    },

    doScan:function(){
        try{
            if(UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI){//微信小程序
                wx.scanQRCode({
                    needResult: 1, //默认为0，扫描结果由微信处理，1则直接返回扫描结果
                    scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
                    success: function (res) {
                        let wxScanResult = res.resultStr;//当needResult 为 1 时，扫码返回的结果
                        if(wxScanResult.startsWith('CODE_128,')
                            || wxScanResult.startsWith('UPC_E,')){
                            wxScanResult=wxScanResult.substring(wxScanResult.indexOf(',')+1);
                        }
                        Scanner.process(null, wxScanResult);//处理结果
                    }
                });
            }else if(UserAgent.getUserAgentType()==UserAgent.UA_ALIPAY_MINI){//支付宝小程序
                my.postMessage({
                    action: 'scan'
                });
            }else{
                try {
                    if(Scanner.source) {
                        if((typeof Scanner.source)=='string'){
                            Scanner.reader.decodeFromImageUrl(Scanner.source).then((result) => {
                                Scanner.process(null, result.text);
                            }).catch((err) => {
                                Logger.err(err);
                                Page.alert('I{scanner,未扫描到有效内容}', 'I{scanner,处理出错}',['<div class="btnH24Gray btnBgRed" onclick="top.Dialog.close();">I{确定}</div>'],Dialog.MSG_TYPE_ERR);
                            })
                        }else{
                            Scanner.reader.decodeFromImage(Scanner.source).then((result) => {
                                Scanner.process(null, result.text);
                            }).catch((err) => {
                                Logger.err(err);
                                Page.alert('I{scanner,未扫描到有效内容}', 'I{scanner,处理出错}',['<div class="btnH24Gray btnBgRed" onclick="top.Dialog.close();">I{确定}</div>'],Dialog.MSG_TYPE_ERR);
                            })
                        }
                    } else {
                        Scanner.initUI();
                        Scanner.reader.decodeFromVideoDevice(Scanner.deviceId, 'ScannerVideo', (result, err) => {
                            if(result) Scanner.process(null, result.text);
                            if(err && !(err instanceof ZXing.NotFoundException)) {
                                Logger.err(err);
                                Page.alert('I{scanner,未扫描到有效内容}', 'I{scanner,处理出错}',['<div class="btnH24Gray btnBgRed" onclick="top.Dialog.close();">I{确定}</div>'],Dialog.MSG_TYPE_ERR);
                            }
                        });
                    }
                }catch (e){
                    Logger.err(e);
                    Page.alert('I{scanner,未扫描到有效内容}', 'I{scanner,处理出错}',['<div class="btnH24Gray btnBgRed" onclick="top.Dialog.close();">I{确定}</div>'],Dialog.MSG_TYPE_ERR);
                }
            }
        }catch(e){
            Logger.err(e);
            Page.alert('I{scanner,未扫描到有效内容}', 'I{scanner,处理出错}',['<div class="btnH24Gray btnBgRed" onclick="top.Dialog.close();">I{确定}</div>'],Dialog.MSG_TYPE_ERR);
        }
    },

    initUI:function(){
        let s=[];
        s.push('<div style="min-height: 200px;"><video id="ScannerVideo" width="300"></video></div>');
        if(Scanner.devices.length>1){
            s.push('<div id="ScannerDevices">');
            for(let i=0; i<Scanner.devices.length && i<2; i++){
                s.push('<div class="'+(i==0?'fl':'fr')+' ScannerDevice'+(Str.equals(Scanner.devices[i].deviceId, Scanner.deviceId) ? 'Current' : '')+'" onclick="Scanner.changeDevice(\''+Scanner.devices[i].deviceId+'\', \''+Scanner.devices[i].label+'\');">'+Scanner.devices[i].label+'</div>');
            }
            s.push('</div>');
        }

        top.Dialog.close();
        top.Dialog.open(-1, -1, -1, -1, Scanner.reset, null, window, 'dialog', 'I{scanner,扫描}');
        top.Dialog.setContent(s.join(''));
        top.Dialog.setBtns('<div class="btnH40" onclick="top.Dialog.close();">I{取消}</div>');
        s=null;
        delete s;
    },

    changeDevice:function (id, name){
        if(Str.equals(id, 'undefined')) id=null;
        this.deviceId=id;
        this.deviceName=name;
        this.reset();
        this.doScan();
    },

    process:function(code, message){
        Scanner.close();
        if(Scanner.contentMapping[message]) message=Scanner.contentMapping[message];
        Scanner.currentContentText=message;
        if(message.indexOf('http')==0){
            if(message.indexOf('to_mini_program/')>-1) message=message.substring(message.indexOf('to_mini_program/')+16);
            else if(message.indexOf('/to_mini_program_')>-1) {
                message=message.substring(requestUrl.indexOf('/to_mini_program_')+17);
                message=message.substring(message.indexOf('/')+1);
            }
            if(Str.getHost(message).endsWith(UserAgent.mainDomain)){
                let uri=Str.getUri(message);
                if(UserAgent.isMobile()
                    && (uri=='/' || uri.endsWith('.htm')>-1)){
                    Layers.open(window,'',message);
                }else{
                    location.href=(message);
                }
            }else{
                Page.alert(message, 'I{scanner,扫描结果}',['<div class="btnH24Gray btnBgBlue" onclick="Copy.copy(decodeURIComponent(\''+message+'\'));">I{scanner,复制扫描结果}</div>'],Dialog.MSG_TYPE_INFO);
            }
        }else{
            let qrcode=null;
            try{
                qrcode=JSON.parse(message);
                Scanner.currentContentJson=qrcode;
            }catch(e){}

            try{
                if(Scanner.callback) Scanner.callback.call(Scanner.callbackTarget, message);
            }catch(e1){
                Page.alert(message, 'I{scanner,扫描结果}',['<div class="btnH24Gray btnBgBlue" onclick="Copy.copy(decodeURIComponent(\''+message+'\'));">I{scanner,复制扫描结果}</div>'],Dialog.MSG_TYPE_INFO);
            }
        }
    },

    onProcessed:function(ajax){
        if(ajax.getReadyState()==4&&ajax.getStatus()==200){
            try{
                top.Dialog.close();
                let resp=ajax.getResponseJson();
                if(resp && resp.success){
                    Page.alert(resp.message,null,null, Dialog.MSG_TYPE_OK);
                }else{
                    Page.alert(resp.message,null,null, Dialog.MSG_TYPE_ERR);
                }
            }catch(e){
                Page.alert(message, 'I{scanner,处理出错}',['<div class="btnH24Gray btnBgRed" onclick="top.Dialog.close();">I{确定}</div>'],Dialog.MSG_TYPE_ERR);
            }
        }
    },

    reset:function(){
        if(Scanner.reader) Scanner.reader.reset();
    }
}
window.Scanner=Scanner;