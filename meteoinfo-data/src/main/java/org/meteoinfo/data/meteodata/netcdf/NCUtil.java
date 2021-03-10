/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.netcdf;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.*;

/**
 *
 * @author Yaqiang Wang
 */
public class NCUtil {
    
    /**
     * Convert netcdf data type to meteothink data type
     * @param ncDataType Netcdf data type
     * @return MeteoThink data type
     */
    public static DataType convertDataType(ucar.ma2.DataType ncDataType) {
        DataType dataType = DataType.getType(ncDataType.toString());
        
        return dataType;
    }
    
    /**
     * Convert meteothink data type to netcdf data type
     * @param dataType MeteoThink data type
     * @return Netcdf data type
     */
    public static ucar.ma2.DataType convertDataType(DataType dataType) {
        ucar.ma2.DataType ncDataType = ucar.ma2.DataType.getType(dataType.toString());
        
        return ncDataType;
    }
    
    /**
     * Convert netcdf array to meteothink array
     * @param ncArray Netcdf array
     * @return MeteoThink array
     */
    public static Array convertArray(ucar.ma2.Array ncArray) {
        if (ncArray == null) {
            return null;
        }
        
        DataType dt = convertDataType(ncArray.getDataType());
        if (dt == DataType.OBJECT && ncArray.getObject(0).getClass() == String.class){
            dt = DataType.STRING;
        }
        Array array = null;
        switch (dt) {
            case STRUCTURE:
            case SEQUENCE:
                array = new ArrayStructure(ncArray.getShape());
                ((ArrayStructure)array).setArrayObject(ncArray);
                break;
            default:
                array = Array.factory(dt, ncArray.getShape(), ncArray.getStorage());
                break;
        }        
        
        return array;
    }
    
    /**
     * Convert meteothink array to netcdf array
     * @param array MeteoThink array
     * @return Netcdf array
     */
    public static ucar.ma2.Array convertArray(Array array) {
        ucar.ma2.Array ncArray = null;
        switch (array.getDataType()) {
            case STRUCTURE:
            case SEQUENCE:
                ncArray = (ucar.ma2.Array)((ArrayStructure)array).getArrayObject();
                break;
            default:
                ncArray = ucar.ma2.Array.factory(convertDataType(array.getDataType()), array.getShape(), array.getStorage());
                break;
        }
        
        return ncArray;
    }
    
    /**
     * Convert from netcdf dimension to meteothink dimension
     * @param ncDim Netcdf dimension
     * @return MeteoThink dimension
     */
    public static Dimension convertDimension(ucar.nc2.Dimension ncDim) {
        Dimension dim = new Dimension();
        dim.setShortName(ncDim.getShortName());
        dim.setLength(ncDim.getLength());
        dim.setUnlimited(ncDim.isUnlimited());
        dim.setShared(ncDim.isShared());
        dim.setVariableLength(ncDim.isVariableLength());
        
        return dim;
    }
    
    /**
     * Convert netcdf dimensions to meteothink dimensions
     * @param ncDims Netcdf dimensions
     * @return MeteoThink dimensions
     */
    public static List<Dimension> convertDimensions(List<ucar.nc2.Dimension> ncDims) {
        List<Dimension> dims = new ArrayList<>();
        for (ucar.nc2.Dimension ncDim : ncDims) {
            dims.add(convertDimension(ncDim));
        }
        
        return dims;
    }
    
    /**
     * Convert netcdf attribute to meteothink attribute
     * @param ncAttr Netcdf attribute
     * @return MeteoThink attribute
     */
    public static Attribute convertAttribute(ucar.nc2.Attribute ncAttr) {
        Attribute attr = new Attribute(ncAttr.getShortName());
        attr.setStringValue(ncAttr.getStringValue());
        attr.setValues(convertArray(ncAttr.getValues()));
        
        return attr;
    }
    
    /**
     * Convert meoteoinfo attribute to netcdf attribute
     * @param attr Attribute
     * @return MeteoInfo attribute
     */
    public static ucar.nc2.Attribute convertAttribute(Attribute attr) {
        ucar.nc2.Attribute ncAttr = new ucar.nc2.Attribute(attr.getShortName(), convertArray(attr.getValues()));
        
        return ncAttr;
    }
    
    /**
     * Convert netcdf variable to meteothink variable
     * @param ncVar Netcdf variable
     * @return MeteoThink variable
     */
    public static Variable convertVariable(ucar.nc2.Variable ncVar) {
        Variable var = new Variable();
        var.setName(ncVar.getFullName());
        var.setShortName(ncVar.getShortName());
        var.setDataType(convertDataType(ncVar.getDataType()));
        var.setDescription(ncVar.getDescription());
        var.setDimensions(convertDimensions(ncVar.getDimensions()));
        for (ucar.nc2.Attribute ncAttr : ncVar.getAttributes()) {
            var.addAttribute(convertAttribute(ncAttr));
        }
        var.setUnits(ncVar.getUnitsString());
        
        return var;
    }
    
    /**
     * Convert netcdf section to meteothink section
     * @param ncSection Netcdf section
     * @return Meteothink section
     */
    public static Section convertSection(ucar.ma2.Section ncSection) {
        try {
            List<Range> ranges = new ArrayList<>();
            for (ucar.ma2.Range range : ncSection.getRanges())
                ranges.add(new Range(range.getName(), range.first(), range.last(), range.stride()));
            Section section = new Section(ranges);
            return section;
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NCUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
