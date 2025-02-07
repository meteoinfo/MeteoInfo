package org.meteoinfo.math.stats.regression;

import org.apache.commons.math4.legacy.stat.regression.OLSMultipleLinearRegression;

import java.util.ArrayList;
import java.util.List;

public class StepwiseRegression {
    private static final double F_THRESHOLD = 4.0; // F test threshold, can be adjusted as needed

    public static void main(String[] args) {
        // sample data
        double[][] xData = {
                {1, 2, 3, 4},
                {2, 3, 4, 5},
                {3, 4, 5, 6},
                {4, 5, 6, 7},
                {5, 6, 7, 8}
        };
        double[] yData = {1, 2, 3, 4, 5};

        // stepwise regression
        StepwiseRegression stepwiseRegression = new StepwiseRegression();
        List<Integer> selectedVariables = stepwiseRegression.stepwise(xData, yData);

        System.out.println("Selected Variables: " + selectedVariables);
    }

    public List<Integer> stepwise(double[][] xData, double[] yData) {
        List<Integer> variables = new ArrayList<>();
        for (int i = 0; i < xData[0].length; i++) {
            variables.add(i);
        }

        List<Integer> selectedVariables = new ArrayList<>();
        boolean changed;

        do {
            changed = false;

            // forward selection
            double bestFValue = Double.NEGATIVE_INFINITY;
            int bestVariable = -1;
            for (int var : variables) {
                if (!selectedVariables.contains(var)) {
                    List<Integer> tempVars = new ArrayList<>(selectedVariables);
                    tempVars.add(var);
                    double fValue = calculateFValue(xData, yData, tempVars);
                    if (fValue > bestFValue) {
                        bestFValue = fValue;
                        bestVariable = var;
                    }
                }
            }

            if (bestFValue > F_THRESHOLD) {
                selectedVariables.add(bestVariable);
                changed = true;
            }

            // backward remove
            bestFValue = Double.NEGATIVE_INFINITY;
            bestVariable = -1;
            for (int var : selectedVariables) {
                List<Integer> tempVars = new ArrayList<>(selectedVariables);
                tempVars.remove(Integer.valueOf(var));
                double fValue = calculateFValue(xData, yData, tempVars);
                if (fValue > bestFValue) {
                    bestFValue = fValue;
                    bestVariable = var;
                }
            }

            if (bestFValue > F_THRESHOLD && selectedVariables.size() > 1) {
                selectedVariables.remove(Integer.valueOf(bestVariable));
                changed = true;
            }
        } while (changed);

        return selectedVariables;
    }

    private double calculateFValue(double[][] xData, double[] yData, List<Integer> variables) {
        int n = xData.length;
        int k = variables.size();

        double[][] xMatrix = new double[n][k + 1];
        for (int i = 0; i < n; i++) {
            xMatrix[i][0] = 1; // intercept
            for (int j = 0; j < k; j++) {
                xMatrix[i][j + 1] = xData[i][variables.get(j)];
            }
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(yData, xMatrix);

        double sse = regression.calculateResidualSumOfSquares();
        double sst = calculateSST(yData);

        double fValue = ((sst - sse) / k) / (sse / (n - k - 1));
        return fValue;
    }

    private double calculateSST(double[] yData) {
        double mean = 0;
        for (double y : yData) {
            mean += y;
        }
        mean /= yData.length;

        double sst = 0;
        for (double y : yData) {
            sst += Math.pow(y - mean, 2);
        }
        return sst;
    }
}
