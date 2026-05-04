//播放器
//媒体播放器
function Player(containerId,src,isLive,width,height,mute,auto,controls,poster,repeat,adjustSide,onerror){
    this.containerId=containerId;
    this.src=src;
    this.isLive=isLive;
    this.width=width;
    this.height=height;
    this.maxWidth=width;
    this.maxHeight=height;
    this.minWidth=0;//width;
    this.minHeight=0;//height;
    this.adjustSide=adjustSide?adjustSide:'N';//哪一边长度保持不变 W：调整宽度，H：调整高度，其它值：不调整
    this.mute=mute;//是否静音
    this.auto=auto;//是否自动播放
    this.controls=controls;//是否显示控制按钮
    this.poster=poster;//封面
    this.repeat=repeat;//是否重复播放
    this.player=null;
    this.video=null;
    this.videoWidth=width;
    this.videoHeight=height;
    this.onerror=onerror;
}

//管理所有播放器实例
let Players={
    players:[],
    playersLoaded:[],
    scriptsLoaded:false,
    attachedVideos:[],
    attachTimer:null,

    addPlayer:function(containerId,src,isLive,width,height,mute,auto,controls,poster,repeat,adjustSide,onerror){
        this.reset(containerId);
        src=decodeURIComponent(src);
        if((typeof isLive)!='boolean') isLive=false;

        if((typeof width)!='number' || width<=0) width=360;
        if((typeof height)!='number' || height<=0) height=Math.ceil(width*(9/16));

        if((typeof mute)!='boolean') mute=false;
        if((typeof auto)!='boolean') auto=false;
        if((typeof controls)!='boolean') controls=true;

        if(Str.isBlank(poster)) poster='/framework/img/blank.png';
        else poster=decodeURIComponent(poster);

        if((typeof repeat)!='boolean') repeat=false;

        let player=new Player(containerId,src,isLive,width,height,mute,auto,controls,poster,repeat,adjustSide,onerror);
        this.players[containerId]=player;
        this.attachedVideos[containerId]=false;

        console.log('add video player:');
        console.log('containerId -> '+containerId+' -> '+_$(this.players[containerId].containerId));
        console.log('src -> '+this.players[containerId].src);
        console.log('isLive -> '+this.players[containerId].isLive);
        console.log('width -> '+this.players[containerId].width);
        console.log('height -> '+this.players[containerId].height);
        console.log('mute -> '+this.players[containerId].mute);
        console.log('auto -> '+this.players[containerId].auto);
        console.log('controls -> '+this.players[containerId].controls);
        console.log('poster -> '+this.players[containerId].poster);
        console.log('repeat -> '+this.players[containerId].repeat);
        console.log('adjustSide -> '+this.players[containerId].adjustSide);

        return player;
    },

    setMaxWidth:function(containerId, maxWidth){
        let p=this.getPlayer(containerId);
        if(p) p.maxWidth=maxWidth;
    },

    setMaxHeight:function(containerId, maxHeight){
        let p=this.getPlayer(containerId);
        if(p) p.maxHeight=maxHeight;
    },

    setMinWidth:function(containerId, minWidth){
        let p=this.getPlayer(containerId);
        if(p) p.minWidth=minWidth;
    },

    setMinHeight:function(containerId, minHeight){
        let p=this.getPlayer(containerId);
        if(p) p.minHeight=minHeight;
    },


    setIsLive:function(containerId, isLive){
        let p=this.getPlayer(containerId);
        if(p) p.isLive=isLive;
    },

    attach:function(video){
        let p=video.parentNode;
        while(p){//从video标签向上寻找初始设置的父容器节点
            if(p.id && this.players[p.id]){//如果找到
                let instance=this.players[p.id];
                if(!instance.video) instance.video=video;

                //已经加载
                if(Players.attachedVideos[p.id]) return true;

                let newWidth=instance.width;
                let newHeight=instance.height;
                if(video.videoWidth>0 && video.videoHeight>0){
                    Players.attachedVideos[p.id]=true;
                    console.log('video info loaded, width -> '+video.videoWidth+", height -> "+video.videoHeight+', adjustSide -> '+instance.adjustSide);

                    if(instance.adjustSide=='H'){//宽度不变，调整高度
                        newWidth=instance.width;
                        newHeight=Math.floor(instance.width*(video.videoHeight/video.videoWidth));
                    }else if(instance.adjustSide=='W'){//高度不变，调整宽度
                        newHeight=instance.height;
                        newWidth=Math.floor(instance.height*(video.videoWidth/video.videoHeight));
                    }

                    //限定高度不超出边界值
                    if(instance.maxHeight>0 && newHeight>instance.maxHeight){//超出最大允许高度
                        newWidth=Math.floor(newWidth*(instance.maxHeight/newHeight));//宽度等比缩小
                        newHeight=instance.maxHeight;//高度设为最大允许高度
                    }
                    if(instance.minHeight>0 && newHeight<instance.minHeight){//小于最小允许高度
                        newWidth=Math.floor(newWidth*(instance.maxHeight/newHeight));//宽度等比放大
                        newHeight=instance.minHeight;//高度设为最小允许高度
                    }
                    //限定高度不超出边界值 end

                    //限定宽度不超出边界值
                    if(instance.maxWidth>0 && newWidth>instance.maxWidth){//超出最大允许宽度
                        newHeight=Math.floor(newHeight*(instance.maxWidth/newWidth));//高度等比缩小
                        newWidth=instance.maxWidth;//宽度设为最大允许宽度
                    }

                    if(newWidth<instance.minWidth>0 && newWidth<instance.minWidth){//小于最小允许宽度
                        newHeight=Math.floor(newHeight*(instance.maxWidth/newWidth));//高度等比放大
                        newWidth=instance.minWidth;//宽度设为最小允许宽度
                    }
                    //限定宽度不超出边界值 end

                    console.log('video newWidth -> '+newWidth+", newHeight -> "+newHeight);
                    instance.player.width(newWidth);
                    instance.player.height(newHeight);

                    if(instance.auto) Players.play(instance);
                    else Players.stop(instance);

                    return true;
                }

                break;
            }

            p=p.parentNode;
        }

        return false;
    },

    attachAll:function(){
        if(Players.attachTimer){
            clearTimeout(Players.attachTimer);
            Players.attachTimer=null;
        }
        let videos=document.getElementsByTagName('video');

        let notAttached=0;
        for(let i=0; i<videos.length; i++){
            if(!Players.attach(videos[i])) notAttached++;
        }

        if(notAttached>0){
            Players.attachTimer=setTimeout(Players.attachAll,1000);
        }
    },

    initPlayers:function(){
        if(this.scriptsLoaded){
            this.startPlayers();
            return;
        }

        new QueuedJS(['/framework/js/TcPlayer/libs/TXLivePlayer-1.3.5.min.js',
                '/framework/js/TcPlayer/libs/hls.min.1.1.7.js',
                '/framework/js/TcPlayer/tcplayer.v5.1.0.min.js'],
            ['UTF-8', 'UTF-8', 'UTF-8'],
            Players.onScriptLoad);
    },

    onScriptLoad:function(){
        loadCSSFromUrl({src: '/framework/css/tcplayer/tcplayer.min.css', callback: Players.onCssLoad});
    },

    onCssLoad:function (){
        Players.scriptsLoaded=true;
        Players.startPlayers();
    },

    startPlayers:function(){
        if(!Players.scriptsLoaded){
            setTimeout(Players.startPlayers, 100);
            return;
        }

        for(let i in Players.players){
            let p=Players.players[i];
            if(!p) continue;
            if(Players.playersLoaded[p.containerId] || p.player) continue;

            Players.playersLoaded[p.containerId]=true;

            let player =  new TCPlayer(p.containerId, {"autoplay": p.auto,
                "poster": p.poster,
                "width":  p.width,
                "height": p.height,
                "licenseUrl": 'https://license.vod2.myqcloud.com/license/v2/1251887951_1/v_cube.license'
            });
            if(p.onerror) player.on('error', p.onerror);

            player.src(p.src);

            p.player=player;
        }

        Players.attachTimer=setTimeout(Players.attachAll,100);
    },

    getPlayer:function(containerId){
        return this.players[containerId];
    },

    play:function(instance){
        if((typeof instance)=='string'){
            instance=this.getPlayer(instance);
        }

        try{
            if(instance && instance.player) instance.player.play(true);
        }catch(e){}
    },

    stop:function(instance){
        if((typeof instance)=='string'){
            instance=this.getPlayer(instance);
        }

        try{
            if(instance && instance.player) instance.player.pause();
        }catch(e){}
    },

    reset:function(containerId){
        let instance=this.getPlayer(containerId);
        if(instance && instance.player){
            try{
                instance.player.dispose();
            }catch(e){}
        }

        this.players[containerId]=null;
        this.playersLoaded[containerId]=null;
    }
}
window.Players=Players;