package j.core.web.mcp.client;

import j.core.web.mcp.McpJson;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestParam {
    private String name;
    private String type;
    private Object value;
    private String meta;

    public RequestParam(){}

    public RequestParam(String name, String type, Object value, String meta){
        this.name = name;
        this.type = type;
        this.value = value;
        this.meta = meta;
    }

    /**
     * 将请求参数列表转换成json串
     * @param params
     * @return
     */
    public static String toJson(List<RequestParam> params) {
        if(params.isEmpty()) return "none";
        return McpJson.mapper().writeValueAsString(params);
    }

    /**
     * 将json串转换成RequestParam对象列表
     * @param json
     * @return
     */
    public static List<RequestParam> fromJson(String json){
        return McpJson.mapper().readValue(json, new TypeReference<>() {});
    }

    /**
     * 获取指定名字的参数
     * @param params
     * @param name
     * @return
     */
    public static RequestParam getParam(List<RequestParam> params, String name){
        for(RequestParam param : params) {
            if(name.equals(param.getName())) return param;
        }
        return null;
    }


    /**
     * 获取名字以指定字符开头的参数列表
     * @param params
     * @param namePrefix
     * @return
     */
    public static List<RequestParam> getParams(List<RequestParam> params, String namePrefix){
        List<RequestParam> matched = new ArrayList<>();
        for(RequestParam param : params) {
            if(param.getName().startsWith(namePrefix)) matched.add(param);
        }
        return matched;
    }

    /**
     * 获取指定名字的参数
     * @param params
     * @param name
     * @return
     */
    public static Object getParamValue(List<RequestParam> params, String name){
        for(RequestParam param : params) {
            if(name.equals(param.getName())) return param.getValue();
        }
        return null;
    }

    /**
     * 移除指定参数
     * @param params
     * @param name
     * @return
     */
    public static List<RequestParam> removeParam(List<RequestParam> params, String name){
        for(int i=0; i<params.size(); i++) {
            RequestParam param = params.get(i);
            if(name.equals(param.getName())){
                params.remove(i);
                i--;
            }
        }
        return params;
    }
}
