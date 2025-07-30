package org.meteoinfo.math.integrate.lsoda.exception;

public class TestsFailException extends RuntimeException{
    private String errorMsg;

    public TestsFailException(String type, double tn, double h){
        super();
        errorMsg = "lsoda: at t =" + tn + " and step size h = " + h+"\n";
        switch (type){
            case "error_test":
                errorMsg += "         error test failed repeatedly or with abs(h)=hmin";
                break;
            case "convergence_test":
                errorMsg += "         corrector convergence failed repeatedly or with abs(h)=hmin";
                break;
        }
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }
}
