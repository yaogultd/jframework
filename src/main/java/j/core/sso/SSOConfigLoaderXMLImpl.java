package j.core.sso;

import j.util.JUtilString;
import org.dom4j.Element;

import java.util.LinkedList;
import java.util.List;

public class SSOConfigLoaderXMLImpl implements SSOConfigLoader{
	@Override
	public List loadClients() throws Exception{
		//nothing
		return null;
	}

	@Override
	public List loadClients(Element root) throws Exception{
		List ssoClients=new LinkedList();
		List clients=root.elements("client");
		for(int i=0;clients!=null&&i<clients.size();i++){
			Element client=(Element)clients.get(i);
			Client c=new Client();
			
			c.setIsSsoServer("true".equalsIgnoreCase(client.attributeValue("isssoserver")));
			c.setCanLogin(!"false".equalsIgnoreCase(client.attributeValue("can-login")));
			c.setId(client.elementText("id"));
			c.setName(client.elementText("name"));
			c.setAccessKey(client.elementText("AccessKey"));
			c.setAccessSecret(client.elementText("AccessSecret"));
			c.setAesKey(client.elementText("AES-KEY"));
			c.setAesOffset(client.elementText("AES-OFFSET"));
			
			List domains=client.elements("domain");
			for(int j=0;j<domains.size();j++){
				Element domainE=(Element)domains.get(j);
				c.addDomain(domainE.getTextTrim());
			}
			
			List urls=client.elements("url");
			for(int j=0;j<urls.size();j++){
				Element urlE=(Element)urls.get(j);
			
				String url=urlE.getTextTrim();
				if(!url.endsWith("/")){
					url+="/";
				}
				
				if("true".equalsIgnoreCase(urlE.attributeValue("default"))){
					c.setUrlDefault(url);
				}
				
				c.addUrl(url);
			}
			
			c.setLoginPage(client.elementText("login-page"));
			c.setHomePage(client.elementText("home-page"));
			c.setPassport(client.elementText("passport"));
			c.setLoginInterface(client.elementText("login-interface"));
			c.setLogoutInterface(client.elementText("logout-interface"));

			Element loginAgentEle=client.element("login-agent");

			LoginAgent la=new LoginAgent(c.getId(),
					loginAgentEle.attributeValue("avail"),
					loginAgentEle.attributeValue("for-other-clients"),
					loginAgentEle.attributeValue("authenticator"),
					JUtilString.appendUrl(c.getUrlDefault(), loginAgentEle.attributeValue("interface")));
			c.setLoginAgent(la);
			
			c.setUserClass(client.elementText("user-class"));
			
			List props=client.elements("property");
			for(int j=0;props!=null&&j<props.size();j++){
				Element prop=(Element)props.get(j);
				if(prop.attributeValue("key")!=null
					&&prop.attributeValue("value")!=null){
					c.setProperty(prop.attributeValue("key"), prop.attributeValue("value"));
				}else if(prop.attributeValue("name")!=null){
					c.setProperty(prop.attributeValue("name"), prop.getText());
				}
			}
			
			ssoClients.add(c);
		}
		
		return ssoClients;
	}
}
