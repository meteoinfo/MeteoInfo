package org.meteoinfo.chart.jogl;

import org.meteoinfo.global.Extent3D;
import org.meteoinfo.shape.PointZ;

public class Transform {
    private float xmin, xmax = 1.0f, ymin;
    private float ymax = 1.0f, zmin, zmax = 1.0f;

    /**
     * Constructor
     */
    public Transform() {

    }

    /**
     * Constructor
     * @param xmin Minimum x
     * @param xmax Maximum x
     * @param ymin Minimum y
     * @param ymax Maximum y
     * @param zmin Minimum z
     * @param zmax Maximum z
     */
    public Transform(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.zmin = zmin;
        this.zmax = zmax;
    }

    /**
     * Constructor
     * @param extent3D Extent 3D
     */
    public Transform(Extent3D extent3D) {
        this.setExtent(extent3D);
    }

    public void setExtent(Extent3D extent3D) {
        this.xmin = (float) extent3D.minX;
        this.xmax = (float) extent3D.maxX;
        this.ymin = (float) extent3D.minY;
        this.ymax = (float) extent3D.maxY;
        this.zmin = (float) extent3D.minZ;
        this.zmax = (float) extent3D.maxZ;
    }

    public float transform_x(float v) {
        return (v - xmin) / (xmax - xmin) * 2.f - 1.0f;
    }

    public double transform_x(double v) {
        return (v - xmin) / (xmax - xmin) * 2. - 1.0;
    }

    public float transform_y(float v) {
        return (v - ymin) / (ymax - ymin) * 2.f - 1.0f;
    }

    public double transform_y(double v) {
        return (v - ymin) / (ymax - ymin) * 2. - 1.0;
    }

    public float transform_z(float v) {
        return (v - zmin) / (zmax - zmin) * 2.f - 1.0f;
    }

    public double transform_z(double v) {
        return (v - zmin) / (zmax - zmin) * 2. - 1.0;
    }

    public float[] transformf(PointZ p) {
        return new float[]{transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z)};
    }

    public double[] transform(PointZ p) {
        return new double[]{transform_x(p.X), transform_y(p.Y), transform_z(p.Z)};
    }
}
