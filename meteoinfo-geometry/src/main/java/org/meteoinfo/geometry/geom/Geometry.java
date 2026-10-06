package org.meteoinfo.geometry.geom;

import org.meteoinfo.geometry.Extent;

import java.io.Serializable;

public abstract class Geometry implements Cloneable, Comparable, Serializable {

    private static final long serialVersionUID = 8763622679187376702L;

    protected static final int TYPECODE_POINT = 0;
    protected static final int TYPECODE_MULTIPOINT = 1;
    protected static final int TYPECODE_LINESTRING = 2;
    protected static final int TYPECODE_LINEARRING = 3;
    protected static final int TYPECODE_MULTILINESTRING = 4;
    protected static final int TYPECODE_POLYGON = 5;
    protected static final int TYPECODE_MULTIPOLYGON = 6;
    protected static final int TYPECODE_GEOMETRYCOLLECTION = 7;

    public static final String TYPENAME_POINT = "Point";
    public static final String TYPENAME_MULTIPOINT = "MultiPoint";
    public static final String TYPENAME_LINESTRING = "LineString";
    public static final String TYPENAME_LINEARRING = "LinearRing";
    public static final String TYPENAME_MULTILINESTRING = "MultiLineString";
    public static final String TYPENAME_POLYGON = "Polygon";
    public static final String TYPENAME_MULTIPOLYGON = "MultiPolygon";
    public static final String TYPENAME_GEOMETRYCOLLECTION = "GeometryCollection";

    protected Extent extent = new Extent();

    /**
     * Returns the name of this Geometry's actual class.
     *
     *@return the name of this <code>Geometry</code>s actual class
     */
    public abstract String getGeometryType();

    /**
     * Returns the number of {@link Geometry}s in a {@link GeometryCollection}
     * (or 1, if the geometry is not a collection).
     *
     * @return the number of geometries contained in this geometry
     */
    public int getNumGeometries() {
        return 1;
    }

    /**
     * Returns an element {@link Geometry} from a {@link GeometryCollection}
     * (or <code>this</code>, if the geometry is not a collection).
     *
     * @param n the index of the geometry element
     * @return the n'th geometry contained in this geometry
     */
    public Geometry getGeometryN(int n) {
        return this;
    }

    /**
     *  Returns a vertex of this geometry
     *  (usually, but not necessarily, the first one),
     *  or <code>null</code> if the geometry is empty.
     *  The returned coordinate should not be assumed
     *  to be an actual <code>Coordinate</code> object used in
     *  the internal representation.
     *
     *@return a coordinate which is a vertex of this <code>Geometry</code>.
     *@return null if this Geometry is empty
     */
    public abstract Coordinate getCoordinate();

    /**
     *  Returns an array containing the values of all the vertices for
     *  this geometry.
     *  If the geometry is a composite, the array will contain all the vertices
     *  for the components, in the order in which the components occur in the geometry.
     *  <p>
     *  In general, the array cannot be assumed to be the actual internal
     *  storage for the vertices.  Thus modifying the array
     *  may not modify the geometry itself.
     *  Use the {@link CoordinateSequence#setOrdinate} method
     *  (possibly on the components) to modify the underlying data.
     *  If the coordinates are modified,
     *  {@link #geometryChanged} must be called afterwards.
     *
     *@return    the vertices of this <code>Geometry</code>
     *@see #geometryChanged
     *@see CoordinateSequence#setOrdinate
     */
    public abstract Coordinate[] getCoordinates();

    /**
     *  Returns the count of this <code>Geometry</code>s vertices. The <code>Geometry</code>
     *  s contained by composite <code>Geometry</code>s must be
     *  Geometry's; that is, they must implement <code>getNumPoints</code>
     *
     *@return    the number of vertices in this <code>Geometry</code>
     */
    public abstract int getNumPoints();

