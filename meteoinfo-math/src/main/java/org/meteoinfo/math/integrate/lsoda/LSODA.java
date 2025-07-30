package org.meteoinfo.math.integrate.lsoda;

import org.meteoinfo.math.integrate.lsoda.exception.*;
import org.apache.commons.math4.legacy.analysis.solvers.UnivariateSolver;
import org.apache.commons.math4.legacy.exception.DimensionMismatchException;
import org.apache.commons.math4.legacy.exception.MaxCountExceededException;
import org.apache.commons.math4.legacy.exception.NoBracketingException;
import org.apache.commons.math4.legacy.exception.NumberIsTooSmallException;
import org.apache.commons.math4.legacy.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math4.legacy.ode.events.EventHandler;
import org.apache.commons.math4.legacy.ode.sampling.StepHandler;
import org.meteoinfo.math.integrate.lsoda.tools.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.meteoinfo.math.integrate.lsoda.SupportingFunctions.*;
import org.apache.commons.math4.legacy.ode.FirstOrderIntegrator;


public class LSODA implements FirstOrderIntegrator{
    public FirstOrderDifferentialEquations ode;
    private final double ETA = 2.2204460492503131e-16;
    private double sqrteta;
    // stability region for non-stiff method
    private final double[] sm1 = {0., 0.5, 0.575, 0.55, 0.45, 0.35, 0.25,
            0.2, 0.15, 0.1, 0.075, 0.05, 0.025};
    // C_q
    private final double[] cm1 = new double[13];
    private final double[] cm2 = new double[6];
    // max order for methods
    private final int[] mord = {0, 12, 5};
    private int illin = 0, init = 0;
    // method used, max order to be used on any step
    private int meth, ixpr = 0, mused, mxordn, mxords, maxord, nqu, maxcor, msbp, mxncf, nslast;
    // private int jtyp;

    // whether stability affects the step size
    private int irflag;
    // signal the information of params to stoda
    private int jstart;
    // for error and convergence tests
    private int kflag;
    // whether to update the Jacobian
    private int ipup;
    // converge or not
    private int corflag;
    // number of convergence failure
    private int ncf;
    // History matrix
    private double[][] yh;

    // current method order
    private int nq;
    private double told, rh, del, delp, m;

    // number of consecutive times an initialization or "first" call
    // has been made to LSODE with same initial and final values for integration interval
    private int ntrep;
    // step size used on last successful step, and current h
    private double hu, h;
    // number of times the step size is too small
    private int nhnil;
    // max r, the inverse of max h, min h, last h
    private double rmax, hmxi, hmin,hmaxi, hold;
    // relative change in hxEL0 since last update of Jacobian matrix
    private double rc;
    // params about convergence test
    private double ccmax, crate, conit, ratio;
    private double pdh;
    private double rhup;
    private int mxstep, mxhnil;
    // integer counter related to step size and method order changes
    private int ialth, lmax, icount;
    // number of columns in history matrix
    private int l;
    private int n;
    private int nslp;

    // whether J is singular
    private int ierpj;
    // whether J is current; 1 for current Jacobian
    private int jcur;
    // coefficient l_0
    private double el0;
    // corrector iteration technique
    private int miter;

    // EWT
    private double[] savf, ewt;
    // local error on last step
    private double[] acor;
    // store the Jacobian
    private double[][] wm;
    // norm of Jacobian(P=I-hbJ), the last P norm, P*h
    private double pdnorm, pdlast, pdest;
    // pivot
    private int[] ipvt;

    // method coefficients
    private final double[][] elco = new double[13][14];
    // current coefficients
    private double[] el = new double[14];
    // coefficients for tests
    private final double[][] tesco = new double[13][4];

    // current value of independent variables
    public double tn;

    public int istate;
    // number of steps taken
    private int nst;
    // max component
    private int imxer;
    // number of J evaluations; number of derivative evaluations
    private int nje, nfe;

    // value of f1,...,fn
    public double[] y;
    // whether to record each points to the csv file
    public boolean write = false;
    private Data recorder;

    private double relativeTol, absoluteTol;

    // for output
    private ArrayList<Double> tvec;
    private ArrayList<Double[]> yvec;

    public LSODA(double hmin, double hmax, double absoluteTol, double relativeTol, int maxOrderN, int maxOrderS){
        if (hmin > 0)
            this.hmin = hmin;
        if (hmax>0)
            this.hmaxi = hmax;
        this.absoluteTol = absoluteTol;
        this.relativeTol = relativeTol;
        this.mxordn = maxOrderN;
        this.mxords = maxOrderS;
    }

    /**
     * compute P = I - h*beta_0*J and e_n^{[m+1]} - e_n^{[m]}
     * @param y   current value of y
     */

    private void calJacobian(double[] y) {
        nje++;
        ierpj = 0;
        jcur = 1;
        double hl0 = h * el0;
        /*
            only when miter == 2, this function will be called
         */
        if (miter != 2) {
            System.err.println("prja: miter!=2");
            throw new RuntimeException();
        }

        double fac = vmnorm(n, savf, ewt);
        double r0 = 1000.0 * Math.abs(h) * ETA * n * fac;
        if (r0 == 0.0)
            r0 = 1.0;
        double yj, r;
        for (int j = 1; j <= n; j++) {
            yj = y[j];
            // formula 3.35 in Description and Use of LSODE
            r = Math.max(sqrteta * Math.abs(yj), r0 / ewt[j]);
            y[j] += r;
            fac = -hl0 / r; // -h*b_0/delta Y
            acor = FirstOrderSystem(y, tn);  // f(y + delta y)
            for (int i = 1; i <= n; i++)
                wm[i][j] = (acor[i] - savf[i]) * fac; // J_{i,j}
            y[j] = yj;  // restore y
        }
        nfe += n;

        // Compute the norm of Jacobian
        pdnorm = fnorm(n, wm, ewt) / Math.abs(hl0);

        // Add identity matrix
        for (int i = 1; i <= n; i++)
            wm[i][i] += 1.0;

        // Do LU decomposition on P to calculate e^{[m+1]}-e^{[m]}
        ReturningValues res = Utility.LUDecomposition(wm, n);
        ipvt = res.ipvt;
        wm = res.a;
        if (res.info != 0)
            ierpj = 1; // singular matrix
    }


