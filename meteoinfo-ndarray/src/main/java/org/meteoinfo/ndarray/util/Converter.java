/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.ndarray.util;

import org.meteoinfo.ndarray.DataType;
import org.meteoinfo.ndarray.constants.CDM;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author wyq
 */
public class Converter {
    // convert byte array to char array, assuming UTF-8 encoding

    static public char[] convertByteToCharUTF(byte[] byteArray) {
        Charset c = CDM.utf8Charset;
        CharBuffer output = c.decode(ByteBuffer.wrap(byteArray));
        return output.array();
    }

    // convert char array to byte array, assuming UTF-8 encoding
    static public byte[] convertCharToByteUTF(char[] from) {
        Charset c = CDM.utf8Charset;
        ByteBuffer output = c.encode(CharBuffer.wrap(from));
        return output.array();
    }

    // convert byte array to char array
    static public char[] convertByteToChar(byte[] byteArray) {
        int size = byteArray.length;
        char[] cbuff = new char[size];
        for (int i = 0; i < size; i++) {
            cbuff[i] = (char) DataType.unsignedByteToShort(byteArray[i]); // NOTE: not Unicode !
        }
        return cbuff;
    }

    // convert char array to byte array
    static public byte[] convertCharToByte(char[] from) {
        int size = from.length;
        byte[] to = new byte[size];
        for (int i = 0; i < size; i++) {
            to[i] = (byte) from[i]; // LOOK wrong, convert back to unsigned byte ???
        }
        return to;
    }
}
