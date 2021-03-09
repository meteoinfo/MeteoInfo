/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.dataframe.impl;

import java.util.List;

/**
 * A function that converts data frame rows to index or
 * group keys.
 *
 * <p>
 * Implementors define {@link #apply(Object)} to accept a data frame row as
 * input and return a key value, most commonly used by
 * DataFrame.groupBy(KeyFunction).</p>
 *
 * @param <I> the type of the input values
 */
public interface KeyFunction<I>
        extends Function<List<I>, Object> {
}
