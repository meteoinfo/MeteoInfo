package org.meteoinfo.geometry.colors;

public enum ExtendType {
    NEITHER,
    BOTH,
    MIN,
    MAX;

    /**
     * Get is extend max or not
     * @return Is extend max or not
     */
    public boolean isExtendMax() {
        return this == BOTH || this == MAX;
    }

    /**
     * Get is extend min or not
     * @return Is extend min or not
     */
    public boolean isExtendMin() {
        return this == BOTH || this == MIN;
    }
}
