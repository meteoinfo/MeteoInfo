/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 *
 * @author Yaqiang Wang
 */
public class Statistics {

    /**
     * Count the data value bigger then a threshold value
     *
     * @param aDataList The data list
     * @param value Threshold value
     * @return Count
     */
    public static int valueCount(List<Double> aDataList, double value) {
        int count = 0;
        for (Double v : aDataList) {
            if (v >= value) {
                count += 1;
            }
        }

        return count;
    }

    /**
     * Sum funtion
     *
     * @param aDataList The data list
     * @return Sum
     */
    public static double sum(List<Double> aDataList) {
        double aSum = 0.0;

        for (Double v : aDataList) {
            aSum = aSum + v;
        }

        return aSum;
    }

    /**
     * Mean funtion
     *
     * @param aDataList The data list
     * @return Mean
     */
    public static double mean(List<Double> aDataList) {
        double aSum = 0.0;

        for (Double v : aDataList) {
            aSum = aSum + v;
        }

        return aSum / aDataList.size();
    }

    /**
     * Maximum function
     *
     * @param aDataList The data list
     * @return Maximum
     */
    public static double maximum(List<Double> aDataList) {
        double aMax;

        aMax = aDataList.get(0);
        for (int i = 1; i < aDataList.size(); i++) {
            aMax = Math.max(aMax, aDataList.get(i));
        }

        return aMax;
    }

    /**
     * Minimum function
     *
     * @param aDataList The data list
     * @return Minimum
     */
    public static double minimum(List<Double> aDataList) {
        double aMin;

        aMin = aDataList.get(0);
        for (int i = 1; i < aDataList.size(); i++) {
            aMin = Math.min(aMin, aDataList.get(i));
        }

        return aMin;
    }

    /**
     * Median funtion
     *
     * @param aDataList The data list
     * @return Median
     */
    public static double median(List<Double> aDataList) {
        Collections.sort(aDataList);
        if (aDataList.size() % 2 == 0) {
            return (aDataList.get(aDataList.size() / 2) + aDataList.get(aDataList.size() / 2 - 1)) / 2.0;
        } else {
            return aDataList.get(aDataList.size() / 2);
        }
    }

    /**
     * Quantile function
     *
     * @param aDataList The data list
     * @param aNum Quantile index
     * @return Quantile value
     */
    public static double quantile(List<Double> aDataList, int aNum) {
        Collections.sort(aDataList);
        double aData = 0;
        switch (aNum) {
            case 0:
                aData = minimum(aDataList);
                break;
            case 1:
                if ((aDataList.size() + 1) % 4 == 0) {
                    aData = aDataList.get((aDataList.size() + 1) / 4 - 1);
                } else {
                    aData = aDataList.get((aDataList.size() + 1) / 4 - 1) + 0.75 * (aDataList.get((aDataList.size() + 1) / 4)
                            - aDataList.get((aDataList.size() + 1) / 4 - 1));
                }
                break;
            case 2:
                aData = median(aDataList);
                break;
            case 3:
                if ((aDataList.size() + 1) % 4 == 0) {
                    aData = aDataList.get((aDataList.size() + 1) * 3 / 4 - 1);
                } else {
                    aData = aDataList.get((aDataList.size() + 1) * 3 / 4 - 1) + 0.25 * (aDataList.get((aDataList.size() + 1) * 3 / 4)
                            - aDataList.get((aDataList.size() + 1) * 3 / 4 - 1));
                }
                break;
            case 4:
                aData = maximum(aDataList);
                break;
        }

        return aData;
    }

    /**
     * Quantile function
     *
     * @param a The data array
     * @param aNum Quantile index
     * @return Quantile value
     */
    public static double quantile(Array a, int aNum) {
        List<Double> dlist = new ArrayList<>();
        IndexIterator ii = a.getIndexIterator();
        double v;
        while (ii.hasNext()) {
            v = ii.getDoubleNext();
            if (!Double.isNaN(v))
                dlist.add(v);
        }
        if (dlist.size() <= 3)
            return Double.NaN;
        else
            return quantile(dlist, aNum);
    }

    /**
     * Quantile function
     *
     * @param aDataList The data list
     * @param qValue Quantile index
     * @return Quantile value
     */
    public static double quantile(List<Double> aDataList, double qValue) {
        Collections.sort(aDataList);
        double aData;

        if (qValue == 0) {
            aData = minimum(aDataList);
        } else if (qValue == 1) {
            aData = maximum(aDataList);
        } else {
            aData = aDataList.get((int) (aDataList.size() * qValue) - 1);
        }

        return aData;
    }

    /**
     * Standard deviation
     *
     * @param aDataList The data list
     * @return Standard deviation value
     */
    public static double standardDeviation(List<Double> aDataList) {
        double theMean, theSqDev, theSumSqDev, theVariance, theStdDev, theValue;
        int i;

        theMean = mean(aDataList);
        theSumSqDev = 0;
        for (i = 0; i < aDataList.size(); i++) {
            theValue = aDataList.get(i);
            theSqDev = (theValue - theMean) * (theValue - theMean);
            theSumSqDev = theSqDev + theSumSqDev;
        }

        if (aDataList.size() > 1) {
            theVariance = theSumSqDev / (aDataList.size() - 1);
            theStdDev = Math.sqrt(theVariance);
        } else {
            //theVariance = 0;
            theStdDev = 0;
        }

        return theStdDev;
    }
}
