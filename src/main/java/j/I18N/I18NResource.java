package j.I18N;

import j.util.ConcurrentMap;
import lombok.Getter;

import java.io.Serializable;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
public class I18NResource implements Serializable {
	private static final long serialVersionUID = 1L;
	private String file;
	private String group;
	private String key;
	private ConcurrentMap<String, String> languages=new ConcurrentMap<>();
	
	/**
	 * 
	 * @param file
	 * @param group
	 * @param key
	 */
	public I18NResource(String file,String group,String key){
		this.file=file;
		this.group=group;
		this.key=key;
	}
	
	/**
	 * 
	 * @param language
	 * @param text
	 */
	public void addLanguage(String language,String text){
		languages.put(language,text);
	}
	
	/**
	 * 
	 * @param language
	 * @return
	 */
	public String getLanguage(String language){
		String text=languages.get(language);
		if(text==null) text=languages.get(I18N.defaultLanguage);
		return text;
	}
}