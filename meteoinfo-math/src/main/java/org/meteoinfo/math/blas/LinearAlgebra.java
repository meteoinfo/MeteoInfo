package org.meteoinfo.math.blas;

import org.meteoinfo.math.blas.openblas.OpenBLAS;

import java.util.logging.Logger;

public abstract class LinearAlgebra implements BLAS, LAPACK {
    public static LinearAlgebra engine = new OpenBLAS();

    /**
     * Set linear algebra engine
     * @param engineName Engine name
     */
    public static void setEngine(String engineName) {
        if (engineName.equalsIgnoreCase("mkl")) {
            LinearAlgebra la = MKL();
            if (la != null) {
                engine = la;
            }
        } else {
            engine = new OpenBLAS();
        }
    }

    static LinearAlgebra MKL() {
        Logger logger = Logger.getLogger("LAPACK.class");

        try {
            Class<?> clazz = Class.forName("org.meteoinfo.math.blas.mkl.MKL");
            logger.info("mkl module is available.");
            return (LinearAlgebra) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.info(String.format("Failed to create MKL instance: %s", e));
        }

        return null;
    }
}
