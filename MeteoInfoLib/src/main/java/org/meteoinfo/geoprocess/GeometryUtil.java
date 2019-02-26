/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geoprocess;

import java.util.ArrayList;
import java.util.List;
import org.meteoinfo.global.PointD;

/**
 *
 * @author wyq
 */
public class GeometryUtil {
    // <editor-fold desc="Ellipse">
    /**
     * Get ellipse coordinate
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major axis
     * @param b Minor axis
     * @param angle Angle
     * @return Coordinate on the ellipse
     */
    public static PointD getEllipseXY(double x0, double y0, double a, double b, double angle) {
        double rangle = Math.toRadians(angle);
        double x = (a * b) / Math.sqrt(b * b + a * a * Math.tan(rangle) * Math.tan(rangle));
        if (angle > 90 && angle < 270){
            x = -x;
        }
        double y = Math.tan(rangle) * x;
        if (angle > 0 && angle < 180) {
            y = -Math.abs(y);
        }
        
        return new PointD(x + x0, y + y0);
    }
    
    /**
     * Get ellipse coordinates
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major axis
     * @param b Minor axis
     * @param deltaAngle Delta angle
     * @return Coordinate on the ellipse
     */
    public static List<PointD> getEllipseCoordinates(double x0, double y0, double a, double b, double deltaAngle) {
        List<PointD> points = new ArrayList<>();
        for (double angle = 0; angle <= 360; angle += deltaAngle){
            points.add(getEllipseXY(x0, y0, a, b, angle));
        }
        
        return points;
    }
    
    /**
     * Get ellipse coordinates
     * @param x0 Center x
     * @param y0 Center y
     * @param a Major axis
     * @param b Minor axis
     * @return Coordinate on the ellipse
     */
    public static List<PointD> getEllipseCoordinates(double x0, double y0, double a, double b) {
        List<PointD> points = new ArrayList<>();
        double deltaAngle = 1;
        for (double angle = 0; angle <= 360; angle += deltaAngle){
            points.add(getEllipseXY(x0, y0, a, b, angle));
        }
        
        return points;
    }
    // </editor-fold>
}
