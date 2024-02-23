package org.meteoinfo.chart.graphic;

import org.meteoinfo.data.mapdata.webmap.*;
import org.meteoinfo.data.mapdata.webmap.empty.EmptyTileFactory;
import org.meteoinfo.geometry.graphic.Graphic;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class WebMapImage extends Graphic {

    private final boolean isNegativeYAllowed = true;
    /**
     * The zoom level. Generally a value between 1 and 15 (TODO Is this true for
     * all the mapping worlds? What does this mean if some mapping system
     * doesn't support the zoom level?
     */
    private int zoom = 11;
    /**
     * The position, in <I>map coordinates</I> of the center point. This is
     * defined as the distance from the top and left edges of the map in pixels.
     * Dragging the map component will change the center position. Zooming
     * in/out will cause the center to be recalculated to remain in the
     * center of the new "map".
     */
    private Point2D center = new Point2D.Double(0, 0);
    /**
     * Indicates whether or not to draw the borders between tiles. Defaults to
     * false.
     *
     * TODO Generally not very nice looking, very much a product of testing
     * Consider whether this should really be a property or not.
     */
    private boolean drawTileBorders = false;
    /**
     * Factory used by this component to grab the tiles necessary for painting
     * the map.
     */
    private TileFactory factory;
    /**
     * The position in latitude/longitude of the "address" being mapped. This is
     * a special coordinate that, when moved, will cause the map to be moved as
     * well. It is separate from "center" in that "center" tracks the current
     * center (in pixels) of the viewport whereas this will not change when
     * panning or zooming. Whenever the addressLocation is changed, however, the
     * map will be repositioned.
     */
    private GeoPosition addressLocation;
    private WebMapProvider defaultProvider = WebMapProvider.OpenStreetMap;
    private double webMapScale = 0.;
    private double width;
    private double height;

    /**
     * Constructor
     */
    public WebMapImage() {
        this.factory = new EmptyTileFactory();
    }

    public TileFactory getTileFactory() {
        return factory;
    }

    /**
     * Set the current tile factory
     *
     * @param factory the new property value
     */
    public void setTileFactory(TileFactory factory) {
        this.factory = factory;
        //this.setZoom(factory.getInfo().getDefaultZoomLevel());
        //this.setCenterPosition(new GeoPosition(0, 0));
    }

    /**
     * Get web map scale
     * @return Web map scale
     */
    public double getWebMapScale() {
        return this.webMapScale;
    }

    /**
     * Set web map scale
     * @param value Web map scale
     */
    public void setWebMapScale(double value) {
        this.webMapScale = value;
    }

    /**
     * Set web map provider
     * @param prov The web map provider
     */
    public void setWebMapProvider(WebMapProvider prov) {
        TileFactoryInfo info = prov.getTileFactoryInfo();
        if (info != null) {
            this.defaultProvider = prov;
            TileFactory tf = new DefaultTileFactory(info);
            setTileFactory(tf);
            setAddressLocation(new GeoPosition(51.5, 0));
        }
    }

    /**
     * Get web map provider
     * @return Web map provider
     */
    public WebMapProvider getWebMapProvider() {
        return this.defaultProvider;
    }

    /**
            * Gets the current zoom level
     *
             * @return the current zoom level
     */
    public int getZoom() {
        return this.zoom;
    }

    /**
     * Set the current zoom level
     *
     * @param zoom the new zoom level
     */
    public void setZoom(int zoom) {
        if (zoom == this.zoom) {
            return;
        }

        TileFactoryInfo info = getTileFactory().getInfo();
        // don't repaint if we are out of the valid zoom levels
        if (info != null
                && (zoom < info.getMinimumZoomLevel()
                || zoom > info.getMaximumZoomLevel())) {
            return;
        }

        //if(zoom >= 0 && zoom <= 15 && zoom != this.zoom) {
        int oldZoom = this.zoom;
        Point2D oldCenter = getCenter();
        Dimension oldMapSize = getTileFactory().getMapSize(oldZoom);
        this.zoom = zoom;
        //this.firePropertyChange("zoom", oldzoom, zoom);

        Dimension mapSize = getTileFactory().getMapSize(zoom);

        setCenter(new Point2D.Double(
                oldCenter.getX() * (mapSize.getWidth() / oldMapSize.getWidth()),
                oldCenter.getY() * (mapSize.getHeight() / oldMapSize.getHeight())));

        //repaint();
    }

    /**
     * Gets the current pixel center of the map. This point is in the global
     * bitmap coordinate system, not as lat/longs.
     *
     * @return the current center of the map as a pixel value
     */
    public Point2D getCenter() {
        return center;
    }

    /**
     * Sets the new center of the map in pixel coordinates.
     *
     * @param center the new center of the map in pixel coordinates
     */
    public void setCenter(Point2D center) {
        this.center = center;
    }

    public void setAddressLocation(GeoPosition pos) {
        this.addressLocation = pos;
        setCenter(getTileFactory().geoToPixel(addressLocation, getZoom()));
    }

    public void draw(Graphics2D g, Rectangle2D area, TileLoadListener tileLoadListener) {
        double width = area.getWidth();
        double height = area.getHeight();
        Rectangle2D viewportBounds = this.calculateViewportBounds(g, width, height);
        int size = this.factory.getTileSize(zoom);
        Dimension mapSize = this.getTileFactory().getMapSize(zoom);

        //calculate the "visible" viewport area in tiles
        int numWide = (int) (viewportBounds.getWidth() / size) + 2;
        int numHigh = (int) (viewportBounds.getHeight() / size) + 2;

        TileFactoryInfo info = this.factory.getInfo();
        int tpx = (int) Math.floor(viewportBounds.getX() / info.getTileSize(0));
        int tpy = (int) Math.floor(viewportBounds.getY() / info.getTileSize(0));

        //fetch the tiles from the factory and store them in the tiles cache
        //attach the TileLoadListener
        for (int x = 0; x <= numWide; x++) {
            for (int y = 0; y <= numHigh; y++) {
                int itpx = x + tpx;
                int itpy = y + tpy;
                Tile tile = this.getTileFactory().getTile(itpx, itpy, zoom);
                tile.addUniquePropertyChangeListener("loaded", tileLoadListener); //this is a filthy hack
                int ox = ((itpx * this.getTileFactory().getTileSize(zoom)) - (int) viewportBounds.getX());
                int oy = ((itpy * this.getTileFactory().getTileSize(zoom)) - (int) viewportBounds.getY());

                //if the tile is off the map to the north/south, then just don't paint anything
                if (this.isTileOnMap(itpx, itpy, mapSize) && tile.isLoaded()) {
                    g.drawImage(tile.getImage(), ox, oy, null);
                }
            }
        }
    }

    public boolean isTileOnMap(int x, int y, Dimension mapSize) {
        return isNegativeYAllowed || y < 0 || y < mapSize.getHeight();
    }

    /**
     * Calculate view port bounds
     *
     * @param g Graphic2D
     * @param width The width
     * @param height The height
     * @return View port bounds rectangle
     */
    public Rectangle2D calculateViewportBounds(Graphics2D g, double width, double height) {
        //calculate the "visible" viewport area in pixels
        //double sx = g.getTransform().getTranslateX();
        //double sy = g.getTransform().getTranslateY();
        double viewportX = (center.getX() - width / 2);
        double viewportY = (center.getY() - height / 2);
        return new Rectangle2D.Double(viewportX, viewportY, width, height);
    }
}
