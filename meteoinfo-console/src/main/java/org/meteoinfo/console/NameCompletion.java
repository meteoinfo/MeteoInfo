/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

/**
 *
 * @author yaqiang
 */
public abstract interface NameCompletion {
    public abstract String[] completeName(String paramString);
    public abstract String[] getTip(String paramString);
}
