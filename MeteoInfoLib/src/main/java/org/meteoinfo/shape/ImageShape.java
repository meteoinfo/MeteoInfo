/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageShape extends PointShape {
    // <editor-fold desc="Variables">
    private BufferedImage image;
    private Object interp;
    private List<PointZ> coords;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ImageShape(){
        super();
        interp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        this.coords = new ArrayList<>();
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
    
    /**
     * Get coordinates - lower left, lower right, upper right, upper left
     * @return Coordinates
     */
    public List<PointZ> getCoords() {
        return this.coords;
    }
    
    /**
     * Set coordinates
     * @param value Coordinates
     */
    public void setCoords(List<PointZ> value) {
        this.coords = value;
    }
    
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    // </editor-fold>
}
