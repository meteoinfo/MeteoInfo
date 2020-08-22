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
package org.meteoinfo.plugin;

import javax.swing.*;

import org.meteoinfo.legend.LayersLegend;
import org.meteoinfo.map.MapView;

/**
 *
 * @author yaqiang
 */
public interface IApplication {
    /**
     * Get MapView object
     * @return MapView object
     */
    public MapView getMapView();  
    
    /**
     * Get LayersLegend object
     * @return LayersLegend object
     */
    public LayersLegend getMapDocument();
    
    /**
     * Get main menu bar object
     * @return Main menu bar object
     */
    public JMenuBar getMainMenuBar();
    
    /**
     * Get plugin menu
     * @return Plugin menu
     */
    public JMenu getPluginMenu();
    
    /**
     * Get tool bar panel
     * @return Tool bar panel
     */
    public JPanel getToolBarPanel();
    
    /**
     * Get current tool
     * @return Current tool
     */
    public AbstractButton getCurrentTool();
    
    /**
     * Set current tool
     * @param value Current tool
     */
    public void setCurrentTool(AbstractButton value);
    
    /**
     * Get progress bar
     * @return The main progress bar
     */
    public JProgressBar getProgressBar();
    
    /**
     * Get progress bar label
     * @return The progress bar label
     */
    public JLabel getProgressBarLabel();
    
    /**
     * Open project file
     * @param fileName The project file name
     */
    public void openProjectFile(String fileName);
        
}
