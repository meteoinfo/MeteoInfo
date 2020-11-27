/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.math.meteo;

import org.meteoinfo.math.ArrayMath;
import org.meteoinfo.math.ArrayUtil;
import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index;
import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.IndexIterator;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author yaqiang
 */
public class MeteoMath {

    /**
     * Calculate saturation vapor pressure
     *
     * @param tc Air temperature
     * @return Saturation vapor pressure
     */
    public static double cal_Es(double tc) {
        double pol, pol1;
        pol = 0.99999683 + tc * (-0.90826951e-02
                + tc * (0.78736169e-04 + tc * (-0.61117958e-06
                + tc * (0.43884187e-08 + tc * (-0.29883885e-10
                + tc * (0.21874425e-12 + tc * (-0.17892321e-14
                + tc * (0.11112018e-16 + tc * (-0.30994571e-19)))))))));
        pol1 = 6.1078 / Math.pow(pol, 8);
        return pol1;
    }

    /**
     * Calculate saturation vapor pressure (Es)
     *
     * @param tc Air temperature
     * @return Saturation vapor pressure
     */
    public static double cal_Es_1(double tc) {
        return 6.11 * Math.pow(10.0, (7.5 * tc / (237.7 + tc)));
    }

    /**
     * Calculate saturation vapor pressure (Es)
     *
     * @param tc Air temperature - degree C
     * @return Saturation vapor pressure
     */
    public static double cal_Es_2(double tc) {
        return 6.112 * Math.exp((17.67 * tc) / (tc + 243.5));
    }

    /**
     * Calculate dewpoint from actual water vapor pressure
     *
     * @param e Actural water vapor pressure
     * @return Dewpoint
     */
    public static double cal_Tdc(double e) {
        double lu, x, dnm, fac, t, edp, dtdew, dt;
        if (e <= 0.06 || e >= 1013.) {
            lu = 9999.;
            return lu;
        }
        x = Math.log(e / 6.1078);
        dnm = 17.269388 - x;
        t = 237.3 * x / dnm;
        fac = 1. / (e * dnm);
        edp = cal_Es(t);
        dtdew = (t + 237.3) * fac;
        dt = dtdew * (e - edp);
        t = t + dt;
        while (Math.abs(dt) >= 1.e-4) {
            edp = cal_Es(t);
            dtdew = (t + 237.3) * fac;
            dt = dtdew * (e - edp);
            t = t + dt;
        }
        return t;
    }

    /**
     * Calculate dewpoint from temperature and relative humidity
     *
     * @param t Temperature
     * @param rh Relative humidity
     * @return Dewpoint
     */
    public static double cal_Tdc(double t, double rh) {
        double esw = cal_Es(t);
        double e = esw * (rh / 100);
        return cal_Tdc(e);
    }

    /**
     * Calculate relative humidity from dewpoint
     *
     * @param tc Temperature
     * @param tdc Dewpoint temperature
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static double dewpoint2rh(double tc, double tdc) {
        double es = cal_Es(tc);
        double e = cal_Es(tdc);
        return e / es * 100;
    }

    /**
     * Calculate relative humidity from dewpoint
     *
     * @param tdc Dewpoint temperature
     * @param tc Temperature
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array dewpoint2rh(Array tdc, Array tc) {
        Array r = Array.factory(tdc.getDataType(), tdc.getShape());
        IndexIterator iter = r.getIndexIterator();
        IndexIterator iter_tdc = tdc.getIndexIterator();
        IndexIterator iter_tc = tc.getIndexIterator();
        while(iter.hasNext()) {
            iter.setDoubleNext(MeteoMath.dewpoint2rh(iter_tc.getDoubleNext(), iter_tdc.getDoubleNext()));
        }

        return r;
    }

    /**
     * Calculate dewpoint from relative humidity and temperature
     *
     * @param rh Relative humidity
     * @param t Temperature
     * @return Dewpoint
     */
    public static double rh2dewpoint(double rh, double t) {
        double esw = cal_Es(t);
        double e = esw * (rh / 100);
        return cal_Tdc(e);
    }

    /**
     * Calculate dewpoint from relative humidity and temperature
     *
     * @param rh Dewpoint temperature
     * @param tc Temperature
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array rh2dewpoint(Array rh, Array tc) {
        Array r = Array.factory(rh.getDataType(), rh.getShape());
        IndexIterator iter = r.getIndexIterator();
        IndexIterator iter_rh = rh.getIndexIterator();
        IndexIterator iter_tc = tc.getIndexIterator();
        while (r.hasNext()) {
            iter.setDoubleNext(MeteoMath.rh2dewpoint(iter_rh.getDoubleNext(), iter_tc.getDoubleNext()));
        }

        return r;
    }

    /**
     * Calculate relative humidity from specific humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param tc Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static double qair2rh(double qair, double tc, double press) {
        double es = cal_Es_2(tc);
        double e = qair * press / (0.378 * qair + 0.622);
        double rh = e / es * 100;
        if (rh > 100) {
            rh = 100;
        } else if (rh < 0) {
            rh = 0;
        }

        return rh;
    }

    /**
     * Calculate dewpoint temperature
     *
     * @param e Actual vapor pressure
     * @return Dewpoint temperature (celsius)
     */
    public static double cal_Tdc_1(double e) {
        return (-430.22 + 237.7 * Math.log(e)) / (-Math.log(e) + 19.08);
    }

