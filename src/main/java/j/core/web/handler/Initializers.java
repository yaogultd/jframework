package j.core.web.handler;

import j.core.annotation.description.ClassDescription;
import j.core.common.JProperties;
import j.core.fs.JDFSFile;
import j.core.nvwa.Nvwa;
import j.util.JUtilString;
import j.util.JUtilTimestamp;

import jakarta.servlet.http.HttpServlet;
import java.io.File;


@ClassDescription(author = "肖炯", date = "2021/07/19", description = "WEB应用启动入口（load-on-startup servlet）", reviewers = {})
public class Initializers extends HttpServlet{	
	private static final long serialVersionUID = 1L;

	@Override
	public void init(){
		System.out.println(JUtilTimestamp.timestamp() +" 系统(WEB)初始化....");
		try{
			String userDir=System.getProperty("user.dir");
			userDir=JUtilString.replaceAll(userDir, "\\", "/");

			File argsFile=new File(JUtilString.appendPath(userDir, "Startup.Args"));
			if(argsFile.exists()){
				String[] args=JUtilString.getTokens(JDFSFile.read(argsFile, "UTF-8"), "^");
				argsFile.delete();

				Nvwa.startup(args);
			}else{
				Nvwa.startup();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
