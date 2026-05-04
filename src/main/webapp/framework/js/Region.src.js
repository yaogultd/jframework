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
			console.log('0000000000');
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