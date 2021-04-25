package org.meteoinfo.geo.util;

import org.locationtech.proj4j.datum.Grid;
import org.meteoinfo.common.PointD;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.GridDataSetting;
import org.meteoinfo.data.StationData;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geo.analysis.GeoComputation;
import org.meteoinfo.geometry.shape.PolygonShape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.geo.analysis.InterpolationSetting;
import org.meteoinfo.math.interpolate.InterpUtil;
import smile.interpolation.KrigingInterpolation2D;

import java.util.ArrayList;
import java.util.List;

public class GeoMathUtil {
    /**
     * Mask out grid data with a polygon shape
     *
     * @param gridData The grid data
     * @param aPGS The polygon shape
     * @return Maskouted grid data
     */
    public static GridData maskout(GridData gridData, PolygonShape aPGS) {
        int xNum = gridData.getXNum();
        int yNum = gridData.getYNum();

        GridData cGrid = new GridData(gridData);
        for (int i = 0; i < yNum; i++) {
            if (gridData.yArray[i] >= aPGS.getExtent().minY && gridData.yArray[i] <= aPGS.getExtent().maxY) {
                for (int j = 0; j < xNum; j++) {
                    if (gridData.xArray[j] >= aPGS.getExtent().minX && gridData.xArray[j] <= aPGS.getExtent().maxX) {
                        if (GeoComputation.pointInPolygon(aPGS, new PointD(gridData.xArray[j], gridData.yArray[i]))) {
                            cGrid.data[i][j] = gridData.data[i][j];
                        } else {
                            cGrid.data[i][j] = gridData.missingValue;
                        }
                    } else {
                        cGrid.data[i][j] = gridData.missingValue;
                    }
                }
            } else {
                for (int j = 0; j < xNum; j++) {
                    cGrid.data[i][j] = gridData.missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
     * Mask out grid data with polygon shapes
     *
     * @param gridData The grid data
     * @param polygons The polygon shapes
     * @return Maskouted grid data
     */
    public static GridData maskout(GridData gridData, List<PolygonShape> polygons) {
        int xNum = gridData.getXNum();
        int yNum = gridData.getYNum();

        GridData cGrid = new GridData(gridData);
        for (int i = 0; i < yNum; i++) {
            for (int j = 0; j < xNum; j++) {
                if (GeoComputation.pointInPolygons(polygons, new PointD(gridData.xArray[j], gridData.yArray[i]))) {
                    cGrid.data[i][j] = gridData.data[i][j];
                } else {
                    cGrid.data[i][j] = gridData.missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
     * Mask out grid data with a polygon layer
     *
     * @param gridData The grid data
     * @param maskLayer The polygon layer
     * @return Maskouted grid data
     */
    public static GridData maskout(GridData gridData, VectorLayer maskLayer) {
        if (maskLayer.getShapeType() != ShapeTypes.POLYGON) {
            return gridData;
        }

        int xNum = gridData.getXNum();
        int yNum = gridData.getYNum();
        GridData cGrid = new GridData(gridData);
        for (int i = 0; i < yNum; i++) {
            if (gridData.yArray[i] >= maskLayer.getExtent().minY && gridData.yArray[i] <= maskLayer.getExtent().maxY) {
                for (int j = 0; j < xNum; j++) {
                    if (gridData.xArray[j] >= maskLayer.getExtent().minX && gridData.xArray[j] <= maskLayer.getExtent().maxX) {
                        if (GeoComputation.pointInPolygonLayer(maskLayer, new PointD(gridData.xArray[j], gridData.yArray[i]), false)) {
                            cGrid.data[i][j] = gridData.data[i][j];
                        } else {
                            cGrid.data[i][j] = gridData.missingValue;
                        }
                    } else {
                        cGrid.data[i][j] = gridData.missingValue;
                    }
                }
            } else {
                for (int j = 0; j < xNum; j++) {
                    cGrid.data[i][j] = gridData.missingValue;
                }
            }
        }

        return cGrid;
    }

    /**
      * Maskout station data
      *
     * @param stationData StationData
      * @param polygonShape Mask polygon shape
      * @return Result station data
      */
     public static StationData maskout(StationData stationData, PolygonShape polygonShape) {
         StationData stData = new StationData();
         stData.projInfo = stationData.projInfo;
         stData.missingValue = stationData.missingValue;
         for (int i = 0; i < stationData.getStNum(); i++) {
             if (GeoComputation.pointInPolygon(polygonShape, new PointD(stationData.getX(i), stationData.getY(i)))) {
                 stData.addData(stationData.getStid(i), stationData.getX(i), stationData.getY(i), stationData.getValue(i));
             }
         }

         return stData;
     }

     /**
      * Maskout station data
      *
      * @param stationData StationData
      * @param polygonShapes Mask polygon shapes
      * @return Result station data
      */
     public static StationData maskout(StationData stationData, List<PolygonShape> polygonShapes) {
         StationData stData = new StationData();
         stData.projInfo = stationData.projInfo;
         stData.missingValue = stationData.missingValue;
         for (int i = 0; i < stationData.getStNum(); i++) {
             if (GeoComputation.pointInPolygons(polygonShapes, new PointD(stationData.getX(i), stationData.getY(i)))) {
                 stData.addData(stationData.getStid(i), stationData.getX(i), stationData.getY(i), stationData.getValue(i));
             }
         }

         return stData;
     }

     /**
      * Maskout station data
      *
      * @param stationData StationData
      * @param maskLayer Mask layer
      * @return Result station data
      */
     public static StationData maskout(StationData stationData, VectorLayer maskLayer) {
         if (maskLayer.getShapeType() != ShapeTypes.POLYGON) {
             return stationData;
         }

         List<PolygonShape> polygons = (List<PolygonShape>) maskLayer.getShapes();
         return maskout(stationData, polygons);
     }

     /**
      * Maskin station data
      *
      * @param stationData StationData
      * @param polygonShape Mask polygon shape
      * @return Result station data
      */
     public static StationData maskin(StationData stationData, PolygonShape polygonShape) {
         StationData stData = new StationData();
         stData.projInfo = stationData.projInfo;
         stData.missingValue = stationData.missingValue;
         for (int i = 0; i < stationData.getStNum(); i++) {
             if (!GeoComputation.pointInPolygon(polygonShape, new PointD(stationData.getX(i), stationData.getY(i)))) {
                 stData.addData(stationData.getStid(i), stationData.getX(i), stationData.getY(i), stationData.getValue(i));
             }
         }

         return stData;
     }

     /**
      * Maskin station data
      *
      * @param stationData StationData
      * @param polygonShapes Mask polygon shapes
      * @return Result station data
      */
     public static StationData maskin(StationData stationData, List<PolygonShape> polygonShapes) {
         StationData stData = new StationData();
         stData.projInfo = stationData.projInfo;
         stData.missingValue = stationData.missingValue;
         for (int i = 0; i < stationData.getStNum(); i++) {
             if (!GeoComputation.pointInPolygons(polygonShapes, new PointD(stationData.getX(i), stationData.getY(i)))) {
                 stData.addData(stationData.getStid(i), stationData.getX(i), stationData.getY(i), stationData.getValue(i));
             }
         }

         return stData;
     }

     /**
      * Maskin station data
      *
      * @param stationData StationData
      * @param maskLayer Mask layer
      * @return Result station data
      */
     public static StationData maskin(StationData stationData, VectorLayer maskLayer) {
         if (maskLayer.getShapeType() != ShapeTypes.POLYGON) {
             return stationData;
         }

         List<PolygonShape> polygons = (List<PolygonShape>) maskLayer.getShapes();
         return maskin(stationData, polygons);
     }

     /**
      * Interpolate to grid data
      *
      * @param stationData StationData
      * @param interSet Interpolation setting
      * @return Grid data
      */
     public static GridData interpolateData(StationData stationData, InterpolationSetting interSet) {
         GridData aGridData = null;
         double[] X;
         double[] Y;
         List<double[]> values = createGridXY(interSet.getGridDataSetting());
         X = values.get(0);
         Y = values.get(1);
         switch (interSet.getInterpolationMethod()) {
             case IDW_RADIUS:
                 stationData.filterData_Radius(interSet.getRadius(), interSet.getGridDataSetting().dataExtent);
                 aGridData = interpolate_Radius(stationData.data,
                         X, Y, interSet.getMinPointNum(), interSet.getRadius(), stationData.missingValue);
                 break;
             case IDW_NEIGHBORS:
                 stationData.filterData_Radius(interSet.getRadius(), interSet.getGridDataSetting().dataExtent);
                 aGridData = interpolate_Neighbor(stationData.data, X, Y,
                         interSet.getMinPointNum(), stationData.missingValue);
                 break;
             case CRESSMAN:
                 stationData.filterData_Radius(0, interSet.getGridDataSetting().dataExtent);
                 aGridData = interpolate_Cressman(stationData.data, X, Y, interSet.getRadiusList(), stationData.missingValue);
                 break;
             case BARNES:
                 stationData.filterData_Radius(0, interSet.getGridDataSetting().dataExtent);
                 double[][] gData = InterpUtil.barnes(stationData.getX(), stationData.getY(), stationData.getStData(),
                         X, Y, interSet.getRadiusList(), 1., 1.);
                 aGridData = new GridData();
                 aGridData.xArray = X;
                 aGridData.yArray = Y;
                 aGridData.data = gData;
                 break;
             case KRIGING:
                 stationData.filterData_Radius(0, interSet.getGridDataSetting().dataExtent);
                 gData = InterpUtil.kriging(stationData.getX(), stationData.getY(), stationData.getStData(),
                         X, Y, interSet.getBeta());
                 aGridData = new GridData();
                 aGridData.xArray = X;
                 aGridData.yArray = Y;
                 aGridData.data = gData;
                 break;
             case ASSIGN_POINT_GRID:
                 stationData.filterData_Radius(0, interSet.getGridDataSetting().dataExtent);
                 aGridData = interpolate_Assign(stationData.data, X, Y, stationData.missingValue);
                 break;
         }

         return aGridData;
     }

     /**
      * Interpolate by IDW radius method
      *
      * @param S Station data array
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param minPNum Minimum point number
      * @param radius Radius
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Radius(double[][] S, double[] X, double[] Y,
             int minPNum, double radius, double missingValue) {
         double[][] dataArray;
         dataArray = wcontour.Interpolate.interpolation_IDW_Radius(S, X, Y, minPNum, radius, missingValue);

         GridData gridData = new GridData();
         gridData.data = dataArray;
         gridData.missingValue = missingValue;
         gridData.xArray = X;
         gridData.yArray = Y;

         return gridData;
     }

     /**
      * Interpolate by IDW radius method
      *
      * @param stationData StationData
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param minPNum Minimum point number
      * @param radius Radius
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Radius(StationData stationData, List<Number> X, List<Number> Y,
             int minPNum, double radius, double missingValue) {
         double[] nX = new double[X.size()];
         double[] nY = new double[Y.size()];
         for (int i = 0; i < X.size(); i++){
             nX[i] = X.get(i).doubleValue();
         }
         for (int i = 0; i < Y.size(); i++){
             nY[i] = Y.get(i).doubleValue();
         }

         return interpolate_Radius(stationData.data, nX, nY, minPNum, radius, missingValue);
     }

     /**
      * Interpolate by IDW_Neighbor method
      *
      * @param S Station data array
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param pNum Point number
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Neighbor(double[][] S, double[] X, double[] Y, int pNum, double missingValue) {
         double[][] dataArray = wcontour.Interpolate.interpolation_IDW_Neighbor(S, X, Y, pNum, missingValue);

         GridData gridData = new GridData();
         gridData.data = dataArray;
         gridData.missingValue = missingValue;
         gridData.xArray = X;
         gridData.yArray = Y;

         return gridData;
     }

     /**
      * Interpolate by IDW_Neighbor method
      *
      * @param stationData StationData
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param pNum Point number
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Neighbor(StationData stationData, List<Number> X, List<Number> Y, int pNum, double missingValue) {
         double[] nX = new double[X.size()];
         double[] nY = new double[Y.size()];
         for (int i = 0; i < X.size(); i++){
             nX[i] = X.get(i).doubleValue();
         }
         for (int i = 0; i < Y.size(); i++){
             nY[i] = Y.get(i).doubleValue();
         }

         return interpolate_Neighbor(stationData.data, nX, nY, pNum, missingValue);
     }

     /**
      * Interpolation by Cressman method
      *
      * @param S Station data array
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param radList Radius list
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Cressman(double[][] S, double[] X, double[] Y,
             List<Double> radList, double missingValue) {
         double[][] dataArray = wcontour.Interpolate.cressman(S, X, Y, missingValue, radList);

         GridData gridData = new GridData();
         gridData.data = dataArray;
         gridData.missingValue = missingValue;
         gridData.xArray = X;
         gridData.yArray = Y;

         return gridData;
     }

     /**
      * Interpolation by Cressman method
      *
      * @param stationData StationData
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param radList Radius list
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Cressman(StationData stationData, List<Number> X, List<Number> Y,
             List<Number> radList, double missingValue) {
         double[] nX = new double[X.size()];
         double[] nY = new double[Y.size()];
         for (int i = 0; i < X.size(); i++){
             nX[i] = X.get(i).doubleValue();
         }
         for (int i = 0; i < Y.size(); i++){
             nY[i] = Y.get(i).doubleValue();
         }

         List<Double> rlist = new ArrayList<>();
         for (Number r : radList){
             rlist.add(r.doubleValue());
         }
         return interpolate_Cressman(stationData.data, nX, nY, rlist, missingValue);
     }

     /**
      * Interpolation by assign method
      *
      * @param S Station data array
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Assign(double[][] S, double[] X, double[] Y, double missingValue) {
         double[][] dataArray = wcontour.Interpolate.assignPointToGrid(S, X, Y, missingValue);

         GridData gridData = new GridData();
         gridData.data = dataArray;
         gridData.missingValue = missingValue;
         gridData.xArray = X;
         gridData.yArray = Y;

         return gridData;
     }

     /**
      * Interpolation by assign method
      *
      * @param stationData StationData
      * @param X X coordinate array
      * @param Y Y coordinate array
      * @param missingValue Missing value
      * @return Grid data
      */
     public static GridData interpolate_Assign(StationData stationData, List<Number> X, List<Number> Y, double missingValue) {
         double[] nX = new double[X.size()];
         double[] nY = new double[Y.size()];
         for (int i = 0; i < X.size(); i++){
             nX[i] = X.get(i).doubleValue();
         }
         for (int i = 0; i < Y.size(); i++){
             nY[i] = Y.get(i).doubleValue();
         }

         return interpolate_Assign(stationData.data, nX, nY, missingValue);
     }

     /**
      * Create grid X/Y coordinate
      *
      * @param gSet
      * @return X/Y coordinate array list
      */
     public static List<double[]> createGridXY(GridDataSetting gSet) {
         double xDelt = (gSet.dataExtent.maxX - gSet.dataExtent.minX) / (double) (gSet.xNum - 1);
         double yDelt = (gSet.dataExtent.maxY - gSet.dataExtent.minY) / (double) (gSet.yNum - 1);

         return wcontour.Interpolate.createGridXY_Delt(gSet.dataExtent.minX, gSet.dataExtent.minY,
                 gSet.dataExtent.maxX, gSet.dataExtent.maxY, xDelt, yDelt);
     }

}
