package org.meteoinfo.math.integrate.lsoda;

import java.util.Arrays;

public class Utility {
    /**
     * ddot.c
     * @param n number of elements
     * @param dx n+1 vector (dx[0] is not used)
     * @param incx storage spacing between elements of dx
     * @param dy n+1 vector (dy[0] is not used)
     * @param incy storage spacing between elements of dy
     * @return dot product of dx and dy
     */
    public static double Dot(int n, double[] dx, int incx, double[] dy, int incy){
        double dotProd = 0;  // result
        int ix = 1, iy = 1;  // index of dx and dy
        if (n <= 0)
            return dotProd;

        // unequal or non-positive increments
        if( incx != incy || incx < 1){

            if (incx < 0)
                ix = (-n+1) * incx + 1;
            if (incx < 0)
                iy = (-n+1) * incy + 1;

            for(int i=1;i<=n;i++){
                dotProd += dx[ix] * dy[iy];
                ix += incx;
                iy += incy;
            }
            return dotProd;
        }

        // Both increments equal to 1
        if(incx == 1){
            int m = n % 5;
            // Clean-up loop so remaining vector length is a multiple of 5
            if (m != 0){
                for (int i=1; i<=m;i++)
                    dotProd += dx[i] * dy[i];
                if (n < 5)
                    return dotProd;
            }
            for (int i=m+1; i<=n;i=i+5)
                dotProd += dx[i] * dy[i] + dx[i+1] * dy[i+1] +
                        dx[i+2] * dy[i+2] + dx[i+3] * dy[i+3] +
                        dx[i+4] * dy[i+4];
            return dotProd;
        }
        // positive equal non-unit increments
        for (int i=1; i<= n*incx; i=i+incx)
            dotProd += dx[i] * dy[i];

        return dotProd;
    }

    /**
     * idamax.c
     * Find largest component of double vector dx
     * @param n number of elements
     * @param dx n+1 vector (dx[0] is not used)
     * @param incx storage spacing between elements of dx
     * @return smallest index
     */
    public static int findMaxMagnitude(int n, double[] dx, int incx){
        double maxValue, curValue;
        int xindex = 0;

        if (n <= 0)
            return xindex;
        xindex = 1;
        if (n <= 1 || incx <= 0)
            return xindex;

        maxValue = Math.abs(dx[1]);
        // increments are not euqal to 1
        if (incx != 1){
            int curIndex = 2;
            for (int i=1+incx; i <= n*incx; i=i+incx){
                curValue = Math.abs(dx[i]);
                if (curValue > maxValue){
                    xindex = curIndex;
                    maxValue = curValue;
                }
                curIndex++;
            }
            return xindex;
        }

        // increments are equal to 1.
        for (int i=2; i <= n; i++){
            curValue = Math.abs(dx[i]);
            if (curValue > maxValue){
                xindex = i;
                maxValue = curValue;
            }
        }
        return xindex;
    }

    /**
     * dscal.c
     * scalar vector multiplication
     * @param n number of elements
     * @param da double scalar multiplier
     * @param dx start+n-1 vector (dx[0] is not used)
     * @param incx storage spacing between elements of dx (should be positive)
     * @param start start position
     * @return da*dx
     */
    public static double[] calAX(int n, double da, double[] dx, int incx, int start){
        if (n <= 0 || incx * (n-1) + start >= dx.length){
            System.out.println("function calAX: illegal input");
            return dx;
        }

        // increments are not equal to 1
        if (incx != 1){
            for (int i = start; i <= (n-1)*incx + start; i = i+incx)
                dx[i] = da * dx[i];
            return dx;
        }

        // increments are equal to 1
        int m = n % 5;
        if ( m != 0 ) {
            for (int i = start ; i <= m+start - 1 ; i++ )
                dx[i] = da * dx[i];
            if ( n < 5 )
                return dx;
        }
        for (int i = m + start; i <= n + start - 1; i = i + 5 ) {
            dx[i] = da * dx[i];
            dx[i+1] = da * dx[i+1];
            dx[i+2] = da * dx[i+2];
            dx[i+3] = da * dx[i+3];
            dx[i+4] = da * dx[i+4];
        }
        return dx;
    }

