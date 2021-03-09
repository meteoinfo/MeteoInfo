/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class OpenStreetMapQuestSatelliteInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public OpenStreetMapQuestSatelliteInfo() {
        super("OpenStreetMapQuestSatellite", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://otile%1$d.mqcdn.com/tiles/1.0.0/sat/%2$d/%3$d/%4$d.jpg",//5/15/10.png",
                "x", "y", "z");
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
//    // </editor-fold>
//    // <editor-fold desc="Methods">

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        int serverNum = this.getServerNum(x, y, 3) + 1;
        String url = String.format(this.baseURL, serverNum, zoom, x, y);
        return url;
    }
    // </editor-fold>
}
