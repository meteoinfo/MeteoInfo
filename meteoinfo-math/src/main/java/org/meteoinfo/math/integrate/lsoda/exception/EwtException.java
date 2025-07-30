package org.meteoinfo.math.integrate.lsoda.exception;

public class EwtException extends RuntimeException{
    private String errorMsg;

    public EwtException(Number i, Number value){
        super();
        errorMsg ="lsoda: ewt["+i+"] = "+value+" <= 0.0";
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
