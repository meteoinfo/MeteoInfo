/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geometry.graphic;

import org.meteoinfo.common.Extent3D;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.legend.BreakTypes;
import org.meteoinfo.geometry.legend.ColorBreak;
import org.meteoinfo.geometry.legend.ColorBreakCollection;
import org.meteoinfo.geometry.legend.LegendScheme;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PolygonZ;
import org.meteoinfo.geometry.shape.PolygonZShape;
import org.meteoinfo.geometry.shape.Shape;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection3D extends GraphicCollection{
    
    private boolean fixZ;
    private double zValue;
    private String zdir;
    private List<Number> sePoint;
    protected boolean allQuads;
    protected boolean allTriangle;
    protected boolean allConvexPolygon;
    protected boolean usingLight;
    protected boolean sphere;
    
    /**
     * Constructor
     */
    public GraphicCollection3D(){
        super();
        fixZ = false;
        zdir = "z";
        sePoint = null;
        allQuads = false;
        allTriangle = false;
        allConvexPolygon = false;
        usingLight = true;
        sphere = false;
    }

    /**
     * Set legend scheme
     *
     * @param value Legend scheme
     */
    @Override
    public void setLegendScheme(LegendScheme value) {
        super.setLegendScheme(value);
    }
    
    /**
     * Get if is 3D
     * @return Boolean
     */
    @Override
    public boolean is3D(){
        return true;
    }
    
    /**
     * Get if is fixed z graphics
     * @return Boolean
     */
    public boolean isFixZ(){
        return this.fixZ;
    }
    
    /**
     * Set if is fixed z graphics
     * @param value Boolean
     */
    public void setFixZ(boolean value){
        this.fixZ = value;
    }
    
    /**
     * Get fixed z value
     * @return Fixed z value
     */
    public double getZValue(){
        return this.zValue;
    }
    
    /**
     * Set fixed z value
     * @param value Fixed z value
     */
    public void setZValue(double value){
        this.zValue = value;
    }
    
    /**
     * Get z direction - x, y or z
     * @return Z direction
     */
    public String getZDir(){
        return this.zdir;
    }
    
    /**
     * Set z direction - x, y or z
     * @param value Z direction
     */
    public void setZDir(String value){
        this.zdir = value;
    }
    
    /**
     * Get start and end points [xstart, ystart, xend, yend]
     * @return Start and end points
     */
    public List<Number> getSEPoint(){
        return this.sePoint;
    }
    
    /**
     * Set start and end points
     * @param value Start and end points
     */
    public void setSEPoint(List<Number> value){
        this.sePoint = value;
    }
    
    /**
     * Get is all quads or not
     * @return All quads or not
     */
    public boolean isAllQuads() {
        return this.allQuads;
    }
    
    /**
     * Set is all quads or not
     * @param value All quads or not
     */
    public void setAllQuads(boolean value) {
        this.allQuads = value;
    }
    
    /**
     * Get is all triangle or not
     * @return All triangle or not
     */
    public boolean isAllTriangle() {
        return this.allTriangle;
    }
    
    /**
     * Set is all triangle or not
     * @param value All triangle or not
     */
    public void setAllTriangle(boolean value) {
        this.allTriangle = value;
    }
    
    /**
     * Get is all convex polygon or not
     * @return All convex polygon or not
     */
    public boolean isAllConvexPolygon() {
        if (this.allConvexPolygon) {
            return true;
        } else {
            return this.allQuads || this.allTriangle;
        }
    }
    
    /**
     * Set is all convex polygon or not
     * @param value All convex polygon or not
     */
    public void setAllConvexPolygon(boolean value) {
        this.allConvexPolygon = value;
    }

    /**
     * Get using light or not
     * @return Boolean
     */
    public boolean isUsingLight() {
        return this.usingLight;
    }

    /**
     * Set using light or not
     * @param value Boolean
     */
    public void setUsingLight(boolean value) {
        this.usingLight = value;
    }

    /**
     * Get is render point as sphere or not
     * @return Render point as sphere or not
     */
    public boolean isSphere() {
        return this.sphere;
    }

    /**
     * Set is render point as sphere or not
     * @param value Render point as sphere or not
     */
    public void setSphere(boolean value) {
        this.sphere = value;
    }

    /**
     * Update legend scheme
     *
     * @param value Legend scheme
     */
    public void updateLegendScheme(LegendScheme value) {
        this.setLegendScheme(value);

        if (!this.graphics.isEmpty()) {
            if (this.getGraphicN(0).getLegend().getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                for (Graphic graphic : this.graphics) {
                    ColorBreakCollection cbs = new ColorBreakCollection();
                    Shape shape = graphic.getShape();
                    for (PointZ pointZ : (List<PointZ>) shape.getPoints()) {
                        ColorBreak cb = this.legendScheme.findLegendBreak(pointZ.M);
                        cbs.add(cb);
                    }
                    graphic.setLegend(cbs);
                }
            }
        }
    }

    /**
     * X coordinate shift
     * @param xs X shift value
     * @return Shifted result
     */
    public GraphicCollection3D xShift(double xs) {
        for (Graphic g : this.graphics) {
            for (PointD p : g.getShape().getPoints()) {
                p.X += xs;
            }
            g.setExtent(((Extent3D) g.getExtent()).shift(xs, 0, 0));
        }
        this._extent.shift(xs, 0);

        return this;
    }

    /**
     * X random shift
     * @param exponent Exponent
     * @return Shifted result
     */
    public GraphicCollection3D xRandomShift(int exponent) {
        Random r = new Random();
        for (Graphic g : this.graphics) {
            for (PointD p : g.getShape().getPoints()) {
                p.X += (1 - r.nextDouble()) * Math.pow(10, exponent);
            }
        }

        return this;
    }

    /**
     * Y random shift
     * @param exponent Exponent
     * @return Shifted result
     */
    public GraphicCollection3D yRandomShift(int exponent) {
        Random r = new Random();
        for (Graphic g : this.graphics) {
            for (PointD p : g.getShape().getPoints()) {
                p.Y += (1 - r.nextDouble()) * Math.pow(10, exponent);
            }
        }

        return this;
    }

    /**
     * X or Y random shift
     * @param exponent Exponent
     * @return Shifted result
     */
    public GraphicCollection3D randomShift(int exponent) {
        if (this.zdir.equals("x")) {
            return this.xRandomShift(exponent);
        } else {
            return this.yRandomShift(exponent);
        }
    }

    /**
     * Save to file
     * @param fileName The file name
     * @throws IOException
     */
    public void saveFile(String fileName) throws IOException {
        File file = new File(fileName);
        BufferedWriter sw = new BufferedWriter(new FileWriter(file));
        sw.write("Polygons");
        sw.newLine();
        sw.write(String.valueOf(this.graphics.size()));
        sw.newLine();
        for (Graphic g : this.graphics) {
            Shape shape = g.getShape();
            switch (shape.getShapeType()) {
                case POLYGON_Z:
                    sw.write("Polygon");
                    sw.newLine();
                    PolygonZShape polygonZShape = (PolygonZShape) shape;
                    PolygonZ polygonZ = (PolygonZ) polygonZShape.getPolygons().get(0);
                    sw.write("Color");
                    sw.newLine();
                    Color color = g.getLegend().getColor();
                    sw.write(String.valueOf(color.getRGB()));
                    sw.newLine();
                    sw.write("Outline");
                    sw.newLine();
                    sw.write(String.valueOf(polygonZ.getOutLine().size()));
                    sw.newLine();
                    for (PointZ p : (List<PointZ>)polygonZ.getOutLine()) {
                        sw.write(String.valueOf(p.X) + "," + String.valueOf(p.Y) +
                                "," + String.valueOf(p.Z));
                        sw.newLine();
                    }
                    sw.write(String.valueOf(polygonZ.getHoleLineNumber()));
                    sw.newLine();
                    if (polygonZ.hasHole()) {
                        for (int i = 0; i < polygonZ.getHoleLineNumber(); i++) {
                            List<PointZ> pointZS = (List<PointZ>) polygonZ.getHoleLine(i);
                            sw.write("Hole");
                            sw.newLine();
                            sw.write(String.valueOf(pointZS.size()));
                            sw.newLine();
                            for (PointZ p : pointZS) {
                                sw.write(String.valueOf(p.X) + "," + String.valueOf(p.Y) +
                                        "," + String.valueOf(p.Z));
                                sw.newLine();
                            }
                        }
                    }
                    break;
            }
        }

        sw.flush();
        sw.close();
    }
}
