package org.meteoinfo.math.special;

import org.meteoinfo.math.Function;

import java.util.HashMap;
import java.util.Map;

/*
 * Airy function, its derivative, and its zeros by J E Hasbun for the OSP library.
 * Refs:
 * "A Numerical Library in Java for Scientists Engineers", H. T. Lau (CRC Press, 2007)
 * http://en.wikipedia.org/wiki/Airy_function,
 * ai=airy function at z
 * aid=airy function derivative at z
 * ra0=nt zeros of the Airy function
 *
 * Zero map added by Wolfgang Christian
 *
 * @author Javier E Hasbun 2009.
 */
public class Airy {
    static Function airyFunction = null;
    static Function airyDerivative = null;
    static final double airyZeroTolerance = 1.0e-9;
    static final Map<Integer, Double> zeroMap = new HashMap<Integer, Double>();

    static { // put first 16 zeros into map
        zeroMap.put(1, -2.338107410459767);
        zeroMap.put(2, -4.087949444130971);
        zeroMap.put(3, -5.520559828095551);
        zeroMap.put(4, -6.786708090071759);
        zeroMap.put(5, -7.944133587120853);
        zeroMap.put(6, -9.022650853340980);
        zeroMap.put(7, -10.04017434155809);
        zeroMap.put(8, -11.00852430373326);
        zeroMap.put(9, -11.93601556323626);
        zeroMap.put(10, -12.82877675286576);
        zeroMap.put(11, -13.69148903521072);
        zeroMap.put(12, -14.52782995177533);
        zeroMap.put(13, -15.34075513597800);
        zeroMap.put(14, -16.13268515694577);
        zeroMap.put(15, -16.90563399742994);
        zeroMap.put(16, -17.66130010569706);
    }

    /**
     * Computes the Airy function at x.
     *
     * @param x
     * @return
     */
    public static double airy(double x) {
        int n, l;
        double s, t, u, v, sc, tc, uc, vc, k1, k2, k3, k4, c, xt, si, co, expxt;
        double sqrtx, wwl, pl, pl1, pl2, pl3, zzz, ai;
        double[] xtmp = new double[26];
        xtmp[1] = 1.4083081072180964e1;
        xtmp[2] = 1.0214885479197331e1;
        xtmp[3] = 7.4416018450450930;
        xtmp[4] = 5.3070943061781927;
        xtmp[5] = 3.6340135029132462;
        xtmp[6] = 2.3310652303052450;
        xtmp[7] = 1.3447970842609268;
        xtmp[8] = 6.4188858369567296e-1;
        xtmp[9] = 2.0100345998121046e-1;
        xtmp[10] = 8.0594359172052833e-3;
        xtmp[11] = 3.1542515762964787e-14;
        xtmp[12] = 6.6394210819584921e-11;
        xtmp[13] = 1.7583889061345669e-8;
        xtmp[14] = 1.3712392370435815e-6;
        xtmp[15] = 4.4350966639284350e-5;
        xtmp[16] = 7.1555010917718255e-4;
        xtmp[17] = 6.4889566103335381e-3;
        xtmp[18] = 3.6440415875773282e-2;
        xtmp[19] = 1.4399792418590999e-1;
        xtmp[20] = 8.1231141336261486e-1;
        xtmp[21] = 0.355028053887817;
        xtmp[22] = 0.258819403792807;
        xtmp[23] = 1.73205080756887729;
        xtmp[24] = 0.78539816339744831;
        xtmp[25] = 0.56418958354775629;
        if ((x >= -5.0) && (x <= 8.0)) {
            u = v = t = uc = vc = tc = 1.0;
            s = sc = 0.5;
            n = 3;
            zzz = x * x * x;
            while (Math.abs(u) + Math.abs(v) + Math.abs(s) + Math.abs(t) > 1.0e-18) {
                u = u * zzz / (n * (n - 1));
                v = v * zzz / (n * (n + 1));
                s = s * zzz / (n * (n + 2));
                t = t * zzz / (n * (n - 2));
                uc += u;
                vc += v;
                sc += s;
                tc += t;
                n += 3;
            }
            if (x < 2.5) {
                ai = xtmp[21] * uc - xtmp[22] * x * vc;
                return ai;
            }
        }
        k1 = k2 = k3 = k4 = 0.0;
        sqrtx = Math.sqrt(Math.abs(x));
        xt = 0.666666666666667 * Math.abs(x) * sqrtx;
        c = xtmp[25] / Math.sqrt(sqrtx);
        if (x < 0.0) {
            x = -x;
            co = Math.cos(xt - xtmp[24]);
            si = Math.sin(xt - xtmp[24]);
            for (l = 1; l <= 10; l++) {
                wwl = xtmp[l + 10];
                pl = xtmp[l] / xt;
                pl2 = pl * pl;
                pl1 = 1.0 + pl2;
                pl3 = pl1 * pl1;
                k1 += wwl / pl1;
                k2 += wwl * pl / pl1;
                k3 += wwl * pl * (1.0 + pl * (2.0 / xt + pl)) / pl3;
                k4 += wwl * (-1.0 - pl * (1.0 + pl * (xt - pl)) / xt) / pl3;
            }
            ai = c * (co * k1 + si * k2);
        } else {
            if (x < 9.0) {
                expxt = Math.exp(xt);
            } else {
                expxt = 1.0;
            }
            for (l = 1; l <= 10; l++) {
                wwl = xtmp[l + 10];
                pl = xtmp[l] / xt;
                pl1 = 1.0 + pl;
                pl2 = 1.0 - pl;
                k1 += wwl / pl1;
                k2 += wwl * pl / (xt * pl1 * pl1);
                k3 += wwl / pl2;
                k4 += wwl * pl / (xt * pl2 * pl2);
            }
            ai = 0.5 * c * k1 / expxt;
            if (x >= 9.0) {
                // Asymptotic behavior follows
                expxt = Math.pow(x, 3. / 2.);
                ai = 0.5 * Math.exp(-2.0 * expxt / 3.0) / Math.sqrt(Math.PI) / Math.pow(x, 0.25);
            }
        }
        return ai;
    }

