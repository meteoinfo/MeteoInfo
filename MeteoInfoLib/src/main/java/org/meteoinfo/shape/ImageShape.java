/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageShape extends PointShape {
    // <editor-fold desc="Variables">
    private BufferedImage image;
    private Object interp;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ImageShape(){
        super();
        interp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get image
     * @return Image
     */
    public BufferedImage getImage(){
        return this.image;
    }
    
    /**
     * Set image
     * @param value Image
     */
    public void setImage(BufferedImage value){
        this.image = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.Image;
    }
    
    /**
     * Get interpolation
     * @return Interpolation
     */
    public Object getInterpolation(){
        return this.interp;
    }
    
    /**
     * Set interpolation object
     * @param value Interpolation object
     */
    public void setInterpolation(Object value){
        this.interp = value;
    }
    
    /**
     * Set interpolation string
     * @param value Interpolation string
     */
    public void setInterpolation(String value){
        switch (value){
            case "nearest":
                this.interp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                break;
            case "bilinear":
                this.interp = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                break;
            case "bicubic":
                this.interp = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                break;
        }
    }
    
//    @Override
//    public Extent getExtent(){
//        Extent extent = new Extent();
//        extent.minX = this.getPoint().X;
//        extent.minY = this.getPoint().Y;
//        extent.maxX = extent.minX + this.image.getWidth();
//        extent.maxY = extent.minY + this.image.getHeight();
//        return extent;
//    }
    // </editor-fold>
}
