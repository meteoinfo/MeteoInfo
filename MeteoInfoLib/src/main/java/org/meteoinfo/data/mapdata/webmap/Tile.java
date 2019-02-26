/*
 * Tile.java
 *
 * Created on March 14, 2006, 4:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.meteoinfo.data.mapdata.webmap;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;
import javax.swing.SwingUtilities;
import org.meteoinfo.beans.AbstractBean;

/**
 * The Tile class represents a particular square image
 * piece of the world bitmap at a particular zoom level.
 * @author joshy
 */

public class Tile extends AbstractBean {
    public enum Priority { High, Low }
    private Priority priority = Priority.High;
    
//    private static final Logger LOG = Logger.getLogger(Tile.class.getName());

    private boolean isLoading = false;
    static {
//        LOG.setLevel(Level.OFF);
    }
    
    
    //Most Recently Accessed Tiles. These are strong references, to prevent reloading
    //of most recently used tiles.
    //private static final Map<URI, BufferedImage> recentlyAccessed = new HashMap<URI, BufferedImage>();
    //private static final TileCache cache = new TileCache();
    
    /**
     * If an error occurs while loading a tile, store the exception
     * here.
     */
    private Throwable error;
    
    /**
     * The url of the image to load for this tile
     */
    private String url;
    
    /**
     * Indicates that loading has succeeded. A PropertyChangeEvent will be fired
     * when the loading is completed
     */
    
    private boolean loaded = false;
    /**
     * The zoom level this tile is for
     */
    private int zoom, x, y;
    
    /**
     * The image loaded for this Tile
     */
    SoftReference<BufferedImage> image = new SoftReference<>(null);
    
    /**
     * Create a new Tile at the specified tile point and zoom level
     * @param x X index
     * @param y Y index
     * @param zoom Zoom
     */
    public Tile(int x, int y, int zoom) {
        loaded = false;
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Create a new Tile that loads its data from the given URL. The URL must
     * resolve to an image
     */
    Tile(int x, int y, int zoom, String url, Priority priority, TileFactory dtf) {
        this.url = url;
        loaded = false;
        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.priority = priority;
        this.dtf = dtf;
        //startLoading();
    }
    
    /**
     *
     * Indicates if this tile's underlying image has been successfully loaded yet.
     * @return true if the Tile has been loaded
     */
    public synchronized boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Toggles the loaded state, and fires the appropriate property change notification
     */
    synchronized void setLoaded(boolean loaded) {
        boolean old = isLoaded();
        this.loaded = loaded;
        firePropertyChange("loaded", old, isLoaded());
    }
    
    /**
     * Returns the last error in a possible chain of errors that occured during
     * the loading of the tile
     * @return 
     */
    public Throwable getUnrecoverableError() {
        return error;
    }
    
    /**
     * Returns the Throwable tied to any error that may have ocurred while
     * loading the tile. This error may change several times if multiple
     * errors occur
     * @return Throwable
     */
    public Throwable getLoadingError() {
        return error;
    }
    
    /**
     * Returns the Image associated with this Tile. This is a read only property
     *          This may return null at any time, however if this returns null,
     *          a load operation will automatically be started for it.
     * @return BufferedImage
     */
    public BufferedImage getImage() {
        BufferedImage img = image.get();
        if (img == null) {
            setLoaded(false);
            dtf.startLoading(this);
        }
        
        return img;
    }
    
    /**
     * @return the location in the world at this zoom level that this tile should
     * be placed
     */
    /*
    public TilePoint getLocation() {
        return location;
    }*/
    
    /**
     * @return the zoom level that this tile belongs in
     */
    public int getZoom() {
        return zoom;
    }
    
    //////////////////JavaOne Hack///////////////////
    private PropertyChangeListener uniqueListener = null;
    /**
     * Adds a single property change listener. If a listener has been previously
     * added then it will be replaced by the new one.
     * @param propertyName
     * @param listener
     */
    public void addUniquePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (uniqueListener != null && uniqueListener != listener) {
            removePropertyChangeListener(propertyName, uniqueListener);
        }
        if (uniqueListener != listener) {
            uniqueListener = listener;
            addPropertyChangeListener(propertyName, uniqueListener);
        }
    }
    
    /////////////////End JavaOne Hack/////////////////
    
    /**
     */
    void firePropertyChangeOnEDT(final String propertyName, final Object oldValue, final Object newValue) {
        if (!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    firePropertyChange(propertyName, oldValue, newValue);
                }
            });
        }
    }
    
    private static void p(String string) {
        System.out.println(string);
    }
    
	/**
	 * @return the error
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(Throwable error) {
		this.error = error;
	}
	
	/**
	 * @return the isLoading
	 */
	public boolean isLoading() {
		return isLoading;
	}

	/**
	 * @param isLoading the isLoading to set
	 */
	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}
	
    /**
     * Gets the loading priority of this tile.
     * @return Priority
     */
    public Priority getPriority() {
        return priority;
    }
    
	/**
	 * Set the loading priority of this tile.
	 * @param priority the priority to set
	 */
    public void setPriority(Priority priority) {
		this.priority = priority;
	}
    
    /**
     * Gets the URL of this tile.
     * @return URL string
     */
    public String getURL() {
        return url;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    private TileFactory dtf;

}

