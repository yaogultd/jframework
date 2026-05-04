package j.core.sso;

import j.core.annotation.description.FieldDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserAgent implements Serializable {
    @FieldDescription(description = "客户端类型：浏览器")
    public static final String UA_TYPE_BROWSER="BROWSER";

    @FieldDescription(description = "客户端类型：APP")
    public static final String UA_TYPE_APP="APP";

    @FieldDescription(description = "客户端类型：微服务调用者")
    public static final String UA_TYPE_SERVICE_CLIENT="SERVICE";

    @FieldDescription(description = "客户端类型：开放平台调用者")
    public static final String UA_TYPE_OPEN_CLIENT="OPEN";

    @FieldDescription(description = "客户端类型")
    private String uaType;

    @FieldDescription(description = "客户端名称，如谷歌浏览器的uaName为 mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36 (khtml, like gecko) chrome/90.0.4430.93 safari/537.36")
    private String uaName;

    @FieldDescription(description = "客户端标识，如WEB SESSION ID、MEID、微服务/开放平台调用者UUID等")
    private String uaIdentify;

    @FieldDescription(description = "客户端IP地址")
    private String uaIp;

    @FieldDescription(description = "首次登入时间")
    private long connectedAt;

    @FieldDescription(description = "最近请求时间")
    private long requestedAt;
}
