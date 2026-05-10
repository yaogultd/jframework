package j.core.web.handler;

import j.util.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JResponse{
	@Setter
	@Getter
	private boolean success;

	@Setter
	@Getter
	private String code;

	@Setter
	@Getter
	private String message;

	private List<JUtilKeyValue> datas=new LinkedList<JUtilKeyValue>();
	
	@Setter
	private JUtilDataConverter outputConverter=null;//输出转换器
	
	@Setter
	private Object outputConverterParams=null;//输出转换参数

	@Setter
	private List<String> excludedKeysWhenToJson=null;

	@Setter
	@Getter
	private boolean outputIgnoreNulls=true;

	@Setter
	private JUtilBeanImpl beanUtil=JUtilBean.getInstance();

	public JResponse(){

	}
	
	public JResponse(boolean success,String code,String message){
		this.success=success;
		this.code=JUtilString.isBlank(code) ? "" : code;
		this.message=message;
	}
	
	/**
	 * 
	 * @param success
	 * @param code
	 * @param message
	 */
	public JResponse(boolean success, String code, String message, HttpServletRequest request){
		this.success=success;
		this.code=code;
		this.message=message;
		
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putData(Object key,Object value){
		datas.add(new JUtilKeyValue(key,value));
	}

	/**
	 *
	 * @param key
	 * @param value
	 */
	public void putData(Object key, Object value, List<String> excludedKeysWhenToJson){
		JUtilKeyValue kv = new JUtilKeyValue(key,value);
		kv.getParams().put("excludedKeysWhenToJson", excludedKeysWhenToJson);
		datas.add(kv);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public JUtilKeyValue getData(String key){
		for(int i=0;i<datas.size();i++) {
			JUtilKeyValue data = datas.get(i);
			if (data == null) continue;
			if(data.getKey().equals(key)) return data;
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public List getDatas(){
		return this.datas;
	}

	@Override
	public String toString(){
		return toString(null);
	}

	public String toString(HttpServletRequest request){
		if(code==null) code="";
		if(message==null) message="";
		
		StringBuffer s=new StringBuffer();
		s.append("{\"success\":"+success);
		s.append(",\"code\":\""+JUtilJSON.convertChars(code)+"\"");

		JSONObject _message=JUtilJSON.parse(message);
		if(_message!=null && _message.length() > 0){
			s.append(",\"message\":"+message);
		}else{
			s.append(",\"message\":\""+JUtilJSON.convertChars(message)+"\"");
		}

		int index=0;
		s.append(",\"datas\":{");
		for(int i=0;i<datas.size();i++){
			JUtilKeyValue data=datas.get(i);
			if(data==null) continue;

			if(index>0) s.append(",");
			index++;

			List<String> _excludedKeysWhenToJson=(List<String>)data.getParams().get("excludedKeysWhenToJson");
			if(_excludedKeysWhenToJson==null) _excludedKeysWhenToJson=excludedKeysWhenToJson;

			Object o = data.getValue();
			
			s.append("\""+data.getKey()+"\":");
			if(o==null){
				s.append("null");
			}else if((o instanceof String) || (o instanceof StringBuffer) || (o instanceof StringBuilder)){
				if(JUtilJSON.isJson(o.toString()) != null) {//本身是json串
					s.append(o);
				}else {
					s.append("\""+JUtilJSON.convertChars(o.toString())+"\"");
				}
			}else if((o instanceof Integer)
					||(o instanceof Long)
					||(o instanceof Short)
					||(o instanceof Boolean)){
				s.append(""+o);
			}else if((o instanceof Double)
					||(o instanceof Float)){
				s.append(JUtilMath.formatPrintPrecisionNoChange((Double)o, (Double)o, 0));
			}else if(o instanceof BigDecimal){
				s.append(o);
			}else if(o instanceof Timestamp){
				s.append("\"").append(o.toString().substring(0, 19)).append("\"");
			}else if((o instanceof List)){
				s.append(beanUtil.beans2Json((List)o, _excludedKeysWhenToJson, outputConverter, outputConverterParams, outputIgnoreNulls));
			}else if((o instanceof Map)){
				s.append(beanUtil.map2Json((Map)o));
			}else if(o instanceof JSONObject){
				s.append(o);
			}else{
				s.append(beanUtil.bean2Json(o, false, null, _excludedKeysWhenToJson, outputConverter, outputConverterParams, outputIgnoreNulls));
			}
		}
		s.append("}}");
		return s.toString();
	}
}