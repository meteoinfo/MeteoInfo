/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mathparser;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;

/**
 *
 * @author yaqiang
 */
public class NumberExpression extends ExpressionBase {

    // <editor-fold desc="Variables">
    private Object _value;
    private ValueTypes _valueType = ValueTypes.Normal;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param value Value object
     */
    public NumberExpression(Object value) {
        _value = value;
        if (value.getClass() == GridData.class) {
            _valueType = ValueTypes.Grid;
        }
        if (value.getClass() == StationData.class) {
            _valueType = ValueTypes.Station;
        }
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    /**
     * Get value type
     *
     * @return Value type
     */
    public ValueTypes getValueType() {
        return this._valueType;
    }

    /**
     * Set value type
     *
     * @param value Value type
     */
    public void setValueType(ValueTypes value) {
        this._valueType = value;
    }

    /**
     * Get value
     *
     * @return Value
     */
    public Object getValue() {
        return this._value;
    }
    // </editor-fold>
    // <editor-fold desc="Methods">

    @Override
    public Object evaluate(Object[] numbers) {
        return _value;
    }

    @Override
    public int getArgumentCount() {
        return 0;
    }

    /**
     * Determines whether the specified char is a number
     *
     * @param c The char
     * @return If the char is a digit or a decimal separator
     */
    public static boolean isNumber(char c) {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return Character.isDigit(c) || symbols.getDecimalSeparator() == c;
    }

    /**
     * Determines whether the specified char is negative sign
     *
     * @param c The char to check
     * @return If the char is negative sign
     */
    public static boolean isNegativeSign(char c) {
        return c == '-';
    }

    /**
     * To string
     *
     * @return String value
     */
    @Override
    public String toString() {
        return String.valueOf(_value);
    }
    // </editor-fold>
}
