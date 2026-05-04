package j.core.common;

import j.util.JUtilString;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author 肖炯
 *
 * 2019年4月7日
 *
 * <b>功能描述</b>
 */
public final class JArray {

	/**
	 * 截取数组的一部分
	 * @param objects
	 * @param sub
	 * @param from
	 * @param to
	 * @return
	 */
	public static Object[] subArray(Object[] objects,Object[] sub,int from,int to) {
		if(objects==null) return null;
		if(from<0||from>to) return null;
		for(int i=from;i<to&&i<objects.length;i++) {
			sub[i-from]=objects[i];
		}
		return sub;
	}

	/**
	 *
	 * @param all
	 * @param from
	 * @param to
	 * @return
	 */
	public static byte[] sub(byte[] all,int from,int to) {
		if(all==null) return null;
		if(from<0||from>to) return null;
		if(from==to) return new byte[0];
		if(to>all.length) to=all.length;
		if(from==0 && to==all.length) return all;

		return Arrays.copyOfRange(all, from, to);
	}

	/**
	 *
	 * @param all
	 * @param prefix
	 * @return
	 */
	public static boolean startsWith(byte[] all, byte[] prefix) {
		if(all==null || prefix==null) return false;
		if(prefix.length > all.length) return false;

		for(int i=0; i<prefix.length; i++){
			if(prefix[i] != all[i]) return false;
		}

		return true;
	}

	/**
	 *
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static byte[] append(byte[] array1, byte[] array2) {
		if(array1==null && array2==null) return new byte[]{};
		if(array2==null) return array1;
		if(array1==null) return array2;

		if(array2.length==0) return array1;
		if(array1.length==0) return array2;

		byte[] combined = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, combined, 0, array1.length);
		System.arraycopy(array2, 0, combined, array1.length, array2.length);
		return combined;
	}

	/**
	 *
	 * @param blocks
	 * @return
	 */
	public static byte[] append(List<byte[]> blocks){
		int size=0;
		for(int i=0; i<blocks.size(); i++){
			if(blocks.get(i)==null) continue;
			size+=blocks.get(i).length;
		}

		byte[] bytes=new byte[size];

		int index=0;
		for(int i=0; i<blocks.size(); i++){
			if(blocks.get(i)==null || blocks.get(i).length==0) continue;

			byte[] block = blocks.get(i);
			System.arraycopy(block, 0, bytes, index, block.length);
			index+=block.length;
		}

		return  bytes;
	}

	/**
	 *
	 * @param all
	 * @param from
	 * @param to
	 * @return
	 */
	public static int[] sub(int[] all,int from,int to) {
		if(all==null) return null;
		if(from<0||from>to) return null;
		if(from==to) return new int[0];
		if(to>all.length) to=all.length;
		if(from==0 && to==all.length) return all;

		return Arrays.copyOfRange(all, from, to);
	}

	/**
	 *
	 * @param all
	 * @param from
	 * @param to
	 * @return
	 */
	public static long[] sub(long[] all,int from,int to) {
		if(all==null) return null;
		if(from<0||from>to) return null;
		if(from==to) return new long[0];
		if(to>all.length) to=all.length;
		if(from==0 && to==all.length) return all;

		return Arrays.copyOfRange(all, from, to);
	}

	/**
	 *
	 * @param all
	 * @param from
	 * @param to
	 * @return
	 */
	public static double[] sub(double[] all,int from,int to) {
		if(all==null) return null;
		if(from<0||from>to) return null;
		if(from==to) return new double[0];
		if(to>all.length) to=all.length;
		if(from==0 && to==all.length) return all;

		return Arrays.copyOfRange(all, from, to);
	}

	/**
	 *
	 * @param array
	 * @return
	 */
	public static boolean duplicatedElements(Object[] array) {
		if(array==null||array.length==0) return false;

		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array.length; j++) {
				if(i==j) continue;

				if(array[i]==null && array[j]==null) return true;
				else if(array[i]!=null && array[j]!=null && array[i].equals(array[j])) return true;
			}
		}

		return false;
	}

	/**
	 *
	 * @param objects
	 * @return
	 */
	public static String toString(List objects) {
		return toString(objects, null);
	}

	/**
	 *
	 * @param objects
	 * @param splitter
	 * @return
	 */
	public static String toString(List objects, String splitter) {
		return toString(objects, splitter, false);
	}

	/**
	 *
	 * @param objects
	 * @param splitter
	 * @param quote
	 * @return
	 */
	public static String toString(List objects, String splitter, boolean quote) {
		if(objects==null) return null;
		if(objects.isEmpty()) return "";

		StringBuffer sb=new StringBuffer();

		for(int i=0; i<objects.size(); i++) {
			if(splitter!=null&&i>0) sb.append(splitter);
			if(!quote) sb.append(objects.get(i)==null?"null":objects.get(i).toString());
			else sb.append(objects.get(i)==null?"null":("\""+objects.get(i).toString()+"\""));
		}

		return sb.toString();
	}

	/**
	 *
	 * @param objects
	 * @return
	 */
	public static String toString(Object[] objects) {
		return toString(objects, null);
	}

	/**
	 *
	 * @param objects
	 * @param splitter
	 * @return
	 */
	public static String toString(Object[] objects, String splitter) {
		return toString(objects, splitter, false);
	}

	/**
	 *
	 * @param objects
	 * @param splitter
	 * @param quote
	 * @return
	 */
	public static String toString(Object[] objects, String splitter, boolean quote) {
		return toString(objects, splitter, quote?"\"":null);
	}

	/**
	 *
	 * @param objects
	 * @param splitter
	 * @param quote
	 * @return
	 */
	public static String toString(Object[] objects, String splitter, String quote) {
		if(objects==null) return null;
		if(objects.length==0) return "";

		StringBuffer sb=new StringBuffer();

		for(int i=0; i<objects.length; i++) {
			if(splitter!=null&&i>0) sb.append(splitter);
			if(JUtilString.isBlank(quote)) sb.append(objects[i]==null?"null":objects[i].toString());
			else sb.append(objects[i]==null?"null":(quote+objects[i].toString()+quote));
		}

		return sb.toString();
	}

	/**
	 *
	 * @param objects
	 * @param splitter
	 * @param quote
	 * @return
	 */
	public static String toString(List objects, String splitter, String quote) {
		if(objects==null) return null;
		if(objects.size()==0) return "";

		StringBuffer sb=new StringBuffer();

		for(int i=0; i<objects.size(); i++) {
			if(splitter!=null&&i>0) sb.append(splitter);
			if(JUtilString.isBlank(quote)) sb.append(objects.get(i)==null?"null":objects.get(i).toString());
			else sb.append(objects.get(i)==null?"null":(quote+objects.get(i).toString()+quote));
		}

		return sb.toString();
	}
}
