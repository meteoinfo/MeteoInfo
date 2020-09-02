/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import org.meteoinfo.data.dataframe.impl.Aggregation;
import org.meteoinfo.data.dataframe.impl.Function;
import org.meteoinfo.data.dataframe.impl.Grouping;
import org.meteoinfo.data.dataframe.impl.SparseBitSet;
import org.meteoinfo.ndarray.InvalidRangeException;

import java.util.*;

/**
 *
 * @author Yaqiang Wang
 */
public class DataFrameGroupBy extends GroupBy implements Iterable<Map.Entry<Object, DataFrame>> {
    // <editor-fold desc="Variables">
    private final DataFrame dataFrame;
    private Map<Object, DataFrame> dfGroups = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param groups The groups
     * @param dataFrame The data frame
     */
    public DataFrameGroupBy(Grouping groups, DataFrame dataFrame) {
        this.groups = groups;
        this.dataFrame = dataFrame;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Get group number
     * @return Group number
     */
    public int groupNumber() {
        return this.groups.keys().size();
    }

    /**
     * Get a group as DataFrame
     * @param key The group key
     * @return DataFrame
     * @throws InvalidRangeException
     */
    public DataFrame getGroup(Object key) throws InvalidRangeException {
        List<Integer> rows = new ArrayList<>();

        SparseBitSet _rows = this.groups.getGroup(key);
        for (int r = _rows.nextSetBit(0); r >= 0; r = _rows.nextSetBit(r + 1)) {
            rows.add(r);
        }

        return (DataFrame) this.dataFrame.select(rows);
    }

    /**
     * Apply a function
     * @param function The function
     * @param <V> Data type
     * @return Result DataFrame
     */
    public <V> DataFrame apply(final Function<?, ?> function) {
        DataFrame df = this.groups.apply(dataFrame, function);
        if (this.dataFrame.getIndex() instanceof DateTimeIndex) {
            ((DateTimeIndex) df.getIndex()).setPeriod(((DateTimeIndex) this.dataFrame.getIndex()).getResamplePeriod());
        }
        return df;
    }
    
    /**
     * Compute the sum of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public DataFrame count() {
        DataFrame r = this.apply(new Aggregation.Count());
        return r;
    }

    /**
     * Compute the sum of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public DataFrame sum() {
        DataFrame r = this.apply(new Aggregation.Sum());
        return r;
    }

    /**
     * Compute the mean of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public DataFrame mean() {
        DataFrame r = this.apply(new Aggregation.Mean());
        return r;
    }
    
    /**
     * Compute the minimum of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public DataFrame min() {
        DataFrame r = this.apply(new Aggregation.Min());        
        return r;
    }
    
    /**
     * Compute the Maximum of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public DataFrame max() {
        DataFrame r = this.apply(new Aggregation.Max());
        return r;
    }
    
    /**
     * Compute the median of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public DataFrame median() {
        DataFrame r = this.apply(new Aggregation.Median());
        return r;
    }
    
    /**
     * Compute the standard deviation of the numeric columns for each group.
     *
     * @return the new dataframe
     */
    @Override
    public DataFrame stdDev() {
        DataFrame r = this.apply(new Aggregation.StdDev<>());
        return r;
    }
    
    /**
     * Compute the percentile of the numeric columns for each group.
     *
     * @param quantile Quantile value
     * @return the new dataframe
     */
    @Override
    public DataFrame percentile(double quantile) {
        DataFrame r = this.apply(new Aggregation.Percentile<>(quantile * 100));
        return r;
    }

    @Override
    public Iterator<Map.Entry<Object, DataFrame>> iterator() {
        if (this.dfGroups == null) {
            this.dfGroups = new LinkedHashMap<>();
            Iterator<Map.Entry<Object, SparseBitSet>> iter = this.groups.iterator();
            while(iter.hasNext()) {
                Map.Entry<Object, SparseBitSet> v = iter.next();
                try {
                    this.dfGroups.put(v.getKey(), this.getGroup(v.getKey()));
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            }
        }
        return dfGroups.entrySet().iterator();
    }
    // </editor-fold>
}
