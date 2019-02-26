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
package org.meteoinfo.data.mapdata.geotiff;

import java.util.HashMap;

/**
 *
 * @author yaqiang
 */
public class Tag implements Comparable {
    // <editor-fold desc="Variables">

    private static HashMap map = new HashMap();
    public static final Tag NewSubfileType = new Tag("NewSubfileType", 254);
    public static final Tag ImageWidth = new Tag("ImageWidth", 256);
    public static final Tag ImageLength = new Tag("ImageLength", 257);
    public static final Tag BitsPerSample = new Tag("BitsPerSample", 258);
    public static final Tag Compression = new Tag("Compression", 259);
    public static final Tag PhotometricInterpretation = new Tag("PhotometricInterpretation", 262);
    public static final Tag FillOrder = new Tag("FillOrder", 266);
    public static final Tag DocumentName = new Tag("DocumentName", 269);
    public static final Tag ImageDescription = new Tag("ImageDescription", 270);
    public static final Tag StripOffsets = new Tag("StripOffsets", 273);
    public static final Tag Orientation = new Tag("Orientation", 274);
    public static final Tag SamplesPerPixel = new Tag("SamplesPerPixel", 277);
    public static final Tag RowsPerStrip = new Tag("RowsPerStrip", 278);
    public static final Tag StripByteCounts = new Tag("StripByteCounts", 279);
    public static final Tag XResolution = new Tag("XResolution", 282);
    public static final Tag YResolution = new Tag("YResolution", 283);
    public static final Tag PlanarConfiguration = new Tag("PlanarConfiguration", 284);
    public static final Tag ResolutionUnit = new Tag("ResolutionUnit", 296);
    public static final Tag PageNumber = new Tag("PageNumber", 297);
    public static final Tag Software = new Tag("Software", 305);
    public static final Tag ColorMap = new Tag("ColorMap", 320);
    public static final Tag TileWidth = new Tag("TileWidth", 322);
    public static final Tag TileLength = new Tag("TileLength", 323);
    public static final Tag TileOffsets = new Tag("TileOffsets", 324);
    public static final Tag TileByteCounts = new Tag("TileByteCounts", 325);
    public static final Tag SampleFormat = new Tag("SampleFormat", 339);
    public static final Tag SMinSampleValue = new Tag("SMinSampleValue", 340);
    public static final Tag SMaxSampleValue = new Tag("SMaxSampleValue", 341);
    public static final Tag ModelPixelScaleTag = new Tag("ModelPixelScaleTag", 33550);
    public static final Tag IntergraphMatrixTag = new Tag("IntergraphMatrixTag", 33920);
    public static final Tag ModelTiepointTag = new Tag("ModelTiepointTag", 33922);
    public static final Tag ModelTransformationTag = new Tag("ModelTransformationTag", 34264);
    public static final Tag GeoKeyDirectoryTag = new Tag("GeoKeyDirectoryTag", 34735);
    public static final Tag GeoDoubleParamsTag = new Tag("GeoDoubleParamsTag", 34736);
    public static final Tag GeoAsciiParamsTag = new Tag("GeoAsciiParamsTag", 34737);
    public static final Tag GDALNoData = new Tag("GDALNoDataTag", 42113);
    private String name;
    private int code;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    static Tag get(int code) {
        return (Tag) map.get(new Integer(code));
    }

    private Tag(String name, int code) {
        this.name = name;
        this.code = code;
        map.put(new Integer(code), this);
    }

    Tag(int code) {
        this.code = code;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get name
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get code
     *
     * @return Code
     */
    public int getCode() {
        return this.code;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * To string
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.code + " (" + this.name + ")";
    }

    /**
     * Compare to
     *
     * @param o Object
     * @return Int
     */
    @Override
    public int compareTo(Object o) {
        return this.code - ((Tag) o).getCode();
    }
    // </editor-fold>
}
