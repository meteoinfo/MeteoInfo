package org.meteoinfo.ndarray.io.npy.dict;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// https://talks.golang.org/2011/lex.slide#27
class Lexer {

    private final char EOF = Character.MIN_VALUE;

    private final String input;
    private final List<Token> tokens = new ArrayList<>();

    private int pos = -1;

    private Lexer(String input) {
        this.input = input;
    }

    static List<Token> lex(String input) {
        Lexer lexer = new Lexer(input);
        lexer.loop();
        return lexer.tokens;
    }

    void loop() {
        for (StateFunction stateFn = this::lexText; stateFn != null; ) {
            stateFn = stateFn.execute();
        }
    }

    private StateFunction lexText() {
        char c = peek();

        // skip white spaces
        while (Character.isWhitespace(c)) {
            pos++;
            c = peek();
        }

        // eof
        if (c == EOF) {
            tokens.add(Token.eof(pos));
            return null;
        }

        // quoted string
        if (c == '"' || c == '\'')
            return this::lexString;

        // number
        if (Character.isDigit(c))
            return this::lexNumber;

        // identifiers
        if (Character.isJavaIdentifierStart(c))
            return this::lexIdentifier;

        // single character tokens
        Optional<Token> charToken = Token.of(pos, c);
        if (charToken.isPresent()) {
            pos++;
            tokens.add(charToken.get());
            return this::lexText;
        }

        // error
        tokens.add(Token.error(pos, "unexpected character: '" + c + "'"));
        return null;
    }

    private StateFunction lexString() {
        char quote = next();
        StringBuilder buffer = new StringBuilder();
        int start = pos;
        while (true) {
            char c = next();
            if (c == EOF) {
                tokens.add(Token.eof(pos));
                return null;
            }
            if (c == quote)
                break;
            buffer.append(c);
        }
        tokens.add(Token.string(start, buffer));
        return this::lexText;
    }

    private StateFunction lexIdentifier() {
        StringBuilder buffer = new StringBuilder();
        int start = pos + 1;
        while (true) {
            char c = peek();
            if (c == EOF || !Character.isJavaIdentifierPart(c))
                break;
            pos++;
            buffer.append(c);
        }
        tokens.add(Token.identifier(start, buffer));
        return this::lexText;
    }

    private StateFunction lexNumber() {
        StringBuilder buffer = new StringBuilder();
        int start = pos + 1;
        while (true) {
            char c = peek();
            if (!Character.isDigit(c))
                break;
            pos++;
            buffer.append(c);
        }
        tokens.add(Token.integer(start, buffer));
        return this::lexText;
    }

    private char next() {
        int nextPos = pos + 1;
        if (nextPos >= input.length())
            return EOF;
        pos = nextPos;
        return input.charAt(nextPos);
    }

    private char peek() {
        int nextPos = pos + 1;
        return nextPos >= input.length()
                ? EOF
                : input.charAt(nextPos);
    }

    @FunctionalInterface
    interface StateFunction {
        StateFunction execute();
    }

}
