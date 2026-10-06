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
package org.meteoinfo.geometry.geoprocess;

 import org.meteoinfo.geometry.Extent;
 import org.meteoinfo.geometry.Coordinate;

 /**
 *
 * @author yaqiang
 */
public class ClipLine {
    // <editor-fold desc="Variables">

    private double _value;
    private boolean _isLon;
    private boolean _isLeftOrTop;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public ClipLine() {
        _isLon = true;
        _isLeftOrTop = true;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get value
     *
     * @return Value
     */
    public double getValue() {
        return _value;
    }

    /**
     * Set value
     *
     * @param value Value
     */
    public void setValue(double value) {
        _value = value;
    }

    /**
     * Get if is longitude
     *
     * @return Boolean
     */
    public boolean isLongitude() {
        return _isLon;
    }

    /**
     * Set if is longitude
     *
     * @param istrue Boolean
     */
    public void setLongitude(boolean istrue) {
        _isLon = istrue;
    }

    /**
     * Get if is left (longitude) or top (latitude)
     *
     * @return Boolean
     */
    public boolean isLeftOrTop() {
        return _isLeftOrTop;
    }

    /**
     * Set if is left (longitude) or top (latitude)
     *
     * @param istrue
     */
    public void setLeftOrTop(boolean istrue) {
        _isLeftOrTop = istrue;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Determine if a point is inside
     *
     * @param aPoint The Point
     * @return If is inside
     */
    public boolean isInside(Coordinate aPoint) {
        boolean isIn = false;
        if (_isLon) {
            if (_isLeftOrTop) {
                isIn = (aPoint.x <= _value);
            } else {
                isIn = (aPoint.x >= _value);
            }
        } else {
            if (_isLeftOrTop) {
                isIn = (aPoint.y >= _value);
            } else {
                isIn = (aPoint.y <= _value);
            }
        }

        return isIn;
    }

    /**
     * Determine if an extent is cross
     *
     * @param aExtent The extent
     * @return Is extent cross
     */
    public boolean isExtentCross(Extent aExtent) {
        if (_isLeftOrTop) {
            Coordinate aPoint = new Coordinate(aExtent.minX, aExtent.maxY);
            return isInside(aPoint);
        } else {
            Coordinate aPoint = new Coordinate(aExtent.maxX, aExtent.minY);
            return isInside(aPoint);
        }
    }

    /**
     * Determine if an extent is inside
     *
     * @param aExtent The extent
     * @return Is extent inside
     */
    public boolean isExtentInside(Extent aExtent) {
        if (_isLeftOrTop) {
            Coordinate aPoint = new Coordinate(aExtent.maxX, aExtent.minY);
            return isInside(aPoint);
        } else {
            Coordinate aPoint = new Coordinate(aExtent.minX, aExtent.maxY);
            return isInside(aPoint);
        }
    }
    // </editor-fold>
}
