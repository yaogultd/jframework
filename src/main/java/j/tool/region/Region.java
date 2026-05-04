package j.tool.region;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.dao.DAO;
import j.core.dao.DB;
import j.core.dao.util.SQLUtil;
import j.core.db.*;
import j.core.nvwa.Nvwa;
import j.core.web.handler.JHandler;
import j.core.web.handler.JSession;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilBean;
import j.util.JUtilString;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
@Handler(path = "/framework/api/tool/region")
public final class Region extends JHandler {
	private static Logger log = Logger.create(Region.class);
	private static ConcurrentMap countries = new ConcurrentMap(false, new LinkedHashMap());//国家
	private static ConcurrentMap countriesKeyedByName = new ConcurrentMap(false, new LinkedHashMap());//国家
	private static ConcurrentMap countriesKeyedByIsoCode = new ConcurrentMap(false, new LinkedHashMap());//国家

	private static ConcurrentMap provinces = new ConcurrentMap(false, new LinkedHashMap());//省份
	private static ConcurrentMap provincesKeyedByName = new ConcurrentMap(false, new LinkedHashMap());//省份

	private static ConcurrentMap cities = new ConcurrentMap(false, new LinkedHashMap());//城市
	private static ConcurrentMap citiesKeyedByName = new ConcurrentMap(false, new LinkedHashMap());//城市
	private static ConcurrentMap citiesKeyedByAreaCode = new ConcurrentMap(false, new LinkedHashMap());//城市

	private static ConcurrentMap counties = new ConcurrentMap(false, new LinkedHashMap());//区县
	private static ConcurrentMap countiesKeyedByName = new ConcurrentMap(false, new LinkedHashMap());//区县

	private static ConcurrentMap zones = new ConcurrentMap(false, new LinkedHashMap());//区县
	private static ConcurrentMap zonesKeyedByName = new ConcurrentMap(false, new LinkedHashMap());//区县

	private static ConcurrentMap cache = new ConcurrentMap();//缓存
	private static boolean loaded=false;

	public static final String COUNTRY_ID_CHINA="8";

	/**
	 *
	 */
	private static void load() {
		if(loaded) return;
		loaded=true;

		DAO dao = null;
		try {
			dao = DB.connect("Region", Region.class);

			List temp = dao.find("j_country", "");
			for (int i = 0; i < temp.size(); i++) {
				Jcountry o = (Jcountry) temp.get(i);
				countries.put(o.getCountryId(), o);
				countriesKeyedByName.put(o.getCountryName().toUpperCase(), o);
				countriesKeyedByName.put(o.getCountryNameEn().toUpperCase(), o);
				countriesKeyedByIsoCode.put(o.getCountryCode().toUpperCase(), o);
			}
			temp.clear();
			temp = null;
			log.log(countries.size() + " countries loaded.", -1);

			temp = dao.find("j_province", "order by province_id*1 asc");
			for (int i = 0; i < temp.size(); i++) {
				Jprovince o = (Jprovince) temp.get(i);
				provinces.put(o.getProvinceId(), o);
				provincesKeyedByName.put(o.getProvinceName(), o);
				provincesKeyedByName.put(o.getProvinceNameEn(), o);

				//内蒙古自治区、广西壮族自治区、西藏自治区、宁夏回族自治区、新疆维吾尔自治区‌
				if("内蒙古自治区".equals(o.getProvinceName())){
					provincesKeyedByName.put("内蒙古", o);
					provincesKeyedByName.put("内蒙", o);
					provincesKeyedByName.put("内蒙古省", o);
				}else if("广西壮族自治区".equals(o.getProvinceName())){
					provincesKeyedByName.put("广西", o);
					provincesKeyedByName.put("广西省", o);
				}else if("西藏自治区".equals(o.getProvinceName())){
					provincesKeyedByName.put("西藏", o);
					provincesKeyedByName.put("西藏省", o);
				}else if("宁夏回族自治区".equals(o.getProvinceName())){
					provincesKeyedByName.put("宁夏", o);
					provincesKeyedByName.put("宁夏省", o);
				}else if("新疆维吾尔自治区".equals(o.getProvinceName())){
					provincesKeyedByName.put("新疆", o);
					provincesKeyedByName.put("新疆省", o);
				}else if(o.getProvinceName().endsWith("省")){
					provincesKeyedByName.put(o.getProvinceName().substring(0, o.getProvinceName().length() - 1), o);
				}else if(o.getProvinceName().endsWith("市")){
					provincesKeyedByName.put(o.getProvinceName().substring(0, o.getProvinceName().length() - 1), o);
				}else{
					provincesKeyedByName.put(o.getProvinceName()+"省", o);
					provincesKeyedByName.put(o.getProvinceName()+"市", o);
				}
			}
			temp.clear();
			temp = null;
			log.log(provinces.size() + " provinces loaded.", -1);

			temp = dao.find("j_city", "order by city_name_en asc");
			for (int i = 0; i < temp.size(); i++) {
				Jcity o = (Jcity) temp.get(i);
				cities.put(o.getCityId(), o);
				citiesKeyedByName.put(o.getCityName(), o);
				citiesKeyedByName.put(o.getCityNameEn(), o);
				if (o.getAreaCode() != null && !"".equals(o.getAreaCode())) {
					citiesKeyedByAreaCode.put(o.getAreaCode(), o);
				}
			}
			temp.clear();
			temp = null;
			log.log(cities.size() + " cities loaded.", -1);

			temp = dao.find("j_county", "");
			for (int i = 0; i < temp.size(); i++) {
				Jcounty o = (Jcounty) temp.get(i);
				counties.put(o.getCountyId(), o);
				countiesKeyedByName.put(o.getCountyName(), o);
				countiesKeyedByName.put(o.getCountyNameEn(), o);
			}
			temp.clear();
			temp = null;
			log.log(counties.size() + " counties loaded.", -1);


			temp = dao.find("j_zone", "");
			for (int i = 0; i < temp.size(); i++) {
				Jzone o = (Jzone) temp.get(i);
				zones.put(o.getZoneId(), o);
				zonesKeyedByName.put(o.getZoneName(), o);
				zonesKeyedByName.put(o.getZoneNameEn(), o);

				String key = "zones." + o.getCountyId();
				List ofParent = (List) cache.get(key);
				if (ofParent == null) {
					ofParent = new ArrayList();
					cache.put(key, ofParent);
				}
				ofParent.add(o);
			}
			temp.clear();
			temp = null;
			log.log(zones.size() + " zones loaded.", -1);

			dao.close();
			dao = null;
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
			try {
				dao.close();
				dao = null;
			} catch (Exception ex) {
			}
		}
	}


