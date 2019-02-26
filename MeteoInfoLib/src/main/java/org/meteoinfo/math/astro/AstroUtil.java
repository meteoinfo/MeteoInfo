/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.astro;

/**
 *
 * @author Yaqiang Wang
 */
public class AstroUtil {

    /**
     * Calculate modified julian day (JD - 2400000.5)
     *
     * @param year Year
     * @param month Month
     * @param day Day
     * @param noon Local mean time noon in UT (12. - longitude / 15.)
     * @return
     */
    public static double MJD(int year, int month, int day, double noon) {
        double A = 1.E04 * year + 1.E02 * month + 1. * day;
        if (month <= 2) {
            month = month + 12;
            year = year - 1;
        }
        double B;
        if (A <= 15821004.1) {
            B = -2 + (year + 4716) / 4 - 1179;
        } else {
            B = year / 400 - year / 100 + year / 4;
        }

        A = 3.65E02 * year - 6.79004E05;
        double mjd = A + B + (int) (30.6001 * (month + 1)) + day + noon / 24.;
        return mjd;
    }

    /**
     * Inverse procedure to MJD
     *
     * @param mjd MJD
     * @return Array of year, month, day, hour
     */
    public static Object[] CALDAT(double mjd) {
        double jd = mjd + 2.4000005E06;
        double jd0 = (int) (jd + 5.0E-1);
        int b;
        double c;
        if (jd0 < 2.299161E06) {
            b = 0;
            c = jd0 + 1.524E03;
        } else {
            b = (int) ((jd0 - 1.86721625E06) / 3.652425E04);
            c = jd0 + (b - b / 4) + 1.525E03;
        }
        int d = (int) ((c - 1.221E02) / 3.6525E02);
        double e = 3.65E02 * d + (d / 4);
        int f = (int) ((c - e) / 3.06001E01);
        int day = (int) ((c - e + 5.0E-1) - (int) (3.06001E01 * f));
        int mon = (int) (f - 1 - 12 * (int) (f / 14));
        int yr = (int) (d - 4715 - (int) ((7 + mon) / 10));
        double hr = 24.0 * (jd + 0.5 - jd0);
        return new Object[]{yr, mon, day, hr};
    }

    /**
     * Local mean sideral time [decimal hours] single precision longitude, west
     * positiv
     *
     * @param mjd Modified Julian day
     * @param lon Longitude
     * @return Local mean sideral time
     */
    public static double LMST(double mjd, double lon) {
        double mjd0 = (int) mjd;
        double ut = (mjd - mjd0) * 24.0;
        double t = (mjd0 - 5.15445E+04) / 3.6525E+4;
        double gmst = 6.697374558 + 1.0027379093 * ut
                + (8640184.812866 + (9.3104E-2 - 6.2D - 6 * t) * t) * t / 3.6E+3;
        t = 24. * dfrac((gmst - (lon / 15.0) / 24.));
        if (t < 0.0) {
            t = t + 24.0;
        }
        return t;
    }

    private static double dfrac(double x) {
        double df = x % 1.0;
        return df;
    }

    /**
     * Greenwich Mean Sideral to Universal time
     *
     * @param mjd Modified Julian day
     * @param gmst Greenwich Mean Sideral time.
     * @return Unversal time
     */
    public static double GMST2UT(double mjd, double gmst) {
        double MJD0 = (int) mjd;
        double T = (mjd - 51544.5) / 36525.0;
        double T0 = (MJD0 - 51544.5) / 36525.0;
        double UT = (gmst - 6.697374558 - (8640184.812866 * T0 + (0.093104 - 6.2E-6 * T) * T) / 3600.0);
        UT = 24.0 * (UT / 24. % 1.);
        if (UT < 0.) {
            UT = UT + 24.;
        }
        double r = UT / 1.0027379093;
        return r;
    }

    /**
     * Solar ephemerides
     *
     * @param t2000
     * @return
     */
    public static double[] sun2000(double t2000) {
        Sun2000 sun2 = new Sun2000();
        return sun2.run(t2000);
    }

    /**
     * !鋛uatoriale Sonnenkoordinaten Rektaszension RA, Deklination in Grad !Rad
     * in Erdradien, T in julian.Jhdt. seit J2000 !Die Koordinaten beziehen sich
     * auf das wahre Aequinoktium des Datums
     *
     * @param T
     * @return
     */
    public static double[] sunEqu(double T) {
        double dt, l, b, x, y, z;
        double ra, dec, rad;
        dt = (8.32 / 1440.0) / 3.6525E4; // Retardierung um 8.32 Minuten Lichtlaufzeit
        double[] rr;
        rr = sun2000(T - dt);  // Ekliptikale Kooerdinaten
        l = rr[0];
        b = rr[1];
        rad = rr[2];
        rr = cart(rad, b, l);    // polar -> kartesisch
        x = rr[0];
        y = rr[1];
        z = rr[2];
        rr = eclEqu(T, x, y, z);        // Ekliptik -> Aequator
        x = rr[0];
        y = rr[1];
        z = rr[2];
        rr = nutEqu(T, x, y, z);		// Nutation
        x = rr[0];
        y = rr[1];
        z = rr[2];
        rr = polar(x, y, z); // kartesisch -> polar
        rad = rr[0];
        dec = rr[1];
        ra = rr[2];
        return new double[]{ra, dec, rad};
    }

