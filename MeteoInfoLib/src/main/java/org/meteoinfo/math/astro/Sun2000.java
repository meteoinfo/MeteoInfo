/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.astro;

/**
 * Solar ephemerides ecliptical, geozentric solar coordinates equinox of date
 * T2000 in Julian centuries Translated from fortran code by Chris Wehrli
 *
 * @author Yaqiang Wang
 */
public class Sun2000 {

    double T;
    double DL = 0.0, DR = 0.0, DB = 0.0;
    double[] C3 = new double[9];
    double[] S3 = new double[9];
    double[] C = new double[9];
    double[] S = new double[9];
    double u = 0., v = 0.;

    public double[] run(double t2000) {
        double P2 = 8.0 * Math.atan(1.0);
        T = t2000; // make it value parameter
        double M2 = P2 * dfrac(0.1387306 + 162.5485917 * T);
        double M3 = P2 * dfrac(0.9931266 + 99.9973604 * T);
        double M4 = P2 * dfrac(0.0543250 + 53.1666028 * T);
        double M5 = P2 * dfrac(0.0551750 + 8.4293972 * T);
        double M6 = P2 * dfrac(0.8816500 + 3.3938722 * T);
        double D = P2 * dfrac(0.8274 + 1236.8531 * T);
        double A = P2 * dfrac(0.3749 + 1325.5524 * T);
        double UU = P2 * dfrac(0.2591 + 1342.2278 * T);
        C3[1] = 1.0;
        C3[2] = Math.cos(M3);
        C3[0] = C3[2];
        S3[1] = 0.0;
        S3[2] = Math.sin(M3);
        S3[0] = -S3[2];
        double[] rr;
        for (int i = 2; i <= 7; i++) {
            rr = addThe(C3[i], S3[i], C3[2], S3[2]);
            C3[i + 1] = rr[0];
            S3[i + 1] = rr[1];
        }

        //PERTVEN  (* Keplerterme und Stoerungen durch Venus *)
        C[8] = 1.0;
        S[8] = 0.0;
        S[7] = -Math.sin(M2);
        for (int i = 7; i >= 3; i -= 1) {
            rr = addThe(C[i], S[i], C[7], S[7]);
            C[i - 1] = rr[0];
            S[i - 1] = rr[1];
        }

        term(1, 0, 0, -0.22, 6892.76, -16707.37, -0.54, 0.00, 0.00);
        term(1, -2, 0, -1.66, 0.62, 0.16, 0.28, 0.00, 0.00);
        term(2, -2, 0, 1.96, 0.57, -1.32, 4.55, 0.00, 0.01);
        term(2, -3, 0, 0.40, 0.15, -0.17, 0.46, 0.00, 0.00);
        term(2, -4, 0, 0.53, 0.26, 0.09, -0.22, 0.00, 0.00);
        term(3, -3, 0, 0.05, 0.12, -0.35, 0.15, 0.00, 0.00);
        term(3, -4, 0, -0.13, -0.48, 1.06, -0.29, 0.01, 0.00);
        term(3, -5, 0, -0.04, -0.20, 0.20, -0.04, 0.00, 0.00);
        term(4, -4, 0, 0.00, -0.03, 0.10, 0.04, 0.00, 0.00);
        term(4, -5, 0, 0.05, -0.07, 0.20, 0.14, 0.00, 0.00);
        term(4, -6, 0, -0.10, 0.11, -0.23, -0.22, 0.00, 0.00);
        term(5, -7, 0, -0.05, 0.00, 0.01, -0.14, 0.00, 0.00);
        term(5, -8, 0, 0.05, 0.01, -0.02, 0.10, 0.00, 0.00);

        //PERTMAR  (* Stoerungen durch Mars *)
        C[7] = Math.cos(M4);
        S[7] = -Math.sin(M4);
        for (int i = 7; i >= 1; i -= 1) {
            rr = addThe(C[i], S[i], C[7], S[7]);
            C[i - 1] = rr[0];
            S[i - 1] = rr[1];
        }
        term(1, -1, 0, -0.22, 0.17, -0.21, -0.27, 0.00, 0.00);
        term(1, -2, 0, -1.66, 0.62, 0.16, 0.28, 0.00, 0.00);
        term(2, -2, 0, 1.96, 0.57, -1.32, 4.55, 0.00, 0.01);
        term(2, -3, 0, 0.40, 0.15, -0.17, 0.46, 0.00, 0.00);
        term(2, -4, 0, 0.53, 0.26, 0.09, -0.22, 0.00, 0.00);
        term(3, -3, 0, 0.05, 0.12, -0.35, 0.15, 0.00, 0.00);
        term(3, -4, 0, -0.13, -0.48, 1.06, -0.29, 0.01, 0.00);
        term(3, -5, 0, -0.04, -0.20, 0.20, -0.04, 0.00, 0.00);
        term(4, -4, 0, 0.00, -0.03, 0.10, 0.04, 0.00, 0.00);
        term(4, -5, 0, 0.05, -0.07, 0.20, 0.14, 0.00, 0.00);
        term(4, -6, 0, -0.10, 0.11, -0.23, -0.22, 0.00, 0.00);
        term(5, -7, 0, -0.05, 0.00, 0.01, -0.14, 0.00, 0.00);
        term(5, -8, 0, 0.05, 0.01, -0.02, 0.10, 0.00, 0.00);

        //PERTJUP  (* Stoerungen durch Jupiter *)
        C[7] = Math.cos(M5);
        S[7] = -Math.sin(M5);
        for (int i = 7; i >= 5; i -= 1) {
            rr = addThe(C[i], S[i], C[7], S[7]);
            C[i - 1] = rr[0];
            S[i - 1] = rr[1];
        }

        term(1, -1, 0, 0.01, 0.07, 0.18, -0.02, 0.00, -0.02);
        term(0, -1, 0, -0.31, 2.58, 0.52, 0.34, 0.02, 0.00);
        term(1, -1, 0, -7.21, -0.06, 0.13, -16.27, 0.00, -0.02);
        term(1, -2, 0, -0.54, -1.52, 3.09, -1.12, 0.01, -0.17);
        term(1, -3, 0, -0.03, -0.21, 0.38, -0.06, 0.00, -0.02);
        term(2, -1, 0, -0.16, 0.05, -0.18, -0.31, 0.01, 0.00);
        term(2, -2, 0, 0.14, -2.73, 9.23, 0.48, 0.00, 0.00);
        term(2, -3, 0, 0.07, -0.55, 1.83, 0.25, 0.01, 0.00);
        term(2, -4, 0, 0.02, -0.08, 0.25, 0.06, 0.00, 0.00);
        term(3, -2, 0, 0.01, -0.07, 0.16, 0.04, 0.00, 0.00);
        term(3, -3, 0, -0.16, -0.03, 0.08, -0.64, 0.00, 0.00);
        term(3, -4, 0, -0.04, -0.01, 0.03, -0.17, 0.00, 0.00);

        //PERTSAT  (* Stoerungen durch Saturn *)
        C[7] = Math.cos(M6);
        S[7] = -Math.sin(M6);
        rr = addThe(C[7], S[7], C[7], S[7]);
        C[6] = rr[0];
        S[6] = rr[0];
        term(0, -1, 0, 0.00, 0.32, 0.01, 0.00, 0.00, 0.00);
        term(1, -1, 0, -0.08, -0.41, 0.97, -0.18, 0.00, -0.01);
        term(1, -2, 0, 0.04, 0.10, -0.23, 0.10, 0.00, 0.00);
        term(2, -2, 0, 0.04, 0.10, -0.35, 0.13, 0.00, 0.00);

        //PERTMOO  (* Differenz Erde-Mond-Schwerpunkt zu Erdmittelpunkt *)
        DL = DL + 6.45 * Math.sin(D) - 0.42 * Math.sin(D - A) + 0.18 * Math.sin(D + A)
                + 0.17 * Math.sin(D - M3) - 0.06 * Math.sin(D + M3);
        DR = DR + 30.76 * Math.cos(D) - 3.06 * Math.cos(D - A) + 0.85 * Math.cos(D + A)
                - 0.58 * Math.cos(D + M3) + 0.57 * Math.cos(D - M3);
        DB = DB + 0.576 * Math.sin(UU);
        DL = DL + 6.40 * Math.sin(P2 * (0.6983 + 0.0561 * T)) + 1.87 * Math.sin(P2
                * (0.5764 + 0.4174 * T)) + 0.27 * Math.sin(P2 * (0.4189 + 0.3306 * T))
                + 0.20 * Math.sin(P2 * (0.3581 + 2.4814 * T));
        double L = 3.6E2 * dfrac(0.7859453 + M3 / P2 + ((6191.2 + 1.1 * T) * T + DL) / 1296.0E3);
        double R = 1.0001398 - 7.0E-7 * T + DR * 1.0E-6;
        double B = DB / 3.6E3;
        return new double[]{B, L, R};
    }

    private static double dfrac(double x) {
        double df = x % 1.0;
        return df;
    }

    private double[] addThe(double C1, double S1, double C2, double S2) {
        double C = C1 * C2 - S1 * S2;
        double S = S1 * C2 + C1 * S2;
        return new double[]{C, S};
    }

    private void term(int i1, int i, int it, double DLC, double DLS,
            double DRC, double DRS, double DBC, double DBS) {
        i1 += 1;
        i += 8;
        double[] rr;
        if (it == 0) {
            rr = addThe(C3[i1], S3[i1], C[i], S[i]);
            u = rr[0];
            v = rr[1];
        } else {
            u = u * T;
            v = v * T;
        }
        DL = DL + DLC * u + DLS * v;
        DR = DR + DRC * u + DRS * v;
        DB = DB + DBC * u + DBS * v;
    }

}
