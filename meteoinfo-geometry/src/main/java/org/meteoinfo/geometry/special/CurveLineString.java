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
package org.meteoinfo.geometry.special;

 import org.meteoinfo.geometry.GeometryType;
 import org.meteoinfo.geometry.MultiLineString;

 /**
 * Curve line shape class
 * 
 * @author Yaqiang
 */
public class CurveLineString extends MultiLineString {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public CurveLineString() {
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public GeometryType getGeometryType(){
        return GeometryType.CURVE_LINE;
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
        CurveLineString aPLS = new CurveLineString();
        aPLS.setValue(this.getValue());
        aPLS.setExtent(this.getExtent());
        aPLS.setPartNum(this.getNumParts());
        aPLS.parts = (int[]) parts.clone();
        aPLS.setCoordinates(this.getCoordinates());
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
    public CurveLineString valueClone() {
        CurveLineString aPLS = new CurveLineString();
        aPLS.setValue(this.getValue());        
        aPLS.setVisible(this.isVisible());
        aPLS.setSelected(this.isSelected());
        
        return aPLS;
    }
    // </editor-fold>
}
