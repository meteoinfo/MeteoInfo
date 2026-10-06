/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import org.meteoinfo.common.Direction;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.colors.ColorUtil;
import org.meteoinfo.chart.graphic.Artist;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.geometry.GeometryUtil;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.geoprocess.BorderPoint;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.legend.LineStyles;
import org.meteoinfo.geometry.legend.PolylineBreak;
import org.meteoinfo.geometry.shape.Line;
import org.meteoinfo.geometry.shape.Polyline;
import org.meteoinfo.projection.GridLabel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class GridLine extends Artist {
    // <editor-fold desc="Variables">
    protected PolylineBreak lineBreak;
    protected boolean drawXLine;
    protected boolean drawYLine;
    protected boolean drawZLine;
    protected boolean top;
    // </editor-fold>    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public GridLine(){
        this(false);
    }

    /**
     * Constructor
     * @param visible Draw grid lines or not
     */
    public GridLine(boolean visible) {
        this.lineBreak = new PolylineBreak();
        this.lineBreak.setColor(new Color(175, 176, 176));
        this.lineBreak.setWidth(0.8f);
        this.lineBreak.setStyle(LineStyles.SOLID);
        this.top = false;
        this.drawXLine = visible;
        this.drawYLine = visible;
        this.drawZLine = visible;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    /**
     * Get color
     * @return Color
     */
    public Color getColor(){
        return this.lineBreak.getColor();
    }
    
    /**
     * Set color
     * @param value Color
     */
    public void setColor(Color value){
        int alpha = this.getColor().getAlpha();
        Color color = ColorUtil.getColor(value, alpha);
        this.lineBreak.setColor(color);
    }

    /**
     * Set color
     * @param value Color
     */
    public void setColorAndAlpha(Color value){
        this.lineBreak.setColor(value);
    }

    /**
     * Set alpha
     * @param value Alpha
     */
    public void setAlpha(int value) {
        Color color = ColorUtil.getColor(this.getColor(), value);
        this.lineBreak.setColor(color);
    }

    /**
     * Set alpha
     * @param value Alpha
     */
    public void setAlpha(float value) {
        Color color = ColorUtil.getColor(this.getColor(), value);
        this.lineBreak.setColor(color);
    }
    
    /**
     * Get size
     * @return Size
     */
    public float getSize(){
        return this.lineBreak.getWidth();
    }
    
    /**
     * Set size
     * @param value Size
     */
    public void setSize(float value) {
        this.lineBreak.setWidth(value);
    }
    
    /**
     * Get style
     * @return Style
     */
    public LineStyles getStyle(){
        return this.lineBreak.getStyle();
    }
    
    /**
     * Set style
     * @param value Style
     */
    public void setStyle(LineStyles value){
        this.lineBreak.setStyle(value);
    }
    
    /**
     * Get if draw x grid lines
     * @return Boolean
     */
    public boolean isDrawXLine(){
        return this.drawXLine;
    }
    
    /**
     * Set if draw x grid lines
     * @param value Boolean
     */
    public void setDrawXLine(boolean value){
        this.drawXLine = value;
    }
    
    /**
     * Get if draw y grid lines
     * @return Boolean
     */
    public boolean isDrawYLine(){
        return this.drawYLine;
    }
    
    /**
     * Set if draw y grid lines
     * @param value Boolean
     */
    public void setDrawYLine(boolean value){
        this.drawYLine = value;
    }

    /**
     * Get if draw z grid lines
     * @return Boolean
     */
    public boolean isDrawZLine(){
        return this.drawZLine;
    }

    /**
     * Set if draw z grid lines
     * @param value Boolean
     */
    public void setDrawZLine(boolean value){
        this.drawZLine = value;
    }
    
    /**
     * Return if the grid draw on the top of the graph
     * @return Boolean
     */
    public boolean isTop(){
        return this.top;
    }
    
    /**
     * Set if the grid draw on the top of the graph
     * @param value Boolean
     */
    public void setTop(boolean value){
        this.top = value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Get grid labels of a polyline
     *
     * @param inPolyLine Polyline
     * @param clipExtent Clipping object
     * @param isVertical If is vertical
     * @return Clip points
     */
    public static java.util.List<GridLabel> getGridLabels(Polyline inPolyLine, Extent clipExtent, boolean isVertical) {
        java.util.List<GridLabel> gridLabels = new ArrayList<>();
        java.util.List<Coordinate> aPList = inPolyLine.getPointList();

        if (!GeometryUtil.isExtentCross(inPolyLine.getExtent(), clipExtent)) {
            return gridLabels;
        }

        int i, j;
        //Judge if all points of the polyline are in the cut polygon - outline
        java.util.List<java.util.List<Coordinate>> newLines = new ArrayList<>();
        Coordinate p1, p2;
        boolean isReversed = false;
        if (GeoComputation.pointInClipObj(clipExtent, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!GeoComputation.pointInClipObj(clipExtent, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                if (inPolyLine.isClosed()) {
                    java.util.List<Coordinate> bPList = new ArrayList<>();
                    bPList.addAll(aPList.subList(notInIdx, aPList.size() - 1));
                    bPList.addAll(aPList.subList(1, notInIdx));
                    bPList.add(bPList.get(0));
                    newLines.add(bPList);
                } else {
                    Collections.reverse(aPList);
                    newLines.add(aPList);
                    isReversed = true;
                }
            } else {    //the input polygon is inside the cut polygon
                p1 = aPList.get(0);
                if (aPList.size() == 2)
                    p2 = aPList.get(1);
                else
                    p2 = aPList.get(2);
                GridLabel aGL = new GridLabel();
                aGL.setLongitude(isVertical);
                aGL.setBorder(false);
                aGL.setCoord(p1);
                if (isVertical) {
                    aGL.setLabDirection(Direction.South);
                } else {
                    aGL.setLabDirection(Direction.Weast);
                }
                aGL.setAnge((float) MIMath.uv2ds(p2.x - p1.x, p2.y - p1.y)[0]);
                gridLabels.add(aGL);

                p1 = aPList.get(aPList.size() - 1);
                if (aPList.size() == 2)
                    p2 = aPList.get(aPList.size() - 2);
                else
                    p2 = aPList.get(aPList.size() - 3);
                aGL = new GridLabel();
                aGL.setLongitude(isVertical);
                aGL.setBorder(false);
                aGL.setCoord(p1);
                if (isVertical) {
                    aGL.setLabDirection(Direction.North);
                } else {
                    aGL.setLabDirection(Direction.East);
                }
                aGL.setAnge((float) MIMath.uv2ds(p2.x - p1.x, p2.y - p1.y)[0]);
                gridLabels.add(aGL);

                return gridLabels;
            }
        } else {
            newLines.add(aPList);
        }

        //Prepare border point list
        java.util.List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP;
        java.util.List<Coordinate> clipPList = GeoComputation.getClipPointList(clipExtent);
        for (Coordinate aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = GeoComputation.pointInClipObj(clipExtent, aPList.get(0));
            Coordinate q1, q2, IPoint = new Coordinate();
            Line lineA, lineB;
            List<Coordinate> newPlist = new ArrayList<>();
            //Polyline bLine = new Polyline();
            p1 = aPList.get(0);
            int inIdx = -1, outIdx = -1;
            //bool newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = aPList.get(i);
                if (GeoComputation.pointInClipObj(clipExtent, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (GeoComputation.isLineSegmentCross(lineA, lineB)) {
                                IPoint = GeoComputation.getCrossPoint(lineA, lineB);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
                        }
                        if (j == borderList.size()) {
                            j = j - 1;
                        }
                        GridLabel aGL = new GridLabel();
                        aGL.setLongitude(isVertical);
                        aGL.setBorder(true);
                        aGL.setCoord(IPoint);
                        if (MIMath.doubleEquals(q1.x, borderList.get(j).Point.x)) {
                            if (MIMath.doubleEquals(q1.x, clipExtent.minX)) {
                                aGL.setLabDirection(Direction.Weast);
                            } else {
                                aGL.setLabDirection(Direction.East);
                            }
                        } else {
                            if (MIMath.doubleEquals(q1.y, clipExtent.minY)) {
                                aGL.setLabDirection(Direction.South);
                            } else {
                                aGL.setLabDirection(Direction.North);
                            }
                        }

                        if (isVertical) {
                            if (aGL.getLabDirection() == Direction.South || aGL.getLabDirection() == Direction.North) {
                                gridLabels.add(aGL);
                            }
                        } else {
                            if (aGL.getLabDirection() == Direction.East || aGL.getLabDirection() == Direction.Weast) {
                                gridLabels.add(aGL);
                            }
                        }

                    }
                    newPlist.add(aPList.get(i));
                    isInPolygon = true;
                } else {
                    if (isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (GeoComputation.isLineSegmentCross(lineA, lineB)) {
                                IPoint = GeoComputation.getCrossPoint(lineA, lineB);
                                outIdx = j;
                                a1 = inIdx;
                                break;
                            }
                            q1 = q2;
                        }
                        if (j == borderList.size()) {
                            j = j - 1;
                        }
                        GridLabel aGL = new GridLabel();
                        aGL.setBorder(true);
                        aGL.setLongitude(isVertical);
                        aGL.setCoord(IPoint);
                        if (MIMath.doubleEquals(q1.x, borderList.get(j).Point.x)) {
                            if (MIMath.doubleEquals(q1.x, clipExtent.minX)) {
                                aGL.setLabDirection(Direction.Weast);
                            } else {
                                aGL.setLabDirection(Direction.East);
                            }
                        } else {
                            if (MIMath.doubleEquals(q1.y, clipExtent.minY)) {
                                aGL.setLabDirection(Direction.South);
                            } else {
                                aGL.setLabDirection(Direction.North);
                            }
                        }

                        if (isVertical) {
                            if (aGL.getLabDirection() == Direction.South || aGL.getLabDirection() == Direction.North) {
                                gridLabels.add(aGL);
                            }
                        } else {
                            if (aGL.getLabDirection() == Direction.East || aGL.getLabDirection() == Direction.Weast) {
                                gridLabels.add(aGL);
                            }
                        }

                        isInPolygon = false;
                        newPlist = new ArrayList<>();
                    }
                }
                p1 = p2;
            }

            if (isInPolygon && newPlist.size() > 1) {
                GridLabel aGL = new GridLabel();
                aGL.setLongitude(isVertical);
                aGL.setBorder(false);
                aGL.setCoord(newPlist.get(newPlist.size() - 1));
                if (isVertical) {
                    if (isReversed) {
                        aGL.setLabDirection(Direction.South);
                    } else {
                        aGL.setLabDirection(Direction.North);
                    }
                } else {
                    if (isReversed) {
                        aGL.setLabDirection(Direction.Weast);
                    } else {
                        aGL.setLabDirection(Direction.East);
                    }
                }

                gridLabels.add(aGL);
            }
        }

        return gridLabels;
    }
    // </editor-fold>
}
