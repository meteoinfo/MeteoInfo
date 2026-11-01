package org.meteoinfo.chart;

import com.jogamp.opengl.GLAutoDrawable;
import org.meteoinfo.chart.graphic.GraphicCollection;
import org.meteoinfo.chart.jogl.GLPlot;
import org.meteoinfo.chart.plot.Plot;
import org.meteoinfo.chart.plot.Plot2D;
import org.meteoinfo.chart.plot.Plot3D;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A single plotting area inside a {@link Figure}.
 *
 * <p>An axes owns one {@link Plot} and one {@link AxesView}.  The view decides
 * whether the axes is 2-D or 3-D.  Painting is delegated to a renderer; this
 * class only comes into play for the Java2D fallback and JOGL forwarding.</p>
 *
 * @author Yaqiang Wang
 */
public class Axes implements Cloneable {

    private Plot plot;
    private AxesView view;

    private Rectangle2D position = new Rectangle2D.Double(0.13, 0.11, 0.775, 0.815);
    private Rectangle2D outerPosition = new Rectangle2D.Double(0, 0, 1, 1);
    private Rectangle2D positionArea = new Rectangle2D.Double();
    private Rectangle2D outerPositionArea = new Rectangle2D.Double();
    private Rectangle2D graphArea = new Rectangle2D.Double();
    private Margin tightInset = new Margin();

    private boolean subPlot;
    private int rowIndex;
    private int columnIndex;
    private boolean outerPosActive = true;
    private boolean visible = true;
    private boolean antiAlias = true;
    private boolean symbolAntiAlias = true;

    private ChartText title;
    private List<ChartLegend> legends = new ArrayList<>();
    private Color background;
    private transient BufferedImage snapshot;

    public Axes() {
        this(null, View2D.INSTANCE);
    }

    public Axes(Plot plot) {
        this(plot, defaultView(plot));
    }

