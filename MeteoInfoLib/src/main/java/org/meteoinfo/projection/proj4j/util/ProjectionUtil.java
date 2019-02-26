package org.meteoinfo.projection.proj4j.util;

import org.meteoinfo.projection.proj4j.ProjCoordinate;

public class ProjectionUtil 
{
  public static String toString(ProjCoordinate p)
  {
    return "[" + p.x + ", " + p.y + "]";
  }

}
