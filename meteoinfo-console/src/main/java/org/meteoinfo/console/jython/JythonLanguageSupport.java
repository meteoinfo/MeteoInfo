/*
 * 01/11/2010
 *
 * Copyright (C) 2011 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.meteoinfo.console.jython;

//import javax.swing.ListCellRenderer;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.meteoinfo.console.autocomplete.AbstractLanguageSupport;

/**
 * Language support for Groovy.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class JythonLanguageSupport extends AbstractLanguageSupport {

    /**
     * The completion provider, shared amongst all text areas.
     */
    private JythonCompletionProvider provider;

    /**
     * Constructor.
     */
    public JythonLanguageSupport() {
        setParameterAssistanceEnabled(true);
        setShowDescWindow(true);
    }

//	/**
//	 * {@inheritDoc}
//	 */
//	protected ListCellRenderer createDefaultCompletionCellRenderer() {
//		return new CCellRenderer();
//	}
    
    /**
     * Get Jython completion provider
     * @return Jython completion provider
     */
    public JythonCompletionProvider getProvider() {
        if (provider == null) {
            provider = new JythonCompletionProvider();
        }
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void install(RSyntaxTextArea textArea) {

        JythonCompletionProvider prov = getProvider();
        AutoCompletion ac = new JythonAutoCompletion(prov);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setParameterAssistanceEnabled(isParameterAssistanceEnabled());
        ac.install(textArea);
        installImpl(textArea, ac);

        textArea.setToolTipSupplier(prov);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstall(RSyntaxTextArea textArea) {
        uninstallImpl(textArea);
    }

}
