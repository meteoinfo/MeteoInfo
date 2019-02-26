/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.layer;

//import com.sun.java.swing.Painter;
import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.meteoinfo.data.mapdata.webmap.AHybridMapInfo;
import org.meteoinfo.data.mapdata.webmap.AMapInfo;
import org.meteoinfo.data.mapdata.webmap.ASatelliteMapInfo;
import org.meteoinfo.data.mapdata.webmap.ArcGISImageInfo;
import org.meteoinfo.data.mapdata.webmap.BaiduMapInfo;
import org.meteoinfo.data.mapdata.webmap.BaiduSatelliteMapInfo;
import org.meteoinfo.data.mapdata.webmap.BingHybridMapInfo;
import org.meteoinfo.data.mapdata.webmap.BingMapInfo;
import org.meteoinfo.data.mapdata.webmap.BingSatelliteMapInfo;
import org.meteoinfo.data.mapdata.webmap.WebMapProvider;
import org.meteoinfo.data.mapdata.webmap.DefaultTileFactory;
import org.meteoinfo.data.mapdata.webmap.GeoPosition;
import org.meteoinfo.data.mapdata.webmap.GoogleHybridMapInfo;
import org.meteoinfo.data.mapdata.webmap.GoogleHybridTerrainMapInfo;
import org.meteoinfo.data.mapdata.webmap.GoogleMapInfo;
import org.meteoinfo.data.mapdata.webmap.GoogleSatelliteMapInfo;
import org.meteoinfo.data.mapdata.webmap.GoogleTerrainMapInfo;
import org.meteoinfo.data.mapdata.webmap.OpenStreetMapInfo;
import org.meteoinfo.data.mapdata.webmap.OpenStreetMapQuestSatelliteInfo;
import org.meteoinfo.data.mapdata.webmap.OviHybridMapInfo;
import org.meteoinfo.data.mapdata.webmap.OviMapInfo;
import org.meteoinfo.data.mapdata.webmap.OviSatelliteMapInfo;
import org.meteoinfo.data.mapdata.webmap.OviTerrainMapInfo;
import org.meteoinfo.data.mapdata.webmap.TencentMapInfo;
import org.meteoinfo.data.mapdata.webmap.Tile;
import org.meteoinfo.data.mapdata.webmap.TileFactory;
import org.meteoinfo.data.mapdata.webmap.TileFactoryInfo;
import org.meteoinfo.data.mapdata.webmap.YahooHybridMapInfo;
import org.meteoinfo.data.mapdata.webmap.YahooMapInfo;
import org.meteoinfo.data.mapdata.webmap.YahooSatelliteMapInfo;
import org.meteoinfo.data.mapdata.webmap.empty.EmptyTileFactory;
import org.meteoinfo.global.Extent;
import org.meteoinfo.shape.ShapeTypes;

/**
 *
 * @author wyq
 */
public class WebMapLayer extends MapLayer {
    // <editor-fold desc="Variables">

    private final boolean isNegativeYAllowed = true; //maybe rename to isNorthBounded and isSouthBounded?
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
     * in/out will cause the center to be recalculated so as to remain in the
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
    /**
     * Specifies whether panning is enabled. Panning is being able to click and
     * drag the map around to cause it to move
     */
    private boolean panEnabled = true;
    /**
     * Specifies whether zooming is enabled (the mouse wheel, for example,
     * zooms)
     */
    private boolean zoomEnabled = true;
    /**
     * Indicates whether the component should recenter the map when the "middle"
     * mouse button is pressed
     */
    private boolean recenterOnClickEnabled = true;
    /**
     * The overlay to delegate to for painting the "foreground" of the map
     * component. This would include painting waypoints, day/night, etc. Also
     * receives mouse events.
     */
    //private Painter overlay;
    private boolean designTime;
    private float zoomScale = 1;
    private Image loadingImage;
    private boolean restrictOutsidePanning = false;
    private boolean horizontalWrapped = true;
    private WebMapProvider defaultProvider = WebMapProvider.OpenStreetMap;
    private Graphics2D graphics;
    private int width;
    private int height;
    private List<Double> scales = new ArrayList<>();
    // </editor-fold>
    // <editor-fold desc="Event">
    // a property change listener which forces repaints when tiles finish loading
    private TileLoadListener tileLoadListener = new TileLoadListener();

