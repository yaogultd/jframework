package j.sms;


import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class SMSSenderConfig {	
	protected String id;
	protected int priority=0;
	protected String[] regions;
	protected String business;
	protected String businessName;
	protected String channelImpl;
	protected String from;
	protected String fromName;
	protected int threads;
	protected int maxTries;
	protected Map<String, String> properties = new HashMap<>();

	public void setRegionString(String regions){
		this.regions=regions.split(",");
	}


	public void setProperty(String key, String value){
		this.properties.put(key, value);
	}
	public String getProperty(String key){
		return this.properties.get(key);
	}
}