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

public class PutninsP5Projection extends Projection {

    protected double A;
    protected double B;
    private final static double C = 1.01346;
    private final static double D = 1.2158542;

    public PutninsP5Projection() {
        A = 2;
        B = 1;
        proj4Name = "putp5";
        name = "Putnins_P5";
    }

    @Override
    public ProjCoordinate project(double lplam, double lpphi, ProjCoordinate xy) {
        xy.x = C * lplam * (A - B * Math.sqrt(1. + D * lpphi * lpphi));
        xy.y = C * lpphi;
        return xy;
    }

    @Override
    public ProjCoordinate projectInverse(double xyx, double xyy, ProjCoordinate lp) {
        lp.y = xyy / C;
        lp.x = xyx / (C * (A - B * Math.sqrt(1. + D * lp.y * lp.y)));
        return lp;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public String toString() {
        return "Putnins P5";
    }
}
