/*
 Copyright 2006 Jerry Huxtable

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 * This file was semi-automatically converted from the public-domain USGS PROJ source.
 */
package org.meteoinfo.projection.proj4j.proj;

import org.meteoinfo.projection.proj4j.ProjCoordinate;

public class TransverseCylindricalEqualArea extends Projection {

    private double rk0;

    public TransverseCylindricalEqualArea() {
        proj4Name = "tcea";
        name = "Transverse_Cylindrical_Equal_Area";
        initialize();
    }

    @Override
    public ProjCoordinate project(double lplam, double lpphi, ProjCoordinate out) {
        out.x = rk0 * Math.cos(lpphi) * Math.sin(lplam);
        out.y = scaleFactor * (Math.atan2(Math.tan(lpphi), Math.cos(lplam)) - projectionLatitude);
        return out;
    }

    @Override
    public ProjCoordinate projectInverse(double xyx, double xyy, ProjCoordinate out) {
        double t;

        out.y = xyy * rk0 + projectionLatitude;
        out.x *= scaleFactor;
        t = Math.sqrt(1. - xyx * xyx);
        out.y = Math.asin(t * Math.sin(xyy));
        out.x = Math.atan2(xyx, t * Math.cos(xyy));
        return out;
    }

    @Override
    public void initialize() { // tcea
        super.initialize();
        rk0 = 1 / scaleFactor;
    }

    @Override
    public boolean isRectilinear() {
        return false;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public String toString() {
        return "Transverse Cylindrical Equal Area";
    }
}
