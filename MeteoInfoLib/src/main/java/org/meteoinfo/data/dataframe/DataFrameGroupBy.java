/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe;

import org.meteoinfo.data.dataframe.impl.Aggregation;
import org.meteoinfo.data.dataframe.impl.Function;
import org.meteoinfo.data.dataframe.impl.Grouping;

/**
 *
 * @author Yaqiang Wang
 */
public class DataFrameGroupBy extends GroupBy {
    // <editor-fold desc="Variables">
    private final DataFrame dataFrame;
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
    @SuppressWarnings("unchecked")
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
    // </editor-fold>
}
