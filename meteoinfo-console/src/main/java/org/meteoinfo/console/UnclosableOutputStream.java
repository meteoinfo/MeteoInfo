/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yaqia
 */
public class UnclosableOutputStream extends PrintStream {

    public UnclosableOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void close() {
        try {
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(UnclosableOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
