package org.meteoinfo.geometry.graphic;

public class Artist {

    protected boolean antiAlias = false;

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
}
