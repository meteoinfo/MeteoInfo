package org.meteoinfo.chart;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import org.meteoinfo.chart.io.FigureExporter;
import org.meteoinfo.chart.plot.Plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A MATLAB-like figure canvas containing one or more {@link Axes}.
 *
 * <p>The figure owns layout and decorations.  Java2D paints the 2-D content and
 * JOGL paints axes backed by {@link org.meteoinfo.chart.jogl.GLPlot}.  The two
 * can be mixed in one figure.</p>
 *
 * @author MeteoInfo
 */
public class Figure implements GLEventListener {

    private final List<Axes> axesList = new ArrayList<>();
    private int currentAxes = -1;

    private int width;
    private int height;
    private int rowNum = 1;
    private int columnNum = 1;

    private ChartText title;
    private ChartText subTitle;
    private ChartText xLabel;
    private ChartText yLabel;
    private final List<ChartText> texts = new ArrayList<>();
    private ChartLegend legend;

    private Color background = Color.white;
    private boolean drawLegend;
    private Rectangle2D plotArea;

    private boolean antiAlias;
    private boolean symbolAntialias = true;
    private boolean sampleBuffers = true;
    private boolean doScreenShot;
    private BufferedImage screenImage;

    private IChartPanel parent;
    private boolean java2D = true;

    public Figure() {
    }

    public Figure(IChartPanel parent) {
        this.parent = parent;
    }

    public Figure(Axes axes) {
        addAxes(axes);
    }

    public Figure(Plot plot) {
        addPlot(plot);
    }

    public Figure(String title, Plot plot) {
        this(plot);
        setTitle(title);
    }

    public Figure(Axes axes, IChartPanel parent) {
        this(parent);
        addAxes(axes);
    }

    public Figure(String title, Axes axes) {
        this(axes);
        setTitle(title);
    }

    // <editor-fold desc="Axes management">
    public List<Axes> getAxesList() {
        return axesList;
    }

    public Axes getCurrentAxes() {
        if (axesList.isEmpty()) {
            return null;
        }
        if (currentAxes < 0 || currentAxes >= axesList.size()) {
            currentAxes = axesList.size() - 1;
        }
        return axesList.get(currentAxes);
    }

    public void setCurrentAxes(Axes axes) {
        if (axes == null) {
            return;
        }
        int index = axesList.indexOf(axes);
        if (index >= 0) {
            currentAxes = index;
        }
    }

    public void setCurrentAxes(int index) {
        if (index >= -1 && index < axesList.size()) {
            currentAxes = index;
        }
    }

    public int getCurrentAxesIndex() {
        return currentAxes;
    }

    public Axes getAxes() {
        return axesList.isEmpty() ? null : axesList.get(0);
    }

    public Axes getAxes(int axesIdx) {
        for (Axes axes : axesList) {
            int index = axes.getRowIndex() * columnNum + axes.getColumnIndex() + 1;
            if (index == axesIdx) {
                return axes;
            }
        }
        if (axesIdx > 0 && axesIdx <= axesList.size()) {
            return axesList.get(axesIdx - 1);
        }
        return null;
    }

    public void addAxes(Axes axes) {
        if (axes == null) {
            return;
        }
        axesList.add(axes);
        if (parent != null && axes.getPlot() instanceof org.meteoinfo.chart.geo.MapPlot) {
            ((org.meteoinfo.chart.geo.MapPlot) axes.getPlot()).setParent(parent);
        }
        if (currentAxes < 0) {
            currentAxes = 0;
        }
    }

    public boolean removeAxes(Axes axes) {
        boolean removed = axesList.remove(axes);
        if (removed && currentAxes >= axesList.size()) {
            currentAxes = axesList.size() - 1;
        }
        return removed;
    }

    public void clearAxes() {
        axesList.clear();
        currentAxes = -1;
    }

    public int getAxesCount() {
        return axesList.size();
    }

    public boolean hasJOGLAxes() {
        for (Axes axes : axesList) {
            if (axes.isJogl()) {
                return true;
            }
        }
        return false;
    }

    public boolean has3DAxes() {
        for (Axes axes : axesList) {
            if (axes.is3D()) {
                return true;
            }
        }
        return false;
    }

    public Axes findAxes(int x, int y) {
        for (int i = axesList.size() - 1; i >= 0; i--) {
            Axes axes = axesList.get(i);
            if (axes.isVisible() && axes.getPositionArea().contains(x, y)) {
                return axes;
            }
        }
        return null;
    }
    // </editor-fold>

    // <editor-fold desc="Legacy plot API">
    public List<Plot> getPlots() {
        List<Plot> plots = new ArrayList<>();
        for (Axes axes : axesList) {
            if (axes.getPlot() != null) {
                plots.add(axes.getPlot());
            }
        }
        return plots;
    }

