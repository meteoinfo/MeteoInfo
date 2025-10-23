package org.meteoinfo.data.meteodata;

import org.meteoinfo.data.dimarray.Dimension;

public class Coordinate extends Variable {

    /**
     * Get dimension
     * @return The dimension
     */
    public Dimension getDimension() {
        return this.dimensions.get(0);
    }

    /**
     * Set dimension
     * @param value The dimension
     */
    public void setDimension(Dimension value) {
        this.dimensions.clear();
        this.dimensions.add(value);
    }
}
