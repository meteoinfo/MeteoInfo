package org.meteoinfo.data.meteodata.numpy;

import org.meteoinfo.data.GridArray;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.DataInfo;
import org.meteoinfo.data.meteodata.IGridDataInfo;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.InvalidRangeException;
import org.meteoinfo.ndarray.io.npy.Npy;
import org.meteoinfo.ndarray.io.npy.NpyArray;
import org.meteoinfo.ndarray.io.npy.NpyUtil;

import java.io.*;

public class NumpyDataInfo extends DataInfo implements IGridDataInfo {
    @Override
    public boolean isValidFile(RandomAccessFile raf) {
        return false;
    }

    @Override
    public void readDataInfo(String fileName) {
        this.setFileName(fileName);
        File file = new File(fileName);
        NpyArray npyArray = Npy.read(file);
        Array array = NpyUtil.toMIArray(npyArray);

        this.addAttribute(new Attribute("File type", "Numpy"));
        int[] shape = npyArray.shape();
        String name = "a";
        Variable variable = new Variable();
        variable.setName(name);
        variable.setDataType(NpyUtil.toMIDataType(npyArray.dataType()));
        variable.setCachedData(array);
        for (int i = 0; i < shape.length; i++) {
            Dimension dim = new Dimension();
            dim.setName("dim_" + name + "_" + String.valueOf(i));
            dim.setLength(shape[i]);
            this.addDimension(dim);
            variable.addDimension(dim);
        }
        this.addVariable(variable);
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
