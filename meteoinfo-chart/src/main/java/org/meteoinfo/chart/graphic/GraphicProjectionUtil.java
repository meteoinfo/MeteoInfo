package org.meteoinfo.chart.graphic;

import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.projection.ProjectionInfo;
import org.meteoinfo.projection.ProjectionNames;
import org.meteoinfo.projection.ProjectionUtil;
import org.meteoinfo.projection.Reproject;

public class GraphicProjectionUtil extends ProjectionUtil {
    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectClipGraphic(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (graphic instanceof MeshGraphic) {
            CoordinateTransform trans = new CoordinateTransformFactory().createTransform(fromProj.getCoordinateReferenceSystem(),
                    toProj.getCoordinateReferenceSystem());
            float[] vertex = ((MeshGraphic) graphic).getVertexPosition();
            for (int i = 0; i < vertex.length; i+=3) {
                ProjCoordinate p1 = new ProjCoordinate(vertex[i], vertex[i + 1]);
                ProjCoordinate p2 = new ProjCoordinate();
                trans.transform(p1, p2);
                vertex[i] = (float) p2.x;
                vertex[i + 1] = (float) p2.y;
            }
            ((MeshGraphic) graphic).setVertexPosition(vertex);
            return graphic;
        } else {
            return ProjectionUtil.projectClipGraphic(graphic, fromProj, toProj);
        }
    }
}
