/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.meteodata.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meteoinfo.data.dimarray.Dimension;
import org.meteoinfo.data.meteodata.Attribute;
import org.meteoinfo.data.meteodata.Variable;
import org.meteoinfo.ndarray.*;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

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
                array = array.copyIfView();
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
        var.setMemberOfStructure(ncVar.isMemberOfStructure());
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

    /**
     * Get pack data from variable
     * @param var The variable
     * @return Pack data
     */
    public static double[] getPackData(ucar.nc2.Variable var) {
        double add_offset = 0, scale_factor = 1, missingValue = Double.NaN;
        for (int i = 0; i < var.getAttributes().size(); i++) {
            ucar.nc2.Attribute att = var.getAttributes().get(i);
            String attName = att.getShortName();
            if (attName.equals("add_offset")) {
                add_offset = Double.parseDouble(att.getValue(0).toString());
            }

            if (attName.equals("scale_factor")) {
                scale_factor = Double.parseDouble(att.getValue(0).toString());
            }

            if (attName.equals("missing_value")) {
                try {
                    missingValue = Double.parseDouble(att.getValue(0).toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            //MODIS NetCDF data
            if (attName.equals("_FillValue")) {
                try {
                    missingValue = Double.parseDouble(att.getValue(0).toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return new double[]{add_offset, scale_factor, missingValue};
    }

    /**
     * Get pack data from variable
     * @param var The variable
     * @return Pack data
     */
    public static double[] getPackData(Variable var) {
        double add_offset = 0, scale_factor = 1, missingValue = Double.NaN;
        for (int i = 0; i < var.getAttributes().size(); i++) {
            Attribute att = var.getAttributes().get(i);
            String attName = att.getShortName();
            if (attName.equals("add_offset")) {
                add_offset = Double.parseDouble(att.getValue(0).toString());
            }

            if (attName.equals("scale_factor")) {
                scale_factor = Double.parseDouble(att.getValue(0).toString());
            }

            if (attName.equals("missing_value")) {
                try {
                    missingValue = Double.parseDouble(att.getValue(0).toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            //MODIS NetCDF data
            if (attName.equals("_FillValue")) {
                try {
                    missingValue = Double.parseDouble(att.getValue(0).toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return new double[]{add_offset, scale_factor, missingValue};
    }

    /**
     * Get missing value from variable
     * @param var The variable
     * @return Missing value
     */
    public static double getMissingValue(Variable var) {
        double missingValue = Double.NaN;
        for (int i = 0; i < var.getAttributes().size(); i++) {
            Attribute att = var.getAttributes().get(i);
            String attName = att.getShortName();

            if (attName.equals("missing_value")) {
                try {
                    missingValue = Double.parseDouble(att.getValue(0).toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            //MODIS NetCDF data
            if (attName.equals("_FillValue")) {
                try {
                    missingValue = Double.parseDouble(att.getValue(0).toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return missingValue;
    }

    /**
     * Read data array from an ucar ArraySequence
     *
     * @param parentArray The ucar ArraySequence
     * @param memberName Member name
     * @return Read data array
     */
    public static Array readSequence(ucar.ma2.ArrayStructure parentArray, String memberName) throws IOException {
        ucar.ma2.StructureMembers.Member member = parentArray.findMember(memberName);
        ucar.ma2.Array r = parentArray.extractMemberArray(member);

        return convertArray(r);
    }

    /**
     * Read data array from an ucar ArrayObject with ArraySequence elements
     *
     * @param parentArray The ucar ArrayObject with ArraySequence elements
     * @param memberName Member name
     * @param index Record index
     * @param missingValue Missing value
     * @return Read data array
     */
    public static Array readSequenceRecord(ucar.ma2.ArrayObject parentArray, String memberName,
                                           int index, double missingValue) throws IOException {
        int n = (int) parentArray.getSize();
        ucar.ma2.IndexIterator pIter = parentArray.getIndexIterator();
        ucar.ma2.StructureMembers.Member member = null;
        while (pIter.hasNext()) {
            ucar.ma2.ArrayStructure sArray = (ucar.ma2.ArrayStructure) pIter.getObjectNext();
            if (sArray != null) {
                member = sArray.findMember(memberName);
                break;
            }
        }

        DataType dataType = convertDataType(member.getDataType());
        Array r = Array.factory(dataType, new int[]{n});
        pIter = parentArray.getIndexIterator();
        IndexIterator rIter = r.getIndexIterator();
        while (pIter.hasNext()) {
            ucar.ma2.ArrayStructure sArray = (ucar.ma2.ArrayStructure) pIter.getObjectNext();
            if (sArray == null) {
                rIter.setObjectNext(missingValue);
            } else {
                member = sArray.findMember(memberName);
                ucar.ma2.Array a = sArray.extractMemberArray(member);
                if (a.getSize() > index) {
                    rIter.setObjectNext(a.getObject(index));
                } else {
                    rIter.setObjectNext(missingValue);
                }
            }
        }

        return r;
    }

    /**
     * Read data array from an ucar ArrayObject with ArraySequence elements
     *
     * @param parentArray The ucar ArrayObject with ArraySequence elements
     * @param memberName Member name
     * @param index Record index
     * @param missingValue Missing value
     * @return Read data array
     */
    public static Array readSequenceRecord(ucar.ma2.ArrayObject parentArray, String memberName,
                                           int index) throws IOException {
        return readSequenceRecord(parentArray, memberName, index, Double.NaN);
    }

    /**
     * Read data array from an ucar ArrayObject with ArraySequence elements
     *
     * @param parentArray The ucar ArrayObject with ArraySequence elements
     * @param memberName Member name
     * @param index Sequence index
     * @return Read data array
     */
    public static Array readSequence(ucar.ma2.ArrayObject parentArray, String memberName,
                                            int index) throws IOException {
        int n = (int) parentArray.getSize();
        ucar.ma2.ArrayStructure sArray = (ucar.ma2.ArrayStructure) parentArray.getObject(index);
        if (sArray == null) {
            return null;
        }

        ucar.ma2.StructureMembers.Member member = sArray.findMember(memberName);
        ucar.ma2.Array r = sArray.extractMemberArray(member);

        return convertArray(r);
    }

    /**
     * Unpack an array
     * @param a The array
     * @param variable The variable including packing parameters
     * @return Unpacked data
     */
    public static Array arrayUnPack(Array a, Variable variable) {
        double[] packValues = getPackData(variable);
        double addOffset = packValues[0];
        double scaleFactor = packValues[1];
        double missingValue = packValues[2];

        return ArrayUtil.unPack(a, missingValue, scaleFactor, addOffset);
    }
}
