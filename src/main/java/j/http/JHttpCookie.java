package j.http;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 
 * @author 肖炯
 *
 */
@Getter
@Setter
public class JHttpCookie {
	private String name;
	private String value;
	private int version;
	private String domain;
	private String path;
	private Date expired;
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param version
	 * @param domain
	 * @param path
	 */
	public JHttpCookie(String name, String value, int version, String domain, String path) {
		this.name=name;
		this.value=value;
		this.version=version;
		this.domain=domain;
		this.path=path;
	}

	/**
	 *
	 * @param name
	 * @param value
	 * @param version
	 * @param domain
	 * @param path
	 * @param expired
	 */
	public JHttpCookie(String name, String value, int version, String domain, String path, Date expired) {
		this.name=name;
		this.value=value;
		this.version=version;
		this.domain=domain;
		this.path=path;
		this.expired=expired;
	}
}
