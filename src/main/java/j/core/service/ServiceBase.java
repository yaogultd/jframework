package j.core.service;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.core.nio.DataSource;
import j.core.nio.DataSourceFile;
import j.core.service.server.config.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/07/14",
        description = "服务需继承该类，每个服务方法的参数都必须是ServiceBase中几个noOp方法中的一种", reviewers = {})
@Getter
@Setter
@j.core.annotation.service.Service(path = "/service/base")
public class ServiceBase {
    @FieldDescription(description = "服务配置信息")
    protected Service config;

    /**
     * 如果请求数据中包含headers、params、files、payload任意一项，则尝试匹配参数列表与本方法相同的方法，如未找到对应方法，再调用j.core.lang.Methods.matches方法寻找与参数列表匹配的方法
     * @param headers 头信息
     * @param params 参数
     * @param files 文件
     * @param payload 请求数据（通常可能是json格式的字符串）
     * @param objects 其它业务自定义类型的对象
     * @return
     * @throws Exception
     */
    @j.core.annotation.service.Service(path = "noOp")
    public ServiceResponse noOp(Map<String, String> headers,
                                Map<String, String> params,
                                Map<String, DataSourceFile> files,
                                String payload,
                                Object[] objects) throws Exception{
        return null;
    }
}
