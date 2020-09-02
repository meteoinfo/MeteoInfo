/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console.editor;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextPane;

/**
 *
 * @author yaqiang
 */
public class JTextPanePrintStream extends PrintStream{
    private final JTextPane _jta;   
    
    /**
     *
     * @param out Output stream
     * @param textArea
     */
    public JTextPanePrintStream(OutputStream out, JTextPane textArea){    
        super(out);
        _jta = textArea;
    }
    
    @Override
    public void write(byte[] buf){
        String message = new String(buf);
        this.append(message);
    }
    
    @Override
    public void write(byte[] buf, int off, int len){
        String message = new String(buf, off, len);
        this.append(message);
        //_jta.setCaretPosition(_jta.getText().length());
    }
    
    @Override
    public void println(String str){
        this.append(str + "\n");
    }
    
    @Override
    public void print(String str){
        this.append(str);
    }
    
    private void append(String string) {
        int slen = _jta.getDocument().getLength();
        _jta.select(slen, slen);
        _jta.replaceSelection(string);
    }
}
