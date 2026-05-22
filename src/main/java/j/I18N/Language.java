package j.I18N;

import j.util.JUtilBean;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Getter
@Setter

public class Language implements Serializable{
	private String bcp47Code;      // 完整 BCP 47 编码 (如 zh-Hans, en-US)
	private String baseCode;       // 基础语言编码 (如 zh, en)自动提取
	private String englishName;    // 英文名称
	private String chineseName;    // 中文名称
	private String nativeName;     // 语种自己的名称
	private List<String> primaryRegions; // 主要使用的国家/地区 ISO 编码

	public Language(){

	}

	public Language(String bcp47Code, String englishName, String chineseName, String nativeName, List<String> primaryRegions) {
		this.bcp47Code = bcp47Code;

		// 核心修改：自动提取基础编码（截取第一个 "-" 之前的部分）
		// 如果没有 "-", 则说明本身就是基础编码（如 "ja" -> "ja"）
		this.baseCode = bcp47Code.contains("-") ? bcp47Code.split("-")[0] : bcp47Code;

		this.englishName = englishName;
		this.chineseName = chineseName;
		this.nativeName = nativeName;
		this.primaryRegions = primaryRegions != null ?
				new ArrayList<>(primaryRegions) :
				Collections.emptyList();
	}

	/**
	 *
	 */
	public void setBaseCodeFromBcp47Code(){
		this.baseCode = bcp47Code.contains("-") ? bcp47Code.split("-")[0] : bcp47Code;
	}

	/**
	 * 默认获取中文名
	 * @param name
	 */
	public void setName(String name){
		this.chineseName = name;
	}

	/**
	 * 默认设置中文名
	 * @return
	 */
	public String getName(){
		return this.chineseName;
	}

	/**
	 *
	 * @param countryCode
	 * @return
	 */
	public boolean matches(String countryCode){
		if(JUtilString.isBlank(countryCode)) return false;
		return this.primaryRegions != null &&  this.primaryRegions.contains(countryCode.toUpperCase());
	}


	@Override
	public String toString() {
		return JUtilBean.bean2Json(this);
	}
}