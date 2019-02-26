 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.table;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author yaqiang
 */
public class SQLExpression {
    // <editor-fold desc="Variables">

    private final String expression;
    private final int exp_len;
    private final char[] exp_array;
    private final ArrayList<String> token_list;
    private boolean is_flushed;
    private char c;
    private StringBuffer sb;
    private int cur_token_index;
    private final int token_count;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    public SQLExpression(String SQLExpression) {
        this.expression = SQLExpression;
        this.exp_len = SQLExpression.length();
        this.exp_array = SQLExpression.toCharArray();
        this.token_list = new ArrayList<>();
        this.is_flushed = true;
        this.cur_token_index = -1;
        this.sb = new StringBuffer();
        this.initTokenList();
        this.token_count = this.token_list.size();

    }

    private void flush() {
        if (this.is_flushed) {
            return;
        }
        
        String token = this.sb.toString();
        if (token.startsWith("\""))
            token = token.substring(1);
        if (token.endsWith("\""))
            token = token.substring(0, token.length() - 1);
        this.token_list.add(token);
        this.sb = new StringBuffer();
        this.is_flushed = true;
    }

    private void collect() {
        this.sb.append(c);
        this.is_flushed = false;
    }

    private boolean nextToken() {

        if (this.cur_token_index < this.token_count - 1) {
            this.cur_token_index++;
            return true;
        }

        return false;
    }

    private String currentToken() {
        return this.token_list.get(this.cur_token_index);
    }

    private void initTokenList() {
        for (int i = 0; i < this.exp_len; i++) {
            c = exp_array[i];
            if (Character.isWhitespace(c)) {
                if (!(this.sb.length() > 0 && this.sb.substring(0, 1).equals("\""))) {
                    this.flush();
                    continue;
                }
            }

            switch (c) {
                case '<':
                    this.flush();
                    if (i + 1 < this.exp_len && exp_array[i + 1] == '=') {
                        this.token_list.add("<=");
                        i++;
                    } else if (i + 1 < this.exp_len && exp_array[i + 1] == '>') {
                        this.token_list.add("<>");
                        i++;
                    } else {
                        this.token_list.add("<");
                    }
                    break;
                case '>':
                    this.flush();
                    if (i + 1 < this.exp_len && exp_array[i + 1] == '=') {
                        this.token_list.add(">=");
                        i++;
                    } else {
                        this.token_list.add(">");
                    }
                    break;
                case '(':
                    this.flush();
                    this.token_list.add("(");
                    break;
                case ')':
                    this.flush();
                    this.token_list.add(")");
                    break;
                case '=':
                    if (i + 1 < this.exp_len && exp_array[i + 1] == '=') {
                        this.flush();
                        //this.token_list.add("==");
                        i++;
                    } else {
                        this.collect();
                    }
                    break;
                case '!':
                    if (i + 1 < this.exp_len && exp_array[i + 1] == '=') {
                        this.flush();
                        this.token_list.add("!=");
                        i++;
                    } else {
                        this.collect();
                    }
                    break;
                case '"':
                    if (this.sb.length() > 0 && this.sb.substring(0, 1).equals("\"")){
                        this.flush();
                    } else {
                        this.collect();
                    }
                    break;
                default:
                    this.collect();
                    break;
            }
        }

        this.flush();
    }

    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    public boolean eval(Map dr) {
        this.nextToken();
        boolean result = this.doAndOr(dr);
        this.cur_token_index = -1;
        return result;
    }

    private boolean doAndOr(Map dr) {
        boolean result = this.doNot(dr);
        String op;

        boolean result_right;

        while ((op = this.currentToken()).equalsIgnoreCase("and") || op.equalsIgnoreCase("or")) {
            this.nextToken();

            if (op.equalsIgnoreCase("and")) {
                result_right = this.doNot(dr);
                result = result && result_right;
            } else {
                result_right = this.doNot(dr);
                result = result || result_right;
            }
        }

        return result;
    }

    private boolean doNot(Map dr) {
        String op;

        if ((op = this.currentToken()).equalsIgnoreCase("not")) {
            this.nextToken();
        }

        boolean result = this.doBrackets(dr);

        if (op.equalsIgnoreCase("not")) {
            return !result;
        }

        return result;
    }

    private boolean doBrackets(Map dr) {
        boolean result;
        if (this.currentToken().equals("(")) {
            this.nextToken();
            result = this.doAndOr(dr);
            this.nextToken();
        } else {
            result = this.doCompare(dr);
        }

        return result;
    }

