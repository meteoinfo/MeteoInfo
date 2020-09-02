/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.meteoinfo.console.jython;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.meteoinfo.console.jython.JythonSourceCompletion;

import javax.swing.text.JTextComponent;


/**
 * Base class for Java source completions.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractJythonSourceCompletion extends BasicCompletion
									implements JythonSourceCompletion {


	public AbstractJythonSourceCompletion(CompletionProvider provider,
										String replacementText) {
		super(provider, replacementText);
	}


	/**
	 * Overridden to ensure that two completions don't just have the same
	 * text value (ignoring case), but that they're of the same "type" of
	 * <code>Completion</code> as well, so, for example, a completion for the
	 * "String" class won't clash with a completion for a "string" LocalVar.
	 *
	 * @param c2 Another completion instance.
	 * @return How this completion compares to the other one.
	 */
	@Override
	public int compareTo(Completion c2) {

		int rc = -1;

		if (c2==this) {
			rc = 0;
		}

		else if (c2!=null) {
			rc = toString().compareToIgnoreCase(c2.toString());
			if (rc==0) { // Same text value
				String clazz1 = getClass().getName();
				clazz1 = clazz1.substring(clazz1.lastIndexOf('.'));
				String clazz2 = c2.getClass().getName();
				clazz2 = clazz2.substring(clazz2.lastIndexOf('.'));
				rc = clazz1.compareTo(clazz2);
			}
		}

		return rc;

	}


	@Override
	public String getAlreadyEntered(JTextComponent comp) {
		String temp = getProvider().getAlreadyEnteredText(comp);
		int lastDot = temp.lastIndexOf('.');
		if (lastDot>-1) {
			temp = temp.substring(lastDot+1);
		}
		return temp;
	}


}