/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mathparser;

/**
 *
 * @author yaqiang
 */
public abstract class ExpressionBase implements IExpression{

    @Override
    public abstract int getArgumentCount();

    /**
     * Validates the specified numbers for the expression
     *
     * @param numbers The numbers to validate
     */
    protected void validate(Object[] numbers) {
        if (numbers == null) {
            throw new java.lang.IllegalArgumentException("numbers");
        }
        if (numbers.length != this.getArgumentCount()) {
            throw new java.lang.IllegalArgumentException("Invalid length of Array");
        }
    }
}
