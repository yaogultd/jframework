package j.http.proxy;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Proxy implements Serializable {
	private String ip;
	private int port;
	private String username;
	private String password;
	private String remark;

	public Proxy(){

	}

	public Proxy(String ip, int port, String username, String password, String remark){
		this.ip=ip;
		this.port=port;
		this.username=username;
		this.password=password;
		this.remark=remark;
	}
}