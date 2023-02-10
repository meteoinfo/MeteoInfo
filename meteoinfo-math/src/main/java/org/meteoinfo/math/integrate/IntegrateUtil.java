package org.meteoinfo.math.integrate;

import org.apache.commons.math4.legacy.ode.ContinuousOutputModel;
import org.apache.commons.math4.legacy.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math4.legacy.ode.FirstOrderIntegrator;
import org.apache.commons.math4.legacy.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math4.legacy.ode.sampling.StepHandler;
import org.apache.commons.math4.legacy.ode.sampling.StepInterpolator;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

import java.io.*;

public class IntegrateUtil {

    /**
     * Integrate a system of ordinary differential equations
     *
     * @param equations Computes the derivative of y at t
     * @param y0 Initial condition on y
     * @param t A sequence of time points for which to solve for y
     * @return Array containing the value of y for each desired time in t, with the initial value y0 in the first row.
     */
    public static Array odeIntegrate(FirstOrderDifferentialEquations equations, Array y0, Array t) throws IOException, ClassNotFoundException {
        y0 = y0.copyIfView();
        t = t.copyIfView();

        int nt = (int) t.getSize();
        int ny0 = (int) y0.getSize();
        FirstOrderIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        double[] y0v = (double[]) y0.getStorage();
        double[] yDot = new double[ny0];

        integrator.addStepHandler(new ContinuousOutputModel());
        integrator.integrate(equations, t.getDouble(0), y0v, t.getDouble(nt - 1), yDot);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        for (StepHandler handler : integrator.getStepHandlers()) {
            oos.writeObject(handler);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

        Array r = Array.factory(DataType.DOUBLE, new int[]{nt, ny0});
        IndexIterator iter = r.getIndexIterator();
        for (int i = 0; i < nt; i++) {
            cm.setInterpolatedTime(t.getDouble(i));
            double[] interpolatedY = cm.getInterpolatedState();
            for (double v : interpolatedY) {
                iter.setDoubleNext(v);
            }
        }

        return r;
    }
}
