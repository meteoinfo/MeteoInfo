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
package org.meteoinfo.geometry;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class Extent implements Cloneable {
    // <editor-fold desc="Variables">
    public double minX;
    public double maxX;
    public double minY;
    public double maxY;
    public double minZ;
    public double maxZ;

    public static Extent Identity = new Extent(0, 1, 0, 1);
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public Extent() {
    }

    /**
     * Constructor
     *
     * @param xMin Minimum X
     * @param xMax Maximum X
     * @param yMin Minimum Y
     * @param yMax Maximum Y
     */
    public Extent(double xMin, double xMax, double yMin, double yMax) {
        if (xMin > xMax) {
            minX = xMax;
            maxX = xMin;
        } else {
            minX = xMin;
            maxX = xMax;
        }

        if (yMin > yMax) {
            minY = yMax;
            maxY = yMin;
        } else {
            minY = yMin;
            maxY = yMax;
        }
    }

    /**
     * Constructor
     *
     * @param xMin Minimum X
     * @param xMax Maximum X
     * @param yMin Minimum Y
     * @param yMax Maximum Y
     * @param zMin Minimum Z
     * @param zMax Maximum Z
     */
    public Extent(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
        if (xMin > xMax) {
            minX = xMax;
            maxX = xMin;
        } else {
            minX = xMin;
            maxX = xMax;
        }

        if (yMin > yMax) {
            minY = yMax;
            maxY = yMin;
        } else {
            minY = yMin;
            maxY = yMax;
        }

        if (zMin > zMax) {
            minZ = zMax;
            maxZ = zMin;
        } else {
            minZ = zMin;
            maxZ = zMax;
        }
    }

    /**
     * Constructor
     *
     * @param aExtent The extent
     */
    public Extent(Extent aExtent) {
        this.minX = aExtent.minX;
        this.maxX = aExtent.maxX;
        this.minY = aExtent.minY;
        this.maxY = aExtent.maxY;
        this.minZ = aExtent.minZ;
        this.maxZ = aExtent.maxZ;
    }

    /**
     * Constructor
     * @param rec The rectangle
     */
    public Extent(Rectangle2D rec) {
        this.minX = rec.getMinX();
        this.maxX = rec.getMaxX();
        this.minY = rec.getMinY();
        this.maxY = rec.getMaxY();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    /**
     * Get Z axis length
     * @return Z axis length
     */
    public double getZLength() {
        return this.maxZ - this.minZ;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Judge if this extent include another extent
     *
     * @param bExtent extent
     * @return is included
     */
    public boolean include(Extent bExtent) {
        if (minX <= bExtent.minX && maxX >= bExtent.maxX && minY <= bExtent.minY && maxY >= bExtent.maxY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tests whether this extent intersects the second extent.
     *
     * @param bET The second extent
     * @return Boolean
     */
    public boolean intersects(Extent bET) {
        return !(maxX < bET.minX || maxY < bET.minY || maxZ < bET.minZ ||
                bET.maxX < minX || bET.maxY < minY || bET.maxZ < minZ);
    }

    /**
     * Tests if the extent contains the point
     *
     * @param p The point
     * @return Contains or not
     */
    public boolean contains(Coordinate p) {
        if (p.is2D()) {
            return (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY);
        } else {
            return (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY && p.z >= minZ && p.z <= maxZ);
        }
    }

    /**
     * Convert to rectangle
     *
     * @return rectangle
     */
    public Rectangle convertToRectangle() {
        return new Rectangle((int) minX, (int) minY, (int) getWidth(), (int) getHeight());
    }

    /**
     * Convert to rectangle
     *
     * @return rectangle
     */
    public Rectangle2D toRectangle() {
        return new Rectangle2D.Double(minX, minY, getWidth(), getHeight());
    }

    /**
     * Get center point
     *
     * @return Center point
     */
    public Coordinate getCenterPoint() {
        return new Coordinate((maxX - minX) / 2 + minX, (maxY - minY) / 2 + minY, (maxZ - minZ) / 2 + minZ);
    }

    /**
     * Get center point
     *
     * @return Center point
     */
    public double[] getCenter() {
        return new double[]{(maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2};
    }

    /**
     * Shift extent
     *
     * @param dx X shift value
     * @param dy Y shift value
     * @param dz Z shift value
     * @return Shifted extent
     */
    public Extent shift(double dx, double dy, double dz) {
        return new Extent(minX + dx, maxX + dx, minY + dy, maxY + dy, minZ + dz, maxZ + dz);
    }

    /**
     * Shift extent
     *
     * @param dx X shift value
     * @param dy Y shift value
     * @return Shifted extent
     */
    public Extent shift(double dx, double dy) {
        return new Extent(minX + dx, maxX + dx, minY + dy, maxY + dy);
    }
    
    /**
     * Extends extent by ratio
     * @param ratio The ratio
     * @return Extended extent
     */
    public Extent extend(double ratio) {
        double dx = this.getWidth() * ratio;
        double dy = this.getHeight() * ratio;
        double dz = (maxZ - minZ) * ratio;
        return extend(dx, dy, dz);
    }

    /**
     * Extends extent
     *
     * @param dx X delta
     * @param dy Y delta
     * @return Extended extent
     */
    public Extent extend(double dx, double dy) {
        return new Extent(minX - dx, maxX + dx, minY - dy, maxY + dy);
    }

    /**
     * Extends extent
     *
     * @param dx X delta
     * @param dy Y delta
     * @param dz Z delta
     * @return Extended extent
     */
    public Extent extend(double dx, double dy, double dz) {
        return new Extent(minX - dx, maxX + dx, minY - dy, maxY + dy, minZ - dz, maxZ + dz);
    }

    /**
     * Get is NaN or not
     *
     * @return Boolean
     */
    public boolean isNaN() {
        return Double.isNaN(minX) || Double.isNaN(maxX) || Double.isNaN(minY) || Double.isNaN(maxY);
    }

    /**
     * Make the extent with non-zero length of the dimensions
     */
    public void asNonZero() {
        double width = this.getWidth();
        double height = this.getHeight();
        double zLength = this.getZLength();
        double v = Math.max(width, height);
        v = Math.max(v, zLength);
        v = v / 2.;
        if (width == 0) {
            this.minX -= v;
            this.maxX += v;
        }
        if (height == 0) {
            this.minY -= v;
            this.maxY += v;
        }
        if (zLength == 0) {
            this.minZ -= v;
            this.maxZ += v;
        }
    }

    /**
     * Return union extent
     *
     * @param ex Other extent
     * @return Union extent
     */
    public Extent union(Extent ex) {
        Extent cET = new Extent();
        if (this.isNaN()) {
            return (Extent) ex.clone();
        } else if (ex.isNaN()) {
            return (Extent) this.clone();
        }

        cET.minX = Math.min(this.minX, ex.minX);
        cET.minY = Math.min(this.minY, ex.minY);
        cET.maxX = Math.max(this.maxX, ex.maxX);
        cET.maxY = Math.max(this.maxY, ex.maxY);
        cET.minZ = Math.min(this.minZ, ex.minZ);
        cET.maxZ = Math.max(this.maxZ, ex.maxZ);

        return cET;
    }

    /**
     * Clone
     *
     * @return Extent object
     */
    @Override
    public Object clone() {
        return new Extent(minX, maxX, minY, maxY, minZ, maxZ);
    }
    // </editor-fold>
}
