package org.meteoinfo.math.optimize;

import org.apache.commons.math3.analysis.UnivariateFunction;

import java.util.List;

public class ParamUnivariateFunction implements UnivariateFunction {
    double[] parameters;

    /**
     * Get parameters
     * @return Parameters
     */
    public double[] getParameters() {
        return this.parameters;
    }

    /**
     * Set parameters
     * @param value Parameters
     */
    public void setParameters(double[] value) {
        this.parameters = value;
    }

    /**
     * Set parameters
     * @param value Parameters
     */
    public void setParameters(List<Double> value) {
        this.parameters = new double[value.size()];
        for (int i = 0; i < this.parameters.length; i++) {
            this.parameters[i] = value.get(i);
        }
    }

    /**
     * Value
     * @param x X
     * @return Y
     */
    public double value(double x) {
        return value(x, this.parameters);
    }

    /**
     * Value
     * @param x X
     * @param parameters Parameters
     * @return Y
     */
    public double value(double x, double... parameters) {
        return 0;
    }
}