    private boolean doCompare(Map dr) {
        Object field = dr.get(this.currentToken().toLowerCase());
        //Object field = this.currentToken();
        this.nextToken();
        String opt = this.currentToken();
        this.nextToken();
        String value = this.currentToken();
        this.nextToken();

        switch (opt) {
            case "like":
                return isLike(field, value);
            case ">":
                return isGreat(field, value);
            case "<":
                return isLess(field, value);
            case "=":
                return isEquals(field, value);
            case ">=":
                return isGreatEquals(field, value);
            case "<=":
                return isLessEquals(field, value);
            case "<>":
                return isNotEquals(field, value);
            default:
                break;
        }

        return false;

    }

//<editor-fold  defaultstate="collapsed" desc="tool method">
    private static boolean isLike(Object field, String value) {
        int len = value.length();
        if (value.startsWith("'%") && value.endsWith("%'")) {
            return Convert.toString(field).contains(value.substring(2, len - 2));
        } else if (value.startsWith("'%")) {
            return Convert.toString(field).endsWith(value.substring(2, len - 1));
        } else if (value.endsWith("%'")) {
            return Convert.toString(field).startsWith(value.substring(1, len - 2));
        } else {
            return Convert.toString(field).equals(value.substring(1, len - 1));
        }
    }

    private static boolean isLess(Object field, String value) {
        if (field instanceof Number) {
            return Convert.toFloat(field) < Convert.toFloat(value);
        }
        if (field instanceof Date) {
            return ((Date)field).before(Convert.toDate(value));
        }
        return Convert.toString(field).compareTo(value.substring(1, value.length() - 1)) < 0;
    }

    private static boolean isGreat(Object field, String value) {
        if (field instanceof Number) {
            return Convert.toFloat(field) > Convert.toFloat(value);
        } 
        if (field instanceof Date) {
            return ((Date) field).after(Convert.toDate(value));
        }
        return Convert.toString(field).compareTo(value.substring(1, value.length() - 1)) > 0;
    }

    private static boolean isEquals(Object field, String value) {
        if (value.equals("null")) {
            return field == null;
        }

        if (field instanceof Number) {
            return Convert.toFloat(field) == Convert.toFloat(value);
        }
        if (field instanceof Boolean) {
            return Convert.toBool(field) == Convert.toBool(value);
        }
        if (field instanceof Date) {
            return ((Date)field).equals(Convert.toDate(value));
        }

        //return Convert.toString(field).equals(value.substring(1, value.length() - 1));
        return Convert.toString(field).equals(value);
    }

    private static boolean isNotEquals(Object field, String value) {
        if (value.equals("null")) {
            return field != null;
        }

        if (field instanceof Number) {
            return Convert.toFloat(field) != Convert.toFloat(value);
        }
        if (field instanceof Boolean) {
            return Convert.toBool(field) != Convert.toBool(value);
        }
        if (field instanceof Date) {
            return !((Date)field).equals(Convert.toDate(value));
        }

        //return !Convert.toString(field).equals(value.substring(1, value.length() - 1));
        return !Convert.toString(field).equals(value);
    }

    private static boolean isLessEquals(Object field, String value) {
        if (field instanceof Number) {
            return Convert.toFloat(field) <= Convert.toFloat(value);
        }
        if (field instanceof Date) {
            return ((Date)field).before(Convert.toDate(value)) || ((Date)field).equals(Convert.toDate(value));
        }
        return Convert.toString(field).compareTo(value.substring(1, value.length() - 1)) <= 0;
    }

    private static boolean isGreatEquals(Object field, String value) {
        if (field instanceof Number) {
            return Convert.toFloat(field) >= Convert.toFloat(value);
        }
        if (field instanceof Date) {
            return ((Date)field).after(Convert.toDate(value)) || ((Date)field).equals(Convert.toDate(value));
        }
        return Convert.toString(field).compareTo(value.substring(1, value.length() - 1)) >= 0;
    }
    // </editor-fold>
}

class Convert {

    public static String toString(Object o) {
        return toString(o, "");
    }

    public static String toString(Object o, String defValue) {
        if (o == null) {
            return defValue;
        }
        return o.toString();
    }

    public static int toInt(Object o) {
        return toInt(o, 0);
    }

    public static int toInt(Object o, int defValue) {
        if (o == null) {
            return defValue;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }

        try {
            return (int) Float.parseFloat(o.toString());
        } catch (Exception e) {
            return defValue;
        }
    }

    public static long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Long) {
            return (Long) o;
        }

        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    public static float toFloat(Object o) {
        if (o == null) {
            return 0F;
        }
        if (o instanceof Float) {
            return (Float) o;
        }

        try {
            return Float.parseFloat(o.toString());
        } catch (Exception e) {
            return 0F;
        }
    }

    public static boolean toBool(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }

        try {
            return Boolean.parseBoolean(o.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public static Date toDate(Object o) {
        return toDate(o, new Date(System.currentTimeMillis()));
    }

    public static Date toDate(Object o, Date defValue) {
        if (o == null) {
            return defValue;
        }

        if (o instanceof java.util.Date) {
            return (Date) o;
        }

        try {
            if (o.toString().contains(":"))
                return Timestamp.valueOf(o.toString());
            else
                return java.sql.Date.valueOf(o.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}