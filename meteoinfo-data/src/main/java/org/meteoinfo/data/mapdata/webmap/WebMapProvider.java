/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mapdata.webmap;

/**
 *
 * @author wyq
 */
public enum WebMapProvider {
    //SwingLabsBlueMarble,
    OpenStreetMap(new OpenStreetMapInfo()),
    OpenStreetMapQuestSatellite(new OpenStreetMapQuestSatelliteInfo()),
    BingMap(new BingMapInfo()),
    BingSatelliteMap(new BingSatelliteMapInfo()),
    BingHybridMap(new BingHybridMapInfo()),
    GoogleMap(new GoogleMapInfo()),
    GoogleSatelliteMap(new GoogleSatelliteMapInfo()),
    GoogleTerrainMap(new GoogleTerrainMapInfo()),
    GoogleHybridMap(new GoogleHybridMapInfo()),
    GoogleHybridTerrainMap(new GoogleHybridTerrainMapInfo()),
    GoogleCNMap(new GoogleCNMapInfo()),
    GoogleCNSatelliteMap(new GoogleCNSatelliteMapInfo()),
    GoogleCNTerrainMap(new GoogleCNTerrainMapInfo()),
    AMap(new AMapInfo()),
    ASatelliteMap(new ASatelliteMapInfo()),
    AHybridMap(new AHybridMapInfo()),
    TencentMap(new TencentMapInfo()),
    GeoQMap(new GeoQMapInfo()),
    GeoQGrayMap(new GeoQGrayMapInfo()),
    GeoQBlueMap(new GeoQBlueMapInfo()),
    GeoQWarmMap(new GeoQWarmMapInfo()),
    YahooMap(new YahooMapInfo()),
    YahooSatelliteMap(new YahooSatelliteMapInfo()),
    YahooHybridMap(new YahooHybridMapInfo()),
    CMA_CVA_MAP(new CMACvaMapInfo()),
    CMA_VEC_MAP(new CMAVecMapInfo()),
    CMA_IMG_MAP(new CMAImgMapInfo());

    private final TileFactoryInfo tileFactoryInfo;

    /**
     * Constructor
     * @param tileFactoryInfo Tile factory info
     */
    WebMapProvider(TileFactoryInfo tileFactoryInfo) {
        this.tileFactoryInfo = tileFactoryInfo;
    }

    /**
     * Get tile factory info
     * @return Tile factory info
     */
    public TileFactoryInfo getTileFactoryInfo() {
        return this.tileFactoryInfo;
    }
}
