package j.I18N;

import j.core.annotation.action.Action;
import j.core.annotation.action.Handler;
import j.core.annotation.description.FieldDescription;
import j.core.common.JArray;
import j.core.dao.util.SQLUtil;
import j.core.fs.JDFSFile;
import j.core.nvwa.Nvwa;
import j.core.nvwa.resource.Resource;
import j.core.nvwa.resource.ResourceHelper;
import j.core.nvwa.resource.ResourceXml;
import j.core.sys.Initializer;
import j.core.sys.SysUtil;
import j.core.web.Constants;
import j.core.web.handler.JHandler;
import j.core.web.handler.JResponse;
import j.core.web.handler.JSession;
import j.log.Logger;
import j.util.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Handler(path = "/framework/api/I18N")
public class I18N extends JHandler implements Initializer{
	private static Logger log=Logger.create(I18N.class);

	@FieldDescription(description = "多语言资源文件的名称（描述性的），key为文件名，value为文件的描述性名称。为避免某些操作系统上使用中文作为文件名可能出现的问题，文件名不使用中文。")
	private static ConcurrentMap<String, String> namesOfFiles=new ConcurrentMap();

	@FieldDescription(description = "多语言资源文件名列表")
	private static ConcurrentList I18NResourceFileNames=new ConcurrentList();

	@FieldDescription(description = "需要进行多语言处理的url")
	private static List urls=new LinkedList();

	@FieldDescription(description = "多语言资源")
	private static ConcurrentMap<String, I18NResource> I18NStringCollection=new ConcurrentMap();

	@FieldDescription(description = "可选语言")
	public static ConcurrentMap languages=new ConcurrentMap();

	@FieldDescription(description = "默认语言")
	public static String defaultLanguage="zh-cn";

	@FieldDescription(description = "是否启用多语言")
	public static boolean enabled=false;

	@FieldDescription(description = "是否通过cookie记录用户选择的语言")
	public static boolean cookieEnabled=false;

	@FieldDescription(description = "未翻译的多语言是否显示标记")
	public static boolean showUnknownTag=false;

	@FieldDescription(description = "在使用默认语言的情况下，未翻译的多语言是否显示标记")
	public static boolean showUnknownTagIfDefaultLang=false;

	@FieldDescription(description = "解析主配置文件得到的xml document对象")
	private static Document docForConfig=null;

	@FieldDescription(description = "解析主配置文件得到的xml document对象的根节点")
	private static Element rootForConfig=null;

	@FieldDescription(description = "解析各多语言资源文件得到的xml document对象")
	private static ConcurrentMap<String, Document> docsForFile=new ConcurrentMap<>();

	@FieldDescription(description = "解析各多语言资源文件得到的xml document对象的根节点")
	private static ConcurrentMap<String, Element> rootsForFile=new ConcurrentMap<>();

	/*
	 *  (non-Javadoc)
	 * @see j.sys.Initializer#initialization()
	 */
	public void initialization()throws Exception{
		//加载配置信息
		loadConfig();
	}

	/**
	 *
	 * @param file
	 * @return
	 */
	public static Document getXmlDocFor(String file){
		return docsForFile.get(file);
	}

	/**
	 *
	 * @param file
	 * @return
	 */
	public static Element getXmlRootFor(String file){
		return rootsForFile.get(file);
	}

