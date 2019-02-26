package org.meteoinfo.projection.proj4j.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.meteoinfo.projection.proj4j.CoordinateReferenceSystem;
import org.meteoinfo.projection.proj4j.InvalidValueException;
import org.meteoinfo.projection.proj4j.Registry;

import org.meteoinfo.projection.proj4j.datum.Datum;
import org.meteoinfo.projection.proj4j.datum.Ellipsoid;
import org.meteoinfo.projection.proj4j.proj.Projection;
import org.meteoinfo.projection.proj4j.proj.TransverseMercatorProjection;
import org.meteoinfo.projection.proj4j.units.Angle;
import org.meteoinfo.projection.proj4j.units.Unit;
import org.meteoinfo.projection.proj4j.units.Units;
import org.meteoinfo.projection.proj4j.util.ProjectionMath;

public class Proj4Parser {

    private Registry registry;

    public Proj4Parser(Registry registry) {
        this.registry = registry;
    }

    public CoordinateReferenceSystem parse(String name, String[] args) {
        if (args == null) {
            return null;
        }

        Map params = createParameterMap(args);
        Proj4Keyword.checkUnsupported(params.keySet());
        DatumParameters datumParam = new DatumParameters();
        parseDatum(params, datumParam);
        parseEllipsoid(params, datumParam);
        Datum datum = datumParam.getDatum();
        Ellipsoid ellipsoid = datum.getEllipsoid();
        // TODO: this makes a difference - why?
        // which is better?
//    Ellipsoid ellipsoid = datumParam.getEllipsoid(); 
        Projection proj = parseProjection(params, ellipsoid);
        return new CoordinateReferenceSystem(name, args, datum, proj);
    }

    public CoordinateReferenceSystem parseEsri(String esriString) {
        Map params = esriStringToProj4Params(esriString);
        Proj4Keyword.checkUnsupported(params.keySet());
        DatumParameters datumParam = new DatumParameters();
        parseDatum(params, datumParam);
        parseEllipsoid(params, datumParam);
        Datum datum = datumParam.getDatum();
        Ellipsoid ellipsoid = datum.getEllipsoid();
        // TODO: this makes a difference - why?
        // which is better?
//    Ellipsoid ellipsoid = datumParam.getEllipsoid(); 
        Projection proj = parseProjection(params, ellipsoid);
        String[] args = this.getParameterArray(params);
        return new CoordinateReferenceSystem("Custom", args, datum, proj);
    }

    public Map esriStringToProj4Params(String esriString) {
        Map params = new HashMap();
        String key, value, name;
        int iStart, iEnd;

        //Projection
        if (!esriString.contains("PROJCS")) {
            key = Proj4Keyword.proj;
            value = "longlat";
            params.put(key, value);
        } else {
            Projection projection = null;
            iStart = esriString.indexOf("PROJECTION") + 12;
            iEnd = esriString.indexOf("]", iStart) - 1;
            String s = esriString.substring(iStart, iEnd);
            if (s != null) {
                projection = registry.getProjectionEsri(s);
                if (projection == null) {
                    throw new InvalidValueException("Unknown projection: " + s);
                }
            }

            String proj4Name = projection.getProj4Name();
            key = Proj4Keyword.proj;
            value = proj4Name;
            params.put(key, value);
        }

        //Datum
        if (esriString.contains("DATUM")) {
            iStart = esriString.indexOf("DATUM") + 7;
            iEnd = esriString.indexOf(",", iStart) - 1;
            if (iEnd > iStart) {
                key = Proj4Keyword.datum;
                value = esriString.substring(iStart, iEnd);
                if (value.equals("D_WGS_1984")) {
                    value = "WGS84";
                } else {
                    value = "WGS84";
                }
                params.put(key, value);
            }
        }

        //Ellipsoid
        if (esriString.contains("SPHEROID")) {
            iStart = esriString.indexOf("SPHEROID") + 9;
            iEnd = esriString.indexOf("]", iStart);
            if (iEnd > iStart) {
                String extracted = esriString.substring(iStart, iEnd);
                String[] terms = extracted.split(",");
                name = terms[0];
                name = name.substring(1, name.length() - 1);
                if (name.equals("WGS_1984")) {
                    name = "WGS84";
                } else {
                    name = "WGS84";
                }
                key = Proj4Keyword.ellps;
                value = name;
                params.put(key, value);
                key = Proj4Keyword.a;
                value = terms[1];
                params.put(key, value);
                key = Proj4Keyword.rf;
                value = terms[2];
                params.put(key, value);
            }
        }

//        //Primem
//        if (esriString.contains("PRIMEM")) {
//            iStart = esriString.indexOf("PRIMEM") + 7;
//            iEnd = esriString.indexOf("]", iStart);
//            if (iEnd > iStart) {
//                String extracted = esriString.substring(iStart, iEnd);
//                String[] terms = extracted.split(",");
//                name = terms[0];
//                name = name.substring(1, name.length() - 1);
//                key = Proj4Keyword.pm;
//                value = terms[1];
//                params.put(key, value);
//            }
//        }

        //Projection parameters
        value = getParameter("False_Easting", esriString);
        if (value != null) {
            key = Proj4Keyword.x_0;
            params.put(key, value);
        }
        value = getParameter("False_Northing", esriString);
        if (value != null) {
            key = Proj4Keyword.y_0;
            params.put(key, value);
        }
        value = getParameter("Central_Meridian", esriString);
        if (value != null) {
            key = Proj4Keyword.lon_0;
            params.put(key, value);
        }
        value = getParameter("Standard_Parallel_1", esriString);
        if (value != null) {
            key = Proj4Keyword.lat_1;
            params.put(key, value);
        }
        value = getParameter("Standard_Parallel_2", esriString);
        if (value != null) {
            key = Proj4Keyword.lat_2;
            params.put(key, value);
        }
        value = getParameter("Scale_Factor", esriString);
        if (value != null) {
            key = Proj4Keyword.k_0;
            params.put(key, value);
        }
        value = getParameter("Latitude_Of_Origin", esriString);
        if (value != null) {
            key = Proj4Keyword.lat_0;
            params.put(key, value);
        }

        //Unit

        return params;
    }