	/**
	 * @return
	 */
	public static List<Jcountry> getCountries() {
		load();
		if (cache.containsKey("countries")) return (List) cache.get("countries");

		List list = countries.listValues();
		cache.put("countries", list);
		return list;
	}

	/**
	 * @param countryId
	 * @return
	 */
	public static Jcountry getCountry(String countryId) {
		load();
		if (countryId == null || "".equals(countryId)) return null;
		return (Jcountry) countries.get(countryId);
	}

	/**
	 * @param countryName
	 * @return
	 */
	public static Jcountry getCountryByName(String countryName) {
		load();
		if (countryName == null || "".equals(countryName)) return null;
		return (Jcountry) countriesKeyedByName.get(countryName.toUpperCase());
	}

	/**
	 *
	 * @param isoCode
	 * @return
	 */
	public static Jcountry getCountryByIsoCode(String isoCode) {
		load();
		if (isoCode == null || "".equals(isoCode)) return null;
		return (Jcountry) countriesKeyedByIsoCode.get(isoCode.toUpperCase());
	}

	/**
	 * @return
	 */
	public static List<Jprovince> getProvinces() {
		return getProvinces("8");
	}

	/**
	 * @param countryId
	 * @return
	 */
	public static List<Jprovince> getProvinces(String countryId) {
		load();
		if (countryId == null || "".equals(countryId)) return null;

		String key = "provinces." + countryId;
		if (cache.containsKey(key)) return (List) cache.get(key);

		List<Jprovince> of = new ArrayList();
		List list = provinces.listValues();
		for (int i = 0; i < list.size(); i++) {
			Jprovince o = (Jprovince) list.get(i);
			if (o.getCountryId().equals(countryId)) {
				of.add(o);
			}
		}
		cache.put(key, of);

		return of;
	}

	/**
	 * @param provinceId
	 * @return
	 */
	public static Jprovince getProvince(String provinceId) {
		load();
		if (provinceId == null || "".equals(provinceId)) return null;
		return (Jprovince) provinces.get(provinceId);
	}

	/**
	 * @param provinceName
	 * @return
	 */
	public static Jprovince getProvinceByName(String provinceName) {
		load();
		if (provinceName == null || "".equals(provinceName)) return null;
		return (Jprovince) provincesKeyedByName.get(provinceName);
	}

