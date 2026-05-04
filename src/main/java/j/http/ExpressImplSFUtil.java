package j.http;

import j.util.JUtilBase64;
import j.util.JUtilMD5;
import j.util.JUtilUUID;
import org.apache.commons.codec.binary.Base64;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class ExpressImplSFUtil {
	public static Map expressTypes=new HashMap();
	
	static{
		expressTypes.put("标准快递",new String[]{"1","C201","T4","B1"});
		expressTypes.put("顺丰标快",new String[]{"1","C201","T4","B1"});
		expressTypes.put("顺丰特惠",new String[]{"2","T6","T6","B1"});
		expressTypes.put("顺丰标快（陆运）",new String[]{"2","T6","T6","B1"});
		expressTypes.put("电商特惠",new String[]{"3","C201","T7","B1"});
		expressTypes.put("顺丰次晨",new String[]{"5","C201","T801","B1"});
		expressTypes.put("即日件",new String[]{"6","C201","T104","B1"});
		expressTypes.put("电商速配",new String[]{"7","C801","T4","B1"});
		expressTypes.put("顺丰宝平邮",new String[]{"9","C11","T13","B1"});
		expressTypes.put("顺丰宝挂号",new String[]{"10","C12","T14","B1"});
		expressTypes.put("医药常温",new String[]{"11","SP321","T4","B1"});
		expressTypes.put("医药温控",new String[]{"12","SP322","T4","B1"});
		expressTypes.put("物流普运",new String[]{"13","C3","T5","B2"});
		expressTypes.put("冷运到家",new String[]{"14","SP331","T15","B1"});
		expressTypes.put("生鲜速配",new String[]{"15","SP330","T4","B1"});
		expressTypes.put("大闸蟹专递",new String[]{"16","SP334","T4","B1"});
		expressTypes.put("汽配吉运",new String[]{"17","SP618","T12","B1"});
		expressTypes.put("重货快运",new String[]{"18","SP619","T4","B1"});
		expressTypes.put("国际特惠（试点）",new String[]{"19","C13","T7","B1"});
		expressTypes.put("行邮专列",new String[]{"20","C14","T17","B1"});
		expressTypes.put("医药专运（常温）",new String[]{"21","SP337","T16","B1"});
		expressTypes.put("国际特惠-文件",new String[]{"23","C101","T9","B1"});
		expressTypes.put("国际特惠-B类包裹",new String[]{"24","C223","T9","B1"});
		expressTypes.put("国际特惠-D类包裹",new String[]{"25","C224","T9","B1"});
		expressTypes.put("国际特惠（保税）",new String[]{"26","C15","T7","B1"});
		expressTypes.put("国际特惠（商家代理）",new String[]{"27","C17","T7","B1"});
		expressTypes.put("电商专配",new String[]{"28","C814","T7","B1"});
		expressTypes.put("俄罗斯电商专递",new String[]{"29","C19","T7","B1"});
		expressTypes.put("三号便利箱/袋",new String[]{"30","SP60303","T4","B1"});
		expressTypes.put("便利封/袋",new String[]{"31","SP601","T4","B1"});
		expressTypes.put("二号便利箱/袋",new String[]{"32","SP60302","T4","B1"});
		expressTypes.put("岛内件",new String[]{"33","C601","T4","B1"});
		expressTypes.put("即日2200",new String[]{"34","C201","T105","B1"});
		expressTypes.put("物资配送",new String[]{"35","SP60617","T23","B1"});
		expressTypes.put("汇票专送",new String[]{"36","SP617","T4","B1"});
		expressTypes.put("证照专递产品",new String[]{"110","SE0087","T4","B1"});
		expressTypes.put("顺丰空配",new String[]{"112","SP802","T64","B1"});
		expressTypes.put("专线普运",new String[]{"125","SE0091","T6","B2"});
		expressTypes.put("重货包裹",new String[]{"154","SP635","T6","B2"});
		expressTypes.put("小票零担",new String[]{"155","SP636","T6","B2"});
		expressTypes.put("医药常温（陆）",new String[]{"195","SP327","T6","B1"});
	}
	
	/**
	 * 
	 * @param typeName
	 * @return
	 */
	public static String[] getExpressType(String typeName){
		if(expressTypes.containsKey(typeName)) return (String[])expressTypes.get(typeName);
		else return (String[])expressTypes.get("顺丰标快");
	}
	
	/**
	 * 
	 * @param methodName
	 * @return
	 */
	public static String getPayMethod(String methodName){
		if("到付".equals(methodName)) return "2";
		else return "1";
	}

	/**
	 *
	 * @param reqURL
	 * @param msgData 业务报文
	 * @param checkWord 客户校验码
	 * @param timestamp 时间戳
	 * @return
	 * @throws Exception
	 */
	public static String call(String reqURL, String serviceCode, String partnerID, String checkWord, String timestamp, String msgData) throws Exception{
		//将业务报文+时间戳+校验码组合成需加密的字符串(注意顺序)
		String toVerifyText = msgData+timestamp+checkWord;

		//因业务报文中可能包含加号、空格等特殊字符，需要urlEnCode处理
		toVerifyText = URLEncoder.encode(toVerifyText,"UTF-8");

		//进行Md5加密
		byte[] md = JUtilMD5.MD5Encode(toVerifyText, "UTF-8");

		//通过BASE64生成数字签名
		String msgDigest = (new Base64()).encodeAsString(md);

    	JHttp http=JHttp.getInstance();
    	JHttpContext context=new JHttpContext();
		context.setRequestEncoding("UTF-8");
    	context.setContentType("application/x-www-form-urlencoded");

    	Map paras=new HashMap();
    	paras.put("serviceCode",serviceCode);
    	paras.put("partnerID",partnerID);
		paras.put("requestID", JUtilUUID.genUUIDShort());
		paras.put("timestamp",timestamp);
		paras.put("msgDigest",msgDigest);
		paras.put("msgData",msgData);

		return http.postResponse(context,null,reqURL,paras,"UTF-8");
    }

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		String s="{\"language\":\"zh-CN\",\"orderId\":\"QIAO-20200618-004\"}12312334453453fjcg5PGKaNpPSHFAZ4QsCOkV71R3zVci";

		s = URLEncoder.encode(s,"UTF-8");

		//进行Md5加密
		byte[] md = JUtilMD5.MD5Encode(s, "UTF-8");

		//通过BASE64生成数字签名
		String msgDigest = JUtilBase64.encode(md);
		System.out.println(msgDigest);
	}
}
