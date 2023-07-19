/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class GoogleCNMapInfo extends TileFactoryInfo {

    // <editor-fold desc="Variables">
    private String version = "1173";
    private String clientKey = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GoogleCNMapInfo() {
        super("GoogleCNMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "https://gac-geo.googlecnapps.cn/maps/vt?lyrs=m&x=%1$d&y=%2$d&z=%3$d",
                "x", "y", "z");
    }
    // </editor-fold>

    // <editor-fold desc="Methods">
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        String url = String.format(this.baseURL, x, y, zoom);

        return url;
    }
    // </editor-fold>
}
