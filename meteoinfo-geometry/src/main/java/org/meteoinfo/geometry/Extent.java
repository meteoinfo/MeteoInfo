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
        this(0, 1, 0, 1);
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

        minZ = Double.NaN;
        maxZ = Double.NaN;
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
     * Constructors
     * @param coordinate The coordinate
     */
    public Extent(Coordinate coordinate) {
        this(coordinate.x, coordinate.x, coordinate.y, coordinate.y, coordinate.z, coordinate.z);
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
        this(rec.getMinX(), rec.getMaxX(), rec.getMinY(), rec.getMaxY());
    }

    /**
     *  Makes this <code>Extent</code> a "null" extent, that is, the extent
     *  of the empty geometry.
     */
    public void setToNull() {
        minX = 0;
        maxX = -1;
        minY = 0;
        maxY = -1;
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

    /**
     * Check whether the extent is 2D or not
     * @return 2D or not
     */
    public boolean is2D() {
        return Double.isNaN(minZ) || Double.isNaN(maxZ);
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
        if (this.is2D() || bExtent.is2D()) {
            return minX <= bExtent.minX && maxX >= bExtent.maxX && minY <= bExtent.minY &&
                    maxY >= bExtent.maxY;
        } else {
            return minX <= bExtent.minX && maxX >= bExtent.maxX && minY <= bExtent.minY &&
                    maxY >= bExtent.maxY && minZ <= bExtent.minZ && maxZ >= bExtent.maxZ;
        }
    }

    /**
     *  Check if the point <code>(x, y)</code>
     *  intersects (lies inside) the region of this <code>Envelope</code>.
     *
     *@param  x  the x-ordinate of the point
     *@param  y  the y-ordinate of the point
     *@return        <code>true</code> if the point overlaps this <code>Envelope</code>
     */
    public boolean intersects(double x, double y) {
        if (isNull()) return false;

        return !(x > maxX ||
                x < minX ||
                y > maxY ||
                y < minY);
    }

    /**
     * Tests if the point <code>p</code>
     * intersects (lies inside) the region of this <code>Envelope</code>.
     *
     *@param  p  the <code>Coordinate</code> to be tested
     *@return <code>true</code> if the point intersects this <code>Envelope</code>
     */
    public boolean intersects(Coordinate p) {
        return intersects(p.x, p.y, p.z);
    }

    /**
     *  Check if the point <code>(x, y)</code>
     *  intersects (lies inside) the region of this <code>Envelope</code>.
     *
     *@param  x  the x-ordinate of the point
     *@param  y  the y-ordinate of the point
     *@return        <code>true</code> if the point overlaps this <code>Envelope</code>
     */
    public boolean intersects(double x, double y, double z) {
        if (isNull()) return false;

        return !(x > maxX ||
                x < minX ||
                y > maxY ||
                y < minY ||
                z > maxZ ||
                z < minZ);
    }

    /**
     * Tests whether this extent intersects the second extent.
     *
     * @param bET The second extent
     * @return Boolean
     */
    public boolean intersects(Extent bET) {
        if (this.is2D() || bET.is2D()) {
            return !(maxX < bET.minX || maxY < bET.minY ||
                    bET.maxX < minX || bET.maxY < minY);
        }
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
        if (this.is2D() || p.is2D()) {
            return (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY);
        } else {
            return (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY && p.z >= minZ && p.z <= maxZ);
        }
    }

    /**
     *  Returns <code>true</code> if this <code>Envelope</code> is a "null"
     *  envelope.
     *
     *@return    <code>true</code> if this <code>Envelope</code> is uninitialized
     *      or is the envelope of the empty geometry.
     */
    public boolean isNull() {
        return maxX < minX;
    }

    /**
     *  Enlarges this <code>Envelope</code> so that it contains
     *  the given point.
     *  Has no effect if the point is already on or within the envelope.
     *
     * @param  x  the value to lower the minimum x to or to raise the maximum x to
     * @param  y  the value to lower the minimum y to or to raise the maximum y to
     */
    public void expandToInclude(double x, double y) {
        if (isNull()) {
            minX = x;
            maxX = x;
            minY = y;
            maxY = y;
        } else {
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
    }

    /**
     *  Enlarges this <code>Envelope</code> so that it contains
     *  the given point.
     *  Has no effect if the point is already on or within the envelope.
     *
     * @param  x  the value to lower the minimum x to or to raise the maximum x to
     * @param  y  the value to lower the minimum y to or to raise the maximum y to
     * @param  z  the value to lower the minimum z to or to raise the maximum z to
     */
    public void expandToInclude(double x, double y, double z) {
        if (isNull()) {
            minX = x;
            maxX = x;
            minY = y;
            maxY = y;
            minZ = z;
            maxZ = z;
        } else {
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
    }

    /**
     *  Enlarges this <code>Envelope</code> so that it contains
     *  the given {@link Coordinate}.
     *  Has no effect if the point is already on or within the envelope.
     *
     *@param  p  the Coordinate to expand to include
     */
    public void expandToInclude(Coordinate p)
    {
        expandToInclude(p.x, p.y, p.z);
    }

    /**
     *  Enlarges this <code>Envelope</code> so that it contains
     *  the <code>other</code> Envelope.
     *  Has no effect if <code>other</code> is wholly on or
     *  within the envelope.
     *
     *@param  other  the <code>Envelope</code> to expand to include
     */
    public void expandToInclude(Extent other) {
        if (other.isNull()) {
            return;
        }

        if (isNull()) {
            minX = other.minX;
            maxX = other.maxX;
            minY = other.minY;
            maxY = other.maxY;
        } else {
            if (other.minX < minX) {
                minX = other.minX;
            }
            if (other.maxX > maxX) {
                maxX = other.maxX;
            }
            if (other.minY < minY) {
                minY = other.minY;
            }
            if (other.maxY > maxY) {
                maxY = other.maxY;
            }
            if (other.minZ < minZ) {
                minZ = other.minZ;
            }
            if (other.maxZ > maxZ) {
                maxZ = other.maxZ;
            }
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
