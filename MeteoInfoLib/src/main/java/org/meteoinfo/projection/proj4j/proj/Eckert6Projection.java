/*
 Copyright 2011 Martin Davis

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */package org.meteoinfo.projection.proj4j.proj;

import org.meteoinfo.projection.proj4j.*;

public class Eckert6Projection extends PseudoCylindricalProjection {

    private static final double n = 2.570796326794896619231321691;
    private static final double C_y = Math.sqrt((2) / n);
    private static final double C_x = C_y / 2;
    private static final int MAX_ITER = 8;
    private static final double LOOP_TOL = 1e-7;

    public Eckert6Projection() {
        proj4Name = "eck6";
        name = "Eckert_VI";
    }

    @Override
    public ProjCoordinate project(double lam, double phi, ProjCoordinate xy) {

        int i;
        double k, V;
        k = n * Math.sin(phi);
        for (i = MAX_ITER; i > 0;) {
            phi -= V = (phi + Math.sin(phi) - k) / (1 + Math.cos(phi));
            if (Math.abs(V) < LOOP_TOL) {
                break;
            }
            --i;
        }
        if (i == 0) {
            throw new ProjectionException("F_ERROR");
        }

        xy.x = C_x * lam * (1 + Math.cos(phi));
        xy.y = C_y * phi;
        return xy;
    }

    @Override
    public ProjCoordinate projectInverse(double x, double y, ProjCoordinate lp) {
        y /= C_y;
        lp.y = Math.asin((y + Math.sin(y)) / n);
        lp.x = x / (C_x * (1 + Math.cos(y)));
        return lp;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public boolean isEqualArea() {
        return true;
    }

    @Override
    public String toString() {
        return "Eckert VI";
    }
}