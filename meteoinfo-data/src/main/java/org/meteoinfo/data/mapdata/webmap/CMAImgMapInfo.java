package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class CMAImgMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public CMAImgMapInfo() {
        super("CMA_IMG_MAP", 0, 18, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://10.1.64.154/DataServer?T=img_w&X={x}&Y={y}&L={z}");
    }
    // </editor-fold>
//    // <editor-fold desc="Get Set Methods">

//    // </editor-fold>
//    // <editor-fold desc="Methods">

    /*@Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        String url = String.format(this.baseURL, x, y, zoom);
        return url;
    }*/
    // </editor-fold>
}
