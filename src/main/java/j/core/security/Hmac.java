package j.core.security;

import j.util.JUtilBytes;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Hmac{
	public static final String ALGORITHM_HMAC_MD5="HmacMD5";
	public static final String ALGORITHM_HMACSHA1="HmacSHA1";
	public static final String ALGORITHM_HMACSHA256="HmacSHA256";
	public static final String ALGORITHM_HMACSHA384="HmacSHA384";
	public static final String ALGORITHM_HMACSHA512="HmacSHA512";

	public static final String CHARSET_UTF8="UTF-8";

	/**
	 * 初始化HMAC密钥
	 * @param algorithm
	 * @return
	 * @throws Exception
	 */
	public static byte[] initHmacKey(String algorithm) throws Exception{
		KeyGenerator keyGenerator=KeyGenerator.getInstance(algorithm);// HmacMD5,HmacSHA1,HmacSHA256,HmacSHA384,HmacSHA512
		return keyGenerator.generateKey().getEncoded();
	}

	/**
	 * 使用Hmac生成的密钥对数据进行加密
	 * @param algorithm
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptHmac(String algorithm, byte[] data, byte[] key) throws Exception{
		SecretKeySpec secretKeySpec=new SecretKeySpec(key, algorithm);
		Mac mac=Mac.getInstance(algorithm);
		mac.init(secretKeySpec);
		return mac.doFinal(data);
	}

	/**
	 *
	 * @param digest
	 * @return
	 */
	public static String toString(byte[] digest){
		StringBuilder sb = new StringBuilder();
		for (byte b: digest) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	/**
	 *
	 * @param queries
	 * @return
	 */
	public static String composeStringToSign(Map<String,String> queries){
		String[] sortedKeys=queries.keySet().toArray(new String[0]);
		Arrays.sort(sortedKeys);
		StringBuilder canonicalizedQueryString=new StringBuilder();
		for(String key:sortedKeys){
			canonicalizedQueryString.append("&").append(percentEncode(key)).append("=").append(percentEncode(queries.get(key)));
		}

		StringBuilder stringToSign=new StringBuilder();
		stringToSign.append("GET");
		stringToSign.append("&");
		stringToSign.append(percentEncode("/"));
		stringToSign.append("&");
		if(canonicalizedQueryString.length()>0){
			stringToSign.append(percentEncode(canonicalizedQueryString.substring(1)));
		}
		return stringToSign.toString();
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	public static String percentEncode(String value){
		try{
			return value==null?null: URLEncoder.encode(value,CHARSET_UTF8)
					.replace("+","%20")
					.replace("*","%2A")
					.replace("%7E","~");
		}catch(Exception e){}
		return "";
	}

	/**
	 *
	 * @param queries
	 * @return
	 */
	public static String sortStringToSign(Map<String,String> queries){
		String[] sortedKeys=queries.keySet().toArray(new String[0]);
		if(!(queries instanceof  TreeMap)) Arrays.sort(sortedKeys);
		StringBuilder canonicalizedQueryString=new StringBuilder();
		for(String key:sortedKeys){
			canonicalizedQueryString.append("&").append(percentEncode(key)).append("=").append(percentEncode(queries.get(key)));
		}

		if(canonicalizedQueryString.length()>0) canonicalizedQueryString.deleteCharAt(0);
		return canonicalizedQueryString.toString();
	}

	/**
	 *
	 * @param queries
	 * @return
	 */
	public static String joinParamsToSign4Binance(Map<String,String> queries){
		String[] keys=queries.keySet().toArray(new String[0]);
		StringBuilder canonicalizedQueryString=new StringBuilder();
		for(String key : keys){
			canonicalizedQueryString.append("&").append(key).append("=").append(URLEncoder.encode(queries.get(key), StandardCharsets.UTF_8));
		}

		if(canonicalizedQueryString.length()>0) canonicalizedQueryString.deleteCharAt(0);
		return canonicalizedQueryString.toString();
	}

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)throws Exception{
		Map<String, String> params=new LinkedHashMap<>();
		params.put("symbol", "LTCBTC");
		params.put("side", "BUY");
		params.put("type", "LIMIT");
		params.put("timeInForce", "GTC");
		params.put("quantity", "1");
		params.put("price", "0.1");
		params.put("recvWindow", "5000");
		params.put("timestamp", "1499827319559");

		String queries = joinParamsToSign4Binance(params);

		System.out.println(queries);

		byte[] hmac = Hmac.encryptHmac(Hmac.ALGORITHM_HMACSHA256,
				queries.getBytes(),
				"NhqPtmdSJYdKjVHjA7PZj4Mge3R5YNiP1e3UZjInClVN65XAbvqqM6A7H5fATj0j".getBytes());

		String sign = Hex.encodeHexString(hmac);

		System.out.println(sign);
		System.out.println(JUtilBytes.byte2Hex(hmac));
	}
}