    private static double cs(double x) {
        return Math.cos(x * 1.745329252E-02);
    }

    private static double sn(double x) {
        return Math.sin(x * 1.745329252E-02);
    }

    private static double[] cart(double r, double theta, double phi) {
        double rcst = r * cs(theta);
        double x = rcst * cs(phi);
        double y = rcst * sn(phi);
        double z = r * sn(theta);
        return new double[]{x, y, z};
    }

    /**
     * !Umwandlung ekliptikaler in 鋛uatoriale Koordinaten !T in Jhdt. seit J2000
     * X-Achse ist gemeinsam
     *
     * @param T
     * @param x
     * @param y
     * @param z
     */
    private static double[] eclEqu(double T, double x, double y, double z) {
        //Schiefe der Ekliptik      
        double eps = 23.43929111 - (46.815 + (5.9E-4 - 1.813E-3 * T) * T) * T / 3.6E3;
        double c = cs(eps);
        double s = sn(eps);
        x = x;
        double v = c * y - s * z;
        z = s * y + c * z;
        y = v;
        return new double[]{x, y, z};
    }

    private static double[] nutEqu(double T, double x, double y, double z) {
        double LS, D, F, N, eps, dpsi, deps, c, s, dx, dy, dz;
        double P2, arc;

        arc = 162.0E03 / Math.atan(1.0);
        P2 = 8. * Math.atan(1.0);

        LS = P2 * dfrac(0.993133 + 99.997306 * T); // mittl.Anomalie Sonne    
        D = P2 * dfrac(0.827362 + 1236.853087 * T); // Diff. L鋘ge Mond-Sonne  
        F = P2 * dfrac(0.259089 + 1342.227826 * T);  // Knotenabstand           
        N = P2 * dfrac(0.347346 - 5.372447 * T);  // L鋘ge des aufst.Knotes
        eps = 0.4090928 - 2.2696E-4 * T;              // Ekliptikschiefe         
        dpsi = (-17.200 * Math.sin(N) - 1.319 * Math.sin(2 * (F - D + N))
                - 0.227 * Math.sin(2 * (F + N)) + 0.206 * Math.sin(2 * N) + 0.143 * Math.sin(LS));
        dpsi = dpsi / arc;
        deps = (+9.203 * Math.cos(N) + 0.574 * Math.cos(2 * (F - D + N)) + 0.098 * Math.cos(2 * (F + N))
                - 0.090 * Math.cos(2 * N)) / arc;
        c = dpsi * Math.cos(eps);
        s = dpsi * Math.sin(eps);
        dx = -(c * y + s * z);
        dy = (c * x - deps * z);
        dz = (s * x + deps * y);
        x = x + dx;
        y = y + dy;
        z = z + dz;
        return new double[]{x, y, z};
    }

    private static double[] polar(double x, double y, double z) {
        double r, theta, phi;
        double r2d = 5.729577951E+01;
        double rho = Math.pow(x, 2) + Math.pow(y, 2);
        r = Math.sqrt(rho + Math.pow(z, 2));
        phi = Math.atan2(y, x) * r2d;
        if (phi < 0.) {
            phi = phi + 3.6E2;
        }
        rho = Math.sqrt(rho);
        theta = Math.atan2(z, rho) * r2d;
        return new double[]{r, theta, phi};
    }