    private final class TileLoadListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("loaded".equals(evt.getPropertyName())
                    && Boolean.TRUE.equals(evt.getNewValue())) {
                Tile t = (Tile) evt.getSource();
                if (t.getZoom() == getZoom()) {
                    repaint();
                    /* this optimization doesn't save much and it doesn't work if you
                     * wrap around the world
                     Rectangle viewportBounds = getViewportBounds();
                     TilePoint tilePoint = t.getLocation();
                     Point point = new Point(tilePoint.getX() * getTileFactory().getTileSize(), tilePoint.getY() * getTileFactory().getTileSize());
                     Rectangle tileRect = new Rectangle(point, new Dimension(getTileFactory().getTileSize(), getTileFactory().getTileSize()));
                     if (viewportBounds.intersects(tileRect)) {
                     //convert tileRect from world space to viewport space
                     repaint(new Rectangle(
                     tileRect.x - viewportBounds.x,
                     tileRect.y - viewportBounds.y,
                     tileRect.width,
                     tileRect.height
                     ));
                     }*/
                }
            }
        }
    }
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public WebMapLayer() {
        super();
        this.setLayerType(LayerTypes.WebMapLayer);
        this.setShapeType(ShapeTypes.Image);
        this.setLayerDrawType(LayerDrawType.Image);
        this.setLayerName("OpenStreetMap");
        this.setExtent(new Extent(-2.0037508342789244E7, 2.0037508342789244E7, -1.8375854901481014E7, 1.8375854901481014E7));
        factory = new EmptyTileFactory();

        // make a dummy loading image
        try {
            URL url = this.getClass().getResource("/images/loading.png");
            this.setLoadingImage(ImageIO.read(url));
        } catch (Throwable ex) {
            System.out.println("could not load 'loading.png'");
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Color.black);
            g2.fillRect(0, 0, 16, 16);
            g2.dispose();
            this.setLoadingImage(img);
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

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
        int oldzoom = this.zoom;
        Point2D oldCenter = getCenter();
        Dimension oldMapSize = getTileFactory().getMapSize(oldzoom);
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

    public boolean isRestrictOutsidePanning() {
        return restrictOutsidePanning;
    }

    public void setRestrictOutsidePanning(boolean restrictOutsidePanning) {
        this.restrictOutsidePanning = restrictOutsidePanning;
    }

    /**
     * Sets the new center of the map in pixel coordinates.
     *
     * @param center the new center of the map in pixel coordinates
     */
    public void setCenter(Point2D center) {
        this.center = center;
//        if(isRestrictOutsidePanning()) {
//            Insets insets = getInsets();
//            int viewportHeight = getHeight() - insets.top - insets.bottom;
//            int viewportWidth = getWidth() - insets.left - insets.right;
//            
//            // don't let the user pan over the top edge
//            Rectangle newVP = calculateViewportBounds(center);
//            if(newVP.getY() < 0) {
//                double centerY = viewportHeight/2;
//                center = new Point2D.Double(center.getX(),centerY);
//            }
//            
//            // don't let the user pan over the left edge
//            if(!isHorizontalWrapped() && newVP.getX() <0) {
//                double centerX = viewportWidth/2;
//                center = new Point2D.Double(centerX, center.getY());
//            }
//            
//            // don't let the user pan over the bottom edge
//            Dimension mapSize = getTileFactory().getMapSize(getZoom());
//            int mapHeight = (int)mapSize.getHeight()*getTileFactory().getTileSize(getZoom());
//            if(newVP.getY() + newVP.getHeight() > mapHeight) {
//                double centerY = mapHeight - viewportHeight/2;
//                center = new Point2D.Double(center.getX(),centerY);
//            }
//            
//            //don't let the user pan over the right edge
//            int mapWidth = (int)mapSize.getWidth()*getTileFactory().getTileSize(getZoom());
//            if(!isHorizontalWrapped() && (newVP.getX() + newVP.getWidth() > mapWidth)) {
//                double centerX = mapWidth - viewportWidth/2;
//                center = new Point2D.Double(centerX, center.getY());
//            }
//            
//            // if map is to small then just center it vert
//            if(mapHeight < newVP.getHeight()) {
//                double centerY = mapHeight/2;//viewportHeight/2;// - mapHeight/2;
//                center = new Point2D.Double(center.getX(),centerY);
//            }
//            
//            // if map is too small then just center it horiz
//            if(!isHorizontalWrapped() && mapWidth < newVP.getWidth()) {
//                double centerX = mapWidth/2;
//                center = new Point2D.Double(centerX, center.getY());
//            }
//        }
//        
//        //joshy: this is an evil hack to force a property change event
//        //i don't know why it doesn't work normally
//        old = new Point(5,6);
//        
//        GeoPosition oldGP = this.getCenterPosition();
    }

    /**
     * A property indicating the center position of the map
     *
     * @param geoPosition the new property value
     */
    public void setCenterPosition(GeoPosition geoPosition) {
        GeoPosition oldVal = getCenterPosition();
        setCenter(getTileFactory().geoToPixel(geoPosition, zoom));
        //repaint();
        GeoPosition newVal = getCenterPosition();
        //firePropertyChange("centerPosition", oldVal, newVal);
    }

    /**
     * A property indicating the center position of the map
     *
     * @return the current center position
     */
    public GeoPosition getCenterPosition() {
        return getTileFactory().pixelToGeo(getCenter(), zoom);
    }

    /**
     * Get the current factory
     *
     * @return the current property value
     */
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
        this.setZoom(factory.getInfo().getDefaultZoomLevel());
        this.setCenterPosition(new GeoPosition(0, 0));
    }

    /**
     * A property for an image which will be display when an image is still
     * loading.
     *
     * @return the current property value
     */
    public Image getLoadingImage() {
        return loadingImage;
    }

    /**
     * A property for an image which will be display when an image is still
     * loading.
     *
     * @param loadingImage the new property value
     */
    public void setLoadingImage(Image loadingImage) {
        this.loadingImage = loadingImage;
    }

    /**
     * Indicates if the tile borders should be drawn. Mainly used for debugging.
     *
     * @return the value of this property
     */
    public boolean isDrawTileBorders() {
        return drawTileBorders;
    }

    /**
     * Set if the tile borders should be drawn. Mainly used for debugging.
     *
     * @param drawTileBorders new value of this drawTileBorders
     */
    public void setDrawTileBorders(boolean drawTileBorders) {
        //boolean old = isDrawTileBorders();
        this.drawTileBorders = drawTileBorders;
        //firePropertyChange("drawTileBorders", old, isDrawTileBorders());
        //repaint();
    }

    /**
     * Set web map provider
     * @param prov The web map provider
     */
    public void setWebMapProvider(WebMapProvider prov) {        
        TileFactoryInfo info = null;
        switch (prov) {
//            case SwingLabsBlueMarble:
//                setTileFactory(new CylindricalProjectionTileFactory());
//                setZoom(3);
//                return;
            case OpenStreetMap:
                info = new OpenStreetMapInfo();
                break;
            case OpenStreetMapQuestSattelite:
                info = new OpenStreetMapQuestSatelliteInfo();
                break;
            case BingMap:
                info = new BingMapInfo();
                break;
            case BingSatelliteMap:
                info = new BingSatelliteMapInfo();
                break;
            case BingHybridMap:
                info = new BingHybridMapInfo();
                break;
//            case OviMap:
//                info = new OviMapInfo();
//                break;
//            case OviSatelliteMap:
//                info = new OviSatelliteMapInfo();
//                break;
//            case OviTerrainMap:
//                info = new OviTerrainMapInfo();
//                break;
//            case OviHybridMap:
//                info = new OviHybridMapInfo();
//                break;
            case YahooMap:
                info = new YahooMapInfo();
                break;
            case YahooSatelliteMap:
                info = new YahooSatelliteMapInfo();
                break;
            case YahooHybridMap:
                info = new YahooHybridMapInfo();
                break;
            case GoogleMap:
                info = new GoogleMapInfo();
                break;
            case GoogleSatelliteMap:
                info = new GoogleSatelliteMapInfo();
                break;
            case GoogleTerrainMap:
                info = new GoogleTerrainMapInfo();
                break;
            case GoogleHybridMap:
                info = new GoogleHybridMapInfo();
                break;
            case GoogleHybridTerrainMap:
                info = new GoogleHybridTerrainMapInfo();
                break;
//            case BaiduMap:
//                info = new BaiduMapInfo();
//                break;
//            case BaiduSatelliteMap:
//                info = new BaiduSatelliteMapInfo();
//                break;
            case AMap:
                info = new AMapInfo();
                break;
            case ASatelliteMap:
                info = new ASatelliteMapInfo();
                break;
            case AHybridMap:
                info = new AHybridMapInfo();
                break;
            case TencentMap:
                info = new TencentMapInfo();
                break;
//            case ArcGISImage:
//                info = new ArcGISImageInfo();
//                break;                
        }

        if (info != null) {
            this.defaultProvider = prov;
            this.setLayerName("WebMap_" + info.getName());
            TileFactory tf = new DefaultTileFactory(info);
            setTileFactory(tf);
            //setZoom(11);            
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

    public GeoPosition getAddressLocation() {
        return this.addressLocation;
    }

    public void setAddressLocation(GeoPosition pos, int zoom) {
        this.addressLocation = pos;
        setCenter(getTileFactory().geoToPixel(pos, zoom));
    }

    public void setAddressLocation(GeoPosition pos) {
        this.addressLocation = pos;
        setCenter(getTileFactory().geoToPixel(addressLocation, getZoom()));
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Repaint
     */
    public void repaint() {
        Rectangle rect = this.calculateViewportBounds(graphics, width, height);
        this.drawMapTiles(graphics, zoom, rect);
    }

    /**
     * Draw the map tiles
     *
     * @param g Graphics2D
     * @param zoom Zoom level to draw at
     * @param width The width
     * @param height The height
     */
    public void drawMapTiles(final Graphics2D g, final int zoom, int width, int height) {
        this.graphics = g;
        this.zoom = zoom;
        this.width = width;
        this.height = height;
        Rectangle rect = this.calculateViewportBounds(g, width, height);
        this.drawMapTiles(g, zoom, rect);
    }

    /**
     * Draw the map tiles. This method is for implementation use only.
     *
     * @param g Graphics
     * @param zoom zoom level to draw at
     * @param viewportBounds View bounds
     */
    public void drawMapTiles(final Graphics2D g, final int zoom, final Rectangle viewportBounds) {
        int size = getTileFactory().getTileSize(zoom);
        Dimension mapSize = getTileFactory().getMapSize(zoom);

        //calculate the "visible" viewport area in tiles
        int numWide = viewportBounds.width / size + 2;
        int numHigh = viewportBounds.height / size + 2;

        //TilePoint topLeftTile = getTileFactory().getTileCoordinate(
        //        new Point2D.Double(viewportBounds.x, viewportBounds.y));
        TileFactoryInfo info = getTileFactory().getInfo();
        int tpx = (int) Math.floor(viewportBounds.getX() / info.getTileSize(0));
        int tpy = (int) Math.floor(viewportBounds.getY() / info.getTileSize(0));
        //TilePoint topLeftTile = new TilePoint(tpx, tpy);

        //p("top tile = " + topLeftTile);
        //fetch the tiles from the factory and store them in the tiles cache
        //attach the tileLoadListener
        for (int x = 0; x <= numWide; x++) {
            for (int y = 0; y <= numHigh; y++) {
                int itpx = x + tpx;//topLeftTile.getX();
                int itpy = y + tpy;//topLeftTile.getY();
                //TilePoint point = new TilePoint(x + topLeftTile.getX(), y + topLeftTile.getY());
                //only proceed if the specified tile point lies within the area being painted
                //if (g.getClipBounds().intersects(new Rectangle(itpx * size - viewportBounds.x,
                //itpy * size - viewportBounds.y, size, size))) {
                Tile tile = getTileFactory().getTile(itpx, itpy, zoom);
                //tile.addUniquePropertyChangeListener("loaded", tileLoadListener); //this is a filthy hack
                int ox = ((itpx * getTileFactory().getTileSize(zoom)) - viewportBounds.x);
                int oy = ((itpy * getTileFactory().getTileSize(zoom)) - viewportBounds.y);

                //if the tile is off the map to the north/south, then just don't paint anything                    
                if (isTileOnMap(itpx, itpy, mapSize)) {
//                        if (isOpaque()) {
//                            g.setColor(getBackground());
//                            g.fillRect(ox,oy,size,size);
//                        }
                } else if (tile.isLoaded()) {
                    g.drawImage(tile.getImage(), ox, oy, null);
                } 
//                  else {
//                    int imageX = (getTileFactory().getTileSize(zoom) - getLoadingImage().getWidth(null)) / 2;
//                    int imageY = (getTileFactory().getTileSize(zoom) - getLoadingImage().getHeight(null)) / 2;
//                    g.setColor(Color.GRAY);
//                    g.fillRect(ox, oy, size, size);
//                    g.drawImage(getLoadingImage(), ox + imageX, oy + imageY, null);
//                }
                if (isDrawTileBorders()) {

                    g.setColor(Color.black);
                    g.drawRect(ox, oy, size, size);
                    g.drawRect(ox + size / 2 - 5, oy + size / 2 - 5, 10, 10);
                    g.setColor(Color.white);
                    g.drawRect(ox + 1, oy + 1, size, size);

                    String text = itpx + ", " + itpy + ", " + getZoom();
                    g.setColor(Color.BLACK);
                    g.drawString(text, ox + 10, oy + 30);
                    g.drawString(text, ox + 10 + 2, oy + 30 + 2);
                    g.setColor(Color.WHITE);
                    g.drawString(text, ox + 10 + 1, oy + 30 + 1);
                }
                //}
            }
        }
    }

    public boolean isTileOnMap(int x, int y, Dimension mapSize) {
        return !isNegativeYAllowed && y < 0 || y >= mapSize.getHeight();
    }

    /**
     * Calculate view port bounds
     *
     * @param g Graphic2D
     * @param width The width
     * @param height The height
     * @return View port bounds rectangle
     */
    public Rectangle calculateViewportBounds(Graphics2D g, int width, int height) {
        //calculate the "visible" viewport area in pixels
        //double sx = g.getTransform().getTranslateX();
        //double sy = g.getTransform().getTranslateY();
        double viewportX = (center.getX() - width / 2);
        double viewportY = (center.getY() - height / 2);
        return new Rectangle((int) viewportX, (int) viewportY, width, height);
    }
    
    /**
     * To string
     * @return String
     */
    @Override
    public String getLayerInfo(){
        String str = "Layer name: " + this.getLayerName();
        str += System.getProperty("line.separator") + "Layer file: " + this.getFileName();
        str += System.getProperty("line.separator") + "Layer type: " + this.getLayerType();
        str += System.getProperty("line.separator") + "Data provider: " + this.defaultProvider;        
        
        return str;
    }
    // </editor-fold>
    // <editor-fold desc="BeanInfo">
    public class WebMapLayerBean {
        public WebMapLayerBean(){
            
        }
        
        // <editor-fold desc="Get Set Methods">
        /**
         * Get layer type
         *
         * @return Layer type
         */
        public LayerTypes getLayerType() {
            return WebMapLayer.this.getLayerType();
        }

        /**
         * Get layer draw type
         *
         * @return Layer draw type
         */
        public LayerDrawType getLayerDrawType() {
            return WebMapLayer.this.getLayerDrawType();
        }

        /**
         * Get layer handle
         *
         * @return Layer handle
         */
        public int getHandle() {
            return WebMapLayer.this.getHandle();
        }

        /**
         * Get layer name
         *
         * @return Layer name
         */
        public String getLayerName() {
            return WebMapLayer.this.getLayerName();
        }

        /**
         * Set layer name
         *
         * @param name Layer name
         */
        public void setLayerName(String name) {
            WebMapLayer.this.setLayerName(name);
        }
        
        /**
         * Get web map provider
         * @return The web map provider
         */
        public String getWebMapProvider(){
            return WebMapLayer.this.defaultProvider.toString();
        }
        
        /**
         * Set web map provider
         * @param provider The web map provider
         */
        public void setWebMapProvider(String provider){
            WebMapLayer.this.setWebMapProvider(WebMapProvider.valueOf(provider));
        }

        /**
         * Get if is maskout
         *
         * @return If is maskout
         */
        public boolean isMaskout() {
            return WebMapLayer.this.isMaskout();
        }

        /**
         * Set if maskout
         *
         * @param value If maskout
         */
        public void setMaskout(boolean value) {
            WebMapLayer.this.setMaskout(value);
        }

        /**
         * Get if is visible
         *
         * @return If is visible
         */
        public boolean isVisible() {
            return WebMapLayer.this.isVisible();
        }

        /**
         * Set if is visible
         *
         * @param value If is visible
         */
        public void setVisible(boolean value) {
            WebMapLayer.this.setVisible(value);
        }
        
        /**
         * Get language
         * @return The language
         */
        public String getLanguage(){
            return WebMapLayer.this.getTileFactory().getInfo().getLanguage();
        }
        
        /**
         * Set language
         * @param value The language
         */
        public void setLanguage(String value) {
            WebMapLayer.this.getTileFactory().getInfo().setLanguage(value);
        }
                
        // </editor-fold>
    }
    
    public static class WebMapLayerBeanBeanInfo extends BaseBeanInfo {

        public WebMapLayerBeanBeanInfo() {
            super(WebMapLayer.WebMapLayerBean.class);
            addProperty("layerType").setCategory("Read only").setReadOnly().setDisplayName("Layer type");
            addProperty("layerDrawType").setCategory("Read only").setReadOnly().setDisplayName("Layer draw type");
            addProperty("handle").setCategory("Read only").setReadOnly().setDisplayName("Handle");
            ExtendedPropertyDescriptor e = addProperty("webMapProvider");
            e.setCategory("Editable").setDisplayName("Web Map Provider");
            e.setPropertyEditorClass(WebMapProviderEditor.class);
            e = addProperty("language");
            e.setCategory("Editable").setDisplayName("Language");
            e.setPropertyEditorClass(LanguageEditor.class);
            addProperty("visible").setCategory("Editable").setDisplayName("Visible");
            addProperty("maskout").setCategory("Editable").setDisplayName("Is maskout");
        }
    }
    
    public static class WebMapProviderEditor extends ComboBoxPropertyEditor {

        public WebMapProviderEditor() {
            super();
            WebMapProvider[] providers = WebMapProvider.values();
            String[] types = new String[providers.length];
            int i = 0;
            for (WebMapProvider prov : providers) {
                types[i] = prov.toString();
                i += 1;
            }
            setAvailableValues(types);
        }
    }
    
    public static class LanguageEditor extends ComboBoxPropertyEditor {

        public LanguageEditor() {
            super();
            String[] langs = new String[]{"en-us", "zh-cn"};
            setAvailableValues(langs);
        }
    }
    // </editor-fold>
}
