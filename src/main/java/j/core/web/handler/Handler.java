package j.core.web.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 肖炯
 *
 */
public class Handler{
	private String path;
	private String pathPattern;
	private String RESTStylePath;
	private String clazz;
	private String clazzShadow;//用于同时支持两个handler类共享一个path
	private String requestBy;
	private boolean singleton;
	private Map<String, Action> actions;
	
	/**
	 * constructor
	 *
	 */
	public Handler(){
		this.actions=new HashMap();
	}
	
	//getters
	public String getPath(){
		return this.path;
	}
	
	public String getRESTStylePath(){
		return this.RESTStylePath;
	}
	
	public String getPathPattern(){
		return this.pathPattern==null?Handlers.getActionPathPattern():this.pathPattern;
	}
	
	public String getClazz(){
		return this.clazz;
	}

	public String getClazzShadow(){
		return this.clazzShadow;
	}
	
	public String getRequestBy(){
		return this.requestBy;
	}
	
	public boolean getSingleton(){
		return this.singleton;
	}
	
	public Action getAction(String id){
		return (Action)actions.get(id);
	}
	
	public List<Action> getActions(){
		List<Action> temp=new ArrayList();
		temp.addAll(actions.values());
		return temp;
	}
	//getters end
	
	//setters
	public void setPath(String path){
		if(!path.startsWith("/")) path="/"+path;
		if(path.endsWith("/")) path=path.substring(0, path.length()-1);
		this.path=path;
	}
	
	public void setRESTStylePath(String RESTStylePath){
		if(!RESTStylePath.startsWith("/")) RESTStylePath="/"+RESTStylePath;
		if(RESTStylePath.endsWith("/")) RESTStylePath=RESTStylePath.substring(0, RESTStylePath.length()-1);
		this.RESTStylePath=RESTStylePath;
	}
	
	public void setPathPattern(String pathPattern){
		this.pathPattern=pathPattern;
	}
	
	public void setClazz(String clazz){
		this.clazz=clazz;
	}

	public void setClazzShadow(String clazzShadow){
		this.clazzShadow=clazzShadow;
	}
	
	public void setRequestBy(String requestBy){
		this.requestBy=requestBy;
	}
	
	public void setSingleton(boolean singleton){
		this.singleton=singleton;
	}
	
	public void addAction(Action action){
		this.actions.put(action.getId(), action);
	}
	//setters end

	//拷贝配置到指定对象
	public void clone(Handler to){
		List<Action> list=this.getActions();
		for(int i=0; i<list.size(); i++){
			to.addAction(list.get(i));
		}
	}
}
