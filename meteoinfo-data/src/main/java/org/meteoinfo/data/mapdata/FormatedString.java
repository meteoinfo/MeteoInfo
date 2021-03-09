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
package org.meteoinfo.data.mapdata;

/**
 *
 * @author yaqiang
 */
public class FormatedString {

    static boolean leadingzeros = false;

    public static String format(double d) {
        return format("" + d, 2);
    }

    public static String format(int i) {
        return format("" + i, 0);
    }

    public static String format(int i, int len) {
        return format("" + i, 0, len);
    }

    public static String format(String in) {
        return format(in, 2);
    }

    public static String format(String in, int dp, int len) {
        StringBuffer sb = new StringBuffer();
        String s = format(in, dp).trim();
        int diff = len - s.length();

        for (int i = 0; i < diff; i++) {
            sb.append(" ");
        }
        sb.append(s);
        return sb.toString();
    }

    public static String format(String in, int dp) {
        int e1 = in.indexOf('e');
        int e2 = in.indexOf('E');
        int e = Math.max(e1, e2);
        if (e > -1) {
            in = expand(in, e);
        }
        int i = in.lastIndexOf('.');
        if (i != -1) {
            String dec = "";
            String num = in.substring(0, i);
            if (dp > 0) {
                if ((i + dp + 1) < in.length()) {
                    dec = in.substring(i, i + dp + 1);
                } else {
                    dec = in.substring(i);

                }
            } else {
                dec = "";
            }
            while (dec.length() < dp + 1) {
                dec += "0";
            }
            if (dp == 0) {
                dec = "";
            }
            if (!leadingzeros) {
                char[] tmp = num.toCharArray();
                for (i = 0; i < tmp.length - 1; i++) {
                    if (tmp[i] != '0' && tmp[i] != ' ') {
                        break;
                    }
                    if (tmp[i] == '0') {
                        tmp[i] = ' ';
                    }
                    if (tmp[i + 1] == '.' && tmp[i] == ' ') {
                        tmp[i] = '0';
                    }
                }
                num = new String(tmp);
            }
            return (num + dec);
        } else {
            String dec = ".";
            while (dec.length() < dp + 1) {
                dec += "0";
            }
            if (dp == 0) {
                dec = "";
            }

            if (!leadingzeros) {
                char[] tmp = in.toCharArray();
                for (i = 0; i < tmp.length - 1; i++) {
                    if (tmp[i] != '0' && tmp[i] != ' ') {
                        break;
                    }
                    if (tmp[i] == '0') {
                        tmp[i] = ' ';
                    }
                    if (tmp[i + 1] == '.' && tmp[i] == ' ') {
                        tmp[i] = '0';
                    }
                }
                in = new String(tmp);
            }
            return (in + dec);
        }
    }

    private static String expand(String s, int e) {
        String last = s.substring(e + 1);
        String start = s.substring(0, e);

        int pow = Integer.parseInt(last);
        //System.out.println(start +" e "+last+" "+pow);
        int i = start.indexOf('.');
        if (i > 0) {
            int d = start.length() - i - 1;
            String a = start.substring(0, i);
            start = a + start.substring(i + 1);
            pow -= d;
        }
        for (i = 0; i < pow; i++) {
            start += "0";
        }
        for (i = pow; i < 0; i++) {
            start = "0" + start;
        }
        if (pow < 0) {
            int lp = start.length() + pow;
            //System.out.println(start+" x "+lp+" "+pow);
            start = start.substring(0, lp) + "." + start.substring(lp);
        }
        //System.out.println("->"+start);

        return start.trim();
    }
}
