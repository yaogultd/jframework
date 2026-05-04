package j.core.service;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;

@ClassDescription(author = "肖炯",
        date = "2021/12/15",
        description = "一些微服务约定的名称和功能")
public class Protocol {
    @FieldDescription(description = "服务不存在")
    public static final String RESP_SERVICE_NOT_FOUND="SERVICE_NOT_FOUND";

    @FieldDescription(description = "服务方法不存在")
    public static final String RESP_METHOD_NOT_FOUND="METHOD_NOT_FOUND";

    @FieldDescription(description = "无效请求")
    public static final String RESP_BAD_REQUEST="BAD_REQUEST";

    @FieldDescription(description = "无效请求")
    public static final String RESP_BAD_RESPONSE="BAD_RESPONSE";

    @FieldDescription(description = "反序列化出错")
    public static final String RESP_DESERIALIZE_ERROR="DESERIALIZE_ERROR";

    @FieldDescription(description = "请求行")
    public static final String REQUEST_LINE="REQUEST_LINE";
}
