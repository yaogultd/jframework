function initOpenWeixinMiniProgram(){
    let uaType = UserAgent.getUserAgentType();
    if(uaType==UserAgent.UA_WECHAT || uaType==UserAgent.UA_WECHAT_MINI) {
        try {
            _$('wechat-web-container').style.display='';
            
            let launchBtn = document.getElementById('launch-btn')
            launchBtn.addEventListener('ready', function (e) {
                console.log('开放标签 ready');
            });

            launchBtn.addEventListener('launch', function (e) {
                console.log('开放标签 success');
            });

            launchBtn.addEventListener('error', function (e) {
                console.log('开放标签 fail', e.detail);
            });
        }catch (e){
            console.error(e);
        }
    }
}