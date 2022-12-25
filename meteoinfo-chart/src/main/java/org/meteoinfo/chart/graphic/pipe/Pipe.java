package org.meteoinfo.chart.graphic.pipe;

import org.joml.Vector3f;
import org.meteoinfo.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * base contour following a path
 * ported from C++ code by Song Ho Ahn (song.ahn@gmail.com)
 */
public class Pipe {
    private List<Vector3f> path;
    private List<Vector3f> contour;
    private List<List<Vector3f>> contours;
    private List<List<Vector3f>> normals;

    /**
     * Constructor
     */
    public Pipe() {

    }

    /**
     * Constructor
     * @param pathPoints Path points
     * @param contourPoints Contour points
     */
    public Pipe(List<Vector3f> pathPoints, List<Vector3f> contourPoints) {
        this.path = pathPoints;
        this.contour = contourPoints;
        generateContours();
    }

    /**
     * Constructor
     * @param pathPoint Path points
     * @param radius Contour radius
     * @param steps Contour steps
     */
    public Pipe(List<Vector3f> pathPoint, float radius, int steps) {
        this.path = pathPoint;
        this.contour = new ArrayList<>();
        float x, y, a;
        for (int i = 0; i < steps; i++) {
            a = (float) (Math.PI * 2 / steps * i);
            x = (float) (radius * Math.cos(a));
            y = (float) (radius * Math.sin(a));
            this.contour.add(new Vector3f(x, y, 0));
        }
        Vector3f v = contour.get(0);
        contour.add(new Vector3f(v.x, v.y, v.z));
        generateContours();
    }

    /**
     * Get path
     * @return Path
     */
    public List<Vector3f> getPath() {
        return this.path;
    }

    /**
     * Set path
     * @param pathPoints Path points
     */
    public void setPath(List<Vector3f> pathPoints) {
        this.path = pathPoints;
        generateContours();
    }

    /**
     * Get contour
     * @return Contour
     */
    public List<Vector3f> getContour() {
        return this.contour;
    }

    /**
     * Get contour
     * @param idx Index
     * @return Contour
     */
    public List<Vector3f> getContour(int idx) {
        return this.contours.get(idx);
    }

    /**
     * Set contour
     * @param contourPoints Contour points
     */
    public void setContour(List<Vector3f> contourPoints) {
        this.contour = contourPoints;
        generateContours();
    }

    /**
     * Get path point number
     * @return Path point number
     */
    public int getPathCount() {
        return this.path.size();
    }

    /**
     * Get contour number
     * @return Contour number
     */
    public int getContourCount() {
        return this.contours.size();
    }

    /**
     * Get vertex number
     * @return Vertex number
     */
    public int getVertexCount() {
        return this.path.size() * this.contour.size();
    }

    /**
     * Get normal
     * @param idx Index
     * @return Normal
     */
    public List<Vector3f> getNormal(int idx) {
        return this.normals.get(idx);
    }

    /**
     * Add a path point
     * @param point Path point
     */
    public void addPathPoint(Vector3f point)
    {
        // add it to path first
        path.add(point);

        int count = path.size();
        if(count == 1)
        {
            transformFirstContour();
            normals.add(computeContourNormal(0));
        }
        else if(count == 2)
        {
            contours.add(projectContour(0, 1));
            normals.add(computeContourNormal(1));
        }
        else
        {
            // add dummy to match same # of contours/normals and path
            List<Vector3f> dummy = new ArrayList<>();
            contours.add(dummy);
            normals.add(dummy);

            // re-project the previous contour
            contours.set(count-2, projectContour(count-3, count-2));
            normals.set(count-2, computeContourNormal(count-2));

            // compute for new contour
            contours.set(count-1, projectContour(count-2, count-1));
            normals.set(count-1, computeContourNormal(count-1));
        }
    }

    void generateContours()
    {
        // reset
        contours = new ArrayList<>();
        normals = new ArrayList<>();

        // path must have at least a point
        if(path.size() < 1)
            return;

        // rotate and translate the contour to the first path point
        transformFirstContour();
        contours.add(this.contour);
        normals.add(computeContourNormal(0));

        // project contour to the plane at the next path point
        int count = (int)path.size();
        for(int i = 1; i < count; ++i)
        {
            contours.add(projectContour(i-1, i));
            normals.add(computeContourNormal(i));
        }
    }

    /**
     * transform the contour at the first path point
     */
    void transformFirstContour()
    {
        int pathCount = (int)path.size();
        int vertexCount = (int)contour.size();
        Matrix4f matrix = new Matrix4f();

        if(pathCount > 0)
        {
            // transform matrix
            if(pathCount > 1)
                matrix.lookAt(path.get(1).sub(path.get(0), new Vector3f()));

            matrix.translate(path.get(0));

            // multiply matrix to the contour
            // NOTE: the contour vertices are transformed here
            //       MUST resubmit contour data if the path is reset to 0
            for(int i = 0; i < vertexCount; ++i)
            {
                contour.set(i, matrix.mul(contour.get(i)));
            }
        }
    }

    /**
     * project a contour to a plane at the path point
     * @param fromIndex From index
     * @param toIndex To index
     * @return Projected contour
     */
    List<Vector3f> projectContour(int fromIndex, int toIndex)
    {
        Vector3f dir1, dir2, normal;
        Line line = new Line();

        dir1 = path.get(toIndex).sub(path.get(fromIndex), new Vector3f());
        if(toIndex == (int)path.size()-1)
            dir2 = dir1;
        else
            dir2 = path.get(toIndex + 1).sub(path.get(toIndex), new Vector3f());

        normal = dir1.add(dir2, new Vector3f());               // normal vector of plane at toIndex
        Plane plane = new Plane(normal, path.get(toIndex));

        // project each vertex of contour to the plane
        List<Vector3f> fromContour = contours.get(fromIndex);
        List<Vector3f> toContour = new ArrayList<>();
        int count = (int)fromContour.size();
        for(int i = 0; i < count; ++i)
        {
            line.set(dir1, fromContour.get(i));
            toContour.add(plane.intersect(line));
        }

        return toContour;
    }

    /**
     * return normal vectors at the current path point
     * @param pathIndex Path index
     * @return Contour normal
     */
    List<Vector3f> computeContourNormal(int pathIndex)
    {
        // get current contour and center point
        List<Vector3f> contour = contours.get(pathIndex);
        Vector3f center = path.get(pathIndex);

        List<Vector3f> contourNormal = new ArrayList<>();
        Vector3f normal;
        for(int i = 0; i < (int)contour.size(); ++i)
        {
            normal = (contour.get(i).sub(center, new Vector3f())).normalize();
            contourNormal.add(normal);
        }

        return contourNormal;
    }
}
