package org.meteoinfo.math.integrate.lsoda.exception;

public class NeqException extends RuntimeException{
    private String errorMsg;

    public NeqException(int neq){
        super();
        errorMsg = "lsoda: neq = "+neq+" is less than 1";
    }

    public NeqException(int neq, int istate){
        super();
        errorMsg = "lsoda: istate = "+istate+" and neq increased";
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
