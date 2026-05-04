package j.core.web.handler;

import j.core.web.JValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class Action{
	private String id;//操作id
	private String name;//操作名称
	private String method;//处理类中对应的方法名
	private boolean getRequestBody=false;//是否获取requestBody
	private String onError;//发生错误时转向哪个<navigate>中配置的页面
	private Map navigates;//返回地址,key condition,value Navigate（根据业务处理类的处理结果来决定返回那个地址）
	private int logEnabled=-1;//-1，默认；0，关闭；1，开启
	private boolean logAllParameters=false;
	private boolean saveRequestBody=false;
	private List logParams;
	private JValidator validator;//http请求数据有效性验证器
	
	/**
	 * constructor
	 *
	 */
	public Action(){
		this.navigates=new HashMap();
		this.logParams=new ArrayList();
	}
	
	//getters
	public String getId(){
		return this.id;
	}	
	
	public String getName(){
		return this.name;
	}
	
	public String getMethod(){
		return this.method;
	}

	public boolean isGetRequestBody(){
		return this.getRequestBody;
	}
	
	public String getOnError(){
		return this.onError;
	}
	
	public Navigate getNavigate(String condition){
		return (Navigate)this.navigates.get(condition);
	}
	
	public int isLogEnabled(){
		return this.logEnabled;
	}
	
	public boolean isLogAllParameters(){
		return this.logAllParameters;
	}
	
	public boolean saveRequestBody() {
		return this.saveRequestBody;
	}
	
	public List getLogParams(){
		return this.logParams;
	}

	public JValidator getValidator(){
		return this.validator;
	}
	//getters end
	
	
	//setters
	public void setId(String id){
		if(id.startsWith("/")) id=id.substring(1);
		if(id.endsWith("/")) id=id.substring(0, id.length()-1);
		this.id=id;
	}	
	
	public void setName(String name){
		this.name=name;
	}
	
	public void setMethod(String method){
		if(method.startsWith("/")) method=method.substring(0);
		if(method.endsWith("/")) method=method.substring(0, id.length()-1);
		this.method=method;
	}

	public void setGetRequestBody(boolean getRequestBody){
		this.getRequestBody=getRequestBody;
	}
	
	public void setOnError(String onError){
		this.onError=onError;
	}
	
	public void addNavigate(Navigate navigate){
		this.navigates.put(navigate.getCondition(),navigate);
	}
	
	public void setLogEnabled(int logEnabled){
		this.logEnabled=logEnabled;
	}
	
	public void setLogAllParameters(boolean logAllParameters){
		this.logAllParameters=logAllParameters;
	}
	
	public void setSaveRequestBody(boolean saveRequestBody) {
		this.saveRequestBody=saveRequestBody;
	}
	
	public void addLogParam(String logParam){
		if(!this.logParams.contains(logParam)) this.logParams.add(logParam);
	}

	public void setValidator(JValidator validator){
		this.validator=validator;
	}
	//setters end
	
	/**
	 * 得到返回地址，如未找到对应信息则返回系统定义的错误页面地址
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public String getNavigateUrl(String condition)throws Exception{
		if(condition==null||condition.equals("")) return null;
		
		Navigate navigate=this.getNavigate(condition);
		return navigate==null?null:navigate.getUrl();
	}	
	
	/**
	 * 得到返回类型，如未找到对应信息则返回Navigate.TYPE_REDIRECT
	 * @param condition
	 * @return String
	 * @throws Exception
	 */
	public String getNavigateType(String condition)throws Exception{
		if(condition==null||condition.equals("")) return null;

		Navigate navigate=this.getNavigate(condition);
		return navigate==null?null:navigate.getType();
	}
}