/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.laboratory.gui;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.ScreenDockStation;
import bibliothek.gui.dock.station.screen.ScreenDockWindow;
import bibliothek.gui.dock.station.screen.ScreenDockWindowFactory;
import bibliothek.gui.dock.station.screen.window.WindowConfiguration;

/**
 *
 * @author wyq
 */
public class CustomWindowFactory implements ScreenDockWindowFactory {

    @Override
    public ScreenDockWindow createWindow(ScreenDockStation station, WindowConfiguration configuration) {                 
        return new FrmCustom(station, configuration);
    }

    @Override
    public ScreenDockWindow updateWindow(ScreenDockWindow window, WindowConfiguration configuration, ScreenDockStation station) {
        return createWindow(station, configuration);
    }
    
}