    /**
     * Compute the method and test coefficient.
     * The coefficients are given by a generating polynomial, i.e.,
     * <p>
     * l(x) = el[1] + el[2]*x + ... + el[nq+1]*x^nq.
     * <p>
     * For the implicit Adams method, l(x) is given by
     * <p>
     * dl/dx = (x+1)*(x+2)*...*(x+nq-1)/factorial(nq-1),   l(-1) = 0.
     * <p>
     * For the bdf methods, l(x) is given by
     * <p>
     * l(x) = (x+1)*(x+2)*...*(x+nq)/k,
     * <p>
     * where   k = factorial(nq)*(1+1/2+...+1/nq).
     * The tesco array contains test constants used for the
     * local error test and the selection of step size and/or order.
     * At order nq, tesco[nq][k] is used for the selection of step
     * size at order nq-1 if k = 1, at order nq if k = 2, and at order
     * nq+1 if k = 3.
     * (This method is from the paper: LINEAR MULTISTEP METHODS FOR
     * ORDINARY DIFFERENTIAL EQUATIONS: METHOD FORMULATIONS, STABILITY,
     * AND THE METHODS OF NORDSIECK AND GEAR.)
     *
     * @param meth 1 for nonstiff method and 2 for stiff method
     */
    private void cfode(int meth) {
        double[] pc = new double[13];  // coefficients for the polynomials
        double rqfac, rq1fac, pint, xpin, ragq;
        int qm1, qp1;
        // non-stiff method: AM
        if (meth == 1) {
            // coefficients for first order method
            elco[1][1] = 1.0;
            elco[1][2] = 1.0;
            tesco[1][1] = 0.0;
            tesco[1][2] = 2.0;
            tesco[2][1] = 1.0;
            tesco[12][3] = 0.0;
            pc[1] = 1.0;
            rqfac = 1.0;

            for (int q = 2; q <= 12; q++) {
/*
    The pc array will contain the coefficients of the polynomial
        p(x) = (x+1)*(x+2)*...*(x+nq-1).
    Initially, p(x) = 1.
 */
                rq1fac = rqfac; // 1/(q-1)!
                rqfac = rqfac / q; // 1/q!
                qm1 = q - 1;
                qp1 = q + 1;

                // form coefficients of p(x)*(x+q-1)
                pc[q] = 0;
                for (int i = q; i >= 2; i--)
                    pc[i] = pc[i - 1] + qm1 * pc[i];
                pc[1] = qm1 * pc[1];

                // compute the integral, -1 to 0, of p(x) and x*p(x)
                // since p(x)=dl/dx

                pint = pc[1];  // p(x)
                xpin = pc[1] / 2; // x*p(x)
                double tsign = 1.0;
                for (int i = 2; i <= q; i++) {
                    tsign = -tsign;
                    pint += tsign * pc[i] / (double) i;
                    xpin += tsign * pc[i] / (double) (i + 1);
                }

                // store coefficients for order q in elco and tesco
                elco[q][1] = pint * rq1fac;
                elco[q][2] = 1.0;
                for (int i = 2; i <= q; i++)
                    elco[q][i + 1] = rq1fac * pc[i] / i;
                ragq = 1 / (rqfac * xpin);
                tesco[q][2] = ragq;
                if (q < 12)
                    tesco[qp1][1] = ragq * rqfac / qp1;
                tesco[qm1][3] = ragq;
            }
            return;
        }

/*
    meth = 2. Coefficients for stiff method
 */
        pc[1] = 1;
        rq1fac = 1;
/*
   The pc array will contain the coefficients of the polynomial

      p(x) = (x+1)*(x+2)*...*(x+nq).

   Initially, p(x) = 1.
*/
        for (int q = 1; q <= 5; q++) {
            qp1 = q + 1;

            // form coefficients of p(x)*(x+nq)
            pc[qp1] = 0;
            for (int i = q + 1; i >= 2; i--)
                pc[i] = pc[i - 1] + q * pc[i];
            pc[1] *= q;

            // Store coefficients in elco and tesco
            for (int i = 1; i <= qp1; i++)
                elco[q][i] = pc[i] / pc[2];
            elco[q][2] = 1;
            tesco[q][1] = rq1fac;
            tesco[q][2] = qp1 / elco[q][1];
            tesco[q][3] = (q + 2) / elco[q][1];
            rq1fac /= q;
        }
    }


    /**
     * this method is used update the history matrix when h changes
     *
     * @param rh the ratio of old and new step size
     */
    private void scaleH(double rh) {
        /*
           If h is being changed, the h ratio rh is checked against
           rmax, hmin, and hmxi, and the yh array is rescaled.  ialth is set to
           l = nq + 1 to prevent a change of h for that many steps, unless
           forced by a convergence or error test failure.
         */
        rh = Math.min(rh, rmax);
        rh = rh / Math.max(1.0, Math.abs(h) * hmxi * rh);

        /*
        If meth = 1, also restrict the new step size by the stability region.

        If this reduces h, set irflag to 1 so that if there are roundoff
        problems later, we can assume that is the cause of the trouble.
        (this flag will be used in methodswitch)
         */
        if (meth == 1) {  // check the stability of nonstiff method
            irflag = 0;
            pdh = Math.max(Math.abs(h) * pdlast, 0.000001);
            if ((rh * pdh * 1.0001) >= sm1[nq]) {
                rh = sm1[nq] / pdh;
                irflag = 1;
            }
        }
        double r = 1;  // used to store r^j
        for (int j = 2; j <= l; j++) {
            r *= rh;
            for (int i = 1; i <= n; i++)
                yh[j][i] *= r;
        }
        h *= rh;
        rc *= rh;
        ialth = l;
    }

