/*
 * Joinery -- Data frames for Java
 * Copyright (c) 2014, 2015 IBM Corp.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meteoinfo.data.dataframe.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.meteoinfo.data.ArrayUtil;

import org.meteoinfo.data.dataframe.DataFrame;
import org.meteoinfo.data.dataframe.Series;
import org.meteoinfo.data.dataframe.impl.Transforms.CumulativeFunction;
import ucar.ma2.Array;

public class Grouping
        implements Iterable<Map.Entry<Object, SparseBitSet>> {

    private final Map<Object, SparseBitSet> groups = new LinkedHashMap<>();
    private final Set<Integer> columns = new LinkedHashSet<>();

    public Grouping() {
    }

    public <V> Grouping(final Series series, final KeyFunction<V> function) {
        final Iterator<List<V>> iter = series.iterator();
        for (int r = 0; iter.hasNext(); r++) {
            final List<V> row = iter.next();
            final Object key = function.apply(row);
            SparseBitSet group = groups.get(key);
            if (group == null) {
                group = new SparseBitSet();
                groups.put(key, group);
            }
            group.set(r);
        }
    }
    
    public <V> Grouping(final Series series, final TimeFunction<DateTime, String> function) {
        final Iterator iter = series.getIndex().iterator();
        for (int r = 0; iter.hasNext(); r++) {
            final Object row = iter.next();
            final String key = function.apply((DateTime)row);
            SparseBitSet group = groups.get(key);
            if (group == null) {
                group = new SparseBitSet();
                groups.put(key, group);
            }
            group.set(r);
        }
    }

    public <V> Grouping(final Series series) {
        this(series, new KeyFunction<V>() {
            @Override
            public Object apply(final List<V> value) {
                return value.get(0);
            }
        });
    }

    public <V> Grouping(final Series series, final WindowFunction function) {
        final Iterator iter = series.getIndex().iterator();
        for (int r = 0; iter.hasNext(); r++) {
            final DateTime row = (DateTime) iter.next();
            final Object key = function.apply(row);
            SparseBitSet group = groups.get(key);
            if (group == null) {
                group = new SparseBitSet();
                groups.put(key, group);
            }
            group.set(r);
        }
    }

    public <V> Grouping(final DataFrame df, final KeyFunction<V> function, final Integer... columns) {
        final Iterator<List<V>> iter = df.iterator();
        for (int r = 0; iter.hasNext(); r++) {
            final List<V> row = iter.next();
            final Object key = function.apply(row);
            SparseBitSet group = groups.get(key);
            if (group == null) {
                group = new SparseBitSet();
                groups.put(key, group);
            }
            group.set(r);
        }

        for (final int column : columns) {
            this.columns.add(column);
        }
    }

    public <V> Grouping(final DataFrame df, final Integer... columns) {
        this(
                df,
                columns.length == 1
                        ? new KeyFunction<V>() {
                    @Override
                    public Object apply(final List<V> value) {
                        return value.get(columns[0]);
                    }

                }
                        : new KeyFunction<V>() {
                    @Override
                    public Object apply(final List<V> value) {
                        final List<Object> key = new ArrayList<>(columns.length);
                        for (final int column : columns) {
                            key.add(value.get(column));
                        }
                        return Collections.unmodifiableList(key);
                    }
                },
                columns
        );
    }

    public <V> Grouping(final DataFrame df, final WindowFunction function) {
        final Iterator iter = df.getIndex().iterator();
        for (int r = 0; iter.hasNext(); r++) {
            final DateTime row = (DateTime) iter.next();
            final Object key = function.apply(row);
            SparseBitSet group = groups.get(key);
            if (group == null) {
                group = new SparseBitSet();
                groups.put(key, group);
            }
            group.set(r);
        }
    }

    @SuppressWarnings("unchecked")
    public <V> Series apply(final Series series, final Function<?, ?> function) {
        if (series.isEmpty()) {
            return series;
        }

        final String name = series.getName();
        final List<Object> index = new ArrayList<>();

        // construct new row index
        if (function instanceof Aggregate && !groups.isEmpty()) {
            for (final Object key : groups.keySet()) {
                index.add(key);
            }
        }

        // add aggregated data column
        final List<V> column = new ArrayList<>();
        if (groups.isEmpty()) {
            try {
                if (function instanceof Aggregate) {
                    column.add((V) Aggregate.class.cast(function).apply(series.getData()));
                } else {
                    for (int r = 0; r < series.size(); r++) {
                        column.add((V) Function.class.cast(function).apply(series.getValue(r)));
                    }
                }
            } catch (final ClassCastException ignored) {
            }

            if (function instanceof CumulativeFunction) {
                CumulativeFunction.class.cast(function).reset();
            }
        } else {
            for (final Map.Entry<Object, SparseBitSet> entry : groups.entrySet()) {
                final SparseBitSet rows = entry.getValue();
                try {
                    if (function instanceof Aggregate) {
                        final List<V> values = new ArrayList<>(rows.cardinality());
                        for (int r = rows.nextSetBit(0); r >= 0; r = rows.nextSetBit(r + 1)) {
                            values.add((V) series.getValue(r));
                        }
                        column.add((V) Aggregate.class.cast(function).apply(values));
                    } else {
                        for (int r = rows.nextSetBit(0); r >= 0; r = rows.nextSetBit(r + 1)) {
                            column.add((V) Function.class.cast(function).apply(series.getValue(r)));
                        }
                    }
                } catch (final ClassCastException ignored) {
                }

                if (function instanceof CumulativeFunction) {
                    CumulativeFunction.class.cast(function).reset();
                }
            }
        }

        if (!column.isEmpty()) {
            Array grouped = ArrayUtil.array(column);
            return new Series(grouped, index, name);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <V> DataFrame apply(final DataFrame df, final Function<?, ?> function) {
        if (df.isEmpty()) {
            return df;
        }

        final List<Array> grouped = new ArrayList<>();
        final List<String> names = df.getColumns().getNames();
        final List<Object> newcols = new ArrayList<>();
        final List<Object> index = new ArrayList<>();

        // construct new row index
        if (function instanceof Aggregate && !groups.isEmpty()) {
            for (final Object key : groups.keySet()) {
                index.add(key);
            }
        }
        
//        // add key columns
//        for (final int c : columns) {
//            if (function instanceof Aggregate && !groups.isEmpty()) {
//                final List<V> column = new ArrayList<>();
//                for (final Map.Entry<Object, SparseBitSet> entry : groups.entrySet()) {
//                    final SparseBitSet rows = entry.getValue();
//                    final int r = rows.nextSetBit(0);
//                    column.add((V)df.getValue(r, c));
//                }
//                grouped.add(ArrayUtil.array(column));
//                newcols.add(names.get(c));
//            } else {
//                try {
//                    grouped.add(df.getColumnData(c));
//                    newcols.add(names.get(c));
//                } catch (InvalidRangeException ex) {
//                    Logger.getLogger(Grouping.class.getName()).log(Level.SEVERE, null, ex);
//                }                
//            }
//        }
        
        // add aggregated data columns
        for (int c = 0; c < df.size(); c++) {
            if (!columns.contains(c)) {
                final List<V> column = new ArrayList<>();
                if (groups.isEmpty()) {
                    try {
                        if (function instanceof Aggregate) {
                            column.add((V) Aggregate.class.cast(function).apply(df.col(c)));
                        } else {
                            for (int r = 0; r < df.length(); r++) {
                                column.add((V) Function.class.cast(function).apply(df.getValue(r, c)));
                            }
                        }
                    } catch (final ClassCastException ignored) {
                    }

                    if (function instanceof CumulativeFunction) {
                        CumulativeFunction.class.cast(function).reset();
                    }
                } else {
                    for (final Map.Entry<Object, SparseBitSet> entry : groups.entrySet()) {
                        final SparseBitSet rows = entry.getValue();
                        try {
                            if (function instanceof Aggregate) {
                                final List<V> values = new ArrayList<>(rows.cardinality());
                                for (int r = rows.nextSetBit(0); r >= 0; r = rows.nextSetBit(r + 1)) {
                                    values.add((V) df.getValue(r, c));
                                }
                                column.add((V) Aggregate.class.cast(function).apply(values));
                            } else {
                                for (int r = rows.nextSetBit(0); r >= 0; r = rows.nextSetBit(r + 1)) {
                                    column.add((V) Function.class.cast(function).apply(df.getValue(r, c)));
                                }
                            }
                        } catch (final ClassCastException ignored) {
                        }

                        if (function instanceof CumulativeFunction) {
                            CumulativeFunction.class.cast(function).reset();
                        }
                    }
                }

                if (!column.isEmpty()) {
                    grouped.add(ArrayUtil.array(column));
                    newcols.add(names.get(c));
                }
            }
        }

//        if (newcols.size() <= columns.size()) {
//            throw new IllegalArgumentException(
//                    "no results for aggregate function "
//                    + function.getClass().getSimpleName()
//            );
//        }
        if (index.isEmpty()){
            for (int i = 0; i < grouped.get(0).getSize(); i++){
                index.add(i);
            }
        }
        return new DataFrame(grouped, index, newcols);
    }

    public Set<Object> keys() {
        return groups.keySet();
    }

    public Set<Integer> columns() {
        return columns;
    }

    @Override
    public Iterator<Map.Entry<Object, SparseBitSet>> iterator() {
        return groups.entrySet().iterator();
    }
}