    /*
  
     // not currently used
     private final static double SIXTH = .1666666666666666667; // 1/6 
     private final static double RA4 = .04722222222222222222; // 17/360 
     private final static double RA6 = .02215608465608465608; // 67/3024 
     private final static double RV4 = .06944444444444444444; // 5/72 
     private final static double RV6 = .04243827160493827160; // 55/1296 
     */
    /**
     * Creates a {@link Projection} initialized from a PROJ.4 argument list.
     */
    private Projection parseProjection(Map params, Ellipsoid ellipsoid) {
        Projection projection = null;

        String s;
        s = (String) params.get(Proj4Keyword.proj);
        if (s != null) {
            projection = registry.getProjection(s);
            if (projection == null) {
                throw new InvalidValueException("Unknown projection: " + s);
            }
        }

        projection.setEllipsoid(ellipsoid);

        // not sure what CSes use this??
   /*
         s = (String)params.get( "init" );
         if ( s != null ) {
         projection = createFromName( s ).getProjection();
         if ( projection == null )
         throw new ProjectionException( "Unknown projection: "+s );
         a = projection.getEquatorRadius();
         es = projection.getEllipsoid().getEccentricitySquared();
         }
         */


        //TODO: better error handling for things like bad number syntax.  
        // Should be able to report the original param string in the error message
        // Also should the exception be lib specific?  (Say ParseException)

        s = (String) params.get(Proj4Keyword.alpha);
        if (s != null) {
            projection.setAlphaDegrees(Double.parseDouble(s));
        }

        s = (String) params.get(Proj4Keyword.lonc);
        if (s != null) {
            projection.setLonCDegrees(Double.parseDouble(s));
        }

        s = (String) params.get(Proj4Keyword.lat_0);
        if (s != null) {
            projection.setProjectionLatitudeDegrees(parseAngle(s));
        }

        s = (String) params.get(Proj4Keyword.lon_0);
        if (s != null) {
            projection.setProjectionLongitudeDegrees(parseAngle(s));
        }

        s = (String) params.get(Proj4Keyword.lat_1);
        if (s != null) {
            projection.setProjectionLatitude1Degrees(parseAngle(s));
        }

        s = (String) params.get(Proj4Keyword.lat_2);
        if (s != null) {
            projection.setProjectionLatitude2Degrees(parseAngle(s));
        }

        s = (String) params.get(Proj4Keyword.lat_ts);
        if (s != null) {
            projection.setTrueScaleLatitudeDegrees(parseAngle(s));
        }

        s = (String) params.get(Proj4Keyword.x_0);
        if (s != null) {
            projection.setFalseEasting(Double.parseDouble(s));
        }

        s = (String) params.get(Proj4Keyword.y_0);
        if (s != null) {
            projection.setFalseNorthing(Double.parseDouble(s));
        }

        s = (String) params.get(Proj4Keyword.k_0);
        if (s == null) {
            s = (String) params.get(Proj4Keyword.k);
        }
        if (s != null) {
            projection.setScaleFactor(Double.parseDouble(s));
        }

        s = (String) params.get(Proj4Keyword.units);
        if (s != null) {
            Unit unit = Units.findUnits(s);
            // TODO: report unknown units name as error
            if (unit != null) {
                projection.setFromMetres(1.0 / unit.value);
                projection.setUnits(unit);
            }
        }

        s = (String) params.get(Proj4Keyword.to_meter);
        if (s != null) {
            projection.setFromMetres(1.0 / Double.parseDouble(s));
        }
        
        s = (String) params.get(Proj4Keyword.h);
        if (s != null) {
            projection.setHeightOfOrbit(Double.parseDouble(s));
        }

        if (params.containsKey(Proj4Keyword.south)) {
            projection.setSouthernHemisphere(true);
        }

        //TODO: implement some of these parameters ?

        // this must be done last, since behaviour depends on other params being set (eg +south)
        if (projection instanceof TransverseMercatorProjection) {
            s = (String) params.get(Proj4Keyword.zone);
            if (s != null) {
                ((TransverseMercatorProjection) projection).setUTMZone(Integer
                        .parseInt(s));
            }
        }

        projection.initialize();

        return projection;
    }

