/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.shape;

import org.meteoinfo.jts.geom.Geometry;

/**
 *
 * @author Yaqiang Wang
 */
public class ShapeFactory {
    /**
     * Create shape from geometry
     * @param a The geometry
     * @return Shape
     */
    public static Shape createShape(Geometry a){
        switch (a.getGeometryType()){
            case "Point":
            case "MultiPoint":
                return new PointShape(a);
            case "LineString":
            case "MultiLineString":
                return new PolylineShape(a);
            case "Polygon":
            case "MultiPolygon":
                return new PolygonShape(a);                
        }
        return null;
    }
}
