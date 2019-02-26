/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.netcdf;

import ucar.nc2.NetcdfFile;
import ucar.nc2.iosp.IOServiceProvider;

/**
 *
 * @author Yaqiang Wang
 */
public class NetcdfFileE extends NetcdfFile {

    /**
     * Constructor
     * @param spi
     * @param location 
     */
    public NetcdfFileE(IOServiceProvider spi, String location) {
        this.spi = spi;
        this.location = location;
    }
}