    /**
     * Calculate dewpoint temperature
     *
     * @param tc Air temperature
     * @param rh Relative humidity
     * @return Dewpoint temperature (celsius)
     */
    public static double cal_Tdc_1(double tc, double rh) {
        double es = cal_Es(tc);
        double e = cal_E(es, rh);
        return cal_Tdc(e);
    }

    /**
     * Calculate celsius temperature from fahrenheit temperature
     *
     * @param tf Fahrenheit temperature
     * @return Celsius temperature
     */
    public static double tf2tc(double tf) {
        return 5.0 / 9.0 * (tf - 32.0);
    }

    /**
     * Calculate fahrenheit temperature from celsius temperature
     *
     * @param tc Celsius temperature
     * @return Fahrenheit temperature
     */
    public static double tc2tf(double tc) {
        return (9.0 / 5.0) * tc + 32;
    }

    /**
     * Calculate actual vapor pressure (E) of the air
     *
     * @param es Saturation vapor pressure
     * @param rh Relative humidity
     * @return Actual vapor pressure
     */
    public static double cal_E(double es, double rh) {
        return (rh * es) / 100;
    }

    /**
     * Calculate height from pressure
     *
     * @param press Pressure - hPa
     * @return Height - meter
     */
    public static double press2Height(double press) {
        if (press >= 1013.3) {
            return 0;
        }

        double[] ps = new double[]{1013.3, 845.4, 700.8, 504.7, 410.4, 307.1, 193.1, 102.8, 46.7, 8.7};
        double[] hs = new double[]{0, 1500, 3000, 5500, 7000, 9000, 12000, 16000, 21000, 32000};
        int idx = -1;
        int i = -1;
        for (double p : ps) {
            if (press > p) {
                idx = i;
                break;
            }
            i += 1;
        }
        if (idx == -1) {
            return 35000;
        } else {
            double z1 = hs[idx];
            double p1 = ps[idx];
            double z2 = hs[idx + 1];
            double p2 = ps[idx + 1];
            double h = (press - p1) * (z2 - z1) / (p2 - p1) + z1;
            return h;
        }
    }

    /**
     * Calculate pressure frmo height
     *
     * @param height Height - meter
     * @return Pressure - hPa
     */
    public static double height2Press(double height) {
        double[] ps = new double[]{1013.3, 845.4, 700.8, 504.7, 410.4, 307.1, 193.1, 102.8, 46.7, 8.7};
        double[] hs = new double[]{0, 1500, 3000, 5500, 7000, 9000, 12000, 16000, 21000, 32000};
        int idx = -1;
        int i = -1;
        for (double h : hs) {
            if (height < h) {
                idx = i;
                break;
            }
            i += 1;
        }
        if (idx == -1) {
            return 5;
        } else {
            double z1 = hs[idx];
            double p1 = ps[idx];
            double z2 = hs[idx + 1];
            double p2 = ps[idx + 1];
            double p = (height - z1) * (p2 - p1) / (z2 - z1) + p1;
            return p;
        }
    }

