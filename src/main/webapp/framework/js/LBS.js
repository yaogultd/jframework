//地图相关
let LBS={
   /**
    * 距离转换成km（如果小于1000则以m为单位显示）
    * @param distance
    */
   formatDistance:function(distance){
      if(distance<1000) return distance+'m';
      else return (distance/1000).toFixed(2)+'km';
   },

   /**
    * 经纬度
    * @param longitude
    * @param latitude
    * @constructor
    */
   LngLat:function(longitude, latitude){
      this.longitude=(typeof longitude)=='string'?longitude*1:longitude;
      this.latitude=(typeof latitude)=='string'?latitude*1:latitude;
   },

   /**
    *
    * @param status 定位状态（complete | error)
    * @param code 定位结果编码
    * @param message 定位结果提示信息
    * @param position 经纬度：LBS.LngLat
    * @param accuracy 定位精度（米）
    * @param altitude 海拔
    * @param altitudeAccuracy 海拔精度（米）
    * @param heading 方向
    * @param speed 速度
    * @param addressCode
    * @param countryName
    * @param provinceName
    * @param cityName
    * @param cityCode
    * @param countyName
    * @param zoneName
    * @param formattedAddress 格式化的地址
    * @param callback 回调
    * @param callbackOriginalParams 回调方法的原始参数（原样回传）
    * @param callbackTarget
    * @constructor
    */
   Locating:function(status,
                     code,
                     message,
                     position,
                     accuracy,
                     altitude,
                     altitudeAccuracy,
                     heading,
                     speed,
                     addressCode,
                     countryName,
                     provinceName,
                     cityName,
                     cityCode,
                     countyName,
                     zoneName,
                     formattedAddress,
                     callback,
                     callbackOriginalParams,
                     callbackTarget){
      this.status=status;
      this.code=code;
      this.message=message;
      this.callback=callback;
      this.callbackOriginalParams=callbackOriginalParams;
      this.callbackTarget=callbackTarget;
      Logger.log('LBS.Locating callback = '+callback);

      if(this.status==0) {//定位成功
         if (!position) this.position = null;
         else if (Array.isArray(position) && position.length == 2) {
            this.position = new LBS.LngLat(position[0], position[1]);
         } else if ((typeof position.getLng) != 'undefined') {
            this.position = new LBS.LngLat(position.getLng(), position.getLat());
         }else{
            this.position=position;
         }

         this.accuracy = accuracy;
         this.altitude = altitude;
         this.altitudeAccuracy = altitudeAccuracy;
         this.heading = heading;
         this.speed = speed;

         //关联地址信息
         if(Str.isBlank(provinceName)){
            this.address = null;
            if(callback) callback(callbackOriginalParams, this);
         }else{
            this.address = new LBS.Address(addressCode, countryName, provinceName, cityName, cityCode, countyName, zoneName, formattedAddress, position, [], this.setAddress, null, this);
         }
      }
   },

   /**
    * 定位地址（通过经纬度、ip反向编码得到）
    * @param addressCode 地址编码
    * @param countryName 国家
    * @param provinceName 省份/州
    * @param cityName 城市
    * @param cityCode 城市编码
    * @param countyName 区县
    * @param zoneName 街道
    * @param formattedAddress 格式化的地址
    * @param position 经纬度
    * @param pois [LBS.POI, LBS.POI...]
    * @param callback 回调
    * @param callbackOriginalParams 回调方法的原始参数（原样回传）
    * @param callbackTarget
    * @param toDecodeAddress 是否解析地址
    * @constructor
    */
   Address:function(addressCode,
                    countryName,
                    provinceName,
                    cityName,
                    cityCode,
                    countyName,
                    zoneName,
                    formattedAddress,
                    position,
                    pois,
                    callback,
                    callbackOriginalParams,
                    callbackTarget,
                    toDecodeAddress){
      this.addressCode=addressCode;
      this.countryName=countryName;
      this.provinceName=provinceName;
      this.cityName=cityName;
      this.cityCode=cityCode;
      this.countyName=countyName;
      this.zoneName=zoneName;
      this.formattedAddress=formattedAddress;
      if(!position) this.position=null;
      else if(Array.isArray(position) && position.length==2){
         this.position=new LBS.LngLat(position[0], position[1]);
      }else if((typeof position.getLng) != 'undefined'){
         this.position=new LBS.LngLat(position.getLng(), position.getLat());
      }else{
         this.position=position;
      }
      this.pois=pois;
      this.callback=callback;
      this.callbackOriginalParams=callbackOriginalParams;
      this.callbackTarget=callbackTarget;

      if((typeof toDecodeAddress)!='boolean') toDecodeAddress=true;

      //如果引入了/framework/js/Region.js，尝试获得精确地域信息
      this.regions=[];//[国家、省/州、市、区县、乡镇]
      if(toDecodeAddress && (typeof Regions) != 'undefined'){
         Logger.log('try to decodeAddress....');
         this.regions=Regions.decodeAddress(null, this.addressCode, this.provinceName, this.cityName, this.countyName, this.zoneName, this.setRegions, this);
      }else if(this.callback){
         this.callback.call(this.callbackTarget?this.callbackTarget:window, this.callbackOriginalParams, this);
      }

      this.pois=[];
   },

   /**
    * @param center 查询中心点（LBS.LngLat）
    * @param id
    * @param name
    * @param position 所处经纬度
    * @param distance 与查询点之间直线距离（米）
    * @param types [所属行业类型, 所属行业类型]
    * @param addressText 地址
    * @param tels [电话, 电话]
    * @param direction 相对查询点方向（东南西北）
    * @param businessArea 业务范围
    * @param website 网站
    * @param photos 相关图片 [LBS.Photo]
    * @param address LBS.Address
    * @constructor
    */
   POI:function(center,
                id,
                name,
                position,
                distance,
                types,
                addressText,
                tels,
                direction,
                businessArea,
                website,
                photos,
                address){
      if(!center) this.center=null;
      else if(Array.isArray(center) && center.length==2){
         this.center=new LBS.LngLat(center[0], center[1]);
      }else if((typeof center.getLng) != 'undefined'){
         this.center=new LBS.LngLat(center.getLng(), center.getLat());
      }else{
         this.center=center;
      }

      this.id=id;
      this.name=name;
      if(!position) this.position=null;
      else if(Array.isArray(position) && position.length==2){
         this.position=new LBS.LngLat(position[0], position[1]);
      }else if((typeof position.getLng) != 'undefined'){
         this.position=new LBS.LngLat(position.getLng(), position.getLat());
      }else{
         this.position=position;
      }
      this.distance=distance;

      if((typeof types)=='string') this.types=types.split(';');
      else this.types=types;

      this.addressText=addressText;

      if((typeof tels)=='string') this.tels=tels.split(';');
      else this.tels=tels;

      this.direction=LBS.getDirection(direction);
      this.businessArea=businessArea;
      this.website=website;
      this.photos=photos;
      this.address=address;
   },

   /**
    * 图片
    * @param title
    * @param url
    * @constructor
    */
   Photo:function (title, url){
      this.title=title;
      this.url=url;
   },

   /**
    * 搜索结果
    * @param total 符合条件的记录数
    * @param rpp 每页多少条
    * @param pn 第几页
    * @param pois poi列表 [LBS.POI]
    * @constructor
    */
   POISearchResult:function(total, pn, rpp, pois){
      this.total=total;
      this.rpp=rpp;
      this.pn=pn;
      this.pois=pois;
      this.totalPages=1;

      if(this.total>this.rpp){
         if(this.total%this.rpp==0) this.totalPages=this.total/this.rpp;
         else this.totalPages=Math.floor(this.total/this.rpp) + 1;
      }else{
         this.totalPages=1;
      }
   },

   /**
    * poi搜索组件
    * @param container 容器
    * @param id
    * @param width 组件宽度（不设置默认200）
    * @param selector 选中组件（JSelector），不指定则自动创建
    * @param poiTypes
    * @param city
    * @param radius
    * @param rpp 每页显示多少条
    * @param onSelected poi被选中时回调
    * @param onSelectedTarget 调用回调方法的对象（默认window）
    * @constructor
    */
   POISeacher:function(container, id, width, selector, poiTypes, city, radius, rpp, onSelected, onSelectedTarget){
      this.container=(typeof container)=='string' ? _$(container) : container;
      this.id=id;
      this.width=((typeof width)!='number' || width<=0 ? 200: width);
      this.selector=selector;
      this.poiTypes=poiTypes;
      this.city=city;
      this.radius=radius;
      this.rpp=rpp;
      this.pn=1;//当前第几页
      this.poiSelected=null;//当前选中的poi
      this.onSelected=onSelected;
      this.onSelectedTarget=onSelectedTarget;
      this.center=null;
      this.result=null;//搜索结果（POISearchResult）
      this.build();
   },

   /**
    * 拖曳选址组件
    * @param id
    * @param mode 拖拽模式，可选'dragMap'、'dragMarker'，默认为'dragMap'
    * @param callback
    * @param callbackTarget
    * @constructor
    */
   PositionPicker:function (id, mode, callback, callbackTarget){
      this.id=id;
      this.mode=mode;
      this.callback=callback;
      this.callbackTarget=callbackTarget;
   },

   //检查资源就绪的时间间隔（毫秒）
   checkReadyStateInterval: 1000,

   //每次方法调用的参数
   _args: [],

   //地图服务提供商
   provider:null,

   //各插件[{loaded:true|false, plugin:object, ...}]
   plugins:[],

   //js是否加载完毕
   jsLoaded:false,

   //地图是否加载完毕
   mapLoaded:false,

   /**
    *
    * @param direction
    * @returns {string|*}
    */
   getDirectionName:function (direction){
      if('E'==direction) return '东';
      else if('S'==direction) return '南';
      else if('W'==direction) return '西';
      else if('N'==direction) return '北';
      return direction;
   },

   /**
    *
    * @param directionName
    * @returns {string|*}
    */
   getDirection:function (directionName){
      if('东'==directionName) return 'E';
      else if('南'==directionName) return 'S';
      else if('西'==directionName) return 'W';
      else if('北'==directionName) return 'N';
      return directionName;
   },

   /**
    * 
    * @param args
    * @returns {{}}
    */
   formatArgs:function (args){
      if((typeof args)=='undefined') args={};
      return args;
   },

   /**
    * 保存某次方法调用的参数
    * @param args
    */
   saveArgs:function(args){
      if(!args) return;
      if(args.invokeUuid) return args.invokeUuid;

      args.invokeUuid='LBS.invoke.'+Math.random();
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
    * 开始加载某个插件
    * @param pluginId
    */
   loadPlugin:function (pluginId){
      LBS.plugins[pluginId]={
         loaded: false,
         object: null,
         data: null
      };
   },

   /**
    * 插件加载完毕
    */
   onPluginLoaded:function(pluginId, pluginObject, pluginData){
      LBS.plugins[pluginId]={
         loaded: true, 
         object: pluginObject,
         data: pluginData
      };
   },

   /**
    *
    * @param pluginId
    * @returns {boolean}
    */
   pluginExists:function(pluginId){
      return ((typeof this.plugins[pluginId]) != 'undefined');
   },

   /**
    * 检查插件是否已经加载
    * @param pluginId
    */
   checkPlugins:function (pluginIds){
      if((typeof pluginIds) == 'string'){
         let plugin=this.plugins[pluginIds];
         return plugin && plugin.loaded;
      }else if(Array.isArray(pluginIds)){
         let ready=true;
         for(let i=0; i<pluginIds.length; i++){
            let plugin=this.plugins[pluginIds[i]];
            ready = ready && plugin && plugin.loaded;
         }
         return ready;
      }
      return false;
   },

   /**
    *
    * @param pluginId
    * @returns {*}
    */
   getPlugin:function (pluginId){
      return this.plugins[pluginId];
   },

   /**
    * 等待插件就绪
    * @param invokeMethod
    * @param args
    * @param pluginIds
    * @returns {null|*}
    */
   waitForPluginReady:function(invokeMethod, args, pluginIds){
      args=LBS.restoreArgs(args);

      if(!LBS.checkPlugins(pluginIds)){
         Logger.log('waitForPluginReady before invoking '+invokeMethod);
         let invokeUuid=LBS.saveArgs(args);
         if(invokeUuid) setTimeout(invokeMethod+"('"+invokeUuid+"')", LBS.checkReadyStateInterval);
         else setTimeout(invokeMethod+"()", LBS.checkReadyStateInterval);
         return null;
      }
      if(args && args.invokeUuid) LBSAmap.deleteArgs(args.invokeUuid);

      return args;
   },

   /**
    * js加载完毕
    */
   onJsLoaded:function(){
      LBS.jsLoaded=true;
   },

   /**
    * js是否加载完成
    */
   checkJs:function(){
      return this.jsLoaded;
   },

   /**
    * 等待js就绪
    * @param invokeMethod
    * @param args
    * @returns {null|*}
    */
   waitForJsReady:function(invokeMethod, args){
      args=LBS.restoreArgs(args);

      if(!LBS.checkJs()){
         let invokeUuid=LBS.saveArgs(args);
         if(invokeUuid) setTimeout(invokeMethod+"('"+invokeUuid+"')", LBS.checkReadyStateInterval);
         else setTimeout(invokeMethod+"()", LBS.checkReadyStateInterval);
         return null;
      }
      if(args && args.invokeUuid) LBS.deleteArgs(args.invokeUuid);

      return args;
   },

   /**
    * 地图加载完毕
    */
   onMapLoaded:function(){
      LBS.mapLoaded=true;
   },

   /**
    * 地图是否加载完成
    */
   checkMap:function(){
      return this.mapLoaded;
   },

   /**
    * 等待地图就绪
    * @param invokeMethod
    * @param args
    * @returns {null|*}
    */
   waitForMapReady:function(invokeMethod, args){
      args=LBS.restoreArgs(args);

      if(!LBS.checkMap()){
         Logger.log('waitForMapReady before invoking '+invokeMethod);
         let invokeUuid=LBS.saveArgs(args);
         if(invokeUuid) setTimeout(invokeMethod+"('"+invokeUuid+"')", LBS.checkReadyStateInterval);
         else setTimeout(invokeMethod+"()", LBS.checkReadyStateInterval);
         return null;
      }
      if(args && args.invokeUuid) LBS.deleteArgs(args.invokeUuid);

      return args;
   },

   /**
    *
    * @param provider 指定地图服务提供商
    */
   setProvider:function (provider){
      if(!this.provider || this.provider!=provider){
         this.jsLoaded=false;
         this.mapLoaded=false;
         this.provider=provider;
      }
   },

   /**
    * 初始化
    * @param provider 指定地图服务提供商
    * @param args 地图服务初始化所需参数
    */
   init:function(args){
      args=LBS.formatArgs(args);
      if(!this.provider){
         Logger.log('no LBS provider.');
         return;
      }

      //已经初始化
      if(this.jsLoaded) return;

      //初始化
      LBS.provider.init(args);
   },

   /**
    * 设置地图参数
    * @param args
    */
   setMapOptions: function(args){
      args=LBS.formatArgs(args);
      if(!this.provider){
         Logger.log('no LBS provider.');
         return;
      }
      LBS.provider.setMapOptions(args);
   },

   /**
    * 创建地图
    * @param args
    */
   createMap: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkJs()){
         args=LBS.waitForJsReady('LBS.createMap', args);
         return;
      }
      LBS.provider.createMap(LBS.restoreArgs(args));
   },

   /**
    * 创建拖曳选址组件并启动
    * @param args
    */
   startPicker: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.startPicker', args);
         return;
      }
      LBS.provider.startPicker(LBS.restoreArgs(args));
   },

   /**
    * 停止拖曳选址组件
    * @param args
    */
   stopPicker: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.stopPicker', args);
         return;
      }
      LBS.provider.stopPicker(LBS.restoreArgs(args));
   },

   /**
    * 点标记
    * @param args
    */
   mark: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.mark', args);
         return;
      }
      LBS.provider.mark(LBS.restoreArgs(args));
   },

   /**
    * 海量点标记
    * @param args
    */
   markMass: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.markMass', args);
         return;
      }
      LBS.provider.markMass(LBS.restoreArgs(args));
   },

   /**
    * 折线/多边形
    * @param args
    */
   line: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.line', args);
         return;
      }
      LBS.provider.line(LBS.restoreArgs(args));
   },

   /**
    * 矩形标记
    * @param args
    */
   rectangle: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.rectangle', args);
         return;
      }
      LBS.provider.rectangle(LBS.restoreArgs(args));
   },

   /**
    * 圆形标记
    * @param args
    */
   circle: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.circle', args);
         return;
      }
      LBS.provider.circle(LBS.restoreArgs(args));
   },

   /**
    * 椭圆标记
    * @param args
    */
   ellipse: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.ellipse', args);
         return;
      }
      LBS.provider.ellipse(LBS.restoreArgs(args));
   },

   /**
    * 信息窗体
    * @param args
    */
   infoWindow: function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkMap()){
         args=LBS.waitForMapReady('LBS.infoWindow', args);
         return;
      }
      LBS.provider.infoWindow(LBS.restoreArgs(args));
   },

   /**
    * 当前定位
    * @param args
    */
   getCurrentPosition:function(args){
      args=LBS.formatArgs(args);
      if(args.mapRequired){//需要配合地图一起使用
         if(!LBS.checkMap()){
            args=LBS.waitForMapReady('LBS.getCurrentPosition', args);
            return;
         }
      }else{
         if(!LBS.checkJs()){
            args=LBS.waitForJsReady('LBS.getCurrentPosition', args);
            return;
         }
      }

      LBS.provider.getCurrentPosition(LBS.restoreArgs(args));
   },

   /**
    * 根据地址获得所在经纬度
    * @param args
    */
   addressToPosition:function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkJs()){
         args=LBS.waitForJsReady('LBS.addressToPosition', args);
         return;
      }

      LBS.provider.addressToPosition(LBS.restoreArgs(args));
   },

   /**
    * 根据经纬度获得地址信息
    * @param args
    */
   positionToAddress:function(args){
      args=LBS.formatArgs(args);
      if(!LBS.checkJs()){
         args=LBS.waitForJsReady('LBS.positionToAddress', args);
         return;
      }
      LBS.provider.positionToAddress(LBS.restoreArgs(args));
   },

   /**
    * 搜索
    * @param args
    */
   search:function (args){
      args=LBS.formatArgs(args);
      if(args.mapRequired) {//需要配合地图一起使用
         if(!LBS.checkMap()){
            args=LBS.waitForMapReady('LBS.search', args);
            return;
         }
      }else{
         if(!LBS.checkJs()){
            args=LBS.waitForJsReady('LBS.search', args);
            return;
         }
      }
      LBS.provider.search(LBS.restoreArgs(args));
   },

   /**
    * 搜索指定点附近
    * @param args
    */
   searchNearBy:function (args){
      if(args.mapRequired) {//需要配合地图一起使用
         if(!LBS.checkMap()){
            args=LBS.waitForMapReady('LBS.searchNearBy', args);
            return;
         }
      }else{
         if(!LBS.checkJs()){
            args=LBS.waitForJsReady('LBS.searchNearBy', args);
            return;
         }
      }
      LBS.provider.searchNearBy(LBS.restoreArgs(args));
   },

   /**
    * 搜索指定范围内
    * @param args
    */
   searchInBounds:function (args){
      args=LBS.formatArgs(args);
      if(args.mapRequired) {//需要配合地图一起使用
         if(!LBS.checkMap()){
            args=LBS.waitForMapReady('LBS.searchInBounds', args);
            return;
         }
      }else{
         if(!LBS.checkJs()){
            args=LBS.waitForJsReady('LBS.searchInBounds', args);
            return;
         }
      }

      LBS.provider.searchInBounds(LBS.restoreArgs(args));
   },

   /**
    * 计算两点间距离
    * @param args
    */
   getDistance:function (args){
      args=LBS.formatArgs(args);
      if(!LBS.checkJs()) return 0;
      return LBS.provider.getDistance(LBS.restoreArgs(args));
   }
}

