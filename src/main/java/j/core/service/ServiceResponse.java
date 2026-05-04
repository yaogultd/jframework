package j.core.service;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.FieldDescription;
import j.core.annotation.description.MethodDescription;
import j.util.JUtilBean;
import j.util.JUtilJSON;
import j.util.JUtilMath;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ClassDescription(author = "肖炯",
        date = "2021/07/14",
        description = "每一个服务都需要返回ServiceResponse对象，其中包含业务所需数据", reviewers = {})
@Getter
@Setter
public class ServiceResponse<T> implements Serializable{
    @FieldDescription(description = "调用是否成功（与业务处理结果无关）")
    private Boolean success=true;

    @FieldDescription(description = "响应状态码")
    private int statusCode=200;

    @FieldDescription(description = "业务处理结果代码")
    private String code;

    @FieldDescription(description = "业务处理结果文本提示")
    private String message;

    @FieldDescription(description = "返回的业务数据")
    private T response;

    @MethodDescription(author = "肖炯", date = "2021/07/14", description = "创建一个服务响应")
    public ServiceResponse(boolean success, String code, String message, T response){
        this.success=success;
        this.code=code;
        this.message=message;
        this.response=response;
    }

    @MethodDescription(author = "肖炯", date = "2021/07/14", description = "创建一个服务响应")
    public ServiceResponse(boolean success, String code, String message){
        this.success=success;
        this.code=code;
        this.message=message;
    }

    @Override
    public String toString(){
        StringBuffer s=new StringBuffer();
        s.append("{\"success\":"+this.success);
        s.append(",\"code\":\""+ JUtilJSON.convertChars(this.code) +"\"");
        s.append(",\"message\":\""+ JUtilJSON.convertChars(this.message) +"\"");

        s.append("\"response\":");
        if(this.response != null){
            Object o = this.response;
            if((o instanceof String)){
                if(JUtilJSON.isJson(o.toString()) != null) {//本身是json串
                    s.append(o);
                }else {
                    s.append("\"").append(JUtilJSON.convertChars(o.toString())).append("\"");
                }
            }else if((o instanceof Integer)
                    ||(o instanceof Long)
                    ||(o instanceof Short)
                    ||(o instanceof Boolean)){
                s.append(o);
            }else if((o instanceof Double)
                    ||(o instanceof Float)){
                s.append(JUtilMath.formatPrintPrecisionNoChange((Double) o, (Double) o, 0));
            }else if(o instanceof BigDecimal){
                s.append(o);
            }else if((o instanceof Timestamp)){
                s.append("\"").append(o.toString().substring(0, 19)).append("\"");
            }else if((o instanceof List)){
                s.append(JUtilBean.beans2Json((List)o));
            }else if((o instanceof Map)){
                s.append(JUtilBean.map2Json((Map)o));
            }else if((o instanceof JSONObject)){
                s.append(o);
            }else if((o instanceof Object[])){//数组
                Object[] array = (Object[])o;
                List list = new ArrayList();
                for(int j=0; j<array.length; j++) list.add(array[j]);
                s.append(JUtilBean.beans2Json(list));
            }else {
                if(o.getClass().isPrimitive()){
                    s.append(o);
                }else if(JUtilJSON.isJson(o.toString()) != null) {//本身是json串
                    s.append(o);
                }else {
                    s.append(JUtilBean.bean2Json(o));
                }
            }
        }else{
            s.append("null");
        }
        s.append("}");
        return s.toString();
    }
}