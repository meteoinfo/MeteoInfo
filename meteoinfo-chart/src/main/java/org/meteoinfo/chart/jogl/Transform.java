package org.meteoinfo.chart.jogl;

import org.joml.Vector3f;
import org.meteoinfo.chart.AspectType;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.shape.PointZ;

import javax.swing.*;
import java.nio.FloatBuffer;

public class Transform {
    protected AspectType aspectType = AspectType.AUTO;
    protected float xmin, xmax = 1.0f, ymin;
    protected float ymax = 1.0f, zmin, zmax = 1.0f;

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
        this.setExtent(xmin, xmax, ymin, ymax, zmin, zmax);
    }

    /**
     * Constructor
     * @param extent3D Extent 3D
     */
    public Transform(Extent3D extent3D) {
        this.setExtent(extent3D);
    }

    public void setExtent(Extent3D extent3D) {
        setExtent((float) extent3D.minX, (float) extent3D.maxX, (float) extent3D.minY,
                  (float) extent3D.maxY, (float) extent3D.minZ, (float) extent3D.maxZ);
    }

    /**
     * Set extent
     * @param xmin X minimum
     * @param xmax X Maximum
     * @param ymin Y minimum
     * @param ymax Y maximum
     * @param zmin Z minimum
     * @param zmax Z maximum
     */
    public void setExtent(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.zmin = zmin;
        this.zmax = zmax;

        if (this.aspectType != AspectType.AUTO) {
            float xRange = xmax - xmin;
            float yRange = ymax - ymin;
            float maxXYRange = xRange > yRange ? xRange : yRange;
            float xRatio = xRange / maxXYRange;
            float yRatio = yRange / maxXYRange;
            if (this.aspectType == AspectType.EQUAL) {
                float zRange = (zmax - zmin);
                float maxRange = zRange > maxXYRange ? zRange : maxXYRange;
                xRatio = xRange / maxRange;
                yRatio = yRange / maxRange;
                float zRatio = zRange / maxRange;
                if (zRatio != 1) {
                    this.zmin = zmin / zRatio;
                    this.zmax = zmax / zRatio;
                }
            }
            if (xRatio != 1) {
                this.xmin = xmin / xRatio;
                this.xmax = xmax / xRatio;
            }
            if (yRatio != 1) {
                this.ymin = ymin / yRatio;
                this.ymax = ymax / yRatio;
            }
        }
    }

    public AspectType getAspectType() {
        return this.aspectType;
    }

    public void setAspectType(AspectType value) {
        this.aspectType = value;
    }

    public boolean equals(Transform other) {
        if (this.aspectType != other.aspectType)
            return false;
        if (this.xmin != other.xmin)
            return false;
        if (this.xmax != other.xmax)
            return false;
        if (this.ymin != other.ymin)
            return false;
        if (this.ymax != other.ymax)
            return false;
        if (this.zmin != other.zmin)
            return false;
        if (this.zmax != other.zmax)
            return false;

        return true;
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

    public float transformXDis(float len) {
        return transform_x(len) - transform_x(0.0f);
    }

    public float transformYDis(float len) {
        return transform_y(len) - transform_y(0);
    }

    public float[] transform(float x, float y, float z) {
        return new float[] {transform_x(x), transform_y(y), transform_z(z)};
    }

    public float[] transformf(PointZ p) {
        return new float[]{transform_x((float) p.X), transform_y((float) p.Y), transform_z((float) p.Z)};
    }

    public double[] transform(PointZ p) {
        return new double[]{transform_x(p.X), transform_y(p.Y), transform_z(p.Z)};
    }

    public float[] transform(Vector3f p) {
        return transform(p.x, p.y, p.z);
    }

    /**
     * Clone
     * @return Cloned transform
     */
    public Object clone() {
        return new Transform(xmin, xmax, ymin, ymax, zmin, zmax);
    }
}
