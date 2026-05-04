/通用组件-日历
//Updated 2022-09-07
let D={
    uuid:null,
    inputHoursDef:'00',
    inputMinutesDef:'00',
    inputSecendsDef:'00',
    days:['I{calendar,SUN}','I{calendar,MON}','I{calendar,TUE}','I{calendar,WED}','I{calendar,THU}','I{calendar,FRI}','I{calendar,SAT}'],
    daysFullName:['I{calendar,SUNDAY}','I{calendar,MONDAY}','I{calendar,TUESDAY}','I{calendar,WEDNESDAY}','I{calendar,THURSDAY}','I{calendar,FRIDAY}','I{calendar,SATURDAY}'],
    onCalendarCloseCallback:null,

    onI18NChanged:function(){
        this.days=['I{calendar,SUN}','I{calendar,MON}','I{calendar,TUE}','I{calendar,WED}','I{calendar,THU}','I{calendar,FRI}','I{calendar,SAT}'];
        this.daysFullName=['I{calendar,SUNDAY}','I{calendar,MONDAY}','I{calendar,TUESDAY}','I{calendar,WEDNESDAY}','I{calendar,THURSDAY}','I{calendar,FRIDAY}','I{calendar,SATURDAY}'];
        for(let i=0; i<this.days.length; i++){
            this.days[i]=Lang.convert(this.days[i]);
            this.daysFullName[i]=Lang.convert(this.daysFullName[i]);
        }
    },

    onCalendarClose:function(input){
        if(this.onCalendarCloseCallback) this.onCalendarCloseCallback(input);
    },

    formatTimeElapse:function(t){
        let show='';
        let t_day = Math.floor(t / Global.msOfDay);
        if(t_day>0) show += t_day + (Lang.getCurrentLang().id=='cn'?'':' ') + 'I{calendar,天} ';

        t = t % Global.msOfDay;
        let t_hour = Math.floor(t / Global.msOfHour);
        if (t_hour < 10) t_hour = '0' + t_hour;
        show += t_hour + ':';

        t = t % Global.msOfHour;
        let t_minute = Math.floor(t / Global.msOfMinute);
        if (t_minute < 10) t_minute = '0' + t_minute;
        show += t_minute + ':';

        t = t % Global.msOfMinute;
        let t_second = Math.floor(t / Global.msOfSecond);
        if (t_second < 10) t_second = '0' + t_second;
        show += t_second;
        return Lang.convert(show);
    },

    /**
     * 日历是否已打开
     * @returns {boolean}
     */
    isOpen:function(){
        return _$('Calendar')?true:false;
    },

    /**
     * 获得星期几的全名
     * @param time Date对象或当前时间的毫秒数，不设置则表示当前时间
     * @returns {string}
     */
    getDayName:function(time){
        if(!time) time=new Date();
        else if((typeof time)=='number') time=new Date(time);
        return this.daysFullName[time.getDay()];
    },

    /**
     * 得到年月日
     * @param time Date对象或当前时间的毫秒数，不设置则表示当前时间
     * @returns {(number|number)[]}
     */
    getYMD:function(time){
        if(!time) time=new Date();
        else if((typeof time)=='number') time=new Date(time);
        return [time.getFullYear(), time.getMonth()+1, time.getDate()];
    },

    /**
     * 是否闰年
     * @param year
     * @returns {boolean}
     */
    isLeapYear:function(year){
        return (year%4==0&&year%100!=0)||(year%100==0&&year%400==0);
    },

    /**
     * 得到指定月份的天数
     * @param year 年
     * @param month 月份
     * @returns {number}
     */
    getDaysOfMonth:function(year,month){
        if(month==1||month==3||month==5||month==7||month==8||month==10||month==12){
            return 31;
        }else if(month==2){
            if(this.isLeapYear(year)) return 29;
            else return 28;
        }else{
            return 30;
        }
    },

    //是否显示时分秒
    showHMS:false,

    //可选择的最小日期
    minDate:null,

    //可选择的最大日期
    maxDate:null,

    //关联的输入框(可多个)
    inputs:null,

    //设置可选择的最小时间
    setMinDate:function(minDate){
        if(minDate){
            if(minDate.length>10) minDate=minDate.substring(0,10);
            this.minDate=minDate;
        }else{
            this.minDate=null;
        }
    },

    //设置可选择的最大时间
    setMaxDate:function(maxDate){
        if(maxDate){
            if(maxDate.length>10) maxDate=maxDate.substring(0,10);
            this.maxDate=maxDate;
        }else{
            this.maxDate=null;
        }
    },

    //显示日历
    showCalendar:function(event, ystart, yend, input, lang, minDate, maxDate, callback){
        if(callback) this.onCalendarCloseCallback=callback;
        else this.onCalendarCloseCallback=null;

        if(!this.isOpen()){
            this.uuid=(new Date()).getTime();
            top.Layers.saveInstance(this.uuid, window, this);
            Page.pushState('','','');
        }

        if(input instanceof Array){
            this.inputs=[];
            for(let i=0; i<input.length; i++){
                if((typeof input[i])=='string') this.inputs[i]=_$(input[i]);
                else this.inputs[i]=input[i];
            }
        }else if((typeof input)=='string'){
            this.inputs=[_$(input)];
        }else{
            this.inputs=[input];
        }

        this.setMinDate(minDate);
        this.setMaxDate(maxDate);

        if(Str.isBlank(lang)) lang=Lang.getCurrentLang().id;

        let html=[];
        let inputDate=_$(input).value;
        let inputHours=this.inputHoursDef;
        let inputMinutes=this.inputMinutesDef;
        let inputSecends=this.inputSecendsDef;
        let setTime=false;
        if(inputDate.indexOf(' ')>0){
            setTime=true;
            let temp=inputDate.substring(inputDate.indexOf(' ')+1).split(':');
            if(temp.length==1){
                inputHours=temp[0];
            }else if(temp.length==2){
                inputHours=temp[0];
                inputMinutes=temp[1];
            }else if(temp.length==3){
                inputHours=temp[0];
                inputMinutes=temp[1];
                inputSecends=temp[2];
            }

            inputDate=inputDate.substring(0, inputDate.indexOf(' '));
        }

        let y=0;
        let m=0;
        let d=0;
        let start=0;
        let end=0;
        if(inputDate && inputDate.match(/^\d{4}-\d{0,2}-\d{2}$/)!=null){
            y=inputDate.substring(0,4)*1;
            m=inputDate.substring(5,7)*1;
            d=inputDate.substring(8,10)*1;
        }else{
            let date=new Date();
            y=date.getFullYear();
            m=date.getMonth()+1;
            d=date.getDate();
        }
        let date=new Date(y,m-1,1);
        start=date.getDay()+1;//当月第一天开始
        end=start+this.getDaysOfMonth(y,m)-1;//当月最后一天结束

        html.push('<div id="CalendarBg" style="z-index:'+W.getMaxZIndex()+' !important;"><iframe id="CalendarFrame" name="CalendarFrame" src="/blank.htm" width="100%" height="100%" frameborder="0" scrolling="no"></iframe></div>');
        html.push('<div id="Calendar" style="z-index:'+W.getMaxZIndex()+' !important;"><table>');
        html.push('<tr id="CalendarHead" class="trHead" style="cursor:move;">');
        html.push('<td colspan="7" class="alignL">');
        html.push('<div class="fl mL0"><select id="year" style="width:60px !important;" onchange="D.onCalendarChange(_$(\'year\').value*1, _$(\'month\').value*1,'+d+');">');
        for(let i=ystart; i<=yend; i++){
            if(i==y) html.push('<option value="'+i+'" selected>'+i+'</option>');
            else html.push('<option value="'+i+'">'+i+'</option>');
        }
        html.push('</select></div>');


        html.push('<div class="fl mL2"><select id="month" style="width:60px !important;" onchange="D.onCalendarChange(_$(\'year\').value*1,_$(\'month\').value*1,'+d+');">');
        for(let i=1;i<=12;i++){
            let v=i;
            if(v<10) v='0'+v;

            if(i==m) html.push('<option value="'+v+'" selected>'+i+'</option>');
            else html.push('<option value="'+v+'">'+i+'</option>');
        }
        html.push('</select></div>');

        html.push('<div class="CalendarClose" onclick="D.close(); try{D.onCalendarClose(\''+input+'\');}catch(e){}">'+Global.textClose+'</div>');
        html.push('<div class="CalendarClear" onclick="D.clear(); D.close(); try{D.onCalendarClose(\''+input+'\');}catch(e){}">'+Global.textClear+'</div>');
        html.push('</td>');
        html.push('</tr>');

        html.push('	<tr>')
        html.push('    	<td class="alignC">'+D.days[0]+'</td>');
        html.push('    	<td class="alignC">'+D.days[1]+'</td>');
        html.push('    	<td class="alignC">'+D.days[2]+'</td>');
        html.push('    	<td class="alignC">'+D.days[3]+'</td>');
        html.push('    	<td class="alignC">'+D.days[4]+'</td>');
        html.push('    	<td class="alignC">'+D.days[5]+'</td>');
        html.push('    	<td class="alignC">'+D.days[6]+'</td>');
        html.push('    </tr>');
        for(let i=1;i<=6;i++){
            html.push('	<tr>');
            for(let j=1;j<=7;j++){
                let sn=(i-1)*7+j;
                let v=(sn-start+1);
                if(v<10) v='0'+v;

                if(sn>=start&&sn<=end){
                    if((sn-start+1)==d){
                        html.push('    	<td class="CalendarCurrentD" id="day'+sn+'" v="'+v+'" onclick="D.choose(this,\''+input+'\');">'+(sn-start+1)+'</td>');
                    }else{
                        html.push('    	<td class="CalendarD" id="day'+sn+'" v="'+v+'" onclick="D.choose(this,\''+input+'\');">'+(sn-start+1)+'</td>');
                    }
                }else{
                    html.push('    	<td class="CalendarDEmpty" id="day'+sn+'" v="'+v+'" onclick="D.choose(this,\''+input+'\');">&nbsp;</td>');
                }
            }
            html.push('    </tr>');
        }

        if(this.showHMS){
            html.push('<tr class="trHead">');
            html.push('<td colspan="7" class="alignL">');
            html.push('<div class="wFull">');
            html.push('		<div class="fl mT3 hidden"><input type="checkbox" id="_d_set_time" checked/></div>');
            html.push('		<div class="fl"><select id="hours">');
            for(let i=0;i<=24;i++){
                let ii=i+'';
                if(ii.length<2) ii='0'+ii;
                html.push('		<option value="'+ii+'"'+(ii==inputHours?' selected':'')+'>'+ii+'</option>');
            }
            html.push('		</select></div><div class="fl alignC" style="width:9px;">:</div><div class="fl"><select id="minutes">');
            for(let i=0;i<=59;i++){
                let ii=i+'';
                if(ii.length<2) ii='0'+ii;
                html.push('		<option value="'+ii+'"'+(ii==inputMinutes?' selected':'')+'>'+ii+'</option>');
            }
            html.push('		</select></div><div class="fl alignC" style="width:9px;">:</div><div class="fl"><select id="seconds">');
            for(let i=0;i<=59;i++){
                let ii=i+'';
                if(ii.length<2) ii='0'+ii;
                html.push('		<option value="'+ii+'"'+(ii==inputSecends?' selected':'')+'>'+ii+'</option>');
            }
            html.push('		</select></div>');
            html.push('	<div class="fr"><div class="CalendarOk" onclick="D.choose(null,\''+input+'\');">'+Global.textOk+'</div></div>');
            html.push('</div>');
            html.push('	</td>');
            html.push(' </tr>');
        }
        html.push('</table></div>');
        html=html.join('');

        document.body.insertAdjacentHTML('afterbegin', Lang.convert(html));
        html=null;

        _$('Calendar').style.left=this.getLeft(event)+'px';
        _$('Calendar').style.top=this.getTop(event)+'px';

        _$('CalendarBg').style.height=W.h()+'px';
        _$('CalendarBg').style.width='100%';
        _$('CalendarBg').style.top='0px';
        _$('CalendarBg').style.left='0px';

        _$('CalendarBg').style.visibility='visible';
        _$('Calendar').style.visibility='visible';

        new Movable('CalendarHead', 'Calendar', null, null, null, null);

        this.setDisabled();
    },

    getTop:function(event){
        let theTop=Math.floor((top.W.vh()-W.elementHeight(_$('Calendar')))/2);
        return Math.max(theTop, 10);
    },

    getLeft:function(event){
        let theLeft=Math.ceil((W.vw()-W.elementWidth(_$('Calendar')))/2);
        return Math.max(theLeft, 10);
    },

    onCalendarChange:function(y,m,d){
        let ds=_$cls('CalendarCurrentD');
        for(let i=0;i<ds.length;i++) ds[i].className='';

        ds=_$cls('CalendarD');
        for(let i=0;i<ds.length;i++) ds[i].className='';

        _$('year').value=y;
        _$('month').value=m>9?m:('0'+m);
        let date=new Date(y,m-1,1);
        let start=date.getDay()+1;
        let end=start+this.getDaysOfMonth(y,m)-1;

        for(let i=1;i<=6;i++){
            for(let j=1;j<=7;j++){
                let sn=(i-1)*7+j;
                let v=(sn-start+1);
                if(v<10) v='0'+v;
                Utils.setAtt(_$('day'+sn),'v',v);

                if(sn>=start&&sn<=end){
                    _$('day'+sn).innerHTML=''+(sn-start+1);
                    _$('day'+sn).style.cursor='pointer';
                    if((sn-start+1)==d){
                        _$('day'+sn).className='CalendarCurrentD';
                    }else{
                        _$('day'+sn).className='CalendarD';
                    }
                }else{
                    _$('day'+sn).innerHTML='&nbsp;';
                    _$('day'+sn).style.cursor='normal';
                    _$('day'+sn).style.backgroundColor='';
                }
            }
        }

        this.setDisabled();
    },

    setDisabled:function(){
        let y=_$('year').value*1;
        let m=_$('month').value*1;

        let date=new Date(y,m-1,1);
        let start=date.getDay()+1;
        let end=start+this.getDaysOfMonth(y,m)-1;

        for(let i=1;i<=6;i++){
            for(let j=1;j<=7;j++){
                let sn=(i-1)*7+j;
                let v=(sn-start+1);
                if(v<10) v='0'+v;

                let _v=y+'-'+(m<10?('0'+m):m)+'-'+v;

                if(sn>=start&&sn<=end){
                    _$('day'+sn).innerHTML=(sn-start+1);

                    if((this.minDate&&_v<this.minDate)
                        ||(this.maxDate&&_v>this.maxDate)){
                        _$('day'+sn).style.color='#CCCCCC';
                        _$('day'+sn).style.cursor='not-allowed';
                    }else{
                        _$('day'+sn).style.color='#333333';
                        _$('day'+sn).style.cursor='pointer';
                        Utils.setAtt(_$('day'+sn),'v',v);
                    }
                }else{
                    _$('day'+sn).innerHTML='&nbsp;';
                    _$('day'+sn).style.cursor='normal';
                    _$('day'+sn).style.backgroundColor='';
                }
            }
        }
    },

    clear:function(){
        for(let x=0;x<this.inputs.length;x++) this.inputs[x].value='';
    },

    close:function(){
        if(this.uuid) top.Layers.delInstance(this.uuid);
        if(_$('CalendarBg')) _$('CalendarBg').parentNode.removeChild(_$('CalendarBg'));
        if(_$('Calendar')) _$('Calendar').parentNode.removeChild(_$('Calendar'));
    },

    choose:function(td,input){
        if(!this.showHMS){
            if(td.innerHTML.match(/\d{1,2}/)==null) return;

            if(td.style.cursor=='not-allowed'){
                Page.alert('I{calendar,请选择允许的日期}');
                return;
            }
            let temp=_$(input).value=_$('year').value+'-'+_$('month').value+'-'+Utils.att(td,'v');
            for(let x=0;x<this.inputs.length;x++){
                this.inputs[x].value=temp;
            }

            this.minDate=null;
            this.maxDate=null;

            try{D.onCalendarClose(input);}catch(e){}

            this.close();
        }else{
            if(td){
                if(td.style.cursor=='not-allowed'){
                    Page.alert('I{calendar,请选择允许的日期}');
                    return;
                }

                if(td.innerHTML.match(/\d{1,2}/)==null) return;
                let ds=_$cls('CalendarCurrentD');
                for(let i=0;i<ds.length;i++) ds[i].className='CalendarD';
                td.className='CalendarCurrentD';
            }else{
                let ds=_$cls('CalendarCurrentD');
                if(!ds||ds.length==0){
                    Page.alert('I{calendar,请选择日期}');
                    return;
                }
                td=ds[0];

                if(td.style.cursor=='not-allowed'){
                    Page.alert('I{calendar,请选择允许的日期}');
                    return;
                }

                if(td.innerHTML.match(/\d{1,2}/)==null){
                    Page.alert('I{calendar,请选择日期}');
                    return;
                }

                let temp=_$(input).value=_$('year').value+'-'+_$('month').value+'-'+Utils.att(td,'v');
                if(_$('_d_set_time')&&_$('_d_set_time').checked) temp+=' '+_$('hours').value+':'+_$('minutes').value+':'+_$('seconds').value;

                for(let x=0;x<this.inputs.length;x++){
                    this.inputs[x].value=temp;
                }

                this.minDate=null;
                this.maxDate=null;

                try{D.onCalendarClose(input);}catch(e){}

                this.close();
            }
        }
    }
}