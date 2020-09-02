/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.console.jython;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;

/**
 *
 * @author Yaqiang Wang
 */
public class JythonCompletionProvider extends LanguageAwareCompletionProvider {

    /**
     * Constructor.
     */
    public JythonCompletionProvider() {
        setDefaultCompletionProvider(createCodeCompletionProvider());
        setStringCompletionProvider(createStringCompletionProvider());
        setCommentCompletionProvider(createCommentCompletionProvider());
    }

    /**
     * Returns the provider to use when editing code.
     *
     * @return The provider.
     * @see #createCommentCompletionProvider()
     * @see #createStringCompletionProvider()
     */
    protected CompletionProvider createCodeCompletionProvider() {
        CompletionProvider cp = new JythonSourceCompletionProvider();
        return cp;
    }

    /**
     * Returns the provider to use when in a comment.
     *
     * @return The provider.
     * @see #createCodeCompletionProvider()
     * @see #createStringCompletionProvider()
     */
    protected CompletionProvider createCommentCompletionProvider() {
        DefaultCompletionProvider cp = new DefaultCompletionProvider();
        cp.addCompletion(new BasicCompletion(cp, "TODO:", "A to-do reminder"));
        cp.addCompletion(new BasicCompletion(cp, "FIXME:", "A bug that needs to be fixed"));
        return cp;
    }

    /**
     * Returns the completion provider to use when the caret is in a string.
     *
     * @return The provider.
     * @see #createCodeCompletionProvider()
     * @see #createCommentCompletionProvider()
     */
    public CompletionProvider createStringCompletionProvider() {
        DefaultCompletionProvider cp = new DefaultCompletionProvider();
        cp.addCompletion(new BasicCompletion(cp, "%c", "char", "Prints a character"));
        cp.addCompletion(new BasicCompletion(cp, "%i", "signed int", "Prints a signed integer"));
        cp.addCompletion(new BasicCompletion(cp, "%f", "float", "Prints a float"));
        cp.addCompletion(new BasicCompletion(cp, "%s", "string", "Prints a string"));
        cp.addCompletion(new BasicCompletion(cp, "%u", "unsigned int", "Prints an unsigned integer"));
        cp.addCompletion(new BasicCompletion(cp, "\\n", "Newline", "Prints a newline"));
        cp.addCompletion(new BasicCompletion(cp, "abstract"));
        cp.addCompletion(new BasicCompletion(cp, "assert"));
        cp.addCompletion(new BasicCompletion(cp, "break"));
        cp.addCompletion(new BasicCompletion(cp, "case"));
        cp.addCompletion(new BasicCompletion(cp, "for"));
        cp.addCompletion(new BasicCompletion(cp, "if"));
        // ... etc ...
        cp.addCompletion(new BasicCompletion(cp, "transient"));
        cp.addCompletion(new BasicCompletion(cp, "try"));
        cp.addCompletion(new BasicCompletion(cp, "void"));
        cp.addCompletion(new BasicCompletion(cp, "volatile"));
        cp.addCompletion(new BasicCompletion(cp, "while"));
        return cp;
    }

}
