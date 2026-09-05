package org.meteoinfo.chart.graphic;

public class Artist {

    protected boolean antiAlias = false;
    protected boolean visible = true;
    protected boolean clipOn = true;

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

    /**
     * Returen clip on
     * @return Clip on
     */
    public boolean isClipOn() {
        return this.clipOn;
    }

    /**
     * Set clip on
     * @param clipOn Clip on
     */
    public void setClipOn(boolean clipOn) {
        this.clipOn = clipOn;
    }
}
