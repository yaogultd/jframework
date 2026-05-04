package j.core.service.registry;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.common.Global;
import j.core.service.server.config.Service;
import j.core.sys.SysUtil;
import j.util.JUtilJSON;
import j.util.JUtilSorter;
import j.util.JUtilString;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ClassDescription(author = "肖炯",
        date = "2021/10/23",
        description = "服务注册信息")
@Getter
@Setter
public class Registration {
    @FieldDescription(description = "对端口排序，选取最空闲的")
    private static PortSorter portSorter=new PortSorter();

    @FieldDescription(description = "通信层类型")
    private String networker;

    @FieldDescription(description = "位于哪个主机/IP地址")
    private String host;

    @FieldDescription(description = "该服务配置信息")
    private Service service;

    @FieldDescription(description = "注册时间")
    private long regAt;

    @FieldDescription(description = "心跳时间")
    private long heartbeatAt;

    @FieldDescription(description = "是否暂停")
    private boolean paused=false;

    @FieldDescription(description = "该节点正在处理中任务数")
    private int packages=0;

    @FieldDescription(description = "节点运行时UUID，用于判定是否本地服务")
    private String runUuid="";

    /**
     *
     * @param networker
     * @param host
     * @param service
     */
    public Registration(String networker, String host, Service service){
        this.networker=networker;
        this.host=host;
        this.service=service;
        this.regAt=SysUtil.getNow();
        this.heartbeatAt=this.regAt;
    }

    /**
     *
     */
    public void heartbeat(){
        this.heartbeatAt=SysUtil.getNow();
    }

    /**
     *
     */
    public void pause(){
        this.paused=true;
    }

    /**
     *
     */
    public void resume(){
        this.paused=false;
    }

    /**
     * 多个端口提供服务时，选取其中最空闲的一个（实现端口上的负载均衡）
     * @return
     */
    public Integer getPort(){
        List<Integer> ports=new ArrayList<>();
        ports.addAll(this.service.getPorts());

        if(ports.isEmpty()) return 0;

        if(ports.size()>1) ports=portSorter.bubble(ports, JUtilSorter.ASC, this.service);

        return ports.get(0);
    }

    /**
     * 是否和上下文处于同一运行时环境中
     * @return
     */
    public boolean isLocal(){
        return JUtilString.equals(this.getRunUuid(), Global.getRunUuid());
    }

    @Override
    public String toString(){
        StringBuffer s=new StringBuffer();
        s.append("{\""+Registry.FLAG_NETWORKER+"\":\""+this.networker+"\"");
        s.append(",\""+Registry.FLAG_HOST+"\":\""+this.host+"\"");
        s.append(",\""+Registry.FLAG_SERVICE+"\":"+this.service.toString());
        s.append(",\""+Registry.FLAG_REG_AT+"\":"+this.regAt);
        s.append(",\""+Registry.FLAG_HEARTBEAT_AT+"\":"+this.heartbeatAt);
        s.append(",\""+Registry.FLAG_PACKAGES +"\":"+this.packages);
        s.append(",\""+Registry.FLAG_RUN_UUID+"\":\""+this.runUuid+"\"");

        s.append("}");
        return s.toString();
    }

    /**
     *
     * @param json
     * @return
     */
    public static Registration fromJson(JSONObject json){
        Service service=Service.fromJson(JUtilJSON.object(json, Registry.FLAG_SERVICE));

        Registration r = new Registration(JUtilJSON.string(json, Registry.FLAG_NETWORKER),
                JUtilJSON.string(json, Registry.FLAG_HOST),
                service);
        r.setRegAt(JUtilJSON.getLong(json, Registry.FLAG_REG_AT));
        r.setHeartbeatAt(JUtilJSON.getLong(json, Registry.FLAG_HEARTBEAT_AT));
        r.setPackages(JUtilJSON.getInteger(json, Registry.FLAG_PACKAGES));
        r.setRunUuid(JUtilJSON.string(json, Registry.FLAG_RUN_UUID));

        Boolean _paused=JUtilJSON.getBoolean(json, Registry.FLAG_PAUSED);
        r.setPaused(_paused==null?false:_paused);

        return r;
    }

    /**
     * 最近5秒之内有心跳表示在线
     * @return
     */
    public boolean available(){
        return SysUtil.getNow() - this.heartbeatAt < 5000L && !this.paused;
    }
}
