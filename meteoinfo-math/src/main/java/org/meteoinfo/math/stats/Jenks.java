package org.meteoinfo.math.stats;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The Jenks optimization method, also called the Jenks natural breaks
 * classification method, is a data classification method designed to determine
 * the best arrangement of values into different classes. This is done by
 * seeking to minimize each class’s average deviation from the class mean, while
 * maximizing each class’s deviation from the means of the other groups. In
 * other words, the method seeks to reduce the variance within classes and
 * maximize the variance between classes.
 */
public class Jenks {

    private LinkedList<Double> list = Lists.newLinkedList();

    public  void addValue(double value) {
        list.add(value);
    }

    public  void addValues(double... values) {
        for (double value : values) {
            addValue(value);
        }
    }

    /**
     * Add values
     * @param values Values array
     */
    public void addValues(Array values) {
        IndexIterator vIter = values.getIndexIterator();
        while (vIter.hasNext()) {
            addValue(vIter.getDoubleNext());
        }
    }

    /**
     * @return
     */
    public  Breaks computeBreaks() {
        double[] list = toSortedArray();

        int uniqueValues = countUnique(list);
        if (uniqueValues <= 3) {
            return computeBreaks(list, uniqueValues);
        }

        Breaks lastBreaks = computeBreaks(list, 2);
        double lastGvf = lastBreaks.gvf();
        double lastImprovement = lastGvf - computeBreaks(list, 1).gvf();

        for (int i = 3; i <= Math.min(6, uniqueValues); ++i) {
            Breaks breaks = computeBreaks(list, 2);
            double gvf = breaks.gvf();
            double marginalImprovement = gvf - lastGvf;
            if (marginalImprovement < lastImprovement) {
                return lastBreaks;
            }
            lastBreaks = breaks;
            lastGvf = gvf;
            lastImprovement = marginalImprovement;
        }

        return lastBreaks;
    }

    private  double[] toSortedArray() {
        double[] values = new double[this.list.size()];
        for (int i = 0; i != values.length; ++i) {
            values[i] = this.list.get(i);
        }
        Arrays.sort(values);
        return values;
    }

