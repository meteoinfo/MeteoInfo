package org.meteoinfo.math.integrate.lsoda.exception;

public class TooMuchAccuracyException extends RuntimeException{
    private String errorMsg;

    public TooMuchAccuracyException(double tolsf){
        super();
        errorMsg = "lsoda: at start of problem, too much accuracy\n" +
                "         requested for precision of machine,\n" +
                "         suggested scaling factor = "+tolsf;
    }

    public TooMuchAccuracyException(double t, double tolsf){
        errorMsg = "lsoda: at t = " + t + ", too much accuracy requested\n" +
                "         for precision of machine, suggested\n" +
                "         scaling factor = " + tolsf;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}