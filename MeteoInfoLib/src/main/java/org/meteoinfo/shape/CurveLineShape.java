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

/**
 * Curve line shape class
 * 
 * @author Yaqiang
 */
public class CurveLineShape extends PolylineShape {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public CurveLineShape() {
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public ShapeTypes getShapeType(){
        return ShapeTypes.CurveLine;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     * 
     * @return CurveLineShape
     */
    @Override
    public Object clone() {
        CurveLineShape aPLS = new CurveLineShape();
        aPLS.setValue(this.getValue());
        aPLS.setExtent(this.getExtent());
        aPLS.setPartNum(this.getPartNum());
        aPLS.parts = (int[]) parts.clone();
        aPLS.setPoints(this.getPoints());
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        
        return aPLS;
    }

    /**
     * Value clone
     * 
     * @return CurveLineShape 
     */
    @Override
    public CurveLineShape valueClone() {
        CurveLineShape aPLS = new CurveLineShape();
        aPLS.setValue(this.getValue());        
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        
        return aPLS;
    }
    // </editor-fold>
}
