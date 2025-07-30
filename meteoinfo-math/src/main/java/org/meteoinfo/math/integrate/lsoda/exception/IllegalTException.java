package org.meteoinfo.math.integrate.lsoda.exception;

public class IllegalTException extends RuntimeException{
    private String errorMsg;

    public IllegalTException(String type, double t, double tout, Number tt){
        switch (type){
            case "tout_behind_t":
                errorMsg = "lsoda: tout ="+tout+" behind t = "+t +
                        "\n   integration direction is given by "+tt;
                break;
            case "tcrit_behind_tout":
                errorMsg = "lsoda: itask = 4 or 5 and tcrit behind tout";
                break;
            case "tcrit_behind_tcur":
                errorMsg = "lsoda: itask = 4 or 5 and tcrit behind tcur";
                break;
            case "tout_close_to_t":
                errorMsg = "lsoda: tout too close to t to start integration";
                break;
            case "tout_behind_tcur_hu":
                errorMsg = "lsoda: itask = "+tt+" and tout behind tcur - hu";
                break;
        }
    }
    @Override
    public String getMessage(){
        return errorMsg;
    }
}
