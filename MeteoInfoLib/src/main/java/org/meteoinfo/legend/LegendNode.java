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
package org.meteoinfo.legend;

import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author yaqiang
 */
public class LegendNode extends ItemNode {
    // <editor-fold desc="Variables">

    private ShapeTypes _shapeType;
    private ColorBreak _legendBreak;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get shape type
     *
     * @return The shape type
     */
    public ShapeTypes getShapeType() {
        return _shapeType;
    }

    /**
     * Set shape type
     *
     * @param type The shape type
     */
    public void setShapeType(ShapeTypes type) {
        _shapeType = type;
    }

    /**
     * Get legend break
     *
     * @return The legend break
     */
    public ColorBreak getLegendBreak() {
        return _legendBreak;
    }

    /**
     * Set legend break
     *
     * @param aCB The legend break
     */
    public void setLegendBreak(ColorBreak aCB) {
        _legendBreak = aCB;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Clone
     *
     * @return LegendNode object
     */
    @Override
    public Object clone() {
        LegendNode aLN = new LegendNode();
        aLN.setShapeType(_shapeType);
        aLN.setLegendBreak(_legendBreak);

        return aLN;
    }
    
    @Override
    public int getDrawHeight() {
        return this.getHeight();
    }

    @Override
    public int getExpandedHeight() {
        return this.getHeight();
    }
    // </editor-fold>
}
