/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import java.util.List;

/**
 * A function that converts lists of {@linkplain DataFrame data frame} values to
 * aggregate results.
 *
 * <p>
 * Implementors define {@link #apply(Object)} to accept a list of data frame
 * values as input and return an aggregate result.</p>
 *
 * @param <I> the type of the input values
 * @param <O> the type of the result
 * @see DataFrame#aggregate(Aggregate)
 */
public interface Aggregate<I, O>
        extends Function<List<I>, O> {
}