    public Axes(Plot plot, AxesView view) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        this.view = view;
        setPlot(plot);
    }

    private static AxesView defaultView(Plot plot) {
        if (plot instanceof GLPlot) {
            return View3D.from((GLPlot) plot);
        } else if (plot instanceof Plot3D) {
            return View3D.from((Plot3D) plot);
        }
        return View2D.INSTANCE;
    }

    // <editor-fold desc="Model">
    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
        if (plot != null && (this.view == null || this.view.is3D())) {
            this.view = defaultView(plot);
        }
        syncPlotLayout();
    }

    public AxesView getView() {
        return view;
    }

    public void setView(AxesView view) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        this.view = view;
        if (plot != null) {
            view.applyTo(plot);
        }
    }

    public void view2D() {
        setView(View2D.INSTANCE);
    }

    public void view3D(double azimuth, double elevation) {
        setView(new View3D(azimuth, elevation));
    }

    public boolean is3D() {
        return view != null && view.is3D();
    }

    public boolean isJogl() {
        return plot instanceof GLPlot;
    }

    public GLPlot getGLPlot() {
        return plot instanceof GLPlot ? (GLPlot) plot : null;
    }

    public Plot3D getPlot3D() {
        return plot instanceof Plot3D ? (Plot3D) plot : null;
    }

    // </editor-fold>
    // <editor-fold desc="Layout">
    public Rectangle2D getPosition() {
        return position;
    }

    public void setPosition(Rectangle2D position) {
        this.position = position;
    }

    public void setPosition(double x, double y, double w, double h) {
        this.position = new Rectangle2D.Double(x, y, w, h);
    }

    public Rectangle2D getOuterPosition() {
        return outerPosition;
    }

    public void setOuterPosition(Rectangle2D outerPosition) {
        this.outerPosition = outerPosition;
    }

    public void setOuterPosition(double x, double y, double w, double h) {
        this.outerPosition = new Rectangle2D.Double(x, y, w, h);
    }

    public Rectangle2D getPositionArea() {
        return positionArea;
    }

    public void setPositionArea(Rectangle2D area) {
        this.positionArea = area;
        syncPlotLayout();
    }

    public Rectangle2D getOuterPositionArea() {
        return outerPositionArea;
    }

    public void setOuterPositionArea(Rectangle2D area) {
        this.outerPositionArea = area;
    }

    public Rectangle2D getGraphArea() {
        return graphArea;
    }

    public void setGraphArea(Rectangle2D area) {
        this.graphArea = area;
    }

    public Rectangle2D getPositionArea(Rectangle2D parent) {
        double x = parent.getX() + parent.getWidth() * position.getX();
        double y = parent.getY() + parent.getHeight() * (1.0 - position.getHeight() - position.getY());
        double w = parent.getWidth() * position.getWidth();
        double h = parent.getHeight() * position.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    public Rectangle2D getOuterPositionArea(Rectangle2D parent) {
        double x = parent.getX() + parent.getWidth() * outerPosition.getX();
        double y = parent.getY() + parent.getHeight() * (1.0 - outerPosition.getHeight() - outerPosition.getY());
        double w = parent.getWidth() * outerPosition.getWidth();
        double h = parent.getHeight() * outerPosition.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }

    public Margin getTightInset() {
        return tightInset;
    }

    public void setTightInset(Margin tightInset) {
        this.tightInset = tightInset == null ? new Margin() : tightInset;
    }

    public Margin getTightInset(Graphics2D g, Rectangle2D positionArea) {
        Margin margin = plot == null ? new Margin(2, 2, 2, 2)
                : plot.getTightInset(g, positionArea);
        if (margin == null) {
            margin = new Margin();
        }
        if (title != null) {
            margin.setTop(margin.getTop() + title.getTrueDimension(g).height + 10);
        }
        for (ChartLegend legend : legends) {
            if (legend == null) {
                continue;
            }
            java.awt.Dimension dim = legend.getLegendDimension(g,
                    new java.awt.Dimension((int) positionArea.getWidth(),
                            (int) positionArea.getHeight()));
            switch (legend.getPosition()) {
                case UPPER_CENTER_OUTSIDE:
                    margin.setTop(margin.getTop() + dim.height + 10);
                    break;
                case LOWER_CENTER_OUTSIDE:
                    margin.setBottom(margin.getBottom() + dim.height + 10);
                    break;
                case LEFT_OUTSIDE:
                    margin.setLeft(margin.getLeft() + dim.width + 10);
                    break;
                case RIGHT_OUTSIDE:
                    margin.setRight(margin.getRight() + dim.width + 10);
                    break;
                default:
                    break;
            }
        }
        this.tightInset = margin;
        syncPlotLayout();
        return margin;
    }

    public Margin getPlotShrink() {
        Margin shrink = new Margin();
        if (outerPositionArea == null || positionArea == null) {
            return shrink;
        }
        if (tightInset.getLeft() + outerPositionArea.getX() > positionArea.getX()) {
            shrink.setLeft(tightInset.getLeft() + outerPositionArea.getX() - positionArea.getX());
        }
        if (tightInset.getRight() + positionArea.getX() + positionArea.getWidth()
                > outerPositionArea.getX() + outerPositionArea.getWidth()) {
            shrink.setRight(tightInset.getRight() + positionArea.getX() + positionArea.getWidth()
                    - (outerPositionArea.getX() + outerPositionArea.getWidth()));
        }
        if (tightInset.getTop() + outerPositionArea.getY() > positionArea.getY()) {
            shrink.setTop(tightInset.getTop() + outerPositionArea.getY() - positionArea.getY());
        }
        if (tightInset.getBottom() + positionArea.getY() + positionArea.getHeight()
                > outerPositionArea.getY() + outerPositionArea.getHeight()) {
            shrink.setBottom(tightInset.getBottom() + positionArea.getY() + positionArea.getHeight()
                    - (outerPositionArea.getY() + outerPositionArea.getHeight()));
        }
        return shrink;
    }

    public void setPlotShrink(Margin shrink) {
        if (positionArea == null || shrink == null) {
            return;
        }
        double x = positionArea.getX() + shrink.getLeft();
        double y = positionArea.getY() + shrink.getTop();
        double w = Math.max(1, positionArea.getWidth() - shrink.getLeft() - shrink.getRight());
        double h = Math.max(1, positionArea.getHeight() - shrink.getTop() - shrink.getBottom());
        this.positionArea = new Rectangle2D.Double(x, y, w, h);
        syncPlotLayout();
    }

    public boolean isSubPlot() {
        return subPlot;
    }

    public void setSubPlot(boolean subPlot) {
        this.subPlot = subPlot;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public boolean isOuterPosActive() {
        return outerPosActive;
    }

    public void setOuterPosActive(boolean outerPosActive) {
        this.outerPosActive = outerPosActive;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private void syncPlotLayout() {
        if (plot == null) {
            return;
        }
        plot.setPositionArea(positionArea);
        plot.setOuterPositionArea(outerPositionArea);
        plot.setGraphArea(graphArea);
        plot.setTightInset(tightInset);
    }

    // </editor-fold>
    // <editor-fold desc="Appearance and delegation">
    public ChartText getTitle() {
        return title;
    }

    public void setTitle(ChartText title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : new ChartText(title);
    }

    public List<ChartLegend> getLegends() {
        return legends;
    }

    public ChartLegend getLegend() {
        return legends.isEmpty() ? null : legends.get(0);
    }

    public void setLegend(ChartLegend legend) {
        legends.clear();
        if (legend != null) {
            legends.add(legend);
        }
    }

    public void addLegend(ChartLegend legend) {
        if (legend != null) {
            legends.add(legend);
        }
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

    public boolean isSymbolAntiAlias() {
        return symbolAntiAlias;
    }

    public void setSymbolAntiAlias(boolean symbolAntiAlias) {
        this.symbolAntiAlias = symbolAntiAlias;
    }

    public BufferedImage getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(BufferedImage snapshot) {
        this.snapshot = snapshot;
    }

    public org.meteoinfo.geometry.Extent getExtent() {
        return plot == null ? null : plot.getExtent();
    }

    public void setDrawExtent(org.meteoinfo.geometry.Extent extent) {
        if (plot != null) {
            plot.setDrawExtent(extent);
        }
    }

    public GraphicCollection getGraphics() {
        if (plot instanceof GLPlot) {
            return ((GLPlot) plot).getGraphics();
        } else if (plot instanceof Plot3D) {
            return ((Plot3D) plot).getGraphics();
        } else if (plot instanceof Plot2D) {
            return ((Plot2D) plot).getGraphics();
        }
        return new GraphicCollection();
    }

    public void setAxisTickFont(Font font) {
        if (plot instanceof GLPlot) {
            ((GLPlot) plot).setAxisTickFont(font);
        } else if (plot instanceof Plot3D) {
            ((Plot3D) plot).setAxisTickFont(font);
        }
    }

    public void setAutoExtent() {
        if (plot instanceof GLPlot) {
            ((GLPlot) plot).setAutoExtent();
        } else if (plot instanceof Plot3D) {
            ((Plot3D) plot).setAutoExtent();
        }
    }

    // </editor-fold>
    // <editor-fold desc="Rendering">
    public void draw2D(Graphics2D g, Rectangle2D figureArea) {
        if (!visible) {
            return;
        }
        if (background != null) {
            g.setColor(background);
            g.fill(positionArea);
        }
        if (plot == null) {
            return;
        }
        if (plot instanceof GLPlot) {
            if (snapshot != null) {
                g.drawImage(snapshot, (int) positionArea.getX(), (int) positionArea.getY(),
                        (int) positionArea.getWidth(), (int) positionArea.getHeight(), null);
            } else {
                g.setColor(new Color(245, 245, 245));
                g.fill(positionArea);
                g.setColor(Color.DARK_GRAY);
                g.drawString("3-D axes requires JOGL rendering",
                        (float) positionArea.getX() + 8, (float) positionArea.getCenterY());
            }
            return;
        }
        plot.setPositionArea(positionArea);
        plot.setGraphArea(positionArea);
        plot.setTightInset(tightInset);
        plot.setAntiAlias(antiAlias);
        plot.setSymbolAntiAlias(symbolAntiAlias);
        plot.draw(g, figureArea);
    }

    public void prepare3D(GLAutoDrawable drawable, int width, int height) {
        if (!isJogl()) {
            return;
        }
        if (view instanceof View3D) {
            view.applyTo(plot);
        }
        GLPlot glPlot = (GLPlot) plot;
        glPlot.setPositionArea(positionArea);
        glPlot.setGraphArea(positionArea);
        glPlot.setTightInset(tightInset);
        glPlot.reshape(drawable, 0, 0, width, height);
    }

    public void draw3D(GLAutoDrawable drawable) {
        if (!visible || !isJogl()) {
            return;
        }
        GLPlot glPlot = (GLPlot) plot;
        glPlot.display(drawable);
        if (glPlot.getScreenImage() != null) {
            snapshot = glPlot.getScreenImage();
        }
    }

    // </editor-fold>
    @Override
    public Axes clone() {
        try {
            Axes copy = (Axes) super.clone();
            copy.position = (Rectangle2D) position.clone();
            copy.outerPosition = (Rectangle2D) outerPosition.clone();
            copy.positionArea = (Rectangle2D) positionArea.clone();
            copy.outerPositionArea = (Rectangle2D) outerPositionArea.clone();
            copy.graphArea = (Rectangle2D) graphArea.clone();
            copy.tightInset = new Margin(tightInset.getLeft(), tightInset.getRight(),
                    tightInset.getTop(), tightInset.getBottom());
            copy.legends = new ArrayList<>(legends);
            copy.snapshot = null;
            return copy;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public String toString() {
        return "Axes[" + (is3D() ? "3D" : "2D") + ", "
                + (plot == null ? "none" : plot.getClass().getSimpleName()) + "]";
    }
}
