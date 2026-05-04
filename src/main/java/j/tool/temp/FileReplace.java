package j.tool.temp;

import j.I18N.I18N;
import j.core.fs.JDFSFile;
import j.util.JUtilInputStream;
import j.util.JUtilString;

import java.io.File;
import java.io.FileInputStream;

/**
 * 
 * @author 肖炯
 *
 */
public class FileReplace {
	private static int count=0;
	
	/**
	 * 		
	 */
	public FileReplace() {
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)throws Exception{
		//File dir=new File("D:\\work\\jshop-20220816\\src\\main\\java");
		File dir=new File("D:\\work\\jshop-20220816\\src\\main\\UI\\js\\lib\\");
		//clearI18NTags(dir,".java");
		clearI18NTags(dir,".js");

		//String s="\r\n\t *@param session";
		//String alt="";

		//replace(dir, "a", "b", "java");
		//replace(dir, "a", "b", "jsp");
		//System.out.println("代码行数："+count);
		System.exit(0);
	}

	/**
	 *
	 * @param file
	 * @param src
	 * @param alt
	 * @param ext
	 */
	private static void replace(File file,String src,String alt,String ext) throws Exception {
		if(file.isDirectory()){
			File[] fs=file.listFiles();
			for(int i=0;i<fs.length;i++){
				replace(fs[i],src,alt,ext);
			}
		}else{
			if(!file.getName().endsWith(ext)) return;
			
			String s= JUtilInputStream.string(new FileInputStream(file), "UTF-8");
			System.out.println(file.getAbsolutePath());
			String[] temp = JUtilString.getTokens(s, "\n");

			count+=temp.length;
		}		
	}

	/**
	 *
	 * @param file
	 * @param ext
	 */
	private static void clearI18NTags(File file,String ext){
		if(file.isDirectory()){
			File[] fs=file.listFiles();
			for(int i=0;i<fs.length;i++){
				clearI18NTags(fs[i],ext);
			}
		}else{
			if(!file.getName().endsWith(ext)) return;

			String s=JDFSFile.read(file, "UTF-8");
			if(s.indexOf("I{")<0) return;

			s=I18N.clearI18NTags(s);

			JDFSFile.save(file.getAbsolutePath(), s, false, "UTF-8");

			System.out.println(file.getAbsolutePath());
		}
	}
}