/**
 *
 * @returns {string}
 */
LBS.LngLat.prototype.toString=function(){
   let s=[];
   s.push('{"longitude":'+this.longitude);
   s.push(',"latitude":'+this.latitude);
   s.push('}');
   return s.join('');
}

/**
 *
 * @param args
 * @param address
 */
LBS.Locating.prototype.setAddress=function(args, address){
   Logger.log('LBS.Locating.setAddress loaded...');
   Logger.log('LBS.Locating.setAddress callback = '+this.callback);
   Logger.log('LBS.Locating.setAddress callbackTarget = '+this.callbackTarget);
   Logger.log('LBS.Locating.setAddress callbackOriginalParams = '+this.callbackOriginalParams);
   this.address=address;
   if(this.callback) this.callback.call(this.callbackTarget?this.callbackTarget:window, this.callbackOriginalParams, this);
}

/**
 *
 * @param regions
 */
LBS.Address.prototype.setRegions=function(regions){
   // Logger.log('LBS.Address.setRegions loaded...');
   // Logger.log('LBS.Address.setRegions callback = '+(typeof this.callback));
   // Logger.log('LBS.Address.setRegions callback = '+this.callback);
   // Logger.log('LBS.Address.setRegions callbackTarget = '+this.callbackTarget);
   // Logger.log('LBS.Address.setRegions callbackOriginalParams = '+this.callbackOriginalParams);
   this.regions=regions;
   if(this.callback){
      this.callback.call(this.callbackTarget?this.callbackTarget:window, this.callbackOriginalParams, this);
   }
}

