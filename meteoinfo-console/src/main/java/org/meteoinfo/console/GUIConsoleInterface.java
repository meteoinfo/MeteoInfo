/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console;

import java.awt.*;

/**
 *
 * @author yaqiang
 */
public abstract interface GUIConsoleInterface extends ConsoleInterface {
    public abstract void print(Object paramObject, Color paramColor);

  public abstract void setNameCompletion(NameCompletion paramNameCompletion);

  public abstract void setWaitFeedback(boolean paramBoolean);
}
