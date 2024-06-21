package org.meteoinfo.projection;

import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;
import org.meteoinfo.common.PointD;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.Transform;

public class GeoTransform extends Transform {

    protected CoordinateTransform coordinateTransform;
    protected ProjectionInfo sourceProj;
    protected ProjectionInfo targetProj;

    /**
     * Constructor
     * @param source Source projection info
     * @param target Target projection info
     */
    public GeoTransform(ProjectionInfo source, ProjectionInfo target) {
        this.sourceProj = source;
        this.targetProj = target;
        this.coordinateTransform = new BasicCoordinateTransform(source.getCoordinateReferenceSystem(),
                target.getCoordinateReferenceSystem());
    }

    /**
     * Get source projection
     * @return Source projection
     */
    public ProjectionInfo getSourceProj() {
        return this.sourceProj;
    }

    /**
     * Get target projection
     * @return Target projection
     */
    public ProjectionInfo getTargetProj() {
        return this.targetProj;
    }

    @Override
    public boolean isValid() {
        return !sourceProj.equals(targetProj);
    }

    @Override
    public PointD transform(double x, double y) {
        ProjCoordinate s = new ProjCoordinate(x, y);
        ProjCoordinate t = new ProjCoordinate();
        this.coordinateTransform.transform(s, t);
        return new PointD(t.x, t.y);
    }

    @Override
    public Graphic transform(Graphic graphic) {
        return ProjectionUtil.projectClipGraphic(graphic, this.sourceProj, this.targetProj);
    }

    @Override
    public Transform inverted() {
        return new GeoTransform(this.targetProj, this.sourceProj);
    }
}
