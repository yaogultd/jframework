package j.core.sys;

import j.core.annotation.description.MethodDescription;
import j.core.nvwa.resource.Consumer;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceXml;
import j.util.JUtilMath;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.LinkedList;
import java.util.List;


/**
 * @author 肖炯 
 *
 */
public class Initializers implements Consumer {
	private static final long serialVersionUID = 1L;
	private static List initialCommands=new LinkedList();
	private static List initializersClasses=new LinkedList();

	@MethodDescription(author = "肖炯", date = "2021/07/21", description = "加载配置")
	private static boolean load(Resource resource){
		try{
			Document doc=((ResourceXml)resource).getResource();
			Element root=doc.getRootElement();

			//新版配置（nvwa.xml中的SYS节点）
			if(root.element("SYS")!=null){
				root=root.element("SYS");
			}

			//未启用
			if("false".equalsIgnoreCase(root.attributeValue("enabled"))){
				return true;
			}

			List commands=root.selectNodes("//Initializers/command");
			for(int i=0;commands!=null&&i<commands.size();i++){
				Element ele=(Element)commands.get(i);
				initialCommands.add(new String[]{ele.getTextTrim(),ele.attributeValue("retries")});
			}

			List initializers=root.selectNodes("//Initializers/Initializer");
			for(int i=0;initializers!=null&&i<initializers.size();i++){
				Element ele=(Element)initializers.get(i);
				initializersClasses.add(ele.attributeValue("init-handler"));
			}

			String os=System.getProperty("os.name");
			System.out.println("os - "+os);
			
			Runtime runtime = Runtime.getRuntime(); 		
			for(int i=0;i<initialCommands.size();i++){
				String[] command=(String[])initialCommands.get(i);
				
				//最多重试次数
				int retries=1;
				if(JUtilMath.isInt(command[1])) retries=Integer.parseInt(command[1]);
	
				//已经尝试执行次数
				int hasRetried=1;
				
				//第一次执行
				boolean success=false;
				try{
					OS.executeCommand(command[0]);
					success=true;
				}catch (Exception ex){
					ex.printStackTrace();
					success=false;
				}

				while(!success && hasRetried<retries){
					try{
						Thread.sleep(10000);
					}catch(Exception ex){}
					hasRetried++;
					try{
						OS.executeCommand(command[0]);
						success=true;
					}catch (Exception ex){
						ex.printStackTrace();
						success=false;
					}
				}
			}
			
			for(int i=0;i<initializersClasses.size();i++){
				try{
					String cls=(String)initializersClasses.get(i);
					Initializer init=(Initializer)Class.forName(cls).newInstance();
					System.out.println("初始化 - "+init.getClass().getName());

					//异步执行
					(new Thread(new InitializerThread(init))).start();
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}

			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean onFound(Resource resource) {
		//不是xml资源不予加载
		if(!(resource instanceof ResourceXml)) return false;

		//仅处理sys.xml（旧版配置）或nvwa.xml（新版配置）
		if(!resource.getPath().endsWith("sys.xml")
				&&!resource.getPath().endsWith("nvwa.xml")) return false;

		return load(resource);
	}

	@Override
	public boolean onUpdate(Resource resource) {
		return true;
	}
}
