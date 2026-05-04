package j.core;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.JProperties;
import j.core.webserver.undertow.UndertowConf;
import j.core.webserver.undertow.UndertowWebServer;
import j.core.fs.JDFSFile;
import j.core.nvwa.Nvwa;
import j.util.JUtilString;

@ClassDescription(author = "肖炯",
        date = "2021/11/16",
        description = "")
public class Startup {
    //启动模式：独立模式
    public final static String STARTUP_MODE_STANDALONE="standalone";

    //启动模式：在web server容器中启动（通过自启动的servlet）
    public final static String STARTUP_MODE_WEBAPP="webapp";

    //部署模式：普通部署
    public final static String DEPLOY_MODE_COMMON="common";

    //部署模式：jar包
    public final static String DEPLOY_MODE_JAR="jar";

    //部署模式
    private static String deployMode=DEPLOY_MODE_COMMON;

    //系统是否已经停止
    private static boolean destroyed=false;

    /**
     *
     * @return
     */
    public static boolean deployAsJar(){
        return "true".equals(JProperties.getEnv("deployAsJar"));
    }

    /**
     *
     */
    public static void destroy(){
        destroyed=true;
    }

    /**
     *
     * @return
     */
    public static boolean isDestroyed(){
        return destroyed;
    }

    /**
     * 启动系统
     * @param args
     */
    public static void main(String[] args) {
        //该配置已作废，改为根据webserver配置自动判断
        //String startupMode= JProperties.getEnv("StartupMode");
        //System.out.println("StartupMode:"+startupMode);

        UndertowConf undertowConf=new UndertowConf();

        if(undertowConf.isEnabled()){//应用在web服务器内启动
            if(args!=null && args.length>0){//启动参数拼接后保存到文件，以便Nvwa.startup获取到（类加载器不一致导致无法直接传递）
                String _args="";
                for(int i=0; i<args.length; i++){
                    if(i>0) _args+="^";
                    _args+=args[i];
                }

                String userDir=JProperties.getUserDir();
                userDir=JUtilString.replaceAll(userDir, "\\", "/");

                JDFSFile.save(JUtilString.appendPath(userDir, "Startup.Args"),
                        _args,
                        false,
                        "UTF-8");
            }

            //尝试启动嵌入式undertow
            try{
                UndertowWebServer undertowWebServer=new UndertowWebServer();
                undertowWebServer.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{//应用单独启动
            Nvwa.startup(args);
        }
    }

    @MethodDescription(author = "肖炯",
            date = "2021/11/10",
            description = "获取默认的ClassLoader对象")
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader classLoader = Nvwa.class.getClassLoader();
        if (classLoader == null) {
            try {
                classLoader = ClassLoader.getSystemClassLoader();
            } catch (Exception e) {}
        }
        return classLoader;
    }
}
