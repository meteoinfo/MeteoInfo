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
package org.meteoinfo.shape;

import org.meteoinfo.geoprocess.GeoComputation;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.meteoinfo.layer.LabelSet;
import org.meteoinfo.legend.BreakTypes;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.LabelBreak;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.PointBreak;

/**
 *
 * @author Yaqiang Wang
 */
public class GraphicCollection extends Graphic implements Iterator {

    // <editor-fold desc="Variables">
    private List<Graphic> graphics = new ArrayList<>();
    private Extent _extent = new Extent();
    protected boolean singleLegend = true;
    private int index;
    private LabelSet labelSet;
    private List<Graphic> labelPoints;
    protected LegendScheme legendScheme;
    protected ColorBreak legendBreak;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GraphicCollection() {
        this.index = 0;
        labelSet = new LabelSet();
        labelPoints = new ArrayList<>();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get graphic list
     *
     * @return Graphic list
     */
    @Override
    public List<Graphic> getGraphics() {
        return this.graphics;
    }

    /**
     * Set graphic list
     *
     * @param value Graphic list
     */
    public void setGraphics(List<Graphic> value) {
        this.graphics = value;
    }

    /**
     * Get extent
     *
     * @return The extent
     */
    @Override
    public Extent getExtent() {
        return _extent;
    }

    /**
     * Set extent
     *
     * @param value Extent
     */
    @Override
    public void setExtent(Extent value) {
        this._extent = value;
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
     * @return The lable points
     */
    public List<Graphic> getLabelPoints() {
        return this.labelPoints;
    }

    /**
     * Set label points
     *
     * @param lps The lable points
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
    }

    /**
     * Get legend break
     *
     * @return Legend break
     */
    public ColorBreak getLegendBreak() {
        return this.legendBreak;
    }

    /**
     * Set legend break
     *
     * @param value Legend break
     */
    public void setLegendBreak(ColorBreak value) {
        this.legendBreak = value;
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

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Update extent
     */
    public void updateExtent() {
        int i = 0;
        Extent extent;
        for (Graphic g : this.graphics) {
            if (g instanceof GraphicCollection) {
                extent = g.getExtent();
            } else {
                extent = g.getShape().getExtent();
            }
            if (i == 0) {
                _extent = extent;
            } else {
                _extent = MIMath.getLagerExtent(_extent, extent);
            }

            i += 1;
        }
    }

    /**
     * Add a graphic
     *
     * @param aGraphic The graphic
     * @return Boolean
     */
    public boolean add(Graphic aGraphic) {
        boolean istrue = this.graphics.add(aGraphic);

        //Update extent
        if (this.graphics.size() == 1) {
            _extent = aGraphic.getExtent();
        } else {
            _extent = MIMath.getLagerExtent(_extent, aGraphic.getExtent());
        }

        return istrue;
    }

    /**
     * Inset a graphic
     *
     * @param index Index
     * @param aGraphic The graphic
     */
    public void add(int index, Graphic aGraphic) {
        this.graphics.add(index, aGraphic);

        //Update extent
        if (this.graphics.size() == 1) {
            _extent = aGraphic.getShape().getExtent();
        } else {
            _extent = MIMath.getLagerExtent(_extent, aGraphic.getShape().getExtent());
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
     * @return Gaphic list size
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
        this.graphics.addAll(gs);
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
                _extent = graphic.getExtent();
            } else {
                _extent = MIMath.getLagerExtent(_extent, graphic.getExtent());
            }
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                this.graphics.add(graphic.getGraphicN(i));
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
     * Get legend
     *
     * @return Legend
     */
    @Override
    public ColorBreak getLegend() {
        if (this.legendBreak != null) {
            return this.legendBreak;
        } else {
            return this.graphics.get(0).getLegend();
        }
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
        PointD aPoint = new PointD();
        aPoint.X = (aExtent.minX + aExtent.maxX) / 2;
        aPoint.Y = (aExtent.minY + aExtent.maxY) / 2;

        for (Graphic aGraphic : this.graphics) {
            switch (aGraphic.getShape().getShapeType()) {
                case Point:
                    PointShape aPS = (PointShape) aGraphic.getShape();
                    if (MIMath.pointInExtent(aPS.getPoint(), aExtent)) {
                        selectedGraphics.add(aGraphic);
                    }
                    break;
                case Polyline:
                case PolylineZ:
                    PolylineShape aPLS = (PolylineShape) aGraphic.getShape();
                    if (MIMath.isExtentCross(aExtent, aPLS.getExtent())) {
                        for (j = 0; j < aPLS.getPoints().size(); j++) {
                            aPoint = aPLS.getPoints().get(j);
                            if (MIMath.pointInExtent(aPoint, aExtent)) {
                                selectedGraphics.add(aGraphic);
                                break;
                            }
                        }
                    }
                    break;
                case Polygon:
                case Rectangle:
                    PolygonShape aPGS = (PolygonShape) aGraphic.getShape();
                    if (!(aPGS.getPartNum() > 1)) {
                        if (GeoComputation.pointInPolygon((List<PointD>) aPGS.getPoints(), aPoint)) {
                            selectedGraphics.add(aGraphic);
                        }
                    } else {
                        for (int p = 0; p < aPGS.getPartNum(); p++) {
                            ArrayList pList = new ArrayList();
                            if (p == aPGS.getPartNum() - 1) {
                                for (int pp = aPGS.parts[p]; pp < aPGS.getPointNum(); pp++) {
                                    pList.add(aPGS.getPoints().get(pp));
                                }
                            } else {
                                for (int pp = aPGS.parts[p]; pp < aPGS.parts[p + 1]; pp++) {
                                    pList.add(aPGS.getPoints().get(pp));
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
     * Get shapes
     *
     * @return Shapes
     */
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList<>();
        for (Graphic g : this.graphics) {
            shapes.add(g.getShape());
        }
        return shapes;
    }

    /**
     * Get shape type
     *
     * @return Shape type
     */
    public ShapeTypes getShapeType() {
        return this.graphics.get(0).getShape().getShapeType();
    }

    private double getMinValue() {
        double min = Double.MAX_VALUE;
        for (Graphic graphic : this.graphics) {
            Shape shape = graphic.getShape();
            if (min > shape.getValue()) {
                min = shape.getValue();
            }
        }
        return min;
    }

    /**
     * Add labels
     */
    private void addLabelsByColor() {
        if (labelSet.isAutoDecimal()) {
            double min = getMinValue();
            labelSet.setDecimalDigits(MIMath.getDecimalNum(min));
        }
        String dFormat = "%1$." + String.valueOf(labelSet.getDecimalDigits()) + "f";
        PointD aPoint;
        for (Graphic graphic : this.graphics) {
            ColorBreak cb = graphic.getLegend();
            Shape shape = graphic.getShape();
            PointShape aPS = new PointShape();
            switch (shape.getShapeType()) {
                case Point:
                case PointM:
                case PointZ:
                    aPS.setPoint((PointD) ((PointShape) shape).getPoint().clone());
                    break;
                case Polyline:
                case PolylineM:
                case PolylineZ:
                    int pIdx = ((PolylineShape) shape).getPoints().size() / 2;
                    aPS.setPoint((PointD) ((PolylineShape) shape).getPoints().get(pIdx - 1).clone());
                    break;
                case Polygon:
                case PolygonM:
                    Extent aExtent = shape.getExtent();
                    aPoint = new PointD();
                    aPoint.X = ((aExtent.minX + aExtent.maxX) / 2);
                    aPoint.Y = ((aExtent.minY + aExtent.maxY) / 2);
                    aPS.setPoint(aPoint);
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
     * Add label point
     *
     * @param aLP Label point
     */
    public void addLabel(Graphic aLP) {
        labelPoints.add(aLP);
    }

    /**
     * Add labels of contour layer dynamicly
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
            Shape shape = graphic.getShape();
            ColorBreak cb = graphic.getLegend();
            PolylineShape aPLS = (PolylineShape) shape;
            Extent IExtent = aPLS.getExtent();
            if (IExtent.maxX - IExtent.minX > (sExtent.maxX - sExtent.minX) / 10
                    || IExtent.maxY - IExtent.minY > (sExtent.maxY - sExtent.minY) / 10) {
                LabelBreak aLP = new LabelBreak();
                int pIdx = aPLS.getPoints().size() / 2;
                //PointF aPoint = new PointF(0, 0);
                PointShape aPS = new PointShape();
                aPS.setPoint(aPLS.getPoints().get(pIdx - 1));
                //text = DataConvert.removeTailingZeros(String.valueOf(aPLS.getValue()));
                text = String.format(dFormat, aPLS.getValue());
                aLP.setText(text);
                aLP.setFont(labelSet.getLabelFont());
                aLP.setAlignType(labelSet.getLabelAlignType());
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
     * Get arrow zoom
     *
     * @return Arrow zoom
     */
    public float getArrowZoom() {
        if (this.getLegend().getBreakType() == BreakTypes.PointBreak) {
            float size = ((PointBreak) this.getLegend()).getSize();
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
    public GraphicCollection clip(List<PolygonShape> clipPolys) {
        GraphicCollection cgraphics = new GraphicCollection();
        for (PolygonShape aPGS : clipPolys) {
            for (int i = 0; i < this.graphics.size(); i++) {
                Shape bShape = this.graphics.get(i).getShape();
                Shape clipShape = bShape.intersection(aPGS);
                if (clipShape != null) {
                    cgraphics.add(new Graphic(clipShape, this.graphics.get(i).getLegend()));
                }
            }
        }
        cgraphics.setSingleLegend(this.singleLegend);
        cgraphics.setLegendScheme((LegendScheme) this.getLegendScheme().clone());

        return cgraphics;
    }
    // </editor-fold>
}