    /**
     * the ODE solving method that offers more customization parameters
     * @param neq number of equations
     * @param yp initial values of y
     * @param t start time
     * @param tout end time
     * @param itol type of error tolerance
     * @param rtol relative error tolerance
     * @param atol absolute error tolerance
     * @param itask types of integration
     * @param istate indicate whether this is the first call or not
     * @param iopt indicate whether the params are default or not
     * @param msg indicate whether to print extra message
     * @param maxstep max steps
     * @param maxhnil number of consecutive times that h is too small to recognize
     * @param maxordn max order for non-stiff method
     * @param maxords max order for stiff method
     * @param tmax max t for integrating
     * @param hinit initial step size
     * @param hmax max step size
     * @param hmin min step size
     */
    public void lsoda(int neq, double[] yp, double t, double tout, int itol, double[] rtol,
                      double[] atol, int itask, int istate, int iopt, int msg, int maxstep, int maxhnil,
                      int maxordn, int maxords, double tmax, double hinit,
                      double hmax, double hmin) {

        int mxstp0 = 1000000, mxhnl0 = 10, lenyh;
        boolean ihit = false;
        double atoli, ayi, big, h0 = 0, hmx, rh, rtoli,
                tcrit = 0, tdist, tnext, tol, tolsf, tp, size, sum, w0;
        tvec = new ArrayList<>();
        yvec = new ArrayList<>();

        if (write) {
            try {
                recorder = new Data("data", "lsoda.csv", neq);
            } catch (IOException e) {
                System.err.println("error in creating file");
                return;
            }
        }

/*
   Block a.
   This code block is executed on every call.
   It tests istate and itask for legality and branches appropriately.
   If istate > 1 but the flag init shows that initialization has not
   yet been done, an error return occurs.
   If istate = 1 and tout = t, return immediately.
*/
        if (istate < 1 || istate > 3) {
            terminate();
            throw new IstateException(istate);
        }

        if (itask < 1 || itask > 5) {
            terminate();
            throw new IllegalInputException("itask",itask);
        }

        if (init == 0 && (istate == 2 || istate == 3)) {
            terminate();
            throw new IstateException(istate, init);
        }

        if (istate == 1) {
            init = 0; // need initialisation
            if (tout == t) {
                ntrep++;
                if (ntrep < 5)
                    return;
                throw new RepeatedInputException();
            }
        }
/*
   Block b.
   The next code block is executed for the initial call ( *istate = 1 ),
   or for a continuation call with parameter changes ( *istate = 3 ).
   It contains checking of all inputs and various initializations.

   First check legality of the non-optional inputs neq, itol, iopt.
*/
        if (istate == 1 || istate == 3) {
            ntrep = 0;
            if (neq <= 0) {
                terminate();
                throw new NeqException(neq);
            }

            if (istate == 3 && neq > n) {
                terminate();
                throw new NeqException(neq, istate);
            }
            n = neq;

            if (itol < 1 || itol > 4) {
                terminate();
                throw new IllegalInputException("itol", itol);
            }

            if (iopt < 0 || iopt > 1) {
                terminate();
                throw new IllegalInputException("ipot", iopt);
            }

            /* Default options*/
            if (iopt == 0) {
                ixpr = 0;
                mxstep = mxstp0;
                mxhnil = mxhnl0;
                hmxi = 0.0;
                this.hmin = 0.0;
                if (istate == 1) {
                    h0 = 0;
                    mxordn = Math.min(mxordn,mord[1]);
                    mxords = Math.min(mxords,mord[2]);
                }
            } else {  // opt == 1
                ixpr = msg;
                if (ixpr < 0 || ixpr > 1) {
                    terminate();
                    throw new IllegalInputException("ixpr", ixpr);
                }
                mxstep = maxstep;
                if (mxstep < 0) {
                    terminate();
                    throw new IllegalInputException("mxstep", mxstep);
                }
                if (mxstep == 0)
                    mxstep = mxstp0;
                mxhnil = maxhnil;
                if (mxhnil < 0) {
                    terminate();
                    throw new IllegalInputException("mxhnil", mxhnil);
                }
                if (istate == 1) {
                    h0 = hinit;
//                    mxordn = maxordn;
                    if (mxordn < 0) {
                        terminate();
                        throw new IllegalInputException("mxordn", mxordn);
                    }
                    if (mxordn == 0)
                        mxordn = 100;
                    mxordn = Math.min(mxordn, mord[1]);
//                    mxords = maxords;
                    if (mxords < 0) {
                        terminate();
                        throw new IllegalInputException("mxords", mxords);
                    }
                    if (mxords == 0)
                        mxords = 100;
                    mxords = Math.min(mxords, mord[2]);
                    if ((tout - t) * h0 < 0.0) {
                        terminate();
                        throw new IllegalTException("tout_behind_t",t,tout,h0);
                    }
                }  // end if (istate == 1)
                if (hmax < 0.) {
                    terminate();
                    throw new IllegalInputException("hmax", hmax);
                }
                hmxi = 0.0;
                if (hmax > 0)
                    hmxi = 1.0 / hmax;
                this.hmin = hmin;
                if (this.hmin < 0.0) {
                    terminate();
                    throw new IllegalInputException("hmin", this.hmin);
                }

            } // end else; end iopt=1
        } // end if (istate == 1 || istate == 3)

        if (istate == 1) {
            sqrteta = Math.sqrt(ETA);
            meth = 1;
            int nyh = n;
            lenyh = 1 + Math.max(mxordn, mxords);

            yh = new double[1 + lenyh][1 + nyh];
            wm = new double[1 + nyh][1 + nyh];
            ewt = new double[1 + nyh];
            savf = new double[1 + nyh];
            acor = new double[1 + nyh];
            ipvt = new int[1 + nyh];
            y = new double[1+neq];
            System.arraycopy(yp,0,y,1,neq);
        }

/*
    Check rtol and atol for legality
 */
        if (istate == 1 || istate == 3) {
            rtoli = rtol[1];
            atoli = atol[1];
            for (int i = 1; i <= n; i++) {
                if (itol >= 3)
                    rtoli = rtol[i];
                if (itol == 2 || itol == 4)
                    atoli = atol[i];
                if (rtoli < 0.0) {
                    terminate();
                    throw new IllegalInputException("rtol", rtoli);
                }
                if (atoli < 0.0) {
                    terminate();
                    throw new IllegalInputException("rtol", atoli);
                }
            }
        } // end if (istate == 1|| istate ==3)

/*
    If istate = 3, set flag to signal parameter changes to stoda.
 */
        if (istate == 3) {
            jstart = -1;
        }

/*
   Block c.
   The next block is for the initial call only ( *istate = 1 ).
   It contains all remaining initializations, the initial call to f,
   and the calculation of the initial step size.
   The error weights in ewt are inverted after being loaded.
*/
        if (istate == 1) {
            tn = t;
            maxord = mxordn;
            if (itask == 4 || itask == 5) {
                tcrit = tmax;
                if ((tcrit - tout) * (tout - t) < 0.0) {
                    terminate();
                    throw new IllegalTException("tcrit_behind_tout", t,tout,tcrit);
                }
                if (h0 != 0.0 && (t + h0 - tcrit) * h0 > 0.0)
                    h0 = tcrit - t;
            }
            jstart = 0;
            nhnil = 0;
            nst = 0;
            nje = 0;
            nslast = 0;
            hu = 0.0;
            nqu = 0;
            mused = 0;
            miter = 0;
            ccmax = 0.3;
            maxcor = 3;
            msbp = 20;
            mxncf = 10;
            yh[2] = FirstOrderSystem(y, tn);
            nfe = 1;

/*
    Load the initial value vector in yh.
*/
            if (n >= 0)
                System.arraycopy(y, 1, yh[1], 1, n);
/*
    Load and invert the ewt array. (h is temporarily set to 1.)
 */
            nq = 1;
            h = 1.0;
            ewt = ewset(y, itol, rtol, atol, n);
            for (int i = 1; i <= n; i++) {
                if (ewt[i] <= 0.) {
                    terminate();
                    throw new EwtException(i,ewt[i]);
                }
                ewt[i] = 1. / ewt[i];  // inverse
            }

/*
    Compute h0, unless the user provide it.
    First check that tout - *t differs significantly from zero.
    A scalar tolerance quantity tol is computed, as max(rtol[i])
    if this is positive, or max(atol[i]/abs(y[i])) otherwise, adjusted
    to be between 100*ETA and 0.001.

    Then the computed value h0 is given by
      h0^(-2) = 1. / ( tol * w0^2 ) + tol * ( norm(f) )^2

    where  w0     = max( fabs(*t), fabs(tout) ),
           f      = the initial value of the vector f(t,y), and
           norm() = the weighted vector norm used throughout, given by
                    the vmnorm function routine, and weighted by the
                    tolerances initially loaded into the ewt array.

   The sign of h0 is inferred from the initial values of tout and *t.
   abs(h0) is made < abs(tout-*t) in any case.
 */
            if (h0 == 0.0) {
                tdist = Math.abs(tout - t);
                w0 = Math.max(Math.abs(t), Math.abs(tout));
                if (tdist < 2 * ETA * w0) {
                    terminate();
                    throw new IllegalTException("tout_close_to_t",t, tout,tcrit);
                }
                tol = rtol[1];
                if (itol > 2) {  // when rtol is an array
                    for (int i = 2; i <= n; i++)
                        tol = Math.max(tol, rtol[i]);
                }
                if (tol <= 0.0) {
                    atoli = atol[1];
                    for (int i = 1; i <= n; i++) {
                        if (itol == 2 || itol == 4) // atol is array
                            atoli = atol[i];
                        ayi = Math.abs(y[i]);
                        if (ayi != 0.0)
                            tol = Math.max(tol, atoli / ayi);
                    }
                }
                // 100*ETA <= tol <= 0.001
                tol = Math.max(tol, 100 * ETA);
                tol = Math.min(tol, 0.001);
                // norm of f
                sum = vmnorm(n, yh[2], ewt);
                sum = 1 / (tol * w0 * w0) + tol * sum * sum;  // 1/(h^2)
                h0 = 1 / Math.sqrt(sum);
                h0 = Math.min(h0, tdist);
                h0 = h0 * ((tout - t >= 0.0) ? 1 : -1);
            } // end if (h0 == 0.)

/*
    Adjust h0 if necessary to meet hmax bound.
 */
            rh = Math.abs(h0) * hmxi;
            if (rh > 1.)
                h0 /= rh;
/*
   Load h with h0 and scale yh[2] by h0.
*/
            h = h0;
            for (int i = 1; i <= n; i++)
                yh[2][i] *= h0;
        } /* if ( *istate == 1 )   */

/*
   Block d.
   The next code block is for continuation calls only ( *istate = 2 or 3 )
   and is to check stop conditions before taking a step.
 */
        int iflag;
        if (istate == 2 || istate == 3) {
            nslast = nst;
            switch (itask) {
                case 1:
                    if ((tn - tout) * h >= 0.) {
                        y = intdy(tout, 0, y, nq, tn, hu, ETA, h, yh, n);
                        iflag = (int) y[0];
                        y[0] = 0;
                        if (iflag != 0) {
                            terminate();
                            throw new InterpolationException(itask, tout);
                        }
                        t = tout;
                        istate = 2;
                        illin = 0;
                        return;
                    }
                    break;
                case 2:
                    break;
                case 3:
                    tp = tn - hu * (1 + 100 * ETA);
                    if ((tp - tout) * h > 0.0) {
                        terminate();
                        throw new IllegalTException("tout_behind_tcur_hu",tn,tout,itask);
                    }
                    if ((tn - tout) * h < 0.0)
                        break;
                    successReturn(ihit, tcrit, itask);
                    return;
                case 4:
                    tcrit = tmax;
                    if ((tn - tcrit) * h > 0.0) {
                        terminate();
                        throw new IllegalTException("tcrit_behind_tcur", tn, tout, tcrit);
                    }

                    if ((tcrit - tout) * h < 0.0) {
                        terminate();
                        throw new IllegalTException("tcrit_behind_tout", tn, tout, tcrit);
//                        return;
                    }
                    if ((tn - tout) * h >= 0.0) {
                        y = intdy(tout, 0, y, nq, tn, hu, ETA, h, yh, n);
                        iflag = (int) y[0];
                        y[0] = 0;
                        if (iflag != 0) {
                            terminate();
                            throw new InterpolationException(itask,tout);
                        }
                        t = tout;
                        istate = 2;
                        illin = 0;
                        return;
                    }
                case 5:
                    if (itask == 5) {
                        tcrit = tmax;
                        if ((tn - tcrit) * h > 0.0) {
                            terminate();
                            throw new IllegalTException("tcrit_behind_tcur", tn, tout, tcrit);
                        }
                    }
                    hmx = Math.abs(tn) + Math.abs(h);
                    ihit = Math.abs(tn - tcrit) <= (100 * ETA * hmx);
                    if (ihit) {
                        t = tcrit;
                        successReturn(ihit, tcrit, itask);
                        return;
                    }
                    tnext = tn + h * (1 - 4 * ETA);
                    if ((tnext - tcrit) * h <= 0.0)
                        break;
                    h = (tcrit - tn) * (1 - 4 * ETA);
                    if (istate == 2)
                        jstart = -2;
                    break;
            } // end switch
        } // end if (istate == 2 || istate == 3)

/*
   Block e.
   The next block is normally executed for all calls and contains
   the call to the one-step core integrator stoda.

   This is a looping point for the integration steps.

   First check for too many steps being taken, update ewt ( if not at
   start of problem).  Check for too much accuracy being requested, and
   check for h below the roundoff level in *t.
*/
        // record the initial value
        recording(t, y);
        while (true) {
            if (istate != 1 || nst != 0) {
                if ((nst - nslast) >= mxstep) {
                    istate = -1;
                    terminate2();
                    throw new ExceedMaxStepsException(mxstep);
                }
                ewt = ewset(yh[1], itol, rtol, atol, n);
                for (int i = 1; i <= n; i++) {
                    if (ewt[i] <= 0.0) {
                        istate = -6;
                        terminate2();
                        throw new EwtException(i,ewt[i]);
                    }
                    ewt[i] = 1 / ewt[i];
                }
            }
            tolsf = ETA * vmnorm(n, yh[1], ewt); // tolerance scale factor
            if (tolsf > 0.01) {
                tolsf = tolsf * 200;
                if (nst == 0) {
                    terminate();
                    throw new TooMuchAccuracyException(tolsf);
                }
                istate = -2;
                terminate2();
                throw new TooMuchAccuracyException(t,tolsf);
            }
            if ((tn + h) == tn) {
                nhnil++;
                if (nhnil <= mxhnil) {
                    System.err.printf("lsoda: warning.. internal t = %g and h = %g are\n" +
                            "         such that in the machine, t+h=t on the next step\n" +
                            "         solver will continue anyway", tn, h);
                    if (nhnil == mxhnil) {
                        System.err.printf("lsoda: above warning has been issued %d times,\n" +
                                "         it will be not be issued again for this problem", nhnil);
                    }
                }
            }
            /*
                Call stoda
             */
            stoda();

            if (kflag == 0) {
/*
   Block f.
   The following block handles the case of a successful return from the
   core integrator ( kflag = 0 ).
   If a method switch was just made, record tsw, reset maxord,
   set jstart to -1 to signal stoda to complete the switch,
   and do extra printing of data if ixpr = 1.
   Then, in any case, check for stop conditions.
*/
                init = 1;
                if (meth != mused) {  // method has been switched
                    maxord = mxordn;
                    if (meth == 2)
                        maxord = mxords;
                    jstart = -1;
                    if (ixpr != 0) {
                        if (meth == 2)
                            System.out.println("lsoda: a switch to the stiff method has occurred");
                        if (meth == 1)
                            System.out.println("lsoda: a switch to the nonstiff method has occurred");
                        System.out.printf("         at t = %g, tentative step size h = %g, step nst = %d\n",
                                tn, h, nst);
                    }
                }  // end if (meth != mused)

                if (itask == 1) {
                    if ((tn - tout) * h < 0.0) {
                        recording(tn, y);
                        continue;
                    }
                    y = intdy(tout, 0, y, nq, tn, hu, ETA, h, yh, n);
                    iflag = (int) y[0];
                    if (iflag != 0) {
                        terminate();
                        throw new InterpolationException(itask, tout);
                    }
                    y[0] = 0;
                    istate = 2;
                    illin = 0;

                    recording(tout, y);

                    return;
                }

                if (itask == 2) {
                    successReturn(ihit, tcrit, itask);
                    return;
                }

                if (itask == 3) {
                    if ((tn - tout) * h >= 0.0) {
                        successReturn(ihit, tcrit, itask);
                        return;
                    }
                    continue;
                }

                // See if tout or tcrit was reached. Adjust h if necessary
                if (itask == 4) {
                    if ((tn - tcrit) * h >= 0.0) {
                        y = intdy(tout, 0, y, nq, tn, hu, ETA, h, yh, n);
                        iflag = (int) y[0];
                        if (iflag != 0) {
                            terminate();
                            throw new InterpolationException(itask, tout);
                        }
                        y[0] = 0;
                        istate = 2;
                        illin = 0;

                        recording(tout, y);

                        return;
                    } else {
                        hmx = Math.abs(tn) + Math.abs(h);
                        ihit = Math.abs(tn - tcrit) <= (100 * ETA * hmx);
                        if (ihit) {
                            successReturn(ihit, tcrit, itask);
                            return;
                        }

                        recording(tn, y);

                        tnext = tn + h * (1 + 4 * ETA);
                        if ((tnext - tcrit) * h <= 0.0)
                            continue;
                        h = (tcrit - tn) * (1 - 4 * ETA);
                        jstart = -2;
                        continue;
                    }
                }
                if (itask == 5) {
                    hmx = Math.abs(tn) + Math.abs(h);
                    ihit = Math.abs(tn - tcrit) <= (100 * ETA * hmx);
                    successReturn(ihit, tcrit, itask);
                    return;
                }
            }
/*
   kflag = -1, error test failed repeatedly or with abs(h) = hmin.
   kflag = -2, convergence failed repeatedly or with abs(h) = hmin.
*/
            if (kflag == -1 || kflag == -2) {
                // find the largest component
                big = 0.0;
                imxer = 1;
                for (int i = 1; i <= n; i++) {
                    size = Math.abs(acor[i]) * ewt[i];
                    if (big < size) {
                        big = size;
                        imxer = i;
                    }
                }
                terminate2();
                if (kflag == -1) {
                    istate = -4;
                    throw new TestsFailException("error_test", tn, h);
                }
                if (kflag == -2) {
                    istate = -5;
                    throw new TestsFailException("convergence_test", tn, h);
                }
            }
        } // end while
    } // end lsoda

