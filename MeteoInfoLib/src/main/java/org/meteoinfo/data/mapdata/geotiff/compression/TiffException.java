/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.geotiff.compression;

/**
 * TIFF exception
 *
 * @author osbornb
 */
public class TiffException extends RuntimeException {

    /**
     * Serial version id
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public TiffException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message
     */
    public TiffException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message
     * @param throwable
     */
    public TiffException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructor
     *
     * @param throwable
     */
    public TiffException(Throwable throwable) {
        super(throwable);
    }

}
