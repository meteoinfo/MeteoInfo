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
package org.meteoinfo.layer;

import java.util.ArrayList;

/**
 *
 * @author Yaqiang Wang
 */
public class LayerCollection extends ArrayList<MapLayer> {
    // <editor-fold desc="Variables">

    private int _selectedLayer;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public LayerCollection() {
        _selectedLayer = -1;
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get selected layer
     *
     * @return Selected layer handle
     */
    public int getSelectedLayer() {
        return _selectedLayer;
    }

    /**
     * Set selected layer handle
     *
     * @param handle Layer handle
     */
    public void setSelectedLayer(int handle) {
        _selectedLayer = handle;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