    private void terminate() {
        if (illin == 5) {
            System.err.println("lsoda: repeated occurrence of illegal input");
            System.err.println("       run aborted.. apparent infinite loop");
        } else {
            illin++;
            istate = -3;
        }
    }

    private void terminate2() {
        if (n >= 0)
            System.arraycopy(yh[1], 1, y, 1, n);
        illin = 0;
    }

    private void successReturn(boolean ihit, double tcrit, int itask) {
        if (n >= 0)
            System.arraycopy(yh[1], 1, y, 1, n);

        double t = tn;
        if (itask == 4 || itask == 5)
            if (ihit)
                t = tcrit;
        istate = 2;
        illin = 0;
        recording(t, y);
    }

    private void resetCoeff() {
        el = elco[nq];
        rc = rc * el[1] / el0;  // ratio of new and old h*el[0]
        el0 = el[1];    // beta_0
        conit = 0.5 / (nq + 2);
    }

    /**
     * integrate one step
     */
    private void stoda() {
        int orderflag = 0;
        double dsm = 0, dup, exup, r;
        double pnorm;

        // initialisation in every single step
        kflag = 0;
        told = tn;
        ncf = 0;  // number of convergence failure
        ierpj = 0;
        jcur = 0;
        delp = 0;  // D_past

        /*
            On the first call, the order is set to 1, and other variables are initialised.
            rmax is the maximum ratio by which h can be increased in a single step.
            It is initially 1.e4 to compensate for the small initial h, but then is normally equal to 10.
            If a failure occurs (in corrector convergence or error test), rmax is set at 2 for the next increase.
         */
        if (jstart == 0) {
            lmax = maxord + 1;
            nq = 1;
            l = 2;
            ialth = 2;  // q+1
            rmax = 10000;
            rc = 0;
            el0 = 1;
            crate = 0.7;
            hold = h;
            nslp = 0;  // Steps when the last Jacobian update
            ipup = miter;

            // Initialise switching parameters
            icount = 20;
            irflag = 0;
            pdest = 0;
            pdlast = 0;
            ratio = 5;
            cfode(2);
            for (int i = 1; i <= 5; i++)
                cm2[i] = tesco[i][2] * elco[i][i + 1];
            cfode(1);
            for (int i = 1; i <= 12; i++)
                cm1[i] = tesco[i][2] * elco[i][i + 1];
            resetCoeff();
        } // end if (jstart == 0)

        if (jstart == -1) {  // error test failed
            ipup = miter;
            lmax = maxord + 1;
            if (ialth == 1)
                ialth = 2;
            if (meth != mused) {
                cfode(meth);
                ialth = l;
                resetCoeff();
            }
            if (h != hold) {
                rh = h / hold;
                h = hold;
                scaleH(rh);
            }
        } // end if (jstart == -1)

        if (jstart == -2) {
            if (h != hold) {
                rh = h / hold;
                h = hold;
                scaleH(rh);
            }
        }  // end if (jstart == -2)

/*
    Prediction
 */
        while (true) {
            while (true) {
                // whether to update P
                // 1. the relative change of hb_0
                if (Math.abs(rc - 1) > ccmax)
                    ipup = miter;
                // 2. 20 steps have passed
                if (nst >= nslp + msbp)
                    ipup = miter;

                tn += h;
                // compute z_n^[0] = z_{n-1}xA
                for (int j = nq; j >= 1; j--)
                    for (int i = j; i <= nq; i++) {
                        for (int ii = 1; ii <= n; ii++)
                            yh[i][ii] += yh[i + 1][ii];
                    }

                pnorm = vmnorm(n, yh[1], ewt);

                // correction
                correction(pnorm);

                if (corflag == 0)
                    break;
                if (corflag == 1) {
                    rh = Math.max(rh, hmin / Math.abs(h));
                    scaleH(rh);
                    continue;
                }

                if (corflag == 2) {  // fail to converge
                    kflag = -2;
                    hold = h;
                    jstart = 1;
                    return;
                }
            }  // end inner while (corrector loop)

            jcur = 0;
            if (m == 0)
                dsm = del / tesco[nq][2];
            if (m > 0)
                dsm = vmnorm(n, acor, ewt) / tesco[nq][2];
            if (dsm <= 1) { // pass the error test
                kflag = 0;
                nst++;
                hu = h;
                nqu = nq;
                mused = meth;
                for (int j = 1; j <= l; j++) {
                    r = el[j];
                    for (int i = 1; i <= n; i++)
                        yh[j][i] += r * acor[i];
                }

                icount--;
                if (icount < 0) {
                    methodSwitch(dsm, pnorm);
                    if (meth != mused) {
                        rh = Math.max(rh, hmin / Math.abs(h));
                        scaleH(rh);
                        rmax = 10;
                        endStoda();
                        break;
                    }
                }

                ialth--;
                if (ialth == 0) {
                    rhup = 0;
                    if (l != lmax) {
                        for (int i = 1; i <= n; i++)
                            savf[i] = acor[i] - yh[lmax][i];
                        dup = vmnorm(n, savf, ewt) / tesco[nq][3];  //D_{q+1}
                        exup = 1.0 / (l + 1);
                        rhup = 1.0 / (1.4 * Math.pow(dup, exup) + 0.0000014);  // r_up
                    }

                    orderflag = orderSwitch(dsm);

                    // No change in h or nq.
                    if (orderflag == 0) {
                        endStoda();
                        break;
                    }
                    // h is changed, but ont nq.
                    if (orderflag == 1) {
                        rh = Math.max(rh, hmin / Math.abs(h));
                        scaleH(rh);
                        rmax = 10;
                        endStoda();
                        break;
                    }
                    // both nq and h are changed.
                    if (orderflag == 2) {
                        resetCoeff();
                        rh = Math.max(rh, hmin / Math.abs(h));
                        scaleH(rh);
                        rmax = 10;
                        endStoda();
                        break;
                    }
                }  // end if (ialth == 0)

                if (ialth > 1 || l == lmax) {
                    endStoda();
                    break;
                }
                if (n >= 0)
                    System.arraycopy(acor, 1, yh[lmax], 1, n);
                endStoda();
                break;
            }   // end if (dsm <= 1)
/*
    The error test failed
 */
            else {
                kflag--;
                tn = told;
                // restore y_{n-1}
                for (int j = nq; j >= 1; j--)
                    for (int i = j; i <= nq; i++) {
                        for (int ii = 1; ii <= n; ii++)
                            yh[i][ii] -= yh[i + 1][ii];
                    }
                rmax = 2;
                if (Math.abs(h) <= hmin * 1.00001) {
                    kflag = -1;
                    hold = h;
                    jstart = 1;
                    break;
                }

                if (kflag > -3) {
                    rhup = 0;
                    orderflag = orderSwitch(dsm);
                    if (orderflag == 1 || orderflag == 0) {
                        if (orderflag == 0)
                            rh = Math.min(rh, 0.2);
                        rh = Math.max(rh, hmin / Math.abs(h));
                        scaleH(rh);
                    }
                    if (orderflag == 2) {
                        resetCoeff();
                        rh = Math.max(rh, hmin / Math.abs(h));
                        scaleH(rh);
                    }
                } // end if (kflag>-3)

                else {  // kflag <= -3
                    if (kflag == -mxncf) {
                        kflag = -1;
                        hold = h;
                        jstart = 1;
                        break;
                    } else {
                        rh = 0.1;
                        rh = Math.max(hmin / Math.abs(h), rh);
                        h *= rh;
                        if (n >= 0) System.arraycopy(yh[1], 1, y, 1, n);
                        savf = FirstOrderSystem(y, tn);
                        nfe++;
                        for (int i = 1; i <= n; i++)
                            yh[2][i] = h * savf[i];
                        ipup = miter;
                        ialth = 5;
                        if (nq == 1)
                            continue;
                        nq = 1;
                        l = 2;
                        resetCoeff();
                    }
                }  // end else kflag <= -3
            }  // end error failure handling
        }  // end outer while
    }

