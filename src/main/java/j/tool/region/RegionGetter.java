package j.tool.region;

import j.core.fs.JDFSFile;
import j.http.JHttp;
import j.http.JHttpContext;
import j.util.JUtilJSON;
import j.util.JUtilString;
import j.util.JUtilTextWriter;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class RegionGetter{	
	private static Map codes=new HashMap();
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		fromTaobao();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private static void fromTaobao() throws Exception{
		//国家信息
		String counties="";

		//获取各国手机号区号（从淘宝-新增收货地址页面中获得）
		Map<String, String> phoneCodes=new HashMap<>();
		String s=JDFSFile.read(new File("D:\\work\\JFramework\\doc\\common\\regions\\countryPhoneCode.txt"),"UTF-8");
		JSONObject json=JUtilJSON.parse(s);
		JSONArray others=JUtilJSON.array(json, "others");
		for(var i=0; i<others.length(); i++){
			JSONObject temp=JUtilJSON.get(others, i);
			JSONArray items=JUtilJSON.array(temp, "items");

			for(var j=0; j<items.length(); j++){
				JSONObject temp2=JUtilJSON.get(items, j);

				String iso=JUtilJSON.string(temp2, "iso");
				String phonePrefix=JUtilJSON.string(temp2, "phonePrefix");
				phonePrefix=JUtilString.replaceAll(phonePrefix, " ", "");
				phonePrefix=JUtilString.replaceAll(phonePrefix, "(", "");
				phonePrefix=JUtilString.replaceAll(phonePrefix, ")", "");
				phoneCodes.put(iso, phonePrefix);
			}
		}

		JSONArray recommends=JUtilJSON.array(json, "recommends");
		for(var i=0; i<recommends.length(); i++){
			JSONObject temp=JUtilJSON.get(recommends, i);
			JSONArray items=JUtilJSON.array(temp, "items");

			for(var j=0; j<items.length(); j++){
				JSONObject temp2=JUtilJSON.get(items, j);

				String iso=JUtilJSON.string(temp2, "iso");
				String phonePrefix=JUtilJSON.string(temp2, "phonePrefix");
				phoneCodes.put(iso, phonePrefix);
			}
		}

		JHttp http=JHttp.getInstance();
		HttpClient client=http.createClient();
		
		String continentId="0";
		String countryId="1";
		String provinceId=null;
		String cityId=null;
		String countyId=null;

		//获取数据并生成sql，保存到文件
		JUtilTextWriter log=new JUtilTextWriter(new File("D:\\work\\JFramework\\doc\\通用\\地域库\\regions.sql"),"UTF-8");

		//这个js的获取地址：https://division-data.alicdn.com/simple/addr_4_1111_1_0.js
		s=JDFSFile.read(new File("D:\\work\\JFramework\\doc\\通用\\地域库\\tdist.js.txt"),"GBK");
		s=s.replaceAll("e3", "000");
		s=s.replaceAll("e4", "0000");
		s=s.replaceAll("e5", "00000");
		//ID中的e4表示0000，e3表示000，e5表示00000
		//System.out.println(s);
		
		JSONObject regions=JUtilJSON.parse(s);
		
		List<String> _keys=new ArrayList();
		Iterator keys=regions.keys();
		while(keys.hasNext()) {
			String key=(String)keys.next();
			_keys.add(key);
		}

		for(int x=0; x<_keys.size(); x++) {
			countryId = _keys.get(x);
			if (countryId.length() > 3) continue;//不是国家

			//非中国数据通过另外接口获取
			if (!"1".equals(countryId)) continue;

			JSONArray country = regions.getJSONArray(countryId);
			String name = country.getString(0);
			String nameEn = "China";

			countryId="8";//中国id改成8，跟燕文物流统一，方便以后调用燕文接口

			log.addLine("insert into j_country values ('" + countryId + "','0','CN','" + name + "','" + JUtilString.toZhTw(name) + "','" + nameEn + "','+86',0,'T');");
			System.out.println("insert into j_country values ('" + countryId + "','0','CN','" + name + "','" + JUtilString.toZhTw(name) + "','" + nameEn + "','+86',0,'T');");

			counties+="countries.add(new CountryData(\"CN\",\"86\",\"中国大陆\",\"China\",\"\",\"^(\\\\+|(00))?((86\\\\-)|(86))\\\\d[\\\\d\\\\-]+\\\\d$\"));\r\n";

			for(int i=0; i<_keys.size(); i++) {
				provinceId=_keys.get(i);
				
				//["安徽省", "1", "an hui sheng", ""],
				JSONArray province=regions.getJSONArray(provinceId);
				String pid=province.getString(1);


				if(!pid.equals("1")) continue;//不属于该国家

				if("1".equals(pid)) pid="8";//中国id改成8，跟燕文物流统一，方便以后调用燕文接口


				name=province.getString(0);
				nameEn=province.getString(2);
				
				String nameShort=name;
				nameShort=nameShort.replaceAll("市","");
				nameShort=nameShort.replaceAll("维吾尔自治区","");
				nameShort=nameShort.replaceAll("壮族自治区","");
				nameShort=nameShort.replaceAll("回族自治区","");
				nameShort=nameShort.replaceAll("自治区","");

				String provinceCode="";
				if(name.indexOf("香港")>-1) provinceCode="HK";
				else if(name.indexOf("澳门")>-1) provinceCode="MO";
				else if(name.indexOf("台湾")>-1) provinceCode="TW";
				
				log.addLine(" insert into j_province values ('"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+nameShort+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+provinceCode+"',0,'','T');");
				System.out.println(" insert into j_province values ('"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+nameShort+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+provinceCode+"',0,'','T');");
				
				//城市
				for(int j=0; j<_keys.size(); j++) {
					cityId=_keys.get(j);
					
					//["安徽省", "1", "an hui sheng", ""],
					JSONArray city=regions.getJSONArray(cityId);
					pid=city.getString(1);
					if(!pid.equals(provinceId)) continue;//不属于该省
					
					name=city.getString(0);
					nameEn=city.getString(2);
					
					nameShort=name;
					nameShort=nameShort.replaceAll("市","");
					nameShort=nameShort.replaceAll("维吾尔自治区","");
					nameShort=nameShort.replaceAll("壮族自治区","");
					nameShort=nameShort.replaceAll("回族自治区","");
					nameShort=nameShort.replaceAll("自治区","");
					
					String areaCode="";
					String postCode="";
					
					log.addLine("  insert into j_city values ('"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+areaCode+"',0,'"+postCode+"','T');");
					System.out.println("  insert into j_city values ('"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+areaCode+"',0,'"+postCode+"','T');");
				
					//区县
					boolean hasCounties=false;//市下面是否有区县
					for(int k=0; k<_keys.size(); k++) {
						countyId=_keys.get(k);
						
						//["安徽省", "1", "an hui sheng", ""],
						JSONArray county=regions.getJSONArray(countyId);
						pid=county.getString(1);
						
						if(!pid.equals(cityId)) continue;//不属于城市
						
						hasCounties=true;
						
						name=county.getString(0);
						nameEn=county.getString(2);
						
						nameShort=name;
						nameShort=nameShort.replaceAll("市","");
						nameShort=nameShort.replaceAll("维吾尔自治区","");
						nameShort=nameShort.replaceAll("壮族自治区","");
						nameShort=nameShort.replaceAll("回族自治区","");
						nameShort=nameShort.replaceAll("自治区","");
											
						log.addLine("   insert into j_county values ('"+countyId+"','"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+areaCode+"',0,'"+postCode+"','T');");
						System.out.println("   insert into j_county values ('"+countyId+"','"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+areaCode+"',0,'"+postCode+"','T');");
					
						//乡镇、街道
						if(countryId.equals("8")) {
							JHttpContext context=new JHttpContext();
							context.addRequestHeader("Referer","http://buy.taobao.com/auction/buy_now.jhtml");
							String zones=http.postResponse(context,client,"http://lsp.wuliu.taobao.com/locationservice/addr/output_address_town.do?l1="+provinceId+"&l2="+cityId+"&l3="+countyId+"&callback=jsonp460",null,"UTF-8");
							if(zones.indexOf("jsonp460(")<0) {
								System.out.println(name+" -> 未获取到街道乡镇 -> "+zones);
							}else {							
								zones=zones.substring(zones.indexOf("jsonp460(")+"jsonp460(".length(), zones.length()-2);
								zones=JUtilString.replaceAll(zones, "'", "\"");
								zones=JUtilString.replaceAll(zones, "success:", "\"success\":");
								zones=JUtilString.replaceAll(zones, "result:", "\"result\":");
								System.out.println(name+" ->> 获取到街道乡镇 -> "+zones);
								
								JSONObject zonesJson=JUtilJSON.parse(zones);
								zonesJson=JUtilJSON.object(zonesJson, "result");
								
								Iterator zonesKeys=zonesJson.keys();
							
								while(zonesKeys.hasNext()) {
									String zonesKey=(String)zonesKeys.next();
									JSONArray zone=JUtilJSON.array(zonesJson, zonesKey);
									
									String zoneId=zonesKey;
									
									name=zone.getString(0);
									nameEn=zone.getString(2);
									
									log.addLine("    insert into j_zone values ('"+zoneId+"','"+countyId+"','"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','',0,'','T');");
									System.out.println("    insert into j_zone values ('"+zoneId+"','"+countyId+"','"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','',0,'','T');");
								}
							}
						}
					}
					
					if(!hasCounties && countryId.equals("1")) {//如果没有区县（比如中山市下面就没有区县），将乡镇街道作为区县
						//乡镇、街道（作为区县）
						JHttpContext context=new JHttpContext();
						
						context.addRequestHeader("Referer","http://buy.taobao.com/auction/buy_now.jhtml");
						String zones=http.postResponse(context,client,"http://lsp.wuliu.taobao.com/locationservice/addr/output_address_town.do?l1="+provinceId+"&l2="+cityId+"&l3="+cityId+"&callback=jsonp460",null,"UTF-8");
						
						if(zones.indexOf("jsonp460(")<0) {
							System.out.println(name+" -> 未获取到街道乡镇 -> "+zones);
						}else {						
							zones=zones.substring(zones.indexOf("jsonp460(")+"jsonp460(".length(), zones.length()-2);
							zones=JUtilString.replaceAll(zones, "'", "\"");
							zones=JUtilString.replaceAll(zones, "success:", "\"success\":");
							zones=JUtilString.replaceAll(zones, "result:", "\"result\":");
							System.out.println(name+" ->> 获取到街道乡镇 -> "+zones);
							
							JSONObject zonesJson=JUtilJSON.parse(zones);
							zonesJson=JUtilJSON.object(zonesJson, "result");
							
							Iterator zonesKeys=zonesJson.keys();
						
							while(zonesKeys.hasNext()) {
								String zonesKey=(String)zonesKeys.next();
								JSONArray zone=JUtilJSON.array(zonesJson, zonesKey);
								String zoneId=zonesKey;
								
								name=zone.getString(0);
								nameEn=zone.getString(2);
								
								log.addLine("   insert into j_county values ('"+zoneId+"','"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+areaCode+"',0,'"+postCode+"','T');");
								System.out.println("   insert into j_county values ('"+zoneId+"','"+cityId+"','"+provinceId+"','"+countryId+"','"+continentId+"','"+name+"','"+JUtilString.toZhTw(name)+"','"+nameEn+"','"+areaCode+"',0,'"+postCode+"','T');");
							}
						}
					}
				}
			}
		}

		//从countryNameEn.txt获取其它国家（来自燕文物流官网）
		s=JDFSFile.read(new File("D:\\work\\JFramework\\doc\\通用\\地域库\\countryNameEn.txt"),"UTF-8");
		JSONArray countries=JUtilJSON.array(JUtilJSON.parse(s), "data");
		for(int i=0; i<countries.length(); i++){
			JSONObject o=JUtilJSON.get(countries, i);

			countryId=JUtilJSON.string(o, "id");
			String nameCh=JUtilJSON.string(o, "nameCh");
			String nameEn=JUtilJSON.string(o, "nameEn");
			String nameEn2=nameEn;
			nameEn2=JUtilString.upperFirstChar(nameEn2.toLowerCase());

			String code=JUtilJSON.string(o, "code");

			//中国、中国香港、中国澳门、中国台湾
			if("CN".equals(code)
					||"HK".equals(code)
					||"TW".equals(code)
					||"MO".equals(code)) continue;

			String phoneCode=phoneCodes.get(code);
			if(phoneCode==null) phoneCode="";
			if(phoneCode.startsWith("+")) phoneCode=phoneCode.substring(1);

			System.out.println(phoneCode+" -> "+code+" -> "+nameCh);

			nameEn=JUtilString.upperFirstChar(nameEn.toLowerCase());
			nameEn=JUtilString.replaceAll(nameEn, "'", "\\'");

			log.addLine("insert into j_country values ('" + countryId + "','0','"+code+"','" + nameCh + "','" + JUtilString.toZhTw(nameCh) + "','" + nameEn + "','"+phoneCode+"',0,'T');");
			System.out.println("insert into j_country values ('" + countryId + "','0','"+code+"','" + nameCh + "','" + JUtilString.toZhTw(nameCh) + "','" + nameEn + "','"+phoneCode+"',0,'T');");

			if(!JUtilString.isBlank(phoneCode)){
				counties+="countries.add(new CountryData(\""+code+"\",\""+phoneCode+"\",\""+nameCh+"\",\""+nameEn2+"\",\"\",\"^(\\\\+|(00))?(("+phoneCode+"\\\\-)|("+phoneCode+"))\\\\d[\\\\d\\\\-]+\\\\d$\"));\r\n";
			}
		}
		//从countryNameEn.txt获取其它国家（来自燕文物流官网） end

		System.out.println(counties);
		System.exit(0);
	}
}
