package org.meteoinfo.math.integrate;

import org.apache.commons.math4.legacy.ode.ContinuousOutputModel;
import org.apache.commons.math4.legacy.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math4.legacy.ode.FirstOrderIntegrator;
import org.apache.commons.math4.legacy.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math4.legacy.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math4.legacy.ode.sampling.StepHandler;
import org.apache.commons.math4.legacy.ode.sampling.StepInterpolator;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.Index2D;
import org.meteoinfo.ndarray.IndexIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Integrate a system of ordinary differential equations
     *
     * @param equations Computes the derivative of y at t
     * @param y0 Initial condition on y
     * @param t0 Integrate start time
     * @param t Integrate end time
     * @param tEval Times at which to store the computed solution
     * @param minStep Minimal step
     * @param maxStep Maximal step
     * @param rtol Allowed relative error
     * @param atol Allowed absolute error
     * @return Array containing the value of y for each desired time in t, with the initial value y0 in the first row.
     */
    public static Array[] ode45(FirstOrderDifferentialEquations equations, Array y0, double t0, double t,
                              Array tEval, Double minStep, Double maxStep, Double rtol, Double atol) throws IOException, ClassNotFoundException {
        y0 = y0.copyIfView();

        int ny0 = (int) y0.getSize();
        minStep = minStep == null ? 1.0e-6 : minStep;
        maxStep = maxStep == null ? 100.0 : maxStep;
        rtol = rtol == null ? 1.0e-6 : rtol;
        atol = atol == null ? 1.0e-6 : atol;
        FirstOrderIntegrator integrator = new DormandPrince54Integrator(minStep, maxStep, atol, rtol);
        double[] y0v = (double[]) y0.getStorage();
        double[] yDot = new double[ny0];
        List<Double> tlist = new ArrayList<>();
        List<double[]> ylist = new ArrayList<>();

        if (tEval == null) {
            integrator.addStepHandler(new StepHandler() {
                @Override
                public void init(double t0, double[] y0, double t) {
                    tlist.add(t0);
                    ylist.add(y0);
                    //System.out.printf("Start: t0 = %.4f, y0 = %.6f%n", t0, y0[0]);
                }

                @Override
                public void handleStep(StepInterpolator interpolator, boolean isLast) {
                    double t = interpolator.getCurrentTime();
                    tlist.add(t);
                    double[] y = interpolator.getInterpolatedState();
                    ylist.add(y.clone());
                    //System.out.printf("t = %.4f, y = %.6f%n", t, y[0]);
                }
            });

            integrator.integrate(equations, t0, y0v, t, yDot);
        } else {
            ContinuousOutputModel solution = new ContinuousOutputModel();
            integrator.addStepHandler(solution);

            integrator.integrate(equations, t0, y0v, t, yDot);

            for (int i = 0; i < tEval.getSize(); i++) {
                tlist.add(tEval.getDouble(i));
                solution.setInterpolatedTime(tEval.getDouble(i));
                ylist.add(solution.getInterpolatedState());
            }
        }

        int nt = tlist.size();
        Array rt = Array.factory(DataType.DOUBLE, new int[]{nt});
        Array r = Array.factory(DataType.DOUBLE, new int[]{ny0, nt});
        Index2D index = (Index2D) r.getIndex();
        for (int i = 0; i < nt; i++) {
            rt.setDouble(i, tlist.get(i));
            double[] interpolatedY = ylist.get(i);
            index.set1(i);
            for (int j = 0; j < ny0; j++) {
                index.set0(j);
                r.setDouble(index, interpolatedY[j]);
            }
        }

        return new Array[]{rt, r};
    }
}
