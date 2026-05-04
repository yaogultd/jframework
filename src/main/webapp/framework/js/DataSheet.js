let DataSheets = {
    instances: [],

    getInstance: function (id) {
        return this.instances[id];
    },

    show: function (ajax){
        if(!ajax || !ajax.DataSheetId) return;

        let sheet = DataSheets.getInstance(ajax.DataSheetId);
        if(!sheet) return;

        let args = ajax.args;
        if(ajax.getReadyState()==4&&ajax.getStatus()==200){
            try{
                if(args.beforeShown){
                    ajax.doNotDestoryAfterFinished=true;
                    let beforeShown=args.beforeShown;
                    ajax.args.beforeShown=null;
                    beforeShown.call(args.beforeShownCaller ? args.beforeShownCaller : window, ajax);
                    return;
                }

                if(sheet.data){
                    sheet.data=null;
                    delete sheet.data;
                }

                if(sheet.dataExtra){
                    sheet.dataExtra=null;
                    delete sheet.dataExtra;
                }

                let resp=ajax.getResponseJson();
                if(!resp.success){
                    if(args.onFail) args.onFail.call(args.onFailCaller ? args.onFailCaller : window, this.id);
                    else{
                        if(resp.code=='non_login' || resp.code=='not_login' || resp.code=='-login'){
                            Auth.showNotLoginMessage();
                        }else{
                            top.Dialog.close();
                        }
                    }

                    resp=null;
                    delete resp;
                    return;
                }

                let datas = resp.datas;
                if(!datas){
                    if(args.onFail) args.onFail.call(args.onFailCaller ? args.onFailCaller : window, sheet);
                    else top.Dialog.close();

                    resp=null;
                    delete resp;

                    datas=null;
                    delete datas;
                    return;
                }

                let total = datas[sheet.dataCountTag]*1;
                if((typeof total) != 'number') total=0;
                sheet.dataCount = total;

                //生成分页
                if(sheet.pagingEnabled){
                    if(sheet.paging){
                        sheet.paging=null;
                        delete sheet.paging;
                    }

                    sheet.paging=new Paging('DataSheet_Paging_'+sheet.id,
                        ['DataSheet_Paging_Container_'+sheet.id],
                        sheet.dataRpp,
                        5,
                        sheet.dataPn,
                        sheet.dataCount,
                        function (pn){
                            sheet.pull({dataPn: pn});
                        },
                        true,
                        true,
                        false);

                    sheet.paging.build();
                }

                let rows = datas[sheet.dataTag];
                sheet.data = rows;

                if(sheet.dataTagExtra){
                    sheet.dataExtra = datas[sheet.dataTagExtra];
                }

                if(rows && (typeof rows.length) == 'number'){
                    if(!sheet.sheetType || sheet.sheetType=='table') {//表格形式
                        let table = _$('DataSheet_' + sheet.id);
                        while (table.rows.length > 1) table.deleteRow(-1);

                        for (let i = 0; i < rows.length; i++) {
                            let row = rows[i];

                            let tr = document.createElement('tr');
                            tr.id = 'DataSheet_' + sheet.id+'_row_'+i;
                            tr.className = 'r1';

                            for (let j = 0; j < sheet.columns.length; j++) {
                                let td = document.createElement('td');
                                tr.appendChild(td);

                                let v = sheet.columns[j].parentId ? row[sheet.columns[j].parentId][sheet.columns[j].id] : row[sheet.columns[j].id];
                                if (!v) v = '';
                                td.innerHTML = Lang.convert(sheet.columns[j].processor.toText(v, row, i, j));
                            }

                            table.appendChild(tr);
                        }
                    }else{//瀑布式列表模式
                        let table = _$('DataSheet_' + sheet.id);
                        table.innerHTML='';

                        for (let i = 0; i < rows.length; i++) {
                            let row = rows[i];

                            let tr = document.createElement('div');
                            tr.id = 'DataSheet_' + sheet.id+'_row_'+i;
                            tr.className = 'DS_row';
                            if(i==rows.length-1) tr.style.marginBottom='0px';
                            table.appendChild(tr);

                            if(sheet.withRowHeader){
                                let header = document.createElement('div');
                                header.id = 'DataSheet_' + sheet.id+'_header_'+i;
                                header.className = 'DS_header';
                                tr.appendChild(header);

                                let summary = document.createElement('div');
                                summary.className = 'DS_header_summary';
                                header.appendChild(summary);
                                summary.innerHTML=Lang.convert(sheet.rowHeaderProcessor.genSummary(row, i));

                                let unfolder = document.createElement('div');
                                unfolder.className = 'DS_row_unfolder';
                                header.appendChild(unfolder);
                                unfolder.innerHTML='<div class="iconfont '+(sheet.unfoldOnLoad?'icon-less':'icon-moreunfold')+'" onclick="DataSheets.unfold(this, \''+sheet.id+'\', '+i+');"></div>';

                                let btns = document.createElement('div');
                                btns.className = 'DS_header_btns';
                                header.appendChild(btns);
                                btns.innerHTML=Lang.convert(sheet.rowHeaderProcessor.genOperationBtns(row, i));
                            }

                            let rowDetail = document.createElement('div');
                            rowDetail.id = 'DataSheet_' + sheet.id+'_detail_'+i;
                            rowDetail.className = 'DS_row_detail';
                            if(!sheet.unfoldOnLoad) rowDetail.style.display='none';
                            tr.appendChild(rowDetail);

                            for (let j = 0; j < sheet.columns.length; j++) {
                                let v = sheet.columns[j].parentId ? row[sheet.columns[j].parentId][sheet.columns[j].id] : row[sheet.columns[j].id];
                                if (!v) v = '';

                                if(!sheet.columns[j].showEmpty && Str.isBlank(v)) continue;

                                let column = document.createElement('div');
                                column.className = 'DS_row_column';
                                rowDetail.appendChild(column);

                                let columnName = document.createElement('div');
                                columnName.className = 'DS_row_column_name';
                                column.appendChild(columnName);
                                if((typeof sheet.columns[j].processor.getName) != 'undefined'){
                                    columnName.innerHTML=Lang.convert(sheet.columns[j].processor.getName(v, row, i, j));
                                }else{
                                    columnName.innerHTML=Lang.convert(sheet.columns[j].name);
                                }

                                let columnContent = document.createElement('div');
                                columnContent.className = 'DS_row_column_content';
                                column.appendChild(columnContent);

                                columnContent.innerHTML = Lang.convert(sheet.columns[j].processor.toText(v, row, i, j));
                            }
                        }
                    }
                }

                resp=null;
                delete resp;

                datas=null;
                delete datas;

                top.Dialog.close();
                if(args.onFinish) args.onFinish.call(args.onFinishCaller ? args.onFinishCaller : window, sheet);
            }catch(e){
                console.log(e);
                if(args.onFail) args.onFail.call(args.onFailCaller ? args.onFailCaller : window, sheet);
            }

            ajax.clear();
        }
    },

    unfold: function (unfolder, id, rowIndex){
        let sheet=DataSheets.getInstance(id);
        let detail= _$('DataSheet_' + id+'_detail_'+rowIndex);
        if(unfolder.className.indexOf('less')>0){//当前为展开状态
            unfolder.className='iconfont icon-moreunfold';
            detail.style.display='none';
        }else{
            unfolder.className='iconfont icon-less';
            detail.style.display='';
        }
    }
}

