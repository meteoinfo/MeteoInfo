/*
 * Calculation algorithm for WRF model output
 */
package org.meteoinfo.math.meteo;

import org.meteoinfo.ndarray.Array;
import org.meteoinfo.ndarray.Index3D;

public class WRF {
    //constants
    public static final double GAMMA_SEVEN = 720.;
    public static final double RHOWAT = 1000.;
    public static final double RHO_R = RHOWAT;
    public static final double RHO_S = 100.;
    public static final double RHO_G = 400.;
    public static final double ALPHA = 0.224;
    public static final double CELKEL = 273.15;
    public static final double PI = 3.141592653589793;
    public static final double RD = 287.04;

    /**
     * Computes equivalent reflectivity factor (in dBZ) at
     * each model grid point.
     *
     * @param prs Pressure
     * @param tmk Temperature
     * @param qvp Water vapor mixing ratio
     * @param qra Rain water mixing ratio
     * @param qsn Snow mixing ratio
     * @param qgr Graupel mixing ratio
     * @param sn0 Whether snow mixing ratio is not all 0
     * @param ivarint The variable intercept parameter
     * @param iliqskin The frozen particles parameter
     * @return Calculated dBZ array
     */
    public static Array calcDBZ(Array prs, Array tmk, Array qvp, Array qra, Array qsn, Array qgr,
                                int sn0, int ivarint, int iliqskin) {
        int[] shape = prs.getShape();
        int nz = shape[0]; int ny = shape[1]; int nx = shape[2];
        int i, j, k;
        double temp_c, virtual_t, gonv, ronv, sonv, factor_g, factor_r, factor_s,
                factorb_g, factorb_s, rhoair, z_e;

        //Constants used to calculate variable intercepts
        double R1 = 1.E-15;
        double RON = 8.E6;
        double RON2 = 1.E10;
        double SON = 2.E7;
        double GON = 5.E7;
        double RON_MIN = 8.E6;
        double RON_QR0 = 0.00010;
        double RON_DELQR0 = 0.25 * RON_QR0;
        double RON_CONST1R = (RON2-RON_MIN)*0.5;
        double RON_CONST2R = (RON2+RON_MIN)*0.5;

        //Constant intercepts
        double RN0_R = 8.E6;
        double RN0_S = 2.E7;
        double RN0_G = 4.E6;

        //Force all Q arrays to be 0.0 or greater.
        Index3D index = (Index3D) Index3D.factory(shape);
        for (k = 0; k < nz; k++) {
            for (j = 0; j < ny; j++) {
                for (i = 0; i < nx; i++) {
                    index.set(k, j, i);
                    if (qvp.getDouble(index) < 0.0) {
                        qvp.setDouble(index, 0.0);
                    }
                    if (qra.getDouble(index) < 0.0) {
                        qra.setDouble(index, 0.0);
                    }
                    if (qsn.getDouble(index) < 0.0) {
                        qsn.setDouble(index, 0.0);
                    }
                    if (qgr.getDouble(index) < 0.0) {
                        qgr.setDouble(index, 0.0);
                    }
                }
            }
        }

        //Input pressure is Pa, but we need hPa in calculations
        if (sn0 == 0) {
            for (k = 0; k < nz; k++) {
                for (j = 0; j < ny; j++) {
                    for (i = 0; i < nx; i++) {
                        index.set(k, j, i);
                        if (tmk.getDouble(index) < CELKEL) {
                            qsn.setDouble(index, qra.getDouble(index));
                            qra.setDouble(index, 0.);
                        }
                    }
                }
            }
        }

        factor_r = GAMMA_SEVEN * 1.E18 * Math.pow((1.0 / (PI * RHO_R)), 1.75);
        factor_s = GAMMA_SEVEN * 1.E18 * Math.pow((1.0 / (PI * RHO_S)), 1.75) *
                Math.pow((RHO_S / RHOWAT), 2) * ALPHA;
        factor_g = GAMMA_SEVEN * 1.E18 * Math.pow((1.0 / (PI * RHO_G)), 1.75) *
                Math.pow((RHO_G / RHOWAT), 2) * ALPHA;

        Array dbz = Array.factory(prs.getDataType(), shape);
        for (k = 0; k < nz; k++) {
            for (j = 0; j < ny; j++) {
                for (i = 0; i < nx; i++) {
                    index.set(k, j, i);
                    virtual_t = tmk.getDouble(index) * (0.622 + qvp.getDouble(index)) /
                            (0.622 * (1. + qvp.getDouble(index)));
                    rhoair = prs.getDouble(index) / (RD * virtual_t);

                    //Adjust factor for brightband, where snow or graupel particle
                    //scatters like liquid water (alpha=1.0) because it is assumed to
                    //have a liquid skin.
                    if (iliqskin == 1 && tmk.getDouble(index) > CELKEL) {
                        factorb_s = factor_s / ALPHA;
                        factorb_g = factor_g / ALPHA;
                    } else {
                        factorb_s = factor_s;
                        factorb_g = factor_g;
                    }

                    //Calculate variable intercept parameters
                    if (ivarint == 1) {
                        temp_c = Math.min(-0.001, tmk.getDouble(index) - CELKEL);
                        sonv = Math.min(2.0E8, 2.0E6 * Math.exp(-0.12 * temp_c));

                        gonv = GON;
                        if (qgr.getDouble(index) > R1) {
                            gonv = 2.38 * Math.pow(PI * RHO_G / (rhoair * qgr.getDouble(index)), 0.92);
                            gonv = Math.max(1.E4, Math.min(gonv, GON));
                        }

                        ronv = RON2;
                        if (qra.getDouble(index) > R1) {
                            ronv = RON_CONST1R * Math.tanh((RON_QR0 - qra.getDouble(index)) / RON_DELQR0) + RON_CONST2R;
                        }

                    } else {
                        ronv = RN0_R;
                        sonv = RN0_S;
                        gonv = RN0_G;
                    }

                    //Total equivalent reflectivity factor (z_e, in mm^6 m^-3) is
                    //the sum of z_e for each hydrometeor species:
                    z_e = factor_r * Math.pow(rhoair * qra.getDouble(index), 1.75) / Math.pow(ronv, .75) +
                            factorb_s * Math.pow(rhoair * qsn.getDouble(index), 1.75) / Math.pow(sonv, .75) +
                            factorb_g * Math.pow(rhoair * qgr.getDouble(index), 1.75) / Math.pow(gonv, .75);

                    //Adjust small values of Z_e so that dBZ is no lower than -30
                    z_e = Math.max(z_e, .001);

                    //Convert to dBZ
                    dbz.setDouble(index, 10. * Math.log10(z_e));
                }
            }
        }

        return dbz;
    }
}