    /**
     * Computes the derivative of the Airy function at x.
     *
     * @param x
     * @return
     */
    public static double airyDerivative(double x) {
        int n, l;
        double s, t, u, v, sc, tc, uc, vc, k1, k2, k3, k4, c, xt, si, co, expxt;
        double sqrtx, wwl, pl, pl1, pl2, pl3, zzz, ai, aid;
        double[] xtmp = new double[26];
        xtmp[1] = 1.4083081072180964e1;
        xtmp[2] = 1.0214885479197331e1;
        xtmp[3] = 7.4416018450450930;
        xtmp[4] = 5.3070943061781927;
        xtmp[5] = 3.6340135029132462;
        xtmp[6] = 2.3310652303052450;
        xtmp[7] = 1.3447970842609268;
        xtmp[8] = 6.4188858369567296e-1;
        xtmp[9] = 2.0100345998121046e-1;
        xtmp[10] = 8.0594359172052833e-3;
        xtmp[11] = 3.1542515762964787e-14;
        xtmp[12] = 6.6394210819584921e-11;
        xtmp[13] = 1.7583889061345669e-8;
        xtmp[14] = 1.3712392370435815e-6;
        xtmp[15] = 4.4350966639284350e-5;
        xtmp[16] = 7.1555010917718255e-4;
        xtmp[17] = 6.4889566103335381e-3;
        xtmp[18] = 3.6440415875773282e-2;
        xtmp[19] = 1.4399792418590999e-1;
        xtmp[20] = 8.1231141336261486e-1;
        xtmp[21] = 0.355028053887817;
        xtmp[22] = 0.258819403792807;
        xtmp[23] = 1.73205080756887729;
        xtmp[24] = 0.78539816339744831;
        xtmp[25] = 0.56418958354775629;
        if ((x >= -5.0) && (x <= 8.0)) {
            u = v = t = uc = vc = tc = 1.0;
            s = sc = 0.5;
            n = 3;
            zzz = x * x * x;
            while (Math.abs(u) + Math.abs(v) + Math.abs(s) + Math.abs(t) > 1.0e-18) {
                u = u * zzz / (n * (n - 1));
                v = v * zzz / (n * (n + 1));
                s = s * zzz / (n * (n + 2));
                t = t * zzz / (n * (n - 2));
                uc += u;
                vc += v;
                sc += s;
                tc += t;
                n += 3;
            }
            if (x < 2.5) {
                ai = xtmp[21] * uc - xtmp[22] * x * vc;
                aid = xtmp[21] * sc * x * x - xtmp[22] * tc;
                return aid;
            }
        }
        k1 = k2 = k3 = k4 = 0.0;
        sqrtx = Math.sqrt(Math.abs(x));
        xt = 0.666666666666667 * Math.abs(x) * sqrtx;
        c = xtmp[25] / Math.sqrt(sqrtx);
        if (x < 0.0) {
            x = -x;
            co = Math.cos(xt - xtmp[24]);
            si = Math.sin(xt - xtmp[24]);
            for (l = 1; l <= 10; l++) {
                wwl = xtmp[l + 10];
                pl = xtmp[l] / xt;
                pl2 = pl * pl;
                pl1 = 1.0 + pl2;
                pl3 = pl1 * pl1;
                k1 += wwl / pl1;
                k2 += wwl * pl / pl1;
                k3 += wwl * pl * (1.0 + pl * (2.0 / xt + pl)) / pl3;
                k4 += wwl * (-1.0 - pl * (1.0 + pl * (xt - pl)) / xt) / pl3;
            }
            ai = c * (co * k1 + si * k2);
            aid = 0.25 * ai / x - c * sqrtx * (co * k3 + si * k4);
        } else {
            if (x < 9.0) {
                expxt = Math.exp(xt);
            } else {
                expxt = 1.0;
            }
            for (l = 1; l <= 10; l++) {
                wwl = xtmp[l + 10];
                pl = xtmp[l] / xt;
                pl1 = 1.0 + pl;
                pl2 = 1.0 - pl;
                k1 += wwl / pl1;
                k2 += wwl * pl / (xt * pl1 * pl1);
                k3 += wwl / pl2;
                k4 += wwl * pl / (xt * pl2 * pl2);
            }
            ai = 0.5 * c * k1 / expxt;
            aid = ai * (-0.25 / x - sqrtx) + 0.5 * c * sqrtx * k2 / expxt;
            if (x >= 9) {
                // Asymptotic behavior follows
                expxt = Math.pow(x, 3. / 2.);
                ai = 0.5 * Math.exp(-2.0 * expxt / 3.0) / Math.sqrt(Math.PI) / Math.pow(x, 0.25);
                aid = -ai * Math.pow(x, 0.5) - ai / x / 4.0;
            }
        }
        return aid;
    }

