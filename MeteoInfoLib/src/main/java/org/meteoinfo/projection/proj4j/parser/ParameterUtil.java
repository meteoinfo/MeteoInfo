package org.meteoinfo.projection.proj4j.parser;

import org.meteoinfo.projection.proj4j.units.Angle;
import org.meteoinfo.projection.proj4j.units.AngleFormat;

public class ParameterUtil {

  public static final AngleFormat format = new AngleFormat( AngleFormat.ddmmssPattern, true );

  /**
   * 
   * @param s Angle string
   * @return Angle double value
   * @deprecated
   * @see Angle#parse(String)
   */
  public static double parseAngle( String s ) {
    return format.parse( s, null ).doubleValue();
  }
}
