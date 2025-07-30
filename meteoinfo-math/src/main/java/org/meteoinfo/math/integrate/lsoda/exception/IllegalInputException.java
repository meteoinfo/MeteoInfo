package org.meteoinfo.math.integrate.lsoda.exception;

public class IllegalInputException extends RuntimeException{
    private String errorMsg;

    public IllegalInputException(String param, Number value){
        super();
        errorMsg = "lsoda: illegal "+param+"="+value;
    }
    @Override
    public String getMessage() {
        return errorMsg;
    }
}
