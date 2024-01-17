package org.meteoinfo.geometry.io.geojson;

import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.shape.*;
import java.util.ArrayList;
import java.util.List;

public class GeoJSONUtil {

    /**
     * Convert GeoJSON geometry to shape
     *
     * @param geometry The geometry
     * @return Shape
     */
    public static Shape toShape(Geometry geometry) {
        if (geometry instanceof Point) {
            return toShape((Point) geometry);
        } else if (geometry instanceof LineString) {
            return toShape((LineString) geometry);
        } else if (geometry instanceof MultiLineString) {
            return toShape((MultiLineString) geometry);
        } else if (geometry instanceof Polygon) {
            return toShape((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            return toShape((MultiPolygon) geometry);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Convert shape to GeoJSON geometry
     *
     * @param shape The shape
     * @return Geometry
     */
    public static Geometry fromShape(Shape shape) {
        if (shape instanceof PointShape) {
            return fromShape((PointShape) shape);
        } else if (shape instanceof PolylineShape) {
            return fromShape((PolylineShape) shape);
        } else if (shape instanceof PolygonShape) {
            return fromShape((PolygonShape) shape);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Convert PointShape to GeoJSON Point
     *
     * @param pointShape The PointShape object
     * @return GeoJSON Point object
     */
    public static Point fromShape(PointShape pointShape) {
        PointD p = pointShape.getPoint();

        return new Point(p.toArray());
    }

    /**
     * Convert GeoJSON Point to PointShape
     *
     * @param point The GeoJSON Point object
     * @return PointShape object
     */
    public static PointShape toShape(Point point) {
        double[] coordinates = point.getCoordinates();
        if (coordinates.length == 2) {
            return new PointShape(new PointD(coordinates[0], coordinates[1]));
        } else {
            return new PointZShape(new PointZ(coordinates[0], coordinates[1], coordinates[2]));
        }
    }

    /**
     * Convert PolylineShape to GeoJSON LineString or MultiLineString
     *
     * @param polylineShape The PolylineShape object
     * @return GeoJSON LineString or MultiLineString object
     */
    public static Geometry fromShape(PolylineShape polylineShape) {
        if (polylineShape.isMultiLine()) {
            int lineNum = polylineShape.getPartNum();
            double[][][] coordinates = new double[lineNum][][];
            int cNum = polylineShape.getShapeType() == ShapeTypes.POLYLINE ? 2 : 3;
            for (int j = 0; j < lineNum; j++) {
                Polyline polyline = polylineShape.getPolylines().get(j);
                int pNum = polyline.getPointList().size();
                double[][] matrix = new double[pNum][cNum];
                for (int i = 0; i < pNum; i++) {
                    matrix[i] = polyline.getPointList().get(i).toArray();
                }
                coordinates[j] = matrix;
            }

            return new MultiLineString(coordinates);
        } else {
            int cNum = polylineShape.getShapeType() == ShapeTypes.POLYLINE ? 2 : 3;
            double[][] coordinates = new double[polylineShape.getPointNum()][cNum];
            for (int i = 0; i < polylineShape.getPointNum(); i++) {
                coordinates[i] = polylineShape.getPoints().get(i).toArray();
            }

            return new LineString(coordinates);
        }
    }

    /**
     * Convert GeoJSON LineString to PolylineShape
     *
     * @param lineString The GeoJSON LineString object
     * @return PolylineShape object
     */
    public static PolylineShape toShape(LineString lineString) {
        double[][] coordinates = lineString.getCoordinates();
        int pNum = coordinates.length;
        if (coordinates[0].length == 2) {
            List<PointD> points = new ArrayList<>();
            for (int i = 0; i < pNum; i++) {
                points.add(new PointD(coordinates[i][0], coordinates[i][1]));
            }

            return new PolylineShape(points);
        } else {
            List<PointZ> points = new ArrayList<>();
            for (int i = 0; i < pNum; i++) {
                points.add(new PointZ(coordinates[i][0], coordinates[i][1], coordinates[i][2]));
            }

            return new PolylineZShape(points);
        }
    }

    /**
     * Convert GeoJSON LineString to PolylineShape
     *
     * @param lineString The GeoJSON LineString object
     * @return PolylineShape object
     */
    public static PolylineShape toShape(MultiLineString lineString) {
        double[][][] coordinates = lineString.getCoordinates();
        int lineNum = coordinates.length;
        int cNum = coordinates[0][0].length;
        if (cNum == 2) {
            PolylineShape polylineShape = new PolylineShape();
            polylineShape.setPartNum(lineNum);
            polylineShape.parts = new int[lineNum];
            List<PointD> points = new ArrayList<>();
            for (int j = 0; j < lineNum; j++) {
                int pNum = coordinates[j].length;
                polylineShape.parts[j] = pNum;
                for (int i = 0; i < pNum; i++) {
                    points.add(new PointD(coordinates[j][i][0], coordinates[j][i][1]));
                }
            }
            polylineShape.setPoints(points);

            return polylineShape;
        } else {
            PolylineZShape polylineZShape = new PolylineZShape();
            polylineZShape.setPartNum(lineNum);
            polylineZShape.parts = new int[lineNum];
            List<PointZ> points = new ArrayList<>();
            for (int j = 0; j < lineNum; j++) {
                int pNum = coordinates[j].length;
                polylineZShape.parts[j] = pNum;
                for (int i = 0; i < pNum; i++) {
                    points.add(new PointZ(coordinates[j][i][0], coordinates[j][i][1], coordinates[j][i][2]));
                }
            }
            polylineZShape.setPoints(points);

            return polylineZShape;
        }
    }

    /**
     * Convert PolygonShape to GeoJSON Polygon or MultiPolygon
     *
     * @param polygonShape The PolygonShape object
     * @return GeoJSON Polygon or MultiPolygon object
     */
    public static Geometry fromShape(PolygonShape polygonShape) {
        int cNum = polygonShape.getShapeType() == ShapeTypes.POLYGON ? 2 : 3;
        if (polygonShape.isMultiPolygon()) {
            int polygonNum = polygonShape.getPolygons().size();
            double[][][][] coordinates = new double[polygonNum][][][];
            for (int k = 0; k < polygonNum; k++) {
                org.meteoinfo.geometry.shape.Polygon sPolygon = polygonShape.getPolygon(k);
                int ringNumber = sPolygon.getRingNumber();
                double[][][] a3d = new double[ringNumber][][];
                int pNum = sPolygon.getOutLine().size();
                double[][] matrix = new double[pNum][cNum];
                for (int i = 0; i < pNum; i++) {
                    matrix[i] = sPolygon.getOutLine().get(i).toArray();
                }
                a3d[0] = matrix;
                if (sPolygon.hasHole()) {
                    for (int j = 0; j < sPolygon.getHoleLineNumber(); j++) {
                        pNum = sPolygon.getHoleLine(j).size();
                        matrix = new double[pNum][cNum];
                        for (int i = 0; i < sPolygon.getHoleLine(j).size(); i++) {
                            matrix[i] = sPolygon.getHoleLine(j).get(i).toArray();
                        }
                        a3d[j + 1] = matrix;
                    }
                }
                coordinates[k] = a3d;
            }

            return new MultiPolygon(coordinates);
        } else {
            org.meteoinfo.geometry.shape.Polygon sPolygon = polygonShape.getPolygon(0);
            int ringNum = sPolygon.getRingNumber();
            double[][][] coordinates = new double[ringNum][][];
            int pNum = sPolygon.getOutLine().size();
            double[][] matrix = new double[pNum][cNum];
            for (int i = 0; i < pNum; i++) {
                matrix[i] = sPolygon.getOutLine().get(i).toArray();
            }
            coordinates[0] = matrix;
            if (sPolygon.hasHole()) {
                for (int j = 0; j < sPolygon.getHoleLineNumber(); j++) {
                    pNum = sPolygon.getHoleLine(j).size();
                    matrix = new double[pNum][cNum];
                    for (int i = 0; i < sPolygon.getHoleLine(j).size(); i++) {
                        matrix[i] = sPolygon.getHoleLine(j).get(i).toArray();
                    }
                    coordinates[j + 1] = matrix;
                }
            }

            return new Polygon(coordinates);
        }
    }

    /**
     * Convert GeoJSON Polygon to PolygonShape
     *
     * @param polygon The GeoJSON Polygon object
     * @return PolygonShape object
     */
    public static PolygonShape toShape(Polygon polygon) {
        double[][][] coordinates = polygon.getCoordinates();
        int ringNum = coordinates.length;
        int cNum = coordinates[0][0].length;
        if (cNum == 2) {
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setPartNum(ringNum);
            polygonShape.parts = new int[ringNum];
            List<PointD> points = new ArrayList<>();
            for (int j = 0; j < ringNum; j++) {
                int pNum = coordinates[j].length;
                polygonShape.parts[j] = pNum;
                for (int i = 0; i < pNum; i++) {
                    points.add(new PointD(coordinates[j][i][0], coordinates[j][i][1]));
                }
            }
            polygonShape.setPoints(points);

            return polygonShape;
        } else {
            PolygonZShape polygonZShape = new PolygonZShape();
            polygonZShape.setPartNum(ringNum);
            polygonZShape.parts = new int[ringNum];
            List<PointZ> points = new ArrayList<>();
            for (int j = 0; j < ringNum; j++) {
                int pNum = coordinates[j].length;
                polygonZShape.parts[j] = pNum;
                for (int i = 0; i < pNum; i++) {
                    points.add(new PointZ(coordinates[j][i][0], coordinates[j][i][1], coordinates[j][i][2]));
                }
            }
            polygonZShape.setPoints(points);

            return polygonZShape;
        }
    }

    /**
     * Convert GeoJSON MultiPolygon to PolygonShape
     *
     * @param multiPolygon The GeoJSON MultiPolygon object
     * @return PolygonShape object
     */
    public static PolygonShape toShape(MultiPolygon multiPolygon) {
        double[][][][] coordinates = multiPolygon.getCoordinates();
        int polygonNum = coordinates.length;
        int cNum = coordinates[0][0][0].length;
        if (cNum == 2) {
            List<org.meteoinfo.geometry.shape.Polygon> polygons = new ArrayList<>();
            for (int k = 0; k < polygonNum; k++) {
                int ringNum = coordinates[k].length;
                org.meteoinfo.geometry.shape.Polygon polygon = new org.meteoinfo.geometry.shape.Polygon();
                for (int j = 0; j < ringNum; j++) {
                    List<PointD> points = new ArrayList<>();
                    int pNum = coordinates[k][j].length;
                    for (int i = 0; i < pNum; i++) {
                        points.add(new PointD(coordinates[k][j][i][0], coordinates[k][j][i][1]));
                    }
                    if (j == 0) {
                        polygon.setOutLine(points);
                    } else {
                        polygon.addHole(points);
                    }
                }
                polygons.add(polygon);
            }
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setPolygons(polygons);

            return polygonShape;
        } else {
            List<org.meteoinfo.geometry.shape.Polygon> polygons = new ArrayList<>();
            for (int k = 0; k < polygonNum; k++) {
                int ringNum = coordinates[k].length;
                org.meteoinfo.geometry.shape.Polygon polygon = new org.meteoinfo.geometry.shape.Polygon();
                for (int j = 0; j < ringNum; j++) {
                    List<PointZ> points = new ArrayList<>();
                    int pNum = coordinates[k][j].length;
                    for (int i = 0; i < pNum; i++) {
                        points.add(new PointZ(coordinates[k][j][i][0], coordinates[k][j][i][1],
                                coordinates[k][j][i][2]));
                    }
                    if (j == 0) {
                        polygon.setOutLine(points);
                    } else {
                        polygon.addHole(points);
                    }
                }
                polygons.add(polygon);
            }
            PolygonZShape polygonZShape = new PolygonZShape();
            polygonZShape.setPolygons(polygons);

            return polygonZShape;
        }
    }
}
