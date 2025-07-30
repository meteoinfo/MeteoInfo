package org.meteoinfo.math.integrate.lsoda.exception;

public class IstateException extends RuntimeException{
    private String errorMsg;

    public IstateException(int istate){
        super();
        errorMsg = "lsoda: illegal istate ="+istate;
    }

    public IstateException(int istate, int init){
        super();
        if (istate>1 && init==0)
            errorMsg = "lsoda: istate > 1 but lsoda not initialised";
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
