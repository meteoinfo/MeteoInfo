/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class BaiduSatelliteMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public BaiduSatelliteMapInfo() {
        super("BaiduSatelliteMap", 3, 18, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://shangetu1.map.bdimg.com/it/u=x=%1$s;y=%2$s;z=%3$s;v=009;type=sate&fm=46&udt=20130506",
                "x", "y", "z");
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
    
//    // </editor-fold>
//    // <editor-fold desc="Methods">
    
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        String url = String.format(this.baseURL, x, y, zoom);
        return url;
    }
    // </editor-fold>
}
