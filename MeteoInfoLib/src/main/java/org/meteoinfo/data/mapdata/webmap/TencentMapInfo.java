/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class TencentMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public TencentMapInfo() {
        super("TencentMap", 0, 16, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://rt1.map.gtimg.com/tile?z=%1$d&x=%2$d&y=%3$d&styleid=1&version=117",
                "x", "y", "z");
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
    
//    // </editor-fold>
//    // <editor-fold desc="Methods">
    
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        //int n = this.getMapWidthInTilesAtZoom(zoom);
        int n = (int)Math.pow(2, zoom);
        y = n - y - 1;
        String url = String.format(this.baseURL, zoom, x, y);
        return url;
    }
    // </editor-fold>
}
