package j.tool.region;

import j.core.common.JObject;
import lombok.Getter;

@Getter
public class CountryData extends JObject{
	private static final long serialVersionUID = 1L;
	public String code;
	public String code3Chars;
	public String codeDigital;
	public String mobileCode;
	public String currencyCode;
	public String cnName;
	public String enName;
	public String enNameFull;
	public String group;

	//手机号格式校验正则
	public String RE="";
	public String RE2="";
	public String RE3="";
	//手机号格式校验正则 end
	
	/**
	 * 
	 * @param code
	 * @param mobileCode
	 * @param cnName
	 * @param enName
	 * @param group
	 * @param RE
	 */
	public CountryData(String code,String mobileCode,String cnName,String enName,String group,String RE){
		this.code=code;
		this.mobileCode=mobileCode;
		this.cnName=cnName;
		this.enName=enName;
		this.group=group;
		this.RE=RE;
	}

	/**
	 *
	 * @param code
	 * @param mobileCode
	 * @param cnName
	 * @param enName
	 * @param group
	 * @param RE
	 * @param RE2
	 * @param RE3
	 */
	public CountryData(String code,String mobileCode,String cnName,String enName,String group,String RE,String RE2,String RE3){
		this.code=code;
		this.mobileCode=mobileCode;
		this.cnName=cnName;
		this.enName=enName;
		this.group=group;
		this.RE=RE;
		this.RE2=RE2;
		this.RE3=RE3;
	}

	/**
	 *
	 * @param code
	 * @param mobileCode
	 * @param cnName
	 * @param enName
	 * @param group
	 * @param RE
	 * @param RE2
	 * @param RE3
	 */
	public CountryData(String code,
					   String code3Chars,
					   String codeDigital,
					   String mobileCode,
					   String currencyCode,
					   String cnName,
					   String enName,
					   String enNameFull,
					   String group,
					   String RE,
					   String RE2,
					   String RE3){
		this.code=code;
		this.code3Chars=code3Chars;
		this.codeDigital=codeDigital;
		this.mobileCode=mobileCode;
		this.currencyCode=currencyCode;
		this.cnName=cnName;
		this.enName=enName;
		this.enNameFull=enNameFull;
		this.group=group;
		this.RE=RE;
		this.RE2=RE2;
		this.RE3=RE3;
	}
}
