/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.linalg;

import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 *
 * @author Yaqiang Wang
 */
public class EOF {

    private static final int MAX_JACOBI_TIMES = 50;
    
    /**
     * EOF algorithm
     *
     * @param N Station or grid number
     * @param LL Time dimension number
     * @param f Origin data
     * @param method Method
     * @return Eigen vector, time factor, accumulated explained variance,
     * ordered eigen value
     */
    public static Object[] SEOF(int N, int LL, double[][] f, DataProMethod method) {
        double[][] H = new double[f.length][f[0].length];
        switch (method) {
            case NONE:
                for (int i = 0; i < LL; i++) {
                    System.arraycopy(f[i], 0, H[i], 0, N);
                }
                break;
            case DEPARTURE:
                H = dep(N, LL, f);
                break;
            default:
                H = nor(N, LL, f);
                break;
        }
        return SEOF(N, LL, f, H);
    }

    /**
     * EOF algorithm
     *
     * @param N Station or grid number
     * @param LL Time dimension number
     * @param f Origin data
     * @param H Processed data
     * @return Eigen vector, time factor, accumulated explained variance,
     * ordered eigen value
     */
    public static Object[] SEOF(int N, int LL, double[][] f, double[][] H) {
        double[][] A = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = 0;
                for (int k = 0; k < LL; k++) {
                    A[i][j] += H[k][i] * H[k][j];
                }
                A[j][i] = A[i][j];
            }
        }

        Object[] rr = jacobi(N, true, A, MAX_JACOBI_TIMES);
        double[] D = (double[]) rr[0];    //eigen value
        double[][] V = (double[][]) rr[1];    //eigen vector

        int[] IX = new int[N];
        for (int i = 0; i < N; i++) {
            IX[i] = i;
        }

        for (int i = 0; i < N - 1; i++) //bubble sort
        {
            for (int j = i + 1; j < N; j++) {
                if (Math.abs(D[i]) >= Math.abs(D[j])) {
                    continue;
                }

                double W1 = D[i];
                D[i] = D[j];
                D[j] = W1;

                int k = IX[i];
                IX[i] = IX[j];
                IX[j] = k;
            }
        }

        double[][] V1 = new double[N][N];    //eigen vector
        for (int j = 0; j < N; j++) {
            int ILW = IX[j];
            for (int i = 0; i < N; i++) {
                V1[i][j] = V[i][ILW];
            }
        }

        double[][] T = new double[LL][N];    //Time factor
        for (int L = 0; L < LL; L++) {
            for (int j = 0; j < N; j++) {
                T[L][j] = 0;
                for (int i = 0; i < N; i++) {
                    T[L][j] += f[L][i] * V1[i][j];
                }
            }
        }

        double AP = 0;
        for (int i = 0; i < N; i++) {
            AP += D[i];
        }

        double[] H1 = new double[N];    //explained variance
        for (int i = 0; i < N; i++) {
            double AP1 = 0;
            for (int j = 0; j <= i; j++) {
                AP1 += D[j];
            }
            H1[i] = 1.0 * AP1 / AP;
        }

        return new Object[]{V1, T, H1, D};
    }
    
    /**
     * EOF algorithm
     *
     * @param f Origin data
     * @param method Method
     * @return Eigen vector, time factor, accumulated explained variance,
     * ordered eigen value
     */
    public static Object[] SEOF(Array f, DataProMethod method) {
        int[] shape = f.getShape();
        int LL = shape[0];
        int N = shape[1];
        Array H = Array.factory(DataType.DOUBLE, shape);
        switch (method) {
            case NONE:
                for (int i = 0; i < H.getSize(); i++) {
                    H.setDouble(i, f.getDouble(i));
                }
                break;
            case DEPARTURE:
                H = dep(N, LL, f);
                break;
            default:
                H = nor(N, LL, f);
                break;
        }
        return SEOF(N, LL, f, H);
    }
    
    /**
     * EOF algorithm
     *
     * @param N Station or grid number
     * @param LL Time dimension number
     * @param f Origin data
     * @param method Method
     * @return Eigen vector, time factor, accumulated explained variance,
     * ordered eigen value
     */
    public static Object[] SEOF(int N, int LL, Array f, DataProMethod method) {
        Array H = Array.factory(DataType.DOUBLE, f.getShape());
        switch (method) {
            case NONE:
                for (int i = 0; i < H.getSize(); i++) {
                    H.setDouble(i, f.getDouble(i));
                }
                break;
            case DEPARTURE:
                H = dep(N, LL, f);
                break;
            default:
                H = nor(N, LL, f);
                break;
        }
        return SEOF(N, LL, f, H);
    }
    
    /**
     * EOF algorithm
     *
     * @param N Station or grid number
     * @param LL Time dimension number
     * @param f Origin data
     * @param H Processed data
     * @return Eigen vector, time factor, accumulated explained variance,
     * ordered eigen value
     */
    public static Object[] SEOF(int N, int LL, Array f, Array H) {
        Array A = Array.factory(DataType.DOUBLE, new int[]{N, N});
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A.setDouble(i * N + j, 0);
                for (int k = 0; k < LL; k++) {
                    A.setDouble(i * N + j, A.getDouble(i * N + j) + H.getDouble(k * N + i) * H.getDouble(k * N + j));
                }
                A.setDouble(j * N + i, A.getDouble(i * N + j));
            }
        }

        Object[] rr = jacobi(N, true, A, MAX_JACOBI_TIMES);
        Array D = (Array) rr[0];    //eigen value
        Array V = (Array) rr[1];    //eigen vector

        int[] IX = new int[N];
        for (int i = 0; i < N; i++) {
            IX[i] = i;
        }

        for (int i = 0; i < N - 1; i++) //bubble sort
        {
            for (int j = i + 1; j < N; j++) {
                if (Math.abs(D.getDouble(i)) >= Math.abs(D.getDouble(j))) {
                    continue;
                }

                double W1 = D.getDouble(i);
                D.setDouble(i, D.getDouble(j));
                D.setDouble(j, W1);

                int k = IX[i];
                IX[i] = IX[j];
                IX[j] = k;
            }
        }

        Array V1 = Array.factory(DataType.DOUBLE, new int[]{N, N});    //eigen vector
        for (int j = 0; j < N; j++) {
            int ILW = IX[j];
            for (int i = 0; i < N; i++) {
                V1.setDouble(i * N + j, V.getDouble(i * N + ILW));
            }
        }

        Array T = Array.factory(DataType.DOUBLE, new int[]{LL, N});    //Time factor
        for (int L = 0; L < LL; L++) {
            for (int j = 0; j < N; j++) {
                T.setDouble(L * N + j, 0);
                for (int i = 0; i < N; i++) {
                    T.setDouble(L * N + j, T.getDouble(L * N + j) + f.getDouble(L * N + i) * V1.getDouble(i * N + j));
                }
            }
        }

        double AP = 0;
        for (int i = 0; i < N; i++) {
            AP += D.getDouble(i);
        }

        Array H1 = Array.factory(DataType.DOUBLE, new int[]{N});    //explained variance
        for (int i = 0; i < N; i++) {
            double AP1 = 0;
            for (int j = 0; j <= i; j++) {
                AP1 += D.getDouble(j);
            }
            H1.setDouble(i, 1.0 * AP1 / AP);
        }

        return new Object[]{V1, T, H1, D};
    }
    
    /**
     * Calculate eigen value and eigen vector using Jacobi method
     *
     * @param N Time dimension number
     * @param EV Always as true
     * @param A Input matrix data
     * @return Eigen value and eigen vector
     */
    public static Object[] jacobi(int N, boolean EV, double[][] A) {
        return jacobi(N, EV, A, MAX_JACOBI_TIMES);
    }
    
    /**
     * Calculate eigen value and eigen vector using Jacobi method
     *
     * @param N Time dimension number
     * @param EV Always as true
     * @param A Input matrix data
     * @return Eigen value and eigen vector
     */
    public static Object[] jacobi(int N, boolean EV, Array A) {
        return jacobi(N, EV, A, MAX_JACOBI_TIMES);
    }

    /**
     * Calculate eigen value and eigen vector using Jacobi method
     *
     * @param N Time dimension number
     * @param EV Always as true
     * @param A Input matrix data
     * @param MAXTIMES Maximum iteration times, default is 50
     * @return Eigen value and eigen vector
     */
    public static Object[] jacobi(int N, boolean EV, double[][] A, int MAXTIMES) {
        double[] D = new double[N];
        double[][] V = new double[N][N];
        int IRT = 0;
        double[] B = new double[N];
        double[] Z = new double[N];
        double t;

        if (EV) {
            for (int k = 0; k < N; k++) {
                for (int L = 0; L < N; L++) {
                    if (k - L == 0) {
                        V[k][k] = 1;
                    } else {
                        V[k][L] = 0;
                    }
                }
            }
        }

        for (int k = 0; k < N; k++) {
            B[k] = A[k][k];
            D[k] = B[k];
            Z[k] = 0;
        }

        for (int i = 0; i < MAXTIMES; i++) {
            double sm = 0;
            int N1 = N - 1;
            for (int k = 0; k < N1; k++) {
                int k1 = k + 1;
                for (int L = k1; L < N; L++) {
                    sm = sm + Math.abs(A[k][L]);
                }
            }
            if (sm == 0) {
                return null;
            } else {
                double tresh = 0;
                if (i - 4 > 0) {
                    tresh = 0.2 * sm / (N * N);
                }
                for (int k = 0; k < N1; k++) {
                    int k1 = k + 1;
                    for (int L = k1; L < N; L++) {
                        double G = 100.0 * Math.abs(A[k][L]);
                        if (i > 4 && Math.abs(G) == 0) {
                            A[k][L] = 0;
                            continue;
                        }
                        if (Math.abs(A[k][L]) <= tresh) {
                            continue;
                        }
                        double H = D[L] - D[k];
                        if (G == 0) {
                            t = A[k][L] / H;
                        } else {
                            double theta = 0.5 * H / A[k][L];
                            t = 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + theta * theta));
                            if (theta < 0) {
                                t = -1 * t;
                            }
                        }
                        double c = 1.0 / Math.sqrt(1.0 + t * t);
                        double s = t * c;
                        H = t * A[k][L];
                        Z[k] = Z[k] - H;
                        Z[L] = Z[L] + H;
                        D[k] = D[k] - H;
                        D[L] = D[L] + H;
                        A[k][L] = 0;
                        int KM1 = k - 1;

                        if (KM1 < 0) {
                            for (int j = 0; j < KM1; j++) {
                                G = A[j][k];
                                H = A[j][L];
                                A[j][k] = c * G - s * H;
                                A[j][L] = s * G + c * H;
                            }
                        }

                        int L1 = L - 1;
                        if (L1 - k1 <= 0) {
                            for (int j = k1; j < L1; j++) {
                                G = A[k][L];
                                H = A[j][L];
                                A[k][j] = c * G - s * H;
                                A[j][L] = s * G + c * H;
                            }
                        }

                        L1 = L + 1;
                        if (L1 < N) {
                            for (int j = L1; j < N; j++) {
                                G = A[k][j];
                                H = A[L][j];
                                A[k][j] = c * G - s * H;
                                A[L][j] = s * G + c * H;
                            }
                        }

                        if (EV) {
                            for (int j = 0; j < N; j++) {
                                G = V[j][k];
                                H = V[j][L];
                                V[j][k] = c * G - s * H;
                                V[j][L] = s * G + c * H;
                            }
                        }

                        IRT = IRT + 1;
                    }
                }
                for (int k = 0; k < N; k++) {
                    D[k] = B[k] + Z[k];
                    B[k] = D[k];
                    Z[k] = 0;
                }
            }
        }

        return new Object[]{D, V};
    }
    
    /**
     * Calculate eigen value and eigen vector using Jacobi method
     *
     * @param N Time dimension number
     * @param EV Always as true
     * @param A Input matrix data
     * @param MAXTIMES Maximum iteration times, default is 50
     * @return Eigen value and eigen vector
     */
    public static Object[] jacobi(int N, boolean EV, Array A, int MAXTIMES) {
        Array D = Array.factory(DataType.DOUBLE, new int[]{N});
        Array V = Array.factory(DataType.DOUBLE, new int[]{N, N});
        int IRT = 0;
        double[] B = new double[N];
        double[] Z = new double[N];
        double t;

        if (EV) {
            for (int k = 0; k < N; k++) {
                for (int L = 0; L < N; L++) {
                    if (k - L == 0) {
                        V.setDouble(k * N + k, 1);
                    } else {
                        V.setDouble(k * N + L, 0);
                    }
                }
            }
        }

        for (int k = 0; k < N; k++) {
            B[k] = A.getDouble(k * N + k);
            D.setDouble(k, B[k]);
            Z[k] = 0;
        }

        for (int i = 0; i < MAXTIMES; i++) {
            double sm = 0;
            int N1 = N - 1;
            for (int k = 0; k < N1; k++) {
                int k1 = k + 1;
                for (int L = k1; L < N; L++) {
                    sm = sm + Math.abs(A.getDouble(k * N + L));
                }
            }
            if (sm == 0) {
                break;
            } else {
                double tresh = 0;
                if (i - 4 > 0) {
                    tresh = 0.2 * sm / (N * N);
                }
                for (int k = 0; k < N1; k++) {
                    int k1 = k + 1;
                    for (int L = k1; L < N; L++) {
                        double G = 100.0 * Math.abs(A.getDouble(k * N + L));
                        if (i > 4 && Math.abs(G) == 0) {
                            A.setDouble(k * N + L, 0);
                            continue;
                        }
                        if (Math.abs(A.getDouble(k * N + L)) <= tresh) {
                            continue;
                        }
                        double H = D.getDouble(L) - D.getDouble(k);
                        if (G == 0) {
                            t = A.getDouble(k * N + L) / H;
                        } else {
                            double theta = 0.5 * H / A.getDouble(k * N + L);
                            t = 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + theta * theta));
                            if (theta < 0) {
                                t = -1 * t;
                            }
                        }
                        double c = 1.0 / Math.sqrt(1.0 + t * t);
                        double s = t * c;
                        H = t * A.getDouble(k * N + L);
                        Z[k] = Z[k] - H;
                        Z[L] = Z[L] + H;
                        D.setDouble(k, D.getDouble(k) - H);
                        D.setDouble(L, D.getDouble(L) + H);
                        A.setDouble(k * N + L, 0);
                        int KM1 = k - 1;

                        if (KM1 < 0) {
                            for (int j = 0; j < KM1; j++) {
                                G = A.getDouble(j * N + k);
                                H = A.getDouble(j * N + L);
                                A.setDouble(j * N + k, c * G - s * H);
                                A.setDouble(j * N + L, s * G + c * H);
                            }
                        }

                        int L1 = L - 1;
                        if (L1 - k1 <= 0) {
                            for (int j = k1; j < L1; j++) {
                                G = A.getDouble(k * N + L);
                                H = A.getDouble(j * N + L);
                                A.setDouble(k * N + j, c * G - s * H);
                                A.setDouble(j * N + L, s * G + c * H);
                            }
                        }

                        L1 = L + 1;
                        if (L1 < N) {
                            for (int j = L1; j < N; j++) {
                                G = A.getDouble(k * N + j);
                                H = A.getDouble(L * N + j);
                                A.setDouble(k * N + j, c * G - s * H);
                                A.setDouble(L * N + j, s * G + c * H);
                            }
                        }

                        if (EV) {
                            for (int j = 0; j < N; j++) {
                                G = V.getDouble(j * N + k);
                                H = V.getDouble(j * N + L);
                                V.setDouble(j * N + k, c * G - s * H);
                                V.setDouble(j * N + L, s * G + c * H);
                            }
                        }

                        IRT = IRT + 1;
                    }
                }
                for (int k = 0; k < N; k++) {
                    D.setDouble(k, B[k] + Z[k]);
                    B[k] = D.getDouble(k);
                    Z[k] = 0;
                }
            }
        }

        return new Object[]{D, V};
    }

    public enum DataProMethod {
        NONE,
        DEPARTURE,
        NORMALIZED
    }

    static double[] average(int N, int LL, double[][] f) {
        double[] Aver = new double[N];
        for (int j = 0; j < N; j++) {
            Aver[j] = 0;
            for (int i = 0; i < LL; i++) {
                Aver[j] += f[i][j];
            }
            Aver[j] /= LL;
        }
        return Aver;
    }
    
    static double[] average(int N, int LL, Array f) {
        double[] Aver = new double[N];
        for (int j = 0; j < N; j++) {
            Aver[j] = 0;
            for (int i = 0; i < LL; i++) {
                Aver[j] += f.getDouble(i * N + j);
            }
            Aver[j] /= LL;
        }
        return Aver;
    }

    static double[][] dep(int N, int LL, double[][] f)///距平，N：格点；LL：时间；f原始值；H：距平；W：平均值
    {
        double[][] H = new double[LL][N];

        double[] Aver = average(N, LL, f);

        for (int j = 0; j < N; j++) {
            for (int i = 0; i < LL; i++) {
                H[i][j] = f[i][j] - Aver[j];
            }
        }
        return H;
    }
    
    static Array dep(int N, int LL, Array f)///距平，N：格点；LL：时间；f原始值；H：距平；W：平均值
    {
        Array H = Array.factory(DataType.DOUBLE, f.getShape());
        double[] Aver = average(N, LL, f);

        for (int j = 0; j < N; j++) {
            for (int i = 0; i < LL; i++) {
                H.setDouble(i * N + j, f.getDouble(i * N + j) - Aver[j]);
            }
        }
        return H;
    }

    static double[][] nor(int N, int LL, double[][] f) {
        double[][] H = new double[LL][N];
        double[] Std = new double[N];

        double[] Aver = average(N, LL, f);
        for (int j = 0; j < N; j++) {
            Std[j] = 0;

            for (int i = 0; i < LL; i++) {
                Std[j] += (f[i][j] - Aver[j]) * (f[i][j] - Aver[j]);
            }
            Std[j] = Math.sqrt(Std[j] / LL);
        }

        for (int j = 0; j < N; j++) {
            for (int i = 0; i < LL; i++) {
                H[i][j] = (f[i][j] - Aver[j]) / Std[j];
            }
        }

        return H;
    }
    
    static Array nor(int N, int LL, Array f) {
        Array H = Array.factory(DataType.DOUBLE, f.getShape());
        double[] Std = new double[N];

        double[] Aver = average(N, LL, f);
        for (int j = 0; j < N; j++) {
            Std[j] = 0;

            for (int i = 0; i < LL; i++) {
                Std[j] += (f.getDouble(i * N + j) - Aver[j]) * (f.getDouble(i * N + j) - Aver[j]);
            }
            Std[j] = Math.sqrt(Std[j] / LL);
        }

        for (int j = 0; j < N; j++) {
            for (int i = 0; i < LL; i++) {
                H.setDouble(i * N + j, (f.getDouble(i * N + j) - Aver[j]) / Std[j]);
            }
        }

        return H;
    }

}
