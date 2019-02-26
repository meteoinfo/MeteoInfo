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
package org.meteoinfo.data.meteodata;

import org.meteoinfo.layer.VectorLayer;

/**
 *
 * @author yaqiang
 */
public interface TrajDataInfo {
    // <editor-fold desc="Methods">
        
    /**
     * Create trajectory line layer
     *    
     * @return Map layer
     */
    public abstract VectorLayer createTrajLineLayer();
    
    /**
     * Create trajectory point layer
     *     
     * @return Map layer
     */
    public abstract VectorLayer createTrajPointLayer();
    
    /**
     * Create trajectory start point layer
     *     
     * @return Map layer
     */
    public abstract VectorLayer createTrajStartPointLayer();
    
    // </editor-fold>
}
