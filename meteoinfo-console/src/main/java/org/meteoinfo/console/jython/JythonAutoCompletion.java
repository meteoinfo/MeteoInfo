/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console.jython;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 *
 * @author Yaqiang Wang
 */
public class JythonAutoCompletion extends AutoCompletion {

    /**
     * Constructor
     *
     * @param provider Completion provider
     */
    public JythonAutoCompletion(CompletionProvider provider) {
        super(provider);
    }

    @Override
    protected int refreshPopupWindow() {
        return super.refreshPopupWindow();
    }

}