    private void parseDatum(Map params, DatumParameters datumParam) {
        String towgs84 = (String) params.get(Proj4Keyword.towgs84);
        if (towgs84 != null) {
            double[] datumConvParams = parseToWGS84(towgs84);
            datumParam.setDatumTransform(datumConvParams);
        }

        String code = (String) params.get(Proj4Keyword.datum);
        if (code != null) {
            Datum datum = registry.getDatum(code);
            if (datum == null) {
                throw new InvalidValueException("Unknown datum: " + code);
            }
            datumParam.setDatum(datum);
        }

    }

    private double[] parseToWGS84(String paramList) {
        String[] numStr = paramList.split(",");

        if (!(numStr.length == 3 || numStr.length == 7)) {
            throw new InvalidValueException("Invalid number of values (must be 3 or 7) in +towgs84: " + paramList);
        }
        double[] param = new double[numStr.length];
        for (int i = 0; i < numStr.length; i++) {
            // TODO: better error reporting
            param[i] = Double.parseDouble(numStr[i]);
        }
        if (param.length > 3) {
            // optimization to detect 3-parameter transform
            if (param[3] == 0.0
                    && param[4] == 0.0
                    && param[5] == 0.0
                    && param[6] == 0.0) {
                param = new double[]{param[0], param[1], param[2]};
            }
        }

        /**
         * PROJ4 towgs84 7-parameter transform uses units of arc-seconds for the
         * rotation factors, and parts-per-million for the scale factor. These
         * need to be converted to radians and a scale factor.
         */
        if (param.length > 3) {
            param[3] *= ProjectionMath.SECONDS_TO_RAD;
            param[4] *= ProjectionMath.SECONDS_TO_RAD;
            param[5] *= ProjectionMath.SECONDS_TO_RAD;
            param[6] = (param[6] / ProjectionMath.MILLION) + 1;
        }

        return param;
    }

