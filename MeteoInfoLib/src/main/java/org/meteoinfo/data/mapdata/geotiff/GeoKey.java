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
public class GeoKey {
    // <editor-fold desc="Variables">

    boolean isDouble = false;
    boolean isString = false;
    private int count;
    private int[] value;
    private double[] dvalue;
    private String valueS;
    protected Tag tag;
    private TagValue tagValue;
    private int id;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param tag Tag
     * @param tagValue Tag value
     */
    GeoKey(Tag tag, TagValue tagValue) {
        this.tag = tag;
        this.tagValue = tagValue;
        this.count = 1;
    }

    /**
     * Constructor
     *
     * @param tag Tag
     * @param value Value
     */
    GeoKey(Tag tag, int value) {
        this.tag = tag;
        this.value = new int[1];
        this.value[0] = value;
        this.count = 1;
    }

    /**
     * Constructor
     *
     * @param tag Tag
     * @param value Int value array
     */
    GeoKey(Tag tag, int[] value) {
        this.tag = tag;
        this.value = value;
        this.count = value.length;
    }

    /**
     * Constructor
     *
     * @param tag Tag
     * @param value Double value array
     */
    GeoKey(Tag tag, double[] value) {
        this.tag = tag;
        this.dvalue = value;
        this.count = value.length;
        this.isDouble = true;
    }

    /**
     * Constructor
     *
     * @param tag Tag
     * @param value Double value
     */
    GeoKey(Tag tag, double value) {
        this.tag = tag;
        this.dvalue = new double[1];
        this.dvalue[0] = value;
        this.count = 1;
        this.isDouble = true;
    }

    /**
     * Constructor
     *
     * @param tag Tag
     * @param value String value
     */
    GeoKey(Tag tag, String value) {
        this.tag = tag;
        this.valueS = value;
        this.count = 1;
        this.isString = true;
    }

