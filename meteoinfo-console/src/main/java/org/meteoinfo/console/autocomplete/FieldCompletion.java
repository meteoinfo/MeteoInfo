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
package org.meteoinfo.console.autocomplete;

import org.fife.ui.autocomplete.CompletionProvider;
import org.meteoinfo.console.jython.AbstractJythonSourceCompletion;

import java.awt.*;

/**
 * A completion for a Java field. This completion gets its information from one
 * of two sources:
 *
 * <ul>
 * <li>A {@link FieldInfo} instance, which is loaded by parsing a class file.
 * This is used when this completion represents a field found in a compiled
 * library.</li>
 * <li>A {@link Field} instance, which is created when parsing a Java source
 * file. This is used when the completion represents a field found in uncompiled
 * source, such as the source in an
 * <tt>RSyntaxTextArea</tt>, or in a loose file on disk.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FieldCompletion extends AbstractJythonSourceCompletion
        implements MemberCompletion {

    public FieldCompletion(CompletionProvider provider, String replacementText) {
        super(provider, replacementText);
    }

    @Override
    public void rendererText(Graphics g, int x, int y, boolean selected) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getEnclosingClassName(boolean fullyQualified) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSignature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDeprecated() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
