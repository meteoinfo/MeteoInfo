/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ndarray;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class DimArray {
    // <editor-fold desc="Variables">
    private Array array;
    private List<Dimension> dimensions;
    // </editor-fold>
    
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public DimArray() {
        this.array = null;
        this.dimensions = new ArrayList<>();
    }
    
    /**
     * Constructor
     * @param array Array
     * @param dims Dimesions
     */
    public DimArray(Array array, List<Dimension> dims) {
        this.array = array;
        this.dimensions = dims;
    }
    // </editor-fold>
    
    // <editor-fold desc="Get and set methods">
    /**
     * Get array
     * @return Array
     */
    public Array getArray() {
        return this.array;
    }
    
    /**
     * Set array
     * @param value Array
     */
    public void setArray(Array value) {
        this.array = value;
    }
    
    /**
     * Get dimensions
     * @return Dimensions
     */
    public List<Dimension> getDimensions() {
        return this.dimensions;
    }
    
    /**
     * Set dimensions
     * @param value Dimensions
     */
    public void setDimensions(List<Dimension> value) {
        this.dimensions = value;
    }
    // </editor-fold>
    
    // <editor-fold desc="Methods">
    /**
     * Get array size
     * @return Array size
     */
    public long getSize() {
        return this.array.getSize();
    }
    
    /**
     * Get array value by index
     * @param idx index
     * @return array value
     */
    public Object getValue(int idx) {
        return this.array.getObject(idx);
    }
    
    /**
     * Get dimension number
     * @return Dimension number
     */
    public int getDimNum() {
        return this.dimensions.size();
    }
    
    /**
     * Get a dimension by index
     * @param idx The index
     * @return Dimension
     */
    public Dimension getDimension(int idx) {
        return this.dimensions.get(idx);
    }
    
    /**
     * Set a dimension by index
     * @param idx Then index
     * @param dim Dimension
     */
    public void setDimension(int idx, Dimension dim) {
        this.dimensions.set(idx, dim);
    }
    
    /**
     * Get dimension value
     * @param dimIdx dimension index
     * @param vIdx value index
     * @return dimension value
     */
    public double getDimValue(int dimIdx, int vIdx) {
        return this.dimensions.get(dimIdx).getDimValue(vIdx);
    }
    
    /**
     * Section
     * @param origin Origin
     * @param size Size
     * @param stride Stride
     * @return Section result dim array
     * @throws InvalidRangeException 
     */
    public DimArray section(int[] origin, int[] size, int[] stride) throws InvalidRangeException {
        Array r = this.array.section(origin, size, stride);
        Array rr = Array.factory(r.getDataType(), r.getShape());
        MAMath.copy(rr, r);
        Section section = new Section(origin, size, stride);
        List<Dimension> dims = new ArrayList<>();
        for (int i = 0; i < section.getRank(); i++) {
            Range range = section.getRange(i);
            if (range.length() > 1) {
                Dimension dim = this.dimensions.get(i).extract(range);
                dims.add(dim);
            }
        }
        
        return new DimArray(rr, dims);
    }
    // </editor-fold>
}
