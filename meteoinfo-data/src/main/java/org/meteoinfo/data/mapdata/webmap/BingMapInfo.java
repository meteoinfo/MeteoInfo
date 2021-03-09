/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author yaqiang
 */
public class BingMapInfo extends TileFactoryInfo {
    // <editor-fold desc="Variables">
    //private String version = "1173";
    private String version = "2660";
    private String clientKey = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public BingMapInfo() {
        super("BingMap", 1, 17, 19,
                256, true, true, // tile size is 256 and x/y orientation is normal
                "http://ecn.t%1$d.tiles.virtualearth.net/tiles/r%2$s?g=%3$s&mkt=%4$s&lbl=l1&stl=h&shading=hill&n=z%5$s",
                "x", "y", "z");
//        super("BingMap", 1, 17, 19,
//                256, true, true, // tile size is 256 and x/y orientation is normal
//                "http://t%1$d.tiles.ditu.live.com/tiles/r%2$s.png?g=%3$s&mkt=%4$s&n=z%5$s",
//                "x", "y", "z");
        if (this.getLanguage().equals("zh-cn"))
            this.baseURL = "http://t%1$d.tiles.ditu.live.com/tiles/r%2$s.png?g=%3$s&mkt=%4$s&n=z%5$s";
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    @Override
    public void setLanguage(String value){
        super.setLanguage(value);
        if (value.equals("zh-cn"))
            this.baseURL = "http://t%1$d.tiles.ditu.live.com/tiles/r%2$s.png?g=%3$s&mkt=%4$s&n=z%5$s";
        else
            this.baseURL = "http://ecn.t%1$d.tiles.virtualearth.net/tiles/r%2$s?g=%3$s&mkt=%4$s&lbl=l1&stl=h&shading=hill&n=z%5$s";
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        zoom = this.getTotalMapZoom() - zoom;
        int serverNum = this.getServerNum(x, y, 4);
        String key = this.tileXYToQuadKey(x, y, zoom);
        String ckey = this.clientKey;
        if (ckey == null)
            ckey = "";
        String url = String.format(this.baseURL, serverNum, key, version, this.getLanguage(), zoom);
        return url;
    }
    
    /// <summary>
      /// Converts tile XY coordinates into a QuadKey at a specified level of detail.
      /// </summary>
      /// <param name="tileX">Tile X coordinate.</param>
      /// <param name="tileY">Tile Y coordinate.</param>
      /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
      /// to 23 (highest detail).</param>
      /// <returns>A string containing the QuadKey.</returns>
      private String tileXYToQuadKey(long tileX, long tileY, int levelOfDetail)
      {
         StringBuilder quadKey = new StringBuilder();
         for(int i = levelOfDetail; i > 0; i--)
         {
            char digit = '0';
            int mask = 1 << (i - 1);
            if((tileX & mask) != 0)
            {
               digit++;
            }
            if((tileY & mask) != 0)
            {
               digit++;
               digit++;
            }
            quadKey.append(digit);
         }
         return quadKey.toString();
      }
    // </editor-fold>
}
