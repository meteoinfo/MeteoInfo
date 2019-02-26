/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.forms;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.meteoinfo.desktop.config.JTextAreaPrintStream;

/**
 *
 * @author wyq
 */
public class StackWindow extends JFrame implements Thread.UncaughtExceptionHandler {

    private final JTextArea textArea;
    JTextAreaPrintStream printStream;

    public StackWindow(
            String title, final int width, final int height) {
        super(title);
        setSize(width, height);

        textArea = new JTextArea();
        JScrollPane pane = new JScrollPane(textArea);
        textArea.setEditable(false);
        getContentPane().add(pane);
        printStream = new JTextAreaPrintStream(System.out, textArea);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
    }    
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        addStackInfo(e);
    }

    public void addStackInfo(final Throwable t) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Bring window to foreground
                setVisible(true);
                toFront();
                // Convert stack dump to string
                //StringWriter sw = new StringWriter();                
                //PrintWriter out = new PrintWriter(sw);
                //t.printStackTrace(out);                                             
                // Add string to end of text area
                //textArea.append(sw.toString());
                t.printStackTrace(printStream);
                textArea.setCaretPosition(0);
            }
        });
    }
    
    private void formWindowClosed(java.awt.event.WindowEvent evt){
        textArea.setText("");
    }
}
