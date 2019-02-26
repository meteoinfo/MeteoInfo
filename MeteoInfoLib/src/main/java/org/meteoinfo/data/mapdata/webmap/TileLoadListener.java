/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Yaqiang Wang a property change listener which forces repaints when
 * tiles finish loading
 */
public class TileLoadListener implements PropertyChangeListener {

    IWebMapPanel panel = null;

    /**
     * Constructor
     *
     * @param panel The IWebMapPanel to draw the tile
     */
    public TileLoadListener(IWebMapPanel panel) {
        this.panel = panel;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("loaded".equals(evt.getPropertyName())
                && Boolean.TRUE.equals(evt.getNewValue())) {
            Tile t = (Tile) evt.getSource();
            if (t.getZoom() == this.panel.getWebMapZoom()) {
                this.panel.reDraw();
            }
        }
    }

}
