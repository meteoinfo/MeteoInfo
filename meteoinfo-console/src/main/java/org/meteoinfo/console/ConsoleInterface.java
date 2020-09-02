/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

import java.io.PrintStream;
import java.io.Reader;

/**
 *
 * @author yaqiang
 */
public abstract interface ConsoleInterface {

    public abstract Reader getIn();

    public abstract PrintStream getOut();

    public abstract PrintStream getErr();

    public abstract void println(Object paramObject);

    public abstract void print(Object paramObject);

    public abstract void error(Object paramObject);
}
