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
public class OperatorExpression extends ExpressionBase {
    // <editor-fold desc="Variables">

    private static final String[] _operatorSymbols = new String[]{"+", "-", "*", "/", "^"};
    private MathOperators _mathOperator;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public OperatorExpression(String operator) {
        if (operator == null || operator.isEmpty()) {
            throw new java.lang.IllegalArgumentException("operator");
        }

        if (operator.equals("+")) {
            this._mathOperator = MathOperators.Add;
        } else if (operator.equals("-")) {
            this._mathOperator = MathOperators.Subtract;
        } else if (operator.equals("*")) {
            this._mathOperator = MathOperators.Multiple;
        } else if (operator.equals("/")) {
            this._mathOperator = MathOperators.Divide;
        } else if (operator.equals("^")) {
            this._mathOperator = MathOperators.Power;
        } else {
            throw new java.lang.IllegalArgumentException("Invalid operator" + operator);
        }
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    @Override
    public int getArgumentCount() {
        return 2;
    }

    @Override
    public Object evaluate(Object[] numbers) {
        switch (this._mathOperator) {
            case Add:
                return this.add(numbers);
            case Subtract:
                return this.subtract(numbers);
            case Multiple:
                return this.multiple(numbers);
            case Divide:
                return this.divide(numbers);
            case Power:
                return this.power(numbers);
        }

        return null;
    }

    /**
     * Add of the specified numbers
     *
     * @param numbers The numbers
     * @return The result of the operation
     */
    public Object add(Object[] numbers) {
        Object result = DataMath.add(numbers[0], numbers[1]);

        return result;
    }

    /**
     * Subtract of the specified numbers
     *
     * @param numbers The numbers
     * @return The result of the operation
     */
    public Object subtract(Object[] numbers) {
        Object result = DataMath.sub(numbers[0], numbers[1]);

        return result;
    }

    /**
     * Multiple of the specified numbers
     *
     * @param numbers The numbers
     * @return The result of the operation
     */
    public Object multiple(Object[] numbers) {
        Object result = DataMath.mul(numbers[0], numbers[1]);

        return result;
    }

    /**
     * Divide of the specified numbers
     *
     * @param numbers The numbers
     * @return The result of the operation
     */
    public Object divide(Object[] numbers) {
        Object result = DataMath.div(numbers[0], numbers[1]);

        return result;
    }

    /**
     * Power of the specified numbers
     *
     * @param numbers The numbers
     * @return The result of the operation
     */
    public Object power(Object[] numbers) {
        Object result = DataMath.pow(numbers[0], (Double) numbers[1]);

        return result;
    }

    /**
     * Determines whether the specified string is a math symbol
     *
     * @param s The string to check
     * @return If the string is a math symbol
     */
    public static boolean isSymbol(String s) {
        if (s == null || s.length() != 1) {
            return false;
        }

        return Arrays.asList(_operatorSymbols).contains(s);
    }

    /**
     * Determines whether the specified char is a math symbol
     *
     * @param c The char to check
     * @return If the char is a math symbol
     */
    public static boolean isSymbol(char c) {
        return isSymbol(String.valueOf(c));
    }

    @Override
    public String toString() {
        return this._mathOperator.toString();
    }
    // </editor-fold>
}
