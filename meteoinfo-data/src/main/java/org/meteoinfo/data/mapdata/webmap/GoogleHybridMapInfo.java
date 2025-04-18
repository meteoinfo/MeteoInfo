/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class GoogleHybridMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GoogleHybridMapInfo() {
        super("GoogleHybridMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://mt1.google.cn/vt/lyrs=y&hl=%1$s&gl=cn&x=%2$d&y=%3$d&z=%4$d&s=Galil");
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
    
//    // </editor-fold>
//    // <editor-fold desc="Methods">
    
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        String url = String.format(this.baseURL, this.getLanguage(), x, y, zoom);
        return url;
    }
    // </editor-fold>
}
