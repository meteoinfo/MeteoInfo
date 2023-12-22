package org.meteoinfo.ndarray.io.npy.dict;

import java.util.ArrayList;
import java.util.List;

class Parser {

  private final List<Token> tokens;
  private int pos = -1;

  private Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  static PyValue parse(String text) {
    if (text == null)
      return PyError.of("empty input");
    List<Token> tokens = Lexer.lex(text);
    if (tokens.isEmpty())
      return PyError.of("empty input");

    // check if there is an error token
    for (Token token : tokens) {
      if (token.type == TokenType.ERROR)
        return PyError.of(
          "syntax error: " + token.value + "; at " + token.position);
    }

    // parse the value and make sure that it is followed by EOF
    Parser parser = new Parser(tokens);
    PyValue value = parser.parseNext();
    if (value.isError())
      return value;
    Token next = parser.next();
    if (!next.isEof())
      return PyError.of(
        "syntax error: expected EOF at "
        + next.position + " but found: " + next);

    return value;
  }

  private PyValue parseNext() {
    Token token = peek();
    switch (token.type) {
      case IDENTIFIER:
        moveNext();
        return new PyIdentifier(token.value);
      case INTEGER:
        moveNext();
        try {
          long value = Long.parseLong(token.value);
          return new PyInt(value);
        } catch (NumberFormatException e) {
          return PyError.of(
            "failed to parse integer: '"
            + token.value + "' at:" + token.position);
        }
      case STRING:
        moveNext();
        return new PyString(token.value);
      case TUPLE_START:
        return parseTuple();
      case DICT_START:
        return parseDict();
      default:
        return PyError.of(
          "syntax error: unexpected token '"
          + token.value + "' at " + token.position);
    }
  }

  private PyValue parseTuple() {
    Token start = next();
    if (start.type != TokenType.TUPLE_START)
      return PyError.of(
        "syntax error: expected tuple start at " + start.position);
    ArrayList<PyValue> values = new ArrayList<PyValue>();
    boolean head = true;

    while (true) {
      Token next = peek();
      if (next.isEof())
        return PyError.of("syntax error: unexpected end of tuple");
      if (next.type == TokenType.TUPLE_END) {
        moveNext();
        break;
      }

      if (!head) {
        if (next.type != TokenType.COMMA)
          return PyError.of("syntax error: unexpected token: " + next);
        head = true;
        moveNext();
        continue;
      }

      PyValue value = parseNext();
      if (value.isError())
        return value;
      values.add(value);
      head = false;
    }
    return new PyTuple(values);
  }

  private PyValue parseDict() {
    Token start = next();
    if (start.type != TokenType.DICT_START)
      return PyError.of(
        "syntax error: expected dict start at " + start.position);
    PyDict dict = new PyDict();
    boolean head = true;

    while (true) {
      Token next = next();
      if (next.type == TokenType.DICT_END)
        break;
      if (!head) {
        if (next.type != TokenType.COMMA)
          return PyError.of("syntax error: unexpected token: " + next);
        head = true;
        continue;
      }
      if (next.type != TokenType.STRING)
        return PyError.of(
          "syntax error: only string keys are allowed but found: " + next);
      String key = next.value;
      Token colon = next();
      if (colon.type != TokenType.COLON)
        return PyError.of(
          "syntax error: expected colon but found: " + next);
      PyValue value = parseNext();
      if (value.isError())
        return value;
      dict.put(key, value);
      head = false;
    }
    return dict;
  }

  private Token peek() {
    int nextPos = pos + 1;
    return nextPos < tokens.size()
      ? tokens.get(nextPos)
      : Token.eof(-1);
  }

  private Token next() {
    Token peeked = peek();
    if (peeked.type != TokenType.EOF) {
      pos++;
    }
    return peeked;
  }

  private void moveNext() {
    pos++;
  }

}
