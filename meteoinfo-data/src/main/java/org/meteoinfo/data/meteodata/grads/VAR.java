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
package org.meteoinfo.data.meteodata.grads;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class VAR {
    // <editor-fold desc="Variables">

    private String _vName;
    private int _levelNum;
    private String _units;
    private String _description;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get variable name
     *
     * @return Variable name
     */
    public String getVName() {
        return _vName;
    }

    /**
     * Set variable name
     *
     * @param value Variable name
     */
    public void setVName(String value) {
        _vName = value;
    }

    /**
     * Get level number
     *
     * @return Level number
     */
    public int getLevelNum() {
        return _levelNum;
    }

    /**
     * Set level number
     *
     * @param value Level number
     */
    public void setLevelNum(int value) {
        _levelNum = value;
    }

    /**
     * Get units
     *
     * @return Units
     */
    public String getUnits() {
        return _units;
    }

    /**
     * Set units
     *
     * @param value Units
     */
    public void setUnits(String value) {
        _units = value;
    }

    /**
     * Get description
     *
     * @return Description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Set description
     *
     * @param value Description
     */
    public void setDescription(String value) {
        _description = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
