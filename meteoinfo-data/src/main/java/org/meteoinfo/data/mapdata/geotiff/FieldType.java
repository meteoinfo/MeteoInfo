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

/**
 *
 * @author yaqiang
 */
public class FieldType {
    // <editor-fold desc="Variables">

    private static FieldType[] types = new FieldType[20];
    public static final FieldType BYTE = new FieldType("BYTE", 1, 1);
    public static final FieldType ASCII = new FieldType("ASCII", 2, 1);
    public static final FieldType SHORT = new FieldType("SHORT", 3, 2);
    public static final FieldType LONG = new FieldType("LONG", 4, 4);
    public static final FieldType RATIONAL = new FieldType("RATIONAL", 5, 8);
    public static final FieldType SBYTE = new FieldType("SBYTE", 6, 1);
    public static final FieldType UNDEFINED = new FieldType("UNDEFINED", 7, 1);
    public static final FieldType SSHORT = new FieldType("SSHORT", 8, 2);
    public static final FieldType SLONG = new FieldType("SLONG", 9, 4);
    public static final FieldType SRATIONAL = new FieldType("SRATIONAL", 10, 8);
    public static final FieldType FLOAT = new FieldType("FLOAT", 11, 4);
    public static final FieldType DOUBLE = new FieldType("DOUBLE", 12, 8);
    String name;
    int code;
    int size;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param name Name
     * @param code Code
     * @param size Size
     */
    private FieldType(String name, int code, int size) {
        this.name = name;
        this.code = code;
        this.size = size;
        types[code] = this;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get field type
     *
     * @param code Field type code
     * @return Field type
     */
    static FieldType get(int code) {
        return types[code];
    }

    /**
     * To string
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.name;
    }
    // </editor-fold>
}
