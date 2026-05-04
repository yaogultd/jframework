package j.core.web.handler;

import j.core.Startup;
import j.core.annotation.action.Handler;
import j.core.common.JProperties;
import j.core.dao.*;
import j.core.db.JactionLog;
import j.core.sso.LoginStatus;
import j.core.sso.SSOClient;
import j.core.sso.User;
import j.core.sys.SysConfig;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.http.JHttp;
import j.log.Logger;
import j.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

@Handler(path = "/framework/api/JActionLogger")
public class ActionLogger extends JHandler implements Runnable {
	private static Logger logger = Logger.create(ActionLogger.class);// 日志输出
	private String sn;
	private ConcurrentMap<String, JactionLog> events = new ConcurrentMap<>();
	private volatile boolean shutdown = false;
	private DAO dao=null;

	/**
	 *
	 */
	private void ensureDAO(){
		try{
			if(this.dao == null || this.dao.isClosed()){
				this.dao = DAOs.create(JProperties.getLogDatabase(), this.getClass(), true);
			}
		}catch (Exception e){
			logger.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 *
	 */
	private void closeDAO(){
		try{
			DAOs.commit(this.dao);
		}catch (Exception e){
			logger.log(e, Logger.LEVEL_ERROR);
		}
	}

	/**
	 *
	 * @return
	 */
	public int getQueueLength() {
		return this.events.size();
	}

	/**
	 *
	 */
	public ActionLogger(){
	}

	/**
	 *
	 * @param sn
	 */
	public ActionLogger(String sn) {
		this.sn = sn;
	}

	@j.core.annotation.action.Action(path = "search")
	public void search(JSession jsession,HttpServletRequest request,HttpServletResponse response) throws Exception {
		String auIp=SysUtil.getHttpParameter(request,"a_u_ip");
		String auId=SysUtil.getHttpParameter(request,"a_u_id");
		String actionHandler=SysUtil.getHttpParameter(request,"action_handler");
		String actionId=SysUtil.getHttpParameter(request,"action_id");
		String t1=SysUtil.getHttpParameter(request,"t1");
		String t2=SysUtil.getHttpParameter(request,"t2");
		String eventStat=SysUtil.getHttpParameter(request,"event_stat");

		int rpp=50;
		int pn=1;

		String RPP=SysUtil.getHttpParameter(request,"rpp");
		if(JUtilMath.isInt(RPP)
				&&Integer.parseInt(RPP)>0
				&&Integer.parseInt(RPP)<=50){
			rpp=Integer.parseInt(RPP);
		}

		String PN=SysUtil.getHttpParameter(request,"pn");
		if(JUtilMath.isInt(PN)
				&&Integer.parseInt(PN)>0){
			pn=Integer.parseInt(PN);
		}

		String sql="del_by_sys<>'D' and a_sys_id='"+SysConfig.getSysId()+"'";
		if(auIp!=null&&!"".equals(auIp)){
			sql+=" and a_u_ip='"+auIp+"'";
		}

		if(auId!=null&&!"".equals(auId)){
			sql+=" and a_u_id='"+auId+"'";
		}

		if(actionHandler!=null&&!"".equals(actionHandler)){
			sql+=" and action_handler='"+actionHandler+"'";
		}

		if(actionId!=null&&!"".equals(actionId)){
			sql+=" and action_id='"+actionId+"'";
		}

		if(JUtilString.isDate(t1)) t1 += "00:00:00";
		if(JUtilTimestamp.isTimestamp(t1)){
			sql+=" and event_time>="+Timestamp.valueOf(t1).getTime();
		}

		if(JUtilString.isDate(t2)){
			t2 += "00:00:00";
			sql+=" and event_time<"+(Timestamp.valueOf(t2).getTime() + JUtilTimestamp.millisOfDay);
		}else if(JUtilTimestamp.isTimestamp(t2)){
			sql+=" and event_time<"+Timestamp.valueOf(t2).getTime();
		}

		if(eventStat!=null&&!"".equals(eventStat)){
			sql+=" and event_stat='"+eventStat+"'";
		}

		if(sql.startsWith(" and ")) sql=sql.substring(5);

		DAO _dao=null;
		try{
			_dao=DB.connectForTables(JProperties.getLogDatabase(),
					new String[]{"j_action_log"},
					ActionLogger.class);

			List list=_dao.find("j_action_log",sql+" order by event_time desc",rpp,pn);
			int total=_dao.getRecordCnt("j_action_log",sql);

			_dao.close();
			_dao=null;

			request.setAttribute("list",list);
			request.setAttribute("total",Integer.valueOf(total));
		}catch(Exception e){
			logger.log(e,Logger.LEVEL_ERROR);
			if(_dao!=null){
				try{
					_dao.close();
					_dao=null;
				}catch(Exception ex){}
			}
		}
	}

	@j.core.annotation.action.Action(path = "ip")
	public void ip(JSession jsession,HttpServletRequest request,HttpServletResponse response) throws Exception {
		String sql="select distinct a_u_id,a_u_ip from j_action_log where action_id='ssologin' and action_handler='/ssoserver.handler' and a_u_ip<>'127.0.0.1'";

		ResultSet rs=null;
		StmtAndRs sr=null;
		try {
			Map result=new LinkedHashMap();
			sr=QueryPool.getPool(JProperties.getLogDatabase()).query(null,sql);
			rs=sr.resultSet();
			while(rs.next()){
				String userId=rs.getString(1);
				String userIp=rs.getString(2);
				List uids=(List)result.get(userIp);
				if(uids==null){
					uids=new LinkedList();
					result.put(userIp,uids);
				}
				if(!uids.contains(userId)) uids.add(userId);
			}
			sr.close();
			rs=null;
			sr=null;

			request.setAttribute("ip",result);
		} catch (Exception ex) {
			logger.log(ex, Logger.LEVEL_ERROR);
		}
	}

	@j.core.annotation.action.Action(path = "del")
	public void del(JSession jsession,HttpServletRequest request,HttpServletResponse response) throws Exception {
		String eventId=SysUtil.getHttpParameter(request,"event_id");
		String auIp=SysUtil.getHttpParameter(request,"a_u_ip");
		String auId=SysUtil.getHttpParameter(request,"a_u_id");
		String actionHandler=SysUtil.getHttpParameter(request,"action_handler");
		String actionId=SysUtil.getHttpParameter(request,"action_id");
		String t1=SysUtil.getHttpParameter(request,"t1");
		String t2=SysUtil.getHttpParameter(request,"t2");
		String destroy=SysUtil.getHttpParameter(request,"destroy");

		String sql="";
		if(eventId!=null&&!"".equals(eventId)){
			sql+=" and event_id='"+eventId+"'";
		}

		if(auIp!=null&&!"".equals(auIp)){
			sql+=" and a_u_ip='"+auIp+"'";
		}

		if(auId!=null&&!"".equals(auId)){
			sql+=" and a_u_id='"+auId+"'";
		}

		if(actionHandler!=null&&!"".equals(actionHandler)){
			sql+=" and action_handler='"+actionHandler+"'";
		}

		if(actionId!=null&&!"".equals(actionId)){
			sql+=" and action_id='"+actionId+"'";
		}

		if(JUtilTimestamp.isTimestamp(t1)){
			sql+=" and event_time>='"+t1+"'";
		}

		if(JUtilTimestamp.isTimestamp(t2)){
			sql+=" and event_time<='"+t2+"'";
		}

		if(sql.startsWith(" and ")) sql=sql.substring(5);

		DAO dao=null;
		try{
			dao=DB.connectForTables(JProperties.getLogDatabase(),
					new String[]{"j_action_log"},
					ActionLogger.class);

			dao.beginTransaction();

			if("T".equalsIgnoreCase(destroy)){
				if("".equals(sql)){
					dao.executeSQL("delete from j_action_log");
				}else{
					dao.executeSQL("delete from j_action_log where "+sql);
				}
			}else{
				if("".equals(sql)){
					dao.executeSQL("update j_action_log set del_by_sys='D'");
				}else{
					dao.executeSQL("update j_action_log set del_by_sys='D' where "+sql);
				}
			}

			dao.commit();
			dao.close();
			dao=null;

			jsession.jresponse=new JResponse(true,"1","删除成功");
		}catch(Exception e){
			logger.log(e,Logger.LEVEL_ERROR);
			if(dao!=null){
				try{
					dao.close();
					dao=null;
				}catch(Exception ex){}
			}jsession.jresponse=new JResponse(false,"ERR","系统错误");
		}
	}

	/**
	 *
	 * @return
	 */
	public String getSn() {
		return this.sn;
	}

	/**
	 *
	 *
	 */
	public void shutdown() {
		this.shutdown = true;
	}

	/**
	 *
	 * @param jsession
	 * @param request
	 * @param action
	 * @param uuid
	 */
	public void before(JSession jsession, HttpServletRequest request, Action action, String uuid) {
		if (action == null || action.isLogEnabled()==0) return;//对象为空或日志关闭
		if(action.isLogEnabled()==-1&&!Handlers.isLoggerOn()) return;//日志未设置且默认未开启

		User user = SSOClient.getCurrentUser(request);
		String userId=(user == null ? null : user.getUserId());
		if(userId==null){
			LoginStatus status = SSOClient.findLoginStatus(request);
			userId=(status == null ? null : status.getUserId());
		}
		if(userId==null){
			userId=SysUtil.getHttpParameter(request,Constants.SSO_USER_ID);
		}

		JactionLog log = new JactionLog();
		log.setEventId(uuid);
		log.setAsvrId(SysConfig.getMachineID());
		log.setAsysId(SysConfig.getSysId());
		log.setAdomain(JUtilString.getHost(request.getRequestURL().toString()));
		log.setAurl(request.getRequestURI());
		log.setAuIp(JHttp.getRemoteIp(request));
		log.setAuId(userId);
		log.setActionHandler(log.getAurl());
		log.setActionId(action.getId());

		//保存参数
		StringBuffer ps=new StringBuffer();
		ps.append("{\"parameters\":{");

		int pIndex=0;

		if(action.isLogAllParameters()) {
			Enumeration parameters = request.getParameterNames();
			while (parameters.hasMoreElements()) {
				String parameter = (String) parameters.nextElement();
				String value=SysUtil.getHttpParameter(request, parameter);
				if(value==null) value="_IS_NULL_";

				if(pIndex>0) ps.append(",");
				ps.append("\""+parameter+"\":\""+JUtilJSON.convert(value)+"\"");
				pIndex++;
			}
		} else {
			List temp = action.getLogParams();
			for (int i = 0; i < temp.size(); i++) {
				String p = (String) temp.get(i);
				String value=SysUtil.getHttpParameter(request, p);
				if(value==null) value="_IS_NULL_";

				if(pIndex>0) ps.append(",");
				ps.append("\""+p+"\":\""+JUtilJSON.convert(value)+"\"");
				pIndex++;
			}
		}
		ps.append("}");

		if(action.saveRequestBody()) {
			try{
				String requestBody=jsession.getRequestBody();
				if(requestBody!=null) {
					requestBody=requestBody.trim();

					if(requestBody.startsWith("{")) {
						ps.append(",\"requestBody\":"+requestBody);
					}else {
						ps.append(",\"requestBody\":{"+requestBody+"}");
					}
					requestBody=null;
				}
			}catch(Exception e) {
				//e.printStackTrace();
			}
		}

		ps.append("}");

		String s = ps.toString();
		if(s.length() > 102400) s = s.substring(0, 102400)+"...";

		log.setActionParameters(s);
		ps=null;
		//保存参数 end

		log.setActionResult(null);
		log.setEventTime(SysUtil.getNow());
		log.setDelBySys("N");

		events.put(uuid, log);
	}

	/**
	 *
	 * @param action
	 * @param uuid
	 */
	public void after(HttpServletRequest request, Action action, String uuid) {
		if (action == null || action.isLogEnabled()==0) return;//对象为空或日志关闭
		if(action.isLogEnabled()==-1&&!Handlers.isLoggerOn()) return;//日志未设置且默认未开启

		JactionLog log = events.get(uuid);
		if (log == null) return;

		if(log.getAuId()==null){
			User user = SSOClient.getCurrentUser(request);
			String userId=(user == null ? null : user.getUserId());
			if(userId==null){
				LoginStatus status = SSOClient.findLoginStatus(request);
				userId=(status == null ? null : status.getUserId());
			}
			log.setAuId(userId);
		}

		log.setEventStat("TRACE");
		log.setActionResult("");
	}

	/**
	 *
	 * @param request
	 * @param action
	 * @param uuid
	 * @param resultString
	 */
	public void after(HttpServletRequest request, Action action,String uuid, String resultString) {
		if (action == null || action.isLogEnabled()==0) return;//对象为空或日志关闭
		if(action.isLogEnabled()==-1&&!Handlers.isLoggerOn()) return;//日志未设置且默认未开启

		JactionLog log = events.get(uuid);
		if (log == null) return;

		if(log.getAuId()==null){
			User user = SSOClient.getCurrentUser(request);
			String userId=(user == null ? null : user.getUserId());
			if(userId==null){
				LoginStatus status = SSOClient.findLoginStatus(request);
				userId=(status == null ? null : status.getUserId());
			}
			log.setAuId(userId);
		}

		if(resultString==null) resultString="";
		else if(resultString.length() > 1024) resultString = resultString.substring(0, 1024)+"...";

		log.setEventStat("TRACE");
		log.setActionResult(resultString);
	}

	/**
	 *
	 * @param request
	 * @param action
	 * @param uuid
	 * @param navigateType
	 * @param navigateUrl
	 */
	public void after(HttpServletRequest request, Action action,String uuid, String navigateType,String navigateUrl) {
		if (action == null || action.isLogEnabled()==0) return;//对象为空或日志关闭
		if(action.isLogEnabled()==-1&&!Handlers.isLoggerOn()) return;//日志未设置且默认未开启

		JactionLog log = events.get(uuid);
		if (log == null) return;

		if(log.getAuId()==null){
			User user = SSOClient.getCurrentUser(request);
			String userId=(user == null ? null : user.getUserId());
			if(userId==null){
				LoginStatus status = SSOClient.findLoginStatus(request);
				userId=(status == null ? null : status.getUserId());
			}
			log.setAuId(userId);
		}

		log.setEventStat("TRACE");
		log.setActionResult(navigateType + " to " + navigateUrl);
	}

	/**
	 *
	 * @param request
	 * @param action
	 * @param uuid
	 * @param e
	 */
	public void after(HttpServletRequest request, Action action,String uuid, Exception e) {
		if (action == null || action.isLogEnabled()==0) return;//对象为空或日志关闭
		if(action.isLogEnabled()==-1&&!Handlers.isLoggerOn()) return;//日志未设置且默认未开启

		JactionLog log = events.get(uuid);
		if (log == null) return;

		if (e == null) {
			log.setActionResult("");
			return;
		}

		String ex = SysUtil.getException(e);

		if(log.getAuId()==null){
			User user = SSOClient.getCurrentUser(request);
			String userId=(user == null ? null : user.getUserId());
			if(userId==null){
				LoginStatus status = SSOClient.findLoginStatus(request);
				userId=(status == null ? null : status.getUserId());
			}
			log.setAuId(userId);
		}

		log.setEventStat("ERROR");
		log.setActionResult(ex == null ? "" : ex);
	}

	@Override
	public void run() {
		while(!shutdown && !Startup.isDestroyed()) {
			try {
				try {
					Thread.sleep(100);
				} catch (Exception e) {}

				if(this.events.isEmpty()) continue;

				long now = SysUtil.getNow();
				List keys = this.events.listKeys();
				for (int i = 0; i < keys.size(); i++) {
					String uuid = (String) keys.get(i);
					JactionLog log = this.events.get(uuid);
					if(log==null) continue;

					if (log.getActionResult() != null
							|| now - log.getEventTime() > Handlers.getActionTimeout()) {
						this.events.remove(uuid);

						if (log.getActionResult() == null) {
							log.setActionResult("ACTION_EXECUTION_TIMEOUT");
							log.setEventStat("ERROR");
						}

						if(log.getActionResult() != null && log.getActionResult().length() > 1024){
							log.setActionResult(log.getActionResult().substring(0, 1024));
						}

						this.ensureDAO();
						this.dao.insert(log);
					}
				}
				keys.clear();
				keys = null;
			} catch (Exception e) {
				logger.log(e, Logger.LEVEL_ERROR);
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {}
			}
		}
		this.closeDAO();
	}
}