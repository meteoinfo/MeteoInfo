import org.meteoinfo.chart.GLChart;
import org.meteoinfo.chart.GLChartPanel;
import org.meteoinfo.chart.MouseMode;
import org.meteoinfo.geometry.graphic.GraphicCollection3D;
import org.meteoinfo.chart.graphic.GraphicFactory;
import org.meteoinfo.chart.jogl.GLPlot;
import org.meteoinfo.chart.jogl.Lighting;
import org.meteoinfo.common.Extent3D;
import org.meteoinfo.geometry.legend.PointBreak;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.math.ArrayMath;
import org.meteoinfo.ndarray.math.ArrayUtil;

import javax.swing.*;
import java.awt.*;

public class SphereTest1 {

    GraphicCollection3D createSpheres() {
        int[] shape = new int[]{1};
        Array z = ArrayUtil.lineSpace(0., 1., 10, true);
        Array x = ArrayMath.mul(z, ArrayMath.sin(ArrayMath.mul(z, 20)));
        Array y = ArrayMath.mul(z, ArrayMath.sin(ArrayMath.mul(z, 20)));
        PointBreak cb = new PointBreak();
        cb.setSize(50);
        cb.setColor(Color.RED);

        GraphicCollection3D graphics = (GraphicCollection3D) GraphicFactory.createPoints3D(x, y, z, cb);
        graphics.setSphere(true);

        return graphics;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Volume Test");
        GLPlot plot = new GLPlot();
        plot.setOrthographic(true);
        plot.setClipPlane(false);
        //plot.setBackground(Color.black);
        //plot.setForeground(Color.blue);
        plot.setDrawBoundingBox(true);
        plot.setDrawBase(true);
        plot.setBoxed(true);
        plot.setDisplayXY(true);
        //plot.getXAxis().setDrawTickLabel(false);
        //plot.getYAxis().setDrawTickLabel(false);
        plot.setDisplayZ(true);
        plot.getZAxis().setLabel("Z axis");
        //plot.getGridLine().setDrawXLine(false);
        //plot.getGridLine().setDrawYLine(false);
        //plot.getGridLine().setDrawZLine(false);

        SphereTest1 test = new SphereTest1();
        GraphicCollection3D graphics = test.createSpheres();
        plot.addGraphic(graphics);
        plot.setDrawExtent(new Extent3D(-2, 2, -2, 2, -2, 2));
        plot.setAntialias(true);
        Lighting lighting = plot.getLighting();
        lighting.setEnable(true);
        lighting.setMaterialSpecular(1.0f);
        GLChartPanel canvas = new GLChartPanel(new GLChart());
        canvas.getChart().addPlot(plot);
        canvas.setMouseMode(MouseMode.ROTATE);
        frame.getContentPane().add(canvas);
        frame.pack();
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
