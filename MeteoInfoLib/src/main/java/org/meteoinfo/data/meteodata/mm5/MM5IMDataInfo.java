/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.mm5;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.global.util.JDateUtil;
import org.meteoinfo.ndarray.Dimension;
import org.meteoinfo.ndarray.DimensionType;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.MeteoDataType;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.global.DataConvert;
import org.meteoinfo.global.util.GlobalUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.data.meteodata.Attribute;

/**
 * MM5 regrid intermediate data info
 *
 * @author yaqiang
 */
public class MM5IMDataInfo extends DataInfo implements IGridDataInfo {

    // <editor-fold desc="Variables">
    private final ByteOrder _byteOrder = ByteOrder.BIG_ENDIAN;
    private DataOutputStream _bw = null;
    private final List<DataHead> _dataHeads = new ArrayList<DataHead>();
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public MM5IMDataInfo(){
        this.setDataType(MeteoDataType.MM5IM);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">    

    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        try {
            RandomAccessFile br = new RandomAccessFile(fileName, "r");
            List<Variable> variables = new ArrayList<>();
            List<LocalDateTime> times = new ArrayList<>();
            while (true) {
                if (br.getFilePointer() >= br.length() - 100) {
                    break;
                }

                long pos = br.getFilePointer();
                DataHead dh = this.readDataHead(br);
                if (!times.contains(dh.getDate()))
                    times.add(dh.getDate());
                dh.position = pos;
                dh.length = (int)(br.getFilePointer() - pos);
                _dataHeads.add(dh);
                int n = dh.idim * dh.jdim;
                br.skipBytes(n * 4 + 8);

                boolean isNewVar = true;
                for (Variable var : variables) {
                    if (var.getName().equals(dh.field)) {
                        isNewVar = false;
                        var.addLevel(dh.level);
                        break;
                    }
                }
                if (isNewVar) {
                    Variable var = new Variable();
                    var.setName(dh.field);
                    var.addLevel(dh.level);
                    var.setUnits(dh.units);
                    var.setDescription(dh.desc);
                    double[] X = new double[dh.idim];
                    int i;
                    for (i = 0; i < dh.idim; i++) {
                        X[i] = dh.startlon + dh.deltalon * i;
                    }
                    double[] Y = new double[dh.jdim];
                    for (i = 0; i < dh.jdim; i++) {
                        Y[i] = dh.startlat + dh.deltalat * (dh.jdim - 1 - i);
                    }
                    Dimension xdim = new Dimension(DimensionType.X);
                    xdim.setValues(X);
                    Dimension ydim = new Dimension(DimensionType.Y);
                    ydim.setValues(Y);
                    var.setXDimension(xdim);
                    var.setYDimension(ydim);
                    variables.add(var);
                    if (this._dataHeads.size() == 1) {
                        this.setXDimension(xdim);
                        this.setYDimension(ydim);
                    }
                }
            }
            
            List<Double> values = new ArrayList<>();
            for (LocalDateTime t : times) {
                values.add(JDateUtil.toOADate(t));
            }
            Dimension tDim = new Dimension(DimensionType.T);
            tDim.setValues(values);
            this.setTimeDimension(tDim);
            for (Variable var : variables){
                var.updateZDimension();
                var.setTDimension(tDim);
            }
            
            this.setVariables(variables);

            br.close();
        } catch (IOException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private DataHead readDataHead(RandomAccessFile br) throws IOException {
        DataHead dh = new DataHead();
        byte[] bytes;

        //Record 1: 4 + 8 bytes
        br.skipBytes(4);
        bytes = new byte[4];
        br.read(bytes);
        dh.iversion = DataConvert.bytes2Int(bytes, _byteOrder);
        br.skipBytes(4);

        //Record 2: 124 + 8 bytes       
        br.skipBytes(4);
        bytes = new byte[24];
        br.read(bytes);
        dh.hdate = new String(bytes).trim();
        bytes = new byte[4];
        br.read(bytes);
        dh.xfcst = DataConvert.bytes2Float(bytes, _byteOrder);
        bytes = new byte[9];
        br.read(bytes);
        dh.field = new String(bytes).trim();
        dh.field = dh.field.split("\\s+")[0];
        bytes = new byte[25];
        br.read(bytes);
        dh.units = new String(bytes).trim();
        bytes = new byte[46];
        br.read(bytes);
        dh.desc = new String(bytes).trim();
        bytes = new byte[4];
        br.read(bytes);
        dh.level = DataConvert.bytes2Float(bytes, _byteOrder);
        br.read(bytes);
        dh.idim = DataConvert.bytes2Int(bytes, _byteOrder);
        br.read(bytes);
        dh.jdim = DataConvert.bytes2Int(bytes, _byteOrder);
        br.read(bytes);
        dh.llflag = DataConvert.bytes2Int(bytes, _byteOrder);
        br.skipBytes(4);

        //Record 3: 16 + 8 bytes
        br.skipBytes(4);
        if (dh.llflag == 0) {
            br.read(bytes);
            dh.startlat = DataConvert.bytes2Float(bytes, _byteOrder);
            br.read(bytes);
            dh.startlon = DataConvert.bytes2Float(bytes, _byteOrder);
            br.read(bytes);
            dh.deltalat = DataConvert.bytes2Float(bytes, _byteOrder);
            br.read(bytes);
            dh.deltalon = DataConvert.bytes2Float(bytes, _byteOrder);
        }
        br.skipBytes(4);

        return dh;
    }
    
    private DataHead findDataHead(String varName, double level){
        for (DataHead dh : this._dataHeads){
            if (dh.field.equals(varName) && dh.level == level){
                return dh;
            }
        }
        
        return this._dataHeads.get(0);
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
        String dataInfo;
        dataInfo = "File Name: " + this.getFileName();        
        dataInfo += System.getProperty("line.separator") + "Xsize = " + String.valueOf(this.getXDimension().getLength())
                + "  Ysize = " + String.valueOf(this.getYDimension().getLength());               
        dataInfo += System.getProperty("line.separator") + "Number of Variables = " + String.valueOf(this.getVariableNum());
        for (String v : this.getVariableNames()) {
            dataInfo += System.getProperty("line.separator") + v;
        }

        return dataInfo;
    }
    
    /**
     * Read array data of a variable
     * 
     * @param varName Variable name
     * @return Array data
     */
    @Override
    public Array read(String varName){
        Variable var = this.getVariable(varName);
        int n = var.getDimNumber();
        int[] origin = new int[n];
        int[] size = new int[n];
        int[] stride = new int[n];
        for (int i = 0; i < n; i++){
            origin[i] = 0;
            size[i] = var.getDimLength(i);
            stride[i] = 1;
        }
        
        Array r = read(varName, origin, size, stride);
        
        return r;
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
    public GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx) {
        try {
            RandomAccessFile br = new RandomAccessFile(this.getFileName(), "r");
            Variable var = this.getVariable(varName);
            DataHead dh = this.findDataHead(var.getName(), var.getLevels().get(levelIdx));
            br.seek(dh.position + dh.length);
            int n = dh.idim * dh.jdim;
            br.skipBytes(4);
            byte[] dataBytes = new byte[n * 4];
            br.read(dataBytes);
            br.close();

            int i, j;
            double[][] theData = new double[dh.jdim][dh.idim];
            int start = 0;
            byte[] bytes = new byte[4];
            for (i = 0; i < dh.jdim; i++) {
                for (j = 0; j < dh.idim; j++) {
                    System.arraycopy(dataBytes, start, bytes, 0, 4);
                    theData[dh.jdim - 1 - i][j] = DataConvert.bytes2Float(bytes, _byteOrder);
                    start += 4;
                }
            }

            GridData gridData = new GridData();
            gridData.data = theData;
            gridData.missingValue = this.getMissingValue();
            double[] X = new double[dh.idim];
            for (i = 0; i < dh.idim; i++) {
                X[i] = dh.startlon + dh.deltalon * i;
            }
            double[] Y = new double[dh.jdim];
            for (i = 0; i < dh.jdim; i++) {
                Y[i] = dh.startlat + dh.deltalat * (dh.jdim - 1 - i);
            }
            gridData.xArray = X;
            gridData.yArray = Y;

            return gridData;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // </editor-fold>

    // <editor-fold desc="Write">
    /**
     * Create MM5 binary data file
     *
     * @param fileName File name
     */
    public void createDataFile(String fileName) {
        try {
            _bw = new DataOutputStream(new FileOutputStream(new File(fileName)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Close the data file created by previos step
     */
    public void closeDataFile() {
        try {
            _bw.close();
        } catch (IOException ex) {
            Logger.getLogger(MM5IMDataInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write data head
     *
     * @param dh The data head
     * @throws IOException
     */
    public void writeDataHead(DataHead dh) throws IOException {
        int skip = 4;
        //Record 1:
        _bw.writeInt(skip);
        _bw.writeInt(dh.iversion);
        _bw.writeInt(skip);

        //Record 2:
        skip = 124;
        _bw.writeInt(skip);
        _bw.writeBytes(GlobalUtil.padRight(dh.hdate, 24, ' '));
        _bw.writeFloat(dh.xfcst);
        _bw.writeBytes(GlobalUtil.padRight(dh.field, 9, ' '));
        _bw.writeBytes(GlobalUtil.padRight(dh.units, 25, ' '));
        _bw.writeBytes(GlobalUtil.padRight(dh.desc, 46, ' '));
        _bw.writeFloat(dh.level);
        _bw.writeInt(dh.idim);
        _bw.writeInt(dh.jdim);
        _bw.writeInt(dh.llflag);
        _bw.writeInt(skip);

        //Record 3:
        skip = 16;
        _bw.writeInt(skip);
        _bw.writeFloat(dh.startlat);
        _bw.writeFloat(dh.startlon);
        _bw.writeFloat(dh.deltalat);
        _bw.writeFloat(dh.deltalon);
        _bw.writeInt(skip);
    }

    /**
     * Write grid data
     *
     * @param gridData The grid data
     * @throws java.io.IOException
     */
    public void writeGridData(GridData gridData) throws IOException {
        int xn = gridData.getXNum();
        int yn = gridData.getYNum();
        byte[] dataBytes = new byte[xn * yn * 4];
        int start = 0;
        byte[] bytes;
        int i, j, k;
        for (i = 0; i < yn; i++) {
            for (j = 0; j < xn; j++) {
                bytes = DataConvert.float2Bytes((float) gridData.data[yn - 1 -i][j], _byteOrder);
                for (k = 0; k < 4; k++) {
                    dataBytes[start + k] = bytes[k];
                }
                start += 4;
            }
        }

        //Write data
        int skip = xn * yn * 4;
        _bw.writeInt(skip);
        _bw.write(dataBytes);
        _bw.writeInt(skip);
    }
    // </editor-fold>
}
