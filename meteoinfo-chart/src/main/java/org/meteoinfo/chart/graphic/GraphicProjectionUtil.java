package org.meteoinfo.chart.graphic;

import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.meteoinfo.chart.graphic.GeoGraphicCollection;
import org.meteoinfo.geometry.geoprocess.GeoComputation;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.projection.*;
import org.meteoinfo.table.DataColumn;
import org.meteoinfo.table.DataRow;
import org.meteoinfo.table.DataTable;
import org.meteoinfo.table.Field;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        } else if (graphic instanceof GeoGraphicCollection) {
            GeoGraphicCollection geoGraphic = (GeoGraphicCollection) graphic;
            try {
                GeoGraphicCollection newGCollection = new GeoGraphicCollection();
                DataTable dataTable = new DataTable();
                for (DataColumn aDC : geoGraphic.getAttributeTable().getTable().getColumns()) {
                    Field bDC = new Field(aDC.getColumnName(), aDC.getDataType());
                    dataTable.getColumns().add(bDC);
                }
                int idx = 0;
                for (Graphic aGraphic : geoGraphic.getGraphics()) {
                    List<? extends Shape> shapes = org.meteoinfo.projection.ProjectionUtil.projectClipShape(aGraphic.getShape(), fromProj, toProj);
                    if (shapes != null && shapes.size() > 0) {
                        aGraphic.setShape(shapes.get(0));
                        newGCollection.add(aGraphic);
                        DataRow aDR = geoGraphic.getAttributeTable().getTable().getRows().get(idx);
                        try {
                            dataTable.addRow(aDR);
                        } catch (Exception ex) {
                            Logger.getLogger(org.meteoinfo.chart.graphic.GraphicProjectionUtil.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    idx += 1;
                }
                newGCollection.setLegendScheme(geoGraphic.getLegendScheme());
                newGCollection.setSingleLegend(geoGraphic.isSingleLegend());
                newGCollection.setAntiAlias(geoGraphic.isAntiAlias());
                newGCollection.getAttributeTable().setTable(dataTable);
                newGCollection.setProjInfo(toProj);

                return newGCollection;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return ProjectionUtil.projectClipGraphic(graphic, fromProj, toProj);
        }
    }

}
