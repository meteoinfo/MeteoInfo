/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.meteoinfo.data.dataframe.DataFrame;
import org.meteoinfo.data.dataframe.Series;

/**
 *
 * Ported from joinery
 */
public class Views {

    public static class ListView<V>
            extends AbstractList<List<V>> {

        private final Object df;
        private final boolean transpose;

        public ListView(final DataFrame df, final boolean transpose) {
            this.df = df;
            this.transpose = transpose;
        }
        
        public ListView(final Series df) {
            this.df = df;
            this.transpose = false;
        }

        @Override
        public List<V> get(final int index) {
            return new DataFrameListView<>((DataFrame)df, index, !transpose);
        }

        @Override
        public int size() {
            return transpose ? ((DataFrame)df).length() : ((DataFrame)df).size();
        }
    }

    public static class DataFrameListView<V>
            extends AbstractList<V> {

        private final DataFrame df;
        private final int index;
        private final boolean transpose;

        public DataFrameListView(final DataFrame df, final int index, final boolean transpose) {
            this.df = df;
            this.index = index;
            this.transpose = transpose;
        }

        @Override
        public V get(final int index) {
            return transpose ? (V) df.getValue(index, this.index) : (V) df.getValue(this.index, index);
        }

        @Override
        public int size() {
            return transpose ? df.length() : df.size();
        }
    }
    
    public static class SeriesListView<V>
    extends AbstractList<V> {
        private final DataFrame df;
        private final int index;
        private final boolean transpose;

        public SeriesListView(final DataFrame df, final int index, final boolean transpose) {
            this.df = df;
            this.index = index;
            this.transpose = transpose;
        }

        @Override
        public V get(final int index) {
            return transpose ? (V)df.getValue(index, this.index) : (V)df.getValue(this.index, index);
        }

        @Override
        public int size() {
            return transpose ? df.length() : df.size();
        }
    }

    public static class MapView<V>
            extends AbstractList<Map<Object, V>> {

        private final DataFrame df;
        private final boolean transpose;

        public MapView(final DataFrame df, final boolean transpose) {
            this.df = df;
            this.transpose = transpose;
        }

        @Override
        public Map<Object, V> get(final int index) {
            return new DataFrameMapView<>(df, index, !transpose);
        }

        @Override
        public int size() {
            return transpose ? df.length() : df.size();
        }
    }

    public static class DataFrameMapView<V>
            extends AbstractMap<Object, V> {

        private final DataFrame df;
        private final int index;
        private final boolean transpose;

        public DataFrameMapView(final DataFrame df, final int index, final boolean transpose) {
            this.df = df;
            this.index = index;
            this.transpose = transpose;
        }

        @Override
        public Set<Map.Entry<Object, V>> entrySet() {
            return new AbstractSet<Map.Entry<Object, V>>() {
                @Override
                public Iterator<Map.Entry<Object, V>> iterator() {
                    final List<Object> names = transpose ? df.getIndex().getValues() : df.getColumns().getNames();
                    final Iterator<Object> it = names.iterator();

                    return new Iterator<Map.Entry<Object, V>>() {
                        int value = 0;

                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Map.Entry<Object, V> next() {
                            final Object key = it.next();
                            final int value = this.value++;
                            return new Map.Entry<Object, V>() {
                                @Override
                                public Object getKey() {
                                    return key;
                                }

                                @Override
                                public V getValue() {
                                    return transpose
                                            ? (V) df.getValue(value, index)
                                            : (V) df.getValue(index, value);
                                }

                                @Override
                                public V setValue(final V value) {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public int size() {
                    return transpose ? df.length() : df.size();
                }
            };
        }
    }

    public static class TransformedView<V, U>
            extends AbstractList<List<U>> {

        protected final DataFrame df;
        protected final Function<V, U> transform;
        protected final boolean transpose;

        public TransformedView(final DataFrame df, final Function<V, U> transform, final boolean transpose) {
            this.df = df;
            this.transform = transform;
            this.transpose = transpose;
        }

        @Override
        public List<U> get(final int index) {
            return new TransformedSeriesView<>(df, transform, index, !transpose);
        }

        @Override
        public int size() {
            return transpose ? df.length() : df.size();
        }
    }

    public static class TransformedSeriesView<V, U>
            extends AbstractList<U> {

        protected final DataFrame df;
        protected final int index;
        protected final boolean transpose;
        protected final Function<V, U> transform;

        public TransformedSeriesView(final DataFrame df, final Function<V, U> transform, final int index, final boolean transpose) {
            this.df = df;
            this.transform = transform;
            this.index = index;
            this.transpose = transpose;
        }

        @Override
        public U get(final int index) {
            final V value = transpose ? (V) df.getValue(index, this.index) : (V) df.getValue(this.index, index);
            return transform.apply(value);
        }

        @Override
        public int size() {
            return transpose ? df.length() : df.size();
        }
    }

    public static class FlatView<V>
            extends AbstractList<V> {

        private final DataFrame df;

        public FlatView(final DataFrame df) {
            this.df = df;
        }

        @Override
        public V get(final int index) {
            return (V) df.getValue(index % df.length(), index / df.length());
        }

        @Override
        public int size() {
            return df.size() * df.length();
        }
    }

    public static class FillNaFunction<V>
            implements Function<V, V> {

        private final V fill;

        public FillNaFunction(final V fill) {
            this.fill = fill;
        }

        @Override
        public V apply(final V value) {
            return value == null ? fill : value;
        }
    }
}
