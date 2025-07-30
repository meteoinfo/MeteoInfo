package org.meteoinfo.math.integrate.lsoda.exception;

public class ExceedMaxStepsException extends RuntimeException{
    private String errorMsg;

    public ExceedMaxStepsException(int mxstep){
        super();
        errorMsg = "lsoda: "+mxstep+" steps taken before reaching tout";
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