    /**
     * Returns the dimension of this geometry.
     * The dimension of a geometry is is the topological
     * dimension of its embedding in the 2-D Euclidean plane.
     * In the JTS spatial model, dimension values are in the set {0,1,2}.
     * <p>
     * Note that this is a different concept to the dimension of
     * the vertex {@link Coordinate}s.
     * The geometry dimension can never be greater than the coordinate dimension.
     * For example, a 0-dimensional geometry (e.g. a Point)
     * may have a coordinate dimension of 3 (X,Y,Z).
     *
     * @return the topological dimension of this geometry.
     *
     * @see #hasDimension(int)
     */
    public abstract int getDimension();

    /**
     * Tests whether an atomic geometry or any element of a collection
     * has the specified dimension.
     * In particular, this can be used with mixed-dimension {@link GeometryCollection}s
     * to test if they contain an element of the specified dimension.
     *
     * @param dim the dimension to test
     * @return true if the geometry has or contains an element with the dimension
     *
     * @see #getDimension()
     */
    public boolean hasDimension(int dim) {
        return dim == getDimension();
    }

    /**
     * Tests whether the set of points covered by this <code>Geometry</code> is
     * empty.
     * <p>
     * Note this test is for topological emptiness,
     * not structural emptiness.
     * A collection containing only empty elements is reported as empty.
     * To check structural emptiness use {@link #getNumGeometries()}.
     *
     *@return <code>true</code> if this <code>Geometry</code> does not cover any points
     */
    public abstract boolean isEmpty();

    /**
     *  Returns whether this <code>Geometry</code> is greater than, equal to,
     *  or less than another <code>Geometry</code> having the same class.
     *
     *@param  o  a <code>Geometry</code> having the same class as this <code>Geometry</code>
     *@return    a positive number, 0, or a negative number, depending on whether
     *      this object is greater than, equal to, or less than <code>o</code>, as
     *      defined in "Normal Form For Geometry" in the JTS Technical
     *      Specifications
     */
    protected abstract int compareToSameClass(Object o);

    /**
     *  Returns whether this <code>Geometry</code> is greater than, equal to,
     *  or less than another <code>Geometry</code>. <P>
     *
     *  If their classes are different, they are compared using the following
     *  ordering:
     *  <UL>
     *    <LI> Point (lowest)
     *    <LI> MultiPoint
     *    <LI> LineString
     *    <LI> LinearRing
     *    <LI> MultiLineString
     *    <LI> Polygon
     *    <LI> MultiPolygon
     *    <LI> GeometryCollection (highest)
     *  </UL>
     *  If the two <code>Geometry</code>s have the same class, their first
     *  elements are compared. If those are the same, the second elements are
     *  compared, etc.
     *
     *@param  o  a <code>Geometry</code> with which to compare this <code>Geometry</code>
     *@return    a positive number, 0, or a negative number, depending on whether
     *      this object is greater than, equal to, or less than <code>o</code>, as
     *      defined in "Normal Form For Geometry" in the JTS Technical
     *      Specifications
     */
    @Override
    public int compareTo(Object o) {
        Geometry other = (Geometry) o;
        if (getTypeCode() != other.getTypeCode()) {
            return getTypeCode() - other.getTypeCode();
        }
        if (isEmpty() && other.isEmpty()) {
            return 0;
        }
        if (isEmpty()) {
            return -1;
        }
        if (other.isEmpty()) {
            return 1;
        }
        return compareToSameClass(o);
    }

    /**
     *  Returns whether the two <code>Geometry</code>s are equal, from the point
     *  of view of the <code>equalsExact</code> method. Called by <code>equalsExact</code>
     *  . In general, two <code>Geometry</code> classes are considered to be
     *  "equivalent" only if they are the same class. An exception is <code>LineString</code>
     *  , which is considered to be equivalent to its subclasses.
     *
     *@param  other  the <code>Geometry</code> with which to compare this <code>Geometry</code>
     *      for equality
     *@return        <code>true</code> if the classes of the two <code>Geometry</code>
     *      s are considered to be equal by the <code>equalsExact</code> method.
     */
    protected boolean isEquivalentClass(Geometry other) {
        return this.getClass().getName().equals(other.getClass().getName());
    }

    protected boolean equal(Coordinate a, Coordinate b, double tolerance) {
        if (tolerance == 0) { return a.equals(b); }
        return a.distance(b) <= tolerance;
    }

    abstract protected int getTypeCode();
}
