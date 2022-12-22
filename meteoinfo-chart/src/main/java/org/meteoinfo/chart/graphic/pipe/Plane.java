package org.meteoinfo.chart.graphic.pipe;

import org.joml.Vector3f;

/**
 * class for a 3D plane with normal vector (a,b,c) and a point (x0,y0,z0)
 * ax + by + cz + d = 0,  where d = -(ax0 + by0 + cz0)
 * ported from C++ code by Song Ho Ahn (song.ahn@gmail.com)
 */
public class Plane {
    private Vector3f normal;
    private float d;
    private float normalLength;
    private float distance;

    /**
     * Constructor
     */
    public Plane() {
        this.normal = new Vector3f(0, 0, 1);
        this.d = 0;
        this.normalLength = 1;
        this.distance = 0;
    }

    /**
     * Constructor
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public Plane(float a, float b, float c, float d) {
        normal.set(a, b, c);
        this.d = d;

        // compute distance
        normalLength = (float) Math.sqrt(a*a + b*b + c*c);
        distance = -d / normalLength;
    }

    /**
     * Constructor
     * @param normal Normal
     * @param point Point
     */
    public Plane(Vector3f normal, Vector3f point) {
        this.normal = normal;
        normalLength = normal.length();
        d = -normal.dot(point);         // -(a*x0 + b*y0 + c*z0)
        distance = -d / normalLength;
    }

    /**
     * Get normal
     * @return Normal
     */
    public Vector3f getNormal() {
        return this.normal;
    }

    /**
     * Get d
     * @return d
     */
    public float getD() {
        return this.d;
    }

    /**
     * compute the shortest distance from a given point P to the plane
     * Note: The distance is signed. If the distance is negative, the point is in
     * opposite side of the plane.
     *
     * D = (a * Px + b * Py + c * Pz + d) / sqrt(a*a + b*b + c*c)
     * reference: www.songho.ca/math/plane.html
     * @param point The point
     * @return Distance
     */
    public float getDistance(Vector3f point)
    {
        float dot = normal.dot(point);
        return (dot + d) / normalLength;
    }

    /**
     * normalize
     * divide each coefficient by the length of normal
     */
    public void normalize()
    {
        float lengthInv = 1.0f / normalLength;
        normal = normal.mul(lengthInv, new Vector3f());
        normalLength = 1.0f;
        d *= lengthInv;
        distance = -d;
    }

    /**
     * find the intersect point
     * // substitute a point on the line to the plane equation, then solve for alpha
     * // a point on a line: (x0 + x*t, y0 + y*t, z0 + z*t)
     * // plane: a*X + b*Y + c*Z + d = 0
     * //
     * // a*(x0 + x*t) + b*(y0 + y*t) + c*(z0 + z*t) + d = 0
     * // a*x0 + a*x*t + b*y0 + b*y*t + c*z0 + c*z*t + d = 0
     * // (a*x + b*x + c*x)*t = -(a*x0 + b*y0 + c*z0 + d)
     * //
     * // t = -(a*x0 + b*y0 + c*z0 + d) / (a*x + b*x + c*x)
     * @param line The line
     * @return Intersect point
     */
    public Vector3f intersect(Line line)
    {
        // from line = p + t * v
        Vector3f p = line.getPoint();        // (x0, y0, z0)
        Vector3f v = line.getDirection();    // (x,  y,  z)

        // dot products
        float dot1 = normal.dot(p);         // a*x0 + b*y0 + c*z0
        float dot2 = normal.dot(v);         // a*x + b*y + c*z

        // if denominator=0, no intersect
        if(dot2 == 0)
            return new Vector3f(Float.NaN, Float.NaN, Float.NaN);

        // find t = -(a*x0 + b*y0 + c*z0 + d) / (a*x + b*y + c*z)
        float t = -(dot1 + d) / dot2;
        if (Math.abs(t) > 2) {
            t = 1;
        }

        // find intersection point
        return p.add(v.mul(t, new Vector3f()), new Vector3f());
    }

    /**
     * find the intersection line of 2 planes
     * // P1: N1 dot p + d1 = 0 (a1*X + b1*Y + c1*Z + d1 = 0)
     * // P2: N2 dot p + d2 = 0 (a2*X + b2*Y + c2*Z + d2 = 0)
     * //
     * // L: p0 + a*V where
     * // V is the direction vector of intersection line = (a1,b1,c1) x (a2,b2,c2)
     * // p0 is a point, which is on the L and both P1 and P2 as well
     * //
     * // p0 can be found by solving a linear system of 3 planes
     * // P1: N1 dot p + d1 = 0     (given)
     * // P2: N2 dot p + d2 = 0     (given)
     * // P3: V dot p = 0           (chosen where d3=0)
     * //
     * // Use the formula for intersecting 3 planes to find p0;
     * // p0 = ((-d1*N2 + d2*N1) x V) / V dot V
     * @param rhs The plane
     * @return Intersect line
     */
    public Line intersect(Plane rhs)
    {
        // find direction vector of the intersection line
        Vector3f v = normal.cross(rhs.getNormal(), new Vector3f());

        // if |direction| = 0, 2 planes are parallel (no intersect)
        // return a line with NaN
        if(v.x == 0 && v.y == 0 && v.z == 0)
            return new Line(new Vector3f(Float.NaN, Float.NaN, Float.NaN), new Vector3f(Float.NaN, Float.NaN, Float.NaN));

        // find a point on the line, which is also on both planes
        // choose simple plane where d=0: ax + by + cz = 0
        float dot = v.dot(v);                       // V dot V
        Vector3f n1 = normal.mul(rhs.getD(), new Vector3f());           // d2 * N1
        Vector3f n2 = rhs.getNormal().mul(-d, new Vector3f());          //-d1 * N2
        Vector3f p = (n1.add(n2, new Vector3f())).cross(v).div(dot);       // (d2*N1-d1*N2) X V / V dot V

        return new Line(v, p);
    }

    /**
     * determine if it intersects with the line
     * @param line The line
     * @return Intersect or not
     */
    public boolean isIntersected(Line line)
    {
        // direction vector of line
        Vector3f v = line.getDirection();

        // dot product with normal of the plane
        float dot = normal.dot(v);  // a*Vx + b*Vy + c*Vz

        if(dot == 0)
            return false;
        else
            return true;
    }

    /**
     * determine if it intersects with the other plane
     * @param plane The plane
     * @return Intersect or not
     */
    public boolean isIntersected(Plane plane)
    {
        // check if 2 plane normals are same direction
        Vector3f cross = normal.cross(plane.getNormal(), new Vector3f());
        if(cross.x == 0 && cross.y == 0 && cross.z == 0)
            return false;
        else
            return true;
    }
}
