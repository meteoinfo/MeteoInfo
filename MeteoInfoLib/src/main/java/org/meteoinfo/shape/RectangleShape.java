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
package org.meteoinfo.shape;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.PointD;

/**
 *
 * @author yaqiang
 */
public class RectangleShape extends PolygonShape {
    // <editor-fold desc="Variables">
    private boolean round = false;
    private double roundX = 0;
    private double roundY = 0;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public RectangleShape(){
        super();
    }
    
    /**
     * Constructor
     * @param x X
     * @param y Y
     * @param width Width
     * @param height Height
     */
    public RectangleShape(double x, double y, double width, double height){
        super();
        List<PointD> points = new ArrayList<>();
        points.add(new PointD(x, y));
        points.add(new PointD(x, y + height));
        points.add(new PointD(x + width, y + height));
        points.add(new PointD(x + width, y));
        this.setPoints(points);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.Rectangle;
    }
    
    /**
     * Get is round or not
     * @return Boolean
     */
    public boolean isRound(){
        return this.round;
    }
    
    /**
     * Get round x
     * @return Round x
     */
    public double getRoundX(){
        return this.roundX;
    }
    
    /**
     * Set round y
     * @param value Round y 
     */
    public void setRoundX(double value){
        this.roundX = value;
        this.round = true;
    }
    
    /**
     * Get round y
     * @return Round y
     */
    public double getRoundY(){
        return this.roundY;
    }
    
    /**
     * Set round y
     * @param value Round y
     */
    public void setRoundY(double value){
        this.roundY = value;
        this.round = true;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     *
     * @return RectangleShape object
     */
    @Override
    public Object clone() {
        RectangleShape aPGS = new RectangleShape();
        aPGS.setExtent(this.getExtent());
        aPGS.setPoints(new ArrayList<>(this.getPoints()));
        aPGS.setVisible(this.isVisible());
        aPGS.setSelected(this.isSelected());
        aPGS.round = this.round;
        aPGS.roundX = this.roundX;
        aPGS.roundY = this.roundY;

        return aPGS;
    }
    // </editor-fold>
}
