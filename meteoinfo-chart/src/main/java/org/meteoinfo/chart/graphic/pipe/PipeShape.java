package org.meteoinfo.chart.graphic.pipe;

import org.meteoinfo.chart.jogl.Transform;
import org.meteoinfo.geometry.Coordinate;
import org.meteoinfo.geometry.shape.PolylineShape;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PipeShape extends PolylineShape {
    private float radius = 0.05f;
    private int steps = 48;
    private Pipe pipe;
    private Transform transform;

    /**
     * Constructor
     * @param shape PolylineZShape
     */
    public PipeShape(PolylineShape shape) {
        this.setPoints(shape.getPoints());
        //generatePipe();
    }

    /**
     * Constructor
     * @param shape PolylineZShape
     * @param radius Radius
     * @param steps Steps
     */
    public PipeShape(PolylineShape shape, float radius, int steps) {
        this.setPoints(shape.getPoints());
        this.radius = radius;
        this.steps = steps;
        generatePipe();
    }

    /**
     * Get pipe
     * @return Pipe
     */
    public Pipe getPipe() {
        return this.pipe;
    }

    /**
     * Get radius
     * @return Radius
     */
    public float getRadius() {
        return this.radius;
    }

    /**
     * Get steps
     * @return Steps
     */
    public int getSteps() {
        return this.steps;
    }

    public int getVertexCount() {
        return this.getPointNum() * (this.steps + 1);
    }

    void generatePipe() {
        List<Vector3f> path = new ArrayList<>();
        for (Coordinate p : (List<Coordinate>) this.getPoints()) {
            path.add(new Vector3f((float)p.x, (float)p.y, (float)p.z));
        }
        this.pipe = new Pipe(path, this.radius, this.steps);
    }

    /**
     * Transform
     * @param transform The transform
     */
    public void transform(Transform transform) {
        if (this.transform != null) {
            if (this.transform.equals(transform))
                return;
        }
        this.transform = (Transform) transform.clone();

        List<Vector3f> path = new ArrayList<>();
        for (Coordinate p : (List<Coordinate>) this.getPoints()) {
            path.add(new Vector3f(transform.transform_x((float)p.x), transform.transform_y((float)p.y),
                    transform.transform_z((float)p.z)));
        }
        this.pipe = new Pipe(path, this.radius, this.steps);
    }
}
