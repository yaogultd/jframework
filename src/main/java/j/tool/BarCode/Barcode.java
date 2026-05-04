/* ================================================================
 * JBarcode : Java Barcode Library
 * ================================================================
 *
 * Project Info:  http://jbcode.sourceforge.net
 * Project Lead:  Fl�vio Sampaio (flavio@ronisons.com);
 *
 * (C) Copyright 2005, by Favio Sampaio
 *
 * This library is free software; you can redistribute it and/or modify it underthe terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package j.tool.BarCode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import j.tool.BarCode.encode.BarSet;
import j.tool.BarCode.encode.BarcodeEncoder;
import j.tool.BarCode.encode.InvalidAtributeException;
import j.tool.BarCode.paint.BarcodePainter;
import j.tool.BarCode.paint.TextPainter;





/**
 * A barcode class implemented using Java 2D APIs.
 * <br>
 * JBarcode coordinates three objects - <code>BarcodeEncoder</code> 
 * encode a text, <code>BarcodePaint</code> implements a stratergy to
 * paint the code, and <code>TextPainter</code> implements the barcode
 * text drawing - and several attibutes to achieve its aim of being able
 * to draw a barcode on a Java 2D graphics device.  
 * 
 * @author Flavio Sampaio
 * @since 0.1
 */
public class Barcode {
    
    public static final double MIN_XDIMENSION = 0.264583333;

	private int barHeight;

	private double wideToNarrow;

    private int xdimension;

	private boolean showText;

	private boolean checkDigit;

	private boolean showCheckDigit;

	private BarcodeEncoder bcencoder;

	private BarcodePainter bcpainter;

    private TextPainter textpainter;
    
    private double module;

    /**
     *
     * @param encoder
     * @param painter
     * @param textpainter
     * @param xdimension
     * @param barHeight
     * @param wideRatio
     */
    public Barcode(BarcodeEncoder encoder, BarcodePainter painter, TextPainter textpainter, double xdimension, int barHeight, double wideRatio){
        this.bcencoder = encoder;
        this.bcpainter = painter;
        this.textpainter = textpainter;
        this.barHeight = barHeight;
        this.xdimension = (int)(xdimension/MIN_XDIMENSION);
        this.xdimension = this.xdimension > 0 ? this.xdimension : 1;
        this.module = xdimension;
        this.wideToNarrow = wideRatio;
        this.showCheckDigit = true;
        this.checkDigit = true;
        this.showText = true;
    }

    /**
     * 
     * 
     * @param encoder
     * @param painter
     * @param textpainter
     */
    public Barcode(BarcodeEncoder encoder, BarcodePainter painter, TextPainter textpainter){
        this(encoder, painter, textpainter, MIN_XDIMENSION, 60, 2.0);
    }
    
    /**
     * 
     * 
     * @param code
     * @return
     * @throws InvalidAtributeException
     */
    public BufferedImage createBarcode(String code) throws InvalidAtributeException{
        String tmp = new String(code);
        if(isCheckDigit()){
            code = code + bcencoder.computeCheckSum(code);
            if(isShowCheckDigit()){
                tmp = code;
            }
            
        }
        BarSet [] encoded = bcencoder.encode(code);
        BufferedImage img = bcpainter.paint(encoded, 1, barHeight, wideToNarrow);
        if(isShowText()){
            textpainter.paintText(img, tmp, 1);
        }
        
        BufferedImage result = new BufferedImage((int)(img.getWidth()*(module/(MIN_XDIMENSION)))+1, (int)getBarHeight(), BufferedImage.TYPE_INT_RGB);
        AffineTransform at =
            AffineTransform.getScaleInstance((module/(MIN_XDIMENSION)),
                getBarHeight()/img.getHeight());
        Graphics2D g2d = result.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, result.getWidth(), result.getHeight());
        g2d.drawRenderedImage(img, at);
	