    private void parseEllipsoid(Map params, DatumParameters datumParam) {
        double b = 0;
        String s;

        /*
         * // not supported by PROJ4 s = (String) params.get(Proj4Param.R); if (s !=
         * null) a = Double.parseDouble(s);
         */

        String code = (String) params.get(Proj4Keyword.ellps);
        if (code != null) {
            Ellipsoid ellipsoid = registry.getEllipsoid(code);
            if (ellipsoid == null) {
                throw new InvalidValueException("Unknown ellipsoid: " + code);
            }
            datumParam.setEllipsoid(ellipsoid);
        }

        /*
         * Explicit parameters override ellps and datum settings
         */
        s = (String) params.get(Proj4Keyword.a);
        if (s != null) {
            double a = Double.parseDouble(s);
            datumParam.setA(a);
        }

        s = (String) params.get(Proj4Keyword.es);
        if (s != null) {
            double es = Double.parseDouble(s);
            datumParam.setES(es);
        }

        s = (String) params.get(Proj4Keyword.rf);
        if (s != null) {
            double rf = Double.parseDouble(s);
            datumParam.setRF(rf);
        }

        s = (String) params.get(Proj4Keyword.f);
        if (s != null) {
            double f = Double.parseDouble(s);
            datumParam.setF(f);
        }

        s = (String) params.get(Proj4Keyword.b);
        if (s != null) {
            b = Double.parseDouble(s);
            datumParam.setB(b);
        }

        if (b == 0) {
            b = datumParam.getA() * Math.sqrt(1. - datumParam.getES());
        }

        parseEllipsoidModifiers(params, datumParam);

        /*
         * // None of these appear to be supported by PROJ4 ??
         * 
         * s = (String)
         * params.get(Proj4Param.R_A); if (s != null && Boolean.getBoolean(s)) { a *=
         * 1. - es * (SIXTH + es * (RA4 + es * RA6)); } else { s = (String)
         * params.get(Proj4Param.R_V); if (s != null && Boolean.getBoolean(s)) { a *=
         * 1. - es * (SIXTH + es * (RV4 + es * RV6)); } else { s = (String)
         * params.get(Proj4Param.R_a); if (s != null && Boolean.getBoolean(s)) { a =
         * .5 * (a + b); } else { s = (String) params.get(Proj4Param.R_g); if (s !=
         * null && Boolean.getBoolean(s)) { a = Math.sqrt(a * b); } else { s =
         * (String) params.get(Proj4Param.R_h); if (s != null &&
         * Boolean.getBoolean(s)) { a = 2. * a * b / (a + b); es = 0.; } else { s =
         * (String) params.get(Proj4Param.R_lat_a); if (s != null) { double tmp =
         * Math.sin(parseAngle(s)); if (Math.abs(tmp) > MapMath.HALFPI) throw new
         * ProjectionException("-11"); tmp = 1. - es * tmp * tmp; a *= .5 * (1. - es +
         * tmp) / (tmp * Math.sqrt(tmp)); es = 0.; } else { s = (String)
         * params.get(Proj4Param.R_lat_g); if (s != null) { double tmp =
         * Math.sin(parseAngle(s)); if (Math.abs(tmp) > MapMath.HALFPI) throw new
         * ProjectionException("-11"); tmp = 1. - es * tmp * tmp; a *= Math.sqrt(1. -
         * es) / tmp; es = 0.; } } } } } } } }
         */
    }

    /**
     * Parse ellipsoid modifiers.
     *
     * @param params
     * @param datumParam
     */
    private void parseEllipsoidModifiers(Map params, DatumParameters datumParam) {
        /**
         * Modifiers are mutually exclusive, so when one is detected method
         * returns
         */
        if (params.containsKey(Proj4Keyword.R_A)) {
            datumParam.setR_A();
            return;
        }

    }

    private Map createParameterMap(String[] args) {
        Map params = new HashMap();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            // strip leading "+" if any
            if (arg.startsWith("+")) {
                arg = arg.substring(1);
            }
            int index = arg.indexOf('=');
            if (index != -1) {
                // param of form pppp=vvvv
                String key = arg.substring(0, index);
                String value = arg.substring(index + 1);
                params.put(key, value);
            } else {
                // param of form ppppp
                //String key = arg.substring(1);
                params.put(arg, null);
            }
        }
        return params;
    }

    private String[] getParameterArray(Map params) {
        String[] args = new String[params.size()];
        int i = 0;
        Set<String> key = params.keySet();
        for (String s : key) {
            args[i] = "+" + s + "=" + params.get(s);
            i += 1;
        }

        return args;
    }

    private String getParameter(String name, String esriString) {
        String result = null;
        String par = "PARAMETER[\"" + name;
        int iStart = esriString.toLowerCase().indexOf(par.toLowerCase());
        if (iStart >= 0) {
            iStart += 13 + name.length();
            int iEnd = esriString.indexOf(",", iStart) - 1;
            result = esriString.substring(iStart, iEnd);
        }
        return result;
    }

    private static double parseAngle(String s) {
        return Angle.parse(s);
    }
}
