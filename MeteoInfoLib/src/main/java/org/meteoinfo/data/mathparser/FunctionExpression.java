/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mathparser;

import java.util.Arrays;
import org.meteoinfo.data.DataMath;

/**
 *
 * @author yaqiang
 */
public class FunctionExpression extends ExpressionBase {

    // <editor-fold desc="Variables">
    private String _function;
    private static final String[] mathFunctions = new String[]{
        "abs", "acos", "asin", "atan", "cos", "exp",
        "log", "log10", "sin", "sqrt", "tan"
    };
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public FunctionExpression(String function) {
        this(function, true);
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    @Override
    public int getArgumentCount() {
        return 1;
    }

    /**
     * Constructor
     * @param function The function
     * @param validate if validate the function name
     */
    protected FunctionExpression(String function, boolean validate) {
        function = function.toLowerCase();

        if (validate && !isFunction(function)) {
            throw new java.lang.IllegalArgumentException("Invalid function name");
        }

        _function = function;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Determines whether the specified function name is a function
     *
     * @param function The function name
     * @return Boolean
     */
    public static boolean isFunction(String function) {
        int idx = Arrays.binarySearch(mathFunctions, function);
        return idx >= 0;
    }

    @Override
    public Object evaluate(Object[] numbers) {
        super.validate(numbers);

        if (_function.equals("abs")) {
            return DataMath.abs(numbers[0]);
        } else if (_function.equals("acos")) {
            return DataMath.acos(numbers[0]);
        } else if (_function.equals("asin")) {
            return DataMath.asin(numbers[0]);
        } else if (_function.equals("atan")) {
            return DataMath.atan(numbers[0]);
        } else if (_function.equals("cos")) {
            return DataMath.cos(numbers[0]);
        } else if (_function.equals("exp")) {
            return DataMath.exp(numbers[0]);
        } else if (_function.equals("log")) {
            return DataMath.log(numbers[0]);
        } else if (_function.equals("log10")) {
            return DataMath.log10(numbers[0]);
        } else if (_function.equals("sin")) {
            return DataMath.sin(numbers[0]);
        } else if (_function.equals("sqrt")) {
            return DataMath.sqrt(numbers[0]);
        } else if (_function.equals("tan")) {
            return DataMath.tan(numbers[0]);
        } else {
            return null;
        }
    }
    
    @Override
    public String toString(){
        return _function;
    }
    
    /**
     * Get function names
     * @return Function names
     */
    public static String[] getFunctionNames(){
        return mathFunctions;
    }
    // </editor-fold>
}
