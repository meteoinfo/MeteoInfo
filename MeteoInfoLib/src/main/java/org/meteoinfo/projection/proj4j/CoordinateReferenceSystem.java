package org.meteoinfo.projection.proj4j;

import org.meteoinfo.projection.proj4j.datum.Datum;
import org.meteoinfo.projection.proj4j.datum.Ellipsoid;
import org.meteoinfo.projection.proj4j.proj.LongLatProjection;
import org.meteoinfo.projection.proj4j.proj.Projection;
import org.meteoinfo.projection.proj4j.units.Unit;
import org.meteoinfo.projection.proj4j.units.Units;

/**
 * Represents a projected or geodetic geospatial coordinate system, to which
 * coordinates may be referenced. A coordinate system is defined by the
 * following things: <ul> <li>an {@link Ellipsoid} specifies how the shape of
 * the Earth is approximated <li>a {@link Datum} provides the mapping from the
 * ellipsoid to actual locations on the earth <li>a {@link Projection} method
 * maps the ellpsoidal surface to a planar space. (The projection method may be
 * null in the case of geodetic coordinate systems). <li>a {@link Unit}
 * indicates how the ordinate values of coordinates are interpreted </ul>
 *
 * @author Martin Davis
 *
 * @see CRSFactory
 *
 */
public class CoordinateReferenceSystem {
    // allows specifying transformations which convert to/from Geographic coordinates on the same datum

    public static final CoordinateReferenceSystem CS_GEO = new CoordinateReferenceSystem("CS_GEO", null, null, null);
    //TODO: add metadata like authority, id, name, parameter string, datum, ellipsoid, datum shift parameters
    private String name;
    private String[] params;
    private Datum datum;
    private Projection proj;

    public CoordinateReferenceSystem(String name, String[] params, Datum datum, Projection proj) {
        this.name = name;
        this.params = params;
        this.datum = datum;
        this.proj = proj;

        if (name == null) {
            String projName = "null-proj";
            if (proj != null) {
                projName = proj.getName();
            }
            this.name = projName + "-CS";
        }
    }

    public String getName() {
        return name;
    }

    public String[] getParameters() {
        return params;
    }

    public Datum getDatum() {
        return datum;
    }

    public Projection getProjection() {
        return proj;
    }

    public String getParameterString() {
        if (params == null) {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            buf.append(params[i]);
            buf.append(" ");
        }
        return buf.toString();
    }

    /**
     * To Esri projection string
     *
     * @return Esri projection string
     */
    public String toEsriString() {
        String result = "";
        String geoName = "GCS_WGS_1984";
        if (proj.getProj4Name().equals("longlat")) {
            result = "GEOGCS[\"" + geoName + "\"," + datum.toEsriString() + "," + "PRIMEM[\"Greenwich\",0.0]"
                    + "," + "UNIT[\"Degree\",0.0174532925199433]" + "]";
            return result;
        } else {
            String name = "Custom";
            result = "PROJCS[\"" + name + "\"," + "GEOGCS[\"" + geoName + "\"," + datum.toEsriString() + ","
                    + "PRIMEM[\"Greenwich\",0.0]" + "," + "UNIT[\"Degree\",0.0174532925199433]" + "]" + ", ";
        }

        if (proj != null) {
            result += "PROJECTION[\"" + proj.getName() + "\"],";
        }
        result += "PARAMETER[\"False_Easting\"," + String.valueOf(proj.getFalseEasting()) + "],";
        result += "PARAMETER[\"False_Northing\"," + String.valueOf(proj.getFalseNorthing()) + "],";
        result += "PARAMETER[\"Central_Meridian\"," + String.valueOf(proj.getProjectionLongitudeDegrees()) + "],";
        result += "PARAMETER[\"Standard_Parallel_1\"," + String.valueOf(proj.getProjectionLatitude1Degrees()) + "],";
        result += "PARAMETER[\"Standard_Parallel_2\"," + String.valueOf(proj.getProjectionLatitude2Degrees()) + "],";
        result += "PARAMETER[\"Scale_Factor\"," + String.valueOf(proj.getScaleFactor()) + "],";
        result += "PARAMETER[\"Latitude_Of_Origin\"," + String.valueOf(proj.getProjectionLatitudeDegrees()) + "],";
        result += "UNIT[\"Meter\",1.0]]";
        return result;
    }

    /**
     * Creates a geographic (unprojected) {@link CoordinateReferenceSystem}
     * based on the {@link Datum} of this CRS. This is useful for defining
     * {@link CoordinateTransform}s to and from geographic coordinate systems,
     * where no datum transformation is required. The {@link Units} of the
     * geographic CRS are set to {@link Units#DEGREES}.
     *
     * @return a geographic CoordinateReferenceSystem based on the datum of this
     * CRS
     */
    public CoordinateReferenceSystem createGeographic() {
        Datum datum = getDatum();
        Projection geoProj = new LongLatProjection();
        geoProj.setEllipsoid(getProjection().getEllipsoid());
        geoProj.setUnits(Units.DEGREES);
        geoProj.initialize();
        return new CoordinateReferenceSystem("GEO-" + datum.getCode(), null, datum, geoProj);
    }

    @Override
    public String toString() {
        return name;
    }
}
