package org.meteoinfo.math.integrate;

import org.apache.commons.math4.legacy.exception.DimensionMismatchException;
import org.apache.commons.math4.legacy.exception.MaxCountExceededException;
import org.apache.commons.math4.legacy.ode.FirstOrderDifferentialEquations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ODEEquations implements FirstOrderDifferentialEquations {

    private List<Double> parameters = new ArrayList<>();
    private int dimension;

    /**
     * Get parameters
     * @return Parameters
     */
    public List<Double> getParameters() {
        return this.parameters;
    }

    /**
     * Set parameters
     * @param value Parameters
     */
    public void setParameters(List<Double> value) {
        this.parameters = value;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    public void setDimension(int value) {
        this.dimension = value;
    }

    @Override
    public void computeDerivatives(double v, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
        List<Double> yd = doComputeDerivatives(Arrays.stream(y).boxed().collect(Collectors.toList()), v);
        int n = yd.size();
        for (int i = 0; i < n; i++) {
            yDot[i] = yd.get(i);
        }
    }

    public List<Double> doComputeDerivatives(List<Double> y, double t) {
        return Arrays.asList(0.);
    }
}
