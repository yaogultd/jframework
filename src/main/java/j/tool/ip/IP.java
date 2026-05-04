package j.tool.ip;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.DatabaseReader.Builder;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import j.core.Startup;
import j.core.common.JArray;
import j.core.common.JProperties;
import j.core.dao.DAO;
import j.core.dao.DB;
import j.core.nvwa.Nvwa;
import j.log.Logger;
import j.core.sys.SysConfig;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilString;
import j.util.JUtilTextWriter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * geoIp数据下载：https://www.maxmind.com/
 * @author 肖炯
 *
 */
public final class IP{
	private static Logger log=Logger.create(IP.class);
	private static ConcurrentMap locations=new ConcurrentMap();
	private static ConcurrentList ips=new ConcurrentList();
	
	private static DatabaseReader geoIpReaderCountry=null;
	private static final Object geoIpLockCountry=new Object();
	
	private static DatabaseReader geoIpReaderCity=null;
	private static final Object geoIpLockCity=new Object();
	
	/**
	 * 得到访问者IP
	 * @param request
	 * @return
	 */
	public static String getRemoteIp(HttpServletRequest request){
		String ip=request.getHeader("x-forwarded-for");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("x-real-ip");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("proxy_forwarded_for");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("true-client-ip");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("remote-host");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("remote-addr");
		if(JUtilString.isBlank(ip)) ip=request.getRemoteHost();

		if(ip.indexOf(",")>0) ip=ip.substring(0,ip.indexOf(","));
		return ip.replaceAll(" ","");
	}
	
	/**
	 * 访问者的多个IP信息
	 * @param request
	 * @return
	 */
	public static String[] getRemoteIps(HttpServletRequest request){
		String ip=request.getHeader("x-forwarded-for");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("x-real-ip");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("proxy_forwarded_for");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("true-client-ip");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("remote-host");
		if(JUtilString.isBlank(ip)) ip=request.getHeader("remote-addr");
		if(JUtilString.isBlank(ip)) ip=request.getRemoteHost();
		
		if(ip.indexOf(",")>0){
			ip=ip.replaceAll(" ","");
			String[] ret=ip.split(",");
			return ret;
		}else{
			return new String[]{ip.trim()};
		}
	}
	
	/**
	 * 将ip地址转换成数值
	 * @param ipCells
	 * @return
	 */
	public static long calIntValue(String[] ipCells){
		String ip="";
		for(int i=0;i<4;i++){
			while(ipCells[i].length()<3){
				ipCells[i]="0"+ipCells[i];
			}
			ip+=ipCells[i];
		}
		return Long.parseLong(ip);
	}
	
