package org.meteoinfo.geometry;

import java.util.List;

public class LinearRing extends LineString {

    /**
     * The minimum number of vertices allowed in a valid non-empty ring.
     * Empty rings with 0 vertices are also valid.
     */
    public static final int MINIMUM_VALID_SIZE = 3;

    private static final long serialVersionUID = -4261142084085851829L;

    /**
     * Constructor
     * @param coordinates Coordinates
     */
    public LinearRing(List<Coordinate> coordinates) {
        super(coordinates);
        if (!isClosed()) {
            this.coordinates.add(this.coordinates.get(0));
        }
    }

    private void validateConstruction() {
        if (!isEmpty() && ! super.isClosed()) {
            throw new IllegalArgumentException("Points of LinearRing do not form a closed linestring");
        }
        if (coordinates.size() >= 1 && coordinates.size() < MINIMUM_VALID_SIZE) {
            throw new IllegalArgumentException("Invalid number of points in LinearRing (found "
                    + coordinates.size() + " - must be 0 or >= " + MINIMUM_VALID_SIZE + ")");
        }
    }
}
