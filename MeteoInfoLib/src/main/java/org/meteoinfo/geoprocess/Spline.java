 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.geoprocess;

import org.meteoinfo.global.PointD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Yaqiang Wang
 */
public class Spline {

    private static class Vec2 {

        public double X, Y;

        public Vec2(double x, double y) {
            this.X = x;
            this.Y = y;
        }

        public Vec2(PointD p) {
            this.X = p.X;
            this.Y = p.Y;
        }

        public PointD toPointD() {
            return new PointD(X, Y);
        }

        public Vec2 add(Vec2 v) {
            return new Vec2(this.X + v.X, this.Y + v.Y);
        }

        public Vec2 subtract(Vec2 v) {
            return new Vec2(this.X - v.X, this.Y - v.Y);
        }

        public Vec2 multiply(float f) {
            return new Vec2(this.X * f, this.Y * f);
        }

        public Vec2 divide(float f) {
            return new Vec2(this.X / f, this.Y / f);
        }
    }

    private static PointD[] interpolateBezier(PointD p0, PointD p1, PointD p2, PointD p3, int samples) {
        PointD[] result = new PointD[samples];
        Vec2 v0 = new Vec2(p0);
        Vec2 v1 = new Vec2(p1);
        Vec2 v2 = new Vec2(p2);
        Vec2 v3 = new Vec2(p3);
        for (int i = 0; i < samples; i++) {
            float t = (i + 1) / (samples + 1.0f);
            result[i] = (v0.multiply((1 - t) * (1 - t) * (1 - t)).
                    add(v1.multiply(3 * (1 - t) * (1 - t) * t)).
                    add(v2.multiply(3 * (1 - t) * t * t)).
                    add(v3.multiply(t * t * t))).toPointD();
        }
        return result;
    }

    private static PointD[] interpolateCardinalSpline(PointD p0, PointD p1, PointD p2, PointD p3, int samples) {
        float tension = 0.5f;
        Vec2 v0 = new Vec2(p0);
        Vec2 v1 = new Vec2(p1);
        Vec2 v2 = new Vec2(p2);
        Vec2 v3 = new Vec2(p3);

        PointD u = v2.subtract(v0).multiply(tension / 3).add(v1).toPointD();
        PointD v = v1.subtract(v3).multiply(tension / 3).add(v2).toPointD();

        return interpolateBezier(p1, u, v, p2, samples);
    }

    /**
     * '基数样条'内插法。 points为通过点，samplesInSegment为两个样本点之间的内插数量。
     *
     * @param points The points
     * @param samplesInSegment Sample in segment
     * @return Splined points
     */
    public static PointD[] cardinalSpline(PointD[] points, int samplesInSegment) {
        List<PointD> result = new ArrayList<>();
        for (int i = 0; i < points.length - 1; i++) {
            result.add(points[i]);
            PointD[] pds = interpolateCardinalSpline(
                    points[Math.max(i - 1, 0)],
                    points[i],
                    points[i + 1],
                    points[Math.min(i + 2, points.length - 1)],
                    samplesInSegment);
            result.addAll(Arrays.asList(pds));
        }
        result.add(points[points.length - 1]);
        return (PointD[]) result.toArray(new PointD[result.size()]);
    }
}
