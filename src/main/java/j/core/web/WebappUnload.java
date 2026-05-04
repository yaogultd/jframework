package j.core.web;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import j.core.Startup;
import j.core.annotation.description.ClassDescription;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

@ClassDescription(author = "肖炯", date = "2022/09/03", description = "web应用停止时执行操作")
public class WebappUnload implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("deregistering jdbc driver......");
        try {
            Enumeration drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = (Driver) drivers.nextElement();
                DriverManager.deregisterDriver(driver);
            }
            AbandonedConnectionCleanupThread.uncheckedShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("deregistering jdbc driver finished.");

        Startup.destroy();
    }
}
