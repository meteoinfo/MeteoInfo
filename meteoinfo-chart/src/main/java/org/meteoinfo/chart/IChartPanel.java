/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.*;

/**
 *
 * @author yaqiang
 */
public interface IChartPanel {
    /**
     * Get whether chart panel is loading
     * @return Boolean
     */
    public abstract boolean isLoading();

    /**
     * Set whether chart panel is loading
     * @param loading Boolean
     */
    public abstract void setLoading(boolean loading);

    /**
     * Save image
     * @param fn Image file name
     */
    public abstract void saveImage(String fn);
    
    /**
     * Set mouse mode
     * @param value Mouse mode
     */
    public abstract void setMouseMode(MouseMode value);
    
    /**
     * Zoom back to full extent
     */
    public abstract void onUndoZoomClick();
    
    /**
     * Paint graphics
     */
    public abstract void paintGraphics();

    public abstract void paintGraphics(Graphics2D g, int width, int height);
}