    /**
     * perform the correction iterations
     * @param pnorm norm of y
     */
    private void correction(double pnorm) {
        double rm, rate, dcon;

        m = 0;
        corflag = 0;
        rate = 0;
        del = 0;
        if (n >= 0) System.arraycopy(yh[1], 1, y, 1, n);
        savf = FirstOrderSystem(y, tn);
        nfe++;

        while (true) {
            if (m == 0) {   // first correction iteration
                if (ipup > 0) {   // update P
                    calJacobian(y);
                    ipup = 0;
                    rc = 1;
                    nslp = nst;
                    crate = 0.7;
                    if (ierpj != 0) {  // iteration matrix was found singular
                        corFailure();
                        return;
                    }
                }
                for (int i = 1; i <= n; i++)
                    acor[i] = 0;
            }  // end if (m == 0)

            if (miter == 0) {    // functional iteration
                for (int i = 1; i <= n; i++) {
                    savf[i] = h * savf[i] - yh[2][i];
                    y[i] = savf[i] - acor[i];  // calculate g
                }
                del = vmnorm(n, y, ewt);  // w_m
                for (int i = 1; i <= n; i++) {
                    y[i] = yh[1][i] + el[1] * savf[i];  // y^[m+1]_n
                    acor[i] = savf[i];  // e^[m+1]
                }
            }
            else {  // Newton Iteration
                for (int i = 1; i <= n; i++)
                    y[i] = h * savf[i] - (yh[2][i] + acor[i]); // h(f-y')-e =g
                y = solsy(y, wm, n, ipvt, miter);  // g = P(e^[m+1]-e^[m])
                del = vmnorm(n, y, ewt);
                for (int i = 1; i <= n; i++) {
                    acor[i] += y[i];    // e^[m+1]
                    y[i] = yh[1][i] + el[1] * acor[i];  // y^[m+1]
                }
            }

        /*
            Test for convergence
         */
            if (del <= 100 * pnorm * ETA)
                break;
            if (m != 0 || meth != 1) {
                if (m != 0) {
                    rm = 1024;
                    if (del <= (1024 * delp))
                        rm = del / delp;
                    rate = Math.max(rate, rm);
                    crate = Math.max(0.2 * crate, rm);
                }
                dcon = del * Math.min(1, 1.5 * crate) / (tesco[nq][2] * conit);
                if (dcon <= 1) {    // converge successfully
                    pdest = Math.max(pdest, rate / Math.abs(h * el[1]));
                    if (pdest != 0.0)
                        pdlast = pdest;
                    break;
                }
            }
        /*
            The corrector iteration failed to converge.
         */
            m++;
            if (m == maxcor || (m >= 2 && del > 2 * delp)) {
                if (miter == 0 || jcur == 1) {
                    corFailure();
                    return;
                }
                ipup = miter;

                // restart corrector if Jacobian is recmoputed
                m = 0;
                rate = 0;
                del = 0;
                if (n >= 0)
                    System.arraycopy(yh[1], 1, y, 1, n);
            }
            // iterate corrector
            else {
                delp = del;
            }
            savf = FirstOrderSystem(y, tn);
            nfe++;
        }
    }

