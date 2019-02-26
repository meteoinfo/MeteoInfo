 /* Copyright 2012 - Yaqiang Wang,
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
package org.meteoinfo.layer;

/**
 *
 * @author Yaqiang Wang
 */
public class VisibleScale {
    // <editor-fold desc="Variables">

    private boolean _enableMinVisScale = false;
    private boolean _enableMaxVisScale = false;
    private double _minVisScale;
    private double _maxVisScale;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get if is enable minimum visible scale
     *
     * @return Boolean
     */
    public boolean isEnableMinVisScale() {
        return _enableMinVisScale;
    }

    /**
     * Set if is enable minimum visible scale
     *
     * @param value Boolean
     */
    public void setEnableMinVisScale(boolean value) {
        _enableMinVisScale = value;
    }

    /**
     * Get if is enable maximum visible scale
     *
     * @return Boolean
     */
    public boolean isEnableMaxVisScale() {
        return _enableMaxVisScale;
    }

    /**
     * Set if is enable maximum visible scale
     *
     * @param value Boolean
     */
    public void setEnableMaxVisScale(boolean value) {
        _enableMaxVisScale = value;
    }

    /**
     * Get minimum visible scale value
     *
     * @return Minimum visible scale value
     */
    public double getMinVisScale() {
        return _minVisScale;
    }

    /**
     * Set minimum visible scale value
     *
     * @param value Minimum visible scale value
     */
    public void setMinVisScale(double value) {
        _minVisScale = value;
    }

    /**
     * Get maximum visible scale value
     *
     * @return maximum visible scale value
     */
    public double getMaxVisScale() {
        return _maxVisScale;
    }

    /**
     * Set maximum visible scale value
     *
     * @param value maximum visible scale value
     */
    public void setMaxVisScale(double value) {
        _maxVisScale = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get if is visible scale enabled
     *
     * @return Is visible scale enabled
     */
    public boolean isVisibleScaleEnabled() {
        if (this._enableMaxVisScale || this._enableMinVisScale) {
            return true;
        } else {
            return false;
        }
    }
    // </editor-fold>
}
