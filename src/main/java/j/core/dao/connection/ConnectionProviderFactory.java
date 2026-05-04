package j.core.dao.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/**
 *
 */
public final class ConnectionProviderFactory {
	private static final Logger log = LoggerFactory.getLogger(ConnectionProviderFactory.class);

	private static final Set SPECIAL_PROPERTIES;
	static {
		SPECIAL_PROPERTIES = new HashSet();
		SPECIAL_PROPERTIES.add(Environment.URL);
		SPECIAL_PROPERTIES.add(Environment.CONNECTION_PROVIDER);
		SPECIAL_PROPERTIES.add(Environment.POOL_SIZE);
		SPECIAL_PROPERTIES.add(Environment.ISOLATION);
		SPECIAL_PROPERTIES.add(Environment.DRIVER);
		SPECIAL_PROPERTIES.add(Environment.USER);
	}

	/**
	 * Instantiate a <tt>ConnectionProvider</tt> using given properties.
	 * Method newConnectionProvider.
	 * 
	 * @param properties hibernate <tt>SessionFactory</tt> properties
	 * @return ConnectionProvider
	 * @throws Exception
	 */
	public static ConnectionProvider newConnectionProvider(Properties properties) throws Exception {
		ConnectionProvider connections;
		String providerClass = properties.getProperty(Environment.CONNECTION_PROVIDER);

		System.out.println("connection properties: "+properties);
		System.out.println("Initializing connection provider: "+providerClass);
		if (providerClass != null) {
			try {
				connections = (ConnectionProvider) Class.forName(providerClass).newInstance();
			} catch (Exception e) {
				log.error("Could not instantiate connection provider", e);
				throw new Exception("Could not instantiate connection provider: "+ providerClass);
			}
		}else {
			throw new Exception("no connection provider");
		}
		connections.configure(properties);
		return connections;
	}
}
