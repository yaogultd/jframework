package j.core.dao.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;


/**
 *
 */
public final class Environment {
	public static final String CONNECTION_PREFIX = "connection";

	/**
	 * <tt>ConnectionProvider</tt> implementor to use when obtaining connections
	 */
	public static final String CONNECTION_PROVIDER ="connection.provider_class";

	/**
	 * JDBC driver class
	 */
	public static final String DRIVER ="connection.driver_class";

	/**
	 * JDBC transaction isolation level
	 */
	public static final String ISOLATION ="connection.isolation";

	/**
	 * JDBC URL
	 */
	public static final String URL ="connection.url";

	/**
	 * JDBC user
	 */
	public static final String USER ="connection.username";

	/**
	 * JDBC password
	 */
	public static final String PASS ="connection.password";

	public static final String POOL_SIZE ="connection.pool_size";
	public static final String INITIAL_POOL_SIZE ="connection.initial_pool_size";
	public static final String MIN_IDLE ="connection.min_idle";
	public static final String MAX_WAIT ="connection.max_wait";
	
	/**
	 * Enable logging of generated SQL to the console
	 */
	public static final String SHOW_SQL ="show_sql";

	private static final HashMap ISOLATION_LEVELS = new HashMap();
	
	private static final Logger log = LoggerFactory.getLogger(Environment.class);
	
	static {				
		ISOLATION_LEVELS.put( Integer.valueOf(Connection.TRANSACTION_NONE), "NONE" );
		ISOLATION_LEVELS.put( Integer.valueOf(Connection.TRANSACTION_READ_UNCOMMITTED), "READ_UNCOMMITTED" );
		ISOLATION_LEVELS.put( Integer.valueOf(Connection.TRANSACTION_READ_COMMITTED), "READ_COMMITTED" );
		ISOLATION_LEVELS.put( Integer.valueOf(Connection.TRANSACTION_REPEATABLE_READ), "REPEATABLE_READ" );
		ISOLATION_LEVELS.put( Integer.valueOf(Connection.TRANSACTION_SERIALIZABLE), "SERIALIZABLE" );
	}
	
	/**
	 * Get the name of a JDBC transaction isolation level
	 *
	 * @see java.sql.Connection
	 * @param isolation as defined by <tt>java.sql.Connection</tt>
	 * @return a human-readable name
	 */
	public static String isolationLevelToString(int isolation) {
		return (String) ISOLATION_LEVELS.get( Integer.valueOf(isolation) );
	}
}







