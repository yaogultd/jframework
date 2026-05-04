package j.tool.javascript;

import j.core.fs.JDFSFile;
import j.util.ConcurrentMap;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;

/**
 * 
 * @author 肖炯
 *
 */
public class JavaScriptBridge {
	private static ConcurrentMap<String, JavaScriptBridge> instances=new ConcurrentMap<>();
	private Invocable invocable;
	
	/**
	 * 
	 * @param invocable
	 */
	public JavaScriptBridge(Invocable invocable){
		this.invocable = invocable;
	}
	
	/**
	 * 
	 * @param filePath
	 * @param fileEncoding
	 * @return
	 * @throws Exception
	 */
	public static JavaScriptBridge getInstanceOfJsFile(String filePath, String fileEncoding) throws Exception{
		if(instances.containsKey(filePath)) return instances.get(filePath);

		String script=JDFSFile.read(new File(filePath),fileEncoding);
		return getInstanceOfJsString(filePath, script);
	}
	
	/**
	 * 
	 * @param instanceName
	 * @param script
	 * @return
	 * @throws Exception
	 */
	public static JavaScriptBridge getInstanceOfJsString(String instanceName, String script) throws Exception{
		if(instances.containsKey(instanceName)){
			return (JavaScriptBridge)instances.get(instanceName);
		}

		try{
			// 1. 获取 GraalJS 引擎
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("graal.js");

			if (engine == null) {
				System.out.println("❌ Graal.js 引擎未找到，请检查依赖");
				return null;
			}
			System.out.println("✅ 成功获取 Graal.js 引擎");

			// 2. ★★★ 关键：注入浏览器环境的 Polyfill（解决 window is not defined）★★★
			String polyfill =
					"// 模拟浏览器全局对象\n" +
							"var window = this;\n" +
							"var global = this;\n" +
							"var self = this;\n" +
							"var navigator = { userAgent: 'GraalVM Java' };\n" +
							"var document = {};\n" +
							"var location = {};\n" +
							"var console = { log: print, error: print };\n" +
							"// 模拟 crypto 对象（jsencrypt 需要）\n" +
							"if (typeof crypto === 'undefined') {\n" +
							"    var crypto = {\n" +
							"        getRandomValues: function(array) {\n" +
							"            for (var i = 0; i < array.length; i++) {\n" +
							"                array[i] = Math.floor(Math.random() * 256);\n" +
							"            }\n" +
							"            return array;\n" +
							"        }\n" +
							"    };\n" +
							"}\n";

			engine.eval(polyfill);
			System.out.println("✅ Polyfill 注入完成");

			engine.eval(script);
			System.out.println("✅ script 加载完成");

			Invocable invocable = (Invocable) engine;
			JavaScriptBridge instance=new JavaScriptBridge(invocable);
			instances.put(instanceName, instance);

			return instances.get(instanceName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 
	 * @param name
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Object call(String name, Object... params) throws Exception{
		return this.invocable.invokeFunction(name, params);
	}

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		String js= JDFSFile.read(new File("D:\\work\\JFramework\\doc\\test\\jsencrypt.min.js"), "UTF-8");

		System.out.println(js);
		JavaScriptBridge jsb=JavaScriptBridge.getInstanceOfJsString("jsencrypt", js);

		String pubKey = "-----BEGIN PUBLIC KEY-----\n" +
				"MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgH1ufjmfY7tAt14i4z1ba8HvjCEf\n" +
				"HeGdG8+9ABVbZhPphrW15dyb/Q6HfTCyBwDLdGZViiPcXli3/guMfv83o6nLZPyz\n" +
				"gg8f7fblKF9vB5l9AmuzKSVL/8DCBaIXIr1aRmMBV04f1v4xXPyUdNXQkzDkXobX\n" +
				"wPpk3jyb/K/XKefJAgMBAAE=\n" +
				"-----END PUBLIC KEY-----";

		String plainText = "As123123";

		String encrypted = (String)jsb.call("encryptText", pubKey, plainText);
		System.out.println(encrypted);
	}
}
