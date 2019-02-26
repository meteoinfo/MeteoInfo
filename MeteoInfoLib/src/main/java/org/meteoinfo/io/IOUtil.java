/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wyq
 */
public class IOUtil {

    /**
     * Get file chart
     *
     * @param filePath File path
     * @return File chart
     * @throws IOException
     */
    public static String getFileChart(String filePath) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(filePath));
        int p = (bin.read() << 8) + bin.read();
        bin.close();
        String code = null;

        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }

        return code;

    }

    /**
     * Guess file encoding
     *
     * @param filePath The file path
     * @return Guessed encoding
     * @throws IOException
     */
    public static String guessFileEncoding(String filePath) throws IOException {
        FileCharsetDetector fcd = new FileCharsetDetector();
        String encoding = fcd.guestFileEncoding(filePath);
        return encoding;
    }

    /**
     * Detect file encoding
     * @param filePath The file path
     * @return Encoding
     */
    public static String encodingDetect(String filePath) {
        return encodingDetect(new File(filePath));
    }
    
    /**
     * Detect file encoding
     * @param file The file
     * @return Encoding
     */
    public static String encodingDetect(File file) {
        EncodingDetect detect = new EncodingDetect();
        int encodingNumber = detect.detectEncoding(file);
        // may return GBK > GB-2312 > GB18030
        String name = EncodingDetect.javaname[encodingNumber];
        return name;
    }
    
    /**
     * Encoding detect to a shape file
     * @param shpfilepath The shape file path
     * @return Encoding
     */
    public static String encodingDetectShp(String shpfilepath){
        String cpgfilepath = shpfilepath.replaceFirst(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".cpg");
        File cpgFile = new File(cpgfilepath);
        if (cpgFile.exists()){
            BufferedReader sr = null;
            try {
                sr = new BufferedReader(new FileReader(cpgFile));
                String ec = sr.readLine().trim();
                sr.close();
                return ec;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (sr != null)
                        sr.close();
                } catch (IOException ex) {
                    Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        String dbffilepath = shpfilepath.replace(shpfilepath.substring(shpfilepath.lastIndexOf(".")), ".dbf");
        File dbfFile = new File(dbffilepath);
        if (!dbfFile.exists()) {
            dbffilepath = dbffilepath.replace(".dbf", ".DBF");
            dbfFile = new File(dbffilepath);
        }
        if (!dbfFile.exists()) {
            System.out.println(".dbf file not exists!");
            return null;
        } else {
            return encodingDetect(dbfFile);
        }
    }

}