	/**
	 * @param provinceId
	 * @return
	 */
	public static List<Jcity> getCities(String provinceId) {
		load();
		if (provinceId == null || "".equals(provinceId)) return null;

		String key = "cities." + provinceId;
		if (cache.containsKey(key)) return (List) cache.get(key);

		List<Jcity> of = new ArrayList();
		List list = cities.listValues();
		for (int i = 0; i < list.size(); i++) {
			Jcity o = (Jcity) list.get(i);
			if (o.getProvinceId().equals(provinceId)) {
				of.add(o);
			}
		}
		cache.put(key, of);

		return of;
	}

	/**
	 * @param cityId
	 * @return
	 */
	public static Jcity getCity(String cityId) {
		load();
		if (cityId == null || "".equals(cityId)) return null;
		return (Jcity) cities.get(cityId);
	}

	/**
	 * @deprecated
	 * @param cityName
	 * @return
	 */
	public static Jcity getCityByName(String cityName) {
		load();
		if (cityName == null || "".equals(cityName)) return null;
		return (Jcity) citiesKeyedByName.get(cityName);
	}

	/**
	 *
	 * @param provinceId
	 * @param cityName
	 * @return
	 */
	public static Jcity getCityByName(String provinceId, String cityName) {
		load();
		if (cityName == null || "".equals(cityName)) return null;

		List<Jcity> all = cities.listValues();
		for(Jcity c : all){
			if(c.getProvinceId().equals(provinceId)
					&& (cityName.equals(c.getCityName()) || cityName.equals(c.getCityNameEn()) || cityName.equals(c.getCityNameTw()))) return c;
		}
		return null;
	}

	/**
	 * @param areaCode
	 * @return
	 */
	public static Jcity getCityByAreaCode(String areaCode) {
		load();
		if (areaCode == null || "".equals(areaCode)) return null;

		return (Jcity) citiesKeyedByAreaCode.get(areaCode);
	}

	/**
	 *
	 * @param proviceId
	 * @param cityId
	 * @return
	 */
	public static Jcity getCity(String proviceId, String cityId) {
		load();
		if (cityId == null || "".equals(cityId)) return null;
		return (Jcity) cities.get(cityId);
	}

	/**
	 * @param cityId
	 * @return
	 */
	public static List<Jcounty> getCounties(String cityId){
		load();
		if (cityId == null || "".equals(cityId)) return null;

		String key = "counties." + cityId;
		if (cache.containsKey(key)) return (List) cache.get(key);

		List<Jcounty> of = new ArrayList();
		List list = counties.listValues();
		for (int i = 0; i < list.size(); i++) {
			Jcounty o = (Jcounty) list.get(i);
			if (o.getCityId().equals(cityId)) {
				of.add(o);
			}
		}
		cache.put(key, of);

		return of;
	}

	/**
	 * @param countyId
	 * @return
	 */
	public static Jcounty getCounty(String countyId) {
		load();
		if (countyId == null || "".equals(countyId)) return null;
		return (Jcounty) counties.get(countyId);
	}

	/**
	 * @deprecated
	 * @param countyName
	 * @return
	 */
	public static Jcounty getCountyByName(String countyName) {
		load();
		if (countyName == null || "".equals(countyName)) return null;
		return (Jcounty) countiesKeyedByName.get(countyName);
	}

	/**
	 *
	 * @param cityId
	 * @param countyName
	 * @return
	 */
	public static Jcounty getCountyByName(String cityId, String countyName) {
		load();
		if (countyName == null || "".equals(countyName)) return null;

		List<Jcounty> all = counties.listValues();
		for(Jcounty c : all){
			if(c.getCityId().equals(cityId)
					&& (countyName.equals(c.getCountyName()) || countyName.equals(c.getCountyNameEn()) || countyName.equals(c.getCountyNameTw()))) return c;
		}
		return null;
	}

	/**
	 * @return
	 */
	public static List<Jzone> getZones() {
		load();
		return zones.listValues();
	}

	/**
	 * @param countyId
	 * @return
	 */
	public static List<Jzone> getZones(String countyId) {
		load();
		if (countyId == null || "".equals(countyId)) return null;

		String key = "zones." + countyId;
		if (cache.containsKey(key)) return (List) cache.get(key);

		List<Jzone> of = new ArrayList();
		List list = zones.listValues();
		for (int i = 0; i < list.size(); i++) {
			Jzone o = (Jzone) list.get(i);
			if (o.getCountyId().equals(countyId)) {
				of.add(o);
			}
		}
		cache.put(key, of);

		return of;
	}

