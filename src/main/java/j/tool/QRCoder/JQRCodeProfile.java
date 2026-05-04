package j.tool.QRCoder;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.util.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;

@ClassDescription(author = "肖炯",
		date = "2021/07/28",
		description = "二维码参数",
		reviewers = {"盖聂"})
@Getter
public class JQRCodeProfile {
	//生成二维码的图片格式-PNG
	public static final String IMG_FORMAT_PNG="PNG";

	//生成二维码的图片格式-JPG
	public static final String IMG_FORMAT_JPG="JPG";

	//默认图片格式
	public static final String DEFAULT_IMG_FORMAT = "PNG";

	//默认编码格式
	public static final String DEFAULT_CHARSET = "UTF-8";

	//默认二维码颜色
	public static final int DEFAULT_QRCODE_COLOR = 0xFF000000;

	//默认二维码背景色
	public static final int DEFAULT_QRCODE_BG = 0xFFFFFFFF;

	//默认二维码尺寸
	public static final int DEFAULT_QRCODE_SIZE = 300;

	//默认边距
	public static final int DEFAULT_MARGIN = 1;

	//默认LOGO宽度
	public static final int DEFAULT_LOGO_WIDTH = 60;

	//默认LOGO高度
	public static final int DEFAULT_LOGO_HEIGHT = 60;

	//LOGO是否需要压缩默认设置
	public static final Boolean DEFAULT_LOGO_COMPRESSED = true;

	//默认二维码配置实例
	public static JQRCodeProfile DEFAULT;

	//二维码配置实例
	private static ConcurrentMap<String, JQRCodeProfile> instances=new ConcurrentMap<>();

	private String imgFormat;
	private String charset;
	private int qrcodeColor;
	private int qrcodeBg;
	private int qrcodeSize;
	private int margin;
	private int logoWidth;
	private int logoHeight;
	private Boolean logoCompressed;

	static {
		DEFAULT=new JQRCodeProfile();
	}

	//setters
	public JQRCodeProfile setImgFormat(String imgFormat){
		this.imgFormat=imgFormat;
		return this;
	}

	public JQRCodeProfile setCharset(String charset){
		this.charset=charset;
		return this;
	}

	public JQRCodeProfile setQrcodeColor(int qrcodeColor){
		this.qrcodeColor=qrcodeColor;
		return this;
	}

	public JQRCodeProfile setQrcodeSize(int qrcodeSize){
		this.qrcodeSize=qrcodeSize;
		return this;
	}

	public JQRCodeProfile setMargin(int margin){
		this.margin=margin;
		return this;
	}

	public JQRCodeProfile setLogoWidth(int logoWidth){
		this.logoWidth=logoWidth;
		return this;
	}

	public JQRCodeProfile setLogoHeight(int logoHeight){
		this.logoHeight=logoHeight;
		return this;
	}

	public JQRCodeProfile setLogoCompressed(Boolean logoCompressed){
		this.logoCompressed=logoCompressed==null?Boolean.FALSE:logoCompressed;
		return this;
	}
	//setters end

	@MethodDescription(author = "肖炯",
			date = "2021/07/28",
			description = "使用默认配置")
	public JQRCodeProfile(){
		this.imgFormat=DEFAULT_IMG_FORMAT;
		this.charset=DEFAULT_CHARSET;
		this.qrcodeColor=DEFAULT_QRCODE_COLOR;
		this.qrcodeBg=DEFAULT_QRCODE_BG;
		this.qrcodeSize=DEFAULT_QRCODE_SIZE;
		this.logoWidth=DEFAULT_LOGO_WIDTH;
		this.logoHeight=DEFAULT_LOGO_HEIGHT;
		this.logoCompressed=DEFAULT_LOGO_COMPRESSED;
		this.margin=DEFAULT_MARGIN;
	}

	@MethodDescription(author = "肖炯",
			date = "2021/07/28",
			description = "自定义二维码配置")
	public JQRCodeProfile(String imgFormat,
						  String charset,
						  int qrcodeColor,
						  int qrcodeBg,
						  int qrcodeSize,
						  int margin,
						  int logoWidth,
						  int logoHeight,
						  Boolean logoCompressed){
		if(!IMG_FORMAT_JPG.equals(imgFormat) && !IMG_FORMAT_PNG.equals(imgFormat)) imgFormat=DEFAULT_IMG_FORMAT;
		if(charset==null || "".equals(charset)) charset=DEFAULT_CHARSET;
		if(qrcodeColor<0) qrcodeColor=DEFAULT_QRCODE_COLOR;
		if(qrcodeBg<0) qrcodeBg=DEFAULT_QRCODE_BG;
		if(qrcodeSize<=0) qrcodeSize=DEFAULT_QRCODE_SIZE;
		if(margin<0) margin=DEFAULT_MARGIN;
		if(logoWidth<=0) logoHeight=DEFAULT_LOGO_WIDTH;
		if(logoHeight<=0) logoHeight=DEFAULT_LOGO_HEIGHT;

		this.imgFormat=imgFormat;
		this.charset=charset;
		this.qrcodeColor=qrcodeColor;
		this.qrcodeBg=qrcodeBg;
		this.qrcodeSize=qrcodeSize;
		this.margin=margin;
		this.logoWidth=logoWidth;
		this.logoHeight=logoHeight;
		this.logoCompressed=logoCompressed;
	}

	@MethodDescription(author = "肖炯", date = "2021/07/28", description = "获取默认二维码配置实例")
	public static JQRCodeProfile getDefault(){
		return DEFAULT;
	}

	@MethodDescription(author = "肖炯", date = "2021/07/28", description = "获取二维码配置实例，如果存在相同配置的实例，则不重复创建")
	public static JQRCodeProfile getInstance(String imgFormat,
											String charset,
											int qrcodeColor,
											int qrcodeBg,
											int qrcodeSize,
											int margin,
											int logoWidth,
											int logoHeight,
											Boolean logoCompressed){
		if(!IMG_FORMAT_JPG.equals(imgFormat) && !IMG_FORMAT_PNG.equals(imgFormat)) imgFormat=DEFAULT_IMG_FORMAT;
		if(charset==null || "".equals(charset)) charset=DEFAULT_CHARSET;
		if(qrcodeColor<0) qrcodeColor=DEFAULT_QRCODE_COLOR;
		if(qrcodeBg<0) qrcodeBg=DEFAULT_QRCODE_BG;
		if(qrcodeSize<=0) qrcodeSize=DEFAULT_QRCODE_SIZE;
		if(margin<0) margin=DEFAULT_MARGIN;
		if(logoWidth<=0) logoHeight=DEFAULT_LOGO_WIDTH;
		if(logoHeight<=0) logoHeight=DEFAULT_LOGO_HEIGHT;

		String settings=imgFormat+"->"+charset+"->"+qrcodeColor+"->"+qrcodeBg+"->"+qrcodeSize+"->"+margin+"->"+logoHeight+"->"+logoHeight+"->"+logoCompressed;
		if(instances.containsKey(settings)) return instances.get(settings);

		JQRCodeProfile profile=new JQRCodeProfile(imgFormat,
				charset,
				qrcodeColor,
				qrcodeBg,
				qrcodeSize,
				margin,
				logoWidth,
				logoHeight,
				logoCompressed);
		instances.put(settings, profile);
		return profile;
	}

	@Override
	public String toString(){
		return imgFormat+"->"+charset+"->"+qrcodeColor+"->"+qrcodeBg+"->"+qrcodeSize+"->"+margin+"->"+logoHeight+"->"+logoHeight+"->"+logoCompressed;
	}
}