function DataSheet(args){
    this.container = (typeof args.container) == 'string' ? _$(args.container) : args.container;
    this.id=args.id;
    this.name=args.name;
    this.className=args.className ? args.className : 'table';//表格样式
    this.columns=args.columns;//数据列
    this.dataUrl=args.dataUrl;//获取数据的url
    this.dataQueries=args.dataQueries;//数据查询条件
    this.dataPullMethod=args.dataPullMethod;//GET / POST
    this.dataCount=0;
    this.dataRpp=args.dataRpp ? args.dataRpp : 10;
    this.dataPn=1;
    this.dataTag=args.dataTag ? args.dataTag : 'rows';//记录数组的json标签
    this.dataTagExtra=args.dataTagExtra;
    this.dataCountTag=args.dataCountTag ? args.dataCountTag : 'total';//总记录数的json标签
    this.pagingEnabled=(typeof args.pagingEnabled) == 'boolean' ? args.pagingEnabled : true;
    this.unfoldOnLoad=(typeof args.unfoldOnLoad) == 'boolean' ? args.unfoldOnLoad : false;//是否默认展开记录详情
    this.withRowHeader=(typeof args.withRowHeader) == 'boolean' ? args.withRowHeader : true;//是否显示记录摘要
    this.rowHeaderProcessor=args.rowHeaderProcessor;//记录摘要处理器（实现genSummary/genOperationBtns方法，即生成摘要和操作按钮）
    this.paging=null;
    this.data=null;
    this.dataExtra=null;
    this.sheetType=args.sheetType;//列表形式：flow/table
    DataSheets.instances[this.id] = this;
}
DataSheet.prototype.init=function(){
    if(!this.sheetType || this.sheetType=='table') {//表格形式
        let header = document.createElement('tr');
        header.className = 'h';
        for (let i = 0; i < this.columns.length; i++) {
            let column = this.columns[i];
            let th = document.createElement('th');
            if (column.colspan > 1) Utils.setAtt(th, 'colspan', column.colspan);
            if (column.rowspan > 1) Utils.setAtt(th, 'rowspan', column.rowspan);
            if (column.width) Utils.setAtt(th, 'width', column.width);
            th.style.textAlign = column.textAlign;
            th.innerHTML = Lang.convert(column.name);
            th.id = 'header_' + column.id;
            header.appendChild(th);
        }
        let sheet = document.createElement('table');
        sheet.id = 'DataSheet_' + this.id;
        sheet.className = this.className;
        sheet.appendChild(header);
        this.container.appendChild(sheet);
    }else{//瀑布式列表模式
        let sheet = document.createElement('div');
        sheet.className='DataSheet';
        sheet.id = 'DataSheet_' + this.id;
        sheet.className = this.className;
        this.container.appendChild(sheet);
    }

    if(this.pagingEnabled){
        let pagingContainer = document.createElement('div');
        pagingContainer.className = 'r';
        pagingContainer.id = 'DataSheet_Paging_Container_'+this.id;
        this.container.appendChild(pagingContainer);
    }
}
DataSheet.prototype.pull=function(args){
    if(args.onStart) args.onStart.call(args.onStartCaller ? args.onStartCaller : window, this);
    else top.Dialog.open(-1, -1, -1, -1, null, null, window, 'waiting', null, null, null, null, null, 30000, null, '<a class="aunderline" href="javascript:_void();" onclick="top.Dialog.close();">I{auth,等待超时，请重试}</a>');

    let queries = args.dataQueries ? args.dataQueries : this.dataQueries;
    let pn = args.dataPn ? args.dataPn : this.dataPn;
    let url = this.dataUrl;
    url += (url.indexOf('?') > 0 ? '&' : '?')
    url+='pn='+pn;
    url+='&rpp='+this.dataRpp;

    this.dataPn=pn;

    try{
        let ajax = new Ajax()
        ajax.args=args;
        ajax.DataSheetId=this.id;
        if('GET' == this.dataPullMethod) ajax.send('GET', DataSheets.show, url);
        else ajax.send('POST', DataSheets.show, url, queries);
    }catch(e){
        console.log(e);
        if(args.onFail) args.onFail.call(args.onFailCaller ? args.onFailCaller : window, this);
    }
}

/**
 * 数据列
 * @param args
 * @constructor
 */
function DataColumn(args){
    this.id=args.id;
    this.parentId=args.parentId;
    this.name=args.name;
    this.width=args.width;
    this.showEmpty=(typeof args.showEmpty) == 'boolean' ? args.showEmpty : true;//是否显示内容为空的列（flow模式）
    this.colspan=args.colspan ? args.colspan : 1;
    this.rowspan=args.rowspan ? args.rowspan : 1;
    this.textAlign=args.textAlign ? args.textAlign : 'center';
    this.processor=args.processor ? args.processor : DataColumnProcessor;
}

/**
 * 默认列数据处理器
 * @type {{toValue: (function(*): *), toText: (function(*): *)}}
 */
const DataColumnProcessor={
    /**
     * 将从服务端取到的值转换为前端显示的文本
     * @param s
     * @returns {*}
     */
    toText:function (s, row){
        return s==null ? '' : s;
    },

    /**
     * 将前端文本转换成服务端保存的值
     * @param s
     * @returns {*}
     */
    toValue:function (s){
        return s==null ? '' : s;
    }
}