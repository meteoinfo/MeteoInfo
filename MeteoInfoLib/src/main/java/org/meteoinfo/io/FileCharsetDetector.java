/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.io;

//Use jchardet get file encoding -javacode   
//Error with Chinese characters using ANSI encoding     
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

/**
 * Using JCharDet get file charset
 *
 * @author icer PS: JCharDet：
 * http://jchardet.sourceforge.net/
 */
public class FileCharsetDetector {

    private boolean found = false;

    private String encoding = null;

    /**
     * Check file encoding
     *
     * @param file File object
     * @return Encoding string
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guestFileEncoding(File file) throws FileNotFoundException,
            IOException {
        return guestFileEncoding(file, new nsDetector());
    }

    /**
     * Check file encoding
     *
     * @param file File object
     * @param languageHint Language comment code, eg: 1 : Japanese; 2 : Chinese; 3 : Simplified
     * Chinese; 4 : Traditional Chinese; 5 : Korean; 6 : Dont know (default)
     * @return File encoding, eg: UTF-8,GBK,GB2312
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guestFileEncoding(File file, int languageHint)
            throws FileNotFoundException, IOException {
        return guestFileEncoding(file, new nsDetector(languageHint));
    }

    /**
     * Check file encoding
     *
     * @param path File path
     * @return File encoding
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guestFileEncoding(String path) throws FileNotFoundException,
            IOException {
        return guestFileEncoding(new File(path));
    }

    /**
     * Get file encoding
     *
     * @param path File path
     * @param languageHint Language comment code, eg: 1 : Japanese; 2 : Chinese; 3 : Simplified
     * Chinese; 4 : Traditional Chinese; 5 : Korean; 6 : Dont know (default)
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String guestFileEncoding(String path, int languageHint)
            throws FileNotFoundException, IOException {
        return guestFileEncoding(new File(path), languageHint);
    }

    /**
     * Get file encoding
     *
     * @param file File object
     * @param det Detector
     * @return File encoding
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String guestFileEncoding(File file, nsDetector det)
            throws FileNotFoundException, IOException {
        // Set an observer...   
        // The Notify() will be called when a matching charset is found.   
        det.Init(new nsICharsetDetectionObserver() {
            @Override
            public void Notify(String charset) {
                found = true;
                encoding = charset;
            }
        });

        BufferedInputStream imp = new BufferedInputStream(new FileInputStream(
                file));

        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        while ((len = imp.read(buf, 0, buf.length)) != -1) {
            // Check if the stream is only ascii.   
            if (isAscii) {
                isAscii = det.isAscii(buf, len);
            }

            // DoIt if non-ascii and not done yet.   
            if (!isAscii && !done) {
                done = det.DoIt(buf, len, false);
            }
        }
        det.DataEnd();

        if (isAscii) {
            encoding = "ASCII";
            found = true;
        }

        if (!found) {
            String prob[] = det.getProbableCharsets();
            if (prob.length > 0) {
                // use file guess encoding when no encoding found   
                encoding = prob[0];
            } else {
                return null;
            }
        }
        return encoding;
    }
}
