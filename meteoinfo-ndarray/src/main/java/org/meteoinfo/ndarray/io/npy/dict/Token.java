package org.meteoinfo.ndarray.io.npy.dict;

import java.util.Optional;

class Token {

    final int position;
    final TokenType type;
    final String value;

    Token(int position, TokenType type, String value) {
        this.position = position;
        this.type = type;
        this.value = value;
    }

    static Token error(int position, String value) {
        return new Token(position, TokenType.ERROR, value);
    }

    static Token eof(int position) {
        return new Token(position, TokenType.EOF, "EOF");
    }

    static Token string(int position, StringBuilder value) {
        return new Token(position, TokenType.STRING, value.toString());
    }

    static Token identifier(int position, StringBuilder value) {
        return new Token(position, TokenType.IDENTIFIER, value.toString());
    }

    static Token integer(int position, StringBuilder value) {
        return new Token(position, TokenType.INTEGER, value.toString());
    }

    static Optional<Token> of(int position, char c) {
        TokenType type = TokenType.of(c);
        return type == null
                ? Optional.empty()
                : Optional.of(new Token(position, type, Character.toString(c)));
    }

    boolean isEof() {
        return type == TokenType.EOF;
    }

    @Override
    public String toString() {
        switch (type) {
            case EOF:
                return "EOF";
            case ERROR:
                return "ERROR: " + value;
            case STRING:
                return "'" + value + "'";
            default:
                return value;
        }
    }

}