	/**
	 * 本地方法
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static String getLocation(String ip) throws Exception{
		try{
			if(!JUtilString.isIP(ip)) return "-";

			if(locations.containsKey(ip)) return (String)locations.get(ip);

			if(JUtilString.isIPv4(ip)){
				String[] ipCells=JUtilString.getTokens(ip,".");

				DAO dao=null;
				String location="-";
				try{
					long ipl=calIntValue(ipCells);

					dao=DB.connect("IP",IP.class);
					j.core.dao.StmtAndRs sr=dao.find("select IP_ADDR from j_ip where IP_START<="+ipl+" AND IP_END>="+ipl);
					java.sql.ResultSet rs=sr.resultSet();
					while(rs!=null&&rs.next()){
						location=rs.getString("IP_ADDR");
						break;
					}
					sr.close();
					dao.close();
					dao=null;
				}catch(Exception e){
					log.log(e,Logger.LEVEL_ERROR);
					try{
						dao.close();
						dao=null;
					}catch(Exception ex){}
					location="-";
				}

				ips.add(ip);
				locations.put(ip,location);
				while(ips.size()>100000){
					String key=(String)ips.remove(0);
					locations.remove(key);
				}
				return location;
			}

			List<String> locations = new ArrayList<>();
			CityResponse cityResponse = geoIpGetCity(ip);
			if(cityResponse!=null){
				Continent continent = cityResponse.getContinent();
				if(continent!=null) locations.add(continent.getName());

				Country country = cityResponse.getCountry();
				if(country!=null) locations.add(country.getName()+"("+country.getIsoCode()+")");

				City city = cityResponse.getCity();
				if(city!=null && !JUtilString.isBlank(city.getName())) locations.add(city.getName());
			}else{
				CountryResponse countryResponse = geoIpGetCountry(ip);
				if(countryResponse!=null){
					Continent continent = countryResponse.getContinent();
					if(continent!=null) locations.add(continent.getName());

					Country country = countryResponse.getCountry();
					if(country!=null) locations.add(country.getName()+"("+country.getIsoCode()+")");
				}
			}

			if(locations.isEmpty()) return "-";
			return JArray.toString(locations, ", ");
		}catch (Exception ignored){
			return "-";
		}
	}		
	
	/**
	 * 
	 * @param ip
	 * @return
	 */
	public static CountryResponse geoIpGetCountry(String ip){
		if("127.0.0.1".equals(ip) || "localhost".equals(ip)) return null;
		try{
			synchronized(geoIpLockCountry){
				if(geoIpReaderCountry==null){
					File database=new File(JProperties.getProperty("GeoIpDbCountry"));
					geoIpReaderCountry=(new Builder(database)).build();
				}
			}
		    InetAddress ipAddress = InetAddress.getByName(ip);
		    CountryResponse response = geoIpReaderCountry.country(ipAddress);
		    return response;
		}catch(Exception ignored){
		    return null;
		}
	}
	
	/**
	 * 
	 * @param ip
	 * @return
	 */
	public static CityResponse geoIpGetCity(String ip){
		if("127.0.0.1".equals(ip) || "localhost".equals(ip)) return null;
		try{
			synchronized(geoIpLockCity){
				if(geoIpReaderCity==null){
					File database=new File(JProperties.getProperty("GeoIpDbCity"));
					geoIpReaderCity=(new Builder(database)).build();
				}
			}

		    InetAddress ipAddress = InetAddress.getByName(ip);
		    CityResponse response = geoIpReaderCity.city(ipAddress);

		    return response;
		}catch(Exception ignored){
		    return null;
		}
	}
	
	/**
	 * 第一步 下载"纯真IP数据库最新版"：https://www.cz88.net/（公众号：纯真IP实验室定期更新）
	 * 第二步 利用纯真IP数据库自带功能解压出IP文本
	 * 第三步 运行以下程序生成sql文件
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)throws Exception{
		JUtilTextWriter log=new JUtilTextWriter(new File("D:\\work\\JFramework\\doc\\通用\\IP库\\ips.sql"),SysConfig.sysEncoding);
		BufferedReader read=new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\work\\JFramework\\doc\\通用\\IP库\\ips.txt")),"gbk"));

		String line=read.readLine();
    	int i=1;
    	while(line!=null){
    		line=read.readLine();

    		if(line.indexOf(" ")<0||line.indexOf("日IP数据")>0) break;

    		while(line.indexOf("  ")>0){
    			line=line.replaceAll("  "," ");
    		}

    		String s=line.substring(0,line.indexOf(" "));
    		line=line.substring(line.indexOf(" ")+1);
    		String e=line.substring(0,line.indexOf(" "));
    		line=line.substring(line.indexOf(" ")+1);
    		line=line.replaceAll("  CZ88.NET","");
    		line=line.replaceAll(" CZ88.NET","");

    		String[] ss=JUtilString.getTokens(s,".");
    		String[] ee=JUtilString.getTokens(e,".");
    		line=JUtilString.replaceAll(line,"'","\\'");
    		//System.out.println(i+", insert into j_ip values ("+i+","+calIntValue(ss)+","+calIntValue(ee)+",'"+line+"');");
    		log.addLine("insert into j_ip values ("+i+","+calIntValue(ss)+","+calIntValue(ee)+",'"+line+"');");
    		i++;
    	}
    	System.out.println("end");
    	log.close();
    	read.close();
	}
}
