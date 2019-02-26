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
package org.meteoinfo.global.colors;

/**
 *
 * @author Yaqiang Wang
 */
public class HSL {
    // <editor-fold desc="Variables">

    private double _h;
    private double _s;
    private double _l;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public HSL() {
        _h = 0;
        _s = 0;
        _l = 0;
    }

    /**
     * Constructor
     *
     * @param h Hue
     * @param s Saturation
     * @param l Luminance
     */
    public HSL(double h, double s, double l) {
        _h = h;
        _s = s;
        _l = l;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get hue
     *
     * @return Hue value
     */
    public double getH() {
        return _h;
    }

    /**
     * Set hue
     *
     * @param h Hue
     */
    public void setH(double h) {
        _h = h;
        _h = _h > 1 ? 1 : _h < 0 ? 0 : _h;
    }

    /**
     * Get saturation
     *
     * @return Saturation
     */
    public double getS() {
        return _s;
    }

    /**
     * Set saturation
     *
     * @param s Saturation
     */
    public void setS(double s) {
        _s = s;
        _s = _s > 1 ? 1 : _s < 0 ? 0 : _s;
    }

    /**
     * Get luminance
     *
     * @return Luminance
     */
    public double getL() {
        return _l;
    }

    /**
     * Set luminance
     *
     * @param l Luminance
     */
    public void setL(double l) {
        _l = l;
        _l = _l > 1 ? 1 : _l < 0 ? 0 : _l;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
