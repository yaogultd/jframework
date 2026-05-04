package j.http.proxy;

import j.core.db.JhttpProxy;
import j.core.sys.SysUtil;
import j.util.JUtilRandom;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 维护代理IP使用者对单个代理IP的使用信息
 */
@Getter
@Setter
public class ProxyUsage {
    //与使用场景关联的ID，由业务指定
    private String id;

    /*
     * 基本信息
     */
    private String proxyUuid;
    private String proxyIp;
    private String proxyIpv6;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private long startUsed;//开始使用时间
    private long latestUsed;//最近一次使用时间
    private int errorsInTotal;//累计网络错误数
    private int errorsContinuous;//连续出现网络错误数
    private int maxErrors;//允许连续出现的最大网络错误数，达到此数时，认为此ip对该使用者无效
    private long maxIdle=300000L;//最大空闲时间

    /*
     * 代理需求
     */
    private int minSurvival;
    private int maxSurvival;
    private boolean httpsSupported;
    private boolean repeatable=false;//是否可重复使用同一代理（false则表示去重）
    private boolean exclusive=false;//是否独占（不使用正被其它使用的代理、自己正在使用的代理不给他人使用）
    private String continentId;
    private String countryId;
    private String provinceId;

    //使用中ip失效时，自动获取新的
    private boolean autoRenew=true;

    //使用记录
    private List<String> usedProxyUuids=new ArrayList<>();

    //是否已经释放
    private boolean released=false;

    /**
     *
     */
    public ProxyUsage(String id){
        this.id=id;
        this.startUsed=SysUtil.getNow();
        this.latestUsed=this.startUsed;
    }

    /**
     * 使用指定代理
     */
    public void use(JhttpProxy proxy){
        this.proxyUuid=proxy.getUuid();
        this.proxyIp=proxy.getProxyIp();
        this.proxyIpv6=proxy.getProxyIpv6();
        this.proxyPort=proxy.getProxyPort();
        this.proxyUsername=proxy.getProxyUsername();
        this.proxyPassword=proxy.getProxyPassword();
        this.errorsContinuous=0;
        this.errorsInTotal=0;
        this.startUsed=SysUtil.getNow();
        this.latestUsed=this.startUsed;
        if(!this.usedProxyUuids.contains(this.proxyUuid)) this.usedProxyUuids.add(this.proxyUuid);
    }

    /**
     * 使用
     */
    public void use(){
        this.latestUsed=SysUtil.getNow();
    }

    /**
     * 正常使用时
     */
    public void onOk(){
        this.errorsContinuous=0;
    }

    /**
     * 出现网络错误时
     */
    public void onError(){
        this.errorsInTotal++;
        this.errorsContinuous++;
    }

    /**
     * 释放（不在使用）
     */
    public void release(){
        this.setReleased(true);
    }

    /**
     * 是否已空闲
     * @return
     */
    public boolean isIdle(){
        return this.maxIdle>0 && (SysUtil.getNow() - this.latestUsed) >= this.maxIdle;
    }
}
