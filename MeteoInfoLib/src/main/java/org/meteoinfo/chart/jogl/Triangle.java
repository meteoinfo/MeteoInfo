package org.meteoinfo.chart.jogl;

import org.meteoinfo.shape.PointZ;

import java.util.ArrayList;
import java.util.List;

public class Triangle {
    private static final long serialVersionUID = 1;

    public final PointZ p1;
    public final PointZ p2;
    public final PointZ p3;

    public Triangle(PointZ p1, PointZ p2, PointZ p3) {
        super();
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public List<PointZ> getPoints() {
        List<PointZ> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        return points;
    }

    public PointZ[] getPointArray() {
        return new PointZ[]{p1, p2, p3};
    }

    @Override
    public String toString() {
        return "Triangle [p1=" + p1 + ", p2=" + p2 + ", p3=" + p3 + "]";
    }
}
