/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class GoogleMapInfo extends TileFactoryInfo {

    // <editor-fold desc="Variables">
    private String version = "1173";
    private String clientKey = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public GoogleMapInfo() {        
//        super("GoogleMap", 1, 17, 19,
//                256, true, true, // tile size is 256 and x/y orientation is normal
//                "http://ecn.t%1$d.tiles.virtualearth.net/tiles/r%2$s?g=%3$s&mkt=%4$s&lbl=l1&stl=h&shading=hill&n=z%5$s",
//                "x", "y", "z");
        super("GoogleMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://mt2.google.cn/vt/lyrs=m&hl=%1$s&gl=cn&x=%2$d&y=%3$d&z=%4$d&s=Galil",
                "x", "y", "z");        
        //String url = "http://mt1.google.com/vt/lyrs=m@129&hl=%1$s&x=%2$d&y=%3$d&z=%4$d&s=Galileo";
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
        //String url = String.format(this.baseURL, x, y, zoom, this.getLanguage());
        return url;
    }

//    @Override
//    public String getTileUrl(int x, int y, int zoom, String language) {
//        zoom = this.getTotalMapZoom() - zoom;
//        int serverNum = this.getServerNum(x, y, 4);
//        String key = this.tileXYToQuadKey(x, y, zoom);
//        String ckey = this.clientKey;
//        if (ckey == null)
//            ckey = "";
//        String url = String.format(this.baseURL, serverNum, key, version, language, zoom, x, y);
//        return url;
//    }
    /// <summary>
    /// Converts tile XY coordinates into a QuadKey at a specified level of detail.
    /// </summary>
    /// <param name="tileX">Tile X coordinate.</param>
    /// <param name="tileY">Tile Y coordinate.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>A string containing the QuadKey.</returns>
    private String tileXYToQuadKey(long tileX, long tileY, int levelOfDetail) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = levelOfDetail; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0) {
                digit++;
            }
            if ((tileY & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();
    }
    // </editor-fold>
}