	/**
	 * 加载配置
	 * @throws Exception
	 */
	private static void loadConfig() throws Exception{
		List<String> paths=new ArrayList<>();

		String configFile=JUtilString.appendPath(ResourceHelper.I18NDir(), "config.xml");

		if((new File(configFile)).exists()) paths.add(configFile);
		paths.add(ResourceHelper.getRelativePath(configFile));

		int seeks=0;
		ConcurrentMap<String, Resource> resources = Nvwa.getResources(paths, new String[]{".xml"});
		while((resources==null || resources.isEmpty() || resources.listValues().get(0)==null) && seeks<30){
			seeks++;

			//log.log("config not found in "+JArray.toString(paths, ","), -1);
			try{
				Thread.sleep(2000);
			}catch (Exception e){}

			resources = Nvwa.getResources(paths, new String[]{".xml"});
		}

		if(resources==null || resources.isEmpty() || resources.listValues().get(0)==null){
			File inFixedPath=new File(ResourceHelper.I18NDir(), "config.xml");
			if(!inFixedPath.exists()){
				//log.log("I18N has been disabled because config not found in "+JArray.toString(paths, ",")+" and fixed path "+inFixedPath.getAbsolutePath(), -1);
				I18N.enabled=false;
				return;
			}

			docForConfig=JUtilDom4j.parse(inFixedPath.getAbsolutePath(), "UTF-8");
		}else{
			ResourceXml xml=(ResourceXml)resources.listValues().get(0);
			docForConfig=xml.getResource();
		}

		//清空
		languages.clear();
		I18NResourceFileNames.clear();
		namesOfFiles.clear();
		urls.clear();

		//解析
		rootForConfig=docForConfig.getRootElement();

		I18N.enabled="true".equalsIgnoreCase(rootForConfig.elementText("enabled"));
		log.log("I18N.enabled:"+enabled,-1);

		I18N.cookieEnabled="true".equalsIgnoreCase(rootForConfig.elementText("cookieEnabled"));
		log.log("I18N.cookieEnabled:"+cookieEnabled,-1);

		I18N.showUnknownTag="true".equalsIgnoreCase(rootForConfig.elementText("showUnknownTag"));
		log.log("I18N.showUnknownTag:"+showUnknownTag,-1);

		I18N.showUnknownTagIfDefaultLang="true".equalsIgnoreCase(rootForConfig.elementText("showUnknownTagIfDefaultLang"));
		log.log("I18N.showUnknownTagIfDefaultLang:"+showUnknownTagIfDefaultLang,-1);

		Element languagesE=rootForConfig.element("languages");
		I18N.defaultLanguage=languagesE.attributeValue("default");
		log.log("I18N.defaultLanguage:"+defaultLanguage,-1);

		List languages=languagesE.elements("language");
		for(int i=0;languages!=null&&i<languages.size();i++){
			Element languageElement=(Element)languages.get(i);
			Language language=new Language();
			language.setCode(languageElement.attributeValue("code"));
			language.setName(languageElement.attributeValue("name"));
			String countries=languageElement.attributeValue("countries");
			if(countries!=null&&!"".equals(countries)){
				language.setCountries(countries.split(","));
			}
			I18N.languages.put(language.getCode(),language);
			log.log("I18N.language:"+language.getCode()+","+language.getName(),-1);
		}

		Element modules=rootForConfig.element("modules");
		List strs=modules.elements("module");
		for(int i=0;i<strs.size();i++){
			Element str=(Element)strs.get(i);
			I18N.I18NResourceFileNames.add(str.getText());
			namesOfFiles.put(str.getText(), str.attributeValue("remark"));
			log.log("I18N.I18NResourceFileName:"+str.attributeValue("remark")+" -> "+str.getText(),-1);
		}

		Element urlsEle=rootForConfig.element("urls");
		List urlEles=urlsEle==null?null:urlsEle.elements("url");
		for(int i=0;urlEles!=null&&i<urlEles.size();i++){
			Element rEle=(Element)urlEles.get(i);

			I18NUrl r=new I18NUrl();
			r.setUrlPattern(rEle.attributeValue("pattern"));
			r.setExtension(rEle.attributeValue("extension"));
			r.setMatch(rEle.attributeValue("match"));

			List excludes=rEle.elements("exclude");
			for(int j=0;excludes!=null&&j<excludes.size();j++){
				Element ex=(Element)excludes.get(j);
				r.addExclude(ex.getText());
			}
			urls.add(r);

			log.log(r.toString(),-1);
		}

		//加载多语言资源
		loadResources();
	}

	/**
	 * 加载多语言资源
	 * @throws Exception
	 */
	private static void loadResources() throws Exception{
		for(int i=0;i<I18N.I18NResourceFileNames.size();i++){
			List<String> paths=new ArrayList<>();

			String name=(String) I18N.I18NResourceFileNames.get(i);

			String configFile=JUtilString.appendPath(ResourceHelper.I18NDir(), name);

			if((new File(configFile)).exists()) paths.add(configFile);
			paths.add(ResourceHelper.getRelativePath(configFile));

			ConcurrentMap<String, Resource> resources= Nvwa.getResources(paths, new String[]{".xml"});
			if(resources==null || resources.isEmpty()){
				log.log("no I18N resource matches "+name+" in "+ JArray.toString(paths, ","), -1);
				continue;
			}

			List<ResourceXml> _resources=resources.listValues();
			for(int j=0; j<_resources.size(); j++){
				ResourceXml i18nResource=_resources.get(j);

				//log.log("resource -> "+i18nResource.getName()+" -> "+i18nResource.getPath(), -1);

				//如果是jar包中的资源，查找外部目录中是否有对应资源（修改过的），如有则不予处理
				if(i18nResource.isInJar() && externalResourceExists(_resources, i18nResource.getName())){
					//log.log("resource "+i18nResource.getPath() +" exists in external directory, so ignored.", -1);
					continue;
				}

				process(i18nResource);
			}
		}
	}

