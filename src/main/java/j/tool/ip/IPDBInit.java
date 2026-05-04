package j.tool.ip;


import j.core.dao.DAO;
import j.core.dao.DB;
import j.core.nvwa.Nvwa;
import j.util.JUtilString;

import java.io.*;

public class IPDBInit {
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		Nvwa.startup();

		try{
			Thread.sleep(30000);
		}catch (Exception e){}

		int cnt=0;
		DAO dao= DB.connect("IP",IPDBInit.class);
		DB.sqliteSetSynchronous(dao,DB.sqliteSynchronousOff);
		dao.executeSQL("create table IF NOT EXISTS j_ip(IP_ID int,IP_START long,IP_END long,IP_ADDR varchar)");
		File file=new File("D:\\work\\JFramework\\doc\\通用\\IP库\\ips.sql");
		InputStream in=new FileInputStream(file);
		BufferedReader reader=new BufferedReader(new InputStreamReader(in,"UTF-8"));
		String line=null;
		try{
	    	line=reader.readLine();
	    	while(line!=null){
	    		dao.executeSQL(JUtilString.replaceAll(line,"\\'","''"));
	    		line=reader.readLine();
	    		if(cnt%1000==0){;
	    			System.out.println(cnt);
	    		}
	    		cnt++;
	    	}

	    	dao.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(line);
		}
		System.out.println("end");
    	System.exit(0);
	}
}
