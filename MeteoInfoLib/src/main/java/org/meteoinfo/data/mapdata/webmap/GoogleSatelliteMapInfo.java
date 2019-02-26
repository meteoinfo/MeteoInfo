/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class GoogleSatelliteMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GoogleSatelliteMapInfo() {
        super("GoogleSatelliteMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://mt3.google.cn/vt/lyrs=s&hl=%1$s&gl=cn&x=%2$d&y=%3$d&z=%4$d&s=Galil",
                "x", "y", "z");
        //String url = "http://mt1.google.com/vt/lyrs=y&hl=%s&x=%d&y=%d&z=%d&s=Ga";
        //String url = "http://mt3.google.com/vt/lyrs=s&hl=%1$s&gl=cn&x=%2$d&y=%3$d&z=%4$d&s=Galil";
        //this.baseURL = url;
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
    
//    // </editor-fold>
//    // <editor-fold desc="Methods">
    
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        String url = String.format(this.baseURL, this.getLanguage(), x, y, zoom);
        //String url = String.format(this.baseURL, x, y, zoom);
        return url;
    }
    // </editor-fold>
}