	/**
	 *
	 * @param xml
	 * @throws Exception
	 */
	private static void process(ResourceXml xml) throws Exception{
		//移除旧记录
		List allResources=I18NStringCollection.listKeys();
		for(int i=0;i<allResources.size();i++){
			Object key=allResources.get(i);
			I18NResource r=I18NStringCollection.get(key);
			if(r.getFile().equals(xml.getName())){
				I18NStringCollection.remove(key);
			}
		}
		//移除旧记录  end

		//解析
		Document doc = xml.getResource();
		Element root = doc.getRootElement();

		docsForFile.put(xml.getName(), doc);
		rootsForFile.put(xml.getName(), root);

		List children=root.elements("group");
		for(int i=0;children!=null&&i<children.size();i++){
			Element child=(Element)children.get(i);
			String group=child.attributeValue("name");

			List resources=child.elements("string");
			for(int j=0;j<resources.size();j++){
				Element resElement=(Element)resources.get(j);
				String key=resElement.attributeValue("key");
				I18NResource resource=new I18NResource(xml.getName(),group,key);

				List languages=resElement.elements("language");
				for(int k=0;k<languages.size();k++){
					Element languageElement=(Element)languages.get(k);
					String language=languageElement.attributeValue("code");
					String text=languageElement.getText();
					resource.addLanguage(language,text);

					if("".equals(group)){
						I18N.I18NStringCollection.put(key+"<SPL>"+language,resource);
						//log.log(key+","+language+" = "+value,-1);
					}else{
						I18N.I18NStringCollection.put(group+"<SPL>"+key+"<SPL>"+language,resource);
						//log.log(group+","+key+","+language+" = "+value,-1);
					}
				}
			}
		}
	}
	
