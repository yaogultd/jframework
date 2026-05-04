let AIGC = {
    callback: null,
    callbackTarget: null,

    showAIGCDialog:function (prompt, callback, callbackTarget){
        this.callback=callback ? callback : null;
        this.callbackTarget=callbackTarget ? callbackTarget : window;

        if(Str.isBlank(prompt)) prompt='';
        let htm=[];
        htm.push('	<div class="hidden">');
        htm.push('	    <form name="aigcForm" id="aigcForm" action="." method="post" encType="multipart/form-data">');
        htm.push('	        <input type="hidden" name="message" value=""/>');
        htm.push('	    </form>');
        htm.push('	</div>');
        htm.push('<div class="r">');
        htm.push('    <textarea type="text" id="aigc_prompt" maxlength="1024" placeholder="I{AIGC,请向AI描述您要生成的图片}" style="width: 100%; height: 100px;">'+prompt+'</textarea></div>');
        htm.push('</div>');
        htm.push('<div class="r">');
        htm.push('<div class="fl fileInputWithSkin w100">');
        htm.push('    <div class="skin">');
        htm.push('        <div class="aBtnWithIcon w100">');
        htm.push('            <div class="aIcon iconfont icon-folder" id="aigc_attachment_icon" onclick="AIGC.aigcAttachmentsDelete();"></div>');
        htm.push('            <div class="aText" id="aigc_attachment_name">I{AIGC,选择文件}</div>');
        htm.push('        </div>');
        htm.push('    </div>');
        htm.push('    <div class="file" style="left: 20px;"><input type="file" id="aigc_attachment" onchange="AIGC.aigcAttachmentsSelected();"/></div>');
        htm.push('</div>');

        //图片尺寸
        htm.push('<div class="fl mL10">');
        htm.push('<select id="aigc_size" class="selectH30">');
        htm.push('<option value="1024x1024">I{.尺寸}</option>');
        htm.push('<option value="1024x1024">1024x1024</option>');
        htm.push('<option value="1792x1024">1792x1024</option>');
        htm.push('<option value="1024x1792">1024x1792</option>');
        htm.push('</select>');
        htm.push('</div>');

        //图片风格
        htm.push('<div class="fl mL10">');
        htm.push('<select id="aigc_style" class="selectH30">');
        htm.push('<option value="fantasy">I{.风格}</option>');
        htm.push('<option value="fantasy">I{.超现实的}</option>');
        htm.push('<option value="natural">I{.自然的}</option>');
        htm.push('</select>');
        htm.push('</div>');

        htm.push('<div class="fr" id="aigcProcessing" style="display: none;"><img src="/framework/img/loading.gif" style="height: 30px; width: auto;"/> </div>');
        htm.push('</div>');

        top.Dialog.open(-1,-1,Math.min(W.vw() - 40, 360),-1,null,null,window,'dialog');
        top.Dialog.setTitle('I{AIGC,AI生图}');
        top.Dialog.setContent(htm);
        top.Dialog.setBtns(['<div class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="AIGC.aigcRequest();">I{AIGC,发出指令}</div>',
            '<div class="btnH40 btnBgGray displayBlock mL5" style="width:45%;" onclick="top.Dialog.close();">I{取消}</div>']);
        htm=null;
        delete htm;
    },

    aigcAttachmentsDelete:function (){
        top._$('aigc_attachment_name').innerHTML=Lang.convert('I{AIGC,选择文件}');;
        top._$('aigc_attachment_icon').className='aIcon iconfont icon-folder';
        top._$('aigc_attachment').value=''
    },

    aigcAttachmentsSelected:function(){
        if(top._$('aigc_attachment').files.length>1){
            Page.alert('I{AIGC,一次只能选择一个文件}');
            return;
        }

        //压缩文件
        for(let f=0;f<top._$('aigc_attachment').files.length;f++){
            let fileName=top._$('aigc_attachment').files[f].name.toLowerCase();
            if(fileName.indexOf('.') < 0){
                Page.alert('I{AIGC,无效的文件类型} - '+fileName);
                AIGC.aigcAttachmentsDelete();
                return;
            }

            let fileExt=fileName.substring(fileName.lastIndexOf('.') + 1);
            if(!Str.contains(['jpg','jpeg','png'], fileExt)){
                Page.alert('I{AIGC,无效的文件类型} - '+fileName);
                AIGC.aigcAttachmentsDelete();
                return;
            }
        }

        if(top._$('aigc_attachment').files.length>0){
            top._$('aigc_attachment_name').innerHTML=top._$('aigc_attachment').files[0].name;
            top._$('aigc_attachment_icon').className='aIcon iconfont icon-delete_light';
        }else{
            AIGC.aigcAttachmentsDelete();
        }
    },

    aigcRequest:function (){
        let prompt = Str.trimAll(top._$('aigc_prompt').value);
        if(top._$('aigc_attachment').value=='' && Str.isBlank(prompt)){
            alert(Lang.convert('I{AIGC,描述和文件不能同时为空}'));
            return;
        }

        if(!Str.isBlank(prompt) && prompt.length < 5){
            alert(Lang.convert('I{AIGC,描述少于5个字符，AI很可能无法理解您的要求，请说得更详细一点}'));
            return;
        }

        if(prompt.length>1024){
            alert(Lang.convert('I{AIGC,描述不能超过1024个字符}'));
            return;
        }

        let _act = '/api/platform/shopping/customizing/ai/image/generate?size='+top._$('aigc_size').value+'&imageStyle='+top._$('aigc_style').value;
        aigcForm.action=_act;
        aigcForm.message.value=prompt;

        let multiparts=[];
        if(top._$('aigc_attachment').files && top._$('aigc_attachment').files.length>0){
            multiparts.push({
                name: 'attachment',
                data: top._$('aigc_attachment').files[0],
                fileName: top._$('aigc_attachment').files[0].name
            });
        }

        top._$('aigcProcessing').style.display='';
        (new Ajax()).sendForm(aigcForm, AIGC.doAigcRequest, multiparts);
    },

    doAigcRequest:function (ajax){
        if(ajax.getReadyState()==4&&ajax.getStatus()==200) {
            top._$('aigcProcessing').style.display='none';
            let resp = ajax.getResponseJson();
            if(!resp.success) {
                Page.alert(resp.message, null, null, Dialog.MSG_TYPE_ERR);
                return;
            }

            if(resp.datas.vision){
                AIGC.aigcAttachmentsDelete();
                top._$('aigc_prompt').value=resp.datas.vision;
                return;
            }

            top.Dialog.close();
            if(AIGC.callback){
                AIGC.callback.call(AIGC.callbackTarget, resp.datas.image);
            }
        }
    }
}
window.AIGC=AIGC;