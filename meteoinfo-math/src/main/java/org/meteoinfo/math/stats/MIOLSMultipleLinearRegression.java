package org.meteoinfo.math.stats;

import org.apache.commons.math4.legacy.stat.regression.OLSMultipleLinearRegression;

public class MIOLSMultipleLinearRegression extends OLSMultipleLinearRegression {
    /**
     * Loads model x and y sample data, overriding any previous sample.
     *
     * Computes and caches QR decomposition of the X matrix.
     * @param y the [n,1] array representing the y sample
     * @param x the [n,k] array representing the x sample
     * @throws MathIllegalArgumentException if the x and y array data are not
     *             compatible for the regression
     */
    @Override
    public void newSampleData(double[] y, double[][] x) {
        //validateSampleData(x, y);
        newYSampleData(y);
        newXSampleData(x);
    }
}
