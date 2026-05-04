//无任何操作
function _void(){}

//全局变量
//Updated 2022-09-07
let Global={
	textOk:'',
	textCancel:'',
	textClear:'',
	textClose:'',
	onI18NChanged:function(){
		this.textOk=Lang.convert('I{确定}');
		this.textCancel=Lang.convert('I{取消}');
		this.textClear=Lang.convert('I{清除}');
		this.textClose=Lang.convert('I{关闭}');
	},

	//是否调试模式
	DEBUG:true,

	//本地路径（测试时用）
	localRoot:'',

	//时间常量（毫秒数）
	msOfDay:3600000*24,
	msOfHour:3600000,
	msOfMinute:60000,
	msOfSecond:1000,

	photoTypes:['jpg', 'jpeg', 'png', 'gif', 'svg', 'gif'],
	videoTypes:['mp4', '3gp', 'mov'],
	audioTypes:['mp3', 'm4a', 'amr', 'acc'],

	//屏幕大小
	screenWidth:screen.availWidth,
	screenHeight:screen.availHeight,

	//更新
	renew:function (){
		this.screenWidth=screen.availWidth;
		this.screenHeight=screen.availHeight;
	},

	//UUID
	uuidIncrement:0,
	generateUUID:function(){
		return ''+((new Date()).getTime()+this.uuidIncrement++);
	}
}
window.Global=Global;

//日志输出
let Logger={
	log:function(content){
		if(Global.DEBUG) console.log(content);
	},
	err:function(content){
		console.error(content);
	},
	warn:function(content){
		console.warn(content);
	}
}
window.Logger=Logger;

//处理版本兼容问题
if((typeof String.prototype.startsWith)=='undefined'){
	String.prototype.startsWith=function(prefix){
		if(!prefix||prefix===''||(typeof prefix)!='string') return false;
		return this.indexOf(prefix)==0;
	};
}

if((typeof String.prototype.endsWith)=='undefined'){
	String.prototype.endsWith=function(suffix){
		if(!suffix||suffix===''||(typeof suffix)!='string') return false;
		return this.indexOf(suffix)==this.length-suffix.length;
	};
}

if((typeof String.prototype.replaceAll)=='undefined'){
	String.prototype.replaceAll=function(s1,s2){
		try{
			return this.replace(new RegExp(s1,"gm"),s2);
		}catch(e){
			return s1;
		}
	};
}

//低版本浏览不支持getElementsByClassName方法
if(!document.getElementsByClassName){
	document.getElementsByClassName = function(clsName){
		let divs=document.getElementsByTagName('div');
		let temp=[];
		for(let i=0;i<divs.length;i++){
			if(divs[i].className==clsName) temp.push(divs[i]);
		}
		return temp;
	}
}

/**
 *
 * @param id
 * @returns {HTMLElement|*}
 * @private
 */
function _$(id){
	let obj=document.getElementById(id);
	if(!obj){
		let ofName=document.getElementsByName(id);
		if(ofName!=null&&ofName.length==1) obj=ofName[0];
	}
	if(obj || !top.Dialog.isOpen()){
		return obj;
	}

	if(top==window
		&& top.Dialog.win
		&& top.Dialog.win != window
		&& (typeof top.Dialog.win.__$)=='function'){//当前是最上层窗口，且Dialog是由子窗口打开的，从打开Dialog的子窗口查找
		return top.Dialog.win.__$(id);
	}

	if(top!=window
		&& top.Dialog.win
		&& top.Dialog.win == window
		&& (typeof top.__$)=='function'){//当前是子窗口，并且Dialog是从本窗口打开的，则从顶层窗口查找
		return top.__$(id);
	}
}
function _$$(inheritFromTop, id){
	if(top!=window && inheritFromTop) return top._$$(false, id);

	let obj=document.getElementById(id);
	if(!obj){
		let ofName=document.getElementsByName(id);
		if(ofName!=null&&ofName.length==1) obj=ofName[0];
	}
	if(obj || !top.Dialog.isOpen()){
		return obj;
	}

	if(top==window
		&& top.Dialog.win
		&& top.Dialog.win != window
		&& (typeof top.Dialog.win.__$)=='function'){//当前是最上层窗口，且Dialog是由子窗口打开的，从打开Dialog的子窗口查找
		return top.Dialog.win.__$(id);
	}

	if(top!=window
		&& top.Dialog.win
		&& top.Dialog.win == window
		&& (typeof top.__$)=='function'){//当前是子窗口，并且Dialog是从本窗口打开的，则从顶层窗口查找
		return top.__$(id);
	}
}

//根据ID得到对象
function __$(id){
	let obj=document.getElementById(id);
	if(!obj){
		let ofName=document.getElementsByName(id);
		if(ofName!=null&&ofName.length==1) obj=ofName[0];
	}
	return obj;
}

//包含css类的对象
function _$cls(clsName){
	return Utils.arrayCopy(document.getElementsByClassName(clsName));
}
function _$$cls(inheritFromTop, clsName){
	if(top!=window && inheritFromTop) return top._$$cls(false, clsName);
	return Utils.arrayCopy(document.getElementsByClassName(clsName));
}

//得到名字为name的对象列表
function _$n(name, inheritFromTop){
	if(top!=window && inheritFromTop) return top._$n(name);
	return Utils.arrayCopy(document.getElementsByName(name));
}
function _$$n(inheritFromTop, name){
	if(top!=window && inheritFromTop) return top._$$n(false, name);
	return Utils.arrayCopy(document.getElementsByName(name));
}

//加载JS
let loadJS = function (params) {
	let script = document.createElement('script');

	if(script.readyState){
		script.onreadystatechange = function(){
			// 如果script已经下载完成
			if(script.readyState == 'complete' || script.readyState == 'loaded'){
				Logger.log('script.readyState -> '+params['src']);
				if(params['callback'] && (typeof params['callback'] == 'function')){
					params['callback'](params['src']);
				}
			}
		}
	}else{
		// 监听script的下载的状态 当状态变为下载完成后 再执行callback
		script.onload = function(){
			Logger.log('script.onload -> '+params['src']);
			if(params['callback'] && (typeof params['callback'] == 'function')){
				params['callback'](params['src']);
			}
		}
	}

	script.onerror = function (){
		Logger.log('script.onerror -> '+params['src']);
		if(params['callback'] && (typeof params['callback'] == 'function')){
			params['callback'](params['src']);
		}
	}

	Utils.setAtt(script, 'charset', params['charset'] || document['charset'] || document['charset'] || 'utf-8');
	for(let i in params){
		let temp=i.toLowerCase();
		if(temp=='charset'||temp=='src'||(typeof params[i]=='function')) continue;
		Utils.setAtt(script,i,params[i]);
	}

	script.src = params['src'];
	try {
		document.body.appendChild(script);
	} catch (e) {}
}
window.loadJS=loadJS;

//按顺序加载一组JS
let QueuedJSIterator={
	instances:[],

	next:function(url){
		let instance=QueuedJSIterator.findInstance(url);
		if(!instance) return;

		instance.next(url);
	},

	findInstance:function (url){
		for(let i=0; i<this.instances.length; i++){
			if(this.instances[i].contains(url)) return this.instances[i];
		}
		return null;
	}
}
window.QueuedJSIterator=QueuedJSIterator;

function QueuedJS(urls, charsets, callback){
	this.urls=urls;
	this.charsets=charsets;
	this.callback=callback;
	this.loaded=[];
	this.index=0;

	QueuedJSIterator.instances.push(this);

	this.next();
}
QueuedJS.prototype.next=function(url){
	if(url){
		if(this.loaded[url]) return;
		this.loaded[url]=true;
	}

	if(this.index>=this.urls.length){
		if(this.callback) this.callback();
		return;
	}

	let current=this.index;
	this.index++;

	loadJS({"src":this.urls[current], "charset":this.charsets[current], "callback":QueuedJSIterator.next});
}
QueuedJS.prototype.contains=function(url){
	for(let i=0; i<this.urls.length; i++){
		if(url==this.urls[i]) return true;
	}
	return false;
}

//动态添加/更新css
let loadCSS = function(id, css){
	let style = document.getElementById(id);
	if(style){//已经存在
		if(Str.isBlank(css)){//删除
			Logger.log('delete css '+id);
			document.head.removeChild(style);
		}else{
			Logger.log('update css '+id);
			style.textContent=css;
		}
		return;
	}

	//新增
	Logger.log('add css '+id);
	style=document.createElement('style');
	style.id=id;//设置id为了更新和删除时查询方便
	style.textContent = css;
	style.setAttribute('type','text/css')
	document.head.appendChild(style);
}
window.loadCSS=loadCSS;

let loadCSSFromUrl = function(params){
	//Logger.log('load css '+params['src']);
	let style=document.createElement('link');

	if(style.readyState){
		style.onreadystatechange = function(){
			// 如果style已经下载完成
			if(style.readyState == 'complete' || style.readyState == 'loaded'){
				Logger.log('style.readyState -> '+params['src']);
				if(params['callback'] && (typeof params['callback'] == 'function')){
					params['callback'](params['src']);
				}
			}
		}
	}else{
		// 监听style的下载的状态 当状态变为下载完成后 再执行callback
		style.onload = function(){
			Logger.log('style.onload -> '+params['src']);
			if(params['callback'] && (typeof params['callback'] == 'function')){
				params['callback'](params['src']);
			}
		}
	}

	style.onerror = function (){
		Logger.log('style.onerror -> '+params['src']);
		if(params['callback'] && (typeof params['callback'] == 'function')){
			params['callback'](params['src']);
		}
	}

	style.setAttribute('type','text/css');
	style.setAttribute('rel','stylesheet');
	style.href=params['src'];
	document.head.appendChild(style);
}
window.loadCSSFromUrl=loadCSSFromUrl;

//字符串相关功能
let Str={
	//是否为空
	isBlank:function(s){
		return ((typeof s)=='undefined') || s==null || s==='';
	},

	equals:function (s1, s2){
		if(this.isBlank(s1) && this.isBlank(s2)) return true;
		return s1===s2;
	},

	//全部替换
	replaceAll:function(str, original, alternative){
		if(this.isBlank(str)) return str;
		return str.replaceAll(original,alternative);
	},

	//替换尾部
	replaceLast:function(str, original, alternative){
		if(this.isBlank(str)) return str;
		if(!str.endsWith(original)) return str;
		return str.substring(0,str.length-original.length)+alternative;
	},

	//去掉首尾空格
	trimAll:function(str){
		if(this.isBlank(str)) return str;
		return str.replace(/^\s+|\s+$/g, '');
	},

	//判断密码是合格
	passwordValid:function(v, minChars, maxChars){
		if(this.isBlank(v)) return false;
		//需要转义的字符：* . ? + $ ^ - | [ ] ( ) { }  \ /
		if(!v.match(/^[a-zA-Z0-9!@#$%\^&\*\(\)_\-\+=\.\?;:,]{1,128}$/)) return false;
		if(minChars && v.length<minChars) return false;
		if(maxChars && v.length>maxChars) return false;
		return true;
	},

	//清除密码中的非法字符
	passwordClear:function(v){
		if(this.isBlank(v)) return v;
		return v.replace(/[^a-zA-Z0-9!@#$%\^&\*\(\)_\-\+=\.\?;:,]/g, '');
	},

	//密码可包含哪些字符
	passwordChars:function (){
		return 'a-z A-Z 0-9 !@#$%^&*()_-+=.?;:,';
	},

	//是否有效邮箱
	emailValid:function (v, maxLength){
		return (v.match(/^[\w\.]{1,}@{1}[\w\.]{1,}$/) && v.length<=maxLength);
	},

	//计算字符串长度（字节数）
	bytes:function(str,encoding){
		if(this.isBlank(str)) return 0;
		if(this.isBlank(encoding)||encoding.toUpperCase()=='UTF-8'){
			str=str.replace(/[\u4E00-\u9FA5]/g, 'xxx');
		}else{
			str=str.replace(/[\u4E00-\u9FA5]/g, 'xx');
		}
		return str.length;
	},

	//数组arr中是否包含字符串str
	contains:function(arr,str){
		if(!arr||arr.length==0) return false;
		if(this.isBlank(str)) return false;

		for(let i=0;i<arr.length;i++){
			if(arr[i]==str) return true;
		}
		return false;
	},

	//数组arr中是否包含字符串str（忽略大小写）
	containsIgnoreCase:function(arr,str){
		if(!arr||arr.length==0) return false;
		if(this.isBlank(str)) return false;

		str=str.toLowerCase();
		for(let i=0;i<arr.length;i++){
			if(arr[i].toLowerCase()==str) return true;
		}
		return false;
	},

	//字符串parentString中是否包含数组subStrings中的某个字符串
	exists:function(parentString, subStrings) {
		if(!subStrings || subStrings.length==0) return false;
		if(this.isBlank(parentString)) return false;

		for (let i = 0; i < subStrings.length; i++) {
			if(!this.isBlank(subStrings[i]) && parentString.indexOf(subStrings[i])>-1) {
				return true;
			}
		}

		return false;
	},

	//字符串parentString中是否包含数组subStrings中的某个字符串（忽略大小写）
	existsIgnoreCase:function(parentString, subStrings) {
		if(!subStrings || subStrings.length==0) return false;
		if(this.isBlank(parentString)) return false;

		parentString=parentString.toLowerCase();
		for (let i = 0; i < subStrings.length; i++) {
			if(!this.isBlank(subStrings[i]) && parentString.indexOf(subStrings[i].toLowerCase())>-1) {
				return true;
			}
		}

		return false;
	},

	//删除html标签
	delTag:function(src,tagName){
		let re=new RegExp('<'+tagName+'[^<]*>','igm');
		src=src.replace(re,'');

		re=new RegExp('</'+tagName+'>','igm');
		src=src.replace(re,'');

		return src;
	},

	//是否以什么开头
	startsWith:function(src,prefix,ignoreCase){
		if(this.isBlank(src) || this.isBlank(prefix)) return false;

		if(ignoreCase){
			src=src.toLowerCase();
			prefix=prefix.toLowerCase();
		}

		return src.startsWith(prefix);
	},

	//是否以什么结尾
	endsWith:function(src,suffix,ignoreCase){
		if(this.isBlank(src) || this.isBlank(suffix)) return false;

		if(ignoreCase){
			src=src.toLowerCase();
			suffix=suffix.toLowerCase();
		}

		return src.endsWith(suffix);
	},

	//是否以什么开头
	startsWithOneOf:function(src,prefixes,ignoreCase){
		for(let i=0;i<prefixes.length;i++){
			if(this.startsWith(src,prefixes[i],ignoreCase)) return true;
		}

		return false;
	},

	//是否以什么结尾
	endsWithOneOf:function(src,suffixes,ignoreCase){
		for(let i=0;i<suffixes.length;i++){
			if(this.endsWith(src,suffixes[i],ignoreCase)) return true;
		}

		return false;
	},

	//字符串转化成ascii码序列
	string2IntSequence:function(str){
		if(this.isBlank(str)) return str;

		let sequence=[];
		sequence.push('jis:');
		for(let i=0;i<str.length;i++){
			if(i>0) sequence.push(',');
			sequence.push(str.charCodeAt(i).toString(36));
		}
		return sequence.join('');
	},

	//字符ascii码序列转化成字符串
	intSequence2String:function(sequence){
		if(this.isBlank(sequence)) return sequence;
		if((typeof sequence)!='string') return sequence;
		if(!sequence.startsWith('jis:')) return sequence;
		while(sequence.startsWith('jis:')) sequence=sequence.substring(4);
		if(this.isBlank(sequence)) return sequence;

		let str=[];
		let chars=sequence.split(',');
		for(let i=0;i<chars.length;i++){
			str.push(String.fromCharCode(parseInt(chars[i],36)));
		}
		return str.join('');
	},

	//返回域名
	getHost:function(url){
		let d=url;
		d=d.substring(d.indexOf('//')+2);
		if(d.indexOf('/')>0) d=d.substring(0,d.indexOf('/'));
		return d;
	},

	//返回网址的根（不含/）
	getUrlBase:function(url){
		let temp1=url.substring(0,8);
		let temp2=url.substring(8);
		if(temp2.indexOf('/')>0) temp2=temp2.substring(0,temp2.indexOf('/'));
		return temp1+temp2;
	},

	getUri:function(url){
		if(!Str.isBlank(Global.localRoot) && url.indexOf(Global.localRoot)==0){
			url = url.substring(Global.localRoot.length);
			if(url.indexOf('?')>0) url=url.substring(0, url.indexOf('?'));
			while(url.endsWith('#')) url=url.substring(0, url.length - 1);
			return url;
		}

		if(url.indexOf("http://")>-1
			||url.indexOf("https://")>-1){
			url=url.substring(8);
		}
		if(url.indexOf('/')>-1) url=url.substring(url.indexOf('/'));
		if(url.indexOf('?')>0) url=url.substring(0, url.indexOf('?'));
		while(url.endsWith('#')) url=url.substring(0, url.length - 1);
		return Str.isBlank(url) ? '/' : url;
	},

	//返回协议(https/http)
	getScheme:function(url){
		if(this.isBlank(url)) url=currentUrl;
		return url.toLowerCase().indexOf('https')==0?'https':'http';
	},

	//解析url获得参数-值对
	getParams:function(url){
		let params=[];
		if(Str.isBlank(url)) return params;

		while(url.lastIndexOf('#')==url.length-1){
			url=url.substring(0,url.length-1);
		}
		let pos = url.indexOf('?');
		if(pos>0){
			let kvs = url.substring(pos+1).split('&');
			for(let i=0; i<kvs.length; i++){
				pos = kvs[i].indexOf('=');
				if(pos<=0) continue;

				params[kvs[i].substring(0,pos)]=JSONUtil.de(kvs[i].substring(pos+1));
			}
		}
		return params;
	},

	//获取url参数串
	getQueryString:function(url){
		if(Str.isBlank(url)) return '';
		while(url.lastIndexOf('#')==url.length-1){
			url=url.substring(0,url.length-1);
		}
		if(url.indexOf('?')>0) return url.substring(url.indexOf('?') + 1);
		return '';
	},

	//拼接url
	appendUrl:function(base, uri) {
		if(uri.startsWith("http")) return uri;
		if(base.endsWith("/")) {
			if(uri.startsWith("/")) return base + uri.substring(1);
			else return base + uri;
		} else {
			if(uri.startsWith("/")) return base + uri;
			else return base + "/" + uri;
		}
	},

	/**
	 * 模糊匹配
	 *
	 * @param src 源字符串，比如 http://www.sina.com.cn
	 * @param pattern 模式，日如 http://|-|.sina.com.cn
	 * @param wildcard 通配符，表示0个或多个任意字符，比如|-|
	 * @return
	 */
	matches:function(src, pattern, wildcard) {
		if(this.isBlank(src)) return -1;
		if(this.isBlank(pattern)) return -1;

		//匹配模式和源字符串相同，或匹配模式只包含通配符
		if(pattern==wildcard || Str.isBlank(Str.replaceAll(pattern, wildcard, ''))){
			return 0;
		}

		if(Str.isBlank(wildcard)
			|| pattern.indexOf(wildcard) < 0) {//通配符为空/配模式不含通配符，则寻找子串
			return src.indexOf(pattern);
		}

		let tokens = pattern.split(wildcard);
		if(tokens == null || tokens.length == 0) return src.indexOf(pattern);
		let index = -1;
		let startIndex = -1;
		let i = 0;
		for (i = 0; i < tokens.length; i++) {
			index = src.indexOf(tokens[i], index);
			if (index < 0) {
				break;
			} else {
				if (i == 0) startIndex = index;
				index += tokens[i].length;
			}
		}
		if(i < tokens.length) return -1;
		return startIndex;
	}
}
window.Str=Str;

//数学相关功能
let MathUtil={
	//将内容大小（字节数）转换成M显示（如果大于1M）
	//bytes  字节数
	size:function(bytes){
		bytes=bytes/1024;
		if(bytes<1024) return bytes+'K';
		else return Math.floor(bytes/1024)+'M';
	},

	//两点间距离
	distance:function(x1,y1,x2,y2){
		let d=(x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)
		return Math.floor(Math.sqrt(d));
	},

	//排列
	p:function(scope, selected){
		if(scope<1||selected<1||selected>scope) return 1;
		let p=1;
		for(let i=scope-selected+1;i<=scope;i++) p*=i;
		return p;
	},

	/**
	 * 打印出所有排列：先选择1个元素，再从剩下元素中选择selected-1个元素......以此递归
	 * @param objects 对象列表
	 * @param selected 排列元素数
	 * @param assembled 已经选出的排列
	 * @param assembling 正在组装的排列
	 * @return
	 */
	pPrint:function(objects, selected, assembled, assembling){
		for(let j=0; j<objects.length; j++) {
			let assemblingCopy=Utils.arrayCopy(assembling);
			assemblingCopy.push(objects[j]);

			if(assemblingCopy.length==selected) {
				assembled.push(assemblingCopy);
			}else {
				let objectsCopy=Utils.arrayCopy(objects, objects[j]);
				this.pPrint(objectsCopy, selected, assembled, assemblingCopy);
			}
		}
	},

	//组合
	c:function(scope, selected){
		if(scope<1||selected<1||selected>scope) return 1;
		let p=1;
		for(let i=1;i<=selected;i++) p*=i;
		return this.p(scope,selected)/p;
	},

	/**
	 * 打印出所有组合：选择1个元素，再从剩下元素中选择selected-1个元素......以此递归，并去掉重复组合
	 * @param objects 对象列表
	 * @param selected 组合元素数
	 * @param assembled 已经选出的组合
	 * @param assembling 正在组装的组合
	 * @return
	 */
	cPrint:function(objects, selected, assembled, assembling){
		this.cPermutationFeatureCache=[];
		for(let j=0; j<objects.length; j++) {
			let assemblingCopy=assembling.slice();
			assemblingCopy.push(objects[j]);
			assemblingCopy.sort();

			if(assemblingCopy.length==selected) {
				if(!this.cPermutationExists(assembled, assemblingCopy)) assembled.push(assemblingCopy);
			}else {
				let objectsCopy=Utils.arrayCopy(objects, objects[j]);
				this.cPrint(objectsCopy, selected, assembled, assemblingCopy);
			}
		}
	},

	/**
	 * 对组合内元素进行排序，然后拼串（为去掉重复组合）
	 * @param array
	 * @return
	 */
	cPermutationFeatureCache:[],
	cPermutationFeature:function(array, index) {
		if((typeof index)=='number' && this.cPermutationFeatureCache[index]) return this.cPermutationFeatureCache[index];
		else{
			let temp = array.join(',');
			if((typeof index)=='number') this.cPermutationFeatureCache[index]=temp;
			return temp;
		}
	},

	/**
	 * 组合（assembling）是否已经存在列表中（assembled）
	 * @param assembled
	 * @param assembling
	 * @return
	 */
	cPermutationExists:function(assembled, assembling) {
		let assemblingFeature=this.cPermutationFeature(assembling);
		for(let i=0; i<assembled.length; i++) {
			if(assemblingFeature == this.cPermutationFeature(assembled[i], i)) return true;
		}
		return false;
	},

	toFixed:function(num, precision){
		num=Str.replaceAll(num+'',',','')*1;
		return num.toFixed(precision)*1;
	},

	toFixedTrim:function(num, precision){
		if(precision==0) return this.toFixed(num,precision);
		num=Str.replaceAll(num+'',',','')*1;
		num=num.toFixed(precision)+'';
		let dot=num.indexOf('.');
		let zero=num.lastIndexOf('0');
		while(zero==num.length-1&&zero>dot){
			num=num.substring(0,num.length-1);
			zero=num.lastIndexOf('0');
		}
		if(dot==num.length-1) num=num.substring(0,num.length-1);

		return num*1;
	}
}
window.MathUtil=MathUtil;

//冒泡排序
//冒泡排序
let Sorter={
	SORT_DESC:'DESC',
	SORT_ASC:'ASC',
	COMPARE_SMALLER:-1,
	COMPARE_BIGGER:1,
	COMPARE_EQUAL:0,

	bubble:function(original,sortType,comparer,additionalParams){//冒泡排序
		let cnt = original.length;
		for (let j = 0; j < cnt - 1; ++j) {
			for (let i = 1; i < cnt - j; ++i) {
				let pre = original[i - 1];
				let after = original[i];

				let comp=0;
				if(comparer){
					comp=comparer.compare(pre, after, additionalParams);
				}else{
					if(pre<after) comp=this.COMPARE_SMALLER;
					else if(pre>after) comp=this.COMPARE_BIGGER;
					else comp=this.COMPARE_EQUAL;
				}

				if(sortType==this.SORT_DESC){
					if(comp==this.COMPARE_SMALLER) {
						original[i - 1]=after;
						original[i]=pre;
					}
				}else{
					if(comp==this.COMPARE_BIGGER) {
						original[i - 1]=after;
						original[i]=pre;
					}
				}
			}
		}
		return original;
	}
}
window.Sorter=Sorter;

//JSON实用方法
let JSONUtil={
	parse:function(s){
		try{
			s=Str.intSequence2String(s);
			s=Lang.convert(s);
			let resp=JSON.parse(s);
			try{
				if(resp.code) resp.code=Str.intSequence2String(resp.code);
			}catch(e){}
			try{
				if(resp.message) resp.message=Str.intSequence2String(resp.message);
			}catch(e){}
			return resp;
		}catch(e){
			//return null;
			return JSON.parse('{}');
		}
	},

	de:function(v){
		if(!v) return v;
		if((typeof v)!='string') return v;
		try{
			v=Str.intSequence2String(v);
		}catch(e){}
		try{
			v=decodeURIComponent(v);
		}catch(e){}
		v=Lang.convert(v);

		return v;
	},

	convert:function(s){
		if(Str.isBlank(s)) return s;
		s=Str.replaceAll(s,'"','\\"');
		return s;
	},

	isJson:function(s){
		if(Str.isBlank(s)) return false;
		try{
			JSON.parse(s);
			return true;
		}catch(e){
			return false;
		}
	},

	get:function (data, key){
		try{
			return data[key];
		}catch (e){
			return null;
		}
	}
}
window.JSONUtil=JSONUtil;

/**
 * 键值对
 * @param k
 * @param v
 * @constructor
 */
function KV(k, v){
	this.k=k;
	this.v=v;
}

//实用方法
let Utils={
	//复制数组
	arrayCopy:function(arr, exclude){
		let c=[];
		for(let i=0; i<arr.length; i++){
			if(!exclude || exclude!=arr[i]) c.push(arr[i]);
		}
		return c;
	},

	rgbToHex:function(rgb){
		rgb=rgb.toUpperCase();
		if(!rgb.startsWith('RGB(')) return rgb;

		if(rgb.startsWith('RGB(')) rgb=rgb.substring(4);
		if(rgb.endsWith(')')) rgb=rgb.substring(0, rgb.length-1);
		rgb=Str.replaceAll(rgb, ' ', '').split(',');
		if(rgb.length!=3) return rgb;

		let hex='';
		let temp=(rgb[0]*1).toString(16);
		if(temp.length==1) temp='0'+temp;
		hex+=temp;

		temp=(rgb[1]*1).toString(16);
		if(temp.length==1) temp='0'+temp;
		hex+=temp;

		temp=(rgb[2]*1).toString(16);
		if(temp.length==1) temp='0'+temp;
		hex+=temp;

		return '#'+hex.toUpperCase();
	},

	//触发事件的对象
	getEventTarget:function(event){
		if(!event) return null;
		if(event.currentTarget){
			return event.currentTarget;
		}else if(event.target){
			return event.target;
		}else if(event.srcElement){
			return event.srcElement;
		}
		return null;
	},

	//获取html元素的属性值
	attInheritParents:0,
	att:function(obj, attName, inheritParents){
		if((typeof inheritParents)!='number') inheritParents=this.attInheritParents;
		if(!obj||!obj.attributes) return null;
		let n=obj.attributes[attName];
		if(!n && inheritParents>0) return this.att(obj.parentNode, attName, inheritParents-1);
		return n?n.value:null;
	},

	//设置html元素的属性值
	setAtt:function(obj,attName,attValue){
		if(!obj.attributes) return null;
		let n=obj.attributes[attName];
		if(n) n.value=attValue;
		else obj.setAttribute(attName,attValue);
	},

	//删除html元素的属性值
	delAtt:function(obj,attName){
		if(!obj) return;
		if(!obj.attributes) return;
		let n=obj.attributes[attName];
		if(n) obj.removeAttribute(attName,false);
	},

	//获取html元素的文本内容，不含隐藏内容
	innerText:function(obj){
		return obj.innerText;
	},

	//获取html元素的文本内容，含隐藏内容
	textContent:function(obj){
		return obj.textContent;
	},

	//获取html元素的文本内容（不包含子节点）
	textExcludeChildNodes:function(obj){
		let texts = [];
		obj.childNodes.forEach(node => {
			console.log('node.nodeName='+node.nodeName);
			if(node.nodeName === '#text') { // 获取所有text节点的内容
				texts.push(node.data);
			}
		})
		return texts.join('');
	},

	//获取html元素的子对象
	childNodes:function(obj){
		let arr=[];
		let cs=obj.childNodes;
		if(!cs) return arr;

		for(let i=0;i<cs.length;i++){
			if(cs[i].tagName) arr.push(cs[i]);
		}
		return arr;
	},

	//查找对象的前一个同级对象
	findBefore:function(obj){
		let siblings=Utils.childNodes(obj.parentNode);
		if(siblings<=1) return null;

		let me=0;
		for(let i=0;i<siblings.length;i++){
			if(siblings[i]==obj){
				me=i;
				break;
			}
		}

		if(me>0) return siblings[me-1];
		else return null;
	},

	//查找对象的后一个同级对象
	findAfter:function(obj){
		let siblings=Utils.childNodes(obj.parentNode);
		if(siblings<=1) return null;

		let me=0;
		for(let i=0;i<siblings.length;i++){
			if(siblings[i]==obj){
				me=i;
				break;
			}
		}

		if(me<siblings.length-1) return siblings[me+1];
		else return null;
	},

	//在targetElement之后插入newElement
	insertAfter:function(newElement, targetElement){
		let parent = targetElement.parentNode;
		if(parent.lastChild == targetElement || !targetElement.nextSibling) {
			parent.appendChild(newElement);
		}else{
			parent.insertBefore(newElement, targetElement.nextSibling);
		}
	},

	//在targetElement之前插入newElement
	insertBefore:function(newElement, targetElement){
		targetElement.parentNode.insertBefore(newElement, targetElement);
	},

	//将对象前移
	moveForward:function(obj){
		let before=Utils.findBefore(obj);
		if(!before) return false;

		let p=obj.parentNode;
		p.removeChild(obj);
		p.insertBefore(obj,before);
		return true;
	},

	//将对象后移
	moveBackward:function(obj){
		let after=Utils.findAfter(obj);
		if(!after) return false;

		let p=obj.parentNode;
		p.removeChild(after);
		p.insertBefore(after,obj);
		return true;
	},

	//元素是否可见
	//recursion 是否递归累积到顶层元素，默认为true
	visible:function(obj, recursion){
		if(!obj) return false;
		if((typeof recursion) != 'boolean') recursion=true;
		if(!Utils.att(obj,'_src')
			&&obj.style
			&&(obj.style.display=='none'||obj.style.visibility=='hidden')) return false;

		if(!recursion) return true;

		let temp=obj.parentNode;
		while(temp&&temp.style&&temp.style.display!='none'&&temp.style.visibility!='hidden'){
			temp=temp.parentNode;
		}
		if(temp&&temp.style&&(temp.style.display=='none'||temp.style.visibility=='hidden')) return false;

		return true;
	},

	//获取form名为name的元素
	formElement:function(name){
		let es=this.form.elements;
		for(let i=0;i<es.length;i++){
			if(!es[i].name||es[i].name==='') continue;

			if(name==es[i].name) return es[i];
		}
		return null;
	},

	//获取form的键值对
	formKeyValues:function(frm,excludes){
		let es=frm.elements;
		let params=[];
		for(let i=0;i<es.length;i++){
			if(this.att(es[i],'type')=='button') continue;
			if(!es[i].name||es[i].name==='') continue;

			if(excludes&&Str.contains(excludes,es[i].name)) continue;

			params[es[i].name]=es[i].value;
		}
		return params;
	},

	//将form的键值对转换成json
	form2Json:function(form,excludes){
		let keyValues=this.formKeyValues(form, excludes);
		let s=[];
		s.push('{');
		let index=0;
		for(let i in keyValues){
			if(index > 0) s.push(',');
			s.push('"'+i+'":"'+JSONUtil.convert(keyValues[i])+'"');
			index++;
		}
		s.push('}');

		return s.join('');
	},

	//选择文本
	selectText:function(input, startIndex, stopIndex){
		if(input.createTextRange) {//ie
			let range = input.createTextRange();
			range.collapse(true);
			range.moveStart('character', startIndex);//起始光标
			range.moveEnd('character', stopIndex - startIndex);//结束光标
			range.select();//不兼容苹果
		} else {//firefox/chrome
			input.setSelectionRange(startIndex, stopIndex);
			input.focus();
		}
	},

	//得到元素指定类型的样式
	getStyle:function(obj,styleName){
		if(!obj) return '';
		if(obj.currentStyle){
			return obj.currentStyle[styleName];//IE下获取非行间样式
		}else{
			return getComputedStyle(obj, false)[styleName];//FF、Chorme下获取非行间样式
		}
	},

	getParentNodeExcludeTag:function(obj,exclude){
		let temp=obj.parentNode;
		while(temp&&temp.tagName.toLowerCase()==exclude){
			temp=temp.parentNode;
		}
		return temp;
	},

	getParentNodeOfTag:function(obj,of){
		let temp=obj.parentNode;
		while(temp&&temp.tagName.toLowerCase()!=of){
			temp=temp.parentNode;
		}
		return temp;
	},

	/**
	 * 调整元素宽度（父节点宽度-其它兄弟节点宽度-额外调整量
	 * @param obj
	 * @param adjustment 额外调整量
	 * @param show 是否自动将被调整对象设为可见
	 * @returns {number|*}
	 */
	adjustObjWidth:function(obj, adjustment, show){
		if(!obj) return;
		let width=W.elementWidth(obj.parentNode);
		let siblings=Utils.childNodes(obj.parentNode);
		for(let i=0; i<siblings.length; i++){
			if(siblings[i]==obj) continue;
			width-=W.elementWidth(siblings[i]);
		}
		width -= ((typeof adjustment)=='number'?adjustment:0);
		obj.style.width=width+'px';
		if(show) obj.style.display='';
	},
	adjustObjWidthBatch:function(objs, adjustment, show){
		for(let i=0; i<objs.length; i++) this.adjustObjWidth(objs[i], adjustment, show);
	},

	/**
	 * 将obj移动到高度为boxHeight区域的垂直居中位置
	 * @param obj
	 * @param boxHeight
	 */
	moveObjCenteredVertically:function (obj, boxHeight){
		if((typeof obj) == 'string') obj=_$(obj);
		let objH=W.elementHeight(obj);
		if(objH>=boxHeight) return;
		obj.style.marginTop=Math.floor((boxHeight - objH)/2)+'px';
	},

	//资金数字转大写
	moneyToDX:function(money) {
		let cnNums = new Array('零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖');//汉字的数字
		let cnIntRadice = new Array('', '拾', '佰', '仟');//基本单位
		let cnIntUnits = new Array('', '万', '亿', '兆');//对应整数部分扩展单位
		let cnDecUnits = new Array('角', '分', '毫', '厘');//对应小数部分单位
		let cnInteger = '';//整数金额时后面跟的字符
		let cnIntLast = '';//整型完以后的单位
		let maxNum = 999999999999999.9999;//最大处理的数字
		let integerNum;//金额整数部分
		let decimalNum;//金额小数部分
		let chineseStr = '';//输出的中文金额字符串
		let parts;//分离金额后用的数组，预定义

		if(money == '') return '';
		money = parseFloat(money);
		if(money >= maxNum) return '';//超出最大处理数字
		if(money == 0) {
			chineseStr = cnNums[0] + cnIntLast + cnInteger;
			return chineseStr;
		}
		//转换为字符串
		money = money.toString();
		if(money.indexOf('.') == -1) {
			integerNum = money;
			decimalNum = '';
		} else {
			parts = money.split('.');
			integerNum = parts[0];
			decimalNum = parts[1].substr(0, 4);
		}
		//获取整型部分转换
		if(parseInt(integerNum, 10) > 0) {
			let zeroCount = 0;
			let IntLen = integerNum.length;
			for (let i = 0; i < IntLen; i++) {
				let n = integerNum.substr(i, 1);
				let p = IntLen - i - 1;
				let q = p / 4;
				let m = p % 4;
				if(n == '0') {
					zeroCount++;
				} else {
					if(zeroCount > 0) chineseStr += cnNums[0];
					zeroCount = 0;//归零
					chineseStr += cnNums[parseInt(n)] + cnIntRadice[m];
				}
				if(m == 0 && zeroCount < 4) chineseStr += cnIntUnits[q];
			}
			chineseStr += cnIntLast;
		}
		//小数部分
		if(decimalNum != '') {
			let decLen = decimalNum.length;
			for (let i = 0; i < decLen; i++) {
				let n = decimalNum.substr(i, 1);
				if(n != '0') chineseStr += cnNums[Number(n)] + cnDecUnits[i];
			}
		}
		if(chineseStr == '') chineseStr += cnNums[0] + cnIntLast + cnInteger;
		else if(decimalNum == '') chineseStr += cnInteger;
		return chineseStr;
	},

	setAvgWidthOfParent(objs, minWidth, adjustParentSize, tabMaxWidth){
		let whole = W.elementWidth(objs[0].parentNode);
		let w = (whole/objs.length).toFixed(0);
		let wLast = whole - w*(objs.length - 1);
		if(minWidth){
			if(w<minWidth) w=minWidth;
			if(wLast<minWidth) wLast=minWidth;
		}

		if(tabMaxWidth){
			if(w>tabMaxWidth) w=tabMaxWidth;
			if(wLast>tabMaxWidth) wLast=tabMaxWidth;
		}

		if(adjustParentSize){
			objs[0].parentNode.style.width= (w*(objs.length - 1) + wLast) + 'px';
		}

		for(let i=0; i<objs.length - 1; i++) objs[i].style.width=w+'px';
		objs[objs.length - 1].style.width=wLast+'px';
	},

	base64ToBlob:function (dataurl) {
		let arr = dataurl.split(',');
		let mime = arr[0].match(/:(.*?);/)[1];
		let bstr = atob(arr[1]);
		let n = bstr.length;
		let u8arr = new Uint8Array(n);
		while (n--) {
			u8arr[n] = bstr.charCodeAt(n);
		}
		return new Blob([u8arr], { type: mime});
	}
}
window.Utils=Utils;

/**
 * Tabs组件实例
 * @type {{}}
 */
let TabsInstances = {
	instances: [],

	saveInstance: function(box, instance){
		this.instances[box.id] = instance;
	},

	hideSlidableTips: function(id){
		let inst = this.instances[id];
		if(inst) inst.hideSlidableTips();
	},

	slideLeft10px: function(id){
		let inst = this.instances[id];
		if(inst) inst.slideLeft10px();
	},

	slideRight10px: function(id){
		let inst = this.instances[id];
		if(inst) inst.slideRight10px();
	}
}
window.TabsInstances=TabsInstances;

/**
 * tab组件
 * @type {{init: Tabs.init, showSlidableTips: Tabs.showSlidableTips}}
 */
function Tabs(box, boxWidth, tabsContainer, tabs, setTab2AgvWidth, tabMinWidth, tabMaxWidth) {
	this.box = box;
	this.boxWidth = boxWidth;
	this.tabsContainer = tabsContainer;
	this.tabs = tabs;
	this.setTab2AgvWidth = setTab2AgvWidth;
	this.tabMinWidth = tabMinWidth;
	this.tabMaxWidth = tabMaxWidth;
	this.initScrollLeft = this.box.scrollLeft;
	TabsInstances.saveInstance(this.box, this);
}

Tabs.prototype.init=function(){
	this.box.style.width=this.boxWidth+'px';
	if(this.setTab2AgvWidth) Utils.setAvgWidthOfParent(this.tabs, this.tabMinWidth, true, this.tabMaxWidth);
}

Tabs.prototype.showSlidableTips=function(){
	//内容宽度比盒子宽度没有多出10px以上，不需要显示
	if(W.elementWidth(this.tabsContainer) - W.elementWidth(this.box) <= 10) return;

	//提示
	let tips='<div id="'+this.box.id+'_tips" class="tabSlidableTips" style="visibility: hidden;"><span class="iconfont icon-bhjleftslide"></span><span>I{.可左右滑动}</span><span class="iconfont icon-bhjrigthslide"></span></div>';
	__$('Jcontent').insertAdjacentHTML('afterbegin', Lang.convert(tips));

	_$(this.box.id+'_tips').style.width = W.elementWidth(this.box)+'px';
	_$(this.box.id+'_tips').style.left = W.elementLeft(this.box)+'px';
	_$(this.box.id+'_tips').style.top = W.elementTop(this.box)+'px';
	_$(this.box.id+'_tips').style.visibility='visible';

	setTimeout('TabsInstances.hideSlidableTips("'+this.box.id+'")', 3000);
	setTimeout('TabsInstances.slideLeft10px("'+this.box.id+'")', 500);
	setTimeout('TabsInstances.slideRight10px("'+this.box.id+'")', 1000);
	setTimeout('TabsInstances.slideLeft10px("'+this.box.id+'")', 1500);
	setTimeout('TabsInstances.slideRight10px("'+this.box.id+'")', 2000);
}

Tabs.prototype.hideSlidableTips=function(){
	_$(this.box.id+'_tips').parentNode.removeChild(_$(this.box.id+'_tips'));
	this.slideTo(this.initScrollLeft);
}

Tabs.prototype.slideLeft10px=function(){
	let scrollLeft = this.box.scrollLeft;
	scrollLeft+=20;
	this.box.scrollLeft=Math.max(0, scrollLeft);
}

Tabs.prototype.slideRight10px=function(){
	let scrollLeft = this.box.scrollLeft;
	scrollLeft-=20;
	let maxScrollLeft=W.elementWidth(this.tabsContainer) - W.elementWidth(this.box);
	this.box.scrollLeft=Math.min(scrollLeft, maxScrollLeft);
}

Tabs.prototype.slideTo=function(left){
	this.box.scrollLeft=left;
}

Tabs.prototype.scrollToView=function(tab){
	let visibleLeft = this.box.scrollLeft;
	let visibleRight = this.box.scrollLeft + W.elementWidth(this.box);

	let tabWidth=W.elementWidth(tab);
	let tabLeft=W.elementLeft(tab);
	let tabRight=W.elementLeft(tab) + tabWidth;

	if(tabRight < visibleLeft + tabWidth){
		//tab靠左，且右侧可见宽度小于tab宽度，尝试全部显示出来
		this.box.scrollLeft=Math.max(tabLeft - tabWidth, 0);
	}else if(tabRight > visibleRight){
		//tab靠右，且左侧可见宽度小于tab宽度，尝试全部显示出来
		let maxScrollLeft=W.elementWidth(this.tabsContainer) - W.elementWidth(this.box);
		this.box.scrollLeft=Math.min(maxScrollLeft, tabRight - W.elementWidth(this.box));
	}
}

Tabs.prototype.changeTab=function(tab){
	for(let i=0; i<this.tabs.length; i++) this.tabs[i].className='tab';
	tab.className='tabCurrent';
	this.scrollToView(tab);
}

/**
 * input输入事件绑定（避免中文输入状态下，未完成输入就触发keyup事件）
 * @param input 必须指定id
 * @param keyupCallback
 * @param keyupCallbackTarget
 * @constructor
 */
function InputEvent(input, keyupCallback, keyupCallbackTarget){
	this.id=input.id;
	this.input=input;
	this.keyupCallback=keyupCallback;
	this.keyupCallbackTarget=keyupCallbackTarget;
	this.typeStart=false;
	this.typeEnd=false;

	this.input.addEventListener('compositionstart', function(event){
		let object=Utils.getEventTarget(event);
		let instance=top.Page.InputEvents[object.id];
		instance.typeStart=true;
		instance.typeEnd=false;
	});

	this.input.addEventListener('compositionend', function(event){
		let object=Utils.getEventTarget(event);
		let instance=top.Page.InputEvents[object.id];
		instance.typeEnd=true;
	});

	this.input.addEventListener('keyup', function(event){
		let object=Utils.getEventTarget(event);
		let instance=top.Page.InputEvents[object.id];

		//中文输入未完成
		if(instance.typeStart && !instance.typeEnd) return;

		instance.keyupCallback.call(instance.keyupCallbackTarget?instance.keyupCallbackTarget:window, event);
	});

	top.Page.InputEvents[this.id]=this;
}

//窗口操作
let W={
	//获取DPI
	getDPI:function() {
		let arrDPI = [];
		if(window.screen.deviceXDPI != undefined){
			arrDPI[0] = window.screen.deviceXDPI;
			arrDPI[1] = window.screen.deviceYDPI;
		}else{
			let tmpNode = document.createElement("DIV");
			tmpNode.style.cssText = "width:1in;height:1in;position:absolute;left:0px;top:0px;";
			document.body.appendChild(tmpNode);
			arrDPI[0] = parseInt(tmpNode.offsetWidth);
			arrDPI[1] = parseInt(tmpNode.offsetHeight);
			tmpNode.parentNode.removeChild(tmpNode);
		}
		return arrDPI;
	},

	//毫米转换成px，1: x  2: y
	mm2px:function(mm, direction){
		let DPI=this.getDPI();
		if(direction==1) return mm*DPI[0]/25.4;
		else return mm*DPI[1]/25.4;
	},

	getPixelRatio:function(context) {
		let backingStore = context.backingStorePixelRatio
			|| context.webkitBackingStorePixelRatio
			|| context.mozBackingStorePixelRatio
			|| context.msBackingStorePixelRatio
			|| context.oBackingStorePixelRatio
			|| context.backingStorePixelRatio
			|| 1;
		return (window.devicePixelRatio || 1) / backingStore;
	},

	//滚动条距离窗口顶部高度
	t:function(){
		return window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop;
	},

	//滚动条距离窗口顶部高度（累计各级窗口）
	tTotal:function(){
		let _t=W.t();
		try{
			let win=window;
			let winParent=window.parent;
			while(winParent&&winParent!=win){
				_t=winParent.W.t();

				win=winParent;
				winParent=win.parent;
			}
		}catch(e){}
		return _t;
	},

	//滚动条距离窗口左边高度
	l:function(){
		return window.pageXOffset || document.documentElement.scrollLeft || document.body.scrollLeft;
	},

	//窗口可视宽度
	vw:function(element){
		if(element){
			let w=this.elementWidth(element);
			if(UserAgent.isPC()){
				let overflowY=Utils.getStyle(element, 'overflowY');
				if(Str.isBlank(overflowY) || overflowY=='scroll' || overflowY=='auto'){
					if(this.elementScrollHeight(element) > this.elementHeight(element)){
						w-=4;
					}
				}
			}
			return w;
		}

		let w = this._vw();
		if(UserAgent.isPC()){
			element = document.getElementsByTagName("body")[0];
			let overflowY=Utils.getStyle(element, 'overflowY');
			if(Str.isBlank(overflowY) || overflowY=='scroll' || overflowY=='auto'){
				if(this.h() > this.vh()) w-=4;
			}
		}
		return w;
	},
	_vw:function (){
		Global.renew();
		if(UserAgent.isMobile()){
			let r1=document.getElementsByTagName("body")[0].scrollWidth;
			r1=Math.min(r1, Global.screenWidth);

			let r2;
			if(window.innerWidth){
				let a=window.innerWidth;
				let b=document.getElementsByTagName("html")[0].offsetWidth;
				r2=Math.min(a, b);
			}else{
				r2=document.getElementsByTagName("html")[0].offsetWidth;
			}
			r2=Math.min(r2, Global.screenWidth);

			return Math.min(r1, r2);
		}else{
			let r;
			if(window.innerWidth){
				let a=window.innerWidth;
				let b=document.getElementsByTagName("html")[0].offsetWidth;
				r=Math.min(a, b);
			}else{
				r=document.getElementsByTagName("html")[0].offsetWidth;
			}
			return Math.min(r, Global.screenWidth);
		}
	},

	//窗口可视宽度（不超过指定值）
	vwNoMoreThan:function(max){
		let w=this.vw();
		if(max && max<w) return max;
		return w;
	},

	//窗口可视高度
	vh:function(){
		if(window.innerHeight){
			return window.innerHeight;
		}else{
			return document.getElementsByTagName("html")[0].offsetHeight;
		}
	},

	//窗口宽度
	w:function(){
		let r;
		try{
			r = document.documentElement.scrollWidth;
		}catch(e){
			r = document.body.scrollWidth;
		}

		if(this.h()>this.vh()){//如果有滚动条，PC端减去滚动条宽度（4）
			return Math.max(r, this.vw())-(UserAgent.isMobile()?0:4);
		}else{//如果没滚动条
			return Math.max(r, this.vw());
		}
	},

	//窗口高度
	h:function(){
		let r;
		try{
			r = document.documentElement.scrollHeight;
		}catch(e){
			r = document.body.scrollHeight;
		}
		return Math.max(r, this.vh());
	},

	//iframe宽度
	iframeWidth:function(id){
		try{
			let obj=_$(id);

			if(obj.contentWindow.document.documentElement.scrollWidth
				&& obj.contentWindow.document.body.scrollWidth){
				return Math.min(obj.contentWindow.document.documentElement.scrollWidth,
					obj.contentWindow.document.body.scrollWidth);
			}else if(obj.contentWindow.document.documentElement.scrollWidth){
				return obj.contentWindow.document.documentElement.scrollWidth;
			}else{
				return obj.contentWindow.document.body.scrollWidth;
			}
		}catch(e){
			return W.vw();
		}
	},

	//iframe高度
	iframeHeight:function(id){
		try{
			let obj=_$(id);

			if(obj.contentWindow.document.documentElement.scrollHeight
				&& obj.contentWindow.document.body.scrollHeight){
				return Math.min(obj.contentWindow.document.documentElement.scrollHeight,
					obj.contentWindow.document.body.scrollHeight);
			}else if(obj.contentWindow.document.documentElement.scrollHeight){
				return obj.contentWindow.document.documentElement.scrollHeight;
			}else{
				return obj.contentWindow.document.body.scrollHeight;
			}
		}catch(e){
			return IFrame.minHeight;
		}
	},

	/**
	 * 元素顶部离上级元素的距离
	 * @param obj
	 * @param recursion 是否递归累积到顶层元素，默认为true
	 * @param relativeTop 相对哪个顶层对象
	 * @returns {number}
	 */
	elementTop:function(obj, recursion, relativeTop){
		if((typeof obj)=='string') obj=_$(obj);
		if((typeof recursion) != 'boolean') recursion=true;
		if(!recursion && obj.parentNode && obj.offsetParent && obj.parentNode != obj.offsetParent){
			return 0;
		}

		let t = obj.offsetTop;
		if(!recursion) return t;

		let p=obj;
		while(p = p.offsetParent){
			if(p==relativeTop) break;
			t += p.offsetTop;
		}
		return t;
	},

	/**
	 * 元素左侧离上级元素的距离
	 * @param obj
	 * @param recursion 是否递归累积到顶层元素，默认为true
	 * @param relativeTop 相对哪个顶层对象
	 * @returns {number}
	 */
	elementLeft:function(obj, recursion, relativeTop){
		if((typeof obj)=='string') obj=_$(obj);
		if((typeof recursion) != 'boolean') recursion=true;
		if(!recursion && obj.parentNode && obj.offsetParent && obj.parentNode != obj.offsetParent){
			return 0;
		}

		let l = obj.offsetLeft;
		if(!recursion) return l;

		let p=obj;
		while(p = p.offsetParent){
			if(p==relativeTop) break;
			l += p.offsetLeft;
		}
		return l;
	},

	/**
	 * 元素顶部离上级元素的距离
	 * @param obj
	 * @param recursion 是否递归累积到顶层元素，默认为true
	 * @param relativeTop 相对哪个顶层对象
	 * @returns {number}
	 */
	scrollTop:function(obj, recursion, relativeTop){
		if((typeof obj)=='string') obj=_$(obj);
		let t = (obj.scrollTop ? obj.scrollTop : 0);
		if((typeof recursion) == 'boolean' && !recursion) return t;

		let p=obj.parentNode;
		while(p){
			if(p==relativeTop) break;
			t += (p.scrollTop ? p.scrollTop : 0);
			p = p.parentNode;
		}
		return t;
	},

	/**
	 * 元素左侧离上级元素的距离
	 * @param obj
	 * @param recursion 是否递归累积到顶层元素，默认为true
	 * @param relativeTop 相对哪个顶层对象
	 * @returns {number}
	 */
	scrollLeft:function(obj, recursion, relativeTop){
		if((typeof obj)=='string') obj=_$(obj);
		let l = (obj.scrollLeft ? obj.scrollLeft : 0);
		if((typeof recursion) == 'boolean' && !recursion) return l;

		let p=obj.parentNode;
		while(p){
			if(p==relativeTop) break;
			l += (p.scrollLeft ? p.scrollLeft : 0);
			p = p.parentNode;
		}
		return l;
	},

	//获取绝对定位元素的顶部偏移量
	positionTop:function(obj){
		if((typeof obj)=='string') obj=_$(obj);
		let t=Utils.getStyle(obj, 'top');
		if(t && t.endsWith('px')) t=t.substring(0, t.length-2);
		t=t*1;
		return (typeof t)=='number'?t:0;
	},

	//获取绝对定位元素的左侧偏移量
	positionLeft:function(obj){
		if((typeof obj)=='string') obj=_$(obj);
		let t=Utils.getStyle(obj, 'left');
		if(t && t.endsWith('px')) t=t.substring(0, t.length-2);
		t=t*1;
		return (typeof t)=='number'?t:0;
	},

	//获取元素的可视高度
	elementHeight:function(obj){
		if(!obj) return 0;
		if((typeof obj)=='string') obj=_$(obj);
		if(obj.offsetHeight) return obj.offsetHeight;
		else if(obj.scrollHeight) return obj.scrollHeight;
		else return 0;
	},

	//获取元素的可视宽度
	elementWidth:function(obj){
		if(!obj) return 0;
		if((typeof obj)=='string') obj=_$(obj);
		if(obj.offsetWidth) return obj.offsetWidth;
		else if(obj.scrollWidth) return obj.scrollWidth;
		else return 0;
	},

	//获取元素的滚动高度
	elementScrollHeight:function(obj){
		if((typeof obj)=='string') obj=_$(obj);
		if(obj.scrollHeight) return obj.scrollHeight;
		else if(obj.offsetHeight) return obj.offsetHeight;
	},

	//获取元素的滚动宽度
	elementScrollWidth:function(obj){
		if((typeof obj)=='string') obj=_$(obj);
		if(obj.scrollWidth) return obj.scrollWidth;
		else if(obj.offsetWidth) return obj.offsetWidth;
	},

	//绝对定位元素的最大z-index值
	maxZIndex:0,

	//当前绝对定位元素的最大z-index值加1
	getMaxZIndex:function(){
		this.maxZIndex++;
		return this.maxZIndex;
	},

	//是否已经滚动到底部
	scrollToBottom:function(obj){
		return W.elementHeight(obj) + obj.scrollTop >= W.elementScrollHeight(obj);
	}
}
window.W=W;

//iframe相关操作
let IFrame={
	//最小高度
	minHeight:100,

	/**
	 * 调整iframe高度
	 * @param id iframe id
	 * @param addition
	 */
	adjustSize:function(id, addition, _minHeight, _maxHeight){
		if(!_minHeight || isNaN(_minHeight)) _minHeight=this.minHeight;
		try{
			let obj=_$(id);
			let h=W.iframeHeight(id);
			h=Math.max(h, _minHeight);
			if(_maxHeight) h=Math.min(h, _maxHeight);

			if(addition){
				obj.height = h + addition;
			}else{
				obj.height = h;
			}
		}catch(e){}
	},

	/**
	 * 调整iframe高度，同时设置父元素高度为同一值
	 * @param id iframe id
	 * @param parentId
	 * @param addition
	 */
	adjustSizeWithParent:function(id, parentId, addition, _minHeight, _maxHeight){
		if(!_minHeight || isNaN(_minHeight)) _minHeight=this.minHeight;
		try{
			let obj=_$(id);
			let h=W.iframeHeight(id);
			h=Math.max(h, _minHeight);
			if(_maxHeight) h=Math.min(h, _maxHeight);

			if(addition){
				obj.height = h + addition;
				_$(parentId).style.height=(h + addition)+'px';
			}else{
				obj.height = h;
				_$(parentId).style.height=h+'px';
			}
		}catch(e){}
	},

	/**
	 * 往iframe窗口中写入内容
	 * @param frm iframe id或对象
	 * @param content
	 */
	setContent:function(frm, content){
		let obj=(typeof frm)=='string'?_$(frm):frm;
		if(!obj) return;
		if(obj.contentDocument){
			obj.contentDocument.write(content);
			obj.contentDocument.close();
		}else{
			obj.document.write(content);
			obj.document.close();
		}
	},

	/**
	 * 读取iframe内容
	 * @param frm iframe id或对象
	 * @returns {string}
	 */
	getContent:function(frm){
		try{
			let obj=(typeof frm)=='string'?_$(frm):frm;
			if(!obj) return null;
			if(obj.contentDocument) return obj.contentDocument.body.innerHTML;
			else return obj.document.body.innerHTML;
		}catch(e){}
		return '';
	},

	/**
	 * 给iframe窗口对象设置内容
	 * @param frm iframe id或对象
	 * @param elementId iframe窗口中的对象id
	 * @param content 往iframe窗口中id为elementId的对象中写入内容
	 */
	setElementContent:function(frm,elementId,content){
		let obj=(typeof frm)=='string'?_$(frm):frm;
		if(!obj) return null;
		if(obj.contentDocument){
			obj.contentDocument.getElementById(elementId).innerHTML=content;
		}else{
			obj.document.getElementById(elementId).innerHTML=content;
		}
	},

	/**
	 * iframe文档对象
	 * @param frm
	 * @returns {*}
	 */
	getDocument:function(frm){
		let obj=(typeof frm)=='string'?_$(frm):frm;
		if(!obj) return null;
		if(obj.contentDocument){
			return obj.contentDocument;
		}else{
			return obj.document;
		}
	},

	/**
	 * iframe window对象
	 * @param frm
	 * @returns {WindowProxy}
	 */
	getWindow:function(frm){
		let obj=(typeof frm)=='string'?_$(frm):frm;
		if(!obj) return null;
		return obj.contentWindow;
	}
}
window.IFrame=IFrame;

//剪贴板操作
let Copy={
	copy:function(text){
		let input = _$('copy-input');
		if(!input){
			input = document.createElement('input');
			input.readOnly = true;//防止聚焦触发键盘事件
			input.style.position = "absolute";
			input.style.top = "-100px";
			input.style.zIndex = "-1";
			document.body.appendChild(input);
		}
		input.value = text;
		Utils.selectText(input, 0, text.length);
		document.execCommand('copy');
		input.blur();
		if(input.id!='copy-input') document.body.removeChild(input);
		Toast.show(null, 'I{已复制}');
	}
}
window.Copy=Copy;

//Cookie处理
let Cookie={
	/**
	 * 设置cookie
	 * @param name
	 * @param value
	 * @param hours 有效时间，默认30天
	 */
	set:function(name,value,hours){
		//已拒绝接受Cookie
		/*if(name!='CookieAccepted'
			&& name!='lang'
			&& name!='currency'
			&& name!='UA_ID'
			&& name!='accessToken'
			&& name!='businessField'
			&& name!='businessRole'
			&& name!='ssoUserRoles'
			&& this.get('CookieAccepted')
			&& this.get('CookieAccepted')=='false'){
			return;
		}*/

		if((typeof hours) != 'number') hours = 30*24;

		let exp  = new Date();
		exp.setTime(exp.getTime() + hours*60*60*1000);
		try{
			top.document.cookie = name + "="+ encodeURIComponent(value) + ";expires=" + exp.toUTCString()+';path=/';
		}catch(e){
		}
		try{
			document.cookie = name + "="+ encodeURIComponent(value) + ";expires=" + exp.toUTCString()+';path=/';
		}catch(e){}
	},

	//使用正则表达式得到cookie值
	get:function(name){
		if(top!=window && (typeof top.Cookie)=='function'){
			return top.Cookie.get(name);
		}

		let reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
		let arr=null;
		try{
			arr=top.document.cookie.match(reg);
		}catch(e){
			try{
				arr=document.cookie.match(reg);
			}catch(e2){}
		}
		if(arr) return decodeURIComponent(arr[2]);
		else return null;
	},

	//将超时时间设置为当前时间以前的的时间，自然cookie就超时了
	del:function(name){
		this.set(name, '', -1);
	},

	//接受
	accept:function (){
		top.Dialog.canClose=true;
		Cookie.set('CookieAccepted', 'true');
		Dialog.close();
		if(!Page.loaded && (typeof onReady)=='function'){
			try{
				onReady();
			}catch (e){
				console.log(e);
			}

			try{
				if((typeof headerOnReady)=='function') headerOnReady();
			}catch (e){
				console.log(e);
			}
		}
		Page.loaded=true;
	},

	//拒绝
	refuse:function (){
		top.Dialog.canClose=true;
		Cookie.set('CookieAccepted', 'false');
		Dialog.close();
		if(!Page.loaded && (typeof onReady)=='function'){
			try{
				onReady();
			}catch (e){
				console.log(e);
			}

			try{
				if((typeof headerOnReady)=='function') headerOnReady();
			}catch (e){
				console.log(e);
			}
		}
		Page.loaded=true;
	},

	//是否已接受
	accepted:function (){
		return this.get('CookieAccepted') && this.get('CookieAccepted')=='true';
	},

	//cookie 隐私策略
	showPolicies:function (forceShow){
		//已经接受，不再弹出
		if(this.get('CookieAccepted') && !forceShow) return;

		let policies = [];
		policies.push('<div class="r alignL" I18N="cn" style="display: none;">我们不会在您的设备上保存任何您的隐私信息（如姓名、电话、电子邮件、身份证号码......等），但我们会在您的计算机或移动设备上存储不包含您的隐私信息的Cookie（如会话标识、语言/时区/币种偏好......等），我们使用Cookie的主要目的为：身份验证、偏好设置、保障数据和服务的安全性、为用户提供更好的服务、提高服务效率、了解与改善服务、广告优化等。您可以选择接受或拒绝，如果您选择拒绝，将无法登录本网站，但不影响您浏览和使用本网站无需身份验证的资源和服务。</div>');
		policies.push('<div class="r alignL" I18N="en" style="display: none;">We will not save any of your privacy information (such as name, phone, email, ID card number, etc.) on your device, but we will store cookies (such as session ID, language/time zone/currency preference, etc.) that do not contain your privacy information on your computer or mobile device. Our main purposes of using cookies are: authentication, preference, security of data and services, providing better services, improve service efficiency, optimize advertising, etc. You can choose to accept or reject. If you reject, you will not be able to log in to this website, but it does not affect your browsing and use of resources and services on this website that do not require authentication.</div>');

		top.Dialog.canClose=false;
		top.Dialog.showAlert(Dialog.MSG_TYPE_WARN,
			null,
			null,
			window,
			'I{Cookie使用条款}',
			policies.join(''),
			['<div class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="Cookie.accept();">I{接受}</div>',
				'<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="Cookie.refuse();">I{拒绝}</div>']);

		top.Lang.translate();
	}
}
window.Cookie=Cookie;

//解析当前url中的参数
let Params={
	params:[],

	//初始化,解析并保持参数
	init:function(){
		this.params=Str.getParams(window.location.href);
	},

	//得到参数
	getPara:function(name){
		return this.params[name];
	},

	//得到参数拼串
	getParas:function(){
		let url='';
		for(let i in this.params){
			if(url===''){
				url+='?'+i+'='+encodeURIComponent(this.params[i]);
			}else{
				url+='&'+i+'='+encodeURIComponent(this.params[i]);
			}
		}
		return url;
	},

	/**
	 * 得到参数拼串
	 * @param excludes 排除参数（单个参数名，或多个参数名数组）
	 * @returns {string}
	 */
	getParamsExcluded:function(excludes){
		let url='';
		for(let i in this.params){
			if(excludes){
				if((typeof excludes)=='string' && i==excludes) continue;
				else if(Str.contains(excludes, i)) continue;
			}

			if(url==='') url+='?'+i+'='+encodeURIComponent(this.params[i]);
			else url+='&'+i+'='+encodeURIComponent(this.params[i]);
		}
		return url;
	},

	//js文件后面的参数
	paramsInJS:[],

	//初始化js文件后面的参数
	initJSParams:function(){
		let scripts=null;
		try{
			scripts=document.scripts;
		}catch(e){
			scripts=document.getElementsByTagName('script');
		}
		for(let i=0; scripts && i<scripts.length; i++){
			if(Str.isBlank(scripts[i].src)) continue;
			let uri=Str.getUri(scripts[i].src);
			if(uri.indexOf('?')>0) uri=uri.substring(0, uri.indexOf('?'));
			let keyValues=Str.getParams(scripts[i].src);
			for(let name in keyValues){
				this.paramsInJS[uri+'/'+name]=keyValues[name];
				//Logger.log(uri+'/'+name+' = '+keyValues[name]);
			}
		}
	},

	//得到跟在js文件后面的参数
	getJSPara:function(uri, name){
		return this.paramsInJS[uri+'/'+name];
	}
}
window.Params=Params;

//多语言资源
let Lang={
	enabed:true,

	//可用语种
	langs:[
		{'id':'en', 'name':'English', 'icon':'/framework/img/countries/GB.png', 'forCountries':['GB','US','UM']},
		{'id':'cn', 'name':'中文', 'icon':'/framework/img/countries/CN.png', 'forCountries':['CN']},
		//{'id':'jp', 'name':'日本語', 'icon':'/framework/img/countries/JP.png', 'forCountries':['JP']},
		//{'id':'ru', 'name':'Русский', 'icon':'/framework/img/countries/RU.png', 'forCountries':['RU']},
		//{'id':'fr', 'name':'Français', 'icon':'/framework/img/countries/FR.png', 'forCountries':['FR']},
		//{'id':'es', 'name':'Español', 'icon':'/framework/img/countries/ES.png', 'forCountries':['ES']}
	],

	//默认语种
	defaultLang:'en',

	//当前语种
	currentLang:null,

	//多语言资源
	strings:[],

	//列表自动隐藏计时器
	timer:null,

	//需要进行多语言处理的组件（需实现onI18NChanged方法）
	I18NComponents:[],

	//语言选择列表方向
	listDirection: 'below',

	/**
	 * 初始化（设置全局多语言资源、翻译页面）
	 */
	init:function (){
		let countryCode=UserAgent.getCountryCode();

		let lang=Params.getPara('lang');
		if(lang){
			console.log('set lang by param => '+lang);
			Lang.setCurrentLang(lang);
		}else if(Cookie.get('lang')){
			console.log('set lang by cookie => '+Cookie.get('lang'));
			lang=Cookie.get('lang');
		}

		if(Str.isBlank(lang)){
			lang = Domains.getSetting().lang;
			console.log('lang for domain => '+lang);

			//lang=this.getLangForCountry(countryCode);
			//console.log('lang for '+countryCode+' => '+lang);
		}

		if(Str.isBlank(lang)) lang=this.defaultLang;
		this.setCurrentLang(lang);

		let langObject=this.getCurrentLang();
		if(_$('langSelectorIcon')) _$('langSelectorIcon').innerHTML='<img src="'+langObject.icon+'"/>';
		if(_$('langSelectorName')) _$('langSelectorName').innerHTML=langObject.name;

		this.translate();
	},

	restore:function(){
		let lang=Params.getPara('lang');
		if(lang) Lang.setCurrentLang(lang);
		else if(Cookie.get('lang')) lang=Cookie.get('lang');
		if(Str.isBlank(lang)) lang=this.defaultLang;
		this.currentLang=lang;
	},

	/**
	 *
	 */
	regComponent:function(id, component){
		this.I18NComponents[id]=component;
	},

	/**
	 *
	 * @param lang
	 */
	getLang:function(lang){
		for(let i=0; i<this.langs.length; i++){
			if(this.langs[i].id==lang) return this.langs[i];
		}
		return this.getCurrentLang();
	},

	getLangForCountry:function (countryCode){
		for(let i=0; i<this.langs.length; i++){
			if(Str.contains(this.langs[i].forCountries, countryCode)) return this.langs[i].id;
		}
		return this.defaultLang;
	},

	/**
	 *
	 * @param lang
	 */
	exists:function(lang){
		if(Str.isBlank(lang)) return false;
		for(let i=0; i<this.langs.length; i++){
			if(this.langs[i].id==lang) return true;
		}
		return false;
	},

	/**
	 * 当前使用语种
	 * @returns {*}
	 */
	getCurrentLang:function(){
		let lang=this.currentLang;
		if(Str.isBlank(lang)) lang=this.defaultLang;
		for(let i=0; i<this.langs.length; i++){
			if(this.langs[i].id==lang) return this.langs[i];
		}
		return this.langs[0];
	},

	/**
	 * 设置使用语种
	 * @param lang
	 */
	setCurrentLang:function(lang){
		this.currentLang=lang;
		Cookie.del('lang');
		console.log('save lang to cookie => '+lang);
		Cookie.set('lang',lang);
	},

	/**
	 *
	 * @param content
	 * @param lang
	 * @returns {*}
	 */
	convert:function(content, lang){
		if(Array.isArray(content)) content=content.join('');

		if((typeof content)!='string' || !content || content.indexOf('I{')<0) return content;
		if(Str.isBlank(lang)) lang=this.getCurrentLang().id;

		let fragments=content.split('I{');
		let startsWith=content.startsWith('I{');

		let _content=[];
		if(!startsWith) _content.push(fragments[0]);
		for(let i=(startsWith?0:1); i<fragments.length; i++) {
			if(Str.isBlank(fragments[i])) continue;

			let end=fragments[i].indexOf('}');
			if(end<0) {
				_content.push(fragments[i]);
				continue;
			}

			let key=fragments[i].substring(0, end);
			let theGroup='';
			let theKey=key;
			if(key.startsWith('.')){
				theGroup=UserAgent.currentUri;
				theKey=key.substring(1);
			}else if(key.indexOf(',')>0){
				theGroup=key.substring(0, key.indexOf(','));
				theKey=key.substring(key.indexOf(',')+1);
			}else{
				theGroup='';
				theKey=key;
			}

			let alt=this.getString(theGroup, theKey, lang);
			if(Str.isBlank(alt)) alt=theKey;

			_content.push(alt);
			_content.push(fragments[i].substring(end+1));
		}

		return _content.join('');
	},

	/**
	 * 批量加载多语言资源
	 * @param resources
	 */
	setStrings:function(resources){
		if(top!=window && (typeof top.Lang) != 'undefined' && (typeof top.Lang.setStrings) != 'undefined'){
			top.Lang.setStrings(resources);
			return;
		}
		for(let i=0; i<resources.groups.length; i++){
			let group = resources.groups[i].group;
			let tags = resources.groups[i].tags;
			for(let j=0; j<tags.length; j++){
				for(let k in tags[j].texts){
					this.setString(group, tags[j].tag, k, tags[j].texts[k]);
				}
			}
		}
	},

	/**
	 *
	 * @param group 多语言资源分组
	 * @param tag 多语言资源标记
	 * @param lang 语种ID
	 * @param val 对应语种的文本
	 */
	setString:function(group, tag, lang, text){
		if(top!=window && (typeof top.Lang) != 'undefined' && (typeof top.Lang.setString) != 'undefined'){
			top.Lang.setString(group, tag, lang, text);
			return;
		}
		if(group=='.') group=UserAgent.currentUri;
		//Logger.log('set I18N string -> '+group+','+tag+','+lang+'='+text);
		if(Str.isBlank(group)) this.strings[tag+'.'+lang]=text;
		else this.strings[group+','+tag+'.'+lang]=text;
	},

	/**
	 *
	 * @param group 多语言资源分组
	 * @param tag 多语言资源标记
	 * @param lang 语种ID
	 * @returns {any}
	 */
	getString:function(group, tag, lang){
		if(top!=window && (typeof top.Lang) != 'undefined' && (typeof top.Lang.getString) != 'undefined'){
			return top.Lang.getString(group, tag, lang);
		}
		if(Str.isBlank(group)){
			return this.strings[tag+'.'+lang];
		}else{
			let text = this.strings[group+','+tag+'.'+lang];
			if(Str.isBlank(text)) text = this.getString(null, tag, lang);
			return text;
		}
	},

	//需要翻译的标签内容
	tagsTobeTranslated:['#text','div','a','span','h1','h2','h3','ul','li','form','pre','table','tr','td','th','tbody'],

	/**
	 *
	 * @param tags
	 */
	translate:function(){
		if(Page.pageTitle){
			document.title=this.convert(Page.pageTitle);
			if(Page.inLayer) Page.inLayer.setTitle(this.convert(Page.pageTitle));
		}

		for(let i in this.I18NComponents){
			try{
				if((typeof this.I18NComponents[i].I18NInit)=='function') this.I18NComponents[i].I18NInit();
			}catch (e){}
			try{
				if((typeof this.I18NComponents[i].onI18NChanged)=='function') this.I18NComponents[i].onI18NChanged();
			}catch (e){}
		}

		//翻译文档标题
		let lang=this.getCurrentLang().id;
		document.title=this.convert(document.title, lang);

		//从body的直接子节点开始翻译
		let nodes = document.body.childNodes;
		for(let i=0; i<nodes.length; i++){
			this.translateNode(nodes[i], lang);
		}

		//翻译input的value和placeholder
		let tags=['input','textarea'];
		for(let i=0; i<tags.length; i++){
			let elements=document.getElementsByTagName(tags[i]);
			for(let j=0; j<elements.length; j++){
				try {
					//Utils.setAtt(elements[j], 'tabindex', '-1');//禁用tab键切换输入焦点

					if ((typeof elements[j].value) == 'string') elements[j].value = this.convert(elements[j].value, lang);

					let placeholder = Utils.att(elements[j], 'placeholder');
					if (!placeholder) continue;
					Utils.setAtt(elements[j], 'placeholder', this.convert(placeholder, lang));
				}catch(e){}
			}
		}

		let I18Ns = _$cls('I18N');
		for(let i=0; i<I18Ns.length; i++) I18Ns[i].style.display='';
		if(__$('Jpage')) __$('Jpage').style.display='';

		if(top.Dialog.isOpen()) top.Dialog.toCenter();
	},

	translateNode:function(node, lang){
		//不需要翻译
		if(Str.containsIgnoreCase(this.tagsTobeTranslated, node.nodeName)) {
			//文本内容直接翻译
			if (node.nodeName == '#text') {
				if (node.nodeValue.indexOf('I{') > -1) {
					node.nodeValue = this.convert(node.nodeValue, lang);
				}
				return;
			}

			//如果I18N属性值为某个语言编码，则说明该元素只有当当前语言与指定语言编码一致时才显示
			let tag = Utils.att(node, 'I18N');
			if (this.exists(tag)) {
				let businessField = Utils.att(node, 'businessField');//如果指定了所属行业
				if (tag == this.currentLang && (Str.isBlank(businessField) || businessField == Fields.currentField)) node.style.display = '';
				else node.style.display = 'none';
				return;
			}
		}

		//翻译子节点
		let children = node.childNodes;
		for (let j = 0; j < children.length; j++) {
			this.translateNode(children[j], lang);
		}
	},

	/**
	 * 初始化语种选择组件
	 * @param container
	 */
	initLangSelector:function(container, listDirection){
		if(listDirection) this.listDirection=listDirection;
		let lang=this.getCurrentLang();

		let htm=[];
		htm.push('<div id="langSelector" onclick="Lang.showLangSelector();">');
		//htm.push('	<div id="langSelectorIcon"><img src="'+lang.icon+'"/></div>');
		htm.push('	<div id="langSelectorName">'+lang.name+'</div>');
		htm.push('	<div id="langSelectorArrow" class="right iconfont '+(this.listDirection=='below'?'icon-moreunfold':'icon-less')+'"></div>');
		htm.push('</div>');

		container=((typeof container)=='string'?_$(container):container);
		container.innerHTML=htm.join('');

		htm=[];
		htm.push('<div id="langSelectorItems">');
		for(let i=0;i<this.langs.length;i++){
			htm.push('	<div class="langSelectorItem"'+(i==0?'':'')+' onclick="Lang.changeLang(\''+this.langs[i].id+'\');">');
			//htm.push('		<div class="icon"><img src="'+this.langs[i].icon+'"/></div>');
			htm.push('		<div class="text">'+this.langs[i].name+'</div>');
			htm.push('	</div>');
		}
		htm.push('</div>');

		document.body.insertAdjacentHTML('afterbegin', htm.join(''));
		htm=null;
	},

	/**
	 * 显示语种选择组件
	 */
	showLangSelector:function(){
		Page.hideWidgets();
		if(this.timer) clearTimeout(this.timer);
		this.timer=setTimeout(Lang.hideLangSelector, 3000);
		if(_$('langSelectorItems')){
			_$('langSelectorItems').style.width=W.elementWidth(_$('langSelector').parentNode)+'px';

			let width=W.elementWidth(_$('langSelectorItems'));
			let offsetTop=W.elementTop(_$('langSelector').parentNode) + W.elementHeight(_$('langSelector').parentNode);
			let offsetLeft=W.elementLeft(_$('langSelector').parentNode);
			if(offsetLeft + width > Global.screenWidth) offsetLeft=Global.screenWidth-width;

			_$('langSelectorItems').style.zIndex=W.getMaxZIndex()+'';

			if(this.listDirection=='below') _$('langSelectorItems').style.top=(offsetTop-1)+'px';
			else{
				offsetTop=W.elementTop(_$('langSelector').parentNode);
				offsetTop -= W.elementHeight(_$('langSelectorItems'));
				_$('langSelectorItems').style.top=(offsetTop + 1)+'px';
			}
			_$('langSelectorItems').style.left=offsetLeft+'px';
			_$('langSelectorItems').style.visibility='visible';
		}
	},

	/**
	 * 隐藏语种选择组件
	 */
	hideLangSelector:function(){
		if(Lang.timer) clearTimeout(Lang.timer);
		if(_$('langSelectorItems')) _$('langSelectorItems').style.visibility='hidden';
	},

	/**
	 * 改变语言
	 * @param lang
	 */
	changeLang:function(lang){
		if(Auth.profile
			&& Auth.profile.country
			&& Auth.profile.country.iso_code){
			if(Auth.profile.country.iso_code=='CN' && lang!='cn'){
				//Page.alert('I{该版本在您当前所在的国家不可用}', null, null, Dialog.MSG_TYPE_WARN);
				//return;
			}
		}
		Cookie.set('lang',lang);
		location.href='/?lang='+lang;
	}
}
window.Lang=Lang;

//币种
let Currency={
	//可用币种
	currenciesAvailable:['CNY','USDT','USD'],
	//currenciesAvailable:['USDT','USD','GBP','EUR','AUD','RUB','JPY','HKD','CNY'],

	//币种
	currencies:[],

	//默认币种
	defaultCurrency:'CNY',

	//当前语种
	currentCurrency:null,

	//列表自动隐藏计时器
	timer:null,

	//自动更新汇率计时器
	refreshInterval:null,

	//语言选择列表方向
	listDirection: 'below',

	//精度设置
	getPrecision:function(precisions, currencyId){
		if(!precisions || !precisions[currencyId]) return 2;
		return precisions[currencyId];
	},

	//初始化
	init:function(resp){
		if(resp.currencies){
			for(let i=0; i<resp.currencies.length; i++){
				if(!Str.contains(Currency.currenciesAvailable, resp.currencies[i].currencyId)) continue;

				let c={'id':resp.currencies[i].currencyId,
					'name':resp.currencies[i].currencyId,
					'nameCn':resp.currencies[i].currencyNameGb,
					'icon':resp.currencies[i].currencySymbol.split(',')[0],
					'rate':resp.currencies[i].currencyRate,
					'precision':Currency.getPrecision(resp.precisions, resp.currencies[i].currencyId)};
				this.currencies.push(c);
				Logger.log('load currency -> '+c);
			}
		}

		let currencyObject=this.getCurrentCurrency();
		if(_$('currencySelectorIcon')) _$('currencySelectorIcon').innerHTML=currencyObject.icon;
		if(_$('currencySelectorName')) _$('currencySelectorName').innerHTML=currencyObject.name;
	},

	//从cookie加载当前币种
	restore:function(){
		let currency=Params.getPara('currency');
		if(currency) Currency.setCurrentCurrency(currency);
		else if(Cookie.get('currency')) this.currentCurrency=Cookie.get('currency');
		else Currency.setCurrentCurrency(Domains.getSetting().currency);
		Logger.log('current currency = '+this.currentCurrency);
	},

	/**
	 *
	 * @param id
	 */
	getCurrency:function(id){
		if(Str.isBlank(id)) return this.getDefaultCurrency();
		for(let i=0; i<this.currencies.length; i++){
			if(this.currencies[i].id==id) return this.currencies[i];
		}
		return null;
	},

	/**
	 *
	 * @param id
	 */
	getDefaultCurrency:function(){
		return this.getCurrency(this.defaultCurrency);
	},

	/**
	 * 当前使用语种
	 * @returns {*}
	 */
	getCurrentCurrency:function(){
		let currency=this.currentCurrency;
		if(Str.isBlank(currency)) currency=this.defaultCurrency;
		for(let i=0; i<this.currencies.length; i++){
			if(this.currencies[i].id==currency) return this.currencies[i];
		}
		return this.currencies[0];
	},

	/**
	 * 设置使用语种
	 * @param currency
	 */
	setCurrentCurrency:function(currency){
		this.currentCurrency=currency;
		Cookie.del('currency');
		Cookie.set('currency',currency);
	},

	/**
	 * 定时更新汇率
	 */
	refreshRatePeriodic:function(){
		if(Currency.refreshInterval) clearInterval(Currency.refreshInterval);
		Currency.refreshInterval=setInterval(Currency.refreshRate, 30000);
	},

	/**
	 * 更新汇率
	 * @param url
	 */
	refreshRate:function(){
		let ajax=new Ajax();
		ajax.send('GET',Currency.doRefreshRate,'/api/jpay/currency/list');
	},

	/**
	 *
	 * @param ajax
	 */
	doRefreshRate:function(ajax){
		if(ajax.getReadyState()==4 && ajax.getStatus()==200){
			let resp=ajax.getResponseJson();
			if(resp.currencies){
				/**
				 * 		"currencyId": "1",
				 * 		"currencyNameBig": "人民币",
				 * 		"currencyNameGb": "人民币",
				 * 		"currencyNameEn": "RMB",
				 * 		"currencyRate": 1,
				 * 		"currencySymbol": "￥,CNY",
				 * 		"currencyFlag": "",
				 * 		"isAvail": "T"
				 */
				for(let i=0; i<resp.currencies.length; i++){
					let id=resp.currencies[i].currencyId;
					let c=Currency.getCurrency(id);
					if(!c) continue;
					c.rate=resp.currencies[i].currencyRate;
					//Logger.log('refresh currency rate '+c.id+' = '+c.rate);
				}
			}
		}
	},

	/**
	 *
	 * @param amount 金额
	 * @param from 转换前币种
	 * @param to 转化后币种
	 * @param precision 精确度
	 * @returns {*}
	 */
	exchange:function(amount, from, to, precision){
		if(!from) from=this.getDefaultCurrency().id;

		let currencyFrom=this.getCurrency(from);
		let currencyTo=this.getCurrency(to);
		if(!currencyTo || currencyTo.id == currencyFrom.id) return amount;//未指定目标币种

		if(!precision) precision=currencyTo.precision;
		amount=amount*currencyFrom.rate/currencyTo.rate;
		return MathUtil.toFixedTrim(amount, precision);
	},

	format:function(amount, precision){
		if(!precision) precision=this.getCurrentCurrency().precision;
		return MathUtil.toFixed(amount, precision);
	},

	/**
	 * 初始化语种选择组件
	 * @param container
	 */
	initCurrencySelector:function(container, listDirection){
		if(listDirection) this.listDirection=listDirection;
		let currency=this.getCurrentCurrency();

		let htm=[];
		htm.push('<div id="currencySelector" onclick="Currency.showCurrencySelector();">');
		htm.push('	<div id="currencySelectorIcon">'+currency.icon+'</div>');
		htm.push('	<div id="currencySelectorName">'+currency.name+'</div>');
		htm.push('	<div id="currencySelectorArrow" class="right iconfont '+(this.listDirection=='below'?'icon-moreunfold':'icon-less')+'"></div>');
		htm.push('</div>');

		container=((typeof container)=='string'?_$(container):container);
		container.innerHTML=htm.join('');

		htm=[];
		htm.push('<div id="currencySelectorItems">');
		for(let i=0;i<this.currenciesAvailable.length;i++){
			let c = this.getCurrency(this.currenciesAvailable[i]);
			htm.push('	<div class="currencySelectorItem"'+(i==0?'':'')+' onclick="Currency.changeCurrency(\''+c.id+'\');">');
			htm.push('		<div class="icon">'+c.icon+'</div>');
			htm.push('		<div class="text">'+c.name+'</div>');
			htm.push('	</div>');
		}
		htm.push('</div>');

		document.body.insertAdjacentHTML('afterbegin', htm.join(''));
		htm=null;
	},

	/**
	 * 显示语种选择组件
	 */
	showCurrencySelector:function(){
		Page.hideWidgets();
		if(this.timer) clearTimeout(this.timer);
		this.timer=setTimeout(Currency.hideCurrencySelector, 3000);
		if(_$('currencySelectorItems')){
			_$('currencySelectorItems').style.width=W.elementWidth(_$('currencySelector').parentNode)+'px';

			let width=W.elementWidth(_$('currencySelectorItems'));
			let offsetTop=W.elementTop(_$('currencySelector').parentNode) + W.elementHeight(_$('currencySelector').parentNode);
			let offsetLeft=W.elementLeft(_$('currencySelector').parentNode);
			if(offsetLeft + width > Global.screenWidth) offsetLeft=Global.screenWidth-width;

			_$('currencySelectorItems').style.zIndex=W.getMaxZIndex()+'';

			if(this.listDirection=='below') _$('currencySelectorItems').style.top=(offsetTop-1)+'px';
			else{
				offsetTop=W.elementTop(_$('currencySelector').parentNode);
				offsetTop -= W.elementHeight(_$('currencySelectorItems'));
				_$('currencySelectorItems').style.top=(offsetTop + 1)+'px';
			}
			_$('currencySelectorItems').style.left=offsetLeft+'px';
			_$('currencySelectorItems').style.visibility='visible';
		}
	},

	/**
	 * 隐藏语种选择组件
	 */
	hideCurrencySelector:function(){
		if(this.Currency) clearTimeout(this.Currency);
		if(_$('currencySelectorItems')) _$('currencySelectorItems').style.visibility='hidden';
	},

	/**
	 * 改变语言
	 * @param currency
	 */
	changeCurrency:function(currency){
		Cookie.set('currency',currency);
		top.location.reload();
	}
}
window.Currency=Currency;

//时区
let TimeZones={
	//时间显示格式（通用）：yyyy-mm-dd hh:mm:ss.sss
	TIME_FORMAT_COMMON:1,

	//时间显示格式（美国）：mm/dd/yyyy hh:mm:ss.sss
	TIME_FORMAT_US:2,

	//时间显示格式（欧洲）：dd/mm/yyyy hh:mm:ss.sss
	TIME_FORMAT_UR:3,

	//语言选择列表方向
	listDirection: 'below',

	/**
	 * 得到当前客户端的时区
	 * @returns {string}
	 */
	getTimeZone: function(){
		let timeZoneOffset = this.getTimeOffset();
		return 'GMT'+(timeZoneOffset<0?'-':'+')+Math.abs(timeZoneOffset);
	},

	/**
	 * 得到当前客户端与GMT+0时区之间的时间差（小时），正数表示GMT+*时区，负数表示GMT-*时区
	 * @returns {string}
	 */
	getTimeOffset: function(){
		let timeZoneOffset = (new Date()).getTimezoneOffset();
		return 0 - Math.round(timeZoneOffset / 60).toFixed(0);
	},

	/**
	 * 得到用户设置的时区（没设置则返回当前时区）
	 * @returns {string}
	 */
	getTimeZoneSet: function(){
		if(Cookie.get('timeZone')) return Cookie.get('timeZone');
		return this.getTimeZone();
	},

	/**
	 * 根据所在国家确定时间显示格式
	 * @param isoCode
	 */
	getTimeFormat:function (isoCode){
		if(!isoCode && top.Auth && top.Auth.profile && top.Auth.profile.country && top.Auth.profile.country.iso_code){
			isoCode=top.Auth.profile.country.iso_code;
		}
		if(!isoCode) isoCode='UNKNOWN';

		if(isoCode=='US' || isoCode=='USA') return TimeZones.TIME_FORMAT_US;
		else if(isoCode=='CN') return TimeZones.TIME_FORMAT_COMMON;
		else return TimeZones.TIME_FORMAT_UR;
	},

	/**
	 * 将utc时间转换为本地格式字符串
	 * @param utcMillis
	 * @param timeZone
	 * @param format
	 * @param showMillis
	 * @returns {string}
	 */
	toLocaleString: function(utcMillis, timeZone, format, showMillis){
		if(!utcMillis) utcMillis=(new Date()).getTime();
		if(timeZone==null || (typeof timeZone)=='undefined') timeZone=this.getTimeZone();

		if(timeZone && (typeof timeZone)=='string' && timeZone.startsWith('GMT')){
			timeZone=timeZone.substring(4)*1;
		}

		if(timeZone && (typeof timeZone)=='number' && timeZone>=-12 && timeZone<=12){
			let timeZoneOffset = this.getTimeOffset();
			let diff = timeZone-timeZoneOffset;
			utcMillis+=Global.msOfHour*diff;
		}

		let time=new Date(utcMillis);

		let y=time.getFullYear();
		let m=time.getMonth()+1;
		let d=time.getDate();

		let hh=time.getHours();
		let mm=time.getMinutes();
		let ss=time.getSeconds();
		let sss=time.getMilliseconds();

		if(!format) format=this.getTimeFormat();
		let s=[];
		if(format==TimeZones.TIME_FORMAT_US){
			s.push((m<10?'0':'')+m);
			s.push('/');
			s.push((d<10?'0':'')+d);
			s.push('/');
			s.push(y);
		}else if(format==TimeZones.TIME_FORMAT_UR){
			s.push((d<10?'0':'')+d);
			s.push('/');
			s.push((m<10?'0':'')+m);
			s.push('/');
			s.push(y);
		}else{
			s.push(y);
			s.push('-');
			s.push((m<10?'0':'')+m);
			s.push('-');
			s.push((d<10?'0':'')+d);
		}
		s.push(' ');
		s.push((hh<10?'0':'')+hh);
		s.push(':');
		s.push((mm<10?'0':'')+mm);
		s.push(':');
		s.push((ss<10?'0':'')+ss);
		if(showMillis && sss>0){
			s.push('.');
			s.push(sss);
		}
		return s.join('');
	},

	/**
	 * 初始化语种选择组件
	 * @param container
	 */
	initTimeZoneSelector:function(container, listDirection){
		if(listDirection) this.listDirection=listDirection;
		let timeZone=this.getTimeZoneSet();

		let htm=[];
		htm.push('<div id="timeZoneSelector" onclick="TimeZones.showTimeZoneSelector();">');
		htm.push('	<div id="timeZoneSelectorName">'+timeZone+'</div>');
		htm.push('	<div id="timeZoneSelectorArrow" class="iconfont '+(this.listDirection=='below'?'icon-moreunfold':'icon-less')+'"></div>');
		htm.push('</div>');

		container=((typeof container)=='string'?_$(container):container);
		container.innerHTML=htm.join('');

		htm=[];
		htm.push('<div id="timeZoneSelectorItems">');
		for(let i=-12;i<=12;i++){
			let zoneName='GMT'+(i>=0?'+':'')+i;
			htm.push('	<div class="timeZoneSelectorItem"'+(i==0?'':'')+' onclick="TimeZones.changeTimeZone(\''+zoneName+'\');">');
			htm.push('		<div class="text">'+zoneName+'</div>');
			htm.push('	</div>');
		}
		htm.push('</div>');

		document.body.insertAdjacentHTML('afterbegin', htm.join(''));
		htm=null;
	},

	/**
	 * 显示语种选择组件
	 */
	showTimeZoneSelector:function(){
		Page.hideWidgets();
		if(this.timer) clearTimeout(this.timer);
		this.timer=setTimeout(TimeZones.hideTimeZoneSelector, 3000);
		if(_$('timeZoneSelectorItems')){
			_$('timeZoneSelectorItems').style.width=W.elementWidth(_$('timeZoneSelector').parentNode)+'px';

			let width=W.elementWidth(_$('timeZoneSelectorItems'));
			let offsetTop=W.elementTop(_$('timeZoneSelector').parentNode) + W.elementHeight(_$('timeZoneSelector').parentNode);
			let offsetLeft=W.elementLeft(_$('timeZoneSelector').parentNode);
			if(offsetLeft + width > Global.screenWidth) offsetLeft=Global.screenWidth-width;

			_$('timeZoneSelectorItems').style.zIndex=W.getMaxZIndex()+'';

			if(this.listDirection=='below') _$('timeZoneSelectorItems').style.top=(offsetTop-1)+'px';
			else{
				offsetTop=W.elementTop(_$('timeZoneSelector').parentNode);
				offsetTop -= W.elementHeight(_$('timeZoneSelectorItems'));
				_$('timeZoneSelectorItems').style.top=(offsetTop + 1)+'px';
			}
			_$('timeZoneSelectorItems').style.left=offsetLeft+'px';
			_$('timeZoneSelectorItems').style.visibility='visible';
		}
	},

	/**
	 * 隐藏语种选择组件
	 */
	hideTimeZoneSelector:function(){
		if(TimeZones.timer) clearTimeout(TimeZones.timer);
		if(_$('timeZoneSelectorItems')) _$('timeZoneSelectorItems').style.visibility='hidden';
	},

	/**
	 * 改变时区
	 * @param timeZone
	 */
	changeTimeZone:function(timeZone){
		Cookie.set('timeZone',timeZone);
		top.location.reload();
	}
}
window.TimeZones=TimeZones;

//行业与业务角色类别选择
let Fields={
	textChooseField:'',
	textChooseRole:'',

	onI18NChanged:function(){
		this.textChooseField=Lang.convert('I{请选择行业}');
		this.textChooseRole=Lang.convert('I{请选择业务角色}');
	},

	currentField:'',
	currentRole:'',
	MERCHANT_NUM:'',//当前设定商户
	fields:[],

	//列表自动隐藏计时器
	timer:null,

	init:function(resp){
		if(resp.businessFields){
			for(let i=0; i<resp.businessFields.length; i++){
				this.addField(''+resp.businessFields[i].code, resp.businessFields[i]);
				Logger.log('load field -> '+resp.businessFields[i]);
			}
		}

		let setting = Domains.getSetting();
		if(!setting.multiFields){
			this.currentField=Domains.getSetting().businessField;
			this.currentRole=Domains.getSetting().businessRole;
			Cookie.set('businessField', this.currentField);
			Cookie.set('businessRole', this.currentRole);
			return;
		}

		if(resp.businessField) Cookie.set('businessField', resp.businessField);
		if(resp.businessRole) Cookie.set('businessRole', resp.businessRole);
	},

	set:function(businessField){
		let setting = Domains.getSetting();
		if(!setting.multiFields){
			return;
		}

		this.currentField=businessField;
		Cookie.set('businessField', this.currentField);
	},

	restore:function(){
		let setting = Domains.getSetting();
		if(!setting.multiFields){
			this.currentField=Domains.getSetting().businessField;
			this.currentRole=Domains.getSetting().businessRole;
			Cookie.set('businessField', this.currentField);
			Cookie.set('businessRole', this.currentRole);
			return;
		}

		let businessField=Params.getPara('businessField');
		if(businessField){
			Cookie.set('businessField', businessField);
		}else if(Cookie.get('businessField')){
			this.currentField=Cookie.get('businessField');
		}else{
			this.currentField=Domains.getSetting().businessField;
		}

		let businessRole=Params.getPara('businessRole');
		if(businessRole){
			Cookie.set('businessRole', businessRole);
		}else if(Cookie.get('businessRole')){
			this.currentRole=Cookie.get('businessRole');
		}else{
			this.currentRole=Domains.getSetting().businessRole;
		}

		let MERCHANT_NUM=Params.getPara('MERCHANT_NUM');
		if(MERCHANT_NUM){
			Cookie.set('MERCHANT_NUM', MERCHANT_NUM);
		}else{
			this.MERCHANT_NUM='';
		}

		if(Cookie.get('businessField')) this.currentField=Cookie.get('businessField');
		if(Cookie.get('businessRole')) this.currentRole=Cookie.get('businessRole');
		if(Cookie.get('MERCHANT_NUM')) this.MERCHANT_NUM=Cookie.get('MERCHANT_NUM');
		Logger.log('current businessField = '+this.currentField);
		Logger.log('current businessRole = '+this.currentRole)
		Logger.log('current MERCHANT_NUM = '+this.MERCHANT_NUM);
	},

	addField:function(fieldId, obj){
		this.fields[fieldId]=obj;
	},

	getField:function(fieldId){
		if(Str.isBlank(fieldId)) return this.fields[this.currentField];
		else return this.fields[fieldId];
	},

	initFields:function(selector,keepFirstItem,initValue){
		if((typeof keepFirstItem)=='undefined') keepFirstItem=true;

		let list=selector.options;
		if(keepFirstItem && list.length==0){
			list.add(new Option(this.textChooseField,''));
		}else{
			while(list && list.length>1){
				list.remove(list.length-1);
			}
		}

		for(let i in this.fields){
			list.add(new Option(this.fields[i].name,this.fields[i].code));
		}
		if(initValue) selector.value=initValue;
	},

	initRoles:function(fieldId,selector,keepFirstItem,initValue){
		if(!selector) return;
		let list=selector.options;
		if(!list) return;

		if((typeof keepFirstItem)=='undefined') keepFirstItem=true;

		if(keepFirstItem && list.length==0){
			list.add(new Option(this.textChooseRole,''));
		}else{
			while(list && list.length>1){
				list.remove(list.length-1);
			}
		}

		if(!this.fields[fieldId]) return;

		for(let i=0;i<this.fields[fieldId].roles.length;i++){
			list.add(new Option(this.fields[fieldId].roles[i].name,this.fields[fieldId].roles[i].code));
		}
		if(initValue) selector.value=initValue;
	},

	/**
	 * 初始化行业选择组件
	 * @param container
	 */
	initFieldSelector:function(container){
		let field = Fields.getField(this.currentField);

		let htm=[];
		htm.push('<div id="fieldSelector" onclick="Fields.showFieldSelector();">');
		htm.push('	<div id="fieldSelectorName">I{field,'+field.name+'}</div>');
		htm.push('	<div id="fieldSelectorArrow" class="right iconfont icon-moreunfold"></div>');
		htm.push('</div>');

		container=((typeof container)=='string'?_$(container):container);
		container.innerHTML=Lang.convert(htm.join(''));

		htm=[];
		htm.push('<div id="fieldSelectorItems">');
		for(let fieldId in this.fields){
			htm.push('	<div class="fieldSelectorItem" onclick="Fields.changeField(\''+this.fields[fieldId].code+'\');">');
			htm.push('		<div class="text">I{field,'+this.fields[fieldId].name+'}</div>');
			htm.push('	</div>');
		}
		htm.push('</div>');

		document.body.insertAdjacentHTML('afterbegin', Lang.convert(htm.join('')));
		htm=null;
	},

	/**
	 * 显示行业选择组件
	 */
	showFieldSelector:function(){
		Page.hideWidgets();
		if(this.timer) clearTimeout(this.timer);
		this.timer=setTimeout(Fields.hideFieldSelector, 3000);
		if(_$('fieldSelectorItems')){
			_$('fieldSelectorItems').style.width=W.elementWidth(_$('fieldSelector').parentNode)+'px';

			let width=W.elementWidth(_$('fieldSelectorItems'));
			let offsetTop=W.elementTop(_$('fieldSelector').parentNode) + W.elementHeight(_$('fieldSelector').parentNode);
			let offsetLeft=W.elementLeft(_$('fieldSelector').parentNode);
			if(offsetLeft + width > Global.screenWidth) offsetLeft=Global.screenWidth-width;

			_$('fieldSelectorItems').style.zIndex=W.getMaxZIndex()+'';

			_$('fieldSelectorItems').style.top=(offsetTop-0)+'px';
			_$('fieldSelectorItems').style.left=offsetLeft+'px';
			_$('fieldSelectorItems').style.visibility='visible';
		}
	},

	/**
	 * 隐藏行业选择组件
	 */
	hideFieldSelector:function(){
		if(Fields.timer) clearTimeout(Fields.timer);
		if(_$('fieldSelectorItems')) _$('fieldSelectorItems').style.visibility='hidden';
	},

	changeField:function(field){
		top.location.href='/?businessField='+field;
	}
}
window.Fields=Fields;

//可滑动tab栏
let SlidableTabsInstances=[];
function SlidableTabs(id,contentId,cookieId,callback,width){
	this.id=id;
	this.contentId=contentId;
	this.cookieId=cookieId;
	this.callback=callback;
	this.width=width;
	this.moveInterval=null;
	this.tabWidth=10;//一个tab的宽度
	this.scrollEnabled=false;

	if(!width){
		this.visibleWidth=W.vw();
	}else{
		this.visibleWidth=width;
	}
	_$(id).style.width=this.visibleWidth+'px';


	let tabs=_$cls('slidableTabCurrent');
	if(tabs.length==0) tabs=_$cls('slidableTab');
	if(tabs.length>0) this.tabWidth=W.elementWidth(tabs[0])+5;

	let tabsWidth=W.elementScrollWidth(_$(contentId));
	this.tabsMaxSlideDistance=tabsWidth-this.visibleWidth;

	SlidableTabsInstances[id]=this;
}

SlidableTabs.prototype.init=function(){
	let tabsWidth=W.elementScrollWidth(_$(this.contentId));
	this.tabsMaxSlideDistance=tabsWidth-this.visibleWidth;

	if(UserAgent.isMobile()&&tabsWidth>this.visibleWidth){//手机端，且需要滑动
		this.scrollEnabled=true;
		let touch=new Touch(_$(this.id),
			10,
			null,
			this.callback?this.callback:SlidableTabsCallbackDefault,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null);
		touch.preventDefaultOnClick=false;
	}
}

SlidableTabs.prototype.initPC=function(){
}

//向左
SlidableTabs.prototype.toLeft=function(){
	if(this.moveInterval){
		clearInterval(this.moveInterval);
		this.moveInterval=null;
	}
	this.moveInterval=setInterval("SlidableTabsToLeft('"+this.id+"')",this.tabWidth>10?500:100);
}
SlidableTabs.prototype.toLeftDo=function(){
	let _left=_$(this.id).scrollLeft;
	_left-=this.tabWidth;
	if(_left<0) _left=0;

	_$(this.id).scrollLeft=_left;
}
SlidableTabs.prototype.toLeftCancel=function(){
	if(this.moveInterval){
		clearInterval(this.moveInterval);
		this.moveInterval=null;
	}
}
SlidableTabs.prototype.toStart=function(){
	this.toLeftCancel();
	_$(this.id).scrollLeft=0;
}
//向左 end

//向右
SlidableTabs.prototype.toRight=function(){
	if(this.moveInterval){
		clearInterval(this.moveInterval);
		this.moveInterval=null;
	}
	this.moveInterval=setInterval("SlidableTabsToRight('"+this.id+"')",this.tabWidth>10?500:100);
}
SlidableTabs.prototype.toRightDo=function(){
	let _left=_$(this.id).scrollLeft;
	_left+=this.tabWidth;
	if(_left>this.tabsMaxSlideDistance) _left=this.tabsMaxSlideDistance;

	_$(this.id).scrollLeft=_left;
}
SlidableTabs.prototype.toRightCancel=function(){
	if(this.moveInterval){
		clearInterval(this.moveInterval);
		this.moveInterval=null;
	}
}
SlidableTabs.prototype.toEnd=function(){
	this.toRightCancel();
	_$(this.id).scrollLeft=this.tabsMaxSlideDistance;
}
//向右 end

function SlidableTabsCallbackDefault(event,_instance){
	let id=_instance.obj.id;

	let distance=Math.floor(_instance.pageX-_instance.initPageX);
	let _left=_$(id).scrollLeft;
	_left-=distance;
	if(_left>SlidableTabsInstances[id].tabsMaxSlideDistance) _left=SlidableTabsInstances[id].tabsMaxSlideDistance;
	if(_left<0) _left=0;

	_$(id).scrollLeft=_left;

	if(SlidableTabsInstances[id].cookieId){
		Cookie.set(SlidableTabsInstances[id].cookieId+'_tabs_scroll_left',_left+'');
	}
}

function SlidableTabsToLeft(id){
	SlidableTabsInstances[id].toLeftDo();
}
function SlidableTabsToRight(id){
	SlidableTabsInstances[id].toRightDo();
}
//可滑动tab栏 end

//通用组件 - Toast
let Toast={
	SHORT:2000,
	LONG:4000,
	timer:null,
	topMin:20,
	_top:0,

	show:function(eventOrPark, txt, showTime, bg, color, _top){
		if(this.timer) clearTimeout(this.timer);
		if(_top) this._top=_top;
		else this._top=0;

		txt=Lang.convert(txt);

		if(_$('TOAST_GLOBAL')) this.hide();

		let str='<div id="TOAST_GLOBAL" style="z-index:'+W.getMaxZIndex()+';'+(bg?('background-color:'+bg+'!important;'):'')+(color?('color:'+color+'!important;'):'')+'visibility:hidden;">'+txt+'</div>';
		document.body.insertAdjacentHTML('afterbegin', str);
		_$('TOAST_GLOBAL').style.visibility='visible';
		_$('TOAST_GLOBAL').style.top=this.getTop(eventOrPark)+'px';
		_$('TOAST_GLOBAL').style.left=this.getLeft(eventOrPark)+'px';

		this.timer=setTimeout(Toast.hide,showTime?showTime:this.LONG);
	},

	hide:function(){
		if(this.timer) clearTimeout(this.timer);
		_$('TOAST_GLOBAL').parentNode.removeChild(_$('TOAST_GLOBAL'));
	},

	setContent:function(txt){
		_$('TOAST_GLOBAL').innerHTML=txt;
	},

	getTop:function(eventOrPark){
		if(this._top>0) return this._top;

		let park=null;
		if(eventOrPark){
			if((typeof eventOrPark)=='object') park=eventOrPark;
			else park=Utils.getEventTarget(eventOrPark);
		}

		let theTop=0;
		if(!park && document.activeElement){
			let tagName=document.activeElement.tagName.toUpperCase();
			if(tagName=='INPUT'||tagName=='TEXTAREA'||tagName=='SELECT') park=document.activeElement;
		}

		if(park) theTop=W.elementTop(park)+W.elementHeight(park)+5;
		else theTop=W.tTotal()+Math.floor((top.W.vh()-W.elementHeight(_$('TOAST_GLOBAL')))/2);

		if(theTop<this.topMin) theTop=this.topMin;
		return theTop;
	},

	getLeft:function(eventOrPark){
		let theLeft=0;

		let park=null;
		if(eventOrPark){
			if((typeof eventOrPark)=='object') park=eventOrPark;
			else park=Utils.getEventTarget(eventOrPark);
		}

		if(!park && document.activeElement){
			let tagName=document.activeElement.tagName.toUpperCase();
			if(tagName=='INPUT'||tagName=='TEXTAREA'||tagName=='SELECT') park=document.activeElement;
		}

		if(park) theLeft=W.elementLeft(park);
		else theLeft=Math.ceil((W.vw()-W.elementWidth(_$('TOAST_GLOBAL')))/2);

		return Math.max(theLeft, 10);
	}
}
window.Toast=Toast;

//XML操作
let XML={
	parse:function(str){
		let doc;
		if(typeof(ActiveXObject)!='undefined'){
			doc=new ActiveXObject('Microsoft.XMLDOM');
			doc.async=false;
			doc.loadXML(str);
		}else{
			doc = (new DOMParser()).parseFromString(str,'text/xml')
		}
		return doc;
	},

	getRoot:function(doc){
		if(doc.getDocumentElement) return doc.getDocumentElement();
		else return doc.documentElement;
	},

	getChildNodes:function(element, childNodeName){
		let nodes=element.childNodes;
		if(nodes && childNodeName){
			let _nodes=[];
			for(let i=0;i<nodes.length;i++){
				if(nodes[i].nodeName == childNodeName) _nodes.push(nodes[i]);
			}
			return _nodes;
		}else{
			return nodes;
		}
	},

	getAttr:function(element, attrName){
		if(!element) return null;
		let attr=element.attributes.getNamedItem(attrName);
		if(typeof(attr.text)!='undefined') return attr.text;
		else return attr.textContent;
	},

	getText:function(element){
		if(!element) return null;
		return element.childNodes && element.childNodes.length>0?element.childNodes[0].nodeValue:'';
	}
}
window.XML=XML;

//API配置
let APIs={
	/**
	 *
	 * @param conf
	 * @constructor
	 */
	API: function (conf){
		this.uri = conf.uri;
		this.desc = conf.desc;
		this.codes = conf.codes;
		this.onOk = conf.onOk;
		this.onErr = conf.onErr;
	},

	//全局错误代码
	codes: {
		ERR: '系统错误',
		not_login: '未登录',
		access_denied: '您没有权限进行当前操作'
	},

	//api配置
	apis: [],

	//配置api信息
	config: function(conf){
		this.apis.push(new APIs.API(conf));
	},

	//根据请求的url获取api配置
	ofUrl: function (url){
		let uri=Str.getUri(url);
		for(let i=0; i<this.apis.length; i++){
			if(this.apis[i].uri == uri) return this.apis[i];
		}
		return null;
	},

	//根据请求对象（Ajax）获取api配置
	ofAjax: function (ajax){
		return this.ofUrl(ajax.url);
	}
}

/**
 * 根据错误代码获取提示信息
 */
APIs.API.prototype.getMsg=function (code){
	return this.codes[code] ? this.codes[code] : (APIs.codes[code] ? APIs.codes[code] : APIs.codes['ERR']);
}

APIs.config({
	uri: '/api/platform/user/thirdparty/code',
	desc: '获取第三方登录授权码',
	codes: {
		invalid_provider: '未指定有效的第三方',
		create_state_code_failed: '启动第三方登录流程失败',
		create_redirect_url_failed: '启动第三方登录流程失败'
	},
	onOk: null,
	onErr: null
});

APIs.config({
	uri: '/api/platform/user/thirdparty/login',
	desc: '用获得的授权码获取第三方登录accessToken和第三方用户信息',
	codes: {
		invalid_provider: '未指定有效的第三方',
		invalid_state_code: '安全检测失败',
		invalid_auth_code: '授权失败',
		get_tokens_failed: '登录失败',
		get_profile_failed: '获取用户信息失败'
	},
	onOk: null,
	onErr: null
});
//API配置 end

//图片处理
/**
 * 图片对象
 * @param id
 * @param src
 * @param onload
 * @param onerr
 * @param ondone
 * @param fullScreen
 * @param parentSizeFixed
 * @constructor
 */
function ImageObject(id,src,onload,onerr,ondone,fullScreen,parentSizeFixed,setStyle){
	this.id=id;
	this.src=src;
	this.fullScreen=(typeof fullScreen)=='undefined'?true:fullScreen;
	this.parentSizeFixed=(typeof parentSizeFixed)=='undefined'?true:parentSizeFixed;
	this.setStyle=(typeof setStyle)=='undefined'?false:setStyle;
	this.adjustTimer=null;

	let img=new Image();
	img.crossOrigin='anonymous';
	img.id=id;
	img.src=src;
	if(onload){
		let appname = navigator.appName.toLowerCase();
		if(appname.indexOf("netscape") == -1){//IE
			img.onreadystatechange = function () {
				if(img.readyState == "complete"||img.readyState == "loaded") onload.call(window, this);
			};
		}else{//其它
			img.onload = function () {
				if(img.complete == true) onload.call(window, this);
			};
		}
	}

	if(onerr){
		img.onerror = function () {
			onerr.call(window, this);
		};
		img.onabort = function () {
			onerr.call(window, this);
		};
	}

	this.ondone=ondone;
	this.img=img;
}

let IMG={
	images:[],

	//添加一个需要预加载的图片
	add:function(id,src,onload,onerr,fullScreen,parentSizeFixed,setStyle){
		let img=this.images[id];
		if(!img){
			img=new ImageObject(id,src,onload,onerr,fullScreen,parentSizeFixed,setStyle);
			this.images[id]=img;
		}
	},

	//根据id得到image对象
	get:function(id){
		return this.images[id];
	},

	//把预加载的图片的src赋给指定的对象
	set:function(obj,id){
		obj.src=this.images[id].src;
	},

	//重置图片
	reset:function(id){
		if(this.images[id]) this.images[id]=null;
		Utils.delAtt(_$(id),'src');
		Utils.delAtt(_$(id),'width');
		Utils.delAtt(_$(id),'height');
	},

	//重置全部记录
	resetAll(){
		this.images = new Array();
	},

	/**
	 *
	 * @param id 图片ID
	 * @param idLoading 表示该图片正在加载中的对象（比如转圈的gif）的ID
	 * @param zoomType 0，不调整大小，1，按宽  2，按高  3，按较长一边  4，正好满屏，较长（相对于容器长宽比例而言）一边超出容器部分隐藏
	 * @param imgMaxWidth 最大宽度
	 * @param imgMaxHeight 最大高度
	 * @param middle 是否上下居中
	 * @param center 是否左右居中
	 * @param onload 图片加载完毕时回调
	 * @param onerr 出错时回调
	 * @param ondone 调整完毕时回调
	 * @param fullScreen 如果实际尺寸小于设定尺寸是否放大到设定尺寸
	 * @param parentSizeFixed 图片容器大小是否固定（不自动调整）
	 * @param setStyle 是否通过style设置大小（默认：否）
	 */
	adjust:function(id,idLoading,zoomType,imgMaxWidth,imgMaxHeight,middle,center,onload,onerr,ondone,fullScreen,parentSizeFixed,setStyle){
		if(!_$(id)) return;
		if((typeof setStyle)=='undefined') setStyle=true;

		let _parentNode=Utils.getParentNodeExcludeTag(_$(id),'a');

		//百分比转换
		if(imgMaxWidth<=1&&imgMaxWidth>0) imgMaxWidth=Math.floor(W.elementWidth(_parentNode)*imgMaxWidth);
		if(imgMaxHeight<=1&&imgMaxHeight>0) imgMaxHeight=Math.floor(W.elementWidth(_parentNode)*imgMaxHeight);

		let imgObejct=this.images[id];
		let imgSrc=Utils.att(_$(id),'_src')?Utils.att(_$(id),'_src'):_$(id).src;
		if(!imgObejct){
			this.add(id,
				imgSrc,
				onload,
				onerr,
				ondone,
				fullScreen,
				parentSizeFixed,
				setStyle);
			_$(id).style.marginTop='0px';
			_$(id).style.marginLeft='0px';
			imgObejct=this.images[id];
		}else{
			if(imgSrc!=imgObejct.src){
				imgObejct.src=imgSrc;
				imgObejct.img.src=imgSrc;
			}
			if(imgObejct.adjustTimer){
				clearTimeout(imgObejct.adjustTimer);
				imgObejct.adjustTimer=null;
			}
		}

		if(zoomType==0){//无需调整，直接显示
			if(!Str.isBlank(Utils.att(_$(id), '_src'))) _$(id).src=imgObejct.src;
			_$(id).style.display='';
			return;
		}

		let img=imgObejct.img;
		if(img.width<=0){
			//等待加载完毕
			imgObejct.adjustTimer=setTimeout("IMG.adjust('"+id+"','"+idLoading+"',"+zoomType+","+imgMaxWidth+","+imgMaxHeight+","+middle+","+center+",null,null,null,"+imgObejct.fullScreen+","+imgObejct.parentSizeFixed+","+imgObejct.setStyle+")",100);
			return;
		}

		if(imgMaxWidth<=0 && imgMaxHeight<=0){
			imgMaxWidth=img.width;
			imgMaxHeight=img.height;
		}
		let w=img.width;//图片实际宽度
		let h=img.height;//图片实际高度
		let newW=w;//新设宽度
		let newH=h;//新设高度

		if(zoomType==1){//1,按宽
			if(imgObejct.fullScreen || w>imgMaxWidth){
				newW=imgMaxWidth;
				newH=Math.round(h*(newW/w));
				_$(id).width=newW;
				_$(id).height=newH;
				if(setStyle){
					_$(id).style.width=newW+'px';
					_$(id).style.height='auto';
				}
			}
		}else if(zoomType==2){//2，按高
			if(imgObejct.fullScreen || h>imgMaxHeight){
				newH=imgMaxHeight;
				newW=Math.round(w*(newH/h));
				_$(id).height=newH;
				_$(id).width=newW;
				if(setStyle) {
					_$(id).style.height=newH+'px';
					_$(id).style.width='auto';
				}
			}
		}else if(zoomType==3){//3，按较长一边
			let containerRatio=imgMaxWidth/imgMaxHeight;//容器宽高比例
			let imgRatio=w/h;//图片宽高比例

			if(!imgObejct.parentSizeFixed){
				if(_parentNode){
					_parentNode.style.width=imgMaxWidth+'px';
					_parentNode.style.height=imgMaxHeight+'px';
				}
			}
			if(_parentNode) _parentNode.style.overflow='hidden';

			if(containerRatio>imgRatio){//容器比图片更狭长，图片高度方向顶边
				if(imgObejct.fullScreen || h>imgMaxHeight){
					newH=imgMaxHeight;
					newW=Math.round(w*(newH/h));
					_$(id).height=newH;
					_$(id).width=newW;
					if(setStyle) {
						_$(id).style.height = newH + 'px';
						_$(id).style.width = 'auto';
					}
				}
			}else{//图片宽度方向顶边
				if(imgObejct.fullScreen || w>imgMaxWidth){
					newW=imgMaxWidth;
					newH=Math.round(h*(newW/w));
					_$(id).width=newW;
					_$(id).height=newH;
					if(setStyle) {
						_$(id).style.width = newW + 'px';
						_$(id).style.height = 'auto';
					}
				}
			}
		}else if(zoomType==4){
			if(!imgObejct.parentSizeFixed){
				if(_parentNode){
					_parentNode.style.width=imgMaxWidth+'px';
					_parentNode.style.height=imgMaxHeight+'px';
				}
			}
			if(_parentNode) _parentNode.style.overflow='hidden';

			let containerRatio=imgMaxWidth/imgMaxHeight;//容器宽高比例
			let imgRatio=w/h;//图片实际宽高比例

			if(containerRatio>imgRatio) {//容器比图片更狭长，满屏后高度方向超出容器
				newW=imgMaxWidth;
				newH=Math.round(newW/imgRatio);

				_$(id).height=newH;
				_$(id).width=newW;
				if(setStyle){
					_$(id).style.height=newH+'px';
					_$(id).style.width=newW+'px';
				}

				if(_parentNode){
					_parentNode.scrollTop=Math.round((newH-imgMaxHeight)/2);
				}
			}else if(imgRatio>containerRatio){//满屏后宽度方向超出容器
				newH=imgMaxHeight;
				newW=Math.round(imgRatio*newH);

				_$(id).height=newH;
				_$(id).width=newW;
				if(setStyle){
					_$(id).style.height=newH+'px';
					_$(id).style.width=newW+'px';
				}

				if(_parentNode){
					_parentNode.scrollLeft=Math.round((newW-imgMaxWidth)/2);
				}
			}else {
				newW=imgMaxWidth;
				newH=imgMaxHeight;

				_$(id).height=newH;
				_$(id).width=newW;
				if(setStyle){
					_$(id).style.height=newH+'px';
					_$(id).style.width=newW+'px';
				}
			}
		}

		//上下居中
		if(middle && zoomType!=4){
			if(newH<imgMaxHeight) _$(id).style.marginTop=Math.floor((imgMaxHeight-newH)/2)+'px';
			else _$(id).style.marginTop='0px';
		}

		//左右居中
		if(center && zoomType!=4){
			if(newW<imgMaxWidth) _$(id).style.marginLeft=Math.floor((imgMaxWidth-newW)/2)+'px';
			else _$(id).style.marginLeft='0px';
		}

		if(idLoading&&_$(idLoading)) _$(idLoading).style.display='none';
		if(!Str.isBlank(Utils.att(_$(id), '_src'))) _$(id).src=imgObejct.src;
		_$(id).style.display='';

		if((typeof imgObejct.ondone)=='function') imgObejct.ondone.call(window,id,newW,newH);
	}
}
window.IMG=IMG;

//图片拼接
/**
 * 图片拼接
 * @param args
 * @constructor
 */
function ImageMerger(args){
	this.width=(typeof args.width)=='number' ? args.width : W.vw();
	this.height=(typeof args.height)=='number' ? args.height : W.vh();
	this.units=(Array.isArray(args.units)) ? args.units : [];
	this.target=args.target;
	this.canvas=null;
	this.context=null;
	this.bgColor=(typeof args.bgColor)=='string' ? args.bgColor : '#FFF';
	this.callback=args.callback;
	this.callbackTarget=args.callbackTarget;
	this.loaded=0;
}

/**
 *
 * @param unit
 */
ImageMerger.prototype.appendUnit=function(unit){
	this.units.push(unit);
}

/**
 *
 */
ImageMerger.prototype.merge=function(){
	//初始化画布
	this.canvas = document.createElement("canvas");
	this.canvas.width = this.width;
	this.canvas.height = this.height;
	this.context = this.canvas.getContext("2d");
	this.unitOffsetYAdjust = 0;

	//根据屏幕像素比缩放
	let ratio=W.getPixelRatio(this.context)
	this.canvas.style.width = this.canvas.width + 'px';
	this.canvas.style.height = this.canvas.height + 'px';
	this.canvas.width = this.canvas.width * ratio;
	this.canvas.height = this.canvas.height * ratio;
	this.context.scale(ratio, ratio);

	if(!Str.isBlank(this.bgColor)) {
		this.context.fillStyle = this.bgColor;
		this.context.fillRect(0, 0, this.width, this.height);
	}

	for(let i=0; i<this.units.length; i++) {
		Logger.log('try to load unit('+this.units[i].type+') -> '+this.units[i].source);
		this.units[i].container=this;
		this.units[i].load();
	}
}

/**
 *
 * @param _target
 * @returns {HTMLImageElement|*}
 */
ImageMerger.prototype.toImage=function(_target){
	if(_target){
		_target.src=this.canvas.toDataURL('image/png');
		return _target;
	}

	if(this.target){
		this.target.src=this.canvas.toDataURL('image/png');
		return this.target;
	}

	let image=new Image();
	image.crossOrigin='anonymous';
	image.src=this.canvas.toDataURL('image/png');
	return image;
}

/**
 * 当某个元素加载完毕
 */
ImageMerger.prototype.onUnitLoad=function(){
	this.loaded++;
	Logger.log('this.loaded = '+this.loaded + ' of '+this.units.length);
	if(this.loaded>=this.units.length){//元素全部加载完毕，回调
		if(this.callback) this.callback.call(this.callbackTarget ? this.callbackTarget : window, this);
	}
}

/**
 * 图片拼接单元
 * @param args
 * @constructor
 */
function ImageMergerUnit(args){
	this.uuid=Global.generateUUID();
	this.type=(typeof args.type)=='string' ? args.type : 'image';// image | text | line
	this.width=(typeof args.width)=='number' ? args.width : W.vw();
	this.height=(typeof args.height)=='number' ? args.height : W.vh();
	this.offsetX=(typeof args.offsetX)=='number' ? args.offsetX : 0;
	this.offsetY=(typeof args.offsetY)=='number' ? args.offsetY : 0;
	this.quality=(typeof args.quality)=='number' ? args.quality : 1;
	this.source=args.source;//url | file | text
	this.fontStyle=args.fontStyle;
	this.container=null;
	this.fontColor=(typeof args.fontColor)=='string' ? args.fontColor : '#333333';
	this.align=(typeof args.align)=='string' ? args.align : 'left';

	let _unit=this;
	if(this.type=='image'){
		this.img=new Image();//图像
		this.img.crossOrigin='anonymous';
		this.img.onload = function(e){//加载完成后
			Logger.log('image unit of ImageMerger loaded -> '+_unit.uuid);
			_unit.container.context.drawImage(_unit.img,
				_unit.offsetX,
				_unit.offsetY,
				_unit.width,
				_unit.height);
			_unit.container.onUnitLoad();
		};

		if((typeof this.source)!='string'){
			this.reader=new FileReader();//文件读取器
			this.reader.onload = function(e){//读取完成后给图像赋值
				Logger.log('image(file) unit of ImageMerger loaded -> '+_unit.uuid);
				_unit.img.src = e.target.result;//图像赋值后会触发img的onload
			};
		}
	}
}

/**
 * 加载
 */
ImageMergerUnit.prototype.load=function(){
	this.offsetY+=this.container.unitOffsetYAdjust;//根据上下元素实际输出高度调整位置

	if(this.type=='text') {//如果是文字
		if(this.fontColor) this.container.context.fillStyle = this.fontColor;
		if(this.fontStyle) this.container.context.font = this.fontStyle;

		let textWidth=this.container.context.measureText(this.source).width;
		if(this.align=='center'){//文本居中
			this.offsetX=Math.round((this.container.width - textWidth)/2);
			if(this.offsetX<0) this.offsetX=0;
		}

		//换行输出
		let line='';
		let output = 0;
		let lines=0;
		let textHeight=0;
		while(output < this.source.length){
			line+=this.source.substring(output, output+1);
			if(this.container.context.measureText(line).width >= this.width){
				this.container.context.fillText(line+'\n',this.offsetX,this.offsetY+lines*20, this.width);
				line='';
				lines++;
				textHeight+=20;
			}
			output++;
		}
		if(!Str.isBlank(line)){
			textHeight+=20;
			this.container.context.fillText(line,this.offsetX,this.offsetY+lines*20, this.width);
		}
		this.container.unitOffsetYAdjust+=(textHeight - this.height);
		//换行输出 end

		Logger.log('unit('+this.type+') '+this.source+' loaded.');
		this.container.onUnitLoad();
		return;
	}

	if(this.type=='line') {//如果是线条
		this.container.context.moveTo(this.offsetX,this.offsetY);
		this.container.context.lineTo(this.offsetX+this.width,this.offsetY);
		this.container.context.closePath();
		this.container.context.fillStyle=this.fontColor;
		this.container.context.lineWidth=this.height;
		this.container.context.fill();
		Logger.log('unit('+this.type+') '+this.source+' loaded.');
		this.container.onUnitLoad();
		return;
	}

	if(Str.isBlank(this.source)){
		this.container.onUnitLoad();
		return;
	}

	if((typeof this.source)=='string') this.img.src = this.source;
	else this.reader.readAsDataURL(this.source);
}

///////////分享//////////////
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
		this.posterHeight=UserAgent.isPC()?800:(top.W.vh()-54);
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
			height: 1,
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

//媒体查看组件
let ImageViewer={
	layer:null,//在哪个Layer对象中操作（如指定了容器，则layer为null）
	container:null,//容器
	width:0,//工作区宽度
	height:0,//工作区高度
	WHRatio:1,//当前图片比例
	win:null,//关联窗口
	onClose:null,//关闭时回调
	current:0,//当前显示第几个
	allImages:[],//媒体列表
	sizes:[],//每个媒体的宽高比
	clickTime:0,//最近一次点击时间（两次点击很近时实现双击还原图片尺寸的功能）
	fromJMedia:false,//是否等待上传的图片（由JMedia加载、压缩）
	trim:false,//是否剪裁
	trimming:false,//是否正在剪裁（编辑）
	trimmingMask:'',//剪裁蒙版
	trimmingMedia:null,//剪裁媒体对象
	trimWHRatio:0,//剪裁区域宽高比
	trimWHRatioCustomized:true,//剪裁比例是否可调整
	initX:0,
	initY:0,
	initTop:0,
	initLeft:0,
	initWidth:0,
	initHeight:0,
	invokeCallback:false,//是否回调
	canClose:true,
	noTitle:false,
	zoomType:3,
	currentObjectId:null,//当前操作的对象（剪裁框或图）
	aigcEnabled:false,//是否启用AIGC

	/**
	 * 媒体对象
	 * @param name
	 * @param url
	 * @param cover
	 * @param mediaId JMedia对象的ID
	 * @param callback 回调
	 * @param callbackTarget 调用回调方法的对象（不指定则为ImageViewer.win）
	 * @param data 业务数据
	 * @constructor
	 */
	Media:function (name, url, cover, mediaId, callback, callbackTarget, data){
		this.name=Lang.convert(name);
		this.url=url;
		this.cover=cover;
		this.mediaId=mediaId;
		this.callback=callback;
		this.callbackTarget=callbackTarget;
		this.data=data;
	},

	/**
	 * 打开指定相册
	 * @param albumId
	 * @param albumPwd
	 * @param mediaType
	 */
	openAlbum:function(albumId, albumPwd, mediaType){
		top.Dialog.open(-1,-1,-1,-1,null, null, window,'waiting');
		let ajax=new Ajax();
		ajax.mediaType=(mediaType?mediaType:'');
		ajax.send('GET',ImageViewer.doOpenAlbum,'/api/platform/cms/media/list?album_id='+albumId+'&album_pwd='+(albumPwd?albumPwd:'')+'&media_type='+(mediaType?mediaType:''));
	},

	/**
	 * 打开用户相册
	 * @param albumId
	 * @param albumPwd
	 * @param mediaType
	 */
	openAlbumOfUser:function(uid, albumPwd, mediaType){
		top.Dialog.open(-1,-1,-1,-1,null, null, window,'waiting');
		let ajax=new Ajax();
		ajax.mediaType=(mediaType?mediaType:'');
		ajax.send('GET',ImageViewer.doOpenAlbum,'/api/platform/cms/media/list?uid='+uid+'&album_pwd='+(albumPwd?albumPwd:'')+'&media_type='+(mediaType?mediaType:''));
	},

	doOpenAlbum:function(ajax){
		if(ajax.getReadyState()==4&&ajax.getStatus()==200){
			top.Dialog.close();
			let resp=ajax.getResponseJson();
			if(resp.success){
				let list=resp.datas.photos;
				if(list.length==0){
					Page.alert('I{ImageViewer,暂无相关照片或视频}', null, null, Dialog.MSG_TYPE_INFO);
				}else{
					let _images=[];
					for(let i=list.length-1; i>=0; i--) _images.push(new ImageViewer.Media('', list[i].display.mediaLink, list[i].display.imgLogo));
					ImageViewer.open(window, _images);
				}
			}else if(resp.code=='non_login'){
				Auth.showNotLoginMessage();
			}else{
				Page.alert(resp.message, null, null, Dialog.MSG_TYPE_ERR);
			}
		}
	},

	/**
	 * 是否已经打开
	 * @returns {boolean}
	 */
	isOpen:function(){
		return this.container!=null;
	},

	/**
	 * 媒体是否已经存在与列表中
	 * @param src
	 */
	exists:function(src){
		for(let j=0;j<this.allImages.length;j++){
			if(this.allImages[j].url==src) return true;
		}
		return false;
	},

	/**
	 * 打开
	 * @param _win 关联窗口
	 * @param _images 图片 [ImageViewer.Media]
	 * @param _currentImgSrc 初始显示的媒体路径
	 * @param _onClose 关闭时回调
	 */
	open:function(_win,_images,_currentImgSrc,_onClose,_canClose,_noTitle,_zoomType,_container){
		if(top != window && (typeof top.ImageViewer) != 'undefined'){
			top.ImageViewer.trim=this.trim;
			top.ImageViewer.trimmingMask=this.trimmingMask;
			top.ImageViewer.trimWHRatioCustomized=this.trimWHRatioCustomized;
			top.ImageViewer.invokeCallback=this.invokeCallback;
			top.ImageViewer.aigcEnabled=this.aigcEnabled;

			this.close();

			top.ImageViewer.open(_win,_images,_currentImgSrc,_onClose,_canClose,_noTitle,_zoomType,_container);
			return;
		}

		if(this.isOpen()) this.close();

		this.win=_win?_win:window;
		this.onClose=_onClose?_onClose:null;
		this.canClose=(typeof _canClose)==='boolean'?_canClose:true;
		this.noTitle=(typeof _noTitle)==='boolean'?_noTitle:false;
		this.zoomType=(typeof _zoomType)==='number'?_zoomType:3;
		if(_container){
			this.container = (typeof _container)=='string' ? _$(_container) : _container;
		}

		if(_images && _images.length>0){//指定了要显示的媒体
			this.allImages=_images;

			//如果是剪裁模式，将url类型Media对象转换为JMedia类型
			if(this.trim && Str.isBlank(this.allImages[0].mediaId)){
				this.trimmingMedia=new JMedia(this.allImages[0].url, _$('ImageViewerCanvasWrapper'),6000,1, ImageViewer.trimPhotoLoaded);
				this.allImages=[];
			}else if(_currentImgSrc){
				for(let i=0;i<_images.length;i++){
					//初始要显示第几个
					if(_currentImgSrc.endsWith(_images[i].url) || _images[i].url.endsWith(_currentImgSrc)){
						this.current=i;
					}
				}
			}
		}else if(this.fromJMedia){//显示JMedia对象
			let jms=this.win.JMedias;
			for(let i in jms){
				if(jms[i] && jms[i].img){
					this.allImages.push(new ImageViewer.Media('', null, null, jms[i].id));
				}
			}
		}else if(!this.trim && Str.isBlank(this.trimmingMask)){//自动获取页面内图片
			let _allImages=this.win.document.getElementsByTagName('img');
			for(let i=0; _allImages && i<_allImages.length; i++){
				let img=_allImages[i];
				if(!Utils.visible(img)) continue;

				let src=Utils.att(img,'_src');
				if(!src) src=img.src;
				if(!src || (src.indexOf('/i/')<0 && src.indexOf('/im/')<0))  continue;

				src=Str.replaceAll(src,'_logo','');
				src=Str.replaceAll(src,'_mini','');

				//图片的自定义属性alt（商品名）
				let alt=Utils.att(img,'alt');
				if(!alt) alt=Utils.att(img.parentNode.parentNode,'alt');

				//图片的自定义属性gid（商品ID）
				let gid=Utils.att(img,'gid');
				if(!gid) gid=Utils.att(img.parentNode.parentNode, 'gid');

				if(this.win.Page.hasPageFeature('onlyShowGoodsImages') && !gid){//仅显示商品图片
					continue;
				}

				//已经存在
				if(this.exists(src)) continue;

				if(_currentImgSrc && (_currentImgSrc.endsWith(src) || src.endsWith(_currentImgSrc))){
					this.current=this.allImages.length;
				}

				if(alt && gid) this.allImages.push(new ImageViewer.Media(alt, src, null, null, gid));
				else this.allImages.push(new ImageViewer.Media('', src, null, null));
			}
		}

		if(!this.trim && Str.isBlank(this.trimmingMask) && this.allImages.length==0){
			this.close(false);
			Page.alert('I{ImageViewer,没有可浏览的图片或视频}');
			return;
		}

		if(this.trim) this.currentObjectId='imageViewerTrimBox';

		//初始化组件
		this.init();

		//显示操作区
		this.showBtns();

		//加载
		this.load();

		//显示
		this.show();
	},

	/**
	 * 初始化组件
	 */
	init:function(){
		if(!this.container){
			this.layer=Layers.open(window,'','',null, '', 0, this.close, true, this.noTitle, this.canClose);
			this.container=this.layer.getContentElement();
		}
		if(this.layer) this.layer.setBtns('<div>&nbsp;</div>');

		this.width=W.elementWidth(this.container);
		this.height=W.elementHeight(this.container);
		this.WHRatio=this.width/this.height;

		let str=[];

		if(UserAgent.isMobile()){
			str.push('<div id="imageViewerTitle">I{ImageViewer,双指缩放，双击还原}</div>');
		}else{
			str.push('	<div id="imageViewerTitle">');
			str.push('		<div class="displayBlock iconfont icon-back" onclick="ImageViewer.right();"></div>');
			str.push('		<div class="displayBlock iconfont icon-more mL30" onclick="ImageViewer.left();"></div>');
			str.push('	</div>');
		}
		if(this.layer) this.layer.setTitle(str.join(''));

		if(this.layer) str=[];
		str.push('<div id="imageViewer">');
		str.push('	<div id="imageViewerContainer" style="width:'+this.width+'px; height:'+this.height+'px;"></div>');
		str.push('</div>');
		if(this.layer) this.layer.setContent(str.join(''));

		if(this.layer) str=[];
		str.push('<div id="imageViewerFooter">');
		str.push('	<div id="imageViewerMediaName"></div>');
		str.push('	<div id="imageViewerNumbers"></div>');
		str.push('</div>');
		if(this.layer) this.layer.setBtns(str.join(''));

		if(!this.layer) this.container.innerHTML=Lang.convert(str.join(''));

		str=[];
		if(!Str.isBlank(this.trimmingMask)){//指定了蒙版
			str.push('<div id="imageViewerTrimBox" class="imageViewerTrimBox" style="border:none !important; z-index:'+W.getMaxZIndex()+' !important;">');
			str.push('<img id="imageViewerTrimMask" _src="'+this.trimmingMask+'" style="display: none;"/>')
		}else{
			str.push('<div id="imageViewerTrimBox" class="imageViewerTrimBox" style="z-index:'+W.getMaxZIndex()+' !important;">');
		}
		str.push('</div>');
		document.body.insertAdjacentHTML('afterbegin', str.join(''));
		str=null;
		delete str;

		if(!_$('ImageViewerCanvasWrapper')){
			let cavasContainer = document.createElement('div');
			cavasContainer.id='ImageViewerCanvasWrapper';
			cavasContainer.className='cavasWrapper';
			document.body.appendChild(cavasContainer);
		}
	},

	/**
	 * 加载
	 */
	load:function(){
		let htm=[];
		for(let i=0;i<this.allImages.length;i++){
			let imgSrc=this.allImages[i].url;

			if(Str.endsWith(imgSrc,'.mp4',true)
				||Str.endsWith(imgSrc,'.mov',true)
				||Str.endsWith(imgSrc,'.3gp',true)){
				htm.push('<div id="imageViewerImage_'+i+'" class="imageViewerImage" style="width:'+this.width+'px; height:'+this.height+'px; display: none;">');
				htm.push('	<video id="imageViewerImage_'+i+'_player" preload="auto" playsinline webkit-playsinline></video>');
				htm.push('</div>');
			}else if(Str.endsWith(imgSrc,'.mp3',true)
				||Str.endsWith(imgSrc,'.amr',true)
				||Str.endsWith(imgSrc,'.ogg',true)
				||Str.endsWith(imgSrc,'.wav',true)){
				htm.push('<div id="imageViewerImage_'+i+'" class="imageViewerImage" style="width:'+this.width+'px; height:'+this.height+'px; display: none;">');
				htm.push('	<audio id="imageViewerImage_'+i+'_player" autoplay="false" controls><source src="'+imgSrc+'" type="'+JMediaUtil.getAudioType(imgSrc)+'"></audio>');
				htm.push('</div>');
			}else{
				htm.push('<div id="imageViewerImage_'+i+'" class="imageViewerImage" style="width:'+this.width+'px; height:'+this.height+'px; display: none;">');
				htm.push('	<img id="imageViewerImage_'+i+'_img'+'" _src="'+imgSrc+'" style="display:none;"/>');
				htm.push('</div>');
			}
		}

		_$('imageViewerContainer').innerHTML=htm.join('');
		htm=null;
		delete htm;

		_$('imageViewerNumbers').innerHTML='1/'+this.allImages.length;
		for(let i=0;i<this.allImages.length;i++){
			if(Str.isBlank(this.allImages[i].mediaId)) continue;
			Utils.setAtt(_$('imageViewerImage_'+i+'_img'),'_src',this.getMedia(this.allImages[i].mediaId).getData());
		}

		//初始化视频播放器
		for(let i=0;i<this.allImages.length;i++){
			let imgSrc=this.allImages[i].url;
			let cover=this.allImages[i].cover;
			if(Str.endsWith(imgSrc,'.mp4',true)
				||Str.endsWith(imgSrc,'.mov',true)
				||Str.endsWith(imgSrc,'.3gp',true)){
				Players.addPlayer('imageViewerImage_'+i+'_player',
					imgSrc,
					false,
					this.width,
					this.height,
					false,
					this.allImages.length==1,
					true,
					(cover?cover:''),
					false,
					'H');

				Players.setMaxHeight('imageViewerImage_'+i+'_player', this.height);
			}
		}
		Players.initPlayers();

		//初始化图片
		for(let i=0;i<this.allImages.length;i++){
			let imgSrc=this.allImages[i].url;
			if(Str.endsWith(imgSrc,'.mp4',true)
				||Str.endsWith(imgSrc,'.mov',true)
				||Str.endsWith(imgSrc,'.3gp',true)
				||Str.endsWith(imgSrc,'.mp3',true)
				||Str.endsWith(imgSrc,'.amr',true)
				||Str.endsWith(imgSrc,'.ogg',true)
				||Str.endsWith(imgSrc,'.wav',true)){
				continue;
			}

			IMG.reset('imageViewerImage_'+i+'_img');
			IMG.adjust('imageViewerImage_'+i+'_img',
				null,
				this.zoomType,
				this.width,
				this.height,
				true,
				false,
				null,
				null,
				ImageViewer.trimShow,
				true,
				true,
				false);
		}

		if(_$('imageViewerTrimMask')){
			_$('imageViewerTrimMask').style.display='none';
			IMG.reset('imageViewerTrimMask');
			IMG.adjust('imageViewerTrimMask',
				null,
				3,
				this.width - 100,
				this.height - 100,
				false,
				false,
				null,
				null,
				ImageViewer.trimMaskLoaded,
				true,
				false,
				false);
		}

		new Touch(_$('imageViewerContainer'),
			10,
			this.start,
			this.moving,
			this.up,
			this.down,
			this.left,
			this.right,
			null,
			this.click,
			this.zoomIn,
			this.zoomOut,
			this.longPress);

		new Touch(_$('imageViewerTrimBox'),
			10,
			this.start,
			this.moving,
			this.up,
			this.down,
			this.left,
			this.right,
			null,
			this.click,
			this.zoomIn,
			this.zoomOut,
			this.longPress);
	},

	/**
	 * 操作区
	 */
	showBtns:function (){
		if(this.trim){//允许编辑
			let s=[];
			s.push('<div id="imageViewerTrimPickFile" class="fl mT10 mR10">');
			s.push('<div class="fileInputWithSkin" style="width:90px;">');
			s.push('    <div class="skin">');
			s.push('        <div class="aBtnWithIcon" style="width:90px;">');
			s.push('            <div class="aIcon iconfont icon-piclight"></div>');
			s.push('            <div class="aText" id="fileInputWithSkinText_image">I{ImageViewer,选择图片}</div>');
			s.push('        </div>');
			s.push('    </div>');
			s.push('    <div class="file">');
			s.push('        <input type="file" id="trimPhoto" name="trimPhoto" accept="image/*" onchange="ImageViewer.trimPhotoPicked();" single/>');
			s.push('    </div>');
			s.push('</div>');
			s.push('</div>');

			if(this.trim && this.aigcEnabled) {
				s.push('<div id="imageViewerAigc" class="fl mR10" onclick="AIGC.showAIGCDialog(null, ImageViewer.onAIGC, this.win);"><div class="font24px iconfont icon-a-Component1"></div></div>');
			}

			s.push('<div id="imageViewerTrimStart" class="btnH30 w60 fl mT10 mR10 btnBgGreen hidden" onclick="ImageViewer.trimShow();">I{ImageViewer,编辑}</div>');

			s.push('<div id="imageViewerTrimWHRatio" class="fl mR10" style="display:none;"><select id="imageViewerTrimWHRatioSelector" onchange="ImageViewer.setTrimWHRatio();">');
			s.push('    <option value="0">I{ImageViewer,宽高比例}</option>');
			s.push('    <option value="1">1:1</option>');
			s.push('    <option value="2/1">2:1</option>');
			s.push('    <option value="1/2">1:2</option>');
			s.push('    <option value="4/3">4:3</option>');
			s.push('    <option value="3/4">3:4</option>');
			s.push('    <option value="16/9">16:9</option>');
			s.push('    <option value="9/16">9:16</option>');
			s.push('</select></div>');

			s.push('<div id="imageViewerTrimZoomIn" class="fl mR10" style="display:none;" onclick="ImageViewer.zoomObject(0.01);"><div class="font24px iconfont icon-fangda"></div></div>');
			s.push('<div id="imageViewerTrimZoomOut" class="fl mR10" style="display:none;" onclick="ImageViewer.zoomObject(-0.01);"><div class="font24px iconfont icon-suoxiao"></div></div>');

			s.push('<div id="imageViewerTrimRotate" class="fl mR10" style="display:none;" onclick="ImageViewer.trimRotate();"><div class="font24px iconfont icon-shunshizhenxuanzhuan"></div></div>');
			s.push('<div id="imageViewerTrimDone" class="fl mR10" style="display:none;" onclick="ImageViewer.trimDone();"><div class="font24px iconfont icon-jianqie1"></div></div>');
			s.push('<div id="imageViewerTrimOriginal" class="fl mR10" style="display:none;" onclick="ImageViewer.trimShowOriginal();"><div class="font24px iconfont icon-huanyuan"></div></div>');
			s.push('<div id="imageViewerTrimFinish" class="fl mR10" style="display:none;" onclick="ImageViewer.trimCancel(); ImageViewer.close(true);"><div class="font24px iconfont icon-chenggong1 green"></div></div>');
			_$('imageViewerMediaName').innerHTML=Lang.convert(s.join(''));
			s=null;
			delete s;
		}else if(this.allImages.length>0){
			let imgBizName=this.allImages[this.current].name;
			_$('imageViewerMediaName').innerHTML=imgBizName;
		}
	},

	/**
	 * 设置额外按钮
	 * @param btns
	 */
	setBtns:function(btns){
		if(!btns) return;
		if(Array.isArray(btns)) btns=btns.join('');
		_$('imageViewerFooter').insertAdjacentHTML('afterbegin', Lang.convert(btns));
	},


	/**
	 * 显示
	 */
	show:function(){
		//停止所有视频、音频播放
		try{
			for(let i=0;i<this.allImages.length;i++){
				let player=Players.getPlayer('imageViewerImage_'+i+'_player');
				if(player){//视频
					Players.stop(player);
				}else if(_$('imageViewerImage_'+i+'_player')){//音频
					_$('imageViewerImage_'+i+'_player').pause();
				}
			}
		}catch(e){}

		let current=this.current;
		if(this.allImages.length>0) {
			for (let i = 0; i < this.allImages.length; i++) {
				_$('imageViewerImage_' + i).style.display = 'none';
			}
			_$('imageViewerImage_' + current).style.display = '';
		}
		_$('imageViewerNumbers').innerHTML=(current+1)+'/'+this.allImages.length;

		//回调
		if(this.allImages.length>0 && this.allImages[current].callback){
			this.allImages[current].callback.call(this.allImages[current].callbackTarget ? this.allImages[current].callbackTarget : this.win,
				current,
				this.allImages[current].data);
		}
	},

	/**
	 * 蒙版加载完毕（设定剪裁框固定比例）
	 */
	trimMaskLoaded:function (id,newW,newH){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.trimMaskLoaded(id,newW,newH);
			return;
		}

		//指定剪裁区比例，禁止手动设定
		if(_$('imageViewerTrimWHRatio')) _$('imageViewerTrimWHRatio').style.display='none';

		if(newW && newH) this.trimWHRatio=newW/newH;
		this.setTrimWHRatio(this.trimWHRatio, newW, newH);
	},

	/**
	 * 蒙版加载完毕（设定剪裁框固定比例）
	 */
	getMedia:function (mediaId){
		if(this.win.JMedias[mediaId]) return this.win.JMedias[mediaId];
		else return JMedias[mediaId];
	},

	/**
	 * 重置（编辑图片后）
	 */
	reset:function(){
		//停止所有视频、音频播放
		try{
			for(let i=0;i<this.allImages.length;i++){
				let player=Players.getPlayer('imageViewerImage_'+i+'_player');
				if(player){//视频
					Players.stop(player);
				}else if(_$('imageViewerImage_'+i+'_player')){//音频
					_$('imageViewerImage_'+i+'_player').pause();
				}
			}
		}catch(e){}

		let cimg=_$('imageViewerImage_'+this.current+'_img');
		let cimgContainer=_$('imageViewerImage_'+this.current);
		if(!cimg) return;
		if(!this.sizes[cimg.id]){
			this.sizes[cimg.id]=[cimg.width,cimg.height];
		}

		let maxWidth=this.width;
		let maxHeight=this.height;

		let sizes=this.sizes[cimg.id];
		let ratio=sizes[0]/sizes[1];
		if(ratio>maxWidth/maxHeight){
			cimg.width=maxWidth;
			cimg.height=Math.floor(cimg.width/ratio);
		}else{
			cimg.height=maxHeight;
			cimg.width=Math.floor(cimg.height*ratio);
		}
		cimgContainer.style.paddingLeft='0px';
		cimgContainer.style.paddingRight='0px';
		cimgContainer.style.paddingTop='0px';
		cimgContainer.style.paddingBottom='0px';
		cimgContainer.scrollLeft=0;
		cimgContainer.scrollTop=0;
	},

	/**
	 * 清除
	 */
	clear:function(clearTrimmingMedia){
		if(clearTrimmingMedia && this.trimmingMedia){
			this.trimmingMedia.clear();
			this.trimmingMedia=null;
		}
		this.allImages=[];
		this.sizes=[];
		this.current=0;
		_$('imageViewerContainer').innerHTML='';
	},

	/**
	 * 关闭
	 * @param _invokeCallback
	 */
	close:function(_invokeCallback){//关闭
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.close(_invokeCallback);
			return;
		}

		let trimBox=_$('imageViewerTrimBox');
		if(trimBox) trimBox.parentNode.removeChild(trimBox);

		if((_invokeCallback || this.invokeCallback) && this.onClose){
			try{
				if(this.trimmingMedia){
					this.onClose.call(this.win?this.win:window, this.trimmingMedia, this.trimmingMedia?this.trimmingMedia.dataOriginal:null, this.trimmingMedia?this.trimmingMedia.data:null);
				}else{
					let cimg = this.allImages[this.current];
					if(cimg && !Str.isBlank(cimg.mediaId) && JMedias[cimg.mediaId]){
						this.onClose.call(this.win?this.win:window, JMedias[cimg.mediaId], JMedias[cimg.mediaId].dataOriginal, JMedias[cimg.mediaId].data);
					}else{
						this.onClose.call(this.win?this.win:window);
					}
				}
			}catch(e){}
		}

		this.container=null;
		this.width=0;
		this.height=0;
		this.WHRatio=1;
		this.currentObjectId=null;
		this.aigcEnabled=false;
		this.win=null;//关联窗口
		this.onClose=null;//关闭时回调
		this.current=0;//当前显示第几个
		this.allImages=[];//每个媒体的路径
		this.sizes=[];//每个媒体的宽高比
		this.clickTime=0;//最近一次点击时间（两次点击很近时实现双击还原图片尺寸的功能）
		this.fromJMedia=false;//是否等待上传的图片（由JMedia加载、压缩）
		this.trim=false;//是否剪裁
		this.trimming=false;//是否正在剪裁
		this.trimmingMask=null;
		this.trimWHRatio=0;
		this.trimWHRatioCustomized=true;
		this.initX=0;
		this.initY=0;
		this.initTop=0;
		this.initLeft=0;
		this.initWidth=0;
		this.initHeight=0;
		this.invokeCallback=false;//是否回调
		if(this.trimmingMedia){
			this.trimmingMedia.clear();
			this.trimmingMedia=null;
		}

		if(this.layer){
			this.layer.close();
			this.layer=null;
		}
	},

	/**
	 * 编辑图片时宽高比（0表示不限制）
	 * @returns {number}
	 * @constructor
	 */
	getTrimWHRatio:function(){
		if(!_$('imageViewerTrimWHRatioSelector')) return 0;
		let ratio=_$('imageViewerTrimWHRatioSelector').value;
		if(ratio=='0') return 0;
		if(ratio.indexOf('/')>0){
			let a=ratio.substring(0, ratio.indexOf('/'));
			let b=ratio.substring(ratio.indexOf('/')+1);
			return a/b;
		}else{
			return ratio*1;
		}
	},

	/**
	 * 设置剪裁框宽高比
	 * @param ratio
	 * @param maxWidth
	 * @param maxHeight
	 * @param doNotMove
	 */
	setTrimWHRatio:function(ratio, maxWidth, maxHeight, doNotMove){
		if(!ratio) ratio= this.getTrimWHRatio();
		this.trimWHRatio = ratio;
		if(ratio==0) return;

		if(!maxWidth) maxWidth=this.width;
		if(!maxHeight) maxHeight=this.height;


		let cimg=_$('imageViewerImage_'+this.current+'_img');

		let trimBoxInitWidth=0;
		let trimBoxInitHeight=0;
		let imageRatio=this.WHRatio;
		if(!cimg){
			trimBoxInitWidth=maxWidth;
			trimBoxInitHeight=maxHeight;
		}else{
			trimBoxInitWidth=Math.min(maxWidth, cimg.width);
			trimBoxInitHeight=Math.min(maxHeight, cimg.height);
			imageRatio=cimg.width/cimg.height;
		}

		if(this.trimWHRatio>0){
			if(this.trimWHRatio > imageRatio){//剪裁框宽度方向顶边
				trimBoxInitHeight = trimBoxInitWidth/this.trimWHRatio;
			}else{//剪裁框高度方向顶边
				trimBoxInitWidth = trimBoxInitHeight*this.trimWHRatio;
			}
		}

		_$('imageViewerTrimBox').style.width=trimBoxInitWidth+'px';
		_$('imageViewerTrimBox').style.height=trimBoxInitHeight+'px';

		if(_$('imageViewerTrimMask')){
			_$('imageViewerTrimMask').width=trimBoxInitWidth;
			_$('imageViewerTrimMask').height=trimBoxInitHeight;
		}

		let trimBoxLeft=Math.floor((this.width-trimBoxInitWidth)/2);
		let trimBoxTop=Math.floor((this.height-trimBoxInitHeight)/2)+54;
		if(!doNotMove){
			_$('imageViewerTrimBox').style.left=trimBoxLeft+'px';
			_$('imageViewerTrimBox').style.top=trimBoxTop+'px';
		}
		_$('imageViewerTrimBox').style.visibility='visible';
	},

	getTrimBoxLeftMin:function (){
		let cimg=_$('imageViewerImage_'+this.current+'_img');
		let cimgContainer=_$('imageViewerImage_'+this.current);

		if(!cimg || cimgContainer.scrollLeft>0) return 0;
		return W.elementLeft(cimg, false);
	},

	getTrimBoxLeftMax:function (){
		return this.width - W.elementWidth('imageViewerTrimBox');
	},

	getTrimBoxTopMin:function (){
		let cimg=_$('imageViewerImage_'+this.current+'_img');
		let cimgContainer=_$('imageViewerImage_'+this.current);

		if(!cimg || cimgContainer.scrollTop>0) return 54;
		return W.elementTop(cimg, false) + 54;
	},

	getTrimBoxTopMax:function (){
		return this.height - W.elementHeight('imageViewerTrimBox') + 54;
	},

	/**
	 * 选择图片
	 */
	trimPhotoPicked:function (){
		this.reset();
		this.clear(true);
		if(_$('trimPhoto').files && _$('trimPhoto').files.length>0){
			this.trimmingMedia=new JMedia(_$('trimPhoto').files[0], _$('ImageViewerCanvasWrapper'),6000,1, ImageViewer.trimPhotoLoaded);
		}
	},

	/**
	 * 改变图片
	 */
	trimPhotoChange:function (imageUrl){
		this.reset();
		this.clear(true);
		this.trimmingMedia=new JMedia(imageUrl, _$('ImageViewerCanvasWrapper'),6000,1, ImageViewer.trimPhotoLoaded);
	},

	/**
	 * 图片加载完毕
	 */
	trimPhotoLoaded:function (img, imgTrim, id){
		ImageViewer.reset();
		ImageViewer.clear(false);
		ImageViewer.allImages.push(new ImageViewer.Media('', null, null, id));
		ImageViewer.fromJMedia=true;
		ImageViewer.load();
		ImageViewer.show();
		ImageViewer.trimShow();
	},

	/**
	 * 显示编辑相关内容
	 */
	trimShow:function(){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.trimShow();
			return;
		}

		if(!this.trim) return;
		if(this.allImages.length==0){
			Page.alert('I{ImageViewer,请先选中一张图片}', null, null, Dialog.MSG_TYPE_INFO);
			return;
		}

		//设置剪裁框大小位置
		this.setTrimWHRatio(this.trimWHRatio);

		this.trimming=true;
		_$('imageViewerTrimBox').style.visibility='visible';
		_$('imageViewerTrimStart').style.display='none';
		_$('imageViewerTrimDone').style.display='';

		if(Str.isBlank(this.trimmingMask) && this.trimWHRatioCustomized){
			_$('imageViewerTrimWHRatio').style.display='';
		}

		_$('imageViewerTrimRotate').style.display='';
		_$('imageViewerTrimFinish').style.display='';
		_$('imageViewerTrimOriginal').style.display='';
		if(_$('imageViewerTrimZoomIn')) _$('imageViewerTrimZoomIn').style.display='';
		if(_$('imageViewerTrimZoomOut')) _$('imageViewerTrimZoomOut').style.display='';
		if(_$('imageViewerAigc')) _$('imageViewerAigc').style.display='';
	},

	/**
	 * 取消编辑
	 */
	trimCancel:function(){
		this.trimming=false;

		_$('imageViewerTrimBox').style.visibility='hidden';
		_$('imageViewerTrimDone').style.display='none';
		_$('imageViewerTrimWHRatio').style.display='none';
		_$('imageViewerTrimRotate').style.display='none';
		_$('imageViewerTrimFinish').style.display='none';
		_$('imageViewerTrimOriginal').style.display='none';
		if(_$('imageViewerTrimZoomIn')) _$('imageViewerTrimZoomIn').style.display='none';
		if(_$('imageViewerTrimZoomOut')) _$('imageViewerTrimZoomOut').style.display='none';
		_$('imageViewerTrimStart').style.display='';
	},

	/**
	 * 剪裁框放大缩小
	 */
	trimBoxZoom:function (movement){
		let cimg=_$('imageViewerImage_'+this.current+'_img');
		if(!cimg) return;

		if(this.trimming){
			this.initWidth=W.elementWidth(_$('imageViewerTrimBox'));
			this.initHeight=W.elementHeight(_$('imageViewerTrimBox'));
		}else{
			this.initWidth=cimg.width;
			this.initHeight=cimg.height;
		}

		//X方向缩放
		let width=Math.min(cimg.width-20, this.initWidth+movement);
		if(width<10) width=10;

		//Y方向缩放
		let height=Math.min(cimg.height-20, this.initHeight+movement);
		if(height<10) height=10;

		this.setTrimWHRatio(this.trimWHRatio, width, height);
	},

	/**
	 * 完成编辑
	 */
	trimDone:function(){
		if(!this.trimming){
			this.close();
			return;
		}

		let i=this.current;

		let cimg=_$('imageViewerImage_'+i+'_img');
		let cimgContainer=_$('imageViewerImage_'+i);

		let trimLeftLength=cimgContainer.scrollLeft + W.elementLeft(_$('imageViewerTrimBox'), false) - W.elementLeft(cimg);
		if(trimLeftLength<0) trimLeftLength=0;

		let trimRightLength=cimg.width - trimLeftLength - W.elementWidth(_$('imageViewerTrimBox'));
		if(trimRightLength<0) trimRightLength=0;

		let trimTopLength=cimgContainer.scrollTop + W.elementTop(_$('imageViewerTrimBox'), false) - W.elementTop(cimg); - 54;
		if(trimTopLength<0) trimTopLength=0;

		let trimBottomLength=cimg.height - trimTopLength - W.elementHeight(_$('imageViewerTrimBox'));
		if(trimBottomLength<0) trimBottomLength=0;

		let jmid=this.allImages[i].mediaId;
		let jm=this.getMedia(jmid);
		if(jm){
			jm.trimLeftRatio=trimLeftLength/cimg.width;//左边裁剪比率
			jm.trimRightRatio=trimRightLength/cimg.width;//右边裁剪比率
			jm.trimTopRatio=trimTopLength/cimg.height;//顶部裁剪比率
			jm.trimBottomRatio=trimBottomLength/cimg.height;//底部裁剪比率
			jm.trim(jmid, this.trimWHRatio);

			this.trimShowCanvas();
		}
	},

	/**
	 * 顺时针旋转90度
	 */
	trimRotate:function(){
		let i=this.current;
		let jmid=this.allImages[i].mediaId;
		let jm=this.getMedia(jmid);
		if(jm){
			jm.rotate(jmid,90);
			this.trimShowCanvas();
		}
	},

	/**
	 * 从JMedia获取裁剪后的图片数据再显示
	 */
	trimShowCanvas:function(){
		let i=this.current;
		IMG.reset('imageViewerImage_'+i+'_img');
		Utils.delAtt(_$('imageViewerImage_'+i+'_img'),'src');
		Utils.setAtt(_$('imageViewerImage_'+i+'_img'),'_src',this.getMedia(this.allImages[i].mediaId).getData());

		IMG.adjust('imageViewerImage_'+i+'_img',
			null,
			3,
			this.width,
			this.height,
			true,
			false,
			null,
			null,
			ImageViewer.trimShow,
			true,
			true,
			false);

		this.sizes['imageViewerImage_'+i+'_img']=null;//需要重新获取图片尺寸
	},

	/**
	 * 显示原图
	 */
	trimShowOriginal:function(){
		let i=this.current;

		let jmid=this.allImages[i].mediaId;
		let jm=this.getMedia(jmid);
		if(jm){
			jm.trimLeftRatio=0;
			jm.trimRightRatio=0;
			jm.trimTopRatio=0;
			jm.trimBottomRatio=0;
			jm.rotates=0;
			jm.zoom(jmid);
		}

		IMG.reset('imageViewerImage_'+i+'_img');
		Utils.delAtt(_$('imageViewerImage_'+i+'_img'),'src');
		Utils.setAtt(_$('imageViewerImage_'+i+'_img'),'_src',this.getMedia(this.allImages[i].mediaId).getData());

		IMG.adjust('imageViewerImage_'+i+'_img',
			null,
			this.zoomType,
			this.width,
			this.height,
			true,
			false,
			null,
			null,
			null,
			true,
			true,
			false);

		this.sizes['imageViewerImage_'+i+'_img']=null;//需要重新获取图片尺寸
	},

	/**
	 *
	 * @param event
	 * @param _touch
	 */
	start:function(event, _touch){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.start(event, _touch, true);
			return;
		}

		this.currentObjectId=_touch.obj.id;

		this.initX=_touch.initScreenX;
		this.initY=_touch.initScreenY;
		if(_touch.obj.id=='imageViewerTrimBox'){//操作剪裁框
			let _top=_$('imageViewerTrimBox').style.top;
			_top=Str.replaceAll(_top.toLowerCase(),'px','')*1;
			this.initTop=_top;

			let left=_$('imageViewerTrimBox').style.left;

			left=Str.replaceAll(left.toLowerCase(),'px','')*1;
			this.initLeft=left;

			this.initWidth=W.elementWidth(_$('imageViewerTrimBox'));
			this.initHeight=W.elementHeight(_$('imageViewerTrimBox'));
		}else{
			let cimg=_$('imageViewerImage_'+this.current+'_img');
			let cimgContainer=_$('imageViewerImage_'+this.current);
			if(!cimgContainer) return;

			this.initTop=cimgContainer.scrollTop;
			this.initLeft=cimgContainer.scrollLeft;
			if(cimg){
				this.initWidth=cimg.width;
				this.initHeight=cimg.height;
			}
		}
		this.distanceOfTwoPoint=0;
		this.distanceOfTwoPointX=0;
		this.distanceOfTwoPointY=0;
	},

	/**
	 *
	 * @param event
	 * @param _touch
	 */
	moving:function(event, _touch){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.moving(event, _touch);
			return;
		}

		let cimg=_$('imageViewerImage_'+this.current+'_img');
		if(_touch.obj.id=='imageViewerTrimBox'){//剪裁模式
			if(!this.trim) return;
			if(_touch.distanceOfTwoPoint==0){//一个手指
				//X方向移动距离
				let movement= Math.floor(_touch.screenX-this.initX);
				let left=this.initLeft+movement;

				let minLeft=this.getTrimBoxLeftMin();
				let maxLeft=this.getTrimBoxLeftMax();
				if(left<minLeft){
					left=minLeft;
				}else if(left > maxLeft){
					left=maxLeft;
				}
				_$('imageViewerTrimBox').style.left=left+'px';
				//X方向移动距离 end

				//Y方向移动距离
				movement=Math.floor(_touch.screenY-this.initY);
				let _top=this.initTop+movement;

				let minTop=this.getTrimBoxTopMin();
				let maxTop=this.getTrimBoxTopMax();
				if(_top<minTop){
					_top=minTop;
				}else if(_top>maxTop){
					_top=maxTop;
				}
				_$('imageViewerTrimBox').style.top=_top+'px';
				//Y方向移动距离 end

				this.initX=_touch.initScreenX;
				this.initY=_touch.initScreenY;
			}else{//两个手指
				//X方向缩放
				if(this.distanceOfTwoPointX==0) this.distanceOfTwoPointX=_touch.distanceOfTwoPointX;
				let movement=_touch.distanceOfTwoPointX-this.distanceOfTwoPointX;
				let width= Math.min(cimg ? cimg.width : this.width, this.initWidth + movement);
				if(width<10) width=10;

				//Y方向缩放
				if(this.distanceOfTwoPointY==0) this.distanceOfTwoPointY=_touch.distanceOfTwoPointY;
				movement=_touch.distanceOfTwoPointY-this.distanceOfTwoPointY;
				let height= Math.min(cimg ? cimg.height : this.height, this.initHeight+movement);
				if(height<10) height=10;

				this.setTrimWHRatio(this.trimWHRatio, width, height);
			}
			return;
		}

		if(!cimg) return;
		let cimgContainer=_$('imageViewerImage_'+this.current);

		let maxWidth=this.width;
		let maxHeight=this.height;

		if((cimg.width>maxWidth || cimg.height>maxHeight)
			&& _touch.distanceOfTwoPoint==0){//图片已被放大，且只是一个手指
			let scrollLeft=this.initLeft;
			let scrollTop=this.initTop;

			let scrollLeftMax=cimg.width-maxWidth;//当前图片最大可偏移水平距离
			if(scrollLeftMax<0) scrollLeftMax=0;

			let scrollTopMax=cimg.height-maxHeight;//当前图片最大可偏移垂直距离
			if(scrollTopMax<0) scrollTopMax=0;

			//X方向移动
			let movement=Math.floor(_touch.screenX-this.initX);
			scrollLeft-=movement;
			if(scrollLeft<0) scrollLeft=0;
			if(scrollLeft>scrollLeftMax) scrollLeft=scrollLeftMax;
			cimgContainer.scrollLeft=Math.floor(scrollLeft);

			//Y方向移动
			movement=Math.floor(_touch.screenY-this.initY);
			scrollTop-=movement;
			if(scrollTop<0) scrollTop=0;
			if(scrollTop>scrollTopMax) scrollTop=scrollTopMax;
			cimgContainer.scrollTop=Math.floor(scrollTop);
			return;
		}

		if(_touch.distanceOfTwoPoint>0){//两个手指
			if(this.distanceOfTwoPoint==0) this.distanceOfTwoPoint=_touch.distanceOfTwoPoint;
			let movement=_touch.distanceOfTwoPoint-this.distanceOfTwoPoint;
			this.zoomImage(cimg, cimgContainer, movement);
		}
	},

	/**
	 * 放大缩小图片
	 * @param cimg
	 * @param cimgContainer
	 * @param movement
	 */
	zoomImage:function (cimg, cimgContainer, movement){
		if(!this.sizes[cimg.id]){
			this.sizes[cimg.id]=[cimg.width, cimg.height];
		}

		let maxWidth=this.width;
		let maxHeight=this.height;

		let sizes=this.sizes[cimg.id];
		let ratio= sizes[0]/sizes[1];

		let widthNew=cimg.width;
		let heightNew=cimg.height;
		if(ratio>maxWidth/maxHeight){
			widthNew=(widthNew+movement);
			if(widthNew < maxWidth) widthNew=maxWidth;

			heightNew=Math.floor(widthNew/ratio);
		}else{
			heightNew=(heightNew+movement);
			if(heightNew < maxHeight) heightNew=maxHeight;

			widthNew=Math.floor(heightNew*ratio);
		}

		cimg.width=widthNew;
		cimg.height=heightNew;

		if(cimg.width<=maxWidth) cimgContainer.scrollLeft=0;
		if(cimg.height<=maxHeight) cimgContainer.scrollTop=0;
	},

	/**
	 * 图片缩放
	 * @param amount 绝对值小于表示百分比，大于等于1表示像素数
	 */
	zoomObject:function(amount){
		if(amount==0) return;

		if(this.currentObjectId=='imageViewerTrimBox'){
			let w=W.elementWidth('imageViewerTrimBox');
			let h=W.elementHeight('imageViewerTrimBox');

			if(Math.abs(amount) < 1){
				amount = amount*W.elementWidth('imageViewerTrimBox');//转换为宽度的百分比
			}

			w+=amount;
			h=w/this.trimWHRatio;

			this.setTrimWHRatio(this.trimWHRatio, w, h, true);
			return;
		}

		let cimg=_$('imageViewerImage_'+this.current+'_img');
		if(!cimg) return;
		let cimgContainer=_$('imageViewerImage_'+this.current);

		if(Math.abs(amount) < 1){
			amount = amount*cimg.width;//转换为宽度的百分比
		}
		this.zoomImage(cimg, cimgContainer, amount);
	},

	/**
	 * 左翻
	 * @param event
	 * @param _touch
	 */
	left:function(event,_touch){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.left(event, _touch);
			return;
		}

		//处于剪裁模式，不对图片进行缩放、移动
		if(this.trimming) return;

		let cimg=_$('imageViewerImage_'+this.current+'_img');
		if(cimg){
			let maxWidth=this.width;
			let maxHeight=this.height;
			if(cimg.width>maxWidth+10 || cimg.height>maxHeight+10){//图片已被放大
				return;
			}
		}else{
			this.reset();
		}
		let i=this.current+1;
		if(i>this.allImages.length-1) i=0;
		this.current=i;

		this.show();
	},

	/**
	 * 右翻
	 * @param event
	 * @param _touch
	 */
	right:function(event,_touch){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.right(event, _touch);
			return;
		}

		//处于剪裁模式，不对图片进行缩放、移动
		if(this.trimming) return;

		let cimg=_$('imageViewerImage_'+this.current+'_img');
		if(cimg){
			let maxWidth=this.width;
			let maxHeight=this.height;
			if(cimg.width>maxWidth+10 || cimg.height>maxHeight+10){//图片已被放大
				return;
			}
		}else{
			this.reset();
		}

		let i=this.current-1;
		if(i<0) i=this.allImages.length-1;
		this.current=i;

		this.show();
	},

	/**
	 * 点击
	 * @param event
	 * @param _touch
	 */
	click:function(event,_touch){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.click(event, _touch);
			return;
		}

		//处于剪裁模式，不对图片进行缩放、移动
		if(this.trimming) return;

		let cimg=_$('imageViewerImage_'+this.current+'_img');
		if(!cimg) return;

		let maxWidth=this.width;
		let maxHeight=this.height;
		if(cimg.width<=maxWidth && cimg.height<=maxHeight){//未放大
			let media = this.allImages[this.current];
			if(media.data && media.data.link){
				let _link = media.data.link;
				let _linkOpenType = media.data.linkOpenType;
				if(!_linkOpenType) _linkOpenType='newLayer';

				if(_linkOpenType==='inLayer'){
					ImageViewer.layer.load(window, '', _link);
				}else if(_linkOpenType==='newLayer'){
					Layers.open(window, '', _link);
				}else if(_linkOpenType==='newWindow'){
					window.open(_link);
				}else if(_linkOpenType==='topWindow'){
					top.location.href=_link;
				}else{
					location.href=_link;
				}
			}
		}

		let n=(new Date()).getTime();
		if(n-this.clickTime<500 && this.sizes[cimg.id]){//双击重置
			this.reset();
		}
		this.clickTime=n;
	},

	up:function(event,_touch){},

	down:function(event,_touch){},

	zoomIn:function(event,_touch){

	},

	zoomOut:function(event,_touch){},

	longPress:function(event,_touch){},

	onAIGC:function(imageUrl){
		if(!ImageViewer.container) return;//非打开状态
		if(!this.container){//回调方式无法直接通过this引用ImageViewer对象
			ImageViewer.onAIGC(imageUrl);
			return;
		}

		top.ImageViewer.trimPhotoChange(imageUrl);
	}
}
window.ImageViewer=ImageViewer;

//AIGC
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

//轮播组件
function Rotation(id,width,height,photos,mediaTypes,mediaSizes,medias,links,direction,speed,showNumbers,openInImageViewer){
	this.id=id;//id
	this.width=width;//宽度
	this.height=height;//高度
	this.photos=photos;//图片（或视频的封面）
	this.mediaTypes=mediaTypes;//媒体类型（photo,video）
	this.mediaSizes=mediaSizes;//媒体宽和高[宽,高]
	this.medias=medias;//媒体（未指定取photos值）
	this.links=links;//点击打开的链接（或动作，如 javascript:alert(some message)）
	this.direction=direction;//图片滚动方向（L,T 表示左移、上移）
	this.speed=speed;//滚动间隔（单位：毫秒）
	this.showNumbers=(showNumbers?showNumbers:false);//是否显示编号
	this.numbersOffSet=-30;//编号显示位置便宜量

	this.sliderIndex=0;//当前显示媒体下标
	this.sliderTimeout=null;//计时器
	this.sliderInterval=null;//计时器
	this.sliderLength=0;//累计滚动长度

	this.paused=(photos.length<2);//是否暂停
	this.inPlays=[];//各媒体是否处于播放状态
	this.zoomType=4;//1,按宽  2，按高  3，按较长一边  4，正好满屏，较长（相对于容器长宽比例而言）一边超出容器部分隐藏
	this.currentMediaType=null;//当前显示媒体类型（视频/图片）
	this.openInImageViewer=(typeof openInImageViewer)=='undefined'?true:openInImageViewer;//点击时是否通过图片查看组件查看图片

	Rotations.instances[id]=this;
}

/**
 * 计算第i个媒体的显示尺寸
 * @param i
 * @returns {[*, *]}
 */
Rotation.prototype.mediaDisplaySize=function(i){
	let w=0;
	let h=0;
	let ratio=this.width/this.height;
	let ratioMedia=this.mediaSizes[i][0]/this.mediaSizes[i][1];
	if(ratio>=ratioMedia){
		h=this.height;
		w=Math.floor(h*ratioMedia);
	}else{
		w=this.width;
		h=Math.floor(w/ratioMedia);
	}

	return [w,h];
}

/**
 * 当前显示媒体类型的媒体个数
 * @returns {number|*}
 */
Rotation.prototype.countOfMediaType=function(type){
	if(!type) type=this.currentMediaType;
	if(!type) return this.photos.length;

	let count=0;
	for(let i=0;i<this.mediaTypes.length;i++){
		if(this.mediaTypes[i]==type) count++;
	}
	return count;
}

/**
 * 所有媒体中是否同时有视频和图片
 * @returns {boolean}
 */
Rotation.prototype.bothVideoAndPhoto=function(){
	let hasVideo=false;
	let hasPhoto=false;
	for(let i=0; i<this.mediaTypes.length; i++){
		if(this.mediaTypes[i]=='video') hasVideo=true;
		if(this.mediaTypes[i]=='photo') hasPhoto=true;
	}
	return hasVideo && hasPhoto;
}

/**
 * 在ImageViewer中打开
 */
Rotation.prototype.openImageViewer=function(){
	let list=[];
	for(let i=0; i<this.medias.length; i++){
		list.push(new ImageViewer.Media('', this.medias[i], this.photos[i]));
	}
	ImageViewer.open(window, list, this.photos[this.sliderIndex]);
}

/**
 * 初始化组件
 * @param containerId 父容器ID
 */
Rotation.prototype.init=function(containerId){
	for(let i=0;i<this.photos.length;i++) this.inPlays[i]=this.paused;

	let htm=[];
	htm.push('<div id="'+this.id+'_container" style="width:'+this.width+'px; height:'+this.height+'px !important; overflow:hidden !important;">');

	if(this.direction=='L'){
		htm.push('<div id="'+this.id+'" style="width:'+this.width*(this.photos.length+1)+'px; height:'+this.height+'px; overflow:hidden !important;">');
	}else{
		htm.push('<div id="'+this.id+'" style="width:'+this.width+'px; height:'+this.height*(this.photos.length+1)+'px; overflow:hidden !important;">');
	}

	for(let i=0; i<this.photos.length; i++){
		htm.push('<div class="rotationMedia" id="'+this.id+'_box_'+i+'" style="width:'+this.width+'px; height:'+this.height+'px; text-align:center; overflow:hidden !important;'+(this.direction=='L'?' float:left;':'')+'">');

		if(this.mediaTypes[i]=='video'){
			htm.push('<video id="'+this.id+'_box_'+i+'_player" width="'+this.width+'" height="'+this.height+'" preload="auto" playsinline webkit-playsinline></video>');
			this.inPlays[i]=false;
			Players.reset(this.id+'_box_'+i+'_player');
		}else{
			let hasLink=false;//是否需要打开链接
			if(this.links[i].indexOf('javascript')==0){
				hasLink=true;
				htm.push('<a href="javascript:_void();" onclick="event.cancelBubble=true; '+this.links[i]+'">');
			}else if(this.links[i]!=''){
				hasLink=true;
				htm.push('<a href="javascript:_void();" onclick="event.cancelBubble=true; Rotations.openUrl(\''+this.links[i]+'\');">');
			}

			if(hasLink){
				htm.push('	<img id="'+this.id+'_img_'+i+'" _src="'+this.photos[i]+'" style="display:none;"/>');
			}else{
				if(this.openInImageViewer) htm.push('	<img id="'+this.id+'_img_'+i+'" _src="'+this.photos[i]+'" style="display:none;" onclick="event.cancelBubble=true; Rotations.instances[\''+this.id+'\'].openImageViewer();"/>');
				else htm.push('	<img id="'+this.id+'_img_'+i+'" _src="'+this.photos[i]+'" style="display:none;" onclick="event.cancelBubble=true;"/>');
			}

			if(hasLink) htm.push('</a>');
		}

		htm.push('</div>');
	}

	//最后添加一个空白框，是的最后一张可以继续滚动效果
	htm.push('<div class="rotationMedia" mediaType="photo" id="'+this.id+'_box_'+this.photos.length+'" style="width:'+this.width+'px; height:'+this.height+'px; text-align:center; overflow:hidden !important;'+(this.direction=='L'?' float:left;':'')+'">');
	htm.push('	<img id="'+this.id+'_img_'+this.photos.length+'" _src="/framework/img/blank.png" style="display:none;"/>');
	htm.push('</div>');
	//最后添加一个空白框，是的最后一张可以继续滚动效果 end

	htm.push('</div>');

	htm.push('</div>');

	//媒体编号
	if(this.showNumbers || (this.bothVideoAndPhoto() && UserAgent.isMobile())){
		htm.push('<div class="rotationNumbers" style="top:'+this.numbersOffSet+'px !important;" id="'+this.id+'_rotationNumbers">');

		if(this.bothVideoAndPhoto() && UserAgent.isMobile()){
			htm.push('<div class="rotationSwitches" id="'+this.id+'_rotationSwitch">');
			htm.push('	<div class="rotationSwitch" style="border-left:none !important;" id="'+this.id+'_switch_video" onclick="event.cancelBubble=true; Rotations.switchType(\''+this.id+'\',\'video\');">I{视频}</div>');
			htm.push('	<div class="rotationSwitch" id="'+this.id+'_switch_photo" onclick="event.cancelBubble=true; Rotations.switchType(\''+this.id+'\',\'photo\');">I{图片}</div>');
			htm.push('</div>');
		}

		if(this.showNumbers){
			if(UserAgent.isMobile()){
				htm.push('	<div id="'+this.id+'_rotationNumbersCount" class="rotationNumbersCount">1/'+this.photos.length+'</div>');
			}else{
				for(let i=0;i<this.photos.length;i++){
					htm.push('<div class="'+(i==0?'rotationNumberCurrent':'rotationNumber')+'" id="'+this.id+'_num_'+i+'" onclick="event.cancelBubble=true; Rotations.show(\''+this.id+'\','+i+');"></div>');
				}
			}
		}

		htm.push('</div>');
	}
	//媒体编号 end

	_$(containerId).innerHTML=Lang.convert(htm.join(''));
	htm=null;
	delete htm;

	if(this.photos.length==0) return;

	for(let i=0;i<this.photos.length;i++){
		if(this.mediaTypes[i]=='video') continue;
		try{
			IMG.reset(this.id+'_img_'+i);

			IMG.adjust(this.id+'_img_'+i,
				this.id+'_img_'+i+'_rotation',
				this.zoomType,
				this.width,
				this.height,
				true,
				false,
				null,
				null,
				null,
				true,
				true);
		}catch(e){}
	}

	if(UserAgent.isMobile()){
		let touch = new Touch(_$(this.id), 20, null, null, null, null, this.showNext, this.showPrevious, null, null);
		touch.preventDefaultOnClick = false;
		touch.callbackCaller=this;
	}

	this.show(0);
}

/**
 * 找到当前指定类别下显示的第i个媒体，在初始的全部媒体列表中是第几个
 * @param i
 */
Rotation.prototype.find=function(i){
	let count=0;
	for(let index=0; index<this.photos.length; index++){
		if(this.currentMediaType && this.currentMediaType!=this.mediaTypes[index]) continue;
		if(count==i) return index;
		count++;
	}
}

/**
 * 当前图片（视频返回封面）
 */
Rotation.prototype.getCurrentPhoto=function(){
	let count=0;
	let index=0;
	for(index=0; index<this.photos.length; index++){
		if(this.currentMediaType && this.currentMediaType!=this.mediaTypes[index]) continue;
		if(count==this.sliderIndex) break;
		count++;
	}
	let size=this.mediaSizes[index];
	return [this.photos[index], size[0], size[1]];
}

/**
 * 当前媒体
 */
Rotation.prototype.getCurrentMedia=function(){
	let count=0;
	for(let index=0; index<this.photos.length; index++){
		if(this.currentMediaType && this.currentMediaType!=this.mediaTypes[index]) continue;
		if(count==this.sliderIndex) return Str.isBlank(this.medias[index]) ? this.photos[index] : this.medias[index];
		count++;
	}
	return Str.isBlank(this.medias[0]) ? this.photos[0] : this.medias[0];
}

/**
 * 显示第i个媒体
 * @param i
 */
Rotation.prototype.show=function(i){
	if(this.sliderInterval) clearInterval(this.sliderInterval);
	if(this.sliderTimeout) clearTimeout(this.sliderTimeout);

	this.sliderLength=0;
	this.sliderIndex=i;
	if(this.direction=='L'){
		_$(this.id+'_container').scrollLeft=(this.sliderIndex*this.width+this.sliderLength);
	}else{
		_$(this.id+'_container').scrollTop=(this.sliderIndex*this.height+this.sliderLength);
	}

	//原始列表中的位置
	i=this.find(i);

	//如果是视频
	if(this.mediaTypes[i]=='video'){
		let playerInstance=Players.getPlayer(this.id+'_box_'+i+'_player');
		if(!playerInstance){
			let size=this.mediaDisplaySize(i);
			Players.addPlayer(this.id+'_box_'+i+'_player',
				this.medias[i],
				false,
				size[0],
				size[1],
				false,
				false,
				true,
				this.photos[i],
				false,
				'W');
			Players.setMaxWidth(this.id+'_box_'+i+'_player', this.width);
			Players.setMaxHeight(this.id+'_box_'+i+'_player', this.height);
			Players.initPlayers();
		}else{
			Players.play(this.id+'_box_'+i+'_player');
		}
	}

	//停止播放非当前显示的视频
	for(let j=0;j<this.photos.length;j++){
		if(this.mediaTypes[j]=='video' && j!=i){
			Players.stop(this.id+'_box_'+j+'_player');
		}
	}

	//切换显示的媒体编号
	for(let n=0;n<this.photos.length;n++){
		if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
	}
	if(_$(this.id+'_num_'+i)) _$(this.id+'_num_'+i).className='rotationNumberCurrent';
	if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

	//非暂停状态下，定时显示下一个媒体
	if(!this.paused){
		this.sliderTimeout=setTimeout("Rotations.doSlider('"+this.id+"')",this.speed);
	}
}

/**
 * 显示下一个
 */
Rotation.prototype.showNext=function(event, by){
	let i=this.sliderIndex+1;
	if(i==this.countOfMediaType()){//开始新的循环
		i=0;
	}
	this.show(i);

	if(by) this.pause();
}

/**
 * 显示前一个
 */
Rotation.prototype.showPrevious=function(event, by){
	let i=this.sliderIndex-1;
	if(i<0){//开始新的循环
		i=this.countOfMediaType()-1;
	}
	this.show(i);

	if(by) this.pause();
}

/**
 * 当前显示的（或指定）媒体是否为视频
 * @param index
 * @returns {boolean}
 */
Rotation.prototype.videoShown=function(index){
	if(this.currentMediaType==null){
		if((typeof index) != 'undefined') return this.mediaTypes[index]=='video';
		else return this.mediaTypes[this.sliderIndex]=='video';
	}else if(this.currentMediaType=='video'){
		return true;
	}else{
		return false;
	}
}

/**
 * 重置轮播组件为初始状态
 */
Rotation.prototype.reset=function(){
	if(this.sliderInterval) clearInterval(this.sliderInterval);
	if(this.sliderTimeout) clearTimeout(this.sliderTimeout);
	this.sliderLength=0;
	this.sliderIndex=0;

	_$(this.id+'_container').scrollLeft=0;
	_$(this.id+'_container').scrollTop=0;

	for(let n=0;n<this.photos.length;n++){
		if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
	}
	if(_$(this.id+'_num_'+this.sliderIndex)){
		_$(this.id+'_num_'+this.sliderIndex).className='rotationNumberCurrent';
	}
	if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

	for(let i=0;i<this.photos.length;i++){
		if(this.mediaTypes[i]=='video'){
			Players.stop(this.id+'_box_'+i+'_player');
		}
	}

	this.show(0)
}

/**
 * 暂停
 */
Rotation.prototype.pause=function(){
	this.paused=true;

	if(this.sliderInterval) clearInterval(this.sliderInterval);
	if(this.sliderTimeout) clearTimeout(this.sliderTimeout);
}

/**
 * 滚动
 */
Rotation.prototype.slider=function(){
	this.sliderLength+=20;//每次滚动20px

	if(this.direction=='L'){
		if(this.sliderLength>this.width) this.sliderLength=this.width;

		_$(this.id+'_container').scrollLeft=(this.sliderIndex*this.width+this.sliderLength);

		if(this.sliderLength>=this.width){
			if(this.sliderInterval) clearInterval(this.sliderInterval);
			if(this.sliderTimeout) clearTimeout(this.sliderTimeout);

			this.sliderLength=0;
			this.sliderIndex++;

			if(this.sliderIndex==this.countOfMediaType()){//开始新的循环
				this.reset();
				return;
			}

			for(let n=0;n<this.photos.length;n++){
				if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
			}
			if(_$(this.id+'_num_'+this.sliderIndex)){
				_$(this.id+'_num_'+this.sliderIndex).className='rotationNumberCurrent';
			}
			if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

			//如果前一个媒体是视频，停止其播放
			if(this.sliderIndex>0 && this.videoShown(this.sliderIndex-1)){
				Players.stop(this.id+'_box_'+(this.sliderIndex-1)+'.player');
			}

			//定时显示下一个
			this.sliderTimeout=setTimeout("Rotations.doSlider('"+this.id+"')",this.speed);
		}
	}else{
		if(this.sliderLength>this.height) this.sliderLength=this.height;

		_$(this.id+'_container').scrollTop=(this.sliderIndex*this.height+this.sliderLength);

		if(this.sliderLength>=this.height){
			if(this.sliderInterval) clearInterval(this.sliderInterval);
			if(this.sliderTimeout) clearTimeout(this.sliderTimeout);

			this.sliderLength=0;
			this.sliderIndex++;

			if(this.sliderIndex==this.countOfMediaType()){//开始新的循环
				this.reset();
				return;
			}

			for(let n=0;n<this.photos.length;n++){
				if(_$(this.id+'_num_'+n)) _$(this.id+'_num_'+n).className='rotationNumber';
			}
			if(_$(this.id+'_num_'+this.sliderIndex)){
				_$(this.id+'_num_'+this.sliderIndex).className='rotationNumberCurrent';
			}
			if(_$(this.id+'_rotationNumbersCount')) _$(this.id+'_rotationNumbersCount').innerHTML=(this.sliderIndex+1)+'/'+this.countOfMediaType();

			//如果前一个媒体是视频，停止其播放
			if(this.sliderIndex>0 && this.videoShown(this.sliderIndex-1)){
				Players.stop(this.id+'_box_'+(this.sliderIndex-1)+'.player');
			}

			//定时显示下一个
			this.sliderTimeout=setTimeout("Rotations.doSlider('"+this.id+"')",this.speed);
		}
	}
}

Rotation.prototype.doSlider=function(){
	//暂停或当前显示的视频，则不自动滚动
	if(this.paused || this.videoShown()) return;

	//每10毫秒滚动
	this.sliderInterval=setInterval("Rotations.slider('"+this.id+"')",10);
}

/**
 * 管理所有轮播组件
 * @type {{openUrl: Rotations.openUrl, slider: Rotations.slider, current: null, doSlider: Rotations.doSlider, instances: any[], show: Rotations.show, switchType: Rotations.switchType, currentIndex: number}}
 */
let Rotations={
	instances:new Array(),

	//当前正在操作的实例
	current:null,
	currentIndex:0,

	/**
	 * 打开url
	 * @param url
	 */
	openUrl:function(url){
		if(url.indexOf('/shopping/shop.htm')<0
			&& url.indexOf('/shopping/zone.htm')<0
			&& url.indexOf('/live/player.htm')<0
			&& url.indexOf('/user/index.htm')<0){
			Layers.load(window,'',url,null, '', 0, null);
		}else{
			location.href=url;
		}
	},

	/**
	 * 显示指定实例的第i个媒体
	 * @param id
	 * @param i
	 */
	show:function(id,i){
		let rotation=this.instances[id];
		rotation.paused=rotation.inPlays[i];
		rotation.show(i);
	},

	/**
	 * 滚动指定实例
	 * @param id
	 */
	slider:function(id){
		let rotation=this.instances[id];
		rotation.slider();
	},

	/**
	 * 滚动指定实例到下一个
	 * @param id
	 */
	doSlider:function(id){
		let rotation=this.instances[id];
		rotation.doSlider();
	},

	/**
	 * 切换指定实例的媒体显示类型
	 * @param id
	 * @param type
	 * @private
	 */
	switchType:function(id, type){
		let rotation=this.instances[id];
		rotation.paused=(type=='video');

		for(let i=0;i<rotation.photos.length;i++){
			if(rotation.mediaTypes[i]!=type){
				_$(id+'_box_'+i).style.display='none';
			}else{
				_$(id+'_box_'+i).style.display='';
			}
		}

		if(rotation.direction=='L'){
			_$(rotation.id).style.width=rotation.width*(rotation.countOfMediaType(type)+1)+'px';
		}else{
			_$(rotation.id).style.height=rotation.height*(rotation.countOfMediaType(type)+1)+'px';
		}

		let temp=_$cls('rotationSwitchCurrent');
		for(let i=0; temp && i<temp.length; i++) temp[i].className='rotationSwitch';
		_$(rotation.id+'_switch_'+type).className='rotationSwitchCurrent';

		rotation.currentMediaType=type;

		if(_$(id+'_rotationNumbersCount')) _$(id+'_rotationNumbersCount').innerHTML='1/'+rotation.countOfMediaType();

		rotation.reset();
	}
}
window.Rotations=Rotations;

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

//媒体处理器/////////
let JMedias=[];
let JMediaUtil={
	/**
	 * 清空媒体对象
	 * @param name 指定对象
	 */
	clear:function(name){
		for(let i in JMedias){
			if(name && name!=i) continue;
			if(JMedias[i]){
				JMedias[i].clear();
				JMedias[i]=null;
			}
		}
		if(!name) JMedias=[];
	},

	/**
	 * 播放声音
	 * @param id
	 * @param mediaUrl
	 */
	playSound:function(id, mediaUrl){
		if(!_$(id)) {
			document.body.insertAdjacentHTML('afterbegin', '<audio id="'+id+'"><source src="' + mediaUrl + '" type="audio/mpeg"/></audio>');
			if(_$(id).load) _$(id).load();
		}
		_$(id).play();
	},

	/**
	 * 停止播放
	 * @param id
	 */
	stopSound:function(id){
		if(_$(id)) _$(id).stop();
	},

	getAudioType:function(fileName){
		if(fileName.indexOf('?')>0) fileName=fileName.substring(0, fileName.indexOf('?'));
		if(Str.endsWith(fileName, ".ogg")) return "audio/ogg";
		else if(Str.endsWith(fileName, ".wav")) return "audio/wav";
		else return "audio/mpeg";
	}
}
window.JMediaUtil=JMediaUtil;
window.JMedias=JMedias;

/**
 * 媒体对象
 * @param fileOrUrl 媒体文件路径/url
 * @param container 容器
 * @param maxLength 最大长度
 * @param quality 品质（0~1）
 * @param callback 回调方法
 * @returns {*}
 * @constructor
 */
function JMedia(fileOrUrl, container, maxLength, quality, callback, callbackTarget){
	let _id='';
	if((typeof fileOrUrl)=='string'){
		_id=('JM'+Math.random());
	}else{
		_id=fileOrUrl.name;
	}
	this.id=_id;
	JMedias[this.id]=this;//保存实例
	if(top != window && (typeof top.JMedias)!='undefined'){
		top.JMedias[this.id]=this;
	}
	this.dataOriginal=null;//原始数据
	this.data=null;//数据
	this.dataSize=0;//大小
	this.dataSizeOriginal=0;//原始大小
	this.widthOriginal=0;//原始宽度
	this.heightOriginal=0;//原始高度
	this.maxLength=maxLength;//最大长度
	this.quality=quality;//品质
	this.callback=callback;//回调
	this.callbackTarget=callbackTarget;//调用回调函数的对象
	this.fileOrUrl=fileOrUrl;//媒体文件路径/url
	this.container=((typeof container)=='string'?_$(container):container);//容器
	this.reader=new FileReader();//文件读取器
	this.img=new Image();//图像
	this.img.crossOrigin='anonymous';
	this.imgTrim=new Image();//剪裁过的图像
	this.imgTrim.crossOrigin='anonymous';
	this.img.onload = function(e){//加载完成后，如果图片长或宽超出maxLength，则自动进行缩放
		JMedias[_id].zoom(_id);
	};
	this.reader.onload = function(e){//读取完成后给图像赋值
		JMedias[_id].img.src = e.target.result;
	};
	if((typeof fileOrUrl)=='string'){
		fileOrUrl=fileOrUrl.toLowerCase();
		if(fileOrUrl.endsWith('.png')){
			this.mimeType='image/png';
		}else{
			this.mimeType='image/jpeg';
		}
	}else{
		this.mimeType=(fileOrUrl.type||'image/png');
	}

	this.trimLeftRatio=0;//左边裁剪比率
	this.trimRightRatio=0;//右边裁剪比率
	this.trimTopRatio=0;//顶部裁剪比率
	this.trimBottomRatio=0;//底部裁剪比率

	//缩放图片需要的canvas
	this.canvas = document.createElement('canvas');
	this.context = this.canvas.getContext('2d');
	if(this.container) this.container.appendChild(this.canvas);

	//开始处理
	this.start();

	return this.id;
}
JMedia.prototype.clear=function(){
	this.img=null;
	this.imgTrim=null;
	this.reader=null;
	if(this.container && this.canvas){
		try{
			this.container.removeChild(this.canvas);
		}catch (e){}
	}
	this.context=null
	this.canvas=null;
	this.data=null;
	this.dataOriginal=null;
}
JMedia.prototype.start=function(){
	if((typeof this.fileOrUrl)=='string'){
		this.img.src = this.fileOrUrl;
	}else{
		this.reader.readAsDataURL(this.fileOrUrl);
	}
}

//压缩
JMedia.prototype.zoom=function(id){
	this.widthOriginal=this.img.width;
	this.heightOriginal=this.img.height;
	let ratio=1;//缩放比率
	if(this.widthOriginal<=this.maxLength
		&& this.heightOriginal<=this.maxLength){
		//不需要压缩
		this.quality=1;
	}else{
		if(this.widthOriginal > this.heightOriginal) ratio=this.maxLength/this.widthOriginal;
		else ratio=this.maxLength/this.heightOriginal;
	}

	let width=Math.floor(this.widthOriginal*ratio);
	let height=Math.floor(this.heightOriginal*ratio);

	//canvas对图片进行缩放
	let dx=0;
	let dy=0;
	this.canvas.width = width;
	this.canvas.height = height;

	//清除画布
	if(width>height) this.context.clearRect(0, 0, width, width);
	else this.context.clearRect(0, 0, height, height);

	//图片压缩
	this.context.restore();//恢复状态
	this.context.drawImage(this.img, dx, dy, width, height);
	this.context.save();

	//输出临时图像到img
	this.imgTrim.src=this.getData();

	//canvas转为blob
	this.canvas.toBlob(function (blob) {
		JMedias[id].data=blob;
		JMedias[id].dataSize=blob.size;
		JMedias[id].dataSizeOriginal=blob.size;
		if(!JMedias[id].dataOriginal) JMedias[id].dataOriginal=blob;
		if(JMedias[id].callback){
			if(JMedias[id].callbackTarget) JMedias[id].callback.call(JMedias[id].callbackTarget, JMedias[id].img, JMedias[id].imgTrim, id);
			JMedias[id].callback.call(window, JMedias[id].img, JMedias[id].imgTrim, id);
		}
	}, JMedias[id].mimeType);
}

//剪裁
JMedia.prototype.trim=function(id, WHRatio){
	this.quality=1;//不再压缩

	let oldWidth = this.canvas.width;
	let oldHeight = this.canvas.height;

	//左上角坐标
	let startX=Math.floor(oldWidth*this.trimLeftRatio);
	let startY=Math.floor(oldHeight*this.trimTopRatio);

	//截取后宽、高
	let width=Math.floor(oldWidth*(1-this.trimLeftRatio-this.trimRightRatio));
	let height=WHRatio ? (width/WHRatio): Math.floor(oldHeight*(1-this.trimTopRatio-this.trimBottomRatio));

	let dx=0;
	let dy=0;

	this.canvas.width = width;
	this.canvas.height = height;

	//清除画布
	if(width>height) this.context.clearRect(0, 0, width, width);
	else this.context.clearRect(0, 0, height, height);

	//图片压缩
	this.context.drawImage(this.imgTrim, startX,startY, width,height,dx, dy, width, height);
	this.context.restore();

	//输出临时图像到img
	this.imgTrim.src=this.getData();

	//canvas转为blob
	this.canvas.toBlob(function (blob) {
		JMedias[id].data=blob;
		JMedias[id].dataSize=blob.size;
		if(!JMedias[id].dataOriginal) JMedias[id].dataOriginal=blob;
	}, JMedias[id].mimeType);
}

//旋转
JMedia.prototype.rotate=function(id, degrees) {
	let width=this.imgTrim.width;
	let height=this.imgTrim.height;

	// 清除画布
	if(width>height) this.context.clearRect(0, 0, width, width);
	else this.context.clearRect(0, 0, height, height);

	this.context.save();//保存状态

	this.canvas.width=height;
	this.canvas.height=width;
	Utils.setAtt(this.canvas,'width',height);
	Utils.setAtt(this.canvas,'height',width);

	this.context.translate(height,0);
	this.context.rotate(degrees*Math.PI/180);

	this.context.drawImage(this.imgTrim, 0,0, width, height);

	this.context.restore();//恢复状态

	//输出临时图像到img
	this.imgTrim.src=this.getData();

	// canvas转为blob
	this.canvas.toBlob(function (blob) {
		JMedias[id].data=blob;
		JMedias[id].dataSize=blob.size;
		JMedias[id].dataSizeOriginal=blob.size;
		if(!JMedias[id].dataOriginal) JMedias[id].dataOriginal=blob;
		if(JMedias[id].callback){
			if(JMedias[id].callbackTarget) JMedias[id].callback.call(JMedias[id].callbackTarget, JMedias[id].img, JMedias[id].imgTrim, id);
			JMedias[id].callback.call(window, JMedias[id].img, JMedias[id].imgTrim, id);
		}
	}, JMedias[id].mimeType);
}
JMedia.prototype.getData=function(){
	//type，设置输出的类型，比如 image/png image/jpeg等
	//encoderOptions： 0-1之间的数字，用于标识输出图片的质量，1表示无损压缩，类型为： image/jpeg 或者image/webp才起作用。
	return this.canvas.toDataURL(this.mimeType, this.quality);
}

let MediaManager={
	callback: null,
	callbackTarget: null,
	multi: true,
	inLayer: null,

	open:function (url, _multi, _callback, _callbackTarget){
		this.multi = (typeof _multi) == 'boolean' ? _multi : true;
		this.callback = (_callback ? _callback : null);
		this.callbackTarget = (_callbackTarget ? _callbackTarget : window);

		url+=(url.indexOf('?') > 0 ? '&' : '?')+'multi='+this.multi;
		url+='&selecting=true';

		this.inLayer = Layers.open(window, 'I{.媒体库}', url, '', null, 0, null);
	},

	done:function (selectedMedias){
		if(!this.multi && selectedMedias && selectedMedias.length>1){
			Page.alert('I{.只能选择一个媒体}');
			return;
		}

		if(this.callback){
			this.callback.call(this.callbackTarget, selectedMedias);
		}

		this.inLayer.close();
	}
}
//媒体处理器 end/////

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

//Ajax/////
let AjaxSetting={
	textNotLogin:'',
	textNoPermission:'',
	textOk:'',
	textError:'',
	text404:'',
	textBeginSubmit:'',
	textFileSizeOverflow:'',
	textFileSizeInTotalOverflow:'',
	textBrowserNotSupported:'',
	textCompressing:'',

	onI18NChanged:function() {
		this.textNotLogin=Lang.convert('I{请登录系统}');
		this.textNoPermission=Lang.convert('I{没有操作权限}');
		this.textOk=Lang.convert('I{成功}');
		this.textError=Lang.convert('I{系统错误}');
		this.text404=Lang.convert('I{很不幸，您遇到了传说中的404}');
		this.textBeginSubmit=Lang.convert('I{开始提交}');
		this.textFileSizeOverflow=Lang.convert('I{文件大小超出限制}');
		this.textFileSizeInTotalOverflow=Lang.convert('I{文件总大小超出限制}');
		this.textBrowserNotSupported=Lang.convert('I{您当前浏览器不支持上传剪裁图片}');
		this.textCompressing=Lang.convert('I{图片加载中，请几秒后再试}');
	}
}
window.AjaxSetting=AjaxSetting;

function Ajax(setResult, closeDialogAuto){
	Page.result='';

	this.id='AJAX_'+(Math.random()+'').substring(2);//ID
	this.doNotDestoryAfterFinished=false;
	this.response=null;//ifame或app方式下的返回结果
	this.readyState=null;//ifame或app方式下的状态
	this.status=null;//ifame或app方式下的状态

	this.mediaType='';//告诉APP可上传的文件类型
	this.doNotUploadFile=false;//设置为true则不调起APP上传文件，直接提交form

	this.setResult=setResult?setResult:false;//是否将相应结果赋值给Page.result
	this.closeDialogAuto=closeDialogAuto?closeDialogAuto:false;//是否自动关闭通用对话框（Dialog）

	//XMLHttpRequest
	this.request=null;
	this.requestContentType=null;
	this.requestHeaders=[];

	if(!Str.isBlank(Auth.accessToken)) this.addRequestHeader('accessToken',Auth.accessToken);//accessToken
	if(!Str.isBlank(Lang.currentLang)) this.addRequestHeader('lang',Lang.currentLang);//语种设置
	if(!Str.isBlank(Currency.currentCurrency)) this.addRequestHeader('currency',Currency.currentCurrency);//币种设置
	if(!Str.isBlank(Fields.currentField)) this.addRequestHeader('businessField',Fields.currentField);//产业链设置
	if(!Str.isBlank(Fields.currentRole)) this.addRequestHeader('businessRole',Fields.currentRole);
	if(Cookie.get('UA_ID')) this.addRequestHeader('UA_ID',Cookie.get('UA_ID'));
	if(top.UserAgent && !Str.isBlank(top.UserAgent.spreader)) this.addRequestHeader('spreader',top.UserAgent.spreader);
	this.addRequestHeader('timeZone', TimeZones.getTimeOffset());
	//this.addRequestHeader('REQUEST_BY_AJAX', "true");

	//请求链接
	this.url='';

	//上传文件单个大小和总大小(单位 K)
	this.maxFileSize=0;
	this.maxFileSizeTotal=0;

	//响应回调
	this.callback=null;

	//开始时回调
	this.onStart=null;
	Page.currentAjax=this;
}

Ajax.prototype.getRequestContentType=function(){
	return this.requestContentType;
}

//将contentType设为application/json，服务端即可用getRequestBody得到数据
Ajax.prototype.setRequestContentType=function(contentType){
	this.requestContentType=contentType;
}

Ajax.prototype.addRequestHeader=function(headerName,headerValue){
	this.requestHeaders[headerName]=headerValue;
}

Ajax.prototype.removeRequestHeader=function(headerName){
	this.requestHeaders[headerName]=null;
}

Ajax.prototype.clearRequestHeaders=function(){
	this.requestHeaders=[];
}

Ajax.prototype.getRequestHeaders=function(){
	return this.requestHeaders;
}

//0：请求未初始化（还没有调用 open()）。
//1：请求已经建立，但是还没有发送。
//2：请求已发送，正在处理中（通常现在可以从响应中获取内容头）。
//3：请求在处理中；通常响应中已有部分数据可用了，但是服务器还没有完成响应的生成。
//4：响应已完成；您可以获取并使用服务器的响应了。
Ajax.prototype.getReadyState=function(){
	if(this.readyState) return this.readyState;
	return this.request.readyState;
}

//http相应代码，如404等
Ajax.prototype.getStatus=function(){
	if(this.status) return this.status;
	try{
		return this.request.status;
	}catch(e){}
}

Ajax.prototype.getStatusText=function(){
	if(this.response) return this.response;
	try{
		return this.request.statusText;
	}catch(e){}
}

Ajax.prototype.getResponseText=function(){
	let txt='';
	if(this.response) txt=this.response;
	else txt=this.request.responseText;
	try{
		let resp=JSONUtil.parse(txt);
		if(resp
			&&(typeof resp.success) != 'undefined'
			&&(typeof resp.code) != 'undefined'
			&&(typeof resp.message) != 'undefined'){
			try{
				return Str.intSequence2String(resp.code);
			}catch(e){}
		}else if(txt.indexOf('redirect.submit();')>-1){//需要登录
			return 'non_login';
		}else{
			return txt;
		}
	}catch(e){}
	return txt;
}

Ajax.prototype.getResponseJson=function(){
	let txt='';
	if(this.response) txt=this.response;
	else txt=this.request.responseText;
	try{
		if(txt.indexOf('redirect.submit();')>-1 || txt=='-login'){//需要登录
			return JSONUtil.parse('{"success":false,"code":"non_login","message":"'+AjaxSetting.textNotLogin+'","datas":{}}');
		}

		let resp=JSONUtil.parse(txt);
		if(resp){//返回正常
			if((typeof resp.success) != 'undefined'
				&&(typeof resp.code) != 'undefined'
				&&(typeof resp.message) != 'undefined'){
				if(resp.code) resp.code=Str.intSequence2String(resp.code);

				if(resp.message){
					resp.message=Str.intSequence2String(resp.message)
					if(resp.message.indexOf('I{') > -1) resp.message=Lang.convert(resp.message);
					else resp.message=Lang.convert('I{'+resp.message+'}');
				}
			}
			return resp;
		}else if(txt=='access_denied'){
			return JSONUtil.parse('{"success":false,"code":"access_denied","message":"'+AjaxSetting.textNoPermission+'","datas":{}}');
		}else{//其它错误
			return JSONUtil.parse('{"success":false,"code":"ERR","message":"'+AjaxSetting.textError+'","datas":{}}');
		}
	}catch(e){
		return JSONUtil.parse('{"success":false,"code":"ERR","message":"'+AjaxSetting.textError+'","datas":{}}');
	}
}

Ajax.prototype.getAllHeaders=function(){
	try{
		return this.request.getAllResponseHeaders();
	}catch(e){}
}

Ajax.prototype.getHeader=function(headerName){
	try{
		return this.request.getResponseHeader(headerName);
	}catch(e){}
}

Ajax.prototype.setHeader=function(headerName,headerValue){
	try{
		this.request.setRequestHeader(headerName, headerValue);
	}catch(e){}
}

Ajax.prototype.abort=function(){
	try{
		this.request.abort();
	}catch(e){}

}

//创建XMLHttpRequest
Ajax.prototype.createRequest=function(){
	return new XMLHttpRequest();
}

//创建XMLHttpRequest,发送请求
//post时data的格式为 p1=v1&p2=v2&p3=v3...
Ajax.prototype.send=function(method,callback,url,data){
	method=method.toUpperCase();
	this.callback=callback;
	this.url=url;
	if(this.request!=null){
		try{
			this.request.abort();
		}catch(e){}
	}else{
		this.request=this.createRequest();
	}

	let _ajax=this;
	let _request=this.request;
	_request.onreadystatechange=function() {
		if(_ajax.getReadyState()==4 && _ajax.getStatus()==403){
			Page.alert('I{禁止访问}', null, null, Dialog.MSG_TYPE_ERR);
			return;
		}

		if(_ajax.getReadyState()==4 && _ajax.getStatus()==200){
			//保存客户端标识
			if(_ajax.getHeader('UA_ID')) UserAgent.saveUaId(_ajax.url, _ajax.getHeader('UA_ID'));

			//设置结果
			if(_ajax.setResult) {
				try {
					let resp = _ajax.getResponseJson();
					Page.result = (resp && (typeof resp.code) != 'undefined') ? resp.code : _ajax.getResponseText();
				} catch (e) {}
			}
		}

		callback(_ajax);

		if(_ajax.request && _ajax.getReadyState()==4 && !_ajax.doNotDestoryAfterFinished){
			_ajax.clear();
			_ajax=null;
			delete _ajax;
		}
	};
	_request.open(method,url,true);

	//请求头
	if(Str.isBlank(this.requestContentType) && 'POST'==method){
		if(JSONUtil.isJson(data)) _request.setRequestHeader('Content-Type','application/json');
		else if(data==null||(typeof data)==='string') _request.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	}else if(!Str.isBlank(this.requestContentType)){
		_request.setRequestHeader('Content-Type',this.requestContentType);
	}

	for(let h in this.requestHeaders){
		if(this.requestHeaders[h]){
			_request.setRequestHeader(h, this.requestHeaders[h]);
		}
	}

	_request.setRequestHeader("If-Modified-Since","0");
	//请求头 end

	//开始发送数据回调
	if(this.onStart) this.onStart('', AjaxSetting.textBeginSubmit);

	//发送数据
	_request.send(data);
}

//发送json数据
Ajax.prototype.sendJson=function(callback,url,data){
	this.requestContentType='application/json';
	this.send('POST',callback,url,data);
}

/**
 * 发送form数据
 * @param _form
 * @param callback
 */
Ajax.prototype.sendForm=function(_form, callback, multiparts){
	let es=_form.elements;

	///////////////////////////////组织数据//////////////
	let data=null;

	let multipart=Utils.att(_form, "enctype") && Utils.att(_form, "enctype").indexOf('multipart')>-1;//是否包含文件
	let fileSizeTotal=0;
	for(let i=0;i<es.length;i++){
		if(es[i].type=='file'){
			multipart=true;
			for(let f = 0; f < es[i].files.length; f++){
				if(this.maxFileSize>0 && es[i].files[f].size > this.maxFileSize*1024){
					Page.alert(AjaxSetting.textFileSizeOverflow+' '+MathUtil.size(this.maxFileSize*1024));
					return;
				}
				fileSizeTotal+=es[i].files[f].size;
			}
		}
	}

	if(multiparts && Array.isArray(multiparts)) multipart=true;

	if(this.maxFileSizeTotal>0 && fileSizeTotal>this.maxFileSizeTotal*1024){
		Page.alert(AjaxSetting.textFileSizeInTotalOverflow+' '+MathUtil.size(this.maxFileSizeTotal*1024));
		return;
	}

	Page.currentAjaxForUpload=this;
	if(multipart){
		Logger.log(this.id+" 使用FormData上传数据......");
		data=new FormData();
		for(let i=0;i<es.length;i++){
			if(es[i].type=='file'){
				for(let f = 0; f < es[i].files.length; f++){
					let jm=JMedias[es[i].files[f].name];
					if(jm){//如果存在压缩过的图片
						data.append(es[i].name, jm.data, es[i].files[f].name);
						JMedias[es[i].files[f].name]=null;//重置
					}else{
						data.append(es[i].name, es[i].files[f], es[i].files[f].name);
					}
				}
			}else{
				data.append(es[i].name, es[i].value);
			}
		}

		if(multiparts && Array.isArray(multiparts)){
			for(let f = 0; f < multiparts.length; f++){
				data.append(multiparts[f].name, multiparts[f].data, multiparts[f].fileName);
			}
		}
	}else{
		console.log(this.id+"...普通POST方式提交数据...");
		data='';
		for(let i=0;i<es.length;i++){
			data+='&'+es[i].name+'='+encodeURIComponent(es[i].value);
		}
		if(data.length>0) data=data.substring(1);
		else data==null;
	}
	///////////////////////////////组织数据//////////////

	//POST方式提交
	this.send('POST',callback,_form.action,data);
}

//创建XMLHttpRequest,发送Canvas中数据,jm为JMedia实例
Ajax.prototype.sendCanvas=function(serverUrl,jm,callback){
	if(!jm || !jm.data){
		Toast.show(AjaxSetting.textCompressing);
		return;
	}

	///////////////////////////////组织数据//////////////
	let data=null;
	if((typeof FormData)==='function'){
		Logger.log(this.id+" 使用FormData上传JMedia......");
		data=new FormData();

		let name=jm.fileOrUrl.substring(jm.fileOrUrl.lastIndexOf('/')+1);
		if(name.indexOf('?')>0) name=name.substring(0,name.indexOf('?'));

		data.append('photo', jm.data, name);
	}else{
		Page.alert(AjaxSetting.textBrowserNotSupported);
		return;
	}

	Page.currentAjaxForUpload=this;

	//POST方式提交
	this.send('POST',callback,serverUrl,data);
}

//清除对象
Ajax.prototype.clear=function(){
	try{
		this.abort();
	}catch(e){}
	try{
		this.request=null;
		delete this.request;
	}catch(e){}
}

//通用处理
Ajax.prototype.callbackDefault=function(){
	if(this.getReadyState()==4&&this.getStatus()==200){
		try{
			let resp=this.getResponseJson();
			if(this.setResult) Page.result=resp.code;

			if(resp.code=='1' || resp.success=='true' || resp.success==true){
				Dialog.showAlert(Dialog.MSG_TYPE_OK,
					null,
					null,
					window,
					AjaxSetting.textOk,
					resp.message,
					['<div class="btnH40 btnBgGreen" onclick="Dialog.close();">'+Global.textOk+'</div>']);
				if(this.closeDialogAuto) Dialog.closeDelay(true, 2000);
			}else if(resp.code=='non_login'){
				Auth.showNotLoginMessage();
			}else{
				Dialog.showAlert(Dialog.MSG_TYPE_ERR,
					null,
					null,
					window,
					AjaxSetting.textError,
					resp.message,
					['<div class="btnH40 btnBgRed" onclick="Dialog.close();">'+Global.textOk+'</div>']);
				if(this.closeDialogAuto) Dialog.closeDelay(true, 2000);
			}
		}catch(e){
			Dialog.showAlert(Dialog.MSG_TYPE_ERR,
				null,
				null,
				window,
				AjaxSetting.textError,
				AjaxSetting.textError,
				['<div class="btnH40 btnBgRed" onclick="Dialog.close();">'+Global.textOk+'</div>']);
		}
	}else if(this.getReadyState()==4&&this.getStatus()==404){
		Dialog.showAlert(Dialog.MSG_TYPE_ERR,
			null,
			null,
			window,
			AjaxSetting.textError,
			AjaxSetting.text404,
			['<div class="btnH40 btnBgRed" onclick="Dialog.close();">'+Global.textOk+'</div>']);
	}else if(this.getReadyState()==4&&this.getStatus()==500){
		Dialog.showAlert(Dialog.MSG_TYPE_ERR,
			null,
			null,
			window,
			AjaxSetting.textError,
			AjaxSetting.textError,
			['<div class="btnH40 btnBgRed" onclick="Dialog.close();">'+Global.textOk+'</div>']);
	}
}
function ajaxCallbackDefault(ajax){
	ajax.callbackDefault();
}
function ajaxGetAndroidAppResult(code, message){
	if(code===''){//开始上传
		if(Page.currentAjaxForUpload.onStart) Page.currentAjaxForUpload.onStart('', AjaxSetting.textBeginSubmit);
		return;
	}

	try{
		Page.currentAjaxForUpload.response=message;//ifame或app方式下的返回结果
	}catch(e){
		Page.currentAjaxForUpload.response='{"success":"false","code":"'+code+'","message":"'+JSONUtil.convert(message)+'","datas":{}}';//ifame或app方式下的返回结果
	}
	Page.currentAjaxForUpload.readyState=4;//ifame或app方式下的状态
	Page.currentAjaxForUpload.status=200;//ifame或app方式下的状态
	if(Page.currentAjaxForUpload.callback) Page.currentAjaxForUpload.callback(Page.currentAjaxForUpload);
}
//Ajax end/////

//客户端
//Updated 2022-09-07
let UserAgent={
	//搜索引擎标识
	BOTS:['googlebot', 'baiduspider', '360spider', 'bytespider', 'bingbot', 'sosospider', 'yahoo!slurp', 'inspectiontool'],

	UA_PC:'UA_PC',

	UA_IOS:'UA_IOS',
	UA_IOS_KEYWORDS:['iphone', 'ipod', 'ipad'],

	UA_ANDROID:'UA_ANDROID',
	UA_ANDROID_KEYWORDS:['android'],

	UA_WECHAT:'UA_WECHAT',
	UA_WECHAT_KEYWORDS:['micromessenger'],

	UA_WECHAT_MINI:'UA_WECHAT_MINI',
	UA_WECHAT_MINI_KEYWORDS:['miniprogram'],

	UA_DOUYIN_MINI:'UA_DOUYIN_MINI',
	UA_DOUYIN_MINI_KEYWORDS:['toutiaomicroapp'],

	UA_ALIPAY:'UA_ALIPAY',
	UA_ALIPAY_KEYWORDS:['alipayclient'],

	UA_ALIPAY_MINI:'UA_ALIPAY_MINI',
	UA_ALIPAY_MINI_KEYWORDS:['miniprogram'],

	UA_MOBILE:'UA_MOBILE',
	UA_MOBILE_KEYWORDS:['iphone', 'ipod', 'ipad', 'android', 'mobile', 'blackberry', 'webos', 'incognito', 'webmate', 'bada', 'nokia', 'lg', 'ucweb', 'skyfire'],

	UA_BROWSER:'UA_BROWSER',
	UA_BROWSER_KEYWORDS:['chrome', 'qqbrowser', 'ucbrowser', 'sogou', 'firefox', 'edge', 'safari', 'opera', 'taobrowser', 'lbbrowser', 'maxthon'],

	DOMAIN_UNKNOWN:'DOMAIN_UNKNOWN',
	DOMAIN_WECHAT:'DOMAIN_WECHAT',
	DOMAIN_MOBILE:'DOMAIN_MOBILE',
	DOMAIN_IOS:'DOMAIN_IOS',
	DOMAIN_ANDROID:'DOMAIN_ANDROID',
	DOMAIN_BROWSER:'DOMAIN_BROWSER',
	DOMAIN_PC:'DOMAIN_BROWSER',

	//客户端ID
	uaId:null,

	//主域名
	mainDomain:'',

	//当前url
	currentUrl:location.href,

	//当前域名
	currentDomain:null,

	//当前地址根
	currentUrlBase:null,

	//https/http
	httpScheme:null,

	//当前uri
	currentUri:null,

	//当前操作窗口
	currentWindow:null,

	domainForDevice:[],

	//与服务端时间差（ms）
	timeDiff:0,

	//推荐人
	spreader:null,

	/**
	 *
	 * 是否爬虫
	 * @returns {boolean}
	 */
	isBot:function (){
		let ua=navigator.userAgent;
		if(!ua) return true;
		ua=Str.replaceAll(ua.toLowerCase(), ' ', '');
		return Str.exists(ua, this.BOTS);
	},

	//ip所在国家
	getCountryCode:function (){
		if(top.Auth && top.Auth.profile && top.Auth.profile.country && top.Auth.profile.country.iso_code){
			return top.Auth.profile.country.iso_code;
		}else{
			return 'CN';
		}
	},

	//本地时间对应的服务器时间
	getServerTime:function(localTime){
		return localTime+this.timeDiff;
	},

	getUserAgentType:function() {
		let ua=navigator.userAgent;
		if(!ua) return this.UA_UNKNOWN;

		ua=ua.toLowerCase();
		if(Str.existsIgnoreCase(ua,this.UA_WECHAT_KEYWORDS)){
			return Str.existsIgnoreCase(ua,this.UA_WECHAT_MINI_KEYWORDS)?this.UA_WECHAT_MINI:this.UA_WECHAT;
		}else if(Str.existsIgnoreCase(ua,this.UA_ALIPAY_KEYWORDS)){
			return Str.existsIgnoreCase(ua,this.UA_ALIPAY_MINI_KEYWORDS)?this.UA_ALIPAY_MINI:this.UA_ALIPAY;
		}else if(Str.existsIgnoreCase(ua,this.UA_DOUYIN_MINI_KEYWORDS)){
			return this.UA_DOUYIN_MINI;
		}else if(Str.existsIgnoreCase(ua,this.UA_IOS_KEYWORDS)){
			return this.UA_IOS;
		}else if(Str.existsIgnoreCase(ua,this.UA_ANDROID_KEYWORDS)){
			return this.UA_ANDROID;
		}else if(Str.existsIgnoreCase(ua,this.UA_MOBILE_KEYWORDS)
			&&Str.existsIgnoreCase(ua,this.UA_BROWSER_KEYWORDS)){
			return this.UA_MOBILE;
		}else{
			return this.UA_PC;
		}
	},

	/**
	 * 从user-agent获取微信小程序ID
	 * @returns {string|null}
	 */
	getWechatMiniProgramId:function(){
		let ua=navigator.userAgent;
		if(!ua) return null;
		let infos = ua.split(' ');
		for(let i=0; i<infos.length; i++){
			if(infos[i].startsWith('miniProgram/')) return infos[i].substring('miniProgram/'.length);
		}
		return null;
	},

	getDomainType:function(_domain) {
		if(Str.isBlank(_domain)) _domain=UserAgent.currentDomain;

		_domain=_domain.toLowerCase();
		if(this.domainForDevice[_domain]=='PC' || this.domainForDevice[_domain]=='PCA') return this.DOMAIN_BROWSER;
		else if(this.domainForDevice[_domain]=='M' || this.domainForDevice[_domain]=='MA') return this.DOMAIN_MOBILE;
		else if(_domain.startsWith("w.") || _domain.startsWith("wechat")) return this.DOMAIN_WECHAT;
		else if(_domain.startsWith("ios.") || _domain.startsWith("ios")) return this.DOMAIN_IOS;
		else if(_domain.startsWith("app.") || _domain.startsWith("app")) return this.DOMAIN_ANDROID;
		else if(_domain.startsWith("m.") || _domain.indexOf("mob-")>-1) return this.DOMAIN_MOBILE;
		else return this.DOMAIN_BROWSER;
	},

	isPC:function(){
		return this.getUserAgentType()==this.UA_PC;
	},

	isMobile:function(){
		return !this.isPC();
	},

	getUaId:function (){
		if(!Str.isBlank(this.uaId)) return this.uaId;
		return Cookie.get('UA_ID');
	},

	saveUaId:function (url, uaId){
		let uri=Str.getUri(url);
		if(uri.startsWith('/api/jpay/')) return;
		if(!Str.equals(this.uaId, uaId)) Logger.log('save UA_ID = '+uaId);
		this.uaId=uaId;
		Cookie.set('UA_ID', uaId);
	},

	init:function(){
		this.currentDomain=Str.getHost(this.currentUrl);
		let temp=this.currentDomain.split('.');
		if(temp.length>2) this.mainDomain=temp[temp.length - 2]+'.'+temp[temp.length - 1];
		else this.mainDomain=this.currentDomain;

		this.currentUrlBase=Str.getUrlBase(this.currentUrl);
		this.httpScheme=Str.getScheme(this.currentUrl);
		this.currentUri=Str.getUri(this.currentUrl);
		this.currentWindow=window;
	}
}
window.UserAgent=UserAgent;

let Domains = {
	settings:{
		'default':{
			lang: 'en',
			multiFields: true,
			showFieldSelector: true,
			businessField: '200',
			businessRole: '2400',
			currency: 'CNY',
			im: {
				showQA: true
			},

			//ui个性化设置
			customize: {
				businessField: false,//行业根据个性化
				language: false,//根据语言个性化
				userAgent: false,//根据设备类型（pc/mobile）个性化
				regByPhone: true,//使用手机注册
				regByEmail: true//使用邮箱注册
			}
		},

		'gugumall.cn':{
			lang: 'cn',
			multiFields: true,
			showFieldSelector: true,
			businessField: '100',
			businessRole: '400',
			currency: 'CNY',
			im: {
				showQA: true
			},

			//ui个性化设置
			customize: {
				businessField: false,//行业根据个性化
				language: false,//根据语言个性化
				userAgent: false,//根据设备类型（pc/mobile）个性化
				regByPhone: true,//使用手机注册
				regByEmail: true//使用邮箱注册
			}
		},

		'instraw.store':{
			lang: 'en',
			multiFields: false,
			showFieldSelector: false,
			businessField: '100',
			businessRole: '400',
			currency: 'USD',
			im: {
				showQA: true
			},

			//ui个性化设置
			customize: {
				businessField: false,//行业根据个性化
				language: false,//根据语言个性化
				userAgent: false,//根据设备类型（pc/mobile）个性化
				regByPhone: false,//使用手机注册
				regByEmail: true//使用邮箱注册
			}
		},

		'fozu.social':{
			lang: 'cn',
			multiFields: false,
			showFieldSelector: false,
			businessField: '400',
			businessRole: '4100',
			currency: 'CNY',
			im: {
				showQA: false
			},

			//ui个性化设置
			customize: {
				businessField: true,//行业根据个性化
				language: false,//根据语言个性化
				userAgent: false,//根据设备类型（pc/mobile）个性化
				regByPhone: true,//使用手机注册
				regByEmail: true//使用邮箱注册
			}
		}
	},

	getSetting:function (){
		let setting = this.settings[UserAgent.mainDomain];
		if(!setting) setting = this.settings['default'];
		return setting;
	}
}
window.Domains=Domains;

//通用组件-日历
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

		if(Array.isArray(input)){
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
window.D=D;

//通用组件 - 对话框
let Dialog={
	MSG_TYPE_OK:0,
	MSG_TYPE_INFO:1,
	MSG_TYPE_WARN:2,
	MSG_TYPE_ERR:3,
	MSG_TYPE_FATAL:4,
	uuid:null,//对话框id
	type:'',//对话框类型
	caller:null,//由哪个窗口调用
	win:null,
	onClose:null,
	onInterrupt:null,//框架机制关闭对话框导致操作中断时调用
	timeout:120000,//waiting模式下多久超时(ms，默认30秒)
	timeoutTimer:0,//waiting模式下超时计时器
	timeoutCallback:null,//waiting模式下超时回调
	timeoutMsg:null,//waiting模式下超时设置的消息
	canClose:true,
	cover:true,//是否显示遮盖窗体
	w:300,
	h:100,
	wMini:300,
	hMini:100,
	topMin:20,
	title:'I{提示}',
	showDelay:0,//延迟显示（已弃用参数）
	autoCloseDelay:0,//延迟自动关闭
	autoCloseTimer:null,//延迟自动关闭计时器
	bg:null,//指定背景（遮盖窗体）
	bgTransparent:'0.5',//背景（遮盖窗体）的透明度
	align:'center',//指定左右对其方式
	valign:'middle',
	bgColor:null,//内容区背景色
	padding:-1,//内容区padding
	noTitle:false,//是否不显示标题
	initContent:null,//初始化消息
	noFocus:true,//UserAgent.isMobile(),//不添加用于捕获输入焦点的input框
	shadow:true,//是否添加窗台阴影
	radius:0,//窗体圆角弧度
	dialogCls:null,//指定对话框css样式
	dialogBgCls:null,//指定对话框背景（遮盖窗体）css样式
	dialogTitleCls:null,//指定对话框标题css样式
	dialogContentCls:null,//指定对话框内容区css样式
	adjustInterval:null,
	_l:-1,
	_t:-1,
	_w:-1,
	_h:-1,

	getCaller:function(){
		if(top!=window && (typeof top.Dialog) != 'undefined'){
			return top.Dialog.win;
		}else{
			return this.win;
		}
	},

	/**
	 * 自动调整对话框位置、样式
	 */
	adjust:function (){
		if(!this.MSG_TYPE_INFO){
			Dialog.adjust();
			return;
		}

		if(!this.isOpen()) return;

		try{
			if(_$('dialogCloseIcon')){
				if(this.canClose) _$('dialogCloseIcon').style.display='';
				else _$('dialogCloseIcon').style.display='none';
			}
		}catch(e){}

		if(this.cover){
			_$('dialogBg').style.left='0px';
			_$('dialogBg').style.top='0px';
			_$('dialogBg').style.width='100%';
			_$('dialogBg').style.height=W.vh()+'px';
		}
	},

	/**
	 * 是否已打开
	 * @returns {boolean}
	 */
	isOpen:function(){
		return __$('dialog')?true:false;
	},

	/**
	 * 设置标题
	 * @returns {boolean}
	 */
	setTitle:function(tit){
		if(tit) this.title=tit;
		this.title=Lang.convert(this.title);
		if(_$('dialogTitleText')) _$('dialogTitleText').innerHTML=this.title;
		else if(_$('dialogTitle')) _$('dialogTitle').innerHTML=this.title;
		else if(_$('dialogWaitingText')){
			_$('dialogWaitingText').innerHTML=this.title;
			_$('dialogWaitingText').style.display=(Str.isBlank(this.title)?'none':'');
		}
	},

	/**
	 * 打开窗体
	 * @param _l 左边距（小于0表示不指定，设为百分比则转换成对话框所属窗口大小的比列）
	 * @param _t 上边距（小于0表示不指定，设为百分比则转换成对话框所属窗口大小的比列）
	 * @param _w 宽度（小于0表示不指定，设为百分比则转换成对话框所属窗口大小的比列）
	 * @param _h 高度（小于0表示不指定，设为百分比则转换成对话框所属窗口大小的比列）
	 * @param _onClose 关闭对话框时回调
	 * @param _onInterrupt 使用系统机制（设备返回键）关闭窗口时回调
	 * @param _win 与对话框关联的window对象
	 * @param _type 对话框显示类型（dialog - 常规，waiting - 表示“请等待”）
	 * @param _title 标题
	 * @param _initContent 初始窗体内容
	 * @param _showDelay 已弃用参数
	 * @param _autoCloseDelay
	 */
	open:function(_l,_t,_w,_h,_onClose,_onInterrupt,_win,_type,_title,_initContent,_cover,_showDelay,_autoCloseDelay,timeout, timeoutCallback, timeoutMsg){
		if(top!=window && (typeof top.Dialog) != 'undefined'){
			top.Dialog.open(_l,_t,_w,_h,_onClose,_onInterrupt,_win,_type,_title,_initContent,_cover,_showDelay,_autoCloseDelay,timeout, timeoutCallback, timeoutMsg);
			return;
		}

		if((typeof timeout)=='number') this.timeout=timeout;
		else this.timeout=30000;

		if((typeof timeoutCallback)=='function') this.timeoutCallback=timeoutCallback;
		else this.timeoutCallback=null;

		if((typeof timeoutMsg)=='string') this.timeoutMsg=Lang.convert(timeoutMsg);
		else this.timeoutMsg=null;

		if(this.timeoutTimer){
			clearTimeout(this.timeoutTimer);
			this.timeoutTimer=null;
		}

		this.closeStop();

		if((typeof _l)=='undefined') _l=-1;
		if((typeof _t)=='undefined') _t=-1;
		if((typeof _w)=='undefined') _w=-1;
		if((typeof _h)=='undefined') _h=-1;

		this._l=_l;
		this._t=_t;
		this._w=_w;
		this._h=_h;

		if(!this.isOpen()){
			this.uuid=(new Date()).getTime();
			top.Layers.saveInstance(this.uuid, _win?_win:window, this);
			Page.pushState('','','');
		}

		if((_l+'').indexOf('%')>0){
			_l=_l.substring(0,_l.length-1);
			_l=Math.ceil((W.w()*_l)/100);
		}

		if((_t+'').indexOf('%')>0){
			_t=_t.substring(0,_t.length-1);
			_t=Math.ceil((W.vh()*_t)/100);
		}

		if((_w+'').indexOf('%')>0){
			_w=_w.substring(0,_w.length-1);
			_w=Math.ceil((W.w()*_w)/100);
		}

		if((_h+'').indexOf('%')>0){
			_h=_h.substring(0,_h.length-1);
			_h=Math.ceil((W.vh()*_h)/100);
		}

		if(Str.isBlank(_type)) _type='dialog';

		if(_title) this.title=_title;
		else if(_type=='waiting') this.title='';
		else this.title='I{提示}';

		if(_initContent) this.initContent=_initContent;
		else this.initContent=null;

		let obj1=_$('dialogBg');
		let obj2=_$('dialog');
		if(obj1) obj1.parentNode.removeChild(obj1);
		if(obj2) obj2.parentNode.removeChild(obj2);

		this.type=_type;
		this.onClose=null;
		this.win=null;

		if(_onClose) this.onClose=_onClose;
		if(_win) this.win=_win;
		if((typeof _cover)=='boolean') this.cover=_cover;
		if((typeof _showDelay)=='number') this.showDelay=_showDelay;
		if((typeof _autoCloseDelay)=='number') this.autoCloseDelay=_autoCloseDelay;

		if(_type=='waiting') this.initWaiting();
		else this.initDialog();

		this.show(_l,_t,_w,_h);
	},

	/**
	 * 显示窗体
	 * @param _l
	 * @param _t
	 * @param _w
	 * @param _h
	 */
	show:function(_l,_t,_w,_h){
		this.closeStop();

		this.setTitle();
		if(this.initContent) this.setContent(this.initContent);

		if(this.cover){
			_$('dialogBg').style.left='0px';
			_$('dialogBg').style.top='0px';
			_$('dialogBg').style.width='100%';
			_$('dialogBg').style.height=W.vh()+'px';
		}

		if(_w>=0) _$('dialog').style.width=_w+'px';
		if(!this.cover) _$('dialogBg').style.width=W.elementWidth(_$('dialog'))+'px';

		if(_h>=0) _$('dialog').style.height=_h+'px';
		if(!this.cover) _$('dialogBg').style.height=W.elementHeight(_$('dialog'))+'px';

		_$('dialogContent').style.height=(W.elementHeight(_$('dialog')) - W.elementHeight(_$('dialogTitle')))+'px';

		if(_l<0){
			_l=Math.floor((W.vw()-W.elementWidth(_$('dialog')))/2);
			if(_l<0) _l=0;
		}
		_$('dialog').style.left=_l+'px';
		if(!this.cover) _$('dialogBg').style.left=_l+'px';

		if(_t<0) _t=Dialog.calcTop(0);
		_$('dialog').style.top=_t+'px';
		if(!this.cover) _$('dialogBg').style.top=_t+'px';

		_$('dialog').style.visibility='visible';
		if(this.cover) _$('dialogBg').style.visibility='visible';
		if(_$('dialogFocusInput')) _$('dialogFocusInput').focus();

		if(this.noTitle) new Movable('dialogContent', 'dialog', null, null, null, this.cover?null:['dialogBg']);
		else if(_$('dialogTitle')) new Movable('dialogTitle', 'dialog', null, null, null, this.cover?null:['dialogBg']);

		Dialog.adjustInterval=setInterval(Dialog.adjust,200);
		if(this.autoCloseDelay>0) this.closeDelay(false);

		if(this.type=='waiting' && this.timeout>0){
			this.timeoutTimer=setTimeout(Dialog.onTimeout, this.timeout);
		}
	},

	//移动至居中
	toCenter:function (){
		if(top!=window && (typeof top.Dialog) != 'undefined'){
			top.Dialog.toCenter();
			return;
		}

		let l=this._l;
		let t=this._t;
		let w=this._w;
		let h=this._h;
		if(w>=0) _$('dialog').style.width=w+'px';
		if(!this.cover) _$('dialogBg').style.width=W.elementWidth(_$('dialog'))+'px';

		if(h>=0) _$('dialog').style.height=h+'px';
		if(!this.cover) _$('dialogBg').style.height=W.elementHeight(_$('dialog'))+'px';

		_$('dialogContent').style.height=(W.elementHeight(_$('dialog')) - W.elementHeight(_$('dialogTitle')))+'px';

		if(l<0){
			l=Math.floor((W.vw()-W.elementWidth(_$('dialog')))/2);
			if(l<0) l=0;
		}
		_$('dialog').style.left=l+'px';
		if(!this.cover) _$('dialogBg').style.left=l+'px';

		if(t<0) t=Dialog.calcTop(0);
		_$('dialog').style.top=t+'px';
		if(!this.cover) _$('dialogBg').style.top=t+'px';
	},

	/**
	 * waiting模式下超时处理
	 */
	onTimeout:function(){
		if(Dialog.timeoutTimer){
			clearTimeout(Dialog.timeoutTimer);
			Dialog.timeoutTimer=null;
		}

		if(Dialog.timeoutCallback){
			try{
				Dialog.timeoutCallback.call(Dialog.win);
				return;
			}catch (e){
				Logger.log(e);
			}
		}
		Page.alert(!Str.isBlank(Dialog.timeoutMsg)?Dialog.timeoutMsg:'I{处理超时}',
			null,
			['<div class="displayBlock btnH40 btnBgBlue w120" onclick="location.reload();">I{刷新页面}</div>',
				'<div class="displayBlock btnH40 w100 mL10" onclick="Page.home();">I{返回首页}</div>'],
			Dialog.MSG_TYPE_WARN,
			null,
			null);
	},

	/**
	 * 关闭对话框
	 * @param bySysMechanism 是否框架机制关闭
	 */
	close:function(bySysMechanism){
		if(top!=window && (typeof top.Dialog) != 'undefined'){
			top.Dialog.close();
			return;
		}

		//waiting模式下禁止系统机制（设备返回键）关闭
		if(bySysMechanism && this.type=='waiting') return;

		if(!this.isOpen()) return;
		if(!this.canClose) return;

		if(this.timeoutTimer){
			clearTimeout(this.timeoutTimer);
			this.timeoutTimer=null;
		}
		this.closeStop();

		//从Layer栈中删除
		if(this.uuid) top.Layers.delInstance(this.uuid);

		this.type='';
		this.cover=true;
		this.title='';
		this.showDelay=0;
		this.autoCloseDelay=0;
		this.align='center';
		this.valign='middle';
		this.bgColor=null;
		this.padding=-1;
		this.noTitle=false;
		this.noFocus=true;//UserAgent.isMobile();
		this.shadow=true;
		this.radius=0;
		this.dialogCls=null;
		this.dialogBgCls=null;
		this.dialogTitleCls=null;
		this.dialogContentCls=null;
		this.canClose=true;
		this.timeout=30000;
		this.onTimeout=null;
		this.timeoutMsg=null;


		if(_$('dialogBg')) _$('dialogBg').parentNode.removeChild(_$('dialogBg'));
		if(_$('dialog')) _$('dialog').parentNode.removeChild(_$('dialog'));

		if(bySysMechanism && this.onInterrupt){
			try{
				this.onInterrupt.call(this.win?this.win:window);
			}catch(e){}
			this.onInterrupt=null;
		}

		if(this.onClose){
			try{
				this.onClose.call(this.win?this.win:window);
			}catch(e){}
			this.onClose=null;
		}

		if(Dialog.adjustInterval){
			clearInterval(Dialog.adjustInterval);
			Dialog.adjustInterval=null;
		}
	},

	/**
	 * 延时关闭
	 * @param bySysMechanism 是否框架机制关闭
	 * @param _autoCloseDelay 延时
	 */
	closeDelay:function(bySysMechanism, _autoCloseDelay){
		if((typeof _autoCloseDelay)=='number'){
			this.autoCloseDelay=_autoCloseDelay;
		}

		if(bySysMechanism) this.autoCloseTimer=setTimeout("Dialog.close(true)", this.autoCloseDelay);
		else this.autoCloseTimer=setTimeout("Dialog.close()", this.autoCloseDelay);
	},

	/**
	 * 停止延时关闭
	 */
	closeStop:function(){
		if(this.autoCloseTimer){
			clearTimeout(this.autoCloseTimer);
			this.autoCloseTimer=null;
		}
	},

	/**
	 * 设置背景（遮盖窗体）中iframe的url
	 * @param bgFrameUrl
	 */
	setBg:function(bgFrameUrl){
		if(bgFrameUrl) dialogFrame.location.href=bgFrameUrl;
	},

	/**
	 * 设置背景（遮盖窗体）透明度
	 * @param _transparent
	 */
	setBgTransparent:function(_transparent){
		if(!this.isOpen()) return;
		this.bgTransparent=_transparent+'';
		if(this.bgTransparent.indexOf('0.')==0){
			_$('dialogBg').style.opacity=this.bgTransparent.substring(1);
		}else{
			_$('dialogBg').style.opacity=this.bgTransparent;
		}
	},

	/**
	 * 设置左右对齐
	 * @param _align
	 */
	setAlign:function(_align){
		this.align=_align;

		if(!_$('dialogText')) return;
		_$('dialogText').style.textAlign=this.align;
	},

	/**
	 * 设置上下对齐
	 * @param _valign
	 */
	setValign:function(_valign) {
		this.valign = _valign;

		if(!_$('dialogText')) return;
		Utils.setAtt(_$('dialogText'), 'valign', _valign);
	},

	/**
	 * 确保窗口已经打开，如未打开则自动打开
	 */
	ensureOpened:function(_l, _t, _w, _h, _onClose, _onInterrupt){
		this.closeStop();
		if(!this.isOpen()) this.open(_l, _t, _w, _h,_onClose,_onInterrupt, window,'dialog');
		else{
			if((typeof _onClose) != 'undefined') this.onClose=_onClose;
			if((typeof _onInterrupt) != 'undefined') this.onInterrupt=_onInterrupt;
		}
	},

	/**
	 * 设置窗体内容
	 * @param content
	 * @param _bg
	 * @param _autoCloseDelay
	 */
	setContent:function(content, _bg, _autoCloseDelay){
		this.ensureOpened();

		_$('dialogText').innerHTML=Lang.convert(content);
		if(_bg) this.setBg(_bg);
		this.toCenter();

		if((typeof _autoCloseDelay)=='number'){
			this.autoCloseDelay=_autoCloseDelay;
			this.closeDelay(false);
		}
	},

	/**
	 * 设置窗体内容（默认的处理成功的样式）
	 * @param content
	 * @param _bg
	 * @param _autoCloseDelay
	 */
	setContentOk:function(content, _bg, _autoCloseDelay){
		this.ensureOpened();

		content='<div class="okColor">'+content+'</div>';
		this.setContent(content,_bg, _autoCloseDelay);
	},

	/**
	 * 设置窗体内容（默认的处理失败的样式）
	 * @param content
	 * @param _bg
	 * @param _autoCloseDelay
	 */
	setContentErr:function(content, _bg, _autoCloseDelay){
		this.ensureOpened();

		content='<div class="errorColor">'+content+'</div>';
		this.setContent(content, _bg, _autoCloseDelay);
	},

	/**
	 * 设置按钮区内容
	 * @param btns
	 */
	setBtns:function(_btns){
		if(!_$('dialogBtns')) return;
		if(!_btns) return;

		if(Array.isArray(_btns)) _btns=_btns.join('');
		_$('dialogBtns').innerHTML=Lang.convert(_btns);
		_$('dialogBtns').style.display='';
		_btns=null;
		delete _btns;
	},

	/**
	 * 初始化对话框（dialog类型）
	 */
	initDialog:function(){
		if(this.isOpen()) return;

		let style='min-width:'+this.wMini+'px; min-height:'+this.hMini+'px;';
		if(this.radius>0) style='border-radius:'+this.radius+'px;';
		if(this.shadow) style+='box-shadow: 0px 0px 2px 2px #dddddd;';

		let str=[];

		str.push('<div id="dialogBg" class="'+(this.dialogBgCls?this.dialogBgCls:'dialogBg')+'" style="z-index:'+W.getMaxZIndex()+' !important;"><iframe id="dialogFrame" name="dialogFrame" src="'+(this.bg?this.bg:'/blank.htm')+'" width="100%"  height="100%" frameborder="0" scrolling="no"></iframe></div>');

		str.push('<div id="dialog" class="'+(this.dialogCls?this.dialogCls:'dialog')+'" style="z-index:'+W.getMaxZIndex()+' !important;'+style+'">');
		str.push('	<div id="dialogTitle" class="noselect dialogTitle'+(this.dialogTitleCls?(' '+this.dialogTitleCls):'')+(this.noTitle?' hidden':'')+'">');
		str.push('		<div id="dialogTitleText" class="dialogTitleText"></div>');
		str.push('		<div id="dialogCloseIcon" class="dialogCloseIcon" onclick="Dialog.close();"></div>');
		str.push('	</div>');

		str.push('	<div id="dialogContent" class="'+(this.dialogContentCls?this.dialogContentCls:'dialogContent')+'">');
		str.push('		<table class="dialogTable">');
		str.push('			<tr>');
		str.push('				<td id="dialogText" class="dialogText" valign="'+this.valign+'" style="text-align:'+this.align+';'+(this.padding>-1?(' padding:'+this.padding+'px !important;'):'')+'"></td>');
		str.push('			</tr>');
		str.push('		</table>');
		str.push('	</div>');

		if(!this.noFocus){
			str.push('	<div id="dialogFocus" class="dialogFocus"><input type="text" id="dialogFocusInput"/></div>');
		}
		str.push('	<div id="dialogBtns" class="dialogBtns" style="display:none;"></div>');
		str.push('</div>');

		document.body.insertAdjacentHTML('afterbegin', Lang.convert(str.join('')));
		str=null;
	},

	/**
	 * 初始化对话框（waiting类型）
	 */
	initWaiting:function(){
		if(this.isOpen()) return;

		let style='';
		if(this.radius>0) style='border-radius:'+this.radius+'px;';

		let str=[];

		str.push('<div id="dialogBg" class="'+(this.dialogBgCls?this.dialogBgCls:'dialogBg')+'" style="z-index:'+W.getMaxZIndex()+' !important;"><iframe id="dialogFrame" name="dialogFrame" src="'+(this.bg?this.bg:'/blank.htm')+'" width="100%"  height="100%" frameborder="0" scrolling="no"></iframe></div>');

		str.push('<div id="dialog" class="'+(this.dialogCls?this.dialogCls:'dialogWaiting')+'" style="z-index:'+W.getMaxZIndex()+' !important;'+style+'">');
		str.push('	<div id="dialogContent" class="'+(this.dialogContentCls?this.dialogContentCls:'dialogContent')+'">');
		str.push('		<table class="dialogTable">');
		str.push('			<tr>');
		str.push('				<td id="dialogText" class="dialogText" valign="'+this.valign+'" style="text-align:'+this.align+';padding:0px !important;">');
		str.push('					<div id="dialogWaitingIcon" class="dialogWaitingIcon"><img src="/framework/img/waiting.gif"/></div>');
		str.push('					<div id="dialogWaitingText" class="dialogWaitingText" style="display:none;"></div>');
		str.push('				</td>');
		str.push('			</tr>');
		str.push('		</table>');
		str.push('	</div>');
		str.push('</div>');

		document.body.insertAdjacentHTML('afterbegin', Lang.convert(str.join('')));
		str=null;
	},

	/**
	 * 计算对话框上边距
	 */
	calcTop:function(offset){
		if((typeof offset)!='number') offset=0;
		let theTop=W.t();
		theTop+=Math.round((top.W.vh()-offset-W.elementHeight(_$('dialog')))/2);
		theTop+=offset;
		return Math.max(this.topMin, theTop);
	},

	/**
	 *
	 * @param _type
	 * @param _onClose
	 * @param _onInterrupt
	 * @param _win
	 * @param _title
	 * @param _msg
	 * @param _btns
	 */
	showAlert:function(_type, _onClose, _onInterrupt, _win, _title, _msg, _btns){
		if(top!=window && (typeof top.Dialog) != 'undefined'){
			top.Dialog.showAlert(_type, _onClose, _onInterrupt, _win, _title, _msg, _btns);
			return;
		}

		if((typeof _type)=='undefined') _type=Dialog.MSG_TYPE_INFO;
		this.dialogTitleCls='dialogTitle'+_type;

		//if(this.isOpen()){
		//	this.canClose=true;
		//	this.close();
		//}

		this.noFocus=true;

		let htm=[];
		htm.push('<div class="r alignC"><div class="alert_msg alert_msg_'+_type+'">'+_msg+'</div></div>');

		this.open(-1, -1, -1, -1, _onClose, _onInterrupt, _win, 'dialog', _title, htm.join(''));
		htm=null;
		delete htm;

		this.setBtns(_btns)
	}
}
window.Dialog=Dialog;


//通用功能-对象移动////
//Updated 2022-09-07
var Movables=[];
let MovableCurrent=null;//当前移动
function Movable(trigger, obj, onStart, onMoving, onEnd, shadows){
	//if(UserAgent.isMobile()) return;//移动端不需要此功能

	this.trigger=((typeof trigger)=='string'?_$(trigger):trigger);//触发移动的对象
	this.obj=((typeof obj)=='string'?_$(obj):obj);//需要移动的对象
	if(!this.trigger) return;

	this.onStart=onStart;//开始移动时执行的操作
	this.onMoving=onMoving;//移动时执行的操作
	this.onEnd=onEnd;//结束移动时执行的操作
	this.shadows=shadows;//跟随一起移动的对象（数组，可多个）

	//是否可开始移动
	this.movable=false;

	//鼠标位置信息
	this.initX=0;
	this.initY=0;
	this.lastX=0;
	this.lastY=0;

	//鼠标移出对象时是否终止移动
	this.endOnMouseout=true;

	//记录实例引用
	Movables[this.trigger.id]=this;

	//绑定事件
	if(UserAgent.isPC()){
		this.trigger.addEventListener('mousedown',function(event){
			let target=Utils.getEventTarget(event);
			if(target && target.id && Movables[target.id]) Movables[target.id].start(event);
		},false);
	}else{
		this.trigger.addEventListener('touchstart',function(event){
			let target=Utils.getEventTarget(event);
			if(target && target.id && Movables[target.id]) Movables[target.id].start(event);
		},false);
	}
}
Movable.prototype.reset=function(event){
	if(event.clientX){
		this.initX=event.clientX;
		this.initY=event.clientY;
	}else if(event.pageX){
		this.initX=event.pageX;
		this.initY=event.pageY;
	}
	this.lastX=this.initX;
	this.lastY=this.initY;
}
Movable.prototype.start=function(event){
	if(this.onStart && !this.onStart(event, this)){
		this.cancel();
		return;
	}

	if(!this.obj) return;

	if(event.targetTouches){
		if(event.targetTouches.length!=1) return;
		event=event.targetTouches[0];
	}

	if(event.clientX){
		this.initX=event.clientX;
		this.initY=event.clientY;
	}else if(event.pageX){
		this.initX=event.pageX;
		this.initY=event.pageY;
	}
	this.lastX=this.initX;
	this.lastY=this.initY;

	//提至最上层
	this.obj.style.zIndex=W.getMaxZIndex()+'';

	this.movable=true;

	MovableCurrent=this;
}
Movable.prototype.move=function(event){
	if(!this.movable) return;

	if(event.targetTouches){
		if(event.targetTouches.length!=1) return;
		event=event.targetTouches[0];
	}

	let movementX=0;
	let movementY=0;
	if(event.clientX){
		movementX=event.clientX-this.lastX;
		movementY=event.clientY-this.lastY;
	}else if(event.pageX){
		movementX=event.pageX-this.lastX;
		movementY=event.pageY-this.lastY;
	}

	if(event.clientX){
		this.lastX=event.clientX
		this.lastY=event.clientY;
	}else if(event.pageX){
		this.lastX=event.pageX;
		this.lastY=event.pageY;
	}

	let l=W.positionLeft(this.obj)*1;
	let t=W.positionTop(this.obj)*1;
	l+=movementX;
	t+=movementY;

	this.obj.style.left=l+'px';
	this.obj.style.top=t+'px';
	if(this.shadows){
		for(let i=0; i<this.shadows.length; i++){
			let shadow=((typeof this.shadows[i])=='string'?_$(this.shadows[i]):this.shadows[i]);
			if(shadow){
				let l=W.positionLeft(shadow)*1;
				let t=W.positionTop(shadow)*1;
				l+=movementX;
				t+=movementY;
				shadow.style.left=l+'px';
				shadow.style.top=t+'px';
			}
		}
	}

	if(this.onMoving) this.onMoving(event, this);
}
Movable.prototype.end=function(event){
	if(!this.movable) return;
	this.movable=false;
	MovableCurrent=null;
	if(this.onEnd) this.onEnd(event, this);
}
Movable.prototype.cancel=function(event){
	this.movable=false;
	MovableCurrent=null;
}
let MovableUtil = {
	create: function (trigger, obj, onStart, onMoving, onEnd, shadows){
		return new Movable(trigger, obj, onStart, onMoving, onEnd, shadows);
	}
}
window.MovableUtil=MovableUtil;
//通用功能-对象移动 end////


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

//窗体管理
let Layers={
	//打开的Layer、Dialog等窗体对象，由顶层窗口保存
	instances:[],

	//根据uuid得到窗口对象
	getInstance:function(uuid){
		if(top != window) return top.Layers.getInstance(uuid);
		for(let i=0; i<this.instances.length; i++){
			if(!this.instances[i]) continue;
			if(uuid==this.instances[i].uuid) return this.instances[i].instance;
		}
		return null;
	},

	//获得指定类型窗体的最新打开实例
	getInstanceOfType:function(type){
		if(top != window) return top.Layers.getInstanceOfType(type);
		if(!type) type='Layer';
		for(let i=this.instances.length-1; i>=0; i--){
			if(!this.instances[i]) continue;
			if(type==this.instances[i].type) return this.instances[i].instance;
		}
		return null;
	},

	//获得指定类型窗体的最新打开实例
	getPreInstanceOfType:function(type){
		if(top != window) return top.Layers.getPreInstanceOfType(type);
		if(!type) type='Layer';
		let lastIndex=this.instances.length-1;
		for(let i=this.instances.length-1; i>=0; i--){
			if(!this.instances[i]) continue;
			if(type==this.instances[i].type){
				lastIndex=i;
				break;
			}
		}

		for(let i=lastIndex-1; i>=0; i--){
			if(!this.instances[i]) continue;
			if(type==this.instances[i].type) return this.instances[i].instance;
		}

		return null;
	},

	//保存实例
	saveInstance:function(uuid, win, instance, doClose, divs, type){
		if(this.getInstance[uuid]) return;
		this.instances.push(new LayerObject(uuid, win, instance, doClose, divs, type));
	},

	//删除实例
	delInstance:function(uuid){
		for(let i=0; i<this.instances.length; i++){
			if(this.instances[i] && uuid==this.instances[i].uuid){
				this.instances[i].destory();
				this.instances[i]=null;
				this.instances.splice(i, 1);
			}
		}
	},

	//打开新Layer
	open:function(_win, _title, _url, _content, _btns, _padding, _onClose, _scrollable, _noTitle, _canClose){
		if(top != window && (typeof top.Layers) != 'undefined'){
			return top.Layers.open(_win,_title,_url,_content, _btns, _padding, _onClose, _scrollable, _noTitle, _canClose);
		}

		if(_title) _title=Lang.convert(_title);
		if(_btns) _btns=Lang.convert(_btns);
		if(_content) _content=Lang.convert(_content);

		let _uuid=''+(new Date()).getTime();
		let layer=new Layer(_uuid,_win?_win:window,_title,_url,_content, _btns, _padding, _onClose, _scrollable, _noTitle, _canClose);
		layer.open();

		top.Page.topLayer=layer;

		return layer;
	},

	//在最新一个Layer中打开
	load:function(_win, _title, _url, _content, _btns, _padding, _onClose, _closeOpenedObjects, _backing, _doNotSaveHistory, _replace, _scrollable, _noTitle, _canClose){
		if(top != window && (typeof top.Layers) != 'undefined'){
			top.Layers.load(_win,_title,_url,_content, _btns, _padding, _onClose, _closeOpenedObjects, _backing, _doNotSaveHistory, _scrollable, _noTitle, _canClose);
			return;
		}

		let currentLayer=this.getInstanceOfType('Layer');
		if(!currentLayer){
			this.open(_win, _title, _url, _content, _btns, _padding, _onClose, _scrollable, _noTitle, _canClose);
			return;
		}

		if(_title) _title=Lang.convert(_title);
		if(_btns) _btns=Lang.convert(_btns);
		if(_content) _content=Lang.convert(_content);

		if(_replace && currentLayer.urlStack && currentLayer.urlStack.length>0) currentLayer.urlStack.pop();
		currentLayer.load(_win,_title,_url,_content, _btns, _padding, _onClose, _closeOpenedObjects, _backing, _doNotSaveHistory);
	},

	//是否有打开的实例
	isOpen:function(){
		if(top != window) return top.Layers.isOpen();
		for(let i=0; i<this.instances.length; i++){
			if(this.instances[i]) return true;
		}
		return false;
	},

	//是否有与某窗口关联的打开的实例
	isOpenFor:function(win){
		if(top != window) return top.Layers.isOpenFor(win);
		for(let i=0; i<this.instances.length; i++){
			if(this.instances[i] && this.instances[i].win && this.instances[i].win==win) return true;
		}
		return false;
	},

	//关闭与某窗口关联的实例
	closeFor:function(win){
		if(!win) return;
		if(top != window){
			top.Layers.closeFor(win);
			return;
		}
		for(let i=0; i<this.instances.length; i++){
			if(this.instances[i] && this.instances[i].win && this.instances[i].win==win){
				let doNotDelInstance=null;//是否不删除实例（比如Layer对象仅仅是返回上页）
				if(this.instances[i].divs){
					for(let d=0; d<this.instances[i].divs.length; d++){
						this.instances[i].divs[d].style.visibility='hidden';
					}
				}else if(this.instances[i].doClose){
					this.instances[i].doClose();
				}else{
					if((typeof this.instances[i].instance.canClose)!='undefined'){
						this.instances[i].instance.canClose=true;
					}
					doNotDelInstance=this.instances[i].instance.close('true');
				}

				if(!doNotDelInstance && this.instances[i]){
					this.instances[i].destory();
					this.instances[i]=null;
					this.instances.splice(i, 1);
				}
			}
		}
	},

	//关闭
	close:function(uuid, noForce, _force){
		if(top != window){
			top.Layers.close(uuid, noForce, _force);
			return;
		}

		if(uuid){
			let inst=this.getInstance(uuid);
			if(!inst) return;

			inst.close(noForce, _force);
			top.Page.topLayer=Layers.getInstanceOfType();
			return;
		}

		for(let i=this.instances.length-1; i>=0; i--){
			if(this.instances[i]){
				let doNotDelInstance=null;//是否不删除实例（比如Layer对象仅仅是返回上页）
				if(this.instances[i].divs){
					for(let d=0; d<this.instances[i].divs.length; d++){
						this.instances[i].divs[d].style.visibility='hidden';
					}
				}else if(this.instances[i].doClose){
					this.instances[i].doClose();
				}else{
					if((typeof this.instances[i].instance.MSG_TYPE_OK) != 'undefined'){
						this.instances[i].instance.canClose=true;
					}
					doNotDelInstance=this.instances[i].instance.close('true', _force);
				}

				if(!doNotDelInstance && this.instances[i]){
					this.instances[i].destory();
					this.instances[i]=null;
				}

				break;
			}
		}
		top.Page.topLayer=Layers.getInstanceOfType();
	},

	//设置各Layer中iframe的高度
	setHeight:function(){
		for(let i=0; i<Layers.instances.length; i++){
			if(Layers.instances[i] && Layers.instances[i].type=='Layer'){
				Layers.instances[i].instance.setHeight();
			}
		}
	},

	//初始化
	init:function(){
		setInterval(Layers.setHeight, 200);
	},

	//设置各Layer的元素可见性
	setVisibility:function (){
		for(let i=0; i<Layers.instances.length; i++){
			if(Layers.instances[i]
				&& Layers.instances[i].type=='Layer'
				&&Layers.instances[i].instance){
				let frame = Layers.instances[i].instance.getFrameWindow();
				if(frame && (typeof frame.Page) != 'undefined') frame.Page.setVisibility();
			}
		}
	}
}
window.Layers=Layers;

//Layer、Dialog、普通DOM元素等窗口对象
function LayerObject(uuid, win, instance, doClose, divs, type){
	this.uuid=uuid;//窗口对象的uuid
	this.win=win;//窗口对象相关联的window对象
	this.instance=instance;//窗口对象
	this.doClose=(doClose?doClose:null);//指定用于执行关闭操作的方法
	this.divs=(divs?divs:null);//position属性为absolute的对象数组
	this.type=(type?type:null);//窗体类型
}
LayerObject.prototype.destory=function(){}

//窗体
//Layer、Dialog、普通DOM元素等窗口对象
function LayerObject(uuid, win, instance, doClose, divs, type){
	this.uuid=uuid;//窗口对象的uuid
	this.win=win;//窗口对象相关联的window对象
	this.instance=instance;//窗口对象
	this.doClose=(doClose?doClose:null);//指定用于执行关闭操作的方法
	this.divs=(divs?divs:null);//position属性为absolute的对象数组
	this.type=(type?type:null);//窗体类型
}
LayerObject.prototype.destory=function(){}

//窗体
function Layer(_uuid,_win,_title,_url,_content, _btns, _padding, _onClose, _scrollable, _noTitle, _canClose){
	this.caller=window;
	this.uuid=_uuid;
	this.win=_win;
	this.title=_title?_title:'';
	this.url=_url?_url:'';
	this.content=_content?_content:'';
	this.btns=_btns?_btns:null;
	this.padding=_padding?_padding:0;
	this.onClose=_onClose?_onClose:null;
	//在Layer中的某些操作需要将处理结果回传给打开该Layer的窗口（this.win）进行处理时，将回调方法赋给onAciton
	//传递给onAciton的参数有两个，第一个为表示发生何种时间的actionType，第二个参数为包含业务数据的对象
	this.onAction=null;
	this.urlStack=[];
	this.scrollable=(typeof _scrollable)=='boolean'?_scrollable:true;
	this.noTitle=(typeof _noTitle)=='boolean'?_noTitle:false;
	this.canClose=(typeof _canClose)=='boolean'?_canClose:true;

	this.opener=null;
	if(_win && (typeof _win.Page)!='undefined' && _win.Page.inLayer){
		this.opener=_win.Page.inLayer;
	}
}
Layer.prototype.action=function(actionType, data){
	if(!this.onAction) return;
	this.onAction.call(this.win?this.win:this.caller, actionType, data);
}
Layer.prototype.me=function(){
	return _$('layer_'+this.uuid);
}
Layer.prototype.bg=function(){
	return _$('layer_'+this.uuid+'_bg');
}
Layer.prototype.getElement=function(id){
	return _$(id);
}
Layer.prototype.getContentElement=function(){
	return _$('layerContent_'+this.uuid);
}
Layer.prototype.getFrame=function(){
	return _$('layerFrame_'+this.uuid);
}
Layer.prototype.getFrameWindow=function(){
	return IFrame.getWindow(this.getFrame());
}
Layer.prototype.getLoadingElement=function(){
	return _$('layerLoading_'+this.uuid);
}
Layer.prototype.getHeaderElement=function(){
	return _$('layerHeader_'+this.uuid);
}
Layer.prototype.getTitleElement=function(){
	return _$('layerTitle_'+this.uuid);
}
Layer.prototype.getBackElement=function(){
	return _$('layerBack_'+this.uuid);
}
Layer.prototype.getCloseElement=function(){
	return _$('layerClose_'+this.uuid);
}
Layer.prototype.getFooterElement=function(){
	return _$('layerFooter_'+this.uuid);
}
Layer.prototype.isOpen=function(){
	return this.getContentElement()?true:false;
}
Layer.prototype.scrollTop=function(){
	return this.getContentElement()?this.getContentElement().scrollTop:0;
}
Layer.prototype.scroll=function(t){
	if(!this.getContentElement()) return;
	this.getContentElement().scrollTop=t;
}
Layer.prototype.getHeight=function(){
	//if(!top.__$('Jcontent')){
	//	return top.W.h() - 54 - (Str.isBlank(this.btns)?0:51);
	//}else{
	return top.W.vh() - (this.noTitle?0:54) - (Str.isBlank(this.btns)?0:51);
	//}
}
Layer.prototype.setHeight=function(){
	if(!this.getFrame()) return;
	IFrame.adjustSize('layerFrame_'+this.uuid, 0, this.getHeight());
}
Layer.prototype.resize=function(){
	this.getContentElement().style.height=this.getHeight()+'px';
	//this.getTitleElement().style.width=(W.vw() - W.elementWidth(this.getBackElement()) - W.elementWidth(this.getCloseElement()) - 30)+'px';
	this.getTitleElement().style.display='';
}
Layer.prototype.open=function(){
	if(!this.isOpen()){
		this.urlStack=[];
		top.Layers.saveInstance(this.uuid, this.win, this, null, null, 'Layer');
	}

	if(!Str.isBlank(this.url) && !Auth.pass(this.url)){
		this.close();
		Auth.showNoPermissionMessage();
		return;
	}

	this.init();
	this.me().style.visibility='visible';
	this.bg().style.visibility='visible';
	this.load(this.win, this.title, this.url, this.content, this.btns, this.padding, this.onClose, false, false, false);
}
Layer.prototype.init=function(){
	if(this.me()) return;

	let s=[];
	s.push('<div class="layerBg" id="layer_'+this.uuid+'_bg" style="z-index:'+W.getMaxZIndex()+' !important; visibility: hidden;"></div>');
	s.push('<div class="layer" id="layer_'+this.uuid+'" style="z-index:'+W.getMaxZIndex()+' !important; visibility: hidden;">');
	s.push('	<div class="layerHeader'+(this.noTitle?' hidden':'')+'" id="layerHeader_'+this.uuid+'">');
	s.push('		<div id="layerBack_'+this.uuid+'" class="layerBack iconfont icon-back_light" onclick="top.Layers.close(\''+this.uuid+'\',\'true\');"></div>');
	s.push('		<div id="layerTitle_'+this.uuid+'" class="layerTitle" style="display: none;">'+this.title+'</div>');
	s.push('		<div id="layerClose_'+this.uuid+'" class="layerClose iconfont icon-close" onclick="top.Layers.close(\''+this.uuid+'\');"></div>');
	s.push('	</div>');

	let layerStyle='height:'+this.getHeight()+'px;';
	if(!this.scrollable) layerStyle+='overflow-y:hidden !important;';
	s.push('	<div class="layerContent" id="layerContent_'+this.uuid+'" style="'+layerStyle+'"></div>');
	s.push('	<div class="layerFooter" id="layerFooter_'+this.uuid+'" style="display: none;"></div>');
	s.push('</div>');

	s.push('<div class="layerLoading" id="layerLoading_'+this.uuid+'" style="z-index:'+W.getMaxZIndex()+' !important; visibility: hidden;"><img src="/framework/img/waiting.gif"/></div>');

	document.body.insertAdjacentHTML('afterbegin', s.join(''));
	this.getFooterElement().style.top=(top.W.vh() - 51)+'px';
	this.me().style.height=top.W.h()+'px';
	this.bg().style.height=top.W.h()+'px';
}
Layer.prototype.load=function(_win, _title, _url, _content, _btns, _padding, _onClose, _closeOpenedObjects, _backing, _doNotSaveHistory){
	if(!this.me()) return;

	if(!Str.isBlank(_url) && !Auth.pass(_url)){
		this.close('true');
		Auth.showNoPermissionMessage(Str.getUri(_url));
		return;
	}

	//关闭打开的窗体
	if((typeof _closeOpenedObjects)=='undefined'
		||_closeOpenedObjects==true){
		Layers.closeFor(IFrame.getWindow('layerFrame_'+this.uuid));
	}

	if(!_backing && !_doNotSaveHistory){
		this.urlStack.push([_win, _title,_url,_content, _btns, _padding, _onClose]);
	}

	this.win=_win;
	this.title=_title;
	this.url=_url;
	this.content=_content;
	this.btns=_btns;
	this.padding=(_padding?_padding:0);
	this.onClose=_onClose;

	this.setTitle(_title);
	this.setBtns(_btns);

	if(Str.isBlank(_url)){
		if(this.padding>0){
			this.getContentElement().innerHTML='<div style="padding:'+this.padding+'px;">'+_content+'</div>';
		}else{
			this.getContentElement().innerHTML=_content;
		}
		this.loaded();
	}else{
		if(_url.indexOf('/')==0 || _url.indexOf(UserAgent.currentDomain)>0){
			this.getLoadingElement().style.top=Math.ceil((W.vh()-W.elementHeight(this.getLoadingElement()))/2)+'px';
			this.getLoadingElement().style.left='0px';
			this.getLoadingElement().style.visibility='visible';
		}else{//跨域无法调用iframe的onload
			this.loaded();
		}

		this.getContentElement().innerHTML='<iframe id="layerFrame_'+this.uuid+'" name="layerFrame_'+this.uuid+'" src="'+_url+'" width="100%" height="100%" frameborder="0" scrolling="no" onload="Layers.getInstance(\''+this.uuid+'\').loaded();"></iframe>';
	}
}
Layer.prototype.setTitle=function(_title){
	if(!this.me())return;
	if(_title) _title=Lang.convert(_title);

	if((typeof _title)!='undefined') this.title=_title;
	this.getTitleElement().innerHTML=this.title;
	this.resize();
}
Layer.prototype.setContent=function(_content){
	if(!this.me()) return;
	if(_content) _content=Lang.convert(_content);
	else _content='';

	this.content=_content;
	this.getContentElement().innerHTML=this.content;
}
Layer.prototype.setBtns=function(_btns){
	if(!this.me()) return;
	if(_btns) _btns=Lang.convert(_btns);

	if((typeof _btns)!='undefined') this.btns=_btns;
	this.getFooterElement().innerHTML=this.btns?this.btns:'';
	this.getFooterElement().style.display=(Str.isBlank(this.btns)?'none':'');
	this.resize();
}
Layer.prototype.loaded=function(_content){
	if(!this.me()) return;

	this.getLoadingElement().style.visibility='hidden';

	top.scrollTo({
		top: 0,
		left: 0,
		behavior: "instant",
	});

	let iframeWindow=IFrame.getWindow('layerFrame_'+this.uuid);
	if(iframeWindow && (typeof iframeWindow.Page) != 'undefined') iframeWindow.Page.inLayer=this;

	Cookie.del('openUrl');
	Page.pushState('','','');
}
Layer.prototype.close=function(noForce){
	if(!this.me()){
		if(this.uuid) top.Layers.delInstance(this.uuid);
		return false;
	}

	//关闭本Layer的iframe相关的窗体
	let iframeWindow=IFrame.getWindow('layerFrame_'+this.uuid);
	if(iframeWindow){
		try{
			Layers.closeFor(iframeWindow);
		}catch(e){}
	}

	if(noForce && noForce=='true'){
		try{
			if(this.urlStack.length>1){
				let _previousPage=this.urlStack[this.urlStack.length-2];
				this.urlStack.pop();

				this.load(_previousPage[0], _previousPage[1], _previousPage[2], _previousPage[3], _previousPage[4], _previousPage[5], _previousPage[6], true, true, true);

				return true;
			}
		}catch(e){}
	}

	try{
		if(this.me()) this.me().parentNode.removeChild(this.me());
		if(this.bg()) this.bg().parentNode.removeChild(this.bg());
		if(this.getLoadingElement()) this.getLoadingElement().parentNode.removeChild(this.getLoadingElement());
	}catch(e){}

	if(this.onClose!=null){
		try{
			this.onClose.call(this.win?this.win:window);
		}catch(e){}
		this.onClose=null;
	}

	this.urlStack=null;
	if(this.win) this.win=null;
	if(this.uuid) top.Layers.delInstance(this.uuid);
	return false;
}
Layer.prototype.hide=function(){
	try{
		if(this.me()) this.me().style.visibility='hidden';
		if(this.bg()) this.bg().style.visibility='hidden';
		if(this.getLoadingElement()) this.getLoadingElement().style.visibility='hidden';
	}catch(e){}
}
Layer.prototype.show=function(){
	try{
		if(this.me()) this.me().style.visibility='visible';
		if(this.bg()) this.bg().style.visibility='visible';
		if(this.getLoadingElement()) this.getLoadingElement().style.visibility='visible';
	}catch(e){}
}

//二维码
let QRCoder={
	init:function(){
		loadJS({src:'/framework/js/qrcode/qrcode.js'});
	},

	/**
	 *
	 * @param container
	 * @param text
	 * @param width
	 * @param height
	 * @param colorDark
	 * @param colorLight
	 * @param correctLevel QRCode.CorrectLevel.L|M|Q|H
	 */
	create:function(container, text, width, height, colorDark, colorLight, correctLevel){
		if(!width) width=256;
		if(!height) height=256;
		if(!colorDark) colorDark='#000000';
		if(!colorLight) colorLight='#ffffff';
		if(!correctLevel) correctLevel=QRCode.CorrectLevel.H;

		if(!container){
			container=document.createElement('div');
			container.className='hidden';
			document.body.appendChild(container);
		}

		return new QRCode(container, {
			text: text,
			width: width,
			height: height,
			colorDark : colorDark,
			colorLight : colorLight,
			correctLevel : correctLevel
		});
	},

	update:function(instance, text){
		instance.clear();
		instance.makeCode(text);
	}
}
window.QRCoder=QRCoder;

//扫描
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
					scanType: ["qrCode", "barCode"], // 可以指定扫二维码还是一维码，默认二者都有
					success: function (res) {
						let wxScanResult = res.resultStr;//当needResult 为 1 时，扫码返回的结果
						if (wxScanResult.startsWith('CODE_128,')
							|| wxScanResult.startsWith('UPC_E,')) {
							wxScanResult = wxScanResult.substring(wxScanResult.indexOf(',') + 1);
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
//扫码处理  end

//单选组件
/**
 * 所有单选组件实例
 * @type {{instances: any[]}}
 */
let Pickers={
	instances:[],

	get:function (id){
		return this.instances[id];
	}
}

/**
 * 选择项
 * @param value 值
 * @param name 名称
 * @param style 样式（css className）
 * @param styleSelected 选中后的样式（css className）
 * @param canBeChoosen 是否可选
 * @param callback 回调
 * @constructor
 */
function PickerItem(value, name, style, styleSelected, canBeChoosen, callback){
	this.value=value;
	this.name=Lang.convert(name);
	this.style=style;
	this.styleSelected=styleSelected;
	this.canBeChoosen=(typeof canBeChoosen)=='boolean'?canBeChoosen:true;
	this.callback=callback;
	this.index=0;
}

/**
 *
 * @param container 容器
 * @param id 组件ID
 * @param style（css className）
 * @param input 关联input
 * @param items 可选项
 * @param initValue 初始值
 * @param itemStyle（css className）
 * @param itemStyleSelected（css className）
 * @param editable 是否可修改选中项（不能则仅用于展现）
 * @param callback 回调
 * @constructor
 */
function Picker(container,
				id,
				style,
				input,
				items,
				initValue,
				itemStyle,
				itemStyleSelected,
				editable,
				callback){
	this.container=(typeof container)=='string'?_$(container):container;
	this.id=id;
	this.style=(!style?'Picker':style);
	this.input=(typeof input)=='string'?_$(input):input;
	this.items=items;
	this.initValue=initValue;
	this.itemStyle=(!itemStyle?'PickerItem':itemStyle);
	this.itemStyleSelected=(!itemStyleSelected?'PickerItemSelected':itemStyleSelected);
	this.editable=(typeof editable)=='boolean'?editable:true;
	this.callback=callback;
	Pickers.instances[id]=this;

	let htm=[];
	htm.push('<div id="'+id+'" class="'+this.style+'">');
	for(let i=0;i<items.length;i++){
		items[i].index=i;
		let item=items[i];
		let _style=Str.isBlank(item.style)?this.itemStyle:item.style;

		let _disabledCss='cursor:not-allowed !important;';
		htm.push('<div id="'+id+'_'+i+'" style="'+(this.editable?'':_disabledCss)+'" class="'+_style + (i==0?' noBorderL':'')+'" onclick="Pickers.instances[\''+id+'\'].pick('+i+', false);">'+item.name+'</div>');
	}
	htm.push('</div>');

	this.container.innerHTML=htm.join('');
	htm=null;
	delete htm;

	for(let i=0;i<items.length;i++){
		if(initValue==items[i].value){
			this.pick(i, true);
			break;
		}
	}
}

/**
 * 查找指定下标或值对应的项
 * @param value
 */
Picker.prototype.findItem=function (valueOrIndex){
	if((typeof valueOrIndex)=='number') return this.items[value];

	for(let i=0; i<this.items.length; i++){
		if(valueOrIndex==this.items[i].value) return this.items[i];
	}
	return null;
}

Picker.prototype.setValue=function (value){
	let item=this.findItem(value);
	if(!item) return;
	this.pick(item.index, true);
}

/**
 * 选中指定项
 * @param index
 * @param force 是否强制选中（即时在不可修改选中项情况下）
 * @returns {null|*}
 */
Picker.prototype.pick=function(index, force){
	let choice=this.items[index];
	if(!choice) return;

	if(!force && (!this.editable || !choice.canBeChoosen)) return;//不可选择

	for(let i=0; i<this.items.length; i++){
		let item=this.items[i];
		let _style=Str.isBlank(item.style)?this.itemStyle:item.style;
		_$(this.id+'_'+i).className=_style+(i==0?' noBorderL':'');
	}

	let _styleSelected=Str.isBlank(choice.styleSelected)?this.itemStyleSelected:choice.styleSelected;
	_$(this.id+'_'+index).className=_styleSelected+(index==0?' noBorderL':'');

	if(this.input) this.input.value=choice.value;

	let _callback=choice.callback?choice.callback:this.callback;
	if(_callback) _callback(this.id, this.input, choice.value, choice.name);
}

//自定义下拉列表
let JSelectors={
	instances:[],

	/**
	 * 保持显示
	 * @param selectorId
	 */
	keep:function (selectorId){
		let instance=JSelectors.instances[selectorId];
		if(!instance) return;

		if(instance.timer){
			clearTimeout(instance.timer);
			instance.timer=null;
		}
	},

	/**
	 * 延时隐藏
	 * @param selectorId
	 */
	hideDelay:function(selectorId){
		let instance=JSelectors.instances[selectorId];
		if(!instance) return;

		if(instance.timer){
			clearTimeout(instance.timer);
			instance.timer=null;
		}

		instance.timer=setTimeout("JSelectors.hide('"+selectorId+"')",5000);
	},

	/**
	 * 隐藏
	 * @param selectorId
	 */
	hide:function(selectorId){
		let instance=JSelectors.instances[selectorId];
		if(!instance) return;
		instance.hide();
	},

	/**
	 * 通用选择项
	 * @param item
	 * @param txt
	 * @param txtSelected
	 * @param callback
	 */
	selectCommonItem:function(item, txt, txtSelected, callback){
		if(!item) return;

		if(item.className=='JSelectorCommonItemSelected'){
			item.className='JSelectorCommonItem';
			item.innerHTML=txt;
		}else{
			item.className='JSelectorCommonItemSelected';
			item.innerHTML=txtSelected;
		}

		if(callback) callback(item);
	},

	/**
	 * 搜索
	 * @param event
	 */
	search:function(event){
		let input=Utils.getEventTarget(event);
		if(!input) return;

		let selectorId=Utils.att(input, 'selectorId');
		let instance=JSelectors.instances[selectorId];
		if(!instance) return;

		if(instance.searchTimer){
			clearTimeout(instance.searchTimer);
			instance.searchTimer=null;
		}

		instance.searchTimer=setTimeout("JSelectors.searchDelay('"+selectorId+"')", 500);
	},

	/**
	 * 输入后延时搜索
	 * @param selectorId
	 */
	searchDelay:function(selectorId){
		let instance=JSelectors.instances[selectorId];
		if(!instance) return;

		if(instance.onSearch) instance.onSearch.call(instance.onSearchTarget?instance.onSearchTarget:window, instance);
		else instance.search();
	},

	/**
	 * 列表滚动
	 * @param event
	 */
	scroll:function(event){
		let list=Utils.getEventTarget(event);
		if(!list) return;

		let selectorId=Utils.att(list, 'selectorId');
		let instance=JSelectors.instances[selectorId];
		if(!instance) return;

		let scrollTop=list.scrollTop;

		//尚未滚动到底部
		if(!W.scrollToBottom(list)){
			//向上滚动时
			if(scrollTop<instance.listScrollTop)instance.listScrollTop=scrollTop;
			return;
		}

		if(scrollTop - instance.listScrollTop < 10) return;//相比上次滚动位置不超过10px，不予处理（避免重复调用）
		instance.listScrollTop=scrollTop;

		if(instance.onScroll) instance.onScroll.call(instance.onScrollTarget?instance.onScrollTarget:window, instance);
	},

	shown:function(){
		for(let i in this.instances){
			if(this.instances[i] && this.instances[i].shown()) return true;
		}
		return false;
	}
}

/**
 *
 * @param container 容器
 * @param selectorId ID
 * @param selectorWidth 宽度
 * @param listId 列表容器ID
 * @param listWidth 列表宽度
 * @param listHeight 列表最大高度
 * @param items 可选列表值[[id,name,callback,callbackTarget],[id,name,callback,callbackTarget]...]
 * @param itemCurrent 默认选择值[id,name]
 * @param selectorStyle 显示容器的css样式类名
 * @param selectorTextStyle 显示容器文本的css样式类名
 * @param selectorArrowStyle 显示容器下拉箭头的css样式类名
 * @param selectorArrowStyleOnShown 显示容器在列表显示状态下，下拉箭头的css样式类名
 * @param listStyle 类别容器的css样式类名
 * @param itemStyle 列表项的css样式类名
 * @param onChange 选中列表项时执行的操作
 * @param onChangeTarget 调用回调方法的对象（默认为window）
 * @param onSearch 用于替换搜索框的默认操作
 * @param onSearchTarget 调用搜功能的对象（默认window）
 * @param onScroll 列表滚动到底部时的操作（默认无操作）
 * @param onScrollTarget 调用列表滚动到底部时的操作的对象（默认window）
 * @param enableSearch 是否启用搜索
 * @constructor
 */
function JSelector(container,
				   selectorId,
				   selectorWidth,
				   listId,
				   listWidth,
				   listHeight,
				   items,
				   itemCurrent,
				   selectorStyle,
				   selectorTextStyle,
				   selectorArrowStyle,
				   selectorArrowStyleOnShown,
				   listStyle,
				   itemsStyle,
				   itemStyle,
				   onChange,
				   onChangeTarget,
				   enableSearch,
				   onSearch,
				   onSearchTarget,
				   onScroll,
				   onScrollTarget){
	if(!listId) listId=selectorId+'_list';
	if(!listWidth) listWidth=selectorWidth;

	this.container=(typeof container)=='string' ? _$(container) : container;
	this.selectorId=selectorId;
	this.selectorWidth=selectorWidth;
	this.listId=listId;
	this.listWidth=listWidth;
	this.listHeight=(typeof listHeight)=='number'?listHeight:0;
	this.items=items;
	this.itemCurrent=itemCurrent;
	this.selectorStyle=Str.isBlank(selectorStyle)?'JSelector':selectorStyle;
	this.selectorTextStyle=Str.isBlank(selectorTextStyle)?'JSelectorText':selectorTextStyle;
	this.selectorArrowStyle=Str.isBlank(selectorArrowStyle)?'JSelectorArrow':selectorArrowStyle;
	this.selectorArrowStyleOnShown=Str.isBlank(selectorArrowStyleOnShown)?'JSelectorArrowOnShown':selectorArrowStyleOnShown;
	this.listStyle=Str.isBlank(listStyle)?'JSelectorList':listStyle;
	this.itemsStyle=Str.isBlank(itemsStyle)?'JSelectorItems':itemsStyle;
	this.itemStyle=Str.isBlank(itemStyle)?'JSelectorItem':itemStyle;
	this.onChange=onChange;
	this.onChangeTarget=onChangeTarget;
	this.onSearch=onSearch;
	this.onSearchTarget=onSearchTarget;
	this.onScroll=onScroll;
	this.onScrollTarget=onScrollTarget;
	this.timer=null;
	this.searchTimer=null;
	this.startIndexToShow=0;
	this.itemCanBeChoosen=true;
	this.enableSearch=(typeof enableSearch)=='boolean'?enableSearch:true;
	if(!this.itemCurrent && this.items && this.items.length>0) this.itemCurrent=this.items[0];
	this.inScrollableContaier=(__$('Jcontent') != null) && ('scroll'==Utils.getStyle(__$('Jcontent'), 'overflowY'));
	this.listScrollTop=0;//列表滚动条位置

	if(this.itemCurrent) this.itemCurrent[1]=Lang.convert(this.itemCurrent[1]);

	//定位类型
	this.listPosition='relative';

	//是否与容器对其
	this.alignToContainer=false;

	//是否只读
	this.readOnly=false;

	JSelectors.instances[selectorId]=this;
}

/**
 * 初始化组件
 * @param container 容器ID
 */
JSelector.prototype.build=function(){
	//未指定必要参数
	if(!this.items || this.items.length==0 || !this.itemCurrent) return;

	let _selectorStyle='';
	if(this.selectorWidth) _selectorStyle+='width: '+this.selectorWidth+'px;';
	if(this.readOnly) _selectorStyle+='background-color: #eee;';

	let htm=[];
	htm.push('<div id="'+this.selectorId+'" class="'+this.selectorStyle+'" style="'+_selectorStyle+'" onclick="JSelectors.instances[\''+this.selectorId+'\'].show();">');
	htm.push('	<div id="'+this.selectorId+'_text" class="'+this.selectorTextStyle+'">'+this.itemCurrent[1]+'</div><div id="'+this.selectorId+'_arrow" class="'+this.selectorArrowStyle+'"></div>');
	htm.push('</div>');
	this.container.innerHTML=htm.join('');

	let arrowWidth=W.elementWidth(_$(this.selectorId+'_arrow'));
	if(arrowWidth<=0) arrowWidth=16;
	_$(this.selectorId+'_text').style.width=(this.selectorWidth - arrowWidth - 10)+'px';

	let _listStyle='width:'+(this.alignToContainer?W.elementWidth(this.container):this.listWidth)+'px; z-index:'+W.getMaxZIndex()+';';
	_listStyle+=' visibility:hidden; display:none; position:'+this.listPosition+';';

	let _itemsStyle='';
	if(this.listHeight>0) _itemsStyle+=' max-height:'+this.listHeight+'px; overflow-y:auto;';
	else _itemsStyle+=' display:inline-table; overflow-y:hidden;';

	htm=[];
	htm.push('<div id="'+this.listId+'" class="'+this.listStyle+'" style="'+_listStyle+'">');
	if(this.enableSearch){
		htm.push('	<div class="'+this.itemStyle+'" style="border-top:none !important;"><input type="text" id="'+this.selectorId+'_searcher" selectorId="'+this.selectorId+'" onfocus="JSelectors.keep(\''+this.selectorId+'\');" onblur="JSelectors.hideDelay(\''+this.selectorId+'\');" placeholder="I{搜索}" style="width:100%;" value="'+this.getSearchKeywords()+'"/></div>');
	}
	htm.push('<div id="'+this.listId+'_items" selectorId="'+this.selectorId+'" class="'+this.itemsStyle+'" style="'+_itemsStyle+'">');
	htm.push('</div>');
	htm.push('</div>');
	htm=Lang.convert(htm.join(''));

	if(this.inScrollableContaier) __$('JcontentBottom').insertAdjacentHTML('beforebegin', htm);
	else document.body.insertAdjacentHTML('beforeend', htm);
	htm=null;
	delete htm;

	if(_$(this.selectorId+'_searcher')){
		new InputEvent(_$(this.selectorId+'_searcher'), JSelectors.search);
	}

	//添加滚动事件监听
	_$(this.listId+'_items').addEventListener('scroll', JSelectors.scroll);
	this.buildList();
}

/**
 * 列表是否显示中
 */
JSelector.prototype.shown=function(){
	return (_$(this.listId) && _$(this.listId).style.visibility=='visible');
}

/**
 * 显示列表
 * @param force 是否强制显示
 */
JSelector.prototype.show=function(force){
	if(this.readOnly) return;
	if(!force && this.shown()){
		this.hide();
		return;
	}

	this.hide();

	let list=_$(this.listId);

	let t=0;
	let l=0;
	if(this.listPosition=='absolute'){
		t=W.elementTop(this.container);
		t+=W.elementHeight(this.container);
	}else{
		if(this.inScrollableContaier) t = W.elementTop(__$('JcontentBottom')) - (W.elementTop(_$(this.selectorId)) - W.elementTop(__$('Jcontent')));
		else t = W.elementHeight(document.body) - W.elementTop(_$(this.selectorId));
		t += W.elementHeight(list);
		t -= W.elementHeight(_$(this.selectorId));
	}
	l=this.alignToContainer?W.elementLeft(this.container):W.elementLeft(_$(this.selectorId));

	list.style.zIndex=W.getMaxZIndex()+'';
	list.style.visibility='visible';
	list.style.display='';
	list.style.left=(l - 1)+'px';
	list.style.top=(0 - t)+'px';
	list.scrollTop=this.listScrollTop;

	_$(this.selectorId+'_text').innerHTML=this.itemCurrent[1];
	_$(this.selectorId+'_arrow').className=this.selectorArrowStyleOnShown;

	//JSelectors.hideDelay(this.selectorId);
}

/**
 * 隐藏列表
 */
JSelector.prototype.hide=function(){
	if(this.timer){
		clearTimeout(this.timer);
		this.timer=null;
	}

	_$(this.listId).style.visibility='hidden';
	_$(this.listId).style.display='none';
	_$(this.selectorId+'_arrow').className=this.selectorArrowStyle;
}

/**
 * 查找列表项
 * @param id
 * @returns {null|*}
 */
JSelector.prototype.findItem=function(id){
	for(let i=0;i<this.items.length;i++){
		if(this.items[i][0]==id) return this.items[i];
	}
	return null;
}

/**
 * 新增一个列表项
 * @param item
 */
JSelector.prototype.addItem=function(item){
	let exists=this.findItem(item[0]);
	if(exists) return;

	this.items.push(item);
}

/**
 * 新增多个列表项
 * @param _items
 */
JSelector.prototype.addItems=function(_items){
	for(let i=0;i<_items.length;i++){
		this.addItem(_items[i]);
	}
	this.buildList();
	if(this.shown()) this.show(true);
}

/**
 * 设置列表项
 * @param _items
 */
JSelector.prototype.setItems=function(_items){
	this.listScrollTop=0;
	this.items=_items;
	this.buildList();
	if(this.shown()) this.show(true);
}

/**
 * 设清除列表项
 */
JSelector.prototype.clearItems=function(){
	this.items=[];
	this.buildList();
	if(this.shown()) this.show(true);
}

/**
 * 重新构建列表
 */
JSelector.prototype.buildList=function(){
	let htm=[];
	let index=this.enableSearch?1:0;
	for(let i=this.startIndexToShow; i<this.items.length; i++){
		htm.push('	<div id="'+this.selectorId+'_item'+i+'" class="'+this.itemStyle+'" style="'+(index==0?'border-top:none !important;':'')+'" onmouseover="JSelectors.keep(\''+this.selectorId+'\');" onmouseout="JSelectors.hideDelay(\''+this.selectorId+'\');" onclick="JSelectors.instances[\''+this.selectorId+'\'].choose(\''+this.items[i][0]+'\',false);">'+this.items[i][1]+'</div>');
		index++;
	}
	_$(this.listId+'_items').innerHTML=Lang.convert(htm.join(''));
	htm=null;
	delete htm;
}

/**
 * 设置当前选中项
 * @param _current
 * @param _doNotCallback 不要回调
 * @param _doNotHideList 不要隐藏列表
 */
JSelector.prototype.setCurrent=function(_current, _doNotCallback, _doNotHideList){
	this.itemCurrent=_current;
	if(this.itemCurrent) this.itemCurrent[1]=Lang.convert(this.itemCurrent[1]);
	this.choose(_current[0],true, _doNotCallback, _doNotHideList);
}
JSelector.prototype.getCurrent=function(){
	return this.itemCurrent;
}

/**
 * 选中列表项
 * @param id
 * @param force 是否强制选中
 * @param _doNotCallback 不要回调
 * @param _doNotHideList 不要隐藏列表
 */
JSelector.prototype.choose=function(id, force, _doNotCallback, _doNotHideList){
	//不可选择且非强制选中
	if(!force && !this.itemCanBeChoosen){
		this.hide();
		return;
	}

	//隐藏列表
	if(!_doNotHideList) this.hide();

	let item=this.findItem(id);
	let text=Lang.convert(item[1]);
	_$(this.selectorId+'_text').innerHTML=text;
	this.itemCurrent=[id, text];
	if(_doNotCallback) return;

	if((typeof item[2])=='function') item[2].call(item[3]?item[3]:(this.onChangeTarget?this.onChangeTarget:window), this.selectorId, id, text);
	else if(this.onChange) this.onChange.call(this.onChangeTarget?this.onChangeTarget:window, this.selectorId, id, text);
}

/**
 *
 */
JSelector.prototype.search=function(){
	if(!_$(this.selectorId+'_searcher')) return;
	let keywords=Str.trimAll(_$(this.selectorId+'_searcher').value);

	for(let i=this.startIndexToShow; i<this.items.length; i++){
		if(!_$(this.selectorId+'_item'+i)) continue;
		let matches=Str.isBlank(keywords) || this.items[i][1].toLowerCase().indexOf(keywords.toLowerCase())>-1;
		_$(this.selectorId+'_item'+i).style.display=(matches?'':'none');
	}
}

/**
 *
 */
JSelector.prototype.getSearchKeywords=function(){
	if(!_$(this.selectorId+'_searcher')) return '';
	return Str.trimAll(_$(this.selectorId+'_searcher').value);
}

/**
 *
 * @param keywords
 */
JSelector.prototype.setSearchKeywords=function(keywords){
	if(!_$(this.selectorId+'_searcher')) return;
	_$(this.selectorId+'_searcher').value=keywords;
}

/**
 * 通用面板（页面）
 * @type {{}}
 */
let Jpanels={
	/**
	 * 面板分区
	 * @param label 分区标签（默认无标签）
	 * @param items 条目  [Jitem]
	 * @param padding
	 * @param radius 圆角半径
	 * @constructor
	 */
	Jblock:function(args){
		//label, items, padding, radius
		this.label=args.label;
		this.items=(Array.isArray(args.items))?args.items:[];
		this.padding=(typeof args.padding)=='number'?args.padding:10;
		this.radius=(typeof args.radius)=='number'?args.radius:10;
		this.blockIndex=0;
		this.itemIndex=0;
		this.forRoles=(Array.isArray(args.roles))?args.roles:null;
		this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
	},

	/**
	 * 条目（限定高度）
	 * @param name
	 * @param info
	 * @param icon
	 * @param iconType 图标类型 img | iconfont
	 * @param withArrow
	 * @param url
	 * @param urlTarget
	 * @param content
	 * @param onclick
	 * @param extra
	 * @constructor
	 */
	Jitem:function(args){
		//name, info, icon, iconType, withArrow, url, urlTarget, content, onclick, extra
		this.name=args.name;
		this.info=args.info;
		this.icon=args.icon;
		this.iconType=Str.isBlank(args.iconType)?'iconfont':args.iconType;
		this.withArrow=(typeof args.withArrow)=='boolean'?args.withArrow:true;
		this.url=args.url;
		this.urlTarget=args.urlTarget;
		this.content=args.content;
		this.onclick=args.onclick;
		this.extra=args.extra;
		this.blockIndex=0;
		this.itemIndex=0;
		this.forRoles=(Array.isArray(args.roles))?args.roles:null;
		this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
	},

	/**
	 * 一行（不限高度）
	 * @param cells [Jcell]
	 * @param cellsPerRow 每行多少个cell
	 * @param cellspacing cell间距
	 * @constructor
	 */
	Jrow:function(args){
		//cells, cellsPerRow, cellspacing
		this.cells=(Array.isArray(args.cells))?args.cells:[];
		this.cellsPerRow=(typeof args.cellsPerRow)=='number'?args.cellsPerRow:2;
		this.cellspacing=(typeof args.cellspacing)=='number'?args.cellspacing:5;
		if(this.cellsPerRow<=0) this.cellsPerRow=1;
		this.blockIndex=0;
		this.itemIndex=0;
		this.forRoles=(Array.isArray(args.roles))?args.roles:null;
		this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
	},

	/**
	 * 一行中的一个内容单元
	 * @param name
	 * @param url
	 * @param urlTarget
	 * @param content
	 * @param onclick
	 * @param extra
	 * @constructor
	 */
	Jcell:function(args){
		//name, url, urlTarget, content, onclick, extra
		this.name=args.name;
		this.url=args.url;
		this.urlTarget=args.urlTarget;
		this.content=args.content;
		this.onclick=args.onclick;
		this.extra=args.extra;
		this.blockIndex=0;
		this.itemIndex=0;
		this.forRoles=(Array.isArray(args.roles))?args.roles:null;
		this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
	},

	/**
	 *
	 * @param args
	 * @constructor
	 */
	Jbtn:function(args){
		//name, url, urlTarget, content, onclick, extra, bg, color
		this.name=args.name;
		this.url=args.url;
		this.urlTarget=args.urlTarget;
		this.content=args.content;
		this.onclick=args.onclick;
		this.extra=args.extra;
		this.blockIndex=0;
		this.itemIndex=0;
		this.forRoles=(Array.isArray(args.roles))?args.roles:null;
		this.showAnyWay=(typeof args.showAnyWay)=='boolean'?args.showAnyWay:false;
		this.bg=args.bg;
		this.color=args.color;
	},

	/**
	 * 往blocks的最后一个或指定位置（index）的block追加一个item
	 * @param blocks
	 * @param index
	 * @param item
	 */
	appendItem:function(blocks, index, item){
		if(!blocks || blocks.length==0) return;
		if((typeof index)!='number' || index<0 || index>blocks.length-1) index=blocks.length-1;
		if((blocks[index] instanceof Jpanels.Jbtn)) return;
		blocks[index].items.push(item);
	},

	/**
	 *
	 * @param panelId
	 * @param blockIndex
	 * @param itemIndex
	 */
	click:function(panelId, blockIndex, itemIndex){
		let instance=this.instances[panelId];
		if(!instance) return;
		instance.click(blockIndex, itemIndex);
	},

	/**
	 * 关闭
	 * @param panelId
	 */
	close:function(panelId){
		if(panelId){
			let instance=this.instances[panelId];
			if(!instance) return;
			instance.close();
		}else{
			for(let i in this.instances) this.instances[i].close();
		}
	},

	//所有面板实例
	instances:[]
}
window.Jpanels=Jpanels;

/**
 *
 * @param panel
 * @param blockIndex
 * @returns {string}
 */
Jpanels.Jbtn.prototype.build=function(panel, blockIndex){
	let s=[];
	let myStyle='';
	if(blockIndex==0) myStyle='margin-top:0px;';
	else myStyle='margin-top:10px;';
	if(!Str.isBlank(this.bg)) myStyle+='background-color:'+this.bg+';';
	if(!Str.isBlank(this.color)) myStyle+='color:'+this.color+';';
	if(!this.showAnyWay && this.forRoles && !Auth.isRole(this.forRoles)) myStyle+='display:none !important;';

	s.push('<div id="'+this.id+'_'+blockIndex+'_0_name" class="Jbtn" style="'+myStyle+'" onclick="Jpanels.click(\''+panel.id+'\','+blockIndex+');">');
	s.push(this.name);
	s.push('</div>');
	return Lang.convert(s.join(''));
}

/**
 *
 * @param panel
 * @param blockIndex
 * @param width 一行的可用宽度（由上级节点决定）
 * @returns {string}
 */
Jpanels.Jrow.prototype.build=function(panel, blockIndex, width){
	let myStyle='';
	if(!this.showAnyWay && this.forRoles && !Auth.isRole(this.forRoles)) myStyle+='display:none !important;';

	let visibleCells=0;//可见单元数
	for(let j=0; j<this.cells.length; j++){
		if(!this.cells[j].showAnyWay && this.cells[j].forRoles && !Auth.isRole(this.cells[j].forRoles)){
			continue;
		}
		visibleCells++;
	}

	let cellWidth=Math.floor((width-this.cellspacing*(this.cellsPerRow+1))/this.cellsPerRow);
	let s=[];
	s.push('<div class="Jrow" style="'+myStyle+'">');

	let index=0;
	for(let j=0; j<this.cells.length; j++){
		this.cells[j].blockIndex=blockIndex;
		this.cells[j].rowIndex=j;
		let isBottom=(index>=visibleCells-this.cellsPerRow);
		let cellStyle='width:'+cellWidth+'px; margin-left:'+this.cellspacing+'px; margin-top:'+this.cellspacing+'px';
		if(isBottom) cellStyle+=' margin-bottom:'+this.cellspacing+'px';

		if(!this.cells[j].showAnyWay && this.cells[j].forRoles && !Auth.isRole(this.cells[j].forRoles)){
			cellStyle+='display:none !important;';
		}else{
			index++;
		}

		s.push('<div id="'+panel.id+'_'+blockIndex+'_'+j+'_name" class="Jcell" style="'+cellStyle+' onclick="Jpanels.click(\''+panel.id+'\','+blockIndex+','+j+');">');
		s.push(this.cells[j].name);
		s.push('</div>');
	}
	s.push('</div>');
	return Lang.convert(s.join(''));
}

/**
 *
 * @param id
 * @param name
 * @param container
 * @param blocks [Jblock | Jbtn | Jrow]
 * @param padding
 * @constructor
 */
function Jpanel(id, name, container, blocks, padding){
	if(container && (typeof container)=='string') container=_$(container);
	this.id=id;
	this.name=Lang.convert(name);
	this.container=container;
	this.blocks=blocks?blocks:[];
	this.padding=(typeof padding)=='number'?padding:10;

	if(Jpanels.instances[this.id]) Jpanels.instances[this.id].close();
	Jpanels.instances[this.id]=this;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 */
Jpanel.prototype.getItem=function(blockIndex, itemIndex){
	if(blockIndex>this.blocks.length-1) return null;
	let b=this.blocks[blockIndex];
	if(itemIndex>b.items.length-1) return null;
	return b.items[itemIndex];
}

Jpanel.prototype.isOpen=function(){
	if(_$(this.id)) return true;
	return false;
}

/**
 *
 */
Jpanel.prototype.build=function(width){
	if((typeof width) != 'number') width=W.vw();
	width-=this.padding*2;

	let s=[];

	let panelStyle='';
	if(this.padding>0) panelStyle=' style="padding:'+this.padding+'px;"';

	s.push('<div class="Jpanel" id="'+this.id+'"'+panelStyle+'>');
	for(let i=0; i<this.blocks.length; i++){
		let b=this.blocks[i];
		b.blockIndex=i;

		if(b instanceof Jpanels.Jbtn){
			s.push(b.build(this, i));
			continue;
		}

		if(b instanceof Jpanels.Jrow){
			s.push(b.build(this, i, width));
			continue;
		}

		let blockStyle='';
		if(b.label || i==0) blockStyle='margin-top:0px;';
		else blockStyle='margin-top:10px;';

		if(b.label){
			let labelStyle='';
			if(i==0) labelStyle='margin-top:0px;';
			else labelStyle='margin-top:10px;';
			s.push('<div class="Jlabel" style="'+labelStyle+'">'+b.label+'</div>');
		}

		if(b.padding>0) blockStyle+=' padding:'+b.padding+'px;';
		if(b.radius>0) blockStyle+=' border-radius:'+b.radius+'px;';
		if(!b.showAnyWay && b.forRoles && !Auth.isRole(b.forRoles)) blockStyle+='display:none !important;';

		s.push('<div class="Jblock" style="'+blockStyle+'">');
		for(let j=0; j<b.items.length; j++){
			let item=b.items[j];
			item.blockIndex=i;
			item.itemIndex=j;

			let itemStyle='';
			if(j==b.items.length-1) itemStyle=' style=""';
			if(!item.showAnyWay && item.forRoles && !Auth.isRole(item.forRoles)) itemStyle+='display:none !important;';

			s.push('<div id="'+this.id+'_'+i+'_'+j+'" class="Jitem"'+itemStyle+' onclick="Jpanels.click(\''+this.id+'\','+i+','+j+');">');
			s.push('<div id="'+this.id+'_'+i+'_'+j+'_name" class="JitemName">'+item.name+'</div>');
			if(item.withArrow) s.push('<div class="JitemArrow"></div>');
			if(!Str.isBlank(item.icon)){
				if(item.iconType=='img') s.push('<div id="'+this.id+'_'+i+'_'+j+'_icon"  class="JitemIcon"><img src="'+item.icon+'"/></div>');
				else s.push('<div id="'+this.id+'_'+i+'_'+j+'_icon"  class="JitemIcon"><div class="iconfont '+item.icon+'"></div></div>');
			}
			if(!Str.isBlank(item.info)) s.push('<div id="'+this.id+'_'+i+'_'+j+'_info"  class="JitemInfo">'+item.info+'</div>');
			s.push('</div>');
		}
		s.push('</div>');
	}
	s.push('</div>');
	s=Lang.convert(s.join(''));

	if(this.container){
		if(this.container instanceof Layer){
			this.container.load(window, this.name, null, s, null, 0, null);
		}else{
			if(this.container.id=='Jcontent' && __$('JcontentBottom')){
				__$('JcontentBottom').insertAdjacentHTML('beforebegin', s);
			}else{
				this.container.innerHTML=s;
			}
		}
	}else{
		this.container = Layers.open(window, this.name, null, s, null, 0, null);
	}

	s=null;
	delete s;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemName=function(blockIndex, itemIndex, content){
	let item=this.getItem(blockIndex,itemIndex);
	if(!item) return;
	item.name=content;
	let itemName=_$(this.id+'_'+blockIndex+'_'+itemIndex+'_name');
	if(!itemName) return;
	itemName.innerHTML=content;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemInfo=function(blockIndex, itemIndex, content){
	let item=this.getItem(blockIndex,itemIndex);
	if(!item) return;
	item.info=content;
	let itemInfo=_$(this.id+'_'+blockIndex+'_'+itemIndex+'_info');
	if(!itemInfo) return;
	itemInfo.innerHTML=content;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemIcon=function(blockIndex, itemIndex, content){
	let item=this.getItem(blockIndex,itemIndex);
	if(!item) return;
	item.icon=content;
	let itemIcon=_$(this.id+'_'+blockIndex+'_'+itemIndex+'_icon');
	if(!itemIcon) return;
	if(itemIcon.innerHTML.indexOf('<img')>-1) itemIcon.innerHTML='<img src="'+content+'"/>';
	else itemIcon.innerHTML='<div class="iconfont '+content+'"></div>';
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param content
 */
Jpanel.prototype.setItemUrl=function(blockIndex, itemIndex, content){
	let item=this.getItem(blockIndex,itemIndex);
	if(!item) return;
	item.url=content;
}

/**
 *
 * @param blockIndex
 * @param itemIndex
 * @param onclick
 */
Jpanel.prototype.setOnClick=function(blockIndex, itemIndex, onclick){
	let item=this.getItem(blockIndex,itemIndex);
	if(!item) return;
	item.onclick=onclick;
}

/**
 * 点击条目
 * @param blockIndex
 * @param itemIndex
 */
Jpanel.prototype.click=function(blockIndex, itemIndex){
	if(blockIndex<0 || itemIndex<0 || blockIndex>this.blocks.length-1) return;
	let b=this.blocks[blockIndex];
	let obj=null;
	if(b instanceof Jpanels.Jbtn) obj=b;
	else if(b instanceof Jpanels.Jrow) obj=b.cells[itemIndex];
	else obj=b.items[itemIndex];

	if(obj.onclick){
		obj.onclick.call(window, obj);
	}else if(obj.url){
		if(obj.urlTarget) obj.urlTarget.location.href=obj.url;
		else if(this.container instanceof Layer) this.container.load(window, obj.name, obj.url, null, null, 0, null);
		else Layers.open(window, obj.name, obj.url, null, null, 0, null);
	}else if(obj.content){
		if(this.container instanceof Layer) this.container.load(window, obj.name, null, obj.content, null, 0, null);
		else Layers.open(window, obj.name, null, obj.content, null, 0, null);
	}
}

/**
 * 关闭
 */
Jpanel.prototype.close=function(){
	if(this.container instanceof Layer) this.container.close();
}

//分页组件
//通用分页
/**
 *
 * @param id 组件ID
 * @param containers [组件容器]，以数组方式指定，指定多个表示在页码多个位置显示分页组件以方便交互
 * @param recordsPerPage 每页多少条（records per page）
 * @param pagesPerSection 每个页码片段显示多少个页码（pages per section）
 * @param pn 当前第几页（page number)
 * @param total 总记录条数
 * @param goto 加载某页的方法，如不指定则约定调用name为frm的表单的submit、并约定frm包含name为pn的表示页码的字段
 * @param showSummary 是否显示形如“共xx条记录”的统计信息，默认显示
 * @param showPageNumbers 是否显示页码，默认显示，设为false则显示为"当前页码/总页数"的形式（多用于手机端）
 * @param hideIfNoRecord 没记录时是否隐藏分页组件，默认不隐藏
 * @param style 组件的样式（PagingStyle对象）
 * @constructor
 */
function Paging(id, containers, recordsPerPage, pagesPerSection, pn, total, goto, showSummary, showPageNumbers, hideIfNoRecord, style){
	this.id=id;
	this.containers=containers;
	for(let i=0; i<this.containers.length; i++){
		if((typeof this.containers[i])=='string') this.containers[i]=_$(this.containers[i]);
	}
	this.recordsPerPage=recordsPerPage;
	this.pagesPerSection=pagesPerSection;
	this.pn=pn;
	this.total=total;
	this.totalPages=1;
	this.goto=goto;
	this.showSummary=(typeof showSummary)=='boolean'?showSummary:true;
	this.showPageNumbers=(typeof showPageNumbers)=='boolean'?showPageNumbers:true;
	this.hideIfNoRecord=(typeof hideIfNoRecord)=='boolean'?hideIfNoRecord:false;
	this.style=style?style:new PagingStyle();

	//计算总页数
	if(this.total>this.recordsPerPage){
		if(this.total%this.recordsPerPage==0) this.totalPages=this.total/this.recordsPerPage;
		else this.totalPages=Math.floor(this.total/this.recordsPerPage) + 1;
	}else{
		this.totalPages=1;
	}

	Pagings.instances[this.id]=this;
}

/**
 * 分页组件样式
 * @param style 整体组件样式
 * @param styleSummary 统计信息样式
 * @param stylePage 页码样式
 * @param styleCurrent 当前页样式
 * @param stylePages 当前页/总页数样式
 * @param stylePrePage 前一页样式
 * @param styleFirstPage 第一页样式
 * @param styleNextPage 下一页样式
 * @param styleLastPage 最后一页样式
 * @constructor
 */
function PagingStyle(style, styleSummary, stylePage, styleCurrent, stylePages, stylePrePage, styleFirstPage, styleNextPage, styleLastPage){
	this.style=Str.isBlank(style)?'Paging':style;
	this.styleSummary=Str.isBlank(styleSummary)?'summary':styleSummary;
	this.stylePage=Str.isBlank(stylePage)?'page':stylePage;
	this.styleCurrent=Str.isBlank(styleCurrent)?'current':styleCurrent;
	this.stylePages=Str.isBlank(stylePages)?'pages':stylePages;
	this.stylePrePage=Str.isBlank(stylePrePage)?'previous':stylePrePage;
	this.styleFirstPage=Str.isBlank(styleFirstPage)?'first':styleFirstPage;
	this.styleNextPage=Str.isBlank(styleNextPage)?'next':styleNextPage;
	this.styleLastPage=Str.isBlank(styleLastPage)?'last':styleLastPage;
}

/**
 * 创建分页组件
 */
Paging.prototype.build=function(){
	if(this.total==0 && this.hideIfNoRecord){
		for(let i=0; i<this.containers.length; i++) this.containers[i].innerHTML='';
		return;
	}

	//确定显示哪些页码（尽可能地使得当前页码处于中间位置）
	let sectionHalf=Math.floor(this.pagesPerSection/2);

	let start=this.pn-(this.pagesPerSection-1);//显示的起始页码（最多比当前页码小pagesPerSection-1）
	if(start<1) start=1;

	//往后移动直至当前页左右两边页码一样多，或最后一个页码已经是最后一页（页码数为偶数时当前页位于左侧一半）
	if(this.pagesPerSection%2==0){
		while(start + (sectionHalf - 1) < this.pn
		&& start + (this.pagesPerSection - 1) < this.totalPages){
			start++;
		}
	}else{
		while(start + sectionHalf < this.pn
		&& start + (this.pagesPerSection - 1) < this.totalPages){
			start++;
		}
	}

	let end=start + this.pagesPerSection - 1;//显示的最后一个页码
	if(end > this.totalPages) end=this.totalPages;
	//确定显示哪些页码（尽可能地使得当前页码处于中间位置） end

	let s=[];

	s.push('<div id="'+this.id+'" class="'+this.style.style+'">');

	//显示统计
	if(this.showSummary){
		s.push('<div class="'+this.style.styleSummary+'">I{总记录数} '+this.total+'</div>');
	}

	//第一页 & 前一页
	if(this.pn > 1){
		s.push('<div onclick="Pagings.gotoPage(\''+this.id+'\', 1);" class="'+this.style.styleFirstPage+'"></div>');
		s.push('<div onclick="Pagings.prePage(\''+this.id+'\', '+this.pn+');" class="'+this.style.stylePrePage+'"></div>');
	}

	//页码
	if(this.showPageNumbers){
		for(let i=start;i<=end;i++){
			if(i==this.pn){
				s.push('<div class="'+this.style.styleCurrent+'">'+i+'</div>');
			}else{
				s.push('<div onclick="Pagings.gotoPage(\''+this.id+'\', '+i+');" class="'+this.style.stylePage+'">'+i+'</div>');
			}
		}
	}else{
		s.push('<div class="'+this.style.stylePages+'">'+this.pn+'/'+this.totalPages+'</div>');
	}

	//下一页 & 最后一页
	if(this.pn < this.totalPages){
		s.push('<div onclick="Pagings.nextPage(\''+this.id+'\', '+this.pn+');" class="'+this.style.styleNextPage+'"></div>');
		s.push('<div onclick="Pagings.gotoPage(\''+this.id+'\', '+this.totalPages+');" class="'+this.style.styleLastPage+'"></div>');
	}

	s.push('</div>');

	s=Lang.convert(s.join(''));
	for(let i=0; i<this.containers.length; i++) this.containers[i].innerHTML=s;
	s=null;
	delete s;
}


/**
 * 管理分页组件实例
 * @type {{gotoPage: Pagings.gotoPage, instances: *[]}}
 */
let Pagings={
	instances:[],

	/**
	 *
	 * @param id
	 * @param pn
	 */
	gotoPage:function(id, pn){
		let paging=this.instances[id];
		if(!paging) return;

		paging.pn=pn;
		if(paging.goto){
			paging.goto(pn);
		}else if((typeof frm) != 'undefined' && (typeof frm.pn) != 'undefined'){
			frm.pn.value=pn;
			frm.submit();
		}
	},

	/**
	 * 下一页
	 * @param id
	 * @param pn
	 */
	nextPage:function(id, pn){
		let paging=this.instances[id];
		if(!paging) return;

		pn++;
		if(pn>paging.totalPages) pn=paging.totalPages;
		this.gotoPage(id, pn);
	},

	/**
	 * 前一页
	 * @param id
	 * @param pn
	 */
	prePage:function(id, pn){
		let paging=this.instances[id];
		if(!paging) return;

		pn--;
		if(pn<1) pn=1;
		this.gotoPage(id, pn);
	}
}
window.Pagings=Pagings;

//用户登录、权限等
let Auth={
	//当前会话ID
	sessionId:'',

	//当前登录用户access token
	accessToken:'',

	//当前登录用户拥有的角色
	ssoUserRoles:'',

	//当前登录用户名称
	ssoUserName:'',

	//当前登录用户类型（由业务逻辑定义）
	ssoUserType:'',

	//当前登录用户ID
	ssoUserId:'',

	//当前登录子账号ID
	ssoSubUserId:'',

	//客户端IP
	ssoUserIp:'',

	//登录或登出回调
	callback:null,

	//登录或登出回调
	callbackTarget:null,

	//访问控制配置
	permissions:[],

	//用户档案
	profile:null,

	/**
	 * 访问控制
	 * @param url 可包含通配符*
	 * @param roles 可访问角色
	 * @param excludes 排除的url（不受该权限控制）
	 * @param loginPage 指定登录页
	 * @param loginPageOpenType 指定登录页打开方式（Layer表示在Layer组件中打开，top表示在顶层窗口中打开）
	 * @constructor
	 */
	PERMISSION:function(url, roles, excludes, loginPage, loginPageOpenType){
		this.url=url;
		this.roles=roles;
		this.excludes=excludes;
		this.loginPage=loginPage;
		this.loginPageOpenType=loginPageOpenType;
		Auth.permissions.push(this);
	},

	/**
	 * 是否已登录
	 * @returns {false|null|*}
	 */
	logined:function(){
		return (!Str.isBlank(this.accessToken) && (typeof top.Auth)!='undefined' && top.Auth.profile && top.Auth.profile.userDisplayName) ? true : false;
	},

	pass:function(uri){
		if(top != window && (typeof top.Auth)!='undefined') return top.Auth.pass(uri);
		for(let i=0; i<this.permissions.length; i++){
			if(!this.permissions[i].pass(uri)) return false;
		}
		return true;
	},

	/**
	 * 由哪个权限规则所拦截
	 * @param uri
	 * @returns {*|boolean}
	 */
	getPermissionKeeper:function(uri){
		if(top != window && (typeof top.Auth)!='undefined') return top.Auth.pass(uri);
		for(let i=0; i<this.permissions.length; i++){
			if(!this.permissions[i].pass(uri)) return this.permissions[i];
		}
		return null;
	},

	restore:function (){
		this.accessToken=Cookie.get('accessToken') ? Cookie.get('accessToken') : '';
		this.ssoUserName=Cookie.get('ssoUserName') ? Cookie.get('ssoUserName') : '';
		this.ssoUserType=Cookie.get('ssoUserType') ? Cookie.get('ssoUserType') : '';
		this.ssoUserRoles=Cookie.get('ssoUserRoles') ? Cookie.get('ssoUserRoles') : '';
		this.ssoUserId=Cookie.get('ssoUserId') ? Cookie.get('ssoUserId') : '';
		this.ssoSubUserId=Cookie.get('ssoSubUserId') ? Cookie.get('ssoSubUserId') : '';
		this.ssoUserIp=Cookie.get('ssoUserIp') ? Cookie.get('ssoUserIp') : '';
	},

	reset:function(){
		this.profile=null;
		this.accessToken='';
		this.ssoUserRoles='';
		this.ssoUserName='';
		this.ssoUserType='';
		this.ssoUserId='';
		this.ssoSubUserId='';
		this.ssoUserIp='';
		Cookie.del('accessToken');
		Cookie.del('ssoUserName');
		Cookie.del('ssoUserType');
		Cookie.del('ssoUserRoles');
		Cookie.del('ssoUserId');
		Cookie.del('ssoSubUserId');
		Cookie.del('ssoUserIp');
	},

	isRole:function(roleId){
		if(top != window && (typeof top.Auth)!='undefined') return top.Auth.isRole(roleId);
		if(Array.isArray(roleId)){
			for(let i=0; i<roleId.length; i++){
				if(roleId[i]=='non_login' && Str.isBlank(this.accessToken)) return true;//未登录时显示
				if(!Str.isBlank(this.ssoUserRoles) && Str.contains(this.ssoUserRoles.split(','), roleId[i])) return true;
			}
			return false;
		}
		if(roleId=='non_login' && Str.isBlank(this.accessToken)) return true;//未登录时显示
		return !Str.isBlank(this.ssoUserRoles) && Str.contains(this.ssoUserRoles.split(','), roleId);
	},

	//退出系统
	logout:function(callback){
		if((typeof callback) == 'function') this.callback=callback;
		else this.callback=null;

		top.Dialog.open(-1,-1,-1,-1,null,null,window,'waiting','I{auth,正在注销}','',true);
		let ajax=new Ajax();
		ajax.send('GET', Auth.doLogout, '/framework/api/sso/client/logout');
	},
	doLogout:function(ajax){
		if(ajax.getReadyState()==4&&ajax.getStatus()==200) {
			let resp=ajax.getResponseJson();
			if(resp.success){
				console.log('logout!!!!');
				Auth.reset();
				if(Auth.callback) Auth.callback.call(window, 'logout', {success:true});
				else Auth.loadProfile('onLogin');
			}else{
				console.log('logout failed, error code = '+resp.code);
				if(Auth.callback) Auth.callback.call(window, 'logout', {success:false});
			}
			top.Dialog.close();
		}
	},

	//提交登录
	login:function(event, isClick, theForm, callback){
		if(!isClick && event && event.keyCode!=13) return false;

		if(!Cookie.accepted()){
			Cookie.showPolicies(true);
			return;
		}

		if((typeof callback) == 'function') this.callback=callback;
		else this.callback=null;

		if((typeof theForm) == 'string') theForm=_$(theForm);
		else if(!theForm) theForm=ssoLogin;

		if(_$('agree') && !_$('agree').checked){
			Page.alert('I{auth,您只有阅读并同意隐私条款才能享受我们提供的服务}', null, null, Dialog.MSG_TYPE_WARN);
			return false;
		}

		theForm.sso_user_id.value=theForm.sso_user_id.value.toLowerCase();
		theForm.sso_user_id.value=Str.trimAll(theForm.sso_user_id.value);
		theForm.sso_user_pwd.value=Str.passwordClear(theForm.sso_user_pwd.value);
		if((theForm.sso_user_id.value.match(/^[\w\.]{1,}@{1}[\w\.]{1,}$/)==null || theForm.sso_user_id.value.length>64)
			&&((typeof Countries) != 'undefined')
			&&!Countries.isPhoneNumberValid(theForm.sso_user_id.value)){
			Page.alert('I{auth,请正确填写邮箱或手机}', null, null, Dialog.MSG_TYPE_WARN);
			return false;
		}

		if(_$('verifyCodeDiv')
			&&_$('verifyCodeDiv').style.display!='none'){//验证码登录
			if(_$('verify_code').value.match(/^[0-9]{4,6}$/)==null){
				Page.alert('I{auth,请正确输入验证码}', null, null, Dialog.MSG_TYPE_WARN);
				return false;
			}
		}else{
			if(!Str.passwordValid(theForm.sso_user_pwd.value, 6, 32)){
				Page.alert('I{auth,密码必须是8~32位非空白字符}', null, null, Dialog.MSG_TYPE_WARN);
				return false;
			}
			if(_$('verify_code')) _$('verify_code').value='';
		}

		if(_$('sso_verifier_code')
			&&theForm.sso_verifier_code.value.match(/^[a-zA-Z0-9]{4,6}$/)==null){
			Page.alert('I{auth,请输入图形验证码}', null, null, Dialog.MSG_TYPE_WARN);
			return false;
		}
		theForm.sso_user_pwd.value=hex_md5(hex_md5(theForm.sso_user_pwd.value));

		top.Dialog.open(-1,-1,-1,-1,null,null,window,'waiting','I{auth,正在登录}','',true);

		let ajax=new Ajax();
		ajax.sendForm(theForm, Auth.doLogin);
		return true;
	},
	doLogin:function(ajax){
		_$('sso_user_pwd').value='';
		if(ajax.getReadyState()==4&&ajax.getStatus()==403) {
			Page.alert('I{auth,安全检测未通过，请稍后再试}', null, null, Dialog.MSG_TYPE_ERR);
			return;
		}
		if(ajax.getReadyState()==4&&ajax.getStatus()==200) {
			let resp=ajax.getResponseJson();
			if(!resp.success){
				let datas=resp.datas;
				let errorCode=(datas && datas.result) ? (datas.result.result+'') : resp.code;

				if(errorCode=='invalid_verifier_code'){
					Page.alert('I{auth,验证码错误}', null, null, Dialog.MSG_TYPE_ERR);
					if((typeof requestVerifier)=='function') requestVerifier();
				}
				else if(errorCode=='verify_failed') Page.alert('I{auth,验证码错误}', null, null, Dialog.MSG_TYPE_ERR);
				else if(errorCode=='0') Page.alert('I{auth,认证失败}', null, null, Dialog.MSG_TYPE_ERR)
				else if(errorCode=='-1' || errorCode=='-2' || errorCode=='-3') Page.alert('I{auth,登录失败}', null, null, Dialog.MSG_TYPE_ERR);
				else if(errorCode=='-5') Page.alert('I{auth,未通过防机器人检测}', null, null, Dialog.MSG_TYPE_ERR)
				else if(errorCode=='-11') Page.alert('I{auth,验证码错误}', null, null, Dialog.MSG_TYPE_ERR);
				else if(errorCode=='-12') Page.alert('I{auth,账号不存在}', null, null, Dialog.MSG_TYPE_ERR);
				else if(errorCode=='-13'){
					if((typeof datas.result.chances)=='number'){
						if(datas.result.chances==0){
							Page.alert('I{auth,您的账号因密码连续错误次数过多被冻结，可尝试通过找回密码解冻账号或联系管理员}', null, null, Dialog.MSG_TYPE_ERR);
						}else{
							Page.alert('I{auth,密码错误}<br/>I{auth,最多还可尝试次数} <span class="red bold">'+datas.result.chances+'</span>', null, null, Dialog.MSG_TYPE_ERR);
						}
					}else{
						Page.alert('I{auth,密码错误}', null, null, Dialog.MSG_TYPE_ERR);
					}
				}
				else if(errorCode=='-14') Page.alert('I{auth,用户状态异常}', null, null, Dialog.MSG_TYPE_ERR);
				else if(errorCode=='-21') Page.alert('I{auth,二次验证失败}', null, null, Dialog.MSG_TYPE_ERR);
				else Page.alert('I{auth,认证失败}', null, null, Dialog.MSG_TYPE_ERR);
			}else{
				if(resp.datas && resp.datas.result){
					console.log('save accessToken = '+resp.datas.result.accessToken);
					Auth.saveAccessToken(resp.datas.result.accessToken);

					let _onLogin=Cookie.get('Login.onLogin');
					if(_onLogin){
						Cookie.del('Login.onLogin');
						if('toWechatMiniprogramOrders'==_onLogin && (typeof wx)!='undefined'){
							let uaId=Cookie.get('UA_ID');
							if(!uaId) uaId='';
							wx.miniProgram.navigateTo({url: '/pages/order/list?uaId='+uaId+'&accessToken='+encodeURIComponent(resp.datas.result.accessToken)});
							return;
						}else if('toDouyinMiniprogramOrders'==_onLogin && (typeof tt)!='undefined'){
							let uaId=Cookie.get('UA_ID');
							if(!uaId) uaId='';
							wx.miniProgram.navigateTo({url: '/pages/order/list?uaId='+uaId+'&accessToken='+encodeURIComponent(resp.datas.result.accessToken)});
							return;
						}
					}
				}
				Auth.loadProfile('onLogin');
			}
		}
	},

	saveAccessToken:function(accessToken){
		this.accessToken=accessToken;
		top.Auth.accessToken=accessToken;
		Cookie.set('accessToken', accessToken);
	},

	/**
	 *
	 * @param onAction 调用场景
	 */
	loadProfile:function(onAction){
		if(!top.Dialog.isOpen()){
			top.Dialog.open(-1,-1,-1,-1,null,null,window,'waiting','','',true);
		}

		//获取用户档案
		let ajax=new Ajax();
		if(onAction) ajax.onAction=onAction;
		ajax.send('GET', Auth.doLoadProfile, '/api/platform/user/profile?t='+(new Date()).getTime()+'&I18NGroups='+encodeURIComponent(','+UserAgent.currentUri));
	},

	doLoadProfile:function(ajax){
		if(ajax.getReadyState()==4&&ajax.getStatus()==200) {
			//关闭等待窗口
			top.Dialog.close();

			//用户信息
			let resp = ajax.getResponseJson();

			//保存用户信息
			Auth.doProfile(ajax, resp);

			//设置页面元素可见性
			if(window != top && (typeof top.Page) != 'undefined'){
				top.Page.setVisibility();
				top.Layers.setVisibility();
			}else{
				Page.setVisibility();
				Layers.setVisibility();
			}

			//回调
			if(top.Auth.callback){
				//如果是登录/注册后调用，关闭登录/注册窗口
				if(ajax.onAction && ajax.onAction=='onLogin'){
					setTimeout(Auth.closeLoginLayer, 200);
				}

				try{
					top.Auth.callback.call(top.Auth.callbackTarget ? top.Auth.callbackTarget : window, 'login', {success:true});
				}catch (e){
					console.log(e);
				}
			}else if(Page.inLayer && Page.inLayer.onAction){
				Page.inLayer.action('login', {success:true});
			}else{
				//如果是登录/注册后调用，关闭登录/注册窗口
				if(ajax.onAction && ajax.onAction=='onLogin'){
					setTimeout(Auth.closeLoginLayer, 200);
				}
			}
		}
	},

	closeLoginLayer:function (){
		let topLayer=Layers.getInstanceOfType('Layer');
		if(topLayer) topLayer.close();
	},

	doProfile:function(ajax, resp){
		if(window != top && (typeof top.Auth) != 'undefined'){
			top.Auth.doProfile(ajax, resp);
			return;
		}

		if(resp.I18NResources){
			for(let i=0; i<resp.I18NResources.length; i++){
				let ofGroup = resp.I18NResources[i];
				let group = ofGroup.group;
				let rs = ofGroup.resources;
				for(let j=0; j<rs.length; j++){
					for(let k in rs[j].languages) Lang.setString(group, rs[j].key, k, rs[j].languages[k]);
				}
			}
		}

		Page.clientMask = resp.clientMask;
		Auth.reset();
		Auth.profile=resp;
		let _accessToken=ajax.getHeader('accessToken');
		let _ssoUserName=ajax.getHeader('sso_user_name');
		let _ssoUserType=ajax.getHeader('sso_user_type');
		let _ssoUserRoles=ajax.getHeader('sso_user_roles');
		let _ssoUserId=ajax.getHeader('sso_user_id');
		let _ssoSubUserId=ajax.getHeader('sso_sub_user_id');
		let _ssoUserIp=ajax.getHeader('sso_user_ip');
		if(_ssoUserName) _ssoUserName=decodeURIComponent(_ssoUserName);

		if(_accessToken) Cookie.set('accessToken', _accessToken);
		else Cookie.del('accessToken');

		if(_ssoUserName) Cookie.set('ssoUserName', _ssoUserName);
		else Cookie.del('ssoUserName');

		if(_ssoUserType) Cookie.set('ssoUserType', _ssoUserType);
		else Cookie.del('ssoUserType');

		if(_ssoUserRoles) Cookie.set('ssoUserRoles', _ssoUserRoles);
		else Cookie.del('ssoUserRoles');

		if(_ssoUserId) Cookie.set('ssoUserId', _ssoUserId);
		else Cookie.del('ssoUserId');

		if(_ssoSubUserId) Cookie.set('ssoSubUserId', _ssoSubUserId);
		else Cookie.del('ssoSubUserId');

		if(_ssoUserIp) Cookie.set('ssoUserIp', _ssoUserIp);
		else Cookie.del('ssoUserIp');

		if(resp.time) UserAgent.timeDiff=resp.time;
		Logger.log('time diff with server = '+UserAgent.timeDiff);

		//币种初始化
		Currency.init(resp);
		Currency.restore();

		//产业链初始化
		Fields.init(resp);
		Fields.restore();

		//用户信息初始化
		Auth.restore();
	},

	//提示未登录
	showNotLoginMessage:function(uri, needCallback, callback, callbackTarget, isMask){
		let loginPage='/sso/signup.htm';
		if(needCallback) loginPage+='?needCallback=true';//登录完需要回调
		top.Auth.callback=callback ? callback : null;
		top.Auth.callbackTarget=callbackTarget ? callbackTarget : null;

		let loginPageOpenType='Layer';
		if(uri){
			let permissionKeeper = Auth.getPermissionKeeper(uri);
			if(permissionKeeper && !Str.isBlank(permissionKeeper.loginPage)) loginPage=permissionKeeper.loginPage;
			if(permissionKeeper && !Str.isBlank(permissionKeeper.loginPageOpenType)) loginPageOpenType=permissionKeeper.loginPageOpenType;
		}

		if(loginPageOpenType=='Layer') {
			Dialog.showAlert(Dialog.MSG_TYPE_WARN,
				null,
				null,
				window,
				'I{提示}',
				'I{请登录后再进行操作}',
				['<div id="noPermissionAlert" class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="top.Dialog.close(); Layers.open(window, \'\', \'' + loginPage + '\', null, null, 0);">I{登录} / I{注册}</div>',
					'<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="clearTimeout(Auth.checkPermissionTimer); top.Dialog.close(); '+(!isMask?'':'top.location.href=\'/\';')+'">I{'+(isMask?'返回首页':'返回')+'}</div>']);
		}else{
			Dialog.showAlert(Dialog.MSG_TYPE_WARN,
				null,
				null,
				window,
				'I{提示}',
				'I{请登录后再进行操作}',
				['<div id="noPermissionAlert" class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="top.Dialog.close(); top.location.href=\''+loginPage+'\';">I{登录} / I{注册}</div>',
					'<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="clearTimeout(Auth.checkPermissionTimer); top.Dialog.close();'+(!isMask?'':'top.location.href=\'/\';')+'}">I{'+(isMask?'返回首页':'返回')+'}</div>']);
		}
	},

	showNoPermissionMessage:function(uri){
		if(!this.logined()){//未登录
			this.showNotLoginMessage(uri, false, null, null, true);
			return;
		}

		let loginPage='/sso/signup.htm';
		let loginPageOpenType='Layer';
		if(uri){
			let permissionKeeper = Auth.getPermissionKeeper(uri);
			if(permissionKeeper && !Str.isBlank(permissionKeeper.loginPage)) loginPage=permissionKeeper.loginPage;
			if(permissionKeeper && !Str.isBlank(permissionKeeper.loginPageOpenType)) loginPageOpenType=permissionKeeper.loginPageOpenType;
		}

		if(loginPageOpenType=='Layer') {
			Dialog.showAlert(Dialog.MSG_TYPE_WARN,
				Auth.back,
				null,
				window,
				'I{提示}',
				'I{您未登录或没有该操作权限}',
				['<div id="noPermissionAlert" class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="top.Dialog.close(); Layers.load(window, \'\', \''+loginPage+'\', null, null, 0, null);">I{登录}</div>',
					'<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="clearTimeout(Auth.checkPermissionTimer); top.Dialog.close(); top.location.href=\'/\';">I{返回首页}</div>']);
		}else{
			Dialog.showAlert(Dialog.MSG_TYPE_WARN,
				Auth.back,
				null,
				window,
				'I{提示}',
				'I{您未登录或没有该操作权限}',
				['<div id="noPermissionAlert" class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="top.Dialog.close(); top.location.href=\''+loginPage+'\';">I{登录}</div>',
					'<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="clearTimeout(Auth.checkPermissionTimer); top.Dialog.close(); top.location.href=\'/\';">I{返回首页}</div>']);
		}
	},

	//提示没有权限
	checkPermissionTimer:null,
	checkPermission:function(){
		if(Auth.checkPermissionTimer){
			clearTimeout(Auth.checkPermissionTimer);
			Auth.checkPermissionTimer=null;
		}

		//重新加载cookie（可能在别的窗口中退出或登录）
		Auth.restore();

		//没有权限
		if(!Auth.pass(UserAgent.currentUri) && !top._$('noPermissionAlert')){
			let topLayer = Layers.getInstanceOfType();
			if(!topLayer
				|| !topLayer.url
				|| (topLayer.url.indexOf('/sso/signup.htm') < 0 && topLayer.url.indexOf('/sso/login.htm') < 0)){
				Auth.showNoPermissionMessage(UserAgent.currentUri);
			}
		}

		Auth.checkPermissionTimer=setTimeout(Auth.checkPermission, 200);
	},

	back:function(){
		top.Layers.close();
	},

	//根据用户IP加密密码
	encrypt:function(s){
		let temp=hex_md5(s);
		return hex_md5(Page.clientMask+temp);
	},

	//获取第三方平台的openid
	getOpenid:function (){
		if(!this.profile || !this.profile.thirdparty || Str.isBlank(this.profile.thirdparty.code)) return null;
		return this.findOpenid(this.profile.thirdparty.code, this.profile.thirdparty.app, this.profile.thirdparty.entity);
	},

	//查找第三方平台的openid
	findOpenid:function (thirdpartyCode, thirdpartyApp, thirdpartyEntity){
		if(!this.profile || !this.profile.thirdpartyUsers) return null;

		for(let i=0; i<this.profile.thirdpartyUsers.length; i++){
			if(this.profile.thirdpartyUsers[i].thirdpartyCode != thirdpartyCode) continue;
			if(!Str.isBlank(thirdpartyApp) && this.profile.thirdpartyUsers[i].thirdpartyApp != thirdpartyApp) continue;
			if(!Str.isBlank(thirdpartyEntity) && this.profile.thirdpartyUsers[i].thirdpartyEntity != thirdpartyEntity) continue;
			return this.profile.thirdpartyUsers[i].thirdpartyOpenid;
		}
		return null;
	},

	//初始化第三方
	initThirdparty:function(){
		if(UserAgent.getUserAgentType()==UserAgent.UA_WECHAT
			||UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI){
			if(!Auth.profile || !Auth.profile.weixinConfig) return;
			loadJS({src:Auth.profile.weixinConfig.jssdk, charset:'utf-8', callback:Auth.initWeixin});
		}

		if(UserAgent.getUserAgentType()==UserAgent.UA_ALIPAY_MINI){
			loadJS({src:'https://appx/web-view.min.js', charset:'utf-8', callback:Auth.initAlipay});
		}else if(UserAgent.getUserAgentType()==UserAgent.UA_ALIPAY){
			setTimeout(Auth.toAliMiniProgram,1000);
		}

		if(UserAgent.getUserAgentType()==UserAgent.UA_DOUYIN_MINI){
			if((typeof tt)=='undefined'){
				loadJS({src:'https://lf3-cdn-tos.bytegoofy.com/obj/goofy/developer/jssdk/jssdk-1.2.1.js', charset:'utf-8', callback:Auth.initDouyin});
			}
		}
	},

	/**
	 * 支付宝环境初始化（依赖的js加载完毕）
	 */
	initAlipayFinished: false,
	initAlipay:function(){
		//if(Auth.initAlipayFinished) return;
		//Auth.initAlipayFinished=true;

		my.onMessage = function(e) {
			if(!e || !e.action) return;
			if(e.action=='scan'){//扫码结果
				if(e.result=='1'){
					Scanner.process(null, e.message);//处理结果
				}
			}
		}
	},
	toAliMiniProgram:function (){
		let toPage=top.location.href;
		let open=Params.getPara('open');
		if(open) toPage=open;

		let jump='alipays://platformapi/startapp?appId=2021001140633701&page=pages/index/index&query='+encodeURIComponent('q='+encodeURIComponent(toPage));
		top.location.href='https://ds.alipay.com/?scheme='+encodeURIComponent(jump);
	},
	alipayNavigate:function (url){
		my.navigateTo({url: url});
	},
	
	/**
	 * 微信环境初始化（依赖的js加载完毕）
	 */
	initWeixinFinished: false,
	initWeixin:function(){
		//if(Auth.initWeixinFinished) return;
		//Auth.initWeixinFinished=true;
		wx.config({
			debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
			appId: Auth.profile.weixinConfig.appId, // 必填，公众号的唯一标识
			timestamp: Auth.profile.weixinConfig.timestamp, // 必填，生成签名的时间戳
			nonceStr: Auth.profile.weixinConfig.nonceStr, // 必填，生成签名的随机串
			signature: Auth.profile.weixinConfig.signature,// 必填，签名，见附录1
			jsApiList: ['checkJsApi',
				'onMenuShareTimeline',
				'onMenuShareAppMessage',
				'onMenuShareQQ',
				'onMenuShareWeibo',
				'onMenuShareQZone',
				'scanQRCode',
				'getBrandWCPayRequest',
				'openLocation'], //必填，需要使用的JS接口列表，所有JS接口列表见附录2
			openTagList:['wx-open-launch-weapp']
		});

		wx.ready(function(){
			// 1 判断当前版本是否支持指定 JS 接口，支持批量判断
			wx.checkJsApi({
				jsApiList:['checkJsApi',
					'onMenuShareTimeline',
					'onMenuShareAppMessage',
					'onMenuShareQQ',
					'onMenuShareWeibo',
					'onMenuShareQZone',
					'scanQRCode',
					'getBrandWCPayRequest',
					'openLocation'],

				success:function(res){}
			});

			// 2. 分享接口
			// 2.1 监听“分享给朋友”，按钮点击、自定义分享内容及分享结果接口
			wx.onMenuShareAppMessage({
				title:Share.sharedTitle,
				desc:Share.sharedDesc,
				link:Share.sharedLink,
				imgUrl:decodeURIComponent(Share.sharedImage),
				trigger:function(res){},
				success:function(res){
					Toast.show(null, 'I{感谢分享}');
				},
				cancel:function (res){},
				fail:function (res){}
			});

			// 2.2 监听“分享到朋友圈”按钮点击、自定义分享内容及分享结果接口
			wx.onMenuShareTimeline({
				title:Share.sharedTitle,
				desc:Share.sharedDesc,
				link:Share.sharedLink,
				imgUrl:decodeURIComponent(Share.sharedImage),
				trigger:function(res){},
				success:function(res){
					Toast.show(null, 'I{感谢分享}');
				},
				cancel:function (res){},
				fail:function (res){}
			});

			// 2.3 监听“分享到QQ”按钮点击、自定义分享内容及分享结果接口
			wx.onMenuShareQQ({
				title:Share.sharedTitle,
				desc:Share.sharedDesc,
				link:Share.sharedLink,
				imgUrl:decodeURIComponent(Share.sharedImage),
				trigger:function(res){},
				success:function(res){},
				cancel:function (res){},
				fail:function (res){}
			});

			// 2.4 监听“分享到微博”按钮点击、自定义分享内容及分享结果接口
			wx.onMenuShareWeibo({
				title:Share.sharedTitle,
				desc:Share.sharedDesc,
				link:Share.sharedLink,
				imgUrl:decodeURIComponent(Share.sharedImage),
				trigger:function(res){},
				success:function(res){},
				cancel:function (res){},
				fail:function (res){}
			});

			// 2.5 监听“分享到QZone”按钮点击、自定义分享内容及分享接口
			wx.onMenuShareQZone({
				title:Share.sharedTitle,
				desc:Share.sharedDesc,
				link:Share.sharedLink,
				imgUrl:decodeURIComponent(Share.sharedImage),
				trigger:function(res){},
				success:function(res){},
				cancel:function (res){},
				fail:function (res){}
			});
		});

		if(UserAgent.getUserAgentType()==UserAgent.UA_WECHAT_MINI
			&& !Str.isBlank(Share.sharedLink)){//小程序环境下逻辑
			wx.miniProgram.postMessage({
				data:{
					title: Share.sharedTitle,
					path: Share.sharedLink,
					imageUrl: decodeURIComponent(Share.sharedImage)
				}
			});
		}
	},
	initDouyin:function(){
		//TODO
	}
}
window.Auth=Auth;

//指定url是否允许访问
Auth.PERMISSION.prototype.pass=function(uri){
	if(Str.matches(uri, this.url, '*') < 0) return true;//不匹配规则
	if(this.excludes && this.excludes.length>0){
		for(let i=0; i<this.excludes.length; i++){
			if(Str.matches(uri, this.excludes[i], '*') > -1) return true;
		}
	}
	return Auth.isRole(this.roles);
}

//机器人检测
let RobotInspector={
	KEY:'0x4AAAAAAAD5NeKQO6UVxvGZ',
	TOKEN:'',
	container:null,
	renderId:null,
	onStart:null,
	onComplete:null,
	onReset:null,

	/**
	 * 是否忽略的客户端类型
	 */
	isIgnoredUserAgent:function (){
		let uaType = UserAgent.getUserAgentType();
		return (uaType == UserAgent.UA_ALIPAY
			||uaType == UserAgent.UA_ALIPAY_MINI
			||uaType == UserAgent.UA_WECHAT
			||uaType == UserAgent.UA_WECHAT_MINI
			||uaType == UserAgent.UA_DOUYIN_MINI);
	},

	/**
	 *
	 * @param container
	 * @param implicit 是否隐式初始化
	 */
	init:function (container, implicit){
		if(this.isIgnoredUserAgent()) return;
		if((typeof container)=='string') container=_$(container);
		this.container=container;

		if(this.onStart) this.onStart.call(window);
		if(implicit){
			let div=document.createElement('div');
			div.className='cf-turnstile';
			container.appendChild(div);
			Utils.setAtt(div, 'data-sitekey', this.KEY);
			Utils.setAtt(div, 'data-callback', 'RobotInspector.onInspected');
			loadJS({src:'https://challenges.cloudflare.com/turnstile/v0/api.js'});
		}else{
			loadJS({src:'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit', callback:RobotInspector.onTurnstileLoaded});
		}
	},

	reset:function(){
		if(this.isIgnoredUserAgent()) return;
		if(Str.isBlank(RobotInspector.renderId)) return;
		if(this.onReset) this.onReset.call(window);
		turnstile.reset(RobotInspector.renderId);
	},

	onTurnstileLoaded:function(){
		if(RobotInspector.isIgnoredUserAgent()) return;
		RobotInspector.renderId=turnstile.render(RobotInspector.container, {
			sitekey: RobotInspector.KEY,
			callback: RobotInspector.onInspected,
			theme: 'light',
			size: 'flexible',
			language: Lang.getCurrentLang().id=='cn'?'zh-cn':'en'
		});
	},

	onInspected:function(token){
		if(RobotInspector.isIgnoredUserAgent()) return;
		console.log('RobotInspector.token='+token);
		RobotInspector.TOKEN=token;
		if(RobotInspector.onComplete) RobotInspector.onComplete.call(window, token);
	}
}
window.RobotInspector=RobotInspector;


//客服
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
			if(__$('JcontentBottom')) __$('JcontentBottom').insertAdjacentHTML('beforebegin', Lang.convert(str));
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
		let H = W.vh() - 54 - 51;
		IFrame.adjustSize('chattingFrame', 0, H);
		_$('chattingContent').style.height=H+'px';
		_$('chatting').style.zIndex=W.getMaxZIndex()+'';
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

//当前页面对象
let Page={
	//安全变量
	clientMask: '',

	//是否已经加载完毕
	loaded:false,

	//主内容容器高度调整
	JcontentH:0,

	//当前请求的处理结果
	result:null,

	//当前执行请求的ajax对象
	currentAjax:null,

	//当前执行文件上传的ajax对象
	currentAjaxForUpload:null,

	//客户端加密用（客户端IP的MD5值）
	clientMask:null,

	//popState事件处理
	onPopState:null,

	//当前页面在哪个Layer中打开
	inLayer:null,

	//最新打开的Layer（仅对顶层窗口有效）
	topLayer:null,

	//标题
	pageTitle:'',

	//include
	includes:[],

	//当前页面的特性，有时候公共函数需根据不同页面的特性进行不同逻辑处理
	pageFeatures:[],

	//操作确认机制
	invokingMethod: null,
	invokingArgs: {},
	invokingConfirm: function (invokingMethod, args, confirmMessage){
		this.invokingArgs=args;
		this.invokingMethod=invokingMethod;

		Dialog.showAlert(Dialog.MSG_TYPE_WARN,
			null,
			null,
			window,
			'I{提示}',
			confirmMessage,
			['<div class="btnH40 btnBgBlue displayBlock" style="width:45%;" onclick="top.Dialog.win.Page.invokingCancelled();">I{取消}</div>',
				'<div class="btnH40 btnBgOrange displayBlock mL5" style="width:45%;" onclick="top.Dialog.win.Page.invokingConfirmed();">I{确定}</div>']);
	},
	invokingCancelled: function(){
		top.Dialog.close();
		this.invokingMethod=null;
		this.invokingArgs={};
	},
	invokingConfirmed: function(){
		top.Dialog.close();
		this.invokingMethod.call(window, this.invokingArgs);
		this.invokingMethod=null;
		this.invokingArgs={};
	},
	//操作确认机制 end

	hasPageFeature:function(feature){
		return Str.contains(this.pageFeatures,feature);
	},

	hasPageFeatureOf:function(features){
		for(let i=0;i<features.length;i++){
			if(Str.contains(this.pageFeatures, features[i])) return true;
		}
		return false;
	},

	//popState事件处理默认实现
	onPopStateDefault:function(e){
		top.Layers.close();
	},

	//是否有id为content的可滚动容器
	hasScrollableContainer:function (){
		return __$('Jcontent') && 'scroll'==Utils.getStyle(__$('Jcontent'), 'overflowY');
	},

	//设置元素可见性
	setVisibility:function(){
		let tags=['div','a','tr','table'];
		for(let i=0; i<tags.length; i++){
			let elements=document.getElementsByTagName(tags[i]);
			for(let j=0; j<elements.length; j++){
				let roles=Utils.att(elements[j], 'roles');
				if(Str.isBlank(roles)) continue;

				if(!Auth.isRole(roles.split(','))){
					elements[j].style.display='none';
				}else if(elements[j].style.display=='none'){
					elements[j].style.display='';
				}
			}
		}
	},

	//调整内容高度
	adjustContentHeight:function(){
		if(_$('Jcontent') && _$('Jcontent2Top')){
			let element=_$('Jcontent');
			let overflowY=Utils.getStyle(element, 'overflowY');
			if(Str.isBlank(overflowY) || overflowY=='scroll' || overflowY=='auto'){
				if(W.elementScrollHeight(element) > W.elementHeight(element) && element.scrollTop>0){
					_$('Jcontent2Top').style.visibility='visible';
				}else{
					_$('Jcontent2Top').style.visibility='hidden';
				}
			}
		}

		if((typeof JSelectors) != 'undefined' && JSelectors.shown()) return;
		if(__$('Jcontent')){
			let contentHeightAdjust=0;
			let _JcontentH=Params.getJSPara('/framework/js/main.js', "JcontentH");
			if(_JcontentH && !isNaN(_JcontentH*1)){//通过js后跟参数指定了content的高度（需要从整体页面高度减去这个值）
				contentHeightAdjust=_JcontentH*1;
			}else if(Page.JcontentH>0){
				contentHeightAdjust=Page.JcontentH;
			}else{
				if(Utils.visible(__$('Jheader'), false)) contentHeightAdjust+=W.elementHeight(__$('Jheader'));
				if(Utils.visible(__$('Jfooter'), false)) contentHeightAdjust+=W.elementHeight(__$('Jfooter'));
			}

			let vh = Page.inLayer ? Page.inLayer.getHeight() : W.vh();
			//console.log(UserAgent.currentUri+' ---=> '+vh+' = '+contentHeightAdjust);
			__$('Jcontent').style.height=(vh - contentHeightAdjust)+'px';
		}
	},

	//include指令个数
	includes:0,

	//处理include指定
	include:function(){
		let _includes=_$cls('include');
		if(!_includes || _includes.length==0 || 'true'==Params.getPara('disableIncludes')){
			Page.init();//获取初始化信息和用户信息
			return;
		}

		for(let i=0; i<_includes.length; i++){
			let src=Utils.att(_includes[i], 'src');
			let js=Utils.att(_includes[i], 'js');
			if(!Str.isBlank(src)) Page.includes++;
			if(!Str.isBlank(js)) Page.includes++;
		}

		for(let i=0; i<_includes.length; i++){
			let src=Utils.att(_includes[i], 'src');
			let js=Utils.att(_includes[i], 'js');
			if(!Str.isBlank(src)){
				src = Str.replaceAll(src, 'BUSINESS_FIELD', Fields.currentField+'');
				src = Str.replaceAll(src, 'UA', UserAgent.isPC()?'pc':'mobile');
				let ajax=new Ajax();
				ajax.container=_includes[i];
				ajax.send('GET',Page.doInclude,src);
			}

			if(!Str.isBlank(js)){
				js = Str.replaceAll(js, 'BUSINESS_FIELD', Fields.currentField+'');
				js = Str.replaceAll(js, 'UA', UserAgent.isPC()?'pc':'mobile');
				Logger.log('load js along with include -> '+js);
				loadJS({'src':js, 'callback':Page.doIncludeJs});
			}
		}
	},
	doInclude:function(ajax){
		if(ajax.getReadyState()==4){
			Page.includes--;
			try{
				if(ajax.getStatus()==200){
					ajax.container.innerHTML=ajax.getResponseText();
				}
			}catch(e){
				if(Global.DEBUG) Logger.log(e);
			}
			if(Page.includes==0) Page.init();//获取初始化信息和用户信息
		}
	},
	doIncludeJs:function(src){
		if(!Page.includes[src]){
			Page.includes[src]=true;
			Page.includes--;
			if(Page.includes==0) Page.init();//获取初始化信息和用户信息
		}
	},

	//获取初始化信息和用户信息
	init:function (){
		//解析js后面跟的参数
		Params.initJSParams();

		//回到顶部图标
		if(_$('Jcontent2Top') && _$('Jcontent')){
			_$('Jcontent2Top').addEventListener('click', function(event) {
				_$('Jcontent').scrollTop=0;
			});
		}

		if((top==window || (typeof top.Auth)=='undefined')
			&& !Params.getJSPara('/framework/js/main.js', "ignore_profile")){
			let ajax=new Ajax();
			ajax.send('GET', Page.doInit, '/api/platform/user/profile?t='+(new Date()).getTime()+'&I18NGroups='+encodeURIComponent(','+UserAgent.currentUri));
		}else{
			Auth.profile=top.Auth.profile;
			Page.onload();//核心功能、组件初始化
		}
	},

	doInit:function(ajax){
		if(ajax.getReadyState()==4&&ajax.getStatus()==200) {
			Auth.doProfile(ajax, ajax.getResponseJson());

			//核心功能、组件初始化；加载js模块；调用自定义onReady方法（实现具体业务逻辑的初始化）
			Page.onload();
		}
	},

	//窗口初始化
	onload:function(){
		//调整内容高度
		this.adjustContentHeight();
		if(__$('Jcontent')) {
			setTimeout(Page.adjustContentHeight, 200);
			setInterval(Page.adjustContentHeight, 1000);
		}

		//自动插入内容区底部元素
		if (Page.hasScrollableContainer() && !__$('JcontentBottom')) {
			let contentBottom = document.createElement('div');
			contentBottom.id = 'JcontentBottom';
			__$('Jcontent').appendChild(contentBottom);
		}

		//设置元素可见性
		Page.setVisibility();

		//监听返回键
		window.addEventListener("popstate", function (e) {
			if (Page.onPopState) Page.onPopState(e);
			else Page.onPopStateDefault(e);
		});

		//移动对象
		if(UserAgent.isPC()){
			window.addEventListener("mousemove", function (e) {
				if (MovableCurrent) MovableCurrent.move(e);
			});
			window.addEventListener("mouseup", function (e) {
				if (MovableCurrent) MovableCurrent.end(e);
			});
		}else{
			window.addEventListener("touchmove", function (e) {
				if (MovableCurrent) MovableCurrent.move(e);
			});
			window.addEventListener("touchend", function (e) {
				if (MovableCurrent) MovableCurrent.end(e);
			});
		}

		//多语言模块
		Lang.regComponent('Global', Global);
		Lang.regComponent('AjaxSetting', AjaxSetting);
		Lang.regComponent('Fields', Fields);
		Lang.regComponent('JWebSocketSetting', JWebSocketSetting);
		Lang.regComponent('D', D);

		//Layers初始化
		Layers.init();

		//多币种初始化
		Currency.refreshRatePeriodic();

		//加载js模块
		Modules.load();

		if(Modules.loadingModules == 0) {
			if (UserAgent.currentUri == '/'
				|| UserAgent.currentUri == '/index.htm'
				|| UserAgent.currentUri == '/index.html') {
				top.Dialog.close();
			}

			//多语言初始化
			Lang.init();

			try {
				//检查访问权限
				if(!top.Auth.pass(UserAgent.currentUri)) {
					top.Auth.checkPermission();
				}else{
					Logger.log('onReady(no js modules)........');
					Page.onReady();
					//Cookie.showPolicies();
				}
			} catch (e) {
				Logger.log(e);
			}
			Logger.log('page loaded.');
		}

		return null;
	},

	//一切就绪
	onReady:function(){
		if((typeof onReady)=='function'){
			try{
				onReady();
			}catch (e){
				console.log(e);
			}

			try{
				if((typeof headerOnReady)=='function') headerOnReady();
			}catch (e){
				console.log(e);
			}
		}
		Page.loaded=true;
	},

	//保持状态
	pushState:function(state, title, url){
		/**
		 1，state --  可以是任意可以被序列化的js对象，浏览器会把这个信息存储到用户的物理存储上，最大size 640K。
		 2，title --  目前浏览器是忽略这个参数的，但是可能会在将来用到，所以建议传递一个空字符串
		 3，URL -- 新历史记录的url，需要注意的是 浏览器并不会尝试加载这个url， url可以是绝对的也可以是相对的，url必须个当前页面同源，不然会报异常。
		 */
		top.history.pushState(state, title, url);
	},

	//刷新页面
	reload:function(){
		location.href=location.href;
	},

	//刷新页面
	reloadIfOk:function(){
		try{
			if(Page.result && Page.result=='1'){
				location.href=location.href;
			}
		}catch(e){}
	},

	//关闭页面
	close:function(){
		window.close();
	},

	//心跳
	heartbeat:function(){
		let ajax=new Ajax();
		ajax.send('GET',Page.doHeartbeat,'/blank.htm?t='+Math.random());
	},

	doHeartbeat:function(ajax){},

	//禁止嵌入
	toTop:function(toUrl){
		try{
			if(top.location.href!=location.href){
				top.location.href=toUrl?toUrl:location.href;
			}
		}catch(e){
			document.body.insertAdjacentHTML('afterbegin', '<form name="toTop" action="'+(toUrl?toUrl:location.href)+'" target="_top" method="get"></form>');
			toTop.submit();
		}
	},

	//返回首页
	home:function(url){
		top.location.href=Str.isBlank(url)?'/':url;
	},

	//to
	to:function(url){
		location.href=Str.isBlank(url)?'/':url;
	},

	//alert
	alert:function(message, title,  btns, _type, _onClose, _onInterrupt){
		Dialog.canClose=true;
		if(Dialog.isOpen()) Dialog.close();

		if(Str.isBlank(_type)) _type=Dialog.MSG_TYPE_INFO;
		if(Str.isBlank(title)) title='I{提示}';

		let btnStyle='';
		if(_type==Dialog.MSG_TYPE_OK) btnStyle=' btnBgGreen';
		else if(_type==Dialog.MSG_TYPE_WARN) btnStyle=' btnBgOrange';
		else if(_type==Dialog.MSG_TYPE_ERR) btnStyle=' btnBgRed';
		else if(_type==Dialog.MSG_TYPE_FATAL) btnStyle=' btnBgRed';
		else btnStyle=' btnBgOrange';

		if(!btns) btns=['<div class="btnH40'+btnStyle+'" onclick="Dialog.close();">'+Global.textOk+'</div>'];
		if(Array.isArray(btns)) btns=btns.join('');

		Dialog.showAlert(_type,
			_onClose,
			_onInterrupt,
			window,
			Lang.convert(title),
			Lang.convert(message),
			Lang.convert(btns));
	},

	//input输入事件绑定所有实例
	InputEvents:[],

	//事件绑定
	bindClickHandler:function(elements){
		for(let i=0; i<elements.length; i++){
			if(!elements[i]) continue;
			let sCallback=Utils.att(elements[i], 'Jcallback');
			let callback=sCallback ? eval(sCallback) : Page.open;
			elements[i].addEventListener('click', function(e){
				callback(e);
			});
		}
	},

	//通用打开页面功能
	open:function(e){
		let o=Utils.getEventTarget(e);
		if(!e) return;

		let openType=Utils.att(o, 'open-type');
		if(!openType) return;

		let url=Utils.att(o, 'data-url');
		if(!url) return;

		if(openType.toLowerCase()=='layer'){
			let name=Utils.att(o, 'data-name');
			if(!name) name='';
			else name=Lang.convert(name);

			Layers.open(window, name, url);
		}else if(openType.toLowerCase()=='top'){
			top.location.href=url;
		}else if(openType.toLowerCase()=='window'){
			window.open(url);
		}
	},

	//隐藏常用小组件
	hideWidgets:function(){
		Currency.hideCurrencySelector();
		Lang.hideLangSelector();
		TimeZones.hideTimeZoneSelector();
		Fields.hideFieldSelector();
	}
}
window.Page=Page;

//js功能模块
let Modules={
	loadingModules:-1,
	loadedModules:[],
	definedModules:[],
	load:function(){
		let jsModules=Params.getJSPara('/framework/js/main.js', "modules");
		let i18nModules=Params.getJSPara('/framework/js/main.js', "I18N");
		if(Str.isBlank(jsModules) && (typeof i18nModules)=='undefined'){
			this.loadingModules=0;
			return;
		}

		this.loadingModules=0;
		if((typeof jsModules)!='undefined'){
			jsModules=jsModules.split(',');
			for(let i=0; i<jsModules.length; i++) {
				if(Modules.definedModules[jsModules[i]]
					|| jsModules[i].endsWith('.js')
					|| jsModules[i].endsWith('.css')) this.loadingModules++;
			}
		}

		if((typeof i18nModules)!='undefined'){
			i18nModules=i18nModules.split(',');
			this.loadingModules+=i18nModules.length;
		}

		//加载js模块
		if(Array.isArray(jsModules) && jsModules.length>0) {
			for (let i = 0; i < jsModules.length; i++) {
				let src = jsModules[i];
				if(Modules.definedModules[src]) src = Modules.definedModules[src];
				src = Str.replaceAll(src, 'BUSINESS_FIELD', Fields.currentField+'');
				src = Str.replaceAll(src, 'UA', UserAgent.isPC()?'pc':'mobile');
				if(src.endsWith('.css')){
					Logger.log('load css -> ' + src);
					loadCSSFromUrl({'src': src, 'callback': Modules.onModuleLoad});
				}else{
					Logger.log('load js -> ' + src);
					loadJS({'src': src, 'callback': Modules.onModuleLoad});
				}
			}
		}

		//自动引入与当前url对应的多语言资源
		if(Array.isArray(i18nModules) && i18nModules.length>0) {
			for (let i = 0; i < i18nModules.length; i++) {
				let src = '';
				if (Str.isBlank(i18nModules[i])) src = '/framework/I18N' + (UserAgent.currentUri == '/' ? '/index' : UserAgent.currentUri) + '.js';
				else src = '/framework/I18N' + i18nModules[i];
				Logger.log('load I18N module -> ' + src);
				loadJS({'src': src, 'callback': Modules.onModuleLoad});
			}
		}
	},

	onModuleLoad:function(src){
		if(!Modules.loadedModules[src]){
			Modules.loadedModules[src]=true;
			Modules.loadingModules--;
		}
		if(Modules.loadingModules==0){
			try{
				if(UserAgent.currentUri=='/'
					||UserAgent.currentUri=='/index.htm'
					||UserAgent.currentUri=='/index.html'){
					top.Dialog.close();
				}

				//多语言初始化
				Lang.init();

				//检查访问权限
				if(!top.Auth.pass(UserAgent.currentUri)) {
					top.Auth.checkPermission();
				}else{
					Logger.log('onReady........');
					Page.onReady();
					//Cookie.showPolicies();
				}
			}catch(e){
				Logger.log(e);
			}
		}
		if(Modules.loadingModules<0) Modules.loadingModules=0;
	}
}
window.Modules=Modules;

//功能模块
Modules.definedModules['Base64']='/framework/js/Base64.js';//Base64
Modules.definedModules['MD5']='/framework/js/jshash/md5-min.js'//Md5
Modules.definedModules['ImageZoom']='/framework/js/ImageZoom.js';//PC端“查看大图”功能
Modules.definedModules['PasswordViewer']='/framework/js/PasswordViewer.js';//密码查看组件（小眼睛）
Modules.definedModules['CascadingMenu']='/framework/js/CascadingMenu.js';//多级菜单
Modules.definedModules['MultiDimensionData']='/framework/js/MultiDimensionData.js';//多维数据表格
Modules.definedModules['Countries']='/framework/js/Countries.js';//全球各国及对应的手机/电话输入组件&格式校验
Modules.definedModules['Region']='/framework/js/Region.js';//地域选择组件
Modules.definedModules['LBS']='/framework/js/LBS.js';//地图与定位
Modules.definedModules['Catalogs']='/framework/js/Catalogs.js';
Modules.definedModules['JEditor']='/framework/js/JEditor.js';
Modules.definedModules['ColorPicker']='/framework/js/ColorPicker.js';
Modules.definedModules['Messager']='/framework/js/Messager.js';
Modules.definedModules['Scanner']='/framework/js/Scanner.js';
Modules.definedModules['Selector']='/framework/js/Selector.js';
Modules.definedModules['Panel']='/framework/js/Panel.js';
Modules.definedModules['Picker']='/framework/js/Picker.js';
Modules.definedModules['Calendar']='/framework/js/Calendar.js';
Modules.definedModules['WebSocket']='/framework/js/WebSocket.js';
Modules.definedModules['Media']='/framework/js/Media.js';
Modules.definedModules['Touch']='/framework/js/Touch.js';
Modules.definedModules['Movable']='/framework/js/Movable.js';
Modules.definedModules['Sorter']='/framework/js/Sorter.js';
Modules.definedModules['IMG']='/framework/js/IMG.js';
Modules.definedModules['Live']='/framework/js/Live.js';
Modules.definedModules['DataSheet']='/framework/js/DataSheet.js';

//全局多语言资源
Lang.setStrings({
	groups: [
		{
			group: "",
			tags: [
				{tag: "该版本在您当前所在的国家不可用", texts: { en: "This version is not available in your country"}},
				{tag: "公司名", texts: { en: "TEENWEAR", cn: "TEENWEAR"}},
				{tag: "正在加载数据", texts: { en: "Loading Data..."}},
				{tag: "是", texts: { en: "Yes"}},
				{tag: "否", texts: { en: "No"}},
				{tag: "可选", texts: { en: "Optional"}},
				{tag: "必选", texts: { en: "Required"}},
				{tag: "确定", texts: { en: "OK"}},
				{tag: "提交", texts: { en: "Submit"}},
				{tag: "完成", texts: { en: "Done"}},
				{tag: "取消", texts: { en: "Cancel"}},
				{tag: "清除", texts: { en: "Clear"}},
				{tag: "关闭", texts: { en: "Close"}},
				{tag: "返回", texts: { en: "Back"}},
				{tag: "搜索", texts: { en: "Search"}},
				{tag: "搜索商品", texts: { en: "Search"}},
				{tag: "请登录系统", texts: { en: "Please login"}},
				{tag: "请登录", texts: { en: "Please login"}},
				{tag: "请登录后再进行操作", texts: { en: "Please login before operation"}},
				{tag: "提示", texts: { en: "Message"}},
				{tag: "同意", texts: { en: "Agree"}},
				{tag: "接受", texts: { en: "Accept"}},
				{tag: "拒绝", texts: { en: "Disagree"}},
				{tag: "手机号码", texts: { en: "Phone number"}},
				{tag: "处理超时", texts: { en: "Processing timeout"}},
				{tag: "返回首页", texts: { en: "Home Page"}},
				{tag: "刷新页面", texts: { en: "Refresh Page"}},
				{tag: "刷新", texts: { en: "Refresh"}},
				{tag: "添加", texts: { en: "Add"}},
				{tag: "选择地点", texts: { en: "Select Region"}},
				{tag: "已复制", texts: { en: "Copied"}},
				{tag: "复制", texts: { en: "Copy"}},
				{tag: "用户中心", texts: { en: "My Account"}},
				{tag: "注册", texts: { en: "Sign Up"}},
				{tag: "立即加入", texts: { en: "Join Now"}},
				{tag: "输入您的邮箱", texts: { en: "Enter your email"}},
				{tag: "登录", texts: { en: "Log In"}},
				{tag: "您还不是商户", texts: { en: "You are not a merchant yet"}},
				{tag: "您未登录或没有该操作权限", texts: { en: "You are not login or have no permission"}},
				{tag: "请选择行业", texts: { en: "Business Field"}},
				{tag: "请选择业务角色", texts: { en: "Business Role"}},
				{tag: "参数无效", texts: { en: "Parameters invalid"}},
				{tag: "记录不存在", texts: { en: "The record is not exists"}},
				{tag: "操作成功", texts: { en: "Operation succeeded"}},
				{tag: "更新成功", texts: { en: "Updated successfully"}},
				{tag: "删除成功", texts: { en: "Deleted successfully"}},
				{tag: "移除成功", texts: { en: "Removed successfully"}},
				{tag: "添加成功", texts: { en: "Added successfully"}},
				{tag: "没有操作权限", texts: { en: "No permission"}},
				{tag: "成功", texts: { en: "Succeeded"}},
				{tag: "系统错误", texts: { en: "System error"}},
				{tag: "很不幸，您遇到了传说中的404", texts: { en: "Unfortunately, you met the 404"}},
				{tag: "开始提交", texts: { en: "Submitting"}},
				{tag: "文件大小超出限制", texts: { en: "The file size exceeds the limit"}},
				{tag: "文件总大小超出限制", texts: { en: "Total size of files exceeds the limit"}},
				{tag: "您当前浏览器不支持上传剪裁图片", texts: { en: "The client does not support uploading clip pictures"}},
				{tag: "图片加载中，请几秒后再试", texts: { en: "The picture is loading, please try again in a few seconds"}},
				{tag: "总记录数", texts: { en: "Total"}},
				{tag: "等待超时，请重试", texts: { en: "Processing timeout"}},
				{tag: "编辑", texts: { en: "Edit"}},
				{tag: "Cookie使用条款", texts: { en: "Cookie Policies"}},
				{tag: "隐私条款", texts: { en: "Privacy Policies"}},
				{tag: "可左右滑动", texts: { en: "Slide left / right to switch the tabs"}},
				{tag: "选择文件", texts: { en: "Pick file"}},
				{tag: "请选择文件", texts: { en: "Please pick a file"}},
				{tag: "文件类型不合法", texts: { en: "Invalid file type"}},
				{tag: "图片压缩中，请等待2秒后重试", texts: { en: "Compressing images, please retry after few seconds"}},
				{tag: "一次只能上传一个文件", texts: { en: "Only one file can be uploaded at one time"}},
				{tag: "查看大图", texts: { en: "View Original"}},
				{tag: "查看原图", texts: { en: "View Original"}},
				{tag: "播放视频", texts: { en: "Play Video"}},
				{tag: "基准币种提示", texts: { en: "We use CNY for settlement. When you choose another currency, the system converts it based on real-time exchange rates. When you make payments, withdrawals, apply for refunds, etc., the final amount is based on CNY.", cn: "本站使用人民币（CNY）进行结算，当您选择其它币种时，系统根据实时汇率进行转换，当您进行付款、提款、申请退款等操作时，最终核算金额以人民币为准。"}},
				{tag: "客服中心", texts: { en: "Customer Service Center"}},
				{tag: "客服", texts: { en: "Customer Service"}},
				{tag: "分享", texts: { en: "Share"}},
				{tag: "处理超时，请稍后检查处理结果", texts: { en: "Processing timeout, please check the result later"}},
				{tag: "空空如也", texts: { en: "No Data"}},
				{tag: "领取成功", texts: { en: "Got successfully"}},
				{tag: "常见问题", texts: { en: "Q&A"}},
				{tag: "Talk To Us", texts: { cn: "聊聊吧"}},
				{tag: "Maybe later", texts: { cn: "稍后再说"}},
				{tag: "Can I help you?", texts: { cn: "需要帮忙吗"}},
				{tag: "下载附件", texts: { en: "Download"}},
				{tag: "查看订单", texts: { en: "View Orders"}},
				{tag: "非法操作", texts: { en: "Illegal operation"}},
				{tag: "上传成功", texts: { en: "Illegal operation"}},
				{tag: "相册不存在", texts: {en: "Album not exists"}},
				{tag: "相册密码错误", texts: {en: "The password used to view the album is incorrect"}},
				{tag: "相册未公开", texts: {en: "The album is not public"}},
				{tag: "AI体验", texts: {en: "AI Playground"}}
			]
		},
		{
			group: "header",
			tags: [
				{tag: "我的", texts: { en: "MY"}},
				{tag: "设置", texts: { en: "Settings"}},
				{tag: "扫一扫", texts: { en: "Scanner"}},
				{tag: "在线客服", texts: { en: "Online Service"}},
				{tag: "我的账号", texts: { en: "My Account"}},
				{tag: "账号安全", texts: { en: "Security"}},
				{tag: "未登录", texts: { en: "Please login"}},
				{tag: "登录密码", texts: { en: "Login PWD"}},
				{tag: "支付密码", texts: { en: "Payment PWD"}},
				{tag: "收货地址", texts: { en: "Shipping Address"}},
				{tag: "消息订阅", texts: { en: "Notification Preference"}},
				{tag: "服务支持", texts: { en: "Support"}},
				{tag: "联系我们", texts: { en: "Contact Us"}},
				{tag: "登录", texts: { en: "Log In"}},
				{tag: "注册", texts: { en: "Sign Up"}},
				{tag: "退出登录", texts: { en: "Log Out"}},
				{tag: "注册 / 登录", texts: { en: "Sign Up / Log In"}},
				{tag: "我的订单", texts: { en: "My Orders"}},
				{tag: "传送门", texts: { en: "Warp gate" }},
				{tag: "第三方登录绑定", texts: { en: "Thirdparty login binding" }}
			]
		},
		{
			group: "footer",
			tags: [
				{tag: "我的", texts: { en: "MY"}},
				{tag: "首页", texts: { en: "HOME"}},
				{tag: "分类", texts: { en: "CATEGORY"}},
				{tag: "购物车", texts: { en: "CART"}},
				{tag: "商户中心", texts: { en: "Seller Center"}},
				{tag: "用户中心", texts: { en: "User Center"}},
				{tag: "注册", texts: { en: "Sign up"}},
				{tag: "登录", texts: { en: "Login"}},
				{tag: "搜索", texts: { en: "SEARCH"}},
				{tag: "订单", texts: { en: "ORDERS"}},
				{tag: "我的订单", texts: { en: "My Orders"}},
				{tag: "方案", texts: { en: "SOLUTION"}},
				{tag: "博客", texts: { en: "BLOG"}},
				{tag: "教程", texts: { en: "COURSE" }},
				{tag: "产品", texts: { en: "PRODUCT" }}
			]
		},
		{
			group: "thirdparty",
			tags: [
				{tag: "微信", texts: { en: "Wechat"}},
				{tag: "Google", texts: { en: "Google"}},
				{tag: "抖音", texts: { en: "Douyin"}},
				{tag: "支付宝", texts: { en: "Alipay"}},
				{tag: "解除绑定", texts: { en: "Unbind"}},
				{tag: "确定要解除以下第三方绑定吗", texts: { en: "Are you sure to unbind the following binding"}},
				{tag: "为避免账号丢失，您需要先设置手机号或邮箱才能解除绑定。", texts: { en: "To avoid account loss, you need to set your phone number or email address first before unbinding."}},
				{tag: "成功解除绑定", texts: { en: "Successfully unbound"}}
			]
		},
		{
			group: "websocket",
			tags: [
				{tag: "当前环境不支持通信方式", texts: { en: "The client does not support this communication mode"}},
				{tag: "连接成功", texts: { en: "Connection established"}},
				{tag: "连接关闭", texts: { en: "Connection closed"}},
				{tag: "通信错误", texts: { en: "Communication error" }}
			]
		},
		{
			group: "calendar",
			tags: [
				{tag: "SUN", texts: { cn: "日"}},
				{tag: "MON", texts: { cn: "一"}},
				{tag: "TUE", texts: { cn: "二"}},
				{tag: "WED", texts: { cn: "三"}},
				{tag: "THU", texts: { cn: "四"}},
				{tag: "FRI", texts: { cn: "五"}},
				{tag: "SAT", texts: { cn: "六"}},
				{tag: "SUNDAY", texts: { cn: "星期日", en: "sunday"}},
				{tag: "MONDAY", texts: { cn: "星期一", en: "monday"}},
				{tag: "TUESDAY", texts: { cn: "星期二", en: "tuesday"}},
				{tag: "WEDNESDAY", texts: { cn: "星期三", en: "wednesday"}},
				{tag: "THURSDAY", texts: { cn: "星期四", en: "thursday"}},
				{tag: "FRIDAY", texts: { cn: "星期五", en: "friday"}},
				{tag: "SATURDAY", texts: { cn: "星期六", en: "saturday"}},
				{tag: "请选择允许的日期", texts: { cn: "Please select the allowed date"}},
				{tag: "请选择日期", texts: { cn: "Please select a date"}},
				{tag: "天", texts: { en: "Days"}},
				{tag: "小时", texts: { en: "Hours"}},
				{tag: "分", texts: { en: "Minutes"}},
				{tag: "秒", texts: { en: "Seconds" }}
			]
		},
		{
			group: "express",
			tags: [
				{tag: "中通", texts: { en: "ZTO"}},
				{tag: "顺丰", texts: { en: "SF"}},
				{tag: "燕文物流", texts: { en: "Global Logistics" }}
			]
		},
		{
			group: "ImageViewer",
			tags: [
				{tag: "暂无相关照片或视频", texts: { en: "No photos or videos"}},
				{tag: "没有可浏览的图片或视频", texts: { en: "No photos or videos"}},
				{tag: "双指缩放，双击还原", texts: { en: "Zoom by two fingers, double click to restore"}},
				{tag: "左右滑动切换图片或视频", texts: { en: "Swipe left and right to switch medias"}},
				{tag: "编辑", texts: { en: "Edit"}},
				{tag: "宽高比例（不限）", texts: { en: "Ratio(unlimited)"}},
				{tag: "旋转", texts: { en: "Rotate"}},
				{tag: "剪裁", texts: { en: "Trim"}},
				{tag: "取消", texts: { en: "Cancel"}},
				{tag: "完成", texts: { en: "Done" }}
			]
		},
		{
			group: "scanner",
			tags: [
				{tag: "未扫描到有效内容", texts: { en: "No valid content was scanned"}},
				{tag: "扫描", texts: { en: "Scanning"}},
				{tag: "处理出错", texts: { en: "Processing error"}},
				{tag: "已扫描，正在处理数据", texts: { en: "Scanned, processing data"}},
				{tag: "复制扫描结果", texts: { en: "Copy scanned results"}},
				{tag: "扫描结果", texts: { en: "Scanned results" }}
			]
		},
		{
			group: "share",
			tags: [
				{tag: "分享", texts: { en: "Share"}},
				{tag: "复制链接", texts: { en: "Copy link"}},
				{tag: "向您推荐", texts: { en: "recommend"}},
				{tag: "我向您推荐", texts: { en: "I recommend "}},
				{tag: "分享海报", texts: { en: "Share poster"}},
				{tag: "切换轮播图，当前照片将用于海报的主图。", texts: { en: "Switch the sliding photos, the current photo will be used for the main photo of poster." }}
			]
		},
		{
			group: "category",
			tags: [
				{tag: "全部产品", texts: { en: "All Products"}},
				{tag: "男装", texts: { en: "Men's wear"}},
				{tag: "女装", texts: { en: "Women's wear"}},
				{tag: "内裤", texts: { en: "Underpants"}},
				{tag: "短裤/卫裤", texts: { en: "Sportswear"}},
				{tag: "居家服", texts: { en: "Homewear"}},
				{tag: "秋衣秋裤", texts: { en: "Thermals"}},
				{tag: "T恤", texts: { en: "T-shirt"}},
				{tag: "卫衣", texts: { en: "Hoodie"}},
				{tag: "答疑解惑", texts: { en: "Problem solving"}},
				{tag: "解惑答疑", texts: { en: "Problem solving"}},
				{tag: "AI探索", texts: { en: "AI exploration"}},
				{tag: "JAVA进阶", texts: { en: "JAVA advanced"}},
				{tag: "原创框架", texts: { en: "Original framework"}},
				{tag: "解决方案", texts: { en: "Solutions"}},
				{tag: "实用代码", texts: { en: "Utils" }},
				{tag: "技术探索", texts: { en: "Research" }},
				{tag: "前沿技术", texts: { en: "Frontier Tech" }}
			]
		},
		{
			group: "vip",
			tags: [
				{tag: "VIP1", texts: { cn: "新新人类", en: "Tourist"}},
				{tag: "VIP2", texts: { cn: "初级会员", en: "Newcomer"}},
				{tag: "VIP3", texts: { cn: "白银会员", en: "Starter"}},
				{tag: "VIP4", texts: { cn: "黄金会员", en: "Senior"}},
				{tag: "VIP5", texts: { cn: "白金会员", en: "Silver"}},
				{tag: "VIP6", texts: { cn: "黑金会员", en: "Golden"}},
				{tag: "VIP7", texts: { cn: "钻石会员", en: "Diamond"}}
			]
		},
		{
			group: "field",
			tags: [
				{tag: "大叔的光", texts: { en: "Light of uncle"}},
				{tag: "百货", texts: { en: "Clothing"}},
				{tag: "鲜花", texts: { en: "Flowers"}},
				{tag: "餐饮", texts: { en: "Food"}},
				{tag: "创意", texts: { en: "Creation"}},
				{tag: "技术咨询", texts: { en: "Consulting"}},
				{tag: "盖聂大叔原创技术BLOG", texts: { en: "Original tech blog of Uncle GeNie"}},
				{tag: "大叔的光-盖聂大叔原创技术BLOG", texts: { en: "Light of uncle - original tech blog of Uncle GeNie"}}
			]
		},
		{
			group: "live",
			tags: [
				{tag: "暂无直播", texts: { en: "No live broadcast"}}
			]
		}
	]
});

//控制权限
new Auth.PERMISSION('/user/*.htm', ['ROLE_USER'], []);
new Auth.PERMISSION('/shopping/zone.htm', ['ROLE_USER'], []);
new Auth.PERMISSION('/shopping/cart.htm', ['ROLE_USER','ROLE_SYS','ROLE_ADMIN'], []);
new Auth.PERMISSION('/security/*.htm', ['ROLE_USER'], ['/security/password.htm']);
new Auth.PERMISSION('/tool/ai/*.htm', ['ROLE_USER'], []);
new Auth.PERMISSION('/manage/*.htm',
	['ROLE_SYS','ROLE_ADMIN','ROLE_FINANCE','ROLE_ORDER','ROLE_GOODS','ROLE_MARKET','ROLE_SERVICE'],
	['/manage/login.htm'],
	'/manage/login.htm',
	'top');
new Auth.PERMISSION('/ai/*.htm', ['ROLE_USER'], []);

//客户端初始化
UserAgent.init();
document.body.onload = function(){
	//解析url参数
	Params.init();
	if(!Str.isBlank(Params.getPara('referer'))){
		let spreader=Params.getPara('referer');
		if(spreader.match(/[A-Za-z0-9]{4,12}/)){
			Cookie.set('spreader', spreader);
			UserAgent.spreader=spreader;
		}
	}

	Currency.restore();
	Fields.restore();
	Lang.restore();
	Auth.restore();

	console.log('accessToken on init = '+Auth.accessToken);
	if(UserAgent.currentUri=='/'
		||UserAgent.currentUri=='/index.htm'
		||UserAgent.currentUri=='/index.html'){
		top.Dialog.open(-1,-1,-1,-1,null,null,window,'waiting','','',true);
	}

	//初始化顺序：处理include标志
	// -> 获取基础数据和用户信息（仅顶层窗口）
	// -> 核心功能、组件初始化
	// -> 加载js模块
	// -> 调用自定义onReady方法（实现具体业务逻辑的初始化）
	Page.include();
}

let windowResizingTimer = null;
window.addEventListener('resize', function(event) {
	if((typeof onWindowResize)=='function'){
		if(windowResizingTimer){
			clearTimeout(windowResizingTimer);
			windowResizingTimer=null;
		}
		windowResizingTimer = setTimeout(onWindowResize, 100);
	}
});

window.addEventListener('touchend', function(event) {
	for(let i in touchTargets) touchTargets[i].stop();
	for(let i in Movables) Movables[i].cancel();
});

window.addEventListener('mouseup', function(event) {
	for(let i in touchTargets) touchTargets[i].stop();
	for(let i in Movables) Movables[i].cancel();
});