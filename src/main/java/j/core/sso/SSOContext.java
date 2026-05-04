package j.core.sso;

import j.core.Startup;
import j.log.Logger;
import j.util.ConcurrentMap;

import java.util.List;

/**
 * 
 * @author 肖炯
 *
 */
public class SSOContext implements Runnable{
	private static Logger log=Logger.create(SSOContext.class);
	private static ConcurrentMap<String, Object[]> tokens=new ConcurrentMap<>();

	static{
		SSOContext instance=new SSOContext();
		Thread thread=new Thread(instance);
		thread.start();
		log.log("SSOContext started",-1);
	}
	
	/**
	 * 
	 * @param globalSessionId
	 * @param token
	 * @param ssoUserId
	 */
	public static void addToken(String globalSessionId,String token,String ssoUserId){
		tokens.put(globalSessionId,new Object[]{token,Long.valueOf(System.currentTimeMillis()),ssoUserId});
	}
	
	/**
	 * 
	 * @param globalSessionId
	 * @return
	 */
	public static Object[] getToken(String globalSessionId){
		if(globalSessionId==null) return null;
		Object[] objs=(Object[])tokens.get(globalSessionId);
		return objs;
	}
	
	/**
	 * 
	 * @param globalSessionId
	 */
	public static void removeToken(String globalSessionId){
		if(globalSessionId==null) return;
		tokens.remove(globalSessionId);
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(!Startup.isDestroyed()){
			try{
				Thread.sleep(15000);
			}catch(Exception ex){}
			
			//清除过期token（15秒）
			List keys=tokens.listKeys();
			for(int i=0;i<keys.size();i++){
				String key=(String)keys.get(i);
				Object[] objs=(Object[])tokens.get(key);
				Long time=(Long)objs[1];
				if(System.currentTimeMillis()-time>15000){
					tokens.remove(key);
				}
			}
		}
	}
}
