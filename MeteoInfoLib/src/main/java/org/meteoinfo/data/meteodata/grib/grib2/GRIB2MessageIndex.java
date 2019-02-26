/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.grib.grib2;

import java.util.Date;
import org.meteoinfo.data.meteodata.Variable;

/**
 *
 * @author yaqiang
 */
public class GRIB2MessageIndex {
    // <editor-fold desc="Variables">
    /// <summary>
    /// Message start position bytes
    /// </summary>

    public long messagePos;
    /// <summary>
    /// Grid data position. record repeat start postion
    /// </summary>
    public long dataPos;
    /// <summary>
    /// repeat start section.
    /// </summary>
    public int startSection;
    /// <summary>
    /// DateTime
    /// </summary>
    public Date dateTime;
    /// <summary>
    /// Level
    /// </summary>
    public double level;
    /// <summary>
    /// Parameter
    /// </summary>
    public Variable parameter;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GRIB2MessageIndex() {
        dateTime = new Date();
        parameter = new Variable();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Judge if two message index are equal
     *
     * @param aMessageIdx The message index
     * @return If is equal
     */
    public boolean equals(GRIB2MessageIndex aMessageIdx) {
        if (dateTime != aMessageIdx.dateTime) {
            return false;
        }
        if (!parameter.equals(aMessageIdx.parameter)) {
            return false;
        }
        if (level != aMessageIdx.level) {
            return false;
        }

        return true;
    }
    // </editor-fold>
}
