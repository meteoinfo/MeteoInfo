package org.meteoinfo.math.integrate.lsoda.exception;

public class InterpolationException extends RuntimeException{
    private String errorMsg;

    public InterpolationException(int itask, double tout){
        super();
        errorMsg = "lsoda: trouble from intdy, itask = "+itask+
                ", tout = "+tout;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
