/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.desktop.config;

import org.python.util.PythonInterpreter;

/**
 *
 * @author yaqiang
 */
public class UPythonInterpreter extends PythonInterpreter{
    
    public UPythonInterpreter(){
        this.cflags.source_is_utf8 = true;
        this.cflags.encoding = "utf-8";
    }
    
}
