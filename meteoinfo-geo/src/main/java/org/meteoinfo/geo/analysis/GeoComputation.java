package org.meteoinfo.geo.analysis;

import org.meteoinfo.common.Direction;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.geometry.GeometryUtil;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.projection.GridLabel;
import org.meteoinfo.geometry.geoprocess.BorderPoint;
import org.meteoinfo.geometry.LineSegment;
import org.meteoinfo.geometry.MultiPolygon;
import org.meteoinfo.geometry.LineString;
import org.meteoinfo.geometry.Geometry;
import org.meteoinfo.math.meteo.MeteoMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeoComputation extends org.meteoinfo.geometry.geoprocess.GeoComputation {
    /**
     * Get grid labels of a polyline
     *
     * @param inPolyLine Polyline
     * @param clipExtent Clipping object
     * @param isVertical If is vertical
     * @return Clip points
     */
    public static List<GridLabel> getGridLabels(LineString inPolyLine, Extent clipExtent, boolean isVertical) {
        List<GridLabel> gridLabels = new ArrayList<>();
        List<Coordinate> aPList = inPolyLine.getCoordinates();

        if (!isExtentCross(inPolyLine.getExtent(), clipExtent)) {
            return gridLabels;
        }

        int i, j;
        //Judge if all points of the polyline are in the cut polygon - outline
        List<List<Coordinate>> newLines = new ArrayList<>();
        Coordinate p1, p2;
        boolean isReversed = false;
        if (pointInClipObj(clipExtent, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInClipObj(clipExtent, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                if (inPolyLine.isClosed()) {
                    List<Coordinate> bPList = new ArrayList<>();
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
                aGL.setAnge((float) MeteoMath.uv2ds(p2.x - p1.x, p2.y - p1.y)[0]);
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
                aGL.setAnge((float) MeteoMath.uv2ds(p2.x - p1.x, p2.y - p1.y)[0]);
                gridLabels.add(aGL);

                return gridLabels;
            }
        } else {
            newLines.add(aPList);
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP;
        List<Coordinate> clipPList = getClipPointList(clipExtent);
        for (Coordinate aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = pointInClipObj(clipExtent, aPList.get(0));
            Coordinate q1, q2, IPoint = new Coordinate();
            LineSegment lineA, lineB;
            List<Coordinate> newPlist = new ArrayList<>();
            //Polyline bLine = new Polyline();
            p1 = aPList.get(0);
            int inIdx = -1, outIdx = -1;
            //bool newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = aPList.get(i);
                if (pointInClipObj(clipExtent, p2)) {
                    if (!isInPolygon) {
                        lineA = new LineSegment();
                        lineA.p1 = p1;
                        lineA.p2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new LineSegment();
                            lineB.p1 = q1;
                            lineB.p2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
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
                        lineA = new LineSegment();
                        lineA.p1 = p1;
                        lineA.p2 = p2;
                        q1 = borderList.get(0).Point;
                        for (j = 1; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new LineSegment();
                            lineB.p1 = q1;
                            lineB.p2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                outIdx = j;
                                a1 = inIdx;
                                break;
                            }
                            q1 = q2;
                        }
                        if (j < borderList.size()) {
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

    /**
     * Get grid labels of a straight line
     *
     * @param inPolyLine Polyline
     * @param clipExtent Clipping object
     * @param isVertical If is vertical
     * @return Clip points
     */
    public static List<GridLabel> getGridLabels_StraightLine(LineString inPolyLine, Extent clipExtent, boolean isVertical) {
        List<GridLabel> gridLabels = new ArrayList<>();
        //List<PointZ> aPList = inPolyLine.getPointList();

        Coordinate aPoint = inPolyLine.getCoordinates().get(0);
        if (isVertical) {
            if (aPoint.x < clipExtent.minX || aPoint.x > clipExtent.maxX) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.South);
            aGL.setCoord(new Coordinate(aPoint.x, clipExtent.minY));
            gridLabels.add(aGL);
        } else {
            if (aPoint.y < clipExtent.minY || aPoint.y > clipExtent.maxY) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.Weast);
            aGL.setCoord(new Coordinate(clipExtent.minX, aPoint.y));
            gridLabels.add(aGL);
        }

        aPoint = inPolyLine.getCoordinates().get(inPolyLine.getCoordinates().size() - 1);
        if (isVertical) {
            if (aPoint.x < clipExtent.minX || aPoint.x > clipExtent.maxX) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.North);
            aGL.setCoord(new Coordinate(aPoint.x, clipExtent.maxY));
            gridLabels.add(aGL);
        } else {
            if (aPoint.y < clipExtent.minY || aPoint.y > clipExtent.maxY) {
                return gridLabels;
            }

            GridLabel aGL = new GridLabel();
            aGL.setLabDirection(Direction.East);
            aGL.setCoord(new Coordinate(clipExtent.maxX, aPoint.y));
            gridLabels.add(aGL);
        }

        return gridLabels;
    }

    /**
     * Determine if a point loacted in a polygon layer
     *
     * @param aLayer The polygon layer
     * @param aPoint The point
     * @param onlySel If check only selected shapes
     * @return Inside or outside
     */
    public static boolean pointInPolygonLayer(VectorLayer aLayer, Coordinate aPoint, boolean onlySel) {
        if (!GeometryUtil.pointInExtent(aPoint, aLayer.getExtent())) {
            return false;
        }

        List<MultiPolygon> polygons = new ArrayList<>();
        if (onlySel) {
            for (Geometry aShape : aLayer.getShapes()) {
                if (aShape.isSelected()) {
                    polygons.add((MultiPolygon) aShape);
                }
            }
        } else {
            for (Geometry aShape : aLayer.getShapes()) {
                polygons.add((MultiPolygon) aShape);
            }
        }

        return pointInPolygons(polygons, aPoint);
    }
}
