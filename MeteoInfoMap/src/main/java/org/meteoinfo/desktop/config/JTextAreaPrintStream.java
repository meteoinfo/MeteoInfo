/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.config;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;

/**
 *
 * @author yaqiang
 */
public class JTextAreaPrintStream extends PrintStream{
    private final JTextArea _jta;   
    
    /**
     *
     * @param out Output stream
     * @param textArea
     */
    public JTextAreaPrintStream(OutputStream out, JTextArea textArea){    
        super(out);
        _jta = textArea;
    }
    
    @Override
    public void write(byte[] buf){
        String message = new String(buf);
        _jta.append(message);
    }
    
    @Override
    public void write(byte[] buf, int off, int len){
        String message = new String(buf, off, len);
        _jta.append(message);
        //_jta.setCaretPosition(_jta.getText().length());
    }
    
    @Override
    public void println(String str){
        _jta.append(str + "\n");
    }
    
    @Override
    public void print(String str){
        _jta.append(str);
    }
}