    /**
     * automatically detect whether to change the method
     *
     * @param dsm   D_same
     * @param pnorm the norm of y
     */
    private void methodSwitch(double dsm, double pnorm) {
        double rh1, rh2, rh1it, exsm, exm1, exm2, dm1, dm2, alpha;
        int nqm1, nqm2, lm1, lm2, lm1p1, lm2p1;

        if (meth == 1) {  // nonstiff method
            if (nq > 5)
                return;
            if (dsm <= (100 * pnorm * ETA) || pdest == 0.0) {  // affected by round-off error
                if (irflag == 0)    //  but not affected by stability
                    return;
                rh2 = 2;
                nqm2 = Math.min(nq, mxords);
            } else {
                exsm = 1.0 / l;
                rh1 = 1.0 / (1.2 * Math.pow(dsm, exsm) + 0.0000012);    // r_same
                rh1it = 2 * rh1;
                pdh = pdlast * Math.abs(h);
                if ((pdh * rh1) > 0.00001)  // stability region
                    rh1it = sm1[nq] / pdh;
                rh1 = Math.min(rh1, rh1it);

                if (nq > mxords) {
                    nqm2 = mxords;
                    lm2 = mxords + 1;
                    exm2 = 1.0 / lm2;
                    lm2p1 = lm2 + 1;
                    dm2 = vmnorm(n, yh[lm2p1], ewt) / cm2[mxords];  // D
                    rh2 = 1.0 / (1.2 * Math.pow(dm2, exm2) + 0.0000012);
                } else {
                    dm2 = dsm * (cm1[nq] / cm2[nq]);  // D
                    rh2 = 1.0 / (1.2 * Math.pow(dm2, exsm) + 0.0000012);
                    nqm2 = nq;
                }

                // cannot change to stiff method
                if (rh2 < ratio * rh1)  // ratio = M+ = 5
                    return;
            }

            // The method switch test passed
            rh = rh2;
            icount = 20;
            meth = 2;
            miter = 2;
            pdlast = 0;
            nq = nqm2;
            l = nq + 1;
            return;
        }   // end if (meth == 1)

        // if the current method is stiff method
        exsm = 1.0 / l;   // 1/(nq+1)
        if (mxordn < nq) {
            nqm1 = mxordn;
            lm1 = mxordn + 1;
            exm1 = 1.0 / lm1;
            lm1p1 = lm1 + 1;
            dm1 = vmnorm(n, yh[lm1p1], ewt) / cm1[mxordn];
            rh1 = 1.0 / (1.2 * Math.pow(dm1, exm1) + 0.0000012);   //r_same
        } else {
            dm1 = dsm * (cm2[nq] / cm1[nq]);
            rh1 = 1.0 / (1.2 * Math.pow(dm1, exsm) + 0.0000012);
            nqm1 = nq;
            exm1 = exsm;
        }

        rh1it = 2 * rh1;
        pdh = pdnorm * Math.abs(h);
        if ((pdh * rh1) > 0.0001)  // stability region for nonstiff method
            rh1it = sm1[nqm1] / pdh;
        rh1 = Math.min(rh1, rh1it);
        rh2 = 1.0 / (1.2 * Math.pow(dsm, exsm) + 0.0000012);
        // if changing method will not improve the efficiency, then return
        if ((rh1 * ratio) < (5 * rh2))
            return;

        alpha = Math.max(0.001, rh1);
        dm1 *= Math.pow(alpha, exm1);
        if (dm1 <= 1000 * ETA * pnorm)
            return;

        // The switch test passed
        rh = rh1;
        icount = 20;
        meth = 1;
        miter = 0;
        pdlast = 0;
        nq = nqm1;
        l = nq + 1;
    }

