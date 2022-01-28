package org.meteoinfo.chart.render.jogl;

import com.jogamp.opengl.GL2;
import org.meteoinfo.chart.render.Render;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.graphic.GraphicCollection;

public abstract class JOGLRender extends Render {
    protected GL2 gl;

    /**
     * Constructor
     *
     * @param graphics Graphics
     */
    public JOGLRender(GraphicCollection graphics) {
        super(graphics);
    }

    /**
     * Constructor
     * @param graphics Graphics
     * @param gl The JOGL GL2 object
     */
    public JOGLRender(GraphicCollection graphics, GL2 gl) {
        super(graphics);
        this.gl = gl;
    }
}
