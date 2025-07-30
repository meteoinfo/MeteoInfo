package org.meteoinfo.math.integrate.lsoda;

public class SupportingFunctions {
    /**
     * @param n number of elements
     * @param v value of the vector
     * @param w weights
     * @return vector norm
     */
    public static double vmnorm(int n, double[] v, double[] w){
        double vm = 0.0;
        for (int i=1; i<=n; i++)
            vm = Math.max(vm, Math.abs(v[i])*w[i]);
        return vm;
    }

    /**
     * full matrix norm
     * Computes the norm of a full n by n matrix,
     * stored in the array a, that is consistent with the weighted max-norm
     * on vectors, with weights stored in the array w.
     *       fnorm = max(i=1,...,n) ( w[i] * sum(j=1,...,n) fabs( a[i][j] ) / w[j] )
     * @param n dimensions
     * @param a the matrix
     * @param w weights
     * @return matrix norm
     */
    public static double fnorm(int n, double[][] a, double[] w){
        double an = 0.0, sum;
        for (int i=1; i <= n ; i++){
            sum = 0;
            for(int j=1; j<=n; j++)
                sum += Math.abs(a[i][j])/w[j];
            an = Math.max(an, sum*w[i]);
        }
        return an;
    }

    /**
     * compute EWT
     * @param ycur current value of y
     */
    public static double[] ewset(double[] ycur, int itol, double[] rtol, double[] atol, int n){
        int i;
        double[] EWT = new double[n+1];
        switch (itol){
            case 1:
                for (i=1; i<=n; i++)  // both are scalars
                    EWT[i] = rtol[1] * Math.abs(ycur[i])+atol[1];
                break;
            case 2:
                for (i=1;i<=n; i++) //scalar rtol and array atol
                    EWT[i] = rtol[1] * Math.abs(ycur[i]) + atol[i];
                break;
            case 3:
                for (i=1;i<=n; i++) //array rtol and scalar atol
                    EWT[i] = rtol[i] * Math.abs(ycur[i]) + atol[1];
                break;
            case 4:
                for (i=1;i<=n; i++) // both are arrays
                    EWT[i] = rtol[i] * Math.abs(ycur[i]) + atol[i];
                break;
        }
        return EWT;
    }

    /**
     * Manages the solution of the linear system arising from
     * a chord iteration.  It is called if miter != 0.
     * @param y the right-hand side vector on input, and the solution vector on output.
     * @return solution of the linear system
     */
    public static double[] solsy(double[] y, double[][] wm, int n, int[] ipvt, int miter){
        if (miter != 2){
            System.out.println("solsy: miter != 2");
            return y;
        }
        return Utility.solveLinearSys(wm,n,ipvt,y);
    }

    /**
     * interpolate at the output station
     * @param t value of independent variable
     * @param k integer that specifies the desired derivative order
     */
    public static double[] intdy(double t, int k, double[] dky, int nq, double tn,
                                 double hu, double ETA, double h, double[][] yh, int n){
        int ic ,jp1;
        double r, s, tp;

        if (k < 0 || k > nq){
            System.err.printf("intdy: k = %d illegal", k);
            dky[0] = -1;
            return dky;
        }
        tp = tn - hu -100.0 * ETA * (tn + hu);
        if ((t-tp) * (t-tn)> 0.0){
            System.err.printf("intdy: t = %f illegal\n" +
                    "t not in interval tcur-hu to tcur\n", t);
            dky[0] = -2;
            return dky;
        }

        s = (t-tn)/h;
        ic = 1;
        for (int i = nq+1-k; i <= nq; i++){
            ic *= i;   // ic = nq(nq-1)...(nq-k+1)
        }
        for (int i=1; i<=n; i++)
            dky[i] = ic* yh[nq+1][i];

        // use Horner's rule
        for (int j = nq-1; j >= k; j--){
            jp1 = j+1;  // order of current derivatives
            ic = 1;
            for (int jj = jp1-k; jj<=j;jj++)
                ic *= jj;   // coefficient
            for (int i=1; i<=n; i++)
                dky[i] = ic * yh[jp1][i] + s * dky[i];
        }
        if (k == 0){
            return dky;
        }
        r = Math.pow(h, -k);
        for (int i=1; i<=n; i++)
            dky[i] = r*dky[i];

        return dky;
    }
}