    /**
     * Constructor
     *
     * @param id Id
     * @param v Value
     */
    GeoKey(int id, int v) {
        this.id = id;
        this.count = 1;

        this.tag = Tag.get(id);
        this.tagValue = TagValue.get(this.tag, v);

        if (this.tagValue == null) {
            this.value = new int[1];
            this.value[0] = v;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get count
     *
     * @return Count
     */
    int count() {
        return this.count;
    }

    /**
     * Get tag code
     *
     * @return Tag code
     */
    int tagCode() {
        if (this.tag != null) {
            return this.tag.code();
        }
        return this.id;
    }

    /**
     * Get value
     *
     * @return Value
     */
    int value() {
        if (this.tagValue != null) {
            return this.tagValue.value();
        }
        return this.value[0];
    }

    /**
     * Get value
     *
     * @param idx Index
     * @return Value
     */
    int value(int idx) {
        if (idx == 0) {
            return value();
        }
        return this.value[idx];
    }

    /**
     * Get double value
     *
     * @param idx Index
     * @return Double value
     */
    double valueD(int idx) {
        return this.dvalue[idx];
    }

    /**
     * Get string value
     *
     * @return String value
     */
    String valueString() {
        return this.valueS;
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
        StringBuilder sbuf = new StringBuilder();
        if (this.tag != null) {
            sbuf.append(new StringBuilder().append(" geoKey = ").append(this.tag).toString());
        } else {
            sbuf.append(new StringBuilder().append(" geoKey = ").append(this.id).toString());
        }
        if (this.tagValue != null) {
            sbuf.append(new StringBuilder().append(" value = ").append(this.tagValue).toString());
        } else {
            sbuf.append(" values = ");

            if (this.valueS != null) {
                sbuf.append(this.valueS);
            } else if (this.isDouble) {
                for (int i = 0; i < this.count; i++) {
                    sbuf.append(new StringBuilder().append(this.dvalue[i]).append(" ").toString());
                }
            } else {
                for (int i = 0; i < this.count; i++) {
                    sbuf.append(new StringBuilder().append(this.value[i]).append(" ").toString());
                }
            }
        }
        return sbuf.toString();
    }
    // </editor-fold>

    static class TagValue
            implements Comparable {

        private static HashMap map = new HashMap();
        public static final TagValue ModelType_Projected = new TagValue(GeoKey.Tag.GTModelTypeGeoKey, "Projected", 1);
        public static final TagValue ModelType_Geographic = new TagValue(GeoKey.Tag.GTModelTypeGeoKey, "Geographic", 2);
        public static final TagValue ModelType_Geocentric = new TagValue(GeoKey.Tag.GTModelTypeGeoKey, "Geocentric", 3);
        public static final TagValue RasterType_Area = new TagValue(GeoKey.Tag.GTRasterTypeGeoKey, "Area", 1);
        public static final TagValue RasterType_Point = new TagValue(GeoKey.Tag.GTRasterTypeGeoKey, "Point", 2);
        public static final TagValue GeographicType_Clarke1866 = new TagValue(GeoKey.Tag.GeographicTypeGeoKey, "Clarke1866", 4008);
        public static final TagValue GeographicType_GRS_80 = new TagValue(GeoKey.Tag.GeographicTypeGeoKey, "GRS_80", 4019);
        public static final TagValue GeographicType_Sphere = new TagValue(GeoKey.Tag.GeographicTypeGeoKey, "Sphere", 4035);
        public static final TagValue GeographicType_NAD83 = new TagValue(GeoKey.Tag.GeographicTypeGeoKey, "GCS_NAD83", 4269);
        public static final TagValue GeographicType_WGS_84 = new TagValue(GeoKey.Tag.GeographicTypeGeoKey, "WGS_84", 4326);
        public static final TagValue GeographicType_GCS_NAD27 = new TagValue(GeoKey.Tag.GeographicTypeGeoKey, "GCS_NAD27", 4267);
        public static final TagValue GeogGeodeticDatum_WGS_84 = new TagValue(GeoKey.Tag.GeogGeodeticDatumGeoKey, "WGS_84", 4326);
        public static final TagValue GeogPrimeMeridian_GREENWICH = new TagValue(GeoKey.Tag.GeogPrimeMeridianGeoKey, "Greenwich", 8901);
        public static final TagValue ProjectedCSType_UserDefined = new TagValue(GeoKey.Tag.ProjectedCSTypeGeoKey, "UserDefined", 32767);
        public static final TagValue ProjCoordTrans_LambertConfConic_2SP = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "LambertConfConic_2SP", 8);
        public static final TagValue ProjCoordTrans_LambertConfConic_1SP = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "LambertConfConic_1SP", 9);
        public static final TagValue ProjCoordTrans_Stereographic = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "Stereographic", 14);
        public static final TagValue ProjCoordTrans_TransverseMercator = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "TransverseMercator", 1);
        public static final TagValue ProjCoordTrans_AlbersConicalEqualArea = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "AlbersConicalEqualArea", 11);
        public static final TagValue ProjCoordTrans_AlbersEqualAreaEllipse = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "AlbersEqualAreaEllipse", 11);
        public static final TagValue ProjCoordTrans_Mercator = new TagValue(GeoKey.Tag.ProjCoordTransGeoKey, "Mercator", 7);
        public static final TagValue ProjLinearUnits_METER = new TagValue(GeoKey.Tag.ProjLinearUnitsGeoKey, "Meter", 9001);
        public static final TagValue GeogAngularUnits_DEGREE = new TagValue(GeoKey.Tag.GeogAngularUnitsGeoKey, "Degree", 9102);
        public static final TagValue GeogGeodeticDatum6267 = new TagValue(GeoKey.Tag.GeogGeodeticDatumGeoKey, "North_American_1927", 6267);
        private GeoKey.Tag tag;
        private String name;
        private int value;

        static TagValue get(GeoKey.Tag tag, int code) {
            if (tag == null) {
                return null;
            }
            return (TagValue) map.get(tag.name + code);
        }

        private TagValue(GeoKey.Tag tag, String name, int value) {
            this.tag = tag;
            this.name = name;
            this.value = value;
            map.put(tag.name + value, this);
        }

        public GeoKey.Tag tag() {
            return this.tag;
        }

        public int value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.value + " (" + this.name + ")";
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof TagValue)) {
                return 0;
            }
            int ret = this.tag.compareTo(o);
            if (ret != 0) {
                return ret;
            }
            return this.value - ((TagValue) o).value;
        }
    }

    static class Tag
            implements Comparable {

        private static HashMap map = new HashMap();
        public static final Tag GTModelTypeGeoKey = new Tag("GTModelTypeGeoKey", 1024);
        public static final Tag GTRasterTypeGeoKey = new Tag("GTRasterTypeGeoKey", 1025);
        public static final Tag GTCitationGeoKey = new Tag("GTCitationGeoKey", 1026);
        public static final Tag GeographicTypeGeoKey = new Tag("GeographicTypeGeoKey", 2048);
        public static final Tag GeogCitationGeoKey = new Tag("GeogCitationGeoKey", 2049);
        public static final Tag GeogGeodeticDatumGeoKey = new Tag("GeogGeodeticDatumGeoKey", 2050);
        public static final Tag GeogPrimeMeridianGeoKey = new Tag("GeogPrimeMeridianGeoKey", 2051);
        public static final Tag GeogLinearUnitsGeoKey = new Tag("GeogLinearUnitsGeoKey", 2052);
        public static final Tag GeogAngularUnitsGeoKey = new Tag("GeogAngularUnitsGeoKey", 2054);
        public static final Tag GeogAngularUnitsSizeGeoKey = new Tag("GeogAngularUnitsSizeGeoKey", 2055);
        public static final Tag GeogSemiMajorAxisGeoKey = new Tag("GeogSemiMajorAxisGeoKey", 2056);
        public static final Tag GeogSemiMinorAxisGeoKey = new Tag("GeogSemiMinorAxisGeoKey", 2057);
        public static final Tag GeogInvFlatteningGeoKey = new Tag("GeogInvFlatteningGeoKey", 2058);
        public static final Tag GeogAzimuthUnitsGeoKey = new Tag("GeogAzimuthUnitsGeoKey", 2060);
        public static final Tag ProjectedCSTypeGeoKey = new Tag("ProjectedCSTypeGeoKey,", 3072);
        public static final Tag PCSCitationGeoKey = new Tag("PCSCitationGeoKey,", 3073);
        public static final Tag ProjectionGeoKey = new Tag("ProjectionGeoKey,", 3074);
        public static final Tag ProjCoordTransGeoKey = new Tag("ProjCoordTransGeoKey", 3075);
        public static final Tag ProjLinearUnitsGeoKey = new Tag("ProjLinearUnitsGeoKey", 3076);
        public static final Tag ProjLinearUnitsSizeGeoKey = new Tag("ProjLinearUnitsSizeGeoKey", 3077);
        public static final Tag ProjStdParallel1GeoKey = new Tag("ProjStdParallel1GeoKey", 3078);
        public static final Tag ProjStdParallel2GeoKey = new Tag("ProjStdParallel2GeoKey", 3079);
        public static final Tag ProjNatOriginLongGeoKey = new Tag("ProjNatOriginLongGeoKey", 3080);
        public static final Tag ProjNatOriginLatGeoKey = new Tag("ProjNatOriginLatGeoKey", 3081);
        public static final Tag ProjFalseEastingGeoKey = new Tag("ProjFalseEastingGeoKey", 3082);
        public static final Tag ProjFalseNorthingGeoKey = new Tag("ProjFalseNorthingGeoKey", 3083);
        public static final Tag ProjFalseOriginLongGeoKey = new Tag("ProjFalseOriginLongGeoKey", 3084);
        public static final Tag ProjFalseOriginLatGeoKey = new Tag("ProjFalseOriginLatGeoKey", 3085);
        public static final Tag ProjFalseOriginEastingGeoKey = new Tag("ProjFalseOriginEastingGeoKey", 3086);
        public static final Tag ProjFalseOriginNorthingGeoKey = new Tag("ProjFalseOriginNorthingGeoKey", 3087);
        public static final Tag ProjCenterLongGeoKey = new Tag("ProjCenterLongGeoKey", 3088);
        public static final Tag ProjCenterLatGeoKey = new Tag("ProjCenterLatGeoKey", 3089);
        public static final Tag ProjCenterEastingGeoKey = new Tag("ProjCenterEastingGeoKey", 3090);
        public static final Tag ProjCenterNorthingGeoKey = new Tag("ProjCenterNorthingGeoKey", 3091);
        public static final Tag ProjScaleAtNatOriginGeoKey = new Tag("ProjScaleAtNatOriginGeoKey", 3092);
        public static final Tag ProjScaleAtCenterGeoKey = new Tag("ProjScaleAtCenterGeoKey", 3093);
        public static final Tag ProjAzimuthAngleGeoKey = new Tag("ProjAzimuthAngleGeoKey", 3094);
        public static final Tag ProjStraightVertPoleLongGeoKey = new Tag("ProjStraightVertPoleLongGeoKey", 3095);
        public static final Tag VerticalCSTypeGeoKey = new Tag("VerticalCSTypeGeoKey", 4096);
        public static final Tag VerticalCitationGeoKey = new Tag("VerticalCitationGeoKey", 4097);
        public static final Tag VerticalDatumGeoKey = new Tag("VerticalDatumGeoKey", 4098);
        public static final Tag VerticalUnitsGeoKey = new Tag("VerticalUnitsGeoKey", 4099);
        public static final Tag GeoKey_ProjCoordTrans = new Tag("GeoKey_ProjCoordTrans", 3075);
        public static final Tag GeoKey_ProjStdParallel1 = new Tag("GeoKey_ProjStdParallel1", 3078);
        public static final Tag GeoKey_ProjStdParallel2 = new Tag("GeoKey_ProjStdParallel2", 3079);
        public static final Tag GeoKey_ProjNatOriginLong = new Tag("GeoKey_ProjNatOriginLong", 3080);
        public static final Tag GeoKey_ProjNatOriginLat = new Tag("GeoKey_ProjNatOriginLat", 3081);
        public static final Tag GeoKey_ProjCenterLong = new Tag("GeoKey_ProjCenterLong", 3088);
        public static final Tag GeoKey_ProjFalseEasting = new Tag("GeoKey_ProjFalseEasting", 3082);
        public static final Tag GeoKey_ProjFalseNorthing = new Tag("GeoKey_ProjFalseNorthing", 3083);
        public static final Tag GeoKey_ProjFalseOriginLong = new Tag("GeoKey_ProjFalseOriginLong", 3084);
        public static final Tag GeoKey_ProjFalseOriginLat = new Tag("GeoKey_ProjFalseOriginLat", 3085);
        String name;
        int code;

        static Tag get(int code) {
            return (Tag) map.get(new Integer(code));
        }

        static Tag getOrMake(int code) {
            Tag tag = get(code);
            return tag != null ? tag : new Tag(code);
        }

        private Tag(String name, int code) {
            this.name = name;
            this.code = code;
            map.put(new Integer(code), this);
        }

        Tag(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }

        @Override
        public String toString() {
            return this.code + " (" + this.name + ")";
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof Tag)) {
                return 0;
            }
            Tag to = (Tag) o;
            return this.code - to.code;
        }
    }
}
