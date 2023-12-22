package org.meteoinfo.ndarray.io.npy.dict;

enum TokenType {

    IDENTIFIER,

    STRING,

    INTEGER,

    COMMA,

    COLON,

    DICT_START,

    DICT_END,

    TUPLE_START,

    TUPLE_END,

    EOF,

    ERROR;

    /**
     * Returns the single-character token type for the given character. Returns
     * null if no such token type exists.
     */
    static TokenType of(char c) {
        switch (c) {
            case ',':
                return COMMA;
            case ':':
                return COLON;
            case '{':
                return DICT_START;
            case '}':
                return DICT_END;
            case '(':
                return TUPLE_START;
            case ')':
                return TUPLE_END;
            default:
                return null;
        }
    }
}