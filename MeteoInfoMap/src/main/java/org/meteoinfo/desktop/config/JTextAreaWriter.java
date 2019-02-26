/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.config;

import java.io.Writer;
import javax.swing.JTextArea;

/**
 *
 * @author yaqiang
 */
public class JTextAreaWriter extends Writer {

    private final JTextArea _jta;
    private final int _maxBuffSize;
    private final StringBuffer _stringBuffer = new StringBuffer();
    private final int _maxLength = 1000;

    public JTextAreaWriter(JTextArea ta) {
        this(ta, -1);
    }

    public JTextAreaWriter(JTextArea ta, int maxBuffSize) {
        _jta = ta;

        _jta.setWrapStyleWord(false);
        _maxBuffSize = maxBuffSize;
    }

    private void flushBufferToTextArea() {
        boolean onLastPosition = _jta.getCaretPosition() == _jta.getText().length();

        _jta.append(_stringBuffer.toString());

        if (onLastPosition) {
            _jta.setCaretPosition(_jta.getText().length());
        }

        _stringBuffer.setLength(0);
    }

    /**
     * returns the written string
     * @return 
     */
    @Override
    public synchronized String toString() {
        flushBufferToTextArea();

        return _jta.getText();
    }

    /**
     * overrides OutputStream.flush()
     */
    @Override
    public synchronized void flush() {
        flushBufferToTextArea();
    }

    /**
     * implements the stream: writes the byte to the string buffer
     */
    @Override
    public synchronized void write(int b) {
        if ((_stringBuffer.length() >= _maxBuffSize) || (b == '\n')) {
            flushBufferToTextArea();
        }

        if (_jta.getText().length() > _maxLength) {
            _jta.setText("");
        }
        
        _stringBuffer.append((char) b);
    }

    @Override
    public synchronized void write(char[] cbuf, int off, int len) {
//        for (int i = off; i < len; i++) {
//            write(cbuf[i]);
//        }

        String line = new String(cbuf, off, len);
        if (_jta.getLineCount() > _maxLength) {
            _jta.setText("");
        }
        _jta.append(line);
    }

    @Override
    public void close() {
        //nothing
    }
}