/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console.jython;

import org.meteoinfo.console.ConsoleColors;
import org.meteoinfo.console.JConsole;
import org.meteoinfo.console.JavaCharStream;
import org.meteoinfo.console.ConsoleExecEvent;
import org.meteoinfo.console.IConsoleExecListener;
import org.python.util.InteractiveConsole;

import javax.swing.event.EventListenerList;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yaqiang
 */
public class PythonInteractiveInterpreter extends InteractiveConsole implements Runnable {

    private Reader in;
    private PrintStream out;
    private PrintStream err;
    public JConsole console;
    private final EventListenerList listeners = new EventListenerList();
    private ConsoleColors consoleColors;

    public PythonInteractiveInterpreter(JConsole console) {
        super();

        this.cflags.source_is_utf8 = true;
        this.console = console;
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
        this.consoleColors = new ConsoleColors();
    }
    
    /**
     * Get console
     * @return Console
     */
    public JConsole getConsole(){
        return this.console;
    }
    
    /**
     * Set console
     * @param console the JConsole
     */
    public void setConsole(JConsole console) {
        this.console = console;
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
    }
    
    /**
     * Set console colors
     * @param value Console colors
     */
    public void setConsoleColors(ConsoleColors value) {
        this.consoleColors = value;
    }
    
    /**
     * Update in, out and err
     */
    public void update() {
        console.updateOut();
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
    }
    
    /**
     * Get this.cflags.source_is_utf8
     * @return Boolean
     */
    public boolean isSourceUTF8(){
        return this.cflags.source_is_utf8;
    }
    
    /**
     * Set this.cflags.source_is_utf8
     * @param value 
     */
    public void setSourceUTF8(boolean value){
        this.cflags.source_is_utf8 = value;
    }

    @Override
    public void run() {
        boolean eof = false;
        JavaCharStream stream = new JavaCharStream(in, 1, 1);
        String ps1 = ">>> ";
        String ps2 = "... ";
        this.console.print(getDefaultBanner() + "\n", this.consoleColors.getPromptColor());
        this.console.print(ps1, this.consoleColors.getPromptColor());         
        String line;
        boolean retVal = false;
        while (!eof) {
            this.console.setStyle(this.consoleColors.getCommandColor());
            // try to sync up the console
            System.out.flush();
            System.err.flush();
            Thread.yield();  // this helps a little

            try {
                boolean eol = false;
                line = "";

                while (!eol) {
                    char aChar = stream.readChar();
                    eol = (aChar == '\n');
                    if (!eol) {
                        line = line + aChar;
                    }
                }
                if (!retVal)
                    line = line.trim();

                //hitting Enter at prompt returns a semicolon
                //get rid of it since it returns an error when executed
                if (line.equals(";")) {
                    line = "";
                }

                retVal = push(line);

                if (retVal) {
                    out.print(ps2);                    
                } else {
                    this.fireConsoleExecEvent();
                }
            } catch (Exception e) {
                out.print(e.toString() + '\n');
                this.resetbuffer();
                this.fireConsoleExecEvent();
            }
        }
    }
    
    @Override
    public void execfile(InputStream s){
        this.cflags.source_is_utf8 = false;
        super.execfile(s);
        this.cflags.source_is_utf8 = true;
        this.fireConsoleExecEvent();
    }
    
    @Override
    public void execfile(String fn) {
        this.cflags.source_is_utf8 = false;
        super.execfile(fn);
        this.cflags.source_is_utf8 = true;
        //this.fireConsoleExecEvent();
    }

    public void execfile_(String fn) {
        this.cflags.source_is_utf8 = false;
        super.execfile(fn);
        this.cflags.source_is_utf8 = true;
    }

    public void addConsoleExecListener(IConsoleExecListener listener) {
        this.listeners.add(IConsoleExecListener.class, listener);
    }

    public void removeConsoleExecListener(IConsoleExecListener listener) {
        this.listeners.remove(IConsoleExecListener.class, listener);
    }

    public void fireConsoleExecEvent() {
        fireConsoleExecEvent(new ConsoleExecEvent(this));

        this.console.awaitPipeDrain();

        this.console.print(">>> ", this.consoleColors.getPromptColor());
        this.console.setStyle(this.consoleColors.getCommandColor());
        this.console.setForeground(this.consoleColors.getCommandColor());
    }

    private void fireConsoleExecEvent(ConsoleExecEvent event) {
        Object[] ls = this.listeners.getListenerList();
        for (int i = 0; i < ls.length; i = i + 2) {
            if (ls[i] == IConsoleExecListener.class) {
                ((IConsoleExecListener) ls[i + 1]).consoleExecEvent(event);
            }
        }
    }
    
}
