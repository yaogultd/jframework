package j.tool.QRCoder;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import j.core.annotation.description.ClassDescription;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

@ClassDescription(author = "肖炯",
		date = "2021/07/28",
		description = "二维码处理",
		reviewers = {"盖聂"})
public class JQRCode {
	/**
	 * 创建二维码图片
	 * @param profile 格式设置
	 * @param content 二维码内容
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @return
	 * @throws Exception
	 */
	private static BufferedImage createImage(JQRCodeProfile profile, String content, Object logo) throws Exception {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, profile.getCharset());
		hints.put(EncodeHintType.MARGIN, profile.getMargin());

		BitMatrix bitMatrix = new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE, profile.getQrcodeSize(), profile.getQrcodeSize(), hints);

		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? profile.getQrcodeColor() : profile.getQrcodeBg());
			}
		}

		// 插入LOGO
		inertLogo(profile, image, logo);

		return image;
	}

	/**
	 * 插入LOGO
	 *
	 * @param parent 所属二维码图片
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	private static void inertLogo(JQRCodeProfile profile, BufferedImage parent, Object logo) throws Exception {
		if(logo==null) return;

		if((logo instanceof String) && "".equals(logo)) return;

		Image src = null;

		if(logo instanceof String){
			File file=new File((String)logo);
			if (!file.exists())  return;

			src=ImageIO.read(file);
		}else if(logo instanceof InputStream){
			src=ImageIO.read((InputStream) logo);
		}else if(logo instanceof Image){
			src=(Image)logo;
		}

		if(src==null) return;

		int width = src.getWidth(null);
		int height = src.getHeight(null);

		if (profile.getLogoCompressed()) { // 压缩LOGO
			if (width > profile.getLogoWidth()) {
				width = profile.getLogoWidth();
			}
			if (height > profile.getLogoHeight()) {
				height = profile.getLogoHeight();
			}
			Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			src = image;
		}

		//插入LOGO
		Graphics2D graph = parent.createGraphics();
		int x = (profile.getQrcodeSize() - width) / 2;
		int y = (profile.getQrcodeSize() - height) / 2;
		graph.drawImage(src, x, y, width, height, null);

		Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}

	////////////////////////图片输出到文件///////////////////////
	/**
	 * 生成二维码图片（默认配置）
	 * @param content 存储内容
	 * @param outPath 图片路径
	 * @throws Exception
	 */
	public static void encode(String content, String outPath) throws Exception{
		encode(JQRCodeProfile.getDefault(), content, outPath);
	}

	/**
	 * 生成二维码图片（默认配置）
	 * @param profile 二维码配置
	 * @param content 存储内容
	 * @param outPath 图片路径
	 * @throws Exception
	 */
	public static void encode(JQRCodeProfile profile, String content, String outPath) throws Exception{
		encode(profile, content, outPath ,null);
	}


	/**
	 * 生成带logo的二维码图片（默认配置）
	 * @param content 存储内容
	 * @param outPath 图片路径
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	public static void encode(String content, String outPath, Object logo) throws Exception{
		encode(JQRCodeProfile.getDefault(), content, outPath, logo);
	}

	/**
	 * 生成二维码图片（默认配置）
	 * @param profile 二维码配置
	 * @param content 存储内容
	 * @param outPath 图片路径
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	public static void encode(JQRCodeProfile profile, String content, String outPath, Object logo) throws Exception{
		BufferedImage img = createImage(profile, content, logo);

		File imgFile = new File(outPath);
		imgFile.mkdirs();

		// 生成二维码QRCode图片
		ImageIO.write(img, profile.getImgFormat(), imgFile);
	}

	////////////////////////图片输出到输出流///////////////////////
	/**
	 * 生成二维码图片（默认配置）
	 * @param content 存储内容
	 * @param out 输出流
	 * @throws Exception
	 */
	public static void encode(String content, OutputStream out) throws Exception{
		encode(JQRCodeProfile.getDefault(), content, out);
	}

	/**
	 * 生成二维码图片（默认配置）
	 * @param profile 二维码配置
	 * @param content 存储内容
	 * @param out 输出流
	 * @throws Exception
	 */
	public static void encode(JQRCodeProfile profile, String content, OutputStream out) throws Exception{
		encode(profile, content, out ,null);
	}


	/**
	 * 生成带logo的二维码图片（默认配置）
	 * @param content 存储内容
	 * @param out 输出流
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	public static void encode(String content, OutputStream out, Object logo) throws Exception{
		encode(JQRCodeProfile.getDefault(), content, out, logo);
	}

	/**
	 * 生成二维码图片（默认配置）
	 * @param profile 二维码配置
	 * @param content 存储内容
	 * @param out 输出流
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	public static void encode(JQRCodeProfile profile, String content, OutputStream out, Object logo) throws Exception{
		BufferedImage img = createImage(profile, content, logo);

		// 生成二维码QRCode图片
		ImageIO.write(img, profile.getImgFormat(), out);

		out.flush();
		out.close();
	}

	////////////////////////返回图片对象///////////////////////
	/**
	 * 生成二维码图片（默认配置）
	 * @param content 存储内容
	 * @throws Exception
	 */
	public static BufferedImage encodeImage(String content) throws Exception{
		return encodeImage(JQRCodeProfile.getDefault(), content);
	}

	/**
	 * 生成二维码图片（默认配置）
	 * @param profile 二维码配置
	 * @param content 存储内容
	 * @throws Exception
	 */
	public static BufferedImage encodeImage(JQRCodeProfile profile, String content) throws Exception{
		return encodeImage(profile, content ,null);
	}


	/**
	 * 生成带logo的二维码图片（默认配置）
	 * @param content 存储内容
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	public static BufferedImage encodeImage(String content, Object logo) throws Exception{
		return encodeImage(JQRCodeProfile.getDefault(), content, logo);
	}

	/**
	 * 生成二维码图片（默认配置）
	 * @param profile 二维码配置
	 * @param content 存储内容
	 * @param logo logo来源（文件路径、输入流、Image对象）
	 * @throws Exception
	 */
	public static BufferedImage encodeImage(JQRCodeProfile profile, String content, Object logo) throws Exception{
		return createImage(profile, content, logo);
	}


	/**
	 * 解析二维码
	 * @param input 二维码输入流
	 * @return
	 * @throws Exception
	 */
	public static String decode(InputStream input) throws Exception {
		BufferedImage image;
		image = ImageIO.read(input);
		if (image == null) return null;

		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
		hints.put(DecodeHintType.CHARACTER_SET, JQRCodeProfile.DEFAULT_CHARSET);
		return new MultiFormatReader().decode(bitmap, hints).getText();
	}


	/**
	 * 解析二维码
	 * @param file 二维码图片文件
	 * @return
	 * @throws Exception
	 */
	public static String decode(File file) throws Exception {
		return decode(new FileInputStream(file));
	}

	/**
	 * 解析二维码
	 * @param path 二维码图片文件路径
	 * @return
	 * @throws Exception
	 */
	public static String decode(String path) throws Exception {
		return decode(new File(path));
	}


	/**
	 * 解析二维码
	 * @param input 二维码输入流
	 * @param charset 字符编码
	 * @return
	 * @throws Exception
	 */
	public static String decode(InputStream input, String charset) throws Exception {
		BufferedImage image;
		image = ImageIO.read(input);
		if (image == null) return null;

		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
		hints.put(DecodeHintType.CHARACTER_SET, charset);
		return new MultiFormatReader().decode(bitmap, hints).getText();
	}


	/**
	 * 解析二维码
	 * @param file 二维码文件
	 * @param charset 字符编码
	 * @return
	 * @throws Exception
	 */
	public static String decode(File file, String charset) throws Exception {
		return decode(new FileInputStream(file), charset);
	}

	/**
	 * 解析二维码
	 * @param path 二维码文件路径
	 * @param charset 字符编码
	 * @return
	 * @throws Exception
	 */
	public static String decode(String path, String charset) throws Exception {
		return decode(new File(path), charset);
	}

	/**
	 * 测试
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("begin...");

		JQRCode.encode(JQRCodeProfile.getInstance(JQRCodeProfile.IMG_FORMAT_PNG,
				null,
				0x00000000,
				0xFFFFFFFF,
				300,
				1,
				50,
				50,
				Boolean.TRUE),
				"https://www.instraw.store",
				"d:/temp/instraw.store.png",
				"D:\\耀古\\运营\\VI\\INSTRAW\\INSTRAW.png");

		//F:\temp
		System.out.println("decode -> "+decode("d:/temp/instraw.store.png"));

		System.exit(0);
	}
}