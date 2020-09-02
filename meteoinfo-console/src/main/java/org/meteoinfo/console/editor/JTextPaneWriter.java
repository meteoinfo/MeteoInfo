/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console.editor;

import java.io.Writer;
import javax.swing.JTextPane;

/**
 *
 * @author yaqiang
 */
public class JTextPaneWriter extends Writer {

    private final JTextPane _jta;
    private final int _maxBuffSize;
    private final StringBuffer _stringBuffer = new StringBuffer();
    private final int _maxLength = 200000;

    public JTextPaneWriter(JTextPane ta) {
        this(ta, -1);
    }

    public JTextPaneWriter(JTextPane ta, int maxBuffSize) {
        _jta = ta;

        //_jta.setWrapStyleWord(false);
        _maxBuffSize = maxBuffSize;
    }

    private void flushBufferToTextArea() {
        boolean onLastPosition = _jta.getCaretPosition() == _jta.getText().length();

        this.append(_stringBuffer.toString());

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
     * @param b
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
        if (_jta.getDocument().getLength() > _maxLength) {
            _jta.setText("");
        }
        this.append(line);
    }

    @Override
    public void close() {
        //nothing
    }
    
    private void append(String string) {
        int slen = _jta.getDocument().getLength();
        _jta.select(slen, slen);
        _jta.replaceSelection(string);
    }
}