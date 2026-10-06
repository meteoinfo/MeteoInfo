package org.meteoinfo.chart.transform;

import org.meteoinfo.geometry.Coordinate;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class Affine2D extends Transform {

    protected AffineTransform affineTransform;

    public Affine2D() {
        this.affineTransform = new AffineTransform();
    }

    public Affine2D(AffineTransform affineTransform) {
        this.affineTransform = affineTransform;
    }

    public AffineTransform getAffineTransform() {
        return  affineTransform;
    }

    @Override
    public Coordinate transform(Coordinate p) {
        Point2D dP = this.affineTransform.transform(new Point2D.Double(p.x, p.y), null);
        clearInvalid();
        return new Coordinate(dP.getX(), dP.getY());
    }

    /** Apply translation (tx, ty) */
    public Affine2D translate(double tx, double ty) {
        this.affineTransform.translate(tx, ty);
        invalidate();
        return this;
    }

    /** Apply scale transform, scaleX / scaleY for each axis */
    public Affine2D scale(double sx, double sy) {
        this.affineTransform.scale(sx, sy);
        invalidate();
        return this;
    }

    /** Rotate by radians around origin */
    public Affine2D rotate(double rad) {
        this.affineTransform.rotate(rad);
        invalidate();
        return this;
    }

    /** Rotate by degrees around origin */
    public Affine2D rotateDeg(double deg) {
        double rad = Math.toRadians(deg);
        this.affineTransform.rotate(rad);
        invalidate();
        return this;
    }

    /** Shear transform, sx: x‑shear factor, sy: y‑shear factor */
    public Affine2D shear(double sx, double sy) {
        this.affineTransform.shear(sx, sy);
        invalidate();
        return this;
    }

    public Affine2D inverted() {
        try {
            AffineTransform at = this.affineTransform.createInverse();
            return new Affine2D(at);
        } catch (NoninvertibleTransformException e) {
            throw new UnsupportedOperationException("Transform can not be inverted");
        }
    }

    /** Create deep copy of current affine transform */
    public Affine2D copy() {
        AffineTransform at = (AffineTransform) this.affineTransform.clone();
        return new Affine2D(at);
    }

    @Override
    public boolean isAffine() {
        return true;
    }

}
