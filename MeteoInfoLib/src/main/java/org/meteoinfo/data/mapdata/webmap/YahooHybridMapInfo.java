/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class YahooHybridMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    private String version = "2.1";
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public YahooHybridMapInfo() {
        super("YahooHybridMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://maps%1$d.yimg.com/hx/tl?v=%2$s&t=h&.intl=%3$s&x=%4$d&y=%5$d&z=%6$d&r=1",
                "x", "y", "z");
        this.baseURL = "http://%1$d.aerial.maps.api.here.com/maptile/%2$s/maptile/newest/hybrid.day/%3$d/%4$d/%5$d/256/jpg?lg=%6$s&token=%7$s&requestid=yahoo.prod&app_id=%8$s";
        this.setLanguage("ENG");
    }
//    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">
//    // </editor-fold>
//    // <editor-fold desc="Methods">

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        int serverNum = this.getServerNum(x, y, 2) + 1;
        //String url = String.format(this.baseURL, serverNum, version, this.getLanguage(), x, ((1 << zoom) >> 1) - 1 - y, zoom + 1);
        String rnd1 = "TrLJuXVK62IQk0vuXFzaig%3D%3D";
        String rnd2 = "eAdkWGYRoc4RfxVo0Z4B";
        String url = String.format(this.baseURL, serverNum, version, zoom, x, y, this.getLanguage(), rnd1, rnd2);
        return url;
    }
   
    // </editor-fold>
}
