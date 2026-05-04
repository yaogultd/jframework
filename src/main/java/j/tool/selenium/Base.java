package j.tool.selenium;

import j.core.common.Global;
import j.core.fs.JDFSFile;
import j.http.JHttp;
import j.http.JHttpContext;
import j.util.JUtilJSON;
import j.util.JUtilString;
import j.util.log.Logger;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于testing的chromedriver.exe及对应版本的chrome.exe下载下载地址：https://googlechromelabs.github.io/chrome-for-testing/
 * chromedriver.exe下载：https://chromedriver.storage.googleapis.com/index.html
 * chrome历史版本下载：https://downzen.com/en/windows/google-chrome/versions
 * geckodriver.exe下载地址：https://github.com/mozilla/geckodriver/releases
 */
public class Base{
    //日志
    private static Logger log = Logger.create(Base.class);

    //插件类型 - Chrome（chromedriver.exe）
    public static final String PLUGIN_TYPE_CHROME="CHROME";

    //插件类型 - Firefox（geckodriver.exe）
    public static final String PLUGIN_TYPE_FIREFOX="FIREFOX";

    //插件路径
    @Setter
    private static String DRIVER_CHROME_PAHT=null;

    @Setter
    private static String DRIVER_CHROME_PROFILE_PAHT=null;

    @Setter
    private static String DRIVER_CHROME_EXECUTABLE=null;

    @Setter
    private static String DRIVER_FIREFOX_PAHT=null;

    @Setter
    private static String DRIVER_FIREFOX_PROFILE_PAHT=null;

    @Setter
    private static String DRIVER_FIREFOX_EXECUTABLE=null;

    //需要注入的js
    @Setter
    private static String injectedScripts;

    //实例
    @Getter
    private static RemoteWebDriver driver;
    
    /**
     * 初始化
     * @param pluginType
     * @throws Exception
     */
    public static RemoteWebDriver init(String pluginType) throws Exception{
        if(!PLUGIN_TYPE_CHROME.equals(pluginType) && !PLUGIN_TYPE_FIREFOX.equals(pluginType)){
            throw new Exception("unsupported plugin type => "+pluginType);
        }

        //关闭已经打开的
        Base.close();

        if(PLUGIN_TYPE_CHROME.equals(pluginType)){
            System.setProperty("webdriver.chrome.driver", DRIVER_CHROME_PAHT);

            ChromeOptions options=new ChromeOptions();
            if(!JUtilString.isBlank(DRIVER_CHROME_EXECUTABLE)) options.setBinary(DRIVER_CHROME_EXECUTABLE);
            if(!JUtilString.isBlank(DRIVER_CHROME_PROFILE_PAHT)){
                options.addArguments(String.format("--user-data-dir=%s", DRIVER_CHROME_PROFILE_PAHT));
            }
            options.setCapability("google:loggingPrefs", "{browser: 'ALL'}");

            driver=new ChromeDriver(options);

            if(!JUtilString.isBlank(injectedScripts)){//注入
                // 准备发送到CDP的命令
                Map<String, Object> params = new HashMap<>();
                params.put("source", injectedScripts);

                // 使用CDP API发送命令
                ((ChromeDriver)driver).executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
            }
        }else{
            System.setProperty("webdriver.gecko.driver",DRIVER_FIREFOX_PAHT);

            FirefoxOptions options=new FirefoxOptions();
            if(!JUtilString.isBlank(DRIVER_FIREFOX_EXECUTABLE)) options.setBinary(DRIVER_FIREFOX_EXECUTABLE);
            if(!JUtilString.isBlank(DRIVER_FIREFOX_PROFILE_PAHT)){
                FirefoxProfile profile=new FirefoxProfile(new File(DRIVER_FIREFOX_PROFILE_PAHT));
                options.setProfile(profile);
            }
            options.setLogLevel(FirefoxDriverLogLevel.DEBUG);

            driver=new FirefoxDriver(options);

            if(!JUtilString.isBlank(injectedScripts)){//注入
                throw new Exception("FirefoxDriver 暂不支持注入js，请使用ChromeDriver");
            }
        }

        return driver;
    }

    /**
     * 关闭
     */
    public static void close(){
        if(driver==null) return;
        try{
            driver.quit();
        }catch (Exception ignored){}
    }

