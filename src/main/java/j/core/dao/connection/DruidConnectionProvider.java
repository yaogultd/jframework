package j.core.dao.connection;

import com.alibaba.druid.pool.DruidDataSource;
import j.util.JUtilMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DruidConnectionProvider implements ConnectionProvider{
    private static final Logger log = LoggerFactory.getLogger(DruidConnectionProvider.class);
    private DataSource ds;
    private Integer isolation;

    /*
     *  (non-Javadoc)
     * @see j.core.dao.connection.ConnectionProvider#getConnection()
     */
    public Connection getConnection() throws SQLException {
        final Connection c = ds.getConnection();
        if (isolation != null){
            c.setTransactionIsolation(isolation.intValue());
        }
        if (c.getAutoCommit()){
            c.setAutoCommit(false);
        }
        return c;
    }

    /*
     *  (non-Javadoc)
     * @see j.core.dao.connection.ConnectionProvider#closeConnection(java.sql.Connection)
     */
    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    /*
     *  (non-Javadoc)
     * @see j.core.dao.connection.ConnectionProvider#configure(java.util.Properties)
     */
    public void configure(Properties props) throws Exception {
        String jdbcDriverClass = props.getProperty(Environment.DRIVER);
        String jdbcUrl = props.getProperty(Environment.URL);

        System.out.println("Druid using driver: " + jdbcDriverClass + " at URL: "+ jdbcUrl);
        System.out.println("Connection properties: " + props);

        if (jdbcDriverClass == null) {
            log.warn("No JDBC Driver class was specified by property " + Environment.DRIVER);
        } else {
            try {
                Class.forName(jdbcDriverClass);
            } catch (ClassNotFoundException cnfe) {
                String msg = "JDBC Driver class not found: " + jdbcDriverClass;
                log.error(msg);
                throw new Exception(msg);
            }
        }

        try {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(jdbcUrl);
            dataSource.setDriverClassName(jdbcDriverClass);
            dataSource.setUsername(props.getProperty(Environment.USER));
            dataSource.setPassword(props.getProperty(Environment.PASS));

            int initialPoolSize=10;
            int maxPoolSize=100;
            int minIdle=10;
            int maxWait=3000;

            String sInitialPoolSize=props.getProperty(Environment.INITIAL_POOL_SIZE);
            String sMaxPoolSize=props.getProperty(Environment.POOL_SIZE);
            String sMinIdle=props.getProperty(Environment.MIN_IDLE);
            String sMaxWait=props.getProperty(Environment.MAX_WAIT);

            if(JUtilMath.isInt(sInitialPoolSize)) initialPoolSize=Integer.parseInt(sInitialPoolSize);
            if(JUtilMath.isInt(sMaxPoolSize)) maxPoolSize=Integer.parseInt(sMaxPoolSize);
            if(JUtilMath.isInt(sMinIdle)) minIdle=Integer.parseInt(sMinIdle);
            if(JUtilMath.isInt(sMaxWait)) maxWait=Integer.parseInt(sMaxWait);

            dataSource.setInitialSize(initialPoolSize);  //初始连接数，默认0
            dataSource.setMaxActive(maxPoolSize);  //最大连接数，默认8
            dataSource.setMinIdle(minIdle);  //最小闲置数
            dataSource.setMaxWait(maxWait);  //获取连接的最大等待时间，单位毫秒
            //dataSource.setPoolPreparedStatements(true); //缓存PreparedStatement，默认false
            //dataSource.setMaxOpenPreparedStatements(20); //缓存PreparedStatement的最大数量，默认-1（不缓存）。大于0时会自动开启缓存PreparedStatement，所以可以省略上一句代码
            dataSource.setTestWhileIdle(true);
            dataSource.setValidationQuery("SELECT 1");

            Properties druidProperties=new Properties();
            druidProperties.put("druid.mysql.usePingMethod", "false");

            dataSource.setConnectProperties(druidProperties);

            ds = dataSource;
        } catch (Exception e) {
            log.error("could not instantiate druid connection pool", e);
            throw new Exception("Could not instantiate druid connection pool", e);
        }

        String i = props.getProperty(Environment.ISOLATION);
        if (i == null) {
            isolation = null;
        } else {
            isolation = Integer.valueOf(i);
            log.info("JDBC isolation level: " + Environment.isolationLevelToString(isolation.intValue()));
        }
    }

    /*
     *  (non-Javadoc)
     * @see j.core.dao.connection.ConnectionProvider#close()
     */
    public void close() {

    }
}
