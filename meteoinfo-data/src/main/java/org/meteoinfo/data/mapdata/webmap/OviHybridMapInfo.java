/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class OviHybridMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    protected final String[] urlServerLetters = new String[]{"b", "c", "d", "e"};
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public OviHybridMapInfo() {
        super("OviHybridMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://%1$s.maptile.maps.svc.ovi.com/maptiler/v2/maptile/newest/hybrid.day/%2$d/%3$d/%4$d/256/png8",//5/15/10.png",
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
        String url = String.format(this.baseURL, urlServerLetters[serverNum], zoom, x, y);
        return url;
    }
    // </editor-fold>
}