    /**
     * Computes the n-th zero of the Airy function.
     *
     * @param n
     * @return
     */
    public static double airyZero(int n) {
        // Referring to http://en.wikipedia.org/wiki/Airy_function,
        // for large -x, ai(-x)~sin((2/3)x^(3/2)+pi/4)/(sqrt(pi)*x^(1/4))
        // so, set sin((2/3)(-x)^(3/2)+pi/4)~n*pi and solve for x to get
        // x~-(pi*(n-1./4.)*3.0/2.0)^(2.0/3.0);
        if (zeroMap.containsKey(n)) {
            return zeroMap.get(n);
        }
        double x = -Math.pow(Math.PI * (n - .25) * 3.0 / 2.0, 2.0 / 3.0); // estimate zero
        int maxIterations = 10;
        while (maxIterations > 0) {
            double x0 = x;
            double ax = airy(x);
            double dax = airyDerivative(x);
            x = x - ax / dax; // Newton-Raphson step
            if (!(Math.abs(x - x0) > airyZeroTolerance)) {
                break;
            }
            maxIterations--;
        }
        if (maxIterations == 0) {
            x = -Math.pow(Math.PI * (n - 0.25) * 3.0 / 2.0, 2.0 / 3.0); // should not happen; use approximate value if it does
        }
        zeroMap.put(n, x); // store value for future use
        return x;
    }

    /**
     * Gets nt zeroes of Airy function
     *
     * @param nt
     * @return
     */
    public static double[] airynZeros(int nt) {
        double[] zeros = new double[nt];
        for (int i = 0; i < nt; i++) {
            zeros[i] = airyZero(i + 1);
        }
        return zeros;
    }

    /**
     * Gets the singleton Airy function.
     */
    public static synchronized Function getFunction() {
        if (airyFunction == null) {
            airyFunction = new Airy.AiryFunction(); // create the singleton
        }
        return airyFunction;
    }

    /**
     * Gets the singleton derivative of the Airy function.
     *
     * @return the derivative
     */
    public static synchronized Function getDerivative() {
        if (airyDerivative == null) {
            airyDerivative = new Airy.AiryDerivative(); // create the singleton
        }
        return airyDerivative;
    }

    /**
     * Computes the Airy function
     */
    static class AiryFunction implements Function {
        AiryFunction() {
        }

        /**
         * Evaluates the Airy function.
         */
        public double evaluate(final double x) {
            return Airy.airy(x);
        }

    }

    /**
     * Computes the derivative of the Airy function
     */
    static class AiryDerivative implements Function {
        AiryDerivative() {
        }

        /**
         * Evaluates the derivative of the Airy function.
         */
        public double evaluate(final double x) {
            return Airy.airyDerivative(x);
        }

    }

}