    public void addPlot(Plot plot) {
        addAxes(new Axes(plot));
    }

    public void setPlot(Plot plot) {
        clearAxes();
        addPlot(plot);
    }

    public Plot getPlot() {
        Axes axes = getAxes();
        return axes == null ? null : axes.getPlot();
    }

    public Plot getCurrentPlot() {
        Axes axes = getCurrentAxes();
        return axes == null ? null : axes.getPlot();
    }

    public void setCurrentPlot(Plot plot) {
        for (int i = 0; i < axesList.size(); i++) {
            if (axesList.get(i).getPlot() == plot) {
                currentAxes = i;
                return;
            }
        }
    }

    public void setCurrentPlot(int index) {
        setCurrentAxes(index);
    }

    public void removePlot(Plot plot) {
        for (int i = axesList.size() - 1; i >= 0; i--) {
            if (axesList.get(i).getPlot() == plot) {
                removeAxes(axesList.get(i));
                return;
            }
        }
    }

    public int getPlotIndex(Plot plot) {
        for (int i = 0; i < axesList.size(); i++) {
            if (axesList.get(i).getPlot() == plot) {
                return i;
            }
        }
        return -1;
    }

    public void clearPlots() {
        clearAxes();
    }
    // </editor-fold>

    // <editor-fold desc="Properties">
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = Math.max(1, rowNum);
    }

    public int getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(int columnNum) {
        this.columnNum = Math.max(1, columnNum);
    }

    public ChartText getTitle() {
        return title;
    }

    public void setTitle(ChartText title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : new ChartText(title);
    }

    public ChartText getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(ChartText subTitle) {
        this.subTitle = subTitle;
    }

    public ChartText getXLabel() {
        return xLabel;
    }

    public void setXLabel(ChartText xLabel) {
        this.xLabel = xLabel;
    }

    public void setXLabel(String xLabel) {
        this.xLabel = xLabel == null ? null : new ChartText(xLabel);
    }

    public ChartText getYLabel() {
        return yLabel;
    }

    public void setYLabel(ChartText yLabel) {
        this.yLabel = yLabel;
    }

    public void setYLabel(String yLabel) {
        this.yLabel = yLabel == null ? null : new ChartText(yLabel);
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public ChartLegend getLegend() {
        return legend;
    }

    public void setLegend(ChartLegend legend) {
        this.legend = legend;
        this.drawLegend = legend != null;
    }

    public boolean isDrawLegend() {
        return drawLegend;
    }

    public void setDrawLegend(boolean drawLegend) {
        this.drawLegend = drawLegend;
    }

    public Rectangle2D getPlotArea() {
        return plotArea;
    }

    public boolean isAntialias() {
        return antiAlias;
    }

    public void setAntialias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

    public boolean isSymbolAntialias() {
        return symbolAntialias;
    }

    public void setSymbolAntialias(boolean symbolAntialias) {
        this.symbolAntialias = symbolAntialias;
    }

    public boolean isDoScreenShot() {
        return doScreenShot;
    }

    public void setDoScreenShot(boolean doScreenShot) {
        this.doScreenShot = doScreenShot;
    }

    public BufferedImage getScreenImage() {
        return screenImage;
    }

    public void setScreenImage(BufferedImage screenImage) {
        this.screenImage = screenImage;
    }

    public IChartPanel getParent() {
        return parent;
    }

    public void setParent(IChartPanel parent) {
        this.parent = parent;
        for (Axes axes : axesList) {
            if (axes.getPlot() instanceof org.meteoinfo.chart.geo.MapPlot) {
                ((org.meteoinfo.chart.geo.MapPlot) axes.getPlot()).setParent(parent);
            }
        }
    }

    public void addText(ChartText text) {
        if (text != null) {
            texts.add(text);
        }
    }

    public void clearTexts() {
        texts.clear();
    }

    public void clearAll() {
        title = null;
        subTitle = null;
        xLabel = null;
        yLabel = null;
        legend = null;
        drawLegend = false;
        texts.clear();
        clearAxes();
    }

    public void useJava2DRenderer() {
        this.java2D = true;
    }

    public void useJOGLRenderer() {
        this.java2D = false;
    }

    public boolean isJava2D() {
        return java2D;
    }
    // </editor-fold>

    // <editor-fold desc="Layout and rendering">
    public Rectangle2D getPlotArea(Graphics2D g, Rectangle2D area) {
        double top = 0;
        double left = 0;
        double bottom = 0;
        double right = 0;
        if (title != null) {
            top += title.getTrueDimension(g).height + 12;
        }
        if (subTitle != null) {
            top += subTitle.getTrueDimension(g).height + 6;
        }
        if (xLabel != null) {
            bottom += xLabel.getTrueDimension(g).height + 10;
        }
        if (yLabel != null) {
            left += yLabel.getTrueDimension(g).width + 10;
        }
        return new Rectangle2D.Double(area.getX() + left, area.getY() + top,
                Math.max(1, area.getWidth() - left - right),
                Math.max(1, area.getHeight() - top - bottom));
    }

    public void layout(Graphics2D g, Rectangle2D area) {
        width = (int) Math.round(area.getWidth());
        height = (int) Math.round(area.getHeight());
        plotArea = getPlotArea(g, area);
        List<Rectangle2D> baseAreas = new ArrayList<>();
        for (Axes axes : axesList) {
            Rectangle2D outer;
            if (axes.isSubPlot()) {
                double rowHeight = plotArea.getHeight() / rowNum;
                double colWidth = plotArea.getWidth() / columnNum;
                double x = plotArea.getX() + axes.getColumnIndex() * colWidth;
                double y = plotArea.getY() + axes.getRowIndex() * rowHeight;
                outer = new Rectangle2D.Double(x, y, colWidth, rowHeight);
                axes.setPosition((x - plotArea.getX()) / plotArea.getWidth(),
                        1.0 - ((y - plotArea.getY()) + rowHeight) / plotArea.getHeight(),
                        colWidth / plotArea.getWidth(), rowHeight / plotArea.getHeight());
            } else {
                outer = axes.getOuterPositionArea(plotArea);
            }
            axes.setOuterPositionArea(outer);
            Rectangle2D position = axes.getPositionArea(outer);
            axes.setPositionArea(position);
            axes.setGraphArea(position);
            axes.getTightInset(g, position);
            baseAreas.add(position);
            syncPlotPosition(axes, area);
        }
        for (int i = 0; i < axesList.size(); i++) {
            Axes axes = axesList.get(i);
            if (!axes.isOuterPosActive()) {
                continue;
            }
            axes.setPositionArea(baseAreas.get(i));
            axes.setPlotShrink(axes.getPlotShrink());
            axes.setGraphArea(axes.getPositionArea());
            syncPlotPosition(axes, area);
        }
    }

    private void syncPlotPosition(Axes axes, Rectangle2D area) {
        Plot plot = axes.getPlot();
        if (plot == null) {
            return;
        }
        Rectangle2D rect = axes.getPositionArea();
        plot.setPosition(new Rectangle2D.Double(
                (rect.getX() - area.getX()) / area.getWidth(),
                1.0 - ((rect.getY() - area.getY()) + rect.getHeight()) / area.getHeight(),
                rect.getWidth() / area.getWidth(), rect.getHeight() / area.getHeight()));
        plot.setOuterPositionArea(axes.getOuterPositionArea());
        plot.setPositionArea(rect);
        plot.setGraphArea(rect);
        plot.setTightInset(axes.getTightInset());
    }

    public void draw(Graphics2D g, Rectangle2D area) {
        configure(g);
        java.awt.geom.AffineTransform oldTransform = g.getTransform();
        java.awt.Rectangle oldClip = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());
        Rectangle2D local = new Rectangle2D.Double(0, 0,
                Math.max(1, area.getWidth()), Math.max(1, area.getHeight()));
        try {
            layout(g, local);
            if (background != null) {
                g.setColor(background);
                g.fill(local);
            }
            drawDecorations(g, local, false);
        } finally {
            g.setTransform(oldTransform);
            g.setClip(oldClip);
        }
    }

    /**
     * Draw only the Java2D part, used as an overlay on a JOGL canvas.
     *
     * @param g graphics
     * @param area area
     */
    public void drawOverlay(Graphics2D g, Rectangle2D area) {
        configure(g);
        java.awt.geom.AffineTransform oldTransform = g.getTransform();
        java.awt.Rectangle oldClip = g.getClipBounds();
        g.setClip(area);
        g.translate(area.getX(), area.getY());
        Rectangle2D local = new Rectangle2D.Double(0, 0,
                Math.max(1, area.getWidth()), Math.max(1, area.getHeight()));
        try {
            layout(g, local);
            drawDecorations(g, local, true);
        } finally {
            g.setTransform(oldTransform);
            g.setClip(oldClip);
        }
    }

    private void drawDecorations(Graphics2D g, Rectangle2D area, boolean skipJogl) {
        float y = 5;
        if (title != null) {
            y += title.getDimension(g).height;
            title.draw(g, (float) area.getWidth() / 2, y);
            y += 5;
        }
        if (subTitle != null) {
            y += subTitle.getDimension(g).height;
            subTitle.draw(g, (float) area.getWidth() / 2, y);
        }
        for (Axes axes : axesList) {
            if (!axes.isVisible()) {
                continue;
            }
            if (skipJogl && axes.isJogl()) {
                continue;
            }
            axes.setAntiAlias(antiAlias);
            axes.setSymbolAntiAlias(symbolAntialias);
            axes.draw2D(g, area);
            if (axes.getTitle() != null) {
                Rectangle2D rect = axes.getPositionArea();
                axes.getTitle().draw(g, (float) rect.getCenterX(), (float) (rect.getY() - 3));
            }
        }
        if (xLabel != null && plotArea != null) {
            xLabel.draw(g, (float) (plotArea.getX() + plotArea.getWidth() / 2),
                    (float) (plotArea.getMaxY() + 5));
        }
        if (yLabel != null && plotArea != null) {
            yLabel.draw(g, (float) (plotArea.getX() - 5),
                    (float) (plotArea.getY() + plotArea.getHeight() / 2));
        }
        for (ChartText text : texts) {
            text.draw(g, (float) (area.getWidth() * text.getX()),
                    (float) (area.getHeight() * (1 - text.getY())));
        }
        if (drawLegend && legend != null) {
            java.awt.Dimension dim = legend.getLegendDimension(g,
                    new java.awt.Dimension((int) area.getWidth(), (int) area.getHeight()));
            float lx = (float) area.getWidth() / 2 - dim.width / 2;
            float ly = (float) area.getHeight() - dim.height - 5;
            legend.draw(g, new org.meteoinfo.common.PointF(lx, ly));
        }
    }

    private void configure(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    public Rectangle2D getGraphArea() {
        return axesList.isEmpty() ? new Rectangle2D.Double() : axesList.get(0).getGraphArea();
    }

    public BufferedImage toImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            draw(g, new Rectangle2D.Double(0, 0, width, height));
        } finally {
            g.dispose();
        }
        return image;
    }

    public void saveImage(String fileName, int width, int height) throws IOException {
        FigureExporter.export(this, fileName, width, height);
    }

    public void saveImage(String fileName) throws IOException {
        saveImage(fileName, width > 0 ? width : 800, height > 0 ? height : 600);
    }

    public void export(String fileName, int width, int height) throws IOException {
        saveImage(fileName, width, height);
    }
    // </editor-fold>

    // <editor-fold desc="GLEventListener">
    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.getGL().setSwapInterval(1);
        drawable.getGL().getGL2().glEnable(com.jogamp.opengl.GL2.GL_DEPTH_TEST);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        int w = Math.max(1, drawable.getSurfaceWidth());
        int h = Math.max(1, drawable.getSurfaceHeight());
        com.jogamp.opengl.GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, w, h);
        if (background != null) {
            float[] rgba = background.getRGBComponents(null);
            gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        gl.glClear(com.jogamp.opengl.GL2.GL_COLOR_BUFFER_BIT
                | com.jogamp.opengl.GL2.GL_DEPTH_BUFFER_BIT);
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = measure.createGraphics();
        try {
            layout(mg, new Rectangle2D.Double(0, 0, w, h));
        } finally {
            mg.dispose();
        }
        gl.glEnable(com.jogamp.opengl.GL2.GL_SCISSOR_TEST);
        for (Axes axes : axesList) {
            if (!axes.isVisible() || !axes.isJogl()) {
                continue;
            }
            Rectangle2D rect = axes.getPositionArea();
            int vx = (int) Math.round(rect.getX());
            int vy = (int) Math.round(h - rect.getY() - rect.getHeight());
            int vw = Math.max(1, (int) Math.round(rect.getWidth()));
            int vh = Math.max(1, (int) Math.round(rect.getHeight()));
            gl.glScissor(vx, vy, vw, vh);
            gl.glViewport(vx, vy, vw, vh);
            axes.prepare3D(drawable, w, h);
            axes.draw3D(drawable);
        }
        gl.glDisable(com.jogamp.opengl.GL2.GL_SCISSOR_TEST);
        if (doScreenShot) {
            com.jogamp.opengl.util.awt.AWTGLReadBufferUtil util =
                    new com.jogamp.opengl.util.awt.AWTGLReadBufferUtil(drawable.getGLProfile(), false);
            screenImage = util.readPixelsToBufferedImage(drawable.getGL(), true);
            doScreenShot = false;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        this.width = w;
        this.height = h;
        drawable.getGL().getGL2().glViewport(0, 0, Math.max(1, w), Math.max(1, h));
    }
    // </editor-fold>
}