    private void endStoda() {
        double r = 1.0 / tesco[nqu][2];
        for (int i = 1; i <= n; i++)
            acor[i] *= r;
        hold = h;
        jstart = 1;
    }

    /**
     * check whether to change the method order
     * @param dsm D_same
     * @return order flag
     */
    private int orderSwitch(double dsm) {
        int newq;
        double exsm, rhdn, rhsm, ddn, exdn, r;
        int orderflag = 0;
        exsm = 1.0 / l;
        rhsm = 1.0 / (1.2 * Math.pow(dsm, exsm) + 0.0000012);

        rhdn = 0;
        if (nq != 1) {
            ddn = vmnorm(n, yh[l], ewt) / tesco[nq][1];   // D_down
            exdn = 1.0 / nq;
            rhdn = 1.0 / (1.3 * Math.pow(ddn, exdn) + 0.0000013);  // r_down
        }

        // stability region for nonstiff method
        if (meth == 1) {
            pdh = Math.max(Math.abs(h) * pdlast, 0.000001);
            if (l < lmax)
                rhup = Math.min(rhup, sm1[l] / pdh);
            rhsm = Math.min(rhsm, sm1[nq] / pdh);
            if (nq > 1)
                rhdn = Math.min(rhdn, sm1[nq - 1] / pdh);
            pdest = 0;
        }

        if (rhsm >= rhup) {
            if (rhsm >= rhdn) {  // keeping the same order gives the max step
                newq = nq;
                rh = rhsm;
            } else {
                newq = nq - 1;
                rh = rhdn;
                if (kflag < 0 && rh > 1)
                    rh = 1;
            }
        } else {
            if (rhup <= rhdn) {  // decrease the order
                newq = nq - 1;
                rh = rhdn;
                if (kflag < 0 && rh > 1)
                    rh = 1;
            } else {  // increase the order
                rh = rhup;
                if (rh >= 1.1) {
                    r = el[l] / l;
                    nq = l;
                    l = nq + 1;
                    for (int i = 1; i <= n; i++)
                        yh[l][i] = acor[i] * r;
                    orderflag = 2;
                } else {
                    ialth = 3;
                }
                return orderflag;
            }
        }

        if (meth == 1) {
            if ((rh * pdh * 1.00001) < sm1[newq])
                if (kflag == 0 && rh < 1.1) {
                    ialth = 3;
                    return orderflag;
                }
        } else {
            if (kflag == 0 && rh < 1.1) {
                ialth = 3;
                return orderflag;
            }
        }
        if (kflag <= -2)    // not pass the convergence test
            rh = Math.min(rh, 0.2);

        if (newq == nq) {
            orderflag = 1;
            return orderflag;
        }

        nq = newq;
        l = nq + 1;
        orderflag = 2;
        return orderflag;
    }

