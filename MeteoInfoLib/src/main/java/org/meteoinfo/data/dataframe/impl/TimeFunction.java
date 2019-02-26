/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.dataframe.impl;

import org.joda.time.DateTime;

/**
 * A function that converts DateTime value to String
 *
 * @param <I> the type of the input values
 * @param <O> the type of the result
 */
public interface TimeFunction<I, O> 
        extends Function<DateTime, String> {    
}
