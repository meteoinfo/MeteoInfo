/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.meteoinfo.data.dataframe.DataFrame;
import org.meteoinfo.data.dataframe.Index;

/**
 *
 * Ported from joinery
 */
public class Selection {
    public static <V> SparseBitSet select(final DataFrame df, final Predicate<V> predicate) {
        final SparseBitSet selected = new SparseBitSet();
        final Iterator<List<V>> rows = df.iterator();
        for (int r = 0; rows.hasNext(); r++) {
            if (predicate.apply(rows.next())) {
                selected.set(r);
            }
        }
        return selected;
    }

    public static Index select(final Index index, final SparseBitSet selected) {
        final List<Object> names = new ArrayList<>(index.getValues());
        final Index newidx = new Index();
        for (int r = selected.nextSetBit(0); r >= 0; r = selected.nextSetBit(r + 1)) {
            final Object name = names.get(r);
            newidx.add(name);
        }
        return newidx;
    }

    public static <V> BlockManager<V> select(final BlockManager<V> blocks, final SparseBitSet selected) {
        final List<List<V>> data = new LinkedList<>();
        for (int c = 0; c < blocks.size(); c++) {
            final List<V> column = new ArrayList<>(selected.cardinality());
            for (int r = selected.nextSetBit(0); r >= 0; r = selected.nextSetBit(r + 1)) {
                column.add(blocks.get(c, r));
            }
            data.add(column);
        }
        return new BlockManager<>(data);
    }

    public static <V> BlockManager<V> select(final BlockManager<V> blocks, final SparseBitSet rows, final SparseBitSet cols) {
        final List<List<V>> data = new LinkedList<>();
        for (int c = cols.nextSetBit(0); c >= 0; c = cols.nextSetBit(c + 1)) {
            final List<V> column = new ArrayList<>(rows.cardinality());
            for (int r = rows.nextSetBit(0); r >= 0; r = rows.nextSetBit(r + 1)) {
                column.add(blocks.get(c, r));
            }
            data.add(column);
        }
        return new BlockManager<>(data);
    }

    public static <V> SparseBitSet[] slice(final DataFrame df,
            final Integer rowStart, final Integer rowEnd, final Integer colStart, final Integer colEnd) {
        final SparseBitSet rows = new SparseBitSet();
        final SparseBitSet cols = new SparseBitSet();
        rows.set(rowStart, rowEnd);
        cols.set(colStart, colEnd);
        return new SparseBitSet[] { rows, cols };
    }

    public static class DropNaPredicate<V>
    implements Predicate<V> {
        @Override
        public Boolean apply(final List<V> values) {
            for (final V value : values) {
                if (value == null) {
                    return false;
                }
            }
            return true;
        }
    }
}
