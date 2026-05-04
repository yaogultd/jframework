//通用功能-触摸事件处理////
//Updated 2022-09-07
//touchstart：触摸开始的时候触发
//touchmove：手指在屏幕上滑动的时候触发
//touchend：触摸结束的时候触发

//而每个触摸事件都包括了三个触摸列表，每个列表里包含了对应的一系列触摸点（用来实现多点触控）：
//touches：当前位于屏幕上的所有手指的列表。
//targetTouches：位于当前DOM元素上手指的列表。
//changedTouches：涉及当前事件手指的列表。

//每个触摸点由包含了如下触摸信息（常用）：
//identifier：一个数值，唯一标识触摸会话（touch session）中的当前手指。一般为从0开始的流水号（android4.1，uc）
//target：DOM元素，是动作所针对的目标。
//pageX/pageY/clientX/clientY/screenX/screenY：一个数值，动作在屏幕上发生的位置（page包含滚动距离,client不包含滚动距离，screen则以屏幕为基准）。　
//radiusX/radiusY/rotationAngle：画出大约相当于手指形状的椭圆形，分别为椭圆形的两个半径和旋转角度。

//触摸目标对象：key,对象ID  value，Touch对象
var touchTargets=[];

/**
 *
 * @param obj
 * @param minMovement 触发动作的最小移动量
 * @param callbackStart 开始时回调
 * @param callbackMoving 滑动时回调
 * @param callbackUp 向上滑动时回调
 * @param callbackDown 向下滑动时回调
 * @param callbackLeft 向左滑动时回调
 * @param callbackRight 向右滑动时回调
 * @param callbackCancel 滑动取消时回调
 * @param callbackOnclick 单击时回调
 * @param callbackZoomIn 放大时回调
 * @param callbackZoomOut 缩小是回调
 * @param callbackLongPress 长按时回调
 * @constructor
 */
function Touch(obj,minMovement,callbackStart,callbackMoving,callbackUp,callbackDown,callbackLeft,callbackRight,callbackCancel,callbackOnclick,callbackZoomIn,callbackZoomOut,callbackLongPress){
    if(!obj||!obj.id) return;

    this.obj=obj;

    this.minMovement=10;
    if(minMovement) this.minMovement=minMovement;

    this.longTimePress=1000;

    this.ended=true;
    this.startTime=0;
    this.endTime=0;
    this.initPageX=0;
    this.initPageY=0;
    this.initClientX=0;
    this.initClientY=0;
    this.initScreenX=0;
    this.initScreenY=0;
    this.initDistanceOfTwoPoint=0;
    this.initDistanceOfTwoPointX=0;
    this.initDistanceOfTwoPointY=0;

    this.pageX=0;
    this.pageY=0;
    this.clientX=0;
    this.clientY=0;
    this.screenX=0;
    this.screenY=0;
    this.distanceOfTwoPoint=0;
    this.distanceOfTwoPointX=0;
    this.distanceOfTwoPointY=0;

    this.callbackStart=callbackStart;
    this.callbackMoving=callbackMoving;
    this.callbackUp=callbackUp;
    this.callbackDown=callbackDown;
    this.callbackLeft=callbackLeft;
    this.callbackRight=callbackRight;
    this.callbackCancel=callbackCancel;
    this.callbackOnclick=callbackOnclick;
    this.callbackZoomIn=callbackZoomIn?callbackZoomIn:null;
    this.callbackZoomOut=callbackZoomOut?callbackZoomOut:null;
    this.callbackLongPress=callbackLongPress?callbackLongPress:null;

    //是否阻止浏览器默认时间处理
    this.preventDefaultOnClick=true;

    if(UserAgent.isPC()){
        this.obj.addEventListener("mousedown", this.touchstart, true);
        this.obj.addEventListener("mousemove", this.touchmove, true);
        this.obj.addEventListener("mouseup", this.touchend, true);
    }else{
        this.obj.addEventListener("touchstart", this.touchstart, true);
        this.obj.addEventListener("touchmove", this.touchmove, true);
        this.obj.addEventListener("touchend", this.touchend, true);
    }

    //由哪个对象调用回调函数（默认为window)
    this.callbackCaller=null;

    touchTargets[this.obj.id]=this;
}