    /**
     * just a sample
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        JHttpContext context1=new JHttpContext();
        test(context1, "https://4903353099-dthc2.mm555.co");
    }

    private static String cf_clearanceValue="";
    private static String __cf_bmValue="";
    private static String usidValue="";
    private static void test(JHttpContext context1, String url) throws Exception{
        JHttp http=JHttp.getInstance();
        org.apache.http.client.HttpClient client=http.createClient();
        try{
            if(!JUtilString.isBlank(cf_clearanceValue)){
                context1.addCookie("cf_clearance", cf_clearanceValue, 0, "4903353099-dthc2.mm555.co", "/");
            }

            if(!JUtilString.isBlank(__cf_bmValue)){
                context1.addCookie("__cf_bm", __cf_bmValue, 0, "4903353099-dthc2.mm555.co", "/");
            }

            if(!JUtilString.isBlank(usidValue)){
                context1.addCookie("usid", usidValue, 0, "4903353099-dthc2.mm555.co", "/");
            }
            context1.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

            String resp=http.getResponse(context1, client, url, "UTF-8");
            System.out.println(resp);
        }catch (Exception e){
            if(context1.getStatus()!=403) e.printStackTrace();
        }
        if(context1.getStatus()==403){
            setCookies(context1, url);
        }
    }

    private static void setCookies(JHttpContext context1, String url) throws Exception{
        Base.setDRIVER_CHROME_PAHT("D:/work/jframework/doc/selenium/chromedriver.exe");
        Base.setDRIVER_CHROME_EXECUTABLE("D:/work/jframework/doc/selenium/geckodriver.exe");
        Base.setDRIVER_CHROME_PROFILE_PAHT("C:/Users/Genie/AppData/Local/Google/Chrome/User Data/Default");

        //Base.setDRIVER_FIREFOX_PAHT("D:/work/jframework/doc/selenium/geckodriver.exe");
        //Base.setDRIVER_FIREFOX_PROFILE_PAHT("C:/Users/Genie/AppData/Roaming/Mozilla/Firefox/Profiles/k86ckrj4.default");

        File scriptFile = new File("D:\\work\\JFramework\\src\\main\\java\\j\\tool\\selenium\\inject.js");
        if(scriptFile.exists()){
            Base.setInjectedScripts(JDFSFile.read(scriptFile, "UTF-8"));
        }else{
            Base.setInjectedScripts(null);
        }
        Base.init(PLUGIN_TYPE_CHROME);

        driver.get(url);
        Thread.sleep(5000);

        int tries = 0;
        String jsVariableName = "turnstileDatas"; // JS变量名称

        Object resultObject = ((JavascriptExecutor) driver).executeScript("return window." + jsVariableName);//返回JS变量的值
        while (resultObject==null && tries<10){
            Global.sleep1000Millis();
            tries++;
            resultObject = ((JavascriptExecutor) driver).executeScript("return window." + jsVariableName);//返回JS变量的值
        }

        if(resultObject==null){
            System.out.println("获取数据失败（可能不需要验证，继续尝试下一步操作）");

            Cookie cf_clearance=driver.manage().getCookieNamed("cf_clearance");
            if(cf_clearance!=null){
                System.out.println("cf_clearance = "+cf_clearance.getValue());
                cf_clearanceValue=cf_clearance.getValue();
            }

            Cookie __cf_bm=driver.manage().getCookieNamed("__cf_bm");
            if(__cf_bm!=null){
                System.out.println("__cf_bm = "+__cf_bm.getValue());
                __cf_bmValue=__cf_bm.getValue();
            }

            Cookie usid=driver.manage().getCookieNamed("usid");
            if(usid!=null){
                System.out.println("usid = "+usid.getValue());
                usidValue=usid.getValue();
            }
            Base.close();
            test(context1, url);
            return;
        }

        String result = resultObject.toString();
        result = JUtilString.replaceAll(result, "YOUR API KEY", "774f8eddf98b2b4a6796367996bf3537");
        System.out.println("获取数据成功 => \r\n"+result);

        String captchaCraker="https://api.2captcha.com";

        JHttp http=JHttp.getInstance();
        JHttpContext context=new JHttpContext();

        JSONObject resultJson = JUtilJSON.parse(result);

        StringBuffer request = new StringBuffer();
        request.append("{");
        request.append("\"clientKey\":\"774f8eddf98b2b4a6796367996bf3537\"");
        request.append(",\"task\":{");
        request.append("\"type\":\"TurnstileTaskProxyless\"");
        request.append(",\"websiteURL\":\"https://4903353099-dthc2.mm555.co/\"");
        request.append(",\"websiteKey\":\""+JUtilJSON.string(resultJson, "sitekey")+"\"");
        request.append(",\"action\":\"managed\"");
        request.append(",\"data\":\""+JUtilJSON.string(resultJson, "data")+"\"");
        request.append(",\"pagedata\":\""+JUtilJSON.string(resultJson, "pagedata")+"\"");
        request.append(",\"userAgent\":\""+JUtilJSON.string(resultJson, "userAgent")+"\"");
        request.append("}}");

        System.out.println("验证破解，请求 -> " + request);

        context.setRequestBody(request.toString());
        context.setContentType("application/json");
        context=http.post(context,null, JUtilString.appendUrl(captchaCraker, "createTask"),null, "UTF-8");

        /**
         * {
         *     "errorId": 0,
         *     "taskId": 72345678901
         * }
         */
        String captchaResp= context.getResponseText();
        System.out.println("验证破解，resp -> " + captchaResp);

