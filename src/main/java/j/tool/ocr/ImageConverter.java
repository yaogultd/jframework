package j.tool.ocr;

import j.core.annotation.description.ClassDescription;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

@ClassDescription(author = "肖炯", date = "2021/11/12", description = "图片颜色转换")
public class ImageConverter {
    /**
     *
     * @param source
     * @param target
     * @param line
     * @param altIfLarger
     * @param altElse
     * @throws IOException
     */
    public static void convert(File source, File target, int line, int altIfLarger, int altElse) throws IOException {
        BufferedImage bi = ImageIO.read(source);//通过imageio将图像载入
        int h = bi.getHeight();//获取图像的高
        int w = bi.getWidth();//获取图像的宽
        int[][] gray = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                gray[x][y] = getGray(bi.getRGB(x, y));
            }
        }

        BufferedImage nbi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int alt = 0;
                if (getAverageColor(gray, x, y, w, h) > line) {
                    alt = altIfLarger;//new Color(0, 0, 0).getRGB();
                } else {
                    alt = altElse;//new Color(255, 255, 255).getRGB();
                }
                nbi.setRGB(x, y, alt);
            }
        }

        File file = target == null ? source : target;
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(source.getName().toLowerCase().endsWith("gif")){
            ImageIO.write(nbi, "gif", file);
        }else if(source.getName().toLowerCase().endsWith("png")){
            ImageIO.write(nbi, "png", file);
        }else{
            ImageIO.write(nbi, "jpg", file);
        }
    }

    /**
     * @param rgb
     * @return
     */
    private static int getGray(int rgb) {
        String str = Integer.toHexString(rgb);
        int r = Integer.parseInt(str.substring(2, 4), 16);
        int g = Integer.parseInt(str.substring(4, 6), 16);
        int b = Integer.parseInt(str.substring(6, 8), 16);
        //or 直接new个color对象
        Color c = new Color(rgb);
        r = c.getRed();
        g = c.getGreen();
        b = c.getBlue();
        int top = (r + g + b) / 3;
        return (int) (top);
    }

    /**
     * @param gray
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    private static int getAverageColor(int[][] gray, int x, int y, int w, int h) {
        int rs = gray[x][y]
                + (x == 0 ? 255 : gray[x - 1][y])
                + (x == 0 || y == 0 ? 255 : gray[x - 1][y - 1])
                + (x == 0 || y == h - 1 ? 255 : gray[x - 1][y + 1])
                + (y == 0 ? 255 : gray[x][y - 1])
                + (y == h - 1 ? 255 : gray[x][y + 1])
                + (x == w - 1 ? 255 : gray[x + 1][y])
                + (x == w - 1 || y == 0 ? 255 : gray[x + 1][y - 1])
                + (x == w - 1 || y == h - 1 ? 255 : gray[x + 1][y + 1]);
        return rs / 9;
    }
}
