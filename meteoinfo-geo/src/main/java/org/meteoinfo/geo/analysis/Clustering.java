/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.geo.analysis;

import org.locationtech.jts.io.InStream;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geometry.shape.PointZ;
import org.meteoinfo.geometry.shape.PolylineZShape;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.math.ArrayUtil;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author wyq
 */
public class Clustering {

    /**
     * Clustering calculation
     *
     * @param inFile Input file
     * @param outFile Output file
     * @param N Row number
     * @param M Column number
     * @param LN Level number
     * @param disType Distant define type: Euclidean or Angle
     * @throws FileNotFoundException
     */
    public static void calculate(String inFile, String outFile, int N, int M, int LN, DistanceType disType) throws FileNotFoundException, IOException {
        double[][] DATA = new double[N][M];

        //---- Open input File            
        String aLine;
        String[] aDataArray;
        int i;
        int j;
        int row;
        int col;
        List<String> flags = new ArrayList<>();    //Date time and height

        BufferedReader sr = new BufferedReader(new FileReader(inFile));
        row = 0;
        while ((aLine = sr.readLine()) != null) {
            if (aLine.isEmpty()) {
                continue;
            }

            aDataArray = aLine.split(",");

            //Ignor the data line with not normal point number
            if ((aDataArray.length - 2) / 3 != M / 2) {
                continue;
            }

            flags.add(aDataArray[0] + "," + aDataArray[1]);

            col = 0;
            for (i = 0; i <= M / 2 - 1; i++) {
                for (j = 0; j <= 2; j++) {
                    if (j != 2) {
                        DATA[row][col] = Double.parseDouble(aDataArray[i * 3 + j + 2]);
                        col += 1;
                    }
                }
            }
            row += 1;
        }
        sr.close();

        //Clustering calculation
        int[][] ICLASS = calculation(DATA, LN, disType);

        //Write clustering result to output file
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(outFile)));
        aLine = "Time,Height";
        for (i = 2; i <= LN; i++) {
            aLine = aLine + "," + String.valueOf(i) + "CL";
        }
        sw.write(aLine);
        sw.newLine();
        for (i = 0; i <= N - 1; i++) {
            aLine = flags.get(i);
            for (j = 0; j <= LN - 2; j++) {
                aLine = aLine + "," + String.valueOf(ICLASS[i][j]);
            }
            sw.write(aLine);
            sw.newLine();
        }
        sw.close();
    }

    /**
     * Clustering calculation
     *
     * @param trajLayers Trajectory layers
     * @param outFile Output file
     * @param N Row number - trajectory number
     * @param M Column number - point number
     * @param LN Level number
     * @param interval Point interval
     * @param disType Distant define type: Euclidean or Angle
     * @throws IOException
     */
    public static void calculate(List<VectorLayer> trajLayers, String outFile, int N, int M, int LN, int interval, DistanceType disType) throws IOException {        
        double[][] DATA = new double[N][M * 2];

        //---- Get data array           
        String aLine;
        int i;
        int j;
        int row;
        int col;
        List<String> flags = new ArrayList<>();    //Date time and height
        LocalDateTime aDate;

        row = 0;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
        for (VectorLayer layer : trajLayers) {
            PointZ aPoint;
            int sNum = layer.getShapeNum();
            for (i = 0; i < sNum; i++) {
                aDate = (LocalDateTime) layer.getCellValue("Date", i);
                int hour = Integer.parseInt(layer.getCellValue("Hour", i).toString());
                aDate = aDate.withHour(hour);
                aLine = format.format(aDate);
                String height = layer.getCellValue("Height", i).toString();
                flags.add(aLine + "," + height);
                PolylineZShape aPLZ = (PolylineZShape) layer.getShapes().get(i);
                col = 0;
                for (j = 0; j < aPLZ.getPointNum(); j++) {
                    if (j % interval == 0) {
                        aPoint = (PointZ) aPLZ.getPoints().get(j);
                        DATA[row][col] = aPoint.X;
                        col += 1;
                        DATA[row][col] = aPoint.Y;
                        col += 1;
                    }
                }
                row += 1;
            }
        }

        //Clustering calculation
        int[][] ICLASS = calculation(DATA, LN, disType);

        //Write clustering result to output file
        saveClassResult(ICLASS, flags, outFile);
    }

    /**
     * Clustering calculation
     *
     * @param trajLayers Trajectory layers
     * @param outFile Output file
     * @param N Row number - trajectory number
     * @param M Column number - point number
     * @param LN Level number
     * @param interval Point interval
     * @throws IOException
     */
    public static void calculate3D(List<VectorLayer> trajLayers, String outFile, int N, int M, int LN, int interval) throws IOException {
        double[][] DATA = new double[N][M * 3];

        //---- Get data array
        String aLine;
        int i;
        int j;
        int row;
        int col;
        List<String> flags = new ArrayList<>();    //Date time and height
        LocalDateTime aDate;

        row = 0;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHH");
        for (VectorLayer layer : trajLayers) {
            PointZ aPoint;
            int sNum = layer.getShapeNum();
            for (i = 0; i < sNum; i++) {
                aDate = (LocalDateTime) layer.getCellValue("Date", i);
                int hour = Integer.parseInt(layer.getCellValue("Hour", i).toString());
                aDate = aDate.withHour(hour);
                aLine = format.format(aDate);
                String height = layer.getCellValue("Height", i).toString();
                flags.add(aLine + "," + height);
                PolylineZShape aPLZ = (PolylineZShape) layer.getShapes().get(i);
                col = 0;
                for (j = 0; j < aPLZ.getPointNum(); j++) {
                    if (j % interval == 0) {
                        aPoint = (PointZ) aPLZ.getPoints().get(j);
                        DATA[row][col] = aPoint.X;
                        col += 1;
                        DATA[row][col] = aPoint.Y;
                        col += 1;
                        DATA[row][col] = aPoint.Z;
                        col += 1;
                    }
                }
                row += 1;
            }
        }

        //Clustering calculation
        int[][] ICLASS = calculation(DATA, LN, null, true);

        //Write clustering result to output file
        saveClassResult(ICLASS, flags, outFile);
    }

    /**
     * Save class results
     * @param data Class data
     * @param flags Trajectory flags
     * @param fileName Output file name
     * @throws IOException
     */
    public static void saveClassResult(int[][] data, List<String> flags, String fileName) throws IOException {
        int N = data.length;
        int LN = data[0].length;
        BufferedWriter sw = new BufferedWriter(new FileWriter(fileName));
        String aLine = "Time,Height";
        for (int i = 2; i <= LN; i++) {
            aLine = aLine + "," + String.valueOf(i) + "CL";
        }
        sw.write(aLine);
        sw.newLine();
        for (int i = 0; i <= N - 1; i++) {
            aLine = flags.get(i);
            for (int j = 0; j <= LN - 2; j++) {
                aLine = aLine + "," + String.valueOf(data[i][j]);
            }
            sw.write(aLine);
            sw.newLine();
        }
        sw.close();
    }

    /**
     * Clustering calculation
     *
     * @param trajData Trajectory data array
     * @param LN Level number
     * @param disType Distant define type: Euclidean or Angle
     * @throws IOException
     */
    public static Array calculate(Array trajData, int LN, DistanceType disType) throws IOException {
        double[][] DATA = (double[][]) ArrayUtil.copyToNDJavaArray_Double(trajData);

        //Clustering calculation
        int[][] ICLASS = calculation(DATA, LN, disType);

        int[] rData = Stream.of(ICLASS).flatMapToInt(IntStream::of).toArray();
        Array r = Array.factory(DataType.INT, new int[]{ICLASS.length, ICLASS[0].length},
                rData);

        return r;
    }

    /**
     * Clustering calculation
     *
     * @param xData X data array
     * @param yData Y data array
     * @param LN Level number
     * @param disType Distant define type: Euclidean or Angle
     * @throws IOException
     */
    public static Array calculate(Array xData, Array yData,  int LN, DistanceType disType) throws IOException {
        xData = xData.copyIfView();
        yData = yData.copyIfView();

        int[] shape = xData.getShape();
        int n = shape[0];
        int pn = shape[1];
        int m = pn * 2;
        double[][] DATA = new double[n][m];
        for (int row = 0; row < n; row++) {
            int col = 0;
            for (int i = 0; i < pn; i++) {
                DATA[row][col] = xData.getDouble(row * pn + i);
                col += 1;
                DATA[row][col] = yData.getDouble(row * pn + i);
                col += 1;
            }
        }

        //Clustering calculation
        int[][] ICLASS = calculation(DATA, LN, disType);

        int[] rData = Stream.of(ICLASS).flatMapToInt(IntStream::of).toArray();
        Array r = Array.factory(DataType.INT, new int[]{ICLASS.length, ICLASS[0].length},
                rData);

        return r;
    }

    /**
     * Clustering calculation
     *
     * @param xData X data array
     * @param yData Y data array
     * @param LN Level number
     * @param disType Distant type: Euclidean or Angle
     * @throws IOException
     */
    public static Array calculate(Array xData, Array yData,  int LN, String disType) throws IOException {
        DistanceType distanceType = DistanceType.valueOf(disType.toUpperCase());

        return calculate(xData, yData, LN, distanceType);
    }

    /**
     * Clustering calculation
     *
     * @param xData X data array
     * @param yData Y data array
     * @param LN Level number
     * @throws IOException
     */
    public static Array calculate(Array xData, Array yData,  int LN) throws IOException {
        return calculate(xData, yData, LN, DistanceType.EUCLIDEAN);
    }

    /**
     * Clustering calculation
     *
     * @param xData X data array
     * @param yData Y data array
     * @param zData Z data array
     * @param LN Level number
     * @throws IOException
     */
    public static Array calculate3D(Array xData, Array yData, Array zData, int LN) throws IOException {
        xData = xData.copyIfView();
        yData = yData.copyIfView();

        int[] shape = xData.getShape();
        int n = shape[0];
        int pn = shape[1];
        int m = pn * 3;
        double[][] DATA = new double[n][m];
        for (int row = 0; row < n; row++) {
            int col = 0;
            for (int i = 0; i < pn; i++) {
                DATA[row][col] = xData.getDouble(row * pn + i);
                col += 1;
                DATA[row][col] = yData.getDouble(row * pn + i);
                col += 1;
                DATA[row][col] = zData.getDouble(row * pn + i);
                col += 1;
            }
        }

        //Clustering calculation
        int[][] ICLASS = calculation(DATA, LN, null, true);

        int[] rData = Stream.of(ICLASS).flatMapToInt(IntStream::of).toArray();
        Array r = Array.factory(DataType.INT, new int[]{ICLASS.length, ICLASS[0].length},
                rData);

        return r;
    }

    /**
     * Clustering calculation
     *
     * @param DATA Input data array
     * @param outFile Output file
     * @param LN Level number
     * @param disType Distant define type: Euclidean or Angle
     * @throws IOException
     */
    public static void calculation(double[][] DATA, String outFile, int LN, DistanceType disType) throws IOException {
        int[][] ICLASS = calculation(DATA, LN, disType);
        int N = ICLASS.length;

        //---- Write output file
        BufferedWriter sw = new BufferedWriter(new FileWriter(new File(outFile)));
        int i, j;
        String aLine = "NO";
        for (i = 2; i <= LN; i++) {
            aLine = aLine + "," + String.valueOf(i) + "CL";
        }
        sw.write(aLine);
        sw.newLine();
        for (i = 0; i <= N - 1; i++) {
            aLine = String.valueOf(i + 1);
            for (j = 0; j <= LN - 2; j++) {
                aLine = aLine + "," + String.valueOf(ICLASS[i][j]);
            }
            sw.write(aLine);
            sw.newLine();
        }
        sw.close();
    }

    /**
     * Clustering calculation
     *
     * @param DATA Input data array
     * @param LN Level number
     * @param disType Distant define type: Euclidean or Angle
     * @return Clustering result array
     */
    public static int[][] calculation(double[][] DATA, int LN, DistanceType disType) {
        return calculation(DATA, LN, disType, false);
    }

    /**
     * Clustering calculation
     *
     * @param DATA Input data array
     * @param LN Level number
     * @param disType Distant define type: Euclidean or Angle
     * @param is3D 3D clustering or not
     * @return Clustering result array
     */
    public static int[][] calculation(double[][] DATA, int LN, DistanceType disType, boolean is3D) {
        //double[,] DATA = new double[N, M];
        int N = DATA.length;
        int M = DATA[0].length;
        double[] CRIT = new double[N];
        double[] MEMBR = new double[N];
        double[] CRITVAL = new double[LN];
        int[] IA = new int[N];
        int[] IB = new int[N];
        int[][] ICLASS = new int[N][LN];
        int[] HVALS = new int[LN];
        int[] IORDER = new int[LN];
        int[] HEIGHT = new int[LN];
        int[] NN = new int[N];
        double[] DISNN = new double[N];
        boolean[] FLAG = new boolean[N];

        //---- IN ABOVE, 18=N, 16=M, 9=LEV, 153=N(N-1)/2


        //---- Call HC
        //int LEN = (N * (N - 1)) / 2;
        int IOPT = 1;
        //---- Construct dissimilarity matrix
        double[] DISS;
        if (is3D) {
            DISS = calDistance3D(IOPT, DATA);
        } else {
            DISS = calDistance(IOPT, DATA, disType);
        }
        HC(N, IOPT, DATA, IA, IB, CRIT, MEMBR, NN, DISNN, FLAG, DISS, disType);

        //---- Call HCASS
        HCASS(N, IA, IB, CRIT, LN, ICLASS, HVALS, IORDER, CRITVAL, HEIGHT);

        //---- Call HCDEN
        //Call HCDEN(LEV, IORDER, HEIGHT, CRITVAL)

        return ICLASS;
    }

    private static double[] calDistance(int IOPT, double[][] DATA, DistanceType distanceType) {
        int N = DATA.length;
        int M = DATA[0].length;
        int i;
        int j;
        int k;
        int IND;
        long nl = (long) N * (N - 1) / 2;
        int n = (int) nl;
        double[] DISS = new double[n];
        if (distanceType == DistanceType.ANGLE) {
            double X0;
            double Y0;
            double ANGLE;
            double A;
            double B;
            double C;
            X0 = DATA[0][0];
            Y0 = DATA[0][1];
            for (i = 0; i <= N - 2; i++) {
                for (j = i + 1; j <= N - 1; j++) {
                    IND = IOFFSET(N, i + 1, j + 1);
                    DISS[IND] = 0.0;
                    for (k = 1; k <= M / 2 - 1; k++) {
                        A = Math.pow((DATA[i][2 * k] - X0), 2) + Math.pow((DATA[i][2 * k + 1] - Y0), 2);
                        B = Math.pow((DATA[j][2 * k] - X0), 2) + Math.pow((DATA[j][2 * k + 1] - Y0), 2);
                        C = Math.pow((DATA[j][2 * k] - DATA[i][2 * k]), 2) + Math.pow((DATA[j][2 * k + 1] - DATA[i][2 * k + 1]), 2);
                        if (A == 0 | B == 0) {
                            ANGLE = 0;
                        } else {
                            ANGLE = 0.5 * (A + B - C) / Math.sqrt(A * B);
                        }
                        if ((Math.abs(ANGLE) > 1.0)) {
                            ANGLE = 1.0;
                        }
                        DISS[IND] = DISS[IND] + Math.acos(ANGLE);
                    }
                    DISS[IND] = DISS[IND] / (M / 2);
                    if (IOPT == 1) {
                        DISS[IND] = DISS[IND] / 2.0;
                    }
                    //           (Above is done for the case of the min. var. method
                    //            where merging criteria are defined in terms of variances
                    //            rather than distances.)
                }
            }
        } else {
            for (i = 0; i <= N - 2; i++) {
                for (j = i + 1; j <= N - 1; j++) {
                    IND = IOFFSET(N, i + 1, j + 1);
                    DISS[IND] = 0.0;
                    for (k = 0; k <= M / 2 - 1; k++) {
                        DISS[IND] = DISS[IND] + Math.pow((DATA[i][2 * k] - DATA[j][2 * k]), 2) + Math.pow((DATA[i][2 * k + 1] - DATA[j][2 * k + 1]), 2);
                    }
                    DISS[IND] = Math.sqrt(DISS[IND]);
                    if (IOPT == 1) {
                        DISS[IND] = DISS[IND] / 2;
                    }
                    //---- (Above is done for the case of the min. var. method
                    //---- where merging criteria are defined in terms of variances
                    //---- rather than distances.)
                }
            }
        }

        return DISS;
    }

    private static double[] calDistance3D(int IOPT, double[][] DATA) {
        int N = DATA.length;
        int M = DATA[0].length;
        int i;
        int j;
        int k;
        int IND;
        long nl = (long) N * (N - 1) / 2;
        int n = (int) nl;
        double[] DISS = new double[n];
        for (i = 0; i <= N - 2; i++) {
            for (j = i + 1; j <= N - 1; j++) {
                IND = IOFFSET(N, i + 1, j + 1);
                DISS[IND] = 0.0;
                for (k = 0; k <= M / 3 - 1; k++) {
                    DISS[IND] = DISS[IND] + Math.pow((DATA[i][2 * k] - DATA[j][2 * k]), 2) +
                            Math.pow((DATA[i][2 * k + 1] - DATA[j][2 * k + 1]), 2) +
                            Math.pow((DATA[i][2 * k + 2] - DATA[j][2 * k + 2]), 2);
                }
                DISS[IND] = Math.sqrt(DISS[IND]);
                if (IOPT == 1) {
                    DISS[IND] = DISS[IND] / 2;
                }
                //---- (Above is done for the case of the min. var. method
                //---- where merging criteria are defined in terms of variances
                //---- rather than distances.)
            }
        }

        return DISS;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //                                                            C
    //  HIERARCHICAL CLUSTERING using (user-specified) criterion. C
    //                                                            C
    //  Parameters:                                               C
    //                                                            C
    //  DATA(N,M)         input data matrix,                      C
    //  DISS(LEN)         dissimilarities in lower half diagonal  C
    //                    storage; LEN = N.N-1/2,                 C
    //  IOPT              clustering criterion to be used,        C
    //  IA, IB, CRIT      history of agglomerations; dimensions   C
    //                    N, first N-1 locations only used,       C
    //  MEMBR, NN, DISNN  vectors of length N, used to store      C 
    //                    cluster cardinalities, current nearest  C
    //                    neighbour, and the dissimilarity assoc. C
    //                    with the latter.                        C
    //  FLAG              boolean indicator of agglomerable obj./ C
    //                    clusters.                               C
    //                                                            C
    //  F. Murtagh, ESA/ESO/STECF, Garching, February 1986.       C
    //                                                            C
    //------------------------------------------------------------C
    private static void HC(int N, int IOPT, double[][] DATA, int[] IA, int[] IB, double[] CRIT, double[] MEMBR,
            int[] NN, double[] DISNN, boolean[] FLAG, double[] DISS, DistanceType DISTYPE) {
        double INF = 1.0 * (Math.pow(10, 20));
        int i;
        int j;
        int k;

        //---- Initializations
        for (i = 0; i <= N - 1; i++) {
            MEMBR[i] = 1.0;
            FLAG[i] = true;
        }
        int NCL = N;

        //---- Carry out an agglomeration - first create list of NNs
        int IND;
        double DMIN;
        int JM = 0;
        for (i = 0; i <= N - 2; i++) {
            DMIN = INF;
            for (j = i + 1; j <= N - 1; j++) {
                IND = IOFFSET(N, i + 1, j + 1);
                if (DISS[IND] >= DMIN) {
                    continue;
                }
                DMIN = DISS[IND];
                JM = j;
            }
            NN[i] = JM;
            DISNN[i] = DMIN;
        }

        //---- Loop 
        do {
            //---- Next, determine least diss. using list of NNs
            int IM = 0;
            DMIN = INF;
            for (i = 0; i <= N - 2; i++) {
                if (!FLAG[i]) {
                    continue;
                }
                if (DISNN[i] >= DMIN) {
                    continue;
                }
                DMIN = DISNN[i];
                IM = i;
                JM = NN[i];
            }
            NCL = NCL - 1;

            //---- This allows an agglomeration to be carried out.
            int I2;
            int J2;

            I2 = Math.min(IM, JM);
            J2 = Math.max(IM, JM);
            IA[N - NCL - 1] = I2 + 1;
            IB[N - NCL - 1] = J2 + 1;
            CRIT[N - NCL - 1] = DMIN;

            //---- Update dissimilarities from new cluster.
            double X;
            double XX;
            int IND1;
            int IND2;
            int IND3;
            int JJ = 0;
            FLAG[J2] = false;
            DMIN = INF;
            for (k = 0; k <= N - 1; k++) {
                if (!FLAG[k]) {
                    continue;
                }
                if (k == I2) {
                    continue;
                }
                X = MEMBR[I2] + MEMBR[J2] + MEMBR[k];
                if (I2 < k) {
                    IND1 = IOFFSET(N, I2 + 1, k + 1);
                } else {
                    IND1 = IOFFSET(N, k + 1, I2 + 1);
                }
                if (J2 < k) {
                    IND2 = IOFFSET(N, J2 + 1, k + 1);
                } else {
                    IND2 = IOFFSET(N, k + 1, J2 + 1);
                }
                IND3 = IOFFSET(N, I2 + 1, J2 + 1);
                XX = DISS[IND3];

                //---- Ward's minimum variance method - IOPT=1.
                if (IOPT == 1) {
                    DISS[IND1] = (MEMBR[I2] + MEMBR[k]) * DISS[IND1] + (MEMBR[J2] + MEMBR[k]) * DISS[IND2] - MEMBR[k] * XX;
                    DISS[IND1] = DISS[IND1] / X;
                }

                //---- Single link method - IOPT=2.
                if (IOPT == 2) {
                    DISS[IND1] = Math.min(DISS[IND1], DISS[IND2]);
                }

                //---- Complete link method - IOPT=3.
                if (IOPT == 3) {
                    DISS[IND1] = Math.max(DISS[IND1], DISS[IND2]);
                }

                //---- Average link (or group average) method - IOPT=4.
                if (IOPT == 4) {
                    DISS[IND1] = (MEMBR[I2] * DISS[IND1] + MEMBR[J2] * DISS[IND2]) / (MEMBR[I2] + MEMBR[J2]);
                }

                //---- Mcquitty's method - IOPT=5.
                if (IOPT == 5) {
                    DISS[IND1] = 0.5 * DISS[IND1] + 0.5 * DISS[IND2];
                }

                //----  MEDIAN (GOWER'S) METHOD - IOPT=6.
                if (IOPT == 6) {
                    DISS[IND1] = 0.5 * DISS[IND1] + 0.5 * DISS[IND2] - 0.25 * XX;
                }

                //  CENTROID METHOD - IOPT=7.
                if (IOPT == 7) {
                    DISS[IND1] = (MEMBR[I2] * DISS[IND1] + MEMBR[J2] * DISS[IND2] - MEMBR[I2] * MEMBR[J2] * XX / (MEMBR[I2] + MEMBR[J2])) / (MEMBR[I2] + MEMBR[J2]);
                }

                if (I2 > k) {
                    continue;
                }
                if (DISS[IND1] >= DMIN) {
                    continue;
                }
                DMIN = DISS[IND1];
                JJ = k;
            }
            MEMBR[I2] = MEMBR[I2] + MEMBR[J2];
            DISNN[I2] = DMIN;
            NN[I2] = JJ;

            //---- Update list of NNs insofar as this is required.
            for (i = 0; i <= N - 2; i++) {
                if (!FLAG[i]) {
                    continue;
                }
                if (NN[i] == I2 | NN[i] == J2) {
                    //---- Redetermine NN of I
                    DMIN = INF;
                    for (j = i + 1; j <= N - 1; j++) {
                        IND = IOFFSET(N, i + 1, j + 1);
                        if (!FLAG[j]) {
                            continue;
                        }
                        if (i == j) {
                            continue;
                        }
                        if (DISS[IND] >= DMIN) {
                            continue;
                        }
                        DMIN = DISS[IND];
                        JJ = j;
                    }
                    NN[i] = JJ;
                    DISNN[i] = DMIN;
                }
            }
        } while (!(NCL == 1));

    }

    private static int IOFFSET(int N, int I, int J) {
        //  Map row I and column J of upper half diagonal symmetric matrix 
        //  onto vector.
        return J + (I - 1) * N - (I * (I + 1)) / 2 - 1;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++C
    //                                                               C
    //  Given a HIERARCHIC CLUSTERING, described as a sequence of    C
    //  agglomerations, derive the assignments into clusters for the C
    //  top LEV-1 levels of the hierarchy.                           C
    //  Prepare also the required data for representing the          C
    //  dendrogram of this top part of the hierarchy.                C
    //                                                               C
    //  Parameters:                                                  C
    //                                                               C
    //  IA, IB, CRIT: vectors of dimension N defining the agglomer-  C
    //                 ations.                                       C
    //  LEV:          number of clusters in largest partition.       C
    //  HVALS:        vector of dim. LEV, used internally only.      C
    //  ICLASS:       array of cluster assignments; dim. N by LEV.   C
    //  IORDER, CRITVAL, HEIGHT: vectors describing the dendrogram,  C
    //                all of dim. LEV.                               C
    //                                                               C
    //  F. Murtagh, ESA/ESO/STECF, Garching, February 1986.          C
    //                                                               C
    //---------------------------------------------------------------C
    private static void HCASS(int N, int[] IA, int[] IB, double[] CRIT, int LEV, int[][] ICLASS, int[] HVALS, int[] IORDER,
            double[] CRITVAL, int[] HEIGHT) {
        //  Pick out the clusters which the N objects belong to,
        //  at levels N-2, N-3, ... N-LEV+1 of the hierarchy.
        //  The clusters are identified by the lowest seq. no. of
        //  their members.
        //  There are 2, 3, ... LEV clusters, respectively, for the
        //  above levels of the hierarchy.

        HVALS[0] = 1;
        HVALS[1] = IB[N - 2];
        int LOC = 2;
        int i;
        int j;
        boolean ifGo;
        for (i = N - 3; i >= N - LEV; i += -1) {
            ifGo = true;
            for (j = 0; j <= LOC - 1; j++) {
                if (IA[i] == HVALS[j]) {
                    ifGo = false;
                }
            }
            if (ifGo) {
                HVALS[LOC] = IA[i];
                LOC += 1;
            }
            ifGo = true;
            for (j = 0; j <= LOC - 1; j++) {
                if (IB[i] == HVALS[j]) {
                    ifGo = false;
                }
            }
            if (ifGo) {
                HVALS[LOC] = IB[i];
                LOC += 1;
            }
        }

        int LEVEL;
        int ICL;
        int ILEV;
        int NCL;
        for (LEVEL = N - LEV; LEVEL <= N - 2; LEVEL++) {
            for (i = 0; i <= N - 1; i++) {
                ICL = i + 1;
                for (ILEV = 0; ILEV <= LEVEL - 1; ILEV++) {
                    if (IB[ILEV] == ICL) {
                        ICL = IA[ILEV];
                    }
                }
                NCL = N - LEVEL - 1;
                ICLASS[i][NCL - 1] = ICL;
            }
        }

        int k;
        for (i = 0; i <= N - 1; i++) {
            for (j = 0; j <= LEV - 2; j++) {
                for (k = 1; k <= LEV - 1; k++) {
                    if (ICLASS[i][j] != HVALS[k]) {
                        continue;
                    }
                    ICLASS[i][j] = k + 1;
                    break; 
                }
            }
        }

    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++C
    //                                                 C
    //  Construct a DENDROGRAM of the top 8 levels of  C
    //  a HIERARCHIC CLUSTERING.                       C
    //                                                 C
    //  Parameters:                                    C
    //                                                 C
    //  IORDER, HEIGHT, CRITVAL: vectors of length LEV C
    //          defining the dendrogram.               C
    //          These are: the ordering of objects     C
    //          along the bottom of the dendrogram     C
    //          (IORDER); the height of the vertical   C
    //          above each object, in ordinal values   C
    //          (HEIGHT); and in real values (CRITVAL).C
    //                                                 C
    //  NOTE: these vectors MUST have been set up with C
    //        LEV = 9 in the prior call to routine     C
    //        HCASS.
    //                                                 C
    //  F. Murtagh, ESA/ESO/STECF, Garching, Feb. 1986.C
    //                                                 C 
    //-------------------------------------------------C
    private static void HCDEN(int LEV, int[] IORDER, int[] HEIGHT, double[] CRITVAL) {
    }
}