	/**
	 * @param zoneId
	 * @return
	 */
	public static Jzone getZone(String zoneId) {
		load();
		if (zoneId == null || "".equals(zoneId)) return null;

		return (Jzone) zones.get(zoneId);
	}

	/**
	 * @deprecated
	 * @param zoneName
	 * @return
	 */
	public static Jzone getZoneByName(String zoneName) {
		load();
		if (zoneName == null || "".equals(zoneName)) return null;
		return (Jzone) zones.get(zoneName);
	}

	/**
	 *
	 * @param countyId
	 * @param zoneName
	 * @return
	 */
	public static Jzone getZoneByName(String countyId, String zoneName) {
		load();
		if (zoneName == null || "".equals(zoneName)) return null;

		List<Jzone> all = zones.listValues();
		for(Jzone c : all){
			if(c.getCountyId().equals(countyId)
					&& (zoneName.equals(c.getZoneName()) || zoneName.equals(c.getZoneNameEn()) || zoneName.equals(c.getZoneNameTw()))) return c;
		}
		return null;
	}

	/**
	 * @param jsession
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@Action(path = "zones", getRequestBody=Action.GET_REQUEST_BODY.FALSE)
	public void zones(JSession jsession, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String[] countyIds = jsession.getParameter("county_id", "").split(",");
		if(countyIds==null || countyIds.length==0 || JUtilString.isBlank(countyIds[0])){
			jsession.resultString="{}";
			return;
		}

		StringBuffer s = new StringBuffer();
		s.append("{\"datas\":[");
		for(int j=0; j<countyIds.length; j++) {
			if(j>0) s.append(",");
 			String countyId=countyIds[j];
			Jcounty county = Region.getCounty(countyId);
			if(county==null){
				s.append("{}");
				continue;
			}

			List<Jzone> zs = Region.getZones(countyId);

			s.append("{\"countryId\":\"" + county.getCountryId() + "\"");
			s.append(",\"provinceId\":\"" + county.getProvinceId() + "\"");
			s.append(",\"cityId\":\"" + county.getCityId() + "\"");
			s.append(",\"countyId\":\"" + countyId + "\"");
			s.append(",\"zones\":[");
			for (int i = 0; i < zs.size(); i++) {
				if (i > 0) s.append(",");
				s.append(JUtilBean.bean2Json(zs.get(i)));
			}
			s.append("]}");
		}
		s.append("]}");
		jsession.resultString=s.toString();
	}

	/**
	 * 生成对应的js
	 */
	private static String genJs(){
		StringBuffer s=new StringBuffer();

		List<Jcountry> countries=getCountries();
		for(int i=0; i<countries.size(); i++){
			Jcountry r0=countries.get(i);
			s.append("_R0('"+r0.getCountryId()+"','"+r0.getCountryCode()+"','"+r0.getCountryName()+"','"+ SQLUtil.deleteCriminalChars(r0.getCountryNameEn()) +"');\r\n");

			List<Jprovince> provinces=Region.getProvinces(r0.getCountryId());
			for(int j=0; j<provinces.size(); j++){
				Jprovince r1=provinces.get(j);
				s.append("\r\n_R1("+i+",'"+r1.getProvinceId()+"','"+r1.getAreaCode()+"','"+r1.getProvinceNameShort()+"','"+ SQLUtil.deleteCriminalChars(r1.getProvinceNameEn()) +"','"+r1.getProvinceName()+"');\r\n");

				List<Jcity> cities=Region.getCities(r1.getProvinceId());
				for(int k=0; k<cities.size(); k++){
					Jcity r2=cities.get(k);
					s.append("_R2("+i+","+j+",'"+r2.getCityId()+"','','"+r2.getCityName()+"','"+ SQLUtil.deleteCriminalChars(r2.getCityNameEn()) +"');\r\n");

					List<Jcounty> counties=Region.getCounties(r2.getCityId());
					for(int x=0; x<counties.size();x++){
						Jcounty r3=counties.get(x);
						s.append("_R3("+i+","+j+","+k+",'"+r3.getCountyId()+"','','"+r3.getCountyName()+"','"+ SQLUtil.deleteCriminalChars(r3.getCountyNameEn()) +"');\r\n");
					}
				}
			}
		}
		return s.toString();
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println("1");
		Nvwa.startup();
		System.out.println("2");
		try{
			Thread.sleep(15000);
		}catch (Exception e){}
		System.out.println("3");

		String s=genJs();
		System.out.println(s);
	}
}