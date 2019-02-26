 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.layout;

/**
 *
 * @author yaqiang
 */
public class PaperSize {
    // <editor-fold desc="Variables">
    private String _name;
    private int _width;
    private int _height;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public PaperSize(){
        
    }
    
    /**
     * Constructor
     * @param name Name
     * @param width Width
     * @param height Height
     */
    public PaperSize(String name, int width, int height){
        this._name = name;
        this._width = width;
        this._height = height;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get name
     * @return Name
     */
    public String getName(){
        return _name;
    }
    
    /**
     * Set name
     * @param name Name
     */
    public void setName(String name){
        _name = name;
    }
    
    /**
     * Get width
     * @return Width
     */
    public int getWidth(){
        return _width;
    }
    
    /**
     * Set width
     * @param width Width 
     */
    public void setWidth(int width){
        _width = width;
    }
    
    /**
     * Get height
     * @return Height
     */
    public int getHeight(){
        return _height;
    }
    
    /**
     * Set height
     * @param height Height 
     */
    public void setHeight(int height){
        _height = height;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
