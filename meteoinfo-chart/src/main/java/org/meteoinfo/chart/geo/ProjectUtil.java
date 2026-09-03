package org.meteoinfo.chart.geo;

import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.geometry.shape.Shape;
import org.meteoinfo.projection.ProjectionInfo;

import java.util.ArrayList;
import java.util.List;

import static org.meteoinfo.projection.ProjectionUtil.projectClipShape;
import static org.meteoinfo.projection.ProjectionUtil.projectShape;

public class ProjectUtil {

    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectGraphic(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (graphic instanceof GraphicCollection) {
            GraphicCollection newGCollection = new GraphicCollection();
            for (Graphic aGraphic : ((GraphicCollection) graphic).getGraphics()) {
                aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
                if (aGraphic.getShape() != null) {
                    newGCollection.add(aGraphic);
                }
            }
            newGCollection.setLegendScheme(((GraphicCollection) graphic).getLegendScheme());
            newGCollection.setSingleLegend(((GraphicCollection) graphic).isSingleLegend());
            newGCollection.setAntiAlias(graphic.isAntiAlias());

            return newGCollection;
        } else {
            Shape shape = projectShape(graphic.getShape(), fromProj, toProj);
            Graphic rGraphic = new Graphic(shape, graphic.getLegendBreak());
            rGraphic.setAntiAlias(graphic.isAntiAlias());
            return rGraphic;
        }
    }

    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectClipGraphic(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (graphic instanceof GraphicCollection) {
            try {
                List<Graphic> graphics = new ArrayList<>();
                for (Graphic aGraphic : ((GraphicCollection) graphic).getGraphics()) {
                    List<? extends Shape> shapes = projectClipShape(aGraphic.getShape(), fromProj, toProj);
                    if (shapes != null && shapes.size() > 0) {
                        if (shapes.get(0).getExtent() == null) {
                            //System.out.println(shapes.get(0).toString());
                            continue;
                        }
                        aGraphic.setShape(shapes.get(0));
                        graphics.add(aGraphic);
                    }
                }
                ((GraphicCollection) graphic).setGraphics(graphics);

                return graphic;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            List<? extends Shape> shapes = projectClipShape(graphic.getShape(), fromProj, toProj);
            if (shapes != null && shapes.size() > 0) {
                graphic.setShape(shapes.get(0));
                return graphic;
            } else {
                return null;
            }
        }
    }

    /**
     * Project graphic
     *
     * @param graphic The graphic
     * @param fromProj From projection
     * @param toProj To projection
     * @return Projected graphic
     */
    public static Graphic projectClipGraphic_old(Graphic graphic, ProjectionInfo fromProj, ProjectionInfo toProj) {
        if (graphic instanceof GraphicCollection) {
            try {
                Graphic newGCollection = graphic.getClass().getDeclaredConstructor().newInstance();
                for (Graphic aGraphic : ((GraphicCollection) graphic).getGraphics()) {
                    List<? extends Shape> shapes = projectClipShape(aGraphic.getShape(), fromProj, toProj);
                    if (shapes != null && shapes.size() > 0) {
                        aGraphic.setShape(shapes.get(0));
                        ((GraphicCollection) newGCollection).add(aGraphic);
                    }
                }
                ((GraphicCollection) newGCollection).setLegendScheme(((GraphicCollection) graphic).getLegendScheme());
                ((GraphicCollection) newGCollection).setSingleLegend(((GraphicCollection) graphic).isSingleLegend());
                newGCollection.setAntiAlias(graphic.isAntiAlias());

                return newGCollection;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            List<? extends Shape> shapes = projectClipShape(graphic.getShape(), fromProj, toProj);
            if (shapes != null && shapes.size() > 0) {
                Graphic rGraphic = new Graphic(shapes.get(0), graphic.getLegendBreak());
                rGraphic.setAntiAlias(graphic.isAntiAlias());
                return rGraphic;
            } else {
                return null;
            }
        }
    }

    /*public static GraphicCollection projectGraphic(GraphicCollection aGCollection, ProjectionInfo fromProj, ProjectionInfo toProj) {
        GraphicCollection newGCollection = new GraphicCollection();
        for (Graphic aGraphic : aGCollection.getGraphics()) {
            aGraphic.setShape(projectShape(aGraphic.getShape(), fromProj, toProj));
            if (aGraphic.getShape() != null) {
                newGCollection.add(aGraphic);
            }
        }

        return newGCollection;
    }*/

    public static List<Graphic> projectGraphic(List<Graphic> graphics, ProjectionInfo fromProj, ProjectionInfo toProj) {
        List<Graphic> newGraphics = new ArrayList<>();
        for (Graphic aGraphic : graphics) {
            Shape aShape = projectShape(aGraphic.getShape(), fromProj, toProj);
            if (aShape != null) {
                newGraphics.add(new Graphic(aShape, aGraphic.getLegendBreak()));
            }
        }

        return newGraphics;
    }
}