    /**
     * my own evaluation of TOMS monthly zonal averages from 1989 until 1999
     ! 36 latitudinal bands of 5?and 12 months, zero in polar nights
     * @param month Month
     * @param latitude Latitude
     * @return Ozone value
     */
    public static double TOMSozone(int month, double latitude) {
        double[] latcntr = {-87.5, -82.5, -77.5, -72.5, -67.5, -62.5, -57.5, -52.5,
            -47.5, -42.5, -37.5, -32.5, -27.5, -22.5, -17.5, -12.5, -7.5, -2.5,
            +2.5, +7.5, 12.5, 17.5, 22.5, 27.5, 32.5, 37.5, 42.5, 47.5, 52.5,
            57.5, 62.5, 67.5, 72.5, 77.5, 82.5, 87.5};
        double[][] TOMS = {{291.0, 278.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 153.3, 218.4, 289.9},
        {294.3, 284.0, 288.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 163.9, 228.0, 294.0},
        {298.4, 288.0, 288.2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 183.3, 244.4, 298.7},
        {304.7, 295.5, 293.3, 0.0, 0.0, 0.0, 0.0, 0.0, 202.2, 216.7, 272.2, 306.5},
        {313.0, 305.4, 298.4, 295.6, 0.0, 0.0, 0.0, 258.0, 238.9, 263.8, 305.7, 316.0},
        {319.8, 309.8, 300.6, 299.9, 302.1, 0.0, 0.0, 292.0, 289.6, 313.7, 332.7, 322.9},
        {320.6, 308.6, 299.2, 300.8, 306.5, 315.6, 319.5, 323.2, 332.5, 347.5, 345.5, 326.0},
        {314.6, 302.5, 293.2, 297.0, 306.5, 316.0, 322.4, 339.9, 351.2, 358.5, 346.3, 323.4},
        {304.2, 293.7, 285.8, 289.3, 302.0, 313.1, 322.7, 343.1, 354.2, 355.4, 339.4, 315.4},
        {293.2, 285.6, 280.9, 281.3, 293.5, 306.9, 320.0, 338.4, 348.1, 345.7, 328.5, 305.3},
        {284.6, 279.2, 277.0, 275.0, 283.4, 296.2, 310.5, 325.7, 335.3, 333.1, 316.9, 296.5},
        {276.8, 272.9, 272.3, 270.7, 273.5, 283.2, 295.6, 307.3, 317.3, 318.0, 304.9, 289.1},
        {270.6, 267.0, 266.4, 265.8, 265.6, 270.4, 279.5, 288.5, 299.4, 302.5, 293.8, 281.7},
        {266.5, 263.3, 262.1, 260.9, 259.6, 260.9, 267.5, 274.9, 285.5, 290.0, 284.2, 274.7},
        {264.1, 262.0, 261.1, 258.3, 255.8, 255.0, 259.8, 265.7, 274.2, 280.0, 275.7, 269.3},
        {262.1, 261.5, 261.8, 259.8, 256.0, 253.6, 257.1, 260.7, 268.0, 272.6, 269.9, 265.5},
        {259.4, 260.1, 262.5, 262.4, 259.5, 257.6, 260.2, 262.0, 268.0, 269.9, 265.9, 262.0},
        {256.0, 258.0, 262.5, 264.6, 263.2, 263.5, 266.7, 267.4, 271.2, 269.3, 262.9, 258.6},
        {252.0, 254.7, 260.9, 265.2, 265.9, 268.0, 271.9, 272.3, 273.4, 268.4, 260.1, 255.1},
        {246.4, 249.4, 257.1, 264.9, 269.6, 273.0, 277.3, 277.7, 276.2, 268.9, 258.5, 250.8},
        {243.2, 246.3, 256.3, 266.8, 273.8, 277.0, 280.5, 280.3, 277.2, 268.5, 256.3, 247.2},
        {244.5, 248.9, 260.6, 273.1, 280.9, 282.3, 283.4, 280.8, 276.3, 267.6, 255.5, 246.9},
        {251.0, 257.8, 270.3, 284.0, 291.8, 289.7, 288.3, 283.4, 277.2, 268.9, 258.3, 251.0},
        {263.8, 272.4, 285.2, 297.8, 304.2, 297.2, 293.2, 287.5, 280.4, 272.5, 263.2, 260.0},
        {284.8, 295.7, 306.9, 315.3, 318.0, 306.4, 298.6, 292.2, 284.5, 276.6, 270.5, 275.1},
        {313.0, 325.3, 333.1, 337.9, 335.1, 320.5, 306.8, 297.5, 289.6, 282.3, 281.6, 295.9},
        {338.9, 351.6, 358.1, 359.5, 352.5, 338.5, 319.3, 305.3, 297.0, 290.4, 295.4, 316.3},
        {358.4, 374.1, 380.7, 376.5, 365.9, 353.0, 331.9, 314.2, 304.9, 298.8, 307.8, 332.0},
        {370.7, 390.1, 397.7, 388.7, 375.4, 360.7, 340.2, 320.6, 309.8, 305.1, 316.2, 342.1},
        {374.1, 399.1, 408.5, 397.4, 383.1, 361.5, 340.6, 322.2, 309.8, 307.8, 319.4, 342.6},
        {0.0, 401.4, 414.9, 405.4, 389.0, 356.8, 334.2, 318.0, 305.4, 306.4, 318.2, 0.0},
        {0.0, 422.2, 417.4, 413.1, 395.4, 354.6, 327.1, 310.9, 300.0, 301.7, 0.0, 0.0},
        {0.0, 0.0, 411.4, 416.2, 401.7, 358.9, 325.3, 304.3, 294.2, 297.9, 0.0, 0.0},
        {0.0, 0.0, 404.5, 415.9, 403.0, 363.0, 326.2, 299.8, 287.2, 0.0, 0.0, 0.0},
        {0.0, 0.0, 0.0, 415.5, 402.7, 364.9, 328.0, 295.9, 284.2, 0.0, 0.0, 0.0},
        {0.0, 0.0, 0.0, 411.1, 399.2, 361.7, 327.2, 291.2, 0.0, 0.0, 0.0, 0.0}};

        double ozone = 0.;
        if (Math.abs(latitude) < 90.0 && month >= 1 && month <= 12) {
            int lindex = (int) ((latitude + 90.0) / 5.0);
            ozone = TOMS[lindex][month - 1];
        }

        return ozone;
    }
}
