package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL2;
import org.meteoinfo.chart.graphic.GraphicCollection3D;

public class PointRender extends JOGLGraphicRender {

    private GraphicCollection3D pointGraphics;

    /**
     * Constructor
     *
     * @param gl The JOGL GL2 object
     */
    public PointRender(GL2 gl) {
        super(gl);
    }

    /**
     * Constructor
     *
     * @param gl The opengl pipeline
     * @param pointGraphics 3D point graphics
     */
    public PointRender(GL2 gl, GraphicCollection3D pointGraphics) {
        super(gl);
        this.pointGraphics = pointGraphics;
    }

    @Override
    public void draw() {

    }
}
