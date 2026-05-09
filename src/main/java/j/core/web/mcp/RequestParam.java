package j.core.web.mcp;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestParam {
    public final static String TASK_ID="taskId";
    public final static String STREAM="stream";
    public final static String CONVERSATION_ID="conversationId";
    public final static String PROVIDER_ID="providerId";
    public final static String MODEL_ID="modelId";
    public final static String PLUGIN_ID="pluginId";
    public final static String STEP_TAG="stepTag";
    public final static String PROMPT="prompt";
    public final static String PROMPT_END_TEXT="promptEndText";
    public final static String OUTPUT_FORMAT_DESCRIPTION="outFormatDesc";
    public final static String FILE_PREFIX="file_";

    //翻译成什么语言
    public final static String TRANSLATE_TO="translateTo";

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
