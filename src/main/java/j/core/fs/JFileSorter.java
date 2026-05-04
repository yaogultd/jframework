package j.core.fs;

import j.util.ConcurrentMap;
import j.util.JUtilBytes;
import j.util.JUtilInputStream;
import j.util.JUtilSorter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * 文件排序器
 * @author 肖炯
 *
 */
public class JFileSorter extends JUtilSorter{
	private static final long serialVersionUID=1L;
	
	public static final int ORDER_BY_TIME_MODIFIED=1;//按最后修改时间排序
	public static final int ORDER_BY_FILENAME=2;//按文件名排序
	public static final int ORDER_BY_LENGTH=3;//按文件大小排序
	
	private static ConcurrentMap<String, JFileSorter> instances=new ConcurrentMap<String, JFileSorter>();//排序类对象
	
	private int orderBy=2;//排序方式（默认为按名称排序）
	
	/**
	 * 
	 * @param orderBy
	 */
	private JFileSorter(int orderBy){
		if(orderBy!=ORDER_BY_TIME_MODIFIED
				&&orderBy!=ORDER_BY_FILENAME
				&&orderBy!=ORDER_BY_LENGTH){
			orderBy=ORDER_BY_FILENAME;
		}
		this.orderBy=orderBy;
	}
	
	/**
	 * 
	 * @param orderBy
	 * @return
	 */
	public static JFileSorter getInstance(int orderBy){
		String _orderBy=orderBy+"";
		if(instances.containsKey(_orderBy)) return (JFileSorter)instances.get(_orderBy);
		else{
			JFileSorter sorter=new JFileSorter(orderBy);
			instances.put(_orderBy,sorter);
			return sorter;
		}
	}

	@Override
	public String compare(Object pre,Object after){
		if(pre==null
				||after==null
				||!(pre instanceof File)
				||!(after instanceof File)){
			return JUtilSorter.EQUAL;
		}
		
		File _pre=(File)pre;
		File _after=(File)after;
		
		if(orderBy==ORDER_BY_TIME_MODIFIED){
			if(_pre.lastModified()<_after.lastModified()) return JUtilSorter.SMALLER;
			else if(_pre.lastModified()>_after.lastModified()) return JUtilSorter.BIGGER;
			else{
				if(_pre.getName().compareTo(_after.getName())<0) return JUtilSorter.SMALLER;
				else if(_pre.getName().compareTo(_after.getName())>0) return JUtilSorter.BIGGER;
			}
			
			return JUtilSorter.EQUAL;
		}else if(orderBy==ORDER_BY_FILENAME){
			if(_pre.getName().compareTo(_after.getName())<0) return JUtilSorter.SMALLER;
			else if(_pre.getName().compareTo(_after.getName())>0) return JUtilSorter.BIGGER;
			else{
				if(_pre.lastModified()<_after.lastModified()) return JUtilSorter.SMALLER;
				else if(_pre.lastModified()>_after.lastModified()) return JUtilSorter.BIGGER;
			}
			
			return JUtilSorter.EQUAL;
		}else if(orderBy==ORDER_BY_LENGTH){
			if(_pre.length()<_after.length())  return JUtilSorter.SMALLER;
			else if(_pre.length()>_after.length())  return JUtilSorter.BIGGER;
			else{
				if(_pre.getName().compareTo(_after.getName())<0) return JUtilSorter.SMALLER;
				else if(_pre.getName().compareTo(_after.getName())>0) return JUtilSorter.BIGGER;
				else{
					if(_pre.lastModified()<_after.lastModified()) return JUtilSorter.SMALLER;
					else if(_pre.lastModified()>_after.lastModified()) return JUtilSorter.BIGGER;
				}
			}
			
			return JUtilSorter.EQUAL;
		}
		
		return JUtilSorter.EQUAL;
	}

	public static void main(String[] args) throws Exception{
		FileInputStream fis=new FileInputStream("D:\\work\\temp\\x.txt");
		ByteArrayInputStream d=new ByteArrayInputStream(JUtilInputStream.bytes(fis));
		fis.close();
		System.out.println(d.markSupported());
		d.mark(0);
		d.read();
		d.reset();
		d.close();
	}
}
