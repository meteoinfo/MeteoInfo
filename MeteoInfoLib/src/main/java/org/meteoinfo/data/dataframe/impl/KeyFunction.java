/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import java.util.List;

/**
 * A function that converts {@linkplain DataFrame data frame} rows to index or
 * group keys.
 *
 * <p>
 * Implementors define {@link #apply(Object)} to accept a data frame row as
 * input and return a key value, most commonly used by
 * {@link DataFrame#groupBy(KeyFunction)}.</p>
 *
 * @param <I> the type of the input values
 * @see DataFrame#groupBy(KeyFunction)
 */
public interface KeyFunction<I>
        extends Function<List<I>, Object> {
}
