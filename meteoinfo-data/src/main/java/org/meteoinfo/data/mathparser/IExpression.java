 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.data.mathparser;

/**
 *
 * @author yaqiang
 */
public interface IExpression {
    /**
     * Get the number of arguments this expression uses
     * @return The argument count
     */
    public int getArgumentCount();
    
    /**
     * Math evaluate
     * @param numbers The numbers to evaluate
     * @return The result of the evaluated numbers
     */
    public Object evaluate(Object[] numbers);
    
}
