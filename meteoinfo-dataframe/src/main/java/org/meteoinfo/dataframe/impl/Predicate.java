/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.dataframe.impl;

import java.util.List;

/**
 * An interface used to filter a DataFrame.
 *
 * <p>
 * Implementors define {@link #apply(Object)} to return {@code true} for rows
 * that should be included in the filtered data frame.</p>
 *
 * @param <I> the type of the input values
 */
public interface Predicate<I>
        extends Function<List<I>, Boolean> {
}
