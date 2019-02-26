/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.meteoinfo.drawing.Draw;
import org.meteoinfo.global.PointF;
import org.meteoinfo.legend.ColorBreak;
import org.meteoinfo.legend.PolygonBreak;
import org.meteoinfo.shape.ArcShape;
import org.meteoinfo.shape.Graphic;
import org.meteoinfo.shape.GraphicCollection;
import org.meteoinfo.shape.Shape;

/**
 *
 * @author Yaqiang Wang
 */
public class PiePlot extends Plot2D {

    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public PiePlot(){
        super();
        this.setAutoAspect(false);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    void drawGraph(Graphics2D g, Rectangle2D area) {
        AffineTransform oldMatrix = g.getTransform();
        //Rectangle oldRegion = g.getClipBounds();
        //g.setClip(area);
        g.translate(area.getX(), area.getY());

        //Draw background
        if (this.background != null) {
            g.setColor(this.getBackground());
            g.fill(new Rectangle2D.Double(0, 0, area.getWidth(), area.getHeight()));
        }

        for (int m = 0; m < this.getGraphics().getNumGraphics(); m++) {
            Graphic graphic = this.getGraphics().get(m);
            ColorBreak cb = graphic.getLegend();
            float dist = 5;
            float ex = this.getExplode();
            Font labelFont = ((GraphicCollection)graphic).getLabelSet().getLabelFont();
            Color labelColor = ((GraphicCollection)graphic).getLabelSet().getLabelColor();
            for (int i = 0; i < graphic.getNumGraphics(); i++) {
                Graphic gg = graphic.getGraphicN(i);
                if (!graphic.isSingleLegend()) {
                    cb = gg.getLegend();
                }
                Shape shape = gg.getShape();
                this.drawArc(g, (ArcShape) shape, (PolygonBreak) cb, area, dist, ex, labelFont,
                    labelColor);
            }
        }

        g.setTransform(oldMatrix);
        //g.setClip(oldRegion);
    }

    private float getExplode() {
        Graphic graphic = this.getGraphics().get(0);
        float ex = 0;
        for (int i = 0; i < graphic.getNumGraphics(); i++) {
            Graphic gg = graphic.getGraphicN(i);
            ArcShape shape = (ArcShape) gg.getShape();
            if (shape.getExplode() > 0) {
                ex = 10;
                break;
            }
        }
        return ex;
    }

    private void drawArc(Graphics2D g, ArcShape aShape, PolygonBreak aPGB,
            Rectangle2D area, float dist, float ex, Font labelFont, Color labelColor) {
        float startAngle = aShape.getStartAngle();
        float sweepAngle = aShape.getSweepAngle();
        float angle = startAngle + sweepAngle / 2;
        float space = 20;
        Rectangle2D rect = new Rectangle2D.Double(area.getX() + ex + space, area.getY() + ex + space, area.getWidth() - ex - space,
                area.getHeight() - ex - space);
        double dx = 0, dy = 0;
        if (aShape.getExplode() > 0) {
            dx = ex * Math.cos((360 - angle) * Math.PI / 180);
            dy = ex * Math.sin((360 - angle) * Math.PI / 180);
            rect.setRect(rect.getX() + dx, rect.getY() + dy, rect.getWidth(), rect.getHeight());
        }
        float sx = (float) (rect.getX() - area.getX());
        float sy = (float) (rect.getY() - area.getY());
        Draw.drawPie(new PointF(sx, sy), (float) rect.getWidth(), (float) rect.getHeight(), 
                startAngle, sweepAngle, aPGB, g);

        //Draw label
        //Rectangle clip = g.getClipBounds();
        //if (clip != null) {
        //    g.setClip(null);
        //}
        float x, y, w, h;
        PointF sPoint = new PointF((float) (rect.getWidth() * 0.5 + sx), (float) (rect.getHeight() * 0.5 + sy));
        String label = aPGB.getCaption();
        if (angle > 360) {
            angle = angle - 360;
        }
        float r = (float) (rect.getWidth() * 0.5) + dist;
        PointF lPoint = Draw.getPieLabelPoint(sPoint, r, angle);
        x = lPoint.X;
        y = lPoint.Y;
        Dimension dim = Draw.getStringDimension(label, g);
        h = dim.height;
        w = dim.width;
        if ((angle >= 0 && angle < 45)) {
            //x = x + dis;
            y = y - h;
        } else if (angle >= 45 && angle < 90) {
            //y = y - dis;
        } else if (angle >= 90 && angle < 135) {
            x = x - w;
            //y = y - dis;
        } else if (angle >= 135 && angle < 225) {
            x = x - w - 3;
            y = y + h / 2;
        } else if (angle >= 225 && angle < 270) {
            x = x - w / 2;
            y = y + h;
        } else if (angle >= 270 && angle < 315) {
            //x = x + dis;
            y = y + h;
        } else {
            //x = x + dis;
            y = y + h / 2;
        }
        g.setFont(labelFont);
        g.setColor(labelColor);
        //g.drawOval((int)(x - 3), (int)(y - 3), 6, 6);
        g.drawString(label, x, y);

        //if (clip != null) {
        //    g.setClip(clip);
        //}
    }
    // </editor-fold>
}