    /**
     * daxpy.c
     * @param n number of elements
     * @param da double scalar multiplier
     * @param dx start+n-1 vector (dx[0] is not used)
     * @param incx storage spacing between elements of dx
     * @param dy start+n-1 vector (dy[0] is not used)
     * @param incy storage spacing between elements of dy
     * @param ystart start position of y
     * @param xstart start position of x
     * @return da * dx + dy
     */
    public static double[] calAXPlusY(int n, double da, double[] dx, int incx, double[] dy, int incy, int xstart, int ystart){
        int ix, iy; // index

        if (incx * (n-1) + xstart >= dx.length||incy * (n-1) + ystart >= dy.length||n<=0){
            System.out.println("function calAXPlusY: illegal input");
            return dy;
        }

        if ( da == 0.0)
            return dy;

        // nonequal or nonpositive increments
        if (incx != incy || incx < 1){
            ix = xstart;
            iy = ystart;
            if (incx < 0)
                ix = (-n+1) * incx + xstart;
            if (incx < 0)
                iy = (-n+1) * incy + ystart;
            for (int i=1; i<=n; i++){
                dy[iy] += da * dx[ix];
                ix += incx;
                iy += incy;
            }
            return dy;
        }

        // increments are equal to 1
        if (incx == 1){
            // Clean-up loop
            int m = n % 4;
            if (m != 0){
                for (int i=1; i<=m; i++)
                    dy[i+ystart-1] += da * dx[i+xstart-1];
                if (n < 4)
                    return dy;
            }
            for (int i=m+1; i <= n; i=i+4){
                dy[i+ystart-1] = dy[i+ystart-1] + da * dx[i+xstart-1];
                dy[i+ystart] = dy[i+ystart] + da * dx[i+xstart];
                dy[i+ystart+1] = dy[i+ystart+1] + da * dx[i+xstart+1];
                dy[i+ystart+2] = dy[i+ystart+2] + da * dx[i+xstart+2];
            }
            return dy;
        }

        // incrememts are equal, positive but not unit
        for (int i = 1; i <= n*incx; i=i+incx)
            dy[ystart+i-1] += da*dx[xstart+i-1];
        return dy;
    }


    /**
     * LU decomposition using partial pivoting
     * @param a (n+1)x(n+1) matrix
     * @param n dimension
     * @return a, ipvt, info
     * if info != 0, the matrix is singular
     */
    public static ReturningValues LUDecomposition(double[][] a, int n){
        double t;
        int info = 0;
        int[] ipvt = new int[n+1];

        for (int k=1 ; k<=n-1 ; k++){
            // Find j = pivot index
            int j = findMaxMagnitude(n-k+1, Arrays.copyOfRange(a[k],k-1,n+1),1)+k-1;
            ipvt[k] = j;

            // zero pivot implies this row already triangularised
            if (a[k][j] == 0.0){
                info = k;
                continue;
            }

            // interchange
            if(j != k){
                t = a[k][j];
                a[k][j] = a[k][k];
                a[k][k] = t;
            }

            // compute multipliers
            t = - 1.0/ a[k][k];
            a[k] = calAX(n-k, t, a[k],1, k+1);

            // column elimination with row indexing
            for (int i=k+1; i<=n;i++){
                t = a[i][j];
                if(j != k){
                    a[i][j] = a[i][k];
                    a[i][k] = t;
                }
                a[i] = calAXPlusY(n-k, t, a[k], 1, a[i], 1,k+1,k+1);
            }
        }

        ipvt[n] = n;
        if(a[n][n] == 0.0)
            info = n;

        return new ReturningValues(a, ipvt, info);
    }

    /**
     * solves the linear system using the factors computed by LUDecomposition
     * @param a (n+1)x(n+1) matrix
     * @param n row dimension of a
     * @param ipvt the pivot vector from LUDecomposition
     * @param b the right hand side vector
     * @return b the solution vector x.
     */
    public static double[] solveLinearSys(double[][] a, int n, int[] ipvt, double[] b){
        int j;
        double t;

        // Ax = LUx = b; Let Ux = y, so Ly = b.
        // 1. Solve L * y = b.
        for(int k=1; k <= n; k++){
            t = Dot(k-1, Arrays.copyOfRange(a[k],0,k),1,b,1);
            b[k] = (b[k]-t) / a[k][k];
        }

        // 2. Solve U * x = y.
        for (int k=n-1; k >= 1; k--){
            b[k] = b[k] + Dot(n-k, Arrays.copyOfRange(a[k],k,n+1),1, Arrays.copyOfRange(b,k,n+1),1);
            j = ipvt[k];
            if (j != k){
                t = b[j];
                b[j] = b[k];
                b[k] = t;
            }
        }
        return b;
    }

    public static Double[] transformYVec(double[] y){
        int len = y.length-1;
        Double[] yvec = new Double[len];
        for (int i=0; i<len;i++)
            yvec[i] = y[i+1];
        return yvec;
    }
}



class ReturningValues{
    public final double[][] a;
    public final int info;
    public final int[] ipvt;
    public ReturningValues(double[][] a, int[] ipvt, int info){
        this.info = info;
        this.a = a;
        this.ipvt = ipvt;
    }
}