/**
 *
 */
LBS.Address.prototype.decodeAddress=function(){
   if((typeof Regions) != 'undefined'){
      Logger.log('try to decodeAddress....');
      this.regions=Regions.decodeAddress(null, this.addressCode, this.provinceName, this.cityName, this.countyName, this.zoneName, this.setRegions, this);
   }
}

/**
 *
 * @returns {string}
 */
LBS.Address.prototype.toString=function(){
   let s=[];
   s.push('{"addressCode":"'+(this.addressCode?this.addressCode:'')+'"');
   s.push(',"countryName":"'+JSONUtil.convert(this.countryName?this.countryName:'')+'"');
   s.push(',"provinceName":"'+JSONUtil.convert(this.provinceName?this.provinceName:'')+'"');
   s.push(',"cityName":"'+JSONUtil.convert(this.cityName?this.cityName:'')+'"');
   s.push(',"cityCode":"'+(this.cityCode?this.cityCode:'')+'"');
   s.push(',"countyName":"'+JSONUtil.convert(this.countyName?this.countyName:'')+'"');
   s.push(',"zoneName":"'+JSONUtil.convert(this.zoneName?this.zoneName:'')+'"');
   s.push(',"formattedAddress":"'+JSONUtil.convert(this.formattedAddress?this.formattedAddress:'')+'"');
   if(this.position){
      s.push(',"position":'+this.position.toString());
   }
   if(this.regions){
      s.push(',"regions":[');
      for(let i=0; i<this.regions.length; i++){
         if(i>0) s.push(',');
         s.push(this.regions[i].toString());
      }
      s.push(']');
   }
   if(this.pois){
      s.push(',"pois":[');
      for(let i=0; i<this.pois.length; i++){
         if(i>0) s.push(',');
         s.push(this.pois[i].toString());
      }
      s.push(']');
   }
   s.push('}');
   return s.join('');
}

/**
 *
 */
LBS.POISeacher.prototype.build=function(){
   if(!this.selector){
      this.selector=new JSelector(this.container,
          this.id+'_selector',
          this.width,
          null,
          0,
          120,
          [['0', 'I{js,点击搜索}']],
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          this.select,
          this,
          true,
          this.search,
          this,
          this.nextPage,
          this);

      this.selector.startIndexToShow=1;
      this.selector.build();
   }
}

/**
 * 设置搜索中心点
 * @param position
 */
LBS.POISeacher.prototype.setCenter=function (position){
   if(!position) this.center=null;
   else if(Array.isArray(position) && position.length==2){
      this.center=new LBS.LngLat(position[0], position[1]);
   }else if((typeof position.getLng) != 'undefined'){
      this.center=new LBS.LngLat(position.getLng(), position.getLat());
   }else{
      this.center=position;
   }
}

/**
 *
 * @param id
 * @returns {null|*}
 */
LBS.POISeacher.prototype.findPOI=function(id){
   if(!this.result) return null;
   for(let i=0; i<this.result.pois.length; i++){
      if(this.result.pois[i].id==id) return this.result.pois[i];
   }
   return null;
}

/**
 *
 * @param selectorId
 * @param itemId
 * @param itemText
 */
LBS.POISeacher.prototype.select=function(selectorId, itemId, itemText){
   if(Str.isBlank(itemId)) return;
   this.poiSelected=this.findPOI(itemId);
   if(this.poiSelected){
      LBS.infoWindow({
         clearMap: false,
         fitView: false,
         toCenter: true,
         position: this.poiSelected.position,
         title: this.poiSelected.name,
         content: this.poiSelected.desc(),
         offset: [0, -20]
      });
   }
   if(this.onSelected) this.onSelected.call(this.onSelectedTarget?this.onSelectedTarget:window, this.poiSelected);
}

/**
 * 点击poi marker
 * @param e
 */
LBS.POISeacher.prototype.markerClick=function(e){
   let itemId=e.id;
   if(Str.isBlank(itemId)) return;
   this.poiSelected=this.findPOI(itemId);
   if(this.poiSelected){
      this.selector.setCurrent([this.poiSelected.id, this.poiSelected.name], true, true)
      this.select(null, itemId, null);
   }
}

/**
 *
 * @param selectorInstance
 */
LBS.POISeacher.prototype.search=function(selectorInstance){
   if(Str.isBlank(this.selector.getSearchKeywords())) return;

   Logger.log('LBS.POISeacher['+this.id+'] search first page...');
   
   this.pn=1;
   if((typeof this.radius) && this.radius>0){
      LBS.searchNearBy({markerClickCallback: this.markerClick,
         markerClickCallbackTarget: this,
         center: this.center?this.center:(LBSAmap.initLocating?LBSAmap.initLocating.position:null),
         city: this.city,
         type: this.poiTypes,
         keywords: this.selector.getSearchKeywords(),
         radius: this.radius,
         pageSize: this.rpp,
         pageIndex: this.pn,
         onComplete: this.onSearch,
         onError: this.onSearch,
         callbackTarget: this});
   }else{
      LBS.search({markerClickCallback: this.markerClick,
         markerClickCallbackTarget: this,
         center: this.center?this.center:(LBSAmap.initLocating?LBSAmap.initLocating.position:null),
         city: this.city,
         type: this.poiTypes,
         keywords: this.selector.getSearchKeywords(),
         pageSize: this.rpp,
         pageIndex: this.pn,
         onComplete: this.onSearch,
         onError: this.onSearch,
         callbackTarget: this});
   }
}

/**
 * 搜索回调
 * @param args
 * @param searchResult
 */
LBS.POISeacher.prototype.onSearch=function(args, searchResult){
   if(this.pn==1){//搜索第一页，清除旧列表
      this.result=searchResult;
   }

   if(!searchResult || !searchResult.pois || searchResult.pois.length==0){
      if(this.pn==1){
         Logger.log('未搜索到poi');
         this.selector.setItems([['0', 'I{js,点击搜索}']]);
         this.selector.setCurrent(['0', 'I{js,点击搜索}'], true, true);
      }else{
         Logger.log('未搜索到poi（已到末页）');
      }
      return;
   }

   Logger.log('搜索到poi（共'+searchResult.total+'条记录）');
   this.result.pn=this.pn;
   for(let i in searchResult.pois){
      let poi=searchResult.pois[i];
      if(this.pn>1) this.result.pois.push(searchResult.pois[i]);//追加到结果
      this.selector.addItem([poi.id, poi.name]);
   }
   for(let i=0; i<this.result.pois.length; i++){
      //Logger.log('搜索到的poi '+i+' = '+this.result.pois[i]);
   }
   this.selector.buildList();
}

/**
 * 下一页
 * @param selectorInstance
 */
LBS.POISeacher.prototype.nextPage=function(selectorInstance){
   if(Str.isBlank(this.selector.getSearchKeywords())) return;

   this.pn++;
   if(this.result && this.pn > this.result.totalPages){
      this.pn=this.totalPages;
      return;
   }

   Logger.log('LBS.POISeacher['+this.id+'] search page -> '+this.pn);

   if((typeof this.radius) && this.radius>0){
      LBS.searchNearBy({center: this.center?this.center:LBSAmap.initLocating.position,
         city: this.city,
         keywords: this.selector.getSearchKeywords(),
         radius: this.radius,
         pageSize: this.rpp,
         pageIndex: this.pn,
         onComplete: this.onSearch,
         onError: this.onSearch,
         callbackTarget: this});
   }else{
      LBS.search({center: this.center?this.center:LBSAmap.initLocating.position,
         city: this.city,
         keywords: this.selector.getSearchKeywords(),
         pageSize: this.rpp,
         pageIndex: this.pn,
         onComplete: this.onSearch,
         onError: this.onSearch,
         callbackTarget: this});
   }
}

/**
 *
 */
LBS.PositionPicker.prototype.start=function(args){
   if(!args) args={};
   args.mode = this.mode;
   args.callback = this.callback;
   args.callbackTarget = this.callbackTarget;

   LBS.startPicker(args);
}

/**
 *
 */
LBS.PositionPicker.prototype.stop=function(){
   LBS.stopPicker({});
}

/**
 *
 * @returns {string}
 */
