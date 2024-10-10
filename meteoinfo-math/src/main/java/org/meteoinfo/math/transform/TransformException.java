package org.meteoinfo.math.transform;

import java.text.MessageFormat;

/**
 * Exception class with constants for frequently used messages.
 * Class is package-private (for internal use only).
 */
public class TransformException extends IllegalArgumentException {

    /** Error message for "out of range" condition. */
    public static final String FIRST_ELEMENT_NOT_ZERO = "First element ({0}) must be 0";
    /** Error message for "not strictly positive" condition. */
    public static final String NOT_STRICTLY_POSITIVE = "Number {0} is not strictly positive";
    /** Error message for "too large" condition. */
    public static final String TOO_LARGE = "Number {0} is larger than {1}";
    /** Error message for "size mismatch" condition. */
    public static final String SIZE_MISMATCH = "Size mismatch: {0} != {1}";
    /** Error message for "pow(2, n) + 1". */
    public static final String NOT_POWER_OF_TWO_PLUS_ONE = "{0} is not equal to 1 + pow(2, n), for some n";
    /** Error message for "pow(2, n)". */
    public static final String NOT_POWER_OF_TWO = "{0} is not equal to pow(2, n), for some n";

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20210522L;

    /**
     * Create an exception where the message is constructed by applying
     * the {@code format()} method from {@code java.text.MessageFormat}.
     *
     * @param message Message format (with replaceable parameters).
     * @param formatArguments Actual arguments to be displayed in the message.
     */
    TransformException(String message, Object... formatArguments) {
        super(MessageFormat.format(message, formatArguments));
    }
}
