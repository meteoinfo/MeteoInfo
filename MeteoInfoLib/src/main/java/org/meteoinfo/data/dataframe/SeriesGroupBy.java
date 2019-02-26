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
public class SeriesGroupBy extends GroupBy {
    // <editor-fold desc="Variables">
    private final Series series;
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     * @param groups The groups
     * @param series The series
     */
    public SeriesGroupBy(Grouping groups, Series series) {
        this.groups = groups;
        this.series = series;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    @SuppressWarnings("unchecked")
    public <V> Series apply(final Function<?, ?> function) {
        Series s = this.groups.apply(this.series, function);
        if (this.series.getIndex() instanceof DateTimeIndex) {
            ((DateTimeIndex) s.getIndex()).setPeriod(((DateTimeIndex) this.series.getIndex()).getResamplePeriod());
        }
        return s;
    }
    
    /**
     * Compute the sum of the numeric columns for each group.
     *
     * @return the new series
     */
    @Override
    public Series count() {
        Series r = this.apply(new Aggregation.Count());
        return r;
    }

    /**
     * Compute the sum of the numeric columns for each group.
     *
     * @return the new series
     */
    @Override
    public Series sum() {
        Series r = this.apply(new Aggregation.Sum());
        return r;
    }

    /**
     * Compute the mean of the numeric columns for each group.
     *
     * @return the new series
     */
    @Override
    public Series mean() {
        Series r = this.apply(new Aggregation.Mean());
        return r;
    }
    
    /**
     * Compute the minimum of the numeric columns for each group.
     *
     * @return the new data frame
     */
    @Override
    public Series min() {
        Series r = this.apply(new Aggregation.Min());        
        return r;
    }
    
    /**
     * Compute the Maximum of the numeric columns for each group.
     *
     * @return the new series
     */
    @Override
    public Series max() {
        Series r = this.apply(new Aggregation.Max());
        return r;
    }
    
    /**
     * Compute the median of the numeric columns for each group.
     *
     * @return the new series
     */
    @Override
    public Series median() {
        Series r = this.apply(new Aggregation.Median());
        return r;
    }
    // </editor-fold>
}
