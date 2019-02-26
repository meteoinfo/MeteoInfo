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

public class CrasterProjection extends Projection {

    private final static double XM = 0.97720502380583984317;
    private final static double RXM = 1.02332670794648848847;
    private final static double YM = 3.06998012383946546542;
    private final static double RYM = 0.32573500793527994772;
    private final static double THIRD = 0.333333333333333333;

    public CrasterProjection() {
        proj4Name = "crast";
        name = "Craster_Parabolic";
    }

    @Override
    public ProjCoordinate project(double lplam, double lpphi, ProjCoordinate out) {
        lpphi *= THIRD;
        out.x = XM * lplam * (2. * Math.cos(lpphi + lpphi) - 1.);
        out.y = YM * Math.sin(lpphi);
        return out;
    }

    @Override
    public ProjCoordinate projectInverse(double xyx, double xyy, ProjCoordinate out) {
        out.y = 3. * Math.asin(xyy * RYM);
        out.x = xyx * RXM / (2. * Math.cos((out.y + out.y) * THIRD) - 1);
        return out;
    }

    /**
     * Returns true if this projection is equal area
     */
    @Override
    public boolean isEqualArea() {
        return true;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public String toString() {
        return "Craster Parabolic (Putnins P4)";
    }
}
