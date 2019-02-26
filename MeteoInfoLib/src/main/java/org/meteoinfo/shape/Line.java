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
package org.meteoinfo.shape;

import org.meteoinfo.global.MIMath;
import org.meteoinfo.global.PointD;

/**
 *
 * @author Yaqiang Wang
 */
public class Line {
    /// <summary>
    /// Point 1
    /// </summary>

    public PointD P1 = new PointD();
    /// <summary>
    /// Point 2
    /// </summary>
    public PointD P2 = new PointD();
    
    /**
     * Determine if the line is horizontal
     * @return Boolean
     */
    public boolean isHorizontal(){
        return (MIMath.doubleEquals(P1.Y, P2.Y));
    }
    
    /**
     * Determine if the line is vertical
     * @return Boolean
     */
    public boolean isVertical(){
        return (MIMath.doubleEquals(P1.X, P2.X));
    }
}