    private void corFailure() {
        ncf++;
        rmax = 2;
        tn = told;
        for (int j = nq; j >= 1; j--)      // recovery
            for (int i = j; i <= nq; i++) {
                for (int ii = 1; ii <= n; ii++)
                    yh[i][ii] -= yh[i + 1][ii];
            }

        if (Math.abs(h) <= hmin * 1.00001 || ncf == mxncf) {
            corflag = 2;
            return;
        }
        corflag = 1;
        rh = 0.25;   // reduce the step size
        ipup = miter;
    }

    /**
     * record the values of each step
     * @param t the value of t
     * @param values the value of y
     */
    private void recording(double t, double[] values) {
        tvec.add(t);
        yvec.add(Utility.transformYVec(values));
        if (write)
            recorder.write(t, values);
    }

    public double[] FirstOrderSystem(double[] y, double t) {
        double[] ydot = new double[ode.getDimension()];
        ode.computeDerivatives(t, Arrays.copyOfRange(y,1,ode.getDimension()+1), ydot);
        double[] ydot1 = new double[ode.getDimension()+1];
        System.arraycopy(ydot, 0, ydot1, 1, ode.getDimension());
        return ydot1;
    }

    /**
     * the ODE integrating method
     * @param ode the definition of the ordinary differential equations
     * @param t0 the initial value of t
     * @param y the initial values
     * @param tout the output station
     * @param result the array used to store the integrating result
     * @throws DimensionMismatchException
     * @throws NumberIsTooSmallException
     * @throws MaxCountExceededException
     * @throws NoBracketingException
     */
    @Override
    public double integrate(FirstOrderDifferentialEquations ode, double t0, double[] y, double tout, double[] result) throws DimensionMismatchException, NumberIsTooSmallException, MaxCountExceededException, NoBracketingException {
        this.ode = ode;
        double[] rtol = new double[]{0, this.relativeTol};
        double[] atol = new double[]{0, this.absoluteTol};
        this.lsoda(ode.getDimension(), y,t0,tout,1,rtol,atol,
                1,1,1,0,0,0,0,0, 0.0,0.0,hmaxi,hmin);
        System.arraycopy(this.y,1,result,0,ode.getDimension());
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void addStepHandler(StepHandler stepHandler) {

    }

    @Override
    public Collection<StepHandler> getStepHandlers() {
        return null;
    }

    @Override
    public void clearStepHandlers() {

    }

    @Override
    public void addEventHandler(EventHandler eventHandler, double v, double v1, int i) {

    }

    @Override
    public void addEventHandler(EventHandler eventHandler, double v, double v1, int i, UnivariateSolver univariateSolver) {

    }

    @Override
    public Collection<EventHandler> getEventHandlers() {
        return null;
    }

    @Override
    public void clearEventHandlers() {

    }

    @Override
    public double getCurrentStepStart() {
        return tn;
    }

    @Override
    public double getCurrentSignedStepsize() {
        return h;
    }

    @Override
    public void setMaxEvaluations(int i) {

    }

    @Override
    public int getMaxEvaluations() {
        return 0;
    }

    @Override
    public int getEvaluations() {
        return nfe;
    }

    public int getJacobianEvaluations(){
        return nje;
    }

    public int getStepsTaken(){
        return nst;
    }

    public int getMaxComponent(){
        return imxer;
    }

    public ArrayList<Double> getTvec(){
        return tvec;
    }

    public ArrayList<Double[]> getYvec(){
        return yvec;
    }
}
