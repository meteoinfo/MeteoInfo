/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Yaqiang Wang
 * 
 * a property change listener which forces repaints when
 * tiles finish loading
 */
public class TileLoadListener implements PropertyChangeListener {

    IWebMapPanel panel = null;
    Graphics2D graphics2D = null;
    Integer width = null;
    Integer height = null;
    AffineTransform transform = null;

    /**
     * Constructor
     *
     * @param panel The IWebMapPanel to draw the tile
     */
    public TileLoadListener(IWebMapPanel panel) {
        this.panel = panel;
    }

    /**
     * Set Graphics2D object
     * @param graphics2D Graphics2D object
     */
    public void setGraphics2D(Graphics2D graphics2D) {
        this.graphics2D = graphics2D;
    }

    /**
     * Set width
     * @param width Width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Set height
     * @param height Height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Set transform
     * @param transform Transform
     */
    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("loaded".equals(evt.getPropertyName())
                && Boolean.TRUE.equals(evt.getNewValue())) {
            Tile t = (Tile) evt.getSource();
            if (t.getZoom() == this.panel.getWebMapZoom()) {
                if (this.graphics2D != null && this.width != null && this.height != null) {
                    if (this.transform != null)
                        this.graphics2D.setTransform(this.transform);
                    this.panel.reDraw(graphics2D, width, height);
                } else {
                    this.panel.reDraw();
                }
            }
        }
    }

}
