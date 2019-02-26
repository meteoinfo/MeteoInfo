/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class OpenStreetMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public OpenStreetMapInfo() {
        super("OpenStreetMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://tile.openstreetmap.org/%1$d/%2$d/%3$d.png",//5/15/10.png",
                "x", "y", "z");
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
//    // </editor-fold>
//    // <editor-fold desc="Methods">

//    @Override
//    public String getTileUrl(int x, int y, int zoom) {
//        zoom = this.getTotalMapZoom() - zoom;
//        String url = String.format(this.baseURL, zoom, x, y);
//        return url;
//    }
    // </editor-fold>
}