LBS.POI.prototype.toString=function(){
   let s=[];
   s.push('{"id":"'+(this.id?this.id:'')+'"');
   s.push(',"name":"'+JSONUtil.convert(this.name?this.name:'')+'"');
   if(this.center){
      s.push(',"center":'+this.center.toString());
   }
   if(this.position){
      s.push(',"position":'+this.position.toString());
   }
   s.push(',"distance":"'+(this.distance?this.distance:'0')+'"');
   if(this.types){
      s.push(',"types":[');
      for(let i=0; i<this.types.length; i++){
         if(i>0) s.push(',');
         s.push('"'+JSONUtil.convert(this.types[i])+'"');
      }
      s.push(']');
   }
   s.push(',"addressText":"'+JSONUtil.convert(this.addressText?this.addressText:'')+'"');
   if(this.tels){
      s.push(',"tels":[');
      for(let i=0; i<this.tels.length; i++){
         if(i>0) s.push(',');
         s.push('"'+JSONUtil.convert(this.tels[i])+'"');
      }
      s.push(']');
   }
   s.push(',"businessArea":"'+JSONUtil.convert(this.businessArea?this.businessArea:'')+'"');
   s.push(',"direction":"'+(this.direction?this.direction:'')+'"');
   s.push(',"website":"'+JSONUtil.convert(this.website?this.website:'')+'"');
   if(this.photos){
      s.push(',"photos":[');
      for(let i=0; i<this.photos.length; i++){
         if(i>0) s.push(',');
         s.push(this.photos[i].toString());
      }
      s.push(']');
   }
   if(this.address){
      s.push(',"address":'+this.address.toString());
   }
   s.push('}');
   return s.join('');
}

/**
 * 简要描述
 */
LBS.POI.prototype.desc=function(){
   let s=[];
   s.push('<div>I{js,地址}: '+this.addressText+'</div>');
   if(this.tels && this.tels.length>0 && !Str.isBlank(this.tels[0])){
      s.push('<div>I{js,电话}: '+this.tels.join(',')+'</div>');
   }
   if(this.distance && this.distance>0){
      s.push('<div>I{js,距离}: '+LBS.formatDistance(this.distance)+'</div>');
   }
   return Lang.convert(s.join(''));
}

/**
 *
 * @returns {string}
 */
LBS.Photo.prototype.toString=function(){
   let s=[];
   s.push('{"title":"'+JSONUtil.convert(this.title?this.title:'')+'"');
   s.push(',"url":"'+JSONUtil.convert(this.url?this.url:'')+'"');
   s.push('}');
   return s.join('');
}

/**
 * 地图服务提供商 - 高德地图
 * clearMap, fitView, toCenter为高德地图各功能方法中的保留参数，分别表示是否清除地图、是否缩放使所有标记物可见、是否将当前创建的标记物置于地图中心
 */
