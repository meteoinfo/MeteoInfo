/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

import java.awt.*;

/**
 *
 * @author Yaqiang Wang
 */
public interface IWebMapPanel {
    /**
     * Get web map layer zoom
     * @return Web map layer zoom
     */
    public abstract int getWebMapZoom();
    
    /**
     * Re draw function
     */
    public abstract void reDraw();

    /**
     * Re draw function
     * @param graphics2D Graphics2D object
     * @param width Width
     * @param height Height
     */
    public abstract void reDraw(Graphics2D graphics2D, int width, int height);
}
