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
package org.meteoinfo.legend;

import org.meteoinfo.global.FrmProperty;
import org.meteoinfo.layer.MapLayer;
import javax.swing.JPanel;

/**
 *
 * @author Yaqiang Wang
 */
public class LegendSchemeControl extends JPanel {
    // <editor-fold desc="Variables">

    public FrmProperty m_FrmSS = null;
    private MapLayer _mapLayer;
    //Form m_FrmMeteoData = new Form();
    LegendScheme _legendScheme = null;
    LayersLegend m_LayersTV = new LayersLegend();
    boolean m_IfFrmMeteoData = false;
    boolean _ifCreateLegendScheme = false;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public LegendSchemeControl() {
        initComponents();
    }

    /**
     * Constructor
     *
     * @param ifFrmMeteoData If is from meteo data
     * @param mapLayer The map layer
     * @param aLayersTV The layersLegend
     */
    public LegendSchemeControl(boolean ifFrmMeteoData, MapLayer mapLayer, LayersLegend aLayersTV) {
        initComponents();

        m_IfFrmMeteoData = ifFrmMeteoData;
        _mapLayer = mapLayer;
        m_LayersTV = aLayersTV;
    }

    private void initComponents() {
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    // </editor-fold>
}
