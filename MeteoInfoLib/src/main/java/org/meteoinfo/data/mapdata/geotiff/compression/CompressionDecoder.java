/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.geotiff.compression;

import java.nio.ByteOrder;

/**
 * Compression decoder interface
 *
 * @author osbornb
 */
public interface CompressionDecoder {

    /**
     * Decode the bytes
     *
     * @param bytes bytes to decode
     * @param byteOrder byte order
     * @return decoded bytes
     */
    public byte[] decode(byte[] bytes, ByteOrder byteOrder);

}
