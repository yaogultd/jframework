package j.core.webserver.undertow;

import j.core.annotation.configuration.Property;
import j.core.nvwa.NvwaField;
import j.util.JUtilBean;
import j.util.JUtilMath;
import j.util.JUtilTimestamp;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.ResourceBundle;

@Getter
@Setter
public class UndertowConf {
    @Property(description="是否启用")
    private boolean enabled;

    @Property(description="是否启用MCP")
    private boolean mcpEnabled;

    @Property(description="端口")
    private int port;

    @Property(description="根目录")
    private String baseDir;

    @Property(description="应用根目录")
    private String webApp;

    @Property(description="主机名")
    private String host = "localhost";

    @Property(description="部署名")
    private String deploymentName = "";

    @Property(description="io线程数")
    private Integer ioThreads = Math.max(Runtime.getRuntime().availableProcessors(), 10);

    @Property(description="worker线程数")
    private Integer workerThreads = ioThreads * 8;

    @Property(description="是否开启gzip压缩")
    private boolean gzipEnabled = false;

    @Property(description="gzip处理优先级")
    private int gzipPriority = 100;

    @Property(description="压缩级别，默认值 -1。 可配置 1 到 9。 1 拥有最快压缩速度，9 拥有最高压缩率")
    private Integer gzipLevel = -1;

    @Property(description="触发压缩的最小内容长度")
    private Integer gzipMinLength = 1024;

    @Property(description="url是否允许特殊字符")
    private boolean allowUnescapedCharactersInUrl = true;

    @Property(description="是否开启http2")
    private boolean http2enabled = false;

    @Property(description="是否开启ssl")
    private boolean sslEnabled=false;

    @Property(description="ssl端口")
    private int sslPort=8443;

    @Property(description="ssl证书类型")
    private String sslKeyStoreType="JKS";

    @Property(description="ssl证书文件路径")
    private String sslKeyStore="";

    @Property(description="ssl证书密码")
    private String sslKeyStorePassword="";

    @Property(description="ssl context协议类型")
    private String sslContextProtocol="TLS";

    @Property(description="ssl协议")
    private String sslProtocol;

    @Property(description="ssl支持的协议")
    private String sslProtocols;

    @Property(description="ssl支持的算法")
    private String sslCiphers;

    @Property(description="websocket类配置，多个用,分隔")
    private String endpointClasses;

    public UndertowConf(){
        try{
            //从配置文件获取属性值
            ResourceBundle keyValuePairs = ResourceBundle.getBundle("webserver.undertow");
            for(Iterator it = keyValuePairs.keySet().iterator(); it.hasNext();){
                String key=(String)it.next();
                String value=(String)keyValuePairs.getObject(key);
                value=new String(value.getBytes("iso-8859-1"),"UTF-8");

                Field field=null;
                try{
                    field=this.getClass().getDeclaredField(key);
                }catch(Exception e){
                    continue;
                }

                //字段类型
                String fieldType=field.getType().getCanonicalName();

                Method setter=JUtilBean.getSetter(this.getClass(), key, new Class[]{field.getType()});

                if ("java.lang.String".equalsIgnoreCase(fieldType)) {
                    setter.invoke(this, value);
                } else if ("java.lang.Integer".equalsIgnoreCase(fieldType)
                        || NvwaField.TYPE_INTEGER_PLAIN.equals(fieldType)) {
                    if (JUtilMath.isInt(value)) setter.invoke(this, Integer.valueOf(value));
                } else if ("java.lang.Long".equalsIgnoreCase(fieldType)
                        ||NvwaField.TYPE_LONG_PLAIN.equals(fieldType)) {
                    if (JUtilMath.isLong(value)) setter.invoke(this, Long.valueOf(value));
                } else if ("java.lang.Double".equalsIgnoreCase(fieldType)
                        ||NvwaField.TYPE_DOUBLE_PLAIN.equals(fieldType)) {
                    if (JUtilMath.isNumber(value)) setter.invoke(this, Double.valueOf(value));
                } else if ("java.sql.Timestamp".equalsIgnoreCase(fieldType)) {
                    if (JUtilTimestamp.isTimestamp(value)) setter.invoke(this, Timestamp.valueOf(value));
                } else if ("java.lang.Boolean".equalsIgnoreCase(fieldType)
                        ||NvwaField.TYPE_BOOLEAN_PLAIN.equals(fieldType)) {
                    setter.invoke(this, Boolean.valueOf("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value) || "T".equalsIgnoreCase(value)));
                }

                System.out.println("webserver.undertow setting -> "+key+" = "+value);
            }
            //从配置文件获取属性值 end
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
