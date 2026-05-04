package j.core.sso;

import j.core.Startup;
import j.core.web.Constants;
import j.core.permission.Signature;
import j.http.JHttp;
import j.http.JHttpContext;
import j.log.Logger;
import j.core.sys.SysUtil;
import j.util.ConcurrentList;
import j.util.JUtilString;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * sso client 通知线程
 * @author 肖炯
 *
 */
public class SSONotifier implements Runnable{
	private static Logger log=Logger.create(SSONotifier.class);
	private static Map notifiers=new HashMap();

	public static final String type_login="login";
	public static final String type_logout="logout";
	
	private ConcurrentList tasks=new ConcurrentList();
	private JHttp http;
	private HttpClient httpClient;
	
	static{
		ConcurrentList ssoClients=SSOConfig.getSsoClients();
		for(int i=0;i<ssoClients.size();i++){
			Client client=(Client)ssoClients.get(i);
			
			SSONotifier notifierSet[]=new SSONotifier[SSOConfig.getNotifiersPerClient()];
			for(int j=0;j<SSOConfig.getNotifiersPerClient();j++){
				notifierSet[j]=new SSONotifier();
				
				Thread thread=new Thread(notifierSet[j]);
				thread.start();
				log.log("sso client "+client.getId()+" 通知线程 "+j+" 已经启动",-1);
			}
			notifiers.put(client.getId(),notifierSet);
		}
	}
	
	/**
	 * constructor
	 *
	 */
	private SSONotifier(){
		http=JHttp.getInstance();
		httpClient=http.createClient(6000);//6秒超时
	}
	
	/**
	 * 
	 * @param client
	 * @return
	 */
	public static SSONotifier getNotifier(Client client){
		SSONotifier[] ns=(SSONotifier[])notifiers.get(client.getId());
		
		Random r=new Random();
		int i=r.nextInt(SSOConfig.getNotifiersPerClient());
		r=null;
		return ns[i];
	}

	/**
	 * 通知登录
	 * @param client
	 * @param session
	 */
	public void login(Client client, SSOSession session){
		try {
			long now = SysUtil.getNow();

			Map<String, String> params = new HashMap<>();
			params.put(Constants.SSO_TIME, "" + now);
			params.put(Constants.ACCESS_TOKEN, session.getAccessToken());
			params.put(Constants.REFRESH_TOKEN, session.getRefreshToken());
			params.put(Constants.SSO_USER_ID, session.getUserId());
			params.put(Constants.SSO_SUB_USER_ID, session.getSubUserId() == null ? "" : session.getSubUserId());
			params.put(Constants.SSO_USER_IP, session.getUserIp() == null ? "" : session.getUserIp());

			if(client.isLocal()){
				//log.log("get login notify from local server -> "+session.getAccessToken(), -1);
				SSOClient.doLogin(client, params);
				return;
			}

			if (StringUtils.isBlank(client.getUrlDefault())
					|| StringUtils.isBlank(client.getLoginInterface())) {
				return;
			}

			String url = JUtilString.appendUrl(client.getUrlDefault(), client.getLoginInterface());

			int loop = 0;
			while (loop < 3) {//最多尝试3次
				loop++;
				try {
					JHttpContext context = Signature.request(http, httpClient, null, url, params, null, client.getAccessKey(), client.getAccessSecret());

					//log.log("tell client "+client.getId()+"  to login -> "+context.getResponseText(), -1);

					context.finalize();
					context = null;
					break;
				} catch (Exception e) {
				}
			}
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 通知注销
	 * @param client
	 * @param session
	 */
	public void logout(Client client, SSOSession session){
		try {
			long now = SysUtil.getNow();

			Map<String, String> params = new HashMap<>();
			params.put(Constants.SSO_TIME, "" + now);
			params.put(Constants.ACCESS_TOKEN, session.getAccessToken());
			params.put(Constants.REFRESH_TOKEN, session.getRefreshToken());
			params.put(Constants.SSO_USER_ID, session.getUserId());
			params.put(Constants.SSO_SUB_USER_ID, session.getSubUserId() == null ? "" : session.getSubUserId());
			params.put(Constants.SSO_USER_IP, session.getUserIp() == null ? "" : session.getUserIp());

			if(client.isLocal()){
				SSOClient.doLogout(client, params);
				return;
			}

			if (StringUtils.isBlank(client.getUrlDefault())
					|| StringUtils.isBlank(client.getLogoutInterface())) {
				return;
			}

			String url = JUtilString.appendUrl(client.getUrlDefault(), client.getLogoutInterface());

			int loop = 0;
			while (loop < 3) {//最多尝试3次
				loop++;
				try {
					JHttpContext context = Signature.request(http, httpClient, null, url, params, null, client.getAccessKey(), client.getAccessSecret());

					//log.log("tell client "+client.getId()+"  to logout -> "+context.getResponseText(), -1);

					context.finalize();
					context = null;
					break;
				} catch (Exception e) {
				}
			}
		}catch(Exception e){
			log.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 * 添加注销或登录任务
	 * @param client
	 * @param session
	 */
	public static void addTask(Client client, SSOSession session, String type){
		if(SSONotifier.type_login.equals(type) && JUtilString.isBlank(client.getLoginInterface())) return;
		if(SSONotifier.type_logout.equals(type) && JUtilString.isBlank(client.getLogoutInterface())) return;

		SSONotifier notifier=getNotifier(client);
		notifier.tasks.add(new Object[]{client, session, type});
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		while(!Startup.isDestroyed()){
			try{
				Thread.sleep(500);
			}catch(Exception ex){}

			if(Startup.isDestroyed()){
				return;
			}
			
			for(int i=0;i<tasks.size();i++){
				Object[] cells=(Object[])tasks.remove(i);
				Client client=(Client)cells[0];
				SSOSession session=(SSOSession)cells[1];
				String type=(String)cells[2];
				
				if(type.equals(type_login)){
					this.login(client, session);
				}else if(type.equals(type_logout)){
					this.logout(client, session);
				}
				i--;
			}
		}
	}	
}
