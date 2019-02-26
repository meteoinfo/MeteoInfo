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

/**
 * Vector break class
 *
 * @author Yaqiang
 */
public class VectorBreak extends ColorBreak {
    // <editor-fold desc="Variables">

    private float _zoom = 1.0f;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public VectorBreak() {
        super();
        this.setBreakType(BreakTypes.VectorBreak);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get zoom
     *
     * @return Zoom value
     */
    public float getZoom() {
        return _zoom;
    }

    /**
     * Set zoom
     *
     * @param zoom Zoom value
     */
    public void setZoom(float zoom) {
        _zoom = zoom;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