        JSONObject _resp= JUtilJSON.parse(captchaResp);
        Integer errorId=JUtilJSON.getInteger(_resp, "errorId");
        if(errorId==null || errorId!=0){
            System.out.println("验证破解，创建任务失败！");
            Base.close();
            return;
        }
        String taskId=JUtilJSON.string(_resp, "taskId");


        StringBuffer resultGetter = new StringBuffer();
        resultGetter.append("{\"clientKey\": \"774f8eddf98b2b4a6796367996bf3537\"");
        resultGetter.append(",\"taskId\": "+taskId);
        resultGetter.append("}");

        tries=0;
        while(tries<30) {
            tries++;

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }

            context.setRequestBody(resultGetter.toString());
            context.setContentType("application/json");
            captchaResp = http.postResponse(context, null, JUtilString.appendUrl(captchaCraker, "getTaskResult"), null, "UTF-8");
            System.out.println("验证破解，获取结果 -> " + captchaResp);

            /**
             * {
             *     "errorId": 0,
             *     "status": "ready",
             *     "solution": {},
             *     "cost": "0.00299",
             *     "ip": "1.2.3.4",
             *     "createTime": 1692863536,
             *     "endTime": 1692863556,
             *     "solveCount": 1
             * }
             */
            _resp = JUtilJSON.parse(captchaResp);
            errorId=JUtilJSON.getInteger(_resp, "errorId");
            if(errorId==null || errorId!=0){
                System.out.println("验证破解，获取结果失败！");
                Base.close();
                return;
            }

            String status=JUtilJSON.string(_resp, "status");
            if(!"ready".equals(status)) continue;
            break;
        }

        JSONObject solution = JUtilJSON.object(_resp, "solution");
        String token = JUtilJSON.string(solution, "token");
        String userAgent = JUtilJSON.string(solution, "userAgent");
        if(JUtilString.isBlank(token)){
            System.out.println("验证破解，token is emplty, 失败！");
            Base.close();
        }

        System.out.println("token = "+token);
        System.out.println("userAgent = "+userAgent);

        //设置cookie
        ((JavascriptExecutor) driver).executeScript("cfCallback('"+token+"')");

        Cookie cf_clearance=driver.manage().getCookieNamed("cf_clearance");
        if(cf_clearance!=null){
            System.out.println("cf_clearance = "+cf_clearance.getValue());
            cf_clearanceValue=cf_clearance.getValue();
        }

        Cookie __cf_bm=driver.manage().getCookieNamed("__cf_bm");
        if(__cf_bm!=null){
            System.out.println("__cf_bm = "+__cf_bm.getValue());
            __cf_bmValue=__cf_bm.getValue();
        }

        Cookie usid=driver.manage().getCookieNamed("usid");
        if(usid!=null){
            System.out.println("usid = "+usid.getValue());
            usidValue=usid.getValue();
        }

        Base.close();
        test(context1, url);
    }
}