/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.config;

import org.meteoinfo.console.JavaCharStream;
import org.meteoinfo.console.JConsole;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import org.python.util.InteractiveConsole;

/**
 *
 * @author yaqiang
 */
public class PythonInteractiveInterpreter extends InteractiveConsole implements Runnable {

    transient Reader in;
    transient PrintStream out;
    transient PrintStream err;
    JConsole console;

    public PythonInteractiveInterpreter(JConsole console) {
        super();
        
        this.cflags.source_is_utf8 = true;
        this.console = console;        
        in = console.getIn();
        out = console.getOut();
        err = console.getErr();
        setOut(out);
        setErr(err);
    }

    @Override
    public void run() {
        boolean eof = false;
        JavaCharStream stream = new JavaCharStream(in, 1, 1);

//        exec("_ps1 = sys.ps1");
//        PyObject ps1Obj = get("_ps1");
//        String ps1 = ps1Obj.toString();
        String ps1 = ">>> ";

//        exec("_ps2 = sys.ps2");
//        PyObject ps2Obj = get("_ps2");
//        String ps2 = ps2Obj.toString();
        String ps2 = "... ";
        out.print(getDefaultBanner() + "\n");

        out.print(ps1);
        String line = "";

        while (!eof) {
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

    			//hitting Enter at prompt returns a semicolon
                //get rid of it since it returns an error when executed
                if (line.equals(";")) {
                    line = "";
                }

                {
                    boolean retVal = push(line);

                    if (retVal) {
                        out.print(ps2);
                    } else {
                        out.print(ps1);
                    }
                }
            } catch (IOException ex) {
            }
        }
    }    
}
