package j.core.security;

import j.util.JUtilBase64;
import j.util.JUtilString;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AES {
    // 编码
    private static final String ENCODING = "UTF-8";
    
    //算法
    private static final String ALGORITHM = "AES";
    
    // 默认的加密算法
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    
    //默认KEY和OFFSET
    private static final String KEY_DEFAULT="YDCk6EXK7i9v3ZeN";
    private static final String OFFSET_DEFAULT="ZGp5IfuxKDyDGyPI";
    
    /**
     * 
     * @param data
     * @param key
     * @param iv
     * @return
     */
    public static String encrypt(String data, String key, String iv) {
    	return encrypt(data, key, iv, null);
    }

	/**
	 *
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 */
	public static String encrypt(String data, String key, String iv, String algorithm) {
		try {
			if(key==null)  key=KEY_DEFAULT;
			if(iv==null) iv=OFFSET_DEFAULT;

			if(JUtilString.isBlank(algorithm)) algorithm=CIPHER_ALGORITHM;

			Cipher cipher = Cipher.getInstance(algorithm);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ASCII"), ALGORITHM);
			IvParameterSpec ivParameterSpec = JUtilString.isBlank(iv) ? null : new IvParameterSpec(iv.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] encrypted = cipher.doFinal(data.getBytes(ENCODING));

			String s=Base64.getMimeEncoder().encodeToString(encrypted);//此处使用BASE64做转码。
			s = s.replaceAll("[\\s*\t\n\r]", "");
			return s;
		}catch(Exception e) {
			return data;
		}
	}
    
    /**
     * 
     * @param data
     * @param key
     * @param iv
     * @return
     */
    public static String decrypt(String data, String key, String iv) {
    	return decrypt(data, key, iv, null);
    }

	/**
	 *
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 */
	public static String decrypt(String data, String key, String iv, String algorithm) {
		try {
			if(key==null)  key=KEY_DEFAULT;
			if(iv==null) iv=OFFSET_DEFAULT;

			if(JUtilString.isBlank(algorithm)) algorithm=CIPHER_ALGORITHM;

			Cipher cipher = Cipher.getInstance(algorithm);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ASCII"), ALGORITHM);
			IvParameterSpec ivParameterSpec = JUtilString.isBlank(iv) ? null : new IvParameterSpec(iv.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] buffer = Base64.getMimeDecoder().decode(data);
			byte[] encrypted = cipher.doFinal(buffer);
			return new String(encrypted, ENCODING);//此处使用BASE64做转码。
		}catch(Exception e) {
			e.printStackTrace();
			return data;
		}
	}



	/**
	 * 微信支付通知数据解密
	 * @param ciphertext
	 * @param key
	 * @param nonce
	 * @param associatedData
	 * @return
	 */
	public static final int WECHAT_KEY_LENGTH_BYTE = 32;
	public static final int WECHAT_TAG_LENGTH_BIT = 128;
    public static String decrypt4Wechat(String ciphertext, String key, String nonce, String associatedData) {
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
			GCMParameterSpec spec = new GCMParameterSpec(WECHAT_TAG_LENGTH_BIT, nonce.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, spec);
			cipher.updateAAD(associatedData.getBytes());

			return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), "utf-8");
		}catch(Exception e) {
			//e.printStackTrace();
			return ciphertext;
		}
	}

	/**
	 * 微信小程序数据解密
	 * @param data
	 * @param key
	 * @param iv
	 * @return
	 */
	public static String decrypt4WechatMiniProgram(String data, String key, String iv) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			SecretKeySpec skeySpec = new SecretKeySpec(JUtilBase64.decode(key, true), ALGORITHM);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(JUtilBase64.decode(iv, true));//使用CBC模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] buffer = Base64.getMimeDecoder().decode(data);
			byte[] encrypted = cipher.doFinal(buffer);
			return new String(encrypted, ENCODING);//此处使用BASE64做转码。
		}catch(Exception e) {
			//e.printStackTrace();
			return data;
		}
	}

	/**
	 * 支付宝小程序数据解密
	 * @param content
	 * @param key
	 * @param charset
	 * @return
	 */
	public static String decrypt4AlipayMiniProgram(String content, String key, String charset){
    	try {
	        //反序列化AES密钥
	        SecretKeySpec keySpec = new SecretKeySpec(Base64.getMimeDecoder().decode(key.getBytes()), "AES");
	         
	        //128bit全零的IV向量
	        byte[] iv = new byte[16];
	        for (int i = 0; i < iv.length; i++) {
	            iv[i] = 0;
	        }
	        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
	         
	        //初始化加密器并加密
	        Cipher deCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        deCipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
	        byte[] encryptedBytes = Base64.getMimeDecoder().decode(content.getBytes());
	        byte[] bytes = deCipher.doFinal(encryptedBytes);
	        return new String(bytes);
    	}catch(Exception e) {
    		//e.fillInStackTrace();
    		return content;
    	}
         
    }
}
