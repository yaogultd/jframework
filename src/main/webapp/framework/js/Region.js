/**
 * 国家或地区
 * @param level 0：国家/地区，1：省份/州，2：城市，3：区县，4：乡镇/街道
 * @param id
 * @param code
 * @param nameCn
 * @param nameEn
 * @param nameCnFull
 * @constructor
 */
function Region(level, id, code, nameCn, nameEn, nameCnFull){
	this.level=level;
	this.id=id;
	this.code=code;
	this.nameCn=nameCn;
	this.nameEn=nameEn;
	this.nameCnFull=nameCnFull;
	this.children=[];
	this.childrenLoaded=level==3?false:true;//下级区域（乡镇或街道）是否已经加载
}

/**
 * 是否直辖市
 */
Region.prototype.isMunicipality=function(){
	return this.level==1 && (Str.exists(this.nameCn, ['重庆', '北京', '上海', '天津']))
}

/**
 * 区县添加添加下级乡镇/街道
 * @param province
 */
Region.prototype.addZone=function(level, id, code, nameCn, nameEn){
	let r=new Region(level, id, code, nameCn, nameEn);
	r.countryId=this.countryId;
	r.provinceId=this.provinceId;
	r.cityId=this.cityId;
	r.countyId=this.id;
	this.children.push(r);
	Regions.regions[id]=r;
}

/**
 * 获取下级区域
 * @param idOrCodeOrName
 * @returns {null|*}
 */
Region.prototype.getChild=function(idOrCodeOrName){
	for(let i=0; i<this.children.length; i++){
		let c=this.children[i];
		if(c.id==idOrCodeOrName
			|| c.code==idOrCodeOrName
			|| c.nameCn==idOrCodeOrName
			|| c.nameEn==idOrCodeOrName
			|| c.nameCnFull==idOrCodeOrName) return c;

		//直辖市的第一个下级匹配的话，便认为该直辖市匹配
		if(c.level==1 && c.isMunicipality()){
			let cc=c.children[0];
			if(cc.id==idOrCodeOrName
				|| cc.code==idOrCodeOrName
				|| cc.nameCn==idOrCodeOrName
				|| cc.nameEn==idOrCodeOrName
				|| cc.nameCnFull==idOrCodeOrName) return c;
		}
	}
	return null;
}

/**
 * 根据当前使用语言返回对应的名称
 * @returns {*}
 */
Region.prototype.getName=function(lang){
	if(!lang) lang=Lang.getCurrentLang().id;
	if(lang=='cn') return this.nameCn;
	else return this.nameEn;
}

/**
 * 获取名称与指定名称相似的下级区域
 * @param name
 * @returns {null|*}
 */
Region.prototype.getChildrenAlike=function(name){
	let alikes=[];
	for(let i=0; i<this.children.length; i++){
		let c=this.children[i];
		if(c.nameCn.indexOf(name)>-1
			||c.nameEn.indexOf(name)>-1
			||name.indexOf(c.nameCn)>-1
			||name.indexOf(c.nameEn)>-1){
			alikes.push(c);
		}else if(c.level==1 && c.isMunicipality()){//直辖市的第一个下级匹配的话，便认为该直辖市匹配
			let cc=c.children[0];
			if(cc.nameCn.indexOf(name)>-1
				||cc.nameEn.indexOf(name)>-1
				||name.indexOf(cc.nameCn)>-1
				||name.indexOf(cc.nameEn)>-1){
				alikes.push(c);
			}
		}
	}
	return alikes;
}

/**
 * 获取父级地域
 */
Region.prototype.getParent=function(){
	if(this.level==0) return null;//国家无上级
	if(this.level==1) return Regions.regions[this.countryId];//省份的上级
	if(this.level==2) return Regions.regions[this.provinceId];//城市的上级
	if(this.level==3) return Regions.regions[this.cityId];//区县的上级
	if(this.level==4) return Regions.regions[this.countyId];//乡镇的上级
	return null;
}

/**
 * 获取包括上级名称的完整地址（直辖市不添加“省份名”）
 * @param lang
 * @param splitter
 * @param topLevel 最高显示到哪一级，默认为1（省份）
 * @returns {string}
 */
Region.prototype.getCanonicalName=function(lang, splitter, topLevel){
	if(!lang) lang=Lang.getCurrentLang().id;
	if(!splitter) splitter='';
	if((typeof topLevel)!='number' || topLevel<0) topLevel=1;

	let n=[];
	n.push(this.getName(lang));

	let p=this.getParent();
	if(this.isMunicipality()) p=p.getParent();//直辖市不添加“省份名”

	while(p && p.level>=topLevel){
		n.push(p.getName(lang));

		p=p.getParent();
		if(p.isMunicipality()) p=p.getParent();//直辖市不添加“省份名”
	}

	n.reverse();
	return n.join(splitter);
}

/**
 * 获取城市级别的上级
 */
Region.prototype.getCity=function(){
	if(this.level==2) return this;//本级就是城市
	if(this.level==1 && this.isMunicipality()) return this.children[0];//本级是省份但为直辖市，返回第一个下级节点
	if(this.level<2) return null;

	let p=this.getParent();
	while(p){
		if(p.level==2) return p;
		p=p.getParent();
	}
	return null;
}

/**
 * 地域链
 */
Region.prototype.getChain=function(){
	let chain=[];
	chain.push(this);
	let p=this.getParent();
	while(p){
		chain.push(p);
		p=p.getParent();
	}
	chain.reverse();
	return chain;
}

/**
 *
 *
 */
Region.prototype.toString=function(){
	let s=[];
	s.push('{"level":'+this.level);
	s.push(',"id":"'+this.id+'"');
	if(this.code) s.push(',"code":"'+this.code+'"');
	s.push(',"nameCn":"'+JSONUtil.convert(this.nameCn)+'"');
	s.push(',"nameEn":"'+JSONUtil.convert(this.nameEn)+'"');
	s.push('}');
	return s.join('');
}

/**
 * 地域相关功能，包括地域选择组件、标准地址输入组件等
 * @type {{}}
 */
let Regions={
	//默认国家/地区ID（8为中国）
	defaultCountryId:'8',

	//顶级地域（国家/地区）
	countries:[],

	//所有地域（region.id=[region.pid, region]）
	regions:[],

	//直辖市
	municipalities:[],

	//每次方法调用的参数
	_args: [],

	//检查资源就绪的时间间隔（毫秒）
	checkReadyStateInterval: 500,

	/**
	 * 保存某次方法调用的参数
	 * @param args
	 */
	saveArgs:function(args){
		if(!args) return;
		if(args.invokeUuid) return args.invokeUuid;

		args.invokeUuid='Region.invoke.'+Math.random();
		this._args[args.invokeUuid] = args;
		return args.invokeUuid;
	},
	restoreArgs:function(invokeUuidOrArgs){
		if(!invokeUuidOrArgs) return invokeUuidOrArgs;

		if((typeof invokeUuidOrArgs) == 'string' && this._args[invokeUuidOrArgs]) return this._args[invokeUuidOrArgs];
		if((typeof invokeUuidOrArgs.invokeUuid) == 'string' && this._args[invokeUuidOrArgs.invokeUuid]) return this._args[invokeUuidOrArgs.invokeUuid];

		return invokeUuidOrArgs;
	},
	deleteArgs:function(invokeUuid){
		this._args[invokeUuid]=null;
	},

	/**
	 * 等待下级加载完毕
	 * @param region
	 * @param invokeMethod
	 * @param args
	 * @returns {null|*}
	 */
	waitForChildren:function(region, invokeMethod, args){
		args=Regions.restoreArgs(args);

		if(!region.childrenLoaded){
			console.log('waitForChildren before invoking '+invokeMethod);
			let invokeUuid=Regions.saveArgs(args);
			if(invokeUuid) setTimeout(invokeMethod+"('"+invokeUuid+"')", Regions.checkReadyStateInterval);
			else setTimeout(invokeMethod+"()", Regions.checkReadyStateInterval);
			return null;
		}
		if(args && args.invokeUuid) Regions.deleteArgs(args.invokeUuid);

		return args;
	},

	//模糊查找
	findRegion:function (countryId, provinceId, cityId, countyId, level, idOrName){
		if(Str.isBlank(idOrName)) return null;
		let found=[];
		for(let i in this.regions){
			let r=this.regions[i];
			if(r.level != level) continue;
			if(countryId && countryId != r.countryId) continue;
			if(provinceId && provinceId != r.provinceId) continue;
			if(cityId && cityId != r.cityId) continue;
			if(countyId && countyId != r.countyId) continue;

			if(r.id == idOrName
				||r.code == idOrName
				||r.nameCn == idOrName
				||r.nameEn == idOrName
				||r.nameCnFull==idOrName){
				found.push(r);
			}else if(r.level==1 && r.isMunicipality()){//直辖市的第一个下级匹配的话，便认为该直辖市匹配
				let cc=r.children[0];
				if(cc.id == idOrName
					|| cc.code == idOrName
					|| cc.nameCn == idOrName
					|| cc.nameEn == idOrName
					|| cc.nameCnFull == idOrName){
					found.push(r);
				}
			}
		}
		return found;
	},

	//模糊查找
	findRegionSingle:function (countryId, provinceId, cityId, countyId, level, idOrName){
		let found=this.findRegion(countryId, provinceId, cityId, countyId, level, idOrName);
		return found && found.length>0 ? found[0] : null;
	},

	//查找顶级地域（国家/地区）
	findCountry:function(idOrCodeOrName){
		for(let i=0; i<this.countries.length; i++){
			if(this.countries[i].id==idOrCodeOrName
				|| this.countries[i].code==idOrCodeOrName
				|| this.countries[i].nameCn==idOrCodeOrName
				|| this.countries[i].nameEn==idOrCodeOrName
				|| this.countries[i].nameCnFull==idOrCodeOrName) return this.countries[i];
		}
		return null;
	},

	//按ID或名字查找省份
	findProvince:function(countryId, idOrName){
		let country=Regions.findCountry(countryId);
		if(!country) return null;

		let child=country.getChild(idOrName);
		if(child) return [child];

		return country.getChildrenAlike(idOrName);
	},
	findProvinceSingle:function(countryId, idOrName){
		let found=this.findProvince(countryId, idOrName);
		return found && found.length>0 ? found[0] : null;
	},

	//按ID或名字查找城市
	findCity:function(countryId, provinceId, idOrName){
		let country=Regions.findCountry(countryId);
		if(!country) return null;

		let province=country.getChild(provinceId);
		if(!province) return null;

		let child=province.getChild(idOrName);
		if(child) return [child];

		return province.getChildrenAlike(idOrName);
	},
	findCitySingle:function(countryId, provinceId, idOrName){
		let found=this.findCity(countryId, provinceId, idOrName);
		return found && found.length>0 ? found[0] : null;
	},

	//按ID或名字查找区县
	findCounty:function(countryId, provinceId, cityId, idOrName){
		let country=Regions.findCountry(countryId);
		if(!country) return null;

		let province=country.getChild(provinceId);
		if(!province) return null;

		let city=province.getChild(cityId);
		if(!city) return null;

		let child=city.getChild(idOrName);
		if(child) return [child];

		return city.getChildrenAlike(idOrName);
	},
	findCountySingle:function(countryId, provinceId, cityId, idOrName){
		let found=this.findCounty(countryId, provinceId, cityId, idOrName);
		return found && found.length>0 ? found[0] : null;
	},

	//按ID或名字查找街道/乡镇
	findZone:function(countryId, provinceId, cityId, countyId, idOrName){
		let country=Regions.findCountry(countryId);
		if(!country) return null;

		let province=country.getChild(provinceId);
		if(!province) return null;

		let city=province.getChild(cityId);
		if(!city) return null;

		let county=city.getChild(countyId);
		if(!county) return null;

		let child=county.getChild(idOrName);
		if(child) return [child];

		return county.getChildrenAlike(idOrName);
	},
	findZoneSingle:function(countryId, provinceId, cityId, countyId, idOrName){
		let found=this.findZone(countryId, provinceId, cityId, countyId, idOrName);
		return found && found.length>0 ? found[0] : null;
	},

	//加载街道
	loadedZones:[],//已经加载过乡镇/街道的区县
	loadZones:function(pickerId, countyId, _callback, zoneIdToBeSelected){
		//已经调用过，不再进行加载（调用该方法的地方必须先判断乡镇/街道未加载过才调用此方法，否则不会触发回调）
		let countyIds=countyId.split(',');
		let countyIdsNotLoaded=[];
		for(let i=0; i<countyIds.length; i++){
			if(!this.loadedZones[countyIds[i]]) countyIdsNotLoaded.push(countyIds[i]);
			this.loadedZones[countyIds[i]]=true;
		}

		if(countyIdsNotLoaded.length==0){//全部加载过了
			if(_callback) _callback(pickerId, null);
			return;
		}

		countyId=countyIdsNotLoaded.join(',');//只加载未加载过的
		let ajax=new Ajax();
		ajax.pickerId=pickerId;
		ajax.countyId=countyId;
		if(zoneIdToBeSelected) ajax.zoneIdToBeSelected=zoneIdToBeSelected;
		if(_callback) ajax._callback=_callback;
		ajax.send('GET', Regions.doLoadZones, '/framework/api/tool/region/zones?county_id='+countyId);
	},
	doLoadZones:function(ajax){
		if(ajax.getReadyState()==4 && ajax.getStatus()==200){
			let resp=JSONUtil.parse(ajax.getResponseText());
			let datas=resp.datas;
			if(!datas){
				if(ajax._callback) ajax._callback(ajax.pickerId);
				return;
			}
			for(let j=0; j<datas.length; j++) {
				let zones = datas[j].zones;
				if (!zones) {
					if(j==datas.length-1 && ajax._callback) ajax._callback(ajax.pickerId);
					return;
				}

				let county = Regions.findCountySingle(datas[j].countryId, datas[j].provinceId, datas[j].cityId, datas[j].countyId);
				if (!county) {
					if(j==datas.length-1 && ajax._callback) ajax._callback(ajax.pickerId);
					return;
				}

				county.children = [];
				for (let i = 0; i < zones.length; i++) {
					county.addZone(4, zones[i].zoneId, '', zones[i].zoneName, zones[i].zoneNameEn);
				}
				county.childrenLoaded = true;

				if(j==datas.length-1){
					if (ajax._callback) ajax._callback(ajax.pickerId, county, ajax.zoneIdToBeSelected);
					else Regions.onZonesLoad(ajax.pickerId, county, ajax.zoneIdToBeSelected);
				}
			}
		}
	},

	onZonesLoad:function (pickerId, county, zoneIdToBeSelected){
		let picker=Layers.getInstanceOfType().win.Regions ? Layers.getInstanceOfType().win.Regions.pickers[pickerId] : null;
		if(picker) picker.onZonesLoad(county, zoneIdToBeSelected);
	},

	/**
	 *
	 * 根据第三方地图组件定位信息获得省、市、区县
	 * @param countryIdOrArgs 国家ID，不指定则使用默认国家
	 * @param regionId 地域ID
	 * @param provinceName
	 * @param cityName
	 * @param countyName
	 * @param zoneName
	 * @param callback
	 * @param callbackTarget
	 * @returns {any}
	 */
	decodeAddress:function(countryIdOrArgs, regionId, provinceName, cityName, countyName, zoneName, callback, callbackTarget){
		countryIdOrArgs=Regions.restoreArgs(countryIdOrArgs);

		let countryId=countryIdOrArgs;
		if(countryIdOrArgs && countryIdOrArgs.params){
			countryId=countryIdOrArgs.params[0];
			regionId=countryIdOrArgs.params[1];
			provinceName=countryIdOrArgs.params[2];
			cityName=countryIdOrArgs.params[3];
			countyName=countryIdOrArgs.params[4];
			zoneName=countryIdOrArgs.params[5];
			callback=countryIdOrArgs.params[6];
			callbackTarget=countryIdOrArgs.params[7];
		}

		console.log('decodeAddress countryId -> '+countryId);
		console.log('decodeAddress regionId -> '+regionId);
		console.log('decodeAddress provinceName -> '+provinceName);
		console.log('decodeAddress cityName -> '+cityName);
		console.log('decodeAddress countyName -> '+countyName);
		console.log('decodeAddress zoneName -> '+zoneName);

		if(Str.isBlank(countryId)) countryId=this.defaultCountryId;
		let country=this.findCountry(countryId);

		//通过regionId匹配
		if(!Str.isBlank(regionId)){
			console.log('decode address by regionId -> '+regionId);
			let r=Regions.regions[regionId];
			if(r){
				let chain=r.getChain();
				if(chain.length==4 && !Str.isBlank(zoneName)){//匹配到了区县，且指定了乡镇/街道
					let county=chain[3];
					if(!county.childrenLoaded){//乡镇/街道尚未加载
						let args={params:[countryId, regionId, provinceName, cityName, countyName, zoneName, callback, callbackTarget]};
						Regions.waitForChildren(county, 'Regions.decodeAddress', args);
						Regions.loadZones(null, county.id, null);
						return county.getChain();
					}

					let zone=Regions.findRegionSingle(countryId, null, null, county.id, 4, zoneName);
					if(zone != null) chain.push(zone);
				}

				if(callback) callback.call(callbackTarget?callbackTarget:window, chain);
				return chain;
			}
		}

		//未通过regionId匹配到地址，继续模糊查找
		let province=country.getChild(provinceName);
		let city=null;
		let county=null;
		let zone=null;
		if(province){//如果省份存在，依次往下查找城市、区县
			city=Regions.findRegionSingle(countryId, province.id, null, null, 2, cityName);
			if(!city && province.isMunicipality()) city=province.children[0];
			if(city){
				county=Regions.findRegionSingle(countryId, null, city.id, null, 3, countyName);
			}
		}else{//如果省份不存在，直接查找城市
			city=Regions.findRegionSingle(countryId, null, null, null, 2, cityName);
			if(city){//如果城市存在，往下查找区县
				county=Regions.findRegionSingle(countryId, null, city.id, null, 3, countyName);
			}else{//如果城市不存在，直接查找区县
				county=Regions.findRegionSingle(countryId, null, null, null, 3, countyName);
			}
		}

		//如果匹配到区县，且指定了乡镇/街道
		if(county && !Str.isBlank(zoneName)){
			if(!county.childrenLoaded){//乡镇/街道尚未加载
				let args={params:[countryId, regionId, provinceName, cityName, countyName, zoneName, callback, callbackTarget]};
				Regions.waitForChildren(county, 'Regions.decodeAddress', args);
				console.log('loading zones - '+county.id);
				Regions.loadZones(null, county.id, null);
				return county.getChain();
			}

			zone=Regions.findRegionSingle(countryId, null, null, county.id, 4, zoneName);
		}

		//如果省份是直辖市，且未匹配到城市，则城市为省份的第一个直接下级（直辖市省份只有一个城市级别下级）
		if(province && province.isMunicipality() && !city) city=province.children[0];

		let chain=null;
		if(zone != null) chain=zone.getChain();//匹配到了乡镇/街道
		else if(county != null) chain=county.getChain();//匹配到了区县
		else if(city != null) chain=city.getChain();//匹配到了城市
		else if(province != null) chain=province.getChain();//匹配到了省份

		if(callback) callback.call(callbackTarget?callbackTarget:window, chain);
		return chain;
	},

	getCanonicalName:function(chain, lang){
		if(!lang) lang=Lang.getCurrentLang().id;
		let names=[];
		for(let i=0; i<chain.length; i++){
			if(chain[i].isMunicipality()) continue;
			if(lang=='cn') names.push(chain[i].nameCn);
			else names.push(chain[i].nameEn);
		}
		if(lang=='cn') return names.join('，');
		else{
			names=names.reverse();
			return names.join(', ');
		}
	},

	idToString:function (obj, levels, addressExcluded){
		let lang=Lang.getCurrentLang().id;
		let addr=[];
		let country=Regions.findCountry(obj.countryId);
		if(country) addr.push(lang == 'cn' ? country.nameCn : country.nameEn);

		if(!levels || levels>=2) {
			if (!Str.isBlank(obj.provinceId)) {
				let province = Regions.findProvinceSingle(obj.countryId, obj.provinceId);
				if (province) {
					addr.push(lang == 'cn' ? ' ' : ', ');
					addr.push(lang == 'cn' ? province.nameCn : province.nameEn);
				}
			} else if (!Str.isBlank(obj.provinceName)) {
				addr.push(lang == 'cn' ? ' ' : ', ');
				addr.push(obj.provinceName);
			}
		}

		if(!levels || levels>=3) {
			if (!Str.isBlank(obj.cityId)) {
				let city = Regions.findCitySingle(obj.countryId, obj.provinceId, obj.cityId);
				if (city) {
					addr.push(lang == 'cn' ? ' ' : ', ');
					addr.push(lang == 'cn' ? city.nameCn : city.nameEn);
				}
			} else if (!Str.isBlank(obj.cityName)) {
				addr.push(lang == 'cn' ? ' ' : ', ');
				addr.push(obj.cityName);
			}
		}

		if((!levels || levels>=4) && !Str.isBlank(obj.countyId)){
			let county=Regions.findCountySingle(obj.countryId, obj.provinceId, obj.cityId, obj.countyId);
			if(county){
				addr.push(lang == 'cn' ? ' ' : ', ');
				addr.push(lang == 'cn' ? county.nameCn : county.nameEn);
			}
		}

		if((!levels || levels>=5) && !Str.isBlank(obj.zoneId)){
			let zone=Regions.findZoneSingle(obj.countryId, obj.provinceId, obj.cityId, obj.countyId, obj.zoneId);
			if(zone){
				addr.push(lang == 'cn' ? ' ' : ', ');
				addr.push(lang == 'cn' ? zone.nameCn : zone.nameEn);
			}
		}

		if(!addressExcluded) {
			if (!Str.isBlank(obj.addr)) {
				addr.push(lang == 'cn' ? ' ' : ', ');
				addr.push(obj.addr);
			}

			if (!Str.isBlank(obj.addr2)) {
				addr.push(lang == 'cn' ? ' ' : ', ');
				addr.push(obj.addr2);
			}
		}
		if(lang != 'cn') addr.reverse();

		return addr.join('');
	},

	//地域选择组件实例
	pickers:[]
}
window.Regions=Regions;

/**
 * 地域选择组件
 * @param id
 * @param minLevel
 * @param maxLevel
 * @param multiChoices 一次选择多个地域
 * @param callback 回调（点击时）
 * @param callbackTarget 调用回调方法的对象（默认window）
 * @constructor
 */
function RegionPicker(id, minLevel, maxLevel, multiChoices, callback, callbackTarget){
	if(!UserAgent.isPC()) top.document.body.style.setProperty('--RegionPickerAreaWidth', Math.floor((top.W.vw())/3 - 10) + 'px');
	else top.document.body.style.setProperty('--RegionPickerAreaWidth', Math.floor((top.W.vw()-8)/3 - 10) + 'px');

	this.id=id;
	this.minLevel=minLevel;
	this.maxlevel=maxLevel;
	this.multiChoices=(typeof multiChoices)=='boolean'?multiChoices:false;
	this.callback=callback;
	this.callbackTarget=callbackTarget;
	this.inLayer=null;
	this.selectedChain=[];//当前选中地域链
	this.title='I{选择地点}';

	Regions.pickers[this.id]=this;
	this.build();
}

RegionPicker.prototype.setTitle=function(title){
	this.title=Str.isBlank(title)?'I{选择地点}':title;
	if(this.inLayer) {
		if(this.selectedChain.length > 0) this.inLayer.setTitle(this.selectedChain[this.selectedChain.length-1].getCanonicalName(null, ', '));
		else this.inLayer.setTitle(this.title);
	}
}

/**
 * 创建组件
 */
RegionPicker.prototype.build=function(){
	let str=[];
	str.push('<div class="RegionPicker" id="'+this.id+'">');

	str.push('	<div class="RegionPickerHead" id="'+this.id+'_head"><input type="text" picker="'+this.id+'" id="'+this.id+'_keywords" placeholder="I{搜索}"/></div>');

	str.push('	<div class="RegionPickerContent" id="'+this.id+'_content">');
	str.push('		<div class="RegionPickerAreas" id="'+this.id+'_L0"></div>');
	str.push('		<div class="RegionPickerAreas" id="'+this.id+'_L1" style="display: none;"></div>');
	str.push('		<div class="RegionPickerAreas" id="'+this.id+'_L2" style="display: none;"></div>');
	str.push('		<div class="RegionPickerAreas" id="'+this.id+'_L3" style="display: none;"></div>');
	str.push('		<div class="RegionPickerAreas" id="'+this.id+'_L4" style="display: none;"></div>');
	str.push('	</div>');
	str.push('</div>');

	let btns=[];
	btns.push('	<div class="RegionPickerContentBtns">');
	btns.push('		<div class="displayBlock btnH40 btnBgBlue w80 mT5" onclick="Layers.getInstanceOfType().win.Regions.pickers[\''+this.id+'\'].done();">'+(this.multiChoices?'I{添加}':'I{确定}')+'</div>');
	btns.push('		<div class="displayBlock btnH40 btnBgGray mT5 w80 mL10" id="'+this.id+'_to_parent" style="display:none;" onclick="Layers.getInstanceOfType().win.Regions.pickers[\''+this.id+'\'].toParent();">I{返回}</div>');
	btns.push('		<div class="displayBlock btnH40 btnBgGray mT5 w80 mL10" onclick="Layers.getInstanceOfType().win.Regions.pickers[\''+this.id+'\'].close();">I{关闭}</div>');
	btns.push('	</div>');

	this.inLayer=Layers.open(window, this.title, null,str.join(''), btns.join(''), 0, null);
	_$$(true, this.id+'_content').style.height=(this.inLayer.getHeight() - W.elementHeight(_$$(true, this.id+'_head')))+'px';
	top.scroll(0,0);

	str=null;
	delete str;

	btns=null;
	delete btns;

	let fullWidth = top.W.elementWidth(top._$(this.id));
	if(!UserAgent.isPC()) top.document.body.style.setProperty('--RegionPickerAreaWidth', Math.floor(fullWidth/3 - 10) + 'px');
	else top.document.body.style.setProperty('--RegionPickerAreaWidth', Math.floor((fullWidth-8)/3 - 10) + 'px');

	//绑定搜索事件
	new InputEvent(top._$$(true, this.id+'_keywords'), this.search, window);

	//初始化顶级地域
	for(let i=0; i<Regions.countries.length; i++){
		let c=Regions.countries[i];
		let area=document.createElement('div');
		area.id=this.id+'_0_'+c.id;
		area.className='RegionPickerArea';
		area.innerHTML=c.getName();
		top._$$(true, this.id+'_L0').appendChild(area);

		Utils.setAtt(area, 'picker', this.id);
		Utils.setAtt(area, 'level', '0');
		Utils.setAtt(area, 'countryId', c.id);

		area.addEventListener('click', function(event){
			let area=Utils.getEventTarget(event);
			let pickerId=Utils.att(area, 'picker');
			if(!pickerId) return;

			let picker=Layers.getInstanceOfType().win.Regions.pickers[pickerId];
			if(!picker) return;

			picker.select(area);
		});
	}
}

/**
 *
 * @param countryId
 * @param provinceId
 * @param cityId
 * @param countyId
 * @param zoneId
 * @param addressText
 */
RegionPicker.prototype.init=function(countryId, provinceId, cityId, countyId, zoneId, addressText){
	if(!Str.isBlank(addressText) && addressText != 'null'){
		addressText=Str.replaceAll(addressText,'  ',' ');
		let addressTextCells=addressText.split(' ');

		if(addressTextCells.length>0 && addressTextCells[0]!=''){//按名字查找省份
			let namedProvince=Regions.findProvinceSingle('8', addressTextCells[0]);
			if(namedProvince){
				countryId='8';
				provinceId=namedProvince.id;

				if(addressTextCells.length>1 && addressTextCells[1]!=''){//按名字查找城市
					let namedCity=Regions.findCitySingle(countryId, provinceId, addressTextCells[1]);
					if(namedCity){
						cityId=namedCity.id;

						if(addressTextCells.length>2 && addressTextCells[2]!=''){//按名字查找区县
							let namedCounty=Regions.findCountySingle(countryId, provinceId, cityId, addressTextCells[2]);
							if(namedCounty) countyId=namedCounty.id;

							if(addressTextCells.length>3 && addressTextCells[3]!=''){//按名字查找区县
								let namedZone=Regions.findZoneSingle(countryId, provinceId, cityId, countyId, addressTextCells[3]);
								if(namedZone) zoneId=namedZone.id;
							}
						}
					}
				}
			}
		}
	}
	
	if(countryId){
		let area=top._$(this.id+'_0_'+countryId);
		if(area) this.select(area);
	}

	if(provinceId){
		let area=top._$(this.id+'_1_'+provinceId);
		if(area) this.select(area);
	}

	if(cityId){
		let area=top._$(this.id+'_2_'+cityId);
		if(area) this.select(area);
	}

	if(countyId){
		let area=top._$(this.id+'_3_'+countyId);
		if(area) this.select(area, zoneId);
	}
}

/**
 * 搜索
 * @param area
 */
RegionPicker.prototype.search=function(event, keywords){
	let input=event ? Utils.getEventTarget(event) : _$$(true, this.id + '_keywords');
	if((typeof keywords)=='string') input.value=keywords;
	let pickerId=input.id.substring(0, input.id.length - '_keywords'.length);
	let picker=Layers.getInstanceOfType().win.Regions.pickers[pickerId];
	if(!picker) return;

	keywords=Str.trimAll(input.value);

	//关键词为空，显示所有
	if(Str.isBlank(keywords)){
		let areas=_$$cls(true, 'RegionPickerAreaSelected');
		for(let i=0; areas && i<areas.length; i++){
			areas[i].style.display='';
		}

		areas=_$$cls(true, 'RegionPickerArea');
		for(let i=0; areas && i<areas.length; i++){
			areas[i].style.display='';
		}

		return;
	}

	//对当前显示级别的地域进行查找
	keywords=keywords.toUpperCase();
	for(let level=0; level<=4; level++){
		let container=_$$(true, pickerId+'_L'+level);
		let display=Utils.getStyle(container, 'display');
		if(display.indexOf('none') > -1) continue;

		let cNodes=container.childNodes;
		for(let i=0; i<cNodes.length; i++){
			if(cNodes[i].innerHTML.toUpperCase().indexOf(keywords) < 0) cNodes[i].style.display='none';
			else cNodes[i].style.display='';
		}
	}
}

/**
 * 选中某个地域
 * @param area
 */
RegionPicker.prototype.select=function(area, zoneIdToBeSelected){
	if(!area) return;

	let level=Utils.att(area, 'level')*1;
	if(level > this.maxlevel) return;//超过允许选择的最大层级

	let countryId=Utils.att(area, 'countryId');
	let provinceId=Utils.att(area, 'provinceId');
	let cityId=Utils.att(area, 'cityId');
	let countyId=Utils.att(area, 'countyId');
	let zoneId=Utils.att(area, 'zoneId');

	let country=null;
	let province=null;
	let city=null;
	let county=null;
	let zone=null;
	if(level==0){
		country=Regions.findCountry(countryId);
		if(!country) return;

		this.selectedChain=[country];
	}else if(level==1){
		country=Regions.findCountry(countryId);
		province=Regions.findProvinceSingle(countryId, provinceId);

		if(!province) return;
		this.selectedChain=[country, province];
	}else if(level==2){
		country=Regions.findCountry(countryId);
		province=Regions.findProvinceSingle(countryId, provinceId);
		city=Regions.findCitySingle(countryId, provinceId, cityId);

		if(!city) return;
		this.selectedChain=[country, province, city];
	}else if(level==3){
		country=Regions.findCountry(countryId);
		province=Regions.findProvinceSingle(countryId, provinceId);
		city=Regions.findCitySingle(countryId, provinceId, cityId);
		county=Regions.findCountySingle(countryId, provinceId, cityId, countyId);

		if(!county) return;

		//未加载乡镇/街道
		if(this.maxlevel>level && !county.childrenLoaded){
			Regions.loadZones(this.id, countyId, null, zoneIdToBeSelected);
			return;
		}

		this.selectedChain=[country, province, city, county];
	}else if(level==4){
		country=Regions.findCountry(countryId);
		province=Regions.findProvinceSingle(countryId, provinceId);
		city=Regions.findCitySingle(countryId, provinceId, cityId);
		county=Regions.findCountySingle(countryId, provinceId, cityId, countyId);
		zone=Regions.findZoneSingle(countryId, provinceId, cityId, countyId, zoneId);

		if(!zone) return;
		this.selectedChain=[country, province, city, county, zone];
	}

	//显示已选区域链
	this.setTitle();

	//设置本级及其下级已选中地区的样式为“未选中”
	let areas=_$$cls(true, 'RegionPickerAreaSelected');
	for(let i=0; areas && i<areas.length; i++){
		if(Utils.att(areas[i], 'level')*1 < level) continue;
		areas[i].className='RegionPickerArea';
	}

	//设置本节点的样式为“选中”
	area.className='RegionPickerAreaSelected';

	//隐藏上级
	for(let i=0; i<level; i++){
		_$$(true, this.id+'_L'+i).style.display='none';
	}

	let children=null;
	if(level==0) children=country.children;
	else if(level==1) children=province.children;
	else if(level==2) children=city.children;
	else if(level==3) children=county.children;

	//显示/隐藏返回上级按钮
	if(level==0 && (!children || children.length==0)) _$$(true, this.id+'_to_parent').style.display='none';
	else _$$(true, this.id+'_to_parent').style.display='';

	//本级已是最大层级（或下级为空），隐藏全部下级
	if(this.maxlevel==level || !children || children.length==0){
		for(let i=4; i>level; i--){
			_$$(true, this.id+'_L'+i).style.display='none';
		}
	}else{
		//清空搜索关键词
		_$$(true, this.id+'_keywords').value='';
		this.search(null, '');

		//隐藏本级
		_$$(true, this.id+'_L'+level).style.display='none';

		//隐藏非直接下级
		for(let i=4; i>level+1; i--){
			_$$(true, this.id+'_L'+i).style.display='none';
		}

		//展现直接下级
		let childLevel=level+1;
		_$$(true, this.id+'_L'+childLevel).innerHTML='';
		_$$(true, this.id+'_L'+childLevel).style.display='';

		for(let i=0; i<children.length; i++){
			let r=children[i];
			let area=document.createElement('div');
			area.id=this.id+'_'+childLevel+'_'+r.id;
			area.className='RegionPickerArea';
			area.innerHTML=r.getName();
			_$$(true, this.id+'_L'+childLevel).appendChild(area);

			Utils.setAtt(area, 'picker', this.id);
			Utils.setAtt(area, 'level', ''+childLevel);
			if(country) Utils.setAtt(area, 'countryId', country.id);
			if(province) Utils.setAtt(area, 'provinceId', province.id);
			if(city) Utils.setAtt(area, 'cityId', city.id);
			if(county) Utils.setAtt(area, 'countyId', county.id);

			if(childLevel==1) Utils.setAtt(area, 'provinceId', r.id);
			else if(childLevel==2) Utils.setAtt(area, 'cityId', r.id);
			else if(childLevel==3) Utils.setAtt(area, 'countyId', r.id);
			else if(childLevel==4) Utils.setAtt(area, 'zoneId', r.id);

			area.addEventListener('click', function(event){
				let area=Utils.getEventTarget(event);
				let pickerId=Utils.att(area, 'picker');
				if(!pickerId) return;

				let picker=Layers.getInstanceOfType().win.Regions.pickers[pickerId];
				if(!picker) return;

				picker.select(area);
			});
		}
		//展现直接下级 end

		if(zoneIdToBeSelected){//街道加载完毕后选中指定的街道
			this.select(_$$(true, this.id+'_4_'+zoneIdToBeSelected));
		}
	}
}

/**
 * 返回上一级
 */
RegionPicker.prototype.toParent=function(){
	//无需返回上级
	if(this.selectedChain.length==0){
		_$$(true, this.id+'_to_parent').style.display='none';
		this.setTitle();
		return;
	}

	//清空搜索关键词
	_$$(true, this.id+'_keywords').value='';
	this.search(null, '');

	//移除最后一个选择的节点
	this.selectedChain.pop();

	//回到顶级
	if(this.selectedChain.length==0){
		_$$(true, this.id+'_to_parent').style.display='none';
		this.setTitle();

		let areas=_$$cls(true, 'RegionPickerAreaSelected');
		for(let i=0; areas && i<areas.length; i++) areas[i].className='RegionPickerArea';

		for(let i=1; i<=4; i++) _$$(true, this.id+'_L'+i).style.display='none';
		_$$(true, this.id+'_L0').style.display='';

		return;
	}

	//上一级
	let area=_$$(true, this.id+'_'+(this.selectedChain.length-1)+'_'+this.selectedChain[this.selectedChain.length - 1].id);
	this.select(area);
}

/**
 * 乡镇/街道加载完毕
 * @param county
 */
RegionPicker.prototype.onZonesLoad=function(county, zoneIdToBeSelected){
	this.select(_$$(true, this.id+'_3_'+county.id), zoneIdToBeSelected);
}

/**
 * 完成选择
 */
RegionPicker.prototype.done=function(){
	if(this.callback){
		this.callback.call(this.callbackTarget?this.callbackTarget:window, this.selectedChain);
	}
	if(!this.multiChoices) this.close();
}

/**
 * 关闭
 */
RegionPicker.prototype.close=function(){
	this.inLayer.close();
}

//添加国家
function _R0(id, code, nameCn, nameEn){
	let r=new Region(0, id, code, nameCn, nameEn);
	Regions.countries.push(r);
	Regions.regions[r.id]=r;
}

//添加省份
function _R1(countryIndex, id, code, nameCn, nameEn, nameCnFull){
	let country=Regions.countries[countryIndex];
	let r=new Region(1, id, code, nameCn, nameEn, nameCnFull);
	r.countryId=country.id;
	country.children.push(r);
	Regions.regions[r.id]=r;
}

//添加城市
function _R2(countryIndex, provinceIndex, id, code, nameCn, nameEn){
	let country=Regions.countries[countryIndex];
	let province=Regions.countries[countryIndex].children[provinceIndex];
	let r=new Region(2, id, code, nameCn, nameEn);
	r.countryId=country.id;
	r.provinceId=province.id;
	province.children.push(r);
	Regions.regions[r.id]=r;
}

//添加区县
function _R3(countryIndex, provinceIndex, cityIndex, id, code, nameCn, nameEn){
	let country=Regions.countries[countryIndex];
	let province=Regions.countries[countryIndex].children[provinceIndex];
	let city=Regions.countries[countryIndex].children[provinceIndex].children[cityIndex];
	let r=new Region(3, id, code, nameCn, nameEn);
	r.countryId=country.id;
	r.provinceId=province.id;
	r.cityId=city.id;
	city.children.push(r);
	Regions.regions[r.id]=r;
}

//所有地区（不含乡镇/街道）
_R0('8','CN','中国','China');

_R1(0,'110000','','北京','bei jing','北京');
_R2(0,0,'110100','','北京市','bei jing shi');
_R3(0,0,0,'110116','','怀柔区','huai rou qu');
_R3(0,0,0,'110117','','平谷区','ping gu qu');
_R3(0,0,0,'110114','','昌平区','chang ping qu');
_R3(0,0,0,'110115','','大兴区','da xing qu');
_R3(0,0,0,'110112','','通州区','tong zhou qu');
_R3(0,0,0,'110113','','顺义区','shun yi qu');
_R3(0,0,0,'110111','','房山区','fang shan qu');
_R3(0,0,0,'110105','','朝阳区','chao yang qu');
_R3(0,0,0,'110106','','丰台区','feng tai qu');
_R3(0,0,0,'110101','','东城区','dong cheng qu');
_R3(0,0,0,'110102','','西城区','xi cheng qu');
_R3(0,0,0,'110109','','门头沟区','men tou gou qu');
_R3(0,0,0,'110107','','石景山区','shi jing shan qu');
_R3(0,0,0,'110108','','海淀区','hai dian qu');
_R3(0,0,0,'110228','','密云区','mi yun qu');
_R3(0,0,0,'110229','','延庆区','yan qing qu');
_R3(0,0,0,'110230','','其它区','qi ta qu');

_R1(0,'120000','','天津','tian jin','天津');
_R2(0,1,'120100','','天津市','tian jin shi');
_R3(0,1,0,'120102','','河东区','he dong qu');
_R3(0,1,0,'120103','','河西区','he xi qu');
_R3(0,1,0,'120104','','南开区','nan kai qu');
_R3(0,1,0,'120105','','河北区','he bei qu');
_R3(0,1,0,'120101','','和平区','he ping qu');
_R3(0,1,0,'120106','','红桥区','hong qiao qu');
_R3(0,1,0,'120113','','北辰区','bei chen qu');
_R3(0,1,0,'120114','','武清区','wu qing qu');
_R3(0,1,0,'120115','','宝坻区','bao di qu');
_R3(0,1,0,'120116','','滨海新区','bin hai xin qu');
_R3(0,1,0,'120110','','东丽区','dong li qu');
_R3(0,1,0,'120111','','西青区','xi qing qu');
_R3(0,1,0,'120112','','津南区','jin nan qu');
_R3(0,1,0,'120223','','静海区','jing hai qu');
_R3(0,1,0,'120225','','蓟州区','ji zhou qu');
_R3(0,1,0,'120226','','其它区','qi ta qu');
_R3(0,1,0,'120221','','宁河区','ning he qu');

_R1(0,'130000','','河北省','he bei sheng','河北省');
_R2(0,2,'130600','','保定市','bao ding shi');
_R3(0,2,0,'130602','','竞秀区','jing xiu qu');
_R3(0,2,0,'130603','','莲池区','lian chi qu');
_R3(0,2,0,'130621','','满城区','man cheng qu');
_R3(0,2,0,'130624','','阜平县','fu ping xian');
_R3(0,2,0,'130625','','徐水区','xu shui qu');
_R3(0,2,0,'130622','','清苑区','qing yuan qu');
_R3(0,2,0,'130623','','涞水县','lai shui xian');
_R3(0,2,0,'130628','','高阳县','gao yang xian');
_R3(0,2,0,'130629','','容城县','rong cheng xian');
_R3(0,2,0,'130626','','定兴县','ding xing xian');
_R3(0,2,0,'130627','','唐县','tang xian');
_R3(0,2,0,'130682','','定州市','ding zhou shi');
_R3(0,2,0,'130683','','安国市','an guo shi');
_R3(0,2,0,'130681','','涿州市','zhuo zhou shi');
_R3(0,2,0,'130684','','高碑店市','gao bei dian shi');
_R3(0,2,0,'130699','','其它区','qi ta qu');
_R3(0,2,0,'130631','','望都县','wang du xian');
_R3(0,2,0,'130632','','安新县','an xin xian');
_R3(0,2,0,'130630','','涞源县','lai yuan xian');
_R3(0,2,0,'130635','','蠡县','li xian');
_R3(0,2,0,'130636','','顺平县','shun ping xian');
_R3(0,2,0,'130633','','易县','yi xian');
_R3(0,2,0,'130634','','曲阳县','qu yang xian');
_R3(0,2,0,'130637','','博野县','bo ye xian');
_R3(0,2,0,'130638','','雄县','xiong xian');
_R2(0,2,'130900','','沧州市','cang zhou shi');
_R3(0,2,1,'130903','','运河区','yun he qu');
_R3(0,2,1,'130902','','新华区','xin hua qu');
_R3(0,2,1,'130983','','黄骅市','huang hua shi');
_R3(0,2,1,'130984','','河间市','he jian shi');
_R3(0,2,1,'130981','','泊头市','bo tou shi');
_R3(0,2,1,'130982','','任丘市','ren qiu shi');
_R3(0,2,1,'130985','','其它区','qi ta qu');
_R3(0,2,1,'130921','','沧县','cang xian');
_R3(0,2,1,'130922','','青县','qing xian');
_R3(0,2,1,'130925','','盐山县','yan shan xian');
_R3(0,2,1,'130926','','肃宁县','su ning xian');
_R3(0,2,1,'130923','','东光县','dong guang xian');
_R3(0,2,1,'130924','','海兴县','hai xing xian');
_R3(0,2,1,'130929','','献县','xian xian');
_R3(0,2,1,'130927','','南皮县','nan pi xian');
_R3(0,2,1,'130928','','吴桥县','wu qiao xian');
_R3(0,2,1,'130930','','孟村回族自治县','meng cun hui zu zi zhi xian');
_R2(0,2,'130800','','承德市','cheng de shi');
_R3(0,2,2,'130804','','鹰手营子矿区','ying shou ying zi kuang qu');
_R3(0,2,2,'130802','','双桥区','shuang qiao qu');
_R3(0,2,2,'130803','','双滦区','shuang luan qu');
_R3(0,2,2,'130822','','兴隆县','xing long xian');
_R3(0,2,2,'130823','','平泉市','ping quan shi');
_R3(0,2,2,'130821','','承德县','cheng de xian');
_R3(0,2,2,'130826','','丰宁满族自治县','feng ning man zu zi zhi xian');
_R3(0,2,2,'130827','','宽城满族自治县','kuan cheng man zu zi zhi xian');
_R3(0,2,2,'130824','','滦平县','luan ping xian');
_R3(0,2,2,'130825','','隆化县','long hua xian');
_R3(0,2,2,'130828','','围场满族蒙古族自治县','wei chang man zu meng gu zu zi zhi xian');
_R3(0,2,2,'130829','','其它区','qi ta qu');
_R2(0,2,'130400','','邯郸市','han dan shi');
_R3(0,2,3,'130430','','邱县','qiu xian');
_R3(0,2,3,'130433','','馆陶县','guan tao xian');
_R3(0,2,3,'130434','','魏县','wei xian');
_R3(0,2,3,'130431','','鸡泽县','ji ze xian');
_R3(0,2,3,'130432','','广平县','guang ping xian');
_R3(0,2,3,'130435','','曲周县','qu zhou xian');
_R3(0,2,3,'130404','','复兴区','fu xing qu');
_R3(0,2,3,'130402','','邯山区','han shan qu');
_R3(0,2,3,'130403','','丛台区','cong tai qu');
_R3(0,2,3,'130406','','峰峰矿区','feng feng kuang qu');
_R3(0,2,3,'130423','','临漳县','lin zhang xian');
_R3(0,2,3,'130426','','涉县','she xian');
_R3(0,2,3,'130427','','磁县','ci xian');
_R3(0,2,3,'130424','','成安县','cheng an xian');
_R3(0,2,3,'130425','','大名县','da ming xian');
_R3(0,2,3,'130428','','肥乡区','fei xiang qu');
_R3(0,2,3,'130429','','永年区','yong nian qu');
_R3(0,2,3,'130481','','武安市','wu an shi');
_R3(0,2,3,'130482','','其它区','qi ta qu');
_R2(0,2,'131100','','衡水市','heng shui shi');
_R3(0,2,4,'131181','','冀州区','ji zhou qu');
_R3(0,2,4,'131183','','其它区','qi ta qu');
_R3(0,2,4,'131182','','深州市','shen zhou shi');
_R3(0,2,4,'131123','','武强县','wu qiang xian');
_R3(0,2,4,'131122','','武邑县','wu yi xian');
_R3(0,2,4,'131121','','枣强县','zao qiang xian');
_R3(0,2,4,'131127','','景县','jing xian');
_R3(0,2,4,'131126','','故城县','gu cheng xian');
_R3(0,2,4,'131125','','安平县','an ping xian');
_R3(0,2,4,'131124','','饶阳县','rao yang xian');
_R3(0,2,4,'131128','','阜城县','fu cheng xian');
_R3(0,2,4,'131102','','桃城区','tao cheng qu');
_R2(0,2,'131000','','廊坊市','lang fang shi');
_R3(0,2,5,'131082','','三河市','san he shi');
_R3(0,2,5,'131081','','霸州市','ba zhou shi');
_R3(0,2,5,'131083','','其它区','qi ta qu');
_R3(0,2,5,'131002','','安次区','an ci qu');
_R3(0,2,5,'131003','','广阳区','guang yang qu');
_R3(0,2,5,'131024','','香河县','xiang he xian');
_R3(0,2,5,'131023','','永清县','yong qing xian');
_R3(0,2,5,'131022','','固安县','gu an xian');
_R3(0,2,5,'131028','','大厂回族自治县','da chang hui zu zi zhi xian');
_R3(0,2,5,'131026','','文安县','wen an xian');
_R3(0,2,5,'131025','','大城县','dai cheng xian');
_R2(0,2,'130300','','秦皇岛市','qin huang dao shi');
_R3(0,2,6,'130323','','抚宁区','fu ning qu');
_R3(0,2,6,'130324','','卢龙县','lu long xian');
_R3(0,2,6,'130321','','青龙满族自治县','qing long man zu zi zhi xian');
_R3(0,2,6,'130322','','昌黎县','chang li xian');
_R3(0,2,6,'130302','','海港区','hai gang qu');
_R3(0,2,6,'130303','','山海关区','shan hai guan qu');
_R3(0,2,6,'130304','','北戴河区','bei dai he qu');
_R3(0,2,6,'130398','','其它区','qi ta qu');
_R2(0,2,'130100','','石家庄市','shi jia zhuang shi');
_R3(0,2,7,'130104','','桥西区','qiao xi qu');
_R3(0,2,7,'130102','','长安区','chang an qu');
_R3(0,2,7,'130107','','井陉矿区','jing xing kuang qu');
_R3(0,2,7,'130108','','裕华区','yu hua qu');
_R3(0,2,7,'130105','','新华区','xin hua qu');
_R3(0,2,7,'130183','','晋州市','jin zhou shi');
_R3(0,2,7,'130184','','新乐市','xin le shi');
_R3(0,2,7,'130181','','辛集市','xin ji shi');
_R3(0,2,7,'130182','','藁城区','gao cheng qu');
_R3(0,2,7,'130185','','鹿泉区','lu quan qu');
_R3(0,2,7,'130186','','其它区','qi ta qu');
_R3(0,2,7,'130121','','井陉县','jing xing xian');
_R3(0,2,7,'130125','','行唐县','xing tang xian');
_R3(0,2,7,'130126','','灵寿县','ling shou xian');
_R3(0,2,7,'130123','','正定县','zheng ding xian');
_R3(0,2,7,'130124','','栾城区','luan cheng qu');
_R3(0,2,7,'130129','','赞皇县','zan huang xian');
_R3(0,2,7,'130127','','高邑县','gao yi xian');
_R3(0,2,7,'130128','','深泽县','shen ze xian');
_R3(0,2,7,'130132','','元氏县','yuan shi xian');
_R3(0,2,7,'130133','','赵县','zhao xian');
_R3(0,2,7,'130130','','无极县','wu ji xian');
_R3(0,2,7,'130131','','平山县','ping shan xian');
_R2(0,2,'130200','','唐山市','tang shan shi');
_R3(0,2,8,'130202','','路南区','lu nan qu');
_R3(0,2,8,'130203','','路北区','lu bei qu');
_R3(0,2,8,'130207','','丰南区','feng nan qu');
_R3(0,2,8,'130204','','古冶区','gu ye qu');
_R3(0,2,8,'130205','','开平区','kai ping qu');
_R3(0,2,8,'130208','','丰润区','feng run qu');
_R3(0,2,8,'130224','','滦南县','luan nan xian');
_R3(0,2,8,'130225','','乐亭县','lao ting xian');
_R3(0,2,8,'130223','','滦州市','luan zhou shi');
_R3(0,2,8,'130229','','玉田县','yu tian xian');
_R3(0,2,8,'130227','','迁西县','qian xi xian');
_R3(0,2,8,'130283','','迁安市','qian an shi');
_R3(0,2,8,'130281','','遵化市','zun hua shi');
_R3(0,2,8,'130284','','其它区','qi ta qu');
_R3(0,2,8,'130230','','曹妃甸区','cao fei dian qu');
_R2(0,2,'130500','','邢台市','xing tai shi');
_R3(0,2,9,'130583','','其它区','qi ta qu');
_R3(0,2,9,'130581','','南宫市','nan gong shi');
_R3(0,2,9,'130582','','沙河市','sha he shi');
_R3(0,2,9,'130522','','临城县','lin cheng xian');
_R3(0,2,9,'130525','','隆尧县','long yao xian');
_R3(0,2,9,'130526','','任泽区','ren ze qu');
_R3(0,2,9,'130523','','内丘县','nei qiu xian');
_R3(0,2,9,'130524','','柏乡县','bai xiang xian');
_R3(0,2,9,'130529','','巨鹿县','ju lu xian');
_R3(0,2,9,'130527','','南和区','nan he qu');
_R3(0,2,9,'130528','','宁晋县','ning jin xian');
_R3(0,2,9,'130532','','平乡县','ping xiang xian');
_R3(0,2,9,'130533','','威县','wei xian');
_R3(0,2,9,'130530','','新河县','xin he xian');
_R3(0,2,9,'130531','','广宗县','guang zong xian');
_R3(0,2,9,'130534','','清河县','qing he xian');
_R3(0,2,9,'130535','','临西县','lin xi xian');
_R3(0,2,9,'130503','','信都区','xin dou qu');
_R3(0,2,9,'130502','','襄都区','xiang dou qu');
_R2(0,2,'130700','','张家口市','zhang jia kou shi');
_R3(0,2,10,'130723','','康保县','kang bao xian');
_R3(0,2,10,'130724','','沽源县','gu yuan xian');
_R3(0,2,10,'130722','','张北县','zhang bei xian');
_R3(0,2,10,'130727','','阳原县','yang yuan xian');
_R3(0,2,10,'130728','','怀安县','huai an xian');
_R3(0,2,10,'130725','','尚义县','shang yi xian');
_R3(0,2,10,'130726','','蔚县','yu xian');
_R3(0,2,10,'130729','','万全区','wan quan qu');
_R3(0,2,10,'130730','','怀来县','huai lai xian');
_R3(0,2,10,'130731','','涿鹿县','zhuo lu xian');
_R3(0,2,10,'130734','','其它区','qi ta qu');
_R3(0,2,10,'130732','','赤城县','chi cheng xian');
_R3(0,2,10,'130733','','崇礼区','chong li qu');
_R3(0,2,10,'130702','','桥东区','qiao dong qu');
_R3(0,2,10,'130705','','宣化区','xuan hua qu');
_R3(0,2,10,'130706','','下花园区','xia hua yuan qu');
_R3(0,2,10,'130703','','桥西区','qiao xi qu');

_R1(0,'140000','','山西省','shan xi sheng','山西省');
_R2(0,3,'140400','','长治市','chang zhi shi');
_R3(0,3,0,'140403','','潞州区','lu zhou qu');
_R3(0,3,0,'140421','','上党区','shang dang qu');
_R3(0,3,0,'140423','','襄垣县','xiang yuan xian');
_R3(0,3,0,'140424','','屯留区','tun liu qu');
_R3(0,3,0,'140425','','平顺县','ping shun xian');
_R3(0,3,0,'140426','','黎城县','li cheng xian');
_R3(0,3,0,'140427','','壶关县','hu guan xian');
_R3(0,3,0,'140428','','长子县','zhang zi xian');
_R3(0,3,0,'140429','','武乡县','wu xiang xian');
_R3(0,3,0,'140481','','潞城区','lu cheng qu');
_R3(0,3,0,'140485','','其它区','qi ta qu');
_R3(0,3,0,'140430','','沁县','qin xian');
_R3(0,3,0,'140431','','沁源县','qin yuan xian');
_R2(0,3,'140200','','大同市','da tong shi');
_R3(0,3,1,'140212','','新荣区','xin rong qu');
_R3(0,3,1,'140213','','平城区','ping cheng qu');
_R3(0,3,1,'140214','','云冈区','yun gang qu');
_R3(0,3,1,'140221','','阳高县','yang gao xian');
_R3(0,3,1,'140222','','天镇县','tian zhen xian');
_R3(0,3,1,'140223','','广灵县','guang ling xian');
_R3(0,3,1,'140224','','灵丘县','ling qiu xian');
_R3(0,3,1,'140225','','浑源县','hun yuan xian');
_R3(0,3,1,'140226','','左云县','zuo yun xian');
_R3(0,3,1,'140227','','云州区','yun zhou qu');
_R3(0,3,1,'140228','','其它区','qi ta qu');
_R2(0,3,'140500','','晋城市','jin cheng shi');
_R3(0,3,2,'140581','','高平市','gao ping shi');
_R3(0,3,2,'140582','','其它区','qi ta qu');
_R3(0,3,2,'140521','','沁水县','qin shui xian');
_R3(0,3,2,'140522','','阳城县','yang cheng xian');
_R3(0,3,2,'140524','','陵川县','ling chuan xian');
_R3(0,3,2,'140525','','泽州县','ze zhou xian');
_R3(0,3,2,'140502','','城区','cheng qu');
_R2(0,3,'140700','','晋中市','jin zhong shi');
_R3(0,3,3,'140702','','榆次区','yu ci qu');
_R3(0,3,3,'140781','','介休市','jie xiu shi');
_R3(0,3,3,'140782','','其它区','qi ta qu');
_R3(0,3,3,'140728','','平遥县','ping yao xian');
_R3(0,3,3,'140729','','灵石县','ling shi xian');
_R3(0,3,3,'140721','','榆社县','yu she xian');
_R3(0,3,3,'140722','','左权县','zuo quan xian');
_R3(0,3,3,'140723','','和顺县','he shun xian');
_R3(0,3,3,'140724','','昔阳县','xi yang xian');
_R3(0,3,3,'140725','','寿阳县','shou yang xian');
_R3(0,3,3,'140726','','太谷区','tai gu qu');
_R3(0,3,3,'140727','','祁县','qi xian');
_R2(0,3,'141000','','临汾市','lin fen shi');
_R3(0,3,4,'141081','','侯马市','hou ma shi');
_R3(0,3,4,'141083','','其它区','qi ta qu');
_R3(0,3,4,'141082','','霍州市','huo zhou shi');
_R3(0,3,4,'141002','','尧都区','yao du qu');
_R3(0,3,4,'141030','','大宁县','da ning xian');
_R3(0,3,4,'141021','','曲沃县','qu wo xian');
_R3(0,3,4,'141023','','襄汾县','xiang fen xian');
_R3(0,3,4,'141022','','翼城县','yi cheng xian');
_R3(0,3,4,'141025','','古县','gu xian');
_R3(0,3,4,'141024','','洪洞县','hong tong xian');
_R3(0,3,4,'141027','','浮山县','fu shan xian');
_R3(0,3,4,'141026','','安泽县','an ze xian');
_R3(0,3,4,'141029','','乡宁县','xiang ning xian');
_R3(0,3,4,'141028','','吉县','ji xian');
_R3(0,3,4,'141032','','永和县','yong he xian');
_R3(0,3,4,'141031','','隰县','xi xian');
_R3(0,3,4,'141034','','汾西县','fen xi xian');
_R3(0,3,4,'141033','','蒲县','pu xian');
_R2(0,3,'141100','','吕梁市','lv liang shi');
_R3(0,3,5,'141102','','离石区','li shi qu');
_R3(0,3,5,'141182','','汾阳市','fen yang shi');
_R3(0,3,5,'141181','','孝义市','xiao yi shi');
_R3(0,3,5,'141183','','其它区','qi ta qu');
_R3(0,3,5,'141122','','交城县','jiao cheng xian');
_R3(0,3,5,'141121','','文水县','wen shui xian');
_R3(0,3,5,'141124','','临县','lin xian');
_R3(0,3,5,'141123','','兴县','xing xian');
_R3(0,3,5,'141126','','石楼县','shi lou xian');
_R3(0,3,5,'141125','','柳林县','liu lin xian');
_R3(0,3,5,'141128','','方山县','fang shan xian');
_R3(0,3,5,'141127','','岚县','lan xian');
_R3(0,3,5,'141129','','中阳县','zhong yang xian');
_R3(0,3,5,'141130','','交口县','jiao kou xian');
_R2(0,3,'140600','','朔州市','shuo zhou shi');
_R3(0,3,6,'140602','','朔城区','shuo cheng qu');
_R3(0,3,6,'140603','','平鲁区','ping lu qu');
_R3(0,3,6,'140621','','山阴县','shan yin xian');
_R3(0,3,6,'140622','','应县','ying xian');
_R3(0,3,6,'140623','','右玉县','you yu xian');
_R3(0,3,6,'140624','','怀仁市','huai ren shi');
_R3(0,3,6,'140625','','其它区','qi ta qu');
_R2(0,3,'140100','','太原市','tai yuan shi');
_R3(0,3,7,'140110','','晋源区','jin yuan qu');
_R3(0,3,7,'140121','','清徐县','qing xu xian');
_R3(0,3,7,'140122','','阳曲县','yang qu xian');
_R3(0,3,7,'140123','','娄烦县','lou fan xian');
_R3(0,3,7,'140105','','小店区','xiao dian qu');
_R3(0,3,7,'140106','','迎泽区','ying ze qu');
_R3(0,3,7,'140107','','杏花岭区','xing hua ling qu');
_R3(0,3,7,'140108','','尖草坪区','jian cao ping qu');
_R3(0,3,7,'140109','','万柏林区','wan bai lin qu');
_R3(0,3,7,'140181','','古交市','gu jiao shi');
_R3(0,3,7,'140182','','其它区','qi ta qu');
_R2(0,3,'140900','','忻州市','xin zhou shi');
_R3(0,3,8,'140926','','静乐县','jing le xian');
_R3(0,3,8,'140927','','神池县','shen chi xian');
_R3(0,3,8,'140928','','五寨县','wu zhai xian');
_R3(0,3,8,'140929','','岢岚县','ke lan xian');
_R3(0,3,8,'140921','','定襄县','ding xiang xian');
_R3(0,3,8,'140922','','五台县','wu tai xian');
_R3(0,3,8,'140923','','代县','dai xian');
_R3(0,3,8,'140924','','繁峙县','fan shi xian');
_R3(0,3,8,'140925','','宁武县','ning wu xian');
_R3(0,3,8,'140930','','河曲县','he qu xian');
_R3(0,3,8,'140931','','保德县','bao de xian');
_R3(0,3,8,'140932','','偏关县','pian guan xian');
_R3(0,3,8,'140902','','忻府区','xin fu qu');
_R3(0,3,8,'140981','','原平市','yuan ping shi');
_R3(0,3,8,'140982','','其它区','qi ta qu');
_R2(0,3,'140300','','阳泉市','yang quan shi');
_R3(0,3,9,'140311','','郊区','jiao qu');
_R3(0,3,9,'140321','','平定县','ping ding xian');
_R3(0,3,9,'140322','','盂县','yu xian');
_R3(0,3,9,'140323','','其它区','qi ta qu');
_R3(0,3,9,'140302','','城区','cheng qu');
_R3(0,3,9,'140303','','矿区','kuang qu');
_R2(0,3,'140800','','运城市','yun cheng shi');
_R3(0,3,10,'140802','','盐湖区','yan hu qu');
_R3(0,3,10,'140827','','垣曲县','yuan qu xian');
_R3(0,3,10,'140828','','夏县','xia xian');
_R3(0,3,10,'140829','','平陆县','ping lu xian');
_R3(0,3,10,'140821','','临猗县','lin yi xian');
_R3(0,3,10,'140822','','万荣县','wan rong xian');
_R3(0,3,10,'140823','','闻喜县','wen xi xian');
_R3(0,3,10,'140824','','稷山县','ji shan xian');
_R3(0,3,10,'140825','','新绛县','xin jiang xian');
_R3(0,3,10,'140826','','绛县','jiang xian');
_R3(0,3,10,'140881','','永济市','yong ji shi');
_R3(0,3,10,'140882','','河津市','he jin shi');
_R3(0,3,10,'140883','','其它区','qi ta qu');
_R3(0,3,10,'140830','','芮城县','rui cheng xian');

_R1(0,'150000','','内蒙古','nei meng gu zi zhi qu','内蒙古自治区');
_R2(0,4,'152900','','阿拉善盟','a la shan meng');
_R3(0,4,0,'152924','','其它区','qi ta qu');
_R3(0,4,0,'152923','','额济纳旗','e ji na qi');
_R3(0,4,0,'152922','','阿拉善右旗','a la shan you qi');
_R3(0,4,0,'152921','','阿拉善左旗','a la shan zuo qi');
_R2(0,4,'150800','','巴彦淖尔市','ba yan nao er shi');
_R3(0,4,1,'150802','','临河区','lin he qu');
_R3(0,4,1,'150826','','杭锦后旗','hang jin hou qi');
_R3(0,4,1,'150827','','其它区','qi ta qu');
_R3(0,4,1,'150824','','乌拉特中旗','wu la te zhong qi');
_R3(0,4,1,'150825','','乌拉特后旗','wu la te hou qi');
_R3(0,4,1,'150822','','磴口县','deng kou xian');
_R3(0,4,1,'150823','','乌拉特前旗','wu la te qian qi');
_R3(0,4,1,'150821','','五原县','wu yuan xian');
_R2(0,4,'150200','','包头市','bao tou shi');
_R3(0,4,2,'150206','','白云鄂博矿区','bai yun e bo kuang qu');
_R3(0,4,2,'150207','','九原区','jiu yuan qu');
_R3(0,4,2,'150204','','青山区','qing shan qu');
_R3(0,4,2,'150205','','石拐区','shi guai qu');
_R3(0,4,2,'150202','','东河区','dong he qu');
_R3(0,4,2,'150203','','昆都仑区','kun du lun qu');
_R3(0,4,2,'150224','','其它区','qi ta qu');
_R3(0,4,2,'150222','','固阳县','gu yang xian');
_R3(0,4,2,'150223','','达尔罕茂明安联合旗','da er han mao ming an lian he qi');
_R3(0,4,2,'150221','','土默特右旗','tu mo te you qi');
_R2(0,4,'150400','','赤峰市','chi feng shi');
_R3(0,4,3,'150431','','其它区','qi ta qu');
_R3(0,4,3,'150430','','敖汉旗','ao han qi');
_R3(0,4,3,'150404','','松山区','song shan qu');
_R3(0,4,3,'150402','','红山区','hong shan qu');
_R3(0,4,3,'150403','','元宝山区','yuan bao shan qu');
_R3(0,4,3,'150428','','喀喇沁旗','ka la qin qi');
_R3(0,4,3,'150429','','宁城县','ning cheng xian');
_R3(0,4,3,'150426','','翁牛特旗','weng niu te qi');
_R3(0,4,3,'150424','','林西县','lin xi xian');
_R3(0,4,3,'150425','','克什克腾旗','ke shi ke teng qi');
_R3(0,4,3,'150422','','巴林左旗','ba lin zuo qi');
_R3(0,4,3,'150423','','巴林右旗','ba lin you qi');
_R3(0,4,3,'150421','','阿鲁科尔沁旗','a lu ke er qin qi');
_R2(0,4,'150600','','鄂尔多斯市','e er duo si shi');
_R3(0,4,4,'150602','','东胜区','dong sheng qu');
_R3(0,4,4,'150603','','康巴什区','kang ba shen qu');
_R3(0,4,4,'150628','','其它区','qi ta qu');
_R3(0,4,4,'150626','','乌审旗','wu shen qi');
_R3(0,4,4,'150627','','伊金霍洛旗','yi jin huo luo qi');
_R3(0,4,4,'150624','','鄂托克旗','e tuo ke qi');
_R3(0,4,4,'150625','','杭锦旗','hang jin qi');
_R3(0,4,4,'150622','','准格尔旗','zhun ge er qi');
_R3(0,4,4,'150623','','鄂托克前旗','e tuo ke qian qi');
_R3(0,4,4,'150621','','达拉特旗','da la te qi');
_R2(0,4,'150100','','呼和浩特市','hu he hao te shi');
_R3(0,4,5,'150125','','武川县','wu chuan xian');
_R3(0,4,5,'150126','','其它区','qi ta qu');
_R3(0,4,5,'150123','','和林格尔县','he lin ge er xian');
_R3(0,4,5,'150124','','清水河县','qing shui he xian');
_R3(0,4,5,'150121','','土默特左旗','tu mo te zuo qi');
_R3(0,4,5,'150122','','托克托县','tuo ke tuo xian');
_R3(0,4,5,'150105','','赛罕区','sai han qu');
_R3(0,4,5,'150103','','回民区','hui min qu');
_R3(0,4,5,'150104','','玉泉区','yu quan qu');
_R3(0,4,5,'150102','','新城区','xin cheng qu');
_R2(0,4,'150700','','呼伦贝尔市','hu lun bei er shi');
_R3(0,4,6,'150727','','新巴尔虎右旗','xin ba er hu you qi');
_R3(0,4,6,'150725','','陈巴尔虎旗','chen ba er hu qi');
_R3(0,4,6,'150726','','新巴尔虎左旗','xin ba er hu zuo qi');
_R3(0,4,6,'150723','','鄂伦春自治旗','e lun chun zi zhi qi');
_R3(0,4,6,'150724','','鄂温克族自治旗','e wen ke zu zi zhi qi');
_R3(0,4,6,'150721','','阿荣旗','a rong qi');
_R3(0,4,6,'150722','','莫力达瓦达斡尔族自治旗','mo li da wa da wo er zu zi zhi qi');
_R3(0,4,6,'150703','','扎赉诺尔区','zha lai nuo er qu');
_R3(0,4,6,'150702','','海拉尔区','hai la er qu');
_R3(0,4,6,'150785','','根河市','gen he shi');
_R3(0,4,6,'150786','','其它区','qi ta qu');
_R3(0,4,6,'150783','','扎兰屯市','zha lan tun shi');
_R3(0,4,6,'150784','','额尔古纳市','e er gu na shi');
_R3(0,4,6,'150781','','满洲里市','man zhou li shi');
_R3(0,4,6,'150782','','牙克石市','ya ke shi shi');
_R2(0,4,'150500','','通辽市','tong liao shi');
_R3(0,4,7,'150502','','科尔沁区','ke er qin qu');
_R3(0,4,7,'150581','','霍林郭勒市','huo lin guo le shi');
_R3(0,4,7,'150582','','其它区','qi ta qu');
_R3(0,4,7,'150525','','奈曼旗','nai man qi');
_R3(0,4,7,'150526','','扎鲁特旗','zha lu te qi');
_R3(0,4,7,'150523','','开鲁县','kai lu xian');
_R3(0,4,7,'150524','','库伦旗','ku lun qi');
_R3(0,4,7,'150521','','科尔沁左翼中旗','ke er qin zuo yi zhong qi');
_R3(0,4,7,'150522','','科尔沁左翼后旗','ke er qin zuo yi hou qi');
_R2(0,4,'150300','','乌海市','wu hai shi');
_R3(0,4,8,'150305','','其它区','qi ta qu');
_R3(0,4,8,'150303','','海南区','hai nan qu');
_R3(0,4,8,'150304','','乌达区','wu da qu');
_R3(0,4,8,'150302','','海勃湾区','hai bo wan qu');
_R2(0,4,'150900','','乌兰察布市','wu lan cha bu shi');
_R3(0,4,9,'150982','','其它区','qi ta qu');
_R3(0,4,9,'150981','','丰镇市','feng zhen shi');
_R3(0,4,9,'150929','','四子王旗','si zi wang qi');
_R3(0,4,9,'150928','','察哈尔右翼后旗','cha ha er you yi hou qi');
_R3(0,4,9,'150927','','察哈尔右翼中旗','cha ha er you yi zhong qi');
_R3(0,4,9,'150926','','察哈尔右翼前旗','cha ha er you yi qian qi');
_R3(0,4,9,'150925','','凉城县','liang cheng xian');
_R3(0,4,9,'150924','','兴和县','xing he xian');
_R3(0,4,9,'150923','','商都县','shang du xian');
_R3(0,4,9,'150922','','化德县','hua de xian');
_R3(0,4,9,'150921','','卓资县','zhuo zi xian');
_R3(0,4,9,'150902','','集宁区','ji ning qu');
_R2(0,4,'152500','','锡林郭勒盟','xi lin guo le meng');
_R3(0,4,10,'152532','','其它区','qi ta qu');
_R3(0,4,10,'152531','','多伦县','duo lun xian');
_R3(0,4,10,'152530','','正蓝旗','zheng lan qi');
_R3(0,4,10,'152502','','锡林浩特市','xi lin hao te shi');
_R3(0,4,10,'152501','','二连浩特市','er lian hao te shi');
_R3(0,4,10,'152529','','正镶白旗','zheng xiang bai qi');
_R3(0,4,10,'152528','','镶黄旗','xiang huang qi');
_R3(0,4,10,'152527','','太仆寺旗','tai pu si qi');
_R3(0,4,10,'152526','','西乌珠穆沁旗','xi wu zhu mu qin qi');
_R3(0,4,10,'152525','','东乌珠穆沁旗','dong wu zhu mu qin qi');
_R3(0,4,10,'152524','','苏尼特右旗','su ni te you qi');
_R3(0,4,10,'152523','','苏尼特左旗','su ni te zuo qi');
_R3(0,4,10,'152522','','阿巴嘎旗','a ba ga qi');
_R2(0,4,'152200','','兴安盟','xing an meng');
_R3(0,4,11,'152225','','其它区','qi ta qu');
_R3(0,4,11,'152224','','突泉县','tu quan xian');
_R3(0,4,11,'152223','','扎赉特旗','zha lai te qi');
_R3(0,4,11,'152222','','科尔沁右翼中旗','ke er qin you yi zhong qi');
_R3(0,4,11,'152221','','科尔沁右翼前旗','ke er qin you yi qian qi');
_R3(0,4,11,'152202','','阿尔山市','a er shan shi');
_R3(0,4,11,'152201','','乌兰浩特市','wu lan hao te shi');

_R1(0,'210000','','辽宁省','liao ning sheng','辽宁省');
_R2(0,5,'210300','','鞍山市','an shan shi');
_R3(0,5,0,'210304','','立山区','li shan qu');
_R3(0,5,0,'210303','','铁西区','tie xi qu');
_R3(0,5,0,'210302','','铁东区','tie dong qu');
_R3(0,5,0,'210311','','千山区','qian shan qu');
_R3(0,5,0,'210323','','岫岩满族自治县','xiu yan man zu zi zhi xian');
_R3(0,5,0,'210321','','台安县','tai an xian');
_R3(0,5,0,'210381','','海城市','hai cheng shi');
_R3(0,5,0,'210382','','其它区','qi ta qu');
_R2(0,5,'210500','','本溪市','ben xi shi');
_R3(0,5,1,'210505','','南芬区','nan fen qu');
_R3(0,5,1,'210504','','明山区','ming shan qu');
_R3(0,5,1,'210503','','溪湖区','xi hu qu');
_R3(0,5,1,'210502','','平山区','ping shan qu');
_R3(0,5,1,'210521','','本溪满族自治县','ben xi man zu zi zhi xian');
_R3(0,5,1,'210523','','其它区','qi ta qu');
_R3(0,5,1,'210522','','桓仁满族自治县','huan ren man zu zi zhi xian');
_R2(0,5,'211300','','朝阳市','chao yang shi');
_R3(0,5,2,'211302','','双塔区','shuang ta qu');
_R3(0,5,2,'211303','','龙城区','long cheng qu');
_R3(0,5,2,'211324','','喀喇沁左翼蒙古族自治县','ka la qin zuo yi meng gu zu zi zhi xian');
_R3(0,5,2,'211322','','建平县','jian ping xian');
_R3(0,5,2,'211321','','朝阳县','chao yang xian');
_R3(0,5,2,'211382','','凌源市','ling yuan shi');
_R3(0,5,2,'211381','','北票市','bei piao shi');
_R3(0,5,2,'211383','','其它区','qi ta qu');
_R2(0,5,'210200','','大连市','da lian shi');
_R3(0,5,3,'210202','','中山区','zhong shan qu');
_R3(0,5,3,'210204','','沙河口区','sha he kou qu');
_R3(0,5,3,'210203','','西岗区','xi gang qu');
_R3(0,5,3,'210213','','金州区','jin zhou qu');
_R3(0,5,3,'210212','','旅顺口区','lv shun kou qu');
_R3(0,5,3,'210211','','甘井子区','gan jing zi qu');
_R3(0,5,3,'210224','','长海县','chang hai xian');
_R3(0,5,3,'210282','','普兰店区','pu lan dian qu');
_R3(0,5,3,'210281','','瓦房店市','wa fang dian shi');
_R3(0,5,3,'210283','','庄河市','zhuang he shi');
_R3(0,5,3,'210298','','其它区','qi ta qu');
_R2(0,5,'210600','','丹东市','dan dong shi');
_R3(0,5,4,'210604','','振安区','zhen an qu');
_R3(0,5,4,'210603','','振兴区','zhen xing qu');
_R3(0,5,4,'210602','','元宝区','yuan bao qu');
_R3(0,5,4,'210624','','宽甸满族自治县','kuan dian man zu zi zhi xian');
_R3(0,5,4,'210682','','凤城市','feng cheng shi');
_R3(0,5,4,'210681','','东港市','dong gang shi');
_R3(0,5,4,'210683','','其它区','qi ta qu');
_R2(0,5,'210400','','抚顺市','fu shun shi');
_R3(0,5,5,'210404','','望花区','wang hua qu');
_R3(0,5,5,'210403','','东洲区','dong zhou qu');
_R3(0,5,5,'210402','','新抚区','xin fu qu');
_R3(0,5,5,'210411','','顺城区','shun cheng qu');
_R3(0,5,5,'210422','','新宾满族自治县','xin bin man zu zi zhi xian');
_R3(0,5,5,'210421','','抚顺县','fu shun xian');
_R3(0,5,5,'210424','','其它区','qi ta qu');
_R3(0,5,5,'210423','','清原满族自治县','qing yuan man zu zi zhi xian');
_R2(0,5,'210900','','阜新市','fu xin shi');
_R3(0,5,6,'210903','','新邱区','xin qiu qu');
_R3(0,5,6,'210902','','海州区','hai zhou qu');
_R3(0,5,6,'210905','','清河门区','qing he men qu');
_R3(0,5,6,'210904','','太平区','tai ping qu');
_R3(0,5,6,'210911','','细河区','xi he qu');
_R3(0,5,6,'210923','','其它区','qi ta qu');
_R3(0,5,6,'210922','','彰武县','zhang wu xian');
_R3(0,5,6,'210921','','阜新蒙古族自治县','fu xin meng gu zu zi zhi xian');
_R2(0,5,'211400','','葫芦岛市','hu lu dao shi');
_R3(0,5,7,'211404','','南票区','nan piao qu');
_R3(0,5,7,'211403','','龙港区','long gang qu');
_R3(0,5,7,'211402','','连山区','lian shan qu');
_R3(0,5,7,'211422','','建昌县','jian chang xian');
_R3(0,5,7,'211421','','绥中县','sui zhong xian');
_R3(0,5,7,'211481','','兴城市','xing cheng shi');
_R3(0,5,7,'211482','','其它区','qi ta qu');
_R2(0,5,'210700','','锦州市','jin zhou shi');
_R3(0,5,8,'210703','','凌河区','ling he qu');
_R3(0,5,8,'210702','','古塔区','gu ta qu');
_R3(0,5,8,'210711','','太和区','tai he qu');
_R3(0,5,8,'210727','','义县','yi xian');
_R3(0,5,8,'210726','','黑山县','hei shan xian');
_R3(0,5,8,'210781','','凌海市','ling hai shi');
_R3(0,5,8,'210783','','其它区','qi ta qu');
_R3(0,5,8,'210782','','北镇市','bei zhen shi');
_R2(0,5,'211000','','辽阳市','liao yang shi');
_R3(0,5,9,'211082','','其它区','qi ta qu');
_R3(0,5,9,'211081','','灯塔市','deng ta shi');
_R3(0,5,9,'211011','','太子河区','tai zi he qu');
_R3(0,5,9,'211005','','弓长岭区','gong chang ling qu');
_R3(0,5,9,'211004','','宏伟区','hong wei qu');
_R3(0,5,9,'211003','','文圣区','wen sheng qu');
_R3(0,5,9,'211002','','白塔区','bai ta qu');
_R3(0,5,9,'211021','','辽阳县','liao yang xian');
_R2(0,5,'211100','','盘锦市','pan jin shi');
_R3(0,5,10,'211103','','兴隆台区','xing long tai qu');
_R3(0,5,10,'211102','','双台子区','shuang tai zi qu');
_R3(0,5,10,'211122','','盘山县','pan shan xian');
_R3(0,5,10,'211121','','大洼区','da wa qu');
_R3(0,5,10,'211123','','其它区','qi ta qu');
_R2(0,5,'210100','','沈阳市','shen yang shi');
_R3(0,5,11,'210103','','沈河区','shen he qu');
_R3(0,5,11,'210102','','和平区','he ping qu');
_R3(0,5,11,'210106','','铁西区','tie xi qu');
_R3(0,5,11,'210105','','皇姑区','huang gu qu');
_R3(0,5,11,'210104','','大东区','da dong qu');
_R3(0,5,11,'210114','','于洪区','yu hong qu');
_R3(0,5,11,'210112','','浑南区','hun nan qu');
_R3(0,5,11,'210111','','苏家屯区','su jia tun qu');
_R3(0,5,11,'210124','','法库县','fa ku xian');
_R3(0,5,11,'210123','','康平县','kang ping xian');
_R3(0,5,11,'210122','','辽中区','liao zhong qu');
_R3(0,5,11,'210181','','新民市','xin min shi');
_R3(0,5,11,'210185','','其它区','qi ta qu');
_R3(0,5,11,'210184','','沈北新区','shen bei xin qu');
_R2(0,5,'211200','','铁岭市','tie ling shi');
_R3(0,5,12,'211202','','银州区','yin zhou qu');
_R3(0,5,12,'211204','','清河区','qing he qu');
_R3(0,5,12,'211221','','铁岭县','tie ling xian');
_R3(0,5,12,'211224','','昌图县','chang tu xian');
_R3(0,5,12,'211223','','西丰县','xi feng xian');
_R3(0,5,12,'211283','','其它区','qi ta qu');
_R3(0,5,12,'211282','','开原市','kai yuan shi');
_R3(0,5,12,'211281','','调兵山市','diao bing shan shi');
_R2(0,5,'210800','','营口市','ying kou shi');
_R3(0,5,13,'210804','','鲅鱼圈区','ba yu quan qu');
_R3(0,5,13,'210803','','西市区','xi shi qu');
_R3(0,5,13,'210802','','站前区','zhan qian qu');
_R3(0,5,13,'210811','','老边区','lao bian qu');
_R3(0,5,13,'210883','','其它区','qi ta qu');
_R3(0,5,13,'210882','','大石桥市','da shi qiao shi');
_R3(0,5,13,'210881','','盖州市','gai zhou shi');

_R1(0,'220000','','吉林省','ji lin sheng','吉林省');
_R2(0,6,'220800','','白城市','bai cheng shi');
_R3(0,6,0,'220802','','洮北区','tao bei qu');
_R3(0,6,0,'220822','','通榆县','tong yu xian');
_R3(0,6,0,'220821','','镇赉县','zhen lai xian');
_R3(0,6,0,'220881','','洮南市','tao nan shi');
_R3(0,6,0,'220883','','其它区','qi ta qu');
_R3(0,6,0,'220882','','大安市','da an shi');
_R2(0,6,'220600','','白山市','bai shan shi');
_R3(0,6,1,'220602','','浑江区','hun jiang qu');
_R3(0,6,1,'220625','','江源区','jiang yuan qu');
_R3(0,6,1,'220621','','抚松县','fu song xian');
_R3(0,6,1,'220623','','长白朝鲜族自治县','chang bai chao xian zu zi zhi xian');
_R3(0,6,1,'220622','','靖宇县','jing yu xian');
_R3(0,6,1,'220681','','临江市','lin jiang shi');
_R3(0,6,1,'220682','','其它区','qi ta qu');
_R2(0,6,'220100','','长春市','chang chun shi');
_R3(0,6,2,'220381','','公主岭市','gong zhu ling shi');
_R3(0,6,2,'220112','','双阳区','shuang yang qu');
_R3(0,6,2,'220102','','南关区','nan guan qu');
_R3(0,6,2,'220104','','朝阳区','chao yang qu');
_R3(0,6,2,'220103','','宽城区','kuan cheng qu');
_R3(0,6,2,'220106','','绿园区','lv yuan qu');
_R3(0,6,2,'220105','','二道区','er dao qu');
_R3(0,6,2,'220122','','农安县','nong an xian');
_R3(0,6,2,'220182','','榆树市','yu shu shi');
_R3(0,6,2,'220181','','九台区','jiu tai qu');
_R3(0,6,2,'220183','','德惠市','de hui shi');
_R3(0,6,2,'220188','','其它区','qi ta qu');
_R2(0,6,'220200','','吉林市','ji lin shi');
_R3(0,6,3,'220211','','丰满区','feng man qu');
_R3(0,6,3,'220203','','龙潭区','long tan qu');
_R3(0,6,3,'220202','','昌邑区','chang yi qu');
_R3(0,6,3,'220204','','船营区','chuan ying qu');
_R3(0,6,3,'220221','','永吉县','yong ji xian');
_R3(0,6,3,'220281','','蛟河市','jiao he shi');
_R3(0,6,3,'220283','','舒兰市','shu lan shi');
_R3(0,6,3,'220282','','桦甸市','hua dian shi');
_R3(0,6,3,'220285','','其它区','qi ta qu');
_R3(0,6,3,'220284','','磐石市','pan shi shi');
_R2(0,6,'220400','','辽源市','liao yuan shi');
_R3(0,6,4,'220403','','西安区','xi an qu');
_R3(0,6,4,'220402','','龙山区','long shan qu');
_R3(0,6,4,'220421','','东丰县','dong feng xian');
_R3(0,6,4,'220423','','其它区','qi ta qu');
_R3(0,6,4,'220422','','东辽县','dong liao xian');
_R2(0,6,'220300','','四平市','si ping shi');
_R3(0,6,5,'220302','','铁西区','tie xi qu');
_R3(0,6,5,'220303','','铁东区','tie dong qu');
_R3(0,6,5,'220322','','梨树县','li shu xian');
_R3(0,6,5,'220323','','伊通满族自治县','yi tong man zu zi zhi xian');
_R3(0,6,5,'220382','','双辽市','shuang liao shi');
_R3(0,6,5,'220383','','其它区','qi ta qu');
_R2(0,6,'220700','','松原市','song yuan shi');
_R3(0,6,6,'220702','','宁江区','ning jiang qu');
_R3(0,6,6,'220724','','扶余市','fu yu shi');
_R3(0,6,6,'220723','','乾安县','qian an xian');
_R3(0,6,6,'220725','','其它区','qi ta qu');
_R3(0,6,6,'220722','','长岭县','chang ling xian');
_R3(0,6,6,'220721','','前郭尔罗斯蒙古族自治县','qian guo er luo si meng gu zu zi zhi xian');
_R2(0,6,'220500','','通化市','tong hua shi');
_R3(0,6,7,'220503','','二道江区','er dao jiang qu');
_R3(0,6,7,'220502','','东昌区','dong chang qu');
_R3(0,6,7,'220521','','通化县','tong hua xian');
_R3(0,6,7,'220524','','柳河县','liu he xian');
_R3(0,6,7,'220523','','辉南县','hui nan xian');
_R3(0,6,7,'220582','','集安市','ji an shi');
_R3(0,6,7,'220581','','梅河口市','mei he kou shi');
_R3(0,6,7,'220583','','其它区','qi ta qu');
_R2(0,6,'222400','','延边朝鲜族自治州','yan bian chao xian zu zi zhi zhou');
_R3(0,6,8,'222406','','和龙市','he long shi');
_R3(0,6,8,'222401','','延吉市','yan ji shi');
_R3(0,6,8,'222404','','珲春市','hun chun shi');
_R3(0,6,8,'222405','','龙井市','long jing shi');
_R3(0,6,8,'222402','','图们市','tu men shi');
_R3(0,6,8,'222403','','敦化市','dun hua shi');
_R3(0,6,8,'222426','','安图县','an tu xian');
_R3(0,6,8,'222427','','其它区','qi ta qu');
_R3(0,6,8,'222424','','汪清县','wang qing xian');

_R1(0,'230000','','黑龙江省','hei long jiang sheng','黑龙江省');
_R2(0,7,'230600','','大庆市','da qing shi');
_R3(0,7,0,'230606','','大同区','da tong qu');
_R3(0,7,0,'230605','','红岗区','hong gang qu');
_R3(0,7,0,'230604','','让胡路区','rang hu lu qu');
_R3(0,7,0,'230603','','龙凤区','long feng qu');
_R3(0,7,0,'230602','','萨尔图区','sa er tu qu');
_R3(0,7,0,'230622','','肇源县','zhao yuan xian');
_R3(0,7,0,'230621','','肇州县','zhao zhou xian');
_R3(0,7,0,'230625','','其它区','qi ta qu');
_R3(0,7,0,'230624','','杜尔伯特蒙古族自治县','du er bo te meng gu zu zi zhi xian');
_R3(0,7,0,'230623','','林甸县','lin dian xian');
_R2(0,7,'232700','','大兴安岭地区','da xing an ling di qu');
_R3(0,7,1,'232724','','加格达奇区','jia ge da qi qu');
_R3(0,7,1,'232725','','其它区','qi ta qu');
_R3(0,7,1,'232721','','呼玛县','hu ma xian');
_R3(0,7,1,'232722','','塔河县','ta he xian');
_R3(0,7,1,'232723','','漠河市','mo he shi');
_R2(0,7,'230100','','哈尔滨市','ha er bin shi');
_R3(0,7,2,'230111','','呼兰区','hu lan qu');
_R3(0,7,2,'230104','','道外区','dao wai qu');
_R3(0,7,2,'230103','','南岗区','nan gang qu');
_R3(0,7,2,'230102','','道里区','dao li qu');
_R3(0,7,2,'230109','','松北区','song bei qu');
_R3(0,7,2,'230108','','平房区','ping fang qu');
_R3(0,7,2,'230106','','香坊区','xiang fang qu');
_R3(0,7,2,'230127','','木兰县','mu lan xian');
_R3(0,7,2,'230126','','巴彦县','ba yan xian');
_R3(0,7,2,'230125','','宾县','bin xian');
_R3(0,7,2,'230124','','方正县','fang zheng xian');
_R3(0,7,2,'230123','','依兰县','yi lan xian');
_R3(0,7,2,'230129','','延寿县','yan shou xian');
_R3(0,7,2,'230128','','通河县','tong he xian');
_R3(0,7,2,'230184','','五常市','wu chang shi');
_R3(0,7,2,'230183','','尚志市','shang zhi shi');
_R3(0,7,2,'230182','','双城区','shuang cheng qu');
_R3(0,7,2,'230181','','阿城区','a cheng qu');
_R3(0,7,2,'230186','','其它区','qi ta qu');
_R2(0,7,'230400','','鹤岗市','he gang shi');
_R3(0,7,3,'230402','','向阳区','xiang yang qu');
_R3(0,7,3,'230407','','兴山区','xing shan qu');
_R3(0,7,3,'230406','','东山区','dong shan qu');
_R3(0,7,3,'230405','','兴安区','xing an qu');
_R3(0,7,3,'230404','','南山区','nan shan qu');
_R3(0,7,3,'230403','','工农区','gong nong qu');
_R3(0,7,3,'230423','','其它区','qi ta qu');
_R3(0,7,3,'230422','','绥滨县','sui bin xian');
_R3(0,7,3,'230421','','萝北县','luo bei xian');
_R2(0,7,'231100','','黑河市','hei he shi');
_R3(0,7,4,'231102','','爱辉区','ai hui qu');
_R3(0,7,4,'231121','','嫩江市','nen jiang shi');
_R3(0,7,4,'231123','','逊克县','xun ke xian');
_R3(0,7,4,'231124','','孙吴县','sun wu xian');
_R3(0,7,4,'231181','','北安市','bei an shi');
_R3(0,7,4,'231182','','五大连池市','wu da lian chi shi');
_R3(0,7,4,'231183','','其它区','qi ta qu');
_R2(0,7,'230300','','鸡西市','ji xi shi');
_R3(0,7,5,'230303','','恒山区','heng shan qu');
_R3(0,7,5,'230302','','鸡冠区','ji guan qu');
_R3(0,7,5,'230307','','麻山区','ma shan qu');
_R3(0,7,5,'230306','','城子河区','cheng zi he qu');
_R3(0,7,5,'230305','','梨树区','li shu qu');
_R3(0,7,5,'230304','','滴道区','di dao qu');
_R3(0,7,5,'230321','','鸡东县','ji dong xian');
_R3(0,7,5,'230383','','其它区','qi ta qu');
_R3(0,7,5,'230382','','密山市','mi shan shi');
_R3(0,7,5,'230381','','虎林市','hu lin shi');
_R2(0,7,'230800','','佳木斯市','jia mu si shi');
_R3(0,7,6,'230833','','抚远市','fu yuan shi');
_R3(0,7,6,'230828','','汤原县','tang yuan xian');
_R3(0,7,6,'230826','','桦川县','hua chuan xian');
_R3(0,7,6,'230822','','桦南县','hua nan xian');
_R3(0,7,6,'230883','','其它区','qi ta qu');
_R3(0,7,6,'230882','','富锦市','fu jin shi');
_R3(0,7,6,'230881','','同江市','tong jiang shi');
_R3(0,7,6,'230811','','郊区','jiao qu');
_R3(0,7,6,'230805','','东风区','dong feng qu');
_R3(0,7,6,'230804','','前进区','qian jin qu');
_R3(0,7,6,'230803','','向阳区','xiang yang qu');
_R2(0,7,'231000','','牡丹江市','mu dan jiang shi');
_R3(0,7,7,'231005','','西安区','xi an qu');
_R3(0,7,7,'231004','','爱民区','ai min qu');
_R3(0,7,7,'231003','','阳明区','yang ming qu');
_R3(0,7,7,'231002','','东安区','dong an qu');
_R3(0,7,7,'231025','','林口县','lin kou xian');
_R3(0,7,7,'231024','','东宁市','dong ning shi');
_R3(0,7,7,'231086','','其它区','qi ta qu');
_R3(0,7,7,'231085','','穆棱市','mu ling shi');
_R3(0,7,7,'231084','','宁安市','ning an shi');
_R3(0,7,7,'231083','','海林市','hai lin shi');
_R3(0,7,7,'231081','','绥芬河市','sui fen he shi');
_R2(0,7,'230200','','齐齐哈尔市','qi qi ha er shi');
_R3(0,7,8,'230204','','铁锋区','tie feng qu');
_R3(0,7,8,'230203','','建华区','jian hua qu');
_R3(0,7,8,'230202','','龙沙区','long sha qu');
_R3(0,7,8,'230208','','梅里斯达斡尔族区','mei li si da wo er zu qu');
_R3(0,7,8,'230207','','碾子山区','nian zi shan qu');
_R3(0,7,8,'230206','','富拉尔基区','fu la er ji qu');
_R3(0,7,8,'230205','','昂昂溪区','ang ang xi qu');
_R3(0,7,8,'230231','','拜泉县','bai quan xian');
_R3(0,7,8,'230230','','克东县','ke dong xian');
_R3(0,7,8,'230225','','甘南县','gan nan xian');
_R3(0,7,8,'230224','','泰来县','tai lai xian');
_R3(0,7,8,'230223','','依安县','yi an xian');
_R3(0,7,8,'230221','','龙江县','long jiang xian');
_R3(0,7,8,'230229','','克山县','ke shan xian');
_R3(0,7,8,'230227','','富裕县','fu yu xian');
_R3(0,7,8,'230282','','其它区','qi ta qu');
_R3(0,7,8,'230281','','讷河市','ne he shi');
_R2(0,7,'230900','','七台河市','qi tai he shi');
_R3(0,7,9,'230904','','茄子河区','qie zi he qu');
_R3(0,7,9,'230903','','桃山区','tao shan qu');
_R3(0,7,9,'230902','','新兴区','xin xing qu');
_R3(0,7,9,'230922','','其它区','qi ta qu');
_R3(0,7,9,'230921','','勃利县','bo li xian');
_R2(0,7,'230500','','双鸭山市','shuang ya shan shi');
_R3(0,7,10,'230506','','宝山区','bao shan qu');
_R3(0,7,10,'230505','','四方台区','si fang tai qu');
_R3(0,7,10,'230503','','岭东区','ling dong qu');
_R3(0,7,10,'230502','','尖山区','jian shan qu');
_R3(0,7,10,'230523','','宝清县','bao qing xian');
_R3(0,7,10,'230522','','友谊县','you yi xian');
_R3(0,7,10,'230521','','集贤县','ji xian xian');
_R3(0,7,10,'230525','','其它区','qi ta qu');
_R3(0,7,10,'230524','','饶河县','rao he xian');
_R2(0,7,'231200','','绥化市','sui hua shi');
_R3(0,7,11,'231202','','北林区','bei lin qu');
_R3(0,7,11,'231221','','望奎县','wang kui xian');
_R3(0,7,11,'231222','','兰西县','lan xi xian');
_R3(0,7,11,'231223','','青冈县','qing gang xian');
_R3(0,7,11,'231224','','庆安县','qing an xian');
_R3(0,7,11,'231225','','明水县','ming shui xian');
_R3(0,7,11,'231226','','绥棱县','sui ling xian');
_R3(0,7,11,'231281','','安达市','an da shi');
_R3(0,7,11,'231282','','肇东市','zhao dong shi');
_R3(0,7,11,'231283','','海伦市','hai lun shi');
_R3(0,7,11,'231284','','其它区','qi ta qu');
_R2(0,7,'230700','','伊春市','yi chun shi');
_R3(0,7,12,'230718','','乌翠区','wu cui qu');
_R3(0,7,12,'230717','','伊美区','yi mei qu');
_R3(0,7,12,'230704','','友好区','you hao qu');
_R3(0,7,12,'230703','','南岔县','nan cha xian');
_R3(0,7,12,'230725','','大箐山县','da qing shan xian');
_R3(0,7,12,'230724','','丰林县','feng lin xian');
_R3(0,7,12,'230723','','汤旺县','tang wang xian');
_R3(0,7,12,'230722','','嘉荫县','jia yin xian');
_R3(0,7,12,'230751','','金林区','jin lin qu');
_R3(0,7,12,'230782','','其它区','qi ta qu');
_R3(0,7,12,'230781','','铁力市','tie li shi');

_R1(0,'310000','','上海','shang hai','上海');
_R2(0,8,'310100','','上海市','shang hai shi');
_R3(0,8,0,'310230','','崇明区','chong ming qu');
_R3(0,8,0,'310231','','其它区','qi ta qu');
_R3(0,8,0,'310120','','奉贤区','feng xian qu');
_R3(0,8,0,'310112','','闵行区','min hang qu');
_R3(0,8,0,'310113','','宝山区','bao shan qu');
_R3(0,8,0,'310114','','嘉定区','jia ding qu');
_R3(0,8,0,'310115','','浦东新区','pu dong xin qu');
_R3(0,8,0,'310116','','金山区','jin shan qu');
_R3(0,8,0,'310117','','松江区','song jiang qu');
_R3(0,8,0,'310118','','青浦区','qing pu qu');
_R3(0,8,0,'310110','','杨浦区','yang pu qu');
_R3(0,8,0,'310101','','黄浦区','huang pu qu');
_R3(0,8,0,'310104','','徐汇区','xu hui qu');
_R3(0,8,0,'310105','','长宁区','chang ning qu');
_R3(0,8,0,'310106','','静安区','jing an qu');
_R3(0,8,0,'310107','','普陀区','pu tuo qu');
_R3(0,8,0,'310109','','虹口区','hong kou qu');

_R1(0,'320000','','江苏省','jiang su sheng','江苏省');
_R2(0,9,'320400','','常州市','chang zhou shi');
_R3(0,9,0,'320404','','钟楼区','zhong lou qu');
_R3(0,9,0,'320402','','天宁区','tian ning qu');
_R3(0,9,0,'320483','','其它区','qi ta qu');
_R3(0,9,0,'320481','','溧阳市','li yang shi');
_R3(0,9,0,'320482','','金坛区','jin tan qu');
_R3(0,9,0,'320412','','武进区','wu jin qu');
_R3(0,9,0,'320411','','新北区','xin bei qu');
_R2(0,9,'320800','','淮安市','huai an shi');
_R3(0,9,1,'320832','','其它区','qi ta qu');
_R3(0,9,1,'320830','','盱眙县','xu yi xian');
_R3(0,9,1,'320831','','金湖县','jin hu xian');
_R3(0,9,1,'320829','','洪泽区','hong ze qu');
_R3(0,9,1,'320826','','涟水县','lian shui xian');
_R3(0,9,1,'320803','','淮安区','huai an qu');
_R3(0,9,1,'320804','','淮阴区','huai yin qu');
_R3(0,9,1,'320802','','清江浦区','qing jiang pu qu');
_R2(0,9,'320700','','连云港市','lian yun gang shi');
_R3(0,9,2,'320724','','灌南县','guan nan xian');
_R3(0,9,2,'320725','','其它区','qi ta qu');
_R3(0,9,2,'320722','','东海县','dong hai xian');
_R3(0,9,2,'320723','','灌云县','guan yun xian');
_R3(0,9,2,'320721','','赣榆区','gan yu qu');
_R3(0,9,2,'320706','','海州区','hai zhou qu');
_R3(0,9,2,'320703','','连云区','lian yun qu');
_R2(0,9,'320100','','南京市','nan jing shi');
_R3(0,9,3,'320115','','江宁区','jiang ning qu');
_R3(0,9,3,'320116','','六合区','lu he qu');
_R3(0,9,3,'320113','','栖霞区','qi xia qu');
_R3(0,9,3,'320114','','雨花台区','yu hua tai qu');
_R3(0,9,3,'320111','','浦口区','pu kou qu');
_R3(0,9,3,'320126','','其它区','qi ta qu');
_R3(0,9,3,'320124','','溧水区','li shui qu');
_R3(0,9,3,'320125','','高淳区','gao chun qu');
_R3(0,9,3,'320106','','鼓楼区','gu lou qu');
_R3(0,9,3,'320104','','秦淮区','qin huai qu');
_R3(0,9,3,'320105','','建邺区','jian ye qu');
_R3(0,9,3,'320102','','玄武区','xuan wu qu');
_R2(0,9,'320600','','南通市','nan tong shi');
_R3(0,9,4,'320612','','通州区','tong zhou qu');
_R3(0,9,4,'320623','','如东县','ru dong xian');
_R3(0,9,4,'320621','','海安市','hai an shi');
_R3(0,9,4,'320602','','崇川区','chong chuan qu');
_R3(0,9,4,'320694','','其它区','qi ta qu');
_R3(0,9,4,'320681','','启东市','qi dong shi');
_R3(0,9,4,'320682','','如皋市','ru gao shi');
_R3(0,9,4,'320684','','海门区','hai men qu');
_R2(0,9,'321300','','宿迁市','su qian shi');
_R3(0,9,5,'321325','','其它区','qi ta qu');
_R3(0,9,5,'321323','','泗阳县','si yang xian');
_R3(0,9,5,'321324','','泗洪县','si hong xian');
_R3(0,9,5,'321322','','沭阳县','shu yang xian');
_R3(0,9,5,'321302','','宿城区','su cheng qu');
_R3(0,9,5,'321311','','宿豫区','su yu qu');
_R2(0,9,'320500','','苏州市','su zhou shi');
_R3(0,9,6,'320508','','姑苏区','gu su qu');
_R3(0,9,6,'320506','','吴中区','wu zhong qu');
_R3(0,9,6,'320507','','相城区','xiang cheng qu');
_R3(0,9,6,'320505','','虎丘区','hu qiu qu');
_R3(0,9,6,'320596','','其它区','qi ta qu');
_R3(0,9,6,'320582','','张家港市','zhang jia gang shi');
_R3(0,9,6,'320583','','昆山市','kun shan shi');
_R3(0,9,6,'320581','','常熟市','chang shu shi');
_R3(0,9,6,'320584','','吴江区','wu jiang qu');
_R3(0,9,6,'320585','','太仓市','tai cang shi');
_R2(0,9,'321200','','泰州市','tai zhou shi');
_R3(0,9,7,'321284','','姜堰区','jiang yan qu');
_R3(0,9,7,'321285','','其它区','qi ta qu');
_R3(0,9,7,'321282','','靖江市','jing jiang shi');
_R3(0,9,7,'321283','','泰兴市','tai xing shi');
_R3(0,9,7,'321281','','兴化市','xing hua shi');
_R3(0,9,7,'321202','','海陵区','hai ling qu');
_R3(0,9,7,'321203','','高港区','gao gang qu');
_R2(0,9,'320200','','无锡市','wu xi shi');
_R3(0,9,8,'320281','','江阴市','jiang yin shi');
_R3(0,9,8,'320282','','宜兴市','yi xing shi');
_R3(0,9,8,'320297','','其它区','qi ta qu');
_R3(0,9,8,'320214','','新吴区','xin wu qu');
_R3(0,9,8,'320213','','梁溪区','liang xi qu');
_R3(0,9,8,'320211','','滨湖区','bin hu qu');
_R3(0,9,8,'320205','','锡山区','xi shan qu');
_R3(0,9,8,'320206','','惠山区','hui shan qu');
_R2(0,9,'320300','','徐州市','xu zhou shi');
_R3(0,9,9,'320382','','邳州市','pi zhou shi');
_R3(0,9,9,'320383','','其它区','qi ta qu');
_R3(0,9,9,'320381','','新沂市','xin yi shi');
_R3(0,9,9,'320311','','泉山区','quan shan qu');
_R3(0,9,9,'320324','','睢宁县','sui ning xian');
_R3(0,9,9,'320322','','沛县','pei xian');
_R3(0,9,9,'320323','','铜山区','tong shan qu');
_R3(0,9,9,'320321','','丰县','feng xian');
_R3(0,9,9,'320305','','贾汪区','jia wang qu');
_R3(0,9,9,'320302','','鼓楼区','gu lou qu');
_R3(0,9,9,'320303','','云龙区','yun long qu');
_R2(0,9,'320900','','盐城市','yan cheng shi');
_R3(0,9,10,'320982','','大丰区','da feng qu');
_R3(0,9,10,'320983','','其它区','qi ta qu');
_R3(0,9,10,'320981','','东台市','dong tai shi');
_R3(0,9,10,'320924','','射阳县','she yang xian');
_R3(0,9,10,'320925','','建湖县','jian hu xian');
_R3(0,9,10,'320922','','滨海县','bin hai xian');
_R3(0,9,10,'320923','','阜宁县','fu ning xian');
_R3(0,9,10,'320921','','响水县','xiang shui xian');
_R3(0,9,10,'320902','','亭湖区','ting hu qu');
_R3(0,9,10,'320903','','盐都区','yan du qu');
_R2(0,9,'321000','','扬州市','yang zhou shi');
_R3(0,9,11,'321002','','广陵区','guang ling qu');
_R3(0,9,11,'321003','','邗江区','han jiang qu');
_R3(0,9,11,'321023','','宝应县','bao ying xian');
_R3(0,9,11,'321093','','其它区','qi ta qu');
_R3(0,9,11,'321088','','江都区','jiang du qu');
_R3(0,9,11,'321084','','高邮市','gao you shi');
_R3(0,9,11,'321081','','仪征市','yi zheng shi');
_R2(0,9,'321100','','镇江市','zhen jiang shi');
_R3(0,9,12,'321183','','句容市','ju rong shi');
_R3(0,9,12,'321184','','其它区','qi ta qu');
_R3(0,9,12,'321181','','丹阳市','dan yang shi');
_R3(0,9,12,'321182','','扬中市','yang zhong shi');
_R3(0,9,12,'321111','','润州区','run zhou qu');
_R3(0,9,12,'321102','','京口区','jing kou qu');
_R3(0,9,12,'321112','','丹徒区','dan tu qu');

_R1(0,'330000','','浙江省','zhe jiang sheng','浙江省');
_R2(0,10,'330100','','杭州市','hang zhou shi');
_R3(0,10,0,'330183','','富阳区','fu yang qu');
_R3(0,10,0,'330185','','临安区','lin an qu');
_R3(0,10,0,'330186','','其它区','qi ta qu');
_R3(0,10,0,'330182','','建德市','jian de shi');
_R3(0,10,0,'330127','','淳安县','chun an xian');
_R3(0,10,0,'330122','','桐庐县','tong lu xian');
_R3(0,10,0,'330114','','临平区','lin ping qu');
_R3(0,10,0,'330110','','余杭区','yu hang qu');
_R3(0,10,0,'330113','','钱塘区','qian tang qu');
_R3(0,10,0,'330108','','滨江区','bin jiang qu');
_R3(0,10,0,'330109','','萧山区','xiao shan qu');
_R3(0,10,0,'330105','','拱墅区','gong shu qu');
_R3(0,10,0,'330106','','西湖区','xi hu qu');
_R3(0,10,0,'330102','','上城区','shang cheng qu');
_R2(0,10,'330500','','湖州市','hu zhou shi');
_R3(0,10,1,'330521','','德清县','de qing xian');
_R3(0,10,1,'330522','','长兴县','chang xing xian');
_R3(0,10,1,'330523','','安吉县','an ji xian');
_R3(0,10,1,'330524','','其它区','qi ta qu');
_R3(0,10,1,'330503','','南浔区','nan xun qu');
_R3(0,10,1,'330502','','吴兴区','wu xing qu');
_R2(0,10,'330400','','嘉兴市','jia xing shi');
_R3(0,10,2,'330424','','海盐县','hai yan xian');
_R3(0,10,2,'330421','','嘉善县','jia shan xian');
_R3(0,10,2,'330411','','秀洲区','xiu zhou qu');
_R3(0,10,2,'330402','','南湖区','nan hu qu');
_R3(0,10,2,'330484','','其它区','qi ta qu');
_R3(0,10,2,'330481','','海宁市','hai ning shi');
_R3(0,10,2,'330482','','平湖市','ping hu shi');
_R3(0,10,2,'330483','','桐乡市','tong xiang shi');
_R2(0,10,'330700','','金华市','jin hua shi');
_R3(0,10,3,'330785','','其它区','qi ta qu');
_R3(0,10,3,'330781','','兰溪市','lan xi shi');
_R3(0,10,3,'330782','','义乌市','yi wu shi');
_R3(0,10,3,'330783','','东阳市','dong yang shi');
_R3(0,10,3,'330784','','永康市','yong kang shi');
_R3(0,10,3,'330727','','磐安县','pan an xian');
_R3(0,10,3,'330723','','武义县','wu yi xian');
_R3(0,10,3,'330726','','浦江县','pu jiang xian');
_R3(0,10,3,'330702','','婺城区','wu cheng qu');
_R3(0,10,3,'330703','','金东区','jin dong qu');
_R2(0,10,'331100','','丽水市','li shui shi');
_R3(0,10,4,'331181','','龙泉市','long quan shi');
_R3(0,10,4,'331182','','其它区','qi ta qu');
_R3(0,10,4,'331126','','庆元县','qing yuan xian');
_R3(0,10,4,'331127','','景宁畲族自治县','jing ning she zu zi zhi xian');
_R3(0,10,4,'331122','','缙云县','jin yun xian');
_R3(0,10,4,'331123','','遂昌县','sui chang xian');
_R3(0,10,4,'331124','','松阳县','song yang xian');
_R3(0,10,4,'331125','','云和县','yun he xian');
_R3(0,10,4,'331121','','青田县','qing tian xian');
_R3(0,10,4,'331102','','莲都区','lian du qu');
_R2(0,10,'330200','','宁波市','ning bo shi');
_R3(0,10,5,'330206','','北仑区','bei lun qu');
_R3(0,10,5,'330203','','海曙区','hai shu qu');
_R3(0,10,5,'330205','','江北区','jiang bei qu');
_R3(0,10,5,'330282','','慈溪市','ci xi shi');
_R3(0,10,5,'330283','','奉化区','feng hua qu');
_R3(0,10,5,'330284','','其它区','qi ta qu');
_R3(0,10,5,'330281','','余姚市','yu yao shi');
_R3(0,10,5,'330225','','象山县','xiang shan xian');
_R3(0,10,5,'330226','','宁海县','ning hai xian');
_R3(0,10,5,'330211','','镇海区','zhen hai qu');
_R3(0,10,5,'330212','','鄞州区','yin zhou qu');
_R2(0,10,'330800','','衢州市','qu zhou shi');
_R3(0,10,6,'330881','','江山市','jiang shan shi');
_R3(0,10,6,'330882','','其它区','qi ta qu');
_R3(0,10,6,'330822','','常山县','chang shan xian');
_R3(0,10,6,'330824','','开化县','kai hua xian');
_R3(0,10,6,'330825','','龙游县','long you xian');
_R3(0,10,6,'330802','','柯城区','ke cheng qu');
_R3(0,10,6,'330803','','衢江区','qu jiang qu');
_R2(0,10,'330600','','绍兴市','shao xing shi');
_R3(0,10,7,'330682','','上虞区','shang yu qu');
_R3(0,10,7,'330683','','嵊州市','sheng zhou shi');
_R3(0,10,7,'330684','','其它区','qi ta qu');
_R3(0,10,7,'330681','','诸暨市','zhu ji shi');
_R3(0,10,7,'330624','','新昌县','xin chang xian');
_R3(0,10,7,'330621','','柯桥区','ke qiao qu');
_R3(0,10,7,'330602','','越城区','yue cheng qu');
_R2(0,10,'331000','','台州市','tai zhou shi');
_R3(0,10,8,'331081','','温岭市','wen ling shi');
_R3(0,10,8,'331082','','临海市','lin hai shi');
_R3(0,10,8,'331083','','其它区','qi ta qu');
_R3(0,10,8,'331023','','天台县','tian tai xian');
_R3(0,10,8,'331024','','仙居县','xian ju xian');
_R3(0,10,8,'331021','','玉环市','yu huan shi');
_R3(0,10,8,'331022','','三门县','san men xian');
_R3(0,10,8,'331002','','椒江区','jiao jiang qu');
_R3(0,10,8,'331003','','黄岩区','huang yan qu');
_R3(0,10,8,'331004','','路桥区','lu qiao qu');
_R2(0,10,'330300','','温州市','wen zhou shi');
_R3(0,10,9,'330327','','苍南县','cang nan xian');
_R3(0,10,9,'330328','','文成县','wen cheng xian');
_R3(0,10,9,'330329','','泰顺县','tai shun xian');
_R3(0,10,9,'330324','','永嘉县','yong jia xian');
_R3(0,10,9,'330326','','平阳县','ping yang xian');
_R3(0,10,9,'330322','','洞头区','dong tou qu');
_R3(0,10,9,'330302','','鹿城区','lu cheng qu');
_R3(0,10,9,'330303','','龙湾区','long wan qu');
_R3(0,10,9,'330304','','瓯海区','ou hai qu');
_R3(0,10,9,'330399','','龙港市','long gang shi');
_R3(0,10,9,'330381','','瑞安市','rui an shi');
_R3(0,10,9,'330382','','乐清市','yue qing shi');
_R3(0,10,9,'330383','','其它区','qi ta qu');
_R2(0,10,'330900','','舟山市','zhou shan shi');
_R3(0,10,10,'330921','','岱山县','dai shan xian');
_R3(0,10,10,'330922','','嵊泗县','sheng si xian');
_R3(0,10,10,'330923','','其它区','qi ta qu');
_R3(0,10,10,'330903','','普陀区','pu tuo qu');
_R3(0,10,10,'330902','','定海区','ding hai qu');

_R1(0,'340000','','安徽省','an hui sheng','安徽省');
_R2(0,11,'340800','','安庆市','an qing shi');
_R3(0,11,0,'340881','','桐城市','tong cheng shi');
_R3(0,11,0,'340882','','其它区','qi ta qu');
_R3(0,11,0,'340824','','潜山市','qian shan shi');
_R3(0,11,0,'340822','','怀宁县','huai ning xian');
_R3(0,11,0,'340827','','望江县','wang jiang xian');
_R3(0,11,0,'340828','','岳西县','yue xi xian');
_R3(0,11,0,'340825','','太湖县','tai hu xian');
_R3(0,11,0,'340826','','宿松县','su song xian');
_R3(0,11,0,'340811','','宜秀区','yi xiu qu');
_R3(0,11,0,'340802','','迎江区','ying jiang qu');
_R3(0,11,0,'340803','','大观区','da guan qu');
_R2(0,11,'340300','','蚌埠市','beng bu shi');
_R3(0,11,1,'340321','','怀远县','huai yuan xian');
_R3(0,11,1,'340324','','其它区','qi ta qu');
_R3(0,11,1,'340322','','五河县','wu he xian');
_R3(0,11,1,'340323','','固镇县','gu zhen xian');
_R3(0,11,1,'340311','','淮上区','huai shang qu');
_R3(0,11,1,'340304','','禹会区','yu hui qu');
_R3(0,11,1,'340302','','龙子湖区','long zi hu qu');
_R3(0,11,1,'340303','','蚌山区','beng shan qu');
_R2(0,11,'341600','','亳州市','bo zhou shi');
_R3(0,11,2,'341624','','其它区','qi ta qu');
_R3(0,11,2,'341623','','利辛县','li xin xian');
_R3(0,11,2,'341622','','蒙城县','meng cheng xian');
_R3(0,11,2,'341621','','涡阳县','guo yang xian');
_R3(0,11,2,'341602','','谯城区','qiao cheng qu');
_R2(0,11,'341700','','池州市','chi zhou shi');
_R3(0,11,3,'341724','','其它区','qi ta qu');
_R3(0,11,3,'341723','','青阳县','qing yang xian');
_R3(0,11,3,'341722','','石台县','shi tai xian');
_R3(0,11,3,'341721','','东至县','dong zhi xian');
_R3(0,11,3,'341702','','贵池区','gui chi qu');
_R2(0,11,'341100','','滁州市','chu zhou shi');
_R3(0,11,4,'341181','','天长市','tian chang shi');
_R3(0,11,4,'341182','','明光市','ming guang shi');
_R3(0,11,4,'341183','','其它区','qi ta qu');
_R3(0,11,4,'341124','','全椒县','quan jiao xian');
_R3(0,11,4,'341122','','来安县','lai an xian');
_R3(0,11,4,'341125','','定远县','ding yuan xian');
_R3(0,11,4,'341126','','凤阳县','feng yang xian');
_R3(0,11,4,'341102','','琅琊区','lang ya qu');
_R3(0,11,4,'341103','','南谯区','nan qiao qu');
_R2(0,11,'341200','','阜阳市','fu yang shi');
_R3(0,11,5,'341204','','颍泉区','ying quan qu');
_R3(0,11,5,'341202','','颍州区','ying zhou qu');
_R3(0,11,5,'341203','','颍东区','ying dong qu');
_R3(0,11,5,'341282','','界首市','jie shou shi');
_R3(0,11,5,'341283','','其它区','qi ta qu');
_R3(0,11,5,'341222','','太和县','tai he xian');
_R3(0,11,5,'341221','','临泉县','lin quan xian');
_R3(0,11,5,'341226','','颍上县','ying shang xian');
_R3(0,11,5,'341225','','阜南县','fu nan xian');
_R2(0,11,'340100','','合肥市','he fei shi');
_R3(0,11,6,'340122','','肥东县','fei dong xian');
_R3(0,11,6,'340123','','肥西县','fei xi xian');
_R3(0,11,6,'340121','','长丰县','chang feng xian');
_R3(0,11,6,'340111','','包河区','bao he qu');
_R3(0,11,6,'340104','','蜀山区','shu shan qu');
_R3(0,11,6,'340102','','瑶海区','yao hai qu');
_R3(0,11,6,'340103','','庐阳区','lu yang qu');
_R3(0,11,6,'341421','','庐江县','lu jiang xian');
_R3(0,11,6,'341400','','巢湖市','chao hu shi');
_R3(0,11,6,'340192','','其它区','qi ta qu');
_R2(0,11,'340600','','淮北市','huai bei shi');
_R3(0,11,7,'340621','','濉溪县','sui xi xian');
_R3(0,11,7,'340622','','其它区','qi ta qu');
_R3(0,11,7,'340603','','相山区','xiang shan qu');
_R3(0,11,7,'340604','','烈山区','lie shan qu');
_R3(0,11,7,'340602','','杜集区','du ji qu');
_R2(0,11,'340400','','淮南市','huai nan shi');
_R3(0,11,8,'340421','','凤台县','feng tai xian');
_R3(0,11,8,'340422','','其它区','qi ta qu');
_R3(0,11,8,'340405','','八公山区','ba gong shan qu');
_R3(0,11,8,'340406','','潘集区','pan ji qu');
_R3(0,11,8,'340403','','田家庵区','tian jia an qu');
_R3(0,11,8,'340404','','谢家集区','xie jia ji qu');
_R3(0,11,8,'340402','','大通区','da tong qu');
_R3(0,11,8,'340499','','寿县','shou xian');
_R2(0,11,'341000','','黄山市','huang shan shi');
_R3(0,11,9,'341024','','祁门县','qi men xian');
_R3(0,11,9,'341025','','其它区','qi ta qu');
_R3(0,11,9,'341022','','休宁县','xiu ning xian');
_R3(0,11,9,'341023','','黟县','yi xian');
_R3(0,11,9,'341021','','歙县','she xian');
_R3(0,11,9,'341002','','屯溪区','tun xi qu');
_R3(0,11,9,'341003','','黄山区','huang shan qu');
_R3(0,11,9,'341004','','徽州区','hui zhou qu');
_R2(0,11,'341500','','六安市','lu an shi');
_R3(0,11,10,'341526','','其它区','qi ta qu');
_R3(0,11,10,'341525','','霍山县','huo shan xian');
_R3(0,11,10,'341524','','金寨县','jin zhai xian');
_R3(0,11,10,'341523','','舒城县','shu cheng xian');
_R3(0,11,10,'341522','','霍邱县','huo qiu xian');
_R3(0,11,10,'341504','','叶集区','ye ji qu');
_R3(0,11,10,'341503','','裕安区','yu an qu');
_R3(0,11,10,'341502','','金安区','jin an qu');
_R2(0,11,'340500','','马鞍山市','ma an shan shi');
_R3(0,11,11,'341424','','和县','he xian');
_R3(0,11,11,'341423','','含山县','han shan xian');
_R3(0,11,11,'340522','','其它区','qi ta qu');
_R3(0,11,11,'340521','','当涂县','dang tu xian');
_R3(0,11,11,'340504','','雨山区','yu shan qu');
_R3(0,11,11,'340503','','花山区','hua shan qu');
_R3(0,11,11,'340506','','博望区','bo wang qu');
_R2(0,11,'341300','','宿州市','su zhou shi');
_R3(0,11,12,'341321','','砀山县','dang shan xian');
_R3(0,11,12,'341322','','萧县','xiao xian');
_R3(0,11,12,'341325','','其它区','qi ta qu');
_R3(0,11,12,'341323','','灵璧县','ling bi xian');
_R3(0,11,12,'341324','','泗县','si xian');
_R3(0,11,12,'341302','','埇桥区','yong qiao qu');
_R2(0,11,'340700','','铜陵市','tong ling shi');
_R3(0,11,13,'340799','','枞阳县','zong yang xian');
_R3(0,11,13,'340722','','其它区','qi ta qu');
_R3(0,11,13,'340721','','义安区','yi an qu');
_R3(0,11,13,'340711','','郊区','jiao qu');
_R3(0,11,13,'340705','','铜官区','tong guan qu');
_R2(0,11,'340200','','芜湖市','wu hu shi');
_R3(0,11,14,'341422','','无为市','wu wei shi');
_R3(0,11,14,'340221','','湾沚区','wan zhi qu');
_R3(0,11,14,'340222','','繁昌区','fan chang qu');
_R3(0,11,14,'340223','','南陵县','nan ling xian');
_R3(0,11,14,'340224','','其它区','qi ta qu');
_R3(0,11,14,'340207','','鸠江区','jiu jiang qu');
_R3(0,11,14,'340203','','弋江区','yi jiang qu');
_R3(0,11,14,'340202','','镜湖区','jing hu qu');
_R2(0,11,'341800','','宣城市','xuan cheng shi');
_R3(0,11,15,'341882','','其它区','qi ta qu');
_R3(0,11,15,'341881','','宁国市','ning guo shi');
_R3(0,11,15,'341825','','旌德县','jing de xian');
_R3(0,11,15,'341824','','绩溪县','ji xi xian');
_R3(0,11,15,'341823','','泾县','jing xian');
_R3(0,11,15,'341822','','广德市','guang de shi');
_R3(0,11,15,'341821','','郎溪县','lang xi xian');
_R3(0,11,15,'341802','','宣州区','xuan zhou qu');

_R1(0,'350000','','福建省','fu jian sheng','福建省');
_R2(0,12,'350100','','福州市','fu zhou shi');
_R3(0,12,0,'350181','','福清市','fu qing shi');
_R3(0,12,0,'350182','','长乐区','zhang le qu');
_R3(0,12,0,'350183','','其它区','qi ta qu');
_R3(0,12,0,'350105','','马尾区','ma wei qu');
_R3(0,12,0,'350102','','鼓楼区','gu lou qu');
_R3(0,12,0,'350103','','台江区','tai jiang qu');
_R3(0,12,0,'350104','','仓山区','cang shan qu');
_R3(0,12,0,'350111','','晋安区','jin an qu');
_R3(0,12,0,'350128','','平潭县','ping tan xian');
_R3(0,12,0,'350121','','闽侯县','min hou xian');
_R3(0,12,0,'350122','','连江县','lian jiang xian');
_R3(0,12,0,'350123','','罗源县','luo yuan xian');
_R3(0,12,0,'350124','','闽清县','min qing xian');
_R3(0,12,0,'350125','','永泰县','yong tai xian');
_R2(0,12,'350800','','龙岩市','long yan shi');
_R3(0,12,1,'350881','','漳平市','zhang ping shi');
_R3(0,12,1,'350882','','其它区','qi ta qu');
_R3(0,12,1,'350802','','新罗区','xin luo qu');
_R3(0,12,1,'350821','','长汀县','chang ting xian');
_R3(0,12,1,'350822','','永定区','yong ding qu');
_R3(0,12,1,'350823','','上杭县','shang hang xian');
_R3(0,12,1,'350824','','武平县','wu ping xian');
_R3(0,12,1,'350825','','连城县','lian cheng xian');
_R2(0,12,'350700','','南平市','nan ping shi');
_R3(0,12,2,'350702','','延平区','yan ping qu');
_R3(0,12,2,'350721','','顺昌县','shun chang xian');
_R3(0,12,2,'350722','','浦城县','pu cheng xian');
_R3(0,12,2,'350723','','光泽县','guang ze xian');
_R3(0,12,2,'350724','','松溪县','song xi xian');
_R3(0,12,2,'350725','','政和县','zheng he xian');
_R3(0,12,2,'350781','','邵武市','shao wu shi');
_R3(0,12,2,'350782','','武夷山市','wu yi shan shi');
_R3(0,12,2,'350783','','建瓯市','jian ou shi');
_R3(0,12,2,'350784','','建阳区','jian yang qu');
_R3(0,12,2,'350785','','其它区','qi ta qu');
_R2(0,12,'350900','','宁德市','ning de shi');
_R3(0,12,3,'350902','','蕉城区','jiao cheng qu');
_R3(0,12,3,'350921','','霞浦县','xia pu xian');
_R3(0,12,3,'350922','','古田县','gu tian xian');
_R3(0,12,3,'350923','','屏南县','ping nan xian');
_R3(0,12,3,'350924','','寿宁县','shou ning xian');
_R3(0,12,3,'350925','','周宁县','zhou ning xian');
_R3(0,12,3,'350926','','柘荣县','zhe rong xian');
_R3(0,12,3,'350981','','福安市','fu an shi');
_R3(0,12,3,'350982','','福鼎市','fu ding shi');
_R3(0,12,3,'350983','','其它区','qi ta qu');
_R2(0,12,'350300','','莆田市','pu tian shi');
_R3(0,12,4,'350303','','涵江区','han jiang qu');
_R3(0,12,4,'350304','','荔城区','li cheng qu');
_R3(0,12,4,'350305','','秀屿区','xiu yu qu');
_R3(0,12,4,'350302','','城厢区','cheng xiang qu');
_R3(0,12,4,'350322','','仙游县','xian you xian');
_R3(0,12,4,'350323','','其它区','qi ta qu');
_R2(0,12,'350500','','泉州市','quan zhou shi');
_R3(0,12,5,'350502','','鲤城区','li cheng qu');
_R3(0,12,5,'350503','','丰泽区','feng ze qu');
_R3(0,12,5,'350504','','洛江区','luo jiang qu');
_R3(0,12,5,'350505','','泉港区','quan gang qu');
_R3(0,12,5,'350524','','安溪县','an xi xian');
_R3(0,12,5,'350525','','永春县','yong chun xian');
_R3(0,12,5,'350526','','德化县','de hua xian');
_R3(0,12,5,'350527','','金门县','jin men xian');
_R3(0,12,5,'350521','','惠安县','hui an xian');
_R3(0,12,5,'350581','','石狮市','shi shi shi');
_R3(0,12,5,'350582','','晋江市','jin jiang shi');
_R3(0,12,5,'350583','','南安市','nan an shi');
_R3(0,12,5,'350584','','其它区','qi ta qu');
_R2(0,12,'350400','','三明市','san ming shi');
_R3(0,12,6,'350403','','三元区','san yuan qu');
_R3(0,12,6,'350424','','宁化县','ning hua xian');
_R3(0,12,6,'350425','','大田县','da tian xian');
_R3(0,12,6,'350426','','尤溪县','you xi xian');
_R3(0,12,6,'350427','','沙县区','sha xian qu');
_R3(0,12,6,'350428','','将乐县','jiang le xian');
_R3(0,12,6,'350429','','泰宁县','tai ning xian');
_R3(0,12,6,'350421','','明溪县','ming xi xian');
_R3(0,12,6,'350423','','清流县','qing liu xian');
_R3(0,12,6,'350430','','建宁县','jian ning xian');
_R3(0,12,6,'350481','','永安市','yong an shi');
_R3(0,12,6,'350482','','其它区','qi ta qu');
_R2(0,12,'350200','','厦门市','xia men shi');
_R3(0,12,7,'350205','','海沧区','hai cang qu');
_R3(0,12,7,'350206','','湖里区','hu li qu');
_R3(0,12,7,'350203','','思明区','si ming qu');
_R3(0,12,7,'350211','','集美区','ji mei qu');
_R3(0,12,7,'350212','','同安区','tong an qu');
_R3(0,12,7,'350213','','翔安区','xiang an qu');
_R3(0,12,7,'350214','','其它区','qi ta qu');
_R2(0,12,'350600','','漳州市','zhang zhou shi');
_R3(0,12,8,'350622','','云霄县','yun xiao xian');
_R3(0,12,8,'350623','','漳浦县','zhang pu xian');
_R3(0,12,8,'350624','','诏安县','zhao an xian');
_R3(0,12,8,'350625','','长泰区','chang tai qu');
_R3(0,12,8,'350626','','东山县','dong shan xian');
_R3(0,12,8,'350627','','南靖县','nan jing xian');
_R3(0,12,8,'350628','','平和县','ping he xian');
_R3(0,12,8,'350629','','华安县','hua an xian');
_R3(0,12,8,'350681','','龙海区','long hai qu');
_R3(0,12,8,'350682','','其它区','qi ta qu');
_R3(0,12,8,'350602','','芗城区','xiang cheng qu');
_R3(0,12,8,'350603','','龙文区','long wen qu');

_R1(0,'360000','','江西省','jiang xi sheng','江西省');
_R2(0,13,'361000','','抚州市','fu zhou shi');
_R3(0,13,0,'361002','','临川区','lin chuan qu');
_R3(0,13,0,'361030','','广昌县','guang chang xian');
_R3(0,13,0,'361027','','金溪县','jin xi xian');
_R3(0,13,0,'361026','','宜黄县','yi huang xian');
_R3(0,13,0,'361025','','乐安县','le an xian');
_R3(0,13,0,'361024','','崇仁县','chong ren xian');
_R3(0,13,0,'361023','','南丰县','nan feng xian');
_R3(0,13,0,'361022','','黎川县','li chuan xian');
_R3(0,13,0,'361021','','南城县','nan cheng xian');
_R3(0,13,0,'361029','','东乡区','dong xiang qu');
_R3(0,13,0,'361028','','资溪县','zi xi xian');
_R3(0,13,0,'361031','','其它区','qi ta qu');
_R2(0,13,'360700','','赣州市','gan zhou shi');
_R3(0,13,1,'360702','','章贡区','zhang gong qu');
_R3(0,13,1,'360728','','定南县','ding nan xian');
_R3(0,13,1,'360729','','全南县','quan nan xian');
_R3(0,13,1,'360726','','安远县','an yuan xian');
_R3(0,13,1,'360727','','龙南市','long nan shi');
_R3(0,13,1,'360724','','上犹县','shang you xian');
_R3(0,13,1,'360725','','崇义县','chong yi xian');
_R3(0,13,1,'360722','','信丰县','xin feng xian');
_R3(0,13,1,'360723','','大余县','da yu xian');
_R3(0,13,1,'360721','','赣县区','gan xian qu');
_R3(0,13,1,'360730','','宁都县','ning du xian');
_R3(0,13,1,'360735','','石城县','shi cheng xian');
_R3(0,13,1,'360733','','会昌县','hui chang xian');
_R3(0,13,1,'360734','','寻乌县','xun wu xian');
_R3(0,13,1,'360731','','于都县','yu du xian');
_R3(0,13,1,'360732','','兴国县','xing guo xian');
_R3(0,13,1,'360782','','南康区','nan kang qu');
_R3(0,13,1,'360783','','其它区','qi ta qu');
_R3(0,13,1,'360781','','瑞金市','rui jin shi');
_R2(0,13,'360800','','吉安市','ji an shi');
_R3(0,13,2,'360803','','青原区','qing yuan qu');
_R3(0,13,2,'360802','','吉州区','ji zhou qu');
_R3(0,13,2,'360829','','安福县','an fu xian');
_R3(0,13,2,'360827','','遂川县','sui chuan xian');
_R3(0,13,2,'360828','','万安县','wan an xian');
_R3(0,13,2,'360825','','永丰县','yong feng xian');
_R3(0,13,2,'360826','','泰和县','tai he xian');
_R3(0,13,2,'360823','','峡江县','xia jiang xian');
_R3(0,13,2,'360824','','新干县','xin gan xian');
_R3(0,13,2,'360821','','吉安县','ji an xian');
_R3(0,13,2,'360822','','吉水县','ji shui xian');
_R3(0,13,2,'360830','','永新县','yong xin xian');
_R3(0,13,2,'360881','','井冈山市','jing gang shan shi');
_R3(0,13,2,'360882','','其它区','qi ta qu');
_R2(0,13,'360200','','景德镇市','jing de zhen shi');
_R3(0,13,3,'360202','','昌江区','chang jiang qu');
_R3(0,13,3,'360203','','珠山区','zhu shan qu');
_R3(0,13,3,'360222','','浮梁县','fu liang xian');
_R3(0,13,3,'360281','','乐平市','le ping shi');
_R3(0,13,3,'360282','','其它区','qi ta qu');
_R2(0,13,'360400','','九江市','jiu jiang shi');
_R3(0,13,4,'360421','','柴桑区','chai sang qu');
_R3(0,13,4,'360429','','湖口县','hu kou xian');
_R3(0,13,4,'360427','','庐山市','lu shan shi');
_R3(0,13,4,'360428','','都昌县','du chang xian');
_R3(0,13,4,'360425','','永修县','yong xiu xian');
_R3(0,13,4,'360426','','德安县','de an xian');
_R3(0,13,4,'360423','','武宁县','wu ning xian');
_R3(0,13,4,'360424','','修水县','xiu shui xian');
_R3(0,13,4,'360430','','彭泽县','peng ze xian');
_R3(0,13,4,'360483','','共青城市','gong qing cheng shi');
_R3(0,13,4,'360481','','瑞昌市','rui chang shi');
_R3(0,13,4,'360482','','其它区','qi ta qu');
_R3(0,13,4,'360403','','浔阳区','xun yang qu');
_R3(0,13,4,'360402','','濂溪区','lian xi qu');
_R2(0,13,'360100','','南昌市','nan chang shi');
_R3(0,13,5,'360102','','东湖区','dong hu qu');
_R3(0,13,5,'360103','','西湖区','xi hu qu');
_R3(0,13,5,'360104','','青云谱区','qing yun pu qu');
_R3(0,13,5,'360111','','青山湖区','qing shan hu qu');
_R3(0,13,5,'360124','','进贤县','jin xian xian');
_R3(0,13,5,'360125','','红谷滩区','hong gu tan qu');
_R3(0,13,5,'360122','','新建区','xin jian qu');
_R3(0,13,5,'360123','','安义县','an yi xian');
_R3(0,13,5,'360121','','南昌县','nan chang xian');
_R3(0,13,5,'360128','','其它区','qi ta qu');
_R2(0,13,'360300','','萍乡市','ping xiang shi');
_R3(0,13,6,'360302','','安源区','an yuan qu');
_R3(0,13,6,'360313','','湘东区','xiang dong qu');
_R3(0,13,6,'360322','','上栗县','shang li xian');
_R3(0,13,6,'360323','','芦溪县','lu xi xian');
_R3(0,13,6,'360321','','莲花县','lian hua xian');
_R3(0,13,6,'360324','','其它区','qi ta qu');
_R2(0,13,'361100','','上饶市','shang rao shi');
_R3(0,13,7,'361102','','信州区','xin zhou qu');
_R3(0,13,7,'361126','','弋阳县','yi yang xian');
_R3(0,13,7,'361125','','横峰县','heng feng xian');
_R3(0,13,7,'361124','','铅山县','yan shan xian');
_R3(0,13,7,'361123','','玉山县','yu shan xian');
_R3(0,13,7,'361122','','广丰区','guang feng qu');
_R3(0,13,7,'361121','','广信区','guang xin qu');
_R3(0,13,7,'361129','','万年县','wan nian xian');
_R3(0,13,7,'361128','','鄱阳县','po yang xian');
_R3(0,13,7,'361127','','余干县','yu gan xian');
_R3(0,13,7,'361130','','婺源县','wu yuan xian');
_R3(0,13,7,'361182','','其它区','qi ta qu');
_R3(0,13,7,'361181','','德兴市','de xing shi');
_R2(0,13,'360500','','新余市','xin yu shi');
_R3(0,13,8,'360502','','渝水区','yu shui qu');
_R3(0,13,8,'360521','','分宜县','fen yi xian');
_R3(0,13,8,'360522','','其它区','qi ta qu');
_R2(0,13,'360900','','宜春市','yi chun shi');
_R3(0,13,9,'360902','','袁州区','yuan zhou qu');
_R3(0,13,9,'360926','','铜鼓县','tong gu xian');
_R3(0,13,9,'360924','','宜丰县','yi feng xian');
_R3(0,13,9,'360925','','靖安县','jing an xian');
_R3(0,13,9,'360922','','万载县','wan zai xian');
_R3(0,13,9,'360923','','上高县','shang gao xian');
_R3(0,13,9,'360921','','奉新县','feng xin xian');
_R3(0,13,9,'360982','','樟树市','zhang shu shi');
_R3(0,13,9,'360983','','高安市','gao an shi');
_R3(0,13,9,'360981','','丰城市','feng cheng shi');
_R3(0,13,9,'360984','','其它区','qi ta qu');
_R2(0,13,'360600','','鹰潭市','ying tan shi');
_R3(0,13,10,'360602','','月湖区','yue hu qu');
_R3(0,13,10,'360622','','余江区','yu jiang qu');
_R3(0,13,10,'360681','','贵溪市','gui xi shi');
_R3(0,13,10,'360682','','其它区','qi ta qu');

_R1(0,'370000','','山东省','shan dong sheng','山东省');
_R2(0,14,'371600','','滨州市','bin zhou shi');
_R3(0,14,0,'371602','','滨城区','bin cheng qu');
_R3(0,14,0,'371626','','邹平市','zou ping shi');
_R3(0,14,0,'371625','','博兴县','bo xing xian');
_R3(0,14,0,'371627','','其它区','qi ta qu');
_R3(0,14,0,'371622','','阳信县','yang xin xian');
_R3(0,14,0,'371621','','惠民县','hui min xian');
_R3(0,14,0,'371624','','沾化区','zhan hua qu');
_R3(0,14,0,'371623','','无棣县','wu di xian');
_R2(0,14,'371400','','德州市','de zhou shi');
_R3(0,14,1,'371402','','德城区','de cheng qu');
_R3(0,14,1,'371422','','宁津县','ning jin xian');
_R3(0,14,1,'371421','','陵城区','ling cheng qu');
_R3(0,14,1,'371428','','武城县','wu cheng xian');
_R3(0,14,1,'371427','','夏津县','xia jin xian');
_R3(0,14,1,'371424','','临邑县','lin yi xian');
_R3(0,14,1,'371423','','庆云县','qing yun xian');
_R3(0,14,1,'371426','','平原县','ping yuan xian');
_R3(0,14,1,'371425','','齐河县','qi he xian');
_R3(0,14,1,'371482','','禹城市','yu cheng shi');
_R3(0,14,1,'371481','','乐陵市','le ling shi');
_R3(0,14,1,'371483','','其它区','qi ta qu');
_R2(0,14,'370500','','东营市','dong ying shi');
_R3(0,14,2,'370503','','河口区','he kou qu');
_R3(0,14,2,'370502','','东营区','dong ying qu');
_R3(0,14,2,'370521','','垦利区','ken li qu');
_R3(0,14,2,'370522','','利津县','li jin xian');
_R3(0,14,2,'370523','','广饶县','guang rao xian');
_R3(0,14,2,'370591','','其它区','qi ta qu');
_R2(0,14,'371700','','菏泽市','he ze shi');
_R3(0,14,3,'371702','','牡丹区','mu dan qu');
_R3(0,14,3,'371725','','郓城县','yun cheng xian');
_R3(0,14,3,'371724','','巨野县','ju ye xian');
_R3(0,14,3,'371727','','定陶区','ding tao qu');
_R3(0,14,3,'371726','','鄄城县','juan cheng xian');
_R3(0,14,3,'371721','','曹县','cao xian');
_R3(0,14,3,'371723','','成武县','cheng wu xian');
_R3(0,14,3,'371722','','单县','shan xian');
_R3(0,14,3,'371729','','其它区','qi ta qu');
_R3(0,14,3,'371728','','东明县','dong ming xian');
_R2(0,14,'370100','','济南市','ji nan shi');
_R3(0,14,4,'371202','','莱芜区','lai wu qu');
_R3(0,14,4,'371203','','钢城区','gang cheng qu');
_R3(0,14,4,'370102','','历下区','li xia qu');
_R3(0,14,4,'370103','','市中区','shi zhong qu');
_R3(0,14,4,'370104','','槐荫区','huai yin qu');
_R3(0,14,4,'370105','','天桥区','tian qiao qu');
_R3(0,14,4,'370124','','平阴县','ping yin xian');
_R3(0,14,4,'370125','','济阳区','ji yang qu');
_R3(0,14,4,'370126','','商河县','shang he xian');
_R3(0,14,4,'370112','','历城区','li cheng qu');
_R3(0,14,4,'370113','','长清区','chang qing qu');
_R3(0,14,4,'370181','','章丘区','zhang qiu qu');
_R3(0,14,4,'370182','','其它区','qi ta qu');
_R2(0,14,'370800','','济宁市','ji ning shi');
_R3(0,14,5,'370826','','微山县','wei shan xian');
_R3(0,14,5,'370827','','鱼台县','yu tai xian');
_R3(0,14,5,'370828','','金乡县','jin xiang xian');
_R3(0,14,5,'370829','','嘉祥县','jia xiang xian');
_R3(0,14,5,'370811','','任城区','ren cheng qu');
_R3(0,14,5,'370830','','汶上县','wen shang xian');
_R3(0,14,5,'370831','','泗水县','si shui xian');
_R3(0,14,5,'370832','','梁山县','liang shan xian');
_R3(0,14,5,'370881','','曲阜市','qu fu shi');
_R3(0,14,5,'370882','','兖州区','yan zhou qu');
_R3(0,14,5,'370883','','邹城市','zou cheng shi');
_R3(0,14,5,'370884','','其它区','qi ta qu');
_R2(0,14,'371500','','聊城市','liao cheng shi');
_R3(0,14,6,'371581','','临清市','lin qing shi');
_R3(0,14,6,'371582','','其它区','qi ta qu');
_R3(0,14,6,'371502','','东昌府区','dong chang fu qu');
_R3(0,14,6,'371521','','阳谷县','yang gu xian');
_R3(0,14,6,'371526','','高唐县','gao tang xian');
_R3(0,14,6,'371523','','茌平区','chi ping qu');
_R3(0,14,6,'371522','','莘县','shen xian');
_R3(0,14,6,'371525','','冠县','guan xian');
_R3(0,14,6,'371524','','东阿县','dong e xian');
_R2(0,14,'371300','','临沂市','lin yi shi');
_R3(0,14,7,'371312','','河东区','he dong qu');
_R3(0,14,7,'371311','','罗庄区','luo zhuang qu');
_R3(0,14,7,'371302','','兰山区','lan shan qu');
_R3(0,14,7,'371330','','其它区','qi ta qu');
_R3(0,14,7,'371321','','沂南县','yi nan xian');
_R3(0,14,7,'371323','','沂水县','yi shui xian');
_R3(0,14,7,'371322','','郯城县','tan cheng xian');
_R3(0,14,7,'371329','','临沭县','lin shu xian');
_R3(0,14,7,'371328','','蒙阴县','meng yin xian');
_R3(0,14,7,'371325','','费县','fei xian');
_R3(0,14,7,'371324','','兰陵县','lan ling xian');
_R3(0,14,7,'371327','','莒南县','ju nan xian');
_R3(0,14,7,'371326','','平邑县','ping yi xian');
_R2(0,14,'370200','','青岛市','qing dao shi');
_R3(0,14,8,'370211','','黄岛区','huang dao qu');
_R3(0,14,8,'370212','','崂山区','lao shan qu');
_R3(0,14,8,'370213','','李沧区','li cang qu');
_R3(0,14,8,'370214','','城阳区','cheng yang qu');
_R3(0,14,8,'370286','','其它区','qi ta qu');
_R3(0,14,8,'370282','','即墨区','ji mo qu');
_R3(0,14,8,'370283','','平度市','ping du shi');
_R3(0,14,8,'370285','','莱西市','lai xi shi');
_R3(0,14,8,'370281','','胶州市','jiao zhou shi');
_R3(0,14,8,'370202','','市南区','shi nan qu');
_R3(0,14,8,'370203','','市北区','shi bei qu');
_R2(0,14,'371100','','日照市','ri zhao shi');
_R3(0,14,9,'371103','','岚山区','lan shan qu');
_R3(0,14,9,'371102','','东港区','dong gang qu');
_R3(0,14,9,'371123','','其它区','qi ta qu');
_R3(0,14,9,'371122','','莒县','ju xian');
_R3(0,14,9,'371121','','五莲县','wu lian xian');
_R2(0,14,'370900','','泰安市','tai an shi');
_R3(0,14,10,'370902','','泰山区','tai shan qu');
_R3(0,14,10,'370903','','岱岳区','dai yue qu');
_R3(0,14,10,'370921','','宁阳县','ning yang xian');
_R3(0,14,10,'370923','','东平县','dong ping xian');
_R3(0,14,10,'370982','','新泰市','xin tai shi');
_R3(0,14,10,'370983','','肥城市','fei cheng shi');
_R3(0,14,10,'370984','','其它区','qi ta qu');
_R2(0,14,'370700','','潍坊市','wei fang shi');
_R3(0,14,11,'370702','','潍城区','wei cheng qu');
_R3(0,14,11,'370703','','寒亭区','han ting qu');
_R3(0,14,11,'370704','','坊子区','fang zi qu');
_R3(0,14,11,'370705','','奎文区','kui wen qu');
_R3(0,14,11,'370724','','临朐县','lin qu xian');
_R3(0,14,11,'370725','','昌乐县','chang le xian');
_R3(0,14,11,'370781','','青州市','qing zhou shi');
_R3(0,14,11,'370782','','诸城市','zhu cheng shi');
_R3(0,14,11,'370783','','寿光市','shou guang shi');
_R3(0,14,11,'370784','','安丘市','an qiu shi');
_R3(0,14,11,'370785','','高密市','gao mi shi');
_R3(0,14,11,'370786','','昌邑市','chang yi shi');
_R3(0,14,11,'370787','','其它区','qi ta qu');
_R2(0,14,'371000','','威海市','wei hai shi');
_R3(0,14,12,'371002','','环翠区','huan cui qu');
_R3(0,14,12,'371082','','荣成市','rong cheng shi');
_R3(0,14,12,'371081','','文登区','wen deng qu');
_R3(0,14,12,'371084','','其它区','qi ta qu');
_R3(0,14,12,'371083','','乳山市','ru shan shi');
_R2(0,14,'370600','','烟台市','yan tai shi');
_R3(0,14,13,'370602','','芝罘区','zhi fu qu');
_R3(0,14,13,'370613','','莱山区','lai shan qu');
_R3(0,14,13,'370611','','福山区','fu shan qu');
_R3(0,14,13,'370612','','牟平区','mu ping qu');
_R3(0,14,13,'370682','','莱阳市','lai yang shi');
_R3(0,14,13,'370683','','莱州市','lai zhou shi');
_R3(0,14,13,'370684','','蓬莱区','peng lai qu');
_R3(0,14,13,'370685','','招远市','zhao yuan shi');
_R3(0,14,13,'370681','','龙口市','long kou shi');
_R3(0,14,13,'370686','','栖霞市','qi xia shi');
_R3(0,14,13,'370687','','海阳市','hai yang shi');
_R3(0,14,13,'370688','','其它区','qi ta qu');
_R2(0,14,'370400','','枣庄市','zao zhuang shi');
_R3(0,14,14,'370481','','滕州市','teng zhou shi');
_R3(0,14,14,'370482','','其它区','qi ta qu');
_R3(0,14,14,'370404','','峄城区','yi cheng qu');
_R3(0,14,14,'370405','','台儿庄区','tai er zhuang qu');
_R3(0,14,14,'370406','','山亭区','shan ting qu');
_R3(0,14,14,'370402','','市中区','shi zhong qu');
_R3(0,14,14,'370403','','薛城区','xue cheng qu');
_R2(0,14,'370300','','淄博市','zi bo shi');
_R3(0,14,15,'370305','','临淄区','lin zi qu');
_R3(0,14,15,'370306','','周村区','zhou cun qu');
_R3(0,14,15,'370302','','淄川区','zi chuan qu');
_R3(0,14,15,'370303','','张店区','zhang dian qu');
_R3(0,14,15,'370304','','博山区','bo shan qu');
_R3(0,14,15,'370321','','桓台县','huan tai xian');
_R3(0,14,15,'370322','','高青县','gao qing xian');
_R3(0,14,15,'370323','','沂源县','yi yuan xian');
_R3(0,14,15,'370324','','其它区','qi ta qu');

_R1(0,'410000','','河南省','he nan sheng','河南省');
_R2(0,15,'410500','','安阳市','an yang shi');
_R3(0,15,0,'410582','','其它区','qi ta qu');
_R3(0,15,0,'410581','','林州市','lin zhou shi');
_R3(0,15,0,'410527','','内黄县','nei huang xian');
_R3(0,15,0,'410526','','滑县','hua xian');
_R3(0,15,0,'410523','','汤阴县','tang yin xian');
_R3(0,15,0,'410522','','安阳县','an yang xian');
_R3(0,15,0,'410503','','北关区','bei guan qu');
_R3(0,15,0,'410502','','文峰区','wen feng qu');
_R3(0,15,0,'410505','','殷都区','yin du qu');
_R3(0,15,0,'410506','','龙安区','long an qu');
_R2(0,15,'410600','','鹤壁市','he bi shi');
_R3(0,15,1,'410602','','鹤山区','he shan qu');
_R3(0,15,1,'410603','','山城区','shan cheng qu');
_R3(0,15,1,'410611','','淇滨区','qi bin qu');
_R3(0,15,1,'410623','','其它区','qi ta qu');
_R3(0,15,1,'410622','','淇县','qi xian');
_R3(0,15,1,'410621','','浚县','xun xian');
_R2(0,15,'410881','','济源市','ji yuan shi');
_R2(0,15,'410800','','焦作市','jiao zuo shi');
_R3(0,15,3,'410884','','其它区','qi ta qu');
_R3(0,15,3,'410883','','孟州市','meng zhou shi');
_R3(0,15,3,'410882','','沁阳市','qin yang shi');
_R3(0,15,3,'410802','','解放区','jie fang qu');
_R3(0,15,3,'410804','','马村区','ma cun qu');
_R3(0,15,3,'410803','','中站区','zhong zhan qu');
_R3(0,15,3,'410811','','山阳区','shan yang qu');
_R3(0,15,3,'410822','','博爱县','bo ai xian');
_R3(0,15,3,'410821','','修武县','xiu wu xian');
_R3(0,15,3,'410823','','武陟县','wu zhi xian');
_R3(0,15,3,'410825','','温县','wen xian');
_R2(0,15,'410200','','开封市','kai feng shi');
_R3(0,15,4,'410205','','禹王台区','yu wang tai qu');
_R3(0,15,4,'410202','','龙亭区','long ting qu');
_R3(0,15,4,'410204','','鼓楼区','gu lou qu');
_R3(0,15,4,'410203','','顺河回族区','shun he hui zu qu');
_R3(0,15,4,'410222','','通许县','tong xu xian');
_R3(0,15,4,'410221','','杞县','qi xian');
_R3(0,15,4,'410224','','祥符区','xiang fu qu');
_R3(0,15,4,'410223','','尉氏县','wei shi xian');
_R3(0,15,4,'410226','','其它区','qi ta qu');
_R3(0,15,4,'410225','','兰考县','lan kao xian');
_R2(0,15,'411100','','漯河市','luo he shi');
_R3(0,15,5,'411102','','源汇区','yuan hui qu');
_R3(0,15,5,'411104','','召陵区','shao ling qu');
_R3(0,15,5,'411103','','郾城区','yan cheng qu');
_R3(0,15,5,'411122','','临颍县','lin ying xian');
_R3(0,15,5,'411121','','舞阳县','wu yang xian');
_R3(0,15,5,'411123','','其它区','qi ta qu');
_R2(0,15,'410300','','洛阳市','luo yang shi');
_R3(0,15,6,'410381','','偃师区','yan shi qu');
_R3(0,15,6,'410327','','宜阳县','yi yang xian');
_R3(0,15,6,'410326','','汝阳县','ru yang xian');
_R3(0,15,6,'410329','','伊川县','yi chuan xian');
_R3(0,15,6,'410328','','洛宁县','luo ning xian');
_R3(0,15,6,'410323','','新安县','xin an xian');
_R3(0,15,6,'410325','','嵩县','song xian');
_R3(0,15,6,'410324','','栾川县','luan chuan xian');
_R3(0,15,6,'410305','','涧西区','jian xi qu');
_R3(0,15,6,'410304','','瀍河回族区','chan he hui zu qu');
_R3(0,15,6,'410307','','洛龙区','luo long qu');
_R3(0,15,6,'410306','','孟津区','meng jin qu');
_R3(0,15,6,'410303','','西工区','xi gong qu');
_R3(0,15,6,'410302','','老城区','lao cheng qu');
_R3(0,15,6,'471005','','其它区','qi ta qu');
_R2(0,15,'411300','','南阳市','nan yang shi');
_R3(0,15,7,'411302','','宛城区','wan cheng qu');
_R3(0,15,7,'411303','','卧龙区','wo long qu');
_R3(0,15,7,'411322','','方城县','fang cheng xian');
_R3(0,15,7,'411321','','南召县','nan zhao xian');
_R3(0,15,7,'411328','','唐河县','tang he xian');
_R3(0,15,7,'411327','','社旗县','she qi xian');
_R3(0,15,7,'411329','','新野县','xin ye xian');
_R3(0,15,7,'411324','','镇平县','zhen ping xian');
_R3(0,15,7,'411323','','西峡县','xi xia xian');
_R3(0,15,7,'411326','','淅川县','xi chuan xian');
_R3(0,15,7,'411325','','内乡县','nei xiang xian');
_R3(0,15,7,'411330','','桐柏县','tong bai xian');
_R3(0,15,7,'411382','','其它区','qi ta qu');
_R3(0,15,7,'411381','','邓州市','deng zhou shi');
_R2(0,15,'410400','','平顶山市','ping ding shan shi');
_R3(0,15,8,'410483','','其它区','qi ta qu');
_R3(0,15,8,'410482','','汝州市','ru zhou shi');
_R3(0,15,8,'410481','','舞钢市','wu gang shi');
_R3(0,15,8,'410404','','石龙区','shi long qu');
_R3(0,15,8,'410403','','卫东区','wei dong qu');
_R3(0,15,8,'410402','','新华区','xin hua qu');
_R3(0,15,8,'410411','','湛河区','zhan he qu');
_R3(0,15,8,'410425','','郏县','jia xian');
_R3(0,15,8,'410422','','叶县','ye xian');
_R3(0,15,8,'410421','','宝丰县','bao feng xian');
_R3(0,15,8,'410423','','鲁山县','lu shan xian');
_R2(0,15,'410900','','濮阳市','pu yang shi');
_R3(0,15,9,'410923','','南乐县','nan le xian');
_R3(0,15,9,'410922','','清丰县','qing feng xian');
_R3(0,15,9,'410929','','其它区','qi ta qu');
_R3(0,15,9,'410928','','濮阳县','pu yang xian');
_R3(0,15,9,'410927','','台前县','tai qian xian');
_R3(0,15,9,'410926','','范县','fan xian');
_R3(0,15,9,'410902','','华龙区','hua long qu');
_R2(0,15,'411200','','三门峡市','san men xia shi');
_R3(0,15,10,'411281','','义马市','yi ma shi');
_R3(0,15,10,'411283','','其它区','qi ta qu');
_R3(0,15,10,'411282','','灵宝市','ling bao shi');
_R3(0,15,10,'411221','','渑池县','mian chi xian');
_R3(0,15,10,'411222','','陕州区','shan zhou qu');
_R3(0,15,10,'411224','','卢氏县','lu shi xian');
_R3(0,15,10,'411202','','湖滨区','hu bin qu');
_R2(0,15,'411400','','商丘市','shang qiu shi');
_R3(0,15,11,'411481','','永城市','yong cheng shi');
_R3(0,15,11,'411482','','其它区','qi ta qu');
_R3(0,15,11,'411421','','民权县','min quan xian');
_R3(0,15,11,'411426','','夏邑县','xia yi xian');
_R3(0,15,11,'411423','','宁陵县','ning ling xian');
_R3(0,15,11,'411422','','睢县','sui xian');
_R3(0,15,11,'411425','','虞城县','yu cheng xian');
_R3(0,15,11,'411424','','柘城县','zhe cheng xian');
_R3(0,15,11,'411403','','睢阳区','sui yang qu');
_R3(0,15,11,'411402','','梁园区','liang yuan qu');
_R2(0,15,'410700','','新乡市','xin xiang shi');
_R3(0,15,12,'410781','','卫辉市','wei hui shi');
_R3(0,15,12,'410783','','其它区','qi ta qu');
_R3(0,15,12,'410782','','辉县市','hui xian shi');
_R3(0,15,12,'410725','','原阳县','yuan yang xian');
_R3(0,15,12,'410724','','获嘉县','huo jia xian');
_R3(0,15,12,'410721','','新乡县','xin xiang xian');
_R3(0,15,12,'410727','','封丘县','feng qiu xian');
_R3(0,15,12,'410726','','延津县','yan jin xian');
_R3(0,15,12,'410728','','长垣市','chang yuan shi');
_R3(0,15,12,'410703','','卫滨区','wei bin qu');
_R3(0,15,12,'410702','','红旗区','hong qi qu');
_R3(0,15,12,'410704','','凤泉区','feng quan qu');
_R3(0,15,12,'410711','','牧野区','mu ye qu');
_R2(0,15,'411500','','信阳市','xin yang shi');
_R3(0,15,13,'411503','','平桥区','ping qiao qu');
_R3(0,15,13,'411502','','浉河区','shi he qu');
_R3(0,15,13,'411526','','潢川县','huang chuan xian');
_R3(0,15,13,'411525','','固始县','gu shi xian');
_R3(0,15,13,'411528','','息县','xi xian');
_R3(0,15,13,'411527','','淮滨县','huai bin xian');
_R3(0,15,13,'411522','','光山县','guang shan xian');
_R3(0,15,13,'411521','','罗山县','luo shan xian');
_R3(0,15,13,'411524','','商城县','shang cheng xian');
_R3(0,15,13,'411523','','新县','xin xian');
_R3(0,15,13,'411529','','其它区','qi ta qu');
_R2(0,15,'411000','','许昌市','xu chang shi');
_R3(0,15,14,'411081','','禹州市','yu zhou shi');
_R3(0,15,14,'411083','','其它区','qi ta qu');
_R3(0,15,14,'411082','','长葛市','chang ge shi');
_R3(0,15,14,'411023','','建安区','jian an qu');
_R3(0,15,14,'411025','','襄城县','xiang cheng xian');
_R3(0,15,14,'411024','','鄢陵县','yan ling xian');
_R3(0,15,14,'411002','','魏都区','wei du qu');
_R2(0,15,'410100','','郑州市','zheng zhou shi');
_R3(0,15,15,'410181','','巩义市','gong yi shi');
_R3(0,15,15,'410188','','其它区','qi ta qu');
_R3(0,15,15,'410183','','新密市','xin mi shi');
_R3(0,15,15,'410182','','荥阳市','xing yang shi');
_R3(0,15,15,'410185','','登封市','deng feng shi');
_R3(0,15,15,'410184','','新郑市','xin zheng shi');
_R3(0,15,15,'410106','','上街区','shang jie qu');
_R3(0,15,15,'410108','','惠济区','hui ji qu');
_R3(0,15,15,'410103','','二七区','er qi qu');
_R3(0,15,15,'410102','','中原区','zhong yuan qu');
_R3(0,15,15,'410105','','金水区','jin shui qu');
_R3(0,15,15,'410104','','管城回族区','guan cheng hui zu qu');
_R3(0,15,15,'410122','','中牟县','zhong mu xian');
_R2(0,15,'411600','','周口市','zhou kou shi');
_R3(0,15,16,'411682','','其它区','qi ta qu');
_R3(0,15,16,'411681','','项城市','xiang cheng shi');
_R3(0,15,16,'411625','','郸城县','dan cheng xian');
_R3(0,15,16,'411624','','沈丘县','shen qiu xian');
_R3(0,15,16,'411627','','太康县','tai kang xian');
_R3(0,15,16,'411626','','淮阳区','huai yang qu');
_R3(0,15,16,'411621','','扶沟县','fu gou xian');
_R3(0,15,16,'411623','','商水县','shang shui xian');
_R3(0,15,16,'411622','','西华县','xi hua xian');
_R3(0,15,16,'411628','','鹿邑县','lu yi xian');
_R3(0,15,16,'411602','','川汇区','chuan hui qu');
_R2(0,15,'411700','','驻马店市','zhu ma dian shi');
_R3(0,15,17,'411730','','其它区','qi ta qu');
_R3(0,15,17,'411702','','驿城区','yi cheng qu');
_R3(0,15,17,'411724','','正阳县','zheng yang xian');
_R3(0,15,17,'411723','','平舆县','ping yu xian');
_R3(0,15,17,'411726','','泌阳县','bi yang xian');
_R3(0,15,17,'411725','','确山县','que shan xian');
_R3(0,15,17,'411722','','上蔡县','shang cai xian');
_R3(0,15,17,'411721','','西平县','xi ping xian');
_R3(0,15,17,'411728','','遂平县','sui ping xian');
_R3(0,15,17,'411727','','汝南县','ru nan xian');
_R3(0,15,17,'411729','','新蔡县','xin cai xian');

_R1(0,'420000','','湖北省','hu bei sheng','湖北省');
_R2(0,16,'420700','','鄂州市','e zhou shi');
_R3(0,16,0,'420702','','梁子湖区','liang zi hu qu');
_R3(0,16,0,'420705','','其它区','qi ta qu');
_R3(0,16,0,'420704','','鄂城区','e cheng qu');
_R3(0,16,0,'420703','','华容区','hua rong qu');
_R2(0,16,'422800','','恩施土家族苗族自治州','en shi tu jia zu miao zu zi zhi zhou');
_R3(0,16,1,'422822','','建始县','jian shi xian');
_R3(0,16,1,'422823','','巴东县','ba dong xian');
_R3(0,16,1,'422825','','宣恩县','xuan en xian');
_R3(0,16,1,'422826','','咸丰县','xian feng xian');
_R3(0,16,1,'422827','','来凤县','lai feng xian');
_R3(0,16,1,'422828','','鹤峰县','he feng xian');
_R3(0,16,1,'422829','','其它区','qi ta qu');
_R3(0,16,1,'422801','','恩施市','en shi shi');
_R3(0,16,1,'422802','','利川市','li chuan shi');
_R2(0,16,'421100','','黄冈市','huang gang shi');
_R3(0,16,2,'421102','','黄州区','huang zhou qu');
_R3(0,16,2,'421123','','罗田县','luo tian xian');
_R3(0,16,2,'421122','','红安县','hong an xian');
_R3(0,16,2,'421121','','团风县','tuan feng xian');
_R3(0,16,2,'421127','','黄梅县','huang mei xian');
_R3(0,16,2,'421126','','蕲春县','qi chun xian');
_R3(0,16,2,'421125','','浠水县','xi shui xian');
_R3(0,16,2,'421124','','英山县','ying shan xian');
_R3(0,16,2,'421181','','麻城市','ma cheng shi');
_R3(0,16,2,'421183','','其它区','qi ta qu');
_R3(0,16,2,'421182','','武穴市','wu xue shi');
_R2(0,16,'420200','','黄石市','huang shi shi');
_R3(0,16,3,'420282','','其它区','qi ta qu');
_R3(0,16,3,'420281','','大冶市','da ye shi');
_R3(0,16,3,'420203','','西塞山区','xi sai shan qu');
_R3(0,16,3,'420202','','黄石港区','huang shi gang qu');
_R3(0,16,3,'420205','','铁山区','tie shan qu');
_R3(0,16,3,'420204','','下陆区','xia lu qu');
_R3(0,16,3,'420222','','阳新县','yang xin xian');
_R2(0,16,'420800','','荆门市','jing men shi');
_R3(0,16,4,'420882','','其它区','qi ta qu');
_R3(0,16,4,'420881','','钟祥市','zhong xiang shi');
_R3(0,16,4,'420804','','掇刀区','duo dao qu');
_R3(0,16,4,'420802','','东宝区','dong bao qu');
_R3(0,16,4,'420822','','沙洋县','sha yang xian');
_R3(0,16,4,'420821','','京山市','jing shan shi');
_R2(0,16,'421000','','荆州市','jing zhou shi');
_R3(0,16,5,'421081','','石首市','shi shou shi');
_R3(0,16,5,'421083','','洪湖市','hong hu shi');
_R3(0,16,5,'421088','','其它区','qi ta qu');
_R3(0,16,5,'421087','','松滋市','song zi shi');
_R3(0,16,5,'421024','','江陵县','jiang ling xian');
_R3(0,16,5,'421023','','监利市','jian li shi');
_R3(0,16,5,'421022','','公安县','gong an xian');
_R3(0,16,5,'421002','','沙市区','sha shi qu');
_R3(0,16,5,'421003','','荆州区','jing zhou qu');
_R2(0,16,'429005','','潜江市','qian jiang shi');
_R2(0,16,'429021','','神农架林区','shen nong jia lin qu');
_R2(0,16,'420300','','十堰市','shi yan shi');
_R3(0,16,8,'420381','','丹江口市','dan jiang kou shi');
_R3(0,16,8,'420383','','其它区','qi ta qu');
_R3(0,16,8,'420324','','竹溪县','zhu xi xian');
_R3(0,16,8,'420323','','竹山县','zhu shan xian');
_R3(0,16,8,'420322','','郧西县','yun xi xian');
_R3(0,16,8,'420321','','郧阳区','yun yang qu');
_R3(0,16,8,'420325','','房县','fang xian');
_R3(0,16,8,'420302','','茅箭区','mao jian qu');
_R3(0,16,8,'420303','','张湾区','zhang wan qu');
_R2(0,16,'421300','','随州市','sui zhou shi');
_R3(0,16,9,'421302','','曾都区','zeng du qu');
_R3(0,16,9,'421321','','随县','sui xian');
_R3(0,16,9,'421382','','其它区','qi ta qu');
_R3(0,16,9,'421381','','广水市','guang shui shi');
_R2(0,16,'429006','','天门市','tian men shi');
_R2(0,16,'420100','','武汉市','wu han shi');
_R3(0,16,11,'420111','','洪山区','hong shan qu');
_R3(0,16,11,'420104','','硚口区','qiao kou qu');
_R3(0,16,11,'420103','','江汉区','jiang han qu');
_R3(0,16,11,'420102','','江岸区','jiang an qu');
_R3(0,16,11,'420107','','青山区','qing shan qu');
_R3(0,16,11,'420106','','武昌区','wu chang qu');
_R3(0,16,11,'420105','','汉阳区','han yang qu');
_R3(0,16,11,'420115','','江夏区','jiang xia qu');
_R3(0,16,11,'420114','','蔡甸区','cai dian qu');
_R3(0,16,11,'420113','','汉南区','han nan qu');
_R3(0,16,11,'420112','','东西湖区','dong xi hu qu');
_R3(0,16,11,'420118','','其它区','qi ta qu');
_R3(0,16,11,'420117','','新洲区','xin zhou qu');
_R3(0,16,11,'420116','','黄陂区','huang pi qu');
_R2(0,16,'421200','','咸宁市','xian ning shi');
_R3(0,16,12,'421222','','通城县','tong cheng xian');
_R3(0,16,12,'421221','','嘉鱼县','jia yu xian');
_R3(0,16,12,'421224','','通山县','tong shan xian');
_R3(0,16,12,'421223','','崇阳县','chong yang xian');
_R3(0,16,12,'421202','','咸安区','xian an qu');
_R3(0,16,12,'421283','','其它区','qi ta qu');
_R3(0,16,12,'421281','','赤壁市','chi bi shi');
_R2(0,16,'429004','','仙桃市','xian tao shi');
_R2(0,16,'420600','','襄阳市','xiang yang shi');
_R3(0,16,14,'420685','','其它区','qi ta qu');
_R3(0,16,14,'420684','','宜城市','yi cheng shi');
_R3(0,16,14,'420683','','枣阳市','zao yang shi');
_R3(0,16,14,'420682','','老河口市','lao he kou shi');
_R3(0,16,14,'420602','','襄城区','xiang cheng qu');
_R3(0,16,14,'420607','','襄州区','xiang zhou qu');
_R3(0,16,14,'420606','','樊城区','fan cheng qu');
_R3(0,16,14,'420625','','谷城县','gu cheng xian');
_R3(0,16,14,'420624','','南漳县','nan zhang xian');
_R3(0,16,14,'420626','','保康县','bao kang xian');
_R2(0,16,'420900','','孝感市','xiao gan shi');
_R3(0,16,15,'420984','','汉川市','han chuan shi');
_R3(0,16,15,'420982','','安陆市','an lu shi');
_R3(0,16,15,'420981','','应城市','ying cheng shi');
_R3(0,16,15,'420985','','其它区','qi ta qu');
_R3(0,16,15,'420922','','大悟县','da wu xian');
_R3(0,16,15,'420921','','孝昌县','xiao chang xian');
_R3(0,16,15,'420923','','云梦县','yun meng xian');
_R3(0,16,15,'420902','','孝南区','xiao nan qu');
_R2(0,16,'420500','','宜昌市','yi chang shi');
_R3(0,16,16,'420584','','其它区','qi ta qu');
_R3(0,16,16,'420583','','枝江市','zhi jiang shi');
_R3(0,16,16,'420582','','当阳市','dang yang shi');
_R3(0,16,16,'420581','','宜都市','yi du shi');
_R3(0,16,16,'420526','','兴山县','xing shan xian');
_R3(0,16,16,'420525','','远安县','yuan an xian');
_R3(0,16,16,'420529','','五峰土家族自治县','wu feng tu jia zu zi zhi xian');
_R3(0,16,16,'420528','','长阳土家族自治县','chang yang tu jia zu zi zhi xian');
_R3(0,16,16,'420527','','秭归县','zi gui xian');
_R3(0,16,16,'420504','','点军区','dian jun qu');
_R3(0,16,16,'420503','','伍家岗区','wu jia gang qu');
_R3(0,16,16,'420502','','西陵区','xi ling qu');
_R3(0,16,16,'420506','','夷陵区','yi ling qu');
_R3(0,16,16,'420505','','猇亭区','xiao ting qu');

_R1(0,'430000','','湖南省','hu nan sheng','湖南省');
_R2(0,17,'430700','','常德市','chang de shi');
_R3(0,17,0,'430781','','津市市','jin shi shi');
_R3(0,17,0,'430782','','其它区','qi ta qu');
_R3(0,17,0,'430721','','安乡县','an xiang xian');
_R3(0,17,0,'430723','','澧县','li xian');
_R3(0,17,0,'430722','','汉寿县','han shou xian');
_R3(0,17,0,'430725','','桃源县','tao yuan xian');
_R3(0,17,0,'430724','','临澧县','lin li xian');
_R3(0,17,0,'430726','','石门县','shi men xian');
_R3(0,17,0,'430703','','鼎城区','ding cheng qu');
_R3(0,17,0,'430702','','武陵区','wu ling qu');
_R2(0,17,'430100','','长沙市','chang sha shi');
_R3(0,17,1,'430181','','浏阳市','liu yang shi');
_R3(0,17,1,'430182','','其它区','qi ta qu');
_R3(0,17,1,'430122','','望城区','wang cheng qu');
_R3(0,17,1,'430124','','宁乡市','ning xiang shi');
_R3(0,17,1,'430111','','雨花区','yu hua qu');
_R3(0,17,1,'430121','','长沙县','chang sha xian');
_R3(0,17,1,'430103','','天心区','tian xin qu');
_R3(0,17,1,'430102','','芙蓉区','fu rong qu');
_R3(0,17,1,'430105','','开福区','kai fu qu');
_R3(0,17,1,'430104','','岳麓区','yue lu qu');
_R2(0,17,'431000','','郴州市','chen zhou shi');
_R3(0,17,2,'431081','','资兴市','zi xing shi');
_R3(0,17,2,'431082','','其它区','qi ta qu');
_R3(0,17,2,'431025','','临武县','lin wu xian');
_R3(0,17,2,'431024','','嘉禾县','jia he xian');
_R3(0,17,2,'431027','','桂东县','gui dong xian');
_R3(0,17,2,'431026','','汝城县','ru cheng xian');
_R3(0,17,2,'431028','','安仁县','an ren xian');
_R3(0,17,2,'431021','','桂阳县','gui yang xian');
_R3(0,17,2,'431023','','永兴县','yong xing xian');
_R3(0,17,2,'431022','','宜章县','yi zhang xian');
_R3(0,17,2,'431003','','苏仙区','su xian qu');
_R3(0,17,2,'431002','','北湖区','bei hu qu');
_R2(0,17,'430400','','衡阳市','heng yang shi');
_R3(0,17,3,'430482','','常宁市','chang ning shi');
_R3(0,17,3,'430481','','耒阳市','lei yang shi');
_R3(0,17,3,'430483','','其它区','qi ta qu');
_R3(0,17,3,'430412','','南岳区','nan yue qu');
_R3(0,17,3,'430406','','雁峰区','yan feng qu');
_R3(0,17,3,'430405','','珠晖区','zhu hui qu');
_R3(0,17,3,'430408','','蒸湘区','zheng xiang qu');
_R3(0,17,3,'430407','','石鼓区','shi gu qu');
_R3(0,17,3,'430422','','衡南县','heng nan xian');
_R3(0,17,3,'430421','','衡阳县','heng yang xian');
_R3(0,17,3,'430424','','衡东县','heng dong xian');
_R3(0,17,3,'430423','','衡山县','heng shan xian');
_R3(0,17,3,'430426','','祁东县','qi dong xian');
_R2(0,17,'431200','','怀化市','huai hua shi');
_R3(0,17,4,'431281','','洪江市','hong jiang shi');
_R3(0,17,4,'431282','','其它区','qi ta qu');
_R3(0,17,4,'431223','','辰溪县','chen xi xian');
_R3(0,17,4,'431222','','沅陵县','yuan ling xian');
_R3(0,17,4,'431225','','会同县','hui tong xian');
_R3(0,17,4,'431224','','溆浦县','xu pu xian');
_R3(0,17,4,'431227','','新晃侗族自治县','xin huang dong zu zi zhi xian');
_R3(0,17,4,'431226','','麻阳苗族自治县','ma yang miao zu zi zhi xian');
_R3(0,17,4,'431229','','靖州苗族侗族自治县','jing zhou miao zu dong zu zi zhi xian');
_R3(0,17,4,'431228','','芷江侗族自治县','zhi jiang dong zu zi zhi xian');
_R3(0,17,4,'431230','','通道侗族自治县','tong dao dong zu zi zhi xian');
_R3(0,17,4,'431221','','中方县','zhong fang xian');
_R3(0,17,4,'431202','','鹤城区','he cheng qu');
_R2(0,17,'431300','','娄底市','lou di shi');
_R3(0,17,5,'431382','','涟源市','lian yuan shi');
_R3(0,17,5,'431381','','冷水江市','leng shui jiang shi');
_R3(0,17,5,'431383','','其它区','qi ta qu');
_R3(0,17,5,'431302','','娄星区','lou xing qu');
_R3(0,17,5,'431322','','新化县','xin hua xian');
_R3(0,17,5,'431321','','双峰县','shuang feng xian');
_R2(0,17,'430500','','邵阳市','shao yang shi');
_R3(0,17,6,'430581','','武冈市','wu gang shi');
_R3(0,17,6,'430582','','其它区','qi ta qu');
_R3(0,17,6,'430521','','邵东市','shao dong shi');
_R3(0,17,6,'430523','','邵阳县','shao yang xian');
_R3(0,17,6,'430522','','新邵县','xin shao xian');
_R3(0,17,6,'430525','','洞口县','dong kou xian');
_R3(0,17,6,'430524','','隆回县','long hui xian');
_R3(0,17,6,'430527','','绥宁县','sui ning xian');
_R3(0,17,6,'430529','','城步苗族自治县','cheng bu miao zu zi zhi xian');
_R3(0,17,6,'430528','','新宁县','xin ning xian');
_R3(0,17,6,'430511','','北塔区','bei ta qu');
_R3(0,17,6,'430503','','大祥区','da xiang qu');
_R3(0,17,6,'430502','','双清区','shuang qing qu');
_R2(0,17,'430300','','湘潭市','xiang tan shi');
_R3(0,17,7,'430381','','湘乡市','xiang xiang shi');
_R3(0,17,7,'430383','','其它区','qi ta qu');
_R3(0,17,7,'430382','','韶山市','shao shan shi');
_R3(0,17,7,'430321','','湘潭县','xiang tan xian');
_R3(0,17,7,'430302','','雨湖区','yu hu qu');
_R3(0,17,7,'430304','','岳塘区','yue tang qu');
_R2(0,17,'433100','','湘西土家族苗族自治州','xiang xi tu jia zu miao zu zi zhi zhou');
_R3(0,17,8,'433101','','吉首市','ji shou shi');
_R3(0,17,8,'433127','','永顺县','yong shun xian');
_R3(0,17,8,'433125','','保靖县','bao jing xian');
_R3(0,17,8,'433126','','古丈县','gu zhang xian');
_R3(0,17,8,'433130','','龙山县','long shan xian');
_R3(0,17,8,'433131','','其它区','qi ta qu');
_R3(0,17,8,'433123','','凤凰县','feng huang xian');
_R3(0,17,8,'433124','','花垣县','hua yuan xian');
_R3(0,17,8,'433122','','泸溪县','lu xi xian');
_R2(0,17,'430900','','益阳市','yi yang shi');
_R3(0,17,9,'430981','','沅江市','yuan jiang shi');
_R3(0,17,9,'430982','','其它区','qi ta qu');
_R3(0,17,9,'430921','','南县','nan xian');
_R3(0,17,9,'430923','','安化县','an hua xian');
_R3(0,17,9,'430922','','桃江县','tao jiang xian');
_R3(0,17,9,'430903','','赫山区','he shan qu');
_R3(0,17,9,'430902','','资阳区','zi yang qu');
_R2(0,17,'431100','','永州市','yong zhou shi');
_R3(0,17,10,'431102','','零陵区','ling ling qu');
_R3(0,17,10,'431103','','冷水滩区','leng shui tan qu');
_R3(0,17,10,'431124','','道县','dao xian');
_R3(0,17,10,'431123','','双牌县','shuang pai xian');
_R3(0,17,10,'431126','','宁远县','ning yuan xian');
_R3(0,17,10,'431125','','江永县','jiang yong xian');
_R3(0,17,10,'431128','','新田县','xin tian xian');
_R3(0,17,10,'431127','','蓝山县','lan shan xian');
_R3(0,17,10,'431129','','江华瑶族自治县','jiang hua yao zu zi zhi xian');
_R3(0,17,10,'431130','','其它区','qi ta qu');
_R3(0,17,10,'431122','','东安县','dong an xian');
_R3(0,17,10,'431121','','祁阳市','qi yang shi');
_R2(0,17,'430600','','岳阳市','yue yang shi');
_R3(0,17,11,'430683','','其它区','qi ta qu');
_R3(0,17,11,'430682','','临湘市','lin xiang shi');
_R3(0,17,11,'430681','','汨罗市','mi luo shi');
_R3(0,17,11,'430611','','君山区','jun shan qu');
_R3(0,17,11,'430602','','岳阳楼区','yue yang lou qu');
_R3(0,17,11,'430603','','云溪区','yun xi qu');
_R3(0,17,11,'430621','','岳阳县','yue yang xian');
_R3(0,17,11,'430624','','湘阴县','xiang yin xian');
_R3(0,17,11,'430623','','华容县','hua rong xian');
_R3(0,17,11,'430626','','平江县','ping jiang xian');
_R2(0,17,'430800','','张家界市','zhang jia jie shi');
_R3(0,17,12,'430811','','武陵源区','wu ling yuan qu');
_R3(0,17,12,'430802','','永定区','yong ding qu');
_R3(0,17,12,'430822','','桑植县','sang zhi xian');
_R3(0,17,12,'430821','','慈利县','ci li xian');
_R3(0,17,12,'430823','','其它区','qi ta qu');
_R2(0,17,'430200','','株洲市','zhu zhou shi');
_R3(0,17,13,'430282','','其它区','qi ta qu');
_R3(0,17,13,'430281','','醴陵市','li ling shi');
_R3(0,17,13,'430211','','天元区','tian yuan qu');
_R3(0,17,13,'430202','','荷塘区','he tang qu');
_R3(0,17,13,'430204','','石峰区','shi feng qu');
_R3(0,17,13,'430203','','芦淞区','lu song qu');
_R3(0,17,13,'430221','','渌口区','lu kou qu');
_R3(0,17,13,'430224','','茶陵县','cha ling xian');
_R3(0,17,13,'430223','','攸县','you xian');
_R3(0,17,13,'430225','','炎陵县','yan ling xian');

_R1(0,'440000','','广东省','guang dong sheng','广东省');
_R2(0,18,'445100','','潮州市','chao zhou shi');
_R3(0,18,0,'445186','','其它区','qi ta qu');
_R3(0,18,0,'445102','','湘桥区','xiang qiao qu');
_R3(0,18,0,'445121','','潮安区','chao an qu');
_R3(0,18,0,'445122','','饶平县','rao ping xian');
_R2(0,18,'441900','','东莞市','dong guan shi');
_R2(0,18,'440600','','佛山市','fo shan shi');
_R3(0,18,2,'440609','','其它区','qi ta qu');
_R3(0,18,2,'440608','','高明区','gao ming qu');
_R3(0,18,2,'440607','','三水区','san shui qu');
_R3(0,18,2,'440606','','顺德区','shun de qu');
_R3(0,18,2,'440605','','南海区','nan hai qu');
_R3(0,18,2,'440604','','禅城区','chan cheng qu');
_R2(0,18,'440100','','广州市','guang zhou shi');
_R3(0,18,3,'440184','','从化区','cong hua qu');
_R3(0,18,3,'440183','','增城区','zeng cheng qu');
_R3(0,18,3,'440189','','其它区','qi ta qu');
_R3(0,18,3,'440115','','南沙区','nan sha qu');
_R3(0,18,3,'440114','','花都区','hua du qu');
_R3(0,18,3,'440113','','番禺区','pan yu qu');
_R3(0,18,3,'440112','','黄埔区','huang pu qu');
_R3(0,18,3,'440111','','白云区','bai yun qu');
_R3(0,18,3,'440106','','天河区','tian he qu');
_R3(0,18,3,'440105','','海珠区','hai zhu qu');
_R3(0,18,3,'440104','','越秀区','yue xiu qu');
_R3(0,18,3,'440103','','荔湾区','li wan qu');
_R2(0,18,'441600','','河源市','he yuan shi');
_R3(0,18,4,'441626','','其它区','qi ta qu');
_R3(0,18,4,'441625','','东源县','dong yuan xian');
_R3(0,18,4,'441624','','和平县','he ping xian');
_R3(0,18,4,'441623','','连平县','lian ping xian');
_R3(0,18,4,'441622','','龙川县','long chuan xian');
_R3(0,18,4,'441621','','紫金县','zi jin xian');
_R3(0,18,4,'441602','','源城区','yuan cheng qu');
_R2(0,18,'441300','','惠州市','hui zhou shi');
_R3(0,18,5,'441303','','惠阳区','hui yang qu');
_R3(0,18,5,'441302','','惠城区','hui cheng qu');
_R3(0,18,5,'441325','','其它区','qi ta qu');
_R3(0,18,5,'441324','','龙门县','long men xian');
_R3(0,18,5,'441323','','惠东县','hui dong xian');
_R3(0,18,5,'441322','','博罗县','bo luo xian');
_R2(0,18,'440700','','江门市','jiang men shi');
_R3(0,18,6,'440786','','其它区','qi ta qu');
_R3(0,18,6,'440785','','恩平市','en ping shi');
_R3(0,18,6,'440784','','鹤山市','he shan shi');
_R3(0,18,6,'440783','','开平市','kai ping shi');
_R3(0,18,6,'440781','','台山市','tai shan shi');
_R3(0,18,6,'440705','','新会区','xin hui qu');
_R3(0,18,6,'440704','','江海区','jiang hai qu');
_R3(0,18,6,'440703','','蓬江区','peng jiang qu');
_R2(0,18,'445200','','揭阳市','jie yang shi');
_R3(0,18,7,'445202','','榕城区','rong cheng qu');
_R3(0,18,7,'445281','','普宁市','pu ning shi');
_R3(0,18,7,'445285','','其它区','qi ta qu');
_R3(0,18,7,'445222','','揭西县','jie xi xian');
_R3(0,18,7,'445221','','揭东区','jie dong qu');
_R3(0,18,7,'445224','','惠来县','hui lai xian');
_R2(0,18,'440900','','茂名市','mao ming shi');
_R3(0,18,8,'440984','','其它区','qi ta qu');
_R3(0,18,8,'440983','','信宜市','xin yi shi');
_R3(0,18,8,'440982','','化州市','hua zhou shi');
_R3(0,18,8,'440981','','高州市','gao zhou shi');
_R3(0,18,8,'440903','','电白区','dian bai qu');
_R3(0,18,8,'440902','','茂南区','mao nan qu');
_R2(0,18,'441400','','梅州市','mei zhou shi');
_R3(0,18,9,'441482','','其它区','qi ta qu');
_R3(0,18,9,'441481','','兴宁市','xing ning shi');
_R3(0,18,9,'441427','','蕉岭县','jiao ling xian');
_R3(0,18,9,'441426','','平远县','ping yuan xian');
_R3(0,18,9,'441424','','五华县','wu hua xian');
_R3(0,18,9,'441423','','丰顺县','feng shun xian');
_R3(0,18,9,'441422','','大埔县','da bu xian');
_R3(0,18,9,'441421','','梅县区','mei xian qu');
_R3(0,18,9,'441402','','梅江区','mei jiang qu');
_R2(0,18,'441800','','清远市','qing yuan shi');
_R3(0,18,10,'441883','','其它区','qi ta qu');
_R3(0,18,10,'441882','','连州市','lian zhou shi');
_R3(0,18,10,'441881','','英德市','ying de shi');
_R3(0,18,10,'441827','','清新区','qing xin qu');
_R3(0,18,10,'441826','','连南瑶族自治县','lian nan yao zu zi zhi xian');
_R3(0,18,10,'441825','','连山壮族瑶族自治县','lian shan zhuang zu yao zu zi zhi xian');
_R3(0,18,10,'441823','','阳山县','yang shan xian');
_R3(0,18,10,'441821','','佛冈县','fo gang xian');
_R3(0,18,10,'441802','','清城区','qing cheng qu');
_R2(0,18,'440500','','汕头市','shan tou shi');
_R3(0,18,11,'440524','','其它区','qi ta qu');
_R3(0,18,11,'440523','','南澳县','nan ao xian');
_R3(0,18,11,'440515','','澄海区','cheng hai qu');
_R3(0,18,11,'440514','','潮南区','chao nan qu');
_R3(0,18,11,'440513','','潮阳区','chao yang qu');
_R3(0,18,11,'440512','','濠江区','hao jiang qu');
_R3(0,18,11,'440511','','金平区','jin ping qu');
_R3(0,18,11,'440507','','龙湖区','long hu qu');
_R2(0,18,'441500','','汕尾市','shan wei shi');
_R3(0,18,12,'441582','','其它区','qi ta qu');
_R3(0,18,12,'441581','','陆丰市','lu feng shi');
_R3(0,18,12,'441502','','城区','cheng qu');
_R3(0,18,12,'441523','','陆河县','lu he xian');
_R3(0,18,12,'441521','','海丰县','hai feng xian');
_R2(0,18,'440200','','韶关市','shao guan shi');
_R3(0,18,13,'440283','','其它区','qi ta qu');
_R3(0,18,13,'440282','','南雄市','nan xiong shi');
_R3(0,18,13,'440281','','乐昌市','le chang shi');
_R3(0,18,13,'440205','','曲江区','qu jiang qu');
_R3(0,18,13,'440204','','浈江区','zhen jiang qu');
_R3(0,18,13,'440203','','武江区','wu jiang qu');
_R3(0,18,13,'440233','','新丰县','xin feng xian');
_R3(0,18,13,'440232','','乳源瑶族自治县','ru yuan yao zu zi zhi xian');
_R3(0,18,13,'440229','','翁源县','weng yuan xian');
_R3(0,18,13,'440224','','仁化县','ren hua xian');
_R3(0,18,13,'440222','','始兴县','shi xing xian');
_R2(0,18,'440300','','深圳市','shen zhen shi');
_R3(0,18,14,'440320','','光明区','guang ming qu');
_R3(0,18,14,'440311','','龙华区','long hua qu');
_R3(0,18,14,'440310','','坪山区','ping shan qu');
_R3(0,18,14,'440309','','其它区','qi ta qu');
_R3(0,18,14,'440308','','盐田区','yan tian qu');
_R3(0,18,14,'440307','','龙岗区','long gang qu');
_R3(0,18,14,'440306','','宝安区','bao an qu');
_R3(0,18,14,'440305','','南山区','nan shan qu');
_R3(0,18,14,'440304','','福田区','fu tian qu');
_R3(0,18,14,'440303','','罗湖区','luo hu qu');
_R2(0,18,'441700','','阳江市','yang jiang shi');
_R3(0,18,15,'441782','','其它区','qi ta qu');
_R3(0,18,15,'441781','','阳春市','yang chun shi');
_R3(0,18,15,'441702','','江城区','jiang cheng qu');
_R3(0,18,15,'441723','','阳东区','yang dong qu');
_R3(0,18,15,'441721','','阳西县','yang xi xian');
_R2(0,18,'445300','','云浮市','yun fu shi');
_R3(0,18,16,'445302','','云城区','yun cheng qu');
_R3(0,18,16,'445321','','新兴县','xin xing xian');
_R3(0,18,16,'445323','','云安区','yun an qu');
_R3(0,18,16,'445322','','郁南县','yu nan xian');
_R3(0,18,16,'445381','','罗定市','luo ding shi');
_R3(0,18,16,'445382','','其它区','qi ta qu');
_R2(0,18,'440800','','湛江市','zhan jiang shi');
_R3(0,18,17,'440884','','其它区','qi ta qu');
_R3(0,18,17,'440883','','吴川市','wu chuan shi');
_R3(0,18,17,'440882','','雷州市','lei zhou shi');
_R3(0,18,17,'440881','','廉江市','lian jiang shi');
_R3(0,18,17,'440811','','麻章区','ma zhang qu');
_R3(0,18,17,'440804','','坡头区','po tou qu');
_R3(0,18,17,'440803','','霞山区','xia shan qu');
_R3(0,18,17,'440802','','赤坎区','chi kan qu');
_R3(0,18,17,'440825','','徐闻县','xu wen xian');
_R3(0,18,17,'440823','','遂溪县','sui xi xian');
_R2(0,18,'441200','','肇庆市','zhao qing shi');
_R3(0,18,18,'441285','','其它区','qi ta qu');
_R3(0,18,18,'441284','','四会市','si hui shi');
_R3(0,18,18,'441283','','高要区','gao yao qu');
_R3(0,18,18,'441226','','德庆县','de qing xian');
_R3(0,18,18,'441225','','封开县','feng kai xian');
_R3(0,18,18,'441224','','怀集县','huai ji xian');
_R3(0,18,18,'441223','','广宁县','guang ning xian');
_R3(0,18,18,'441203','','鼎湖区','ding hu qu');
_R3(0,18,18,'441202','','端州区','duan zhou qu');
_R2(0,18,'442000','','中山市','zhong shan shi');
_R2(0,18,'440400','','珠海市','zhu hai shi');
_R3(0,18,20,'440488','','其它区','qi ta qu');
_R3(0,18,20,'440404','','金湾区','jin wan qu');
_R3(0,18,20,'440403','','斗门区','dou men qu');
_R3(0,18,20,'440402','','香洲区','xiang zhou qu');

_R1(0,'450000','','广西','guang xi zhuang zu zi zhi qu','广西壮族自治区');
_R2(0,19,'451000','','百色市','bai se shi');
_R3(0,19,0,'451030','','西林县','xi lin xian');
_R3(0,19,0,'451032','','其它区','qi ta qu');
_R3(0,19,0,'451031','','隆林各族自治县','long lin ge zu zi zhi xian');
_R3(0,19,0,'451027','','凌云县','ling yun xian');
_R3(0,19,0,'451026','','那坡县','na po xian');
_R3(0,19,0,'451029','','田林县','tian lin xian');
_R3(0,19,0,'451028','','乐业县','le ye xian');
_R3(0,19,0,'451023','','平果市','ping guo shi');
_R3(0,19,0,'451022','','田东县','tian dong xian');
_R3(0,19,0,'451025','','靖西市','jing xi shi');
_R3(0,19,0,'451024','','德保县','de bao xian');
_R3(0,19,0,'451021','','田阳区','tian yang qu');
_R3(0,19,0,'451002','','右江区','you jiang qu');
_R2(0,19,'450500','','北海市','bei hai shi');
_R3(0,19,1,'450521','','合浦县','he pu xian');
_R3(0,19,1,'450522','','其它区','qi ta qu');
_R3(0,19,1,'450512','','铁山港区','tie shan gang qu');
_R3(0,19,1,'450503','','银海区','yin hai qu');
_R3(0,19,1,'450502','','海城区','hai cheng qu');
_R2(0,19,'451400','','崇左市','chong zuo shi');
_R3(0,19,2,'451481','','凭祥市','ping xiang shi');
_R3(0,19,2,'451482','','其它区','qi ta qu');
_R3(0,19,2,'451423','','龙州县','long zhou xian');
_R3(0,19,2,'451422','','宁明县','ning ming xian');
_R3(0,19,2,'451425','','天等县','tian deng xian');
_R3(0,19,2,'451424','','大新县','da xin xian');
_R3(0,19,2,'451421','','扶绥县','fu sui xian');
_R3(0,19,2,'451402','','江州区','jiang zhou qu');
_R2(0,19,'450600','','防城港市','fang cheng gang shi');
_R3(0,19,3,'450682','','其它区','qi ta qu');
_R3(0,19,3,'450681','','东兴市','dong xing shi');
_R3(0,19,3,'450602','','港口区','gang kou qu');
_R3(0,19,3,'450603','','防城区','fang cheng qu');
_R3(0,19,3,'450621','','上思县','shang si xian');
_R2(0,19,'450800','','贵港市','gui gang shi');
_R3(0,19,4,'450882','','其它区','qi ta qu');
_R3(0,19,4,'450881','','桂平市','gui ping shi');
_R3(0,19,4,'450804','','覃塘区','qin tang qu');
_R3(0,19,4,'450803','','港南区','gang nan qu');
_R3(0,19,4,'450802','','港北区','gang bei qu');
_R3(0,19,4,'450821','','平南县','ping nan xian');
_R2(0,19,'450300','','桂林市','gui lin shi');
_R3(0,19,5,'450333','','其它区','qi ta qu');
_R3(0,19,5,'450330','','平乐县','ping le xian');
_R3(0,19,5,'450332','','恭城瑶族自治县','gong cheng yao zu zi zhi xian');
_R3(0,19,5,'450331','','荔浦市','li pu shi');
_R3(0,19,5,'450327','','灌阳县','guan yang xian');
_R3(0,19,5,'450326','','永福县','yong fu xian');
_R3(0,19,5,'450329','','资源县','zi yuan xian');
_R3(0,19,5,'450328','','龙胜各族自治县','long sheng ge zu zi zhi xian');
_R3(0,19,5,'450323','','灵川县','ling chuan xian');
_R3(0,19,5,'450322','','临桂区','lin gui qu');
_R3(0,19,5,'450325','','兴安县','xing an xian');
_R3(0,19,5,'450324','','全州县','quan zhou xian');
_R3(0,19,5,'450321','','阳朔县','yang shuo xian');
_R3(0,19,5,'450311','','雁山区','yan shan qu');
_R3(0,19,5,'450305','','七星区','qi xing qu');
_R3(0,19,5,'450304','','象山区','xiang shan qu');
_R3(0,19,5,'450303','','叠彩区','die cai qu');
_R3(0,19,5,'450302','','秀峰区','xiu feng qu');
_R2(0,19,'451200','','河池市','he chi shi');
_R3(0,19,6,'451282','','其它区','qi ta qu');
_R3(0,19,6,'451281','','宜州区','yi zhou qu');
_R3(0,19,6,'451229','','大化瑶族自治县','da hua yao zu zi zhi xian');
_R3(0,19,6,'451228','','都安瑶族自治县','du an yao zu zi zhi xian');
_R3(0,19,6,'451225','','罗城仫佬族自治县','luo cheng mu lao zu zi zhi xian');
_R3(0,19,6,'451224','','东兰县','dong lan xian');
_R3(0,19,6,'451227','','巴马瑶族自治县','ba ma yao zu zi zhi xian');
_R3(0,19,6,'451226','','环江毛南族自治县','huan jiang mao nan zu zi zhi xian');
_R3(0,19,6,'451221','','南丹县','nan dan xian');
_R3(0,19,6,'451223','','凤山县','feng shan xian');
_R3(0,19,6,'451222','','天峨县','tian e xian');
_R3(0,19,6,'451202','','金城江区','jin cheng jiang qu');
_R2(0,19,'451100','','贺州市','he zhou shi');
_R3(0,19,7,'451103','','平桂区','ping gui qu');
_R3(0,19,7,'451102','','八步区','ba bu qu');
_R3(0,19,7,'451122','','钟山县','zhong shan xian');
_R3(0,19,7,'451121','','昭平县','zhao ping xian');
_R3(0,19,7,'451124','','其它区','qi ta qu');
_R3(0,19,7,'451123','','富川瑶族自治县','fu chuan yao zu zi zhi xian');
_R2(0,19,'451300','','来宾市','lai bin shi');
_R3(0,19,8,'451382','','其它区','qi ta qu');
_R3(0,19,8,'451381','','合山市','he shan shi');
_R3(0,19,8,'451302','','兴宾区','xing bin qu');
_R3(0,19,8,'451324','','金秀瑶族自治县','jin xiu yao zu zi zhi xian');
_R3(0,19,8,'451323','','武宣县','wu xuan xian');
_R3(0,19,8,'451322','','象州县','xiang zhou xian');
_R3(0,19,8,'451321','','忻城县','xin cheng xian');
_R2(0,19,'450200','','柳州市','liu zhou shi');
_R3(0,19,9,'450205','','柳北区','liu bei qu');
_R3(0,19,9,'450202','','城中区','cheng zhong qu');
_R3(0,19,9,'450204','','柳南区','liu nan qu');
_R3(0,19,9,'450203','','鱼峰区','yu feng qu');
_R3(0,19,9,'450227','','其它区','qi ta qu');
_R3(0,19,9,'450224','','融安县','rong an xian');
_R3(0,19,9,'450223','','鹿寨县','lu zhai xian');
_R3(0,19,9,'450226','','三江侗族自治县','san jiang dong zu zi zhi xian');
_R3(0,19,9,'450225','','融水苗族自治县','rong shui miao zu zi zhi xian');
_R3(0,19,9,'450222','','柳城县','liu cheng xian');
_R3(0,19,9,'450221','','柳江区','liu jiang qu');
_R2(0,19,'450100','','南宁市','nan ning shi');
_R3(0,19,10,'450128','','其它区','qi ta qu');
_R3(0,19,10,'450125','','上林县','shang lin xian');
_R3(0,19,10,'450124','','马山县','ma shan xian');
_R3(0,19,10,'450127','','横州市','heng zhou shi');
_R3(0,19,10,'450126','','宾阳县','bin yang xian');
_R3(0,19,10,'450123','','隆安县','long an xian');
_R3(0,19,10,'450122','','武鸣区','wu ming qu');
_R3(0,19,10,'450107','','西乡塘区','xi xiang tang qu');
_R3(0,19,10,'450109','','邕宁区','yong ning qu');
_R3(0,19,10,'450108','','良庆区','liang qing qu');
_R3(0,19,10,'450103','','青秀区','qing xiu qu');
_R3(0,19,10,'450102','','兴宁区','xing ning qu');
_R3(0,19,10,'450105','','江南区','jiang nan qu');
_R2(0,19,'450700','','钦州市','qin zhou shi');
_R3(0,19,11,'450723','','其它区','qi ta qu');
_R3(0,19,11,'450722','','浦北县','pu bei xian');
_R3(0,19,11,'450721','','灵山县','ling shan xian');
_R3(0,19,11,'450703','','钦北区','qin bei qu');
_R3(0,19,11,'450702','','钦南区','qin nan qu');
_R2(0,19,'450400','','梧州市','wu zhou shi');
_R3(0,19,12,'450482','','其它区','qi ta qu');
_R3(0,19,12,'450481','','岑溪市','cen xi shi');
_R3(0,19,12,'450403','','万秀区','wan xiu qu');
_R3(0,19,12,'450406','','龙圩区','long wei qu');
_R3(0,19,12,'450405','','长洲区','chang zhou qu');
_R3(0,19,12,'450422','','藤县','teng xian');
_R3(0,19,12,'450421','','苍梧县','cang wu xian');
_R3(0,19,12,'450423','','蒙山县','meng shan xian');
_R2(0,19,'450900','','玉林市','yu lin shi');
_R3(0,19,13,'450903','','福绵区','fu mian qu');
_R3(0,19,13,'450902','','玉州区','yu zhou qu');
_R3(0,19,13,'450982','','其它区','qi ta qu');
_R3(0,19,13,'450981','','北流市','bei liu shi');
_R3(0,19,13,'450924','','兴业县','xing ye xian');
_R3(0,19,13,'450921','','容县','rong xian');
_R3(0,19,13,'450923','','博白县','bo bai xian');
_R3(0,19,13,'450922','','陆川县','lu chuan xian');

_R1(0,'460000','','海南省','hai nan sheng','海南省');
_R2(0,20,'469030','','白沙黎族自治县','bai sha li zu zi zhi xian');
_R2(0,20,'469035','','保亭黎族苗族自治县','bao ting li zu miao zu zi zhi xian');
_R2(0,20,'469031','','昌江黎族自治县','chang jiang li zu zi zhi xian');
_R2(0,20,'469027','','澄迈县','cheng mai xian');
_R2(0,20,'469003','','儋州市','dan zhou shi');
_R2(0,20,'469025','','定安县','ding an xian');
_R2(0,20,'469007','','东方市','dong fang shi');
_R2(0,20,'460100','','海口市','hai kou shi');
_R3(0,20,7,'460108','','美兰区','mei lan qu');
_R3(0,20,7,'460107','','琼山区','qiong shan qu');
_R3(0,20,7,'460106','','龙华区','long hua qu');
_R3(0,20,7,'460105','','秀英区','xiu ying qu');
_R3(0,20,7,'460109','','其它区','qi ta qu');
_R2(0,20,'469033','','乐东黎族自治县','le dong li zu zi zhi xian');
_R2(0,20,'469028','','临高县','lin gao xian');
_R2(0,20,'469034','','陵水黎族自治县','ling shui li zu zi zhi xian');
_R2(0,20,'469002','','琼海市','qiong hai shi');
_R2(0,20,'469036','','琼中黎族苗族自治县','qiong zhong li zu miao zu zi zhi xian');
_R2(0,20,'460300','','三沙市','san sha shi');
_R3(0,20,13,'460322','','南沙区','nan sha qu');
_R3(0,20,13,'460321','','西沙区','xi sha qu');
_R2(0,20,'460200','','三亚市','san ya shi');
_R3(0,20,14,'460205','','崖州区','ya zhou qu');
_R3(0,20,14,'460204','','天涯区','tian ya qu');
_R3(0,20,14,'460203','','吉阳区','ji yang qu');
_R3(0,20,14,'460202','','海棠区','hai tang qu');
_R2(0,20,'469026','','屯昌县','tun chang xian');
_R2(0,20,'469006','','万宁市','wan ning shi');
_R2(0,20,'469005','','文昌市','wen chang shi');
_R2(0,20,'469001','','五指山市','wu zhi shan shi');

_R1(0,'500000','','重庆','chong qing','重庆');
_R2(0,21,'500100','','重庆市','chong qing shi');
_R3(0,21,0,'500385','','其它区','qi ta qu');
_R3(0,21,0,'500384','','南川区','nan chuan qu');
_R3(0,21,0,'500383','','永川区','yong chuan qu');
_R3(0,21,0,'500382','','合川区','he chuan qu');
_R3(0,21,0,'500381','','江津区','jiang jin qu');
_R3(0,21,0,'500238','','巫溪县','wu xi xian');
_R3(0,21,0,'500237','','巫山县','wu shan xian');
_R3(0,21,0,'500236','','奉节县','feng jie xian');
_R3(0,21,0,'500242','','酉阳土家族苗族自治县','you yang tu jia zu miao zu zi zhi xian');
_R3(0,21,0,'500241','','秀山土家族苗族自治县','xiu shan tu jia zu miao zu zi zhi xian');
_R3(0,21,0,'500240','','石柱土家族自治县','shi zhu tu jia zu zi zhi xian');
_R3(0,21,0,'500243','','彭水苗族土家族自治县','peng shui miao zu tu jia zu zi zhi xian');
_R3(0,21,0,'500228','','梁平区','liang ping qu');
_R3(0,21,0,'500227','','璧山区','bi shan qu');
_R3(0,21,0,'500226','','荣昌区','rong chang qu');
_R3(0,21,0,'500225','','大足区','da zu qu');
_R3(0,21,0,'500229','','城口县','cheng kou xian');
_R3(0,21,0,'500231','','垫江县','dian jiang xian');
_R3(0,21,0,'500230','','丰都县','feng du xian');
_R3(0,21,0,'500235','','云阳县','yun yang xian');
_R3(0,21,0,'500234','','开州区','kai zhou qu');
_R3(0,21,0,'500233','','忠县','zhong xian');
_R3(0,21,0,'500232','','武隆区','wu long qu');
_R3(0,21,0,'500224','','铜梁区','tong liang qu');
_R3(0,21,0,'500223','','潼南区','tong nan qu');
_R3(0,21,0,'500222','','綦江区','qi jiang qu');
_R3(0,21,0,'500115','','长寿区','chang shou qu');
_R3(0,21,0,'500107','','九龙坡区','jiu long po qu');
_R3(0,21,0,'500106','','沙坪坝区','sha ping ba qu');
_R3(0,21,0,'500105','','江北区','jiang bei qu');
_R3(0,21,0,'500104','','大渡口区','da du kou qu');
_R3(0,21,0,'500109','','北碚区','bei bei qu');
_R3(0,21,0,'500108','','南岸区','nan an qu');
_R3(0,21,0,'500114','','黔江区','qian jiang qu');
_R3(0,21,0,'500113','','巴南区','ba nan qu');
_R3(0,21,0,'500112','','渝北区','yu bei qu');
_R3(0,21,0,'500103','','渝中区','yu zhong qu');
_R3(0,21,0,'500102','','涪陵区','fu ling qu');
_R3(0,21,0,'500101','','万州区','wan zhou qu');

_R1(0,'510000','','四川省','si chuan sheng','四川省');
_R2(0,22,'513200','','阿坝藏族羌族自治州','a ba zang zu qiang zu zi zhi zhou');
_R3(0,22,0,'513222','','理县','li xian');
_R3(0,22,0,'513221','','汶川县','wen chuan xian');
_R3(0,22,0,'513226','','金川县','jin chuan xian');
_R3(0,22,0,'513225','','九寨沟县','jiu zhai gou xian');
_R3(0,22,0,'513224','','松潘县','song pan xian');
_R3(0,22,0,'513223','','茂县','mao xian');
_R3(0,22,0,'513233','','红原县','hong yuan xian');
_R3(0,22,0,'513232','','若尔盖县','ruo er gai xian');
_R3(0,22,0,'513231','','阿坝县','a ba xian');
_R3(0,22,0,'513230','','壤塘县','rang tang xian');
_R3(0,22,0,'513234','','其它区','qi ta qu');
_R3(0,22,0,'513229','','马尔康市','ma er kang shi');
_R3(0,22,0,'513228','','黑水县','hei shui xian');
_R3(0,22,0,'513227','','小金县','xiao jin xian');
_R2(0,22,'511900','','巴中市','ba zhong shi');
_R3(0,22,1,'511921','','通江县','tong jiang xian');
_R3(0,22,1,'511924','','其它区','qi ta qu');
_R3(0,22,1,'511922','','南江县','nan jiang xian');
_R3(0,22,1,'511923','','平昌县','ping chang xian');
_R3(0,22,1,'511902','','巴州区','ba zhou qu');
_R3(0,22,1,'511903','','恩阳区','en yang qu');
_R2(0,22,'510100','','成都市','cheng du shi');
_R3(0,22,2,'512081','','简阳市','jian yang shi');
_R3(0,22,2,'510184','','崇州市','chong zhou shi');
_R3(0,22,2,'510183','','邛崃市','qiong lai shi');
_R3(0,22,2,'510185','','其它区','qi ta qu');
_R3(0,22,2,'510182','','彭州市','peng zhou shi');
_R3(0,22,2,'510181','','都江堰市','du jiang yan shi');
_R3(0,22,2,'510113','','青白江区','qing bai jiang qu');
_R3(0,22,2,'510112','','龙泉驿区','long quan yi qu');
_R3(0,22,2,'510104','','锦江区','jin jiang qu');
_R3(0,22,2,'510106','','金牛区','jin niu qu');
_R3(0,22,2,'510105','','青羊区','qing yang qu');
_R3(0,22,2,'510108','','成华区','cheng hua qu');
_R3(0,22,2,'510107','','武侯区','wu hou qu');
_R3(0,22,2,'510131','','蒲江县','pu jiang xian');
_R3(0,22,2,'510132','','新津区','xin jin qu');
_R3(0,22,2,'510129','','大邑县','da yi xian');
_R3(0,22,2,'510122','','双流区','shuang liu qu');
_R3(0,22,2,'510121','','金堂县','jin tang xian');
_R3(0,22,2,'510124','','郫都区','pi dou qu');
_R3(0,22,2,'510115','','温江区','wen jiang qu');
_R3(0,22,2,'510114','','新都区','xin du qu');
_R2(0,22,'511700','','达州市','da zhou shi');
_R3(0,22,3,'511781','','万源市','wan yuan shi');
_R3(0,22,3,'511782','','其它区','qi ta qu');
_R3(0,22,3,'511702','','通川区','tong chuan qu');
_R3(0,22,3,'511722','','宣汉县','xuan han xian');
_R3(0,22,3,'511723','','开江县','kai jiang xian');
_R3(0,22,3,'511721','','达川区','da chuan qu');
_R3(0,22,3,'511724','','大竹县','da zhu xian');
_R3(0,22,3,'511725','','渠县','qu xian');
_R2(0,22,'510600','','德阳市','de yang shi');
_R3(0,22,4,'510681','','广汉市','guang han shi');
_R3(0,22,4,'510683','','绵竹市','mian zhu shi');
_R3(0,22,4,'510682','','什邡市','shi fang shi');
_R3(0,22,4,'510684','','其它区','qi ta qu');
_R3(0,22,4,'510603','','旌阳区','jing yang qu');
_R3(0,22,4,'510623','','中江县','zhong jiang xian');
_R3(0,22,4,'510626','','罗江区','luo jiang qu');
_R2(0,22,'513300','','甘孜藏族自治州','gan zi zang zu zi zhi zhou');
_R3(0,22,5,'513321','','康定市','kang ding shi');
_R3(0,22,5,'513325','','雅江县','ya jiang xian');
_R3(0,22,5,'513324','','九龙县','jiu long xian');
_R3(0,22,5,'513323','','丹巴县','dan ba xian');
_R3(0,22,5,'513322','','泸定县','lu ding xian');
_R3(0,22,5,'513339','','其它区','qi ta qu');
_R3(0,22,5,'513338','','得荣县','de rong xian');
_R3(0,22,5,'513337','','稻城县','dao cheng xian');
_R3(0,22,5,'513332','','石渠县','shi qu xian');
_R3(0,22,5,'513331','','白玉县','bai yu xian');
_R3(0,22,5,'513330','','德格县','de ge xian');
_R3(0,22,5,'513336','','乡城县','xiang cheng xian');
_R3(0,22,5,'513335','','巴塘县','ba tang xian');
_R3(0,22,5,'513334','','理塘县','li tang xian');
_R3(0,22,5,'513333','','色达县','se da xian');
_R3(0,22,5,'513329','','新龙县','xin long xian');
_R3(0,22,5,'513328','','甘孜县','gan zi xian');
_R3(0,22,5,'513327','','炉霍县','lu huo xian');
_R3(0,22,5,'513326','','道孚县','dao fu xian');
_R2(0,22,'511600','','广安市','guang an shi');
_R3(0,22,6,'511681','','华蓥市','hua ying shi');
_R3(0,22,6,'511683','','其它区','qi ta qu');
_R3(0,22,6,'511602','','广安区','guang an qu');
_R3(0,22,6,'511603','','前锋区','qian feng qu');
_R3(0,22,6,'511623','','邻水县','lin shui xian');
_R3(0,22,6,'511621','','岳池县','yue chi xian');
_R3(0,22,6,'511622','','武胜县','wu sheng xian');
_R2(0,22,'510800','','广元市','guang yuan shi');
_R3(0,22,7,'510802','','利州区','li zhou qu');
_R3(0,22,7,'510821','','旺苍县','wang cang xian');
_R3(0,22,7,'510824','','苍溪县','cang xi xian');
_R3(0,22,7,'510825','','其它区','qi ta qu');
_R3(0,22,7,'510822','','青川县','qing chuan xian');
_R3(0,22,7,'510823','','剑阁县','jian ge xian');
_R3(0,22,7,'510811','','昭化区','zhao hua qu');
_R3(0,22,7,'510812','','朝天区','chao tian qu');
_R2(0,22,'511100','','乐山市','le shan shi');
_R3(0,22,8,'511132','','峨边彝族自治县','e bian yi zu zi zhi xian');
_R3(0,22,8,'511133','','马边彝族自治县','ma bian yi zu zi zhi xian');
_R3(0,22,8,'511129','','沐川县','mu chuan xian');
_R3(0,22,8,'511126','','夹江县','jia jiang xian');
_R3(0,22,8,'511182','','其它区','qi ta qu');
_R3(0,22,8,'511181','','峨眉山市','e mei shan shi');
_R3(0,22,8,'511102','','市中区','shi zhong qu');
_R3(0,22,8,'511124','','井研县','jing yan xian');
_R3(0,22,8,'511123','','犍为县','qian wei xian');
_R3(0,22,8,'511113','','金口河区','jin kou he qu');
_R3(0,22,8,'511111','','沙湾区','sha wan qu');
_R3(0,22,8,'511112','','五通桥区','wu tong qiao qu');
_R2(0,22,'513400','','凉山彝族自治州','liang shan yi zu zi zhi zhou');
_R3(0,22,9,'513438','','其它区','qi ta qu');
_R3(0,22,9,'513437','','雷波县','lei bo xian');
_R3(0,22,9,'513436','','美姑县','mei gu xian');
_R3(0,22,9,'513431','','昭觉县','zhao jue xian');
_R3(0,22,9,'513430','','金阳县','jin yang xian');
_R3(0,22,9,'513435','','甘洛县','gan luo xian');
_R3(0,22,9,'513434','','越西县','yue xi xian');
_R3(0,22,9,'513433','','冕宁县','mian ning xian');
_R3(0,22,9,'513432','','喜德县','xi de xian');
_R3(0,22,9,'513428','','普格县','pu ge xian');
_R3(0,22,9,'513427','','宁南县','ning nan xian');
_R3(0,22,9,'513426','','会东县','hui dong xian');
_R3(0,22,9,'513425','','会理市','hui li shi');
_R3(0,22,9,'513429','','布拖县','bu tuo xian');
_R3(0,22,9,'513401','','西昌市','xi chang shi');
_R3(0,22,9,'513424','','德昌县','de chang xian');
_R3(0,22,9,'513423','','盐源县','yan yuan xian');
_R3(0,22,9,'513422','','木里藏族自治县','mu li zang zu zi zhi xian');
_R2(0,22,'510500','','泸州市','lu zhou shi');
_R3(0,22,10,'510502','','江阳区','jiang yang qu');
_R3(0,22,10,'510504','','龙马潭区','long ma tan qu');
_R3(0,22,10,'510503','','纳溪区','na xi qu');
_R3(0,22,10,'510522','','合江县','he jiang xian');
_R3(0,22,10,'510521','','泸县','lu xian');
_R3(0,22,10,'510524','','叙永县','xu yong xian');
_R3(0,22,10,'510526','','其它区','qi ta qu');
_R3(0,22,10,'510525','','古蔺县','gu lin xian');
_R2(0,22,'511400','','眉山市','mei shan shi');
_R3(0,22,11,'511421','','仁寿县','ren shou xian');
_R3(0,22,11,'511422','','彭山区','peng shan qu');
_R3(0,22,11,'511402','','东坡区','dong po qu');
_R3(0,22,11,'511425','','青神县','qing shen xian');
_R3(0,22,11,'511426','','其它区','qi ta qu');
_R3(0,22,11,'511423','','洪雅县','hong ya xian');
_R3(0,22,11,'511424','','丹棱县','dan ling xian');
_R2(0,22,'510700','','绵阳市','mian yang shi');
_R3(0,22,12,'510727','','平武县','ping wu xian');
_R3(0,22,12,'510722','','三台县','san tai xian');
_R3(0,22,12,'510724','','安州区','an zhou qu');
_R3(0,22,12,'510723','','盐亭县','yan ting xian');
_R3(0,22,12,'510726','','北川羌族自治县','bei chuan qiang zu zi zhi xian');
_R3(0,22,12,'510725','','梓潼县','zi tong xian');
_R3(0,22,12,'510782','','其它区','qi ta qu');
_R3(0,22,12,'510781','','江油市','jiang you shi');
_R3(0,22,12,'510704','','游仙区','you xian qu');
_R3(0,22,12,'510703','','涪城区','fu cheng qu');
_R2(0,22,'511300','','南充市','nan chong shi');
_R3(0,22,13,'511382','','其它区','qi ta qu');
_R3(0,22,13,'511381','','阆中市','lang zhong shi');
_R3(0,22,13,'511322','','营山县','ying shan xian');
_R3(0,22,13,'511323','','蓬安县','peng an xian');
_R3(0,22,13,'511321','','南部县','nan bu xian');
_R3(0,22,13,'511304','','嘉陵区','jia ling qu');
_R3(0,22,13,'511302','','顺庆区','shun qing qu');
_R3(0,22,13,'511303','','高坪区','gao ping qu');
_R3(0,22,13,'511324','','仪陇县','yi long xian');
_R3(0,22,13,'511325','','西充县','xi chong xian');
_R2(0,22,'511000','','内江市','nei jiang shi');
_R3(0,22,14,'511025','','资中县','zi zhong xian');
_R3(0,22,14,'511024','','威远县','wei yuan xian');
_R3(0,22,14,'511011','','东兴区','dong xing qu');
_R3(0,22,14,'511029','','其它区','qi ta qu');
_R3(0,22,14,'511028','','隆昌市','long chang shi');
_R3(0,22,14,'511002','','市中区','shi zhong qu');
_R2(0,22,'510400','','攀枝花市','pan zhi hua shi');
_R3(0,22,15,'510403','','西区','xi qu');
_R3(0,22,15,'510402','','东区','dong qu');
_R3(0,22,15,'510423','','其它区','qi ta qu');
_R3(0,22,15,'510422','','盐边县','yan bian xian');
_R3(0,22,15,'510421','','米易县','mi yi xian');
_R3(0,22,15,'510411','','仁和区','ren he qu');
_R2(0,22,'510900','','遂宁市','sui ning shi');
_R3(0,22,16,'510903','','船山区','chuan shan qu');
_R3(0,22,16,'510904','','安居区','an ju qu');
_R3(0,22,16,'510923','','大英县','da ying xian');
_R3(0,22,16,'510924','','其它区','qi ta qu');
_R3(0,22,16,'510921','','蓬溪县','peng xi xian');
_R3(0,22,16,'510922','','射洪市','she hong shi');
_R2(0,22,'511800','','雅安市','ya an shi');
_R3(0,22,17,'511802','','雨城区','yu cheng qu');
_R3(0,22,17,'511827','','宝兴县','bao xing xian');
_R3(0,22,17,'511828','','其它区','qi ta qu');
_R3(0,22,17,'511821','','名山区','ming shan qu');
_R3(0,22,17,'511822','','荥经县','ying jing xian');
_R3(0,22,17,'511825','','天全县','tian quan xian');
_R3(0,22,17,'511826','','芦山县','lu shan xian');
_R3(0,22,17,'511823','','汉源县','han yuan xian');
_R3(0,22,17,'511824','','石棉县','shi mian xian');
_R2(0,22,'511500','','宜宾市','yi bin shi');
_R3(0,22,18,'511530','','其它区','qi ta qu');
_R3(0,22,18,'511524','','长宁县','chang ning xian');
_R3(0,22,18,'511525','','高县','gao xian');
_R3(0,22,18,'511522','','南溪区','nan xi qu');
_R3(0,22,18,'511523','','江安县','jiang an xian');
_R3(0,22,18,'511528','','兴文县','xing wen xian');
_R3(0,22,18,'511529','','屏山县','ping shan xian');
_R3(0,22,18,'511526','','珙县','gong xian');
_R3(0,22,18,'511527','','筠连县','jun lian xian');
_R3(0,22,18,'511521','','叙州区','xu zhou qu');
_R3(0,22,18,'511502','','翠屏区','cui ping qu');
_R2(0,22,'510300','','自贡市','zi gong shi');
_R3(0,22,19,'510323','','其它区','qi ta qu');
_R3(0,22,19,'510322','','富顺县','fu shun xian');
_R3(0,22,19,'510321','','荣县','rong xian');
_R3(0,22,19,'510311','','沿滩区','yan tan qu');
_R3(0,22,19,'510302','','自流井区','zi liu jing qu');
_R3(0,22,19,'510304','','大安区','da an qu');
_R3(0,22,19,'510303','','贡井区','gong jing qu');
_R2(0,22,'512000','','资阳市','zi yang shi');
_R3(0,22,20,'512002','','雁江区','yan jiang qu');
_R3(0,22,20,'512022','','乐至县','le zhi xian');
_R3(0,22,20,'512021','','安岳县','an yue xian');
_R3(0,22,20,'512082','','其它区','qi ta qu');

_R1(0,'520000','','贵州省','gui zhou sheng','贵州省');
_R2(0,23,'520400','','安顺市','an shun shi');
_R3(0,23,0,'520421','','平坝区','ping ba qu');
_R3(0,23,0,'520422','','普定县','pu ding xian');
_R3(0,23,0,'520423','','镇宁布依族苗族自治县','zhen ning bu yi zu miao zu zi zhi xian');
_R3(0,23,0,'520424','','关岭布依族苗族自治县','guan ling bu yi zu miao zu zi zhi xian');
_R3(0,23,0,'520425','','紫云苗族布依族自治县','zi yun miao zu bu yi zu zi zhi xian');
_R3(0,23,0,'520426','','其它区','qi ta qu');
_R3(0,23,0,'520402','','西秀区','xi xiu qu');
_R2(0,23,'522400','','毕节市','bi jie shi');
_R3(0,23,1,'522422','','大方县','da fang xian');
_R3(0,23,1,'522423','','黔西市','qian xi shi');
_R3(0,23,1,'522424','','金沙县','jin sha xian');
_R3(0,23,1,'522425','','织金县','zhi jin xian');
_R3(0,23,1,'522426','','纳雍县','na yong xian');
_R3(0,23,1,'522427','','威宁彝族回族苗族自治县','wei ning yi zu hui zu miao zu zi zhi xian');
_R3(0,23,1,'522428','','赫章县','he zhang xian');
_R3(0,23,1,'522429','','其它区','qi ta qu');
_R3(0,23,1,'522401','','七星关区','qi xing guan qu');
_R2(0,23,'520100','','贵阳市','gui yang shi');
_R3(0,23,2,'520103','','云岩区','yun yan qu');
_R3(0,23,2,'520102','','南明区','nan ming qu');
_R3(0,23,2,'520112','','乌当区','wu dang qu');
_R3(0,23,2,'520111','','花溪区','hua xi qu');
_R3(0,23,2,'520151','','观山湖区','guan shan hu qu');
_R3(0,23,2,'520113','','白云区','bai yun qu');
_R3(0,23,2,'520121','','开阳县','kai yang xian');
_R3(0,23,2,'520122','','息烽县','xi feng xian');
_R3(0,23,2,'520123','','修文县','xiu wen xian');
_R3(0,23,2,'520182','','其它区','qi ta qu');
_R3(0,23,2,'520181','','清镇市','qing zhen shi');
_R2(0,23,'520200','','六盘水市','liu pan shui shi');
_R3(0,23,3,'520221','','水城区','shui cheng qu');
_R3(0,23,3,'520222','','盘州市','pan zhou shi');
_R3(0,23,3,'520223','','其它区','qi ta qu');
_R3(0,23,3,'520201','','钟山区','zhong shan qu');
_R3(0,23,3,'520203','','六枝特区','liu zhi te qu');
_R2(0,23,'522600','','黔东南苗族侗族自治州','qian dong nan miao zu dong zu zi zhi zhou');
_R3(0,23,4,'522601','','凯里市','kai li shi');
_R3(0,23,4,'522632','','榕江县','rong jiang xian');
_R3(0,23,4,'522633','','从江县','cong jiang xian');
_R3(0,23,4,'522634','','雷山县','lei shan xian');
_R3(0,23,4,'522635','','麻江县','ma jiang xian');
_R3(0,23,4,'522636','','丹寨县','dan zhai xian');
_R3(0,23,4,'522637','','其它区','qi ta qu');
_R3(0,23,4,'522629','','剑河县','jian he xian');
_R3(0,23,4,'522622','','黄平县','huang ping xian');
_R3(0,23,4,'522623','','施秉县','shi bing xian');
_R3(0,23,4,'522624','','三穗县','san sui xian');
_R3(0,23,4,'522625','','镇远县','zhen yuan xian');
_R3(0,23,4,'522626','','岑巩县','cen gong xian');
_R3(0,23,4,'522627','','天柱县','tian zhu xian');
_R3(0,23,4,'522628','','锦屏县','jin ping xian');
_R3(0,23,4,'522630','','台江县','tai jiang xian');
_R3(0,23,4,'522631','','黎平县','li ping xian');
_R2(0,23,'522700','','黔南布依族苗族自治州','qian nan bu yi zu miao zu zi zhi zhou');
_R3(0,23,5,'522728','','罗甸县','luo dian xian');
_R3(0,23,5,'522729','','长顺县','chang shun xian');
_R3(0,23,5,'522722','','荔波县','li bo xian');
_R3(0,23,5,'522723','','贵定县','gui ding xian');
_R3(0,23,5,'522725','','瓮安县','weng an xian');
_R3(0,23,5,'522726','','独山县','du shan xian');
_R3(0,23,5,'522727','','平塘县','ping tang xian');
_R3(0,23,5,'522730','','龙里县','long li xian');
_R3(0,23,5,'522701','','都匀市','du yun shi');
_R3(0,23,5,'522702','','福泉市','fu quan shi');
_R3(0,23,5,'522731','','惠水县','hui shui xian');
_R3(0,23,5,'522732','','三都水族自治县','san du shui zu zi zhi xian');
_R3(0,23,5,'522733','','其它区','qi ta qu');
_R2(0,23,'522300','','黔西南布依族苗族自治州','qian xi nan bu yi zu miao zu zi zhi zhou');
_R3(0,23,6,'522322','','兴仁市','xing ren shi');
_R3(0,23,6,'522323','','普安县','pu an xian');
_R3(0,23,6,'522324','','晴隆县','qing long xian');
_R3(0,23,6,'522325','','贞丰县','zhen feng xian');
_R3(0,23,6,'522326','','望谟县','wang mo xian');
_R3(0,23,6,'522327','','册亨县','ce heng xian');
_R3(0,23,6,'522328','','安龙县','an long xian');
_R3(0,23,6,'522329','','其它区','qi ta qu');
_R3(0,23,6,'522301','','兴义市','xing yi shi');
_R2(0,23,'522200','','铜仁市','tong ren shi');
_R3(0,23,7,'522201','','碧江区','bi jiang qu');
_R3(0,23,7,'522222','','江口县','jiang kou xian');
_R3(0,23,7,'522223','','玉屏侗族自治县','yu ping dong zu zi zhi xian');
_R3(0,23,7,'522224','','石阡县','shi qian xian');
_R3(0,23,7,'522225','','思南县','si nan xian');
_R3(0,23,7,'522226','','印江土家族苗族自治县','yin jiang tu jia zu miao zu zi zhi xian');
_R3(0,23,7,'522227','','德江县','de jiang xian');
_R3(0,23,7,'522228','','沿河土家族自治县','yan he tu jia zu zi zhi xian');
_R3(0,23,7,'522229','','松桃苗族自治县','song tao miao zu zi zhi xian');
_R3(0,23,7,'522230','','万山区','wan shan qu');
_R3(0,23,7,'522231','','其它区','qi ta qu');
_R2(0,23,'520300','','遵义市','zun yi shi');
_R3(0,23,8,'520381','','赤水市','chi shui shi');
_R3(0,23,8,'520382','','仁怀市','ren huai shi');
_R3(0,23,8,'520383','','其它区','qi ta qu');
_R3(0,23,8,'520302','','红花岗区','hong hua gang qu');
_R3(0,23,8,'520303','','汇川区','hui chuan qu');
_R3(0,23,8,'520321','','播州区','bo zhou qu');
_R3(0,23,8,'520322','','桐梓县','tong zi xian');
_R3(0,23,8,'520323','','绥阳县','sui yang xian');
_R3(0,23,8,'520324','','正安县','zheng an xian');
_R3(0,23,8,'520325','','道真仡佬族苗族自治县','dao zhen ge lao zu miao zu zi zhi xian');
_R3(0,23,8,'520326','','务川仡佬族苗族自治县','wu chuan ge lao zu miao zu zi zhi xian');
_R3(0,23,8,'520327','','凤冈县','feng gang xian');
_R3(0,23,8,'520328','','湄潭县','mei tan xian');
_R3(0,23,8,'520329','','余庆县','yu qing xian');
_R3(0,23,8,'520330','','习水县','xi shui xian');

_R1(0,'530000','','云南省','yun nan sheng','云南省');
_R2(0,24,'530500','','保山市','bao shan shi');
_R3(0,24,0,'530502','','隆阳区','long yang qu');
_R3(0,24,0,'530525','','其它区','qi ta qu');
_R3(0,24,0,'530523','','龙陵县','long ling xian');
_R3(0,24,0,'530524','','昌宁县','chang ning xian');
_R3(0,24,0,'530521','','施甸县','shi dian xian');
_R3(0,24,0,'530522','','腾冲市','teng chong shi');
_R2(0,24,'532300','','楚雄彝族自治州','chu xiong yi zu zi zhi zhou');
_R3(0,24,1,'532322','','双柏县','shuang bai xian');
_R3(0,24,1,'532332','','其它区','qi ta qu');
_R3(0,24,1,'532331','','禄丰市','lu feng shi');
_R3(0,24,1,'532329','','武定县','wu ding xian');
_R3(0,24,1,'532328','','元谋县','yuan mou xian');
_R3(0,24,1,'532327','','永仁县','yong ren xian');
_R3(0,24,1,'532326','','大姚县','da yao xian');
_R3(0,24,1,'532325','','姚安县','yao an xian');
_R3(0,24,1,'532324','','南华县','nan hua xian');
_R3(0,24,1,'532323','','牟定县','mou ding xian');
_R3(0,24,1,'532301','','楚雄市','chu xiong shi');
_R2(0,24,'532900','','大理白族自治州','da li bai zu zi zhi zhou');
_R3(0,24,2,'532924','','宾川县','bin chuan xian');
_R3(0,24,2,'532923','','祥云县','xiang yun xian');
_R3(0,24,2,'532922','','漾濞彝族自治县','yang bi yi zu zi zhi xian');
_R3(0,24,2,'532929','','云龙县','yun long xian');
_R3(0,24,2,'532928','','永平县','yong ping xian');
_R3(0,24,2,'532927','','巍山彝族回族自治县','wei shan yi zu hui zu zi zhi xian');
_R3(0,24,2,'532926','','南涧彝族自治县','nan jian yi zu zi zhi xian');
_R3(0,24,2,'532925','','弥渡县','mi du xian');
_R3(0,24,2,'532901','','大理市','da li shi');
_R3(0,24,2,'532933','','其它区','qi ta qu');
_R3(0,24,2,'532932','','鹤庆县','he qing xian');
_R3(0,24,2,'532931','','剑川县','jian chuan xian');
_R3(0,24,2,'532930','','洱源县','er yuan xian');
_R2(0,24,'533100','','德宏傣族景颇族自治州','de hong dai zu jing po zu zi zhi zhou');
_R3(0,24,3,'533103','','芒市','mang shi');
_R3(0,24,3,'533102','','瑞丽市','rui li shi');
_R3(0,24,3,'533125','','其它区','qi ta qu');
_R3(0,24,3,'533124','','陇川县','long chuan xian');
_R3(0,24,3,'533123','','盈江县','ying jiang xian');
_R3(0,24,3,'533122','','梁河县','liang he xian');
_R2(0,24,'533400','','迪庆藏族自治州','di qing zang zu zi zhi zhou');
_R3(0,24,4,'533422','','德钦县','de qin xian');
_R3(0,24,4,'533421','','香格里拉市','xiang ge li la shi');
_R3(0,24,4,'533424','','其它区','qi ta qu');
_R3(0,24,4,'533423','','维西傈僳族自治县','wei xi li su zu zi zhi xian');
_R2(0,24,'532500','','红河哈尼族彝族自治州','hong he ha ni zu yi zu zi zhi zhou');
_R3(0,24,5,'532531','','绿春县','lv chun xian');
_R3(0,24,5,'532530','','金平苗族瑶族傣族自治县','jin ping miao zu yao zu dai zu zi zhi xian');
_R3(0,24,5,'532528','','元阳县','yuan yang xian');
_R3(0,24,5,'532527','','泸西县','lu xi xian');
_R3(0,24,5,'532526','','弥勒市','mi le shi');
_R3(0,24,5,'532525','','石屏县','shi ping xian');
_R3(0,24,5,'532524','','建水县','jian shui xian');
_R3(0,24,5,'532523','','屏边苗族自治县','ping bian miao zu zi zhi xian');
_R3(0,24,5,'532522','','蒙自市','meng zi shi');
_R3(0,24,5,'532529','','红河县','hong he xian');
_R3(0,24,5,'532502','','开远市','kai yuan shi');
_R3(0,24,5,'532501','','个旧市','ge jiu shi');
_R3(0,24,5,'532533','','其它区','qi ta qu');
_R3(0,24,5,'532532','','河口瑶族自治县','he kou yao zu zi zhi xian');
_R2(0,24,'530100','','昆明市','kun ming shi');
_R3(0,24,6,'530181','','安宁市','an ning shi');
_R3(0,24,6,'530182','','其它区','qi ta qu');
_R3(0,24,6,'530111','','官渡区','guan du qu');
_R3(0,24,6,'530103','','盘龙区','pan long qu');
_R3(0,24,6,'530102','','五华区','wu hua qu');
_R3(0,24,6,'530121','','呈贡区','cheng gong qu');
_R3(0,24,6,'530122','','晋宁区','jin ning qu');
_R3(0,24,6,'530112','','西山区','xi shan qu');
_R3(0,24,6,'530113','','东川区','dong chuan qu');
_R3(0,24,6,'530129','','寻甸回族彝族自治县','xun dian hui zu yi zu zi zhi xian');
_R3(0,24,6,'530127','','嵩明县','song ming xian');
_R3(0,24,6,'530128','','禄劝彝族苗族自治县','lu quan yi zu miao zu zi zhi xian');
_R3(0,24,6,'530125','','宜良县','yi liang xian');
_R3(0,24,6,'530126','','石林彝族自治县','shi lin yi zu zi zhi xian');
_R3(0,24,6,'530124','','富民县','fu min xian');
_R2(0,24,'530700','','丽江市','li jiang shi');
_R3(0,24,7,'530702','','古城区','gu cheng qu');
_R3(0,24,7,'530723','','华坪县','hua ping xian');
_R3(0,24,7,'530724','','宁蒗彝族自治县','ning lang yi zu zi zhi xian');
_R3(0,24,7,'530721','','玉龙纳西族自治县','yu long na xi zu zi zhi xian');
_R3(0,24,7,'530722','','永胜县','yong sheng xian');
_R3(0,24,7,'530725','','其它区','qi ta qu');
_R2(0,24,'530900','','临沧市','lin cang shi');
_R3(0,24,8,'530902','','临翔区','lin xiang qu');
_R3(0,24,8,'530921','','凤庆县','feng qing xian');
_R3(0,24,8,'530922','','云县','yun xian');
_R3(0,24,8,'530927','','沧源佤族自治县','cang yuan wa zu zi zhi xian');
_R3(0,24,8,'530928','','其它区','qi ta qu');
_R3(0,24,8,'530925','','双江拉祜族佤族布朗族傣族自治县','shuang jiang la hu zu wa zu bu lang zu dai zu zi zhi xian');
_R3(0,24,8,'530926','','耿马傣族佤族自治县','geng ma dai zu wa zu zi zhi xian');
_R3(0,24,8,'530923','','永德县','yong de xian');
_R3(0,24,8,'530924','','镇康县','zhen kang xian');
_R2(0,24,'533300','','怒江傈僳族自治州','nu jiang li su zu zi zhi zhou');
_R3(0,24,9,'533326','','其它区','qi ta qu');
_R3(0,24,9,'533325','','兰坪白族普米族自治县','lan ping bai zu pu mi zu zi zhi xian');
_R3(0,24,9,'533324','','贡山独龙族怒族自治县','gong shan du long zu nu zu zi zhi xian');
_R3(0,24,9,'533323','','福贡县','fu gong xian');
_R3(0,24,9,'533321','','泸水市','lu shui shi');
_R2(0,24,'530800','','普洱市','pu er shi');
_R3(0,24,10,'530822','','墨江哈尼族自治县','mo jiang ha ni zu zi zhi xian');
_R3(0,24,10,'530823','','景东彝族自治县','jing dong yi zu zi zhi xian');
_R3(0,24,10,'530821','','宁洱哈尼族彝族自治县','ning er ha ni zu yi zu zi zhi xian');
_R3(0,24,10,'530828','','澜沧拉祜族自治县','lan cang la hu zu zi zhi xian');
_R3(0,24,10,'530829','','西盟佤族自治县','xi meng wa zu zi zhi xian');
_R3(0,24,10,'530826','','江城哈尼族彝族自治县','jiang cheng ha ni zu yi zu zi zhi xian');
_R3(0,24,10,'530827','','孟连傣族拉祜族佤族自治县','meng lian dai zu la hu zu wa zu zi zhi xian');
_R3(0,24,10,'530824','','景谷傣族彝族自治县','jing gu dai zu yi zu zi zhi xian');
_R3(0,24,10,'530825','','镇沅彝族哈尼族拉祜族自治县','zhen yuan yi zu ha ni zu la hu zu zi zhi xian');
_R3(0,24,10,'530802','','思茅区','si mao qu');
_R3(0,24,10,'530830','','其它区','qi ta qu');
_R2(0,24,'530300','','曲靖市','qu jing shi');
_R3(0,24,11,'530328','','沾益区','zhan yi qu');
_R3(0,24,11,'530325','','富源县','fu yuan xian');
_R3(0,24,11,'530326','','会泽县','hui ze xian');
_R3(0,24,11,'530323','','师宗县','shi zong xian');
_R3(0,24,11,'530324','','罗平县','luo ping xian');
_R3(0,24,11,'530321','','马龙区','ma long qu');
_R3(0,24,11,'530322','','陆良县','lu liang xian');
_R3(0,24,11,'530381','','宣威市','xuan wei shi');
_R3(0,24,11,'530382','','其它区','qi ta qu');
_R3(0,24,11,'530302','','麒麟区','qi lin qu');
_R2(0,24,'532600','','文山壮族苗族自治州','wen shan zhuang zu miao zu zi zhi zhou');
_R3(0,24,12,'532627','','广南县','guang nan xian');
_R3(0,24,12,'532626','','丘北县','qiu bei xian');
_R3(0,24,12,'532625','','马关县','ma guan xian');
_R3(0,24,12,'532624','','麻栗坡县','ma li po xian');
_R3(0,24,12,'532623','','西畴县','xi chou xian');
_R3(0,24,12,'532622','','砚山县','yan shan xian');
_R3(0,24,12,'532621','','文山市','wen shan shi');
_R3(0,24,12,'532629','','其它区','qi ta qu');
_R3(0,24,12,'532628','','富宁县','fu ning xian');
_R2(0,24,'532800','','西双版纳傣族自治州','xi shuang ban na dai zu zi zhi zhou');
_R3(0,24,13,'532801','','景洪市','jing hong shi');
_R3(0,24,13,'532824','','其它区','qi ta qu');
_R3(0,24,13,'532823','','勐腊县','meng la xian');
_R3(0,24,13,'532822','','勐海县','meng hai xian');
_R2(0,24,'530400','','玉溪市','yu xi shi');
_R3(0,24,14,'530426','','峨山彝族自治县','e shan yi zu zi zhi xian');
_R3(0,24,14,'530427','','新平彝族傣族自治县','xin ping yi zu dai zu zi zhi xian');
_R3(0,24,14,'530424','','华宁县','hua ning xian');
_R3(0,24,14,'530425','','易门县','yi men xian');
_R3(0,24,14,'530422','','澄江市','cheng jiang shi');
_R3(0,24,14,'530423','','通海县','tong hai xian');
_R3(0,24,14,'530421','','江川区','jiang chuan qu');
_R3(0,24,14,'530428','','元江哈尼族彝族傣族自治县','yuan jiang ha ni zu yi zu dai zu zi zhi xian');
_R3(0,24,14,'530429','','其它区','qi ta qu');
_R3(0,24,14,'530402','','红塔区','hong ta qu');
_R2(0,24,'530600','','昭通市','zhao tong shi');
_R3(0,24,15,'530624','','大关县','da guan xian');
_R3(0,24,15,'530625','','永善县','yong shan xian');
_R3(0,24,15,'530622','','巧家县','qiao jia xian');
_R3(0,24,15,'530623','','盐津县','yan jin xian');
_R3(0,24,15,'530621','','鲁甸县','lu dian xian');
_R3(0,24,15,'530628','','彝良县','yi liang xian');
_R3(0,24,15,'530629','','威信县','wei xin xian');
_R3(0,24,15,'530626','','绥江县','sui jiang xian');
_R3(0,24,15,'530627','','镇雄县','zhen xiong xian');
_R3(0,24,15,'530602','','昭阳区','zhao yang qu');
_R3(0,24,15,'530631','','其它区','qi ta qu');
_R3(0,24,15,'530630','','水富市','shui fu shi');

_R1(0,'540000','','西藏','xi zang zi zhi qu','西藏自治区');
_R2(0,25,'542500','','阿里地区','a li di qu');
_R3(0,25,0,'542525','','革吉县','ge ji xian');
_R3(0,25,0,'542524','','日土县','ri tu xian');
_R3(0,25,0,'542527','','措勤县','cuo qin xian');
_R3(0,25,0,'542526','','改则县','gai ze xian');
_R3(0,25,0,'542521','','普兰县','pu lan xian');
_R3(0,25,0,'542523','','噶尔县','ga er xian');
_R3(0,25,0,'542522','','札达县','zha da xian');
_R3(0,25,0,'542528','','其它区','qi ta qu');
_R2(0,25,'542100','','昌都市','chang du shi');
_R3(0,25,1,'542129','','芒康县','mang kang xian');
_R3(0,25,1,'542128','','左贡县','zuo gong xian');
_R3(0,25,1,'542125','','丁青县','ding qing xian');
_R3(0,25,1,'542124','','类乌齐县','lei wu qi xian');
_R3(0,25,1,'542127','','八宿县','ba su xian');
_R3(0,25,1,'542126','','察雅县','cha ya xian');
_R3(0,25,1,'542132','','洛隆县','luo long xian');
_R3(0,25,1,'542134','','其它区','qi ta qu');
_R3(0,25,1,'542133','','边坝县','bian ba xian');
_R3(0,25,1,'542121','','卡若区','ka ruo qu');
_R3(0,25,1,'542123','','贡觉县','gong jue xian');
_R3(0,25,1,'542122','','江达县','jiang da xian');
_R2(0,25,'540100','','拉萨市','la sa shi');
_R3(0,25,2,'540126','','达孜区','da zi qu');
_R3(0,25,2,'540127','','墨竹工卡县','mo zhu gong ka xian');
_R3(0,25,2,'540128','','其它区','qi ta qu');
_R3(0,25,2,'540122','','当雄县','dang xiong xian');
_R3(0,25,2,'540123','','尼木县','ni mu xian');
_R3(0,25,2,'540124','','曲水县','qu shui xian');
_R3(0,25,2,'540125','','堆龙德庆区','dui long de qing qu');
_R3(0,25,2,'540121','','林周县','lin zhou xian');
_R3(0,25,2,'540102','','城关区','cheng guan qu');
_R2(0,25,'542600','','林芝市','lin zhi shi');
_R3(0,25,3,'542624','','墨脱县','mo tuo xian');
_R3(0,25,3,'542623','','米林县','mi lin xian');
_R3(0,25,3,'542626','','察隅县','cha yu xian');
_R3(0,25,3,'542625','','波密县','bo mi xian');
_R3(0,25,3,'542622','','工布江达县','gong bu jiang da xian');
_R3(0,25,3,'542621','','巴宜区','ba yi qu');
_R3(0,25,3,'542628','','其它区','qi ta qu');
_R3(0,25,3,'542627','','朗县','lang xian');
_R2(0,25,'542400','','那曲市','na qu shi');
_R3(0,25,4,'542432','','双湖县','shuang hu xian');
_R3(0,25,4,'542426','','申扎县','shen zha xian');
_R3(0,25,4,'542425','','安多县','an duo xian');
_R3(0,25,4,'542428','','班戈县','ban ge xian');
_R3(0,25,4,'542427','','索县','suo xian');
_R3(0,25,4,'542422','','嘉黎县','jia li xian');
_R3(0,25,4,'542421','','色尼区','se ni qu');
_R3(0,25,4,'542424','','聂荣县','nie rong xian');
_R3(0,25,4,'542423','','比如县','bi ru xian');
_R3(0,25,4,'542429','','巴青县','ba qing xian');
_R3(0,25,4,'542431','','其它区','qi ta qu');
_R3(0,25,4,'542430','','尼玛县','ni ma xian');
_R2(0,25,'542300','','日喀则市','ri ka ze shi');
_R3(0,25,5,'542327','','昂仁县','ang ren xian');
_R3(0,25,5,'542326','','拉孜县','la zi xian');
_R3(0,25,5,'542329','','白朗县','bai lang xian');
_R3(0,25,5,'542328','','谢通门县','xie tong men xian');
_R3(0,25,5,'542323','','江孜县','jiang zi xian');
_R3(0,25,5,'542322','','南木林县','nan mu lin xian');
_R3(0,25,5,'542325','','萨迦县','sa jia xian');
_R3(0,25,5,'542324','','定日县','ding ri xian');
_R3(0,25,5,'542330','','仁布县','ren bu xian');
_R3(0,25,5,'542332','','定结县','ding jie xian');
_R3(0,25,5,'542331','','康马县','kang ma xian');
_R3(0,25,5,'542301','','桑珠孜区','sang zhu zi qu');
_R3(0,25,5,'542338','','岗巴县','gang ba xian');
_R3(0,25,5,'542337','','萨嘎县','sa ga xian');
_R3(0,25,5,'542339','','其它区','qi ta qu');
_R3(0,25,5,'542334','','亚东县','ya dong xian');
_R3(0,25,5,'542333','','仲巴县','zhong ba xian');
_R3(0,25,5,'542336','','聂拉木县','nie la mu xian');
_R3(0,25,5,'542335','','吉隆县','ji long xian');
_R2(0,25,'542200','','山南市','shan nan shi');
_R3(0,25,6,'542234','','其它区','qi ta qu');
_R3(0,25,6,'542228','','洛扎县','luo zha xian');
_R3(0,25,6,'542227','','措美县','cuo mei xian');
_R3(0,25,6,'542229','','加查县','jia cha xian');
_R3(0,25,6,'542224','','桑日县','sang ri xian');
_R3(0,25,6,'542223','','贡嘎县','gong ga xian');
_R3(0,25,6,'542226','','曲松县','qu song xian');
_R3(0,25,6,'542225','','琼结县','qiong jie xian');
_R3(0,25,6,'542231','','隆子县','long zi xian');
_R3(0,25,6,'542233','','浪卡子县','lang ka zi xian');
_R3(0,25,6,'542232','','错那县','cuo na xian');
_R3(0,25,6,'542222','','扎囊县','zha nang xian');
_R3(0,25,6,'542221','','乃东区','nai dong qu');

_R1(0,'610000','','陕西省','shan xi sheng','陕西省');
_R2(0,26,'610900','','安康市','an kang shi');
_R3(0,26,0,'610930','','其它区','qi ta qu');
_R3(0,26,0,'610928','','旬阳市','xun yang shi');
_R3(0,26,0,'610929','','白河县','bai he xian');
_R3(0,26,0,'610926','','平利县','ping li xian');
_R3(0,26,0,'610927','','镇坪县','zhen ping xian');
_R3(0,26,0,'610924','','紫阳县','zi yang xian');
_R3(0,26,0,'610925','','岚皋县','lan gao xian');
_R3(0,26,0,'610922','','石泉县','shi quan xian');
_R3(0,26,0,'610923','','宁陕县','ning shan xian');
_R3(0,26,0,'610921','','汉阴县','han yin xian');
_R3(0,26,0,'610902','','汉滨区','han bin qu');
_R2(0,26,'610300','','宝鸡市','bao ji shi');
_R3(0,26,1,'610328','','千阳县','qian yang xian');
_R3(0,26,1,'610329','','麟游县','lin you xian');
_R3(0,26,1,'610326','','眉县','mei xian');
_R3(0,26,1,'610327','','陇县','long xian');
_R3(0,26,1,'610324','','扶风县','fu feng xian');
_R3(0,26,1,'610331','','太白县','tai bai xian');
_R3(0,26,1,'610332','','其它区','qi ta qu');
_R3(0,26,1,'610330','','凤县','feng xian');
_R3(0,26,1,'610322','','凤翔区','feng xiang qu');
_R3(0,26,1,'610323','','岐山县','qi shan xian');
_R3(0,26,1,'610304','','陈仓区','chen cang qu');
_R3(0,26,1,'610302','','渭滨区','wei bin qu');
_R3(0,26,1,'610303','','金台区','jin tai qu');
_R2(0,26,'610700','','汉中市','han zhong shi');
_R3(0,26,2,'610731','','其它区','qi ta qu');
_R3(0,26,2,'610728','','镇巴县','zhen ba xian');
_R3(0,26,2,'610729','','留坝县','liu ba xian');
_R3(0,26,2,'610726','','宁强县','ning qiang xian');
_R3(0,26,2,'610727','','略阳县','lve yang xian');
_R3(0,26,2,'610724','','西乡县','xi xiang xian');
_R3(0,26,2,'610725','','勉县','mian xian');
_R3(0,26,2,'610722','','城固县','cheng gu xian');
_R3(0,26,2,'610723','','洋县','yang xian');
_R3(0,26,2,'610721','','南郑区','nan zheng qu');
_R3(0,26,2,'610730','','佛坪县','fo ping xian');
_R3(0,26,2,'610702','','汉台区','han tai qu');
_R2(0,26,'611000','','商洛市','shang luo shi');
_R3(0,26,3,'611027','','其它区','qi ta qu');
_R3(0,26,3,'611026','','柞水县','zha shui xian');
_R3(0,26,3,'611025','','镇安县','zhen an xian');
_R3(0,26,3,'611024','','山阳县','shan yang xian');
_R3(0,26,3,'611023','','商南县','shang nan xian');
_R3(0,26,3,'611022','','丹凤县','dan feng xian');
_R3(0,26,3,'611021','','洛南县','luo nan xian');
_R3(0,26,3,'611002','','商州区','shang zhou qu');
_R2(0,26,'610200','','铜川市','tong chuan shi');
_R3(0,26,4,'610223','','其它区','qi ta qu');
_R3(0,26,4,'610222','','宜君县','yi jun xian');
_R3(0,26,4,'610203','','印台区','yin tai qu');
_R3(0,26,4,'610204','','耀州区','yao zhou qu');
_R3(0,26,4,'610202','','王益区','wang yi qu');
_R2(0,26,'610500','','渭南市','wei nan shi');
_R3(0,26,5,'610582','','华阴市','hua yin shi');
_R3(0,26,5,'610583','','其它区','qi ta qu');
_R3(0,26,5,'610581','','韩城市','han cheng shi');
_R3(0,26,5,'610528','','富平县','fu ping xian');
_R3(0,26,5,'610526','','蒲城县','pu cheng xian');
_R3(0,26,5,'610527','','白水县','bai shui xian');
_R3(0,26,5,'610524','','合阳县','he yang xian');
_R3(0,26,5,'610525','','澄城县','cheng cheng xian');
_R3(0,26,5,'610522','','潼关县','tong guan xian');
_R3(0,26,5,'610523','','大荔县','da li xian');
_R3(0,26,5,'610521','','华州区','hua zhou qu');
_R3(0,26,5,'610502','','临渭区','lin wei qu');
_R2(0,26,'610100','','西安市','xi an shi');
_R3(0,26,6,'610102','','新城区','xin cheng qu');
_R3(0,26,6,'610103','','碑林区','bei lin qu');
_R3(0,26,6,'610126','','高陵区','gao ling qu');
_R3(0,26,6,'610127','','其它区','qi ta qu');
_R3(0,26,6,'610115','','临潼区','lin tong qu');
_R3(0,26,6,'610116','','长安区','chang an qu');
_R3(0,26,6,'610124','','周至县','zhou zhi xian');
_R3(0,26,6,'610125','','鄠邑区','hu yi qu');
_R3(0,26,6,'610122','','蓝田县','lan tian xian');
_R3(0,26,6,'610104','','莲湖区','lian hu qu');
_R3(0,26,6,'610113','','雁塔区','yan ta qu');
_R3(0,26,6,'610114','','阎良区','yan liang qu');
_R3(0,26,6,'610111','','灞桥区','ba qiao qu');
_R3(0,26,6,'610112','','未央区','wei yang qu');
_R2(0,26,'610400','','咸阳市','xian yang shi');
_R3(0,26,7,'610429','','旬邑县','xun yi xian');
_R3(0,26,7,'610427','','彬州市','bin zhou shi');
_R3(0,26,7,'610428','','长武县','chang wu xian');
_R3(0,26,7,'610425','','礼泉县','li quan xian');
_R3(0,26,7,'610426','','永寿县','yong shou xian');
_R3(0,26,7,'610423','','泾阳县','jing yang xian');
_R3(0,26,7,'610424','','乾县','qian xian');
_R3(0,26,7,'610430','','淳化县','chun hua xian');
_R3(0,26,7,'610431','','武功县','wu gong xian');
_R3(0,26,7,'610422','','三原县','san yuan xian');
_R3(0,26,7,'610403','','杨陵区','yang ling qu');
_R3(0,26,7,'610404','','渭城区','wei cheng qu');
_R3(0,26,7,'610402','','秦都区','qin du qu');
_R3(0,26,7,'610481','','兴平市','xing ping shi');
_R3(0,26,7,'610482','','其它区','qi ta qu');
_R2(0,26,'610600','','延安市','yan an shi');
_R3(0,26,8,'610632','','黄陵县','huang ling xian');
_R3(0,26,8,'610633','','其它区','qi ta qu');
_R3(0,26,8,'610629','','洛川县','luo chuan xian');
_R3(0,26,8,'610627','','甘泉县','gan quan xian');
_R3(0,26,8,'610628','','富县','fu xian');
_R3(0,26,8,'610625','','志丹县','zhi dan xian');
_R3(0,26,8,'610626','','吴起县','wu qi xian');
_R3(0,26,8,'610623','','子长市','zi chang shi');
_R3(0,26,8,'610624','','安塞区','an sai qu');
_R3(0,26,8,'610621','','延长县','yan chang xian');
_R3(0,26,8,'610622','','延川县','yan chuan xian');
_R3(0,26,8,'610630','','宜川县','yi chuan xian');
_R3(0,26,8,'610631','','黄龙县','huang long xian');
_R3(0,26,8,'610602','','宝塔区','bao ta qu');
_R2(0,26,'610800','','榆林市','yu lin shi');
_R3(0,26,9,'610832','','其它区','qi ta qu');
_R3(0,26,9,'610830','','清涧县','qing jian xian');
_R3(0,26,9,'610831','','子洲县','zi zhou xian');
_R3(0,26,9,'610829','','吴堡县','wu bu xian');
_R3(0,26,9,'610827','','米脂县','mi zhi xian');
_R3(0,26,9,'610828','','佳县','jia xian');
_R3(0,26,9,'610825','','定边县','ding bian xian');
_R3(0,26,9,'610826','','绥德县','sui de xian');
_R3(0,26,9,'610823','','横山区','heng shan qu');
_R3(0,26,9,'610824','','靖边县','jing bian xian');
_R3(0,26,9,'610821','','神木市','shen mu shi');
_R3(0,26,9,'610822','','府谷县','fu gu xian');
_R3(0,26,9,'610802','','榆阳区','yu yang qu');

_R1(0,'620000','','甘肃省','gan su sheng','甘肃省');
_R2(0,27,'620400','','白银市','bai yin shi');
_R3(0,27,0,'620422','','会宁县','hui ning xian');
_R3(0,27,0,'620423','','景泰县','jing tai xian');
_R3(0,27,0,'620424','','其它区','qi ta qu');
_R3(0,27,0,'620421','','靖远县','jing yuan xian');
_R3(0,27,0,'620402','','白银区','bai yin qu');
_R3(0,27,0,'620403','','平川区','ping chuan qu');
_R2(0,27,'621100','','定西市','ding xi shi');
_R3(0,27,1,'621102','','安定区','an ding qu');
_R3(0,27,1,'621127','','其它区','qi ta qu');
_R3(0,27,1,'621126','','岷县','min xian');
_R3(0,27,1,'621123','','渭源县','wei yuan xian');
_R3(0,27,1,'621122','','陇西县','long xi xian');
_R3(0,27,1,'621125','','漳县','zhang xian');
_R3(0,27,1,'621124','','临洮县','lin tao xian');
_R3(0,27,1,'621121','','通渭县','tong wei xian');
_R2(0,27,'623000','','甘南藏族自治州','gan nan zang zu zi zhi zhou');
_R3(0,27,2,'623027','','夏河县','xia he xian');
_R3(0,27,2,'623028','','其它区','qi ta qu');
_R3(0,27,2,'623025','','玛曲县','ma qu xian');
_R3(0,27,2,'623026','','碌曲县','lu qu xian');
_R3(0,27,2,'623023','','舟曲县','zhou qu xian');
_R3(0,27,2,'623024','','迭部县','die bu xian');
_R3(0,27,2,'623021','','临潭县','lin tan xian');
_R3(0,27,2,'623022','','卓尼县','zhuo ni xian');
_R3(0,27,2,'623001','','合作市','he zuo shi');
_R2(0,27,'620200','','嘉峪关市','jia yu guan shi');
_R2(0,27,'620300','','金昌市','jin chang shi');
_R3(0,27,4,'620321','','永昌县','yong chang xian');
_R3(0,27,4,'620322','','其它区','qi ta qu');
_R3(0,27,4,'620302','','金川区','jin chuan qu');
_R2(0,27,'620900','','酒泉市','jiu quan shi');
_R3(0,27,5,'620921','','金塔县','jin ta xian');
_R3(0,27,5,'620922','','瓜州县','gua zhou xian');
_R3(0,27,5,'620923','','肃北蒙古族自治县','su bei meng gu zu zi zhi xian');
_R3(0,27,5,'620924','','阿克塞哈萨克族自治县','a ke sai ha sa ke zu zi zhi xian');
_R3(0,27,5,'620902','','肃州区','su zhou qu');
_R3(0,27,5,'620983','','其它区','qi ta qu');
_R3(0,27,5,'620981','','玉门市','yu men shi');
_R3(0,27,5,'620982','','敦煌市','dun huang shi');
_R2(0,27,'620100','','兰州市','lan zhou shi');
_R3(0,27,6,'620121','','永登县','yong deng xian');
_R3(0,27,6,'620122','','皋兰县','gao lan xian');
_R3(0,27,6,'620123','','榆中县','yu zhong xian');
_R3(0,27,6,'620124','','其它区','qi ta qu');
_R3(0,27,6,'620111','','红古区','hong gu qu');
_R3(0,27,6,'620103','','七里河区','qi li he qu');
_R3(0,27,6,'620104','','西固区','xi gu qu');
_R3(0,27,6,'620105','','安宁区','an ning qu');
_R3(0,27,6,'620102','','城关区','cheng guan qu');
_R2(0,27,'622900','','临夏回族自治州','lin xia hui zu zi zhi zhou');
_R3(0,27,7,'622928','','其它区','qi ta qu');
_R3(0,27,7,'622927','','积石山保安族东乡族撒拉族自治县','ji shi shan bao an zu dong xiang zu sa la zu zi zhi xian');
_R3(0,27,7,'622924','','广河县','guang he xian');
_R3(0,27,7,'622923','','永靖县','yong jing xian');
_R3(0,27,7,'622926','','东乡族自治县','dong xiang zu zi zhi xian');
_R3(0,27,7,'622925','','和政县','he zheng xian');
_R3(0,27,7,'622922','','康乐县','kang le xian');
_R3(0,27,7,'622921','','临夏县','lin xia xian');
_R3(0,27,7,'622901','','临夏市','lin xia shi');
_R2(0,27,'621200','','陇南市','long nan shi');
_R3(0,27,8,'621229','','其它区','qi ta qu');
_R3(0,27,8,'621226','','礼县','li xian');
_R3(0,27,8,'621225','','西和县','xi he xian');
_R3(0,27,8,'621228','','两当县','liang dang xian');
_R3(0,27,8,'621227','','徽县','hui xian');
_R3(0,27,8,'621222','','文县','wen xian');
_R3(0,27,8,'621221','','成县','cheng xian');
_R3(0,27,8,'621224','','康县','kang xian');
_R3(0,27,8,'621223','','宕昌县','dang chang xian');
_R3(0,27,8,'621202','','武都区','wu du qu');
_R2(0,27,'620800','','平凉市','ping liang shi');
_R3(0,27,9,'620802','','崆峒区','kong tong qu');
_R3(0,27,9,'620826','','静宁县','jing ning xian');
_R3(0,27,9,'620827','','其它区','qi ta qu');
_R3(0,27,9,'620822','','灵台县','ling tai xian');
_R3(0,27,9,'620823','','崇信县','chong xin xian');
_R3(0,27,9,'620824','','华亭市','hua ting shi');
_R3(0,27,9,'620825','','庄浪县','zhuang lang xian');
_R3(0,27,9,'620821','','泾川县','jing chuan xian');
_R2(0,27,'621000','','庆阳市','qing yang shi');
_R3(0,27,10,'621028','','其它区','qi ta qu');
_R3(0,27,10,'621027','','镇原县','zhen yuan xian');
_R3(0,27,10,'621024','','合水县','he shui xian');
_R3(0,27,10,'621023','','华池县','hua chi xian');
_R3(0,27,10,'621026','','宁县','ning xian');
_R3(0,27,10,'621025','','正宁县','zheng ning xian');
_R3(0,27,10,'621022','','环县','huan xian');
_R3(0,27,10,'621021','','庆城县','qing cheng xian');
_R3(0,27,10,'621002','','西峰区','xi feng qu');
_R2(0,27,'620500','','天水市','tian shui shi');
_R3(0,27,11,'620525','','张家川回族自治县','zhang jia chuan hui zu zi zhi xian');
_R3(0,27,11,'620526','','其它区','qi ta qu');
_R3(0,27,11,'620521','','清水县','qing shui xian');
_R3(0,27,11,'620522','','秦安县','qin an xian');
_R3(0,27,11,'620523','','甘谷县','gan gu xian');
_R3(0,27,11,'620524','','武山县','wu shan xian');
_R3(0,27,11,'620503','','麦积区','mai ji qu');
_R3(0,27,11,'620502','','秦州区','qin zhou qu');
_R2(0,27,'620600','','武威市','wu wei shi');
_R3(0,27,12,'620624','','其它区','qi ta qu');
_R3(0,27,12,'620621','','民勤县','min qin xian');
_R3(0,27,12,'620622','','古浪县','gu lang xian');
_R3(0,27,12,'620623','','天祝藏族自治县','tian zhu zang zu zi zhi xian');
_R3(0,27,12,'620602','','凉州区','liang zhou qu');
_R2(0,27,'620700','','张掖市','zhang ye shi');
_R3(0,27,13,'620723','','临泽县','lin ze xian');
_R3(0,27,13,'620724','','高台县','gao tai xian');
_R3(0,27,13,'620725','','山丹县','shan dan xian');
_R3(0,27,13,'620726','','其它区','qi ta qu');
_R3(0,27,13,'620721','','肃南裕固族自治县','su nan yu gu zu zi zhi xian');
_R3(0,27,13,'620722','','民乐县','min le xian');
_R3(0,27,13,'620702','','甘州区','gan zhou qu');

_R1(0,'630000','','青海省','qing hai sheng','青海省');
_R2(0,28,'632600','','果洛藏族自治州','guo luo zang zu zi zhi zhou');
_R3(0,28,0,'632624','','达日县','da ri xian');
_R3(0,28,0,'632623','','甘德县','gan de xian');
_R3(0,28,0,'632622','','班玛县','ban ma xian');
_R3(0,28,0,'632621','','玛沁县','ma qin xian');
_R3(0,28,0,'632627','','其它区','qi ta qu');
_R3(0,28,0,'632626','','玛多县','ma duo xian');
_R3(0,28,0,'632625','','久治县','jiu zhi xian');
_R2(0,28,'632200','','海北藏族自治州','hai bei zang zu zi zhi zhou');
_R3(0,28,1,'632224','','刚察县','gang cha xian');
_R3(0,28,1,'632223','','海晏县','hai yan xian');
_R3(0,28,1,'632222','','祁连县','qi lian xian');
_R3(0,28,1,'632221','','门源回族自治县','men yuan hui zu zi zhi xian');
_R3(0,28,1,'632225','','其它区','qi ta qu');
_R2(0,28,'632100','','海东市','hai dong shi');
_R3(0,28,2,'632129','','其它区','qi ta qu');
_R3(0,28,2,'632128','','循化撒拉族自治县','xun hua sa la zu zi zhi xian');
_R3(0,28,2,'632127','','化隆回族自治县','hua long hui zu zi zhi xian');
_R3(0,28,2,'632126','','互助土族自治县','hu zhu tu zu zi zhi xian');
_R3(0,28,2,'632121','','平安区','ping an qu');
_R3(0,28,2,'632123','','乐都区','le du qu');
_R3(0,28,2,'632122','','民和回族土族自治县','min he hui zu tu zu zi zhi xian');
_R2(0,28,'632500','','海南藏族自治州','hai nan zang zu zi zhi zhou');
_R3(0,28,3,'632525','','贵南县','gui nan xian');
_R3(0,28,3,'632524','','兴海县','xing hai xian');
_R3(0,28,3,'632523','','贵德县','gui de xian');
_R3(0,28,3,'632522','','同德县','tong de xian');
_R3(0,28,3,'632526','','其它区','qi ta qu');
_R3(0,28,3,'632521','','共和县','gong he xian');
_R2(0,28,'632800','','海西蒙古族藏族自治州','hai xi meng gu zu zang zu zi zhi zhou');
_R3(0,28,4,'632803','','茫崖市','mang ya shi');
_R3(0,28,4,'632802','','德令哈市','de ling ha shi');
_R3(0,28,4,'632801','','格尔木市','ge er mu shi');
_R3(0,28,4,'632822','','都兰县','du lan xian');
_R3(0,28,4,'632821','','乌兰县','wu lan xian');
_R3(0,28,4,'632825','','海西蒙古族藏族自治州直辖','hai xi meng gu zu zang zu zi zhi zhou zhi xia');
_R3(0,28,4,'632824','','其它区','qi ta qu');
_R3(0,28,4,'632823','','天峻县','tian jun xian');
_R2(0,28,'632300','','黄南藏族自治州','huang nan zang zu zi zhi zhou');
_R3(0,28,5,'632323','','泽库县','ze ku xian');
_R3(0,28,5,'632322','','尖扎县','jian zha xian');
_R3(0,28,5,'632321','','同仁市','tong ren shi');
_R3(0,28,5,'632325','','其它区','qi ta qu');
_R3(0,28,5,'632324','','河南蒙古族自治县','he nan meng gu zu zi zhi xian');
_R2(0,28,'630100','','西宁市','xi ning shi');
_R3(0,28,6,'630104','','城西区','cheng xi qu');
_R3(0,28,6,'630105','','城北区','cheng bei qu');
_R3(0,28,6,'630102','','城东区','cheng dong qu');
_R3(0,28,6,'630103','','城中区','cheng zhong qu');
_R3(0,28,6,'630122','','湟中区','huang zhong qu');
_R3(0,28,6,'630123','','湟源县','huang yuan xian');
_R3(0,28,6,'630121','','大通回族土族自治县','da tong hui zu tu zu zi zhi xian');
_R3(0,28,6,'630124','','其它区','qi ta qu');
_R2(0,28,'632700','','玉树藏族自治州','yu shu zang zu zi zhi zhou');
_R3(0,28,7,'632723','','称多县','cheng duo xian');
_R3(0,28,7,'632722','','杂多县','za duo xian');
_R3(0,28,7,'632721','','玉树市','yu shu shi');
_R3(0,28,7,'632727','','其它区','qi ta qu');
_R3(0,28,7,'632726','','曲麻莱县','qu ma lai xian');
_R3(0,28,7,'632725','','囊谦县','nang qian xian');
_R3(0,28,7,'632724','','治多县','zhi duo xian');

_R1(0,'640000','','宁夏','ning xia hui zu zi zhi qu','宁夏回族自治区');
_R2(0,29,'640400','','固原市','gu yuan shi');
_R3(0,29,0,'640423','','隆德县','long de xian');
_R3(0,29,0,'640422','','西吉县','xi ji xian');
_R3(0,29,0,'640425','','彭阳县','peng yang xian');
_R3(0,29,0,'640424','','泾源县','jing yuan xian');
_R3(0,29,0,'640426','','其它区','qi ta qu');
_R3(0,29,0,'640402','','原州区','yuan zhou qu');
_R2(0,29,'640200','','石嘴山市','shi zui shan shi');
_R3(0,29,1,'640202','','大武口区','da wu kou qu');
_R3(0,29,1,'640205','','惠农区','hui nong qu');
_R3(0,29,1,'640221','','平罗县','ping luo xian');
_R3(0,29,1,'640222','','其它区','qi ta qu');
_R2(0,29,'640300','','吴忠市','wu zhong shi');
_R3(0,29,2,'640302','','利通区','li tong qu');
_R3(0,29,2,'640303','','红寺堡区','hong si bao qu');
_R3(0,29,2,'640324','','同心县','tong xin xian');
_R3(0,29,2,'640323','','盐池县','yan chi xian');
_R3(0,29,2,'640382','','其它区','qi ta qu');
_R3(0,29,2,'640381','','青铜峡市','qing tong xia shi');
_R2(0,29,'640100','','银川市','yin chuan shi');
_R3(0,29,3,'640182','','其它区','qi ta qu');
_R3(0,29,3,'640181','','灵武市','ling wu shi');
_R3(0,29,3,'640104','','兴庆区','xing qing qu');
_R3(0,29,3,'640106','','金凤区','jin feng qu');
_R3(0,29,3,'640105','','西夏区','xi xia qu');
_R3(0,29,3,'640122','','贺兰县','he lan xian');
_R3(0,29,3,'640121','','永宁县','yong ning xian');
_R2(0,29,'640500','','中卫市','zhong wei shi');
_R3(0,29,4,'640502','','沙坡头区','sha po tou qu');
_R3(0,29,4,'640522','','海原县','hai yuan xian');
_R3(0,29,4,'640521','','中宁县','zhong ning xian');
_R3(0,29,4,'640523','','其它区','qi ta qu');

_R1(0,'650000','','新疆','xin jiang wei wu er zi zhi qu','新疆维吾尔自治区');
_R2(0,30,'652900','','阿克苏地区','a ke su di qu');
_R3(0,30,0,'652930','','其它区','qi ta qu');
_R3(0,30,0,'652924','','沙雅县','sha ya xian');
_R3(0,30,0,'652925','','新和县','xin he xian');
_R3(0,30,0,'652926','','拜城县','bai cheng xian');
_R3(0,30,0,'652927','','乌什县','wu shi xian');
_R3(0,30,0,'652928','','阿瓦提县','a wa ti xian');
_R3(0,30,0,'652929','','柯坪县','ke ping xian');
_R3(0,30,0,'652922','','温宿县','wen su xian');
_R3(0,30,0,'652923','','库车市','ku che shi');
_R3(0,30,0,'652901','','阿克苏市','a ke su shi');
_R2(0,30,'659002','','阿拉尔市','a la er shi');
_R2(0,30,'654300','','阿勒泰地区','a le tai di qu');
_R3(0,30,2,'654321','','布尔津县','bu er jin xian');
_R3(0,30,2,'654322','','富蕴县','fu yun xian');
_R3(0,30,2,'654323','','福海县','fu hai xian');
_R3(0,30,2,'654324','','哈巴河县','ha ba he xian');
_R3(0,30,2,'654325','','青河县','qing he xian');
_R3(0,30,2,'654326','','吉木乃县','ji mu nai xian');
_R3(0,30,2,'654327','','其它区','qi ta qu');
_R3(0,30,2,'654301','','阿勒泰市','a le tai shi');
_R2(0,30,'652800','','巴音郭楞蒙古自治州','ba yin guo leng meng gu zi zhi zhou');
_R3(0,30,3,'652801','','库尔勒市','ku er le shi');
_R3(0,30,3,'652830','','其它区','qi ta qu');
_R3(0,30,3,'652825','','且末县','qie mo xian');
_R3(0,30,3,'652826','','焉耆回族自治县','yan qi hui zu zi zhi xian');
_R3(0,30,3,'652827','','和静县','he jing xian');
_R3(0,30,3,'652828','','和硕县','he shuo xian');
_R3(0,30,3,'652829','','博湖县','bo hu xian');
_R3(0,30,3,'652822','','轮台县','lun tai xian');
_R3(0,30,3,'652823','','尉犁县','yu li xian');
_R3(0,30,3,'652824','','若羌县','ruo qiang xian');
_R2(0,30,'659005','','北屯市','bei tun shi');
_R2(0,30,'652700','','博尔塔拉蒙古自治州','bo er ta la meng gu zi zhi zhou');
_R3(0,30,5,'652701','','博乐市','bo le shi');
_R3(0,30,5,'652702','','阿拉山口市','a la shan kou shi');
_R3(0,30,5,'652722','','精河县','jing he xian');
_R3(0,30,5,'652723','','温泉县','wen quan xian');
_R3(0,30,5,'652724','','其它区','qi ta qu');
_R2(0,30,'652300','','昌吉回族自治州','chang ji hui zu zi zhi zhou');
_R3(0,30,6,'652301','','昌吉市','chang ji shi');
_R3(0,30,6,'652302','','阜康市','fu kang shi');
_R3(0,30,6,'652323','','呼图壁县','hu tu bi xian');
_R3(0,30,6,'652324','','玛纳斯县','ma na si xian');
_R3(0,30,6,'652325','','奇台县','qi tai xian');
_R3(0,30,6,'652327','','吉木萨尔县','ji mu sa er xian');
_R3(0,30,6,'652328','','木垒哈萨克自治县','mu lei ha sa ke zi zhi xian');
_R3(0,30,6,'652329','','其它区','qi ta qu');
_R2(0,30,'652200','','哈密市','ha mi shi');
_R3(0,30,7,'652222','','巴里坤哈萨克自治县','ba li kun ha sa ke zi zhi xian');
_R3(0,30,7,'652201','','伊州区','yi zhou qu');
_R3(0,30,7,'652223','','伊吾县','yi wu xian');
_R3(0,30,7,'652224','','其它区','qi ta qu');
_R2(0,30,'653200','','和田地区','he tian di qu');
_R3(0,30,8,'653201','','和田市','he tian shi');
_R3(0,30,8,'653224','','洛浦县','luo pu xian');
_R3(0,30,8,'653225','','策勒县','ce le xian');
_R3(0,30,8,'653226','','于田县','yu tian xian');
_R3(0,30,8,'653227','','民丰县','min feng xian');
_R3(0,30,8,'653228','','其它区','qi ta qu');
_R3(0,30,8,'653221','','和田县','he tian xian');
_R3(0,30,8,'653222','','墨玉县','mo yu xian');
_R3(0,30,8,'653223','','皮山县','pi shan xian');
_R2(0,30,'659010','','胡杨河市','hu yang he shi');
_R2(0,30,'653100','','喀什地区','ka shi di qu');
_R3(0,30,10,'653101','','喀什市','ka shi shi');
_R3(0,30,10,'653125','','莎车县','sha che xian');
_R3(0,30,10,'653126','','叶城县','ye cheng xian');
_R3(0,30,10,'653127','','麦盖提县','mai gai ti xian');
_R3(0,30,10,'653128','','岳普湖县','yue pu hu xian');
_R3(0,30,10,'653129','','伽师县','jia shi xian');
_R3(0,30,10,'653130','','巴楚县','ba chu xian');
_R3(0,30,10,'653131','','塔什库尔干塔吉克自治县','ta shi ku er gan ta ji ke zi zhi xian');
_R3(0,30,10,'653132','','其它区','qi ta qu');
_R3(0,30,10,'653121','','疏附县','shu fu xian');
_R3(0,30,10,'653122','','疏勒县','shu le xian');
_R3(0,30,10,'653123','','英吉沙县','ying ji sha xian');
_R3(0,30,10,'653124','','泽普县','ze pu xian');
_R2(0,30,'659008','','可克达拉市','ke ke da la shi');
_R2(0,30,'650200','','克拉玛依市','ke la ma yi shi');
_R3(0,30,12,'650206','','其它区','qi ta qu');
_R3(0,30,12,'650205','','乌尔禾区','wu er he qu');
_R3(0,30,12,'650204','','白碱滩区','bai jian tan qu');
_R3(0,30,12,'650203','','克拉玛依区','ke la ma yi qu');
_R3(0,30,12,'650202','','独山子区','du shan zi qu');
_R2(0,30,'653000','','克孜勒苏柯尔克孜自治州','ke zi le su ke er ke zi zi zhi zhou');
_R3(0,30,13,'653001','','阿图什市','a tu shi shi');
_R3(0,30,13,'653022','','阿克陶县','a ke tao xian');
_R3(0,30,13,'653023','','阿合奇县','a he qi xian');
_R3(0,30,13,'653024','','乌恰县','wu qia xian');
_R3(0,30,13,'653025','','其它区','qi ta qu');
_R2(0,30,'659009','','昆玉市','kun yu shi');
_R2(0,30,'659001','','石河子市','shi he zi shi');
_R2(0,30,'659007','','双河市','shuang he shi');
_R2(0,30,'654200','','塔城地区','ta cheng di qu');
_R3(0,30,17,'654201','','塔城市','ta cheng shi');
_R3(0,30,17,'654202','','乌苏市','wu su shi');
_R3(0,30,17,'654221','','额敏县','e min xian');
_R3(0,30,17,'654223','','沙湾市','sha wan shi');
_R3(0,30,17,'654224','','托里县','tuo li xian');
_R3(0,30,17,'654225','','裕民县','yu min xian');
_R3(0,30,17,'654226','','和布克赛尔蒙古自治县','he bu ke sai er meng gu zi zhi xian');
_R3(0,30,17,'654227','','其它区','qi ta qu');
_R2(0,30,'659006','','铁门关市','tie men guan shi');
_R2(0,30,'652100','','吐鲁番市','tu lu fan shi');
_R3(0,30,19,'652124','','其它区','qi ta qu');
_R3(0,30,19,'652101','','高昌区','gao chang qu');
_R3(0,30,19,'652122','','鄯善县','shan shan xian');
_R3(0,30,19,'652123','','托克逊县','tuo ke xun xian');
_R2(0,30,'659003','','图木舒克市','tu mu shu ke shi');
_R2(0,30,'659004','','五家渠市','wu jia qu shi');
_R2(0,30,'650100','','乌鲁木齐市','wu lu mu qi shi');
_R3(0,30,22,'650121','','乌鲁木齐县','wu lu mu qi xian');
_R3(0,30,22,'650107','','达坂城区','da ban cheng qu');
_R3(0,30,22,'650106','','头屯河区','tou tun he qu');
_R3(0,30,22,'650105','','水磨沟区','shui mo gou qu');
_R3(0,30,22,'650104','','新市区','xin shi qu');
_R3(0,30,22,'650103','','沙依巴克区','sha yi ba ke qu');
_R3(0,30,22,'650102','','天山区','tian shan qu');
_R3(0,30,22,'650109','','米东区','mi dong qu');
_R3(0,30,22,'650122','','其它区','qi ta qu');
_R2(0,30,'654000','','伊犁哈萨克自治州','yi li ha sa ke zi zhi zhou');
_R3(0,30,23,'654027','','特克斯县','te ke si xian');
_R3(0,30,23,'654028','','尼勒克县','ni le ke xian');
_R3(0,30,23,'654029','','其它区','qi ta qu');
_R3(0,30,23,'654002','','伊宁市','yi ning shi');
_R3(0,30,23,'654003','','奎屯市','kui tun shi');
_R3(0,30,23,'654004','','霍尔果斯市','huo er guo si shi');
_R3(0,30,23,'654021','','伊宁县','yi ning xian');
_R3(0,30,23,'654022','','察布查尔锡伯自治县','cha bu cha er xi bo zi zhi xian');
_R3(0,30,23,'654023','','霍城县','huo cheng xian');
_R3(0,30,23,'654024','','巩留县','gong liu xian');
_R3(0,30,23,'654025','','新源县','xin yuan xian');
_R3(0,30,23,'654026','','昭苏县','zhao su xian');

_R1(0,'710000','TW','台湾','tai wan sheng','台湾');
_R2(0,31,'710200','','高雄市','gao xiong shi');
_R3(0,31,0,'710249','','桥头区','qiao tou qu');
_R3(0,31,0,'710248','','燕巢区','yan chao qu');
_R3(0,31,0,'710250','','梓官区','zi guan qu');
_R3(0,31,0,'710254','','凤山区','feng shan qu');
_R3(0,31,0,'710253','','湖内区','hu nei qu');
_R3(0,31,0,'710252','','永安区','yong an qu');
_R3(0,31,0,'710251','','弥陀区','mi tuo qu');
_R3(0,31,0,'710258','','大树区','da shu qu');
_R3(0,31,0,'710257','','鸟松区','niao song qu');
_R3(0,31,0,'710256','','林园区','lin yuan qu');
_R3(0,31,0,'710255','','大寮区','da liao qu');
_R3(0,31,0,'710243','','大社区','da she qu');
_R3(0,31,0,'710242','','仁武区','ren wu qu');
_R3(0,31,0,'710241','','苓雅区','ling ya qu');
_R3(0,31,0,'710247','','田寮区','tian liao qu');
_R3(0,31,0,'710246','','阿莲区','a lian qu');
_R3(0,31,0,'710245','','路竹区','lu zhu qu');
_R3(0,31,0,'710244','','冈山区','gang shan qu');
_R3(0,31,0,'710207','','前镇区','qian zhen qu');
_R3(0,31,0,'710206','','旗津区','qi jin qu');
_R3(0,31,0,'710205','','鼓山区','gu shan qu');
_R3(0,31,0,'710204','','盐埕区','yan cheng qu');
_R3(0,31,0,'710209','','左营区','zuo ying qu');
_R3(0,31,0,'710208','','三民区','san min qu');
_R3(0,31,0,'710210','','楠梓区','nan zi qu');
_R3(0,31,0,'710212','','其它区','qi ta qu');
_R3(0,31,0,'710211','','小港区','xiao gang qu');
_R3(0,31,0,'710203','','芩雅区','qin ya qu');
_R3(0,31,0,'710202','','前金区','qian jin qu');
_R3(0,31,0,'710201','','新兴区','xin xing qu');
_R3(0,31,0,'710259','','旗山区','qi shan qu');
_R3(0,31,0,'710261','','六龟区','liu gui qu');
_R3(0,31,0,'710260','','美浓区','mei nong qu');
_R3(0,31,0,'710265','','桃源区','tao yuan qu');
_R3(0,31,0,'710264','','甲仙区','jia xian qu');
_R3(0,31,0,'710263','','杉林区','shan lin qu');
_R3(0,31,0,'710262','','内门区','nei men qu');
_R3(0,31,0,'710268','','茄萣区','qie ding qu');
_R3(0,31,0,'710267','','茂林区','mao lin qu');
_R3(0,31,0,'710266','','那玛夏区','nei ma xia qu');
_R2(0,31,'712600','','花莲县','hua lian xian');
_R3(0,31,1,'712628','','富里乡','fu li xiang');
_R3(0,31,1,'712624','','瑞穗乡','rui sui xiang');
_R3(0,31,1,'712625','','万荣乡','wan rong xiang');
_R3(0,31,1,'712626','','玉里镇','yu li zhen');
_R3(0,31,1,'712627','','卓溪乡','zhuo xi xiang');
_R3(0,31,1,'712617','','太鲁阁','tai lu ge');
_R3(0,31,1,'712618','','秀林乡','xiu lin xiang');
_R3(0,31,1,'712619','','吉安乡','ji an xiang');
_R3(0,31,1,'712615','','花莲市','hua lian shi');
_R3(0,31,1,'712616','','新城乡','xin cheng xiang');
_R3(0,31,1,'712620','','寿丰乡','shou feng xiang');
_R3(0,31,1,'712621','','凤林镇','feng lin zhen');
_R3(0,31,1,'712622','','光复乡','guang fu xiang');
_R3(0,31,1,'712623','','丰滨乡','feng bin xiang');
_R2(0,31,'710700','','基隆市','ji long shi');
_R3(0,31,2,'710702','','信义区','xin yi qu');
_R3(0,31,2,'710701','','仁爱区','ren ai qu');
_R3(0,31,2,'710706','','暖暖区','nuan nuan qu');
_R3(0,31,2,'710705','','安乐区','an le qu');
_R3(0,31,2,'710704','','中山区','zhong shan qu');
_R3(0,31,2,'710703','','中正区','zhong zheng qu');
_R3(0,31,2,'710708','','其它区','qi ta qu');
_R3(0,31,2,'710707','','七堵区','qi du qu');
_R2(0,31,'710900','','嘉义市','jia yi shi');
_R3(0,31,3,'710903','','其它区','qi ta qu');
_R3(0,31,3,'710902','','西区','xi qu');
_R3(0,31,3,'710901','','东区','dong qu');
_R2(0,31,'711900','','嘉义县','jia yi xian');
_R3(0,31,4,'711919','','番路乡','fan lu xiang');
_R3(0,31,4,'711935','','义竹乡','yi zhu xiang');
_R3(0,31,4,'711936','','布袋镇','bu dai zhen');
_R3(0,31,4,'711931','','新港乡','xin gang xiang');
_R3(0,31,4,'711932','','民雄乡','min xiong xiang');
_R3(0,31,4,'711933','','大林镇','da lin zhen');
_R3(0,31,4,'711934','','溪口乡','xi kou xiang');
_R3(0,31,4,'711924','','大埔乡','da pu xiang');
_R3(0,31,4,'711925','','水上乡','shui shang xiang');
_R3(0,31,4,'711926','','鹿草乡','lu cao xiang');
_R3(0,31,4,'711927','','太保市','tai bao shi');
_R3(0,31,4,'711920','','梅山乡','mei shan xiang');
_R3(0,31,4,'711921','','竹崎乡','zhu qi xiang');
_R3(0,31,4,'711922','','阿里山乡','a li shan xiang');
_R3(0,31,4,'711923','','中埔乡','zhong pu xiang');
_R3(0,31,4,'711928','','朴子市','po zi shi');
_R3(0,31,4,'711929','','东石乡','dong shi xiang');
_R3(0,31,4,'711930','','六脚乡','liu jiao xiang');
_R2(0,31,'710500','','金门县','jin men xian');
_R3(0,31,5,'710508','','金湖镇','jin hu zhen');
_R3(0,31,5,'710507','','金沙镇','jin sha zhen');
_R3(0,31,5,'710509','','金宁乡','jin ning xiang');
_R3(0,31,5,'710511','','烈屿乡','lie yu xiang');
_R3(0,31,5,'710510','','金城镇','jin cheng zhen');
_R3(0,31,5,'710512','','乌坵乡','wu qiu xiang');
_R2(0,31,'712800','','连江县','lian jiang xian');
_R3(0,31,6,'712805','','南竿乡','nan gan xiang');
_R3(0,31,6,'712806','','北竿乡','bei gan xiang');
_R3(0,31,6,'712807','','莒光乡','ju guang xiang');
_R3(0,31,6,'712808','','东引乡','dong yin xiang');
_R2(0,31,'711500','','苗栗县','miao li xian');
_R3(0,31,7,'711519','','竹南镇','zhu nan zhen');
_R3(0,31,7,'711520','','头份市','tou fen shi');
_R3(0,31,7,'711521','','三湾乡','san wan xiang');
_R3(0,31,7,'711522','','南庄乡','nan zhuang xiang');
_R3(0,31,7,'711523','','狮潭乡','shi tan xiang');
_R3(0,31,7,'711535','','西湖乡','xi hu xiang');
_R3(0,31,7,'711536','','卓兰镇','zhuo lan zhen');
_R3(0,31,7,'711528','','造桥乡','zao qiao xiang');
_R3(0,31,7,'711529','','头屋乡','tou wu xiang');
_R3(0,31,7,'711524','','后龙镇','hou long zhen');
_R3(0,31,7,'711525','','通霄镇','tong xiao zhen');
_R3(0,31,7,'711526','','苑里镇','yuan li zhen');
_R3(0,31,7,'711527','','苗栗市','miao li shi');
_R3(0,31,7,'711531','','大湖乡','da hu xiang');
_R3(0,31,7,'711532','','泰安乡','tai an xiang');
_R3(0,31,7,'711533','','铜锣乡','tong luo xiang');
_R3(0,31,7,'711534','','三义乡','san yi xiang');
_R3(0,31,7,'711530','','公馆乡','gong guan xiang');
_R2(0,31,'710600','','南投县','nan tou xian');
_R3(0,31,8,'710625','','竹山镇','zhu shan zhen');
_R3(0,31,8,'710624','','信义乡','xin yi xiang');
_R3(0,31,8,'710623','','鱼池乡','yu chi xiang');
_R3(0,31,8,'710622','','水里乡','shui li xiang');
_R3(0,31,8,'710626','','鹿谷乡','lu gu xiang');
_R3(0,31,8,'710614','','南投市','nan tou shi');
_R3(0,31,8,'710618','','埔里镇','pu li zhen');
_R3(0,31,8,'710617','','国姓乡','guo xing xiang');
_R3(0,31,8,'710616','','草屯镇','cao tun zhen');
_R3(0,31,8,'710615','','中寮乡','zhong liao xiang');
_R3(0,31,8,'710619','','仁爱乡','ren ai xiang');
_R3(0,31,8,'710621','','集集镇','ji ji zhen');
_R3(0,31,8,'710620','','名间乡','ming jian xiang');
_R2(0,31,'712700','','澎湖县','peng hu xian');
_R3(0,31,9,'712707','','马公市','ma gong shi');
_R3(0,31,9,'712708','','西屿乡','xi yu xiang');
_R3(0,31,9,'712709','','望安乡','wang an xiang');
_R3(0,31,9,'712710','','七美乡','qi mei xiang');
_R3(0,31,9,'712711','','白沙乡','bai sha xiang');
_R3(0,31,9,'712712','','湖西乡','hu xi xiang');
_R2(0,31,'712400','','屏东县','ping dong xian');
_R3(0,31,10,'712434','','屏东市','ping dong shi');
_R3(0,31,10,'712435','','三地门乡','san di men xiang');
_R3(0,31,10,'712436','','雾台乡','wu tai xiang');
_R3(0,31,10,'712448','','泰武乡','tai wu xiang');
_R3(0,31,10,'712449','','来义乡','lai yi xiang');
_R3(0,31,10,'712450','','万峦乡','wan luan xiang');
_R3(0,31,10,'712455','','东港镇','dong gang zhen');
_R3(0,31,10,'712456','','琉球乡','liu qiu xiang');
_R3(0,31,10,'712457','','佳冬乡','jia dong xiang');
_R3(0,31,10,'712458','','新园乡','xin yuan xiang');
_R3(0,31,10,'712451','','崁顶乡','kan ding xiang');
_R3(0,31,10,'712452','','新埤乡','xin pi xiang');
_R3(0,31,10,'712453','','南州乡','nan zhou xiang');
_R3(0,31,10,'712454','','林边乡','lin bian xiang');
_R3(0,31,10,'712437','','玛家乡','ma jia xiang');
_R3(0,31,10,'712438','','九如乡','jiu ru xiang');
_R3(0,31,10,'712439','','里港乡','li gang xiang');
_R3(0,31,10,'712444','','竹田乡','zhu tian xiang');
_R3(0,31,10,'712445','','内埔乡','nei pu xiang');
_R3(0,31,10,'712446','','万丹乡','wan dan xiang');
_R3(0,31,10,'712447','','潮州镇','chao zhou zhen');
_R3(0,31,10,'712440','','高树乡','gao shu xiang');
_R3(0,31,10,'712441','','盐埔乡','yan pu xiang');
_R3(0,31,10,'712442','','长治乡','chang zhi xiang');
_R3(0,31,10,'712443','','麟洛乡','lin luo xiang');
_R3(0,31,10,'712459','','枋寮乡','fang liao xiang');
_R3(0,31,10,'712460','','枋山乡','fang shan xiang');
_R3(0,31,10,'712461','','春日乡','chun ri xiang');
_R3(0,31,10,'712466','','满州乡','man zhou xiang');
_R3(0,31,10,'712462','','狮子乡','shi zi xiang');
_R3(0,31,10,'712463','','车城乡','che cheng xiang');
_R3(0,31,10,'712464','','牡丹乡','mu dan xiang');
_R3(0,31,10,'712465','','恒春镇','heng chun zhen');
_R2(0,31,'710100','','台北市','tai bei shi');
_R3(0,31,11,'710108','','士林区','shi lin qu');
_R3(0,31,11,'710107','','信义区','xin yi qu');
_R3(0,31,11,'710106','','万华区','wan hua qu');
_R3(0,31,11,'710105','','大安区','da an qu');
_R3(0,31,11,'710109','','北投区','bei tou qu');
_R3(0,31,11,'710111','','南港区','nan gang qu');
_R3(0,31,11,'710110','','内湖区','nei hu qu');
_R3(0,31,11,'710113','','其它区','qi ta qu');
_R3(0,31,11,'710112','','文山区','wen shan qu');
_R3(0,31,11,'710104','','松山区','song shan qu');
_R3(0,31,11,'710103','','中山区','zhong shan qu');
_R3(0,31,11,'710102','','大同区','da tong qu');
_R3(0,31,11,'710101','','中正区','zhong zheng qu');
_R2(0,31,'712500','','台东县','tai dong xian');
_R3(0,31,12,'712529','','金峰乡','jin feng xiang');
_R3(0,31,12,'712525','','池上乡','chi shang xiang');
_R3(0,31,12,'712526','','东河乡','dong he xiang');
_R3(0,31,12,'712527','','成功镇','cheng gong zhen');
_R3(0,31,12,'712528','','长滨乡','chang bin xiang');
_R3(0,31,12,'712532','','太麻里乡','tai ma li xiang');
_R3(0,31,12,'712530','','大武乡','da wu xiang');
_R3(0,31,12,'712531','','达仁乡','da ren xiang');
_R3(0,31,12,'712518','','绿岛乡','lv dao xiang');
_R3(0,31,12,'712519','','兰屿乡','lan yu xiang');
_R3(0,31,12,'712517','','台东市','tai dong shi');
_R3(0,31,12,'712521','','卑南乡','bei nan xiang');
_R3(0,31,12,'712522','','鹿野乡','lu ye xiang');
_R3(0,31,12,'712523','','关山镇','guan shan zhen');
_R3(0,31,12,'712524','','海端乡','hai duan xiang');
_R3(0,31,12,'712520','','延平乡','yan ping xiang');
_R2(0,31,'710300','','台南市','tai nan shi');
_R3(0,31,13,'710349','','官田区','guan tian qu');
_R3(0,31,13,'710348','','龙崎区','long qi qu');
_R3(0,31,13,'710347','','关庙区','guan miao qu');
_R3(0,31,13,'710353','','七股区','qi gu qu');
_R3(0,31,13,'710352','','西港区','xi gang qu');
_R3(0,31,13,'710351','','佳里区','jia li qu');
_R3(0,31,13,'710350','','麻豆区','ma dou qu');
_R3(0,31,13,'710357','','新营区','xin ying qu');
_R3(0,31,13,'710356','','北门区','bei men qu');
_R3(0,31,13,'710355','','学甲区','xue jia qu');
_R3(0,31,13,'710354','','将军区','jiang jun qu');
_R3(0,31,13,'710339','','永康区','yong kang qu');
_R3(0,31,13,'710342','','左镇区','zuo zhen qu');
_R3(0,31,13,'710341','','新化区','xin hua qu');
_R3(0,31,13,'710340','','归仁区','gui ren qu');
_R3(0,31,13,'710346','','仁德区','ren de qu');
_R3(0,31,13,'710345','','南化区','nan hua qu');
_R3(0,31,13,'710344','','楠西区','nan xi qu');
_R3(0,31,13,'710343','','玉井区','yu jing qu');
_R3(0,31,13,'710369','','安定区','an ding qu');
_R3(0,31,13,'710359','','白河区','bai he qu');
_R3(0,31,13,'710358','','后壁区','hou bi qu');
_R3(0,31,13,'710360','','东山区','dong shan qu');
_R3(0,31,13,'710364','','盐水区','yan shui qu');
_R3(0,31,13,'710363','','柳营区','liu ying qu');
_R3(0,31,13,'710362','','下营区','xia ying qu');
_R3(0,31,13,'710361','','六甲区','liu jia qu');
_R3(0,31,13,'710368','','新市区','xin shi qu');
_R3(0,31,13,'710367','','山上区','shan shang qu');
_R3(0,31,13,'710366','','大内区','da nei qu');
_R3(0,31,13,'710365','','善化区','shan hua qu');
_R3(0,31,13,'710306','','安南区','an nan qu');
_R3(0,31,13,'710305','','安平区','an ping qu');
_R3(0,31,13,'710304','','北区','bei qu');
_R3(0,31,13,'710303','','南区','nan qu');
_R3(0,31,13,'710307','','其它区','qi ta qu');
_R3(0,31,13,'710302','','东区','dong qu');
_R3(0,31,13,'710301','','中西区','zhong xi qu');
_R2(0,31,'710400','','台中市','tai zhong shi');
_R3(0,31,14,'710405','','北区','bei qu');
_R3(0,31,14,'710404','','西区','xi qu');
_R3(0,31,14,'710403','','南区','nan qu');
_R3(0,31,14,'710402','','东区','dong qu');
_R3(0,31,14,'710409','','其它区','qi ta qu');
_R3(0,31,14,'710408','','南屯区','nan tun qu');
_R3(0,31,14,'710407','','西屯区','xi tun qu');
_R3(0,31,14,'710406','','北屯区','bei tun qu');
_R3(0,31,14,'710401','','中区','zhong qu');
_R3(0,31,14,'710434','','乌日区','wu ri qu');
_R3(0,31,14,'710433','','雾峰区','wu feng qu');
_R3(0,31,14,'710432','','大里区','da li qu');
_R3(0,31,14,'710431','','太平区','tai ping qu');
_R3(0,31,14,'710449','','大甲区','da jia qu');
_R3(0,31,14,'710448','','清水区','qing shui qu');
_R3(0,31,14,'710447','','梧栖区','wu qi qu');
_R3(0,31,14,'710446','','龙井区','long jing qu');
_R3(0,31,14,'710451','','大安区','da an qu');
_R3(0,31,14,'710450','','外埔区','wai pu qu');
_R3(0,31,14,'710438','','东势区','dong shi qu');
_R3(0,31,14,'710437','','石冈区','shi gang qu');
_R3(0,31,14,'710436','','后里区','hou li qu');
_R3(0,31,14,'710435','','丰原区','feng yuan qu');
_R3(0,31,14,'710439','','和平区','he ping qu');
_R3(0,31,14,'710441','','潭子区','tan zi qu');
_R3(0,31,14,'710440','','新社区','xin she qu');
_R3(0,31,14,'710445','','沙鹿区','sha lu qu');
_R3(0,31,14,'710444','','大肚区','da du qu');
_R3(0,31,14,'710443','','神冈区','shen gang qu');
_R3(0,31,14,'710442','','大雅区','da ya qu');
_R2(0,31,'711400','','桃园市','tao yuan shi');
_R3(0,31,15,'711425','','大园区','da yuan qu');
_R3(0,31,15,'711426','','芦竹区','lu zhu qu');
_R3(0,31,15,'711418','','新屋区','xin wu qu');
_R3(0,31,15,'711419','','观音区','guan yin qu');
_R3(0,31,15,'711414','','中坜区','zhong li qu');
_R3(0,31,15,'711415','','平镇区','ping zhen qu');
_R3(0,31,15,'711416','','龙潭区','long tan qu');
_R3(0,31,15,'711417','','杨梅区','yang mei qu');
_R3(0,31,15,'711421','','龟山区','gui shan qu');
_R3(0,31,15,'711422','','八德区','ba de qu');
_R3(0,31,15,'711423','','大溪区','da xi qu');
_R3(0,31,15,'711424','','复兴区','fu xing qu');
_R3(0,31,15,'711420','','桃园区','tao yuan qu');
_R2(0,31,'711100','','新北市','xin bei shi');
_R3(0,31,16,'711150','','新庄区','xin zhuang qu');
_R3(0,31,16,'711151','','泰山区','tai shan qu');
_R3(0,31,16,'711152','','林口区','lin kou qu');
_R3(0,31,16,'711157','','三芝区','san zhi qu');
_R3(0,31,16,'711158','','石门区','shi men qu');
_R3(0,31,16,'711153','','芦洲区','lu zhou qu');
_R3(0,31,16,'711154','','五股区','wu gu qu');
_R3(0,31,16,'711155','','八里区','ba li qu');
_R3(0,31,16,'711156','','淡水区','dan shui qu');
_R3(0,31,16,'711139','','贡寮区','gong liao qu');
_R3(0,31,16,'711140','','新店区','xin dian qu');
_R3(0,31,16,'711141','','坪林区','ping lin qu');
_R3(0,31,16,'711146','','三峡区','san xia qu');
_R3(0,31,16,'711147','','树林区','shu lin qu');
_R3(0,31,16,'711148','','莺歌区','ying ge qu');
_R3(0,31,16,'711149','','三重区','san zhong qu');
_R3(0,31,16,'711142','','乌来区','wu lai qu');
_R3(0,31,16,'711143','','永和区','yong he qu');
_R3(0,31,16,'711144','','中和区','zhong he qu');
_R3(0,31,16,'711145','','土城区','tu cheng qu');
_R3(0,31,16,'711130','','万里区','wan li qu');
_R3(0,31,16,'711135','','石碇区','shi ding qu');
_R3(0,31,16,'711136','','瑞芳区','rui fang qu');
_R3(0,31,16,'711137','','平溪区','ping xi qu');
_R3(0,31,16,'711138','','双溪区','shuang xi qu');
_R3(0,31,16,'711131','','金山区','jin shan qu');
_R3(0,31,16,'711132','','板桥区','ban qiao qu');
_R3(0,31,16,'711133','','汐止区','xi zhi qu');
_R3(0,31,16,'711134','','深坑区','shen keng qu');
_R2(0,31,'710800','','新竹市','xin zhu shi');
_R3(0,31,17,'710801','','东区','dong qu');
_R3(0,31,17,'710804','','其它区','qi ta qu');
_R3(0,31,17,'710803','','香山区','xiang shan qu');
_R3(0,31,17,'710802','','北区','bei qu');
_R2(0,31,'711300','','新竹县','xin zhu xian');
_R3(0,31,18,'711319','','芎林乡','xiong lin xiang');
_R3(0,31,18,'711315','','湖口乡','hu kou xiang');
_R3(0,31,18,'711316','','新丰乡','xin feng xiang');
_R3(0,31,18,'711317','','新埔镇','xin pu zhen');
_R3(0,31,18,'711318','','关西镇','guan xi zhen');
_R3(0,31,18,'711322','','五峰乡','wu feng xiang');
_R3(0,31,18,'711323','','横山乡','heng shan xiang');
_R3(0,31,18,'711324','','尖石乡','jian shi xiang');
_R3(0,31,18,'711325','','北埔乡','bei pu xiang');
_R3(0,31,18,'711320','','宝山乡','bao shan xiang');
_R3(0,31,18,'711321','','竹东镇','zhu dong zhen');
_R3(0,31,18,'711314','','竹北市','zhu bei shi');
_R3(0,31,18,'711326','','峨眉乡','e mei xiang');
_R2(0,31,'711200','','宜兰县','yi lan xian');
_R3(0,31,19,'711216','','礁溪乡','jiao xi xiang');
_R3(0,31,19,'711217','','壮围乡','zhuang wei xiang');
_R3(0,31,19,'711218','','员山乡','yuan shan xiang');
_R3(0,31,19,'711219','','罗东镇','luo dong zhen');
_R3(0,31,19,'711223','','冬山乡','dong shan xiang');
_R3(0,31,19,'711224','','苏澳镇','su ao zhen');
_R3(0,31,19,'711225','','南澳乡','nan ao xiang');
_R3(0,31,19,'711226','','钓鱼台','diao yu tai');
_R3(0,31,19,'711220','','三星乡','san xing xiang');
_R3(0,31,19,'711221','','大同乡','da tong xiang');
_R3(0,31,19,'711222','','五结乡','wu jie xiang');
_R3(0,31,19,'711214','','宜兰市','yi lan shi');
_R3(0,31,19,'711215','','头城镇','tou cheng zhen');
_R2(0,31,'712100','','云林县','yun lin xian');
_R3(0,31,20,'712129','','麦寮乡','mai liao xiang');
_R3(0,31,20,'712130','','斗六市','dou liu shi');
_R3(0,31,20,'712131','','林内乡','lin nei xiang');
_R3(0,31,20,'712136','','北港镇','bei gang zhen');
_R3(0,31,20,'712137','','水林乡','shui lin xiang');
_R3(0,31,20,'712138','','口湖乡','kou hu xiang');
_R3(0,31,20,'712139','','四湖乡','si hu xiang');
_R3(0,31,20,'712132','','古坑乡','gu keng xiang');
_R3(0,31,20,'712133','','莿桐乡','ci tong xiang');
_R3(0,31,20,'712134','','西螺镇','xi luo zhen');
_R3(0,31,20,'712135','','二仑乡','er lun xiang');
_R3(0,31,20,'712125','','褒忠乡','bao zhong xiang');
_R3(0,31,20,'712126','','东势乡','dong shi xiang');
_R3(0,31,20,'712127','','台西乡','tai xi xiang');
_R3(0,31,20,'712128','','仑背乡','lun bei xiang');
_R3(0,31,20,'712121','','斗南镇','dou nan zhen');
_R3(0,31,20,'712122','','大埤乡','da pi xiang');
_R3(0,31,20,'712123','','虎尾镇','hu wei zhen');
_R3(0,31,20,'712124','','土库镇','tu ku zhen');
_R3(0,31,20,'712140','','元长乡','yuan zhang xiang');
_R2(0,31,'711700','','彰化县','zhang hua xian');
_R3(0,31,21,'711737','','社头乡','she tou xiang');
_R3(0,31,21,'711738','','永靖乡','yong jing xiang');
_R3(0,31,21,'711739','','埔心乡','pu xin xiang');
_R3(0,31,21,'711733','','线西乡','xian xi xiang');
_R3(0,31,21,'711734','','和美镇','he mei zhen');
_R3(0,31,21,'711735','','伸港乡','shen gang xiang');
_R3(0,31,21,'711736','','员林市','yuan lin shi');
_R3(0,31,21,'711740','','溪湖镇','xi hu zhen');
_R3(0,31,21,'711741','','大村乡','da cun xiang');
_R3(0,31,21,'711742','','埔盐乡','pu yan xiang');
_R3(0,31,21,'711743','','田中镇','tian zhong zhen');
_R3(0,31,21,'711727','','彰化市','zhang hua shi');
_R3(0,31,21,'711728','','芬园乡','fen yuan xiang');
_R3(0,31,21,'711729','','花坛乡','hua tan xiang');
_R3(0,31,21,'711730','','秀水乡','xiu shui xiang');
_R3(0,31,21,'711731','','鹿港镇','lu gang zhen');
_R3(0,31,21,'711732','','福兴乡','fu xing xiang');
_R3(0,31,21,'711748','','竹塘乡','zhu tang xiang');
_R3(0,31,21,'711749','','二林镇','er lin zhen');
_R3(0,31,21,'711744','','北斗镇','bei dou zhen');
_R3(0,31,21,'711745','','田尾乡','tian wei xiang');
_R3(0,31,21,'711746','','埤头乡','pi tou xiang');
_R3(0,31,21,'711747','','溪州乡','xi zhou xiang');
_R3(0,31,21,'711751','','芳苑乡','fang yuan xiang');
_R3(0,31,21,'711752','','二水乡','er shui xiang');
_R3(0,31,21,'711750','','大城乡','da cheng xiang');

_R1(0,'810000','HK','香港特别行政区','xiang gang te bie xing zheng qu','香港特别行政区');
_R2(0,32,'810200','','九龙','jiu long');
_R3(0,32,0,'810205','','观塘区','guan tang qu');
_R3(0,32,0,'810201','','九龙城区','jiu long cheng qu');
_R3(0,32,0,'810202','','油尖旺区','you jian wang qu');
_R3(0,32,0,'810203','','深水埗区','shen shui bu qu');
_R3(0,32,0,'810204','','黄大仙区','huang da xian qu');
_R2(0,32,'810100','','香港岛','xiang gang dao');
_R3(0,32,1,'810101','','中西区','zhong xi qu');
_R3(0,32,1,'810102','','湾仔区','wan zai');
_R3(0,32,1,'810103','','东区','dong qu');
_R3(0,32,1,'810104','','南区','nan qu');
_R2(0,32,'810300','','新界','xin jie');
_R3(0,32,2,'810304','','西贡区','xi gong qu');
_R3(0,32,2,'810305','','元朗区','yuan lang qu');
_R3(0,32,2,'810306','','屯门区','tun men qu');
_R3(0,32,2,'810307','','荃湾区','quan wan qu');
_R3(0,32,2,'810308','','葵青区','kui qing qu');
_R3(0,32,2,'810309','','离岛区','li dao qu');
_R3(0,32,2,'810301','','北区','bei qu');
_R3(0,32,2,'810302','','大埔区','da bu qu');
_R3(0,32,2,'810303','','沙田区','sha tian qu');

_R1(0,'820000','MO','澳门特别行政区','ao men te bie xing zheng qu','澳门特别行政区');
_R2(0,33,'820100','','澳门半岛','ao men ban dao');
_R3(0,33,0,'820004','','大堂区','da tang qu');
_R3(0,33,0,'820005','','风顺堂区','feng shun tang qu');
_R3(0,33,0,'820002','','花王堂区','hua wang tang qu');
_R3(0,33,0,'820003','','望德堂区','wang de tang qu');
_R3(0,33,0,'820001','','花地玛堂区','hua di ma tang qu');
_R2(0,33,'820200','','离岛','li dao');
_R3(0,33,1,'820008','','圣方济各堂区','sheng fang ji ge tang qu');
_R3(0,33,1,'820006','','嘉模堂区','jia mu tang qu');
_R3(0,33,1,'820007','','路凼填海区','lu dang tian hai qu');
_R0('1','AW','阿鲁巴','Aruba');
_R0('2','GL','格陵兰岛','Greenland');
_R0('3','GE','格鲁吉亚','Georgia');
_R0('4','CO','哥伦比亚','Colombia');
_R0('5','GP','瓜德罗普','Guadeloupe');
_R0('6','GU','关岛','Guam');
_R0('10','CU','古巴','Cuba');
_R0('11','OM','阿曼','Oman');
_R0('13','GY','圭亚那','Guyana');
_R0('18','HT','海地','Haiti');
_R0('22','AD','安道尔','Andorra');
_R0('24','KR','韩国','Republic of korea');
_R0('30','NL','荷兰','Netherlands');
_R0('35','HN','洪都拉斯','Honduras');
_R0('40','AO','安哥拉','Angola');
_R0('44','GH','加纳','Ghana');
_R0('45','CA','加拿大','Canada');
_R0('49','KH','柬埔寨','Cambodia');
_R0('50','GA','加蓬','Gabon');
_R0('52','DJ','吉布提','Djibouti');
_R0('53','CZ','捷克','Czech republic');
_R0('56','ZW','津巴布韦','Zimbabwe');
_R0('58','GN','几内亚','Guinea');
_R0('66','KY','开曼群岛','Cayman islands');
_R0('67','CM','喀麦隆','Cameroon');
_R0('69','QA','卡塔尔','Qatar');
_R0('70','HR','克罗地亚','Croatia');
_R0('71','KM','科摩罗','Comoros');
_R0('73','KE','肯尼亚','Kenya');
_R0('74','CI','科特迪瓦','Cote d\'ivoire');
_R0('75','KW','科威特','Kuwait');
_R0('76','CK','库克群岛','Cook islands');
_R0('81','LA','老挝','Laos');
_R0('82','LV','拉脱维亚','Latvia');
_R0('83','AG','安提瓜','Antigua and barbuda');
_R0('87','LB','黎巴嫩','Lebanon');
_R0('88','LR','利比里亚','Liberia');
_R0('89','LY','利比亚','Libya');
_R0('92','AU','澳大利亚','Australia');
_R0('94','LT','立陶宛','Lithuania');
_R0('95','RE','留尼汪岛','Reunion island');
_R0('97','RO','罗马尼亚','Romania');
_R0('99','LU','卢森堡','Luxembourg');
_R0('100','RW','卢旺达','Rwanda');
_R0('102','AT','奥地利','Austria');
_R0('104','MV','马尔代夫','Maldives');
_R0('105','MT','马耳他','Malta');
_R0('106','MY','马来西亚','Malaysia');
_R0('107','MW','马拉维','Malawi');
_R0('108','ML','马里','Mali');
_R0('110','MU','毛里求斯','Mauritius');
_R0('111','MK','马其顿','Macedonia');
_R0('113','MQ','马提尼克','Martinique');
_R0('115','US','美国','United states');
_R0('117','MN','蒙古','Mongolia');
_R0('118','BD','孟加拉国','Bangladesh');
_R0('121','MM','缅甸','Myanmar');
_R0('123','PE','秘鲁','Peru');
_R0('124','DM','多米尼克','Dominica');
_R0('125','MD','摩尔多瓦','Moldova');
_R0('126','MA','摩洛哥','Morocco');
_R0('127','MC','摩纳哥','Monaco');
_R0('128','MZ','莫桑比克','Mozambique');
_R0('130','AZ','阿塞拜疆','Azerbaijan');
_R0('131','MX','墨西哥','Mexico');
_R0('133','NA','纳米比亚','Namibia');
_R0('135','ZA','南非','South africa');
_R0('140','BB','巴巴多斯','Barbados');
_R0('143','NP','尼泊尔','Nepal');
_R0('144','NI','尼加拉瓜','Nicaragua');
_R0('147','NE','尼日尔','Niger');
_R0('148','NG','尼日利亚','Nigeria');
_R0('150','NO','挪威','Norway');
_R0('152','PW','帕劳','Palau');
_R0('154','PT','葡萄牙','Portugal');
_R0('160','BS','巴哈马','Bahamas');
_R0('163','JP','日本','Japan');
_R0('165','SE','瑞典','Sweden');
_R0('166','CH','瑞士','Switzerland');
_R0('167','SV','萨尔瓦多','El salvador');
_R0('168','RS','塞尔维亚','Serbia');
_R0('169','SL','塞拉利昂','Sierra leone');
_R0('170','SN','塞内加尔','Senegal');
_R0('171','BY','白俄罗斯','Belarus');
_R0('172','CY','塞浦路斯','Cyprus');
_R0('173','SC','塞舌尔','Seychelles');
_R0('174','WS','萨摩亚','Samoa');
_R0('182','BM','百慕大','Bermuda');
_R0('184','KN','圣基茨','Saint kitts');
_R0('185','SM','圣马力诺','San marino');
_R0('190','LK','斯里兰卡','Sri lanka');
_R0('191','PK','巴基斯坦','Pakistan');
_R0('192','SK','斯洛伐克','Slovakia');
_R0('193','SZ','斯威士兰','Swaziland');
_R0('194','SD','苏丹','Sudan');
_R0('197','SR','苏里南','Suriname');
_R0('201','AF','阿富汗','Afghanistan');
_R0('202','PY','巴拉圭','Paraguay');
_R0('203','TH','泰国','Thailand');
_R0('209','TO','汤加','Tonga');
_R0('211','TZ','坦桑尼亚','Tanzania');
_R0('213','PS','巴勒斯坦','Palestine');
_R0('216','TR','土耳其','Turkey');
_R0('217','TN','突尼斯','Tunisia');
_R0('218','VU','瓦努阿图','Vanuatu');
_R0('219','GT','危地马拉','Guatemala');
_R0('223','VE','委内瑞拉','Venezuela');
_R0('224','BN','文莱','Brunei darussalam');
_R0('226','UG','乌干达','Uganda');
_R0('231','UA','乌克兰','Ukraine');
_R0('232','BH','巴林','Bahrain');
_R0('233','UY','乌拉圭','Uruguay');
_R0('241','PA','巴拿马','Panama');
_R0('242','ES','西班牙','Spain');
_R0('243','GR','希腊','Greece');
_R0('248','SG','新加坡','Singapore');
_R0('250','NZ','新西兰','New zealand');
_R0('254','HU','匈牙利','Hungary');
_R0('259','SY','叙利亚','Syrian arab republic');
_R0('261','JM','牙买加','Jamaica');
_R0('262','BG','保加利亚','Bulgaria');
_R0('263','AM','亚美尼亚','Armenia');
_R0('269','YE','也门','Yemen');
_R0('272','IT','意大利','Italy');
_R0('274','IQ','伊拉克','Iraq');
_R0('275','IR','伊朗','Iran');
_R0('277','IN','印度','India');
_R0('279','GB','英国','United kingdom');
_R0('281','IL','以色列','Israel');
_R0('282','BR','巴西','Brazil');
_R0('285','JO','约旦','Jordan');
_R0('286','VN','越南','Vietnam');
_R0('288','TD','乍得','Chad');
_R0('289','ZM','赞比亚','Zambia');
_R0('297','GI','直布罗陀','Gibraltar');
_R0('298','CL','智利','Chile');
_R0('299','CF','中非','Central african republic');
_R0('303','AR','阿根廷','Argentina');
_R0('308','BJ','贝宁','Benin');
_R0('309','BE','比利时','Belgium');
_R0('310','IS','冰岛','Iceland');
_R0('311','BW','博茨瓦纳','Botswana');
_R0('312','PR','波多黎各','Puerto rico');
_R0('313','BA','波黑','Bosnia and herzegovina');
_R0('314','PL','波兰','Poland');
_R0('315','IE','爱尔兰','Ireland');
_R0('316','BO','玻利维亚','Bolivia');
_R0('317','BZ','伯利兹','Belize');
_R0('318','BT','不丹','Bhutan');
_R0('319','BI','布隆迪','Burundi');
_R0('324','EG','埃及','Egypt');
_R0('326','KP','朝鲜','Democratic peoples republic of korea');
_R0('334','DK','丹麦','Denmark');
_R0('338','DE','德国','Germany');
_R0('344','TG','多哥','Togo');
_R0('345','EE','爱沙尼亚','Estonia');
_R0('347','EC','厄瓜多尔','Ecuador');
_R0('348','RU','俄罗斯','Russian federation');
_R0('350','FR','法国','France');
_R0('351','FO','法罗群岛','Faroe islands');
_R0('352','FJ','斐济','Fiji');
_R0('353','PH','菲律宾','Philippines');
_R0('354','AE','阿联酋','United arab emirates');
_R0('356','FI','芬兰','Finland');
_R0('360','GM','冈比亚','Gambia');
_R0('363','AL','阿尔巴尼亚','Albania');
_R0('364','CR','哥斯达黎加','Costa rica');
_R0('365','KZ','哈萨克斯坦','Kazakhstan');
_R0('366','AN','荷属安的列斯群岛','Netherlands antilles');
_R0('367','KG','吉尔吉斯斯坦','Kyrgyzstan');
_R0('368','LI','列支敦士登','Liechtenstein');
_R0('369','MG','马达加斯加','Madagascar');
_R0('370','DZ','阿尔及利亚','Algeria');
_R0('371','MR','毛里塔尼亚','Mauritania');
_R0('372','MH','马绍尔群岛','Marshall islands');
_R0('373','AS','美属萨摩亚','American samoa');
_R0('374','VI','美属维尔京群岛','United states virgin islands');
_R0('375','FM','密克罗尼西亚','Micronesia');
_R0('376','PG','巴布亚新几内亚','Papua new guinea');
_R0('377','SA','沙特阿拉伯','Saudi arabia');
_R0('378','PM','圣皮埃尔和密克隆','Saint pierre and miquelon');
_R0('379','SI','斯洛文尼亚','Slovenia');
_R0('380','TT','特立尼达和多巴哥','Trinidad and tobago');
_R0('382','UZ','乌兹别克斯坦','Uzbekistan');
_R0('383','NC','新喀里多尼亚','New caledonia');
_R0('384','ID','印度尼西亚','Indonesia');
_R0('385','VG','英属维尔京','Virgin islands, u.s.');
_R0('388','BF','布基纳法索','Burkina faso');
_R0('389','ET','埃塞俄比亚','Ethiopia');
_R0('390','PF','法属波利尼西亚','French polynesia');
_R0('391','GF','法属圭亚那','French guiana');
_R0('395','KI','基里巴斯','Kiribati');
_R0('397','SO','索马里','Somalia');
_R0('398','ER','厄立特里亚','Eritrea');
_R0('400','GD','格林纳达','Grenada');
_R0('401','AI','安圭拉','Anguilla');
_R0('404','GW','几内亚比绍','Guinea-bissau');
_R0('408','LC','圣卢西亚','Saint lucia');
_R0('410','JE','泽西岛','Jersey');
_R0('411','GG2','格恩西岛','Guernsey2');
_R0('413','XI','马德拉群岛','Madeira islands');
_R0('414','XH','亚速尔群岛','Azores');
_R0('415','XJ','巴利阿里群岛','Balearic islands');
_R0('419','MF','圣马丁','Saint martin');
_R0('533','LS','莱索托','Lesotho');
_R0('564','WF','瓦利斯和富图纳群岛','Wallis and futuna');
_R0('715','MP','北马里亚纳群岛','Northern mariana islands');
_R0('823','ME','黑山','Montenegro');
_R0('2714','IC','加那利群岛','Canary islands');
_R0('2747','VA','梵蒂冈','Vatican city state');
_R0('2750','FK','福克兰群岛','Falkland islands');
_R0('2751','TJ','塔吉克斯坦','Tajikistan');
_R0('2753','GQ','赤道几内亚','Equatorial guinea');
_R0('2754','VC','圣文森特和格林纳丁斯','Saint vincent and the grenadines');
_R0('2756','YT','法属马约特岛','Territorial collectivity of mayotte');
_R0('2758','TC','特克斯和凯科斯群岛','Turks and caicos islands');
_R0('2759','SB','所罗门群岛','Solomon islands');
_R0('2760','XN','尼维斯','The federation of nevis');
_R0('2763','EH','西撒哈拉','Western sahara');
_R0('2764','TM','土库曼斯坦','Turkmenistan');
_R0('2765','CV','佛得角','Cape verde');
_R0('2766','NR','瑙鲁共和国','Nauru');
_R0('2767','MP','塞班岛','Saipan lsland');
_R0('2771','NF','诺福克岛','Norfolk island');
_R0('2772','YK','科索沃','Kosovo');
_R0('2776','CW','库拉索','Curacao');
_R0('2777','GG','根西岛','Guernsey');
_R0('2778','KN','圣基茨和尼维斯联邦','Saint kitts and nevis');
_R0('2779','MS','蒙特塞拉特','Montserrat');
_R0('2780','CD','刚果(金)','The democratic republic of the congo');
_R0('2781','CG','刚果(布)','Congo');
_R0('2782','Z2','阿森松岛','Ascension island');
_R0('2783','TL','东帝汶','Timor-leste');
_R0('2785','NU','纽埃','Niue');
_R0('2786','PN','皮特凯恩群岛','Pitcairn islands');
_R0('2787','CX','圣诞岛','Christmas island');
_R0('2788','ST','圣多美和普林西比','Sao tome and principe');
_R0('2789','SH','圣赫勒拿','Saint helena');
_R0('2790','TV','图瓦卢','Tuvalu');
_R0('2791','TK','托克劳','Tokelau');
_R0('2793','IM','马恩岛','Isle of man');
_R0('2795','MP','马里亚纳群岛','Mariana lslands');
_R0('2816','DO','多米尼加共和国','Dominican republic');
_R0('2826','GS','南乔治亚岛和南桑威奇群岛','South georgia and the south sandwich islands');
_R0('2828','IO','英属印度洋领地','British indian ocean territory');
_R0('2834','UM','美国本土外小岛屿','United states minor outlying islands');
_R0('2881','RA','美国偏远地区','Remote area of us');