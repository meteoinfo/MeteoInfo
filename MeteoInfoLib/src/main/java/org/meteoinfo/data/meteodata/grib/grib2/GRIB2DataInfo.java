/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.grib.grib2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.DataMath;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.Bytes2Number;
import org.meteoinfo.projection.info.ProjectionInfo;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.grib.grib2.Grib2SectionGridDefinition;
import ucar.nc2.grib.grib2.Grib2SectionIdentification;
import ucar.nc2.grib.grib2.Grib2SectionIndicator;
import ucar.nc2.grib.grib2.Grib2SectionLocalUse;
import ucar.nc2.grib.grib2.Grib2SectionProductDefinition;
import ucar.unidata.io.RandomAccessFile;

/**
 *
 * @author yaqiang
 */
public class GRIB2DataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private int _headerLength = 0;
    private List<GRIB2MessageIndex> _messageIdxList = new ArrayList<>();
    private ProjectionInfo _projInfo;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public GRIB2DataInfo(){
        this.setDataType(MeteoDataType.GRIB2);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get header length
     *
     * @return Header length
     */
    public int getHeaderLength() {
        return _headerLength;
    }

    /**
     * Set header length
     *
     * @param length
     */
    public void setHeaderLength(int length) {
        _headerLength = length;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");

            //Shift header
            br.seek(_headerLength);
            boolean isNewTime = false;
            int recordNum = 0;
            int gridNum = 0;
            List<Date> times = new ArrayList<Date>();
            List<Variable> variables = new ArrayList<Variable>();
            double[] X, Y;
            while (br.getFilePointer() < br.length() - 30) {
                Grib2SectionIndicator rINS = new Grib2SectionIndicator(br);
                long messageStart = rINS.getStartPos();
                long messageEnd = rINS.getEndPos();
                Grib2SectionIdentification rIDS = new Grib2SectionIdentification(br);

                while (this.readSectionNumber(br) != 8) {
                    int sectionNum = readSectionNumber(br);
                    GRIB2MessageIndex messageIdx = new GRIB2MessageIndex();
                    messageIdx.messagePos = messageStart;
                    messageIdx.dataPos = br.getFilePointer();
                    messageIdx.startSection = sectionNum;
                    if (sectionNum == 2) {
                        Grib2SectionLocalUse rLUS = new Grib2SectionLocalUse(br);
                        Grib2SectionGridDefinition rGDS = new Grib2SectionGridDefinition(br);
                        if (gridNum == 0) {
                            //this._projInfo = getProjectionInfo(rGDS);
                            this.setProjectionInfo(this._projInfo);
                            //Object[] XY = this.getXYArray(rGDS);
                            //X = (double[]) XY[0];
                            //Y = (double[]) XY[1];
//                            Dimension xDim = new Dimension(DimensionType.X);
//                            xDim.setValues(X);
//                            this.setXDimension(xDim);
//                            Dimension yDim = new Dimension(DimensionType.Y);
//                            yDim.setValues(Y);
//                            this.setYDimension(yDim);
                        } else {
                            //Object[] XY = this.getXYArray(rGDS);
                            //X = (double[]) XY[0];
                            //Y = (double[]) XY[1];
                        }
                        gridNum += 1;
                    }
                    if (sectionNum == 3) {
                        Grib2SectionGridDefinition rGDS = new Grib2SectionGridDefinition(br);
                        if (gridNum == 0) {
                            //this._projInfo = getProjectionInfo(rGDS);
                            this.setProjectionInfo(this._projInfo);
                            //Object[] XY = this.getXYArray(rGDS);
                            //X = (double[]) XY[0];
                            //Y = (double[]) XY[1];
//                            Dimension xDim = new Dimension(DimensionType.X);
//                            xDim.setValues(X);
//                            this.setXDimension(xDim);
//                            Dimension yDim = new Dimension(DimensionType.Y);
//                            yDim.setValues(Y);
//                            this.setYDimension(yDim);
                        } else {
                            //Object[] XY = this.getXYArray(rGDS);
                            //X = (double[]) XY[0];
                            //Y = (double[]) XY[1];
                        }
                        gridNum += 1;
                    }
                    Grib2SectionProductDefinition rPDS = new Grib2SectionProductDefinition(br);
                    seekNextSction(br);    //Skip Data representation section
                    seekNextSction(br);    //Skip Bitmap section
                    seekNextSction(br);    //Skip Data section

                    recordNum += 1;
                                        
                }
            }

            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GRIB2DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GRIB2DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int readSectionNumber(RandomAccessFile br) throws IOException {
        byte[] bytes = br.readBytes(4);
        if (new String(bytes).trim().equals("GRIB")) {
            br.seek(br.getFilePointer() - 4);
            return 0;
        } else if (bytes[0] == '7' && bytes[1] == '7' && bytes[2] == '7' && bytes[3] == '7') {
            br.seek(br.getFilePointer() - 4);
            return 8;
        } else {
            int sectionNum = br.readByte();
            br.seek(br.getFilePointer() - 5);
            return sectionNum;
        }
    }

    private void seekNextSction(RandomAccessFile br) {
        try {
            int length = Bytes2Number.int4(br.getRandomAccessFile());
            br.seek(br.getFilePointer() + length - 4);
        } catch (IOException ex) {
            Logger.getLogger(GRIB2DataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    /**
//     * Get projection info from grid definition section
//     *
//     * @param rGDS The grid definition section
//     * @return Projection info
//     */
//    private ProjectionInfo getProjectionInfo(Grib2SectionGridDefinition rGDS) {
//        ProjectionInfo aProjInfo;
//        String projStr = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";
//        switch (rGDS.getSource()) {  // Grid Definition Template Number
//
//            case 0:
//            case 1:
//            case 2:
//            case 3:       // Latitude/Longitude Grid
//                projStr = KnownCoordinateSystems.geographic.world.WGS1984.toProj4String();
//                break;
//            case 10:  // Mercator
//                if (rGDS.getGDS().getNy() == 0) {
//                    projStr = "+proj=merc+lon_0=" + String.valueOf(GRIBData.getMeanLongitude(rGDS.getLo1(), rGDS.getLo2()));
//                } else //Transverse_Mercator
//                {
//                    projStr = "+proj=tmerc+lon_0=" + String.valueOf(GRIBData.getMeanLongitude(rGDS.getLo1(), rGDS.getLo2()))
//                            + "+lat_0=" + String.valueOf(rGDS.getLad());
//                }
//                break;
//            case 20:  // Polar stereographic projection
//                double lat0 = 90;
//                if ((rGDS.getProjectionCenter() & 128) != 0) {
//                    lat0 = -90;
//                }
//                projStr = "+proj=stere+lon_0=" + String.valueOf(rGDS.getLov())
//                        + "+lat_0=" + String.valueOf(lat0);
//                break;
//            case 30:  // Lambert Conformal
//                projStr = "+proj=lcc+lon_0=" + String.valueOf(rGDS.getLov())
//                        + "+lat_0=" + String.valueOf(rGDS.getLad())
//                        + "+lat_1=" + String.valueOf(rGDS.getLatin1())
//                        + "+lat_2=" + String.valueOf(rGDS.getLatin2());
//                break;
//            case 31:  // Albers Equal Area
//
//
//                break;
//            case 40:
//            case 41:
//            case 42:
//            case 43:  // Gaussian latitude/longitude
//                projStr = KnownCoordinateSystems.geographic.world.WGS1984.toProj4String();
//                break;
//            case 50:
//            case 51:
//            case 52:
//            case 53:                     // Spherical harmonic coefficients
//
//
//                break;
//            case 90:  // Space view perspective or orthographic
//                projStr = "+proj=ortho+lon_0=" + String.valueOf(rGDS.getLop())
//                        + "+lat_0=" + String.valueOf(rGDS.getLap());
//
//                //if (isOrtho)
//                //    projStr = "+proj=ortho+lon_0=" + lop.ToString() +
//                //        "+lat_0=" + lap.ToString();
//                //else
//                //    projStr = "+proj=geos+lon_0=" + lop.ToString() +
//                //        "+h=" + altitude.ToString();
//                break;
//            case 100:  // Triangular grid based on an icosahedron
//
//
//                break;
//            case 110:  // Equatorial azimuthal equidistant projection
//
//
//                break;
//            case 120:  // Azimuth-range Projection
//
//
//                break;
//            case 204:  // Curvilinear orthographic
//
//                break;
//            default:
//                return null;
//        }
//
//        aProjInfo = new ProjectionInfo(projStr);
//        return aProjInfo;
//    }

//    /**
//     * Get X coordinate array
//     *
//     * @param rGDS The grid definition section
//     * @return X array
//     */
//    public double[] getXArray(Grib2SectionGridDefinition rGDS) {
//        double[] X = new double[rGDS.getNx()];
//        double ddx = Math.abs(rGDS.getDx());
//        double ddx1 = (rGDS.getLo2() - rGDS.getLo1()) / (rGDS.getNx() - 1);
//        if (!MIMath.doubleEquals(ddx, ddx1) && Math.abs(ddx - ddx1) < 0.001) {
//            ddx = ddx1;
//        }
//
//        for (int i = 0; i < rGDS.getNx(); i++) {
//            X[i] = rGDS.getLo1() + ddx * i;
//        }
//
//        return X;
//    }
//
//    /**
//     * Get Y coordinate array
//     *
//     * @param rGDS The grid definition section
//     * @return Y array
//     */
//    public double[] getYArray(Grib2GridDefinitionSection rGDS) {
//        double[] Y = new double[rGDS.getNy()];
//        double ddy = Math.abs(rGDS.getDy());
//        double sLat = Math.min(rGDS.getLa1(), rGDS.getLa2());
//        for (int i = 0; i < rGDS.getNy(); i++) {
//            Y[i] = sLat + ddy * i;
//        }
//
//        return Y;
//    }

    /**
     * Get Y coordinate array of Gaussian grid
     *
     * @param ny Y number
     * @return Y coordinate array
     */
    public double[] getGaussYArray(int ny) {
        double[] Y = new double[ny];
        Y = (double[]) DataMath.gauss2Lats(ny)[0];

        //double ymin = Y[0];
        //double ymax = Y[Y.Length - 1];
        //double delta = (ymax - ymin) / (NY - 1);
        //for (int i = 0; i < NY; i++)
        //    Y[i] = ymin + i * delta;

        return Y;
    }

//    /**
//     * Get X/Y coordinate array
//     *
//     * @param rGDS The grid definition section
//     * @return X/Y coordinate array
//     */
//    public Object[] getXYArray(Grib2GridDefinitionSection rGDS) {
//        ProjectionInfo aProjInfo = getProjectionInfo(rGDS);
//        double[] X = new double[1], Y = new double[1];
//        switch (aProjInfo.getProjectionName()) {
//            case LongLat:
//                switch (rGDS.getGdtn()) {
//                    case 0:
//                    case 1:
//                    case 2:
//                    case 3:
//                        X = getXArray(rGDS);
//                        Y = getYArray(rGDS);
//                        break;
//                    case 40:
//                    case 41:
//                    case 42:
//                    case 43:  // Gaussian latitude/longitude
//                        X = getXArray(rGDS);
//                        Y = getGaussYArray(rGDS.getNy());
//                        break;
//                }
//                break;
//            case Orthographic_Azimuthal:
//            case Geostationary_Satellite:
//                //Get under satellite point X/Y
//                ProjectionInfo fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
//                double s_X,
//                 s_Y;
//                double[][] points = new double[1][];
//                points[0] = new double[]{rGDS.getLo1(), rGDS.getLa1()};
//                Reproject.reprojectPoints(points, fromProj, aProjInfo, 0, 1);
//                s_X = points[0][0];
//                s_Y = points[0][1];
//
//                //Get integer sync X/Y            
//                int i_XP,
//                 i_YP;
//                double i_X,
//                 i_Y;
//                i_XP = (int) rGDS.getXp();
//                if (rGDS.getXp() == i_XP) {
//                    i_X = s_X;
//                } else {
//                    i_X = s_X - (rGDS.getXp() - i_XP) * rGDS.getDx();
//                }
//                i_YP = (int) rGDS.getYp();
//                if (rGDS.getYp() == i_YP) {
//                    i_Y = s_Y;
//                } else {
//                    i_Y = s_Y - (rGDS.getYp() - i_YP) * rGDS.getDy();
//                }
//
//                //Get left bottom X/Y
//                int nx,
//                 ny;
//                nx = X.length;
//                ny = Y.length;
//                double xlb,
//                 ylb;
//                xlb = i_X - (i_XP - 1) * rGDS.getDx();
//                ylb = i_Y - (i_YP - 1) * rGDS.getDy();
//
//                //Get X Y with orient 0
//                int i;
//                X = new double[rGDS.getNx()];
//                Y = new double[rGDS.getNy()];
//                for (i = 0; i < rGDS.getNx(); i++) {
//                    X[i] = xlb + i * rGDS.getDx();
//                }
//                for (i = 0; i < rGDS.getNy(); i++) {
//                    Y[i] = ylb + i * rGDS.getDy();
//                }
//                break;
//            default:
//                //Get start X/Y
//                fromProj = KnownCoordinateSystems.geographic.world.WGS1984;
//                points = new double[1][];
//                points[0] = new double[]{rGDS.getLo1(), rGDS.getLa1()};
//                Reproject.reprojectPoints(points, fromProj, aProjInfo, 0, 1);
//                s_X = points[0][0];
//                s_Y = points[0][1];
//
//                //Get X/Y
//                X = new double[rGDS.getNx()];
//                Y = new double[rGDS.getNy()];
//                for (i = 0; i < rGDS.getNx(); i++) {
//                    X[i] = s_X + rGDS.getDx() * i;
//                }
//
//                for (i = 0; i < rGDS.getNy(); i++) {
//                    Y[i] = s_Y + rGDS.getDy() * i;
//                }
//                break;
//        }
//
//        return new Object[]{X, Y};
//    }
    
    /**
     * Read array data of a variable
     * 
     * @param varName Variable name
     * @return Array data
     */
    @Override
    public Array read(String varName){
        return null;
    }
    
    /**
     * Read array data of the variable
     *
     * @param varName Variable name
     * @param origin The origin array
     * @param size The size array
     * @param stride The stride array
     * @return Array data
     */
    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        return null;
    }
    
    /**
     * Get global attributes
     * @return Global attributes
     */
    @Override
    public List<Attribute> getGlobalAttributes(){
        return new ArrayList<>();
    }

    @Override
    public String generateInfoText() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Get grid data
     *
     * @param varName Variable name
     * @return Grid data
     */
    @Override
    public GridArray getGridArray(String varName) {
        return null;    
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, int varIdx, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, int varIdx, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, int varIdx, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>
}