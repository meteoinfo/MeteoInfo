
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ODESolver {

    private IntegrateMethod method;
    private FirstOrderIntegrator integrator;
    private FirstOrderDifferentialEquations equations;
    private Array y0;
    private double t0;
    private double tf;
    private Array tEval;
    private double minStep = 1.0e-6;
    private double maxStep = 100.0;
    private double rTol = 1.0e-6;
    private double aTol = 1.0e-6;
    private boolean denseOutput = false;
    private ContinuousOutputModel denseOutputModel;
    private Array tResult;
    private Array yResult;

    /**
     * Constructor
     * @param method Integration method
     * @param equations Differential equations
     * @param t0 Integration start time
     * @param tf Integration end time
     * @param y0 Initial state
     */
    public ODESolver(String method, FirstOrderDifferentialEquations equations, double t0, double tf,
                     Array y0) {
        this.method = IntegrateMethod.valueOf(method.toUpperCase());
        this.equations = equations;
        this.t0 = t0;
        this.tf = tf;
        this.y0 = y0.copyIfView();
    }

    /**
     * Set times at which to store the computed solution
     * @param value The value
     */
    public void setTEval(Array value) {
        this.tEval = value;
    }

    /**
     * Set first step size
     * @param value First step size
     */
    public void setMinStep(double value) {
        this.minStep = value;
    }

    /**
     * Set maximum allowed step size
     * @param value Maximum allowed step size
     */
    public void setMaxStep(double value) {
        this.maxStep = value;
    }

    /**
     * Set relative tolerance
     * @param value Relative tolerance
     */
    public void setRTol(double value) {
        this.rTol = value;
    }

    /**
     * Set absolute tolerance
     * @param value Absolute tolerance
     */
    public void setATol(double value) {
        this.aTol = value;
    }

    /**
     * Set dense output or not
     * @param value Dense output or not
     */
    public void setDenseOutput(boolean value) {
        this.denseOutput = value;
    }

    /**
     * Get time result
     * @return Time result
     */
    public Array getTResult() {
        return this.tResult;
    }

    /**
     * Get y result
     * @return Y result
     */
    public Array getYResult() {
        return this.yResult;
    }

    /**
     * Solve function
     * @throws NoSuchMethodException
     */
    public void solve() throws NoSuchMethodException {
        switch (this.method) {
            case RK45:
                this.integrator = new DormandPrince54Integrator(minStep, maxStep, aTol, rTol);
                break;
            case DOP853:
                this.integrator = new DormandPrince853Integrator(minStep, maxStep, aTol, rTol);
                break;
            default:
                throw new NoSuchMethodException("No such");
        }

        int ny0 = (int) y0.getSize();
        double[] y0v = (double[]) y0.getStorage();
        double[] yDot = new double[ny0];
        List<Double> tlist = new ArrayList<>();
        List<double[]> ylist = new ArrayList<>();

        if (this.denseOutput) {
            denseOutputModel = new ContinuousOutputModel();
            integrator.addStepHandler(denseOutputModel);

            integrator.integrate(equations, t0, y0v, tf, yDot);
        } else {
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

                integrator.integrate(equations, t0, y0v, tf, yDot);
            } else {
                ContinuousOutputModel denseOutputModel = new ContinuousOutputModel();
                integrator.addStepHandler(denseOutputModel);

                integrator.integrate(equations, t0, y0v, tf, yDot);

                for (int i = 0; i < tEval.getSize(); i++) {
                    double t = tEval.getDouble(i);
                    tlist.add(t);
                    denseOutputModel.setInterpolatedTime(t);
                    double[] v = denseOutputModel.getInterpolatedState();
                    ylist.add(v.clone());
                }
            }

            int nt = tlist.size();
            tResult = Array.factory(DataType.DOUBLE, new int[]{nt});
            yResult = Array.factory(DataType.DOUBLE, new int[]{ny0, nt});
            Index2D index = (Index2D) yResult.getIndex();
            for (int i = 0; i < nt; i++) {
                tResult.setDouble(i, tlist.get(i));
                double[] interpolatedY = ylist.get(i);
                index.set1(i);
                for (int j = 0; j < ny0; j++) {
                    index.set0(j);
                    yResult.setDouble(index, interpolatedY[j]);
                }
            }
        }
    }

    /**
     * Solve by time steps - only valid with dense output
     * @param t Time steps
     * @return Values of the solution at time steps
     */
    public Array solve(Array t) {
        t = t.copyIfView();
        int ny0 = (int) y0.getSize();
        int nt = (int) t.getSize();
        Array y = Array.factory(DataType.DOUBLE, new int[]{ny0, nt});
        Index2D index = (Index2D) y.getIndex();
        for (int i = 0; i < t.getSize(); i++) {
            denseOutputModel.setInterpolatedTime(t.getDouble(i));
            double[] v = denseOutputModel.getInterpolatedState();
            index.set1(i);
            for (int j = 0; j < ny0; j++) {
                index.set0(j);
                y.setDouble(index, v[j]);
            }
        }

        return y;
    }

}
