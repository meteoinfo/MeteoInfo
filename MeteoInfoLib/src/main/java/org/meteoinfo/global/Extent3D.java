/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global;

/**
 *
 * @author Yaqiang Wang
 */
public class Extent3D extends Extent{
    public double minZ;
    public double maxZ;
    
    /**
     * Constructor
     */
    public Extent3D(){
        
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
    public Extent3D(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
        super(xMin, xMax, yMin, yMax);
        minZ = zMin;
        maxZ = zMax;
    }
    
    /**
     * Get is 3D or not
     * @return false
     */
    @Override
    public boolean is3D(){
        return true;
    }
    
    /**
     * Tests whether this extent intersects the second extent.
     * @param extent The second extent
     * @return Boolean
     */
    @Override
    public boolean intersects(Extent extent) {
        Extent3D bET = (Extent3D)extent;
        return !(maxX < bET.minX || maxY < bET.minY || maxZ < bET.minZ ||
                bET.maxX < minX || bET.maxY < minY || bET.maxZ < minZ);
    }
    
    /**
     * Return union extent
     *
     * @param ex Other extent
     * @return Union extent
     */
    public Extent3D union(Extent3D ex) {
        Extent3D cET = new Extent3D();
        if (this.isNaN()) {
            return (Extent3D) ex.clone();
        } else if (ex.isNaN()) {
            return (Extent3D) this.clone();
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
     * Extends extent by ratio
     * @param ratio The ratio
     * @return Extended extent
     */
    public Extent3D extend(double ratio) {
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
     * @param dz Z delta
     * @return Extended extent
     */
    public Extent3D extend(double dx, double dy, double dz) {
        return new Extent3D(minX - dx, maxX + dx, minY - dy, maxY + dy, minZ - dz, maxZ + dz);
    }
    
    /**
     * Clone
     *
     * @return Extent object
     */
    @Override
    public Object clone() {
        return new Extent3D(minX, maxX, minY, maxY, minZ, maxZ);
    }

}
