package org.meteoinfo.geometry.geom;

public class Point extends Geometry {

    private static final long serialVersionUID = 4902022702746614570L;
    /**
     *  The <code>Coordinate</code> wrapped by this <code>Point</code>.
     */
    private Coordinate coordinate;

    /**
     * Constructor
     * @param coordinate Coordinate
     */
    public Point(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean isEmpty() {
        return this.coordinate == null;
    }

    public int getNumPoints() {
        return isEmpty() ? 0 : 1;
    }

    public double getX() {
        if (getCoordinate() == null) {
            throw new IllegalStateException("getX called on empty Point");
        }
        return getCoordinate().x;
    }

    public double getY() {
        if (getCoordinate() == null) {
            throw new IllegalStateException("getY called on empty Point");
        }
        return getCoordinate().y;
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    @Override
    public Coordinate[] getCoordinates() {
        return isEmpty() ? new Coordinate[]{} : new Coordinate[]{this.coordinate};
    }

    @Override
    public String getGeometryType() {
        return Geometry.TYPENAME_POINT;
    }

    @Override
    public int getDimension() {
        return 0;
    }

    public boolean equalsExact(Geometry other, double tolerance) {
        if (!isEquivalentClass(other)) {
            return false;
        }
        if (isEmpty() && other.isEmpty()) {
            return true;
        }
        if (isEmpty() != other.isEmpty()) {
            return false;
        }
        return equal(((Point) other).getCoordinate(), this.getCoordinate(), tolerance);
    }

    @Override
    protected int compareToSameClass(Object other) {
        Point point = (Point) other;
        return getCoordinate().compareTo(point.getCoordinate());
    }

    @Override
    protected int getTypeCode() {
        return Geometry.TYPECODE_POINT;
    }
}
