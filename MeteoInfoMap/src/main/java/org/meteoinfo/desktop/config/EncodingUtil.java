/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author yaqiang
 */
public class EncodingUtil {

    private static final Pattern pep263EncodingPattern = Pattern.compile("#.*coding[:=]\\s*([-\\w.]+)");

    public static String matchEncoding(String inputStr) {
        Matcher matcher = pep263EncodingPattern.matcher(inputStr);
        boolean matchFound = matcher.find();

        if ((matchFound) && (matcher.groupCount() == 1)) {
            String groupStr = matcher.group(1);
            return groupStr;
        }
        return null;
    }

    public static String findEncoding(BufferedReader br)
            throws IOException {
        String encoding = null;
        for (int i = 0; i < 2; i++) {
            String strLine = br.readLine();
            if (strLine == null) {
                break;
            }
            String result = matchEncoding(strLine);
            if (result != null) {
                encoding = result;
                break;
            }
        }
        return encoding;
    }
    
    public static String findEncoding(String text) throws IOException{
        return findEncoding(new BufferedReader(new StringReader(text)));
    }
}
