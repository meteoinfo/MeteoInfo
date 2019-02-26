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
package org.meteoinfo.map;

import org.meteoinfo.global.Direction;
import org.meteoinfo.global.PointD;

/**
 *
 * @author Yaqiang Wang
 */
public class GridLabel {
    // <editor-fold desc="Variables">

    private Direction _labDirection;
    private String _labString;
    private PointD _labPoint;
    private PointD coord;
    private boolean _isLon;
    private boolean _isBorder;
    private float _value;
    private float angle;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public GridLabel() {
        _labDirection = Direction.East;
        _isLon = true;
        _isBorder = true;
        this.angle = Float.NaN;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get label direction
     *
     * @return Label direction
     */
    public Direction getLabDirection() {
        return _labDirection;
    }

    /**
     * Set label direction
     *
     * @param dir Label direction
     */
    public void setLabDirection(Direction dir) {
        _labDirection = dir;
    }

    /**
     * Get label string
     *
     * @return label string
     */
    public String getLabString() {
        return _labString;
    }

    /**
     * Set label string
     *
     * @param str Label string
     */
    public void setLabString(String str) {
        _labString = str;
    }

    /**
     * Get label point
     *
     * @return Label Point
     */
    public PointD getLabPoint() {
        return _labPoint;
    }

    /**
     * Set label point
     *
     * @param p Label Point
     */
    public void setLabPoint(PointD p) {
        _labPoint = p;
    }
    
    /**
     * Get coordinate
     * @return Coordinate
     */
    public PointD getCoord() {
        return this.coord;
    }
    
    /**
     * Set coordinate
     * @param value Coordinate
     */
    public void setCoord(PointD value) {
        this.coord = value;
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
     * Get if is border
     *
     * @return Boolean
     */
    public boolean isBorder() {
        return _isBorder;
    }

    /**
     * Set if is border
     *
     * @param istrue Boolean
     */
    public void setBorder(boolean istrue) {
        _isBorder = istrue;
    }

    /**
     * Get value
     *
     * @return Value
     */
    public float getValue() {
        return _value;
    }

    /**
     * Set value
     *
     * @param value Value
     */
    public void setValue(float value) {
        _value = value;
    }
    
    /**
     * Get angle
     * @return Angle
     */
    public float getAngle() {
        return this.angle;
    }
    
    /**
     * Set angle
     * @param value Angle
     */
    public void setAnge(float value){
        this.angle = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
