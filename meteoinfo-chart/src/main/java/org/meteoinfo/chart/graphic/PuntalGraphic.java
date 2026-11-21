package org.meteoinfo.chart.graphic;

public interface PuntalGraphic {

    /**
     * Get is render point as sphere or not
     * @return Render point as sphere or not
     */
    public abstract boolean isSphere();

    /**
     * Set is render point as sphere or not
     * @param value Render point as sphere or not
     */
    public abstract void setSphere(boolean value);
}
