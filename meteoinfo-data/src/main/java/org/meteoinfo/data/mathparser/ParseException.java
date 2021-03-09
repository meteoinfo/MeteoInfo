/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mathparser;

/**
 *
 * @author yaqiang
 */
public class ParseException extends Exception{
    private String message;
    
    public ParseException(String errorMessage){
        message = errorMessage;
    }
    
    @Override
    public String getMessage(){
        return message;
    }
}
