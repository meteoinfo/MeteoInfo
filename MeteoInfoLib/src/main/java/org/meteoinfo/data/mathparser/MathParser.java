/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.data.mathparser;

import java.beans.Expression;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.meteoinfo.global.MIMath;

/**
 *
 * @author yaqiang
 */
public class MathParser {
    // <editor-fold desc="Variables">

    private boolean _isGridData;
    private StringBuilder _buffer = new StringBuilder();
    private Stack<String> _symbolStack = new Stack<String>();
    private Queue<IExpression> _expressionQueue = new LinkedList<IExpression>();
    private Map<String, IExpression> _expressionCache = new HashMap<String, IExpression>();
    private Stack<Object> _calculationStack = new Stack<Object>();
    private Stack<Object> _parameters = new Stack<Object>();
    private List<String> _variables = new ArrayList<String>();
    private StringReader _expressionReader;
    private MeteoDataInfo _meteoDataInfo = null;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     */
    public MathParser() {
    }

    /**
     * Constructor
     *
     * @param aDataInfo MeteoDataInfo
     */
    public MathParser(MeteoDataInfo aDataInfo) {
        _meteoDataInfo = aDataInfo;
        _isGridData = aDataInfo.isGridData();
        _variables = aDataInfo.getDataInfo().getVariableNames();
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     * Evaluates the specified expression
     *
     * @param expression The expression to evaluate
     * @return The evaluated result
     */
    public Object evaluate(String expression) throws ParseException, IOException {
        if (expression == null || expression.isEmpty()) {
            throw new java.lang.IllegalArgumentException("expression");
        }

        _expressionReader = new StringReader(expression);
        _symbolStack.clear();
        _expressionQueue.clear();

        parseExpressionToQueue();

        Object result = calculateFromQueue();

        //_variables[AnswerVariable] = result;
        return result;
    }

    private void parseExpressionToQueue() throws ParseException, IOException {
        int ic;
        char c;
        while ((ic = _expressionReader.read()) != -1) {
            c = (char) ic;
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (tryNumber(c)) {
                continue;
            }
            if (tryString(c)) {
                continue;
            }
            if (tryStartGroup(c)) {
                continue;
            }
            if (tryOperator(c)) {
                continue;
            }
            if (tryEndGroup(c)) {
                continue;
            }
            //if (TryConvert(c))
            //    continue;
            throw new ParseException("Invalid character encountered" + c);
        }

        processSymbolStack();
    }

    private boolean tryNumber(char c) throws IOException, ParseException {
        boolean isNumber = NumberExpression.isNumber(c);
        boolean isNegative = false;
        if (NumberExpression.isNegativeSign(c)) {
            if (_expressionQueue.size() == 0) {
                isNegative = true;
            } else if (_expressionQueue.size() > 0 && _symbolStack.size() > 0) {
                if (((String) _symbolStack.peek()).equals("(")) {
                    isNegative = true;
                }
            }
        }

        if (!isNumber && !isNegative) {
            return false;
        }

        _buffer.setLength(0);
        _buffer.append(c);

        _expressionReader.mark(1);
        char p = (char) _expressionReader.read();
        while (NumberExpression.isNumber(p)) {
            _buffer.append(p);
            _expressionReader.mark(1);
            p = (char) _expressionReader.read();
        }
        _expressionReader.reset();

        double value;
        try {
            value = Double.parseDouble(_buffer.toString());
        } catch (Exception e) {
            throw new ParseException("Invalid number format: " + _buffer);
        }

        NumberExpression expression = new NumberExpression(value);
        _expressionQueue.offer(expression);

        return true;
    }

    private boolean tryString(char c) throws IOException, ParseException {
        if (!Character.isLetter(c)) {
            return false;
        }

        _buffer.setLength(0);
        _buffer.append(c);

        _expressionReader.mark(1);
        char p = (char) _expressionReader.read();
        while (Character.isLetterOrDigit(p) || p == '_' || p == '@' || p == '.') {
            _buffer.append(p);
            _expressionReader.mark(1);
            p = (char) _expressionReader.read();
        }
        _expressionReader.reset();

        if (_variables.contains(_buffer.toString())) {
            Object value = getVariableValue(_buffer.toString());
            NumberExpression expression = new NumberExpression(value);
            _expressionQueue.offer(expression);

            return true;
        }

        if (FunctionExpression.isFunction(_buffer.toString())) {
            _symbolStack.push(_buffer.toString());
            return true;
        }

        throw new ParseException("Invalid variable: " + _buffer);
    }

    private boolean tryStartGroup(char c) {
        if (c != '(') {
            return false;
        }

        _symbolStack.push(String.valueOf(c));
        return true;
    }

    private boolean tryOperator(char c) throws ParseException {
        if (!OperatorExpression.isSymbol(c)) {
            return false;
        }

        boolean repeat;
        String s = String.valueOf(c);

        do {
            String p = _symbolStack.size() == 0 ? "" : _symbolStack.peek();
            repeat = false;
            if (_symbolStack.size() == 0) {
                _symbolStack.push(s);
            } else if (p.equals("(")) {
                _symbolStack.push(s);
            } else if (precedence(s) > precedence(p)) {
                _symbolStack.push(s);
            } else {
                IExpression e = getExpressionFromSymbol(_symbolStack.pop());
                _expressionQueue.offer(e);
                repeat = true;
            }
        } while (repeat);

        return true;
    }

    private boolean tryEndGroup(char c) throws ParseException {
        if (c != ')') {
            return false;
        }

        boolean ok = false;

        while (_symbolStack.size() > 0) {
            String p = _symbolStack.pop();
            if (p.equals("(")) {
                ok = true;
                break;
            }

            IExpression e = getExpressionFromSymbol(p);
            _expressionQueue.offer(e);
        }

        if (!ok) {
            throw new ParseException("Unbalance parenthese");
        }

        return true;
    }

    private void processSymbolStack() throws ParseException {
        while (_symbolStack.size() > 0) {
            String p = _symbolStack.pop();
            if (p.length() == 1 && p.equals("(")) {
                throw new ParseException("Unbalance parenthese");
            }

            IExpression e = getExpressionFromSymbol(p);
            _expressionQueue.offer(e);
        }
    }

    private static int precedence(String c) {
        String s = c.substring(0, 1);
        if (c.length() == 1 && s.equals("*") || s.equals("/") || s.equals("%")) {
            return 2;
        }

        return 1;
    }

    private IExpression getExpressionFromSymbol(String p) throws ParseException {
        IExpression e;

        if (_expressionCache.containsKey(p)) {
            e = _expressionCache.get(p);
        } else if (OperatorExpression.isSymbol(p)) {
            e = new OperatorExpression(p);
            _expressionCache.put(p, e);
        } else if (FunctionExpression.isFunction(p)) {
            e = new FunctionExpression(p, false);
            _expressionCache.put(p, e);
        } //else if (ConvertExpression.IsConvertExpression(p))
        //{
        //    e = new ConvertExpression(p);
        //    _expressionCache.Add(p, e);
        //}
        else {
            throw new ParseException("Invalid symbol on stack" + p);
        }

        return e;
    }

    private Object calculateFromQueue() throws ParseException {
        Object result;
        _calculationStack.clear();

        for (IExpression expression : _expressionQueue) {
            if (_calculationStack.size() < expression.getArgumentCount()) {
                throw new ParseException("Not enough numbers" + expression);
            }

            _parameters.clear();
            for (int i = 0; i < expression.getArgumentCount(); i++) {
                _parameters.push(_calculationStack.pop());
            }
            
            Object[] parameters = _parameters.toArray();
            MIMath.arrayReverse(parameters);
            _calculationStack.push(expression.evaluate(parameters));
        }

        result = _calculationStack.pop();
        return result;
    }

    private Object getVariableValue(String varName) {
        if (_meteoDataInfo == null) {
            return 100;
        } else {
            if (_isGridData) {
                return _meteoDataInfo.getGridData(varName);
            } else {
                return _meteoDataInfo.getStationData(varName);
            }
        }
    }
    // </editor-fold>
}
