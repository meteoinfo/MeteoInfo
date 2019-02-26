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
import org.meteoinfo.projection.proj4j.util.ProjectionMath;

public class MillerProjection extends CylindricalProjection {

    public MillerProjection() {
        proj4Name = "mill";
        name = "MillerCylindrical";
    }

    @Override
    public ProjCoordinate project(double lplam, double lpphi, ProjCoordinate out) {
        out.x = lplam;
        out.y = Math.log(Math.tan(ProjectionMath.QUARTERPI + lpphi * .4)) * 1.25;
        return out;
    }

    @Override
    public ProjCoordinate projectInverse(double xyx, double xyy, ProjCoordinate out) {
        out.x = xyx;
        out.y = 2.5 * (Math.atan(Math.exp(.8 * xyy)) - ProjectionMath.QUARTERPI);
        return out;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public String toString() {
        return "Miller Cylindrical";
    }
}