let LBSAmap={
   DEFAULT_KEY: 'cf8ad4a38a55092c40c7cc815fcc4f40',
   DEFAULT_SECRET: '',
   DEFAULT_VERSION: '2.0',
   DEFAULT_UI_VERSION: '1.1',
   DEFAULT_ZOOM: 12,
   DEFAULT_POI_TYPES: '餐饮服务|商务住宅|生活服务',
   ALL_POI_TYPES: ['汽车服务',
      '汽车销售',
      '汽车维修',
      '摩托车服务',
      '餐饮服务',
      '购物服务',
      '生活服务',
      '体育休闲服务',
      '医疗保健服务',
      '住宿服务',
      '风景名胜',
      '商务住宅',
      '政府机构及社会团体',
      '科教文化服务',
      '交通设施服务',
      '金融保险服务',
      '公司企业',
      '道路附属设施',
      '地名地址信息',
      '公共设施'],

   //是否正在加载
   initializing: false,

   //地图容器
   container: null,

   //地图对象(AMap.Map)
   map: null,

   //地图对象初始参数
   mapOptions: {
      center: null,
      zoom: 12
   },

   //初始定位位置
   initLocating: null,

   /**
    * 初始化
    * @param args 初始化所需参数 {container, version, key, uiVerion, zoom, [center], callback}
    */
   init:function(args){
      if(this.initializing) return;
      this.initializing=true;

      if((typeof AMap)=='undefined'){
         this.container=args.container;

         let key=args.key;
         let secret=args.secret;
         let version=args.version;
         let uiVerion=args.uiVerion;
         let zoom=args.zoom;
         if(Str.isBlank(key)) key=this.DEFAULT_KEY;
         if(Str.isBlank(secret)) secret=this.DEFAULT_SECRET;
         if(Str.isBlank(version)) version=this.DEFAULT_VERSION;
         if(Str.isBlank(uiVerion)) uiVerion=this.DEFAULT_UI_VERSION;
         if((typeof zoom)!='number' || zoom<=0) zoom=this.DEFAULT_ZOOM;

         window._AMapSecurityConfig = {
            serviceHost: Str.appendUrl(UserAgent.currentUrlBase, '/_AMapService')
            //securityJsCode:secret
         }

         this.mapOptions.zoom=zoom;
         if(args.center){
            if(Array.isArray(args.center)) args.center=new LBS.LngLat(args.center[0], args.center[1]);
            this.mapOptions.center=[args.center.longitude, args.center.latitude];
         }

         new QueuedJS(['https://webapi.amap.com/maps?v='+version+'&key='+key, 'https://webapi.amap.com/ui/'+uiVerion+'/main.js'],
             ['utf-8', 'utf-8'],
             LBSAmap.onJsLoaded);
      }else{
         this.initializing=false;
      }
   },

   /**
    * 初始化（加载js）完成
    */
   onJsLoaded:function(){
      LBS.onJsLoaded();
      this.initializing=false;
   },

   /**
    * 首次自动定位时
    * @param args
    * @param locating 初始定位信息
    */
   onAutoLocated:function(args, locating){
      if(locating) LBSAmap.initLocating=locating;
      if(args.onInit) args.onInit(args, locating);
   },

   /**
    * 创建地图
    * @param args
    */
   createMap:function(args){
      if(args){
         if(args.container) this.container=args.container;

         let zoom=args.zoom;
         if((typeof zoom)!='number' || zoom<=0) zoom=this.DEFAULT_ZOOM;

         this.mapOptions.zoom=zoom;
         if(args.center){
            if(Array.isArray(args.center)) args.center=new LBS.LngLat(args.center[0], args.center[1]);
            this.mapOptions.center=[args.center.longitude, args.center.latitude];
         }
      }

      if(this.map){
         try{
            this.map.destroy();
         }catch(e){}
      }

      //初始化地图对象，加载地图
      this.map = new AMap.Map(this.container, this.mapOptions);

      //地图加载完毕
      this.map.on('complete', function(e){
         LBS.onMapLoaded();
         //LBS.getCurrentPosition({
         //   onComplete: LBSAmap.onAutoLocated,
         //   onInit: args.onInit
         //});
      });
   },

   /**
    *
    * @param args
    */
   setMapOptions: function(args){
      if(!args) return;
      this.mapOptions=args;

      if(args.container) this.container=args.container;
      if(args.zoom) this.mapOptions.zoom=args.zoom;

      if(args.center){
         if(Array.isArray(args.center)) args.center=new LBS.LngLat(args.center[0], args.center[1]);
         this.mapOptions.center=[args.center.longitude, args.center.latitude];
      }
   },

   /**
    *
    * @param args  {fitView, toCenter, onclick, position:[longitude, latitude] | LngLat, title, offset:[offsetx, offsety], size:[sizex, sizey], content, iconUrl}
    */
   mark:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      let position=args.position;
      if(position instanceof Array) position=new LBS.LngLat(position[0], position[1]);

      //图标与位置偏移量[x, y]
      let offset=args.offset;
      if(!offset) offset=[0, 0];

      //图标大小[x, y]
      let size=args.size;
      if(!size) size=[25, 34];

      let iconUrl=args.iconUrl;
      if(Str.isBlank(iconUrl)) iconUrl='//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png';

      //图标
      let icon = new AMap.Icon({
         size: new AMap.Size(size[0], size[1]),
         image: iconUrl, //icon的图像
         imageSize: new AMap.Size(size[0], size[1])
      });

      //Marker
      let obj = new AMap.Marker({
         position: new AMap.LngLat(position.longitude, position.latitude),
         offset: new AMap.Pixel(offset[0], offset[1]),
         content: args.content,
         icon: icon,
         title: (args.title ? args.title : ''),
         extData: args.extData
      });

      //点击时
      if(args.onclick) obj.on('click', args.onclick);

      //添加至地图实例
      this.map.add(obj);

      if(args.fitView) this.map.setFitView();
      if(args.toCenter) this.map.panTo([position.longitude, position.latitude]);
   },

   /**
    * 批量（海量）点标记
    * @param args {zooms:[minZoom, maxZoom], styles:[{iconUrl, iconSize, offset}], datas:[{position, id, name, styleIndex}]}
    */
   markMass:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      let zooms=args.zooms;
      if(!zooms) zooms=[3, 20];

      // 样式对象数组
      let styles=[];
      if(args.styles){
         for(let i=0; i<args.styles.length; i++){
            if(!args.styles[i].iconSize) args.styles[i].iconSize=[11, 11];
            if(!args.styles[i].offset) args.styles[i].offset=[0, 0];
            styles.push({
               url: args.styles[i].iconUrl,// 图标地址
               size: new AMap.Size(args.styles[i].iconSize[0], args.styles[i].iconSize[1]),
               anchor: new AMap.Pixel(args.styles[i].offset[0], args.styles[i].offset[1])
            });
         }
      }else{
         styles.push({
            url: '//vdata.amap.com/icons/b18/1/2.png',// 图标地址
            size: new AMap.Size(11, 11),      // 图标大小
            anchor: new AMap.Pixel(5, 5) // 图标显示位置偏移量，基准点为图标左上角});
         });
      }

      // 实例化 AMap.MassMarks
      let obj = new AMap.MassMarks({
         zooms: zooms,// 在指定地图缩放级别范围内展示海量点图层
         style: styles //多种样式对象的数组
      });

      //点数组
      let datas=[];
      for(let i=0; i<args.datas.length; i++){
         //position, id, name, styleIndex
         if(args.datas[i].position instanceof Array) args.datas[i].position=new LBS.LngLat(args.datas[i].position[0], args.datas[i].position[1]);
         if((typeof args.datas[i].styleIndex)!='number'
             || args.datas[i].styleIndex<0
             || args.datas[i].styleIndex>styles.length-1) args.datas[i].styleIndex=0;

         datas.push({
            lnglat: [args.datas[i].position.longitude, args.datas[i].position.latitude],
            name: args.datas[i].name,
            id: args.datas[i].id,
            style: args.datas[i].styleIndex
         });
      }

      //设置数据
      obj.setData(datas);

      //添加至地图实例
      obj.setMap(this.map);

      if(args.fitView) this.map.setFitView();
   },

   /**
    * 折线/多边形
    * @param args {type, path:[[lng, lat],[lng, lat]], lineType, lineWidth, lineColor, lineOpacity, fillColor, fillOpacity, extData}
    */
   line:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      let path=[];
      for(let i=0; i<args.path.length; i++){
         if(args.path[i] instanceof Array) args.path[i]=new LBS.LngLat(args.path[i][0], args.path[i][1]);
         path.push(new AMap.LngLat(args.path[i].longitude, args.path[i].latitude));
      }

      //创建实例
      let obj=null;
      if(args.type=='polygon'){
         if(!args.strokeStyle || ('solid' != args.strokeStyle  && 'dashed' != args.strokeStyle )) args.strokeStyle='solid';
         if((typeof args.strokeWeight) != 'number' || args.strokeWeight<1) args.strokeWeight=2;
         if((typeof args.strokeOpacity) != 'number' || args.strokeOpacity<0 || args.strokeOpacity>1) args.strokeOpacity=0.9;
         if(Str.isBlank(args.strokeColor)) args.strokeColor='#FF0000';
         if((typeof args.fillOpacity ) != 'number' || args.fillOpacity <0 || args.fillOpacity >1) args.fillOpacity=0.6;
         if(Str.isBlank(args.fillColor)) args.fillColor='#ffaf3d';

         obj = new AMap.Polygon({
            path: path,
            strokeStyle: args.strokeStyle,
            strokeWeight: args.strokeWeight,
            strokeOpacity: args.strokeOpacity,
            strokeColor: args.strokeColor,
            fillColor: args.fillColor,
            fillOpacity: args.fillOpacity,
            lineJoin: 'round',//折线拐点连接处样式
            extData: args.extData
         });
      }else{
         if(!args.strokeStyle || ('solid' != args.strokeStyle  && 'dashed' != args.strokeStyle )) args.strokeStyle='solid';
         if((typeof args.strokeWeight) != 'number' || args.strokeWeight<1) args.strokeWeight=2;
         if((typeof args.strokeOpacity) != 'number' || args.strokeOpacity<0 || args.strokeOpacity>1) args.strokeOpacity=0.9;
         if(Str.isBlank(args.strokeColor)) args.strokeColor='#FF0000';

         obj = new AMap.Polyline({
            path: path,
            strokeStyle: args.strokeStyle,
            strokeWeight: args.strokeWeight,
            strokeOpacity: args.strokeOpacity,
            strokeColor: args.strokeColor,
            lineJoin: 'round',//折线拐点连接处样式
            extData: args.extData
         });
      }

      //添加至地图实例
      this.map.add(obj);

      if(args.fitView) this.map.setFitView();
      if(args.toCenter) this.map.panTo(path[0]);
   },

   /**
    * 矩形
    * @param args {southWest :[lng, lat] | LngLat, northEast :[lng, lat] | LngLat, radius, lineType, lineWidth, lineColor, lineOpacity, fillColor, fillOpacity, extData}
    */
   rectangle:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      if(args.southWest instanceof Array) args.southWest=new LBS.LngLat(args.southWest[0], args.southWest[1]);
      if(args.northEast instanceof Array) args.northEast=new LBS.LngLat(args.northEast[0], args.northEast[1]);
      if(!args.lineType || ('solid' != args.lineType && 'dashed' != args.lineType)) args.lineType='solid';
      if((typeof args.lineWidth) != 'number' || args.lineWidth<1) args.lineWidth=2;
      if((typeof args.lineOpacity) != 'number' || args.lineOpacity<0 || args.lineOpacity>1) args.lineOpacity=0.9;
      if((typeof args.fillOpacity) != 'number' || args.fillOpacity<0 || args.fillOpacity>1) args.fillOpacity=0.5;

      let southWest = new AMap.LngLat(args.southWest.longitude, args.southWest.latitude);
      let northEast = new AMap.LngLat(args.northEast.longitude, args.northEast.latitude);
      let bounds = new AMap.Bounds(southWest, northEast);

      //创建实例
      let obj = new AMap.Rectangle({
         bounds: bounds,
         strokeStyle: args.lineType,
         strokeColor: args.lineColor,
         strokeOpacity: args.lineOpacity,
         borderWeight: args.lineWidth,
         fillColor: args.fillColor,
         fillOpacity: args.fillOpacity,
         extData: args.extData
      });

      //添加至地图实例
      this.map.add(obj);

      if(args.fitView) this.map.setFitView();
      if(args.toCenter) this.map.panTo(obj.getCenter());
   },

   /**
    * 圆形
    * @param args {position:[lng, lat] | LngLat, radius, lineType, lineWidth, lineColor, lineOpacity, fillColor, fillOpacity, extData}
    */
   circle:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      let position=args.position;
      if(position instanceof Array) position=new LBS.LngLat(position[0], position[1]);

      if(!args.lineType || ('solid' != args.lineType && 'dashed' != args.lineType)) args.lineType='solid';
      if((typeof args.lineWidth) != 'number' || args.lineWidth<1) args.lineWidth=2;
      if((typeof args.lineOpacity) != 'number' || args.lineOpacity<0 || args.lineOpacity>1) args.lineOpacity=0.9;
      if((typeof args.fillOpacity) != 'number' || args.fillOpacity<0 || args.fillOpacity>1) args.fillOpacity=0.5;

      //创建实例
      let obj = new AMap.Circle({
         center: new AMap.LngLat(position.longitude, position.latitude),
         radius: args.radius,
         strokeStyle: args.lineType,
         strokeColor: args.lineColor,
         strokeOpacity: args.lineOpacity,
         borderWeight: args.lineWidth,
         fillColor: args.fillColor,
         fillOpacity: args.fillOpacity,
         extData: args.extData
      });

      //添加至地图实例
      this.map.add(obj);

      if(args.fitView) this.map.setFitView();
      if(args.toCenter) this.map.panTo(obj.getCenter());
   },

   /**
    * 椭圆
    * @param args {position:[lng, lat] | LngLat, radiusX, radiusY, lineType, lineWidth, lineColor, lineOpacity, fillColor, fillOpacity, extData}
    */
   ellipse:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      let position=args.position;
      if(position instanceof Array) position=new LBS.LngLat(position[0], position[1]);

      if(!args.lineType || ('solid' != args.lineType && 'dashed' != args.lineType)) args.lineType='solid';
      if((typeof args.lineWidth) != 'number' || args.lineWidth<1) args.lineWidth=2;
      if((typeof args.lineOpacity) != 'number' || args.lineOpacity<0 || args.lineOpacity>1) args.lineOpacity=0.9;
      if((typeof args.fillOpacity) != 'number' || args.fillOpacity<0 || args.fillOpacity>1) args.fillOpacity=0.5;

      //创建实例
      let obj = new AMap.Circle({
         center: new AMap.LngLat(position.longitude, position.latitude),
         radius: [args.radiusX, args.radiusY],
         strokeStyle: args.lineType,
         strokeColor: args.lineColor,
         strokeOpacity: args.lineOpacity,
         borderWeight: args.lineWidth,
         fillColor: args.fillColor,
         fillOpacity: args.fillOpacity,
         extData: args.extData
      });

      //添加至地图实例
      this.map.add(obj);

      if(args.fitView) this.map.setFitView();
      if(args.toCenter) this.map.panTo(obj.getCenter());
   },

   /**
    * 信息窗体
    * @param args  {isCustomized, anchor, position:[longitude, latitude] | LngLat, offset:[offsetx, offsety], size:[sizex, sizey], padding:[top, right, bottom, left], content}
    */
   infoWindow:function(args){
      //清除其它覆盖物
      if(args.clearMap) this.map.clearMap();

      //是否自定义
      if((typeof args.isCustomized)!='boolean') args.isCustomized=false;

      let position=args.position;
      if(position instanceof Array) position=new LBS.LngLat(position[0], position[1]);

      //锚点位置
      if(Str.isBlank(args.anchor)) args.anchor='bottom-left';

      //图标与位置偏移量[x, y]
      let offset=args.offset;
      if(!offset) offset=[0, 0];

      //图标大小[x, y]
      let size=args.size;
      if(!size) size=[0, 0];

      //自动平移到视野内后的上右下左的避让宽度
      let padding=args.padding;
      if(!padding) padding=[20, 20, 20, 20];

      let iconUrl=args.iconUrl;
      if(Str.isBlank(iconUrl)) iconUrl='';

      let width=args.width;
      if((typeof width) != 'number') width=200;

      let height=args.height;
      if((typeof height) != 'number') height=100;

      let content=[];
      content.push('<div class="LBS">');
      content.push('<div class="infoWindow" style="max-width: '+width+'px !important;">');
      content.push('<div class="title">');
      content.push(args.title);
      content.push('</div>');
      content.push('<div class="content">');
      content.push(args.content);
      content.push('</div>');
      content.push('</div>');
      content.push('</div>');
      content=content.join('');

      //实例
      let obj = new AMap.InfoWindow({
         isCustom: args.isCustomized,
         position: new AMap.LngLat(position.longitude, position.latitude),
         offset: new AMap.Pixel(offset[0], offset[1]),
         size: new AMap.Size(size[0], size[1]),
         avoid: padding,
         content: content,
         anchor: args.anchor
      });

      //添加至地图实例
      obj.open(this.map);

      if(args.fitView) this.map.setFitView();
      if(args.toCenter) this.map.panTo([position.longitude, position.latitude]);
   },

   /**
    * 当前定位
    * @param args
    */
   getCurrentPosition:function(args){
      if(!LBS.pluginExists('AMap.Geolocation')){
         LBS.loadPlugin('AMap.Geolocation');
         AMap.plugin('AMap.Geolocation', function (){
            LBSAmap.PluginLoaded_Geolocation(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.Geolocation'])){
         args=LBS.waitForPluginReady('LBSAmap.getCurrentPosition', args, ['AMap.Geolocation']);
         return;
      }

      args=LBS.restoreArgs(args);

      let plugin=LBS.getPlugin('AMap.Geolocation');
      plugin.object.getCurrentPosition(function(status, result){
         if(status=='complete'){
            for(let i in result){
               Logger.log('getCurrentPosition '+i+' -> '+result[i]);
            }

            //定位成功但转换地址失败，尝试通过ip定位获取地址
            if(result.info=='SUCCESS'
                && result.message.indexOf('Get geolocation success')>-1
                && result.message.indexOf("Get address fail")>-1){
               Logger.log('定位成功但转换地址失败，尝试通过ip定位获取地址....');
               LBSAmap.getCurrentCity(result, args);
               return;
            }

            if(args.onComplete){
               let addressComponent=result.addressComponent;
               if(addressComponent){
                  for(let i in addressComponent) Logger.log('getCurrentPosition addressComponent -> '+i+' = '+addressComponent[i]);

                  new LBS.Locating(result.status,
                      result.code,
                      result.message,
                      result.position,
                      result.accuracy,
                      result.altitude,
                      result.altitudeAccuracy,
                      result.heading,
                      result.speed,
                      addressComponent.cityCode,
                      null,
                      addressComponent.province,
                      addressComponent.city,
                      addressComponent.cityCode,
                      addressComponent.district,
                      addressComponent.township,
                      result.formattedAddress,
                      args.onComplete,
                      args,
                      args.callbackTarget);
               }else{
                  new LBS.Locating(result.status,
                      result.code,
                      result.message,
                      result.position,
                      result.accuracy,
                      result.altitude,
                      result.altitudeAccuracy,
                      result.heading,
                      result.speed,
                      result.adcode,
                      result.country,
                      result.province,
                      result.city,
                      result.cityCode,
                      null,
                      null,
                      result.formattedAddress,
                      args.onComplete,
                      args,
                      args.callbackTarget);
               }
            }
         }else{
            if(args.onError){
               new LBS.Locating(result.status,
                   result.code,
                   result.message,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   args.onComplete,
                   args,
                   args.callbackTarget);
            }
         }
      });
   },

   /**
    *
    * @param locationResult 定位信息
    * @param args
    */
   getCurrentCity:function(locationResult, args){
      if(!LBS.pluginExists('AMap.Geolocation')){
         LBS.loadPlugin('AMap.Geolocation');
         AMap.plugin('AMap.Geolocation', function (){
            LBSAmap.PluginLoaded_Geolocation(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.Geolocation'])){
         args=LBS.waitForPluginReady('LBSAmap.getCityInfo', args, ['AMap.Geolocation']);
         return;
      }

      args=LBS.restoreArgs(args);

      let plugin=LBS.getPlugin('AMap.Geolocation');
      plugin.object.getCityInfo(function(status, result){
         if(status=='complete'){
            for(let i in result){
               Logger.log('getCityInfo '+i+' -> '+result[i]);
            }

            if(args.onComplete){
               if(locationResult){
                  new LBS.Locating(locationResult.status,
                      locationResult.code,
                      locationResult.message,
                      locationResult.position,
                      locationResult.accuracy,
                      locationResult.altitude,
                      locationResult.altitudeAccuracy,
                      locationResult.heading,
                      locationResult.speed,
                      result.adcode,
                      result.country,
                      result.province,
                      result.city,
                      result.cityCode,
                      null,
                      null,
                      null,
                      args.onComplete,
                      args,
                      args.callbackTarget);
               }else{
                  new LBS.Locating(result.status,
                      result.code,
                      result.message,
                      result.position,
                      result.accuracy,
                      result.altitude,
                      result.altitudeAccuracy,
                      result.heading,
                      result.speed,
                      result.adcode,
                      result.country,
                      result.province,
                      result.city,
                      result.cityCode,
                      null,
                      null,
                      null,
                      args.onComplete,
                      args,
                      args.callbackTarget);
               }
            }
         }else{
            if(args.onError){
               for(let i in result){
                  Logger.log('getCityInfo '+i+' -> '+result[i]);
               }
               new LBS.Locating(result.status,
                   result.code,
                   result.message,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   null,
                   args.onComplete,
                   args,
                   args.callbackTarget);
            }
         }
      });
   },

   //Geolocation插件加载完毕
   PluginLoaded_Geolocation:function (args){
      let obj = new AMap.Geolocation({
         //是否使用高精度定位，默认：true
         enableHighAccuracy: true,

         //设置定位超时时间，默认：无穷大
         timeout: 30000,

         //定位按钮的停靠位置的偏移量
         offset: [10, 10],

         //定位成功后是否自动移动到响应位置
         panToLocation: LBSAmap.map?true:false,

         //定位成功后调整地图视野范围使定位位置及精度范围视野内可见，默认：false
         zoomToAccuracy: LBSAmap.map?true:false,

         //定位按钮的排放位置,  RB表示右下
         position: 'RB',

         //是否需要将定位结果进行逆地理编码操作
         needAddress: true,

         //是否需要详细的逆地理编码信息，默认为'base'只返回基本信息，可选'all'
         extensions: 'all',

         //定位失败之后是否返回基本城市定位信息
         getCityWhenFail: true
      });

      if(LBSAmap.map) LBSAmap.map.addControl(obj);

      LBS.onPluginLoaded('AMap.Geolocation', obj, null);
   },

   /**
    * 根据地址获得所在经纬度
    * @param args
    */
   addressToPosition:function(args){
      if(!LBS.pluginExists('AMap.Geocoder')){
         LBS.loadPlugin('AMap.Geocoder');
         AMap.plugin('AMap.Geocoder', function (){
            LBSAmap.PluginLoaded_Geocoder(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.Geocoder'])){
         args=LBS.waitForPluginReady('LBSAmap.addressToPosition', args, ['AMap.Geocoder']);
         return;
      }

      args=LBS.restoreArgs(args);
      let address=args.address;
      let region=args.region;
      if(!address && !region){
         Logger.log('LBSAmap.addressToPosition方法必须指定地址参数（address、region）！');
         return;
      }

      let city=args.city;
      if(!city && region) city = region.getCity();

      if(Str.isBlank(address)) address='';
      if(region) address=region.getCanonicalName(null, '', 1)+address;

      let plugin=LBS.getPlugin('AMap.Geocoder');
      Logger.log('AMap.Geocoder -> getLocation of -> '+address+' in city -> '+city);
      if(city) plugin.object.setCity(city);
      plugin.object.getLocation(address, function(status, result){
         if(status=='complete'){
            let geocodes=result.geocodes;

            if(args.onComplete && geocodes && geocodes.length>0){
               let geocode=geocodes[0];
               for(let i in geocode) Logger.log('geocode -> '+i+' -> '+geocode[i]);

               let addressComponent=geocode.addressComponent;
               if(addressComponent){
                  for(let i in addressComponent) Logger.log('addressComponent -> '+i+' = '+addressComponent[i]);
                  new LBS.Address(geocode.adcode,
                      null,
                      addressComponent.province,
                      addressComponent.city,
                      addressComponent.citycode,
                      addressComponent.district,
                      addressComponent.township,
                      geocode.formattedAddress,
                      geocode.location,
                      [],
                      args.onComplete,
                      args);
               }
            }
         }else if(status=='error'){
            if(args.onError) args.onError.call(args.callbackTarget?args.callbackTarget:window, args, result);
         }else if(status=='no_data'){
            if(args.onError)  args.onError.call(args.callbackTarget?args.callbackTarget:window, args, 'no_data');
         }
      });
   },

   /**
    * 根据经纬度获得所在地址信息
    * @param args
    */
   positionToAddress:function(args){
      if(!LBS.pluginExists('AMap.Geocoder')){
         LBS.loadPlugin('AMap.Geocoder');
         AMap.plugin('AMap.Geocoder', function (){
            Logger.log('AMap.Geocoder args = '+args);
            LBSAmap.PluginLoaded_Geocoder(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.Geocoder'])){
         args=LBS.waitForPluginReady('LBSAmap.positionToAddress', args, ['AMap.Geocoder']);
         return;
      }

      args=LBS.restoreArgs(args);
      let position=args.position;
      if(!position){
         Logger.log('LBSAmap.positionToAddress方法必须指定经纬度参数（position）！');
         return;
      }
      if(Array.isArray(position)) position=new LBS.LngLat(position[0], position[1]);

      let plugin=LBS.getPlugin('AMap.Geocoder');
      Logger.log('AMap.Geocoder -> getAddress of -> '+position);
      plugin.object.getAddress(new AMap.LngLat(position.longitude, position.latitude), function(status, result){
         if(status=='complete'){
            let regeocode=result.regeocode;
            for(let i in regeocode) Logger.log('regeocode -> '+i+' -> '+regeocode[i]);

            let addressComponent=regeocode?regeocode.addressComponent:null;
            if(addressComponent){
               for(let i in addressComponent) Logger.log('[positionToAddress] addressComponent -> '+i+' -> '+addressComponent[i]);
            }

            let pois=regeocode.pois;
            let _pois=[];
            for(let i in pois){
               let poi = pois[i];
               //for(let i in poi) Logger.log('poi -> '+i+' -> '+poi[i]);

               let address=null;
               if(poi.adcode) address=new LBS.Address(poi.adcode,
                   null,
                   poi.pname,
                   poi.cityname,
                   poi.citycode,
                   poi.adname,
                   null,
                   poi.address,
                   poi.location,
                   null,
                   null,
                   null,
                   null,
                   false);

               let _photos=[];
               let photos=poi.photos;
               for(let j in photos) _photos.push(new LBS.Photo(photos[j].title, photos[j].url));

               _pois.push(new LBS.POI(position,
                   poi.id,
                   poi.name,
                   poi.location,
                   poi.distance,
                   poi.type,
                   poi.address,
                   poi.tel,
                   poi.direction,
                   poi.businessArea,
                   poi.website,
                   _photos,
                   address));
            }

            if(args.onComplete && addressComponent){
               new LBS.Address(addressComponent.adcode,
                   null,
                   addressComponent.province,
                   addressComponent.city,
                   addressComponent.citycode,
                   addressComponent.district,
                   addressComponent.township,
                   regeocode.formattedAddress,
                   position,
                   _pois,
                   args.onComplete,
                   args);
            }
         }else if(status=='error'){
            if(args.onError) args.onError.call(args.callbackTarget?args.callbackTarget:window, args, result);
         }else if(status=='no_data'){
            if(args.onError)  args.onError.call(args.callbackTarget?args.callbackTarget:window, args, 'no_data');
         }
      });
   },

   //Geocoder插件加载完毕
   PluginLoaded_Geocoder:function (args){
      let address=args.address;
      let city=args.city;

      if(address){
         if(address instanceof Region){
            if(!city) city=address.getCity();
         }else if(Array.isArray(address)){
            if(address[0] instanceof Region){
               if(!city) city=address[address.length-1].getCity();
            }else{
               if(!city) city=address[0];
            }
         }else{
            if(!city) city=address;
         }
      }

      if(city && (city instanceof Region)) city=city.nameCn;

      let obj = new AMap.Geocoder({
         city: city,
         radius: args.radius,
         extensions: args.radius?'all':'base'
      });

      LBS.onPluginLoaded('AMap.Geocoder', obj, null);
   },

   /**
    * 查询PlaceSearch
    * @param args
    */
   search:function(args){
      if(!LBS.pluginExists('AMap.PlaceSearch')){
         LBS.loadPlugin('AMap.PlaceSearch');
         AMap.plugin('AMap.PlaceSearch', function (){
            LBSAmap.PluginLoaded_PlaceSearch(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.PlaceSearch'])){
         args=LBS.waitForPluginReady('LBSAmap.search', args, ['AMap.PlaceSearch']);
         return;
      }

      args=LBS.restoreArgs(args);
      let _args=LBSAmap.searchArgsFormat(args);

      let plugin=LBS.getPlugin('AMap.PlaceSearch');
      plugin.object.setType(_args.type);
      plugin.object.setCity(_args.city);
      plugin.object.setCityLimit(_args.citylimit);
      plugin.object.setPageSize(_args.pageSize);
      plugin.object.setPageIndex(_args.pageIndex);
      plugin.object.setLang(_args.lang);
      plugin.object.search(args.keywords, function(status, result){
         if(status=='complete'){
            for(let i in result) Logger.log('search -> '+i+' -> '+result[i]);

            let poiList=result.poiList;
            for(let i in poiList) Logger.log('search poiList -> '+i+' -> '+poiList[i]);

            if(args.onComplete && result.poiList){
               let total=result.poiList.count;
               let pn=result.poiList.pageIndex;
               let pois=result.poiList.pois;

               if((typeof total)!='number') total=0;
               if((typeof pn)!='number') pn=1;

               let _pois=[];
               for(let i in pois){
                  let poi = pois[i];
                  //for(let i in poi) Logger.log('search poi -> '+i+' -> '+poi[i]);

                  let address=null;
                  if(poi.adcode) address=new LBS.Address(poi.adcode,
                      null,
                      poi.pname,
                      poi.cityname,
                      poi.citycode,
                      poi.adname,
                      null,
                      poi.address,
                      poi.location,
                      null,
                      null,
                      null,
                      null,
                      false);

                  let _photos=[];
                  let photos=poi.photos;
                  for(let j in photos)  _photos.push(new LBS.Photo(photos[j].title, photos[j].url));

                  _pois.push(new LBS.POI(null,
                      poi.id,
                      poi.name,
                      poi.location,
                      poi.distance,
                      poi.type,
                      poi.address,
                      poi.tel,
                      poi.direction,
                      poi.businessArea,
                      poi.website,
                      _photos,
                      address));
               }

               args.onComplete.call(args.callbackTarget?args.callbackTarget:window, args, new LBS.POISearchResult(total, _args.pageSize, pn, _pois));
            }
         }else if(status=='error'){
            if(args.onError) args.onError.call(args.callbackTarget?args.callbackTarget:window, args, result);
         }else if(status=='no_data'){
            if(args.onError)  args.onError.call(args.callbackTarget?args.callbackTarget:window, args, 'no_data');
         }
      });
   },

   /**
    * 查询指定位置附近PlaceSearch
    * @param args
    */
   searchNearBy:function(args){
      if(!LBS.pluginExists('AMap.PlaceSearch')){
         LBS.loadPlugin('AMap.PlaceSearch');
         AMap.plugin('AMap.PlaceSearch', function (){
            LBSAmap.PluginLoaded_PlaceSearch(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.PlaceSearch'])){
         args=LBS.waitForPluginReady('LBSAmap.searchNearBy', args, ['AMap.PlaceSearch']);
         return;
      }

      args=LBS.restoreArgs(args);
      let _args=LBSAmap.searchArgsFormat(args);

      let plugin=LBS.getPlugin('AMap.PlaceSearch');
      plugin.object.setType(Str.isBlank(_args.type) ? LBSAmap.DEFAULT_POI_TYPES : _args.type);
      plugin.object.setCity(_args.city);
      plugin.object.setCityLimit(_args.citylimit);
      plugin.object.setPageSize(_args.pageSize);
      plugin.object.setPageIndex(_args.pageIndex);
      plugin.object.setLang(_args.lang);
      plugin.object.searchNearBy(_args.keywords, _args.center, _args.radius, function(status, result){
         if(status=='complete'){
            for(let i in result) Logger.log('searchNearBy -> '+i+' -> '+result[i]);

            let poiList=result.poiList;
            for(let i in poiList) Logger.log('searchNearBy poiList -> '+i+' -> '+poiList[i]);

            if(args.onComplete && result.poiList){
               let total=result.poiList.count;
               let pn=result.poiList.pageIndex;
               let pois=result.poiList.pois;

               if((typeof total)!='number') total=0;
               if((typeof pn)!='number') pn=1;

               let _pois=[];
               for(let i in pois){
                  let poi = pois[i];
                  for(let i in poi) Logger.log('searchNearBy poi -> '+i+' -> '+poi[i]);

                  let address=null;
                  if(poi.adcode) address=new LBS.Address(poi.adcode,
                      null,
                      poi.pname,
                      poi.cityname,
                      poi.citycode,
                      poi.adname,
                      null,
                      poi.address,
                      poi.location,
                      null,
                      null,
                      null,
                      null,
                      true);

                  let _photos=[];
                  let photos=poi.photos;
                  for(let j in photos)  _photos.push(new LBS.Photo(photos[j].title, photos[j].url));

                  _pois.push(new LBS.POI(_args.center,
                      poi.id,
                      poi.name,
                      poi.location,
                      poi.distance,
                      poi.type,
                      poi.address,
                      poi.tel,
                      poi.direction,
                      poi.businessArea,
                      poi.website,
                      _photos,
                      address));
               }

               args.onComplete.call(args.callbackTarget?args.callbackTarget:window, args, new LBS.POISearchResult(total, _args.pageSize, pn, _pois));
            }
         }else if(status=='error'){
            if(args.onError) args.onError.call(args.callbackTarget?args.callbackTarget:window, args, result);
         }else if(status=='no_data'){
            if(args.onError)  args.onError.call(args.callbackTarget?args.callbackTarget:window, args, 'no_data');
         }
      });
   },

   /**
    * 查询指定范围内PlaceSearch
    * @param args
    */
   searchInBounds:function(args){
      if(!LBS.pluginExists('AMap.PlaceSearch')){
         LBS.loadPlugin('AMap.PlaceSearch');
         AMap.plugin('AMap.PlaceSearch', function (){
            LBSAmap.PluginLoaded_PlaceSearch(args);
         });
      }

      if(!LBS.checkPlugins(['AMap.PlaceSearch'])){
         args=LBS.waitForPluginReady('LBSAmap.searchInBounds', args, ['AMap.PlaceSearch']);
         return;
      }

      args=LBS.restoreArgs(args);
      let _args=LBSAmap.searchArgsFormat(args);

      let plugin=LBS.getPlugin('AMap.PlaceSearch');
      plugin.object.setType(_args.type);
      plugin.object.setCity(_args.city);
      plugin.object.setCityLimit(_args.citylimit);
      plugin.object.setPageSize(_args.pageSize);
      plugin.object.setPageIndex(_args.pageIndex);
      plugin.object.setLang(_args.lang);
      plugin.object.searchInBounds(_args.keywords, _args.bounds, function(status, result){
         if(status=='complete'){
            for(let i in result) Logger.log('searchInBounds -> '+i+' -> '+result[i]);

            let poiList=result.poiList;
            for(let i in poiList) Logger.log('searchInBounds poiList -> '+i+' -> '+poiList[i]);

            if(args.onComplete && result.poiList){
               let total=result.poiList.count;
               let pn=result.poiList.pageIndex;
               let pois=result.poiList.pois;

               if((typeof total)!='number') total=0;
               if((typeof pn)!='number') pn=1;

               let _pois=[];
               for(let i in pois){
                  let poi = pois[i];
                  //for(let i in poi) Logger.log('searchInBounds poi -> '+i+' -> '+poi[i]);

                  let address=null;
                  if(poi.adcode) address=new LBS.Address(poi.adcode,
                      null,
                      poi.pname,
                      poi.cityname,
                      poi.citycode,
                      poi.adname,
                      null,
                      poi.address,
                      poi.location,
                      null,
                      null,
                      null,
                      null,
                      false);

                  let _photos=[];
                  let photos=poi.photos;
                  for(let j in photos)  _photos.push(new LBS.Photo(photos[j].title, photos[j].url));

                  _pois.push(new LBS.POI(_args.center,
                      poi.id,
                      poi.name,
                      poi.location,
                      poi.distance,
                      poi.type,
                      poi.address,
                      poi.tel,
                      poi.direction,
                      poi.businessArea,
                      poi.website,
                      _photos,
                      address));
               }

               args.onComplete.call(args.callbackTarget?args.callbackTarget:window, args, new LBS.POISearchResult(total, _args.pageSize, pn, _pois));
            }
         }else if(status=='error'){
            if(args.onError) args.onError.call(args.callbackTarget?args.callbackTarget:window, args, result);
         }else if(status=='no_data'){
            if(args.onError)  args.onError.call(args.callbackTarget?args.callbackTarget:window, args, 'no_data');
         }
      });
   },

   /**
    * 格式化搜索参数
    * @param args
    * @returns
    */
   searchArgsFormat:function (args){
      let city=args.city;
      if(city instanceof Region){
         city=city.getCity();
      }else if(Array.isArray(city)){
         if(city[0] instanceof Region){
            city=city[city.length-1].getCity();
         }else{
            city=city[0];
         }
      }
      if(city instanceof Region) city=city.nameCn;

      let citylimit=args.citylimit;
      if((typeof citylimit)!='boolean') citylimit=false;

      let children=args.children;
      if(children!=1 && children!=0) children=0;

      /**
       * 兴趣点类别，多个类别用“|”分割，如“餐饮|酒店|电影院”
       POI搜索类型共分为以下20种：
       汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|
       医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|
       交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
       默认值：餐饮服务、商务住宅、生活服务
       */
      let type=args.type;
      if(Str.isBlank(type)) type='汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施';

      let lang=args.lang;
      if(Str.isBlank(lang)) lang=Lang.getCurrentLang().id;

      let pageSize=args.pageSize;
      if((typeof pageSize)!='number' || pageSize<1 || pageSize>50) pageSize=50;

      let pageIndex=args.pageIndex;
      if((typeof pageIndex)!='number' || pageIndex<1 || pageIndex>100) pageIndex=1;

      let extensions=args.extensions;
      if(!extensions || ('base'!=extensions && 'all'!=extensions)) extensions='all';

      let panel=args.panel;
      if((typeof panel)=='string') panel=_$(panel);

      let showCover=args.showCover;
      if((typeof showCover)!='boolean') showCover=true;

      let renderStyle=args.renderStyle;
      if(!renderStyle || ('newpc'!=renderStyle && 'default'!=renderStyle)) renderStyle='default';

      let autoFitView=args.fitView;
      if((typeof autoFitView)!='boolean') autoFitView=true;

      let center=args.center;
      if(center){
         if(Array.isArray(center) && center.length == 2) {
            center = new LBS.LngLat(center[0], center[1]);
         } else if ((typeof center.getLng) != 'undefined') {
            center = new LBS.LngLat(center.getLng(), center.getLat());
         }
         center=new AMap.LngLat(center.longitude, center.latitude);
      }

      let radius=args.radius;
      if((typeof radius)!='number' || radius<1) radius=1000;

      let bounds=args.bounds;
      let polygon=null;
      if(bounds && (bounds instanceof Array)){
         for(let i=0; i<bounds.length; i++){
            let p=bounds[i];
            if (Array.isArray(p) && p.length == 2) {
               p = new LBS.LngLat(p[0], p[1]);
            } else if ((typeof p.getLng) != 'undefined') {
               p = new LBS.LngLat(p.getLng(), p.getLat());
            }
            bounds[i]=[p.longitude, p.latitude];
         }

         polygon = new AMap.Polygon({
            path: bounds
         });
      }

      return {
         city: city,
         citylimit: citylimit,
         children: children,
         type: type,
         lang: lang,
         pageSize: pageSize,
         pageIndex: pageIndex,
         extensions: extensions,
         panel: panel,
         showCover: showCover,
         renderStyle: renderStyle,
         autoFitView: autoFitView,
         keywords: args.keywords,
         center: center,
         radius: radius,
         bounds: polygon,
         POI: args.POI
      };
   },

   //PlaceSearch插件加载完毕
   PluginLoaded_PlaceSearch:function (args){
      let _args=LBSAmap.searchArgsFormat(args);

      let obj = new AMap.PlaceSearch({
         map: LBSAmap.map,
         city: _args.city,
         citylimit: _args.citylimit,
         children: _args.children,
         type: _args.type,
         lang: _args.lang,
         pageSize: _args.pageSize,
         pageIndex: _args.pageIndex,
         extensions: _args.extensions,
         panel: _args.panel,
         showCover: _args.showCover,
         renderStyle: _args.renderStyle,
         autoFitView: _args.autoFitView
      });

      obj.on('markerClick', function(e){
         let markerClickCallback=args.markerClickCallback;
         let markerClickCallbackTarget=args.markerClickCallbackTarget;
         if(markerClickCallback) markerClickCallback.call(markerClickCallbackTarget?markerClickCallbackTarget:window, e);
      });

      LBS.onPluginLoaded('AMap.PlaceSearch', obj, null);
   },

   /**
    *
    * @param args
    */
   startPicker:function (args){
      if(!LBS.pluginExists('AMap.PositionPicker')){
         LBS.loadPlugin('AMap.PositionPicker');
         AMapUI.loadUI(['misc/PositionPicker'], function(PositionPicker){
            LBSAmap.PluginLoaded_PositionPicker(PositionPicker);
         });
      }

      if(!LBS.checkPlugins(['AMap.PositionPicker'])){
         args=LBS.waitForPluginReady('LBSAmap.startPicker', args, ['AMap.PositionPicker']);
         return;
      }

      args=LBS.restoreArgs(args);
      let plugin=LBS.getPlugin('AMap.PositionPicker');
      plugin.object.on('success', function(result) {
         for(let i in result) Logger.log('picker on success '+i+' = '+result[i]);

         let regeocode=result.regeocode;
         for(let i in regeocode) Logger.log('regeocode -> '+i+' -> '+regeocode[i]);

         let addressComponent=regeocode?regeocode.addressComponent:null;
         if(addressComponent){
            for(let i in addressComponent) Logger.log('[startPicker] addressComponent -> '+i+' -> '+addressComponent[i]);
         }

         let position=result.position;
         if(position){
            if(Array.isArray(position) && position.length==2){
               position=new LBS.LngLat(position[0], position[1]);
            }else if((typeof position.getLng) != 'undefined'){
               position=new LBS.LngLat(position.getLng(), position.getLat());
            }
         }

         let pois=regeocode.pois;
         let _pois=[];
         for(let i in pois){
            let poi = pois[i];
            //for(let i in poi) Logger.log('poi -> '+i+' -> '+poi[i]);

            let address=null;
            if(poi.adcode) address=new LBS.Address(poi.adcode,
                null,
                poi.pname,
                poi.cityname,
                poi.citycode,
                poi.adname,
                null,
                poi.address,
                poi.location,
                null,
                null,
                null,
                null,
                false);

            let _photos=[];
            let photos=poi.photos;
            for(let j in photos) _photos.push(new LBS.Photo(photos[j].title, photos[j].url));

            _pois.push(new LBS.POI(position,
                poi.id,
                poi.name,
                poi.location,
                poi.distance,
                poi.type,
                poi.address,
                poi.tel,
                poi.direction,
                poi.businessArea,
                poi.website,
                _photos,
                address));
         }

         if(args.callback && addressComponent){
            new LBS.Address(addressComponent.adcode,
                null,
                addressComponent.province,
                addressComponent.city,
                addressComponent.citycode,
                addressComponent.district,
                addressComponent.township,
                regeocode.formattedAddress,
                position,
                _pois,
                args.callback,
                args,
                args.callbackTarget);
         }
      });

      plugin.object.on('fail', function(result) {
         Logger.log('position picker 获取地址失败 => '+result);
         if(args.callback) args.callback.call(args.callbackTarget?args.callbackTarget:window, null);
      });

      if(args.center){
         if(Array.isArray(args.center)){
            args.center = new AMap.LngLat(args.center[0], args.center[1]);
         }else if(args.center instanceof LBS.LngLat){
            args.center = new AMap.LngLat(args.center.longitude, args.center.latitude);
         }
      }
      plugin.object.start(args.center);
   },

   /**
    *
    * @param args
    */
   stopPicker:function (args){
      if(!LBS.checkPlugins(['AMap.PositionPicker'])) return;
      let plugin=LBS.getPlugin('AMap.PositionPicker');
      plugin.object.stop();
   },

   //PositionPicker插件加载完毕
   PluginLoaded_PositionPicker:function(PositionPicker) {
      let obj = new PositionPicker({
         mode:'dragMap',
         map: LBSAmap.map
      });
      LBS.onPluginLoaded('AMap.PositionPicker', obj, null);
   },

   /**
    *
    * @param args 必须指定args.positions，格式为经纬度数组 [LBS.LngLat | [lng, lat]]
    */
   getDistance:function (args){
      let positions=args.positions;
      if(!positions || !Array.isArray(positions) || positions.length<2) return 0;

      //格式转换
      for(let i=0; i<positions.length; i++){
         let p=positions[i];
         if(Array.isArray(p) && p.length==2){
            p=new LBS.LngLat(p[0], p[1]);
         }else if((typeof p.getLng) != 'undefined'){
            p=new LBS.LngLat(p.getLng(), p.getLat());
         }
         positions[i]=p;
      }

      if(positions.length==2){//两个点，计算直线距离
         return AMap.GeometryUtil.distance(new AMap.LngLat(positions[0].longitude, positions[0].latitude),
             new AMap.LngLat(positions[1].longitude, positions[1].latitude));
      }else{//折线距离
         for(let i=0; i<positions.length; i++) positions[i]=new AMap.LngLat(positions[i].longitude, positions[i].latitude);
         return AMap.GeometryUtil.distanceOfLine(positions);
      }
   }
}