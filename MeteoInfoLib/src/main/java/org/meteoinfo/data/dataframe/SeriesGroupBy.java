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
public class SeriesGroupBy extends GroupBy implements Iterable<Map.Entry<Object, Series>> {
    // <editor-fold desc="Variables">
    private final Series series;
    private Map<Object, Series> sGroups = null;
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
    /**
     * Get a group as DataFrame
     * @param key The group key
     * @return DataFrame
     * @throws InvalidRangeException
     */
    public Series getGroup(Object key) throws InvalidRangeException {
        List<Integer> rows = new ArrayList<>();

        SparseBitSet _rows = this.groups.getGroup(key);
        for (int r = _rows.nextSetBit(0); r >= 0; r = _rows.nextSetBit(r + 1)) {
            rows.add(r);
        }

        return this.series.getValues(rows);
    }

    /**
     * Apply a function
     * @param function The function
     * @param <V> Data type
     * @return Series
     */
    public <V> Series apply(final Function<?, ?> function) {
        Series s = this.groups.apply(this.series, function);
        if (this.series.getIndex() instanceof DateTimeIndex &&
                s.getIndex() instanceof DateTimeIndex) {
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
    
    /**
     * Compute the standard deviation of the numeric columns for each group.
     *
     * @return the new series
     */
    @Override
    public Series stdDev() {
        Series r = this.apply(new Aggregation.StdDev<>());
        return r;
    }
    
    /**
     * Compute the percentile of the numeric columns for each group.
     *
     * @param quantile Quantile value
     * @return the new series
     */
    @Override
    public Series percentile(double quantile) {
        Series r = this.apply(new Aggregation.Percentile<>(quantile * 100));
        return r;
    }

    @Override
    public Iterator<Map.Entry<Object, Series>> iterator() {
        if (this.sGroups == null) {
            this.sGroups = new LinkedHashMap<>();
            Iterator<Map.Entry<Object, SparseBitSet>> iter = this.groups.iterator();
            while(iter.hasNext()) {
                Map.Entry<Object, SparseBitSet> v = iter.next();
                try {
                    this.sGroups.put(v.getKey(), this.getGroup(v.getKey()));
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            }
        }
        return sGroups.entrySet().iterator();
    }
    // </editor-fold>
}
