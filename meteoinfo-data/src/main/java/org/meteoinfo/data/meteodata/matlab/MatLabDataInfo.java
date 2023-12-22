package org.meteoinfo.data.meteodata.matlab;

import com.google.common.base.Charsets;
import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.io.matlab.MatLabUtil;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.*;
import us.hebi.matlab.mat.util.Bytes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MatLabDataInfo extends DataInfo implements IGridDataInfo {

    private static final String MAT5_IDENTIFIER = "MATLAB 5.0 MAT-file";

    @Override
    public boolean isValidFile(RandomAccessFile raf) {
        try {
            byte[] bytes = new byte[116];
            raf.seek(0);
            raf.read(bytes);
            String magic = MatLabUtil.parseAsciiString(bytes);
            if (magic.startsWith(MAT5_IDENTIFIER)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Test can open or not
     * @param fileName Data file name
     * @return Can open or not
     */
    public static boolean canOpen(String fileName) {
        try {
            byte[] bytes = new byte[116];
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            raf.seek(0);
            raf.read(bytes);
            raf.close();
            String magic = MatLabUtil.parseAsciiString(bytes);
            if (magic.startsWith(MAT5_IDENTIFIER)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void readDataInfo(String fileName) {
        // Iterate over all entries in the mat file
        try (Source source = Sources.openFile(fileName)){
            this.setFileName(fileName);
            MatFile mat = Mat5.newReader(source).readMat();
            this.addAttribute(new Attribute("File type", "MatLab"));
            for (MatFile.Entry entry : mat.getEntries()) {
                Matrix array = (Matrix) entry.getValue();
                int[] dimensions = array.getDimensions();
                List<Dimension> dimensionList = new ArrayList<>();
                for (int i = 0; i < dimensions.length; i++) {
                    Dimension dimension = new Dimension("dim_" + entry.getName() + "_" + String.valueOf(i + 1), dimensions[i]);
                    this.addDimension(dimension);
                    dimensionList.add(dimension);
                }
                Variable variable = new Variable();
                variable.setName(entry.getName());
                variable.setDataType(MatLabUtil.fromMatLabDataType(array.getType()));
                variable.addAttribute("Name", entry.getName());
                for (Dimension dimension : dimensionList) {
                    variable.addDimension(dimension);
                }
                variable.setCachedData(MatLabUtil.fromMatLabArray(array));
                this.addVariable(variable);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Array read(String varName) {
        Variable var = this.getVariable(varName);
        int n = var.getDimNumber();
        int[] origin = new int[n];
        int[] size = new int[n];
        int[] stride = new int[n];
        for (int i = 0; i < n; i++) {
            origin[i] = 0;
            size[i] = var.getDimLength(i);
            stride[i] = 1;
        }

        Array r = read(varName, origin, size, stride);

        return r;
    }

    @Override
    public Array read(String varName, int[] origin, int[] size, int[] stride) {
        try {
            Variable variable = this.getVariable(varName);
            return variable.getCachedData().section(origin, size, stride).copy();
        } catch (InvalidRangeException e) {
            return null;
        }
    }

    @Override
    public GridArray getGridArray(String varName) {
        return null;
    }

    @Override
    public GridData getGridData_LonLat(int timeIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_TimeLat(int lonIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_TimeLon(int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelLat(int lonIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelLon(int latIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_LevelTime(int latIdx, String varName, int lonIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Time(int lonIdx, int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Level(int lonIdx, int latIdx, String varName, int timeIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Lon(int timeIdx, int latIdx, String varName, int levelIdx) {
        return null;
    }

    @Override
    public GridData getGridData_Lat(int timeIdx, int lonIdx, String varName, int levelIdx) {
        return null;
    }
}
