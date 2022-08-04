package org.meteoinfo.math.optimize;

import org.apache.commons.math4.legacy.analysis.UnivariateFunction;
import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.IndexIterator;

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
    @Override
    public double value(double x) {
        return value(x, this.parameters);
    }

    public RealVector value(Array x) {
        RealVector r = new ArrayRealVector((int) x.getSize());
        IndexIterator iter = x.getIndexIterator();
        int i = 0;
        while(iter.hasNext()) {
            r.setEntry(i, value(iter.getDoubleNext()));
            i += 1;
        }

        return r;
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
