package j.tool.BarCode;

import j.core.fs.JDFSFile;
import j.log.Logger;
import j.tool.BarCode.encode.Code128Encoder;
import j.tool.BarCode.encode.EAN13Encoder;
import j.tool.BarCode.encode.EAN8Encoder;
import j.tool.BarCode.paint.BaseLineTextPainter;
import j.tool.BarCode.paint.EAN13TextPainter;
import j.tool.BarCode.paint.EAN8TextPainter;
import j.tool.BarCode.paint.WidthCodedPainter;
import j.tool.BarCode.util.ImageUtil;
import jakarta.servlet.http.HttpServletResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/** 
* 支持EAN13, EAN8, UPCA, UPCE, Code 3 of 9, Codabar, Code 11, Code 93, Code 128, MSI/Plessey, Interleaved 2 of PostNet等
* 利用jbarcode生成各种条形码！测试成功！分享给大家！
*/ 
public class JBarCode { 
	private static Logger log=Logger.create(JBarCode.class);
	public static final String IMAGE_JPEG="jpeg";
	public static final String IMAGE_GIF="gif";
	public static final String IMAGE_PNG="png";
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 */
	public static BufferedImage createCode128(String num,String imageType) { 
		try { 
			Barcode code = new Barcode(Code128Encoder.getInstance(),WidthCodedPainter.getInstance(),BaseLineTextPainter.getInstance()); 
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		} 
	} 

	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param showText
	 */
	public static BufferedImage createCode128(String num,String imageType,boolean showText) { 
		try { 
			Barcode code = new Barcode(Code128Encoder.getInstance(), WidthCodedPainter.getInstance(), BaseLineTextPainter.getInstance());
			code.setShowText(showText);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param xDimension
	 */
	public static BufferedImage createCode128(String num,String imageType,double xDimension) { 
		try { 
			Barcode code = new Barcode(Code128Encoder.getInstance(),WidthCodedPainter.getInstance(),BaseLineTextPainter.getInstance()); 
			code.setXDimension(xDimension);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static BufferedImage createCode128(String num,String imageType,double xDimension,boolean showText) { 
		try { 
			Barcode code = new Barcode(Code128Encoder.getInstance(),WidthCodedPainter.getInstance(),BaseLineTextPainter.getInstance()); 
			code.setXDimension(xDimension);
			code.setShowText(showText);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 */
	public static BufferedImage createCodeEAN13(String num,String imageType) { 
		try { 
			Barcode code = new Barcode(EAN13Encoder.getInstance(),WidthCodedPainter.getInstance(), EAN13TextPainter.getInstance());
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param showText
	 */
	public static BufferedImage createCodeEAN13(String num,String imageType,boolean showText) { 
		try { 
			Barcode code = new Barcode(EAN13Encoder.getInstance(),WidthCodedPainter.getInstance(),EAN13TextPainter.getInstance()); 
			code.setShowText(showText);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param xDimension
	 */
	public static BufferedImage createCodeEAN13(String num,String imageType,double xDimension) { 
		try { 
			Barcode code = new Barcode(EAN13Encoder.getInstance(),WidthCodedPainter.getInstance(),EAN13TextPainter.getInstance()); 
			code.setXDimension(xDimension);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static BufferedImage createCodeEAN13(String num,String imageType,double xDimension,boolean showText) { 
		try { 
			Barcode code = new Barcode(EAN13Encoder.getInstance(),WidthCodedPainter.getInstance(),EAN13TextPainter.getInstance()); 
			code.setXDimension(xDimension);
			code.setShowText(showText);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 */
	public static BufferedImage createCodeEAN8(String num,String imageType) { 
		try { 
			Barcode code = new Barcode(EAN8Encoder.getInstance(),WidthCodedPainter.getInstance(), EAN8TextPainter.getInstance());
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param showText
	 */
	public static BufferedImage createCodeEAN8(String num,String imageType,boolean showText) { 
		try { 
			Barcode code = new Barcode(EAN8Encoder.getInstance(),WidthCodedPainter.getInstance(),EAN8TextPainter.getInstance()); 
			code.setShowText(showText);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param xDimension
	 */
	public static BufferedImage createCodeEAN8(String num,String imageType,double xDimension) { 
		try { 
			Barcode code = new Barcode(EAN8Encoder.getInstance(),WidthCodedPainter.getInstance(),EAN8TextPainter.getInstance()); 
			code.setXDimension(xDimension);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static BufferedImage createCodeEAN8(String num,String imageType,double xDimension,boolean showText) { 
		try { 
			Barcode code = new Barcode(EAN8Encoder.getInstance(),WidthCodedPainter.getInstance(),EAN8TextPainter.getInstance()); 
			code.setXDimension(xDimension);
			code.setShowText(showText);
			BufferedImage image = code.createBarcode(num); 
			
			return image;
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
			return null;
		}  
	} 

	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 */
	public static void createCode128(String num,String imagePath,String imageType) { 
		try { 
			BufferedImage image = createCode128(num, imageType); 
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 

	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param showText
	 */
	public static void createCode128(String num,String imagePath,String imageType,boolean showText) { 
		try { 
			BufferedImage image = createCode128(num,imageType,showText);
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param xDimension
	 */
	public static void createCode128(String num,String imagePath,String imageType,double xDimension) { 
		try { 
			BufferedImage image = createCode128(num,imageType,xDimension);
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static void createCode128(String num,String imagePath,String imageType,double xDimension,boolean showText) { 
		try { 
			BufferedImage image = createCode128(num,imageType,xDimension,showText);
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 */
	public static void createCodeEAN13(String num,String imagePath,String imageType) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param showText
	 */
	public static void createCodeEAN13(String num,String imagePath,String imageType,boolean showText) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType,showText); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param xDimension
	 */
	public static void createCodeEAN13(String num,String imagePath,String imageType,double xDimension) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType,xDimension); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static void createCodeEAN13(String num,String imagePath,String imageType,double xDimension,boolean showText) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType,xDimension,showText); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 */
	public static void createCodeEAN8(String num,String imagePath,String imageType) { 
		try { 
			BufferedImage image = createCodeEAN8(num, imageType); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param showText
	 */
	public static void createCodeEAN8(String num,String imagePath,String imageType,boolean showText) { 
		try { 
			BufferedImage image = createCodeEAN8(num,imageType,showText); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param imagePath
	 * @param imageType
	 * @param xDimension
	 */
	public static void createCodeEAN8(String num,String imagePath,String imageType,double xDimension) { 
		try { 
			BufferedImage image = createCodeEAN8(num,imageType,xDimension); 
			
			saveToFile(image, imagePath,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 */
	public static void createCode128(String num,HttpServletResponse response,String imageType) { 
		try { 
			BufferedImage image = createCode128(num, imageType); 
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 

	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param showText
	 */
	public static void createCode128(String num,HttpServletResponse response,String imageType,boolean showText) { 
		try { 
			BufferedImage image = createCode128(num,imageType,showText);
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param xDimension
	 */
	public static void createCode128(String num,HttpServletResponse response,String imageType,double xDimension) { 
		try { 
			BufferedImage image = createCode128(num,imageType,xDimension);
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static void createCode128(String num,HttpServletResponse response,String imageType,double xDimension,boolean showText) { 
		try { 
			BufferedImage image = createCode128(num,imageType,xDimension,showText);
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 */
	public static void createCodeEAN13(String num,HttpServletResponse response,String imageType) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param showText
	 */
	public static void createCodeEAN13(String num,HttpServletResponse response,String imageType,boolean showText) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType,showText); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param xDimension
	 */
	public static void createCodeEAN13(String num,HttpServletResponse response,String imageType,double xDimension) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType,xDimension); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param xDimension
	 * @param showText
	 */
	public static void createCodeEAN13(String num,HttpServletResponse response,String imageType,double xDimension,boolean showText) { 
		try { 
			BufferedImage image = createCodeEAN13(num,imageType,xDimension,showText); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 */
	public static void createCodeEAN8(String num,HttpServletResponse response,String imageType) { 
		try { 
			BufferedImage image = createCodeEAN8(num, imageType); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param showText
	 */
	public static void createCodeEAN8(String num,HttpServletResponse response,String imageType,boolean showText) { 
		try { 
			BufferedImage image = createCodeEAN8(num,imageType,showText); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	} 
	
	/**
	 * 
	 * @param num
	 * @param response
	 * @param imageType
	 * @param xDimension
	 */
	public static void createCodeEAN8(String num,HttpServletResponse response,String imageType,double xDimension) { 
		try { 
			BufferedImage image = createCodeEAN8(num,imageType,xDimension); 
			
			writeImage(image, response,imageType);
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		}  
	}
	

	/**
	 * 
	 * @param image
	 * @param imagePath
	 * @param imageType
	 */
	private static void saveToFile(BufferedImage image, String imagePath, String imageType) { 
		try { 
			FileOutputStream fos = new FileOutputStream(imagePath); 
			ImageUtil.encodeAndWrite(image, imageType, fos, ImageUtil.DEFAULT_DPI, ImageUtil.DEFAULT_DPI);
			fos.close(); 
		}catch (Exception e) { 
			log.log(e, Logger.LEVEL_ERROR);
		} 
	} 
	
	/**
	 * 
	 * @param image
	 * @param response
	 */
	private static void writeImage(BufferedImage image,HttpServletResponse response,String imageType) {
		try{
			//输出图象
			OutputStream os= response.getOutputStream();
			ImageIO.write(image,imageType.toUpperCase(),os);
			os.flush();
			os.close();
		}catch(Exception e){
			log.log(e,Logger.LEVEL_ERROR);
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
//		8（保留位）400031（重庆邮编）04（款式）1（1/2/3 平角、加长、三角）1（1/2/3/4  白色/黑色/灰色/红色...）1（1/2/3/4.....L/XL/XXL/XXXL）

//		String[] models=new String[]{"011","012","013","021","022","023","031","032","033"};
//		String[] modelNames=new String[]{"01B","01C","01D","02B","02C","02D","03B","03C","03D"};
		String[] models=new String[]{"013","023","033"};
		String[] modelNames=new String[]{"01D","02D","03D"};
		String[] colors=new String[]{"1","2","3"};
		String[] colorNames=new String[]{"W","B","G"};
		String[] sizes=new String[]{"1","2","3","4"};
		String[] sizeNames=new String[]{"L","XL","2XL","3XL"};
		String[] sizeNamesOfAI=new String[]{"L","XL","XXL","XXXL"};

//		for(int i=0; i<models.length; i++){
//			for(int j=0; j<colors.length; j++){
//				for(int k=0; k<sizes.length; k++){
//					String sn="8400031"+models[i]+colors[j]+sizes[k];
//					String name=modelNames[i]+"_"+colorNames[j]+"_"+sizeNames[k];
//
//					createCodeEAN13(sn,"D:\\耀古\\内裤\\生产\\物料\\型号标签\\PRO\\"+name+".png",IMAGE_PNG,0.8,true);
//				}
//			}
//		}


		for(int j=0; j<colors.length; j++){
			for(int k=0; k<sizes.length; k++){
				File _01D=new File("D:\\耀古\\内裤\\生产\\物料\\型号标签\\PRO\\01D_B_"+sizeNamesOfAI[k]+".ai");

				for(int ii=0; ii<models.length; ii++){
					String name2=modelNames[ii]+"_"+colorNames[j]+"_"+sizeNamesOfAI[k]+".ai";
					if(name2.equalsIgnoreCase(_01D.getName())) continue;

					File _copied=new File("D:\\耀古\\内裤\\生产\\物料\\型号标签\\PRO\\"+name2);

					System.out.println(_01D.getAbsolutePath() +" -> "+_copied.getAbsolutePath());

					JDFSFile.save(new FileInputStream(_01D), _copied.getAbsolutePath());
				}
			}
		}

		System.exit(0);
	}
}