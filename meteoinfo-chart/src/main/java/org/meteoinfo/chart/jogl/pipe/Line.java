package org.meteoinfo.chart.jogl.pipe;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * class to construct a line with parametric form
 * Line = p + aV (a point and a direction vector on the line)
 * ported from C++ code by Song Ho Ahn (song.ahn@gmail.com)
 */
public class Line {
    private Vector3f direction;
    private Vector3f point;

    /**
     * Constructor
     */
    public Line() {
        this.direction = new Vector3f();
        this.point = new Vector3f();
    }

    /**
     * Constructor
     * @param slope Line slope
     * @param intercept Line intercept
     */
    public Line(float slope, float intercept) {
        set(slope, intercept);
    }

    /**
     * Constructor
     * @param direction Line direction
     * @param point Lint point
     */
    public Line(Vector3f direction, Vector3f point) {
        set(direction, point);
    }

    void set(Vector3f v, Vector3f p)
    {
        this.direction = v;
        this.point = p;
    }

    void set(Vector2f v, Vector2f p)
    {
        // convert 2D to 3D
        this.direction = new Vector3f(v.x, v.y, 0);
        this.point = new Vector3f(p.x, p.y, 0);
    }

    void set(float slope, float intercept)
    {
        // convert slope-intercept form (2D) to parametric form (3D)
        this.direction = new Vector3f(1, slope, 0);
        this.point = new Vector3f(0, intercept, 0);
    }

    /**
     * Get line direction
     * @return Line direction
     */
    public Vector3f getDirection() {
        return this.direction;
    }

    /**
     * Get line point
     * @return Line point
     */
    public Vector3f getPoint() {
        return this.point;
    }

    /**
     * find the intersection point with the other line.
     * If no intersection, return a point with NaN in it.
     * @param line Other line
     * @return Intersect point
     */
    public Vector3f intersect(Line line) {
        Vector3f v2 = line.getDirection();
        Vector3f p2 = line.getPoint();
        Vector3f result = new Vector3f(Float.NaN, Float.NaN, Float.NaN);    // default with NaN

        // find v3 = (p2 - p1) x V2
        Vector3f v3 = (p2.sub(point, new Vector3f())).cross(v2, new Vector3f());

        // find v4 = V1 x V2
        Vector3f v4 = direction.cross(v2, new Vector3f());

        // find (V1xV2) . (V1xV2)
        float dot = v4.dot(v4);

        // if both V1 and V2 are same direction, return NaN point
        if(dot == 0)
            return result;

        // find a = ((p2-p1)xV2).(V1xV2) / (V1xV2).(V1xV2)
        float alpha = v3.dot(v4) / dot;

        /*
        // if both V1 and V2 are same direction, return NaN point
        if(v4.x == 0 && v4.y == 0 && v4.z == 0)
            return result;

        float alpha = 0;
        if(v4.x != 0)
            alpha = v3.x / v4.x;
        else if(v4.y != 0)
            alpha = v3.y / v4.y;
        else if(v4.z != 0)
            alpha = v3.z / v4.z;
        else
            return result;
        */

        // find intersect point
        result = point.add(direction.mul(alpha, new Vector3f()));
        return result;
    }

    /**
     * determine if it intersects with the other line
     * @param line Other line
     * @return Intersect or not
     */
    public boolean isIntersected(Line line)
    {
        // if 2 lines are same direction, the magnitude of cross product is 0
        Vector3f v = this.direction.cross(line.getDirection(), new Vector3f());
        if(v.x == 0 && v.y == 0 && v.z == 0)
            return false;
        else
            return true;
    }
}