	/**
	 *
	 * @param xml
	 * @throws Exception
	 */
	private static void process(File xml) throws Exception{
		if(xml==null || !xml.exists()) return;

		//移除旧记录
		List allResources=I18NStringCollection.listKeys();
		for(int i=0;i<allResources.size();i++){
			Object key=allResources.get(i);
			I18NResource r=(I18NResource)I18NStringCollection.get(key);
			if(r.getFile().equals(xml.getName())){
				I18NStringCollection.remove(key);
			}
		}
		//移除旧记录  end

		//解析
		Document doc = JUtilDom4j.parse(new FileInputStream(xml), "UTF-8");
		Element root = doc.getRootElement();

		docsForFile.put(xml.getName(), doc);
		rootsForFile.put(xml.getName(), root);

		List children=root.elements("group");
		for(int i=0;children!=null&&i<children.size();i++){
			Element child=(Element)children.get(i);
			String group=child.attributeValue("name");

			List resources=child.elements("string");
			for(int j=0;j<resources.size();j++){
				Element resElement=(Element)resources.get(j);
				String key=resElement.attributeValue("key");
				I18NResource resource=new I18NResource(xml.getName(),group,key);

				List languages=resElement.elements("language");
				for(int k=0;k<languages.size();k++){
					Element languageElement=(Element)languages.get(k);
					String language=languageElement.attributeValue("code");
					String text=languageElement.getText();
					resource.addLanguage(language,text);

					if("".equals(group)){
						I18N.I18NStringCollection.put(key+"<SPL>"+language,resource);
						//log.log(key+","+language+" = "+value,-1);
					}else{
						I18N.I18NStringCollection.put(group+"<SPL>"+key+"<SPL>"+language,resource);
						//log.log(group+","+key+","+language+" = "+value,-1);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param resources
	 * @param name
	 * @return
	 */
	private static boolean externalResourceExists(List<ResourceXml> resources, String name){
		for(int i=0; i<resources.size(); i++) {
			Resource resource = resources.get(i);
			if(!resource.isInJar() && resource.getName().equals(name)) return true;
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public static List getLanguages(){
		return languages.listValues();
	}
	
	/**
	 * 
	 * @return
	 */
	public static Language getLanguage(String key){
		return (Language)languages.get(key);
	}
	
	/**
	 * 
	 * @return
	 */
	public static List getI18NResourceFileNames(){
		I18NFileSorter sorter=new I18NFileSorter();
		return sorter.bubble(I18NResourceFileNames,JUtilSorter.ASC);
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getNameOfFile(String fileName) {
		return (String)namesOfFiles.get(fileName);
	}
	
	/**
	 * 
	 * @param key
	 * @param language
	 * @return
	 */
	private static String getText(String key,String language){
		if(key.indexOf(",")>-1){
			int i=key.indexOf(",");
			return getText(key.substring(0,i),key.substring(i+1),language);
		}else{
			return getText((String)null,key,language);
		}
	}
	
	
	/**
	 * 
	 * @param group
	 * @param key
	 * @param language
	 * @return
	 */
	private static String getText(String group,String key,String language){
		if(language==null||language.equals("")){
			language=I18N.defaultLanguage;
		}
		
		boolean nonJs=false;
		if(key.startsWith("NON-JS")){
			key=key.substring(6);
			nonJs=true;
		}

		I18NResource resource=null;
		if(group==null||group.equals("")){
			resource=I18N.I18NStringCollection.get(key+"<SPL>"+language);
		}else{
			resource=I18N.I18NStringCollection.get(group+"<SPL>"+key+"<SPL>"+language);
			if(resource==null){
				resource=I18N.I18NStringCollection.get(key+"<SPL>"+language);
			}
		}
		
		String string=resource==null?null:resource.getLanguage(language);
		if(!nonJs&&string!=null) {
			string=string.replaceAll("'", "\\\\'");
		}
		
		return string;
	}
	
	/**
	 * 
	 * @param group
	 * @param key
	 * @param language
	 * @return
	 */
	private static String getTextIgnoreGlobal(String group,String key,String language){
		if(language==null||language.equals("")){
			language=I18N.defaultLanguage;
		}
		
		boolean nonJs=false;
		if(key.startsWith("NON-JS")){
			key=key.substring(6);
			nonJs=true;
		}

		I18NResource resource=null;
		if(group==null||group.equals("")){
			resource=I18N.I18NStringCollection.get(key+"<SPL>"+language);
		}else{
			resource=I18N.I18NStringCollection.get(group+"<SPL>"+key+"<SPL>"+language);
		}
		
		String string=resource==null?null:resource.getLanguage(language);
		if(!nonJs&&string!=null) {
			string=string.replaceAll("'", "\\\\'");
		}
		
		return string;
	}
	
	/**
	 * 
	 * @param group
	 * @param lang
	 * @return
	 */
	private static ConcurrentMap getTexts(String group, String lang){
		ConcurrentMap texts=new ConcurrentMap();
		List keys=I18N.I18NStringCollection.listKeys();
		for(int i=0;i<keys.size();i++){
			String key=(String)keys.get(i);
			String[] keyCells=key.split("<SPL>");
			
			if(group==null||group.equals("")){
				if(keyCells.length==2){
					if(I18N.I18NStringCollection.get(key)!=null) texts.put(key, I18N.I18NStringCollection.get(key).getLanguage(lang));
				}
			}else{
				if(keyCells.length==3&&keyCells[0].equals(group)){
					if(I18N.I18NStringCollection.get(key)!=null) texts.put(key, I18N.I18NStringCollection.get(key).getLanguage(lang));
				}
			}
		}
		
		return texts;
	}


	/**
	 *
	 * @param lang
	 * @return
	 */
	private static boolean _showUnknownTag(String lang){
		if(I18N.defaultLanguage.equals(lang)) return showUnknownTagIfDefaultLang;
		else return showUnknownTag;
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static String getCurrentLanguage(HttpServletRequest request){
		if(request==null) return I18N.defaultLanguage;

		String lang=SysUtil.getHttpHeader(request, Constants.I18N_LANGUAGE);
		if(JUtilString.isBlank(lang)) lang=SysUtil.getCookie(request, Constants.I18N_LANGUAGE);
		return JUtilString.isBlank(lang)?I18N.defaultLanguage:lang;
	}

	/**
	 *
	 * @param group
	 * @return
	 * @throws Exception
	 */
	public static List<I18NResource> getResources(String group) throws Exception {
		List<I18NResource> rs = new ArrayList<>();
		List<I18NResource> allResources = I18NStringCollection.listValues();
		for (int i = 0; i < allResources.size(); i++) {
			I18NResource r = allResources.get(i);
			if(JUtilString.isBlank(group) && JUtilString.isBlank(r.getGroup())) rs.add(r);
			else if(JUtilString.equals(group, r.getGroup())) rs.add(r);
		}
		return rs;
	}


	/**
	 *
	 * @param _content
	 * @param request
	 * @return
	 */
	public static String convert(String _content,HttpServletRequest request){
		return request==null?convert(_content,I18N.getCurrentLanguage(null)):convert(_content,request.getRequestURI(),I18N.getCurrentLanguage(request));
	}

	/**
	 *
	 * @param _content
	 * @param group
	 * @param request
	 * @return
	 */
	public static String convert(String _content,String group,HttpServletRequest request){
		return convert(_content,group,I18N.getCurrentLanguage(request));
	}

	/**
	 *
	 * @param content
	 * @param group
	 * @param lang
	 * @return
	 */
	public static String convert(String content,String group,String lang){
		if(!I18N.enabled) return content;

		if(content==null||content.indexOf("I{")<0) return content;

		List<String> cells=new ArrayList<String>();

		String[] contents=content.split("I\\{");

		boolean startsWith=content.startsWith("I{");

		StringBuffer _content=new StringBuffer(startsWith?"":contents[0]);
		for(int i=(startsWith?0:1);i<contents.length;i++) {
			if(JUtilString.isBlank(contents[i])) continue;

			int end=contents[i].indexOf("}");
			if(end<0) {
				_content.append(contents[i]);
				continue;
			}

			String key=contents[i].substring(0,end);

			String theKey=null;
			if(key.startsWith(".")){
				theKey=key.substring(1);
			}else if(key.indexOf(",")>0){
				theKey=key.substring(key.indexOf(",")+1);
			}else{
				theKey=key;
			}

			String alt=getTextIgnoreGlobal(group,theKey,lang);//强制优先获取针对本网页定义的多语言资源
			if(alt==null){
				if(key.startsWith(".")) alt=getText(group,theKey,lang);
				else alt=getText(key,lang);
			}

			if(alt==null) {
				String thisGroup="";
				if(key.startsWith(".")){
					alt=key.substring(1);
					thisGroup=".";
				}else if(key.indexOf(",")>0){
					alt=key.substring(key.indexOf(",")+1);
					thisGroup=key.substring(0,key.indexOf(",")+1);
				}else{
					alt=key;
				}
				if(!alt.startsWith("NON-JS")){
					alt=alt.replaceAll("'", "\\\\'");
				}

				if(_showUnknownTag(lang)) {
					alt="I{"+thisGroup+alt+"}";
				}else if(alt.startsWith("NON-JS")){
					alt=alt.substring(6);
				}
			}

			_content.append(alt);
			_content.append(contents[i].substring(end+1));
		}

		cells.clear();
		cells=null;

		//引入到js
		content=_content.toString();
		int start=content.indexOf("<import-i1n8>")+13;
		int end=content.indexOf("</import-i1n8>",start);
		while(start>=13){
			group=content.substring(start,end);
			String _src=content.substring(start-13,end+14);

			ConcurrentMap strings=getTexts(group, lang);
			String js="";
			List keys=strings.listKeys();
			for(int i=0;i<keys.size();i++){
				String key=(String)keys.get(i);
				String[] keyCells=key.split("<SPL>");

				if(keyCells.length==2){
					js+="Lang.a('"+keyCells[0]+"','"+keyCells[1]+"','"+strings.get(key)+"');\r\n";
				}else{
					js+="Lang.a('"+keyCells[1]+"','"+keyCells[2]+"','"+strings.get(key)+"');\r\n";
				}
			}

			content=JUtilString.replaceAll(content, _src, js);

			start=content.indexOf("<import-i1n8>",end)+13;
			end=content.indexOf("</import-i1n8>",start);
		}
		//引入到js end

		_content=null;

		return content;
	}

	/**
	 *
	 * @param content
	 * @param lang
	 * @return
	 */
	public static String convert(String content,String lang){
		if(!I18N.enabled) return content;

		if(content==null||content.indexOf("I{")<0) return content;

		String[] contents=content.split("I\\{");

		boolean startsWith=content.startsWith("I{");

		StringBuffer _content=new StringBuffer(startsWith?"":contents[0]);
		for(int i=(startsWith?0:1);i<contents.length;i++) {
			if(JUtilString.isBlank(contents[i])) continue;

			int end=contents[i].indexOf("}");
			if(end<0) {
				_content.append(contents[i]);
				continue;
			}

			String key=contents[i].substring(0,end);

			String alt=null;
			if(key.startsWith(".")){
				alt=getText(key.substring(1),lang);
			}else{
				alt=getText(key,lang);
			}

			if(alt==null) {
				String thisGroup="";
				if(key.startsWith(".")){
					alt=key.substring(1);
					thisGroup=".";
				}else if(key.indexOf(",")>0){
					alt=key.substring(key.indexOf(",")+1);
					thisGroup=key.substring(0,key.indexOf(",")+1);
				}else{
					alt=key;
				}
				if(!alt.startsWith("NON-JS")){
					alt=alt.replaceAll("'", "\\\\'");
				}

				if(_showUnknownTag(lang)) {
					alt="I{"+thisGroup+alt+"}";
				}else if(alt.startsWith("NON-JS")){
					alt=alt.substring(6);
				}
			}

			_content.append(alt);
			_content.append(contents[i].substring(end+1));
		}

		//引入到js
		content=_content.toString();
		int start=content.indexOf("<import-i1n8>")+13;
		int end=content.indexOf("</import-i1n8>",start);
		while(start>=13){
			String group=content.substring(start,end);
			String _src=content.substring(start-13,end+14);

			ConcurrentMap strings=getTexts(group, lang);
			String js="";
			List keys=strings.listKeys();
			for(int i=0;i<keys.size();i++){
				String key=(String)keys.get(i);
				String[] keyCells=key.split("<SPL>");

				if(keyCells.length==2){
					js+="Lang.a('"+keyCells[0]+"','"+keyCells[1]+"','"+strings.get(key)+"');\r\n";
				}else{
					js+="Lang.a('"+keyCells[1]+"','"+keyCells[2]+"','"+strings.get(key)+"');\r\n";
				}
			}

			content=JUtilString.replaceAll(content, _src, js);

			start=content.indexOf("<import-i1n8>",end)+13;
			end=content.indexOf("</import-i1n8>",start);
		}
		//引入到js end

		_content=null;

		return content;
	}

	/**
	 *
	 * @param content
	 * @param substring
	 * @param alt
	 * @return
	 */
	private static String replaceAll(String content, String substring, String alt) {
		substring=substring.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}").replaceAll("\\+", "\\\\+");
		return content.replaceAll(substring, alt);
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public static boolean need(HttpServletRequest request){
		for(int i=0;i<urls.size();i++){
			I18NUrl r=(I18NUrl)urls.get(i);
			if(r.matches(request)) return true;
		}

		return false;
	}

	/**
	 * 添加一个多语言资源文件
	 * @param fileName 存储到文件系统的文件名
	 * @param remark 对文件的描述性名称
	 * @return
	 */
	private static boolean addModule(String fileName, String remark){
		try{
			if(rootForConfig==null) return false;

			Element modulesElement=rootForConfig.element("modules");
			if(modulesElement==null) return false;

			if(!fileName.toLowerCase().endsWith(".xml")) fileName+=".xml";

			boolean exists=false;
			List modules=modulesElement.elements("module");
			for(int i=0;i<modules.size();i++){
				Element module=(Element)modules.get(i);
				String _fileName=module.getTextTrim();
				if(_fileName.equals(fileName)){
					exists=true;
					break;
				}
			}

			if(!exists){
				Element module=modulesElement.addElement("module");
				module.addAttribute("remark", remark);
				module.setText(fileName);
				I18NResourceFileNames.add(fileName);
				namesOfFiles.put(fileName, remark);

				JUtilDom4j.save(docForConfig,
						JUtilString.appendPath(ResourceHelper.I18NDir(), "config.xml"),
						"UTF-8");
			}

			return true;
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return false;
		}
	}

	/**
	 * 删除一个多语言资源文件
	 * @param fileName 保存在文件系统的文件名称
	 * @return
	 */
	private static boolean delModule(String fileName){
		try{
			if(rootForConfig==null) return false;

			Element modulesElement=rootForConfig.element("modules");
			if(modulesElement==null) return false;

			if(!fileName.toLowerCase().endsWith(".xml")) fileName+=".xml";

			Element exists=null;
			List modules=modulesElement.elements("module");
			for(int i=0;i<modules.size();i++){
				Element module=(Element)modules.get(i);
				String _fileName=module.getTextTrim();
				if(_fileName.equals(fileName)){
					exists=module;
					break;
				}
			}

			if(exists!=null){
				modulesElement.remove(exists);
				JUtilDom4j.save(docForConfig,
						JUtilString.appendPath(ResourceHelper.I18NDir(), "config.xml"),
						"UTF-8");

				File i18nConfigFile=new File(JUtilString.appendPath(ResourceHelper.I18NDir(), fileName));
				if(i18nConfigFile.exists()) i18nConfigFile.delete();

				//重新加载多语言资源
				loadConfig();

				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			return false;
		}
	}
	
	@Action(path = "addFile")
	public void addFile(JSession jsession, HttpServletRequest request, HttpServletResponse response)throws Exception{
		String fileName=SysUtil.getHttpParameter(request, "name","");
		if(!fileName.matches("^[a-zA-Z0-9\\u4E00-\\u9FA5_.\\-]{1,90}$")){
			jsession.jresponse=new JResponse(false,"invalid_file_name","文件名称格式错误");
		}
		
		String remark=SysUtil.getHttpParameter(request, "remark","");
		if(!remark.matches("^[a-zA-Z0-9\\u4E00-\\u9FA5_.\\-]{1,90}$")){
			jsession.jresponse=new JResponse(false,"invalid_remark","备注格式错误");
		}
		
		if(!fileName.endsWith(".xml")) fileName+=".xml";

		String saveIn=ResourceHelper.I18NDir();

		File i18nConfigFile=new File(JUtilString.appendPath(saveIn, fileName));
		if(!i18nConfigFile.exists()){
      		String s="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
      		s+="<root>\r\n";
      		s+="</root>\r\n";
      		
      		JDFSFile.save(i18nConfigFile.getAbsolutePath(), s, false, "UTF-8");
      		
      		if(addModule(fileName,remark)){
				process(new File(JUtilString.appendPath(ResourceHelper.I18NDir(), fileName)));
    			jsession.jresponse=new JResponse(true,"1","添加成功");
      		}else{
    			jsession.jresponse=new JResponse(false,"add_module_failed","添加失败");
      		}
      	}else{
			jsession.jresponse=new JResponse(false,"exists","文件已存在");
      	}
	}

	@Action(path = "delFile")
	public void delFile(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		String name=SysUtil.getHttpParameter(request, "name","");
		if(!name.matches("^[a-zA-Z0-9\\u4E00-\\u9FA5_.\\-]{1,90}$")){
			jsession.jresponse=new JResponse(false,"invalid_file_name","文件名称格式错误");
		}
		
		if(!name.endsWith(".xml")) name+=".xml";
		
		if(delModule(name)){
			jsession.jresponse=new JResponse(true,"1","删除成功");
  		}else{
			jsession.jresponse=new JResponse(false,"del_module_failed","删除失败");
  		}
	}

	@Action(path = "addGroup")
	public void addGroup(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		try{
			String fileName=SysUtil.getHttpParameter(request, "name","");
			if(!fileName.matches("^[a-zA-Z0-9\\u4E00-\\u9FA5_.\\-]{1,90}$")){
				jsession.jresponse=new JResponse(false,"invalid_file_name","文件名称格式错误");
			}
			
			String group=SQLUtil.deleteCriminalChars(SysUtil.getHttpParameter(request, "group",""));
			if(!group.matches("^[\\S ]{0,128}$")){
				jsession.jresponse=new JResponse(false,"invalid_group","分组名称格式错误");
			}
			
			String desc = SQLUtil.deleteCriminalChars(SysUtil.getHttpParameter(request, "desc",""));
			if(!desc.matches("^[\\S ]{1,90}$")){
				jsession.jresponse=new JResponse(false,"invalid_desc","分组描述格式错误");
			}
			
			if(!fileName.endsWith(".xml")) fileName+=".xml";

			Document doc = docsForFile.get(fileName);
			Element root = rootsForFile.get(fileName);

			if(doc==null || root==null){
				jsession.jresponse=new JResponse(false,"file_not_exists","文件不存在");
				return;
			}
			
			Element groupElement=root.addElement("group");
			groupElement.addAttribute("name",group);
			groupElement.addAttribute("desc",desc);
			
			JUtilDom4j.save(doc,JUtilString.appendPath(ResourceHelper.I18NDir(), fileName), "UTF-8");
			process(new File(JUtilString.appendPath(ResourceHelper.I18NDir(), fileName)));

			jsession.jresponse=new JResponse(true,"1","添加成功");
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false,"ERR","系统错误");
		}
	}

	@Action(path = "delGroup")
	public void delGroup(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		try{
			String fielName=SysUtil.getHttpParameter(request, "name","");
			if(!fielName.matches("^[a-zA-Z0-9\\u4E00-\\u9FA5_.\\-]{1,90}$")){
				jsession.jresponse=new JResponse(false,"invalid_file_name","文件名称格式错误");
			}
			
			String group=SQLUtil.deleteCriminalChars(SysUtil.getHttpParameter(request, "group",""));
			if(!group.matches("^[\\S ]{0,128}$")){
				jsession.jresponse=new JResponse(false,"invalid_group","分组名称格式错误");
			}
			
			if(!fielName.endsWith(".xml")) fielName+=".xml";

			Document doc = docsForFile.get(fielName);
			Element root = rootsForFile.get(fielName);

			if(doc==null || root==null){
				jsession.jresponse=new JResponse(false,"file_not_exists","文件不存在");
				return;
			}
			
			Element exists=null;
			List groups=root.elements("group");
			for(int i=0;i<groups.size();i++){
				Element groupElement=(Element)groups.get(i);
				if(groupElement.attributeValue("name").equals(group)){
					exists=groupElement;
					break;
				}
			}
			
			if(exists!=null){
				root.remove(exists);

				JUtilDom4j.save(doc, JUtilString.appendPath(ResourceHelper.I18NDir(), fielName), "UTF-8");
				process(new File(JUtilString.appendPath(ResourceHelper.I18NDir(), fielName)));
				
				jsession.jresponse=new JResponse(true,"1","删除成功");
			}else{
				jsession.jresponse=new JResponse(false,"group_not_exists","分组不存在");
			}
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
			jsession.jresponse=new JResponse(false,"ERR","系统错误");
		}
	}

	@Action(path = "save")
	public void save(JSession jsession,HttpServletRequest request,HttpServletResponse response)throws Exception{
		String content=SysUtil.getHttpParameter(request, "content","");
		String[] elements=content.split("I18N_SPLITTER_A");
		
		Map temp=new HashMap();
		
		Document doc=DocumentHelper.createDocument();
		Element root=doc.addElement("root");
		for(int i=0;i<elements.length;i++){
			if(elements[i].equals("")) continue;
			
			String[] cells=elements[i].split("I18N_SPLITTER_B");
			if(cells.length!=5) continue;
			
			String groupName=cells[0];//SysUtil.getHttpParameter(request, "group_name_"+i);
			String groupDesc=cells[1];//SysUtil.getHttpParameter(request, "group_desc_"+i);
			String key=cells[2];//SysUtil.getHttpParameter(request, "key_"+i);
			String lang=cells[3];//SysUtil.getHttpParameter(request, "lang_"+i);
			String value=cells[4];//SysUtil.getHttpParameter(request, "value_"+i);
			
			Element group=null;
			if(!temp.containsKey(groupName)){
				group=root.addElement("group");
				group.addAttribute("name", groupName);
				group.addAttribute("desc", groupDesc);
				temp.put(groupName,group);
			}else{
				group=(Element)temp.get(groupName);
			}

			Element string=null;
			if(!temp.containsKey(groupName+","+key)){
				string=group.addElement("string");
				string.addAttribute("key", key);
				temp.put(groupName+","+key,string);
			}else{
				string=(Element)temp.get(groupName+","+key);
			}

			Element resource=string.addElement("language");
			resource.addAttribute("code", lang);
			resource.setText(value);
		}
		
		String fileName=SysUtil.getHttpParameter(request, "file");
		JUtilDom4j.save(doc, JUtilString.appendPath(ResourceHelper.I18NDir(), fileName), "UTF-8");
		process(new File(JUtilString.appendPath(ResourceHelper.I18NDir(), fileName)));

		jsession.jresponse=new JResponse(true,"1","保存成功");
	}

	/**
	 * 删除字符串中的多语言标签
	 * @param content
	 * @return
	 */
	public static String clearI18NTags(String content){
		//log.log("convert "+group, -1);

		if(content==null||content.indexOf("I{")<0) return content;

		List<String> cells=new ArrayList<String>();

		String[] contents=content.split("I\\{");

		boolean startsWith=content.startsWith("I{");

		StringBuffer _content=new StringBuffer(startsWith?"":contents[0]);
		for(int i=(startsWith?0:1);i<contents.length;i++) {
			if(JUtilString.isBlank(contents[i])) continue;

			int end=contents[i].indexOf("}");
			if(end<0) {
				_content.append(contents[i]);
				continue;
			}

			String key=contents[i].substring(0,end);

			String alt=null;
			String thisGroup="";
			if(key.startsWith(".")){
				alt=key.substring(1);
				thisGroup=".";
			}else if(key.indexOf(",")>0){
				alt=key.substring(key.indexOf(",")+1);
				thisGroup=key.substring(0,key.indexOf(",")+1);
			}else{
				alt=key;
			}

			if(alt.startsWith("NON-JS")){
				alt=alt.substring(6);
			}

			_content.append(alt);
			_content.append(contents[i].substring(end+1));
		}

		cells.clear();
		cells=null;

		content=_content.toString();
		_content=null;

		return content;
	}
}
