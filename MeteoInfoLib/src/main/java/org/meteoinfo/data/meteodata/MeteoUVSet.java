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
package org.meteoinfo.data.meteodata;

import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class MeteoUVSet {
    // <editor-fold desc="Variables">

    private String _uStr;
    private String _vStr;
    private boolean _isFixUVStr;
    private boolean _isUV;
    private MeteoDataInfo uDataInfo;
    private MeteoDataInfo vDataInfo;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MeteoUVSet() {
        _uStr = "U";
        _vStr = "V";
        _isFixUVStr = false;
        _isUV = true;
    }
    
    /**
     * Constructor
     * @param mdi MeteoDataInfo
     */
    public MeteoUVSet(MeteoDataInfo mdi){
        this();
        this.uDataInfo = mdi;
        this.vDataInfo = mdi;
    }
    
    /**
     * Constructor
     * @param umdi U MeteoDataInfo
     * @param vmdi V MeteoDataInfo
     */
    public MeteoUVSet(MeteoDataInfo umdi, MeteoDataInfo vmdi){
        this();
        this.uDataInfo = umdi;
        this.vDataInfo = vmdi;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get U variable name
     *
     * @return U variable name
     */
    public String getUStr() {
        return _uStr;
    }

    /**
     * Set U variable name
     *
     * @param value U variable name
     */
    public void setUStr(String value) {
        _uStr = value;
    }

    /**
     * Get V variable name
     *
     * @return V variable name
     */
    public String getVStr() {
        return _vStr;
    }

    /**
     * Set V variable name
     *
     * @param value V variable name
     */
    public void setVStr(String value) {
        _vStr = value;
    }

    /**
     * Get if fix U/V variable names
     *
     * @return Boolean
     */
    public boolean isFixUVStr() {
        return _isFixUVStr;
    }

    /**
     * Set if fix U/V variable name
     *
     * @param istrue Boolean
     */
    public void setFixUVStr(boolean istrue) {
        _isFixUVStr = istrue;
    }

    /**
     * Get if is U/V or direction/speed
     *
     * @return Boolean
     */
    public boolean isUV() {
        return _isUV;
    }

    /**
     * Set if is U/V or direction/speed
     *
     * @param istrue Boolean
     */
    public void setUV(boolean istrue) {
        _isUV = istrue;
    }
    
    /**
     * Get U data info
     * @return U data info
     */
    public MeteoDataInfo getUDataInfo(){
        return this.uDataInfo;
    }
    
    /**
     * Set U data info
     * @param value U data info
     */
    public void setUDataInfo(MeteoDataInfo value){
        this.uDataInfo = value;
    }
    
    /**
     * Get V data info
     * @return V data info
     */
    public MeteoDataInfo getVDataInfo(){
        return this.vDataInfo;
    }
    
    /**
     * Set V data info
     * @param value V data info
     */
    public void setVDataInfo(MeteoDataInfo value){
        this.vDataInfo = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Try to set U/V variable names automatic
     *
     * @param vList Variables list
     * @return If can find U/V variables
     */
    public boolean autoSetUVStr(List<String> vList) {
        Boolean isOK = false;
        if (vList.contains("U") && vList.contains("V")) {
            _uStr = "U";
            _vStr = "V";
            isOK = true;
        }

        if (vList.contains("u") && vList.contains("v")) {
            _uStr = "u";
            _vStr = "v";
            isOK = true;
        }

        if (vList.contains("U10M") && vList.contains("V10M")) {
            _uStr = "U10M";
            _vStr = "V10M";
            isOK = true;
        }

        if (vList.contains("UWND") && vList.contains("VWND")) {
            _uStr = "UWND";
            _vStr = "VWND";
            isOK = true;
        }

        if (vList.contains("uwnd") && vList.contains("vwnd")) {
            _uStr = "uwnd";
            _vStr = "vwnd";
            isOK = true;
        }

        if (vList.contains("ugrd") && vList.contains("vgrd")) {
            _uStr = "ugrd";
            _vStr = "vgrd";
            isOK = true;
        }

        return isOK;
    }
    // </editor-fold>
}
