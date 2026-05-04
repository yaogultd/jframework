package j.core.sso;

import org.dom4j.Element;

import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
public interface SSOConfigLoader {
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public List loadClients() throws Exception;

	public List loadClients(Element root) throws Exception;
}