    private  int countUnique(double[] sortedList) {
        int count = 1;
        for (int i = 1; i < sortedList.length; ++i) {
            if (sortedList[i] != sortedList[i - 1]) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param list     sorted list of values
     * @param numclass int number of classes
     * @return int[] breaks (upper indices of class)
     */
    public  Breaks computeBreaks(int numclass) {
        return computeBreaks(toSortedArray(), numclass, new Identity());
    }

    private  Breaks computeBreaks(double[] list, int numclass) {
        return computeBreaks(list, numclass, new Identity());
    }

    private  Breaks computeBreaks(double[] list, int numclass, DoubleFunction transform) {

        int numdata = list.length;

        if (numdata == 0) {
            return new Breaks(new double[0], new int[0]);
        }

        double[][] mat1 = new double[numdata + 1][numclass + 1];
        double[][] mat2 = new double[numdata + 1][numclass + 1];

        for (int i = 1; i <= numclass; i++) {
            mat1[1][i] = 1;
            mat2[1][i] = 0;
            for (int j = 2; j <= numdata; j++) {
                mat2[j][i] = Double.MAX_VALUE;
            }
        }
        double v = 0;
        for (int l = 2; l <= numdata; l++) {
            double s1 = 0;
            double s2 = 0;
            double w = 0;
            for (int m = 1; m <= l; m++) {
                int i3 = l - m + 1;

                double val = transform.apply(list[i3 - 1]);

                s2 += val * val;
                s1 += val;

                w++;
                v = s2 - (s1 * s1) / w;
                int i4 = i3 - 1;
                if (i4 != 0) {
                    for (int j = 2; j <= numclass; j++) {
                        if (mat2[l][j] >= (v + mat2[i4][j - 1])) {
                            mat1[l][j] = i3;
                            mat2[l][j] = v + mat2[i4][j - 1];
                        }
                    }
                }
            }
            mat1[l][1] = 1;
            mat2[l][1] = v;
        }
        int k = numdata;

        int[] kclass = new int[numclass];

        kclass[numclass - 1] = list.length - 1;

        for (int j = numclass; j >= 2; j--) {
            int id = (int) (mat1[k][j]) - 2;

            kclass[j - 2] = id;

            k = (int) mat1[k][j] - 1;
        }
        return new Breaks(list, kclass);
    }

    private interface DoubleFunction {
        double apply(double x);
    }

    private static class Log10 implements DoubleFunction {

        @Override
        public double apply(double x) {
            return Math.log10(x);
        }
    }

    public static class Identity implements DoubleFunction {

        @Override
        public double apply(double x) {
            return x;
        }

    }

    public static class Breaks {

        private double[] sortedValues;
        private int[] breaks;

        /**
         * @param sortedValues the complete array of sorted data values
         * @param breaks       the indexes of the values within the sorted array that begin new classes
         */
        private Breaks(double[] sortedValues, int[] breaks) {
            this.sortedValues = sortedValues;
            this.breaks = breaks;
        }

        /**
         * The Goodness of Variance Fit (GVF) is found by taking the difference
         * between the squared deviations from the array mean (SDAM) and the
         * squared deviations from the class means (SDCM), and dividing by the
         * SDAM
         *
         * @return
         */
        public  double gvf() {
            double sdam = sumOfSquareDeviations(sortedValues);
            double sdcm = 0.0;
            for (int i = 0; i != numClasses(); ++i) {
                sdcm += sumOfSquareDeviations(classList(i));
            }
            return (sdam - sdcm) / sdam;
        }

        private  double sumOfSquareDeviations(double[] values) {
            double mean = mean(values);
            double sum = 0.0;
            for (int i = 0; i != values.length; ++i) {
                double sqDev = Math.pow(values[i] - mean, 2);
                sum += sqDev;
            }
            return sum;
        }

        public  double[] getValues() {
            return sortedValues;
        }

        private  double[] classList(int i) {
            int classStart = (i == 0) ? 0 : breaks[i - 1] + 1;
            int classEnd = breaks[i];
            double list[] = new double[classEnd - classStart + 1];
            for (int j = classStart; j <= classEnd; ++j) {
                list[j - classStart] = sortedValues[j];
            }
            return list;
        }

        /**
         * Get value arrays for each group
         * @return Value arrays for each group
         */
        public List<Array> getGroups() {
            List<Array> groups = new ArrayList<>();
            int n = numClasses();
            for (int i = 0; i < n; i++) {
                double[] values = classList(i);
                groups.add(Array.factory(DataType.DOUBLE, new int[]{values.length}, values));
            }

            return groups;
        }

        /**
         * @param classIndex
         * @return the minimum value (inclusive) of the given class
         */
        public  double getClassMin(int classIndex) {
            if (classIndex == 0) {
                return sortedValues[0];
            } else {
                return sortedValues[breaks[classIndex - 1] + 1];
            }
        }

        /**
         * @param classIndex
         * @return the maximum value (inclusive) of the given class
         */
        public  double getClassMax(int classIndex) {
            return sortedValues[breaks[classIndex]];
        }

        public  int getClassCount(int classIndex) {
            if (classIndex == 0) {
                return breaks[0] + 1;
            } else {
                return breaks[classIndex] - breaks[classIndex - 1];
            }
        }

        private  double mean(double[] values) {
            double sum = 0;
            for (int i = 0; i != values.length; ++i) {
                sum += values[i];
            }
            return sum / values.length;
        }

        public  int numClasses() {
            return breaks.length;
        }

        /**
         * Get classes bound values
         * @return Classes bound values
         */
        public Array getClassValues() {
            double[] classValues = new double[breaks.length + 1];
            classValues[0] = sortedValues[0];
            for (int i = 0; i < breaks.length; i++) {
                classValues[i + 1] = getClassMax(i);
            }

            return Array.factory(DataType.DOUBLE, new int[]{classValues.length}, classValues);
        }

        @Override
        public  String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i != numClasses(); ++i) {
                if (getClassMin(i) == getClassMax(i)) {
                    sb.append(getClassMin(i));
                } else {
                    sb.append(getClassMin(i)).append(" - ").append(getClassMax(i));
                }
                sb.append(" (" + getClassCount(i) + ")");
                sb.append(" = ").append(Arrays.toString(classList(i)));
                sb.append("\n");
            }
            return sb.toString();
        }

        public  String printClusters() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i != numClasses(); ++i) {
                if (getClassMin(i) == getClassMax(i)) {
                    sb.append(getClassMin(i));
                } else {
                    sb.append(getClassMin(i)).append(" - ").append(getClassMax(i));
                }
                sb.append(" (" + getClassCount(i) + ");");
                // sb.append("\n");
            }
            return sb.toString();
        }

        public  int classOf(double value) {
            for (int i = 0; i != numClasses(); ++i) {
                if (value <= getClassMax(i)) {
                    return i;
                }
            }
            return numClasses() - 1;
        }

        /**
         * Get class index of values
         * @param values The values array
         * @return Class index array
         */
        public Array classOf(Array values) {
            Array r = Array.factory(DataType.INT, values.getShape());
            IndexIterator vIter = values.getIndexIterator();
            IndexIterator rIter = r.getIndexIterator();
            while (vIter.hasNext()) {
                rIter.setIntNext(classOf(vIter.getDoubleNext()));
            }

            return r;
        }

        public static void main(String[] args) {
            Jenks jenks = new Jenks();
            jenks.addValue(1.0);
            jenks.addValue(2.0);
            jenks.addValue(2.0);
            jenks.addValue(2.0);
            jenks.addValue(3.0);
        }

    }

}
