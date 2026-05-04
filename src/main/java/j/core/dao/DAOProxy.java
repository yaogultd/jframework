package j.core.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import j.core.dao.intervener.RowIntervener;
import j.util.JUtilString;

/**
 * @author 肖炯
 *
 */
public class DAOProxy implements InvocationHandler{
	private final static String[] statusMethods = new String[]{"getTimeout","isClosed","isInTransaction","isUsing","getLastUsingTime"};
	private DAO dao;
	
	/**
	 * 
	 * @param _dao
	 * @return
	 */
	public Object bind(DAO _dao){
		dao=_dao;
		return Proxy.newProxyInstance(RdbmsDao.class.getClassLoader(), RdbmsDao.class.getInterfaces(),this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName=method.getName();
		
		DBMirror m=dao.getMirror();
		if(m!=null){
			if(!m.isReadable()){
				if(methodName.startsWith("find")
						||methodName.indexOf("getMax")>-1
						||methodName.indexOf("getMin")>-1
						||methodName.indexOf("getSum")>-1
						||methodName.equals("getRecordCnt")){
					throw new Exception("this mirror can't be read.");
				}
			}
			
			if(dao.getReadOnly()||!m.isInsertable()){
				if(methodName.startsWith("insert")){
					throw new Exception("this mirror can't be inserted.");
				}
			}
			
			if(dao.getReadOnly()||!m.isUpdatable()){
				if(methodName.startsWith("update")||methodName.startsWith("execute")){
					throw new Exception("this mirror can't be updated.");
				}
			}
		}

		boolean isStatusMethod = JUtilString.contain(statusMethods,methodName);
		if(!isStatusMethod){
			dao.begin();
			dao.beforeAnyInvocation();
		}
		Object returnValue=null;
		try{
			returnValue=method.invoke(dao,args);
			if(!isStatusMethod){
				dao.afterAnyInvocation();
				dao.finish();
			}

			RowIntervener intervener=dao.getRowIntervener();
			if(intervener!=null
					&&returnValue!=null
					&&("find".equals(methodName) || "findScale".equals(methodName) ||"findSingle".equals(methodName))
					&&!(returnValue instanceof StmtAndRs)) {
				if(returnValue instanceof List) {
					List rows=(List)returnValue;
					for(int i=0; i<rows.size(); i++) {
						rows.set(i, intervener.update(rows.get(i), dao.getRuntimeDatas()));
					}
					returnValue=rows;
				}else {
					returnValue=intervener.update(returnValue, dao.getRuntimeDatas());
				}
			}
		}catch(Exception e){
			dao.onException();
			if(!isStatusMethod){
				dao.afterAnyInvocation();
				dao.finish();
			}
			throw e;
		}
		return returnValue;
	}
}