        return result;
    }
    
    /**
     * 
     * 
     * @param text
     * @return
     * @throws InvalidAtributeException
     */
    public String calcCheckSum(String text) throws InvalidAtributeException{
        return bcencoder.computeCheckSum(text);
    }

    /**
     * Get the bar height value (in millimeter).
     * 
     * @return Returns the barHeigth.
     */
    public double getBarHeight() {
        return barHeight;
    }

    /**
     * Set the bar height value (in millimeter).
     *
     * @param barHeight The bar height value (in millimeter).
     */
    public void setBarHeight(double barHeight) {
        this.barHeight = (int)Math.round(barHeight/MIN_XDIMENSION);
    }

    /**
     * 
     * 
     * @return Returns the bcencoder.
     */
    public BarcodeEncoder getEncoder() {
        return bcencoder;
    }

    /**
     * 
     * 
     * @param bcencoder The bcencoder to set.
     */
    public void setEncoder(BarcodeEncoder bcencoder) {
        this.bcencoder = bcencoder;
    }

    /**
     * 
     * 
     * @return Returns the bcpainter.
     */
    public BarcodePainter getPainter() {
        return bcpainter;
    }

    /**
     * 
     * 
     * @param bcpainter The bcpainter to set.
     */
    public void setPainter(BarcodePainter bcpainter) {
        this.bcpainter = bcpainter;
    }

    /**
     * 
     * 
     * @return Returns the wideToNarrow.
     */
    public double getWideRatio() {
        return wideToNarrow;
    }

    /**
     * 
     * @param wideRatio The wideToNarrow to set.
     * @throws InvalidAtributeException 
     */
    public void setWideRatio(double wideRatio) throws InvalidAtributeException {
        if(wideRatio < 1){
            throw new InvalidAtributeException("[JBarcode] Invalid wide to narrow ratio value.");
        }    
        this.wideToNarrow = wideRatio;
    }

    /**
     * Get the narrowest bar width value (in millimeter).
     * 
     * @return Returns the narrowWidth.
     */
    public double getXDimension() {
        return module;
    }

    /**
     * Set the narrowest bar width value (in millimeter).
     * 
     * @param xdimension The narrowest bar width value (in millimeter).
     */
    public void setXDimension(double xdimension) throws InvalidAtributeException {
        if(xdimension < 0){
            throw new InvalidAtributeException("[JBarcode] Invalid x-dimention value.");
        }        
        this.xdimension = (int)(xdimension/MIN_XDIMENSION);
        this.xdimension = this.xdimension > 0 ? this.xdimension : 1;
        this.module = xdimension;
    }

    /**
     * 
     * 
     * @return Returns the checkDigit.
     */
    public boolean isCheckDigit() {
        return checkDigit;
    }

    /**
     * 
     * 
     * @param checkDigit The checkDigit to set.
     */
    public void setCheckDigit(boolean checkDigit) {
        this.checkDigit = checkDigit;
    }

    /**
     * 
     * 
     * @return Returns the showCheckDigit.
     */
    public boolean isShowCheckDigit() {
        return showCheckDigit;
    }

    /**
     * 
     * 
     * @param showCheckDigit The showCheckDigit to set.
     */
    public void setShowCheckDigit(boolean showCheckDigit) {
        this.showCheckDigit = showCheckDigit;
    }

    /**
     * 
     * 
     * @return Returns the showText.
     */
    public boolean isShowText() {
        return showText;
    }

    /**
     * 
     * 
     * @param showText The showText to set.
     */
    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    /**
     * 
     * 
     * @return Returns the textpainter.
     */
    public TextPainter getTextPainter() {
        return textpainter;
    }

    /**
     * 
     * 
     * @param textpainter The textpainter to set.
     */
    public void setTextPainter(TextPainter textpainter) {
        this.textpainter = textpainter;
    }
    
    /**
     * 
     */
    public String toString(){
        return bcencoder.toString();
    }
}