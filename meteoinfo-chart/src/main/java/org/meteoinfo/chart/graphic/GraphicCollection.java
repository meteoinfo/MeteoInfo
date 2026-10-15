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
package org.meteoinfo.chart.graphic;

import org.meteoinfo.chart.transform.Transform;
import org.meteoinfo.geometry.*;
import org.meteoinfo.geometry.Point;
import org.meteoinfo.geometry.Polygon;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection extends Graphic implements Iterator {

    // <editor-fold desc="Variables">
    protected List<? extends Graphic> graphics = new ArrayList<>();
    protected Extent extent = new Extent();
    protected boolean singleLegend = true;
    protected int index;
    protected LabelSet labelSet;
    protected List<Graphic> labelPoints;
    protected LegendScheme legendScheme;
    protected boolean avoidCollision = false;

    private boolean fixZ;
    private double zValue;
    private String zdir;
    private List<Number> sePoint;
    protected boolean allQuads;
    protected boolean allTriangle;
    protected boolean allConvexPolygon;
    protected boolean usingLight;
    protected boolean sphere;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GraphicCollection() {
        this.index = 0;
        labelSet = new LabelSet();
        labelPoints = new ArrayList<>();

        fixZ = false;
        zdir = "z";
        sePoint = null;
        allQuads = false;
        allTriangle = false;
        allConvexPolygon = false;
        usingLight = true;
        sphere = false;
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get shape
     *
     * @return Shape
     */
    @Override
    public Geometry getGeometry() {
        if (this.graphics.isEmpty()) {
            return null;
        } else {
            return this.graphics.get(0).getGeometry();
        }
    }

    /**
     * Get shape by index
     * @param index The index
     * @return Shape
     */
    public Geometry getShape(int index) {
        if (this.graphics.isEmpty()) {
            return null;
        } else {
            return this.graphics.get(index).getGeometry();
        }
    }

    /**
     * Get graphic list
     *
     * @return Graphic list
     */
    public List<? extends Graphic> getGraphics() {
        return this.graphics;
    }

    /**
     * Set graphic list
     *
     * @param value Graphic list
     */
    public void setGraphics(List<Graphic> value) {
        this.graphics = value;
        this.updateExtent();
        this.updateTransform();
    }

    /**
     * Set transform
     * @param value Transform The transform
     */
    @Override
    public void setTransform(Transform value) {
        super.setTransform(value);
        this.updateTransform();
    }

    /**
     * Get extent
     *
     * @return The extent
     */
    @Override
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set extent
     *
     * @param value Extent
     */
    @Override
    public void setExtent(Extent value) {
        this.extent = value;
    }

    /**
     * Get is single legend or not
     *
     * @return Boolean
     */
    @Override
    public boolean isSingleLegend() {
        return this.singleLegend;
    }

    /**
     * Set is single legend or not
     *
     * @param value Boolean
     */
    public void setSingleLegend(boolean value) {
        this.singleLegend = value;
    }

    /**
     * Get label set
     *
     * @return Label set
     */
    public LabelSet getLabelSet() {
        return labelSet;
    }

    /**
     * Set label set
     *
     * @param ls Label set
     */
    public void setLabelSet(LabelSet ls) {
        labelSet = ls;
    }

    /**
     * Get label points
     *
     * @return The label points
     */
    public List<Graphic> getLabelPoints() {
        return this.labelPoints;
    }

    /**
     * Set label points
     *
     * @param lps The label points
     */
    public void setLabelPoints(List<Graphic> lps) {
        this.labelPoints = lps;
    }

    /**
     * Get legend scheme
     *
     * @return Legend scheme
     */
    public LegendScheme getLegendScheme() {
        return this.legendScheme;
    }

    /**
     * Set legend scheme
     *
     * @param value Legend scheme
     */
    public void setLegendScheme(LegendScheme value) {
        this.legendScheme = value;

        if (!this.graphics.isEmpty()) {
            if (this.getGraphicN(0).getLegendBreak().getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                for (Graphic graphic : this.graphics) {
                    ColorBreakCollection cbs = new ColorBreakCollection();
                    Geometry shape = graphic.getGeometry();
                    for (Coordinate pointZ : (List<Coordinate>) shape.getCoordinates()) {
                        ColorBreak cb = this.legendScheme.findLegendBreak(pointZ.m);
                        cbs.add(cb);
                    }
                    graphic.setLegendBreak(cbs);
                }
            }
        }
    }

    /**
     * Get legend break
     *
     * @return Legend break
     */
    @Override
    public ColorBreak getLegendBreak() {
        if (this.legendBreak == null) {
            return this.get(0).legendBreak;
        } else {
            return this.legendBreak;
        }
    }

    /**
     * Get is 3D or not
     *
     * @return Boolean
     */
    public boolean is3D() {
        return false;
    }

    /**
     * Get if is GraphicCollection
     *
     * @return Boolean
     */
    @Override
    public boolean isCollection() {
        return true;
    }

    /**
     * Return avoid collision or not
     * @return Avoid collision or not
     */
    public boolean isAvoidCollision() {
        return this.avoidCollision;
    }

    /**
     * Set avoid collision or not
     * @param value Avoid collision or not
     */
    public void setAvoidCollision(boolean value) {
        this.avoidCollision = value;
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

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update extent
     */
    public void updateExtent() {
        int i = 0;
        Extent extent;
        for (Graphic g : this.graphics) {
            extent = g.getExtent();
            if (i == 0) {
                this.extent = extent;
            } else {
                this.extent = GeometryUtil.getLagerExtent(this.extent, extent);
            }

            i += 1;
        }
    }

    /**
     * Update transform
     */
    public void updateTransform() {
        for (Graphic graphic : this.graphics) {
            graphic.setTransform(this.transform);
        }
    }

    /**
     * Add a graphic
     *
     * @param graphic The graphic
     * @return Boolean
     */
    public boolean add(Graphic graphic) {
        graphic.transform = this.transform;
        boolean istrue = ((List<Graphic>) this.graphics).add(graphic);

        //Update extent
        if (this.graphics.size() == 1) {
            extent = graphic.getExtent();
        } else {
            extent = GeometryUtil.getLagerExtent(extent, graphic.getExtent());
        }

        return istrue;
    }

    /**
     * Inset a graphic
     *
     * @param index Index
     * @param graphic The graphic
     */
    public void add(int index, Graphic graphic) {
        graphic.transform = this.transform;
        ((List<Graphic>) this.graphics).add(index, graphic);

        //Update extent
        if (this.graphics.size() == 1) {
            extent = graphic.getExtent();
        } else {
            extent = GeometryUtil.getLagerExtent(extent, graphic.getExtent());
        }
    }

    /**
     * Get a graphic by index
     *
     * @param idx Index
     * @return Graphic
     */
    public Graphic get(int idx) {
        return this.graphics.get(idx);
    }

    /**
     * Index of
     *
     * @param g Graphic
     * @return Index
     */
    public int indexOf(Graphic g) {
        return this.graphics.indexOf(g);
    }

    /**
     * Contains or not
     *
     * @param g Graphic
     * @return Boolean
     */
    public boolean contains(Graphic g) {
        return this.graphics.contains(g);
    }

    /**
     * Get graphic list size
     *
     * @return Graphic list size
     */
    public int size() {
        return this.graphics.size();
    }

    /**
     * Get is empty or not
     *
     * @return Boolean
     */
    public boolean isEmpty() {
        return this.graphics.isEmpty();
    }

    /**
     * Get graphics number
     *
     * @return 1
     */
    @Override
    public int getNumGraphics() {
        return this.size();
    }

    /**
     * Get Graphic by index
     *
     * @param idx Index
     * @return Graphic
     */
    @Override
    public Graphic getGraphicN(int idx) {
        return this.get(idx);
    }

    /**
     * Set a graphic
     * @param idx The index
     * @param graphic The graphic
     */
    public void setGraphicN(int idx, Graphic graphic) {
        ((List<Graphic>) this.graphics).set(idx, graphic);
    }

    /**
     * Remove a graphic
     *
     * @param aGraphic The graphic
     * @return Boolean
     */
    public boolean remove(Graphic aGraphic) {
        boolean istrue = this.graphics.remove(aGraphic);
        this.updateExtent();

        return istrue;
    }

    /**
     * Remove a graphic by index
     *
     * @param index The index
     * @return The removed graphic
     */
    public Graphic remove(int index) {
        Graphic ag = this.graphics.remove(index);
        this.updateExtent();

        return ag;
    }

    /**
     * Clear graphics
     */
    public void clear() {
        this.graphics.clear();
    }

    /**
     * Add all
     *
     * @param gs Graphic list
     */
    public void addAll(List<Graphic> gs) {
        ((List<Graphic>) this.graphics).addAll(gs);

        // Update extent
        int i = 0;
        Extent extent;
        for (Graphic g : gs) {
            extent = g.getExtent();
            if (i == 0) {
                this.extent = extent;
            } else {
                this.extent = GeometryUtil.getLagerExtent(this.extent, extent);
            }

            i += 1;
        }
    }

    /**
     * Join this graphics with other graphics
     *
     * @param graphic Other graphics
     */
    public void join(Graphic graphic) {
        if (graphic.isCollection()) {
            //Update extent
            if (this.isEmpty()) {
                extent = graphic.getExtent();
            } else {
                extent = GeometryUtil.getLagerExtent(extent, graphic.getExtent());
            }
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                ((List<Graphic>) this.graphics).add(graphic.getGraphicN(i));
            }
        } else {
            this.add(graphic);
        }
    }

    /**
     * Remove all
     *
     * @param gs Graphic list
     */
    public void removeAll(List<Graphic> gs) {
        this.graphics.removeAll(gs);
    }

    /**
     * Get legend list
     * @return Legend list
     */
    @Override
    public List<? extends ColorBreak> getLegendBreaks() {
        List<ColorBreak> breaks = new ArrayList<>();
        if (this.legendBreak != null) {
            breaks.add(this.getLegendBreak());
        } else {
            if (this.legendScheme != null) {
                breaks.addAll(this.legendScheme.getLegendBreaks());
            } else {
                for (Graphic graphic : this.graphics) {
                    breaks.add(graphic.getLegendBreak());
                }
            }
        }

        return breaks;
    }

    /**
     * Select graphics by an extent
     *
     * @param aExtent The extent
     * @return Selected graphics
     */
    public GraphicCollection selectGraphics(Extent aExtent) {
        GraphicCollection selectedGraphics = new GraphicCollection();
        int i, j;
        Coordinate aPoint = new Coordinate();
        aPoint.x = (aExtent.minX + aExtent.maxX) / 2;
        aPoint.y = (aExtent.minY + aExtent.maxY) / 2;

        for (Graphic aGraphic : this.graphics) {
            switch (aGraphic.getGeometry().getGeometryType()) {
                case POINT:
                case MULTI_POINT:
                    Point aPS = (Point) aGraphic.getGeometry();
                    if (aExtent.contains(aPS.getCoordinate())) {
                        selectedGraphics.add(aGraphic);
                    }
                    break;
                case LINESTRING:
                case MULTI_LINESTRING:
                    MultiLineString aPLS = (MultiLineString) aGraphic.getGeometry();
                    if (GeometryUtil.isExtentCross(aExtent, aPLS.getExtent())) {
                        for (j = 0; j < aPLS.getCoordinates().size(); j++) {
                            aPoint = aPLS.getCoordinates().get(j);
                            if (aExtent.contains(aPoint)) {
                                selectedGraphics.add(aGraphic);
                                break;
                            }
                        }
                    }
                    break;
                case POLYGON:
                case MULTI_POLYGON:
                case RECTANGLE:
                    MultiPolygon aPGS = (MultiPolygon) aGraphic.getGeometry();
                    if (!(aPGS.getNumParts() > 1)) {
                        if (GeoComputation.pointInPolygon(aPGS.getCoordinates(), aPoint)) {
                            selectedGraphics.add(aGraphic);
                        }
                    } else {
                        for (int p = 0; p < aPGS.getNumParts(); p++) {
                            ArrayList pList = new ArrayList();
                            if (p == aPGS.getNumParts() - 1) {
                                for (int pp = aPGS.parts[p]; pp < aPGS.getNumPoints(); pp++) {
                                    pList.add(aPGS.getCoordinates().get(pp));
                                }
                            } else {
                                for (int pp = aPGS.parts[p]; pp < aPGS.parts[p + 1]; pp++) {
                                    pList.add(aPGS.getCoordinates().get(pp));
                                }
                            }
                            if (GeoComputation.pointInPolygon(pList, aPoint)) {
                                selectedGraphics.add(aGraphic);
                                break;
                            }
                        }
                    }
                    break;
            }
        }

        return selectedGraphics;
    }

    @Override
    public boolean hasNext() {
        return index <= this.size() - 1;
    }

    @Override
    public Object next() {
        if (index >= this.size()) {
            throw new NoSuchElementException();
        }

        return this.get(index++);
    }

    /**
     * Add labels
     */
    public void addLabels() {
        addLabelsByColor();
        labelSet.setDrawLabels(true);
    }

    /**
     * Add labels by texts array
     *
     * @param texts Texts array
     */
    public void addLabels(Array texts) {
        addLabelsByColor(texts);
        labelSet.setDrawLabels(true);
    }

    /**
     * Get shapes
     *
     * @return Shapes
     */
    public List<? extends Geometry> getShapes() {
        List<Geometry> shapes = new ArrayList<>();
        for (Graphic g : this.graphics) {
            shapes.add(g.getGeometry());
        }
        return shapes;
    }

    /**
     * Get shape type
     *
     * @return Shape type
     */
    @Override
    public GeometryType getShapeType() {
        if (this.graphics.isEmpty()) {
            return null;
        } else {
            return this.graphics.get(0).getGeometry().getGeometryType();
        }
    }

    private double getMinValue() {
        double min = Double.MAX_VALUE;
        for (Graphic graphic : this.graphics) {
            Geometry shape = graphic.getGeometry();
            if (min > shape.getValue()) {
                min = shape.getValue();
            }
        }
        return min;
    }

    /**
     * Add labels
     */
    protected void addLabelsByColor() {
        if (labelSet.isAutoDecimal()) {
            double min = getMinValue();
            labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
        }
        String dFormat = "%1$." + String.valueOf(labelSet.getDecimalDigits()) + "f";
        Coordinate aPoint;
        for (Graphic graphic : this.graphics) {
            ColorBreak cb = graphic.getLegendBreak();
            Geometry shape = graphic.getGeometry();
            Point aPS = new Point();
            switch (shape.getGeometryType()) {
                case POINT:
                case MULTI_POINT:
                    aPS.setCoordinate((Coordinate) ((Point) shape).getCoordinate().clone());
                    break;
                case LINESTRING:
                case MULTI_LINESTRING:
                    int pIdx = ((MultiLineString) shape).getCoordinates().size() / 2;
                    aPS.setCoordinate((Coordinate) ((MultiLineString) shape).getCoordinates().get(pIdx - 1).clone());
                    break;
                case POLYGON:
                case MULTI_POLYGON:
                    Extent aExtent = shape.getExtent();
                    aPoint = new Coordinate();
                    aPoint.x = ((aExtent.minX + aExtent.maxX) / 2);
                    aPoint.y = ((aExtent.minY + aExtent.maxY) / 2);
                    aPS.setCoordinate(aPoint);
                    break;
            }

            LabelBreak aLP = new LabelBreak();
            //aLP.setText(DataConvert.removeTailingZeros(String.valueOf(shape.getValue())));
            aLP.setText(String.format(dFormat, shape.getValue()));
            if (labelSet.isColorByLegend()) {
                aLP.setColor(cb.getColor());
            } else {
                aLP.setColor(labelSet.getLabelColor());
            }
            aLP.setFont(labelSet.getLabelFont());
            aLP.setAlignType(labelSet.getLabelAlignType());
            aLP.setYShift(labelSet.getYOffset());
            aLP.setXShift(labelSet.getXOffset());
            Graphic aGraphic = new Graphic(aPS, aLP);
            addLabel(aGraphic);
        }
    }

    /**
     * Add labels
     *
     * @param texts Texts array
     */
    protected void addLabelsByColor(Array texts) {
        texts = texts.copyIfView();

        if (labelSet.isAutoDecimal()) {
            double min = getMinValue();
            labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
        }
        String dFormat = "%1$." + String.valueOf(labelSet.getDecimalDigits()) + "f";
        Coordinate aPoint;
        IndexIterator iter = texts.getIndexIterator();
        for (Graphic graphic : this.graphics) {
            if (!iter.hasNext()) {
                break;
            }

            ColorBreak cb = graphic.getLegendBreak();
            Geometry shape = graphic.getGeometry();
            Point aPS = new Point();
            switch (shape.getGeometryType()) {
                case POINT:
                case MULTI_POINT:
                    aPS.setCoordinate((Coordinate) ((Point) shape).getCoordinate().clone());
                    break;
                case LINESTRING:
                case MULTI_LINESTRING:
                    int pIdx = ((MultiLineString) shape).getCoordinates().size() / 2;
                    aPS.setCoordinate((Coordinate) ((MultiLineString) shape).getCoordinates().get(pIdx - 1).clone());
                    break;
                case POLYGON:
                case MULTI_POLYGON:
                    Extent aExtent = shape.getExtent();
                    aPoint = new Coordinate();
                    aPoint.x = ((aExtent.minX + aExtent.maxX) / 2);
                    aPoint.y = ((aExtent.minY + aExtent.maxY) / 2);
                    aPS.setCoordinate(aPoint);
                    break;
            }

            LabelBreak aLP = new LabelBreak();
            aLP.setText(iter.getStringNext());
            if (labelSet.isColorByLegend()) {
                aLP.setColor(cb.getColor());
            } else {
                aLP.setColor(labelSet.getLabelColor());
            }
            aLP.setFont(labelSet.getLabelFont());
            aLP.setAlignType(labelSet.getLabelAlignType());
            aLP.setYShift(labelSet.getYOffset());
            aLP.setXShift(labelSet.getXOffset());
            Graphic aGraphic = new Graphic(aPS, aLP);
            addLabel(aGraphic);
        }
    }

    /**
     * Add label point
     *
     * @param aLP Label point
     */
    public void addLabel(Graphic aLP) {
        labelPoints.add(aLP);
    }

    /**
     * Add labels of contour layer dynamically
     *
     * @param sExtent View extent of MapView
     */
    public void addLabelsContourDynamic(Extent sExtent) {
        if (labelSet.isAutoDecimal()) {
            double min = getMinValue();
            labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
        }
        String dFormat = "%1$." + String.valueOf(labelSet.getDecimalDigits()) + "f";
        String text;
        for (Graphic graphic : this.graphics) {
            Geometry shape = graphic.getGeometry();
            ColorBreak cb = graphic.getLegendBreak();
            MultiLineString aPLS = (MultiLineString) shape;
            Extent IExtent = aPLS.getExtent();
            if (IExtent.maxX - IExtent.minX > (sExtent.maxX - sExtent.minX) / 10
                    || IExtent.maxY - IExtent.minY > (sExtent.maxY - sExtent.minY) / 10) {
                LabelBreak aLP = new LabelBreak();
                int pIdx = aPLS.getCoordinates().size() / 2;
                //PointF aPoint = new PointF(0, 0);
                Point aPS = new Point();
                aPS.setCoordinate(aPLS.getCoordinates().get(pIdx - 1));
                //text = DataConvert.removeTailingZeros(String.valueOf(aPLS.getValue()));
                text = String.format(dFormat, aPLS.getValue());
                aLP.setText(text);
                aLP.setFont(labelSet.getLabelFont());
                aLP.setAlignType(labelSet.getLabelAlignType());
                aLP.setXShift(labelSet.getXOffset());
                aLP.setYShift(labelSet.getYOffset());
                if (labelSet.isColorByLegend()) {
                    aLP.setColor(cb.getColor());
                } else {
                    aLP.setColor(labelSet.getLabelColor());
                }
                Graphic aGraphic = new Graphic(aPS, aLP);
                addLabel(aGraphic);
            }
        }

        labelSet.setDrawLabels(true);
    }

    /**
     * Get a label by text
     *
     * @param text Text
     * @return Label
     */
    public Graphic getLabel(String text) {
        for (Graphic lb : labelPoints) {
            if (((LabelBreak) lb.getLegendBreak()).getText().equals(text)) {
                return lb;
            }
        }
        return null;
    }

    /**
     * Move label
     *
     * @param text Label text
     * @param x X
     * @param y Y
     */
    public void moveLabel(String text, float x, float y) {
        Graphic lb = this.getLabel(text);
        if (lb != null) {
            this.moveLabel(lb, x, y);
        }
    }

    /**
     * Move label
     *
     * @param lb Label
     * @param x X
     * @param y Y
     */
    public void moveLabel(Graphic lb, float x, float y) {
        LabelBreak lbb = (LabelBreak) lb.getLegendBreak();
        lbb.setXShift(lbb.getXShift() + x);
        lbb.setYShift(lbb.getYShift() + y);
    }

    /**
     * Remove all labels
     */
    public void removeLabels() {
        labelPoints.clear();
        labelSet.setDrawLabels(false);
    }

    /**
     * Get arrow zoom
     *
     * @return Arrow zoom
     */
    public float getArrowZoom() {
        if (this.getLegendBreak().getBreakType() == BreakTypes.POINT_BREAK) {
            float size = ((PointBreak) this.getLegendBreak()).getSize();
            return size / 10;
        }

        return 1.0f;
    }

    /**
     * Clip
     *
     * @param clipPolys Clipping polygons
     * @return Clipped graphics
     */
    public GraphicCollection clip(List<MultiPolygon> clipPolys) {
        GraphicCollection cgraphics = new GraphicCollection();
        for (MultiPolygon aPGS : clipPolys) {
            for (int i = 0; i < this.graphics.size(); i++) {
                Geometry bShape = this.graphics.get(i).getGeometry();
                Geometry clipShape = bShape.intersection(aPGS);
                if (clipShape != null) {
                    cgraphics.add(new Graphic(clipShape, this.graphics.get(i).getLegendBreak()));
                }
            }
        }
        cgraphics.setSingleLegend(this.singleLegend);
        cgraphics.setLegendScheme((LegendScheme) this.getLegendScheme().clone());

        return cgraphics;
    }

    /**
     * X coordinate shift
     * @param xs X shift value
     */
    public GraphicCollection xShift(double xs) {
        for (Graphic g : this.graphics) {
            for (Coordinate p : g.getGeometry().getCoordinates()) {
                p.x += xs;
            }
            g.setExtent(g.getExtent().shift(xs, 0));
        }
        this.extent.shift(xs, 0);

        return this;
    }

    /**
     * X coordinate shift as a new GraphicCollection object
     * @param xs X shift value
     */
    public GraphicCollection xShiftCopy(double xs) {
        GraphicCollection graphicCollection = new GraphicCollection();
        for (Graphic g : this.graphics) {
            Geometry shape = (Geometry) g.getGeometry().clone();
            for (Coordinate p : shape.getCoordinates()) {
                p.x += xs;
            }
            Graphic graphic = new Graphic(shape, g.legendBreak);
            graphic.setExtent(graphic.getExtent().shift(xs, 0));
            graphicCollection.add(graphic);
        }

        return graphicCollection;
    }

    /**
     * X random shift
     * @param exponent Exponent
     * @return Shifted result
     */
    public GraphicCollection xRandomShift(int exponent) {
        Random r = new Random();
        for (Graphic g : this.graphics) {
            for (Coordinate p : g.getGeometry().getCoordinates()) {
                p.x += (1 - r.nextDouble()) * Math.pow(10, exponent);
            }
        }

        return this;
    }

    /**
     * Y random shift
     * @param exponent Exponent
     * @return Shifted result
     */
    public GraphicCollection yRandomShift(int exponent) {
        Random r = new Random();
        for (Graphic g : this.graphics) {
            for (Coordinate p : g.getGeometry().getCoordinates()) {
                p.y += (1 - r.nextDouble()) * Math.pow(10, exponent);
            }
        }

        return this;
    }

    /**
     * X or Y random shift
     * @param exponent Exponent
     * @return Shifted result
     */
    public GraphicCollection randomShift(int exponent) {
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
            Geometry shape = g.getGeometry();
            switch (shape.getGeometryType()) {
                case POLYGON:
                case MULTI_POLYGON:
                    sw.write("Polygon");
                    sw.newLine();
                    MultiPolygon polygonShape = (MultiPolygon) shape;
                    Polygon polygonZ = polygonShape.getPolygons().get(0);
                    sw.write("Color");
                    sw.newLine();
                    Color color = g.getLegendBreak().getColor();
                    sw.write(String.valueOf(color.getRGB()));
                    sw.newLine();
                    sw.write("Outline");
                    sw.newLine();
                    sw.write(String.valueOf(polygonZ.getOutLine().size()));
                    sw.newLine();
                    for (Coordinate p : (List<Coordinate>)polygonZ.getOutLine()) {
                        sw.write(String.valueOf(p.x) + "," + String.valueOf(p.y) +
                                "," + String.valueOf(p.z));
                        sw.newLine();
                    }
                    sw.write(String.valueOf(polygonZ.getNumInteriorRing()));
                    sw.newLine();
                    if (polygonZ.hasHoles()) {
                        for (int i = 0; i < polygonZ.getNumInteriorRing(); i++) {
                            List<Coordinate> pointZS = (List<Coordinate>) polygonZ.getHoleLine(i);
                            sw.write("Hole");
                            sw.newLine();
                            sw.write(String.valueOf(pointZS.size()));
                            sw.newLine();
                            for (Coordinate p : pointZS) {
                                sw.write(String.valueOf(p.x) + "," + String.valueOf(p.y) +
                                        "," + String.valueOf(p.z));
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
    // </editor-fold>
}