    /**
     * Estimate sea level pressure
     *
     * @param z Height (m)
     * @param t Temperature array (K)
     * @param p Pressure array (Pa)
     * @param q Mixing ratio (kg/kg)
     * @return Sea level pressure (Pa)
     */
    public static Array calSeaPrs(Array z, Array t, Array p, Array q) {
        //Specific constants for assumptions made in this routine:
        double RD = 287.0;
        double G = 9.81;
        double USSALR = 0.00650;      // deg C per m
        double TC = 273.16 + 17.5;
        double PCONST = 10000.;
        boolean ridiculous_mm5_test = true;

        int[] shape = z.getShape();
        int nz = shape[0];
        int ny = shape[1];
        int nx = shape[2];
        int i, j, k;
        int klo, khi;
        int errcnt, bad_i, bad_j;
        double bad_sfp;
        double plo, phi, tlo, thi, zlo, zhi;
        double p_at_pconst, t_at_pconst, z_at_pconst;
        boolean l1, l2, l3, found;

        //  Find least zeta level that is PCONST Pa above the surface.  We
        //  later use this level to extrapolate a surface pressure and
        //  temperature, which is supposed to reduce the effect of the diurnal
        //  heating cycle in the pressure field.
        //int errstat = 0;
        errcnt = 0;
        bad_i = -1;
        bad_j = -1;
        bad_sfp = -1;
        int[][] level = new int[ny][nx];
        Index pIdx = p.getIndex();
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                level[i][j] = -1;
                k = 0;
                found = false;
                while ((!found) && (k < nz)) {
                    if (p.getDouble(pIdx.set(k, i, j)) < p.getDouble(pIdx.set(0, i, j)) - PCONST) {
                        level[i][j] = k;
                        found = true;
                    }
                    k = k + 1;
                }

                if (level[i][j] == -1) {
                    errcnt = errcnt + 1;
                    //$OMP CRITICAL
                    // Only do this the first time
                    if (bad_i == -1) {
                        bad_i = i;
                        bad_j = j;
                        bad_sfp = p.getDouble(pIdx.set(0, i, j)) / 100.;
                    }
                    //$OMP END CRITICAL
                }
            }
        }

        if (errcnt > 0) {
            //errstat = ALGERR;
            System.out.println("Error in finding 100 hPa up.  i=" + bad_i + "j=" + bad_j + "sfc_p=" + bad_sfp);
            return null;
        }

        //     Get temperature PCONST Pa above surface.  Use this to extrapolate
        //     the temperature at the surface and down to sea level.
        //$OMP PARALLEL DO COLLAPSE(2) PRIVATE(i,j,klo,khi,plo, &
        //$OMP phi,tlo,thi,zlo,zhi,p_at_pconst,t_at_pconst,z_at_pconst) &
        //$OMP REDUCTION(+:errcnt) SCHEDULE(runtime)
        double[][] t_surf = new double[ny][nx];
        double[][] t_sea_level = new double[ny][nx];
        Index tIdx = t.getIndex();
        Index zIdx = z.getIndex();
        Index qIdx = q.getIndex();
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                klo = Math.max(level[i][j] - 1, 0);
                khi = Math.min(klo + 1, nz - 1);

                if (klo == khi) {
                    errcnt = errcnt + 1;
                    //$OMP CRITICAL
                    if (bad_i == -1) {
                        bad_i = i;
                        bad_j = j;
                    }
                    //$OMP END CRITICAL
                }

                plo = p.getDouble(pIdx.set(klo, i, j));
                phi = p.getDouble(pIdx.set(khi, i, j));
                tlo = t.getDouble(tIdx.set(klo, i, j)) * (1.0 + 0.6080 * q.getDouble(qIdx.set(klo, i, j)));
                thi = t.getDouble(tIdx.set(khi, i, j)) * (1.0 + 0.6080 * q.getDouble(qIdx.set(khi, i, j)));
                zlo = z.getDouble(zIdx.set(klo, i, j));
                zhi = z.getDouble(zIdx.set(khi, i, j));
                p_at_pconst = p.getDouble(pIdx.set(0, i, j)) - PCONST;
                t_at_pconst = thi - (thi - tlo) * Math.log(p_at_pconst / phi) * Math.log(plo / phi);
                z_at_pconst = zhi - (zhi - zlo) * Math.log(p_at_pconst / phi) * Math.log(plo / phi);

                t_surf[i][j] = t_at_pconst * Math.pow(p.getDouble(pIdx.set(0, i, j)) / p_at_pconst, USSALR * RD / G);
                t_sea_level[i][j] = t_at_pconst + USSALR * z_at_pconst;
            }
        }
        //$OMP END PARALLEL DO

        if (errcnt > 0) {
            //errstat = ALGERR;
            System.out.println("Error trapping levels at i=" + bad_i + "j=" + bad_j);
            return null;
        }

        // If we follow a traditional computation, there is a correction to the
        // sea level temperature if both the surface and sea level
        // temperatures are *too* hot.
        if (ridiculous_mm5_test) {
            //$OMP PARALLEL DO COLLAPSE(2) PRIVATE(l1,l2,l3) SCHEDULE(runtime)   
            l1 = true;
            for (i = 0; i < ny; i++) {
                for (j = 0; j < nx; j++) {
                    l1 = (t_sea_level[i][j] < TC);                
                    l2 = (t_surf[i][j] <= TC);
                    l3 = !l1;
                    if (l2 && l3) {
                        t_sea_level[i][j] = TC;
                    } else {
                        t_sea_level[i][j] = TC - 0.0050 * Math.pow(t_surf[i][j] - TC, 2);
                    }
                }
            }
        }
        //$OMP END PARALLEL DO

        //     The grand finale: ta da!
        //$OMP PARALLEL DO COLLAPSE(2) SCHEDULE(runtime)
        Array sea_level_pressure = Array.factory(DataType.DOUBLE, new int[]{ny, nx});
        double v;
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                //z_half_lowest = z(i,j,1)

                // Convert to hPa in this step, by multiplying by 0.01. The original
                // Fortran routine didn't do this, but the NCL script that called it
                // did, so we moved it here.
                v = 0.01 * (p.getDouble(pIdx.set(0, i, j)) * Math.exp((2.0 * G * z.getDouble(zIdx.set(0, i, j)))
                        / (RD * (t_sea_level[i][j] + t_surf[i][j]))));
                sea_level_pressure.setDouble(i * nx + j, v);
            }
        }
        //$OMP END PARALLEL DO

        return sea_level_pressure;
    }

    /**
     * Estimate sea level pressure
     *
     * @param z Height (m)
     * @param t Temperature array (K)
     * @param p Pressure array (Pa)
     * @param q Mixing ratio (kg/kg)
     * @return Sea level pressure (Pa)
     */
    public static Array calSeaPrs_bak(Array z, Array t, Array p, Array q) {
        //Specific constants for assumptions made in this routine:
        double RD = 287.0;
        double G = 9.81;
        double USSALR = 0.00650;      // deg C per m
        double TC = 273.16 + 17.5;
        double PCONST = 10000.;
        boolean ridiculous_mm5_test = true;

        int[] shape = z.getShape();
        int nz = shape[0];
        int ny = shape[1];
        int nx = shape[2];
        int i, j, k;
        int klo, khi;
        int errcnt, bad_i, bad_j;
        double bad_sfp;
        double plo, phi, tlo, thi, zlo, zhi;
        double p_at_pconst, t_at_pconst, z_at_pconst;
        boolean l1, l2, l3, found;

        //  Find least zeta level that is PCONST Pa above the surface.  We
        //  later use this level to extrapolate a surface pressure and
        //  temperature, which is supposed to reduce the effect of the diurnal
        //  heating cycle in the pressure field.
        //int errstat = 0;
        errcnt = 0;
        bad_i = -1;
        bad_j = -1;
        bad_sfp = -1;
        int[][] level = new int[ny][nx];
        Index idx3 = Index.factory(shape);
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                level[i][j] = -1;
                k = 0;
                found = false;
                while ((!found) && (k < nz)) {
                    if (p.getDouble(idx3.set(k, i, j)) < p.getDouble(idx3.set(0, i, j)) - PCONST) {
                        level[i][j] = k;
                        found = true;
                    }
                    k = k + 1;
                }

                if (level[i][j] == -1) {
                    errcnt = errcnt + 1;
                    //$OMP CRITICAL
                    // Only do this the first time
                    if (bad_i == -1) {
                        bad_i = i;
                        bad_j = j;
                        bad_sfp = p.getDouble(idx3.set(0, i, j)) / 100.;
                    }
                    //$OMP END CRITICAL
                }
            }
        }

        if (errcnt > 0) {
            //errstat = ALGERR;
            System.out.println("Error in finding 100 hPa up.  i=" + bad_i + "j=" + bad_j + "sfc_p=" + bad_sfp);
            return null;
        }

        //     Get temperature PCONST Pa above surface.  Use this to extrapolate
        //     the temperature at the surface and down to sea level.
        //$OMP PARALLEL DO COLLAPSE(2) PRIVATE(i,j,klo,khi,plo, &
        //$OMP phi,tlo,thi,zlo,zhi,p_at_pconst,t_at_pconst,z_at_pconst) &
        //$OMP REDUCTION(+:errcnt) SCHEDULE(runtime)
        double[][] t_surf = new double[ny][nx];
        double[][] t_sea_level = new double[ny][nx];
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                klo = Math.max(level[i][j] - 1, 0);
                khi = Math.min(klo + 1, nz - 1);

                if (klo == khi) {
                    errcnt = errcnt + 1;
                    //$OMP CRITICAL
                    if (bad_i == -1) {
                        bad_i = i;
                        bad_j = j;
                    }
                    //$OMP END CRITICAL
                }

                plo = p.getDouble(idx3.set(klo, i, j));
                phi = p.getDouble(idx3.set(khi, i, j));
                tlo = t.getDouble(idx3.set(klo, i, j)) * (1.0 + 0.6080 * q.getDouble(idx3.set(klo, i, j)));
                thi = t.getDouble(idx3.set(khi, i, j)) * (1.0 + 0.6080 * q.getDouble(idx3.set(khi, i, j)));
                zlo = z.getDouble(idx3.set(klo, i, j));
                zhi = z.getDouble(idx3.set(khi, i, j));
                p_at_pconst = p.getDouble(idx3.set(0, i, j)) - PCONST;
                t_at_pconst = thi - (thi - tlo) * Math.log(p_at_pconst / phi) * Math.log(plo / phi);
                z_at_pconst = zhi - (zhi - zlo) * Math.log(p_at_pconst / phi) * Math.log(plo / phi);

                t_surf[i][j] = t_at_pconst * Math.pow(p.getDouble(idx3.set(0, i, j)) / p_at_pconst, USSALR * RD / G);
                t_sea_level[i][j] = t_at_pconst + USSALR * z_at_pconst;
            }
        }
        //$OMP END PARALLEL DO

        if (errcnt > 0) {
            //errstat = ALGERR;
            System.out.println("Error trapping levels at i=" + bad_i + "j=" + bad_j);
            return null;
        }

        // If we follow a traditional computation, there is a correction to the
        // sea level temperature if both the surface and sea level
        // temperatures are *too* hot.
        if (ridiculous_mm5_test) {
            //$OMP PARALLEL DO COLLAPSE(2) PRIVATE(l1,l2,l3) SCHEDULE(runtime)
            l1 = true;
            for (i = 0; i < ny; i++) {
                for (j = 0; j < nx; j++) {
                    l1 = (t_sea_level[i][j] < TC);
                    l2 = (t_surf[i][j] <= TC);
                    l3 = !l1;
                    if (l2 && l3) {
                        t_sea_level[i][j] = TC;
                    } else {
                        t_sea_level[i][j] = TC - 0.0050 * Math.pow(t_surf[i][j] - TC, 2);
                    }
                }
            }
        }
        //$OMP END PARALLEL DO

        //     The grand finale: ta da!
        //$OMP PARALLEL DO COLLAPSE(2) SCHEDULE(runtime)
        Array sea_level_pressure = Array.factory(DataType.DOUBLE, new int[]{ny, nx});
        double v;
        for (i = 0; i < ny; i++) {
            for (j = 0; j < nx; j++) {
                //z_half_lowest = z(i,j,1)

                // Convert to hPa in this step, by multiplying by 0.01. The original
                // Fortran routine didn't do this, but the NCL script that called it
                // did, so we moved it here.
                v = 0.01 * (p.getDouble(idx3.set(0, i, j)) * Math.exp((2.0 * G * z.getDouble(idx3.set(0, i, j)))
                        / (RD * (t_sea_level[i][j] + t_surf[i][j]))));
                sea_level_pressure.setDouble(i * nx + j, v);
            }
        }
        //$OMP END PARALLEL DO

        return sea_level_pressure;
    }
    
    /**
     * Calculate fahrenheit temperature from celsius temperature
     *
     * @param tc Celsius temperature
     * @return Fahrenheit temperature
     */
    public static Array tc2tf(Array tc) {
        Array r = Array.factory(tc.getDataType(), tc.getShape());
        IndexIterator rIter = r.getIndexIterator();
        IndexIterator tcIter = tc.getIndexIterator();
        while (rIter.hasNext()) {
            rIter.setDoubleNext(MeteoMath.tc2tf(tcIter.getDoubleNext()));
        }

        return r;
    }

    /**
     * Calculate celsius temperature from fahrenheit temperature
     *
     * @param tf Fahrenheit temperature
     * @return Celsius temperature
     */
    public static Array tf2tc(Array tf) {
        Array r = Array.factory(tf.getDataType(), tf.getShape());
        IndexIterator rIter = r.getIndexIterator();
        IndexIterator tfIter = tf.getIndexIterator();
        while (rIter.hasNext()) {
            rIter.setDoubleNext(MeteoMath.tf2tc(tfIter.getDoubleNext()));
        }

        return r;
    }

    /**
     * Calculate relative humidity from specific humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param temp Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array qair2rh(Array qair, Array temp, double press) {
        Array r = Array.factory(DataType.DOUBLE, qair.getShape());
        IndexIterator rIter = r.getIndexIterator();
        IndexIterator qairIter = qair.getIndexIterator();
        IndexIterator tempIter = temp.getIndexIterator();
        double rh;
        while (rIter.hasNext()) {
            rh = MeteoMath.qair2rh(qairIter.getDoubleNext(), tempIter.getDoubleNext(), press);
            rIter.setDoubleNext(rh);
        }

        return r;
    }

    /**
     * Calculate relative humidity
     *
     * @param qair Specific humidity, dimensionless (e.g. kg/kg) ratio of water
     * mass / total air mass
     * @param temp Temperature - degree c
     * @param press Pressure - hPa (mb)
     * @return Relative humidity as percent (i.e. 80%)
     */
    public static Array qair2rh(Array qair, Array temp, Array press) {
        Array r = Array.factory(DataType.DOUBLE, qair.getShape());
        IndexIterator rIter = r.getIndexIterator();
        IndexIterator qairIter = qair.getIndexIterator();
        IndexIterator tempIter = temp.getIndexIterator();
        IndexIterator pressIter = press.getIndexIterator();
        double rh;
        while (rIter.hasNext()) {
            rh = MeteoMath.qair2rh(qairIter.getDoubleNext(), tempIter.getDoubleNext(), pressIter.getDoubleNext());
            rIter.setDoubleNext(rh);
        }

        return r;
    }

    /**
     * Calculate height from pressure
     *
     * @param press Pressure - hPa
     * @return Height - m
     */
    public static Array press2Height(Array press) {
        Array r = Array.factory(DataType.DOUBLE, press.getShape());
        IndexIterator rIter = r.getIndexIterator();
        IndexIterator pressIter = press.getIndexIterator();
        double rh;
        while (rIter.hasNext()) {
            rh = MeteoMath.press2Height(pressIter.getDoubleNext());
            rIter.setDoubleNext(rh);
        }

        return r;
    }

    /**
     * Calculate pressure from height
     *
     * @param height Height - m
     * @return Pressure - hPa
     */
    public static Array height2Press(Array height) {
        Array r = Array.factory(DataType.DOUBLE, height.getShape());
        IndexIterator iter = height.getIndexIterator();
        double rh;
        for (int i = 0; i < r.getSize(); i++) {
            rh = MeteoMath.height2Press(iter.getDoubleNext());
            r.setDouble(i, rh);
        }

        return r;
    }
    
    /**
     * Get wind direction and wind speed from U/V
     *
     * @param u U component
     * @param v V component
     * @return Wind direction and wind speed
     */
    public static Array[] uv2ds(Array u, Array v) {
        Array windSpeed = ArrayMath.sqrt(ArrayMath.add(ArrayMath.mul(u, u), ArrayMath.mul(v, v)));
        Array windDir = Array.factory(windSpeed.getDataType(), windSpeed.getShape());
        double ws, wd, U, V;
        if (u.getIndexPrivate().isFastIterator() && v.getIndexPrivate().isFastIterator()) {
            for (int i = 0; i < windSpeed.getSize(); i++) {
                U = u.getDouble(i);
                V = v.getDouble(i);
                if (Double.isNaN(U) || Double.isNaN(V)) {
                    windDir.setDouble(i, Double.NaN);
                    continue;
                }
                ws = windSpeed.getDouble(i);
                if (ws == 0) {
                    wd = 0;
                } else {
                    wd = Math.asin(U / ws) * 180 / Math.PI;
                    if (U <= 0 && V < 0) {
                        wd = 180.0 - wd;
                    } else if (U > 0 && V < 0) {
                        wd = 180.0 - wd;
                    } else if (U < 0 && V > 0) {
                        wd = 360.0 + wd;
                    }
                    wd += 180;
                    if (wd >= 360) {
                        wd -= 360;
                    }
                }
                windDir.setDouble(i, wd);
            }
        } else {
            IndexIterator iterU = u.getIndexIterator();
            IndexIterator iterV = v.getIndexIterator();
            IndexIterator iterWS = windSpeed.getIndexIterator();
            IndexIterator iterWD = windDir.getIndexIterator();
            while (iterU.hasNext()) {
                U = iterU.getDoubleNext();
                V = iterV.getDoubleNext();
                ws = iterWS.getDoubleNext();
                if (Double.isNaN(U) || Double.isNaN(V)) {
                    iterWD.setDoubleNext(Double.NaN);
                    continue;
                }
                if (ws == 0) {
                    wd = 0;
                } else {
                    wd = Math.asin(U / ws) * 180 / Math.PI;
                    if (U <= 0 && V < 0) {
                        wd = 180.0 - wd;
                    } else if (U > 0 && V < 0) {
                        wd = 180.0 - wd;
                    } else if (U < 0 && V > 0) {
                        wd = 360.0 + wd;
                    }
                    wd += 180;
                    if (wd >= 360) {
                        wd -= 360;
                    }
                }
                iterWD.setDoubleNext(wd);
            }
        }

        return new Array[]{windDir, windSpeed};
    }

    /**
     * Get wind direction and wind speed from U/V
     *
     * @param u U component
     * @param v V component
     * @return Wind direction and wind speed
     */
    public static double[] uv2ds(double u, double v) {
        double ws = Math.sqrt(u * u + v * v);
        double wd;
        if (ws == 0) {
            wd = 0;
        } else {
            wd = Math.asin(u / ws) * 180 / Math.PI;
            if (u <= 0 && v < 0) {
                wd = 180.0 - wd;
            } else if (u > 0 && v < 0) {
                wd = 180.0 - wd;
            } else if (u < 0 && v > 0) {
                wd = 360.0 + wd;
            }
            wd += 180;
            if (wd >= 360) {
                wd -= 360;
            }
        }

        return new double[]{wd, ws};
    }

    /**
     * Get wind U/V components from wind direction and speed
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @return Wind U/V components
     */
    public static Array[] ds2uv(Array windDir, Array windSpeed) {
        Array U = Array.factory(DataType.DOUBLE, windDir.getShape());
        Array V = Array.factory(DataType.DOUBLE, windDir.getShape());
        if (windDir.getIndexPrivate().isFastIterator() && windSpeed.getIndexPrivate().isFastIterator()) {
            double dir;
            for (int i = 0; i < U.getSize(); i++) {
                if (Double.isNaN(windDir.getDouble(i)) || Double.isNaN(windSpeed.getDouble(i))) {
                    U.setDouble(i, Double.NaN);
                    V.setDouble(i, Double.NaN);
                }
                dir = windDir.getDouble(i) + 180;
                if (dir > 360) {
                    dir = dir - 360;
                }
                dir = dir * Math.PI / 180;
                U.setDouble(i, windSpeed.getDouble(i) * Math.sin(dir));
                V.setDouble(i, windSpeed.getDouble(i) * Math.cos(dir));
            }
        } else {
            IndexIterator iterU = U.getIndexIterator();
            IndexIterator iterV = V.getIndexIterator();
            IndexIterator iterWS = windSpeed.getIndexIterator();
            IndexIterator iterWD = windDir.getIndexIterator();
            double dir, wd, ws;
            while (iterU.hasNext()) {
                wd = iterWD.getDoubleNext();
                ws = iterWS.getDoubleNext();
                if (Double.isNaN(wd) || Double.isNaN(ws)) {
                    iterU.setDoubleNext(Double.NaN);
                    iterV.setDoubleNext(Double.NaN);
                }
                dir = wd + 180;
                if (dir > 360) {
                    dir = dir - 360;
                }
                dir = dir * Math.PI / 180;
                iterU.setDoubleNext(ws * Math.sin(dir));
                iterV.setDoubleNext(ws * Math.cos(dir));
            }
        }

        return new Array[]{U, V};
    }

    /**
     * Get wind U/V components from wind direction and speed
     *
     * @param windDir Wind direction
     * @param windSpeed Wind speed
     * @return Wind U/V components
     */
    public static double[] ds2uv(double windDir, double windSpeed) {
        double dir;
        dir = windDir + 180;
        if (dir > 360) {
            dir = dir - 360;
        }
        dir = dir * Math.PI / 180;
        double u = windSpeed * Math.sin(dir);
        double v = windSpeed * Math.cos(dir);

        return new double[]{u, v};
    }

    /**
     * Performs a centered difference operation on a grid data along one
     * dimension direction
     *
     * @param data The grid data
     * @param dimIdx Direction dimension index
     * @return Result grid data
     */
    public static Array cdiff(Array data, int dimIdx) {
        Array r = Array.factory(DataType.DOUBLE, data.getShape());
        Index index = data.getIndex();
        Index indexr = r.getIndex();
        int[] shape = data.getShape();
        int[] current, cc;
        double a, b;
        for (int i = 0; i < r.getSize(); i++) {
            current = indexr.getCurrentCounter();
            if (current[dimIdx] == 0 || current[dimIdx] == shape[dimIdx] - 1) {
                r.setDouble(indexr, Double.NaN);
            } else {
                cc = Arrays.copyOf(current, current.length);
                cc[dimIdx] = cc[dimIdx] - 1;
                index.set(cc);
                a = data.getDouble(index);
                cc[dimIdx] = cc[dimIdx] + 2;
                index.set(cc);
                b = data.getDouble(index);
                if (Double.isNaN(a) || Double.isNaN(b)) {
                    r.setDouble(indexr, Double.NaN);
                } else {
                    r.setDouble(indexr, a - b);
                }
            }
            indexr.incr();
        }

        return r;
    }

    /**
     * Performs a centered difference operation on a grid data in the x or y
     * direction
     *
     * @param data The grid data
     * @param isX If is x direction
     * @return Result grid data
     */
    public static Array cdiff_bak(Array data, boolean isX) {
        if (data.getRank() == 2) {
            int xnum = data.getShape()[1];
            int ynum = data.getShape()[0];
            Array r = Array.factory(DataType.DOUBLE, data.getShape());
            for (int i = 0; i < ynum; i++) {
                for (int j = 0; j < xnum; j++) {
                    if (i == 0 || i == ynum - 1 || j == 0 || j == xnum - 1) {
                        r.setDouble(i * xnum + j, Double.NaN);
                    } else {
                        double a, b;
                        if (isX) {
                            a = data.getDouble(i * xnum + j + 1);
                            b = data.getDouble(i * xnum + j - 1);
                        } else {
                            a = data.getDouble((i + 1) * xnum + j);
                            b = data.getDouble((i - 1) * xnum + j);
                        }
                        if (Double.isNaN(a) || Double.isNaN(b)) {
                            r.setDouble(i * xnum + j, Double.NaN);
                        } else {
                            r.setDouble(i * xnum + j, a - b);
                        }
                    }
                }
            }

            return r;
        } else if (data.getRank() == 1) {
            int n = data.getShape()[0];
            Array r = Array.factory(DataType.DOUBLE, data.getShape());
            for (int i = 0; i < n; i++) {
                if (i == 0 || i == n - 1) {
                    r.setDouble(i, Double.NaN);
                } else {
                    double a, b;
                    a = data.getDouble(i + 1);
                    b = data.getDouble(i - 1);
                    if (Double.isNaN(a) || Double.isNaN(b)) {
                        r.setDouble(i, Double.NaN);
                    } else {
                        r.setDouble(i, a - b);
                    }
                }
            }

            return r;
        } else {
            System.out.println("Data dimension number must be 1 or 2!");
            return null;
        }
    }

    /**
     * Calculates the vertical component of the curl (ie, vorticity)
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Curl
     */
    public static Array vorticity(Array uData, Array vData, Array xx, Array yy) {
        int rank = uData.getRank();
        int[] shape = uData.getShape();
        Array lonData = Array.factory(DataType.DOUBLE, shape);
        Array latData = Array.factory(DataType.DOUBLE, shape);
        Index index = lonData.getIndex();
        int[] current;
        for (int i = 0; i < lonData.getSize(); i++) {
            current = index.getCurrentCounter();
            lonData.setDouble(index, xx.getDouble(current[rank - 1]));
            latData.setDouble(index, yy.getDouble(current[rank - 2]));
            index.incr();
        }

        Array dv = cdiff(vData, rank - 1);
        Array dx = ArrayMath.mul(cdiff(lonData, rank - 1), Math.PI / 180);
        Array du = cdiff(ArrayMath.mul(uData, ArrayMath.cos(ArrayMath.mul(latData, Math.PI / 180))), rank - 2);
        Array dy = ArrayMath.mul(cdiff(latData, rank - 2), Math.PI / 180);
        Array gData = ArrayMath.div(ArrayMath.sub(ArrayMath.div(dv, dx), ArrayMath.div(du, dy)), ArrayMath.mul(ArrayMath.cos(ArrayMath.mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Calculates the horizontal divergence using finite differencing
     *
     * @param uData U component
     * @param vData V component
     * @param xx X dimension value
     * @param yy Y dimension value
     * @return Divergence
     */
    public static Array divergence(Array uData, Array vData, Array xx, Array yy) {
        int rank = uData.getRank();
        int[] shape = uData.getShape();
        Array lonData = Array.factory(DataType.DOUBLE, shape);
        Array latData = Array.factory(DataType.DOUBLE, shape);
        Index index = lonData.getIndex();
        int[] current;
        for (int i = 0; i < lonData.getSize(); i++) {
            current = index.getCurrentCounter();
            lonData.setDouble(index, xx.getDouble(current[rank - 1]));
            latData.setDouble(index, yy.getDouble(current[rank - 2]));
            index.incr();
        }

        Array du = cdiff(uData, rank - 1);
        Array dx = ArrayMath.mul(cdiff(lonData, rank - 1), Math.PI / 180);
        Array dv = cdiff(ArrayMath.mul(vData, ArrayMath.cos(ArrayMath.mul(latData, Math.PI / 180))), rank - 2);
        Array dy = ArrayMath.mul(cdiff(latData, rank - 2), Math.PI / 180);
        Array gData = ArrayMath.div(ArrayMath.add(ArrayMath.div(du, dx), ArrayMath.div(dv, dy)), ArrayMath.mul(ArrayMath.cos(ArrayMath.mul(latData, Math.PI / 180)), 6.37e6));

        return gData;
    }

    /**
     * Take magnitude value from U/V grid data
     *
     * @param uData U grid data
     * @param vData V grid data
     * @return Magnitude grid data
     */
    public static Array magnitude(Array uData, Array vData) {
        int[] shape = uData.getShape();
        Array r = Array.factory(DataType.DOUBLE, shape);
        IndexIterator iterU = uData.getIndexIterator();
        IndexIterator iterV = vData.getIndexIterator();
        IndexIterator iterR = r.getIndexIterator();
        double u, v;
        while (iterU.hasNext()) {
            u = iterU.getDoubleNext();
            v = iterV.getDoubleNext();
            if (Double.isNaN(u) || Double.isNaN(v)) {
                iterR.setDoubleNext(Double.NaN);
            } else {
                iterR.setDoubleNext(Math.sqrt(Math.pow(u, 2) + Math.pow(v, 2)));
            }
        }

        return r;
    }
}
