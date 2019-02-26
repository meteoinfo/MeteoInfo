/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.script;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.WindowConstants;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.data.meteodata.DrawType2D;
import org.meteoinfo.layer.MapLayer;
import org.meteoinfo.layout.MapLayout;
import org.meteoinfo.legend.MapFrame;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.map.MapView;

/**
 *
 * @author yaqiang
 */
public class MeteoInfoMap {

    // <editor-fold desc="Variables">
    private boolean batchMode;
    private MapLayout mapLayout;
    private DrawType2D drawType2D;
    private String startUpPath;

    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     *
     * @param path Start up path
     */
    public MeteoInfoMap(String path) {
        this.startUpPath = path;
        this.batchMode = true;
        this.mapLayout = new MapLayout();
        drawType2D = DrawType2D.Contour;

        //Add default map layer
        //String fn = path + File.separator + "map" + File.separator + "country1.shp";
        String fn = "D:/Temp/map/country1.shp";
        if (new File(fn).exists()) {
            try {
                MapLayer layer = MapDataManage.loadLayer(fn);
                PolygonBreak pgb = (PolygonBreak) layer.getLegendScheme().getLegendBreaks().get(0);
                pgb.setDrawFill(false);
                MapFrame mapFrame = mapLayout.getActiveMapFrame();
                MapView mapView = mapFrame.getMapView();
                mapView.setLockViewUpdate(true);
                mapFrame.addLayer(layer);
                mapFrame.setGridXDelt(60);
                mapFrame.setGridYDelt(30);
                mapLayout.getActiveLayoutMap().zoomToExtentLonLatEx(mapView.getLayersWholeExtent());
                mapView.setLockViewUpdate(false);
            } catch (Exception ex) {
                Logger.getLogger(MeteoInfoMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">    
    // <editor-fold desc="Display">        
    /**
     * Dislay
     *
     * @param gdata GridData
     */
    public void display(GridData gdata) {
        MapLayer layer = DrawMeteoData.createContourLayer(gdata, "meteo_layer", "data");
        if (mapLayout == null) {
            mapLayout = new MapLayout();
        }

        MapFrame mapFrame = mapLayout.getActiveMapFrame();
        MapView mapView = mapFrame.getMapView();
        mapView.setLockViewUpdate(true);
        mapFrame.addLayer(layer);
        mapLayout.getActiveLayoutMap().zoomToExtentLonLatEx(mapView.getMeteoLayersExtent());
        mapView.setLockViewUpdate(false);
        mapLayout.paintGraphics();
    }

    // </editor-fold>    
    // <editor-fold desc="Form"> 

    /**
     * Create and show map figure form
     */
    public void show() {
        MapForm frame = new MapForm(this.mapLayout);
        frame.setSize(750, 540);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    // </editor-fold>
    
    // </editor-fold>
}
