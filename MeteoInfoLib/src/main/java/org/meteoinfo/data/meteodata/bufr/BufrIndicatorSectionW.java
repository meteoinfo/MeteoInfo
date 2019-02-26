/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.bufr;

import java.io.IOException;
import ucar.nc2.iosp.bufr.BufrIndicatorSection;
import ucar.unidata.io.RandomAccessFile;

/**
 *
 * @author Yaqiang Wang
 */
public class BufrIndicatorSectionW extends BufrIndicatorSection{
    
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param raf RandomAccessFile
     * @throws IOException 
     */
    public BufrIndicatorSectionW(RandomAccessFile raf) throws IOException {
        super(raf);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>    
    
}
