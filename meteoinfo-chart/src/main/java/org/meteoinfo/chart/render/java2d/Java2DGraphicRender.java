package org.meteoinfo.chart.render.java2d;

import org.meteoinfo.chart.graphic.Graphic;
import org.meteoinfo.chart.render.GraphicRender;
import org.meteoinfo.geometry.Extent;
import org.meteoinfo.common.MIMath;
import org.meteoinfo.common.PointF;
import org.meteoinfo.geometry.GeometryUtil;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.geometry.shape.ArcShape;
import org.meteoinfo.geometry.shape.EllipseShape;
import org.meteoinfo.geometry.shape.PolygonShape;
import org.meteoinfo.render.java2d.Draw;

import java.awt.*;

public class Java2DGraphicRender implements GraphicRender {

    /**
     * Draw graphic
     *
     * @param points The points
     * @param aGraphic The graphic
     * @param g Graphics2D
     * @param isEditingVertices Is editing vertices
     */
    public static void drawGraphic(PointF[] points, Graphic aGraphic, Graphics2D g, boolean isEditingVertices) {
        Rectangle rect = new Rectangle();
        Extent aExtent = org.meteoinfo.geometry.GeometryUtil.getPointFsExtent(points);
        rect.x = (int) aExtent.minX;
        rect.y = (int) aExtent.minY;
        rect.width = (int) aExtent.getWidth();
        rect.height = (int) aExtent.getHeight();

        switch (aGraphic.getShape().getShapeType()) {
            case POINT:
                switch (aGraphic.getLegendBreak().getBreakType()) {
                    case POINT_BREAK:
                        Draw.drawPoint((PointF) points[0].clone(), (PointBreak) aGraphic.getLegendBreak(), g);
                        int aSize = (int) ((PointBreak) aGraphic.getLegendBreak()).getSize() / 2 + 2;
                        rect.x = (int) points[0].X - aSize;
                        rect.y = (int) points[0].Y - aSize;
                        rect.width = aSize * 2;
                        rect.height = aSize * 2;
                        break;
                    case LABEL_BREAK:
                        Draw.drawLabelPoint((PointF) points[0].clone(), (LabelBreak) aGraphic.getLegendBreak(), g, rect);
                        break;
                }
                break;
            case POLYLINE:
                if (aGraphic.getLegendBreak().getBreakType() == BreakTypes.COLOR_BREAK_COLLECTION) {
                    Draw.drawPolyline(points, (ColorBreakCollection) aGraphic.getLegendBreak(), g);
                } else {
                    Draw.drawPolyline(points, (PolylineBreak) aGraphic.getLegendBreak(), g);
                }
                break;
            case POLYGON:
                PolygonShape pgs = (PolygonShape) aGraphic.getShape().clone();
                pgs.setPoints_keep(points);
                Draw.drawPolygonShape(pgs, (PolygonBreak) aGraphic.getLegendBreak(), g);
                break;
            case RECTANGLE:
                //drawPolygon(points, (PolygonBreak) aGraphic.getLegend(), g);
                Extent extent = GeometryUtil.getExtent(points);
                Draw.drawRectangle(new PointF((float)extent.minX, (float)extent.minY),
                        (float)extent.getWidth(), (float)extent.getHeight(),
                        (PolygonBreak) aGraphic.getLegendBreak(), g);
                break;
            case CURVE_LINE:
                Draw.drawCurveLine(points, (PolylineBreak) aGraphic.getLegendBreak(), g);
                break;
            case CURVE_POLYGON:
                Draw.drawCurvePolygon(points, (PolygonBreak) aGraphic.getLegendBreak(), g);
                break;
            case CIRCLE:
                Draw.drawCircle(points, (PolygonBreak) aGraphic.getLegendBreak(), g);
                break;
            case ELLIPSE:
                EllipseShape eshape = (EllipseShape) aGraphic.getShape();
                Draw.drawEllipse(points, eshape.getAngle(), (PolygonBreak) aGraphic.getLegendBreak(), g);
                break;
            case ARC:
                ArcShape arcShape = (ArcShape) aGraphic.getShape();
                Draw.drawArc(points, arcShape, (PolygonBreak) aGraphic.getLegendBreak(), g);
                break;
        }

        //Draw selected rectangle
        if (aGraphic.getShape().isSelected()) {
            if (isEditingVertices) {
                Draw.drawSelectedVertices(g, points);
            } else {
                float[] dashPattern = new float[]{2.0F, 1.0F};
                g.setColor(Color.cyan);
                g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
                g.draw(rect);
                switch (aGraphic.getShape().getShapeType()) {
                    case POINT:
                        if (aGraphic.getLegendBreak().getBreakType() == BreakTypes.POINT_BREAK) {
                            Draw.drawSelectedCorners(g, rect);
                        }
                        break;
                    case POLYLINE:
                    case CURVE_LINE:
                    case POLYGON:
                    case RECTANGLE:
                    case ELLIPSE:
                    case CURVE_POLYGON:
                        Draw.drawSelectedCorners(g, rect);
                        Draw.drawSelectedEdgeCenters(g, rect);
                        break;
                    case CIRCLE:
                        Draw.drawSelectedCorners(g, rect);
                        break;
                }
            }
        }
    }

    @Override
    public void draw() {

    }
}