Touch.prototype.touchstart=function(event){
    if(!event.targetTouches){//PC
        event.preventDefault();//阻止浏览器默认事件

        let target=Utils.getEventTarget(event);
        let _instance=touchTargets[target.id];

        if(!_instance) return;

        _instance.ended=false;
        _instance.distanceOfTwoPoint=0;
        _instance.distanceOfTwoPointX=0;
        _instance.distanceOfTwoPointY=0;

        _instance.startTime=(new Date()).getTime();
        _instance.initPageX=event.pageX;
        _instance.initPageY=event.pageY;
        _instance.initClientX=event.clientX;
        _instance.initClientY=event.clientY;
        _instance.initScreenX=event.screenX;
        _instance.initScreenY=event.screenY;

        _instance.pageX=event.pageX;
        _instance.pageY=event.pageY;
        _instance.clientX=event.clientX;
        _instance.clientY=event.clientY;
        _instance.screenX=event.screenX;
        _instance.screenY=event.screenY;

        if(_instance.callbackStart){
            _instance.callbackStart.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }else if(event.targetTouches.length==1){//如果这个元素的位置内有1个手指的话
        let target=Utils.getEventTarget(event);
        let _instance=touchTargets[target.id];

        if(!_instance) return;

        let touch = event.targetTouches[0];

        _instance.distanceOfTwoPoint=0;
        _instance.distanceOfTwoPointX=0;
        _instance.distanceOfTwoPointY=0;

        _instance.startTime=(new Date()).getTime();
        _instance.initPageX=touch.pageX;
        _instance.initPageY=touch.pageY;
        _instance.initClientX=touch.clientX;
        _instance.initClientY=touch.clientY;
        _instance.initScreenX=touch.screenX;
        _instance.initScreenY=touch.screenY;

        _instance.pageX=touch.pageX;
        _instance.pageY=touch.pageY;
        _instance.clientX=touch.clientX;
        _instance.clientY=touch.clientY;
        _instance.screenX=touch.screenX;
        _instance.screenY=touch.screenY;

        if(_instance.callbackStart){
            _instance.callbackStart.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }else if(event.targetTouches.length==2){//如果这个元素的位置内有2个手指的话
        event.preventDefault();//阻止浏览器默认事件

        let target=Utils.getEventTarget(event);
        let _instance=touchTargets[target.id];

        if(!_instance) return;

        let touch1 = event.targetTouches[0];
        let touch2 = event.targetTouches[1];

        _instance.initDistanceOfTwoPoint=MathUtil.distance(touch1.screenX,touch1.screenY,touch2.screenX,touch2.screenY);
        _instance.initDistanceOfTwoPointX=MathUtil.distance(touch1.screenX,0,touch2.screenX,0);
        _instance.initDistanceOfTwoPointY=MathUtil.distance(0,touch1.screenY,0,touch2.screenY);

        if(_instance.callbackStart){
            _instance.callbackStart.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }
}

Touch.prototype.touchmove=function(event){
    if(!event.targetTouches) {//PC
        event.preventDefault();//阻止浏览器默认事件

        let target=Utils.getEventTarget(event);
        let _instance=touchTargets[target.id];
        if(!_instance) return;

        if(_instance.ended) return;

        _instance.distanceOfTwoPoint=0;
        _instance.distanceOfTwoPointX=0;
        _instance.distanceOfTwoPointY=0;

        _instance.pageX=event.pageX;
        _instance.pageY=event.pageY;
        _instance.clientX=event.clientX;
        _instance.clientY=event.clientY;
        _instance.screenX=event.screenX;
        _instance.screenY=event.screenY;

        if(_instance.callbackMoving){
            _instance.callbackMoving.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }else if(event.targetTouches.length==1){//如果这个元素的位置内有1个手指的话
        event.preventDefault();//阻止浏览器默认事件

        let target=Utils.getEventTarget(event);
        let _instance=touchTargets[target.id];

        if(!_instance) return;

        let touch = event.targetTouches[0];

        _instance.distanceOfTwoPoint=0;
        _instance.distanceOfTwoPointX=0;
        _instance.distanceOfTwoPointY=0;

        _instance.pageX=touch.pageX;
        _instance.pageY=touch.pageY;
        _instance.clientX=touch.clientX;
        _instance.clientY=touch.clientY;
        _instance.screenX=touch.screenX;
        _instance.screenY=touch.screenY;

        if(_instance.callbackMoving){
            _instance.callbackMoving.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }else if(event.targetTouches.length==2){//如果这个元素的位置内有2个手指的话
        event.preventDefault();//阻止浏览器默认事件

        let target=Utils.getEventTarget(event);
        let _instance=touchTargets[target.id];

        if(!_instance) return;

        _instance.initScreenX=_instance.screenX;
        _instance.initScreenY=_instance.screenY;

        let touch1 = event.targetTouches[0];
        let touch2 = event.targetTouches[1];

        _instance.distanceOfTwoPoint=MathUtil.distance(touch1.screenX,touch1.screenY,touch2.screenX,touch2.screenY);
        _instance.distanceOfTwoPointX=MathUtil.distance(touch1.screenX,0,touch2.screenX,0);
        _instance.distanceOfTwoPointY=MathUtil.distance(0,touch1.screenY,0,touch2.screenY);

        if(_instance.callbackMoving){
            _instance.callbackMoving.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }
}

Touch.prototype.stop=function(event) {
    this.ended = true;
}

Touch.prototype.touchend=function(event){
    let target=Utils.getEventTarget(event);
    let _instance=touchTargets[target.id];

    if(!_instance) return;
    _instance.ended=true;

    if(_instance.distanceOfTwoPoint==0){////如果这个元素的位置内有1个手指的话
        _instance.endTime=(new Date()).getTime();

        let isValidMove=false;
        if(_instance.screenX-_instance.initScreenX>_instance.minMovement){//右
            isValidMove=true;
            if(_instance.callbackRight){
                _instance.callbackRight.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
            }
        }

        if(_instance.initScreenX-_instance.screenX>_instance.minMovement){//左
            isValidMove=true;
            if(_instance.callbackLeft){
                _instance.callbackLeft.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
            }
        }

        if(_instance.screenY-_instance.initScreenY>_instance.minMovement){//下
            isValidMove=true;
            if(_instance.callbackDown){
                _instance.callbackDown.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
            }
        }

        if(_instance.initScreenY-_instance.screenY>_instance.minMovement){//上
            isValidMove=true;
            if(_instance.callbackUp){
                _instance.callbackUp.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
            }
        }

        if(!isValidMove){
            let pressTime=_instance.endTime-_instance.startTime;
            if(pressTime>=_instance.longTimePress){
                if(_instance.callbackLongPress){
                    event.preventDefault();//阻止浏览器默认事件，重要
                    _instance.callbackLongPress.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
                }
            }else{
                if(_instance.preventDefaultOnClick) event.preventDefault();//阻止浏览器默认事件，重要

                if(_instance.callbackCancel){
                    _instance.callbackCancel.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
                }
                if(_instance.callbackOnclick){
                    _instance.callbackOnclick.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
                }
            }
        }
    }else{//如果这个元素的位置内有2个手指的话
        event.preventDefault();//阻止浏览器默认事件，重要

        let movement=_instance.distanceOfTwoPoint-_instance.initDistanceOfTwoPoint;
        movement=Math.floor(movement);
        if(movement<=(0-_instance.minMovement)){//缩小
            if(_instance.callbackZoomOut){
                _instance.callbackZoomOut.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
            }
        }else if(movement>=_instance.minMovement){//放大
            if(_instance.callbackZoomIn){
                _instance.callbackZoomIn.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
            }
        }else if(_instance.callbackCancel){
            _instance.callbackCancel.call(_instance.callbackCaller?_instance.callbackCaller:window, event, _instance);
        }
    }
}
//通用功能-触摸事件处理 end////