package org.meteoinfo.math.integrate.lsoda.exception;

public class RepeatedInputException extends RuntimeException{
    private String errorMsg;

    public RepeatedInputException(){
        super();
        errorMsg ="lsoda: repeated calls with istate = 1 and tout = t\n"+
                "       run aborted.. apparent infinite loop";
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
