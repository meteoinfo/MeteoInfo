package org.meteoinfo.geometry.graphic;

public class Artist {

    protected boolean antiAlias = false;
    protected boolean visible = true;

    /**
     * Return antiAlias
     * @return AntiAlias
     */
    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    /**
     * Set antiAlias
     * @param value Set antiAlias
     */
    public void setAntiAlias(boolean value) {
        this.antiAlias = value;
    }

    /**
     * Return visible
     * @return Visible
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Set visible
     * @param value Visible
     */
    public void setVisible(boolean value) {
        this.visible = value;
    }
}
